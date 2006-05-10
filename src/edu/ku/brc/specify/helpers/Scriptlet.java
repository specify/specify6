/*
 * ============================================================================
 *                   GNU Lesser General Public License
 * ============================================================================
 *
 * JasperReports - Free Java report-generating library.
 * Copyright (C) 2001-2005 Teodor Danciu teodord@users.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Teodor Danciu
 * 173, Calea Calarasilor, Bl. 42, Sc. 1, Ap. 18
 * Postal code 030615, Sector 3
 * Bucharest, ROMANIA
 * Email: teodord@users.sourceforge.net
 */
package edu.ku.brc.specify.helpers;

import java.text.DecimalFormat;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id: Scriptlet.java,v 1.7 2005/04/04 15:18:41 teodord Exp $
 */
public class Scriptlet extends JRDefaultScriptlet
{

    /**
     * beforeReportInit
     */
    public void beforeReportInit() throws JRScriptletException
    {
        //System.out.println("call beforeReportInit");
    }


    /**
     * afterReportInit
     */
    public void afterReportInit() throws JRScriptletException
    {
        //System.out.println("call afterReportInit");
    }


    /**
     * beforePageInit
     */
    public void beforePageInit() throws JRScriptletException
    {
        //System.out.println("call   beforePageInit : PAGE_NUMBER = " + this.getVariableValue("PAGE_NUMBER"));
    }


    /**
     *
     */
    public void afterPageInit() throws JRScriptletException
    {
        //System.out.println("call   afterPageInit  : PAGE_NUMBER = " + this.getVariableValue("PAGE_NUMBER"));
    }


    /**
     *
     */
    public void beforeColumnInit() throws JRScriptletException
    {
        //System.out.println("call     beforeColumnInit");
    }


    /**
     * afterColumnInit
     */
    public void afterColumnInit() throws JRScriptletException
    {
        //System.out.println("call     afterColumnInit");
    }


    /**
     * beforeGroupInit
     */
    public void beforeGroupInit(String groupName) throws JRScriptletException
    {
        /*if (groupName.equals("CityGroup"))
        {
            System.out.println("call       beforeGroupInit : City = " + this.getFieldValue("City"));
        }*/
    }


    /**
     * afterGroupInit
     */
    public void afterGroupInit(String groupName) throws JRScriptletException
    {
        /*if (groupName.equals("CityGroup"))
        {
            System.out.println("call       afterGroupInit  : City = " + this.getFieldValue("City"));

            String allCities = (String)this.getVariableValue("AllCities");
            String city = (String)this.getFieldValue("City");
            StringBuffer sbuffer = new StringBuffer();

            if (allCities != null)
            {
                sbuffer.append(allCities);
                sbuffer.append(", ");
            }

            sbuffer.append(city);
            this.setVariableValue("AllCities", sbuffer.toString());
        }*/
    }


    /**
     * beforeDetailEval
     */
    public void beforeDetailEval() throws JRScriptletException
    {
        //System.out.println("        detail");
    }


    /**
     * afterDetailEval
     */
    public void afterDetailEval() throws JRScriptletException
    {
    }


    /**
     * Formats a String to a float to a String
     *
     * @param floatStr the string with a Float value
     * @return Formats a String to a float to a String
     * @throws JRScriptletException xxx
     */
    public String format(String floatStr) throws JRScriptletException
    {
        if (floatStr == null)
        {
            return "";
        }
        return format(new Float(Float.parseFloat(floatStr)));
    }

    /**
     * Formats a float to a string
     * @param floatVar the float variable
     * @return Formats a float to a string
     * @throws JRScriptletException
     */
    public String format(Float floatVar) throws JRScriptletException
    {
        if (floatVar == null)
        {
            return "";
        }

        DecimalFormat df = new DecimalFormat("#.####");
        return df.format(floatVar.floatValue());
    }

    /**
     * Formats a float to a string with "N","S","E", "W"
     * @param floatVal the float value
     * @param isLat whether it is a lat or lon
     * @return Formats a float to a string with "N","S","E", "W"
     */
    public String getDirChar(Float floatVal, boolean isLat)
    {
        if (floatVal == null)
        {
            return "";
        }

        if (isLat)
            return floatVal.floatValue() > 0.0 ? "N" : "S";
        else
            return floatVal.floatValue() > 0.0 ? "E" : "W";

    }

    /**
     * Formats a String as a float with "N","S","E", "W"
     * @param floatVal the float value
     * @param isLat whether it is a lat or lon
     * @return Formats a String as a float with "N","S","E", "W"
     */
    public String getDirChar(String floatVal, boolean isLat)
    {
        if (floatVal == null)
        {
            return "";
        }
        return getDirChar(new Float(Float.parseFloat(floatVal)), isLat);
    }

    /**
     * Formats a float string into a lat/lon with "N","S","E", "W"
     * @param floatStr the float to be formatted
     * @param isLat whether itis a lat or lon
     * @return Formats a float string into a lat/lon with "N","S","E", "W"
     * @throws JRScriptletException
     */
    public String degrees(Float floatStr, boolean isLat) throws JRScriptletException
    {
        if (floatStr == null)
        {
            return "";
        }
        float coord = floatStr.floatValue();
        DecimalFormat df = new DecimalFormat("#.####");
        String dir = "";
        if (isLat)
            dir = coord > 0.0 ? "N" : "S";
        else
            dir = coord > 0.0 ? "E" : "W";
        /*String str = "";
        for (int i=150;i<190;i++)
        {
            str += Character.forDigit(i, 10);
        }*/
        //return str;
        //try {
        //String degress = new String("&#8600;?".getBytes("UTF-8"), "UTF-8");
        //"�"
        return df.format(coord) + " " + dir;
        //} catch (Exception ex)
        //{

        //}
        //return "XXX";
    }

    /**
     * Formats a String with a float value as a degrees
     * @param floatStr
     * @param isLat inidcates whether it is a latitude or a longitude
     * @return Formats a String with a float value as a degrees
     * @throws JRScriptletException XXX
     */
    public String degrees(String floatStr, boolean isLat) throws JRScriptletException
    {
        return degrees(new Float(Float.parseFloat(floatStr)), isLat);
    }


    /**
     * Formats a Lat,Lon into a single string where the values are separated by a comma
     * @param desc a prefix of a description
     * @param lat the latitude
     * @param lon the longitude
     * @return Formats a Lat,Lon into a single string where the values are separated by a comma
     * @throws JRScriptletException XXX
     */
    public String locality(Object desc, Float lat, Float lon) throws JRScriptletException
    {

        StringBuffer strBuf = new StringBuffer();
        if (desc instanceof String)
        {
            strBuf.append(((String)desc));
        } else if (desc instanceof byte[])
        {
            strBuf.append(new String((byte[])desc));
        }
        strBuf.append(" ");
        strBuf.append(degrees(lat, true));
        strBuf.append(", ");
        strBuf.append(degrees(lon, false));
        return strBuf.toString();
    }

    /**
     * @param fieldNumber
     * @return
     */
    public String formatFieldNo(String fieldNumber)
    {
        return fieldNumber == null ? "" : fieldNumber;
    }

}
