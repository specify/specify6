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
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jul 9, 2008
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spexportschema")
@org.hibernate.annotations.Table(appliesTo="spexportschema")
public class SpExportSchema extends DataModelObjBase
{
    protected Integer						spExportSchemaId;
	protected String						schemaName;
	protected String						schemaVersion;
	protected String						description;
	protected Set<SpExportSchemaItem>		spExportSchemaItems;
	protected Set<SpExportSchemaMapping>	spExportSchemaMappings;
	protected Discipline					discipline;
    
    /**
     * 
     */
    public SpExportSchema()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
		spExportSchemaId = null;
		schemaName = null;
		schemaVersion = null;
		description = null;
		spExportSchemaItems = new HashSet<SpExportSchemaItem>();
		spExportSchemaMappings = new HashSet<SpExportSchemaMapping>();
		discipline = null;
    }

    /**
     * @param exportSchemaId the exportSchemaId to set
     */
    public void setSpExportSchemaId(Integer exportSchemaId)
    {
        this.spExportSchemaId = exportSchemaId;
    }

    /**
     * @param schemaName the schemaName to set
     */
    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    /**
     * @param schemaVersion the schemaVersion to set
     */
    public void setSchemaVersion(String schemaVersion)
    {
        this.schemaVersion = schemaVersion;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @param items the items to set
     */
    public void setSpExportSchemaItems(Set<SpExportSchemaItem> items)
    {
        this.spExportSchemaItems = items;
    }
    
    /**
     * @param mappings the mappings to set
     */
    public void setSpExportSchemaMappings(Set<SpExportSchemaMapping> mappings)
    {
    	this.spExportSchemaMappings = mappings;
    }
    
    /**
     * @param discipline the discipline to set
     */
    public void setDiscipline(Discipline discipline)
    {
        this.discipline = discipline;
    }

    /**
     * @return the exportSchemaId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpExportSchemaID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpExportSchemaId()
    {
        return spExportSchemaId;
    }

    /**
     * @return the schemaName
     */
    @Column(name = "SchemaName", unique = false, nullable = true, insertable = true, updatable = true, length = 80)
    public String getSchemaName()
    {
        return schemaName;
    }

    /**
     * @return the schemaVersion
     */
    @Column(name = "SchemaVersion", unique = false, nullable = true, insertable = true, updatable = true, length = 80)
    public String getSchemaVersion()
    {
        return schemaVersion;
    }

    /**
     * @return the description
     */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the items
     */
    @OneToMany(mappedBy = "spExportSchema")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpExportSchemaItem> getSpExportSchemaItems()
    {
        return spExportSchemaItems;
    }
    
    @ManyToMany(mappedBy = "spExportSchemas")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<SpExportSchemaMapping> getSpExportSchemaMappings()
    {
    	return spExportSchemaMappings;
    }
        
    /**
     * @return the schemaItem
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DisciplineID", unique = false, nullable = false, insertable = true, updatable = true)
    public Discipline getDiscipline()
    {
        return discipline;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpExportSchema.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spExportSchemaId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Transient
    @Override
    public boolean isChangeNotifier()
    {
        return false;
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 524;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
	 */
	@Override
	public String toString()
	{
		return schemaName;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
	 */
	@Override
	public void forceLoad()
	{
		super.forceLoad();
		for (SpExportSchemaItem item : this.spExportSchemaItems)
		{
			item.getId();
		}
	}
    
    
}
