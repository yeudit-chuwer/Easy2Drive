package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.*;
import model.Teacher;
import model.Lesson;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import util.JPAUtil;

/**
 * TEACHER CALENDAR DATA PROVIDER
 * This servlet fetches all scheduled lessons for a specific teacher.
 * The output is formatted as a JSON array compatible with modern calendar libraries.
 */
@WebServlet("/GetTeacherLessonsServlet")
public class GetTeacherLessonsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	/**
     * Handles GET requests to populate the teacher's schedule.
     * Logic: Identify Teacher -> Fetch Related Lessons -> Map to Calendar Event Objects.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        EntityManager em = JPAUtil.getEntityManager();

        try {
        	//Retrieve teacher's phone to identify the profile
            String phone = request.getParameter("phone");
            //Find the Teacher entity
            TypedQuery<Teacher> query = em.createQuery("SELECT t FROM Teacher t WHERE t.phone = :phone", Teacher.class);
            query.setParameter("phone", phone);
            Teacher teacher = query.getSingleResult();

            //Fetch all lessons associated with this teacher's unique ID
            TypedQuery<Lesson> lQuery = em.createQuery("SELECT l FROM Lesson l WHERE l.teacher.id = :tId", Lesson.class);
            lQuery.setParameter("tId", teacher.getTeacherId());
            List<Lesson> lessons = lQuery.getResultList();

            JSONArray arr = new JSONArray();
         
            //Iterate through lessons and map them to JSON "Event" objects
            for (Lesson l : lessons) {
                JSONObject obj = new JSONObject();
                
                // Detailed time data for custom logic (e.g., reminders)
                obj.put("date", l.getDate().toString());
                obj.put("start_time", l.getStart_time().toString());
                
                //Sending the student's name 
                if (l.getStudent() != null) {
                    obj.put("student_name", l.getStudent().getName());
                    obj.put("title", "שיעור: " + l.getStudent().getName());
                } else {
                    obj.put("student_name", "תלמיד");
                    obj.put("title", "שיעור נהיגה");
                }

                obj.put("start", l.getDate().toString() + "T" + l.getStart_time().toString());
                obj.put("backgroundColor", "#3b82f6");// Corporate blue color for UI consistency
                obj.put("display", "block");
                
                arr.put(obj);
            }
            //Return the populated array
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