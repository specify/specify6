/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createRadioButton;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static java.sql.Types.NUMERIC;

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
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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
import edu.ku.brc.ui.BiColorTableCellRenderer;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MouseOverJLabel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import org.apache.poi.ss.usermodel.*;

/**
 * Class that provides "fancy" dialog for importing data from csv or XLS,
 * allowing users to specify such information as delimiters, text qualifiers,
 * character sets, escape modes, etc.  Dialog provides a preview table
 * displaying how the data will appear when imported into a spreadsheet table.
 * 
 * (This needs to be converted to CustomDialog)
 * 
 * @author megkumin
 *
 * @code_status Complete
 *
 * Created Date: Mar 26, 2007
 *
 */
@SuppressWarnings("serial")
public class DataImportDialog extends JDialog implements ActionListener
{
    protected static final Logger log = Logger.getLogger(DataImportDialog.class);
	private JButton cancelBtn;
	private JButton okBtn;
	private JButton helpBtn;
	
	public static final int OK_BTN = 1;
	public static final int CANCEL_BTN = 2;
	public static final int HELP_BTN = 4;
	protected int btnPressed = CANCEL_BTN;
	
	protected JRadioButton tab;
	protected JRadioButton space;
	protected JRadioButton semicolon;
	protected JRadioButton comma;
	protected JRadioButton other;
	protected JTextField otherText;
	
	protected char stringQualifierChar;
	protected char delimChar;
	protected Charset charset;
	protected int escapeMode;
	protected boolean doesFirstRowHaveHeaders;
	protected boolean shouldUseTextQualifier;

	private JLabel textQualLabel;
	private JComboBox textQualCombo;
	private JLabel charSetLabel;
	private JComboBox charSetCombo;
	private JLabel escapeModeLabel;
	private JComboBox escapeModeCombo;

	protected JCheckBox containsHeaders;

	protected boolean isCancelled = true;
	protected boolean hasTooManyRows = false;

	private String fileName;
	private File file;
	protected ConfigureExternalDataBase config;
	protected ConfigureCSV configCSV;
	protected ConfigureXLS configXLS;
	
	private JTable myDisplayTable;
	protected PreviewTableModel model;
	protected DataErrorPanel errorPanel = new DataErrorPanel();
   
	protected int highestColumnCount;
    
    protected boolean ignoreActions = false;
    
