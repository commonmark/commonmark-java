package org.commonmark.ext.gfm.tables;

import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TablesTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Set.of(TablesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void mustHaveHeaderAndSeparator() {
        assertRendering("Abc|Def", "<p>Abc|Def</p>\n");
        assertRendering("Abc | Def", "<p>Abc | Def</p>\n");
    }

    @Test
    public void separatorMustBeOneOrMore() {
        assertRendering("Abc|Def\n-|-", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "</table>\n");
        assertRendering("Abc|Def\n--|--", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "</table>\n");
    }

    @Test
    public void separatorMustNotContainInvalidChars() {
        assertRendering("Abc|Def\n |-a-|---", "<p>Abc|Def\n|-a-|---</p>\n");
        assertRendering("Abc|Def\n |:--a|---", "<p>Abc|Def\n|:--a|---</p>\n");
        assertRendering("Abc|Def\n |:--a--:|---", "<p>Abc|Def\n|:--a--:|---</p>\n");
    }

    @Test
    public void separatorCanHaveLeadingSpaceThenPipe() {
        assertRendering("Abc|Def\n |---|---", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "</table>\n");
    }

    @Test
    public void separatorCanNotHaveAdjacentPipes() {
        assertRendering("Abc|Def\n---||---", "<p>Abc|Def\n---||---</p>\n");
    }

    @Test
    public void separatorNeedsPipes() {
        assertRendering("Abc|Def\n|--- ---", "<p>Abc|Def\n|--- ---</p>\n");
    }

    @Test
    public void oneHeadNoBody() {
        assertRendering("Abc|Def\n---|---", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "</table>\n");
    }

    @Test
    public void oneColumnOneHeadNoBody() {
        String expected = "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "</table>\n";
        assertRendering("|Abc\n|---\n", expected);
        assertRendering("|Abc|\n|---|\n", expected);
        assertRendering("Abc|\n---|\n", expected);

        // Pipe required on separator
        assertRendering("|Abc\n---\n", "<h2>|Abc</h2>\n");
        // Pipe required on head
        assertRendering("Abc\n|---\n", "<p>Abc\n|---</p>\n");
    }

    @Test
    public void oneColumnOneHeadOneBody() {
        String expected = "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n";
        assertRendering("|Abc\n|---\n|1", expected);
        assertRendering("|Abc|\n|---|\n|1|", expected);
        assertRendering("Abc|\n---|\n1|", expected);

        // Pipe required on separator
        assertRendering("|Abc\n---\n|1", "<h2>|Abc</h2>\n<p>|1</p>\n");
    }

    @Test
    public void oneHeadOneBody() {
        assertRendering("Abc|Def\n---|---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void spaceBeforeSeparator() {
        assertRendering("  |Abc|Def|\n  |---|---|\n  |1|2|", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void separatorMustNotHaveLessPartsThanHead() {
        assertRendering("Abc|Def|Ghi\n---|---\n1|2|3", "<p>Abc|Def|Ghi\n---|---\n1|2|3</p>\n");
    }

    @Test
    public void padding() {
        assertRendering(" Abc  | Def \n --- | --- \n 1 | 2 ", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void paddingWithCodeBlockIndentation() {
        assertRendering("Abc|Def\n---|---\n    1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void pipesOnOutside() {
        assertRendering("|Abc|Def|\n|---|---|\n|1|2|", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void pipesOnOutsideWhitespaceAfterHeader() {
        assertRendering("|Abc|Def| \n|---|---|\n|1|2|", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void pipesOnOutsideZeroLengthHeaders() {
        // This is literally what someone has done IRL - it helped to expose
        // an issue with parsing the last header cell correctly
        assertRendering("||center header||\n" +
                        "-|-------------|-\n" +
                        "1|      2      |3",
                "<table>\n" +
                        "<thead>\n" +
                        "<tr>\n" +
                        "<th></th>\n" +
                        "<th>center header</th>\n" +
                        "<th></th>\n" +
                        "</tr>\n" +
                        "</thead>\n" +
                        "<tbody>\n" +
                        "<tr>\n" +
                        "<td>1</td>\n" +
                        "<td>2</td>\n" +
                        "<td>3</td>\n" +
                        "</tr>\n" +
                        "</tbody>\n" +
                        "</table>\n");
    }

    @Test
    public void inlineElements() {
        assertRendering("*Abc*|Def\n---|---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th><em>Abc</em></th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void escapedPipe() {
        assertRendering("Abc|Def\n---|---\n1\\|2|20", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1|2</td>\n" +
                "<td>20</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void escapedBackslash() {
        // This is a bit weird in the GFM spec IMO. `1\\|2` looks like an escaped backslash, followed by a pipe
        // (so two cells). Instead, the `\|` is parsed as an escaped pipe first, so just a single cell. The inline
        // parser then gets `1\|2` which renders as `1|2`.
        assertRendering("Abc|Def\n---|---\n1\\\\|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1|2</td>\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void escapedOther() {
        // This is a tricky one. For \`, we don't want to remove the backslash when we parse the table, otherwise
        // inline parsing is wrong. So we have to be careful where we do/don't consume the backslash.
        assertRendering("Abc|Def\n---|---\n1|\\`not code`", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>`not code`</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void backslashAtEnd() {
        assertRendering("Abc|Def\n---|---\n1|2\\", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2\\</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void alignLeft() {
        assertRendering("Abc|Def\n:-|-\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"left\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"left\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
        assertRendering("Abc|Def\n:-|-\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"left\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"left\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
        assertRendering("Abc|Def\n:---|---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"left\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"left\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void alignRight() {
        assertRendering("Abc|Def\n-:|-\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"right\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"right\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
        assertRendering("Abc|Def\n--:|--\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"right\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"right\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
        assertRendering("Abc|Def\n---:|---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"right\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"right\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void alignCenter() {
        assertRendering("Abc|Def\n:-:|-\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"center\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"center\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
        assertRendering("Abc|Def\n:--:|--\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"center\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"center\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
        assertRendering("Abc|Def\n:---:|---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"center\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"center\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void alignCenterSecond() {
        assertRendering("Abc|Def\n---|:---:\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th align=\"center\">Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td align=\"center\">2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void alignLeftWithSpaces() {
        assertRendering("Abc|Def\n :--- |---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th align=\"left\">Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td align=\"left\">1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void alignmentMarkerMustBeNextToDashes() {
        assertRendering("Abc|Def\n: ---|---", "<p>Abc|Def\n: ---|---</p>\n");
        assertRendering("Abc|Def\n--- :|---", "<p>Abc|Def\n--- :|---</p>\n");
        assertRendering("Abc|Def\n---|: ---", "<p>Abc|Def\n---|: ---</p>\n");
        assertRendering("Abc|Def\n---|--- :", "<p>Abc|Def\n---|--- :</p>\n");
    }

    @Test
    public void bodyCanNotHaveMoreColumnsThanHead() {
        assertRendering("Abc|Def\n---|---\n1|2|3", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void bodyWithFewerColumnsThanHeadResultsInEmptyCells() {
        assertRendering("Abc|Def|Ghi\n---|---|---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "<th>Ghi</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void insideBlockQuote() {
        assertRendering("> Abc|Def\n> ---|---\n> 1|2", "<blockquote>\n" +
                "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</blockquote>\n");
    }

    @Test
    public void tableWithLazyContinuationLine() {
        assertRendering("Abc|Def\n---|---\n1|2\nlazy", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>lazy</td>\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void issue142() {
        assertRendering("||Alveolar|Bilabial\n" +
                        "|:--|:-:|:-:\n" +
                        "|**Plosive**|t, d|b\n" +
                        "|**Tap**|ɾ|",
                "<table>\n" +
                        "<thead>\n" +
                        "<tr>\n" +
                        "<th align=\"left\"></th>\n" +
                        "<th align=\"center\">Alveolar</th>\n" +
                        "<th align=\"center\">Bilabial</th>\n" +
                        "</tr>\n" +
                        "</thead>\n" +
                        "<tbody>\n" +
                        "<tr>\n" +
                        "<td align=\"left\"><strong>Plosive</strong></td>\n" +
                        "<td align=\"center\">t, d</td>\n" +
                        "<td align=\"center\">b</td>\n" +
                        "</tr>\n" +
                        "<tr>\n" +
                        "<td align=\"left\"><strong>Tap</strong></td>\n" +
                        "<td align=\"center\">ɾ</td>\n" +
                        "<td align=\"center\"></td>\n" +
                        "</tr>\n" +
                        "</tbody>\n" +
                        "</table>\n");
    }

    @Test
    public void danglingPipe() {
        assertRendering("Abc|Def\n" +
                "---|---\n" +
                "1|2\n" +
                "|", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "<p>|</p>\n");

        assertRendering("Abc|Def\n" +
                "---|---\n" +
                "1|2\n" +
                "  |  ", "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>Abc</th>\n" +
                "<th>Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "<p>|</p>\n");
    }

    @Test
    public void interruptsParagraph() {
        assertRendering("text\n" +
                "|a  |\n" +
                "|---|\n" +
                "|b  |", "<p>text</p>\n" +
                "<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th>a</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>b</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void attributeProviderIsApplied() {
        AttributeProviderFactory factory = new AttributeProviderFactory() {
            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                return new AttributeProvider() {
                    @Override
                    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
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
                    }
                };
            }
        };
        HtmlRenderer renderer = HtmlRenderer.builder()
                .attributeProviderFactory(factory)
                .extensions(EXTENSIONS)
                .build();
        String rendered = renderer.render(PARSER.parse("Abc|Def\n---|---\n1|2"));
        assertThat(rendered).isEqualTo("<table test=\"block\">\n" +
                "<thead test=\"head\">\n" +
                "<tr test=\"row\">\n" +
                "<th test=\"cell\">Abc</th>\n" +
                "<th test=\"cell\">Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody test=\"body\">\n" +
                "<tr test=\"row\">\n" +
                "<td test=\"cell\">1</td>\n" +
                "<td test=\"cell\">2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void columnWidthIsRecorded() {
        AttributeProviderFactory factory = new AttributeProviderFactory() {
            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                return new AttributeProvider() {
                    @Override
                    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
                        if (node instanceof TableCell && "th".equals(tagName)) {
                            attributes.put("width", ((TableCell) node).getWidth() + "em");
                        }
                    }
                };
            }
        };
        HtmlRenderer renderer = HtmlRenderer.builder()
                .attributeProviderFactory(factory)
                .extensions(EXTENSIONS)
                .build();
        String rendered = renderer.render(PARSER.parse("Abc|Def\n-----|---\n1|2"));
        assertThat(rendered).isEqualTo("<table>\n" +
                "<thead>\n" +
                "<tr>\n" +
                "<th width=\"5em\">Abc</th>\n" +
                "<th width=\"3em\">Def</th>\n" +
                "</tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>1</td>\n" +
                "<td>2</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void sourceSpans() {
        Parser parser = Parser.builder()
                .extensions(EXTENSIONS)
                .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build();
        Node document = parser.parse("Abc|Def\n---|---\n|1|2\n 3|four|\n|||\n");

        TableBlock block = (TableBlock) document.getFirstChild();
        assertThat(block.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 0, 0, 7), SourceSpan.of(1, 0, 8, 7),
                SourceSpan.of(2, 0, 16, 4), SourceSpan.of(3, 0, 21, 8), SourceSpan.of(4, 0, 30, 3)));

        TableHead head = (TableHead) block.getFirstChild();
        assertThat(head.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 0, 0, 7)));

        TableRow headRow = (TableRow) head.getFirstChild();
        assertThat(headRow.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 0, 0, 7)));
        TableCell headRowCell1 = (TableCell) headRow.getFirstChild();
        TableCell headRowCell2 = (TableCell) headRow.getLastChild();
        assertThat(headRowCell1.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 0, 0, 3)));
        assertThat(headRowCell1.getFirstChild().getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 0, 0, 3)));
        assertThat(headRowCell2.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 4, 4, 3)));
        assertThat(headRowCell2.getFirstChild().getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 4, 4, 3)));

        TableBody body = (TableBody) block.getLastChild();
        assertThat(body.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 0, 16, 4), SourceSpan.of(3, 0, 21, 8), SourceSpan.of(4, 0, 30, 3)));

        TableRow bodyRow1 = (TableRow) body.getFirstChild();
        assertThat(bodyRow1.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 0, 16, 4)));
        TableCell bodyRow1Cell1 = (TableCell) bodyRow1.getFirstChild();
        TableCell bodyRow1Cell2 = (TableCell) bodyRow1.getLastChild();
        assertThat(bodyRow1Cell1.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 1, 17, 1)));
        assertThat(bodyRow1Cell1.getFirstChild().getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 1, 17, 1)));
        assertThat(bodyRow1Cell2.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 3, 19, 1)));
        assertThat(bodyRow1Cell2.getFirstChild().getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 3, 19, 1)));

        TableRow bodyRow2 = (TableRow) body.getFirstChild().getNext();
        assertThat(bodyRow2.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(3, 0, 21, 8)));
        TableCell bodyRow2Cell1 = (TableCell) bodyRow2.getFirstChild();
        TableCell bodyRow2Cell2 = (TableCell) bodyRow2.getLastChild();
        assertThat(bodyRow2Cell1.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(3, 1, 22, 1)));
        assertThat(bodyRow2Cell1.getFirstChild().getSourceSpans()).isEqualTo(List.of(SourceSpan.of(3, 1, 22, 1)));
        assertThat(bodyRow2Cell2.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(3, 3, 24, 4)));
        assertThat(bodyRow2Cell2.getFirstChild().getSourceSpans()).isEqualTo(List.of(SourceSpan.of(3, 3, 24, 4)));

        TableRow bodyRow3 = (TableRow) body.getLastChild();
        assertThat(bodyRow3.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(4, 0, 30, 3)));
        TableCell bodyRow3Cell1 = (TableCell) bodyRow3.getFirstChild();
        TableCell bodyRow3Cell2 = (TableCell) bodyRow3.getLastChild();
        assertThat(bodyRow3Cell1.getSourceSpans()).isEqualTo(List.of());
        assertThat(bodyRow3Cell2.getSourceSpans()).isEqualTo(List.of());
    }

    @Test
    public void sourceSpansWhenInterrupting() {
        var parser = Parser.builder()
                .extensions(EXTENSIONS)
                .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
                .build();
        var document = parser.parse("a\n" +
                "bc\n" +
                "|de|\n" +
                "|---|\n" +
                "|fg|");

        var paragraph = (Paragraph) document.getFirstChild();
        var text = (Text) paragraph.getFirstChild();
        assertThat(text.getLiteral()).isEqualTo("a");
        assertThat(text.getNext()).isInstanceOf(SoftLineBreak.class);
        var text2 = (Text) text.getNext().getNext();
        assertThat(text2.getLiteral()).isEqualTo("bc");

        assertThat(paragraph.getSourceSpans()).isEqualTo(List.of(
                SourceSpan.of(0, 0, 0, 1),
                SourceSpan.of(1, 0, 2, 2)));

        var table = (TableBlock) document.getLastChild();
        assertThat(table.getSourceSpans()).isEqualTo(List.of(
                SourceSpan.of(2, 0, 5, 4),
                SourceSpan.of(3, 0, 10, 5),
                SourceSpan.of(4, 0, 16, 4)));
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
