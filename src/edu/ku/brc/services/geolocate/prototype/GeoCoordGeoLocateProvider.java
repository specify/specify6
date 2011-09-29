package edu.ku.brc.services.geolocate.prototype;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.rpc.ServiceException;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.services.biogeomancer.GeoCoordDataIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordServiceProviderIFace;
import edu.ku.brc.services.geolocate.prototype.ui.GeoLocateResultsChooser;
import edu.ku.brc.services.geolocate.prototype.client.GeolocatesvcLocator;
import edu.ku.brc.services.geolocate.prototype.client.GeolocatesvcSoap;
import edu.ku.brc.services.geolocate.prototype.client.Georef_Result;
import edu.ku.brc.services.geolocate.prototype.client.Georef_Result_Set;
import edu.ku.brc.specify.prefs.GEOLocatePrefsPanel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

public class GeoCoordGeoLocateProvider implements GeoCoordServiceProviderIFace, Runnable {
	protected GeoCoordProviderListenerIFace listener    = null;
    protected String                        helpContext = null;
    private boolean stopWork = false;
    private List<GeoCoordDataIFace> items;
    private List<Pair<GeoCoordDataIFace, Georef_Result_Set>> glResults = new Vector<Pair<GeoCoordDataIFace, Georef_Result_Set>>();
    private ProgressDialog progressDialog;
    
    /*******************************GeoLocate Options Strings*******************************/
    private static final String GL_HYWX    				= "GEOLocate.HYWX";
    private static final String GL_WTRBODY 				= "GEOLocate.WATERBODY";
    private static final String GL_RESTRICT				= "GEOLocate.RESTRICTTOLOWESTADM";
    private static final String GL_DOUNCERT				= "GEOLocate.DOUNCERT";
    private static final String GL_DOPOLY				= "GEOLocate.DOPOLY";
    private static final String GL_DISPLACEPOLY			= "GEOLocate.DISPLACEPOLY";
    //private static final String GL_POLYASLINKID			= "GEOLocate.POLYASLINKID";
    private static final String GL_LANGKEY				= "GEOLocate.LANGUAGEKEY";
    /***************************************************************************************/
    
    public GeoCoordGeoLocateProvider()
    {
        //This block is empty.
    }
    
