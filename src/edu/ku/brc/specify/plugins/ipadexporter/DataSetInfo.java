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
package edu.ku.brc.specify.plugins.ipadexporter;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 11, 2012
 *
 */
public class DataSetInfo
{
    private int     id;
    private String  name;
    private String  inst;
    private String  div;
    private String  disp;
    private String  coll;
    private boolean isGlobal;


    /**
     * @param id
     * @param name
     * @param inst
     * @param div
     * @param disp
     * @param coll
     * @param isGlobal
     */
    public DataSetInfo(int id, String name, String inst, String div, String disp, String coll,
                       boolean isGlobal)
    {
        super();
        this.id = id;
        this.name = name;
        this.inst = inst;
        this.div = div;
        this.disp = disp;
        this.coll = coll;
        this.isGlobal = isGlobal;
    }


    /**
     * @return the id
     */
    public int getId()
    {
        return id;
    }


    /**
     * @param id the id to set
     */
    public void setId(int id)
    {
        this.id = id;
    }


    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }


    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * @return the inst
     */
    public String getInst()
    {
        return inst;
    }


    /**
     * @param inst the inst to set
     */
    public void setInst(String inst)
    {
        this.inst = inst;
    }


    /**
     * @return the div
     */
    public String getDiv()
    {
        return div;
    }


    /**
     * @param div the div to set
     */
    public void setDiv(String div)
    {
        this.div = div;
    }


    /**
     * @return the disp
     */
    public String getDisp()
    {
        return disp;
    }


    /**
     * @param disp the disp to set
     */
    public void setDisp(String disp)
    {
        this.disp = disp;
    }


    /**
     * @return the coll
     */
    public String getColl()
    {
        return coll;
    }


    /**
     * @param coll the coll to set
     */
    public void setColl(String coll)
    {
        this.coll = coll;
    }


    /**
     * @return the isGlobal
     */
    public boolean isGlobal()
    {
        return isGlobal;
    }


    /**
     * @param isGlobal the isGlobal to set
     */
    public void setGlobal(boolean isGlobal)
    {
        this.isGlobal = isGlobal;
    }
    
    
}