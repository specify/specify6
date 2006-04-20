/* Filename:    $RCSfile: SearchDialogFactory.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/10 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.ui.db;

import static edu.ku.brc.specify.helpers.XMLHelper.getAttr;
import static edu.ku.brc.specify.helpers.XMLHelper.readFileToDOM4J;

import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.specify.exceptions.ConfigurationException;
import edu.ku.brc.specify.helpers.XMLHelper;
/**
 * This factory knows how to create GenericSearchDialogs by name.
 *
 * @author rods
 *
 */
public class DialogFactory
{
    private final static Logger  log        = Logger.getLogger(DialogFactory.class);

    protected static DialogFactory instance = new DialogFactory();

    protected Hashtable<String, DialogInfo> searchDialogs = new Hashtable<String, DialogInfo>();
    protected Hashtable<String, DialogInfo> dialogs       = new Hashtable<String, DialogInfo>();

    /**
     * Protected Constructor
     */
    protected  DialogFactory()
    {
        // These will eventually be defined in an XML file.
/*
        dialogs.put("AgentSearch", new DialogInfo("Search", "AgenSearch", "AgentAddressSearch",
                getResourceString("AgentSearchTitle"),
                "edu.ku.brc.specify.datamodel.Agent",
                "agentId"));

        dialogs.put("PermitSearch", new DialogInfo("Search", "PermitSearch", "PermitSearch",
                getResourceString("PermitSearchTitle"),
                "edu.ku.brc.specify.datamodel.Permit",
                "permitId"));
                */
        init();
    }

    /**
     *
     */
    protected void init()
    {
        try
        {
            Element root = readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath("dialog_defs.xml")));
            //Element  root     = document.getRootElement();
            if (root != null)
            {
                for ( Iterator i = root.elementIterator( "dialog" ); i.hasNext(); )
                {
                    Element fileElement = (Element) i.next();
                    String  type        = getAttr(fileElement, "type", "display");
                    String  name        = getAttr(fileElement, "name", null);
                    DialogInfo di = new DialogInfo(getAttr(fileElement, "viewset", null),
                                                    getAttr(fileElement, "view", null),
                                                    name,
                                                    getAttr(fileElement, "title", null),
                                                    getAttr(fileElement, "class", null),
                                                    getAttr(fileElement, "idfield", null));

                    if (type.equalsIgnoreCase("search"))
                    {
                        searchDialogs.put(name, di);
                    } else
                    {
                        dialogs.put(name, di);
                    }
                }
            } else
            {
                String msg = "The root element for the document was null!";
                log.error(msg);
                throw new ConfigurationException(msg);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
    }


    /**
     * Creates a new GenericSearchDialog by name
     * @param name the name of the GenericSearchDialog to return
     * @return a GenericSearchDialog by name
     */
    public static GenericSearchDialog createSearchDialog(final String name)
    {
        DialogInfo info =  instance.searchDialogs.get(name);
        if (info != null)
        {
            return new GenericSearchDialog(info.getViewSetName(),
                                             info.getViewName(),
                                             info.getDialogName(),
                                             info.getTitle(),
                                             info.getClassName(),
                                             info.getIdFieldName()
                                             );
        } else
        {
            throw new RuntimeException("Couldn't create GenericSearchDialog by name["+name+"]");
        }
    }

    /**
     * Creates a new GenericSearchDialog by name
     * @param name the name of the GenericSearchDialog to return
     * @return a GenericSearchDialog by name
     */
    public static GenericDisplayFrame createDisplayDialog(final String name)
    {
        DialogInfo info =  instance.dialogs.get(name);
        if (info != null)
        {
            return new GenericDisplayFrame(info.getViewSetName(),
                                            info.getViewName(),
                                            info.getDialogName(),
                                            info.getTitle(),
                                            info.getClassName(),
                                            info.getIdFieldName()
                                            );
        } else
        {
            throw new RuntimeException("Couldn't create GenericDisplayFrame by name["+name+"]");
        }
    }

    //-----------------------------------------------------
    //-- Inner Classes
    //-----------------------------------------------------
    class DialogInfo
    {
        protected String viewSetName;
        protected String viewName;
        protected String dialogName;
        protected String title;
        protected String className;
        protected String idFieldName;

        public DialogInfo(String viewSetName,
                          String viewName,
                          String dialogName,
                          String title,
                          String className,
                          String idFieldName)
        {
            super();
            // TODO Auto-generated constructor stub
            this.viewSetName = viewSetName;
            this.viewName    = viewName;
            this.dialogName  = dialogName;
            this.title       = title;
            this.className   = className;
            this.idFieldName = idFieldName;
        }

        public String getClassName()
        {
            return className;
        }

        public String getViewName()
        {
            return viewName;
        }

        public String getIdFieldName()
        {
            return idFieldName;
        }

        public String getDialogName()
        {
            return dialogName;
        }

        public String getTitle()
        {
            return title;
        }

        public String getViewSetName()
        {
            return viewSetName;
        }
    }


}
