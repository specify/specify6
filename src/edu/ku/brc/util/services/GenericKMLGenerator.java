/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.util.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

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

    /** Standard XML file type declaration. */
    protected static String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    /** Keyhole Markup Language namespace declaration. */
    protected static String KML_NAMESPACE_DECL = "<kml xmlns=\"http://earth.google.com/kml/2.1\">\n";

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
    public void addPlacemark(Pair<Double,Double> point, String name, String htmlDescription)
    {
        //log.debug("Adding point to KML generator: " + point + "   " + htmlDescription);

        if (point == null)
        {
            return;
        }
        
        points.add(point);
        pointNameMap.put(point, name);
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
     * @param iconURL
     * @return
     */
    public static String generateIconCode(final String iconURL)
    {
        if (StringUtils.isNotEmpty(iconURL))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<IconStyle>\n");
            sb.append("<Icon>\n");
            sb.append("<href>");
            sb.append(iconURL);
            sb.append("</href>\n");
            sb.append("</Icon>\n");
            sb.append("</IconStyle>\n");
            return sb.toString();
        }
        return "";
    }
    
    /**
     * @param balloonStyleBgColor
     * @param balloonStyleTextColor
     * @param balloonStyleText
     * @return
     */
    public static String generateBalloon(final String balloonStyleBgColor, 
                                         final String balloonStyleTextColor, 
                                         final String balloonStyleText)
    {
        if (balloonStyleBgColor != null || balloonStyleTextColor != null || balloonStyleText != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<BalloonStyle>\n");
            
            if (balloonStyleBgColor != null)
            {
                sb.append("<bgColor>" + balloonStyleBgColor + "</bgColor>\n");
            }
            
            if (balloonStyleTextColor != null)
            {
                sb.append("<textColor>" + balloonStyleTextColor + "</textColor>\n");
            }
            
            if (balloonStyleText != null)
            {
                sb.append("<text><![CDATA[" + balloonStyleText + "]]></text>\n");
            }
            sb.append("</BalloonStyle>\n");
            return sb.toString();
        }
        return "";
    }
    
    /**
     * @param iconURL
     * @param balloonStyleBgColor
     * @param balloonStyleTextColor
     * @param balloonStyleText
     * @return
     */
    public static String generateStyle(final String iconURL,
                                       final String balloonStyleBgColor, 
                                       final String balloonStyleTextColor, 
                                       final String balloonStyleText)
    {
        if (iconURL != null || balloonStyleBgColor != null || balloonStyleTextColor != null || balloonStyleText != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("<Style id=\"custom\">\n");
            sb.append(generateIconCode(iconURL));
            sb.append(generateBalloon(balloonStyleBgColor, balloonStyleTextColor, balloonStyleText));
            sb.append("</Style>\n"); 
            return sb.toString();
        }
        return "";
    }

    /**
     * Generates KML output based on the current points, names and descriptions given to the generator.
     * 
     * @return a String containing the generated KML
     */
    public String generateKML()
    {
        StringBuilder kmlBuilder = new StringBuilder();
        kmlBuilder.append(XML_DECLARATION);
        kmlBuilder.append(KML_NAMESPACE_DECL);
        kmlBuilder.append("<Document>\n");
        if (StringUtils.isNotEmpty(description))
        {
            kmlBuilder.append("<description><![CDATA[");
            kmlBuilder.append(description);
            kmlBuilder.append("]]></description>");
        }
        // setup the custom style, if any of these are non-null
        kmlBuilder.append(generateStyle(placemarkIconURL, balloonStyleBgColor, balloonStyleTextColor, balloonStyleText));
        
        // generate a placemark for each point
        for (Pair<Double, Double> point: points)
        {
            String name     = pointNameMap.get(point);
            String htmlDesc = pointDescMap.get(point);
            kmlBuilder.append(buildPlacemark(point, name, htmlDesc, placemarkIconURL));
        }
        
        kmlBuilder.append("</Document>\n");
        kmlBuilder.append("</kml>\n");
        return kmlBuilder.toString();
    }
    
    /**
     * Builds the XML for a KML 'Placemark' element using the given data.
     * 
     * @param point the geolocation of the placemark
     * @param name the string label of the placemark
     * @param htmlDesc the HTML content of the placemark popup balloon
     * @return the XML string
     */
    public static String buildPlacemark(final Pair<Double, Double> point, final String name, final String htmlDesc, final String iconURL)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(generateXmlStartTag("Placemark"));
        sb.append(generateXmlElement("name", name));
        
        if (iconURL != null)
        {
            sb.append(generateXmlElement("styleUrl", "#custom"));
            sb.append("\n");
        }
        sb.append(generateXmlElement("description", "<![CDATA[" + htmlDesc + "]]>"));
        sb.append("\n");
        sb.append(buildPointAndLookAt(point));
        sb.append("\n");
        sb.append(generateXmlEndTag("Placemark"));
        
        return sb.toString();
    }
    
    /**
     * Builds the XML for KML 'LookAt' and 'Point' elements.
     * 
     * @param point the geolocation of the point
     * @return the XML string
     */
    public static String buildPointAndLookAt(final Pair<Double,Double> point)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<LookAt>\n");
        sb.append("<latitude>");
        sb.append(point.first);
        sb.append("</latitude>\n");
        sb.append("<longitude>");
        sb.append(point.second);
        sb.append("</longitude>\n");
        sb.append("<range>300000.00</range>\n");
        sb.append("</LookAt>\n");
        sb.append("<Point>\n");
        sb.append("<coordinates>");
        sb.append(point.second);
        sb.append(",");
        sb.append(point.first);
        sb.append("</coordinates>\n");
        sb.append("</Point>\n");
        return sb.toString();
    }
    
    /**
     * Generates KML output based on the current points, names and descriptions given to the generator.
     * 
     * @param out a stream to which the KML is written
     * @throws IOException if an I/O error occurs
     */
    public void generateKML(final OutputStream out) throws IOException
    {
        String kml = generateKML();
        out.write(kml.getBytes());
    }

    /**
     * Generates KML output based on the current points, names and descriptions given to the generator.
     * 
     * @param outputFile the file in which to write the KML output
     * @throws IOException if an I/O error occurs while writing the output
     */
    public void generateKML(final File outputFile) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(outputFile);
        generateKML(fos);
    }
    
    /**
     * Builds an XML start tag of the given name.
     * 
     * @param name the name of the start tag
     * @return the XML snippet
     */
    public static String generateXmlStartTag(final String name)
    {
        return "<" + name + ">";
    }

    /**
     * Builds an XML end tag of the given name.
     * 
     * @param name the name of the end tag
     * @return the XML snippet
     */
    public static String generateXmlEndTag(final String name)
    {
        return "</" + name + ">";
    }
    
    /**
     * Builds an XML element with the given name and content.
     * 
     * @param name the name of the element
     * @param content the content of the element
     * @return the XML snippet
     */
    public static String generateXmlElement(final String name, final String content)
    {
        if (content == null || content.equals("") )
        {
            return "<" + name + "/>";
        }

        return "<" + name + ">" + content + "</" + name + ">";
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
