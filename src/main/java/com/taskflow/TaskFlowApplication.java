package com.taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the TaskFlow application.
 *
 * <p>TaskFlow is a Kanban-style task management system that provides
 * a visual board with three columns: TODO, IN_PROGRESS, and DONE.
 * Users can create, update, assign, and drag-drop tasks between columns.</p>
 *
 * @author TaskFlow Team
 * @version 1.0.0
 */
@SpringBootApplication
public class TaskFlowApplication {

    /**
     * Bootstraps the Spring Boot application context and starts the embedded server.
     *
     * @param args command-line arguments passed at startup
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskFlowApplication.class, args);
    }
}
