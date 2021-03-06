package org.itsnat.droid.impl.xmlinflater.layout.stdalone;

import android.content.Context;

import org.itsnat.droid.AttrDrawableInflaterListener;
import org.itsnat.droid.AttrLayoutInflaterListener;
import org.itsnat.droid.impl.xmlinflated.layout.InflatedLayoutStandaloneImpl;
import org.itsnat.droid.impl.xmlinflater.layout.XMLInflaterLayout;

/**
 * Created by jmarranz on 4/11/14.
 */
public class XMLInflaterLayoutStandalone extends XMLInflaterLayout
{
    public XMLInflaterLayoutStandalone(InflatedLayoutStandaloneImpl inflatedXML,int bitmapDensityReference,AttrLayoutInflaterListener inflateLayoutListener,AttrDrawableInflaterListener attrDrawableInflaterListener,Context ctx)
    {
        super(inflatedXML,bitmapDensityReference,inflateLayoutListener,attrDrawableInflaterListener,ctx);
    }
}
