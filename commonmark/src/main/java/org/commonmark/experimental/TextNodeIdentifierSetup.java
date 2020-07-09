package org.commonmark.experimental;

public interface TextNodeIdentifierSetup extends NodeSetup {
    TextIdentifier textIdentifier();

    int priority();

    class DefaultPriority {
        public static final int DEFAULT = 0;
        public static final int REPEATABLE_SYMBOL = 10;
        public static final int BRACKET_SYMBOL = 20;
        public static final int BRACKET_PLUS_START_SYMBOL = 30;

        private DefaultPriority() {
        }
    }
}
