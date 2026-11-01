package servlets;

import org.json.JSONObject;
import jakarta.persistence.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Student;
import java.io.IOException;
import util.JPAUtil;

/**
 * STUDENT PROFILE PROVIDER
 * This servlet retrieves personal profile data for a specific student using their phone number.
 * It also fetches linked teacher information to initialize the student dashboard.
 */
@WebServlet("/GetStudentDataServlet")
public class GetStudentDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	

    /**
     * Handles GET requests to fetch student profile data.
     * Response Format: JSON
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        EntityManager em = JPAUtil.getEntityManager();
        
        try {
        	// Retrieve the phone number from request parameters
            String phone = request.getParameter("phone");
            
            //Retrieve student by phone
            TypedQuery<Student> query = em.createQuery("SELECT s FROM Student s WHERE s.phone = :p", Student.class);
            query.setParameter("p", phone);
            Student s = query.getSingleResult();
            
            //Build the JSON response object
            JSONObject json = new JSONObject();
            json.put("name", s.getName());
            
            //Check if the student has an assigned teacher.
            if (s.getTeacher() != null) {
                //Include teacher details for scheduling and communication.
                json.put("teacherId", s.getTeacher().getTeacherId());
                json.put("teacherPhone", s.getTeacher().getPhone());
            } else {
            	// Fallback for students without an assigned teacher
                json.put("teacherId", 0);
            }
            
            //Send the JSON string back to the client
            response.getWriter().write(json.toString());
            
        } catch (NoResultException e) {
            //If the phone number is not registered, return a 404 status
            response.setStatus(404);
            JSONObject json = new JSONObject();
            json.put("error", "NOT_REGISTERED");
            
            response.getWriter().write(json.toString());
        } catch (Exception e) {
        	//Triggers for any unexpected backend failures.
        	e.printStackTrace(); 
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
        } finally {
        	// Resource Management: Ensure EntityManager is closed
        	if (em != null) {
                em.close();
            }
        }
    }
}