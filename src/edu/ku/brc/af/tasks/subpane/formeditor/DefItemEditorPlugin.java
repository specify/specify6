/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.tasks.subpane.formeditor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIPluginable;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.persist.FormRowIFace;
import edu.ku.brc.ui.forms.persist.FormViewDef;

/**
 * Implementation of a Google Earth Export plugin for the form system.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Oct 17, 2007
 *
 */
public class DefItemEditorPlugin extends JPanel implements GetSetValueIFace, UIPluginable
{
    protected String  defStr;
    protected JButton editBtn;
    protected JLabel  label;
    
    protected FormViewDef formViewDef;
    protected boolean     isRow = false;
    
    protected FormViewDef.JGDefItem item;
    
    /**
     * 
     */
    public DefItemEditorPlugin()
    {
        defStr = null;
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("p:g,4px,p", "p"), this);

        label   = new JLabel("        ");
        editBtn = new JButton("Edit");
        
        pb.add(label,   cc.xy(1, 1));
        pb.add(editBtn, cc.xy(3, 1));
        
        editBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                edit();
            }
        });
    }
    
    protected void edit()
    {
        int maxCols = 0;
        if (!isRow)
        {
            for (FormRowIFace row : formViewDef.getRows())
            {
                maxCols = Math.max(row.getCells().size(), maxCols);
            }
        }
        
        item = isRow ? formViewDef.getRowDefItem() : formViewDef.getColumnDefItem();
        
        DefItemEditorPanel panel = new DefItemEditorPanel(item, 
                                                          isRow ? formViewDef.getRows().size()*2 : maxCols*2, 
                                                          true);
        
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), (isRow ? "Row" : "Column") + " Definition Editor", true, panel);
        dlg.setVisible(true);
        
        if (!dlg.isCancelled())
        {
            panel.getDataFromUI();
            defStr = isRow ? formViewDef.getRowDef() : formViewDef.getColumnDef();
            label.setText(defStr);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return formViewDef;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(final Object value, final String defaultValue)
    {
        if (value != null && value instanceof FormViewDef)
        {
            formViewDef = (FormViewDef)value;
            editBtn.setEnabled(true); 
            
            defStr = isRow ? formViewDef.getRowDef() : formViewDef.getColumnDef();
            item   = isRow ? formViewDef.getRowDefItem() : formViewDef.getColumnDefItem();
            
            label.setText(defStr);
            
        } else
        {
            editBtn.setEnabled(false); 
            formViewDef = null;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#initialize(java.util.Properties, boolean)
     */
    public void initialize(Properties properties, boolean isViewMode)
    {
        isRow = properties.get("type").equals("rowDef");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setCellName(java.lang.String)
     */
    public void setCellName(String cellName)
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
     */
    public void setChangeListener(ChangeListener listener)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#shutdown()
     */
    @Override
    public void shutdown()
    {
        // TODO Auto-generated method stub
        
    }
    
    
}
