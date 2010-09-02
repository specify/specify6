/* This library is free software; you can redistribute it and/or
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
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.toycode.mexconabio;

import com.thoughtworks.xstream.XStream;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 2, 2010
 *
 */
public class CollStatSQLDefs
{
    public enum StatType {eTotalNumRecords, eGeoRefed, eHasCollNum, eHasYearOnly, eHasYearMonOnly, eHasYMDayOnly, eHasSciNameNoGenSp, eMissingLocality, eMissingCollectors, eMissingCountries, }
    
    protected StatType type;
    protected String name;
    protected String SQL;
    

    /**
     * 
     */
    public CollStatSQLDefs()
    {
        super();
        // TODO Auto-generated constructor stub
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
     * @return the sQL
     */
    public String getSQL()
    {
        return SQL;
    }
    /**
     * @param sQL the sQL to set
     */
    public void setSQL(String sQL)
    {
        SQL = sQL;
    }
    
    /**
     * @return the type
     */
    public StatType getType()
    {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(StatType type)
    {
        this.type = type;
    }
    /**
     * @param xstream
     */
    public static void config(final XStream xstream)
    {
        xstream.alias("colstatsqldefs", CollStatSQLDefs.class); //$NON-NLS-1$
        xstream.useAttributeFor(CollStatSQLDefs.class, "name"); //$NON-NLS-1$
        xstream.useAttributeFor(CollStatSQLDefs.class, "type"); //$NON-NLS-1$
    }
}
