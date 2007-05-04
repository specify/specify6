/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.csvreader.CsvReader;

import edu.ku.brc.specify.exporters.ExportFileConfigurationFactory;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.UIHelper;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 * Configures a csv file for import to a workbench. Currently the default behavior is to use
 * annoying prompts for delimiter, escapemode, character set, and whether the first row has headers.
 * If first row has no headers they are automatically set to "column1", "column2" ...
 * 
 * Error handling is not handled.
 */
public class ConfigureCSV extends ConfigureExternalDataBase
{
    private static final Logger log = Logger.getLogger(ConfigureCSV.class);
    
    private int     escapeMode;
    private char    delimiter;
    private char    textQualifier;
    private Charset charset;
    private int numOfColsToAppend;
    private boolean shouldUseTextQualifier;// = true;
    
    /**
     * Constructor sets defaults (hard coded).
     */
    public ConfigureCSV(final File file)
    {
        super();
        log.debug("ConfigureCSV");
        escapeMode = getDefaultEscapeMode();
        delimiter  = getDefaultDelimiter();
        charset    = getDefaultCharset();
        textQualifier = getDefaultTextQualifier();
        shouldUseTextQualifier = getDefaultUserTextQualifer();
        numOfColsToAppend = 0;
        readConfig(file);
    }

    public ConfigureCSV(final Properties props)
    {
        super(props);
        
        String prop;
        prop = props.getProperty("escapeMode");
        if (prop == "backslash")
        {
            escapeMode = CsvReader.ESCAPE_MODE_BACKSLASH;
        }
        else if (prop == "doubled")
        {
            escapeMode = CsvReader.ESCAPE_MODE_DOUBLED;
        }
        else 
        {
            escapeMode = getDefaultEscapeMode();
        }
        
        prop = props.getProperty("delimiter");
        if (prop == "comma")
        {
            delimiter = ',';
        }
        else if (prop == "tab")
        {
            delimiter = '\t';
        }
        else
        {
            delimiter = getDefaultDelimiter();
        }
        
        prop = props.getProperty("charset");
        if (prop == null || prop == "US-ASCII")
        {
           charset = Charset.defaultCharset();     
        }
        else
        {
           charset = Charset.forName(prop);    
        }
        prop = props.getProperty("textQualifier");
        if(prop == "doublequote")
        {
            textQualifier = '\"';
        }
        else if(prop == "singlequote")
        {
            textQualifier = '\'';
        }
        else if(prop == "{none}")
        {
            shouldUseTextQualifier = false;
        }
        else
        {
            textQualifier = getDefaultTextQualifier();
        }
    }
    
    public char getDelimiter()
    {
        return delimiter;
    }

    public Charset getCharset()
    {
        return charset;
    }

    public int getEscapeMode()
    {
        return escapeMode;
    }

    /**
     * @param delimiterArg -nthe column delimiter
     * @param charsetArg - the character set used in the file (e.g. ISO-8859-1)
     * @param escapeModeArg - method used to escape reserved characters (backslash or doubled)
     * @return CsvReader for externalFile
     */
    private CsvReader makeReader(final char delimiterArg,
                                 final Charset charsetArg,
                                 final int escapeModeArg,
                                 final char txtQualArg)
    {
        log.debug("makeReader: ");
        log.debug("   delimiterArg: " + delimiterArg);
        log.debug("   charsetArg: " + charsetArg);
        log.debug("   escapeModeArg: " + escapeModeArg);
        log.debug("   txtQualArg: " + txtQualArg);
        if (externalFile != null)
        {
            try
            {
                InputStream input  = new FileInputStream(externalFile);
                CsvReader   result = new CsvReader(input, delimiterArg, charsetArg);
                result.setEscapeMode(escapeModeArg);
                if(shouldUseTextQualifier)
                {
                    result.setTextQualifier(txtQualArg);
                }
                result.setUseTextQualifier(shouldUseTextQualifier);
                log.debug("Status being set to Valid");
                status = Status.Valid;

                return result;

            } catch (FileNotFoundException ex)
            {
                ex.printStackTrace();
            }
        }
        status = Status.Error;
        
        return null;
    }

