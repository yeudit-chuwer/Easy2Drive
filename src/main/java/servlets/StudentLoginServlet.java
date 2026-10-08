package servlets;

import jakarta.persistence.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Student;
import java.io.IOException;
import java.util.List;
import util.JPAUtil;

/**
 * STUDENT LOGIN SERVLET
 * Responsible for authenticating students based on their phone number.
 * Upon successful login, a server-side session is created to maintain the user's state.
 */
@WebServlet("/StudentLoginServlet")
public class StudentLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	

    /**
     * Handles POST requests for student authentication.
     * Logic: Query student by phone -> If exists, create session and redirect to dashboard -> Else, return to login with error.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String phone = request.getParameter("phone");
        EntityManager em = JPAUtil.getEntityManager();

        try {
        	// Secure query to find the student by their unique phone number
            TypedQuery<Student> query = em.createQuery("SELECT s FROM Student s WHERE s.phone = :p", Student.class);
            query.setParameter("p", phone);
            List<Student> students = query.getResultList();

            if (!students.isEmpty()) {
            	// Successful authentication: Initialize a new session
            	jakarta.servlet.http.HttpSession session = request.getSession(); 
                session.setAttribute("loggedInUser", phone);
                session.setMaxInactiveInterval(30 * 60); //Set session timeout to 30 minutes (1800 seconds)
                //Redirect the authenticated student to their dashboard
                response.sendRedirect("student_dashboard.html?phone=" + phone);
            } else {
            	// Authentication failure: Redirect to login with error flag
                response.sendRedirect("student_login.html?error=notfound");
            }
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