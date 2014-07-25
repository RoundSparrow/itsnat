package org.itsnat.droid.impl.browser.clientdoc.evtlistadapter;

import android.view.KeyEvent;
import android.view.View;

import org.itsnat.droid.impl.browser.clientdoc.ItsNatViewImpl;

/**
 * Created by jmarranz on 24/07/14.
 */
public class KeyEventListenerViewAdapter extends EventListenerViewAdapter implements View.OnKeyListener
{
    protected View.OnKeyListener keyboardListener;

    public KeyEventListenerViewAdapter(ItsNatViewImpl viewData)
    {
        super(viewData);
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent)
    {
        String type = "";
        int action = keyEvent.getAction();
        switch(action)
        {
            case KeyEvent.ACTION_DOWN:
                type = "keydown";
                break;
            case KeyEvent.ACTION_UP:
                type = "keyup";
                break;
            // keypress ??
        }

        dispatch(type,keyEvent);

        boolean res = false;
        if (keyboardListener != null) res = keyboardListener.onKey(viewData.getView(), i, keyEvent);

        return res;
    }

    public void setOnKeyListener(View.OnKeyListener keyboardListener)
    {
        this.keyboardListener = keyboardListener;
    }
}