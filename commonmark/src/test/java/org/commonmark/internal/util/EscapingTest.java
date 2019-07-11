package org.commonmark.internal.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EscapingTest {

    @Test
    public void testEscapeHtml() {
        assertEquals("nothing to escape", Escaping.escapeHtml("nothing to escape"));
        assertEquals("&amp;", Escaping.escapeHtml("&"));
        assertEquals("&lt;", Escaping.escapeHtml("<"));
        assertEquals("&gt;", Escaping.escapeHtml(">"));
        assertEquals("&quot;", Escaping.escapeHtml("\""));
        assertEquals("&lt; start", Escaping.escapeHtml("< start"));
        assertEquals("end &gt;", Escaping.escapeHtml("end >"));
        assertEquals("&lt; both &gt;", Escaping.escapeHtml("< both >"));
        assertEquals("&lt; middle &amp; too &gt;", Escaping.escapeHtml("< middle & too >"));
    }
}
