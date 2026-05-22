package servlet;
 
import db.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
 
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
 
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
 
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
 
        BufferedReader br = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        String json = sb.toString();
 
        try {
            String email = json.split("\"email\":\"")[1].split("\"")[0];
            String pass  = json.split("\"password\":\"")[1].split("\"")[0];
 
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT id, name FROM users WHERE email=? AND password=?"
            );
            ps.setString(1, email);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
 
            if (rs.next()) {
                HttpSession session = request.getSession();
                session.setAttribute("userId",   rs.getInt("id"));
                session.setAttribute("userName", rs.getString("name"));
 
                // Return both id and name so frontend can store userId in sessionStorage
                out.print("SUCCESS:" + rs.getInt("id") + ":" + rs.getString("name"));
            } else {
                out.print("INVALID");
            }
 
        } catch (Exception e) {
            e.printStackTrace();
            out.print("ERROR");
        }
    }
}