package org.commonmark.ext.front.matter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.ext.front.matter.parser.FrontMatterParser;
import org.commonmark.parser.SourceLine;
import org.junit.jupiter.api.Test;

public class YamlFrontMatterCustomParserTest extends YamlFrontMatterTestCase {
    @Override
    Set<Extension> getExtensions() {
        return Set.of(YamlFrontMatterExtension.create(new Factory()));
    }

    @Test
    public void separatorCanBePartOfRawContent() {
        final String input = "---" + "\nstring: '" + "\n---" + "\n'" + "\n..." + "\n" + "\ngreat";
        final String rendered = "<p>great</p>\n";

        String content = getFrontMatterContent(input);

        assertThat(content).isEqualTo("string: '\n" + "---\n" + "'\n");
        assertRendering(input, rendered);
    }

    /** Allows testing {@link FrontMatterParser.SeparatorRole#CONTENT} */
    class CustomParser implements FrontMatterParser {
        private StringBuilder content;
        private boolean separatorAsContent = false;

        public CustomParser() {
            content = new StringBuilder();
        }

        @Override
        public void onNextLine(YamlFrontMatterBlock block, SourceLine line) {
            content.append(line.getContent()).append('\n');
        }

        @Override
        public SeparatorRole onEndingSeparator(YamlFrontMatterBlock block, SourceLine separator) {
            if (!separatorAsContent) {
                content.append(separator.getContent()).append('\n');
                separatorAsContent = true;
                return SeparatorRole.CONTENT;
            } else {
                block.appendChild(new YamlFrontMatterRawContent(content.toString()));
                return SeparatorRole.BLOCK_END;
            }
        }
    }

    public class Factory implements FrontMatterParser.Factory {
        @Override
        public FrontMatterParser create() {
            return new CustomParser();
        }
    }
}
