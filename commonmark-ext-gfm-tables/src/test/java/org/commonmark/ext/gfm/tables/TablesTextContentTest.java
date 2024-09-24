package org.commonmark.ext.gfm.tables;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.LineBreakRendering;
import org.commonmark.renderer.text.TextContentRenderer;
import org.commonmark.testutil.Asserts;
import org.junit.Test;

import java.util.Set;

public class TablesTextContentTest {

    private static final Set<Extension> EXTENSIONS = Set.of(TablesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final TextContentRenderer RENDERER = TextContentRenderer.builder().extensions(EXTENSIONS).build();

    private static final TextContentRenderer COMPACT_RENDERER = TextContentRenderer.builder().extensions(EXTENSIONS).build();
    private static final TextContentRenderer SEPARATE_RENDERER = TextContentRenderer.builder().extensions(EXTENSIONS)
            .lineBreakRendering(LineBreakRendering.SEPARATE_BLOCKS).build();
    private static final TextContentRenderer STRIPPED_RENDERER = TextContentRenderer.builder().extensions(EXTENSIONS)
            .lineBreakRendering(LineBreakRendering.STRIP).build();

    @Test
    public void oneHeadNoBody() {
        assertCompact("Abc|Def\n---|---", "Abc| Def");
    }

    @Test
    public void oneColumnOneHeadNoBody() {
        String expected = "Abc";
        assertCompact("|Abc\n|---\n", expected);
        assertCompact("|Abc|\n|---|\n", expected);
        assertCompact("Abc|\n---|\n", expected);

        // Pipe required on separator
        assertCompact("|Abc\n---\n", "|Abc");
        // Pipe required on head
        assertCompact("Abc\n|---\n", "Abc\n|---");
    }

    @Test
    public void oneColumnOneHeadOneBody() {
        String expected = "Abc\n1";
        assertCompact("|Abc\n|---\n|1", expected);
        assertCompact("|Abc|\n|---|\n|1|", expected);
        assertCompact("Abc|\n---|\n1|", expected);

        // Pipe required on separator
        assertCompact("|Abc\n---\n|1", "|Abc\n|1");
    }

    @Test
    public void oneHeadOneBody() {
        assertCompact("Abc|Def\n---|---\n1|2", "Abc| Def\n1| 2");
    }

    @Test
    public void separatorMustNotHaveLessPartsThanHead() {
        assertCompact("Abc|Def|Ghi\n---|---\n1|2|3", "Abc|Def|Ghi\n---|---\n1|2|3");
    }

    @Test
    public void padding() {
        assertCompact(" Abc  | Def \n --- | --- \n 1 | 2 ", "Abc| Def\n1| 2");
    }

    @Test
    public void paddingWithCodeBlockIndentation() {
        assertCompact("Abc|Def\n---|---\n    1|2", "Abc| Def\n1| 2");
    }

    @Test
    public void pipesOnOutside() {
        assertCompact("|Abc|Def|\n|---|---|\n|1|2|", "Abc| Def\n1| 2");
    }

    @Test
    public void inlineElements() {
        assertCompact("*Abc*|Def\n---|---\n1|2", "Abc| Def\n1| 2");
    }

    @Test
    public void escapedPipe() {
        assertCompact("Abc|Def\n---|---\n1\\|2|20", "Abc| Def\n1|2| 20");
    }

    @Test
    public void alignLeft() {
        assertCompact("Abc|Def\n:---|---\n1|2", "Abc| Def\n1| 2");
    }

    @Test
    public void alignRight() {
        assertCompact("Abc|Def\n---:|---\n1|2", "Abc| Def\n1| 2");
    }

    @Test
    public void alignCenter() {
        assertCompact("Abc|Def\n:---:|---\n1|2", "Abc| Def\n1| 2");
    }

    @Test
    public void alignCenterSecond() {
        assertCompact("Abc|Def\n---|:---:\n1|2", "Abc| Def\n1| 2");
    }

    @Test
    public void alignLeftWithSpaces() {
        assertCompact("Abc|Def\n :--- |---\n1|2", "Abc| Def\n1| 2");
    }

    @Test
    public void alignmentMarkerMustBeNextToDashes() {
        assertCompact("Abc|Def\n: ---|---", "Abc|Def\n: ---|---");
        assertCompact("Abc|Def\n--- :|---", "Abc|Def\n--- :|---");
        assertCompact("Abc|Def\n---|: ---", "Abc|Def\n---|: ---");
        assertCompact("Abc|Def\n---|--- :", "Abc|Def\n---|--- :");
    }

    @Test
    public void bodyCanNotHaveMoreColumnsThanHead() {
        assertCompact("Abc|Def\n---|---\n1|2|3", "Abc| Def\n1| 2");
    }

    @Test
    public void bodyWithFewerColumnsThanHeadResultsInEmptyCells() {
        assertCompact("Abc|Def|Ghi\n---|---|---\n1|2", "Abc| Def| Ghi\n1| 2| ");
    }

    @Test
    public void insideBlockQuote() {
        assertCompact("> Abc|Def\n> ---|---\n> 1|2", "«Abc| Def\n1| 2»");
    }

    @Test
    public void tableWithLazyContinuationLine() {
        assertCompact("Abc|Def\n---|---\n1|2\nlazy", "Abc| Def\n1| 2\nlazy| ");
    }

    @Test
    public void tableBetweenOtherBlocks() {
        var s = "Foo\n\nAbc|Def\n---|---\n1|2\n\nBar";
        assertCompact(s, "Foo\nAbc| Def\n1| 2\nBar");
        assertSeparate(s, "Foo\n\nAbc| Def\n1| 2\n\nBar");
        assertStripped(s, "Foo Abc| Def 1| 2 Bar");
    }

    private void assertCompact(String source, String expected) {
        var doc = PARSER.parse(source);
        var actualRendering = COMPACT_RENDERER.render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }

    private void assertSeparate(String source, String expected) {
        var doc = PARSER.parse(source);
        var actualRendering = SEPARATE_RENDERER.render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }

    private void assertStripped(String source, String expected) {
        var doc = PARSER.parse(source);
        var actualRendering = STRIPPED_RENDERER.render(doc);
        Asserts.assertRendering(source, expected, actualRendering);
    }
}
