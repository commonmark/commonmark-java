package org.commonmark.ext.gfm.tables;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TablesTextContentTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Collections.singleton(TablesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final TextContentRenderer RENDERER = TextContentRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void oneHeadNoBody() {
        assertTableRendering("Abc|Def\n---|---", "| Abc | Def |\n| --- | --- |\n");
    }

    @Test
    public void oneColumnOneHeadNoBody() {
        String expected = "| Abc |\n| --- |\n";
        assertTableRendering("|Abc\n|---\n", expected);
        assertTableRendering("|Abc|\n|---|\n", expected);
        assertTableRendering("Abc|\n---|\n", expected);

        // Pipe required on separator
        assertIsNotTable("|Abc\n---\n");
        // Pipe required on head
        assertIsNotTable("Abc\n|---\n");
    }

    @Test
    public void oneColumnOneHeadOneBody() {
        String expected = "| Abc |\n| --- |\n| 1 |\n";
        assertTableRendering("|Abc\n|---\n|1", expected);
        assertTableRendering("|Abc|\n|---|\n|1|", expected);
        assertTableRendering("Abc|\n---|\n1|", expected);

        // Pipe required on separator
        assertIsNotTable("|Abc\n---\n|1");
    }

    @Test
    public void oneHeadOneBody() {
        assertTableRendering("Abc|Def\n---|---\n1|2", "| Abc | Def |\n| --- | --- |\n| 1 | 2 |\n");
    }

    @Test
    public void separatorMustNotHaveLessPartsThanHead() {
        assertIsNotTable("Abc|Def|Ghi\n---|---\n1|2|3");
    }

    @Test
    public void padding() {
        assertTableRendering(" Abc  | Def \n --- | --- \n 1 | 2 ", "| Abc | Def |\n| --- | --- |\n| 1 | 2 |\n");
    }

    @Test
    public void paddingWithCodeBlockIndentation() {
        assertTableRendering("Abc|Def\n---|---\n    1|2", "| Abc | Def |\n| --- | --- |\n| 1 | 2 |\n");
    }

    @Test
    public void pipesOnOutside() {
        assertTableRendering("|Abc|Def|\n|---|---|\n|1|2|", "| Abc | Def |\n| --- | --- |\n| 1 | 2 |\n");
    }

    @Test
    public void inlineElements() {
        assertTableRendering("*Abc*|Def\n---|---\n1|2", "| Abc | Def |\n| --- | --- |\n| 1 | 2 |\n");
    }

    @Test
    public void escapedPipe() {
        assertTableRendering("Abc|Def\n---|---\n1\\|2|20", "| Abc | Def |\n| --- | --- |\n| 1|2 | 20 |\n");
    }

    @Test
    public void alignLeft() {
        assertTableRendering("Abc|Def\n:---|---\n1|2", "| Abc | Def |\n| :--- | --- |\n| 1 | 2 |\n");
    }

    @Test
    public void alignRight() {
        assertTableRendering("Abc|Def\n---:|---\n1|2", "| Abc | Def |\n| ---: | --- |\n| 1 | 2 |\n");
    }

    @Test
    public void alignCenter() {
        assertTableRendering("Abc|Def\n:---:|---\n1|2", "| Abc | Def |\n| :---: | --- |\n| 1 | 2 |\n");
    }

    @Test
    public void alignCenterSecond() {
        assertTableRendering("Abc|Def\n---|:---:\n1|2", "| Abc | Def |\n| --- | :---: |\n| 1 | 2 |\n");
    }

    @Test
    public void alignLeftWithSpaces() {
        assertTableRendering("Abc|Def\n :--- |---\n1|2", "| Abc | Def |\n| :--- | --- |\n| 1 | 2 |\n");
    }

    @Test
    public void alignmentMarkerMustBeNextToDashes() {
        assertIsNotTable("Abc|Def\n: ---|---");
        assertIsNotTable("Abc|Def\n--- :|---");
        assertIsNotTable("Abc|Def\n---|: ---");
        assertIsNotTable("Abc|Def\n---|--- :");
    }

    @Test
    public void bodyCanNotHaveMoreColumnsThanHead() {
        assertTableRendering("Abc|Def\n---|---\n1|2|3", "| Abc | Def |\n| --- | --- |\n| 1 | 2 |\n");
    }

    @Test
    public void bodyWithFewerColumnsThanHeadResultsInEmptyCells() {
        assertTableRendering("Abc|Def|Ghi\n---|---|---\n1|2", "| Abc | Def | Ghi |\n| --- | --- | --- |\n| 1 | 2 | |\n");
    }

    @Test
    public void insideBlockQuote() {
        assertRendering("> Abc|Def\n> ---|---\n> 1|2", "«\n| Abc | Def |\n| --- | --- |\n| 1 | 2 |\n»");
    }

    @Test
    public void tableWithLazyContinuationLine() {
        assertTableRendering("Abc|Def\n---|---\n1|2\nlazy", "| Abc | Def |\n| --- | --- |\n| 1 | 2 |\n| lazy | |\n");
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }

    private void assertTableRendering(String source, String expectedResult) {
        assertIsTable(source);
        assertRendering(source, expectedResult);
    }

    private void assertIsTable(String input) {
        Node parsed = PARSER.parse(input).getFirstChild();
        assertEquals("Source is not a table", TableBlock.class, parsed.getClass());
    }

    private void assertIsNotTable(String input) {
        Node parsed = PARSER.parse(input).getFirstChild();
        assertNotEquals("Source is a table", TableBlock.class, parsed.getClass());
    }
}
