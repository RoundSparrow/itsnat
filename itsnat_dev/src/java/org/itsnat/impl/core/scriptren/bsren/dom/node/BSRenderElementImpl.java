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

package org.itsnat.impl.core.scriptren.bsren.dom.node;


import org.itsnat.impl.core.scriptren.shared.dom.node.CannotInsertAsMarkupCauseImpl;
import org.itsnat.impl.core.scriptren.shared.dom.node.InsertAsMarkupInfoImpl;
import org.itsnat.impl.core.scriptren.shared.dom.node.InnerMarkupCodeImpl;
import org.itsnat.impl.core.clientdoc.droid.ClientDocumentStfulDelegateDroidImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.domutil.DOMUtilInternal;
import org.itsnat.impl.core.domutil.NodeConstraints;
import org.itsnat.impl.core.dompath.NodeLocationImpl;
import org.itsnat.impl.core.template.ItsNatStfulDocumentTemplateVersionImpl;
import org.itsnat.impl.core.template.MarkupTemplateVersionImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author jmarranz
 */
public abstract class BSRenderElementImpl extends BSRenderHasChildrenNodeImpl implements NodeConstraints
{

    /** Creates a new instance of JSElementRender */
    public BSRenderElementImpl()
    {
    }

    public static BSRenderElementImpl getBSRenderElement()
    {
        return BSRenderElementDefaultImpl.SINGLETON;
    }

    protected String createNodeCode(Node node,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        Element nodeElem = (Element)node;
        return createElement(nodeElem,clientDoc);
    }

    protected String createElement(Element nodeElem,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        String tagName = nodeElem.getTagName();
        return createElement(nodeElem,tagName,clientDoc);
    }

    protected String createElement(Element nodeElem,String tagName,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        String namespace = nodeElem.getNamespaceURI();
        if (namespace != null)
            return "itsNatDoc.createElementNS(\"" + namespace + "\",\"" + tagName + "\")";
        else
            return "itsNatDoc.createElement(\"" + tagName + "\")";      
    }

    protected String addAttributesBeforeInsertNode(Node node,String elemVarName,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        Element elem = (Element)node;
        BSRenderAttributeImpl render = BSRenderAttributeImpl.getBSRenderAttribute();          
        StringBuilder code = new StringBuilder();
        NamedNodeMap attribList = elem.getAttributes();      
        for(int i = 0; i < attribList.getLength(); i++)
        {
            Attr attr = (Attr)attribList.item(i);
            code.append( render.setAttributeCode(attr,elem,elemVarName,true,clientDoc) );
        }
        return code.toString();
    }


    @Override
    protected Object appendChildNodes(Node parent, String parentVarName,boolean beforeParent,InsertAsMarkupInfoImpl insertMarkupInfo,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        CannotInsertAsMarkupCauseImpl cannotInsertMarkup = canInsertAllChildrenAsMarkup((Element)parent,clientDoc.getItsNatStfulDocument().getItsNatStfulDocumentTemplateVersion(),insertMarkupInfo);
        if (cannotInsertMarkup == null)
        {
            // Sabemos que el retorno, innerMarkup, nunca es nulo en este contexto
            InnerMarkupCodeImpl innerMarkup = appendChildrenAsMarkup(parentVarName,parent,clientDoc);
            return innerMarkup;
        }
        else
        {
            InsertAsMarkupInfoImpl insertMarkupInfoNextLevel = cannotInsertMarkup.createInsertAsMarkupInfoNextLevel(); // Puede ser null
            return super.appendChildNodes(parent,parentVarName,beforeParent,insertMarkupInfoNextLevel,clientDoc);
        }
    }

    public Object getInsertNewNodeCode(Node newNode,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        CannotInsertAsMarkupCauseImpl cannotInsertMarkup = canInsertSingleChildNodeAsMarkup(newNode,clientDoc);
        if (cannotInsertMarkup == null)
            return appendSingleChildNodeAsMarkup(newNode,clientDoc);
        else
        {
            InsertAsMarkupInfoImpl insertMarkupInfoNextLevel = cannotInsertMarkup.createInsertAsMarkupInfoNextLevel(); // Puede ser null
            return super.getInsertNewNodeCode(newNode,insertMarkupInfoNextLevel,clientDoc);
        }
    }

