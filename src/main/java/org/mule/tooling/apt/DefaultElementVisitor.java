package org.mule.tooling.apt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementKindVisitor8;

public class DefaultElementVisitor implements ElementVisitor<String, StringBuilder>
{
    private final ProcessingEnvironment processingEnvironment;
    private final Set<Element> processedTypes;

    public DefaultElementVisitor(ProcessingEnvironment processingEnvironment)
    {
        this.processingEnvironment = processingEnvironment;
        this.processedTypes = new HashSet<>();
    }

    private void logName(Element e)
    {
        System.out.println(e.asType());

    }

    private String getModifiers(Collection<Modifier> modifiers)
    {
        List<String> modifiersStr = new ArrayList<>();
        for (Modifier modifier : modifiers)
        {
            modifiersStr.add(from(modifier));
        }
        return modifiersStr.stream().filter(x -> !x.isEmpty()).collect(Collectors.joining(" "));

    }

    private String from(Modifier modifier)
    {
        switch (modifier)
        {
        case PUBLIC:
            return "+";
        case PRIVATE:
            return "-";
        case PROTECTED:
            return "#";
        case DEFAULT:
            return "~";
        case ABSTRACT:
            return "{abstract}";
        case STATIC:
            return "{static}";
        case FINAL:
            return "";
        default:
            return modifier.toString();
        }
    }

    private String classModifier(Modifier modifier)
    {
        switch (modifier)
        {
        case PUBLIC:
            return "+";
        case PRIVATE:
            return "-";
        case PROTECTED:
            return "#";
        case DEFAULT:
            return "~";
        case ABSTRACT:
            return "abstract";
        case STATIC:
            return "static";
        case FINAL:
            return "";
        default:
            return modifier.toString();
        }
    }

    private void processTypeElement(TypeElement e, StringBuilder o)
    {
        if (processedTypes.contains(e))
        {
            return;
        }
        ElementKindVisitor8 visitor = new ElementKindVisitor8<String, StringBuilder>()
        {
            public String visitTypeAsAnnotationType(TypeElement e, StringBuilder p)
            {
                p.append("annotation ");
                return "";
            }

            public String visitTypeAsClass(TypeElement e, StringBuilder p)
            {
                p.append("class ");
                return "";
            }

            public String visitTypeAsEnum(TypeElement e, StringBuilder p)
            {
                p.append("enum ");
                return "";
            }

            public String visitTypeAsInterface(TypeElement e, StringBuilder p)
            {
                p.append("interface ");
                return "";
            }
        };
        e.accept(visitor, o);
        o.append(e.getQualifiedName()).append("{\n");
        processedTypes.add(e);
        e.getEnclosedElements().stream().filter(x -> !isInnerType(x)).forEach(x -> x.accept(this, o));
        o.append("}\n");
        e.getEnclosedElements().stream().filter(x -> isInnerType(x)).forEach(x -> x.accept(this, o));
    }

    private boolean isInnerType(Element element)
    {
        ElementKindVisitor8 visitor = new ElementKindVisitor8<Boolean, Object>(false)
        {
            public Boolean visitTypeAsAnnotationType(TypeElement e, Object p)
            {
                return true;
            }

            public Boolean visitTypeAsClass(TypeElement e, Object p)
            {
                return true;
            }

            public Boolean visitTypeAsEnum(TypeElement e, Object p)
            {
                return true;
            }

            public Boolean visitTypeAsInterface(TypeElement e, Object p)
            {
                return true;
            }
        };
        Object accept = element.accept(visitor, null);
        return (Boolean) accept;
    }

    @Override public String visit(Element e, StringBuilder o)
    {
        e.accept(this, o);
        return o.toString();
    }

    @Override public String visit(Element e)
    {
        StringBuilder o = new StringBuilder();
        e.accept(this, o);
        return o.toString();
    }

    @Override public String visitPackage(PackageElement e, StringBuilder o)
    {
        logName(e);
        e.getEnclosedElements().forEach(x -> x.accept(this, o));
        return null;
    }

    @Override public String visitType(TypeElement e, StringBuilder o)
    {
        processTypeElement(e, o);
        return o.toString();
    }

    @Override public String visitVariable(VariableElement e, StringBuilder o)
    {
        o.append("\t" + getModifiers(e.getModifiers()) + e.getSimpleName().toString() + ": " + e.asType().toString()).append("\n");
        return null;
    }

    @Override public String visitExecutable(ExecutableElement e, StringBuilder o)
    {
        o.append("\t");
        if (!e.getEnclosingElement().getKind().isInterface())
        {
            o.append(getModifiers(e.getModifiers()));
        }
        o.append(e.getSimpleName())
         .append("(" + e.getParameters().stream().map(x -> x.asType() + " " + x.getSimpleName()).collect(Collectors.joining(",")) + "): ")
         .append(e.getReturnType())
         .append("\n");
        return null;
    }

    @Override public String visitTypeParameter(TypeParameterElement e, StringBuilder o)
    {
        logName(e);
        e.getEnclosedElements().forEach(x -> x.accept(this, o));
        return null;
    }

    @Override public String visitUnknown(Element e, StringBuilder o)
    {
        logName(e);
        e.getEnclosedElements().forEach(x -> x.accept(this, o));
        return null;
    }
}
