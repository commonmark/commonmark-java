package com.atlassian.rstocker.cm.nodes;

public abstract class AbstractVisitor implements Visitor {

	protected void visitChildren(Node node) {
		Node child = node.getFirstChild();
		while (child != null) {
			child.accept(this);
			child = child.getNext();
		}
	}

	@Override
	public void visit(BlockQuote blockQuote) {
		visitChildren(blockQuote);
	}

	@Override
	public void visit(Code code) {
		visitChildren(code);
	}

	@Override
	public void visit(CodeBlock codeBlock) {
		visitChildren(codeBlock);
	}

	@Override
	public void visit(Document document) {
		visitChildren(document);
	}

	@Override
	public void visit(Emphasis emphasis) {
		visitChildren(emphasis);
	}

	@Override
	public void visit(HardLineBreak hardLineBreak) {
		visitChildren(hardLineBreak);
	}

	@Override
	public void visit(Header header) {
		visitChildren(header);
	}

	@Override
	public void visit(HorizontalRule horizontalRule) {
		visitChildren(horizontalRule);
	}

	@Override
	public void visit(Html html) {
		visitChildren(html);
	}

	@Override
	public void visit(HtmlBlock htmlBlock) {
		visitChildren(htmlBlock);
	}

	@Override
	public void visit(Image image) {
		visitChildren(image);
	}

	@Override
	public void visit(Link link) {
		visitChildren(link);
	}

	@Override
	public void visit(ListBlock listBlock) {
		visitChildren(listBlock);
	}

	@Override
	public void visit(ListItem listItem) {
		visitChildren(listItem);
	}

	@Override
	public void visit(Paragraph paragraph) {
		visitChildren(paragraph);
	}

	@Override
	public void visit(SoftLineBreak softLineBreak) {
		visitChildren(softLineBreak);
	}

	@Override
	public void visit(StrongEmphasis strongEmphasis) {
		visitChildren(strongEmphasis);
	}

	@Override
	public void visit(Text text) {
		visitChildren(text);
	}
}
