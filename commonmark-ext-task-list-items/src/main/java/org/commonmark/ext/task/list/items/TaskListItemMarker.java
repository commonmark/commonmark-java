package org.commonmark.ext.task.list.items;

import org.commonmark.node.CustomNode;

/**
 * A marker node indicating that a list item contains a task.
 */
public class TaskListItemMarker extends CustomNode {

    private final boolean checked;

    public TaskListItemMarker(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }
}
