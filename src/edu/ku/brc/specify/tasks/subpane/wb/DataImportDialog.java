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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.csvreader.CsvReader;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MouseOverJLabel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * Class that provides "fancy" dialog for importing data from csv or XLS,
 * allowing users to specify such information as delimiters, text qualifiers,
 * character sets, escape modes, etc.  Dialog provides a preview table
 * displaying how the data will appear when imported into a spreadsheet table.
 * 
 * @author megkumin
 *
 * @code_status Alpha
 *
 * Created Date: Mar 26, 2007
 *
 */
@SuppressWarnings("serial")
public class DataImportDialog extends JDialog implements ActionListener
{
	private static final Logger log = Logger.getLogger(DataImportDialog.class);
	private JButton cancelBtn;
	private JButton okBtn;
	private JButton helpBtn;
	
	public static final int OK_BTN = 1;
	public static final int CANCEL_BTN = 2;
	public static final int HELP_BTN = 4;
	protected int btnPressed = CANCEL_BTN;
	
	private JRadioButton tab;
	private JRadioButton space;
	private JRadioButton semicolon;
	private JRadioButton comma;
	private JRadioButton other;
	private JTextField otherText;
	
	private char stringQualifierChar;
	private char delimChar;
	private Charset charset;
	private int escapeMode;
	private boolean doesFirstRowHaveHeaders;
    private boolean shouldUseTextQualifier;

	private JLabel textQualLabel;
	private JComboBox textQualCombo;
	private JLabel charSetLabel;
	private JComboBox charSetCombo;
	private JLabel escapeModeLabel;
	private JComboBox escapeModeCombo;

	private JCheckBox containsHeaders;

	private boolean isCancelled = false;
	private boolean hasTooManyRows = false;

	private String fileName;
	private File file;
	private ConfigureExternalDataBase config;
    private ConfigureCSV configCSV;
    private ConfigureXLS configXLS;
	
	private JTable myDisplayTable;
	private PreviewTableModel model;
	private DataErrorPanel errorPanel = new DataErrorPanel();
   
    private int highestColumnCount;
    /**
     * Constructor for Import Dialog for a csv
     * 
     * @param config - the config class for configing a csv import
     * @param defaultDelimChar - default delimiter to be used
     * @param defaultTextQual - default text qualifer to be used
     * @param defaultCharSet - default character set to be used
     * @param defaultEscMode - default escape mode to be used
     * @param doesHaveHeaders - default for whether or not the data file contains headers in the first row.
     * @param useTxtQual
     */
    public DataImportDialog(final ConfigureCSV config, char defaultDelimChar, 
                            char defaultTextQual, Charset defaultCharSet,
                            int defaultEscMode, boolean doesHaveHeaders, boolean useTxtQual)
    {
    	this.config = config;
        this.configCSV = config;
    	this.file = config.getFile();
    	this.fileName = file.getAbsolutePath().toString();
        this.doesFirstRowHaveHeaders = doesHaveHeaders;
        this.charset = defaultCharSet;
        this.escapeMode = defaultEscMode;
        this.delimChar = defaultDelimChar;
        this.stringQualifierChar = defaultTextQual;
        this.shouldUseTextQualifier = useTxtQual;
        highestColumnCount = 0;
        myDisplayTable = new JTable();
        model = new PreviewTableModel();
        createUiForCSV();
    }
    
    /**
     * Constructor for Import dialog for xls
     * 
     * @param config - the config class for configing an xls import
     * @param doesHaveHeaders
     */
    public DataImportDialog(final ConfigureXLS config, boolean doesHaveHeaders)
	{
        this.config = config;
        this.configXLS = config;
        this.file = config.getFile();
        this.fileName = file.getAbsolutePath().toString();
        this.doesFirstRowHaveHeaders = doesHaveHeaders;
        myDisplayTable = new JTable();
        model = new PreviewTableModel();
        createUiForXLS();
	}

    /**
     * Initialize UI for a csv import
     * 
     * void
     */
    private void createUiForCSV()
    {
    	JPanel p = createConfigPanelForCSV();
    	setContentPane(p);  
    	if(!hasTooManyRows)
    	{
    		init(getResourceString("IMPORT_CVS"));    
    	}
    	else
    	{
    		isCancelled = true;
    	}
    }
      

    /**
     * Initialize UI for an XLS import
     * 
     * void
     */
    @SuppressWarnings("unused")
    private void createUiForXLS()
    {
    	JPanel p = createConfigPanelForXLS();
    	setContentPane(p);
    	if(!hasTooManyRows)
    	{
    		init(getResourceString("IMPORT_XLS"));    
    	}
    	else
    	{
    		isCancelled = true;
    	}
    }
    
