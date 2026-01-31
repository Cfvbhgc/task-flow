package com.taskflow.service;

import com.taskflow.dto.TaskCreateRequest;
import com.taskflow.dto.TaskUpdateRequest;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service contract for task management operations.
 *
 * <p>Implementations of this interface are responsible for orchestrating
 * business logic around task CRUD, status transitions, and filtering.</p>
 *
 * @author TaskFlow Team
 */
public interface TaskService {

    /**
     * Retrieves all tasks currently stored in the system.
     *
     * @return a list of every task, unfiltered
     */
    List<Task> findAllTasks();

    /**
     * Looks up a single task by its unique identifier.
     *
     * @param id the primary key of the desired task
     * @return an {@link Optional} containing the task if found, or empty otherwise
     */
    Optional<Task> findTaskById(Long id);

    /**
     * Retrieves all tasks belonging to a specific Kanban column.
     *
     * @param status the column to query
     * @return a list of tasks in the given status
     */
    List<Task> findTasksByStatus(TaskStatus status);

    /**
     * Builds a grouped map of tasks keyed by their status, suitable for
     * rendering the Kanban board in a single pass.
     *
     * @return a map where each key is a {@link TaskStatus} and the value
     *         is the list of tasks in that column
     */
    Map<TaskStatus, List<Task>> getTasksGroupedByStatus();

    /**
     * Creates a new task from the supplied request data and persists it.
     *
     * @param request the creation payload
     * @return the newly persisted task with generated id and timestamps
     */
    Task createTask(TaskCreateRequest request);

    /**
     * Applies the supplied updates to an existing task.
     *
     * @param id      the identifier of the task to update
     * @param request the update payload
     * @return the updated task entity
     * @throws jakarta.persistence.EntityNotFoundException if no task with the given id exists
     */
    Task updateTask(Long id, TaskUpdateRequest request);

    /**
     * Moves a task to a different Kanban column.
     *
     * @param id        the task identifier
     * @param newStatus the target column
     * @return the task after the status change
     */
    Task moveTask(Long id, TaskStatus newStatus);

    /**
     * Permanently removes a task from the system.
     *
     * @param id the identifier of the task to delete
     */
    void deleteTask(Long id);

    /**
     * Retrieves tasks assigned to a specific person.
     *
     * @param strAssignee the assignee name
     * @return a list of tasks assigned to the given person
     */
    List<Task> findTasksByAssignee(String strAssignee);
}
