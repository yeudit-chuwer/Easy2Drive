package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.*;
import model.Lesson;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import util.JPAUtil;
/**
 * STUDENT LESSONS RETRIEVER
 * Fetches all active (non-cancelled) lessons for a specific student.
 * Used to populate the student's personal calendar dashboard.
 */
@WebServlet("/GetStudentLessonsServlet")
public class GetStudentLessonsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	
	/**
     * Handles GET requests to retrieve lesson data.
     * Logic: Subquery by phone -> Filter by status -> Format Time -> Return JSON Array.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        // Initialize EntityManager
        EntityManager em = JPAUtil.getEntityManager();
        
        try {
            String phone = request.getParameter("phone");
            //We use a subquery to find the student_id associated with the provided phone.
            //This prevents potential NullPointerExceptions if the student object isn't fully loaded.
            String sql = "SELECT lesson_id, lesson_date, start_time FROM lessons " +
                         "WHERE student_id = (SELECT student_id FROM students WHERE phone = ?) " +
                         "AND status <> 'cancelled'";
            
            Query q = em.createNativeQuery(sql);
            q.setParameter(1, phone);
            List<Object[]> results = q.getResultList();

            JSONArray arr = new JSONArray();
            for (Object[] row : results) {
            	//// Safety check: Skip corrupted records without a date
                if (row[1] == null) continue; 
                
                JSONObject obj = new JSONObject();
                obj.put("id", row[0].toString());
                obj.put("date", row[1].toString()); //lesson_date (YYYY-MM-DD)
                // Extract only HH:mm from the start_time timestamp (e.g., "14:30").
                obj.put("time", row[2].toString().substring(0, 5)); 
                arr.put(obj);
            }
            //Send the JSON array back to the frontend
            response.getWriter().write(arr.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	if (em != null) {
                em.close();
            }
        }
    }
}