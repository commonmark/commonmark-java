package org.commonmark.node;

public interface LinkFormat {
    public String getDestination();
    public void setDestination(String destination);
    public String getRawDestination();
    public void setRawDestination(String destination);
    public String getLabel();
    public void setLabel(String label);
    public String getRawLabel();
    public void setRawLabel(String label);
    public String getTitle();
    public void setTitle(String title);
    public String getRawTitle();
    public void setRawTitle(String title);
    public char getTitleSymbol();
    public void setTitleSymbol(char symbol);
    public LinkType getLinkType();
    public void setLinkType(LinkType linkType);
    public String getWhitespacePreDestination();
    public void setWhitespacePreDestination(String whitespace);
    public String getWhitespacePreTitle();
    public void setWhitespacePreTitle(String whitespace);
    public String getWhitespacePostContent();
    public void setWhitespacePostContent(String whitespace);
    
    public enum LinkType {
        INLINE, REFERENCE, AUTOLINK, NULL;
    }
}
