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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.EventObject;
import java.util.Vector;

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
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
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
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.ui.HelpMgr;

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
    private Charset  charset;
    private JLabel textQualLabel;
    private boolean doesFirstRowHaveHeaders;
    private JComboBox textQualCombo;
    private int     escapeMode;
    
    private JLabel charSetLabel;
    private JComboBox charSetCombo;
    private JLabel escapeModeLabel;
    private JComboBox escapeModeCombo;
    private JCheckBox containsHeaders;
    private static final Logger log = Logger.getLogger(DataImportDialog.class);
    protected boolean           isCancelled      = true;
    //private boolean isCsvImport = true;
    public static final int OK_BTN             = 1;
    public static final int CANCEL_BTN         = 2;
    public static final int HELP_BTN           = 4;
    protected int               btnPressed       = CANCEL_BTN;
    private String fileName;
    private File file;
    private ConfigureCSV config;
    private JTable myDisplayTable;
    PreviewTableModel model ;
    /**
     * 
     */
    public DataImportDialog(final ConfigureCSV config)
    {
    	this.config = config;
    	this.file = config.getFile();
    	this.fileName = file.getAbsolutePath().toString();
        this.doesFirstRowHaveHeaders = true;
        myDisplayTable = new JTable();
        model = new PreviewTableModel();

    }
    
    public void initForCSV()
    {
    	setContentPane(createConfigPanelForCSV());
    	init(getResourceString("IMPORT_CVS"));
    }
    
    public void initForXSL()
    {
    	setContentPane(createConfigPanelForXSL());
    	init(getResourceString("IMPORT_XSL"));   	
    }
    private void init(String title)
    {
        //containsHeaders = ;
        setTitle(title);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
    }

    private JPanel createConfigPanelForCSV()
    {
        //JPanel configPanel = new FormDebugPanel();
        JPanel configPanel = new JPanel();
        CellConstraints cc = new CellConstraints();
        PanelBuilder builder = new PanelBuilder(new FormLayout(
                "5px, 15px,p,30px , p:g,15px,", // columns
                
                "5px," + 		//padding
                "p,3px, " +		//separator     
                "p,10px, " +	//delim panel        
                "p,3px, " +		//file info separator
                "p,3px, " +		//file info lable
                "p,10px, " +	//row header
                "p, 3px,"+		//previe separator
                "p,10px," + 	//tablePreview
                "p,10px" 		//buttongs
                ), configPanel);// rows

        JLabel fileInfo = new JLabel(getResourceString("FILE_PREVIEW") + " " + fileName);
        JPanel buttonpanel = buildButtons();
        containsHeaders = new JCheckBox(getResourceString("COLUMN_HEAD"));
        containsHeaders.setSelected(true);
        containsHeaders.addItemListener(new CheckboxItemListener());
        //containsHeaders.addActionListener(new CheckBoxItemListener());
        
        
        builder.addSeparator(getResourceString("DATA_IMPORT_OPS"),  cc.xyw(2,2,4)); 
        builder.add         (createDelimiterPanel(),                cc.xy (3,4));        
        builder.add         (createOtherControlsPanel(),            cc.xy (5,4));       
          
        builder.addSeparator(getResourceString("FILE_IMPORT"),      cc.xyw(2,6,4));
        builder.add         (fileInfo,                              cc.xyw(3,8,4));
        builder.add         (containsHeaders,                       cc.xyw(3,10,3));   
        
        builder.addSeparator(getResourceString("DATA_PREVIEW"),     cc.xyw(2,12,4));
        
        myDisplayTable = setTableData(myDisplayTable);
        builder.add         (addtoScroll(myDisplayTable),              cc.xyw(3,14,3));       
        builder.add         (buttonpanel,                           cc.xyw(2,16,4)); 
        configPanel.setMinimumSize(buttonpanel.getMinimumSize());
        return configPanel;
    }
    
    private JScrollPane addtoScroll(JTable t)
    {
    	JScrollPane pane = new JScrollPane(t, 
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
       pane.setPreferredSize(new Dimension(500,100));
       return pane;
    }

    private JPanel createConfigPanelForXSL()
    {
        //JPanel configPanel = new FormDebugPanel();
        JPanel configPanel = new JPanel();
        CellConstraints cc = new CellConstraints();
        PanelBuilder builder = new PanelBuilder(new FormLayout(
                "5px, 15px,p,30px , p:g,15px,", // columns
                
                "5px," + 		//padding      
                "p,3px, " +		//file info separator
                "p,3px, " +		//file info lable
                "p,10px, " +	//row header
                "p, 3px,"+		//previe separator
                "p,10px," + 	//tablePreview
                "p,10px" 		//buttongs
                ), configPanel);// rows

        JLabel fileInfo = new JLabel(getResourceString("FILE_PREVIEW") + " " + fileName);
        JPanel buttonpanel = buildButtons();
        containsHeaders = new JCheckBox(getResourceString("COLUMN_HEAD"));
        containsHeaders.setSelected(true);
        //containsHeaders.addActionListener(this);   
        containsHeaders.addItemListener(new CheckboxItemListener());
          
        builder.addSeparator(getResourceString("FILE_IMPORT"),      cc.xyw(2,2,4));
        builder.add         (fileInfo,                              cc.xyw(3,4,4));
        builder.add         (containsHeaders,                       cc.xyw(3,6,3));   
        
        builder.addSeparator(getResourceString("DATA_PREVIEW"),     cc.xyw(2,8,4));
        myDisplayTable = setTableData(myDisplayTable);
        builder.add         (addtoScroll(myDisplayTable),           cc.xyw(3,10,3));       
        builder.add         (buttonpanel,                           cc.xyw(2,12,4)); 
        configPanel.setMinimumSize(buttonpanel.getMinimumSize());
        return configPanel;
    }
    private JPanel buildButtons()
    {
        cancelBtn = new JButton(getResourceString("Cancel"));
        okBtn = new JButton(getResourceString("OK"));
        helpBtn = new JButton(getResourceString("Help"));

        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                isCancelled = true;
                btnPressed  = CANCEL_BTN;
                setVisible(false);
            }
        });

        okBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                    isCancelled = false;
                    btnPressed  = OK_BTN;
                    setVisible(false);
            }
        });
        
        helpBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                isCancelled = false;
                btnPressed  = HELP_BTN;
                setVisible(false);
            }
        });

        getRootPane().setDefaultButton(okBtn);
        HelpMgr.registerComponent(helpBtn, "configcsv");
        return  ButtonBarFactory.buildOKCancelHelpBar(okBtn, cancelBtn, helpBtn);
    }
    
    private JPanel createOtherControlsPanel()
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
    
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        log.debug("itemStateChanged");
        if (!(source == containsHeaders)) 
        {
        	log.error("Unexpected checkbox source");
        }

        if (e.getStateChange() == ItemEvent.DESELECTED)
        {
        	doesFirstRowHaveHeaders = false;
        }
        else if(e.getStateChange() == ItemEvent.SELECTED)
        {
        	doesFirstRowHaveHeaders = true;  	
        }
        updateTableDisplay();
        
    }
    
    /** Listens to the combo box. */
    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        String str = (String)cb.getSelectedItem();
        log.debug("actionPerformed");
        if(str.equals("\""))
        {
            stringQualifierChar = '\"';         
        }
        else if(str.equals("\'"))
        {
            stringQualifierChar = '\'';
        }
        else if(str.equals("US-ASCII") || str.equals("ISO-8859-1") || str.equals("UTF-8"))// || str.equals(anObject))
        {
        	charset = Charset.forName(str);      	
        }
        else if(str.equals("DEFAULT"))
        {
        	charset = Charset.defaultCharset(); 
        }
        else if(str.equals("backslash"))
        {
        	escapeMode = CsvReader.ESCAPE_MODE_BACKSLASH;        	
        }
        else if(str.equals("doubled"))
        {
        	escapeMode = CsvReader.ESCAPE_MODE_DOUBLED;
        }
        updateTableDisplay();
        setTableData(myDisplayTable);
    }
    private JPanel createDelimiterPanel()
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
        tab.addItemListener(new DelimButtonItemListener());
        tab.setBackground(newColor);
        
        space = new JRadioButton(getResourceString("SPACE"));
        space.addItemListener(new DelimButtonItemListener());
        space.setBackground(newColor);

        comma = new JRadioButton(getResourceString("COMMA"));
        comma.addItemListener(new DelimButtonItemListener());
        comma.setSelected(true);
        comma.setBackground(newColor);
        
        semicolon = new JRadioButton(getResourceString("SEMICOLON"));
        semicolon.addItemListener(new DelimButtonItemListener());
        semicolon.setBackground(newColor);
        
        other = new JRadioButton(getResourceString("OTHER"));
        other.addItemListener(new DelimButtonItemListener());
        other.setBackground(newColor);
        
        otherText = new JTextField();
        otherText.addKeyListener(new CharFieldKeyAdapter());
        otherText.setColumns(1);
        otherText.setEnabled(false);
        otherText.setEditable(false);
        otherText.setDocument(new CharLengthLimitDocument(1));//limits the textfield to only allowing on character
        
        ButtonGroup group = new ButtonGroup();
        group.add(tab);
        group.add(space);
        group.add(other);
        group.add(comma);
        group.add(semicolon);
        
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
    
    //public void createDummyCsvReader()
    //{
    	//ConfigureCSV testcsv = new ConfigureCSV(file);
