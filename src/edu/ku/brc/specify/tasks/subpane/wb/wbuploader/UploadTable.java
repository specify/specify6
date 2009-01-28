/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.ObjectDeletedException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.CriteriaIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.AccessionAuthorization;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttribute;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttribute;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationAttribute;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.dbsupport.RecordTypeCodeBuilder;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Field;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Relationship;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Table;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader.ParentTableEntry;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.GeoRefConverter.GeoRefFormat;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 * 
 */
public class UploadTable implements Comparable<UploadTable>
{
    private static boolean                          debugging               = true;
    //if true then 'Undos' are accomplished with sql delete statements. This is safe
    //if modification to the database is prevented while uploading.
    private static boolean                          doRawDeletes            = true;
    
    
    protected final Uploader                        uploader;      
    /**
     * The 'underlying' table being uploaded to. Could be an artificial table added to represent a
     * level in a tree (eg. TaxonFamily).
     */
    protected final Table                               table;
    /**
     * A vector containing, for each 'sequence', a vector of the fields in table that are present in
     * the dataset being uploaded. 'sequence' - e.g Collector1, Collector2 ....
     */
    protected Vector<Vector<UploadField>>               uploadFields;
    /**
     * the relationship between this table and it's 'child'.
     */
    protected final Relationship                        relationship;
    /**
     * true if this table must contain data? Not currently fully thought out or implemented.
     */
    protected boolean                                   required                     = false;
    /**
     * A vector containing, for each 'sequence', a vector of the ImportTables that have this
     * ImportTable for a child.
     * 
     * (NOTE: possibly not necessary to store Vector for each sequence??)
     */
    protected Vector<Vector<Uploader.ParentTableEntry>> parentTables;
    /**
     * ids of records uploaded during the most recent upload.
     */
    protected SortedSet<UploadedRecordInfo>                               uploadedRecs;
    
    /**
     * The workbench index of the row currently being processed.
     */
    protected int wbCurrentRow;
    
    protected static final Logger                       log                          = Logger
                                                                                             .getLogger(UploadTable.class);
    /**
     * A vector storing the most recently written object for each 'sequence'.
     * 
     * (NOTE: probably eliminates need for currentIds)
     */
    protected Vector<DataModelObjBase>                  currentRecords;
    /**
     * The Java class of the table being uploaded to.
     */
    protected Class<?>                                  tblClass;
    /**
     * A vector of the related classes that must be non-null.
     */
    protected Vector<RelatedClassSetter>                requiredRelClasses;
    /**
     * default objectids for related classes that are not present in the dataset being uploaded.
     */
    protected Vector<RelatedClassSetter>                relatedClassDefaults;
    /**
     * The session used for writing records.
     */
    protected DataProviderSessionIFace                  tblSession;
    /**
     * Converts from strings to Calendar.
     */
    protected DateConverter                             dateConverter;
    /**
     * true if an edge in the import graph leads from this table. Used to determine whether to
     * search for matches before writing to the database.
     */
    protected boolean                                   hasChildren;

    /**
     * A vector of related tables that need to be checked when finding matching existing records.
     * For example: Collectors must be checked when matching collectingEvents.
     */
    protected Vector<UploadTable>                       matchChildren;
    
    protected boolean                                   skipMatching = false;
    /**
     * If a matching parent including matchChildren has been found then skipRow is set true for
     * members of matchChildren
     */    
    protected boolean                                   skipRow                      = false;
    /**
     * Non-null fields in tblClass that are not included in the uploading dataset.
     */
    protected Vector<DefaultFieldEntry>                 missingRequiredFlds;

    protected UploadMatchSetting                        matchSetting;

    /**
     * If true then matching records are updated with values in uploading dataset.
     * 
     */
    protected boolean                                   updateMatches                = false;

    /**
     * Used in processing new objects added as result of the UploadMatchSetting.ADD_NEW_MODE option.
     */
    protected Vector<MatchRestriction>              restrictedValsForAddNewMatch = null;

    /**
     * internationalized boolean string representations for validation.
     */
    protected static String[]                           boolStrings                  = {
            getResourceString("WB_TRUE"), getResourceString("WB_FALSE"),
            getResourceString("WB_TRUE_ABBR"), getResourceString("WB_FALSE_ABBR"),
            getResourceString("WB_YES"), getResourceString("WB_NO"),
            getResourceString("WB_YES_ABBR"), getResourceString("WB_NO_ABBR"), "1", "0" };

    protected boolean                                   validatingValues             = false;
    protected Object                                    autoAssignedVal              = null; //Assuming only one per table.
    protected UploadField                               autoAssignedField            = null; //Assuming one per table.
    protected Collection                                collection                   = null;
    protected Discipline                                discipline                   = null;
    
    UploadedRecFinalizerIFace                           finalizer                    = null;
    List<Pair<UploadField, Method>>                     precisionDateFields         = new LinkedList<Pair<UploadField, Method>>();
    
    
    /**
     * @author timbo
     *
     * @code_status Alpha
     * 
     * Stores info on restrictions used when searching for matching records.
     *
     */
    public class MatchRestriction implements Comparable<MatchRestriction>
    {
        protected final String fieldName;
        protected final String restriction;
        protected final int    col;
        
        /**
         * @param fieldName
         * @param restriction
         * @param col
         */
        public MatchRestriction(final String fieldName, final String restriction, 
                                final int col)
        {
            this.fieldName = fieldName;
            this.restriction = restriction;
            this.col = col;
        }

        /**
         * @return the fieldName
         */
        public String getFieldName()
        {
            return fieldName;
        }

        /**
         * @return the restriction
         */
        public String getRestriction()
        {
            return restriction;
        }

        /**
         * @return the col
         */
        public int getCol()
        {
            return col;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(MatchRestriction o)
        {
            //first check col == -1 to put the ones without columns at the end of lists.
            if (col == -1)
            {
                return 1;
            }
            
            if (o.col == -1)
            {
                return -1;
            }
            
            return col < o.col ? -1 : col == o.col ? 0 : 1;
        }
        
        
    }
    /**
     * @param table
     * @param relationship
     */
    public UploadTable(Uploader uploader, Table table, Relationship relationship)
    {
        super();
        this.uploader = uploader;
        this.table = table;
        this.relationship = relationship;
        uploadFields = new Vector<Vector<UploadField>>();
        uploadedRecs = new TreeSet<UploadedRecordInfo>();
        currentRecords = new Vector<DataModelObjBase>();
        matchChildren = new Vector<UploadTable>();
        relatedClassDefaults = null;
        dateConverter = new DateConverter();
        matchSetting = new UploadMatchSetting();
    }

    /**
     * @return true if the findMatch step should be skipped.
     * 
     * Match-skipping is done to implement one-to-one relationships, which
     * currently must be annotated as many-to-ones in Hibernate.
     */
    protected boolean shouldSkipMatching()
    {
        return isOneToOneChild();
    }
    
    /**
     * @return true if this table is the 'child' in a 1-1 relationship.
     * 
     * One-to-one relationships currently must be annotated as many-to-ones in Hibernate.
     * A more general solution might be to check for a DELETE_ORPHAN hibernate 
     * annotation on the MANY side of the relationship between
     * this table and it's many-side 'child'.
     */
    public boolean isOneToOneChild()
    {
        return tblClass.equals(CollectionObjectAttribute.class)
            || tblClass.equals(PreparationAttribute.class)
            || tblClass.equals(CollectingEventAttribute.class);
       
    }
    /**
     * Icky workaround for some problems with determining tblClass in constructor. Must be called
     * after constructor.
     * 
     * @throws UploaderException
     */
    public void init() throws UploaderException
    {
        determineTblClass();
        initReqRelClasses();
        skipMatching = shouldSkipMatching();
        finalizer = findFinalizer();
    }

    
    public void findPrecisionDateFields()
    {
        for (UploadField fld : uploadFields.get(0)) //assuming all 'seqs' in uploadFields have the same fields.
        {
            if (fld.getField() != null && fld.getField().getFieldInfo() != null)
            {
                DBFieldInfo precFld = this.getDatePrecisionFld(fld.getField().getFieldInfo());
                if (precFld != null)
                {
                    this.precisionDateFields.add(new Pair<UploadField, Method>(fld, this.getFldSetter(precFld)));
                }
            }
        }
    }
    
    protected UploadedRecFinalizerIFace findFinalizer() throws UploaderException
    {
        String className = this.getClass().getPackage().getName() + "." + tblClass.getSimpleName() + "RecFinalizer";
        try
        {
            Class<?> cls = Class.forName(className);
            return (UploadedRecFinalizerIFace )cls.newInstance();
        }
        catch (ClassNotFoundException ex)
        {
            return null;
        }
        catch (Exception ex)
        {
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        }
    }
    /**
     * Determines the Java class for the specify table being uploaded.
     * 
     * @throws UploaderException
     */
    protected void determineTblClass() throws UploaderException
    {
        try
        {
            tblClass = Class.forName("edu.ku.brc.specify.datamodel." + table.getName());
        }
        catch (ClassNotFoundException cnfEx)
        {
            throw new UploaderException(cnfEx, UploaderException.ABORT_IMPORT);
        }
    }

    /**
     * 
     * @throws UploaderException
     */
    protected void initReqRelClasses() throws UploaderException
    {
        try
        {
            requiredRelClasses = buildReqRelClasses();
        }
        catch (NoSuchMethodException nsmEx)
        {
            throw new UploaderException(nsmEx, UploaderException.ABORT_IMPORT);
        }
    }

    /**
     * @param getter
     * @return method in tblClass named 'setXXX' given method named 'getXXX'
     * @throws NoSuchMethodException
     */
    protected Method getSetterForGetter(Method getter) throws NoSuchMethodException
    {
        Class<?> params[] = new Class<?>[1];
        params[0] = getter.getReturnType();
        String setterName = "set" + getter.getName().substring(3);
        return tblClass.getMethod(setterName, params);
    }

