package com.atlassian.rstocker.cm;

import static com.atlassian.rstocker.cm.Common.ESCAPABLE;
import static com.atlassian.rstocker.cm.Common.normalizeReference;
import static com.atlassian.rstocker.cm.Common.normalizeURI;
import static com.atlassian.rstocker.cm.Common.unescapeString;
import static com.atlassian.rstocker.cm.Html5Entities.entityToChar;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.rstocker.cm.Node.Type;

public class InlineParser {
	// Constants for character codes:

	private static final char C_NEWLINE = 10;
	private static final char C_ASTERISK = 42;
	private static final char C_UNDERSCORE = 95;
	private static final char C_BACKTICK = 96;
	private static final char C_OPEN_BRACKET = 91;
	private static final char C_CLOSE_BRACKET = 93;
	private static final char C_LESSTHAN = 60;
	private static final char C_BANG = 33;
	private static final char C_BACKSLASH = 92;
	private static final char C_AMPERSAND = 38;
	private static final char C_OPEN_PAREN = 40;
	private static final char C_COLON = 58;

	private static final String ESCAPED_CHAR = "\\\\" + ESCAPABLE;
	private static final String REG_CHAR = "[^\\\\()\\x00-\\x20]";
	private static final String IN_PARENS_NOSP = "\\((" + REG_CHAR + '|' + ESCAPED_CHAR + ")*\\)";
	private static final String TAGNAME = "[A-Za-z][A-Za-z0-9]*";
	private static final String ATTRIBUTENAME = "[a-zA-Z_:][a-zA-Z0-9:._-]*";
	private static final String UNQUOTEDVALUE = "[^\"'=<>`\\x00-\\x20]+";
	private static final String SINGLEQUOTEDVALUE = "'[^']*'";
	private static final String DOUBLEQUOTEDVALUE = "\"[^\"]*\"";
	private static final String ATTRIBUTEVALUE = "(?:" + UNQUOTEDVALUE + "|" + SINGLEQUOTEDVALUE
			+ "|" + DOUBLEQUOTEDVALUE + ")";
	private static final String ATTRIBUTEVALUESPEC = "(?:" + "\\s*=" + "\\s*" + ATTRIBUTEVALUE
			+ ")";
	private static final String ATTRIBUTE = "(?:" + "\\s+" + ATTRIBUTENAME + ATTRIBUTEVALUESPEC
			+ "?)";
	private static final String OPENTAG = "<" + TAGNAME + ATTRIBUTE + "*" + "\\s*/?>";
	private static final String CLOSETAG = "</" + TAGNAME + "\\s*[>]";
	private static final String HTMLCOMMENT = "<!---->|<!--(?:-?[^>-])(?:-?[^-])*-->";
	private static final String PROCESSINGINSTRUCTION = "[<][?].*?[?][>]";
	private static final String DECLARATION = "<![A-Z]+" + "\\s+[^>]*>";
	private static final String CDATA = "<!\\[CDATA\\[[\\s\\S]*?\\]\\]>";
	private static final String HTMLTAG = "(?:" + OPENTAG + "|" + CLOSETAG + "|" + HTMLCOMMENT
			+ "|" +
			PROCESSINGINSTRUCTION + "|" + DECLARATION + "|" + CDATA + ")";
	private static final String ENTITY = "&(?:#x[a-f0-9]{1,8}|#[0-9]{1,8}|[a-z][a-z0-9]{1,31});";

	private static final Pattern rePunctuation = Pattern
			.compile("^[\u2000-\u206F\u2E00-\u2E7F\\'!\"#\\$%&\\(\\)\\*\\+,\\-\\./:;<=>\\?@\\[\\]\\^_`\\{\\|\\}~]");

	private static final Pattern reHtmlTag = Pattern.compile('^' + HTMLTAG, Pattern.CASE_INSENSITIVE);

	private static final Pattern reLinkTitle = Pattern.compile(
			"^(?:\"(" + ESCAPED_CHAR + "|[^\"\\x00])*\"" +
					'|' +
					"'(" + ESCAPED_CHAR + "|[^'\\x00])*'" +
					'|' +
					"\\((" + ESCAPED_CHAR + "|[^)\\x00])*\\))");

