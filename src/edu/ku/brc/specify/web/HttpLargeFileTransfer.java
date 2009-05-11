/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.web;

/**
 * @author rods
 *
 */
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIRegistry;


/**
 * @author rods
 *
 */
public class HttpLargeFileTransfer
{
    protected static final Logger log = Logger.getLogger(HttpLargeFileTransfer.class);
    
    public static final int    BUFFER_SIZE             = 4096;

    public static final int    MAX_CHUNK_SIZE          = 1000 * BUFFER_SIZE;         // ~4.1MB
    public static final String FILE_NAME_HEADER        = "Transfer-File-Name";
    public static final String CLIENT_ID_HEADER        = "Transfer-Client-ID";
    public static final String FILE_CHUNK_HEADER       = "Transfer-File-Chunk";
    public static final String FILE_CHUNK_COUNT_HEADER = "Transfer-File-Chunk-Count";

    /**
     * @param infileName
     * @param outFileName
     * @param changeListener
     * @return
     */
    public boolean compressFile(final String infileName,
                                final String outFileName,
                                final ChangeListener changeListener)
    {
        final File file = new File(infileName);
        if (file.exists())
        {
            long fileSize = file.length();
            if (fileSize > 0)
            {
                SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
                {
                    protected String           errorMsg = null;
                    protected FileInputStream  fis      = null;
                    protected GZIPOutputStream fos      = null;
                    
                    /* (non-Javadoc)
                     * @see javax.swing.SwingWorker#doInBackground()
                     */
                    @Override
                    protected Integer doInBackground() throws Exception
                    {
                        try
                        {
                            Thread.sleep(100);
                            
                            long totalSize = file.length();
                            long bytesCnt  = 0;
                            
                            FileInputStream  fis = new FileInputStream(infileName);
                            GZIPOutputStream fos = new GZIPOutputStream(new FileOutputStream(outFileName));
                            
                            byte[] bytes = new byte[BUFFER_SIZE*10];
                            
                            while (true)
                            {
                                int len = fis.read(bytes);
                                if (len > 0)
                                {
                                    fos.write(bytes, 0, len);
                                    bytesCnt += len;
                                    firePropertyChange("MEGS", 0, (int)(((double)bytesCnt / (double)totalSize)*100.0));
                                } else
                                {
                                    break;
                                }
                            }
                            
                            fis.close();
                            fos.close();
                            
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                            errorMsg = ex.toString();
                            
                        } finally
                        {
                            try
                            {
                                if (fis != null)
                                {
                                    fis.close();
                                }
                                if (fos != null)
                                {
                                    fos.close();
                                }
                            } catch (IOException ex)
                            {
                                errorMsg = ex.toString();   
                            }
                        }
                        firePropertyChange("MEGS", 0, 100);
                        return null;
                    }

                    @Override
                    protected void done()
                    {
                        super.done();
                        
                        UIRegistry.getStatusBar().setProgressDone(HttpLargeFileTransfer.class.toString());
                        
                        UIRegistry.clearSimpleGlassPaneMsg();
                        
                        if (StringUtils.isNotEmpty(errorMsg))
                        {
                            UIRegistry.showError(errorMsg);
                        }
                    }
                };
                
                final JStatusBar statusBar = UIRegistry.getStatusBar();
                statusBar.setIndeterminate(HttpLargeFileTransfer.class.toString(), true);
                
                UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("Compressing Backup..."), 24);
                
                backupWorker.addPropertyChangeListener(
                        new PropertyChangeListener() {
                            public  void propertyChange(final PropertyChangeEvent evt) {
                                if ("MEGS".equals(evt.getPropertyName())) 
                                {
                                    long value = (Long)evt.getNewValue();
                                    double val = value / 10.0;
                                    statusBar.setText(UIRegistry.getLocalizedMessage("MySQLBackupService.BACKUP_MEGS", val));
                                }
                            }
                        });
                backupWorker.execute();
                
            } else
            {
                // file doesn't exist
            }
        } else
        {
            // file doesn't exist
        }
        return false;
    }
    
    
    /**
     * @param infileName
     * @param outFileName
     * @param changeListener
     * @return
     */
    public static boolean uncompressFile(final String infileName,
                                         final String outFileName,
                                         final ChangeListener changeListener)
    {
       File file = new File(infileName);
       if (file.exists())
       {
           long fileSize = file.length();
           if (fileSize > 0)
           {
               try
               {
                   FileInputStream      fis = new FileInputStream(infileName);
                   BufferedOutputStream fos = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outFileName)));
                   
                   byte[] bytes = new byte[BUFFER_SIZE*10];
                   
                   while (true)
                   {
                       int len = fis.read(bytes);
                       if (len > 0)
                       {
                           fos.write(bytes, 0, len);
                       } else
                       {
                           break;
                       }
                   }
                   
                   fis.close();
                   fos.close();
                   
                   return true;
                   
               } catch (IOException ex)
               {
                   ex.printStackTrace();
               }
           } else
           {
               // file doesn't exist
           }
       } else
       {
           // file doesn't exist
       }
       return false;
    }
    

    
    /**
     * @param fileName
     * @param urlStr
     */
    public static void transferFile(final String fileName, 
                                    final String urlStr)
    {
        try
        {
            URL url   = new URL(urlStr);
            File file = new File(fileName);
            if (file.exists())
            {
                long fileSize = file.length();
                if (fileSize > 0)
                {
                    FileInputStream fileIS = new FileInputStream(file);
        
                    int nChunks = (int) (fileSize / MAX_CHUNK_SIZE);
                    if (fileSize % MAX_CHUNK_SIZE > 0)
                    {
                        nChunks++;
                    }
        
                    byte[] buf = new byte[BUFFER_SIZE];
                    long bytesRemaining = fileSize;
        
                    String clientID = String.valueOf((long) (Long.MIN_VALUE * Math.random()));
        
                    for (int i = 0; i < nChunks; i++)
                    {
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("PUT");
                        conn.setDoOutput(true);
                        conn.setDoInput(true);
                        conn.setUseCaches(false);
        
                        int chunkSize = (int) ((bytesRemaining > MAX_CHUNK_SIZE) ? MAX_CHUNK_SIZE : bytesRemaining);
                        bytesRemaining -= chunkSize;
        
                        conn.setRequestProperty("Content-Type", "application/octet-stream");
                        conn.setRequestProperty("Content-Length", String.valueOf(chunkSize));
        
                        conn.setRequestProperty(CLIENT_ID_HEADER, clientID);
                        conn.setRequestProperty(FILE_NAME_HEADER, fileName);
                        conn.setRequestProperty(FILE_CHUNK_COUNT_HEADER, String.valueOf(nChunks));
                        conn.setRequestProperty(FILE_CHUNK_HEADER, String.valueOf(i));
        
                        OutputStream out       = conn.getOutputStream();
                        int          bytesRead = 0;
                        while (bytesRead < chunkSize)
                        {
                            int read = fileIS.read(buf);
                            if (read == -1)
                            {
                                break;
                                
                            } else if (read > 0)
                            {
                                bytesRead += read;
                                out.write(buf, 0, read);
                            }
                        }
                        out.close();
        
                        if (conn.getResponseCode() != HttpServletResponse.SC_OK)
                        {
                            System.err.println(conn.getResponseMessage());
                        }
        
                    }
                } else
                {
                    // file doesn't exist
                }
            } else
            {
                // file doesn't exist
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    public void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        String fileName = req.getHeader(HttpLargeFileTransfer.FILE_NAME_HEADER);
        if (fileName == null)
        {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Filename not specified");
        }

        String clientID = req.getHeader(HttpLargeFileTransfer.CLIENT_ID_HEADER);
        if (null == clientID)
        {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing Client ID");
        }

        int numChunks = req.getIntHeader(HttpLargeFileTransfer.FILE_CHUNK_COUNT_HEADER);
        int chunkCnt     = req.getIntHeader(HttpLargeFileTransfer.FILE_CHUNK_HEADER);

        if (numChunks == -1 || chunkCnt == -1)
        {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Missing chunk information");
        }

        if (chunkCnt == 0)
        {
            // check permission to create file here
        }

        OutputStream fos = null;
        if (numChunks == 1)
        {
            fos = new FileOutputStream(fileName); // create
        } else
        {
            fos = new FileOutputStream(getTempFile(clientID), (chunkCnt > 0)); // append
        }

        InputStream fis  = req.getInputStream();
        byte[]      buf = new byte[BUFFER_SIZE];
        int         totalLen = 0;
        
        while (true)
        {
            int len = fis.read(buf);
            if (len > 0)
            {
                totalLen += len;
                fos.write(buf, 0, len);
                
            } else if (len == -1)
            {
                break;
            }
        }
        fis.close();
        fos.close();

        if (numChunks > 1 && chunkCnt == numChunks - 1)
        {
            File tmpFile  = new File(getTempFile(clientID));
            File destFile = new File(fileName);
            if (destFile.exists())
            {
                destFile.delete();
            }
            if (!tmpFile.renameTo(destFile))
            {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to create file");
            } else
            {
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        } else
        {
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }

    /**
     * @param clientID
     * @return
     */
    private static String getTempFile(final String clientID)
    {
        return clientID + ".tmp";
    }


    /**
     * @param args
     */
    public static void main(String[] args)
    {
        transferFile(args[0], args[1]);
    }
}
