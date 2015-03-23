/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.core.expresssearch;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToggleButtonChooserPanel;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 15, 2007
 *
 */
public class DigirABCConfigPanel extends JPanel
{
    protected SearchConfig config;
    protected JList        tableList;
    protected ToggleButtonChooserPanel<DisplayFieldConfig> displayList;
    
    /**
     * @param config
     */
    public DigirABCConfigPanel(final SearchConfig config,
                               final JButton      okBtn)
    {
        this.config = config;
        
        buildUI(okBtn);
    }
    
    /**
     * 
     */
    protected void buildUI(final JButton okBtn)
    {
        
        Vector<SearchTableConfig> tiRenderList = new Vector<SearchTableConfig>();
        for (SearchTableConfig stc : config.getTables())
        {
            tiRenderList.add(stc);
        }
        Collections.sort(tiRenderList);
        
        tableList = new JList(tiRenderList);
        TableNameRenderer nameRender = new TableNameRenderer(IconManager.IconSize.Std24);
        nameRender.setUseIcon("PlaceHolder"); //$NON-NLS-1$
        tableList.setCellRenderer(nameRender);
        
        tableList.setVisibleRowCount(10);
        JScrollPane sp = new JScrollPane(tableList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
                                                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        tableList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            //@Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    tableSelected();
                }
            }
        });
        
        displayList = new ToggleButtonChooserPanel<DisplayFieldConfig>(15, ToggleButtonChooserPanel.Type.Checkbox);
        displayList.setUseScrollPane(true);
        displayList.setOkBtn(okBtn);
        displayList.setActionListener(new ActionListener() 
        {
            //@Override
            public void actionPerformed(ActionEvent e)
            {
                final JToggleButton tb = (JToggleButton)e.getSource();
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        itemDisplayChecked(tb);
                    }
                });
            }
        });        
        displayList.createUI();
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("p,2px,p", "p,2px,f:p:g"), this); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc      = new CellConstraints();

        builder.add(createLabel(getResourceString("DigirABCConfigPanel.DIGIR_AVAIL_TABLES"), SwingConstants.CENTER), cc.xy(1,1)); //$NON-NLS-1$
        builder.add(createLabel(getResourceString("DigirABCConfigPanel.DIGIR_FIELDNAME"), SwingConstants.CENTER), cc.xy(3,1)); //$NON-NLS-1$
        builder.add(sp, cc.xy(1,3));

        JScrollPane dspSp = new JScrollPane(displayList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        builder.add(dspSp, cc.xy(3, 3));
    }
    
    /**
     * 
     */
    protected void tableSelected()
    {
        SearchTableConfig stc = (SearchTableConfig)tableList.getSelectedValue();
        
        //Collections.sort(stc.getDisplayFields());
        
        displayList.setItems(stc.getDisplayFields());
        
        for (DisplayFieldConfig dfc : stc.getDisplayFields())
        {
            if (dfc.isWebServiceField())
            {
                displayList.setSelectedObj(dfc);
            }
        }
    }

    /**
     * @param btn the btn clicked on
     */
    protected void itemDisplayChecked(final JToggleButton btn)
    {
        DisplayFieldConfig dfc = displayList.getItemForBtn(btn);
        if (btn.isSelected())
        {
            dfc.setInUse(true);
            dfc.setIsWebServiceField(true);
        } else
        {
            dfc.setInUse(false);
            dfc.setIsWebServiceField(false);
        }
    }

}