	@Override
	public void processGeoRefData(List<GeoCoordDataIFace> items,
			GeoCoordProviderListenerIFace listener, String helpContext) {
		this.listener    = listener;
        this.helpContext = helpContext;
        
     // create a progress bar dialog to show the network progress
        progressDialog = new ProgressDialog(getResourceString("GeoCoordGeoLocateProvider.GEOLOC_PROGRESS"), false, true); //$NON-NLS-1$
        progressDialog.getCloseBtn().setText(getResourceString("GeoCoordGeoLocateProvider.CANCEL")); //$NON-NLS-1$
        progressDialog.setModal(true);
        progressDialog.setProcess(0, items.size());

        progressDialog.setIconImage( IconManager.getImage("AppIcon").getImage());

        // Create a thread for doing the GEOLocate web service requests.
        this.items = items;
        final Thread georeference = new Thread(this);
        georeference.start();
        
        // If the user hits close, stop the worker thread
        progressDialog.getCloseBtn().addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public void actionPerformed(ActionEvent ae)
            {
                georeference.interrupt();
                stopWork = true;
            }
        });

        // popup the progress dialog
        UIHelper.centerAndShow(progressDialog);
	}

	@Override
	public void run() {
		for (GeoCoordDataIFace grItem: items)
        {
		 	if (!stopWork)
		 	{

	            //GeoCoordDataIFace item = grItem;
	            // get the locality data
                String localityNameStr = grItem.getLocalityString();
                        
                // get the geography data
                String country = grItem.getCountry();
                String state   = grItem.getState();
                String county  = grItem.getCounty();
                
                country = country == null ? "" : country;
                state   = state   == null ? "" : state;
                county  = county  == null ? "" : county;
                
                boolean hwyX = AppPreferences.getLocalPrefs().getBoolean(GL_HYWX, false);
                boolean findWaterbody = AppPreferences.getLocalPrefs().getBoolean(GL_WTRBODY, false);
                boolean restrictToLowestAdm = AppPreferences.getLocalPrefs().getBoolean(GL_RESTRICT, false);
                boolean doUncert = AppPreferences.getLocalPrefs().getBoolean(GL_DOUNCERT, false);
                boolean doPoly = AppPreferences.getLocalPrefs().getBoolean(GL_DOPOLY, false);
                boolean displacePoly = AppPreferences.getLocalPrefs().getBoolean(GL_DISPLACEPOLY, false);
                boolean polyAsLinkID = false;
                
                GEOLocatePrefsPanel GLP = new GEOLocatePrefsPanel();
                FormViewObj fvo = (FormViewObj)GLP.getForm();
                ValComboBox languageKeyCoBX = fvo.getCompById(GL_LANGKEY);
                int languageKey = languageKeyCoBX.getComboBox().getSelectedIndex();
                
                //System.out.println(hwyX + " " + findWaterbody + " " + restrictToLowestAdm + " " + doUncert
                		 //+ " " + doPoly + " " + displacePoly + " " + polyAsLinkID + " " + languageKey);
               
                
                // make the web service request
                // Call Web Service Operation
                GeolocatesvcLocator service = new GeolocatesvcLocator();
                GeolocatesvcSoap port;
				try 
				{
					port = service.getgeolocatesvcSoap();
					try 
	                {
						final Georef_Result_Set glResultSet = port.georef2(country, state, county, localityNameStr, 
								hwyX, findWaterbody, restrictToLowestAdm, doUncert, doPoly, displacePoly, 
								polyAsLinkID, languageKey);
						glResults.add(new Pair<GeoCoordDataIFace, Georef_Result_Set>(grItem, glResultSet));
					} 
	                
	                catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						glResults.add(new Pair<GeoCoordDataIFace, Georef_Result_Set>(grItem, new Georef_Result_Set()));
					}
				} 
				
				catch (ServiceException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					glResults.add(new Pair<GeoCoordDataIFace, Georef_Result_Set>(grItem, new Georef_Result_Set()));
				}
                

                // update the progress bar
                SwingUtilities.invokeLater(new Runnable()
                {
                   public void run()
                   {
                       int progress = progressDialog.getProcess();
                       progressDialog.setProcess(++progress);
                   }
                });
		 	}
		 	
		 	else
		 		break;
        }
		
		if (!stopWork) //We finished without interruptions so let's proceed.
		{
			// do the UI work to show the results
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    progressDialog.setVisible(false);
                    
                    final JStatusBar statusBar = UIRegistry.getStatusBar();
                    if (statusBar != null)
                    {
                    	statusBar.setText(getResourceString("GeoCoordGeoLocateProvider.GEOLOC_COMPLETED")); //$NON-NLS-1$
                        
                        List<Pair<GeoCoordDataIFace, Georef_Result_Set>> withResults = new Vector<Pair<GeoCoordDataIFace, Georef_Result_Set>>();
                
                        for (Pair<GeoCoordDataIFace, Georef_Result_Set> result: glResults)
                        {
                            if (result.second.getNumResults() > 0)
                            {
                                withResults.add(result);
                            }
                        }
                        
                        if (withResults.size() == 0)
                        {
                            statusBar.setText(getResourceString("GeoCoordGeoLocateProvider.NO_GL_RESULTS")); //$NON-NLS-1$
                            JOptionPane.showMessageDialog(UIRegistry.getTopWindow(),
                                    getResourceString("GeoCoordGeoLocateProvider.NO_GL_RESULTS"), //$NON-NLS-1$
                                    getResourceString("NO_RESULTS"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
                
                            return;
                        }
                        
                        if (listener != null)
                        {
                            listener.aboutToDisplayResults();
                        }
                        
                        // ask the user if they want to review the results
                        String message = String.format(getResourceString("GeoCoordGeoLocateProvider.GEOLOCATE_RESULTS_VIEW_CONFIRM"), String.valueOf(withResults.size())); //$NON-NLS-1$
                        int userChoice = JOptionPane.showConfirmDialog(UIRegistry.getTopWindow(), message,
                                getResourceString("GeoCoordGeoLocateProvider.GEO_CONTINUE"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
                        
                        if (userChoice != JOptionPane.YES_OPTION)
                        {
                            statusBar.setText(getResourceString("GeoCoordGeoLocateProvider.USER_TERMINATED")); //$NON-NLS-1$
                            return;
                        }
                
                        // create the UI for displaying the BG results
                        JFrame topFrame = (JFrame)UIRegistry.getTopWindow();
                        GeoLocateResultsChooser bgResChooser = new GeoLocateResultsChooser(topFrame, withResults); //$NON-NLS-1$
                        
                        List<Georef_Result> results = bgResChooser.getResultsChosen();
                        
                        int itemsUpdated = 0;
                        
                        for (int i = 0; i < results.size(); ++i)
                        {
                            GeoCoordDataIFace item = withResults.get(i).first;
                            Georef_Result chosenResult = results.get(i);
                            
                            if (chosenResult != null)
                            {
                                Double latitude = chosenResult.getWGS84Coordinate().getLatitude();
                                Double longitude = chosenResult.getWGS84Coordinate().getLongitude();
                                item.set(String.format("%7.5f", latitude), String.format("%7.5f", longitude)); //$NON-NLS-1$ //$NON-NLS-2$
                                
                                itemsUpdated++;
                            }
                        }
                        
                        if (listener != null)
                        {
                            listener.complete(items, itemsUpdated);
                        }
                    }
                }
            });
		}
		
	}

}
