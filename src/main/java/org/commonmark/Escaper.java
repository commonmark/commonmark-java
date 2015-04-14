package org.commonmark;

// TODO: Should probably work with Appendable/StringBuilder
public interface Escaper {
	String escape(String input, boolean preserveEntities);
}
