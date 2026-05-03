package org.commonmark.ext.gfm.alerts;

import org.commonmark.node.CustomNode;

/**
 * Inline content container for the optional custom title of an {@link Alert}.
 *
 * <p>
 * When present, an {@code AlertTitle} is always the first child of an {@link Alert}.
 * Its own children are the parsed inline nodes of the title (i.e., the text after
 * the {@code [!TYPE]} marker on the same line). For example, in
 *
 * <pre>{@code
 * > [!NOTE] Custom _title_
 * > Body text
 * }</pre>
 *
 * the {@code AlertTitle} contains a {@code Text} node ({@code "Custom "}) followed
 * by an {@code Emphasis} node wrapping {@code "title"}.
 *
 * @see AlertsExtension.Builder#allowCustomTitles()
 * @see AlertsExtension.Builder#disallowCustomTitles()
 */
public class AlertTitle extends CustomNode {
}
