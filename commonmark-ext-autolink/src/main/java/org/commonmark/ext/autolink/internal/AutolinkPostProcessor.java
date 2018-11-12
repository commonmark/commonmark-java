package org.commonmark.ext.autolink.internal;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.PostProcessor;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;
import org.nibor.autolink.Span;

import java.util.EnumSet;

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

    private void linkify(Text textNode) {
        String literal = textNode.getLiteral();

        Node lastNode = textNode;

        for (Span span : linkExtractor.extractSpans(literal)) {
            String text = literal.substring(span.getBeginIndex(), span.getEndIndex());
            if (span instanceof LinkSpan) {
                String destination = getDestination((LinkSpan) span, text);
                Text contentNode = new Text(text);
                Link linkNode = new Link(destination, null);
                linkNode.appendChild(contentNode);
                lastNode = insertNode(linkNode, lastNode);
            } else {
                lastNode = insertNode(new Text(text), lastNode);
            }
        }

        // Original node no longer needed
        textNode.unlink();
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
