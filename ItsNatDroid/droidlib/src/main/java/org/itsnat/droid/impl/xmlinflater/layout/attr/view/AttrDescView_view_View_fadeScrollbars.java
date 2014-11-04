package org.itsnat.droid.impl.xmlinflater.layout.attr.view;

import android.view.View;

import org.itsnat.droid.impl.xmlinflater.layout.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.layout.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.layout.attr.AttrDescViewReflecMethodBoolean;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;


/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescView_view_View_fadeScrollbars extends AttrDescViewReflecMethodBoolean
{
    public AttrDescView_view_View_fadeScrollbars(ClassDescViewBased parent)
    {
        super(parent,"fadeScrollbars","setScrollbarFadingEnabled",true);
    }

    public void setAttribute(final View view,final String value, final OneTimeAttrProcess oneTimeAttrProcess,final PendingPostInsertChildrenTasks pending)
    {
        if (oneTimeAttrProcess != null)
        {
            // Delegamos al final para que esté totalmente claro si hay o no scrollbars
            oneTimeAttrProcess.addLastTask(new Runnable()
            {
                @Override
                public void run()
                {
                    AttrDescView_view_View_fadeScrollbars.super.setAttribute(view,value,oneTimeAttrProcess,pending);
                }
            });
        }
        else
        {
            super.setAttribute(view,value,oneTimeAttrProcess,pending);
        }
    }

}
