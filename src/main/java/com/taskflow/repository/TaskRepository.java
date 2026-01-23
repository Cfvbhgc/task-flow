package com.taskflow.repository;

import com.taskflow.model.Task;
import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Task} entities.
 *
 * <p>Provides standard CRUD operations inherited from {@link JpaRepository}
 * plus several custom finder and update methods used by the service layer.</p>
 *
 * @author TaskFlow Team
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Retrieves all tasks belonging to a specific Kanban column, ordered by
     * priority weight descending so that critical items appear first.
     *
     * @param status the board column to filter on
     * @return a list of tasks in the given status, highest priority first
     */
    @Query("SELECT t FROM Task t WHERE t.status = :status ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findByStatus(@Param("status") TaskStatus status);

    /**
     * Retrieves all tasks assigned to a particular person.
     *
     * @param assignee the name or identifier of the assignee
     * @return a list of matching tasks ordered by deadline ascending
     */
    @Query("SELECT t FROM Task t WHERE t.assignee = :assignee ORDER BY t.deadline ASC NULLS LAST")
    List<Task> findByAssignee(@Param("assignee") String assignee);

    /**
     * Retrieves tasks whose deadline falls before the supplied date,
     * useful for identifying overdue or upcoming items.
     *
     * @param date the cutoff date (exclusive upper bound)
     * @return a list of tasks with deadlines earlier than the given date
     */
    List<Task> findByDeadlineBefore(LocalDate date);

    /**
     * Retrieves tasks filtered by both status and priority.
     *
     * @param status   the board column
     * @param priority the priority level
     * @return a list of tasks matching both criteria
     */
    List<Task> findByStatusAndPriority(TaskStatus status, TaskPriority priority);

    /**
     * Bulk-updates the status of a single task identified by its primary key.
     * This is used by the drag-and-drop endpoint to move cards efficiently.
     *
     * @param id     the task identifier
     * @param status the new status to apply
     */
    @Modifying
    @Query("UPDATE Task t SET t.status = :status, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") TaskStatus status);

    /**
     * Counts the number of tasks in each status column.
     *
     * @param status the column to count
     * @return the number of tasks in the given status
     */
    long countByStatus(TaskStatus status);
}
