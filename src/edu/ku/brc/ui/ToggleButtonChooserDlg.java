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

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Choose an object from a list of Objects using their "toString"
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ToggleButtonChooserDlg<T> extends JDialog implements ChangeListener, ActionListener
{
    public enum Type {Checkbox, RadioButton}
    
    // Data Members
    protected JButton               cancelBtn;
    protected JButton               okBtn       =  null;
    protected List<T>               items;
    protected Vector<JToggleButton> buttons     = new Vector<JToggleButton>(10);
    protected ImageIcon             icon        = null;
    protected boolean               isCancelled = false;
    
    protected ButtonGroup           group       = null;
    
    // Needed for Delayed Creation
    protected String                title       = null;
    protected String                desc        = null;
    protected Type                  uiType      = null;
    protected int                   initialSelectedIndex = -1;
    
    // This means it should build it as a vertical list with no scrollpane
    protected boolean               useScrollPane = false;

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
        this(parentFrame, title, desc, listItems, null, Type.Checkbox);
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
                              final Type      uiType) throws HeadlessException
    {
        super(parentFrame, true);
        
        this.items  = listItems;
        this.icon   = icon;
        this.title  = title;
        this.desc   = desc;
        this.uiType = uiType;

        //setLocationRelativeTo(UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    /**
     * Create the UI for the dialog.
     * @param title dialog title
     * @param desc description label above list (optional)
     */
    protected void createUI()
    {
        setTitle(getResourceString(title));
        
        int rows = desc != null ? 3 : 2;
        int y    = 1;
        CellConstraints cc         = new CellConstraints();
        PanelBuilder    panelBlder = new PanelBuilder(new FormLayout("f:p:g", UIHelper.createDuplicateJGoodiesDef("p", "2px", rows)));
        JPanel          panel      = panelBlder.getPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(4,4,4,2));
        if (desc != null)
        {
            JLabel lbl = new JLabel(getResourceString(desc), SwingConstants.CENTER);
            panelBlder.add(lbl, cc.xy(1, y)); y += 2;
        }
        
        PanelBuilder listBldr  = new PanelBuilder(new FormLayout("f:p:g", UIHelper.createDuplicateJGoodiesDef("p", "2px", items.size())));
        JPanel       listPanel = listBldr.getPanel();
        listPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        if (useScrollPane)
        {
            listPanel.setBackground(Color.WHITE);
            listPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), listPanel.getBorder()));
        }
        
        group = uiType == Type.Checkbox ? null : new ButtonGroup();

        int yy = 1;
        for (Object obj : items)
        {
            JToggleButton togBtn;
            if (uiType == Type.Checkbox)
            {
                togBtn = new JCheckBox(obj.toString());
            } else
            {
                togBtn = new JRadioButton(obj.toString());
                group.add(togBtn);
            }
            
            togBtn.setOpaque(false);
            togBtn.addChangeListener(this);
            buttons.add(togBtn);
            listPanel.add(togBtn, cc.xy(1, yy));
            yy += 2;
        }
        Dimension size = listPanel.getPreferredSize();
        listPanel.setSize(size);
        listPanel.setPreferredSize(size);

        if (buttons.size() > 0 && useScrollPane)
        {
            JToggleButton togBtn = buttons.get(0);
            Dimension     dim    = getPreferredSize();
            dim.height = togBtn.getPreferredSize().height * 10;
            listPanel.setPreferredSize(dim);
        }

        if (useScrollPane)
        {
            JScrollPane listScroller = new JScrollPane(listPanel);
            Dimension   psize        = listScroller.getViewport().getPreferredSize();
            psize.width = size.width + 15; // add 15 for scrollbar
            listScroller.getViewport().setSize(psize);
            listScroller.getViewport().setPreferredSize(psize);
            panelBlder.add(listScroller, cc.xy(1, y)); y += 2;
            
        } else
        {
            panelBlder.add(listPanel, cc.xy(1, y)); y += 2;
        }

        // Bottom Button UI
        cancelBtn         = new JButton(getResourceString("Cancel"));
        okBtn             = new JButton(getResourceString("OK"));

        okBtn.addActionListener(this);
        getRootPane().setDefaultButton(okBtn);

        JPanel btnBar = ButtonBarFactory.buildOKCancelBar(okBtn, cancelBtn);
        btnBar.setBorder(BorderFactory.createEmptyBorder(2,0,0,0));
        
        cancelBtn.addActionListener(new ActionListener()
                    {  public void actionPerformed(ActionEvent ae) { setVisible(false); isCancelled = true;} });

        panelBlder.add(btnBar, cc.xy(1, y)); y += 2;

        setContentPane(panel);
        pack();
        
        if (initialSelectedIndex != -1)
        {
            setSelectedIndex(initialSelectedIndex);
        }

    }

    /* (non-Javadoc)
     * @see java.awt.Component#setVisible(boolean)
     */
    @Override
    public void setVisible(final boolean visible)
    {
        if (okBtn == null && visible)
        {
            createUI();
        }
        UIHelper.centerWindow(this);
        super.setVisible(visible);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        setVisible(false);
    }

    /**
     * @param suppressScrollPane the suppressScrollPane to set
     */
    public void setUseScrollPane(boolean useScrollPane)
    {
        this.useScrollPane = useScrollPane;
    }

    /**
     * Returns the selected Object or null if nothing was selected.
     * @return the selected Object or null if nothing was selected
     */
    public T getSelectedObject()
    {
        int inx = 0;
        for (JToggleButton tb : buttons)
        {
            if (tb.isSelected())
            {
                return items.get(inx);
            }
            inx++;
        }
        return null;
    }
    
    /**
     * Sets a single button selected.
     * @param index tyhe index of the button to be selected
     */
    public void setSelectedIndex(final int index)
    {
        if (okBtn != null)
        {
            if (index > -1 && index < buttons.size())
            {
                buttons.get(index).setSelected(true);
            }
        } else
        {
            initialSelectedIndex = index;
        }
    }
    
    /**
     * Returns the index of the first selected item or -1.
     * @return the index
     */
    public int getSelectedIndex()
    {
        int inx = 0;
        for (JToggleButton tb : buttons)
        {
            if (tb.isSelected())
            {
                return inx;
            }
            inx++;
        } 
        return -1;
    }

    /**
     * Sets the object in the items list to be selected
     * @param selectedItems the list of items to be selected
     */
    public void setSelectedObjects(final List<T> selectedItems)
    {
        for (T obj : selectedItems)
        {
            int inx = 0;
            for (JToggleButton tb : buttons)
            {
                if (obj == items.get(inx))
                {
                    tb.setSelected(true);
                    break;
                }
                inx++;
            }
        }
    }

    /**
     * Returns the selected Object or null if nothing was selected.
     * @return the selected Object or null if nothing was selected
     */
    public List<T> getSelectedObjects()
    {
        List<T> list = new ArrayList<T>(5);
        int inx = 0;
        for (JToggleButton tb : buttons)
        {
            if (tb.isSelected())
            {
                list.add(items.get(inx));
            }
            inx++;
        }
        return list;
    }

    /**
     * Returns whether it was cancelled.
     * @return whether it was cancelled
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }

    //----------------------------------------------------------------
    //-- ChangeListener Interface
    //----------------------------------------------------------------

    public void stateChanged(ChangeEvent e)
    {
        // do nothing
    }


}
