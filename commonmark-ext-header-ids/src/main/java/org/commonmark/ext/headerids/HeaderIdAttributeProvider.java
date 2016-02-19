package org.commonmark.ext.headerids;

import org.commonmark.html.AttributeProvider;
import org.commonmark.node.*;

import java.util.*;

/**
 * Extension for automatically turning plain URLs and email addresses into links.
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link org.commonmark.html.HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The parsed links are turned into normal {@link org.commonmark.node.Link} nodes.
 * </p>
 */
public class HeaderIdAttributeProvider implements AttributeProvider {

    private final Map<String, Integer> headingMap;

    private HeaderIdAttributeProvider() {
        headingMap = new HashMap<>();
    }

    public static AttributeProvider create() {
        return new HeaderIdAttributeProvider();
    }

    private class IdAttribute {
        StringBuilder sb = new StringBuilder();

        public void add(String s) {

            // Do some basic substitution
            s = s.toLowerCase();
            s = s.replaceAll(" +", "-");

            for(char c: s.toCharArray()) {
                if(0x0041 < c && c < 0x007A || c == '-' || c == '_') {
                    sb.append(c);
                }
            }
        }

        public String getUniqueHeader(Map<String, Integer> headingMap) {
            String currentValue = toString();
            if(!headingMap.containsKey(currentValue)) {
                headingMap.put(currentValue, 1);
                return currentValue;
            } else {
                int currentCount = headingMap.get(currentValue);
                headingMap.put(currentValue, currentCount + 1);
                return currentValue + currentCount;
            }

        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    @Override
    public void setAttributes(Node node, final Map<String, String> attributes) {

        if(node instanceof Heading) {

            final IdAttribute idAttribute = new IdAttribute();

            node.accept(new AbstractVisitor() {
                @Override
                public void visit(Text text) {
                    idAttribute.add(text.getLiteral());
                }

                @Override
                public void visit(Code code) {
                    idAttribute.add(code.getLiteral());
                }
            });

            attributes.put("id", idAttribute.getUniqueHeader(headingMap));
        }
    }
}
