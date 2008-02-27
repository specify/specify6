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
package edu.ku.brc.specify.tasks;

import java.util.Vector;

import com.thoughtworks.xstream.XStream;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 27, 2008
 *
 */
public class DataEntryXML
{
    protected Vector<DataEntryView> std  = new Vector<DataEntryView>();
    protected Vector<DataEntryView> misc = new Vector<DataEntryView>();
    
    /**
     * 
     */
    public DataEntryXML()
    {
        super();
    }
    /**
     * @return the std
     */
    public Vector<DataEntryView> getStd()
    {
        return std;
    }
    /**
     * @param std the std to set
     */
    public void setStd(Vector<DataEntryView> std)
    {
        this.std = std;
    }
    /**
     * @return the misc
     */
    public Vector<DataEntryView> getMisc()
    {
        return misc;
    }
    /**
     * @param misc the misc to set
     */
    public void setMisc(Vector<DataEntryView> misc)
    {
        this.misc = misc;
    }
    
    /**
     * Configures inner classes for XStream.
     * @param xstream the xstream
     */
    public static void config(final XStream xstream)
    {
        xstream.alias("views", DataEntryXML.class);
    }
}
