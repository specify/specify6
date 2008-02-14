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

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.forms.persist.FormCell;
import edu.ku.brc.ui.forms.persist.FormCellIFace;
import edu.ku.brc.ui.forms.persist.FormRowIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 25, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spuirow")
@org.hibernate.annotations.Table(appliesTo="spuirow", indexes =
    {   @Index (name="SpUIRowRowNumIDX", columnNames={"RowNum"})
    })
public class SpUIRow implements java.io.Serializable, FormRowIFace, Comparable<SpUIRow>
{
    protected Integer       spUIRowId;
    protected Short         rowNum;           // The used for ordering the rows
    protected Set<SpUICell> spCells;
    
    protected SpUIViewDef   spViewDef;
    
    protected Vector<FormCellIFace> cells;
    
    /**
     * 
     */
    public SpUIRow()
    {
        // no op
    }

    /**
     * @return the spUIRowId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpUIRowID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpUIRowId()
    {
        return spUIRowId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    public void initialize()
    {
        spUIRowId = null;
        rowNum    = null;
        spCells   = new HashSet<SpUICell>();
        spViewDef   = null;
    }
    
    /**
     * @return the cells
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "spRow")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<SpUICell> getSpCells()
    {
        return spCells;
    }

    /**
     * @param cells the cells to set
     */
    public void setSpCells(Set<SpUICell> spCells)
    {
        this.spCells = spCells;
    }

    /**
     * @return the rowId
     */
    @Column(name = "RowNum", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getRowNum()
    {
        return rowNum;
    }

    /**
     * @param rowId the rowId to set
     */
    public void setRowNum(Short rowNum)
    {
        this.rowNum = rowNum;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormRowIFace#setRowNumber(byte)
     */
    public void setRowNumber(byte num)
    {
        this.rowNum = (short)num;
    }

    /**
     * @param spUIRowId the spUIRowId to set
     */
    public void setSpUIRowId(Integer spUIRowId)
    {
        this.spUIRowId = spUIRowId;
    }

    /**
     * @return the viewDef
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpUIAltViewID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpUIViewDef getSpViewDef()
    {
        return spViewDef;
    }

    /**
     * @param viewDef the viewDef to set
     */
    public void setSpViewDef(SpUIViewDef viewDef)
    {
        this.spViewDef = viewDef;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    public Class<?> getDataClass()
    {
        return SpUIRow.class;
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
        return 510;
    }
    
    /**
     * Helper.
     * @param cell the cell to be added to the set and the vector
     * @return the same cell
     */
    public FormCellIFace addSpCell(final SpUICell cell)
    {
        cell.setSpRow(this);
        spCells.add(cell);
        
        return cell;
    }


    //-----------------------------------------------------------
    //-- FormRowIFace
    //-----------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormRowIFace#addCell(edu.ku.brc.ui.forms.persist.FormCell)
     */
    public FormCellIFace addCell(FormCellIFace cell)
    {
        if (cells == null)
        {
            cells = new Vector<FormCellIFace>();
        }
        cells.add(cell);
        
        if (cell instanceof SpUICell)
        {
            addSpCell((SpUICell)cell);
        }
        return cell;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormRowIFace#cleanUp()
     */
    public void cleanUp()
    {
        // no op
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormRowIFace#getCells()
     */
    public Vector<FormCellIFace> getCells()
    {
        return cells;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormRowIFace#setCells(java.util.Vector)
     */
    public void setCells(Vector<FormCellIFace> cells)
    {
        this.cells = cells;
    }
    

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(SpUIRow row)
    {
        return rowNum.compareTo(row.rowNum);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        SpUIRow formRow = (SpUIRow)super.clone();
        formRow.cells   = new Vector<FormCellIFace>();
        for (FormCellIFace cell : cells)
        {
            formRow.cells.add((FormCell)cell.clone());
        }
        return formRow; 
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormRowIFace#toXML(java.lang.StringBuffer)
     */
    public void toXML(StringBuilder sb)
    {
        if (cells != null && cells.size() > 0)
        {
            int ident = 12;
            XMLHelper.indent(sb, ident);
            sb.append("<rows>\n");
            for (FormCellIFace cell : cells)
            {
                cell.toXML(sb);
            }
            XMLHelper.indent(sb, ident);
            sb.append("</rows>\n");
        }
    }
}
