package edu.ku.brc.services.geolocate.prototype;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;

@SuppressWarnings("serial")
public class Mapper extends JXMapKit {
	
	private static final int ptStrokeOffsetX = -6;
	private static final int ptStrokeOffsetY = -6;
	private static final int ptStrokeWidth = 12;
	private static final int ptStrokeHeight = 12;
	private static final int ptFillOffsetX = -5;
	private static final int ptFillOffsetY = -5;
	private static final int ptFillWidth = 10;
	private static final int ptFillHeight = 10;
	private static final int defaultZoom = 13;
	private final int uHandleX = 10;
	private final int uHandleY = 0;
	private final int uHandleStrokeWidth = 3;
	private final int uHandleNPoints = 4;
	private final double uRotationAngleDeg = -45;
	private final double uRotationAngleRad = uRotationAngleDeg * Math.PI/180;
	private final int[] xArray = {uHandleX, uHandleX+40, uHandleX+30, uHandleX+40};
    private final int[] yArray = {uHandleY, uHandleY-6, uHandleY, uHandleY+6};
    private int previousFrameCount = 0;
    private boolean shiftRegion = false;
	private WaypointPainter<JXMapViewer> greenPainter;
	private WaypointPainter<JXMapViewer> redPainter;
	private WaypointPainter<JXMapViewer> polyCursorPainter;
	private WaypointPainter<JXMapViewer> uncertaintyHandlePainter;
	private Painter<JXMapViewer> polygonOverlayPainter;
	private Painter<JXMapViewer> uPolygonOverlayPainter;
	private WaypointPainter<JXMapViewer> rulerCursorPainter;
	private Painter<JXMapViewer> rulerOverlayPainter;
	private boolean isGreenPtSelected;
	private boolean isUHandleSelected;
	private boolean mouseDragged;
	private boolean drawErrorPolygon;
	private boolean measureDistance;
	private boolean isEditUncertaintyHandlePersisted;
	private long uncertaintyRadius;
	private List<GeoPosition> errorRegion;
	private List<GeoPosition> uncertaintyRegion;
	private List<GeoPosition> rulerSegments;
	private LocalityWaypoint mostAccurateResultPt;
	private Set<LocalityWaypoint> resultPoints;
	private GeoPosition handlePos;
	private double handleAz;
	private double handleOffsetX;
	private double handleOffsetY;
	private ScaleLine scaleLine;
	private boolean scaleLineVisible = true;
	private Point scaleLineLocation;
	
	protected EventListenerList mapPointerMoveListeners = new EventListenerList();
	protected EventListenerList mostAccuratePointReleaseListeners = new EventListenerList();
	protected EventListenerList mostAccuratePointSnapListeners = new EventListenerList();
	protected EventListenerList errorPolygonDrawListeners = new EventListenerList();
	protected EventListenerList errorPolygonDrawCancelListeners = new EventListenerList();
	protected EventListenerList uncertaintyCircleResizeListeners = new EventListenerList();
	protected EventListenerList uncertaintyCircleResizeCancelListeners = new EventListenerList();
	protected EventListenerList uncertaintyCircleChangeListeners = new EventListenerList();
	protected EventListenerList measureDistanceListeners = new EventListenerList();
	protected EventListenerList measureDistanceCancelListeners = new EventListenerList();
	
	public ScaleLine getScaleLine() {
		return this.scaleLine;
	}
	
	public boolean isScaleLineVisible() {
		return scaleLineVisible;
	}
	
	public void setScaleLineVisible(boolean scaleLineVisible) {
		this.scaleLineVisible = scaleLineVisible;
		scaleLine.setVisible(scaleLineVisible);
	}
	
	//Converts distance in kilometers or meters to miles.
	private double metricToMiles(double metricDist, boolean isMeters) {
	    double cFactor = 0.621371192; //km to mi conversion factor.
	    
	    if (isMeters)
	    	metricDist /= 1000; //Convert m to km.
	    double distInMiles = metricDist * cFactor;

	    return distInMiles; 
	}
	
	//Converts distance in miles or feet to meters.
	private double imperialToMeters(double imperialDist, boolean isFeet) {
		int miToFtFactor = 5280; // mi to ft conversion factor.
		double cFactor = 0.621371192; //km to mi conversion factor.
		if (isFeet)
			imperialDist /= miToFtFactor; //Convert ft to mi;
		
		double distInMeters = 1000 * (imperialDist / cFactor);
		return distInMeters;
	}
	
	//Converts miles to feet.
	private double milesToFeet(double distInMiles) {
		int miToFtFactor = 5280; // mi to ft conversion factor.
		double distInFeet = miToFtFactor * distInMiles;
		
		return distInFeet;
	}
	
	//Normalizes imperial distance.
	private int[] normalizeImperial(double distInMiles) {
		int isMi = 1;
		
		if (distInMiles < 1) { //Convert to feet.
			distInMiles = milesToFeet(distInMiles);
			isMi = 0;
		}
		
		//calculate an even multiple of 1000.
		double mod = distInMiles % 1000;
		if ((mod > 0) && ((mod - distInMiles) != 0)) {
			distInMiles = distInMiles - mod;
		}
		
		//calculate an even multiple of 100.
		mod = distInMiles % 100;
		if ((mod > 0) && ((mod - distInMiles) != 0)) {
			distInMiles = distInMiles - mod;
		}
		
		//calculate an even multiple of 10.
		mod = distInMiles % 10;
		if ((mod > 0) && ((mod - distInMiles) != 0)) {
			distInMiles = distInMiles - mod;
		}
		
		return new int[] {(int)Math.round(distInMiles), isMi};
	}
	
	//Normalizes metric distance.
	private int[] normalizeMetric(double distInMeters) {
		int isKm = 0;
		//Is the distance big enough to be in km?
		double mod = distInMeters % 1000;
		if ((mod > 0) && ((mod - distInMeters) != 0)) {
			//About how many km?
			isKm = 1;
			distInMeters = (distInMeters - mod) / 1000; //Distance is now in kilometers.
		}
		
		//calculate an even multiple of 100.
		mod = distInMeters % 100;
		if ((mod > 0) && ((mod - distInMeters) != 0)) {
			distInMeters = distInMeters - mod;
		}
		
		//calculate an even multiple of 10.
		mod = distInMeters % 10;
		if ((mod > 0) && ((mod - distInMeters) != 0)) {
			distInMeters = distInMeters - mod;
		}
		
		return new int[] {(int)Math.round(distInMeters), isKm};
	}
	
	private void adjustScaleLine() {
		//The goal is to find the greatest whole multiple on 10 (in geo-distance) we can fit in the
		//maximum pixel length of the bar.
		int maxPxLength = 90;
		int startPxX = scaleLineLocation.x;
		int endPxX = startPxX + maxPxLength;
		
		Point endScaleLineLocation = new Point(endPxX, scaleLineLocation.y);
		
		GeoPosition startGP = getMainMap().convertPointToGeoPosition(scaleLineLocation);
		startGP = new GeoPosition(startGP.getLatitude(), getSphericalLon(startGP.getLongitude()));
		GeoPosition endGP = getMainMap().convertPointToGeoPosition(endScaleLineLocation);
		endGP = new GeoPosition(endGP.getLatitude(), getSphericalLon(endGP.getLongitude()));
		
		GeodeticPosition geodaticP = computeGeod(startGP, endGP); //Distance in meters.
		double distInMiles = metricToMiles(geodaticP.getDist(), true); 
		
		int[] normalizedMetric = normalizeMetric(geodaticP.getDist());
		boolean isKm = (normalizedMetric[1] == 1);
		int dist = normalizedMetric[0];
		int[] normalizedImperial = normalizeImperial(distInMiles);
		boolean isMi = (normalizedImperial[1] == 1);
		int impDist = normalizedImperial[0];
				
		String metricCaption = dist + " " + ((isKm)? "km" : "m");
		String imperialCaption = impDist + " " + ((isMi)? "mi" : "ft");
		endGP = computeGeog(startGP, ((isKm)? dist * 1000 : dist), -90);
		GeoPosition endGPImp = computeGeog(startGP, imperialToMeters(impDist, !isMi), -90);
		Point2D metricTickLocation2D = getMainMap().convertGeoPositionToPoint(endGP);
		Point2D imperialTickLocation2D = getMainMap().convertGeoPositionToPoint(endGPImp);
		
		
		int mapWidth = getMainMap().getBounds().width;
		Point metricTickLocation = new Point((int)metricTickLocation2D.getX(),  (int)metricTickLocation2D.getY());
		Point imperialTickLocation = new Point((int)imperialTickLocation2D.getX(),  (int)imperialTickLocation2D.getY());
		if (metricTickLocation.getX() > mapWidth)
		{
			Point2D rightMostPt = getMainMap().getTileFactory().geoToPixel(new GeoPosition(0, 180), 
					getMainMap().getZoom());
	        int fullMapWidth = (int) rightMostPt.getX();
			int newX = metricTickLocation.x % fullMapWidth;
			metricTickLocation.setLocation(newX, metricTickLocation.y);
		}
		
		else if (metricTickLocation.getX() < 0)
		{
			Point2D rightMostPt = getMainMap().getTileFactory().geoToPixel(new GeoPosition(0, 180), 
					getMainMap().getZoom());
	        int fullMapWidth = (int) rightMostPt.getX();
			int newX = fullMapWidth - (Math.abs(metricTickLocation.x) % fullMapWidth);
			fullMapWidth = newX;
			metricTickLocation.setLocation(newX, metricTickLocation.y);
		}
		
		if (imperialTickLocation.getX() > mapWidth)
		{
			Point2D rightMostPt = getMainMap().getTileFactory().geoToPixel(new GeoPosition(0, 180), 
					getMainMap().getZoom());
	        int fullMapWidth = (int) rightMostPt.getX();
			int newX = imperialTickLocation.x % fullMapWidth;
			imperialTickLocation.setLocation(newX, imperialTickLocation.y);
		}
		
		else if (imperialTickLocation.getX() < 0)
		{
			Point2D rightMostPt = getMainMap().getTileFactory().geoToPixel(new GeoPosition(0, 180), 
					getMainMap().getZoom());
	        int fullMapWidth = (int) rightMostPt.getX();
			int newX = fullMapWidth - (Math.abs(imperialTickLocation.x) % fullMapWidth);
			fullMapWidth = newX;
			imperialTickLocation.setLocation(newX, imperialTickLocation.y);
		}
		
		scaleLine.adjust(metricCaption, metricTickLocation, imperialCaption, imperialTickLocation, scaleLineLocation.x);
	}
	
