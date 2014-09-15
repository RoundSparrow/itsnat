package org.itsnat.droid.impl.xmlinflater.attr;

import android.animation.ObjectAnimator;
import android.widget.AdapterViewAnimator;

import org.itsnat.droid.impl.xmlinflater.classtree.ClassDescViewBased;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDesc_widget_AdapterViewAnimator_inAnimation extends AttrDesc_widget_AdapterViewAnimator_inoutAnimation_Base
{
    public AttrDesc_widget_AdapterViewAnimator_inAnimation(ClassDescViewBased parent)
    {
        super(parent,"inAnimation");
    }

    @Override
    protected void setAnimation(AdapterViewAnimator view, ObjectAnimator animator)
    {
        view.setInAnimation(animator);
    }

    @Override
    protected ObjectAnimator getDefaultAnimation()
    {
        return getDefaultInAnimation();
    }

    protected static ObjectAnimator getDefaultInAnimation()
    {
        // Copiado del código fuente
        ObjectAnimator anim = ObjectAnimator.ofFloat(null, "alpha", 0.0f, 1.0f);
        anim.setDuration(DEFAULT_ANIMATION_DURATION);
        return anim;
    }
}
