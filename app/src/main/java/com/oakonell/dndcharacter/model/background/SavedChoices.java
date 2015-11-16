package com.oakonell.dndcharacter.model.background;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rob on 11/9/2015.
 */
public class SavedChoices {
    @ElementMap(entry = "choice", key = "name", value = "selections", required = false)
    Map<String, SavedSelections> choices = new HashMap<>();

    // TODO use a wrapper for SimpleXML serialization
    public static class SavedSelections {
        @ElementList(required = false,type=String.class, inline=true)
        List<String> selections = new ArrayList<>();
    }

    public List<String> getChoicesFor(String choiceName) {
        SavedSelections selections = choices.get(choiceName);
        if (selections == null) {
            selections = new SavedSelections();
            choices.put(choiceName, selections);
        }
        return selections.selections;
    }
}
