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
package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.createTextArea;
import static edu.ku.brc.ui.UIRegistry.forceTopFrameRepaint;
import static edu.ku.brc.ui.UIRegistry.getMostRecentWindow;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSource;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceBase;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace;
import edu.ku.brc.specify.tasks.subpane.qb.ReportParametersPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
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
public class LabelsPane extends BaseSubPane implements AsynchronousFilllListener, 
                                                       JasperCompileListener, 
                                                       QBDataSourceListenerIFace
{
    // Static Data Members
    protected static final Logger    log = Logger.getLogger(LabelsPane.class);
    
    protected static int         virtualizerThresholdSize = 666;
    
    // Data Members
    protected AsynchronousFillHandle asyncFillHandler = null;
    protected JLabel                 label            = null;
    protected JasperCompilerRunnable compiler         = null;
    protected AtomicBoolean			 compiling        = new AtomicBoolean(false);
    protected AtomicBoolean			 filling          = new AtomicBoolean(false);
    protected AtomicBoolean          loading          = new AtomicBoolean(false);
    protected RecordSetIFace         recordSet        = null;
    protected JRDataSource           dataSource       = null;
    protected File                   cachePath        = null;
    
    protected Properties             params           = null;
    protected boolean                requiresHibernate = false;
    protected Session                session           = null;
    protected ImageIcon              icon              = null;
    
    protected java.lang.Throwable    errThrowable      = null;
    

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
        super(name, task, true, true);
        
        this.params = params;
       
        cachePath = JasperReportsCache.checkAndCreateReportsCache();
        
        progressCancelBtn.addActionListener(new ActionListener(){

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (compiler != null && compiling.get())
				{
					compiler.stop();
				}
				else if (asyncFillHandler != null && filling.get())
				{
					try
					{
						asyncFillHandler.cancellFill();
						if (dataSource != null && (dataSource instanceof QBDataSource)
								&& loading.get())
						{
							((QBDataSource )dataSource).cancelLoad();
						}
					} catch (JRException ex)
					{
						log.error(ex);
					}
				}
			}
        	
        });
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
            
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g,p,2px,p,f:p:g", "f:p:g, p, f:p:g"), this);//$NON-NLS-1$ //$NON-NLS-2$
            
            JButton moreBtn = createIconBtn("InfoIcon", IconManager.IconSize.Std16, null, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    showErrorMsgs();
                }
            });
            label = createLabel("", SwingConstants.CENTER);
            pb.add(label, cc.xy(2, 2));
            pb.add(moreBtn, cc.xy(4, 2));
            
            moreBtn.setEnabled(true);
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
    
    private void showErrorMsgs()
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        JTextArea       ta = createTextArea(40,80);
        pb.add(createScrollPane(ta, true), cc.xy(1,1));
        pb.setDefaultDialogBorder();
        
        ta.setText(errThrowable != null ? getStackTrace(errThrowable) : "");
        ta.setEditable(false);
        
        CustomDialog dlg = new CustomDialog((Frame)getMostRecentWindow(), getResourceString("Error"), true, CustomDialog.OK_BTN, pb.getPanel());
        dlg.setOkLabel(getResourceString("CLOSE"));
        centerAndShow(dlg);
    }
    
    /**
     * @param throwable
     * @return
     */
    protected String getStackTrace(Throwable throwable)
    {
        String        sep    = System.getProperty("line.separator");
        StringBuilder result = new StringBuilder();
        result.append(throwable.toString());
        result.append(sep);

        for (StackTraceElement element : throwable.getStackTrace())
        {
            result.append(element);
            result.append(sep);
        }
        return result.toString();
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
        else
        {
            this.recordSet = null;
            this.dataSource = null;
        }
        this.params    = paramsArg;

        AppResourceIFace appRes    = AppContextMgr.getInstance().getResource(mainReportName); 
        String resName = params.getProperty("name");
        if (appRes == null)
        {
            if (resName != null)
            {
                appRes = AppContextMgr.getInstance().getResource(resName); 
            }
        }
        if (appRes != null)
        {
            String hqlStr = appRes.getMetaData("hql");
            if (StringUtils.isNotEmpty(hqlStr))
            {
                requiresHibernate = Boolean.parseBoolean(hqlStr.toLowerCase());
            }
        }
        else
        {
            log.error("could not find report resource for " + mainReportName);
        }

        File compiledFile = (File )params.get("compiled-file");
        if (compiledFile != null)
        {
            compileComplete(compiledFile, null);
        }
        else
        {
            progressLabel.setText(getResourceString("JasperReportCompiling"));            
            compiler = new JasperCompilerRunnable(this, mainReportName, resName); 
            compiling.set(true);
            compiler.start();
        }        
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.JasperCompileListener#compileComplete(java.io.File)
     */
    public void compileComplete(final File compiledFile, final Throwable throwable)
    {
        compiling.set(false);
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
                        if (dataSource instanceof QBDataSourceBase)
                        {
                            //if source has not finished retrieving results, size = -1
                            size = ((QBDataSourceBase )dataSource).size();
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
                            CustomDialog cd = new CustomDialog((Frame) getTopWindow(), getResourceString("LabelsPane.REPORT_PARAMS"), true, rpp);
                            centerAndShow(cd);
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
                        
                        parameters.put("RPT_IMAGE_DIR", JasperReportsCache.getImagePath().getAbsolutePath());
                        parameters.put("SUBREPORT_DIR", cachePath.getAbsoluteFile() + File.separator);
                        parameters.put("DATASOURCE", dataSource);
                        
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
                        if (recordSet != null)
                        {
                            parameters.put("itemnum", DBTableIdMgr.getInstance().getInClause(recordSet));
                        }

                        // XXX What about losing a connection here?
                        if (requiresHibernate)
                        {
                            session = HibernateUtil.getNewSession();
                            parameters.put(
                                    JRHibernateQueryExecuterFactory.PARAMETER_HIBERNATE_SESSION,
                                    session);
                        }

                        if (dataSource instanceof QBDataSourceBase)
                        {
                        	((QBDataSourceBase )dataSource).addListener(this);
                        	progressLabel.setText(getResourceString("LabelsPane.LoadingReportData"));
                        }
                        else
                        {
                        	progressLabel.setText(getResourceString("JasperReportFilling"));
                        }
                        
                        //This needs to be done for newer jasperreports.jar. see bugzilla #7940 for details.
                        jasperReport.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
                        
                        if (recordSet != null || (recordSet == null && dataSource == null))
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
                        filling.set(true);
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LabelsPane.class, ex);
                setLabelText(getResourceString("JasperReportCreatingViewer"));
                log.error(ex);
                ex.printStackTrace();
            }
        } else
        {
            errThrowable = throwable;
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
        removeAll();
        setLabelText(getResourceString("JasperReportCancelled"));
        asyncFillHandler = null;
        closeSession();
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LabelsPane.class, ex);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.fill.AsynchronousFilllListener#reportFillError(java.lang.Throwable)
     */
    public void reportFillError(java.lang.Throwable t)
    {
        errThrowable = t;
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
                        errThrowable = ex;
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LabelsPane.class, ex);
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
        filling.set(false);
    	try
        {
            removeAll();
            label = null;

            JRViewer jasperViewer = new JRViewer(print);  
            
            //this vile hack for SPNHC demo may be useful for debugging...
//            JRViewer jasperViewer = new JRViewerPrintHackForSPNHCBDayQueryLinuxOnly(print);            
            //... end vile hack
            
            add(jasperViewer, BorderLayout.CENTER);

            forceTopFrameRepaint();

            closeSession();
            
        } catch (Exception ex)
        {
            errThrowable = ex;
            log.error(ex);
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LabelsPane.class, ex);
            setLabelText(getResourceString("JasperReportCreatingViewer"));
            ex.printStackTrace();
        }
        asyncFillHandler = null;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#currentRow(int)
	 */
	@Override
	public void currentRow(final long currentRow)
	{
		SwingUtilities.invokeLater(new Runnable(){

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run()
			{
				if (progressBar.isIndeterminate() && dataSource instanceof QBDataSource)
				{
					progressBar.setMinimum(0);
					progressBar.setMaximum(((QBDataSource )dataSource).size());
					progressBar.setIndeterminate(false);
				}
				progressBar.setValue((int )currentRow);
			}
			
		});
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#done(int)
	 */
	@Override
	public void done(long rows)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#filling()
	 */
	@Override
	public void filling()
	{
		SwingUtilities.invokeLater(new Runnable(){

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run()
			{
				progressBar.setValue(0);
				progressLabel.setText(getResourceString("JasperReportFilling"));
			}
			
		});
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#loaded()
	 */
	@Override
	public void loaded()
	{
		loading.set(false);
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#loading()
	 */
	@Override
	public void loading()
	{
		loading.set(true);
		SwingUtilities.invokeLater(new Runnable(){

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run()
			{
				progressLabel.setText(getResourceString("LabelsPane.LoadingReportData"));
			}
			
		});
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#rowCount(int)
	 */
	@Override
	public void rowCount(final long rowCount)
	{
		if (rowCount > 0)
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				@Override
				public void run()
				{
					progressBar.setMinimum(0);
					progressBar.setMaximum((int )rowCount);
					progressBar.setValue(0);
					progressBar.setIndeterminate(false);
				}

			});
		}
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#isListeningClosely()
     */
    @Override
    public boolean isListeningClosely()
    {
        return true;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#doTellAll()
	 */
	@Override
	public boolean doTellAll() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#deletedRecs(java.util.List)
	 */
	@Override
	public void deletedRecs(List<Integer> keysDeleted) {
		//nuthin
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#updatedRec(java.lang.Integer)
	 */
	@Override
	public void updatedRec(Integer key) {
		//nuthin
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace#addedRec(java.lang.Integer)
	 */
	@Override
	public void addedRec(Integer key) {
		//whatever
	}
    
    
}
