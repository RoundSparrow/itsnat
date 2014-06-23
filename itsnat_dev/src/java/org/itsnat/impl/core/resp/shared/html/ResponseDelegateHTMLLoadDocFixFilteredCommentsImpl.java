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

package org.itsnat.impl.core.resp.shared.html;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.itsnat.core.domutil.ItsNatTreeWalker;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.doc.web.ItsNatHTMLDocumentImpl;
import org.itsnat.impl.core.domutil.DOMUtilHTML;
import org.itsnat.impl.core.domutil.DOMUtilInternal;
import org.itsnat.impl.core.scriptren.jsren.JSRenderImpl;
import org.itsnat.impl.core.domutil.NamespaceUtil;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLHeadElement;

/**
 *
 * @author jmarranz
 */
public class ResponseDelegateHTMLLoadDocFixFilteredCommentsImpl
{
    protected static final String FAKE_COMMENT_NAME_VALUE = "itsnatFakeCommName";
    protected static final String FAKE_COMMENT_ID_VALUE = "itsnatFakeCommId_";
    protected static final String PARENT_COMMENT_ID_ATTR_NAME = "idItsNatFakeCommParent";
    protected static final String NEXT_COMMENT_ID_ATTR_NAME = "idItsNatFakeCommPrevious";

    public static final Set<String> ELEMENTS_NOT_VALID_CHILD_COMMENT = new HashSet<String>();

    static
    {
        // Elementos que NO pueden tener elementos como hijo diferentes
        // a unos concretos y por tanto en donde no funcionar�a el uso de
        // <span> o <style>

        // Es el caso de BlackBerry y S60WebKit FP 1 en donde los comentarios son siempre filtrados
        // en carga, por ello para conservarlos los substituimos temporalmente por <style> bajo <head> y <span> bajo <body>
        // pero este <span> es TAMBIEN filtrado en algunos elementos cuyos elementos hijos est�n
        // predeterminados. Aunque s�lo
        // he estudiado el problema en TABLE, TBODY, TFOOT, THEAD y TR es razonable
        // que un <span> no puede ser hijo de elementos que s�lo admiten
        // ciertos tipos de elementos como hijo, por ejemplo un <span> bajo un <table>.
        // En algunos de estos elementos los comentarios son filtrados ya a nivel
        // de servidor porque son problem�ticos por ejemplo para el IE 6 sin "workaround".
        // proporcionado por ItsNat, por lo que no se dar� el caso de necesitar reinserci�n
        // (pero por si acaso los incluimos aqu�).

        ELEMENTS_NOT_VALID_CHILD_COMMENT.add("dl");
        ELEMENTS_NOT_VALID_CHILD_COMMENT.add("frameset");
        ELEMENTS_NOT_VALID_CHILD_COMMENT.add("html");
        ELEMENTS_NOT_VALID_CHILD_COMMENT.add("ol");
        ELEMENTS_NOT_VALID_CHILD_COMMENT.add("optgroup");
        ELEMENTS_NOT_VALID_CHILD_COMMENT.add("select");
        ELEMENTS_NOT_VALID_CHILD_COMMENT.add("table");
        ELEMENTS_NOT_VALID_CHILD_COMMENT.add("tbody");
        ELEMENTS_NOT_VALID_CHILD_COMMENT.add("tfoot");
        ELEMENTS_NOT_VALID_CHILD_COMMENT.add("thead");
        ELEMENTS_NOT_VALID_CHILD_COMMENT.add("tr");
        ELEMENTS_NOT_VALID_CHILD_COMMENT.add("ul");
    }

    protected LinkedList<Comment> comments = new LinkedList<Comment>();
    protected LinkedList<Node[]> fakeCommentList = new LinkedList<Node[]>();
    protected LinkedList<Node[]> holdCommentList = new LinkedList<Node[]>();
    protected ResponseDelegateHTMLLoadDocImpl responseParent;

    /**
     * Creates a new instance of ResponseDelegateHTMLLoadDocFixFilteredCommentsImpl
     */
    public ResponseDelegateHTMLLoadDocFixFilteredCommentsImpl(ResponseDelegateHTMLLoadDocImpl responseParent)
    {
        this.responseParent = responseParent;
    }

    public ClientDocumentStfulImpl getClientDocumentStful()
    {
        return responseParent.getClientDocumentStful();
    }

    public void preSerializeDocument()
    {
        // En este caso la estrategia es la siguiente: el DOM del documento
        // tiene nodos cacheados,
        // dichos nodos cacheados pueden tener comentarios que ser�n filtrados
        // en el cliente, sin embargo esos comentarios NO nos interesan pues
        // no se reflejan en el DOM del servidor por tanto no ser�n accedidos ni habr� c�lculo
        // de paths ni path-ids de los mismos
        ItsNatHTMLDocumentImpl itsNatDoc = responseParent.getItsNatHTMLDocument();
        HTMLDocument doc = itsNatDoc.getHTMLDocument();

        replaceComments(doc);
    }

