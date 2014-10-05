package org.itsnat.itsnatdroidtest.testact.local;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.TabHost;

import org.itsnat.droid.InflatedLayout;
import org.itsnat.itsnatdroidtest.R;
import org.itsnat.itsnatdroidtest.testact.TestActivity;
import org.itsnat.itsnatdroidtest.testact.TestActivityTabFragment;
import org.itsnat.itsnatdroidtest.testact.util.CustomScrollView;

import java.io.InputStream;

/**
 * Created by jmarranz on 16/07/14.
 */
public class TestLayoutLocal2 extends TestLayoutLocalBase
{
    public TestLayoutLocal2(TestActivityTabFragment fragment)
    {
        super(fragment);
    }

    public void test()
    {
        final TestActivity act = fragment.getTestActivity();
        final View compiledRootView = loadCompiledAndBindBackReloadButtons(R.layout.test_local_layout_compiled_2);

        final View buttonReload = compiledRootView.findViewById(R.id.buttonReload);
        buttonReload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // TEST de carga dinámica de layout guardado localmente
                InputStream input = act.getResources().openRawResource(R.raw.test_local_layout_dynamic_2);

                InflatedLayout layout = loadDynamicAndBindBackReloadButtons(input);
                View dynamicRootView = layout.getRootView();

                defineInitalData(act,dynamicRootView);

                TestLocalXMLInflate2.test((CustomScrollView) compiledRootView, (CustomScrollView) dynamicRootView);
            }
        });

        defineInitalData(act,compiledRootView);
    }

    private static void defineInitalData(TestActivity act,View rootView)
    {
        defineTabHost(act,rootView);
    }

    private static void defineTabHost(TestActivity act,View rootView)
    {
        Resources res = act.getResources();
        TabHost tabHost = (TabHost)rootView.findViewById(R.id.tabHostTest);
        tabHost.setup();

        setNewTab(act, tabHost, "tab1", "Tab 1", android.R.drawable.star_on, R.id.tab1);
        setNewTab(act, tabHost, "tab2", "Tab 2", android.R.drawable.star_on, R.id.tab2);
        setNewTab(act, tabHost, "tab3", "Tab 3", android.R.drawable.star_on, R.id.tab3);

        //tabHost.setCurrentTabByTag("tab2"); //-- optional to set a tab programmatically.
    }

    private static void setNewTab(Context context,final TabHost tabHost, String tag, String title, int icon,final int contentID)
    {
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setIndicator(title, context.getResources().getDrawable(icon));
        tabSpec.setContent(contentID);
        tabHost.addTab(tabSpec);
    }



}
