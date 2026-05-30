package org.commonmark.ext.task.list.items.internal;

import java.util.Set;
import org.commonmark.ext.task.list.items.TaskListItemMarker;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;

public abstract class TaskListItemNodeRenderer implements NodeRenderer {
    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(TaskListItemMarker.class);
    }
}
