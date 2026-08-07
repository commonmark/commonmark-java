package org.commonmark.ext.front.matter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Node;
import org.junit.jupiter.api.Test;

public class YamlFrontMatterDataTest extends YamlFrontMatterTestCase {
    private static final Set<Extension> EXTENSIONS = Set.of(YamlFrontMatterExtension.create());

    @Override
    Set<Extension> getExtensions() {
        return EXTENSIONS;
    }

    @Test
    public void simpleValue() {
        final String input =
                """
                ---
                hello: world
                ...

                great""";
        final String rendered = "<p>great</p>\n";

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).hasSize(1);
        assertThat(data.keySet().iterator().next()).isEqualTo("hello");
        assertThat(data.get("hello")).hasSize(1);
        assertThat(data.get("hello").get(0)).isEqualTo("world");

        assertRendering(input, rendered);
    }

    @Test
    public void emptyValue() {
        final String input =
                """
                ---
                key:
                ---

                great""";
        final String rendered = "<p>great</p>\n";

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).hasSize(1);
        assertThat(data.keySet().iterator().next()).isEqualTo("key");
        assertThat(data.get("key")).hasSize(0);

        assertRendering(input, rendered);
    }

    @Test
    public void listValues() {
        final String input =
                """
                ---
                list:
                  - value1
                  - value2
                ...

                great
                """;
        final String rendered = "<p>great</p>\n";

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).hasSize(1);
        assertThat(data).containsKey("list");
        assertThat(data.get("list")).hasSize(2);
        assertThat(data.get("list").get(0)).isEqualTo("value1");
        assertThat(data.get("list").get(1)).isEqualTo("value2");

        assertRendering(input, rendered);
    }

    @Test
    public void literalValue1() {
        final String input =
                """
                ---
                literal: |
                  hello markdown!
                  literal thing...
                ---

                great
                """;
        final String rendered = "<p>great</p>\n";

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).hasSize(1);
        assertThat(data).containsKey("literal");
        assertThat(data.get("literal")).hasSize(1);
        assertThat(data.get("literal").get(0)).isEqualTo("hello markdown!\nliteral thing...");

        assertRendering(input, rendered);
    }

    @Test
    public void literalValue2() {
        final String input =
                """
                ---
                literal: |
                  - hello markdown!
                ---

                great
                """;
        final String rendered = "<p>great</p>\n";

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).hasSize(1);
        assertThat(data).containsKey("literal");
        assertThat(data.get("literal")).hasSize(1);
        assertThat(data.get("literal").get(0)).isEqualTo("- hello markdown!");

        assertRendering(input, rendered);
    }

    @Test
    public void complexValues() {
        final String input =
                """
                ---
                simple: value
                literal: |
                  hello markdown!

                  literal literal
                list:
                    - value1
                    - value2
                ---
                great
                """;
        final String rendered = "<p>great</p>\n";

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).hasSize(3);

        assertThat(data).containsKey("simple");
        assertThat(data.get("simple")).hasSize(1);
        assertThat(data.get("simple").get(0)).isEqualTo("value");

        assertThat(data).containsKey("literal");
        assertThat(data.get("literal")).hasSize(1);
        assertThat(data.get("literal").get(0)).isEqualTo("hello markdown!\n\nliteral literal");

        assertThat(data).containsKey("list");
        assertThat(data.get("list")).hasSize(2);
        assertThat(data.get("list").get(0)).isEqualTo("value1");
        assertThat(data.get("list").get(1)).isEqualTo("value2");

        assertRendering(input, rendered);
    }

    @Test
    public void empty() {
        final String input =
                """
                ---
                ---
                test
                """;
        final String rendered = "<p>test</p>\n";

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).isEmpty();

        assertRendering(input, rendered);
    }

    @Test
    public void yamlInParagraph() {
        final String input =
                """
                # hello

                hello markdown world!
                ---
                hello: world
                ---
                """;
        final String rendered =
                """
                <h1>hello</h1>
                <h2>hello markdown world!</h2>
                <h2>hello: world</h2>
                """;

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).isEmpty();

        assertRendering(input, rendered);
    }

    @Test
    public void yamlOnSecondLine() {
        final String input =
                """
                hello

                ---
                hello: world
                ---
                """;
        final String rendered =
                """
                <p>hello</p>
                <hr />
                <h2>hello: world</h2>
                """;

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).isEmpty();

        assertRendering(input, rendered);
    }

    @Test
    public void nonMatchedStartTag() {
        final String input =
                """
                ----
                test
                """;
        final String rendered =
                """
                <hr />
                <p>test</p>
                """;

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).isEmpty();

        assertRendering(input, rendered);
    }

    @Test
    public void inList() {
        final String input =
                """
                * ---
                  ---
                test
                """;
        final String rendered =
                """
                <ul>
                <li>
                <hr />
                <hr />
                </li>
                </ul>
                <p>test</p>
                """;

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).isEmpty();

        assertRendering(input, rendered);
    }

    @Test
    public void visitorIgnoresOtherCustomNodes() {
        final String input =
                """
                ---
                hello: world
                ---
                """;

        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        Node document = parser.parse(input);
        document.appendChild(new TestNode());
        document.accept(visitor);

        Map<String, List<String>> data = visitor.getData();
        assertThat(data).hasSize(1);
        assertThat(data).containsKey("hello");
        assertThat(data.get("hello")).isEqualTo(List.of("world"));
    }

    @Test
    public void nodesCanBeModified() {
        final String input =
                """
                ---
                hello: world
                ---
                """;

        Node document = parser.parse(input);
        YamlFrontMatterNode node = (YamlFrontMatterNode) document.getFirstChild().getFirstChild();
        node.setKey("see");
        node.setValues(List.of("you"));

        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        document.accept(visitor);

        Map<String, List<String>> data = visitor.getData();
        assertThat(data).hasSize(1);
        assertThat(data).containsKey("see");
        assertThat(data.get("see")).isEqualTo(List.of("you"));
    }

    @Test
    public void dotInKeys() {
        final String input =
                """
                ---
                ms.author: author
                ---
                """;

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).hasSize(1);
        assertThat(data.keySet().iterator().next()).isEqualTo("ms.author");
        assertThat(data.get("ms.author")).hasSize(1);
        assertThat(data.get("ms.author").get(0)).isEqualTo("author");
    }

    @Test
    public void singleQuotedLiterals() {
        final String input =
                """
                ---
                string: 'It''s me'
                list:
                  - 'I''m here'
                ---
                """;

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).hasSize(2);
        assertThat(data.get("string").get(0)).isEqualTo("It's me");
        assertThat(data.get("list").get(0)).isEqualTo("I'm here");
    }

    @Test
    public void doubleQuotedLiteral() {
        final String input =
                """
                ---
                string: "backslash: \\\\ quote: \\""
                list:
                  - "hey"
                ---
                """;

        Map<String, List<String>> data = getFrontMatterData(input);

        assertThat(data).hasSize(2);
        assertThat(data.get("string").get(0)).isEqualTo("backslash: \\ quote: \"");
        assertThat(data.get("list").get(0)).isEqualTo("hey");
    }

    @Test
    public void contentNodesNotPresent() {
        final String input = "---" + "\nhello: world" + "\n..." + "\n" + "\ngreat";

        String content = getFrontMatterContent(input);

        assertThat(content).isEmpty();
    }

    @Test
    public void frontMatterPresent() {
        final String input = "---" + "\nhello: world" + "\n..." + "\n" + "\ngreat";

        Node document = parser.parse(input);
        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        document.accept(visitor);

        assertThat(visitor.getData()).isNotEmpty();
        assertThat(visitor.isFrontMatterPresent()).isTrue();
    }

    @Test
    public void frontMatterNotPresent() {
        final String input = "great!";

        Node document = parser.parse(input);
        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        document.accept(visitor);

        assertThat(visitor.getData()).isEmpty();
        assertThat(visitor.isFrontMatterPresent()).isFalse();
    }

    // Custom node for tests
    private static class TestNode extends CustomNode {}
}
