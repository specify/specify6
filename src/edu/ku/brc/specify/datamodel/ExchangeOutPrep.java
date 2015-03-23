/* Copyright (C) 2015, University of Kansas Center for Research
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

import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "exchangeoutprep")
@org.hibernate.annotations.Table(appliesTo="exchangeoutprep", indexes =
    {   @Index (name="ExchgOutPrepDspMemIDX", columnNames={"DisciplineID"})
    })
public class ExchangeOutPrep extends DisciplineMember implements java.io.Serializable, Comparable<ExchangeOutPrep>
{
    // Fields    
    protected Integer                       exchangeOutPrepId;
    protected Integer                       quantity;
    protected String                        descriptionOfMaterial;
    protected String                        comments;          
    protected String                        text1;           
    protected String                        text2;   
    protected Integer                       number1;
    protected Preparation                   preparation;
    protected ExchangeOut                   exchangeOut;

    // Constructors

    /** default constructor */
    public ExchangeOutPrep() {
        //
    }
    
    /** constructor with id */
    public ExchangeOutPrep(Integer exchangeOutPrepId) 
    {
        this.exchangeOutPrepId = exchangeOutPrepId;
    }
   
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        exchangeOutPrepId = null;
        quantity = null;
        descriptionOfMaterial = null;
        comments = null;
        text1 = null;
        text2 = null;
        preparation = null;
        exchangeOut = null;
        exchangeOut = null;
        number1 = null;
    }
    // End Initializer

    /**
     * PrimaryKey
     */
    @Id
    @GeneratedValue
    @Column(name = "ExchangeOutPrepID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getExchangeOutPrepId() {
        return this.exchangeOutPrepId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.exchangeOutPrepId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return ExchangeOutPrep.class;
    }
    
    public void setExchangeOutPrepId(Integer exchangeOutPrepId) {
        this.exchangeOutPrepId = exchangeOutPrepId;
    }

    /**
     * 
     */
    @Column(name = "Quantity", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getQuantity() 
    {
        return this.quantity == null ? 0 : this.quantity;
    }
    
    public void setQuantity(Integer quantity) 
    {
        this.quantity = quantity;
    }

    /**
     * Description of material (intended to be used for non-cataloged items, i.e. when PreparationID is null)
     */
    @Column(name = "DescriptionOfMaterial", unique = false, nullable = true, insertable = true, updatable = true)
    public String getDescriptionOfMaterial() {
        return this.descriptionOfMaterial;
    }
    
    public void setDescriptionOfMaterial(String descriptionOfMaterial) {
        this.descriptionOfMaterial = descriptionOfMaterial;
    }

    /**
     * Comments on item 
     */
    @Lob
    @Column(name = "Comments", unique = false, nullable = true, insertable = true, updatable = true)
    public String getComments() {
        return this.comments;
    }
    
    public void setComments(String outComments) {
        this.comments = outComments;
    }

    /**
     * Comments on item when returned
     */
    @Lob
    @Column(name = "Text1", unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String inComments) {
        this.text1 = inComments;
    }

    /**
     * @return the receivedComments
     */
    @Lob
    @Column(name = "Text2", unique = false, nullable = true, insertable = true, updatable = true)
    public String getText2()
    {
        return text2;
    }

    /**
     * @param receivedComments the receivedComments to set
     */
    public void setText2(String receivedComments)
    {
        this.text2 = receivedComments;
    }

    /**
     * @return the number1
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber1()
    {
        return number1;
    }

    /**
     * @param number1 the number1 to set
     */
    public void setNumber1(Integer number1)
    {
        this.number1 = number1;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PreparationID", unique = false, nullable = true, insertable = true, updatable = true)
    public Preparation getPreparation() {
        return this.preparation;
    }
    
    public void setPreparation(Preparation preparation) {
        this.preparation = preparation;
    }

    /**
     * ExchangeOut containing the Preparation
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ExchangeOutID", unique = false, nullable = true, insertable = true, updatable = true)
    public ExchangeOut getExchangeOut() {
        return this.exchangeOut;
    }
    
    public void setExchangeOut(ExchangeOut exchangeOut) 
    {
        this.exchangeOut = exchangeOut;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return ExchangeOut.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return exchangeOut != null ? exchangeOut.getId() : null;
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
        return 141;
    }
    
    //----------------------------------------------------------------------
    //-- Comparable Interface
    //----------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ExchangeOutPrep obj)
    {
        return timestampCreated != null && obj != null && obj.timestampCreated != null ? timestampCreated.compareTo(obj.timestampCreated) : 0;
    }
}
