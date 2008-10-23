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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "spsynonymydetermination")
public class SpSynonymyDetermination extends DataModelObjBase
{
    protected Integer         spSynonymyDeterminationId;
    protected SpTaxonSynonymy taxonSynonymy;
    protected Determination   oldDetermination;
    protected Determination   newDetermination;
    
    /**
     * 
     */
    public SpSynonymyDetermination()
    {
        //nothing.
    }
    
    
    
    /**
     * @return the spSynonymyDeterminationId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpSynonymyDeterminationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpSynonymyDeterminationId()
    {
        return spSynonymyDeterminationId;
    }



    /**
     * @param spSynonymyDeterminationId the spSynonymyDeterminationId to set
     */
    public void setSpSynonymyDeterminationId(Integer spSynonymyDeterminationId)
    {
        this.spSynonymyDeterminationId = spSynonymyDeterminationId;
    }



    /**
     * @return the taxonSynonymy
     */
     @ManyToOne
     @JoinColumn(name = "TaxonSynonymyId", unique = false, nullable = false, insertable = true, updatable = true)
     public SpTaxonSynonymy getTaxonSynonymy()
    {
        return taxonSynonymy;
    }



    /**
     * @param taxonSynonymy the taxonSynonymy to set
     */
    public void setTaxonSynonymy(SpTaxonSynonymy taxonSynonymy)
    {
        this.taxonSynonymy = taxonSynonymy;
    }



    /**
     * @return the oldDetermination
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OldDeterminationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Determination getOldDetermination()
    {
        return oldDetermination;
    }



    /**
     * @param oldDetermination the oldDetermination to set
     */
    public void setOldDetermination(Determination oldDetermination)
    {
        this.oldDetermination = oldDetermination;
    }



    /**
     * @return the newDetermination
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "NewDeterminationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Determination getNewDetermination()
    {
        return newDetermination;
    }



    /**
     * @param newDetermination the newDetermination to set
     */
    public void setNewDetermination(Determination newDetermination)
    {
        this.newDetermination = newDetermination;
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpSynonymyDetermination.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spSynonymyDeterminationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return 528;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spSynonymyDeterminationId = null;
        taxonSynonymy = null;
        oldDetermination = null;
        newDetermination = null;
    }

}