    public abstract String getAppendChildrenCodeAsMarkupSentence(InnerMarkupCodeImpl innerMarkupRender,ClientDocumentStfulDelegateDroidImpl clientDoc);

    protected CannotInsertAsMarkupCauseImpl canInsertSingleChildNodeAsMarkup(Node newChildNode,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        // Este es un escenario en donde queremos insertar un nuevo nodo pero puede
        // haber antes ya otros previamente insertados, por lo que s�lo podemos
        // insertar como markup si este nodo es el �nico nodo o el �ltimo
        // y disponemos de un InnerMarkupCodeImpl

        Element parent = (Element)newChildNode.getParentNode();
        if (!DOMUtilInternal.isTheOnlyChildNode(newChildNode))
        {
            // Debe ser el �ltimo.
            // De esta manera permitimos "reusar" el innerHTML (o innerXML) en casos
            // por ejemplo de appendChild sucesivos por parte del programador (mismo padre claro)
            if (parent.getLastChild() != newChildNode)
                return new CannotInsertAsMarkupCauseImpl(parent); // No es el �ltimo
            // Los anteriores debieron ser insertados inmediatamente antes como innerHTML
            // quiz�s en el futuro podamos detectar que los �ltimos cambios realizados en el DOM no afectan
            // al �ltimo InnerMarkupCodeImpl asociado al nodo actual pero por ahora es complicado
            // y no merece la pena.
            Object lastCode = clientDoc.getClientDocumentStful().getLastCodeToSend();
            if (!(lastCode instanceof InnerMarkupCodeImpl))
                return new CannotInsertAsMarkupCauseImpl(parent); // Si existe un InnerMarkupCodeImpl debe ser lo �ltimo que se hizo
            InnerMarkupCodeImpl lastInnerCode = (InnerMarkupCodeImpl)lastCode;
            if (lastInnerCode.getParentNode() != parent)
                return new CannotInsertAsMarkupCauseImpl(parent); // Es un inner de otro nodo padre
            // Si llegamos aqu� es que los anteriores al nuevo son compatibles con innerHTML o nuestro innerXML
        }

        ItsNatStfulDocumentTemplateVersionImpl template = clientDoc.getItsNatStfulDocument().getItsNatStfulDocumentTemplateVersion();
        return canInsertChildNodeAsMarkupIgnoringOther(parent,newChildNode,template); // En el caso de �nico hijo obviamente los dem�s se ignoran pues no hay m�s
    }

    protected InnerMarkupCodeImpl appendSingleChildNodeAsMarkup(Node newNode, ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        String newNodeMarkup = itsNatDoc.serializeNode(newNode);
        if (DOMUtilInternal.isTheOnlyChildNode(newNode))
        {
            // Caso de �nico nodo
            Element parent = (Element)newNode.getParentNode();
            return appendChildrenCodeAsMarkup(null,parent,newNodeMarkup,clientDoc);
        }
        else // Caso de �ltimo nodo, sabemos que podemos usar el �ltimo InnerMarkupCodeImpl el cual est� asociado al nodo padre
        {
            InnerMarkupCodeImpl lastCode = (InnerMarkupCodeImpl)clientDoc.getClientDocumentStful().getLastCodeToSend();
            lastCode.addInnerMarkup(newNodeMarkup);
            return null; // No se a�ade nada y se deja como �ltimo este lastCode
        }
    }

    protected InnerMarkupCodeImpl appendChildrenAsMarkup(String parentVarName, Node parentNode, ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        // Se supone que hay nodos hijo (si no no llamar).
        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        StringBuilder childrenCode = new StringBuilder();

        if (parentNode.hasChildNodes())
        {
            Node child = parentNode.getFirstChild();
            while(child != null)
            {
                String nodeCode = itsNatDoc.serializeNode(child);
                childrenCode.append( nodeCode );

                child = child.getNextSibling();
            }
        }
        
        return appendChildrenCodeAsMarkup(parentVarName,(Element)parentNode,childrenCode.toString(),clientDoc);
    }

