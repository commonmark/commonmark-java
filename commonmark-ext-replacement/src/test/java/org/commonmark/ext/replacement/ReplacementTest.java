package org.commonmark.ext.replacement;

import org.commonmark.Extension;
import org.commonmark.clean.CleanRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplacementTest {
    private Parser mParser;

    @Before
    public void setUp() throws Exception {
        List<Extension> extensions = new ArrayList<>();

        Map<String, String> replacementMap = new HashMap<>();

        replacementMap.put(":)", "x_X");
        replacementMap.put("is", "it");
        replacementMap.put("before", "after");
        replacementMap.put("text", "string");

        extensions.add(ReplacementExtension.create(replacementMap));

        mParser = Parser.builder().extensions(extensions).build();
    }

    @Test
    public void testSingleWord() throws Exception {
        String before = "before";
        String after = "after";

        test(before, after);
    }

    @Test
    public void testSingleSmileWord() throws Exception {
        String before = ":)";
        String after = "x_X";

        test(before, after);
    }

    @Test
    public void testOneWord() throws Exception {
        String before = "This text for testing";
        String after = "This string for testing";

        test(before, after);
    }

    @Test
    public void testManyWords() throws Exception {
        String before = "This is text before testing";
        String after = "This it string after testing";

        test(before, after);
    }

    @Test
    public void testWithoutWhitespaces() throws Exception {
        String before = "This istext before testing";
        String after = "This istext after testing";

        test(before, after);
    }

    private void test(String before, String after) {
        Node document = mParser.parse(before);

        CleanRenderer renderer = new CleanRenderer();
        String result = renderer.render(document);

        Assert.assertEquals(after, result);
    }
}