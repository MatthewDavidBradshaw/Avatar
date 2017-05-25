package com.matthewtamlin.avatar.element_supplier.id_based_element_supplier;

import com.google.common.collect.ImmutableSet;
import com.google.testing.compile.JavaFileObjects;
import com.matthewtamlin.avatar.element_supplier.IdBasedElementSupplier;
import com.matthewtamlin.avatar.element_supplier.UniqueElementNotFoundException;
import org.junit.Before;
import org.junit.Test;

import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Set;

import static com.matthewtamlin.avatar.element_supplier.testing_utils.ElementNameUtil.getElementNames;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for the {@link IdBasedElementSupplier} class.
 */
public class TestIdBasedElementSupplier {
	private static final Set<String> ID1_NAMES = ImmutableSet.of("field1");
	
	private static final Set<String> ID2_NAMES = ImmutableSet.of("method1", "method2");
	
	private static final File EMPTY_FILE = new File("src/test/java/com/matthewtamlin/avatar/" +
			"element_supplier/id_based_element_supplier/EmptySourceFile.java");
	
	private static final File NORMAL_FILE = new File("src/test/java/com/matthewtamlin/avatar/" +
			"element_supplier/id_based_element_supplier/NormalSourceFile.java");
	
	private JavaFileObject emptyJavaFileObject;
	
	private JavaFileObject normalJavaFileObject;
	
	@Before
	public void setup() throws MalformedURLException {
		assertThat("Empty file does not exist.", EMPTY_FILE.exists(), is(true));
		assertThat("Normal file does not exist.", NORMAL_FILE.exists(), is(true));
		
		emptyJavaFileObject = JavaFileObjects.forResource(EMPTY_FILE.toURI().toURL());
		normalJavaFileObject = JavaFileObjects.forResource(NORMAL_FILE.toURI().toURL());
		
		assertThat("Empty java file object is null.", emptyJavaFileObject, is(notNullValue()));
		assertThat("Normal java file object is null.", normalJavaFileObject, is(notNullValue()));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_nullFile() {
		new IdBasedElementSupplier(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetElementsWithId_nullAnnotation() {
		final IdBasedElementSupplier supplier = new IdBasedElementSupplier(normalJavaFileObject);
		
		supplier.getElementsWithId(null);
	}
	
	@Test
	public void testGetElementsWithId_idNotFound() {
		final IdBasedElementSupplier supplier = new IdBasedElementSupplier(normalJavaFileObject);
		
		final Set<Element> elements = supplier.getElementsWithId("0");
		
		assertThat("Returned set should never be null.", elements, is(notNullValue()));
		assertThat("Returned set should be empty.", elements.isEmpty(), is(true));
	}
	
	@Test
	public void testGetElementsWithAnnotation_elementIsFoundOnce() {
		final IdBasedElementSupplier supplier = new IdBasedElementSupplier(normalJavaFileObject);
		
		final Set<Element> elements = supplier.getElementsWithId("1");
		
		assertThat("Returned set should never be null.", elements, is(notNullValue()));
		assertThat("Returned set did not match expected set.", getElementNames(elements), is(ID1_NAMES));
	}
	
	@Test
	public void testGetElementsWithAnnotation_elementIsFoundMultipleTimes() {
		final IdBasedElementSupplier supplier = new IdBasedElementSupplier(normalJavaFileObject);
		
		final Set<Element> elements = supplier.getElementsWithId("2");
		
		assertThat("Returned set should never be null.", elements, is(notNullValue()));
		assertThat("Returned set did not match expected set.", getElementNames(elements), is(ID2_NAMES));
	}
	
	@Test
	public void testGetElementsWithAnnotation_fileIsEmpty() {
		final IdBasedElementSupplier supplier = new IdBasedElementSupplier(emptyJavaFileObject);
		
		final Set<Element> elements = supplier.getElementsWithId("1");
		
		assertThat("Returned set should never be null.", elements, is(notNullValue()));
		assertThat("Returned set should be empty.", elements.isEmpty(), is(true));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetUniqueElementWithId_nullId() {
		final IdBasedElementSupplier supplier = new IdBasedElementSupplier(normalJavaFileObject);
		
		supplier.getUniqueElementWithId(null);
	}
	
	@Test(expected = UniqueElementNotFoundException.class)
	public void testGetUniqueElementWithId_idNotFound() {
		final IdBasedElementSupplier supplier = new IdBasedElementSupplier(normalJavaFileObject);
		
		supplier.getUniqueElementWithId("0");
	}
	
	@Test
	public void testGetUniqueElementWithId_idFoundOnce() {
		final IdBasedElementSupplier supplier = new IdBasedElementSupplier(normalJavaFileObject);
		
		final Element element = supplier.getUniqueElementWithId("1");
		
		final String expectedElementName = ID1_NAMES.iterator().next();
		final String actualElementName = element.getSimpleName().toString();
		
		assertThat("Returned element should never be null.", element, is(notNullValue()));
		assertThat("Returned element did not match expected element.", actualElementName, is(expectedElementName));
	}
	
	@Test(expected = UniqueElementNotFoundException.class)
	public void testGetUniqueElementWithId_elementFoundTwice() {
		final IdBasedElementSupplier supplier = new IdBasedElementSupplier(normalJavaFileObject);
		
		supplier.getUniqueElementWithId("2");
	}
}