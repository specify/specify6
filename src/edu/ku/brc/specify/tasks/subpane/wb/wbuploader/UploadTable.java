/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.CriteriaIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.AccessionAuthorization;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Relationship;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Table;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader.ParentTableEntry;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.forms.BusinessRulesIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *
 */
public class UploadTable implements Comparable<UploadTable>
{
    /**
     * The 'underlying' table being uploaded to.
     * Could be an artificial table added to represent a level in a tree (eg. TaxonFamily).
     */
    protected Table                       table;
    /**
     * A vector containing, for each 'sequence', a vector of the fields in table that are present in the dataset being uploaded.
     * 'sequence' - e.g Collector1, Collector2 ....
    */
    protected Vector<Vector<UploadField>> uploadFields;
    /**
     * the relationship between this table and it's 'child'.
     */
    protected Relationship                relationship;
    /**
     * true if this table must contain data?
     * Not currently fully thought out or implemented.
     */
    protected boolean                     required = false;
    /**
     * A vector containing, for each 'sequence', a vector of the ImportTables that have this ImportTable for a child.
     * 
     *  (NOTE: possibly not necessary to store Vector for each sequence??)
     */
    protected Vector<Vector<Uploader.ParentTableEntry>> parentTables;
    /**
     *  ids of records uploaded during the most recent upload.  
     */
    protected Set<Object> uploadedKeys;
    protected static final Logger log = Logger.getLogger(UploadTable.class);
    /**
     * A vector storing the most recently written object for each 'sequence'.
     * 
     * (NOTE: probably eliminates need for currentIds)
     */
    protected Vector<DataModelObjBase> currentRecords; 
    /**
     * The Java class of the table being uploaded to.
     */
    protected Class<?> tblClass;
    /**
     * A vector of the related classes that must be non-null.
     */
    protected Vector<RelatedClassEntry> requiredRelClasses;
    /**
     * default objectids for related classes that are not present in the dataset being uploaded.
     */
    protected Vector<RelatedClassEntry> relatedClassDefaults;
    /**
     * The session used for writing records.
     */
    protected DataProviderSessionIFace tblSession;
    /**
     * Converts from strings to Calendar.
     */
    protected DateConverter dateConverter;
    /**
     * true if an edge in the import graph leads from this table. Used to determine whether to
     * search for matches before writing to the database. 
     */
    protected boolean hasChildren;
    
    /**
     * A vector of related tables that need to be checked when finding matching existing records.
     * For example: Collectors must be checked when matching collectingEvents. 
     */
    protected Vector<UploadTable> matchChildren;
    /**
     * If a matching parent including matchChildren has been found then skipRow is set true for members of matchChildren
     */
    protected boolean skipRow = false;
    /**
     * Non-null fields in tblClass that are not included in the uploading dataset.  
     */
    protected Vector<DefaultFieldEntry> missingRequiredFlds;
    
    protected UploadMatchSetting matchSetting;
    
    /**
     * If true then matching records are updated with values in uploading dataset.
     * 
     */
    protected boolean updateMatches = false;
    
    /**
     * @param table
     * @param relationship
     */
    public UploadTable(Table table, Relationship relationship)
    {
        super();
        this.table = table;
        this.relationship = relationship;
        uploadFields = new Vector<Vector<UploadField>>();
        uploadedKeys = new HashSet<Object>();
        currentRecords = new Vector<DataModelObjBase>();
        matchChildren = new Vector<UploadTable>();
        relatedClassDefaults = null;
        dateConverter = new DateConverter();
        matchSetting = new UploadMatchSetting();
    }

