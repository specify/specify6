
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

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 28, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "conservrecommendation")
public class ConservRecommendation extends DataModelObjBase implements java.io.Serializable
{

    // Fields    

    protected Integer           conservRecommendationId;
    
    protected Calendar          completedDate;
    protected String            completedComments;
    
    protected String            remarks;
    
    protected ConservRecmdType  conservRecmdType;
    
    protected ConservEvent      lightRecommendationConservEvent;
    protected ConservEvent      displayRecommendationConservEvent;
    protected ConservEvent      otherRecommendationConservEvent;
    

    // Constructors

    /** default constructor */
    public ConservRecommendation()
    {
        //
    }

    /** constructor with id */
    public ConservRecommendation(Integer conservRecommendationId)
    {
        this.conservRecommendationId = conservRecommendationId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        conservRecommendationId = null;
        completedDate     = null;
        completedComments = null;
        remarks           = null;
        conservRecmdType  = null;
        
        lightRecommendationConservEvent    = null;
        displayRecommendationConservEvent  = null;
        otherRecommendationConservEvent    = null;

    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "ConservRecommendationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getConservRecommendationId()
    {
        return this.conservRecommendationId;
    }
    public void setConservRecommendationId(Integer conservRecommendationId)
    {
        this.conservRecommendationId = conservRecommendationId;
    }

    /**
     *
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "CompletedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getCompletedDate() {
        return this.completedDate;
    }

    public void setCompletedDate(Calendar completedDate) {
        this.completedDate = completedDate;
    }
    
    /**
     *
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *
     */
    @Lob
    @Column(name="CompletedComments", length = 4096)
    public String getCompletedComments() {
        return this.completedComments;
    }

    public void setCompletedComments(String completedComments) {
        this.completedComments = completedComments;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ConservRecmdTypeID", unique = false, nullable = false, insertable = true, updatable = true)
    public ConservRecmdType getConservRecmdType() {
        return this.conservRecmdType;
    }

    public void setConservRecmdType(ConservRecmdType conservRecmdType) {
        this.conservRecmdType = conservRecmdType;
    }


    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LightRecommendationConservRmdID", unique = false, nullable = true, insertable = true, updatable = true)
    public ConservEvent getLightRecommendationConservEvent()
    {
        return this.lightRecommendationConservEvent;
    }

    public void setLightRecommendationConservEvent(final ConservEvent lightRecommendationConservEvent)
    {
        this.lightRecommendationConservEvent = lightRecommendationConservEvent;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DisplayRecommendationConservRmdID", unique = false, nullable = true, insertable = true, updatable = true)
    public ConservEvent getDisplayRecommendationConservEvent()
    {
        return this.displayRecommendationConservEvent;
    }

    public void setDisplayRecommendationConservEvent(final ConservEvent displayRecommendationConservEvent)
    {
        this.displayRecommendationConservEvent = displayRecommendationConservEvent;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "OtherRecommendationConservRmdID", unique = false, nullable = true, insertable = true, updatable = true)
    public ConservEvent getOtherRecommendationConservEvent()
    {
        return this.otherRecommendationConservEvent;
    }

    public void setOtherRecommendationConservEvent(final ConservEvent otherRecommendationConservEvent)
    {
        this.otherRecommendationConservEvent = otherRecommendationConservEvent;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.conservRecommendationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return ConservRecommendation.class;
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
        return 104;
    }

}
