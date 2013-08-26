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
package edu.ku.brc.util.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import edu.ku.brc.util.Pair;

/**
 * This class generates KML output representing the provided geolocations
 * and HTML descriptions.  For a more detailed description of KML, see
 * <a href="http://code.google.com/apis/kml/documentation/index.html">the Google KML reference</a>.
 * 
 * @author jstewart
 * 
 * @code_status Complete
 */
public class GenericKMLGenerator
{
    /** Logger used to emit any messages from this class. */
    //private static final Logger log = Logger.getLogger(GenericKmlGenerator.class);

    /** Keyhole Markup Language namespace declaration. */
    protected static String KML_NAMESPACE_DECL = "http://earth.google.com/kml/2.1";

    /** A list of all points to be included in the generated KML. */
    protected List<Pair<Double, Double>> points = new Vector<Pair<Double,Double>>();

    /** A mapping from a geo reference point to the name of the placemark. */
    protected Map<Pair<Double,Double>, String> pointNameMap = new Hashtable<Pair<Double,Double>, String>();
    
    /** A mapping from a geo reference point to the HTML text description for that point. */
    protected Map<Pair<Double,Double>, String> pointDescMap = new Hashtable<Pair<Double,Double>, String>();
    
    /** A URL to an image file to be used as the placemark icon. */
    protected String placemarkIconURL;
    
    /** The background color of the placemark balloons. */
    protected String balloonStyleBgColor;
    
    /** The text color for the placemark balloons. */
    protected String balloonStyleTextColor;
    
    /** The format description for the placemark balloons. */
    protected String balloonStyleText;
    
    /** The description of the KML/KMZ File */
    protected String description;
    
