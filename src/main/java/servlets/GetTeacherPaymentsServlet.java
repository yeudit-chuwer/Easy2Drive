package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.*;
import model.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import util.JPAUtil;

/**
 * TEACHER BILLING & PAYMENT TRACKER
 * Retrieves all lessons associated with a teacher to display their financial status.
 * Used by the Teacher Dashboard to monitor paid and pending earnings.
 */
@WebServlet("/GetTeacherPaymentsServlet")
public class GetTeacherPaymentsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	/**
     * Handles GET requests for financial records.
     * Logic: Query lessons by teacher phone -> Sort by most recent -> Build detailed JSON array.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        EntityManager em = JPAUtil.getEntityManager();

        try {
            String phone = request.getParameter("phone");
            
            // JPQL query Retrieves all lessons for the teacher, sorted by date and time (descending order).
            TypedQuery<Lesson> query = em.createQuery(
                "SELECT l FROM Lesson l WHERE l.teacher.phone = :p ORDER BY l.date DESC, l.start_time DESC", Lesson.class);
            query.setParameter("p", phone);
            
            List<Lesson> lessons = query.getResultList();
            JSONArray arr = new JSONArray();
            
            //Transform each Lesson entity into a JSON object for the frontend table
            for (Lesson l : lessons) {
                JSONObject obj = new JSONObject();
                obj.put("id", l.getLessonId());
                //Student information with fallback for unassigned slots
                obj.put("student", l.getStudent() != null ? l.getStudent().getName() : "תלמיד כללי");
                //Date and Time formatting
                obj.put("date", l.getDate() != null ? l.getDate().toString() : "");
                obj.put("time", l.getStart_time() != null ? l.getStart_time().toString() : "");
                //Financial details
                obj.put("amount", l.getPrice() != null ? l.getPrice() : 0.0);
                
                //Payment Stattus: Can be 'paid', 'pending', 'scheduled', or 'cancelled'.
                obj.put("status", l.getStatus() != null ? l.getStatus() : "pending");
                
                arr.put(obj);
            }
         // Return the complete list as a JSON string
            response.getWriter().write(arr.toString());

        } catch (Exception e) {
            e.printStackTrace();
           //Triggers the global error.html page defined in web.xml.
           response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	if (em != null) {
                em.close();
            }
        }
    }
}