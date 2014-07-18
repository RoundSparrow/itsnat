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

package org.itsnat.impl.core.registry.dom;

import org.itsnat.impl.core.listener.dom.ItsNatDOMEventListenerWrapperImpl;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedClientImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.droid.ClientDocumentStfulDelegateDroidImpl;
import org.itsnat.impl.core.clientdoc.web.SVGWebInfoImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.event.EventListenerInternal;
import org.itsnat.impl.core.registry.EventListenerRegistryImpl;
import org.itsnat.impl.core.util.MapUniqueId;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.views.AbstractView;
import org.w3c.dom.views.DocumentView;

/**
 *
 * @author jmarranz
 */
public abstract class ItsNatDOMEventListenerRegistryImpl extends EventListenerRegistryImpl implements Serializable
{
    protected ItsNatStfulDocumentImpl itsNatDoc;
    protected ClientDocumentStfulImpl clientDocTarget; // Si es null es que el registro es a nivel de documento y pertenece a todos los clientes
    protected int capturingCount;
    protected MapUniqueId<ItsNatDOMEventListenerWrapperImpl> eventListenersById; // No es weak porque necesitamos sujetar el listener wrapper pues es un objeto de uso interno para este fin

    /**
     * Creates a new instance of ItsNatDOMEventListenerRegistryImpl
     */
    public ItsNatDOMEventListenerRegistryImpl(ItsNatStfulDocumentImpl itsNatDoc,ClientDocumentStfulImpl clientDoc)
    {
        this.itsNatDoc = itsNatDoc;
        this.clientDocTarget = clientDoc; // puede ser null

        this.eventListenersById = new MapUniqueId<ItsNatDOMEventListenerWrapperImpl>(itsNatDoc.getUniqueIdGenerator());
    }

    public ClientDocumentStfulImpl getClientDocumentStfulTarget()
    {
        return clientDocTarget; // puede ser null
    }
    
    public ClientDocumentStfulDelegateImpl getClientDocumentStfulDelegateFromDocument()
    {
        return ((ClientDocumentStfulImpl)itsNatDoc.getClientDocumentOwnerImpl()).getClientDocumentStfulDelegate();
    }    
    
    public ClientDocumentStfulDelegateImpl getClientDocumentStfulDelegateTarget()
    {    
        // Puede ser null si clientDocTarget es null
        return clientDocTarget != null ? clientDocTarget.getClientDocumentStfulDelegate() : null;
    }
    
    public boolean isEmpty()
    {
        return eventListenersById.isEmpty();
    }

    public void checkValidEventTarget(EventTarget target)
    {
        isValidEventTarget(target,true);
    }

    public boolean isValidEventTarget(EventTarget target,boolean throwErr)
    {
        // MSIE no admite asociar eventos DOM a text nodes y FireFox lo permite pero no los procesa por ej. los clicks
        // En los comentarios s� se permite aunque es absurdo (no pueden ser pulsados etc) delegamos en el programador
        if (target == null) return true; // Derivar si no se permite
        Node node = (Node)target; // nuestro AbstractView implementa Node
        int type = node.getNodeType();
        if (type == Node.TEXT_NODE)
            if (throwErr) throw new ItsNatException("Text node is not allowed",target);
            else return false;
        return true;
    }

    public int getCapturingCount()
    {
        return capturingCount;
    }

    public boolean canAddItsNatDOMEventListener(EventTarget target,EventListener listener)
    {
        if (!ItsNatDOMEventListenerWrapperImpl.canAddItsNatNormalEventListenerWrapper(listener,itsNatDoc, clientDocTarget))
            return false;
        checkValidEventTarget(target); // Lanza excepci�n si no es v�lido
        return true;
    }

    protected void addItsNatDOMEventListener(ItsNatDOMEventListenerWrapperImpl listenerWrapper)
    {
        eventListenersById.put(listenerWrapper);

        addItsNatEventListenerCode(listenerWrapper,getClientDocumentStfulDelegateTarget());
                
        if (listenerWrapper.getUseCapture())
            capturingCount++;
    }

    public void removeItsNatDOMEventListener(ItsNatDOMEventListenerWrapperImpl listener,boolean updateClient,boolean expunged)
    {
        // expunged tiene sentido en clases derivadas
        ItsNatDOMEventListenerWrapperImpl listenerRes = removeItsNatDOMEventListenerByIdPrivate(listener.getId(),updateClient);
        if (listenerRes == null)
            return; // Ya se elimin� seguramente
        if (listenerRes != listener)
            throw new ItsNatException("INTERNAL ERROR");
    }

