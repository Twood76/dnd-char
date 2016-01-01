package com.oakonell.dndcharacter.model;

import com.activeandroid.query.Select;
import com.oakonell.dndcharacter.model.components.Feature;
import com.oakonell.dndcharacter.model.components.ProficiencyType;
import com.oakonell.dndcharacter.model.components.RefreshType;
import com.oakonell.dndcharacter.model.components.UseType;
import com.oakonell.dndcharacter.model.item.CreateCharacterArmorVisitor;
import com.oakonell.dndcharacter.model.item.CreateCharacterWeaponVisitor;
import com.oakonell.dndcharacter.model.item.ItemRow;
import com.oakonell.dndcharacter.model.item.ItemType;
import com.oakonell.dndcharacter.utils.XmlUtils;
import com.oakonell.dndcharacter.views.FeatureContext;

import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Rob on 11/18/2015.
 */
public class ApplyChangesToGenericComponent<C extends BaseCharacterComponent> extends AbstractComponentVisitor {
    private final C component;
    private final SavedChoices savedChoices;
    private final Character character;
    String currentChoiceName;

    public ApplyChangesToGenericComponent(SavedChoices savedChoices, C component, Character character) {
        this.component = component;
        this.savedChoices = savedChoices;
        this.character = character;
    }


    private static void deleteItems(List<? extends CharacterItem> items, ComponentType componentType) {
        for (Iterator<? extends CharacterItem> iterator = items.iterator(); iterator.hasNext(); ) {
            CharacterItem item = iterator.next();
            if (item.getSource() == componentType) {
                iterator.remove();
            }
        }
    }

    public static <C extends BaseCharacterComponent> void applyToCharacter(Element element, SavedChoices savedChoices, C component, Character character, boolean deleteEquipment) {
        if (deleteEquipment) {
            // first clear any equipment from this type previous value
            ComponentType componentType = component.getType();
            deleteItems(character.getItems(), componentType);
            deleteItems(character.getArmor(), componentType);
            deleteItems(character.getWeapons(), componentType);
        }

        ApplyChangesToGenericComponent<C> newMe = new ApplyChangesToGenericComponent<>(savedChoices, component, character);
        newMe.visitChildren(element);
    }

    @Override
    protected void visitFeature(Element element) {
        Feature feature = new Feature();
        String name = XmlUtils.getElementText(element, "name");
        feature.setName(name);
        feature.setDescription(XmlUtils.getElementText(element, "shortDescription"));
        // TODO handle refreshes, and other data in XML

        final String contextsString = XmlUtils.getElementText(element, "context");
        if (contextsString != null) {
            String[] contexts = contextsString.split(",");
            for (String each : contexts) {
                String contextString = each.trim();
                FeatureContext context = FeatureContext.valueOf(contextString.toUpperCase());
                feature.addContext(context);
            }
        }

        String refreshString = XmlUtils.getElementText(element, "refreshes");
        RefreshType refreshType = null;
        if (refreshString != null) {
            refreshString = refreshString.toLowerCase();
            refreshString = refreshString.replaceAll(" ", "");
            switch (refreshString) {
                case "rest": // fall through
                case "shortrest":
                    refreshType = RefreshType.SHORT_REST;
                    break;
                case "longrest":
                    refreshType = RefreshType.LONG_REST;
                    break;
                default:
                    throw new RuntimeException("Unknown refresh string '" + refreshString + "' on feature " + component.getName() + "." + name);
            }
        }
        final String uses = XmlUtils.getElementText(element, "uses");
        final String pool = XmlUtils.getElementText(element, "pool");
        // TODO fail if both
        UseType useType;
        if (uses != null) {
            useType = UseType.PER_USE;
            feature.setFormula(uses);
            if (refreshType == null) {
                throw new RuntimeException("Missing refreshes element on feature " + component.getName() + "." + name);
            }
            feature.setRefreshesOn(refreshType);
            feature.setUseType(useType);
        } else if (pool != null) {
            useType = UseType.POOL;
            feature.setFormula(pool);
            if (refreshType == null) {
                throw new RuntimeException("Missing refreshes element on feature " + component.getName() + "." + name);
            }
            feature.setRefreshesOn(refreshType);
            feature.setUseType(useType);
        }
        final String ac = XmlUtils.getElementText(element, "ac");
        feature.setAcFormula(ac);

        final String condition = XmlUtils.getElementText(element, "condition");
        feature.setActiveFormula(condition);

        component.addFeature(feature);
        super.visitFeature(element);
    }

