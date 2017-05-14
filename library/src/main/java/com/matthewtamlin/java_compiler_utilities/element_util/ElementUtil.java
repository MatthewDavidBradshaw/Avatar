package com.matthewtamlin.java_compiler_utilities.element_util;

import com.google.common.collect.ImmutableSet;
import com.matthewtamlin.java_utilities.testing.Tested;
import in_memory_file_utils.InMemoryJavaFileManager;

import javax.lang.model.element.Element;
import javax.tools.*;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Set;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Creates hierarchical Java element models from Java file objects.
 */
@Tested(testMethod = "automated")
public class ElementUtil {
	/**
	 * The exception to throw if there is no Java compiler available.
	 */
	private static final CompilerMissingException NO_COMPILER_EXCEPTION = new CompilerMissingException(
			"Cannot get elements if there is no Java compiler available at runtime.");
	
	/**
	 * Returns all root elements in the supplied Java file. A full element model is generated to mirror the
	 * conditions in an annotation processor, and can be explored via successive method calls on the elements. This
	 * method might return an empty set but it will never return null.
	 *
	 * @param javaFileObject
	 * 		the Java file object to get the elements from, not null
	 *
	 * @return the root elements found in the supplied file, not null, unmodifiable
	 *
	 * @throws CompilerMissingException
	 * 		if no Java compiler is found at runtime
	 * @throws IllegalArgumentException
	 * 		if {@code javaFileObject} is null
	 */
	public static Set<Element> getRootElementsFrom(final JavaFileObject javaFileObject) throws
			CompilerMissingException {
		
		checkNotNull(javaFileObject, "Argument \'source\' cannot be null.");
		
		return ImmutableSet.copyOf(getElementsUsingCollector(javaFileObject, new RootElementCollector()));
	}
	
	/**
	 * Returns all elements in the supplied Java file object which have at least one of the supplied tags. A full
	 * element model is generated to mirror the conditions in an annotation processor, and can be explored via
	 * successive method calls on the elements. This method might return an empty set but it will never return null.
	 *
	 * @param javaFileObject
	 * 		the Java file object to get the elements from, not null
	 * @param tags
	 * 		the tags to use when searching for elements, not null
	 *
	 * @return all elements found in the file object which have at least one of the supplied tags, not null,
	 * unmodifiable
	 *
	 * @throws CompilerMissingException
	 * 		if no Java compiler is found at runtime
	 * @throws IllegalArgumentException
	 * 		if {@code javaFileObject} is null
	 * @throws IllegalArgumentException
	 * 		if {@code tags} is null
	 */
	public static Set<Element> getTaggedElementsFrom(
			final JavaFileObject javaFileObject,
			final Set<Class<? extends Annotation>> tags) throws CompilerMissingException {
		
		checkNotNull(javaFileObject, "Argument \'source\' cannot be null.");
		checkNotNull(tags, "Argument \'tags\' cannot be null.");
		
		return ImmutableSet.copyOf(getElementsUsingCollector(javaFileObject, new TaggedElementCollector(tags)));
	}
	
	/**
	 * Compiles the supplied Java file object using the system Java compiler and the supplied collector, and returns
	 * the collected elements. This method might return an empty set but it will never return null.
	 *
	 * @param javaFileObject
	 * 		the java file object to compile, not null
	 * @param collector
	 * 		the collector to use when compiling, not null
	 *
	 * @return the elements collected by the supplied collector, not null
	 *
	 * @throws CompilerMissingException
	 * 		if no Java compiler is found at runtime
	 * @throws IllegalArgumentException
	 * 		if {@code javaFileObject} is null
	 * @throws IllegalArgumentException
	 * 		if {@code collector} is null
	 */
	private static <T> T getElementsUsingCollector(
			final JavaFileObject javaFileObject,
			final ElementCollector<T> collector)
			throws CompilerMissingException {
		
		checkNotNull(javaFileObject, "Argument \'javaFileObject\' cannot be null.");
		checkNotNull(javaFileObject, "Argument \'collector\' cannot be null.");
		
		final JavaCompiler compiler = checkNotNull(ToolProvider.getSystemJavaCompiler(), NO_COMPILER_EXCEPTION);
		final DiagnosticCollector<JavaFileObject> diagnostic = new DiagnosticCollector<>();
		
		final JavaFileManager fileManager = compiler.getStandardFileManager(diagnostic, Locale.getDefault(), UTF_8);
		final JavaFileManager inMemoryFileManager = new InMemoryJavaFileManager(fileManager);
		
		final JavaCompiler.CompilationTask task = compiler.getTask(
				null,
				inMemoryFileManager,
				diagnostic,
				null,
				null,
				ImmutableSet.of(javaFileObject));
		
		task.setProcessors(ImmutableSet.of(collector));
		task.call();
		
		return collector.getCollectedElements();
	}
}