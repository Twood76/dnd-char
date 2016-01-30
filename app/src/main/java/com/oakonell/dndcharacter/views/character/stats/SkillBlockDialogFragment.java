package com.oakonell.dndcharacter.views.character.stats;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.oakonell.dndcharacter.R;
import com.oakonell.dndcharacter.model.character.BaseCharacterComponent;
import com.oakonell.dndcharacter.model.character.Character;
import com.oakonell.dndcharacter.model.character.Proficient;
import com.oakonell.dndcharacter.model.character.stats.SkillBlock;
import com.oakonell.dndcharacter.model.character.stats.SkillType;
import com.oakonell.dndcharacter.utils.NumberUtils;
import com.oakonell.dndcharacter.views.character.MainActivity;
import com.oakonell.dndcharacter.views.character.feature.FeatureContext;
import com.oakonell.dndcharacter.views.character.RollableDialogFragment;
import com.oakonell.dndcharacter.views.character.RowWithSourceAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Rob on 11/7/2015.
 */
public class SkillBlockDialogFragment extends RollableDialogFragment {
    public static final String TYPE = "type";
    private TextView statLabel;
    private TextView statModLabel;
    private TextView statMod;
    private TextView proficiency;
    private View proficiencyLayout;
    private TextView total;
    private RecyclerView listView;

    private SkillBlock skillBlock;
    private SkillType type;

    private SkillSourceAdapter adapter;

    @NonNull
    public static SkillBlockDialogFragment create(@NonNull SkillBlock block) {
        SkillBlockDialogFragment frag = new SkillBlockDialogFragment();
        int typeIndex = block.getType().ordinal();
        Bundle args = new Bundle();
        args.putInt(TYPE, typeIndex);
        frag.setArguments(args);

        return frag;
    }


    @Override
    public View onCreateTheView(@NonNull LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.skill_dialog, container);
        superCreateView(view, savedInstanceState);

        statLabel = (TextView) view.findViewById(R.id.stat_label);
        statModLabel = (TextView) view.findViewById(R.id.stat_mod_lbl);
        statMod = (TextView) view.findViewById(R.id.stat_mod);
        proficiency = (TextView) view.findViewById(R.id.proficiency);
        proficiencyLayout = view.findViewById(R.id.proficiency_layout);

        total = (TextView) view.findViewById(R.id.total);
        listView = (RecyclerView) view.findViewById(R.id.list);

        return view;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.skill_proficiency);
    }


    @Override
    public void onCharacterLoaded(@NonNull Character character) {
        super.onCharacterLoaded(character);
        int typeIndex = getArguments().getInt(TYPE);
        type = SkillType.values()[typeIndex];
        skillBlock = character.getSkillBlock(type);

        updateView(character);

        RowWithSourceAdapter.ListRetriever<Character.ProficientWithSource> listRetriever = new RowWithSourceAdapter.ListRetriever<Character.ProficientWithSource>() {
            @Override
            public List<Character.ProficientWithSource> getList(@NonNull Character character) {
                return character.getSkillBlock(type).getProficiencies();
            }
        };

        adapter = new SkillSourceAdapter(this, listRetriever);
        listView.setAdapter(adapter);

        listView.setLayoutManager(new org.solovyev.android.views.llm.LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        listView.setHasFixedSize(false);
    }

    @NonNull
    @Override
    protected Set<FeatureContext> getContextFilter() {
        Set<FeatureContext> filter = new HashSet<>();
        filter.add(FeatureContext.DICE_ROLL);
        filter.add(FeatureContext.SKILL_ROLL);
        return filter;
    }

    private void updateView(Character character) {
        setModifier(skillBlock.getBonus());

        List<Character.ProficientWithSource> proficiencies = skillBlock.getProficiencies();

        if (proficiencies.isEmpty()) {
            proficiencyLayout.setVisibility(View.GONE);
        } else {
            proficiencyLayout.setVisibility(View.VISIBLE);
            proficiency.setText(NumberUtils.formatNumber(skillBlock.getCharacter().getProficiency()));
        }

        statModLabel.setText(getString(R.string.statname_modifier_label, skillBlock.getType().getStatType().toString()));
        statMod.setText(NumberUtils.formatNumber(skillBlock.getStatModifier()));
        statLabel.setText(skillBlock.getType().getStatType().toString());
        getDialog().setTitle(skillBlock.getType().toString());
        total.setText(NumberUtils.formatNumber(skillBlock.getBonus()));
    }

    @Override
    public void onCharacterChanged(@NonNull Character character) {
        super.onCharacterChanged(character);

        int typeIndex = getArguments().getInt(TYPE);
        SkillType type = SkillType.values()[typeIndex];
        skillBlock = character.getSkillBlock(type);

        updateView(character);

        adapter.reloadList(character);
    }

    public static class SkillProfWithSourceViewHolder extends RowWithSourceAdapter.WithSourceViewHolder<Character.ProficientWithSource> {

        public SkillProfWithSourceViewHolder(View view) {
            super(view);
        }

        @Override
        public void bind(MainActivity activity, RowWithSourceAdapter<Character.ProficientWithSource, RowWithSourceAdapter.WithSourceViewHolder<Character.ProficientWithSource>> adapter, Character.ProficientWithSource item) {
            super.bind(activity, adapter, item);
            Proficient value = item.getProficient();
            this.value.setText(value.toString());
            final BaseCharacterComponent source = item.getSource();
            if (source == null) {
                // a base stat
                this.source.setText(R.string.base_stat);
            } else {
                this.source.setText(source.getSourceString());
            }
        }
    }

    public static class SkillSourceAdapter extends RowWithSourceAdapter<Character.ProficientWithSource, SkillProfWithSourceViewHolder> {
        SkillSourceAdapter(@NonNull SkillBlockDialogFragment fragment, ListRetriever<Character.ProficientWithSource> listRetriever) {
            super(fragment.getMainActivity(), listRetriever);
        }


        @NonNull
        @Override
        protected SkillProfWithSourceViewHolder newViewHolder(View view) {
            return new SkillProfWithSourceViewHolder(view);
        }
    }


}
