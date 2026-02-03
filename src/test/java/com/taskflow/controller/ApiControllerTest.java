package com.taskflow.controller;

import com.taskflow.model.Task;
import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import com.taskflow.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice tests for {@link ApiController} that verify the REST API
 * endpoints behave correctly using MockMvc.
 */
@WebMvcTest(ApiController.class)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    /**
     * Builds a sample task entity for use in tests.
     */
    private Task buildSampleTask(Long id, String strTitle, TaskStatus status) {
        return Task.builder()
                .id(id)
                .title(strTitle)
                .status(status)
                .priority(TaskPriority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/tasks returns all tasks as JSON")
    void getAllTasks_shouldReturnJsonList() throws Exception {
        List<Task> lstTasks = List.of(
                buildSampleTask(1L, "Task A", TaskStatus.TODO),
                buildSampleTask(2L, "Task B", TaskStatus.DONE)
        );
        when(taskService.findAllTasks()).thenReturn(lstTasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Task A")))
                .andExpect(jsonPath("$[1].status", is("DONE")));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} returns task when found")
    void getTaskById_shouldReturnTask_whenExists() throws Exception {
        Task task = buildSampleTask(5L, "Found Task", TaskStatus.IN_PROGRESS);
        when(taskService.findTaskById(5L)).thenReturn(Optional.of(task));

        mockMvc.perform(get("/api/tasks/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Found Task")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} returns 404 when not found")
    void getTaskById_shouldReturn404_whenNotExists() throws Exception {
        when(taskService.findTaskById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/status moves task to new column")
    void updateTaskStatus_shouldMoveTask() throws Exception {
        Task movedTask = buildSampleTask(1L, "Moved Task", TaskStatus.DONE);
        when(taskService.moveTask(eq(1L), any(TaskStatus.class))).thenReturn(movedTask);

        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DONE")));
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/status returns 400 for missing status")
    void updateTaskStatus_shouldReturn400_whenStatusMissing() throws Exception {
        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/tasks/{id}/status returns 400 for invalid status value")
    void updateTaskStatus_shouldReturn400_whenStatusInvalid() throws Exception {
        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"INVALID_STATUS\"}"))
                .andExpect(status().isBadRequest());
    }
}
