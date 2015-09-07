package org.commonmark.clean;

public class CleanWriter {
    private final StringBuilder mBuffer;

    private char mLastChar = 0;

    public CleanWriter(StringBuilder out) {
        mBuffer = out;
    }

    public void divider() {
        if (mLastChar != 0 && mLastChar != ' ') {
            mLastChar = ' ';
            mBuffer.append(mLastChar);
        }
    }

    public void write(String text) {
        StringBuilder ssb = new StringBuilder(text.trim().replace("\n+", " "));

        if (ssb.length() != 0) {
            mLastChar = ssb.charAt(ssb.length() - 1);
        }

        mBuffer.append(ssb);
    }
}
