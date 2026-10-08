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
 * STUDENT PAYMENTS & BILLING SERVLET
 * Retrieves the financial status for a specific student.
 * Segregates lessons into "Paid History" and "Unpaid Balance".
 */
@WebServlet("/GetStudentPaymentsServlet")
public class GetStudentPaymentsServlet extends HttpServlet {
	
	
	
	/**
     * Handles GET requests to fetch payment data.
     * Response: A JSON object containing two arrays: 'history' and 'unpaid'.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        EntityManager em = JPAUtil.getEntityManager();
        
        try {
            String phone = request.getParameter("phone");
            
            //Fetches all lessons for the student, ordered by the most recent date.
            TypedQuery<Lesson> query = em.createQuery(
                "SELECT l FROM Lesson l WHERE l.student.phone = :p ORDER BY l.date DESC", Lesson.class);
            query.setParameter("p", phone);
            
            List<Lesson> lessons = query.getResultList();
            
            JSONArray historyArr = new JSONArray();
            JSONArray unpaidArr = new JSONArray();

            // Iterate through lessons to classify payment status
            for (Lesson l : lessons) {
                JSONObject obj = new JSONObject();
                obj.put("date", l.getDate().toString());
                obj.put("time", l.getStart_time().toString());
                obj.put("amount", l.getPrice() != null ? l.getPrice() : 0.0);
                obj.put("status", l.getStatus() != null ? l.getStatus() : "pending");

                //If status is 'paid', it goes to the history dashboard.
                if ("paid".equals(l.getStatus())) {
                    historyArr.put(obj);
                } else {//Otherwise (scheduled, completed, etc.), it is treated as a debt/pending payment.
                    unpaidArr.put(obj);
                }
            }

            // Wrap both arrays into a single result object
            JSONObject result = new JSONObject();
            result.put("history", historyArr);
            result.put("unpaid", unpaidArr);
            
            response.getWriter().write(result.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            //Sends an internal server error to trigger the custom error.html page.
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	//Resource cleanup: Always close EntityManager
        	if (em != null) {
                em.close();
            }
        }
    }
}