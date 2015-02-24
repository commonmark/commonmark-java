package com.atlassian.rstocker.cm.nodes;

public abstract class Node {

	public enum Type {
		// containers
		Document,
		BlockQuote,
		List,
		Item,
		Paragraph,
		Header,
		Emph,
		Strong,
		Link,
		Image,

		// non-container
		CodeBlock,
		HtmlBlock,
		HorizontalRule,
		Text,
		Softbreak,
		Hardbreak,
		Html,
		Code
	}

	private Node parent = null;
	private Node firstChild = null;
	private Node lastChild = null;
	private Node prev = null;
	private Node next = null;

	public boolean isContainer() {
		switch (getType()) {
		case Document:
		case BlockQuote:
		case List:
		case Item:
		case Paragraph:
		case Header:
		case Emph:
		case Strong:
		case Link:
		case Image:
			return true;
		default:
			return false;
		}
	}

	public abstract Type getType();

	public Node getNext() {
		return next;
	}

	public Node getFirstChild() {
		return firstChild;
	}

	public Node getLastChild() {
		return lastChild;
	}

	public Node getParent() {
		return parent;
	}

	protected void setParent(Node parent) {
		this.parent = parent;
	}

	public void appendChild(Node child) {
		child.unlink();
		child.setParent(this);
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
		child.setParent(this);
		if (this.firstChild != null) {
			this.firstChild.prev = child;
			child.next = this.firstChild;
			this.firstChild = child;
		} else {
			this.firstChild = child;
			this.lastChild = child;
		}
	}

	public void unlink() {
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

	@Override
	public String toString() {
		return "Node{type=" + getType() + "}";
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
