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

package org.itsnat.impl.core.scriptren.bsren.node;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulDelegateImpl;
import org.itsnat.impl.core.clientdoc.droid.ClientDocumentStfulDelegateDroidImpl;
import org.itsnat.impl.core.scriptren.shared.dom.node.InsertAsMarkupInfoImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author jmarranz
 */
public class BSRenderElementImpl extends BSRenderHasChildrenNodeImpl //implements NodeConstraints
{
    public static final BSRenderElementImpl SINGLETON = new BSRenderElementImpl();
    
    /** Creates a new instance of BSRenderElementImpl */
    public BSRenderElementImpl()
    {
    }

    public static BSRenderElementImpl getBSRenderElement()
    {
        return BSRenderElementImpl.SINGLETON;
    }

    public String createNodeCode(Node node,ClientDocumentStfulDelegateImpl clientDoc)
    {
        Element nodeElem = (Element)node;
        return createElement(nodeElem,clientDoc);
    }

    protected String createElement(Element nodeElem,ClientDocumentStfulDelegateImpl clientDoc)
    {
        String tagName = nodeElem.getTagName();
        return createElement(nodeElem,tagName,clientDoc);
    }

    protected String createElement(Element nodeElem,String tagName,ClientDocumentStfulDelegateImpl clientDoc)
    {
        String namespaceURI = nodeElem.getNamespaceURI();
        if (namespaceURI != null)
        {
            String namespaceURIScript = shortNamespaceURI(namespaceURI);            
            return "itsNatDoc.createElementNS(" + namespaceURIScript + ",\"" + tagName + "\")";
        }
        else
            return "itsNatDoc.createElement(\"" + tagName + "\")";      
    }

    public String addAttributesBeforeInsertNode(Node node,String elemVarName,ClientDocumentStfulDelegateImpl clientDoc)
    {
        // En Droid la renderizaci�n de atributos se hace con una �nica instancia de BSRenderAttributeImpl por lo que hay una �nica forma compartida
        // de renderizar entre atributos (eso no lo podemos hacer en web, pero en Web afortunadamente se utiliza mucho el innerHTML) 
        // podemos considerar una estrategia de definir atributos con una s�la sentencia a modo de batch, se enviar� mucho menos c�digo y ser� m�s r�pido de parsear en beanshell
        
        Element elem = (Element)node;
        BSRenderAttributeImpl render = BSRenderAttributeImpl.getBSRenderAttribute();          
        StringBuilder code = new StringBuilder();
        
        NamedNodeMap attribList = elem.getAttributes();    
        if (attribList.getLength() <= 1)
        {
            // No vale la pena el batch
            for(int i = 0; i < attribList.getLength(); i++)
            {
                Attr attr = (Attr)attribList.item(i);
                code.append( render.setAttributeCode(attr,elem,elemVarName,clientDoc) );
            }
       }
       else
       {
            Map<String,List<Attr>> mapByNamespace = new HashMap<String,List<Attr>>();
            List<Attr> listNoNamespace = new LinkedList<Attr>();            
            for(int i = 0; i < attribList.getLength(); i++)
            {
                Attr attr = (Attr)attribList.item(i);
                String ns = attr.getNamespaceURI();
                if (ns != null)
                {
                    List<Attr> list = mapByNamespace.get(ns);
                    if (list == null) 
                    {
                        list = new LinkedList<Attr>();
                        mapByNamespace.put(ns,list);
                    }
                    list.add(attr);
                    mapByNamespace.put(ns,list);
                }
                else listNoNamespace.add(attr);
            }       
            
            if (!mapByNamespace.isEmpty())
            {
                code.append( render.setAttributeCodeBatchNS(elem,elemVarName,mapByNamespace,clientDoc) );
            }
           
            if (!listNoNamespace.isEmpty())
            {
                code.append( render.setAttributeCodeBatch(elem,elemVarName,listNoNamespace,clientDoc) );                
            }
       }
           
        return code.toString();
    }

    public boolean isAddChildNodesBeforeNode(Node parent,ClientDocumentStfulDelegateImpl clientDoc)
    {
         return false;
    }
    
    public Object getInsertNewNodeCode(Node newNode,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        // No hay inserci�n como markup porque en cuanto a namespaces son los atributos son los que tienen namespace y al serializar el DOM no podemos conseguir
        // que los atributos autom�ticamente se pongan con prefijo salvo que lo especifique el usuario en el localName del setAttributeNS, el problema es que sin
        // usar prefijo funciona sin inserci�n markup y obligar�amos al usuario a usar siempre el prefijo y tendr�amos que filtrarlo por otra parte en el servidor
        // Es un tema SOLO de rendimiento pot lo que podemos sacrificar la inserci�n como markup.
        InsertAsMarkupInfoImpl insertMarkupInfoNextLevel = null; 
        return super.getInsertNewNodeCode(newNode,insertMarkupInfoNextLevel,clientDoc);
    }    
}