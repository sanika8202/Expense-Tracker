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

@WebServlet("/AddExpenseServlet")
public class AddExpenseServlet extends HttpServlet {

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

        while((line = br.readLine()) != null) {
            sb.append(line);
        }

        String json = sb.toString();
        System.out.println("Received JSON: " + json);

        try {

            String title =
                json.split("\"title\":\"")[1]
                .split("\"")[0];

            String amount =
            	    json.split("\"amount\":")[1]
            	    .split("[,}]")[0]
            	    .trim()
            	    .replace("\"", "");

            String category =
                json.split("\"category\":\"")[1]
                .split("\"")[0];

            String date =
                json.split("\"date\":\"")[1]
                .split("\"")[0];

            System.out.println("Title: " + title);
            System.out.println("Amount: " + amount);
            System.out.println("Category: " + category);
            System.out.println("Date: " + date);

            Connection con = DBConnection.getConnection();

            if(con == null) {
                System.out.println("Connection is NULL!");
                out.println("Database connection failed!");
                return;
            }

            PreparedStatement ps =
                con.prepareStatement(
                "INSERT INTO expenses(title, amount, category, expense_date) VALUES(?,?,?,?)"
            );

            ps.setString(1, title);
            ps.setDouble(2, Double.parseDouble(amount));
            ps.setString(3, category);
            ps.setString(4, date);

            int rows = ps.executeUpdate();

            if(rows > 0) {
                System.out.println("Expense saved successfully!");
                out.println("Expense Added Successfully!");
            } else {
                out.println("Failed to save expense!");
            }

        } catch(Exception e) {
            e.printStackTrace();
            out.println("Error: " + e.getMessage());
        }
    }
}