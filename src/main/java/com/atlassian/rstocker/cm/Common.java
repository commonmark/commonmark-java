package com.atlassian.rstocker.cm;

import java.net.URI;
import java.net.URISyntaxException;
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
			return replaceAll(reEntityOrEscapedChar, s, match -> unescapeChar(match));
		} else {
			return s;
		}
	}

	public static String normalizeURI(String uri) {
		try {
			// foo: equivalent to encodeURI(decodeURI(uri))?
			uri = uri.replaceAll(" ", "%20");
			uri = uri.replaceAll("\\\\", "%5C");
			return new URI(uri).toASCIIString();
		} catch (URISyntaxException e) {
			return uri;
		}
	}

	private static Pattern whitespace = Pattern.compile("[ \t\r\n]+");

	public static String normalizeReference(String input) {
		// foo: is this the same as JS?
		return whitespace.matcher(input.toLowerCase(Locale.ROOT)).replaceAll(" ");
	}

	public static Escaper XML_ESCAPER = (s, preserveEntities) -> {
		Pattern p = preserveEntities ? reXmlSpecialOrEntity : reXmlSpecial;
		return replaceAll(p, s, match -> replaceUnsafeChar(match));
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
