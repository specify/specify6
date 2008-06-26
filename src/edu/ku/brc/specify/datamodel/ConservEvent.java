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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

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
    {   @Index (name="ConservExamDateIDX", columnNames={"ExamDate"}),
        @Index (name="ConservCompletedDateIDX", columnNames={"completedDate"})
    })
public class ConservEvent extends DataModelObjBase implements AttachmentOwnerIFace<ConservEventAttachment>, java.io.Serializable
{
    // Fields    
    protected Integer                    conservEventId;
    
    // Step #1
    protected Calendar                   examDate;
    protected String                     conditionReport;
    protected String                     advTestingExam;
    protected String                     advTestingExamResults;
    protected String                     photoDocs;              // documents the problem
    protected Agent                      examinedByAgent;
        
    // Step #2
    protected String                     recommendedComments;
    protected Agent                      curator;
    protected Calendar                   curatorApprovalDate;
    
    // Step #3
    protected String                     treatmentReport;
    protected Calendar                   treatmentCompDate;
    protected Agent                      treatedByAgent;
    
    // Step #4 
    protected Calendar                   completedDate;
    protected String                     completedComments; 
    
    // Additional Information
    protected String                     remarks;
    
    protected ConservDescription          conservDescription;    
    protected Set<ConservEventAttachment> conservEventAttachments;

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
        conservDescription      = null;
        
        recommendedComments     = null;
        completedDate           = null;
        completedComments       = null;
        remarks                 = null;
        curatorApprovalDate     = null;
        curator                 = null;
        
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
    @Column(name = "ConditionReport", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
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
    @Column(name = "AdvTestingExam", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
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
    @Column(name = "AdvTestingExamResults", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
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
    @Column(name = "TreatmentReport", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
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
    @Column(name = "PhotoDocs", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
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
    @Lob
    @Column(name = "CompletedComments", length = 4096)
    public String getCompletedComments()
    {
        return this.completedComments;
    }

    public void setCompletedComments(String completedComments)
    {
        this.completedComments = completedComments;
    }

    /**
     * 
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "CuratorApprovalDate", unique = false, nullable = true, insertable = true, updatable = true, length = 8192)
    public Calendar getCuratorApprovalDate()
    {
        return this.curatorApprovalDate;
    }

    public void setCuratorApprovalDate(final Calendar curatorApprovalDate)
    {
        this.curatorApprovalDate = curatorApprovalDate;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CuratorID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getCurator()
    {
        return this.curator;
    }

    public void setCurator(final Agent curator)
    {
        this.curator = curator;
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
    
    //@OneToMany(mappedBy = "conservEvent")
    //@Cascade( {CascadeType.ALL} )
    @OneToMany(mappedBy = "conservEvent")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
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

    @Transient
    public Set<ConservEventAttachment> getAttachmentReferences()
    {
        return conservEventAttachments;
    }
}
