package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Frame;
import java.util.List;
import java.util.Vector;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.services.biogeomancer.BioGeomancerResultStruct;
import edu.ku.brc.services.biogeomancer.BioGeomancerResultsDisplay;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;

public class BioGeomancerResultsChooser extends CustomDialog
{
    protected BioGeomancerResultsDisplay resultsDisplayPanel = new BioGeomancerResultsDisplay();
    protected List<WorkbenchRow> rows;
    protected List<BioGeomancerResultStruct> chosenResults;
    protected boolean hasBeenShown;
    protected int rowIndex;
    protected String baseTitle;
    
    public BioGeomancerResultsChooser(Frame parent, String title, List<WorkbenchRow> rows)
    {
        super(parent,title,true,CustomDialog.OKCANCELAPPLYHELP,null);
        this.rows = rows;
        this.hasBeenShown = false;
        this.baseTitle = title;
        
        // create a vector for all of the user choices
        chosenResults = new Vector<BioGeomancerResultStruct>(rows.size());
        // make sure it's the same size as the incoming list of rows
        for (int i = 0; i < rows.size(); ++i)
        {
            chosenResults.add(null);
        }
        
        setContentPanel(resultsDisplayPanel);
        
        this.applyLabel = getResourceString("Skip");
        
        resultsDisplayPanel.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                // ignore the event that fires during a user change
                // just catch the one that fires when the user is done changing
                if (e.getValueIsAdjusting())
                {
                    return;
                }
                
                if (onLastRecord())
                {
                    // no need to mess with the button now, since it should be disabled
                    return;
                }
                
                if (resultsDisplayPanel.getSelectedResult() == null)
                {
                    // if the user hasn't selected a result record, put "Skip" on the button
                    setApplyLabel(getResourceString("Skip"));
                }
                else
                {
                    // if the user selected a result record, put "Next" on the button
                    setApplyLabel(getResourceString("Next"));
                }
            }
        });
        
        rowIndex = -1;
    }
    
    public List<BioGeomancerResultStruct> getResultsChosen()
    {
        if (!hasBeenShown)
        {
            setVisible(true);
        }
        
        return chosenResults;
    }
    
    @Override
    public void setVisible(boolean visible)
    {
        if (hasBeenShown == false && visible)
        {
            hasBeenShown = true;
            createUI();

            showNextRecord();

            UIHelper.centerWindow(this);
        }
        
        super.setVisible(visible);
    }

    @Override
    protected void applyButtonPressed()
    {
        // remember, we're using the 'apply' button for "next" or "skip" to progress
        // to the next record in the list
        
        super.applyButtonPressed();
        
        // store the user selection into the chosen results list
        BioGeomancerResultStruct result = resultsDisplayPanel.getSelectedResult();
        chosenResults.set(rowIndex, result);
        
        showNextRecord();
    }

    @Override
    protected void helpButtonPressed()
    {
        // TODO Auto-generated method stub
        super.helpButtonPressed();
        
        // show the help window
    }

    @Override
    protected void okButtonPressed()
    {
        // store the user selection into the chosen results list
        BioGeomancerResultStruct result = resultsDisplayPanel.getSelectedResult();
        chosenResults.set(rowIndex, result);
        
        super.okButtonPressed();
    }
    
    protected void showNextRecord()
    {
        rowIndex++;

        if (onLastRecord())
        {
            applyBtn.setEnabled(false);
        }

        setTitle(baseTitle + ": " + (rowIndex+1) + " " + getResourceString("of") + " " + rows.size());
        
        try
        {
            resultsDisplayPanel.setBioGeomancerResultsData(rows.get(rowIndex).getBioGeomancerResults());
        }
        catch (Exception e)
        {
            UICacheManager.getStatusBar().setErrorMessage("Error while displaying BioGeomancer results", e);
            super.setVisible(false);
        }
    }
    
    protected boolean onLastRecord()
    {
        return (rowIndex == rows.size()-1) ? true : false;
    }
}
