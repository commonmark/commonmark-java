package com.atlassian.rstocker.cm.nodes;

public interface Visitor {

	void visit(BlockQuote blockQuote);

	void visit(Code code);

	void visit(CodeBlock codeBlock);

	void visit(Document document);

	void visit(Emphasis emphasis);

	void visit(HardLineBreak hardLineBreak);

	void visit(Header header);

	void visit(HorizontalRule horizontalRule);

	void visit(Html html);

	void visit(HtmlBlock htmlBlock);

	void visit(Image image);

	void visit(Link link);

	void visit(ListBlock listBlock);

	void visit(ListItem listItem);

	void visit(Paragraph paragraph);

	void visit(SoftLineBreak softLineBreak);

	void visit(StrongEmphasis strongEmphasis);

	void visit(Text text);
}