	private static final Pattern reLinkDestinationBraces = Pattern.compile(
			"^(?:[<](?:[^<>\\n\\\\\\x00]" + '|' + ESCAPED_CHAR + '|' + "\\\\)*[>])");

	private static final Pattern reLinkDestination = Pattern.compile(
			"^(?:" + REG_CHAR + "+|" + ESCAPED_CHAR + '|' + IN_PARENS_NOSP + ")*");

	private static final Pattern reEscapable = Pattern.compile(ESCAPABLE);

	private static final Pattern reEntityHere = Pattern.compile('^' + ENTITY, Pattern.CASE_INSENSITIVE);

	private static final Pattern reTicks = Pattern.compile("`+");

	private static final Pattern reTicksHere = Pattern.compile("^`+");

	private static final Pattern reEmailAutolink = Pattern
			.compile("^<([a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*)>");

	// foo: /i
	private static final Pattern reAutolink = Pattern
			.compile("^<(?:coap|doi|javascript|aaa|aaas|about|acap|cap|cid|crid|data|dav|dict|dns|file|ftp|geo|go|gopher|h323|http|https|iax|icap|im|imap|info|ipp|iris|iris.beep|iris.xpc|iris.xpcs|iris.lwz|ldap|mailto|mid|msrp|msrps|mtqp|mupdate|news|nfs|ni|nih|nntp|opaquelocktoken|pop|pres|rtsp|service|session|shttp|sieve|sip|sips|sms|snmp|soap.beep|soap.beeps|tag|tel|telnet|tftp|thismessage|tn3270|tip|tv|urn|vemmi|ws|wss|xcon|xcon-userid|xmlrpc.beep|xmlrpc.beeps|xmpp|z39.50r|z39.50s|adiumxtra|afp|afs|aim|apt|attachment|aw|beshare|bitcoin|bolo|callto|chrome|chrome-extension|com-eventbrite-attendee|content|cvs|dlna-playsingle|dlna-playcontainer|dtn|dvb|ed2k|facetime|feed|finger|fish|gg|git|gizmoproject|gtalk|hcp|icon|ipn|irc|irc6|ircs|itms|jar|jms|keyparc|lastfm|ldaps|magnet|maps|market|message|mms|ms-help|msnim|mumble|mvn|notes|oid|palm|paparazzi|platform|proxy|psyc|query|res|resource|rmi|rsync|rtmp|secondlife|sftp|sgn|skype|smb|soldat|spotify|ssh|steam|svn|teamspeak|things|udp|unreal|ut2004|ventrilo|view-source|webcal|wtai|wyciwyg|xfire|xri|ymsgr):[^<>\u0000-\u0020]*>");

	private static final Pattern reSpnl = Pattern.compile("^ *(?:\n *)?");

	private static final Pattern reWhitespaceChar = Pattern.compile("^\\s");

	// foo: /g
	private static final Pattern reWhitespace = Pattern.compile("\\s+");
	private static final Pattern reSingleWhitespace = Pattern.compile("\\s");

	private static final Pattern reFinalSpace = Pattern.compile(" *$");

	private static final Pattern reLineEnd = Pattern.compile("^ *(?:\n|$)");

	private static final Pattern reInitialSpace = Pattern.compile("^ *");

	private static final Pattern reLinkLabel = Pattern
			.compile("^\\[(?:[^\\\\\\[\\]]|\\\\[\\[\\]]){0,1000}\\]");

	// Matches a string of non-special characters.
	// foo: /m = multiline?
	private static final Pattern reMain = Pattern.compile("^[^\n`\\[\\]\\\\!<&*_]+");

	private String subject = "";
	// TODO: rename?
	private Delimiter delimiters;
	private int pos = 0;
	private Map<String, Node> refmap;

	// match: match,
	// peek: peek,
	// spnl: spnl,
	// parseBackticks: parseBackticks,
	// parseBackslash: parseBackslash,
	// parseAutolink: parseAutolink,
	// parseHtmlTag: parseHtmlTag,
	// scanDelims: scanDelims,
	// parseEmphasis: parseEmphasis,
	// parseLinkTitle: parseLinkTitle,
	// parseLinkDestination: parseLinkDestination,
	// parseLinkLabel: parseLinkLabel,
	// parseOpenBracket: parseOpenBracket,
	// parseCloseBracket: parseCloseBracket,
	// parseBang: parseBang,
	// parseEntity: parseEntity,
	// parseString: parseString,
	// parseNewline: parseNewline,
	// parseReference: parseReference,
	// parseInline: parseInline,
	// processEmphasis: processEmphasis,
	// removeDelimiter: removeDelimiter,
	// parse: parseInlines

