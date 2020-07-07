package org.commonmark.experimental;


import org.commonmark.node.Image;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InlineParserNodeSetupFactoryTest {
    private InlineParser inlineParser;
    private Paragraph paragraph = new Paragraph();

    @Before
    public void setUp() {
        inlineParser = new InlineParserNodeSetupFactory().create(new InlineParserContext() {
            @Override
            public List<DelimiterProcessor> getCustomDelimiterProcessors() {
                return null;
            }

            @Override
            public LinkReferenceDefinition getLinkReferenceDefinition(String label) {
                return null;
            }
        });

        paragraph = new Paragraph();
    }

    @Test
    public void shouldTakeLiteralTextInParagraph() {
        inlineParser.parse("easy test", paragraph);

        assertThat(paragraph.getFirstChild(), instanceOf(Text.class));
        assertThat(((Text)paragraph.getFirstChild()).getLiteral(), is("easy test"));
    }

    @Test
    public void shouldNoticeImageLogoNode() {
        inlineParser.parse("![foo](/train.jpg)", paragraph);

        assertThat(paragraph.getFirstChild(), instanceOf(Image.class));
        assertThat(((Image)paragraph.getFirstChild()).getTitle(), is("foo"));
        assertThat(((Image)paragraph.getFirstChild()).getDestination(), is("/train.jpg"));
    }
}
