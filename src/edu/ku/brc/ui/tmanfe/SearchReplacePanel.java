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
/**
 * 
 */
package edu.ku.brc.ui.tmanfe;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.TableSearcher;
import edu.ku.brc.ui.TableSearcherCell;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author megkumin
 * 
 * @code_status Beta
 * 
 * Created Date: Mar 1, 2007
 * 
 */
@SuppressWarnings("serial")
public class SearchReplacePanel extends JPanel
{
    private final String          FIND                    = "Find"; //i18n

    protected SpreadSheet         table;

    protected boolean             isSearchDown            = true;
    @SuppressWarnings("unused")
    private boolean               isFinishedSearchingDown = false;
    @SuppressWarnings("unused")
    private boolean               isFinishedSearchingUp   = true;
    @SuppressWarnings("unused")
    // private int lastIndex = -1;
    private JLabel                findLabel;
    private JButton               cancelButton;
    protected JButton             nextButton;
    protected JButton             previousButton;
    protected JButton             replaceButton;
    protected JButton             replaceAllButton;
    private int                   textFieldLength         = 10;
    protected JTextField          findField               = createTextField();
    private JTextField            replaceField            = createTextField();
    private JCheckBox             matchCaseButton;
    private JCheckBox             wrapSearchButton;
    private JLabel                statusInfo;

    private HideFindPanelAction   hideFindPanelAction     = new HideFindPanelAction();
    private SearchAction          searchAction            = new SearchAction();
    private ReplaceAction         replaceAction           = new ReplaceAction();
    private LaunchFindAction      launchFindAction        = null;
    private ListSelectionListener listSelectionListener   = null;
    private CellConstraints       cc                      = new CellConstraints();
    private FormLayout            formLayout              = new FormLayout(
                                                                  "p,8px,p,1px,p,1px,p,1px,p,4px,p,1px,"
                                                                          + "p,1px,p,1px,p,1px,p",
                                                                  "p,1px,p,1px");
    private PanelBuilder          builder                 = new PanelBuilder(formLayout, this);

    protected static final Logger log                     = Logger
                                                                  .getLogger(SearchReplacePanel.class);
    TableSearcher tableSearcher ;//= new TableSearcher(table, getPanel());
    /**
     * Constructor for the Find/Replace panel, takes a SearchableJXTable (extended from JXTable)
     * 
     * @param query - a SearchableJXTable table
     */
    public SearchReplacePanel(final SpreadSheet mytable)
    {
        this.table = mytable;
        this.setVisible(false);
        createFindAndReplacePanel();
        handleTableSelections();
        tableSearcher = new TableSearcher(table, this);
    }
    
