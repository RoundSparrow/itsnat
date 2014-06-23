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

package org.itsnat.impl.core.mut.client.web;

import org.itsnat.impl.core.clientdoc.web.ClientDocumentStfulDelegateWebImpl;
import org.itsnat.impl.core.domutil.DOMUtilHTML;
import org.w3c.dom.Node;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.html.HTMLSelectElement;

/**
 *
 * @author jmarranz
 */
public class ClientMutationEventListenerHTMLWebKitS40Impl extends ClientMutationEventListenerHTMLWebKitImpl
{
    public ClientMutationEventListenerHTMLWebKitS40Impl(ClientDocumentStfulDelegateWebImpl clientDoc)
    {
        super(clientDoc);
    }

    @Override
    public void postRenderAndSendMutationCode(MutationEvent mutEvent)
    {
        super.postRenderAndSendMutationCode(mutEvent);

        String type = mutEvent.getType();
        if ( type.equals("DOMNodeInserted")||type.equals("DOMNodeRemoved") )
        {
            Node node = (Node)mutEvent.getTarget(); // node es el nuevo o a eliminar
            if (DOMUtilHTML.isHTMLOptionOfSelectMultipleOrWithSize(node))
            {
                HTMLSelectElement select = (HTMLSelectElement)node.getParentNode();
                fixHTMLSelectMultipleOrWithSize(select);
            }
        }
    }

    private void fixHTMLSelectMultipleOrWithSize(HTMLSelectElement select)
    {
        // El S40WebKit tiene un error visual tonto: cuando se a�ade una nueva opci�n a un <select>
        // el scroll no se actualiza por lo que no podemos llegar con scroll a los elementos que no est�n en la parte visible
        // Se soluciona eliminando el SELECT e insertando de nuevo, as� renderiza completamente el control
        // y adapta el scroll a los elementos. Como es el mismo nodo no hay problema de actualizar listeners etc
        // En el caso de quitar un elemento tampoco se actualiza el control, pero
        // esto no es tan grave pues no impide seleccionar.
        ClientDocumentStfulDelegateWebImpl clientDoc = getClientDocumentStfulDelegateWeb();
        
        StringBuilder code = new StringBuilder();
        code.append("var elem = " + clientDoc.getNodeReference(select,true,true) + ";\n"); // elem es un <SELECT>
        code.append("var elemClone = elem.cloneNode(true);\n"); // El deep = true no es necesario pero lo ponemos para evitar un posible "parpadeo" al insertar un select vac�o
        code.append("elem.parentNode.replaceChild(elemClone,elem);\n");
        code.append("elemClone.parentNode.replaceChild(elem,elemClone);\n");

        clientDoc.addCodeToSend(code.toString());
    }
}
