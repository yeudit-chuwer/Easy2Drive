package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.*;
import model.*;
import java.io.IOException;
import util.JPAUtil;

@WebServlet("/BookLessonServlet")
public class BookLessonServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	
	/**
     * Handles the lesson booking process.
     * The 'synchronized' keyword is CRITICAL here to prevent Race Conditions.
     * It ensures that only one thread can execute this method at a time, 
     * preventing multiple students from booking the same time slot simultaneously.
     */
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String studentPhone = request.getParameter("studentPhone");
        String dateStr = request.getParameter("date"); 
        String timeStr = request.getParameter("time"); 

        EntityManager em = JPAUtil.getEntityManager();
        
        try {
            System.out.println("DEBUG: Starting booking for phone: " + studentPhone);
            
            //Retrieve Student and Teacher entities
            Student student = em.createQuery("SELECT s FROM Student s WHERE s.phone = :p", Student.class)
                                .setParameter("p", studentPhone).getSingleResult();
            Teacher teacher = student.getTeacher();

            //Calculate Quota Period based on Teacher's booking range
            int rangeDays = teacher.getBookingRange();
            int maxLessons = teacher.getMaxLessons();
            java.time.LocalDate now = java.time.LocalDate.now();

            java.time.LocalDate startPeriod;
            java.time.LocalDate endPeriod;

            //Logic to determine if we are checking a weekly or monthly quota.
            if (rangeDays > 14) {
                //Monthly quota logic
                startPeriod = now.withDayOfMonth(1);
                endPeriod = startPeriod.plusMonths(2).withDayOfMonth(1);
                
                System.out.println("DEBUG: Monthly check from " + startPeriod + " to " + endPeriod);
            } else {
            	// Weekly quota logic (starts from previous Sunday)
                int dayOfWeek = now.getDayOfWeek().getValue() % 7; 
                startPeriod = now.minusDays(dayOfWeek);
                endPeriod = startPeriod.plusDays(rangeDays);
                
                System.out.println("DEBUG: Weekly check from " + startPeriod + " to " + endPeriod);
            }

            //Quota Validation: Ensure student hasn't exceeded their lesson limit.
            String sql = "SELECT COUNT(*) FROM lessons WHERE student_id = ? " +
                         "AND lesson_date >= ? AND lesson_date < ? AND status <> 'cancelled'";
                         
            Query q = em.createNativeQuery(sql);
            q.setParameter(1, student.getStudentId());
            q.setParameter(2, java.sql.Date.valueOf(startPeriod));
            q.setParameter(3, java.sql.Date.valueOf(endPeriod));

            long currentCount = ((Number) q.getSingleResult()).longValue();
            System.out.println("DEBUG: Lessons found in DB: " + currentCount);

            //Ensure student hasn't reached their lesson limit
            if (currentCount >= maxLessons) {
                response.setStatus(400);//Bad Request
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("הגעת למכסה המקסימלית לתקופה הזאת (" + maxLessons + " שיעורים)");
                return;
            }
            
            //Availability Check: Final verification to see if the slot is still free.
            String checkDuplicateSql = "SELECT COUNT(*) FROM lessons WHERE teacher_id = ? " +
                    "AND lesson_date = ? AND start_time = ? AND status <> 'cancelled'";

				Query qCheck = em.createNativeQuery(checkDuplicateSql);
				qCheck.setParameter(1, teacher.getTeacherId());
				qCheck.setParameter(2, java.sql.Date.valueOf(dateStr));
				qCheck.setParameter(3, java.sql.Time.valueOf(timeStr + ":00"));
				
				long duplicateCount = ((Number) qCheck.getSingleResult()).longValue();
				if (duplicateCount > 0) {
				// Return 409 Conflict - The slot is no longer available
				response.setStatus(409); 
				response.setContentType("text/plain;charset=UTF-8");
				response.getWriter().write("מצטערים, השיעור הזה כבר נתפס על ידי תלמיד אחר. נא לבחור שעה אחרת.");
				return;
			}

            //Start transaction and save the lesson
            em.getTransaction().begin();
            Lesson lesson = new Lesson();
            lesson.setStudent(student);
            lesson.setTeacher(teacher);
            lesson.setDate(java.sql.Date.valueOf(dateStr));
            lesson.setStart_time(java.sql.Time.valueOf(timeStr + ":00"));
            
            //Calculate end_time based on teacher settings
            long durationMillis = teacher.getLessonDuration() * 60 * 1000L;
            lesson.setEnd_time(new java.sql.Time(lesson.getStart_time().getTime() + durationMillis));
            
            lesson.setPrice(teacher.getLessonPrice());
            lesson.setStatus("scheduled");
            
            em.merge(lesson);
            em.getTransaction().commit();//Finalize save
           
            
            System.out.println("DEBUG: Lesson saved successfully!");
            
            response.getWriter().write("Success");

        } catch (PersistenceException e) {//Database protection (unique constraint)

        	if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();

        	response.setStatus(409);

        	response.setContentType("text/plain;charset=UTF-8");

        	response.getWriter().write("מצטערים, השיעור בדיוק נתפס על ידי מישהו אחר.");

        	return; 
        }catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace(); 
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	//Always close EntityManager to prevent memory leaks
        	if (em != null) {
        	    em.close();
        	}
        }
    }
}
