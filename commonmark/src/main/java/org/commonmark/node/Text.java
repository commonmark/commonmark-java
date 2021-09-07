package org.commonmark.node;

import java.util.Objects;

public class Text extends Node {

    private String literal;
    private String raw = "";
    private String preContentWhitespace;
    private String postContentWhitespace;

    public Text() {
    }

    public Text(String literal) {
        this.literal = literal;
        this.raw = literal;
        this.preContentWhitespace = "";
        this.postContentWhitespace = "";
    }
    
    public Text(String literal, String raw, String preContentWhitespace, String postContentWhitespace) {
        this.literal = literal;
        this.raw = raw;
        this.preContentWhitespace = preContentWhitespace;
        this.postContentWhitespace = postContentWhitespace;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }
    
    public String getRaw() {
        return raw;
    }
    
    public void setRaw(String raw) {
        this.raw = raw;
    }
    
    public String whitespacePreContent() {
        if(preContentWhitespace != null) {
            return preContentWhitespace;
        }else {
            return "";
        }
    }
    
    public String whitespacePostContent() {
        if(postContentWhitespace != null) {
            return postContentWhitespace;
        }else {
            return "";
        }
    }
    
    public void setWhitespace(String pre, String post) {
        preContentWhitespace = pre;
        postContentWhitespace = post;
    }

    @Override
    protected String toStringAttributes() {
        return "literal=" + literal;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Text) {
            if(((Text)obj).getLiteral().equals(literal) && ((Text)obj).getRaw().equals(raw) &&
                    ((Text)obj).whitespacePreContent().equals(preContentWhitespace) &&
                    ((Text)obj).whitespacePostContent().equals(postContentWhitespace)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(literal, raw, preContentWhitespace, postContentWhitespace);
    }
}
