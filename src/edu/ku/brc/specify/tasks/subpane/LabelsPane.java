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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
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
    protected ImageIcon              icon              = null;

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
       
        cachePath = JasperReportsCache.checkAndCreateReportsCache();
        
        // Checks for out of date Labels / Reports
        // and copies them over to the cache for compiling
        JasperReportsCache.refreshCacheFromDatabase();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#getIcon()
     */
    @Override
    public Icon getIcon()
    {
        return icon != null ? icon : super.getIcon();
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(ImageIcon icon)
    {
        this.icon = icon;
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
        this.params    = paramsArg;
     
        JasperReportsCache.refreshCacheFromDatabase();
        
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
                    
                    parameters.put("RPT_IMAGE_DIR", JasperReportsCache.getImagePath().getAbsolutePath());
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

}
