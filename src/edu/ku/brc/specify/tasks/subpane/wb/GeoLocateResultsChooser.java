/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.services.geolocate.client.GeorefResult;
import edu.ku.brc.services.geolocate.client.GeorefResultSet;
import edu.ku.brc.services.geolocate.ui.GeoLocateResultsDisplay;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

public class GeoLocateResultsChooser extends CustomDialog
{
    protected GeoLocateResultsDisplay resultsDisplayPanel = new GeoLocateResultsDisplay();
    protected List<Pair<WorkbenchRow,GeorefResultSet>> rowsAndResults;
    protected List<GeorefResult> chosenResults;
    protected boolean hasBeenShown;
    protected int rowIndex;
    protected String baseTitle;
    private int localityNameColIndex;
    private int countryColIndex;
    private int stateColIndex;
    private int countyColIndex;
    
    public GeoLocateResultsChooser(Frame parent, String title, List<Pair<WorkbenchRow,GeorefResultSet>> rowsAndResults)
    {
        super(parent,title,true,CustomDialog.OKCANCELAPPLYHELP,null);
        this.rowsAndResults = rowsAndResults;
        this.hasBeenShown = false;
        this.baseTitle = title;
        
        if (rowsAndResults.size() == 0)
        {
            throw new IllegalArgumentException("WorkbenchRow set must be non-empty");
        }
        
        // get the proper column indices
        WorkbenchRow r = rowsAndResults.get(0).first;
        localityNameColIndex = r.getLocalityStringIndex();
        countryColIndex      = r.getCountryIndex();
        stateColIndex        = r.getStateIndex();
        countyColIndex       = r.getCountyIndex();
        
        // create a vector for all of the user choices
        chosenResults = new Vector<GeorefResult>(rowsAndResults.size());
        // make sure it's the same size as the incoming list of rows
        for (int i = 0; i < rowsAndResults.size(); ++i)
        {
            chosenResults.add(null);
        }
        
        setContentPanel(resultsDisplayPanel);
        
        this.cancelLabel = getResourceString("Skip");
        this.applyLabel  = getResourceString("Accept");
        this.okLabel     = getResourceString("Quit");
        
        rowIndex = -1;
    }
    
    public List<GeorefResult> getResultsChosen()
    {
        if (!hasBeenShown)
        {
            pack();
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
        GeorefResult result = resultsDisplayPanel.getSelectedResult();
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
        // remember, we're using the 'OK' button for "Dismiss" to accept the
        // currently selected result and hide the dialog

        // right now we're NOT storing the user selection when "Dismiss" is pressed
        // to enable storing of the user selection, just uncomment the following lines...
        //----------------------------------
        // store the user selection into the chosen results list
        // BioGeomancerResultStruct result = resultsDisplayPanel.getSelectedResult();
        // chosenResults.set(rowIndex, result);
        //----------------------------------
        
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
        
        // skip any records with no results
        GeorefResultSet resSet = rowsAndResults.get(rowIndex).second;
        if (resSet.getNumResults() == 0)
        {
            showNextRecord();
        }

        setTitle(baseTitle + ": " + (rowIndex+1) + " " + getResourceString("of") + " " + rowsAndResults.size());
        
        try
        {
            WorkbenchRow row = rowsAndResults.get(rowIndex).first;
            
            String localityString = row.getData(localityNameColIndex);
            String countyString   = row.getData(countyColIndex);
            String stateString    = row.getData(stateColIndex);
            String countryString  = row.getData(countryColIndex);
            
            resultsDisplayPanel.setGeoLocateQueryAndResults(localityString, countyString, stateString, countryString, resSet);
            resultsDisplayPanel.setSelectedResult(0);
        }
        catch (Exception e)
        {
            UIRegistry.getStatusBar().setErrorMessage("Error while displaying GEOLocate results", e); // TODO i18n
            super.setVisible(false);
        }
    }
    
    protected boolean onLastRecord()
    {
        return (rowIndex == rowsAndResults.size()-1) ? true : false;
    }
}
