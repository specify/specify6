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

import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.JRParameterDefaultValuesEvaluator;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.util.Pair;

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
    
    protected String[] paramNames;
    
    
    /**
     * @param jasperReport
     * @param userPanel 
     * 
     * Creates a parameter panel for the supplied JasperReport. If userPanel is true,
     * then only user-defined parameters are displayed. Otherwise only system parameters 
     * are displayed. 
     * Only parameters for which isForPrompting() is true are displayed.
     * 
     */
    public ReportParametersPanel(final JasperReport jasperReport, final boolean userPanel)
    {
        super();
        this.jasperReport = jasperReport;
        this.userPanel = userPanel;
        createUI();
    }
    
    /**
     * @param param
     * @return true if the param should be displayed.
     */
    protected boolean shouldDisplay(final net.sf.jasperreports.engine.JRParameter param)
    {
        return param.isSystemDefined() != userPanel && param.isForPrompting();
    }
    
    /**
     * @return a JGoodies row layout string.
     * 
     *  Also counts the number of displayed parameters.
     */
    protected String getRowLayoutAndCountParams()
    {
        String result = "10dlu";
        net.sf.jasperreports.engine.JRParameter[] params = jasperReport.getParameters();
        for (int p = 0; p < params.length; p++)
        {
            net.sf.jasperreports.engine.JRParameter param = params[p];
            if (shouldDisplay(param))
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
    @SuppressWarnings("unchecked")
    public void createUI()
    {
        try
        {
            Map<Object, Object> defaultVals = JRParameterDefaultValuesEvaluator.evaluateParameterDefaultValues(jasperReport, null);
            PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, r:p, 2dlu, f:85dlu:g, 5dlu", getRowLayoutAndCountParams()), this);
            net.sf.jasperreports.engine.JRParameter[] params = jasperReport.getParameters();
            int currentParam = 0;
            CellConstraints cc = new CellConstraints();
            paramTexts = new JTextField[paramCount];
            paramNames = new String[paramCount];
            for (int p = 0; p < params.length; p++)
            {
                net.sf.jasperreports.engine.JRParameter param = params[p];
                if (shouldDisplay(param))
                {
                    Object paramValObj = defaultVals.get(param.getName());
                    String paramVal = paramValObj != null ? paramValObj.toString() : null;
                    paramTexts[currentParam] = new JTextField(paramVal);
                    paramNames[currentParam] = param.getName();
                    pb.add(new JLabel(param.getName()), cc.xy(2, 2*currentParam + 2));
                    pb.add(paramTexts[currentParam], cc.xy(4, 2*currentParam + 2));
                    currentParam++;
                }
            }
            validate();
        }
        catch (JRException jrex)
        {
            throw new RuntimeException(jrex);
        }
        
    }

    /**
     * @return the paramCount
     */
    public int getParamCount()
    {
        return paramCount;
    }
    
    /**
     * @param paramIdx
     * @return the parameter at position paramIdx.
     */
    public Pair<String, String> getParam(int paramIdx)
    {
        return new Pair<String, String>(paramNames[paramIdx], paramTexts[paramIdx].getText());
    }
}
