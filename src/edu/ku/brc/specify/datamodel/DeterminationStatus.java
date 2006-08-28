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
	protected Integer determinationStatusId;
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
	
	public DeterminationStatus(Integer determinationStatusId)
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
        timestampModified = new Date();
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

	public Integer getDeterminationStatusId()
	{
		return determinationStatusId;
	}

	public void setDeterminationStatusId(Integer determinationStatusId)
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
