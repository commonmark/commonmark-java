package org.commonmark.internal;

class DelimiterRun {

    final int numDelims;
    final boolean canClose;
    final boolean canOpen;

    DelimiterRun(int numDelims, boolean canOpen, boolean canClose) {
        this.numDelims = numDelims;
        this.canOpen = canOpen;
        this.canClose = canClose;
    }

}
