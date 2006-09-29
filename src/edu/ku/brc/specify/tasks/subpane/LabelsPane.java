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

package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.AsynchronousFillHandle;
import net.sf.jasperreports.engine.fill.AsynchronousFilllListener;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JRViewer;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.UICacheManager;


/**
 * This class will display Label previews and may eventually hold a labels editor
 *
 * @code_status Complete
 *
 * @author rods
 *
 *
 */
@SuppressWarnings("serial")
public class LabelsPane extends BaseSubPane implements AsynchronousFilllListener
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(LabelsPane.class);

    // Data Members
    protected AsynchronousFillHandle asyncFillHandler = null;
    protected JLabel                 label            = null;
    protected JasperCompilerRunnable compiler         = null;

    protected RecordSet              recordSet        = null;
    protected File                   cachePath        = null;

    /**
     * Constructor.
     * @param name name of subpanel
     * @param task the owning task
     */
    public LabelsPane(final String name,
                      final Taskable task)
    {
        super(name, task);
        
        cachePath = checkAndCreateReportsCache();
        
        refreshCacheFromDatabase();
    }

    /**
     * Set the text to the label (create the label if it doesn't exist)
     * @param msg the message to be displayed
     */
    public void setLabelText(final String msg)
    {
        if (label == null)
        {
            removeAll();
            label = new JLabel("", JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                label.setText(msg);
                invalidate();
                doLayout();
                repaint();
            }
          });

    }
    
    /**
     * Returns whether the LabelsPane contains a "label".
     * @return  whether the LabelsPane contains a "label".
     */
    public boolean isEmpty()
    {
        return label != null;
    }

    /**
     * Checks to see if any files in the database need to be copied to the database. A file may not
     * exist or be out of date.
     */
    protected void refreshCacheFromDatabase()
    {
        Hashtable<String, File> hash = new Hashtable<String, File>();
        for (File f : cachePath.listFiles())
        {
            log.info(f.getName());
            hash.put(f.getName(), f);  
        }
        
        for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType("jrxml/label"))
        {
            
            boolean updateCache = false;
            File file = hash.get(ap.getName());
            if (file == null)
            {
                updateCache = true;
                
            } else
            {
                Date fileDate = new Date(file.lastModified());
                updateCache = fileDate.getTime() < ap.getTimestampModified().getTime();
            }
            
            log.info(ap.getName()+"  "+updateCache);
            if (updateCache)
            {
                File localFilePath = new File(cachePath.getAbsoluteFile() + File.separator + ap.getName());
                try
                {
                    XMLHelper.setContents(localFilePath, ap.getDataAsString());
                    
                } catch (Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
    /**
     * Starts the report creation process
     * @param fileName the XML file name of the report definition
     * @param recrdSet the recordset to use to fill the labels
     */
    public void createReport(final String fileName, final RecordSet recrdSet)
    {
        this.recordSet = recrdSet;
     
        refreshCacheFromDatabase();

        String compiledName = getFileNameWithoutExt(fileName) + ".jasper";
        File   compiledPath = new File(cachePath.getAbsoluteFile() + File.separator + compiledName);

        AppResourceIFace appRes = AppContextMgr.getInstance().getResource(fileName);

        File reportPath = new File(cachePath.getAbsoluteFile() + File.separator + fileName);
        try
        {
            XMLHelper.setContents(reportPath, appRes.getDataAsString());
            
        } catch (Exception ex)
        {
            log.error(ex);
            throw new RuntimeException(ex);
        }
       

        // Check to see if it needs to be recompiled, if it doesn't need compiling then
        // call "compileComplete" directly to have it start filling the labels
        // otherswise create the compiler runnable and have it be compiled 
        // asynchronously
        if (compiledPath != null && compiledPath.exists() && appRes.getTimestampModified().getTime() < compiledPath.lastModified())
        {
            this.compileComplete(compiledPath);

        } else
        {
            progressLabel.setText(getResourceString("JasperReportCompiling"));
            compiler = new JasperCompilerRunnable(this, reportPath, compiledPath);
            compiler.start();
        }
    }

    /**
     * The compiling of the report is complete
     * @param report the completeed report, or null if there was a compiling error
     */
    protected void compileComplete(final File compiledFile)
    {
        if (compiledFile != null)
        {
            try
            {
                String itemnum = "";

                if (recordSet != null)
                {
                    itemnum = DBTableIdMgr.getInClause(recordSet);
                }
                
                JasperReport jasperReport = (JasperReport)JRLoader.loadObject(compiledFile.getAbsoluteFile());
                if (jasperReport != null)
                {

                    Map<Object, Object> parameters = new HashMap<Object, Object>();
                    if (recordSet != null)
                    {
                        parameters.put("itemnum", itemnum);
                    }

                    progressLabel.setText(getResourceString("JasperReportFilling"));
                    asyncFillHandler = AsynchronousFillHandle.createHandle(jasperReport, parameters, DBConnection.getConnection());
                    asyncFillHandler.addListener(this);
                    asyncFillHandler.startFill();
                } else
                {
                    log.error("jasperReport came back null ["+compiledFile.getAbsolutePath()+"]");
                    progressBar.setIndeterminate(false);
                    setLabelText(getResourceString("JasperReportReadingCachedReport"));
                }

            } catch (JRException ex)
            {
                setLabelText(getResourceString("JasperReportCreatingViewer"));
                log.error(ex);
                ex.printStackTrace();
            }
        } else
        {
            setLabelText(getResourceString("JasperReportCompileError"));
        }
        compiler = null;
    }

    /**
     * XXX This really needs to be moved to a more centralized location
     *
     */
    public static File checkAndCreateReportsCache()
    {
        try
        {
            File path = new File(UICacheManager.getDefaultWorkingPath() + File.separator + "reportsCache"); 
            if (!path.exists())
            {
                if (!path.mkdir())
                {
                    String msg = "unable to create directory [" + path.getAbsolutePath() + "]";
                    log.error(msg);
                    throw new RuntimeException(msg);
                }
            }
            return path;

        } catch (Exception ex)
        {
           log.error(ex);
        }
        return null;
    }

    /**
     * Returns just the name part with the path or the extension
     * @param path the fill path with file name and extension
     * @return Returns just the name part with the path or the extension
     */
    protected String getFileNameWithoutExt(final String path)
    {
        int inx = path.indexOf(File.separator);
        return path.substring(inx+1, path.lastIndexOf('.'));
    }

    //------------------------------------------------------------
    // AsynchronousFilllListener
    //------------------------------------------------------------

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.fill.AsynchronousFilllListener#reportCancelled()
     */
    public void reportCancelled()
    {
        asyncFillHandler = null;
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.fill.AsynchronousFilllListener#reportFillError(java.lang.Throwable)
     */
    public void reportFillError(java.lang.Throwable t)
    {
        removeAll();
        setLabelText(getResourceString("JasperReportFillError"));
        log.error(t);
        t.printStackTrace();
        asyncFillHandler = null;
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.fill.AsynchronousFilllListener#reportFinished(net.sf.jasperreports.engine.JasperPrint)
     */
    public void reportFinished(JasperPrint print)
    {
        try
        {
            removeAll();
            label = null;

            JRViewer jasperViewer = new JRViewer(print);
            add(jasperViewer, BorderLayout.CENTER);

            UICacheManager.forceTopFrameRepaint();


        } catch (Exception ex)
        {
            setLabelText(getResourceString("JasperReportCreatingViewer"));
            log.error(ex);
            ex.printStackTrace();
        }
        asyncFillHandler = null;
    }

    //------------------------------------------------------------
    // Inner Classes
    //------------------------------------------------------------
    public class JasperCompilerRunnable implements Runnable
    {
        protected Thread               thread;
        protected LabelsPane           listener;
        protected File                 reportFile;
        protected File                 compiledFile;

        /**
         * Constructs a an object to execute an SQL staement and then notify the listener
         * @param listener the listener
         * @param reportFile the file that contains the report
         * @param compiledFile the file that will contain the compiled report
         */
        public JasperCompilerRunnable(final LabelsPane listener, final File reportFile, final File compiledFile)
        {
            this.listener     = listener;
            this.reportFile   = reportFile;
            this.compiledFile = compiledFile;
        }


        /**
         * Starts the thread to make the SQL call
         *
         */
        public void start()
        {
            thread = new Thread(this);
            thread.start();
        }

        /**
         * Stops the thread making the call
         *
         */
        public synchronized void stop()
        {
            if (thread != null)
            {
                thread.interrupt();
            }
            thread = null;
            notifyAll();
        }

        /**
         * Creates a connection, makes the call and returns the results
         */
        public void run()
        {
            try
            {
                JasperCompileManager.compileReportToFile(reportFile.getAbsolutePath(), compiledFile.getAbsolutePath());
                listener.compileComplete(compiledFile);

            } catch (Exception ex)
            {
                log.error(ex);
                listener.compileComplete(null);
            }
            listener     = null;
            reportFile   = null;
            compiledFile = null;
        }
    }

}
