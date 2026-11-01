package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * This servlet acts as a security checkpoint for the client-side (JavaScript).
 * It verifies if a valid session exists and identifies the currently logged-in user.
 */
@WebServlet("/CheckSessionServlet")
public class CheckSessionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Processes GET requests to verify session status.
     * Logic: Check for existing session -> Retrieve user ID -> Return status or data.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        
        //It retrieves the current session if it exists, but does NOT create a new one.
        HttpSession session = request.getSession(false); 
        
        //We look for the "loggedInUser" attribute (which stores the user's phone number).
        String phone = (session != null) ? (String) session.getAttribute("loggedInUser") : null;

        if (phone == null) {
            //If no session exists or the attribute is missing, we return HTTP 401.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            //If a valid user is found, we return their phone number.
            response.getWriter().write(phone);
        }
    }
}