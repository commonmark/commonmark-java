package org.commonmark.internal;

import org.commonmark.node.Text;

class Delimiter {

	final Text node;
	Delimiter previous;
	final int index;

	char cc;
	int numdelims = 1;
	Delimiter next;
	// foo2: camelCase these?
	boolean can_open = true;
	boolean can_close = false;
	boolean active = true;

	Delimiter(Text node, Delimiter previous, int index) {
		this.node = node;
		this.previous = previous;
		this.index = index;
	}
}
