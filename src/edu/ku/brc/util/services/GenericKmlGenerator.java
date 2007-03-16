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

import edu.ku.brc.util.Pair;

/**
 * This class generates KML output representing the provided geo reference data
 * and HTML descriptions.
 * 
 * @author jstewart
 * @code_status Alpha
 */
public class GenericKmlGenerator
{
    /**
     * A list of all points to be included in the generated KML.
     */
    List<Pair<Double, Double>> points = new Vector<Pair<Double,Double>>();

    /**
     * A mapping from a geo reference point to the HTML text description for that point.
     */
    Map<Pair<Double,Double>, String> pointDescMap = new Hashtable<Pair<Double,Double>, String>();
    
    /**
     * Constructs an instance.
     */
    public GenericKmlGenerator()
    {
        // nothing needed here
    }
    
    /**
     * Includes the given point and HTML text to be included in the generated KML output.
     * 
     * @param point the geo reference point
     * @param htmlDescription the text description (as HTML)
     */
    public void addPointAndDescription(Pair<Double,Double> point, String htmlDescription)
    {
        points.add(point);
        pointDescMap.put(point, htmlDescription);
    }
    
    /**
     * Generates KML output based on the current points and descriptions given to the generator.
     * 
     * @return a String containing the generated KML
     */
    public String generateKML()
    {
        return "";
    }
    
    /**
     * Generates KML output based on the current points and descriptions given to the generator.
     * 
     * @param out a stream to which the KML is written
     * @throws IOException if an I/O error occurs
     */
    public void generateKML( OutputStream out ) throws IOException
    {
        String kml = generateKML();
        out.write(kml.getBytes());
    }

    /**
     * Generates KML output based on the current points and descriptions given to the generator.
     * 
     * @param outputFile the file in which to write the KML output
     * @throws IOException if an I/O error occurs while writing the output
     */
    public void generateKML( File outputFile ) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(outputFile);
        generateKML(fos);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
    }
}
