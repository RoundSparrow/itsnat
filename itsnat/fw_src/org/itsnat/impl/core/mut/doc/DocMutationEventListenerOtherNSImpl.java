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

package org.itsnat.impl.core.mut.doc;

import java.util.LinkedList;
import org.itsnat.impl.core.doc.ItsNatOtherNSDocumentImpl;
import org.w3c.dom.Node;
import org.w3c.dom.events.MutationEvent;

/**
 *
 * @author jmarranz
 */
public class DocMutationEventListenerOtherNSImpl extends DocMutationEventListenerStfulImpl
{
    public DocMutationEventListenerOtherNSImpl(ItsNatOtherNSDocumentImpl itsNatDoc)
    {
        super(itsNatDoc);
    }

    protected void checkOperation(MutationEvent mutEvent)
    {
    }

}
