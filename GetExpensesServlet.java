package servlet;

import db.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/GetExpensesServlet")
public class GetExpensesServlet extends HttpServlet {

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM expenses ORDER BY expense_date DESC"
            );

            ResultSet rs = ps.executeQuery();

            StringBuilder json = new StringBuilder();
            json.append("[");

            boolean first = true;

            while(rs.next()) {

                if(!first) json.append(",");

                json.append("{")
                    .append("\"id\":" + rs.getInt("id") + ",")
                    .append("\"title\":\"" + rs.getString("title") + "\",")
                    .append("\"amount\":" + rs.getDouble("amount") + ",")
                    .append("\"category\":\"" + rs.getString("category") + "\",")
                    .append("\"date\":\"" + rs.getString("expense_date") + "\"")
                    .append("}");

                first = false;
            }

            json.append("]");

            out.print(json.toString());

        } catch(Exception e) {
            e.printStackTrace();
            out.print("[]");
        }
    }
}
