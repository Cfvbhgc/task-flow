package com.taskflow.dto;

import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Immutable data carrier for task creation requests.
 *
 * @param title       the task title (required, max 255 chars)
 * @param description an optional longer description
 * @param status      initial board column (defaults to TODO if null)
 * @param priority    task priority level (defaults to MEDIUM if null)
 * @param assignee    optional person responsible for the task
 * @param deadline    optional due date
 */
public record TaskCreateRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        String description,

        TaskStatus status,

        TaskPriority priority,

        @Size(max = 100, message = "Assignee name must not exceed 100 characters")
        String assignee,

        LocalDate deadline
) {
}
