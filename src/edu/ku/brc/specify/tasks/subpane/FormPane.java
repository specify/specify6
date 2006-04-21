package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Set;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.dnd.GhostActionable;
import edu.ku.brc.specify.ui.forms.MultiView;
import edu.ku.brc.specify.ui.forms.Viewable;
import edu.ku.brc.specify.ui.forms.ViewFactory;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.persist.AltView;
import edu.ku.brc.specify.ui.forms.persist.View;

/**
 * A class that can display a form, it is dervied from DroppableTaskPane which means objects can be
 * dropped on it as long as the data is a instanceof DroppableFormObject
 *
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
    protected Viewable      viewable      = null;
    protected FormProcessor formProcessor = null;

    protected String        cacheDesc   = null;

    /**
     * @param name the name of the pane
     * @param task the owning task
     * @param desc a description to display until a component is added to the pane
     */
    public FormPane(final String   name,
                    final Taskable task,
                    final String   desc)
    {
        super(name, task, desc);
    }

    /**
     * @param name the name of the pane
     * @param task the owning task
     * @param viewSetName the name of the view set to use
     * @param viewName the ID of the form to be created from within the ViewSet
     * @param data the data to fill the form
     */
    public FormPane(final String   name,
                    final Taskable task,
                    final String   viewSetName,
                    final String   viewName,
                    final Object   data)
    {
        this(name, task, null);

        this.viewSetName = viewSetName;
        this.viewName    = viewName;
        this.data        = data;

        createForm(viewSetName, viewName, data);
    }

    /**
     * Returns the processor
     * @return the processor
     */
    public FormProcessor getFormProcessor()
    {
        return formProcessor;
    }

    /**
     * Sets the processor
     * @param formProcessor the processor
     */
    public void setFormProcessor(FormProcessor formProcessor)
    {
        this.formProcessor = formProcessor;
        formProcessor.setViewable(this);
    }

    //-----------------------------------------------
    // GhostActionable Interface
    //-----------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#doAction(edu.ku.brc.specify.ui.dnd.GhostActionable)
     */
    public void doAction(GhostActionable src)
    {
        if (src != null)
        {

            Object data = src.getData();

            if (data != null && data instanceof DroppableFormObject)
            {
                DroppableFormObject dfo = (DroppableFormObject)data;
                createForm(dfo.getViewSetName(), DBTableIdMgr.lookupDefaultFormNameById(dfo.getFormId()), dfo.getData());
             }
        }
    }

    /**
     * Creates a form from the viewSet name and id and sets the data in
     * @param viewSetName the name of the view set to use
     * @param viewName the ID of the form to be created from within the ViewSet
     * @param data the data to fill the form
     */
    public void createForm(final String viewSetName,
                           final String viewName,
                           final Object data)
    {
        View view = ViewMgr.getView(viewSetName, viewName);
        if (view != null)
        {
            boolean isList = data != null && (data instanceof List || data instanceof Set);
            AltView.CreationMode mode = AltView.CreationMode.View;//data != null && (data instanceof List || data instanceof Set) ? AltView.CreationMode.View : AltView.CreationMode.Edit;
            multiView = new MultiView(null, view, mode, isList, true);
            //viewable = multiView.get
            if (multiView != null)
            {
                this.viewSetName = viewSetName;
                this.viewName    = viewName;
                this.data        = data;

                this.removeAll();

                multiView.invalidate();
                add(multiView, BorderLayout.NORTH);

                if (data != null)
                {
                    multiView.setData(data);
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
        }
    }

    /**
     * Removes any forms that have been created and the default message displays
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
     * Helper to create a form component from the View Set Name and the Id
     * @param viewSetName the view set name to get the ID from
     * @param viewName the viewName within the view set
     * @param data the data to fill into the form
     * @return the form component
     */
    public static Viewable createFormView(final String viewSetName, final String viewName, final Object data)
    {
        // create form
        View view = ViewMgr.getView(viewSetName, viewName);
        if (view != null)
        {
            Viewable form = ViewFactory.createFormView(null, view, null, data);
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
    }

    /**
     * Return the ID of the form
     * @return Return the ID of the form
     */
    public String getViewName()
    {
        return viewName;
    }

    /**
     * Return the view set name
     * @return Return the view set name
     */
    public String getViewSetName()
    {
        return viewSetName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#getData()
     */
    public Object getData()
    {
        return data;
    }

    /**
     * Returns the Viewable for this FormPane
     * @return the Viewable for this FormPane
     */
    public Viewable getViewable()
    {
        return multiView.getCurrentView();
    }


}
