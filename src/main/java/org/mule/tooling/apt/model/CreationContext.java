package org.mule.tooling.apt.model;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.PackageElement;

public class CreationContext
{

    private Map<PackageElement, ClassDiagram> packages = new HashMap<>();

    public ClassDiagram getOrCreate(PackageElement packageElement)
    {
        return packages.computeIfAbsent(packageElement, x -> new ClassDiagram());
    }

    public Map<PackageElement, ClassDiagram> getPackages()
    {
        return packages;
    }
}