	public Mapper()
	{
		super();
		this.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
        this.setAddressLocationShown(false);
        this.setCenterPosition(new GeoPosition(39.64, -97.56));//Center the map on the U.S. by default.
        this.setZoom(defaultZoom);
        Dimension mapDim = new Dimension(800, 600);
        this.setPreferredSize(mapDim);
        this.setMinimumSize(mapDim);
        this.setMaximumSize(mapDim);
        
        scaleLine = new ScaleLine();
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 0;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
	    gridBagConstraints.weightx = 1.0;
	    gridBagConstraints.weighty = 1.0;
	    gridBagConstraints.insets = new java.awt.Insets(4, 50, 4, 4);
		getMainMap().add(scaleLine, gridBagConstraints);
		
		scaleLine.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				scaleLineLocation = scaleLine.getLocation();
				adjustScaleLine();
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
        
        greenPainter = null;
        redPainter = null;
        polyCursorPainter = null;
        uncertaintyHandlePainter = null;
    	polygonOverlayPainter = null;
    	uPolygonOverlayPainter = null;
        isGreenPtSelected = false;
        isUHandleSelected = false;
        mouseDragged = false;
        drawErrorPolygon = false;
        measureDistance = false;
        isEditUncertaintyHandlePersisted = false;
        errorRegion = null;
        uncertaintyRegion  = null;
        rulerSegments = null;
        uncertaintyRadius = 0;
        mostAccurateResultPt = null;
        resultPoints = new HashSet<LocalityWaypoint>();
        
        this.getMainMap().addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String propName = evt.getPropertyName().toLowerCase();
				
				if (!((propName.equals("mapoverlay")) || (propName.equals("panenabled"))))
				{
					if (propName.equals("centerposition"))
					{
						if (scaleLineLocation != null) {
							//redraw scale bar.
							adjustScaleLine();
						}
					}
					
					if ((!drawErrorPolygon) && propName.equals("centerposition"))
					{
						boolean frameChanged = frameChanged();
						if (frameChanged)
						{
			    			if (errorRegion != null)
			    			{
			                    List<GeoPosition> tempERegion = errorRegion;
			                    errorRegion = null;
			    				clearPolygonOverlay();
			    				errorRegion = tempERegion;
			    				drawPolygonOverlay(errorRegion);
			    			}
			    			
			    			if (uncertaintyRegion != null)
			    			{
			    				List<GeoPosition> tempURegion = uncertaintyRegion;
			    				uncertaintyRegion = null;
			    				clearUncertaintyOverlay();
			    				uncertaintyRegion = tempURegion;
			    				drawUncertaintyOverlay(uncertaintyRegion);
			    			}
			    			
			    			if (rulerSegments != null)
			    			{
			    				List<GeoPosition> tempRSegments = rulerSegments;
			    				rulerSegments = null;
			    				clearRulerOverlay();
			    				rulerSegments = tempRSegments;
			    				drawRulerOverlay(rulerSegments);
			    			}
						}
					}
				}
			}
		});

        this.getMainMap().addMouseMotionListener(new MouseInputAdapter() {
        	
        	@Override
            public void mouseMoved(MouseEvent me) {
        		JXMapViewer map = getMainMap();
        		Point pt = me.getPoint();
        		
        		GeoPosition gp = map.convertPointToGeoPosition(pt);
        		
        		//Change the cursor if the mouse is over a point of interest.
        		boolean isMouseOverGreen = isEventOnGreenPt(pt);
        		
        		Cursor cCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        		if (isMouseOverGreen || isEventOnUHandle(pt))
        			cCursor = new Cursor(Cursor.MOVE_CURSOR);
        		
        		else if (isEventOnRedPt(pt))
        			cCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
        		
        		if (drawErrorPolygon)
        		{
            		Waypoint newWPt = new Waypoint(gp.getLatitude(), getSphericalLon(gp.getLongitude()));
            		drawPolyCursor(newWPt);
            		
            		if ((errorRegion != null) && (errorRegion.size() > 0))
            		{
            			List<GeoPosition> dynamicRegion = new ArrayList<GeoPosition>();
            			dynamicRegion.addAll(errorRegion);
            			dynamicRegion.add(gp);
            			drawPolygonOverlay(dynamicRegion);
            		}
        		}
        		
        		if (measureDistance)
        		{
        			Waypoint newWPt = new Waypoint(gp.getLatitude(), getSphericalLon(gp.getLongitude()));
        			drawRulerCursor(newWPt);
        			
        			if ((rulerSegments != null) && (rulerSegments.size() > 0))
            		{
            			List<GeoPosition> dynamicSegments = new ArrayList<GeoPosition>();
            			dynamicSegments.addAll(rulerSegments);
            			dynamicSegments.add(gp);
            			drawRulerOverlay(dynamicSegments);
            		}
        		}
        		
    			map.setCursor(cCursor);
        		
        		double lat = decimalRound(gp.getLatitude(), 6);
        		double lon = decimalRound(getSphericalLon(gp.getLongitude()), 6);
        		//Fire map pointer move event.
        		GeoPosition MapPMELocation = new GeoPosition(lat, lon);
        		MapPointerMoveEvent MapPME = new MapPointerMoveEvent(map, MapPMELocation);
        		fireMapPointerMoveEvent(MapPME);
            }
        	
        	@Override
            public void mouseDragged(MouseEvent me) {
        		
        		if (isGreenPtSelected || isUHandleSelected)
        		{
        			JXMapViewer map = getMainMap();
            		Point pt = me.getPoint();
            		
            		GeoPosition gp = map.convertPointToGeoPosition(pt);
            		Waypoint newWPt = new Waypoint(gp.getLatitude(), getSphericalLon(gp.getLongitude()));
            		
            		if (isGreenPtSelected)
            		{
            			drawMostAccuratePt(newWPt);
            			mostAccurateResultPt.setPosition(newWPt.getPosition());
            		}
            		
            		else
            		{
            			moveEditUncertaintyHandleTo(newWPt);
            			
            			GeoPosition centerPos = mostAccurateResultPt.getPosition();
            	        GeodeticPosition geodetics = computeGeod(centerPos, newWPt.getPosition());
            	        long tmpUncertaintyRadius = Math.round(geodetics.getDist());
            	        
            			//Fire uncertainty circle resize event.
            			UncertaintyCircleChangeEvent uncertaintyCREvt = new UncertaintyCircleChangeEvent(getMainMap(), 
            					tmpUncertaintyRadius);
            			fireUncertaintyCircleChangeEvent(uncertaintyCREvt);
            		}
            		
            		mouseDragged = true;
            		
            		double lat = decimalRound(gp.getLatitude(), 6);
            		double lon = decimalRound(getSphericalLon(gp.getLongitude()), 6);
            		//Fire map pointer move event.
            		GeoPosition MapPMELocation = new GeoPosition(lat, lon);
            		MapPointerMoveEvent MapPME = new MapPointerMoveEvent(map, MapPMELocation);
            		fireMapPointerMoveEvent(MapPME);
        		}
            }
		});
        
        this.getMainMap().addMouseListener(new MouseInputAdapter() {
        	
        	@Override
        	public void mousePressed(MouseEvent me) {
        		Point pt = me.getPoint();
        		JXMapViewer map = getMainMap();
        		
        		//If the Green point or the uncertainty handle is selected, disable panning.
        		isGreenPtSelected = isEventOnGreenPt(pt);
        		isUHandleSelected = isEventOnUHandle(pt);
        		map.setPanEnabled(!(isGreenPtSelected || isUHandleSelected));
        		if (isUHandleSelected)
        		{
        			GeoPosition mousePos = map.convertPointToGeoPosition(pt);
        			handleOffsetX = getSphericalLon(handlePos.getLongitude()) - getSphericalLon(mousePos.getLongitude());
        			handleOffsetY = handlePos.getLatitude() - mousePos.getLatitude();
        		}
        	}
        	
        	@Override
        	public void mouseReleased(MouseEvent me) {
        		getMainMap().setPanEnabled(true);
        		if (isGreenPtSelected || isUHandleSelected)
        		{
        			getMainMap().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        			if (mouseDragged && isGreenPtSelected)
        			{
        				Point pt = me.getPoint();
                		JXMapViewer map = getMainMap();
                		GeoPosition gp = map.convertPointToGeoPosition(pt);
        				double lat = decimalRound(gp.getLatitude(), 6);
                		double lon = decimalRound(getSphericalLon(gp.getLongitude()), 6);
                		
                		//Redraw the uncertainty circle.
               	 		clearEditUncertaintyHandle();
               	 		uncertaintyRegion = getUncertaintyRegion(mostAccurateResultPt.getPosition(), uncertaintyRadius);
               	 		drawUncertaintyOverlay(uncertaintyRegion);
               	 		
        				//Fire most accurate point release event.
                		GeoPosition MapPMELocation = new GeoPosition(lat, lon);
                		MapPointerMoveEvent MapPME = new MapPointerMoveEvent(map, MapPMELocation);
                		fireMostAccuratePointReleaseEvent(MapPME);
                		
                		
        			}
        			
        			else if (isUHandleSelected)
        			{
        				handlePos = getMainMap().convertPointToGeoPosition(me.getPoint());
        				//Keep the distance between the tip of the marker and the actual mouse click position intact.
        				handlePos = new GeoPosition(handlePos.getLatitude() + handleOffsetY, 
        						getSphericalLon(handlePos.getLongitude() + handleOffsetX));
        				resizeUncertainty((LocalityWaypoint) mostAccurateResultPt.clone(), handlePos);
        			}
        			
        			if (isEditUncertaintyHandlePersisted)
           	 			drawEditUncertaintyHandle(mostAccurateResultPt.getPosition(), uncertaintyRadius, handleAz);
        			
        			mouseDragged = false;
        			isEditUncertaintyHandlePersisted = false;
        		}
        		
        		else {
        			//Potential panning, redraw scale bar.
        			adjustScaleLine();
        			
        			if (isEventOnRedPt(me.getPoint()))
        			getMainMap().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        		}
        		
        		isGreenPtSelected = false;
        		isUHandleSelected = false;
        	}

            @Override
            public void mouseClicked(MouseEvent me) {     
                // Get the screen point of mouse click.
                Point pt = me.getPoint();

                JXMapViewer map = getMainMap();
                
                // Get the pixel coordinates of the waypoint in question from the map.
                boolean greenClicked = isEventOnGreenPt(pt);
                boolean redClicked  = false;
                
                if (greenClicked && !drawErrorPolygon && !measureDistance)
                {
           	 		//Remove the uncertainty radius marker.
           	 		clearEditUncertaintyHandle();
           	 		fireUncertaintyCircleResizeCancelEvent();
                }
                
                else
                {
                	redClicked = isEventOnRedPt(pt);
                	if (redClicked && !drawErrorPolygon && !measureDistance)
                	{
                   	 	//Snap most accurate point to this one.
                		mostAccurateResultPt = getClickedWpt(pt);
                		drawMostAccuratePt(mostAccurateResultPt);
            			getMainMap().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            			
                		GeoPosition gp = mostAccurateResultPt.getPosition();
            			double snapLat = decimalRound(gp.getLatitude(), 6);
                		double snapLon = decimalRound(getSphericalLon(gp.getLongitude()), 6);
                		//Fire most accurate point snap event.
                		GeoPosition MapPMELocation = new GeoPosition(snapLat, snapLon);
                		MapPointerMoveEvent MapPME = new MapPointerMoveEvent(map, MapPMELocation);
                		fireMostAccuratePointSnapEvent(MapPME);
            			
            			//Remove the uncertainty radius marker.
               	 		clearEditUncertaintyHandle();
               	 		
               	 		//Draw the uncertainty circle at new location.
               	 		clearUncertaintyOverlay();
               	 		String uncertaintyStr = mostAccurateResultPt.getLocality().getUncertaintyMeters();
               	 		uncertaintyRadius = (uncertaintyStr.toLowerCase().equals("unavailable"))? 0 : 
            			Long.parseLong(uncertaintyStr);
               	 		uncertaintyRegion = getUncertaintyRegion(mostAccurateResultPt.getPosition(), uncertaintyRadius);
               	 		drawUncertaintyOverlay(uncertaintyRegion);
               	 		
               	 		//Draw error polygon at new location.
               	        clearPolygonOverlay();
               	        if (!mostAccurateResultPt.getLocality().getErrorPolygon().toLowerCase().equals("unavailable"))
               	        {
               	        	double lat = Double.NaN; 
               	        	double lon = Double.NaN;
               	        	String[] latLons =  mostAccurateResultPt.getLocality().getErrorPolygon().split(",");
               	        	for (int i=0; i<latLons.length; i++)
               	        	{
               	        		if ((i%2) == 0) //Latitude.
               	        			lat = Double.parseDouble(latLons[i]);
               	        		else //Longitude.
               	        		{
               	        			lon = Double.parseDouble(latLons[i]);
               	        			GeoPosition pos = new GeoPosition(lat, lon);
               	        			if (errorRegion == null)
               	        				errorRegion = new ArrayList<GeoPosition>();
               	        			errorRegion.add(pos);
               	        		}
               	        	}
               	        	drawPolygonOverlay(errorRegion);
               	        }
                	}
                	
                	else if (isEventOnUHandle(pt) && !drawErrorPolygon && !measureDistance)
                	{
               	 		//Remove the uncertainty radius marker.
               	 		clearEditUncertaintyHandle();
               	 		fireUncertaintyCircleResizeCancelEvent();
                	}
                }
                
                if (drawErrorPolygon)
                {
                	GeoPosition gp = map.convertPointToGeoPosition(pt);
                	if (errorRegion == null)
                        errorRegion = new ArrayList<GeoPosition>();
                	
                	GeoPosition vertex = new GeoPosition(decimalRound(gp.getLatitude(), 6), decimalRound(gp.getLongitude(), 6));
                	if (!errorRegion.contains(vertex))
                    errorRegion.add(vertex);
                }
                
                if (measureDistance)
                {
                	GeoPosition gp = map.convertPointToGeoPosition(pt);
                	if (rulerSegments == null)
                		rulerSegments = new ArrayList<GeoPosition>();
                	
                	GeoPosition vertex = new GeoPosition(decimalRound(gp.getLatitude(), 6), decimalRound(gp.getLongitude(), 6));
                	if (!rulerSegments.contains(vertex))
                		rulerSegments.add(vertex);
                }
                
                if (me.getClickCount() == 2) {
                	if (drawErrorPolygon || measureDistance)
                	{
                		if (drawErrorPolygon)
                    	{
                    		//In order to stay consistent with the polygon offset calculations, redraw the polygon after re-adjusting 
                            //the vertices' longitudes to spherical.
                    		List<GeoPosition> tempERegion = new ArrayList<GeoPosition>();
                    		String polyStr = "";
                            Iterator<GeoPosition> it = errorRegion.iterator();
                            while (it.hasNext()) {
                                GeoPosition gp = it.next();
                                double lat = gp.getLatitude();
                                double lon = decimalRound(getSphericalLon(gp.getLongitude()), 6);
                                polyStr += lat + "," + lon + ",";
                                tempERegion.add(new GeoPosition(lat, lon));
                            }
                            
                            polyStr = polyStr.substring(0, polyStr.length() - 1);
                            errorRegion = null;
            				clearPolygonOverlay();
            				clearPolyCursor();
            				drawErrorPolygon = false;
            				errorRegion = tempERegion;
            				mostAccurateResultPt.getLocality().setErrorPolygon(polyStr);
            				drawPolygonOverlay(errorRegion);
            				
            				//Fire error polygon draw event.
                    		ErrorPolygonDrawEvent errorPolyDrawEvt = new ErrorPolygonDrawEvent(map, tempERegion);
                    		fireErrorPolygonDrawEvent(errorPolyDrawEvt);
                    	}
                		
                		if (measureDistance)
                    	{
                    		List<GeoPosition> tempRSegments = new ArrayList<GeoPosition>();
                            Iterator<GeoPosition> it = rulerSegments.iterator();
                            while (it.hasNext()) {
                                GeoPosition gp = it.next();
                                double lat = gp.getLatitude();
                                double lon = decimalRound(getSphericalLon(gp.getLongitude()), 6);
                                tempRSegments.add(new GeoPosition(lat, lon));
                            }
                            
                            rulerSegments = null;
            				clearRulerOverlay();
            				clearRulerCursor();
            				measureDistance = false;
            				rulerSegments = tempRSegments;
            				drawRulerOverlay(rulerSegments);
            				rulerSegments = null;
            				
            				//Fire measure distance event.
                    		MeasureDistanceEvent MeasureDistEvt = new MeasureDistanceEvent(map, tempRSegments);
                    		fireMeasureDistanceEvent(MeasureDistEvt);
                    	}
                	}
                	
                	else
                	{
                		GeoPosition gp = map.convertPointToGeoPosition(pt);
                		map.setCenterPosition(gp);
                		map.setZoom(map.getZoom() - 1);
                	}
                	
                    return;
                    
                }

                if (me.getButton() == MouseEvent.BUTTON3) {
                	if (drawErrorPolygon || measureDistance)
                	{
                		if (drawErrorPolygon)
                    	{
                    		drawErrorPolygon = false;
        					errorRegion = null;
        					clearPolygonOverlay();
        					clearPolyCursor();
        					fireErrorPolygonDrawCancelEvent();
                    	}
                		
                		if (measureDistance)
                		{
                			measureDistance = false;
        					rulerSegments = null;
        					clearRulerOverlay();
        					clearRulerCursor();
        					fireMeasureDistanceCancelEvent();
                		}
                	}
                	
                	
                	else
                	{
                		GeoPosition gp = map.convertPointToGeoPosition(pt);
                		map.setCenterPosition(gp);
                		map.setZoom(map.getZoom() + 1);
                	}
                }
            }
        });
	}
	
	public void snapMostAccuratePointTo(GeoPosition newPos)
	{
		
		clearUncertaintyOverlay();
		clearPolygonOverlay();
		setCenterPosition(newPos);
		
		//Find which result point was clicked and assign that as the new most accurate point.
		Iterator<LocalityWaypoint> it = resultPoints.iterator();
    	LocalityWaypoint lWp = null;
        while (it.hasNext()) 
        {
        	lWp = it.next();
        	if (lWp.getPosition().equals(newPos))
        		break;
        	else
        		lWp = null;
        } 
        
        if (lWp != null)
        {
        	mostAccurateResultPt = (LocalityWaypoint) lWp.clone();
        	
        	//Draw error polygon at new location.
            clearPolygonOverlay();
            if (!mostAccurateResultPt.getLocality().getErrorPolygon().toLowerCase().equals("unavailable"))
            {
            	double lat = Double.NaN; 
            	double lon = Double.NaN;
            	String[] latLons =  mostAccurateResultPt.getLocality().getErrorPolygon().split(",");
            	for (int i=0; i<latLons.length; i++)
            	{
            		if ((i%2) == 0) //Latitude.
            			lat = Double.parseDouble(latLons[i]);
            		else //Longitude.
            		{
            			lon = Double.parseDouble(latLons[i]);
            			GeoPosition pos = new GeoPosition(lat, lon);
            			if (errorRegion == null)
            				errorRegion = new ArrayList<GeoPosition>();
            			errorRegion.add(pos);
            		}
            	}
            	drawPolygonOverlay(errorRegion);
            }
        }
        else
        {
        	mostAccurateResultPt.setPosition(newPos);
        	mostAccurateResultPt.getLocality().setPrecision("");
        	mostAccurateResultPt.getLocality().setScore(-1);
        	mostAccurateResultPt.getLocality().setMultipleResults("");
        	
        	//Draw old error polygon for compatibility.
            if (!mostAccurateResultPt.getLocality().getErrorPolygon().toLowerCase().equals("unavailable"))
            {
            	double lat = Double.NaN; 
            	double lon = Double.NaN;
            	String[] latLons =  mostAccurateResultPt.getLocality().getErrorPolygon().split(",");
            	for (int i=0; i<latLons.length; i++)
            	{
            		if ((i%2) == 0) //Latitude.
            			lat = Double.parseDouble(latLons[i]);
            		else //Longitude.
            		{
            			lon = Double.parseDouble(latLons[i]);
            			GeoPosition pos = new GeoPosition(lat, lon);
            			if (errorRegion == null)
            				errorRegion = new ArrayList<GeoPosition>();
            			errorRegion.add(pos);
            		}
            	}
            	drawPolygonOverlay(errorRegion);
            }
        }
        
		drawMostAccuratePt(new Waypoint(mostAccurateResultPt.getPosition()));
		
		
		//Draw uncertainty at new location.
		String uncertaintyStr = mostAccurateResultPt.getLocality().getUncertaintyMeters();
		uncertaintyRadius = (uncertaintyStr.toLowerCase().equals("unavailable"))? 0 : 
			Long.parseLong(uncertaintyStr);
		uncertaintyRegion = getUncertaintyRegion(mostAccurateResultPt.getPosition(), uncertaintyRadius);
        drawUncertaintyOverlay(uncertaintyRegion);
	}
	
	public void plotResultSet(LocalityWaypoint[] resultPoints, int mostAccurateIndex)
	{
		clearMostAccuratePt();
		clearPoints();
		
		List<Waypoint> resultWps = new ArrayList<Waypoint>();
		int index = (mostAccurateIndex < resultPoints.length)? mostAccurateIndex : 0;
		mostAccurateResultPt = resultPoints[index];
		//double minLat, maxLat, minLon, maxLon;
        //minLat = maxLat = minLon = maxLon = Double.NaN;
   	 	for (int i=0; i<resultPoints.length; i++)
   	 	{
   	 		LocalityWaypoint point = resultPoints[i];
   	 		this.resultPoints.add(point);
   	 		GeoPosition gp = point.getPosition();
   	 		resultWps.add(new Waypoint(gp));

   	 		//Determine the appropriate bounding box for the result set.
	   	 	/*if (Double.isNaN(minLat))
	        {
	            minLat = gp.getLatitude();
	            maxLat = gp.getLatitude();
	            minLon = gp.getLongitude();
	            maxLon = gp.getLongitude();
	        }
	            
	        else
	        {
	            if (gp.getLatitude() < minLat)
	            		minLat = gp.getLatitude();
	            if (gp.getLatitude() > maxLat)
	            	maxLat = gp.getLatitude();
	            if (gp.getLongitude() < minLon)
	            	minLon = gp.getLongitude();
	            if (gp.getLongitude() > maxLon)
	            	maxLon = gp.getLongitude();
	        }
	   	 	
	   	 	JXMapViewer map = getMainMap();
	   	 	Point2D topleft = map.getTileFactory().geoToPixel(new GeoPosition(minLat, minLon), map.getZoom());
	        Point2D botright = map.getTileFactory().geoToPixel(new GeoPosition(maxLat, maxLon), map.getZoom());
	        int x =(int)topleft.getX();
	        int y =(int)topleft.getY();
	        int width = (int)(botright.getX() - topleft.getX());
	        int height = (int)(botright.getY() - topleft.getY());
	   	 	setBounds(x - 50, y - 50, width + 50, height + 50);*/
   	 	}
   	 	
   	 	Waypoint mAWp = new Waypoint(mostAccurateResultPt.getPosition());
   	 	setCenterPosition(mAWp.getPosition());
   	 	setZoom(9);
   	 	drawPoints(resultWps);
		drawMostAccuratePt(mAWp);
		
		//Draw uncertainty.
		clearUncertaintyOverlay();
		String uncertaintyStr = mostAccurateResultPt.getLocality().getUncertaintyMeters();
		uncertaintyRadius = (uncertaintyStr.toLowerCase().equals("unavailable"))? 0 : 
			Long.parseLong(uncertaintyStr);
		uncertaintyRegion = getUncertaintyRegion(mostAccurateResultPt.getPosition(), uncertaintyRadius);
        drawUncertaintyOverlay(uncertaintyRegion);
        
        //Draw error polygon.
        clearPolygonOverlay();
        if (!mostAccurateResultPt.getLocality().getErrorPolygon().toLowerCase().equals("unavailable"))
        {
        	double lat = Double.NaN; 
        	double lon = Double.NaN;
        	String[] latLons =  mostAccurateResultPt.getLocality().getErrorPolygon().split(",");
        	for (int i=0; i<latLons.length; i++)
        	{
        		if ((i%2) == 0) //Latitude.
        			lat = Double.parseDouble(latLons[i]);
        		else //Longitude.
        		{
        			lon = Double.parseDouble(latLons[i]);
        			GeoPosition pos = new GeoPosition(lat, lon);
        			if (errorRegion == null)
        				errorRegion = new ArrayList<GeoPosition>();
        			errorRegion.add(pos);
        		}
        	}
        	drawPolygonOverlay(errorRegion);
        }
        
	}
	
	public void removePolygon()
	{
		clearPolyCursor();
		clearPolygonOverlay();
	}
	
	public void removeRuler()
	{
		clearRulerCursor();
		clearRulerOverlay();
	}
	
	public void drawPolygon(List<GeoPosition> vertices)
	{
		clearPolygonOverlay();
		errorRegion = new ArrayList<GeoPosition>();
		errorRegion.addAll(vertices);
		String polyStr = "";
		for (GeoPosition vertex : vertices)
		{
			polyStr += vertex.getLatitude() + "," + vertex.getLongitude() + ",";
		}
		
		if (polyStr.length() > 0)
		{
			polyStr = polyStr.substring(0, polyStr.length() - 1);
			mostAccurateResultPt.getLocality().setErrorPolygon(polyStr);
			drawPolygonOverlay(errorRegion);
		}
		
		else
			mostAccurateResultPt.getLocality().setErrorPolygon("Unavailable");
	}
	
	public void setMapSize(Dimension mapDimensions)
	{
		setPreferredSize(mapDimensions);
        setMinimumSize(mapDimensions);
        setMaximumSize(mapDimensions);
	}
	
	public void addMapPointerMoveListener(MapPointerMoveListener listener)
	{
		mapPointerMoveListeners.add(MapPointerMoveListener.class, listener);
	}
	
	public void removeMapPointerMoveListener(MapPointerMoveListener listener)
	{
		mapPointerMoveListeners.remove(MapPointerMoveListener.class, listener);
	}
	
	public void addMostAccuratePointReleaseListener(MostAccuratePointReleaseListener listener)
	{
		mostAccuratePointReleaseListeners.add(MostAccuratePointReleaseListener.class, listener);
	}
	
	public void removeMostAccuratePointReleaseListener(MostAccuratePointReleaseListener listener)
	{
		mostAccuratePointReleaseListeners.remove(MostAccuratePointReleaseListener.class, listener);
	}
	
	public void addMostAccuratePointSnapListener(MostAccuratePointSnapListener listener)
	{
		mostAccuratePointSnapListeners.add(MostAccuratePointSnapListener.class, listener);
	}
	
	public void removeMostAccuratePointSnapListener(MostAccuratePointSnapListener listener)
	{
		mostAccuratePointSnapListeners.remove(MostAccuratePointSnapListener.class, listener);
	}
	
	public void addErrorPolygonDrawListener(ErrorPolygonDrawListener listener)
	{
		errorPolygonDrawListeners.add(ErrorPolygonDrawListener.class, listener);
	}
	
	public void removeErrorPolygonDrawListener(ErrorPolygonDrawListener listener)
	{
		errorPolygonDrawListeners.remove(ErrorPolygonDrawListener.class, listener);
	}
	
	public void addErrorPolygonDrawCancelListener(ErrorPolygonDrawCancelListener listener)
	{
		errorPolygonDrawCancelListeners.add(ErrorPolygonDrawCancelListener.class, listener);
	}
	
	public void removeErrorPolygonDrawCancelListener(ErrorPolygonDrawCancelListener listener)
	{
		errorPolygonDrawCancelListeners.remove(ErrorPolygonDrawCancelListener.class, listener);
	}
	
	public void addMeasureDistanceListener(MeasureDistanceListener listener)
	{
		measureDistanceListeners.add(MeasureDistanceListener.class, listener);
	}
	
	public void removeMeasureDistanceListener(MeasureDistanceListener listener)
	{
		measureDistanceListeners.remove(MeasureDistanceListener.class, listener);
	}
	
	public void addMeasureDistanceCancelListener(MeasureDistanceCancelListener listener)
	{
		measureDistanceCancelListeners.add(MeasureDistanceCancelListener.class, listener);
	}
	
	public void removeMeasureDistanceCancelListener(MeasureDistanceCancelListener listener)
	{
		measureDistanceCancelListeners.remove(MeasureDistanceCancelListener.class, listener);
	}
	
	public void addUncertaintyCircleResizeListener(UncertaintyCircleResizeListener listener)
	{
		uncertaintyCircleResizeListeners.add(UncertaintyCircleResizeListener.class, listener);
	}
	
	public void removeUncertaintyCircleResizeListener(UncertaintyCircleResizeListener listener)
	{
		uncertaintyCircleResizeListeners.remove(UncertaintyCircleResizeListener.class, listener);
	}
	
	public void addUncertaintyCircleResizeCancelListener(UncertaintyCircleResizeCancelListener listener)
	{
		uncertaintyCircleResizeCancelListeners.add(UncertaintyCircleResizeCancelListener.class, listener);
	}
	
	public void removeUncertaintyCircleResizeCancelListener(UncertaintyCircleResizeCancelListener listener)
	{
		uncertaintyCircleResizeCancelListeners.remove(UncertaintyCircleResizeCancelListener.class, listener);
	}
	
	public void addUncertaintyCircleChangeListener(UncertaintyCircleChangeListener listener)
	{
		uncertaintyCircleChangeListeners.add(UncertaintyCircleChangeListener.class, listener);
	}
	
	public void removeUncertaintyCircleChangeListener(UncertaintyCircleChangeListener listener)
	{
		uncertaintyCircleChangeListeners.remove(UncertaintyCircleChangeListener.class, listener);
	}
	
	private void fireMapPointerMoveEvent(MapPointerMoveEvent evt)
	{
		Object[] listeners = mapPointerMoveListeners.getListenerList();
		
		for (int i=0; i<listeners.length; i++)
		{
			if (listeners[i] == MapPointerMoveListener.class)
				((MapPointerMoveListener)listeners[i+1]).mapPointerMoved(evt);
		}
	}
	
	private void fireMostAccuratePointReleaseEvent(MapPointerMoveEvent evt)
	{
		Object[] listeners = mostAccuratePointReleaseListeners.getListenerList();
		
		for (int i=0; i<listeners.length; i++)
		{
			if (listeners[i] == MostAccuratePointReleaseListener.class)
				((MostAccuratePointReleaseListener)listeners[i+1]).mostAccuratePointReleased(evt);
		}
	}
	
	private void fireMostAccuratePointSnapEvent(MapPointerMoveEvent evt)
	{
		Object[] listeners = mostAccuratePointSnapListeners.getListenerList();
		
		for (int i=0; i<listeners.length; i++)
		{
			if (listeners[i] == MostAccuratePointSnapListener.class)
				((MostAccuratePointSnapListener)listeners[i+1]).mostAccuratePointSnapped(evt);
		}
	}
	
	private void fireErrorPolygonDrawEvent(ErrorPolygonDrawEvent evt)
	{
		Object[] listeners = errorPolygonDrawListeners.getListenerList();
		
		for (int i=0; i<listeners.length; i++)
		{
			if (listeners[i] == ErrorPolygonDrawListener.class)
				((ErrorPolygonDrawListener)listeners[i+1]).errorPolygonDrawn(evt);
		}
	}
	
	private void fireErrorPolygonDrawCancelEvent()
	{
		Object[] listeners = errorPolygonDrawCancelListeners.getListenerList();
		
		for (int i=0; i<listeners.length; i++)
		{
			if (listeners[i] == ErrorPolygonDrawCancelListener.class)
				((ErrorPolygonDrawCancelListener)listeners[i+1]).errorPolygonDrawCancelled();
		}
	}
	
	private void fireMeasureDistanceEvent(MeasureDistanceEvent evt)
	{
		Object[] listeners = measureDistanceListeners.getListenerList();
		
		for (int i=0; i<listeners.length; i++)
		{
			if (listeners[i] == MeasureDistanceListener.class)
				((MeasureDistanceListener)listeners[i+1]).distanceMeasured(evt);
		}
	}
	
	private void fireMeasureDistanceCancelEvent()
	{
		Object[] listeners = measureDistanceCancelListeners.getListenerList();
		
		for (int i=0; i<listeners.length; i++)
		{
			if (listeners[i] == MeasureDistanceCancelListener.class)
				((MeasureDistanceCancelListener)listeners[i+1]).measureDistanceCancelled();
		}
	}
	
	private void fireUncertaintyCircleResizeEvent(UncertaintyCircleResizeEvent evt)
	{
		Object[] listeners = uncertaintyCircleResizeListeners.getListenerList();
		
		for (int i=0; i<listeners.length; i++)
		{
			if (listeners[i] == UncertaintyCircleResizeListener.class)
				((UncertaintyCircleResizeListener)listeners[i+1]).uncertaintyCircleResized(evt);
		}
	}
	
	private void fireUncertaintyCircleResizeCancelEvent()
	{
		Object[] listeners = uncertaintyCircleResizeCancelListeners.getListenerList();
		
		for (int i=0; i<listeners.length; i++)
		{
			if (listeners[i] == UncertaintyCircleResizeCancelListener.class)
				((UncertaintyCircleResizeCancelListener)listeners[i+1]).uncertaintyCircleResizeCancelled();
		}
	}
	
	private void fireUncertaintyCircleChangeEvent(UncertaintyCircleChangeEvent evt)
	{
		Object[] listeners = uncertaintyCircleChangeListeners.getListenerList();
		
		for (int i=0; i<listeners.length; i++)
		{
			if (listeners[i] == UncertaintyCircleChangeListener.class)
				((UncertaintyCircleChangeListener)listeners[i+1]).uncertaintyCircleChanged(evt);
		}
	}

	public Set<LocalityWaypoint> getResultPoints() {
		return resultPoints;
	}

	public LocalityWaypoint getMostAccurateResultPt() {
		return mostAccurateResultPt;
	}
	
	private boolean frameChanged()
	{
		boolean frameChanged = false;
		JXMapViewer map = getMainMap();
		Rectangle bounds = map.getViewportBounds();
		Point2D topLeft = bounds.getLocation();
		Point2D rightMostPt = map.getTileFactory().geoToPixel(new GeoPosition(0, 180), map.getZoom());
		
        int fullMapWidth = (int) rightMostPt.getX();
        double x = topLeft.getX();
        //Count how many times we've scrolled the full length of the map.
        int frameCount = (int) Math.floor(x/fullMapWidth);
        
        //Check to see if we have changed frames.
        if (frameCount != previousFrameCount)
        {
        	frameChanged = true;
        	
        	if (frameCount < previousFrameCount)
        		shiftRegion = true;
        	else
        		shiftRegion = false;
        	previousFrameCount = frameCount;
        }
        
        else
        	frameChanged = false;
        
        return frameChanged;
	}
	
	private Painter<JXMapViewer> getRulerPainter(final  List<GeoPosition> segments)
	{
		Painter<JXMapViewer> segmentOverlay = new Painter<JXMapViewer>() {
            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
            	/**************************Determine if the polygon needs to be offset to the current view port**************************/
        		double minLat, maxLat, minLon, maxLon;
                minLat = maxLat = minLon = maxLon = Double.NaN;
                Iterator<GeoPosition> it = segments.iterator();
                while (it.hasNext()) {
                    GeoPosition gp = it.next();
                    if (Double.isNaN(minLat))
                    {
                        minLat = gp.getLatitude();
                        maxLat = gp.getLatitude();
                        minLon = gp.getLongitude();
                        maxLon = gp.getLongitude();
                    }
                        
                    else
                    {
                        if (gp.getLatitude() < minLat)
                        		minLat = gp.getLatitude();
                        if (gp.getLatitude() > maxLat)
                        	maxLat = gp.getLatitude();
                        if (gp.getLongitude() < minLon)
                        	minLon = gp.getLongitude();
                        if (gp.getLongitude() > maxLon)
                        	maxLon = gp.getLongitude();
                    }
                }
                    
                Rectangle bounds = map.getViewportBounds();
                int xOffset = 0;
                    
                Point2D topleft = map.getTileFactory().geoToPixel(new GeoPosition(minLat, minLon), map.getZoom());
                Point2D botright = map.getTileFactory().geoToPixel(new GeoPosition(maxLat, maxLon), map.getZoom());
                if ((!bounds.contains(topleft) && !bounds.contains(botright)) || shiftRegion)
                {
                    Point2D rightMostPt = map.getTileFactory().geoToPixel(new GeoPosition(0, 180), map.getZoom());
                    double fullMapWidth = rightMostPt.getX();
                        
                    it = segments.iterator();
                    if (it.hasNext())
                    {
                        //Offset x by how many times we've scrolled the full length of the map.
                        xOffset = (int) (fullMapWidth * previousFrameCount);
                        double minBoundX = (bounds.getX() - Math.floor(bounds.getX()/fullMapWidth) * fullMapWidth);
                        double maxBoundX = minBoundX + bounds.getWidth();
                            	
                        if ((minBoundX < botright.getX()) && (botright.getX() < maxBoundX))
                        {
                            xOffset = (int) (Math.floor(bounds.getX()/fullMapWidth) * fullMapWidth);
                        }
                        
                        if (shiftRegion)
                        	shiftRegion = false;
                    }
                }
        		/***********************************************************************************************************/
                
                g = (Graphics2D) g.create();
                //convert from viewport to world bitmap
                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x + xOffset, -rect.y);
                //create a polyline.
                int[] xPoints = new int[segments.size()];
                int[] yPoints = new int[segments.size()];
                
                double dist = 0;
                GeoPosition prevVertex = null;
                GeoPosition currentVertex = null;
                int count = 0;
                for (GeoPosition gp : segments) {
                    //convert geo to world bitmap pixel
                    Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                    xPoints[count] = (int) pt.getX();
                    yPoints[count] = (int) pt.getY();
                    count++;
                    
                    if (prevVertex == null)
                    {
                    	prevVertex = gp;
                    	currentVertex = gp;
                    }
                    
                    else
                    {
                    	currentVertex = gp;
                    	GeodeticPosition geodP = computeGeod(prevVertex, currentVertex);
                    	dist += geodP.getDist();
                    	prevVertex = currentVertex;
                    }
                }
                
                double impDist = metricToMiles(dist, true);
                boolean isKm = false;
                boolean isMi = true;
                
                if (dist > 1000)
                {
                	dist /= 1000;
                	isKm = true;
                }
                
                if (impDist < 1)
                {
                	impDist = milesToFeet(impDist);
                	isMi = false;
                }
                
                dist = decimalRound(dist, 2);
                impDist = decimalRound(impDist, 2);
                String distanceCaption = dist + " " + (isKm? "km" : "m") + " (" + impDist + " "	+ (isMi? "mi" : "ft") + 
                		")";
                
              //Do the drawing.
            	g.setColor(Color.RED);
            	float dash[] = { 8.0f, 3.0f };
                
            	g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
            	        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));

            	g.drawPolyline(xPoints, yPoints, segments.size());
            	
            	g.setColor(Color.BLUE);
            	g.setFont(new Font("Arial", Font.BOLD, 11));
            	Rectangle2D labelBounds = g.getFontMetrics().getStringBounds(distanceCaption, g);
                int strOffsetX = ((int)labelBounds.getWidth()/2);
            	g.drawString(distanceCaption, xPoints[segments.size()-1] - ptStrokeOffsetX/2 - strOffsetX, 
            			yPoints[segments.size()-1] + ptStrokeOffsetY/2 - 1);
            }
		};
		
		return segmentOverlay;
	}
	
	private Painter<JXMapViewer> getPolygonPainter(final List<GeoPosition> region, final boolean isUncertaintyCircle)
	{
		Painter<JXMapViewer> polygonOverlay = new Painter<JXMapViewer>() {
            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
            	/**************************Determine if the polygon needs to be offset to the current view port**************************/
        		double minLat, maxLat, minLon, maxLon;
                minLat = maxLat = minLon = maxLon = Double.NaN;
                Iterator<GeoPosition> it = region.iterator();
                while (it.hasNext()) {
                    GeoPosition gp = it.next();
                    if (Double.isNaN(minLat))
                    {
                        minLat = gp.getLatitude();
                        maxLat = gp.getLatitude();
                        minLon = gp.getLongitude();
                        maxLon = gp.getLongitude();
                    }
                        
                    else
                    {
                        if (gp.getLatitude() < minLat)
                        		minLat = gp.getLatitude();
                        if (gp.getLatitude() > maxLat)
                        	maxLat = gp.getLatitude();
                        if (gp.getLongitude() < minLon)
                        	minLon = gp.getLongitude();
                        if (gp.getLongitude() > maxLon)
                        	maxLon = gp.getLongitude();
                    }
                }
                    
                Rectangle bounds = map.getViewportBounds();
                int xOffset = 0;
                    
                Point2D topleft = map.getTileFactory().geoToPixel(new GeoPosition(minLat, minLon), map.getZoom());
                Point2D botright = map.getTileFactory().geoToPixel(new GeoPosition(maxLat, maxLon), map.getZoom());
                if ((!bounds.contains(topleft) && !bounds.contains(botright)) || shiftRegion)
                {
                    Point2D rightMostPt = map.getTileFactory().geoToPixel(new GeoPosition(0, 180), map.getZoom());
                    double fullMapWidth = rightMostPt.getX();
                        
                    it = region.iterator();
                    if (it.hasNext())
                    {
                        //Offset x by how many times we've scrolled the full length of the map.
                        xOffset = (int) (fullMapWidth * previousFrameCount);
                        double minBoundX = (bounds.getX() - Math.floor(bounds.getX()/fullMapWidth) * fullMapWidth);
                        double maxBoundX = minBoundX + bounds.getWidth();
                            	
                        if ((minBoundX < botright.getX()) && (botright.getX() < maxBoundX))
                        {
                            xOffset = (int) (Math.floor(bounds.getX()/fullMapWidth) * fullMapWidth);
                        }
                        
                        if (shiftRegion)
                        	shiftRegion = false;
                    }
                }
        		/***********************************************************************************************************/
                
                g = (Graphics2D) g.create();
                //convert from viewport to world bitmap
                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x + xOffset, -rect.y);
                //create a polygon
                Polygon poly = new Polygon();
                for (GeoPosition gp : region) {
                    //convert geo to world bitmap pixel
                    Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                    poly.addPoint((int) pt.getX(), (int) pt.getY());
                }

                //Do the drawing. different styling for the uncertainty circle.
                if (isUncertaintyCircle)
                {
                	g.setColor(new Color(119, 119, 119, 100));
                	g.fill(poly);
                	g.setColor(Color.BLACK);
                	g.draw(poly);
                }
                
                else
                {
                	g.setColor(new Color(255, 0, 0, 100));
                	g.fill(poly);
                	g.setColor(Color.RED);
                	g.draw(poly);
                }

                g.dispose();
            }
		};
		
		return polygonOverlay;
	}
	
	public void editUncertaintyCircle(long uncertaintyRadiusInMeters)
	{
		uncertaintyRadius = uncertaintyRadiusInMeters;
		clearUncertaintyOverlay();
		mostAccurateResultPt.getLocality().setUncertaintyMeters(Long.toString(uncertaintyRadius));
		uncertaintyRegion = getUncertaintyRegion(mostAccurateResultPt.getPosition(), uncertaintyRadius);
        drawUncertaintyOverlay(uncertaintyRegion);
	}
	
	public void showEditPolygonHandle()
	{
		drawErrorPolygon = true;
		if (measureDistance)
		{
			clearRulerCursor();
			clearRulerOverlay();
			measureDistance = false;
		}
	}
	
	public void hideEditPolygonHandle()
	{
		clearPolyCursor();
		drawErrorPolygon = false;
	}
	
	public void showMeasureDistanceHandle()
	{
		measureDistance = true;
	}
	
	public void hideMeasureDistanceHandle()
	{
		clearRulerCursor();
		measureDistance = false;
	}
	
	public void hideEditUncertaintyHandle()
	{
		clearEditUncertaintyHandle();
		getMainMap().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void showEditUncertaintyHandle()
	{
		clearEditUncertaintyHandle();
		uncertaintyRadius = Long.parseLong(mostAccurateResultPt.getLocality().getUncertaintyMeters());
		drawEditUncertaintyHandle(mostAccurateResultPt.getPosition(), uncertaintyRadius, uRotationAngleDeg);
	}
	
	public void persistEditUncertaintyHandle()
	{
		clearEditUncertaintyHandle();
		uncertaintyRadius = Long.parseLong(mostAccurateResultPt.getLocality().getUncertaintyMeters());
		
		if (handlePos != null)
			isEditUncertaintyHandlePersisted = true;
	}
	
	private void resizeUncertainty(LocalityWaypoint centerWaypt, GeoPosition radialPos)
    {
		GeoPosition centerPos = centerWaypt.getPosition();
        GeodeticPosition geodetics = computeGeod(centerPos, radialPos);
        uncertaintyRadius = Math.round(geodetics.getDist());
		handleAz = -1 * geodetics.getFBearing();
        mostAccurateResultPt.getLocality().setUncertaintyMeters(Long.toString(uncertaintyRadius));
        uncertaintyRegion = getUncertaintyRegion(centerPos, uncertaintyRadius);
        drawUncertaintyOverlay(uncertaintyRegion);
        
		//Fire uncertainty circle resize event.
		UncertaintyCircleResizeEvent uncertaintyCREvt = new UncertaintyCircleResizeEvent(getMainMap(), centerWaypt);
		fireUncertaintyCircleResizeEvent(uncertaintyCREvt);
    }
	
	private List<GeoPosition> getUncertaintyRegion(GeoPosition centerPos, long radiusInMeters)
	{
		List<GeoPosition> region = new ArrayList<GeoPosition>();
    	int numVertices = 360; //A polygon representation of a circle looks good enough with 40 vertices.
    	int bearingIncrement = 1; //360 degrees divided by number of vertices.
    	for(int i = 1; i <= numVertices; i++)
    	{
    		GeoPosition gp = computeGeog(centerPos, radiusInMeters, (i *  bearingIncrement));
            region.add(gp);
    	}
    	
    	return region;
	}
	
	private void setOverlayPainters()
	{
		JXMapViewer map = this.getMainMap();
		
		if (greenPainter != null)
			map.setOverlayPainter(greenPainter);
		
		if (redPainter != null)
		{
			if (map.getOverlayPainter() == null)
				map.setOverlayPainter(redPainter);
			else
			{
				CompoundPainter<JXMapViewer> cpaint = new CompoundPainter<JXMapViewer>();
				cpaint.setPainters(redPainter, map.getOverlayPainter());
		        cpaint.setCacheable(false);
		        map.setOverlayPainter(cpaint);
			}
		}
		
		if (uncertaintyHandlePainter != null)
		{
			if (map.getOverlayPainter() == null)
				map.setOverlayPainter(uncertaintyHandlePainter);
			else
			{
				CompoundPainter<JXMapViewer> cpaint = new CompoundPainter<JXMapViewer>();
				cpaint.setPainters(uncertaintyHandlePainter, map.getOverlayPainter());
		        cpaint.setCacheable(false);
		        map.setOverlayPainter(cpaint);
			}
		}

		if (uPolygonOverlayPainter != null)
		{
			if (map.getOverlayPainter() == null)
				map.setOverlayPainter(uPolygonOverlayPainter);
			else
			{
				CompoundPainter<JXMapViewer> cpaint = new CompoundPainter<JXMapViewer>();
				cpaint.setPainters(uPolygonOverlayPainter, map.getOverlayPainter());
		        cpaint.setCacheable(false);
		        map.setOverlayPainter(cpaint);
			}
		}
		
		if (polyCursorPainter != null)
		{
			if (map.getOverlayPainter() == null)
				map.setOverlayPainter(polyCursorPainter);
			else
			{
				CompoundPainter<JXMapViewer> cpaint = new CompoundPainter<JXMapViewer>();
				cpaint.setPainters(polyCursorPainter, map.getOverlayPainter());
		        cpaint.setCacheable(false);
		        map.setOverlayPainter(cpaint);
			}
		}
		
		if (polygonOverlayPainter != null)
		{
			if (map.getOverlayPainter() == null)
				map.setOverlayPainter(polygonOverlayPainter);
			else
			{
				CompoundPainter<JXMapViewer> cpaint = new CompoundPainter<JXMapViewer>();
				cpaint.setPainters(polygonOverlayPainter, map.getOverlayPainter());
		        cpaint.setCacheable(false);
		        map.setOverlayPainter(cpaint);
			}
		}
		
		if (rulerCursorPainter != null)
		{
			if (map.getOverlayPainter() == null)
				map.setOverlayPainter(rulerCursorPainter);
			else
			{
				CompoundPainter<JXMapViewer> cpaint = new CompoundPainter<JXMapViewer>();
				cpaint.setPainters(rulerCursorPainter, map.getOverlayPainter());
		        cpaint.setCacheable(false);
		        map.setOverlayPainter(cpaint);
			}
		}
		
		if (rulerOverlayPainter != null)
		{
			if (map.getOverlayPainter() == null)
				map.setOverlayPainter(rulerOverlayPainter);
			else
			{
				CompoundPainter<JXMapViewer> cpaint = new CompoundPainter<JXMapViewer>();
				cpaint.setPainters(rulerOverlayPainter, map.getOverlayPainter());
		        cpaint.setCacheable(false);
		        map.setOverlayPainter(cpaint);
			}
		}
	}
	
	private void clearUncertaintyOverlay()
	{
		uncertaintyRegion = null;
		uPolygonOverlayPainter = null;
		setOverlayPainters();
	}
    
    private void drawUncertaintyOverlay(List<GeoPosition> region)
    {
    	uPolygonOverlayPainter = getPolygonPainter(region, true);
    	setOverlayPainters();
    }
    
    private void clearPolygonOverlay()
    {
    	errorRegion = null;
    	polygonOverlayPainter = null;
    	setOverlayPainters();
    }
	
	private void drawPolygonOverlay(List<GeoPosition> region) {
		polygonOverlayPainter = getPolygonPainter(region, false);
		setOverlayPainters(); 
	}
	
	private void clearRulerOverlay()
    {
		rulerSegments = null;
    	rulerOverlayPainter = null;
    	setOverlayPainters();
    }
	
	private void drawRulerOverlay(List<GeoPosition> segments) {
		rulerOverlayPainter = getRulerPainter(segments);
		setOverlayPainters();
	}
	
	private void clearPolyCursor()
	{
		polyCursorPainter = null;
		setOverlayPainters();
	}
	
	
	private void drawPolyCursor(Waypoint polyWPt)
	{
		polyCursorPainter = new WaypointPainter<JXMapViewer>();
        Set<Waypoint> waypoints = new HashSet<Waypoint>();
        waypoints.add(polyWPt);
        polyCursorPainter.setWaypoints(waypoints);
        polyCursorPainter.setRenderer(new WaypointRenderer() {
            public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
            	g.setColor(Color.BLUE); //Outline.
            	g.drawOval(ptStrokeOffsetX, ptStrokeOffsetY, ptStrokeWidth, ptStrokeHeight);
            	g.drawOval(ptFillOffsetX, ptFillOffsetY, ptFillWidth, ptFillHeight);
            	g.fillOval(ptFillOffsetX+4, ptFillOffsetY+4, ptFillWidth-7, ptFillHeight-7);
                return true;
            }
        });
        
        setOverlayPainters();
	}
	
	private void clearRulerCursor()
	{
		rulerCursorPainter = null;
		setOverlayPainters();
	}
	
	private void drawRulerCursor(Waypoint rulerWPt)
	{
		rulerCursorPainter = new WaypointPainter<JXMapViewer>();
        Set<Waypoint> waypoints = new HashSet<Waypoint>();
        waypoints.add(rulerWPt);
        rulerCursorPainter.setWaypoints(waypoints);
        rulerCursorPainter.setRenderer(new WaypointRenderer() {
            public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
            	g.setColor(Color.BLUE); //Outline.
            	g.fillRect(ptStrokeOffsetX/2, ptStrokeOffsetY/2, ptStrokeWidth/2, ptStrokeHeight/2);
                return true;
            }
        });
        
        setOverlayPainters();
	}
	
	private void clearEditUncertaintyHandle()
	{
		uncertaintyHandlePainter = null;
		setOverlayPainters();
	}
	
	private void drawEditUncertaintyHandle(GeoPosition centerPos, long radiusInMeters, double markerBearing)
	{
		handlePos = computeGeog(centerPos, radiusInMeters, markerBearing);
		uncertaintyHandlePainter = new WaypointPainter<JXMapViewer>();
		Set<Waypoint> waypoints = new HashSet<Waypoint>();
		waypoints.add(new Waypoint(handlePos));
		uncertaintyHandlePainter.setWaypoints(waypoints);
		uncertaintyHandlePainter.setRenderer(new WaypointRenderer() {
			public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
				
				//create a polygon representing the shape of the handle.
				g.setColor(Color.BLUE);
                g.rotate(uRotationAngleRad);
                g.setStroke(new BasicStroke(uHandleStrokeWidth));
                g.drawPolygon(xArray, yArray,uHandleNPoints);
				return true;
			}
		});
		
		setOverlayPainters();
	}
	
	private void moveEditUncertaintyHandleTo(Waypoint wPt)
	{
		uncertaintyHandlePainter = new WaypointPainter<JXMapViewer>();
		Set<Waypoint> waypoints = new HashSet<Waypoint>();
		//Keep the distance between the tip of the marker and the actual mouse click position intact.
		wPt.setPosition(new GeoPosition(wPt.getPosition().getLatitude() + handleOffsetY, wPt.getPosition().getLongitude() + 
				handleOffsetX));
		waypoints.add(wPt);
		uncertaintyHandlePainter.setWaypoints(waypoints);
		uncertaintyHandlePainter.setRenderer(new WaypointRenderer() {
			public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
				
				//create a polygon representing the shape of the handle.
				g.setColor(Color.BLUE);
                g.rotate(uRotationAngleRad);
                g.setStroke(new BasicStroke(uHandleStrokeWidth));
                g.drawPolygon(xArray, yArray,uHandleNPoints);
				return true;
			}
		});
		
		setOverlayPainters();
	}
	
	public void clearMostAccuratePt()
	{
		mostAccurateResultPt = null;
		greenPainter = null;
		setOverlayPainters();
	}

	public void drawMostAccuratePt(Waypoint wayPt) {
        greenPainter = new WaypointPainter<JXMapViewer>();
        Set<Waypoint> waypoints = new HashSet<Waypoint>();
        waypoints.add(wayPt);
        greenPainter.setWaypoints(waypoints);
        greenPainter.setRenderer(new WaypointRenderer() {
            public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
            	g.setColor(new Color(50, 50, 50)); //Outline.
                g.fillOval(ptStrokeOffsetX, ptStrokeOffsetY, ptStrokeWidth, ptStrokeHeight);
                g.setColor(new Color(10, 200, 20)); //Green point.
                g.fillOval(ptFillOffsetX, ptFillOffsetY, ptFillWidth, ptFillHeight);
                return true;
            }
        });
        
        setOverlayPainters();
	}
	
	public void clearPoints()
	{
		resultPoints = new HashSet<LocalityWaypoint>();
		redPainter = null;
		setOverlayPainters();
	}
	
	public void drawPoints(final List<Waypoint> waypoints) {
        redPainter = new WaypointPainter<JXMapViewer>();
        Set<Waypoint> waypointSet = new HashSet<Waypoint>(waypoints);
        redPainter.setWaypoints(waypointSet);
        redPainter.setRenderer(new WaypointRenderer() {
            public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
            	g.setColor(new Color(50, 50, 50)); //Outline.
                g.fillOval(ptStrokeOffsetX, ptStrokeOffsetY, ptStrokeWidth, ptStrokeHeight);
                g.setColor(new Color(200, 10, 20)); //Red point.
                g.fillOval(ptFillOffsetX, ptFillOffsetY, ptFillWidth, ptFillHeight);
                
                //Draw index marker.
                int index = waypoints.indexOf(wp) + 1;
                
                g.setPaint(new Color(0, 0, 255, 180));
                Polygon pin = new Polygon();
                
                pin.addPoint(ptStrokeOffsetX - 12, ptStrokeOffsetY - 9);
                pin.addPoint(ptStrokeOffsetX + 2, ptStrokeOffsetY + 2);
                pin.addPoint(ptStrokeOffsetX - 9, ptStrokeOffsetY - 12);
                g.fill(pin);
                Rectangle2D labelBounds = g.getFontMetrics().getStringBounds(String.valueOf(index), g);
                int lenOffset = String.valueOf(index).length();
                lenOffset = ((lenOffset - 1)  < 2)? 1 : lenOffset;
                int strOffsetX = ((int) labelBounds.getWidth()/2) - lenOffset;
                int strOffsetY = ((int) labelBounds.getHeight()/2) - lenOffset;
                int diam = (int) (Math.max(labelBounds.getHeight(), labelBounds.getWidth()));
                g.fillOval(ptStrokeOffsetX - diam - 7, ptStrokeOffsetY - diam - 7, diam + 2 , diam + 2);
                //draw text w/ shadow
                g.setPaint(Color.BLACK); 
                g.drawString(String.valueOf(index), ptStrokeOffsetX - strOffsetX - diam, ptStrokeOffsetY - strOffsetY - diam + 11); //shadow
                g.drawString(String.valueOf(index), ptStrokeOffsetX - strOffsetX - diam, ptStrokeOffsetY - strOffsetY - diam + 11); //shadow
                g.setPaint(Color.WHITE);
                g.drawString(String.valueOf(index), ptStrokeOffsetX - strOffsetX - diam, ptStrokeOffsetY - strOffsetY - diam + 12); //text
                return true;
            }
        });
        
        setOverlayPainters();
	}
	
	public double getSphericalLon(double linearLon)
	{
		//Normalize linear longitude output from JXMapKit into spherical system.
		double t = Double.NaN;
		double radLon = Double.NaN;
		double cosT = Double.NaN;
		double normLon = Double.NaN;
		double cosSign = Double.NaN;
		double sinSign = Double.NaN;
		double sphericalLon = linearLon;
		
		//Re-project longitude range from [-180, 180] to [0, 360].
		if ((linearLon < -180) || (linearLon > 180))
		{
			if (linearLon < -180)
    		{
    			normLon = linearLon + (int)(Math.ceil(Math.abs(linearLon)/(double)360) * 360);
    		}
    		else if (linearLon > 180)
    		{
    			normLon = linearLon - (int)(Math.ceil(linearLon/(double)360) * 360);
    		}
			
			if (normLon < 0)
				normLon += 360;
			
			radLon = linearLon * Math.PI/(double)180; //Convert to radians.
			cosSign = -1 * Math.signum(Math.cos(radLon));
			sinSign = Math.signum(Math.sin(radLon));
			
			cosT = cosSign * Math.sqrt(1 - Math.pow(Math.sin(radLon), 2));
			t = Math.acos(cosT);
			sphericalLon = sinSign * (180 - (t * 180/Math.PI)); //Convert calculated angle theta back to decimal degrees.
		}
		
		return sphericalLon;
	}
	
	public double decimalRound(double input, int decimalPlace)
	{
		BigDecimal bd = new BigDecimal(Double.toString(input));
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}
	
	private boolean isEventOnGreenPt(Point pt)
	{
		boolean isOnPt = false;
		if (greenPainter != null)
        {
			JXMapViewer map = this.getMainMap();
        	Iterator<Waypoint> points = greenPainter.getWaypoints().iterator();
       	 	while (points.hasNext()) {
                Waypoint wp = points.next();
                Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());

                // Adjust the pixel coordinates to their relative position on screen.
                Rectangle bounds = map.getViewportBounds();
                Point2D rightMostPt = map.getTileFactory().geoToPixel(new GeoPosition(0, 180), map.getZoom());
                double fullMapWidth = rightMostPt.getX();
                int x = (int) (point.getX() - bounds.getX());
                int y = (int) (point.getY() - bounds.getY());
                //Offset x by how many times we've scrolled the full length of the map.
                x = (int) (x - Math.floor(x/fullMapWidth) * fullMapWidth);

                // Create a bounding rectangle around the waypoint, and see if the mouse event occured
                // within its boundaries.
                Rectangle rect = new Rectangle(x + ptStrokeOffsetX, y + ptStrokeOffsetY, ptStrokeWidth, ptStrokeHeight);
                
                if (rect.contains(pt))
                {
                	isOnPt = true;
               	 	break;
                }
       	 	}
        }
		return isOnPt;
	}
	
	private boolean isEventOnRedPt(Point pt)
	{
		boolean isOnPt = false;
		if (redPainter != null)
        {
			JXMapViewer map = this.getMainMap();
        	Iterator<Waypoint> points = redPainter.getWaypoints().iterator();
       	 	while (points.hasNext()) {
                Waypoint wp = points.next();

                Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());

                // Adjust the pixel coordinates to their relative position on screen.

                Rectangle bounds = map.getViewportBounds();
                Point2D rightMostPt = map.getTileFactory().geoToPixel(new GeoPosition(0, 180), map.getZoom());
                double fullMapWidth = rightMostPt.getX();
                int x = (int) (point.getX() - bounds.getX());
                int y = (int) (point.getY() - bounds.getY());
                //Offset x by how many times we've scrolled the full length of the map.
                x = (int) (x - Math.floor(x/fullMapWidth) * fullMapWidth);

                // Create a bounding rectangle around the waypoint, and see if the mouse event occured
                // within its boundaries.

                Rectangle rect = new Rectangle(x + ptStrokeOffsetX, y + ptStrokeOffsetY, ptStrokeWidth, ptStrokeHeight);
                if (rect.contains(pt))
                {
                	isOnPt = true;
               	 	break;
                }
       	 	}
        }
		return isOnPt;
	}
	
	private boolean isEventOnUHandle(Point pt)
	{
		boolean isOnPt = false;
		if (uncertaintyHandlePainter != null)
        {
			JXMapViewer map = this.getMainMap();
        	Iterator<Waypoint> points = uncertaintyHandlePainter.getWaypoints().iterator();
       	 	while (points.hasNext()) {
                Waypoint wp = points.next();

                Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());

                // Adjust the pixel coordinates to their relative position on screen.

                Rectangle bounds = map.getViewportBounds();
                Point2D rightMostPt = map.getTileFactory().geoToPixel(new GeoPosition(0, 180), map.getZoom());
                double fullMapWidth = rightMostPt.getX();
                int x = (int) (point.getX() - bounds.getX());
                int y = (int) (point.getY() - bounds.getY());
                //Offset x by how many times we've scrolled the full length of the map.
                x = (int) (x - Math.floor(x/fullMapWidth) * fullMapWidth);

                //See if the mouse event occured within the handle's boundaries.
                //Rotate the boundaries by 45 counter clockwise degrees by applying trigonometry laws of the unit circle.
                int[] xComps = new int[uHandleNPoints];
                int[] yComps = new int[uHandleNPoints];
                for (int i=0; i<uHandleNPoints; i++)
                {
                	int xComp = xArray[i];
                	int yComp = yArray[i];
                	xComps[i] = (int) (xComp*Math.cos(-uRotationAngleRad) + yComp*Math.sin(-uRotationAngleRad));
                	yComps[i] = (int) (-xComp*Math.sin(-uRotationAngleRad) + yComp*Math.cos(-uRotationAngleRad));
                }
                
                Polygon poly = new Polygon(xComps, yComps, uHandleNPoints);
                poly.translate(x, y);
                
                if (poly.contains(pt))
                {
                	isOnPt = true;
               	 	break;
                }
       	 	}
        }
		return isOnPt;
	}
	
	private LocalityWaypoint getClickedWpt(Point pt)
	{
		LocalityWaypoint clickedWpt = null;
		if ((redPainter != null))
        {
			JXMapViewer map = this.getMainMap();
        	Iterator<Waypoint> points = redPainter.getWaypoints().iterator();
       	 	while (points.hasNext()) {
       	 		Waypoint wp = points.next();

                Point2D point = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());

                // Adjust the pixel coordinates to their relative position on screen.
                Rectangle bounds = map.getViewportBounds();
                Point2D rightMostPt = map.getTileFactory().geoToPixel(new GeoPosition(0, 180), map.getZoom());
                double fullMapWidth = rightMostPt.getX();
                int x = (int) (point.getX() - bounds.getX());
                int y = (int) (point.getY() - bounds.getY());
                //Offset x by how many times we've scrolled the full length of the map.
                x = (int) (x - Math.floor(x/fullMapWidth) * fullMapWidth);

                // Create a bounding rectangle around the waypoint, and see if the mouse click occured
                // within its boundaries.

                Rectangle rect = new Rectangle(x + ptStrokeOffsetX, y + ptStrokeOffsetY, ptStrokeWidth, ptStrokeHeight);
                if (rect.contains(pt))
                {
                	Iterator<LocalityWaypoint> it = resultPoints.iterator();
                	LocalityWaypoint lWp = null;
                    while (it.hasNext()) 
                    {
                    	lWp = it.next();
                    	if (lWp.getPosition().equals(wp.getPosition()))
                    	{
                    		clickedWpt = (LocalityWaypoint) lWp.clone();
                    		break;
                    	}
                    } 
               	 	break;
                }
       	 	}
        }
		return clickedWpt;
	}
	
	/*************************************************Uncertainty circle stuff***************************************************/  
	private GeoPosition computeGeog(GeoPosition startingPos, double distInMeters, double fAz)
    {
        double geoFact = Math.PI / 180; //Degrees to radians conversion factor.
        double iGeoFact = 180 / Math.PI; //inverse geoFact.
        int nmCvFact = 1852; //Meters to nautical miles conversion factor.
        //WGS84 ellipsoid has radius a = 6,378,137 meters at the equator and flattening f = 1/298.257223563.
        Ellipsoid ellipse = new Ellipsoid("WGS84", (6378137 / nmCvFact), 298.257223563);
        
        double gLat = geoFact * startingPos.getLatitude();
        double gLon = geoFact * startingPos.getLongitude();
        double nmDist = distInMeters / nmCvFact;
        double gFAz = geoFact * fAz;
        
        //Spherical (Haversine) method.
        /*GeographicPosOutput out = getGPosHav(gLat, gLon, gFAz, distInMeters);
        double newLat = out.getgLat() * iGeoFact;  
        double newLon = out.getgLon() * iGeoFact;*/
    
        //Elliptical (Vincenty) method.
        GeographicPosOutput out = getGPos(gLat, -gLon, gFAz, nmDist, ellipse);  // ellipse uses East negative.
        double newLat = out.getGLat() * iGeoFact;  
        double newLon = -out.getGLon() * iGeoFact;                  // ellipse uses East negative.
        
        
        GeoPosition gp = new GeoPosition(newLat, newLon);
        return gp;
    }
    
    private GeodeticPosition computeGeod(GeoPosition centerPos, GeoPosition radialPos)
    {
        double geoFact = Math.PI / 180; //Degrees to radians conversion factor.
        double iGeoFact = 180 / Math.PI; //inverse geoFact.
        double nmCvFact = 1852; //Meters to nautical miles conversion factor.
        //WGS84 ellipsoid has radius a = 6,378,137 meters at the equator and flattening f = 1/298.257223563.
        Ellipsoid ellipse = new Ellipsoid("WGS84", (6378137 / nmCvFact), 298.257223563);
        
        double gCLat, gCLon, gRLat, gRLon;

        gCLat = geoFact * centerPos.getLatitude();
        gRLat = geoFact * radialPos.getLatitude();
        gCLon = geoFact * centerPos.getLongitude();
        gRLon = geoFact * radialPos.getLongitude();
        
        GeodeticDistOutput out = getGDist(gCLat, gCLon, gRLat, gRLon, ellipse);  // ellipse uses East negative.
        double faz = out.getFaz() * iGeoFact;  
        double baz = out.getBaz() * iGeoFact;
        double dist = out.getNmDist() * nmCvFact;  // go to meters.
        GeodeticPosition geodetics = new GeodeticPosition(dist, faz, baz);
        return geodetics;
    }
	
	//Calculates the geographic position of a point B (forward) relative to a point A using the geodetic distance and 
	// azimuth (bearing) between them, according to "Haversine's Formula" for Great-Circles.
    @SuppressWarnings("unused")
	private GeographicPosOutput getGPosHav(double glat1, double glon1, double faz, double s)
    {
        final int R = 6371000; //Meters.
        double lat = Math.asin( Math.sin(glat1)*Math.cos(s/R) + Math.cos(glat1)*Math.sin(s/R)*Math.cos(faz) );
        double lon = glon1 + Math.atan2(Math.sin(faz)*Math.sin(s/R)*Math.cos(glat1), Math.cos(s/R)-Math.sin(glat1)*Math.sin(lat));
        GeographicPosOutput out = new GeographicPosOutput(lat, lon, faz);
        return out;
    }
    
	//Calculates the geodetic distance and azimuth between a center point A and a radial point B
    private GeodeticDistOutput getGDist(double glat1, double glon1, double glat2, double glon2, Ellipsoid ellipse)
    {
        // glat1 initial geodetic latitude in radians N positive 
        // glon1 initial geodetic longitude in radians E positive 
        // glat2 final geodetic latitude in radians N positive 
        // glon2 final geodetic longitude in radians E positive 
        double a = ellipse.getRadius();
        double f = 1/ellipse.getFlattening();
        GeodeticDistOutput out = new GeodeticDistOutput(0, 0, Math.PI);
        
        double r, tu1, tu2, cu1, su1, cu2, s1, b1, f1;
        double x, sx, cx, sy, cy,y, sa, c2a, cz, e, c, d;
        double EPS= 0.00000000005;
        double faz, baz, s;
        int iter = 1;
        int MAXITER = 100;
        
        if ((glat1 + glat2 == 0.) && (Math.abs(glon1 - glon2) == Math.PI)){
        	System.out.println("Course and distance between antipodal points is undefined");
            glat1 = glat1 + 0.00001; // allow algorithm to complete
        }
        
        if (glat1==glat2 && (glon1==glon2 || Math.abs(Math.abs(glon1-glon2)-2*Math.PI) <  EPS)){
        	System.out.println("Points 1 and 2 are identical - course undefined");
            return out;
        }
          
        r = 1 - f;
        tu1 = r * Math.tan (glat1);
        tu2 = r * Math.tan (glat2);
        cu1 = 1. / Math.sqrt (1. + tu1 * tu1);
        su1 = cu1 * tu1;
        cu2 = 1. / Math.sqrt (1. + tu2 * tu2);
        s1 = cu1 * cu2;
        b1 = s1 * tu2;
        f1 = b1 * tu1;
        x = glon2 - glon1;
        d = x + 1; // force one pass
        
        sx = cx = c2a = e = cy = sy = cz = y = Double.NaN;
        while ((Math.abs(d - x) > EPS) && (iter < MAXITER))
        {
        	iter = iter + 1;
        	sx = Math.sin (x);
              
            cx = Math.cos (x);
            tu1 = cu2 * sx;
            tu2 = b1 - su1 * cu2 * cx;
            sy = Math.sqrt(tu1 * tu1 + tu2 * tu2);
            cy = s1 * cx + f1;
            y = atan2(sy, cy);
            sa = s1 * sx / sy;
            c2a = 1 - sa * sa;
            cz = f1 + f1;
            
            if (c2a > 0.)
            	cz = cy - cz / c2a;
            
            e = cz * cz * 2. - 1.;
            c = ((-3. * c2a + 4.) * f + 4.) * c2a * f / 16.;
            d = x;
            x = ((e * cy * c + cz) * sy * c + y) * sa;
            x = (1. - c) * x * f + glon2 - glon1;
        }
        
        faz = modcrs(atan2(tu1, tu2));
        baz = modcrs(atan2(cu1 * sx, b1 * cx - su1 * cu2) + Math.PI);
        x = Math.sqrt ((1 / (r * r) - 1) * c2a + 1);
        x += 1;
        x = (x - 2.) / x;
        c = 1. - x;
        c = (x * x / 4. + 1.) / c;
        d = (0.375 * x * x - 1.) * x;
        x = e * cy;
        s = ((((sy*sy*4.-3.)*(1.-e-e)*cz*d/6.-x)*d/4.+cz)*sy*d+y)*c*a*r;
          
        out = new GeodeticDistOutput(s, faz, baz);
        
        if (Math.abs(iter-MAXITER)<EPS){
        	System.out.println("Algorithm did not converge");
        }
        
        return out;
    }
    
    private GeographicPosOutput getGPos(double glat1, double glon1, double faz, double s, Ellipsoid ellipse)
    {
    	// glat1 initial geodetic latitude in radians N positive 
        // glon1 initial geodetic longitude in radians E positive 
        // faz forward azimuth in radians
        // s distance in units of a (=nm)

        double EPS= 0.00000000005;
        double r, tu, sf, cf, b, cu, su, sa, c2a, x, c, d, y, sy, cy, cz, e;
        double glat2, glon2, baz, f;

        if ((Math.abs(Math.cos(glat1))<EPS) && !(Math.abs(Math.sin(faz))<EPS)){
        	System.out.println("Only N-S courses are meaningful, starting at a pole!");
        }

        double a = ellipse.getRadius();
        f = 1/ellipse.getFlattening();
        r = 1 - f;
        tu = r * Math.tan (glat1);
        sf = Math.sin (faz);
        cf = Math.cos (faz);
          
        if (cf == 0)
        	b = 0.;
        else
        	b = 2. * atan2(tu, cf);
        
        cu = 1. / Math.sqrt (1 + tu * tu);
        su = tu * cu;
        sa = cu * sf;
        c2a = 1 - sa * sa;
        x = 1. + Math.sqrt (1. + c2a * (1. / (r * r) - 1.));
        x = (x - 2.) / x;
        c = 1. - x;
        c = (x * x / 4. + 1.) / c;
        d = (0.375 * x * x - 1.) * x;
        tu = s / (r * a * c);
        y = tu;
        c = y + 1;
          
        cy = sy = e = cz = Double.NaN;
        while (Math.abs (y - c) > EPS)
        {
        	sy = Math.sin (y);
            cy = Math.cos (y);
            cz = Math.cos (b + y);
            e = 2. * cz * cz - 1.;
            c = y;
            x = e * cy;
            y = e + e - 1.;
            y = (((sy * sy * 4. - 3.) * y * cz * d / 6. + x) *  d / 4. - cz) * sy * d + tu;
        }

        b = cu * cy * cf - su * sy;
        c = r * Math.sqrt (sa * sa + b * b);
        d = su * cy + cu * sy * cf;
        glat2 = modlat(atan2 (d, c));
        c = cu * cy - su * sy * cf;
        x = atan2 (sy * sf, c);
        c = ((-3. * c2a + 4.) * f + 4.) * c2a * f / 16.;
        d = ((e * cy * c + cz) * sy * c + y) * sa;
        glon2 = modlon(glon1 + x - (1. - c) * d * f);	// fix date line problems 
        baz = modcrs(atan2 (sa, b) + Math.PI);

          GeographicPosOutput out = new GeographicPosOutput(glat2, glon2, baz);
          return out;
    }
    
    private double modcrs(double x){
        return mod(x, 2*Math.PI);
    }
	
    private double mod(double x, double y){
        return x-y*Math.floor(x/y);
    }
    
    private double modlat(double x){
        return mod(x + Math.PI/2, 2*Math.PI)-Math.PI/2;
    }
    
    private double modlon(double x){
        return mod(x + Math.PI, 2*Math.PI)-Math.PI;
    }
    
    private double atan2(double y , double x){
        double out = Double.NaN;
        if (x < 0)
        	out = Math.atan(y/x) + Math.PI;
        
        if ((x > 0) && (y >= 0))
        	out = Math.atan(y/x);
          
        if ((x > 0) && (y < 0))
        	out = Math.atan(y/x)+2*Math.PI;
          
        if ((x == 0) && (y > 0))
        	out = Math.PI/2;
          
        if ((x == 0) && (y < 0))
        	out= 3*Math.PI/2;
          
        if ((x==0) && (y==0))
        	out= 0.;
          
      return out;
    }
	/****************************************************************************************************************************/
}
