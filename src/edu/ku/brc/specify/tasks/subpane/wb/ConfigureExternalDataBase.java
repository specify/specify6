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
        log.debug("ConfigureExternalDataBase()");
        interactive = true;
        firstRowHasHeaders = true;
        appendData = false;     
    }

    public ConfigureExternalDataBase(final Properties props)
    {
        log.debug("ConfigureExternalDataBase(Properties props)");
        interactive        = (props.getProperty("interactive", "true") == "true");
        firstRowHasHeaders = (props.getProperty("firstRowHasHeaders", "false") == "true");
        appendData         = (props.getProperty("appendData", "false") == "false");
        fileName           = props.getProperty("fileName");
        
        readHeaders(props.getProperty("headers"));
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
                log.error(e);
            }
        }
    }
    
    protected String getHeaderString()
    {
        if (headers == null) 
        { 
            return null; 
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
        Object[] options = { "Yes", "No" };
        int n = JOptionPane.showOptionDialog(null, "Does the first row contain column names?", "",
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
        log.debug("ConfigureExternalDataBase getConfig(File)" + file.toString());
        externalFile = file;
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
        log.debug("ConfigureExternalDataBase getProperties()" );
        Properties result = new Properties();
        addBoolProperty(result, "interactive", interactive);
        addBoolProperty(result, "firstRowHasHeaders", firstRowHasHeaders);
        addBoolProperty(result, "appendData", appendData);
        result.setProperty("fileName", fileName);
        result.setProperty("headers", this.getHeaderString());
        return result;
    }
    
    protected void addBoolProperty(final Properties props, final String key, final boolean value)    
    {
        props.setProperty(key, (value ? "true" : "false"));
    }
}
