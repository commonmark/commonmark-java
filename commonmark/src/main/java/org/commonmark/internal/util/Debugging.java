package org.commonmark.util;

import java.util.Stack;
import org.commonmark.node.Node;

public class Debugging {

    /**
     * Print a tree-representation of the given node and its children.
     */
    public static String toStringTree(Node node) {
        StringBuilder b = new StringBuilder();
        Stack<Node> stack = new Stack();

        visit(b, stack, node);

        return b.toString();
    }

    private static void visit(StringBuilder b, Stack<Node> stack, Node node) {

        int sz = stack.size();
        for (int i = 0; i < sz; i++) {
            b.append('.');
        }
        b.append(node.toString());
        b.append('\n');

        Node current = node.getFirstChild();
        while (current != null) {
            stack.push(current);
            visit(b, stack, stack.peek());
            stack.pop();
            current = current.getNext();
        }

    }

    /**
     * Log a simple message.  This is not a substitute for a logging
     * framework.
     */
    public static void log(String msg) {
        System.out.println("commonmark-java: " + msg);
    }

    /**
     * Print a stacktrace with the given message.
     */
    public static void stacktrace(String msg) {
	try {
	    throw new Exception(msg);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

}
