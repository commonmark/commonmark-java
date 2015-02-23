package com.atlassian.rstocker.cm;

public class ListBlock extends Block {

	private final ListType type;
	private final char orderedDelimiter;
	private final int orderedStart;
	private final char bulletMarker;
	private boolean tight;

	public enum ListType {
		BULLET,
		ORDERED
	}

	// TODO: Split into two classes?
	public ListBlock(SourcePos sourcePos, ListType type, char orderedDelimiter, int orderedStart, char bulletMarker) {
		super(Type.List, sourcePos);
		this.type = type;
		this.orderedDelimiter = orderedDelimiter;
		this.orderedStart = orderedStart;
		this.bulletMarker = bulletMarker;
	}

	public ListType getListType() {
		return type;
	}

	public char getOrderedDelimiter() {
		return orderedDelimiter;
	}

	public int getOrderedStart() {
		return orderedStart;
	}

	public char getBulletMarker() {
		return bulletMarker;
	}

	public void setTight(boolean tight) {
		this.tight = tight;
	}

	public boolean isTight() {
		return tight;
	}

}
