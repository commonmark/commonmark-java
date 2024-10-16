package org.commonmark.ext.autolink.internal;

import org.commonmark.node.*;
import org.commonmark.parser.PostProcessor;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;
import org.nibor.autolink.Span;

import java.util.*;

public class AutolinkPostProcessor implements PostProcessor {

    private LinkExtractor linkExtractor = LinkExtractor.builder()
            .linkTypes(EnumSet.of(LinkType.URL, LinkType.EMAIL))
            .build();

    @Override
    public Node process(Node node) {
        AutolinkVisitor autolinkVisitor = new AutolinkVisitor();
        node.accept(autolinkVisitor);
        return node;
    }

    private void linkify(Text originalTextNode) {
        String literal = originalTextNode.getLiteral();

        Node lastNode = originalTextNode;
        List<SourceSpan> sourceSpans = originalTextNode.getSourceSpans();
        SourceSpan sourceSpan = sourceSpans.size() == 1 ? sourceSpans.get(0) : null;

        Iterator<Span> spans = linkExtractor.extractSpans(literal).iterator();
        while (spans.hasNext()) {
            Span span = spans.next();

            if (lastNode == originalTextNode && !spans.hasNext() && !(span instanceof LinkSpan)) {
                // Didn't find any links, don't bother changing existing node.
                return;
            }

            Text textNode = createTextNode(literal, span, sourceSpan);
            if (span instanceof LinkSpan) {
                String destination = getDestination((LinkSpan) span, textNode.getLiteral());

                Link linkNode = new Link(destination, null);
                linkNode.appendChild(textNode);
                linkNode.setSourceSpans(textNode.getSourceSpans());
                lastNode = insertNode(linkNode, lastNode);
            } else {
                lastNode = insertNode(textNode, lastNode);
            }
        }

        // Original node no longer needed
        originalTextNode.unlink();
    }

    private static Text createTextNode(String literal, Span span, SourceSpan sourceSpan) {
        int beginIndex = span.getBeginIndex();
        int endIndex = span.getEndIndex();
        String text = literal.substring(beginIndex, endIndex);
        Text textNode = new Text(text);
        if (sourceSpan != null) {
            textNode.addSourceSpan(sourceSpan.subSpan(beginIndex, endIndex));
        }
        return textNode;
    }

    private static String getDestination(LinkSpan linkSpan, String linkText) {
        if (linkSpan.getType() == LinkType.EMAIL) {
            return "mailto:" + linkText;
        } else {
            return linkText;
        }
    }

    private static Node insertNode(Node node, Node insertAfterNode) {
        insertAfterNode.insertAfter(node);
        return node;
    }

    private class AutolinkVisitor extends AbstractVisitor {
        int inLink = 0;

        @Override
        public void visit(Link link) {
            inLink++;
            super.visit(link);
            inLink--;
        }

        @Override
        public void visit(Text text) {
            if (inLink == 0) {
                linkify(text);
            }
        }
    }
}
