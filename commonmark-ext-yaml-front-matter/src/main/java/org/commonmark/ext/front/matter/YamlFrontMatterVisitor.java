package org.commonmark.ext.front.matter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.commonmark.ext.front.matter.parser.YamlSubsetParser;
import org.commonmark.ext.front.matter.parser.RawContentParser;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Node;

public class YamlFrontMatterVisitor extends AbstractVisitor {
    private boolean present;
    private Map<String, List<String>> data;
    private String content;

    public YamlFrontMatterVisitor() {
        data = new LinkedHashMap<>();
        content = "";
        present = false;
    }

    /**
     * Reads the YAML front matter metadata, if the Markdown
     * document has the front matter and the extension
     * uses {@link YamlSubsetParser} (default).
     *
     * @return The data stored in YAML front matter or empty map.
     */
    public static Map<String, List<String>> readData(Node document) {
        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        document.accept(visitor);
        return visitor.getData();
    }

    /**
     * Reads the raw content of the front matter, if the Markdown document has
     * the front matter and the extension uses {@link RawContentParser}.
     *
     * @return Raw content of the front matter as string or empty string.
     */
    public static String readRawContent(Node document) {
        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        document.accept(visitor);
        return visitor.getRawContent();
    }

    @Override
    public void visit(CustomNode customNode) {
        if (customNode instanceof YamlFrontMatterNode) {
            data.put(
                    ((YamlFrontMatterNode) customNode).getKey(),
                    ((YamlFrontMatterNode) customNode).getValues()
            );
            present = true;
        } else if (customNode instanceof YamlFrontMatterRawContent) {
            content = ((YamlFrontMatterRawContent) customNode).getContent();
            present = true;
        } else {
            super.visit(customNode);
        }
    }

    /**
     * Returns the YAML front matter metadata, if the Markdown document has
     * the front matter and the extension uses {@link YamlSubsetParser}
     * (default).
     *
     * @return The data stored in YAML front matter or empty map
     */
    public Map<String, List<String>> getData() {
        return data;
    }

    /**
     * Returns the raw content of the front matter, if the Markdown
     * document has the front matter and the extension uses
     * {@link RawContentParser}.
     *
     * @return Raw content of the front matter as string or empty string.
     */
    public String getRawContent() {
        return content;
    }

    public boolean isFrontMatterPresent() {
        return present;
    }
}
