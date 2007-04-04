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
import java.util.Vector;

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
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.log4j.Logger;

import com.csvreader.CsvReader;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.MouseOverJLabel;
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
    private boolean userTextQualifier;

	private JLabel textQualLabel;
	private JComboBox textQualCombo;
	private JLabel charSetLabel;
	private JComboBox charSetCombo;
	private JLabel escapeModeLabel;
	private JComboBox escapeModeCombo;

	private JCheckBox containsHeaders;

	private boolean isCancelled = false;

	private String fileName;
	private File file;
	private ConfigureCSV config;


	private JTable myDisplayTable;
	private PreviewTableModel model;
    DataErrorPanel errorPanel = new DataErrorPanel();
   
    public DataImportDialog(final ConfigureCSV config, char defaultDelimChar, 
                            char defaultTextQual, Charset defaultCharSet,
                            int defaultEscMode, boolean doesHaveHeaders, boolean useTxtQual)
    {
    	this.config = config;
    	this.file = config.getFile();
    	this.fileName = file.getAbsolutePath().toString();
        this.doesFirstRowHaveHeaders = doesHaveHeaders;
        this.charset = defaultCharSet;
        this.escapeMode = defaultEscMode;
        this.delimChar = defaultDelimChar;
        this.stringQualifierChar = defaultTextQual;
        this.userTextQualifier = useTxtQual;
        myDisplayTable = new JTable();
        model = new PreviewTableModel();
        initForCSV();
    }
    
    public DataImportDialog(final ConfigureXLS config, boolean doesHaveHeaders)
	{
	}

    private void initForCSV()
    {
    	setContentPane(createConfigPanelForCSV());
    	init(getResourceString("IMPORT_CVS"));
        setModal(true);
        UIHelper.centerAndShow(this);
        
    }
    
    private boolean checkForErrors(String[]headers, String[][]data)
    {
        JList listOfErrors = checkTableDataForSizeConstraints(headers, data);
        if(listOfErrors.getModel().getSize()>0)
        {
           return true;
        }  
        return false;
    }
    
    private void showErrors()
    {
        JList listOfErrors = checkTableDataForSizeConstraints(model.getColumnNames(), model.data);
        if (listOfErrors.getModel().getSize() > 0)
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
            JOptionPane.showMessageDialog(UICacheManager.get(UICacheManager.FRAME), pane,getResourceString("DATA_IMPORT_ISSUES"),JOptionPane.WARNING_MESSAGE);
        }
    }
    
    @SuppressWarnings("unused")
    private void initForXSL()
    {
    	setContentPane(createConfigPanelForXSL());
    	init(getResourceString("IMPORT_XSL"));   
        setModal(true);
        UIHelper.centerAndShow(this);
    }
    
    private void init(String title)
    {
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
                "f:p:g,5px," + 	//tablePreview
                "30px,3px,"+
                "p,10px" 		//buttongs
                ), configPanel);// rows

        JLabel fileInfo = new JLabel(getResourceString("FILE_PREVIEW") + " " + fileName);
        JPanel buttonpanel = buildButtons();
        containsHeaders = new JCheckBox(getResourceString("COLUMN_HEAD"));
        containsHeaders.setSelected(true);
        containsHeaders.addItemListener(new CheckboxItemListener());
        
        

        builder.addSeparator(getResourceString("DATA_IMPORT_OPS"),  cc.xyw(2,2,4)); 
        builder.add         (createDelimiterPanel(),                cc.xy (3,4));        
        builder.add         (createOtherControlsPanel(),            cc.xy (5,4));       
          
        builder.addSeparator(getResourceString("FILE_IMPORT"),      cc.xyw(2,6,4));
        builder.add         (fileInfo,                              cc.xyw(3,8,4));
        builder.add         (containsHeaders,                       cc.xyw(3,10,3));   
        
        builder.addSeparator(getResourceString("DATA_PREVIEW"),     cc.xyw(2,12,4));
        
        myDisplayTable = setTableData(myDisplayTable);
        builder.add         (addtoScroll(myDisplayTable),              cc.xyw(3,14,3));   

        builder.add         (errorPanel,              cc.xyw(3,16,4));  
        builder.add         (buttonpanel,                           cc.xyw(2,18,4)); 
        configPanel.setMinimumSize(buttonpanel.getMinimumSize());
        return configPanel;
    }
    
    private JScrollPane addtoScroll(JTable t)
    {
    	JScrollPane pane = new JScrollPane(t, 
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
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
                "f:p:g,5px," + 	//tablePreview
                "30px,3px,"+
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
        myDisplayTable = setTableData(myDisplayTable);
        builder.add         (addtoScroll(myDisplayTable),           cc.xyw(3,10,3));
        builder.add         (errorPanel,              cc.xyw(3,12,4));  
        builder.add         (buttonpanel,                           cc.xyw(2,14,4)); 
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
    
    /** Listens to the combo box. */
    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        //Object source = e.g
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
        else if(str.equals("{"+getResourceString("NONE")+"}" ))
        {
            
        }
        else if(str.equals("US-ASCII") || 
        		str.equals("ISO-8859-1") || 
        		str.equals("UTF-8"))
        {
        	charset = Charset.forName(str);      	
        }
        else if(str.equals(getResourceString("DEFAULT")))
        {
        	charset = Charset.defaultCharset(); 
        }
        else if(str.equals(getResourceString("BACKSLASH")))
        {
        	escapeMode = CsvReader.ESCAPE_MODE_BACKSLASH;        	
        }
        else if(str.equals(getResourceString("DOUBLED")))
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
                "p,p,2px, p:g,2px, p,2px,p,2px,",
                "p,2px,   p,2px, p,2px , p,2px , p,2px , p,2px, p,2px ");
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
    
    
    private void updateTableDisplay()
    {
    	config.setFirstRowHasHeaders(doesFirstRowHaveHeaders);
    	config.setTextQualifier(true, stringQualifierChar);
    	config.setCharset(charset);
    	config.setEscapeMode(escapeMode);
    	setTableData(myDisplayTable);
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

    
    public int getLargestColumnCount()
    {
    	try
		{
			CsvReader csv = new CsvReader(new FileInputStream(config.getFile()), config.getDelimiter(), config.getCharset());
			csv.setEscapeMode(config.getEscapeMode());
			csv.setTextQualifier(config.getTextQualifier());
			int curRowColumnCount = 0;
			int highestColumnCount = 0;
			if (config.getFirstRowHasHeaders())
			{
				csv.readHeaders();
				highestColumnCount = Math.max(highestColumnCount, csv.getHeaders().length);
			}
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
    
    private boolean isStringLongerThan(int length, String colName)
    {
        if(colName.length()<= length)
        {
            return true;
        }
        return false;
    }
    
    private JTable setTableData(JTable t)
	{
		try
		{
			log.debug("setTableData - file - " + config.getFile().toString());
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

			if (!config.getFirstRowHasHeaders() || headers == null)
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

            if(checkForErrors(headers, tableData)) 
                {
                errorPanel.showDataImportStatusPanel(true);
                }
            else{
                errorPanel.showDataImportStatusPanel(false);
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
    
    public JList checkTableDataForSizeConstraints(String[]headers, String[][]data)
    {     
        DefaultListModel listModel = new DefaultListModel();
        JList listOfImportDataErrors = new JList();
        for(int i=0; i<headers.length; i++)
         {
            if(!isStringLongerThan(WorkbenchTemplateMappingItem.getImportedColNameMaxLength(), headers[i]))
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
                String str = data[i][j];
                if(!isStringLongerThan(255, str))
                {
                    String msg = "The value in cell Row=" + i + ", Column=" + headers[j] + " is too long to be inserted into the database.  It will be truncated.\n"
                    + "Current Value:\n" + str+ "\nTruncated Value:\n" + str.substring(0, 254);
                    log.warn(msg);
                    listModel.addElement(msg);
                }
            }
        }
        listOfImportDataErrors.setModel(listModel);
        return listOfImportDataErrors;
    }
    
    public String[] createDummyHeaders( int count)
    {
    	String[] headers = new String[count];
		for (int i = 0; i < count; i++)
		{ 
			headers[i] = "Column " + i;
		}  
		return headers;
    }
    
    public String[] padArray(int highestColumnCnt, String[] array, boolean replaceWithColumnName)
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
		for (int i = paddingIndex; i < highestColumnCnt; i++)
		{
			if (replaceWithColumnName)
			{
				newArray[i] = "Column " + i;
			} 
			else
			{
				newArray[i] = "";
			}
		}
		return newArray;
	}
    
    public void printArray(String[] arrayList)
	{
		for (int i = 0; i < arrayList.length; i++)
		{
			//if(log.isDebugEnabled())System.out.print("[" + (i) + "]" + arrayList[i] + " ");
		}
		//log.debug("");
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

    /**
     * @return the isCancelled
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }

    /**
     * @param isCancelled the isCancelled to set
     */
    public void setCancelled(boolean isCancelled)
    {
        this.isCancelled = isCancelled;
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
            else if(otherText.getText().length()>1)
            {
                log.error("Other field should not allow more that one character as a delimiter");
            }
            updateTableDisplay();
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
		}
		
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
			statusInfoLabel.setIcon(new ImageIcon(Specify.class.getResource("images/validation-error.gif")));
			statusInfoLabel.setText(getResourceString("DATA_IMPORT_ERROR"));
			statusInfoLabel.setActivatedTextColor(Color.RED);
			statusInfoLabel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					showErrors();
				}
			});
			builder.add(statusInfoLabel, cc.xy(1, 1));
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
                }  
            } 
            else if(tab.isSelected())
            {
                delimChar = '\t';
                otherText.setEnabled(false);
            }
            else if(space.isSelected())
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
     * @return the userTextQualifier
     */
    public boolean isUserTextQualifier()
    {
        return userTextQualifier;
    }

    /**
     * @param userTextQualifier the userTextQualifier to set
     */
    public void setUserTextQualifier(boolean userTextQualifier)
    {
        this.userTextQualifier = userTextQualifier;
    }
 
}
