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
package edu.ku.brc.af.tasks.subpane.formeditor;

import java.util.Vector;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 23, 2007
 *
 */
public class Control implements ControlIFace
{
    protected String type;
    protected String desc;
    
    protected Vector<Attr>       attrs       = new Vector<Attr>();
    protected Vector<Param>      params      = new Vector<Param>();
    protected Vector<SubControl> subcontrols = new Vector<SubControl>();
    
    public Control()
    {
        
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return the attrs
     */
    public Vector<Attr> getAttrs()
    {
        return attrs;
    }

    /**
     * @param attrs the attrs to set
     */
    public void setAttrs(Vector<Attr> attrs)
    {
        this.attrs = attrs;
    }

    /**
     * @return the desc
     */
    public String getDesc()
    {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    /**
     * @return the params
     */
    public Vector<Param> getParams()
    {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(Vector<Param> params)
    {
        this.params = params;
    }

    /**
     * @return the subcontrols
     */
    public Vector<SubControl> getSubcontrols()
    {
        return subcontrols;
    }

    /**
     * @param subcontrols the subcontrols to set
     */
    public void setSubcontrols(Vector<SubControl> subcontrols)
    {
        this.subcontrols = subcontrols;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return type;
    }
    
}