    /**
     * Gets ready for an upload.
     * @throws UploaderException
     */
    @SuppressWarnings("unused")
    public void prepareToUpload() throws UploaderException
    {
        uploadedRecs.clear();
        matchSetting.clear();
    }

    /**
     * @param fldName
     * @return true if a field named fldname is in the uploading dataset.
     */
    protected boolean fldInDataset(final String fldName)
    {
        if (uploadFields != null && uploadFields.size() > 0)
        {
            for (UploadField fld : uploadFields.get(0))
            {
                if (fld.getField().getName().equalsIgnoreCase(fldName)) { return true; }
            }
        }
        return false;
    }

    /**
     * @throws NoSuchMethodException
     * 
     * Builds vector of non-nullable fields in tblClass that are not present in the uploading
     * dataset. SIDE EFFECT: UploadFields' required properties are set.
     */
    protected void buildMissingRequiredFlds() throws NoSuchMethodException
    {
        missingRequiredFlds = new Vector<DefaultFieldEntry>();
        for (Method m : tblClass.getMethods())
        {
            Annotation a = m.getAnnotation(javax.persistence.Column.class);
            Annotation b = m.getAnnotation(javax.persistence.GeneratedValue.class);
            if (a != null && b == null)
            {
                javax.persistence.Column col = (javax.persistence.Column) a;
                if (!col.nullable() && !col.name().startsWith("Timestamp"))
                {
                    if (!fldInDataset(col.name()))
                    {
                        logDebug("adding required field: " + tblClass.getName() + " - "
                                + m.getName());
                        Method setter = getSetterForGetter(m);
                        String fldName = col.name();
                        if (fldName.equalsIgnoreCase("CollectionMemberID"))
                        {
                            fldName = "collectionMemberId";
                        }
                        DefaultFieldEntry dfe;
                        // Now find UploadField corresponding to field and set Required to true.
                        UploadField uploadField = null;
                        for (Vector<UploadField> ufs : uploadFields)
                        {
                            for (UploadField uf : ufs)
                            {
                                if (uf.getField().getName().equalsIgnoreCase(col.name()))
                                {
                                    uf.setRequired(true);
                                    uploadField = uf;
                                }
                            }
                        }
                        if (table.getName().equalsIgnoreCase("collector") && fldName.equalsIgnoreCase("isprimary"))
                        {
                            //Creating a new DefaultFieldEntry class makes it possible to handle
                            //the conditional/parameterized default behavior of isPrimary without spreading these
                            //cheezy field by field conditions to code that is not yet infected.by cheezy fld by fld conditions.
                            dfe = new DefaultIsPrimaryEntry(this, m.getReturnType(),
                                    setter, fldName, uploadField);
                        }
                        else
                        {
                            dfe = new DefaultFieldEntry(this, m.getReturnType(),
                                    setter, fldName, uploadField);
                        }
                        missingRequiredFlds.add(dfe);
                    }
                }
            }
        }
    }

    /**
     * @return the missingRequiredFlds.
     */
    public Iterator<DefaultFieldEntry> getMissingRequiredFlds() throws NoSuchMethodException
    {
        if (missingRequiredFlds == null)
        {
            buildMissingRequiredFlds();
        }
        return missingRequiredFlds.iterator();
    }

    public Vector<String> getWbFldNames()
    {
        Vector<String> result = new Vector<String>();
        for (Vector<UploadField> ufs : uploadFields)
        {
            for (UploadField uf : ufs)
            {
                if (uf.getWbFldName() != null)
                {
                    result.add(uf.getWbFldName());
                }
            }
        }
        return result;
    }

