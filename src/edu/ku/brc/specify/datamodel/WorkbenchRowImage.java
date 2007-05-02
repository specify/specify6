/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
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

/**
 * A data class to hold images corresponding to WorkbenchRows.
 *
 * @author jstewart
 * @code_status Alpha
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "workbenchrowimage")
@org.hibernate.annotations.Proxy(lazy = false)
public class WorkbenchRowImage implements java.io.Serializable
{
    protected Long         workbenchRowImageId;
    protected Integer      order;
    protected byte[]       cardImageData;
    protected String       cardImageFullPath;
    protected WorkbenchRow workbenchRow;
    
    /**
     * Constructor (for JPA compliance).
     */
    public WorkbenchRowImage()
    {
        //
    }
    
    public void initialize()
    {
        workbenchRowImageId = null;
        order               = null;
        cardImageData       = null;
        cardImageFullPath   = null;
        workbenchRow        = null;
    }

    @Id
    @GeneratedValue
    @Column(name = "WorkbenchRowImageID", nullable = false)
    public Long getWorkbenchRowImageId()
    {
        return workbenchRowImageId;
    }

    public void setWorkbenchRowImageId(Long workbenchRowImageId)
    {
        this.workbenchRowImageId = workbenchRowImageId;
    }
    
    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    public Long getId()
    {
        return this.workbenchRowImageId;
    }
    
    @Lob
    @Column(name = "CardImageData", length=16000000)
    public byte[] getCardImageData()
    {
        return cardImageData;
    }

    public void setCardImageData(byte[] cardImageData)
    {
        this.cardImageData = cardImageData;
    }

    @Column(name="CardImageFullPath", length=255)
    public String getCardImageFullPath()
    {
        return cardImageFullPath;
    }

    public void setCardImageFullPath(String cardImageFullPath)
    {
        this.cardImageFullPath = cardImageFullPath;
    }

    @Column(name = "Order")
    public Integer getOrder()
    {
        return order;
    }

    public void setOrder(Integer order)
    {
        this.order = order;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "WorkbenchRowID", unique = false, nullable = false, insertable = true, updatable = true)
    public WorkbenchRow getWorkbenchRow()
    {
        return workbenchRow;
    }

    public void setWorkbenchRow(WorkbenchRow workbenchRow)
    {
        this.workbenchRow = workbenchRow;
    }
}