package com.atlassian.rstocker.cm;

import java.util.List;

public class Node {

    // TODO: change to enum
    private final String _type;
    private final int[][] _sourcepos;

    Node parent = null;
    Node firstChild = null;
    public Node lastChild = null;
    private Node prev = null;
    Node next = null;

    List<String> strings = null;
    public String string_content = null;
    public boolean last_line_blank = false;
    public boolean open = true;
    String literal;
    public ListData list_data = null;
    String info;
    String destination = null;
    String title = null;
    char fence_char; // null
    public int fence_length = 0;
    public int fence_offset = 0; // null
    int level = 0; // null

    public Node(String nodeType) {
    	this(nodeType, new int[0][0]);
    }
    
    public Node(String nodeType, int[][] sourcepos) {
        this._type = nodeType;
        this._sourcepos = sourcepos;
    }

    public boolean isContainer() {
        switch (_type) {
            case "Document":
            case "BlockQuote":
            case "List":
            case "Item":
            case "Paragraph":
            case "Header":
            case "Emph":
            case "Strong":
            case "Link":
            case "Image":
                return true;
            default:
                return false;
        }
    }

    public String type() {
        return this._type;
    }

    public int[][] sourcepos() {
        return this._sourcepos;
    }
    
    public boolean isListTight() {
    	return list_data.tight;
    }
    
    public String getListType() {
    	return list_data.type;
    }
    
    public int getListStart() {
    	return list_data.start;
    }

    public void appendChild(Node child) {
        child.unlink();
        child.parent = this;
        if (this.lastChild != null) {
            this.lastChild.next = child;
            child.prev = this.lastChild;
            this.lastChild = child;
        } else {
            this.firstChild = child;
            this.lastChild = child;
        }
    }

    public void prependChild(Node child) {
        child.unlink();
        child.parent = this;
        if (this.firstChild != null) {
            this.firstChild.prev = child;
            child.next = this.firstChild;
            this.firstChild = child;
        } else {
            this.firstChild = child;
            this.lastChild = child;
        }
    }

    void unlink() {
        if (this.prev != null) {
            this.prev.next = this.next;
        } else if (this.parent != null) {
            this.parent.firstChild = this.next;
        }
        if (this.next != null) {
            this.next.prev = this.prev;
        } else if (this.parent != null) {
            this.parent.lastChild = this.prev;
        }
        this.parent = null;
        this.next = null;
        this.prev = null;
    }

    public void insertAfter(Node sibling) {
        sibling.unlink();
        sibling.next = this.next;
        if (sibling.next != null) {
            sibling.next.prev = sibling;
        }
        sibling.prev = this;
        this.next = sibling;
        sibling.parent = this.parent;
        if (sibling.next == null) {
            sibling.parent.lastChild = sibling;
        }
    }

    public void insertBefore(Node sibling) {
        sibling.unlink();
        sibling.prev = this.prev;
        if (sibling.prev != null) {
            sibling.prev.next = sibling;
        }
        sibling.next = this;
        this.prev = sibling;
        sibling.parent = this.parent;
        if (sibling.prev == null) {
            sibling.parent.firstChild = sibling;
        }
    }

    public NodeWalker walker() {
        return new NodeWalker(this);
    }

    // foo: root field seems to be unnecessary

    public static class NodeWalker {
        private Node current;
        private boolean entering = true;

        public NodeWalker(Node root) {
            this.current = root;
        }

        public Entry next() {
                Node cur = this.current;
                boolean entering = this.entering;

                if (cur == null) {
                    return null;
                }

                boolean container = cur.isContainer();

                if (entering && container) {
                    if (cur.firstChild != null) {
                        this.current = cur.firstChild;
                        this.entering = true;
                    } else {
                        // stay on node but exit
                        this.entering = false;
                    }

                } else if (cur.next == null) {
                    this.current = cur.parent;
                    this.entering = false;

                } else {
                    this.current = cur.next;
                    this.entering = true;
                }

                return new Entry(cur, entering);
        }

        public static class Entry {
            final Node node;
            final boolean entering;

            public Entry(Node node, boolean entering) {
                this.node = node;
                this.entering = entering;
            }
        }
    }

}
