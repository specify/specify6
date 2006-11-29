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
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @code_status Beta
 * @author jstewart
 */
public class DeterminationStatus extends DataModelObjBase implements Serializable
{
    protected Long               determinationStatusId;
    protected String             name;
    protected String             remarks;
    protected Set<Determination> determinations;

    public DeterminationStatus()
    {
        super();
    }

    public DeterminationStatus(Long determinationStatusId)
    {
        super();
        this.determinationStatusId = determinationStatusId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        determinationStatusId = null;
        name = null;
        remarks = null;
        timestampCreated = new Date();
        timestampModified = null;
        lastEditedBy = null;
        determinations = new HashSet<Determination>();
    }

    public Set<Determination> getDeterminations()
    {
        return determinations;
    }

    public void setDeterminations(Set<Determination> determinations)
    {
        this.determinations = determinations;
    }

    public Long getDeterminationStatusId()
    {
        return determinationStatusId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    public Long getId()
    {
        return this.determinationStatusId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class<?> getDataClass()
    {
        return DeterminationStatus.class;
    }

    public void setDeterminationStatusId(Long determinationStatusId)
    {
        this.determinationStatusId = determinationStatusId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getRemarks()
    {
        return remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getLastEditedBy()
     */
    @Override
    public String getLastEditedBy()
    {
        return lastEditedBy;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#setLastEditedBy(java.lang.String)
     */
    @Override
    public void setLastEditedBy(String lastEditedBy)
    {
        this.lastEditedBy = lastEditedBy;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTimestampCreated()
     */
    @Override
    public Date getTimestampCreated()
    {
        return timestampCreated;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#setTimestampCreated(java.util.Date)
     */
    @Override
    public void setTimestampCreated(Date timestampCreated)
    {
        this.timestampCreated = timestampCreated;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTimestampModified()
     */
    @Override
    public Date getTimestampModified()
    {
        return timestampModified;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#setTimestampModified(java.util.Date)
     */
    @Override
    public void setTimestampModified(Date timestampModified)
    {
        this.timestampModified = timestampModified;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 501;
    }

    @Override
    public String getIdentityTitle()
    {
        // TODO Auto-generated method stub
        return "DeterminationStatus: " + this.name;
    }

}
