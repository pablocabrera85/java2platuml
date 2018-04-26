package org.mule.tooling.apt;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.Assert.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

import javax.tools.JavaFileObject;

import org.junit.Test;

public class DiagramGeneratorTest
{
    private static String CLASS_SOURCE_CODE = "package com.foo;\n" +
                                              "\n" +
                                              "public class MyClass\n" +
                                              "{\n" +
                                              "    private int id;\n" +
                                              "    protected String name;\n" +
                                              "\n" +
                                              "    public int getId()\n" +
                                              "    {\n" +
                                              "        return id;\n" +
                                              "    }\n" +
                                              "\n" +
                                              "    public void setId(int id)\n" +
                                              "    {\n" +
                                              "        this.id = id;\n" +
                                              "    }\n" +
                                              "}\n";

    private static String INTERFACE_SOURCE_CODE = "package com.arg;\n" +
                                                  "\n" +
                                                  "import com.foo.MyClass;\n" +
                                                  "\n" +
                                                  "public interface Creator\n" +
                                                  "{\n" +
                                                  "    MyClass create();\n" +
                                                  "}\n";

    private static String ANNOTATION = "package javax.xml.bind.annotation;\n" +
                                       "\n" +
                                       "import static java.lang.annotation.ElementType.TYPE;\n" +
                                       "import java.lang.annotation.Retention;\n" +
                                       "import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" +
                                       "import java.lang.annotation.Target;\n" +
                                       "\n" +
                                       "@Retention(RUNTIME) @Target({TYPE})\n" +
                                       "public @interface XmlEnum {\n" +
                                       "    /**\n" +
                                       "     * Java type that is mapped to a XML simple type.\n" +
                                       "     *\n" +
                                       "     */\n" +
                                       "    Class<?> value() default String.class;\n" +
                                       "}";
    private static String ENUM_WITH_ANNOTATION = "package org.mule.tooling.editor.model.element;\n" +
                                                 "\n" +
                                                 "import javax.xml.bind.annotation.XmlEnum;\n" +
                                                 "\n" +
                                                 "@XmlEnum\n" +
                                                 "public enum MetaDataKeyParamAffectsType {\n" +
                                                 "    INPUT,\n" +
                                                 "    OUTPUT,\n" +
                                                 "    BOTH,\n" +
                                                 "    AUTO\n" +
                                                 "}";

    private static String CLASS_WITH_INNER = "package com.foo;\n" +
                                             "\n" +
                                             "public class ClassWithInner\n" +
                                             "{\n" +
                                             "    private int number;\n" +
                                             "\n" +
                                             "    private class InnerClass\n" +
                                             "    {\n" +
                                             "        private float money;\n" +
                                             "    }\n" +
                                             "}\n";

    private static String CLASS_WITH_GENERICS = "package com.foo;\n" +
                                                "import java.util.Comparator;\n" +
                                                "\n" +
                                                "public final class XmlOrderComparator implements Comparator<Object> {\n" +
                                                "\n" +
                                                "    @Override\n" +
                                                "    public int compare(Object from, Object to) {\n" +
                                                "        return 1;\n" +
                                                "    }\n" +
                                                "}\n";

    @Test
    public void createClassDiagram()
    {
        JavaFileObject testClass = JavaFileObjects.forSourceString("com.foo.MyClass", CLASS_SOURCE_CODE);
        Compiler compiler = javac().withProcessors(new DiagramGenerator());
        Compilation compilation = compiler.compile(testClass);
        assertTrue(compilation.errors().isEmpty());
    }

    @Test
    public void createInterfaceDiagram()
    {
        JavaFileObject testClass = JavaFileObjects.forSourceString("com.foo.MyClass", CLASS_SOURCE_CODE);
        JavaFileObject testInterface = JavaFileObjects.forSourceString("com.arg.Creator", INTERFACE_SOURCE_CODE);
        Compiler compiler = javac().withProcessors(new DiagramGenerator());
        Compilation compilation = compiler.compile(testClass, testInterface);
        assertTrue(compilation.errors().isEmpty());
    }

    @Test
    public void createEnumDiagram()
    {
        JavaFileObject testClass = JavaFileObjects.forSourceString("org.mule.tooling.editor.model.element.MetaDataKeyParamAffectsType", ENUM_WITH_ANNOTATION);
        JavaFileObject testInterface = JavaFileObjects.forSourceString("javax.xml.bind.annotation.XmlEnum", ANNOTATION);
        Compiler compiler = javac().withProcessors(new DiagramGenerator());
        Compilation compilation = compiler.compile(testClass, testInterface);
        assertTrue(compilation.errors().isEmpty());
    }

    @Test
    public void createInnerDiagram()
    {
        JavaFileObject testClass = JavaFileObjects.forSourceString("com.foo.ClassWithInner", CLASS_WITH_INNER);
        Compiler compiler = javac().withProcessors(new DiagramGenerator());
        Compilation compilation = compiler.compile(testClass);
        assertTrue(compilation.errors().isEmpty());
    }

    @Test
    public void parentWithGenerics()
    {
        JavaFileObject testClass = JavaFileObjects.forSourceString("com.foo.XmlOrderComparator", CLASS_WITH_GENERICS);
        Compiler compiler = javac().withProcessors(new DiagramGenerator());
        Compilation compilation = compiler.compile(testClass);
        assertTrue(compilation.errors().isEmpty());
    }
}
