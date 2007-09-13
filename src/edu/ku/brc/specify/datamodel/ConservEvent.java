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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

/**
 * 
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 29, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "conservevent")
@org.hibernate.annotations.Table(appliesTo="conservevent", indexes =
    {   @Index (name="ConservExamDateIDX", columnNames={"ExamDate"})
    })
public class ConservEvent extends DataModelObjBase implements java.io.Serializable
{
    // Fields    
    protected Integer                    conservEventId;
    protected Calendar                   examDate;
    protected String                     conditionReport;
    protected String                     advTestingExam;
    protected String                     advTestingExamResults;
    protected String                     treatmentReport;
    protected Calendar                   treatmentCompDate;
    protected String                     photoDocs;
    
    protected Agent                      examinedByAgent;
    protected Agent                      treatedByAgent;
    protected ConservDescription         conservDescription;
    
    protected Set<ConservRecommendation> lightRecommendations;
    protected Set<ConservRecommendation> displayRecommendations;
    protected Set<ConservRecommendation> otherRecommendations;
    private Set<ConservEventAttachment> conservEventAttachments;

    // Constructors

    /** default constructor */
    public ConservEvent()
    {
        //
    }

    /** constructor with id */
    public ConservEvent(Integer conservEventId)
    {
        this.conservEventId = conservEventId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        examDate                = null;
        conditionReport         = null;
        advTestingExam          = null;
        advTestingExamResults   = null;
        treatmentReport         = null;
        treatmentCompDate       = null;
        photoDocs               = null;
        examinedByAgent         = null;
        treatedByAgent          = null;
        lightRecommendations    = new HashSet<ConservRecommendation>();
        displayRecommendations  = new HashSet<ConservRecommendation>();
        otherRecommendations    = new HashSet<ConservRecommendation>();
        conservDescription      = null;
        conservEventAttachments = new HashSet<ConservEventAttachment>();
    }

    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "ConservEventID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getConservEventId()
    {
        return this.conservEventId;
    }

    public void setConservEventId(Integer conservEventId)
    {
        this.conservEventId = conservEventId;
    }

    /**
     *
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "ExamDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getExamDate()
    {
        return this.examDate;
    }

    public void setExamDate(final Calendar examDate)
    {
        this.examDate = examDate;
    }

    /**
     *
     */
    @Lob
    @Column(name = "ConditionReport", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
    public String getConditionReport()
    {
        return this.conditionReport;
    }

    public void setConditionReport(final String conditionReport)
    {
        this.conditionReport = conditionReport;
    }

    /**
     *
     */
    @Lob
    @Column(name = "AdvTestingExam", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
    public String getAdvTestingExam()
    {
        return this.advTestingExam;
    }

    public void setAdvTestingExam(final String advTestingExam)
    {
        this.advTestingExam = advTestingExam;
    }

    /**
     *
     */
    @Lob
    @Column(name = "AdvTestingExamResults", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
    public String getAdvTestingExamResults()
    {
        return this.advTestingExamResults;
    }

    public void setAdvTestingExamResults(final String advTestingExamResults)
    {
        this.advTestingExamResults = advTestingExamResults;
    }

    /**
     *
     */
    @Lob
    @Column(name = "TreatmentReport", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
    public String getTreatmentReport()
    {
        return this.treatmentReport;
    }

    public void setTreatmentReport(final String treatmentReport)
    {
        this.treatmentReport = treatmentReport;
    }

    /**
     *
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "TreatmentCompDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getTreatmentCompDate()
    {
        return this.treatmentCompDate;
    }

    public void setTreatmentCompDate(final Calendar treatmentCompDate)
    {
        this.treatmentCompDate = treatmentCompDate;
    }

    /**
     *
     */
    @Lob
    @Column(name = "PhotoDocs", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
    public String getPhotoDocs()
    {
        return this.photoDocs;
    }

    public void setPhotoDocs(final String photoDocs)
    {
        this.photoDocs = photoDocs;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ExaminedByAgentID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getExaminedByAgent()
    {
        return this.examinedByAgent;
    }

    public void setExaminedByAgent(final Agent examinedByAgent)
    {
        this.examinedByAgent = examinedByAgent;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "TreatedByAgentID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getTreatedByAgent()
    {
        return this.treatedByAgent;
    }

    public void setTreatedByAgent(final Agent treatedByAgent)
    {
        this.treatedByAgent = treatedByAgent;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "lightRecommendationConservEvent")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<ConservRecommendation> getLightRecommendations()
    {
        return this.lightRecommendations;
    }

    public void setLightRecommendations(final Set<ConservRecommendation> lightRecommendations)
    {
        this.lightRecommendations = lightRecommendations;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "displayRecommendationConservEvent")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<ConservRecommendation> getDisplayRecommendations()
    {
        return this.displayRecommendations;
    }

    public void setDisplayRecommendations(final Set<ConservRecommendation> displayRecommendations)
    {
        this.displayRecommendations = displayRecommendations;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "otherRecommendationConservEvent")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<ConservRecommendation> getOtherRecommendations()
    {
        return this.otherRecommendations;
    }

    public void setOtherRecommendations(final Set<ConservRecommendation> otherRecommendations)
    {
        this.otherRecommendations = otherRecommendations;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ConservDescriptionID", unique = false, nullable = false, insertable = true, updatable = true)
    public ConservDescription getConservDescription()
    {
        return this.conservDescription;
    }

    public void setConservDescription(final ConservDescription conservDescription)
    {
        this.conservDescription = conservDescription;
    }
    
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "conservEvent")
    public Set<ConservEventAttachment> getConservEventAttachments()
    {
        return conservEventAttachments;
    }

    public void setConservEventAttachments(Set<ConservEventAttachment> conservEventAttachments)
    {
        this.conservEventAttachments = conservEventAttachments;
    }

   /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.conservEventId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return ConservEvent.class;
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
        return 73;
    }

}
