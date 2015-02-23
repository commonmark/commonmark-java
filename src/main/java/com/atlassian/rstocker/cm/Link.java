package com.atlassian.rstocker.cm;

public class Link extends Node {

	private final String destination;
	private final String title;

	public Link(String destination, String title) {
		super(Type.Link);
		this.destination = destination;
		this.title = title;
	}

	public String getDestination() {
		return destination;
	}

	public String getTitle() {
		return title;
	}
}
