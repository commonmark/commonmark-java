package org.commonmark.internal.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EscapingTest {

    @Test
    void testEscapeHtml() {
        assertThat(Escaping.escapeHtml("nothing to escape")).isEqualTo("nothing to escape");
        assertThat(Escaping.escapeHtml("&")).isEqualTo("&amp;");
        assertThat(Escaping.escapeHtml("<")).isEqualTo("&lt;");
        assertThat(Escaping.escapeHtml(">")).isEqualTo("&gt;");
        assertThat(Escaping.escapeHtml("\"")).isEqualTo("&quot;");
        assertThat(Escaping.escapeHtml("< start")).isEqualTo("&lt; start");
        assertThat(Escaping.escapeHtml("end >")).isEqualTo("end &gt;");
        assertThat(Escaping.escapeHtml("< both >")).isEqualTo("&lt; both &gt;");
        assertThat(Escaping.escapeHtml("< middle & too >")).isEqualTo("&lt; middle &amp; too &gt;");
    }
}
