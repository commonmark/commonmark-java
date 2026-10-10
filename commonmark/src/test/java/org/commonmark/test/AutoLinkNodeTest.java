package org.commonmark.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.junit.jupiter.api.Test;

public class AutoLinkNodeTest {

    private static final Parser PARSER = Parser.builder().build();

    @Test
    public void uriAutoLink() {
        var doc = PARSER.parse("<http://example.com>");
        var autoLink = (AutoLink) doc.getFirstChild().getFirstChild();
        assertThat(autoLink.isEmail()).isFalse();
        assertThat(autoLink.getContent()).isEqualTo("http://example.com");
        assertThat(autoLink.getDestination()).isEqualTo("http://example.com");
        assertThat(autoLink.getTitle()).isNull();
    }

    @Test
    public void emailAutoLink() {
        var doc = PARSER.parse("<foo@example.com>");
        var autoLink = (AutoLink) doc.getFirstChild().getFirstChild();
        assertThat(autoLink.isEmail()).isTrue();
        assertThat(autoLink.getContent()).isEqualTo("foo@example.com");
        assertThat(autoLink.getDestination()).isEqualTo("mailto:foo@example.com");
        assertThat(autoLink.getTitle()).isNull();
    }

    @Test
    public void autoLinkHasNoChildren() {
        var doc = PARSER.parse("<http://example.com>");
        var autoLink = (AutoLink) doc.getFirstChild().getFirstChild();
        assertThat(autoLink.getFirstChild()).isNull();
    }
}
