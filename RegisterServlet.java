package servlet;

import db.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

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
            String name  = json.split("\"name\":\"")[1].split("\"")[0];
            String email = json.split("\"email\":\"")[1].split("\"")[0];
            String pass  = json.split("\"password\":\"")[1].split("\"")[0];

            Connection con = DBConnection.getConnection();

            // Check if email already exists
            PreparedStatement check = con.prepareStatement(
                "SELECT id FROM users WHERE email=?"
            );
            check.setString(1, email);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                out.print("EMAIL_EXISTS");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users(name, email, password) VALUES(?,?,?)"
            );
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, pass);
            ps.executeUpdate();

            out.print("SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            out.print("ERROR");
        }
    }
}