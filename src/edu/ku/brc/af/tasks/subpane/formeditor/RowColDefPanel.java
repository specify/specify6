/* Copyright (C) 2013, University of Kansas Center for Research
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.tasks.subpane.formeditor.JGoodiesDefItem.ALIGN_TYPE;
import edu.ku.brc.af.tasks.subpane.formeditor.JGoodiesDefItem.MINMAX_TYPE;
import edu.ku.brc.af.ui.forms.persist.FormViewDef;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 27, 2007
 *
 */
public class RowColDefPanel extends JPanel
{
    protected JGoodiesDefItem.MINMAX_TYPE[] minMax   = {MINMAX_TYPE.None, MINMAX_TYPE.Min, MINMAX_TYPE.Max};
    protected JGoodiesDefItem.ALIGN_TYPE[]  rowAlign = {ALIGN_TYPE.None, ALIGN_TYPE.Top, ALIGN_TYPE.Bottom};
    protected JGoodiesDefItem.ALIGN_TYPE[]  colAlign = {ALIGN_TYPE.None, ALIGN_TYPE.Left, ALIGN_TYPE.Right};
    
    protected String  def;
    protected boolean isRow;
    protected Vector<JGoodiesDefItem> items = new Vector<JGoodiesDefItem>();
    
    protected JList           itemList;
    
    protected JGoodiesDefItem currentItem = null;
    
    protected DefItemPropPanel   propsPanel;
    protected EditDeleteAddPanel controlPanel;
    
    
    /**
     * @param defStr
     * @param isRow
     */
    public RowColDefPanel(final FormViewDef.JGDefItem item,
                          final int     numInUse,
                          final boolean isRow)
    {
        super(new BorderLayout());
        
        this.isRow = isRow;
        
        createUI(numInUse, isRow);

        DefaultListModel model = (DefaultListModel)itemList.getModel();
        int cnt = 0;
        for (String tok : StringUtils.split(item.getDefStr(), ",")) //$NON-NLS-1$
        {
            JGoodiesDefItem jgItem = new JGoodiesDefItem(tok, isRow);
            jgItem.setInUse(cnt < numInUse);
            items.add(jgItem);
            model.addElement(jgItem);
            cnt++;
        }
    }
    
    protected void createUI(final int     numInUse,
                            @SuppressWarnings("hiding")
                            final boolean isRow)
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("max(125px;p):g,16px,p", "p,2px,p,2px,p:g")); //$NON-NLS-1$ //$NON-NLS-2$

        propsPanel = new DefItemPropPanel(numInUse, isRow);
        
        ActionListener addAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addItem();
            }
        };
        
        ActionListener delAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                delItem();
            }
        };
        
        controlPanel = new EditDeleteAddPanel(null, delAL, addAL);
        controlPanel.getAddBtn().setEnabled(true);
        
        itemList = new JList(new DefaultListModel());
        itemList.setCellRenderer(new DefItemRenderer(IconManager.IconSize.Std16));
        JScrollPane sp = UIHelper.createScrollPane(itemList);
        pb.addSeparator((isRow ? "Row" : "Column") + " Items", cc.xy(1, 1)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        pb.add(sp, cc.xy(1,3));
        pb.add(controlPanel, cc.xy(1,5));
        
        pb.addSeparator("Properties", cc.xy(3, 1)); //$NON-NLS-1$
        pb.add(propsPanel, cc.xywh(3,3,1,3));

        
        itemList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    itemSelected();
                }
            }
        });
        
        add(pb.getPanel(), BorderLayout.CENTER);
    }
    
    public void addItem()
    {
        DefaultListModel model = (DefaultListModel)itemList.getModel();
        JGoodiesDefItem item = new JGoodiesDefItem("p", isRow); //$NON-NLS-1$
        item.setInUse(false);
        items.add(item);
        model.addElement(item);
    }

    public void delItem()
    {
        if (!currentItem.isInUse())
        {
            DefaultListModel model = (DefaultListModel)itemList.getModel();
            items.remove(currentItem);
            model.removeElement(currentItem);
        }
    }

    /**
     * 
     */
    protected void itemSelected()
    {
        currentItem = (JGoodiesDefItem)itemList.getSelectedValue();
        if (currentItem != null)
        {
            propsPanel.getDataFromUI();
            
            controlPanel.getDelBtn().setEnabled(!currentItem.isInUse());
        }
        
        propsPanel.setCurrentItem(currentItem);
    }
    
    /**
     * @return
     */
    public String getDefStr()
    {
        StringBuilder sb = new StringBuilder();
        
        for (JGoodiesDefItem item : items)
        {
            if (sb.length() > 0) sb.append(","); //$NON-NLS-1$
            sb.append(item.toString());
        }
        
        return sb.toString();
    }
    
    /**
     * 
     */
    public void getDataFromUI()
    {
        propsPanel.getDataFromUI();
    }

}
