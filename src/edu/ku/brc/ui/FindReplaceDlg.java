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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.specify.Specify;

/**
 * @author megkumin
 * 
 * @code_status Alpha
 * 
 * Created Date: Mar 1, 2007
 * 
 */
public class FindReplaceDlg extends JDialog implements ActionListener, ItemListener
{
    private JCheckBox                                   matchCaseButton;
    private int                                         textFieldLength = 15;
    final JTextField findField = new JTextField();
    final JTextField replaceField = new JTextField();
    /**
     * boolean foundOnce is used by 2 methods returned true if the search word is found
     */
    private boolean                                     foundOnce;

    /**
     * boolean isReplace is used to know if the frame is a replace function or a find function
     */
    private boolean                                     isReplace;

    /**
     * textfields hold String user input
     */
    private JTextField                                  textfield, replaceText;

    /**
     * JCheckBox cbCase, if true the search is case sensitive JCheckBox cbWhole, if true the search
     * is for the whole word only
     * 
     * @see #caseNotSelected()
     * @see #wholeWordIsSelected()
     */
    private JCheckBox                                   cbCase, cbWhole;

    /**
     * (boolean) JRadioButton down is the search from the start of the text toward the bottom
     * 
     * @see #isSearchDown()
     */
    private JRadioButton                                down;

    /**
     * statusInfo is for user messages
     */
    private JLabel                                      statusInfo;

