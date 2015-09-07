package org.commonmark.ext.autolink.internal;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.PostProcessor;

import android.util.Patterns;

import java.util.regex.Matcher;

public class AutolinkPostProcessor implements PostProcessor {
    @Override
    public Node process(Node node) {
        AutolinkVisitor autolinkVisitor = new AutolinkVisitor();
        node.accept(autolinkVisitor);
        return node;
    }

    private void linkify(Text text) {
        String literal = text.getLiteral();

        Matcher matcher = Patterns.WEB_URL.matcher(literal);

        Node lastNode = text;
        int last = 0;
        while (matcher.find()) {
            String linkText = matcher.group();

            if (matcher.start() != last) {
                lastNode = insertNode(new Text(literal.substring(last, matcher.start())), lastNode);
            }
            Text contentNode = new Text(linkText);
            Link linkNode = new Link(linkText, null);
            linkNode.appendChild(contentNode);
            lastNode = insertNode(linkNode, lastNode);
            last = matcher.end();
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
