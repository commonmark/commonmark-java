package org.commonmark.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.commonmark.html.HtmlRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ParserTest {
    @Test
    public void ioReaderTest() throws IOException {
        Parser parser = Parser.builder().build();
        
        InputStream input1 = ParserTest.class.getResourceAsStream("/spec.txt");
        Node document1;
        try (InputStreamReader reader = new InputStreamReader(input1)) {
            document1 = parser.parseReader(reader);
        }
        
        InputStream input2 = ParserTest.class.getResourceAsStream("/spec.txt");
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(input2)) {
            int ch;
            while ((ch = reader.read()) != -1){
                sb.append((char)ch);
            }
        }
        
        Node document2 = parser.parse(sb.toString());
        
        HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
        assertEquals(renderer.render(document2), renderer.render(document1));
    }
    
    @Test
    public void outputStreamTest() throws IOException {
        Parser parser = Parser.builder().build();

        InputStream input = ParserTest.class.getResourceAsStream("/spec.txt");
        Node document;
        try (InputStreamReader reader = new InputStreamReader(input)) {
            document = parser.parseReader(reader);
        }
        
        HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        renderer.render(document, baos);
        
        assertEquals(renderer.render(document), baos.toString());
    }
}
