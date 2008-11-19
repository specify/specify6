/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "sptaxonsynonymy")
//@org.hibernate.annotations.Table(appliesTo="sptaxonsynonymy", indexes =
//    {   @Index (name="SpSynonymyAccepted", columnNames={"AcceptedId"}),
//        @Index (name="SpSynonymyNotAccepted", columnNames={"NotAcceptedId"})
//    })
public class SpTaxonSynonymy extends DataModelObjBase
{

    protected Integer                       spTaxonSynonymyId;
    protected TaxonTreeDef                  taxonTreeDef;
    protected Taxon                         accepted;
    protected Taxon                         notAccepted;
    protected Agent                         synonymizer;
    protected Set<SpSynonymyDetermination> determinations;
    
    
    /**
     * 
     */
    public SpTaxonSynonymy()
    {
        //nothing
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spTaxonSynonymyId = null;
        taxonTreeDef = null;
        accepted = null;
        notAccepted = null;
        synonymizer = null;
        determinations = new HashSet<SpSynonymyDetermination>();
    }

    /**
     * @return the spSynonymyId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpTaxonSynonymyID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpTaxonSynonymyId()
    {
        return spTaxonSynonymyId;
    }


    
    /**
     * @param spTaxonSynonymyId the spTaxonSynonymyId to set
     */
    public void setSpTaxonSynonymyId(Integer spTaxonSynonymyId)
    {
        this.spTaxonSynonymyId = spTaxonSynonymyId;
    }


    /**
     * @return the taxonTreeDef
     */
    @ManyToOne
    @JoinColumn(name = "TaxonTreeDefID", nullable = false)
    public TaxonTreeDef getTaxonTreeDef()
    {
        return taxonTreeDef;
    }


    /**
     * @param taxonTreeDef the taxonTreeDef to set
     */
    public void setTaxonTreeDef(TaxonTreeDef taxonTreeDef)
    {
        this.taxonTreeDef = taxonTreeDef;
    }


    /**
     * @return the accepted
     */
    @ManyToOne
    @JoinColumn(name = "AcceptedID")
    public Taxon getAccepted()
    {
        return accepted;
    }


    /**
     * @param accepted the accepted to set
     */
    public void setAccepted(Taxon accepted)
    {
        this.accepted = accepted;
    }


    /**
     * @return the notAccepted
     */
    @ManyToOne
    @JoinColumn(name = "NotAcceptedID")
    public Taxon getNotAccepted()
    {
        return notAccepted;
    }


    /**
     * @param notAccepted the notAccepted to set
     */
    public void setNotAccepted(Taxon notAccepted)
    {
        this.notAccepted = notAccepted;
    }


    /**
     * @return the synonymizer
     */
    @ManyToOne
    @JoinColumn(name = "SynonymizerID")
    public Agent getSynonymizer()
    {
        return synonymizer;
    }


    /**
     * @param synonymizer the synonymizer to set
     */
    public void setSynonymizer(Agent synonymizer)
    {
        this.synonymizer = synonymizer;
    }


    /**
     * @return the determinations
     */
    @OneToMany(mappedBy = "taxonSynonymy")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpSynonymyDetermination> getDeterminations()
    {
        return determinations;
    }

    public void removeDetermination(final SpSynonymyDetermination toRemove)
    {
        SpSynonymyDetermination toRemoveObj = null;
        for (SpSynonymyDetermination det: determinations)
        {
            if (det.getId().equals(toRemove.getId()))
            {
                toRemoveObj = det;
                break;
            }
        }
        if (toRemoveObj != null)
        {
            determinations.remove(toRemoveObj);
        }
    }

    /**
     * @param determinations the determinations to set
     */
    public void setDeterminations(Set<SpSynonymyDetermination> determinations)
    {
        this.determinations = determinations;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpTaxonSynonymy.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spTaxonSynonymyId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return 527;
    }
 
}
