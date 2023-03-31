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
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createRadioButton;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.NavBoxLayoutManager;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 6, 2007
 *
 */
public class ToggleButtonChooserPanel<T> extends JPanel implements ActionListener
{
    public enum Type {Checkbox, RadioButton}
    
    // Data Members
    protected List<T>               items                = new Vector<T>();
    protected Vector<JToggleButton> buttons              = new Vector<JToggleButton>();
    protected Vector<JToggleButton> unusedButtons        = new Vector<JToggleButton>();
    protected JButton               selectAllBtn;
    protected JButton               deselectAllBtn;
    protected ButtonGroup           group                = null;
    protected JScrollPane           listScroller         = null;
    protected JPanel                listPanel            = null;
    
    // Needed for Delayed Creation
    protected Type                  uiType               = null;
    protected String                desc                 = null;
    protected int                   initialSelectedIndex = -1;
    protected boolean               addSelectAll         = false;
    protected List<T>               selectedItems        = null;
    protected boolean               isBuilt              = false;
    protected ChangeListener        changeListener       = null;
    protected ActionListener        actionListener       = null;
    
    // This means it should build it as a vertical list with no scrollpane
    protected boolean               useScrollPane        = false;
    
    protected JButton               okBtn                = null;
    protected int                   staticSize           = -1;
    protected boolean               wasCreated           = false;
    protected boolean               hasInitialSelection  = false;
    
    /**
     * Constructor.
     * @param spItems the list to be selected from
     * @param uiType the type toggle buttons
     */
    public ToggleButtonChooserPanel(final int  staticSize, 
                                    final Type uiType)
    {
        this(null, null, uiType);
        
        this.staticSize = staticSize;
    }

    /**
     * Constructor.
     * @param spItems the list to be selected from
     * @param uiType the type toggle buttons
     */
    public ToggleButtonChooserPanel(final List<T>   listItems, 
                                    final Type      uiType)
    {
        this(listItems, null, uiType);
    }

    /**
     * Constructor.
     * @param spItems the list to be selected from
     * @param desc description label above list (optional)
     * @param uiType the type toggle buttons
     */
    public ToggleButtonChooserPanel(final List<T>   listItems, 
                                    final String    desc, 
                                    final Type      uiType)
    {
        if (listItems != null)
        {
            this.items.addAll(listItems);
        }
        
        this.desc   = desc;
        this.uiType = uiType;
        
        setDoubleBuffered(true);
    }

