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

package org.itsnat.impl.core.scriptren.shared;

import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.web.BrowserMSIEOld;

/**
 *
 * @author jmarranz
 */
public class JSAndBSRenderSharedUtil
{
    public static String toLiteralStringScript(String value)
    {
        if (value == null)
            value = "null";
        else
            value = "\"" + value + "\"";
        return value;
    }    
    
    public static String toTransportableStringLiteral(String text,boolean addQuotation,Browser browser)
    {
        StringBuilder encoded = new StringBuilder(text);
        for (int i = 0; i < encoded.length(); i++)
        {
            char c = encoded.charAt(i);
            switch(c)
            {
                case '\r':  encoded.deleteCharAt(i); // Caso Windows (CR), deber� seguir un LF (\n). Lo eliminamos porque en navegadores no MSIE genera dos fines de l�nea, en MSIE lo que haremos ser� a�adir un \r al procesar el \n
                            i--; // Pues el i++ a�ade otro m�s y al eliminar uno no nos hemos movido
                            break;
                case '\n':  encoded.deleteCharAt(i);
                            if (browser instanceof BrowserMSIEOld) // Importante por ejemplo cuando se a�ade din�micamente el nodo de texto a un <textarea> o a un <pre> (no probado pero el problema parece que es el mismo)
                            {
                                encoded.insert(i,"\\r");
                                i += 2; // Pues hemos a�adido dos caracteres
                            }
                            encoded.insert(i,"\\n");
                            i++; // Uno s�lo pues el i++ del for ya a�ade otro m�s
                            break;
                case '"':   encoded.deleteCharAt(i);
                            encoded.insert(i,"\\\"");
                            i++;
                            break;
                case '\'':  if (!addQuotation) // Si la cadena se mete entre "" no hace falta "escapar" la ' 
                            {
                                encoded.deleteCharAt(i);
                                encoded.insert(i,"\\'");
                                i++;
                            }
                            break;
                case '\\':  encoded.deleteCharAt(i);
                            encoded.insert(i,"\\\\");
                            i++;
                            break;
                case '\t':  encoded.deleteCharAt(i);
                            encoded.insert(i,"\\t");
                            i++;
                            break;
                case '\f':  encoded.deleteCharAt(i); // FORM FEED
                            encoded.insert(i,"\\f");
                            i++;
                            break;
                case '\b':  encoded.deleteCharAt(i); // BACK SPACE
                            encoded.insert(i,"\\b");
                            i++;
                            break;
            }
        }

        if (addQuotation)
        {
            if (encoded.indexOf("</script>") != -1) // Raro pero puede ocurrir por ejemplo si el texto es el contenido de un comentario y se procesa por JavaScript como en BlackBerry y S60WebKit en carga o est� en el valor inicial en carga de un input o similar
            {
                String encoded2 = encoded.toString().replaceAll("</script>", "</\" + \"script>");
                //String encoded2 = encoded.toString().replaceAll("</script>", "<\\/script>"); NO VALE, genera un </script> normal
                return "\"" + encoded2 + "\"";
            }
            else
                return "\"" + encoded + "\"";
        }
        else
            return encoded.toString();
    }    
}
