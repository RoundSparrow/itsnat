/*
  ItsNat Java Web Application Framework
  Copyright (C) 2007-2014 Jose Maria Arranz Santamaria, Spanish citizen

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

package org.itsnat.impl.core.doc.droid;

import org.itsnat.impl.comp.mgr.ItsNatDocComponentManagerImpl;
import org.itsnat.impl.comp.mgr.droid.ItsNatStfulDroidDocComponentManagerImpl;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.domutil.DefaultElementGroupManagerImpl;
import org.itsnat.impl.core.domutil.ElementGroupManagerImpl;
import org.itsnat.impl.core.mut.doc.DocMutationEventListenerImpl;
import org.itsnat.impl.core.mut.doc.droid.DocMutationEventListenerStfulDroidImpl;
import org.itsnat.impl.core.servlet.ItsNatSessionImpl;
import org.itsnat.impl.core.template.ItsNatDocumentTemplateVersionImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author jmarranz
 */
public class ItsNatDroidDocumentImpl extends ItsNatStfulDocumentImpl
{

    public ItsNatDroidDocumentImpl(Document doc, ItsNatDocumentTemplateVersionImpl docTemplate, Browser browser, String requestURL, ItsNatSessionImpl ownerSession, boolean stateless)
    {
        super(doc, docTemplate, browser, requestURL, ownerSession, stateless);
    }

    @Override
    public Element getVisualRootElement()
    {
        return getDocument().getDocumentElement();
    }

    @Override
    public boolean isNewNodeDirectChildOfContentRoot(Node newNode)
    {
        Node parentNode = newNode.getParentNode();
        if (parentNode == null) return false; // No ocurre nunca pero por si acaso
        Document doc = getDocument();
        return (parentNode.getParentNode() == doc.getDocumentElement());
    }

    @Override
    public DocMutationEventListenerImpl createInternalMutationEventListener()
    {
        return new DocMutationEventListenerStfulDroidImpl(this);
    }

    @Override
    public ElementGroupManagerImpl createElementGroupManager()
    {
        return new DefaultElementGroupManagerImpl(this);
    }

    @Override
    public ItsNatDocComponentManagerImpl createItsNatComponentManager()
    {
        return new ItsNatStfulDroidDocComponentManagerImpl(this);
    }
    
}
