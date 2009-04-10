/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.af.core;

import java.sql.Timestamp;
import java.util.Properties;

import edu.ku.brc.specify.datamodel.Agent;

/**
 * Represents a resource that the application needs to run and the format is specified by a mime type. This is typically an XML
 * "blob", but it doesn't have to be.
 * 
 * @code_status Beta
 * 
 * @author rods
 *
 */
public interface AppResourceIFace
{

    /**
     * Returns the premission/priviledge level
     * @return the premission/priviledge level
     */
    public abstract Short getLevel();

    /**
     * Sets the premission/priviledge level
     * @param level the level
     */
    public abstract void setLevel(Short level);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract String getDescription();

    public abstract void setDescription(String description);

    public abstract String getMimeType();

    public abstract void setMimeType(String mimeType);

    public abstract Timestamp getTimestampCreated();

    public abstract void setTimestampCreated(Timestamp timestampCreated);

    public abstract Timestamp getTimestampModified();

    public abstract void setTimestampModified(Timestamp timestampModified);

    public abstract Agent getModifiedByAgent();

    public abstract void setModifiedByAgent(Agent lastEditedBy);
    
    public abstract String getMetaData();
    
    public abstract String getMetaData(String attr);

    public abstract void setMetaData(String metaData);
    
    public abstract Properties getMetaDataMap();
    
    public abstract void setDataAsString(final String dataStr);
    
    public abstract String getDataAsString();
    
    public abstract String toString();
    
    public abstract String getFileName();
    
    public abstract void setFileName(String fileName);

    //public abstract Set<AppResourceDefault> getAppContexts();
    //public abstract void setAppContexts(Set<AppResourceDefault> appContexts);

}
