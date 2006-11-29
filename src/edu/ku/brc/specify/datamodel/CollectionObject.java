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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.ui.forms.FormDataObjIFace;




/**

 */
public class CollectionObject extends DataModelObjBase implements java.io.Serializable {

    // Fields

     protected Long collectionObjectId;
     protected String fieldNumber;
     protected String description;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Integer countAmt;
     protected String remarks;
     protected String name;
     protected String modifier;
     protected Calendar catalogedDate;
     protected String catalogedDateVerbatim;
     protected String guid;
     protected String altCatalogNumber;
     protected Integer groupPermittedToView;
     protected Boolean deaccessioned;
     protected Float catalogNumber;
     protected CollectingEvent collectingEvent;
     protected ContainerItem containerItem;
     protected Set<CollectionObjectCitation> collectionObjectCitations;
     protected Set<AttributeIFace> attrs;
     protected Set<Preparation> preparations;
     protected Set<Determination> determinations;
     protected CollectionObjDef collectionObjDef;
     protected Set<ProjectCollectionObject> projectCollectionObjects;
     protected Set<DeaccessionCollectionObject> deaccessionCollectionObjects;
     protected Set<OtherIdentifier> otherIdentifiers;
     protected CatalogSeries catalogSeries;
     protected Accession accession;
     protected Agent cataloger;
     protected Set<Attachment>          attachments;
     protected Container container;


    // Constructors

    /** default constructor */
    public CollectionObject()
    {
        // do nothing
    }

    /** constructor with id */
    public CollectionObject(Long collectionObjectId) {
        this.collectionObjectId = collectionObjectId;
    }




    // Initializer
    public void initialize()
    {
        collectionObjectId = null;
        fieldNumber = null;
        description = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        countAmt = null;
        remarks = null;
        name = null;
        modifier = null;
        catalogedDate = null;
        catalogedDateVerbatim = null;
        guid = null;
        altCatalogNumber = null;
        groupPermittedToView = null;
        timestampCreated = new Date();
        timestampModified = null;
        lastEditedBy = null;
        deaccessioned = null;
        catalogNumber = null;
        collectingEvent = null;
        containerItem = null;
        collectionObjectCitations = new HashSet<CollectionObjectCitation>();
        attrs = new HashSet<AttributeIFace>();
        preparations = new HashSet<Preparation>();
        determinations = new HashSet<Determination>();
        collectionObjDef = null;
        projectCollectionObjects = new HashSet<ProjectCollectionObject>();
        deaccessionCollectionObjects = new HashSet<DeaccessionCollectionObject>();
        otherIdentifiers = new HashSet<OtherIdentifier>();
        catalogSeries = null;
        accession = null;
        cataloger = null;
        attachments = new HashSet<Attachment>();
        container = null;
    }
    // End Initializer
    
    public void initForSearch()
    {
        catalogSeries = new CatalogSeries();
        catalogSeries.initialize();
        
        accession = new Accession();
        accession.initialize();
        
        cataloger  = new Agent();
        cataloger.initialize();
    }

    // Property accessors

