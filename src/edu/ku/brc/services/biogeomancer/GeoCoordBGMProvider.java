/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.services.biogeomancer;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 14, 2008
 *
 */
public class GeoCoordBGMProvider implements GeoCoordServiceProviderIFace
{
    private static final Logger log = Logger.getLogger(GeoCoordBGMProvider.class);
    
    protected GeoCoordProviderListenerIFace listener    = null;
    protected String                        helpContext = null;
    /**
     * 
     */
    public GeoCoordBGMProvider()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordServiceProviderIFace#processGeoRefData(java.util.List, edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace, java.lang.String)
     */
    public void processGeoRefData(final List<GeoCoordDataIFace>      items, 
                                  final GeoCoordProviderListenerIFace listenerArg,
                                  final String helpContextArg)
    {
        this.listener    = listenerArg;
        this.helpContext = helpContextArg;
        
        UsageTracker.incrUsageCount("Tools.BioGeomancerData");
        
        log.info("Performing BioGeomancer lookup of selected records");
        
        // create a progress bar dialog to show the network progress
        final ProgressDialog progressDialog = new ProgressDialog("BioGeomancer Progress", false, true); // I18N
        progressDialog.getCloseBtn().setText(getResourceString("Cancel"));
        progressDialog.setModal(true);
        progressDialog.setProcess(0, items.size());
        
        // XXX Java 6
        //progressDialog.setIconImage( IconManager.getImage("AppIcon").getImage());

        // use a SwingWorker thread to do all of the work, and update the GUI when done
        final SwingWorker bgTask = new SwingWorker()
        {
            final JStatusBar statusBar = UIRegistry.getStatusBar();
            protected boolean cancelled = false;
            
            @Override
            public void interrupt()
            {
                super.interrupt();
                cancelled = true;
            }
                        
            @SuppressWarnings("synthetic-access")
            @Override
            public Object construct()
            {
                // perform the BG web service call ON all rows, storing results in the rows
                
                int progress = 0;

                for (GeoCoordDataIFace item : items)
                {
                    if (cancelled)
                    {
                        break;
                    }
                    
                    // get the locality data
                    String localityNameStr = item.getLocalityString();
                            
                    // get the geography data
                    String country = item.getCountry();
                    String state   = item.getState();
                    String county  = item.getCounty();
                    
                    log.info("Making call to BioGeomancer service: " + localityNameStr);
                    String bgResults;
                    BioGeomancerQuerySummaryStruct bgQuerySummary;
                    try
                    {
                        bgResults = BioGeomancer.getBioGeomancerResponse(item.getId().toString(), country, state, county, localityNameStr);
                        bgQuerySummary = BioGeomancer.parseBioGeomancerResponse(bgResults);
                    }
                    catch (IOException ex1)
                    {
                        String warning = getResourceString("WB_BIOGEOMANCER_UNAVAILABLE");
                        statusBar.setWarningMessage(warning, ex1);
                        log.error("A network error occurred while contacting the BioGeomancer service", ex1);
                        
                        // update the progress bar UI and move on
                        progressDialog.setProcess(++progress);
                        continue;
                    }
                    catch (Exception ex2)
                    {
                        // right now we'll simply blame this on BG
                        // in the future we might get more specific about this error
                        String warning = getResourceString("WB_BIOGEOMANCER_UNAVAILABLE");
                        statusBar.setWarningMessage(warning, ex2);
                        log.warn("Failed to get result count from BioGeomancer respsonse", ex2);
                        
                        // update the progress bar UI and move on
                        progressDialog.setProcess(++progress);
                        continue;
                    }

                    // if there was at least one result, pre-cache a map for that result
                    int resCount = bgQuerySummary.results.length;
                    if (resCount > 0)
                    {
                        final int rowNumber = item.getId();
                        final BioGeomancerQuerySummaryStruct summaryStruct = bgQuerySummary;
                        // create a thread to go grab the map so it will be cached for later use
                        Thread t = new Thread(new Runnable()
                        {
                            public void run()
                            {
                                try
                                {
                                    log.info("Requesting map of BioGeomancer results for workbench row " + rowNumber);
                                    BioGeomancer.getMapOfQuerySummary(summaryStruct, null);
                                }
                                catch (Exception e)
                                {
                                    log.warn("Failed to pre-cache BioGeomancer results map",e);
                                }
                            }
                        });
                        t.setName("Map Pre-Caching Thread: row " + item.getId()); // I18N
                        log.debug("Starting map pre-caching thread");
                        t.start();
                    }
                    
                    // if we got at least one result...
                    if (resCount > 0)
                    {
                        // everything must have worked and returned at least 1 result
                        // XXX TEMP FIX FOR BUG 4562 RELEASE 
                        // For now, all calls the setBioGeomancerResults() and getBioGeomancerResults() have been converted
                        // to calls to setTmpBgResults() and getTmpBgResults() to allow for continued use of the BG features
                        // without causing Hibernate to store the results to the DB, throwing the BUG 4562.
                        //row.setBioGeomancerResults(bgResults);
                        item.setXML(bgResults);
                        // ZZZ setChanged(true);
                    }
                    
                    // update the progress bar UI and move on
                    progressDialog.setProcess(++progress);
                }
                
                return null;
            }
        
            @Override
            public void finished()
            {
                if (!cancelled)
                {
                    // hide the progress dialog
                    progressDialog.setVisible(false);

                    // find out how many records actually had results
                    List<GeoCoordDataIFace> rowsWithResults = new Vector<GeoCoordDataIFace>();
                    for (GeoCoordDataIFace row : items)
                    {
                        if (row.getXML() != null)
                        {
                            rowsWithResults.add(row);
                        }
                    }

                    // if no records had possible results...
                    int numRecordsWithResults = rowsWithResults.size();
                    if (numRecordsWithResults == 0)
                    {
                        statusBar.setText(getResourceString("NO_BG_RESULTS"));
//                        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(),
//                                getResourceString("NO_BG_RESULTS"),
//                                getResourceString("NO_RESULTS"), JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    
                    if (listener != null)
                    {
                        listener.aboutToDisplayResults();
                    }
                    
                    // ask the user if they want to review the results
                    // TODO: i18n
                    // XXX: i18n
                    String message = "BioGeomancer returned results for " + numRecordsWithResults
                            + " records.  Would you like to view them now?"; // I18N
                    int userChoice = JOptionPane.showConfirmDialog((Frame)UIRegistry.getTopWindow(), message,
                            "Continue?", JOptionPane.YES_NO_OPTION); // I18N
                    
                    if (userChoice != JOptionPane.OK_OPTION)
                    {
                        statusBar.setText("BioGeomancer process terminated by user");
                        return;
                    }

                    displayBioGeomancerResults(rowsWithResults);
                }
            }
        };
        
        // if the user hits close, stop the worker thread
        progressDialog.getCloseBtn().addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent ae)
            {
                log.debug("Stopping the BioGeomancer service worker thread");
                bgTask.interrupt();
            }
        });
        
        log.debug("Starting the BioGeomancer service worker thread");
        bgTask.start();
        UIHelper.centerAndShow(progressDialog);
    }

    
    /**
     * Create a dialog to display the set of rows that had at least one result option
     * returned by BioGeomancer.  The dialog allows the user to iterate through the
     * records supplied, choosing a result (or not) for each one.
     * 
     * @param items the set of records containing valid BioGeomancer responses with at least one result
     */
    protected void displayBioGeomancerResults(final List<GeoCoordDataIFace> items)
    {
        // create the UI for displaying the BG results
        JFrame topFrame = (JFrame)UIRegistry.getTopWindow();
        BioGeomancerResultsChooser bgResChooser = new BioGeomancerResultsChooser(topFrame, 
                "BioGeomancer Results Chooser", 
                items, 
                helpContext); // I18N
        
        int itemsUpdated = 0;
        List<BioGeomancerResultStruct> results = bgResChooser.getResultsChosen();
        for (int i = 0; i < items.size(); ++i)
        {
            GeoCoordDataIFace          item       = items.get(i);
            BioGeomancerResultStruct userChoice = results.get(i);
            
            if (userChoice != null)
            {
                //System.out.println(userChoice.coordinates);
                
                String[] coords = StringUtils.split(userChoice.coordinates);

                item.set(coords[1], coords[0]);
                itemsUpdated++;
            }
        }
        
        if (listener != null)
        {
            listener.complete(items, itemsUpdated);
            listener = null;
        }
    }
}
