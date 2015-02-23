package com.atlassian.rstocker.cm;

public class Image extends Node {

	private final String destination;
	private final String title;

	public Image(String destination, String title) {
		super(Type.Image);
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
