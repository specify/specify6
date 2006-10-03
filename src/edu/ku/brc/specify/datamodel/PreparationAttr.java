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
package edu.ku.brc.specify.datamodel;

import java.util.Date;

import edu.ku.brc.dbsupport.AttributeIFace;

/**
 * 
 */
public class PreparationAttr extends DataModelObjBase implements AttributeIFace, java.io.Serializable
{

    // Fields

    protected Long      attrId;
    protected String       strValue;
    protected Double       dblValue;
    protected AttributeDef definition;
    protected Preparation  preparation;

    // Constructors

    /** default constructor */
    public PreparationAttr()
    {
    }

    /** constructor with id */
    public PreparationAttr(Long attrId)
    {
        this.attrId = attrId;
    }

    // Initializer
    public void initialize()
    {
        attrId = null;
        strValue = null;
        dblValue = null;
        timestampCreated = new Date();
        timestampModified = null;
        definition = null;
        preparation = null;
    }

    // End Initializer

    // Property accessors

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#getAttrId()
     */
    public Long getAttrId()
    {
        return this.attrId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#setAttrId(java.lang.Integer)
     */
    public void setAttrId(Long attrId)
    {
        this.attrId = attrId;
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
        {           
            throw new RuntimeException("Attribute being accessed as ["+AttributeIFace.FieldType.getString(type.getType())+"] when it is of type["+AttributeIFace.FieldType.getString(definition.getDataType())+"]");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AttributeIFace#getStrValue()
     */
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
    public Preparation getPreparation()
    {
        return this.preparation;
    }

    public void setPreparation(Preparation preparation)
    {
        this.preparation = preparation;
    }

    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 64;
    }

}
