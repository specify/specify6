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
import javax.swing.ImageIcon;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.Specify;
import edu.ku.brc.ui.tmanfe.SpreadSheet;
/**
 * @author megkumin
 * 
 * @code_status Alpha
 * 
 * Created Date: Mar 1, 2007
 * 
 */
@SuppressWarnings("serial")
public class SearchReplacePanel extends JPanel
{
    private SpreadSheet table;
    private Pattern                  pattern;
    //private Searchable               searchable;
    @SuppressWarnings("unused")
    private boolean                  isStartOfSearch         = true;
    private boolean                  isSearchDown            = true;
    @SuppressWarnings("unused")
    private boolean                  isFinishedSearchingDown = false;
    @SuppressWarnings("unused")
    private boolean                  isFinishedSearchingUp   = true;
    private int                      lastIndex               = -1;

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
    FormLayout                       formLayout              = new FormLayout("p,1px,p,1px,p,1px,p,1px,p,4px,p,1px," +
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
    
    public void handleTableSelections()
    {
        ListSelectionListener lsl = new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                log.debug("table selectoin");
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
        	log.debug("showing Find/replace panel");
            this.setVisible(true);
            findField.requestFocusInWindow();
        }
        return this;
    }

    /**
     * 
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
    }
    
    /**
     * 
     */
    private void createReplacePanel()
    {
        replaceField.setColumns(textFieldLength);
        replaceField.addKeyListener(new FindReplaceTextFieldKeyAdapter());
        
        replaceButton = new JButton(getResourceString("REPLACE"));
        replaceButton.setEnabled(false);
        replaceButton.setMargin(new Insets(0, 0, 0, 0));
        replaceButton.addActionListener(replaceAction);
        
  
        replaceAllButton = new JButton(getResourceString("REPLACEALL"));
        replaceAllButton.setEnabled(false);
        replaceAllButton.setMargin(new Insets(0, 0, 0, 0));
        replaceAllButton.addActionListener(replaceAction);
        
        //replaceButton.setMnemonic(KeyEvent.VK_N);
        //replaceButton.addActionListener(searchAction);
		//JComponent[] itemSample = { new JMenuItem("Replace"), new JMenuItem("Replace All") };
		//memoryReplaceButton = new MemoryDropDownButton("Replace", IconManager.getIcon("DropDownArrow"),
		//                1, java.util.Arrays.asList(itemSample));
		//memoryReplaceButton.setOverrideBorder(true, memoryReplaceButton.raisedBorder);
		//memoryReplaceButton.setEnabled(false);
        
        builder.add(replaceField,          cc.xy(5,3));
        builder.add(replaceButton,          cc.xy(7,3));
        builder.add(replaceAllButton,          cc.xy(9,3));
        
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
    }
    
    /**
     * Creates the panel that displays the close button, the search field, the next button,
     * the previous button, the match case checkbox and the wrap search checkbox
     */
    private void createFindPanel()
    {
        cancelButton = new JButton(hideFindPanelAction);
        cancelButton.setIcon(new ImageIcon(Specify.class.getResource("images/close.gif")));
        cancelButton.setMargin(new Insets(0, 0, 0, 0));

        JLabel findLabel = new JLabel(getResourceString("FIND") + ": ");

        nextButton = new JButton(getResourceString("NEXT"));//, new ImageIcon(Specify.class.getResource("images/down.png")));
        nextButton.setEnabled(false);
        nextButton.setMargin(new Insets(0, 0, 0, 0));
        nextButton.setMnemonic(KeyEvent.VK_N);
        nextButton.addActionListener(searchAction);

        previousButton = new JButton(getResourceString("PREVIOUS"));//, new ImageIcon(Specify.class.getResource("images/up.png")));
        previousButton.setEnabled(false);
        previousButton.setMargin(new Insets(0, 0, 0, 0));
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
                pattern = null;
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
               
        builder.add(cancelButton,          cc.xy(1,1));
        builder.add(findLabel,          cc.xy(3,1));
        builder.add(findField,          cc.xy(5,1));
        builder.add(nextButton,          cc.xy(7,1));
        builder.add(previousButton,          cc.xy(9,1));
        builder.add(matchCaseButton,          cc.xy(11,1));
        builder.add(wrapSearchButton,          cc.xy(13,1));
       // statusInfo = new JLabel("");
       // builder.add(statusInfo,          cc.xy(15,1));
    }

