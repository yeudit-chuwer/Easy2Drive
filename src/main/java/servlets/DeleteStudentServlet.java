package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.*;
import java.io.IOException;
import util.JPAUtil;

/**
 * STUDENT DELETION SERVLET
 * This servlet handles the full removal of a student from the system.
 * It performs a "Cascade Delete" manually to maintain database integrity.
 */
@WebServlet("/DeleteStudentServlet")
public class DeleteStudentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    
    
    /**
     * Processes POST requests to delete a student.
     * Logic: Delete Messages -> Delete Lessons -> Delete Student.
     * This order prevents Foreign Key constraint violations.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        //Retrieve the student's unique identifier (phone number) from the request
        String studentPhone = request.getParameter("studentPhone");
        
        //Basic parameter validation
        if (studentPhone == null || studentPhone.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Phone number is missing");
            return;
        }

        EntityManager em = JPAUtil.getEntityManager();
        
        try {
        	//Start a transaction to ensure all deletion steps succeed as one unit
            em.getTransaction().begin();
            
            //Step A: Deletes all chat history associated with this student phone.
            em.createNativeQuery("DELETE FROM messages WHERE student_phone = ?")
              .setParameter(1, studentPhone)
              .executeUpdate();
              
            //Step B: Deletes all scheduled or past lessons linked to the student.
            em.createNativeQuery("DELETE FROM lessons WHERE student_id = (SELECT student_id FROM students WHERE phone = ?)")
              .setParameter(1, studentPhone)
              .executeUpdate();
            
            // Step C: Finally, remove the student profile from the primary 'students' table.
            int deletedRows = em.createNativeQuery("DELETE FROM students WHERE phone = ?")
                                .setParameter(1, studentPhone)
                                .executeUpdate();
            
            if (deletedRows > 0) {
            	// Success: Commit all changes to the database
                em.getTransaction().commit();
                response.setStatus(HttpServletResponse.SC_OK);
                System.out.println("DEBUG: Student with phone " + studentPhone + " was deleted.");
            } else {
            	// Failure: Student wasn't found in the first place
                em.getTransaction().rollback();
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Student not found");
            }
            
        } catch (Exception e) {//If any of the deletion steps fail, we rollback the entire transaction.
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            //Triggers the custom error page defined in web.xml
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//500
        } finally {
        	//Resource Management: Always close the Entity Manager
        	if (em != null) {
                em.close();
            }
        }
    }
}