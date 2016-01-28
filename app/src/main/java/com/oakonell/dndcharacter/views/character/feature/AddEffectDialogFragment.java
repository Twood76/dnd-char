package com.oakonell.dndcharacter.views.character.feature;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.activeandroid.Model;
import com.oakonell.dndcharacter.R;
import com.oakonell.dndcharacter.model.character.CharacterEffect;
import com.oakonell.dndcharacter.model.effect.AddEffectToCharacterVisitor;
import com.oakonell.dndcharacter.model.effect.Effect;
import com.oakonell.dndcharacter.views.character.AbstractAddComponentDialogFragment;

/**
 * Created by Rob on 1/3/2016.
 */
public class AddEffectDialogFragment extends AbstractAddComponentDialogFragment<AbstractAddComponentDialogFragment.RowViewHolder> {


    @NonNull
    public static AddEffectDialogFragment createDialog() {
        return new AddEffectDialogFragment();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.add_effect);
    }

    @NonNull
    @Override
    public Class<? extends Model> getComponentClass() {
        return Effect.class;
    }

    @Override
    protected boolean applyAction(long id) {
        Effect effect = Effect.load(Effect.class, id);

        final String name = effect.getName();
        final CharacterEffect existingEffect = getCharacter().getEffectNamed(name);
        if (existingEffect != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.already_has_effect, name));
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(@NonNull DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return false;
        }

        CharacterEffect charEffect = AddEffectToCharacterVisitor.applyToCharacter(effect, getCharacter());
        getMainActivity().updateViews();
        getMainActivity().saveCharacter();

        return true;
    }


    @NonNull
    @Override
    public RowViewHolder newRowViewHolder(View newView) {
        return new RowViewHolder(newView);
    }


}
