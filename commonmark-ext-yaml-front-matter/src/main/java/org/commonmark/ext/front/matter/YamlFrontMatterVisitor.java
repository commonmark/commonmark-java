package org.commonmark.ext.front.matter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.commonmark.ext.front.matter.extractor.YamlDataExtractor;
import org.commonmark.ext.front.matter.extractor.YamlContentExtractor;
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
     * document has the YAML front matter and the extension
     * uses {@link YamlDataExtractor} (default).
     *
     * @return The data stored in YAML front matter or empty map
     */
    public static Map<String, List<String>> readData(Node document) {
        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        document.accept(visitor);
        return visitor.getData();
    }

    /**
     * Reads the YAML Front Matter metadata as a string, if the Markdown
     * document has the YAML Front Matter and the extension uses
     * {@link YamlContentExtractor} (default).
     *
     * @return The content of YAML front matter as string or empty string.
     */
    public static String readContent(Node document) {
        YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
        document.accept(visitor);
        return visitor.getContent();
    }

    @Override
    public void visit(CustomNode customNode) {
        if (customNode instanceof YamlFrontMatterNode) {
            data.put(
                    ((YamlFrontMatterNode) customNode).getKey(),
                    ((YamlFrontMatterNode) customNode).getValues()
            );
            present = true;
        } else if (customNode instanceof YamlFrontMatterContent) {
            content = ((YamlFrontMatterContent) customNode).getContent();
            present = true;
        } else {
            super.visit(customNode);
        }
    }

    /**
     * Returns the YAML front matter metadata, if the Markdown document has
     * the YAML front matter and the extension uses {@link YamlDataExtractor}
     * (default).
     *
     * @return The data stored in YAML front matter or empty map
     */
    public Map<String, List<String>> getData() {
        return data;
    }

    /**
     * Returns the YAML Front Matter metadata as a string, if the Markdown
     * document has the YAML Front Matter and the extension uses
     * {@link YamlContentExtractor} (default).
     *
     * @return The content of YAML front matter as string or empty string.
     */
    public String getContent() {
        return content;
    }

    public boolean isFrontMatterPresent() {
        return present;
    }
}
