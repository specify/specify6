/* Filename:    $RCSfile: PickListProcessor.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.specify.tasks.subpane;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.ku.brc.specify.tasks.SystemSetupTask;
import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.CommandDispatcher;
import edu.ku.brc.specify.ui.db.PickList;
import edu.ku.brc.specify.ui.db.PickListItem;
import edu.ku.brc.specify.ui.forms.Viewable;

/**
 * The process for the pick list "editor" form
 *  
 * @author rods
 *
 */
public class PickListProcessor implements FormProcessor
{

    protected FormPane     formPane = null;
    protected Viewable Viewable;
    
    // UI
    protected JButton    addBtn     = null;
    protected JButton    removeBtn  = null;
    protected JList      list       = null;
    protected JTextField title      = null;
    protected JTextField value      = null;
    protected JTextField name       = null;
    
    protected List<String> titlesList = new ArrayList<String>();
    protected List<String> usedInList = new ArrayList<String>();
       
    protected PickList   pickList;
    
    /**
     * Default Constructor
     */
    public PickListProcessor(final List<String> titlesList)
    {
        this.titlesList = titlesList;
    }
    
    /**
     * Return the number of forms the pick list is used in
     * @return Return the number of forms the pick list is used in
     */
    public int getNumUsed()
    {
       return usedInList.size();
    }

    public boolean doEvaluate()
    {
        String nameStr = name.getText();
        return isNotEmpty(nameStr) && Collections.binarySearch(titlesList, nameStr) < 0;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.FormProcessor#setViewable(edu.ku.brc.specify.tasks.subpane.FormPane)
     */
    public void setViewable(final FormPane formPane)
    {
        this.formPane = formPane;
        
        if (formPane != null) 
        {
            Viewable = formPane.getViewable();
            pickList     = (PickList)Viewable.getDataObj();
            
            name  = (JTextField)Viewable.getComp("name");
            list  = (JList)Viewable.getComp("items");
            title = (JTextField)Viewable.getComp("title");
            value = (JTextField)Viewable.getComp("value");
            
            list.setModel(new AbstractListModel() {
                public int getSize() { return usedInList.size(); }
                public Object getElementAt(int index) { return usedInList.get(index); }
            });

            JButton saveBtn = (JButton) Viewable.getComp("savePL");
            Viewable.getValidator().registerOKButton(saveBtn);
            
            //Viewable.getValidator().addRuleObjectMapping("titleVal", new TitleValidator(name, titlesList));
            Viewable.getValidator().addRuleObjectMapping("processor", this);
    
            addBtn    = (JButton) Viewable.getComp("AddItem");
            removeBtn = (JButton) Viewable.getComp("RemoveItem");
            
            
            addBtn.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            PickListItem pli = new PickListItem(title.getText(), null, new Date());
                            pickList.getItems().add(pli);
                            title.setText("");
                            value.setText("");
                            
                            ListModel lm = list.getModel();
                            if (lm instanceof DefaultListModel)
                            {
                                ((DefaultListModel)lm).addElement(pli);
                            } else
                            {
                                throw new RuntimeException("Unknown model type for JList["+lm+"]");
                            }
                            list.setSelectedIndex(-1);
                            Viewable.getValidator().validateForm();
                        }
                    });
                  
            removeBtn.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            Object[] objs = list.getSelectedValues();
                            for (Object o : objs)
                            {
                                pickList.getItems().remove(o);
                                ListModel lm = list.getModel();
                                if (lm instanceof DefaultListModel)
                                {
                                    ((DefaultListModel)lm).removeElement(o);
                                } else
                                {
                                    throw new RuntimeException("Unknown model type for JList["+lm+"]");
                                }
                            }
                            list.setSelectedIndex(-1);
                            Viewable.getValidator().validateForm();
                        }
                    });
            
            JButton deleteBtn = (JButton) Viewable.getComp("deletePL");
            deleteBtn.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            CommandDispatcher.dispatch(new CommandAction(SystemSetupTask.SYSTEMSETUPTASK, "DeletePickList", Viewable.getDataObj()));
                            
                        }
                    });
            
            saveBtn.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            pickList.setName(((JTextField)Viewable.getComp("name")).getText());
                            
                            String sizeStr = ((JTextField)Viewable.getComp("sizeLimit")).getText().trim();
                            pickList.setSizeLimit(isNotEmpty(sizeStr) ? Integer.parseInt(sizeStr) : 0);
                            
                            pickList.setReadOnly(((JCheckBox)Viewable.getComp("readOnly")).isSelected());
                            
                            CommandDispatcher.dispatch(new CommandAction(SystemSetupTask.SYSTEMSETUPTASK, "SavePickList", Viewable.getDataObj()));
                            
                        }
                    });
            
            Viewable.getValidator().validateForm();
            
        } else
        {
            // do cleanup
            Viewable = null;
            pickList     = null;
        }
    }

    //-------------------------------------------------------------
    // Inner Classes
    //-------------------------------------------------------------
    /*class TitleValidator
    {
        public boolean doEvaluate()
        {
            String nameStr = name.getText();
            System.out.println("["+nameStr+"]["+(Collections.binarySearch(titlesList, nameStr) < 0)+"]");
            return isNotEmpty(nameStr) && Collections.binarySearch(titlesList, nameStr) < 0;
        }
    }*/

    
}
