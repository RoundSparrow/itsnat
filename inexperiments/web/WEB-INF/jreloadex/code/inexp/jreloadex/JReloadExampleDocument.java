package inexp.jreloadex;

import org.itsnat.comp.ItsNatComponentManager;
import org.itsnat.comp.text.ItsNatHTMLInputText;
import org.itsnat.core.ItsNatServletRequest;
import org.itsnat.core.html.ItsNatHTMLDocument;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLDocument;

public class JReloadExampleDocument
{
    protected ItsNatHTMLDocument itsNatDoc; // ItsNatHTMLDocument
    protected ItsNatHTMLInputText textInput; // ItsNatHTMLInputText
    protected Element resultsElem; // Element   

    public static class AuxMember 
    { 
        public static void log()
        {
            System.out.println("JReloadExampleDocument.AuxMember: 13 " + AuxMember.class.getClassLoader().hashCode());
        }        
    }
           
    
    public JReloadExampleDocument(ItsNatServletRequest request,ItsNatHTMLDocument itsNatDoc,FalseDB db)
    {
        class AuxMemberInMethod 
        { 
            public void log()
            {
                System.out.println("JReloadExampleDocument.AuxMemberInMethod: 1 " + AuxMemberInMethod.class.getClassLoader().hashCode());
            }        
        }        
        
        this.itsNatDoc = itsNatDoc;

        if (db.getCityList().size() != 3) 
            throw new RuntimeException("Unexpected");

        HTMLDocument doc = itsNatDoc.getHTMLDocument();

        ItsNatComponentManager compMgr = itsNatDoc.getItsNatComponentManager();
        this.textInput = (ItsNatHTMLInputText)compMgr.createItsNatComponentById("inputId");

       EventListener listener = new EventListener()
       {    
           {
                System.out.println("JReloadExampleDocument Anonymous Inner 21 " + this.getClass().getClassLoader().hashCode());
           }
           
            public void handleEvent(Event evt) 
            {
                String text = textInput.getText(); 
                resultsElem.setTextContent(text);
            }
        };
        
        Element buttonElem = doc.getElementById("buttonId");
        ((EventTarget)buttonElem).addEventListener("click",listener,false);

        this.resultsElem = doc.getElementById("resultsId");
        
        System.out.println("JReloadExampleDocument 2 " + this.getClass().getClassLoader().hashCode());        
        new AuxMemberInMethod().log();
        AuxMember.log();
        //JReloadExampleDocumentAuxInSameFile.log();
        JReloadExampleAux.log();
    }

}

/*
class JReloadExampleDocumentAuxInSameFile
{
    public static void log()
    {
        System.out.println("JReloadExampleDocumentAuxInSameFile: 1 " + JReloadExampleDocumentAuxInSameFile.class.getClassLoader().hashCode());
    }    
}
*/