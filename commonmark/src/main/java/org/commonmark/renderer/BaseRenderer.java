package org.commonmark.renderer;

import org.commonmark.node.Node;

public abstract class BaseRenderer implements Renderer {

    @Override
    public void render(Node node, Appendable output) {
        NodeRendererContext context = createContext(output);
        context.render(node);
    }


    @Override
    public String render(Node node) {
        StringBuilder sb = new StringBuilder();
        render(node, sb);
        return sb.toString();
    }

    /**
     * Create context for renderer.
     *
     * @param out the output for rendering
     * @return context for renderer
     */
    protected abstract NodeRendererContext createContext(Appendable out);
}
