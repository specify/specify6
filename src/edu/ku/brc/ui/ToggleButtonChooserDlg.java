/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.List;

import javax.swing.ImageIcon;

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
    protected String                      title   = null;
    protected ToggleButtonChooserPanel<T> panel;
    

    /**
     * Constructor.
     * @param parentFrame the parent Frame
     * @param title dialog title
     * @param desc description label above list (optional)
     * @param items the list to be selected from
     * @throws HeadlessException
     */
    public ToggleButtonChooserDlg(final Frame   parentFrame, 
                                  final String  title, 
                                  final List<T> listItems) throws HeadlessException
    {
        this(parentFrame, title, null, listItems);
    }

    /**
     * Constructor.
     * @param parentFrame the parent Frame
     * @param title dialog title
     * @param desc description label above list (optional)
     * @param items the list to be selected from
     * @throws HeadlessException
     */
    public ToggleButtonChooserDlg(final Frame   parentFrame, 
                                  final String  title, 
                                  final List<T> listItems,
                                  final ToggleButtonChooserPanel.Type uiType) throws HeadlessException
    {
        this(parentFrame, title, null, listItems, null, OKCANCEL, uiType);
    }

    /**
     * Constructor.
     * @param title dialog title
     * @param desc description label above list (optional)
     * @param items the list to be selected from
     * @throws HeadlessException
     */
    public ToggleButtonChooserDlg(final Frame   parentFrame, 
                              final String  title,
                              final String  desc, 
                              final List<T> listItems) throws HeadlessException
    {
        this(parentFrame, title, desc, listItems, null, OKCANCEL, ToggleButtonChooserPanel.Type.Checkbox);
    }

    /**
     * Constructor.
     * @param parentFrame the parent Frame
     * @param title dialog title
     * @param desc description label above list (optional)
     * @param items the list to be selected from
     * @param icon the icon to be displayed in front of each entry in the list
     * @throws HeadlessException
     */
    public ToggleButtonChooserDlg(final Frame     parentFrame, 
                                  final String    title, 
                                  final String    desc, 
                                  final List<T>   listItems, 
                                  final ImageIcon icon, 
                                  final int       whichButtons,
                                  final ToggleButtonChooserPanel.Type uiType) throws HeadlessException
    {
        super(parentFrame, getResourceString(title), true, whichButtons, null);
        
        this.icon   = icon;
        this.title  = title;
        
        panel = new ToggleButtonChooserPanel<T>(listItems, desc, uiType);

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
        
        mainPanel.add(panel.getUIComponent(), BorderLayout.CENTER);
        
        pack();
        
        okBtn.setEnabled(false);
        
    }

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
}
