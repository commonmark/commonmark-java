package org.commonmark.internal;

class DelimiterRun {

    final int numdelims;
    final boolean can_close;
    final boolean can_open;

    DelimiterRun(int numdelims, boolean can_open, boolean can_close) {
        this.numdelims = numdelims;
        this.can_open = can_open;
        this.can_close = can_close;
    }

}