    /**
     * creates reader using current configuration
     * 
     * @return CsvReader for externalFile
     */
    private CsvReader makeReader()
    {
        return makeReader(delimiter, charset, escapeMode,  textQualifier);
    }

    /**
     * Currently (tentatively) used to generate default column headers.
     * 
     * @return "Column"
     */
    private String getDefaultColHeader()
    {
        return getResourceString("DEFAULT_COLUMN_NAME");
    }
    
    private boolean getDefaultUserTextQualifer()
    {
        return true;
    }

    /**
     * Tentatively used to generate default column headers
     * 
     * @return ("Column1", "Column2", ...)
     */
    public String[] setupHeaders()
    {
        CsvReader csv = makeReader();
        if (csv != null && status == Status.Valid)
        {
            try
            {
                if (csv.readRecord())
                {
                    String[] result = new String[csv.getColumnCount()];
                    for (int h = 0; h < csv.getColumnCount(); h++)
                    {
                        result[h] = getDefaultColHeader() + String.valueOf(h + 1);
                    }
                    
                    status = Status.Valid;
                    
                    return result;
                }
            } catch (IOException ex)
            {
                ex.printStackTrace();
                status = Status.Error;
            }
        }
        status = Status.Error;
        return null;
    }

    /**
     * Lame prompt for delimiter.
     * 
     * @return selected delimiter
     */
    @SuppressWarnings("unused")
    private char determineDelimiter()
    {
        Vector<String> list = new Vector<String>();
        list.add(",");
        list.add("TAB");
        ChooseFromListDlg<String> dlg = new ChooseFromListDlg<String>((Frame)UIRegistry.get(UIRegistry.FRAME), 
                                                                      "Delimiter?", 
                                                                      null,
                                                                      ChooseFromListDlg.OKCANCELHELP, 
                                                                      list, 
                                                                      "WorkbenchImportCvs"); //XXX I18N
        dlg.setModal(true);
        UIHelper.centerAndShow(dlg);

        String delim = dlg.getSelectedObject();

        if (delim == "TAB") { return '\t'; }
        return ',';
    }

    /**
     * Lame prompt for text qualifier.
     * 
     * @return selected delimiter
     */
    @SuppressWarnings("unused")
    private char determineTextQualifier()
    {
        Vector<String> list = new Vector<String>();
        list.add("\"");
        list.add("\'");
        list.add("{none}");
        ChooseFromListDlg<String> dlg = new ChooseFromListDlg<String>((Frame)UIRegistry.get(UIRegistry.FRAME), 
                                                                      "Text Qualifier?", 
                                                                      null,
                                                                      ChooseFromListDlg.OKCANCELHELP, 
                                                                      list, 
                                                                      "WorkbenchImportCvs"); //XXX I18N
        dlg.setModal(true);
        UIHelper.centerAndShow(dlg);

        String delim = dlg.getSelectedObject();

        if (delim == "\"") { return '\"'; }
        if (delim == "\'"){ return '\"'; }
        return ' ';
    }
    private char getDefaultDelimiter()
    {
        return ',';
    }
    private char getDefaultTextQualifier()
    {
        return '"';
    }
    /**
     * Lame prompt for Character set.
     * 
     * @return selected character set
     */
    @SuppressWarnings("unused")
    private Charset determineCharset()
    {
        Vector<String> list = new Vector<String>();
        list.add("default");
        list.add("US-ASCII");
        list.add("ISO-8859-1");
        list.add("UTF-8");
        ChooseFromListDlg<String> dlg = new ChooseFromListDlg<String>((Frame)UIRegistry.get(UIRegistry.FRAME), 
                                                                      "Character Set", 
                                                                      null,
                                                                      ChooseFromListDlg.OKCANCELHELP,
                                                                      list, 
                                                                      "WorkbenchImportCvs");//XXX I18N
        dlg.setModal(true);
        UIHelper.centerAndShow(dlg);

        String delim = dlg.getSelectedObject();

        if (delim == "default") 
        { 
            return Charset.defaultCharset(); 
        }

        return Charset.forName(delim);

    }

