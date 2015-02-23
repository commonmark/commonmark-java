package com.atlassian.rstocker.cm;

public class ListData {

	ListBlock.ListType type;
	boolean tight = true;
	char bulletChar;
	int start; // null
	char delimiter;

	int markerOffset;
	int padding = 0; // null

	public ListData(int indent) {
		this.markerOffset = indent;
	}

}
