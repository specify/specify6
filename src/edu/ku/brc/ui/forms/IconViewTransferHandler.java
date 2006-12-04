/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui.forms;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class IconViewTransferHandler extends TransferHandler
{
    protected IconViewObj iconViewObj;
    /**
     * 
     */
    public IconViewTransferHandler(IconViewObj iconViewObj)
    {
        this.iconViewObj = iconViewObj;
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
    {
        for (DataFlavor df: transferFlavors)
        {
            if (df.equals(DataFlavor.javaFileListFlavor))
            {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(JComponent comp, Transferable t)
    {
        boolean result = false;
        if (!canImport(comp, t.getTransferDataFlavors()))
        {
            return false;
        }
        
        try
        {
            Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
            
            List<File> fileList = (List<File>)data;
            
            for (File f: fileList)
            {
                // import f
                boolean response = iconViewObj.addRecord(f);
                if (response)
                {
                    result = response;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
}