	// All of the parsers below try to match something at the current position
	// in the subject. If they succeed in matching anything, they
	// return the inline matched, advancing the subject.

	private static Node text(String s) {
		Node node = new Node(Type.Text);
		node.literal = s;
		return node;
	}

	// If re matches at current position in the subject, advance
	// position in subject and return the match; otherwise return null.
	String match(Pattern re) {
		if (this.pos >= this.subject.length()) {
			return null;
		}
		Matcher matcher = re.matcher(this.subject.substring(this.pos));
		boolean m = matcher.find();
		if (m) {
			// foo: is this correct?
			this.pos += matcher.end();
			return matcher.group();
		} else {
			return null;
		}
	};

	// Returns the code for the character at the current subject position, or -1
	// there are no more characters.
	char peek() {
		if (this.pos < this.subject.length()) {
			return this.subject.charAt(this.pos);
		} else {
			// FIXME: not sure if this is bad or not
			return '\0';
		}
	}

	// Parse zero or more space characters, including at most one newline
	boolean spnl() {
		this.match(reSpnl);
		return true;
	}

	// Attempt to parse backticks, adding either a backtick code span or a
	// literal sequence of backticks.
	private boolean parseBackticks(Node block) {
		String ticks = this.match(reTicksHere);
		if (ticks == null) {
			return false;
		}
		int afterOpenTicks = this.pos;
		boolean foundCode = false;
		String matched;
		Node node;
		while (!foundCode && (matched = this.match(reTicks)) != null) {
			if (matched.equals(ticks)) {
				node = new Node(Type.Code);
				String content = this.subject.substring(afterOpenTicks, this.pos - ticks.length());
				node.literal = reWhitespace.matcher(content.trim()).replaceAll(" ");
				block.appendChild(node);
				return true;
			}
		}
		// If we got here, we didn't match a closing backtick sequence.
		this.pos = afterOpenTicks;
		block.appendChild(text(ticks));
		return true;
	}

	// Parse a backslash-escaped special character, adding either the escaped
	// character, a hard line break (if the backslash is followed by a newline),
	// or a literal backslash to the block's children.
	boolean parseBackslash(Node block) {
		String subj = this.subject;
		int pos = this.pos;
		Node node;
		if (subj.charAt(pos) == C_BACKSLASH) {
			int next = pos + 1;
			if (next < subj.length() && subj.charAt(next) == '\n') {
				this.pos = this.pos + 2;
				node = new Node(Type.Hardbreak);
				block.appendChild(node);
			} else if (next < subj.length() && reEscapable.matcher(subj.substring(next, next + 1)).matches()) {
				this.pos = this.pos + 2;
				block.appendChild(text(subj.substring(next, next + 1)));
			} else {
				this.pos++;
				block.appendChild(text("\\"));
			}
			return true;
		} else {
			return false;
		}
	}

	// Attempt to parse an autolink (URL or email in pointy brackets).
	boolean parseAutolink(Node block) {
		String m;
		String dest;
		if ((m = this.match(reEmailAutolink)) != null) {
			dest = m.substring(1, m.length() - 1);
			Node node = new Node(Type.Link);
			node.destination = normalizeURI("mailto:" + dest);
			node.title = "";
			node.appendChild(text(dest));
			block.appendChild(node);
			return true;
		} else if ((m = this.match(reAutolink)) != null) {
			dest = m.substring(1, m.length() - 1);
			Node node = new Node(Type.Link);
			node.destination = normalizeURI(dest);
			node.title = "";
			node.appendChild(text(dest));
			block.appendChild(node);
			return true;
		} else {
			return false;
		}
	};

	// Attempt to parse a raw HTML tag.
	boolean parseHtmlTag(Node block) {
		String m = this.match(reHtmlTag);
		if (m != null) {
			Node node = new Node(Type.Html);
			node.literal = m;
			block.appendChild(node);
			return true;
		} else {
			return false;
		}
	}

