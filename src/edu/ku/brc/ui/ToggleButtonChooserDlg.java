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
package edu.ku.brc.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.List;

import javax.swing.BorderFactory;

/**
 * Choose an object from a list of Objects using their "toString"
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ToggleButtonChooserDlg<T> extends CustomDialog
{
    // Needed for Delayed Creation
    protected ToggleButtonChooserPanel<T> panel;
    

    /**
     * Constructor.
     * @param parentFrame the parent Frame
     * @param titleKey dialog title
     * @param desc description label above list (optional)
     * @param items the list to be selected from
     * @throws HeadlessException
     */
    public ToggleButtonChooserDlg(final Frame   parentFrame, 
                                  final String  titleKey, 
                                  final List<T> listItems) throws HeadlessException
    {
        this(parentFrame, titleKey, null, listItems);
    }

    /**
     * Constructor.
     * @param parentFrame the parent Frame
     * @param titleKey dialog title
     * @param desc description label above list (optional)
     * @param items the list to be selected from
     * @throws HeadlessException
     */
    public ToggleButtonChooserDlg(final Frame   parentFrame, 
                                  final String  titleKey, 
                                  final List<T> listItems,
                                  final ToggleButtonChooserPanel.Type uiType) throws HeadlessException
    {
        this(parentFrame, titleKey, null, listItems, OKCANCEL, uiType);
    }

    /**
     * Constructor.
     * @param parentFrame the parent Frame
     * @param titleKey dialog title
     * @param desckey description label above list (optional)
     * @param items the list to be selected from
     * @throws HeadlessException
     */
    public ToggleButtonChooserDlg(final Frame   parentFrame, 
                                  final String  titleKey,
                                  final String  desckey, 
                                  final List<T> listItems) throws HeadlessException
    {
        this(parentFrame, titleKey, desckey, listItems, OKCANCEL, ToggleButtonChooserPanel.Type.Checkbox);
    }

    /**
     * Constructor.
     * @param parentFrame the parent Frame
     * @param key dialog title
     * @param descKey description label above list (optional)
     * @param items the list to be selected from
     * @param icon the icon to be displayed in front of each entry in the list
     * @throws HeadlessException
     */
    public ToggleButtonChooserDlg(final Frame     parentFrame, 
                                  final String    key, 
                                  final String    descKey, 
                                  final List<T>   listItems, 
                                  final int       whichButtons,
                                  final ToggleButtonChooserPanel.Type uiType) throws HeadlessException
    {
        super(parentFrame, UIRegistry.getResourceString(key), true, whichButtons, null);
        
        panel = new ToggleButtonChooserPanel<T>(listItems, UIRegistry.getResourceString(descKey), uiType);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Constructor.
     * @param parentDlg the parent Dialog
     * @param titleKey dialog title
     * @param desc description label above list (optional)
     * @param items the list to be selected from
     * @throws HeadlessException
     */
    public ToggleButtonChooserDlg(final Dialog  parentDlg, 
                                  final String  titleKey, 
                                  final List<T> listItems) throws HeadlessException
    {
        this(parentDlg, titleKey, null, listItems);
    }

    /**
     * Constructor.
     * @param parentDlg the parent Dialog
     * @param titleKey dialog title
     * @param desc description label above list (optional)
     * @param items the list to be selected from
     * @throws HeadlessException
     */
    public ToggleButtonChooserDlg(final Dialog  parentDlg, 
                                  final String  titleKey, 
                                  final List<T> listItems,
                                  final ToggleButtonChooserPanel.Type uiType) throws HeadlessException
    {
        this(parentDlg, titleKey, null, listItems, OKCANCEL, uiType);
    }

    /**
     * Constructor.
     * @param parentDlg the parent Dialog
     * @param titleKey dialog title
     * @param desckey description label above list (optional)
     * @param items the list to be selected from
     * @throws HeadlessException
     */
    public ToggleButtonChooserDlg(final Dialog  parentDlg, 
                                  final String  titleKey,
                                  final String  desckey, 
                                  final List<T> listItems) throws HeadlessException
    {
        this(parentDlg, titleKey, desckey, listItems, OKCANCEL, ToggleButtonChooserPanel.Type.Checkbox);
    }

    /**
     * Constructor.
     * @param parentDlg the parent Dialog
     * @param key dialog title
     * @param descKey description label above list (optional)
     * @param items the list to be selected from
     * @param icon the icon to be displayed in front of each entry in the list
     * @throws HeadlessException
     */
    public ToggleButtonChooserDlg(final Dialog    parentDlg, 
                                  final String    key, 
                                  final String    descKey, 
                                  final List<T>   listItems, 
                                  final int       whichButtons,
                                  final ToggleButtonChooserPanel.Type uiType) throws HeadlessException
    {
        super(parentDlg, UIRegistry.getResourceString(key), true, whichButtons, null);
        
        panel = new ToggleButtonChooserPanel<T>(listItems, UIRegistry.getResourceString(descKey), uiType);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        panel.setOkBtn(okBtn);
        panel.createUI();
        
        mainPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 6, 14));
        mainPanel.add(panel.getUIComponent(), BorderLayout.CENTER);
        
        pack();
        
        okBtn.setEnabled(false);
        
    }

    /**
     * @param addSelectAll
     */
    public void setAddSelectAll(boolean addSelectAll)
    {
        panel.setAddSelectAll(addSelectAll);
    }

    /**
     * @param suppressScrollPane the suppressScrollPane to set
     */
    public void setUseScrollPane(boolean useScrollPane)
    {
        panel.setUseScrollPane(useScrollPane);
    }

    /**
     * Returns the selected Object or null if nothing was selected.
     * @return the selected Object or null if nothing was selected
     */
    public T getSelectedObject()
    {
        return panel.getSelectedObject();
    }
    
    /**
     * Sets a single button selected.
     * @param index tyhe index of the button to be selected
     */
    public void setSelectedIndex(final int index)
    {
        panel.setSelectedIndex(index);
    }
    
    /**
     * Returns the index of the first selected item or -1.
     * @return the index
     */
    public int getSelectedIndex()
    {
        return panel.getSelectedIndex();
    }

    /**
     * Sets the object in the items list to be selected
     * @param selectedItems the list of items to be selected
     */
    public void setSelectedObjects(final List<T> selectedItems)
    {
        panel.setSelectedObjects(selectedItems);
    }

    /**
     * Returns the selected Object or null if nothing was selected.
     * @return the selected Object or null if nothing was selected
     */
    public List<T> getSelectedObjects()
    {
        return panel.getSelectedObjects();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#isCancelled()
     */
    @Override
    public boolean isCancelled()
    {
        return isCancelled;
    }

    /**
     * @return the panel
     */
    public ToggleButtonChooserPanel<T> getPanel()
    {
        return panel;
    }
}
