package org.commonmark.ext.autolink;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.junit.jupiter.api.Test;

public class BareAutoLinkNodeTest {

    private static final Set<Extension> EXTENSIONS = Set.of(AutolinkExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

    @Test
    public void urlDestination() {
        var autoLink = parse("http://example.com");
        assertThat(autoLink.getAutolinkType()).isEqualTo(AutolinkType.URL);
        assertThat(autoLink.getContent()).isEqualTo("http://example.com");
        assertThat(autoLink.getDestination()).isEqualTo("http://example.com");
        assertThat(autoLink.getTitle()).isNull();
    }

    @Test
    public void emailDestination() {
        var autoLink = parse("foo@example.com");
        assertThat(autoLink.getAutolinkType()).isEqualTo(AutolinkType.EMAIL);
        assertThat(autoLink.getContent()).isEqualTo("foo@example.com");
        assertThat(autoLink.getDestination()).isEqualTo("mailto:foo@example.com");
        assertThat(autoLink.getTitle()).isNull();
    }

    @Test
    public void wwwDestination() {
        var autoLink = parse("www.example.com");
        assertThat(autoLink.getAutolinkType()).isEqualTo(AutolinkType.WWW);
        assertThat(autoLink.getContent()).isEqualTo("www.example.com");
        assertThat(autoLink.getDestination()).isEqualTo("http://www.example.com");
        assertThat(autoLink.getTitle()).isNull();
    }

    @Test
    public void bareAutoLinkHasNoChildren() {
        var autoLink = parse("http://example.com");
        assertThat(autoLink.getFirstChild()).isNull();
    }

    private BareAutoLink parse(String source) {
        var doc = PARSER.parse(source);
        return (BareAutoLink) doc.getFirstChild().getFirstChild();
    }
}
