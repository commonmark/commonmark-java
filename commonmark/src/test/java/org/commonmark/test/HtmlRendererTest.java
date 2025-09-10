package org.commonmark.test;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.*;
import org.commonmark.testutil.TestResources;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlRendererTest {

    @Test
    public void htmlAllowingShouldNotEscapeInlineHtml() {
        String rendered = htmlAllowingRenderer().render(parse("paragraph with <span id='foo' class=\"bar\">inline &amp; html</span>"));
        assertThat(rendered).isEqualTo("<p>paragraph with <span id='foo' class=\"bar\">inline &amp; html</span></p>\n");
    }

    @Test
    public void htmlAllowingShouldNotEscapeBlockHtml() {
        String rendered = htmlAllowingRenderer().render(parse("<div id='foo' class=\"bar\">block &amp;</div>"));
        assertThat(rendered).isEqualTo("<div id='foo' class=\"bar\">block &amp;</div>\n");
    }

    @Test
    public void htmlEscapingShouldEscapeInlineHtml() {
        String rendered = htmlEscapingRenderer().render(parse("paragraph with <span id='foo' class=\"bar\">inline &amp; html</span>"));
        // Note that &amp; is not escaped, as it's a normal text node, not part of the inline HTML.
        assertThat(rendered).isEqualTo("<p>paragraph with &lt;span id='foo' class=&quot;bar&quot;&gt;inline &amp; html&lt;/span&gt;</p>\n");
    }

    @Test
    public void htmlEscapingShouldEscapeHtmlBlocks() {
        String rendered = htmlEscapingRenderer().render(parse("<div id='foo' class=\"bar\">block &amp;</div>"));
        assertThat(rendered).isEqualTo("<p>&lt;div id='foo' class=&quot;bar&quot;&gt;block &amp;amp;&lt;/div&gt;</p>\n");
    }

    @Test
    public void textEscaping() {
        String rendered = defaultRenderer().render(parse("escaping: & < > \" '"));
        assertThat(rendered).isEqualTo("<p>escaping: &amp; &lt; &gt; &quot; '</p>\n");
    }

    @Test
    public void characterReferencesWithoutSemicolonsShouldNotBeParsedShouldBeEscaped() {
        String input = "[example](&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65&#x72&#x74&#x28&#x27&#x58&#x53&#x53&#x27&#x29)";
        String rendered = defaultRenderer().render(parse(input));
        assertThat(rendered).isEqualTo("<p><a href=\"&amp;#x6A&amp;#x61&amp;#x76&amp;#x61&amp;#x73&amp;#x63&amp;#x72&amp;#x69&amp;#x70&amp;#x74&amp;#x3A&amp;#x61&amp;#x6C&amp;#x65&amp;#x72&amp;#x74&amp;#x28&amp;#x27&amp;#x58&amp;#x53&amp;#x53&amp;#x27&amp;#x29\">example</a></p>\n");
    }

    @Test
    public void attributeEscaping() {
        Paragraph paragraph = new Paragraph();
        Link link = new Link();
        link.setDestination("&colon;");
        paragraph.appendChild(link);
        assertThat(defaultRenderer().render(paragraph)).isEqualTo("<p><a href=\"&amp;colon;\"></a></p>\n");
    }

    @Test
    public void rawUrlsShouldNotFilterDangerousProtocols() {
        Paragraph paragraph = new Paragraph();
        Link link = new Link();
        link.setDestination("javascript:alert(5);");
        paragraph.appendChild(link);
        assertThat(rawUrlsRenderer().render(paragraph)).isEqualTo("<p><a href=\"javascript:alert(5);\"></a></p>\n");
    }

    @Test
    public void sanitizedUrlsShouldSetRelNoFollow() {
        Paragraph paragraph = new Paragraph();
        Link link = new Link();
        link.setDestination("/exampleUrl");
        paragraph.appendChild(link);
        assertThat(sanitizeUrlsRenderer().render(paragraph)).isEqualTo("<p><a rel=\"nofollow\" href=\"/exampleUrl\"></a></p>\n");

        paragraph = new Paragraph();
        link = new Link();
        link.setDestination("https://google.com");
        paragraph.appendChild(link);
        assertThat(sanitizeUrlsRenderer().render(paragraph)).isEqualTo("<p><a rel=\"nofollow\" href=\"https://google.com\"></a></p>\n");
    }

    @Test
    public void sanitizedUrlsShouldAllowSafeProtocols() {
        Paragraph paragraph = new Paragraph();
        Link link = new Link();
        link.setDestination("http://google.com");
        paragraph.appendChild(link);
        assertThat(sanitizeUrlsRenderer().render(paragraph)).isEqualTo("<p><a rel=\"nofollow\" href=\"http://google.com\"></a></p>\n");

        paragraph = new Paragraph();
        link = new Link();
        link.setDestination("https://google.com");
        paragraph.appendChild(link);
        assertThat(sanitizeUrlsRenderer().render(paragraph)).isEqualTo("<p><a rel=\"nofollow\" href=\"https://google.com\"></a></p>\n");

        paragraph = new Paragraph();
        link = new Link();
        link.setDestination("mailto:foo@bar.example.com");
        paragraph.appendChild(link);
        assertThat(sanitizeUrlsRenderer().render(paragraph)).isEqualTo("<p><a rel=\"nofollow\" href=\"mailto:foo@bar.example.com\"></a></p>\n");

        String image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAFiUAABYlAUlSJPAAAAAQSURBVBhXY/iPBVBf8P9/AG8TY51nJdgkAAAAAElFTkSuQmCC";
        paragraph = new Paragraph();
        link = new Link();
        link.setDestination(image);
        paragraph.appendChild(link);
        assertThat(sanitizeUrlsRenderer().render(paragraph)).isEqualTo("<p><a rel=\"nofollow\" href=\"" + image + "\"></a></p>\n");
    }

    @Test
    public void sanitizedUrlsShouldFilterDangerousProtocols() {
        Paragraph paragraph = new Paragraph();
        Link link = new Link();
        link.setDestination("javascript:alert(5);");
        paragraph.appendChild(link);
        assertThat(sanitizeUrlsRenderer().render(paragraph)).isEqualTo("<p><a rel=\"nofollow\" href=\"\"></a></p>\n");

        paragraph = new Paragraph();
        link = new Link();
        link.setDestination("ftp://google.com");
        paragraph.appendChild(link);
        assertThat(sanitizeUrlsRenderer().render(paragraph)).isEqualTo("<p><a rel=\"nofollow\" href=\"\"></a></p>\n");
    }

    @Test
    public void percentEncodeUrlDisabled() {
        assertThat(defaultRenderer().render(parse("[a](foo&amp;bar)"))).isEqualTo("<p><a href=\"foo&amp;bar\">a</a></p>\n");
        assertThat(defaultRenderer().render(parse("[a](ä)"))).isEqualTo("<p><a href=\"ä\">a</a></p>\n");
        assertThat(defaultRenderer().render(parse("[a](foo%20bar)"))).isEqualTo("<p><a href=\"foo%20bar\">a</a></p>\n");
    }

    @Test
    public void percentEncodeUrl() {
        // Entities are escaped anyway
        assertThat(percentEncodingRenderer().render(parse("[a](foo&amp;bar)"))).isEqualTo("<p><a href=\"foo&amp;bar\">a</a></p>\n");
        // Existing encoding is preserved
        assertThat(percentEncodingRenderer().render(parse("[a](foo%20bar)"))).isEqualTo("<p><a href=\"foo%20bar\">a</a></p>\n");
        assertThat(percentEncodingRenderer().render(parse("[a](foo%61)"))).isEqualTo("<p><a href=\"foo%61\">a</a></p>\n");
        // Invalid encoding is escaped
        assertThat(percentEncodingRenderer().render(parse("[a](foo%)"))).isEqualTo("<p><a href=\"foo%25\">a</a></p>\n");
        assertThat(percentEncodingRenderer().render(parse("[a](foo%a)"))).isEqualTo("<p><a href=\"foo%25a\">a</a></p>\n");
        assertThat(percentEncodingRenderer().render(parse("[a](foo%a_)"))).isEqualTo("<p><a href=\"foo%25a_\">a</a></p>\n");
        assertThat(percentEncodingRenderer().render(parse("[a](foo%xx)"))).isEqualTo("<p><a href=\"foo%25xx\">a</a></p>\n");
        // Reserved characters are preserved, except for '[' and ']'
        assertThat(percentEncodingRenderer().render(parse("[a](!*'();:@&=+$,/?#[])"))).isEqualTo("<p><a href=\"!*'();:@&amp;=+$,/?#%5B%5D\">a</a></p>\n");
        // Unreserved characters are preserved
        assertThat(percentEncodingRenderer().render(parse("[a](ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~)"))).isEqualTo("<p><a href=\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~\">a</a></p>\n");
        // Other characters are percent-encoded (LATIN SMALL LETTER A WITH DIAERESIS)
        assertThat(percentEncodingRenderer().render(parse("[a](ä)"))).isEqualTo("<p><a href=\"%C3%A4\">a</a></p>\n");
        // Other characters are percent-encoded (MUSICAL SYMBOL G CLEF, surrogate pair in UTF-16)
        assertThat(percentEncodingRenderer().render(parse("[a](\uD834\uDD1E)"))).isEqualTo("<p><a href=\"%F0%9D%84%9E\">a</a></p>\n");
    }

    @Test
    public void attributeProviderForCodeBlock() {
        AttributeProviderFactory custom = new AttributeProviderFactory() {
            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                return new AttributeProvider() {
                    @Override
                    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
                        if (node instanceof FencedCodeBlock && tagName.equals("code")) {
                            FencedCodeBlock fencedCodeBlock = (FencedCodeBlock) node;
                            // Remove the default attribute for info
                            attributes.remove("class");
                            // Put info in custom attribute instead
                            attributes.put("data-custom", fencedCodeBlock.getInfo());
                        } else if (node instanceof FencedCodeBlock && tagName.equals("pre")) {
                            attributes.put("data-code-block", "fenced");
                        }
                    }
                };
            }
        };

        HtmlRenderer renderer = HtmlRenderer.builder().attributeProviderFactory(custom).build();
        String rendered = renderer.render(parse("```info\ncontent\n```"));
        assertThat(rendered).isEqualTo("<pre data-code-block=\"fenced\"><code data-custom=\"info\">content\n</code></pre>\n");

        String rendered2 = renderer.render(parse("```evil\"\ncontent\n```"));
        assertThat(rendered2).isEqualTo("<pre data-code-block=\"fenced\"><code data-custom=\"evil&quot;\">content\n</code></pre>\n");
    }

    @Test
    public void attributeProviderForImage() {
        AttributeProviderFactory custom = new AttributeProviderFactory() {
            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                return new AttributeProvider() {
                    @Override
                    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
                        if (node instanceof Image) {
                            attributes.remove("alt");
                            attributes.put("test", "hey");
                        }
                    }
                };
            }
        };

        HtmlRenderer renderer = HtmlRenderer.builder().attributeProviderFactory(custom).build();
        String rendered = renderer.render(parse("![foo](/url)\n"));
        assertThat(rendered).isEqualTo("<p><img src=\"/url\" test=\"hey\" /></p>\n");
    }

    @Test
    public void attributeProviderFactoryNewInstanceForEachRender() {
        AttributeProviderFactory factory = new AttributeProviderFactory() {
            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                return new AttributeProvider() {
                    int i = 0;

                    @Override
                    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
                        attributes.put("key", "" + i);
                        i++;
                    }
                };
            }
        };

        HtmlRenderer renderer = HtmlRenderer.builder().attributeProviderFactory(factory).build();
        String rendered = renderer.render(parse("text node"));
        String secondPass = renderer.render(parse("text node"));
        assertThat(secondPass).isEqualTo(rendered);
    }

    @Test
    public void overrideNodeRender() {
        HtmlNodeRendererFactory nodeRendererFactory = new HtmlNodeRendererFactory() {
            @Override
            public NodeRenderer create(final HtmlNodeRendererContext context) {
                return new NodeRenderer() {
                    @Override
                    public Set<Class<? extends Node>> getNodeTypes() {
                        return Set.of(Link.class);
                    }

                    @Override
                    public void render(Node node) {
                        context.getWriter().text("test");
                    }
                };
            }
        };

        HtmlRenderer renderer = HtmlRenderer.builder().nodeRendererFactory(nodeRendererFactory).build();
        String rendered = renderer.render(parse("foo [bar](/url)"));
        assertThat(rendered).isEqualTo("<p>foo test</p>\n");
    }

    @Test
    public void orderedListStartZero() {
        assertThat(defaultRenderer().render(parse("0. Test\n"))).isEqualTo("<ol start=\"0\">\n<li>Test</li>\n</ol>\n");
    }

    @Test
    public void imageAltTextWithSoftLineBreak() {
        assertThat(defaultRenderer().render(parse("![foo\nbar](/url)\n"))).isEqualTo("<p><img src=\"/url\" alt=\"foo\nbar\" /></p>\n");
    }

    @Test
    public void imageAltTextWithHardLineBreak() {
        assertThat(defaultRenderer().render(parse("![foo  \nbar](/url)\n"))).isEqualTo("<p><img src=\"/url\" alt=\"foo\nbar\" /></p>\n");
    }

    @Test
    public void imageAltTextWithEntities() {
        assertThat(defaultRenderer().render(parse("![foo &auml;](/url)\n"))).isEqualTo("<p><img src=\"/url\" alt=\"foo \u00E4\" /></p>\n");
    }

    @Test
    public void imageAltTextWithInlines() {
        assertThat(defaultRenderer().render(parse("![_foo_ **bar** [link](/url)](/url)\n"))).isEqualTo("<p><img src=\"/url\" alt=\"foo bar link\" /></p>\n");
    }

    @Test
    public void imageAltTextWithCode() {
        assertThat(defaultRenderer().render(parse("![`foo` bar](/url)\n"))).isEqualTo("<p><img src=\"/url\" alt=\"foo bar\" /></p>\n");
    }

    @Test
    public void canRenderContentsOfSingleParagraph() {
        Node paragraphs = parse("Here I have a test [link](http://www.google.com)");
        Node paragraph = paragraphs.getFirstChild();

        Document document = new Document();
        Node child = paragraph.getFirstChild();
        while (child != null) {
            Node current = child;
            child = current.getNext();

            document.appendChild(current);
        }

        assertThat(defaultRenderer().render(document)).isEqualTo("Here I have a test <a href=\"http://www.google.com\">link</a>");
    }

    @Test
    public void omitSingleParagraphP() {
        var renderer = HtmlRenderer.builder().omitSingleParagraphP(true).build();
        assertThat(renderer.render(parse("hi *there*"))).isEqualTo("hi <em>there</em>");
    }

    @Test
    public void threading() throws Exception {
        var parser = Parser.builder().build();
        var spec = TestResources.readAsString(TestResources.getSpec());
        var document = parser.parse(spec);

        var htmlRenderer = HtmlRenderer.builder().build();
        var expectedRendering = htmlRenderer.render(document);

        // Render in parallel using the same HtmlRenderer instance.
        var futures = new ArrayList<Future<String>>();
        var executorService = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 40; i++) {
            var future = executorService.submit(() -> htmlRenderer.render(document));
            futures.add(future);
        }

        for (var future : futures) {
            var rendering = future.get();
            assertThat(rendering).isEqualTo(expectedRendering);
        }
    }

    private static HtmlRenderer defaultRenderer() {
        return HtmlRenderer.builder().build();
    }

    private static HtmlRenderer htmlAllowingRenderer() {
        return HtmlRenderer.builder().escapeHtml(false).build();
    }

    private static HtmlRenderer sanitizeUrlsRenderer() {
        return HtmlRenderer.builder().sanitizeUrls(true).urlSanitizer(new DefaultUrlSanitizer()).build();
    }

    private static HtmlRenderer rawUrlsRenderer() {
        return HtmlRenderer.builder().sanitizeUrls(false).build();
    }

    private static HtmlRenderer htmlEscapingRenderer() {
        return HtmlRenderer.builder().escapeHtml(true).build();
    }

    private static HtmlRenderer percentEncodingRenderer() {
        return HtmlRenderer.builder().percentEncodeUrls(true).build();
    }

    private static Node parse(String source) {
        return Parser.builder().build().parse(source);
    }
}
