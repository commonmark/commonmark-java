package org.commonmark.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.junit.jupiter.api.Test;

public class DelimitedTest {

    @Test
    public void emphasisDelimiters() {
        String input =
                """
                * *emphasis*\s
                * **strong**\s
                * _important_\s
                * __CRITICAL__\s
                """;

        Parser parser = Parser.builder().build();
        Node document = parser.parse(input);

        final List<Delimited> list = new ArrayList<>();
        Visitor visitor =
                new AbstractVisitor() {
                    @Override
                    public void visit(Emphasis node) {
                        list.add(node);
                    }

                    @Override
                    public void visit(StrongEmphasis node) {
                        list.add(node);
                    }
                };
        document.accept(visitor);

        assertThat(list).hasSize(4);

        Delimited emphasis = list.get(0);
        Delimited strong = list.get(1);
        Delimited important = list.get(2);
        Delimited critical = list.get(3);

        assertThat(emphasis.getOpeningDelimiter()).isEqualTo("*");
        assertThat(emphasis.getClosingDelimiter()).isEqualTo("*");
        assertThat(strong.getOpeningDelimiter()).isEqualTo("**");
        assertThat(strong.getClosingDelimiter()).isEqualTo("**");
        assertThat(important.getOpeningDelimiter()).isEqualTo("_");
        assertThat(important.getClosingDelimiter()).isEqualTo("_");
        assertThat(critical.getOpeningDelimiter()).isEqualTo("__");
        assertThat(critical.getClosingDelimiter()).isEqualTo("__");
    }
}
