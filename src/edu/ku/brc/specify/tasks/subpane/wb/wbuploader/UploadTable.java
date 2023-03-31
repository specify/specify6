/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.CascadeType;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.core.db.*;
import edu.ku.brc.specify.datamodel.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.ObjectDeletedException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.BusinessRulesIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.CriteriaIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules;
import edu.ku.brc.specify.dbsupport.RecordTypeCodeBuilder;
import edu.ku.brc.specify.dbsupport.SpecifyDeleteHelper;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Field;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Relationship;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Table;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader.ParentTableEntry;
import edu.ku.brc.specify.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.DateConverter;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.GeoRefConverter.GeoRefFormat;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.Pair;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 * 
 */
public class UploadTable implements Comparable<UploadTable>
{
    protected static boolean                          debugging               = true;
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
     * the dataset being uploaded. 'sequence' - e.g Collector1, Collector2 ...
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
     * true if this table was added to complete a relationship. E.g. collectingevent
     * if collectionobject and locality fields are mapped but no ce fields are.
     */
    protected boolean hiddenMissingLink = false;
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
    protected Triplet<List<UploadedRecordInfo>, SortedSet<UploadedRecordInfo>, SortedSet<Integer>>  uploadedRecs;
    
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
    
    protected char 										decSep = new DecimalFormatSymbols().getDecimalSeparator();
    
    /**
     * true if an edge in the import graph leads from this table. Used to determine whether to
     * search for matches before writing to the database.
     */
    protected boolean                                   hasChildren;

    /**
     * A vector of related tables that need to be checked when finding matching existing records.
     * For example: Collectors must be checked when matching collectingEvents.
     */
    protected Vector<UploadTable>                       specialChildren;
    protected AtomicBoolean						        skipChildrenMatching = new AtomicBoolean(false);
    
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
    protected boolean 									matchRecordId = false;	
    protected Integer									exportedRecordId = null;
    protected DataModelObjBase                          exportedRecord = null;
    protected boolean									reusingExportedRec = false;
    protected boolean                                   currentRecSetFromExportedRec = false;
    protected boolean                                   updateAddingNewRecord = false;

    //for a one-to-many child, exportedRecordId will hold the parent's ID,
    //exportedOneToManyID stores the child's recordID for the current 'sequence' - 1st collector, 2nd collector... 
    protected Integer 									exportedOneToManyId = null;
	protected boolean 									exportedOneToManyDelete = false;

    /**
     * i.e. is this a many of 1 - many. (eg: Determination 1, 2. Collector 1, 2, ...)
     */
    protected boolean                                   isSequenced                 = false;
    
    /**
     * If true then matching records are updated with values in uploading dataset.
     * 
     */
    protected boolean                                   updateMatches                = true;

    /**
     * If true then Match Status will be displayed
     */
    protected boolean									checkMatchInfo                = false;
    protected Integer[]                                 matchCountForCurrentRow;
    /**
	 * @return the multipleMatchCountForRow
	 */
	public Integer[] getMatchCountForCurrentRow() {
		return matchCountForCurrentRow;
	}



	/**
     * Used in processing new objects added as result of the UploadMatchSetting.ADD_NEW_MODE option.
     */
    protected Vector<MatchRestriction>              restrictedValsForAddNewMatch = null;


    protected boolean                                   validatingValues             = false;
    protected Object                                    autoAssignedVal              = null; //Assuming only one per table.
    protected Object									prevAutoAssignedVal			 = null; //For auto-incrementing. Assuming one per table.
    protected UploadField                               autoAssignedField            = null; //Assuming one per table.
    protected Collection                                collection                   = null;
    protected Discipline                                discipline                   = null;
    protected Division									division                     = null;
    
    UploadedRecFinalizerIFace                           finalizer                    = null;
    List<Pair<UploadField, Method>>                     precisionDateFields          = new LinkedList<Pair<UploadField, Method>>();
    
    protected boolean                                   isSecurityOn; //Storing this here since checking security now requires db access.
    
    protected GeoRefConverter                           geoRefConverter              = new GeoRefConverter();
    
    protected boolean 									isUploadRoot = false;
    
    protected boolean                                   plugHoles = true;    //if false then blank one-to-many records are allowed. 
    											                             //Eg. Prep2 fields are allowed to contain data when all prep1 fields are blank.
    protected List<Boolean>								blankSeqs = null;									

