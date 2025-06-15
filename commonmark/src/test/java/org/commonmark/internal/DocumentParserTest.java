package org.commonmark.internal;

import org.commonmark.node.*;
import org.commonmark.parser.block.BlockParserFactory;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentParserTest {
    private static final List<BlockParserFactory> CORE_FACTORIES = List.of(
            new BlockQuoteParser.Factory(),
            new HeadingParser.Factory(),
            new FencedCodeBlockParser.Factory(),
            new HtmlBlockParser.Factory(),
            new ThematicBreakParser.Factory(),
            new ListBlockParser.Factory(),
            new IndentedCodeBlockParser.Factory());

    @Test
    void calculateBlockParserFactories_givenAFullListOfAllowedNodes_includesAllCoreFactories() {
        List<BlockParserFactory> customParserFactories = List.of();
        var enabledBlockTypes = Set.of(BlockQuote.class, Heading.class, FencedCodeBlock.class, HtmlBlock.class, ThematicBreak.class, ListBlock.class, IndentedCodeBlock.class);

        List<BlockParserFactory> blockParserFactories = DocumentParser.calculateBlockParserFactories(customParserFactories, enabledBlockTypes);
        assertThat(blockParserFactories).hasSameSizeAs(CORE_FACTORIES);

        for (BlockParserFactory factory : CORE_FACTORIES) {
            assertThat(hasInstance(blockParserFactories, factory.getClass())).isTrue();
        }
    }

    @Test
    void calculateBlockParserFactories_givenAListOfAllowedNodes_includesAssociatedFactories() {
        List<BlockParserFactory> customParserFactories = List.of();
        Set<Class<? extends Block>> nodes = new HashSet<>();
        nodes.add(IndentedCodeBlock.class);

        List<BlockParserFactory> blockParserFactories = DocumentParser.calculateBlockParserFactories(customParserFactories, nodes);

        assertThat(blockParserFactories).hasSize(1);
        assertThat(hasInstance(blockParserFactories, IndentedCodeBlockParser.Factory.class)).isTrue();
    }

    private boolean hasInstance(List<BlockParserFactory> blockParserFactories, Class<? extends BlockParserFactory> factoryClass) {
        for (BlockParserFactory factory : blockParserFactories) {
            if (factory.getClass().equals(factoryClass)) {
                return true;
            }
        }
        return false;
    }

}
