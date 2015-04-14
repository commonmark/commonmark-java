package org.commonmark.internal;

import org.commonmark.Escaper;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Common {

	private static final String ENTITY = "&(?:#x[a-f0-9]{1,8}|#[0-9]{1,8}|[a-z][a-z0-9]{1,31});";

	// foo: not sure about [ in [
	static final String ESCAPABLE = "[!\"#$%&\'()*+,./:;<=>?@\\[\\\\\\]^_`{|}~-]";

	// foo: not sure about the backslashes here
	private static final Pattern reBackslashOrAmp = Pattern.compile("[\\\\&]");

	// foo: had flags 'gi' before
	private static final Pattern reEntityOrEscapedChar =
			Pattern.compile("\\\\" + ESCAPABLE + '|' + ENTITY, Pattern.CASE_INSENSITIVE);

	private static final String XMLSPECIAL = "[&<>\"]";

	// foo: had flags 'g' before
	private static final Pattern reXmlSpecial = Pattern.compile(XMLSPECIAL);

	// foo: had flags 'gi' before
	private static final Pattern reXmlSpecialOrEntity = Pattern.compile(ENTITY + '|' + XMLSPECIAL,
			Pattern.CASE_INSENSITIVE);

	// From MDN encodeURI documentation
	private static final Pattern reEscapeInUri =
			Pattern.compile("[^%;,/?:@&=+$#\\-_.!~*'()a-zA-Z0-9]");

	private static final char[] HEX_DIGITS = new char[] {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	// Replace entities and backslash escapes with literal characters.
	public static String unescapeString(String s) {
		if (reBackslashOrAmp.matcher(s).find()) {
			return replaceAll(reEntityOrEscapedChar, s, UNESCAPE_REPLACER);
		} else {
			return s;
		}
	}

	public static String normalizeURI(String uri) {
		return replaceAll(reEscapeInUri, uri, URI_REPLACER);
	}

	private static Pattern whitespace = Pattern.compile("[ \t\r\n]+");

	public static String normalizeReference(String input) {
		// foo: is this the same as JS?
		return whitespace.matcher(input.toLowerCase(Locale.ROOT)).replaceAll(" ");
	}

	public static Escaper XML_ESCAPER = new Escaper() {
		@Override
		public String escape(String input, boolean preserveEntities) {
			Pattern p = preserveEntities ? reXmlSpecialOrEntity : reXmlSpecial;
			return replaceAll(p, input, UNSAFE_CHAR_REPLACER);
		}
	};

	private static Replacer UNSAFE_CHAR_REPLACER = new Replacer() {
		@Override
		public void replace(String input, StringBuilder sb) {
			switch (input) {
				case "&":
					sb.append("&amp;");
					break;
				case "<":
					sb.append("&lt;");
					break;
				case ">":
					sb.append("&gt;");
					break;
				case "\"":
					sb.append("&quot;");
					break;
				default:
					sb.append(input);
			}
		}
	};

	private static Replacer UNESCAPE_REPLACER = new Replacer() {
		@Override
		public void replace(String input, StringBuilder sb) {
			if (input.charAt(0) == '\\') {
				sb.append(input, 1, input.length());
			} else {
				sb.append(Html5Entities.entityToString(input));
			}
		}
	};

	private static Replacer URI_REPLACER = new Replacer() {
		@Override
		public void replace(String input, StringBuilder sb) {
			byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
			for (byte b : bytes) {
				sb.append('%');
				sb.append(HEX_DIGITS[(b >> 4) & 0xF]);
				sb.append(HEX_DIGITS[b & 0xF]);
			}
		}
	};

	private static String replaceAll(Pattern p, String s,
	                                 Replacer replacer) {
		Matcher matcher = p.matcher(s);

		if (!matcher.find()) {
			return s;
		}

		StringBuilder sb = new StringBuilder(s.length() + 16);
		int lastEnd = 0;
		do {
			sb.append(s, lastEnd, matcher.start());
			replacer.replace(matcher.group(), sb);
			lastEnd = matcher.end();
		} while (matcher.find());

		if (lastEnd != s.length()) {
			sb.append(s, lastEnd, s.length());
		}
		return sb.toString();
	}

	private interface Replacer {
		void replace(String input, StringBuilder sb);
	}
}
