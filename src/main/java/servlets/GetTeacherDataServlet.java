package servlets;

import jakarta.persistence.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Teacher;
import java.io.IOException;
import util.JPAUtil;

/**
 * TEACHER CONFIGURATION PROVIDER
 * Retrieves the full profile and business settings for a teacher.
 * This data is used to initialize the teacher dashboard and enforce booking rules.
 */
@WebServlet("/GetTeacherDataServlet")
public class GetTeacherDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    

    /**
     * Handles GET requests to fetch teacher settings.
     * Response Format: JSON containing name, duration, price, and policy limits.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String phone = request.getParameter("phone");
        EntityManager em = JPAUtil.getEntityManager();
        response.setContentType("application/json;charset=UTF-8");
        
        try {
        	//Query the database for the Teacher entity using their phone number
            Teacher t = em.createQuery("SELECT t FROM Teacher t WHERE t.phone = :p", Teacher.class)
                          .setParameter("p", phone)
                          .getSingleResult();
            
            //Using JSONObject for cleaner and safer JSON construction compared to manual String formatting. This includes all business logic constraints (lesson price, cancellation hours, etc.).
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("name", t.getName());
            json.put("lessonDuration", t.getLessonDuration());
            json.put("lessonPrice", t.getLessonPrice());
            json.put("cancelHours", t.getCancelHours());
            json.put("maxLessons", t.getMaxLessons());
            json.put("bookingRange", t.getBookingRange());
            json.put("reminderHours", t.getReminderHours());
            
            response.getWriter().write(json.toString());
        } catch (NoResultException e) {
        	// Case: Teacher not found - Return 404
            response.setStatus(404);
        } catch (Exception e) {
            //Logs the error and triggers the global error.html page defined in web.xml.
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	// Resource Cleanup: Ensure EntityManager is closed
        	if (em != null) {
                em.close();
            }
        }
    }
}