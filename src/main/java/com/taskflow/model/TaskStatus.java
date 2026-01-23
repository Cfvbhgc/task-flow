package com.taskflow.model;

/**
 * Enumeration representing the possible statuses a task can occupy
 * on the Kanban board. Each value maps to a column in the UI.
 *
 * @author TaskFlow Team
 */
public enum TaskStatus {

    /** Task has not been started yet. */
    TODO("To Do"),

    /** Task is currently being worked on. */
    IN_PROGRESS("In Progress"),

    /** Task has been completed. */
    DONE("Done");

    private final String strDisplayName;

    TaskStatus(String strDisplayName) {
        this.strDisplayName = strDisplayName;
    }

    /**
     * Returns the human-readable display name for this status.
     *
     * @return a formatted string suitable for rendering in the UI
     */
    public String getDisplayName() {
        return strDisplayName;
    }
}
