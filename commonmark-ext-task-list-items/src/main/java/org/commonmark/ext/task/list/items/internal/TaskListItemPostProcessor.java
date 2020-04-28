package org.commonmark.ext.task.list.items.internal;

import org.commonmark.ext.task.list.items.TaskListItemMarker;
import org.commonmark.node.*;
import org.commonmark.parser.PostProcessor;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskListItemPostProcessor implements PostProcessor {

    private static final Pattern REGEX_TASK_LIST_ITEM = Pattern.compile("^\\[([xX\\s])]\\s+(.*)");

    @Override
    public Node process(Node node) {
        TaskListItemVisitor visitor = new TaskListItemVisitor();
        node.accept(visitor);
        return node;
    }

    private static class TaskListItemVisitor extends AbstractVisitor {

        @Override
        public void visit(ListItem listItem) {
            Node child = listItem.getFirstChild();
            if (child instanceof Paragraph) {
                Node node = child.getFirstChild();
                if (node instanceof Text) {
                    Text textNode = (Text) node;
                    Matcher matcher = REGEX_TASK_LIST_ITEM.matcher(textNode.getLiteral());
                    if (matcher.matches()) {
                        String checked = matcher.group(1);
                        boolean isChecked = Objects.equals(checked, "X") || Objects.equals(checked, "x");

                        // Add the task list item marker node as the first child of the list item.
                        listItem.prependChild(new TaskListItemMarker(isChecked));

                        // Parse the node using the input after the task marker (in other words, group 2 from the matcher).
                        // (Note that the String has been trimmed, so we should add a space between the
                        // TaskListItemMarker and the text that follows it when we come to render it).
                        textNode.setLiteral(matcher.group(2));
                    }
                }
            }
            visitChildren(listItem);
        }
    }
}
