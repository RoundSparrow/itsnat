/*
  ItsNat Java Web Application Framework
  Copyright (C) 2007-2011 Jose Maria Arranz Santamaria, Spanish citizen

  This software is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 3 of
  the License, or (at your option) any later version.
  This software is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details. You should have received
  a copy of the GNU Lesser General Public License along with this program.
  If not, see <http://www.gnu.org/licenses/>.
*/

package org.itsnat.impl.core.jsren.listener;

import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.listener.ItsNatEventListenerWrapperImpl;
import org.itsnat.impl.core.listener.domext.ItsNatTimerEventListenerWrapperImpl;
import org.itsnat.impl.core.path.NodeLocationImpl;
import org.w3c.dom.Node;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public class JSRenderItsNatTimerEventListenerImpl extends JSRenderItsNatDOMExtEventListenerImpl
{
    public static final JSRenderItsNatTimerEventListenerImpl SINGLETON = new JSRenderItsNatTimerEventListenerImpl();

    /** Creates a new instance of JSRenderItsNatTimerEventListenerImpl */
    public JSRenderItsNatTimerEventListenerImpl()
    {
    }

    private String addItsNatTimerEventListenerCode(ItsNatTimerEventListenerWrapperImpl itsNatListener,ClientDocumentStfulImpl clientDoc)
    {
        StringBuilder code = new StringBuilder();

        long delay = itsNatListener.getDelayFirstTime();

        EventTarget currentTarget = itsNatListener.getCurrentTarget();

        String listenerId = itsNatListener.getId();
        int commMode = itsNatListener.getCommModeDeclared();
        long eventTimeout = getEventTimeout(itsNatListener,clientDoc);

        String functionVarName = addCustomCodeFunction(itsNatListener,code);

        NodeLocationImpl nodeLoc = clientDoc.getNodeLocation((Node)currentTarget,true);
        // Hay que tener en cuenta que currentTarget puede ser NULO
        code.append( "itsNatDoc.addTimerEventListener(" + nodeLoc.toJSNodeLocation(false) + ",\"" + listenerId + "\"," + functionVarName + "," + commMode + "," + eventTimeout + "," + delay + ");\n" );

        return code.toString();
    }

    private String removeItsNatTimerEventListenerCode(ItsNatTimerEventListenerWrapperImpl itsNatListener,ClientDocumentStfulImpl clientDoc)
    {
        String listenerId = itsNatListener.getId();

        StringBuilder code = new StringBuilder();

        code.append( "\n" );
        code.append( "itsNatDoc.removeTimerEventListener(\"" + listenerId + "\");\n" );

        return code.toString();
    }

    public void updateItsNatTimerEventListenerCode(ItsNatTimerEventListenerWrapperImpl itsNatListener,long computedPeriod,ClientDocumentStfulImpl clientDoc)
    {
        String listenerId = itsNatListener.getId();

        StringBuilder code = new StringBuilder();

        code.append( "\n" );
        code.append( "itsNatDoc.updateTimerEventListener(\"" + listenerId + "\"," + computedPeriod + ");\n" );

        clientDoc.addCodeToSend(code.toString());
    }

    protected String addItsNatEventListenerCodeInherit(ItsNatEventListenerWrapperImpl itsNatListener,ClientDocumentStfulImpl clientDoc)
    {
        return addItsNatTimerEventListenerCode((ItsNatTimerEventListenerWrapperImpl)itsNatListener,clientDoc);
    }

    protected String removeItsNatEventListenerCodeInherit(ItsNatEventListenerWrapperImpl itsNatListener,ClientDocumentStfulImpl clientDoc)
    {
        return removeItsNatTimerEventListenerCode((ItsNatTimerEventListenerWrapperImpl)itsNatListener,clientDoc);
    }
}
