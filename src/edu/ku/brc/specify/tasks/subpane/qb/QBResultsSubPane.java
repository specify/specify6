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

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.specify.tasks.subpane.ESResultsSubPane;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class QBResultsSubPane extends ESResultsSubPane
{
    public QBResultsSubPane(final String name,
                            final Taskable task,
                            final boolean includeExplainPane)
    {
        super(name, task, includeExplainPane);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsSubPane#createResultsTable(edu.ku.brc.af.ui.db.QueryForIdResultsIFace)
     */
    @Override
    protected ESResultsTablePanel createResultsTable(QueryForIdResultsIFace results)
    {
        // TODO Auto-generated method stub
        return new QBResultsTablePanel(this, results, results.shouldInstallServices(), results.isExpanded());
    }

    
}
