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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.AsynchronousFillHandle;
import net.sf.jasperreports.engine.fill.AsynchronousFilllListener;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JRViewer;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.UICacheManager;


/**
 * This class will display Label previews and may eventually hold a labels editor
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
    
    /**
     * 
     *
     */
    public LabelsPane(final String name, 
                      final Taskable task)
    {
        super(name, task);
    }
    
    /**
     * Set the text to the label (create the label if it doesn't exist)
     * @param msg the message to be displayed
     */
    protected void setLabelText(final String msg)
    {
        if (label == null)
        {
            removeAll();
            label = new JLabel("", JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }
        label.setText(msg);
        invalidate();
        doLayout();
        repaint();
    }
    
    /**
     * Starts the report creation process
     * @param fileName the XML file name of the report definition
     */
    public void createReport(final String fileName, final RecordSet recordSet)
    {
        this.recordSet = recordSet;
        
        String compiledName = getFileNameWithoutExt(fileName) + ".jasper";
        File compiledPath = null;
        
        File reportPath = new File(XMLHelper.getConfigDirPath(fileName));
        File cachePath  = checkAndCreateReportsCache();
        if (cachePath != null)
        {
            compiledPath = new File(cachePath.getAbsoluteFile() + File.separator + compiledName);
        }
        
        // check to see if it needs to be recompiled
        if (compiledPath != null && compiledPath.exists() && reportPath.lastModified() < compiledPath.lastModified())
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
                // 28594
                String itemnum = "";
                
                /* TEMPOARY 
                if ( recordSet == null)
                {
                    itemnum = JOptionPane.showInputDialog(this, getResourceString("AskCatalogNum"));
                    if (itemnum == null)
                    {
                        itemnum = "28594";
                    }
                    itemnum = "= " + itemnum;
                } else
                {
                    itemnum = DBTableIdMgr.getInClause(recordSet);
                }
                */
                itemnum = DBTableIdMgr.getInClause(recordSet);
                JasperReport jasperReport = (JasperReport)JRLoader.loadObject(compiledFile.getAbsoluteFile());
                if (jasperReport != null)
                {
                    
                    Map<Object, Object> parameters = new HashMap<Object, Object>();
                    parameters.put("itemnum", itemnum);
                    
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
            File path = new File(System.getProperty("user.home")+File.separator+"Specify");
            if (!path.exists())
            {
                if (!path.mkdir())
                {
                    String msg = "unable to create directory [" + path.getAbsolutePath() + "]";
                    log.error(msg); 
                    throw new RuntimeException(msg);
                }
            }
            path = new File(path.getAbsoluteFile()+File.separator+"reportsCache");
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
        }
    }
   
}
