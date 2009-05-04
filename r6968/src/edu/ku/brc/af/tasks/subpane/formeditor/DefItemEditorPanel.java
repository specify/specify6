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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.persist.FormViewDef;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 27, 2007
 *
 */
public class DefItemEditorPanel extends JPanel
{
    protected CardLayout       cardLayout = new CardLayout();
    protected JPanel           cardPanel;
    
    protected RowColDefPanel   propsPanel;
    protected JPanel           autoPropsPanel;
    
    protected DefItemPropPanel cellItemPanel;
    protected DefItemPropPanel sepItemPanel;
    
    protected JGoodiesDefItem  cellItem;
    protected JGoodiesDefItem  sepItem;
    
    protected JRadioButton     autoCB;
    protected JRadioButton     listCB;
    
    protected boolean          isUsingAuto    = false; 
    
    protected FormViewDef.JGDefItem item;
    protected int              numInUse;
    
    /**
     * 
     */
    public DefItemEditorPanel(final FormViewDef.JGDefItem item, 
                              final int     numInUse,
                              final boolean isRow)
    {
        super(new BorderLayout());
        
        this.item     = item;
        this.numInUse = numInUse;
        
        createUI(numInUse, isRow);
        
        String cellStr = item.getCellDefStr();
        String sepStr  = item.getSepDefStr();
        if (StringUtils.isEmpty(cellStr))
        {
            cellStr = "p"; //$NON-NLS-1$
        }
        if (StringUtils.isEmpty(sepStr))
        {
            sepStr = "2px"; //$NON-NLS-1$
        }
        cellItem = new JGoodiesDefItem(cellStr, isRow);
        sepItem  = new JGoodiesDefItem(sepStr, isRow);
        
        cellItemPanel.setCurrentItem(cellItem);
        sepItemPanel.setCurrentItem(sepItem);
    }

    /**
     * @param numInUseArg
     * @param isRow
     */
    protected void createUI(final int     numInUseArg,
                            final boolean isRow)
    {
        propsPanel     = new RowColDefPanel(item, numInUseArg, isRow);
        autoPropsPanel = createAutoPropertyPanel(numInUseArg, isRow);
        
        cardPanel = new JPanel(cardLayout);
        cardPanel.add("Auto",  autoPropsPanel);
        cardPanel.add("List",  propsPanel);
        
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                isUsingAuto = autoCB.isSelected();
                cardLayout.show(cardPanel, isUsingAuto ? "Auto" : "List");
            }
        };
        autoCB = new JRadioButton("Auto");
        autoCB.addActionListener(action);
        listCB = new JRadioButton("List");
        listCB.addActionListener(action);
        ButtonGroup group = new ButtonGroup();
        group.add(autoCB);
        group.add(listCB);
        
        autoCB.setSelected(true);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g,p,16px,p,f:p:g", "p")); //$NON-NLS-1$ //$NON-NLS-2$
        pb.add(listCB, cc.xy(2, 1));
        pb.add(autoCB, cc.xy(4, 1));
        
        add(pb.getPanel(), BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        
        cardLayout.show(cardPanel, "List");
        listCB.setSelected(true);
        isUsingAuto = false;

    }
    
    /**
     * @param numInUseArg
     * @param isRow
     * @return
     */
    protected JPanel createAutoPropertyPanel(final int     numInUseArg,
                                             final boolean isRow)
    {
        
        cellItemPanel = new DefItemPropPanel(numInUseArg, isRow);
        sepItemPanel = new DefItemPropPanel(numInUseArg, isRow);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,16px,p", "p,2px,p")); //$NON-NLS-1$ //$NON-NLS-2$

        pb.addSeparator((isRow ? "Row" : "Column") + " Item", cc.xy(1, 1));
        pb.add(cellItemPanel, cc.xy(1,3));
        
        pb.addSeparator("Separator", cc.xy(3,1));
        pb.add(sepItemPanel, cc.xy(3,3));
        
        return pb.getPanel();
    }
    
    /**
     * 
     */
    public void getDataFromUI()
    {
        item.setAuto(isUsingAuto);
        
        if (isUsingAuto)
        {
            cellItemPanel.getDataFromUI();
            sepItemPanel.getDataFromUI();
            
            String cellStr = cellItem.toString();
            String sepStr  = sepItem.toString();
            
            item.setCellDefStr(cellStr);
            item.setSepDefStr(sepStr);
            
            StringBuilder sb = new StringBuilder();
            for (int i=0;i<numInUse;i++)
            {
                if (sb.length() > 0) sb.append(',');
                sb.append(cellStr);
                sb.append(',');
                sb.append(sepStr);
            }
            item.setDefStr(sb.toString());
            
        } else
        {
            propsPanel.getDataFromUI();
            item.setDefStr(propsPanel.getDefStr());
        }
        //System.out.println(item);
        
        item.toString();
    }

    /**
     * @return the isUsingAuto
     */
    public boolean isUsingAuto()
    {
        return isUsingAuto;
    }
}
