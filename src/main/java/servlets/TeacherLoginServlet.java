package servlets;

import jakarta.persistence.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Teacher; 
import java.io.IOException;
import java.util.List;
import util.JPAUtil;


/**
 * TEACHER LOGIN & REGISTRATION SERVLET
 * This servlet handles teacher authentication. 
 * Logic:
 * 1. If the teacher exists, create a session and redirect to dashboard.
 * 2. If the teacher is new (NoResultException), automatically create a profile 
 * with default settings and redirect to setup.
 */
@WebServlet("/TeacherLoginServlet")
public class TeacherLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
    	String phone = request.getParameter("phone");
    	EntityManager em = JPAUtil.getEntityManager();

    	try {
    	    Teacher teacher;
    	    try {
    	    	// Attempt to find an existing teacher by phone
    	        teacher = em.createQuery("SELECT t FROM Teacher t WHERE t.phone = :p", Teacher.class)
    	                    .setParameter("p", phone)
    	                    .getSingleResult();
    	        
    	        //Successful authentication: Session initialization
    	        jakarta.servlet.http.HttpSession session = request.getSession();
                session.setAttribute("loggedInUser", phone);
                session.setMaxInactiveInterval(30 * 60);// 30 minutes
    	        
    	        //If we found it - normal entry
    	        response.sendRedirect("teacher_dashboard.html?phone=" + phone);
    	        
    	    } catch (NoResultException e) {
    	    	// New teacher identified: automatic registration process
    	    	em.getTransaction().begin();
    	    	teacher = new Teacher();
    	    	teacher.setPhone(phone);
    	    	teacher.setName("מורה חדש"); 
    	    	teacher.setLessonPrice(150.0); 
    	    	teacher.setLessonDuration(40);
    	    	teacher.setBookingRange(7);

    	    	em.persist(teacher);
    	    	em.getTransaction().commit();
    	    	
    	    	//Initialize session for the newly created teacher
                jakarta.servlet.http.HttpSession session = request.getSession();
                session.setAttribute("loggedInUser", phone);
                session.setMaxInactiveInterval(60 * 30);
    	        
                //Redirect to dashboard with a flag to trigger the settings modal
    	        response.sendRedirect("teacher_dashboard.html?phone=" + phone + "&newTeacher=true");
    	    }
    	} catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	if (em != null) {
                em.close();
            }
    	}
    }
}