    private Charset getDefaultCharset()
    {
        return Charset.defaultCharset();
    }

    /**
     * lame prompt for escape mode.
     * 
     * @return selected escape mode
     */
    @SuppressWarnings("unused")
    private int determineEscapeMode()
    {
        Vector<String> list = new Vector<String>();
        list.add("backslash");
        list.add("doubled");
        
        ChooseFromListDlg<String> dlg = new ChooseFromListDlg<String>((Frame)UIRegistry.get(UIRegistry.FRAME), 
                                                                      "Escape Mode?", null,
                                                                      ChooseFromListDlg.OKCANCELHELP,
                                                                      list, "WorkbenchImportCvs"); //XXX I18N
        dlg.setModal(true);
        UIHelper.centerAndShow(dlg);

        String delim = dlg.getSelectedObject();

        if (delim == "backslash") 
        { 
            return CsvReader.ESCAPE_MODE_BACKSLASH; 
        }
        return CsvReader.ESCAPE_MODE_DOUBLED;
    }

    private int getDefaultEscapeMode()
    {
        return CsvReader.ESCAPE_MODE_DOUBLED;
    }

    private ImportColumnInfo.ColumnType getCellType(@SuppressWarnings("unused") final int colIndex)
    {
        return ImportColumnInfo.ColumnType.String; // hmmmm....
    }

    /*
     * (non-Javadoc) Gets configuration properties from user.
     * 
     * @see edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataBase#interactiveConfig()
     */
    @Override
	protected void interactiveConfig()
	{
        log.debug("interactiveConfig");
//		 delimiter = determineDelimiter();
//		 charset = determineCharset();
//		 escapeMode = determineEscapeMode();
//		 firstRowHasHeaders = determineFirstRowHasHeaders();
//		 textQualifier = determineTextQualifier();
//         nonInteractiveConfig();
		 
		DataImportDialog dlg = new DataImportDialog(this, delimiter,
                textQualifier, charset, escapeMode, firstRowHasHeaders, shouldUseTextQualifier);

		if (!dlg.isCancelled())
        {
            delimiter = dlg.getDelimChar();
            charset = dlg.getCharset();
            escapeMode = dlg.getEscapeMode();
            firstRowHasHeaders = dlg.getDoesFirstRowHaveHeaders();
            textQualifier = dlg.getStringQualifierChar();
            shouldUseTextQualifier = dlg.getShouldUseTextQualifier();
            numOfColsToAppend = dlg.getHighestColumnCount();
            nonInteractiveConfig();
        }
        else
        {
            status = Status.Cancel;
        }

		log.debug("delim: " + delimiter);
		log.debug("charset: " + charset);
		log.debug("escapemode: " + escapeMode);
		log.debug("furst row has headers: " + firstRowHasHeaders);
		log.debug("textqualifier: " + textQualifier);	
	}

