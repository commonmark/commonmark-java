package org.commonmark.internal;

import org.commonmark.node.DefinitionMap;

import java.util.HashMap;
import java.util.Map;

public class Definitions {

    private final Map<Class<?>, DefinitionMap<?>> definitionsByType = new HashMap<>();

    public <D> void addDefinitions(DefinitionMap<D> definitionMap) {
        var existingMap = getMap(definitionMap.getType());
        if (existingMap == null) {
            definitionsByType.put(definitionMap.getType(), definitionMap);
        } else {
            existingMap.addAll(definitionMap);
        }
    }

    public <V> V getDefinition(Class<V> type, String label) {
        var definitionMap = getMap(type);
        if (definitionMap == null) {
            return null;
        }
        return definitionMap.get(label);
    }

    private <V> DefinitionMap<V> getMap(Class<V> type) {
        //noinspection unchecked
        return (DefinitionMap<V>) definitionsByType.get(type);
    }
}
