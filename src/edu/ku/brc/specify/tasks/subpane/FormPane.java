package edu.ku.brc.specify.tasks.subpane;

import java.awt.BorderLayout;
import java.awt.Component;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.dnd.GhostActionable;

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
    protected String viewSetName = null;
    protected int    formId      = -1;
    protected Object data        = null; 
    
    /**
     * @param name the name of the pane
     * @param task the owning task
     * @param desc a description to display until a component is added to the pane
     */
    public FormPane(final String name, 
                    final Taskable task,
                    final String desc)
    {
        super(name, task, desc);
    }
    
    /**
     * @param name the name of the pane
     * @param task the owning task
     * @param viewSetName the name of the view set to use 
     * @param formId the ID of the form to be created from within the ViewSet
     * @param data the data to fill the form
     */
    public FormPane(final String name, 
                    final Taskable task,
                    final String viewSetName,
                    final int    formId,
                    final Object data)
    {
        this(name, task, null);
        
        this.viewSetName = viewSetName;
        this.formId      = formId;
        this.data        = data;
        
        createForm(viewSetName, formId, data);
    }
    
    /**
     * Cleanup
     */
    public void finalize()
    {
        this.viewSetName = null;
        this.formId      = -1;
        this.data        = null;
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
                createForm(dfo.getViewSetName(), dfo.getFormId(), dfo.getData());
             }
        }
    }
    
    /**
     * Creates a form from the viewSet name and id and sets the data in
     * @param viewSetName the name of the view set to use 
     * @param formId the ID of the form to be created from within the ViewSet
     * @param data the data to fill the form
     */
    public void createForm(final String viewSetName,
                           final int    formId,
                           final Object data)
    {
        Component comp = UICacheManager.createForm(viewSetName, formId, data);
        if (comp != null)
        {
            this.viewSetName = viewSetName;
            this.formId      = formId;
            this.data        = data;
            
            this.removeAll();
            
            comp.invalidate(); 
            add(comp, BorderLayout.CENTER);
            desc = null;
            doLayout();
            UICacheManager.forceTopFrameRepaint();
        }

    }

    /**
     * Return the ID of the form
     * @return Return the ID of the form
     */
    public int getFormId()
    {
        return formId;
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

}
