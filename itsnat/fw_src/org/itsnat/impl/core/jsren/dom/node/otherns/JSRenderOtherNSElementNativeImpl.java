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

package org.itsnat.impl.core.jsren.dom.node.otherns;

import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.BrowserMSIEOld;
import org.itsnat.impl.core.browser.BrowserW3C;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.domutil.NamespaceUtil;
import org.w3c.dom.Element;

/**
 * La palabra "Native" en este caso quiere decir que el markup es directamente
 * gestionado por el navegador aunque el navegador sea un plugin SVG, pero dicho
 * plugin es tratado como un navegador gestionando un documento completo.
 *
 * @author jmarranz
 */
public abstract class JSRenderOtherNSElementNativeImpl extends JSRenderOtherNSElementImpl
{
    /**
     * Creates a new instance of JSRenderOtherNSElementW3CImpl
     */
    public JSRenderOtherNSElementNativeImpl()
    {
    }

    public static JSRenderOtherNSElementNativeImpl getJSRenderOtherNSElementNative(Browser browser)
    {
        // En el caso de MSIE es el caso de insertar elementos de namespace desconocido
        // en documentos HTML
        if (browser instanceof BrowserMSIEOld)
            return JSRenderOtherNSElementMSIEOldImpl.getJSRenderOtherNSElementMSIEOld((BrowserMSIEOld)browser);
        else
            return JSRenderOtherNSElementW3CImpl.getJSRenderOtherNSElementW3C((BrowserW3C)browser);
    }

    public boolean isInsertedScriptNotExecuted(Element script,ClientDocumentStfulImpl clientDoc)
    {
        if (NamespaceUtil.isSVGElement(script))
        {
            Browser browser = clientDoc.getBrowser();
            return browser.isInsertedSVGScriptNotExecuted();
        }
        // Si es XUL, FireFox no necesita nada especial para el script en XUL, desconocemos
        // otros namespaces de <script>
        return false;
    }

    public boolean isTextAddedToInsertedScriptNotExecuted(Element script,ClientDocumentStfulImpl clientDoc)
    {
        if (NamespaceUtil.isSVGElement(script))
        {
            Browser browser = clientDoc.getBrowser();
            return browser.isTextAddedToInsertedSVGScriptNotExecuted();
        }
        // Si es XUL, FireFox no necesita nada especial para el script en XUL, desconocemos
        // otros namespaces con <script>
        return false;
    }

}
