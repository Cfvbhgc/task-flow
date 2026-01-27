/**
 * TaskFlow - Kanban Board Drag & Drop Logic
 *
 * Implements HTML5 native drag-and-drop for moving task cards
 * between Kanban columns. On drop, an AJAX PATCH request is
 * sent to the API to persist the status change.
 */

let nDraggedTaskId = null;

/**
 * Stores the dragged task's identifier and applies a visual
 * cue (reduced opacity) to the card being moved.
 *
 * @param {DragEvent} event the dragstart event
 */
function handleDragStart(event) {
    const elCard = event.target.closest('.task-card');
    if (!elCard) return;

    nDraggedTaskId = elCard.getAttribute('data-task-id');
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', nDraggedTaskId);

    // Delay adding the class so the drag preview is rendered first
    setTimeout(function () {
        elCard.classList.add('dragging');
    }, 0);
}

/**
 * Allows the drop by preventing the default browser behaviour
 * and highlights the target column.
 *
 * @param {DragEvent} event the dragover event
 */
function handleDragOver(event) {
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';

    const elColumn = event.currentTarget;
    elColumn.classList.add('drag-over');
}

/**
 * Removes the visual highlight when the dragged card leaves
 * a column boundary.
 *
 * @param {DragEvent} event the dragleave event
 */
function handleDragLeave(event) {
    const elColumn = event.currentTarget;
    elColumn.classList.remove('drag-over');
}

/**
 * Handles the drop event: determines the target column's status,
 * sends a PATCH request to update the task, and moves the DOM
 * element on success.
 *
 * @param {DragEvent} event the drop event
 */
function handleDrop(event) {
    event.preventDefault();

    const elColumn = event.currentTarget;
    elColumn.classList.remove('drag-over');

    const strTaskId = event.dataTransfer.getData('text/plain');
    if (!strTaskId) return;

    const elKanbanColumn = elColumn.closest('.kanban-column');
    const strTargetStatus = elKanbanColumn.getAttribute('data-status');

    // Find the dragged card across all columns
    const elDraggedCard = document.querySelector(
        '.task-card[data-task-id="' + strTaskId + '"]'
    );

    if (!elDraggedCard) return;

    // Optimistically move the card in the DOM
    elDraggedCard.classList.remove('dragging');
    elColumn.appendChild(elDraggedCard);

    // Remove the "no tasks" placeholder if present
    const elEmpty = elColumn.querySelector('.empty-column');
    if (elEmpty) {
        elEmpty.remove();
    }

    // Persist the status change via AJAX
    fetch('/api/tasks/' + strTaskId + '/status', {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: strTargetStatus }),
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error('Failed to update task status');
            }
            return response.json();
        })
        .then(function (data) {
            console.log('Task ' + strTaskId + ' moved to ' + strTargetStatus);
            updateColumnCounts();
        })
        .catch(function (error) {
            console.error('Error updating task status:', error);
            // Reload the page to restore consistent state
            window.location.reload();
        });
}

/**
 * Recalculates and updates the task count badges displayed
 * in each column header.
 */
function updateColumnCounts() {
    var lstColumns = document.querySelectorAll('.kanban-column');
    lstColumns.forEach(function (elCol) {
        var nCount = elCol.querySelectorAll('.task-card').length;
        var elBadge = elCol.querySelector('.task-count');
        if (elBadge) {
            elBadge.textContent = nCount;
        }
    });
}

/**
 * Cleans up dragging class from all cards when a drag operation ends,
 * regardless of whether the drop was successful.
 */
document.addEventListener('dragend', function (event) {
    var lstCards = document.querySelectorAll('.task-card.dragging');
    lstCards.forEach(function (el) {
        el.classList.remove('dragging');
    });
});

/**
 * Auto-dismiss flash messages after a few seconds.
 */
document.addEventListener('DOMContentLoaded', function () {
    var elFlash = document.getElementById('flashMessage');
    if (elFlash) {
        setTimeout(function () {
            elFlash.style.transition = 'opacity 0.3s ease';
            elFlash.style.opacity = '0';
            setTimeout(function () {
                elFlash.remove();
            }, 300);
        }, 4000);
    }
});