    /**
     * Constructs an instance.
     */
    public GenericKMLGenerator()
    {
        // nothing needed here
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Adds the given point, name and HTML text to be included in the generated KML output.
     * 
     * @param point the geo reference point
     * @param name the name of the placemark
     * @param htmlDescription the text description (as HTML)
     */
    public void addPlacemark(final Pair<Double,Double> point, 
                             final String name, 
                             final String htmlDescription)
    {
        //log.debug("Adding point to KML generator: " + point + "   " + htmlDescription);

        if (point == null)
        {
            return;
        }
        
        points.add(point);
        pointNameMap.put(point, name == null ? "" : name);
        pointDescMap.put(point, htmlDescription);
    }
    
    /**
     * @param descritpion the descritpion to set
     */
    public void setDescritpion(String descritpion)
    {
        this.description = descritpion;
    }

    /**
     * Returns the URL to the placemark icon.
     * 
     * @return the URL to the icon image
     */
    public String getPlacemarkIconURL()
    {
        return placemarkIconURL;
    }

    /**
     * Sets the URL to the placemark icon.
     * 
     * @param placemarkIconURL the URL to the icon image
     */
    public void setPlacemarkIconURL(String placemarkIconURL)
    {
        this.placemarkIconURL = placemarkIconURL;
    }

    /**
     * Gets the the balloon background color.
     * 
     * @return the balloon background color
     */
    public String getBalloonStyleBgColor()
    {
        return balloonStyleBgColor;
    }

    /**
     * Sets the balloon background color.
     * 
     * @param the balloon background color
     */
    public void setBalloonStyleBgColor(String balloonStyleBgColor)
    {
        this.balloonStyleBgColor = balloonStyleBgColor;
    }

    /**
     * Gets the balloon text format description.
     * 
     * @return the balloon text format description
     */
    public String getBalloonStyleText()
    {
        return balloonStyleText;
    }

    /**
     * Sets the balloon text format description.
     * 
     * @param the balloon text format description
     */
    public void setBalloonStyleText(String balloonStyleText)
    {
        this.balloonStyleText = balloonStyleText;
    }

    /**
     * Gets the balloon text color.
     * 
     * @return the balloon text color
     */
    public String getBalloonStyleTextColor()
    {
        return balloonStyleTextColor;
    }

    /**
     * Sets the balloon text color.
     * 
     * @param the balloon text color
     */
    public void setBalloonStyleTextColor(String balloonStyleTextColor)
    {
        this.balloonStyleTextColor = balloonStyleTextColor;
    }
    
    /**
     * @param style 
     * @param iconURL
     * @return
     */
    public static void generateIconCode(Element style, final String iconURL)
    {
        if (StringUtils.isNotEmpty(iconURL))
        {
            style.addElement("IconStyle")
            .addElement("Icon")
            .addElement("href")
            .addText(iconURL);
        }
    }
    
    /**
     * @param style 
     * @param balloonStyleBgColor
     * @param balloonStyleTextColor
     * @param balloonStyleText
     * @return
     */
    public static void generateBalloon(Element style, final String balloonStyleBgColor, 
                                         final String balloonStyleTextColor, 
                                         final String balloonStyleText)
    {
        if (balloonStyleBgColor != null || balloonStyleTextColor != null || balloonStyleText != null)
        {
            Element balloonStyle = style.addElement("BalloonStyle");
            
            if (balloonStyleBgColor != null)
            {
                balloonStyle.addElement("bgColor").addText(balloonStyleBgColor);
            }
            
            // GoogleEarth 5.0 on Windows and Linux causes the color to color the entire test area
            /*if (balloonStyleTextColor != null)
            {
                sb.append("<textColor>" + balloonStyleTextColor + "</textColor>\n");
            }*/
            
            if (balloonStyleText != null)
            {
                balloonStyle.addElement("text").addCDATA(balloonStyleText);
            }
        }
    }
    
    /**
     * @param kmlDocument 
     * @param iconURL
     * @param balloonStyleBgColor
     * @param balloonStyleTextColor
     * @param balloonStyleText
     * @return
     */
    public static void generateStyle(Element kmlDocument, final String iconURL,
                                       final String balloonStyleBgColor, 
                                       final String balloonStyleTextColor, 
                                       final String balloonStyleText)
    {
        if (iconURL != null || balloonStyleBgColor != null || balloonStyleTextColor != null || balloonStyleText != null)
        {
            Element style = kmlDocument.addElement("Style").addAttribute("id", "custom");
            generateIconCode(style, iconURL);
            generateBalloon(style, balloonStyleBgColor, balloonStyleTextColor, balloonStyleText);
        }
    }

    /**
     * Generates KML output based on the current points, names and descriptions given to the generator.
     * 
     * @return a String containing the generated KML
     */
    public Document generateKML()
    {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("kml").addAttribute("xmlns", KML_NAMESPACE_DECL);
        Element kmlDocument = root.addElement("Document");
        if (StringUtils.isNotEmpty(description))
        {
            kmlDocument.addElement("description").addText(description);
        }
        // setup the custom style, if any of these are non-null
        generateStyle(kmlDocument, placemarkIconURL, balloonStyleBgColor, balloonStyleTextColor, balloonStyleText);
        
        // generate a placemark for each point
        for (Pair<Double, Double> point: points)
        {
            String name     = pointNameMap.get(point);
            String htmlDesc = pointDescMap.get(point);
            buildPlacemark(kmlDocument, point, name, balloonStyleTextColor, htmlDesc, placemarkIconURL);
        }
        return document;
    }
    
    /**
     * Builds the XML for a KML 'Placemark' element using the given data.
     * @param kmlDocument 
     * 
     * @param point the geolocation of the placemark
     * @param name the string label of the placemark
     * @param textColor the 6 digit color string of the label of the placemark (for HTML)
     * @param htmlDesc the HTML content of the placemark popup balloon
     * @return the XML string
     */
    public static void buildPlacemark(Element kmlDocument, final Pair<Double, Double> point, 
                                        final String name, 
                                        final String textColor,
                                        final String htmlDesc, 
                                        @SuppressWarnings("unused") final String iconURL)
    {
        Element placemark = kmlDocument.addElement("Placemark");
        placemark.addElement("name").addText(name);
        placemark.addElement("styleUrl").addText("#custom");
        placemark.addElement("description").addText(htmlDesc);
        buildPointAndLookAt(placemark, point);
    }
    
    /**
     * Builds the XML for KML 'LookAt' and 'Point' elements.
     * @param placemark 
     * 
     * @param point the geolocation of the point
     * @return the XML string
     */
    public static void buildPointAndLookAt(Element placemark, final Pair<Double,Double> point)
    {
        Element lookAt = placemark.addElement("LookAt");
        lookAt.addElement("latitude").addText("" + point.first);
        lookAt.addElement("longitude").addText("" + point.second);
        lookAt.addElement("range").addText("300000.00");
        
        placemark.addElement("Point").addElement("coordinates").addText(
                point.second + "," + point.first);
    }
    
    /**
     * Generates KML output based on the current points, names and descriptions given to the generator.
     * 
     * @param out a stream to which the KML is written
     * @throws IOException if an I/O error occurs
     */
    public void generateKML(final FileWriter out) throws IOException
    {
        Document kml = generateKML();
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(out, format);
        writer.write(kml);
        writer.close();
    }

    /**
     * Generates KML output based on the current points, names and descriptions given to the generator.
     * 
     * @param outputFile the file in which to write the KML output
     * @throws IOException if an I/O error occurs while writing the output
     */
    public void generateKML(final File outputFile) throws IOException
    {
        FileWriter out = new FileWriter(outputFile);
        generateKML(out);
        out.close();
    }
    

    /**
     * A test main.
     * 
     * @param args ignored
     */
    /*public static void main(String[] args)
    {
        // tests
        Pair<Double,Double> allenFieldHouse = new Pair<Double, Double>(38.954,-95.252);
        String name = "Allen Fieldhouse";
        String htmlDesc = "<b>Allen Fieldhouse</b>\n<br/><a href=\"http://www.google.com\">Google</a>";
        GoogelEarthKMZGenerator kmlGen = new GoogelEarthKMZGenerator();
        kmlGen.setPlacemarkIconURL("http://redbud.nhm.ku.edu/specify/images/MammalCircle.png");
        kmlGen.addPlacemark(allenFieldHouse, name, htmlDesc);
        
        System.out.println(kmlGen.generateKML());
    }*/
}
