package ext.heading.anchor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provider of unique strings to be used as an identifier
 */
public class UniqueIdentifierProvider {
    private final Pattern allowedCharacters = Pattern.compile("[\\w\\-_]+", Pattern.UNICODE_CHARACTER_CLASS);
    private final Map<String, Integer> identityMap;
    private String defaultIdentifier;

    /**
     * Create a provider with the default identifier of "id"
     */
    public UniqueIdentifierProvider() {
        this("id");
    }

    /**
     * @param defaultIdentifier default identifier to use if given a null, or empty {@code providedIdentity}
     *                          in {@link #getUniqueIdentifier(String)}
     */
    public UniqueIdentifierProvider(String defaultIdentifier) {
        this.defaultIdentifier = defaultIdentifier;
        this.identityMap = new HashMap<>();
    }

    public String getDefaultIdentifier() {
        return defaultIdentifier;
    }

    public void setDefaultIdentifier(String defaultIdentifier) {
        this.defaultIdentifier = defaultIdentifier;
    }

    /**
     * <p>
     * Provides unique strings over the lifetime of the instance
     * </p>
     * <p>
     * This method is not thread safe, concurrent calls can end up
     * with non-unique identifiers
     * </p>
     * <p>
     * Note that collision can occur in the case that
     * <ul>
     * <li>String 'X' provided to getUniqueIdentifier</li>
     * <li>String 'X' provided again to getUniqueIdentifier</li>
     * <li>String 'X-1' provided to getUniqueIdentifier</li>
     * </ul>
     * </p>
     * <p>
     * In that case, the three ids provided will be
     * <ul>
     * <li>X</li>
     * <li>X-1</li>
     * <li>X-1</li>
     * </ul>
     * </p>
     * <p>
     * Therefore if collisions are unnacceptable you should ensure that
     * numbers are stripped from end of {@code providedIdentity}
     * </p>
     *
     * @param providedIdentity tentative identifier to be used. Will be normalised, then used to generate the
     *                         identifier.
     *                         Becomes the basis for the returned identifier.
     * @return {@code providedIdentity} if this is the first instance that the {@code providedIdentity} has been passed
     * to the method. Otherwise, {@code providedIdentity + "-" + X} will be returned, where X is the number of times
     * that {@code providedIdentity} has previously been passed in. If {@code providedIdentity} is empty, the default
     * identifier given in the constructor (or "id" if none given) will be used.
     */
    public String getUniqueIdentifier(String providedIdentity) {
        String normalizedIdentity =
                providedIdentity != null ?
                        normalizeProvidedIdentity(providedIdentity) : defaultIdentifier;

        if (normalizedIdentity.trim().length() == 0) {
            normalizedIdentity = defaultIdentifier;
        }

        if (!identityMap.containsKey(normalizedIdentity)) {
            identityMap.put(normalizedIdentity, 1);
            return normalizedIdentity;
        }
        else {
            int currentCount = identityMap.get(normalizedIdentity);
            identityMap.put(normalizedIdentity, currentCount + 1);
            return normalizedIdentity + "-" + currentCount;
        }
    }

    /**
     * Assume we've been given a space separated
     *
     * @param providedIdentity
     */
    private String normalizeProvidedIdentity(String providedIdentity) {
        String firstPassNormalising = providedIdentity.toLowerCase()
                .replace(" ", "-");

        StringBuilder sb = new StringBuilder();
        Matcher matcher = allowedCharacters.matcher(firstPassNormalising);

        while (matcher.find()) {
            sb.append(matcher.group());
        }

        return sb.toString();
    }
}
