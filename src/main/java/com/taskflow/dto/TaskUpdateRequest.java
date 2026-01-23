package com.taskflow.dto;

import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Immutable data carrier for task update requests.
 *
 * @param title       the updated task title (required)
 * @param description the updated description
 * @param status      the new board column
 * @param priority    the new priority level
 * @param assignee    the updated assignee
 * @param deadline    the updated due date
 */
public record TaskUpdateRequest(
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
