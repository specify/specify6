/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
package edu.ku.brc.af.ui.forms;

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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(IconViewTransferHandler.class, e);
            e.printStackTrace();
        }
        return result;
    }
}