    @Override
    protected void visitProficiency(Element element) {
        String skillName = element.getTextContent();
        String category = element.getAttribute("category");

        // TODO how to endode expert, or half prof- via attribute?
        String level = element.getAttribute("level");
        Proficient proficient = Proficient.PROFICIENT;
        if (level != null && level.trim().length() > 0) {
            proficient = Proficient.valueOf(level.toUpperCase());
        }


        if (state == AbstractComponentVisitor.VisitState.SKILLS) {
            skillName = skillName.replaceAll(" ", "_");
            skillName = skillName.toUpperCase();
            SkillType type = SkillType.valueOf(SkillType.class, skillName);
            // TODO handle error
            component.addSkill(type, proficient);
        } else if (state == VisitState.TOOLS || state == VisitState.ARMOR || state == VisitState.WEAPONS) {
            ProficiencyType type;
            if (state == VisitState.TOOLS) {
                type = ProficiencyType.TOOL;
            } else if (state == VisitState.ARMOR) {
                type = ProficiencyType.ARMOR;
            } else if (state == VisitState.WEAPONS) {
                type = ProficiencyType.WEAPON;
            } else {
                throw new RuntimeException("Unexpected state " + state);
            }

            // TODO handle error
            if (category != null && category.trim().length() > 0) {
                component.addToolCategoryProficiency(type, category, proficient);
            } else {
                component.addToolProficiency(type, skillName, proficient);
            }
        } else if (state == VisitState.SAVING_THROWS) {
            skillName = skillName.replaceAll(" ", "_");
            skillName = skillName.toUpperCase();
            StatType type = StatType.valueOf(StatType.class, skillName);
            // TODO handle error
            component.addSaveThrow(type, proficient);

        }
        super.visitProficiency(element);
    }

    @Override
    protected void visitIncrease(Element element) {
        String statName = element.getAttribute("name");
        String amountStr = element.getTextContent();

        statName = statName.replaceAll(" ", "_");
        statName = statName.toUpperCase();
        StatType type = StatType.valueOf(StatType.class, statName);

        int amount = Integer.parseInt(amountStr);

        component.addModifier(type, amount);
        super.visitIncrease(element);
    }

    @Override
    protected void visitLanguage(Element element) {
        String language = element.getTextContent();
        component.getLanguages().add(language);
        super.visitLanguage(element);
    }

    @Override
    protected void visitChoose(Element element) {
        String oldChoiceName = currentChoiceName;

        currentChoiceName = element.getAttribute("name");

        List<Element> childOrElems = XmlUtils.getChildElements(element, "or");
        if (childOrElems.size() == 0) {
            // category, context sensitive choices ?
            categoryChoices();
        } else {
            super.visitChoose(element);
        }

        currentChoiceName = oldChoiceName;
    }

    @Override
    protected void visitItem(Element element) {
        final String itemName = element.getTextContent();
        addItem(itemName);
    }

    private void addItem(String itemName) {
        // look up in items table for more information
        List<ItemRow> items = new Select()
                .from(ItemRow.class).where("UPPER(name) = ?", itemName.toUpperCase()).execute();
        if (!items.isEmpty()) {
            if (items.size() > 1) {
                throw new RuntimeException("Too many items named " + itemName);
            }
            final ItemRow itemRow = items.get(0);
            final ItemType itemType = itemRow.getItemType();
            switch (itemType) {
                case ARMOR:
                    CharacterArmor armor = CreateCharacterArmorVisitor.createArmor(itemRow, character);
                    armor.setName(itemName);
                    armor.setSource(component.getType());
                    break;
                case WEAPON:
                    CharacterWeapon weapon = CreateCharacterWeaponVisitor.createWeapon(itemRow, character);
                    weapon.setName(itemName);
                    weapon.setSource(component.getType());
                    break;
                case EQUIPMENT:
                    CharacterItem item = new CharacterItem();
                    item.setName(itemName);
                    item.setSource(component.getType());

                    ApplyChangesToGenericComponent.applyToCharacter(XmlUtils.getDocument(itemRow.getXml()).getDocumentElement(), null, item, character, false);

                    character.addItem(item);
                    break;
            }
        } else {
            CharacterItem item = new CharacterItem();
            item.setName(itemName);
            item.setSource(component.getType());
            character.addItem(item);
        }
    }

    private void categoryChoices() {
        List<String> selections = savedChoices.getChoicesFor(currentChoiceName);
        for (String selection : selections) {
            switch (state) {
                case LANGUAGES:
                    component.getLanguages().add(selection);
                    break;
                case TOOLS:
                    component.addToolProficiency(ProficiencyType.TOOL, selection, Proficient.PROFICIENT);
                    break;
                case ARMOR:
                    component.addToolProficiency(ProficiencyType.ARMOR, selection, Proficient.PROFICIENT);
                    break;
                case WEAPONS:
                    component.addToolCategoryProficiency(ProficiencyType.WEAPON, selection, Proficient.PROFICIENT);
                    break;
                case EQUIPMENT:
                    addItem(selection);
                    break;
            }
        }

    }

    @Override
    protected void visitOr(Element element) {
        String optionName = element.getAttribute("name");
        List<String> selections = savedChoices.getChoicesFor(currentChoiceName);
        if (selections.contains(optionName)) {
            super.visitOr(element);
        }
    }


}
