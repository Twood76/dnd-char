package com.oakonell.dndcharacter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.oakonell.dndcharacter.model.*;
import com.oakonell.dndcharacter.model.Character;
import com.oakonell.dndcharacter.views.AbstractSheetFragment;
import com.oakonell.dndcharacter.views.FeatureBlockView;

/**
 * Created by Rob on 10/26/2015.
 */
public class FeaturesFragment extends AbstractSheetFragment {

    FeatureAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.feature_sheet, container, false);

        superCreateViews(rootView);

        adapter = new FeatureAdapter(this.getActivity(), character);
        GridView gridView = (GridView) rootView.findViewById(R.id.features);
        gridView.setAdapter(adapter);

        updateViews(rootView);

        return rootView;
    }


    private void updateViews(ViewGroup rootView) {
        super.updateViews(rootView);
        if (character == null) {
            adapter.notifyDataSetInvalidated();

            return;
        }
        adapter.notifyDataSetInvalidated();
    }

    public void setCharacter(Character character) {
        super.setCharacter(character);
        if (adapter != null) {
            adapter.setCharacter(character);
        }
    }


    public static class FeatureAdapter extends BaseAdapter {
        private Context context;
        Character character;

        public FeatureAdapter(Context context, Character character) {
            this.context = context;
            this.character = character;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            FeatureBlockView gridView;

            if (convertView == null) {
                gridView = new FeatureBlockView(context);
            } else {
                gridView = (FeatureBlockView) convertView;
            }
            gridView.setCharacter(character);
            gridView.setFeatureInfo(getItem(position));

            return gridView;
        }

        @Override
        public int getCount() {
            if (character == null) return 0;
            return character.getFeatureInfos().size();
        }

        @Override
        public FeatureInfo getItem(int position) {
            if (character == null) return null;
            return character.getFeatureInfos().get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void setCharacter(Character character) {
            if (this.character != character) {
                this.character = character;
                notifyDataSetInvalidated();
            }
        }
    }
}