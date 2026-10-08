package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * LOGOUT SERVLET
 * This servlet handles the termination of a user session.
 * It ensures that all session-bound data is cleared from the server's memory
 * to prevent unauthorized access after the user leaves the application.
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Processes GET requests to log out a user.
     * Logic: Retrieve the existing session -> Invalidate it -> Return Success Status.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //Retrieve the current session, but do not create a new one if it doesn't exist (false)
    	HttpSession session = request.getSession(false);
        if (session != null) {
        	// "Invalidate" clears all attributes and marks the session as expired
            session.invalidate(); 
         // Logging for backend tracking
            System.out.println("DEBUG: User session invalidated successfully.");
        }
        //Return 200 OK so the frontend JavaScript knows it can redirect to the login page
        response.setStatus(HttpServletResponse.SC_OK);
    }
}