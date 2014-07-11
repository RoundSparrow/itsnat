package org.itsnat.droid.impl.browser.clientdoc;

import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.itsnat.droid.ItsNatDroidException;
import org.itsnat.droid.ItsNatDroidNetworkException;
import org.itsnat.droid.impl.browser.HttpUtil;
import org.itsnat.droid.impl.browser.ItsNatDroidBrowserImpl;
import org.itsnat.droid.impl.util.ValueUtil;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jmarranz on 10/07/14.
 */
public class EventSender
{
    protected EventManager evtManager;

    public EventSender(EventManager evtManager)
    {
        this.evtManager = evtManager;
    }

    public String requestSyncText(String servletPath,List<NameValuePair> params)
    {
        ItsNatDroidBrowserImpl browser = evtManager.getItsNatDocImpl().getPageImpl().getItsNatDroidBrowserImpl();

        HttpParams httpParamsRequest = null;

        StatusLine[] status = new StatusLine[1];
        byte[] result = HttpUtil.httpPost(servletPath, browser.getHttpContext(), httpParamsRequest, browser.getHttpParams(),params,status);
        if (status[0].getStatusCode() != 200)
            throw new ItsNatDroidNetworkException(status[0].getStatusCode(),status[0].getReasonPhrase(),result);

        return ValueUtil.toString(result);
    }
}
