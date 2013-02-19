package edu.ku.brc.services.geolocate.prototype.ui;

import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPElement;

import org.apache.axis.message.MessageElement;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.mapviewer.GeoPosition;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.services.geolocate.prototype.ErrorPolygonDrawCancelListener;
import edu.ku.brc.services.geolocate.prototype.ErrorPolygonDrawEvent;
import edu.ku.brc.services.geolocate.prototype.ErrorPolygonDrawListener;
import edu.ku.brc.services.geolocate.prototype.Locality;
import edu.ku.brc.services.geolocate.prototype.LocalityWaypoint;
import edu.ku.brc.services.geolocate.prototype.MapPointerMoveEvent;
import edu.ku.brc.services.geolocate.prototype.MapPointerMoveListener;
import edu.ku.brc.services.geolocate.prototype.Mapper;
import edu.ku.brc.services.geolocate.prototype.MeasureDistanceCancelListener;
import edu.ku.brc.services.geolocate.prototype.MeasureDistanceEvent;
import edu.ku.brc.services.geolocate.prototype.MeasureDistanceListener;
import edu.ku.brc.services.geolocate.prototype.MostAccuratePointReleaseListener;
import edu.ku.brc.services.geolocate.prototype.MostAccuratePointSnapListener;
import edu.ku.brc.services.geolocate.prototype.UncertaintyCircleChangeEvent;
import edu.ku.brc.services.geolocate.prototype.UncertaintyCircleChangeListener;
import edu.ku.brc.services.geolocate.prototype.UncertaintyCircleResizeCancelListener;
import edu.ku.brc.services.geolocate.prototype.UncertaintyCircleResizeEvent;
import edu.ku.brc.services.geolocate.prototype.UncertaintyCircleResizeListener;
import edu.ku.brc.services.geolocate.prototype.client.GeographicPoint;
import edu.ku.brc.services.geolocate.prototype.client.Georef_Result;
import edu.ku.brc.services.geolocate.prototype.client.Georef_Result_Set;
import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.services.mapping.LatLonPoint;
import edu.ku.brc.services.mapping.LocalityMapper.MapperListener;
import edu.ku.brc.services.usgs.elevation.Elevation_ServiceLocator;
import edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoap;
import edu.ku.brc.services.usgs.elevation.GetElevationResponseGetElevationResult;
import edu.ku.brc.specify.ui.ClickAndGoSelectListener;
import edu.ku.brc.specify.ui.WorldWindPanel;
import edu.ku.brc.ui.BiColorTableCellRenderer;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.markers.BasicMarker;
//ZZZ import gov.nasa.worldwind.view.OrbitView;

/**
 * A UI panel for use in displaying the results of a GEOLocate web service query.
 * 
 * @author jstewart
 * @author rods
 * 
 * @code_status Alpha
 */
/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 12, 2009
 *
 */
public class GeoLocateResultsDisplay extends JPanel implements MapperListener, SelectListener
{
    private enum ErrBtnStateType    {eDraw, eApply, eClear};
    private enum UnCertBtnStateType {eEdit, eApply};
    
    private static final String L10N = "GeoLocateResultsDisplay.";
    
    protected static final int MAP_WIDTH  = 500;
    protected static final int MAP_HEIGHT = 500;
    protected static final int MAP_WIDTH2  = 600;
    protected static final int MAP_HEIGHT2 = 450;

    protected ResultsTableModel tableModel;
    protected JTable            resultsTable;
    
    protected JLabel            mapLabel;
    
    protected JTextField        localityStringField;
    protected JTextField        countyField;
    protected JTextField        stateField;
    protected JTextField        countryField;
    protected JTextField		polyField;   
    
    protected ErrBtnStateType    errBtnState = ErrBtnStateType.eApply;
    protected UnCertBtnStateType ucBtnState  = UnCertBtnStateType.eApply;
    
    protected JButton           acceptBtn     = null;
    
    protected WorldWindPanel    wwPanel       = null;
    protected Mapper 			geoMapper	  = null;
    protected Georef_Result      userDefGeoRef = null;
    protected Position          lastClickPos  = null;
    protected boolean useWorldWind = false;
    
    protected JTextField 		latText;
    protected JTextField 		lonText;
    protected JButton 			coordBtn;
    protected JTextField 		uncertTxt;
    protected JButton 			uncertBtn;
    protected JTextArea 		errorPTxt;
    protected JButton 			errorPBtn;
    
    protected JLabel statusLatLbl;
	protected JLabel statusLonLbl;
	protected JLabel statusURLbl;
	protected JLabel statusErrorLbl;
	
	protected JButton getElevBtn;
	protected JCheckBox statusMeasureTool;
	protected JButton statusClearRulerBtn;
	protected JPanel statusElevPanel;
	protected JPanel statusClearRulerPanel;
	protected JPanel statusPanel;
	protected JLabel statusElevLbl;
	protected JLabel statusElevInfoLbl;
	protected boolean isElevBtnDown = false;
    
    /**
     * Constructor.
     */
    public GeoLocateResultsDisplay()
    {
        super();
        
        PanelBuilder mainPB;
        useWorldWind = !AppPreferences.getLocalPrefs().getBoolean("GEOLocate.USEGL_MAPS", true);
        if (useWorldWind)
            mainPB = new PanelBuilder(new FormLayout("p,10px,500px,10px,f:p:g", "p,2px,p,2px,p,2px,p,2px,p,10px,p,2px,f:p:g"), this); //$NON-NLS-1$ //$NON-NLS-2$
        else
            mainPB = new PanelBuilder(new FormLayout("p,10px,500px,10px,f:p:g", "p,2px,p,2px,p,2px,p,2px,p,5px,p,10px,p,2px,f:p:g,10px,f:p:g"), this);
        
        CellConstraints cc = new CellConstraints();
        
        // add the query fields to the display
        int rowIndex = 1;
        mainPB.addSeparator(UIRegistry.getResourceString(L10N+"LOC_INFO"));
        rowIndex += 2;
        localityStringField = addRow(cc, getResourceString(L10N + "LOCALITY_DESC"),      1, rowIndex); //$NON-NLS-1$
        rowIndex += 2;
        countyField         = addRow(cc, getResourceString(L10N + "COUNTY"), 1, rowIndex); //$NON-NLS-1$
        rowIndex += 2;
        stateField          = addRow(cc, getResourceString(L10N + "STATE"),    1, rowIndex); //$NON-NLS-1$
        rowIndex += 2;
        countryField        = addRow(cc, getResourceString(L10N + "COUNTRY"),    1, rowIndex); //$NON-NLS-1$
        rowIndex += 2;

        // add the JLabel to show the map
        mapLabel = createLabel(getResourceString(L10N + "LOADING_MAP")); //$NON-NLS-1$
        mapLabel.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        
        if (!useWorldWind)
        {
            FocusListener focLis = new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) { }
                
                @Override
                public void focusGained(FocusEvent e) {
                    statusErrorLbl.setText("");
                }
            };
            