	// Scan a sequence of characters with code cc, and return information about
	// the number of delimiters and whether they are positioned such that
	// they can open and/or close emphasis or strong emphasis. A utility
	// function for strong/emph parsing.
	ScanDelimsResult scanDelims(char cc) {
		int numdelims = 0;
		String char_before;
		String char_after;
		char cc_after;
		int startpos = this.pos;
		boolean left_flanking, right_flanking, can_open, can_close;

		char_before = this.pos == 0 ? "\n" :
				this.subject.substring(this.pos - 1, this.pos);

		while (this.peek() == cc) {
			numdelims++;
			this.pos++;
		}

		cc_after = this.peek();
		if (cc_after == '\0') {
			char_after = "\n";
		} else {
			// foo: fromCodePoint?
			char_after = "" + cc_after;
		}

		left_flanking = numdelims > 0 &&
				!(reWhitespaceChar.matcher(char_after).matches()) &&
				!(rePunctuation.matcher(char_after).matches() &&
						!(reSingleWhitespace.matcher(char_before).matches()) &&
				!(rePunctuation.matcher(char_before).matches()));
		right_flanking = numdelims > 0 &&
				!(reWhitespaceChar.matcher(char_before).matches()) &&
				!(rePunctuation.matcher(char_before).matches() &&
						!(reWhitespaceChar.matcher(char_after).matches()) &&
				!(rePunctuation.matcher(char_after).matches()));
		if (cc == C_UNDERSCORE) {
			can_open = left_flanking && !right_flanking;
			can_close = right_flanking && !left_flanking;
		} else {
			can_open = left_flanking;
			can_close = right_flanking;
		}
		this.pos = startpos;
		return new ScanDelimsResult(numdelims, can_open, can_close);
	};

	private static class ScanDelimsResult {
		private final int numdelims;
		private final boolean can_open;
		private final boolean can_close;

		ScanDelimsResult(int numdelims, boolean can_open, boolean can_close) {
			this.numdelims = numdelims;
			this.can_open = can_open;
			this.can_close = can_close;
		}
	}

	// Attempt to parse emphasis or strong emphasis.
	boolean parseEmphasis(char cc, Node block) {
		ScanDelimsResult res = this.scanDelims(cc);
		int numdelims = res.numdelims;
		int startpos = this.pos;

		if (numdelims == 0) {
			return false;
		}

		this.pos += numdelims;
		Node node = text(this.subject.substring(startpos, this.pos));
		block.appendChild(node);

		// Add entry to stack for this opener
		this.delimiters = new Delimiter(node, this.delimiters, startpos);
		this.delimiters.cc = cc;
		this.delimiters.numdelims = numdelims;
		this.delimiters.can_open = res.can_open;
		this.delimiters.can_close = res.can_close;
		this.delimiters.active = true;
		if (this.delimiters.previous != null) {
			this.delimiters.previous.next = this.delimiters;
		}

		return true;
	}

	void removeDelimiter(Delimiter delim) {
		if (delim.previous != null) {
			delim.previous.next = delim.next;
		}
		if (delim.next == null) {
			// top of stack
			this.delimiters = delim.previous;
		} else {
			delim.next.previous = delim.previous;
		}
	}

