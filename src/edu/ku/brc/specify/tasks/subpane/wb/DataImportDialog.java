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
import javax.swing.JSpinner;
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

import edu.ku.brc.specify.help.HelpMgr;
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
    JButton                     backBtn;
    JButton                     nextBtn;
    JButton                     finishBtn;
    JButton                     helpBtn;
    private JRadioButton           tab;
    private JRadioButton           space;
    private JRadioButton           semicolon;
    private JRadioButton           comma;
    private JRadioButton           other;
    private JTextField          otherText;
    private char                delimiterChar;
    @SuppressWarnings("unused")
    private char                stringQualifierChar;
    private JLabel textQualLabel;
    private JComboBox textQualCombo;
    private JCheckBox containsHeaders;
    private static final Logger log = Logger.getLogger(DataImportDialog.class);
    
    private boolean isCsvImport = true;

    /**
     * 
     */
    public DataImportDialog(final Frame frame, final String title)
    {
        setContentPane(createConfigPanel(isCsvImport));
        setTitle(getResourceString("logintitle"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
    }

    public JPanel createConfigPanel(boolean isCSV)
    {
        JPanel configPanel = new FormDebugPanel();
        CellConstraints cc = new CellConstraints();
        PanelBuilder builder = new PanelBuilder(new FormLayout(
                "5dlu, p,3dlu , p:g,3dlu,", // columns
                
                "5dlu," + //padding
                "p,15dlu, " +//directions
                "p,15dlu, " +//delim panel
                "p, 3dlu,"+//previe separator
                "p,3dlu," + //tablePreview
                "p,5dlu" //buttongs
                ), configPanel);// rows

       // JLabel directions = new JLabel(getResourceString("DELIM_EXPLAIN"));
        
        JTextArea directions = new JTextArea(getResourceString("DELIM_EXPLAIN"));
        directions.setLineWrap(true);
        directions.setWrapStyleWord(true);
        directions.setEditable(false);
        directions.setBackground(builder.getPanel().getBackground());
        //directions.setFont(builder.getPanel().getFont());
        
        builder.add(directions, cc.xyw(2,2,4));
        builder.add(createDelimiterPanel(), cc.xy(2, 4));        
        builder.add(createOtherControlsPanel(), cc.xy(4, 4));
        
        builder.addSeparator(getResourceString("DATA_PREVIEW"),    cc.xyw(2, 6,4));
        builder.add(createTablePreview(null),                       cc.xyw(2, 8,4));
        builder.add(buildButtons(),          cc.xyw(2,10,4)); 
        return configPanel;
    }

    public JPanel buildButtons()
    {

        cancelBtn = new JButton(getResourceString("Cancel"));
        backBtn = new JButton(getResourceString("Back"));
        nextBtn = new JButton(getResourceString("Next"));
        finishBtn = new JButton(getResourceString("Finish"));
        helpBtn = new JButton(getResourceString("Help"));

        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("cacnel button clicked");
            }
        });

        backBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("back button clicked");
            }
        });

        nextBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("next button clicked");
            }
        });

        finishBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("finish button clicked");
            }
        });

        getRootPane().setDefaultButton(nextBtn);
        HelpMgr.registerComponent(helpBtn, "configcsv");
        return ButtonBarFactory.buildRightAlignedBar(helpBtn, cancelBtn, backBtn, nextBtn, finishBtn);
    }
    
    public JPanel createOtherControlsPanel()
    {
        JPanel myPanel = new JPanel();
        CellConstraints cc = new CellConstraints();
        FormLayout formLayout = new FormLayout(
                "p,3dlu, p,3dlu",// p,3dlu", 
                //"p,3dlu, " + //
                "p,3dlu,   p,3dlu");
        PanelBuilder builder = new PanelBuilder(formLayout, myPanel);

        textQualLabel = new JLabel(getResourceString("TEXT_QUAL"));
        
        String[] qualifiers = { "  \"", "  \'", "{none}" };

        textQualCombo = new JComboBox(qualifiers);
        textQualCombo.setSelectedIndex(0);
        textQualCombo.addActionListener(this);
        //textQualCombo
        
        containsHeaders = new JCheckBox("First row contain column headers");
        builder.add(textQualLabel,    cc.xy(1, 1));
        builder.add(textQualCombo,    cc.xy(3, 1));     
        builder.add(containsHeaders,    cc.xyw(1, 3,3));     
        return myPanel;       
    }
    /** Listens to the combo box. */
    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        String qualifier = (String)cb.getSelectedItem();
        if(qualifier.equals("  \""))
        {
            stringQualifierChar = '\'';
        }
        else if(qualifier.equals("  \'"))
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
                "p,p,3dlu, p:g,3dlu, p,3dlu,p,3dlu,",// p,3dlu", 
                //"p,3dlu, " + //
                "p,3dlu,   p,3dlu, p,3dlu , p,3dlu , p,3dlu , p,3dlu, p,3dlu ");
        PanelBuilder builder = new PanelBuilder(formLayout, myPanel);

        
        
        tab = new JRadioButton(getResourceString("TAB"));
        tab.addItemListener(new CheckboxItemListener());
        
        space = new JRadioButton(getResourceString("SPACE"));
        space.addItemListener(new CheckboxItemListener());

        comma = new JRadioButton(getResourceString("COMMA"));
        comma.addItemListener(new CheckboxItemListener());

        semicolon = new JRadioButton(getResourceString("SEMICOLON"));
        semicolon.addItemListener(new CheckboxItemListener());
        
        other = new JRadioButton(getResourceString("OTHER"));
        other.addItemListener(new CheckboxItemListener());
        
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
        builder.addSeparator(getResourceString("SELECT_DELIMS"),    cc.xyw(1, 1, 6));
        builder.add(tab,                                            cc.xyw(1, 3,4));
        builder.add(space,                                          cc.xyw(1, 5,4));
        builder.add(comma,                                          cc.xyw(1, 7,4));
        builder.add(semicolon,                                      cc.xyw(1, 9,4));
        builder.add(other,                                          cc.xy(1, 11));
        builder.add(otherText,                                      cc.xy(2, 11));
        
        
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
        pane.setPreferredSize(new Dimension(100,100));
        //pane.add(t);
        //pane.set
        //pane.p

        return pane;
    }
    
    public void updateTableDispaly()
    {
        
    }
    /**
     * Adjust all the column width for the data in the column, this may be handles with JDK 1.6 (6.)
     * @param tableArg the table that should have it's columns adjusted
     */
