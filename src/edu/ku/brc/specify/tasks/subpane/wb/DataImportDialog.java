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
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.undo.UndoManager;

import org.apache.log4j.Logger;

import com.csvreader.CsvReader;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;

/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 * Created Date: Mar 26, 2007
 *
 */
@SuppressWarnings("serial")
public class DataImportDialog extends JDialog implements ActionListener // implements ChangeListener 
{
    // ConfigureCSV conf = new ConfigureCSV();

    JButton                     cancelBtn;
    //JButton                     backBtn;
    //JButton                     nextBtn;
    JButton                     okBtn;
    JButton                     helpBtn;
    private JRadioButton           tab;
    private JRadioButton           space;
    private JRadioButton           semicolon;
    private JRadioButton           comma;
    private JRadioButton           other;
    private JTextField          otherText;
    //@SuppressWarnings("unused")
    //private char                delimiterChar;
    @SuppressWarnings("unused")
    private char                stringQualifierChar;
    private char                delimChar;
    private JLabel textQualLabel;
    private JComboBox textQualCombo;
    
    private JLabel charSetLabel;
    private JComboBox charSetCombo;
    private JLabel escapeModeLabel;
    private JComboBox escapeModeCombo;
    private JCheckBox containsHeaders;
    private static final Logger log = Logger.getLogger(DataImportDialog.class);
    
    private boolean isCsvImport = true;

    /**
     * 
     */
    public DataImportDialog(final Frame frame, final String title, final String filePathName)
    {
        setContentPane(createConfigPanel(filePathName, isCsvImport));
        setTitle(getResourceString(title));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
    }

    public JPanel createConfigPanel(String filePathName, boolean isCSV)
    {
        //JPanel configPanel = new FormDebugPanel();
        JPanel configPanel = new JPanel();
        CellConstraints cc = new CellConstraints();
        PanelBuilder builder = new PanelBuilder(new FormLayout(
                "5px, 15px,p,30px , p:g,15px,", // columns
                
                "5px," + //padding
                //"p,15px, " +//directions
                "p,3px, " +//separator     
                "p,10px, " +//delim panel
                //"p,15px, " +//separator           
                "p,3px, " +//file info separator
                "p,3px, " +//file info lable
                "p,10px, " +//row header
                "p, 3px,"+//previe separator
                "p,10px," + //tablePreview
                "p,10px" //buttongs
                ), configPanel);// rows

       // JLabel directions = new JLabel(getResourceString("DELIM_EXPLAIN"));
        
//        JTextArea directions = new JTextArea(getResourceString("DELIM_EXPLAIN"));
//        directions.setLineWrap(true);
//        directions.setWrapStyleWord(true);
//        directions.setEditable(false);
//        directions.setBackground(builder.getPanel().getBackground());
        //directions.setFont(builder.getPanel().getFont());
        JLabel fileInfo = new JLabel(getResourceString("FILE_PREVIEW") + " " + filePathName);
        JPanel buttonpanel = buildButtons();
        containsHeaders = new JCheckBox(getResourceString("COLUMN_HEAD"));
        containsHeaders.setSelected(true);
        
        //builder.add         (directions,                            cc.xyw(2,2,4));
        builder.addSeparator(getResourceString("DATA_IMPORT_OPS"),  cc.xyw(2,2,4)); 
        builder.add         (createDelimiterPanel(),                cc.xy (3,4));        
        builder.add         (createOtherControlsPanel(),            cc.xy (5,4));       
        //builder.addSeparator("",                                    cc.xyw(2,6,4));      
          
        builder.addSeparator(getResourceString("FILE_IMPORT"),      cc.xyw(2,6,4));
        builder.add         (fileInfo,                              cc.xyw(3,8,4));
        builder.add         (containsHeaders,                       cc.xyw(3,10,3));   
        
        builder.addSeparator(getResourceString("DATA_PREVIEW"),     cc.xyw(2,12,4));
        builder.add         (createTablePreview(null),              cc.xyw(3,14,3));       
        builder.add         (buttonpanel,                           cc.xyw(2,16,4)); 
        configPanel.setMinimumSize(buttonpanel.getMinimumSize());
        return configPanel;
    }

