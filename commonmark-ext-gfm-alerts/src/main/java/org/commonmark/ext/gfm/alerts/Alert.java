package org.commonmark.ext.gfm.alerts;

import org.commonmark.node.CustomBlock;

/**
 * Alert block for highlighting important information using {@code [!TYPE]} syntax.
 *
 * @see AlertTitle
 */
public class Alert extends CustomBlock {

    private final String type;

    public Alert(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * @return Whether this alert has any body content (not including the title).
     * <p>
     *
     * - Examples where this would be {@code true}:
     *   <pre>{@code
     *   > [!NOTE]
     *   > Body text
     *   }</pre>
     *   <pre>{@code
     *   > [!NOTE] Custom title
     *   > Body text
     *   }</pre>
     *
     * - Examples where this would be {@code false}:
     *
     *   <pre>{@code
     *   > [!NOTE]
     *   }</pre>
     *   <pre>{@code
     *   > [!NOTE]
     *   >
     *   >
     *   }</pre>
     *   <pre>{@code
     *   > [!NOTE] Custom title
     *   }</pre>
     */
    public boolean hasBody() {
        var first = this.getFirstChild();
        if (first instanceof AlertTitle) {
            // Body exists if there's a sibling after AlertTitle
            return first.getNext() != null;
        } else {
            // Body exists if there are any children
            return first != null;
        }
    }
}
