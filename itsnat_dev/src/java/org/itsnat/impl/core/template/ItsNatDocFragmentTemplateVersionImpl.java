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

package org.itsnat.impl.core.template;

import org.itsnat.core.ItsNatServletRequest;
import org.itsnat.core.ItsNatServletResponse;
import org.itsnat.impl.core.MarkupContainerImpl;
import org.itsnat.impl.core.domutil.DOMUtilInternal;
import org.itsnat.impl.core.scriptren.jsren.dom.node.JSRenderElementImpl;
import org.itsnat.impl.core.scriptren.jsren.dom.node.html.JSRenderHTMLElementAllBrowsersImpl;
import org.itsnat.impl.core.scriptren.jsren.dom.node.otherns.JSRenderOtherNSElementW3CDefaultImpl;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLElement;
import org.xml.sax.InputSource;

/**
 *
 * @author jmarranz
 */
public abstract class ItsNatDocFragmentTemplateVersionImpl extends MarkupTemplateVersionImpl
{
    protected DocumentFragment templateDocFragment; // Se utilizar� para el contenido del <body> en el caso HTML (no para el <head>)
    
    /**
     * Creates a new instance of ItsNatDocFragmentTemplateVersionImpl
     */
    public ItsNatDocFragmentTemplateVersionImpl(ItsNatDocFragmentTemplateImpl docTemplate,InputSource source,long timeStamp,ItsNatServletRequest request,ItsNatServletResponse response)
    {
        super(docTemplate,source,timeStamp,request,response);

        doCacheAndNormalizeDocument();
        
        this.templateDocFragment = extractChildrenToDocFragment(getContainerElement());        
    }

    public ItsNatDocFragmentTemplateImpl getItsNatDocFragmentTemplate()
    {
        return (ItsNatDocFragmentTemplateImpl)markupTemplate;
    }

    public abstract Element getContainerElement();
    
    public DocumentFragment loadDocumentFragment(MarkupContainerImpl target)
    {
        return loadDocumentFragment(templateDocFragment,target); // Por defecto el <body>
    }        
    
    public DocumentFragment loadDocumentFragment(DocumentFragment cachedDocFrament,MarkupContainerImpl target)
    {
        // Es una falsa carga, es un clonado pues importNode hace un clonado (est� documentado)
        // no hace falta por tanto otro cloneNode(true)
        // El clonado admite multihilo
        target.addUsedMarkupTemplateVersionWithCachedNodes(this);

        Document docTarget = target.getDocument();

        return (DocumentFragment)docTarget.importNode(cachedDocFrament,true);
    }

    public DocumentFragment loadDocumentFragmentByIncludeTag(MarkupContainerImpl target,Element includeElem)
    {
        return loadDocumentFragment(target);
    }    

    public DocumentFragment extractChildrenToDocFragment(Element parent)
    {
        // El copiar nodos a un DocumentFragment conlleva inevitablemente
        // quitarlos del nodo padre, por tanto HAY QUE RECORDAR que parent
        // ha quedado vac�o.

        return DOMUtilInternal.extractChildrenToDocFragment(parent);
    }

    @Override
    protected boolean isElementValidForCaching(Element elem)
    {
        if (!super.isElementValidForCaching(elem))
            return false; // Ya arriba se dice que no.

        if (elem == getDocument().getDocumentElement())
        {
            // No permitimos cachear el contenido directo del elemento root
            // por varias razones:

            // - Fragmentos XML:
            
            // Para evitar que en el caso de ser cacheados quede con �nico nodo hijo
            // de text. El problema es que al que al obtener el DocumentFragment
            // quedar�a como un simple nodo de texto en donde el padre (el <root>)
            // se han perdido, y en el cacheado es importante que el
            // elemento contenedor sea el verdadero padre del contenido cacheado.
            // De todas formas yo creo que en XML es irrelevante esto, pero as�
            // evitamos un DocumentFragment de template con un simple nodo texto

            // - Fragmentos HTML (root <html>):

            // Evitamos cachear el contenido del <html> para evitar que los propios elementos <head> y <body>
            // queden ambos como un �nico nodo de texto, pues necesitamos
            // obtener los DocumentFragment del contenido del <head> y el <body>.

            // - Fragmentos No HTML con estado

            // Mismas razones que en fragmentos XML            

            return false;
        }

        // La raz�n de esto es la siguiente: un fragmento puede insertarse
        // en un documento despu�s de estar cargado por ejemplo durante un evento AJAX
        // por tanto la inserci�n en el cliente del c�digo serializado de un elemento
        // cacheado ha de hacerse necesariamente via innerHTML o setInnerXML.

        // El fragmento podr�a ser insertado en el documento en la fase de carga
        // ya sea via <include> est�tico o via DOM en caso de fast load (el �rbol no ha sido serializado todav�a)
        // en ese caso no se utiliza innerHTML/setInnerXML pero no lo sabemos a priori, por tanto
        // contemplamos todos los escenarios.
        // Otra opci�n alternativa a innerHTML/setInnerXML ser�a reconstruir el DOM del nodo cacheado y convertirlo
        // en instrucciones DOM JavaScript pero esto es mucho
        // m�s lento y necesita de parseado parcial.
        // Por tanto NO podemos cachear el interior de un elemento que no puede ser enviado su contenido al cliente
        // e insertado por JavaScript usando innerHTML o las alternativas XML (aunque s� sus hijos que a lo mejor permiten innerHTML
        // pero eso ha de decidirse para cada hijo concreto).
        // Tampoco sabemos qu� navegador/es usar� el template por tanto consideramos una mezcla de todas
        // las restricciones de los navegadores soportados

        // En el caso de que este sea un fragmento XML (namespace cuyos documentos no tienen estado)
        // el documento destino s� puede tener estado (MUY RARO pero posible), es decir
        // puede ser un X/HTML, SVG o XUL. Por tanto lo anterior tambi�n se aplica.

        JSRenderElementImpl render;
        if (elem instanceof HTMLElement)
        {
            // Si el nodo es HTML suponemos que el navegador destino es el especial
            // com�n denominador de todos.
            render = JSRenderHTMLElementAllBrowsersImpl.SINGLETON;
        }
        else
        {
            // Tanto W3C como MSIE admiten inserci�n de markup usando DOMRender (parseXML en ASV) y loadXML
            // respectivamente. 
            // El criterio tanto en W3C como en MSIE por ahora es el mismo por lo que
            // usamos el SINGLETON de W3C
            render = JSRenderOtherNSElementW3CDefaultImpl.SINGLETON;
        }

        return render.canInsertAllChildrenAsMarkup(elem,this);
    }

    public boolean isInvalid(ItsNatServletRequest request,ItsNatServletResponse response)
    {
        MarkupSourceImpl source = getItsNatDocFragmentTemplate().getMarkupSource();
        return isInvalid(source,request,response);
    }
    
    
    @Override
    public void cleanDOMPattern()
    {
        super.cleanDOMPattern();

        this.templateDocFragment = null;
    }    
}
