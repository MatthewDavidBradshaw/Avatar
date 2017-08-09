package com.matthewtamlin.avatar.compilation;

import javax.tools.JavaCompiler;

/**
 * Exception which indicates the absence of a {@link JavaCompiler} at runtime.
 */
public class CompilerMissingException extends RuntimeException {
	public CompilerMissingException() {
		super();
	}
	
	public CompilerMissingException(final String message) {
		super(message);
	}
	
	public CompilerMissingException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public CompilerMissingException(final Throwable cause) {
		super(cause);
	}
}