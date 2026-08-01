package org.commonmark.ext.front.matter;

import org.commonmark.Extension;
import org.commonmark.ext.front.matter.extractor.YamlContentExtractor;
import org.commonmark.node.Node;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class YamlFrontMatterMarkdownRendererContentTest extends YamlFrontMatterMarkdownRendererTestCase {
    private static final Set<Extension> EXTENSIONS = Set.of(
            YamlFrontMatterExtension.create(new YamlContentExtractor.Factory())
    );

    @Override
    protected Set<Extension> getExtensions() {
        return EXTENSIONS;
    }

    @Test
    public void testRoundTripSimple() {
        assertRoundTrip("---\ntitle: My Document\n---\n\nMarkdown content\n");
    }

    @Test
    public void testAppendMissingTrailingNewline() {
        final String input = "---" + "\nhello: world" + "\n---" + "\n\ngreat";

        Node document = parser.parse(input);
        YamlFrontMatterContent contentNode = (YamlFrontMatterContent) document.getFirstChild().getFirstChild();

        contentNode.setContent("see: you    ");

        assertRenderedEquals(document, "---" + "\nsee: you    " + "\n---" + "\n\ngreat\n");
    }
}
