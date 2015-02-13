package com.atlassian.rstocker.cm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.atlassian.rstocker.cm.Node.NodeWalker;
import com.atlassian.rstocker.cm.Node.NodeWalker.Entry;

public class HtmlRenderer {

	private final String softbreak;
	private final Escaper escaper;
	private final boolean sourcepos;

	public static Builder builder() {
		return new Builder();
	}

	public String render(Node block) {
		HtmlWriter html = new HtmlWriter();

		NodeWalker walker = block.walker();
		Entry entry;

		while ((entry = walker.next()) != null) {
			boolean entering = entry.entering;
			Node node = entry.node;

			// TODO: meh
			List<String[]> attrs = new ArrayList<>();
			if (sourcepos) {
				int[][] pos = node.sourcepos();
				if (pos != null) {
					attrs.add(new String[] { "data-sourcepos",
							"" + pos[0][0] + ':' +
									pos[0][1] + '-' + pos[1][0] + ':' +
									pos[1][1] });
				}
			}

			switch (node.type()) {
			case "Text":
				html.raw(esc(node.literal, false));
				break;

			case "Softbreak":
				html.raw(softbreak);
				break;

			case "Hardbreak":
				html.tag("br", true);
				html.line();
				break;

			case "Emph":
				html.tag(entering ? "em" : "/em");
				break;

			case "Strong":
				html.tag(entering ? "strong" : "/strong");
				break;

			case "Html":
				html.raw(node.literal);
				break;

			case "Link":
				if (entering) {
					attrs.add(new String[] { "href",
							esc(node.destination, true) });
					if (node.title != null) {
						attrs.add(new String[] { "title", esc(node.title, true) });
					}
					html.tag("a", attrs);
				} else {
					html.tag("/a");
				}
				break;

			case "Image":
				if (entering) {
					if (html.isHtmlAllowed()) {
						html.raw("<img src=\"" + esc(node.destination, true) +
								"\" alt=\"");
					}
					html.enter();
				} else {
					html.leave();
					if (html.isHtmlAllowed()) {
						if (node.title != null) {
							html.raw("\" title=\"" + esc(node.title, true));
						}
						html.raw("\" />");
					}
				}
				break;

			case "Code":
				html.tag("code");
				html.raw(esc(node.literal, false));
				html.tag("/code");
				break;

			case "Document":
				break;

			case "Paragraph":
				Node grandparent = node.parent.parent;
				if (grandparent != null &&
						grandparent.type() == "List") {
					if (grandparent.isListTight()) {
						break;
					}
				}
				if (entering) {
					html.line();
					html.tag("p", attrs);
				} else {
					html.tag("/p");
					html.line();
				}
				break;

			case "BlockQuote":
				if (entering) {
					html.line();
					html.tag("blockquote", attrs);
					html.line();
				} else {
					html.line();
					html.tag("/blockquote");
					html.line();
				}
				break;

			case "Item":
				if (entering) {
					html.tag("li", attrs);
				} else {
					html.tag("/li");
					html.line();
				}
				break;

			case "List":
				String tagname = node.getListType().equals("Bullet") ? "ul"
						: "ol";
				if (entering) {
					int start = node.getListStart();
					if (start > 1) {
						attrs.add(new String[] { "start", String.valueOf(start) });
					}
					html.line();
					html.tag(tagname, attrs);
					html.line();
				} else {
					html.line();
					html.tag('/' + tagname);
					html.line();
				}
				break;

			case "Header":
				String htag = "h" + node.level;
				if (entering) {
					html.line();
					html.tag(htag, attrs);
				} else {
					html.tag('/' + htag);
					html.line();
				}
				break;

			case "CodeBlock":
				// TODO: Just use indexOf(' ')
				String[] info_words = node.info != null ? node.info.split(" +")
						: new String[0];
				if (info_words.length > 0 && info_words[0].length() > 0) {
					attrs.add(new String[] { "class",
							"language-" + esc(info_words[0], true) });
				}
				html.line();
				html.tag("pre");
				html.tag("code", attrs);
				html.raw(esc(node.literal, false));
				html.tag("/code");
				html.tag("/pre");
				html.line();
				break;

			case "HtmlBlock":
				html.line();
				html.raw(node.literal);
				html.line();
				break;

			case "HorizontalRule":
				html.line();
				html.tag("hr", attrs, true);
				html.line();
				break;

			default:
				throw new IllegalStateException("Unknown node type "
						+ node.type());
			}

		}

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
		// TODO: XML escaper
		private Escaper escaper = (input, preserveEntities) -> input;
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

	@FunctionalInterface
	public interface Escaper {
		String escape(String input, boolean preserveEntities);
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
