package org.commonmark.html;

import org.commonmark.Extension;
import org.commonmark.internal.util.Escaping;
import org.commonmark.node.*;

import java.util.*;

public class HtmlRenderer {

    private static final Map<String, String> NO_ATTRIBUTES = Collections.emptyMap();

    private final String softbreak;
    private final boolean escapeHtml;
    private final boolean sourcepos;
    private final boolean percentEncodeUrls;
    private final CodeBlockAttributeProvider codeBlockAttributeProvider;
    private final List<CustomHtmlRenderer> customHtmlRenderers;

    private HtmlRenderer(Builder builder) {
        this.softbreak = builder.softbreak;
        this.escapeHtml = builder.escapeHtml;
        this.sourcepos = builder.sourcepos;
        this.percentEncodeUrls = builder.percentEncodeUrls;
        this.codeBlockAttributeProvider = builder.codeBlockAttributeProvider;
        this.customHtmlRenderers = builder.customHtmlRenderers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void render(Node node, Appendable output) {
        RendererVisitor rendererVisitor = new RendererVisitor(new HtmlWriter(output), customHtmlRenderers);
        node.accept(rendererVisitor);
    }

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

    // default options:
    // softbreak: '\n', // by default, soft breaks are rendered as newlines in
    // HTML
    // set to "<br />" to make them hard breaks
    // set to " " if you want to ignore line wrapping in source
    public static class Builder {

        private String softbreak = "\n";
        private boolean sourcepos = false;
        private boolean escapeHtml = false;
        private CodeBlockAttributeProvider codeBlockAttributeProvider = new CodeBlockAttributeProvider();
        private List<CustomHtmlRenderer> customHtmlRenderers = new ArrayList<>();
        private boolean percentEncodeUrls = false;

        public Builder softbreak(String softbreak) {
            this.softbreak = softbreak;
            return this;
        }

        /**
         * Whether {@link HtmlTag} and {@link HtmlBlock} should be escaped.
         * <p/>
         * Note that {@link HtmlTag} is only a tag itself, not the text between an opening tag and a closing tag. So markup
         * in the text will be parsed as normal and is not affected by this option.
         *
         * @param escapeHtml true for escaping, false for preserving raw HTML
         * @return {@code this}
         */
        public Builder escapeHtml(boolean escapeHtml) {
            this.escapeHtml = escapeHtml;
            return this;
        }

        /**
         * Whether URLs of link or images should be percent-encoded. If enabled, the following is done:
         * <ul>
         *     <li>Existing percent-encoded parts are preserved (e.g. "%20" is kept as "%20")</li>
         *     <li>Reserved characters such as "/" are preserved, except for "[" and "]" (see encodeURI in JS)</li>
         *     <li>Unreserved characters such as "a" are preserved</li>
         *     <li>Other characters such umlauts are percent-encoded</li>
         * </ul>
         * @param percentEncodeUrls true to percent-encode, false for leaving as-is; default is false
         * @return {@code this}
         */
        public Builder percentEncodeUrls(boolean percentEncodeUrls) {
            this.percentEncodeUrls = percentEncodeUrls;
            return this;
        }

        public Builder sourcepos(boolean sourcepos) {
            this.sourcepos = sourcepos;
            return this;
        }

        public Builder codeBlockAttributeProvider(CodeBlockAttributeProvider codeBlockAttributeProvider) {
            this.codeBlockAttributeProvider = codeBlockAttributeProvider;
            return this;
        }

        /**
         * @param extensions extensions to use on this HTML renderer
         * @return this
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

        public Builder customHtmlRenderer(CustomHtmlRenderer customHtmlRenderer) {
            this.customHtmlRenderers.add(customHtmlRenderer);
            return this;
        }

        public HtmlRenderer build() {
            return new HtmlRenderer(this);
        }
    }

    /**
     * Extension for HTML renderer.
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
        public void visit(Header header) {
            String htag = "h" + header.getLevel();
            html.line();
            html.tag(htag, getAttrs(header));
            visitChildren(header);
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
            Map<String, String> providerAttributes = codeBlockAttributeProvider.getAttributes(fencedCodeBlock);
            Map<String, String> attrs = new LinkedHashMap<>();
            for (Map.Entry<String, String> attribute : providerAttributes.entrySet()) {
                String escaped = escape(attribute.getValue(), true);
                attrs.put(attribute.getKey(), escaped);
            }
            String literal = fencedCodeBlock.getLiteral();
            renderCodeBlock(literal, attrs);
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
        public void visit(HorizontalRule horizontalRule) {
            html.line();
            html.tag("hr", getAttrs(horizontalRule), true);
            html.line();
        }

        @Override
        public void visit(IndentedCodeBlock indentedCodeBlock) {
            renderCodeBlock(indentedCodeBlock.getLiteral(), NO_ATTRIBUTES);
        }

        @Override
        public void visit(Link link) {
            Map<String, String> attrs = getAttrs(link);
            String url = optionallyPercentEncodeUrl(link.getDestination());
            attrs.put("href", escape(url, true));
            if (link.getTitle() != null) {
                attrs.put("title", escape(link.getTitle(), true));
            }
            html.tag("a", attrs);
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
            Map<String, String> attrs = getAttrs(orderedList);
            if (start > 1) {
                attrs.put("start", String.valueOf(start));
            }
            renderListBlock(orderedList, "ol", attrs);
        }

        @Override
        public void visit(Image image) {
            if (html.isTagAllowed()) {
                String url = optionallyPercentEncodeUrl(image.getDestination());
                html.raw("<img src=\"" + escape(url, true) +
                        "\" alt=\"");
            }
            html.disableTags();
            visitChildren(image);
            html.enableTags();
            if (html.isTagAllowed()) {
                if (image.getTitle() != null) {
                    html.raw("\" title=\"" + escape(image.getTitle(), true));
                }
                html.raw("\" />");
            }
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
        public void visit(HtmlTag htmlTag) {
            if (escapeHtml) {
                html.raw(escape(htmlTag.getLiteral(), false));
            } else {
                html.raw(htmlTag.getLiteral());
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
            Map<String, String> attrs = new LinkedHashMap<>();
            if (sourcepos && node instanceof Block) {
                Block block = (Block) node;
                SourcePosition pos = block.getSourcePosition();
                if (pos != null) {
                    attrs.put("data-sourcepos",
                            "" + pos.getStartLine() + ':' +
                                    pos.getStartColumn() + '-' + pos.getEndLine() + ':' +
                                    pos.getEndColumn());
                }
            }
            return attrs;
        }
    }
}