        	// Add the correction marker section.
            PanelBuilder corMarkerPB = new PanelBuilder(new FormLayout("f:p:g,10px,f:p:g,10px,f:p:g", "f:p:g"));

            JLabel lbl = UIHelper.createLabel("");
            
            corMarkerPB.getPanel().setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), UIRegistry.getResourceString(L10N+"GREEN_PROPS"),
                                                     TitledBorder.LEFT, TitledBorder.TOP, lbl.getFont(), lbl.getForeground()));
            mainPB.add(corMarkerPB.getPanel(), cc.xyw(1, rowIndex, 3));
            rowIndex += 2;

            latText = UIHelper.createTextField();
            latText.addFocusListener(focLis);
            
            lonText = UIHelper.createTextField();
            lonText.addFocusListener(focLis);

            coordBtn = UIHelper.createI18NButton("Apply");
            coordBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					applyManualCoordinates();
				}
			});
            
            PanelBuilder coordsPB = new PanelBuilder(new FormLayout("p,2px,f:p:g,p", "p,2px,p,2px,p,f:p:g,p"));
            coordsPB.addSeparator(UIRegistry.getResourceString(L10N+"POS"), cc.xyw(1, 1, 4));
            coordsPB.add(UIHelper.createI18NFormLabel("Latitude", SwingConstants.RIGHT), cc.xy(1, 3));
            coordsPB.add(latText, cc.xyw(3, 3, 2));
            coordsPB.add(UIHelper.createI18NFormLabel("Longitude", SwingConstants.RIGHT), cc.xy(1, 5));
            coordsPB.add(lonText, cc.xyw(3, 5, 2));
            
            coordsPB.add(coordBtn, cc.xy(4, 7));
            
            corMarkerPB.add(coordsPB.getPanel(), cc.xy(1,1));
            
            uncertTxt = UIHelper.createTextField();
            uncertTxt.addFocusListener(focLis);
            uncertBtn = UIHelper.createI18NButton("Apply");
            
            PanelBuilder uncertaintyPB = new PanelBuilder(new FormLayout("p,2px,p:g,p", "p,2px,p,4px,f:p:g,p"));
            uncertaintyPB.addSeparator(UIRegistry.getResourceString(L10N+"UNCRT_RADIUS"), cc.xyw(1, 1, 4));
            uncertaintyPB.add(UIHelper.createI18NFormLabel("In meters"), cc.xy(1, 3));
            uncertaintyPB.add(uncertTxt, cc.xyw(3, 3, 2));
            uncertaintyPB.add(uncertBtn, cc.xy(4, 6));
            
            corMarkerPB.add(uncertaintyPB.getPanel(), cc.xy(3, 1));

            errorPTxt = UIHelper.createTextArea();
            errorPTxt.setLineWrap(true);
            errorPTxt.setWrapStyleWord(true);
            errorPTxt.addFocusListener(focLis);
            
            JScrollPane errorPScrollPane = new JScrollPane(errorPTxt, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            errorPScrollPane.setPreferredSize(new Dimension(70, 50));
            errorPBtn = UIHelper.createI18NButton("Apply");
            
            PanelBuilder errorPolygonPB = new PanelBuilder(new FormLayout("f:p:g,2px,r:p", "p,2px,p,4px,f:p:g,p"));
            errorPolygonPB.addSeparator(UIRegistry.getResourceString(L10N+"ERROR_POLY"), cc.xyw(1, 1, 3));
            errorPolygonPB.add(errorPScrollPane, cc.xyw(1,3,3));
            errorPolygonPB.add(errorPBtn,        cc.xy(3, 6));
            
            corMarkerPB.add(errorPolygonPB.getPanel(), cc.xy(5,1));
            
            uncertBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (ucBtnState == UnCertBtnStateType.eApply)
                    {
                        long uRad = 0;
                        try
                        {
                            uRad = Long.parseLong(uncertTxt.getText());
                        } catch (Exception ex)
                        {
                            statusErrorLbl.setText(UIRegistry.getResourceString(L10N+"INV_ERR_RADIUS"));
                            return;
                        }
                        
                        geoMapper.editUncertaintyCircle(uRad);
                        geoMapper.hideEditUncertaintyHandle();
                        
                        uncertTxt.setEditable(false);
                        uncertBtn.setText(UIRegistry.getResourceString(L10N+"UNCRT_RADIUS"));
                        ucBtnState = UnCertBtnStateType.eEdit;
    					statusURLbl.setText("");
                    } else
                    {
                        uncertTxt.setEditable(true);
                        uncertBtn.setText(UIRegistry.getResourceString("Apply"));
                        ucBtnState = UnCertBtnStateType.eApply;
                        geoMapper.showEditUncertaintyHandle();
                    }
                }
            });
            
            errorPBtn.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (errBtnState == ErrBtnStateType.eApply)
                    {

                        List<GeoPosition> errorRegion = new ArrayList<GeoPosition>();
                        try
                        {
                            if (errorPTxt.getText().length() > 0)
                            {
                                double lat = Double.NaN; 
                                double lon = Double.NaN;
                                String[] latLons =  errorPTxt.getText().split(",");
                                for (int i=0; i<latLons.length; i++)
                                {
                                    if ((i%2) == 0) //Latitude.
                                        lat = Double.parseDouble(latLons[i]);
                                    else //Longitude.
                                    {
                                        lon = Double.parseDouble(latLons[i]);
                                        GeoPosition pos = new GeoPosition(lat, lon);
                                        errorRegion.add(pos);
                                    }
                                }
                            }
                        } catch (Exception ex)
                        {
                            statusErrorLbl.setText(UIRegistry.getResourceString(L10N+"INV_POLY"));
                            return;
                        }
                        
                        geoMapper.drawPolygon(errorRegion);
                        geoMapper.hideEditPolygonHandle();
                        errorPBtn.setText(UIRegistry.getResourceString(L10N+"CLR_POLYGON"));
                        errBtnState = ErrBtnStateType.eClear;
                        errorPTxt.setEditable(false);
                        statusMeasureTool.setEnabled(true);
                        statusClearRulerBtn.setEnabled(true);
                        
                    } else if (errBtnState == ErrBtnStateType.eDraw)
                    {
                        errorPTxt.setEditable(true);
                        errorPBtn.setText(UIRegistry.getResourceString("Apply"));
                        errBtnState = ErrBtnStateType.eApply;
                        geoMapper.showEditPolygonHandle();
                        
                        statusMeasureTool.setSelected(false);
                        statusMeasureTool.setEnabled(false);
                        
                    } else if (errBtnState == ErrBtnStateType.eClear)
                    {
                        errorPTxt.setText("");
                        geoMapper.removePolygon();
                        errorPBtn.setText(UIRegistry.getResourceString(L10N+"DRW_POLYGON"));
                        errBtnState = ErrBtnStateType.eDraw;
                        geoMapper.getMostAccurateResultPt().getLocality().setErrorPolygon(null);
                    }
                }
            });
            
            //add(mapLabel, cc.xywh(5,1,1,9));
            //Add the map.
        	geoMapper = new Mapper();
        	geoMapper.setMapSize(new Dimension(MAP_WIDTH2, MAP_HEIGHT2));
        	//geoMapper.setZoomButtonsVisible(false);
        	//geoMapper.setZoomSliderVisible(false);
        	
        	statusPanel = new JPanel();
        	statusPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        	statusPanel.setPreferredSize(new Dimension(MAP_WIDTH2, 46));
        	statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        	
        	Font font = new Font("Arial", Font.PLAIN, 10);
        	
        	statusLatLbl = new JLabel("Lat: ,", SwingConstants.LEFT);
        	statusLatLbl.setPreferredSize(new Dimension(80, 12));
        	statusLatLbl.setFont(font);
        	
        	statusLonLbl = new JLabel("Lon: ", SwingConstants.LEFT);
        	statusLonLbl.setPreferredSize(new Dimension(85, 12));
        	statusLonLbl.setFont(font);
        	
        	statusMeasureTool = new JCheckBox("Measure");
        	statusMeasureTool.setPreferredSize(new Dimension(60, 12));
        	statusMeasureTool.setFocusable(false);
        	statusMeasureTool.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if (statusMeasureTool.isSelected())
					{
						geoMapper.showMeasureDistanceHandle();
						statusClearRulerBtn.setEnabled(false);
					}
					
					else
					{
						geoMapper.hideMeasureDistanceHandle();
						statusClearRulerBtn.setEnabled(true);
					}
				}
			});
        	statusMeasureTool.setFont(font);
        	
        	statusURLbl = new JLabel("U. Radius: ", SwingConstants.LEFT);
        	statusURLbl.setPreferredSize(new Dimension(115, 12));
        	statusURLbl.setFont(font);
        	
        	//////////////////////////////////////////////////////////////////////////////////////////////////////
        	statusElevPanel = new JPanel();
        	statusElevPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        	statusElevPanel.setPreferredSize(new Dimension(176, 16));
        	statusElevPanel.setBorder(BorderFactory.createEmptyBorder());
        	
        	getElevBtn = new JButton("Get Elevation");
        	getElevBtn.setPreferredSize(new Dimension(80, 15));
        	getElevBtn.setFont(font);
        	getElevBtn.setFocusable(false);
        	statusElevPanel.add(getElevBtn);
        	
        	getElevBtn.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					isElevBtnDown = false;
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					isElevBtnDown = true;
					LocalityWaypoint mostAccurate = geoMapper.getMostAccurateResultPt();
					Double lat = mostAccurate.getPosition().getLatitude();
					Double lon = mostAccurate.getPosition().getLongitude();
					Double rLat = geoMapper.decimalRound(lat, 6);
					Double rLon = geoMapper.decimalRound(lon, 6);
					
					statusElevInfoLbl.setForeground(Color.BLACK);
					statusElevInfoLbl.setText("Getting Elevation @(" + rLat + ", " + rLon + ")...");
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					if (isElevBtnDown)
					{
						isElevBtnDown = false;
						statusElevInfoLbl.setText("");
					}
					
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
        	
        	getElevBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					Elevation_ServiceLocator service = null;
					Elevation_ServiceSoap port = null;
					
					String elevInfo = "";
					Double elev = 0d;
					
					LocalityWaypoint mostAccurate = geoMapper.getMostAccurateResultPt();
					Double lat = mostAccurate.getPosition().getLatitude();
					Double lon = mostAccurate.getPosition().getLongitude();
					//Double rLat = geoMapper.decimalRound(lat, 6);
					//Double rLon = geoMapper.decimalRound(lon, 6);
					
					//statusElevInfoLbl.setForeground(Color.BLACK);
					//statusElevInfoLbl.setText("Getting Elevation at (" + rLat + ", " + rLon + ")...");
					
					try
					{
						service = new Elevation_ServiceLocator();
						if (service != null)
						{
							port = service.getElevation_ServiceSoap();
							if (port != null)
							{
								try
								{
									GetElevationResponseGetElevationResult result = 
											port.getElevation(lon.toString(), lat.toString(), "meters", "", "");

									Iterator<?> it = result.get_any()[0].getChildElements();
									while (it.hasNext()) {
										MessageElement msgEle = (MessageElement) it.next();
										if (msgEle.getName().toLowerCase().equals("Elevation_Query".toLowerCase()))
										{
											Iterator<?> it2 = msgEle.getChildElements();
											
											
											while (it2.hasNext()) {
												Object it2Obj = it2.next();
												if(it2Obj.getClass() == MessageElement.class)
												{
													MessageElement msgEle2 = (MessageElement) it2Obj;
													if (msgEle2.getName().toLowerCase().equals(
															"Data_Source".toLowerCase()))
													{
														elevInfo = msgEle2.getValue();
													}
													
													if (msgEle2.getName().toLowerCase().equals(
															"Elevation".toLowerCase()))
													{
														String elevStr = msgEle2.getValue();
														elev = geoMapper.decimalRound(Double.parseDouble(elevStr), 2);
													}
												}
												
												else
												{
													//The elevation query node contains a string describing
													//the error. mostly:
													//"ERROR: No Elevation values were returned from any source".
													elevInfo = "";
													statusElevInfoLbl.setForeground(Color.BLACK);
													statusElevInfoLbl.setText("");
													statusElevLbl.setText("N/A");
													break;
												}
											}
											
											if (elevInfo.length() > 0)
											{
												statusElevInfoLbl.setForeground(Color.BLACK);
												statusElevInfoLbl.setText(elevInfo);
												statusElevLbl.setText(elev.toString() + "m");
											}
											
											break;
										}
									}
								}
								
								catch (RemoteException remEx)
								{
									statusElevInfoLbl.setForeground(Color.RED);
									statusElevInfoLbl.setText("Error: unable to parse elevation data.");
								}
							}
						}
					}
					
					catch (ServiceException servEx) {
						statusElevInfoLbl.setForeground(Color.RED);
						statusElevInfoLbl.setText("Error: elevation service unavailable.");
						servEx.printStackTrace();
					} catch (OutOfMemoryError em1) {
					    SwingUtilities.invokeLater(new Runnable()
	                    {
	                        @Override
	                        public void run()
	                        {
	                            UIRegistry.showError("Out of Memory! Please restart Specify.");
	                        }
	                    });
					    
					    return;
					}
				}
			});
        	
        	statusElevLbl = new JLabel("", SwingConstants.LEFT);
        	statusElevLbl.setPreferredSize(new Dimension(90, 12));
        	statusElevLbl.setFont(font);
        	statusElevPanel.add(statusElevLbl);
        	
        	statusElevInfoLbl = new JLabel("", SwingConstants.LEFT);
        	statusElevInfoLbl.setPreferredSize(new Dimension(596, 12));
        	statusElevInfoLbl.setFont(font);
        	//////////////////////////////////////////////////////////////////////////////////////////////////////

        	//////////////////////////////////////////////////////////////////////////////////////////////////////
        	statusClearRulerPanel = new JPanel();
        	statusClearRulerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        	statusClearRulerPanel.setPreferredSize(new Dimension(80, 16));
        	statusClearRulerPanel.setBorder(BorderFactory.createEmptyBorder());
        	
        	statusClearRulerBtn = new JButton("Clear Ruler");
        	statusClearRulerBtn.setPreferredSize(new Dimension(70, 15));
        	statusClearRulerBtn.setFont(font);
        	statusClearRulerBtn.setFocusable(false);
        	statusClearRulerBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					geoMapper.removeRuler();
					if (statusMeasureTool.isSelected())
						geoMapper.showMeasureDistanceHandle();
				}
			});
        	statusClearRulerPanel.add(statusClearRulerBtn);
        	//////////////////////////////////////////////////////////////////////////////////////////////////////
        	
        	statusErrorLbl = new JLabel("", SwingConstants.LEFT);
        	statusErrorLbl.setPreferredSize(new Dimension(596, 12));
        	statusErrorLbl.setForeground(Color.RED);
        	statusErrorLbl.setFont(font);
        	
        	statusPanel.add(statusElevPanel);
        	statusPanel.add(statusURLbl);
        	statusPanel.add(statusMeasureTool);
        	statusPanel.add(statusClearRulerPanel);
        	statusPanel.add(statusLatLbl);
        	statusPanel.add(statusLonLbl);
        	statusPanel.add(statusElevInfoLbl);
        	statusPanel.add(statusErrorLbl);
        	
        	JPanel mPanel = new JPanel((LayoutManager) (new BorderLayout(0, 2)));
        	mPanel.setPreferredSize(new Dimension(MAP_WIDTH2, MAP_HEIGHT2 + statusPanel.getHeight()));
        	mPanel.setMinimumSize(new Dimension(MAP_WIDTH2, MAP_HEIGHT2 + statusPanel.getHeight()));
        	
        	geoMapper.addMeasureDistanceListener(new MeasureDistanceListener() {
				
				@Override
				public void distanceMeasured(MeasureDistanceEvent evt) {
					statusMeasureTool.setSelected(false);
					statusClearRulerBtn.setEnabled(true);
				}
			});
        	
        	geoMapper.addMeasureDistanceCancelListener(new MeasureDistanceCancelListener() {
				
				@Override
				public void measureDistanceCancelled() {
					statusMeasureTool.setSelected(false);
					statusClearRulerBtn.setEnabled(true);
				}
			});
        	
        	geoMapper.addUncertaintyCircleResizeCancelListener(new UncertaintyCircleResizeCancelListener() {
				
				@Override
				public void uncertaintyCircleResizeCancelled() {
                    uncertBtn.setText(UIRegistry.getResourceString(L10N+"UNCRT_RADIUS"));
                    ucBtnState = UnCertBtnStateType.eEdit;
					uncertTxt.setEditable(false);
					statusURLbl.setText("");
				}
			});
        	
        	geoMapper.addErrorPolygonDrawCancelListener(new ErrorPolygonDrawCancelListener() {
				
				@Override
				public void errorPolygonDrawCancelled() {
					errorPTxt.setText("");
					errorPTxt.setEditable(false);
                    errorPBtn.setText(UIRegistry.getResourceString(L10N+"DRW_POLYGON"));
                    errBtnState = ErrBtnStateType.eDraw;
                    statusMeasureTool.setEnabled(true);
                    statusClearRulerBtn.setEnabled(true);
				}
			});
        	
        	geoMapper.addErrorPolygonDrawListener(new ErrorPolygonDrawListener() {
				
				@Override
				public void errorPolygonDrawn(ErrorPolygonDrawEvent evt) {
				    setTextIntoErrorPolygonTA(geoMapper.getMostAccurateResultPt().getLocality().getErrorPolygon());
					geoMapper.hideEditPolygonHandle();
                    errorPBtn.setText(UIRegistry.getResourceString(L10N+"CLR_POLYGON"));
                    errBtnState = ErrBtnStateType.eClear;
					errorPTxt.setEditable(false);
					statusMeasureTool.setEnabled(true);
					statusClearRulerBtn.setEnabled(true);
				}
			});
        	
        	geoMapper.addUncertaintyCircleResizeListener(new UncertaintyCircleResizeListener() {
				
				@Override
				public void uncertaintyCircleResized(UncertaintyCircleResizeEvent evt) {
					uncertTxt.setText(geoMapper.getMostAccurateResultPt().getLocality()
							.getUncertaintyMeters());
					
					//geoMapper.hideEditUncertaintyHandle();
                    //uncertBtn.setText(UIRegistry.getResourceString(L10N+"UNCRT_RADIUS"));
                    //ucBtnState = UnCertBtnStateType.eEdit;
					//uncertTxt.setEditable(false);
					//statusURLbl.setText("U. Radius: ");
				}
			});
        	
        	geoMapper.addUncertaintyCircleChangeListener(new UncertaintyCircleChangeListener() {
				
				@Override
				public void uncertaintyCircleChanged(UncertaintyCircleChangeEvent evt) {
					statusURLbl.setText("U. Radius: " + Long.toString(evt.getUncertaintyRadiusInMeters()) + "m");
				}
			});
        	
        	geoMapper.addMapPointerMoveListener(new MapPointerMoveListener() {
				
				@Override
				public void mapPointerMoved(MapPointerMoveEvent evt) {
					GeoPosition pointerPos = evt.getLocation();
            		setStatusBarCoordinates(pointerPos.getLatitude(), pointerPos.getLongitude());
				}
			});
        	
        	geoMapper.addMostAccuratePointReleaseListener(new MostAccuratePointReleaseListener() {
				
				@Override
				public void mostAccuratePointReleased(MapPointerMoveEvent evt) {
					LocalityWaypoint mostAccurate = geoMapper.getMostAccurateResultPt();
					double lat = mostAccurate.getPosition().getLatitude();
                	double lon = mostAccurate.getPosition().getLongitude();
                	latText.setText(Double.toString(geoMapper.decimalRound(lat, 6)));
					lonText.setText(Double.toString(geoMapper.decimalRound(lon, 6)));
					
					statusElevInfoLbl.setText("");
			        statusElevLbl.setText("");
					
					String resUncert = mostAccurate.getLocality().getUncertaintyMeters();
					if ((resUncert != null) && !(resUncert.equalsIgnoreCase("unavailable")))
						uncertTxt.setText(resUncert);
					else
						uncertTxt.setText("");
					
					String resErrorP = mostAccurate.getLocality().getErrorPolygon();
					if ((resErrorP != null) && !(resErrorP.equalsIgnoreCase("unavailable")))
						setTextIntoErrorPolygonTA(resErrorP);
					else
						errorPTxt.setText("");
					
					if (ucBtnState == UnCertBtnStateType.eApply)
						geoMapper.persistEditUncertaintyHandle();
				}
			});
        	
        	geoMapper.addMostAccuratePointSnapListener(new MostAccuratePointSnapListener() {
				
				@Override
				public void mostAccuratePointSnapped(MapPointerMoveEvent evt) {
					int index = 0;
					double snapLat = evt.getLocation().getLatitude();
					double snapLon = evt.getLocation().getLongitude();
					
					for (int i=0; i<tableModel.getResults().size(); i++)
					{
						index = i;
						Georef_Result res = tableModel.getResult(index);
						double resLat = geoMapper.decimalRound(res.getWGS84Coordinate().getLatitude(), 6);
						double resLon = geoMapper.decimalRound(res.getWGS84Coordinate().getLongitude(), 6);
						if ((snapLat == resLat) && (snapLon == resLon))
						{
							latText.setText(Double.toString(resLat));
							lonText.setText(Double.toString(resLon));
							String resUncert = res.getUncertaintyRadiusMeters();
							if ((resUncert != null) && !(resUncert.equalsIgnoreCase("unavailable")))
								uncertTxt.setText(resUncert);
							else
								uncertTxt.setText("");
							
							String resErrorP = res.getUncertaintyPolygon();
							if ((resErrorP != null) && !(resErrorP.equalsIgnoreCase("unavailable")))
							{
								setTextIntoErrorPolygonTA(resErrorP);
		                        errorPBtn.setText(UIRegistry.getResourceString(L10N+"CLR_POLYGON"));
		                        errBtnState = ErrBtnStateType.eClear;
		                        
							} else
							{
								errorPTxt.setText("");
							}
							break;
						}
					}
					
					uncertBtn.setText(UIRegistry.getResourceString(L10N+"UNCRT_RADIUS"));
					ucBtnState = UnCertBtnStateType.eEdit;
					uncertTxt.setEditable(false);
					statusURLbl.setText("");
					statusElevInfoLbl.setText("");
			        statusElevLbl.setText("");
					
					// Auto select the row corresponding to the point snapped on.
			        resultsTable.getSelectionModel().setSelectionInterval(index, index);
			        resultsTable.repaint();
				}
			});

        	mPanel.add(geoMapper, BorderLayout.CENTER);
        	mPanel.add(statusPanel, BorderLayout.SOUTH);
            mainPB.add(mPanel, cc.xywh(5,1,1,15)); 
        } 
        
        else
        {
            wwPanel = new WorldWindPanel();
            wwPanel.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
            wwPanel.getWorld().addSelectListener(new ClickAndGoSelectListener(wwPanel.getWorld(), MarkerLayer.class));
            wwPanel.getWorld().addSelectListener(this);
            
            wwPanel.getWorld().getInputHandler().addMouseListener(new MouseAdapter()
            {
                /* (non-Javadoc)
                 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
                 */
                @Override
                public void mouseClicked(final MouseEvent e)
                {
                    super.mouseClicked(e);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            Position pos = wwPanel.getWorld().getCurrentPosition();
                            if (!pos.equals(lastClickPos))
                            {
                                if (userDefGeoRef == null)
                                {
                                    addUserDefinedMarker();
                                } else 
                                {
                                    repositionUserDefMarker();
                                }
                            }
                        }
                    });
                }
            });
            
            mainPB.add(wwPanel, cc.xywh(5,1,1,13));
        }

        // add the results table
        tableModel   = new ResultsTableModel();
        resultsTable = new JTable(tableModel);
        resultsTable.setShowVerticalLines(false);
        resultsTable.setShowHorizontalLines(false);
        resultsTable.setRowSelectionAllowed(true);
        resultsTable.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false));
        
        resultsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    if (acceptBtn != null)
                    {
                        //System.out.println(resultsTable.getSelectedRowCount());
                        acceptBtn.setEnabled(resultsTable.getSelectedRowCount() > 0);
                    }
                }
            }
        });
        
        if (wwPanel != null)
        {
            resultsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    wwPanel.flyToMarker(resultsTable.getSelectedRow());
                }
            });
        } else
        {
        	resultsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                	if (resultsTable.getSelectedRow() > -1)
                	{
	                	Georef_Result res = tableModel.getResult(resultsTable.getSelectedRow());
	                	double lat = res.getWGS84Coordinate().getLatitude();
	                	double lon = res.getWGS84Coordinate().getLongitude();
	                	geoMapper.snapMostAccuratePointTo(res.getReferenceLocation());
	                	latText.setText(Double.toString(geoMapper.decimalRound(lat, 6)));
						lonText.setText(Double.toString(geoMapper.decimalRound(lon, 6)));
						geoMapper.hideEditUncertaintyHandle();
						geoMapper.hideEditPolygonHandle();
						geoMapper.hideMeasureDistanceHandle();
						geoMapper.removeRuler();
						statusMeasureTool.setSelected(false);
						statusMeasureTool.setEnabled(true);
						statusClearRulerBtn.setEnabled(true);
						statusURLbl.setText("");
						statusElevInfoLbl.setText("");
						statusElevLbl.setText("");
						statusErrorLbl.setText("");
						
						String resUncert = res.getUncertaintyRadiusMeters();
						if ((resUncert != null) && !(resUncert.equalsIgnoreCase("unavailable")))
						{
							uncertTxt.setText(resUncert);
							uncertTxt.setEditable(false);
	                        uncertBtn.setText(UIRegistry.getResourceString(L10N+"UNCRT_RADIUS"));
	                        ucBtnState = UnCertBtnStateType.eEdit;
						}
						else
						{
							uncertTxt.setText("");
							uncertTxt.setEditable(true);
	                        uncertBtn.setText(UIRegistry.getResourceString("Apply"));
	                        ucBtnState = UnCertBtnStateType.eApply;

						}
						
						String resErrorP = res.getUncertaintyPolygon();
						if ((resErrorP != null) && !(resErrorP.equalsIgnoreCase("unavailable")))
						{
							setTextIntoErrorPolygonTA(resErrorP);
							errorPTxt.setEditable(false);
	                        errorPBtn.setText(UIRegistry.getResourceString(L10N+"CLR_POLYGON"));
	                        errBtnState = ErrBtnStateType.eClear;
						}
						else
						{
							errorPTxt.setText("");
							errorPTxt.setEditable(false);
	                        errorPBtn.setText(UIRegistry.getResourceString(L10N+"DRW_POLYGON"));
	                        errBtnState = ErrBtnStateType.eDraw;
						}
                	}
                }
            });
        }

        // add a cell renderer that uses the tooltip to show the text of the "parse pattern" column in case
        // it is too long to show and gets truncated by the standard cell renderer
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof String)
                {
                    ((JLabel)c).setToolTipText((String)value);
                }
                return c;
            }
        };
        resultsTable.getColumnModel().getColumn(3).setCellRenderer(cellRenderer);
        
        mainPB.addSeparator(UIRegistry.getResourceString(L10N+"GEO_LOC_RESULTS"), cc.xywh(1,rowIndex, 3, 1));
        rowIndex +=2;
        
        JScrollPane scrollPane = new JScrollPane(resultsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainPB.add(scrollPane, cc.xywh(1,rowIndex, 3, 1));
        rowIndex += 2;
        
        //mainPB.setDefaultDialogBorder();
    }
    
    /**
     * @param errStr
     */
    private void setTextIntoErrorPolygonTA(final String errStr)
    {
        if (StringUtils.isNotEmpty(errStr))
        {
            errorPTxt.setText(StringUtils.replace(errStr, ",", ", "));
        }
    }
    
    /**
     * @param latitude
     * @param longitude
     */
    protected void setStatusBarCoordinates(double latitude, double longitude)
    {
        statusLatLbl.setText(String.format("Lat: %10.6f,", latitude));
        statusLonLbl.setText(String.format("Lon: %11.6f", longitude));
    }

    /**
     * 
     */
    protected void applyManualCoordinates()
    {

        double lat = Double.NaN;
        double lon = Double.NaN;

        try
        {
            lat = Double.parseDouble(latText.getText());
            lon = Double.parseDouble(lonText.getText());
        } catch (Exception ex)
        {
            statusErrorLbl.setText("Error: Invalid coordinate(s) data type.");
            return;
        }

        statusElevInfoLbl.setText("");
        statusElevLbl.setText("");
        geoMapper.snapMostAccuratePointTo(new GeoPosition(lat, lon));
    }

	/**
     * @param acceptBtn the acceptBtn to set
     */
    public void setAcceptBtn(JButton acceptBtn)
    {
        this.acceptBtn = acceptBtn;
    }

    /**
     * 
     */
    private void addUserDefinedMarker()
    {
        Position        pos = wwPanel.getWorld().getCurrentPosition();
        GeographicPoint pnt = new GeographicPoint();
        pnt.setLatitude(pos.getLatitude().getDegrees());
        pnt.setLongitude(pos.getLongitude().getDegrees());
        
        // Create User defined point/marker
        userDefGeoRef = new Georef_Result();
        userDefGeoRef.setWGS84Coordinate(pnt);
        userDefGeoRef.setParsePattern(getResourceString(L10N + "USRDEF")); // XXX I18N
        tableModel.add(userDefGeoRef);
        
        // Auto select the User Defined row
        int lastRow = tableModel.getRowCount() - 1;
        resultsTable.getSelectionModel().setSelectionInterval(lastRow, lastRow);
        resultsTable.repaint();
        
        wwPanel.placeMarkers(tableModel.getPoints(), null);
    }
    
    /**
     * 
     */
    private void repositionUserDefMarker()
    {
        Position        pos = wwPanel.getWorld().getCurrentPosition();
        GeographicPoint pnt = userDefGeoRef.getWGS84Coordinate();
        pnt.setLatitude(pos.getLatitude().getDegrees());
        pnt.setLongitude(pos.getLongitude().getDegrees());
        
        tableModel.fireTableCellUpdated(tableModel.getRowCount()-1, 1);
        tableModel.fireTableCellUpdated(tableModel.getRowCount()-1, 2);
        
        wwPanel.placeMarkers(tableModel.getPoints(), null);
        wwPanel.getWorld().repaint();
        
        int lastRow = tableModel.getRowCount() - 1;
        resultsTable.getSelectionModel().setSelectionInterval(lastRow, lastRow);
        resultsTable.repaint();
    }
    
    /**
     * @param localityString
     * @param county
     * @param state
     * @param country
     * @param georefResults
     */
    public void setGeoLocateQueryAndResults(String localityString, 
                                            String county, 
                                            String state, 
                                            String country, 
                                            Georef_Result_Set georefResults)
    {
        localityStringField.setText(localityString);
        localityStringField.setCaretPosition(0);
        countyField.setText(county);
        countyField.setCaretPosition(0);
        stateField.setText(state);
        stateField.setCaretPosition(0);
        countryField.setText(country);
        countryField.setCaretPosition(0);
        
        tableModel.setResultSet(georefResults.getResultSet());
        
        if (wwPanel != null)
        {
            ArrayList<LatLonPlacemarkIFace> pnts = new ArrayList<LatLonPlacemarkIFace>(georefResults.getResultSet().length);
            for (Georef_Result grr : georefResults.getResultSet())
            {
                pnts.add(new LatLonPoint(grr.getWGS84Coordinate().getLatitude(), grr.getWGS84Coordinate().getLongitude()));
            }
            wwPanel.placeMarkers(pnts, 0);
            
        } else
        {
            //mapLabel.setText(getResourceString(L10N + "LOADING_MAP")); //$NON-NLS-1$
        	//Build locality way points to plot.
        	LocalityWaypoint[] lWps = new LocalityWaypoint[ georefResults.getNumResults()];
        	Integer index = 0;
        	for (Georef_Result grr : georefResults.getResultSet())
            {
        		Locality loc = new Locality();
        		loc.setLocality(localityString);
        		loc.setCountry(country);
        		loc.setState(state);
        		loc.setCounty(county);
        		loc.setPrecision(grr.getPrecision());
        		loc.setScore(grr.getScore());
        		loc.setLatitude(grr.getWGS84Coordinate().getLatitude());
        		loc.setLongitude(grr.getWGS84Coordinate().getLongitude());
        		loc.setErrorPolygon(grr.getUncertaintyPolygon());
        		loc.setUncertaintyMeters(grr.getUncertaintyRadiusMeters());
        		loc.setLocalityId(index.toString());
        		lWps[index] = new LocalityWaypoint(loc);
        		grr.setReferenceLocation(index.toString());
        		index++;
            }
        	geoMapper.plotResultSet(lWps, 0);
        }
        //TODO: This might be crucial!
        //GeoLocate.getMapOfGeographicPoints(georefResults.getResultSet(), GeoLocateResultsDisplay.this);
        
        // set the table height to at most 10 rows
        Dimension size = resultsTable.getPreferredScrollableViewportSize();
        size.height = Math.min(size.height, resultsTable.getRowHeight()*10);
        resultsTable.setPreferredScrollableViewportSize(size);
        UIHelper.calcColumnWidths(resultsTable);
    }
    
    /**
     * Returns the selected result.
     * 
     * @return the selected result
     */
    public Georef_Result getSelectedResult()
    {
        int rowIndex = resultsTable.getSelectedRow();
        if (rowIndex < 0 || rowIndex > tableModel.getRowCount())
        {
            return null;
        }
        
        return tableModel.getResult(rowIndex);
    }
    
    /**
     * Selects the result with the given index in the results list.
     * 
     * @param index the index of the result to select
     */
    public void setSelectedResult(int index)
    {
        if (index < 0 || index > resultsTable.getRowCount()-1)
        {
            resultsTable.clearSelection();
        }
        else
        {
            resultsTable.setRowSelectionInterval(index, index);
            int colCount = resultsTable.getColumnCount();
            resultsTable.setColumnSelectionInterval(0, colCount-1);
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener#exceptionOccurred(java.lang.Exception)
     */
    public void exceptionOccurred(Exception e)
    {
        if (mapLabel != null) mapLabel.setText(getResourceString(L10N + "ERROR_GETTING_MAP")); //$NON-NLS-1$
        JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setErrorMessage(getResourceString(L10N + "ERROR_GETTING_MAP"), e); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener#mapReceived(javax.swing.Icon)
     */
    public void mapReceived(Icon map)
    {
        JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setText(""); //$NON-NLS-1$
        mapLabel.setText(null);
        mapLabel.setIcon(map);
        repaint();
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.worldwind.event.SelectListener#selected(gov.nasa.worldwind.event.SelectEvent)
     */
    @Override
    public void selected(final SelectEvent event)
    {
        if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
        {
            // This is a left click
            if (event.hasObjects() && event.getTopPickedObject().hasPosition())
            {
                lastClickPos = wwPanel.getWorld().getCurrentPosition();
                
                // There is a picked object with a position
// ZZZ                 
//                if (wwPanel.getWorld().getView() instanceof OrbitView)
//                {
//                    if (event.getTopObject().getClass().equals(BasicMarker.class))
//                    {
//                        int inx = wwPanel.getMarkers().indexOf(event.getTopObject());
//                        if (inx > -1)
//                        {
//                            resultsTable.setRowSelectionInterval(inx, inx);
//                        }
//                    } else if (event.getTopObject().getClass().equals(GlobeAnnotation.class))
//                    {
//                        int inx = wwPanel.getAnnotations().indexOf(event.getTopObject());
//                        if (inx > -1)
//                        {
//                            resultsTable.setRowSelectionInterval(inx, inx);
//                        }
//                    }
//                }
            }
        }
    }

    /**
     * Adds a new row to this object's content area.
     * 
     * @param cc the cell constraints of the new row
     * @param labelStr the text label for the new row
     * @param column the starting column number for the new row's UI
     * @param row the row number of the new row
     * @return the {@link JTextField} added to the new row
     */
    protected JTextField addRow(final CellConstraints cc,
                                final String labelStr,
                                final int column,
                                final int row)
    {
        add(createI18NFormLabel(labelStr), cc.xy(column,row)); //$NON-NLS-1$
        JTextField tf = createTextField();
        tf.setEditable(false);
        add(tf, cc.xy(column+2,row));
        return tf;
    }
    
    /**
     * Cleans up the panel.
     */
    public void shutdown()
    {
        if (wwPanel != null)
        {
            wwPanel.shutdown();
        }
    }

    /**
     * Creates a {@link JTextField} customized for use in this UI widget.
     * 
     * @return a {@link JTextField}
     */
    protected JTextField createTextField()
    {
        JTextField tf     = UIHelper.createTextField();
        Insets     insets = tf.getBorder().getBorderInsets(tf);
        tf.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        tf.setForeground(Color.BLACK);
        tf.setBackground(Color.WHITE);
        tf.setEditable(false);
        return tf;
    }
    
    //-----------------------------------------------------------------
    //
    //-----------------------------------------------------------------
    protected class ResultsTableModel extends AbstractTableModel
    {
        protected List<Georef_Result> results;
        
        public void setResultSet(Georef_Result[] results)
        {
            this.results = new ArrayList<Georef_Result>();
            for (Georef_Result grr : results)
            	this.results.add(grr);
            		
            fireTableDataChanged();
        }
        
        /**
         * @param grr
         */
        public void add(final Georef_Result grr)
        {
            results.add(grr);
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    fireTableDataChanged();
                }
            });
        }
        
        /**
         * @param index
         * @return
         */
        public Georef_Result getResult(int index)
        {
            return results.get(index);
        }
        
        /**
         * @return the results
         */
        public List<Georef_Result> getResults()
        {
            return results;
        }
        
        /**
         * @return
         */
        public List<LatLonPlacemarkIFace> getPoints()
        {
            ArrayList<LatLonPlacemarkIFace> pnts = new ArrayList<LatLonPlacemarkIFace>(results.size());
            
            for (Georef_Result grr : results)
            {
                pnts.add(new LatLonPoint(grr.getWGS84Coordinate().getLatitude(), grr.getWGS84Coordinate().getLongitude()));
            }
            return pnts;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            switch (columnIndex)
            {
                case 0:
                {
                    return Integer.class;
                }
                case 1:
                case 2:
                {
                    return Double.class;
                }
                case 3:
                case 4:
                case 5:
                case 6:
                {
                    return String.class;
                }
            }
            return null;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            switch (column)
            {
                case 0:
                {
                    return getResourceString(L10N + "NUMBER"); //$NON-NLS-1$
                }
                case 1:
                {
                    return getResourceString(L10N + "LATITUDE"); //$NON-NLS-1$
                }
                case 2:
                {
                    return getResourceString(L10N + "LONGITUDE"); //$NON-NLS-1$
                }
                case 3:
                {
                    return getResourceString(L10N + "PARSE_PATTERN"); //$NON-NLS-1$
                }
                case 4:
                {
                    return getResourceString(L10N + "PRECISION"); //$NON-NLS-1$
                }
                case 5:
                {
                    return getResourceString(L10N + "ERROR_POLY"); //$NON-NLS-1$
                }
                case 6:
                {
                    return getResourceString(L10N + "UNCERTAINTY"); //$NON-NLS-1$
                }
            }
            return null;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return 7;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return (results == null) ? 0 : results.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            Georef_Result res = results.get(rowIndex);
            switch (columnIndex)
            {
                case 0:
                {
                    return rowIndex+1;
                }
                case 1:
                {
                    return res.getWGS84Coordinate().getLatitude();
                }
                case 2:
                {
                    return res.getWGS84Coordinate().getLongitude();
                }
                case 3:
                {
                	String pattern = res.getParsePattern();
                	String debugStr = res.getDebug();
                	if ((debugStr != null) && (debugStr.length() > 0))
                	{
                		String[] debugPairs = debugStr.split("\\|");
                		for (int i = 0; i < debugPairs.length; i++)
                		{
                			String key = "";
                			String value = "";
                			
                			if (debugPairs[i].split("=").length > 1)
                			{
                				key = debugPairs[i].split("=")[0];
                    			value = debugPairs[i].split("=")[1];
                			}
                			
                			if (key.toLowerCase().indexOf(":adm") == 0)
                			{
                				pattern += " (Adm: " + value + ")";
                				break;
                			}
                		}
                	}
                	
                	return pattern;
                }
                case 4:
                {
                	if (res.getPrecision() != null)
                		return (res.getPrecision() + " (" + res.getScore() + ")");
                	else
                		return "N\\A";
                }
                case 5:
                {
                	String cellText = "unavailable";
                	if (res.getUncertaintyPolygon() != null)
                	{
	                	String polyString = res.getUncertaintyPolygon();
	                	if (!cellText.equalsIgnoreCase(polyString.toLowerCase()))
	                	{
	                		cellText = "present";
	                	}
                	}
                	
                	else
                		cellText = "N\\A";
                	
                	return cellText;
                }
                case 6:
                {
                	if (res.getUncertaintyRadiusMeters() != null)
                		return res.getUncertaintyRadiusMeters();
                	else
                		return "N\\A";
                }
            }
            return null;
        }
    }
}
