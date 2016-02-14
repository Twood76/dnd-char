package com.oakonell.dndcharacter.model.components;

import android.support.annotation.NonNull;

import com.oakonell.dndcharacter.model.character.*;
import com.oakonell.dndcharacter.model.character.Character;
import com.oakonell.expression.Expression;
import com.oakonell.expression.ExpressionContext;
import com.oakonell.expression.ExpressionType;
import com.oakonell.expression.context.SimpleFunctionContext;
import com.oakonell.expression.context.SimpleVariableContext;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Rob on 10/24/2015.
 */
public class Feature extends AbstractContextualComponent {
    @Element(required = false)
    private String usesFormula;
    @Element(required = false)
    private RefreshType refreshType;
    @Element(required = false)
    private UseType useType;

    @Element(required = false)
    private String appliesFormula;

    @NonNull
    @ElementList(required = false)
    private List<FeatureCharacterEffect> effects = new ArrayList<>();
    @NonNull
    @ElementList(required = false)
    private List<FeatureAction> actions = new ArrayList<>();

    @Element(required = false)
    private FeatureExtensionType extensionType;

    @NonNull
    @Override
    public ComponentType getType() {
        return ComponentType.FEATURE;
    }

    public UseType getUseType() {
        return useType;
    }

    public void setUseType(UseType useType) {
        this.useType = useType;
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

    public void addEffect(FeatureCharacterEffect characterEffect) {
        effects.add(characterEffect);
    }

    @NonNull
    public List<FeatureCharacterEffect> getEffects() {
        return effects;
    }

    public void addAction(FeatureAction action) {
        actions.add(action);
    }

    @NonNull
    public List<FeatureAction> getActions() {
        return actions;
    }

    @NonNull
    public List<IFeatureAction> getActionsAndEffects() {
        List<IFeatureAction> result = new ArrayList<>();
        result.addAll(getActions());
        result.addAll(getEffects());
        return result;
    }

    public boolean applies(Character character) {
        if (appliesFormula == null || appliesFormula.trim().length() == 0) return true;

        // TODO can't construct the normal context variables, as it looks on features!
        //    possibly calculate variables without this feature, in some contextual way
        SimpleVariableContext variableContext = new SimpleVariableContext();
        variableContext.setNumber("level", character.getClasses().size());

        try {
            Expression<Boolean> expression = Expression.parse(appliesFormula, ExpressionType.BOOLEAN_TYPE, new ExpressionContext(new SimpleFunctionContext(), variableContext));
            return expression.evaluate();
        } catch (Exception e) {
            // should be done at formula save time...
            return false;
        }
    }

    public void setAppliesFormula(String appliesFormula) {
        this.appliesFormula = appliesFormula;
    }

    public FeatureExtensionType getExtensionType() {
        return extensionType;
    }

    public void setExtensionType(FeatureExtensionType extensionType) {
        this.extensionType = extensionType;
    }

    public static class FeatureAction implements IFeatureAction {
        @Element(required = false)
        String name;
        @Element(required = false)
        private String description;

        @Element(required = false)
        int cost;
        @Element(required = false)
        private FeatureExtensionType extensionType;

        @Override
        public String getAction() {
            return getName();
        }

        public String getName() {
            return name;
        }

        @Override
        public List<FeatureEffectVariable> getVariables() {
            return Collections.emptyList();
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

        @Override
        public int getCost() {
            return cost;
        }

        @Override
        public boolean hasVariables() {
            return false;
        }

        @Override
        public void applyToCharacter(com.oakonell.dndcharacter.model.character.Character character, Map<FeatureEffectVariable, String> promptValues) {
            // do nothing
        }

        @Override
        public String getActionDescription() {
            return getDescription();
        }


        public void setCost(int cost) {
            this.cost = cost;
        }

        public void setExtensionType(FeatureExtensionType extensionType) {
            this.extensionType = extensionType;
        }

        @Override
        public boolean replacesPrevious() {
            return extensionType == FeatureExtensionType.REPLACE;
        }
    }

    public static class FeatureEffectVariable {
        @Element(required = false)
        private String name;
        @Element(required = false)
        private String prompt;
        @ElementArray(required = false)
        private String[] values;

        public FeatureEffectVariable() {
            // Needed for simple XML framework
        }

        public FeatureEffectVariable(String varName, String promptString, String[] values) {
            this.name = varName;
            this.prompt = promptString;
            this.values = values;
        }

        public String getPrompt() {
            return prompt;
        }

        public String getName() {
            return name;
        }

        public String[] getValues() {
            return values;
        }
    }

    public static class FeatureCharacterEffect extends CharacterEffect implements IFeatureAction, Cloneable {
        @Element(required = false)
        private String action;

        @Element(required = false)
        int cost;
        @Element(required = false)
        private String actionDescription;
        @Element(required = false)
        private FeatureExtensionType extensionType;

        @ElementList(required = false)
        private List<FeatureEffectVariable> variables = new ArrayList<>();

        public void addVariable(FeatureEffectVariable variable) {
            variables.add(variable);
        }

        public boolean hasVariables() {
            return !variables.isEmpty();
        }

        public void setCost(int cost) {
            this.cost = cost;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public int getCost() {
            return cost;
        }


        @Override
        public void applyToCharacter(@NonNull Character character, Map<FeatureEffectVariable, String> variableValues) {
            // TODO create a clone, so that specialized variables can be applied?
            if (!variableValues.isEmpty()) {
                try {
                    CharacterEffect newEffect = (CharacterEffect) clone();
                    String theName = getName();
                    String theDescription = getDescription();
                    for (Map.Entry<FeatureEffectVariable, String> entry : variableValues.entrySet()) {
                        String variablePattern = "\\$" + entry.getKey().name;
                        String value = entry.getValue();
                        theName = theName.replaceAll(variablePattern, value);
                        if (theDescription != null) {
                            theDescription = theDescription.replaceAll(variablePattern, value);
                        }
                        // TODO look through the contexts to replace any prompt variables
                    }
                    newEffect.setName(theName);
                    newEffect.setDescription(theDescription);
                    character.addEffect(newEffect);
                } catch (CloneNotSupportedException e) {
                    character.addEffect(this);

                }
            } else {
                character.addEffect(this);
            }
        }

        @Override
        public String getActionDescription() {
            return actionDescription;
        }

        public void setActionDescription(String actionDescription) {
            this.actionDescription = actionDescription;
        }

        public void setExtensionType(FeatureExtensionType extensionType) {
            this.extensionType = extensionType;
        }

        @Override
        public boolean replacesPrevious() {
            return extensionType == FeatureExtensionType.REPLACE;
        }

        @Override
        public List<FeatureEffectVariable> getVariables() {
            return variables;
        }
    }
}
