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
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

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
public class ChooseFromListDlg<T> extends JDialog implements ActionListener
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(ChooseFromListDlg.class);

    // Data Members
    protected JButton        cancelBtn;
    protected JButton        okBtn;
    protected JList          list;
    protected List<T>        items;
    protected ImageIcon      icon        = null;
    protected boolean        isCancelled = false;

    /**
     * Constructor.
     * @param frame parent frame
     * @param title the title of the dialog
     * @param itemList the list to be selected from
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame   frame, 
                             final String  title, 
                             final List<T> itemList) throws HeadlessException
    {
        this(frame, title, null, itemList, true);
    }

    /**
     * Constructor.
     * @param frame parent frame
     * @param title the title of the dialog
     * @param desc a description of what they are to do
     * @param itemList the list to be selected from
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame   frame, 
                             final String  title, 
                             final String  desc, 
                             final List<T> itemList) throws HeadlessException
    {
        this(frame, title, desc, itemList, true);
    }

    /**
     * Constructor.
     * @param frame parent frame
     * @param title the title of the dialog
     * @param itemList the list to be selected from
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame   frame, 
                             final String  title, 
                             final List<T> itemList, 
                             final boolean includeCancelBtn) throws HeadlessException
    {
        this(frame, title, null, itemList, includeCancelBtn);
    }

    /**
     * Constructor.
     * @param frame parent frame
     * @param title the title of the dialog
     * @param desc the list to be selected from
     * @param itemList the list to be selected from
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame frame, 
                             final String title, 
                             final String desc, 
                             final List<T> itemList, 
                             final boolean includeCancelBtn) throws HeadlessException
    {
        super(frame, true);
        
        this.items = itemList;
        createUI(title, desc, includeCancelBtn);
        
        setLocationRelativeTo(UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    /**
     * Constructor.
     * @param frame parent frame
     * @param title the title of the dialog
     * @param items the list to be selected from
     * @param icon the icon to be displayed in front of each entry in the list
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame     frame, 
                             final String    title, 
                             final List<T>   itemList, 
                             final ImageIcon icon) throws HeadlessException
    {
        super(frame, true);
        this.items = itemList;
        this.icon  = icon;

        createUI(title, null, true);
        setLocationRelativeTo(UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    /**
     * Create the UI for the dialog.
     * @param title title for dialog
     * @param desc the list to be selected from
     * @param includeCancelBtn indicates whether to create and displaty a cancel btn
     */
    protected void createUI(final String title, final String desc, final boolean includeCancelBtn)
    {
        this.setTitle(title);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
        
        if (desc != null)
        {
            JLabel lbl = new JLabel(desc, SwingConstants.CENTER);
            panel.add(lbl, BorderLayout.NORTH);
        }

        try
        {

            ListModel listModel = new AbstractListModel()
            {
                public int getSize() { return items.size(); }
                public Object getElementAt(int index) { return items.get(index).toString(); }
            };

            list = new JList(listModel);
            if (icon != null)
            {
                list.setCellRenderer(new IconListCellRenderer(icon)); // icon comes from the base class (it's probably size 16)
            }
            list.setVisibleRowCount(10);
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        okBtn.doClick(); //emulate button click
                    }
                }
            });
            list.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        updateUIState();
                    }
                }
            });
            JScrollPane listScroller = new JScrollPane(list);
            panel.add(listScroller, BorderLayout.CENTER);

            // Bottom Button UI
            okBtn             = new JButton(getResourceString("OK"));
            okBtn.addActionListener(this);
            getRootPane().setDefaultButton(okBtn);

            if (includeCancelBtn)
            {
                cancelBtn = new JButton(getResourceString("Cancel"));
                ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
                btnBuilder.addGriddedButtons(new JButton[] {cancelBtn, okBtn});
    
                cancelBtn.addActionListener(new ActionListener()
                        {  public void actionPerformed(ActionEvent ae) { setVisible(false); isCancelled = true;} });
                
                panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);
            } else
            {
                panel.add(okBtn, BorderLayout.SOUTH);
            }

            updateUIState();

        } catch (Exception ex)
        {
            log.error(ex);
        }

        setContentPane(panel);
        pack();
        //setLocationRelativeTo(locationComp);

    }
    
    /**
     * Allows the list to be configured for multi-item selection 
     */
    public void setMultiSelect(boolean val)
    {
        list.setSelectionMode(val ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * Update the button UI given the state of the list.
     */
    protected void updateUIState()
    {
        okBtn.setEnabled(list.getSelectedIndex() != -1);
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
        int inx = list.getSelectedIndex();
        if (inx != -1)
        {
            return items.get(inx);
        }
        return null;
    }
    
    /**
     * Returns the selected Object or null if nothing was selected.
     * @return the selected Object or null if nothing was selected
     */
    @SuppressWarnings("unchecked")
    public List<T> getSelectedObjects()
    {
        List<T> selectedItems = new ArrayList<T>(5);
        for (Object obj : list.getSelectedValues())
        {
            selectedItems.add((T)obj);
        }
        return selectedItems;
    }
    
    /**
     * Returns the indices that were selected.
     * @return the indices that were selected 
     */
    public int[] getSelectedIndices()
    {
        return list.getSelectedIndices();
    }
    
    /**
     * Set the selcted indices.
     * @param indices the array of indices
     */
    public void setIndices(final int[] indices)
    {
        list.setSelectedIndices(indices);
    }

    /**
     * Returns whether it was cancelled.
     * @return whether it was cancelled
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }



}
