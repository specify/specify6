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
package edu.ku.brc.specify.ui;

import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.util.Pair;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.examples.GazetteerPanel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 25, 2009
 *
 */
public class WorldWindPanel extends JPanel
{
    protected AnnotationLayer            annoLayer;
    protected MarkerLayer                markerLayer = new MarkerLayer();
    protected RenderableLayer            lineLayer   = new RenderableLayer();
    protected WorldWindowGLCanvas        world = null;
    protected StatusBar                  statusBar;
    
    protected ArrayList<Marker>          markers     = new ArrayList<Marker>();
    protected ArrayList<GlobeAnnotation> annotations = new ArrayList<GlobeAnnotation>();
    
    protected AnnotationAttributes       annoAttrs   = new AnnotationAttributes();
    protected BasicMarkerAttributes      markerAttrs;
    

    /**
     * @throws HeadlessException
     */
    public WorldWindPanel() throws HeadlessException
    {
        super();
        
        //Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
        //Configuration.setValue(AVKey.VIEW_CLASS_NAME, FlatOrbitView.class.getName());
        
        init();
    }
    
    /**
     * 
     */
    protected void init()
    {
        if (world == null)
        {
            world = new WorldWindowGLCanvas();
            
            int radius = 3;
            annoAttrs.setCornerRadius(radius);
            annoAttrs.setInsets(new Insets(4, 4, 4, 4));
            annoAttrs.setBackgroundColor(new Color(0f, 0f, 0f, 0.5f));
            annoAttrs.setDrawOffset(new Point(0, radius+3));
            annoAttrs.setDistanceMinScale(0.5);
            annoAttrs.setDistanceMaxScale(2);
            annoAttrs.setDistanceMinOpacity(0.5);
            annoAttrs.setLeader(FrameFactory.LEADER_NONE);
            annoAttrs.setFont(Font.decode("Arial-BOLD-10"));
            annoAttrs.setTextColor(Color.WHITE);
            annoAttrs.setBorderColor(new Color(0.75f, 0.75f, 0.75f, 0.75f));
            
            Material material = new Material(new Color(1f, 0.1f, 0.1f, 0.75f));
            markerAttrs = new BasicMarkerAttributes(material, BasicMarkerShape.ORIENTED_SPHERE, 1d, radius, radius);
            
            markerLayer.setOverrideMarkerElevation(true);
            markerLayer.setKeepSeparated(false);
            markerLayer.setElevation(0);
            
            /*world.addRenderingExceptionListener(new RenderingExceptionListener()
            {
                public void exceptionThrown(Throwable t)
                {
                    if (t instanceof WWAbsentRequirementException)
                    {
                        String message = "Computer does not meet minimum graphics requirements.\n";
                        message += "Please install up-to-date graphics driver and try again.\n";
                        message += "Reason: " + t.getMessage() + "\n";
                        message += "This program will end when you press OK.";

                        JOptionPane.showMessageDialog(null, message, "Unable to Start Program",
                            JOptionPane.ERROR_MESSAGE);
                        System.exit(-1);
                    }
                }
            });*/
            
            initWorldWindLayerModel();
        }
    }

