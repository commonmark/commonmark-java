package org.commonmark.renderer.roundtrip;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.commonmark.internal.renderer.text.BulletListHolder;
import org.commonmark.internal.renderer.text.ListHolder;
import org.commonmark.internal.renderer.text.OrderedListHolder;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlankLine;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.LinkFormat.LinkType;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.ListBlock;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.renderer.NodeRenderer;

/**
 * The node renderer that renders all the core nodes (comes last in the order of node renderers).
 */
public class CoreCommonMarkNodeRenderer extends AbstractVisitor implements NodeRenderer {

    protected final CommonMarkNodeRendererContext context;
    private final CommonMarkWriter textContent;

    private ListHolder listHolder;
    
    public CoreCommonMarkNodeRenderer(CommonMarkNodeRendererContext context) {
        this.context = context;
        this.textContent = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return new HashSet<>(Arrays.asList(
                Document.class,
                Heading.class,
                Paragraph.class,
                BlockQuote.class,
                BulletList.class,
                FencedCodeBlock.class,
                HtmlBlock.class,
                ThematicBreak.class,
                IndentedCodeBlock.class,
                Link.class,
                LinkReferenceDefinition.class,
                ListBlock.class,
                ListItem.class,
                OrderedList.class,
                Image.class,
                Emphasis.class,
                StrongEmphasis.class,
                Text.class,
                Code.class,
                HtmlInline.class,
                SoftLineBreak.class,
                HardLineBreak.class,
                BlankLine.class
        ));
    }

    @Override
    public void render(Node node) {
        node.accept(this);
    }

    @Override
    public void visit(Document document) {
        // No rendering itself (aside from post-block whitespace, if present)
    	
        visitChildren(document);

        textContent.write(document.whitespacePostBlock());
    }

    public void visit(BlockQuote blockQuote) {
    	if(blockQuote.getFirstChild() instanceof Heading ||
    			blockQuote.getFirstChild() instanceof IndentedCodeBlock ||
    			blockQuote.getFirstChild() instanceof FencedCodeBlock) {
	    	textContent.write(blockQuote.whitespacePreBlock());
	    	textContent.write(">");
	    	textContent.write(blockQuote.whitespacePreContent());
    	}
    	
        visitChildren(blockQuote);
        
        writeEndOfLineIfNeeded(blockQuote, null);
    }

    @Override
    public void visit(BulletList bulletList) {
        listHolder = new BulletListHolder(listHolder, bulletList);
        visitChildren(bulletList);
        writeEndOfLineIfNeeded(bulletList, null);

        if (listHolder.getParent() != null) {
            listHolder = listHolder.getParent();
        } else {
            listHolder = null;
        }
    }

    public void visit(Code code) {
        for(int i = 0; i < code.getNumBackticks(); i++) {
            textContent.write('`');
        }
        
        textContent.write(code.getRaw());
        
        for(int i = 0; i < code.getNumBackticks(); i++) {
            textContent.write('`');
        }
    }
    
    @Override
    public void visit(Emphasis emphasis) {
    	textContent.write(emphasis.whitespacePreBlock());
        textContent.write(emphasis.getOpeningDelimiter());
        visitChildren(emphasis);
        textContent.write(emphasis.getClosingDelimiter());
    }

