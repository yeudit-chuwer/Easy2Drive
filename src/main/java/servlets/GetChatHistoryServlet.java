package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import util.JPAUtil;

//Handles message retrieval, group message aggregation, and read-receipt updates.
@WebServlet("/GetChatHistoryServlet")
public class GetChatHistoryServlet extends HttpServlet {
	
	
	
	/**
     * GET method handles two main actions: 
     * 1. Fetching chat history (JSON format).
     * 2. Marking messages as read (triggered by UI interactions).
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    response.setContentType("application/json;charset=UTF-8");
	    String myPhone = request.getParameter("phone");
	    String role = request.getParameter("role");
	    String action = request.getParameter("action");

	    EntityManager em = JPAUtil.getEntityManager();
	    
	    try {
	    	//Updates the database to clear notification badges for either teacher or student.
	    	if ("markRead".equals(action)) {
	    	    try {
	    	        em.getTransaction().begin();
	    	        String updateSql = "";
	    	        
	    	        if ("teacher".equals(role)) {
	    	        	// Mark messages coming FROM students as read
	    	        	updateSql = "UPDATE messages SET is_read = 1 WHERE teacher_id = (SELECT teacher_id FROM teachers WHERE phone = ?) AND content LIKE '(הודעה מתלמיד)%'";
	    	         
	    	        } else {
	    	        	// Mark messages coming FROM teacher (private or broadcast) as read
	    	        	updateSql = "UPDATE messages SET is_read = 1 WHERE student_id = (SELECT student_id FROM students WHERE phone = ?) AND content NOT LIKE '(הודעה מתלמיד)%'";
	    	        }
	    	        
	    	        Query updateQuery = em.createNativeQuery(updateSql);
	    	        updateQuery.setParameter(1, myPhone);
	    	        updateQuery.executeUpdate();
	    	        em.getTransaction().commit();
	    	        
	    	        System.out.println("DEBUG: markRead executed for " + role + " phone: " + myPhone);
	    	        response.setStatus(HttpServletResponse.SC_OK);
	    	        return; 
	    	    } catch (Exception ex) {
	    	        if (em.getTransaction().isActive()) em.getTransaction().rollback();
	    	        ex.printStackTrace();
	    	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    	        return;
	    	    } 
	        }

	        
	     
	        String sql = "";
	        if ("teacher".equals(role)) {
	            //Merges identical messages sent to multiple students at the same time. If more than 3 students received it, it's labeled as a "📢 Broadcast"
	        	sql = "SELECT m.content, m.sent_at, m.is_read, " +
	        		      "CASE WHEN COUNT(DISTINCT m.student_id) > 3 THEN '📢 הודעה קבוצתית' " +
	        		      "     ELSE GROUP_CONCAT(DISTINCT s.name SEPARATOR ', ') END as partyName, " +
	        		      "CASE WHEN m.content LIKE '(הודעה מתלמיד)%' THEN 'in' ELSE 'out' END as direction " +
	        		      "FROM messages m LEFT JOIN students s ON m.student_id = s.student_id " +
	        		      "WHERE m.teacher_id = (SELECT teacher_id FROM teachers WHERE phone = ?) " +
	        		      "GROUP BY m.content, m.sent_at, m.is_read, direction " + 
	        		      "ORDER BY m.sent_at ASC";
	        } else {
	        	// STUDENT VIEW: Simple linear history of their own messages and teacher replies.
	        	sql = "SELECT m.content, m.sent_at, m.is_read, 'המורה' as name, " +
	        		      "CASE WHEN m.content LIKE '(הודעה מתלמיד)%' THEN 'out' ELSE 'in' END as direction " +
	        		      "FROM messages m " +
	        		      "WHERE m.student_id = (SELECT student_id FROM students WHERE phone = ?) " + 
	        		      "ORDER BY m.sent_at ASC";
	        }

	        Query q = em.createNativeQuery(sql);
	        q.setParameter(1, myPhone);
	        List<Object[]> results = q.getResultList();

	        //CONVERT TO JSON
	        JSONArray arr = new JSONArray();
	        for (Object[] row : results) {
	            JSONObject obj = new JSONObject();
	            
	            // Clean the message content by removing the internal routing prefix
	            obj.put("content", row[0].toString().replace("(הודעה מתלמיד) ", ""));
	            
	            // Date Processing: Extract HH:mm from the timestamp string
	            String dateStr = row[1].toString();
	            obj.put("date", dateStr.length() >= 16 ? dateStr.substring(11, 16) : dateStr);
	            
	            obj.put("isRead", row[2]);
	            obj.put("partyName", row[3]); 
	            obj.put("direction", row[4]); 
	            arr.put(obj);
	        }
	        response.getWriter().write(arr.toString());

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    } finally {
	    	if (em != null) {
                em.close();
            }
	    }
	}
}