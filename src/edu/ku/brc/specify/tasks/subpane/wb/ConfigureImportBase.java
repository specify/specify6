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
import java.util.Vector;

import javax.swing.JOptionPane;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Base class for workbench import configuration.
 */
public abstract class ConfigureImportBase
{
    protected File                     inputFile;
    protected Vector<ImportColumnInfo> colInfo = null;
    protected boolean                  firstRowHasHeaders;
    protected boolean                  interactive;

    public ConfigureImportBase()
    {
        interactive = true;
        firstRowHasHeaders = false;
    }

    public void setInteractive(boolean arg)
    {
        interactive = arg;
    }

    public boolean getFirstRowHasHeaders()
    {
        return firstRowHasHeaders;
    }

    public Vector<ImportColumnInfo> getColInfo()
    {
        return colInfo;
    }

    public File getFile()
    {
        return inputFile;
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
    public void getConfig(File file)
    {
        inputFile = file;
        if (interactive)
        {
            interactiveConfig();
        } else
        {
            nonInteractiveConfig();
        }
    }
}
