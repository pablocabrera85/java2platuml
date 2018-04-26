package org.mule.tooling.apt.model;

import java.util.Collection;
import java.util.HashSet;

import javax.lang.model.element.TypeElement;

public class ClassDiagram
{
    private String packageName;

    public Collection<TypeElement> getTypes()
    {
        return types;
    }

    private Collection<TypeElement> types = new HashSet<>();

    public void addType(TypeElement type)
    {
        types.add(type);
    }

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }
}