    public String postSerializeDocument(String docMarkup)
    {
        if (!comments.isEmpty())
        {
            restoreComments();

            addCodeToSendComments();
        }

        return docMarkup;
    }

    protected boolean replaceComments(HTMLDocument doc)
    {
        // Tenemos la garant�a de que no hay comentarios en lugares donde
        // no puede ponerse un <style> bajo <head> y <span> bajo BODY pues fueron filtrados en la normalizaci�n.
        // El S60WebKit (FP 1) mueve los <style> bajo <body> a <head>
        // Sabemos que en este contexto los mutation events est�n desactivados

        HTMLHeadElement head = DOMUtilHTML.getHTMLHead(doc);
        replaceComments(head,"style");
        HTMLElement body = doc.getBody();
        replaceComments(body,"span");

        return !comments.isEmpty(); // Si false es que no hay ningun comentario, no se ha hecho nada
    }

    protected Node replaceComments(Node node,String localNameReplacing)
    {
        if (node.getNodeType() == Node.COMMENT_NODE)
        {
            Node nodeRes = node; // En un caso node es reemplazado por otro
            
            int index = comments.size(); // Antes de a�adir, empezamos en cero
            Element parentNode = (Element)node.getParentNode();
            String idCommentValue = FAKE_COMMENT_ID_VALUE + index;
            if ((parentNode instanceof HTMLElement) &&
                 ELEMENTS_NOT_VALID_CHILD_COMMENT.contains(parentNode.getLocalName()))
            {
                Element targetNode;
                String attrName;
                Element nextNode = ItsNatTreeWalker.getNextSiblingElement(node);
                if (nextNode != null)
                {
                    targetNode = nextNode;
                    attrName = NEXT_COMMENT_ID_ATTR_NAME;
                }
                else
                {
                    targetNode = parentNode;
                    attrName = PARENT_COMMENT_ID_ATTR_NAME;
                }
                // Puede darse el caso de varios comentarios seguidos, por lo que a�adimos los ids separados por comas
                String attrValue = targetNode.getAttribute(attrName);
                if (attrValue.equals("")) attrValue = idCommentValue;
                else attrValue = attrValue + "," + idCommentValue;
                DOMUtilInternal.setAttribute(targetNode,attrName,attrValue);
                holdCommentList.add(new Node[]{node,targetNode});
            }
            else
            {
                Document doc = node.getOwnerDocument();
                Element fakeComm = doc.createElementNS(NamespaceUtil.XHTML_NAMESPACE,localNameReplacing);
                DOMUtilInternal.setAttribute(fakeComm,"name",FAKE_COMMENT_NAME_VALUE);
                DOMUtilInternal.setAttribute(fakeComm,"id"  ,idCommentValue);
                parentNode.replaceChild(fakeComm, node);
                fakeCommentList.add(new Node[]{node,fakeComm});
                nodeRes = fakeComm;  // Ha sido reemplazado
            }
            comments.add((Comment)node);

            return nodeRes;
        }
        else
        {
            if (!node.hasChildNodes()) return node;

            Node child = node.getFirstChild();
            while(child != null)
            {
                // Hay que tener en cuenta que replaceComments reemplaza el nodo child si es un comentario
                // por otro                
                child = replaceComments(child,localNameReplacing);

                child = child.getNextSibling();
            }
            
            return node;
        }
   }


    protected void restoreComments()
    {
        if (comments.isEmpty()) return;

        // Restauramos el estado anterior del DOM
        if (!fakeCommentList.isEmpty())
        {
            for(Node[] pair : fakeCommentList)
            {
                Comment comm = (Comment)pair[0];
                Element fakeComm = (Element)pair[1];
                fakeComm.getParentNode().replaceChild(comm,fakeComm);
            }
        }

        if (!holdCommentList.isEmpty())
        {
            for(Node[] pair : holdCommentList)
            {
                //Comment comm = (Comment)pair[0];
                Element container = (Element)pair[1];
                container.removeAttribute(NEXT_COMMENT_ID_ATTR_NAME);
                container.removeAttribute(PARENT_COMMENT_ID_ATTR_NAME);
            }
        }
    }

