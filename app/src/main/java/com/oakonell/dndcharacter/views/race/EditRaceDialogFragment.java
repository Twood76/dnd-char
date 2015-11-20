package com.oakonell.dndcharacter.views.race;

import com.oakonell.dndcharacter.model.race.Race;
import com.oakonell.dndcharacter.views.AbstractEditComponentDialogFragment;

/**
 * Created by Rob on 11/19/2015.
 */
public class EditRaceDialogFragment extends AbstractEditComponentDialogFragment {

    public static EditRaceDialogFragment create(Race race) {
        EditRaceDialogFragment frag = new EditRaceDialogFragment();
        frag.setModel(race);
        return frag;
    }
}
