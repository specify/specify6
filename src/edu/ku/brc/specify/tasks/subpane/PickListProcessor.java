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
package edu.ku.brc.specify.tasks.subpane;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.tasks.subpane.FormProcessor;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.specify.tasks.SystemSetupTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;

/**
 * The process for the pick list "editor" form
 
 * @code_status Alpha
 **
 * @author rods
 *
 */
public class PickListProcessor implements FormProcessor
{

    protected FormPane     formPane = null;
    protected Viewable     viewable;

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
            viewable = formPane.getViewable();
            pickList     = (PickList)viewable.getDataObj();

            name  = viewable.getCompById("name");
            list  = viewable.getCompById("items");
            title = viewable.getCompById("title");
            value = viewable.getCompById("value");

            list.setModel(new AbstractListModel() {
                public int getSize() { return usedInList.size(); }
                public Object getElementAt(int index) { return usedInList.get(index); }
            });

            JButton saveBtn = viewable.getCompById("savePL");
            viewable.getValidator().addEnableItem(saveBtn, FormValidator.EnableType.ValidAndChangedItems);

            //Viewable.getValidator().addRuleObjectMapping("titleVal", new TitleValidator(name, titlesList));
            viewable.getValidator().addRuleObjectMapping("processor", this);

            addBtn    = viewable.getCompById("AddItem");
            removeBtn = viewable.getCompById("RemoveItem");


            addBtn.addActionListener(new ActionListener()
                    {
                        @SuppressWarnings("unchecked")
                        public void actionPerformed(ActionEvent ae)
                        {
                            PickListItem pli = new PickListItem(title.getText(), null, new Timestamp(System.currentTimeMillis()));
                            pickList.addItem(pli);
                            pli.setPickList(pickList);
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
                            viewable.getValidator().validateForm();
                        }
                    });

            removeBtn.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            Object[] objs = list.getSelectedValues();
                            for (Object o : objs)
                            {
                                pickList.removeItem((PickListItemIFace)o);
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
                            viewable.getValidator().validateForm();
                        }
                    });

            JButton deleteBtn = viewable.getCompById("deletePL");
            deleteBtn.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            CommandDispatcher.dispatch(new CommandAction(SystemSetupTask.SYSTEMSETUPTASK, "DeletePickList", viewable.getDataObj()));

                        }
                    });

            saveBtn.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            pickList.setName(((JTextField)viewable.getCompById("name")).getText());

                            String sizeStr = ((JTextField)viewable.getCompById("sizeLimit")).getText().trim();
                            pickList.setSizeLimit(isNotEmpty(sizeStr) ? Integer.parseInt(sizeStr) : 0);

                            pickList.setReadOnly(((JCheckBox)viewable.getCompById("readOnly")).isSelected());

                            CommandDispatcher.dispatch(new CommandAction(SystemSetupTask.SYSTEMSETUPTASK, "SavePickList", viewable.getDataObj()));

                        }
                    });

            viewable.getValidator().validateForm();

        } else
        {
            // do cleanup
            viewable = null;
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