    /**
     * General init ui method
     * 
     * @param title - the title of the dialog
     * void
     */
    private void init(String title)
    {
        setTitle(title);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setModal(true);
        UIHelper.centerAndShow(this);
    }
    
    /**
     * Creates the UI panel for a csv import, displays config opionts.
     * 
     * @return
     * JPanel - the panel to be displayed
     */
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
                "f:p:g,5px," + 	//tablePreview
                "30px,3px,"+    //data import status error panel
                "p,10px" 		//buttongs
                ), configPanel);// rows

        JLabel fileInfo = new JLabel(getResourceString("FILE_PREVIEW") + " " + fileName);
        JPanel buttonpanel = buildButtons();
        containsHeaders = new JCheckBox(getResourceString("COLUMN_HEAD"));
        containsHeaders.setSelected(true);
        containsHeaders.addItemListener(new CheckboxItemListener());

        builder.addSeparator(getResourceString("DATA_IMPORT_OPS"),  cc.xyw(2,2,4)); 
        builder.add         (createDelimiterPanel(),                cc.xy (3,4));        
        builder.add         (createOtherControlsForCSVPanel(),            cc.xy (5,4));       
          
        builder.addSeparator(getResourceString("FILE_IMPORT"),      cc.xyw(2,6,4));
        builder.add         (fileInfo,                              cc.xyw(3,8,4));
        builder.add         (containsHeaders,                       cc.xyw(3,10,3));   
        
        builder.addSeparator(getResourceString("DATA_PREVIEW"),     cc.xyw(2,12,4));
        
        myDisplayTable = setCSVTableData(myDisplayTable);
        builder.add         (addtoScroll(myDisplayTable),           cc.xyw(3,14,3));   

        builder.add         (errorPanel,                            cc.xyw(3,16,4));  
        builder.add         (buttonpanel,                           cc.xyw(2,18,4)); 
        configPanel.setMinimumSize(buttonpanel.getMinimumSize());
        return configPanel;
    }

    /**
     * Creates the UI panel for a xls import
     * 
     * @return
     * JPanel
     */
    private JPanel createConfigPanelForXLS()
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
                "f:p:g,5px," + 	//tablePreview
                "30px,3px,"+    //data import status error panel
                "p,10px" 		//buttongs
                ), configPanel);// rows

        JLabel fileInfo = new JLabel(getResourceString("FILE_PREVIEW") + " " + fileName);
        JPanel buttonpanel = buildButtons();
        containsHeaders = new JCheckBox(getResourceString("COLUMN_HEAD"));
        containsHeaders.setSelected(true);
        containsHeaders.addItemListener(new CheckboxItemListener());
          
        builder.addSeparator(getResourceString("FILE_IMPORT"),      cc.xyw(2,2,4));
        builder.add         (fileInfo,                              cc.xyw(3,4,4));
        builder.add         (containsHeaders,                       cc.xyw(3,6,3));   
        
        builder.addSeparator(getResourceString("DATA_PREVIEW"),     cc.xyw(2,8,4));
        myDisplayTable = setXLSTableData(myDisplayTable);
        builder.add         (addtoScroll(myDisplayTable),           cc.xyw(3,10,3));
        builder.add         (errorPanel,                            cc.xyw(3,12,4));  
        builder.add         (buttonpanel,                           cc.xyw(2,14,4)); 
        configPanel.setMinimumSize(buttonpanel.getMinimumSize());
        return configPanel;
    }
    
    /**
     * Builds okay, cancel, help button bar.
     * @return
     * JPanel
     */
    private JPanel buildButtons()
    {
        cancelBtn = new JButton(getResourceString("Cancel"));
        okBtn = new JButton(getResourceString("OK"));
        helpBtn = new JButton(getResourceString("Help"));

        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("User cancelled DataImportDialog");
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
                    config.setFirstRowHasHeaders(doesFirstRowHaveHeaders);
                    if (config instanceof ConfigureCSV)
                    {
                        configCSV = (ConfigureCSV)config;
                        configCSV.setTextQualifier(true, stringQualifierChar);
                        configCSV.setCharset(charset);
                        configCSV.setEscapeMode(escapeMode);
                        configCSV.setDelimiter(delimChar);
                    }
            }
        });
        
        helpBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("User okayed DataImportDialog");
                isCancelled = false;
                btnPressed  = HELP_BTN;
                setVisible(false);
            }
        });

        getRootPane().setDefaultButton(okBtn);
        HelpMgr.registerComponent(helpBtn, "configcsv");
        return  ButtonBarFactory.buildOKCancelHelpBar(okBtn, cancelBtn, helpBtn);
    }
    
    /**
     * Creates panel that displays the combox boxes for the text qualifier,
     * Character Set, and Escape Mode for CSV import
     * @return
     * JPanel - the panel to display
     */
    private JPanel createOtherControlsForCSVPanel()
    {
        JPanel myPanel = new JPanel();
        CellConstraints cc = new CellConstraints();
        FormLayout formLayout = new FormLayout(
                "p,3px, p,3px",
                "p,4px,   p,4px,  p,4px,  p,4px");
        PanelBuilder builder = new PanelBuilder(formLayout, myPanel);

        textQualLabel = new JLabel(getResourceString("TEXT_QUAL"));
        String[] qualifiers = { "\"", "\'", "{"+getResourceString("NONE")+"}" };
        textQualCombo = new JComboBox(qualifiers);
        textQualCombo.setSelectedIndex(0);
        textQualCombo.addActionListener(this);

        charSetLabel = new JLabel(getResourceString("CHAR_SET"));
        String[] charsets = { getResourceString("DEFAULT"), "US-ASCII", "ISO-8859-1", "UTF-8" };
        charSetCombo = new JComboBox(charsets);
        charSetCombo.setSelectedIndex(0);
        charSetCombo.addActionListener(this);
        
        escapeModeLabel = new JLabel(getResourceString("ESCAPE_MODE"));
        String[] escapeModes = {getResourceString("BACKSLASH"), getResourceString("DOUBLED")};
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
    

    /* 
     * Does actionPerformed on all of the Combo box selections
     * 
     * (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        //Object source = e.g
        String str = (String)cb.getSelectedItem();
        log.debug("actionPerformed");
        if (str.equals("\""))
        {
            stringQualifierChar = '\"';         
        }
        else if (str.equals("\'"))
        {
            stringQualifierChar = '\'';
        }
        else if (str.equals("{"+getResourceString("NONE")+"}" ))
        {
            
        }
        else if (str.equals("US-ASCII") || 
        		str.equals("ISO-8859-1") || 
        		str.equals("UTF-8"))
        {
        	charset = Charset.forName(str);      	
        }
        else if (str.equals(getResourceString("DEFAULT")))
        {
        	charset = Charset.defaultCharset(); 
        }
        else if (str.equals(getResourceString("BACKSLASH")))
        {
        	escapeMode = CsvReader.ESCAPE_MODE_BACKSLASH;        	
        }
        else if (str.equals(getResourceString("DOUBLED")))
        {
        	escapeMode = CsvReader.ESCAPE_MODE_DOUBLED;
        }
        updateTableDisplay();
    }
    
    /**
     * Creates ui panel for allowing user to select delimiters for csv import
     * @return
     * JPanel
     */
    private JPanel createDelimiterPanel()
    {      
        JPanel myPanel = new JPanel();
        CellConstraints cc = new CellConstraints();
        FormLayout formLayout = new FormLayout(
                "p,p,2px, p:g,2px, p,2px,p,2px,",
                "p,1px,   p,1px, p,1px , p,1px , p,1px , p,1px, p,1px ");
        PanelBuilder builder = new PanelBuilder(formLayout, myPanel);
        //Color curColor = myPanel.getBackground();
        //Color newColor = curColor;//.brighter();
           
        tab = new JRadioButton(getResourceString("TAB"));
        tab.addItemListener(new DelimButtonItemListener());
        //tab.setBackground(newColor);
        
        space = new JRadioButton(getResourceString("SPACE"));
        space.addItemListener(new DelimButtonItemListener());
        //space.setBackground(newColor);

        comma = new JRadioButton(getResourceString("COMMA"));
        comma.addItemListener(new DelimButtonItemListener());
        comma.setSelected(true);
        //comma.setBackground(newColor);
        
        semicolon = new JRadioButton(getResourceString("SEMICOLON"));
        semicolon.addItemListener(new DelimButtonItemListener());
        //semicolon.setBackground(newColor);
        
        other = new JRadioButton(getResourceString("OTHER"));
        other.addItemListener(new DelimButtonItemListener());
        //other.setBackground(newColor);
        
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
        
        //myPanel.setBackground(newColor);
        return myPanel;
    }
    
    /**
     * 
     * void
     */
    private void updateTableDisplay()
    {
        if (config instanceof ConfigureCSV)
        {
            setCSVTableData(myDisplayTable);
        }
        else 
        {
            setXLSTableData(myDisplayTable);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
    	new ConfigureCSV(new File("demo_files\\workbench\\johsoncountyTrip.csv"));
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
     * Checks if data that is being imported will encounter issues during import,
     * due to field size constraints in teh data base.  Creates a list of errors to
     * be displayed to the user
     * 
     * @param headers - the column names
     * @param data - the table data
     * @return
     * boolean whether the data contains errors
     */
    private boolean checkForErrors(String[]headers, String[][]data)
    {
        JList listOfErrors = genListOfErrorWhereTableDataDefiesSizeConstraints(headers, data);
		if (listOfErrors == null)
		{
			return false;
		}
		if (listOfErrors.getModel().getSize() > 0)
		{
			return true;
		}
		return false;
    }
    /**
	 * Takes the list of data import errors and displays then to the user
	 * 
	 * void
	 */
    private void showErrors()
    {
        JList listOfErrors = genListOfErrorWhereTableDataDefiesSizeConstraints(model.getColumnNames(), model.data);
        
        if ((model.getColumnNames() ==null )|| (model.data == null) || (listOfErrors==null) || (listOfErrors.getModel().getSize() == 0))
        {
            JTextArea textArea = new JTextArea();
            textArea.setRows(25);
            textArea.setColumns(60);
            //String newline = "\n";
            //for (int i = 0; i < listOfErrors.getModel().getSize(); i++)
            //{
                textArea.append("The imported file is in the incorrect format and cannot be imported.");//TODO i8n
            //}
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setCaretPosition(0);
            JScrollPane pane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            JOptionPane.showMessageDialog(UIRegistry.get(UIRegistry.TOPFRAME), pane,getResourceString("DATA_IMPORT_ISSUES"),JOptionPane.WARNING_MESSAGE);
             	
        }
        else if (listOfErrors.getModel().getSize() > 0)
        {
            JTextArea textArea = new JTextArea();
            textArea.setRows(25);
            textArea.setColumns(60);
            String newline = "\n";
            for (int i = 0; i < listOfErrors.getModel().getSize(); i++)
            {
                textArea.append((String) listOfErrors.getModel().getElementAt(i) + newline
                        + newline);
            }
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setCaretPosition(0);
            JScrollPane pane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            JOptionPane.showMessageDialog(UIRegistry.get(UIRegistry.TOPFRAME), pane,getResourceString("DATA_IMPORT_ISSUES"),JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Reads in the data import file and determines the largest number of columns in the
     * dataset, including headers.  Is used to determine how large to make the 
     * table
     * @return
     * int - the largest number of columns encounterd
     */    
    public int getLargestColumnCountFromCSV()
    {
    	try
		{
			CsvReader csv = new CsvReader(new FileInputStream(configCSV.getFile()), configCSV.getDelimiter(), configCSV.getCharset());
			csv.setEscapeMode(configCSV.getEscapeMode());
			csv.setTextQualifier(configCSV.getTextQualifier());
			int curRowColumnCount = 0;
			int highestColumnCount = 0;
			//seeing how many columns are defined
			if (configCSV.getFirstRowHasHeaders())
			{
				csv.readHeaders();
				highestColumnCount = Math.max(highestColumnCount, csv.getHeaders().length);
			}
			//see what the largest number of columns is per row of data
			while (csv.readRecord())
			{
				curRowColumnCount = csv.getColumnCount();
				highestColumnCount = Math.max(highestColumnCount, curRowColumnCount);

			}
			return highestColumnCount;
		} 
    	catch (Exception e)
		{
    		log.error("Error attempting to parse input csv file:" + e);
		}
		return 0;
    }
    
    /**
     * Checks to see if the String length is shorter than the given value
     * @param length
     * @param colName
     * @return
     * boolean whether the string is shorter than the given value
     */
    private boolean isStringShorterThan(int length, String colName)
    {
        if (colName==null)
        {
            return true;
        }
        if (colName.length()<= length)
        {
            return true;
        }
        return false;
    }
  
    /**
     * Parses the given import xls file according to the users selection and creates/updates the Preview table,
     * showing the user how the import options effect the way the data will be imported into the spreadsheet.
     * @param t - the table to display the data
     * @return
     * JTable - the table to display the data
     */
    private JTable setXLSTableData(JTable t)
    {
        int numRows = 0;
        short numCols = 0;
        String[] headers = {};
        Vector<Vector<String>> tableDataVector = new Vector<Vector<String>>();
        Vector<String> rowData = new Vector<String>();
        Vector<String> headerVector = new Vector<String>();
        DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
        try
        {
            log.debug("setXLSTableData - file - " + configXLS.getFile().toString());

            InputStream input = new FileInputStream(configXLS.getFile());
            POIFSFileSystem fs = new POIFSFileSystem(input);
            HSSFWorkbook workBook = new HSSFWorkbook(fs);
            HSSFSheet sheet = workBook.getSheetAt(0);

            boolean firstRow = true;

            // Iterate over each row in the sheet
            Iterator rows = sheet.rowIterator();
            while (rows.hasNext())
            {
                numCols = 0;
                rowData = new Vector<String>();
                HSSFRow row = (HSSFRow) rows.next();
                while (numCols < row.getLastCellNum())
                {
                    HSSFCell cell = (HSSFCell) row.getCell(numCols);
                    String value = null;
                    //if cell is blank, set value to ""
                    if (cell == null)
                    {
                        value = "";
                    }
                    else
                    {
                        switch (cell.getCellType())
                        {
                            case HSSFCell.CELL_TYPE_NUMERIC:
                                //The best I can do at this point in the app is to guess if a cell is a date.
                                //Handle dates carefully while using HSSF. Excel stores all dates as numbers, internally. 
                                //The only way to distinguish a date is by the formatting of the cell. (If you
                                //have ever formatted a cell containing a date in Excel, you will know what I mean.)
                                //Therefore, for a cell containing a date, cell.getCellType() will return 
                                //HSSFCell.CELL_TYPE_NUMERIC. However, you can use a utility function, 
                                //HSSFDateUtil.isCellDateFormatted(cell), to check if the cell can be a date. 
                                //This function checks the format against a few internal formats to decide the issue, 
                                //but by its very nature it is prone to false negatives. 
                                if (HSSFDateUtil.isCellDateFormatted(cell))
                                {
                                    value = scrDateFormat.getSimpleDateFormat().format(
                                            cell.getDateCellValue());
                                } else
                                {
                                    value = Double.toString(cell.getNumericCellValue());
                                }
                                break;

                            case HSSFCell.CELL_TYPE_STRING:
                                value = cell.getStringCellValue();
                                break;

                            case HSSFCell.CELL_TYPE_BLANK:
                                value = "";
                                break;

                            case HSSFCell.CELL_TYPE_BOOLEAN:
                                value = Boolean.toString(cell.getBooleanCellValue());
                                break;

                            default:
                                value = "";
                                System.out.println("unsuported cell type");
                                break;
                        }
                    }

                    rowData.add(value.toString());
                    numCols++;
                }
                if (doesFirstRowHaveHeaders && firstRow)
                {
                    headerVector = rowData;
                    headers = new String[rowData.size()];
                }
                else if (!doesFirstRowHaveHeaders && firstRow){
                    //headers = createDummyHeaders(rowData.size());
                    headerVector = createDummyHeadersAsVector(rowData.size());
                    headers = new String[rowData.size()];
                    tableDataVector.add(rowData);
                }
                else
                {
                    tableDataVector.add(rowData);
                }
                firstRow = false;
                numRows++;

            }
            for (int i = 0; i < headerVector.size(); i++)
            {
                headers[i] = (String) headerVector.elementAt(i);

            }
            printArray(headers);

            String[][] tableData = new String[tableDataVector.size()][headers.length];
            for (int i = 0; i < tableDataVector.size(); i++)
            {
                Vector<String> v = tableDataVector.get(i);
                for (int j = 0; j < v.size(); j++)
                {
                    tableData[i][j] = v.get(j).toString();
                }

            }
            if (checkForErrors(headers, tableData))
            {
                errorPanel.showDataImportStatusPanel(true);
            } else
            {
                errorPanel.showDataImportStatusPanel(false);
            }
            
            if(numRows >= WorkbenchTask.MAX_ROWS)
            {
            	showTooManyRowsErrorDialog();
            }
            model = new PreviewTableModel(headers, tableData);
            t.setModel(model);
            t.setColumnSelectionAllowed(false);
            t.setRowSelectionAllowed(false);
            t.setCellSelectionEnabled(false);
            t.getTableHeader().setReorderingAllowed(false);
            t.setPreferredScrollableViewportSize(new Dimension(500, 100));
            t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            model.fireTableDataChanged();
            model.fireTableStructureChanged();
            return t;

        } catch (IOException ex)
        {
            //log.error("Error attempting to parse input xls file:" + ex);
            //ex.printStackTrace();
        }

        return null;       
    }
    
    private void showTooManyRowsErrorDialog()
    {
        PanelBuilder    builder = new PanelBuilder(new FormLayout("p", "c:p:g"));
        CellConstraints cc      = new CellConstraints();

        //builder.add(new JLabel(IconManager.getIcon("SpecifyLargeIcon")), cc.xy(1,1));
        builder.add(new JLabel("<html>"
        		+"The preview release of the Specify 6.0 Workbench"
        		+"<br>"
        		+"can only import 2000 rows of data.  The file you "
        		+"<br>"
        		+"are attempting to import contains too many rows."
        		+"<br>"
        		+"Please select another file."
        		+"</html>"), cc.xy(1,1)); //TODO i8n

        CustomDialog maxRowExceededMsg = new CustomDialog((Frame)UIRegistry.get(UIRegistry.FRAME), getResourceString("WB_MAXROWS") , true, CustomDialog.OK_BTN, builder.getPanel());
        maxRowExceededMsg.setOkLabel(getResourceString("Close"));
        UIHelper.centerAndShow(maxRowExceededMsg);
    	
        hasTooManyRows = true;
        
        //okBtn.setEnabled(false);   	
    }
    
    /**
     * Parses the given import file according to the users selection and creates/updates the Preview table,
     * showing the user how the import options effect the way the data will be imported into the spreadsheet.
     * @param t - the table to display the data
     * @return
     * JTable - the table to display the data
     */
    private JTable setCSVTableData(JTable t)
	{
		try
		{
			log.debug("setTableData - file - " + configCSV.getFile().toString());
			CsvReader csv = new CsvReader(new FileInputStream(configCSV.getFile()), configCSV
					.getDelimiter(), configCSV.getCharset());
			csv.setEscapeMode(configCSV.getEscapeMode());
			csv.setTextQualifier(configCSV.getTextQualifier());

			String[] headers = {};
			Vector<String[]> tableDataVector = new Vector<String[]>();
			
			 highestColumnCount = getLargestColumnCountFromCSV();

			if (configCSV.getFirstRowHasHeaders())
			{
				csv.readHeaders();
				headers = csv.getHeaders();
			}

			int rowColumnCount = 0;
			while (csv.readRecord())
			{
				//how many columns does this row of data contain
				rowColumnCount = csv.getColumnCount();
				//create an array that contains teh row data
				String[] rowData = new String[csv.getColumnCount()];
				for (int col = 0; col < csv.getColumnCount(); col++)
				{
					rowData[col] = csv.get(col);
				}
				//if the column count in this row of data is not as large
				//as the column header count, then "insert" blank string into the cells
				String[] newArray = padArray(highestColumnCount, rowData, false);
				//stick the row data into a vector because we do not know how many
				//rows of data there are
				tableDataVector.add(newArray);
			}

			if (!configCSV.getFirstRowHasHeaders() || headers == null)
			{
				//create headers with names Column1, Column2...
				headers = createDummyHeaders(rowColumnCount);
			}
			
			//if the header count is not as large as the longest column count in the data set
			//create dummy headers and append to end of table.
			headers = padArray(highestColumnCount, headers, true);
			
			log.debug("---------------------------------------------------");
			printArray(headers);
			log.debug("---------------------------------------------------");
			

			//pull row data out of vector and stick into an array for table model.
			String[][] tableData = new String[tableDataVector.size()][rowColumnCount];
			for (int i = 0; i < tableData.length; i++)
			{
				tableData[i] = (String[]) tableDataVector.elementAt(i);
				printArray(tableData[i]);
			}

            if (checkForErrors(headers, tableData)) 
            {
                errorPanel.showDataImportStatusPanel(true);
            }
            else {
                errorPanel.showDataImportStatusPanel(false);
            }
            
            if(tableDataVector.size() >= WorkbenchTask.MAX_ROWS)
            {
            	showTooManyRowsErrorDialog();
            }
			model = new PreviewTableModel(headers, tableData);
			t.setModel(model);
			t.setColumnSelectionAllowed(false);
			t.setRowSelectionAllowed(false);
			t.setCellSelectionEnabled(false);
			t.getTableHeader().setReorderingAllowed(false);
            t.setPreferredScrollableViewportSize(new Dimension(500,100));
			t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			model.fireTableDataChanged();
			model.fireTableStructureChanged();
			return t;

		} catch (IOException ex)
		{
			log.error("Error attempting to parse input csv file:" + ex);
		}
		return null;
	}
  
    /**
     * Adds table to scrollpanel
     * @param t - the table to be displayed in the preview pane
     * @return
     * JScrollPane
     */
    private JScrollPane addtoScroll(JTable t)
    {
    	JScrollPane pane = new JScrollPane(t, 
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
       return pane;
    }
    
    /**
     * Goes through and checks the import data for potential errors (where the data being import
     * violates size constraints when being imported into the data base - ie. column header length (64)
     * or field value length (255)
     * @param headers - the column headers
     * @param data - the table data
     * @return
     * JList - all list of errors
     */
    private JList genListOfErrorWhereTableDataDefiesSizeConstraints(String[]headers, String[][]data)
    {     
        DefaultListModel listModel = new DefaultListModel();
        JList listOfImportDataErrors = new JList();
        for(int i=0; i<headers.length; i++)
         {
            if (!isStringShorterThan(WorkbenchTemplateMappingItem.getImportedColNameMaxLength(), headers[i]))
            {
                String msg = "Column at index=" + i + " is too long to be inserted into the database.  It will be truncated.\n"
                + "Current Value:\n" + headers[i]+ "\nTruncated Value:\n" 
                + headers[i].substring(0, WorkbenchTemplateMappingItem.getImportedColNameMaxLength()-1);
                log.warn(msg);
                listModel.addElement(msg);
            }
        }  
        for(int i = 0; i < data.length; i++){
            for(int j=0; j < data[i].length; j++)
            {
            	//WorkbenchDataItem.class.getDeclaredMethod("getCellData", null).getDeclaredAnnotations();
                String str = data[i][j];
                if (!isStringShorterThan(WorkbenchDataItem.getCellDataLength(), str))
                {
                    String msg = "The value in cell Row=" + i + ", Column=" + headers[j] + " is too long to be inserted into the database.  It will be truncated.\n"
                    + "Current Value:\n" + str+ "\nTruncated Value:\n" + str.substring(0, WorkbenchDataItem.getCellDataLength()-1);
                    log.warn(msg);
                    listModel.addElement(msg);
                }
            }
        }
        listOfImportDataErrors.setModel(listModel);
        return listOfImportDataErrors;
    }
    
    /**
     * If the user does not provide "first row contains headers", this creates
     * as set of headers of notation "Column 1"....
     * @param count
     * @return
     * String[]
     */
    public Vector<String> createDummyHeadersAsVector( int count)
    {
        Vector<String> headerVector = new Vector<String>();
        for (int i = 0; i < count; i++)
        { 
            headerVector.add(getResourceString("DEFAULT_COLUMN_NAME") + " " + (i + 1));
        }  
        return headerVector;
    }
    
    /**
     * If the user does not provide "first row contains headers", this creates
     * as set of headers of notation "Column 1"....
     * @param count
     * @return
     * String[]
     */
    public String[] createDummyHeaders( int count)
    {
    	String[] headers = new String[count];
		for (int i = 0; i < count; i++)
		{ 
			headers[i] = getResourceString("DEFAULT_COLUMN_NAME") + " " + (i +1);
		}  
		return headers;
    }
    
    /**
     * Takes an array of data (could be a column array def, or a row of data), and
     * the highest Column count, then inserts blank/dummy data into empty cells.
     * We need to do this because JTable requires that the column header count,
     * and the row data column count are the same.
     * 
     * @param highestColumnCnt - the largest number of columns, or the value that the 
     * array needs to be padded to.
     * @param array - the array needing padding
     * @param replaceWithColumnName - is a column array, if so replace with dummy columns, instead of blank data
     * @return
     * String[] - the new array of data
     */
    public static String[] padArray(int highestColumnCnt, String[] array, boolean replaceWithColumnName)
	{
		if (array.length >= highestColumnCnt)
		{
			return array;
		}
		String[] newArray = new String[highestColumnCnt];
		int paddingIndex = 0;
		for (int i = 0; i < array.length; i++)
		{
			newArray[i] = array[i];
			paddingIndex = i;
		}
		paddingIndex++;
        int padDisplayIndex = 1;
		for (int i = paddingIndex; i < highestColumnCnt; i++)
		{
			if (replaceWithColumnName)
			{
				newArray[i] =  getResourceString("DEFAULT_COLUMN_NAME") + " " + (i+1);
			} 
			else
			{
				newArray[i] = "";
			}
            padDisplayIndex++;
		}
		return newArray;
	}
    
    /**
     * Debugging method for printing an array.
     * @param arrayList
     * void
     */
    private void printArray(String[] arrayList)
	{
		for (int i = 0; i < arrayList.length; i++)
		{
			if (log.isDebugEnabled())System.out.print("[" + (i) + "]" + arrayList[i] + " ");
		}
		log.debug("");
	}
    
    /**
     * Getter for the string qualifier character
     * @return the stringQualifierChar
     */
    public char getStringQualifierChar()
    {
        return stringQualifierChar;
    }


    /**
     * Getter for the character delimiter
     * 
     * @return the delimChar
     */
    public char getDelimChar()
    {
        return delimChar;
    }

	/**
	 * Getter for the Characterset
	 * 
	 * @return the charset
	 */
	public Charset getCharset()
	{
		return this.charset;
	}

	/**
	 * Getter for the escape mode
	 * 
	 * @return the escapeMode
	 */
	public int getEscapeMode()
	{
		return this.escapeMode;
	}


	/**
	 * Getter for the doesFirstRowHaveHeaders
	 * 
	 * @return the doesFirstRowHaveHeaders
	 */
	public boolean getDoesFirstRowHaveHeaders()
	{
		return this.doesFirstRowHaveHeaders;
	}

	/**
	 * Getter for the file name of the import file
	 * @return the fileName
	 */
	public String getFileName()
	{
		return this.fileName;
	}

    /**
     * Getter for whether the operation has been canceled
     * 
     * @return the isCancelled
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }
    
    /**
     * Class the listens to the "Other" delimiter textfield, for user input.
     * Detects a key released event and reads the value.
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
            if (otherText.getText().length() == 1)
            {                
                delimChar = otherText.getText().toCharArray()[0];
                configCSV.setDelimiter(delimChar);
                log.debug("Other value selected for delimiter: ["+ delimChar +"]" );
            }            
            else if (otherText.getText().length()>1)
            {
                log.error("Other field should not allow more that one character as a delimiter");
            }
            updateTableDisplay();
        }
    }
    
    /**
     * Class that forces the "Other" delimiter textfield to only allow
     * one character of input
     * 
     * @author megkumin
     * 
     * @code_status Alpha
     * 
     * Created Date: Mar 27, 2007
     * 
     */
    private class CharLengthLimitDocument extends PlainDocument
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
	 * Listens to check box about whether first row has headers
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
            Object source = e.getItemSelectable();
            log.debug("itemStateChanged");
            if (!(source == containsHeaders)) 
            {
            	log.error("Unexpected checkbox source");
            }
        	log.debug("itemStateChange for first row has header checkbox");
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
            	log.debug("itemStateChanges - SELECTED");
            	doesFirstRowHaveHeaders = true;
            	config.setFirstRowHasHeaders(true);
            }
            else
            {
            	log.debug("itemStateChanges - UNSELECTED");
            	doesFirstRowHaveHeaders = false;
            	config.setFirstRowHasHeaders(false);
            }
            updateTableDisplay();
        }
    }  
    
    /**
     * Table model for the import data table preview.  We need to user the AbstractTableModel
     * to be able to fire of table update changes.
     * 
     * @author megkumin
     *
     */
    private class PreviewTableModel extends AbstractTableModel
    {
        private String[] columnNames = {};
        private String[][] data = {{}};
        
        /**
		 * 
		 */
		public PreviewTableModel()
		{
			super();
		}

		/**
		 * @param headers
		 * @param data
		 */
		public PreviewTableModel(String[] headers, String[][]data)
        {
        	super();
        	this.columnNames = headers;
        	this.data = data;
        }
		
	    /* (non-Javadoc)
	     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	     */
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
		}
		
        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        public String getColumnName(int col) {
            return columnNames[col];
        }
        
		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount()
		{
			return data.length;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			return data[rowIndex][columnIndex];
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
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

    
    /**
     * Creates a Panel the displays a message about whether the data being imported 
     * could have data size constraint violations.  displays a mouseover label for clikcing.
     * @author megkumin
     *
     */
    private class DataErrorPanel extends JPanel
    {
        /**
         * 
         */
        public DataErrorPanel()
		{
			CellConstraints cc = new CellConstraints();
			FormLayout formLayout = new FormLayout("p,5px,p", "d");
			PanelBuilder builder = new PanelBuilder(formLayout, this);
			MouseOverJLabel statusInfoLabel = new MouseOverJLabel();
			statusInfoLabel.setHorizontalTextPosition(JLabel.RIGHT);
			statusInfoLabel.setIcon(IconManager.getIcon("Error", IconManager.IconSize.Std16));
			statusInfoLabel.setText(getResourceString("DATA_IMPORT_ERROR"));
			statusInfoLabel.setActivatedTextColor(Color.RED);
			statusInfoLabel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					showErrors();
				}
			});
			builder.add(statusInfoLabel, cc.xy(1, 1));
            //this.showDataImportStatusPanel(false);
		}
        
        /**
		 * @param shouldShow -
		 *            flag noting whether the panel should be visible
		 * @return the data import error panel to be displayed
		 */
        private JPanel showDataImportStatusPanel(boolean shouldShow)
		{
			if (!shouldShow)
			{
				this.setVisible(false);
			} 
			else
			{
				this.setVisible(true);
			}
			return this;
		}
    }
    /**
	 * Listens to the delimiter radiobuttons for user input.
	 * 
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
            if (other==null)return;
            if (other.isSelected())
            {
                otherText.setEditable(true);
                otherText.requestFocus();
                otherText.setEnabled(true);
                if (otherText.getText().length() == 1)
                {                
                    delimChar = otherText.getText().toCharArray()[0];
                    configCSV.setDelimiter(delimChar);
                }  
            } 
            else if (tab.isSelected())
            {
                delimChar = '\t';
                otherText.setEnabled(false);
            }
            else if (space.isSelected())
            {
                delimChar = ' ';
                otherText.setEditable(false);
                otherText.setEnabled(false);
            }
            else if (semicolon.isSelected())
            {
                delimChar = ';';
                otherText.setEditable(false);
                otherText.setEnabled(false);
            }
            else if (comma.isSelected())
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
            configCSV.setDelimiter(delimChar);
            updateTableDisplay();
        }
    }
    /**
     * Determines whether user wants to use TextQualifier,
     * allows us to turn off text qualification int eh CSVREader.
     * 
     * @return the shouldUseTextQualifier
     */
    public boolean getShouldUseTextQualifier()
    {
        return shouldUseTextQualifier;
    }

    /**
     * @return the highestColumnCount
     */
    public int getHighestColumnCount()
    {
        return highestColumnCount;
    }

 
}