	void processEmphasis(Node block, Delimiter stack_bottom) {
		Delimiter opener, closer;
		Node opener_inl, closer_inl;
		Delimiter nextstack, tempstack;
		int use_delims;
		Node tmp, next;

		// find first closer above stack_bottom:
		closer = this.delimiters;
		while (closer != null && closer.previous != stack_bottom) {
			closer = closer.previous;
		}
		// move forward, looking for closers, and handling each
		while (closer != null) {
			if (closer.can_close && (closer.cc == C_UNDERSCORE || closer.cc == C_ASTERISK)) {
				// found emphasis closer. now look back for first matching opener:
				opener = closer.previous;
				while (opener != null && opener != stack_bottom) {
					if (opener.cc == closer.cc && opener.can_open) {
						break;
					}
					opener = opener.previous;
				}
				if (opener != null && opener != stack_bottom) {
					// calculate actual number of delimiters used from this closer
					if (closer.numdelims < 3 || opener.numdelims < 3) {
						use_delims = closer.numdelims <= opener.numdelims ?
								closer.numdelims : opener.numdelims;
					} else {
						use_delims = closer.numdelims % 2 == 0 ? 2 : 1;
					}

					opener_inl = opener.node;
					closer_inl = closer.node;

					// remove used delimiters from stack elts and inlines
					opener.numdelims -= use_delims;
					closer.numdelims -= use_delims;
					opener_inl.literal =
							opener_inl.literal.substring(0,
									opener_inl.literal.length() - use_delims);
					closer_inl.literal =
							closer_inl.literal.substring(0,
									closer_inl.literal.length() - use_delims);

					// build contents for new emph element
					Node emph = new Node(use_delims == 1 ? Type.Emph : Type.Strong);

					tmp = opener_inl.next;
					while (tmp != null && tmp != closer_inl) {
						next = tmp.next;
						tmp.unlink();
						emph.appendChild(tmp);
						tmp = next;
					}

					opener_inl.insertAfter(emph);

					// remove elts btw opener and closer in delimiters stack
					tempstack = closer.previous;
					while (tempstack != null && tempstack != opener) {
						nextstack = tempstack.previous;
						this.removeDelimiter(tempstack);
						tempstack = nextstack;
					}

					// if opener has 0 delims, remove it and the inline
					if (opener.numdelims == 0) {
						opener_inl.unlink();
						this.removeDelimiter(opener);
					}

					if (closer.numdelims == 0) {
						closer_inl.unlink();
						tempstack = closer.next;
						this.removeDelimiter(closer);
						closer = tempstack;
					}

				} else {
					closer = closer.next;
				}

			} else {
				closer = closer.next;
			}

		}

		// remove all delimiters
		while (this.delimiters != stack_bottom) {
			this.removeDelimiter(this.delimiters);
		}
	}

	// Attempt to parse link title (sans quotes), returning the string
	// or null if no match.
	String parseLinkTitle() {
		String title = this.match(reLinkTitle);
		if (title != null) {
			// chop off quotes from title and unescape:
			return unescapeString(title.substring(1, title.length() - 1));
		} else {
			return null;
		}
	}

	// Attempt to parse link destination, returning the string or
	// null if no match.
	String parseLinkDestination() {
		String res = this.match(reLinkDestinationBraces);
		if (res != null) { // chop off surrounding <..>:
			if (res.length() == 2) {
				return "";
			} else {
				return normalizeURI(unescapeString(res.substring(1, res.length() - 1)));
			}
		} else {
			res = this.match(reLinkDestination);
			if (res != null) {
				return normalizeURI(unescapeString(res));
			} else {
				return null;
			}
		}
	}

	// Attempt to parse a link label, returning number of characters parsed.
	int parseLinkLabel() {
		String m = this.match(reLinkLabel);
		return m == null ? 0 : m.length();
	};

	// Add open bracket to delimiter stack and add a text node to block's children.
	boolean parseOpenBracket(Node block) {
		int startpos = this.pos;
		this.pos += 1;

		Node node = text("[");
		block.appendChild(node);

		// Add entry to stack for this opener
		this.delimiters = new Delimiter(node, this.delimiters, startpos);
		this.delimiters.cc = C_OPEN_BRACKET;
		this.delimiters.numdelims = 1;
		this.delimiters.can_open = true;
		this.delimiters.can_close = false;
		this.delimiters.active = true;
		if (this.delimiters.previous != null) {
			this.delimiters.previous.next = this.delimiters;
		}

		return true;

	}

	// IF next character is [, and ! delimiter to delimiter stack and
	// add a text node to block's children. Otherwise just add a text node.
	boolean parseBang(Node block) {
		int startpos = this.pos;
		this.pos += 1;
		if (this.peek() == C_OPEN_BRACKET) {
			this.pos += 1;

			Node node = text("![");
			block.appendChild(node);

			// Add entry to stack for this opener
			this.delimiters = new Delimiter(node, this.delimiters, startpos + 1);
			this.delimiters.cc = C_BANG;
			this.delimiters.numdelims = 1;
			this.delimiters.can_open = true;
			this.delimiters.can_close = false;
			this.delimiters.active = true;
			if (this.delimiters.previous != null) {
				this.delimiters.previous.next = this.delimiters;
			}
		} else {
			block.appendChild(text("!"));
		}
		return true;
	}

