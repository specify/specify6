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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.ViewBasedDialogFactoryIFace;
import edu.ku.brc.af.ui.db.ViewBasedDisplayActionAdapter;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayFrame;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.dbsupport.TaskSemaphoreMgr;
import edu.ku.brc.ui.UIRegistry;

/**
 * This class is the implementation for the ViewBasedDialogFactoryIFace interface for the entire application.
 * <BR><BR>
 * This class reads in dialog/frame definitions from dialog_defs.xml, there are two types of dialog: "search" and "display".
 * Certain UI components use this factory to create dialogs (modal or non-modal) for searching or displaying child objects.
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
    private static final Logger  log         = Logger.getLogger(DBObjDialogFactory.class);
    
    public static final String   factoryName = "edu.ku.brc.specify.ui.DBObjDialogFactory"; //$NON-NLS-1$
    
    public enum FormLockStatus {Skip, OK, GotLock, ViewOnly}
    
    protected static DBObjDialogFactory instance = null;

    protected Hashtable<String, DialogInfo> searchDialogs = new Hashtable<String, DialogInfo>();
    protected Hashtable<String, DialogInfo> dialogs       = new Hashtable<String, DialogInfo>();

    /**
     * Constructor - enables it to be constructed from its class name, it can only be constructed
     * one time from the default constructor otherwise it throws a RuntimeException.
     */
    public DBObjDialogFactory()
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
     * Returns the instance of the DBObjDialogFactory.
     * @return the instance of the DBObjDialogFactory.
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
                for ( Iterator<?> i = root.elementIterator( "dialog" ); i.hasNext(); )
                {
                    Element fileElement = (Element) i.next();
                    String  type        = getAttr(fileElement, "type", "display");
                    String  name        = getAttr(fileElement, "name", null);
                    boolean isDisplay   = type.equals("display");
                    
                    DialogInfo di = new DialogInfo(isDisplay ? null : getAttr(fileElement, "viewset", null),
                                                    getAttr(fileElement, "view", null),
                                                    name,
                                                    getAttr(fileElement, "class", null),
                                                    getAttr(fileElement, "idfield", null),
                                                    getAttr(fileElement, "helpcontext", ""));

                    if (isDisplay)
                    {
                        dialogs.put(name, di);
                    } else
                    {
                        searchDialogs.put(name, di);
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DBObjDialogFactory.class, ex);
            log.error(ex);
            ex.printStackTrace();
            throw new RuntimeException("Couldn't load DialogDefs");
        }
    }

    //----------------------------------------------------------
    // ViewBasedDialogFactoryIFace interface
    //----------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ViewBasedDialogFactoryIFace#createSearchDialog(java.awt.Window, java.lang.String)
     */
    public ViewBasedSearchDialogIFace createSearchDialog(final Window parent, final String name)
    {
        DialogInfo info =  instance.searchDialogs.get(name);
        if (info != null)
        {
            String title = "";
            DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(info.getClassName());
            if (ti != null)
            {
                title = ti.getTitle() + " " + UIRegistry.getResourceString("SEARCH");
            }
            
            if (parent instanceof Frame)
            {
                return new DBObjSearchDialog((Frame)parent,
                                                info.getViewSetName(),
                                                info.getViewName(),
                                                info.getSearchName(),
                                                title,
                                                info.getClassName(),
                                                info.getIdFieldName(),
                                                info.getHelpContext());
            }
            // else
            return new DBObjSearchDialog((Dialog)parent,
                                            info.getViewSetName(),
                                            info.getViewName(),
                                            info.getSearchName(),
                                            title,
                                            info.getClassName(),
                                            info.getIdFieldName(),
                                            info.getHelpContext());                
        }
        // else
        throw new RuntimeException("Couldn't create object implementing ViewBasedSearchDialogIFace by name["+name+"]");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ViewBasedDialogFactoryIFace#createDisplay(java.awt.Window, java.lang.String, java.lang.String, java.lang.String, boolean, int, edu.ku.brc.ui.ViewBasedDialogFactoryIFace.FRAME_TYPE)
     */
    public ViewBasedDisplayIFace createDisplay(final Window     parent, 
                                               final String     name,
                                               final String     frameTitleArg,
                                               final String     closeBtnTitleArg,
                                               final boolean    isEditArg,
                                               final int        optionsArg,
                                               final FRAME_TYPE type)
    {
        boolean isEdit        = isEditArg;
        int     options       = optionsArg;
        String  closeBtnTitle = closeBtnTitleArg;
        
        // Override when trying to parent a Frame to a Dialog, because
        // the Frame will be placed behind the Dialog
        
         DialogInfo info =  instance.dialogs.get(name);
        if (info != null)
        {
            SpecifyAppContextMgr sacm = (SpecifyAppContextMgr)AppContextMgr.getInstance();
            
            String viewSetName = info.getViewSetName();
            String viewName    = info.getViewName();
            
            final ViewIFace view = sacm.getView(null, viewName);
            if (view != null)
            {
                viewSetName = view.getViewSetName();
            } else
            {
                throw new RuntimeException("Couldn't find view ["+viewName+"]");
            }
            
            FormLockStatus lockStatus = isLockOK("LockTitle", view, MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT), isEdit);
            if (lockStatus != FormLockStatus.GotLock)
            {
                if (lockStatus == FormLockStatus.ViewOnly)
                {
                    isEdit = false;
                    options &= ~MultiView.IS_EDITTING; // Clear Bit first
                    options |= MultiView.HIDE_SAVE_BTN;
                    closeBtnTitle = UIRegistry.getResourceString("CLOSE");
                    
                } else if (lockStatus == FormLockStatus.Skip)
                {
                    return null;
                }
            }
            
            String frameTitle = frameTitleArg;
            if (StringUtils.isEmpty(frameTitle))
            {
                DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
                if (ti != null)
                {
                    frameTitle = ti.getTitle();
                }
            }
            
            ViewBasedDisplayIFace viewDisplay;
            if (type == ViewBasedDialogFactoryIFace.FRAME_TYPE.FRAME)
            {
                
                if (parent instanceof Frame)
                {
                    viewDisplay = new ViewBasedDisplayFrame(viewSetName,
                                                             info.getViewName(),
                                                             info.getSearchName(),
                                                             frameTitle,
                                                             closeBtnTitle,
                                                             info.getClassName(),
                                                             info.getIdFieldName(),
                                                             isEdit,
                                                             options);
                } else
                {
                
                    viewDisplay = new ViewBasedDisplayDialog((Dialog)parent,
                                                                viewSetName,
                                                                info.getViewName(),
                                                                info.getSearchName(),
                                                                frameTitle,
                                                                closeBtnTitle,
                                                                info.getClassName(),
                                                                info.getIdFieldName(),
                                                                isEdit,
                                                                options);
                }
                
            } else if (parent instanceof Frame)
            {
                viewDisplay = new ViewBasedDisplayDialog((Frame)parent,
                                                          viewSetName,
                                                          info.getViewName(),
                                                          info.getSearchName(),
                                                          frameTitle,
                                                          closeBtnTitle,
                                                          info.getClassName(),
                                                          info.getIdFieldName(),
                                                          isEdit,
                                                          options);
            } else
            {
                viewDisplay = new ViewBasedDisplayDialog((Dialog)parent,
                                                          viewSetName,
                                                          info.getViewName(),
                                                          info.getSearchName(),
                                                          frameTitle,
                                                          closeBtnTitle,
                                                          info.getClassName(),
                                                          info.getIdFieldName(),
                                                          isEdit,
                                                          options);
            }
            
            if (viewDisplay != null)
            {
                if (lockStatus == FormLockStatus.GotLock)
                {
                    final String ERR_UNLOCKING_FORM = "DBObjDialogFactory.ERR_UNLOCKING_FORM";
                    viewDisplay.setCloseListener(new ViewBasedDisplayActionAdapter() {
                        @Override
                        public boolean cancelPressed(ViewBasedDisplayIFace vbd)
                        {
                            if (!unLockOK("LockTitle", view))
                            {
                                UIRegistry.showLocalizedError(ERR_UNLOCKING_FORM);
                            }
                            return true;
                        }
                        @Override
                        public boolean okPressed(ViewBasedDisplayIFace vbd)
                        {
                            if (!unLockOK("LockTitle", view))
                            {
                                UIRegistry.showLocalizedError(ERR_UNLOCKING_FORM);
                            }
                            return true;
                        }
                    });
                }
            }
            return viewDisplay;
        }
        // else
        throw new RuntimeException("Couldn't create ViewBasedDisplayFrame by name["+name+"] (Check the List of dialog in dailog_defs.xml)");
    }
    
    /**
     * Checks the tree to see if it is locked so a form for the tree can be opened. Note: this method
     * will display a localized error before it returns.
     * 
     * @param lockTitle Title (not important)
     * @param view the view that needs to be opened
     * @param isNewForm whether the form is for a new object
     * @param isEdit whether the form is to edit an object
     * @return FormLockStatus for what happened
     */
    public static FormLockStatus isLockOK(final String    lockTitle, 
                                          final ViewIFace view, 
                                          final boolean   isNewForm, 
                                          final boolean   isEdit)
    {
        Class<?> treeDefClass = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getTreeDefClass(view);
        if (treeDefClass != null)
        {
            String  treeSemaphoreName     = treeDefClass.getSimpleName();
            String  treeFormSemaphoreName = treeDefClass.getSimpleName() + "Form";
            
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
            String       title    = tableInfo.getTitle() + " " + "Tree";
            
            // If this user owns the Tree Form Lock then they can open the View
            if (TaskSemaphoreMgr.doesOwnSemaphore(treeFormSemaphoreName, TaskSemaphoreMgr.SCOPE.Discipline))
            {
                return FormLockStatus.OK;
            }
            
            TaskSemaphoreMgr.USER_ACTION action = TaskSemaphoreMgr.USER_ACTION.OK;
            if (isEdit || isNewForm)
            {
                // Check to see if the Tree Lock is locked
                //if (TaskSemaphoreMgr.isLocked(lockTitle, treeSemaphoreName, TaskSemaphoreMgr.SCOPE.Discipline))
                action = TaskSemaphoreMgr.lock(title, treeSemaphoreName, "def", TaskSemaphoreMgr.SCOPE.Discipline, !isNewForm && isEdit);
                if (action != TaskSemaphoreMgr.USER_ACTION.OK)
                {
                    if (action == TaskSemaphoreMgr.USER_ACTION.Cancel)
                    {
                        return FormLockStatus.Skip;
                    }
                    if (isNewForm)
                    {
                        //UIRegistry.showLocalizedError("TREE_LOCKED_NEW_OBJ");
                        return FormLockStatus.Skip;
                    }
                    
                    if (isEdit)
                    {
                        //UIRegistry.showLocalizedError("TREE_LOCKED_EDT_OBJ");
                        return FormLockStatus.ViewOnly;
                    }
                    
                    return FormLockStatus.Skip;
                }
            } else
            {
                return FormLockStatus.ViewOnly;
            }
            
            // First try to grab the tree Lock
            //action = TaskSemaphoreMgr.lock(lockTitle, treeSemaphoreName, "def", TaskSemaphoreMgr.SCOPE.Discipline, false); 
            if (action == TaskSemaphoreMgr.USER_ACTION.OK)
            {
                // Now grab the Tree Form Lock
                action = TaskSemaphoreMgr.lock(title, treeFormSemaphoreName, "def", TaskSemaphoreMgr.SCOPE.Discipline, false);
                if (action != TaskSemaphoreMgr.USER_ACTION.OK)
                {
                    // Since for some bizarre reason we didn't get the treeForm Lock release the tree lock.
                    TaskSemaphoreMgr.unlock(title, treeSemaphoreName, TaskSemaphoreMgr.SCOPE.Discipline);
                    
                    UIRegistry.showLocalizedError("TREE_LOCKED_ERR_FRM");
                    return FormLockStatus.Skip;
                }
                return FormLockStatus.GotLock;
                
            } else
            {
                UIRegistry.showLocalizedError("TREE_LOCKED_ERR");
                return FormLockStatus.Skip;
            }
        }
        return FormLockStatus.OK;
    }
    
    /**
     * @param lockTitle
     * @param view
     * @return
     */
    public static boolean unLockOK(final String    lockTitle, 
                                   final ViewIFace view)
    {
        Class<?> treeDefClass = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getTreeDefClass(view);
        if (treeDefClass != null)
        {
            String  treeSemaphoreName     = treeDefClass.getSimpleName();
            String  treeFormSemaphoreName = treeDefClass.getSimpleName() + "Form";
            
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
            String       title    = tableInfo.getTitle() + " " + "Tree";
            
            // If this user owns the Tree Form Lock then they can open the View
            if (!TaskSemaphoreMgr.doesOwnSemaphore(treeFormSemaphoreName, TaskSemaphoreMgr.SCOPE.Discipline))
            {
                return false;
            }
            
            if (TaskSemaphoreMgr.unlock(title, treeFormSemaphoreName, TaskSemaphoreMgr.SCOPE.Discipline))
            {
                if (TaskSemaphoreMgr.unlock(title, treeSemaphoreName, TaskSemaphoreMgr.SCOPE.Discipline))
                {
                    return true;
                }
                return false;
            }
            return false;
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.ViewBasedDialogFactoryIFace#getSearchName(java.lang.String)
     */
    @Override
    public String getSearchName(String searchDlgName)
    {
        DialogInfo info = instance.searchDialogs.get(searchDlgName);
        return info != null ? info.getSearchName() : null;
    }

    //-----------------------------------------------------
    //-- Inner Classes
    //-----------------------------------------------------
    class DialogInfo
    {
        protected String viewSetName;
        protected String viewName;
        protected String searchName;
        protected String className;
        protected String idFieldName;
        protected String helpContext;

        public DialogInfo(String viewSetName,
                          String viewName,
                          String dialogName,
                          String className,
                          String idFieldName,
                          String helpContext)
        {
            super();
            this.viewSetName = viewSetName;
            this.viewName    = viewName;
            this.searchName  = dialogName;
            this.className   = className;
            this.idFieldName = idFieldName;
            this.helpContext = helpContext;
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

        public String getSearchName()
        {
            return searchName;
        }

        public String getViewSetName()
        {
            return viewSetName;
        }

        public String getHelpContext()
        {
            return helpContext;
        }
    }

}
