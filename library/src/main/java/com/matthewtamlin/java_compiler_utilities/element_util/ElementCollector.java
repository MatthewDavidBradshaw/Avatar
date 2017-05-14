package com.matthewtamlin.java_compiler_utilities.element_util;

import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.element.Element;
import java.util.Collection;

/**
 * An annotation processor which collects elements as it processes them. The implementation determines which elements
 * are retained if any.
 */
public abstract class ElementCollector<T extends Collection<? extends Element>> extends AbstractProcessor {
	/**
	 * Gets the elements which have been collected thus far. This method will not return meaningful data unless
	 * processing is complete. This method might return an empty set but it will never return null.
	 *
	 * @return the elements collected by this collector, not null
	 */
	public abstract T getCollectedElements();
}