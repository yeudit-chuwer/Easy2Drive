package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.*;
import model.Teacher;
import model.WorkDay;
import model.Lesson;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import util.JPAUtil;

/**
 * WORK DAY DETAILS PROVIDER
 * This servlet retrieves all details for a specific work day, including:
 * 1. Working hours and breaks.
 * 2. All booked lessons with student names.
 * 3. Calculation of remaining free slots for that day, excluding breaks.
 */
@WebServlet("/GetWorkDayDetailsServlet")
public class GetWorkDayDetailsServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

    /**
     * Handles GET requests to provide a full snapshot of a specific date.
     * Logic: Identify Teacher -> Fetch WorkDay -> Map Lessons -> Calculate Sync-Safe Availability.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String teacherPhone = request.getParameter("phone");
        String dateStr = request.getParameter("date");

        EntityManager em = JPAUtil.getEntityManager();

        try {
        	//Validate essential parameters
            if (teacherPhone == null || dateStr == null) {
                response.getWriter().write("{\"exists\": false}");
                return;
            }

            //Retrieve the teacher profile via phone number
            LocalDate date = LocalDate.parse(dateStr);

            Teacher teacher = em.createQuery("SELECT t FROM Teacher t WHERE t.phone = :p", Teacher.class)
                                .setParameter("p", teacherPhone)
                                .getSingleResult();

            //Check for defined working hours on the requested date
            TypedQuery<WorkDay> wdQuery = em.createQuery(
                "SELECT wd FROM WorkDay wd WHERE wd.teacher.teacherId = :tId AND wd.work_date = :wDate", WorkDay.class);
            wdQuery.setParameter("tId", teacher.getTeacherId());
            wdQuery.setParameter("wDate", date);
            
            List<WorkDay> workDays = wdQuery.getResultList();

            JSONObject res = new JSONObject();
            if (workDays.isEmpty()) {
                res.put("exists", false);
            } else {
                WorkDay wd = workDays.get(0);
                res.put("exists", true);
                res.put("start_time", wd.getStart_time().toString());
                res.put("end_time", wd.getEnd_time().toString());
                res.put("break_start", wd.getBreak_start() != null ? wd.getBreak_start() : "");
                res.put("break_duration", wd.getBreak_duration());

                // Boundary break calculation: determining precise times for overlap detection
                LocalTime breakStart = null;
                LocalTime breakEnd = null;
                if (wd.getBreak_start() != null && !wd.getBreak_start().isEmpty()) {
                    breakStart = LocalTime.parse(wd.getBreak_start());
                    breakEnd = breakStart.plusMinutes(wd.getBreak_duration());
                }

                //Retrieve all scheduled lessons for the day
                TypedQuery<Lesson> lessonQuery = em.createQuery(
                    "SELECT l FROM Lesson l WHERE l.teacher.teacherId = :tId AND l.date = :lDate", Lesson.class);
                lessonQuery.setParameter("tId", teacher.getTeacherId());
                lessonQuery.setParameter("lDate", date);
                List<Lesson> bookedLessons = lessonQuery.getResultList();

                JSONArray lessonsArr = new JSONArray();
                for (Lesson l : bookedLessons) {
                    JSONObject lo = new JSONObject();
                    lo.put("student_name", l.getStudent().getName());
                    lo.put("start_time", l.getStart_time().toString());
                    //Calculate end time dynamically using teacher's lesson duration setting
                    lo.put("end_time", l.getStart_time().toLocalTime().plusMinutes(teacher.getLessonDuration()).toString());
                    lessonsArr.put(lo);
                }
                res.put("lessons", lessonsArr);

                //Capacity logic: Overriding working hours and skipping overlapping slots with breaks
                int duration = teacher.getLessonDuration() > 0 ? teacher.getLessonDuration() : 45;
                int totalPossibleSlots = 0;
                LocalTime currentTime = wd.getStart_time();

                while (!currentTime.plusMinutes(duration).isAfter(wd.getEnd_time())) {
                    LocalTime slotStart = currentTime;
                    LocalTime slotEnd = currentTime.plusMinutes(duration);

                    boolean isInsideBreak = false;
                    if (breakStart != null) {
                    	//Overlap check: A slot is invalid if it starts before the end of the break and also ends after the start of the break
                        if (slotStart.isBefore(breakEnd) && slotEnd.isAfter(breakStart)) {
                            isInsideBreak = true;
                        }
                    }

                    //Only count slots that are fully outside the break period
                    if (!isInsideBreak) {
                        totalPossibleSlots++;
                    }
                    currentTime = currentTime.plusMinutes(duration);
                }

                //Return synchronized stats for teacher/student UI alignment
                res.put("totalPossibleSlots", totalPossibleSlots); 
                res.put("freeSlots", Math.max(0, totalPossibleSlots - bookedLessons.size()));
            }

            // Write the final JSON response
            response.getWriter().write(res.toString());

        } catch (NoResultException e) {
        	//Return 'false' if no teacher/data is found
            response.getWriter().write("{\"exists\": false}");
        } catch (Exception e) {
            e.printStackTrace();
            //Official 500 error code for internal failures
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	//Safe resource cleanup to prevent connection leaks
            if (em != null) {
                em.close();
            }
        }
    }
}