    protected JToggleButton createBtn(final String label) {
        JToggleButton togBtn;
        if (uiType == Type.Checkbox) {
            togBtn = createCheckBox(label);
        } else {
            togBtn = createRadioButton(label);
            group.add(togBtn);
        }

        if (changeListener != null) {
            togBtn.addChangeListener(changeListener);
        }

        if (actionListener != null) {
            togBtn.addActionListener(actionListener);
        }

        togBtn.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (((JToggleButton) e.getSource()).isSelected() && okBtn != null) {
                    okBtn.setEnabled(true);
                }
            }
        });
        togBtn.setOpaque(false);
        return togBtn;
    }
    
    /**
     * Creates the UI. 
     */
    public void createUI()
    {
        removeAll();
        buttons.clear();
        
        isBuilt = true;
        
        StringBuffer rowDef = new StringBuffer();
        if (desc != null)
        {
            rowDef.append("p,2px,");
        }
        
        rowDef.append("f:p:g");
        
        if (addSelectAll && uiType == Type.Checkbox)
        {
            rowDef.append(",2px,p");
        }
        rowDef.append(",10px");
        
        int y    = 1;
        CellConstraints cc         = new CellConstraints();
        PanelBuilder    panelBlder = new PanelBuilder(new FormLayout("f:p:g", rowDef.toString()), this);
        JPanel          panel      = panelBlder.getPanel();
        panel.setDoubleBuffered(true);
        // Please document when this is needed, it might need to be a configuration thing. - rods
        //panel.setBorder(BorderFactory.createEmptyBorder(4,4,4,2));
        
        if (desc != null)
        {
            JLabel lbl = createLabel(getResourceString(desc), SwingConstants.CENTER);
            panelBlder.add(lbl, cc.xy(1, y)); y += 2;
        }

        boolean isStaticSize = staticSize > -1;
        int len = isStaticSize ? staticSize : items.size();
        
        listPanel = new JPanel(new NavBoxLayoutManager(2,2));
        listPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        listPanel.setDoubleBuffered(true);
        
        if (useScrollPane)
        {
            listPanel.setBackground(Color.WHITE);
            // Please document when this is needed, it might need to be a configuration thing. - rods
            //listPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), listPanel.getBorder()));
        }
        
        group = uiType == Type.Checkbox ? null : new ButtonGroup();

        int yy  = 1;
        for (int i=0;i<len;i++)
        {
            T      obj   = isStaticSize ? null : items.get(i); 
            String label = isStaticSize ? "XXXXXXXXXXXXXXXXXXXXXXXXXX" : obj.toString();
            
            JToggleButton togBtn = createBtn(label);
            
            buttons.add(togBtn);
            listPanel.add(togBtn);
            yy += 2;
        }
        Dimension size = listPanel.getPreferredSize();
        listPanel.setSize(size);

        // if we are using a JScrollPane, create it and put the listPanel inside it
        // then add it to the panelBuilder
        if (useScrollPane)
        {
            if (listScroller == null)
            {
                listScroller = new JScrollPane(listPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                // Please document when this is needed, it might need to be a configuration thing. - rods
                //listScroller.setBorder(BorderFactory.createEmptyBorder());
                listScroller.setDoubleBuffered(true);
            }
            
            // if there is at least 1 button, size the scrollpane to be the height of 10 buttons
            if (buttons.size() > 0)
            {
                // get the size of a button
                Dimension btnPrefSize     = buttons.get(0).getPreferredSize();
                Dimension scollerPrefSize = listScroller.getPreferredSize();
                
                // set the scrollpane to have a pref height the same as the height of 11 buttons
                // this will actually result in the scrollpane being able to show about 10 buttons, due
                // to spacing between the buttons
                scollerPrefSize.height = btnPrefSize.height * 11;
                //listScroller.setPreferredSize(scollerPrefSize);
                listScroller.getViewport().setPreferredSize(scollerPrefSize);
             }
            
            panelBlder.add(listScroller, cc.xy(1, y)); y += 2;
            
        } else
        {
            panelBlder.add(listPanel, cc.xy(1, y)); y += 2;
        }
        
        panel.setDoubleBuffered(true);
        
        if (addSelectAll && uiType == Type.Checkbox)
        {
            selectAllBtn   = createButton(getResourceString("SELECTALL"));
            deselectAllBtn = createButton(getResourceString("DeselectAll"));

            selectAllBtn.addActionListener(this);
            deselectAllBtn.addActionListener(this);

            PanelBuilder bb = new PanelBuilder(new FormLayout("f:p:g,p,10px,p,f:p:g", "p"));
            bb.add(selectAllBtn, cc.xy(2, 1));
            bb.add(deselectAllBtn, cc.xy(4, 1));
            panelBlder.add(bb.getPanel(), cc.xy(1, y)); y += 2;
        }
        
        if (initialSelectedIndex != -1)
        {
            setSelectedIndex(initialSelectedIndex);
        }
        
        if (isStaticSize)
        {
            setItems(null);
        }
        
        wasCreated = true;
        
        setSelectedObjects(selectedItems);
    }
    
    /**
     * @return
     */
    public JComponent getUIComponent()
    {
        return this;//listScroller != null ? listScroller : this;
    }

    /**
     * @param btn
     * @return
     */
    public T getItemForBtn(final JToggleButton btn)
    {
        int inx = buttons.indexOf(btn);
        if (inx > -1 && inx < items.size())
        {
            return items.get(inx);
        }
        System.err.println("Mismatch between btn slected and items list!");
        return null;
    }
    
    /**
     * @return the list of buttons
     */
    public Vector<JToggleButton> getButtons()
    {
        return buttons;
    }

    /**
     * @param itemsArg the items to set
     */
    public void setItems(final List<T> itemsArg)
    {
        if (group != null)
        {
            group.clearSelection();        // Java 6
        }
        
        this.items.clear();
        listPanel.removeAll();
        
        unusedButtons.addAll(buttons);
        buttons.clear();
        
        if (itemsArg != null)
        {
            this.items.addAll(itemsArg);
            
            for (int i=0;i<itemsArg.size();i++)
            {
                JToggleButton tb;
                if (unusedButtons.size() > 0)
                {
                    tb = unusedButtons.get(0);
                    unusedButtons.remove(0);
                } else
                {
                    tb = createBtn(" ");
                }
                buttons.add(tb);
                tb.setText(itemsArg.get(i).toString());
                tb.setSelected(false);
                tb.setEnabled(true);
                listPanel.add(tb);
            }
        }
        Dimension size = listPanel.getPreferredSize();
        listPanel.setSize(size);
        
        //listPanel.validate();
        //listPanel.invalidate();
        //listPanel.repaint();
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        
        for (int i=0;i<items.size();i++)
        {
            JToggleButton tb = buttons.elementAt(i);
            tb.setEnabled(enabled);
        }
    }

    /**
     * @return the items thwere used to create the buttons.
     */
    public List<T> getItems()
    {
        return items;
    }

    /**
     * @param addSelectAll
     */
    public void setAddSelectAll(boolean addSelectAll)
    {
        this.addSelectAll = addSelectAll;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        boolean doSelect = e.getSource() == selectAllBtn;
        
        if (!doSelect && hasInitialSelection)
        {
            okBtn.setEnabled(true);
        }
        
        for (JToggleButton tb : buttons)
        {
            if (tb.isEnabled())
            {
                tb.setSelected(doSelect);
            }
        }
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
        if (wasCreated)
        {
            if (index > -1 && index < buttons.size())
            {
                buttons.get(index).setSelected(true);
            }
        } else
        {
            hasInitialSelection = true;
            initialSelectedIndex = index;
        }
    }
    
    /**
     * Sets a single button selected (after the list is created).
     * @param item the item to be selected
     */
    public void setSelectedObj(final T item)
    {
        if (!wasCreated)
        {
            hasInitialSelection = true;
        }
        setSelectedIndex(items.indexOf(item));
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
        if (!wasCreated)
        {
            this.selectedItems = selectedItems;
            hasInitialSelection = true;
            
        } else if (selectedItems != null)
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
    }

    /**
     * Returns the selected Object or null if nothing was selected.
     * @return the selected Object or null if nothing was selected
     */
    public List<T> getSelectedObjects()
    {
        List<T> list = new Vector<T>();
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
     * @return true if at least one item is selected.
     */
    public boolean hasSelection()
    {
        for (JToggleButton tb : buttons)
        {
            if (tb.isSelected())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param okBtn the okBtn to set
     */
    public void setOkBtn(JButton okBtn)
    {
        this.okBtn = okBtn;
    }

    /**
     * @param changeListener the changeListener to set
     */
    public void setChangeListener(final ChangeListener changeListener)
    {
        this.changeListener = changeListener;
        
        if (wasCreated)
        {
            for (JToggleButton tb : buttons)
            {
                tb.addChangeListener(changeListener);
            }
        }
    }

    /**
     * @param actionListener the actionListener to set
     */
    public void setActionListener(ActionListener actionListener)
    {
        this.actionListener = actionListener;
        
        if (wasCreated)
        {
            for (JToggleButton tb : buttons)
            {
                tb.addActionListener(actionListener);
            }
        }
    }
    
}
