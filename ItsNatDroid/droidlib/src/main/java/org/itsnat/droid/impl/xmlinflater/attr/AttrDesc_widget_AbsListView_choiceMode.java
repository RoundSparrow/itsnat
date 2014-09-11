package org.itsnat.droid.impl.xmlinflater.attr;

import android.widget.AbsListView;

import org.itsnat.droid.impl.xmlinflater.classtree.ClassDescViewBased;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDesc_widget_AbsListView_choiceMode extends AttrDescReflecSingleName
{
    static Map<String, Integer> valueMap = new HashMap<String, Integer>();
    static
    {
        valueMap.put("none", AbsListView.CHOICE_MODE_NONE);
        valueMap.put("singleChoice",AbsListView.CHOICE_MODE_SINGLE);
        valueMap.put("multipleChoice",AbsListView.CHOICE_MODE_MULTIPLE);
        valueMap.put("multipleChoiceModal",AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
    }

    public AttrDesc_widget_AbsListView_choiceMode(ClassDescViewBased parent)
    {
        super(parent,"choiceMode",valueMap,"none");
    }


}