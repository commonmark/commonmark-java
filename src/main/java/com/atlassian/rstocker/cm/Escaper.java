package com.atlassian.rstocker.cm;

@FunctionalInterface
public interface Escaper {
	String escape(String input, boolean preserveEntities);
}