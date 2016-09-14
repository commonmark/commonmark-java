package org.commonmark.content.renderer;

import org.commonmark.content.TextContentWriter;
import org.commonmark.renderer.BaseNodeRendererContext;

public abstract class TextContentNodeRendererContext extends BaseNodeRendererContext<TextContentWriter> {

    /**
     * @return true for stripping new lines and render text as "single line",
     * false for keeping all line breaks.
     */
    public abstract boolean stripNewlines();
}
