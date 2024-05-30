package com.example.foodorder.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/order")
public class OrderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        response.getWriter().println("<h1>Order Food</h1>");
        response.getWriter().println("<form action='order' method='post'>");
        response.getWriter().println("Food: <input type='text' name='food'><br>");
        response.getWriter().println("Quantity: <input type='number' name='quantity'><br>");
        response.getWriter().println("<input type='submit' value='Order'>");
        response.getWriter().println("</form>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String food = request.getParameter("food");
        String quantity = request.getParameter("quantity");
        response.setContentType("text/html");
        response.getWriter().println("<h1>Order Summary</h1>");
        response.getWriter().println("Food: " + food + "<br>");
        response.getWriter().println("Quantity: " + quantity + "<br>");
        response.getWriter().println("<a href='order'>Place another order</a>");
    }
}

