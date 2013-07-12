package edu.ku.brc.services.gpx;
/*
 * Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute, 1345 Jayhawk Boulevard,
 * Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import edu.ku.brc.services.gpx.io.GpxType;
import edu.ku.brc.services.gpx.io.RteType;
import edu.ku.brc.services.gpx.io.TrkType;
import edu.ku.brc.services.gpx.io.TrksegType;
import edu.ku.brc.services.gpx.io.WptType;

/**
 * @author rods
 * 
 * @code_status Alpha
 * 
 * Aug 6, 2009
 * 
 */
public class GPXDataSet
{

    protected Exception exception = null;
    
    /**
     * 
     */
    public GPXDataSet()
    {
       
    }
    
    /**
     * @param gpxFileName
     * @return
     */
    public GpxType load(final String gpxFileName)
    {
        return load(new File(gpxFileName));
    }

    /**
     * @param gpxFile
     * @return
     */
    public GpxType load(final File gpxFile)
    {
        try
        {
            JAXBContext jaxContext = JAXBContext.newInstance("edu.ku.brc.services.gpx.io");

            Unmarshaller unmarshaller = jaxContext.createUnmarshaller();

            javax.xml.bind.JAXBElement<?> element = (javax.xml.bind.JAXBElement<?>) unmarshaller.unmarshal(new FileInputStream(gpxFile));

            GpxType gpx = (GpxType) element.getValue();

            return gpx;

        } catch (UnmarshalException ue)
        {
            ue.printStackTrace();
            exception = ue;

        } catch (JAXBException je)
        {
            je.printStackTrace();
            exception = je;
            
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
            exception = ioe;
        }
        return null;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        GPXDataSet gpx = new GPXDataSet();
        GpxType gpxType = gpx.load("/Users/rods/workspace/GPX/blue_hills.gpx");
        
        if (gpxType != null)
        {
            System.out.println(gpxType.getVersion());
            
            System.out.println("-------------- Way Points ---------------------------");
            for (WptType wt : gpxType.getWpt())
            {
                System.out.println(wt.getName() + " -> " + wt.getLat().doubleValue() + ", " + wt.getLon());
            }
            
            System.out.println("---------------- Routes -------------------------");
            for (RteType rt : gpxType.getRte())
            {
                System.out.println(rt.getName()+"  "+rt.getDesc());
                for (WptType wt : rt.getRtept())
                {
                    System.out.println("    "+wt.getName() + " -> " + wt.getLat().doubleValue() + ", " + wt.getLon());
                }
            }
            
            System.out.println("---------------- Tracks -------------------------");
            for (TrkType tt : gpxType.getTrk())
            {
                System.out.println(tt.getName()+"  "+tt.getDesc());
                for (TrksegType tst : tt.getTrkseg())
                {
                    for (WptType wt : tst.getTrkpt())
                    {
                        System.out.println("    "+wt.getName() + " -> " + wt.getLat().doubleValue() + ", " + wt.getLon());
                    }
                }
            }
        }
    }

}
