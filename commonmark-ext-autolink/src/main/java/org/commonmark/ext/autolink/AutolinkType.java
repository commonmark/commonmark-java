package org.commonmark.ext.autolink;

/**
 * The types of strings that can be automatically turned into links.
 */
public enum AutolinkType {
    /**
     * URL such as {@code http://example.com}
     */
    URL,
    /**
     * Email address such as {@code foo@example.com}
     */
    EMAIL,
    /**
     * URL such as {@code www.example.com}
     */
    WWW
}
