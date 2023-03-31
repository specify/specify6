/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.tasks.subpane.wb;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * This specialized Dialog enables a user to selectbetween two radiobuttons. One that indicates a
 * new object should be created a second indicating they want to select from the list of existing items.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * May 2, 2007
 *
 */
public class SelectNewOrExistingDlg<T> extends CustomDialog
{
    protected JRadioButton      createNewRB;
    protected JRadioButton      useExistingRB;
    protected JList             list;
    protected Vector<T>         items;
    
    /**
     * @param frame the parent frame
     * @param titleKey the title 
     * @param createNewLabelKey the localized key for the radiobutton
     * @param useExistingLabelKey  the localized key for the radiobutton
     * @param helpContext the help context
     * @param list the list of items to be placed in the listbox
     * @throws HeadlessException
     */
    public SelectNewOrExistingDlg(final Frame  frame, 
                                  final String titleKey, 
                                  final String createNewLabelKey, 
                                  final String useExistingLabelKey, 
                                  final String helpContext, 
                                  final List<T> list) throws HeadlessException
    {
        super(frame, UIRegistry.getResourceString(titleKey), true, OKCANCELHELP, null);
        
        this.helpContext = helpContext;
        this.items = new Vector<T>(list);
        
        createNewRB   = new JRadioButton(UIRegistry.getResourceString(createNewLabelKey));
        useExistingRB  = new JRadioButton(UIRegistry.getResourceString(useExistingLabelKey));

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        ButtonGroup group = new ButtonGroup();
        group.add(createNewRB);
        group.add(useExistingRB);
        
        list = new JList(items);
        
        PanelBuilder panel = new PanelBuilder(new FormLayout("f:max(300px;p):g", "p,2px,p,2px,p"));
        CellConstraints cc = new CellConstraints();
        panel.add(createNewRB,   cc.xy(1,1));
        panel.add(useExistingRB,  cc.xy(1,3));
        panel.add(UIHelper.createScrollPane(list), cc.xy(1,5));
        
        createNewRB.setSelected(true);
        list.setEnabled(false);
        
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        contentPanel = panel.getPanel();
        
        super.createUI();

        pack();
        
        createNewRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                list.setEnabled(false);
                okBtn.setEnabled(true);
            }
        });
        
        useExistingRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                list.setEnabled(true);
                if (items.size() == 1)
                {
                    list.setSelectedIndex(0);
                }
                okBtn.setEnabled(list.getSelectedIndex() > -1);
            }
        });
        
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    okBtn.setEnabled(list.getSelectedIndex() > -1);
                }
            }
        });
        list.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                if (list.getSelectedIndex() > -1 && e.getClickCount() == 2)
                {
                    okBtn.doClick();
                }
            }
        });
    }
    
    /**
     * @return true indicates a new one should be created, false means use an existing one
     */
    public boolean isCreateNew()
    {
        return createNewRB.isSelected();
    }
    
    /**
     * Returns the selected Object or null if nothing was selected.
     * @return the selected Object or null if nothing was selected
     */
    public T getSelectedObject()
    {
        int inx = list.getSelectedIndex();
        if (inx != -1)
        { 
            return items.get(inx); 
        }
        return null;
    }
    
}