//        CsvReader csv = new CsvReader(new FileInputStream(config.getFile()), config.getDelimiter(), config.getCharset());
//        csv.setEscapeMode(config.getEscapeMode());
//        csv.setTextQualifier(config.getTextQualifier());
    //}
    
//    private JScrollPane createTablePreview()
//    {
//        
//        Object[] columnNames = { "First Name", "Last Name", "Sport", "First Name", "Last Name", "Sport","First Name", "Last Name", "Sport", "# of Years", "Vegetarian" };
//
//        Object[][] data = {
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Philip", "Milne", "Pool",  new Integer(10), new Boolean(false) } ,
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Philip", "Milne", "Pool",  new Integer(10), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Philip", "Milne", "Pool",  new Integer(10), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Philip", "Milne", "Pool",  new Integer(10), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
//                { "Mary bary", "Campione", "Snowboarding","Mary bary", "Campione", "Snowboarding","Philip", "Milne", "Pool",  new Integer(10), new Boolean(false) }};
//
//        //JTable myJTable = new JTable(data, columnNames);
//        JTable t = new JTable(data, columnNames);
//        
//        t.setColumnSelectionAllowed(false);
//        t.setRowSelectionAllowed(false);
//        t.setCellSelectionEnabled(false);
//        t.setPreferredScrollableViewportSize(t.getPreferredSize());
//        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        //t.setPreferredScrollableViewportSize(new Dimension(200,100));
//        //initColumnSizes(t);
//        //Dimension size = t.getPreferredScrollableViewportSize();
//        //t.setPreferredScrollableViewportSize
//        //    (new Dimension(100,100));
//        //t.setShowGrid(true);
//        //t.getModel().i
//        //t.setModel(new PreviewTableModel());
//        JScrollPane pane = new JScrollPane(t, 
//                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
//                 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//        pane.setPreferredSize(new Dimension(500,100));
//        //pane.add(t);
//        //pane.set
//        //pane.p
//
//        return pane;
//    }
    
    private void updateTableDisplay()
    {
    	config.setFirstRowHasHeaders(doesFirstRowHaveHeaders);
    	config.setTextQualifier(stringQualifierChar);
    	config.setCharset(charset);
    	config.setEscapeMode(escapeMode);
    	setTableData(myDisplayTable);   
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
    	new ConfigureCSV(new File("johsoncountyTrip.csv"));
//        DataImportDialog dlg = new DataImportDialog(new ConfigureCSV(new File("johsoncountyTrip.csv")));
//		dlg.setEscapeMode(1);
//		dlg.setDelimChar(',');
//		dlg.setStringQualifierChar('\"');
//		dlg.setCharset(Charset.defaultCharset());
//		dlg.initForCSV();
//		dlg.setModal(true);
//		UIHelper.centerAndShow(dlg);
//		char delimiter = dlg.getDelimChar();
//		Charset charset = dlg.getCharset();
//		int escapeMode = dlg.getEscapeMode();
//		boolean firstRowHasHeaders = dlg.getDoesFirstRowHaveHeaders();
//		char textQualifier = dlg.getStringQualifierChar();
//
//		log.debug("delim: " + delimiter);
//		log.debug("charset: " + charset);
//		log.debug("escapemode: " + escapeMode);
//		log.debug("furst row has headers: " + firstRowHasHeaders);
//		log.debug("textqualifier: " + textQualifier);// charset = dlg.getC
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
                config.setDelimiter(delimChar);
                log.debug("Other value selected for delimiter: ["+ delimChar +"]" );
            }            
            else if(otherText.getText().length()==0)
            {
                //delimChar = ',';
                //log.debug("Other value cleared for delimiter setting to default: ["+ delimChar +"]" );
            }
            else
            {
                log.error("Other field should not allow more that one character as a delimiter");
            }
            updateTableDisplay();
        }
    }
    
    public int getLargestColumnCount()
    {
    	try
    	{
		CsvReader csv = new CsvReader(new FileInputStream(config.getFile()), config
				.getDelimiter(), config.getCharset());
		csv.setEscapeMode(config.getEscapeMode());
		csv.setTextQualifier(config.getTextQualifier());	
		int curRowColumnCount = 0;
		int highestColumnCount = 0;
		if(config.getFirstRowHasHeaders())
		{
			csv.readHeaders();
			highestColumnCount = Math.max(highestColumnCount, csv.getHeaders().length);
			//;
		}
		while (csv.readRecord())
		{
			curRowColumnCount = csv.getColumnCount();
			highestColumnCount = Math.max(highestColumnCount, curRowColumnCount);
			
		}
		return highestColumnCount;
    	}
    	catch(Exception e)
    	{
    		
    	}
    	return 0;
    }
    
    public  String[] padArray(int desiredSize, String[] array)
    {
    	
    	//int arraySize =;
    	if( array.length < desiredSize)
    	{
    		String[] newArray = new String[desiredSize];
    		//int i = 0;
    		int highI = 0;
    		for(int i = 0; i < array.length; i++)
    		{
    			newArray[i] = array[i];
    			highI = i;
    		}
    		for(int i = highI; i< desiredSize; i++)
    			
    		{
    			newArray[i] = "";
    		}
    		return newArray;
    	}
    	return array;
    	
    }
    
    /*
     * (non-Javadoc) Loads data from the file configured by the config member into a workbench.
     * @see edu.ku.brc.specify.tasks.subpane.wb.DataImportIFace#getData(edu.ku.brc.specify.datamodel.Workbench)
     */
    public JTable setTableData(JTable t)
	{
		try
		{
			log.debug("getDAta =- fiele - " + config.getFile().toString());
			CsvReader csv = new CsvReader(new FileInputStream(config.getFile()), config
					.getDelimiter(), config.getCharset());
			csv.setEscapeMode(config.getEscapeMode());
			csv.setTextQualifier(config.getTextQualifier());

			String[] headers = {};

			Vector<String[]> tableDataVector = new Vector<String[]>();
			
			int highestColumnCount = getLargestColumnCount();

			if (config.getFirstRowHasHeaders())
			{
				csv.readHeaders();
				headers = csv.getHeaders();
			}
			padArray(highestColumnCount, headers);

			int rowColumnCount = 0;
			int rowCount = 0;
			while (csv.readRecord())
			{
				rowColumnCount = csv.getColumnCount();
				log.debug("RowColumnCount:" + rowColumnCount);
				String[] rowData = new String[csv.getColumnCount()];
				for (int col = 0; col < csv.getColumnCount(); col++)
				{
					rowData[col] = csv.get(col);
				}
				padArray(highestColumnCount, rowData);
				
				tableDataVector.add(rowData);
				rowCount++;
			}

			if (!config.getFirstRowHasHeaders() || headers == null)
			{
				headers = new String[rowColumnCount];
				for (int i = 0; i < rowColumnCount; i++)
				{ 
					headers[i] = "Column " + i;
				}
				padArray(highestColumnCount, headers);

			}

			String[][] tableData = new String[tableDataVector.size()][rowColumnCount];
			for (int i = 0; i < tableData.length; i++)
			{
				tableData[i] = (String[]) tableDataVector.elementAt(i);
				printArray(tableData[i]);
			}

			//model.setHeaders(headers);
			//model.setData(tableData);
			model = new PreviewTableModel(headers, tableData);
			t.setModel(model);
			t.setColumnSelectionAllowed(false);
			t.setRowSelectionAllowed(false);
			t.setCellSelectionEnabled(false);
			t.setPreferredScrollableViewportSize(t.getPreferredSize());
			t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			model.fireTableDataChanged();
			model.fireTableStructureChanged();
			//model.fire
			//t.repaint();
			return t;

		} catch (IOException ex)
		{
			log.error(ex);
		}

		return null;
	}
    
    public void printArray(String[] arrayList)
	{
		System.out.println();
		for (int i = 0; i < arrayList.length; i++)
		{
			System.out.print("[" + (i) + "]" + arrayList[i] + " ");
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
    private class DelimButtonItemListener implements ItemListener
    {
        public DelimButtonItemListener()
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
            if(other==null)return;
            if (other.isSelected())
            {
                otherText.setEditable(true);
                otherText.requestFocus();
                otherText.setEnabled(true);
                if(otherText.getText().length() == 1)
                {                
                    delimChar = otherText.getText().toCharArray()[0];
                    config.setDelimiter(delimChar);
                    //log.debug("Other value selected for delimiter: ["+ delimChar +"]" );
                }  
            } else if(tab.isSelected())
            {
                delimChar = '\t';
                //otherText.setEditable(false);
                otherText.setEnabled(false);
            }else if(space.isSelected())
            {
                delimChar = ' ';
                otherText.setEditable(false);
                otherText.setEnabled(false);
            }
            else if(semicolon.isSelected())
            {
                delimChar = ';';
                otherText.setEditable(false);
                otherText.setEnabled(false);
            }
            else if(comma.isSelected())
            {
                delimChar = ',';
                otherText.setEditable(false);
                otherText.setEnabled(false);
            }
            else
            {
                otherText.setEditable(false);
                otherText.setEnabled(false);
            }
            config.setDelimiter(delimChar);
            updateTableDisplay();
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
        	log.debug("itemStateChanges");
        	//config
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
            	log.debug("itemStateChanges - SELECTED");
            	doesFirstRowHaveHeaders = true;
            	config.setFirstRowHasHeaders(true);
            }
            else
            {
            	log.debug("itemStateChanges - other");
            	doesFirstRowHaveHeaders = false;
            	config.setFirstRowHasHeaders(false);
            }

            updateTableDisplay();
        }
    }  
    
    class PreviewTableModel extends AbstractTableModel
    {
        private String[] columnNames = {};
        private String[][] data = {{}};
        
        /**
		 * 
		 */
		public PreviewTableModel()
		{
			super();
			// TODO Auto-generated constructor stub
		}

		public PreviewTableModel(String[] headers, String[][]data)
        {
        	super();
        	this.columnNames = headers;
        	this.data = data;
        }
		
	    public void setValueAt(Object value, int row, int col) {
	        data[row][col] = value.toString();
	        fireTableCellUpdated(row, col);
	    }

    	/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount()
		{
			return columnNames.length;
			// TODO Auto-generated method stub
			//return 0;
		}
        public String getColumnName(int col) {
            return columnNames[col];
        }
		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount()
		{
			// TODO Auto-generated method stub
			//return 0;
			return data.length;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			// TODO Auto-generated method stub
			return data[rowIndex][columnIndex];
			//return null;
		}

		public boolean isCellEditable(int row, int column)
        {
            return false;
        }

		/**
		 * @return the headers
		 */
		public String[] getColumnNames()
		{
			return this.columnNames;
		}

		/**
		 * @param headers the headers to set
		 */
		public void setColumnNames(String[] headers)
		{
			this.columnNames = headers;
			//this.
		}

		/**
		 * @return the data
		 */
		public String[][] getData()
		{
			return this.data;
		}

		/**
		 * @param data the data to set
		 */
		public void setData(String[][] data)
		{
			this.data = data;
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

	/**
	 * @return the charset
	 */
	public Charset getCharset()
	{
		return this.charset;
	}

	/**
	 * @param charset the charset to set
	 */
	public void setCharset(Charset charset)
	{
		this.charset = charset;
	}

	/**
	 * @return the escapeMode
	 */
	public int getEscapeMode()
	{
		return this.escapeMode;
	}

	/**
	 * @param escapeMode the escapeMode to set
	 */
	public void setEscapeMode(int escapeMode)
	{
		this.escapeMode = escapeMode;
	}

	/**
	 * @return the doesFirstRowHaveHeaders
	 */
	public boolean getDoesFirstRowHaveHeaders()
	{
		return this.doesFirstRowHaveHeaders;
	}

	/**
	 * @param doesFirstRowHaveHeaders the doesFirstRowHaveHeaders to set
	 */
	public void setDoesFirstRowHaveHeaders(boolean doesFirstRowHaveHeaders)
	{
		this.doesFirstRowHaveHeaders = doesFirstRowHaveHeaders;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName()
	{
		return this.fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
}
