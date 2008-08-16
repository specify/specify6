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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.dbsupport.RecordTypeCode;
import edu.ku.brc.specify.dbsupport.RecordTypeCodeItem;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.ui.db.PickListItemIFace;

/**
 * 
 * @code_status Beta
 * @author jstewart
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "determinationstatus")
@org.hibernate.annotations.Table(appliesTo="determinationstatus", indexes =
    {   @Index (name="DeterStatusNameIDX", columnNames={"Name"})
    })
public class DeterminationStatus extends DataModelObjBase implements Serializable,
                                                                     Comparable<DeterminationStatus>
{
    
    public static final byte CURRENT          = 1;
    public static final byte OLDDETERMINATION = 2;
    public static final byte NOTCURRENT       = 3;
    public static final byte USERDEFINED      = 64;
    
    protected Integer            determinationStatusId;
    protected Byte               type;
    protected String             name;
    protected String             remarks;
    
    protected Discipline         discipline;
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        determinationStatusId = null;
        type                  = null;
        name                  = null;
        remarks               = null;
        discipline            = AppContextMgr.getInstance().getClassObject(Discipline.class);
        determinations        = new HashSet<Determination>();
    }

    @Id
    @GeneratedValue
    @Column(name = "DeterminationStatusID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getDeterminationStatusId()
    {
        return determinationStatusId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.determinationStatusId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return DeterminationStatus.class;
    }

    public void setDeterminationStatusId(Integer determinationStatusId)
    {
        this.determinationStatusId = determinationStatusId;
    }

    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * @return the type
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Byte type)
    {
        this.type = type;
    }

    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }
    
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "status")
    @Cascade( { CascadeType.MERGE, CascadeType.LOCK })
    public Set<Determination> getDeterminations()
    {
        return determinations;
    }

    public void setDeterminations(Set<Determination> determinations)
    {
        this.determinations = determinations;
    }

    /**
     * @return the discipline
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DisciplineID", unique = false, nullable = false, insertable = true, updatable = true)
    public Discipline getDiscipline()
    {
        return discipline;
    }

    /**
     * @param discipline the discipline to set
     */
    public void setDiscipline(Discipline discipline)
    {
        this.discipline = discipline;
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
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 88;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return StringUtils.isNotEmpty(name) ? name : super.getIdentityTitle();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(DeterminationStatus o)
    {
        return type.compareTo(o.type);
    }
    
    /**
     * @return List of pick lists for predefined system type codes.
     * 
     * The QueryBuilder function is used to generate picklist criteria controls for querying,
     * and to generate text values for the typed fields in query results and reports.
     * 
     * The WB uploader will also need this function.
     * 
     */
    @Transient
    public static List<PickListDBAdapterIFace> getSpSystemTypeCodes()
    {
        List<PickListDBAdapterIFace> result = new Vector<PickListDBAdapterIFace>(1);
        Vector<PickListItemIFace> stats = new Vector<PickListItemIFace>(4);
        stats.add(new RecordTypeCodeItem(UIRegistry.getResourceString("DeterminationStatus_CURRENT"), DeterminationStatus.CURRENT));
        stats.add(new RecordTypeCodeItem(UIRegistry.getResourceString("DeterminationStatus_OLDDETERMINATION"), DeterminationStatus.OLDDETERMINATION));
        stats.add(new RecordTypeCodeItem(UIRegistry.getResourceString("DeterminationStatus_NOTCURRENT"), DeterminationStatus.NOTCURRENT));
        //XXX UserDefined???
        stats.add(new RecordTypeCodeItem(UIRegistry.getResourceString("DeterminationStatus_USERDEFINED"), DeterminationStatus.USERDEFINED));
        result.add(new RecordTypeCode(stats, "type"));
        return result;
    }

    /**
     * @return a list (probably never containing more than one element) of fields
     * with predefined system type codes.
     */
    @Transient
    public static String[] getSpSystemTypeCodeFlds()
    {
        String[] result = {"type"};
        return result;
    }

}
