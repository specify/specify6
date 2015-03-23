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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 11, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "autonumberingscheme")
@org.hibernate.annotations.Table(appliesTo="autonumberingscheme", indexes =
    {   @Index (name="SchemeNameIDX", columnNames={"SchemeName"})
    })
public class AutoNumberingScheme extends DataModelObjBase implements java.io.Serializable
{
    protected Integer         autoNumberingSchemeId;

    protected Integer         tableNumber; // Table Id number that matched DBTableIDMgr
    protected String          schemeName;
    protected String          schemeClassName;
    protected String          formatName;
    protected Boolean         isNumericOnly;
    
    protected Set<Collection> collections;
    protected Set<Division>   divisions;
    protected Set<Discipline> disciplines;
    
    // Transient 
    protected UIFieldFormatterIFace formatter;
    
    public AutoNumberingScheme()
    {
        // no op
    }
    
    /** constructor with id */
    public AutoNumberingScheme(Integer autoNumberingSchemeId) 
    {
        this.autoNumberingSchemeId = autoNumberingSchemeId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        autoNumberingSchemeId = null;
        schemeName           = null;
        schemeClassName      = null;
        formatName           = null;
        collections          = new HashSet<Collection>();
        divisions            = new HashSet<Division>();
        disciplines          = new HashSet<Discipline>();
        isNumericOnly        = false;
        
        formatter            = null;
    }
    
    // End Initializer
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.autoNumberingSchemeId;
    }
    
    @Id
    @GeneratedValue
    @Column(name = "AutoNumberingSchemeID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getAutoNumberingSchemeId()
    {
        return autoNumberingSchemeId;
    }

    public void setAutoNumberingSchemeId(Integer autoNumberingSchemeId)
    {
        this.autoNumberingSchemeId = autoNumberingSchemeId;
    }

    @Column(name = "SchemeName", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getSchemeName()
    {
        return schemeName;
    }

    public void setSchemeName(String schemeName)
    {
        this.schemeName = schemeName;
    }

    @Column(name = "SchemeClassName", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getSchemeClassName()
    {
        return schemeClassName;
    }

    public void setSchemeClassName(String schemeClassName)
    {
        this.schemeClassName = schemeClassName;
    }

    /**
     * @return the formatName
     */
    @Column(name = "FormatName", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getFormatName()
    {
        return formatName;
    }

    /**
     * @param formatName the formatName to set
     */
    public void setFormatName(String formatName)
    {
        this.formatName = formatName;
    }

    @Column(name="IsNumericOnly", unique=false, nullable=false, updatable=true, insertable=true)
    public Boolean getIsNumericOnly()
    {
        return isNumericOnly;
    }

    public void setIsNumericOnly(Boolean isNumericOnly)
    {
        this.isNumericOnly = isNumericOnly;
    }

    /**
     * @return the type
     */
    @Column(name="TableNumber", unique=false, nullable=false, updatable=true, insertable=true)
    public Integer getTableNumber()
    {
        return tableNumber;
    }

    /**
     * @param type the type to set
     */
    public void setTableNumber(Integer tableNumber)
    {
        this.tableNumber = tableNumber;
    }

    @ManyToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="numberingSchemes")
    @Cascade( {CascadeType.SAVE_UPDATE} )
    public Set<Collection> getCollections()
    {
        return collections;
    }

    public void setCollections(Set<Collection> collections)
    {
        this.collections = collections;
    }
    
    @ManyToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="numberingSchemes")
    @Cascade( {CascadeType.SAVE_UPDATE} )
    public Set<Division> getDivisions()
    {
        return divisions;
    }

    public void setDivisions(Set<Division> divisions)
    {
        this.divisions = divisions;
    }
    
    @ManyToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="numberingSchemes")
    @Cascade( {CascadeType.SAVE_UPDATE} )
    public Set<Discipline> getDisciplines()
    {
        return disciplines;
    }

    public void setDisciplines(Set<Discipline> disciplines)
    {
        this.disciplines = disciplines;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    public String toString()
    {
        return schemeName;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return AutoNumberingScheme.class;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Override
    @Transient
    public boolean isChangeNotifier()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return schemeName;
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
     * @return the formatter
     */
    @Transient
    public UIFieldFormatterIFace getFormatter()
    {
        return formatter;
    }

    /**
     * @param formatter the formatter to set
     */
    public void setFormatter(UIFieldFormatterIFace formatter)
    {
        this.formatter = formatter;
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 97;
    }
}
