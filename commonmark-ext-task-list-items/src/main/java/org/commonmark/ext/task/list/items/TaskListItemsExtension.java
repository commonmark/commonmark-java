package org.commonmark.ext.task.list.items;

import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.ext.task.list.items.internal.TaskListItemHtmlNodeRenderer;
import org.commonmark.ext.task.list.items.internal.TaskListItemMarkdownNodeRenderer;
import org.commonmark.ext.task.list.items.internal.TaskListItemPostProcessor;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory;
import org.commonmark.renderer.markdown.MarkdownRenderer;

/**
 * Extension for adding task list items.
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 *
 * @since 0.15.0
 */
public class TaskListItemsExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension,
        MarkdownRenderer.MarkdownRendererExtension {

    private TaskListItemsExtension() {
    }

    public static Extension create() {
        return new TaskListItemsExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.postProcessor(new TaskListItemPostProcessor());
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(TaskListItemHtmlNodeRenderer::new);
    }

    @Override
    public void extend(MarkdownRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new MarkdownNodeRendererFactory() {
            @Override
            public NodeRenderer create(MarkdownNodeRendererContext context) {
                return new TaskListItemMarkdownNodeRenderer(context);
            }

            @Override
            public Set<Character> getSpecialCharacters() {
                return Set.of();
            }
        });
    }
}
