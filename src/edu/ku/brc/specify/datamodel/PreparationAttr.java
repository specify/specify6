/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import edu.ku.brc.dbsupport.AttributeIFace;

/**
 * 
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "preparationattr")
@org.hibernate.annotations.Table(appliesTo="preparationattr", indexes =
    {   @Index (name="PrepAttrColMemIDX", columnNames={"CollectionMemberID"})
    })
public class PreparationAttr extends CollectionMember implements AttributeIFace, java.io.Serializable
{

    // Fields

    protected Integer      attrId;
    protected String       strValue;
    protected Double       dblValue;
    protected AttributeDef definition;
    protected Preparation  preparation;

    // Constructors

    /** default constructor */
    public PreparationAttr()
    {
        //
    }

    /** constructor with id */
    public PreparationAttr(Integer attrId)
    {
        this.attrId = attrId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        attrId = null;
        strValue = null;
        dblValue = null;
        definition = null;
        preparation = null;
    }

    // End Initializer

    // Property accessors

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#getAttrId()
     */
    @Id
    @GeneratedValue
    @Column(name = "AttrID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getAttrId()
    {
        return this.attrId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#setAttrId(java.lang.Integer)
     */
    public void setAttrId(Integer attrId)
    {
        this.attrId = attrId;
    }

    @Transient
    @Override
    public Integer getId()
    {
        return attrId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return PreparationAttr.class;
    }

    /**
     * Verifies that the Attr type if correct 
     * @param type the type to check
     */
    protected void verifyType(AttributeIFace.FieldType type)
    {
        if (definition == null)
        {
            throw new RuntimeException("Attribute being accessed without a definition.");
        }
        if (definition.getDataType() != type.getType())
        //
        {           
            throw new RuntimeException("Attribute being accessed as ["+AttributeIFace.FieldType.getString(type.getType())+"] when it is of type["+AttributeIFace.FieldType.getString(definition.getDataType())+"]");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#getStrValue()
     */
    @Column(name = "StrValue", unique = false, nullable = true, insertable = true, updatable = true)
    public String getStrValue()
    {
        //verifyType(AttributeIFace.FieldType.StringType);
        
        return this.strValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#setStrValue(java.lang.String)
     */
    public void setStrValue(String strValue)
    {
        //verifyType(AttributeIFace.FieldType.StringType);
        this.strValue = strValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#getDblValue()
     */
    @Column(name = "DoubleValue", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getDblValue()
    {
        //verifyType(AttributeIFace.FieldType.DoubleType);
        return dblValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#setDblValue(java.lang.Double)
     */
    public void setDblValue(Double value)
    {
        //verifyType(AttributeIFace.FieldType.DoubleType);
        dblValue = value;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#getFloatValue()
     */
    @Transient
    public Float getFloatValue()
    {
        //verifyType(AttributeIFace.FieldType.FloatType);
        return new Float(dblValue);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#setFloatValue(java.lang.Float)
     */
    public void setFloatValue(Float value)
    {
        //verifyType(AttributeIFace.FieldType.FloatType);
        dblValue = new Double(value);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#getIntValue()
     */
    @Transient
    public Integer getIntValue()
    {
        //verifyType(AttributeIFace.FieldType.IntegerType);
        return new Integer(dblValue.intValue());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#setIntValue(java.lang.Integer)
     */
    public void setIntValue(Integer value)
    {
        //verifyType(AttributeIFace.FieldType.IntegerType);
        dblValue = new Double(value);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#getBoolValue()
     */
    @Transient
    public Boolean getBoolValue()
    {
        //verifyType(AttributeIFace.FieldType.BooleanType);
        return new Boolean(dblValue.intValue() == 1);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#setBoolValue(java.lang.Boolean)
     */
    public void setBoolValue(Boolean value)
    {
        //verifyType(AttributeIFace.FieldType.BooleanType);
        dblValue = new Double(value ? 1.0 : 0.0);
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AttributeDefID", unique = false, nullable = false, insertable = true, updatable = true)
    public AttributeDef getDefinition()
    {
        return this.definition;
    }

    public void setDefinition(AttributeDef definition)
    {
        this.definition = definition;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PreparationId", unique = false, nullable = false, insertable = true, updatable = true)
    public Preparation getPreparation()
    {
        return this.preparation;
    }

    public void setPreparation(Preparation preparation)
    {
        this.preparation = preparation;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Preparation.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return preparation != null ? preparation.getId() : null;
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
        return 64;
    }

}
