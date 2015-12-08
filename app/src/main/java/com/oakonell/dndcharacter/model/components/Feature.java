package com.oakonell.dndcharacter.model.components;

import org.simpleframework.xml.Element;

/**
 * Created by Rob on 10/24/2015.
 */
public class Feature {
    @Element(required = false)
    private String name;
    @Element(required = false)
    private String description;
    @Element(required = false)
    private String usesFormula;
    @Element(required = false)
    private RefreshType refreshType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsesFormula() {
        return usesFormula;
    }

    public void setFormula(String formula) {
        usesFormula = formula;
    }

    public RefreshType getRefreshesOn() {
        return refreshType;
    }

    public void setRefreshesOn(RefreshType type) {
        this.refreshType = type;
    }
}