    protected abstract boolean isInsertChildNodesAsMarkupCapable(Element parent,MarkupTemplateVersionImpl template);

    public boolean match(Node node, Object context)
    {
        // Esto es por claridad pues "match" no nos dice mucho sobre lo que tenemos que hacer
        return isChildNotValidInsertedAsMarkup(node,(MarkupTemplateVersionImpl)context);
    }

    public abstract boolean isChildNotValidInsertedAsMarkup(Node childNode,MarkupTemplateVersionImpl template);

    protected CannotInsertAsMarkupCauseImpl canInsertChildNodeAsMarkupIgnoringOther(Element parent,Node childNode,MarkupTemplateVersionImpl template)
    {
        if (!isInsertChildNodesAsMarkupCapable(parent,template))
            return new CannotInsertAsMarkupCauseImpl(parent);

        // Preguntamos si puede insertarse como markup si fuera el �nico nodo hijo
        Node badChildNode = DOMUtilInternal.getNodeOrFirstContainedNodeMatching(childNode,this,template);
        if (badChildNode != null)
            return new CannotInsertAsMarkupCauseImpl(parent,badChildNode);

        return null;
    }

    public boolean canInsertAllChildrenAsMarkup(Element parent,MarkupTemplateVersionImpl template)
    {
        CannotInsertAsMarkupCauseImpl cannotInsertMarkup = canInsertAllChildrenAsMarkup(parent,template,null);
        return (cannotInsertMarkup == null);
    }

    public CannotInsertAsMarkupCauseImpl canInsertAllChildrenAsMarkup(Element parent,MarkupTemplateVersionImpl template,InsertAsMarkupInfoImpl insertMarkupInfo)
    {
        int res = InsertAsMarkupInfoImpl.DO_NOT_KNOW;
        if (insertMarkupInfo != null)
        {
            res = insertMarkupInfo.canInsertAllChildrenAsMarkup(parent);
            if (res == InsertAsMarkupInfoImpl.CANNOT_INSERT_CHILDREN_VERIFIED)
                return new CannotInsertAsMarkupCauseImpl(insertMarkupInfo); // Salvamos as� en la causa todo lo que ya sabemos del "sub�rbol"
            // Si se llega aqu�, es el caso desconocido InsertAsMarkupInfoImpl.DO_NOT_KNOW
            // o bien IS_VALID_INSERTED_AS_MARKUP
        }
        // Vemos si tiene capacidad de insertar como markup sus hijos, por ejemplo si tiene innerHTML
        if (!isInsertChildNodesAsMarkupCapable(parent,template))
            return new CannotInsertAsMarkupCauseImpl(parent);

        // Ahora vemos si sus hijos son insertables como markup:

        // Si sabemos que puede ser insertado como markup entonces
        // sabemos que los hijos pueden ser tambi�n insertados como markup
        // por tanto no lo averiguamos de nuevo y as� ganamos en rendimiento
        if (res != InsertAsMarkupInfoImpl.IS_VALID_INSERTED_AS_MARKUP)
        {
            Node badChildNode = getFirstChildIsNotValidInsertedAsMarkup(parent,template);
            if (badChildNode != null)
                return new CannotInsertAsMarkupCauseImpl(parent,badChildNode);
        }
        return null;
    }

    public Node getFirstChildIsNotValidInsertedAsMarkup(Element parent,MarkupTemplateVersionImpl template)
    {
        return DOMUtilInternal.getFirstContainedNodeMatching(parent,this,template);
    }

