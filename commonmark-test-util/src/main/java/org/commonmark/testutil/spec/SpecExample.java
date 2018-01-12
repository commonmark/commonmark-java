package org.commonmark.testutil.spec;

public class SpecExample {

    private final String section;
    private final int exampleNumber;
    private final String source;
    private final String html;

    public SpecExample(String section, int exampleNumber, String source, String html) {
        this.section = section;
        this.exampleNumber = exampleNumber;
        this.source = source;
        this.html = html;
    }

    public String getSource() {
        return source;
    }

    public String getHtml() {
        return html;
    }

    @Override
    public String toString() {
        return "Section \"" + section + "\" example " + exampleNumber;
    }
}
