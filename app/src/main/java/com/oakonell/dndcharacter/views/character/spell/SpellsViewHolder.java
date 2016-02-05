package com.oakonell.dndcharacter.views.character.spell;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.oakonell.dndcharacter.R;
import com.oakonell.dndcharacter.model.character.Character;
import com.oakonell.dndcharacter.model.character.spell.CharacterSpell;
import com.oakonell.dndcharacter.utils.NumberUtils;

/**
 * Created by Rob on 1/24/2016.
 */
public class SpellsViewHolder extends SpellLevelsAdapter.AbstractSpellLevelViewHolder {
    @NonNull
    private final TextView available_slots;
    @NonNull
    private final TextView total_slots;
    @NonNull
    private final TextView level;
    private final Button use_button;

    public SpellsViewHolder(@NonNull View itemView) {
        super(itemView);
        level = (TextView) itemView.findViewById(R.id.level);
        available_slots = (TextView) itemView.findViewById(R.id.available_slots);
        total_slots = (TextView) itemView.findViewById(R.id.total_slots);
        use_button = (Button) itemView.findViewById(R.id.use_button);
    }

    @Override
    public void bind(final SpellsFragment context, SpellLevelsAdapter adapter, final @NonNull com.oakonell.dndcharacter.model.character.Character.SpellLevelInfo info) {
        super.bind(context, adapter, info);
        available_slots.setText(NumberUtils.formatNumber(info.getSlotsAvailable()));
        total_slots.setText(NumberUtils.formatNumber(info.getMaxSlots()));
        level.setText(NumberUtils.formatNumber(info.getLevel()));
        use_button.setEnabled(info.getSlotsAvailable() > 0);
        use_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.getCharacter().useSpellSlot(info.getLevel());
                context.updateViews();
                context.getMainActivity().saveCharacter();
            }
        });
    }

    @NonNull
    @Override
    protected AbstractSpellAdapter newAdapter(SpellsFragment context, Character.SpellLevelInfo info) {
        return new SpellAdapter(context, info);
    }

    public static class SpellAdapter extends AbstractSpellAdapter<SpellViewHolder> {
        SpellAdapter(SpellsFragment context, Character.SpellLevelInfo spellLevelInfo) {
            super(context, spellLevelInfo);
        }

        @NonNull
        @Override
        public SpellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View newView = LayoutInflater.from(parent.getContext()).inflate(R.layout.spell_char_row, parent, false);
            return new SpellViewHolder(newView);
        }
    }

    public static class SpellViewHolder extends AbstractSpellAdapter.AbstractSpellViewHolder {
        @NonNull
        final CheckBox prepared;

        public SpellViewHolder(@NonNull View itemView) {
            super(itemView);
            prepared = (CheckBox) itemView.findViewById(R.id.prepared);
        }

        @Override
        public void bind(@NonNull final SpellsFragment context, AbstractSpellAdapter adapter, @NonNull final CharacterSpell info) {
            super.bind(context, adapter, info);
            if (!info.isPreparable()) {
                prepared.setVisibility(View.INVISIBLE);
            } else {
                prepared.setVisibility(View.VISIBLE);
            }
            prepared.setOnCheckedChangeListener(null);
            prepared.setChecked(info.isPrepared());
            prepared.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    info.setPrepared(isChecked);
                    context.updateViews();
                    context.getMainActivity().saveCharacter();
                }
            });
        }
    }
}