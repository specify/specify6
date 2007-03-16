/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;
import edu.ku.brc.ui.forms.ResultSetControllerListener;
import edu.ku.brc.ui.validation.ValFormattedTextField;
import edu.ku.brc.ui.validation.ValTextField;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 8, 2007
 *
 */
public class FormPane extends JPanel implements ResultSetControllerListener, GhostActionable, DocumentListener
{
    protected WorkbenchPaneSS    workbenchPane;
    protected Workbench          workbench;
    protected boolean            hasChanged    = false;
    protected Vector<InputPanel> uiComps       = new Vector<InputPanel>();
    protected boolean            ignoreChanges = false;
    protected boolean            isInImageMode = false;

    protected Vector<WorkbenchTemplateMappingItem> headers = new Vector<WorkbenchTemplateMappingItem>();
    
    protected List<DataFlavor>   dropFlavors  = new ArrayList<DataFlavor>();
    
    /**
     * Creates a Pane for editing a Workbench as a form.
     * @param workbenchPane the workbench pane to be parented into
     * @param workbench the workbench
     */
    public FormPane(final WorkbenchPaneSS workbenchPane, final Workbench workbench)
    {
        setLayout(null);
        
        this.workbenchPane = workbenchPane;
        this.workbench     = workbench;
        
        dropFlavors.add(InputPanel.INPUTPANEL_FLAVOR);
        
        headers.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        Collections.sort(headers);
        
        int maxWidth =0;
        int x = 5;
        int y = 5;
        for (WorkbenchTemplateMappingItem wbtmi : headers)
        {
            InputPanel p = new InputPanel(wbtmi, wbtmi.getCaption()+":", createUIComp(wbtmi));
            p.createMouseInputAdapter(); // this makes it draggable

            Dimension size = p.getPreferredSize();
            p.setSize(size);
            p.setPreferredSize(size);
            
            maxWidth = Math.max(p.getLabel().getPreferredSize().width, maxWidth);
            uiComps.add(p);
            add(p);
            
            // NOTE: that the constructor sets the location from WorkbenchTemplateMappingItem object
            if (wbtmi.getXCoord() == null || wbtmi.getYCoord() == null || wbtmi.getXCoord() == -1 || wbtmi.getYCoord() == -1)
            {
                p.setLocation(x, y);
                y += size.height + 4;
            }
            
        }
        
        for (InputPanel p : uiComps)
        {
            JLabel    lbl  = p.getLabel();
            Dimension size = lbl.getPreferredSize();
            int       diff = maxWidth - size.width;
            size.width = maxWidth;
            lbl.setSize(size);
            lbl.setPreferredSize(size);
            Rectangle r = lbl.getBounds();
            r.width = maxWidth;
            lbl.setBounds(r);
            
            r = p.getBounds();
            r.width += diff;
            p.setBounds(r);
            
            p.validate();
            p.doLayout();
        }
    }

    
    /**
     * Returns a UI component for editing.
     * @param wbtmi the mapping item
     * @return a UI component for editing
     */
    protected JComponent createUIComp(final WorkbenchTemplateMappingItem wbtmi)
    {
        //System.out.println(wbtmi.getCaption()+" "+wbtmi.getDataType());
        String type = wbtmi.getDataType();
        
        if (type.equals("calendar_date"))
        {
             return new ValFormattedTextField("Date"); 
        }
        return new ValTextField(15);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexAboutToChange(int, int)
     */
    public boolean indexAboutToChange(int oldIndex, int newIndex)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexChanged(int)
     */
    public void indexChanged(int newIndex)
    {
        ignoreChanges = true; 
        WorkbenchRow wbRow = workbench.getWorkbenchRowsAsList().get(newIndex);
        for (InputPanel p : uiComps)
        {
            int col = p.getWbtmi().getViewOrder();
            
            if (p.getComp() instanceof GetSetValueIFace)
            {
                ((GetSetValueIFace)p.getComp()).setValue(wbRow.getData(col), wbRow.getData(col));
                
            } else
            {
                ((JTextField)p.getComp()).setText(wbRow.getData(col));
            }
        }
        ignoreChanges = false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#newRecordAdded()
     */
    public void newRecordAdded()
    {
        // TODO Auto-generated method stub
    }
    
    //-----------------------------------------------
    // GhostActionable Interface
    //-----------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#doAction(edu.ku.brc.ui.dnd.GhostActionable)
     */
    public void doAction(GhostActionable src)
    {
        if (src instanceof InputPanel)
        {
            InputPanel inputPanel = (InputPanel)src;
            Point offSet   = src.getMouseInputAdapter().getOffsetFromStartPnt();
            Point location = inputPanel.getLocation();
            Point offset   = ((GhostGlassPane)UICacheManager.get(UICacheManager.GLASSPANE)).getOffset();
            location.translate(offSet.x - offset.x, offSet.y - offset.y);
            inputPanel.setLocation(location);
            remove(inputPanel);
            add(inputPanel);
            repaint();
            validate();
            doLayout();
            workbenchPane.setChanged(true);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setData(java.lang.Object)
     */
    public void setData(final Object data)
    {
        //this.data = data;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getData()
     */
    public Object getData()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataForClass(java.lang.Class)
     */
    public Object getDataForClass(Class classObj)
    {
        return UIHelper.getDataForClass(this, classObj);
    }
   
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#createMouseInputAdapter()
     */
    public void createMouseInputAdapter()
    {
    }
    
    /**
     * Returns the adaptor for tracking mouse drop gestures
     * @return Returns the adaptor for tracking mouse drop gestures
     */
    public GhostMouseInputAdapter getMouseInputAdapter()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getBufferedImage()
     */
    public BufferedImage getBufferedImage() 
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataFlavor()
     */
    public List<DataFlavor> getDropDataFlavors()
    {
        return dropFlavors;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDragDataFlavors()
     */
    public List<DataFlavor> getDragDataFlavors()
    {
        return null; // this is not draggable
    }
    
    //-----------------------------------------------
    // DocumentListener Interface
    //-----------------------------------------------
    public void insertUpdate(DocumentEvent e) 
    {
        if (!ignoreChanges)
        {
            workbenchPane.setChanged(true);
        }
    }
    
    public void removeUpdate(DocumentEvent e) 
    {
        if (!ignoreChanges)
        {
            workbenchPane.setChanged(true);
        }
    }
    
    public void changedUpdate(DocumentEvent e) 
    {
        if (!ignoreChanges)
        {
            workbenchPane.setChanged(true);
        }
    }
}
