package com.matthewtamlin.avatar.rules;

/**
 * Defines an ID for an element in a source file. There is no requirement for IDs to be unique.
 */
public @interface ElementId {
	/**
	 * @return the ID for the annotated element, not necessary unique
	 */
	String value();
}