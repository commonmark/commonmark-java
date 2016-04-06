package org.commonmark.html;

import org.commonmark.Extension;
import org.commonmark.internal.util.Escaping;
import org.commonmark.node.*;

import java.util.*;

/**
 * Renders a tree of nodes to HTML.
 * <p>
 * Start with the {@link #builder} method to configure the renderer. Example:
 * <pre><code>
 * HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
 * renderer.render(node);
 * </code></pre>
 */
public class HtmlRenderer {

    private static final Map<String, String> NO_ATTRIBUTES = Collections.emptyMap();

    private final String softbreak;
    private final boolean escapeHtml;
    private final boolean percentEncodeUrls;
    private final List<CustomHtmlRenderer> customHtmlRenderers;
    private final List<AttributeProvider> attributeProviders;

    private HtmlRenderer(Builder builder) {
        this.softbreak = builder.softbreak;
        this.escapeHtml = builder.escapeHtml;
        this.percentEncodeUrls = builder.percentEncodeUrls;
        this.customHtmlRenderers = builder.customHtmlRenderers;
        this.attributeProviders = builder.attributeProviders;
    }

    /**
     * Create a new builder for configuring an {@link HtmlRenderer}.
     *
     * @return a builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public void render(Node node, Appendable output) {
        RendererVisitor rendererVisitor = new RendererVisitor(new HtmlWriter(output), customHtmlRenderers);
        node.accept(rendererVisitor);
    }

    /**
     * Render the tree of nodes to HTML.
     * @param node the root node
     * @return the rendered HTML
     */
    public String render(Node node) {
        StringBuilder sb = new StringBuilder();
        render(node, sb);
        return sb.toString();
    }

    private String escape(String input, boolean preserveEntities) {
        return Escaping.escapeHtml(input, preserveEntities);
    }

    private String optionallyPercentEncodeUrl(String url) {
        if (percentEncodeUrls) {
            return Escaping.percentEncodeUrl(url);
        } else {
            return url;
        }
    }

    /**
     * Builder for configuring an {@link HtmlRenderer}. See methods for default configuration.
     */
    public static class Builder {

        private String softbreak = "\n";
        private boolean escapeHtml = false;
        private boolean percentEncodeUrls = false;
        private List<CustomHtmlRenderer> customHtmlRenderers = new ArrayList<>();
        private List<AttributeProvider> attributeProviders = new ArrayList<>();

        /**
         * @return the configured {@link HtmlRenderer}
         */
        public HtmlRenderer build() {
            return new HtmlRenderer(this);
        }

        /**
         * The HTML to use for rendering a softbreak, defaults to {@code "\n"} (meaning the rendered result doesn't have
         * a line break).
         * <p>
         * Set it to {@code "<br>"} (or {@code "<br />"} to make them hard breaks.
         * <p>
         * Set it to {@code " "} to ignore line wrapping in the source.
         *
         * @param softbreak HTML for softbreak
         * @return {@code this}
         */
        public Builder softbreak(String softbreak) {
            this.softbreak = softbreak;
            return this;
        }

        /**
         * Whether {@link HtmlInline} and {@link HtmlBlock} should be escaped, defaults to {@code false}.
         * <p>
         * Note that {@link HtmlInline} is only a tag itself, not the text between an opening tag and a closing tag. So
         * markup in the text will be parsed as normal and is not affected by this option.
         *
         * @param escapeHtml true for escaping, false for preserving raw HTML
         * @return {@code this}
         */
        public Builder escapeHtml(boolean escapeHtml) {
            this.escapeHtml = escapeHtml;
            return this;
        }

        /**
         * Whether URLs of link or images should be percent-encoded, defaults to {@code false}.
         * <p>
         * If enabled, the following is done:
         * <ul>
         * <li>Existing percent-encoded parts are preserved (e.g. "%20" is kept as "%20")</li>
         * <li>Reserved characters such as "/" are preserved, except for "[" and "]" (see encodeURI in JS)</li>
         * <li>Unreserved characters such as "a" are preserved</li>
         * <li>Other characters such umlauts are percent-encoded</li>
         * </ul>
         *
         * @param percentEncodeUrls true to percent-encode, false for leaving as-is
         * @return {@code this}
         */
        public Builder percentEncodeUrls(boolean percentEncodeUrls) {
            this.percentEncodeUrls = percentEncodeUrls;
            return this;
        }

