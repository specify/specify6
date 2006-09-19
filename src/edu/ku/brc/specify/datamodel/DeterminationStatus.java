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
package edu.ku.brc.specify.datamodel;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class DeterminationStatus implements Serializable
{
	protected Long determinationStatusId;
	protected String name;
	protected String remarks;
    protected Date timestampCreated;
    protected Date timestampModified;
    protected String lastEditedBy;
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

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.determinationStatusId;
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

	public String getLastEditedBy()
	{
		return lastEditedBy;
	}

	public void setLastEditedBy(String lastEditedBy)
	{
		this.lastEditedBy = lastEditedBy;
	}

	public Date getTimestampCreated()
	{
		return timestampCreated;
	}

	public void setTimestampCreated(Date timestampCreated)
	{
		this.timestampCreated = timestampCreated;
	}

	public Date getTimestampModified()
	{
		return timestampModified;
	}

	public void setTimestampModified(Date timestampModified)
	{
		this.timestampModified = timestampModified;
	}
}
