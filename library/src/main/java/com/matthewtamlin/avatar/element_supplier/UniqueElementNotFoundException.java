package com.matthewtamlin.avatar.element_supplier;

/**
 * Exception which indicates that a unique element for some criteria could not be found when searching a source file.
 *
 * @deprecated use {@link com.matthewtamlin.avatar.rules.UniqueElementNotFoundException}
 */
@Deprecated
public class UniqueElementNotFoundException extends RuntimeException {
	public UniqueElementNotFoundException() {
		super();
	}
	
	public UniqueElementNotFoundException(final String message) {
		super(message);
	}
	
	public UniqueElementNotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public UniqueElementNotFoundException(final Throwable cause) {
		super(cause);
	}
}