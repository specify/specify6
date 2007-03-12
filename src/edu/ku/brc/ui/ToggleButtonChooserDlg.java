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

import java.awt.BorderLayout;
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
import javax.swing.BoxLayout;
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

import com.jgoodies.forms.builder.ButtonBarBuilder;

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
    protected JButton               okBtn;
    protected List<T>               items;
    protected Vector<JToggleButton> butons      = new Vector<JToggleButton>(10);
    protected ImageIcon             icon        = null;
    protected boolean               isCancelled = false;
    
    protected ButtonGroup           group       = null;

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

        createUI(title, desc, uiType);
        setLocationRelativeTo(UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    /**
     * Create the UI for the dialog.
     * @param title dialog title
     * @param desc description label above list (optional)
     */
    protected void createUI(final String title, final String desc, final Type uiType)
    {
        setTitle(title);
        
        JPanel panel = new JPanel(new BorderLayout());
        if (desc != null)
        {
            JLabel lbl = new JLabel(desc, SwingConstants.CENTER);
            panel.add(lbl, BorderLayout.NORTH);
            panel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        }

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
        listPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), listPanel.getBorder()));

        group = uiType == Type.Checkbox ? null : new ButtonGroup();

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
            butons.add(togBtn);
            listPanel.add(togBtn);
        }

        if (butons.size() > 0)
        {
            JToggleButton togBtn = butons.get(0);
            Dimension     dim    = getPreferredSize();
            dim.height = togBtn.getPreferredSize().height * 10;
            listPanel.setPreferredSize(dim);
        }

        JScrollPane listScroller = new JScrollPane(listPanel);
        panel.add(listScroller, BorderLayout.CENTER);

        // Bottom Button UI
        cancelBtn         = new JButton(getResourceString("Cancel"));
        okBtn             = new JButton(getResourceString("OK"));

        okBtn.addActionListener(this);
        getRootPane().setDefaultButton(okBtn);

        ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
        btnBuilder.addGriddedButtons(new JButton[] {cancelBtn, okBtn});

        cancelBtn.addActionListener(new ActionListener()
                    {  public void actionPerformed(ActionEvent ae) { setVisible(false); isCancelled = true;} });

        panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);

        setContentPane(panel);
        pack();

    }


    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        setVisible(false);
    }

    /**
     * Returns the selected Object or null if nothing was selected.
     * @return the selected Object or null if nothing was selected
     */
    public T getSelectedObject()
    {
        int inx = 0;
        for (JToggleButton tb : butons)
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
     * Sets the object in the items list to be selected
     * @param selectedItems the list of items to be selected
     */
    public void setSelectedObjects(final List<T> selectedItems)
    {
        for (T obj : selectedItems)
        {
            int inx = 0;
            for (JToggleButton tb : butons)
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
        for (JToggleButton tb : butons)
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
