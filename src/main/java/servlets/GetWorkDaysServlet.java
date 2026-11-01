package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.persistence.*;
import model.WorkDay;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import util.JPAUtil;

/**
 * WORK DAY CALENDAR PROVIDER
 * This servlet identifies which dates are marked as working days for a teacher.
 * It supports queries by either teacherId (for students) or phone number (for teachers).
 * The output is formatted for calendar background highlighting.
 */
@WebServlet("/GetWorkDaysServlet")
public class GetWorkDaysServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	/**
     * Handles GET requests to retrieve a list of working dates.
     * Response: JSON array of background events for the calendar UI.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        EntityManager em = JPAUtil.getEntityManager();

        try {
            String phone = request.getParameter("phone");
            String tIdParam = request.getParameter("teacherId");
            List<WorkDay> workDays;

            /**
             * POLYMORPHIC QUERY LOGIC
             * 1. If teacherId is provided: Fetch by ID (Student view).
             * 2. If phone is provided: Fetch by Phone (Teacher view).
             */
            if (tIdParam != null && !tIdParam.isEmpty() && !tIdParam.equals("undefined")) {
                int teacherId = Integer.parseInt(tIdParam);
                workDays = em.createQuery("SELECT wd FROM WorkDay wd WHERE wd.teacher.teacherId = :id", WorkDay.class)
                             .setParameter("id", teacherId)
                             .getResultList();
            } 
            else if (phone != null && !phone.isEmpty()) {
                workDays = em.createQuery("SELECT wd FROM WorkDay wd WHERE wd.teacher.phone = :p", WorkDay.class)
                             .setParameter("p", phone)
                             .getResultList();
            } else {
                workDays = java.util.Collections.emptyList();
            }

            JSONArray arr = new JSONArray();
            for (WorkDay wd : workDays) {
                JSONObject obj = new JSONObject();
                
                //Calendar UI Properties - 'start' maps the date. 'display: background' highlights the entire calendar cell.
                obj.put("start", wd.getWork_date().toString()); 
                obj.put("display", "background"); 
                obj.put("backgroundColor", "#90ee90"); // ירוק בהיר
                arr.put(obj);
            }
            //Return the array to the client
            response.getWriter().write(arr.toString());

        } catch (Exception e) {
            e.printStackTrace();
            //Redirects to the central error page via web.xml on backend failure.
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        	//Resource cleanup: Important to close both EM and EMF if created in method
        	if (em != null) {
        		em.close();
        	}
        }
    }
}