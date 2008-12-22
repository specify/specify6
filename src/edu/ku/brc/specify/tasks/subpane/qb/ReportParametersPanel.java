/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import it.businesslogic.ireport.JRParameter;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import net.sf.jasperreports.engine.JasperReport;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.tools.ireportspecify.MainFrameSpecify;
import edu.ku.brc.specify.tools.ireportspecify.ReportSpecify;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ReportParametersPanel extends JPanel
{
    protected static final Logger    log = Logger.getLogger(ReportParametersPanel.class);
    
    protected final SpReport report;
    protected final ReportSpecify reportSp;
    protected final JasperReport jasperReport;
    
    protected final boolean userPanel;
    
    protected int paramCount = 0;
    
    /**
     * @param report
     */
    public ReportParametersPanel(final SpReport report, final boolean userPanel)
    {
        super();
        this.report = report;
        this.reportSp = MainFrameSpecify.loadReport(report.getAppResource());
        this.jasperReport = null;
        this.userPanel = userPanel;
    }
    
    public ReportParametersPanel(final JasperReport jasperReport, final boolean userPanel)
    {
        super();
        this.report = null;
        this.reportSp = null;
        this.jasperReport = jasperReport;
        this.userPanel = userPanel;
    }
    
    
    /**
     * sets up for SpReport report object.
     */
    protected void createSpReportUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        for (Object paramObj : reportSp.getParameters())
        {
            JRParameter param = (JRParameter )paramObj;
            if (param.isBuiltin() != userPanel)
            {
                ReportParameterPanel rpp = new ReportParameterPanel(this, param.getName(), param.getDefaultValueExpression());
                rpp.createUI();
                add(rpp);
                paramCount++;
            }
        }
        validate();        
    }
    
    /**
     * sets up ui for JasperReport report object.
     */
    protected void createJasperReportUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        net.sf.jasperreports.engine.JRParameter[] params = jasperReport.getParameters();
        for (int p = 0; p < params.length; p++)
        {
            net.sf.jasperreports.engine.JRParameter param = params[p];
            if (param.isSystemDefined() != userPanel)
            {
                ReportParameterPanel rpp = new ReportParameterPanel(this, param.getName(), 
                    param.getDefaultValueExpression() != null ? param.getDefaultValueExpression().toString(): null);
                rpp.createUI();
                add(rpp);
                paramCount++;
            }
        }
        validate();
    }
    
    /**
     * Sets up UI for the parameters in the report.
     */
    public void createUI()
    {
        if (reportSp != null)
        {
            createSpReportUI();
        }
        else if (jasperReport != null)
        {
            createJasperReportUI();
        }
        else
        {
            log.error("Unable to load report parameters.");
        }
    }

    /**
     * @return the paramCount
     */
    public int getParamCount()
    {
        return paramCount;
    }
}
