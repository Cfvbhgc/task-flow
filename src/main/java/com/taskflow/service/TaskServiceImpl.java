package com.taskflow.service;

import com.taskflow.dto.TaskCreateRequest;
import com.taskflow.dto.TaskUpdateRequest;
import com.taskflow.model.Task;
import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import com.taskflow.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of {@link TaskService}.
 *
 * <p>Handles all business logic for task management including creation,
 * updates, status transitions, and retrieval. All mutating operations
 * are wrapped in transactions.</p>
 *
 * @author TaskFlow Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Task> findAllTasks() {
        List<Task> lstTasks = taskRepository.findAll();
        log.debug("Retrieved {} tasks from the database", lstTasks.size());
        return lstTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Task> findTaskById(Long id) {
        log.debug("Looking up task with id={}", id);
        return taskRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Task> findTasksByStatus(TaskStatus status) {
        List<Task> lstTasks = taskRepository.findByStatus(status);
        log.debug("Found {} tasks with status={}", lstTasks.size(), status);
        return lstTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<TaskStatus, List<Task>> getTasksGroupedByStatus() {
        Map<TaskStatus, List<Task>> mapGrouped = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            List<Task> lstColumn = taskRepository.findByStatus(status);
            mapGrouped.put(status, lstColumn);
        }
        log.debug("Grouped tasks into {} columns", mapGrouped.size());
        return mapGrouped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Task createTask(TaskCreateRequest request) {
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status() != null ? request.status() : TaskStatus.TODO)
                .priority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM)
                .assignee(request.assignee())
                .deadline(request.deadline())
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("Created new task: id={}, title='{}'", savedTask.getId(), savedTask.getTitle());
        return savedTask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Task updateTask(Long id, TaskUpdateRequest request) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Task not found for update: id={}", id);
                    return new EntityNotFoundException("Task not found with id: " + id);
                });

        existingTask.setTitle(request.title());
        existingTask.setDescription(request.description());

        if (request.status() != null) {
            existingTask.setStatus(request.status());
        }
        if (request.priority() != null) {
            existingTask.setPriority(request.priority());
        }

        existingTask.setAssignee(request.assignee());
        existingTask.setDeadline(request.deadline());

        Task updatedTask = taskRepository.save(existingTask);
        log.info("Updated task: id={}, title='{}'", updatedTask.getId(), updatedTask.getTitle());
        return updatedTask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Task moveTask(Long id, TaskStatus newStatus) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Task not found for move: id={}", id);
                    return new EntityNotFoundException("Task not found with id: " + id);
                });

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);
        Task movedTask = taskRepository.save(task);

        log.info("Moved task id={} from {} to {}", id, oldStatus, newStatus);
        return movedTask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            log.error("Cannot delete non-existent task: id={}", id);
            throw new EntityNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
        log.info("Deleted task: id={}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Task> findTasksByAssignee(String strAssignee) {
        List<Task> lstTasks = taskRepository.findByAssignee(strAssignee);
        log.debug("Found {} tasks assigned to '{}'", lstTasks.size(), strAssignee);
        return lstTasks;
    }
}
