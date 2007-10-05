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

import org.hibernate.annotations.Index;

import edu.ku.brc.ui.forms.persist.FormColumnIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 27, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spuicolumn")
@org.hibernate.annotations.Table(appliesTo="spuicolumn", indexes =
    {   @Index (name="SpUIColumnNameIDX", columnNames={"Name"})
    })
public class SpUIColumn extends DataModelObjBase implements FormColumnIFace
{
    protected Integer spUIColumnId;
    protected String  name;
    protected String  label;
    protected String  dataObjFormatter;
    protected String  format;
    protected Integer colOrder;
    
    protected SpUIViewDef spViewDef;
    
    /**
     * 
     */
    public SpUIColumn()
    {
        // No op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spUIColumnId     = null;
        name             = null;
        label            = null;
        dataObjFormatter = null;
        format           = null; 
        colOrder         = null;
        spViewDef        = null;
    }
    
    /**
     * @return the spUIColumnId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpUIColumnID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpUIColumnId()
    {
        return spUIColumnId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormColumnIFace#getDataObjFormatter()
     */
    @Column(name = "DataObjFormatter", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getDataObjFormatter()
    {
        return dataObjFormatter;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormColumnIFace#getFormat()
     */
    @Column(name = "Format", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getFormat()
    {
        return format;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormColumnIFace#getLabel()
     */
    @Column(name = "Label", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getLabel()
    {
        return label;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormColumnIFace#getName()
     */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpUIView.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return spUIColumnId;
    }

    /**
     * @return the order
     */
    public Integer getColOrder()
    {
        return colOrder;
    }

    /**
     * @param order the order to set
     */
    public void setColOrder(Integer colOrder)
    {
        this.colOrder = colOrder;
    }

    /**
     * @param spUIColumnId the spUIColumnId to set
     */
    public void setSpUIColumnId(Integer spUIColumnId)
    {
        this.spUIColumnId = spUIColumnId;
    }

    /**
     * @param dataObjFormatter the dataObjFormatter to set
     */
    public void setDataObjFormatter(String dataObjFormatter)
    {
        this.dataObjFormatter = dataObjFormatter;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    

    /**
     * @return the viewDef
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpUIViewDefID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpUIViewDef getSpViewDef()
    {
        return spViewDef;
    }

    /**
     * @param viewDef the viewDef to set
     */
    public void setSpViewDef(SpUIViewDef spViewDef)
    {
        this.spViewDef = spViewDef;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
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
        return 512;
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
     * @param col
     */
    public void copyInto(final FormColumnIFace col)
    {
        name   = col.getName();
        label  = col.getLabel();
        dataObjFormatter = col.getDataObjFormatter();
        format = col.getFormat();
    }
}
