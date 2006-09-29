/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.ui.ViewBasedDialogFactoryIFace;
import edu.ku.brc.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.ui.db.ViewBasedDisplayFrame;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.db.ViewBasedSearchDialogIFace;

/**
 * This class is the implementation for the ViewBasedDialogFactoryIFace interface for the entire application.
 * <BR><BR>
 * This class reads in dialog/frame definitions from dialog_defs.xml, there are two types of dialog: "search" and "display".
 * Certain UI components use this factory to create dialogs (model or non-model) for searching or displaying child objects.
 * <BR><BR>
 * For example, the TextWithInfo or the ComboBoxFromQuery has buttons that enables the user to pop up a dialog for displaying the current object in the control,
 * or to pop up a search dialog for locating (more precisely the object they desire.
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
public class DBObjDialogFactory implements ViewBasedDialogFactoryIFace
{
    private static final Logger  log             = Logger.getLogger(DBObjDialogFactory.class);

    protected static DBObjDialogFactory instance = null;

    protected Hashtable<String, DialogInfo> searchDialogs = new Hashtable<String, DialogInfo>();
    protected Hashtable<String, DialogInfo> dialogs       = new Hashtable<String, DialogInfo>();

    /**
     * Constructor - enables it to be constructed from its class name, it can only be constructed
     * one time from the default constructor otherwise it throws a RuntimeException.
     */
    public  DBObjDialogFactory()
    {
        if (DBObjDialogFactory.instance == null)
        {
            DBObjDialogFactory.instance = this;

        } else
        {
            throw new RuntimeException("DBObjDialogFactory cannot be instanitated more than once");
        }
        init();
    }

    /**
     * Returns the singleton instance
     * @return the singleton instance
     */
    public static DBObjDialogFactory getInstance()
    {
        return instance;
    }

    /**
     *
     */
    protected void init()
    {
        try
        {
            Element root = AppContextMgr.getInstance().getResourceAsDOM("DialogDefs");
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
            log.error(ex);
            ex.printStackTrace();
            throw new RuntimeException("Couldn't load DialogDefs");
        }
    }

    //----------------------------------------------------------
    // ViewBasedDialogFactoryIFace interface
    //----------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ViewBasedDialogFactoryIFace#createSearchDialog(java.lang.String)
     */
    public ViewBasedSearchDialogIFace createSearchDialog(final String name)
    {
        DialogInfo info =  instance.searchDialogs.get(name);
        if (info != null)
        {
            return new DBObjSearchDialog(info.getViewSetName(),
                                         info.getViewName(),
                                         info.getDialogName(),
                                         info.getTitle(),
                                         info.getClassName(),
                                         info.getIdFieldName()
                                         );
        } else
        {
            throw new RuntimeException("Couldn't create object implementing ViewBasedSearchDialogIFace by name["+name+"]");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ViewBasedDialogFactoryIFace#createDisplay(java.lang.String, java.lang.String, java.lang.String, boolean, int, edu.ku.brc.ui.ViewBasedDialogFactoryIFace.FRAME_TYPE)
     */
    public ViewBasedDisplayIFace createDisplay(final String name,
                                               final String     frameTitle,
                                               final String     closeBtnTitle,
                                               final boolean    isEdit,
                                               final int        options,
                                               final FRAME_TYPE type)
    {
        DialogInfo info =  instance.dialogs.get(name);
        if (info != null)
        {
            if (type == ViewBasedDialogFactoryIFace.FRAME_TYPE.FRAME)
            {
                return new ViewBasedDisplayFrame(info.getViewSetName(),
                                                info.getViewName(),
                                                info.getDialogName(),
                                                frameTitle,
                                                closeBtnTitle,
                                                info.getClassName(),
                                                info.getIdFieldName(),
                                                isEdit,
                                                options);
            } else
            {
                return new ViewBasedDisplayDialog(info.getViewSetName(),
                                                  info.getViewName(),
                                                  info.getDialogName(),
                                                  frameTitle,
                                                  closeBtnTitle,
                                                  info.getClassName(),
                                                  info.getIdFieldName(),
                                                  isEdit,
                                                  options);
            }
        } else
        {
            throw new RuntimeException("Couldn't create ViewBasedDisplayFrame by name["+name+"]");
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
