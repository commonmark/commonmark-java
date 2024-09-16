package org.commonmark.test;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.*;
import org.commonmark.testutil.Asserts;
import org.commonmark.testutil.RenderingTestCase;
import org.commonmark.testutil.TestResources;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HtmlRendererTest {

    @Test
    public void htmlAllowingShouldNotEscapeInlineHtml() {
        String rendered = htmlAllowingRenderer().render(parse("paragraph with <span id='foo' class=\"bar\">inline &amp; html</span>"));
        assertEquals("<p>paragraph with <span id='foo' class=\"bar\">inline &amp; html</span></p>\n", rendered);
    }

    @Test
    public void htmlAllowingShouldNotEscapeBlockHtml() {
        String rendered = htmlAllowingRenderer().render(parse("<div id='foo' class=\"bar\">block &amp;</div>"));
        assertEquals("<div id='foo' class=\"bar\">block &amp;</div>\n", rendered);
    }

    @Test
    public void htmlEscapingShouldEscapeInlineHtml() {
        String rendered = htmlEscapingRenderer().render(parse("paragraph with <span id='foo' class=\"bar\">inline &amp; html</span>"));
        // Note that &amp; is not escaped, as it's a normal text node, not part of the inline HTML.
        assertEquals("<p>paragraph with &lt;span id='foo' class=&quot;bar&quot;&gt;inline &amp; html&lt;/span&gt;</p>\n", rendered);
    }

    @Test
    public void htmlEscapingShouldEscapeHtmlBlocks() {
        String rendered = htmlEscapingRenderer().render(parse("<div id='foo' class=\"bar\">block &amp;</div>"));
        assertEquals("<p>&lt;div id='foo' class=&quot;bar&quot;&gt;block &amp;amp;&lt;/div&gt;</p>\n", rendered);
    }

    @Test
    public void textEscaping() {
        String rendered = defaultRenderer().render(parse("escaping: & < > \" '"));
        assertEquals("<p>escaping: &amp; &lt; &gt; &quot; '</p>\n", rendered);
    }

    @Test
    public void characterReferencesWithoutSemicolonsShouldNotBeParsedShouldBeEscaped() {
        String input = "[example](&#x6A&#x61&#x76&#x61&#x73&#x63&#x72&#x69&#x70&#x74&#x3A&#x61&#x6C&#x65&#x72&#x74&#x28&#x27&#x58&#x53&#x53&#x27&#x29)";
        String rendered = defaultRenderer().render(parse(input));
        assertEquals("<p><a href=\"&amp;#x6A&amp;#x61&amp;#x76&amp;#x61&amp;#x73&amp;#x63&amp;#x72&amp;#x69&amp;#x70&amp;#x74&amp;#x3A&amp;#x61&amp;#x6C&amp;#x65&amp;#x72&amp;#x74&amp;#x28&amp;#x27&amp;#x58&amp;#x53&amp;#x53&amp;#x27&amp;#x29\">example</a></p>\n", rendered);
    }

    @Test
    public void attributeEscaping() {
        Paragraph paragraph = new Paragraph();
        Link link = new Link();
        link.setDestination("&colon;");
        paragraph.appendChild(link);
        assertEquals("<p><a href=\"&amp;colon;\"></a></p>\n", defaultRenderer().render(paragraph));
    }

    @Test
    public void rawUrlsShouldNotFilterDangerousProtocols() {
        Paragraph paragraph = new Paragraph();
        Link link = new Link();
        link.setDestination("javascript:alert(5);");
        paragraph.appendChild(link);
        assertEquals("<p><a href=\"javascript:alert(5);\"></a></p>\n", rawUrlsRenderer().render(paragraph));
    }

    @Test
    public void sanitizedUrlsShouldSetRelNoFollow() {
        Paragraph paragraph = new Paragraph();
        Link link = new Link();
        link.setDestination("/exampleUrl");
        paragraph.appendChild(link);
        assertEquals("<p><a rel=\"nofollow\" href=\"/exampleUrl\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph));

        paragraph = new Paragraph();
        link = new Link();
        link.setDestination("https://google.com");
        paragraph.appendChild(link);
        assertEquals("<p><a rel=\"nofollow\" href=\"https://google.com\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph));
    }

    @Test
    public void sanitizedUrlsShouldAllowSafeProtocols() {
        Paragraph paragraph = new Paragraph();
        Link link = new Link();
        link.setDestination("http://google.com");
        paragraph.appendChild(link);
        assertEquals("<p><a rel=\"nofollow\" href=\"http://google.com\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph));

        paragraph = new Paragraph();
        link = new Link();
        link.setDestination("https://google.com");
        paragraph.appendChild(link);
        assertEquals("<p><a rel=\"nofollow\" href=\"https://google.com\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph));

        paragraph = new Paragraph();
        link = new Link();
        link.setDestination("mailto:foo@bar.example.com");
        paragraph.appendChild(link);
        assertEquals("<p><a rel=\"nofollow\" href=\"mailto:foo@bar.example.com\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph));

        String image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAAFiUAABYlAUlSJPAAAAAQSURBVBhXY/iPBVBf8P9/AG8TY51nJdgkAAAAAElFTkSuQmCC";
        paragraph = new Paragraph();
        link = new Link();
        link.setDestination(image);
        paragraph.appendChild(link);
        assertEquals("<p><a rel=\"nofollow\" href=\"" + image + "\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph));
    }

    @Test
    public void sanitizedUrlsShouldFilterDangerousProtocols() {
        Paragraph paragraph = new Paragraph();
        Link link = new Link();
        link.setDestination("javascript:alert(5);");
        paragraph.appendChild(link);
        assertEquals("<p><a rel=\"nofollow\" href=\"\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph));

        paragraph = new Paragraph();
        link = new Link();
        link.setDestination("ftp://google.com");
        paragraph.appendChild(link);
        assertEquals("<p><a rel=\"nofollow\" href=\"\"></a></p>\n", sanitizeUrlsRenderer().render(paragraph));
    }

    @Test
    public void percentEncodeUrlDisabled() {
        assertEquals("<p><a href=\"foo&amp;bar\">a</a></p>\n", defaultRenderer().render(parse("[a](foo&amp;bar)")));
        assertEquals("<p><a href=\"ä\">a</a></p>\n", defaultRenderer().render(parse("[a](ä)")));
        assertEquals("<p><a href=\"foo%20bar\">a</a></p>\n", defaultRenderer().render(parse("[a](foo%20bar)")));
    }

    @Test
    public void percentEncodeUrl() {
        // Entities are escaped anyway
        assertEquals("<p><a href=\"foo&amp;bar\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo&amp;bar)")));
        // Existing encoding is preserved
        assertEquals("<p><a href=\"foo%20bar\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo%20bar)")));
        assertEquals("<p><a href=\"foo%61\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo%61)")));
        // Invalid encoding is escaped
        assertEquals("<p><a href=\"foo%25\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo%)")));
        assertEquals("<p><a href=\"foo%25a\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo%a)")));
        assertEquals("<p><a href=\"foo%25a_\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo%a_)")));
        assertEquals("<p><a href=\"foo%25xx\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](foo%xx)")));
        // Reserved characters are preserved, except for '[' and ']'
        assertEquals("<p><a href=\"!*'();:@&amp;=+$,/?#%5B%5D\">a</a></p>\n", percentEncodingRenderer().render(parse("[a](!*'();:@&=+$,/?#[])")));
        // Unreserved characters are preserved
        assertEquals("<p><a href=\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~\">a</a></p>\n",
                percentEncodingRenderer().render(parse("[a](ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~)")));
        // Other characters are percent-encoded (LATIN SMALL LETTER A WITH DIAERESIS)
        assertEquals("<p><a href=\"%C3%A4\">a</a></p>\n",
                percentEncodingRenderer().render(parse("[a](ä)")));
        // Other characters are percent-encoded (MUSICAL SYMBOL G CLEF, surrogate pair in UTF-16)
        assertEquals("<p><a href=\"%F0%9D%84%9E\">a</a></p>\n",
                percentEncodingRenderer().render(parse("[a](\uD834\uDD1E)")));
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
        assertEquals("<pre data-code-block=\"fenced\"><code data-custom=\"info\">content\n</code></pre>\n", rendered);

        String rendered2 = renderer.render(parse("```evil\"\ncontent\n```"));
        assertEquals("<pre data-code-block=\"fenced\"><code data-custom=\"evil&quot;\">content\n</code></pre>\n", rendered2);
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
        assertEquals("<p><img src=\"/url\" test=\"hey\" /></p>\n", rendered);
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
        assertEquals(rendered, secondPass);
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
        assertEquals("<p>foo test</p>\n", rendered);
    }

    @Test
    public void orderedListStartZero() {
        assertEquals("<ol start=\"0\">\n<li>Test</li>\n</ol>\n", defaultRenderer().render(parse("0. Test\n")));
    }

    @Test
    public void imageAltTextWithSoftLineBreak() {
        assertEquals("<p><img src=\"/url\" alt=\"foo\nbar\" /></p>\n",
                defaultRenderer().render(parse("![foo\nbar](/url)\n")));
    }

    @Test
    public void imageAltTextWithHardLineBreak() {
        assertEquals("<p><img src=\"/url\" alt=\"foo\nbar\" /></p>\n",
                defaultRenderer().render(parse("![foo  \nbar](/url)\n")));
    }

    @Test
    public void imageAltTextWithEntities() {
        assertEquals("<p><img src=\"/url\" alt=\"foo \u00E4\" /></p>\n",
                defaultRenderer().render(parse("![foo &auml;](/url)\n")));
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

        assertEquals("Here I have a test <a href=\"http://www.google.com\">link</a>",
                defaultRenderer().render(document));
    }

    @Test
    public void omitSingleParagraphP() {
        var renderer = HtmlRenderer.builder().omitSingleParagraphP(true).build();
        assertEquals("hi <em>there</em>", renderer.render(parse("hi *there*")));
    }

    @Test
    public void threading() throws Exception {
        Parser parser = Parser.builder().build();
        String spec = TestResources.readAsString(TestResources.getSpec());
        final Node document = parser.parse(spec);

        final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
        String expectedRendering = htmlRenderer.render(document);

        // Render in parallel using the same HtmlRenderer instance.
        List<Future<String>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 40; i++) {
            Future<String> future = executorService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return htmlRenderer.render(document);
                }
            });
            futures.add(future);
        }

        for (Future<String> future : futures) {
            String rendering = future.get();
            assertThat(rendering, is(expectedRendering));
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
