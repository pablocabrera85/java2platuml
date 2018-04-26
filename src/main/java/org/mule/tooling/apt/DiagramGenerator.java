package org.mule.tooling.apt;

import org.mule.tooling.apt.model.ClassDiagram;
import org.mule.tooling.apt.model.CreationContext;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.TypeKindVisitor8;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
public class DiagramGenerator extends AbstractProcessor
{


    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Generating diagram");
        CreationContext context = new CreationContext();
        if (roundEnv.processingOver())
        {
            return false;
        }
        Elements elements = processingEnv.getElementUtils();

        roundEnv.getRootElements().forEach(element ->
        {
            ClassDiagram driagram = context.getOrCreate(elements.getPackageOf(element));
            if (element instanceof TypeElement)
            {
                driagram.getTypes().add((TypeElement) element);
            }
            else
            {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Not a Type " + element);
            }
        });
        context.getPackages().forEach((x, y) ->
        {
            writeDiagram(x, y);
        });


        return false;
    }

    private boolean isObject(TypeMirror typeMirror)
    {
        return processingEnv.getTypeUtils().isSameType(typeMirror, processingEnv.getElementUtils().getTypeElement("java.lang.Object").asType());
    }

    private void writeDiagram(PackageElement x, ClassDiagram y)
    {
        String imageFormat = "svg";
        boolean hideMembers = true;
        Filer filer = processingEnv.getFiler();
        String diagramName = x.getQualifiedName().toString().replace(".", "-") + "-class-diagram";
        try (Writer writer = filer.createResource(StandardLocation.CLASS_OUTPUT, x.getQualifiedName(), diagramName + ".adoc").openWriter())
        {
            Set<String> processedHierarchyEntries = new HashSet<>();
            ElementVisitor<String, StringBuilder> visitor = new DefaultElementVisitor(processingEnv);
            StringBuilder classList = new StringBuilder();
            for (TypeElement typeElement : y.getTypes())
            {
                typeElement.accept(visitor, classList);
            }
            for (TypeElement typeElement : y.getTypes())
            {
                if (ElementKind.ENUM.equals(typeElement.getKind()))
                {
                    continue;
                }
                addHierarchy(visitor, classList, typeElement, processedHierarchyEntries);

            }
            String diagramContent = "[plantuml, " + diagramName + ", " + imageFormat + "]\n" +
                                    "....\n" +
                                    "package " + x.getQualifiedName().toString() + " {\n" +

                                    classList.toString() + "\n" +
                                    "}\n" +
                                    ((hideMembers) ? "hide members\n" : "")
                                    +
                                    "....";
            writer.write(diagramContent);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void addHierarchy(ElementVisitor<String, StringBuilder> visitor, StringBuilder classList, TypeElement typeElement, Set<String> processedHierarchyEntries)
    {
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass != null && !isObject(superclass) && !(superclass instanceof NoType))
        {
            String parent = superclass.toString();
            if (containsGenerics(superclass))
            {
                parent = processingEnv.getTypeUtils().asElement(superclass).toString();
            }
            String hierarchyLine = parent + "<|--" + typeElement.getQualifiedName();
            if (!processedHierarchyEntries.contains(hierarchyLine))
            {
                classList.append(hierarchyLine + "\n");
                processedHierarchyEntries.add(hierarchyLine);
            }
            Element superClassType = processingEnv.getTypeUtils().asElement(superclass);
            if (superClassType instanceof TypeElement)
            {
                addHierarchy(visitor, classList, (TypeElement) superClassType, processedHierarchyEntries);
            }
        }
        if (!typeElement.getInterfaces().isEmpty())
        {
            typeElement.getInterfaces().forEach(x ->
            {
                Element interfaceElement = processingEnv.getTypeUtils().asElement(x);
                interfaceElement.accept(visitor, classList);
                String parent = x.toString();
                if (containsGenerics(x))
                {
                    parent = processingEnv.getTypeUtils().asElement(x).toString();
                }
                String hierarchyLine = parent + "<|--" + typeElement.getQualifiedName();
                if (!processedHierarchyEntries.contains(hierarchyLine))
                {
                    classList.append(hierarchyLine + "\n");
                    processedHierarchyEntries.add(hierarchyLine);
                }
                if (interfaceElement instanceof TypeElement)
                {
                    addHierarchy(visitor, classList, (TypeElement) interfaceElement, processedHierarchyEntries);
                }
            });
        }
    }

    private boolean containsGenerics(TypeMirror superclass)
    {
        Boolean hasGenerics = superclass.accept(new TypeKindVisitor8<Boolean, Object>(false)
        {
            @Override
            public Boolean visitDeclared(DeclaredType t, Object p)
            {
                return !t.getTypeArguments().isEmpty();
            }
        }, null);
        return hasGenerics;
    }


}
