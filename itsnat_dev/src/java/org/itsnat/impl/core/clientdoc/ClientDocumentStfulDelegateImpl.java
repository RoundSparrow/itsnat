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

package org.itsnat.impl.core.clientdoc;

import org.itsnat.core.ItsNatException;
import org.itsnat.core.script.ScriptUtil;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.droid.BrowserDroid;
import org.itsnat.impl.core.browser.web.BrowserWeb;
import org.itsnat.impl.core.clientdoc.droid.ClientDocumentStfulDelegateDroidImpl;
import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.mut.client.ClientMutationEventListenerStfulImpl;
import org.itsnat.impl.core.dompath.DOMPathResolver;
import org.itsnat.impl.core.dompath.NodeLocationImpl;
import org.itsnat.impl.core.dompath.NodeLocationWithParentImpl;
import org.itsnat.impl.core.scriptren.jsren.dom.node.JSRenderNodeImpl;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public abstract class ClientDocumentStfulDelegateImpl
{
    protected ClientDocumentStfulImpl clientDoc;
    protected ClientMutationEventListenerStfulImpl mutationListener; 
    protected NodeCacheRegistryImpl nodeCache;     
    protected DOMPathResolver pathResolver;
    
    public ClientDocumentStfulDelegateImpl(ClientDocumentStfulImpl clientDoc)
    {
        this.clientDoc = clientDoc;
        this.mutationListener = ClientMutationEventListenerStfulImpl.createClientMutationEventListenerStful(this); 
        this.pathResolver = DOMPathResolver.createDOMPathResolver(this);        
        // A d�a de hoy s�lo los documentos HTML y SVG generan JavaScript necesario para mantener un cach� de nodos en el cliente
        if (clientDoc.getItsNatStfulDocument().isNodeCacheEnabled())
            this.nodeCache = new NodeCacheRegistryImpl(this);          
    }

    public static ClientDocumentStfulDelegateImpl createClientDocumentStfulDelegate(ClientDocumentStfulImpl clientDoc)
    {
        Browser browser = clientDoc.getBrowser();
        if (browser instanceof BrowserWeb)
            return new ClientDocumentStfulDelegateWebImpl(clientDoc);
        else if (browser instanceof BrowserDroid)
            return new ClientDocumentStfulDelegateDroidImpl(clientDoc);        
        else
            return null;
    }
    
    public ClientDocumentStfulImpl getClientDocumentStful()
    {
        return clientDoc;
    }
    
    public Browser getBrowser()
    {
        return clientDoc.getBrowser();
    }      
    
    public DOMPathResolver getDOMPathResolver()
    {
        return pathResolver;
    }        
    
    public ItsNatStfulDocumentImpl getItsNatStfulDocument()
    {
        return clientDoc.getItsNatStfulDocument();
    }
        
    public ClientMutationEventListenerStfulImpl getClientMutationEventListenerStful()
    {
        return mutationListener;
    }        
    
    public boolean isNodeCacheEnabled()
    {
        return nodeCache != null;
    }

    public NodeCacheRegistryImpl getNodeCacheRegistry()
    {
        return nodeCache; // puede ser null (no cach�)
    }    
    
    public void addCodeToSend(Object code)
    {
        clientDoc.addCodeToSend(code);
    }
    
    public String getCachedNodeId(Node node)
    {
        NodeCacheRegistryImpl cacheNode = getNodeCacheRegistry();
        if (cacheNode == null) return null;
        return cacheNode.getId(node);
    }        
    
    public String removeNodeFromCache(Node node)
    {
        NodeCacheRegistryImpl cacheNode = getNodeCacheRegistry();
        if (cacheNode == null)
            return null;
        return cacheNode.removeNode(node);
    }        
    
    public Node getNodeFromPath(String pathStr,Node topParent)
    {
        return getDOMPathResolver().getNodeFromPath(pathStr,topParent);
    }    
    
    public String getRelativeStringPathFromNodeParent(Node child)
    {
        return getDOMPathResolver().getRelativeStringPathFromNodeParent(child);
    }    
    
    public String getStringPathFromNode(Node node)
    {
        return getDOMPathResolver().getStringPathFromNode(node);
    }    
    
    public String getStringPathFromNode(Node node,Node topParent)
    {
        return getDOMPathResolver().getStringPathFromNode(node,topParent);
    }    
    
    public Node getNextSiblingInClientDOM(Node node)
    {
        return getDOMPathResolver().getNextSiblingInClientDOM(node);        
    }        
    
    public NodeLocationImpl getNodeLocation(Node node,boolean cacheIfPossible)
    {
        return NodeLocationImpl.getNodeLocation(this,node,cacheIfPossible);
    }    
    
    public NodeLocationImpl getRefNodeLocationInsertBefore(Node newNode,Node nextSibling)
    {
        return NodeLocationImpl.getRefNodeLocationInsertBefore(this, newNode, nextSibling);
    }    
    
    public String removeNodeFromCacheAndSendCode(Node node)
    {    
        String oldId = removeNodeFromCache(node);
        if (oldId == null) return null;
         // Estaba cacheado
        addCodeToSend( renderRemoveNodeFromCache(oldId) );          
        return oldId;
    }        
    
    public Node getNodeFromStringPathFromClient(String pathStr,boolean cacheIfPossible)
    {
        if (pathStr.equals("null"))
            return null;

        NodeCacheRegistryImpl nodeCache = getNodeCacheRegistry();

        // El pathStr es generado por el navegador
        if (pathStr.startsWith("id:"))
        {
            // Formato: id:idvalue
            String id = pathStr.substring("id:".length());
            return nodeCache.getNodeById(id); // La cach� debe estar activado s� o s�
        }
        else
        {
            // Nodo no cacheado
            Node node;
            String path;
            Node parent;
            String parentId;

            if (pathStr.startsWith("pid:"))
            {
                // El nodo no est� cacheado pero el padre s�.
                // Formato: pid:idparent:pathrel
                int posSepPath = pathStr.lastIndexOf(':');
                parentId = pathStr.substring("pid:".length(),posSepPath);
                path = pathStr.substring(posSepPath + 1);
                // La cach� debe estar activada s� o s�
                parent = nodeCache.getNodeById(parentId);  // parent no puede ser null
                if (parent == null) throw new ItsNatException("INTERNAL ERROR");
            }
            else
            {
                // Formato: pathabs
                path = pathStr;
                parent = null;
                parentId = null;
            }

            node = getNodeFromPath(path,parent);

            // En teor�a node ha de encontrarse pues existe en el cliente
            // pero hay un caso en el que s� que puede ser null
            // y aun as� ser tolerable y es el caso en el que en el servidor el nodo (y algunos padres)
            // no est� porque est� cacheado porque est� en un sub�rbol est�tico
            // Es el caso por ejemplo de listener asociado a un elemento
            // cuyos hijos son est�ticos y cacheados en el servidor, el currentTarget
            // no es nulo pero s� puede ser el nodo "target" que puede ser un nodo
            // bajo el currentTarget cacheado en el servidor. El m�todo getTarget()
            // devolver� nulo lo cual puede ser aceptable (el programador
            // deber� investigar que la causa es que en el servidor no est� por ser cacheado).
            if (node == null) return null;

            if (cacheIfPossible && (nodeCache != null))
            {
                // Intentamos guardar en la cach� y enviamos al cliente, as� mejoramos el rendimiento
                // Hay que tener en cuenta que el nodo no est� cacheado en el cliente
                // pero quiz�s al resolver otra referencia anteriormente que apunta al mismo
                // nodo es posible que ya lo hayamos cacheado en el servidor, por lo que
                // evitamos un intento de cachear de nuevo (pues da error).
                String id = nodeCache.getId(node);
                if (id != null) return node; // Ya fue cacheado

                id = nodeCache.addNode(node);  // node no puede ser null
                if (id != null) // Si es null es que el nodo no es cacheable o la cach� est� bloqueada
                {
                    NodeLocationWithParentImpl nodeLoc = NodeLocationWithParentImpl.getNodeLocationWithParent(node,id,path,parent,parentId,true,this);
                    addCodeToSend( renderAddNodeToCache(nodeLoc) );
                }
            }

            return node;
        }
    }        
    
    public abstract ScriptUtil createScriptUtil();
    public abstract boolean dispatchEvent(EventTarget target,Event evt,int commMode,long eventTimeout) throws EventException;   
    public abstract String getNodeReference(Node node,boolean cacheIfPossible,boolean errIfNull);    
    protected abstract String renderAddNodeToCache(NodeLocationWithParentImpl nodeLoc);    
    protected abstract String renderRemoveNodeFromCache(String id);
}
