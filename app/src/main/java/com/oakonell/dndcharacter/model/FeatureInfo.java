package com.oakonell.dndcharacter.model;

import com.oakonell.dndcharacter.model.components.Feature;
import com.oakonell.dndcharacter.model.components.UseType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rob on 10/26/2015.
 */
public class FeatureInfo {
    Feature feature;
    BaseCharacterComponent source;

    public String getName() {
        return feature.getName();
    }

    public String getSourceString() {
        return source.getSourceString();
    }

    public String getShortDescription() {
        return feature.getDescription();
    }


    public boolean hasLimitedUses() {
        String formula = feature.getUsesFormula();
        return formula != null && formula.length() > 0;
    }

    public BaseCharacterComponent getSource() {
        return source;
    }

    public Feature getFeature() {
        return feature;
    }

    public int evaluateMaxUses(com.oakonell.dndcharacter.model.Character character) {
        Map<String, Integer> extraVariables = new HashMap<>();
        source.addExtraFormulaVariables(extraVariables);
        return character.evaluateFormula(feature.getUsesFormula(), extraVariables);
    }


    public UseType getUseType() {
        return feature.getUseType();
    }
}
