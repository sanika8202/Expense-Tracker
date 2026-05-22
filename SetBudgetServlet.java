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
 
@WebServlet("/SetBudgetServlet")
public class SetBudgetServlet extends HttpServlet {
 
    // POST — save or update budget
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
            String amountStr = json.split("\"amount\":")[1]
                    .split("[,}]")[0].trim().replace("\"", "");
            String monthStr  = json.split("\"month\":")[1]
                    .split("[,}]")[0].trim();
            String yearStr   = json.split("\"year\":")[1]
                    .split("[,}]")[0].trim();
            String userIdStr = json.split("\"userId\":")[1]
                    .split("[,}]")[0].trim();
 
            double amount = Double.parseDouble(amountStr);
            int    month  = Integer.parseInt(monthStr);
            int    year   = Integer.parseInt(yearStr);
            int    userId = Integer.parseInt(userIdStr);
 
            Connection con = DBConnection.getConnection();
 
            // Check if budget already exists for this month/year/user
            PreparedStatement check = con.prepareStatement(
                "SELECT id FROM budget WHERE month=? AND year=? AND user_id=?"
            );
            check.setInt(1, month);
            check.setInt(2, year);
            check.setInt(3, userId);
            ResultSet rs = check.executeQuery();
 
            if (rs.next()) {
                // UPDATE existing
                PreparedStatement ps = con.prepareStatement(
                    "UPDATE budget SET amount=? WHERE month=? AND year=? AND user_id=?"
                );
                ps.setDouble(1, amount);
                ps.setInt(2, month);
                ps.setInt(3, year);
                ps.setInt(4, userId);
                ps.executeUpdate();
                out.print("UPDATED");
            } else {
                // INSERT new
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO budget(month, year, amount, user_id) VALUES(?,?,?,?)"
                );
                ps.setInt(1, month);
                ps.setInt(2, year);
                ps.setDouble(3, amount);
                ps.setInt(4, userId);
                ps.executeUpdate();
                out.print("SAVED");
            }
 
        } catch (Exception e) {
            e.printStackTrace();
            out.print("ERROR: " + e.getMessage());
        }
    }
 
    // GET — fetch budget for given month/year/userId
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
 
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
 
        try {
            int month  = Integer.parseInt(request.getParameter("month"));
            int year   = Integer.parseInt(request.getParameter("year"));
            int userId = Integer.parseInt(request.getParameter("userId"));
 
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT amount FROM budget WHERE month=? AND year=? AND user_id=?"
            );
            ps.setInt(1, month);
            ps.setInt(2, year);
            ps.setInt(3, userId);
            ResultSet rs = ps.executeQuery();
 
            if (rs.next()) {
                out.print("{\"amount\":" + rs.getDouble("amount") + "}");
            } else {
                out.print("{\"amount\":0}");
            }
 
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"amount\":0}");
        }
    }
}