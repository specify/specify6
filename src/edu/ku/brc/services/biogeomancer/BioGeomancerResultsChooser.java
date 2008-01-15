/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.services.biogeomancer;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * This class is for displaying thBGM Results. It was moved form the WorkBench and made generic.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jan 14, 2008
 *
 */
public class BioGeomancerResultsChooser extends CustomDialog
{
    protected BioGeomancerResultsDisplay resultsDisplayPanel = new BioGeomancerResultsDisplay();
    protected List<GeoCoordDataIFace> items;
    protected List<BioGeomancerResultStruct> chosenResults;
    protected boolean hasBeenShown;
    protected int     rowIndex;
    protected String  baseTitle;
    protected String  helpContext;
    
    /**
     * @param parent
     * @param title
     * @param items
     * @param helpContext
     */
    public BioGeomancerResultsChooser(final Frame parent, 
                                      final String title, 
                                      final List<GeoCoordDataIFace> items,
                                      final String helpContext)
    {
        super(parent,title,true,CustomDialog.OKCANCELAPPLYHELP,null);
        this.items        = items;
        this.hasBeenShown = false;
        this.baseTitle    = title;
        this.helpContext  = helpContext;
        
        // XXX Java 6
        // setIconImage(IconManager.getImage("AppIcon").getImage());
        
        // create a vector for all of the user choices
        chosenResults = new Vector<BioGeomancerResultStruct>(items.size());
        // make sure it's the same size as the incoming list of rows
        for (int i = 0; i < items.size(); ++i)
        {
            chosenResults.add(null);
        }
        
        setContentPanel(resultsDisplayPanel);
        
        this.cancelLabel = getResourceString("Skip");
        this.applyLabel  = getResourceString("Accept");
        this.okLabel     = getResourceString("Quit");
        
        rowIndex = -1;
    }
    
    /**
     * @return
     */
    public List<BioGeomancerResultStruct> getResultsChosen()
    {
        if (!hasBeenShown)
        {
            setVisible(true);
        }
        
        return chosenResults;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible)
    {
        if (hasBeenShown == false && visible)
        {
            hasBeenShown = true;
            createUI();

            HelpMgr.registerComponent(this.helpBtn, helpContext);

            showNextRecord();

            UIHelper.centerWindow(this);
            pack();
        }

        super.setVisible(visible);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#applyButtonPressed()
     */
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

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cancelButtonPressed()
     */
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

    /**
     * 
     */
    protected void showNextRecord()
    {
        rowIndex++;

        setTitle(baseTitle + ": " + (rowIndex+1) + " " + getResourceString("of") + " " + items.size());
        
        try
        {
            resultsDisplayPanel.setBioGeomancerResultsData(items.get(rowIndex).getXML());
            resultsDisplayPanel.setSelectedResult(0);
        }
        catch (Exception e)
        {
            UIRegistry.getStatusBar().setErrorMessage("Error while displaying BioGeomancer results", e); // i18n
            super.setVisible(false);
        }
    }
    
    /**
     * @return
     */
    protected boolean onLastRecord()
    {
        return (rowIndex == items.size()-1) ? true : false;
    }
}
