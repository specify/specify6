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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.AsynchronousFillHandle;
import net.sf.jasperreports.engine.fill.AsynchronousFilllListener;
import net.sf.jasperreports.engine.query.JRHibernateQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.ui.UIRegistry;


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
    
    private static boolean reportsCacheWasCleared = false;

    // Data Members
    protected AsynchronousFillHandle asyncFillHandler = null;
    protected JLabel                 label            = null;
    protected JasperCompilerRunnable compiler         = null;

    protected RecordSetIFace         recordSet        = null;
    protected JRDataSource           dataSource       = null;
    protected File                   cachePath        = null;
    
    protected Properties             params           = null;
    protected boolean                requiresHibernate = false;
    protected Session                session           = null;

    /**
     * Constructor.
     * @param name name of subpanel
     * @param task the owning task
     * @param params parameters for the report
     */
    public LabelsPane(final String     name,
                      final Taskable   task, 
                      final Properties params)
    {
        super(name, task);
        
        this.params = params;
       
        cachePath = checkAndCreateReportsCache();
        
        refreshCacheFromDatabase("jrxml/label");
        refreshCacheFromDatabase("jrxml/report");
        refreshCacheFromDatabase("jrxml/subreport");
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
            label = new JLabel("", SwingConstants.CENTER);
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
    protected void refreshCacheFromDatabase(final String mimeType)
    {
        Hashtable<String, File> hash = new Hashtable<String, File>();
        for (File f : cachePath.listFiles())
        {
            //log.info("Report Cache File["+f.getName()+"]");
            hash.put(f.getName(), f);  
        }
        
        for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType(mimeType))
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
            
            log.debug("Report Cache File["+ap.getName()+"]  updateCache["+updateCache+"]");
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
    public ReportCompileInfo checkReport(final File file)
    {
        String fileName     = file.getName();
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
        boolean needsCompiling = compiledPath.exists() && 
                                 appRes.getTimestampModified().getTime() < compiledPath.lastModified();
        
        return new ReportCompileInfo(reportPath, compiledPath, needsCompiling);
    }
    
    
    /**
     * Starts the report creation process
     * @param fileName the XML file name of the report definition
     * @param recrdSet the recordset to use to fill the labels
     * @param paramsArg parameters for the report
    */
    public void createReport(final String         mainReportName, 
                             final Object         data, 
                             final Properties     paramsArg)
    {
        if (data instanceof RecordSetIFace)
        {
            this.recordSet  = (RecordSetIFace)data;
            this.dataSource = null;
            
        } else if (data instanceof JRDataSource)
        {
            this.recordSet  = null;
            this.dataSource = (JRDataSource)data;
        }
        this.params    = paramsArg;
     
        refreshCacheFromDatabase("jrxml/label");
        refreshCacheFromDatabase("jrxml/report");
        refreshCacheFromDatabase("jrxml/subreport");
        
        Vector<File>   reportFiles = new Vector<File>();
        AppResourceIFace appRes    = AppContextMgr.getInstance().getResource(mainReportName); 
        if (appRes != null)
        {
            String subReportsStr = appRes.getMetaData("subreports");
            String hqlStr = appRes.getMetaData("hql");
            
            if (StringUtils.isNotEmpty(hqlStr))
            {
                requiresHibernate = Boolean.parseBoolean(hqlStr.toLowerCase());
            }
            
            if (StringUtils.isNotEmpty(subReportsStr))
            {
                String[] subReportNames = subReportsStr.split(",");
                for (String subReportName : subReportNames)
                {
                    AppResourceIFace subReportAppRes = AppContextMgr.getInstance().getResource(subReportName); 
                    if (subReportAppRes != null)
                    {
                        File subReportPath = new File(cachePath.getAbsoluteFile() + File.separator + subReportName);
                        if (subReportPath.exists())
                        {
                            reportFiles.add(subReportPath);
                            
                        } else
                        {
                            throw new RuntimeException("Subreport doesn't exist on disk ["+subReportPath.getAbsolutePath()+"]");
                        }
                        
                    } else
                    {
                        throw new RuntimeException("Couldn't load subreport ["+name+"]");
                    }
                }
            }
            
            File reportPath = new File(cachePath.getAbsoluteFile() + File.separator + mainReportName);
            if (reportPath.exists())
            {
                reportFiles.add(reportPath);
                
            } else
            {
                throw new RuntimeException("Subreport doesn't exist on disk ["+reportPath.getAbsolutePath()+"]");
            }
            
        } else
        {
            throw new RuntimeException("Couldn't load report/label ["+mainReportName+"]");
        }


        boolean allAreCompiled = true;
        Vector<ReportCompileInfo> files = new Vector<ReportCompileInfo>();
        for (File file : reportFiles)
        {
            ReportCompileInfo info = checkReport(file);
            files.add(info);
            if (!info.isCompiled())
            {
                allAreCompiled = false;
            }
        }

        // Check to see if it needs to be recompiled, if it doesn't need compiling then
        // call "compileComplete" directly to have it start filling the labels
        // otherswise create the compiler runnable and have it be compiled 
        // asynchronously
        if (allAreCompiled)
        {
            this.compileComplete(files.get(files.size()-1).getCompiledFile());

        } else
        {
            progressLabel.setText(getResourceString("JasperReportCompiling"));
            compiler = new JasperCompilerRunnable(this, files);
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
                JasperReport jasperReport = (JasperReport)JRLoader.loadObject(compiledFile.getAbsoluteFile());
                if (jasperReport != null)
                {
                    Map<Object, Object> parameters = new HashMap<Object, Object>();
                    
                    // XXX PREF - This will be converted to a Preference
                    File imageDir = UIRegistry.getAppDataSubDir("report_images", false);
                    // XXXX RELEASE - This Reference to demo_files will need to be removed
                    if (!imageDir.exists())
                    {
                        imageDir = new File("demo_files");
                    }

                    parameters.put("RPT_IMAGE_DIR", imageDir.getAbsolutePath());
                    parameters.put("SUBREPORT_DIR", cachePath.getAbsoluteFile() + File.separator);
                    
                    if (recordSet != null)
                    {
                        parameters.put("itemnum", DBTableIdMgr.getInClause(recordSet));
                    }
                    
                    // Add external parameters
                    if (params != null)
                    {
                        for (Object key : params.keySet())
                        {
                            parameters.put(key, params.get(key));
                        }
                    }
                    
                    if (requiresHibernate)
                    {
                        session = HibernateUtil.getNewSession();
                        parameters.put(JRHibernateQueryExecuterFactory.PARAMETER_HIBERNATE_SESSION, session);
                    }

                    progressLabel.setText(getResourceString("JasperReportFilling"));
                    if (recordSet != null)
                    {
                        asyncFillHandler = AsynchronousFillHandle.createHandle(jasperReport, parameters, DBConnection.getInstance().getConnection());
                    } else
                    {
                        asyncFillHandler = AsynchronousFillHandle.createHandle(jasperReport, parameters, dataSource);
                    }
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
        File path = UIRegistry.getAppDataSubDir("reportsCache", true); 
        if (path == null)
        {
            String msg = "The reportsCache directory is empty.";
            log.error(msg);
            throw new RuntimeException(msg);
        }
        
        // If the JVM version (Major Version) has changed the JasperReports need to be recompiled
        // so we remove all the ".jasper" files so they can be recompiled.
        if (SpecifyAppContextMgr.isNewJavaVersionAtAppStart() && !reportsCacheWasCleared)
        {
            try
            {
                //FileUtils.cleanDirectory(path);
                for (Iterator iter = FileUtils.iterateFiles(path, new String[] {"jasper"}, false);iter.hasNext();)
                {
                    FileUtils.forceDelete((File)iter.next());
                }
            } catch (Exception ex)
            {
               log.error(ex);
            }
            reportsCacheWasCleared = true;
        }
        
        return path;

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
    
    protected void closeSession()
    {
        
        if (session != null)
        {
            try
            {
                session.close();
                
            } catch (Exception ex)
            {
                log.error(ex);
            }
        }
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
        closeSession();
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

            UIRegistry.forceTopFrameRepaint();

            closeSession();
            
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
        protected Thread                    thread;
        protected LabelsPane                listener;
        protected Vector<ReportCompileInfo> files;

        /**
         * Constructs a an object to execute an SQL staement and then notify the listener
         * @param listener the listener
         * @param reportFile the file that contains the report
         * @param compiledFile the file that will contain the compiled report
         */
        public JasperCompilerRunnable(final LabelsPane listener, 
                                      final Vector<ReportCompileInfo> files)
        {
            this.listener     = listener;
            this.files   = files;
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
                for (ReportCompileInfo info : files)
                {
                    JasperCompileManager.compileReportToFile(info.getReportFile().getAbsolutePath(), info.getCompiledFile().getAbsolutePath());
                }
                listener.compileComplete(files.get(files.size()-1).getCompiledFile());

            } catch (Exception ex)
            {
                log.error(ex);
                listener.compileComplete(null);
            }
            listener     = null;
            files.clear();
            files = null;
        }
    }
    
    
    
    class ReportCompileInfo
    {
        protected File reportFile;
        protected File compiledFile;
        protected boolean needsCompiled;
        
        public ReportCompileInfo(File reportFile, File compiledFile, boolean needsCompiled)
        {
            super();
            this.reportFile = reportFile;
            this.compiledFile = compiledFile;
            this.needsCompiled = needsCompiled;
        }

        public File getCompiledFile()
        {
            return compiledFile;
        }

        public boolean isCompiled()
        {
            return needsCompiled;
        }

        public File getReportFile()
        {
            return reportFile;
        }
        
        
    }

}