    private ItsNatDOMEventListenerWrapperImpl removeItsNatDOMEventListenerByIdPrivate(String id,boolean updateClient)
    {
        ItsNatDOMEventListenerWrapperImpl listenerWrapper = eventListenersById.removeById(id);
        if (listenerWrapper == null)
            return null; // Ya se elimin� o nunca se a�adi� (raro)

        if (updateClient)
        {
            removeItsNatEventListenerCode(listenerWrapper,getClientDocumentStfulDelegateTarget());           
        }
        
        if (listenerWrapper.getUseCapture())
            capturingCount--;

        return listenerWrapper;
    }
   
    
    protected ItsNatDOMEventListenerWrapperImpl removeItsNatDOMEventListenerById(String id,boolean updateClient)
    {
        return removeItsNatDOMEventListenerByIdPrivate(id,updateClient);
    }

    public ItsNatDOMEventListenerWrapperImpl getItsNatDOMEventListenerById(String listenerId)
    {
        return eventListenersById.get(listenerId);
    }

    /**
     * Devolvemos un array y no un iterador o una colecci�n interna porque al procesar los
     * handlers es posible que se a�adan o se quiten listenerList (el iterador fallar�a)
     */
    public abstract EventListener[] getEventListenersArrayCopy(EventTarget target,String type,boolean useCapture);

    public void renderItsNatDOMEventListeners(final ClientDocumentAttachedClientImpl clientDoc)
    {
        // Usado para renderizar los listeners de documento para un cliente nuevo
        if ((clientDoc == null) || (getClientDocumentStfulTarget() != null))
            throw new ItsNatException("INTERNAL ERROR");

        if (eventListenersById.isEmpty()) return;

        LinkedList<ItsNatDOMEventListenerWrapperImpl> svgWebNodes = null;
        for(Map.Entry<String,ItsNatDOMEventListenerWrapperImpl> entry : eventListenersById.entrySet())
        {
            ItsNatDOMEventListenerWrapperImpl listenerWrapper = entry.getValue();

            if (!clientDoc.canReceiveNormalEvents(listenerWrapper))
                continue;

            ClientDocumentStfulDelegateImpl clientDocDeleg = clientDoc.getClientDocumentStfulDelegate();
            
            if (clientDocDeleg instanceof ClientDocumentStfulDelegateWebImpl)
            {
                EventTarget currTarget = listenerWrapper.getCurrentTarget();
                if (SVGWebInfoImpl.isSVGNodeProcessedBySVGWebFlash((Node)currTarget,(ClientDocumentStfulDelegateWebImpl)clientDocDeleg))
                {
                    // Si el nodo es procesado por SVGWeb Flash no podemos enviar ahora el registro del listener
                    // al cliente pues el nodo SVG no existe hasta que se renderize lo cual
                    // sabemos que ha ocurrido tras el evento SVGLoad de la p�gina.
                    if (svgWebNodes == null) svgWebNodes = new LinkedList<ItsNatDOMEventListenerWrapperImpl>();
                    svgWebNodes.add(listenerWrapper);
                    continue;
                }

                addItsNatEventListenerCode(listenerWrapper,clientDocDeleg);
            }
            else if (clientDocDeleg instanceof ClientDocumentStfulDelegateDroidImpl)
            {
                addItsNatEventListenerCode(listenerWrapper,clientDocDeleg);         
            }
        }

        if (svgWebNodes != null)
        {
            final LinkedList<ItsNatDOMEventListenerWrapperImpl> svgWebNodes2 = svgWebNodes;
            EventListener listener = new EventListenerInternal()
            {
                public void handleEvent(Event evt)
                {
                    for(ItsNatDOMEventListenerWrapperImpl listenerWrapper : svgWebNodes2)
                    {
                        if (!eventListenersById.containsKey(listenerWrapper)) 
                            continue; // Ha sido eliminado mientras se procesaba el evento SVGLoad

                        ClientDocumentStfulDelegateImpl clientDocDeleg = clientDoc.getClientDocumentStfulDelegate();                        
                        
                        addItsNatEventListenerCode(listenerWrapper,clientDocDeleg);                        
                    }
                }
            };
            AbstractView view = ((DocumentView)itsNatDoc.getDocument()).getDefaultView();
            clientDoc.addEventListener((EventTarget)view,"SVGLoad",listener,false);
        }
    }

    public void removeAllItsNatDOMEventListeners(boolean updateClient)
    {
        if (eventListenersById.isEmpty()) return;

        ItsNatDOMEventListenerWrapperImpl[] listenerList = eventListenersById.toArray(new ItsNatDOMEventListenerWrapperImpl[eventListenersById.size()]);
        
        for(int i = 0; i < listenerList.length; i++)
        {
            ItsNatDOMEventListenerWrapperImpl listener = listenerList[i];
            removeItsNatDOMEventListener(listener,updateClient,false);
        }
    }
}
