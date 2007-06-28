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
/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package edu.ku.brc.specify.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * 
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "accessionauthorization", uniqueConstraints = { @UniqueConstraint(columnNames = { "PermitID", "AccessionID" }), @UniqueConstraint(columnNames = { "RepositoryAgreementID" }) })
public class AccessionAuthorization extends DataModelObjBase implements java.io.Serializable,
        Comparable<AccessionAuthorization>
{

    // Fields

    protected Long                accessionAuthorizationId;
    protected String              remarks;
    protected Permit              permit;
    protected Accession           accession;
    protected RepositoryAgreement repositoryAgreement;

    // Constructors

    /** default constructor */
    public AccessionAuthorization()
    {
        // do nothing
    }

    /** constructor with id */
    public AccessionAuthorization(Long accessionAuthorizationId)
    {
        this.accessionAuthorizationId = accessionAuthorizationId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        accessionAuthorizationId = null;
        remarks = null;
        permit = null;
        accession = null;
        repositoryAgreement = null;
    }

    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "AccessionAuthorizationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getAccessionAuthorizationId()
    {
        return this.accessionAuthorizationId;
    }

    /**
     * Generic Getter for the ID Property.
     * 
     * @returns ID Property.
     */
    @Override
    @Transient
    public Long getId()
    {
        return this.accessionAuthorizationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return AccessionAuthorization.class;
    }

    public void setAccessionAuthorizationId(Long accessionAuthorizationId)
    {
        this.accessionAuthorizationId = accessionAuthorizationId;
    }

    /**
     * 
     */
    @Lob
    @Column(name="Remarks", unique=false, nullable=true, updatable=true, insertable=true)
   public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    /**
     * * Permit authorizing accession
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "PermitID", unique = false, nullable = false, insertable = true, updatable = true)
    public Permit getPermit()
    {
        return this.permit;
    }

    public void setPermit(Permit permit)
    {
        this.permit = permit;
    }

    /**
     * * Accession authorized by permit
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AccessionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Accession getAccession()
    {
        return this.accession;
    }

    public void setAccession(Accession accession)
    {
        this.accession = accession;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "RepositoryAgreementID", unique = false, nullable = true, insertable = true, updatable = true)
    public RepositoryAgreement getRepositoryAgreement()
    {
        return this.repositoryAgreement;
    }

    public void setRepositoryAgreement(RepositoryAgreement repositoryAgreement)
    {
        this.repositoryAgreement = repositoryAgreement;
    }

    public int compareTo(AccessionAuthorization obj)
    {
        if (permit != null && permit.permitNumber != null &&
                obj.permit.permitNumber != null &&
                obj.permit.permitNumber != null)
        {
            return permit.permitNumber.compareTo(obj.permit.permitNumber);
        }
        // else
        return timestampCreated.compareTo(obj.timestampCreated);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#addReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void addReference(FormDataObjIFace ref, String refType)
    {
        if (StringUtils.isNotEmpty(refType))
        {
            if (refType.equals("permit"))
            {
                if (ref instanceof Permit)
                {
                    permit = (Permit)ref;
                    ((Permit)ref).getAccessionAuthorizations().add(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of Permit");
                }
                
            } else if (refType.equals("accession"))
            {
                if (ref instanceof Accession)
                {
                    accession = (Accession)ref;
                    ((Accession)ref).getAccessionAuthorizations().add(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of Accession");
                }
                
            } else if (refType.equals("repositoryAgreement"))
            {
                if (ref instanceof RepositoryAgreement)
                {
                    repositoryAgreement = (RepositoryAgreement)ref;
                    ((RepositoryAgreement)ref).getRepositoryAgreementAuthorizations().add(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of RepositoryAgreement");
                }
                
            }
        } else
        {
            throw new RuntimeException("Adding Object ["+ref.getClass().getSimpleName()+"] and the refType is null.");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#removeReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void removeReference(FormDataObjIFace ref, String refType)
    {
        if (StringUtils.isNotEmpty(refType))
        {
            if (refType.equals("permit"))
            {
                if (ref instanceof Permit)
                {
                    permit = null;
                    ((Agent)ref).getAccessionAgents().remove(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of Permit");
                }
                
            } else if (refType.equals("accession"))
            {
                if (ref instanceof Accession)
                {
                    accession = null;
                    ((Accession)ref).getAccessionAgents().remove(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of Accession");
                }
                
            } else if (refType.equals("repositoryAgreement"))
            {
                if (ref instanceof RepositoryAgreement)
                {
                    repositoryAgreement = null;
                    ((RepositoryAgreement)ref).getRepositoryAgreementAgents().remove(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of RepositoryAgreement");
                }
                
            }
        } else
        {
            throw new RuntimeException("Removing Object ["+ref.getClass().getSimpleName()+"] and the refType is null.");
        }
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
        return 13;
    }

}
