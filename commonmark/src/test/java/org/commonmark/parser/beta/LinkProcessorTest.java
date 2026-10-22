package org.commonmark.parser.beta;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.commonmark.node.Link;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.test.Nodes;
import org.junit.jupiter.api.Test;

class LinkProcessorTest {

    @Test
    void linkTextIsAvailableDuringProcessing() {
        var texts = new ArrayList<String>();
        var parser =
                Parser.builder()
                        .linkProcessor(
                                (info, scanner, context) -> {
                                    var position = scanner.position();
                                    texts.add(info.text(scanner));
                                    assertThat(scanner.position()).isEqualTo(position);
                                    return LinkResult.none();
                                })
                        .build();
        parser.parse("![outer\n![inner](image)](outer)\n\n[other](url)");
        parser.parse("[another](url)");

        assertThat(texts).containsExactly("inner", "outer\n![inner](image)", "other", "another");
    }

    @Test
    void testLinkMarkerShouldNotBeIncludedByDefault() {
        // If a link marker is registered but is not processed, the built-in link processor
        // shouldn't consume it. And I think by default, other processors shouldn't consume it
        // either (by accident).
        // So requiring processors to opt into including the marker is better
        // than requiring them to opt out, because processors that look for a marker already need to
        // write some code to deal with the marker anyway,
        // and will have tests ensuring that the marker is part of the parsed node, not the text.
        var parser = Parser.builder().linkMarker('^').build();
        var doc = parser.parse("^[test](url)");
        var link = Nodes.find(doc, Link.class);
        assertThat(link.getDestination()).isEqualTo("url");
        assertThat(((Text) link.getPrevious()).getLiteral()).isEqualTo("^");
    }
}
