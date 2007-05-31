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
package edu.ku.brc.ui;

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
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
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
import edu.ku.brc.ui.tmanfe.SpreadSheet;
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
    private SpreadSheet              table;
    @SuppressWarnings("unused")
    private boolean                  isStartOfSearch         = true;
    private boolean                  isSearchDown            = true;
    @SuppressWarnings("unused")
    private boolean                  isFinishedSearchingDown = false;
    @SuppressWarnings("unused")
    private boolean                  isFinishedSearchingUp   = true;
    @SuppressWarnings("unused")
    private int                      lastIndex               = -1;

    private JLabel                   findLabel;
    private JButton                  cancelButton;
    private JButton                  nextButton;
    private JButton                  previousButton;
    private JButton                  replaceButton;
    private JButton                  replaceAllButton;   
    private int                      textFieldLength         = 10;
    private JTextField               findField               = new JTextField();
    private JTextField               replaceField            = new JTextField();
    private JCheckBox                matchCaseButton;
    private JCheckBox                wrapSearchButton;
    private JLabel                   statusInfo;

    private HideFindPanelAction      hideFindPanelAction     = new HideFindPanelAction();
    private SearchAction             searchAction            = new SearchAction();
    private ReplaceAction            replaceAction           = new ReplaceAction();
    private LaunchFindAction         launchFindAction        = null;

    CellConstraints                  cc                      = new CellConstraints();
    FormLayout                       formLayout              = new FormLayout("p,8px,p,1px,p,1px,p,1px,p,4px,p,1px," +
                                                                              "p,1px,p,1px,p,1px,p", "p,1px,p,1px");
    PanelBuilder                     builder                 = new PanelBuilder(formLayout, this);

    protected static final Logger    log                     = Logger.getLogger(SearchReplacePanel.class);

    /**
     * Constructor for the Find/Replace panel, takes a SearchableJXTable (extended from JXTable)
     * 
     * @param table - a SearchableJXTable table
     */
    public SearchReplacePanel(SpreadSheet mytable)
    {
        this.table = mytable;
        this.setVisible(false);
        //this.searchable = mytable.getSearchable();
        createFindAndReplacePanel();
        handleTableSelections();
    }
    
    /**
     * detects when a table changes has been made and changes the next, previous buttons, as well as
     * clears teh status label. 
     */
   public void handleTableSelections()
    {
        ListSelectionListener lsl = new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
               // log.debug("table selectoin");
                nextButton.setEnabled(true);
                previousButton.setEnabled(true);
                clearStatusLabel();
            }          
        };
        table.getSelectionModel().addListSelectionListener(lsl);
    }
    
    /**
     * @param shouldShow - flag noting whether the panel should be visible
     * @return the find/replace panel to be displayed
     */
    private JPanel showFindAndReplacePanel(boolean shouldShow)
    {
        if (!shouldShow)
        {
            log.debug("hiding Find/Replace panel");
            this.setVisible(false);
        } else
        {
            if (table.getCellEditor() != null)
            {
                table.getCellEditor().stopCellEditing();
            }
            
            log.debug("showing Find/replace panel");
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
        //override the "Ctrl-F" function for launching the find dialog shipped with JXTable
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Find");        
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), "Find");
        
        //create action that will display the find/replace dialog
        launchFindAction = new LaunchFindAction();
        table.getActionMap().put("Find", launchFindAction);
        
        //Allow ESC buttun to call DisablePanelAction   
        String CANCEL_KEY = "CANCEL_KEY";
        //Allow ENTER button to SearchAction
        String ENTER_KEY = "ENTER_KEY";

        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);

        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(enterKey, ENTER_KEY);
        inputMap.put(escapeKey, CANCEL_KEY);

        ActionMap actionMap = getActionMap();
        actionMap.put(ENTER_KEY, searchAction);
        actionMap.put(CANCEL_KEY, hideFindPanelAction);
        
        //TODO: need to change mapping for search action on "Enter", this fires a search if
        //enter is hit in iether the find box, or the replace box

    }
    
    /**
     * 
     */
    private void createReplacePanel()
    {
        replaceField.setColumns(textFieldLength);
        replaceField.addKeyListener(new FindReplaceTextFieldKeyAdapter());
        
        replaceButton = new JButton(getResourceString("REPLACE"));
        //replaceButton.setEnabled(false);
        //replaceButton.setMargin(new Insets(0, 0, 0, 0));
        replaceButton.addActionListener(replaceAction);
        
  
        replaceAllButton = new JButton(getResourceString("REPLACEALL"));
        //replaceAllButton.setEnabled(false);
        //replaceAllButton.setMargin(new Insets(0, 0, 0, 0));
        replaceAllButton.addActionListener(replaceAction);
        
        //replaceButton.setMnemonic(KeyEvent.VK_N);
        //replaceButton.addActionListener(searchAction);
        //JComponent[] itemSample = { new JMenuItem("Replace"), new JMenuItem("Replace All") };
        //memoryReplaceButton = new MemoryDropDownButton("Replace", IconManager.getIcon("DropDownArrow"),
        //                1, java.util.Arrays.asList(itemSample));
        //memoryReplaceButton.setOverrideBorder(true, memoryReplaceButton.raisedBorder);
        //memoryReplaceButton.setEnabled(false);
        
        builder.add(replaceField,          cc.xy(5,3));
        builder.add(replaceButton,         cc.xy(7,3));
        builder.add(replaceAllButton,      cc.xy(9,3));
        
        statusInfo = new JLabel("");
        builder.add(statusInfo,          cc.xywh(11,3, 4,1));
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
        cancelButton = new JButton(hideFindPanelAction);
        cancelButton.setIcon(IconManager.getIcon("Close"));
        cancelButton.setMargin(new Insets(0, 0, 0, 0));

        findLabel = new JLabel(getResourceString("FIND") + ": ", SwingConstants.RIGHT);

        nextButton = new JButton(getResourceString("NEXT"));//, new ImageIcon(Specify.class.getResource("images/down.png")));
        nextButton.setEnabled(false);
        //nextButton.setMargin(new Insets(0, 0, 0, 0));
        nextButton.setMnemonic(KeyEvent.VK_N);
        nextButton.addActionListener(searchAction);

        previousButton = new JButton(getResourceString("PREVIOUS"));//, new ImageIcon(Specify.class.getResource("images/up.png")));
        previousButton.setEnabled(false);
        //previousButton.setMargin(new Insets(0, 0, 0, 0));
        previousButton.setMnemonic(KeyEvent.VK_P);
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

        matchCaseButton = new JCheckBox(getResourceString("MATCHCASE"));
        matchCaseButton.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
            }
        });

        wrapSearchButton = new JCheckBox(getResourceString("WRAP"));
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
        
       // statusInfo = new JLabel("");
       // builder.add(statusInfo,          cc.xy(15,1));
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {

    }

    /**
     * @return the state fo the "match case" option
     */
    private boolean getMatchCaseFlag()
    {
        return matchCaseButton.isSelected();
    }

    /**
     * @return the state of the "wrap search" option
     */
    private boolean getWrapSearchFlag()
    {
        return wrapSearchButton.isSelected();
    }

    /**
     * Setst the status label to alert the user that a word has not been found.
     */
    private void setStatusLabelWithFailedFind()
    {
        log.info("NOT FOUND - Findvalue[" + findField.getText() + "] displaying statusInfo to the user");
        statusInfo.setHorizontalTextPosition(JLabel.RIGHT);
        statusInfo.setIcon(IconManager.getIcon("Error", IconManager.IconSize.Std16));
        statusInfo.setText(getResourceString("PHRASENOTFOUND"));
    }

    /**
     *  Setst the status label to alert the user that the end of the table is 
     *  has been reached during the search.
     *      
     */
    private void setStatusLabelEndReached()
    {
        log.info("NOT FOUND - Findvalue[" + findField.getText() + "] displaying statusInfo to the user");
        statusInfo.setHorizontalTextPosition(JLabel.RIGHT);
        statusInfo.setIcon(IconManager.getIcon("ValidationValid", IconManager.IconSize.Std16));
        statusInfo.setText(getResourceString("ENDOFTABLE"));
    }
    
    /**
     * Clears the label that tells the user the status of the search/replace
     */
    private void clearStatusLabel()
    {
        //log.debug("clearing status lable");
        statusInfo.setHorizontalTextPosition(JLabel.RIGHT);
        statusInfo.setIcon(null);
        statusInfo.setText("");
    }    
    
    /**
     * replaces the contents of cell where part of the cell contains the string found with the string
     * that is provided for replacement.
     */
    private void replace()
    {
        log.debug("replace called");
        if (!isTableValid())
        {
            return;
        }
        stopTableEditing();        
        int selectedCol = table.getSelectedColumn();
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1 || selectedCol ==-1)
        {
            setStatusLabelWithFailedFind(); 
            return;
        }

        Object o = table.getValueAt(selectedRow, selectedCol);
        if (!(o instanceof String))
        {
            if (!(o instanceof Boolean) && !(o instanceof Integer))
            {
                log.info("The value at row=[" + selectedRow + "] col=[" + selectedCol+ "] is not a String and cannot be replaced");
                return;
            }

        }
     
        String myStrToReplaceValueIn = o.toString();
        String myReplaceValue = replaceField.getText();      
        String myNewStr = "";
        String myFindValue = findField.getText();

        TableSearcher as = new TableSearcher();
        TableSearcherCell cell = as.cellContains(myFindValue, table, table.getModel(), selectedRow, selectedCol,getMatchCaseFlag());
        boolean found = cell.isFound();
        if (found)
        {
            if (getMatchCaseFlag())
            {
                 myNewStr = Pattern.compile(myFindValue).matcher(myStrToReplaceValueIn).replaceAll(myReplaceValue);
            }
            else
            {
                log.debug("Need to implement case insensitivity");
                myNewStr = Pattern.compile(myFindValue, Pattern.CASE_INSENSITIVE).matcher(myStrToReplaceValueIn).replaceAll(myReplaceValue);
            }  
            
            table.setValueAt(myNewStr, selectedRow, selectedCol);   
            
            ListSelectionModel rsm = table.getSelectionModel();
            ListSelectionModel csm = table.getColumnModel().getSelectionModel();
            rsm.setSelectionInterval(selectedRow, selectedRow);
            csm.setSelectionInterval(selectedCol, selectedCol);
        }    
        else
        {
            setStatusLabelWithFailedFind(); 
        }
        setCheckAndSetWrapOption();
        find();
    }
    
    /**
     * replaces all of the values where a cell contains the string
     * @return
     */
    private void replaceAll()
    {
        log.debug("replaceAll() called");
        if (!isTableValid()) 
        { 
            return ; 
        }        
        stopTableEditing();
        
        String str = findField.getText();
        log.debug("replaceAll() - FindValue[" + str + "] SearchingDown[" + isSearchDown + "]");
        log.debug("tableSize - rowCount: " + table.getRowCount() + " columnCount: " + table.getColumnCount());

        int curRow = 0;
        int curCol = 0;

        isSearchDown = true;

        TableSearcher as = new TableSearcher();
        TableSearcherCell cell = as.tableContains(str, table, table.getModel(), curRow, curCol,getMatchCaseFlag(), isSearchDown, getWrapSearchFlag());
        boolean found = cell.isFound();  
        if (!found)
        {
          log.debug("repalceall() found nothing");
          if (isSearchDown)
          {
              isFinishedSearchingDown = true;
              if (!wrapSearchButton.isSelected())
              {
                  nextButton.setEnabled(false);
                  previousButton.setEnabled(true);                 
              }
              setStatusLabelWithFailedFind();
          } 
          else
          {
              isFinishedSearchingUp = true;
              if (!wrapSearchButton.isSelected())
              {
                  previousButton.setEnabled(false);
                  log.debug("seeting next button to true");
                  nextButton.setEnabled(true);                
              }
              setStatusLabelWithFailedFind();
          }      
        }
        while (found)
        {
            log.debug("repalceall() found value");
            curRow = cell.getRow();
            curCol = cell.getCol();
            ListSelectionModel rsm = table.getSelectionModel();
            ListSelectionModel csm = table.getColumnModel().getSelectionModel();
            rsm.setSelectionInterval(curRow, curRow);
            csm.setSelectionInterval(curCol, curCol);
            replace();
            
            if(curCol >= (table.getColumnModel().getColumnCount()-1)) 
            {
                curRow++;
                curCol = -1;
            }
            curCol++;
//            if((curRow==(table.getRowCount()-1))&&(curCol==(table.getColumnCount()-1)))
//            {
//              break;
//            }
            log.debug("replace all");
            //as = new TableSearcher();
            cell = as.tableContains(str, table, table.getModel(), curRow, curCol,getMatchCaseFlag(), isSearchDown, false);
            found = cell.isFound();

        }  
        nextButton.setEnabled(false);
        previousButton.setEnabled(false);
        replaceButton.setEnabled(false);
        replaceAllButton.setEnabled(false);
        setStatusLabelEndReached();
    }  
        
    
    /**
     * @return boolean false if the table is null
     */
    private boolean isTableValid()
    {
        if (table == null)
        {
            log.error("The search table is null!");
            setStatusLabelWithFailedFind();
            return false;
        }
        return true;
    }

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
     * performs a search/find on a particular string.
     * @return
     */
    private void find()
    {   
        if (!isTableValid())
        {
            return ;
        }        
        stopTableEditing();
        
        String str = findField.getText();
        log.debug("find() - FindValue[" + str + "] SearchingDown[" + isSearchDown+ "]");
        
        int curRow = table.getSelectedRow();
        int curCol = table.getSelectedColumn();
        log.debug("find() - curRow[" + curRow + "] curCol[" + curCol+ "]");

        if (isSearchDown){
            if (curRow == -1) 
            {
                curRow++;
            }
            if (curCol >= (table.getColumnModel().getColumnCount()-1)) 
            {
                curRow++;
                curCol = -1;
            }
            if(getWrapSearchFlag() && curRow >= table.getRowCount())
            {
                curRow = 0;
            }
            curCol++;
        }
        
        //is previous clicked, reverse direction
        else
        {
            if (curRow == -1)
            {
                curRow = table.getRowCount()-1;
            }
            if (curCol <= 0 ) 
            {
                curRow--;
                curCol = table.getColumnModel().getColumnCount();
            }
            curCol--;     
        }
        
        
        TableSearcher as = new TableSearcher();
        TableSearcherCell cell = as.tableContains(str, table, table.getModel(), curRow, curCol, getMatchCaseFlag(),isSearchDown, getWrapSearchFlag() );
        boolean found = cell.isFound();
        
        if (found)
        {
            curRow = cell.getRow();
            curCol = cell.getCol();
            ListSelectionModel rsm = table.getSelectionModel();
            ListSelectionModel csm = table.getColumnModel().getSelectionModel();
            rsm.setSelectionInterval(curRow, curRow);
            csm.setSelectionInterval(curCol, curCol);
            
            int ar = table.getSelectionModel().getAnchorSelectionIndex();
            int ac = table.getColumnModel().getSelectionModel().getAnchorSelectionIndex();

            Rectangle rect = table.getCellRect(ar, ac, false);
            if (rect!=null && table.getAutoscrolls()) 
            {
                table.scrollRectToVisible(rect);
            }

            if (isSearchDown)
            {
                previousButton.setEnabled(true);
            }            
            else
            {
                log.debug("setting next button true");
                nextButton.setEnabled(true);
            }
            clearStatusLabel();
        }
        else
        {
            log.debug("find() found nothing");
            if (isSearchDown)
            {
                isFinishedSearchingDown = true;
                if (!wrapSearchButton.isSelected())
                {
                    log.debug("=======" + isSearchDown);
                    nextButton.setEnabled(false);
                    previousButton.setEnabled(true);
                    
                }
                setStatusLabelWithFailedFind();
            }
            else
            {
                isFinishedSearchingUp = true;
                if (!wrapSearchButton.isSelected())
                {
                    previousButton.setEnabled(false);
                    log.debug("seeting next button to true");
                    nextButton.setEnabled(true);
                    
                }
                setStatusLabelWithFailedFind();
            }
        }
    }
    
    /**
     * checks to see if the "wrap search" option is enabled, sets teh button state and clears the label.
     */
    private void setCheckAndSetWrapOption()
    {
        if (wrapSearchButton.isSelected())
        {
            isFinishedSearchingDown = false;
            isFinishedSearchingUp = false;
            log.debug("seeting next button to true");
            nextButton.setEnabled(true);     
            previousButton.setEnabled(true);
            clearStatusLabel();
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
            log.debug("ReplaceAction.actionPerformed");
            log.debug("closing the FindReplace Dialog - either the close \"X\" button was pressed or the esc button was pressed");
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
            log.debug("ReplaceAction.actionPerformed");
            Object source = evt.getSource();
            if (source == replaceButton)
            {
                isSearchDown = true;
                //moved these two from after the if/else statement
                setCheckAndSetWrapOption();
                replace();
                UsageTracker.incrUsageCount("WB.ReplaceButton");
            }
            else if (source == replaceAllButton)
            {
                replaceAll();
                UsageTracker.incrUsageCount("WB.ReplaceAllButton");
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
            log.debug("SearchAction.actionPerformed" + evt.getActionCommand().toString());
            isSearchDown = true;
            Object source = evt.getSource();
            if (source == nextButton)
            {
                isSearchDown = true;
            } else if (source == previousButton)
            {
                isSearchDown = false;
            } 

            UsageTracker.incrUsageCount("WB.FindButton");
            setCheckAndSetWrapOption();
            find(); 
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
            super("Find");
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
        public void keyReleased(KeyEvent ke)
        {            
            // make sure the user has entered a text string in teh find box before enabling find buttons
            boolean findTextState = (findField.getText().length() > 0);
            nextButton.setEnabled(findTextState);

            if (table.getSelectedRow() > 0 || table.getSelectedColumn() > 0)
            {
                previousButton.setEnabled(findTextState);
            }
            // make sure the user has entered a text string in teh searck textfield before enabling replace buttons
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
                clearStatusLabel();
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

    /**
     * @return the searchAction
     */
    public SearchAction getSearchAction()
    {
        return searchAction;
    }

    /**
     * @param searchAction the searchAction to set
     */
    public void setSearchAction(SearchAction searchAction)
    {
        this.searchAction = searchAction;
    }

    /**
     * @return the replaceAction
     */
    public ReplaceAction getReplaceAction()
    {
        return replaceAction;
    }

    /**
     * @param replaceAction the replaceAction to set
     */
    public void setReplaceAction(ReplaceAction replaceAction)
    {
        this.replaceAction = replaceAction;
    }
}