    protected InnerMarkupCodeImpl appendChildrenCodeAsMarkup(String parentVarName,Element parentNode,String childrenCode,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        boolean useNodeLocation;
        String parentNodeLocator;
        if (parentVarName == null)
        {
            useNodeLocation = true;
            NodeLocationImpl parentLoc = clientDoc.getNodeLocation(parentNode,true);
            parentNodeLocator = parentLoc.toScriptNodeLocation(true);
        }
        else
        {
            useNodeLocation = false;
            parentNodeLocator = parentVarName;
        }

        return new InnerMarkupCodeBSImpl(this,parentNode,parentNodeLocator,useNodeLocation,childrenCode);
    }


    /* Problema de SVG */
    public abstract boolean isInsertedScriptNotExecuted(Element script,ClientDocumentStfulDelegateDroidImpl clientDoc);

    // Algunos navegadores WebKit tal y como el primer S60WebKit 
    // no ejecutan el c�digo dentro del <script> si se a�ade despu�s de estar ya insertado en el documento
    public abstract boolean isTextAddedToInsertedScriptNotExecuted(Element script,ClientDocumentStfulDelegateDroidImpl clientDoc);


    public boolean isScriptWithContent(Node newNode)
    {
        return (newNode.hasChildNodes() &&
                newNode.getLocalName().equals("script"));
    }

    public boolean isScriptWithSingleChildTextNode(Node newNode)
    {
        return (isScriptWithContent(newNode) &&
               (newNode.getFirstChild() == newNode.getLastChild()) && // Un �nico hijo
                newNode.getFirstChild() instanceof CharacterData);
    }

    protected String getAppendCompleteChildNode(String parentVarName,Node newNode,String newNodeCode,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        if (isScriptWithSingleChildTextNode(newNode) && isInsertedScriptNotExecuted((Element)newNode,clientDoc) )
        {
            // Aunque el c�digo del <script> ya ha sido insertado en el <script>, al insertar
            // el <script> de todas formas no se ejecutar�, hemos de usar un eval tras insertar el <script>

            // newNodeCode debe ser la variable del nuevo <script>

            StringBuilder code = new StringBuilder();
            code.append( super.getAppendCompleteChildNode(parentVarName, newNode, newNodeCode,clientDoc) );

            // Lo anterior no sirve, no se ha ejecutado, lo ejecutamos "manualmente"
            code.append( "eval(" + newNodeCode + ".firstChild.data);");

            return code.toString();
        }
        else
            return super.getAppendCompleteChildNode(parentVarName, newNode, newNodeCode,clientDoc);
    }

    protected String getInsertCompleteNodeCode(Node newNode,String newNodeCode,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        if (isScriptWithSingleChildTextNode(newNode) && isInsertedScriptNotExecuted((Element)newNode,clientDoc) )
        {
            // Aunque el c�digo del <script> haya sido insertado en el <script>, al insertar
            // el <script> de todas formas no se ejecutar�, hemos de usar un eval tras insertar el <script>

            // Si este m�todo ha sido llamado y <script> tiene un nodo
            // de texto hijo entonces necesariamente newNodeCode es una variable JavaScript
            // pues dicha variable se necesita para insertar despu�s de este m�todo
            // los nodos hijos
            StringBuilder code = new StringBuilder();
            code.append( super.getInsertCompleteNodeCode(newNode, newNodeCode,clientDoc) );

            // Lo anterior no sirve, no se ha ejecutado, lo ejecutamos "manualmente"
            code.append( "eval(" + newNodeCode + ".firstChild.data);");

            return code.toString();
        }
        else
            return super.getInsertCompleteNodeCode(newNode,newNodeCode,clientDoc);
    }

    public boolean isAddChildNodesBeforeNode(Node parent,ClientDocumentStfulDelegateDroidImpl clientDoc)
    {
        // Algunos navegadores WebKit tal y como el primer S60WebKit 
        // no ejecutan el c�digo dentro del <script> una vez insertado en el documento
        // si dicho c�digo se inserta despu�s de la inserci�n del <script> en el documento
        // Esto es la forma normal de inserci�n en ItsNat, por ello en este caso
        // a�adimos el nodo de texto con el c�digo al nodo script antes de insertar en el doc.

        return isScriptWithContent(parent);
    }
}
