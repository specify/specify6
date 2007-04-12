package edu.ku.brc.specify.tasks.subpane.wb;

import java.awt.Frame;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.tasks.services.biogeomancer.BioGeomancerResult;
import edu.ku.brc.specify.tasks.services.biogeomancer.BioGeomancerResultsDisplay;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;

public class BioGeomancerResultsChooser extends CustomDialog
{
    protected BioGeomancerResultsDisplay resultsDisplayPanel = new BioGeomancerResultsDisplay();
    protected List<WorkbenchRow> rows;
    protected List<BioGeomancerResult> chosenResults;
    protected boolean hasBeenShown;
    protected int rowIndex;
    
    public BioGeomancerResultsChooser(Frame parent, String title, List<WorkbenchRow> rows)
    {
        super(parent,title,true,null);
        this.rows = rows;
        hasBeenShown = false;
        
        // create a vector for all of the user choices
        chosenResults = new Vector<BioGeomancerResult>(rows.size());
        // make sure it's the same size as the incoming list of rows
        for (int i = 0; i < rows.size(); ++i)
        {
            chosenResults.add(null);
        }
        
        setContentPanel(resultsDisplayPanel);
        
        rowIndex = 0;
    }
    
    public List<BioGeomancerResult> getResultsChosen()
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
            
            if (rowIndex < rows.size()-1)
            {
                setOkLabel("Next");
            }
            else
            {
                setOkLabel("OK");
            }

            try
            {
                resultsDisplayPanel.setBioGeomancerResultsData(rows.get(0).getBioGeomancerResults());
            }
            catch (Exception e)
            {
                UICacheManager.getStatusBar().setErrorMessage("Error while displaying BioGeomancer results", e);
                super.setVisible(false);
            }

            UIHelper.centerWindow(this);
        }
        
        super.setVisible(visible);
    }

    @Override
    protected void okButtonPressed()
    {
        // don't call super.okButtonPressed() since we don't want to go invisible
        isCancelled = false;
        btnPressed  = OK_BTN;
        
        BioGeomancerResult result = resultsDisplayPanel.getSelectedResult();
        chosenResults.set(rowIndex, result);
        
        // if that was the last one to work on...
        // close the dialog
        if (rowIndex >= rows.size()-1)
        {
            super.setVisible(false);
            return;
        }
        
        // since we have more rows to work on...
        
        rowIndex++;
        if (rowIndex < rows.size()-1)
        {
            setOkLabel("Next");
        }
        else
        {
            setOkLabel("OK");
        }

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
}
