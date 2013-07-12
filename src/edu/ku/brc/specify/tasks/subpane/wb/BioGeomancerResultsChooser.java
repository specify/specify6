/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
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
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

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
        
        this.cancelLabel = getResourceString("SKIP");
        this.applyLabel  = getResourceString("ACCEPT");
        this.okLabel     = getResourceString("QUIT");
        
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

        setTitle(baseTitle + ": " + (rowIndex+1) + " " + getResourceString("of") + " " + rows.size());
        
        try
        {
            resultsDisplayPanel.setBioGeomancerResultsData(rows.get(rowIndex).getBioGeomancerResults());
            resultsDisplayPanel.setSelectedResult(0);
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BioGeomancerResultsChooser.class, e);
            UIRegistry.getStatusBar().setErrorMessage("Error while displaying BioGeomancer results", e); // i18n
            super.setVisible(false);
        }
    }
    
    protected boolean onLastRecord()
    {
        return (rowIndex == rows.size()-1) ? true : false;
    }
}
