package org.commonmark.node;

import org.commonmark.internal.util.Escaping;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A map that can be used to store and look up reference definitions by a label. The labels are case-insensitive and
 * normalized, the same way as for {@link LinkReferenceDefinition} nodes.
 *
 * @param <D> the type of value
 */
public class DefinitionMap<D> {

    private final Class<D> type;
    // LinkedHashMap for determinism and to preserve document order
    private final Map<String, D> definitions = new LinkedHashMap<>();

    public DefinitionMap(Class<D> type) {
        this.type = type;
    }

    public Class<D> getType() {
        return type;
    }

    public void addAll(DefinitionMap<D> that) {
        for (var entry : that.definitions.entrySet()) {
            // Note that keys are already normalized, so we can add them directly
            definitions.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Store a new definition unless one is already in the map. If there is no definition for that label yet, return null.
     * Otherwise, return the existing definition.
     * <p>
     * The label is normalized by the definition map before storing.
     */
    public D putIfAbsent(String label, D definition) {
        String normalizedLabel = Escaping.normalizeLabelContent(label);

        // spec: When there are multiple matching link reference definitions, the first is used
        return definitions.putIfAbsent(normalizedLabel, definition);
    }

    /**
     * Look up a definition by label. The label is normalized by the definition map before lookup.
     *
     * @return the value or null
     */
    public D get(String label) {
        String normalizedLabel = Escaping.normalizeLabelContent(label);
        return definitions.get(normalizedLabel);
    }

    public Set<String> keySet() {
        return definitions.keySet();
    }

    public Collection<D> values() {
        return definitions.values();
    }
}
