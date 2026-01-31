package com.taskflow.controller;

import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller that exposes a lightweight JSON API consumed by the
 * drag-and-drop JavaScript on the Kanban board. This controller handles
 * status transitions triggered by card movements between columns.
 *
 * @author TaskFlow Team
 */
@RestController
@RequestMapping("/api/tasks")
@Slf4j
@RequiredArgsConstructor
public class ApiController {

    private final TaskService taskService;

    /**
     * Returns all tasks as a JSON array. Primarily used for debugging
     * or external integrations.
     *
     * @return a {@link ResponseEntity} containing the full task list
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> lstTasks = taskService.findAllTasks();
        log.debug("API: returning {} tasks", lstTasks.size());
        return ResponseEntity.ok(lstTasks);
    }

    /**
     * Moves a task to a new Kanban column. The request body must contain
     * a JSON object with a single key {@code "status"} whose value is one
     * of the {@link TaskStatus} enum names (e.g. {@code "IN_PROGRESS"}).
     *
     * <p>This endpoint is called by the front-end drag-and-drop handler
     * whenever a card is dropped into a different column.</p>
     *
     * @param id      the identifier of the task to move
     * @param payload a map containing the target status under the key "status"
     * @return the updated task wrapped in a {@link ResponseEntity}
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable Long id,
                                                  @RequestBody Map<String, String> payload) {
        String strNewStatus = payload.get("status");
        if (strNewStatus == null || strNewStatus.isBlank()) {
            log.warn("API: missing status in PATCH request for task id={}", id);
            return ResponseEntity.badRequest().build();
        }

        try {
            TaskStatus newStatus = TaskStatus.valueOf(strNewStatus.toUpperCase());
            Task movedTask = taskService.moveTask(id, newStatus);
            log.info("API: moved task id={} to status={}", id, newStatus);
            return ResponseEntity.ok(movedTask);
        } catch (IllegalArgumentException ex) {
            log.warn("API: invalid status value '{}' for task id={}", strNewStatus, id);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Returns a single task by its identifier.
     *
     * @param id the primary key of the task
     * @return the task if found, or a 404 response
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.findTaskById(id)
                .map(task -> {
                    log.debug("API: returning task id={}", id);
                    return ResponseEntity.ok(task);
                })
                .orElseGet(() -> {
                    log.warn("API: task not found id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }
}
