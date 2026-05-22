
package servlet;

import db.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

@WebServlet("/AIAdviceServlet")
public class AIAdviceServlet extends HttpServlet {

    private static final String API_KEY = "Your_key";

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM expenses"
            );
            ResultSet rs = ps.executeQuery();

            StringBuilder expenseList = new StringBuilder();
            double total = 0;

            while(rs.next()) {
                expenseList.append("Title: ")
                    .append(rs.getString("title"))
                    .append(", Amount: ")
                    .append(rs.getDouble("amount"))
                    .append(", Category: ")
                    .append(rs.getString("category"))
                    .append(". ");
                total += rs.getDouble("amount");
            }

            String userMessage =
                "I have these expenses: " +
                expenseList.toString() +
                " Total spent: " + total +
                ". Give me exactly 3 short financial" +
                " advice tips." +
                " Use plain text only." +
                " No bold, no asterisks, no markdown." +
                " Just simple numbered tips 1. 2. 3.";

            String advice = callGroqAPI(userMessage);
            out.println(advice);

        } catch(Exception e) {
            e.printStackTrace();
            out.println("Error: " + e.getMessage());
        }
    }

    private String callGroqAPI(String userMessage)
            throws Exception {

        URL url = new URL(
            "https://api.groq.com/openai/v1/chat/completions"
        );

        HttpURLConnection conn =
            (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty(
            "Content-Type", "application/json");
        conn.setRequestProperty(
            "Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);

        String cleanMessage = userMessage
            .replace("\\", "")
            .replace("\"", "'")
            .replace("\n", " ")
            .replace("\r", " ");

        String jsonBody = "{"
            + "\"model\":\"llama-3.3-70b-versatile\","
            + "\"max_tokens\":300,"
            + "\"messages\":["
            + "{\"role\":\"user\","
            + "\"content\":\"" + cleanMessage + "\"}"
            + "]"
            + "}";

        byte[] input = jsonBody.getBytes("utf-8");
        conn.setRequestProperty("Content-Length",
            String.valueOf(input.length));

        OutputStream os = conn.getOutputStream();
        os.write(input);
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();

        Scanner scanner;
        if(responseCode == 200) {
            scanner = new Scanner(conn.getInputStream());
        } else {
            scanner = new Scanner(conn.getErrorStream());
        }

        StringBuilder responseBody = new StringBuilder();
        while(scanner.hasNextLine()) {
            responseBody.append(scanner.nextLine());
        }
        scanner.close();

        String response = responseBody.toString();
        System.out.println("Full Response: " + response);

        if(responseCode == 200) {

            String searchKey = "\"content\":\"";
            int startIdx = response.indexOf(searchKey);

            if(startIdx != -1) {

                startIdx = startIdx + searchKey.length();

                StringBuilder result = new StringBuilder();
                int i = startIdx;

                while(i < response.length()) {

                    char c = response.charAt(i);

                    if(c == '\\'
                            && i + 1 < response.length()) {

                        char next = response.charAt(i + 1);

                        if(next == 'n') {
                            result.append('\n');
                            i += 2;
                            continue;
                        } else if(next == '"') {
                            result.append('"');
                            i += 2;
                            continue;
                        } else if(next == '\\') {
                            result.append('\\');
                            i += 2;
                            continue;
                        }
                    }

                    if(c == '"') {
                        break;
                    }

                    result.append(c);
                    i++;
                }

                String text = result.toString();

                text = text
                    .replace("**", "")
                    .replace("*", "")
                    .trim();

                return text;
            }
        }

        return "Could not get advice. Please try again.";
    }
}