	// Try to match close bracket against an opening in the delimiter
	// stack. Add either a link or image, or a plain [ character,
	// to block's children. If there is a matching delimiter,
	// remove it from the delimiter stack.
	boolean parseCloseBracket(Node block) {
		int startpos;
		boolean is_image;
		String dest = null;
		String title = null;
		boolean matched = false;
		String reflabel;
		Delimiter opener;

		this.pos += 1;
		startpos = this.pos;

		// look through stack of delimiters for a [ or ![
		opener = this.delimiters;

		while (opener != null) {
			if (opener.cc == C_OPEN_BRACKET || opener.cc == C_BANG) {
				break;
			}
			opener = opener.previous;
		}

		if (opener == null) {
			// no matched opener, just return a literal
			block.appendChild(text("]"));
			return true;
		}

		if (!opener.active) {
			// no matched opener, just return a literal
			block.appendChild(text("]"));
			// take opener off emphasis stack
			this.removeDelimiter(opener);
			return true;
		}

		// If we got here, open is a potential opener
		is_image = opener.cc == C_BANG;

		// Check to see if we have a link/image

		// Inline link?
		if (this.peek() == C_OPEN_PAREN) {
			this.pos++;
			if (this.spnl() &&
					((dest = this.parseLinkDestination()) != null)
					&&
					this.spnl()
					&&
					// make sure there's a space before the title:
					(reWhitespaceChar.matcher(this.subject.substring(this.pos - 1, this.pos))
							.matches()) &&
					(title = this.parseLinkTitle()) != null &&
					this.spnl() &&
					this.subject.charAt(this.pos) == ')') {
				this.pos += 1;
				matched = true;
			}
		} else {

			// Next, see if there's a link label
			int savepos = this.pos;
			this.spnl();
			int beforelabel = this.pos;
			int n = this.parseLinkLabel();
			if (n == 0 || n == 2) {
				// empty or missing second label
				reflabel = this.subject.substring(opener.index, startpos);
			} else {
				reflabel = this.subject.substring(beforelabel, beforelabel + n);
			}
			if (n == 0) {
				// If shortcut reference link, rewind before spaces we skipped.
				this.pos = savepos;
			}

			// lookup rawlabel in refmap
			// foo: normalizeReference?
			Node link = this.refmap.get(normalizeReference(reflabel));
			if (link != null) {
				dest = link.destination;
				title = link.title;
				matched = true;
			}
		}

		if (matched) {
			Node node = new Node(is_image ? Type.Image : Type.Link);
			node.destination = dest;
			node.title = title != null ? title : "";

			Node tmp, next;
			tmp = opener.node.next;
			while (tmp != null) {
				next = tmp.next;
				tmp.unlink();
				node.appendChild(tmp);
				tmp = next;
			}
			block.appendChild(node);
			this.processEmphasis(node, opener.previous);

			opener.node.unlink();

			// processEmphasis will remove this and later delimiters.
			// Now, for a link, we also deactivate earlier link openers.
			// (no links in links)
			if (!is_image) {
				opener = this.delimiters;
				while (opener != null) {
					if (opener.cc == C_OPEN_BRACKET) {
						opener.active = false; // deactivate this opener
					}
					opener = opener.previous;
				}
			}

			return true;

		} else { // no match

			this.removeDelimiter(opener); // remove this opener from stack
			this.pos = startpos;
			block.appendChild(text("]"));
			return true;
		}

	};

	// Attempt to parse an entity, return Entity object if successful.
	boolean parseEntity(Node block) {
		String m;
		if ((m = this.match(reEntityHere)) != null) {
			block.appendChild(text(entityToChar(m)));
			return true;
		} else {
			return false;
		}
	}

	// Parse a run of ordinary characters, or a single character with
	// a special meaning in markdown, as a plain string.
	boolean parseString(Node block) {
		String m;
		if ((m = this.match(reMain)) != null) {
			block.appendChild(text(m));
			return true;
		} else {
			return false;
		}
	}