    /**
     *
     */
    public Long getCollectionObjectId() {
        return this.collectionObjectId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.collectionObjectId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class<?> getDataClass()
    {
        return CollectionObject.class;
    }

    public void setCollectionObjectId(Long collectionObjectId) {
        this.collectionObjectId = collectionObjectId;
    }

    /**
     *      * BiologicalObject (Bird, Fish, etc)
     */
    public String getFieldNumber() {
        return this.fieldNumber;
    }

    public void setFieldNumber(String fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    /**
     *      * Image, Sound, Preparation, Container(Container Label?)
     */
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *      * User definable
     */
    public String getText1() {
        return this.text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    public String getText2() {
        return this.text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    public Float getNumber1() {
        return this.number1;
    }

    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    public Float getNumber2() {
        return this.number2;
    }

    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo2() {
        return this.yesNo2;
    }

    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *
     */
    public Integer getCountAmt() {
        return this.countAmt;
    }

    public void setCountAmt(Integer countAmt) {
        this.countAmt = countAmt;
    }

    /**
     *
     */
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    public String getModifier() {
        return this.modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    /**
     *
     */
    public Calendar getCatalogedDate() {
        return this.catalogedDate;
    }

    public void setCatalogedDate(Calendar catalogedDate) {
        this.catalogedDate = catalogedDate;
    }

    /**
     *
     */
    public String getCatalogedDateVerbatim() {
        return this.catalogedDateVerbatim;
    }

    public void setCatalogedDateVerbatim(String catalogedDateVerbatim) {
        this.catalogedDateVerbatim = catalogedDateVerbatim;
    }

    /**
     *
     */
    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     *
     */
    public String getAltCatalogNumber() {
        return this.altCatalogNumber;
    }

    public void setAltCatalogNumber(String altCatalogNumber) {
        this.altCatalogNumber = altCatalogNumber;
    }

    /**
     *
     */
    public Integer getGroupPermittedToView() {
        return this.groupPermittedToView;
    }

    public void setGroupPermittedToView(Integer groupPermittedToView) {
        this.groupPermittedToView = groupPermittedToView;
    }

    /**
     *
     */
    public Boolean getDeaccessioned() {
        return this.deaccessioned;
    }

    public void setDeaccessioned(Boolean deaccessioned) {
        this.deaccessioned = deaccessioned;
    }

    /**
     *
     */
    public Float getCatalogNumber() {
        return this.catalogNumber;
    }

    public void setCatalogNumber(Float catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    /**
     *      * BiologicalObject (Bird, Fish, etc)
     */
    public CollectingEvent getCollectingEvent() {
        return this.collectingEvent;
    }

    public void setCollectingEvent(CollectingEvent collectingEvent) {
        this.collectingEvent = collectingEvent;
    }

    /**
     *
     */
    public ContainerItem getContainerItem() {
        return this.containerItem;
    }

    public void setContainerItem(ContainerItem containerItem) {
        this.containerItem = containerItem;
    }

    /**
     *
     */
    public Set<CollectionObjectCitation> getCollectionObjectCitations() {
        return this.collectionObjectCitations;
    }

    public void setCollectionObjectCitations(Set<CollectionObjectCitation> collectionObjectCitations) {
        this.collectionObjectCitations = collectionObjectCitations;
    }

    /**
     *
     */
    public Set<AttributeIFace> getAttrs() {
        return this.attrs;
    }

    public void setAttrs(Set<AttributeIFace> attrs) {
        this.attrs = attrs;
    }

    /**
     *
     */
    public Set<Preparation> getPreparations() {
        return this.preparations;
    }

    public void setPreparations(Set<Preparation> preparations) {
        this.preparations = preparations;
    }

    /**
     *
     */
    public Set<Determination> getDeterminations() {
        return this.determinations;
    }

    public void setDeterminations(Set<Determination> determinations) {
        this.determinations = determinations;
    }

    /**
     *
     */
    public CollectionObjDef getCollectionObjDef() {
        return this.collectionObjDef;
    }

    public void setCollectionObjDef(CollectionObjDef collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }

    /**
     *
     */
    public Set<ProjectCollectionObject> getProjectCollectionObjects() {
        return this.projectCollectionObjects;
    }

    public void setProjectCollectionObjects(Set<ProjectCollectionObject> projectCollectionObjects) {
        this.projectCollectionObjects = projectCollectionObjects;
    }

    /**
     *
     */
    public Set<DeaccessionCollectionObject> getDeaccessionCollectionObjects() {
        return this.deaccessionCollectionObjects;
    }

    public void setDeaccessionCollectionObjects(Set<DeaccessionCollectionObject> deaccessionCollectionObjects) {
        this.deaccessionCollectionObjects = deaccessionCollectionObjects;
    }

    /**
     *
     */
    public Set<OtherIdentifier> getOtherIdentifiers() {
        return this.otherIdentifiers;
    }

    public void setOtherIdentifiers(Set<OtherIdentifier> otherIdentifiers) {
        this.otherIdentifiers = otherIdentifiers;
    }

    /**
     *
     */
    public CatalogSeries getCatalogSeries() {
        return this.catalogSeries;
    }

    public void setCatalogSeries(CatalogSeries catalogSeries) {
        this.catalogSeries = catalogSeries;
    }

    /**
     *
     */
    public Accession getAccession() {
        return this.accession;
    }

    public void setAccession(Accession accession) {
        this.accession = accession;
    }

    /**
     *
     */
    public Agent getCataloger() {
        return this.cataloger;
    }

    public void setCataloger(Agent cataloger) {
        this.cataloger = cataloger;
    }


    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    /**
     *      * Preparation, Container
     */
    public Container getContainer() {
        return this.container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#addReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void addReference(FormDataObjIFace ref, String refType)
    {
        if (ref instanceof Preparation)
        {
            preparations.add((Preparation)ref);
            ((Preparation)ref).setCollectionObject(this);
            
        } else if (ref instanceof Determination)
        {
            determinations.add((Determination)ref);
            ((Determination)ref).setCollectionObject(this);
            
        } else if (ref instanceof CollectionObjectCitation)
        {
            collectionObjectCitations.add((CollectionObjectCitation)ref);
            ((CollectionObjectCitation)ref).setCollectionObject(this);
            
        } else if (ref instanceof CollectionObjectAttr)
        {
            attrs.add((CollectionObjectAttr)ref);
            ((CollectionObjectAttr)ref).setCollectionObject(this);
            
        } else if (ref instanceof ProjectCollectionObject)
        {
            projectCollectionObjects.add((ProjectCollectionObject)ref);
            ((ProjectCollectionObject)ref).setCollectionObject(this);
            
        } else if (ref instanceof DeaccessionCollectionObject)
        {
            deaccessionCollectionObjects.add((DeaccessionCollectionObject)ref);
            ((DeaccessionCollectionObject)ref).setCollectionObject(this);
            
        } else if (ref instanceof OtherIdentifier)
        {
            otherIdentifiers.add((OtherIdentifier)ref);
            ((OtherIdentifier)ref).setCollectionObject(this);

        } else
        {
            throw new RuntimeException("Adding Object ["+ref.getClass().getSimpleName()+"] and the refType is null.");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#removeReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void removeReference(FormDataObjIFace ref, String refType)
    {       
        if (ref instanceof Preparation)
        {
            preparations.remove(ref);
            ((Preparation)ref).setCollectionObject(null);
            
        } else if (ref instanceof Determination)
        {
            determinations.remove(ref);
            ((Determination)ref).setCollectionObject(null);
            
        } else if (ref instanceof CollectionObjectCitation)
        {
            collectionObjectCitations.remove(ref);
            ((CollectionObjectCitation)ref).setCollectionObject(null);
            
        } else if (ref instanceof CollectionObjectAttr)
        {
            attrs.remove(ref);
            ((CollectionObjectAttr)ref).setCollectionObject(null);
            
        } else if (ref instanceof ProjectCollectionObject)
        {
            projectCollectionObjects.remove(ref);
            ((ProjectCollectionObject)ref).setCollectionObject(null);
            
        } else if (ref instanceof DeaccessionCollectionObject)
        {
            deaccessionCollectionObjects.remove(ref);
            ((DeaccessionCollectionObject)ref).setCollectionObject(null);
            
        } else if (ref instanceof OtherIdentifier)
        {
            otherIdentifiers.remove(ref);
            ((OtherIdentifier)ref).setCollectionObject(null);

        } else
        {
            throw new RuntimeException("Removing Object ["+ref.getClass().getSimpleName()+"] and the refType is null.");
        }
    }
    
    //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    public String getIdentityTitle()
    {
        String title = catalogNumber.toString();
        if (StringUtils.isNotEmpty(title))
        {
            title = fieldNumber;
        }
        return title != null ? title : super.getIdentityTitle();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 1;
    }
}
