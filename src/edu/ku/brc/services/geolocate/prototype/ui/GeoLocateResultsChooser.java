package edu.ku.brc.services.geolocate.prototype.ui;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getStatusBar;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.services.biogeomancer.GeoCoordDataIFace;
import edu.ku.brc.services.geolocate.prototype.LocalityWaypoint;
import edu.ku.brc.services.geolocate.prototype.client.GeographicPoint;
import edu.ku.brc.services.geolocate.prototype.client.Georef_Result;
import edu.ku.brc.services.geolocate.prototype.client.Georef_Result_Set;
import edu.ku.brc.services.geolocate.prototype.ui.GeoLocateResultsDisplay;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

public class GeoLocateResultsChooser extends CustomDialog
{
    protected GeoLocateResultsDisplay resultsDisplayPanel = new GeoLocateResultsDisplay();
    protected List<Pair<GeoCoordDataIFace, Georef_Result_Set>> rowsAndResults;
    protected List<Georef_Result> chosenResults;
    protected boolean            hasBeenShown;
    protected int                rowIndex;
    
    /**
     * @param parent
     * @param title
     * @param rowsAndResults
     */
    public GeoLocateResultsChooser(final Frame parent, 
                                   final List<Pair<GeoCoordDataIFace, Georef_Result_Set>> rowsAndResults)
    {
        super(parent, "", true, CustomDialog.OKCANCELAPPLYHELP, null);
        
        this.rowsAndResults = rowsAndResults;
        this.hasBeenShown   = false;
        
        if (rowsAndResults.size() == 0)
        {
            throw new IllegalArgumentException("WorkbenchRow set must be non-empty"); //$NON-NLS-1$
        }
        
        // create a vector for all of the user choices
        chosenResults = new Vector<Georef_Result>(rowsAndResults.size());
        // make sure it's the same size as the incoming list of rows
        for (int i = 0; i < rowsAndResults.size(); ++i)
        {
            chosenResults.add(null);
        }
        
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
    	 JButton webGeorefBtn = new JButton("GEOLocate Web", IconManager.getIcon("GEOLocate16"));
         webGeorefBtn.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 	            try
 	            {
 	            	String url = "http://www.museum.tulane.edu/geolocate/web/WebGeoref.aspx?v=1";
 	            	
            		Georef_Result_Set res = rowsAndResults.get(rowIndex).second;
            		GeoCoordDataIFace loc = rowsAndResults.get(rowIndex).first;
            		
            		url += String.format("&country=%1$s&state=%2$s&county=%3$s&locality=%4$s",  URLEncoder.encode(loc.getCountry(), "UTF-8"),
            				URLEncoder.encode(loc.getState(), "UTF-8"),  URLEncoder.encode(loc.getCounty(), "UTF-8"), 
            				URLEncoder.encode( loc.getLocalityString(), "UTF-8"));
            		
            		String points = "&points=";
            		for (int j = 0; j < res.getNumResults(); j++)
            		{
            			String latStr = Double.toString(res.getResultSet()[j].getWGS84Coordinate().getLatitude());
 	            		String lonStr = Double.toString(res.getResultSet()[j].getWGS84Coordinate().getLongitude());
 	            		String patStr = res.getResultSet()[j].getParsePattern();
 	            		patStr = (patStr == null)? "" : patStr;
 	            		String precStr = res.getResultSet()[j].getPrecision();
 	            		precStr = (precStr == null)? "" : precStr + " (" + res.getResultSet()[j].getScore() + ")";
 	            		String uncertStr = res.getResultSet()[j].getUncertaintyRadiusMeters();
 	            		uncertStr = ((uncertStr == null) || uncertStr.equalsIgnoreCase("unavailable"))? "unavailable" :
 	            			uncertStr;
 	            		
 	            		points += URLEncoder.encode(String.format("%1$s|%2$s|%3$s|%4$s|%5$s", cleanString(latStr), cleanString(lonStr), cleanString(patStr), 
 	            						cleanString(precStr), cleanString(uncertStr)), "UTF-8");
 	            		if (j < (res.getNumResults() - 1))
 	            			points += ":";
            		}
            		
            		url += points;
 	        
 	                AttachmentUtils.openURI(new URL(url).toURI());
 	            }
 	            catch (Exception ex)
 	            {
 	                ex.printStackTrace();
 	            }
 			}
 		});
         
         setExtraBtn(webGeorefBtn);
         
        super.createUI();
        
        applyBtn.setEnabled(false);
        
        resultsDisplayPanel.setAcceptBtn(applyBtn);
    }
    
    private String cleanString(String str)
    {
        String newStr = str;
        String[] offenders = {"\0", "\b", "\f", "\n", "\r", "\t"};
        for (int i = 0; i < offenders.length; i++)
        {
            newStr = newStr.replace(offenders[i], " ");
        }
        return newStr.trim();
    }

    /**
     * @return
     */
    public List<Georef_Result> getResultsChosen()
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
        Georef_Result result = null;
        if (resultsDisplayPanel.useWorldWind)
        {
        	result = resultsDisplayPanel.getSelectedResult();
        }
        
        else
        {
        	LocalityWaypoint accResult = resultsDisplayPanel.geoMapper.getMostAccurateResultPt();
        	result = new Georef_Result();
        	result.setParsePattern(getResourceString("GeoLocateResultsDisplay.USRDEF"));
        	result.setPrecision(accResult.getLocality().getPrecision());
        	result.setScore(accResult.getLocality().getScore());
        	result.setUncertaintyPolygon(accResult.getLocality().getErrorPolygon());
        	result.setUncertaintyRadiusMeters(accResult.getLocality().getUncertaintyMeters());
        	result.setWGS84Coordinate(new GeographicPoint(accResult.getLocality().getLatitude(), 
        			accResult.getLocality().getLongitude()));
            result.setErrorPolygon(accResult.getLocality().getErrorPolygon());
            
            BigDecimal errEst       = null;
            String     uncertMeters = accResult.getLocality().getUncertaintyMeters();
            if (StringUtils.isNotEmpty(uncertMeters))
            {
                try
                {
                    double value = Double.parseDouble(uncertMeters);
                    errEst = new BigDecimal(value);
                } catch (NumberFormatException ex) {}
            }
            result.setErrorEstimate(errEst);
        }
        
        chosenResults.set(rowIndex, result);
        
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

    @Override
    protected void okButtonPressed()
    {
        resultsDisplayPanel.shutdown();
        
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
        Georef_Result_Set resSet = rowsAndResults.get(rowIndex).second;
        if (resSet.getNumResults() == 0)
        {
            showNextRecord();
        }

        setTitle(getLocalizedMessage("GeoLocateResultsChooser.TITLE", (rowIndex+1), rowsAndResults.size())); //$NON-NLS-1$
        
        try
        {
            GeoCoordDataIFace item = rowsAndResults.get(rowIndex).first;
            
            resultsDisplayPanel.setGeoLocateQueryAndResults(item.getLocalityString(), 
                                                            item.getCounty(), 
                                                            item.getState(), 
                                                            item.getCountry(), 
                                                            resSet);
            resultsDisplayPanel.setSelectedResult(0);
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GeoLocateResultsChooser.class, e);
            getStatusBar().setErrorMessage(getResourceString("GeoLocateResultsChooser.ERROR_DISPLAY_GL_RESULTS"), e);//$NON-NLS-1$
            super.setVisible(false);
        }
    }
    
    /**
     * @return
     */
    protected boolean onLastRecord()
    {
        return (rowIndex == rowsAndResults.size()-1) ? true : false;
    }
}