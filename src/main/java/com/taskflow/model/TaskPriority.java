package com.taskflow.model;

/**
 * Enumeration representing the priority levels that can be assigned
 * to a task. Higher priority tasks should be addressed first.
 *
 * @author TaskFlow Team
 */
public enum TaskPriority {

    /** Lowest priority — address when convenient. */
    LOW("Low", 1),

    /** Standard priority — normal workflow item. */
    MEDIUM("Medium", 2),

    /** Elevated priority — should be handled soon. */
    HIGH("High", 3),

    /** Highest priority — requires immediate attention. */
    CRITICAL("Critical", 4);

    private final String strLabel;
    private final int nWeight;

    TaskPriority(String strLabel, int nWeight) {
        this.strLabel = strLabel;
        this.nWeight = nWeight;
    }

    /**
     * Returns the human-readable label for this priority.
     *
     * @return a formatted priority label
     */
    public String getLabel() {
        return strLabel;
    }

    /**
     * Returns the numeric weight used for sorting tasks by priority.
     *
     * @return an integer weight where higher values indicate higher priority
     */
    public int getWeight() {
        return nWeight;
    }
}
