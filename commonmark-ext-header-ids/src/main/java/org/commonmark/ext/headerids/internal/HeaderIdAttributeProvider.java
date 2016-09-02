package org.commonmark.ext.headerids.internal;

import java.util.Map;

import org.commonmark.ext.headerids.UniqueIdentifierProvider;
import org.commonmark.html.AttributeProvider;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Code;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Text;

public class HeaderIdAttributeProvider implements AttributeProvider {

    private final UniqueIdentifierProvider idProvider;

    private HeaderIdAttributeProvider() {
        idProvider = new UniqueIdentifierProvider("heading");
    }

    public static HeaderIdAttributeProvider create() {
        return new HeaderIdAttributeProvider();
    }

    @Override
    public void setAttributes(Node node, final Map<String, String> attributes) {

        if (node instanceof Heading) {

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

            attributes.put("id", idProvider.getUniqueIdentifier(idAttribute.toString()));
        }
    }

    private class IdAttribute {
        StringBuilder sb = new StringBuilder();

        public void add(String s) {

            // Do some basic substitution
            s = s.toLowerCase();
            s = s.replaceAll(" +", "-");

            for (char c : s.toCharArray()) {
                if (isAllowedCharacter(c)) {
                    sb.append(c);
                }
            }
        }

        public boolean isAllowedCharacter(char c) {
            return Character.isAlphabetic(c)
                    || Character.isDigit(c)
                    || c == '_' || c == '-'
                    || (0x0300 <= c && c <= 0x036F); // Combining diacritical marks
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