//    private void initColumnSizes(final JTable tableArg) 
//    {
//        TableModel  tblModel    = tableArg.getModel();
//        TableColumn column      = null;
//        Component   comp        = null;
//        int         headerWidth = 0;
//        int         cellWidth   = 0;
//        
//        TableCellRenderer headerRenderer = tableArg.getTableHeader().getDefaultRenderer();
//
//        GridCellEditor cellEditor = new GridCellEditor();
//        //UICacheManager.getInstance().hookUpUndoableEditListener(cellEditor);
//        
//        for (int i = 0; i < tblModel.getColumnCount(); i++) 
//        {
//            column = tableArg.getColumnModel().getColumn(i);
//
//            comp = headerRenderer.getTableCellRendererComponent(
//                                 null, column.getHeaderValue(),
//                                 false, false, 0, 0);
//            headerWidth = comp.getPreferredSize().width;
//
//            comp = tableArg.getDefaultRenderer(tblModel.getColumnClass(i)).
//                                               getTableCellRendererComponent(tableArg, tblModel.getValueAt(0, i), false, false, 0, i);
//            
//            cellWidth = comp.getPreferredSize().width;
//            
//            //comp.setBackground(Color.WHITE);
//            
//            int maxWidth = headerWidth + 10;
//            TableModel m = tableArg.getModel();
//            FontMetrics fm     = new JLabel().getFontMetrics(myPanel.getFont());
//            for (int row=0;row<tableArg.getModel().getRowCount();row++)
//            {
//                String text = m.getValueAt(row, i).toString();
//                maxWidth = Math.max(maxWidth, fm.stringWidth(text)+10);
//                //System.out.println(i+" "+maxWidth);
//            }
//
//            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
//            column.setPreferredWidth(Math.max(maxWidth, cellWidth));
//            
//            column.setCellEditor(cellEditor);
//        }
//        
//        //tableArg.setCellEditor(new GridCellEditor());
//
//    }
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DataImportDialog dlg = new DataImportDialog((Frame) UICacheManager
                .get(UICacheManager.FRAME), "Column Mapper");

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
//            log.debug("character typed");
//            // make sure the user has entered a text string in teh find box before enabling find buttons
//            boolean charentered = (otherText.getText().length() == 1);
//            delimiterChar = otherText.getText().toCharArray()[0];
//            log.debug("char entered: " + delimiterChar);
            //if(replaceField!=null) replaceTextState = (replaceField.getText().length() > 0);
            //nextButton.setEnabled(findTextState);
            //memoryReplaceButton.setEnabled(findTextState && replaceTextState);
            //make sure the user has entered a text string in teh replace textfield before enabling replace buttons
            //if(replaceButton!=null)replaceButton.setEnabled(findTextState && replaceTextState);
            //if(replaceAllButton!=null)replaceAllButton.setEnabled(findTextState && replaceTextState);
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
            if (other.isSelected())
            {
                otherText.setEditable(true);
                otherText.requestFocus();
            } else otherText.setEditable(false);
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
}
