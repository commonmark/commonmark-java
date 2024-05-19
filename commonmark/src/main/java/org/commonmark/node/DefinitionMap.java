package org.commonmark.node;

import org.commonmark.internal.util.Escaping;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A map that can be used to store and lookup reference definitions by a label. The labels are case-insensitive and
 * normalized, the same way as for {@link LinkReferenceDefinition} nodes.
 *
 * @param <V> the type of value
 */
public class DefinitionMap<V> {

    // LinkedHashMap for determinism and to preserve document order
    private final Map<String, V> definitions = new LinkedHashMap<>();

    /**
     * Store a new definition unless one is already in the map.
     */
    public void putIfAbsent(String label, V definition) {
        String normalizedLabel = Escaping.normalizeLabelContent(label);

        // spec: When there are multiple matching link reference definitions, the first is used
        definitions.putIfAbsent(normalizedLabel, definition);
    }

    /**
     * Lookup a definition by normalized label.
     *
     * @return the value or null
     */
    public V get(String label) {
        String normalizedLabel = Escaping.normalizeLabelContent(label);
        return definitions.get(normalizedLabel);
    }
}
