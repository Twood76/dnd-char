package com.oakonell.dndcharacter.views.character.rest;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.oakonell.dndcharacter.model.character.SpellSlotResetInfo;
import com.oakonell.dndcharacter.utils.NumberUtils;
import com.oakonell.dndcharacter.views.BindableComponentViewHolder;
import com.oakonell.dndcharacter.R;
import com.oakonell.dndcharacter.model.character.rest.AbstractRestRequest;
import com.oakonell.dndcharacter.model.character.Character;
import com.oakonell.dndcharacter.model.character.FeatureInfo;
import com.oakonell.dndcharacter.model.character.FeatureResetInfo;
import com.oakonell.dndcharacter.model.components.RefreshType;
import com.oakonell.dndcharacter.views.character.AbstractCharacterDialogFragment;
import com.oakonell.dndcharacter.views.DividerItemDecoration;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Rob on 11/8/2015.
 */
public abstract class AbstractRestDialogFragment extends AbstractCharacterDialogFragment {
    public static final String FEATURE_RESETS_SAVE_BUNDLE_KEY = "featureResets";
    public static final String SPELL_SLOT_RESETS_SAVE_BUNDLE_KEY = "spellSlotResets";

    public static final String RESET = "reset";
    public static final String NUM_TO_RESTORE = "numToRestore";

    private View extraHealingGroup;
    private TextView extraHealingtextView;
    private TextView finalHp;
    private TextView startHp;
    private View finalHpGroup;
    private View noHealingGroup;

    private FeatureResetsAdapter featureResetAdapter;
    private RecyclerView featureListView;
    private SpellSlotsResetsAdapter spellSlotResetAdapter;
    private RecyclerView spell_slot_list;

    @Nullable
    private Bundle savedFeatureResets;
    private Bundle savedSpellSlotResets;

    protected boolean allowExtraHealing() {
        return getCharacter().getHP() != getCharacter().getMaxHP();
    }

