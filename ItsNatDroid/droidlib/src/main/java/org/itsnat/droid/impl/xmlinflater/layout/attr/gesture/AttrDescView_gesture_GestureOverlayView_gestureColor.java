package org.itsnat.droid.impl.xmlinflater.layout.attr.gesture;

import android.graphics.Paint;
import android.view.View;

import org.itsnat.droid.impl.xmlinflater.layout.attr.AttrDescViewReflecMethodColor;
import org.itsnat.droid.impl.xmlinflater.layout.attr.FieldContainer;
import org.itsnat.droid.impl.xmlinflater.layout.attr.MethodContainer;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescView_gesture_GestureOverlayView_gestureColor extends AttrDescViewReflecMethodColor
{
    protected FieldContainer<Paint> fieldGesturePaint;
    protected MethodContainer<Void> methodPaintSetColor;
    protected FieldContainer<Integer> fieldCurrentColor;

    public AttrDescView_gesture_GestureOverlayView_gestureColor(ClassDescViewBased parent)
    {
        super(parent,"gestureColor","#ffff00"/*yellow*/);

        this.fieldGesturePaint = new FieldContainer<Paint>(parent,"mGesturePaint");
        this.methodPaintSetColor = new MethodContainer<Void>(fieldGesturePaint.getField().getType(),"setColor",new Class[]{int.class});
        this.fieldCurrentColor = new FieldContainer<Integer>(parent,"mCurrentColor");
    }

    protected void callMethod(View view, Object convertedValue)
    {
        super.callMethod(view,convertedValue);

        // Tenemos que hacer más cosas:
        Paint paint = fieldGesturePaint.get(view);
        methodPaintSetColor.invoke(paint,(Integer)convertedValue);

        fieldCurrentColor.set(view,(Integer)convertedValue);
    }

}
