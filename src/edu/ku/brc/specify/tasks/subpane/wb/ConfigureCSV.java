/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

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
import edu.ku.brc.ui.UICacheManager;
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
public class ConfigureCSV extends ConfigureExternalDataBase implements ConfigureExternalData
{
    private int     escapeMode;
    private char    delimiter;
    private char    textQualifier;
    private Charset charset;
    private static final Logger log = Logger.getLogger(ConfigureCSV.class);
    /**
     * Constructor sets defaults (hard coded).
     */
    public ConfigureCSV(final File file)
    {
        super();
        log.info("ConfigureCSV");
        escapeMode = getDefaultEscapeMode();
        delimiter  = getDefaultDelimiter();
        charset    = getDefaultCharset();
        textQualifier = getDefaultTextQualifier();
        getConfig(file);
    }

    public ConfigureCSV(Properties props)
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
                                 final int escapeModeArg)
    {
        if (externalFile != null)
        {
            try
            {
                InputStream input = new FileInputStream(externalFile);
                CsvReader result = new CsvReader(input, delimiterArg, charsetArg);
                result.setEscapeMode(escapeModeArg);
                return result;

            } catch (FileNotFoundException ex)
            {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * creates reader using current configuration
     * 
     * @return CsvReader for externalFile
     */
    private CsvReader makeReader()
    {
        return makeReader(delimiter, charset, escapeMode);
    }

    /**
     * Currently (tentatively) used to generate default column headers.
     * 
     * @return "Column"
     */
    private String getDefaultColHeader()
    {
        return "Column";
    }

    /**
     * Tentatively used to generate default column headers
     * 
     * @return ("Column1", "Column2", ...)
     */
    private String[] setupHeaders()
    {
        CsvReader csv = makeReader();
        if (csv != null)
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
                    return result;
                }
            } catch (IOException ex)
            {
                ex.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * Lame prompt for delimiter.
     * 
     * @return selected delimiter
     */
    private char determineDelimiter()
    {
        Vector<String> list = new Vector<String>();
        list.add(",");
        list.add("TAB");
        ChooseFromListDlg<String> dlg = new ChooseFromListDlg<String>((Frame)UICacheManager.get(UICacheManager.FRAME), 
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
    private char determineTextQualifier()
    {
        Vector<String> list = new Vector<String>();
        list.add("\"");
        list.add("\'");
        list.add("{none}");
        ChooseFromListDlg<String> dlg = new ChooseFromListDlg<String>((Frame)UICacheManager.get(UICacheManager.FRAME), 
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
    private Charset determineCharset()
    {
        Vector<String> list = new Vector<String>();
        list.add("default");
        list.add("US-ASCII");
        list.add("ISO-8859-1");
        list.add("UTF-8");
        ChooseFromListDlg<String> dlg = new ChooseFromListDlg<String>((Frame)UICacheManager.get(UICacheManager.FRAME), 
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
    private int determineEscapeMode()
    {
        Vector<String> list = new Vector<String>();
        list.add("backslash");
        list.add("doubled");
        
        ChooseFromListDlg<String> dlg = new ChooseFromListDlg<String>((Frame)UICacheManager.get(UICacheManager.FRAME), 
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

    private ImportColumnInfo.ColumnType getCellType(@SuppressWarnings("unused") int colIndex)
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
        delimiter = determineDelimiter();
        charset = determineCharset();
        escapeMode = determineEscapeMode();
        firstRowHasHeaders = determineFirstRowHasHeaders();
        textQualifier = determineTextQualifier();
        nonInteractiveConfig();
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
        CsvReader csv = makeReader();
        if (csv != null)
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

                colInfo = new Vector<ImportColumnInfo>(csv.getHeaderCount());

                for (int h = 0; h < csv.getHeaderCount(); h++)
                {
                    colInfo.add(new ImportColumnInfo((short)h, getCellType(h), csv.getHeader(h), null));
                }
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
            Collections.sort(colInfo);
        }
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
     * @return the textQualifier
     */
    public char getTextQualifier()
    {
        return textQualifier;
    }

    /**
     * @param textQualifier the textQualifier to set
     */
    public void setTextQualifier(char textQualifier)
    {
        this.textQualifier = textQualifier;
    }
}
