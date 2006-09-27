package edu.ku.brc.af.tasks.subpane;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.ui.UICacheManager;
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
    protected String        viewSetName   = null;
    protected String        viewName      = null;
    protected Object        data          = null;

    protected MultiView     multiView     = null;
    protected FormProcessor formProcessor = null;

    protected String        cacheDesc     = null;
    protected ImageIcon     icon          = null;

    /**
     * Creates a form pane for a task.
     * @param session the DB session to use
     * @param name the name of the pane
     * @param task the owning task
     */
    public FormPane(final Session  session,
                    final String   name,
                    final Taskable task)
    {
        super(session, name, task);
    }

    /**
     * Creates a form pane for a task.
     * @param session the DB session to use
     * @param name the name of the pane
     * @param task the owning task
     * @param viewSetName the name of the view set to use
     * @param viewName the ID of the form to be created from within the ViewSet
     * @param data the data to fill the form
     * @param createViewSwitcher can be used to make sure that the multiview switcher is not created
     * @param isNewForm indicates that it is a "new" form for entering in new data
     */
    public FormPane(final Session session,
                    final String   name,
                    final Taskable task,
                    final String   viewSetName,
                    final String   viewName,
                    final String   mode,
                    final Object   data,
                    final boolean  createViewSwitcher,
                    final boolean  isNewForm)
    {
        this(session, name, task);

        this.viewSetName = viewSetName;
        this.viewName    = viewName;
        this.data        = data;

        createForm(viewSetName, viewName, AltView.parseMode(mode, AltView.CreationMode.View), data, createViewSwitcher, isNewForm);
    }

    /**
     * Creates a form pane for a task.
     * @param session the DB session to use
     * @param name the name of the pane
     * @param task the owning task
     * @param view the view to use
     * @param data the data to fill the form
     * @param createViewSwitcher can be used to make sure that the multiview switcher is not created
     * @param isNewForm indicates that it is a "new" form for entering in new data
     */
    public FormPane(final Session session,
                    final String   name,
                    final Taskable task,
                    final View     view,
                    final String   mode,
                    final Object   data,
                    final boolean  createViewSwitcher,
                    final boolean  isNewForm)
    {
        this(session, name, task);

        this.viewSetName = view.getViewSetName();
        this.viewName    = view.getName();
        this.data        = data;

        createForm(view, AltView.parseMode(mode, AltView.CreationMode.View), data, createViewSwitcher, isNewForm);
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

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#shutdown()
     */
    @Override
    public void shutdown()
    {
        multiView.shutdown();
        
        super.shutdown(); // closes session
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
                createForm(dfo.getViewSetName(), DBTableIdMgr.lookupDefaultFormNameById(dfo.getFormId()), AltView.CreationMode.View, dfo.getData(), true, false);
             }
        }
    }

    /**
     * Creates a form from the viewSet name and id and sets the data in
     * @param viewSetNameArg the name of the view set to use.
     * @param viewNameArg the ID of the form to be created from within the ViewSet
     * @param mode the creation mode
     * @param dataArg the data to fill the form
     * @param createViewSwitcher can be used to make sure that the multiview switcher is not created
     * @param isNewForm indicates that it is a "new" form for entering in new data
     */
    public void createForm(final String  viewSetNameArg,
                           final String  viewNameArg,
                           final AltView.CreationMode mode,
                           final Object  dataArg,
                           final boolean createViewSwitcher,
                           final boolean isNewForm)
    {
        this.viewSetName = viewSetNameArg;
        this.viewName    = viewNameArg;

        createForm(AppContextMgr.getInstance().getView(viewSetName, viewName), mode, dataArg, createViewSwitcher, isNewForm);
    }

    /**
     * Creates a form from the view and sets the data in
     * @param view the view to use (throws RuntimeException if null)
     * @param mode the creation mode
     * @param dataArg the data to fill the form
     * @param createViewSwitcher can be used to make sure that the multiview switcher is not created
     * @param isNewForm indicates that it is a "new" form for entering in new data
     */
    public void createForm(final View    view,
                           final AltView.CreationMode mode,
                           final Object  dataArg,
                           final boolean createViewSwitcher,
                           final boolean isNewForm)
    {
        if (view != null)
        {
            name = view.getName(); // names the Tab

            boolean isList = dataArg != null && (dataArg instanceof List || dataArg instanceof Set);
            multiView = new MultiView(null, view, mode, isList, createViewSwitcher, isNewForm);
            if (multiView != null)
            {
                this.data = dataArg;
                this.removeAll();
                
                multiView.setSession(session);

                multiView.invalidate();
                add(multiView, BorderLayout.NORTH);

                if (data != null)
                {
                    multiView.setData(data);
                }

                // Tells it is is a new form and all the validator painting should be supressed
                // on required fields until the user inputs something
                if (isNewForm)
                {
                    multiView.setIsNewForm(true);
                }

                if (multiView.getCurrentView().getValidator() != null)
                {
                    multiView.getCurrentView().getValidator().validateForm();
                }

                cacheDesc = desc;
                desc      = null;
                doLayout();
                UICacheManager.forceTopFrameRepaint();

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
        UICacheManager.forceTopFrameRepaint();
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
                UICacheManager.displayErrorDlg(getResourceString("cantcreateform")+" viewset name["+viewSetName+"]  id["+viewName+"]");
            }
        } else
        {
            UICacheManager.displayErrorDlg(getResourceString("cantfindviewdef")+" viewset name["+viewSetName+"]  id["+viewName+"]");
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

    /**
     * Returns the Viewable for this FormPane.
     * @return the Viewable for this FormPane
     */
    public Viewable getViewable()
    {
        return multiView.getCurrentView();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#aboutToShutdown()
     */
    @Override
    public boolean aboutToShutdown()
    {
        Viewable viewable = multiView.getCurrentView();
        if (viewable instanceof FormViewObj)
        {
            return ((FormViewObj)viewable).checkForChanges();
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

}