        /**
         * Add an attribute provider for adding/changing HTML attributes to the rendered tags.
         *
         * @param attributeProvider the attribute provider to add
         * @return {@code this}
         */
        public Builder attributeProvider(AttributeProvider attributeProvider) {
            this.attributeProviders.add(attributeProvider);
            return this;
        }

        public Builder customHtmlRenderer(CustomHtmlRenderer customHtmlRenderer) {
            this.customHtmlRenderers.add(customHtmlRenderer);
            return this;
        }

        /**
         * @param extensions extensions to use on this HTML renderer
         * @return {@code this}
         */
        public Builder extensions(Iterable<? extends Extension> extensions) {
            for (Extension extension : extensions) {
                if (extension instanceof HtmlRendererExtension) {
                    HtmlRendererExtension htmlRendererExtension = (HtmlRendererExtension) extension;
                    htmlRendererExtension.extend(this);
                }
            }
            return this;
        }
    }

    /**
     * Extension for {@link HtmlRenderer}.
     */
    public interface HtmlRendererExtension extends Extension {
        void extend(Builder rendererBuilder);
    }

    private class RendererVisitor extends AbstractVisitor {

        private final HtmlWriter html;
        private final List<CustomHtmlRenderer> customHtmlRenderers;

        public RendererVisitor(HtmlWriter html, List<CustomHtmlRenderer> customHtmlRenderers) {
            this.html = html;
            this.customHtmlRenderers = customHtmlRenderers;
        }

        @Override
        public void visit(Document document) {
            visitChildren(document);
        }

        @Override
        public void visit(Heading heading) {
            String htag = "h" + heading.getLevel();
            html.line();
            html.tag(htag, getAttrs(heading));
            visitChildren(heading);
            html.tag('/' + htag);
            html.line();
        }

        @Override
        public void visit(Paragraph paragraph) {
            boolean inTightList = isInTightList(paragraph);
            if (!inTightList) {
                html.line();
                html.tag("p", getAttrs(paragraph));
            }
            visitChildren(paragraph);
            if (!inTightList) {
                html.tag("/p");
                html.line();
            }
        }

        @Override
        public void visit(BlockQuote blockQuote) {
            html.line();
            html.tag("blockquote", getAttrs(blockQuote));
            html.line();
            visitChildren(blockQuote);
            html.line();
            html.tag("/blockquote");
            html.line();
        }

        @Override
        public void visit(BulletList bulletList) {
            renderListBlock(bulletList, "ul", getAttrs(bulletList));
        }

        @Override
        public void visit(FencedCodeBlock fencedCodeBlock) {
            String literal = fencedCodeBlock.getLiteral();
            Map<String, String> attributes = new LinkedHashMap<>();
            String info = fencedCodeBlock.getInfo();
            if (info != null && !info.isEmpty()) {
                int space = info.indexOf(" ");
                String language;
                if (space == -1) {
                    language = info;
                } else {
                    language = info.substring(0, space);
                }
                attributes.put("class", "language-" + language);
            }
            renderCodeBlock(literal, getAttrs(fencedCodeBlock, attributes));
        }

        @Override
        public void visit(HtmlBlock htmlBlock) {
            html.line();
            if (escapeHtml) {
                html.raw(escape(htmlBlock.getLiteral(), false));
            } else {
                html.raw(htmlBlock.getLiteral());
            }
            html.line();
        }

        @Override
        public void visit(ThematicBreak thematicBreak) {
            html.line();
            html.tag("hr", getAttrs(thematicBreak), true);
            html.line();
        }

        @Override
        public void visit(IndentedCodeBlock indentedCodeBlock) {
            renderCodeBlock(indentedCodeBlock.getLiteral(), getAttrs(indentedCodeBlock));
        }

        @Override
        public void visit(Link link) {
            Map<String, String> attrs = new LinkedHashMap<>();
            String url = optionallyPercentEncodeUrl(link.getDestination());
            attrs.put("href", url);
            if (link.getTitle() != null) {
                attrs.put("title", link.getTitle());
            }
            html.tag("a", getAttrs(link, attrs));
            visitChildren(link);
            html.tag("/a");
        }

