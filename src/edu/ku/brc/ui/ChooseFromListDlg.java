/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import edu.ku.brc.specify.help.HelpMgr;

/**
 * Choose an object from a list of Objects using their "toString"
 * 
 * @code_status Complete
 * 
 * @author rods
 * 
 */
@SuppressWarnings("serial")
public class ChooseFromListDlg<T> extends JDialog
{
    // Static Data Members
    private static final Logger log              = Logger.getLogger(ChooseFromListDlg.class);

    // Data Members
    protected JButton           cancelBtn;
    protected JButton           okBtn;
    protected JButton           helpBtn;
    protected JList             list             = null;
    protected List<T>           items;
    protected ImageIcon         icon             = null;
    protected boolean           isCancelled      = false;

    // Needed for delayed building of Dialog
    protected String            title            = null;
    protected String            desc             = null;
    protected Boolean           includeCancelBtn = true;
    protected Boolean           includeHelpBtn   = false;
    protected String            okLabel          = null;
    protected String            cancelLabel      = null;
    protected String            helpLabel        = null;
    protected String            helpContext      = "";
    protected boolean           isMultiSelect    = false;
    protected int[]             selectedIndices  = null;

    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param itemList the list to be selected from
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame frame, final String title, final List<T> itemList)
            throws HeadlessException
    {
        this(frame, title, null, itemList, true, false, "");
    }

    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param desc a description of what they are to do
     * @param itemList the list to be selected from
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame frame, final String title, final String desc,
            final List<T> itemList) throws HeadlessException
    {
        this(frame, title, desc, itemList, true, false, "");
    }

    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param itemList the list to be selected from
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame frame, final String title, final List<T> itemList,
            final boolean includeCancelBtn) throws HeadlessException
    {
        this(frame, title, null, itemList, includeCancelBtn, false, "");
    }

    /**
     * Constructor.
     * 
     * @param frame  parent frame
     * @param title the title of the dialog
     * @param itemList the list to be selected from
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame frame, final String title, final String desc,
            final List<T> itemList, final boolean includeCancelBtn) throws HeadlessException
    {
        this(frame, title, desc, itemList, includeCancelBtn, false, "");
    }

    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param desc the list to be selected from
     * @param itemList the list to be selected from
     * @param includeCancelBtn  indicates whether to create and displaty a cancel btn
     * @param includeHelpBtn indicates whether to create and displaty a help btn
     * @param helpContext  help context identifier
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame frame, final String title, final String desc,
            final List<T> itemList, final boolean includeCancelBtn, final boolean includeHelpBtn,
            final String helpContext) throws HeadlessException
    {
        super(frame, true);

        this.title = title;
        this.desc = desc;
        this.items = itemList;
        this.includeCancelBtn = includeCancelBtn;
        this.includeHelpBtn = includeHelpBtn;
        this.helpContext = helpContext;

        setLocationRelativeTo(UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    /**
     * Constructor.
     * 
     * @param frame  parent frame
     * @param title the title of the dialog
     * @param items the list to be selected from
     * @param icon the icon to be displayed in front of each entry in the list
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame frame, final String title, final List<T> itemList,
            final ImageIcon icon) throws HeadlessException
    {
        super(frame, true);
        this.title = title;
        this.items = itemList;
        this.icon = icon;

        setLocationRelativeTo(UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param items the list to be selected from
     * @param icon the icon to be displayed in front of each entry in the list
     * @param includeHelpBtn indicates whether to create and displaty a help btn
     * @param helpContext help context identifier
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame frame, final String title, final List<T> itemList,
            final ImageIcon icon, final boolean includeHelpBtn, final String helpContext)
            throws HeadlessException
    {
        super(frame, true);
        this.title = title;
        this.items = itemList;
        this.icon = icon;
        this.includeHelpBtn = includeHelpBtn;
        this.helpContext = helpContext;

        setLocationRelativeTo(UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    /**
     * Create the UI for the dialog.
     * 
     * @param title title for dialog
     * @param desc the list to be selected from
     * @param includeCancelBtn  indicates whether to create and displaty a cancel btn
     * @param includeHelpBtn indicates whether to create and displaty a help btn
     * @param helpContext help context identifier
     * @param titleArg title for dialog
     * @param desc the list to be selected from
     * @param includeCancelBtn indicates whether to create and displaty a cancel btn
     */
    protected void createUI()
    {
        setTitle(title);

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
                public int getSize()
                {
                    return items.size();
                }

                public Object getElementAt(int index)
                {
                    return items.get(index).toString();
                }
            };

            list = new JList(listModel);
            if (icon != null)
            {
                list.setCellRenderer(new IconListCellRenderer(icon)); // icon comes from the base
                // class (it's probably size
                // 16)
            }
            list.setSelectionMode(isMultiSelect ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
            list.setVisibleRowCount(10);
            
            if (selectedIndices != null)
            {
                list.setSelectedIndices(selectedIndices);
            }

            list.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getClickCount() == 2)
                    {
                        okBtn.doClick(); // emulate button click
                    }
                }
            });
            list.addListSelectionListener(new ListSelectionListener()
            {
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
            okBtn = new JButton(StringUtils.isNotEmpty(okLabel) ? okLabel : getResourceString("OK"));
            okBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    setVisible(false);
                    isCancelled = false;
                }
            });
            getRootPane().setDefaultButton(okBtn);

            if (includeCancelBtn || includeHelpBtn)
            {
                ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
                if (includeCancelBtn)
                {
                    cancelBtn = new JButton(StringUtils.isNotEmpty(cancelLabel) ? cancelLabel
                            : getResourceString("Cancel"));
                    cancelBtn.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            setVisible(false);
                            isCancelled = true;
                        }
                    });
                }
                if (includeHelpBtn)
                {
                    helpBtn = new JButton(StringUtils.isNotEmpty(cancelLabel) ? cancelLabel
                            : getResourceString("Help"));
                    HelpMgr.registerComponent(helpBtn, helpContext);
                }
                if (includeCancelBtn && includeHelpBtn)
                {
                    btnBuilder.addGriddedButtons(new JButton[] { cancelBtn, okBtn, helpBtn });
                } else if (includeCancelBtn)
                {
                    btnBuilder.addGriddedButtons(new JButton[] { cancelBtn, okBtn });
                } else
                {
                    btnBuilder.addGriddedButtons(new JButton[] { okBtn, helpBtn });
                }
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
        // setLocationRelativeTo(locationComp);

    }

    /**
     * Allows the list to be configured for multi-item selection.
     */
    public void setMultiSelect(boolean isMultiSelectArg)
    {
        this.isMultiSelect = isMultiSelectArg;
    }

    /**
     * Update the button UI given the state of the list.
     */
    protected void updateUIState()
    {
        okBtn.setEnabled(list.getSelectedIndex() != -1);
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
            selectedItems.add((T) obj);
        }
        return selectedItems;
    }

    /**
     * Returns the indices that were selected.
     * @return the indices that were selected
     */
    public int[] getSelectedIndices()
    {
        return list != null ? list.getSelectedIndices() : null;
    }

    /**
     * Set the selcted indices.
     * @param indices
     *            the array of indices
     */
    public void setIndices(final int[] indices)
    {
        selectedIndices = indices;
    }

    /**
     * Returns whether it was cancelled.
     * @return whether it was cancelled
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }

    public void setOkLabel(final String text)
    {
        this.okLabel = text;
    }

    public void setCancelLabel(final String text)
    {
        this.cancelLabel = text;
    }

    /*
     * (non-Javadoc)
     * @see java.awt.Dialog#setVisible(boolean)
     */
    @Override
    public void setVisible(final boolean visible)
    {
        if (visible && list == null)
        {
            createUI();
        }

        super.setVisible(visible);
    }

}
