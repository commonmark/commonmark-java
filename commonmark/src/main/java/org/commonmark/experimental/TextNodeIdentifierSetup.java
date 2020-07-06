package org.commonmark.experimental;

public interface TextNodeIdentifierSetup extends NodeSetup {
    TextIdentifier textIdentifier();

    int priority();
}
