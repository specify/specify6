/* Copyright (C) 2013, University of Kansas Center for Research
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

import java.math.BigDecimal;

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
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 16, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "localitydetail")
public class LocalityDetail extends DataModelObjBase
{
    // Fields    
    protected Integer               localityDetailId;
    protected String                baseMeridian;
    protected String                rangeDesc;
    protected String                rangeDirection;
    protected String                township;
    protected String                townshipDirection;
    protected String                section;
    protected String                sectionPart;
    protected String                gml;
    protected String                nationalParkName;
    protected String                islandGroup;
    protected String                island;
    protected String                waterBody;
    protected String                drainage;
    
    // Depths primarily for Paleo
    protected Double                startDepth;
    protected Byte                  startDepthUnit;
    protected String                startDepthVerbatim;
    
    protected Double                endDepth;
    protected Byte                  endDepthUnit;
    protected String                endDepthVerbatim;
    
    // HUC Code for fishes
    protected String                hucCode;
    
    protected String                text1;
    protected String                text2;
    protected Double                number1;
    protected Double                number2;
    protected Boolean               yesNo1;
    protected Boolean               yesNo2;

    
    // UTM Fields
    protected BigDecimal            utmEasting;
    protected BigDecimal            utmNorthing;
    protected Integer               utmFalseEasting;
    protected Integer               utmFalseNorthing;
    protected String                utmDatum;
    protected Short                 utmZone;
    protected BigDecimal            utmOrigLatitude;
    protected BigDecimal            utmOrigLongitude;
    protected BigDecimal            utmScale;
    protected String                mgrsZone;            // GZD only, precision level 6 x 8 (in most cases)
    
     
    protected Locality              locality;


    // Constructors

    /** default constructor */
    public LocalityDetail()
    {
        // do nothing
    }
    
    /** constructor with id */
    public LocalityDetail(Integer localityDetailId) 
    {
        this.localityDetailId = localityDetailId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        localityDetailId = null;
        baseMeridian = null;
        rangeDesc       = null;
        rangeDirection = null;
        township    = null;
        townshipDirection = null;
        section     = null;
        sectionPart = null;
        gml         = null;
        nationalParkName = null;
        islandGroup = null;
        island      = null;
        waterBody   = null;
        drainage    = null;
        
        startDepth         = null;
        startDepthUnit     = null;
        startDepthVerbatim = null;
        endDepth           = null;
        endDepthUnit       = null;
        endDepthVerbatim   = null;
        
        locality    = null;
        
        utmEasting       = null;
        utmNorthing      = null;
        utmFalseEasting  = null;
        utmFalseNorthing = null;
        utmDatum         = null;
        utmZone          = null;
        utmOrigLatitude  = null;
        utmOrigLongitude = null;
        utmScale         = null;
        mgrsZone         = null;
        
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "LocalityDetailID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getLocalityDetailId() 
    {
        return this.localityDetailId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.localityDetailId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Locality.class;
    }
    
    /**
     * @param localityDetailId
     */
    public void setLocalityDetailId(Integer localityDetailId) 
    {
        this.localityDetailId = localityDetailId;
    }

    /**
     *      * BaseMeridian for the Range/Township/Section data
     */
    @Column(name = "BaseMeridian", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getBaseMeridian() {
        return this.baseMeridian;
    }
    
    public void setBaseMeridian(String baseMeridian) {
        this.baseMeridian = baseMeridian;
    }

    /**
     *      * The Range of a legal description
     */
    @Column(name = "RangeDesc", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getRangeDesc() {
        return this.rangeDesc;
    }
    
    public void setRangeDesc(String rangeDesc) {
        this.rangeDesc = rangeDesc;
    }

    /**
     * 
     */
    @Column(name = "RangeDirection", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getRangeDirection() {
        return this.rangeDirection;
    }
    
    public void setRangeDirection(String rangeDirection) {
        this.rangeDirection = rangeDirection;
    }

    /**
     *      * The Township of a legal description
     */
    @Column(name = "Township", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getTownship() {
        return this.township;
    }
    
    public void setTownship(String township) {
        this.township = township;
    }

    /**
     * 
     */
    @Column(name = "TownshipDirection", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getTownshipDirection() {
        return this.townshipDirection;
    }
    
    public void setTownshipDirection(String townshipDirection) {
        this.townshipDirection = townshipDirection;
    }

    /**
     *      * The Section of a legal description
     */
    @Column(name = "Section", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getSection() {
        return this.section;
    }
    
    public void setSection(String section) {
        this.section = section;
    }

    /**
     * 
     */
    @Column(name = "SectionPart", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getSectionPart() {
        return this.sectionPart;
    }
    
    public void setSectionPart(String sectionPart) {
        this.sectionPart = sectionPart;
    }

    /**
     * @return
     */
    @Lob
    @Column(name = "GML")
    public String getGml()
    {
        return gml;
    }

    public void setGml(String gml)
    {
        this.gml = gml;
    }

    /**
     * 
     */
    @Column(name = "NationalParkName", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getNationalParkName() {
        return this.nationalParkName;
    }
    
    public void setNationalParkName(String nationalParkName) {
        this.nationalParkName = nationalParkName;
    }

    /**
     * 
     */
    @Column(name = "IslandGroup", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getIslandGroup() {
        return this.islandGroup;
    }
    
    public void setIslandGroup(String islandGroup) {
        this.islandGroup = islandGroup;
    }

    /**
     * 
     */
    @Column(name = "Island", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getIsland() {
        return this.island;
    }
    
    public void setIsland(String island) {
        this.island = island;
    }

    /**
     * 
     */
    @Column(name = "WaterBody", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getWaterBody() {
        return this.waterBody;
    }
    
    public void setWaterBody(String waterBody) {
        this.waterBody = waterBody;
    }

    /**
     * 
     */
    @Column(name = "Drainage", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getDrainage() {
        return this.drainage;
    }
    
    public void setDrainage(String drainage) {
        this.drainage = drainage;
    }

    /**
     * @return the startDepth
     */
    @Column(name = "StartDepth", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getStartDepth()
    {
        return startDepth;
    }

    /**
     * @param startDepth the startDepth to set
     */
    public void setStartDepth(Double startDepth)
    {
        this.startDepth = startDepth;
    }

    /**
     * @return the startDepthUnit
     */
    @Column(name = "StartDepthUnit", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getStartDepthUnit()
    {
        return startDepthUnit;
    }

    /**
     * @param startDepthUnit the startDepthUnit to set
     */
    public void setStartDepthUnit(Byte startDepthUnit)
    {
        this.startDepthUnit = startDepthUnit;
    }

    /**
     * @return the startDepthVerbatim
     */
    @Column(name = "StartDepthVerbatim", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getStartDepthVerbatim()
    {
        return startDepthVerbatim;
    }

    /**
     * @param startDepthVerbatim the startDepthVerbatim to set
     */
    public void setStartDepthVerbatim(String startDepthVerbatim)
    {
        this.startDepthVerbatim = startDepthVerbatim;
    }

    /**
     * @return the endDepth
     */
    @Column(name = "EndDepth", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getEndDepth()
    {
        return endDepth;
    }

    /**
     * @param endDepth the endDepth to set
     */
    public void setEndDepth(Double endDepth)
    {
        this.endDepth = endDepth;
    }

    /**
     * @return the endDepthUnit
     */
    @Column(name = "EndDepthUnit", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getEndDepthUnit()
    {
        return endDepthUnit;
    }

    /**
     * @param endDepthUnit the endDepthUnit to set
     */
    public void setEndDepthUnit(Byte endDepthUnit)
    {
        this.endDepthUnit = endDepthUnit;
    }

    /**
     * @return the endDepthVerbatim
     */
    @Column(name = "EndDepthVerbatim", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getEndDepthVerbatim()
    {
        return endDepthVerbatim;
    }

    /**
     * @param endDepthVerbatim the endDepthVerbatim to set
     */
    public void setEndDepthVerbatim(String endDepthVerbatim)
    {
        this.endDepthVerbatim = endDepthVerbatim;
    }

    /**
     * * User definable
     */
    @Lob
    @Column(name = "Text1", length = 65535)
    public String getText1()
    {
        return this.text1;
    }

    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    /**
     * * User definable
     */
    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2()
    {
        return this.text2;
    }

    public void setText2(String text2)
    {
        this.text2 = text2;
    }

    /**
     * * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber1()
    {
        return this.number1;
    }

    public void setNumber1(Double number1)
    {
        this.number1 = number1;
    }

    /**
     * * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Double getNumber2()
    {
        return this.number2;
    }

    public void setNumber2(Double number2)
    {
        this.number2 = number2;
    }

    /**
     * * User definable
     */
    @Column(name = "YesNo1", unique = false, nullable = true, updatable = true, insertable = true)
    public Boolean getYesNo1()
    {
        return this.yesNo1;
    }

    public void setYesNo1(Boolean yesNo1)
    {
        this.yesNo1 = yesNo1;
    }

    /**
     * * User definable
     */
    @Column(name = "YesNo2", unique = false, nullable = true, updatable = true, insertable = true)
    public Boolean getYesNo2()
    {
        return this.yesNo2;
    }

    public void setYesNo2(Boolean yesNo2)
    {
        this.yesNo2 = yesNo2;
    }
    
    /**
     * @return the hucCode
     */
    @Column(name = "HucCode", unique = false, nullable = true, updatable = true, insertable = true, length=16)
    public String getHucCode()
    {
        return hucCode;
    }

    /**
     * @param hucCode the hucCode to set
     */
    public void setHucCode(String hucCode)
    {
        this.hucCode = hucCode;
    }

    /**
     * @return the utmEasting
     */
    @Column(name = "UtmEasting", unique = false, nullable = true, updatable = true, insertable = true, precision = 19, scale = 2)
    public BigDecimal getUtmEasting()
    {
        return utmEasting;
    }

    /**
     * @param utmEasting the utmEasting to set
     */
    public void setUtmEasting(BigDecimal utmEasting)
    {
        this.utmEasting = utmEasting;
    }

    /**
     * @return the utmNorthing
     */
    @Column(name = "UtmNorthing", unique = false, nullable = true, updatable = true, insertable = true, precision = 19, scale = 2)
    public BigDecimal getUtmNorthing()
    {
        return utmNorthing;
    }

    /**
     * @param utmNorthing the utmNorthing to set
     */
    public void setUtmNorthing(BigDecimal utmNorthing)
    {
        this.utmNorthing = utmNorthing;
    }

    /**
     * @return the utmFalseEasting
     */
    @Column(name = "UtmFalseEasting", unique = false, nullable = true, updatable = true, insertable = true)
    public Integer getUtmFalseEasting()
    {
        return utmFalseEasting;
    }

    /**
     * @param utmFalseEasting the utmFalseEasting to set
     */
    public void setUtmFalseEasting(Integer utmFalseEasting)
    {
        this.utmFalseEasting = utmFalseEasting;
    }

    /**
     * @return the utmFalseNorthing
     */
    @Column(name = "UtmFalseNorthing", unique = false, nullable = true, updatable = true, insertable = true)
    public Integer getUtmFalseNorthing()
    {
        return utmFalseNorthing;
    }

    /**
     * @param utmFalseNorthing the utmFalseNorthing to set
     */
    public void setUtmFalseNorthing(Integer utmFalseNorthing)
    {
        this.utmFalseNorthing = utmFalseNorthing;
    }

    /**
     * @return the utmDatum
     */
    @Column(name = "UtmDatum", unique = false, nullable = true, updatable = true, insertable = true, length = 32)
    public String getUtmDatum()
    {
        return utmDatum;
    }

    /**
     * @param utmDatum the utmDatum to set
     */
    public void setUtmDatum(String utmDatum)
    {
        this.utmDatum = utmDatum;
    }

    /**
     * @return the utmZone
     */
    @Column(name = "UtmZone", unique = false, nullable = true, updatable = true, insertable = true)
    public Short getUtmZone()
    {
        return utmZone;
    }

    /**
     * @param utmZone the utmZone to set
     */
    public void setUtmZone(Short utmZone)
    {
        this.utmZone = utmZone;
    }

    /**
     * @return the utmOrigLatitude
     */
    @Column(name = "UtmOrigLatitude", unique = false, nullable = true, updatable = true, insertable = true, precision = 19, scale = 2)
    public BigDecimal getUtmOrigLatitude()
    {
        return utmOrigLatitude;
    }

    /**
     * @param utmOrigLatitude the utmOrigLatitude to set
     */
    public void setUtmOrigLatitude(BigDecimal utmOrigLatitude)
    {
        this.utmOrigLatitude = utmOrigLatitude;
    }

    /**
     * @return the utmOrigLongitude
     */
    @Column(name = "UtmOrigLongitude", unique = false, nullable = true, updatable = true, insertable = true, precision = 19, scale = 2)
    public BigDecimal getUtmOrigLongitude()
    {
        return utmOrigLongitude;
    }

    /**
     * @param utmOrigLongitude the utmOrigLongitude to set
     */
    public void setUtmOrigLongitude(BigDecimal utmOrigLongitude)
    {
        this.utmOrigLongitude = utmOrigLongitude;
    }

    /**
     * @return the utmScale
     */
    @Column(name = "UtmScale", unique = false, nullable = true, updatable = true, insertable = true, precision = 20, scale = 10)
    public BigDecimal getUtmScale()
    {
        return utmScale;
    }

    /**
     * @param utmScale the utmScale to set
     */
    public void setUtmScale(BigDecimal utmScale)
    {
        this.utmScale = utmScale;
    }

    /**
     * @return the mgrsZone
     */
    @Column(name = "MgrsZone", unique = false, nullable = true, updatable = true, insertable = true, length = 4)
    public String getMgrsZone()
    {
        return mgrsZone;
    }

    /**
     * @param mgrsZone the mgrsZone to set
     */
    public void setMgrsZone(String mgrsZone)
    {
        this.mgrsZone = mgrsZone;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isRestrictable()
     */
    @Transient
    @Override
    public boolean isRestrictable()
    {
        return true;
    }  
    
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LocalityID", unique = false, nullable = true, insertable = true, updatable = true)
    public Locality getLocality() 
    {
        return this.locality;
    }
    
    public void setLocality(Locality locality) 
    {
        this.locality = locality;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Locality.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return locality != null ? locality.getId() : null;
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
        return 124;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        LocalityDetail ld = (LocalityDetail)super.clone();
        
        ld.localityDetailId = null;
        ld.locality         = null;
        
        return ld;
    }

    /**
     * @param o
     * @return true if 'non-system' fields all match.
     * 
     */
    public boolean matches(LocalityDetail o)
    {
        if (o == null)
        {
        	return false;
        }
        
    	return
        	((baseMeridian == null && o.baseMeridian == null) || ((baseMeridian != null && o.baseMeridian != null) && baseMeridian.equals(o.baseMeridian))) &&
        	((rangeDesc == null && o.rangeDesc == null) || ((rangeDesc != null && o.rangeDesc != null) && rangeDesc.equals(o.rangeDesc))) &&
            ((rangeDirection == null && o.rangeDirection == null) || ((rangeDirection != null && o.rangeDirection != null) && rangeDirection.equals(o.rangeDirection))) &&
            ((township == null && o.township == null) || ((township != null && o.township != null) && township.equals(o.township))) &&
            ((townshipDirection == null && o.townshipDirection == null) || ((townshipDirection != null && o.townshipDirection != null)
            		&& townshipDirection.equals(o.townshipDirection))) &&
            ((section == null && o.section == null) || ((section != null && o.section != null)
            		&& section.equals(o.section))) &&
            ((sectionPart == null && o.sectionPart == null) || ((sectionPart != null && o.sectionPart != null)
            		&& sectionPart.equals(o.sectionPart))) &&
            ((gml == null && o.gml == null) || ((gml != null && o.gml != null) && gml.equals(o.gml))) &&
            ((nationalParkName == null && o.nationalParkName == null) || ((nationalParkName != null && o.nationalParkName != null)
                    && nationalParkName.equals(o.nationalParkName))) &&
            ((islandGroup == null && o.islandGroup == null) || ((islandGroup != null && o.islandGroup != null)
                    && islandGroup.equals(o.islandGroup))) &&
            ((island == null && o.island == null) || ((island != null && o.island != null)
            		&& island.equals(o.island))) &&
            ((drainage == null && o.drainage == null) || ((drainage != null && o.drainage != null)
                    && drainage.equals(o.drainage))) &&
            ((utmDatum == null && o.utmDatum == null) || ((utmDatum != null && o.utmDatum != null)
                    && utmDatum.equals(o.utmDatum))) &&
            ((utmScale == null && o.utmScale == null) || ((utmScale != null && o.utmScale != null) && utmScale.compareTo(o.utmScale) == 0)) &&
            ((hucCode == null && o.hucCode == null) || ((hucCode != null && o.hucCode != null) && hucCode.equals(o.hucCode))) &&
            ((text1 == null && o.text1 == null) || ((text1 != null && o.text1 != null) && text1.equals(o.text1))) &&
            ((text2 == null && o.text2 == null) || ((text2 != null && o.text2 != null) && text2.equals(o.text2))) &&
            ((number1 == null && o.number1 == null) || ((number1 != null && o.number1 != null) && number1.equals(o.number1))) &&
            ((number2 == null && o.number2 == null) || ((number2 != null && o.number2 != null) && number2.equals(o.number2))) &&
            ((yesNo1 == null && o.yesNo1 == null) || ((yesNo1 != null && o.yesNo1 != null) && yesNo1.equals(o.yesNo1))) &&
            ((yesNo2 == null && o.yesNo2 == null) || ((yesNo2 != null && o.yesNo2 != null) && yesNo2.equals(o.yesNo2))) &&
            ((utmEasting == null && o.utmEasting == null) || ((utmEasting != null && o.utmEasting != null) && utmEasting.compareTo(o.utmEasting) == 0)) &&
            ((utmNorthing == null && o.utmNorthing == null) || ((utmNorthing != null && o.utmNorthing != null) && utmNorthing.compareTo(o.utmNorthing) == 0)) &&
            ((utmFalseEasting == null && o.utmFalseEasting == null) || ((utmFalseEasting != null && o.utmFalseEasting != null) && utmFalseEasting.equals(o.utmFalseEasting))) &&
            ((utmFalseNorthing == null && o.utmFalseNorthing == null) || ((utmFalseNorthing != null && o.utmFalseNorthing != null) && utmFalseNorthing.equals(o.utmFalseNorthing))) &&
            ((utmZone == null && o.utmZone == null) || ((utmZone != null && o.utmZone != null) && utmZone.equals(o.utmZone))) &&
            ((utmOrigLatitude == null && o.utmOrigLatitude == null) || ((utmOrigLatitude != null && o.utmOrigLatitude != null) && utmOrigLatitude.compareTo(o.utmOrigLatitude) == 0)) &&
            ((utmOrigLongitude == null && o.utmOrigLongitude == null) || ((utmOrigLongitude != null && o.utmOrigLongitude != null) && utmOrigLongitude.compareTo(o.utmOrigLongitude) == 0)) &&
            ((waterBody == null && o.waterBody == null) || ((waterBody != null && o.waterBody != null) && waterBody.equals(o.waterBody)));
    }
    
}
