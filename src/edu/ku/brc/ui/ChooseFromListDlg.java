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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.ui.HelpMgr;

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
    public static final int OK_BTN             = 1;
    public static final int CANCEL_BTN         = 2;
    public static final int HELP_BTN           = 4;
    public static final int APPLY_BTN          = 8;
    
    public static final int OKCANCEL           = OK_BTN | CANCEL_BTN;
    public static final int OKHELP             = OK_BTN | HELP_BTN;
    public static final int OKCANCELHELP       = OK_BTN | CANCEL_BTN | HELP_BTN;
    public static final int OKCANCELAPPLY      = OK_BTN | CANCEL_BTN | APPLY_BTN;
    public static final int OKCANCELAPPLYHELP  = OK_BTN | CANCEL_BTN | APPLY_BTN | HELP_BTN;
    
    // Static Data Members
    private static final Logger log              = Logger.getLogger(ChooseFromListDlg.class);

    // Data Members
    protected JButton           okBtn            = null;
    protected JButton           cancelBtn        = null;
    protected JButton           helpBtn          = null;
    protected JButton           applyBtn         = null;
    
    // Button Labels
    protected String            okLabel          = null;
    protected String            cancelLabel      = null;
    protected String            helpLabel        = null;
    protected String            applyLabel       = null;

    protected JList             list             = null;
    protected List<T>           items;
    protected ImageIcon         icon             = null;
    protected boolean           isCancelled      = true;
    protected int               btnPressed       = CANCEL_BTN;

    // Needed for delayed building of Dialog
    protected String            title            = null;
    protected String            desc             = null;
    protected int               whichBtns        = OK_BTN;
    protected String            helpContext      = null;
    protected boolean           isMultiSelect    = false;
    protected int[]             selectedIndices  = null;
    protected boolean           isCloseOnApply   = false;
    
    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param itemList the list to be selected from
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame frame, 
                             final String title, 
                             final List<T> itemList) throws HeadlessException
    {
        this(frame, title, OK_BTN | CANCEL_BTN, itemList);
    }

    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param itemList the list to be selected from
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame   frame, 
                             final String  title, 
                             final int     whichBtns,
                             final List<T> itemList) throws HeadlessException
    {
        this(frame, title, null, whichBtns, itemList);
    }

    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param desc a description of what they are to do
     * @param whichBtns mask describing which buttons to create
     * @param itemList the list to be selected from
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame   frame, 
                             final String  title, 
                             final String  desc,
                             final int     whichBtns,
                             final List<T> itemList) throws HeadlessException
    {
        this(frame, title, desc, whichBtns, itemList, null);
    }

    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param desc the list to be selected from
     * @param itemList the list to be selected from
     * @param whichBtns mask describing which buttons to create
     * @param helpContext  help context identifier
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame   frame, 
                             final String  title, 
                             final String  desc,
                             final int     whichBtns,
                             final List<T> itemList,
                             final String  helpContext) throws HeadlessException
    {
        super(frame, true);

        this.title       = title;
        this.desc        = desc;
        this.items       = itemList;
        this.whichBtns   = whichBtns;
        this.helpContext = helpContext;

        setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
    public ChooseFromListDlg(final Frame     frame, 
                             final String    title, 
                             final List<T>   itemList,
                             final ImageIcon icon) throws HeadlessException
    {
        this(frame, title, OKCANCEL, itemList, icon, null);
    }

    /**
     * Constructor.
     * 
     * @param frame  parent frame
     * @param title the title of the dialog
     * @param items the list to be selected from
     * @param whichBtns mask describing which buttons to create
     * @param icon the icon to be displayed in front of each entry in the list
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame     frame, 
                             final String    title, 
                             final int       whichBtns,
                             final List<T>   itemList,
                             final ImageIcon icon) throws HeadlessException
    {
        this(frame, title, whichBtns, itemList, icon, null);
    }

    /**
     * Constructor.
     * 
     * @param frame  parent frame
     * @param title the title of the dialog
     * @param items the list to be selected from
     * @param whichBtns mask describing which buttons to create
     * @param icon the icon to be displayed in front of each entry in the list
     * @param helpContext  help context identifier
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final Frame     frame, 
                             final String    title, 
                             final int       whichBtns,
                             final List<T>   itemList,
                             final ImageIcon icon,
                             final String    helpContext) throws HeadlessException
    {
        this(frame, title, whichBtns, itemList);
        
        this.icon        = icon;
        this.helpContext = helpContext;
        
        setModal(true);

        setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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

        boolean hasDesc = StringUtils.isNotEmpty(desc);
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:max(300px;p):g", "p," + (hasDesc ? "2px,p," : "") + "5px,p"));
        CellConstraints cc      = new CellConstraints();
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        int y = 1;
        if (hasDesc)
        {
            JLabel lbl = new JLabel(desc, SwingConstants.CENTER);
            builder.add(lbl, cc.xy(1,y)); y += 2;
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
            builder.add(listScroller, cc.xy(1,y)); y += 2;

            // Bottom Button UI
            okBtn = new JButton(StringUtils.isNotEmpty(okLabel) ? okLabel : getResourceString("OK"));
            okBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    isCancelled = false;
                    btnPressed  = OK_BTN;
                    setVisible(false);
                }
            });
            getRootPane().setDefaultButton(okBtn);
            
            
            if ((whichBtns & CANCEL_BTN) == CANCEL_BTN)
            {
                cancelBtn = new JButton(StringUtils.isNotEmpty(cancelLabel) ? cancelLabel : getResourceString("Cancel"));
                cancelBtn.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        isCancelled = true;
                        btnPressed  = CANCEL_BTN;
                        setVisible(false);
                    }
                });
            }
            
            if ((whichBtns & HELP_BTN) == HELP_BTN)
            {
                helpBtn = new JButton(StringUtils.isNotEmpty(cancelLabel) ? cancelLabel : getResourceString("Help"));
                if (StringUtils.isNotEmpty(helpContext))
                {
                    HelpMgr.registerComponent(helpBtn, helpContext);
                } else
                {
                    helpBtn.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            btnPressed  = HELP_BTN;
                        }
                    }); 
                }
            }
            
            if ((whichBtns & APPLY_BTN) == APPLY_BTN)
            {
                applyBtn = new JButton(StringUtils.isNotEmpty(applyLabel) ? applyLabel : getResourceString("Apply"));
                applyBtn.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        btnPressed  = APPLY_BTN;
                        if (isCloseOnApply)
                        {
                            isCancelled = false;
                            setVisible(false);
                        }
                    }
                });
            }
            
            JPanel bb;
            if (whichBtns == OK_BTN)
            {
                bb = ButtonBarFactory.buildOKBar(okBtn);
                
            } else if (whichBtns == OKCANCEL)
            {
                bb = ButtonBarFactory.buildOKCancelBar(okBtn, cancelBtn);
                
            } else if (whichBtns == OKCANCELAPPLY)
            {
                bb = ButtonBarFactory.buildOKCancelApplyBar(okBtn, cancelBtn, applyBtn);
                
            } else if (whichBtns == OKHELP)
            {
                bb = ButtonBarFactory.buildOKHelpBar(okBtn, helpBtn);
                
            } else if (whichBtns == OKCANCELHELP)
            {
                bb = ButtonBarFactory.buildOKCancelHelpBar(okBtn, cancelBtn, helpBtn);
                
            } else if (whichBtns == OKCANCELAPPLYHELP)
            {
                bb = ButtonBarFactory.buildOKCancelApplyHelpBar(okBtn, cancelBtn, applyBtn, helpBtn);
                
            } else
            {
                bb = ButtonBarFactory.buildOKBar(okBtn);
            }

            builder.add(bb, cc.xy(1,y)); y += 2;

            updateUIState();

        } catch (Exception ex)
        {
            log.error(ex);
        }

        setContentPane(builder.getPanel());
        pack();
        // setLocationRelativeTo(locationComp);

    }
    
    public void setCloseOnApply(final boolean isCloseOnApply)
    {
        this.isCloseOnApply = isCloseOnApply;
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

    public int getBtnPressed()
    {
        return btnPressed;
    }

    public void setApplyLabel(String applyLabel)
    {
        this.applyLabel = applyLabel;
    }

    public void setHelpContext(String helpContext)
    {
        this.helpContext = helpContext;
    }

    public void setHelpLabel(String helpLabel)
    {
        this.helpLabel = helpLabel;
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
        UIHelper.centerWindow(this);
        super.setVisible(visible);
    }

}