        @Override
        public void visit(ListItem listItem) {
            html.tag("li", getAttrs(listItem));
            visitChildren(listItem);
            html.tag("/li");
            html.line();
        }

        @Override
        public void visit(OrderedList orderedList) {
            int start = orderedList.getStartNumber();
            Map<String, String> attrs = new LinkedHashMap<>();
            if (start != 1) {
                attrs.put("start", String.valueOf(start));
            }
            renderListBlock(orderedList, "ol", getAttrs(orderedList, attrs));
        }

        @Override
        public void visit(Image image) {
            String url = optionallyPercentEncodeUrl(image.getDestination());

            AltTextVisitor altTextVisitor = new AltTextVisitor();
            image.accept(altTextVisitor);
            String altText = altTextVisitor.getAltText();

            Map<String, String> attrs = new LinkedHashMap<>();
            attrs.put("src", url);
            attrs.put("alt", altText);
            if (image.getTitle() != null) {
                attrs.put("title", image.getTitle());
            }

            html.tag("img", getAttrs(image, attrs), true);
        }

        @Override
        public void visit(Emphasis emphasis) {
            html.tag("em");
            visitChildren(emphasis);
            html.tag("/em");
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            html.tag("strong");
            visitChildren(strongEmphasis);
            html.tag("/strong");
        }

        @Override
        public void visit(Text text) {
            html.raw(escape(text.getLiteral(), false));
        }

        @Override
        public void visit(Code code) {
            html.tag("code");
            html.raw(escape(code.getLiteral(), false));
            html.tag("/code");
        }

        @Override
        public void visit(HtmlInline htmlInline) {
            if (escapeHtml) {
                html.raw(escape(htmlInline.getLiteral(), false));
            } else {
                html.raw(htmlInline.getLiteral());
            }
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            html.raw(softbreak);
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            html.tag("br", NO_ATTRIBUTES, true);
            html.line();
        }

        @Override
        public void visit(CustomBlock customBlock) {
            renderCustom(customBlock);
        }

        @Override
        public void visit(CustomNode customNode) {
            renderCustom(customNode);
        }

        private void renderCustom(Node node) {
            for (CustomHtmlRenderer customHtmlRenderer : customHtmlRenderers) {
                // TODO: Should we pass attributes here?
                boolean handled = customHtmlRenderer.render(node, html, this);
                if (handled) {
                    break;
                }
            }
        }

        private void renderCodeBlock(String literal, Map<String, String> attributes) {
            html.line();
            html.tag("pre");
            html.tag("code", attributes);
            html.raw(escape(literal, false));
            html.tag("/code");
            html.tag("/pre");
            html.line();
        }

        private void renderListBlock(ListBlock listBlock, String tagName, Map<String, String> attributes) {
            html.line();
            html.tag(tagName, attributes);
            html.line();
            visitChildren(listBlock);
            html.line();
            html.tag('/' + tagName);
            html.line();
        }

        private boolean isInTightList(Paragraph paragraph) {
            Node parent = paragraph.getParent();
            if (parent != null) {
                Node gramps = parent.getParent();
                if (gramps != null && gramps instanceof ListBlock) {
                    ListBlock list = (ListBlock) gramps;
                    return list.isTight();
                }
            }
            return false;
        }

        private Map<String, String> getAttrs(Node node) {
            return getAttrs(node, Collections.<String, String>emptyMap());
        }

        private Map<String, String> getAttrs(Node node, Map<String, String> defaultAttributes) {
            Map<String, String> attrs = new LinkedHashMap<>(defaultAttributes);
            setCustomAttributes(node, attrs);
            return attrs;
        }

        private void setCustomAttributes(Node node, Map<String, String> attrs) {
            for (AttributeProvider attributeProvider : attributeProviders) {
                attributeProvider.setAttributes(node, attrs);
            }
        }
    }

    private static class AltTextVisitor extends AbstractVisitor {

        private final StringBuilder sb = new StringBuilder();

        String getAltText() {
            return sb.toString();
        }

        @Override
        public void visit(Text text) {
            sb.append(text.getLiteral());
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            sb.append('\n');
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            sb.append('\n');
        }
    }
}
