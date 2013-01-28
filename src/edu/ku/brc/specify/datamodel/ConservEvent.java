/* Copyright (C) 2009, University of Kansas Center for Research
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
    
    protected String                     text1;
    protected String                     text2;
    protected Integer                    number1;
    protected Integer                    number2;
    protected Boolean                    yesNo1;
    protected Boolean                    yesNo2;

    
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
        
        text1                   = null;
        text2                   = null;
        number1                 = null;
        number2                 = null;
        yesNo1                  = null;
        yesNo2                  = null;
        
        conservEventAttachments = new HashSet<ConservEventAttachment>();
    }

    // End Initializer

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        conservEventAttachments.size();
    }

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
     * * User definable
     */
    @Column(name = "Text1", length = 64, unique = false, nullable = true, insertable = true, updatable = true)
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
    @Column(name = "Text2", length = 64, unique = false, nullable = true, insertable = true, updatable = true)
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
    public Integer getNumber1()
    {
        return this.number1;
    }

    public void setNumber1(Integer number1)
    {
        this.number1 = number1;
    }

    /**
     * * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getNumber2()
    {
        return this.number2;
    }

    public void setNumber2(Integer number2)
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return ConservDescription.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return conservDescription != null ? conservDescription.getId() : null;
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentTableId()
     */
    @Override
    @Transient
    public int getAttachmentTableId()
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<ConservEventAttachment> getAttachmentReferences()
    {
        return conservEventAttachments;
    }
}
