package com.atlassian.rstocker.cm;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.Function;
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

	static String unescapeChar(String s) {
		if (s.charAt(0) == '\\') {
			return s.substring(1);
		} else {
			return Html5Entities.entityToString(s);
		}
	}

	// Replace entities and backslash escapes with literal characters.
	public static String unescapeString(String s) {
		if (reBackslashOrAmp.matcher(s).find()) {
			return replaceAll(reEntityOrEscapedChar, s, Common::unescapeChar);
		} else {
			return s;
		}
	}

	public static String normalizeURI(String uri) {
		return replaceAll(reEscapeInUri, uri, Common::uriEscape);
	}

	private static String uriEscape(String s) {
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		StringBuilder sb = new StringBuilder(bytes.length * 3);
		for (byte b : bytes) {
			sb.append('%');
			sb.append(HEX_DIGITS[(b >> 4) & 0xF]);
			sb.append(HEX_DIGITS[b & 0xF]);
		}
		return sb.toString();
	}

	private static Pattern whitespace = Pattern.compile("[ \t\r\n]+");

	public static String normalizeReference(String input) {
		// foo: is this the same as JS?
		return whitespace.matcher(input.toLowerCase(Locale.ROOT)).replaceAll(" ");
	}

	public static Escaper XML_ESCAPER = (s, preserveEntities) -> {
		Pattern p = preserveEntities ? reXmlSpecialOrEntity : reXmlSpecial;
		return replaceAll(p, s, Common::replaceUnsafeChar);
	};

	private static String replaceUnsafeChar(String s) {
		switch (s) {
		case "&":
			return "&amp;";
		case "<":
			return "&lt;";
		case ">":
			return "&gt;";
		case "\"":
			return "&quot;";
		default:
			return s;
		}
	}

	private static String replaceAll(Pattern p, String s,
			Function<String, String> replacer) {
		Matcher matcher = p.matcher(s);

		if (!matcher.find()) {
			return s;
		}

		StringBuilder sb = new StringBuilder(s.length() + 16);
		int lastEnd = 0;
		do {
			sb.append(s, lastEnd, matcher.start());
			String replaced = replacer.apply(matcher.group());
			sb.append(replaced);
			lastEnd = matcher.end();
		} while (matcher.find());

		if (lastEnd != s.length()) {
			sb.append(s, lastEnd, s.length());
		}
		return sb.toString();
	}
}
