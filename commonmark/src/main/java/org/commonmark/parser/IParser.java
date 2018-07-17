package org.commonmark.parser;

import java.io.IOException;
import java.io.Reader;
import org.commonmark.node.Node;

/**
 * Markdown parser interface.
 */
public interface IParser {

    Node parse(String input);

    Node parseReader(Reader input) throws IOException;

}