    /**
     * detects when a table changes has been made and changes the next, previous buttons, as well as
     * clears the status label. 
     */
   public void handleTableSelections()
    {
        listSelectionListener = new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                nextButton.setEnabled(true);
                previousButton.setEnabled(true);
                updateStatusLabel(-1, false);
            }          
        };
        table.getSelectionModel().addListSelectionListener(listSelectionListener);
    }
    
    /**
     * @param shouldShow - flag noting whether the panel should be visible
     * @return the find/replace panel to be displayed
     */
    protected JPanel showFindAndReplacePanel(boolean shouldShow)
    {
        if (!shouldShow)
        {
            this.setVisible(false);
        } else
        {
            stopTableEditing();            
            this.setVisible(true);
            findField.requestFocusInWindow();
            UsageTracker.incrUsageCount("WB.ShowFindReplace");
        }
        return this;
    }

    /**
     * sets up the keystroke mappings for "Ctrl-F" firing the find/replace panel
     * Escape making it disappear, and enter key firing a search
     */
    private void setupKeyStrokeMappings()
    {
        table.getActionMap().clear();
        
        //override the "Ctrl-F" function for launching the find dialog shipped with JXTable
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), UIRegistry.getResourceString(FIND));        
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), UIRegistry.getResourceString(FIND));
        
        //create action that will display the find/replace dialog
        launchFindAction = new LaunchFindAction();
        table.getActionMap().put(FIND, launchFindAction);

        //Allow ESC buttun to call DisablePanelAction   
        String CANCEL_KEY = "CANCELKEY"; // i18n
        //Allow ENTER button to SearchAction
        String ENTER_KEY = "ENTERKEY"; // i18n
        String REPLACE_KEY = "REPLACEKEY"; // i18n

        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);

        InputMap textFieldInputMap = findField.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        textFieldInputMap.put(enterKey, ENTER_KEY);
        textFieldInputMap.put(escapeKey, CANCEL_KEY);
        
        ActionMap textFieldActionMap = findField.getActionMap();
        textFieldActionMap.put(ENTER_KEY, searchAction);
        textFieldActionMap.put(CANCEL_KEY, hideFindPanelAction);
        
        if (!table.isReadOnly())
        {
            InputMap replaceFieldInputMap = replaceField.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            replaceFieldInputMap.put(enterKey, REPLACE_KEY);
            replaceFieldInputMap.put(escapeKey, CANCEL_KEY);
        
            ActionMap replaceFieldActionMap = replaceField.getActionMap();
            replaceFieldActionMap.put(REPLACE_KEY, replaceAction);
            replaceFieldActionMap.put(CANCEL_KEY, hideFindPanelAction);
        }
    }
    
    /**
     * 
     */
    private void createReplacePanel()
    {
        if (!table.isReadOnly())
        {
            replaceField.setColumns(textFieldLength);
            replaceField.addKeyListener(new FindReplaceTextFieldKeyAdapter());

            replaceButton = createButton(getResourceString("SS_SR_REPLACE"));
            replaceButton.addActionListener(replaceAction);

            replaceAllButton = createButton(getResourceString("SS_SR_REPLACEALL"));
            replaceAllButton.addActionListener(replaceAction);

            // replaceButton.setMnemonic(KeyEvent.VK_N);
            // replaceButton.addActionListener(searchAction);
            // JComponent[] itemSample = { new JMenuItem("Replace"), new JMenuItem("Replace All") };
            // memoryReplaceButton = new MemoryDropDownButton("Replace",
            // IconManager.getIcon("DropDownArrow"),
            // 1, java.util.Arrays.asList(itemSample));
            // memoryReplaceButton.setOverrideBorder(true, memoryReplaceButton.raisedBorder);
            // memoryReplaceButton.setEnabled(false);

            builder.add(replaceField, cc.xy(5, 3));
            builder.add(replaceButton, cc.xy(7, 3));
            builder.add(replaceAllButton, cc.xy(9, 3));
        }
    }
    
    /**
     * creates the find/replace JPanel and associates keyboard shortcuts for launching,
     * displaying and navigating the search panel
     */
    private void createFindAndReplacePanel()
    {
        setupKeyStrokeMappings();
        createFindPanel();
        createReplacePanel();  
        
        statusInfo = createLabel("");
        builder.add(statusInfo, cc.xywh(11, 3, 4, 1));
        
        /*
        Font font = wrapSearchButton.getFont();
        font = new Font(font.getFontName(), font.getStyle(), font.getSize()-2);
        nextButton.setFont(font);
        previousButton.setFont(font);
        matchCaseButton.setFont(font);
        wrapSearchButton.setFont(font);
        replaceButton.setFont(font);
        replaceAllButton.setFont(font);
        
        font = findLabel.getFont();
        font = new Font(font.getFontName(), font.getStyle(), font.getSize()-2);
        findLabel.setFont(font);
        statusInfo.setFont(font);*/
    }
    
    /**
     * Creates the panel that displays the close button, the search field, the next button,
     * the previous button, the match case checkbox and the wrap search checkbox
     */
    private void createFindPanel()
    {
        cancelButton = createButton(hideFindPanelAction);
        cancelButton.setIcon(IconManager.getIcon("Close"));
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        cancelButton.setBorder(null);
        
        findLabel = createLabel(getResourceString("SS_SR_FIND") + ": ", SwingConstants.RIGHT);

        nextButton = createButton(getResourceString("SS_SR_NEXT"));//, new ImageIcon(Specify.class.getResource("images/down.png")));
        nextButton.setEnabled(false);
        UIHelper.setLocalizedMnemonic(previousButton, "SS_SR_NEXT_MNEU");
        nextButton.addActionListener(searchAction);

        previousButton = createButton(getResourceString("SS_SR_PREVIOUS"));//, new ImageIcon(Specify.class.getResource("images/up.png")));
        previousButton.setEnabled(false);
        UIHelper.setLocalizedMnemonic(previousButton, "SS_SR_PREVIOUS_MNEU");

        previousButton.addActionListener(searchAction);

        //JComponent[] itemSample = { new JMenuItem("Replace"), new JMenuItem("Replace All") };
        //replaceButton = new MemoryDropDownButton("Replace", IconManager.getIcon("DropDownArrow"),
        //                1, java.util.Arrays.asList(itemSample));
        //replaceButton.setOverrideBorder(true, replaceButton.raisedBorder);
        //replaceButton.setEnabled(false);

        findField.setColumns(textFieldLength);
        findField.setText("");
        findField.addKeyListener(new FindReplaceTextFieldKeyAdapter());

        //replaceField.setColumns(textFieldLength);
        //replaceField.addKeyListener(new InputFieldKeyAdapter());

        matchCaseButton = createCheckBox(getResourceString("SS_SR_MATCHCASE"));
        matchCaseButton.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                //don't care
            }
        });

        wrapSearchButton = createCheckBox(getResourceString("SS_SR_WRAP"));
        wrapSearchButton.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                setCheckAndSetWrapOption();
            }
        });
               
        builder.add(cancelButton,        cc.xy(1,1));
        builder.add(findLabel,           cc.xy(3,1));
        builder.add(findField,           cc.xy(5,1));
        builder.add(nextButton,          cc.xy(7,1));
        builder.add(previousButton,      cc.xy(9,1));
        builder.add(matchCaseButton,     cc.xy(11,1));
        builder.add(wrapSearchButton,    cc.xy(13,1));
        
       // statusInfo = createLabel("");
       // builder.add(statusInfo,          cc.xy(15,1));
    }

