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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "project")
@org.hibernate.annotations.Table(appliesTo="project", indexes =
    {   @Index (name="ProjectNameIDX", columnNames={"ProjectName"}),
        @Index (name="ProjectNumberIDX", columnNames={"ProjectNumber"}) 
    })
@SuppressWarnings("serial")
public class Project extends CollectionMember implements java.io.Serializable {

    // Fields    

    protected Integer                      projectId;
    protected String                       projectName;
    protected String                       projectNumber;
    protected String                       projectDescription;
    protected String                       grantNumber;
    protected String                       grantAgency;
    protected String                       url;
    protected Calendar                     startDate;
    protected Calendar                     endDate;
    protected String                       remarks;
    protected String                       text1;
    protected String                       text2;
    protected Float                        number1;
    protected Float                        number2;
    protected Boolean                      yesNo1;
    protected Boolean                      yesNo2;
    
    protected Agent                        agent;
    protected Set<CollectionObject>        collectionObjects;


    // Constructors

    /** default constructor */
    public Project() {
        //
    }
    
    /** constructor with id */
    public Project(Integer projectId) {
        this.projectId = projectId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        projectId             = null;
        projectName           = null;
        projectNumber         = null;
        grantAgency           = null;
        grantNumber           = null;
        projectDescription    = null;
        url = null;
        startDate = null;
        endDate = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        agent = null;
        collectionObjects = new HashSet<CollectionObject>();
    }
    // End Initializer

    
    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "ProjectID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getProjectId() {
        return this.projectId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        // loads the Project's CollectionObjects and then each ColObj's Project
        for (CollectionObject co : collectionObjects)
        {
            co.getCollection().getCollectionId();
            co.getProjects().size();
        }
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.projectId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Project.class;
    }
    
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    /**
     *      * Name of the project
     */
    @Column(name = "ProjectName", unique = false, nullable = false, insertable = true, updatable = true, length = 128)
    public String getProjectName() {
        return this.projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     *      * Description of project
     */
    @Column(name = "ProjectDescription", length=255, unique = false, nullable = true, insertable = true, updatable = true)
    public String getProjectDescription() {
        return this.projectDescription;
    }
    
    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    /**
     * @return the grantNumber
     */
    @Column(name = "GrantNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getGrantNumber()
    {
        return grantNumber;
    }

    /**
     * @param grantNumber the grantNumber to set
     */
    public void setGrantNumber(String grantNumber)
    {
        this.grantNumber = grantNumber;
    }

    /**
     * @param projectNumber the projectNumber to set
     */
    public void setProjectNumber(String projectNumber)
    {
        this.projectNumber = projectNumber;
    }
    
    /**
     * @return the projectNumber
     */
    @Column(name = "ProjectNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getProjectNumber()
    {
        return projectNumber;
    }

    /**
     * @return the grantAgency
     */
    @Column(name = "GrantAgency", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getGrantAgency()
    {
        return grantAgency;
    }

    /**
     * @param grantAgency the grantAgency to set
     */
    public void setGrantAgency(String grantAgency)
    {
        this.grantAgency = grantAgency;
    }

    /**
     *      * URL for project
     */
    @Column(name = "URL", length=1024, unique = false, nullable = true, insertable = true, updatable = true)
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *      * Date project began
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "StartDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getStartDate() {
        return this.startDate;
    }
    
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     *      * Date project ended
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "EndDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getEndDate() {
        return this.endDate;
    }
    
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
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
     *      * User definable
     */
    @Lob
    @Column(name = "Text1", length = 65535)
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *      * Agent record for project
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ProjectAgentID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     * 
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
    @JoinTable(name = "project_colobj", 
            joinColumns = { @JoinColumn(name = "ProjectID", unique = false, nullable = false, insertable = true, updatable = false) }, 
            inverseJoinColumns = { @JoinColumn(name = "CollectionObjectID", unique = false, nullable = false, insertable = true, updatable = false) })
    public Set<CollectionObject> getCollectionObjects() 
    {
        return this.collectionObjects;
    }
    
    public void setCollectionObjects(Set<CollectionObject> projectCollectionObjects) 
    {
        this.collectionObjects = projectCollectionObjects;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Collection.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return collectionMemberId != null ? collectionMemberId : null;
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
        return 66;
    }

}