    /**
     * @return the related classes which cannot be null.
     * @throws NoSuchMethodException
     */
    protected Vector<RelatedClassSetter> buildReqRelClasses() throws NoSuchMethodException
    {
        Vector<RelatedClassSetter> result = new Vector<RelatedClassSetter>();
        for (Method m : tblClass.getMethods())
        {
            Annotation a = m.getAnnotation(javax.persistence.JoinColumn.class);
            if (a != null)
            {
                javax.persistence.JoinColumn jc = (javax.persistence.JoinColumn) a;
                logDebug(jc.columnDefinition());
                if (!jc.nullable())
                {
                    logDebug("adding required class: " + tblClass.getName() + " - " + m.getName());
                    javax.persistence.ManyToOne mto = m.getAnnotation(javax.persistence.ManyToOne.class);
                    if (mto != null)
                    {
                        CascadeType[] ct = mto.cascade();
                        for (int c=0; c<ct.length; c++)
                            logDebug(ct[c]);
                    }
                    Method setter = getSetterForGetter(m);
                    String fldName = jc.referencedColumnName();
                    if (fldName == null || fldName.equals(""))
                    {
                        fldName = jc.name();
                    }
                    if (addToReqRelClasses(m.getReturnType()))
                    {
                        result
                                .add(RelatedClassSetter.createRelatedClassSetter(this, m
                                        .getReturnType(), fldName, null, null, setter, uploadFields
                                        .size()));
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param relatedClass.
     * @return true if the related class needs to be added as a requirement.
     */
    @SuppressWarnings("unused")
    protected boolean addToReqRelClasses(Class<?> relatedClass)
    {
        return true;
    }

    
    /**
     * @param rce
     * @return true if a value for the related class can be determined by the uploader.
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unused")
    protected boolean findValueForReqRelClass(final RelatedClassSetter rce) throws ClassNotFoundException, UploaderException
    {
        for (Vector<ParentTableEntry> its : parentTables)
        {
            for (ParentTableEntry pt : its)
            {
                String parentName = capitalize(pt.getImportTable().getWriteTable().getName());
                Class<?> parentTblClass = Class.forName("edu.ku.brc.specify.datamodel."
                        + parentName);
                if (rce.getRelatedClass() == parentTblClass
                        && pt.getParentRel().getRelatedField().getName().equalsIgnoreCase(
                                rce.getFieldName()))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * @return the required classes that are not present in the dataset being uploaded.
     * @throws ClassNotFoundException
     */
    protected void bldMissingReqRelClasses() throws ClassNotFoundException, UploaderException
    {
        relatedClassDefaults = new Vector<RelatedClassSetter>();
        for (RelatedClassSetter rce : this.requiredRelClasses)
        {
            //refresh for DeterminationStatusSetters...
            rce.refresh(this.uploadFields.size());
            if (!findValueForReqRelClass(rce))
            {
                relatedClassDefaults.add(rce);
            }
        }
    }

    public Iterator<RelatedClassSetter> getRelatedClassDefaults() throws ClassNotFoundException, UploaderException
    {
        if (relatedClassDefaults == null)
        {
            bldMissingReqRelClasses();
        }
        return relatedClassDefaults.iterator();
    }

    /**
     * @return true if all required local (excluding foreign keys) fields for the table are in the
     *         dataset.
     */
    protected boolean requiredLocalFldsArePresent()
    {
        return true;
    }

    protected Vector<Field> getMissingReqLocalFlds()
    {
        return new Vector<Field>();
    }

    @SuppressWarnings("unused")
    public Vector<InvalidStructure> verifyUploadability() throws UploaderException,
            ClassNotFoundException
    {
        Vector<InvalidStructure> result = new Vector<InvalidStructure>();
        String tblTitle = getTable().getTableInfo().getTitle();
        if (!requiredLocalFldsArePresent())
        {
            for (Field fld : getMissingReqLocalFlds())
            {
                String fldTitle = fld.getFieldInfo().getTitle();
                String msg = getResourceString("WB_UPLOAD_MISSING_FLD") + ": " + tblTitle + "."
                        + fldTitle;
                result.add(new InvalidStructure(msg, this));
            }
        }
        return result;
    }

    /**
     * @param field
     */
    public void addField(UploadField field)
    {
        int idx = field.getSequence() == null ? 0 : field.getSequence();
        while (uploadFields.size() < idx + 1)
        {
            uploadFields.add(new Vector<UploadField>());
        }
        uploadFields.get(idx).add(field);
    }

    /**
     * @return the table.
     */
    public Table getTable()
    {
        return table;
    }

    /**
     * @return uploadFields
     */
    public Vector<Vector<UploadField>> getUploadFields()
    {
        return uploadFields;
    }

    /**
     * @param name
     * @param sequence
     * @return named import field of sequence
     */
    public UploadField getField(String name, Integer sequence)
    {
        int index = sequence == null ? 0 : sequence;
        if (index < uploadFields.size())
        {
            for (UploadField imp : uploadFields.get(index))
            {
                if (imp.getField().getName().equals(name)) { return imp; }
            }
        }
        return null;
    }

    /**
     * @return the table
     */
    public Table getWriteTable()
    {
        return getTable();
    }

    /**
     * @return the relationship
     */
    public Relationship getRelationship()
    {
        return relationship;
    }

    /**
     * @param recNum
     * @return true if all required fields have data.
     */
    protected boolean requiredDataPresent(int recNum)
    {
        int index = uploadFields.size() > 1 ? recNum : 0;
        for (UploadField f : uploadFields.get(index))
        {
            if (!f.getField().isForeignKey()) //Foreign Keys get assigned later in process...
            {
                if (StringUtils.isEmpty(f.getValue()) && f.isRequired() && f != autoAssignedField) 
                { 
                    return false; 
                }
            }
        }
        return true;
    }

    /**
     * @param recNum
     * @return true if record is not empty and required data is present.
     */
    protected boolean dataToWrite(int recNum)
    {
        int index = uploadFields.size() > 1 ? recNum : 0;
        boolean result = false;
        for (UploadField f : uploadFields.get(index))
        {
            if (StringUtils.isNotEmpty(f.getValue()) || f == autoAssignedField)
            {
                result = true;
                break;
            }
        }
        return result && requiredDataPresent(recNum);
    }

    /**
     * @param recNum
     * @return true if current data is valid for writing.
     */
    protected boolean isValid(int recNum)
    {
        return requiredDataPresent(recNum);
    }

    /**
     * @return required.
     */
    public boolean isRequired()
    {
        return required;
    }

    public int compareTo(UploadTable impT)
    {
        if (impT == null) { return -1; }
        int result = toString().compareTo(impT.toString());
        if (result != 0) { return result; }
        if (relationship != null) { return relationship.compareTo(impT.relationship); }
        if (impT.relationship != null) { return 1; }
        return 0;
    }

    /**
     * @return the parentTables
     */
    public final Vector<Vector<Uploader.ParentTableEntry>> getParentTables()
    {
        return parentTables;
    }

    /**
     * @param parentTables the parentTables to set
     */
    public final void setParentTables(Vector<Vector<Uploader.ParentTableEntry>> parentTables) throws NoSuchMethodException, 
        ClassNotFoundException
    {
        this.parentTables = parentTables;
        for (Vector<ParentTableEntry> ptes : this.parentTables)
        {
            for (ParentTableEntry pte : ptes)
            {
                if (pte.getImportTable().needToMatchChild(tblClass))
                {
                    pte.getImportTable().addChild(this);
                }
            }
        }
        assignParentSetters();
    }

    protected void addChild(final UploadTable child)
    {
        matchChildren.add(child);
    }

    /**
     * @return the uploadedKeys
     */
    public final Set<UploadedRecordInfo> getUploadedRecs()
    {
        return uploadedRecs;
    }

    /**
     * @param index Specifies the 'sequence' (for one-to-many relationships).
     * @return Current (or last uploaded) record for this table.
     */
    public DataModelObjBase getCurrentRecord(int index)
    {
        if (currentRecords.size() == 0) 
        { 
            return null; 
        }
        if (index > currentRecords.size() - 1) 
        { 
            return currentRecords.get(0); 
        }
        return currentRecords.get(index);
    }

    /**
     * @param index
     * @return The current record if it exists, or a newly created record.
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     */
    protected DataModelObjBase getCurrentRecordForSave(int index) throws IllegalAccessException,
            InstantiationException
    {
        if (index > currentRecords.size() - 1 || currentRecords.get(index) == null) { return createRecord(); }
        return currentRecords.get(index);
    }

    /**
     * Stores most recently uploaded record. (NOTE: Currently, after calling 'set' the first time,
     * it is actually only necessary to call it again if findMatch method finds a match...)
     * 
     * @param rec
     * @param index
     */
    protected void setCurrentRecord(DataModelObjBase rec, int index)
    {
        while (currentRecords.size() < index + 1)
        {
            currentRecords.add(null);
        }
        currentRecords.set(index, rec);
    }

    /**
     * @return A new record of type tlbClass.
     * @throws InstantiationException
     * @throws IllegalAccessException /
     */
    protected DataModelObjBase createRecord() throws InstantiationException, IllegalAccessException
    {
        return (DataModelObjBase) tblClass.newInstance();
    }

    /**
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * 
     * Determines and stores setters for parent classes.
     */
    protected void assignParentSetters() throws ClassNotFoundException, NoSuchMethodException
    {
        for (Vector<ParentTableEntry> pts : parentTables)
        {
            for (ParentTableEntry pt : pts)
            {
                String parentName = capitalize(pt.getImportTable().getWriteTable().getName());
                Class<?> parentTblClass = Class.forName("edu.ku.brc.specify.datamodel."
                        + parentName);
                Class<?> parType[] = new Class<?>[1];
                parType[0] = parentTblClass;
                String keyName = pt.getForeignKey();
                String setterName = capitalize(keyName.substring(0, keyName.length() - 2));
                if (tblClass == Preparation.class && setterName.equals("PreparedBy"))
                {
                    setterName = "PreparedByAgent";
                }
                else if (tblClass == Determination.class
                        && setterName.equals("DeterminationStatus"))
                {
                    setterName = "Status";
                }
                pt.setSetter(tblClass.getMethod("set" + setterName, parType));
            }
        }
    }

    /**
     * @param rec - the record being prepared to write to the database.
     * @param recNum - the 1-many 'sequence' of the record.
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * 
     * Sets parent classes for rec.
     */
    protected boolean setParents(DataModelObjBase rec, int recNum) throws InvocationTargetException,
            IllegalArgumentException, IllegalAccessException, UploaderException
    {
        boolean requirementsMet = true;
        for (Vector<ParentTableEntry> ptes : parentTables)
        {
            for (ParentTableEntry pt : ptes)
            {
                Object arg[] = new Object[1];
                DataModelObjBase parentRec = pt.getImportTable().getParentRecord(recNum, this);
                if (parentRec == null || parentRec.getId() == null)
                {
                    arg[0] = null;
                }
                else
                {
                    arg[0] = parentRec;
                }
                pt.getSetter().invoke(rec, arg);
                requirementsMet = requirementsMet && arg[0] != null || !pt.isRequired();
            }
        }
        return requirementsMet;
    }

    /**
     * @param ufld
     * @return true if ufld represents a geoCoord field.
     * 
     * Assumes the field's type has already been determined BigDecimal.
     */
    protected boolean isLatLongFld(final UploadField ufld)
    {
        String name = ufld.getField().getName();
        name = name.substring(0, name.length()-1);
        return name.equalsIgnoreCase("latitude") || name.equalsIgnoreCase("longitude");
    }
    
    /**
     * @param fld
     * @return true if 
     */
    protected boolean isDateWithPrecision(final DBFieldInfo fld)
    {
        return getDatePrecisionFld(fld) != null;
    }
    
    /**
     * @param fld
     * @return precision field associated with fld.
     */
    protected DBFieldInfo getDatePrecisionFld(final DBFieldInfo fld)
    {
        String precFldName = fld.getName() + "Precision";
        for (DBFieldInfo otherFld : fld.getTableInfo().getFields())
        {
            if (otherFld.getName().equalsIgnoreCase(precFldName))
            {
                return otherFld;
            }
        }
        return null;
    }
    
    /**
     * @param fld
     * @return values of the correct class for fld's setter.
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ParseException
     * 
     * Converts fld's string value to the correct class for the field being uploaded to.
     */
    protected Object[] getArgForSetter(final UploadField ufld) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException, UploaderException
    {
        try
        {
            Object arg[] = new Object[1];
            Class<?> fldClass;
//            if (tblClass.equals(DeterminationStatus.class) && ufld.getField().getName().equalsIgnoreCase("type"))
//            {
//                fldClass = Boolean.class;
//            }
//            else
            {
                fldClass = ufld.getSetter().getParameterTypes()[0];
            }
            String fldStr;
            if (ufld.getValueObject() == null)
            {
                fldStr = null;
            }
            else
            {
               fldStr = fldClass.equals(String.class) ? ufld.getValueObject() : ufld.getValueObject().trim();
            }
            if (fldClass == java.util.Calendar.class || fldClass == java.util.Date.class)
            {
                // There are problems with DateConverter (see DateConverter)
                if (fldStr == null || fldStr.equals(""))
                {
                    arg[0] = null;
                }
                else
                {
                    if (isDateWithPrecision(ufld.getField().getFieldInfo()))
                    {
                        fldStr = dateConverter.adjustForPrecision(fldStr);
                    }
                    
                    arg[0] = dateConverter.convert(fldStr);
                }
            }
            else if (fldClass == BigDecimal.class)
            {
                if (fldStr == null || fldStr.equals(""))
                {
                    arg[0] = null;
                }
                else
                {
                    if (isLatLongFld(ufld))
                    {
                        try
                        {
                            fldStr = new GeoRefConverter().convert(fldStr, GeoRefFormat.D_PLUS_MINUS.name());
                        }
                        catch (Exception ex)
                        {
                            throw new UploaderException(ex, UploaderException.INVALID_DATA);
                        }
                    }
                    arg[0] = new BigDecimal(fldStr);
                }
            }
            else if (fldClass == Boolean.class)
            {
                if (fldStr == null || fldStr.equals(""))
                {
                    arg[0] = null;
                }
                else
                {
                    int i;
                    for (i = 0; i < boolStrings.length; i++)
                    {
                        if (fldStr.equalsIgnoreCase(boolStrings[i]))
                            break;
                    }
                    if (i == boolStrings.length) { throw new UploaderException(
                            getResourceString("WB_INVALID_BOOL_CELL_VALUE"),
                            UploaderException.INVALID_DATA); }
                    arg[0] = i % 2 == 0 ? true : false;
                }
                //grotesquery
                //sorry, too much extra processing involved with maintaining one and only one current determination
                //to mess around with the isCurrent workbench mapping. An uploaded co is current or not current.
//                if (tblClass.equals(DeterminationStatus.class)
//                        && ufld.getField().getName().equalsIgnoreCase("type"))
//                {
//                    if (arg[0] == null) { throw new UploaderException(
//                            getResourceString("WB_INVALID_BOOL_CELL_VALUE"),
//                            UploaderException.INVALID_DATA); }
//                    Boolean c = (Boolean) arg[0];
//                    if (c)
//                    {
//                        arg[0] = DeterminationStatus.CURRENT;
//                    }
//                    else
//                    {
//                        arg[0] = DeterminationStatus.NOTCURRENT;
//                    }
//                }
            }
            else if (fldClass != String.class)
            {       
                Class<?> stringArg[] = new Class<?>[1];
                stringArg[0] = String.class;
                Method converter = fldClass.getMethod("valueOf", stringArg);
                Object converterArg[] = new Object[1];
                converterArg[0] = fldStr;
                if (converterArg[0] != null)
                {
                    arg[0] = converter.invoke(fldClass, converterArg);
                }
                else
                {
                    arg[0] = null;
                }
            }
            else
            {
                UIFieldFormatterIFace formatter = ufld.getField().getFieldInfo().getFormatter();
                if (StringUtils.isBlank(fldStr) && (formatter == null || !formatter.isIncrementer()))
                {
                    arg[0] = null;
                }
                else
                {
                    Object val = fldStr;
                    if (ufld.getField().getFieldInfo() != null)
                    {
                        if (formatter != null)
                        {
                            if (StringUtils.isBlank(fldStr) && formatter.isIncrementer())
                            {
                                if (!this.validatingValues || autoAssignedVal == null)
                                {
                                    val = formatter.getNextNumber("");
                                    autoAssignedVal = formatter.formatToUI(val);
                                }
                                if (autoAssignedField == null)
                                {
                                    autoAssignedField = ufld;
                                }
                            }
                            else
                            {
                                val = formatter.formatFromUI(fldStr);
                                if (!formatter.isValid((String )val))
                                {
                                    throw new UploaderException(UIRegistry.getResourceString("WB_UPLOAD_INVALID_FORMAT"), UploaderException.INVALID_DATA);
                                }
                            }
                        }
                    }
                    arg[0] = val;
                }
            }
            return arg;
        }
        catch (IllegalArgumentException ex)
        {
            throw new UploaderException(ex, UploaderException.INVALID_DATA);
        }
        catch (ParseException ex)
        {
            throw new UploaderException(ex, UploaderException.INVALID_DATA);
        }
    }

    /**
     * @param fld
     * @return the setter for fld, if it exists.
     */
    protected Method getFldSetter(final DBFieldInfo fld)
    {
        Class<?> fldClass = getFieldClass(fld);
        Class<?> parTypes[] = new Class<?>[1];
        parTypes[0] = fldClass;
        String methName = "set" + capitalize(fld.getName());
        try
        {
            return tblClass.getMethod(methName, parTypes);
        }
        catch (NoSuchMethodException nsmEx)
        {
            // this should only happen for many-to-many relationships, in which cases the
            // field
            // actually gets handled via the parentSetters
            return null;
        }
    }
    
    /**
     * Finds and assigns setXXX methods for each upload field.
     * 
     * @throws NoSuchMethodException
     */
    public void assignFldSetters()
    {
        for (Vector<UploadField> flds : uploadFields)
        {
            for (UploadField fld : flds)
            {
                if (fld.getField() != null && fld.getField().getFieldInfo() != null)
                {
                    fld.setSetter(getFldSetter(fld.getField().getFieldInfo()));
                }
            }
        }
    }

    /**
     * @param rec
     * @param flds
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ParseException
     * 
     * Calls each upload field's setter for values in current row of uploading dataset.
     */
    protected void setFields(DataModelObjBase rec, Vector<UploadField> flds)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
            UploaderException
    {
        for (UploadField fld : flds)
        {
            Method setter = fld.getSetter();
            if (setter != null)
            {
                setter.invoke(rec, getArgForSetter(fld));
            }
        }
    }

    /**
     * @param rec
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * 
     * Sets missing required flds in rec.
     */
    protected void setRequiredFldDefaults(DataModelObjBase rec, int recNum) throws InvocationTargetException,
            IllegalAccessException
    {
        for (DefaultFieldEntry dfe : missingRequiredFlds)
        {
            Object[] arg = new Object[1];
            arg[0] = dfe.getDefaultValue(recNum);
            dfe.getSetter().invoke(rec, arg);
        }
    }

    /**
     * @param rec
     * @throws UploaderException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * 
     * Sets values for classes with specified defaults.
     */
    protected void setRelatedDefaults(DataModelObjBase rec, int recNum) throws UploaderException,
            InvocationTargetException, IllegalAccessException
    {
        for (RelatedClassSetter rce : relatedClassDefaults)
        {
            Object args[] = new Object[1];
            args[0] = rce.getDefaultObj(recNum);
            rce.getSetter().invoke(rec, args);
        }
    }

    protected boolean checkChildrenMatch(int recNum) throws UploaderException
    {
        boolean result = true;
        DataModelObjBase match = getCurrentRecord(recNum);
        logDebug("Checking to see if children match:" + tblClass.toString() + "=" + match.getId());
        if (tblClass.equals(CollectingEvent.class))
        {
            for (UploadTable child : matchChildren)
            {
                logDebug(child.getTable().getName());
                if (child.getTblClass().equals(Collector.class))
                {
                    DataProviderSessionIFace matchSession = DataProviderFactory.getInstance()
                            .createSession();
                    try
                    {
                        QueryIFace matchesQ = matchSession
                                .createQuery("from Collector where collectingeventid = "
                                        + match.getId() + " order by orderNumber", false);
                        List<?> matches = matchesQ.list();
                        child.loadFromDataSet();
                        if (matches.size() > child.getUploadFields().size())
                        {
                            result = false;
                        }
                        else
                            for (int rec = 0; rec < matches.size(); rec++)
                            {
                                Collector coll1 = (Collector) matches.get(rec);
                                Collector coll2 = (Collector) child.getCurrentRecord(rec);
                                if (coll2 == null)
                                {
                                    result = false;
                                    break;
                                }
                                if (!coll1.getOrderNumber().equals(coll2.getOrderNumber()))
                                {
                                    // maybe this doesn't really need to be checked?
                                    result = false;
                                    break;
                                }
                                else if (coll2.getAgent() == null || !coll1.getAgent().getId().equals(coll2.getAgent().getId()))
                                {
                                    result = false;
                                    break;
                                }
                                else if (!coll1.getIsPrimary().equals(coll1.getIsPrimary()))
                                {
                                    result = false;
                                    break;
                                }
                            }
                    }
                    finally
                    {
                        matchSession.close();
                    }
                }
                if (!result)
                {
                    break;
                }
            }
        }
        else if (tblClass.equals(Accession.class))
        {
            for (UploadTable child : matchChildren)
            {
                logDebug(child.getTable().getName());
                if (child.getTblClass().equals(AccessionAgent.class))
                {
                    DataProviderSessionIFace matchSession = DataProviderFactory.getInstance()
                            .createSession();
                    try
                    {
                        QueryIFace matchesQ = matchSession
                                .createQuery("from AccessionAgent where accessionid = "
                                        + match.getId(), false);
                        List<?> matches = matchesQ.list();
                        child.loadFromDataSet();
                        if (matches.size() != child.getUploadFields().size())
                        {
                            result = false;
                            break;
                        }
                        for (int rec = 0; rec < matches.size(); rec++)
                        {
                            AccessionAgent ag1 = (AccessionAgent) matches.get(rec);
                            int c = 0;
                            boolean matched = false;
                            while (c < child.getUploadFields().size() && !matched)
                            {
                                AccessionAgent ag2 = (AccessionAgent) child.getCurrentRecord(c++);
                                matched = ag1.getAgent().getId().equals(ag2.getAgent().getId());
                            }
                            if (!matched)
                            {
                                result = false;
                                break;
                            }
                        }
                    }
                    finally
                    {
                        matchSession.close();
                    }

                }
                else if (child.getTblClass().equals(AccessionAuthorization.class))
                {
                    DataProviderSessionIFace matchSession = DataProviderFactory.getInstance()
                            .createSession();
                    try
                    {
                        QueryIFace matchesQ = matchSession
                                .createQuery("from AccessionAuthorization where accessionid = "
                                        + match.getId(), false);
                        List<?> matches = matchesQ.list();
                        child.loadFromDataSet();
                        if (matches.size() != child.getUploadFields().size())
                        {
                            result = false;
                            break;
                        }
                        for (int rec = 0; rec < matches.size(); rec++)
                        {
                            AccessionAuthorization au1 = (AccessionAuthorization) matches.get(rec);
                            int c = 0;
                            boolean matched = false;
                            while (c < child.getUploadFields().size() && !matched)
                            {
                                AccessionAuthorization au2 = (AccessionAuthorization) child
                                        .getCurrentRecord(c++);
                                matched = au1.getPermit().getId().equals(au2.getPermit().getId());
                            }
                            if (!matched)
                            {
                                result = false;
                                break;
                            }
                        }
                    }
                    finally
                    {
                        matchSession.close();
                    }

                }
                else if (!result)
                {
                    break;
                }
            }
        }
        else if (tblClass.equals(CollectionObject.class) && !updateMatches)
        {
            result = true;
        }
        else
        // Oh no!!
        {
            log.error("Unable to check matching children for " + tblClass.getName());
            throw new UploaderException("Unable to check matching children for "
                    + tblClass.getName(), UploaderException.ABORT_IMPORT);
        }
        return result;
    }

    protected boolean needToMatchChildren()
    {
        // temporary fix. Really should determine based on cascade rules and the fields in the
        // dataset.
        return tblClass.equals(CollectingEvent.class) || tblClass.equals(Accession.class)
                || tblClass.equals(CollectionObject.class);
    }

    protected boolean needToMatchChild(Class<?> childClass)
    {
        // temporary fix. Really should determine based on cascade rules and the fields in the
        // dataset.
        logDebug("need to add more child classes");
        if (tblClass.equals(Accession.class)) { return childClass.equals(AccessionAgent.class)
                || childClass.equals(AccessionAuthorization.class); }
        if (tblClass.equals(CollectingEvent.class)) { return childClass.equals(Collector.class); }
        if (tblClass.equals(CollectionObject.class)) { return childClass
                .equals(Determination.class)
                || childClass.equals(Preparation.class); }
        return false;
    }

    /**
     * @param critter
     * @param propName
     * @param arg
     * 
     * returns string representation of the restriction added.
     * 
     * Adds a restriction to critter for propName.
     */
    protected String addRestriction(final CriteriaIFace critter,
                                    final String propName,
                                    final Object arg,
                                    boolean ignoreNulls)
    {
        if (arg != null && (!(arg instanceof String) || StringUtils.isNotBlank((String )arg)))
        {
            critter.add(Restrictions.eq(propName, arg));
            if (arg instanceof DataModelObjBase) 
            { 
                String value = DataObjFieldFormatMgr.getInstance().format(arg, arg.getClass());
                if (value != null)
                {
                    return value;
                }
                return ((DataModelObjBase) arg).getId()
                    .toString(); 
            }
            return arg.toString();
        }
        
        if (!ignoreNulls || matchSetting.isMatchEmptyValues())
        {
            critter.add(Restrictions.isNull(propName));
            return getNullRestrictionText();
        }
        
        return "";
    }

    /**
     * @return indicator that a null restriction is in effect.
     */
    public String getNullRestrictionText()
    {
        return "#null#";
    }
    
    protected List<DataModelObjBase> matchChildren(List<DataModelObjBase> matches, int recNum)
            throws UploaderException
    {
        List<DataModelObjBase> result = new ArrayList<DataModelObjBase>();
        for (DataModelObjBase currMatch : matches)
        {
            DataModelObjBase saveRec = getCurrentRecord(recNum);
            try
            {
                // set current record so children access the current id when matching.
                setCurrentRecord(currMatch, recNum);
                if (checkChildrenMatch(recNum))
                {
                    result.add(currMatch);
                }
            }
            finally
            {
                setCurrentRecord(saveRec, recNum);
            }
        }
        return result;
    }

    /**
     * @param recNum
     * @param forChild
     * @return the current value for the parent represented by pte.
     * @throws UploaderException
     */
    @SuppressWarnings("unused")
    protected DataModelObjBase getParentRecord(final int recNum, UploadTable forChild) throws UploaderException
    {
        return getCurrentRecord(recNum);
    }
    
    protected Discipline getDiscipline() throws UploaderException
    {
    	if (discipline == null)
    	{
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                DataModelObjBase temp = AppContextMgr.getInstance().getClassObject(Discipline.class);
                discipline = (Discipline )session.get(temp.getDataClass(), temp.getId());
            }
            catch (Exception ex)
            {
                throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
            }
            finally
            {
                session.close();
            }
    	}
    	return discipline;
    }
    
    /**
     * @param criteriam
     * @throws UploaderException
     * 
     * Adds extra criteria related to discipline.
     */
    protected void addDomainCriteria(CriteriaIFace criteria) throws UploaderException
    {
        //XXX might be better to add getSpecialColumnCriteria() method to QueryAdjusterForDomain??
    	//but it would only be used here. 
    	//but this code will need to be checked whenever QueryAdjusterForDomain.getSpecialColumns is updated...
    	        
        /* CollectionMember and Discipline (and Division?) gets done by MissingClassResolver.
    	if (CollectionMember.class.isAssignableFrom(tblClass))
        {
        	criteria.add(Restrictions.eq("collectionMemberId", getCollection().getId()));
        	return;
        }
        if (DisciplineMember.class.isAssignableFrom(tblClass))
        {
        	criteria.add(Restrictions.eq("discipline", getDiscipline()));
        	return;
        }*/
        if (Agent.class.isAssignableFrom(tblClass))
        {
        	//there is probably an nicer way to to do this
        	criteria.addSubCriterion("disciplines", Restrictions.eq("userGroupScopeId", getDiscipline().getUserGroupScopeId()));
        	return;
        }
    }
    
    protected boolean getMatchCriteria(CriteriaIFace critter,
                                       final int recNum,
                                       Vector<MatchRestriction> restrictedVals)
            throws UploaderException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException
    {
        boolean ignoringBlankCell = false;
        for (UploadField uf : uploadFields.get(recNum))
        {
            if (uf.getSetter() != null)
            {
                String restriction = addRestriction(critter, deCapitalize(uf.getField().getName()),
                        getArgForSetter(uf)[0], true);
                ignoringBlankCell = ignoringBlankCell || restriction.equals("")
                        && !matchSetting.isMatchEmptyValues();
                //restrictedVals.add(new Pair<String, String>(uf.getWbFldName(), restriction));
                String fldName = uf.getWbFldName();
                if (StringUtils.isBlank(fldName))
                {
                    if (uf.getField() != null && uf.getField().getFieldInfo() != null)
                    {
                        fldName = uf.getField().getFieldInfo().getTitle();
                    }
                    else if (uf.getField() != null)
                    {
                        fldName = uf.getField().getName();
                    }
                    else
                    {
                        fldName = "?";
                        log.error("unable to find field title or name for " + uf);
                    }
                }
                restrictedVals.add(new MatchRestriction(fldName, restriction, uf.getIndex()));
            }
        }
        for (Vector<ParentTableEntry> ptes : parentTables)
        {
            for (ParentTableEntry pte : ptes)
            {
                restrictedVals.add(new MatchRestriction(pte.getPropertyName(), addRestriction(
                        critter, pte.getPropertyName(), pte.getImportTable().getParentRecord(
                                recNum, this), false), -1));
            }
        }
        for (RelatedClassSetter rce : relatedClassDefaults)
        {
            critter.add(Restrictions.eq(rce.getPropertyName(), rce.getDefaultObj(recNum)));
        }
        for (DefaultFieldEntry dfe : missingRequiredFlds)
        {
            {
                critter.add(Restrictions.eq(deCapitalize(dfe.getFldName()), dfe
                        .getDefaultValue(recNum)));
            }
        }

        addDomainCriteria(critter);
        
        Collections.sort(restrictedVals);
        
        return ignoringBlankCell;
    }
    
    /**
     * @param recNum
     * @return
     * @throws UploaderException
     * 
     * Searches the database for matches to values in current row of uploading dataset. If one match
     * is found it is set to the current value for this table. If no matches are created a new
     * record is created and saved for the current value. If more than one match is found then
     * action depends on props of matchSetting member.
     */
    @SuppressWarnings( { "unchecked", "unused" })
    protected boolean findMatch(int recNum, boolean forceMatch) throws UploaderException,
            InvocationTargetException, IllegalAccessException, ParseException,
            NoSuchMethodException
    {
        // if (!forceMatch && (!hasChildren || tblClass == CollectionObject.class)) { return false;
        // }
        
        if (skipMatching)
        {
            return false;
        }
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        DataModelObjBase match = null;
        Vector<MatchRestriction> restrictedVals = new Vector<MatchRestriction>();
        boolean ignoringBlankCell = false;
        try
        {
            CriteriaIFace critter = session.createCriteria(tblClass);
            ignoringBlankCell = getMatchCriteria(critter, recNum, restrictedVals);
            
            List<DataModelObjBase> matches;
            List<DataModelObjBase> matchList = (List<DataModelObjBase>) critter.list();
            if (matchList.size() > 1)
            {
                // filter out duplicates. This seems very weird, but docs i found say it is normal
                // for
                // list() to return duplicate objects, and they did not mention any sort of 'select
                // distinct' property.
                Set<DataModelObjBase> matchSet = new HashSet<DataModelObjBase>(matchList);
                matches = new ArrayList<DataModelObjBase>(matchSet);
            }
            else
            {
                matches = matchList;
            }
            if (needToMatchChildren())
            {
                matches = matchChildren(matches, recNum);
            }
            if (matches.size() == 1)
            {
                match = matches.get(0);
                if (ignoringBlankCell)
                {
                    uploader.addMsg(new PartialMatchMsg(restrictedVals, match
                            .toString(), uploader.getRow() + 1, this));
                }
            }
            else if (matches.size() > 1)
            {
                // don't bother anybody with DeterminationStatus, for now.
//                if (tblClass.equals(DeterminationStatus.class))
//                {
//                    match = matches.get(0);
//                }
//                else
//                {
                    match = dealWithMultipleMatches(matches, restrictedVals, recNum);
                    if (match != null)
                    {
                        matchSetting.addSelection(matchSetting.new MatchSelection(restrictedVals,
                                uploader.getRow(), match.getId(), matchSetting
                                        .getMode()));
                    }
//                }
            }
        }
        finally
        {
            session.close();
        }
        if (match != null)
        {
            setCurrentRecord(match, recNum);
            // if a match was found matchChildren don't need to do anything. (assuming
            // !updateMatches!!!)
            for (UploadTable child : matchChildren)
            {
                child.skipRow = true;
            }
            return true;
        }
        return false;
    }

    public void onAddNewMatch(final Vector<MatchRestriction> restrictedVals)
    {
        // yuck. Only want to create one new record for each set values. If cell values 'Roger'
        // 'Johnson' match 2 records in database
        // we only want to add ONE new record for 'Roger' 'Johnson' and use it from now on.
        restrictedValsForAddNewMatch = restrictedVals;
    }

    /**
     * @param matches
     * @return selected match
     */
    protected DataModelObjBase dealWithMultipleMatches(final List<DataModelObjBase> matches,
                                                       final Vector<MatchRestriction> restrictedVals,
                                                       int recNum) throws UploaderException
    {
        return new MatchHandler(this).dealWithMultipleMatches(matches, restrictedVals, recNum);
    }

    /**
     * @param rec
     * @param recNum
     * 
     * Performs extra tasks to get rec ready to be saved to the database.
     */
    @SuppressWarnings("unused")
    protected void finalizeWrite(DataModelObjBase rec, int recNum) throws UploaderException
    {
        finalizeDatePrecisionFields(rec);
        if (finalizer != null)
        {
            finalizer.finalizeForWrite(rec, recNum, uploader);
        }
    }

    /**
     * @param rec
     * @throws UploaderException
     * 
     * Sets values for XXXPrecision fields that exist for dates in the table.
     */
    protected void finalizeDatePrecisionFields(final DataModelObjBase rec) throws UploaderException
    {
        for (Pair<UploadField, Method> fld : precisionDateFields)
        {
            if (fld.getSecond() != null)
            {
                try
                {
                    fld.getSecond().invoke(rec, (byte )getDatePrecision(fld.getFirst()).ordinal());
                }
                catch (InvocationTargetException ex)
                {
                    throw new UploaderException(ex, UploaderException.ABORT_ROW);
                }
                catch (IllegalAccessException ex)
                {
                    throw new UploaderException(ex, UploaderException.ABORT_ROW);
                }
                catch (ParseException ex)
                {
                    throw new UploaderException(ex, UploaderException.ABORT_ROW);
                }
            }
        }
    }
    
    /**
     * @param fld
     * @return the date precision for the current value of fld.
     * @throws ParseException
     */
    protected UIFieldFormatterIFace.PartialDateEnum getDatePrecision(final UploadField fld) throws ParseException
    {
        return dateConverter.getDatePrecision(fld.getValue());
    }
    
    protected UploadField findUploadField(final String name, int seq)
    {
        if (seq >= uploadFields.size())
        {
            log.error("seq out of range.");
            return null;
        }
        for (UploadField uf : uploadFields.get(seq))
        {
            if (uf.getField().getName().equalsIgnoreCase(name)) { return uf; }
        }
        return null;
    }

    /**
     * @param fld
     * @returns true if fld is empty and that is not OK.
     */
    @SuppressWarnings("unused")
    protected boolean invalidNull(final UploadField fld,
                                  final UploadData uploadData,
                                  int row,
                                  int seq) throws UploaderException
    {
        boolean blankButRequired = fld.isRequired() && (fld.getValue() == null || fld.getValue().trim().equals(""));
        boolean isAutoAssignable = fld.getField().getFieldInfo() != null && fld.getField().getFieldInfo().getFormatter() != null
            && fld.getField().getFieldInfo().getFormatter().isIncrementer();
        return blankButRequired && !isAutoAssignable;
    }

    protected String getPickListName(final UploadField fld)
    {
        if (fld.getIndex() != -1 && fld.getField().getFieldInfo() != null)
        {
            String pickListName = fld.getField().getFieldInfo().getPickListName();
            if (StringUtils.isEmpty(pickListName))
            {
                if (RecordTypeCodeBuilder.isTypeCodeField(fld.getField().getFieldInfo()))
                {
                    pickListName = fld.getField().getFieldInfo().getColumn() + "PREDEFSYS";
                }
            }
            return pickListName;
        }
        return null;
    }
    
    /**
     * @param fld
     * @return true if field does not have a picklist or current value of fld is in it's picklist.
     * 
     * This method assumes invalidNull(fld, ...) returns false.
     */
    protected boolean pickListCheck(final UploadField fld)
    {
        if (fld.getValue() == null || fld.getValue().trim().equals(""))
        {
            //assuming invalidNull() is false.
            //XXX issues with null values and pick lists???
            return true;
        }
        
        Map<String, PickListItemIFace> validValues = fld.getValidValues();
        if (validValues == null)
        {
            return true;
        }
        
        return validValues.containsKey(fld.getValue());              
    }
    
    /**
     * @param fld
     * @return a (possibly really really long) message listing the valid values for the fld.
     */
    protected String getInvalidPicklistValErrMsg(final UploadField fld)
    {
        String valList = "";
        Map<String, PickListItemIFace> vals = fld.getValidValues();
        if (vals != null)
        {
            for (String val : vals.keySet())
            {
                if (!StringUtils.isEmpty(valList))
                {
                    valList += ", ";
                }
                valList += "'" + val + "'";
            }
            return String.format(UIRegistry.getResourceString("WB_UPLOAD_VALID_VALS"), valList);
        }
        // this should never happen
        log.error("Could not find picklist values for "
                + (fld.getField().getFieldInfo() == null ? fld.getField().getName() : fld
                        .getField().getFieldInfo().getColumn()));
        return UIRegistry.getResourceString("WB_UPLOAD_UNABLE_TO_FIND_VALID_VALS");
    }
    
    /**
     * @param fld
     * @param seq
     * @param row
     * @param uploadData
     * @return true if the fld is blank/empty
     * This function is called during validation. It is used to validate
     * consistency for one-to-many fields such as "Collector LastName XX"
     */
    protected boolean isBlankVal(final UploadField fld, final int seq, final int row, final UploadData uploadData)
    {
        return StringUtils.isEmpty(fld.getValue());
    }
    
    /**
     * @param msgs
     * @param fld
     * @param name
     * @param row
     * @param seq
     */
    protected void addInvalidValueMsgForOneToManySkip(Vector<UploadTableInvalidValue> msgs, UploadField fld, String name, int row, int seq)
    {
        msgs.add(new UploadTableInvalidValue(null, this, fld, row, 
                new Exception(String.format(UIRegistry.getResourceString("WB_UPLOAD_ONE_TO_MANY_SKIP"),
                        name, seq+1))));
    }
    
    /**
     * @param row
     * @param uploadData
     * @return true if all the fields corresponding directly to columns in the dataset are blank,
     */
    protected boolean isBlankRow(int row, UploadData uploadData)
    {
        int seq = 0;
    	for (Vector<UploadField> flds : uploadFields)
        {
            for (UploadField fld : flds)    
        	{
            	if (fld.getIndex() != -1)
            	{
            		fld.setValue(uploadData.get(row, fld.getIndex()));
                    if (!isBlankVal(fld, seq, row, uploadData))
                    {
                    	return false;
                    }
            	}
        	}
            seq++;
        }
        return true;
    }
    
    /**
     * @param row
     * @param uploadData
     * @param invalidValues
     * 
     * Validates user-entered fields for the row.
     * Validation issues are added to invalidValues vector.
     */
    protected void validateRowValues(int row, UploadData uploadData, Vector<UploadTableInvalidValue> invalidValues)
    {
        int seq = 0;
        boolean gotABlank = false;
        int blankSeq = 0;
        
        //for Locality table only
        LatLonConverter.FORMAT llFmt = null;
        GeoRefConverter gc = new GeoRefConverter();
        Vector<UploadTableInvalidValue> invalidNulls = new Vector<UploadTableInvalidValue>();
        Vector<Integer> invalidBlankSeqs = new Vector<Integer>();
        for (Vector<UploadField> flds : uploadFields)
        {
            boolean isBlank = true;
            UploadField currFirstFld = null;
            for (UploadField fld : flds)
            {
                if (fld.getIndex() != -1)
                {
                    if (currFirstFld == null)
                    {
                        currFirstFld = fld;
                    }
                    fld.setValue(uploadData.get(row, fld.getIndex()));
                    isBlank &= isBlankVal(fld, seq, row, uploadData);
                    try
                    {
                        if (invalidNull(fld, uploadData, row, seq)) 
                        { 
                        	if (!tblClass.equals(PrepType.class) || !Uploader.currentUpload.getUploadTableByName("Preparation").isBlankRow(row, uploadData))
                        	{
                        		throw new Exception(
                        				getResourceString("WB_UPLOAD_FIELD_MUST_CONTAIN_DATA")); 
                        	}
                        }       
                        if (!pickListCheck(fld))
                        {
                            throw new Exception(getInvalidPicklistValErrMsg(fld));
                        }
                        getArgForSetter(fld);
                    }
                    catch (Exception e)
                    {
                        if (hasChildren)
                        {
                            invalidValues.add(new UploadTableInvalidValue(null, this, fld, row, e));
                        }
                        else
                        {
                            invalidNulls.add(new UploadTableInvalidValue(null, this, fld, row, e));
                        }
                    }
                }
                if (tblClass.equals(Locality.class))
                {
                    //Check each row to see that lat/long formats are the same.
                    if (fld.getField().getName().equalsIgnoreCase("latitude1") || fld.getField().getName().equalsIgnoreCase("latitude1")
                            || fld.getField().getName().equalsIgnoreCase("longitude1") || fld.getField().getName().equalsIgnoreCase("longitude2"))
                    {
                        LatLonConverter.FORMAT fmt = gc.getLatLonFormat(uploadData.get(row, fld.getIndex()));
                        if (llFmt == null)
                        {
                            llFmt = fmt;
                        }
                        else
                        {
                            if (!llFmt.equals(fmt))
                            {
                                invalidValues.add(new UploadTableInvalidValue(null, this, fld, row, 
                                        new Exception(UIRegistry.getResourceString("WB_UPLOADER_UNMATCHING_LATLONG_FORMAT"))));
                            }
                        }
                    }
                }
            }
            if (isBlank)
            /* 
             * Disallow situations where 1-many lists have 'holes' - eg. CollectorLastName2 is blank but CollectorLastName1 and -3 are not.
             * 
             */
            {
                gotABlank = true;
                blankSeq = seq;
            }
            else if (!isBlank && gotABlank)
            {
            	if (!invalidBlankSeqs.contains(blankSeq))
            	{
            		for (UploadField blankSeqFld : uploadFields.get(blankSeq))
            		{
            			if (blankSeqFld.getIndex() != -1)
            			{
            				addInvalidValueMsgForOneToManySkip(invalidValues, blankSeqFld, toString(), row, blankSeq);
            			}            			
            		}
            		invalidBlankSeqs.add(blankSeq);
            	}
            }
            
            if (!hasChildren && !isBlank && invalidNulls.size() != 0)
            {
                invalidValues.addAll(invalidNulls);
            }
            invalidNulls.clear();
                
            seq++;
        }
    }
    
    /**
     * @param uploadData
     * @return Vector of invalid values.
     * 
     * Validates values in all workbench cells that are mapped to this table.
     */
    public Vector<UploadTableInvalidValue> validateValues(final UploadData uploadData)
    {
        try
        {
            validatingValues = true;

            Vector<UploadTableInvalidValue> result = new Vector<UploadTableInvalidValue>();
            for (int row = 0; row < uploadData.getRows(); row++)
            {
                validateRowValues(row, uploadData, result);
            }
            if (tblClass.equals(Determination.class))
            {
                // check that isCurrent is ok. 1 and only one true.
                boolean isCurrentPresent = false;
                UploadField anIsCurrentFld = null;
                for (int row = 0; row < uploadData.getRows(); row++)
                {
                    int trueCount = 0;
                    for (Vector<UploadField> flds : uploadFields)
                    {
                        for (UploadField fld : flds)
                        {
                            if (fld.getField().getName().equalsIgnoreCase("iscurrent"))
                            {
                                isCurrentPresent = true;
                                anIsCurrentFld = fld;
                                fld.setValue(uploadData.get(row, fld.getIndex()));
                                try
                                {
                                    Object[] boolVal = getArgForSetter(fld);
                                    if (boolVal[0] != null && (Boolean) boolVal[0])
                                    {
                                        trueCount++;
                                    }
                                }
                                catch (Exception e)
                                {
                                    // ignore. assuming problem was already caught above.
                                }
                            }
                        }
                    }
                    if (isCurrentPresent && trueCount != 1)
                    {
                        result.add(new UploadTableInvalidValue(null, this, anIsCurrentFld, row,
                                new Exception(
                                        getResourceString("WB_UPLOAD_ONE_CURRENT_DETERMINATION"))));
                    }
                }
            }
            return result;
        }
        finally
        {
            validatingValues = false;
        }
    }

    /**
     * @throws UploaderException
     * 
     * This is loads values from dataset into current DataModelObj record, but does not save record
     * to the database. It might be a good idea to track that data has been loaded to avoid
     * unecessary repetition.
     */
    protected void loadFromDataSet() throws UploaderException
    {
        writeRowOrNot(true, true);
    }

    protected void writeRow(int row) throws UploaderException
    {
        wbCurrentRow = row;
        if (!skipRow)
        {
            writeRowOrNot(false, false);
        }
        else
        {
            skipRow = false;
        }
    }
    
    /**
     * Searches for matching record in database. If match is found it is set to be the current
     * record. If no match then a record is initialized and populated and written to the database.
     * 
     * @throws UploaderException
     */
    protected void writeRowOrNot(boolean doNotWrite, boolean skipMatch) throws UploaderException
    {
        int recNum = 0;
        logDebug("writeRowOrNot: " + this.table.getName());
        autoAssignedVal = null;  //assumes one autoassign field per table.
        for (Vector<UploadField> seq : uploadFields)
        {
            try
            {
                if (needToWrite(recNum))
                {
                    if (skipMatch || !findMatch(recNum, false))
                    {
                        DataModelObjBase rec = getCurrentRecordForSave(recNum);
                        rec.initialize();
                        setFields(rec, seq);
                        setRequiredFldDefaults(rec, recNum);
                        boolean gotRequiredParents = setParents(rec, recNum);
                        setRelatedDefaults(rec, recNum);
                        finalizeWrite(rec, recNum);
                        if (!doNotWrite)
                        {
                            if (!gotRequiredParents && hasChildren)
                            {
                                throw new UploaderException(UIRegistry.getResourceString("UPLOADER_MISSING_REQUIRED_DATA"), UploaderException.ABORT_ROW);
                            }
                            if (gotRequiredParents)
                            {
                                doWrite(rec);
                                uploadedRecs.add(new UploadedRecordInfo(rec.getId(), wbCurrentRow,
                                    recNum, autoAssignedVal));
                            }
                        }
                        setCurrentRecord(rec, recNum);
                        finishMatching(rec);
                    }
                }
                else
                {
                    setCurrentRecord(null, recNum);
                }
            }
            catch (InstantiationException ieEx)
            {
                throw new UploaderException(ieEx, UploaderException.ABORT_IMPORT);
            }
            catch (IllegalAccessException iaEx)
            {
                throw new UploaderException(iaEx, UploaderException.ABORT_IMPORT);
            }
            catch (NoSuchMethodException ssmEx)
            {
                throw new UploaderException(ssmEx, UploaderException.ABORT_IMPORT);
            }
            catch (InvocationTargetException itEx)
            {
                throw new UploaderException(itEx, UploaderException.ABORT_IMPORT);
            }
            catch (IllegalArgumentException iaA)
            {
                throw new UploaderException(iaA, UploaderException.ABORT_IMPORT);
            }
            catch (ParseException peEx)
            {
                throw new UploaderException(peEx, UploaderException.ABORT_IMPORT);
            }
            recNum++;
        }
    }

    protected void finishMatching(final DataModelObjBase rec)
    {
        if (restrictedValsForAddNewMatch != null)
        {
            matchSetting.addSelection(matchSetting.new MatchSelection(restrictedValsForAddNewMatch,
                    uploader.getRow(), rec.getId(), matchSetting.getMode()));
            restrictedValsForAddNewMatch = null;
        }
    }

    public Vector<ParentTableEntry> getAncestors()
    {
        Vector<ParentTableEntry> result = new Vector<ParentTableEntry>();
        for (Vector<ParentTableEntry> ptes : parentTables)
        {
            for (ParentTableEntry pte : ptes)
            {
                result.add(pte);
                result.addAll(pte.getImportTable().getAncestors());
            }
        }
        return result;
    }

    /**
     * @param recNum
     * @return true if there is some data in the current row dataset that needs to be written to
     *         this table in the database.
     */
    protected boolean needToWrite(int recNum)
    {
        return dataToWrite(recNum) || parentTables.size() > 0;
    }

    /**
     * @param rec
     * @param recNum
     * @throws UploaderException
     * 
     * Creates session and saves rec.
     */
    protected void doWrite(DataModelObjBase rec) throws UploaderException
    {
        tblSession = DataProviderFactory.getInstance().createSession();
        boolean tblTransactionOpen = false;
        try
        {
            BusinessRulesIFace busRule = DBTableIdMgr.getInstance().getBusinessRule(tblClass);
            if (busRule != null)
            {
                busRule.beforeSave(rec, tblSession);
            }
            tblSession.beginTransaction();
            tblTransactionOpen = true;
            tblSession.save(rec);
            if (busRule != null)
            {
                if (!busRule.beforeSaveCommit(rec, tblSession))
                {
                    tblSession.rollback();
                    throw new Exception("Business rules processing failed");
                }
            }
            tblSession.commit();
            tblTransactionOpen = false;
            if (busRule != null)
            {
                busRule.afterSaveCommit(rec, tblSession);
            }
            if (needToRefreshAfterWrite())
            {
                tblSession.refresh(rec);
            }
        }
        catch (Exception ex)
        {
            if (tblTransactionOpen)
            {
                tblSession.rollback();
            }
            if (ex instanceof org.hibernate.exception.ConstraintViolationException)
            {
                throw new UploaderException(ex, UploaderException.ABORT_ROW);
            }
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        }
        finally
        {
            tblSession.close();
        }
    }

    /**
     * @return true if current record needs to be refreshed after writes.
     */
    protected boolean needToRefreshAfterWrite()
    {
        //The refresh call slows performance hugely so only calling it when necessary.
        //This may be risky. At this time the refresh is required only because
        //of changes made in business rule processing of treeables.
        //But if business rules change...
        return false;
    }
    
    /**
     * Creates a new Hibernate session and associates the given objects with it. NOTE: Static for
     * testing reasons only.
     * 
     * @param objects the objects to associate with the new session
     * @return the newly created session
     */
    /*
     * protected Session getNewSession(Object... objects) { log.trace("enter");
     * 
     * Session session = HibernateUtil.getSessionFactory().openSession(); for (Object o: objects) {
     * if (o!=null) { // make sure not to attempt locking an unsaved object DataModelObjBase dmob =
     * (DataModelObjBase)o; if (dmob.getId() != null) { session.lock(o, LockMode.NONE); } } }
     * log.trace("exit"); return session; }
     */
    public static String capitalize(final String toCap)
    {
        return toCap.substring(0, 1).toUpperCase().concat(toCap.substring(1));
    }

    public static String deCapitalize(final String toDecap)
    {
        return toDecap.substring(0, 1).toLowerCase().concat(toDecap.substring(1));
    }

    private Class<?> getFieldClass(DBFieldInfo fi)
    {
        if (fi == null) { return Integer.class; }
        String type = fi.getType();
        if (StringUtils.isNotEmpty(type))
        {
            if (type.equals("calendar_date"))
            {
                return Calendar.class;

            }
            else if (type.equals("text"))
            {
                return String.class;

            }
            else if (type.equals("boolean"))
            {
                return Boolean.class;

            }
            else if (type.equals("short"))
            {
                return Short.class;

            }
            else if (type.equals("byte"))
            {
                return Byte.class;

            }
            else
            {
                try
                {
                    return Class.forName(type);

                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UploadTable.class, e);
                    log.error(e);
                }
            }
        }
        throw new RuntimeException("Could not find [" + fi.getName() + "]");
    }

    /**
     * undoes the most recent upload.
     * 
     */
    public void undoUpload(final boolean showProgress) throws UploaderException
    {
        //if isOneToOneChild() we most probably don't even need to worry about deleting 
        //but just to be sure will always delete.
        deleteObjects(uploadedRecs.iterator(), showProgress);
    }

    
    /**
     * @param session
     * @return sql delete statements necessary to delete uploaded records.
     */
    protected Vector<QueryIFace> getQueriesForRawDeletes(final DataProviderSessionIFace session)
    {
        Vector<QueryIFace> result = new Vector<QueryIFace>();
        if (tblClass.equals(Agent.class))
        {
            result.add(session.createQuery("delete from agent_discipline where AgentID =:theKey", true));
        }
        result.add(session.createQuery("delete from " + getWriteTable().getName().toLowerCase() + " where " 
                + getWriteTable().getTableInfo().getIdColumnName() + "=:theKey", true));
        return result;
    }
    
    /**
     * @param objs - an iterator of object ids.
     * 
     * Deletes all the objects whose keys are present in objs.
     */
    protected void deleteObjects(Iterator<UploadedRecordInfo> objs, final boolean showProgress) throws UploaderException
    {
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        Vector<QueryIFace> q;
        if (doRawDeletes)
        {
            q = getQueriesForRawDeletes(session);
        }
        else
        {
            q = new Vector<QueryIFace>(1);
            String hql = "from " + getWriteTable().getName() + " where id =:theKey";
            q.add(session.createQuery(hql, false));
        }
        Runnable progShower = null;        
        if (showProgress)
        {
            progShower = new Runnable()
            {
                public void run()
                {
                    uploader.undoStep();
                }
            };
        }
        try
        {
            while (objs.hasNext())
            {
                Object key = objs.next().getKey();
                if (key != null)
                {
                    boolean committed = false;
                    boolean opened = false;
                    try
                    {
                        for (QueryIFace qFace : q)
                        {
                            qFace.setParameter("theKey", key);
                        }
                        if (doRawDeletes)
                        {
                            for (QueryIFace qFace : q)
                            {
                                session.beginTransaction();
                                opened = true;
                                qFace.executeUpdate();
                                session.commit();
                                committed = true;
                            }
                        }
                        else
                        {
                            DataModelObjBase obj = (DataModelObjBase) q.get(0).uniqueResult();
                            if (obj != null)
                            {
                                session.beginTransaction();
                                BusinessRulesIFace busRule = DBTableIdMgr.getInstance().getBusinessRule(tblClass);
                                opened = true;
                                if (busRule != null)
                                {
                                    busRule.beforeDelete(obj, session);
                                }
                                session.delete(obj);
                                if (busRule != null)
                                {
                                    busRule.beforeDeleteCommit(obj, session);
                                }
                                session.commit();
                                committed = true;
                            }
                            else
                            {
                                if (!isOneToOneChild())
                                {
                                    log.error(tblClass.getSimpleName() + ": record with key " + key + " does not exist.");
                                }
                            }
                        }
                    }
                    catch (ConstraintViolationException ex)
                    {
                        // the delete may fail if another user has used or deleted uploaded
                        // records...
                        log.info(table.getName() + ":" + ex);
                        if (opened && !committed)
                        {
                            session.rollback();
                        }
                    }
                    catch (ObjectDeletedException ex)
                    {
                        log.info(table.getName() + "." + key + ":" + ex);
                        if (opened && !committed)
                        {
                            session.rollback();
                        }
                    }
                    catch (Exception ex)
                    {
                        log.info(table.getName() + ":" + ex);
                        if (opened && !committed)
                        {
                            session.rollback();
                        }
                        throw new UploaderException(ex);
                    }
                }
                if (showProgress)
                {
                    SwingUtilities.invokeLater(progShower);
                }
            }
        }
        finally
        {
            session.close();
        }
    }

    private Vector<Method> getGetters()
    {
        Method[] methods = tblClass.getMethods();
        Vector<Method> result = new Vector<Method>();
        for (Method m : methods)
        {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0
                    && m.getReturnType() != void.class && Modifier.isPublic(m.getModifiers())
                    && !Modifier.isTransient(m.getModifiers())
                    && !Modifier.isStatic(m.getModifiers())
                    && !Modifier.isAbstract(m.getModifiers()))
            {
                Annotation jc = m.getAnnotation(javax.persistence.Column.class);
                Annotation c = m.getAnnotation(javax.persistence.JoinColumn.class);
                Annotation otm = m.getAnnotation(javax.persistence.OneToMany.class);
                if (otm == null
                        && (jc != null || c != null || m.getName().equalsIgnoreCase("getId")))
                {
                    result.add(m);
                }

            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Vector<Vector<String>> printUpload() throws InvocationTargetException,
            IllegalAccessException
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        Vector<Method> getters = getGetters();
        Object[] args = new Object[0];
        Vector<Vector<String>> result = new Vector<Vector<String>>();
        try
        {
            String hql = "from " + tblClass.getSimpleName() + " obj where id=:theKey";
            QueryIFace qif = session.createQuery(hql, false);
            boolean wroteHeaders = false;
            for (Object key : uploadedRecs)
            {
                if (key == null)
                {
                    log.error("null key");
                    continue;
                }
                qif.setParameter("theKey", key);
                Object rec = qif.uniqueResult();
                if (rec == null)
                {
                    log.error("null object for key: " + key.toString());
                    continue;
                }
                if (!wroteHeaders)
                {
                    Vector<String> heads = new Vector<String>();
                    heads.add("Id");
                    for (Method getter : getters)
                    {
                        if (!getter.getName().equalsIgnoreCase("getId")
                                && !getter.getName().equalsIgnoreCase(
                                        "get" + getWriteTable().getName() + "Id"))
                        {
                            heads.add(getter.getName().substring(3));
                        }
                    }
                    result.add(heads);
                    wroteHeaders = true;
                }
                Vector<String> row = new Vector<String>();
                row.add(key.toString());
                for (Method getter : getters)
                {
                    if (!getter.getName().equalsIgnoreCase("getId")
                            && !getter.getName().equalsIgnoreCase(
                                    "get" + getWriteTable().getName() + "Id"))
                    {
                        Object obj = getter.invoke(rec, args);
                        if (obj == null)
                        {
                            row.add(null);
                        }
                        else if (DataModelObjBase.class.isInstance(obj))
                        {
                            DataModelObjBase dbobj = (DataModelObjBase) obj;
                            row.add(dbobj.getId() == null ? null : dbobj.getId().toString());
                        }
                        else
                        {
                            row.add(obj.toString());
                        }
                    }
                }
                result.add(row);
            }
        }
        finally
        {
            session.close();
        }
        return result;
    }

    /**
     * @param hasChildren the hasChildren to set
     */
    public final void setHasChildren(boolean hasChildren)
    {
        this.hasChildren = hasChildren;
    }

    /**
     * @return the tblClass
     */
    public final Class<?> getTblClass()
    {
        return tblClass;
    }

    /**
     * @return a name for recordset of uploaded objects.
     */
    protected String getRecordSetName()
    {
        int maxNameLength = DBTableIdMgr.getInstance().getInfoByTableName("recordset").getFieldByColumnName("name").getLength();
        String tblName = DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName()).getTitle();
        String uploadName = uploader.getIdentifier();
        String rsName = tblName + "_" + uploadName;
        if (rsName.length() > maxNameLength)
        {
            //add as many pieces of the upload time as will fit...
            Calendar now = uploader.getUploadTime();
            String[] chunks = {"_" + String.valueOf(now.get(Calendar.YEAR)), "-" + String.valueOf(now.get(Calendar.MONTH) + 1),
                    "-" + String.valueOf(now.get(Calendar.DAY_OF_MONTH)), "_" + String.valueOf(now.get(Calendar.HOUR_OF_DAY)),
                    ":" + String.valueOf(now.get(Calendar.SECOND))}; 
            rsName = tblName;
            int c = 0;
            while (c < chunks.length && (rsName + chunks[c]).length() <= maxNameLength)
            {
                rsName += chunks[c++];
            }
        }
        return rsName;
    }

    /**
     * @return a recordset containing the the objects created during last upload.
     */
    public RecordSet getRecordSet()
    {
        RecordSet result = new RecordSet();
        result.initialize();
        result.set(getRecordSetName(), 
                   DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName()).getTableId(), 
                   RecordSet.WB_UPLOAD);
        result.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
        for (UploadedRecordInfo rec : uploadedRecs)
        {
            result.addItem(rec.getKey().intValue());
        }
        return result;
    }

    /**
     * @return the matchChildren
     */
    public Vector<UploadTable> getMatchChildren()
    {
        return matchChildren;
    }

    @Override
    public String toString()
    {
        String result = DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName())
                .getTitle();
        if (tblClass.equals(Agent.class))
        {
            result += " (" + relationship.getRelatedField().getTable().getTableInfo().getTitle()
                    + ")";
        }
        return result;
    }

    public UploadMatchSetting getMatchSetting()
    {
        return matchSetting;
    }

    public class PartialMatchMsg extends BaseUploadMessage
    {
        protected String      matchVals;
        protected String      matchedText;
        protected int         row;
        protected UploadTable uploadTable;

        /**
         * @param matchVals
         * @param matchedText
         * @param row
         * @param uploadTable
         */
        public PartialMatchMsg(Vector<MatchRestriction> cellVals, String matchedText, int row,
                UploadTable uploadTable)
        {

            super(null);
            StringBuilder sb = new StringBuilder();
            for (MatchRestriction p : cellVals)
            {
                if (!sb.toString().equals(""))
                {
                    sb.append(", ");
                }
                sb.append(p.getFieldName());
                sb.append("=");
                sb.append("\"" + p.getRestriction() + "\"");
            }
            this.matchVals = sb.toString();
            this.matchedText = matchedText;
            this.row = row;
            this.uploadTable = uploadTable;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage#getData()
         */
        @Override
        public Object getData()
        {
            return uploadTable;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage#getRow()
         */
        @Override
        public int getRow()
        {
            return row;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage#getMsg()
         */
        @Override
        public String getMsg()
        {
            StringBuilder result = new StringBuilder(getResourceString("WB_UPLOAD_ROW"));
            result.append(" ");
            result.append(String.valueOf(row));
            result.append(": ");
            result.append(getResourceString("WB_UPLOAD_PARTIAL_MATCH"));
            result.append(" (");
            result.append(matchVals);
            result.append(" ");
            result.append(getResourceString("WB_UPLOAD_MATCHED"));
            result.append(" ");
            result.append("\"" + matchedText + "\")");
            return result.toString();
        }

    }
    
    /**
     * cleans up and stuff?
     * Currently only used as a way of testing Tree updates.
     */
    @SuppressWarnings("unused")
    public void finishUpload(boolean cancelled) throws UploaderException
    {
        //nothing to do here.
    }
    
    @SuppressWarnings("unused")
    public void shutdown() throws UploaderException
    {
        //nothing to do here.
    }

    /**
     * @return the skipMatching
     */
    public boolean isSkipMatching()
    {
        return skipMatching;
    }

    /**
     * @param skipMatching the skipMatching to set
     */
    public void setSkipMatching(boolean skipMatching)
    {
        this.skipMatching = skipMatching;
    }
    
    private void logDebug(Object toLog)
    {
        if (debugging)
        {
            log.debug(toLog);
        }
    }
    
    /**
     * @return hasChildren
     */
    public boolean getHasChildren()
    {
        return hasChildren;
    }

    /**
     * @return the autoAssignedField
     */
    public UploadField getAutoAssignedField()
    {
        return autoAssignedField;
    }
}
