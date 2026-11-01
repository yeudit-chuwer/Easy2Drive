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
 * Servlet implementation for adding a new student to the system.
 * Handles duplicate checks for phone numbers and student names per teacher.
 */
@WebServlet("/AddStudentServlet")
public class AddStudentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;


    /**
     * Processes student registration requests.
     * Logic: Validate input -> Check phone duplicate -> Check name duplicate -> Link to teacher -> Save.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        
        // Extract parameters sent from the Teacher Dashboard
        String teacherPhone = request.getParameter("teacherPhone");
        String name = request.getParameter("name");
        String studentPhone = request.getParameter("phone");

        //Ensure required fields are not null or empty strings
        if (name == null || name.trim().isEmpty() || studentPhone == null || studentPhone.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("שגיאה: שם ומספר טלפון הם שדות חובה.");
            return;
        }

        EntityManager em = JPAUtil.getEntityManager();
        
        try {
        	//A phone number must be unique across the entire system (used as unique login key)
            Long phoneCount = em.createQuery("SELECT COUNT(s) FROM Student s WHERE s.phone = :p", Long.class)
                               .setParameter("p", studentPhone)
                               .getSingleResult();

            if (phoneCount > 0) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);// Error 409
                response.getWriter().write("שגיאה: מספר הטלפון הזה כבר רשום במערכת.");
                return;
            }

            // Prevents a specific teacher from having two students with the exact same name.
            Long nameCount = em.createQuery("SELECT COUNT(s) FROM Student s WHERE s.name = :n AND s.teacher.phone = :tp", Long.class)
                               .setParameter("n", name.trim())
                               .setParameter("tp", teacherPhone)
                               .getSingleResult();

            if (nameCount > 0) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);// Error 409
                response.getWriter().write("כבר קיים אצלך תלמיד בשם '" + name + "'. אנא הוסף שם משפחה.");
                return;
            }

            //Start database transaction
            em.getTransaction().begin();

            //If this phone number belonged to a deleted student, we clear old message logs to ensure the new student starts with a clean chat history. 
            String deleteSql = "DELETE FROM messages WHERE student_id = ?";
            em.createNativeQuery(deleteSql)
              .setParameter(1, studentPhone)
              .executeUpdate();

            //Retrieve the Teacher entity based on the provided phone number
            Teacher teacher = em.createQuery("SELECT t FROM Teacher t WHERE t.phone = :p", Teacher.class)
                                 .setParameter("p", teacherPhone)
                                 .getSingleResult();

            //Creating and saving a new student object
            model.Student newStudent = new model.Student();
            newStudent.setName(name.trim());
            newStudent.setPhone(studentPhone.trim());
            newStudent.setTeacher(teacher); //Foreign Key association

            em.persist(newStudent);
            
            //Finalize the database changes
            em.getTransaction().commit();
            
            response.setStatus(200);
            response.getWriter().write("התלמיד נוסף בהצלחה.");

        } catch (Exception e) {
        	//Error Handling: Rollback any database changes if an exception occurs
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace(); 
            // Send a standard 500 error to trigger the system's global error page
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	// Crucial: Always close the Entity Manager to prevent memory leaks
        	if (em != null) {
        	    em.close();
        	}
        }
    }
}