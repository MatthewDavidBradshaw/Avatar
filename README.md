# JavaCompilerUtilities
While testing my [Spyglass](https://github.com/MatthewTamlin/Spyglass) library, I was frustrated by how difficult it is to write unit test for classes which use `javax.tools.Element` objects. There's no way to easily instantiate elements directly, and the usual mocking frameworks produce interconnected networks of unmaintainable mess. Andrew Phillips has done a great job of articulating the problem in his [blog post](http://blog.xebia.com/testing-annotation-processors/) so I wont go into further detail, other that to say there needs to be an easy way to create elements for unit tests. 

This library solves the problem by providing a utility which directly converts source files to element models. This is the simplest way to create elements, because the developer only has to write normal source code and can ignore the complexities of the element API.

## Download
Releases are made available through jCentre. Add `compile 'com.matthew-tamlin:java-compiler-utilities:1.2.0'` to your gradle build file to use the latest version.

## Basic examples
These examples provide enough information to start using the library within minutes. For a more in depth example, see the End-to-end example section. The main class of this library is the ElementsUtil class. It contains methods which can be used to convert source files to element objects.

### Get a JavaFileObject for the source file
Consider the following source file defined in `src/main/java/com/matthewtamlin/example/MyClass.java`:
```java
public class MyClass {
    @SomeAnnotation
    public String method1() {
        return "example method";
    }
}

@SomeAnnotation
class MyOtherClass {
   @ElementId("some ID")
   private static final boolean field1 = true;
   
   @ElementId("some other ID")
   @SomeAnnotation
   private static final boolean field2 = false;
   
   @ElementId("some other ID")
   public String field3 = "example field";
}
```

The elements in this file are entirely made up, except for the `@ElementId` annotation which belongs to this library.

To get elements from this source file, we must first get a JavaFileObject to represent it. Creating JavaFileObjects is not a trivial task, but luckily Google's [compile testing](https://github.com/google/compile-testing) library has a great utility for this.
```java
File srcFile = new File("src/main/java/com/matthewtamlin/example/MyClass.java")
JavaFileObject srcFileObject = JavaFileObjects.forResource(SRC_FILE.toURI().toURL());
```

### Get all root elements
The ElementUtil class can be used to get all root elements in a file. For example:
```java
Set<Element> foundElements = ElementUtil.getRootElements(srcFileObject);

for (Element e : foundElements) {
    System.out.println("Found element " + e.getSimpleName().toString());
}
```
produces:
```
Found element MyClass
Found element MyOtherClass
```

### Get elements with a particular annotation
The ElementUtil class can be used to get all elements with a particular annotation. For example:
```java
Set<Element> foundElements = ElementUtil.getTaggedElements(srcFileObject, SomeAnnotation.class);

for (Element e : foundElements) {
    System.out.println("Found element " + e.getSimpleName().toString());
}
```
produces:
```
Found element MyOtherClass
Found element method1
Found element field2
```

### Get elements with a particular ID
The ElementUtil class can be used to get elements by ID, where the IDs are defined using the `@ElementId` annotation. This annotation can be applied to any source element, and uses String values for the IDs. For example:
```java
Set<Element> foundElements = ElementUtil.getElementsById(srcFileObject, "some other ID");

for (Element e : foundElements) {
    System.out.println("Found element " + e.getSimpleName().toString());
}
```
produces:
```
Found element field2
Found element field3
```

Additionally, the `ElementUtil.getUniqueElementById(JavaFileObject, String)` method can be used to get a single element by ID. This method expects to find only one element with the supplied ID, and will throw an exception if none or multiple are found.

## End-to-end example
Some context is necessary for a good example, so we will define a few source files and then some tests files. This example assumes you are familiar with the basic concepts of Java annotations, annotation processors and unit testing.

### Source files
Imagine we have an annotation which can be used to mark methods which return void. If the annotation is used in an annotation processor, we probably want to write a validator to make sure the annotation has actually been applied correctly.

The annotation is defined in `src/main/java/com/matthewtamlin/example/ReturnsNothing.java` as:
```java
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ReturnsNothing {}
```

The validator is defined in `src/main/java/com/matthewtamlin/example/Validator.java` as:
```java
public class Validator {
    public static void validate(Element element) throws ValidationException {
        if (element == null) {
            return;
        }
        
        if (!(element instanceof ExecutableElement)) {
            return;
        }
        
        if (element.getAnnotation(ReturnsNothing.class) == null) {
            return;
        }
        
        TypeMirror returnType = ((ExecutableElement) element).getReturnType();
        
        if (returnType instanceof NoType && ((NoType) returnType).equals(TypeKind.VOID) {
            // This is fine
        } else {
            throw new ValidationException("ReturnsNothing tag found with non-void return type");
        }
    }
}
```
### Test files
We're good developers, so we decide we want to write some unit tests to make sure the validation logic is working as expected. We want to make sure the validator obeys the following rules:
- Validation passes if the element is null
- Validation passes if the element is not an ExecutableElement
- Validation passes if the element is missing the annotation and returns void
- Validation passes if the element is missing the annotation and returns a primitive
- Validation passes if the element is missing the annotation and returns an object
- Validation passes if the element has the annotation and returns void
- Validation fails if the element has the annotation and returns a primitive
- Validation fails if the element has the annotation and returns an object

To test the validator, we need to pass it specific elements and compare the actual results to the expected outcomes. Getting mock elements which actually behave as real elements is hard, but luckily this library provides the ElementUtil class. This utility allows us to directly convert a source file to an element model, so that we can use real elements in our unit test. 

We define the source file we wish to convert in `src/test/java/com/matthewtamlin/example/TestValidatorData.java` as:
```java
public class TestValidatorData {
    @ElementId("void with annotation")
    @ReturnsNothing
    public void method1() {}
    
    @ElementId("void without annotation")
    public void method2() {}
    
    @ElementId("primitive with annotation")
    @ReturnsNothing
    public int method3() {}
    
    @ElementId("primitive without annotation")
    public int method4() {}
    
    @ElementId("object with annotation")
    @ReturnsNothing
    public Object method5() {}
    
    @ElementId("object without annotation")
    public Object method6() {}
}
```

The `@ElementId` annotations provide unique identifiers for the elements, so that the `ElementUtil.getUniqueElementById()` method can be used to get references to them. Using the utility class we define our test class in `src/test/java/com/matthewtamlin/example/TestValidator.java` as:
```java
@RunWith(JUnit4.class)
public class TestValidator {
    private static final File SRC_FILE = new File("src/test/java/com/matthewtamlin/example" +
            "/TestValidatorData.java");
    
    private JavaFileObject srcFileObject;
    
    @Before
    public void setUp() {
        assertThat("The source file cannot be found.", SRC_FILE.exists(), is(true));
        
        // The Google compile-testing library contains a great utility for creating JavaFileObjects
        srcFileObject = JavaFileObjects.forResource(SRC_FILE.toURI().toURL());
    }
    
    @Test
    public void testValidate_nullElement() {
        Validator.validate(null);
    }
    
    @Test
    public void testValidate_noAnnotationVoidReturn() {
        Element e = ElementUtil.getUniqueElementById(
                srcFileObject, 
                "void without annotation");
                
        Validator.validate(e);
    }
    
    @Test
    public void testValidate_noAnnotationPrimitiveReturn() {
        Element e = ElementUtil.getUniqueElementById(
                srcFileObject, 
                "primitive without annotation");
                
        Validator.validate(e);
    }
    
    @Test
    public void testValidate_noAnnotationObjectReturn() {
        Element e = ElementUtil.getUniqueElementById(
                srcFileObject, 
                "object without annotation");
                
        Validator.validate(e);
    }
    
    @Test
    public void testValidate_annotationPresentVoidReturn() {
        Element e = ElementUtil.getUniqueElementById(
                srcFileObject, 
                "void with annotation");
                
        Validator.validate(e);
    }
    
    @Test(expected = ValidationException.class)
    public void testValidate_annotationPresentPrimitiveReturn() {
        Element e = ElementUtil.getUniqueElementById(
                srcFileObject, 
                "primitive with annotation");
                
        Validator.validate(e);
    }
    
    @Test(expected = ValidationException.class)
    public void testValidate_annotationPresentObjectReturn() {
        Element e = ElementUtil.getUniqueElementById(
                srcFileObject, 
                "object with annotation");
                
        Validator.validate(e);
    }
}
```

This produces a standard test class which can be run against the JVM. And thus concludes our in depth example.
