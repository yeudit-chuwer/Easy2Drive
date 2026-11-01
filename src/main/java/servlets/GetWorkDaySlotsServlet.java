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
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import util.JPAUtil;

/**
 * AVAILABLE SLOTS GENERATOR
 * This servlet calculates available time slots for booking a lesson.
 * Logic: 
 * 1. Dividies work hours into slots based on lesson duration.
 * 2. Filters out slots that overlap with teacher breaks.
 * 3. Marks slots as 'available: false' if they are already booked.
 */
@WebServlet("/GetWorkDaySlotsServlet")
public class GetWorkDaySlotsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	

	/**
     * Handles GET requests to provide a list of time slots for a specific date.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        EntityManager em = JPAUtil.getEntityManager();

        try {
            String tIdParam = request.getParameter("teacherId");
            String dateStr = request.getParameter("date");

            //Basic validation for missing parameters
            if (tIdParam == null || dateStr == null || tIdParam.equals("undefined")) {
                response.getWriter().write("[]");
                return;
            }

            int teacherId = Integer.parseInt(tIdParam);
            LocalDate date = LocalDate.parse(dateStr);

            // Retrieve Teacher settings (especially lesson duration)
            Teacher teacher = em.find(Teacher.class, teacherId);
            int duration = (teacher != null && teacher.getLessonDuration() > 0) ? teacher.getLessonDuration() : 45;

            //Fetch WorkDay configuration for the selected date
            TypedQuery<WorkDay> wdQuery = em.createQuery(
                "SELECT wd FROM WorkDay wd WHERE wd.teacher.teacherId = :tId AND wd.work_date = :wDate", WorkDay.class);
            wdQuery.setParameter("tId", teacherId);
            wdQuery.setParameter("wDate", date);
            
            List<WorkDay> workDays = wdQuery.getResultList();
            if (workDays.isEmpty()) {
                response.getWriter().write("[]"); //No work day defined = no slots
                return;
            }
            WorkDay workDay = workDays.get(0);

            // Fetch existing lessons to identify occupied slots
            TypedQuery<Lesson> lessonQuery = em.createQuery(
                "SELECT l FROM Lesson l WHERE l.teacher.teacherId = :tId AND l.date = :lDate", Lesson.class);
            lessonQuery.setParameter("tId", teacherId);
            lessonQuery.setParameter("lDate", date);
            List<Lesson> bookedLessons = lessonQuery.getResultList();

            //Extract only the start times of booked lessons for fast lookup
            List<LocalTime> bookedTimes = bookedLessons.stream()
                    .map(lesson -> lesson.getStart_time().toLocalTime())
                    .collect(Collectors.toList());

            //Slot Generation Logic
            JSONArray slotsArr = new JSONArray();
            LocalTime currentTime = workDay.getStart_time();
            LocalTime dayEnd = workDay.getEnd_time();

            //Handle Teacher Break times
            LocalTime breakStart = null;
            LocalTime breakEnd = null;
            if (workDay.getBreak_start() != null && !workDay.getBreak_start().isEmpty()) {
                breakStart = LocalTime.parse(workDay.getBreak_start());
                breakEnd = breakStart.plusMinutes(workDay.getBreak_duration());
            }

            //Creates slots as long as the slot end time doesn't exceed the work day end time.
            while (!currentTime.plusMinutes(duration).isAfter(dayEnd)) {
                LocalTime slotStart = currentTime;
                LocalTime slotEnd = currentTime.plusMinutes(duration);

                //Check for overlap with teacher's break
                boolean isInsideBreak = false;
                if (breakStart != null) {
                    // The lesson begins before the end of the break and ends after the start of the break
                    if (slotStart.isBefore(breakEnd) && slotEnd.isAfter(breakStart)) {
                        isInsideBreak = true;
                    }
                }

                //Only add slot if it's outside the break period
                if (!isInsideBreak) {
                    JSONObject slotObj = new JSONObject();
                    slotObj.put("time", slotStart.toString());
                    
                    //Mark availability based on existing bookings
                    boolean isBooked = bookedTimes.contains(slotStart);
                    slotObj.put("available", !isBooked); 
                    
                    slotsArr.put(slotObj); 
                }

                //Advance currentTime to the start of the next potential slot
                currentTime = currentTime.plusMinutes(duration);
            }
            
            response.getWriter().write(slotsArr.toString());

        } catch (Exception e) {
            e.printStackTrace();
            //Sends an official 500 error to trigger the custom error.html page.
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	if (em != null) {
                em.close();
            }
        }
    }
}