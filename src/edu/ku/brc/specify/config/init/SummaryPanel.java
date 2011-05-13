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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
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
    public static final String           PRINT_GRID          = "RPT.PrintTable";

    protected Vector<BaseSetupPanel> panels;
    protected JTable                 table;
    
    protected JTable                 printTable;
    
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
        
        table      = new JTable();
        printTable = new JTable();
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,10px,p"), this);
        
        pb.add(UIHelper.createScrollPane(table), cc.xy(1, 1));
        
        JButton printBtn = UIHelper.createI18NButton("PRINT");
        PanelBuilder lpb = new PanelBuilder(new FormLayout("f:p:g,p", "p"));
        lpb.add(printBtn, cc.xy(2,1));
        
        pb.add(lpb.getPanel(), cc.xy(1, 3));
        
        printBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PrintTableHelper pth = new PrintTableHelper(printTable);
                pth.printGrid(UIRegistry.getResourceString("SUMMARY"));
            }
        });
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#doingNext()
     */
    @Override
    public void doingNext()
    {
        Vector<Pair<String, String>> values      = new Vector<Pair<String,String>>();
        Vector<Pair<String, String>> printValues = new Vector<Pair<String,String>>();
        for (BaseSetupPanel p : panels)
        {
            List<Pair<String, String>> list = p.getSummary();
            if (list != null)
            {
                values.addAll(list);
                printValues.addAll(list);
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
        
        i = 0;
        Object[][] pValueObjs = new Object[printValues.size()][2];
        for (Pair<String, String> p : printValues)
        {
            pValueObjs[i][0] = p.first;
            pValueObjs[i][1] = p.second;
            i++;
        }
        
        String nameStr = UIRegistry.getResourceString("Name");
        String valueStr = UIRegistry.getResourceString("Value");
        
        DefaultTableModel model = new DefaultTableModel(valueObjs, new String[] {nameStr, valueStr})
        {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
        };
        table.setModel(model);
        printTable.setModel(new DefaultTableModel(pValueObjs, new String[] {nameStr, valueStr}));
        
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
