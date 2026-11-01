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

//Handles the removal of a scheduled lesson from the database.
@WebServlet("/DeleteLessonServlet")
public class DeleteLessonServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	
	/**
     * Processes DELETE requests to remove a lesson by its unique ID.
     * Logic: Validate ID -> Find Entity -> Transaction Begin -> Remove -> Commit.
     */
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Retrieve the lesson ID from request parameters
    	String idStr = request.getParameter("id");
        //Validate the parameter
    	if (idStr == null) {
            response.setStatus(400);
            return;
        }

    	// Initialize JPA Entity Manager
    	EntityManager em = JPAUtil.getEntityManager();
        try {
        	//Start a database transaction for safe removal
            em.getTransaction().begin();
            //Find the lesson entity in the database
            Lesson lesson = em.find(Lesson.class, Integer.parseInt(idStr));
            if (lesson != null) {
            	// Case: Lesson found - Proceed with deletion
                em.remove(lesson);
                em.getTransaction().commit();
                response.setStatus(200);
            } else {
            	// Case: Lesson not found
            	if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                response.setStatus(404);
            }
        } catch (Exception e) {
            //If a database error occurs, we must rollback the transaction 
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            // Send a standard 500 error to the client
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	// Always close the Entity Manager to prevent memory leaks
        	if (em != null) {
                em.close();
            }
        }
    }
}