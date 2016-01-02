package com.oakonell.dndcharacter.views.background;

import com.oakonell.dndcharacter.model.background.Background;
import com.oakonell.dndcharacter.views.AbstractEditComponentDialogFragment;

/**
 * Created by Rob on 11/10/2015.
 */
public class EditBackgroundDialogFragment extends AbstractEditComponentDialogFragment<Background> {

    public static EditBackgroundDialogFragment create(Background background) {
        EditBackgroundDialogFragment frag = new EditBackgroundDialogFragment();
        frag.setModel(background);
        return frag;
    }
}
