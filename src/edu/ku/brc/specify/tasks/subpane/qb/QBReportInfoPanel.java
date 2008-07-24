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

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.ui.UIHelper;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Just lists a query's reports and displays a message/warning
 */
public class QBReportInfoPanel extends JPanel
{
    protected JLabel        msg;
    protected JList         reps;

    protected final SpQuery query;
    protected final String  msgTxt;

    /**
     * @param query
     * @param msgTxt
     */
    public QBReportInfoPanel(SpQuery query, String msgTxt)
    {
        super();
        this.query = query;
        this.msgTxt = msgTxt;
        createUI();
    }

    protected void createUI()
    {
        PanelBuilder builder = new PanelBuilder(new FormLayout("2dlu, fill:p:grow, 2dlu", "5dlu, p, 5dlu, p, 5dlu"), this);        
        CellConstraints cc = new CellConstraints();
        
        msg = UIHelper.createLabel(msgTxt);
        msg.setHorizontalAlignment(SwingConstants.CENTER);
        msg.setVerticalAlignment(SwingConstants.CENTER);
        builder.add(msg, cc.xy(2, 2));

        Vector<SpReport> sortedReps = new Vector<SpReport>();
        // assuming that query.forceLoad() has been called (and that query has reports).
        for (SpReport spRep : query.getReports())
        {
            sortedReps.add(spRep);
        }
        Collections.sort(sortedReps, new Comparator<SpReport>()
        {

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(SpReport o1, SpReport o2)
            {
                return o1.toString().compareTo(o2.toString());
            }

        });
        reps = UIHelper.createList(sortedReps);
        reps.setBorder(new BevelBorder(BevelBorder.LOWERED));
        builder.add(reps, cc.xy(2, 4));
    }
}
