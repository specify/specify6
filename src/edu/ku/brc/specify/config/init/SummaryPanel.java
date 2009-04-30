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
package edu.ku.brc.specify.config.init;

import java.awt.Component;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.BiColorTableCellRenderer;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 12, 2009
 *
 */
public class SummaryPanel extends BaseSetupPanel
{
    protected Vector<BaseSetupPanel> panels;
    protected JTable                 table;
    protected DefaultTableModel      model;  
    
    /**
     * @param panelName
     * @param nextBtn
     */
    public SummaryPanel(final String  panelName,
                        final String  helpContext, 
                        final JButton nextBtn,
                        final JButton prevBtn,
                        final Vector<BaseSetupPanel> panels)
    {
        super(panelName, helpContext, nextBtn, prevBtn, false);
        
        this.panels = panels;
        
        table = new JTable();
        model = new DefaultTableModel();
        table.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false));

        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"), this);
        
        pb.add(UIHelper.createScrollPane(table), cc.xy(1, 1));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingNext()
     */
    @Override
    public void doingNext()
    {
        Vector<Pair<String, String>> values = new Vector<Pair<String,String>>();
        for (BaseSetupPanel p : panels)
        {
            List<Pair<String, String>> list = p.getSummary();
            if (list != null)
            {
                values.addAll(list);
            }
            values.add(new Pair<String, String>("", ""));
        }
        
        int i = 0;
        Object[][] valueObjs = new Object[values.size()][2];
        for (Pair<String, String> p : values)
        {
            valueObjs[i][0] = p.first;
            valueObjs[i][1] = p.second;
            i++;
        }
        
        table.setModel(new DefaultTableModel(valueObjs, new String[] {"Name", "Value"}));
        
        UIHelper.makeTableHeadersCentered(table, false);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getSummary()
     */
    @Override
    public List<Pair<String, String>> getSummary()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getUIComponent()
     */
    @Override
    public Component getUIComponent()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getValues(java.util.Properties)
     */
    @Override
    public void getValues(Properties props)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#setValues(java.util.Properties)
     */
    @Override
    public void setValues(Properties values)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#updateBtnUI()
     */
    @Override
    public void updateBtnUI()
    {
    }

}
