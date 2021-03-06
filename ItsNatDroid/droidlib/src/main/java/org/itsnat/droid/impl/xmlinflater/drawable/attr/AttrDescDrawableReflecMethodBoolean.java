package org.itsnat.droid.impl.xmlinflater.drawable.attr;

import android.content.Context;

import org.itsnat.droid.impl.dom.DOMAttr;
import org.itsnat.droid.impl.xmlinflater.drawable.XMLInflaterDrawable;
import org.itsnat.droid.impl.xmlinflater.drawable.classtree.ClassDescDrawable;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescDrawableReflecMethodBoolean<Tdrawable> extends AttrDescDrawableReflecMethod<Tdrawable>
{
    public AttrDescDrawableReflecMethodBoolean(ClassDescDrawable parent, String name, String methodName)
    {
        super(parent,name,methodName,getClassParam());
    }

    public AttrDescDrawableReflecMethodBoolean(ClassDescDrawable parent, String name)
    {
        super(parent,name,getClassParam());
    }

    protected static Class<?> getClassParam()
    {
        return boolean.class;
    }

    @Override
    public void setAttribute(Tdrawable draw, DOMAttr attr,XMLInflaterDrawable xmlInflaterDrawable,Context ctx)
    {
        boolean convValue = getBoolean(attr.getValue(),ctx);
        callMethod(draw, convValue);
    }
}
