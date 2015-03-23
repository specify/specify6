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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.ui.db.PickListTableAdapter;
import edu.ku.brc.ui.UIHelper;

/**
 * @author timo
 *
 * @code_status Alpha
 *
 *
 */
@SuppressWarnings("serial")
public class PickListCriteriaCombo extends JComboBox
{
    protected static final Logger log = Logger.getLogger(QueryFieldPanel.class);
    protected static final String NULL_CODE = "|null|";
    protected final PickListDBAdapterIFace items;
    protected List<PickListItemIFace> sels = new ArrayList<PickListItemIFace>();
    protected static final int SELMODE_SINGLE = 0;
    protected static final int SELMODE_DOUBLE = 1;
    protected static final int SELMODE_MULTI = 2;
   
    protected int selMode = SELMODE_MULTI;
    
    protected SpQueryField.OperatorType currentOp;
    
    public PickListCriteriaCombo(final PickListDBAdapterIFace items)
    {
        super();
        this.items = items;
        setupItems();
    }

    protected void setupItems()
    {
        setModel((ComboBoxModel)items);
        
        addActionListener(new ActionListener(){

            /* (non-Javadoc)
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            //@Override
            public void actionPerformed(ActionEvent e)
            {
                if (e.getActionCommand().equals("comboBoxChanged"))
                {
                    PickListItemIFace theSelection = (PickListItemIFace)getSelectedItem();
                    setSelectedItem(null);
                    if (selMode == SELMODE_SINGLE)
                    {
                        boolean addIt = !sels.contains(theSelection);
                        sels.clear();
                        if (addIt)
                        {
                            sels.add(theSelection);
                        }
                    }
                    else if (selMode == SELMODE_MULTI)
                    {
                        if (sels.contains(theSelection))
                        {
                            sels.remove(theSelection);
                        }
                        else
                        {
                            sels.add(theSelection);
                        }
                    }
                    else if (selMode == SELMODE_DOUBLE)
                    {
                        if (sels.size() == 2)
                        {
                            sels.clear();
                        }
                        sels.add(theSelection);
                    }
                }
            }
            
        });
    }
    
    /**
     * @author timbo
     *
     * @code_status Alpha
     *
     * Renderer that can display multiple selections in ComboBox text area.
     */
    protected class PickListCellRenderer extends DefaultListCellRenderer
    {
        /* (non-Javadoc)
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            if (index == -1) //currently, this should always be the case 
            {
                return UIHelper.createLabel(getSelectionsText());
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
    
    
    /* (non-Javadoc)
     * @see javax.swing.JComboBox#getRenderer()
     */
    @Override
    public ListCellRenderer getRenderer()
    {
        return new PickListCellRenderer();
    }

    /**
     * @param getValues - if true then show item values, else use item titles.
     * @return a textual representation (comma-delimited list) of the selected items.
     */
    public String getText(final boolean getValues)
    {
        if (selMode == SELMODE_MULTI)
        {
            Collections.sort(sels); //alphabetically, for now
        }
        StringBuilder sb = new StringBuilder();
        for (PickListItemIFace sel : sels)
        {
            String s;
			if (getValues) 
			{
				if (items instanceof PickListTableAdapter) 
				{
					Object selObj = sel.getValueObject();
					if (selObj != null) 
					{
						if (selObj instanceof DataModelObjBase) 
						{
							DataModelObjBase dataObj = (DataModelObjBase) sel
									.getValueObject();
							Integer id = dataObj.getId();
							s = id != null ? id.toString() : "";

						} else if (selObj instanceof String) 
						{
							s = "'" + selObj.toString() + "'";
						} else 
						{
							s = selObj.toString();
						}
					} else 
					{
						s = "";
					}
				} else 
				{
					s = sel.getValue();
				}
			} else 
			{
				s = sel.getTitle();
			}

            if (s != null)
            {
            	if (sb.length() != 0)
            	{	
            		sb.append(", ");
            	}
            	sb.append(s);
            }
        }
        return sb.toString();
    }
    
    
    /**
     * @return true is an item whose value is null is picked
     */
    public boolean nullItemIsPicked() 
    {
    	for (PickListItemIFace sel : sels) 
    	{
            if (items instanceof PickListTableAdapter)
            {
                if (sel.getValueObject() == null || NULL_CODE.equalsIgnoreCase(sel.getValueObject().toString()))
                {
                	return true;
                }
            } else if (sel.getValue() == null || NULL_CODE.equalsIgnoreCase(sel.getValue().toString()))
            {
            	return true;
            }
    	}
    	return false;
    }
    
    /**
     * @return selected item titles as text.
     */
    public String getSelectionsText()
    {
        return getText(false);
    }

    /**
     * @return selected item values as text.
     */
    public String getCriteria()
    {
        return getText(true);
    }

    /**
     * @param itemText
     * @return pickList item with title = itemText.
     */
    protected PickListItemIFace getItemForText(final String itemText)
    {
        for (PickListItemIFace item : items.getList())
        {
            if (item.getTitle().equals(itemText))
                return item;
        }
        return null;
    }
    
    /**
     * @param selections - a list of selections in format produced by getSelectionsText();
     */
    public void setSelections(final String selections)
    {
        sels.clear();
        String[] selsToSet = selections.split(", ");
        for (String selToSet : selsToSet)
        {
            PickListItemIFace match = getItemForText(selToSet);
            if (match != null)
            {
                sels.add(match);
            }
            else
            {
                log.info("no picklist item found for '" + selToSet + "'");
            }
        }
    }
        
    /**
     * @param args
     * 
     * For testing. Totally broken at this point.
     */
//    public static void main(final String[] args)
//    {
//        PickListDBAdapterIFace pick = null;
//        String fldName = null;
//        for (DBTableInfo tbl : DBTableIdMgr.getInstance().getTables())
//        {
//            System.out.println(tbl.getName());
//            for (DBFieldInfo fld : tbl.getFields())
//            {
//                System.out.println("   " + fld.getName());
//                    pick = PickListDBAdapterFactory.getInstance().create(fld.getName(), false);
//                    if (pick != null)
//                    {
//                        fldName = tbl.getName() + "." + fld.getName();
//                        break;
//                    }
//            }
//            if (pick != null)
//            {
//                break;
//            }
//        }
//        if (pick == null)
//        {
//            System.out.println("no picklist in db");
//            System.exit(1);
//        }
//        System.out.println(fldName);
//        final PickListCriteriaCombo plc = new PickListCriteriaCombo(pick);
//        JPanel plcPain = new JPanel(new BorderLayout());
//        plcPain.add(plc, BorderLayout.CENTER);
//        JPanel pain = new JPanel(new BorderLayout());
//        pain.add(plcPain, BorderLayout.CENTER);
//        
//        DefaultListModel ops = new DefaultListModel();
//        ops.addElement(SpQueryField.OperatorType.BETWEEN);
//        //ops.addElement(SpQueryField.OperatorType.CONTAINS);
//        ops.addElement(SpQueryField.OperatorType.EQUALS);
//        //ops.addElement(SpQueryField.OperatorType.GREATERTHAN);
//        //ops.addElement(SpQueryField.OperatorType.GREATERTHANEQUALS);
//        ops.addElement(SpQueryField.OperatorType.IN);
//        //ops.addElement(SpQueryField.OperatorType.LESSTHAN);
//        //ops.addElement(SpQueryField.OperatorType.LESSTHANEQUALS);
//        //ops.addElement(SpQueryField.OperatorType.LIKE);
//        
//        final JList opsList = new JList(ops);
//        opsList.addListSelectionListener(new ListSelectionListener(){
//
//            /* (non-Javadoc)
//             * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
//             */
//            @Override
//            public void valueChanged(ListSelectionEvent e)
//            {
//                System.out.println("setting curront op");
//                plc.setCurrentOp((SpQueryField.OperatorType)opsList.getSelectedValue());
//            }
//            
//        });
//        pain.add(opsList, BorderLayout.WEST);
//        
//        JFrame frm = new JFrame();
//        frm.setPreferredSize(new Dimension(600,400));
//        frm.setContentPane(pain);
//        frm.pack();
//        frm.setVisible(true);
//    }

    /**
     * @param op
     * @return the SELMODE appropriate for op.
     */
    protected int getModeForOp(final SpQueryField.OperatorType op)
    {
        if (op.getOrdinal() == SpQueryField.OperatorType.IN.getOrdinal())
            return SELMODE_MULTI;
        if (op.getOrdinal() == SpQueryField.OperatorType.BETWEEN.getOrdinal())
            return SELMODE_DOUBLE;
        // else
        return SELMODE_SINGLE;
    }
    
    /**
     * @param mode - the selMode to set.
     */
    protected void setSelMode(final int mode)
    {
        setSelectedItem(null);
        sels.clear();
        selMode = mode;
        repaint();
    }
    
    /**
     * @return the currentOp
     */
    public SpQueryField.OperatorType getCurrentOp()
    {
        return currentOp;
    }

    /**
     * @param newOp the currentOp to set
     */
    public void setCurrentOp(SpQueryField.OperatorType newOp)
    {
        if (getModeForOp(newOp) != selMode)
        {
            setSelMode(getModeForOp(newOp));
        }
        this.currentOp = newOp;
    }
    
}
