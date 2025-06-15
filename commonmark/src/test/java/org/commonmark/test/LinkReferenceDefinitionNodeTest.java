package org.commonmark.test;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkReferenceDefinitionNodeTest {

    @Test
    public void testDefinitionWithoutParagraph() {
        Node document = parse("This is a paragraph with a [foo] link.\n\n[foo]: /url 'title'");
        List<Node> nodes = Nodes.getChildren(document);

        assertThat(nodes).hasSize(2);
        assertThat(nodes.get(0)).isInstanceOf(Paragraph.class);
        LinkReferenceDefinition definition = assertDef(nodes.get(1), "foo");

        assertThat(definition.getDestination()).isEqualTo("/url");
        assertThat(definition.getTitle()).isEqualTo("title");
    }

    @Test
    public void testDefinitionWithParagraph() {
        Node document = parse("[foo]: /url\nThis is a paragraph with a [foo] link.");
        List<Node> nodes = Nodes.getChildren(document);

        assertThat(nodes).hasSize(2);
        // Note that definition is not part of the paragraph, it's a sibling
        assertThat(nodes.get(0)).isInstanceOf(LinkReferenceDefinition.class);
        assertThat(nodes.get(1)).isInstanceOf(Paragraph.class);
    }

    @Test
    public void testMultipleDefinitions() {
        Node document = parse("This is a paragraph with a [foo] link.\n\n[foo]: /url\n[bar]: /url");
        List<Node> nodes = Nodes.getChildren(document);

        assertThat(nodes).hasSize(3);
        assertThat(nodes.get(0)).isInstanceOf(Paragraph.class);
        assertDef(nodes.get(1), "foo");
        assertDef(nodes.get(2), "bar");
    }

    @Test
    public void testMultipleDefinitionsWithSameLabel() {
        Node document = parse("This is a paragraph with a [foo] link.\n\n[foo]: /url1\n[foo]: /url2");
        List<Node> nodes = Nodes.getChildren(document);

        assertThat(nodes).hasSize(3);
        assertThat(nodes.get(0)).isInstanceOf(Paragraph.class);
        LinkReferenceDefinition def1 = assertDef(nodes.get(1), "foo");
        assertThat(def1.getDestination()).isEqualTo("/url1");
        // When there's multiple definitions with the same label, the first one "wins", as in reference links will use
        // that. But we still want to preserve the original definitions in the document.
        LinkReferenceDefinition def2 = assertDef(nodes.get(2), "foo");
        assertThat(def2.getDestination()).isEqualTo("/url2");
    }

    @Test
    public void testDefinitionOfReplacedBlock() {
        Node document = parse("[foo]: /url\nHeading\n=======");
        List<Node> nodes = Nodes.getChildren(document);

        assertThat(nodes).hasSize(2);
        assertDef(nodes.get(0), "foo");
        assertThat(nodes.get(1)).isInstanceOf(Heading.class);
    }

    @Test
    public void testDefinitionInListItem() {
        Node document = parse("* [foo]: /url\n  [foo]\n");
        assertThat(document.getFirstChild()).isInstanceOf(BulletList.class);
        Node item = document.getFirstChild().getFirstChild();
        assertThat(item).isInstanceOf(ListItem.class);

        List<Node> nodes = Nodes.getChildren(item);
        assertThat(nodes).hasSize(2);
        assertDef(nodes.get(0), "foo");
        assertThat(nodes.get(1)).isInstanceOf(Paragraph.class);
    }

    @Test
    public void testDefinitionInListItem2() {
        Node document = parse("* [foo]: /url\n* [foo]\n");
        assertThat(document.getFirstChild()).isInstanceOf(BulletList.class);

        List<Node> items = Nodes.getChildren(document.getFirstChild());
        assertThat(items).hasSize(2);
        Node item1 = items.get(0);
        Node item2 = items.get(1);

        assertThat(item1).isInstanceOf(ListItem.class);
        assertThat(item2).isInstanceOf(ListItem.class);

        assertThat(Nodes.getChildren(item1)).hasSize(1);
        assertDef(item1.getFirstChild(), "foo");

        assertThat(Nodes.getChildren(item2)).hasSize(1);
        assertThat(item2.getFirstChild()).isInstanceOf(Paragraph.class);
    }

    @Test
    public void testDefinitionLabelCaseIsPreserved() {
        Node document = parse("This is a paragraph with a [foo] link.\n\n[fOo]: /url 'title'");
        List<Node> nodes = Nodes.getChildren(document);

        assertThat(nodes).hasSize(2);
        assertThat(nodes.get(0)).isInstanceOf(Paragraph.class);
        assertDef(nodes.get(1), "fOo");
    }

    private static Node parse(String input) {
        Parser parser = Parser.builder().build();
        return parser.parse(input);
    }

    private static LinkReferenceDefinition assertDef(Node node, String label) {
        assertThat(node).isInstanceOf(LinkReferenceDefinition.class);
        LinkReferenceDefinition def = (LinkReferenceDefinition) node;
        assertThat(def.getLabel()).isEqualTo(label);
        return def;
    }
}
