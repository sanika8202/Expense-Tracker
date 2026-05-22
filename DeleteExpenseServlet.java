package servlet;

import db.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/DeleteExpenseServlet")
public class DeleteExpenseServlet extends HttpServlet {

    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");

        String id = request.getParameter("id");

        try {

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                "DELETE FROM expenses WHERE id=?"
            );

            ps.setInt(1, Integer.parseInt(id));
            ps.executeUpdate();

            response.getWriter().println("Expense Deleted!");

        } catch(Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error deleting!");
        }
    }
}