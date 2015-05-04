package org.commonmark.html;

import org.commonmark.node.FencedCodeBlock;

import java.util.HashMap;
import java.util.Map;

public class CodeBlockAttributeProvider {

    /**
     * @param fencedCodeBlock the block to provide attributes for
     * @return a map of attribute name to values; the values will be escaped by the caller before rendering
     */
    public Map<String, String> getAttributes(FencedCodeBlock fencedCodeBlock) {
        Map<String, String> attributes = new HashMap<>();
        String info = fencedCodeBlock.getInfo();
        if (info != null && !info.isEmpty()) {
            int space = info.indexOf(" ");
            String language;
            if (space == -1) {
                language = info;
            } else {
                language = info.substring(0, space);
            }
            attributes.put("class", "language-" + language);
        }
        return attributes;
    }

}
