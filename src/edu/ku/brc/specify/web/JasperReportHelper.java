/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.web;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JLabel;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.AsynchronousFillHandle;
import net.sf.jasperreports.engine.fill.AsynchronousFilllListener;
import net.sf.jasperreports.engine.query.JRHibernateQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.tasks.subpane.JasperReportsCache;
import edu.ku.brc.specify.tasks.subpane.ReportCompileInfo;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 29, 2008
 *
 */
public class JasperReportHelper implements AsynchronousFilllListener, JasperCompilerListener
{
    private static final Logger log = Logger.getLogger(JasperReportHelper.class);
    
    // Data Members
    protected JasperReportHelperListener listener;
    protected boolean                    isAsynchronous;
    
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
     * @param listener
     * @param isAsynchronous
     */
    public JasperReportHelper(final JasperReportHelperListener listener,
                              final boolean                    isAsynchronous)
    {
        this.listener       = listener;
        this.isAsynchronous = isAsynchronous;
        
        cachePath = JasperReportsCache.checkAndCreateReportsCache();
        
        // Checks for out of date Labels / Reports
        // and copies them over to the cache for compiling
        JasperReportsCache.refreshCacheFromDatabase();
    }

    
    /**
     * Starts the report creation process
     * @param fileName the XML file name of the report definition
     * @param recrdSet the recordset to use to fill the labels
     * @param paramsArg parameters for the report
    */
    public void createReport(final String     mainReportName, 
                             final Object     data, 
                             final Properties paramsArg)
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
        this.params = paramsArg;
     
        JasperReportsCache.refreshCacheFromDatabase();
        
        Vector<File>   reportFiles = new Vector<File>();
        AppResourceIFace appRes    = AppContextMgr.getInstance().getResource(mainReportName); 
        if (appRes != null)
        {
            String subReportsStr = appRes.getMetaData("subreports");
            String hqlStr        = appRes.getMetaData("hql");
            
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
                        throw new RuntimeException("Couldn't load subreport ["+mainReportName+"]"); // ??
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
            ReportCompileInfo info = JasperReportsCache.checkReport(file);
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
            System.out.println("Everything is compiled");

            this.compileComplete(files.get(files.size()-1).getCompiledFile());

        } else
        {
            System.out.println("About to Compile "+isAsynchronous);
            //progressLabel.setText(getResourceString("JasperReportCompiling"));
            compiler = new JasperCompilerRunnable(this, files);
            if (isAsynchronous)
            {
                compiler.start();
            } else
            {
                compiler.run();
            }
        }
    }

    /**
     * The compiling of the report is complete
     * @param report the completeed report, or null if there was a compiling error
     */
    public void compileComplete(final File compiledFile)
    {
        if (compiledFile != null)
        {
            try
            {
                System.out.println("Creating report...");

                JasperReport jasperReport = (JasperReport)JRLoader.loadObject(compiledFile.getAbsoluteFile());
                if (jasperReport != null)
                {
                    System.out.println("Loaded report...");
                    
                    Map<Object, Object> parameters = new HashMap<Object, Object>();
                    
                    parameters.put("RPT_IMAGE_DIR", JasperReportsCache.getImagePath().getAbsolutePath());
                    parameters.put("SUBREPORT_DIR", cachePath.getAbsoluteFile() + File.separator);
                    parameters.put("DATASOURCE", dataSource);
                    
                    if (recordSet != null)
                    {
                        parameters.put("itemnum", DBTableIdMgr.getInstance().getInClause(recordSet));
                    }
                    
                    // Add external parameters
                    if (params != null)
                    {
                        for (Object key : params.keySet())
                        {
                            //System.out.println("key["+key+"]  Val["+params.get(key)+"]");
                            parameters.put(key, params.get(key));
                        }
                    }
                    
                    // XXX What about losing a connection here?
                    if (requiresHibernate)
                    {
                        session = HibernateUtil.getNewSession();
                        parameters.put(JRHibernateQueryExecuterFactory.PARAMETER_HIBERNATE_SESSION, session);
                    }

                    if (isAsynchronous)
                    {
                        System.out.println("Filling report asynchronously...");
                        
                        //progressLabel.setText(getResourceString("JasperReportFilling"));
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
                        System.out.println("Filling report synchronously...");
                        JasperPrint jasperPrint;
                        if (recordSet != null)
                        {
                            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, DBConnection.getInstance().getConnection());
                        } else
                        {
                            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
                        }
                        
                        if (jasperPrint != null)
                        {
                            listener.complete(jasperPrint);
                        } else
                        {
                            listener.completedWithError();
                        }
                    }
                    
                } else
                {
                    log.error("jasperReport came back null ["+compiledFile.getAbsolutePath()+"]");
                    //progressBar.setIndeterminate(false);
                    //setLabelText(getResourceString("JasperReportReadingCachedReport"));
                }

            } catch (JRException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(JasperReportHelper.class, ex);
                //setLabelText(getResourceString("JasperReportCreatingViewer"));
                log.error(ex);
                ex.printStackTrace();
            }
        } else
        {
            //setLabelText(getResourceString("JasperReportCompileError"));
        }
        compiler = null;
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(JasperReportHelper.class, ex);
                log.error(ex);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.fill.AsynchronousFilllListener#reportFillError(java.lang.Throwable)
     */
    public void reportFillError(java.lang.Throwable t)
    {
        //removeAll();
        //setLabelText(getResourceString("JasperReportFillError"));
        
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

            listener.complete(print);
            
            //?? JRViewer jasperViewer = new JRViewer(print);
            
            // ?? add(jasperViewer, BorderLayout.CENTER);

            // ?? UIRegistry.forceTopFrameRepaint();

            closeSession();
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(JasperReportHelper.class, ex);
            log.error(ex);
            ex.printStackTrace();
        }
        asyncFillHandler = null;
    }
    
    //------------------------------------------------------------
    // Inner Classes
    //------------------------------------------------------------
    
    public interface JasperReportHelperListener
    {
        /**
         * @param compiledFile
         */
        public abstract void complete(final JasperPrint jasperPrint);
        
        /**
         * @param status
         */
        public abstract void status(final int status);
        
        /**
         * 
         */
        public abstract void completedWithError();
    }

    public class JasperCompilerRunnable implements Runnable
    {
        protected Thread                    thread;
        protected JasperCompilerListener    compileListener;
        protected Vector<ReportCompileInfo> files;

        /**
         * Constructs a an object to execute an SQL statement and then notify the listener
         * @param listener the listener
         * @param reportFile the file that contains the report
         * @param compiledFile the file that will contain the compiled report
         */
        public JasperCompilerRunnable(final JasperCompilerListener listener, 
                                      final Vector<ReportCompileInfo> files)
        {
            this.compileListener = listener;
            this.files    = files;
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
                System.out.println("Done  Compiling...");
                compileListener.compileComplete(files.get(files.size()-1).getCompiledFile());
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(JasperReportHelper.class, ex);
                log.error(ex);
                ex.printStackTrace();
                
                compileListener.compileComplete(null);
            }
            compileListener = null;
            files.clear();
            files = null;
        }
    }

}
