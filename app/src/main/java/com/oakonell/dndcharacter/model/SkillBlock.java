package com.oakonell.dndcharacter.model;

import java.util.List;

/**
 * Created by Rob on 10/22/2015.
 */
public class SkillBlock {
    private Character character;
    private SkillType type;

    public SkillBlock(Character character, SkillType type) {
        this.character = character;
        this.type = type;
    }

    public SkillType getType() {
        return type;
    }


    public int getBonus() {
        Proficient proficient = getProficiency();
        int statMod = character.getStatBlock(type.getStatType()).getModifier();
        int proficiency = character.getProficiency();
        return ((int) (proficiency * proficient.getMultiplier())) + statMod;
    }

    public Proficient getProficiency() {
        return character.deriveSkillProciency(type);
    }

    public List<Character.ProficientAndReason> getProficiencies() {
        return character.deriveSkillProciencies(type);
    }

    public int getStatModifier() {
        return character.getStatBlock(type.getStatType()).getModifier();
    }

    public Character getCharacter() {
        return character;
    }
}