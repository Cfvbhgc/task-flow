package com.taskflow.controller;

import com.taskflow.dto.TaskCreateRequest;
import com.taskflow.dto.TaskUpdateRequest;
import com.taskflow.model.Task;
import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * MVC controller responsible for rendering Thymeleaf views related to
 * task management. Handles the Kanban board display, task creation form,
 * task editing form, and deletion.
 *
 * @author TaskFlow Team
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Renders the main Kanban board page with tasks grouped into three columns.
     *
     * @param model the Spring MVC model to populate with board data
     * @return the name of the Thymeleaf template to render
     */
    @GetMapping("/")
    public String showBoard(Model model) {
        Map<TaskStatus, List<Task>> mapGrouped = taskService.getTasksGroupedByStatus();
        model.addAttribute("todoTasks", mapGrouped.get(TaskStatus.TODO));
        model.addAttribute("inProgressTasks", mapGrouped.get(TaskStatus.IN_PROGRESS));
        model.addAttribute("doneTasks", mapGrouped.get(TaskStatus.DONE));
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
        log.debug("Rendering Kanban board");
        return "index";
    }

    /**
     * Displays the task creation form pre-populated with default values.
     *
     * @param model the Spring MVC model
     * @return the name of the task form template
     */
    @GetMapping("/tasks/new")
    public String showCreateForm(Model model) {
        model.addAttribute("task", new TaskCreateRequest("", "", TaskStatus.TODO, TaskPriority.MEDIUM, "", null));
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
        model.addAttribute("isEdit", false);
        log.debug("Showing create task form");
        return "task-form";
    }

    /**
     * Processes the task creation form submission. If validation errors are
     * present the form is re-displayed with error messages; otherwise the
     * task is persisted and the user is redirected to the board.
     *
     * @param request            the validated form payload
     * @param bindingResult      holds any validation errors
     * @param model              the Spring MVC model
     * @param redirectAttributes flash attributes for success messages
     * @return a redirect to the board on success, or the form view on error
     */
    @PostMapping("/tasks")
    public String createTask(@Valid @ModelAttribute("task") TaskCreateRequest request,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("priorities", TaskPriority.values());
            model.addAttribute("isEdit", false);
            log.warn("Task creation validation failed: {}", bindingResult.getAllErrors());
            return "task-form";
        }

        Task createdTask = taskService.createTask(request);
        redirectAttributes.addFlashAttribute("successMessage",
                "Task '" + createdTask.getTitle() + "' created successfully!");
        log.info("Task created via form: id={}", createdTask.getId());
        return "redirect:/";
    }

    /**
     * Displays the task editing form pre-populated with the current values
     * of the specified task.
     *
     * @param id    the identifier of the task to edit
     * @param model the Spring MVC model
     * @return the task form template, or a redirect if the task does not exist
     */
    @GetMapping("/tasks/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return taskService.findTaskById(id)
                .map(task -> {
                    TaskUpdateRequest request = new TaskUpdateRequest(
                            task.getTitle(),
                            task.getDescription(),
                            task.getStatus(),
                            task.getPriority(),
                            task.getAssignee(),
                            task.getDeadline()
                    );
                    model.addAttribute("task", request);
                    model.addAttribute("taskId", id);
                    model.addAttribute("statuses", TaskStatus.values());
                    model.addAttribute("priorities", TaskPriority.values());
                    model.addAttribute("isEdit", true);
                    log.debug("Showing edit form for task id={}", id);
                    return "task-form";
                })
                .orElseGet(() -> {
                    log.warn("Attempted to edit non-existent task id={}", id);
                    return "redirect:/";
                });
    }

    /**
     * Processes the task update form submission.
     *
     * @param id                 the identifier of the task being updated
     * @param request            the validated update payload
     * @param bindingResult      holds any validation errors
     * @param model              the Spring MVC model
     * @param redirectAttributes flash attributes for success messages
     * @return a redirect to the board on success, or the form view on error
     */
    @PostMapping("/tasks/{id}")
    public String updateTask(@PathVariable Long id,
                             @Valid @ModelAttribute("task") TaskUpdateRequest request,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("taskId", id);
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("priorities", TaskPriority.values());
            model.addAttribute("isEdit", true);
            log.warn("Task update validation failed for id={}: {}", id, bindingResult.getAllErrors());
            return "task-form";
        }

        Task updatedTask = taskService.updateTask(id, request);
        redirectAttributes.addFlashAttribute("successMessage",
                "Task '" + updatedTask.getTitle() + "' updated successfully!");
        log.info("Task updated via form: id={}", id);
        return "redirect:/";
    }

    /**
     * Deletes a task and redirects back to the Kanban board.
     *
     * @param id                 the identifier of the task to remove
     * @param redirectAttributes flash attributes for success messages
     * @return a redirect to the board
     */
    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        taskService.deleteTask(id);
        redirectAttributes.addFlashAttribute("successMessage", "Task deleted successfully!");
        log.info("Task deleted via form: id={}", id);
        return "redirect:/";
    }

    /**
     * Filters the board by assignee name, displaying only that person's tasks.
     *
     * @param strAssignee the assignee to filter by
     * @param model       the Spring MVC model
     * @return the board template with filtered results
     */
    @GetMapping("/tasks/filter")
    public String filterByAssignee(@RequestParam("assignee") String strAssignee, Model model) {
        List<Task> lstFiltered = taskService.findTasksByAssignee(strAssignee);
        Map<TaskStatus, List<Task>> mapGrouped = taskService.getTasksGroupedByStatus();

        if (!strAssignee.isBlank()) {
            for (TaskStatus status : TaskStatus.values()) {
                List<Task> lstColumn = mapGrouped.get(status).stream()
                        .filter(t -> strAssignee.equalsIgnoreCase(t.getAssignee()))
                        .toList();
                mapGrouped.put(status, lstColumn);
            }
        }

        model.addAttribute("todoTasks", mapGrouped.get(TaskStatus.TODO));
        model.addAttribute("inProgressTasks", mapGrouped.get(TaskStatus.IN_PROGRESS));
        model.addAttribute("doneTasks", mapGrouped.get(TaskStatus.DONE));
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
        model.addAttribute("filterAssignee", strAssignee);
        log.debug("Filtered board by assignee='{}'", strAssignee);
        return "index";
    }
}