    public JPanel buildButtons()
    {

        cancelBtn = new JButton(getResourceString("Cancel"));
        okBtn = new JButton(getResourceString("OK"));
        helpBtn = new JButton(getResourceString("Help"));

        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("cacnel button clicked");
            }
        });

        okBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("finish button clicked");
            }
        });

        getRootPane().setDefaultButton(okBtn);
        HelpMgr.registerComponent(helpBtn, "configcsv");
        return  ButtonBarFactory.buildOKCancelHelpBar(okBtn, cancelBtn, helpBtn);
    }
    
    public JPanel createOtherControlsPanel()
    {
        JPanel myPanel = new JPanel();
        CellConstraints cc = new CellConstraints();
        FormLayout formLayout = new FormLayout(
                "p,3px, p,3px",// p,3px", 
                //"p,3px, " + //
                "p,4px,   p,4px,  p,4px,  p,4px");
        PanelBuilder builder = new PanelBuilder(formLayout, myPanel);

        textQualLabel = new JLabel(getResourceString("TEXT_QUAL"));
        String[] qualifiers = { "\"", "\'", "{none}" };
        textQualCombo = new JComboBox(qualifiers);
        textQualCombo.setSelectedIndex(0);
        textQualCombo.addActionListener(this);


        charSetLabel = new JLabel(getResourceString("CHAR_SET"));
        String[] charsets = { "DEFAULT", "US-ASCII", "ISO-8859-1", "UTF-8" };
        charSetCombo = new JComboBox(charsets);
        charSetCombo.setSelectedIndex(0);
        charSetCombo.addActionListener(this);
        
        escapeModeLabel = new JLabel(getResourceString("ESCAPE_MODE"));
        String[] escapeModes = { "backslash", "doubled"};
        escapeModeCombo = new JComboBox(escapeModes);
        escapeModeCombo.setSelectedIndex(0);
        escapeModeCombo.addActionListener(this);

        builder.add(textQualLabel,    cc.xy(1, 1));
        builder.add(textQualCombo,    cc.xy(3, 1));     
        builder.add(charSetLabel,    cc.xy(1, 3));
        builder.add(charSetCombo,    cc.xy(3, 3));            
        builder.add(escapeModeLabel,    cc.xy(1, 5));
        builder.add(escapeModeCombo,    cc.xy(3, 5));   

        return myPanel;       
    }
    
    /** Listens to the combo box. */
    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        String str = (String)cb.getSelectedItem();
        //cb.getS
        if(str.equals("\""))
        {
            stringQualifierChar = '\'';
        }
        else if(str.equals("\'"))
        {
            stringQualifierChar = '\'';
        }
        //updateLabel(petName);
    }
    public JPanel createDelimiterPanel()
    {
        
        JPanel myPanel = new JPanel();
        CellConstraints cc = new CellConstraints();
        FormLayout formLayout = new FormLayout(
                "p,p,2px, p:g,2px, p,2px,p,2px,",// p,3px", 
                //"p,3px, " + //
                "p,2px,   p,2px, p,2px , p,2px , p,2px , p,2px, p,2px ");
        PanelBuilder builder = new PanelBuilder(formLayout, myPanel);
        Color curColor = myPanel.getBackground();
        Color newColor = curColor;//.brighter();
        
        
        tab = new JRadioButton(getResourceString("TAB"));
        tab.addItemListener(new CheckboxItemListener());
        tab.setBackground(newColor);
        
        space = new JRadioButton(getResourceString("SPACE"));
        space.addItemListener(new CheckboxItemListener());
        space.setBackground(newColor);

        comma = new JRadioButton(getResourceString("COMMA"));
        comma.addItemListener(new CheckboxItemListener());
        comma.setSelected(true);
        comma.setBackground(newColor);
        
        semicolon = new JRadioButton(getResourceString("SEMICOLON"));
        semicolon.addItemListener(new CheckboxItemListener());
        semicolon.setBackground(newColor);
        
        other = new JRadioButton(getResourceString("OTHER"));
        other.addItemListener(new CheckboxItemListener());
        other.setBackground(newColor);
        
        otherText = new JTextField();
        otherText.addKeyListener(new CharFieldKeyAdapter());
        otherText.setColumns(1);
        otherText.setEditable(false);
        otherText.setDocument(new CharLengthLimitDocument(1));//limits the textfield to only allowing on character
        
        ButtonGroup group = new ButtonGroup();
        group.add(tab);
        group.add(space);
        group.add(other);
        group.add(comma);
        group.add(semicolon);
        
        //builder.add(directions,                                     cc.xywh(2, 1,  8, 1));
        builder.addSeparator(getResourceString("SELECT_DELIMS"),    cc.xyw(1, 1, 4));
        builder.add(comma,                                            cc.xyw(1, 3,4));
        builder.add(semicolon,                                          cc.xyw(1, 5,4));
        builder.add(space,                                          cc.xyw(1, 7,4));
        builder.add(tab,                                      cc.xyw(1, 9,4));
        builder.add(other,                                          cc.xy(1, 11));
        builder.add(otherText,                                      cc.xy(2, 11));
        
        
        myPanel.setBackground(newColor);
        return myPanel;
    }
    

    
    public void createDummyCsvReader()
    {
//        CsvReader csv = new CsvReader(new FileInputStream(config.getFile()), config.getDelimiter(), config.getCharset());
//        csv.setEscapeMode(config.getEscapeMode());
//        csv.setTextQualifier(config.getTextQualifier());
    }
    
    public JScrollPane createTablePreview(CsvReader csv)
    {
        
        Object[] columnNames = { "First Name", "Last Name", "Sport", "First Name", "Last Name", "Sport","First Name", "Last Name", "Sport", "# of Years", "Vegetarian" };

        Object[][] data = {
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Philip", "Milne", "Pool",  new Integer(10), new Boolean(false) } ,
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Philip", "Milne", "Pool",  new Integer(10), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Philip", "Milne", "Pool",  new Integer(10), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Philip", "Milne", "Pool",  new Integer(10), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Philip", "Milne", "Pool",  new Integer(10), new Boolean(false) }};

        //JTable myJTable = new JTable(data, columnNames);
        JTable t = new JTable(data, columnNames);
        t.setColumnSelectionAllowed(false);
        t.setRowSelectionAllowed(false);
        t.setCellSelectionEnabled(false);
        t.setPreferredScrollableViewportSize(t.getPreferredSize());
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //t.setPreferredScrollableViewportSize(new Dimension(200,100));
        //initColumnSizes(t);
        //Dimension size = t.getPreferredScrollableViewportSize();
        //t.setPreferredScrollableViewportSize
        //    (new Dimension(100,100));
        //t.setShowGrid(true);
        //t.getModel().i
        //t.setModel(new PreviewTableModel());
        JScrollPane pane = new JScrollPane(t, 
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        pane.setPreferredSize(new Dimension(500,100));
        //pane.add(t);
        //pane.set
        //pane.p

        return pane;
    }
    
    public void updateTableDispaly()
    {
        
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DataImportDialog dlg = new DataImportDialog((Frame) UICacheManager
                .get(UICacheManager.FRAME), getResourceString("IMPORT_CVS"), "c:\\work\\blah\\blah\\filename.csv");

        UIHelper.centerAndShow(dlg);
    }
    /**
     * @author megkumin
     *
     * @code_status Alpha
     *
     * Created Date: Mar 15, 2007
     *
     */
    private class CharFieldKeyAdapter extends KeyAdapter
    {
        /**
         * 
         */
        public CharFieldKeyAdapter()
        {
            super();
        }

        
        /* (non-Javadoc)
         * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
         */
        public void keyReleased(KeyEvent ke)
        {
            if(otherText.getText().length() == 1)
            {                
                delimChar = otherText.getText().toCharArray()[0];
                log.debug("Other value selected for delimiter: ["+ delimChar +"]" );
            }            
            else if(otherText.getText().length()==0)
            {
                delimChar = ',';
                log.debug("Other value cleared for delimiter setting to default: ["+ delimChar +"]" );
            }
            else
            {
                log.error("Other field should not allow more that one character as a delimiter");
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
    private class CheckboxItemListener implements ItemListener
    {
        /**
         * 
         */
        public CheckboxItemListener()
        {
            super();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        public void itemStateChanged(ItemEvent e)
        {
            
//            private JRadioButton           tab;
//            private JRadioButton           space;
//            private JRadioButton           semicolon;
//            private JRadioButton           comma;
//            private JRadioButton           other;
            
            
            if(other==null)return;
            if (other.isSelected())
            {
                otherText.setEditable(true);
                otherText.requestFocus();
            } else if(tab.isSelected())
            {
                delimChar = '\t';
            }else if(space.isSelected())
            {
                delimChar = ' ';
            }
            else if(semicolon.isSelected())
            {
                delimChar = ';';
            }
            else if(comma.isSelected())
            {
                delimChar = ',';
            }
            else
            {
                otherText.setEditable(false);
            }
            updateTableDispaly();
        }
    }  
    
    class PreviewTableModel extends DefaultTableModel
    {
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }    
    }

    //------------------------------------------------------------
    // Inner Classes
    //------------------------------------------------------------


    class GridCellEditor extends AbstractCellEditor implements TableCellEditor//, UndoableTextIFace
    {
        protected JTextField  textField   = new JTextField();
        protected UndoManager undoManager = new UndoManager();

        public GridCellEditor()
        {
            textField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }

        /* (non-Javadoc)
         * @see javax.swing.CellEditor#getCellEditorValue()
         */
        public Object getCellEditorValue() 
        {
            return textField.getText();
        }

        /* (non-Javadoc)
         * @see javax.swing.AbstractCellEditor#isCellEditable(java.util.EventObject)
         */
        @Override
        public boolean isCellEditable(EventObject anEvent) 
        { 
            return false; 
        }
        
        //
        //          Implementing the CellEditor Interface
        //
        /** Implements the <code>TableCellEditor</code> interface. */
        public Component getTableCellEditorComponent(JTable  tbl, 
                                                     Object  value,
                                                     boolean isSelected,
                                                     int     row, 
                                                     int     column)
        {
            textField.setText(value != null ? value.toString() : "");
            //textField.selectAll();
            //undoManager.discardAllEdits();
            //UICacheManager.getUndoAction().setUndoManager(undoManager);
            //UICacheManager.getRedoAction().setUndoManager(undoManager);
            return textField;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.UICacheManager.UndoableTextIFace#getUndoManager()
         */
        public UndoManager getUndoManager()
        {
            return undoManager;
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.UICacheManager.UndoableTextIFace#getText()
         */
        public JTextComponent getTextComponent()
        {
            return textField;
        }
     }
  
    /**
     * @author megkumin
     * 
     * @code_status Alpha
     * 
     * Created Date: Mar 27, 2007
     * 
     */
    class CharLengthLimitDocument extends PlainDocument
    {
        int limit;

        /**
         * @param limit
         */
        public CharLengthLimitDocument(int limit)
        {
            this.limit = limit;
        }

        /* (non-Javadoc)
         * @see javax.swing.text.PlainDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
         */
        public void insertString(int offset, String s, AttributeSet a) throws BadLocationException
        {
            if (offset + s.length() <= limit)
            {
                super.insertString(offset, s, a);
            } else
            {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    /**
     * @return the stringQualifierChar
     */
    public char getStringQualifierChar()
    {
        return stringQualifierChar;
    }

    /**
     * @param stringQualifierChar the stringQualifierChar to set
     */
    public void setStringQualifierChar(char stringQualifierChar)
    {
        this.stringQualifierChar = stringQualifierChar;
    }

    /**
     * @return the delimChar
     */
    public char getDelimChar()
    {
        return delimChar;
    }

    /**
     * @param delimChar the delimChar to set
     */
    public void setDelimChar(char delimChar)
    {
        this.delimChar = delimChar;
    }
}