//    /**
//     * @param args
//     */
//    public static void main(String[] args)
//    {
//        
//    }

    /**
     * @return the state fo the "match case" option
     */
    protected boolean getMatchCaseFlag()
    {
        return matchCaseButton.isSelected();
    }

    /**
     * @return the state of the "wrap search" option
     */
    protected boolean getWrapSearchFlag()
    {
        return wrapSearchButton.isSelected();
    }

    /**
     * Setst the status label to alert the user that a word has not been found.
     */
    public void setStatusLabelWithFailedFind()
    {
        log.info("NOT FOUND - Findvalue[" + findField.getText() + "] displaying statusInfo to the user");
        statusInfo.setHorizontalTextPosition(SwingConstants.RIGHT);
        statusInfo.setIcon(IconManager.getIcon("Error", IconManager.IconSize.Std16));
        statusInfo.setText(getResourceString("SS_SR_PHRASENOTFOUND"));
    }

    /**
     *  Setst the status label to alert the user that the end of the table is 
     *  has been reached during the search.
     *      
     */
    public void setStatusLabelEndReached()
    {
        log.info("NOT FOUND - Findvalue[" + findField.getText() + "] displaying statusInfo to the user");
        statusInfo.setHorizontalTextPosition(SwingConstants.RIGHT);
        statusInfo.setIcon(IconManager.getIcon("ValidationValid", IconManager.IconSize.Std16));
        statusInfo.setText(getResourceString("SS_SR_ENDOFTABLE"));
    }
    
    /**
     * Clears the label that tells the user the status of the search/replace
     */
    public void updateStatusLabel(int count, boolean isReplace)
    {
        //log.debug("clearing status lable");
        statusInfo.setHorizontalTextPosition(SwingConstants.RIGHT);
        statusInfo.setIcon(null);
        if (count > 0)
        {
            if(!isReplace)
            {
                //Count for find is always 1?? But maybe not when replacing. 
                if (!table.isReadOnly() || count > 1)
                {
                    String key = count == 1 ? "SearchReplacePanel.FOUND_MATCH" : "SearchReplacePanel.FOUND_MATCHES";
                    statusInfo.setText(String.format(UIRegistry.getResourceString(key), count));
                }
            }
            else
            {
                String key = count == 1 ? "SearchReplacePanel.REPLACE_CELL" : "SearchReplacePanel.REPLACED_CELLS";
                statusInfo.setText(String.format(UIRegistry.getResourceString(key), count));
            }
        }
        else statusInfo.setText("");
    }    
    
