package com.example.foodorder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class App implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Code to initialize resources or configurations
        System.out.println("Food Ordering App is starting up!");
        // You can initialize database connections, read configuration files, etc. here
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Code to clean up resources
        System.out.println("Food Ordering App is shutting down!");
        // Clean up database connections, close files, etc. here
    }

    public static void main(String[] args) {
        // This method is typically not used in a web application as Tomcat manages the lifecycle.
        // However, if you need to run some standalone tasks, you can place them here.
        System.out.println("Food Ordering App standalone mode.");
    }
}

