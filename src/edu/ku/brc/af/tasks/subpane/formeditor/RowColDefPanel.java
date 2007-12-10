/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
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
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.tasks.subpane.formeditor.JGoodiesDefItem.ALIGN_TYPE;
import edu.ku.brc.af.tasks.subpane.formeditor.JGoodiesDefItem.MINMAX_TYPE;
import edu.ku.brc.ui.AddRemoveEditPanel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.forms.persist.FormViewDef;

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
    protected AddRemoveEditPanel controlPanel;
    
    
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
        for (String tok : StringUtils.split(item.getDefStr(), ","))
        {
            JGoodiesDefItem jgItem = new JGoodiesDefItem(tok, isRow);
            jgItem.setInUse(cnt < numInUse);
            items.add(jgItem);
            model.addElement(jgItem);
            cnt++;
        }
    }
    
    protected void createUI(final int     numInUse,
                            final boolean isRow)
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("max(125px;p):g,16px,p", "p,2px,p,2px,p:g"));

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
        
        controlPanel = new AddRemoveEditPanel(addAL, delAL, null);
        controlPanel.getAddBtn().setEnabled(true);
        
        itemList = new JList(new DefaultListModel());
        itemList.setCellRenderer(new DefItemRenderer(IconManager.IconSize.Std16));
        JScrollPane sp = new JScrollPane(itemList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pb.addSeparator((isRow ? "Row" : "Column") + " Items", cc.xy(1, 1));
        pb.add(sp, cc.xy(1,3));
        pb.add(controlPanel, cc.xy(1,5));
        
        pb.addSeparator("Properties", cc.xy(3, 1));
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
        JGoodiesDefItem item = new JGoodiesDefItem("p", isRow);
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
            if (sb.length() > 0) sb.append(",");
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