    public void visit(FencedCodeBlock fencedCodeBlock) {
        for(int i = 0; i < fencedCodeBlock.getStartFenceIndent(); i++) {
            textContent.write(" ");
        }
        
        for(int i = 0; i < fencedCodeBlock.getStartFenceLength(); i++) {
            textContent.write(fencedCodeBlock.getFenceChar());
        }
        
        if(fencedCodeBlock.getInfo() != null && !fencedCodeBlock.getInfo().isEmpty()) {
            textContent.write(fencedCodeBlock.getInfo());
        }
        
        // CommonMark test case #96 shows that content with no ending fence and
        //    completely blank content should not trigger a new line
        if(fencedCodeBlock.getEndFenceLength() > 0 || !fencedCodeBlock.getRaw().isEmpty()) {
        	textContent.line();
        }
        
        textContent.write(fencedCodeBlock.getRaw());
        
        if(fencedCodeBlock.getEndFenceLength() > 0 && !fencedCodeBlock.getRaw().isEmpty()) {
        	textContent.line();
        }
        
        for(int i = 0; i < fencedCodeBlock.getEndFenceIndent(); i++) {
            textContent.write(" ");
        }
        
        for(int i = 0; i < fencedCodeBlock.getEndFenceLength(); i++) {
            textContent.write(fencedCodeBlock.getFenceChar());
        }
        
        if(fencedCodeBlock.getEndFenceLength() > 0) {
            textContent.write(fencedCodeBlock.whitespacePostBlock());
        }
        
        writeEndOfLineIfNeeded(fencedCodeBlock, null);
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
    	if(hardLineBreak.hasBackslash()) {
    		textContent.write("\\");
    	}
    	
        writeEndOfLine();
    }

    public void visit(Heading heading) {
        
        if(heading.getSymbolType() == '#') {
        	textContent.write(heading.whitespacePreBlock());
            
            for(int i = 0; i < heading.getLevel(); i++) {
                textContent.write(heading.getSymbolType());
            }
            
            textContent.write(heading.whitespacePreContent());
            
            visitChildren(heading);
            
            textContent.write(heading.whitespacePostContent());
            
            if(heading.getNumEndingSymbol() > 0) {
                for(int i = 0; i < heading.getNumEndingSymbol(); i++) {
                    textContent.write(heading.getSymbolType());
                }
            }
            
            textContent.write(heading.whitespacePostBlock());
            
            writeEndOfLineIfNeeded(heading, null);
            
        }else {
        	textContent.write(heading.whitespacePreBlock());
        	
        	visitChildren(heading);
        	
        	textContent.write(heading.whitespacePreContent());
        	
        	writeEndOfLine();

        	textContent.write(heading.whitespacePostContent());
        	
            if(heading.getNumEndingSymbol() > 0) {
            	for(int i = 0; i < heading.getNumEndingSymbol(); i++) {
            		textContent.write(heading.getSymbolType());
            	}
            }
            
           	textContent.write(heading.whitespacePostBlock());
            
            writeEndOfLineIfNeeded(heading, null);
        }
    }

    public void visit(ThematicBreak thematicBreak) {
    	if(thematicBreak.getContent() != null) {
    		textContent.write(thematicBreak.getContent().toString());
    	}
    	
        writeEndOfLineIfNeeded(thematicBreak, null);
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        textContent.write(htmlInline.getLiteral());
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
    	textContent.write(htmlBlock.getRaw());
    	writeEndOfLineIfNeeded(htmlBlock, null);
    }

    @Override
    public void visit(Image image) {
        writeLink(image, image.getTitle(), image.getDestination());
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
   		textContent.write(indentedCodeBlock.whitespacePreContent());
    	
    	textContent.write(indentedCodeBlock.getRaw());
    	
    	writeEndOfLineIfNeeded(indentedCodeBlock, null);
    }

    public void visit(Link link) {
        writeLink(link, link.getTitle(), link.getDestination());
    }
    
    @Override
    public void visit(LinkReferenceDefinition linkDef) {
        writeLinkReference(linkDef);
        writeEndOfLineIfNeeded(linkDef, null);
    }

