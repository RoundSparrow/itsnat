package org.itsnat.droid.impl.xmlinflater.layout.attr.widget;

import android.content.Context;
import android.view.View;

import org.itsnat.droid.impl.dom.DOMAttr;
import org.itsnat.droid.impl.xmlinflater.layout.OneTimeAttrProcess;
import org.itsnat.droid.impl.xmlinflater.layout.PendingPostInsertChildrenTasks;
import org.itsnat.droid.impl.xmlinflater.layout.XMLInflaterLayout;
import org.itsnat.droid.impl.xmlinflater.layout.attr.AttrDescViewReflecMethodBoolean;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;


/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescView_widget_TextView_textAllCaps extends AttrDescViewReflecMethodBoolean
{
    public AttrDescView_widget_TextView_textAllCaps(ClassDescViewBased parent)
    {
        super(parent,"textAllCaps","setAllCaps",false);
    }

    public void setAttribute(final View view, final DOMAttr attr,final XMLInflaterLayout xmlInflaterLayout, final Context ctx, final OneTimeAttrProcess oneTimeAttrProcess, final PendingPostInsertChildrenTasks pending)
    {
        if (oneTimeAttrProcess != null)
        {
            // Delegamos al final porque es necesario que antes se ejecute textIsSelectable="false"
            oneTimeAttrProcess.addLastTask(new Runnable()
            {
                @Override
                public void run()
                {
                    AttrDescView_widget_TextView_textAllCaps.super.setAttribute(view,attr,xmlInflaterLayout, ctx, oneTimeAttrProcess, pending);
                }
            });
        }
        else
        {
            super.setAttribute(view,attr, xmlInflaterLayout, ctx, oneTimeAttrProcess,pending);
        }
    }

}