    protected void conditionallyShowExtraHealing() {
        extraHealingGroup.setVisibility(allowExtraHealing() ? View.VISIBLE : View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedFeatureResets = savedInstanceState.getBundle(FEATURE_RESETS_SAVE_BUNDLE_KEY);
            savedSpellSlotResets = savedInstanceState.getBundle(SPELL_SLOT_RESETS_SAVE_BUNDLE_KEY);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle resets = new Bundle();
        for (FeatureResetInfo each : featureResetAdapter.resets) {
            Bundle reset = new Bundle();
            reset.putByte(RESET, (byte) (each.reset ? 1 : 0));
            reset.putInt(NUM_TO_RESTORE, each.numToRestore);
            resets.putBundle(each.name, reset);
        }
        outState.putBundle(FEATURE_RESETS_SAVE_BUNDLE_KEY, resets);

        Bundle slotResets = new Bundle();
        for (SpellSlotResetInfo each : spellSlotResetAdapter.resets) {
            Bundle reset = new Bundle();
            reset.putByte(RESET, (byte) (each.reset ? 1 : 0));
            reset.putInt(NUM_TO_RESTORE, each.restoreSlots);
            slotResets.putBundle(each.level + "", reset);
        }
        outState.putBundle(SPELL_SLOT_RESETS_SAVE_BUNDLE_KEY, slotResets);
    }

    @Override
    public void onCharacterLoaded(@NonNull Character character) {
        super.onCharacterLoaded(character);

        conditionallyShowExtraHealing();

        if (character.getHP() == character.getMaxHP()) {
            noHealingGroup.setVisibility(View.VISIBLE);

            finalHpGroup.setVisibility(View.GONE);
        } else {
            noHealingGroup.setVisibility(View.GONE);

            finalHpGroup.setVisibility(View.VISIBLE);
        }

        startHp.setText(getString(R.string.fraction_d_slash_d, character.getHP(), character.getMaxHP()));

        List<FeatureInfo> features = character.getFeatureInfos();
        List<FeatureResetInfo> featureResets = new ArrayList<>();
        for (FeatureInfo each : features) {
            RefreshType refreshOn = each.getFeature().getRefreshesOn();
            if (refreshOn == null) continue;

            FeatureResetInfo resetInfo = new FeatureResetInfo();
            resetInfo.name = each.getName();
            resetInfo.description = each.getShortDescription();
            int maxUses = each.evaluateMaxUses(character);
            int usesRemaining = character.getUsesRemaining(each);
            Bundle resetBundle = null;
            if (savedFeatureResets != null) {
                resetBundle = savedFeatureResets.getBundle(each.getName());
            }
            if (resetBundle != null) {
                resetInfo.reset = resetBundle.getByte(RESET) != 0;
                resetInfo.numToRestore = resetBundle.getInt(NUM_TO_RESTORE);
            } else {
                resetInfo.reset = shouldReset(refreshOn);
                if (resetInfo.reset) {
                    resetInfo.numToRestore = maxUses - usesRemaining;
                } else {
                    resetInfo.numToRestore = 0;
                }
            }
            resetInfo.refreshOn = refreshOn;
            resetInfo.maxToRestore = maxUses - usesRemaining;
            resetInfo.uses = getString(R.string.fraction_d_slash_d, usesRemaining, maxUses);
            resetInfo.needsResfesh = usesRemaining != maxUses;
            featureResets.add(resetInfo);
        }
        savedFeatureResets = null;
        featureResetAdapter = new FeatureResetsAdapter(getActivity(), featureResets);
        featureListView.setAdapter(featureResetAdapter);

        featureListView.setHasFixedSize(false);
        featureListView.setLayoutManager(new org.solovyev.android.views.llm.LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        featureListView.addItemDecoration(itemDecoration);


        List<SpellSlotResetInfo> spellSlotResets = new ArrayList<>();

        final List<Character.SpellLevelInfo> spellInfos = character.getSpellInfos();
        for (Character.SpellLevelInfo each : spellInfos) {
            if (each.getLevel() == 0) continue;
            SpellSlotResetInfo resetInfo = new SpellSlotResetInfo();
            resetInfo.level = each.getLevel();
            resetInfo.maxSlots = each.getMaxSlots();
            resetInfo.availableSlots = each.getSlotsAvailable();

            Bundle resetBundle = null;
            if (savedSpellSlotResets != null) {
                resetBundle = savedSpellSlotResets.getBundle(each.getLevel() + "");
            }
            if (resetBundle != null) {
                resetInfo.reset = resetBundle.getByte(RESET) != 0;
                resetInfo.restoreSlots = resetBundle.getInt(NUM_TO_RESTORE);
            } else {

                int toRestore = getSlotsToRestore(each);
                resetInfo.reset = toRestore > 0;
                if (resetInfo.reset) {
                    resetInfo.restoreSlots = toRestore;
                } else {
                    resetInfo.restoreSlots = 0;
                }
            }
            spellSlotResets.add(resetInfo);
        }
        savedSpellSlotResets = null;


        spellSlotResetAdapter = new SpellSlotsResetsAdapter(getActivity(), spellSlotResets);
        spell_slot_list.setAdapter(spellSlotResetAdapter);

        spell_slot_list.setHasFixedSize(false);
        spell_slot_list.setLayoutManager(new org.solovyev.android.views.llm.LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL_LIST);
        spell_slot_list.addItemDecoration(horizontalDecoration);

    }


    protected abstract int getSlotsToRestore(Character.SpellLevelInfo each);

    protected void configureCommon(@NonNull View view) {
        //featureResetsGroup = view.findViewById(R.id.feature_resets);
        startHp = (TextView) view.findViewById(R.id.start_hp);
        finalHp = (TextView) view.findViewById(R.id.final_hp);
        finalHpGroup = view.findViewById(R.id.final_hp_group);
        extraHealingGroup = view.findViewById(R.id.extra_heal_group);
        extraHealingtextView = (TextView) view.findViewById(R.id.extra_healing);
        noHealingGroup = view.findViewById(R.id.no_healing_group);

        featureListView = (RecyclerView) view.findViewById(R.id.feature_list);
        spell_slot_list = (RecyclerView) view.findViewById(R.id.spell_slot_list);

        extraHealingtextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateView();
            }
        });


    }

    protected abstract boolean shouldReset(RefreshType refreshesOn);

    protected int getExtraHealing() {
        String extraHealString = extraHealingtextView.getText().toString();
        if (extraHealString.trim().length() > 0) {
            return Integer.parseInt(extraHealString);
        }
        return 0;
    }

    public void updateView() {
        Character character = getCharacter();
        if (character == null) return;
        int hp = character.getHP();
        int healing = getHealing();
        hp = Math.min(hp + healing, character.getMaxHP());
        finalHp.setText(getString(R.string.fraction_d_slash_d, hp, character.getMaxHP()));
    }

    protected abstract int getHealing();

    protected boolean updateCommonRequest(@NonNull AbstractRestRequest request) {
        boolean isValid = true;
        for (FeatureResetInfo each : featureResetAdapter.resets) {
            if (each.reset) {
                isValid = isValid && each.numToRestore <= each.maxToRestore;
                request.addFeatureReset(each.name, each.numToRestore);
            }
        }
        for (SpellSlotResetInfo each : spellSlotResetAdapter.resets) {
            if (each.reset) {
                isValid = isValid && each.restoreSlots <= each.maxSlots - each.availableSlots;
                request.addSpellSlotReset(each.level, each.restoreSlots);
            }
        }
        return isValid;
    }

    static class SpellSlotResetViewHolder extends BindableComponentViewHolder<SpellSlotResetInfo, Context, SpellSlotsResetsAdapter> {
        private final CheckBox level;
        private final TextView slots;
        private final TextView final_slots;

        public SpellSlotResetViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.findViewById(R.id.level_label).setVisibility(View.GONE);
            level = (CheckBox) itemView.findViewById(R.id.level);
            level.setVisibility(View.VISIBLE);
            slots = (TextView) itemView.findViewById(R.id.slots);
            final_slots = (TextView) itemView.findViewById(R.id.final_slots);
        }

        @Override
        public void bind(Context context, final SpellSlotsResetsAdapter adapter, final SpellSlotResetInfo info) {
            level.setText(NumberUtils.formatNumber(info.level));
            level.setOnCheckedChangeListener(null);
            level.setChecked(info.reset);
            slots.setText(context.getString(R.string.fraction_d_slash_d, info.availableSlots, info.maxSlots));
            level.setEnabled(info.availableSlots < info.maxSlots);


            level.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        info.restoreSlots = info.maxSlots - info.availableSlots;
                    } else {
                        info.restoreSlots = 0;
                    }
                    info.reset = isChecked;
                    adapter.notifyItemChanged(getAdapterPosition());
                }
            });

            final_slots.setText(context.getString(R.string.fraction_d_slash_d, info.availableSlots + info.restoreSlots, info.maxSlots));
        }
    }

    static class SpellSlotsResetsAdapter extends RecyclerView.Adapter<SpellSlotResetViewHolder> {
        private final List<SpellSlotResetInfo> resets;
        private final Context context;

        SpellSlotsResetsAdapter(Context context, List<SpellSlotResetInfo> resets) {
            this.context = context;
            this.resets = resets;
        }

        @Override
        public SpellSlotResetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(context, R.layout.rest_spell_slot_reset_item, null);
            SpellSlotResetViewHolder holder = new SpellSlotResetViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(SpellSlotResetViewHolder holder, int position) {
            holder.bind(context, this, resets.get(position));
        }

        @Override
        public int getItemCount() {
            return resets.size();
        }
    }

    static class FeatureResetsAdapter extends RecyclerView.Adapter<FeatureResetViewHolder> {
        private final List<FeatureResetInfo> resets;
        private final Context context;

        public FeatureResetsAdapter(Context context, List<FeatureResetInfo> resets) {
            this.context = context;
            this.resets = resets;
        }


        public FeatureResetInfo getItem(int position) {
            return resets.get(position);
        }

        @NonNull
        @Override
        public FeatureResetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(context, R.layout.feature_reset_item, null);
            FeatureResetViewHolder viewHolder = new FeatureResetViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final FeatureResetViewHolder viewHolder, int position) {
            final FeatureResetInfo row = getItem(position);
            viewHolder.bind(context, this, row);

        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return resets.size();
        }


    }

    static class FeatureResetViewHolder extends BindableComponentViewHolder<FeatureResetInfo, Context, FeatureResetsAdapter> {
        @Nullable
        public TextWatcher watcher;
        @NonNull
        final CheckBox name;
        @NonNull
        final TextView description;
        @NonNull
        final TextView uses;
        @NonNull
        final EditText numToRestore;

        public FeatureResetViewHolder(@NonNull View view) {
            super(view);
            name = (CheckBox) view.findViewById(R.id.feature_name);
            description = (TextView) view.findViewById(R.id.description);
            uses = (TextView) view.findViewById(R.id.uses);
            numToRestore = (EditText) view.findViewById(R.id.num_to_restore);
        }

        @Override
        public void bind(@NonNull final Context context, final FeatureResetsAdapter adapter, @NonNull final FeatureResetInfo row) {
            if (watcher != null) {
                numToRestore.removeTextChangedListener(watcher);
            }
            name.setOnCheckedChangeListener(null);

            name.setText(row.name);
            name.setChecked(row.reset);

            description.setText(row.description);

            uses.setText(row.uses);

            numToRestore.setText(NumberUtils.formatNumber(row.numToRestore));
            numToRestore.setEnabled(row.reset);

            if (!row.needsResfesh) {
                name.setChecked(false);
                name.setEnabled(false);
                numToRestore.setEnabled(false);
            } else {
                name.setEnabled(true);
            }

            TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    numToRestore.setError(null);
                }

                @Override
                public void afterTextChanged(@Nullable Editable s) {
                    // TODO error handling, if val is too large
                    if (s == null) return;
                    if (!row.reset) return;

                    String stringVal = s.toString().trim();
                    if (stringVal.length() == 0) return;
                    int value;
                    try {
                        value = Integer.parseInt(stringVal);
                    } catch (Exception e) {
                        numToRestore.setError(context.getString(R.string.enter_number_less_than_equal_n, row.maxToRestore));
                        return;
                    }
                    if (value > row.maxToRestore) {
                        numToRestore.setError(context.getString(R.string.enter_number_less_than_equal_n, row.maxToRestore));
                    }

                    row.numToRestore = value;
                }
            };
            numToRestore.addTextChangedListener(watcher);
            this.watcher = watcher;

            name.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    row.reset = isChecked;
                    numToRestore.setEnabled(row.reset);
                    if (row.reset) {
                        // force a validation
                        numToRestore.setText(numToRestore.getText());
                    } else {
                        numToRestore.setError(null);
                    }
                }
            });
        }
    }
}