    /*
     * Initialize WW model with default layers
     */
    protected void initWorldWindLayerModel() 
    {
       Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
       world.setModel(m);
       
       LayerList layers = world.getModel().getLayers();
       for (Object layer : layers)
       {
           if (layer instanceof AnnotationLayer)
           {
               annoLayer = (AnnotationLayer) layer;
               break;
           }
       }
       
       if (annoLayer == null)
       {
           annoLayer = new AnnotationLayer();
       }

       insertBeforePlacenames(world, markerLayer);
       insertBeforePlacenames(world, annoLayer);
       //insertBeforePlacenames(world, lineLayer);
       world.getModel().getLayers().add(this.lineLayer);
       
       setLayout(new BorderLayout());
       
       statusBar = new StatusBar();
       add(statusBar, BorderLayout.SOUTH);
       statusBar.setEventSource(world);
       
       add(world, BorderLayout.CENTER);
       try
        {
           add(new GazetteerPanel(world, null), BorderLayout.NORTH);
           
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
            
        } catch (InstantiationException e)
        {
            e.printStackTrace();
            
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Adds a polyline to the polyline line layer.
     * @param polyline the polyline
     */
    public void addPolyline(final Polyline polyline)
    {
        lineLayer.addRenderable(polyline);
    }
    
    /**
     * Places marker points on map.
     * @param points the list of points
     * @param flyToIndex
     */
    public void placeMarkers(final List<LatLonPlacemarkIFace> points, final Integer flyToIndex)
    {
        placeMarkers(points, false, true, flyToIndex);
    }
    
    /**
     * Clears the marker and annotations layers and creates a new Polyline layer.
     */
    public void reset()
    {
        init(); // does this the first time only
        
        annoLayer.clearList();
        annoLayer.removeAllAnnotations();
        markerLayer.clearList();
        lineLayer.clearList();
        
        world.getModel().getLayers().remove(lineLayer);
        world.getModel().getLayers().add(lineLayer = new RenderableLayer());
        
        markers.clear();
        annotations.clear();

    }
    
    /**
     * Places marker points on map.
     * @param points the list of points
     * @param doAddTitles
     * @param doAddMarkCntText
     * @param flyToIndex
     */
    public void placeMarkers(final List<LatLonPlacemarkIFace> points, 
                             final boolean doAddTitles,
                             final boolean doAddMarkCntText, 
                             final Integer flyToIndex)
    {
        reset();
        
        int i = 1;
        for (LatLonPlacemarkIFace pnt : points)
        {
            Pair<Double, Double> latLon = pnt.getLatLon();
            if (latLon != null && latLon.first != null && latLon.second != null)
            {
                Position pos = Position.fromDegrees(latLon.first, latLon.second, 0);
                
                Marker marker = new BasicMarker(pos, markerAttrs);
                marker.setPosition(Position.fromDegrees(latLon.first, latLon.second, 0));
                markers.add(marker);
                
                if (doAddTitles || doAddMarkCntText)
                {
                    String txt = doAddMarkCntText ? Integer.toString(i++) : (pnt.getTitle() != null ? pnt.getTitle() : "");
                    GlobeAnnotation ga = new GlobeAnnotation(txt, pos, annoAttrs);
                    if (txt.length() > 0)
                    {
                        annoLayer.addAnnotation(ga);
                    }
                    annotations.add(ga);
                }
            }
        }
        
        markerLayer.setMarkers(markers);
        
        if (flyToIndex != null && flyToIndex > -1 && flyToIndex < points.size())
        {
            Pair<Double, Double> latLon = points.get(0).getLatLon();
            if (latLon != null && latLon.first != null && latLon.second != null)
            {
                flyTo(LatLon.fromDegrees(latLon.first, latLon.second));
            }
        }
    }
    
    /**
     * @param markerIndex
     */
    public void flyToMarker(final int markerIndex)
    {
        if (markerIndex > -1 && markerIndex < markers.size())
        {
            Marker marker = markers.get(markerIndex);
            flyTo(marker.getPosition().getLatLon());
        }
    }

    /**
     * @return the statusBar
     */
    public StatusBar getStatusBar()
    {
        return statusBar;
    }

    /**
     * @param statusBar the statusBar to set
     */
    public void setStatusBar(StatusBar statusBar)
    {
        this.statusBar = statusBar;
    }

    /**
     * @return the annoLayer
     */
    public AnnotationLayer getAnnoLayer()
    {
        return annoLayer;
    }

    /**
     * @return the markerLayer
     */
    public MarkerLayer getMarkerLayer()
    {
        return markerLayer;
    }
    
    /**
     * @return the annoAttrs (not a copy), can be used to alter the current attributes for the annotations.
     */
    public AnnotationAttributes getAnnoAttrs()
    {
        return annoAttrs;
    }

    /**
     * @return the markerAttrs (not a copy), can be used to alter the current attributes for the markers.
     */
    public BasicMarkerAttributes getMarkerAttrs()
    {
        return markerAttrs;
    }

    /**
     * @return the world
     */
    public WorldWindowGLCanvas getWorld()
    {
        return world;
    }

    /**
     * @return the markers
     */
    public ArrayList<Marker> getMarkers()
    {
        return markers;
    }

    /**
     * @return the annotations
     */
    public ArrayList<GlobeAnnotation> getAnnotations()
    {
        return annotations;
    }
    
    /**
     * Shuts down the world.
     */
    public void shutdown()
    {
        //world.shutdown();
    }

    /**
     * @param wwd
     * @param layer
     */
    public static void insertBeforeCompass(final WorldWindow wwd, final Layer layer)
    {
        // Insert the layer into the layer list just before the compass.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof CompassLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, layer);
    }

    public static void insertBeforePlacenames(final WorldWindow wwd, final Layer layer)
    {
        // Insert the layer into the layer list just before the placenames.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof PlaceNameLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, layer);
    }

    /**
     * @param wwd
     * @param layer
     */
    public static void insertAfterPlacenames(final WorldWindow wwd, final Layer layer)
    {
        // Insert the layer into the layer list just after the placenames.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof PlaceNameLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition + 1, layer);
    }

    public static void insertBeforeLayerName(final WorldWindow wwd, final Layer layer, final String targetName)
    {
        // Insert the layer into the layer list just before the target layer.
        int targetPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l.getName().indexOf(targetName) != -1)
            {
                targetPosition = layers.indexOf(l);
                break;
            }
        }
        layers.add(targetPosition, layer);
    }
    
    /**
     * @param latlon
     */
    public void flyTo (final LatLon latlon) 
    {
       Position pos = new Position(latlon.latitude, latlon.longitude, 3e3);
       View view       = world.getView();
       Globe globe = world.getModel().getGlobe();
   
       view.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
                                       (OrbitView)view
                                       , globe
                                       , pos          // bbox
                                       , Angle.ZERO   // Heading
                                       , Angle.ZERO   // Pitch
                                       , 20000.0 )        // Altitude/Zoom (m)
                                       );
    }

}