    protected int geoDataCol = -1;
    protected Vector<Integer> imageDataCols = new Vector<Integer>();

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
        
        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }
    }
    
    /**
     * Constructor for Import dialog for xls
     * 
     * @param config - the config class for configing an xls import
     * @param doesHaveHeaders
     */
    public DataImportDialog(final ConfigureXLS config, boolean doesHaveHeaders)
	{
        this.config    = config;
        this.configXLS = config;
        this.file      = config.getFile();
        this.fileName  = file.getAbsolutePath().toString();
        this.doesFirstRowHaveHeaders = doesHaveHeaders;
        myDisplayTable = null;
        model = new PreviewTableModel();
        
        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }
	}
    
    /**
     * @return false means the dialog should not be shown.
     */
    public boolean init()
    {
        return createUiForXLS();
    }

    /**
     * @return true if ui creation succeeds, else false
     */
    public boolean initForCSV()
    {
    	return createUiForCSV();
    }
    
    /**
     * Initialize UI for a csv import
     * 
     * void
     */
    private boolean createUiForCSV()
    {
    	JPanel p = createConfigPanelForCSV();
    	setContentPane(p);  
    	if(!hasTooManyRows)
    	{
    		init(getResourceString("IMPORT_CVS"));    
    		return true;
    	}
    	return false;
    }
      

    /**
     * Initialize UI for an XLS import
     * 
     * void
     */
    private boolean createUiForXLS()
    {
    	JPanel p = createConfigPanelForXLS();
    	if (p != null)
    	{
        	setContentPane(p);
        	if(!hasTooManyRows)
        	{
        		init(getResourceString("IMPORT_XLS"));    
        	}
        	return true;
    	}
    	return false;
    }
    
    /**
     * General init ui method
     * 
     * @param title - the title of the dialog
     * void
     */
    private void init(final String title)
    {
        setTitle(title);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setModal(true);
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
                "5px, 15px, p, 30px, p:g, 15px", // columns
                
                "5px," + 		//padding
                "p,3px, " +		//separator     
                "p,10px, " +	//delim panel        
                "p,3px, " +		//file info separator
                "p,3px, " +		//file info lable
                "p,10px, " +	//row header
                "p, 3px,"+		//preview separator
                "f:p:g,5px," + 	//tablePreview
                "30px,3px,"+    //data import status error panel
                "p,10px" 		//buttongs
                ), configPanel);// rows

        JLabel fileInfo = createLabel(getResourceString("FILE_PREVIEW") + " " + fileName);
        JPanel buttonpanel = buildButtons();
        containsHeaders = createCheckBox(getResourceString("COLUMN_HEAD"));
        containsHeaders.setSelected(true);
        containsHeaders.addItemListener(new CheckboxItemListener());

        builder.addSeparator(getResourceString("DATA_IMPORT_OPS"),  cc.xyw(2,2,4)); 
        builder.add         (createDelimiterPanel(),                cc.xy (3,4));        
        builder.add         (createOtherControlsForCSVPanel(),      cc.xy (5,4));       
          
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
                "5px, 15px, p, 30px, p:g, 15px", // columns               
                "5px," + 		//padding      
                "p,3px, " +		//file info separator
                "p,3px, " +		//file info lable
                "p,10px, " +	//row header
                "p, 3px,"+		//previe separator
                "f:p:g,5px," + 	//tablePreview
                "30px,3px,"+    //data import status error panel
                "p,10px" 		//buttongs
                ), configPanel);// rows

        JLabel fileInfo = createLabel(getResourceString("FILE_PREVIEW") + " " + fileName);
        JPanel buttonpanel = buildButtons();
        containsHeaders = createCheckBox(getResourceString("COLUMN_HEAD"));
        containsHeaders.setSelected(true);
        containsHeaders.addItemListener(new CheckboxItemListener());
          
        builder.addSeparator(getResourceString("FILE_IMPORT"),      cc.xyw(2,2,4));
        builder.add         (fileInfo,                              cc.xyw(3,4,4));
        builder.add         (containsHeaders,                       cc.xyw(3,6,3));   
        
        builder.addSeparator(getResourceString("DATA_PREVIEW"),     cc.xyw(2,8,4));
        myDisplayTable = setXLSTableData(myDisplayTable);
        if (myDisplayTable == null)
        {
            return null;
        }
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
        cancelBtn = createButton(getResourceString("CANCEL"));
        okBtn     = createButton(getResourceString("OK"));
        helpBtn   = createButton(getResourceString("HELP"));

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
                log.debug("User pressed help");
                btnPressed  = HELP_BTN;
            }
        });

        getRootPane().setDefaultButton(okBtn);
        
        if (config instanceof ConfigureCSV)
        {
            HelpMgr.registerComponent(helpBtn, "WorkbenchImportCSV");
        }
        else if (config instanceof ConfigureXLS)
        {
            HelpMgr.registerComponent(helpBtn, "WorkbenchImportXLS");
        }
        else
        {
            HelpMgr.registerComponent(helpBtn, "WorkbenchImportData");
        }
       
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

        textQualLabel = createLabel(getResourceString("TEXT_QUAL"));
        String[] qualifiers = { "\"", "\'", "{"+getResourceString("WB_NONE")+"}" };
        textQualCombo = createComboBox(qualifiers);
        //textQualCombo.setSelectedIndex(0);
        textQualCombo.setSelectedItem(String.valueOf(this.stringQualifierChar));
        textQualCombo.addActionListener(this);

        charSetLabel = createLabel(getResourceString("CHAR_SET"));
        String[] charsets = { getResourceString("DEFAULT"), "US-ASCII", "ISO-8859-1", "UTF-8" };
        charSetCombo = createComboBox(charsets);
        charSetCombo.setSelectedIndex(0);
        charSetCombo.addActionListener(this);
        
        escapeModeLabel = createLabel(getResourceString("ESCAPE_MODE"));
        String[] escapeModes = {getResourceString("BACKSLASH"), getResourceString("DOUBLED")};
        escapeModeCombo = createComboBox(escapeModes);
        escapeModeCombo.setSelectedIndex(this.escapeMode);
        escapeModeCombo.addActionListener(this);
        
        setContentPane(textQualCombo);
        setContentPane(charSetCombo);
        setContentPane(escapeModeCombo);

        builder.add(textQualLabel,    cc.xy(1, 1));
        builder.add(textQualCombo,    cc.xy(3, 1));     
        builder.add(charSetLabel,    cc.xy(1, 3));
        builder.add(charSetCombo,    cc.xy(3, 3));            
        builder.add(escapeModeLabel,    cc.xy(1, 5));
        builder.add(escapeModeCombo,    cc.xy(3, 5));   

        return myPanel;       
    }
    
    protected void changeQualifier(final String newQualifier)
    {
        ((ConfigureCSV )config).setTextQualifier(!StringUtils.isEmpty(newQualifier), newQualifier.charAt(0));
        updateTableDisplay();
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
            changeQualifier(str);
        }
        else if (str.equals("\'"))
        {
            stringQualifierChar = '\'';
            changeQualifier(str);
        }
        else if (str.equals("{"+getResourceString("NONE")+"}" ))
        {
            changeQualifier(str);
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
           
        tab = createRadioButton(getResourceString("TAB"));
        tab.addItemListener(new DelimButtonItemListener());
        //tab.setBackground(newColor);
        
        space = createRadioButton(getResourceString("SPACE"));
        space.addItemListener(new DelimButtonItemListener());
        //space.setBackground(newColor);

        comma = createRadioButton(getResourceString("COMMA"));
        comma.addItemListener(new DelimButtonItemListener());
        comma.setSelected(true);
        //comma.setBackground(newColor);
        
        semicolon = createRadioButton(getResourceString("SEMICOLON"));
        semicolon.addItemListener(new DelimButtonItemListener());
        //semicolon.setBackground(newColor);
        
        other = createRadioButton(getResourceString("OTHER"));
        other.addItemListener(new DelimButtonItemListener());
        //other.setBackground(newColor);
        
        otherText = createTextField();
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
    protected void updateTableDisplay()
    {
        if (config instanceof ConfigureCSV)
        {
            setCSVTableData(myDisplayTable);
        }
        else 
        {
            setXLSTableData(myDisplayTable);
        }
        SwingUtilities.invokeLater(new Runnable() {

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run()
			{
				okBtn.setEnabled(!hasTooManyRows);
			}
        	
        });
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
    protected void showErrors()
    {
        JList listOfErrors = genListOfErrorWhereTableDataDefiesSizeConstraints(model.getColumnNames(), model.getData());
        
        if ((model.getColumnNames() ==null )|| (model.getData() == null) || (listOfErrors==null) || (listOfErrors.getModel().getSize() == 0))
        {
            JTextArea textArea = new JTextArea();
            textArea.setRows(25);
            textArea.setColumns(60);
            //String newline = "\n";
            //for (int i = 0; i < listOfErrors.getModel().getSize(); i++)
            //{
                textArea.append(getResourceString("WB_PARSE_FILE_ERROR2"));
            //}
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setCaretPosition(0);
            JScrollPane pane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                                         ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow() != null ? UIRegistry.getMostRecentWindow() : UIRegistry.getTopWindow(), pane,getResourceString("DATA_IMPORT_ISSUES"),JOptionPane.WARNING_MESSAGE);
            okBtn.setEnabled(false); 	
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
            JScrollPane pane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                                         ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow() != null ? UIRegistry.getMostRecentWindow() : UIRegistry.getTopWindow(), pane,getResourceString("DATA_IMPORT_ISSUES"),JOptionPane.WARNING_MESSAGE);
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
			int curRowColCount  = 0;
			int highestColCount = 0;
			//seeing how many columns are defined
			if (configCSV.getFirstRowHasHeaders())
			{
				csv.readHeaders();
				highestColCount = Math.max(highestColCount, csv.getHeaders().length);
			}
			//see what the largest number of columns is per row of data
			while (csv.readRecord())
			{
				curRowColCount = csv.getColumnCount();
				highestColCount = Math.max(highestColCount, curRowColCount);

			}
			return highestColCount;
		} 
    	catch (Exception e)
		{
    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataImportDialog.class, e);
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
    
    private void checkUserColInfo(final String value, int colNum)
    {
        if (value.equals(DataImport.GEO_DATA_HEADING))
        {
            geoDataCol = colNum;
        }
        if (value.equals(DataImport.IMAGE_PATH_HEADING))
        {
            imageDataCols.add(colNum);
        }
    }
    
    private boolean isUserCol(int colNum)
    {
        return geoDataCol != colNum && !imageDataCols.contains(colNum);
    }
  
    
    /**
     * Parses the given import xls file according to the users selection and creates/updates the
     * Preview table, showing the user how the import options effect the way the data will be
     * imported into the spreadsheet.
     * 
     * @param table - the table to display the data
     * @return JTable - the table to display the data
     */
    private JTable setXLSTableData(final JTable table)
    {
        int      numRows = 0;
        int    numCols = 0;
        String[] headers = {};
        Vector<Vector<String>> tableDataVector = new Vector<Vector<String>>();
        Vector<String> rowData = new Vector<String>();
        Vector<String> headerVector = new Vector<String>();
        DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
        try
        {
            log.debug("setXLSTableData - file - " + configXLS.getFile().toString());

            InputStream     input    = new FileInputStream(configXLS.getFile());
            Workbook workBook = WorkbookFactory.create(input);
            Sheet sheet    = workBook.getSheetAt(0);

            Vector<Integer> badHeads = new Vector<Integer>();
            Vector<Integer> emptyCols = new Vector<Integer>();
            ((ConfigureXLS)config).checkHeadsAndCols(sheet, badHeads, emptyCols);
            if (badHeads.size() > 0 && doesFirstRowHaveHeaders)
            {
                if (table != null)
                {
                    ((ConfigureXLS)config).showBadHeadingsMsg(badHeads, emptyCols, getTitle());
                }
                this.doesFirstRowHaveHeaders = false;
                try
                {
                    ignoreActions = true;
                    this.containsHeaders.setSelected(false);
                }
                finally
                {
                    ignoreActions = false;
                }
                if (table != null)
                {
                    return table;
                }
            }
            boolean firstRow = true;

            //quick fix to prevent ".0" at end of catalog numbers etc
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(20);
            nf.setGroupingUsed(false); //gets rid of commas
            
            int maxCols = 0;
            
            // Iterate over each row in the sheet
            Iterator<?> rows = sheet.rowIterator();
            while (rows.hasNext())
            {
                numCols = 0;
                rowData = new Vector<String>();
                Row row = (Row) rows.next();
                //log.debug(row.getLastCellNum()+"  "+row.getPhysicalNumberOfCells());
                int maxSize = Math.max(row.getPhysicalNumberOfCells(), row.getLastCellNum());
                if (maxSize > maxCols)
                {
                    maxCols = maxSize;
                }
                while (numCols < maxSize)
                {
                    if (emptyCols.indexOf(new Integer(numCols)) == -1)
                    {
                        Cell cell = row.getCell(numCols);
                        String value = null;
                        // if cell is blank, set value to ""
                        if (cell == null)
                        {
                            value = "";
                        }
                        else
                        {
                            CellType type = cell.getCellType();
                        	if (type == CellType.NUMERIC) {
                                // The best I can do at this point in the app is to guess if a
                                // cell is a date.
                                // Handle dates carefully while using HSSF. Excel stores all
                                // dates as numbers, internally.
                                // The only way to distinguish a date is by the formatting of
                                // the cell. (If you
                                // have ever formatted a cell containing a date in Excel, you
                                // will know what I mean.)
                                // Therefore, for a cell containing a date, cell.getCellType()
                                // will return
                                // HSSFCell.CELL_TYPE_NUMERIC. However, you can use a utility
                                // function,
                                // HSSFDateUtil.isCellDateFormatted(cell), to check if the cell
                                // can be a date.
                                // This function checks the format against a few internal
                                // formats to decide the issue,
                                // but by its very nature it is prone to false negatives.
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    value = scrDateFormat.getSimpleDateFormat().format(
                                            cell.getDateCellValue());
                                    //value = scrDateFormat.getSimpleDateFormat().format(cell.getDateCellValue());
                                } else {
                                    double numeric = cell.getNumericCellValue();
                                    value = nf.format(numeric);
                                }
                            } else if (type == CellType.STRING) {
                                value = cell.getRichStringCellValue().getString();
                            } else if (type == CellType.BLANK) {
                                value = "";
                            }  else if (type == CellType.BOOLEAN) {
                                value = Boolean.toString(cell.getBooleanCellValue());
                            } else if (type == CellType.FORMULA) {
                                value = UIRegistry.getResourceString("WB_FORMULA_IMPORT_NO_PREVIEW");
                            } else {
                        	    value = "";
                        	    log.error("unsuported cell type");
                            }
                        }
                        if (firstRow && doesFirstRowHaveHeaders)
                        {
                            checkUserColInfo(value, numCols);
                        }
                        if (isUserCol(numCols))
                        {
                            rowData.add(value.toString());
                        }
                    }
                    numCols++;
                }
                if (doesFirstRowHaveHeaders && firstRow)
                {
                    headerVector = rowData;
                    headers = new String[rowData.size()];
                }
                else if (!doesFirstRowHaveHeaders && firstRow){
                    //headers = createDummyHeaders(rowData.size());
                    tableDataVector.add(rowData);
                }
                else
                {
                    tableDataVector.add(rowData);
                }
                firstRow = false;
                numRows++;
            }
            maxCols -= emptyCols.size();
            if (!doesFirstRowHaveHeaders)
            {
                headerVector = createDummyHeadersAsVector(maxCols);
                headers = new String[maxCols];
            }
            for (int i = 0; i < headerVector.size(); i++)
            {
                headers[i] = headerVector.elementAt(i);
            }
            printArray(headers);

            String[][] tableData = new String[tableDataVector.size()][maxCols];
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
            
            if((doesFirstRowHaveHeaders ? numRows-1 : numRows) > WorkbenchTask.MAX_ROWS)
            {
            	hasTooManyRows = true;
            	showTooManyRowsErrorDialog();
            }
            else
            {
            	hasTooManyRows = false;
            }
            log.debug(headers);
            log.debug(tableData);
            model = new PreviewTableModel(headers, tableData);
            JTable result = null;
            if (table == null)
            {
                result = new JTable();
                result.setColumnSelectionAllowed(false);
                result.setRowSelectionAllowed(false);
                result.setCellSelectionEnabled(false);
                result.getTableHeader().setReorderingAllowed(false);
                result.setPreferredScrollableViewportSize(new Dimension(500, 100));
                result.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            }
            else
            {
            	result = table;
            }
            result.setModel(model);
            result.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false));
            model.fireTableDataChanged();
            model.fireTableStructureChanged();
            return result;
        } 
        catch (Exception ex)
        {
        	UIRegistry.displayErrorDlgLocalized(UIRegistry.getResourceString("WB_ERROR_READING_IMPORT_FILE"));
        	if (table != null)
        	{
            	String[] columnNames = {};
            	String[][] blankData = {{}};
                model = new PreviewTableModel(columnNames, blankData);
                table.setModel(model);
                table.setColumnSelectionAllowed(false);
                table.setRowSelectionAllowed(false);
                table.setCellSelectionEnabled(false);
                table.getTableHeader().setReorderingAllowed(false);
                table.setPreferredScrollableViewportSize(new Dimension(500, 100));
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                table.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false));
                model.fireTableDataChanged();
                model.fireTableStructureChanged();
                return table;
        	}
            //log.error("Error attempting to parse input xls file:" + ex);
            //ex.printStackTrace();
        }

        return null;       
    }
    
    private void showTooManyRowsErrorDialog()
    {
        PanelBuilder    builder = new PanelBuilder(new FormLayout("p:g", "c:p:g"));
        CellConstraints cc      = new CellConstraints();

        //The Specify 6 Workbench can only import 2000 rows of data.  The file you tried to import had more than that.  Please reduce the record account and try again.
        builder.add(createLabel(String.format(UIRegistry.getResourceString("DataImportDialog.TooManyRows"), WorkbenchTask.getMaxRows())), cc.xy(1,1));
        builder.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
        CustomDialog maxRowExceededDlg = new CustomDialog((Frame)UIRegistry.get(UIRegistry.FRAME), getResourceString("WB_MAXROWS") , true, CustomDialog.OK_BTN, builder.getPanel());
        UIHelper.centerAndShow(maxRowExceededDlg);
    }
    
    /**
     * Parses the given import file according to the users selection and creates/updates the Preview table,
     * showing the user how the import options effect the way the data will be imported into the spreadsheet.
     * @param table - the table to display the data
     * @return
     * JTable - the table to display the data
     */
    private JTable setCSVTableData(final JTable table)
	{
		try
		{
			log.debug("setTableData - file - " + configCSV.getFile().toString());
			CsvReader csv = new CsvReader(new FileInputStream(configCSV.getFile()), 
			                                                  configCSV.getDelimiter(), 
			                                                  configCSV.getCharset());
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
				tableData[i] = tableDataVector.elementAt(i);
				printArray(tableData[i]);
			}

            if (checkForErrors(headers, tableData)) 
            {
                errorPanel.showDataImportStatusPanel(true);
            }
            else {
                errorPanel.showDataImportStatusPanel(false);
            }
            
            if((doesFirstRowHaveHeaders ? tableDataVector.size()-1 : tableDataVector.size())  > WorkbenchTask.MAX_ROWS)
            {
            	hasTooManyRows = true;
            	showTooManyRowsErrorDialog();
            }
            else
            {
            	hasTooManyRows = false;
            }
			model = new PreviewTableModel(headers, tableData);
			table.setModel(model);
			table.setColumnSelectionAllowed(false);
			table.setRowSelectionAllowed(false);
			table.setCellSelectionEnabled(false);
			table.getTableHeader().setReorderingAllowed(false);
            table.setPreferredScrollableViewportSize(new Dimension(500,100));
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false));
			
			model.fireTableDataChanged();
			model.fireTableStructureChanged();
			return table;

		} catch (IOException ex)
		{
    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataImportDialog.class, ex);
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
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
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
                if (!isStringShorterThan(WorkbenchDataItem.getMaxWBCellLength(), str))
                {
                    String msg = "The value in cell Row=" + i + ", Column=" + headers[j] + " is too long to be inserted into the database.  It will be truncated.\n"
                    + "Current Value:\n" + str+ "\nTruncated Value:\n" + str.substring(0, WorkbenchDataItem.getMaxWBCellLength()-1);
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
     * as set of headers of notation "Column 1"...
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
     * as set of headers of notation "Column 1"...
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
        String str = "";
		for (int i = 0; i < arrayList.length; i++)
		{
			str = str + "[" + (i) + "]" + arrayList[i] + " ";
		}
		log.debug(str);
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
        @Override
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
        @Override
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
            if (!ignoreActions)
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

		/* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
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
		@Override
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
		@Override
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
		@Override
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
			statusInfoLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
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
        protected JPanel showDataImportStatusPanel(boolean shouldShow)
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