	// Parse a newline. If it was preceded by two spaces, return a hard
	// line break; otherwise a soft line break.
	boolean parseNewline(Node block) {
		this.pos += 1; // assume we're at a \n
		// check previous node for trailing spaces
		Node lastc = block.lastChild;
		if (lastc != null && lastc.type() == Type.Text) {
			Matcher matcher = reFinalSpace.matcher(lastc.literal);
			int sps = matcher.find() ? matcher.end() - matcher.start() : 0;
			if (sps > 0) {
				lastc.literal = matcher.replaceAll("");
			}
			block.appendChild(new Node(sps >= 2 ? Type.Hardbreak : Type.Softbreak));
		} else {
			block.appendChild(new Node(Type.Softbreak));
		}
		this.match(reInitialSpace); // gobble leading spaces in next line
		return true;
	};

	// Attempt to parse a link reference, modifying refmap.
	int parseReference(String s, Map<String, Node> refmap) {
		this.subject = s;
		this.pos = 0;
		String rawlabel;
		String dest;
		String title;
		int matchChars;
		int startpos = this.pos;

		// label:
		matchChars = this.parseLinkLabel();
		if (matchChars == 0) {
			return 0;
		} else {
			rawlabel = this.subject.substring(0, matchChars);
		}

		// colon:
		if (this.peek() == C_COLON) {
			this.pos++;
		} else {
			this.pos = startpos;
			return 0;
		}

		// link url
		this.spnl();

		dest = this.parseLinkDestination();
		if (dest == null || dest.length() == 0) {
			this.pos = startpos;
			return 0;
		}

		int beforetitle = this.pos;
		this.spnl();
		title = this.parseLinkTitle();
		if (title == null) {
			title = "";
			// rewind before spaces
			this.pos = beforetitle;
		}

		// make sure we're at line end:
		if (this.pos != this.subject.length() && this.match(reLineEnd) == null) {
			this.pos = startpos;
			return 0;
		}

		String normlabel = normalizeReference(rawlabel);

		if (!refmap.containsKey(normlabel)) {
			Node link = new Node(Type.Link);
			link.destination = dest;
			link.title = title;
			refmap.put(normlabel, link);
		}
		return this.pos - startpos;
	};

	// Parse string_content in block into inline children,
	// using refmap to resolve references.
	public void parse(Node block, Map<String, Node> refmap) {
		this.subject = block.string_content.trim();
		this.pos = 0;
		this.refmap = refmap; // foo: || {};
		this.delimiters = null;
		while (this.parseInline(block)) {
		}
		this.processEmphasis(block, null);
	}

	// Parse the next inline element in subject, advancing subject position.
	// On success, add the result to block's children and return true.
	// On failure, return false.
	private boolean parseInline(Node block) {
		boolean res;
		char c = this.peek();
		if (c == '\0') {
			return false;
		}
		switch (c) {
		case C_NEWLINE:
			res = this.parseNewline(block);
			break;
		case C_BACKSLASH:
			res = this.parseBackslash(block);
			break;
		case C_BACKTICK:
			res = this.parseBackticks(block);
			break;
		case C_ASTERISK:
		case C_UNDERSCORE:
			res = this.parseEmphasis(c, block);
			break;
		case C_OPEN_BRACKET:
			res = this.parseOpenBracket(block);
			break;
		case C_BANG:
			res = this.parseBang(block);
			break;
		case C_CLOSE_BRACKET:
			res = this.parseCloseBracket(block);
			break;
		case C_LESSTHAN:
			res = this.parseAutolink(block) || this.parseHtmlTag(block);
			break;
		case C_AMPERSAND:
			res = this.parseEntity(block);
			break;
		default:
			res = this.parseString(block);
			break;
		}
		if (!res) {
			this.pos += 1;
			Node textnode = new Node(Type.Text);
			// foo: fromCodePoint?
			textnode.literal = String.valueOf(c);
			block.appendChild(textnode);
		}

		return true;
	};

	private static class Delimiter {

		final Node node;
		Delimiter previous;
		final int index;

		char cc = C_BANG;
		int numdelims = 1;
		Delimiter next;
		// foo2: camelCase these?
		boolean can_open = true;
		boolean can_close = false;
		boolean active = true;

		public Delimiter(Node node, Delimiter previous, int index) {
			this.node = node;
			this.previous = previous;
			this.index = index;
		}
	}
}
