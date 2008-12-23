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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.jasperreports.engine.JasperReport;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ReportParametersPanel extends JPanel
{
    protected static final Logger    log = Logger.getLogger(ReportParametersPanel.class);
    
    protected final JasperReport jasperReport;
    
    protected final boolean userPanel;
    
    protected int paramCount = 0;
    
    protected JTextField[] paramTexts;
    
    
    public ReportParametersPanel(final JasperReport jasperReport, final boolean userPanel)
    {
        super();
        this.jasperReport = jasperReport;
        this.userPanel = userPanel;
    }
    
    protected String getRowLayoutAndCountParams()
    {
        String result = "2dlu";
        net.sf.jasperreports.engine.JRParameter[] params = jasperReport.getParameters();
        for (int p = 0; p < params.length; p++)
        {
            net.sf.jasperreports.engine.JRParameter param = params[p];
            if (param.isSystemDefined() != userPanel)
            {
                result += ", f:p, 2dlu";
                paramCount++;
            }
        }
        
        return result;
    }
    /**
     * Sets up UI for the parameters in the report.
     */
    public void createUI()
    {
        PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, r:p, 2dlu, f:p:g, 5dlu", getRowLayoutAndCountParams()), this);
        net.sf.jasperreports.engine.JRParameter[] params = jasperReport.getParameters();
        int currentParam = 0;
        CellConstraints cc = new CellConstraints();
        paramTexts = new JTextField[paramCount];
        for (int p = 0; p < params.length; p++)
        {
            net.sf.jasperreports.engine.JRParameter param = params[p];
            if (param.isSystemDefined() != userPanel)
            {
                paramTexts[currentParam] = new JTextField(param.getDefaultValueExpression() != null ? param.getDefaultValueExpression().toString(): null);
                pb.add(new JLabel(param.getName()), cc.xy(2, 2*currentParam + 2));
                pb.add(paramTexts[currentParam], cc.xy(4, 2*currentParam + 2));
                currentParam++;
            }
        }
        validate();
    }

    /**
     * @return the paramCount
     */
    public int getParamCount()
    {
        return paramCount;
    }
}
