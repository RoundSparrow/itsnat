/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.itsnat.impl.core.event.client.droid;

import org.itsnat.core.event.droid.InputEvent;
import org.itsnat.impl.core.listener.droid.ItsNatDroidEventListenerWrapperImpl;
import org.itsnat.impl.core.req.norm.RequestNormalEventImpl;

/**
 *
 * @author jmarranz
 */
public abstract class ClientItsNatDroidInputEventImpl extends ClientItsNatDroidEventImpl implements InputEvent
{

    public ClientItsNatDroidInputEventImpl(ItsNatDroidEventListenerWrapperImpl listenerWrapper, RequestNormalEventImpl request)
    {
        super(listenerWrapper, request);
    }
    
}