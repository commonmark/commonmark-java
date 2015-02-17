package com.atlassian.rstocker.cm;

public class ListData {

	int markerOffset;
	String type = null;
	boolean tight = true;
	char bulletChar;
	int start; // null
	String delimiter;
	int padding = 0; // null

	public ListData(int indent) {
		this.markerOffset = indent;
	}

}
