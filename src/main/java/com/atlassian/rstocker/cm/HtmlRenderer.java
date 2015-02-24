package com.atlassian.rstocker.cm;

import com.atlassian.rstocker.cm.node.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class HtmlRenderer {

	private final String softbreak;
	private final Escaper escaper;
	private final boolean sourcepos;

	public static Builder builder() {
		return new Builder();
	}

	public String render(Node nodeToRender) {
		HtmlWriter html = new HtmlWriter();

		RendererVisitor visitor = new RendererVisitor(html);
		nodeToRender.accept(visitor);

		return html.build();
	}

	private String esc(String input, boolean preserveEntities) {
		return escaper.escape(input, preserveEntities);
	}

	private HtmlRenderer(Builder builder) {
		this.softbreak = builder.softbreak;
		this.escaper = builder.escaper;
		this.sourcepos = builder.sourcepos;
	}

	// default options:
	// softbreak: '\n', // by default, soft breaks are rendered as newlines in
	// HTML
	// set to "<br />" to make them hard breaks
	// set to " " if you want to ignore line wrapping in source
	public static class Builder {

		private String softbreak = "\n";
		private Escaper escaper = Common.XML_ESCAPER;
		private boolean sourcepos = false;

		public Builder softbreak(String softbreak) {
			this.softbreak = softbreak;
			return this;
		}

		public Builder escaper(Escaper escaper) {
			this.escaper = escaper;
			return this;
		}

		public Builder sourcepos(boolean sourcepos) {
			this.sourcepos = sourcepos;
			return this;
		}

		public HtmlRenderer build() {
			return new HtmlRenderer(this);
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
			List<String[]> attrs = getAttrs(listBlock);
			if (start > 1) {
				attrs.add(new String[]{"start", String.valueOf(start)});
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
		public void visit(CodeBlock codeBlock) {
			// TODO: Just use indexOf(' ')
			String[] info_words = codeBlock.getInfo() != null ? codeBlock.getInfo().split(" +")
					: new String[0];
			List<String[]> attrs = getAttrs(codeBlock);
			if (info_words.length > 0 && info_words[0].length() > 0) {
				attrs.add(new String[]{"class",
						"language-" + esc(info_words[0], true)});
			}
			html.line();
			html.tag("pre");
			html.tag("code", attrs);
			html.raw(esc(codeBlock.getLiteral(), false));
			html.tag("/code");
			html.tag("/pre");
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
		public void visit(HtmlBlock htmlBlock) {
			html.line();
			html.raw(htmlBlock.getLiteral());
			html.line();
		}

		@Override
		public void visit(HorizontalRule horizontalRule) {
			html.line();
			html.tag("hr", getAttrs(horizontalRule), true);
			html.line();
		}

		@Override
		public void visit(Link link) {
			List<String[]> attrs = getAttrs(link);
			attrs.add(new String[]{"href",
					esc(link.getDestination(), true)});
				if (link.getTitle() != null) {
					attrs.add(new String[]{"title", esc(link.getTitle(), true)});
				}
				html.tag("a", attrs);
			visitChildren(link);
				html.tag("/a");
		}

		@Override
		public void visit(Image image) {
				if (html.isHtmlAllowed()) {
					html.raw("<img src=\"" + esc(image.getDestination(), true) +
							"\" alt=\"");
				}
				html.enter();
			visitChildren(image);
				html.leave();
				if (html.isHtmlAllowed()) {
					if (image.getTitle() != null) {
						html.raw("\" title=\"" + esc(image.getTitle(), true));
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
			html.raw(esc(text.getLiteral(), false));
		}

		@Override
		public void visit(Code code) {
			html.tag("code");
			html.raw(esc(code.getLiteral(), false));
			html.tag("/code");
		}

		@Override
		public void visit(Html htmlNode) {
			html.raw(htmlNode.getLiteral());
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

		private List<String[]> getAttrs(Node node) {
			List<String[]> attrs = new ArrayList<>();
			if (sourcepos && node instanceof Block) {
				Block block = (Block) node;
				SourcePos pos = block.getSourcePos();
				if (pos != null) {
					attrs.add(new String[] { "data-sourcepos",
							"" + pos.getStartLine() + ':' +
									pos.getStartColumn() + '-' + pos.getEndLine() + ':' +
									pos.getEndColumn() });
				}
			}
			return attrs;
		}
	}

	private static class HtmlWriter {
		// foo: \<?
		private static final Pattern reHtmlTag = Pattern.compile("\\<[^>]*\\>");

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
			tag(name, Collections.emptyList(), false);
		}

		void tag(String name, List<String[]> attrs) {
			tag(name, attrs, false);
		}

		void tag(String name, boolean selfClosing) {
			tag(name, Collections.emptyList(), selfClosing);
		}

		// Helper function to produce an HTML tag.
		void tag(String name, List<String[]> attrs, boolean selfclosing) {
			if (!isHtmlAllowed()) {
				return;
			}

			buffer.append('<');
			buffer.append(name);
			if (attrs != null && !attrs.isEmpty()) {
				for (String[] attrib : attrs) {
					buffer.append(' ');
					buffer.append(attrib[0]);
					buffer.append("=\"");
					buffer.append(attrib[1]);
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
}