    protected void addCodeToSendComments()
    {
        ItsNatHTMLDocumentImpl itsNatDoc = responseParent.getItsNatHTMLDocument();
        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();

        StringBuilder code = new StringBuilder();

        // Metemos una funci�n para evitar dejar variables globales
        code.append("var func = function()\n");
        code.append("{\n");
        code.append("  var commMap = new Object();\n");

    int i = 0;
    for(Comment comm : comments)
    {
        String text = comm.getData();
        // Sabemos que text es posible que contenga un ${} indicando que el comentario
        // fue cacheado, "descacheamos" aqu�.
        text = itsNatDoc.resolveCachedNodes(text,false); // La verdad es que el resolveEntities da igual en este caso
        code.append("  commMap[\"" + (FAKE_COMMENT_ID_VALUE + i) + "\"] = " + JSRenderImpl.toTransportableStringLiteral(text,clientDoc.getBrowser()) + ";\n");
        i++;
    }

    if (!fakeCommentList.isEmpty())
    {
        // Resolvemos los comentarios que fueron substituidos por elemento "fake comment"
        code.append("  var fakeNodeList = document.getElementsByName(\"" + FAKE_COMMENT_NAME_VALUE + "\");\n");
        // Hacemos una copia porque un NodeList seg�n el est�ndar es una colecci�n viva por tanto
        // si quitamos los elementos del documento puede cambiar (de hecho es as� en S60WebKit 5th v0.9)
        // y no queremos eso.
        code.append("  var len = fakeNodeList.length;\n");
        code.append("  var fakeNodeArr = new Array(len);\n");
        code.append("  for(var i=0;i < len; i++) fakeNodeArr[i] = fakeNodeList[i];\n");
        code.append("  fakeNodeList = null;\n"); // para ahorrar memoria
        code.append("  for(var i=0;i < len; i++)\n");
        code.append("  {\n");
        code.append("    var fakeCom = fakeNodeArr[i];\n");
        code.append("    var text = commMap[fakeCom.id];\n");
        code.append("    var comm = document.createComment(text);\n");
        code.append("    fakeCom.parentNode.replaceChild(comm,fakeCom);\n");
        code.append("  }\n");
        code.append("  fakeNodeArr = null;\n"); // para ahorrar memoria
    }

    if (!holdCommentList.isEmpty())
    {
        // Ahora los que fueron "recordados" a trav�s de ids especiales
        // en el elemento padre o en el next
        // Insertaremos los comentarios en la misma zona seguidos, seguramente no coincidir�
        // con la posici�n respecto a los nodos de texto (si no son filtrados) pero dichos nodos
        // de texto son espacios y/o fines de l�nea que no influyen en el c�lculo
        // de paths y es absurdo necesitar acceder a ellos desde el servidor (no valen para nada por eso
        // suelen ser filtrados).

        // Necesitamos esta funci�n getNextNode para iterar (obtenida de ItsNatTreeWalker)
        code.append("  function getNextNode(node)\n");
        code.append("  {\n");
        code.append("    if (node == null) return null;\n");
        code.append("    var result = node.firstChild;\n");
        code.append("    if (result != null) return result;\n");
        code.append("    result = node.nextSibling;\n");
        code.append("    if (result != null) return result;\n");
        code.append("    var parentNode = node.parentNode;\n");
        code.append("    while (parentNode != null)\n");
        code.append("    {\n");
        code.append("      result = parentNode.nextSibling;\n");
        code.append("      if (result != null) return result;\n");
        code.append("      else parentNode = parentNode.parentNode;\n");
        code.append("    }\n");
        code.append("    return null;\n");
        code.append("  }\n");

        code.append("  function addComments(node,name,value,isNext,commMap)\n");
        code.append("  {\n");
        code.append("    var nextNode, parentNode;\n");
        code.append("    if (isNext) { nextNode = node; parentNode = node.parentNode; }\n");
        code.append("    else { nextNode = null; parentNode = node; }\n");
        code.append("    var idCommList = value.split(\",\");\n");
        code.append("    for(var i = 0; i < idCommList.length; i++)\n");
        code.append("    {\n");
        code.append("      var text = commMap[idCommList[i]];\n");
        code.append("      var comm = document.createComment(text);\n");
        code.append("      parentNode.insertBefore(comm,nextNode);\n");
        code.append("    }\n");
        code.append("    node.removeAttribute(name);\n"); // Ya no se necesita
        code.append("  }\n");

        code.append("  var node = document;\n");
        code.append("  do \n");
        code.append("  {\n");
        code.append("    if (node.nodeType == 1)\n");
        code.append("    {\n"); // Un mismo nodo puede ser referencia tanto padre como next
        code.append("      var name,value;\n");
        code.append("      name = \"" + NEXT_COMMENT_ID_ATTR_NAME + "\";\n");
        code.append("      value = node.getAttribute(name);\n");
        code.append("      if ((value!=null)&&(value!=\"\")) addComments(node,name,value,true,commMap);\n");
        code.append("      name = \"" + PARENT_COMMENT_ID_ATTR_NAME + "\";\n");
        code.append("      value = node.getAttribute(name);\n");
        code.append("      if ((value!=null)&&(value!=\"\")) addComments(node,name,value,false,commMap);\n");
        code.append("    }\n");
        code.append("    node = getNextNode(node);\n");
        code.append("  }\n");
        code.append("  while(node != null);\n");
    }

        code.append("};\n");
        code.append("func();\n");
        code.append("func = null;\n"); // No se necesita m�s, para liberar memoria

        // Insertamos AL PRINCIPIO para que el c�digo generado por el usuario (que va despu�s)
        // vea ya los comentarios como comentarios.
        responseParent.addFixDOMCodeToSend(code.toString());
    }
}
