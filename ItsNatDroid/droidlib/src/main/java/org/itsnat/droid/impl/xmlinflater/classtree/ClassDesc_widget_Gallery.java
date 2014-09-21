package org.itsnat.droid.impl.xmlinflater.classtree;

import org.itsnat.droid.impl.xmlinflater.attr.AttrDescReflecMethodDimensionInt;
import org.itsnat.droid.impl.xmlinflater.attr.AttrDescReflecMethodFloat;
import org.itsnat.droid.impl.xmlinflater.attr.AttrDescReflecMethodInt;
import org.itsnat.droid.impl.xmlinflater.attr.GravityUtil;
import org.itsnat.droid.impl.xmlinflater.attr.AttrDescReflecMultipleName;

/**
 * Created by jmarranz on 30/04/14.
 */
public class ClassDesc_widget_Gallery extends ClassDescViewBased
{
    public ClassDesc_widget_Gallery(ClassDesc_widget_AbsSpinner parentClass)
    {
        super("android.widget.Gallery",parentClass);
    }

    protected void init()
    {
        super.init();

        addAttrDesc(new AttrDescReflecMethodInt(this,"animationDuration",400));
        addAttrDesc(new AttrDescReflecMultipleName(this,"gravity", GravityUtil.valueMap,"left")); // No está claro que left sea el valor por defecto
        addAttrDesc(new AttrDescReflecMethodDimensionInt(this,"spacing",0f));
        addAttrDesc(new AttrDescReflecMethodFloat(this,"unselectedAlpha",0.5f));


    }
}

