package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.*;
import model.Lesson;
import java.io.IOException;
import util.JPAUtil;

/**
 * PAYMENT STATUS UPDATER
 * This servlet handles updating the payment status of a lesson (e.g., from 'pending' to 'paid').
 * It is primarily used by the Teacher Dashboard to track earnings.
 */
@WebServlet("/UpdatePaymentStatusServlet")
public class UpdatePaymentStatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	/**
     * Processes POST requests to change the status of a lesson.
     * Logic: Find Lesson by ID -> Update status string -> Commit Transaction.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String lessonId = request.getParameter("lessonId");
        String status = request.getParameter("status"); //Usually receives "paid"

        // Basic validation
        if (lessonId == null || status == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }
        
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            //Fetch the lesson entity using its primary key
            Lesson lesson = em.find(Lesson.class, Integer.parseInt(lessonId));
            if (lesson != null) {
                lesson.setStatus(status);
             // JPA automatically tracks changes to 'lesson' and updates the DB on commit
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            tx.commit();
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
        	//Rollback ensures that if something fails, the DB state remains consistent
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	if (em != null) {
                em.close();
            }
        }
    }
}