    /**
     * Icky workaround for some problems with determining tblClass in constructor. 
     * Must be called after constructor.
     * 
     * @throws UploaderException
     */
    public void init() throws UploaderException
    {
        determineTblClass();
        initReqRelClasses();
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
     */
    public void prepareToUpload() throws NoSuchMethodException, ClassNotFoundException
    {
        assignFldSetters();
        assignParentSetters();
        uploadedKeys.clear();
    }
    
    /**
     * @param fldName
     * @return true if a field named fldname is in the uploading dataset.
     */
    protected boolean fldInDataset(final String fldName)
    {
        for (UploadField fld : uploadFields.get(0))
        {
            if (fld.getField().getName().equalsIgnoreCase(fldName))
            {
                return true;
            }
        }
        return false;
    }
    /**
     * @throws NoSuchMethodException
     * 
     * Builds vector of non-nullable fields in tblClass that are not present in the uploading dataset.
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
                if (!col.nullable() && !col.name().startsWith("Timestamp") && !fldInDataset(col.name()))
                {
                    log.debug("adding required field: " + tblClass.getName() + " - " + m.getName());
                    Method setter = getSetterForGetter(m);
                    String fldName = col.name();
                    missingRequiredFlds.add(new DefaultFieldEntry(this, m.getReturnType(), setter, fldName));
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
    protected Vector<RelatedClassEntry> buildReqRelClasses()  throws NoSuchMethodException
    {
        Vector<RelatedClassEntry> result = new Vector<RelatedClassEntry>();
        for (Method m : tblClass.getMethods())
        {
            Annotation a = m.getAnnotation(javax.persistence.JoinColumn.class);
            if (a != null)
            {
                javax.persistence.JoinColumn jc = (javax.persistence.JoinColumn) a;
                if (!jc.nullable())
                {
                    log.debug("adding required class: " + tblClass.getName() + " - " + m.getName());
                    Method setter = getSetterForGetter(m);
                    String fldName = jc.referencedColumnName();
                    if (fldName == null || fldName.equals(""))
                    {
                        fldName = jc.name();
                    }
                    if (addToReqRelClasses(m.getReturnType()))
                    {
                        result.add(new RelatedClassEntry(this, m.getReturnType(), fldName, null, null, setter));
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
     * @return the required classes that are not present in the dataset being uploaded.
     * @throws ClassNotFoundException
     */
/*    public final Vector<RelatedClassEntry> getMissingReqRelClasses() throws ClassNotFoundException
    {
        Vector<RelatedClassEntry> result = new Vector<RelatedClassEntry>();
        for (RelatedClassEntry rce : this.requiredRelClasses)
        {
            boolean foundEntry = false;
            for (Vector<ParentTableEntry> its : parentTables)
            {
                for (ParentTableEntry pt : its)
                {
                    String parentName = capitalize(pt.getImportTable().getWriteTable().getName());
                    Class<?> parentTblClass = Class.forName("edu.ku.brc.specify.datamodel."
                            + parentName);
                    if (rce.getRelatedClass() == parentTblClass && pt.getParentRel().getRelatedField().getName().equalsIgnoreCase(rce.getFieldName()))
                    {
                        foundEntry = true;
                        break;
                    }
                }
                if (foundEntry)
                {
                    break;
                }
            }
            if (!foundEntry)
            {
                result.add(rce);
            }
        }
        return result;
    }
*/
    /**
     * @return the required classes that are not present in the dataset being uploaded.
     * @throws ClassNotFoundException
     */
    protected void bldMissingReqRelClasses() throws ClassNotFoundException
    {
        relatedClassDefaults = new Vector<RelatedClassEntry>();
        for (RelatedClassEntry rce : this.requiredRelClasses)
        {
            boolean foundEntry = false;
            for (Vector<ParentTableEntry> its : parentTables)
            {
                for (ParentTableEntry pt : its)
                {
                    String parentName = capitalize(pt.getImportTable().getWriteTable().getName());
                    Class<?> parentTblClass = Class.forName("edu.ku.brc.specify.datamodel."
                            + parentName);
                    if (rce.getRelatedClass() == parentTblClass && pt.getParentRel().getRelatedField().getName().equalsIgnoreCase(rce.getFieldName()))
                    {
                        foundEntry = true;
                        break;
                    }
                }
                if (foundEntry)
                {
                    break;
                }
            }
            if (!foundEntry)
            {
                relatedClassDefaults.add(rce);
            }
        }
    }
    
    public Iterator<RelatedClassEntry> getRelatedClassDefaults() throws ClassNotFoundException
    {
        if (relatedClassDefaults == null)
        {
            bldMissingReqRelClasses();
        }
        return relatedClassDefaults.iterator();
    }
    
    /**
     * @author timbo
     *
     * @code_status Alpha
     *
     * Stores default values for (probably only required) fields not in uploading dataset. 
     */
    public class DefaultFieldEntry
    {
        /**
         * The upload table that created this entry.
         */
        protected final UploadTable uploadTbl;
        /**
         * The Java class of the field being uploaded to.
         */
        protected Class<?> fldClass;
        /**
         * The method in tblClass that is used set values to the field being uploaded to.
         */
        protected Method    setter;
        /**
         * Default arg for setter member.
         */
        protected Object[] defaultValue;
        /**
         * The name of the field being uploaded to.
         */
        protected String fldName;
        /**
         * @param fldClass
         * @param setter
         * @param defaultValue
         * @param fldName
         */
        public DefaultFieldEntry(final UploadTable uploadTbl, Class<?> fldClass, Method setter, String fldName)
        {
            super();
            this.uploadTbl = uploadTbl;
            this.fldClass = fldClass;
            this.setter = setter;
            this.defaultValue = new Object[1];
            defaultValue[0] = null;
            this.fldName = fldName;
        }
        /**
         * @return the defaultValue as an array for use as a parameter for method invocation.
         */
        protected final Object[] getDefaultValueArg()
        {
            return defaultValue;
        }
        /**
         * @return the default value Object
         */
        protected Object getDefaultValue()
        {
            return defaultValue[0];
        }
        /**
         * @param defaultValue the defaultValue to set
         */
        public final void setDefaultValue(Object defaultValue)
        {
            this.defaultValue[0] = defaultValue;
        }
        /**
         * @return the fldClass
         */
        public final Class<?> getFldClass()
        {
            return fldClass;
        }
        /**
         * @return the fldName
         */
        public final String getFldName()
        {
            return fldName;
        }
        /**
         * @return the setter
         */
        public final Method getSetter()
        {
            return setter;
        }
        
        public boolean isDefined()
        {
            return defaultValue[0] != null;
        }
        /**
         * @return the uploadTbl
         */
        public final UploadTable getUploadTbl()
        {
            return uploadTbl;
        }
    }
    /**
     * @author timbo
     *
     * @code_status Alpha
     *
     * Stores info about related tables.
     */
    public class RelatedClassEntry
    {
        /**
         * the UploadTable that defined this entry
         */
        private final UploadTable uploadTbl;
        /**
         * Java class for the related table.
         */
        private Class<?>          relatedClass;
        /**
         * Name of the foreign key.
         */
        private String            fieldName;
        /**
         * Default key value for the related date.
         */
        private Object      defaultId;
        /**
         * Default object of the related class.
         */
        private Object            defaultObj;
        /**
         * tblClass method to set related class values.
         */
        private Method            setter;
        /**
         * @param relatedClass
         * @param fieldName
         * @param defaultId
         * @param defaultObj
         * @param setter
         */
        /**
         * Name of the hibernate property of the foreign key
         */
        private String            propertyName;

        RelatedClassEntry(final UploadTable uploadTbl, Class<?> relatedClass, String fieldName, Object defaultId,
                Object defaultObj, Method setter)
        {
            this.uploadTbl = uploadTbl;
            this.relatedClass = relatedClass;
            this.fieldName = fieldName;
            this.defaultId = defaultId;
            this.defaultObj = defaultObj;
            this.setter = setter;
            if (this.setter.getName().startsWith("set"))
            {
                this.propertyName = UploadTable.deCapitalize(this.setter.getName().substring(3));
            }
            else
            {
                this.propertyName = UploadTable.deCapitalize(this.setter.getName());
            }
        }
        /**
         * @return the defaultValue
         */
        public final Object getDefaultId()
        {
            return defaultId;
        }
        /**
         * @param defaultId the defaultId to set
         */
        public final void setDefaultId(Object defaultId)
        {
            this.defaultId = defaultId;
            this.defaultObj = null;
        }
        /**
         * @return the fieldName
         */
        public final String getFieldName()
        {
            return fieldName;
        }
        /**
         * @return the relatedClass
         */
        public final Class<?> getRelatedClass()
        {
            return relatedClass;
        }
        /**
         * @return the setter
         */
        public final Method getSetter()
        {
            return setter;
        }
        private Method getSessionGetMethod()
        {
            Method[] meths = DataProviderSessionIFace.class.getMethods();
            for (Method result : meths)
            {
                if (result.getName().equals("get") && result.getParameterTypes().length == 2)
                {
                    return result;
                }                    
            }
            return null;
        }
        /**
         * @return the defaultObj
         */
        public final Object getDefaultObj() throws UploaderException
        {
            if (defaultObj == null && defaultId != null)
            {
                DataProviderSessionIFace objSession = DataProviderFactory.getInstance()
                        .createSession();
                Method getMethod = getSessionGetMethod();
                if (getMethod != null)
                {
                    Class<?> keyClass = getMethod.getParameterTypes()[1];
                    try
                    {
                        //defaultObj = objSession.get(relatedClass, keyClass.cast(defaultId));
                        Object[] args = new Object[2];
                        args[0] = relatedClass;
                        args[1] = keyClass.cast(defaultId);
                        defaultObj = getMethod.invoke(objSession, args);
                        if (defaultObj == null) { throw new UploaderException("Object with id "
                                + defaultId.toString() + " does not exist.",
                                UploaderException.ABORT_IMPORT); }
                    }
                    catch (IllegalAccessException iaEx)
                    {
                        throw new UploaderException(iaEx, UploaderException.ABORT_IMPORT);
                    }
                    catch (InvocationTargetException itEx)
                    {
                        throw new UploaderException(itEx, UploaderException.ABORT_IMPORT);
                    }
                     finally
                    {
                        objSession.close();
                    }
                }
                else
                {
                    throw new UploaderException(
                            "Could not find DataProviderSessionIFace.get method.",
                            UploaderException.ABORT_IMPORT);
                }
            }
            return defaultObj;
        }
        /**
         * @return the propertyName
         */
        public final String getPropertyName()
        {
            return propertyName;
        }
        
        public boolean isDefined()
        {
            return defaultObj != null || defaultId != null;
        }
        /**
         * @return the uploadTbl
         */
        public final UploadTable getUploadTbl()
        {
            return uploadTbl;
        }
    }
    
    /**
     * @return true if Table is importable.
     */
    public boolean isImportable()
    {
        return requiredFldsArePresent();
    }

    /**
     * @return
     */
    protected boolean requiredFldsArePresent()
    {
        return true;
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
            if (f.getValue() == null && f.isRequired()) { return false; }
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
            if (f.getValue() != null)
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
        int result = table.getName().compareTo(impT.table.getName());
        if (result != 0) { return result; }
        if (relationship != null)
        {
            return relationship.compareTo(impT.relationship);
        }
        if (impT.relationship != null)
        {
            return 1;
        }
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
    public final void setParentTables(Vector<Vector<Uploader.ParentTableEntry>> parentTables)
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
    }
    
    protected void addChild(final UploadTable child)
    {
        matchChildren.add(child);
    }

    /**
     * @return the uploadedKeys
     */
    public final Set<Object> getUploadedKeys()
    {
        return uploadedKeys;
    }
    /**
     * @param t
     * @throws UploaderException
     * 
     * writes data (if necessary) for t.
     */
//    protected void writeRowOLD() throws UploaderException
//    {
//        int recNum = 0;
//        for (Vector<UploadField> seq : uploadFields)
//        {
//            // log.debug("writeRow: writing table: " + t.getTable().getName());
//            DB.BogusRecord rec = table.getSchema().getDb().getBogusRecord(getWriteTable());
//            for (UploadField f : seq)
//            {
//                rec.setField(f.getField().getName(), f.getValue());
//            }
//            for (Vector<ParentTableEntry> its : parentTables)
//            {
//                for (ParentTableEntry pt : its)
//                {
//                    rec.setField(pt.getParentRel().getRelatedField().getName(), pt
//                                .getImportTable().getCurrentId(recNum).toString());
//                }
//            }
//            saveARec(rec);
//            doWriteOLD(rec, recNum);
//            recNum++;
//        }
//    }

//    private void doWriteOLD(DB.BogusRecord rec, int recNum) throws UploaderException
//    {
//        boolean goodRec = dataToWrite(recNum) && isValid(recNum);
//        if (!isRequired())
//        {
//            goodRec = parentTables.size() > 0 || goodRec;
//        }
//        if (!goodRec)
//        {
//            throw new UploaderException(
//                    "requred data missing", UploaderException.ABORT_ROW);
//        }
//        if (dataToWrite(recNum) || parentTables.size() > 0)
//        {
//            try
//            {
//                setCurrentId(rec.writeValues(), recNum);
//            } catch (DB.BogusRecord.DuplicateMatchException ex)
//            {
//                throw new UploaderException(ex, UploaderException.ABORT_ROW);
//            }
//            
//        } else
//        {
//            setCurrentId(null, recNum);
//        }
//    }

//    protected void writeRow() throws UploaderException
//    {
//        writeRowNEW();
//        //writeRowOLD();
//    }
    
    /**
     * @param index Specifies the 'sequence' (for one-to-many relationships). 
     * @return Current (or last uploaded) record for this table.
     */
    public final DataModelObjBase getCurrentRecord(int index)
    {
        if (currentRecords.size() == 0) { return null; }
        if (index > currentRecords.size() - 1) { return currentRecords.get(0); }
        return currentRecords.get(index);
    }
    
    /**
     * @param index
     * @return The current record if it exists, or a newly created record. 
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     */
    protected DataModelObjBase getCurrentRecordForSave(int index) throws IllegalAccessException, InstantiationException
    {
        if (index > currentRecords.size() - 1)
        {
            return createRecord();
        }
        return currentRecords.get(index);
    }
    
    /**
     * Stores most recently uploaded record. 
     * (NOTE: Currently, after calling 'set' the first time, it is actually only necessary to call it again if findMatch method finds a match...)
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
     * @throws IllegalAccessException
/     */
    protected DataModelObjBase createRecord() throws InstantiationException, IllegalAccessException
    {
        return  (DataModelObjBase) tblClass.newInstance();   
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
                String setterName = capitalize(keyName.substring(0, keyName.length()-2));
                if (tblClass == Preparation.class && setterName.equals("PreparedBy"))
                {
                    setterName = "PreparedByAgent";
                }
                else if (tblClass == Determination.class && setterName.equals("DeterminationStatus"))
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
    protected void setParents(DataModelObjBase rec, int recNum) throws InvocationTargetException,
            IllegalArgumentException, IllegalAccessException
    {
        for (Vector<ParentTableEntry> ptes : parentTables)
        {
            for (ParentTableEntry pt : ptes)
            {
                Object arg[] = new Object[1];
                DataModelObjBase parentRec = pt.getImportTable().getCurrentRecord(recNum);
                if (parentRec.getId() == null)
                {
                    arg[0] = null;
                }
                else
                {
                    arg[0] = parentRec;
                }
                pt.getSetter().invoke(rec, arg);
            }
        }
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
    protected Object[] getArgForSetter(UploadField fld)
            throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, ParseException
    {
        Object arg[] = new Object[1];
        Class<?> fldClass = fld.getSetter().getParameterTypes()[0];
        if (fldClass == java.util.Calendar.class || fldClass == java.util.Date.class)
        {
            //There are problems with DateConverter (see DateConverter)
            if (fld.getValue() == null || fld.getValue().equals(""))
            {
                arg[0] = null;
            }
            else
            {
                arg[0] = dateConverter.convert(fld.getValue());
            }
        }
        else if (fldClass == BigDecimal.class)
        {
            if (fld.getValue() == null || fld.getValue().equals(""))
            {
                arg[0] = null;
            }
            else
            {
                arg[0] = new BigDecimal(fld.getValue());
            }
        }
        else if (fldClass != String.class)
        {
            Class<?> stringArg[] = new Class<?>[1];
            stringArg[0] = String.class;
            Method converter = fldClass.getMethod("valueOf", stringArg);
            Object converterArg[] = new Object[1];
            converterArg[0] = fld.getValue();
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
            arg[0] = fld.getValue();
        }
        return arg;
    }
    
    /**
     * Finds and assigns setXXX methods for each upload field.
     * @throws NoSuchMethodException
     */
    protected void assignFldSetters() 
    {
        for (Vector<UploadField> flds : uploadFields)
        {
            for (UploadField fld : flds)
            {
                Class<?> fldClass = getFieldClass(fld.getField().getFieldInfo());
                Class<?> parTypes[] = new Class<?>[1];
                parTypes[0] = fldClass;
                String methName = "set" + capitalize(fld.getField().getName());
                try
                {
                    fld.setSetter(tblClass.getMethod(methName, parTypes));
                } catch (NoSuchMethodException nsmEx)
                {
                    //this should only happen for many-to-many relationships, in which cases the field
                    //actually gets handled via the parentSetters
                    fld.setSetter(null);
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
            throws NoSuchMethodException, InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, ParseException
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
    protected void setRequiredFldDefaults(DataModelObjBase rec) throws InvocationTargetException, IllegalAccessException
    {
        for (DefaultFieldEntry dfe : missingRequiredFlds)
        {
            dfe.getSetter().invoke(rec, dfe.getDefaultValueArg());
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
    protected void setRelatedDefaults(DataModelObjBase rec) throws UploaderException, InvocationTargetException, IllegalAccessException
    {
        for (RelatedClassEntry rce : relatedClassDefaults)
        {
            Object args[] = new Object[1];
            args[0] = rce.getDefaultObj();
            rce.getSetter().invoke(rec, args);
        }
    }
    
    
    protected boolean checkChildrenMatch(int recNum) throws UploaderException
    {
        boolean result = true;
        DataModelObjBase match = getCurrentRecord(recNum);
        log.debug("Checking to see if children match:" + tblClass.toString() + "=" + match.getId());
        if (tblClass.equals(CollectingEvent.class)) 
        { 
            for (UploadTable child : matchChildren)
            {
                log.debug(child.getTable().getName());
                if (child.getTblClass().equals(Collector.class))
                {
                    DataProviderSessionIFace matchSession = DataProviderFactory.getInstance()
                            .createSession();
                    try
                    {
                        QueryIFace matchesQ = matchSession.createQuery("from Collector where collectingeventid = " + match.getId() + " order by orderNumber");
                        List<?> matches = matchesQ.list();
                        child.loadFromDataSet();
                        if (matches.size() != child.getUploadFields().size())
                        {
                            result = false;
                        }
                        else for (int rec = 0; rec < matches.size(); rec++)
                        {
                            Collector coll1 = (Collector)matches.get(rec);
                            Collector coll2 = (Collector)child.getCurrentRecord(rec);
                            if (!coll1.getOrderNumber().equals(coll2.getOrderNumber()))
                            {
                                //maybe this doesn't really need to be checked?
                                result = false;
                                break;
                            }
                            else if (!coll1.getAgent().getId().equals(coll2.getAgent().getId()))
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
        else if(tblClass.equals(Accession.class))
        {
            for (UploadTable child : matchChildren)
            {
                log.debug(child.getTable().getName());
                if (child.getTblClass().equals(AccessionAgent.class))
                {
                    DataProviderSessionIFace matchSession = DataProviderFactory.getInstance()
                            .createSession();
                    try
                    {
                        QueryIFace matchesQ = matchSession.createQuery("from AccessionAgent where accessionid = " + match.getId());
                        List<?> matches = matchesQ.list();
                        child.loadFromDataSet();
                        if (matches.size() != child.getUploadFields().size())
                        {
                            result = false;
                            break;
                        }
                        for (int rec = 0; rec < matches.size(); rec++)
                        {
                            AccessionAgent ag1 = (AccessionAgent)matches.get(rec);
                            int c = 0;
                            boolean matched = false;
                            while (c < child.getUploadFields().size() && !matched) 
                            {
                                AccessionAgent ag2 = (AccessionAgent)child.getCurrentRecord(c++);
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
                                        + match.getId());
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
                                AccessionAuthorization au2 = (AccessionAuthorization) child.getCurrentRecord(c++);
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
        else // Oh no!!
        {
            log.error("Unable to check matching children for " + tblClass.getName());
            throw new UploaderException("Unable to check matching children for " + tblClass.getName(),
                UploaderException.ABORT_IMPORT);
        }
        return result; 
    }
    
    protected boolean needToMatchChildren()
    {
        //temporary fix. Really should determine based on cascade rules and the fields in the dataset.
        return tblClass.equals(CollectingEvent.class)
          || tblClass.equals(Accession.class)
          || tblClass.equals(CollectionObject.class);  
    }
    
    protected boolean needToMatchChild(Class<?> childClass)
    {
        //temporary fix. Really should determine based on cascade rules and the fields in the dataset.
        log.debug("need to add more child classes");
        if (tblClass.equals(Accession.class)) 
        { 
            return childClass.equals(AccessionAgent.class)
                || childClass.equals(AccessionAuthorization.class); 
        }
        if (tblClass.equals(CollectingEvent.class)) 
        { 
            return childClass.equals(Collector.class); 
        }
        if (tblClass.equals(CollectionObject.class)) 
        { 
            return childClass.equals(Determination.class)
              || childClass.equals(Preparation.class);
        }
        return false;
    }
    
    /**
     * @param critter
     * @param propName
     * @param arg
     * 
     * Adds a restriction to critter for propName.
     */
    protected void addRestriction(CriteriaIFace critter, String propName, Object arg)
    {
        if (arg != null)
        {
            critter.add(Restrictions.eq(propName, arg));
        }
        else
        {
            critter.add(Restrictions.isNull(propName));
        }
    }
    
    protected List<DataModelObjBase> matchChildren(List<DataModelObjBase> matches, int recNum) throws UploaderException
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
     * @return
     * @throws UploaderException
     * 
     * Searches the database for matches to values in current row of uploading dataset. If one match
     * is found it is set to the current value for this table. If no matches are created a new
     * record is created and saved for the current value. If more than one match an Uploader
     * exception is thrown. (NOTE: validation SHOULD have already been performed to prevent more
     * than one match from occurring, or to specify which object to choose when multiple matches are
     * found.
     */
    @SuppressWarnings({"unchecked", "unused"})
    protected boolean findMatch(int recNum, boolean forceMatch) throws UploaderException,
            InvocationTargetException, IllegalAccessException, ParseException,
            NoSuchMethodException
    {
        //if (!forceMatch && (!hasChildren || tblClass == CollectionObject.class)) { return false; }
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        DataModelObjBase match = null;
        try
        {
            CriteriaIFace critter = session.createCriteria(tblClass);
            for (UploadField uf : uploadFields.get(recNum))
            {
                if (uf.getSetter() != null)
                {
                    addRestriction(critter, deCapitalize(uf.getField().getName()), getArgForSetter(uf)[0]);
                }
            }
            for (Vector<ParentTableEntry> ptes : parentTables)
            {
                for (ParentTableEntry pte : ptes)
                {
                    addRestriction(critter, pte.getPropertyName(), pte.getImportTable().getCurrentRecord(recNum));
                }
            }
            for (RelatedClassEntry rce : relatedClassDefaults)
            {
                critter.add(Restrictions.eq(rce.getPropertyName(), rce.getDefaultObj()));
            }
            for (DefaultFieldEntry dfe : missingRequiredFlds)
            {
                critter.add(Restrictions.eq(deCapitalize(dfe.getFldName()), dfe
                        .getDefaultValueArg()[0]));
            }

            List<DataModelObjBase> matches;
            List<DataModelObjBase> matchList = (List<DataModelObjBase>) critter.list();
            if (matchList.size() > 1)
            {
                //filter out duplicates. This seems very weird, but docs i found say it is normal for
                //list() to return duplicate objects, and they did not mention any sort of 'select distinct' property.
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
            }
            else if (matches.size() > 1)
            {
                //don't bother anybody with DeterminationStatus, for now.
                if (tblClass.equals(DeterminationStatus.class))
                {
                    match = matches.get(0);
                }
                else
                {
                    match = dealWithMultipleMatches(matches);
                }
            }
        }
        finally
        {
            session.close();
        }
        if (match != null)
        {
            setCurrentRecord(match, recNum);
            // if a match was found matchChildren don't need to do anything. (assuming !updateMatches!!!)
            for (UploadTable child : matchChildren)
            {
                child.skipRow = true;
            }
            return true;
        }
        return false;
    }
    
    /**
     * @param matches
     * @return 
     */
    protected DataModelObjBase dealWithMultipleMatches(List<DataModelObjBase> matches) throws UploaderException
    {
        if (matchSetting.getMode() == UploadMatchSetting.ADD_NEW_MODE)
        {
            return null;
        }
        if (matchSetting.getMode() == UploadMatchSetting.PICK_FIRST_MODE)
        {
            return matches.get(0);
        }
        if (matchSetting.getMode() == UploadMatchSetting.SKIP_ROW_MODE)
        {
            throw new UploaderException("Skipping row due to multiple matches", UploaderException.ABORT_ROW);
        }
        
        String title = null;
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoByTableName(getWriteTable().getName());        
        if (ti != null)
        {
            title = ti.getTitle();
        }

        String msg = title != null ? String.format("Choose the a %s", title)
                : "Choose the best Match"; //i18n

        ChooseFromListDlg<DataModelObjBase> dlg = new ChooseFromListDlg<DataModelObjBase>(null,
                msg, matches);
        dlg.setModal(true);
        dlg.setVisible(true);
        if (!dlg.isCancelled()) { return dlg.getSelectedObject(); }
        return null; 
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
        //do nothing for now
    }
    
    public class UploadTableInvalidValue 
    {
        protected UploadTable uploadTbl;
        protected UploadField uploadFld;
        protected Integer rowNum;
        protected Exception cause;
        /**
         * @param uploadTbl
         * @param uploadFld
         * @param rowNum
         * @param issueName
         * @param description
         */
        public UploadTableInvalidValue(UploadTable uploadTbl, UploadField uploadFld, int rowNum, Exception cause)
        {
            super();
            this.uploadTbl = uploadTbl;
            this.uploadFld = uploadFld;
            this.rowNum = new Integer(rowNum);
            this.cause = cause;
        }
        /**
         * @return the description
         */
        public String getDescription()
        {
            if (cause == null)
            {
                return null;
            }
            StringBuilder result = new StringBuilder(cause.getClass().getSimpleName());
            if (cause.getLocalizedMessage() != null)
            {
                result.append(cause.getLocalizedMessage());
            }
            return result.toString();
        }
        /**
         * @return the issueName
         */
        public String getIssueName()
        {
            return cause.getClass().getSimpleName();
        }
        /**
         * @return the rowNum
         */
        public int getRowNum()
        {
            return rowNum;
        }
        /**
         * @param rowNum the rowNum to set
         */
        public void setRowNum(int rowNum)
        {
            this.rowNum = rowNum;
        }
        /**
         * @return the uploadFld
         */
        public UploadField getUploadFld()
        {
            return uploadFld;
        }
        /**
         * @param uploadFld the uploadFld to set
         */
        public void setUploadFld(UploadField uploadFld)
        {
            this.uploadFld = uploadFld;
        }
        /**
         * @return the uploadTbl
         */
        public UploadTable getUploadTbl()
        {
            return uploadTbl;
        }
        /**
         * @param uploadTbl the uploadTbl to set
         */
        public void setUploadTbl(UploadTable uploadTbl)
        {
            this.uploadTbl = uploadTbl;
        }
        @Override
        public String toString()
        {
            return uploadTbl.getTable().getName() + "." + uploadFld.getWbFldName() + " (row " + rowNum.toString() + "): " + getIssueName();
        }
     }
        
    public Vector<UploadTableInvalidValue> validateValues(final UploadData uploadData)
    {
        Vector<UploadTableInvalidValue> result = new Vector<UploadTableInvalidValue>();
        for (int row = 0; row < uploadData.getRows(); row++)
        {
            for (Vector<UploadField> flds : uploadFields)
            {
                for (UploadField fld : flds)
                {
                    if (fld.getIndex() != -1)
                    {
                        fld.setValue(uploadData.get(row, fld.getIndex()));
                        try
                        {
                            getArgForSetter(fld);
                        } catch (Exception e)
                        {
                            result.add(new UploadTableInvalidValue(this, fld, row, e));
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * @throws UploaderException
     * 
     * This is loads values from dataset into current DataModelObj record, but does not save record to the database.
     * It might be a good idea to track that data has been loaded to avoid unecessary repetition.
     */
    protected void loadFromDataSet() throws UploaderException
    {
        writeRowOrNot(true, true);
    }
    
    protected void writeRow() throws UploaderException
    {
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
     * Searches for matching record in database. If match is found it is set to be the current record.
     * If no match then a record is initialized and populated and written to the database.
     * 
     * @throws UploaderException
     */
    protected void writeRowOrNot(boolean doNotWrite, boolean forceMatch) throws UploaderException
    {
        int recNum = 0;
        log.debug("writeRowOrNot: " + this.table.getName());
        for (Vector<UploadField> seq : uploadFields)
        {
           try
            {
                if (!findMatch(recNum, forceMatch))
                {
                    DataModelObjBase rec = getCurrentRecordForSave(recNum);
                    rec.initialize();
                    setFields(rec, seq);
                    setRequiredFldDefaults(rec);
                    setParents(rec, recNum);
                    setRelatedDefaults(rec);
                    finalizeWrite(rec, recNum);
                    if (!doNotWrite)
                    {
                        doWrite(rec, recNum);
                        uploadedKeys.add(rec.getId());
                    }
                    setCurrentRecord(rec, recNum);
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
     * @param rec
     * @param recNum
     * @throws UploaderException
     * 
     * Creates session and saves rec.
     */
    protected void doWrite(DataModelObjBase rec, int recNum) throws UploaderException
    {
        boolean goodRec = dataToWrite(recNum) && isValid(recNum);
        boolean tblTransactionOpen = false;
        if (!isRequired())
        {
            goodRec = parentTables.size() > 0 || goodRec;
        }
        if (!goodRec) { throw new UploaderException("requred data missing",
                UploaderException.ABORT_ROW); }
        if (dataToWrite(recNum) || parentTables.size() > 0)
        {
            tblSession = DataProviderFactory.getInstance().createSession();
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
                    busRule.afterSaveCommit(rec);
                }
                tblSession.refresh(rec);
            }
            catch (Exception ex)
            {
                if (tblTransactionOpen)
                {
                    tblSession.rollback();
                }
                throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
            }
            finally
            {
                tblSession.close();
            }
        }
    }


    /**
     * Creates a new Hibernate session and associates the given objects with it.
     * NOTE: Static for testing reasons only.
     * @param objects the objects to associate with the new session
     * @return the newly created session
     */
/*    protected Session getNewSession(Object... objects)
    {
        log.trace("enter");

        Session session = HibernateUtil.getSessionFactory().openSession();
        for (Object o: objects)
        {
            if (o!=null)
            {
                // make sure not to attempt locking an unsaved object
                DataModelObjBase dmob = (DataModelObjBase)o;
                if (dmob.getId() != null)
                {
                    session.lock(o, LockMode.NONE);
                }
            }
        }
        log.trace("exit");
        return session;
    }
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
        if (fi == null)
        {
            return Integer.class;
        }
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
    public void undoUpload()
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        String hql = "from " + getWriteTable().getName() + " where id =:theKey";
        QueryIFace q = session.createQuery(hql);
        try
        {
            for (Object key : uploadedKeys)
            {
                try
                {
                    q.setParameter("theKey", key);
                    DataModelObjBase obj = (DataModelObjBase)q.uniqueResult();
                    session.beginTransaction();
                    session.delete(obj);
                    session.commit();
                }
                catch (Exception ex)
                {
                    //the delete may fail if another user has used or deleted uploaded records...
                    log.info(ex);
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
                if (otm == null && (jc != null || c != null || m.getName().equalsIgnoreCase("getId")))
                {
                    result.add(m);
                }

            }
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    public Vector<Vector<String>> printUpload() throws InvocationTargetException, IllegalAccessException
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        Vector<Method> getters = getGetters();
        Object[] args = new Object[0];
        Vector<Vector<String>> result = new Vector<Vector<String>>();
        try
        {
            String hql = "from " + tblClass.getSimpleName() + " obj where id=:theKey";
            QueryIFace qif = session.createQuery(hql);
            boolean wroteHeaders = false;
            for (Object key : uploadedKeys)
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
                        if (!getter.getName().equalsIgnoreCase("getId") && !getter.getName().equalsIgnoreCase("get" + getWriteTable().getName() + "Id"))
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
                    if (!getter.getName().equalsIgnoreCase("getId") && !getter.getName().equalsIgnoreCase("get" + getWriteTable().getName() + "Id"))
                    {
                        Object obj = getter.invoke(rec, args);
                        if (obj == null)
                        {
                            row.add(null);
                        }
                        else if (DataModelObjBase.class.isInstance(obj))
                        {
                            DataModelObjBase dbobj = (DataModelObjBase)obj;
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
        return DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName()).getTitle() + "_" + Uploader.currentUpload.getIdentifier();
    }
    
    /**
     * @return a recordset containing the the objects created during last upload.
     */
    public RecordSet getRecordSet()
    {
        RecordSet result = new RecordSet(getRecordSetName(), DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName()).getTableId());
        result.initialize();
        result.setSpecifyUser(SpecifyUser.getCurrentUser());
        for (Object key : uploadedKeys)
        {
            result.addItem(((Integer)key).intValue());
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
//        if (tblClass.equals(Agent.class))
//        {
//            if (relationship.getRelatedField().getFieldInfo() != null)
//            {
//                return relationship.getRelatedField().getFieldInfo().getTitle();
//            }
//            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName());
//            DBFieldInfo fldInfo = tblInfo.getFieldByColumnName(relationship.getRelatedField().getName());
//            if (fldInfo != null)
//            {
//                return fldInfo.getTitle();
//            }
//            List<DBRelationshipInfo> rels = tblInfo.getRelationships();
//            for (DBRelationshipInfo rel : rels)
//            {
//                System.out.println(rel.getName() + ", " + rel.getColName() + ", " + rel.getOtherSide() + ", " + rel.getClassName());
//            }
//            return "Blunderous Moo Cow";
//        }
        return DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName()).getTitle();
    }
    
    public UploadMatchSetting getMatchSetting()
    {
        return matchSetting;
    }
}
