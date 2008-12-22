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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ReportParameterPanel extends JPanel
{
    protected final ReportParametersPanel container;
    protected final String paramName;
    protected String paramDefault;
    protected JLabel pNameLbl;
    protected JTextField pTxt;
    
    /**
     * @param container
     * @param paramName
     * @param paramValue
     */
    public ReportParameterPanel(final ReportParametersPanel container, final String paramName, final String paramValue)
    {
        super();
        this.container = container;
        this.paramName = paramName;
        this.paramDefault = paramValue;
    }
    
    /**
     * Creates and lays out UI components
     */
    public void createUI()
    {
        PanelBuilder pb = new PanelBuilder(new FormLayout("2dlu, r:p:g, 2dlu, f:p:g, 2dlu", "f:p"), this);
        pNameLbl = new JLabel(paramName);
        CellConstraints cc = new CellConstraints();
        pb.add(pNameLbl, cc.xy(2, 1));
        pTxt = new JTextField(paramDefault != null ? paramDefault.toString() : null);
        pb.add(pTxt, cc.xy(4,1));
        
        validate();
    }
   
    /**
     * @return the value of the parameter.
     */
    public String getParamValue()
    {
        return pTxt.getText();
    }
}
