package org.commonmark.ext.heading.anchor.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.commonmark.html.AttributeProvider;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Code;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Text;

import org.commonmark.ext.heading.anchor.UniqueIdentifierProvider;

public class HeadingIdAttributeProvider implements AttributeProvider {

    private final UniqueIdentifierProvider idProvider;

    private HeadingIdAttributeProvider() {
        idProvider = new UniqueIdentifierProvider("heading");
    }

    public static HeadingIdAttributeProvider create() {
        return new HeadingIdAttributeProvider();
    }

    @Override
    public void setAttributes(Node node, final Map<String, String> attributes) {

        if (node instanceof Heading) {

            final List<String> wordList = new ArrayList<>();

            node.accept(new AbstractVisitor() {
                @Override
                public void visit(Text text) {
                    wordList.add(text.getLiteral());
                }

                @Override
                public void visit(Code code) {
                    wordList.add(code.getLiteral());
                }
            });

            attributes.put("id", idProvider.getUniqueIdentifier(String.join("", wordList)).toLowerCase());
        }
    }

}
