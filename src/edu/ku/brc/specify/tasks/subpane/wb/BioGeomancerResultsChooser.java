package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.services.biogeomancer.BioGeomancerResultStruct;
import edu.ku.brc.services.biogeomancer.BioGeomancerResultsDisplay;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;
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
        
        // XXX Java 6
        // setIconImage(IconManager.getImage("AppIcon").getImage());
        
        // create a vector for all of the user choices
        chosenResults = new Vector<BioGeomancerResultStruct>(rows.size());
        // make sure it's the same size as the incoming list of rows
        for (int i = 0; i < rows.size(); ++i)
        {
            chosenResults.add(null);
        }
        
        setContentPanel(resultsDisplayPanel);
        
        this.cancelLabel = getResourceString("Skip");
        this.applyLabel  = getResourceString("Accept");
        this.okLabel     = getResourceString("Done");
        
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

            HelpMgr.registerComponent(this.helpBtn, "WorkbenchSpecialTools");

            showNextRecord();

            UIHelper.centerWindow(this);
            pack();
        }

        super.setVisible(visible);
    }

    @Override
    protected void applyButtonPressed()
    {
        // remember, we're using the 'Apply' button for "accept" to progress
        // to the next record in the list and accept the currently selected result
        
        super.applyButtonPressed();
        
        // store the user selection into the chosen results list
        BioGeomancerResultStruct result = resultsDisplayPanel.getSelectedResult();
        chosenResults.set(rowIndex, result);
        
        // if this was the last record, close the window
        // otherwise, move on to the next record
        if (onLastRecord())
        {
            super.okButtonPressed();
        }
        else
        {
            showNextRecord();
        }
    }

    @Override
    protected void okButtonPressed()
    {
        // remember, we're using the 'OK' button for "done" to accept the
        // currently selected result and hide the dialog

        // store the user selection into the chosen results list
        BioGeomancerResultStruct result = resultsDisplayPanel.getSelectedResult();
        chosenResults.set(rowIndex, result);
        
        super.okButtonPressed();
    }
    
    @Override
    protected void cancelButtonPressed()
    {
        // remember, we're using the 'Cancel' button for "skip" to skip the
        // currently selected result and move onto the next one

        // if this was the last record, close the window
        // otherwise, move on to the next record
        if (onLastRecord())
        {
            super.okButtonPressed();
        }
        else
        {
            showNextRecord();
        }
    }

    protected void showNextRecord()
    {
        rowIndex++;

        setTitle(baseTitle + ": " + (rowIndex+1) + " " + getResourceString("of") + " " + rows.size());
        
        try
        {
            resultsDisplayPanel.setBioGeomancerResultsData(rows.get(rowIndex).getBioGeomancerResults());
            resultsDisplayPanel.setSelectedResult(0);
        }
        catch (Exception e)
        {
            UIRegistry.getStatusBar().setErrorMessage("Error while displaying BioGeomancer results", e);
            super.setVisible(false);
        }
    }
    
    protected boolean onLastRecord()
    {
        return (rowIndex == rows.size()-1) ? true : false;
    }
}
