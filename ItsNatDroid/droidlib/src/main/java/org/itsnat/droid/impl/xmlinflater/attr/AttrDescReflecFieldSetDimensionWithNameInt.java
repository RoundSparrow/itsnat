package org.itsnat.droid.impl.xmlinflater.attr;

import android.view.View;

import org.itsnat.droid.impl.xmlinflater.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.classtree.ClassDescViewBased;


/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescReflecFieldSetDimensionWithNameInt extends AttrDescReflecFieldSet
{
    protected String defaultValue;

    public AttrDescReflecFieldSetDimensionWithNameInt(ClassDescViewBased parent, String name, String fieldName,String defaultValue)
    {
        super(parent,name,fieldName);
        this.defaultValue = defaultValue;
    }

    public void setAttribute(View view, String value, OneTimeAttrProcess oneTimeAttrProcess, PendingPostInsertChildrenTasks pending)
    {
        int convertedValue = getDimensionWithNameInt(view, value);

        setField(view,convertedValue);
    }

    public void removeAttribute(View view)
    {
        if (defaultValue != null)
            setAttribute(view,defaultValue,null,null);
    }


}