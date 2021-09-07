package org.commonmark.internal.inline;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Code;
import org.commonmark.node.Text;
import org.commonmark.parser.SourceLines;

/**
 * Attempt to parse backticks, returning either a backtick code span or a literal sequence of backticks.
 */
public class BackticksInlineParser implements InlineContentParser {

    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState) {
        Scanner scanner = inlineParserState.scanner();
        Position start = scanner.position();
        String preBlockWhitespace = "";
        
        char listCheck = scanner.peek();
        if(listCheck == '-' || listCheck == '*' || listCheck == '+') {
            preBlockWhitespace = listCheck + scanner.whitespaceAsString();
        }
        
        int openingTicks = scanner.matchMultiple('`');
        Position afterOpening = scanner.position();

        while (scanner.find('`') > 0) {
            Position beforeClosing = scanner.position();
            int count = scanner.matchMultiple('`');
            if (count == openingTicks) {
                Code node = new Code();

                node.setNumBackticks(count);
                
                String content = scanner.getSource(afterOpening, beforeClosing).getContent();
                // Capture raw string for roundtrip rendering
                node.setRaw(content);
                content = content.replace('\n', ' ');

                // spec: If the resulting string both begins and ends with a space character, but does not consist
                // entirely of space characters, a single space character is removed from the front and back.
                if (content.length() >= 3 &&
                        content.charAt(0) == ' ' &&
                        content.charAt(content.length() - 1) == ' ' &&
                        Parsing.hasNonSpace(content)) {
                    content = content.substring(1, content.length() - 1);
                }

                node.setLiteral(content);
                return ParsedInline.of(node, scanner.position());
            }
        }

        // If we got here, we didn't find a matching closing backtick sequence.
        SourceLines source = scanner.getSource(start, afterOpening);
        Text text = new Text(source.getContent(), source.getContent(), preBlockWhitespace, "");
        return ParsedInline.of(text, afterOpening);
    }
    
    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState, String prefix) {
        // Backticks do not have significant prefix values
        return tryParse(inlineParserState);
    }
}
