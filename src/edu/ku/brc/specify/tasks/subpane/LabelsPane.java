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

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.AsynchronousFillHandle;
import net.sf.jasperreports.engine.fill.AsynchronousFilllListener;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.query.JRHibernateQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase;
import edu.ku.brc.specify.tasks.subpane.qb.ReportParametersPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;


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
public class LabelsPane extends BaseSubPane implements AsynchronousFilllListener, JasperCompileListener
{
    // Static Data Members
    protected static final Logger    log = Logger.getLogger(LabelsPane.class);
    
    protected static int         virtualizerThresholdSize = 666;
    
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
            label = createLabel("", SwingConstants.CENTER);
            add(label, BorderLayout.CENTER);
        }
        
        if (progressBarPanel != null)
        {
            remove(progressBarPanel);
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
        if (progressBarPanel != null)
        {
            if (label != null)
            {
                remove(label);
            }
            add(progressBarPanel, BorderLayout.CENTER);
        }
        
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

        AppResourceIFace appRes    = AppContextMgr.getInstance().getResource(mainReportName); 
        String hqlStr = appRes.getMetaData("hql");
        if (StringUtils.isNotEmpty(hqlStr))
        {
            requiresHibernate = Boolean.parseBoolean(hqlStr.toLowerCase());
        }

        File compiledFile = (File )params.get("compiled-file");
        if (compiledFile != null)
        {
            compileComplete(compiledFile);
        }
        else
        {
            progressLabel.setText(getResourceString("JasperReportCompiling"));            
            compiler = new JasperCompilerRunnable(this, mainReportName); 
            compiler.start();
        }        
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.JasperCompileListener#compileComplete(java.io.File)
     */
    public void compileComplete(final File compiledFile)
    {
        if (compiledFile != null)
        {
            try
            {
                JasperReport jasperReport = (JasperReport)JRLoader.loadObject(compiledFile.getAbsoluteFile());
                if (jasperReport != null)
                {
                    int size = -1;
                    if (recordSet != null)
                    {
                        size = recordSet.getNumItems();
                        
                    } else if (dataSource != null)
                    {
                        if (dataSource instanceof QBJRDataSourceBase)
                        {
                            //if source has not finished retrieving results, size = -1
                            size = ((QBJRDataSourceBase )dataSource).size();
                        }
                    }

                    boolean go = true;
                    ReportParametersPanel rpp = null;
                    String skipParams = params.getProperty("skip-parameter-prompt");
                    if (StringUtils.isBlank(skipParams) || skipParams.equalsIgnoreCase("false"))
                    {
                        rpp = new ReportParametersPanel(jasperReport, true);
                        if (rpp.getParamCount() > 0)
                        {
                            CustomDialog cd = new CustomDialog((Frame) UIRegistry.getTopWindow(), UIRegistry
                                    .getResourceString("LabelsPane.REPORT_PARAMS"), true, rpp);
                            UIHelper.centerAndShow(cd);
                            go = !cd.isCancelled(); // XXX what about x box?
                            cd.dispose();

                        }
                    }
                    
                    if (go)
                    {
                        Map<Object, Object> parameters = new HashMap<Object, Object>();

                        if (rpp != null)
                        {
                            for (int p = 0; p < rpp.getParamCount(); p++)
                            {
                                Pair<String, String> param = rpp.getParam(p);
                                if (StringUtils.isNotBlank(param.getSecond()))
                                {
                                    parameters.put(param.getFirst(), param.getSecond());
                                }
                            }
                        }
                        
                        parameters.put("RPT_IMAGE_DIR", JasperReportsCache.getImagePath()
                                .getAbsolutePath());
                        parameters.put("SUBREPORT_DIR", cachePath.getAbsoluteFile()
                                + File.separator);
                        parameters.put("DATASOURCE", dataSource);

                        if (recordSet != null)
                        {
                            parameters.put("itemnum", DBTableIdMgr.getInstance().getInClause(
                                    recordSet));
                        }
                        if (size > virtualizerThresholdSize)
                        {
                            JRFileVirtualizer fileVirtualizer = new JRFileVirtualizer(10);
                            parameters.put(JRParameter.REPORT_VIRTUALIZER, fileVirtualizer);
                        }
                        // Add external parameters
                        if (params != null)
                        {
                            for (Object key : params.keySet())
                            {
                                // System.out.println("key["+key+"] Val["+params.get(key)+"]");
                                parameters.put(key, params.get(key));
                            }
                        }

                        // XXX What about losing a connection here?
                        if (requiresHibernate)
                        {
                            session = HibernateUtil.getNewSession();
                            parameters.put(
                                    JRHibernateQueryExecuterFactory.PARAMETER_HIBERNATE_SESSION,
                                    session);
                        }

                        progressLabel.setText(getResourceString("JasperReportFilling"));
                        if (recordSet != null)
                        {
                            asyncFillHandler = AsynchronousFillHandle.createHandle(jasperReport,
                                    parameters, DBConnection.getInstance().getConnection());
                        }
                        else
                        {
                            asyncFillHandler = AsynchronousFillHandle.createHandle(jasperReport,
                                    parameters, dataSource);
                        }
                        asyncFillHandler.addListener(this);
                        asyncFillHandler.startFill();
                    }
                    else
                    {
                        removeAll();
                        setLabelText(getResourceString("LabelsPane.ReportCancelled"));
                    }
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

    /**
     * @author timo
     *
     *cheap hack for SPNCH demo. keeping it around for debugging.
     */
    protected class JRViewerPrintHackForSPNHCBDayQueryLinuxOnly extends JRViewer
    {
        protected final JasperPrint jp;
        
        JRViewerPrintHackForSPNHCBDayQueryLinuxOnly(final JasperPrint print)
        {
            super(print);
            jp = print;
            ActionListener[] lists = btnPrint.getActionListeners();
            for (int l = 0; l < lists.length; l++)
            {
                btnPrint.removeActionListener(lists[l]);
            }
            this.btnPrint.addActionListener(new ActionListener()
            {

                /* (non-Javadoc)
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                public void actionPerformed(ActionEvent e)
                {
                    try
                    {
                        JasperExportManager.exportReportToPdfFile(jp, "/home/timo/hbd.pdf");
                        Runtime.getRuntime().exec("lp -P1 /home/timo/hbd.pdf");
                    }
                    catch (Exception ex)
                    {
                        setLabelText(getResourceString("JasperReportCreatingViewer"));
                        //log.error(ex);
                        ex.printStackTrace();
                    }
                }
                
            });
        }
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
            
            //this vile hack for SPNHC demo may be useful for debugging...
//            JRViewer jasperViewer = new JRViewerPrintHackForSPNHCBDayQueryLinuxOnly(print);            
            //... end vile hack
            
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

}
