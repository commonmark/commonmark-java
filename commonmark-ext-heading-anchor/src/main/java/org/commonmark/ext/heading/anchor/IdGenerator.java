package org.commonmark.ext.heading.anchor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates strings to be used as identifiers.
 * <p>
 * Use {@link #builder()} to create an instance.
 */
public class IdGenerator {
    private final Pattern allowedCharacters;
    private final Map<String, Integer> identityMap;
    private final String prefix;
    private final String suffix;
    private String defaultIdentifier;

    private IdGenerator(Builder builder) {
        this.allowedCharacters = compileAllowedCharactersPattern();
        this.defaultIdentifier = builder.defaultIdentifier;
        this.prefix = builder.prefix;
        this.suffix = builder.suffix;
        this.identityMap = new HashMap<>();
    }

    /**
     * @return a new builder with default arguments
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * <p>
     * Generate an ID based on the provided text and previously generated IDs.
     * <p>
     * This method is not thread safe, concurrent calls can end up
     * with non-unique identifiers.
     * <p>
     * Note that collision can occur in the case that
     * <ul>
     * <li>Method called with 'X'</li>
     * <li>Method called with 'X' again</li>
     * <li>Method called with 'X-1'</li>
     * </ul>
     * <p>
     * In that case, the three generated IDs will be:
     * <ul>
     * <li>X</li>
     * <li>X-1</li>
     * <li>X-1</li>
     * </ul>
     * <p>
     * Therefore if collisions are unacceptable you should ensure that
     * numbers are stripped from end of {@code text}.
     *
     * @param text Text that the identifier should be based on. Will be normalised, then used to generate the
     * identifier.
     * @return {@code text} if this is the first instance that the {@code text} has been passed
     * to the method. Otherwise, {@code text + "-" + X} will be returned, where X is the number of times
     * that {@code text} has previously been passed in. If {@code text} is empty, the default
     * identifier given in the constructor will be used.
     */
    public String generateId(String text) {
        String normalizedIdentity = text != null ? normalizeText(text) : defaultIdentifier;

        if (normalizedIdentity.length() == 0) {
            normalizedIdentity = defaultIdentifier;
        }

        if (!identityMap.containsKey(normalizedIdentity)) {
            identityMap.put(normalizedIdentity, 1);
            return prefix + normalizedIdentity + suffix;
        } else {
            int currentCount = identityMap.get(normalizedIdentity);
            identityMap.put(normalizedIdentity, currentCount + 1);
            return prefix + normalizedIdentity + "-" + currentCount + suffix;
        }
    }

    private static Pattern compileAllowedCharactersPattern() {
        String regex = "[\\w\\-_]+";
        try {
            return Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS);
        } catch (IllegalArgumentException e) {
            // Android only supports the flag in API level 24. But it actually uses Unicode character classes by
            // default, so not specifying the flag is ok. See issue #71.
            return Pattern.compile(regex);
        }
    }

    /**
     * Assume we've been given a space separated text.
     *
     * @param text Text to normalize to an ID
     */
    private String normalizeText(String text) {
        String firstPassNormalising = text.toLowerCase().replace(" ", "-");

        StringBuilder sb = new StringBuilder();
        Matcher matcher = allowedCharacters.matcher(firstPassNormalising);

        while (matcher.find()) {
            sb.append(matcher.group());
        }

        return sb.toString();
    }

    public static class Builder {
        private String defaultIdentifier = "id";
        private String prefix = "";
        private String suffix = "";

        public IdGenerator build() {
            return new IdGenerator(this);
        }

        /**
         * @param defaultId the default identifier to use in case the provided text is empty or only contains unusable characters
         * @return {@code this}
         */
        public Builder defaultId(String defaultId) {
            this.defaultIdentifier = defaultId;
            return this;
        }

        /**
         * @param prefix the text to place before the generated identity
         * @return {@code this}
         */
        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * @param suffix the text to place after the generated identity
         * @return {@code this}
         */
        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }
    }
}
