package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.*;
import model.Message;
import java.io.IOException;
import java.util.List;
import model.Teacher;
import model.Student;
import util.JPAUtil;

/**
 * MESSAGE DISPATCHER SERVLET
 * This servlet handles sending messages from a teacher to one or multiple students.
 * It supports a "Broadcast" feature where a teacher can send a single message to all
 * their registered students simultaneously.
 */
@WebServlet("/SendMessageServlet")
public class SendMessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	/**
     * Processes POST requests to send messages.
     * Logic: Identify Recipients (Single/Multiple/All) -> Format Content -> Persist Messages.
     */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    request.setCharacterEncoding("UTF-8");
	    String teacherPhone = request.getParameter("phone");
	    String recipientParam = request.getParameter("recipient"); 
	    String content = request.getParameter("content");

	    EntityManager em = JPAUtil.getEntityManager();
	    try {
	        em.getTransaction().begin();

	        String[] recipients;
	        
	        //Broadcast logic: If 'All' is selected, every student associated with this teacher should be retrieved
	        if ("all".equals(recipientParam)) {
	            //Using a native subquery to find all students belonging to the teacher's ID
	            Query q = em.createNativeQuery("SELECT phone FROM students WHERE teacher_id = (SELECT teacher_id FROM teachers WHERE phone = ?)");
	            q.setParameter(1, teacherPhone);
	            List<Object> phoneList = q.getResultList();
	            
	            recipients = new String[phoneList.size()];
	            for (int i = 0; i < phoneList.size(); i++) {
	                recipients[i] = phoneList.get(i).toString();
	            }
	        } else {
	        	//Standard logic: split the comma-separated string of phone numbers
	            recipients = recipientParam.split(",");
	        }

	        //Iterate through the final list of recipients and persist each message
	        for (String sPhone : recipients) {
	            String trimmedPhone = sPhone.trim();
	            if (trimmedPhone.isEmpty()) continue;

	            Message msg = new Message();

	         
	         Teacher t = em.createQuery("SELECT t FROM Teacher t WHERE t.phone = :p", Teacher.class)
	                       .setParameter("p", teacherPhone).getSingleResult();
	         Student s = em.createQuery("SELECT s FROM Student s WHERE s.phone = :p", Student.class)
	                       .setParameter("p", trimmedPhone).getSingleResult();

	         msg.setTeacher(t);  
	         msg.setStudent(s); 
	            
	            //UI Visual Cue: Add a megaphone icon if the message was part of a broadcastד
	            if (recipients.length > 1) {
	                msg.setContent("📢 " + content);
	            } else {
	                msg.setContent(content);
	            }
	            em.persist(msg);
	        }

	        em.getTransaction().commit();
	        response.setStatus(HttpServletResponse.SC_OK);
	    } catch (Exception e) {
	    	// Ensure data integrity by rolling back if any message fails to save
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