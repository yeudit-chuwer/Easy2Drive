package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.*;
import model.Teacher;
import model.Student;
import model.Lesson; // ודאי שייבאת את Lesson
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import util.JPAUtil;

/**
 * TEACHER'S STUDENT LIST PROVIDER
 * Retrieves all students associated with a specific teacher.
 * Also calculates the total number of completed lessons for each student.
 */
@WebServlet("/GetTeacherStudentsServlet")
public class GetTeacherStudentsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	/**
     * Handles GET requests to populate the teacher's student management table.
     * Logic: Fetch Teacher -> Iterate Students -> Count Completed Lessons per Student -> Return JSON.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String phone = request.getParameter("phone");
        
        EntityManager em = JPAUtil.getEntityManager();
        try {
        	//Identify the teacher profile
            Teacher teacher = em.createQuery("SELECT t FROM Teacher t WHERE t.phone = :p", Teacher.class)
                                .setParameter("p", phone).getSingleResult();
            
            JSONArray arr = new JSONArray();
            
            //Iterate through the teacher's student collection (One-to-Many relationship)
            if (teacher.getStudents() != null) {
                for (Student s : teacher.getStudents()) {
                    JSONObject obj = new JSONObject();
                    obj.put("name", s.getName());
                    obj.put("phone", s.getPhone());
                    
                    //Count lessons that are: Not cancelled and Dated in the past OR dated today with a start time that has already passed.
                    String countSql = "SELECT COUNT(l) FROM Lesson l WHERE l.student.id = :sId " +
                                     "AND l.status != 'cancelled' " +
                                     "AND (l.date < CURRENT_DATE OR (l.date = CURRENT_DATE AND l.start_time < CURRENT_TIME))";
                    
                    Long lessonsDone = em.createQuery(countSql, Long.class)
                                         .setParameter("sId", s.getStudentId())
                                         .getSingleResult();
                    
                    obj.put("lessonCount", lessonsDone != null ? lessonsDone : 0);
                   
                    arr.put(obj);
                }
            }
            //Return the finalized JSON array
            response.getWriter().write(arr.toString());
        } catch (Exception e) {
            e.printStackTrace();
            //Redirects to the central error page via web.xml on backend failure.
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	if (em != null) {
                em.close();
            }
        }
    }
}