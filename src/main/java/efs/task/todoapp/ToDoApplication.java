package efs.task.todoapp;

import com.sun.net.httpserver.HttpServer;
import efs.task.todoapp.web.WebServerFactory;

import java.io.IOException;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ToDoApplication {
    private static final Logger LOGGER = Logger.getLogger(ToDoApplication.class.getName());

    public static void main(String[] args) {
        var application = new ToDoApplication();
        var server = application.createServer();
        server.start();

        LOGGER.info("ToDoApplication's server started ...");
        LOGGER.info("server Started at  http://localhost:8080");

    }

    public HttpServer createServer() {
        try {
            return WebServerFactory.createServer();
        } catch (IOException e) {
            LOGGER.warning("Creating server error.");
            e.printStackTrace();
            return null;
        }
    }
}
