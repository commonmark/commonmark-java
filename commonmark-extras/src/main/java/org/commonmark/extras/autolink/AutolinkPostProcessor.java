package org.commonmark.extras.autolink;

import org.commonmark.extras.TextFusingVisitor;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.PostProcessor;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;

import java.util.List;

public class AutolinkPostProcessor implements PostProcessor {

    private LinkExtractor linkExtractor = LinkExtractor.builder().build();

    @Override
    public Node process(Node node) {
        TextFusingVisitor textFusingVisitor = new TextFusingVisitor();
        AutolinkVisitor autolinkVisitor = new AutolinkVisitor(linkExtractor);
        node.accept(textFusingVisitor);
        node.accept(autolinkVisitor);
        return node;
    }

    private static class AutolinkVisitor extends AbstractVisitor {

        private final LinkExtractor linkExtractor;

        public AutolinkVisitor(LinkExtractor linkExtractor) {
            this.linkExtractor = linkExtractor;
        }

        @Override
        public void visit(Text text) {
            String literal = text.getLiteral();
            Iterable<LinkSpan> links = linkExtractor.extractLinks(literal);

            Node lastNode = text;
            int last = 0;
            for (LinkSpan span : links) {
                if (span.getBeginIndex() != last) {
                    lastNode = insertNode(new Text(literal.substring(last, span.getBeginIndex())), lastNode);
                }
                String destination = literal.substring(span.getBeginIndex(), span.getEndIndex());
                Text linkText = new Text(destination);
                Link link = new Link(destination, null);
                link.appendChild(linkText);
                lastNode = insertNode(link, lastNode);
                last = span.getEndIndex();
            }
            if (last != literal.length()) {
                insertNode(new Text(literal.substring(last)), lastNode);
            }
            text.unlink();
        }

        private static Node insertNode(Node node, Node insertAfterNode) {
            insertAfterNode.insertAfter(node);
            return node;
        }
    }
}
