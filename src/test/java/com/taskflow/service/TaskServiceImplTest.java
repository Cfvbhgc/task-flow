package com.taskflow.service;

import com.taskflow.dto.TaskCreateRequest;
import com.taskflow.dto.TaskUpdateRequest;
import com.taskflow.model.Task;
import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import com.taskflow.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TaskServiceImpl} that verify business logic
 * in isolation from the database by mocking the repository layer.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = Task.builder()
                .id(1L)
                .title("Sample Task")
                .description("A sample task for testing")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .assignee("Alice")
                .deadline(LocalDate.of(2026, 4, 15))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("findAllTasks returns every task from the repository")
    void findAllTasks_shouldReturnAllTasks() {
        Task secondTask = Task.builder().id(2L).title("Second").status(TaskStatus.DONE).priority(TaskPriority.LOW).build();
        when(taskRepository.findAll()).thenReturn(List.of(sampleTask, secondTask));

        List<Task> lstResult = taskService.findAllTasks();

        assertThat(lstResult).hasSize(2);
        verify(taskRepository).findAll();
    }

    @Test
    @DisplayName("findTaskById returns the task when it exists")
    void findTaskById_shouldReturnTask_whenExists() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        Optional<Task> result = taskService.findTaskById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Sample Task");
    }

    @Test
    @DisplayName("findTaskById returns empty when task does not exist")
    void findTaskById_shouldReturnEmpty_whenNotExists() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Task> result = taskService.findTaskById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("createTask persists and returns a new task with defaults applied")
    void createTask_shouldPersistTask() {
        TaskCreateRequest request = new TaskCreateRequest(
                "New Task", "Description", null, null, "Bob", LocalDate.of(2026, 5, 1));

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Task created = taskService.createTask(request);

        assertThat(created.getId()).isEqualTo(10L);
        assertThat(created.getTitle()).isEqualTo("New Task");
        assertThat(created.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(created.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTask modifies existing task fields")
    void updateTask_shouldUpdateFields() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskUpdateRequest request = new TaskUpdateRequest(
                "Updated Title", "Updated Desc", TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH, "Charlie", LocalDate.of(2026, 6, 1));

        Task updated = taskService.updateTask(1L, request);

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(updated.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(updated.getAssignee()).isEqualTo("Charlie");
    }

    @Test
    @DisplayName("updateTask throws EntityNotFoundException when task does not exist")
    void updateTask_shouldThrow_whenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        TaskUpdateRequest request = new TaskUpdateRequest("T", null, null, null, null, null);

        assertThatThrownBy(() -> taskService.updateTask(99L, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("moveTask changes the task status")
    void moveTask_shouldChangeStatus() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task moved = taskService.moveTask(1L, TaskStatus.DONE);

        assertThat(moved.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    @DisplayName("deleteTask removes the task when it exists")
    void deleteTask_shouldRemoveTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTask throws EntityNotFoundException when task does not exist")
    void deleteTask_shouldThrow_whenNotFound() {
        when(taskRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask(99L))
                .isInstanceOf(EntityNotFoundException.class);
        verify(taskRepository, never()).deleteById(99L);
    }

    @Test
    @DisplayName("getTasksGroupedByStatus returns a map with all three statuses")
    void getTasksGroupedByStatus_shouldReturnAllColumns() {
        when(taskRepository.findByStatus(TaskStatus.TODO)).thenReturn(List.of(sampleTask));
        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS)).thenReturn(List.of());
        when(taskRepository.findByStatus(TaskStatus.DONE)).thenReturn(List.of());

        Map<TaskStatus, List<Task>> mapResult = taskService.getTasksGroupedByStatus();

        assertThat(mapResult).hasSize(3);
        assertThat(mapResult.get(TaskStatus.TODO)).hasSize(1);
        assertThat(mapResult.get(TaskStatus.IN_PROGRESS)).isEmpty();
        assertThat(mapResult.get(TaskStatus.DONE)).isEmpty();
    }
}
