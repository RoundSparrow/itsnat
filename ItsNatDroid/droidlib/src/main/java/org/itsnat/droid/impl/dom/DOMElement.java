package org.itsnat.droid.impl.dom;

import org.itsnat.droid.impl.util.ValueUtil;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by jmarranz on 31/10/14.
 */
public abstract class DOMElement
{
    protected String name;
    protected DOMElement parentElement; // Si es null es el root
    protected ArrayList<DOMAttr> attribs;
    protected LinkedList<DOMElement> childList;
    protected String sourceXPath;

    public DOMElement(String name,DOMElement parentElement)
    {
        this.name = name;
        this.parentElement = parentElement;
    }

    public String getName()
    {
        return name;
    }

    public DOMElement getParentDOMElement()
    {
        return parentElement;
    }

    public ArrayList<DOMAttr> getDOMAttributeList()
    {
        return attribs;
    }

    public void initDOMAttribList(int count)
    {
        // Aunque luego sea alguno menos (el style de los View no se guarda aquí por ej.) no importa, así evitamos que reconstruya el array interno
        this.attribs = new ArrayList<DOMAttr>(count);
    }

    public void addDOMAttribute(DOMAttr attr)
    {
        attribs.add(attr);
    }

    public DOMAttr findDOMAttribute(String namespaceURI, String name)
    {
        for(int i = 0; i < attribs.size(); i++)
        {
            DOMAttr attr = attribs.get(i);
            String currNamespaceURI = attr.getNamespaceURI();
            if (!ValueUtil.equalsNullAllowed(currNamespaceURI, namespaceURI)) continue;
            String currName = attr.getName(); // El nombre devuelto no contiene el namespace
            if (!name.equals(currName)) continue;
            return attr;
        }
        return null;
    }

    public LinkedList<DOMElement> getChildDOMElementList()
    {
        return childList;
    }

    public void addChildDOMElement(DOMElement domElement)
    {
        if (childList == null) this.childList = new LinkedList<DOMElement>();
        childList.add(domElement);
    }



    /*
    ToDo: use this XPath navigation advice: http://stackoverflow.com/questions/26159603/need-to-identify-xpath-for-android-element-using-appium

    EditTextLinearLayout[?]/EditTextorg.itsnat.itsnatdroidtest.testact.util.CustomScrollView[?]/EditTextLinearLayout[?]/EditText'
    TextViewLinearLayout/TextVieworg.itsnat.itsnatdroidtest.testact.util.CustomScrollView/TextViewLinearLayout/TextView
     */
    public String zzgetSourceXPath()
    {
        return "HERE_AAE: what? " + name;
    }

    public String getSourceXPath() {
        String outResult = name + "[" + "id" + "]";

        DOMElement focusedElement = parentElement;
        int onIteration = 0;
        while (focusedElement != null) {
            onIteration++;
            // ToDo: get the attribute id=?
            outResult = focusedElement.name + "[" + "id" + "]" + "/" + outResult;
            //outResult = focusedElement.name +  " " + onIteration + "/" + outResult;
            focusedElement = focusedElement.parentElement;
            if (onIteration > 15)
            {
                outResult = "HALT // " + outResult;
                break;
            }
        }

        //android.util.Log.i("INFLATEXML", "How can it be? " + outResult + " 1: " + sourceXPath);

        return "HERE_AAA: " + outResult;
    };

    public void setSourcePath(String inPath) {
        sourceXPath = inPath;
    }
}