    /*
	 * (non-Javadoc)
	 * 
	 * Sets up colInfo for externalFile.
	 * 
	 * @see edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataBase#nonInteractiveConfig()
	 */
    @Override
    protected void nonInteractiveConfig()
    {
        log.debug("nonInteractiveConfig");
        CsvReader csv = makeReader();
        if (csv != null && status == Status.Valid)
        {
            try
            {
                if (firstRowHasHeaders)
                {
                    csv.readHeaders();
                } else
                {
                    csv.setHeaders(setupHeaders());
                }
                String[] newHeaders = null;
                if(numOfColsToAppend > csv.getHeaderCount())
                {
                    newHeaders = padColumnHeaders(numOfColsToAppend, csv.getHeaders());
                    csv.setHeaders(newHeaders);
                }

                if (status == Status.Valid)
                {
                    //int headerCount = Math.max(arg0, arg1)
                    colInfo = new Vector<ImportColumnInfo>(csv.getHeaderCount());
    
                    for (int h = 0; h < csv.getHeaderCount(); h++)
                    {
                        colInfo.add(new ImportColumnInfo((short)h, getCellType(h), csv.getHeader(h), csv.getHeader(h), null));
                    }
                    Collections.sort(colInfo);
                    
                    return;
                }
                 
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        status = Status.Error; // shouldn't be needed because cvs should be null and valid
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataBase#getProperties()
     */
    @Override
    public Properties getProperties()
    {
        Properties result = super.getProperties();
        result.setProperty("mimetype", ExportFileConfigurationFactory.CSV_MIME_TYPE);
        if (escapeMode == CsvReader.ESCAPE_MODE_BACKSLASH)
        {
            result.setProperty("escapeMode", "backslash");
        }
        else if (escapeMode == CsvReader.ESCAPE_MODE_DOUBLED)
        {
            result.setProperty("escapeMode", "doubled");
        }
        
        if (delimiter == ',')
        {
            result.setProperty("delimiter", "comma");
        }
        else if (delimiter == '\t')
        {
            result.setProperty("delimiter", "tab");
        }
        
        if(textQualifier == '\"')
        {
            result.setProperty("textQualifer", "doublequote");
        }
        else if(textQualifier == '\'')
        {
            result.setProperty("textQualifer", "singlequote");
        }        
        result.setProperty("charset", charset.name());
       
        return result;
    }

    /**
     * Takes an array of column  defs,  and
     * the highest Column count, then inserts dummy column headers into headers.
     * 
     * @param highestColumnCnt - the largest number of columns, or the value that the 
     * array needs to be padded to.
     * @param array - the array needing padding
     * @return
     * String[] - the new header array 
     */
    public String[] padColumnHeaders(final int highestColumnCnt, final String[] array)
    {
        return DataImportDialog.padArray(highestColumnCnt, array, true);
    }
    /**
     * @return the textQualifier
     */
    public char getTextQualifier()
    {
        return textQualifier;
    }

    /**
     * @param textQualifier the textQualifier to set
     */
    public void setTextQualifier(final boolean use, final char textQualifier)
    {
        shouldUseTextQualifier = true;
        if(use)
        {
            this.textQualifier = textQualifier;
        }
    }

	/**
	 * @param escapeMode the escapeMode to set
	 */
	public void setEscapeMode(final int escapeMode)
	{
		this.escapeMode = escapeMode;
	}

	/**
	 * @param delimiter the delimiter to set
	 */
	public void setDelimiter(final char delimiter)
	{
		this.delimiter = delimiter;
	}

	/**
	 * @param charset the charset to set
	 */
	public void setCharset(final Charset charset)
	{
		this.charset = charset;
	}

    /**
     * @return the numOfColsToAppend
     */
    public int getNumOfColsToAppend()
    {
        return numOfColsToAppend;
    }

    /**
     * @param numOfColsToAppend the numOfColsToAppend to set
     */
    public void setNumOfColsToAppend(final int numOfColsToAppend)
    {
        this.numOfColsToAppend = numOfColsToAppend;
    }


    /**
     * @return the shouldUseTextQualifier
     */
    public boolean isShouldUseTextQualifier()
    {
        return shouldUseTextQualifier;
    }

    /**
     * @param shouldUseTextQualifier the shouldUseTextQualifier to set
     */
    public void setShouldUseTextQualifier(final boolean userTextQualifier)
    {
        this.shouldUseTextQualifier = userTextQualifier;
    }
}
