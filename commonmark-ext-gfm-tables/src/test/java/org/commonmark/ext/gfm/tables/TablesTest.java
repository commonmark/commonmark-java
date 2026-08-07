package org.commonmark.ext.gfm.tables;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.jupiter.api.Test;

public class TablesTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Set.of(TablesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER =
            HtmlRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void mustHaveHeaderAndSeparator() {
        assertRendering("Abc|Def", "<p>Abc|Def</p>\n");
        assertRendering("Abc | Def", "<p>Abc | Def</p>\n");
    }

    @Test
    public void separatorMustBeOneOrMore() {
        assertRendering(
                """
                Abc|Def
                -|-
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                </table>
                """);
        assertRendering(
                """
                Abc|Def
                --|--
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                </table>
                """);
    }

    @Test
    public void separatorMustNotContainInvalidChars() {
        assertRendering(
                """
                Abc|Def
                |-a-|---
                """,
                """
                <p>Abc|Def
                |-a-|---</p>
                """);
        assertRendering(
                """
                Abc|Def
                |:--a|---
                """,
                """
                <p>Abc|Def
                |:--a|---</p>
                """);
        assertRendering(
                """
                Abc|Def
                |:--a--:|---
                """,
                """
                <p>Abc|Def
                |:--a--:|---</p>
                """);
    }

    @Test
    public void separatorCanHaveLeadingSpaceThenPipe() {
        assertRendering(
                """
                Abc|Def
                 |---|---
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                </table>
                """);
    }

    @Test
    public void separatorCanNotHaveAdjacentPipes() {
        assertRendering(
                """
                Abc|Def
                ---||---
                """,
                """
                <p>Abc|Def
                ---||---</p>
                """);
    }

    @Test
    public void separatorNeedsPipes() {
        assertRendering(
                """
                Abc|Def
                |--- ---
                """,
                """
                <p>Abc|Def
                |--- ---</p>
                """);
    }

    @Test
    public void oneHeadNoBody() {
        assertRendering(
                """
                Abc|Def
                ---|---
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                </table>
                """);
    }

    @Test
    public void oneColumnOneHeadNoBody() {
        String expected =
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                </tr>
                </thead>
                </table>
                """;
        assertRendering(
                """
                |Abc
                |---
                """,
                expected);
        assertRendering(
                """
                |Abc|
                |---|
                """,
                expected);
        assertRendering(
                """
                Abc|
                ---|
                """,
                expected);

        // Pipe required on separator
        assertRendering(
                """
                |Abc
                ---
                """,
                """
                <h2>|Abc</h2>
                """);
        // Pipe required on head
        assertRendering(
                """
                Abc
                |---
                """,
                """
                <p>Abc
                |---</p>
                """);
    }

    @Test
    public void oneColumnOneHeadOneBody() {
        String expected =
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                </tr>
                </tbody>
                </table>
                """;
        assertRendering(
                """
                |Abc
                |---
                |1
                """,
                expected);
        assertRendering(
                """
                |Abc|
                |---|
                |1|
                """,
                expected);
        assertRendering(
                """
                Abc|
                ---|
                1|
                """,
                expected);

        // Pipe required on separator
        assertRendering(
                """
                |Abc
                ---
                |1
                """,
                """
                <h2>|Abc</h2>
                <p>|1</p>
                """);
    }

    @Test
    public void oneHeadOneBody() {
        assertRendering(
                """
                Abc|Def
                ---|---
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void spaceBeforeSeparator() {
        assertRendering(
                """
                |Abc|Def|
                |---|---|
                |1|2|
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void separatorMustNotHaveLessPartsThanHead() {
        assertRendering(
                """
                Abc|Def|Ghi
                ---|---
                1|2|3
                """,
                """
                <p>Abc|Def|Ghi
                ---|---
                1|2|3</p>
                """);
    }

    @Test
    public void padding() {
        assertRendering(
                """
                 Abc  | Def\s
                 --- | ---\s
                 1 | 2\s
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void paddingWithCodeBlockIndentation() {
        assertRendering(
                """
                Abc|Def
                ---|---
                    1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void pipesOnOutside() {
        assertRendering(
                """
                |Abc|Def|
                |---|---|
                |1|2|
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void pipesOnOutsideWhitespaceAfterHeader() {
        assertRendering(
                """
                |Abc|Def|\s
                |---|---|
                |1|2|
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void pipesOnOutsideZeroLengthHeaders() {
        // This is literally what someone has done IRL - it helped to expose
        // an issue with parsing the last header cell correctly
        assertRendering(
                """
                ||center header||
                -|-------------|-
                1|      2      |3
                """,
                """
                <table>
                <thead>
                <tr>
                <th></th>
                <th>center header</th>
                <th></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                <td>3</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void inlineElements() {
        assertRendering(
                """
                *Abc*|Def
                ---|---
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th><em>Abc</em></th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void escapedPipe() {
        assertRendering(
                """
                Abc|Def
                ---|---
                1\\|2|20
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1|2</td>
                <td>20</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void escapedBackslash() {
        // This is a bit weird in the GFM spec IMO. `1\\|2` looks like an escaped backslash,
        // followed by a pipe (so two cells). Instead, the `\|` is parsed as an escaped pipe first,
        // so just a single cell. The inline parser then gets `1\|2` which renders as `1|2`.
        assertRendering(
                """
                Abc|Def
                ---|---
                1\\\\|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1|2</td>
                <td></td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void escapedOther() {
        // This is a tricky one. For \`, we don't want to remove the backslash when we parse the
        // table, otherwise inline parsing is wrong. So we have to be careful where we do/don't
        // consume the backslash.
        assertRendering(
                """
                Abc|Def
                ---|---
                1|\\`not code`
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>`not code`</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void backslashAtEnd() {
        assertRendering(
                """
                Abc|Def
                ---|---
                1|2\\
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2\\</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void alignLeft() {
        assertRendering(
                """
                Abc|Def
                :-|-
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th align="left">Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td align="left">1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
        assertRendering(
                """
                Abc|Def
                :-|-
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th align="left">Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td align="left">1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
        assertRendering(
                """
                Abc|Def
                :---|---
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th align="left">Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td align="left">1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void alignRight() {
        assertRendering(
                """
                Abc|Def
                -:|-
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th align="right">Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td align="right">1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
        assertRendering(
                """
                Abc|Def
                --:|--
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th align="right">Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td align="right">1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
        assertRendering(
                """
                Abc|Def
                ---:|---
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th align="right">Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td align="right">1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void alignCenter() {
        assertRendering(
                """
                Abc|Def
                :-:|-
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th align="center">Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td align="center">1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
        assertRendering(
                """
                Abc|Def
                :--:|--
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th align="center">Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td align="center">1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
        assertRendering(
                """
                Abc|Def
                :---:|---
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th align="center">Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td align="center">1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void alignCenterSecond() {
        assertRendering(
                """
                Abc|Def
                ---|:---:
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th align="center">Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td align="center">2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void alignLeftWithSpaces() {
        assertRendering(
                """
                Abc|Def
                 :--- |---
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th align="left">Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td align="left">1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void alignmentMarkerMustBeNextToDashes() {
        assertRendering(
                """
                Abc|Def
                : ---|---
                """,
                """
                <p>Abc|Def
                : ---|---</p>
                """);
        assertRendering(
                """
                Abc|Def
                --- :|---
                """,
                """
                <p>Abc|Def
                --- :|---</p>
                """);
        assertRendering(
                """
                Abc|Def
                ---|: ---
                """,
                """
                <p>Abc|Def
                ---|: ---</p>
                """);
        assertRendering(
                """
                Abc|Def
                ---|--- :
                """,
                """
                <p>Abc|Def
                ---|--- :</p>
                """);
    }

    @Test
    public void bodyCanNotHaveMoreColumnsThanHead() {
        assertRendering(
                """
                Abc|Def
                ---|---
                1|2|3
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void bodyWithFewerColumnsThanHeadResultsInEmptyCells() {
        assertRendering(
                """
                Abc|Def|Ghi
                ---|---|---
                1|2
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                <th>Ghi</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                <td></td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void insideBlockQuote() {
        assertRendering(
                """
                > Abc|Def
                > ---|---
                > 1|2
                """,
                """
                <blockquote>
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                </blockquote>
                """);
    }

    @Test
    public void tableWithLazyContinuationLine() {
        assertRendering(
                """
                Abc|Def
                ---|---
                1|2
                lazy
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                </tr>
                <tr>
                <td>lazy</td>
                <td></td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void issue142() {
        assertRendering(
                """
                ||Alveolar|Bilabial
                |:--|:-:|:-:
                |**Plosive**|t, d|b
                |**Tap**|ɾ|""",
                """
                <table>
                <thead>
                <tr>
                <th align="left"></th>
                <th align="center">Alveolar</th>
                <th align="center">Bilabial</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td align="left"><strong>Plosive</strong></td>
                <td align="center">t, d</td>
                <td align="center">b</td>
                </tr>
                <tr>
                <td align="left"><strong>Tap</strong></td>
                <td align="center">ɾ</td>
                <td align="center"></td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void danglingPipe() {
        assertRendering(
                """
                Abc|Def
                ---|---
                1|2
                |
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                <p>|</p>
                """);

        assertRendering(
                """
                Abc|Def
                ---|---
                1|2
                  | \s
                """,
                """
                <table>
                <thead>
                <tr>
                <th>Abc</th>
                <th>Def</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>1</td>
                <td>2</td>
                </tr>
                </tbody>
                </table>
                <p>|</p>
                """);
    }

    @Test
    public void interruptsParagraph() {
        assertRendering(
                """
                text
                |a  |
                |---|
                |b  |
                """,
                """
                <p>text</p>
                <table>
                <thead>
                <tr>
                <th>a</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                <td>b</td>
                </tr>
                </tbody>
                </table>
                """);
    }

    @Test
    public void attributeProviderIsApplied() {
        AttributeProviderFactory factory =
                context ->
                        (node, tagName, attributes) -> {
                            if (node instanceof TableBlock) {
                                attributes.put("test", "block");
                            } else if (node instanceof TableHead) {
                                attributes.put("test", "head");
                            } else if (node instanceof TableBody) {
                                attributes.put("test", "body");
                            } else if (node instanceof TableRow) {
                                attributes.put("test", "row");
                            } else if (node instanceof TableCell) {
                                attributes.put("test", "cell");
                            }
                        };
        HtmlRenderer renderer =
                HtmlRenderer.builder()
                        .attributeProviderFactory(factory)
                        .extensions(EXTENSIONS)
                        .build();
        String rendered = renderer.render(PARSER.parse("Abc|Def\n---|---\n1|2"));
        assertThat(rendered)
                .isEqualTo(
                        """
                        <table test="block">
                        <thead test="head">
                        <tr test="row">
                        <th test="cell">Abc</th>
                        <th test="cell">Def</th>
                        </tr>
                        </thead>
                        <tbody test="body">
                        <tr test="row">
                        <td test="cell">1</td>
                        <td test="cell">2</td>
                        </tr>
                        </tbody>
                        </table>
                        """);
    }

    @Test
    public void columnWidthIsRecorded() {
        AttributeProviderFactory factory =
                context ->
                        (node, tagName, attributes) -> {
                            if (node instanceof TableCell && "th".equals(tagName)) {
                                attributes.put("width", ((TableCell) node).getWidth() + "em");
                            }
                        };
        HtmlRenderer renderer =
                HtmlRenderer.builder()
                        .attributeProviderFactory(factory)
                        .extensions(EXTENSIONS)
                        .build();
        String rendered = renderer.render(PARSER.parse("Abc|Def\n-----|---\n1|2"));
        assertThat(rendered)
                .isEqualTo(
                        """
                        <table>
                        <thead>
                        <tr>
                        <th width="5em">Abc</th>
                        <th width="3em">Def</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                        <td>1</td>
                        <td>2</td>
                        </tr>
                        </tbody>
                        </table>
                        """);
    }

    @Test
    public void testErrorsWhenMaxCellsLimitExceeded() {
        var extension = TablesExtension.builder().maxCells(6).build();
        var parser = Parser.builder().extensions(List.of(extension)).build();
        // Note that even omitted cells count towards the limit as they result in an empty cell
        var input = " A|B|C\n-|-|-\n1|2\nbad";
        assertThatThrownBy(() -> parser.parse(input))
                .hasMessage(
                        "Aborting parsing because maximum number of cells reached (maxCells = 6)");
    }

    @Test
    public void sourceSpans() {
        Parser parser =
                Parser.builder()
                        .extensions(EXTENSIONS)
                        .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                        .build();
        Node document = parser.parse("Abc|Def\n---|---\n|1|2\n 3|four|\n|||\n");

        TableBlock block = (TableBlock) document.getFirstChild();
        assertThat(block.getSourceSpans())
                .isEqualTo(
                        List.of(
                                SourceSpan.of(0, 0, 0, 7),
                                SourceSpan.of(1, 0, 8, 7),
                                SourceSpan.of(2, 0, 16, 4),
                                SourceSpan.of(3, 0, 21, 8),
                                SourceSpan.of(4, 0, 30, 3)));

        TableHead head = (TableHead) block.getFirstChild();
        assertThat(head.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 0, 0, 7)));

        TableRow headRow = (TableRow) head.getFirstChild();
        assertThat(headRow.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 0, 0, 7)));
        TableCell headRowCell1 = (TableCell) headRow.getFirstChild();
        TableCell headRowCell2 = (TableCell) headRow.getLastChild();
        assertThat(headRowCell1.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 0, 0, 3)));
        assertThat(headRowCell1.getFirstChild().getSourceSpans())
                .isEqualTo(List.of(SourceSpan.of(0, 0, 0, 3)));
        assertThat(headRowCell2.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 4, 4, 3)));
        assertThat(headRowCell2.getFirstChild().getSourceSpans())
                .isEqualTo(List.of(SourceSpan.of(0, 4, 4, 3)));

        TableBody body = (TableBody) block.getLastChild();
        assertThat(body.getSourceSpans())
                .isEqualTo(
                        List.of(
                                SourceSpan.of(2, 0, 16, 4),
                                SourceSpan.of(3, 0, 21, 8),
                                SourceSpan.of(4, 0, 30, 3)));

        TableRow bodyRow1 = (TableRow) body.getFirstChild();
        assertThat(bodyRow1.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 0, 16, 4)));
        TableCell bodyRow1Cell1 = (TableCell) bodyRow1.getFirstChild();
        TableCell bodyRow1Cell2 = (TableCell) bodyRow1.getLastChild();
        assertThat(bodyRow1Cell1.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 1, 17, 1)));
        assertThat(bodyRow1Cell1.getFirstChild().getSourceSpans())
                .isEqualTo(List.of(SourceSpan.of(2, 1, 17, 1)));
        assertThat(bodyRow1Cell2.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 3, 19, 1)));
        assertThat(bodyRow1Cell2.getFirstChild().getSourceSpans())
                .isEqualTo(List.of(SourceSpan.of(2, 3, 19, 1)));

        TableRow bodyRow2 = (TableRow) body.getFirstChild().getNext();
        assertThat(bodyRow2.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(3, 0, 21, 8)));
        TableCell bodyRow2Cell1 = (TableCell) bodyRow2.getFirstChild();
        TableCell bodyRow2Cell2 = (TableCell) bodyRow2.getLastChild();
        assertThat(bodyRow2Cell1.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(3, 1, 22, 1)));
        assertThat(bodyRow2Cell1.getFirstChild().getSourceSpans())
                .isEqualTo(List.of(SourceSpan.of(3, 1, 22, 1)));
        assertThat(bodyRow2Cell2.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(3, 3, 24, 4)));
        assertThat(bodyRow2Cell2.getFirstChild().getSourceSpans())
                .isEqualTo(List.of(SourceSpan.of(3, 3, 24, 4)));

        TableRow bodyRow3 = (TableRow) body.getLastChild();
        assertThat(bodyRow3.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(4, 0, 30, 3)));
        TableCell bodyRow3Cell1 = (TableCell) bodyRow3.getFirstChild();
        TableCell bodyRow3Cell2 = (TableCell) bodyRow3.getLastChild();
        assertThat(bodyRow3Cell1.getSourceSpans()).isEqualTo(List.of());
        assertThat(bodyRow3Cell2.getSourceSpans()).isEqualTo(List.of());
    }

    @Test
    public void sourceSpansWhenInterrupting() {
        var parser =
                Parser.builder()
                        .extensions(EXTENSIONS)
                        .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                        .build();
        var document =
                parser.parse(
                        """
                        a
                        bc
                        |de|
                        |---|
                        |fg|
                        """);

        var paragraph = (Paragraph) document.getFirstChild();
        var text = (Text) paragraph.getFirstChild();
        assertThat(text.getLiteral()).isEqualTo("a");
        assertThat(text.getNext()).isInstanceOf(SoftLineBreak.class);
        var text2 = (Text) text.getNext().getNext();
        assertThat(text2.getLiteral()).isEqualTo("bc");

        assertThat(paragraph.getSourceSpans())
                .isEqualTo(List.of(SourceSpan.of(0, 0, 0, 1), SourceSpan.of(1, 0, 2, 2)));

        var table = (TableBlock) document.getLastChild();
        assertThat(table.getSourceSpans())
                .isEqualTo(
                        List.of(
                                SourceSpan.of(2, 0, 5, 4),
                                SourceSpan.of(3, 0, 10, 5),
                                SourceSpan.of(4, 0, 16, 4)));
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