    protected boolean									deleteUnusedRecs			 = false;
    protected Set<Pair<Integer, String>>           		disUsedRecs		             = new TreeSet<Pair<Integer, String>>(new Comparator<Pair<Integer, String>>() {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Pair<Integer, String> arg0,
				Pair<Integer, String> arg1) {
			return arg0.getFirst().compareTo(arg1.getFirst());
		}
    	
    });

    protected List<Pair<Integer, String>>           	deletedRecs		             = new Vector<>();

    protected Map<Integer, Pair<DataModelObjBase, Timestamp>> recordStash            = new HashMap<>();

    public class Triplet<F, S, T> extends Pair<F, S> {
        public T third = null;

        public Triplet() {
            super();
        }
        public Triplet(F first, S second, T third) {
            super(first, second);
            this.third = third;
        }
        public T getThird() {
            return third;
        }
        public void setThird(T third) {
            this.third = third;
        }
    }
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
    public void setTblSession(DataProviderSessionIFace theSession) {
    	this.tblSession = theSession;
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
        uploadFields = new Vector<>();
        uploadedRecs = new Triplet<>();
        uploadedRecs.setFirst(new ArrayList<>());
        uploadedRecs.setSecond(new TreeSet<>());
        uploadedRecs.setThird(new TreeSet<>());

        currentRecords = new Vector<>();
        specialChildren = new Vector<>();
        relatedClassDefaults = null;
        dateConverter = new DateConverter();
        matchSetting = new UploadMatchSetting();
        plugHoles = !AppPreferences.getLocalPrefs().getBoolean("WB_UnPlugManyHoles", false);
    }

    /**
     * @return true if this table is a one-to-many or one-to-one child of some other table.
     */
    public boolean isMatchChild()
    {
    	for (Vector<ParentTableEntry> ptes : parentTables)
    	{
    		for (ParentTableEntry pte : ptes)
    		{
    			if (pte.getImportTable().specialChildren != null 
    					&& pte.getImportTable().specialChildren.contains(this)
    					&& pte.getImportTable().needToMatchChild(tblClass))
    			{
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * @return true if the findMatch step should be skipped.
     * 
     * Match-skipping is done to implement one-to-one relationships, which
     * currently must be annotated as many-to-ones in Hibernate.
     */
    protected boolean shouldSkipMatching()
    {
        return isOneToOneChild() 
        	|| (tblClass.equals(CollectingEvent.class) &&  AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent())
        	|| (tblClass.equals(PaleoContext.class) && AppContextMgr.getInstance().getClassObject(Discipline.class).getIsPaleoContextEmbedded());
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
        return isAttributeTbl()
        	|| tblClass.equals(GeoCoordDetail.class)
        	|| tblClass.equals(LocalityDetail.class);
       
    }
   
    /**
     * @return
     */
    public boolean isAttributeTbl() {
        return tblClass.equals(CollectionObjectAttribute.class)
                || tblClass.equals(PreparationAttribute.class)
                || tblClass.equals(CollectingEventAttribute.class)
                || tblClass.equals(CollectingTripAttribute.class);
    }
    
    public boolean isZeroToOneMany()
    {
        return tblClass.equals(GeoCoordDetail.class)
    		|| tblClass.equals(LocalityDetail.class);
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


    /**
     *
     */
    public void findPrecisionDateFields() {
        for (UploadField fld : uploadFields.get(0)) { //assuming all 'seqs' in uploadFields have the same fields.
            if (fld.getField() != null && fld.getField().getFieldInfo() != null) {
                DBFieldInfo precFld = this.getDatePrecisionFld(fld.getField().getFieldInfo());
                if (precFld != null) {
                    this.precisionDateFields.add(new Pair<UploadField, Method>(fld, this.getFldSetter(precFld)));
                }
            }
        }
    }

    /**
     *
     * @return
     * @throws UploaderException
     */
    protected UploadedRecFinalizerIFace findFinalizer() throws UploaderException {
        String className = this.getClass().getPackage().getName() + "." + tblClass.getSimpleName() + "RecFinalizer";
        try {
            Class<?> cls = Class.forName(className);
            return (UploadedRecFinalizerIFace )cls.newInstance();
        } catch (ClassNotFoundException ex) {
            return null;
        } catch (Exception ex) {
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        }
    }
    
    /**
     * Determines the Java class for the specify table being uploaded.
     * 
     * @throws UploaderException
     */
    protected void determineTblClass() throws UploaderException {
        try {
            tblClass = Class.forName("edu.ku.brc.specify.datamodel." + table.getName());
        } catch (ClassNotFoundException cnfEx) {
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
    public void prepareToUpload(boolean inTransaction) throws UploaderException {
    	isUploadRoot = uploader.getRootTable() == this;
        uploadedRecs.getFirst().clear();
        uploadedRecs.getSecond().clear();
        uploadedRecs.getThird().clear();
        matchSetting.clear();
        if (updateMatches) {
            matchSetting.setMode(UploadMatchSetting.PICK_FIRST_MODE);
        }
        deletedRecs.clear();
        disUsedRecs.clear();
        recordStash.clear();
        isSecurityOn = AppContextMgr.isSecurityOn();
        if (matchRecordId) {
        	for (UploadTable ut : specialChildren) {
        		ut.setSkipMatching(false);
        		ut.setMatchRecordId(true);
        	}
        }
        matchCountForCurrentRow = new Integer[uploadFields.size()];
    }

    /**
     * 
     */
    protected void adjustPlugHoles() {
        if (plugHoles && this.uploadFields.size() > 1) {
        	List<UploadTable> kids = uploader.getChildren(this);
        	if (kids.size() == 1 && kids.get(0).getUploadFields().size() > 1) {
        		plugHoles = false;
        	}
        }
    }
    
    /**
     * 
     */
    public void clearBlankness() {
    	if (blankSeqs ==  null) {
    		blankSeqs = new ArrayList<Boolean>(this.uploadFields.size());
    		for (int b = 0; b < this.uploadFields.size(); b++) {
    			blankSeqs.add(true);
    		}
    	} else {
    		for (int b = 0; b < this.blankSeqs.size(); b++) {
    			blankSeqs.set(b, true);
    		}
    	}
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

    
    protected boolean colIsNullable(javax.persistence.Column col)
    {
        if (!tblClass.equals(Locality.class) || 
        		!(col.name().equalsIgnoreCase("LatLongType") || 
        				col.name().equalsIgnoreCase("Latitude1") || col.name().equalsIgnoreCase("Longitude1") ||
        				col.name().equalsIgnoreCase("Latitude2") || col.name().equalsIgnoreCase("Longitude2")))
        {
        	boolean unRequiredInSchema = true;
        	DBTableInfo tbl = DBTableIdMgr.getInstance().getByShortClassName(this.tblClass.getSimpleName());
        	if (tbl != null) {
        		DBFieldInfo f = tbl.getFieldByColumnName(col.name());
        		if (f != null) {
        			unRequiredInSchema = !(f.isRequired());
        		}
        	}
        	return col.nullable() && unRequiredInSchema;
        }
        
    	//if 2nd geocoord is present then LatLongType is required
        for (Vector<UploadField> flds : uploadFields)
        {
             for (UploadField fld : flds)
             {
             	String fldName = fld.getField().getName();
                if (col.name().equalsIgnoreCase("LatLongType"))
                {
                	if (fldName.equalsIgnoreCase("Latitude2") || fldName.equalsIgnoreCase("Longitude2"))
                	{
                		return false;
                	}
                } else if (col.name().equalsIgnoreCase("Latitude1"))
                {
                	if (fldName.equalsIgnoreCase("LatLongType") || fldName.equalsIgnoreCase("Longitude1") || fldName.equalsIgnoreCase("Latitude2") || fldName.equalsIgnoreCase("Longitude2"))
                	{
                		return false;
                	}
                } else if (col.name().equalsIgnoreCase("Latitude2"))
                {
                	if (fldName.equalsIgnoreCase("Longitude2"))
                	{
                		return false;
                	}
                } else if (col.name().equalsIgnoreCase("Longitude1"))
                {
                	if (fldName.equalsIgnoreCase("LatLongType") || fldName.equalsIgnoreCase("Latitude1") || fldName.equalsIgnoreCase("Latitude2") || fldName.equalsIgnoreCase("Longitude2"))
                	{
                		return false;
                	}
                }
                else if (col.name().equalsIgnoreCase("Longitude2"))
                {
                	if (fldName.equalsIgnoreCase("Latitude2"))
                	{
                		return false;
                	}
                }


             }
        }
        return true;
    	
    }
    /**
     * @throws NoSuchMethodException
     * 
     * Builds vector of non-nullable fields in tblClass that are not present in the uploading
     * dataset. SIDE EFFECT: Required uploadFields, that are present may get their required properties set to true.
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
                if ((!colIsNullable(col)/* {Partial fix for issues with db level requirements versus sp requirement customizations} || DBTableIdMgr.getInstance().getByClassName(tblClass.getName()).getFieldByName(col.name()).isRequired()*/) 
                		&& !col.name().startsWith("Timestamp") 
                		&& !col.name().equalsIgnoreCase("srcLatLongUnit"))
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
                        if (table.getName().equalsIgnoreCase("collector") && fldName.equalsIgnoreCase("isprimary"))
                        {
                            //Creating a new DefaultFieldEntry class makes it possible to handle
                            //the conditional/parameterized default behavior of isPrimary without spreading these
                            //cheezy field by field conditions to code that is not yet infected.by cheezy fld by fld conditions.
                            dfe = new DefaultIsPrimaryEntry(this, m.getReturnType(),
                                    setter, fldName, null);
                        }
                        else
                        {
                            dfe = new DefaultFieldEntry(this, m.getReturnType(),
                                    setter, fldName, null);
                        }
                        missingRequiredFlds.add(dfe);
                    } else if (col.name().equalsIgnoreCase("LatLongType"))
                    {
                        //Find UploadField corresponding to field and make sure Required to true.
                        for (Vector<UploadField> ufs : uploadFields)
                        {
                            for (UploadField uf : ufs)
                            {
                                if (!uf.getField().isRequired() && uf.getField().getName().equalsIgnoreCase(col.name()))
                                {
                                    uf.setRequired(true);
                                }
                            }
                        }

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
    protected Vector<RelatedClassSetter> buildReqRelClasses() throws NoSuchMethodException  {
        Vector<RelatedClassSetter> result = new Vector<RelatedClassSetter>();
        for (Method m : tblClass.getMethods())  {
            Annotation a = m.getAnnotation(javax.persistence.JoinColumn.class);
            if (a != null)  {
                javax.persistence.JoinColumn jc = (javax.persistence.JoinColumn) a;
                logDebug(jc.columnDefinition());
                if (!jc.nullable() || m.getName().equals("getDivision"))  {
                    logDebug("adding required class: " + tblClass.getName() + " - " + m.getName());
                    javax.persistence.ManyToOne mto = m.getAnnotation(javax.persistence.ManyToOne.class);
                    if (mto != null)  {
                        CascadeType[] ct = mto.cascade();
                        for (int c=0; c<ct.length; c++)
                            logDebug(ct[c]);
                    }
                    Method setter = getSetterForGetter(m);
                    String fldName = jc.referencedColumnName();
                    if (fldName == null || fldName.equals(""))  {
                        fldName = jc.name();
                    }
                    if (addToReqRelClasses(m.getReturnType()))  {
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
     * @param relatedClass
     * @return true if the related class needs to be added as a requirement.
     */
    protected boolean addToReqRelClasses(Class<?> relatedClass)
    {
        return true;
    }

    
    /**
     * @param rce
     * @return true if a value for the related class can be determined by the uploader.
     * @throws ClassNotFoundException
     */
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
    protected void bldMissingReqRelClasses() throws ClassNotFoundException, UploaderException {
        relatedClassDefaults = new Vector<RelatedClassSetter>();
        for (RelatedClassSetter rce : this.requiredRelClasses) {
            //refresh for DeterminationStatusSetters...
            rce.refresh(this.uploadFields.size());
            if (!findValueForReqRelClass(rce)) {
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

    /**
     * @return
     */
    protected Vector<Field> getMissingReqLocalFlds()
    {
        return new Vector<Field>();
    }

    /**
     * @return
     * @throws UploaderException
     * @throws ClassNotFoundException
     */
    public Vector<InvalidStructure> verifyUploadability() throws UploaderException,
            ClassNotFoundException
    {
        Vector<InvalidStructure> result = new Vector<InvalidStructure>();
        String tblTitle = getTable().getTableInfo().getTitle();

        if (uploadFields.size() > 1)
        {
        	Integer fldCount = null;
        	int seq = 1;
        	for (Vector<UploadField> ups : uploadFields)
        	{
        		if (fldCount == null)
        		{
        			fldCount = ups.size();
        		}
        		else if (fldCount != ups.size())
        		{
                    String msg = String.format(getResourceString("WB_UPLOAD_INVALID_MANY_MAPPING"), seq) + ": " + tblTitle;
                    result.add(new InvalidStructure(msg, this));
        		}
        		seq++;
        	}
        }
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
        isSequenced = field.getSequence() != null;
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
            if (!f.getField().isForeignKey() //Foreign Keys get assigned later in process...
            		&& !"LatLongType".equalsIgnoreCase(f.getField().getName())) //cheap fix for bug#10280. Really seems like this method is no longer necessary but
            	                                                                //removing it would require broad and thorough testing.
            {
                if (StringUtils.isEmpty(f.getValue()) && f.isRequired() && f != autoAssignedField 
                		//&& !(autoAssignedField == null && f.isAutoAssignable())
                		&& !f.isAutoAssignable()) 
                { 
                    return false; 
                }
            }
        }
        return true;
    }

    /**
     * @param f
     * @return true if the field is irrelevant in determining whether it's record contains writable data
     */
    protected boolean ignoreFieldData(UploadField f)
    {
    	if (f.getField().getName().equalsIgnoreCase("ordernumber"))
    	{
    		return true;
    	}
    	return false;
    }
    /**
     * @param recNum
     * @return true if record is not empty and required data is present.
     */
    protected boolean dataToWrite(int recNum) {
        int index = uploadFields.size() > 1 ? recNum : 0;
        boolean result = false;
        for (UploadField f : uploadFields.get(index)) {
            if (!ignoreFieldData(f)) {
            	String val = f.getValue();
            	if (val != null) {
            		val = val.trim();
            	}
            	if (StringUtils.isNotEmpty(val) || f == autoAssignedField 
            			//|| (autoAssignedField == null && f.isAutoAssignable())
            			) {
            		result = true;
            		break;
            	}
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
     *
     * @return a flattened, not-necessarily ordered list of any table that must be uploaded before this table
     */
    public List<UploadTable> getAncestorTables() {
        List<UploadTable> result = new ArrayList<UploadTable>();
        List<ParentTableEntry> ancestors = this.getAncestors();
        for (ParentTableEntry pt : ancestors) {
            UploadTable t = pt.getImportTable();
            if (result.indexOf(t) == -1) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * @param parent
     * @return
     */
    public ParentTableEntry getParentTableEntry(final UploadTable parent) {
    	for (Vector<ParentTableEntry> ptes : parentTables) {
    		for (ParentTableEntry pte : ptes) {
    			if (pte.getImportTable() == parent) {
    				return pte;
    			}
    		}
    	}
    	return null;
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
                else if (needToMatchChildren(true) && pte.getImportTable().isOneToOneChild())
                {
                	addChild(pte.getImportTable());
                }
            }
        }
        assignParentSetters();
    }

    protected void addChild(final UploadTable child)
    {
        specialChildren.add(child);
    }

    /**
     * @return the uploadedKeys
     */
    public Triplet<List<UploadedRecordInfo>, SortedSet<UploadedRecordInfo>, SortedSet<Integer>> getUploadedRecs()
    {
        return uploadedRecs;
    }

    /**
     * @param index Specifies the 'sequence' (for one-to-many relationships).
     * @return Current (or last uploaded) record for this table.
     */
    public DataModelObjBase getCurrentRecord(int index) {
        if (currentRecords.size() == 0) {
            return null; 
        }
        if (index < uploadFields.size() && index > currentRecords.size() - 1) {
        	return null;
        }
        if (index > currentRecords.size() - 1) {
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
    protected Pair<Boolean, DataModelObjBase> getCurrentRecordForSave(int index) throws IllegalAccessException,
            InstantiationException {
        if (index > currentRecords.size() - 1 || currentRecords.get(index) == null) {
        	return new Pair<Boolean, DataModelObjBase>(true, createRecord());
        }
        return new Pair<Boolean, DataModelObjBase>(false, currentRecords.get(index));
    }

    /**
     * Stores most recently uploaded record. (NOTE: Currently, after calling 'set' the first time,
     * it is actually only necessary to call it again if findMatch method finds a match...)
     * 
     * @param rec
     * @param index
     */
    protected void setCurrentRecord(DataModelObjBase rec, int index) {
        while (currentRecords.size() < index + 1) {
            currentRecords.add(null);
        }
        currentRecords.set(index, rec);
    }

    /**
     * @param rec
     * @param seq
     */
    protected void loadMyRecord(DataModelObjBase rec, int seq) {
    	setCurrentRecord(rec, seq);
    }
    
    /**
     * @param pt
     * @return
     */
    protected boolean shouldLoadParentTbl(UploadTable pt)
    {
    	return !pt.specialChildren.contains(this) || !pt.needToMatchChild(tblClass);
    }
    
    /**
     * @param pte
     * @return 
     */
    protected boolean shouldClearParent(ParentTableEntry pte)
    {
    	return shouldLoadParentTbl(pte.getImportTable());
    }
    
    /**
     * 
     */
    protected void clearCurrentRecords() {
    	this.clearCurrents(true);
    }
    
    /**
     * @param checkParents
     */
    protected void clearCurrents(boolean checkParents)
    {
    	for (int r = 0; r < uploadFields.size(); r++)
    	{
    		setCurrentRecord(null, r);
    	}
    	if (checkParents) {
    		for (Vector<ParentTableEntry> ptes : parentTables)
    		{
    			for (ParentTableEntry pte : ptes)
    			{
    				if (shouldClearParent(pte))
    				{
    					pte.getImportTable().clearCurrentRecords();
    				}
    			}
    		}
    	}
    }
    
    /**
     * 
     */
    public void clearRecords() {
    	this.clearCurrents(false);
    }
    
    /**
     * @return
     */
    protected Pair<DataProviderSessionIFace, Boolean> getSession() {
    	if (this.tblSession != null) {
    		return new Pair<DataProviderSessionIFace, Boolean>(this.tblSession, false);
    	} else {
    		return new Pair<DataProviderSessionIFace, Boolean>(DataProviderFactory.getInstance().createSession(), true);
    	}
    }
    
    /**
     * @param sessObj
     */
    protected void getRidOfSession(Pair<DataProviderSessionIFace, Boolean> sessObj) {
    	if (sessObj.getSecond()) {
    		sessObj.getFirst().close();
    	}
    }

    /**
     * @return
     */
    protected DataModelObjBase getExportedRecord() {
        return getExportedRecord(wbCurrentRow);
    }

    /**
     *
     * @param row
     * @return
     */
    protected DataModelObjBase getExportedRecord(int row) {
        return getExportedRecord(row, false, null);
    }

    /**
     *
     * @param row
     * @param force
     * @return
     */
    protected DataModelObjBase getExportedRecord(int row, boolean force, final DataProviderSessionIFace sessArg) {
        if (exportedRecordId != null) {
            if (exportedRecord == null || !exportedRecord.getId().equals(exportedRecordId)) {
                Pair<DataModelObjBase, Timestamp> stashed = force ? null : recordStash.get(row);
                if (stashed == null || stashed.getFirst() == null /*what if it got deleted */ || !stashed.getFirst().getId().equals(exportedRecordId)) {
                    Pair<DataProviderSessionIFace, Boolean> sessObj = sessArg == null ? getSession() : new Pair<>(sessArg, false);
                    DataProviderSessionIFace session = sessObj.getFirst();
                    try {
                        DataModelObjBase obj = (DataModelObjBase) session.get(tblClass, exportedRecordId);
                        if (obj != null) {
                            obj.forceLoad();
                            exportedRecord = obj;
                            recordStash.put(row, new Pair<>(obj, new Timestamp(System.currentTimeMillis())));
                        }
                    } finally {
                        getRidOfSession(sessObj);
                    }
                } else {
                    exportedRecord = stashed.getFirst();
                }
            }
            return exportedRecord;
    	}
    	return null;
    }

    /**
     * @param row
     * @param id
     * @throws Exception
     */
    public void loadExportedRecord(final int row, final Integer id, boolean force) throws Exception { 
        exportedRecordId = id;
        Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
        try {
            DataModelObjBase rec = getExportedRecord(row, force, sessObj.getFirst());
            if (rec != null) {
                loadRecord(rec, 0);
            }
        } finally {
            getRidOfSession(sessObj);
        }
    }


    /**
     * @param c
     * @return
     */
    protected boolean isParentTable(UploadTable c)
    {
    	for (Vector<ParentTableEntry> ptes : parentTables)
    	{
    		for (ParentTableEntry pte : ptes)
    		{
    			if (pte.getImportTable() == c)
    			{
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * @param rec
     * @throws Exception
     */
    public void loadRecord(DataModelObjBase rec, int seq) throws Exception {
    	//XXX Updates - seq?
    	loadMyRecord(rec, seq);
    	for (Vector<ParentTableEntry> ptes : parentTables) {
    		for (ParentTableEntry pte : ptes) {
    			if (shouldLoadParentTbl(pte.getImportTable())) {
    				//System.out.println("loading " + pte.getImportTable());
    				if (rec != null) {
    					pte.getImportTable().loadRecord((DataModelObjBase )pte.getGetter().invoke(rec, (Object[] )null), seq);
    				} else {
    					pte.getImportTable().loadRecord(null, seq);
    				}
    			}
    		}
    	}
    	
    	List<UploadTable> childrenToLoad = new ArrayList<UploadTable>();
		for (UploadTable c : specialChildren) {
			if (!isParentTable(c) || !shouldLoadParentTbl(c)) { //Attribute tables are both parents and (obviously) special children.  
				c.clearCurrentRecords();
				childrenToLoad.add(c);
			}
		}
		
    	if (rec != null) {
    		for (UploadTable c : childrenToLoad) {
    			int cSeq = 0;
    			List<DataModelObjBase> childRecs = getChildRecords(c, rec);
    			for (DataModelObjBase childRec : childRecs) {
    				if (cSeq < c.uploadFields.size()) {
    					c.loadRecord(childRec, cSeq++);
    				}
    				else {
    					log.warn("Not loading " + c.getTblTitle() + " child "  + cSeq+1 + " into dataset because only " + c.uploadFields.size() + " children are mapped.");
    				}
    			}    			
    		}
    	}
    }
    /**
     * @param child
     * @return
     */
    protected Method getOneToManyChildGetter(UploadTable child)
    {
    	//Assumes only 1 one-to-many child relationship per child class
    	for (int tries = 0; tries < 2; tries++)
    	{
    		String name = "get" + child.getTblClass().getSimpleName() 
    			+ (tries == 0 ? "s" : "es");
    		try 
    		{
    			//System.out.println("getting method " + name);
    			return getTblClass().getMethod(name, (Class<?>[] )null);
    		} catch (NoSuchMethodException ex)
    		{
    			if (tries > 0)
    			{
    				return null;
    			}
    		}
    	}
    	return null;
    }
    
    /**
     * @param child
     * @param parentRec
     * @return
     */
	@SuppressWarnings("unchecked")
    protected List<DataModelObjBase> getChildRecords(UploadTable child, DataModelObjBase parentRec) throws Exception
    {
    	Vector<DataModelObjBase> result = new Vector<DataModelObjBase>();
    	Method getter = getOneToManyChildGetter(child);
    	if (getter != null)
    	{
    		Set<DataModelObjBase> set = (Set<DataModelObjBase> )getter.invoke(parentRec, (Object[] )null);
    		for (DataModelObjBase element : set)
    		{
    			result.add(element);
    		}
    		orderChildRecordsForLoad(child, result);
    	}
    	return result;
    }
    
	/**
	 * @param child
	 * @param childRecords
	 * @throws Exception
	 */
	protected void orderChildRecordsForLoad(final UploadTable child, final List<DataModelObjBase> childRecords) throws Exception
	{
		if (childRecords != null && childRecords.size() > 0)
		{
			RecordComparator rc = RecordComparator.createRecordComparator(child.getTblClass());
			if (rc != null)
			{
				Collections.sort(childRecords, rc);
			}
		}
	}

    /**
     *
     * @param rec
     * @return
     */
	protected boolean shouldSetExportedRec(final DataModelObjBase rec) {
	   return rec != null;
    }

    /**
     *
     * @param rec
     * @param idWasSet
     */
    protected DataModelObjBase getExportedRecIdForParent(final ParentTableEntry pte, final DataModelObjBase rec, boolean idWasSet) throws Exception {
        return (DataModelObjBase )pte.getGetter().invoke(rec, (Object[] )null);
    }
    /**
     * @param rec
     * @throws Exception
     */
    public void setExportedRecordId(DataModelObjBase rec) throws Exception {
    	boolean shouldSetExportedRec = shouldSetExportedRec(rec);
        if (!shouldSetExportedRec) {
    		exportedRecordId = null;
    		exportedRecord = null;
    	} else {
    		exportedRecordId = rec.getId();
    		exportedRecord = rec;
    	}
    	for (Vector<ParentTableEntry> ptes : parentTables) {
    		for (ParentTableEntry pte : ptes) {
    		    if (pte.getImportTable().getSpecialChildren().indexOf(this) == -1) {
                    if (rec == null) {
                        pte.getImportTable().setExportedRecordId(null);
                    } else {
                        pte.getImportTable().setExportedRecordId(getExportedRecIdForParent(pte, rec, shouldSetExportedRec));
                    }
                }
    		}
    	}
    	if (matchRecordId) {
    		for (UploadTable sut : specialChildren) {
    			if (sut.matchRecordId) {
    				if (sut.isAttributeTbl()) {
    					String fkey = sut.getTable().getTableInfo().getName() + "ID";
    					String key = getTable().getTableInfo().getIdFieldName(); 
    					String tbl = getTable().getTableInfo().getName().toLowerCase();
    					String sql = "select " + fkey + " from " + tbl + " where " + key + "=" + rec.getId();
    					List<?> id = queryForInts(sql, getSession().getFirst());
    					sut.exportedRecordId = id != null && id.size() > 0 ? (Integer)id.get(0) : null;
    				} else {
    				    if (rec == null) {
    				        sut.setExportedRecordId(null);
                        } else {
                            ParentTableEntry pte = sut.getParentTableEntry(this);
                            //This assumes all special kids use the parent primary key as a foreign key
                            String hql = "from " + sut.getTblClass().getSimpleName() + " where "
                                    + this.getTable().getTableInfo().getName() + "Id = " + rec.getId();
                            Pair<DataProviderSessionIFace, Boolean> sessInfo = getSession();
                            DataProviderSessionIFace sess = getSession().getFirst();
                            try {
                                QueryIFace matchesQ = sess.createQuery(hql, false);
                                List<?> matches = matchesQ.list();
                                if (matches != null && matches.size() > 0) {
                                    sut.setExportedRecordId((DataModelObjBase) matches.get(0));
                                } else {
                                    sut.exportedRecordId = null;
                                }
                            } finally {
                                getRidOfSession(sessInfo);
                            }
                        }
    				}
    			}
    		}
    	}
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
                //XXX This is mucho el cheapo. Probably should add an attribute to one the workbench xml file
                //to specify the member name when it does not match the class name
                //OR, if the ParentTableEntry.relationship included the relationship name (or was replaced
                //by a DBRelationshipInfo object), the name could serve as a second choice for the member name.
                if (tblClass.equals(Preparation.class) && setterName.equals("PreparedBy"))
                {
                    setterName = "PreparedByAgent";
                }
                else if (tblClass.equals(Determination.class)
                        && setterName.equals("DeterminationStatus"))
                {
                    setterName = "Status";
                }
                else if (tblClass.equals(GeoCoordDetail.class) && setterName.equals("Agent"))
                {
                	setterName = "GeoRefDetBy";
                }
                else if (tblClass.equals(GeoCoordDetail.class) && setterName.equals("CompiledBy"))
                {
                    setterName = "GeoRefCompiledBy";
                }
                else if (tblClass.equals(FieldNotebook.class) && setterName.equals("Agent"))
                {
                	setterName = "OwnerAgent";
                }
                else if (tblClass.equals(FieldNotebookPage.class) && setterName.equals("FieldNotebookPageSet"))
                {
                	setterName = "PageSet";
                } 
                else if (tblClass.equals(DNASequencingRun.class) && setterName.startsWith("DNA"))
                {
                	setterName = setterName.replace("DNA", "Dna");
                }
                else if (tblClass.equals(ConservEvent.class))
                {
                	//System.out.println(setterName);
                	log.info(setterName);
                } 
                else if (tblClass.equals(CollectionRelationship.class))
                {
                	if ("LeftSideCollection".equals(setterName) || "RightSideCollection".equals(setterName)) {
                		setterName = setterName.replace("Collection", "");
                	}
                		
                }
                pt.setSetter(tblClass.getMethod("set" + setterName, parType));
                pt.setGetter(tblClass.getMethod("get" + setterName, (Class<?>[] )null));
            }
        }
    }


    protected void addChildToParentSet(DataModelObjBase rec, DataModelObjBase parentRec, ParentTableEntry pt) {
        //fuck generality
        if (parentRec instanceof Locality) {
            if (rec instanceof GeoCoordDetail) {
                ((Locality)parentRec).getGeoCoordDetails().add((GeoCoordDetail)rec);
            } else if (rec instanceof LocalityDetail) {
                ((Locality)parentRec).getLocalityDetails().add((LocalityDetail)rec);
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
    protected boolean setParents(DataModelObjBase rec, int recNum, boolean isForWrite) throws InvocationTargetException,
            IllegalArgumentException, IllegalAccessException, UploaderException {
        boolean requirementsMet = true;
        boolean result = false;
        boolean isNewRecord = rec.getId() == null;    	
        for (List<ParentTableEntry> ptes : parentTables) {
            for (ParentTableEntry pt : ptes) {
                if (!updateMatches || isNewRecord || uploader.getUploadedTablesForCurrentRow().indexOf(pt.getImportTable()) != -1) {
                    Object arg[] = new Object[1];
                    DataModelObjBase parentRec = pt.getImportTable().getParentRecord(recNum, this);
                    if (parentRec == null || parentRec.getId() == null) {
                        arg[0] = null;
                    } else {
                        arg[0] = parentRec;
                    }
                    if (!isNewRecord && !result) {
                        result = valueChange(rec, pt.getGetter(), arg);
                        if (isForWrite && result && !pt.getImportTable().matchRecordId && pt.getImportTable().deleteUnusedRecs) {
                            addDisusedRec(rec, pt);
                        }
                    }
                    pt.getSetter().invoke(rec, arg);
                    if (updateMatches) {
                        addChildToParentSet(rec, parentRec, pt);
                    }
                    requirementsMet = requirementsMet && (arg[0] != null || !pt.isRequired());
                }
            }
        }
        if (!requirementsMet) {
        	throw new UploaderException("MissingRequiredParent", UploaderException.ABORT_ROW);
        }
        return result;
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
     * @param ufld
     * @return true if ufld represents a latitude field.
     * 
     * Assumes the field's type has already been determined BigDecimal.
     */
    protected boolean isLatFld(final UploadField ufld)
    {
        String name = ufld.getField().getName();
        name = name.substring(0, name.length()-1);
        return name.equalsIgnoreCase("latitude");
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
     * @return
     */
    protected DateConverter.DateFormats getDateConverterForWbExport()
    {
    	return DateConverter.DateFormats.LYEAR_MON_DAY;
    }
    
    /**
     * @return
     */
    protected String getDateSeparatorForWbExport()
    {
    	return "/";
    }
    
    /**
     * @param ufld
     * @param value
     * @param seq
     * @return
     * @throws Exception
     */
    public String getTextForFieldValue(UploadField ufld, Object value, int seq) throws Exception {
    	if (value == null) {
    		return null;
    	}
    	
    	Class<?> fldClass = ufld.getSetter().getParameterTypes()[0];
    	
    	String result = null;
        if (fldClass == java.util.Calendar.class || fldClass == java.util.Date.class) {
            DateConverter.DateFormats df = getDateConverterForWbExport();
        	SimpleDateFormat sdf = new SimpleDateFormat(df.getFormatString(getDateSeparatorForWbExport()));	
        	result = sdf.format(((Calendar )value).getTime());
            if (isDateWithPrecision(ufld.getField().getFieldInfo())) {
            	Integer precision = BasicSQLUtils.querySingleObj("select " + getDatePrecisionFld(ufld.getField().getFieldInfo()).getColumn() 
            			+ " from " + getTable().getTableInfo().getName() + " where " + getTable().getTableInfo().getIdColumnName()
            			+ " = " + getCurrentRecord(seq).getId());
            	UIFieldFormatterIFace.PartialDateEnum prec = UIFieldFormatterIFace.PartialDateEnum.None;
            	if (precision != null) {
            		for (UIFieldFormatterIFace.PartialDateEnum pde : UIFieldFormatterIFace.PartialDateEnum.values()) {
            			if (pde.ordinal() == precision) {
            				prec = pde;
            				break;
            			}
            		}
            	}
            	result = df.adjustForPrecisionOut(result, prec);
            }
        } else {
            UIFieldFormatterIFace formatter = ufld.getField().getFieldInfo().getFormatter();
            String pickListName = ufld.getField().getFieldInfo().getPickListName();
            if (formatter != null) {
            	result = formatter.formatToUI(value).toString();
            } else if (isSpSystemTypeFld(ufld)) {
            	result = getSystemTypeCodeText(value);
            } else if (isLatLongFld(ufld)) {
            	result = getLatLongText(ufld, value);
            }	else if (StringUtils.isNotBlank(pickListName)) {
            	PickListDBAdapterIFace pl = PickListDBAdapterFactory.getInstance().create(pickListName, false);
            	//this could get slow...
            	String val = value.toString();
            	if (StringUtils.isNotBlank(val)) {
            		for (PickListItemIFace i : pl.getList()) {
            			if (val.equalsIgnoreCase(i.getTitle()) || val.equalsIgnoreCase(i.getValue())) {
            				val = i.getTitle();
            				break;
            			}
            		}
            	}
            	result = val;
            } else {
            	result = value.toString();
            }
        }    
        return result;
    }
    
    /**
     * @param ufld
     * @param value
     * @return lat/long in text format saved in the sp db,
     * 	or in DecimalDegree format in case of no formatted text in db
     */
    protected String getLatLongText(UploadField ufld, Object value) {
    	if (value != null && 
    	    ufld  != null && 
    	    ufld.getField() != null && 
    	    ufld.getField().getName() != null) {
    		
    		/*
    		String result = getLatLongTextFldVal(ufld.getField().getName());
    		if (result == null) {
    			LatLonConverter.LATLON ll = isLatFld(ufld) ? LatLonConverter.LATLON.Latitude : LatLonConverter.LATLON.Longitude;
    			result = LatLonConverter.format((BigDecimal)value, ll, LatLonConverter.FORMAT.DDDDDD, LatLonConverter.DEGREES_FORMAT.None,  
    					LatLonConverter.DECIMAL_SIZES[LatLonConverter.FORMAT.DDDDDD.ordinal()]);
    		}
    		if (result != null) {
    			result = result.replace(':', ' ');
    		}     		
    		//there may be issues with decimal places. WB enforces limits in
    		//LatLonConverter.DECIMAL_SIZES[], but the forms don't seem to
    		*/
    		

    		String textVal = getLatLongTextFldVal(ufld.getField().getFieldInfo().getColumn());
    		LatLonConverter.FORMAT frm = getOriginalLatLngUnit();
            String result;
    		if (StringUtils.isNotEmpty(textVal) && isTextValConsistentWithLLFormat(textVal, frm)) {
    		    result = textVal.trim().replaceAll("  ", " ").replaceAll("  ", " ");
    		    result = result.replaceAll(":", " ");
            } else {
                frm = frm == LatLonConverter.FORMAT.None ? LatLonConverter.FORMAT.DDDDDD : frm;
                LatLonConverter.LATLON ll = isLatFld(ufld) ? LatLonConverter.LATLON.Latitude : LatLonConverter.LATLON.Longitude;
                BigDecimal dval = (BigDecimal) value;
			    result =LatLonConverter.format(dval, ll, frm, LatLonConverter.DEGREES_FORMAT.None,
                        LatLonConverter.DECIMAL_SIZES[frm.ordinal()]);
                if (dval.abs() != dval && !result.startsWith("-")) {
                    result = "-" + result;
                }
            }
    		return result;
    	}
    	return null;
    }

    /**
     *
     * @param textVal
     * @param frm
     * @return
     */
    private boolean isTextValConsistentWithLLFormat(final String textVal, final LatLonConverter.FORMAT frm) {
        //wtf? wtfc? htfdik?
        return StringUtils.isNotEmpty(textVal);
    }
    /**
     * @param fldName
     * @return
     */
    protected String getLatLongTextFldVal(String fldName) {
    	if ("latitude1".equalsIgnoreCase(fldName)) {
    		return ((Locality )getCurrentRecord(0)).getLat1text();
    	}
    	if ("latitude2".equalsIgnoreCase(fldName)) {
    		return ((Locality )getCurrentRecord(0)).getLat2text();
    	}
    	if ("longitude1".equalsIgnoreCase(fldName)) {
    		return ((Locality )getCurrentRecord(0)).getLong1text();
    	}
    	if ("longitude2".equalsIgnoreCase(fldName)) {
    		return ((Locality )getCurrentRecord(0)).getLong2text();
    	}
    	return null;
    }
    
    /**
     * @return
     */
    protected LatLonConverter.FORMAT getOriginalLatLngUnit() {
    	Integer val = ((Locality)getCurrentRecord(0)).getOriginalLatLongUnit();
    	if (val == null) {
    		return LatLonConverter.FORMAT.None;
    	} else {
    		return LatLonConverter.convertIntToFORMAT(val);
    	}
    }

    @SuppressWarnings("unchecked")
    protected String getSystemTypeCodeText(Object value) 
    {
    	//Assuming this is never called unless isSpSystemTypeFld(ufld) returns true.

    	if (!(value instanceof Number))
    	{
    		return value.toString();
    	}

    	try
    	{
    		Method textGetter = this.tblClass.getMethod("getSpSystemTypeCodes");
    		try 
    		{
    			List<PickListDBAdapterIFace> lsts = (List<PickListDBAdapterIFace>) textGetter.invoke(null);
    			//Assuming there is only one
    			return lsts.get(0).getItem(((Number)value).intValue()).getTitle();
    		} catch (Exception e) 
    		{
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UploadTable.class, e);
                log.error(e);    	
                return null;
    		}
    	} catch (NoSuchMethodException mex)
    	{
    		return null;
    	}
    	
    	
    }
    /**
     * @param ufld
     * @return
     */
    protected boolean isSpSystemTypeFld(UploadField ufld)
    {
    	try
    	{
    		Method typeFldGetter = this.tblClass.getMethod("getSpSystemTypeCodeFlds");
    		try 
    		{
    			String[] typeFlds = (String[]) typeFldGetter.invoke(null);
    			for (String f : typeFlds)
    			{
    				if (f.equalsIgnoreCase(ufld.getField().getName()))
    				{
    					return true;
    				}
    			}
    			return false;
    		} catch (Exception e) 
    		{
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(UploadTable.class, e);
                log.error(e);    	
                return false;
    		}
    	} catch (NoSuchMethodException mex)
    	{
    		return false;
    	}
    	
    }
    
    /**
     * @param fldConfigs
     */
    public void copyFldConfigs(UploadField[] fldConfigs) {
    	if (fldConfigs != null) {
    		for (UploadField uf : fldConfigs) {
    			copyFldConfig(uf);
    		}
    	}
    }
    
    /**
     * @param fld
     * @return
     */
    private boolean isFieldFromMyTable(UploadField fld) {
    	return fld.getField().getFieldInfo() != null
    		&& fld.getField().getFieldInfo().getTableInfo().getTableId() ==
				table.getTableInfo().getTableId();
    }
    
    /**
     * @param fld1
     * @param fld2
     * 
     * Assumes fld2 has DBFieldInfo
     * 
     * @return
     */
    private boolean isFieldNameMatch(UploadField fld1, UploadField fld2) {
    	return fld1.getField().getFieldInfo() != null
				&& fld1.getField().getFieldInfo().getName().equals(fld2.getField().getFieldInfo().getName());
    }
    
    /**
     * @param fldConfig
     */
    public void copyFldConfig(UploadField fldConfig) {
    	if (isFieldFromMyTable(fldConfig)) {
    		for (List<UploadField> ufs : uploadFields) {
    			for (UploadField uf : ufs) {
    				if (isFieldNameMatch(uf, fldConfig)) {
    					uf.copyConfig(fldConfig);
    					break;
    				}
    			}
    		}
    	}
    }
    
    /**
     * @param formatter
     * @return true if formatter has variable components and should be use for autofilling
     */
    protected boolean hasInconstants(UIFieldFormatterIFace formatter) {
    	for (UIFieldFormatterField f : formatter.getFields()) {
    		if (f.getType().equals(UIFieldFormatterField.FieldType.alpha) || f.getType().equals(UIFieldFormatterField.FieldType.alphanumeric)
    				||  f.getType().equals(UIFieldFormatterField.FieldType.anychar)) {
    			return true;
    		}
    	}
    	return false;
    }
    /**
     * @return
     */
    public List<UploadField> getAutoAssignableFields() {
    	List<UploadField> result = new ArrayList<UploadField>();
    	for (List<UploadField> ufs : uploadFields) {
    		for (UploadField uf : ufs) {
    			if (uf.getField().getFieldInfo() != null) {
    				UIFieldFormatterIFace formatter = uf.getField().getFieldInfo().getFormatter();
    				if (formatter != null 
    						/*&& (formatter.isNumeric() || (formatter instanceof CatalogNumberUIFieldFormatter && ((CatalogNumberUIFieldFormatter)formatter).isNumericCatalogNumber())) && */
    						&& formatter.isIncrementer() && !hasInconstants(formatter)) {
    					result.add(uf);
    				}
    			}
    		}
    	}
    	return result;
    }
    
    /**
     *
     * @param ufld
     * @return values of the correct class for fld's setter.
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws UploaderException
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
               if ("".equals(fldStr.trim())) {
            	   fldStr = null;
               }
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
                    else
                    {
                    	//need to do this because even with lenient = false, Calendar.parse
                    	//will still interpret '00/Jun/2004' as '31/May/2004'.
                    	try
                    	{
                    		UIFieldFormatterIFace.PartialDateEnum prec = dateConverter.getDatePrecision(fldStr);
                    		if (prec.equals(UIFieldFormatterIFace.PartialDateEnum.Month)
                    				|| prec.equals(UIFieldFormatterIFace.PartialDateEnum.Year))
                    		{
                    			ParseException pex = new ParseException(UIRegistry.getResourceString("WB_UPLOAD_INVALID_FORMAT"), 0);
                    			throw new UploaderException(pex, UploaderException.INVALID_DATA);
                    		}
                    	}
                    	catch (ParseException pex)
                    	{
                    		//ignore. Problem should get caught in dateConverter.convert call below.
                    	}
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
                    if (!isLatLongFld(ufld))
                    {
                    	if (!ufld.checkPrecisionAndScale(fldStr)) 
                    	{
                    		//System.out.println("bam");
                    		throw new UploaderException(String.format(getResourceString("WB_UPLOAD_INVALID_PREC_SCALE"), 
                    				ufld.getPrecision() - ufld.getScale(), ufld.getScale()),
                    				UploaderException.INVALID_DATA);
                    	}
                    }
                    else
                    {
                    	boolean gotANumber = UIHelper.parseDoubleToBigDecimal(fldStr) != null;
                    	if (!gotANumber)
                    	{
                    		try
                    		{
                    			fldStr = geoRefConverter.convert(StringUtils.stripToNull(fldStr), GeoRefFormat.D_PLUS_MINUS.name());
                    		}
                    		catch (Exception ex)
                    		{
                    			throw new UploaderException(ex, UploaderException.INVALID_DATA);
                    		}
                    	}
                    }
                    BigDecimal val = UIHelper.parseDoubleToBigDecimal(fldStr);
                    if (StringUtils.isNotEmpty(fldStr) && val == null) {
                    	throw new UploaderException(UIRegistry.getResourceString("WB_UPLOAD_INVALID_FORMAT"),
                    			UploaderException.INVALID_DATA);
                    }
                    if (isLatLongFld(ufld))
                    {
                    	Double maxVal =  isLatFld(ufld) ? new Double("90") : new Double("180");
                    	if (Math.abs(val.doubleValue()) > maxVal)
                    	{
                    		throw new UploaderException(getResourceString("WB_UPLOAD_INVALID_GEOREF_VALUE"), UploaderException.INVALID_DATA);
                    	}
                    }
                    arg[0] = val;
                    
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
                    for (i = 0; i < WorkbenchTask.boolStrings.length; i++)
                    {
                        if (fldStr.equalsIgnoreCase(WorkbenchTask.boolStrings[i]))
                            break;
                    }
                    if (i == WorkbenchTask.boolStrings.length) { throw new UploaderException(
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
                if (StringUtils.isBlank(fldStr) && (formatter == null || !formatter.isIncrementer()/* || !formatter.isNumeric()*/))
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
                            if (isUploadRoot && StringUtils.isBlank(fldStr) && formatter.isIncrementer() 
                            		&& ufld.isAutoAssignForUpload())
                            {
                                if (!this.validatingValues || autoAssignedVal == null)
                                {
                                	if (autoAssignedVal == null)
                                	{
                                		if (prevAutoAssignedVal != null) 
                                		{
                                			val = formatter.getNextNumber(formatter.formatFromUI(prevAutoAssignedVal.toString()).toString(), true);
                                		} else
                                		{
                                			val = formatter.getNextNumber(formatter.formatToUI("").toString());
                                		}
                                		// XXX timo - Need to check here for a null return value.
                                		autoAssignedVal = formatter.formatToUI(val);
                                		prevAutoAssignedVal = autoAssignedVal;
                                	} else if (!this.validatingValues)
                                    {
                                    	val = formatter.formatFromUI(autoAssignedVal);
                                    }

                                }                                 
                                if (autoAssignedField == null)
                                {
                                    autoAssignedField = ufld;
                                }
                            }
                            else
                            {
                                if (StringUtils.isBlank(fldStr))
                                {
                                	if (!this.validatingValues && ufld.isAutoAssignable() && ufld.isAutoAssignForUpload()) {
                                		val = formatter.getNextNumber(formatter.formatToUI("").toString());
                                	} else {
                                		val = null;
                                	}
                                }
                                else
                                {
                                	val = formatter.formatFromUI(fldStr);
                                	if (!formatter.isValid((String)val))
                                	{
                                		throw new UploaderException(UIRegistry.getResourceString("WB_UPLOAD_INVALID_FORMAT"), UploaderException.INVALID_DATA);
                                	}
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
     *
     * @param rec
     * @param getter
     * @param newVal
     * @return true if fields value has changed.
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    protected boolean valueChange(DataModelObjBase rec, Method getter, Object[] newVal) 
    	throws InvocationTargetException, IllegalAccessException {
		boolean result = false;
		Object newValObj = newVal[0]; 
		if (getter != null && rec != null) {
			Object currentVal = getter.invoke(rec);
			if (currentVal == null ^ newValObj == null) {
				result = true;
			} else if (currentVal != null && newValObj != null) {
				if (currentVal instanceof DataModelObjBase) {
					Integer currentValId = ((DataModelObjBase )currentVal).getId();
					Integer newValObjId = ((DataModelObjBase )newValObj).getId();
					if (currentValId == null ^ newValObjId == null) {
						result = true;
					} else {
						result = currentValId.longValue() != newValObjId.longValue();
					}
				}
				else if (currentVal instanceof Comparable<?>) {
					result = ((Comparable<Object> )currentVal).compareTo((Comparable<?> )newValObj) != 0;
				}
				else {
					result = !currentVal.equals(newValObj); //how well will this work?
				}
			}
		} else {
			if (rec == null && newValObj != null) {
				result = true;
			}
		}
    	return result;
    }
	
    /**
     * @param rec
     * @param flds
     * @return true if rec was modified else false.
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws UploaderException
     * 
     * Calls each upload field's setter for values in current row of uploading dataset.
     */
    protected boolean setFields(DataModelObjBase rec, Vector<UploadField> flds)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
            UploaderException
    {
        boolean result = false;
        boolean isNewRecord = rec.getId() == null;    	
        for (UploadField fld : flds)
        {
            Method setter = fld.getSetter();
            if (setter != null)
            {
            	Object[] arg = getArgForSetter(fld);
            	if (!isNewRecord && !result)
            	{
            		result = valueChange(rec, fld.getGetter(), arg);
            	}
        		setter.invoke(rec, arg);
            }
        }
    	return result;
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

    /**
     * @param recNum
     * @return true if children for matching records match the children of this record.
     * 
     * Also will delete and clear matched one-to-one children (i.e. XXXAttribute tables)
     * 
     * @throws UploaderException
     */
    protected boolean checkChildrenMatch(int recNum) throws UploaderException
    {
        boolean result = true;
        DataModelObjBase match = getCurrentRecord(recNum);
        Vector<UploadTable> deletes = new Vector<UploadTable>();
        logDebug("Checking to see if children match:" + tblClass.toString() + "=" + match.getId());
        if (tblClass.equals(CollectingEvent.class)) {
            for (UploadTable child : specialChildren) {
                logDebug(child.getTable().getName());
                if (child.getTblClass().equals(Collector.class)) {
                    Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
            		DataProviderSessionIFace matchSession = sessObj.getFirst();
                    try {
                        QueryIFace matchesQ = matchSession
                                .createQuery("from Collector where collectingeventid = "
                                        + match.getId() + " order by orderNumber", false);
                        List<?> matches = matchesQ.list();
                        try {
                        	child.loadFromDataSet(wbCurrentRow);
                        	int childCount = 0;
                        	for (int c = 0; c < child.getUploadFields().size(); c++) {
                        		if (child.getCurrentRecord(c) != null) {
                        			childCount++;
                        		}
                        	}
                        	if (matches.size() != childCount) {
                        		result = false;
                        	} else {
                        		for (int rec = 0; rec < matches.size(); rec++) {
                        			Collector coll1 = (Collector) matches.get(rec);
                        			Collector coll2 = (Collector) child.getCurrentRecord(rec);
                        			if (!coll1.getOrderNumber().equals(coll2.getOrderNumber())) {
                        				// maybe this doesn't really need to be checked?
                        				return false;
                        			}
                        			if (coll2.getAgent() == null || !coll1.getAgent().getId().equals(coll2.getAgent().getId())) {
                        				return false;
                        			}
                        			if (coll2.getRemarks() == null ^ coll1.getRemarks() == null) {
                        				return false;
                        			} else if (coll2.getRemarks() != null && !coll2.getRemarks().equals(coll1.getRemarks())) {
                        				return false;
                        			}
//                        			if (!coll1.getIsPrimary().equals(coll2.getIsPrimary() == null ? false : coll2.getIsPrimary()))
//                        			{
//                        				return false;
//                        			}
                        		}
                        	} 
                        } finally {
                        		child.loadFromDataSet(child.wbCurrentRow);
                        }
                    }
                    finally {
            			getRidOfSession(sessObj);
                    }
                }
                else if (child.getTblClass().equals(CollectingEventAttribute.class))
                {
                    Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
            		DataProviderSessionIFace matchSession = sessObj.getFirst();
                    try
                    {
                    	String hql = "from CollectingEventAttribute where collectingEventAttributeId ";
                    	CollectingEvent ceMatch = (CollectingEvent )match;
                    	if (ceMatch.getCollectingEventAttribute() == null)
                    	{
                    		hql += "is null";
                    	}
                    	else
                    	{
                    		hql += "= " + ((CollectingEvent )match).getCollectingEventAttribute().getId();
                    	}
                    	QueryIFace matchesQ = matchSession
                        	.createQuery(hql, false);
                    	List<?> matches = matchesQ.list();
                    	try
                    	{
                    		child.loadFromDataSet(wbCurrentRow); 
                    		CollectingEventAttribute cea2 = (CollectingEventAttribute) child.getCurrentRecord(0);
                    		if (cea2 == null && matches.size() == 0)
                    		{
                    			continue;
                    		}
                    		if (cea2 == null && matches.size() != 0)
                    		{
                    			return false;
                    		}
                    		if (cea2 != null && matches.size() == 0)
                    		{
                    			return false;
                    		}
                    		CollectingEventAttribute cea1 = (CollectingEventAttribute) matches.get(0);
                    		result = cea1.matches(cea2);
                    		if (result)
                    		{
                    			//need to delete already-created "child" (due to weird hibernate 1-1 config requirements)
                    			deletes.add(child);
                    		}
                    	} finally
                    	{
                    		child.loadFromDataSet(child.wbCurrentRow);
                    	}
                    }
                    finally
                    {
            			getRidOfSession(sessObj);
                    }
                }
                else if (child.getTblClass().equals(CollectingEventAuthorization.class))
                {
                    Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
                    DataProviderSessionIFace matchSession = sessObj.getFirst();
                    try
                    {
                        QueryIFace matchesQ = matchSession
                                .createQuery("from CollectingEventAuthorization where collectingEventid = "
                                        + match.getId(), false);
                        List<?> matches = matchesQ.list();
                        try
                        {
                            child.loadFromDataSet(wbCurrentRow);
                            int childCount = 0;
                            for (int c = 0; c < child.getUploadFields().size(); c++)
                            {
                                if (child.getCurrentRecord(c) != null)
                                {
                                    childCount++;
                                }
                            }
                            if (matches.size() != childCount)
                            {
                                return false;
                            }
                            for (int rec = 0; rec < matches.size(); rec++)
                            {
                                CollectingEventAuthorization au1 = (CollectingEventAuthorization) matches.get(rec);
                                CollectingEventAuthorization au2 = (CollectingEventAuthorization) child.getCurrentRecord(rec);
                                if (!au1.getPermit().getId().equals(au2.getPermit().getId()))
                                {
                                    return  false;
                                }
                            }
                        } finally
                        {
                            child.loadFromDataSet(child.wbCurrentRow);
                        }
                    }
                    finally
                    {
                        getRidOfSession(sessObj);
                    }

                }
                if (!result)
                {
                    break;
                }
            }
        }
        else if (tblClass.equals(CollectingTrip.class)) {
            for (UploadTable child : specialChildren) {
                logDebug(child.getTable().getName());
                if (child.getTblClass().equals(CollectingTripAttribute.class))
                {
                    Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
                    DataProviderSessionIFace matchSession = sessObj.getFirst();
                    try
                    {
                        String hql = "from CollectingTripAttribute where collectingTripAttributeId ";
                        CollectingTrip ceMatch = (CollectingTrip )match;
                        if (ceMatch.getCollectingTripAttribute() == null)
                        {
                            hql += "is null";
                        }
                        else
                        {
                            hql += "= " + ((CollectingTrip )match).getCollectingTripAttribute().getId();
                        }
                        QueryIFace matchesQ = matchSession
                                .createQuery(hql, false);
                        List<?> matches = matchesQ.list();
                        try
                        {
                            child.loadFromDataSet(wbCurrentRow);
                            CollectingTripAttribute cea2 = (CollectingTripAttribute) child.getCurrentRecord(0);
                            if (cea2 == null && matches.size() == 0)
                            {
                                continue;
                            }
                            if (cea2 == null && matches.size() != 0)
                            {
                                return false;
                            }
                            if (cea2 != null && matches.size() == 0)
                            {
                                return false;
                            }
                            CollectingTripAttribute cea1 = (CollectingTripAttribute) matches.get(0);
                            result = cea1.matches(cea2);
                            if (result)
                            {
                                //need to delete already-created "child" (due to weird hibernate 1-1 config requirements)
                                deletes.add(child);
                            }
                        } finally
                        {
                            child.loadFromDataSet(child.wbCurrentRow);
                        }
                    }
                    finally
                    {
                        getRidOfSession(sessObj);
                    }
                }
                else if (child.getTblClass().equals(CollectingTripAuthorization.class))
                {
                    Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
                    DataProviderSessionIFace matchSession = sessObj.getFirst();
                    try
                    {
                        QueryIFace matchesQ = matchSession
                                .createQuery("from CollectingTripAuthorization where collectingTripid = "
                                        + match.getId(), false);
                        List<?> matches = matchesQ.list();
                        try
                        {
                            child.loadFromDataSet(wbCurrentRow);
                            int childCount = 0;
                            for (int c = 0; c < child.getUploadFields().size(); c++)
                            {
                                if (child.getCurrentRecord(c) != null)
                                {
                                    childCount++;
                                }
                            }
                            if (matches.size() != childCount)
                            {
                                return false;
                            }
                            for (int rec = 0; rec < matches.size(); rec++)
                            {
                                CollectingTripAuthorization au1 = (CollectingTripAuthorization) matches.get(rec);
                                CollectingTripAuthorization au2 = (CollectingTripAuthorization) child.getCurrentRecord(rec);
                                if (!au1.getPermit().getId().equals(au2.getPermit().getId()))
                                {
                                    return  false;
                                }
                            }
                        } finally
                        {
                            child.loadFromDataSet(child.wbCurrentRow);
                        }
                    }
                    finally
                    {
                        getRidOfSession(sessObj);
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
            for (UploadTable child : specialChildren)
            {
                logDebug(child.getTable().getName());
                if (child.getTblClass().equals(AccessionAgent.class))
                {
                    Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
            		DataProviderSessionIFace matchSession = sessObj.getFirst();
                    try
                    {
                        QueryIFace matchesQ = matchSession
                                .createQuery("from AccessionAgent where accessionid = "
                                        + match.getId(), false);
                        List<?> matches = matchesQ.list();
                        try
						{
							child.loadFromDataSet(wbCurrentRow);
							int childCount = 0;
                        	for (int c = 0; c < child.getUploadFields().size(); c++)
                        	{
                        		if (child.getCurrentRecord(c) != null)
                        		{
                        			childCount++;
                        		}
                        	}
							if (matches.size() != childCount)
							{
								return false;
							}
							for (int rec = 0; rec < matches.size(); rec++)
							{
								AccessionAgent ag1 = (AccessionAgent) matches.get(rec);
								AccessionAgent ag2 = (AccessionAgent) child.getCurrentRecord(rec);
								if (!(ag1.getAgent() == null && ag2.getAgent() == null))
								{
									if ((ag1.getAgent() == null ^ ag2.getAgent() == null) ||  !ag1.getAgent().getId().equals(ag2.getAgent().getId()))
									{
										return false;
									}
								}
							}
						} finally
						{
							child.loadFromDataSet(child.wbCurrentRow);
						}
                    }
                    finally
                    {
            			getRidOfSession(sessObj);
                    }

                }
                else if (child.getTblClass().equals(AccessionAuthorization.class))
                {
                    Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
            		DataProviderSessionIFace matchSession = sessObj.getFirst();
                    try
                    {
                        QueryIFace matchesQ = matchSession
                                .createQuery("from AccessionAuthorization where accessionid = "
                                        + match.getId(), false);
                        List<?> matches = matchesQ.list();
                        try
						{
							child.loadFromDataSet(wbCurrentRow);
							int childCount = 0;
                        	for (int c = 0; c < child.getUploadFields().size(); c++)
                        	{
                        		if (child.getCurrentRecord(c) != null)
                        		{
                        			childCount++;
                        		}
                        	}
							if (matches.size() != childCount)
							{
								return false;
							}
							for (int rec = 0; rec < matches.size(); rec++)
							{
								AccessionAuthorization au1 = (AccessionAuthorization) matches.get(rec);
								AccessionAuthorization au2 = (AccessionAuthorization) child.getCurrentRecord(rec);
								if (!au1.getPermit().getId().equals(au2.getPermit().getId()))
								{
									return  false;
								}
							}
						} finally
						{
							child.loadFromDataSet(child.wbCurrentRow);
						}
                    }
                    finally
                    {
            			getRidOfSession(sessObj);
                    }

                }
                else if (!result)
                {
                    break;
                }
            }
        }
        else if (tblClass.equals(Agent.class))
        {
            for (UploadTable child : specialChildren)
            {
                logDebug(child.getTable().getName());
                if (child.getTblClass().equals(Address.class))
                {
                    Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
            		DataProviderSessionIFace matchSession = sessObj.getFirst();
                    try
                    {
                        QueryIFace matchesQ = matchSession
                                .createQuery("from Address where agentId = "
                                        + match.getId(), false);
                        List<?> matches = matchesQ.list();
                        try
						{
							child.loadFromDataSet(wbCurrentRow);
							int childCount = 0;
                        	for (int c = 0; c < child.getUploadFields().size(); c++)
                        	{
                        		if (child.getCurrentRecord(c) != null)
                        		{
                        			childCount++;
                        		}
                        	}
							if (matches.size() != childCount)
							{
								return false;
							}
							for (int rec = 0; rec < matches.size(); rec++)
							{
								Address ad1 = (Address) matches.get(rec);
								Address ad2 = (Address) child.getCurrentRecord(rec);
								if (!ad1.matches(ad2))
								{
									return false;
								}
							}
						} finally
						{
							child.loadFromDataSet(child.wbCurrentRow);
						}
                    }
                    finally
                    {
            			getRidOfSession(sessObj);
                    }

                } else if (!result)
                {
                    break;
                }

            }
        }
        else if (tblClass.equals(CollectionObject.class)) //XXX Updates
        {
            result = true;
        }
        else if (tblClass.equals(Locality.class)) {
        	for (UploadTable child : specialChildren) {
        		if (child.getTblClass().equals(LocalityDetail.class)) {
                    Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
            		DataProviderSessionIFace matchSession = sessObj.getFirst();
                    try {
                        QueryIFace matchesQ = matchSession
                                .createQuery("from LocalityDetail where localityId = "
                                        + match.getId(), false);
                        List<?> matches = matchesQ.list();
                        try {
							child.loadFromDataSet(wbCurrentRow);
							LocalityDetail ld2 = (LocalityDetail) child.getCurrentRecord(0);
							if (ld2 == null && matches.size() == 0) {
								continue;
							}
							if (ld2 == null && matches.size() != 0) {
								return false;
							}
							if (ld2 != null && matches.size() == 0) {
								return false;
							}
							LocalityDetail ld1 = (LocalityDetail) matches.get(0);
							result = ld1.matches(ld2);
							if (!result) {
								return false;
							}
						} finally {
							child.loadFromDataSet(child.wbCurrentRow);
						}
                    } finally {
            			getRidOfSession(sessObj);
                    }
                }
                if (child.getTblClass().equals(GeoCoordDetail.class)) {
                    Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
            		DataProviderSessionIFace matchSession = sessObj.getFirst();
                    try {
                        QueryIFace matchesQ = matchSession
                                .createQuery("from GeoCoordDetail where localityId = "
                                        + match.getId(), false);
                        List<?> matches = matchesQ.list();
                        try {
							child.loadFromDataSet(wbCurrentRow);
							GeoCoordDetail ld2 = (GeoCoordDetail) child.getCurrentRecord(0);
							if (ld2 == null && matches.size() == 0) {
								continue;
							}
							if (ld2 == null && matches.size() != 0) {
								return false; }
							if (ld2 != null && matches.size() == 0)  {
								return false;
							}
							GeoCoordDetail ld1 = (GeoCoordDetail) matches
									.get(0);
							result = ld1.matches(ld2);
							if (!result) {
								return false;
							}
						} finally {
							child.loadFromDataSet(child.wbCurrentRow);
						}
                    } finally {
            			getRidOfSession(sessObj);
                    }
                }
            }
        } else if (tblClass.equals(ReferenceWork.class))
        {
            for (UploadTable child : specialChildren)
            {
                logDebug(child.getTable().getName());
                if (child.getTblClass().equals(Author.class))
                {
                    Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
            		DataProviderSessionIFace matchSession = sessObj.getFirst();
                    try
                    {
                        QueryIFace matchesQ = matchSession
                                .createQuery("from Author where referenceworkid = "
                                        + match.getId() + " order by orderNumber", false);
                        List<?> matches = matchesQ.list();
                        try
                        {
                        	child.loadFromDataSet(wbCurrentRow);
                        	int childCount = 0;
                        	for (int c = 0; c < child.getUploadFields().size(); c++)
                        	{
                        		if (child.getCurrentRecord(c) != null)
                        		{
                        			childCount++;
                        		}
                        	}
                        	if (matches.size() != childCount)
                        	{
                        		result = false;
                        	}
                        	else
                        	{
                        		for (int rec = 0; rec < matches.size(); rec++)
                        		{
                        			Author auth1 = (Author) matches.get(rec);
                        			Author auth2 = (Author) child.getCurrentRecord(rec);
                        			if (!auth1.getOrderNumber().equals(auth2.getOrderNumber()))
                        			{
                        				// maybe this doesn't really need to be checked?
                        				return false;
                        			}
                        			if (auth2.getAgent() == null || !auth1.getAgent().getId().equals(auth2.getAgent().getId()))
                        			{
                        				return false;
                        			}
                        			if (auth2.getRemarks() == null ^ auth1.getRemarks() == null)
                        			{
                        				return false;
                        			} else if (auth2.getRemarks() != null && !auth2.getRemarks().equals(auth1.getRemarks()))
                        			{
                        				return false;
                        			}
                        		}
                        	} 
                        } finally
                        {
                        		child.loadFromDataSet(child.wbCurrentRow);
                        }
                    }
                    finally
                    {
            			getRidOfSession(sessObj);
                    }
                } else if (!result)
                {
                    break;
                }

            }
        }
        else //if (!updateMatches) //XXX Updates!!!!
        // Oh no!!
        {
            log.error("Unable to check matching children for " + tblClass.getName());
            throw new UploaderException("Unable to check matching children for "
                    + tblClass.getName(), UploaderException.ABORT_IMPORT);
        }
        if (result)
        {
        	for (UploadTable ut : deletes)
        	{
        		ut.abortRow(ut.wbCurrentRow);
        	}
        }
        return result;
    }

    protected boolean needToMatchChildren() {
        return needToMatchChildren(false);
    }
    /**
     *
     * @return
     */
    protected boolean needToMatchChildren(boolean buildingUploader)
    {
        if (!buildingUploader && uploader.isUpdateUpload()) {
            return !matchUsingExportedRecord();
        }

        // temporary fix. Really should determine based on cascade rules and the fields in the
        // dataset.
        return (buildingUploader || !skipChildrenMatching.get()) &&
        	(tblClass.equals(CollectingEvent.class)
                || tblClass.equals(CollectingTrip.class)
        		|| tblClass.equals(Accession.class)
                || tblClass.equals(Agent.class)
        		|| tblClass.equals(CollectionObject.class) 
                || tblClass.equals(Locality.class)
                || tblClass.equals(ReferenceWork.class))
                ;
    }

    /**
     *
     * @param childClass
     * @return
     */
    protected boolean needToMatchChild(Class<?> childClass)
    {
        //XXX temporary fix. REALLY should determine based on cascade rules and the fields in the
        // dataset.
        //logDebug("need to add more child classes");
        if (tblClass.equals(Agent.class))
        {
        	return childClass.equals(Address.class);
        }
        if (tblClass.equals(Accession.class)) 
        { 
        	return childClass.equals(AccessionAgent.class)
                || childClass.equals(AccessionAuthorization.class); 
        }
        if (tblClass.equals(CollectingEvent.class)) 
        { 
        	return childClass.equals(Collector.class)
        		|| childClass.equals(CollectingEventAttribute.class)
                    || childClass.equals(CollectingEventAuthorization.class);
        }
        if (tblClass.equals(CollectingTrip.class))
        {
            return childClass.equals(CollectingTripAttribute.class)
                    || childClass.equals(CollectingTripAuthorization.class);
        }
        if (tblClass.equals(CollectionObject.class))
        { 
        	return childClass
                .equals(Determination.class)
                || childClass.equals(Preparation.class)
                || childClass.equals(CollectionObjectAttribute.class)
                || childClass.equals(CollectionObjectCitation.class)
                || childClass.equals(DNASequence.class)
                || childClass.equals(ConservDescription.class)
                || childClass.equals(OtherIdentifier.class)
                    || childClass.equals(CollectionObjectProperty.class);
        }
        if (tblClass.equals(Locality.class))
        {
        	return childClass.equals(GeoCoordDetail.class) || childClass.equals(LocalityDetail.class) || childClass.equals(LocalityCitation.class) || childClass.equals(LocalityNameAlias.class);
        }
        if (tblClass.equals(ReferenceWork.class))
        {
        	return childClass.equals(Author.class);
        }
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
                                    boolean ignoreNulls) {
        if (arg != null && (!(arg instanceof String) || StringUtils.isNotBlank((String )arg))) {
            critter.add(Restrictions.eq(propName, arg));
        } else if (!ignoreNulls || matchSetting.isMatchEmptyValues()) {
            critter.add(Restrictions.isNull(propName));
        }
        return getRestrictionArgText(arg, ignoreNulls);
    }

    /**
     *
     * @param arg
     * @param ignoreNulls
     * @return
     */
    protected String getRestrictionArgText(final Object arg, boolean ignoreNulls) {
        if (arg != null && (!(arg instanceof String) || StringUtils.isNotBlank((String )arg))) {
            if (arg instanceof DataModelObjBase) {
                String value = DataObjFieldFormatMgr.getInstance().format(arg, arg.getClass());
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
                return ((DataModelObjBase) arg).getId().toString();
            }
            return arg.toString();
        } else if (!ignoreNulls || matchSetting.isMatchEmptyValues()) {
            return getNullRestrictionText();
        } else {
            return "";
        }
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
    protected DataModelObjBase getParentRecord(final int recNum, UploadTable forChild) throws UploaderException
    {
        return getCurrentRecord(recNum);
    }
    
    protected Discipline getDiscipline() throws UploaderException
    {
    	if (discipline == null)
    	{
    		discipline = (Discipline )getClassObject(Discipline.class);
    	}
    	return discipline;
    }

    protected Division getDivision() throws UploaderException
    {
    	if (division == null)
    	{
    		division = (Division )getClassObject(Division.class);
    	}
    	return division;
    }
    
    protected DataModelObjBase getClassObject(Class<?> toGet)
			throws UploaderException
	{
        Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
		DataProviderSessionIFace session = sessObj.getFirst();
		try
		{
			DataModelObjBase temp = (DataModelObjBase) AppContextMgr
					.getInstance().getClassObject(toGet);
			temp = (DataModelObjBase) session.get(temp.getDataClass(), temp
					.getId());
			return temp;
		} catch (Exception ex)
		{
			throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
		} finally
		{
			getRidOfSession(sessObj);
		}
	}

    /**
     * @param criteria
     * @throws UploaderException
     * 
     * Adds extra criteria related to 'domain'
     */
    protected void addDomainCriteria(CriteriaIFace criteria) throws UploaderException
    {
        //XXX might be better to add getSpecialColumnCriteria() method to QueryAdjusterForDomain??
    	//but it would only be used here. 
    	//but this code will need to be checked whenever QueryAdjusterForDomain.getSpecialColumns is updated...
    	        
        /* CollectionMember and Discipline and Division conditions get added via relatedClassDefaults in getMatchCriteria().
    	if (CollectionMember.class.isAssignableFrom(tblClass))
        {
        	criteria.add(Restrictions.eq("collectionMemberId", getCollection().getId()));
        	return;
        }
        if (DisciplineMember.class.isAssignableFrom(tblClass))
        {
        	criteria.add(Restrictions.eq("discipline", getDiscipline()));
        	return;
        }
        if (Agent.class.isAssignableFrom(tblClass))
        {
        	//there is probably an nicer way to to do this
        	//criteria.addSubCriterion("division", Restrictions.eq("userGroupScopeId", getDivision().getUserGroupScopeId()));
        	return;
        }*/
    }
    
    /**
     *
     * @param unmatchableCols
     * @param seq
     * @return
     */
    protected boolean isMatchable(Set<Integer> unmatchableCols, int seq)
    {
    	for (UploadField fld : uploadFields.get(seq))
    	{
    		if (unmatchableCols.contains(new Integer(fld.getIndex())))
    		{
    			return false;
    		}
    	}
    	return true;
    }

    
//    protected boolean getUpdateMatchCriteria(CriteriaIFace critter,
//                                       final int recNum,
//                                       Vector<MatchRestriction> restrictedVals)
//    	throws UploaderException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
//    {
//    	//XXX Updates
//    	//totally hard-coded for CollectionObject - need to add RecordID field to workbench row.
//        for (UploadField uf : uploadFields.get(recNum))
//        {
//        	if (uf.getField().getFieldInfo().getName().equalsIgnoreCase("catalognumber"))
//        	{
//            	addRestriction(critter, deCapitalize("CatalogNumber"),getArgForSetter(uf)[0], true);
//            	return false;
//        	}
//        }
//        return false;
//    }
    
    
    /**
     * Relevant for update uploads only.
     * 
     * @return the (presumably one and only parent) that 'owns' this table and is matched by exported record id. 
     */
    protected ParentTableEntry getControllingIDMatchingParent() {
		for (List<ParentTableEntry> ptes : parentTables)  {
			for (ParentTableEntry pte : ptes)  {
				if (pte.getImportTable().specialChildren.contains(this)) {
					return pte; 
				}
			}
		}
		return null;
    }
   
    /**
     * Relevant for update uploads only.
     * 
     * @return the (presumably one and only parent) that 'owns' this table and is matched by exported record id. 
     */
    protected ParentTableEntry getControllingParent()
    {
		for (Vector<ParentTableEntry> ptes : parentTables)
		{
			for (ParentTableEntry pte : ptes)
			{
				if (pte.getImportTable().getSpecialChildren().indexOf(this) != -1)
				{
					return pte; 
				}
			}
		}
		return null;
    }

    /**
     * @param parents
     * @param critter
     * @param recNum
     * @return true if a match condition was added
     */
    protected boolean checkParentsForMatchCriteria(Vector<Vector<ParentTableEntry>> parents, CriteriaIFace critter, int recNum, UploadTable child) {
		boolean gotIt = false;
		for (Vector<ParentTableEntry> ptes : parents) {
			for (ParentTableEntry pte : ptes) {
				if (pte.getImportTable() == this || (child == null && pte.getImportTable().isMatchRecordId())) {
					if (child == null) {
						addRestriction(critter, pte.getPropertyName(), pte.getImportTable().getCurrentRecord(recNum), true);
					} else {
						//Can use pte.getPropertyName because the propNames happen to be equal for all currently applicable cases 
						addRestriction(critter, pte.getPropertyName(), getCurrentRecord(recNum), true);
					}
					gotIt = true;
					break;
				}
			}
			if (gotIt) {
				break;
			}
		}
    	return gotIt;	
    }
    /**
     *
     * @param recNum
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws UploaderException
     */
    protected Map<DBInfoBase, Object> getParentOverridesForExportedRecMatching(int recNum) throws InvocationTargetException,
            IllegalArgumentException, IllegalAccessException, UploaderException {
        HashMap<DBInfoBase, Object> result = new HashMap<>();
        if (parentTables != null && parentTables.size() > 0) {
            for (ParentTableEntry pt : parentTables.get(Math.min(parentTables.size() - 1, recNum))) {
                if (!updateMatches || uploader.getUploadedTablesForCurrentRow().indexOf(pt.getImportTable()) != -1) {
                    Object arg[] = new Object[1];
                    DataModelObjBase parentRec = pt.getImportTable().getParentRecord(recNum, this);
                    if (parentRec == null || parentRec.getId() == null) {
                        arg[0] = null;
                    } else {
                        arg[0] = parentRec;
                    }
                    if (valueChange(getExportedRecord(), pt.getGetter(), arg)) {
                        result.put(getRelationshipInfoForMatchingOverride(pt), arg[0]);
                    }
                }
            }
        }
        return result;
    }

    /**
     *
     * @param pt
     * @return relationship with same getter as pt, which is good enough for matching override
     */
    protected DBRelationshipInfo getRelationshipInfoForMatchingOverride(ParentTableEntry pt) {
        for (DBRelationshipInfo ri : getTable().getTableInfo().getRelationships()) {
            if (RecordMatchUtils.getFldGetter(ri, getTable().getTableInfo()).equals(pt.getGetter())) {
                return ri;
            }
        }
        return null;
    }

    /**
     *
     * @param matchSql
     * @param session
     * @return
     */
    protected List<?> queryForInts(final String matchSql, final DataProviderSessionIFace session) {
        QueryIFace q = session.createQuery(matchSql, true);
        return q.list();
    }

    protected Map<DBInfoBase, Object> getOverridesForExportedRecMatching(int recNum, final List<MatchRestriction> restrictedVals)
            throws UploaderException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Map<DBInfoBase, Object> overrides = new HashMap<>();
        for (UploadField uf : uploadFields.get(recNum)) {
            if (isFieldToMatchOn(uf)) {
                Object arg = getArgForSetter(uf)[0];
                overrides.put(uf.getField().getFieldInfo(), arg);
                restrictedVals.add(new MatchRestriction(uf.getWbFldName(), getRestrictionArgText(arg, false), uf.getIndex()));
            }
        }
        overrides.putAll(getParentOverridesForExportedRecMatching(recNum));
        overrides.putAll(getSpecialChildrenOverridesForExportedRecMatching(recNum, restrictedVals));
        return checkOverrides(overrides);
    }

    protected Map<DBInfoBase, Object> checkOverrides(Map<DBInfoBase, Object> overrides) {
        if (this.tblClass.equals(Locality.class)) {
            DBTableInfo locInfo = DBTableIdMgr.getInstance().getInfoByTableName("locality");
            DBFieldInfo lat1 = locInfo.getFieldByName("latitude1");
            DBFieldInfo lat2 = locInfo.getFieldByName("latitude2");
            DBFieldInfo lng1 = locInfo.getFieldByName("longitude1");
            DBFieldInfo lng2 = locInfo.getFieldByName("longitude2");
            if (overrides.containsKey(lat1) || overrides.containsKey(lat2)
                    || overrides.containsKey(lng1) || overrides.containsKey(lng2)) {
                WorkbenchRow wbRow = uploader.getWb().getRow(wbCurrentRow);
                GeoRefConverter geoRefConverter = new GeoRefConverter();
                LatLonConverter.FORMAT fmt;
                fmt = GeoRefConverter.getLeastCommonFmt(geoRefConverter.getLatLonFormat(StringUtils.stripToNull(wbRow.getLat1Text())),
                                                    geoRefConverter.getLatLonFormat(StringUtils.stripToNull(wbRow.getLong1Text())));
                String lat2Text = wbRow.getLat2Text();
                if (lat2Text != null && !"".equals(lat2Text)) {
                    LatLonConverter.FORMAT fmt2 = GeoRefConverter.getLeastCommonFmt(geoRefConverter.getLatLonFormat(StringUtils.stripToNull(wbRow.getLat2Text())),
                            geoRefConverter.getLatLonFormat(StringUtils.stripToNull(wbRow.getLong2Text())));
                    fmt = GeoRefConverter.getLeastCommonFmt(fmt, fmt2);

                }
                overrides.put(locInfo.getFieldByName("srcLatLongUnit"), fmt == null ? 3 : fmt.ordinal());
                DBFieldInfo llType = locInfo.getFieldByName("latLongType");
                if (!overrides.containsKey(llType)) {
                    boolean pnt1Changed = overrides.containsKey(lat1) || overrides.containsKey(lng1);
                    boolean pnt2Changed = overrides.containsKey(lat2) || overrides.containsKey(lng2);
                    if (pnt1Changed || pnt2Changed) {
                        boolean pnt1Cleared = pnt1Changed && overrides.get(lat1) == null;
                        boolean pnt2Cleared = pnt2Changed && overrides.get(lat2) == null;
                        Locality rec = (Locality) getExportedRecord();
                        if (rec != null) {
                            boolean origPoint2Present = rec.getLatitude2() != null;
                            if (pnt1Cleared) {
                                overrides.put(llType, null);
                            } else if (pnt2Cleared) {
                                overrides.put(llType, "Point");
                            } else if (pnt2Changed && !origPoint2Present) {
                                overrides.put(llType, "Line");  //hello: or "Rect"?
                            }
                        }
                    }
                }
            }
        }
        return overrides;
    }

    protected Map<DBInfoBase, Object> getSpecialChildrenOverridesForExportedRecMatching(int recNum, final List<MatchRestriction> restrictedVals)
            throws UploaderException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Map<DBInfoBase, Object>  overrides = new HashMap<>();
        for (UploadTable ut : specialChildren) {
            uploader.loadRow(ut, wbCurrentRow);
            overrides.putAll(ut.getOverridesForExportedRecMatching(recNum, restrictedVals));
        }
        return overrides;
    }

    protected Pair<Boolean, CriteriaIFace>
    getUpdateMatchCriteriaFromExportedRecord(final DataProviderSessionIFace session,
                                                                                    int recNum, final List<MatchRestriction> restrictedVals,
                                                                                    final HashMap<UploadTable, DataModelObjBase> overrideParentParams)
            throws UploaderException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        if (isOneToOneChild()) {
            return new Pair<>(false, null);
        }

        Map<DBInfoBase, Object> overrides = getOverridesForExportedRecMatching(recNum, restrictedVals);

        try {
            String matchSql = RecordMatchUtils.getMatchingSql(getExportedRecord(), overrides);
            List matches = matchSql != null ? queryForInts(matchSql, session) : new ArrayList();
            if (matches.size() == 0
                    || (matches.size() == 1 && matches.get(0).equals(exportedRecordId))) {
                //force (re)use of exported record
                return new Pair<>(false, null);
            } else {
                List previousCreates = new ArrayList<>();
                for (Object m : matches) {
                    if (uploadedRecs.getThird().contains(m)) {
                        previousCreates.add(m);
                    }
                }
                CriteriaIFace critter = session.createCriteria(this.tblClass);
                //use matches created by current upload w/o asking. hopefully there won't be more than 1
                critter.add(Restrictions.in("id", previousCreates.size() == 0 ? matches : previousCreates));
                return new Pair<>(false, critter);
            }
        } catch (Exception e) {
            throw new UploaderException(e, UploaderException.ABORT_IMPORT);
        }
    }


    /**
     *
     * @return
     */
    protected boolean matchUsingExportedRecord() {
        if (!updateMatches) {
            return false;
        } else {
            ParentTableEntry lordAndMaster = getControllingIDMatchingParent();
            boolean reUsingParent = lordAndMaster != null && lordAndMaster.getImportTable().reusingExportedRec;
            return matchUsingExportedRecord(reUsingParent);
        }
    }

    /**
     *
     * @param reUsingParent
     * @return
     */
    protected boolean matchUsingExportedRecord(boolean reUsingParent) {
        return !(matchRecordId || reUsingParent);
    }
    /**
     *
     * @param session
     * @param recNum
     * @param restrictedVals
     * @param overrideParentParams
     * @return
     * @throws UploaderException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
	protected Pair<Boolean, CriteriaIFace> getUpdateMatchCriteria(final DataProviderSessionIFace session,
					final int recNum, Vector<MatchRestriction> restrictedVals,
					HashMap<UploadTable, DataModelObjBase> overrideParentParams)
					throws UploaderException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
	    ParentTableEntry lordAndMaster = getControllingIDMatchingParent();
		boolean reUsingParent = lordAndMaster != null && lordAndMaster.getImportTable().reusingExportedRec;
        CriteriaIFace critter = session.createCriteria(tblClass);
		if (!matchUsingExportedRecord(reUsingParent)) {
			if (exportedRecordId != null || reUsingParent) {
				if (getTable().getTableInfo().getTableId() == uploader.getUpdateTableId() || matchRecordId) {
					addRestriction(critter, "id", exportedRecordId, true);
				} else if ((uploader.getUpdateTableId() == CollectionObject.getClassTableId() && getTable().getTableInfo().getTableId() == CollectingEvent.getClassTableId())
						|| getTable().getTableInfo().getTableId() == CollectionObjectAttribute.getClassTableId()
						|| getTable().getTableInfo().getTableId() == CollectingEventAttribute.getClassTableId()
                        || getTable().getTableInfo().getTableId() == CollectingTripAttribute.getClassTableId()
						|| getTable().getTableInfo().getTableId() == PreparationAttribute.getClassTableId()) {
					addRestriction(critter, "id", exportedRecordId, true);
				} else {
					//must be a child of a table for which matchRecordid is true.
					//Assuming that there is only parent table with matchRecordId true.
					if (lordAndMaster != null){
						//exportedOneToManyId should have been set from writeRowOrNot
						if (exportedOneToManyId != null){
							addRestriction(critter, "id", exportedOneToManyId, true);
						} else {
							return getInsertMatchCriteria(session, recNum, restrictedVals, overrideParentParams);
						}
					}
				}

			} else { //an insert?
				critter = null;
			}
			return new Pair<Boolean, CriteriaIFace>(false, critter);
		}
		if (exportedRecordId != null) {
            return getUpdateMatchCriteriaFromExportedRecord(session, recNum, restrictedVals, overrideParentParams);
        } else {
		    return getInsertMatchCriteria(session, recNum, restrictedVals, overrideParentParams);
        }
	}

	/**
	 * @param recNum
	 * 
	 * get and set the RecordID and IsDeleted status for the current row and recNum
	 * for a 'controlled' one-to-many table
	 */
	protected void updateExportedRecInfo(int recNum) {
		exportedOneToManyId = null;
		exportedOneToManyDelete = false;
		ParentTableEntry controllingParent = getControllingIDMatchingParent();	
		
		if (controllingParent == null) {
			return;
		}
		//if (matchRecordId && exportedRecordId != null && getControllingIDMatchingParent() != null)
		if ((matchRecordId && exportedRecordId != null) ||  controllingParent.getImportTable().reusingExportedRec) {
			Pair<Integer, Boolean> info = getExportedRecInfo(recNum);
			if (info != null) {
				exportedOneToManyId = info.getFirst();
				exportedOneToManyDelete = info.getSecond();
			}
		}
	}
	
	/**
	 * @param recNum
	 * @return the RecordID and IsDeleted status for the current row and recNum
	 * for a 'controlled' one-to-many table
	 */
	protected Pair<Integer, Boolean> getExportedRecInfo(int recNum)
	{
		String sql = "select wber.RecordID  from workbenchrow wbr inner join "
			+ "workbenchrowexportedrelationship wber on wber.workbenchrowid = wbr.workbenchrowID "
			+ " where wber.Sequence = " + recNum + " and "
			+ "wber.TableName = '" + tblClass.getSimpleName().toLowerCase() + "' and "
			+ "wbr.RowNumber = " + wbCurrentRow + " and "
			+ "wbr.WorkbenchID = " + uploader.getWbSS().getWorkbench().getId();
		Vector<Object[]> recInfo = BasicSQLUtils.query(sql);
		if (recInfo == null || recInfo.size() == 0)
		{
			return null;
		}
		if (recInfo.size() > 1)
		{
			log.warn("Multiple exported rec info records found. Table=" + tblClass.getSimpleName().toLowerCase() +
					" row=" + wbCurrentRow  + " seq=" + recNum);
		}
		return new Pair<Integer, Boolean>((Integer )recInfo.get(0)[0], false /*(Boolean )recInfo.get(0)[1]*/);
	}
	
	protected Pair<Boolean, CriteriaIFace> getMatchCriteria(final DataProviderSessionIFace session, final int recNum,
			Vector<MatchRestriction> restrictedVals, 
			HashMap<UploadTable, DataModelObjBase> overrideParentParams) throws UploaderException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {
		if (updateMatches) { // XXX Updates 
			return getUpdateMatchCriteria(session, recNum, restrictedVals, overrideParentParams);
		}
		return getInsertMatchCriteria(session, recNum, restrictedVals, overrideParentParams);
	}

	/**
	 * @param uf
	 * @return true if field needs to be used when creating match criteria
	 */
	protected boolean isFieldToMatchOn(final UploadField uf)
	{
		if (tblClass.equals(Collector.class) && uf.getField().getFieldInfo() != null && uf.getField().getFieldInfo().getColumn().equalsIgnoreCase("isprimary"))
		{
			return false;
		}
		return uf.getSetter() != null;
	}
	
	protected UploadTable getOneToOneParent() {
		UploadTable result = null;
		if (uploader != null) {
			result = uploader.getOneToOneParent(this);
		}
		return result;
	}
	/**
	 * @param pte
	 * @param recNum
	 * @param overrideParentParams
	 * @return
	 * @throws UploaderException
	 */
	protected DataModelObjBase getParentParam(final ParentTableEntry pte, final int recNum, HashMap<UploadTable, DataModelObjBase> overrideParentParams) throws UploaderException
	{
      	return overrideParentParams != null ? overrideParentParams.get(pte.getImportTable())
      			: pte.getImportTable().getParentRecord(recNum, this);
	}
    /**
     *
     * @param session
     * @param recNum
     * @param restrictedVals
     * @param overrideParentParams
     * @return true if blank cells were ignored when matching
     * @throws UploaderException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    protected Pair<Boolean, CriteriaIFace> getInsertMatchCriteria(DataProviderSessionIFace session,
                                       final int recNum,
                                       Vector<MatchRestriction> restrictedVals,
                                       HashMap<UploadTable, DataModelObjBase> overrideParentParams)
            throws UploaderException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
    	Boolean ignoringBlankCell = false;
        CriteriaIFace critter = session.createCriteria(tblClass);
        for (UploadField uf : uploadFields.get(recNum)) {
            if (isFieldToMatchOn(uf)) {
                String restriction = addRestriction(critter, deCapitalize(uf.getField().getName()),
                        getArgForSetter(uf)[0], true);
                ignoringBlankCell = ignoringBlankCell || restriction.equals("")
                        && !matchSetting.isMatchEmptyValues();
                String fldName = uf.getWbFldName();
                if (StringUtils.isBlank(fldName)) {
                    if (uf.getField() != null && uf.getField().getFieldInfo() != null) {
                        fldName = uf.getField().getFieldInfo().getTitle();
                    }
                    else if (uf.getField() != null) {
                        fldName = uf.getField().getName();
                    }
                    else {
                        fldName = "?";
                        log.error("unable to find field title or name for " + uf);
                    }
                }
                restrictedVals.add(new MatchRestriction(fldName, restriction, uf.getIndex()));
            }
        }
        if (isOneToOneChild() && !isZeroToOneMany()) {
        	UploadTable oneToOneParent = getOneToOneParent();
        	Method g = null;
        	if (oneToOneParent != null) {
        		for (List<ParentTableEntry> ptes : oneToOneParent.getParentTables()) {
        			for (ParentTableEntry pte : ptes) {
        				if (pte.getImportTable().equals(this)) {
        					g = pte.getGetter();
        					break;
        				}
        			}
        			if (g != null) break;
        		}
        		DataModelObjBase prec = overrideParentParams.get(oneToOneParent);
        		if (g != null && prec != null) {
        			DataModelObjBase obj = (DataModelObjBase)g.invoke(prec);
        			if (obj != null) {
        				addRestriction(critter, "id", obj.getId(), true);
        			}
        		}
        	}
        }
        for (Vector<ParentTableEntry> ptes : parentTables) {
            for (ParentTableEntry pte : ptes) {
              if (!needToMatchChildren() || !pte.getImportTable().isOneToOneChild()) {
              	  if (!pte.getImportTable().isOneToOneChild()) {
              		  DataModelObjBase parentParam = getParentParam(pte, recNum, overrideParentParams);//overrideParentParams != null ? overrideParentParams.get(pte.getImportTable())
              			//: pte.getImportTable().getParentRecord(
                        //          recNum, this);
              		  restrictedVals.add(new MatchRestriction(pte.getPropertyName(), addRestriction(
              				  critter, pte.getPropertyName(), parentParam, false), -1));
              	  }
              }
            }
        }
        for (RelatedClassSetter rce : relatedClassDefaults)
        {
            critter.add(Restrictions.eq(rce.getPropertyName(), rce.getDefaultObj(recNum)));
        }
        addMissingRequiredFieldsToMatchCriteria(critter, recNum);
        addDomainCriteria(critter);
        
        Collections.sort(restrictedVals);
        
        return new Pair<Boolean, CriteriaIFace>(ignoringBlankCell, critter);
    }
    
    /**
     * @param critter
     * @param recNum
     */
    protected void addMissingRequiredFieldsToMatchCriteria(CriteriaIFace critter, int recNum) {
    	for (DefaultFieldEntry dfe : missingRequiredFlds) {
    		if (shouldAddMissingReqFldToMatchCriteria(dfe)) {
    			if (dfe.isMultiValued()) {
    				critter.add(Restrictions.in(deCapitalize(dfe.getFldName()), dfe
    						.getDefaultValues(recNum)));
    			} else {
    				critter.add(Restrictions.eq(deCapitalize(dfe.getFldName()), dfe
    					.getDefaultValue(recNum)));
    			}
    		}
    	}
    }
    
    /**
     * @param dfe
     * @return
     */
    protected boolean shouldAddMissingReqFldToMatchCriteria(DefaultFieldEntry dfe) {
    	return !(tblClass.equals(ReferenceWork.class) || tblClass.equals(PrepType.class));
    }
//    /**
//     * 
//     * 
//     * @param critter
//     * @param recNum
//     * @param restrictedVals
//     * @param overrideParentParams
//     * @return true if blank cells were ignored when matching
//     * @throws UploaderException
//     * @throws IllegalAccessException
//     * @throws NoSuchMethodException
//     * @throws InvocationTargetException
//     */
//    protected boolean getMatchCriteria(CriteriaIFace critter,
//                                       final int recNum,
//                                       Vector<MatchRestriction> restrictedVals,
//                                       HashMap<UploadTable, DataModelObjBase> overrideParentParams)
//            throws UploaderException, IllegalAccessException, NoSuchMethodException,
//            InvocationTargetException
//    {
//        if (updateMatches) //XXX Updates
//        {
//        	return getUpdateMatchCriteria(critter, recNum, restrictedVals);
//        }
//        
//    	boolean ignoringBlankCell = false;
//        for (UploadField uf : uploadFields.get(recNum))
//        {
//            if (uf.getSetter() != null)
//            {
//                String restriction = addRestriction(critter, deCapitalize(uf.getField().getName()),
//                        getArgForSetter(uf)[0], true);
//                ignoringBlankCell = ignoringBlankCell || restriction.equals("")
//                        && !matchSetting.isMatchEmptyValues();
//                //restrictedVals.add(new Pair<String, String>(uf.getWbFldName(), restriction));
//                String fldName = uf.getWbFldName();
//                if (StringUtils.isBlank(fldName))
//                {
//                    if (uf.getField() != null && uf.getField().getFieldInfo() != null)
//                    {
//                        fldName = uf.getField().getFieldInfo().getTitle();
//                    }
//                    else if (uf.getField() != null)
//                    {
//                        fldName = uf.getField().getName();
//                    }
//                    else
//                    {
//                        fldName = "?";
//                        log.error("unable to find field title or name for " + uf);
//                    }
//                }
//                restrictedVals.add(new MatchRestriction(fldName, restriction, uf.getIndex()));
//            }
//        }
//        for (Vector<ParentTableEntry> ptes : parentTables)
//        {
//            for (ParentTableEntry pte : ptes)
//            {
//                if (!needToMatchChildren() || !pte.getImportTable().isOneToOneChild())
//                {
//                	DataModelObjBase parentParam = overrideParentParams != null ? overrideParentParams.get(pte.getImportTable())
//                			: pte.getImportTable().getParentRecord(
//                                    recNum, this);
//                	restrictedVals.add(new MatchRestriction(pte.getPropertyName(), addRestriction(
//                        critter, pte.getPropertyName(), parentParam, false), -1));
//                }
//            }
//        }
//        for (RelatedClassSetter rce : relatedClassDefaults)
//        {
//            critter.add(Restrictions.eq(rce.getPropertyName(), rce.getDefaultObj(recNum)));
//        }
//        if (!tblClass.equals(ReferenceWork.class))
//        {
//        	for (DefaultFieldEntry dfe : missingRequiredFlds)
//        	{
//        		if (dfe.isMultiValued())
//        		{
//        			critter.add(Restrictions.in(deCapitalize(dfe.getFldName()), dfe
//						.getDefaultValues(recNum)));
//        		}
//        		else
//        		{
//        			critter.add(Restrictions.eq(deCapitalize(dfe.getFldName()), dfe
//        					.getDefaultValue(recNum)));
//        		}
//        	}
//        }
//        addDomainCriteria(critter);
//        
//        Collections.sort(restrictedVals);
//        
//        return ignoringBlankCell;
//    }
    
    /**
     * @author timo
     *
     */
    private class ParentMatchInfo 
    {
    	protected final List<DataModelObjBase> matches;
    	protected final UploadTable table;
    	protected final boolean isBlank;
    	protected boolean isSkipped; //true if matching was not attempted because of un-matched parent
    	protected final int recNum;
    	protected final boolean stoppedAlready;
        /**
         *
         * @param matches
         * @param table
         * @param isBlank
         * @param isSkipped
         * @param recNum
         * @param stoppedAlready
         */
		public ParentMatchInfo(List<DataModelObjBase> matches,
				UploadTable table, boolean isBlank, boolean isSkipped, int recNum, boolean stoppedAlready)
		{
			super();
			this.matches = matches;
			this.table = table;
			this.isBlank = isBlank;
			this.isSkipped = isSkipped;
			this.recNum = recNum;
			this.stoppedAlready = stoppedAlready;
		}
		
		/**
		 * @return stoppedAlready
		 */
		public boolean getStoppedAlready()
		{
			return stoppedAlready;
		}
		
		/**
		 * @return the matches
		 */
		public List<DataModelObjBase> getMatches() 
		{
			return matches;
		}
		/**
		 * @return the parent
		 */
		public UploadTable getTable() 
		{
			return table;
		}
		/**
		 * @return the isBlank
		 */
		public boolean isBlank() 
		{
			return isBlank;
		}
		/**
		 * @return the isSkipped
		 */
		public boolean isSkipped() 
		{
			return isSkipped;
		}
		
		/**
		 * @param isSkipped
		 */
		public void setIsSkipped(boolean isSkipped)
		{
			this.isSkipped = isSkipped;
		}
		
		/**
		 * @return the recNum
		 */
		public int getRecNum() 
		{
			return recNum;
		}
		
    }
    
    /**
     * @param recNum
     * @param invalidColNums - indexes of cols that contain invalid values.
     * @return
     */
    protected boolean containsInvalidCol(int recNum, Set<Integer> invalidColNums)
    {
    	int adjustedRecNum = uploadFields.size() == 1 ? 0 : recNum; //I guess
    	for (UploadField fld : uploadFields.get(adjustedRecNum))
    	{
    		if (invalidColNums.contains(fld.getIndex()))
    		{
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * @param row
     * @param recNum
     * @return
     * @throws UploaderException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ParseException
     * @throws NoSuchMethodException
     */
    protected List<ParentMatchInfo> getMatchInfoInternal(int row, int recNum, 
    		Set<Integer> invalidColNums, List<Pair<UploadTable, List<DataModelObjBase>>> matchChildrenParents) throws UploaderException,
    		InvocationTargetException, IllegalAccessException, ParseException,
    		NoSuchMethodException, InstantiationException, SQLException
    {
    	//XXX still need to follow matchChildren links
    	//XXX when doing children need to do all recnums - collector1 2 3 ...
    	//XXX need to skip tables with invalid columns
    	//XXX what about ...Attribute tables? Do they need special treatment?
    	//XXX if CE is embedded? 
    	
    	int adjustedRecNum = uploadFields.size() == 1 ? 0 : recNum;
    	//XXX assuming that an upload is NOT in progress!!
    	wbCurrentRow = row;

    	List<ParentMatchInfo> result = new Vector<ParentMatchInfo>();
    	Vector<List<ParentMatchInfo>> parentMatches = new Vector<List<ParentMatchInfo>>();
    	Vector<List<ParentMatchInfo>> childMatches = new Vector<List<ParentMatchInfo>>();
    	List<UploadTable> childTables = new ArrayList<UploadTable>(specialChildren);


		if (this instanceof UploadTableTree && parentTables.size() == 0) {
			DataModelObjBase p = getParentRecord(recNum, this /*only the type of the 2nd param is relevant*/);
			List<DataModelObjBase> m = new ArrayList<DataModelObjBase>();
			m.add(p);
			List<ParentMatchInfo> pm = new ArrayList<ParentMatchInfo>();
			pm.add(new ParentMatchInfo(m, this, isBlankRow(row, uploader.getUploadData(), adjustedRecNum), 
					false, recNum, false));
			parentMatches.add(pm);
		} else {
			for (Vector<ParentTableEntry> ptes : parentTables) {
				for (ParentTableEntry pte : ptes) {
					if (!pte.getImportTable().specialChildren.contains(this)
							|| !pte.getImportTable().needToMatchChild(tblClass)) {
						if (pte.getImportTable().isOneToOneChild()) {
							if (childTables.indexOf(pte.getImportTable()) == -1) {
								childTables.add(pte.getImportTable());
							}
						} else {
							parentMatches.add(pte.getImportTable()
									.getMatchInfoInternal(row, adjustedRecNum,
											invalidColNums,
											null));
						}
					}
				}
			}
		}
    	HashMap<UploadTable, DataModelObjBase> parentParams = new HashMap<UploadTable, DataModelObjBase>();
    	boolean doMatch = true; 
    	boolean matched = false;
    	boolean blankParentage = true;
    	boolean blank = isBlankRow(row, uploader.getUploadData(), adjustedRecNum);
    	boolean stopAlready = false;
		ArrayList<DataModelObjBase> matches = new ArrayList<DataModelObjBase>();
		ArrayList<DataModelObjBase> myMatches = new ArrayList<DataModelObjBase>();
		//XXX need to include matchChildrenParents in parentParams 
		ParentMatchInfo nearest  = null;
		for (List<ParentMatchInfo> pm : parentMatches) {
			if (doMatch && pm.size() > 0) {
				nearest = pm.get(pm.size() - 1);
				int b = 2;
				while (pm.size() - b >= 0 && nearest.isSkipped() && !nearest.getStoppedAlready()) {
					nearest = pm.get(pm.size() - b++);
				}
				blankParentage &= nearest != null & nearest.isBlank();
				if (nearest.getMatches().size() == 1
						|| (!nearest.getStoppedAlready() && nearest.getMatches().size() == 0 && (nearest.isBlank() || nearest.isSkipped()))) {
					DataModelObjBase match = nearest.getMatches().size() == 1 ? nearest
							.getMatches().get(0)
							: null;
							stopAlready = this instanceof UploadTableTree && nearest.getMatches().size() == 0 && (nearest.isBlank() || nearest.isSkipped());
					parentParams.put(nearest.getTable(), match);
				} else {
					doMatch = false;
					stopAlready = nearest.getStoppedAlready();
				}
			}
			result.addAll(pm);
		}
		if (doMatch && (!blank || !blankParentage)) {
			for (Vector<UploadField> ufs : uploadFields) {
				for (UploadField uf : ufs) {
	                if (uf.getIndex() != -1) {
	                	uf.setValue(uploader.getUploadData().get(row, uf.getIndex()));
	                }
				}
			}
			//if (!this.getTblClass().equals(CollectingEvent.class)) {
				skipChildrenMatching.set(true);
			//}
			try {
				if (matchChildrenParents == null || matchChildrenParents.size() == 0) {
					if (!blank || !Treeable.class.isAssignableFrom(tblClass)) {
						findMatch(adjustedRecNum, false, myMatches, parentParams);
					} else if (nearest != null){
						myMatches.add(parentParams.get(nearest.getTable()));
					}
				} else {
					//XXX assuming matchChildrenParents is only being used for ...Attribute tables and will only have one item
					Pair<UploadTable, List<DataModelObjBase>> mp = matchChildrenParents.get(0);
					for (DataModelObjBase p : mp.getSecond()) {
						parentParams.put(mp.getFirst(), p);
						findMatch(adjustedRecNum, false, myMatches, parentParams);
						parentParams.remove(mp.getFirst());
					}
				}
				matched = true;
			} finally
			{
				//if (!this.getTblClass().equals(CollectingEvent.class)) {
					skipChildrenMatching.set(false);
				//}
			}
	    	
	    	
	    	
		}
		
		//XXX add this table to matchChildrenParents
		//taking advantage of this mysterious structure to deal with ...Attribute tbls.
		if (matchChildrenParents == null) {
			matchChildrenParents = new ArrayList<Pair<UploadTable, List<DataModelObjBase>>>();
		}
    	for (UploadTable ut : childTables) {
    		for (int rc = 0; rc < ut.getUploadFields().size(); rc++) {
    			if (!this.tblClass.equals(CollectionObject.class)) {
    				matchChildrenParents.add(new Pair<UploadTable, List<DataModelObjBase>>(this, myMatches));
    			}
    			childMatches.add(ut.getMatchInfoInternal(row, rc, invalidColNums, matchChildrenParents));
    			if (!this.tblClass.equals(CollectionObject.class)) {
    				matchChildrenParents.remove(matchChildrenParents.size()-1);
    			}
    		}
    	}
    	//XXX what the hell to do with childMatches??? Need to add them to result to get Agent, taxon matches, but how to 
    	//Use them in findMatch?? Does findMatch need to be done first?? WTF?
		// XXX what the hell happens for matchchildren in findMatch??
		//OK. Current plan is to match children in a way similar to the above - create a parentParam for this object and pass
		//it to findMatch for the children...
		// XXX this is not the final word on matchchildren matches
    	
    	
    	//XXX the new way of handling child matches...
    	if (!this.tblClass.equals(CollectionObject.class) && childMatches.size() > 0) {
    		List<DataModelObjBase> keeperMatches = new ArrayList<DataModelObjBase>(myMatches);
    		for (DataModelObjBase mine : myMatches) {
    			boolean go = true;
    			for (List<ParentMatchInfo> cm : childMatches) {
    				for (ParentMatchInfo pmi : cm) {
    					try {
    						ParentTableEntry pte = findParentTableEntry(pmi.getTable());
    						if (pte != null) {
    							//System.out.println(pte.getImportTable());
    							Method getter = pte.getGetter();
    							boolean invokeMe = getter.getDeclaringClass().equals(this.getTblClass());
    							if (pmi.getMatches().size() == 0) {
    								if (invokeMe) {
    									go = pmi.isBlank() ? getter.invoke(mine) == null : false;
    								} else {
    									//System.out.println("one-to-many count check");
    									String sql = "select count(*) from " + pmi.getTable().getTable().getTableInfo().getName() + " where " + this.getTable().getTableInfo().getPrimaryKeyName() + " = " + mine.getId();
    									go = pmi.isBlank() ? BasicSQLUtils.getCount(sql).equals(0) : false;
    								}
    							} else {
    								for (DataModelObjBase theirs : pmi.getMatches()) {
    									DataModelObjBase objA = (DataModelObjBase)(invokeMe ? getter.invoke(mine) : getter.invoke(theirs));
    									DataModelObjBase objB = invokeMe ? theirs : mine;
    									if (objA != null && objB != null && objA.getId().equals(objB.getId())) {
    										//System.out.println("  match (" + invokeMe +")");
    										//all is well
    									} else {
    										//System.out.println("  no match (" + invokeMe + ")");
    										go = false;
    										break;
    									}
    								}
    							}
    						} else {
    							//System.out.println("no pte; wtf");
    							//whatever
    						}
    						if (!go) {
    							//childMatchesForLosers.add(cm);
    							break;
    						}
    					} catch (Exception e) {
    						e.printStackTrace();
    					}
    				}
    				if (!go) {
    					keeperMatches.remove(mine);
    					break;
    				}
    			}
    		}
    	
    		//remove incomplete matches
    		for (int m = myMatches.size() - 1; m >= 0; m--) {
    			if (keeperMatches.indexOf(myMatches.get(m)) == -1) {
    				myMatches.remove(m);
    			}
    		}
		
    		//if no matches, we know all children are also unmatched
    		//technically should only clear/remove matches stemming from matches removed above, but... too much work for too little benefit
    		if (myMatches.size() == 0) {
    			for (List<ParentMatchInfo> cmis : childMatches) {
    				for (ParentMatchInfo cmi : cmis) {
    					UploadTable cmit = cmi.getTable();
    					if (cmit.isMatchChild() || cmit.isOneToOneChild() || cmit.isZeroToOneMany() || specialChildren.indexOf(cmit) >= 0) {
    						cmi.getMatches().clear();
    					}
    				}
    			}
    		}
    		
    		
    	}
    	
		matches.addAll(myMatches);

    	//I think this loop eliminates chains of no-match indications??
    	boolean invalid = containsInvalidCol(adjustedRecNum, invalidColNums);
    	for (List<ParentMatchInfo> cm : childMatches) {
			if (!invalid && matched) {
				result.addAll(cm);
			} else {
				for (int i = cm.size()-1; i > -1; i--) {
					ParentMatchInfo mi = cm.get(i);
					if (mi.getTable().isOneToOneChild()) {
						if (invalid) {
							cm.remove(i);
						} else if (!matched) {
							mi.setIsSkipped(true);
						}
					}
				}
				result.addAll(cm);
			}
		}
     	
    	if (!containsInvalidCol(adjustedRecNum, invalidColNums))
    	{
//    		if (!matched)
//    		{
//    			for (List<ParentMatchInfo> cm : childMatches)
//    			{
//    				for (ParentMatchInfo mi : cm)
//    				{
//    					if (mi.getTable().isOneToOneChild())
//    					{
//    						mi.setIsSkipped(true);
//    					}
//    				}
//    			}
//    		}
    		result.add(new ParentMatchInfo(matches, this, blank && blankParentage, !matched, recNum, stopAlready));
    	}
    	
    	return result;
    }
    
    /**
     * @param ut
     * @return
     */
    protected ParentTableEntry findParentTableEntry(UploadTable ut) {
    	Vector<Vector<ParentTableEntry>> ptes = ut.isOneToOneChild() && !ut.isZeroToOneMany() ? this.parentTables : ut.parentTables;
    	UploadTable matchUt = ut.isOneToOneChild() && !ut.isZeroToOneMany() ? ut : this;
    	for (Vector<ParentTableEntry> pte : ptes) {
    		for (ParentTableEntry p : pte) {
    			if (p.getImportTable() == matchUt) {
    				return p;
    			}
    		}
    	}
    	return null;
    }
    /**
     * @param row
     * @param recNum
     * @return the number matches for the current contents of this tables columns for row and recNum
     * 
     * @throws UploaderException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ParseException
     * @throws NoSuchMethodException
     */
    public List<UploadTableMatchInfo> getMatchInfo(int row, int recNum, Set<Integer> invalidColNums) throws UploaderException,
    	InvocationTargetException, IllegalAccessException, ParseException,
    	NoSuchMethodException, InstantiationException, SQLException
    {
    	//XXX assuming that an upload is NOT in progress!!
    	wbCurrentRow = row;
    	//XXX assuming this public method is called by Uploader for its Root table.
    	List<ParentMatchInfo> internalResult = getMatchInfoInternal(row, recNum, invalidColNums, null);
    	List<UploadTableMatchInfo> result = new Vector<UploadTableMatchInfo>();
    	for (ParentMatchInfo pmi : internalResult)
    	{
    		//System.out.println(pmi.getTable() + " " + pmi.isBlank() + " " + pmi.getMatches());
        	if (pmi.getTable().checkMatchInfo)
        	{
        		Vector<Integer> colIdxs = new Vector<Integer>();
        		int adjustedRecNum = pmi.getTable().getUploadFields().size() == 1 ? 0 : 
        			(pmi.getRecNum() == -1 ? 0 : pmi.getRecNum());
        		for (UploadField uf : pmi.getTable().getUploadFields().get(adjustedRecNum))
        		{
        			if (uf.getIndex() != -1)
        			{
        				colIdxs.add(uf.getIndex());
        			}
        		}
        		result.add(new UploadTableMatchInfo(pmi.getTable().getTblTitle(), pmi.matches.size(), colIdxs, pmi.isBlank(), pmi.isSkipped()));
        	}
    	}
    	return result;
    	
    }
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// json stuff for sp7 uploader experimentation...

    protected List<java.lang.reflect.Field> getFldsForJSON() {
    	java.lang.reflect.Field[] flds = UploadTable.class.getDeclaredFields();
//    	Arrays.sort(flds, new Comparator<java.lang.reflect.Field>(){
//    		public int compare(java.lang.reflect.Field f1, java.lang.reflect.Field f2) {
//    			return f1.getName().compareTo(f2.getName());
//    		}
//    	});
    	String[] skippers = {"autoAssignedVal", "blankSeqs", "collection", "currentRecords", "dateConverter", "debugging", "deleteUnusedRecs", "deletedRecs", "disUsedRecs", "discipline", "division", "doRawDeletes", 
    			"exportedOneToManyDelete", "exportedOneToManyId", "exportedRecordId", "geoRefConverter", "isSecurityOn", "log", "matchCountForCurrentRow", "prevAutoAssignedVal", "restrictedValsForAddNewMatch",
    			"reusingExportedRec","skipRow", "tblSession", "updateMatches", "uploadedRecs", "uploader", "validatingValues", "wbCurrentRow"};
    	List<java.lang.reflect.Field> result = new ArrayList<java.lang.reflect.Field>();
    	for (java.lang.reflect.Field fld : flds) {
    		if (0 > Arrays.binarySearch(skippers, fld.getName())) {
    			result.add(fld);
    		}
    	}
    	return result;
    }
    
    protected Object getValForJSON(java.lang.reflect.Field fld) throws IllegalAccessException {
    	Object val = fld.get(this);
    	return val != null ? val.toString() : val;
    }
    
    protected Map<String, Object> getJSONMap() throws IllegalAccessException {
    	Map<String,Object> jj = new HashMap<String,Object>();
    	List<java.lang.reflect.Field> flds = getFldsForJSON();
    	for (java.lang.reflect.Field fld : flds) {
    		jj.put(fld.getName(), getValForJSON(fld));
    	}
    	return jj;
    }
    
    public JSON toJSON() throws IllegalAccessException {
    	return JSONSerializer.toJSON(getJSONMap());
    }
    
//...json stuff for sp7 uploader experimentation    
//////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Searches the database for matches to values in current row of uploading dataset. If one match
     * is found it is set to the current value for this table. If no matches are created a new
     * record is created and saved for the current value. If more than one match is found then
     * action depends on props of matchSetting member.
     *
     * @param recNum
     * @param forceMatch
     * @param returnMatches
     * @param overrideParentParams
     * @return
     * @throws UploaderException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ParseException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    protected boolean findMatch(int recNum, boolean forceMatch, List<DataModelObjBase> returnMatches, 
    		HashMap<UploadTable, DataModelObjBase> overrideParentParams) throws UploaderException,
            InvocationTargetException, IllegalAccessException, ParseException,
            NoSuchMethodException, InstantiationException, SQLException {
        if (skipMatching && !matchRecordId && (overrideParentParams == null || overrideParentParams.size() == 0)) {
            if (updateMatches) {
                setCurrentRecordFromExportedRecord(recNum);
                return true;
            }
            return false;
        }

        Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
		DataProviderSessionIFace session = sessObj.getFirst();
        DataModelObjBase match = null;
        Vector<MatchRestriction> restrictedVals = new Vector<MatchRestriction>();
        Boolean ignoringBlankCell = false;
        try {
            Pair<Boolean, CriteriaIFace> critterObj = getMatchCriteria(session, recNum, restrictedVals, overrideParentParams);
            CriteriaIFace critter = critterObj.getSecond();
            ignoringBlankCell = critterObj.getFirst();
            List<DataModelObjBase> matches;
            List<DataModelObjBase> matchList;
            try {
                matchList = critter == null ? new ArrayList<DataModelObjBase>() : (List<DataModelObjBase>) critter.list();
            } catch (Exception x) {
                matchList = new ArrayList<>();
                log.error(x);
            }
            if (matchList.size() > 1) {
                // filter out duplicates. This seems very weird, but docs i found say it is normal
                // for
                // list() to return duplicate objects, and they did not mention any sort of 'select
                // distinct' property.
                Set<DataModelObjBase> matchSet = new TreeSet<DataModelObjBase>(new Comparator<DataModelObjBase>() {

					/* (non-Javadoc)
					 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
					 */
					@Override
					public int compare(DataModelObjBase arg0,
							DataModelObjBase arg1)
					{
						// TODO Auto-generated method stub
						return arg0.getId().compareTo(arg1.getId());
					}
                	
                });
                matchSet.addAll(matchList);
                matches = new ArrayList<DataModelObjBase>(matchSet);
            } else {
                matches = matchList;
            }
            if (!matchRecordId && needToMatchChildren()) {
                matches = matchChildren(matches, recNum);
            }
            if (returnMatches != null) {
            	returnMatches.addAll(matches);
            	return true;
            }
            matchCountForCurrentRow[recNum] = matches.size();
            if (matches.size() == 1) {
                match = matches.get(0);
                if (ignoringBlankCell) {
                    uploader.addMsg(new PartialMatchMsg(restrictedVals, match
                            .toString(), uploader.getRow() + 1, this));
                }
            } else if (matches.size() > 1) {
                match = dealWithMultipleMatches(matches, restrictedVals, recNum);
                if (match != null) {
                    matchSetting.addSelection(matchSetting.new MatchSelection(restrictedVals,
                                uploader.getRow(), match.getId(), matchSetting
                                        .getMode()));
                }
            }
        	setCurrentRecordFromMatch(match, recNum);
            if (match != null) {
                if (updateMatches && ((matchRecordId && !reusingExportedRec) || specialChildren.size() > 0)) {
                	match.forceLoad();
                }
                //XXX Updates
                // if a match was found matchChildren don't need to do anything. (assuming
                // !updateMatches!!!)
                if (!updateMatches) {
                	for (UploadTable child : specialChildren) {
                		if (needToMatchChild(child.tblClass) && (!child.isOneToOneChild() || child.isZeroToOneMany())) {
                			child.skipRow = true;
                		}
                	}
                }
                return true;
            }
            return false;
        } finally {
        	getRidOfSession(sessObj);
        }
    }


    /**
     *
     * @param rec
     * @return
     * @throws SQLException
     */
    protected boolean isThisRecordShared(final DataModelObjBase rec) throws SQLException {
        boolean recIsShared = false;
        if (rec != null) {
            if (tblClass.equals(Agent.class) || Treeable.class.isAssignableFrom(tblClass)) {
                //System.out.println("Assuming shared for class: " + tblClass);
                recIsShared = true;
            } else {
                SpecifyDeleteHelper delhel = uploader.getDeleteHelper();
                recIsShared = rec == null ? false : delhel.isRecordShared(tblClass, rec.getId(), !CollectingEvent.class.equals(tblClass), tblSession);
                delhel.done(false);
            }
        }
        return recIsShared;
    }


    /**
     *
     * @param recNum
     * @throws SQLException
     * @throws UploaderException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    protected void setCurrentRecordFromExportedRecord(int recNum) throws SQLException, UploaderException, InstantiationException,
        IllegalAccessException {
        DataModelObjBase expRec = getExportedRecord(); //Assumes updateExportedRecInfo has been called for recNum
        if (!isUploadRoot) {
            DataModelObjBase newRec = null;
            if (isOneToOneChild()) {
                ParentTableEntry pte = getControllingIDMatchingParent();
                if (pte != null) {
                    UploadTable p = pte.importTable;
                    if (this.tblClass.equals(LocalityDetail.class)) {
                        if (p.getCurrentRecord(0) != null) {
                            Set<LocalityDetail> ds = ((Locality)p.getCurrentRecord(0)).getLocalityDetails();
                            if (ds.size() > 0) {
                                newRec = ds.iterator().next();
                            }
                        }
                    } else if (this.tblClass.equals(GeoCoordDetail.class)) {
                        if (p.getCurrentRecord(0) != null) {
                            Set<GeoCoordDetail> ds = ((Locality)p.getCurrentRecord(0)).getGeoCoordDetails();
                            if (ds.size() > 0) {
                                newRec = ds.iterator().next();
                            }
                        }
                    }
                    setCurrentRecord(newRec, 0);
                    return;
                }
            }
            if (expRec == null) {
                setCurrentRecord(null, recNum);
                return;
            }
            boolean recIsShared = isThisRecordShared(expRec);
            if (!recIsShared)  {
                setCurrentRecord(expRec, recNum);
                reusingExportedRec = true;
            } else {
                if (expRec == null) {
                    newRec = createRecord();
                } else {
                    try {
                        newRec = (DataModelObjBase) expRec.clone();
                        if (!newRec.initializeClone(expRec, false, getSession().getFirst())) {
                            throw new UploaderException("Failed to initialize " + this.tblClass.getSimpleName() + " clone", UploaderException.ABORT_IMPORT);
                        }
                    } catch (Exception e) {
                        throw new UploaderException(e, UploaderException.ABORT_IMPORT);
                    }
                    addDisusedRec(exportedRecord);
                    currentRecSetFromExportedRec = true;
                }
                setCurrentRecord(newRec, recNum);
            }
        } else {
            setCurrentRecord(expRec, recNum);
        }
    }

    /**
     * @param match
     * @param recNum
     */
    protected void setCurrentRecordFromMatch(final DataModelObjBase match, int recNum)
    	throws IllegalAccessException, InstantiationException, SQLException, UploaderException {
        if (match == null && updateMatches && !isUploadRoot) {
            if (exportedRecordId != null) {
                setCurrentRecordFromExportedRecord(recNum);
            } else {
                updateAddingNewRecord = true;
                setCurrentRecord(match, recNum);
            }
        } else {
        	setCurrentRecord(match, recNum);
        }

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
     * returns true if finalization changed any values
     */
    protected boolean finalizeWrite(DataModelObjBase rec, int recNum) throws UploaderException {
        finalizeDatePrecisionFields(rec);
        if (finalizer != null) {
            return finalizer.finalizeForWrite(rec, recNum, uploader);
        } else {
            return false;
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
    protected boolean invalidNull(final UploadField fld,
                                  final UploadData uploadData,
                                  int row,
                                  int seq) throws UploaderException
    {
        if ("latlongtype".equalsIgnoreCase(fld.getField().getName())) return false;

//     	boolean isRequired = fld.isRequired();
//     	//Special case for PrepType
//     	DBFieldInfo fldInfo = fld.getField().getFieldInfo();
//     	if (fldInfo != null && fldInfo.getTableInfo().getTableId() == PrepType.getClassTableId() && fldInfo.getName().equals("name"))
//     	{
//     		isRequired = true;
//     	}
        boolean blankButRequired = fld.isRequired() && (fld.getValue() == null || fld.getValue().trim().equals(""));
        if (blankButRequired && tblClass.equals(Locality.class) && fld.getField().getName().equalsIgnoreCase("LatLongType"))
        {
        	boolean geoDataPresent = false;
        	for (UploadField f : getLatLongFlds())
        	{
        		if (StringUtils.isNotBlank(f.getValue()))
        		{
        			geoDataPresent = true;
        			break;
        		}
        	}
        	blankButRequired = geoDataPresent;
        }
//        boolean isAutoAssignable = fld.getField().getFieldInfo() != null && fld.getField().getFieldInfo().getFormatter() != null
//            && fld.getField().getFieldInfo().getFormatter().isIncrementer(); 
//            //&& fld.getField().getFieldInfo().getFormatter().isNumeric();
        boolean isAutoAssignable = fld.isAutoAssignForUpload() 
        		//|| (autoAssignedField == null && fld.isAutoAssignable())
        		|| fld.isAutoAssignable();
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
        
        if (!fld.isPicklistWarn() && !fld.isReadOnlyValidValues())
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
    	return getInvalidPicklistValErrMsg(fld, fld.getValidValues().keySet());
    }

    /**
     * @param fld
     * @param vals
     * @return a (possibly really really long) message listing the valid values for the fld.
     */
    protected String getInvalidPicklistValErrMsg(final UploadField fld, final Set<String> vals)
    {
        String valList = "";
        //Map<String, PickListItemIFace> vals = fld.getValidValues();
        if (vals != null) {
            int valCount = 0;
        	for (String val : vals) {
                if (!StringUtils.isEmpty(valList)) {
                    valList += ", ";
                }
                valList += "'" + val + "'";
                if (++valCount == 13) {
                	valList += " ...";
                	break;
                }
            }
            if (valCount > 0) {
                if (fld.isReadOnlyValidValues()) {
                    return String.format(UIRegistry.getResourceString("WB_UPLOAD_VALID_VALS"), valList);
                }
                return String.format(UIRegistry.getResourceString("WB_UPLOAD_VALID_VALS_WARN"), valList);
            } else {
        	    return UIRegistry.getResourceString("WB_UPLOAD_NO_VALID_VALS");
            }
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
    protected void addInvalidValueMsgForOneToManySkip(List<UploadTableInvalidValue> msgs, UploadField fld, String name, int row, int seq)
    {
        msgs.add(new UploadTableInvalidValue(null, this, fld, row, 
                new Exception(String.format(UIRegistry.getResourceString("WB_UPLOAD_ONE_TO_MANY_SKIP"),
                        name, seq+1))));
    }
    
    /**
     * @param seq
     * @return adjusted seq.
     */
    protected int getAdjustedSeqForBlankRowCheck(int seq)
    {
    	return seq;
    }
    /**
     * @param row
     * @param uploadData
     * @param seq
     * @return true if all the fields corresponding directly to columns in the dataset are blank,
     */
    protected boolean isBlankRow(int row, UploadData uploadData, int seq) {
    	for (UploadField fld : uploadFields.get(getAdjustedSeqForBlankRowCheck(seq))) {
			if (fld.getIndex() != -1 || fld.getField().isForeignKey()) {
				int idx = fld.getIndex();
				if (idx == -1) {
					idx = uploadData.indexOfWbFldName(fld.getWbFldName());
				}
				if (!StringUtils.isEmpty(uploadData.get(row, idx))) {
					return false;
				}
			}
		}
		return true;
	}
    
    /**
     * @param row
     * @param uploadData
     * @return true if a non-null constraint needs to be enforced.
     */
    protected boolean shouldEnforceNonNullConstraint(final int row, final UploadData uploadData, final int seq)
    {
    	//Generally, if all fields in a table are blank, and related tables don't require a record,
    	//then there is no need to enforce not-null constraints.
    	if (tblClass.equals(PrepType.class)) 
    	{
    		UploadTable prepTbl = uploader.getUploadTableByName("Preparation");
    		return !prepTbl.isBlankRow(row, uploadData, seq) || prepTbl.parentTableIsNonBlank(row, uploadData, true, seq);
    	}
    	    	
    	if (!isBlankRow(row, uploadData, seq))
    	{
    		return true;
    	}
    	
    	if (parentTableIsNonBlank(row, uploadData, true, seq))
    	{
    		return true;
    	}
    	
    	if (seq == 0 && iAmRequiredByARelationship())
    	{
    		return true;
    	}
    	
    	List<UploadTable> chillun = uploader.getChildren(this);
    	for (UploadTable chile : chillun) 	
    	{
    		if (iControlTheChild(chile) && !chile.isBlankRow(row, uploadData, seq))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    	
    }
    
    /**
     * @param child
     * @return true if this UploadTable "owns" child.
     */
    protected boolean iControlTheChild(UploadTable child)
    {
		if (specialChildren.contains(child))
		{
			return true;
		}
		
    	DBTableInfo tblInfo = child.getTable().getTableInfo();
		if (tblInfo != null)
		{
			for (DBRelationshipInfo relInfo : tblInfo.getRelationships())
			{
				if (relInfo.getDataClass().equals(tblClass))
				{
					if (relInfo.isRequired())
					{
						return true;
					}
				}
			}
		}
    	return false;
    }
    
    /**
     * @return return true if there is a relationship in the upload graph that requires this UploadTable
     */
    protected boolean iAmRequiredByARelationship()
    {
    	for (Vector<ParentTableEntry> parents : parentTables)
    	{
    		for (ParentTableEntry pte : parents)
    		{
    			DBTableInfo tblInfo = pte.getImportTable().getTable().getTableInfo();
    			if (tblInfo != null)
    			{
    				for (DBRelationshipInfo relInfo : tblInfo.getRelationships())
    				{
    					if (relInfo.getDataClass().equals(tblClass) /*&& don't think the colname matters here*/ 
    							&& relInfo.isRequired())
    					{
    						return true;
    					}
    				}
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * @param row
     * @param uploadData
     * @return
     */
    protected boolean parentTableIsNonBlank(final int row, final UploadData uploadData)
    {
    	return parentTableIsNonBlank(row, uploadData, false, -1);
    }

    
    /**
     *
     * @param row
     * @param uploadData
     * @param ignoreControllingParents
     * @param seq
     * @return
     */
    protected boolean parentTableIsNonBlank(final int row, final UploadData uploadData, final Boolean ignoreControllingParents, final int seq)
    {
    	for (Vector<ParentTableEntry> parents : parentTables)
    	{
    		for (ParentTableEntry pte : parents)
    		{
    			UploadTable ut = pte.getImportTable();
    			if (ignoreControllingParents && ut.specialChildren != null && ut.specialChildren.contains(this))
    			{
    				break;
    			}
    			if (seq == -1)
    			{
        			for (int s = 0; s < ut.getUploadFields().size(); s++)
        			{
        				if (!ut.isBlankRow(row, uploadData, s))
        				{
        					return true;
        				}
            			if (ut.parentTableIsNonBlank(row, uploadData))
            			{
            				return true;
            			}
        			}
    			} else 
    			{
    				if (!ut.isBlankRow(row, uploadData, seq))
    				{
    					return true;
    				}
        			if (ut.parentTableIsNonBlank(row, uploadData, false, seq))
        			{
        				return true;
        			}
    			}
    		}
     	}
    	return false;
    }
    
    /**
     * @return a list of the latlong flds in the table.
     */
    protected List<UploadField> getLatLongFlds()
    {
        Vector<UploadField> result = new Vector<UploadField>();
    	for (Vector<UploadField> flds : uploadFields)
        {
             for (UploadField fld : flds)
             {
             	String fldName = fld.getField().getName();
                if (fldName.equalsIgnoreCase("latitude1") || fldName.equalsIgnoreCase("latitude2")
                        || fldName.equalsIgnoreCase("longitude1") || fldName.equalsIgnoreCase("longitude2"))
                {
                	result.add(fld);
                }
            	 
             }
        }
    	return result;
    }
    
    /**
     * @return for an exported dataset, fields that are not the same
     * as the corresponding fields in the database for the given row, because 
     * they have been edited in the workbench or by other means.
     * 
     * NOTE: Assumes loadRecord() has been called for row for the 'root' table
     * of the dataset.
     */
    protected List<Pair<UploadField, Object>> getChangedFields(int row) throws UploaderException,
		InvocationTargetException, IllegalAccessException,
		NoSuchMethodException {
    	List<Pair<UploadField, Object>> result = new ArrayList<Pair<UploadField, Object>>();
//    	if (uploadFields.size() > 1) {
//    		throw new UploaderException("getChangedFields() is not implemented for one-to-manies", UploaderException.INVALID_DATA);
//    	} else {
//    		result.addAll(getChangedFields(row, 0));
//     	}
    	for (int s = 0; s < uploadFields.size(); s++) {
    		result.addAll(getChangedFields(row, s));
    	}
    	return result;
    }
        
    /**
     * @return for an exported dataset, fields that are not the same
     * as the corresponding fields in the database for the given row and seq, because 
     * they have been edited in the workbench or by other means.
     * 
     * NOTE: Assumes loadRecord() has been called for row for the 'root' table
     * of the dataset.
     */
   protected List<Pair<UploadField, Object>> getChangedFields(int row, int seq) throws UploaderException,
		InvocationTargetException, IllegalAccessException,
		NoSuchMethodException {
    	DataModelObjBase rec = getCurrentRecord(seq); //'root'.loadRecord(..., row) has been called!!!
    	List<Pair<UploadField, Object>> result = new ArrayList<Pair<UploadField, Object>>();
    	for (UploadField fld : uploadFields.get(seq)) {
    		if(fld.getIndex() != -1) {
    			//XXX it would be good to get valueChange to return the 
    			//db value, and return it with the fld...
    			Method getter = fld.getGetter();
    			boolean changed = false;
    			try {
    				if (isLatLongFld(fld)) {
    					String origVal = rec == null ? null : getTextForFieldValue(fld, getter.invoke(rec), seq);
    					if (origVal == null) {
    						origVal = "";
    					}
    					String currVal = fld.getValue();
    					if (currVal == null) {
    						currVal = "";
    					}
    					changed = !currVal.equals(origVal);
    				} else {
    					Object[] value = getArgForSetter(fld);
    					changed = valueChange(rec, getter, value);
    				}
    			} catch (Exception e) {
    				//probably an invalid entry
    				changed = true;
    			}
    			if (changed) {
    				try {
    					result.add(new Pair<UploadField, Object>(fld, getTextForFieldValue(fld, rec == null ? null : getter.invoke(rec),seq)));
    				} catch (Exception ex) {
    					throw new UploaderException(ex, UploaderException.INVALID_DATA);
    				}
    			} 
    		}
    	}
    	return result;
    }
   
   /**
 * @param uploadData
 * @param row
 * @return
 */
   protected boolean attachmentsPresent(UploadData uploadData, int row)
   {
	   List<WorkbenchRowImage> imgs = new ArrayList<WorkbenchRowImage>();
	   imgs.addAll(uploadData.getWbRow(row).getWorkbenchRowImages());
       for (WorkbenchRowImage wri : uploadData.getWbRow(row).getWorkbenchRowImages())
       {
       		if (uploader.getAttachToTable(wri) == this)
       		{
       			return true;
       		}
       }
       return false;
   }

    /**
     * @param row
     * @param uploadData
     * @param invalidValues
     * 
     * Validates user-entered fields for the row.
     * Validation issues are added to invalidValues vector.
     */
    public void validateRowValues(int row, UploadData uploadData, List<UploadTableInvalidValue> invalidValues) 
    {
        
    	if (uploadData.isEmptyRow(row))
    	{
			if (attachmentsPresent(uploadData, row))
			{
				invalidValues.add(new UploadTableInvalidValue(
						null,
						this,
						this.uploadFields.get(0),
						row,
						new Exception(
								String.format(getResourceString("UploadTable.AttachmentPresentButNoData"), getTable().getTableInfo().getTitle()))));
			}
    		return;
    	}
    	
		try
		{
			validatingValues = true;

			int seq = 0;
			boolean gotABlank = false;

			// for Locality table only
			LatLonConverter.FORMAT llFmt1 = null;
			LatLonConverter.FORMAT llFmt2 = null;
			// GeoRefConverter gc = new GeoRefConverter();
			UploadField llFld = null; // for 'generic' latlon errors.

			Vector<UploadTableInvalidValue> invalidNulls = new Vector<UploadTableInvalidValue>();
			Vector<Integer> blankSeqsLocal = new Vector<Integer>();
			for (Vector<UploadField> flds : uploadFields) {
				boolean isBlank = true;
				UploadField currFirstFld = null;
				for (UploadField fld : flds) {
					if (fld.getIndex() != -1) {
						if (currFirstFld == null) {
							currFirstFld = fld;
						}
						fld.setValue(uploadData.get(row, fld.getIndex()));
						isBlank &= isBlankVal(fld, seq, row, uploadData);;
						try {
							if (invalidNull(fld, uploadData, row, seq)) {
								if (shouldEnforceNonNullConstraint(row,
										uploadData, seq)) {
									//throw new Exception(
									//		getResourceString("WB_UPLOAD_FIELD_MUST_CONTAIN_DATA"));
									invalidNulls.add(new UploadTableInvalidValue(
											getResourceString("WB_UPLOAD_FIELD_MUST_CONTAIN_DATA"), this, fld, row, null));		
									continue;
								}
							}
							if (fld.getValue() != null && !"".equals(fld.getValue()) && fld.isAutoAssignForUpload()/* && fld == autoAssignedField*/) {
								boolean throwUp = !isUpdateMatches();
								/* assume any autoincrementer being batch edited is ok, though complications may arise for
								 * fields other than catalognumber
								if (!throwUp) {
								    //this should be 99.8% OK
							        DBFieldInfo fi = fld.getField() != null ? fld.getField().getFieldInfo() : null;
							        boolean catnum = fi != null && fi.getTableInfo().getTableId() == CollectionObject.getClassTableId() && "catalognumber".equals(fi.getName().toLowerCase());
                                    throwUp = !catnum;
                                }
                                */
							    if (throwUp) {
							        throw new Exception(UIRegistry.getResourceString("WB_UPLOAD_AutoAssMustBeBlankErrMsg"));
                                }
							}
							if (!pickListCheck(fld)) {
								if (!fld.isReadOnlyValidValues()) {
									if (uploader != Uploader.currentUpload) {
										invalidValues
												.add(new UploadTableInvalidValue(
														null,
														this,
														fld,
														null,
														row,
														new Exception(
																getInvalidPicklistValErrMsg(fld)),
														true));
										continue;
									}
								} else {
									throw new Exception(
											getInvalidPicklistValErrMsg(fld));
								}
							}
							Object[] finalVal = getArgForSetter(fld);
							if (!updateMatches) {
								//XXX But what if catnum has been changed... Shouldn't CatNum be made unchangeable???
								checkUniqueness(finalVal, fld);
							}
						} catch (Exception e) {
							invalidValues.add(new UploadTableInvalidValue(
								null, this, fld, row, e));
						}
					}
					if (tblClass.equals(Locality.class)) {
						// Check row to see that lat/long formats are the same.
						String fldName = fld.getField().getName();
						if (fldName.equalsIgnoreCase("latitude1")
								|| fldName.equalsIgnoreCase("latitude2")
								|| fldName.equalsIgnoreCase("longitude1")
								|| fldName.equalsIgnoreCase("longitude2")) {
							llFld = fld;
							LatLonConverter.FORMAT fmt = geoRefConverter.getLatLonFormat(StringUtils.stripToNull(fld.getValue()));
							LatLonConverter.FORMAT llFmt = fldName.endsWith("1") ? llFmt1 : llFmt2;
							boolean checkDecimalPlaces = true;
							if (llFmt == null) {
								llFmt = fmt;
								if (fldName.endsWith("1")) {
									llFmt1 = fmt;
								} else {
									llFmt2 = fmt;
								}
							} else {
								fmt = GeoRefConverter.getLeastCommonFmt(llFmt, fmt);
							    //if (!llFmt.equals(fmt)) {
                                if (fmt == null) {
									checkDecimalPlaces = false;
									invalidValues.add(new UploadTableInvalidValue(null, this, getLatLongFlds(), row,
													new Exception(UIRegistry.getResourceString("WB_UPLOADER_INVALID_LATLONG"))));
								} 
							}
							if (checkDecimalPlaces && fmt != null && fmt != LatLonConverter.FORMAT.None) {
								//check decimal places
								//lame
								int c = fld.getValue().indexOf(decSep);
								if (c > -1) {
									int d;
									String points = fld.getValue().substring(c+1);
									for (d = 0; d < points.length(); d++) {
										//System.out.println(points.substring(d, d+1));
										if (!"0123456789".contains(points.substring(d, d+1))) break;
									}
									if (d > LatLonConverter.DECIMAL_SIZES[fmt.ordinal()]) {
										invalidValues.add(new UploadTableInvalidValue(null, this, fld, row,
												new Exception(String.format(
														UIRegistry.getResourceString("WB_UPLOADER_TOO_MANY_FRACTION_DIGITS"),
														LatLonConverter.DECIMAL_SIZES[fmt.ordinal()]))));
									}
								}
							}
						}
						//Check LatLongType
						if (fldName.equalsIgnoreCase("LatLongType"))
						{
							boolean hasLat1 = false, hasLong1 = false, hasLat2 = false, hasLong2 = false;
							for (UploadField f : getLatLongFlds())
							{
			                	f.setValue(uploadData.get(row, f.getIndex()));
								String coordName = f.getField().getName();
								if (coordName.equalsIgnoreCase("latitude1") && StringUtils.isNotBlank(f.getValue()))
								{
									hasLat1 = true;
								} else if (coordName.equalsIgnoreCase("longitude1") && StringUtils.isNotBlank(f.getValue()))
								{
									hasLong1 = true;
								} else if (coordName.equalsIgnoreCase("latitude2") && StringUtils.isNotBlank(f.getValue()))
								{
									hasLat2 = true;
								} else if (coordName.equalsIgnoreCase("longitude2") && StringUtils.isNotBlank(f.getValue()))
								{
									hasLong2 = true;
								}
							}
							boolean hasCoord1 = hasLat1 && hasLong1;
							boolean hasCoord2 = hasLat2 && hasLong2;

							if ((hasCoord1 || hasCoord2) && StringUtils.isBlank(fld.getValue())) {
								invalidNulls.add(new UploadTableInvalidValue(
										getResourceString("WB_UPLOAD_FIELD_MUST_CONTAIN_DATA"), this, fld, row, null));		
								continue;

							}
								
							if (!hasCoord1 && !hasCoord2 && StringUtils.isBlank(fld.getValue())) {
								continue;
							}

											
							//Assuming the pick list is localized...
							String pntStr = UIRegistry.getResourceString("Locality.LL_TYPE_POINT");
							String lineStr = UIRegistry.getResourceString("Locality.LL_TYPE_LINE");
							String rectStr = UIRegistry.getResourceString("Locality.LL_TYPE_RECTANGLE");

							Set<String> validValues = new TreeSet<String>();
							for (String item : fld.getValidValues().keySet())
							{
								if (item.equals(pntStr) && hasCoord1 && !hasCoord2)
								{
									validValues.add(item);
								} else if ((item.equals(lineStr) || item.equals(rectStr)) && hasCoord1 && hasCoord2)
								{
									validValues.add(item);
								}
							}
							if (!validValues.contains(fld.getValue()))
							{
								if (uploader != Uploader.currentUpload)
								{
									invalidValues
										.add(new UploadTableInvalidValue(
											null,
											this,
											fld,
											null,
											row,
											new Exception(
													getInvalidPicklistValErrMsg(fld, validValues)),
											false));
									continue;
								}
							}
						}
					}
				}

				if (tblClass.equals(Locality.class))
				{
					if (llFmt1 != llFmt2
						&& llFmt2 != null
						&& llFmt2 != LatLonConverter.FORMAT.None)
					{
						invalidValues
							.add(new UploadTableInvalidValue(
									null,
									this,
									llFld,
									row,
									new Exception(
											UIRegistry
													.getResourceString("WB_UPLOADER_INVALID_LATLONG"))));
					}				
				}	
	
				if (isBlank)
				{
					if (attachmentsPresent(uploadData, row))
					{
						invalidValues.add(new UploadTableInvalidValue(
								null,
								this,
								flds,
								row,
								new Exception(
										String.format(getResourceString("UploadTable.AttachmentPresentButNoData"), getTable().getTableInfo().getTitle()))));
					}
					
				} 				
				isBlank = isBlankSequence(isBlank, uploadData, row, seq, null);
				blankSeqs.set(seq, isBlank);
				if (isBlank)
				/*
				 * Disallow situations where 1-many lists have 'holes' - eg.
				 * CollectorLastName2 is blank but CollectorLastName1 and -3 are
				 * not.
				 */
				{
					gotABlank = true;
					blankSeqsLocal.add(seq);
					
				} else if (!isBlank && gotABlank && plugHoles)
				{
					for (Integer blank : blankSeqsLocal)
					{
						for (UploadField blankSeqFld : getBlankFields(blank,
								row, uploadData))
						{
							addInvalidValueMsgForOneToManySkip(invalidValues,
									blankSeqFld, toString(), row, blank);
						}
					}
					blankSeqsLocal.clear();
				}

				invalidValues.addAll(invalidNulls);
				invalidNulls.clear();

				seq++;
			}
			if (tblClass.equals(Determination.class))
			{
				// check that isCurrent is ok. 1 and only one true.
				boolean isCurrentPresent = false;
				UploadField anIsCurrentFld = null;
				boolean isBlank = true;
				// for (int row = 0; row < uploadData.getRows(); row++)
				// {
				int trueCount = 0;
				for (Vector<UploadField> flds : uploadFields)
				{
					for (UploadField fld : flds)
					{
						if (isBlank && StringUtils.isNotBlank(uploadData.get(row, fld.getIndex()))) {
							isBlank = false;
						}
						if (fld.getField().getName().equalsIgnoreCase(
								"iscurrent"))
						{
							isCurrentPresent = true;
							if (anIsCurrentFld == null) {
								anIsCurrentFld = fld;
							}
							fld.setValue(uploadData.get(row, fld.getIndex()));
							try
							{
								Object[] boolVal = getArgForSetter(fld);
								if (boolVal[0] != null && (Boolean) boolVal[0])
								{
									trueCount++;
								}
							} catch (Exception e)
							{
								// ignore. assuming problem was already caught
								// above.
							}
						}
					}
				}
				if (isCurrentPresent && !isBlank && trueCount != 1)
				{
					invalidValues
							.add(new UploadTableInvalidValue(
									null,
									this,
									anIsCurrentFld,
									row,
									new Exception(
											getResourceString("WB_UPLOAD_ONE_CURRENT_DETERMINATION"))));
				}
				// }
			}

			if (tblClass.equals(Agent.class))
			{
				// check that isCurrent is ok. 1 and only one true.
				boolean nonPersonNonEmpty = false;
				boolean isNonPerson = false;
				Vector<UploadField> personOnlyFlds = new Vector<UploadField>();
				for (Vector<UploadField> flds : uploadFields)
				{
					for (UploadField fld : flds)
					{
						try
						{
							if (fld.getField().getName().equalsIgnoreCase(
									"firstName")
									|| fld.getField().getName()
											.equalsIgnoreCase("middleInitial")
									|| fld.getField().getName()
											.equalsIgnoreCase("title"))
							{
								Object[] val = getArgForSetter(fld);
								nonPersonNonEmpty = StringUtils
										.isNotEmpty((String) val[0]);
								personOnlyFlds.add(fld);
							}
							if (fld.getField().getName().equalsIgnoreCase(
									"agenttype"))
							{
								Object[] val = getArgForSetter(fld);
								isNonPerson = val[0] != null
										&& !((Byte) val[0])
												.equals(Agent.PERSON);
								if (!isNonPerson)
								{
									break;
								}
							}
						} catch (Exception e)
						{
							// ignore. assuming problem was already caught
							// above.
						}
					}
					if (isNonPerson && nonPersonNonEmpty)
					{
						for (UploadField poFld : personOnlyFlds)
						{
							invalidValues
									.add(new UploadTableInvalidValue(
											null,
											this,
											poFld,
											row,
											new Exception(
													getResourceString("UploadTable.FieldNotApplicableForAgentType"))));
						}
					}
				}
			}
		} finally
		{
			validatingValues = false;
		}

    }
    
    /**
     * @param val
     * @param fld
     * @throws Exception if val for fld already exists
     */
    protected void checkUniqueness(final Object[] val, final UploadField fld) throws Exception
    {
		if (val != null && val[0] != null 
				&& fld.getIndex() != -1
				&& fld.getField() != null 
				&& fld.getField().getFieldInfo() != null)
		{
			//assuming null values are ignored in uniqueness constraint
			DBFieldInfo fldInfo = fld.getField().getFieldInfo();
			DBTableInfo tblInfo = fldInfo.getTableInfo();
			//Just doing this for catalognumber. There doesn't seem to be enough info in DBTableInfo
			//and DBFieldInfo to do it generally.
			if (fldInfo.getName().equalsIgnoreCase("catalognumber")  && tblInfo.getName().equals("collectionobject"))
			{
				if (!BasicSQLUtils.getCount("select count(*) from collectionobject where CollectionMemberID = "
					+ AppContextMgr.getInstance().getClassObject(Collection.class).getId()
					+ " and CatalogNumber = '" + val[0] + "'").equals(0))
				{
					throw new Exception(getResourceString("UploadTable.UniquenessViolation"));
				}
			}
		}

    }
    
    
    /**
     * @param rec
     * @param pt
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    protected void addDisusedRec(DataModelObjBase rec, ParentTableEntry pt) throws InvocationTargetException, IllegalAccessException {
    	//XXX See comments for disUseRecs()
        UploadTable parentTbl = pt.getImportTable();
    	DataModelObjBase parentRec = (DataModelObjBase )pt.getGetter().invoke(rec);
    	if (parentRec != null) {
            pt.getImportTable().addDisusedRec(parentRec);
        }
    }

    /**
     *
     * @param rec
     */
    protected void addDisusedRec(DataModelObjBase rec) {
        //XXX See comments for disUseRecs()
        if (deleteUnusedRecs) {
            String text = DataObjFieldFormatMgr.getInstance().format(rec, getTable().getTableInfo().getDataObjFormatter());
            Pair<Integer, String> disused = new Pair<Integer, String>(rec.getId(), text);
            if (!disUsedRecs.contains(disused)) {
                disUsedRecs.add(disused);
            }
        }
    }

    /**
     *
     * @param theDisUsed
     * @param session
     */
    protected void disUseRecs(Set<Pair<Integer, String>> theDisUsed, final DataProviderSessionIFace session) {
    	//This is called during FinishUpload, by the Uploader in reverse order of the upload graph.
    	//An attempt is made to delete any record that was dereferenced during the upload. 
    	//XXX should test if deletable before trying. Current process is to delete and see if it works.
    	//XXX Also need to recurse upwards. If a locality is disused, then it's geography needs to be disused, and the geography's parent, ...
        if (tblClass.equals(Agent.class) || Treeable.class.isAssignableFrom(tblClass) || tblClass.equals(PrepType.class))  {
        	log.warn("Not attempting to remove disused recs for class: " + tblClass);
        	return;
        }

    	
    	SpecifyDeleteHelper delhel = uploader.getDeleteHelper();
    	for (Pair<Integer, String> disUsedOne : theDisUsed) {
    		disUseRec(disUsedOne, delhel, session);
    	}
    	delhel.done(false);
    	for (Pair<Integer, String> deletedRec : deletedRecs) {
        	log.info("Not attempting to remove disused recs for class: " + tblClass);
    	}
    }

    /**
     *
     * @param disUsedRec
     * @param delhel
     * @param session
     */
    protected void disUseRec(final Pair<Integer, String> disUsedRec, final SpecifyDeleteHelper delhel, final DataProviderSessionIFace session) {
    	log.info("deleting " + disUsedRec);
        //XXX Delete helper absolutely blows for PrepType. Will let any preptype be deleted, deletes all associated preparations. WTF.
        //disUseRecs() filters out preptype, but what other tables might be screwed??? Check persistence annotations??
        try {
    		//XXX what will isRecordReferenced() do for a locality with no ces, but with locdet/geocoorddet??
            if (!delhel.isRecordReferenced(tblClass, disUsedRec.getFirst(), session)) {
                delhel.delRecordFromTable(tblClass, disUsedRec.getFirst(), true, session);
                if (session != null) {
                    session.flush();
                    session.clear();
                }
                deletedRecs.add(disUsedRec);
            }
    	} catch (SQLException sqex) {
            if (session == null) delhel.rollback();
    		log.warn("unable to delete " + tblClass.getSimpleName() + ":" + disUsedRec);
    	}
    }
    

    /**
     * @param blankSeq
     * @param row
     * @param uploadData
     * @return
     */
    protected List<UploadField> getBlankFields(int blankSeq, int row, UploadData uploadData)
    {
		List<UploadField> result = new LinkedList<UploadField>();
		if (blankSeq < uploadFields.size()) {
			for (UploadField blankSeqFld : uploadFields.get(blankSeq)) {
				if (blankSeqFld.getIndex() != -1) {
					result.add(blankSeqFld);
				}
			}

			// Set<Class<?>> pts = getSequedParentClasses();
			for (Vector<ParentTableEntry> ptes : parentTables) {
				for (ParentTableEntry pte : ptes) {
					if (pte.getImportTable().isSequenced)
					// if (pts.contains(pte.getImportTable().getTblClass()))
					{
						result.addAll(pte.getImportTable().getBlankFields(
								blankSeq, row, uploadData));
					}
				}
			}
		}
		return result;
    }
    
    /**
     * @param blank
     * @param uploadData
     * @param row
     * @param seq
     *
     * @return true if blank and blankness matters
     * 
     * Checks relationships and datatype to see if table data for row and sequence is really blank and/or
     * if it is not OK for it to be blank.
     */
    protected boolean isBlankSequence(final boolean blank, final UploadData uploadData, final int row, final int seq, final UploadTable childCaller) {
		if (!blank) {
			return false;
		}
		if (parentTables.size() > 0) {
			for (Vector<ParentTableEntry> ptes : parentTables) {
				for (ParentTableEntry pte : ptes) {
					if (pte.getImportTable().isSequenced) {
						if (!pte.getImportTable().isBlankSequence(pte.getImportTable().blankSeqs.get(seq), uploadData, row, seq, this)) {
							return false;
						}
					}
				}
			}
			return true;
		}
		return true;
    }
    
    
     /**
     * @param uploadData
     * @return Vector of invalid values.
     * 
     * Validates values in all workbench cells that are mapped to this table.
     */
	public Vector<UploadTableInvalidValue> validateValues(
			final UploadData uploadData) {
		Vector<UploadTableInvalidValue> result = new Vector<UploadTableInvalidValue>();
		boolean isUpdate = uploader.isUpdateUpload();
		for (int row = 0; row < uploadData.getRows(); row++) {
			if (!isUpdate || uploader.rowHasEdits(row)) {
                validateRowValues(row, uploadData, result);
            }
		}
		return result;
	}

    /**
     * @param wbRow
     * @throws UploaderException
     * 
     * This is loads values from dataset into current DataModelObj record, but does not save record
     * to the database. It might be a good idea to track that data has been loaded to avoid
     * unecessary repetition.
     */
    protected void loadFromDataSet(int wbRow) throws UploaderException
    {
    	if (wbRow == 0 || wbRow != wbCurrentRow)
    	{
    		readFromDataSet(wbRow, false);
    		writeRowOrNot(wbRow == 0 || wbCurrentRow < wbRow, wbRow == 0 || wbCurrentRow < wbRow, false);
    		readFromDataSet(wbCurrentRow, true);
    	}
    }

    /**
     *
     * @param wbRow
     * @param restore
     * reads data from the dataset to fields in this table and it's parent tables
     */
    protected void readFromDataSet(int wbRow, boolean restore) {
    	uploader.loadRow(this, wbRow);
		for (Vector<ParentTableEntry> ptes : parentTables) {
			for (ParentTableEntry pt : ptes) {
				if (pt.getImportTable() != null) {
					pt.getImportTable().readFromDataSet(restore ? pt.getImportTable().wbCurrentRow : wbRow, restore);
				}
			}
		}
    }
    
    /**
     *
     * @param row
     * @param tblAndAncestorsUnchanged
     * @throws UploaderException
     */
    protected void writeRow(int row, boolean tblAndAncestorsUnchanged) throws UploaderException {
        wbCurrentRow = row;
        for (int i = 0; i < matchCountForCurrentRow.length; i++) {
        	matchCountForCurrentRow[i] = 0;
        }
        if (!skipRow) {
        	writeRowOrNot(false, false, tblAndAncestorsUnchanged);
        } else {
        	skipRow = false;
       	}
    }
    
    /**
     *
     * @param row
     * @param tblsWithChanges
     * @return
     */
    public boolean rowHasChanges(int row, List<UploadTable> tblsWithChanges) {
    	//assumes loadrow has been called
    	List<WorkbenchDataItem> changedItems = uploader.getEditedItems(row);
    	List<Integer> editedCols = new ArrayList<Integer>();
    	for (WorkbenchDataItem di : changedItems) {
    		WorkbenchTemplateMappingItem mi = di.getWorkbenchTemplateMappingItem();
    		if (mi != null) {
    			editedCols.add(mi.getViewOrder().intValue());
    		}
    	}
    	for (List<UploadField> flds : uploadFields) {
    		for (UploadField fld : flds) {
    			if (editedCols.indexOf(fld.getIndex()) != -1) {
    				return true;
    			}
    		}
    	}
    	for (UploadTable sc: specialChildren) {
    	    if (sc.rowHasChanges(row, tblsWithChanges)) {
    	        return true;
            }
        }
    	for (List<ParentTableEntry> ptes : parentTables) {
    	    for (ParentTableEntry pte : ptes) {
    	        if (tblsWithChanges.indexOf(pte.getImportTable()) != -1) {
    	            return true;
                }
            }
        }
    	return false;
    }

    /**
     *
     * @param tblAndAncestorsUnchanged
     * @param seq
     * @return
     */
    protected boolean stillNeedToWrite(boolean tblAndAncestorsUnchanged, int seq) throws UploaderException {
        if (!updateMatches) {
            return true;
        }
        if (tblAndAncestorsUnchanged && specialChildren.size() == 0) {
            return false;
        }
        //assuming findMatch and setCurrentRecordFromMatch have been called...
        boolean isBlankRow = isBlankRow(wbCurrentRow, uploader.getUploadData(), seq);
        if (isBlankRow) {
            for (UploadTable sc: specialChildren) {
                if (!sc.isBlankRow(wbCurrentRow, uploader.getUploadData(), seq)) {
                    isBlankRow = false;
                    break;
                }
            }
        }
        if (isRemovedRecord(seq, tblAndAncestorsUnchanged)) {
            return false;
        }
        if (!isBlankRow) {
            return true;
        }
        if (getCurrentRecord(seq) == null) {
            return needToCreateRecordIfParentChanged(seq);
        }
        if (getCurrentRecord(seq) != null) {
            return !tblAndAncestorsUnchanged;
        }
        if (!hasChildren) {
            return false;
        }
        return true; //wtf??
    }

    /**
     *
     * @return
     * @throws UploaderException
     */
    protected boolean needToCreateRecordIfParentChanged(int recNum) throws UploaderException {
        //no attempt at generality
        if (tblClass.equals(CollectingEvent.class) || tblClass.equals(Locality.class) || tblClass.equals(PaleoContext.class)) {
            return true;
        } else if (isOneToOneChild()) {
            return false;
        } else {
            throw new UploaderException("Unsupported situation for " + this.toString(), UploaderException.ABORT_ROW);
        }
    }

    protected boolean probablyNeedToWrite(int recNum, boolean tblAndAncestorsUnchanged) throws UploaderException {
        if (updateMatches) {
            return needToWrite(recNum) || !tblAndAncestorsUnchanged;
        } else {
            return needToWrite(recNum);
        }
    }

    protected int numberOfVisibleFields() {
        int result = 0;
        for (UploadField uf : uploadFields.get(0)) {
            if (uf.getIndex() > -1) {
                result++;
            }
        }
        return result;
    }

    protected boolean isRemovedRecord(int seq, boolean tblAndAncestorsUnchanged) throws UploaderException {
        boolean result = false;
        if (Treeable.class.isAssignableFrom(tblClass)) {
            return isBlankRow(wbCurrentRow, uploader.getUploadData(), seq)
                    && numberOfVisibleFields() == 1;
        } else {
            result = !needToWrite(seq) && !tblAndAncestorsUnchanged;
            if (result) {
                boolean requiredIsPresent = false;
                for (UploadField uf : uploadFields.get(0)) {
                    if (uf.isRequired()) {
                        requiredIsPresent = true;
                        break;
                    }
                }
                result &= requiredIsPresent;
            }
        }
        return result;
    }

    protected boolean findMatch(boolean doSkipMatch, boolean tblAndAncestorsUnchanged, int recNum)
            throws UploaderException,
            InvocationTargetException, IllegalAccessException, ParseException,
            NoSuchMethodException, InstantiationException, SQLException   {
        if (updateMatches) {
            if (doSkipMatch || isRemovedRecord(recNum, tblAndAncestorsUnchanged)) {
                return true;
            }
            if (tblAndAncestorsUnchanged) {
                return false;
            }
            return findMatch(recNum, false, null, null) || updateAddingNewRecord || reusingExportedRec || currentRecSetFromExportedRec;
        } else {
            return doSkipMatch || !findMatch(recNum, false, null, null);
        }
    }
    /**
     * Searches for matching record in database. If match is found it is set to be the current
     * record. If no match then a record is initialized and populated and written to the database.
     *
     *
     * @param doNotWrite
     * @param skipMatch
     * @param tblAndAncestorsUnchanged
     * @throws UploaderException
     */
    protected void writeRowOrNot(boolean doNotWrite, boolean skipMatch, boolean tblAndAncestorsUnchanged) throws UploaderException
    {
        //int recNum = 0;
        logDebug("writeRowOrNot: " + this.table.getName());
        //System.out.println("writeRowOrNot: " + this.table.getName() + " (" + wbCurrentRow + ")");
        autoAssignedVal = null;  //assumes one autoassign field per table.
        reusingExportedRec = false;
        currentRecSetFromExportedRec = false;
        updateAddingNewRecord = false;
        boolean doSkipMatch = false;
        if (!updateMatches) {
        	doSkipMatch = skipMatch || isMatchChild() //Bug #9375 don't prevent dup manies in 1-manies. May cause constraint violations for some tables.
            		|| (this.table.getTableInfo() != null && this.table.getTableInfo().getTableId() == 1 && this == uploader.getRootTable());
        }
        for (int recNum = uploadFields.size() - 1; recNum >= 0; recNum--) {
            Vector<UploadField> seq = uploadFields.get(recNum);
        	try {
                do {
                    if (updateMatches) {
                        updateExportedRecInfo(recNum);
                    }
                    if (probablyNeedToWrite(recNum, tblAndAncestorsUnchanged)) {
                        if (findMatch(doSkipMatch, tblAndAncestorsUnchanged, recNum)) {
                            if (isSecurityOn && !getWriteTable().getTableInfo().getPermissions().canAdd()) {
                                throw new UploaderException(String.format(UIRegistry.getResourceString("WB_UPLOAD_NO_ADD_PERMISSION"), getWriteTable().getTableInfo().getTitle()),
                                        UploaderException.ABORT_ROW);
                            }
                            if (stillNeedToWrite(tblAndAncestorsUnchanged, recNum)) {
                                Pair<Boolean, DataModelObjBase> recObj = getCurrentRecordForSave(recNum);
                                    DataModelObjBase rec = recObj.getSecond();
                                    boolean isNewRecord = rec.getId() == null;
                                if (!updateMatches || isNewRecord || checkForUpdate(recObj.getSecond())) {
                                    boolean freshlyCreatedRec = recObj.getFirst();
                                    //XXX is it not possible to skip all the field setting here
                                    //if no changes have been made? getChangedFields() could be used
                                    // ... and if no fields have been changed then skip the rest (or nearly the rest)
                                    //of this method, else use the changed fields to make the rest more efficient???.
                                    //
                                    //XXX But HEY!!! getChangedFields does not check parents. It was originally designed
                                    //to be run for all tables during getChangedFields(row).
                                    if (freshlyCreatedRec || !updateMatches) {
                                        rec.initialize();
                                    }
                                    boolean valuesChanged = (updateMatches && hiddenMissingLink) ? false : setFields(rec, seq);
                                    boolean isUpdate = updateMatches && !isNewRecord && valuesChanged;
                                    boolean gotRequiredParents = true;
                                    try {
                                        valuesChanged |= setParents(rec, recNum, !doNotWrite);
                                        isUpdate |= valuesChanged;
                                    } catch (UploaderException ex) {
                                        if ("MissingRequiredParent".equals(ex.getMessage())) {
                                            gotRequiredParents = false;
                                        } else {
                                            throw ex;
                                        }
                                    }
                                    if (!updateMatches || freshlyCreatedRec) {
                                        setRequiredFldDefaults(rec, recNum);
                                        setRelatedDefaults(rec, recNum);
                                    }
                                    if (updateMatches && isUpdate && !isNewRecord) {
                                        rec.setTimestampModified(new Timestamp(System.currentTimeMillis()));
                                        rec.setModifiedByAgent(Agent.getUserAgent());
                                    }
                                    isUpdate |= finalizeWrite(rec, recNum);
                                    //if (updateMatches && recUserFldsAreBlank(rec) && shouldDumpBlankRec()) {
                                    //    setCurrentRecord(null, recNum);
                                    //}else
                                    if (!(doNotWrite || (updateMatches && !isUpdate && !isNewRecord && matchCountForCurrentRow[recNum] != 1))) {
                                        if (!gotRequiredParents && hasChildren) {
                                            throw new UploaderException(UIRegistry.getResourceString("UPLOADER_MISSING_REQUIRED_DATA"), UploaderException.ABORT_ROW);
                                        }
                                        rec = doWrite(rec);
                                        setCurrentRecord(rec, recNum);
                                        doUploadBookkeeping(rec, recNum, isUpdate, isNewRecord);
                                    }
                                }
                                finishDepth(rec, recNum);
                            } else if (updateMatches) {
                                //if (isBlankRow(wbCurrentRow, uploader.getUploadData(), recNum)) {
                                if (isRemovedRecord(recNum, tblAndAncestorsUnchanged)) {
                                    setCurrentRecord(null, recNum);
                                } else {
                                    setCurrentRecord(exportedRecord, recNum);
                                }
                            }
                        } else if (updateMatches) {
                            setCurrentRecord(getExportedRecord(), recNum);
                        }
                    } else {
                        if (exportedOneToManyId != null) {
                            banishChild();
                        }
                        setCurrentRecord(null, recNum);
                    }

                } while(fallDown());
                finishRow();
            } catch (InstantiationException ieEx) {
                throw new UploaderException(ieEx, UploaderException.ABORT_IMPORT);
            }
            catch (IllegalAccessException iaEx) {
                throw new UploaderException(iaEx, UploaderException.ABORT_IMPORT);
            }
            catch (NoSuchMethodException ssmEx) {
                throw new UploaderException(ssmEx, UploaderException.ABORT_IMPORT);
            }
            catch (InvocationTargetException itEx) {
                throw new UploaderException(itEx, UploaderException.ABORT_IMPORT);
            }
            catch (IllegalArgumentException iaA) {
                throw new UploaderException(iaA, UploaderException.ABORT_IMPORT);
            }
            catch (ParseException peEx) {
                throw new UploaderException(peEx, UploaderException.ABORT_IMPORT);
            }
            catch (SQLException sqEx) {
                throw new UploaderException(sqEx, UploaderException.ABORT_IMPORT);
            }
        }
    }

    protected boolean shouldDumpBlankRec() {
        return !(tblClass.equals(CollectingEvent.class) && AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent());
    }

    protected boolean recUserFldsAreBlank(final DataModelObjBase rec) throws UploaderException {
        DBTableInfo tblInfo = getTable().getTableInfo();
        List<Method> getters = new ArrayList<>();
        try {
            for (DBFieldInfo fldInfo : tblInfo.getFields()) {
                if (isUserFld(fldInfo)) {
                    getters.add(tblClass.getMethod("get" + capitalize(fldInfo.getName())));
                }
            }
            for (DBRelationshipInfo relInfo : tblInfo.getRelationships()) {
                if (relInfo.getType().equals(DBRelationshipInfo.RelationshipType.ManyToOne) && !isSystemRel(relInfo)) {
                    getters.add(tblClass.getMethod("get" + capitalize(relInfo.getName())));
                }
            }
            boolean result = true;
            for (Method getter :  getters) {
                if (getter.invoke(rec) != null) {
                    result = false;
                    break;
                }
            }
            return result;
        } catch (Exception e) {
            log.error(e);
            throw new UploaderException(e, UploaderException.ABORT_IMPORT);
        }
    }

    protected boolean isUserFld(final DBFieldInfo fld) {
        boolean result = RecordMatchUtils.isUserFld(fld.getColumn());
        if (result) {
            result = !(fld.getColumn().equalsIgnoreCase("originallatlongunit")
                    || fld.getColumn().equalsIgnoreCase("srclatlongunit")
                    || fld.getColumn().equalsIgnoreCase("latlongtype")
                    || fld.getColumn().equalsIgnoreCase("isaccepted")
                    || fld.getColumn().equalsIgnoreCase("acceptedid")
                    || fld.getColumn().equalsIgnoreCase("rankid")
            );
        }
        return result;
    }

    protected boolean isSystemRel(final DBRelationshipInfo rel)  {
        return (rel.getClassName().equals("edu.ku.brc.specify.datamodel.Agent") && ("CreatedByAgentID".equalsIgnoreCase(rel.getColName()) || "ModifiedByAgentID".equalsIgnoreCase(rel.getColName())))
                || "DisciplineID".equalsIgnoreCase(rel.getColName())
                || "CollectionID".equalsIgnoreCase(rel.getColName())
                || "CollectionMemberID".equalsIgnoreCase(rel.getColName())
                || "DivisionID".equalsIgnoreCase(rel.getColName())
                || "InstitutionID".equalsIgnoreCase(rel.getColName())
                || "SpecifyUserID".equalsIgnoreCase(rel.getColName())
                || "ParentID".equalsIgnoreCase(rel.getColName())
                || rel.getColName().endsWith("TreeDefID")
                || rel.getColName().endsWith("TreeDefItemID");
    }
    /**
     *
     */
    protected void finishRow() {
        //nada
    }
    /**
     *
     * @return false if rock bottom else true
     */
    protected boolean fallDown() {
        return false;
    }
    /**
     *
     * @param rec
     * @param recNum
     */
    protected void finishDepth(final DataModelObjBase rec, int recNum) throws UploaderException {
        setCurrentRecord(rec, recNum);
        finishMatching(rec);
    }

    protected boolean hasRecordBeenUpdated(final DataModelObjBase rec) {
        Timestamp recStamp = rec.getTimestampModified() == null ? rec.getTimestampCreated() : rec.getTimestampModified();
        boolean result = recStamp.after(uploader.getWb().getTimestampCreated());
        if (result) {
            if(isOneToOneChild()) {
                UploadTable p = getControllingIDMatchingParent().importTable;
                DataModelObjBase prec = p.getCurrentRecord(0);
                if (prec != null) {
                    result = !p.hasRecordBeenUploaded(prec.getId());
                }
            } else {
                result = !hasRecordBeenUploaded(rec.getId());
            }
        }

        return result;
    }

    protected boolean hasRecordBeenUploaded(final Integer id) {
        boolean result = uploadedRecs.getThird().contains(id);
        if (!result) {
            for (UploadedRecordInfo ri : uploadedRecs.getFirst()) {
                if (ri.getKey().equals(id)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    protected boolean checkForUpdate(final DataModelObjBase rec) {
        boolean result = hasRecordBeenUpdated(rec);
        if (result) {
            uploader.addMsg(new EditedRecNotUpdatedMsg(rec, uploader.getRow(), this));
        }
        return !result;
    }


    /**
     * @param rec
     * @param recNum
     * @param isUpdate
     * @param isNewRecord
     */
    protected void doUploadBookkeeping(final DataModelObjBase rec, final int recNum, final boolean isUpdate, final boolean isNewRecord) {
        if (!updateMatches || isNewRecord) {
        	uploadedRecs.getSecond().add(new UploadedRecordInfo(rec.getId(), wbCurrentRow,
        		recNum, autoAssignedVal));
        	if (updateMatches) {
                uploadedRecs.getThird().add(rec.getId());
            }
        } else if (isUpdate && updateMatches) {
        	//System.out.println("UploadTable.writeRowOrNot: updated " + rec.getId() + " in " + rec.getClass().getSimpleName());
        	uploadedRecs.getFirst().add(new UploadedRecordInfo(rec.getId(), wbCurrentRow, recNum, autoAssignedVal, true,
        			null, null)); //could clone rec before setFields call and save it here?? Leaving tableName arg null as it seems irrelevant.	
        }
    }

    /**
     *
     * @return
     */
    public Pair<Integer, Integer> getUploadedRecStats() {
        return new Pair<>(uploadedRecs.getFirst().size(), uploadedRecs.getSecond().size());
    }

    /**
     *
     * @return
     */
    public Integer getUploadedRecTotalCount() {
        return uploadedRecs.getFirst().size() + uploadedRecs.getSecond().size();
    }

    /**
     *
     * @return
     */
    public List<UploadedRecordInfo> getAllUploadedRecords() {
        List<UploadedRecordInfo> all = new ArrayList<>(uploadedRecs.getFirst());
        all.addAll(uploadedRecs.getSecond());
        Collections.sort(all);
        return all;
    }
    /**
     * @param recId
     * @return true if fields other than the fields mapped in the wb are empty
     */
    protected boolean areUnmappedFldsEmpty(final Integer recId) {
    	if (recId == null) return true;
    	
    	List<String> fldConditions = new ArrayList<String>();
    	List<String> wbFlds = new ArrayList<String>();
    	//assume each 'seq' has the same fields
    	for (UploadField f : uploadFields.get(0)) {
    		if (f.getIndex() != -1) {
    			wbFlds.add(f.getField().getFieldInfo().getColumn());
    		}
    	}
    	for (DBFieldInfo f : getTable().getTableInfo().getFields()) {
    		System.out.println(f.getColumn());
    		//almost all fields (as opposed to getRelationships()) should be "user" fields?
    		if (wbFlds.indexOf(f.getColumn()) == -1) {
    			fldConditions.add(f.getColumn() + " is null ");
    		}
    	}
    	//and what about one-to-manies from this record? Trust in referential integrity?
    	String sql = "select " + getTable().getTableInfo().getPrimaryKeyName() + " from " + tblClass.getSimpleName().toLowerCase() + " where " + getTable().getTableInfo().getPrimaryKeyName() + " = " + recId;
    	for (String fc : fldConditions) {
    		sql += " and " + fc;
    	}
    	List<Object[]> r = BasicSQLUtils.query(sql);
    	return r.size() == 1;
    }
    
    protected void banishChild() {
    	//XXX isOkToDelete check??? 
    	//XXX recs in related tables that need to be deleted first ???
    	//XXX tracking deletes for feedback to user ???
    	if (areUnmappedFldsEmpty(exportedOneToManyId)) {
    		String sql = "delete from " + tblClass.getSimpleName().toLowerCase() + " where " + getTable().getTableInfo().getPrimaryKeyName() + " = " + exportedOneToManyId;
    		int r = BasicSQLUtils.update(sql);
    		System.out.println(r + " deleted (" + sql + ")");
    		log.info(r + " deleted (" + sql + ")");
    	}
    }
    /**
     * @param rec
     * 
     * Called after a write to update Match selection history.
     */
    protected void finishMatching(final DataModelObjBase rec) {
        if (restrictedValsForAddNewMatch != null) {
            matchSetting.addSelection(matchSetting.new MatchSelection(restrictedValsForAddNewMatch,
                    uploader.getRow(), rec.getId(), matchSetting.getMode()));
            restrictedValsForAddNewMatch = null;
        }
    }

    /**
     * @return all tables that precede this table in the Upload graph
     */
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
    protected boolean needToWrite(int recNum) throws UploaderException {
    	if (dataToWrite(recNum)) {
        	return true;
        }
    	if (tblClass.equals(CollectingEvent.class)
    			&& AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent()) {
    		return true;
    	}
        if (tblClass.equals(PaleoContext.class)
                && AppContextMgr.getInstance().getClassObject(Discipline.class).getIsPaleoContextEmbedded()) {
            return true;
        }
        for (UploadTable child : specialChildren) {
        	if (needToMatchChild(child.tblClass) && !child.isOneToOneChild()) {
        		child.loadFromDataSet(wbCurrentRow);
        		for (int c = 0; c < child.getUploadFields().size(); c++) {
        			if (child.getCurrentRecord(c) != null) {
        				return true;
        			}
        		}
        	}
        }
        if (parentTables.size() == 0) {
        	return false;
        }
    	for (Vector<ParentTableEntry> pts : parentTables) {
    		for (ParentTableEntry pt : pts) {
    			UploadTable parentTbl = pt.getImportTable();
    			boolean checkParent =  parentTbl instanceof UploadTableTree || parentTbl.isOneToOneChild();
    			if (!checkParent && pt.getParentRel() != null) {
    				if (pt.getParentRel().getRelType().startsWith("OneTo")) {
    					checkParent = !parentTbl.specialChildren.contains(this);    				
    				} else {
    					checkParent = true;
    				}
    			}
    			if (checkParent) {
    				try {
    					if (pt.getImportTable().getParentRecord(recNum, this) != null) {
    						return true;
    					}
    				} catch (Exception ex) {
    					throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
    				}
    			}
    		}
    	}
    	return false;
    }

    protected DataModelObjBase prepareRecForWriting(final DataProviderSessionIFace theSession, final DataModelObjBase rec)
            throws Exception {
        DataModelObjBase result = rec;
        if (updateMatches && (rec instanceof Accession)) {
            result =  theSession.merge(rec);
            result.forceLoad();
        }
        return result;
    }
    /**
     *
     * @param rec
     * @throws UploaderException
     */
    protected DataModelObjBase doWrite(DataModelObjBase rec) throws UploaderException
    {
    	Pair<DataProviderSessionIFace,Boolean> sessObj = getSession();
    	DataProviderSessionIFace theSession = sessObj.getFirst();
        boolean tblTransactionOpen = false;
		try
		{
			//DataModelObjBase mergedRec = rec;
			DataModelObjBase mergedRec = prepareRecForWriting(theSession, rec);

        	BusinessRulesIFace busRule = DBTableIdMgr.getInstance().getBusinessRule(tblClass);
        	if (busRule instanceof AttachmentOwnerBaseBusRules)
        	{
        		((AttachmentOwnerBaseBusRules )busRule).setProcessOwnersAndRefs(true);
        	}
            if (busRule != null)
            {
                busRule.beforeSave(mergedRec, theSession);
            }
            if (sessObj.getSecond()) {
            	theSession.beginTransaction();
            	tblTransactionOpen = true;
            }
            theSession.saveOrUpdate(mergedRec);
            if (busRule != null) {
                if (!busRule.beforeSaveCommit(mergedRec, theSession)) {
                    if (sessObj.getSecond()) {
                    	theSession.rollback();
                    	tblTransactionOpen = false;
                    }
                    throw new Exception("Business rules processing failed");
                }
            }
            if (sessObj.getSecond()) {
            	theSession.commit();
            	tblTransactionOpen = false;
            }
            if (busRule != null) {
                busRule.afterSaveCommit(mergedRec, theSession);
            }
            if (needToRefreshAfterWrite()) {
                theSession.refresh(mergedRec);
            }
            return mergedRec;
        }
        catch (Exception ex)
        {
        	if (tblTransactionOpen && sessObj.getSecond())
            {
            	theSession.rollback();
            }
            if (ex instanceof org.hibernate.exception.ConstraintViolationException)
            {
                throw new UploaderException(ex, UploaderException.ABORT_ROW);
            }
            throw new UploaderException(ex, UploaderException.ABORT_IMPORT);
        }
        finally
        {
            getRidOfSession(sessObj);
        }
    }

    /**
     * @return true if current record needs to be refreshed after writes.
     */
    public boolean needToRefreshAfterWrite()
    {
        //The refresh call slows performance hugely so only calling it when necessary.
        //This may be risky. At this time the refresh is required only because
        //of changes made in business rule processing of treeables.
        //But if business rules change...
        return false; 
    }
    
    /*
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
        deleteObjects(uploadedRecs.getSecond().iterator(), showProgress);
    }

    /**
     * @param row
     * @throws UploaderException
     * 
     * deletes all records uploaded for row.
     */
    public void abortRow(final int row) throws UploaderException {
    	UploadedRecordInfo arg1 = new UploadedRecordInfo(null, row, 0, null);
    	UploadedRecordInfo arg2 = new UploadedRecordInfo(null, row+1, 0, null);
    	SortedSet<UploadedRecordInfo> recsForRow = uploadedRecs.getSecond().subSet(arg1, arg2);
    	if (recsForRow.size() > 0) {
    		deleteObjects(recsForRow.iterator(), false);
        	uploadedRecs.getSecond().removeAll(recsForRow);
    	}
    }
    
    /**
     * @param session
     * @return sql delete statements necessary to delete uploaded records.
     */
    protected Vector<DeleteQuery> getQueriesForRawDeletes(final DataProviderSessionIFace session)
    {
        Vector<DeleteQuery> result = new Vector<DeleteQuery>();
        if (AttachmentOwnerIFace.class.isAssignableFrom(getTblClass()))
        {
        	//Attachments undo is now handled in Uploader.
        	
        	//weird relationships/annotations require extra work. Can't delete attachments
        	//until XXXAttachment records are deleted, but can't know what attachments to delete
        	//after XXXAttachment records are deleted. 
        	
        	//gets the ids of the attachments to delete.
//        	result.add(new DeleteQuery(session.createQuery("select attachmentid from " + 
//        			getWriteTable().getName().toLowerCase() + "attachment where " +
//        			getWriteTable().getName().toLowerCase() + "id =:theKey", true), false, -1));
        	
        	//deletes XXXAttachment records
//        	result.add(new DeleteQuery(
//        			session.createQuery("delete from " + getWriteTable().getName().toLowerCase() + "attachment where " +
//        					getWriteTable().getName().toLowerCase() + "id =:theKey", true), true, -1));
        	
        	//deletes attachments using results from first query above
//        	result.add(new DeleteQuery(
//        			session.createQuery("delete from attachment where attachmentid =:theKey", true), true, 0));
        }
   
        result.add(new DeleteQuery(
        		session.createQuery("delete from " + getWriteTable().getName().toLowerCase() + " where " 
                + getWriteTable().getTableInfo().getIdColumnName() + "=:theKey", true), true, -1));
        
        return result;
    }
    
    /**
     * @param objs - an iterator of object ids.
     * 
     * Deletes all the objects whose keys are present in objs.
     */
    protected void deleteObjects(Iterator<UploadedRecordInfo> objs, final boolean showProgress) throws UploaderException
    {
        log.debug("deleting from " + getWriteTable().getName());
        //This method is currently never called when tblSession is non null, 
        //so checks before transaction actions are not included
        Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
        DataProviderSessionIFace session = sessObj.getFirst();
        Vector<DeleteQuery> q;
        if (doRawDeletes)
        {
            q = getQueriesForRawDeletes(session);
        }
        else
        {
            q = new Vector<DeleteQuery>(1);
            String hql = "from " + getWriteTable().getName() + " where id =:theKey";
            q.add(new DeleteQuery(session.createQuery(hql, false), true, -1));
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
                        for (DeleteQuery qFace : q)
                        {
                            if (qFace.getKeyGeneratorIdx() == -1)
                            {
                            	qFace.getQuery().setParameter("theKey", key);
                            }
                        }
                        if (doRawDeletes)
                        {
                            HashMap<Integer, List<?>> subKeysMap = new HashMap<Integer, List<?>>();
                        	int qIdx = 0;
                            for (DeleteQuery qFace : q)
                            {
                                if (sessObj.getSecond()) {
                                	session.beginTransaction();
                                	opened = true;
                                }
                                if (qFace.isDeletes())
                                {
                                	if (qFace.getKeyGeneratorIdx() == -1)
                                	{
                                		qFace.getQuery().executeUpdate();
                                	}
                                	else
                                	{
                                		List<?> subKeys = subKeysMap.get(qFace.getKeyGeneratorIdx());
                                		{
                                			for (Object subKeyObj : subKeys)
                                			{
                                				qFace.getQuery().setParameter("theKey", subKeyObj);
                                				qFace.getQuery().executeUpdate();
                                			}
                                		}
                                	}
                                }
                                else
                                {
                                	subKeysMap.put(qIdx, qFace.getQuery().list());
                                }
                                if (sessObj.getSecond()) {
                                	session.commit();
                                    committed = true;
                                }
                                qIdx++;
                            }
                        }
                        else
                        {
                            DataModelObjBase obj = (DataModelObjBase) q.get(0).getQuery().uniqueResult();
                            if (obj != null)
                            {
                                if (sessObj.getSecond()) {
                                	session.beginTransaction();
                                	opened = true;
                                }
                                BusinessRulesIFace busRule = DBTableIdMgr.getInstance().getBusinessRule(tblClass);
                                if (busRule != null)
                                {
                                    obj = (DataModelObjBase)busRule.beforeDelete(obj, session);
                                }
                                session.delete(obj);
                                if (busRule != null)
                                {
                                    busRule.beforeDeleteCommit(obj, session);
                                }
                                if (sessObj.getSecond()) {
                                	session.commit();
                                	committed = true;
                                }
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
            getRidOfSession(sessObj);
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

    public Vector<Vector<String>> printUpload() throws InvocationTargetException,
            IllegalAccessException
    {
        Pair<DataProviderSessionIFace, Boolean> sessObj = getSession();
        DataProviderSessionIFace session = sessObj.getFirst();
        Vector<Method> getters = getGetters();
        Object[] args = new Object[0];
        Vector<Vector<String>> result = new Vector<Vector<String>>();
        try
        {
            String hql = "from " + tblClass.getSimpleName() + " obj where id=:theKey";
            QueryIFace qif = session.createQuery(hql, false);
            boolean wroteHeaders = false;
            for (Object key : uploadedRecs.getSecond())
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
            getRidOfSession(sessObj);
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
    protected String getRecordSetName(boolean showRecordSetInUI)
    {
        int maxNameLength = DBTableIdMgr.getInstance().getInfoByTableName("recordset").getFieldByColumnName("name").getLength();
        String rsName = getFullRecordSetName(showRecordSetInUI, maxNameLength);
        if (rsName.length() > maxNameLength)
        {
            //add as many pieces of the upload time as will fit...
            Calendar now = uploader.getUploadTime();
            String[] chunks = {"_" + String.valueOf(now.get(Calendar.YEAR)), "-" + String.valueOf(now.get(Calendar.MONTH) + 1),
                    "-" + String.valueOf(now.get(Calendar.DAY_OF_MONTH)), "_" + String.valueOf(now.get(Calendar.HOUR_OF_DAY)),
                    ":" + String.valueOf(now.get(Calendar.SECOND))}; 
            rsName = getShortRecordSetName();
            int c = 0;
            while (c < chunks.length && (rsName + chunks[c]).length() <= maxNameLength)
            {
                rsName += chunks[c++];
            }
        }
        return rsName;
    }


    /**
     * @return
     */
    protected String getFullRecordSetName(boolean showRecordSetInUI, int maxNameLength)
    {
        String tblName = showRecordSetInUI ? "" :
        	DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName()).getTitle() + "_";
        String uploadName = isUpdateMatches() && showRecordSetInUI ?
                suffixBeWBName(uploader.getWb().getSrcFilePath(), showRecordSetInUI, maxNameLength)
                : uploader.getIdentifier();
        return tblName + uploadName;
    }

    protected String crapOutOfBEWBName(final String beWbName) {
        String result = StringUtils.reverse(beWbName);
        result = result.substring(result.indexOf("-- ") + 3).trim();
        return StringUtils.reverse(result);
    }

    protected String suffixBeWBName(final String name, boolean showRecordSetInUI, int maxNameLength) {
        List<Object> names = BasicSQLUtils.querySingleCol("select name from recordset where `type` = "
                + (showRecordSetInUI ? RecordSet.GLOBAL : RecordSet.WB_UPLOAD)
                + " and name like '" + name + "%' order by name");
        Integer append = 1;
        while (names.indexOf(name + " " + append) != -1 && (name + " " + append).length() < maxNameLength) {
            append++;
        }
        int maxLen = DBTableIdMgr.getInstance().getInfoById(RecordSet.getClassTableId()).getFieldByName("Name").getLength();
        String coreName = name;
        if ((coreName + " " + append).length() > maxLen) {
            int diff = (coreName + " " + append).length() - 64;
            coreName = coreName.substring(0, coreName.length() - diff - 1);
        }
        return coreName + " " + append;
    }
    /**
     * @return
     */
    protected String getShortRecordSetName()
    {
    	return DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName()).getTitle();
    }

    /**
     * @return a recordset containing the the objects created during last upload.
     */
    public RecordSet getRecordSet(boolean showRecordSetInUI)
    {
        RecordSet result = new RecordSet();
        result.initialize();
        result.set(getRecordSetName(showRecordSetInUI), 
                   DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName()).getTableId(), 
                   showRecordSetInUI ? RecordSet.GLOBAL : RecordSet.WB_UPLOAD);
        result.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
        if (isUpdateMatches() && uploader.getRootTable() == this) {
            List<WorkbenchRow> rows = uploader.getWb().getWorkbenchRowsAsList();
            for (Integer r : uploader.getUploadedRows()) {
                result.addItem(rows.get(r).getRecordId());
            }
        } else {
            for (UploadedRecordInfo rec : getAllUploadedRecords()) {
                result.addItem(rec.getKey().intValue());
            }
        }
        return result;
    }

    /**
     * @return the matchChildren
     */
    public Vector<UploadTable> getSpecialChildren()
    {
        return specialChildren;
    }

    @Override
    public String toString()
    {
        String result = getTblTitle();
        if (tblClass.equals(Agent.class))
        {
            if (relationship != null)
            {
            	result += " (" + relationship.getRelatedField().getTable().getTableInfo().getTitle()
                    + ")";
            }
        }
        return result;
    }

    /**
     * @return title of the underlying specify 6 table
     */
    public String getTblTitle()
    {
        return DBTableIdMgr.getInstance().getByShortClassName(tblClass.getSimpleName()).getTitle();
    }
    
    public UploadMatchSetting getMatchSetting()
    {
        return matchSetting;
    }

    public class EditedRecNotUpdatedMsg extends BaseUploadMessage {
    	protected final DataModelObjBase rec;
    	protected final int row;
    	protected final UploadTable uploadTable;
    	
    	public EditedRecNotUpdatedMsg(final DataModelObjBase rec, final int row, final UploadTable uploadTable) {
    		super(null);
    		this.rec = rec;
    		this.row = row;
    		this.uploadTable = uploadTable;
    	}


		/* (non-Javadoc)
		 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.BaseUploadMessage#getRow()
		 */
		@Override
		public int getRow() {
			return this.row;
		}

		/* (non-Javadoc)
		 * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.BaseUploadMessage#toString()
		 */
		@Override
		public String toString() {
	    	Timestamp recStamp; 
	    	Agent editor;
	    	if (rec.getTimestampModified() == null) {
	    		recStamp = rec.getTimestampCreated();
	    		editor = rec.getCreatedByAgent();
	    	} else {
	    		recStamp = rec.getTimestampModified();
	    		editor = rec.getModifiedByAgent();
	    	}
	    	String editorName = DataObjFieldFormatMgr.getInstance().format(editor, Agent.class);
			return String.format(getResourceString("WB_UPLOAD_EDITED_REC_NOT_UPDATED"), row + 1, uploadTable.toString(), new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(recStamp), editorName);
		}
    	
    	
    }
    public class PartialMatchMsg extends BaseUploadMessage
    {
        protected String      matchVals;
        protected String      matchedText;
        protected int         row;
        protected UploadTable uploadTable;

        /**
         * @param cellVals
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
     * @author timo
     *
     *Class to store queries needed to undo uploads.
     *
     */
    protected class DeleteQuery
    {
    	protected final QueryIFace query;
    	protected final boolean deletes; //if true then the query is executed, else results are stored
    	protected final int keyGeneratorIdx; //if not -1 then the index of the query that generates
    	                                     //keys to be deleted
		/**
		 * @param query
		 * @param deletes
		 * @param keyGeneratorIdx
		 */
		public DeleteQuery(QueryIFace query, boolean deletes,
				int keyGeneratorIdx)
		{
			super();
			this.query = query;
			this.deletes = deletes;
			this.keyGeneratorIdx = keyGeneratorIdx;
		}
		/**
		 * @return the query
		 */
		public QueryIFace getQuery()
		{
			return query;
		}
		/**
		 * @return the deletes
		 */
		public boolean isDeletes()
		{
			return deletes;
		}
		/**
		 * @return the keyGeneratorIdx
		 */
		public int getKeyGeneratorIdx()
		{
			return keyGeneratorIdx;
		}
    	
    	
    }
    
    /**
     * @param cancelled
     * @param theSession
     * @throws UploaderException
     * 
     * cleans up and stuff?
     * Currently only used as a way of testing Tree updates.
     */
    public void finishUpload(boolean cancelled, DataProviderSessionIFace theSession) throws UploaderException {
        //nothing to do here.
    	if (updateMatches) {
    		disUseRecs(disUsedRecs, theSession);
    	}
    }
    
    /**
     * @throws UploaderException
     */
    public void finishUndoUpload() throws UploaderException
    {
    	//don't do nothin
    }
    
    /**
     * @throws UploaderException
     */
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

    /**
     *
     * @param toLog
     */
    private void logDebug(Object toLog) {
        if (debugging) {
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

	/**
	 * @return checkMatchInfo
	 */
	public boolean isCheckMatchInfo() 
	{
		return checkMatchInfo;
	}

	/**
	 * @param checkMatchInfo the checkMatchInfo
	 * 
	 */
	public void setCheckMatchInfo(boolean checkMatchInfo) 
	{
		//NOTE: this field needs to be set carefully. Dependencies in the upload graph
		//must be considered. In general, I think, if a table has checkMatchInfo true, then
		//all its ancestors in the upload graph should also have checkMatchInfo true.
		this.checkMatchInfo = checkMatchInfo;
	}

	/**
	 * @param matchRecordId the matchRecordId to set
	 */
	public void setMatchRecordId(boolean matchRecordId) 
	{
		this.matchRecordId = matchRecordId;
	}

	/**
	 * @return the updateMatches
	 */
	public boolean isUpdateMatches() 
	{
		return updateMatches;
	}

	/**
	 * @param updateMatches the updateMatches to set
	 */
	public void setUpdateMatches(boolean updateMatches) 
	{
		this.updateMatches = updateMatches;
	}

	/**
	 * @return the matchRecordId
	 */
	public boolean isMatchRecordId() 
	{
		return matchRecordId;
	}
    

    
}
