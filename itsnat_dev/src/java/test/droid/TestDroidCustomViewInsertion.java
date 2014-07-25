/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package test.droid;

import org.itsnat.core.ItsNatDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public class TestDroidCustomViewInsertion extends TestDroidBase implements EventListener
{
   
    public TestDroidCustomViewInsertion(ItsNatDocument itsNatDoc)
    {
        super(itsNatDoc);

        Element testLauncher = getDocument().getElementById("testCustomViewInsertionId");
        ((EventTarget)testLauncher).addEventListener("click", this, false);      
    }
    
    public void handleEvent(Event evt)
    {
        Document doc = getDocument();
        Element testLauncherHidden = doc.getElementById("testCustomViewInsertionHiddenId");          
        
        Element customTextView = doc.createElement("org.itsnat.itsnatdroidtest.CustomTextView");
        // El style debe definirse ANTES de insertar
        customTextView.setAttribute("style", "@style/test"); // A�ade left/right padding
        customTextView.setAttributeNS(ANDROID_NS, "text", "OK if text is shown and left/right padding");    // CustomTextView deriva de TextView
        
        testLauncherHidden.getParentNode().insertBefore(customTextView, testLauncherHidden);
    }
    
}