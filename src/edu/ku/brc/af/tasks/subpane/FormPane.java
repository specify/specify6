package edu.ku.brc.af.tasks.subpane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.View;

/**
 * A class that can display a form, it is dervied from DroppableTaskPane which means objects can be
 * dropped on it as long as the data is a instanceof DroppableFormObject.

 * @code_status Complete
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class FormPane extends DroppableTaskPane
{
    protected String         viewSetName   = null;
    protected String         viewName      = null;
    protected Object         data          = null;
    protected RecordSetIFace recordSet     = null;

    protected MultiView      multiView     = null;
    protected FormProcessor  formProcessor = null;

    protected String         cacheDesc     = null;
    protected ImageIcon      icon          = null;

    /**
     * Creates a form pane for a task.
     * @param session the DB session to use
     * @param name the name of the pane
     * @param task the owning task
     * @param desc string displayed in the center of the pane
     */
    public FormPane(final String   name,
                    final Taskable task,
                    final String   desc)
    {
        super(name, task, desc);
    }

    /**
     * Creates a form pane for a task.
     * @param name the name of the pane
     * @param task the owning task
     * @param viewSetName the name of the view set to use
     * @param viewName the ID of the form to be created from within the ViewSet
     * @param data the data to fill the form
     * @param options the options needed for creating the form
     */
    public FormPane(final String   name,
                    final Taskable task,
                    final String   viewSetName,
                    final String   viewName,
                    final String   mode,
                    final Object   data,
                    final int      options)
    {
        this(name, task, "");

        this.viewSetName = viewSetName;
        this.viewName    = viewName;
        this.data        = data;

        createForm(viewSetName, viewName, AltView.parseMode(mode, AltView.CreationMode.View), data, options);
    }

    /**
     * Creates a form pane for a task.
     * @param name the name of the pane
     * @param task the owning task
     * @param view the view to use
     * @param data the data to fill the form
     * @param options the options needed for creating the form
     */
    public FormPane(final String   name,
                    final Taskable task,
                    final View     view,
                    final String   mode,
                    final Object   data,
                    final int      options)
    {
        this(name, task, "");

        this.viewSetName = view.getViewSetName();
        this.viewName    = view.getName();
        this.data        = data;

        createForm(view, AltView.parseMode(mode, AltView.CreationMode.View), data, options);
    }

    /**
     * Returns the processor.
     * @return the processor
     */
    public FormProcessor getFormProcessor()
    {
        return formProcessor;
    }

    /**
     * Sets the processor.
     * @param formProcessor the processor
     */
    public void setFormProcessor(FormProcessor formProcessor)
    {
        this.formProcessor = formProcessor;
        formProcessor.setViewable(this);
    }

    /**
     * Sets the Icon.
     * @param icon the icon for the pane
     */
    public void setIcon(ImageIcon icon)
    {
        this.icon = icon;
    }
    
    
    /**
     * Returns the recordset that was used to fill this form.
     * @return the recordset that was used to fill this form.
     */
    public RecordSetIFace getRecordSet()
    {
        return recordSet;
    }

    /**
     * Sets the recordset that was used to fill this form.
     * @param recordSet the recordset
     */
    public void setRecordSet(RecordSetIFace recordSet)
    {
        this.recordSet = recordSet;
        
        multiView.getCurrentView().setRecordSet(recordSet);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#getMultiView()
     */
    public MultiView getMultiView()
    {
        return multiView;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#shutdown()
     */
    @Override
    public void shutdown()
    {
        recordSet = null;
        
        if (multiView != null)
        {
            multiView.shutdown();
            multiView = null;
        }
        
        super.shutdown(); // closes session
    }

    
    //-----------------------------------------------
    // CommandListener Interface
    //-----------------------------------------------
    public void doCommand(CommandAction cmdAction)
    {
        System.out.println(cmdAction);
    }

    //-----------------------------------------------
    // GhostActionable Interface
    //-----------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#doAction(edu.ku.brc.ui.dnd.GhostActionable)
     */
    @Override
    public void doAction(final GhostActionable src)
    {
        if (src != null)
        {

            Object srcData = src.getData();
            if (srcData != null && srcData instanceof DroppableFormObject)
            {
                DroppableFormObject dfo = (DroppableFormObject)srcData;
                
                createForm(dfo.getViewSetName(), 
                           DBTableIdMgr.getInstance().getDefaultFormNameById(dfo.getFormId()), 
                           AltView.CreationMode.View, 
                           dfo.getData(), 
                           MultiView.VIEW_SWITCHER);
             }
        }
    }

    /**
     * Creates a form from the viewSet name and id and sets the data in
     * @param viewSetNameArg the name of the view set to use.
     * @param viewNameArg the ID of the form to be created from within the ViewSet
     * @param mode the creation mode
     * @param dataArg the data to fill the form
     * @param options the options needed for creating the form
     */
    public void createForm(final String  viewSetNameArg,
                           final String  viewNameArg,
                           final AltView.CreationMode mode,
                           final Object  dataArg,
                           final int     options)
    {
        this.viewSetName = viewSetNameArg;
        this.viewName    = viewNameArg;

        createForm(AppContextMgr.getInstance().getView(viewSetName, viewName), mode, dataArg, options);
    }

    /**
     * Creates a form from the view and sets the data in; NOTE: This method will automatically determine whether
     * the RESULTSET_CONTROLLER should be turned on.
     *  
     * @param view the view to use (throws RuntimeException if null)
     * @param mode the creation mode
     * @param dataArg the data to fill the form
     * @param options the options needed for creating the form
     */
    public void createForm(final View    view,
                           final AltView.CreationMode mode,
                           final Object  dataArg,
                           final int     options)
    {
        if (view != null)
        {
            name = view.getName(); // names the Tab

            // Clear the MultiView.RESULTSET_CONTROLLER bit and then reset it if it needs to be set
            //MultiView.printCreateOptions("Before", options);
            int opts = options;
            opts &= ~MultiView.RESULTSET_CONTROLLER; // Clear Bit first
            opts |= (dataArg != null && (dataArg instanceof List || dataArg instanceof Set)) ? MultiView.RESULTSET_CONTROLLER : 0;
                       
            multiView = new MultiView(null, null, view, mode, options);
            if (multiView != null)
            {
                this.data = dataArg;
                this.removeAll();

                multiView.invalidate();
                add(multiView, BorderLayout.NORTH);

                if (data != null)
                {
                    multiView.setData(data);
                }

                // Tells it is is a new form and all the validator painting should be supressed
                // on required fields until the user inputs something
                if (MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT))
                {
                    multiView.setIsNewForm(true, false); // traverse immediate children only
                }

                if (multiView.getCurrentView().getValidator() != null)
                {
                    multiView.getCurrentView().getValidator().validateForm();
                }

                cacheDesc = desc;
                desc      = null;
                doLayout();
                UIRegistry.forceTopFrameRepaint();

            }
        } else
        {
            throw new RuntimeException("The View was null!");
        }
    }

    /**
     * Removes any forms that have been created and the default message displays.
     */
    public void clearForm()
    {
        this.removeAll();
        desc = cacheDesc;
        data = null;

        if (formProcessor != null)
        {
            formProcessor.setViewable(null);
            formProcessor = null;
        }
        doLayout();
        UIRegistry.forceTopFrameRepaint();
    }

    /**
     * Helper to create a form component from the View Set Name and the Id.
     * @param viewSetName the view set name to get the ID from
     * @param viewName the viewName within the view set
     * @param data the data to fill into the form
     * @return the form component
     */
    /*public static Viewable createFormView(final String viewSetName, final String viewName, final Object data)
    {
        // create form
        View view = AppContextMgr.getInstance().getView(viewSetName, viewName);
        if (view != null)
        {
            Viewable form = ViewFactory.createFormView(null, view, null, data, false);
            if (form != null)
            {
                return form;

            } else
            {
                UIRegistry.displayErrorDlg(getResourceString("cantcreateform")+" viewset name["+viewSetName+"]  id["+viewName+"]");
            }
        } else
        {
            UIRegistry.displayErrorDlg(getResourceString("cantfindviewdef")+" viewset name["+viewSetName+"]  id["+viewName+"]");
        }
        return null;
    }*/

    /**
     * Return the ID of the form.
     * @return the ID of the form
     */
    public String getViewName()
    {
        return viewName;
    }

    /**
     * Return the view set name.
     * @return the view set name
     */
    public String getViewSetName()
    {
        return viewSetName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getData()
     */
    @Override
    public Object getData()
    {
        return data;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setActive(boolean)
     */
    public void setActive(boolean isActive)
    {
        // Auto-generated method stub
    }
    
    /**
     * Returns the Viewable for this FormPane.
     * @return the Viewable for this FormPane
     */
    public Viewable getViewable()
    {
        return multiView != null ? multiView.getCurrentView() : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#aboutToShutdown()
     */
    @Override
    public boolean aboutToShutdown()
    {
        if (multiView != null)
        {
            FormViewObj formViewObj = multiView.getCurrentViewAsFormViewObj();
            if (formViewObj != null)
            {
                return formViewObj.checkForChanges();
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#getIcon()
     */
    @Override
    public ImageIcon getIcon()
    {
        return icon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#getFirstFocusable()
     */
    @Override
    public Component getFirstFocusable()
    {
        return multiView != null ? multiView.getFirstFocusable() : null;
    }

}