    @Override
    public void visit(ListItem listItem) {
        if (listHolder != null && listHolder instanceof OrderedListHolder) {
            OrderedListHolder orderedListHolder = (OrderedListHolder) listHolder;
            
            textContent.write(listItem.whitespacePreBlock());
            
            if(!(listItem.getFirstChild() instanceof ThematicBreak) &&
            		!(listItem.getFirstChild() instanceof HtmlBlock) &&
            		!(listItem.getParent().getParent() instanceof BlockQuote) &&
            		!(listItem.getFirstChild() instanceof BlankLine)) {
            	textContent.write(listItem.getRawNumber() + "");
            	textContent.write(orderedListHolder.getDelimiter());
            	textContent.write(listItem.whitespacePreContent());
            }
            
            visitChildren(listItem);
            
            textContent.write(listItem.whitespacePostBlock());
            
            writeEndOfLineIfNeeded(listItem, null);
        } else if (listHolder != null && listHolder instanceof BulletListHolder) {
            BulletListHolder bulletListHolder = (BulletListHolder) listHolder;
            
            textContent.write(listItem.whitespacePreBlock());
            
            if(!(listItem.getFirstChild() instanceof ThematicBreak) &&
            		!(listItem.getFirstChild() instanceof HtmlBlock) &&
            		!(listItem.getParent().getParent() instanceof BlockQuote) &&
            		!(listItem.getFirstChild() instanceof BlankLine)) {
            	textContent.write(bulletListHolder.getMarker());
            	textContent.write(listItem.whitespacePreContent());
            }
            visitChildren(listItem);
            
            textContent.write(listItem.whitespacePostBlock());
            
            writeEndOfLineIfNeeded(listItem, null);
        }
        
    }

    @Override
    public void visit(OrderedList orderedList) {
        listHolder = new OrderedListHolder(listHolder, orderedList);
        visitChildren(orderedList);
        writeEndOfLineIfNeeded(orderedList, null);

        if (listHolder.getParent() != null) {
            listHolder = listHolder.getParent();
        } else {
            listHolder = null;
        }
        
    }

    public void visit(Paragraph paragraph) {
        visitChildren(paragraph);
        
        writeEndOfLineIfNeeded(paragraph, null);

        textContent.write(paragraph.whitespacePostBlock());
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
    	// Within roundtrip rendering, a soft break is always a newline of some kind
        writeEndOfLine();
    }
    
    @Override
    public void visit(StrongEmphasis strongEmphasis) {
    	textContent.write(strongEmphasis.whitespacePreBlock());
        textContent.write(strongEmphasis.getOpeningDelimiter());
        visitChildren(strongEmphasis);
        textContent.write(strongEmphasis.getClosingDelimiter());
    }

    @Override
    public void visit(Text text) {
    	if(!text.getRaw().isEmpty()) {
			textContent.write(text.whitespacePreContent() + text.getRaw() + text.whitespacePostContent());
		}else {
			textContent.write(text.whitespacePreContent() + text.getLiteral() + text.whitespacePostContent());
		}
    }
    
    @Override
	public void visit(BlankLine blankLine) {
		System.out.println(blankLine.getRaw());
		textContent.write(blankLine.getRaw());
		writeEndOfLineIfNeeded(blankLine, null);
	}

	@Override
    protected void visitChildren(Node parent) {
        Node node = parent.getFirstChild();
        
        while (node != null) {
            Node next = node.getNext();
            
            context.render(node);
            node = next;
        }
    }

    private void writeLink(Node node, String title, String destination) {
        if(node instanceof Link) {
            writeLink((Link)node);
        }
        
        if(node instanceof Image) {
            writeImage((Image)node);
        }
    }
    
    private void writeLink(Link node) {
        // Autolink
        if(node.getLinkType() == LinkType.AUTOLINK) {
            textContent.write("<");
            visitChildren(node);
            textContent.write(">");
        }else if(node.getLinkType() == LinkType.REFERENCE) {
        	textContent.write("[");
            visitChildren(node);
            textContent.write("]");
            
            if(node.getLabel() != null) {
	            textContent.write("[");
	            if(node.getRawLabel() != null && !node.getRawLabel().isEmpty()) {
	            	textContent.write(node.getRawLabel());
	            }else {
	            	textContent.write(node.getLabel());
	            }
	            textContent.write("]");
            }
        }else {
            textContent.write("[");
            visitChildren(node);
            textContent.write("]");
            
            textContent.write("(");
            
            if(!node.getRawDestination().isEmpty()) {
            	textContent.write(node.getWhitespacePreDestination());
            	textContent.write(node.getRawDestination());
            }else if(node.getDestination() != null) {
                textContent.write(node.getWhitespacePreDestination());
                textContent.write(node.getDestination());
            }else {
                visitChildren(node);
            }
            
            if(!node.getRawTitle().isEmpty()) {
            	textContent.write(node.getWhitespacePreTitle());
            	textContent.write(node.getRawTitle());
            }else if(node.getTitle() != null) {
                textContent.write(node.getWhitespacePreTitle());
                textContent.write(node.getTitleSymbol() + "");
                
                textContent.write(node.getTitle());
                
                if(node.getTitleSymbol() == '(') {
                	textContent.write(")");
                }else {
                	textContent.write(node.getTitleSymbol());
                }
            }
            
            textContent.write(node.getWhitespacePostContent());
            textContent.write(")");
        }
    }
    
