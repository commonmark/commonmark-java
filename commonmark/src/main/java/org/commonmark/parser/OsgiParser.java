package org.commonmark.parser;


import java.io.IOException;
import java.io.Reader;
import org.commonmark.node.Node;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * This class exposes the Parser as an OSGi DS service.
 */
@Component(service = IParser.class, immediate = false)
public class OsgiParser implements IParser {

    protected Parser parser;

    @Activate
    public void activate() {
        parser = Parser.builder().build();
    }

    @Override
    public Node parse(String input) {
        return parser.parse(input);
    }

    @Override
    public Node parseReader(Reader input) throws IOException {
        return parser.parseReader(input);
    }
}
