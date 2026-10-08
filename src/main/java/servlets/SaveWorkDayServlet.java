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
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import util.JPAUtil;

/**
 * WORK DAY SAVER & SCHEDULE UPDATER
 * This servlet handles the saving or updating of a teacher's work day.
 * It includes critical business logic: if work hours are shortened, it automatically
 * cancels overlapping lessons and notifies the relevant students via a system message.
 */
@WebServlet("/SaveWorkDayServlet")
public class SaveWorkDayServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	/**
     * Processes POST requests to update the work schedule.
     * Workflow: Validate Inputs -> Check for Overlapping Lessons -> Delete Invalid Lessons -> Update WorkDay.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        
        try {
        	//Extract Request Parameters
            String phone = request.getParameter("phone");
            String dateStr = request.getParameter("date");
            String startStr = request.getParameter("start");
            String endStr = request.getParameter("end");

            LocalTime newStart = LocalTime.parse(startStr);
            LocalTime newEnd = LocalTime.parse(endStr);
            LocalDate workDate = LocalDate.parse(dateStr);

            //Ensure start time is before end time
            if (newStart.isAfter(newEnd) || newStart.equals(newEnd)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("שעת הסיום חייבת להיות אחרי שעת ההתחלה.");
                return;
            }

            EntityManager em = JPAUtil.getEntityManager();
            
            try {
                em.getTransaction().begin();

                //Identify the teacher profile
                Teacher teacher = em.createQuery("SELECT t FROM Teacher t WHERE t.phone = :p", Teacher.class)
                                    .setParameter("p", phone)
                                    .getSingleResult();

                //Checking and deleting extra lessons + sending a message
                TypedQuery<Lesson> qL = em.createQuery("SELECT l FROM Lesson l WHERE l.teacher.teacherId = :tId AND l.date = :d", Lesson.class);
                qL.setParameter("tId", teacher.getTeacherId());
                qL.setParameter("d", workDate);
                List<Lesson> lessons = qL.getResultList();
                
                LocalTime currentTime = LocalTime.now(); 
                LocalDate today = LocalDate.now();
                
                for (Lesson l : lessons) {
                	LocalTime lessonStart = l.getStart_time().toLocalTime();
                	// Calculate lesson end time based on the teacher's specific lesson duration
                    LocalTime lessonEnd = lessonStart.plusMinutes(teacher.getLessonDuration()); 

                    if (workDate.equals(today) && lessonStart.isBefore(currentTime)) {
                        continue; 
                    }

                    //Check if the lesson starts before the new opening hour OR ends after the new closing hour.
                    if (lessonStart.isBefore(newStart) || lessonEnd.isAfter(newEnd)) {

                    	// A. Insert Automated Cancellation Message using numerical IDs for Chat Hub compatibility
                        Query msgQuery = em.createNativeQuery(
                            "INSERT INTO messages (teacher_id, student_id, content, sent_at, is_read) " +
                            "VALUES (?, ?, ?, NOW(), 0)"
                        );
                        
                       
                        msgQuery.setParameter(1, teacher.getTeacherId());
                        msgQuery.setParameter(2, l.getStudent().getStudentId());
                        msgQuery.setParameter(3, "(הודעה אוטומטית) השיעור שנקבע לתאריך " + dateStr + 
                                                 " בשעה " + l.getStart_time() + " בוטל עקב שינוי בלו\"ז המורה.");
                        
                        msgQuery.executeUpdate(); 

                        // B. Remove the conflicting lesson from the database
                        em.remove(l);
                    }
                }
                //Update workday configuration
                TypedQuery<WorkDay> qW = em.createQuery("SELECT w FROM WorkDay w WHERE w.teacher.teacherId = :tId AND w.work_date = :d", WorkDay.class);
                qW.setParameter("tId", teacher.getTeacherId());
                qW.setParameter("d", workDate);
                List<WorkDay> results = qW.getResultList();

                WorkDay wd = results.isEmpty() ? new WorkDay() : results.get(0);
                wd.setTeacher(teacher);
                wd.setWork_date(workDate);
                wd.setStart_time(newStart);
                wd.setEnd_time(newEnd);
                
                String bStart = request.getParameter("break_start");
                wd.setBreak_start(bStart != null ? bStart : "");
                
                String bDur = request.getParameter("break_duration");
                wd.setBreak_duration(bDur != null && !bDur.isEmpty() ? Integer.parseInt(bDur) : 0);

                //Persist new entry or merge existing one
                if (results.isEmpty()) {
                    em.persist(wd);
                } else {
                    em.merge(wd);
                }
                
                em.getTransaction().commit();
                response.setStatus(200);
                response.getWriter().write("הלו\"ז עודכן בהצלחה.");

            } catch (Exception e) {
            	//Rollback transaction to maintain data integrity on failure
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } finally {
            	if (em != null) {
            		em.close();
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}