    /**
     * Creates a table for testing. 
     */
    private static SearchableJXTable createTestTableFromJTable()
    {
        Object[] columnNames = { "First Name", "Last Name", "Sport", "# of Years", "Vegetarian" };

        Object[][] data = {
                { "Mary bary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) },
                { "Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
                { "Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) },
                { "Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
                { "Philip", "Milne", "Pool",  new Integer(10), new Boolean(false) } };

        JTable myJTable = new JTable(data, columnNames);
        log.debug("creating searchablejxtable");
        final SearchableJXTable mytable = new SearchableJXTable(myJTable.getModel());
        return mytable;
    }
    

    /**
     * @param args
     */
    public static void main(String[] args)
    {
//    	log.debug("main");
//        final JDialog dialog = new JDialog();
//        dialog.setLayout(new FlowLayout());
//        dialog.addWindowListener(new WindowAdapter()
//        {
//            public void windowClosing(WindowEvent we)
//            {
//                dialog.dispose();
//            }
//        });
//        log.debug("Creating testtable");
//        SearchableJXTable t = createTestTableFromJTable();
//        
//        t.setColumnSelectionAllowed(true);
//        t.setRowSelectionAllowed(true);
//        t.setCellSelectionEnabled(true);
//        dialog.getContentPane().add( new JScrollPane(t));
//        dialog.getContentPane().add(t.getFindReplacePanel());//getMyPanel());
//        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//        dialog.setVisible(true);
//        dialog.setSize(900, 900);
//        UIHelper.centerAndShow(dialog);
    }

    /**
     * @return
     */
    private boolean getMatchCaseFlag()
    {
        boolean isMatchCase = matchCaseButton.isSelected();
        return isMatchCase;
    }

    /**
     * @return
     */
    private boolean getWrapSearchFlag()
    {
        boolean isWrapSearch = wrapSearchButton.isSelected();
        return isWrapSearch;
    }
    /**
     * @return
     */
    private Pattern getSearchablePattern(String str)
    {
        if (str.length() == 0) { return null; }
        
        if (pattern == null || !pattern.pattern().equals(str))
        {
            log.debug("getSearchablePattern - compiling a Pattern for string:" + str);
            //force case insensitivity is match case flag is not set
            pattern = Pattern.compile(str, getMatchCaseFlag() ? 0: Pattern.CASE_INSENSITIVE);
            // Start from the beginning.
            lastIndex = -1;
        }
        return pattern;
    }

    /**
     * 
     */
    private void setStatusLabelWithFailedFind()
    {
        log.info("NOT FOUND - Findvalue[" + findField.getText() + "] displaying statusInfo to the user");
    	statusInfo.setHorizontalTextPosition(JLabel.RIGHT);
        statusInfo.setIcon(IconManager.getIcon("Error", IconManager.IconSize.Std16));
        statusInfo.setText(getResourceString("PHRASENOTFOUND"));
    }

    private void setStatusLabelEndReached()
    {
        log.info("NOT FOUND - Findvalue[" + findField.getText() + "] displaying statusInfo to the user");
        statusInfo.setHorizontalTextPosition(JLabel.RIGHT);
        statusInfo.setIcon(IconManager.getIcon("ValidationValid", IconManager.IconSize.Std16));
        statusInfo.setText(getResourceString("ENDOFTABLE"));
    }
    /**
     * 
     */
    private void clearStatusLabel()
    {
        log.debug("clearing status lable");
        statusInfo.setHorizontalTextPosition(JLabel.RIGHT);
        statusInfo.setIcon(null);
        statusInfo.setText("");
    }
    
    
    /**
     * 
     */
    private void replace()
    {
        log.debug("replace called");
        if (!isTableValid())
        {
            setStatusLabelWithFailedFind(); 
            return;
        }

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

        ArraySearcher as = new ArraySearcher();
        ASearchableCell cell = as.cellContains(myFindValue, table, table.getModel(), selectedRow, selectedCol,getMatchCaseFlag());
        boolean found = cell.isFound();
        if (found)
        {
            if (getMatchCaseFlag())
            {
                 myNewStr = Pattern.compile(myFindValue).matcher(myStrToReplaceValueIn).replaceAll(myReplaceValue);
            }
            else
            {
                log.error("Need to implement case insensitivity");
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
     * @return
     */
    private boolean replaceAll()
    {
        log.debug("replaceAll() called");
        if (!isTableValid()) 
        { 
            setStatusLabelWithFailedFind();
            return false; 
        }
        
        String str = findField.getText();
        log.debug("find() - FindValue[" + str + "] SearchingDown[" + !isSearchDown + "]");
        log.debug("tableSize - rowCount: " + table.getRowCount() + " columnCount: " + table.getColumnCount());

        int curRow = 0;
        int curCol = 0;


        ArraySearcher as = new ArraySearcher();
        ASearchableCell cell = as.tableContains(str, table, table.getModel(), curRow, curCol,getMatchCaseFlag(), isSearchDown, getWrapSearchFlag());
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
                  setStatusLabelWithFailedFind();

              }
          } else
          {
              isFinishedSearchingUp = true;
              if (!wrapSearchButton.isSelected())
              {
                  previousButton.setEnabled(false);
                  log.debug("seeting next button to true");
                  nextButton.setEnabled(true);
                  setStatusLabelWithFailedFind();

              }
          }
          return false;         
        }
        while (found)
        {
            curRow = cell.getRow();
            curCol = cell.getCol();
            ListSelectionModel rsm = table.getSelectionModel();
            ListSelectionModel csm = table.getColumnModel().getSelectionModel();
            rsm.setSelectionInterval(curRow, curRow);
            csm.setSelectionInterval(curCol, curCol);
            replace();
            
            if(curCol >= table.getColumnModel().getColumnCount()) 
            {
                curRow++;
                curCol = -1;
            }
            curCol++;
            cell = as.tableContains(str, table, table.getModel(), curRow, curCol,getMatchCaseFlag(), isSearchDown, getWrapSearchFlag());
            found = cell.isFound();
//            if (found)
//            {
//                curRow = cell.getRow();
//                curCol = cell.getCol();
//                ListSelectionModel rsm = table.getSelectionModel();
//                ListSelectionModel csm = table.getColumnModel().getSelectionModel();
//                rsm.setSelectionInterval(curRow, curRow);
//                csm.setSelectionInterval(curCol, curCol);
//                replace();
//            }
//            else
//            {
//                log.debug("repalceallNOT FOUND!!!!!!");
//            }
        }  
      nextButton.setEnabled(false);
      previousButton.setEnabled(false);
      replaceButton.setEnabled(false);
      replaceAllButton.setEnabled(false);
        setStatusLabelEndReached();
        return true;
    }  
        
        
//        if (!found)
//        {
//            log.debug("repalceall() found nothing");
//            if (isSearchDown)
//            {
//                isFinishedSearchingDown = true;
//                if (!wrapSearchButton.isSelected())
//                {
//                    nextButton.setEnabled(false);
//                    previousButton.setEnabled(true);
//                    setLabelForFailedFind();
//
//                }
//            } else
//            {
//                isFinishedSearchingUp = true;
//                if (!wrapSearchButton.isSelected())
//                {
//                    previousButton.setEnabled(false);
//                    nextButton.setEnabled(true);
//                    setLabelForFailedFind();
//
//                }
//            }
//            return false;
//        }
//        if (isSearchDown)
//        {
//            previousButton.setEnabled(true);
//        }            
//        else
//        {
//            nextButton.setEnabled(true);
//        }
//        clearLabelFromFailedFind();
//        return true;        
//		if (!isTableValid())return false;
//    	
//        String str = findField.getText();
//        log.debug("replaceAll() - FindValue[" + str + "] SearchingDown[" + !isSearchDown+ "]");
//        lastIndex = searchable.search(getSearchablePattern(str), lastIndex, !isSearchDown);
//        
//        if (lastIndex>-1)found= true;
//        while(lastIndex!=-1) 
//        {
//        	replace();
//        	lastIndex = searchable.search(getSearchablePattern(str), lastIndex, !isSearchDown);
//        }
//        if (lastIndex == -1)
//        {
//            log.debug("replaceAll() found nothing");
//            if (isSearchDown)
//            {
//                isFinishedSearchingDown = true;
//                if (!wrapSearchButton.isSelected())
//                {
//                    nextButton.setEnabled(false);
//                    previousButton.setEnabled(false);
//                    if (!found)setLabelForFailedFind();
//
//                }
//            } else
//            {
//                isFinishedSearchingUp = true;
//                if (!wrapSearchButton.isSelected())
//                {
//                    previousButton.setEnabled(false);
//                    nextButton.setEnabled(false);
//                    if (!found)setLabelForFailedFind();
//
//                }
//            }
//            return false;
//        }
//        if (isSearchDown)
//        {
//            previousButton.setEnabled(true);
//        }            
//        else
//        {
//            nextButton.setEnabled(true);
//        }
//        clearLabelFromFailedFind();
//        
//        return true;
//    }
    
    /**
     * @return
     */
    private boolean isTableValid()
    {
		if (table == null)
		{
			log.error("The search table is null!");
			return false;
		}
		return true;
    }

    /**
     * 
     */
    private boolean find()
    { 	
        if (!isTableValid())
        {
            setStatusLabelWithFailedFind();
            return false;
        }
        
        String str = findField.getText();
        log.debug("find() - FindValue[" + str + "] SearchingDown[" + !isSearchDown+ "]");
        
        int curRow = table.getSelectedRow();
        int curCol = table.getSelectedColumn();

        if (isSearchDown){
            if (curRow == -1) curRow++;
            if (curCol >= (table.getColumnModel().getColumnCount()-1)) 
            {
                curRow++;
                curCol = -1;
            }
            curCol++;
        }
        //is previous clicked, reverse direction
        else
        {
            if (curRow == -1) curRow++;
            if (curCol <= 0 ) 
            {
                curRow--;
                curCol = table.getColumnModel().getColumnCount()-1;
            }
            curCol--;     
        }
        ArraySearcher as = new ArraySearcher();
        ASearchableCell cell = as.tableContains(str, table, table.getModel(), curRow, curCol, getMatchCaseFlag(),isSearchDown, getWrapSearchFlag() );
        boolean found = cell.isFound();
        
        if (found)
        {
            curRow = cell.getRow();
            curCol = cell.getCol();
            ListSelectionModel rsm = table.getSelectionModel();
            ListSelectionModel csm = table.getColumnModel().getSelectionModel();
            rsm.setSelectionInterval(curRow, curRow);
            csm.setSelectionInterval(curCol, curCol);

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
                    setStatusLabelWithFailedFind();
                }
            }
            else
            {
                isFinishedSearchingUp = true;
                if (!wrapSearchButton.isSelected())
                {
                    previousButton.setEnabled(false);
                    log.debug("seeting next button to true");
                    nextButton.setEnabled(true);
                    setStatusLabelWithFailedFind();
                }
            }
            return false;
        }
        return true;
    }
    
    /**
     * 
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
     * @author megkumin
     *
     * @code_status Alpha
     *
     * Created Date: Mar 15, 2007
     *
     */
    public class HideFindPanelAction extends AbstractAction
    {
        /**
         * 
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
         * 
         */
        public void hide()
        {
            showFindAndReplacePanel(false);
        }
    }

    /**
     * @author megkumin
     *
     * @code_status Alpha
     *
     * Created Date: Mar 15, 2007
     *
     */
    private class ReplaceAction extends AbstractAction
	{
		/**
		 * 
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
			}
			else if (source == replaceAllButton)
			{
				replaceAll();
			}
		}
	}
    
    
    /**
	 * @author megkumin
	 * 
	 * @code_status Alpha
	 * 
	 * Created Date: Mar 15, 2007
	 * 
	 */
    private class SearchAction extends AbstractAction
    {
        /**
         * 
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
            	log.debug("Find Next button selected - searching downwards");
                isSearchDown = true;
            } else if (source == previousButton)
            {
            	log.debug("Find Previous button selected - searching upwards");
                isSearchDown = false;
            } 


            setCheckAndSetWrapOption();
            find();
        }
    }

    /**
     * @author megkumin
     *
     * @code_status Alpha
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
     * @author megkumin
     *
     * @code_status Alpha
     *
     * Created Date: Mar 15, 2007
     *
     */
    private class FindReplaceTextFieldKeyAdapter extends KeyAdapter
    {
        /**
         * 
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
            
            boolean replaceTextState = false;
            // make sure the user has entered a text string in teh find box before enabling find
            // buttons
            boolean findTextState = (findField.getText().length() > 0);
            if (replaceField != null)
            {
                replaceTextState = (replaceField.getText().length() > 0);
            }
            nextButton.setEnabled(findTextState);

            if (table.getSelectedRow() > 0 || table.getSelectedColumn() > 0)
            {
                previousButton.setEnabled(findTextState);
            }
            // memoryReplaceButton.setEnabled(findTextState && replaceTextState);
            // make sure the user has entered a text string in teh replace textfield before enabling
            // replace buttons
            if (replaceButton != null)
            {
                replaceButton.setEnabled(findTextState && replaceTextState);
            }
            if (replaceAllButton != null)
            {
                replaceAllButton.setEnabled(findTextState && replaceTextState);
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
