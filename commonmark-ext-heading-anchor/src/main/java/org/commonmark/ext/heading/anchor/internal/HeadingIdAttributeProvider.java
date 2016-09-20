package org.commonmark.ext.heading.anchor.internal;

import org.commonmark.ext.heading.anchor.IdGenerator;
import org.commonmark.html.attribute.AttributeProvider;
import org.commonmark.node.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HeadingIdAttributeProvider implements AttributeProvider {

    private final IdGenerator idGenerator;

    private HeadingIdAttributeProvider() {
        idGenerator = IdGenerator.builder().defaultId("heading").build();
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

            String finalString = "";
            for (String word : wordList) {
                finalString += word;
            }
            finalString = finalString.trim().toLowerCase();

            attributes.put("id", idGenerator.generateId(finalString));
        }
    }
}
