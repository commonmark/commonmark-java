package org.commonmark.ext.heading.anchor.internal;

import org.commonmark.ext.heading.anchor.IdGenerator;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.node.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HeadingIdAttributeProvider implements AttributeProvider {

    private final IdGenerator idGenerator;

    private HeadingIdAttributeProvider(String defaultId, String prefix, String suffix) {
        idGenerator = IdGenerator.builder()
                .defaultId(defaultId)
                .prefix(prefix)
                .suffix(suffix)
                .build();
    }

    public static HeadingIdAttributeProvider create(String defaultId, String prefix, String suffix) {
        return new HeadingIdAttributeProvider(defaultId, prefix, suffix);
    }

    @Override
    public void setAttributes(Node node, String tagName, final Map<String, String> attributes) {

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
