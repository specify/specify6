/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
    protected Vector<DataEntryView> batch = new Vector<DataEntryView>();
    
    /**
     * 
     */
    public DataEntryXML()
    {
        super();
    }
    
    /**
     * 
     */
    public DataEntryXML(final Vector<DataEntryView> stds, 
                        final Vector<DataEntryView> miscs,
                        final Vector<DataEntryView> batches)
    {
        super();
        std.addAll(stds);
        misc.addAll(miscs);
        //batch.addAll(batches);
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
	 * @return the batch
	 */
	public Vector<DataEntryView> getBatch() {
		return batch;
	}

	/**
	 * @param batch the batch to set
	 */
	public void setBatch(Vector<DataEntryView> batch) {
		this.batch = batch;
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