    /**
     * sharedInstance()
     */
    private Frame                                       newOwner;
    // End of class variables
    private JEditorPane                                 editorPane      = new JEditorPane();
    private JTable                                 table      = new JTable();
    /**
     * Constructor is a sharedInstance() of the MainEditor.class.
     * 
     * @param java.awt.Frame
     * @param boolean
     *            if true the class is the Replace frame /else the Find frame
     */
    public FindReplaceDlg()
    {
        // super(owner);
        this.isReplace = false;
        // newOwner = owner;
        // if(isReplace) setTitle(" Find And Replace:");
        // else setTitle(" Find:");
        JPanel north = new JPanel();
        JPanel center = new JPanel();
        JPanel south = new JPanel();
        if (isReplace)
            setReplacePanel(north);
        else setFindPanel(north);
        setPanelCenter(center);
        statusInfo = new JLabel("Status info:");
        south.add(statusInfo);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent we)
            {
                dispose();
            }
        });
        getContentPane().add(north, BorderLayout.NORTH);
        getContentPane().add(center, BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        // int x = (owner.getWidth()*3/5) - (getWidth()/2);
        // int y = (owner.getHeight()/2) - (getHeight()/2);
        // setLocation(x,y); // 3/5ths accross + centred
        setVisible(true);
        editorPane.setText("meg");
        editorPane.setSelectionStart(0);

    }

    /**
     * Constructor is a sharedInstance() of the MainEditor.class.
     * 
     * @param java.awt.Frame
     * @param boolean
     *            if true the class is the Replace frame /else the Find frame
     */
    public FindReplaceDlg(int i)
    {
        JPanel north = new JPanel();
        setFindAndReplacePanel(north);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent we)
            {
                dispose();
            }
        });
        getContentPane().add(north, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setVisible(true);
        editorPane.setText("meg");
        editorPane.setSelectionStart(0);
    }

    private void slkdjf(JTable table)
    {
        DefaultCellEditor df = (DefaultCellEditor)table.getColumnModel().getColumn(0).getCellEditor();
        JTextField txf =(JTextField) df.getComponent();
        System.out.println(txf);
        txf.select(0,1);
    }
    private JTable createTestTable()
    {
        String[] columnNames = { "First Name", "Last Name", "Sport", "# of Years", "Vegetarian" };

        Object[][] data = {
                { "Mary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) },
                { "Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
                { "Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) },
                { "Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
                { "Philip", "Milne", "Pool", new Integer(10), new Boolean(false) } };

        final JTable table = new JTable(data, columnNames);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                //printDebugData(table);
                int col = table.getSelectedColumn();
                int row = table.getSelectedRow();
                System.out.println(table.getValueAt(row, col));
            }
        });
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent lse) {
        if (lse.getValueIsAdjusting()) return;
        int row = table.getSelectedRow(), col = table.getSelectedColumn();
        System.out.println("Value = "+table.getValueAt(row, col));
            }
          });
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        //table.setSelectionMode(arg0)
        return table;
    }
    
    private void printDebugData(JTable table) {
        int numRows = table.getRowCount();
        int numCols = table.getColumnCount();
        javax.swing.table.TableModel model = table.getModel();

        System.out.println("Value of data: ");
        for (int i=0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j=0; j < numCols; j++) {
                System.out.print("  " + model.getValueAt(i, j));
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
    /**
     * setFindPanel is part of the main class constructor for the construction of a Find Frame This
     * north panel uses a default layout manager
     * 
     * @param north
     *            javax.swing.JPanel
     */
    private void setFindPanel(JPanel north)
    {
        final JButton nextButton = new JButton("Find Next");
        nextButton.setMnemonic('F');
        nextButton.addActionListener(this);
        nextButton.setEnabled(false);
        try
        {
            textfield = new JTextField(editorPane.getSelectedText(), 10);
        } catch (NoSuchFieldError nsf)
        {
            textfield = new JTextField(10);
        }
        textfield.addActionListener(this);
        textfield.addKeyListener(new KeyAdapter()
        {
            public void keyReleased(KeyEvent ke)
            {
                boolean state = (textfield.getDocument().getLength() > 0);
                nextButton.setEnabled(state);
                foundOnce = false;
            }
        });
        if (textfield.getText().length() > 0)
            nextButton.setEnabled(true);
        north.add(new JLabel("Find Word:"));
        north.add(textfield);
        north.add(nextButton);
    }

    /**
     * setFindPanel is part of the main class constructor for the construction of a Find Frame This
     * north panel uses a default layout manager
     * 
     * @param panel
     *            javax.swing.JPanel
     */
    private void setFindAndReplacePanel(JPanel panel)
    {
        JButton cancelButton = new JButton(new ImageIcon(Specify.class
                .getResource("images/close.gif")));
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("cancel button clicked");
            }
        });

        JLabel findLabel = new JLabel("Find: ");

        final JButton nextButton = new JButton("Next", new ImageIcon(Specify.class
                .getResource("images/down.png")));
        nextButton.setEnabled(false);
        nextButton.setMargin(new Insets(0, 0, 0, 0));
        nextButton.setMnemonic(KeyEvent.VK_N);

        final JButton previousButton = new JButton("Previous", new ImageIcon(Specify.class
                .getResource("images/up.png")));
        previousButton.setEnabled(false);
        previousButton.setMargin(new Insets(0, 0, 0, 0));
        nextButton.setMnemonic(KeyEvent.VK_P);

        JComponent[] itemSample = { new JMenuItem("Replace"), new JMenuItem("Replace All") };
        final MemoryDropDownButton replaceButton = new MemoryDropDownButton("Find Next...", null,
                1, java.util.Arrays.asList(itemSample));
        replaceButton.setOverrideBorder(true, replaceButton.raisedBorder);
        replaceButton.setEnabled(false);
        // replaceButton.setMargin(new Insets(0,0,0,0));

        
        findField.setColumns(textFieldLength);
        findField.addActionListener(this);

        
        replaceField.setColumns(textFieldLength);
        replaceField.addActionListener(this);

        findField.addKeyListener(new KeyAdapter()
        {
            public void keyReleased(KeyEvent ke)
            {
                System.out.println("findField keyreleased");
                boolean findState = (findField.getText().length() > 0);
                boolean replaceState = (replaceField.getText().length() > 0);
                nextButton.setEnabled(findState);
                previousButton.setEnabled(findState);
                replaceButton.setEnabled(findState && replaceState);
            }
        });

        replaceField.addKeyListener(new KeyAdapter()
        {
            public void keyReleased(KeyEvent ke)
            {
                System.out.println("replaceField keyreleased");
                boolean findState = (findField.getText().length() > 0);
                boolean replaceState = (replaceField.getText().length() > 0);
                replaceButton.setEnabled(findState && replaceState);
            }
        });

        matchCaseButton = new JCheckBox("Match case");
        matchCaseButton.setMnemonic(KeyEvent.VK_C);
        matchCaseButton.addItemListener(this);

        panel.add(cancelButton);
        panel.add(findLabel);
        panel.add(findField);
        panel.add(nextButton);
        panel.add(previousButton);
        panel.add(replaceField);
        panel.add(replaceButton);
        panel.add(matchCaseButton);
        statusInfo = new JLabel("Status info:");
        panel.add(statusInfo);
        panel.add(createTestTable());
    }

    /**
     * setReplacePanel is part of the main class constructor for the construction of a Replace
     * Frame. This north panel uses a GridBagLayout manager
     * 
     * @param north
     *            javax.swing.JPanel
     */
    private void setReplacePanel(JPanel north)
    {
        GridBagLayout grid = new GridBagLayout();
        north.setLayout(grid);
        GridBagConstraints con = new GridBagConstraints();
        con.fill = GridBagConstraints.HORIZONTAL;
        JLabel labUpper = new JLabel("Next:");
        JLabel labLower = new JLabel("Replace: ");
        final JButton nextButton = new JButton("Replace");
        nextButton.setMnemonic('N');
        nextButton.addActionListener(this);
        nextButton.setEnabled(false);
        final JButton replaceButton = new JButton("Replace All");
        replaceButton.setMnemonic('R');
        replaceButton.addActionListener(this);
        replaceButton.setEnabled(false);
        textfield = new JTextField(editorPane.getSelectedText(), 12);
        replaceText = new JTextField(12);
        replaceText.addActionListener(this);
        replaceText.addKeyListener(new KeyAdapter()
        {
            public void keyReleased(KeyEvent ke)
            {
                boolean state = (replaceText.getDocument().getLength() > 0);
                nextButton.setEnabled(state);
                replaceButton.setEnabled(state);
                foundOnce = false;
            }
        });
        con.gridx = 0;
        con.gridy = 0;
        grid.setConstraints(labUpper, con);
        north.add(labUpper);
        con.gridx = 1;
        con.gridy = 0;
        grid.setConstraints(textfield, con);
        north.add(textfield);
        con.gridx = 2;
        con.gridy = 0;
        grid.setConstraints(nextButton, con);
        north.add(nextButton);
        con.gridx = 0;
        con.gridy = 1;
        grid.setConstraints(labLower, con);
        north.add(labLower);
        con.gridx = 1;
        con.gridy = 1;
        grid.setConstraints(replaceText, con);
        north.add(replaceText);
        con.gridx = 2;
        con.gridy = 1;
        grid.setConstraints(replaceButton, con);
        north.add(replaceButton);
    }

    /**
     * setPanelCenter is part of the main class constructor the construction is the same for both
     * Replace Frame or Find Frame. This center panel uses a simple GridLayout manager
     * 
     * @param center
     *            javax.swing.JPanel
     */
    private void setPanelCenter(JPanel center)
    {
        JPanel east = new JPanel();
        JPanel west = new JPanel();
        center.setLayout(new GridLayout(1, 2));
        east.setLayout(new GridLayout(2, 1));
        west.setLayout(new GridLayout(2, 1));
        cbCase = new JCheckBox("Match Case", true);
        cbWhole = new JCheckBox("Match Word", true);
        ButtonGroup group = new ButtonGroup();
        JRadioButton up = new JRadioButton("Search Up", false);
        down = new JRadioButton("Search Down", true);
        cbCase.setMnemonic('C');
        cbWhole.setMnemonic('W');
        up.setMnemonic('U');
        down.setMnemonic('D');
        group.add(up);
        group.add(down);
        east.add(cbCase);
        east.add(cbWhole);
        east.setBorder(BorderFactory.createTitledBorder("Search options:"));
        west.add(up);
        west.add(down);
        west.setBorder(BorderFactory.createTitledBorder("Search direction:"));
        center.add(east);
        center.add(west);
    }

    /**
     * process gathers information to begin the text search the word or replace the words which is
     * handled by the class variable isReplace. When the search is ended int returns -1<br>
     * getWord() gets the word to be searched for: returns String <br>
     * getAllText() gets all the text to be searched: returns String <br>
     * 
     * @see #getWord()
     * @see #getAllText()
     * @see #search(String, String, int)
     * @see #endResultMessage(boolean, int)
     */
    private void process()
    {
        if (isReplace)
            statusInfo.setText("Replacing " + textfield.getText() + " in ");// +MainEditor.file.getName());
        else statusInfo.setText("Searching for " + textfield.getText() + " in ");// +MainEditor.file.getName());
        int caret = editorPane.getCaretPosition();
        String word = getWord();
        String text = getAllText();
        caret = search(text, word, caret);
        if (caret < 0)
            endResultMessage(false, 0);
    }

    public void itemStateChanged(ItemEvent e)
    {

        Object source = e.getItemSelectable();

        if (source == matchCaseButton)
        {
            System.out.println("macth case selected");
        }

        // Now that we know which button was pushed, find out
        // whether it was selected or deselected.
        if (e.getStateChange() == ItemEvent.DESELECTED)
        {
            System.out.println("macth case NOT selected");
        }

    }

    /**
     * search searches through the text from the requested caret position down or up in a for loop
     * 
     * @param String :
     *            text in the text area
     * @param String :
     *            word being searched for
     * @param int :
     *            the text area caret postion
     * @return int - the caret position if the word is found
     * @see #isSearchDown()
     * @see #wholeWordIsSelected()
     * @see #checkForWholeWord(int, String, int, int)
     */
    private int search(String text, String word, int caret)
    {
        boolean found = false;
        int all = text.length();
        int check = word.length();
        if (isSearchDown())
        {
            int add = 0;
            for (int i = caret + 1; i < (all - check); i++)
            {
                add++;
                String temp = text.substring(i, (i + check));
                if (temp.equals(word))
                {
                    if (wholeWordIsSelected())
                    {
                        if (checkForWholeWord(check, text, add, caret))
                        {
                            caret = i;
                            found = true;
                            break;
                        }
                    } else
                    { // Not whole word
                        caret = i;
                        found = true;
                        break;
                    }
                } // temp=word
            } // for
        } // end if
        else
        { // else the search is up
            int add = caret;
            for (int i = caret - 1; i >= check; i--)
            {
                add--;
                String temp = text.substring((i - check), i);
                if (temp.equals(word))
                {
                    if (wholeWordIsSelected())
                    {
                        if (checkForWholeWord(check, text, add, caret))
                        {
                            caret = i;
                            found = true;
                            break;
                        }
                    } else
                    { // Not whole word
                        caret = i;
                        found = true;
                        break;
                    }
                } // temp=word
            } // for
            editorPane.setCaretPosition(0);
        } // end else
        if (found)
        {
            editorPane.requestFocus();
            if (isSearchDown())
                editorPane.select(caret, caret + check);
            else editorPane.select(caret - check, caret);
            if (isReplace)
            {
                String replace = replaceText.getText();
                editorPane.replaceSelection(replace);
                if (isSearchDown())
                    editorPane.select(caret, caret + replace.length());
                else editorPane.select(caret - replace.length(), caret);
                // MainEditor.isSaved=false;
                // MainEditor.button[2].setEnabled(true);
            }
            foundOnce = true; // the search word has been found 1 or more times
            return caret;
        }
        return -1;
    }

    /**
     * checkForWholeWord returns true if the character to the left and right of the seached word is
     * NOT a letter or a digit (number)
     * 
     * @param int
     *            check: place of the caret in the current search
     * @param String
     *            text: the text area text
     * @param int
     *            add: the place in the for loop
     * @param int
     *            caret: the last caret postion checked
     * @return boolean: true if it is a whole word
     */
    private boolean checkForWholeWord(int check, String text, int add, int caret)
    {
        int offsetLeft = (caret + add) - 1;
        int offsetRight = (caret + add) + check;
        if ((offsetLeft < 0) || (offsetRight > text.length()))
            return true;
        return ((!Character.isLetterOrDigit(text.charAt(offsetLeft))) && (!Character
                .isLetterOrDigit(text.charAt(offsetRight))));
    }

    /**
     * replaceAll replaces the search word with the new word selected by the user
     * 
     * @see #checkForWholeWord(int, String, int, int)
     */
    private void replaceAll()
    {
        String word = textfield.getText();
        String text = editorPane.getText();
        String insert = replaceText.getText();
        StringBuffer sb = new StringBuffer(text);
        int diff = insert.length() - word.length();
        int offset = 0;
        int tally = 0;
        for (int i = 0; i < text.length() - word.length(); i++)
        {
            String temp = text.substring(i, i + word.length());
            if ((temp.equals(word)) && (checkForWholeWord(word.length(), text, 0, i)))
            {
                tally++;
                sb.replace(i + offset, i + offset + word.length(), insert);
                offset += diff;
            } // if equals(word)
        } // for
        editorPane.setText(sb.toString());
        endResultMessage(true, tally);
        editorPane.setCaretPosition(0);
    }

    /**
     * endResultMessage replaces the search word with the new word selected by the user
     * 
     * @param boolean
     *            isReplaceAll: whether this was a replace or a Find search
     * @param int
     *            tally: how many words were replaced in
     * @see #replaceAll()
     * @see #statusInfo
     */
    private void endResultMessage(boolean isReplaceAll, int tally)
    {
        String message = "";
        if (isReplaceAll)
        {
            if (tally == 0)
                message = textfield.getText() + " not found";
            else if (tally == 1)
                message = "One change was made to " + textfield.getText();
            else message = "" + tally + " changes were to " + textfield.getText();
            if (tally > 0)
            {
                // MainEditor.isSaved=false;
                // MainEditor.button[2].setEnabled(true);
            }
        } else
        {
            String str = "";
            if (isSearchDown())
                str = "search down";
            else str = "search up";
            if (foundOnce && !isReplace)
                message = "End of " + str + " for " + textfield.getText();
            else if (foundOnce && isReplace)
                message = "End of replace " + textfield.getText() + " with "
                        + replaceText.getText();
            else message = textfield.getText() + " was not found";
        }
        statusInfo.setText("<html><font color='a00000'><b>" + message + "</b><font></html>");
    }

    /**
     * getWord gets the word to be searched or replaced from the text field <br>
     * caseNotSelected() sets the word to {@see java.lang.String.toLowerCase()} lower case
     * 
     * @return String (search word)
     * @see #caseNotSelected()
     */
    private String getWord()
    {
        if (caseNotSelected())
            return textfield.getText().toLowerCase();
        return textfield.getText();
    }

    /**
     * getAllText gets the all the text to be searched through from the main editor text area <br>
     * caseNotSelected() sets all text to {@see java.lang.String.toLowerCase()} lower case if the
     * search is not case sensitive
     * 
     * @return String (whole text document)
     * @see #caseNotSelected()
     */
    private String getAllText()
    {
        if (caseNotSelected())
            return editorPane.getText().toLowerCase();
        return editorPane.getText();
    }

    /**
     * caseNotSelected returns the value of the radio button cbCase
     * 
     * @return true if the search is case sensitive
     */
    private boolean caseNotSelected()
    {
        return !cbCase.isSelected();
    }

    /**
     * wholeWordIsSelected returns the value of the radio button cbWhole
     * 
     * @return true if the search is for a whole word
     */
    private boolean wholeWordIsSelected()
    {
        return cbWhole.isSelected();
    }

    /**
     * wholeWordIsSelected() returns the value of the radio button down
     * 
     * @return true if the search is down
     */
    private boolean isSearchDown()
    {
        return down.isSelected();
    }

    protected MemoryDropDownButton createMemoryToolbarButton(final String label,
                                                             final String iconName,
                                                             final String hint,
                                                             final List<JComponent> menus)
    {
        ImageIcon buttonIcon = IconManager.getIcon(iconName, IconManager.IconSize.Std24);

        MemoryDropDownButton btn = new MemoryDropDownButton(label, buttonIcon,
                SwingConstants.BOTTOM, menus);
        btn.setStatusBarHintText(hint);
        return btn;
    }

    public static void main(String[] args)
    {
        FindReplaceDlg pane = new FindReplaceDlg(1);
        FindReplaceDlg pane1 = new FindReplaceDlg();
        // dlg.setContentPane(pane);
        pane.pack();
        // dlg.doLayout();
        // dlg.setPreferredSize(dlg.getPreferredSize());
        // dlg.setSize(dlg.getPreferredSize());
        UIHelper.centerAndShow(pane);
    }

    /**
     * actionPerformed(ActionEvent) handles actions from the buttons
     * 
     * @param java.awt.event.ActionEvent
     * @see #process()
     * @see #replaceAll()
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource().equals(textfield) || e.getSource().equals(replaceText)
                || e.getActionCommand().equals("Find Next")
                || e.getActionCommand().equals("Replace Next"))
            validate();
        process();
        if (e.getActionCommand().equals("Replace All"))
            replaceAll();
    }

    class PopupPanel extends JPanel
    {
        JPopupMenu popup = new JPopupMenu();

        public PopupPanel()
        {
            JMenuItem item;
            popup.add(item = new JMenuItem("Cut"));
            popup.add(item = new JMenuItem("Copy"));
            popup.add(item = new JMenuItem("Paste"));
            popup.addSeparator();
            popup.add(item = new JMenuItem("Select All"));
            popup.setInvoker(this);
            addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent e)
                {
                    if (e.isPopupTrigger())
                    {
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }

                public void mouseReleased(MouseEvent e)
                {
                    if (e.isPopupTrigger())
                    {
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
        }
    }
}
