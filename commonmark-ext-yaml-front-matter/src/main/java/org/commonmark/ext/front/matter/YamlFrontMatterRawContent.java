package org.commonmark.ext.front.matter;

import org.commonmark.node.CustomNode;

public class YamlFrontMatterRawContent extends CustomNode {
    private String content;

    public YamlFrontMatterRawContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
