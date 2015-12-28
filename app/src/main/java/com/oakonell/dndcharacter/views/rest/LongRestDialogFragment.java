package com.oakonell.dndcharacter.views.rest;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.oakonell.dndcharacter.R;
import com.oakonell.dndcharacter.model.Character;
import com.oakonell.dndcharacter.model.LongRestRequest;
import com.oakonell.dndcharacter.model.components.RefreshType;
import com.oakonell.dndcharacter.views.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Rob on 11/8/2015.
 */
public class LongRestDialogFragment extends AbstractRestDialogFragment {
    View fullHealingGroup;
    CheckBox fullHealing;
    private HitDiceRestoreAdapter diceAdapter;
    private RecyclerView hitDiceListView;

    public static LongRestDialogFragment createDialog() {
        return new LongRestDialogFragment();
    }

    @Override
    protected boolean allowExtraHealing() {
        return !fullHealing.isChecked();
    }

    @Override
    public View onCreateTheView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.long_rest_dialog, container);
        configureCommon(view);
        getDialog().setTitle("Long Rest");

        fullHealingGroup = view.findViewById(R.id.full_heal_group);
        fullHealing = (CheckBox) view.findViewById(R.id.full_healing);

        hitDiceListView = (RecyclerView) view.findViewById(R.id.hit_dice_restore_list);

        fullHealing.setChecked(true);

        fullHealing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                conditionallyShowExtraHealing();
                updateView();
            }
        });

        return view;
    }


    @Override
    protected boolean onDone() {
        boolean isValid = true;
        LongRestRequest request = new LongRestRequest();
        for (HitDieRestoreRow each : diceAdapter.diceCounts) {
            if (each.numDiceToRestore > 0) {
                request.restoreHitDice(each.dieSides, each.numDiceToRestore);
            }
        }
        request.setHealing(getHealing());

        isValid = isValid && updateCommonRequest(request);
        isValid = isValid && super.onDone();
        if (isValid) {
            getCharacter().longRest(request);
        }
        return isValid;
    }

    @Override
    public void onCharacterLoaded(Character character) {
        super.onCharacterLoaded(character);

        diceAdapter = new HitDiceRestoreAdapter(getActivity(), character);
        hitDiceListView.setAdapter(diceAdapter);

        hitDiceListView.setHasFixedSize(false);
        hitDiceListView.setLayoutManager(new org.solovyev.android.views.llm.LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        hitDiceListView.addItemDecoration(itemDecoration);


        updateView();
    }

    @Override
    public void onCharacterChanged(Character character) {
        updateView();
        diceAdapter.reloadList(character);
    }

    @Override
    public void updateView() {
        super.updateView();
        if (getCharacter().getHP() == getCharacter().getMaxHP()) {
            fullHealingGroup.setVisibility(View.GONE);
        } else {
            fullHealingGroup.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected boolean shouldReset(RefreshType refreshesOn) {
        return true;
    }


    @Override
    protected int getHealing() {
        if (fullHealing.isChecked()) {
            return getCharacter().getMaxHP() - getCharacter().getHP();
        }
        return getExtraHealing();
    }

    private class HitDieRestoreRow {
        public int dieSides;
        public int currentDiceRemaining;
        public int numDiceToRestore;
        public int totalDice;
    }

    private class HitDiceRestoreAdapter extends RecyclerView.Adapter<HitDiceRestoreViewHolder> {
        List<HitDieRestoreRow> diceCounts;
        private Context context;

        public HitDiceRestoreAdapter(Context context, Character character) {
            this.context = context;
            populateDiceCounts(character);
        }

        private void populateDiceCounts(Character character) {
            diceCounts = new ArrayList<>();
            for (Character.HitDieRow each : character.getHitDiceCounts()) {
                HitDieRestoreRow newRow = new HitDieRestoreRow();
                newRow.dieSides = each.dieSides;
                newRow.currentDiceRemaining = each.numDiceRemaining;
                newRow.totalDice = each.totalDice;
                newRow.numDiceToRestore = Math.min(Math.max(each.totalDice / 2, 1), each.totalDice - each.numDiceRemaining);

                diceCounts.add(newRow);
            }
        }


        public void reloadList(Character character) {
            populateDiceCounts(character);
            notifyDataSetChanged();
        }

        public HitDieRestoreRow getItem(int position) {
            return diceCounts.get(position);
        }

        @Override
        public HitDiceRestoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(context, R.layout.hit_dice_restore_item, null);
            HitDiceRestoreViewHolder viewHolder = new HitDiceRestoreViewHolder(view);
            viewHolder.dieSides = (TextView) view.findViewById(R.id.die);
            viewHolder.currentDiceRemaining = (TextView) view.findViewById(R.id.current_dice_remaining);
            viewHolder.numDiceToRestore = (TextView) view.findViewById(R.id.dice_to_restore);
            viewHolder.totalDice = (TextView) view.findViewById(R.id.total);
            viewHolder.resultDice = (TextView) view.findViewById(R.id.resultant_dice);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(HitDiceRestoreViewHolder viewHolder, int position) {
            final HitDieRestoreRow row = getItem(position);
            viewHolder.dieSides.setText(row.dieSides + "");
            viewHolder.currentDiceRemaining.setText(row.currentDiceRemaining + "");
            viewHolder.numDiceToRestore.setText(row.numDiceToRestore + "");
            viewHolder.totalDice.setText(row.totalDice + "");
            viewHolder.resultDice.setText((row.currentDiceRemaining + row.numDiceToRestore) + "");
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return diceCounts.size();
        }


    }

    static class HitDiceRestoreViewHolder extends RecyclerView.ViewHolder {
        TextView dieSides;
        TextView currentDiceRemaining;
        TextView numDiceToRestore;
        TextView totalDice;
        TextView resultDice;

        public HitDiceRestoreViewHolder(View itemView) {
            super(itemView);
        }
    }
}