    private void writeLinkReference(LinkReferenceDefinition node) {
    	textContent.write(node.whitespacePreLabel());
    	
        textContent.write("[");
        textContent.write(node.getLabel());
        textContent.write("]:");
        
        if(node.getDestination() != null) {
        	textContent.write(node.whitespacePreDestination());
        	if(!node.getRawDestination().isEmpty()) {
        		textContent.write(node.getRawDestination());
        	}else {
        		textContent.write(node.getDestination());
        	}
            
            if(node.getTitle() != null) {
            	textContent.write(node.whitespacePreTitle());
            	
            	char delimiter = node.getDelimiterChar();
            	
            	if(delimiter == Character.MIN_VALUE) {
            		delimiter = '"';
            	}
            	
            	textContent.write(delimiter);
            	
            	if(!node.getRawTitle().isEmpty()) {
            		textContent.write(node.getRawTitle());
            	}else {
            		textContent.write(node.getTitle());
            	}
            	
            	textContent.write(delimiter);
            }
        }
        
        textContent.write(node.whitespacePostTitle());
    }
    
    private void writeImage(Image node) {
        // Autolink
        if(node.getLinkType() == LinkType.AUTOLINK) {
            textContent.write("<");
            visitChildren(node);
            textContent.write(">");
        }else if(node.getLinkType() == LinkType.REFERENCE) {
        	textContent.write("![");
            visitChildren(node);
            textContent.write("]");
            
            if(node.getLabel() != null) {
	            textContent.write("[");
	            if(node.getRawLabel() != null && !node.getRawLabel().isEmpty()) {
	            	textContent.write(node.getRawLabel());
	            }else {
	            	textContent.write(node.getLabel());
	            }
	            textContent.write("]");
            }
        }else {
            textContent.write("![");
            visitChildren(node);
            textContent.write("]");
            
            textContent.write("(");
            
            if(!node.getRawDestination().isEmpty()) {
            	textContent.write(node.getWhitespacePreDestination());
            	textContent.write(node.getRawDestination());
            }else if(node.getDestination() != null) {
                textContent.write(node.getWhitespacePreDestination());
                textContent.write(node.getDestination());
            }else {
                visitChildren(node);
            }
            
            if(!node.getRawTitle().isEmpty()) {
            	textContent.write(node.getWhitespacePreTitle());
            	textContent.write(node.getRawTitle());
            }else if(node.getTitle() != null) {
                textContent.write(node.getWhitespacePreTitle());
                textContent.write(node.getTitleSymbol() + "");
                
                textContent.write(node.getTitle());
                
                if(node.getTitleSymbol() == '(') {
                	textContent.write(")");
                }else {
                	textContent.write(node.getTitleSymbol());
                }
            }
            
            textContent.write(node.getWhitespacePostContent());
            textContent.write(")");
        }
    }

    private void writeEndOfLineIfNeeded(Node node, Character c) {
        if (node.getNext() != null) {
        	if(node instanceof ListItem) {
        		// If a list item has post-block whitespace, allow it to handle whitespace
        		if(((ListItem)node).whitespacePostBlock().isEmpty()) {
        			textContent.line();
        		}
        	}else if(node instanceof Paragraph) {
        		// If a paragraph has post-block whitespace, allow it to handle whitespace
        		if(((Paragraph)node).whitespacePostBlock().isEmpty()) {
        			textContent.line();
        		}
        	}else {
        		textContent.line();
        	}
        }
    }

    private void writeEndOfLine() {
        textContent.line();
    }
}
