package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.*;
import model.Teacher;
import java.io.IOException;
import util.JPAUtil;

/**
 * TEACHER SETTINGS UPDATER
 * This servlet handles the modification of teacher profile settings, 
 * including lesson pricing, durations, cancellation policies, and booking ranges.
 */
@WebServlet("/UpdateTeacherSettingsServlet")
public class UpdateTeacherSettingsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	
	/**
     * Processes POST requests to update teacher configurations.
     * Logic: Identify Teacher -> Parse parameters -> Merge changes into DB.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String phone = request.getParameter("phone");
        
        EntityManager em = JPAUtil.getEntityManager();
        
        try {
            em.getTransaction().begin();

            //Retrieve the teacher profile from the database
            Teacher t = em.createQuery("SELECT t FROM Teacher t WHERE t.phone = :p", Teacher.class)
                          .setParameter("p", phone)
                          .getSingleResult();

            //Update all fields based on the incoming parameters from the UI
            t.setName(request.getParameter("name"));
            t.setLessonPrice(Double.parseDouble(request.getParameter("price")));
            t.setLessonDuration(Integer.parseInt(request.getParameter("duration")));
            t.setCancelHours(Integer.parseInt(request.getParameter("cancel_hours")));
            t.setMaxLessons(Integer.parseInt(request.getParameter("max_lessons")));
            t.setBookingRange(Integer.parseInt(request.getParameter("booking_range")));
            
            
            String reminder = request.getParameter("reminder_hours");
            if (reminder != null && !reminder.isEmpty()) {
                t.setReminderHours(Integer.parseInt(reminder));
            }

            //Final merge and commit
            em.merge(t); 
            em.getTransaction().commit();
            response.setStatus(200);
            
        } catch (NoResultException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Teacher not found");
        } catch (Exception e) {
            // Safety rollback in case of parsing errors or DB connection issues
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            // Standard cleanup
            if (em != null) {
                em.close();
            }
        }
    }
}