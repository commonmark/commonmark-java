package org.commonmark.internal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Html5Entities {

    private static final Map<String, String> NAMED_CHARACTER_REFERENCES = readEntities();
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^&#[Xx]?");

    public static String entityToString(String input) {
        Matcher matcher = NUMERIC_PATTERN.matcher(input);

        if (matcher.find()) {
            int base = matcher.end() == 2 ? 10 : 16;
            try {
                int codePoint = Integer.parseInt(input.substring(matcher.end(), input.length() - 1), base);
                if (codePoint == 0) {
                    return "\uFFFD";
                }
                return new String(Character.toChars(codePoint));
            } catch (IllegalArgumentException e) {
                return "\uFFFD";
            }
        } else {
            String name = input.substring(1, input.length() - 1);
            String s = NAMED_CHARACTER_REFERENCES.get(name);
            if (s != null) {
                return s;
            } else {
                return input;
            }
        }
    }

    private static Map<String, String> readEntities() {
        Map<String, String> entities = new HashMap<>();
        InputStream stream = Html5Entities.class.getResourceAsStream("entities.properties");
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                int equal = line.indexOf("=");
                String key = line.substring(0, equal);
                String value = line.substring(equal + 1);
                entities.put(key, value);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading data for HTML named character references", e);
        } finally {
            if(bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        entities.put("NewLine", "\n");
        return entities;
    }
}
