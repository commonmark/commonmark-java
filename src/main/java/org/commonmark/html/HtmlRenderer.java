package org.commonmark.html;

import org.commonmark.internal.util.Escaping;
import org.commonmark.node.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class HtmlRenderer {

    private static final Map<String, String> NO_ATTRIBUTES = Collections.emptyMap();

    private final String softbreak;
    private final boolean escapeHtml;
    private final boolean sourcepos;
    private final CodeBlockAttributeProvider codeBlockAttributeProvider;

    private HtmlRenderer(Builder builder) {
        this.softbreak = builder.softbreak;
        this.escapeHtml = builder.escapeHtml;
        this.sourcepos = builder.sourcepos;
        this.codeBlockAttributeProvider = builder.codeBlockAttributeProvider;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String render(Node nodeToRender) {
        HtmlWriter html = new HtmlWriter();

        RendererVisitor visitor = new RendererVisitor(html);
        nodeToRender.accept(visitor);

        return html.build();
    }

    private String escape(String input, boolean preserveEntities) {
        return Escaping.escapeHtml(input, preserveEntities);
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

        public Builder sourcepos(boolean sourcepos) {
            this.sourcepos = sourcepos;
            return this;
        }

        public Builder codeBlockAttributeProvider(CodeBlockAttributeProvider codeBlockAttributeProvider) {
            this.codeBlockAttributeProvider = codeBlockAttributeProvider;
            return this;
        }

        public HtmlRenderer build() {
            return new HtmlRenderer(this);
        }
    }

    private static class HtmlWriter {
        private static final Pattern reHtmlTag = Pattern.compile("<[^>]*>");

        private StringBuilder buffer = new StringBuilder();
        private int nesting = 0;

        void raw(String s) {
            if (isHtmlAllowed()) {
                buffer.append(s);
            } else {
                buffer.append(reHtmlTag.matcher(s).replaceAll(""));
            }
        }

        boolean isHtmlAllowed() {
            return nesting == 0;
        }

        void enter() {
            nesting++;
        }

        void leave() {
            nesting--;
        }

        void tag(String name) {
            tag(name, NO_ATTRIBUTES, false);
        }

        void tag(String name, Map<String, String> attrs) {
            tag(name, attrs, false);
        }

        void tag(String name, boolean selfClosing) {
            tag(name, NO_ATTRIBUTES, selfClosing);
        }

        // Helper function to produce an HTML tag.
        void tag(String name, Map<String, String> attrs, boolean selfclosing) {
            if (!isHtmlAllowed()) {
                return;
            }

            buffer.append('<');
            buffer.append(name);
            if (attrs != null && !attrs.isEmpty()) {
                for (Map.Entry<String, String> attrib : attrs.entrySet()) {
                    buffer.append(' ');
                    buffer.append(attrib.getKey());
                    buffer.append("=\"");
                    buffer.append(attrib.getValue());
                    buffer.append('"');
                }
            }
            if (selfclosing) {
                buffer.append(" /");
            }

            buffer.append('>');
        }

        void line() {
            if (buffer.length() > 0 && buffer.charAt(buffer.length() - 1) != '\n') {
                buffer.append('\n');
            }
        }

        String build() {
            return buffer.toString();
        }
    }

    private class RendererVisitor extends AbstractVisitor {

        private final HtmlWriter html;

        public RendererVisitor(HtmlWriter html) {
            this.html = html;
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
        public void visit(ListBlock listBlock) {
            String tagname = listBlock.getListType() == ListBlock.ListType.BULLET ? "ul"
                    : "ol";
            int start = listBlock.getOrderedStart();
            Map<String, String> attrs = getAttrs(listBlock);
            if (start > 1) {
                attrs.put("start", String.valueOf(start));
            }
            html.line();
            html.tag(tagname, attrs);
            html.line();
            visitChildren(listBlock);
            html.line();
            html.tag('/' + tagname);
            html.line();
        }

        @Override
        public void visit(ListItem listItem) {
            html.tag("li", getAttrs(listItem));
            visitChildren(listItem);
            html.tag("/li");
            html.line();
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
            attrs.put("href", escape(link.getDestination(), true));
            if (link.getTitle() != null) {
                attrs.put("title", escape(link.getTitle(), true));
            }
            html.tag("a", attrs);
            visitChildren(link);
            html.tag("/a");
        }

        @Override
        public void visit(Image image) {
            if (html.isHtmlAllowed()) {
                html.raw("<img src=\"" + escape(image.getDestination(), true) +
                        "\" alt=\"");
            }
            html.enter();
            visitChildren(image);
            html.leave();
            if (html.isHtmlAllowed()) {
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
        public void visit(HtmlTag inlineHmtl) {
            if (escapeHtml) {
                html.raw(escape(inlineHmtl.getLiteral(), false));
            } else {
                html.raw(inlineHmtl.getLiteral());
            }
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            html.raw(softbreak);
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            html.tag("br", true);
            html.line();
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
