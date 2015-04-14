package org.commonmark;

@FunctionalInterface
public interface Escaper {
	String escape(String input, boolean preserveEntities);
}
