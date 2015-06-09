package org.commonmark.extras.test;

import org.commonmark.extras.TextFusingVisitor;
import org.commonmark.node.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TextFusingVisitorTest {

    @Test
    public void alreadySingleText() {
        Paragraph paragraph = new Paragraph();
        paragraph.appendChild(new Text("a"));
        process(paragraph);
        assertText(paragraph.getFirstChild(), "a");
        assertNull(paragraph.getFirstChild().getNext());
        assertText(paragraph.getLastChild(), "a");
    }

    @Test
    public void twoTexts() {
        Paragraph paragraph = new Paragraph();
        paragraph.appendChild(new Text("a"));
        paragraph.appendChild(new Text("b"));
        process(paragraph);
        assertText(paragraph.getFirstChild(), "ab");
        assertNull(paragraph.getFirstChild().getNext());
        assertText(paragraph.getLastChild(), "ab");
    }

    @Test
    public void threeTexts() {
        Paragraph paragraph = new Paragraph();
        paragraph.appendChild(new Text("a"));
        paragraph.appendChild(new Text("b"));
        paragraph.appendChild(new Text("c"));
        process(paragraph);
        assertText(paragraph.getFirstChild(), "abc");
        assertNull(paragraph.getFirstChild().getNext());
        assertText(paragraph.getLastChild(), "abc");
    }

    @Test
    public void twoTextsOtherThenTwoTexts() {
        Paragraph paragraph = new Paragraph();
        paragraph.appendChild(new Text("a"));
        paragraph.appendChild(new Text("b"));
        paragraph.appendChild(new Code());
        paragraph.appendChild(new Text("c"));
        paragraph.appendChild(new Text("d"));
        process(paragraph);
        assertText(paragraph.getFirstChild(), "ab");
        assertTrue(paragraph.getFirstChild().getNext() instanceof Code);
        assertText(paragraph.getFirstChild().getNext().getNext(), "cd");
        assertNull(paragraph.getFirstChild().getNext().getNext().getNext());
        assertText(paragraph.getLastChild(), "cd");
    }

    @Test
    public void listItemWithTextCodeText() {
        ListBlock listBlock = new BulletList();
        ListItem listItem = new ListItem();
        listBlock.appendChild(listItem);

        listItem.appendChild(new Text("a"));
        listItem.appendChild(new Code("b"));
        listItem.appendChild(new Text("c"));

        process(listBlock);

        assertText(listItem.getFirstChild(), "a");
        assertTrue(listItem.getFirstChild().getNext() instanceof Code);
        assertText(listItem.getFirstChild().getNext().getNext(), "c");
        assertNull(listItem.getFirstChild().getNext().getNext().getNext());
        assertText(listItem.getLastChild(), "c");
    }

    private void assertText(Node node, String expectedText) {
        assertTrue("Expected Text node, but was " + node, node instanceof Text);
        Text text = (Text) node;
        assertEquals(expectedText, text.getLiteral());
    }

    private void process(Node input) {
        TextFusingVisitor textFusingVisitor = new TextFusingVisitor();
        input.accept(textFusingVisitor);
    }
}
