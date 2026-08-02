package org.commonmark.ext.front.matter;

import org.commonmark.Extension;
import org.commonmark.ext.front.matter.parser.RawContentParser;
import org.commonmark.node.Node;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class YamlFrontMatterRawContentTest extends YamlFrontMatterTestCase {
    private static final Set<Extension> EXTENSIONS = Set.of(
            YamlFrontMatterExtension.create(new RawContentParser.Factory())
    );

    @Override
    Set<Extension> getExtensions() {
        return EXTENSIONS;
    }

    @Test
    public void frontMatterAsRawContent() {
        final String input = "---" + "\n  first: foo" + "\n  second: bar" + "\n..." + "\n" + "\ngreat";
        final String rendered = "<p>great</p>\n";

        String content = getFrontMatterContent(input);

        assertThat(content).isEqualTo("  first: foo\n" + "  second: bar\n");
        assertRendering(input, rendered);
    }

    @Test
    public void indentedDashesDoNotTerminateFrontMatter() {
        final String input = "---" + "\n  first: foo" + "\n  second: |" + "\n    ---" + "\n  third: bar" + "\n..." + "\n" + "\ngreat";
        final String rendered = "<p>great</p>\n";

        String content = getFrontMatterContent(input);

        assertThat(content).isEqualTo("  first: foo\n" + "  second: |\n" + "    ---\n" + "  third: bar\n");
        assertRendering(input, rendered);
    }

    @Test
    public void contentNodeCanBeModified() {
        final String input = "---" + "\nhello: world" + "\n---" + "\n";

        Node document = parser.parse(input);
        YamlFrontMatterRawContent contentNode = (YamlFrontMatterRawContent) document.getFirstChild().getFirstChild();

        contentNode.setContent("see: you\n");

        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        document.accept(visitor);

        String content = visitor.getRawContent();
        assertThat(visitor.isFrontMatterPresent()).isTrue();
        assertThat(content).isEqualTo("see: you\n");
    }

    @Test
    public void dataNodesNotPresent() {
        final String input = "---" + "\nhello: world" + "\n..." + "\n" + "\ngreat";

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).isEmpty();
    }

    @Test
    public void frontMatterPresent() {
        final String input = "---" + "\nhello: world" + "\n..." + "\n" + "\ngreat";

        Node document = parser.parse(input);
        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        document.accept(visitor);

        assertThat(visitor.getRawContent()).isNotEmpty();
        assertThat(visitor.isFrontMatterPresent()).isTrue();
    }

    @Test
    public void frontMatterNotPresent() {
        final String input = "great!";

        Node document = parser.parse(input);
        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        document.accept(visitor);

        assertThat(visitor.getRawContent()).isEmpty();
        assertThat(visitor.isFrontMatterPresent()).isFalse();
    }
}
