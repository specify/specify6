

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

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

import java.text.DecimalFormat;

/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id: Scriptlet.java,v 1.7 2005/04/04 15:18:41 teodord Exp $
 */
public class Scriptlet extends JRDefaultScriptlet
{


    /**
     *
     */
    public void beforeReportInit() throws JRScriptletException
    {
        System.out.println("call beforeReportInit");
    }


    /**
     *
     */
    public void afterReportInit() throws JRScriptletException
    {
        System.out.println("call afterReportInit");
    }


    /**
     *
     */
    public void beforePageInit() throws JRScriptletException
    {
        System.out.println("call   beforePageInit : PAGE_NUMBER = " + this.getVariableValue("PAGE_NUMBER"));
    }


    /**
     *
     */
    public void afterPageInit() throws JRScriptletException
    {
        System.out.println("call   afterPageInit  : PAGE_NUMBER = " + this.getVariableValue("PAGE_NUMBER"));
    }


    /**
     *
     */
    public void beforeColumnInit() throws JRScriptletException
    {
        System.out.println("call     beforeColumnInit");
    }


    /**
     *
     */
    public void afterColumnInit() throws JRScriptletException
    {
        System.out.println("call     afterColumnInit");
    }


    /**
     *
     */
    public void beforeGroupInit(String groupName) throws JRScriptletException
    {
        if (groupName.equals("CityGroup"))
        {
            System.out.println("call       beforeGroupInit : City = " + this.getFieldValue("City"));
        }
    }


    /**
     *
     */
    public void afterGroupInit(String groupName) throws JRScriptletException
    {
        if (groupName.equals("CityGroup"))
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
        }
    }


    /**
     *
     */
    public void beforeDetailEval() throws JRScriptletException
    {
        System.out.println("        detail");
    }


    /**
     *
     */
    public void afterDetailEval() throws JRScriptletException
    {
    }


    /**
     *
     */
    public String format(String aFloatStr) throws JRScriptletException
    {
        return format(new Float(Float.parseFloat(aFloatStr)));
    }

    /**
     *
     */
    public String format(Float aFloat) throws JRScriptletException
    {
        DecimalFormat df = new DecimalFormat("#.####");
        return df.format(aFloat.floatValue());
    }

    public String getDirChar(Float aFloat, boolean isLat)
    {
        if (isLat)
            return aFloat.floatValue() > 0.0 ? "N" : "S";
        else
            return aFloat.floatValue() > 0.0 ? "E" : "W";
        
    }
    
    public String getDirChar(String aFloatStr, boolean isLat)
    {
        return getDirChar(new Float(Float.parseFloat(aFloatStr)), isLat);
    }
    
    public String degrees(Float aFloatStr, boolean isLat) throws JRScriptletException
    {
        float coord = aFloatStr.floatValue();
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
        //"½"
        return df.format(coord) + "º" + dir;
        //} catch (Exception ex)
        //{
            
        //}
        //return "XXX";
    }

    public String degrees(String aFloatStr, boolean isLat) throws JRScriptletException
    {
        return degrees(new Float(Float.parseFloat(aFloatStr)), isLat);
    }
    
    public String locality(String aDesc, Float aLat, Float aLon) throws JRScriptletException
    {
        StringBuffer strBuf = new StringBuffer(aDesc);
        strBuf.append(" ");
        strBuf.append(degrees(aLat, true));
        strBuf.append(", ");
        strBuf.append(degrees(aLon, false));
        return strBuf.toString();
    }


}
