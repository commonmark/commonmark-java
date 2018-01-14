package org.commonmark.ext.gfm.tables;

import java.util.Collections;
import java.util.Set;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

public class TablesTextContentTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Collections.singleton(TablesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final TextContentRenderer RENDERER = TextContentRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void oneHeadNoBody() {
        assertRendering("Abc|Def\n---|---", "Abc| Def\n");
    }

    @Test
    public void oneColumnOneHeadNoBody() {
        String expected = "Abc\n";
        assertRendering("|Abc\n|---\n", expected);
        assertRendering("|Abc|\n|---|\n", expected);
        assertRendering("Abc|\n---|\n", expected);

        // Pipe required on separator
        assertRendering("|Abc\n---\n", "|Abc");
        // Pipe required on head
        assertRendering("Abc\n|---\n", "Abc\n|---");
    }

    @Test
    public void oneColumnOneHeadOneBody() {
        String expected = "Abc\n1\n";
        assertRendering("|Abc\n|---\n|1", expected);
        assertRendering("|Abc|\n|---|\n|1|", expected);
        assertRendering("Abc|\n---|\n1|", expected);

        // Pipe required on separator
        assertRendering("|Abc\n---\n|1", "|Abc\n|1");

        // Pipe required on body
        assertRendering("|Abc\n|---\n1\n", "Abc\n\n1");
    }

    @Test
    public void oneHeadOneBody() {
        assertRendering("Abc|Def\n---|---\n1|2", "Abc| Def\n1| 2\n");
    }

    @Test
    public void separatorMustNotHaveLessPartsThanHead() {
        assertRendering("Abc|Def|Ghi\n---|---\n1|2|3", "Abc|Def|Ghi\n---|---\n1|2|3");
    }

    @Test
    public void padding() {
        assertRendering(" Abc  | Def \n --- | --- \n 1 | 2 ", "Abc| Def\n1| 2\n");
    }

    @Test
    public void paddingWithCodeBlockIndentation() {
        assertRendering("Abc|Def\n---|---\n    1|2", "Abc| Def\n1| 2\n");
    }

    @Test
    public void pipesOnOutside() {
        assertRendering("|Abc|Def|\n|---|---|\n|1|2|", "Abc| Def\n1| 2\n");
    }

    @Test
    public void inlineElements() {
        assertRendering("*Abc*|Def\n---|---\n1|2", "Abc| Def\n1| 2\n");
    }

    @Test
    public void escapedPipe() {
        assertRendering("Abc|Def\n---|---\n1\\|2|20", "Abc| Def\n1|2| 20\n");
    }

    @Test
    public void escapedBackslash() {
        assertRendering("Abc|Def\n---|---\n1\\\\|2", "Abc| Def\n1\\| 2\n");
    }

    @Test
    public void alignLeft() {
        assertRendering("Abc|Def\n:---|---\n1|2", "Abc| Def\n1| 2\n");
    }

    @Test
    public void alignRight() {
        assertRendering("Abc|Def\n---:|---\n1|2", "Abc| Def\n1| 2\n");
    }

    @Test
    public void alignCenter() {
        assertRendering("Abc|Def\n:---:|---\n1|2", "Abc| Def\n1| 2\n");
    }

    @Test
    public void alignCenterSecond() {
        assertRendering("Abc|Def\n---|:---:\n1|2", "Abc| Def\n1| 2\n");
    }

    @Test
    public void alignLeftWithSpaces() {
        assertRendering("Abc|Def\n :--- |---\n1|2", "Abc| Def\n1| 2\n");
    }

    @Test
    public void alignmentMarkerMustBeNextToDashes() {
        assertRendering("Abc|Def\n: ---|---", "Abc|Def\n: ---|---");
        assertRendering("Abc|Def\n--- :|---", "Abc|Def\n--- :|---");
        assertRendering("Abc|Def\n---|: ---", "Abc|Def\n---|: ---");
        assertRendering("Abc|Def\n---|--- :", "Abc|Def\n---|--- :");
    }

    @Test
    public void bodyCanNotHaveMoreColumnsThanHead() {
        assertRendering("Abc|Def\n---|---\n1|2|3", "Abc| Def\n1| 2\n");
    }

    @Test
    public void bodyWithFewerColumnsThanHeadResultsInEmptyCells() {
        assertRendering("Abc|Def|Ghi\n---|---|---\n1|2", "Abc| Def| Ghi\n1| 2| \n");
    }

    @Test
    public void insideBlockQuote() {
        assertRendering("> Abc|Def\n> ---|---\n> 1|2", "«\nAbc| Def\n1| 2\n»");
    }

    @Test
    public void tableEndWithoutEmptyLine() {
        assertRendering("Abc|Def\n---|---\n1|2\ntable, you are over", "Abc| Def\n1| 2\n\ntable, you are over");
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
