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
package edu.ku.brc.specify.config.init;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 30, 2010
 *
 */
public class TaxonDownloadDlg extends ProgressDialog
{
    private static final String BAD_TAXON_XLS_DL  = "BAD_TAXON_XLS_DL";
    private static final String WRSZ_TAXON_XLS_DL = "WRSZ_TAXON_XLS_DL";
    
    public enum StatusType { eError, eBadFileSize, eOK}
    
    private static String PRG = "PRG";
    
    private URL        url;
    private File       outFile;
    private StatusType status            = StatusType.eError;
    private int        fileSizeInBytes = 0;
    
    private SwingWorker<StatusType, Object> worker = null;
    
    /**
     * @param title
     * @param includeBothBars
     * @param includeClose
     */
    public TaxonDownloadDlg(final URL url, final File outFile, final int fileSizeInBytes)
    {
        super(UIRegistry.getResourceString("DWN_TX_XLS"), false, true);
        
        this.url     = url;
        this.outFile = outFile;
        this.fileSizeInBytes = fileSizeInBytes;
        
        
    }
    
    /**
     * @return
     */
    public StatusType getStatus()
    {
        return status;
    }
    
    /**
     * @return
     */
    private StatusType downloaddTaxonFile()
    {
        setProcess(0, 100);
        
        URLConnection urlConnection;
        try
        {
            urlConnection = url.openConnection();
            urlConnection.connect();
            
            InputStream      input = url.openStream();
            DataInputStream  dis   = new DataInputStream(input);
            DataOutputStream fos   = new DataOutputStream(new FileOutputStream(outFile));
            
            byte[] bytes = new byte[32768];

            int totalBytes = 0;
            while (true)
            {
                int numBytesRead = dis.read(bytes);
                if (numBytesRead == -1)
                {
                    break;
                }
                totalBytes += numBytesRead;
                fos.write(bytes, 0, numBytesRead);
                worker.firePropertyChange(PRG, numBytesRead, totalBytes);
            }
            fos.flush();
            fos.close();

            status = totalBytes != fileSizeInBytes ? StatusType.eBadFileSize : StatusType.eOK;
            
            return status;
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return status = StatusType.eError;
    }

    /**
     * 
     */
    private void startWork()
    {
        worker = new SwingWorker<StatusType, Object>()
        {
            @Override
            protected StatusType doInBackground() throws Exception
            {
                
                return downloaddTaxonFile();
            }

            @Override
            protected void done()
            {
                endWork();
            }
        };
        
        worker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (PRG.equals(evt.getPropertyName())) 
                        {
                            int byteRead = (Integer)evt.getNewValue();
                            int percent = (int)(((double)byteRead / (double)fileSizeInBytes) * 100.0);
                            setProcess(percent);
                        }
                    }
                });
        
        worker.execute();
    }
    
    /**
     * 
     */
    protected void endWork()
    {
        if (status == StatusType.eOK)
        {
            setVisible(false);
            dispose();
            
        } else
        {
            String msg = UIRegistry.getResourceString( status == StatusType.eError ? BAD_TAXON_XLS_DL : WRSZ_TAXON_XLS_DL);
            int rv = UIRegistry.askYesNoLocalized("DWNLD_TRY_AGAIN", "SKIP", msg, "ERROR");
            if (rv == JOptionPane.NO_OPTION)
            {
                // leave isOK false
                setVisible(false);
                dispose();
                
            } else
            {
                startWork();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.Dialog#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean b)
    {
        if (!isVisible() && b)
        {
            startWork();
        }
        
        super.setVisible(b);
    }
}