/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.tasks.subpane.formeditor;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;

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

import org.apache.commons.lang.NotImplementedException;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.af.ui.forms.persist.FormRowIFace;
import edu.ku.brc.af.ui.forms.persist.FormViewDef;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIRegistry;

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
        PanelBuilder pb = new PanelBuilder(new FormLayout("p:g,4px,p", "p"), this); //$NON-NLS-1$ //$NON-NLS-2$

        label   = createLabel("        "); //$NON-NLS-1$
        editBtn = createButton("Edit");
        
        pb.add(label,   cc.xy(1, 1));
        pb.add(editBtn, cc.xy(3, 1));
        
        editBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                edit();
            }
        });
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#canCarryForward()
     */
    @Override
    public boolean canCarryForward()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getCarryForwardFields()
     */
    @Override
    public String[] getCarryForwardFields()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getTitle()
     */
    @Override
    public String getTitle()
    {
        return "DefItemEditorPlugin";
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
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        throw new NotImplementedException("isNotEmpty not implement!");
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
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#initialize(java.util.Properties, boolean)
     */
    public void initialize(Properties properties, boolean isViewMode)
    {
        isRow = properties.get("type").equals("rowDef");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setCellName(java.lang.String)
     */
    public void setCellName(String cellName)
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener(ChangeListener listener)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#shutdown()
     */
    public void shutdown()
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setViewable(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void setParent(FormViewObj parent)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return null;
    }
}
