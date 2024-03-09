package org.commonmark.text;

import java.util.BitSet;
import java.util.Set;

/**
 * Char matcher that can match ASCII characters efficiently.
 */
public class AsciiMatcher implements CharMatcher {
    private final BitSet set;

    private AsciiMatcher(Builder builder) {
        this.set = builder.set;
    }

    @Override
    public boolean matches(char c) {
        return set.get(c);
    }

    public Builder newBuilder() {
        return new Builder((BitSet) set.clone());
    }

    public static Builder builder() {
        return new Builder(new BitSet());
    }

    public static Builder builder(AsciiMatcher matcher) {
        return new Builder((BitSet) matcher.set.clone());
    }

    public static class Builder {
        private final BitSet set;

        private Builder(BitSet set) {
            this.set = set;
        }

        public Builder c(char c) {
            if (c > 127) {
                throw new IllegalArgumentException("Can only match ASCII characters");
            }
            set.set(c);
            return this;
        }

        public Builder anyOf(String s) {
            for (int i = 0; i < s.length(); i++) {
                c(s.charAt(i));
            }
            return this;
        }

        public Builder anyOf(Set<Character> characters) {
            for (Character c : characters) {
                c(c);
            }
            return this;
        }

        public Builder range(char from, char toInclusive) {
            for (char c = from; c <= toInclusive; c++) {
                c(c);
            }
            return this;
        }

        public AsciiMatcher build() {
            return new AsciiMatcher(this);
        }
    }
}
