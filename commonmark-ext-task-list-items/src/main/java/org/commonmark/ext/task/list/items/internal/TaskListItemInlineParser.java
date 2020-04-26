package org.commonmark.ext.task.list.items.internal;

import org.commonmark.ext.task.list.items.TaskListItemMarker;
import org.commonmark.internal.InlineParserImpl;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.parser.InlineParserContext;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An extension of {@link InlineParserImpl} which parses {@link TaskListItemMarker}s in {@link ListItem}s.
 */
public class TaskListItemInlineParser extends InlineParserImpl {

    private static final Pattern REGEX_BULLET = Pattern.compile("[\\-+*]?");
    private static final Pattern REGEX_TASK_LIST_ITEM = Pattern.compile("^(\\s*" + REGEX_BULLET + "\\s*)\\[([xX\\s])]\\s+(.*)");

    public TaskListItemInlineParser(InlineParserContext inlineParserContext) {
        super(inlineParserContext);
    }

    @Override
    public void parse(String input, Node node) {
        Node parent = node.getParent();
        if (parent instanceof ListItem) {
            Matcher matcher = REGEX_TASK_LIST_ITEM.matcher(input);
            if (matcher.matches()) {
                String checked = matcher.group(2);
                boolean isChecked = Objects.equals(checked, "X") || Objects.equals(checked, "x");

                // Add the task list item marker node as the first child of the parent.
                parent.prependChild(new TaskListItemMarker(isChecked));

                // Parse the node using the input after the task marker (in other words, group 3 from the matcher).
                // (Note that the String gets trimmed in the call to super.parse(..), so we should add a space between
                // the TaskListItemMarker and the text that follows it when we come to render it).
                super.parse(matcher.group(3), node);
                return;
            }
        }

        super.parse(input, node);
    }
}
