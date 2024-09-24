package org.commonmark.renderer.text;

/**
 * Control how line breaks are rendered.
 */
public enum LineBreakRendering {
    /**
     * Strip all line breaks within blocks and between blocks, resulting in all the text in a single line.
     */
    STRIP,
    /**
     * Use single line breaks between blocks, not a blank line (also render all lists as tight).
     */
    COMPACT,
    /**
     * Separate blocks by a blank line (and respect tight vs loose lists).
     */
    SEPARATE_BLOCKS,
}
