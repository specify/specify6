/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Base class for workbench import configuration.
 */
public abstract class ConfigureExternalDataBase implements ConfigureExternalDataIFace
{
    private static final Logger log = Logger.getLogger(ConfigureExternalDataBase.class);
    
    private final String TRUE  = "true";
    private final String FALSE = "false";
    
    protected Status                   status = Status.None;
    
    protected File                     externalFile;
    protected String                   fileName;
    protected Vector<ImportColumnInfo> colInfo = null;
    protected boolean                  firstRowHasHeaders;
    protected boolean                  interactive;
    protected boolean                  appendData;
    protected String[]                 headers;
    
    public ConfigureExternalDataBase()
    {
        log.debug("ConfigureExternalDataBase()"); //$NON-NLS-1$
        interactive = true;
        firstRowHasHeaders = true;
        appendData = false;     
    }

    public ConfigureExternalDataBase(final Properties props)
    {
        log.debug("ConfigureExternalDataBase(Properties props)"); //$NON-NLS-1$
        interactive        = (props.getProperty("interactive", TRUE) == TRUE); //$NON-NLS-1$
        firstRowHasHeaders = (props.getProperty("firstRowHasHeaders", FALSE) == TRUE); //$NON-NLS-1$
        appendData         = (props.getProperty("appendData", FALSE) == FALSE); //$NON-NLS-1$
        fileName           = props.getProperty("fileName"); //$NON-NLS-1$
        
        readHeaders(props.getProperty("headers")); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataIFace#getStatus()
     */
    public Status getStatus()
    {
        return status;
    }

    protected void readHeaders(final String prop)
    {
        if (prop != null)
        {
            CsvReader csv = new CsvReader(new StringReader(prop), ',');
            try
            {
                if (csv.readHeaders())
                {
                    headers = csv.getHeaders();
                }
            }
            catch (IOException e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConfigureExternalDataBase.class, e);
                log.error(e);
            }
        }
    }
    
    protected String getHeaderString()
    {
        if (headers == null) 
        { 
            return "";  //$NON-NLS-1$
        }
        StringWriter sw = new StringWriter();
        CsvWriter csv = new CsvWriter(sw, ',');
        try
        {
            csv.writeRecord(headers, true);
            csv.flush();
            
        } catch (IOException e)
        {
            log.error(e);
        }
        return sw.toString();
    }
    
    public String[] getHeaders()
    {
        return headers;
    }
    
    public void setHeaders(final String[] headers)
    {
        this.headers = headers;
    }
    
    public void setInteractive(final boolean arg)
    {
        interactive = arg;
    }

    public boolean getFirstRowHasHeaders()
    {
        return firstRowHasHeaders;
    }
    
    public void setFirstRowHasHeaders(final boolean value)
    {
        firstRowHasHeaders = value;
    }

    public Vector<ImportColumnInfo> getColInfo()
    {
        return colInfo;
    }

    public File getFile()
    {
        return externalFile;
    }

    public boolean getAppendData()
    {
        return appendData;
    }
    
    public String getFileName()
    {
        return fileName;
    }
    
    protected boolean determineFirstRowHasHeaders()
    {
        Object[] options = { getResourceString("Yes"), getResourceString("No") }; //$NON-NLS-1$ //$NON-NLS-2$
        int n = JOptionPane.showOptionDialog(null, UIRegistry.getResourceString("ConfigureExternalDataBase.DOES_1ST_COL_NM"), "", //I18N //$NON-NLS-1$ //$NON-NLS-2$
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return n == JOptionPane.YES_OPTION;
    }

    protected abstract void interactiveConfig();

    protected abstract void nonInteractiveConfig();

    /**
     * Sets up the properties used when importing the file.
     * @param file - the file containing data to be imported to a workbench
     */
    public void readConfig(final File file)
    {
        log.debug("ConfigureExternalDataBase getConfig(File)" + file.toString()); //$NON-NLS-1$
        externalFile = file;
        fileName = externalFile.getName();
        if (interactive)
        {
            interactiveConfig();
        } else
        {
            nonInteractiveConfig();
        }
    }
    
    public Properties getProperties()
    {
        log.debug("ConfigureExternalDataBase getProperties()" ); //$NON-NLS-1$
        Properties result = new Properties();
        addBoolProperty(result, "interactive", interactive); //$NON-NLS-1$
        addBoolProperty(result, "firstRowHasHeaders", firstRowHasHeaders); //$NON-NLS-1$
        addBoolProperty(result, "appendData", appendData); //$NON-NLS-1$
        if (fileName != null)
        {
            result.setProperty("fileName", fileName); //$NON-NLS-1$
        }
        result.setProperty("headers", this.getHeaderString()); //$NON-NLS-1$
        return result;
    }
    
    protected void addBoolProperty(final Properties props, final String key, final boolean value)    
    {
        props.setProperty(key, (value ? TRUE : FALSE));
    }
}
