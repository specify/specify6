/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.plugins.sgr;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 27, 2010
 *
 */
public class SGRResultsChooser extends CustomDialog
{
    protected SGRResultsDisplay        resultsDisplayPanel;
    protected List<DataResultsRow>     rowsAndResults;
    protected Vector<RawData>          chosenResults;
    protected boolean                  hasBeenShown;
    protected int                      rowIndex;
    
    /**
     * @param parent
     * @param rowsAndResults
     */
    public SGRResultsChooser(final Frame parent, 
                             final List<DataResultsRow> rowsAndResults)
    {
        super(parent, "", true, CustomDialog.OKCANCELAPPLYHELP, null);
        
        this.rowsAndResults = rowsAndResults;
        this.hasBeenShown   = false;
        
        if (rowsAndResults.size() == 0)
        {
            throw new IllegalArgumentException("WorkbenchRow set must be non-empty"); //$NON-NLS-1$
        }
        
        // create a vector for all of the user choices
        chosenResults = new Vector<RawData>(rowsAndResults.size());
        // make sure it's the same size as the incoming list of rows
        for (int i = 0; i < rowsAndResults.size(); ++i)
        {
            chosenResults.add(null);
        }
        
        resultsDisplayPanel = new SGRResultsDisplay(GroupHashDAO.getInstance().getConnection());
        resultsDisplayPanel.createUI();
        
        setContentPanel(resultsDisplayPanel);
        
        this.cancelLabel = getResourceString("GeoLocateResultsChooser.SKIP"); //$NON-NLS-1$
        this.applyLabel  = getResourceString("GeoLocateResultsChooser.ACCEPT"); //$NON-NLS-1$
        this.okLabel     = getResourceString("GeoLocateResultsChooser.QUIT"); //$NON-NLS-1$
        
        rowIndex = -1;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        applyBtn.setEnabled(false);
        
        resultsDisplayPanel.setAcceptBtn(applyBtn);
    }

    /**
     * @return
     */
    public List<RawData> getResultsChosen()
    {
        if (!hasBeenShown)
        {
            pack();
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

            HelpMgr.registerComponent(this.helpBtn, "WorkbenchSpecialTools"); //$NON-NLS-1$

            showNextRecord();

            pack();
            setSize(1024, 800);
            UIHelper.centerWindow(this);
            
            resultsDisplayPanel.beforeDisplay();
        }

        super.setVisible(visible);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#applyButtonPressed()
     */
    @Override
    protected void applyButtonPressed()
    {
        // Remember, we're using the 'Apply' button for "Accept" to move
        // to the next record in the list and accept the currently selected result
        
        super.applyButtonPressed();
        
        if (resultsDisplayPanel.hasData())
        {
            RawData rawData = (RawData)resultsDisplayPanel.getDataRow();
            if (rawData != null)
            {
                chosenResults.remove(rowIndex);
                chosenResults.insertElementAt(rawData, rowIndex);
            }
        }
        
        // if this was the last record, close the window
        // otherwise, move on to the next record
        if (onLastRecord())
        {
            resultsDisplayPanel.shutdown();
            
            super.okButtonPressed();
        } else
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
        resultsDisplayPanel.shutdown();
        
        // remember, we're using the 'OK' button for "Dismiss" to accept the
        // currently selected result and hide the dialog

        // right now we're NOT storing the user selection when "Dismiss" is pressed
        // to enable storing of the user selection, just uncomment the following lines...

        
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
            resultsDisplayPanel.shutdown();
            
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
        
        // skip any records with no results
        DataResultsRow dataResRow = rowsAndResults.get(rowIndex);
        if (dataResRow.getRawData() == null)
        {
            System.out.println("");
        }

        setTitle(String.format("Results %d / %d", (rowIndex+1), rowsAndResults.size()));
        //setTitle(getLocalizedMessage("SGRResultsChooser.TITLE", (rowIndex+1), rowsAndResults.size())); //$NON-NLS-1$
        
        resultsDisplayPanel.setGroupData(dataResRow);
    }
    
    /**
     * @return
     */
    protected boolean onLastRecord()
    {
        return (rowIndex == rowsAndResults.size()-1) ? true : false;
    }
}
