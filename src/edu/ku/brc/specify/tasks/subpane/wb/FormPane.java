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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.forms.ResultSetControllerListener;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 8, 2007
 *
 */
public class FormPane extends JPanel implements ResultSetControllerListener
{
    protected Workbench workbench;
    protected Vector<WorkbenchTemplateMappingItem> headers = new Vector<WorkbenchTemplateMappingItem>();
    protected boolean     hasChanged = false;
    protected Vector<InputPanel> uiComps = new Vector<InputPanel>();
    
    /**
     * @param workbench
     */
    public FormPane(final Workbench workbench)
    {
        setLayout(null);
        
        this.workbench = workbench;
        
        headers.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        Collections.sort(headers);
        
        int maxWidth =0;
        int x = 5;
        int y = 5;
        for (WorkbenchTemplateMappingItem wbtmi : headers)
        {
            InputPanel p = new InputPanel(wbtmi, wbtmi.getCaption()+":", new JTextField(15));
            Dimension size = p.getPreferredSize();
            p.setSize(size);
            p.setPreferredSize(size);
            
            maxWidth = Math.max(p.getLabel().getPreferredSize().width, maxWidth);
            uiComps.add(p);
            add(p);
            p.setLocation(x, y);
            //p.setBounds(x, y, size.width, size.height);
            System.out.println(p.getBounds());
            y += size.height +4;
        }
        
        for (InputPanel p : uiComps)
        {
            JLabel    lbl  = p.getLabel();
            Dimension size = lbl.getPreferredSize();
            int diff = maxWidth - size.width;
            System.out.println(size.width+"  "+maxWidth+"  "+diff);
            size.width = maxWidth;
            lbl.setSize(size);
            lbl.setPreferredSize(size);
            Rectangle r = lbl.getBounds();
            r.width = maxWidth;
            lbl.setBounds(r);
            
            r = p.getBounds();
            r.width += diff;
            p.setBounds(r);
            
            //p.invalidate();
            p.validate();
            p.doLayout();
        }
    }
    
    class InputPanel extends JPanel
    {
        protected WorkbenchTemplateMappingItem wbtmi;
        protected JLabel     label;
        protected JComponent comp;
        
        public InputPanel(WorkbenchTemplateMappingItem wbtmi, String label, JComponent comp)
        {
            super(new BorderLayout());
            
            this.wbtmi = wbtmi;
            this.label = new JLabel(label, JLabel.RIGHT);
            this.comp  = comp;
            
            add(this.label, BorderLayout.WEST);
            add(comp, BorderLayout.EAST);
        }
        /**
         * @return the comp
         */
        public JComponent getComp()
        {
            return comp;
        }
        /**
         * @return the label
         */
        public JLabel getLabel()
        {
            return label;
        }
        /**
         * @return the wbtmi
         */
        public WorkbenchTemplateMappingItem getWbtmi()
        {
            return wbtmi;
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexAboutToChange(int, int)
     */
    public boolean indexAboutToChange(int oldIndex, int newIndex)
    {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexChanged(int)
     */
    public void indexChanged(int newIndex)
    {
        WorkbenchRow wbRow = workbench.getWorkbenchRowsAsList().get(newIndex);
        for (InputPanel p : uiComps)
        {
            int col = p.getWbtmi().getViewOrder();
            ((JTextField)p.getComp()).setText(wbRow.getData(col));
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#newRecordAdded()
     */
    public void newRecordAdded()
    {
        // TODO Auto-generated method stub
        
    }
    
    
}