//    /**
//     * @return boolean false if the table is null
//     */
//    private boolean isTableValid()
//    {
//        if (table == null)
//        {
//            setStatusLabelWithFailedFind();
//            return false;
//        }
//        return true;
//    }

    /**
     * stops editing of the table.  
     */
    private void stopTableEditing()
    {
        if (table.getCellEditor() != null)
        {
            table.getCellEditor().stopCellEditing();
        }   
    }


    
    /**
     * checks to see if the "wrap search" option is enabled, sets teh button state and clears the label.
     */
    public void setCheckAndSetWrapOption()
    {
        if (wrapSearchButton.isSelected())
        {
            isFinishedSearchingDown = false;
            isFinishedSearchingUp = false;
            nextButton.setEnabled(true);     
            previousButton.setEnabled(true);
            updateStatusLabel(-1, false);
        }
    }
    
    
    
    public void updateTableUiForFoundValue(TableSearcherCell cell, int replacementCount, boolean isReplace)
    {
        //log.debug("updateTableUiForFoundValue()");
        //boolean found = cell.isFound();
        int curRow = -1;
        int curCol = -1;
        if (replacementCount > 0) 
        {
            //log.debug("updateTableUiForFoundValue() - update for found cell");

            curRow = cell.getRow();
            curCol = cell.getColumn();
            //log.debug("updateTableUiForFoundValue() - Cell row[" + curRow + "] ");
            //log.debug("                               Cell col[" + curCol + "] ");
            //log.debug("updateTableUiForFoundValue() - preoapring to set selection model");
            ListSelectionModel rsm = table.getSelectionModel();
            ListSelectionModel csm = table.getColumnModel().getSelectionModel();
            rsm.setSelectionInterval(curRow, curRow);
            csm.setSelectionInterval(curCol, curCol);
            //log.debug("updateTableUiForFoundValue() - getting selection model");
            int ar = table.getSelectionModel().getAnchorSelectionIndex();
            int ac = table.getColumnModel().getSelectionModel().getAnchorSelectionIndex();

            Rectangle rect = table.getCellRect(ar, ac, false);
            if (rect != null && table.getAutoscrolls()) 
            {
                //log.debug("updateTableUiForFoundValue() - preparing to scroll");
                table.scrollRectToVisible(rect);
                //log.debug("updateTableUiForFoundValue() - done scrolling");
            }
            if (isSearchDown())
            {
                enablePreviousButton();
            }            
            else
            {
                enableNextButton();
            }
            updateStatusLabel(replacementCount, isReplace);
        }
        else
        {
            //log.debug("updateTableUiForFoundValue() found nothing");
            if (isSearchDown())
            {
                setFinishedSearchingDown(true);
                if (!getWrapSearchFlag())
                {
                    disableNextButton();
                    enablePreviousButton();                    
                }
                setStatusLabelWithFailedFind();
            }
            else
            {
                setFinishedSearchingUp(true);
                if (!getWrapSearchFlag())
                {
                    disablePreviousButton();
                    enableNextButton();                    
                }
                setStatusLabelWithFailedFind();
            }
        } 
    }
    
    /**
     * @return the launchFindAction
     */
    public Action getLaunchFindAction()
    {
        return launchFindAction;
    }
    
    /**
     * Clean up references.
     */
    public void cleanUp()
    {
        this.table.getSelectionModel().removeListSelectionListener(listSelectionListener);
        this.table.getActionMap().remove(FIND);
        this.table = null;
    }
    
    public SearchReplacePanel getPanel()
    {
        return this;
    }

    /**
     * Action that hides the search/repalce panel
     * 
     * @author megkumin
     *
     * @code_status Complete
     *
     * Created Date: Mar 15, 2007
     *
     */
    public class HideFindPanelAction extends AbstractAction
    {
        /**
         * Constructor
         */
        public HideFindPanelAction()
        {
            super();
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent evt)
        {
            showFindAndReplacePanel(false);
        }
        
        /**
         * hides the search/repalce panel
         */
        public void hide()
        {
            showFindAndReplacePanel(false);
        }
    }

    /**
     * Action that fires off a replace event
     * 
     * @author megkumin
     *
     * @code_status Complete
     *
     * Created Date: Mar 15, 2007
     *
     */

    private class ReplaceAction extends AbstractAction
    {
        /**
         * Constructor
         */
        public ReplaceAction()
        {
            super();
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent evt)
        {
            Object source = evt.getSource();
            setCheckAndSetWrapOption();
            final String replaceValue = getRepalceFieldValue();
            final String findValue = getFindFieldValue();
            TableSearcherCell cell = null;
            int replacementCount = 0;            
            if (source == replaceAllButton)
            {
                log.debug("replaceAllButton --------------------------------------------------------");
                tableSearcher = new TableSearcher(table, getPanel());
                int selectedCol = 0;
                int selectedRow = 0;
                int rowCount = table.getModel().getRowCount();
                int colCount = table.getModel().getColumnCount();
                boolean found = false;
                cell = tableSearcher.checkCellForMatch(getFindFieldValue(), selectedRow, selectedCol,  getMatchCaseFlag());
                while ((selectedRow > -1) 
                        && (selectedCol > -1) 
                        && (selectedRow <= rowCount - 1)
                        && (selectedCol <= colCount - 1))
                {
                    found = cell.isFound();
                    if (found)
                    {
                        tableSearcher.replace(cell, findValue, replaceValue, getMatchCaseFlag());
                        replacementCount++;
                    }
                    cell = tableSearcher.findNext(findValue, selectedRow, selectedCol, true, false, getMatchCaseFlag());
                    selectedCol = cell.getColumn();
                    selectedRow = cell.getRow();
                }
                updateTableUiForFoundValue(cell, replacementCount, true);
                UsageTracker.incrUsageCount("WB.ReplaceAllButton");
            }
            else if(source == replaceButton )
            {
                log.debug("replaceButton --------------------------------------------------------");
                tableSearcher = new TableSearcher(table, getPanel());
                int selectedCol = table.getSelectedColumn();
                int selectedRow = table.getSelectedRow();

                cell = tableSearcher.checkCellForMatch(getFindFieldValue(),selectedRow, selectedCol, getMatchCaseFlag());
                if (cell.isFound())
                {
                    replacementCount++;
                    tableSearcher.replace(cell, findValue, replaceValue, getMatchCaseFlag());
                    selectedCol = cell.getColumn();
                    selectedRow = cell.getRow();
                    cell = tableSearcher.findNext(findValue, selectedRow, selectedCol, isSearchDown(), getWrapSearchFlag(), getMatchCaseFlag());
                    updateTableUiForFoundValue(cell, replacementCount, true);
                }
                else
                {
                    setStatusLabelWithFailedFind();
                }
                UsageTracker.incrUsageCount("WB.ReplaceButton");
            }            
        }   
    }
    
    
    /**
     * Action that fires off a search event and determins if the search is
     * forwards or backwards.
     * 
     * @author megkumin
     * 
     * @code_status Complete
     * 
     * Created Date: Mar 15, 2007
     * 
     */
    private class SearchAction extends AbstractAction
    {
        /**
         * Constructor
         */
        public SearchAction()
        {
            super();
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent evt)
        {
            isSearchDown = true;
            Object source = evt.getSource();
            tableSearcher = new TableSearcher(table, getPanel());
            if (source == nextButton)
            {
                log.debug("nextButton --------------------------------------------------------");
                isSearchDown = true;
            } else if (source == previousButton)
            {
                log.debug("previousButton --------------------------------------------------------");
                isSearchDown = false;
            } 
            int replacementCount = 0;
            UsageTracker.incrUsageCount("WB.FindButton");
            setCheckAndSetWrapOption();
            log.debug("action performed");
            final String findValue = getFindFieldValue();
            int curRow = table.getSelectedRow();
            int curCol = table.getSelectedColumn();
            TableSearcherCell cell = tableSearcher.findNext(findValue, curRow, curCol, isSearchDown(), getWrapSearchFlag(), getMatchCaseFlag());
            if(cell.isFound())replacementCount++;
            updateTableUiForFoundValue(cell, replacementCount, false);
        }
    }

    /**
     * 
     * Action that displays the Find/Replace panel
     * @author megkumin
     *
     * @code_status Complete
     *
     * Created Date: Mar 22, 2007
     *
     */
    public class LaunchFindAction extends AbstractAction
    {       
        /**
         */
        public LaunchFindAction()
        {
            super(FIND);
            setEnabled(true);
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            showFindAndReplacePanel(true);
        }
    }
    
    /**
     * Handles the key events in the the search and replace textfields
     * @author megkumin
     *
     * @code_status Complete
     *
     * Created Date: Mar 15, 2007
     *
     */
    private class FindReplaceTextFieldKeyAdapter extends KeyAdapter
    {
        /**
         * Constructor
         */

        public FindReplaceTextFieldKeyAdapter()
        {
            super();
        }

        
        /* (non-Javadoc)
         * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
         */
        @Override
        public void keyReleased(KeyEvent ke)
        {            
            // make sure the user has entered a text string in teh find box before enabling find buttons
            boolean findTextState = (findField.getText().length() > 0);
            nextButton.setEnabled(findTextState);

            if (table.getSelectedRow() > 0 || table.getSelectedColumn() > 0)
            {
                previousButton.setEnabled(findTextState);
            }
            // make sure the user has entered a text string in teh 
            // searck textfield before enabling replace buttons
            // must make sure replace buttons aren't null because 
            // depending on context replace panel might not exsist
            if (replaceButton != null)
            {
                replaceButton.setEnabled(findTextState);
            }
            if (replaceAllButton != null)
            {
                replaceAllButton.setEnabled(findTextState);
            }
            int key = ke.getKeyCode();
            if (key != KeyEvent.VK_ENTER)
            {
                updateStatusLabel(-1, false);
            }            
        }
    }
    
    /**
     * @return the hideFindPanelAction
     */
    public HideFindPanelAction getHideFindPanelAction()
    {
        return hideFindPanelAction;
    }

    /**
     * @param hideFindPanelAction the hideFindPanelAction to set
     */
    public void setHideFindPanelAction(HideFindPanelAction hideFindPanelAction)
    {
        this.hideFindPanelAction = hideFindPanelAction;
    }

    
    public String getFindFieldValue()
    {
        if (findField != null)
        {
            return findField.getText();
        }
        return null;
    }

    public String getRepalceFieldValue()
    {
        if (replaceField != null)
        {
            return replaceField.getText();
        }
        return null;
    }
    
    public void enablePreviousButton()
    {
        if (previousButton != null)
            previousButton.setEnabled(true);
    }

    public void enableNextButton()
    {
        if (nextButton != null)
            nextButton.setEnabled(true);
    }
    
    public void disablePreviousButton()
    {
        if (previousButton != null)
            previousButton.setEnabled(false);
    }

    public void disableNextButton()
    {
        if (nextButton != null)
            nextButton.setEnabled(false);
    }

    /**
     * @return the isSearchDown
     */
    protected boolean isSearchDown()
    {
        return isSearchDown;
    }

    /**
     * @return the isFinishedSearchingDown
     */
    public boolean isFinishedSearchingDown()
    {
        return isFinishedSearchingDown;
    }

    /**
     * @param isFinishedSearchingDown the isFinishedSearchingDown to set
     */
    public void setFinishedSearchingDown(boolean isFinishedSearchingDown)
    {
        this.isFinishedSearchingDown = isFinishedSearchingDown;
    }

    /**
     * @return the isFinishedSearchingUp
     */
    public boolean isFinishedSearchingUp()
    {
        return isFinishedSearchingUp;
    }

    /**
     * @param isFinishedSearchingUp the isFinishedSearchingUp to set
     */
    public void setFinishedSearchingUp(boolean isFinishedSearchingUp)
    {
        this.isFinishedSearchingUp = isFinishedSearchingUp;
    }

    /**
     * @param isSearchDown the isSearchDown to set
     */
    public void setSearchDown(boolean isSearchDown)
    {
        this.isSearchDown = isSearchDown;
    }

    /**
     * @return the replaceButton
     */
    public JButton getReplaceButton()
    {
        return replaceButton;
    }

    /**
     * @param replaceButton the replaceButton to set
     */
    public void setReplaceButton(JButton replaceButton)
    {
        this.replaceButton = replaceButton;
    }

    /**
     * @return the replaceAllButton
     */
    public JButton getReplaceAllButton()
    {
        return replaceAllButton;
    }

    /**
     * @param replaceAllButton the replaceAllButton to set
     */
    public void setReplaceAllButton(JButton replaceAllButton)
    {
        this.replaceAllButton = replaceAllButton;
    }
}
