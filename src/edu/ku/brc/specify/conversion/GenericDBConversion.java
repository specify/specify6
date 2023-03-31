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
 * but WITHOUT ANY WARRANTcheckCrY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.specify.config.init.DataBuilder.createLithoStratTreeDef;
import static edu.ku.brc.specify.config.init.DataBuilder.createLithoStratTreeDefItem;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.addToValueMapper;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.buildSelectFieldList;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.clearValueMapper;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.copyTable;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.createFieldNameMap;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.deleteAllRecordsFromTable;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.escapeStringLiterals;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getCount;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getFieldMetaDataFromSchema;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getFieldMetaDataFromSchemaHash;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getFieldNamesFromSchema;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getHighestId;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getInsertedId;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getNumRecords;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getPartialDate;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getStrValue;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.hasIgnoreFields;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.removeForeignKeyConstraints;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.setFieldsToIgnoreWhenMappingIDs;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.setFieldsToIgnoreWhenMappingNames;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.setIdentityInsertOFFCommandForSQLServer;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.setIdentityInsertONCommandForSQLServer;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.setOneToOneIDHash;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.setShowErrors;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.setTblWriter;
import static edu.ku.brc.ui.UIRegistry.showError;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.contains;
import static org.apache.commons.lang.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.splitPreserveAllTokens;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.math.BigDecimal;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.BrowseBtnPanel;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.DisciplineType.STD_DISCIPLINES;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.config.init.DataBuilder;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttr;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef;
import edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.LithoStratTreeDef;
import edu.ku.brc.specify.datamodel.LithoStratTreeDefItem;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.Storage;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.StorageTreeDefItem;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.treeutils.TreeFactory;
import edu.ku.brc.specify.treeutils.TreeHelper;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

/**
 * This class is used for copying over the and creating all the tables that are not specify to any
 * one collection. This assumes that the "static" data members of DBConnection have been set up with
 * the new Database's driver, name, user and password. This is created with the old Database's
 * driver, name, user and password.
 * 
 * @author rods
 * 
 */
public class GenericDBConversion implements IdMapperIndexIncrementerIFace
{
    public enum TableType {
        CollectingEvent(0), CollectionObject(1), ExternalResource(2), Preparation(3);

        TableType(final int ord)
        {
            this.ord = (short)ord;
        }

        private short ord;

        public short getType()
        {
            return ord;
        }
    }
    
    public enum CollectionResultType { eOK, eCancel, eError }

    // public enum VISIBILITY_LEVEL {All, Institution}
    public static int                                       defaultVisibilityLevel = 0;                                                   // User/Security
                                                                                                                                            // changes

    protected static final Logger                           log                    = Logger.getLogger(GenericDBConversion.class);

    protected static StringBuilder                          strBuf                 = new StringBuilder("");

    protected static SimpleDateFormat                       dateTimeFormatter      = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static SimpleDateFormat                       dateFormatter          = new SimpleDateFormat("yyyy-MM-dd");
    protected static Timestamp                              now                    = new Timestamp(System .currentTimeMillis());
    protected static String                                 nowStr                 = dateTimeFormatter.format(now);

    protected String                                        oldDBName              = "";
    protected String                                        newDBName              = "";

    protected IdMapperMgr                                   idMapperMgr;

    protected Connection                                    oldDBConn;
    protected Connection                                    newDBConn;

    protected String[]                                      standardDataTypes      = { "Plant", "Animal", "Mineral", "Fungi",};
    protected Hashtable<String, Integer>                    dataTypeNameIndexes    = new Hashtable<String, Integer>();                   // Name to Index in Array
    protected Hashtable<String, Integer>                    dataTypeNameToIds      = new Hashtable<String, Integer>();                   // name  to Record ID
    
    protected Hashtable<Integer, Vector<Integer>>           catSeriesToNewCollectionID = new Hashtable<Integer, Vector<Integer>>();            

    protected Hashtable<String, TableStats>                 tableStatHash          = new Hashtable<String, TableStats>();

    // Helps during debugging
    protected static boolean                                shouldCreateMapTables  = true;
    protected static boolean                                shouldDeleteMapTables  = true;
    
    protected static boolean                                doDeleteAllMappings    = true;

    protected SpecifyAppContextMgr                          appContextMgr          = new SpecifyAppContextMgr();

    protected ProgressFrame                                 frame                  = null;
    protected boolean                                       hasFrame               = false;

    protected Hashtable<String, Integer>                    collectionHash         = new Hashtable<String, Integer>();
    protected Hashtable<String, String>                     prefixHash             = new Hashtable<String, String>();
    protected Hashtable<String, Integer>                    catNumSchemeHash       = new Hashtable<String, Integer>();
    
    protected Hashtable<STD_DISCIPLINES, Pair<Integer, Boolean>> dispToObjTypeHash = new Hashtable<STD_DISCIPLINES, Pair<Integer, Boolean>>();
    
    protected boolean                                       isSharingCollectingEvents = false;
    
    // New Multi-Collection data members
    protected Vector<CollectionInfo>                        collectionInfoList;
    protected Vector<CollectionInfo>                        collectionInfoShortList;
    protected HashMap<Integer, AutoNumberingScheme>         catSeriesToAutoNumSchemeHash = new HashMap<Integer, AutoNumberingScheme>();
    
    protected Hashtable<String, LithoStrat>                  lithoStratHash              = new Hashtable<String, LithoStrat>();


    protected Session                                       session;

    // Temp
    protected Agent                                         creatorAgent;
    protected Agent                                         modifierAgent;
    protected Hashtable<String, BasicSQLUtilsMapValueIFace> columnValueMapper      = new Hashtable<String, BasicSQLUtilsMapValueIFace>();
    protected Division                                      division = null;
    protected Integer                                       curDivisionID          = 0;
    
    protected Integer                                       curDisciplineID        = 0;
    protected Integer                                       curCollectionID        = 0;
    protected Integer                                       curAgentCreatorID      = 0;
    protected Integer                                       curAgentModifierID     = 0;
    protected Integer                                       colObjTypeID           = null;
    
    protected int                                           globalIdNumber         = 1;
    protected TaxonTypeHolder                               taxonTypeHolder        = null;
    protected DisciplineType                                disciplineType         = null;
    protected ConversionLogger                              convLogger             = null;
    protected SimpleDateFormat                              sdf                    = new SimpleDateFormat("yyyy-MM-dd");
    
    private Hashtable<Integer, Collection>                  collIdToCollObj        = new Hashtable<Integer, Collection>();
    
    // Old Agent Name Mapping for CreatedBy String
    private static final HashMap<String, Integer>           oldAgentHash          = new HashMap<String, Integer>();
    private static final HashSet<String>                    oldUnmappedAgentNames = new HashSet<String>();
    private static IdMapperIFace                            agentIdMapper         = null;



    /**
     * "Old" means the database you want to copy "from"
     * @param oldDriver old driver
     * @param oldDBName old database name
     * @param connectionStr old server name
     * @param oldUserName old user name
     * @param oldPassword old password
     */
    public GenericDBConversion(final Connection oldDBConn, 
                               final Connection newDBConn,
                               final String oldDBName,
                               final ConversionLogger convLogger)
    {
        this.oldDBConn = oldDBConn;
        this.newDBConn = newDBConn;
        this.convLogger = convLogger;
        
        this.idMapperMgr = IdMapperMgr.getInstance();
        
        loadAgentIdMappings();
        
        //                      0        1     2     3     4     5    6      7     8     9    10    11    
        int[]     objTypes  = {10,      14,   17,   18,   15,   9,    12,   13,   16,   19,    0,   0,   };
        boolean[] isEmbdded = {false, true,  true, true, true, true, true, true, true, true, true, true, };
        
        /*
            0            1            2            3          4     5       6       7       8       9            10      11            
         fish, herpetology, paleobotany, invertpaleo, vertpaleo, bird, mammal, insect, botany, invertebrate, minerals, fungi, 
         */
        for (STD_DISCIPLINES dt : STD_DISCIPLINES.values())
        {
            int i = dt.ordinal();
            dispToObjTypeHash.put(dt, new Pair<Integer, Boolean>(objTypes[i], isEmbdded[i]));
        }
   }
    
    /**
     * @return the CollectionObjectTypeID
     */
    public Integer findColObjTypeID()
    {
        Integer colObjTypeCnt  = getCount(oldDBConn, "SELECT COUNT(*) FROM collectionobject WHERE CollectionObjectTypeID > 8 AND CollectionObjectTypeID < 20 ");
        if (colObjTypeCnt != null && colObjTypeCnt > 0)
        {
            String sql = "SELECT CollectionObjectTypeID, CollectionObjectTypeName, Category FROM collectionobjecttype WHERE CollectionObjectTypeID in (SELECT DISTINCT CollectionObjectTypeID FROM collectionobject WHERE CollectionObjectTypeID > 8 AND CollectionObjectTypeID < 20)";
            Vector<TaxonTypeHolder> datas = new Vector<TaxonTypeHolder>();
            Vector<Object[]> rows = query(oldDBConn, sql); 
            for (Object[] row : rows)
            {
               TaxonTypeHolder tth = new TaxonTypeHolder(row);
               datas.add(tth);
            }
            
            TaxonTypeHolder selectedTTH = null;
            if (datas.size() > 1)
            {
                ToggleButtonChooserDlg<TaxonTypeHolder> dlg = new ToggleButtonChooserDlg<TaxonTypeHolder>((Frame)UIRegistry.getTopWindow(),
                        "Choose a Collection Object Type", 
                        datas, 
                        ToggleButtonChooserPanel.Type.RadioButton);
                dlg.setVisible(true);
                if (!dlg.isCancelled())
                {
                    selectedTTH = dlg.getSelectedObject();
                }
            } else if (datas.size() == 1)
            {
                selectedTTH = datas.get(0);
            }
            
            if (selectedTTH != null)
            {
                return selectedTTH.getId();
            }
        }
        return null;
    }
    
    /**
     * 
     */
    private void fixIdaho()
    {
        try
        {
            String prefix = "CIDA";
            
            HashSet<Integer> catSeriesOldIds    = new HashSet<Integer>();
            HashSet<Integer> catSeriesDefOldIds = new HashSet<Integer>();
            
            System.out.println(String.format("%11s %11s %11s %11s %11s %11s %16s %11s ", 
                    "CatSerDef", "CatSeries", "ColObjType", 
                    "TaxoNameId", "TaxonType", "TaxUnitType", "COT Name", "CO Cnt"));
            
            HashMap<String, ArrayList<CollectionInfo>> hashTaxonRootToCI = new HashMap<String, ArrayList<CollectionInfo>>();
            for (CollectionInfo ci : collectionInfoList)
            {
                System.out.print(String.format("%11d %11d %11d %11d %11d %11d %16s %11d ", 
                        ci.getCatSeriesDefId(), ci.getCatSeriesId(), ci.getColObjTypeId(), 
                        ci.getTaxonNameId(),    ci.getTaxonomyTypeId(), ci.getTaxonomicUnitTypeID(), 
                        ci.getColObjTypeName(), ci.getColObjCnt()));
                
                String key = String.format("%d,%d", ci.getTaxonNameId(), ci.getColObjTypeId());
                //System.out.print(key);
                
                ArrayList<CollectionInfo> list = hashTaxonRootToCI.get(key);
                if (list == null)
                {
                    list = new ArrayList<CollectionInfo>();
                    System.out.print(key);
                    hashTaxonRootToCI.put(key, list);
                }
                list.add(ci);
                System.out.println();
            }
            
            String dateStr = "2011-06-30 00:00:00";
            
            String sql1 = "INSERT INTO catalogseries (CatalogSeriesID, CollectionID, SeriesName, CatalogSeriesPrefix, Remarks, TimestampModified, TimestampCreated, LastEditedBy) VALUES (%d,%d,'%s','%s','%s','%s','%s','%s')";
            String sql2 = "INSERT INTO catalogseriesdefinition (CatalogSeriesDefinitionID, CatalogSeriesID, ObjectTypeID, Remarks,TimestampModified,TimestampCreated,LastEditedBy) VALUES (%d,%d,'%s','%s','%s','%s','%s')";
            String sql3 = "UPDATE collectionobjectcatalog SET CatalogSeriesID=%d WHERE CatalogSeriesID=%d AND CollectionObjectTypeID=%d";
            String sql4 = "SELECT COUNT(*) FROM collectionobjectcatalog WHERE CatalogSeriesID=%d AND CollectionObjectTypeID=%d";
            
            int catSeriesID    = 1;
            int catSeriesDefID = 1;
            
            HashMap<Integer, HashSet<Integer>> txRootToObjsIdHash = new HashMap<Integer, HashSet<Integer>>();
            
            for (String key : hashTaxonRootToCI.keySet())
            {
                ArrayList<CollectionInfo> ciList = hashTaxonRootToCI.get(key);
                
                int taxRootID = ciList.get(0).getTaxonNameId();
                
                System.out.println(String.format("\n-------------- For TaxonId: %d (%s) ---------------", ciList.get(0).getTaxonNameId(), ciList.get(0).getTaxonName()));
                for (CollectionInfo ci : ciList)
                {
                    System.out.println(String.format("    %s - %s", ci.getTaxonNameId(), ci.getTaxonName()));
                }
                System.out.println();
                
                int colObjTypeID = -1;
                StringBuilder combinedCatSeriesName = new StringBuilder();
                String        catSeriesName         = null;
                for (CollectionInfo ci : ciList)
                {
                    String sql = "SELECT CollectionObjectTypeName FROM collectionobjecttype WHERE CollectionObjectTypeID = " + ci.getColObjTypeId();
                    //System.out.println("sql: ["+sql+"]");
                    String objTypeName = BasicSQLUtils.querySingleObj(oldDBConn, sql);
                    //System.out.println("objTypeName: ["+objTypeName+"]");
                    if (combinedCatSeriesName.length() > 0) combinedCatSeriesName.append('|');
                    combinedCatSeriesName.append(objTypeName);
                    if (combinedCatSeriesName.length() > 50) combinedCatSeriesName.setLength(50);
                    
                    
                    catSeriesOldIds.add(ci.getCatSeriesId());
                    catSeriesDefOldIds.add(ci.getCatSeriesDefId());
                    
                    if (catSeriesName == null ||
                        (!containsIgnoreCase(catSeriesName, "paleo") && containsIgnoreCase(objTypeName, "paleo")))
                    {
                        catSeriesName  = objTypeName;
                        colObjTypeID   = ci.getColObjTypeId();
                    }
                }
                
                System.out.println("Combined Series Name: ["+combinedCatSeriesName+"]");
                System.out.println("New Cat Series Name:  ["+catSeriesName+"]");
                System.out.println("New Cat Series ID:    ["+catSeriesID+"]");
                System.out.println("New Cat Series Def ID:["+catSeriesDefID+"]");
                System.out.println("Col Obj Type:         ["+colObjTypeID+"]");
                
                String insert = String.format(sql1, catSeriesID, 0, catSeriesName, prefix, "", dateStr, dateStr, "conversion");
                System.out.println(insert);
                BasicSQLUtils.update(oldDBConn, insert);
                
                insert = String.format(sql2, catSeriesDefID, catSeriesID, colObjTypeID, "", dateStr, dateStr, "conversion");
                System.out.println(insert);
                BasicSQLUtils.update(oldDBConn, insert);
                
                HashSet<Integer> colObjIDHash = new HashSet<Integer>();
                for (CollectionInfo ci : ciList)
                {
                    int cnt = BasicSQLUtils.getCountAsInt(oldDBConn, String.format(sql4, ci.getCatSeriesId(), ci.getColObjTypeId()));
                    String updateStr = String.format(sql3, catSeriesID, ci.getCatSeriesId(), ci.getColObjTypeId());
                    System.out.println("Count: "+cnt +" -> "+updateStr);
                    BasicSQLUtils.update(oldDBConn, updateStr);
                    
                    colObjIDHash.add(ci.getColObjTypeId());
                    
                    String sql = "SELECT DISTINCT co2.CollectionObjectTypeID FROM collectionobject co1 INNER JOIN collectionobject co2 ON co1.CollectionObjectID = co2.DerivedFromID WHERE co1.CollectionObjectTypeID = " + ci.getColObjTypeId();
                    for (Integer cotId : BasicSQLUtils.queryForInts(oldDBConn, sql))
                    {
                        cnt = BasicSQLUtils.getCountAsInt(oldDBConn, String.format(sql4, ci.getCatSeriesId(), cotId));
                        updateStr = String.format(sql3, catSeriesID, ci.getCatSeriesId(), cotId);
                        System.out.println("Count: "+cnt +" -> "+updateStr);
                        BasicSQLUtils.update(oldDBConn, updateStr);
                        colObjIDHash.add(cotId);
                    }
                }
                
                txRootToObjsIdHash.put(taxRootID, colObjIDHash);
                
                catSeriesID++;
                catSeriesDefID++;
            }
            
            duplicateTaxonTree(6,  // Old TaxonomyTypeID (to be duplicated)
                               8,  // New TaxonomyTypeID 
                               3,  // Kingdom
                               17, // BiologicalObjectTypeID
                               "PaleoBotany"); //  17, 29
            
            duplicateTaxonTree(7,  // Old TaxonomyTypeID (to be duplicated)
                               9,  // New TaxonomyTypeID 
                               5,  // Kingdom
                               18, // BiologicalObjectTypeID 
                               "Invert Paleo");       //       18, 30  
            
            for (Integer id : catSeriesDefOldIds)
            {
                String s = "DELETE FROM catalogseriesdefinition WHERE CatalogSeriesDefinitionID = "+id;
                log.debug(s);
                BasicSQLUtils.update(oldDBConn, s);
            }
            
            for (Integer id : catSeriesOldIds)
            {
                String s = "DELETE FROM catalogseries WHERE CatalogSeriesID = "+id;
                log.debug(s);
                BasicSQLUtils.update(oldDBConn, s);
            }
            
            CollectionInfo.getCollectionInfoList().clear();
            collectionInfoList = CollectionInfo.getCollectionInfoList(oldDBConn, false);

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param oldTaxonomyTypeID
     * @param newTaxonomyTypeID
     * @param kingdom
     * @param curColObjTypeID
     * @param taxonTypeName
     */
    private void duplicateTaxonTree(final int    oldTaxonomyTypeID, 
                                    final int    newTaxonomyTypeID,
                                    final int    kingdom,
                                    final int    curColObjTypeID,
                                    final String taxonTypeName)
    {
        try
        {
            String dateStr = "2011-06-30 00:00:00";

            String postFix = Integer.toString(oldTaxonomyTypeID);
            
            System.out.println("----------------------------------------");
            System.out.println(String.format("Copying 'taxonomicunittype' Old TaxonomyTypeID %d to New TaxonomyTypeID %d\n", oldTaxonomyTypeID, newTaxonomyTypeID));
            IdMapperMgr.getInstance().setDBs(oldDBConn, newDBConn);
            TableDuplicator taxonomicunittypeDup = new TableDuplicator(oldDBConn, "taxonomicunittype", "TaxonomicUnitTypeID", "TaxonomyTypeID = "+oldTaxonomyTypeID, "RankID", postFix);
            taxonomicunittypeDup.initialize();
            taxonomicunittypeDup.duplicate();
            
            TableDuplicator taxonnameDup = new TableDuplicator(oldDBConn, "taxonname", "TaxonNameID", "TaxonomyTypeID = "+oldTaxonomyTypeID, "RankID", postFix);
            taxonnameDup.initialize();
            taxonnameDup.duplicate();
            
            String sql = String.format("INSERT INTO taxonomytype (TaxonomyTypeID, KingdomID, TaxonomyTypeName, TreeInfoUpToDate) VALUES(%d, %d, '%s', 1)", newTaxonomyTypeID, kingdom, taxonTypeName);
            System.out.println(sql);
            BasicSQLUtils.update(oldDBConn, sql);

            sql = String.format("UPDATE taxonomicunittype SET TaxonomyTypeID = %d WHERE TaxonomicUnitTypeID = ?", newTaxonomyTypeID);
            System.out.println(sql);
            PreparedStatement pStmt = oldDBConn.prepareStatement(sql);
            Statement         stmt  = oldDBConn.createStatement();
            
            sql = "SELECT NewID FROM "+taxonomicunittypeDup.getMapperName();
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                pStmt.setInt(1, rs.getInt(1));
                
                System.out.println(String.format("UPDATE taxonomicunittype SET TaxonomyTypeID = %d WHERE TaxonomicUnitTypeID = %d", newTaxonomyTypeID, rs.getInt(1)));
                pStmt.executeUpdate();
            }
            rs.close();
            pStmt.close();
            
            int cnt = 0;
            pStmt = oldDBConn.prepareStatement(String.format("UPDATE taxonname SET TaxonomyTypeID = %d WHERE TaxonNameID = ?", newTaxonomyTypeID));
            sql = "SELECT NewID FROM "+taxonnameDup.getMapperName();
            System.out.println(sql);
            rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                pStmt.setInt(1, rs.getInt(1));
                pStmt.executeUpdate();
                
                cnt++;
                if (cnt % 2000 == 0)
                {
                    System.out.println(cnt);
                    break;
                }
            }
            rs.close();
            pStmt.close();
            
            IdMapperIFace taxonMapper       = taxonnameDup.getMapper();
            IdMapperIFace taxUnitTypeMapper = taxonomicunittypeDup.getMapper();
            
            cnt = 0;
            pStmt = oldDBConn.prepareStatement("UPDATE taxonname SET ParentTaxonNameID=?, AcceptedID=?, TaxonomicUnitTypeID=? WHERE TaxonNameID = ?");
            rs = stmt.executeQuery("SELECT TaxonNameID, ParentTaxonNameID, AcceptedID, TaxonomicUnitTypeID FROM taxonname WHERE TaxonomyTypeID = "+newTaxonomyTypeID);
            while (rs.next())
            {
                int     taxonId  = rs.getInt(1);
                Integer parentId = rs.getInt(2);
                Integer acceptId = rs.getInt(3);
                Integer tutId    = rs.getInt(4);
                
                if (parentId == null || parentId == 0)
                {
                    pStmt.setObject(1, null);
                } else
                {
                    Integer newId = taxonMapper.get(parentId);
                    if (newId != null)
                    {
                        pStmt.setInt(1, newId);
                    } else
                    {
                        pStmt.setObject(1, null);
                    }
                }
                
                if (acceptId == null || acceptId == 0)
                {
                    pStmt.setObject(2, null);
                } else
                {
                    Integer newId = taxonMapper.get(acceptId);
                    if (newId != null)
                    {
                        pStmt.setInt(2, newId);
                    } else
                    {
                        pStmt.setObject(2, null);
                    }
                }
                
                pStmt.setInt(3, taxUnitTypeMapper.get(tutId));
                pStmt.setInt(4, taxonId);
                
                pStmt.executeUpdate();
                
                cnt++;
                if (cnt % 2000 == 0)
                {
                    System.out.println(cnt);
                    break;
                }
            }
            rs.close();
            pStmt.close();
            
            stmt.close();
            
            taxonomicunittypeDup.cleanup();
            taxonnameDup.cleanup();
            
            sql = String.format("SELECT CollectionTaxonomyTypesID FROM collectiontaxonomytypes WHERE BiologicalObjectTypeID = %d AND TaxonomyTypeID = %d", curColObjTypeID, oldTaxonomyTypeID);
            System.out.println(sql);
            int collectiontaxonomytypesID = BasicSQLUtils.getCountAsInt(oldDBConn, sql);
            
            sql = String.format("UPDATE collectiontaxonomytypes SET TaxonomyTypeID = %d WHERE CollectionTaxonomyTypesID = %d", newTaxonomyTypeID, collectiontaxonomytypesID);
            
            //sql = String.format("INSERT INTO collectiontaxonomytypes (CollectionTaxonomyTypesID,CollectionID,BiologicalObjectTypeID,TaxonomyTypeID,TimestampModified,TimestampCreated,LastEditedBy,DisplaySubSpecificTaxaLevelIndicators) " +
            //                    "VALUES(%d, %d, %d, %d, '%s', '%s', 'db', 1)", newColTaxTypeID, 0, newColObjTypeID, newTaxonomyTypeID, dateStr, dateStr);
            System.out.println(sql);
            BasicSQLUtils.update(oldDBConn, sql);
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @return
     */
    public CollectionResultType initialize()
    {

        collectionInfoList = CollectionInfo.getCollectionInfoList(oldDBConn, false);
        //fixIdaho();
        if (collectionInfoList == null)
        {
            if (CollectionInfo.isAskForFix())
            {
                if (ConvertTaxonHelper.fixTaxonomicUnitType(oldDBConn))
                {
                    collectionInfoList = CollectionInfo.getCollectionInfoList(oldDBConn, true);
                } else
                {
                    try
                    {
                        oldDBConn.close();
                    } catch (SQLException e) {}
                    System.exit(0);
                }
            } else
            {
                try
                {
                    oldDBConn.close();
                } catch (SQLException e) {}
                System.exit(0);
            }
        }
        
        collectionInfoShortList = CollectionInfo.getFilteredCollectionInfoList();
        
        if (collectionInfoList != null && collectionInfoList.size() > 0)
        {
            int paleoCnt = 0;
            
            // This is a Hash of TaxonObjectType to see how many collections use the same TaxonObjectType
            HashMap<Integer, HashSet<CollectionInfo>> taxonomyTypeHash = new HashMap<Integer, HashSet<CollectionInfo>>();
            
            // Get a List for each type of Paleo Collection, hashed by the Root Id
            HashMap<Integer, Vector<CollectionInfo>>                  paleoColInfoHash = new HashMap<Integer, Vector<CollectionInfo>>();
            HashMap<Integer, HashSet<DisciplineType.STD_DISCIPLINES>> paleoDispTypeHash = new HashMap<Integer, HashSet<DisciplineType.STD_DISCIPLINES>>();
            
            for (CollectionInfo colInfo : collectionInfoShortList)
            {
                // Tracks a 'set' of CollectionInfo objects for each TaxonomyTypeId
                HashSet<CollectionInfo> taxonomyTypeSet = taxonomyTypeHash.get(colInfo.getTaxonomyTypeId());
                if (taxonomyTypeSet == null)
                {
                    System.out.println("Creating TxTypeID: "+colInfo.getTaxonomyTypeId()+"  From "+colInfo.getCatSeriesName());
                    
                    taxonomyTypeSet = new HashSet<CollectionInfo>();
                    taxonomyTypeHash.put(colInfo.getTaxonomyTypeId(), taxonomyTypeSet);
                } else
                {
                    System.out.println("Adding TxTypeID: "+colInfo.getTaxonomyTypeId()+"  From "+colInfo.getCatSeriesName()+"  "+taxonomyTypeSet.size());
                }
                taxonomyTypeSet.add(colInfo);

                //---
                DisciplineType dType = getStandardDisciplineName(colInfo.getTaxonomyTypeName(),
                                                                 colInfo.getColObjTypeName(), 
                                                                 colInfo.getCatSeriesName());
                colInfo.setDisciplineTypeObj(dType);
                
                if (dType != null && dType.isPaleo())
                {
                    Vector<CollectionInfo> ciList = paleoColInfoHash.get(colInfo.getTaxonNameId());
                    if (ciList == null)
                    {
                        ciList = new Vector<CollectionInfo>();
                        paleoColInfoHash.put(colInfo.getTaxonNameId(), ciList);
                    }
                    ciList.add(colInfo);
                    
                    HashSet<DisciplineType.STD_DISCIPLINES> typeDispSet = paleoDispTypeHash.get(colInfo.getTaxonNameId());
                    if (typeDispSet == null)
                    {
                        typeDispSet = new HashSet<DisciplineType.STD_DISCIPLINES>();
                        paleoDispTypeHash.put(colInfo.getTaxonNameId(), typeDispSet);
                    }
                    typeDispSet.add(colInfo.getDisciplineTypeObj().getDisciplineType());
                    
                    paleoCnt++;
                }
                System.out.println("--------------------------------------");
                //System.out.println(colInfo.toString()+"\n");
            } // for loop
            
            int cnt = 0;
            StringBuilder msg = new StringBuilder();
            for (Integer taxonomyTypId : taxonomyTypeHash.keySet())
            {
                HashSet<CollectionInfo> taxonomyTypeSet = taxonomyTypeHash.get(taxonomyTypId);
                if (taxonomyTypeSet.size() > 1)
                {
                    msg.append(String.format("<html>TaxonomyTypeId %d has more than one Discpline/Collection:<br><OL>", taxonomyTypId));
                    for (CollectionInfo ci : taxonomyTypeSet)
                    {
                        msg.append(String.format("<LI>%s - %s - %s</LI>", ci.getCatSeriesName(), ci.getColObjTypeName(), ci.getTaxonomyTypeName()));
                    }
                    msg.append("</OL>");
                    cnt++;
                }
            }
            
            if (cnt > 0)
            {
                JOptionPane.showConfirmDialog(null, msg.toString(), "Taxomony Type Issues", JOptionPane.CLOSED_OPTION, JOptionPane.QUESTION_MESSAGE); 
            }
            
            // Will be zero for no Paleo collections
            if (paleoCnt > 1)
            {
                // Check to see if they all use the same tree
                if (paleoColInfoHash.size() > 1)
                {
                    msg.setLength(0);
                    // We get here when there is more than one Taxon Tree for the Paleo Collections
                    for (Integer treeId : paleoColInfoHash.keySet())
                    {
                        Vector<CollectionInfo> ciList  = paleoColInfoHash.get(treeId);
                        CollectionInfo         colInfo = ciList.get(0);
                        msg.append(String.format("The following collections use Taxon Tree '%s':\n", colInfo.getTaxonomyTypeName()));
                        for (CollectionInfo ci : paleoColInfoHash.get(treeId))
                        {
                            DisciplineType dType = getStandardDisciplineName(ci.getTaxonomyTypeName(), ci.getColObjTypeName(), ci.getCatSeriesName());
                            
                            String name = String.format("%s / %s / %s / %s / %s", ci.getCatSeriesPrefix(), ci.getCatSeriesName(), ci.getColObjTypeName(), ci.getTaxonomyTypeName(), dType.toString());
                            msg.append(name);
                            msg.append("\n");
                        }
                        msg.append("\n");
                    }
                    
                    JOptionPane.showConfirmDialog(null, msg.toString(), "Paleo Taxon Tree Issues", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE);
                        
                } else
                {
                    StringBuilder colNames = new StringBuilder();
                    for (Integer treeId : paleoColInfoHash.keySet())
                    {
                        for (CollectionInfo ci : paleoColInfoHash.get(treeId))
                        {
                            colNames.append("<LI>");
                            colNames.append(ci.getCatSeriesName());
                            colNames.append("</LI>");
                        }
                    }
                    
                    // You get here when all the Paleo Disciplines use the same tree
                    String msgStr = "<html>All the Paleo Collections need to use the same Taxon Tree and<br>therefore needs to be in the same discipline:<br><ol>";
                    JOptionPane.showConfirmDialog(null, msgStr + colNames.toString(), "Paleo Taxon Tree Issues", JOptionPane.CLOSED_OPTION, JOptionPane.QUESTION_MESSAGE);
                    
                    for (Integer treeId : paleoColInfoHash.keySet())
                    {
                        Vector<CollectionInfo> ciList  = paleoColInfoHash.get(treeId);
                        CollectionInfo         colInfo = ciList.get(0);
                        for (CollectionInfo ci : paleoColInfoHash.get(treeId))
                        {
                            ci.setDisciplineTypeObj(colInfo.getDisciplineTypeObj());
                        }
                    }
                }
                //
            }
            
            DefaultTableModel model = CollectionInfo.getCollectionInfoTableModel(false);
            if (model.getRowCount() > 1)
            {
                TableWriter colInfoTblWriter = convLogger.getWriter("colinfo.html", "Collection Info");
                
                colInfoTblWriter.startTable();
                colInfoTblWriter.logHdr(CollectionInfoModel.getHeaders());
                
                
                Object[] row = new Object[model.getColumnCount()];
                for (int r=0;r<model.getRowCount();r++)
                {
                    for (int i=0;i<model.getColumnCount();i++)
                    {
                        row[i] = model.getValueAt(r, i);
                    }
                    colInfoTblWriter.logObjRow(row);
                }
                colInfoTblWriter.endTable();
                colInfoTblWriter.println("<BR><h3>Collections to be Created.</h3>");
                colInfoTblWriter.startTable();
                colInfoTblWriter.logHdr(CollectionInfoModel.getHeaders());
                
                model = CollectionInfo.getCollectionInfoTableModel(true);
                row = new Object[model.getColumnCount()];
                for (int r=0;r<model.getRowCount();r++)
                {
                    for (int i=0;i<model.getColumnCount();i++)
                    {
                        row[i] = model.getValueAt(r, i);
                    }
                    colInfoTblWriter.logObjRow(row);
                }
                colInfoTblWriter.endTable();
                colInfoTblWriter.close();
                
                File file = new File(colInfoTblWriter.getFileName());
                if (file != null && file.exists())
                {
                    try
                    {
                        AttachmentUtils.openURI(file.toURI());
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
            
            for (CollectionInfo ci : CollectionInfo.getFilteredCollectionInfoList())
            {
                String sql = "select preparationmethod, ct.* from usyscollobjprepmeth pt inner join usysmetafieldsetsubtype st on st.fieldsetsubtypeid = pt.fieldsetsubtypeid " +
                "inner join collectionobjecttype ct1 on ct1.collectionobjecttypeid = st.fieldvalue " +
                "inner join collectionobjecttype ct on ct.collectionobjecttypename = replace(ct1.collectionobjecttypename, ' Preparation', '') " +
                "inner join catalogseriesdefinition csd on csd.objecttypeid = ct.collectionobjecttypeid " +
                "inner join catalogseries cs on cs.catalogseriesid = csd.catalogseriesid " +
                "WHERE csd.catalogseriesid = " + ci.getCatSeriesId();
                
                
                
                System.out.println("\n------------------");
                System.out.println(ci.getCatSeriesName());
                System.out.println(sql);
                System.out.println("------------------");
                
                int i = 0;
                Vector<Object[]> list = BasicSQLUtils.query(oldDBConn, sql);
                if (list.size() > 0)
                {
                    for (Object[] row : list)
                    {
                        System.out.print(i+" - ");
                        for (Object col: row)
                        {
                            System.out.print(col != null ? col.toString() : "null");
                            System.out.print(", ");
                        }
                        System.out.println();
                        i++;
                    }
                } else
                {
                    System.out.println("No Results");
                }
                
                
                
                sql = "select ct.*, (select relatedsubtypevalues from usysmetacontrol c " +
                "left join usysmetafieldsetsubtype fst on fst.fieldsetsubtypeid = c.fieldsetsubtypeid " +
                "where objectid = 10290 and ct.taxonomytypeid = c.relatedsubtypevalues) as DeterminationTaxonType " +
                "from collectiontaxonomytypes ct where ct.biologicalobjecttypeid = " + ci.getColObjTypeId();
                
                sql = String.format("SELECT CollectionTaxonomyTypesID, BiologicalObjectTypeID, CollectionObjectTypeName FROM (select ct.*, " +
                		"(SELECT distinct relatedsubtypevalues FROM usysmetacontrol c " +
                		"LEFT JOIN usysmetafieldsetsubtype fst ON fst.fieldsetsubtypeid = c.fieldsetsubtypeid " +
                		"WHERE objectid = 10290 AND ct.taxonomytypeid = c.relatedsubtypevalues) AS DeterminationTaxonType " +
                		"FROM collectiontaxonomytypes ct WHERE ct.biologicalobjecttypeid = %d) T1 " +
                		"INNER JOIN collectionobjecttype cot ON T1.biologicalobjecttypeid = cot.CollectionObjectTypeID", ci.getColObjTypeId());
                
                System.out.println("\n------------------");
                System.out.println(ci.getColObjTypeName());
                System.out.println(sql);
                System.out.println("------------------");
                
                i = 0;
                list = BasicSQLUtils.query(oldDBConn, sql);
                if (list.size() > 0)
                {
                    for (Object[] row : list)
                    {
                        System.out.print(i+" - ");
                        for (Object col: row)
                        {
                            System.out.print(col != null ? col.toString() : "null");
                            System.out.print(", ");
                        }
                        System.out.println();
                        i++;
                    }
                } else
                {
                    System.out.println("No Results");
                }
            }
            
            /*
            
            String sql = " select ct.*, (select relatedsubtypevalues from usysmetacontrol c " +
            		"left join usysmetafieldsetsubtype fst on fst.fieldsetsubtypeid = c.fieldsetsubtypeid " +
            		"where objectid = 10290 and ct.taxonomytypeid = c.relatedsubtypevalues) as DeterminationTaxonType " +
            		"from collectiontaxonomytypes ct where ct.biologicalobjecttypeid = 13";
            
            System.out.println("\n------------------");
            System.out.println("List of the taxonomytypes associated with a CollectionObjectTypeID");
            System.out.println(sql);
            System.out.println("------------------");

            int i = 0;
            Vector<Object[]> list = BasicSQLUtils.query(oldDBConn, sql);
            if (list.size() > 0)
            {
                for (Object[] row : list)
                {
                    System.out.print(i+" - ");
                    for (Object col: row)
                    {
                        System.out.print(col != null ? col.toString() : "null");
                        System.out.print(", ");
                    }
                    System.out.println();
                }
            } else
            {
                System.out.println("No Results");
            }*/
            
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p,2px,f:p:g,10px,p,2px,p:g,8px"));
            
            JTable tableTop = new JTable(CollectionInfo.getCollectionInfoTableModel(false));
            JTable tableBot = new JTable(CollectionInfo.getCollectionInfoTableModel(!CollectionInfo.DOING_ACCESSSION));
            
            int rows = 10;
            tableTop.setPreferredScrollableViewportSize(new Dimension( 
                    tableTop.getPreferredScrollableViewportSize().width, 
                    rows*tableTop.getRowHeight()));
            tableBot.setPreferredScrollableViewportSize(new Dimension( 
                    tableBot.getPreferredScrollableViewportSize().width, 
                    rows*tableBot.getRowHeight()));
            
            pb.add(UIHelper.createLabel("Available Specify 5 Taxononmic Types", SwingConstants.CENTER), cc.xy(1,1));
            pb.add(UIHelper.createScrollPane(tableTop), cc.xy(1,3));
            
            pb.add(UIHelper.createLabel("Specify 5 Collections to be Created", SwingConstants.CENTER), cc.xy(1,5));
            pb.add(UIHelper.createScrollPane(tableBot), cc.xy(1,7));
            
            pb.setDefaultDialogBorder();
            CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), "Taxononic Types", true, pb.getPanel());
            dlg.createUI();
           
            dlg.setSize(1024, 500);
            
            UIHelper.centerWindow(dlg);
            dlg.setAlwaysOnTop(true);
            dlg.setVisible(true);
            
            if (dlg.isCancelled())
            {
                return CollectionResultType.eCancel;
            }
            
            Pair<CollectionInfo, DisciplineType> pair = CollectionInfo.getDisciplineType(oldDBConn);
            if (pair == null || pair.second == null)
            {
                CollectionInfo colInfo = pair.first;
                disciplineType = getStandardDisciplineName(colInfo.getTaxonomyTypeName(), colInfo.getColObjTypeName(), colInfo.getCatSeriesName());
            } else
            {
                disciplineType = pair.second;
            }
            
            return disciplineType != null ? CollectionResultType.eOK : CollectionResultType.eError;
        }
        return CollectionResultType.eError;
    }
    
    /**
     * @return the frame
     */
    public ProgressFrame getFrame()
    {
        return frame;
    }

    /**
     * @param newDBConn the newDBConn to set
     */
    public void setNewDBConn(Connection newDBConn)
    {
        this.newDBConn = newDBConn;
    }

    public boolean isPaleo()
    {
        return disciplineType.isPaleo();
    }
    
    /**
     * 
     */
    public void shutdown()
    {
        try
        {
            oldDBConn.close();
            newDBConn.close();

        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * @return the colObjTypeID
     */
    public Integer getColObjTypeID()
    {
        return colObjTypeID;
    }

    /**
     * Sets a UI feedback frame.
     * @param frame the frame
     */
    public void setFrame(final ProgressFrame frame)
    {
        this.frame = frame;
        hasFrame = frame != null;

        BasicSQLUtils.setFrame(frame);

        if (idMapperMgr != null)
        {
            idMapperMgr.setFrame(frame);
        }
    }

    public void setOverall(final int min, final int max)
    {
        if (hasFrame)
        {
            frame.setOverall(min, max);
        }
    }

    public void setOverall(final int value)
    {
        if (hasFrame)
        {
            frame.setOverall(value);
        }
    }

    public void setProcess(final int min, final int max)
    {
        if (hasFrame)
        {
            frame.setProcess(min, max);
        }
    }

    public void setProcess(final int value)
    {
        if (hasFrame)
        {
            frame.setProcess(value);
        }
    }

    public void setDesc(final String text)
    {
        if (hasFrame)
        {
            frame.setDesc(text);
        }
    }

    /**
     * Return the SQL Connection to the Old Database
     * @return the SQL Connection to the Old Database
     */
    public Connection getOldDBConnection()
    {
        return oldDBConn;
    }

    /**
     * Return the SQL Connection to the New Database
     * @return the SQL Connection to the New Database
     */
    public Connection getNewDBConnection()
    {
        return newDBConn;
    }

    public void showStats()
    {
        for (Enumeration<TableStats> ts = tableStatHash.elements(); ts.hasMoreElements();)
        {
            ts.nextElement().compareStats();
        }
    }

    /**
     * 
     */
    public void doInitialize()
    {

        BasicSQLUtilsMapValueIFace collectionMemberIDValueMapper = getCollectionMemberIDValueMapper();
        BasicSQLUtilsMapValueIFace agentCreatorValueMapper       = getAgentCreatorValueMapper();
        BasicSQLUtilsMapValueIFace agentModiferValueMapper       = getAgentModiferValueMapper();
        BasicSQLUtilsMapValueIFace versionValueMapper            = getVersionValueMapper();
        BasicSQLUtilsMapValueIFace divisionValueMapper           = getDivisionValueMapper();

        columnValueMapper.put("CollectioMemberID",  collectionMemberIDValueMapper);
        columnValueMapper.put("CreatedByAgentID",   agentCreatorValueMapper);
        columnValueMapper.put("ModifiedByAgentID",  agentModiferValueMapper);
        columnValueMapper.put("Version",            versionValueMapper);
        columnValueMapper.put("DivisionID",         divisionValueMapper);

        /*String[] tableNames = { "locality", "accession", "accessionagents",
                "accessionauthorizations", "address", "agent", "agentaddress", "authors",
                "biologicalobjectattributes", "biologicalobjectrelation",
                "biologicalobjectrelationtype", "borrow", "borrowagents", "borrowmaterial",
                "borrowreturnmaterial", "borrowshipments", "catalogseries",
                "catalogseriesdefinition", "collectingevent", "collection", "collectionobject",
                "collectionobjectcatalog", "collectionobjectcitation", "collectionobjecttype",
                "collectiontaxonomytypes", "collectors", "collevent_verbdate", "deaccession",
                "deaccessionagents", "deaccessioncollectionobject", "determination",
                "determinationcitation", "exchangein", "exchangeout", "geography",
                "geologictimeboundary", "geologictimeperiod", "grouppersons", "habitat", "image",
                "imageagents", "imagecollectionobjects", "imagelocalities", "journal", "loan",
                "loanagents", "loanphysicalobject", "loanreturnphysicalobject", "locality",
                "localitycitation", "observation", "otheridentifier", "permit", "preparation",
                "project", "projectcollectionobjects", "raveproject", "referencework", "reports",
                "shipment", "sound", "soundeventstorage", "stratigraphy", "taxoncitation",
                "taxonname", "taxonomicunittype", "taxonomytype", "usysaccessionarole",
                "usysaccessionstatus", "usysaccessiontype", "usysactionhotkey", "usysautoreports",
                "usysbiologicalsex", "usysbiologicalstage", "usysborrowagenrole",
                "usyscatalognumber", "usyscollectinggrouppermittedto", "usyscollectingmethod",
                "usyscollectioncontainertype", "usyscollobjprepmeth", "usysdatadefinition",
                "usysdatemasktypes", "usysdeaccessiorole", "usysdeaccessiotype",
                "usysdefaultquerydef", "usysdeterminatconfidence", "usysdeterminatmethod",
                "usysdeterminattypestatusname", "usysdigir", "usysdigirconcept", "usysfieldtypes",
                "usysfulltextcatalog", "usysfulltextfield", "usysfulltextupdates",
                "usyshabitathabitattype", "usyslanguages", "usyslatlongmasktypes",
                "usyslatlongtype", "usysloanagentsrole", "usyslocalityelevationmethod",
                "usyslocalitygrouppermittedtovi", "usyslocalitylatlongmethod", "usysmetacontrol",
                "usysmetadefaultcontrol", "usysmetafieldset", "usysmetafieldsetsubtype",
                "usysmetainterface", "usysmetaobject", "usysmetarelationshipsubtyperule",
                "usysmetasubtypecontents", "usysmetasubtypefield",
                "usysobservatioobservationmetho", "usyspermittype", "usyspreparatiocontainertype",
                "usyspreparatiomedium", "usyspreparatiopreparationtype", "usysquery",
                "usysqueryinterfaces", "usyssecuritytypes", "usysshipmentshipmentmethod",
                "usysspecifyadmin", "usysspecifyfullaccessuser", "usysspecifyguest",
                "usysspecifylimitedaccessuser", "usysspecifymanager", "usysstatistics",
                "usystaxonnamegrouppermittedtov", "usystemprequired", "usysuserpreferences",
                "usysversion", "usyswebqueryform", "usyswebquerylog", "usyswebquerytemplate",
                "webadmin" };*/
    }

    /**
     * Creates backstop creator and modifer agents.
     */
    public void initializeAgentInfo(final boolean startFromScratch)
    {
        if (startFromScratch)
        {
            Hashtable<String, Agent> agentMap = new Hashtable<String, Agent>();
            creatorAgent = new Agent();
            creatorAgent.initialize();
            creatorAgent.setAgentType(Agent.PERSON);
            creatorAgent.setFirstName("DB");
            creatorAgent.setLastName("Creator");
            agentMap.put("Creator", creatorAgent);

            modifierAgent = new Agent();
            modifierAgent.initialize();
            modifierAgent.setAgentType(Agent.PERSON);
            modifierAgent.setFirstName("DB");
            modifierAgent.setLastName("Modifier");
            agentMap.put("Modifier", modifierAgent);

            setSession(HibernateUtil.getCurrentSession());
            startTx();
            persist(creatorAgent);
            persist(modifierAgent);
            commitTx();
            setSession(null);
            
        } else
        {
            DataProviderSessionIFace s = DataProviderFactory.getInstance().createSession();
            creatorAgent = s.getData(Agent.class, "lastName",  "Creator", DataProviderSessionIFace.CompareType.Equals);
            modifierAgent = s.getData(Agent.class, "lastName", "Modifier", DataProviderSessionIFace.CompareType.Equals);
            s.close(); // OK
        }

        curAgentCreatorID  = creatorAgent.getId();
        curAgentModifierID = modifierAgent.getId();

        /*
         * for (String tableName : tableNames) { try { Statement stmt = oldDBConn.createStatement();
         * ResultSet rs = stmt.executeQuery("select unique LastEditedBy from "+tableName);
         * 
         * while (rs.next()) { String modifierAgent = rs.getString(1); if
         * (isNotEmpty(editedBy)) { Agent agent = agentMap.get(editedBy); } }
         *  } catch (Exception ex) {
         *  } }
         */
    }

    /**
     * 
     */
    public void mapIds() throws SQLException
    {
        log.debug("mapIds()");
        
        boolean skipRestOfMappings = false;
        
        // These are the names as they occur in the old datamodel
        String[] tableNamesXXX = {
                "CollectionObjectType",
                "CollectionTaxonomyTypes",
        };
        
        // These are the names as they occur in the old datamodel
        String[] tableNames = {
                "Locality", 
                "Accession",
                // XXX "AccessionAgents",
                "AccessionAuthorizations",
                // "Address",
                // "Agent",
                // "AgentAddress",
                // XXX "Authors",
                "BiologicalObjectAttributes", // Turn back on when datamodel checked in
                "BiologicalObjectRelation",
                "BiologicalObjectRelationType",
                "Borrow",
                // XXX "BorrowAgents",
                "BorrowMaterial",
                "BorrowReturnMaterial",
                // "BorrowShipments",
                // "Collection",
                "CatalogSeriesDefinition",
                "CollectingEvent",
                // "Collection",
                //"CollectionObject",
                //"CollectionObjectCatalog",
                "CollectionObjectCitation",
                "CollectionObjectType",
                "CollectionTaxonomyTypes",
                "Collectors",
                "Deaccession",
                // XXX "DeaccessionAgents",
                "DeaccessionCollectionObject",
                "Determination",
                "DeterminationCitation",
                "ExchangeIn",
                "ExchangeOut",
                // "Geography",
                "GeologicTimeBoundary",
                // "GeologicTimePeriod",
                "GroupPersons",
                //"Habitat", // done as part of Taxon
                // XXX "ImageAgents",
                "ImageCollectionObjects", 
                "ImageLocalities",
                "Journal",
                //"Loan",
                // XXX "LoanAgents",
                //"LoanPhysicalObject", 
                "LoanReturnPhysicalObject", 
                "LocalityCitation",
                "Observation", 
                "OtherIdentifier",
                "Permit",
                "Preparation", 
                "Project", 
                "ProjectCollectionObjects", 
                "ReferenceWork", 
                "Shipment", 
                //"Sound",
                //"SoundEventStorage", 
                "Stratigraphy", }; // NOTE: the TAXON tables are done in ConvertTaxonHelper

        // shouldCreateMapTables = false;

        IdTableMapper idMapper = null;
        IdHashMapper.setEnableDelete(true);
        for (String tableName : tableNames)
        {
            idMapper = idMapperMgr.addTableMapper(tableName, tableName + "ID", doDeleteAllMappings);
            log.debug("mapIds() for table" + tableName);
            
            if (shouldCreateMapTables)
            {
                idMapper.mapAllIds();
            }
        }

        if (!skipRestOfMappings)
        {
            //---------------------------------
            // This mapping is used by Loans
            //---------------------------------
            idMapper = idMapperMgr.addTableMapper("Loan", "LoanID", "SELECT LoanID FROM loan WHERE Category = 0 ORDER BY LoanID", true);//doDeleteAllMappings);
            if (shouldCreateMapTables)
            {
                idMapper.mapAllIdsWithSQL();
            }
    
            //-------------------------------------
            // This mapping is used by Loans Preps
            //-------------------------------------
            idMapper = idMapperMgr.addTableMapper("LoanPhysicalObject", "LoanPhysicalObjectID", 
                    "SELECT LoanPhysicalObjectID FROM loanphysicalobject lpo INNER JOIN loan l ON l.LoanID = lpo.LoanID WHERE l.Category = 0 ORDER BY l.LoanID", doDeleteAllMappings);
            if (shouldCreateMapTables)
            {
                idMapper.mapAllIdsWithSQL();
            }
            
            // Gifts
            IdTableMapper giftPrepsIdMapper = new IdTableMapper("giftphysicalobject", 
                                                                "id", 
                                                                "SELECT LoanPhysicalObjectID FROM loanphysicalobject lpo INNER JOIN loan l ON l.LoanID = lpo.LoanID WHERE l.Category = 1 ORDER BY l.LoanID",
                                                                true, // doDelete
                                                                false); // check old DB
            idMapperMgr.addMapper(giftPrepsIdMapper);
            if (shouldCreateMapTables)
            {
                giftPrepsIdMapper.mapAllIdsWithSQL();
            }

    
            //--------------------------------------------
            // This mapping is used by Loans Return Preps
            //--------------------------------------------
            /*idMapper = idMapperMgr.addTableMapper("LoanReturnPhysicalObject", "LoanReturnPhysicalObjectID", 
                       "SELECT r.LoanReturnPhysicalObjectID FROM loanphysicalobject lpo INNER JOIN loan l ON l.LoanID = lpo.LoanID Left Join loanreturnphysicalobject AS r ON lpo.LoanPhysicalObjectID = r.LoanPhysicalObjectID WHERE l.Category = 0 AND r.LoanReturnPhysicalObjectID IS NOT NULL ORDER BY l.LoanID", doDeleteAllMappings);
            if (shouldCreateMapTables)
            {
                idMapper.mapAllIdsWithSQL();
            }*/
    
            //---------------------------------
            // Map all the Logical IDs
            //---------------------------------
            idMapper = idMapperMgr.addTableMapper("collectionobjectcatalog", "CollectionObjectCatalogID", doDeleteAllMappings);
            if (shouldCreateMapTables)
            {
                idMapper.mapAllIds("select CollectionObjectID from collectionobject Where collectionobject.DerivedFromID Is Null order by CollectionObjectID");
            }
    
            //---------------------------------
            // Map all the Physical IDs
            //---------------------------------
            idMapper = idMapperMgr.addTableMapper("collectionobject", "CollectionObjectID", doDeleteAllMappings);
            if (shouldCreateMapTables)
            {
                idMapper.mapAllIds("select CollectionObjectID From collectionobject co WHERE co.CollectionObjectTypeID > 20 ORDER BY CollectionObjectID");
            }

            // meg commented out because it was blowing away map table created above
            // // Map all the Physical IDs
            // idMapper = idMapperMgr.addTableMapper("preparation", "PreparationID");
            // if (shouldCreateMapTables)
            // {
            // idMapper.mapAllIds("select CollectionObjectID from collectionobject Where not
            // (collectionobject.DerivedFromID Is Null) order by CollectionObjectID");
            // }
    
            // Map all the Physical IDs
            // Meg working copy, completely commented out.
            //        idMapper = idMapperMgr.addTableMapper("geography", "GeographyID");
            //        if (shouldCreateMapTables)
            //        {
            //            idMapper.mapAllIds("SELECT DISTINCT GeographyID,ContinentOrOcean,Country,State,County" +
            //                  "FROM demo_fish2.geography" +
            //                  "WHERE( (ContinentOrOcean IS NOT NULL) OR (Country IS NOT NULL) OR (State IS NOT NULL) OR (County IS NOT NULL) )" +
            //                  "AND ( (IslandGroup IS NULL) AND (Island IS NULL) AND (WaterBody IS NULL) AND (Drainage IS NULL) ) " +
            //                  "GROUP BY ContinentOrOcean,Country,State,County" );
            //        }
            
            // idMapper = idMapperMgr.addTableMapper("geography", "GeographyID");
            // if (shouldCreateMapTables)
            // {
            // idMapper.mapAllIds("SELECT DISTINCT GeographyID,ContinentOrOcean,Country,State,County" +
            // "FROM demo_fish2.geography" +
            // "WHERE( (ContinentOrOcean IS NOT NULL) OR (Country IS NOT NULL) OR (State IS NOT NULL) OR
            // (County IS NOT NULL) )" +
            // "AND ( (IslandGroup IS NULL) AND (Island IS NULL) AND (WaterBody IS NULL) AND (Drainage
            // IS NULL) ) " +
            // "GROUP BY ContinentOrOcean,Country,State,County" );
            // }
    
            // Meg had to added conditional for Current field
            String oldDetermination_Current      = "Current";
            String oldDetermination_CurrentValue = "1";
    
            /*if (BasicSQLUtils.mySourceServerType == BasicSQLUtils.SERVERTYPE.MySQL)
            {
                oldDetermination_Current = "IsCurrent";
                oldDetermination_CurrentValue = "-1";
            }*/
            // Map all the CollectionObject to its TaxonomyType
            // Meg had to add taxonname.TaxonomyTypeID to the GroupBy clause for SQL Server, ugh.
            // IdHashMapper idHashMapper = idMapperMgr.addHashMapper("ColObjCatToTaxonType", "Select
            // collectionobjectcatalog.CollectionObjectCatalogID, taxonname.TaxonomyTypeID From
            // collectionobjectcatalog Inner Join determination ON determination.BiologicalObjectID =
            // collectionobjectcatalog.CollectionObjectCatalogID Inner Join taxonname ON
            // taxonname.TaxonNameID = determination.TaxonNameID Where determination.IsCurrent = '-1'
            // group by collectionobjectcatalog.CollectionObjectCatalogID, taxonname.TaxonomyTypeID");
            IdHashMapper idHashMapper = idMapperMgr.addHashMapper(
                            "ColObjCatToTaxonType",
                            "Select collectionobjectcatalog.CollectionObjectCatalogID, taxonname.TaxonomyTypeID From collectionobjectcatalog " +
                            "Inner Join determination ON determination.BiologicalObjectID = collectionobjectcatalog.CollectionObjectCatalogID " +
                            "Inner Join taxonname ON taxonname.TaxonNameID = determination.TaxonNameID Where determination."
                                    + oldDetermination_Current
                                    + " = '"
                                    + oldDetermination_CurrentValue
                                    + "'  group by collectionobjectcatalog.CollectionObjectCatalogID, taxonname.TaxonomyTypeID", doDeleteAllMappings);
    
            if (shouldCreateMapTables)
            {
                idHashMapper.mapAllIds();
                log.info("colObjTaxonMapper: " + idHashMapper.size());
    
            }
        }
        
        // When you run in to this table1.field, go to that table2 and look up the id
        String[] mappings = {
                // "DeaccessionCollectionObject", "DeaccessionCollectionObjectID",
                // "DeaccessionPreparation", "DeaccessionPreparationID",
                // "LoanReturnPhysicalObject", "LoanReturnPhysicalObjectID",
                // "LoanReturnPreparation", "LoanReturnPreparationID",

                "BorrowReturnMaterial",
                "BorrowMaterialID",
                "BorrowMaterial",
                "BorrowMaterialID",
                
                // XXX "BorrowReturnMaterial", "ReturnedByID", "Agent", "AgentID",
                // XXX "Preparation", "PreparedByID", "Agent", "AgentID",
                
/*                "Preparation",
                "ParasiteTaxonNameID",
                "TaxonName",
                "TaxonNameID",*/

                "LoanPhysicalObject",
                "PhysicalObjectID",
                "CollectionObject",
                "CollectionObjectID",
                
                "LoanPhysicalObject",
                "LoanID",
                "Loan",
                "LoanID",

                // XXX "ExchangeIn", "ReceivedFromOrganizationID", "Agent", "AgentID",
                // XXX "ExchangeIn", "CatalogedByID", "Agent", "AgentID",
                // XXX "Collection", "OrganizationID", "Agent", "AgentID",
                // XXX "GroupPersons", "GroupID", "Agent", "AgentID",
                // XXX "GroupPersons", "MemberID", "Agent", "AgentID",

                // ??? "ExchangeOut", "CollectionID", "Collection", "CollectionID",
                // XXX "ExchangeOut", "SentToOrganizationID", "Agent", "AgentID",
                // XXX "ExchangeOut", "CatalogedByID", "Agent", "AgentID",
                "ExchangeOut",
                "ShipmentID",
                "Shipment",
                "ShipmentID",

                "ReferenceWork",
                "JournalID",
                "Journal",
                "JournalID",
                
                "ReferenceWork",
                "ContainingReferenceWorkID",
                "ReferenceWork",
                "ReferenceWorkID",

                // "ImageAgents", "ImageID", "Image", "ImageID",
                // "ImageAgents", "AgentID", "Agent", "AgentID",

                "BiologicalObjectRelation",
                "BiologicalObjectID",
                "CollectionObject",
                "CollectionObjectID",
                
                "BiologicalObjectRelation",
                "RelatedBiologicalObjectID",
                "CollectionObject",
                "CollectionObjectID",
                
                "BiologicalObjectRelation",
                "BiologicalObjectRelationTypeID",
                "BiologicalObjectRelationType",
                "BiologicalObjectRelationTypeID",

                // XXX "Shipment", "ShipperID", "AgentAddress", "AgentAddressID",
                // XXX "Shipment", "ShippedToID", "AgentAddress", "AgentAddressID",
                // XXX "Shipment", "ShippedByID", "Agent", "AgentID",
                // "Shipment", "ShipmentMethodID", "ShipmentMethod", "ShipmentMethodID",

/*                "Habitat",
                "HostTaxonID",
                "TaxonName",
                "TaxonNameID",*/

                // XXX "Authors", "AgentID", "Agent", "AgentID",
                "Authors",
                "ReferenceWorkID",
                "ReferenceWork",
                "ReferenceWorkID",
                
                "BorrowMaterial",
                "BorrowID",
                "Borrow",
                "BorrowID",
                
                "BorrowAgents",
                "BorrowID",
                "Borrow",
                "BorrowID",
                // XXX "BorrowAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",

                "DeaccessionCollectionObject",
                "DeaccessionID",
                "Deaccession",
                "DeaccessionID",
                
                "DeaccessionCollectionObject",
                "CollectionObjectID",
                "Preparation",
                "PreparationID",

                // "DeaccessionCollectionObject", "DeaccessionID", "Deaccession", "DeaccessionID",
                // // not sure this is needed
                // "DeaccessionCollectionObject", "CollectionObjectID",
                // "DeaccessionCollectionObject", "CollectionObjectID", // not sure this is needed

                "CollectionObjectCitation",
                "ReferenceWorkID",
                "ReferenceWork",
                "ReferenceWorkID",
                
                "CollectionObjectCitation",
                "BiologicalObjectID",
                "CollectionObjectCatalog",
                "CollectionObjectCatalogID",
                
                "CollectingEvent",
                "HabitatAttributeID",
                "Habitat",
                "HabitatID",
                
                "CollectingEvent",
                "LocalityID",
                "Locality",
                "LocalityID",
                
                "CollectingEvent",
                "StratigraphyID",
                "Stratigraphy",
                "StratigraphyID",

                "Collectors",
                "CollectingEventID",
                "CollectingEvent",
                "CollectingEventID",
                // XXX "Collectors", "AgentID", "Agent", "AgentID",

                // XXX "Permit", "IssuerID", "AgentAddress", "AgentAddressID",
                // XXX "Permit", "IssueeID", "AgentAddress", "AgentAddressID",
                // XXX "Sound", "RecordedByID", "Agent", "AgentID",
                
                // done in ConvertTaxonHelper
                //"TaxonCitation", 
                //"ReferenceWorkID",
                //"ReferenceWork",
                //"ReferenceWorkID",
                
                //"TaxonCitation",
                //"TaxonNameID",
                //"TaxonName",
                //"TaxonNameID",
                
                // XXX "Determination", "DeterminerID", "Agent", "AgentID",
                
/*                "Determination",
                "TaxonNameID",
                "TaxonName",
                "TaxonNameID",*/
                
                "Determination",
                "BiologicalObjectID",
                "CollectionObjectCatalog",
                "CollectionObjectCatalogID",

                // ??? "GeologicTimePeriod", "UpperBoundaryID", "UpperBoundary", "UpperBoundaryID",
                // ??? "GeologicTimePeriod", "LowerBoundaryID", "LowerBoundary", "LowerBoundaryID",

                // ************************************************************************************
                // NOTE: Since we are mapping CollectionObjectType to CatalogSeriesDefinition
                // then we might as well map CollectionObjectTypeID to CatalogSeriesDefinitionID
                //
                // The Combination of the CatalogSeriesDefinition and CollectionObjectType become
                // the
                // new Discipline
                // As you might expect the CatalogSeriesDefinitionID is mapped to the new
                // Discipline
                // ************************************************************************************
                "CollectionObjectType",  "CollectionObjectTypeID", "CatalogSeriesDefinition", "CatalogSeriesDefinitionID",
                
                "CollectionObject", "CollectionObjectTypeID", "CatalogSeriesDefinition", "CatalogSeriesDefinitionID",

                "CollectionObject", "CollectingEventID", "CollectingEvent", "CollectingEventID",

                "CollectionObject", "AccessionID", "Accession", "AccessionID",
                // XXX "CollectionObject", "CatalogerID", "Agent", "AgentID",
                "Loan", "ShipmentID", "Shipment", "ShipmentID",
                "AccessionAuthorizations", "AccessionID", "Accession", "AccessionID",
                "AccessionAuthorizations", "PermitID", "Permit", "PermitID",
                "AccessionAgents", "AccessionID", "Accession", "AccessionID",
                // XXX "AccessionAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
                "DeterminationCitation", "ReferenceWorkID", "ReferenceWork", "ReferenceWorkID",
                "DeterminationCitation", "DeterminationID", "Determination", "DeterminationID",
                "OtherIdentifier", "CollectionObjectID", "CollectionObjectCatalog", "CollectionObjectCatalogID",

                // XXX "Agent", "ParentOrganizationID", "Agent", "AgentID",
                // XXX "AgentAddress", "AddressID", "Address", "AddressID",
                // XXX "AgentAddress", "AgentID", "Agent", "AgentID",
                // XXX "AgentAddress", "OrganizationID", "Agent", "AgentID",

                "LocalityCitation", "ReferenceWorkID", "ReferenceWork",  "ReferenceWorkID",
                "LocalityCitation", "LocalityID", "Locality", "LocalityID",
                "LoanReturnPhysicalObject", "LoanPhysicalObjectID", "LoanPhysicalObject", "LoanPhysicalObjectID",
                // XXX "LoanReturnPhysicalObject", "ReceivedByID", "Agent", "AgentID",
                "LoanReturnPhysicalObject", "DeaccessionPhysicalObjectID", "CollectionObject", "CollectionObjectID",
                "DeaccessionAgents", "DeaccessionID", "Deaccession", "DeaccessionID",
                // XXX "DeaccessionAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
                "ProjectCollectionObjects", "ProjectID", "Project", "ProjectID",
                "ProjectCollectionObjects", "CollectionObjectID", "CollectionObject", "CollectionObjectID",
                // XXX "Project", "ProjectAgentID", "Agent", "AgentID",
                "LoanAgents", "LoanID", "Loan", "LoanID",
                // XXX "LoanAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",

                // done in ConvertTaxonHelper
                // taxonname ID mappings
                //"TaxonName", "ParentTaxonNameID", "TaxonName", "TaxonNameID", "TaxonName",
                //"TaxonomicUnitTypeID", "TaxonomicUnitType", "TaxonomicUnitTypeID", "TaxonName",
                //"TaxonomyTypeID", "TaxonomyType", "TaxonomyTypeID", "TaxonName", "AcceptedID",
                //"TaxonName", "TaxonNameID",

                // taxonomytype ID mappings
                // NONE

                // taxonomicunittype ID mappings
                //"TaxonomicUnitType", "TaxonomyTypeID", "TaxonomyType", "TaxonomyTypeID" 
                };

        for (int i = 0; i < mappings.length; i += 4)
        {
            idMapperMgr.mapForeignKey(mappings[i], mappings[i + 1], mappings[i + 2], mappings[i + 3]);
        }
    }

    /**
     * @throws SQLException
     */
    public void mapAgentRelatedIds()
    {
        log.debug("mapAgentRelatedIds()");

        // These are the names as they occur in the old datamodel
        String[] tableNames = { "AccessionAgents",
                             // "Address",
                             // "Agent",
                             // "AgentAddress",
                                "Authors", 
                                "BorrowAgents", 
                                "DeaccessionAgents",
                                "ImageAgents"};

        // shouldCreateMapTables = false;

        IdTableMapper idMapper = null;
        for (String tableName : tableNames)
        {
            idMapper = idMapperMgr.addTableMapper(tableName, tableName + "ID");
            log.debug("mapIds() for table" + tableName);
            if (shouldCreateMapTables)
            {
                idMapper.mapAllIds();
            }
        }
        
        IdTableMapper loanAgentsMapper = idMapperMgr.addTableMapper("loanagents", "LoanAgentsID", 
                "SELECT loanagents.LoanAgentsID FROM loanagents INNER JOIN loan ON loanagents.LoanID = loan.LoanID ORDER BY loan.LoanID", true);
        if (shouldCreateMapTables)
        {
            loanAgentsMapper.mapAllIdsWithSQL();
        }

        // When you run in to this table1.field, go to that table2 and look up the id
        String[] mappings = { 
                "BorrowReturnMaterial", "ReturnedByID", 
                "Agent",        "AgentID",
                
                "Preparation",  "PreparedByID", 
                "Agent",        "AgentID", 
                
                "ExchangeIn",   "ReceivedFromOrganizationID", 
                "Agent",        "AgentID", 
                
                "ExchangeIn",   "CatalogedByID",
                "Agent",        "AgentID", 
                
                "Collection",   "OrganizationID",
                "Agent",        "AgentID",
                
                "GroupPersons", "GroupID", 
                "Agent",        "AgentID", 
                
                "GroupPersons", "MemberID", 
                "Agent",        "AgentID",      
                
                "ExchangeOut",  "SentToOrganizationID", 
                "Agent",        "AgentID",
                
                "ExchangeOut",  "CatalogedByID", 
                "Agent",        "AgentID", 
                
                "ExchangeOut",  "ShipmentID",
                "Shipment",     "ShipmentID", 
                
                "Shipment",     "ShipperID", 
                "AgentAddress", "AgentAddressID",  
                
                "Shipment",     "ShippedToID", 
                "AgentAddress", "AgentAddressID",
                
                "Shipment",     "ShippedByID", 
                "Agent",        "AgentID", 
                
                "Authors",      "AgentID", 
                "Agent",        "AgentID",
                
                "BorrowAgents", "AgentAddressID", 
                "AgentAddress", "AgentAddressID",
                
                "Collectors",   "AgentID", 
                "Agent",        "AgentID", 
                
                "Permit",       "IssuerID", 
                "AgentAddress", "AgentAddressID", 
                
                "Permit",       "IssueeID", 
                "AgentAddress", "AgentAddressID", 
                
                //"Sound",        "RecordedByID", 
                //"Agent",        "AgentID", 
                
                "Determination", "DeterminerID", 
                "Agent",         "AgentID", 
                
                "CollectionObject", "CatalogerID", 
                "Agent",         "AgentID",
                
                "AccessionAgents", "AgentAddressID", 
                "AgentAddress",    "AgentAddressID", 
                
                "Agent",         "ParentOrganizationID", 
                "Agent",         "AgentID", 
                
                "AgentAddress",  "AddressID", 
                "Address",       "AddressID", 
                
                "AgentAddress",  "AgentID", 
                "Agent",         "AgentID", 
                
                "AgentAddress",  "OrganizationID", 
                "Agent",         "AgentID", 
                
                "LoanReturnPhysicalObject", "ReceivedByID",
                "Agent",         "AgentID", 
                
                "DeaccessionAgents", "AgentAddressID", 
                "AgentAddress",  "AgentAddressID", 
                
                "Project",       "ProjectAgentID", 
                "Agent",         "AgentID", 
                
                "LoanAgents",    "AgentAddressID", 
                "AgentAddress",  "AgentAddressID",
                
                //"CollectionObject", "ReferenceWorkID", 
                //"ReferenceWork",  "ReferenceWorkID",
                
                

        // XXX "BorrowReturnMaterial", "ReturnedByID", "Agent", "AgentID",
        // XXX "Preparation", "PreparedByID", "Agent", "AgentID",
        // XXX "ExchangeIn", "ReceivedFromOrganizationID", "Agent", "AgentID",
        // XXX "ExchangeIn", "CatalogedByID", "Agent", "AgentID",
        // XXX "Collection", "OrganizationID", "Agent", "AgentID",
        // XXX "GroupPersons", "GroupID", "Agent", "AgentID",
        // XXX "GroupPersons", "MemberID", "Agent", "AgentID",
        // XXX "ExchangeOut", "SentToOrganizationID", "Agent", "AgentID",
        // XXX "ExchangeOut", "CatalogedByID", "Agent", "AgentID",
        // XXX "Shipment", "ShipperID", "AgentAddress", "AgentAddressID",
        // XXX "Shipment", "ShippedToID", "AgentAddress", "AgentAddressID",
        // XXX "Shipment", "ShippedByID", "Agent", "AgentID",
        // XXX "Authors", "AgentID", "Agent", "AgentID",
        // XXX "BorrowAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
        // XXX "Collectors", "AgentID", "Agent", "AgentID",
        // XXX "Permit", "IssuerID", "AgentAddress", "AgentAddressID",
        // XXX "Permit", "IssueeID", "AgentAddress", "AgentAddressID",
        // XXX "Sound", "RecordedByID", "Agent", "AgentID",
        // XXX "Determination", "DeterminerID", "Agent", "AgentID",
        // XXX "Determination", "TaxonNameID", "TaxonName", "TaxonNameID",
        // XXX "Determination", "BiologicalObjectID", "CollectionObject", "CollectionObjectID",
        // XXX "AccessionAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
        // XXX "Agent", "ParentOrganizationID", "Agent", "AgentID",
        // XXX "AgentAddress", "AddressID", "Address", "AddressID",
        // XXX "AgentAddress", "AgentID", "Agent", "AgentID",
        // XXX "AgentAddress", "OrganizationID", "Agent", "AgentID",
        // XXX "LoanReturnPhysicalObject", "ReceivedByID", "Agent", "AgentID",
        // XXX "DeaccessionAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
        // XXX "Project", "ProjectAgentID", "Agent", "AgentID",
        // XXX "LoanAgents", "AgentAddressID", "AgentAddress", "AgentAddressID",
        };

        for (int i = 0; i < mappings.length; i += 4)
        {
            idMapperMgr.mapForeignKey(mappings[i],     mappings[i + 1], 
                                      mappings[i + 2], mappings[i + 3]);
        }
    }

    /**
     * @return
     */
    protected BasicSQLUtilsMapValueIFace getCollectionMemberIDValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            @Override
            public String mapValue(Object oldValue)
            {
                return curCollectionID.toString();
            }
        };
    }

    /**
     * @return
     */
    protected BasicSQLUtilsMapValueIFace getAgentCreatorValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            @Override
            public String mapValue(Object oldValue)
            {
                if (oldValue instanceof String)
                {
                    Integer id = getCreatorAgentId((String)oldValue);
                    if (id != null)
                    {
                        return id.toString();
                    }
                }
                return curAgentCreatorID.toString();
            }
        };
    }

    /**
     * @return
     */
    protected BasicSQLUtilsMapValueIFace getAgentModiferValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            @Override
            public String mapValue(Object oldValue)
            {
                if (oldValue instanceof String && !oldValue.toString().equals("NULL"))
                {
                    Integer id = getModifiedByAgentId((String)oldValue);
                    if (id != null)
                    {
                        return id.toString();
                    }
                }
                return curAgentModifierID.toString();
            }
        };
    }

    /**
     * @return
     */
    protected BasicSQLUtilsMapValueIFace getVersionValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            @Override
            public String mapValue(Object oldValue)
            {
                return "0";
            }
        };
    }

    /**
     * @return
     */
    protected BasicSQLUtilsMapValueIFace getIsPrimaryValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            @Override
            public String mapValue(Object oldValue)
            {
                return "1";
            }
        };
    }

    /**
     * @return
     */
    protected BasicSQLUtilsMapValueIFace getDivisionValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            @Override
            public String mapValue(Object oldValue)
            {
                return curDivisionID.toString();
            }
        };
    }

    /**
     * @return
     */
    protected BasicSQLUtilsMapValueIFace getDisciplineValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            @Override
            public String mapValue(Object oldValue)
            {
                return curDisciplineID.toString();
            }
        };
    }

    protected BasicSQLUtilsMapValueIFace getSrcLatLongUnitValueMapper()
    {
        return new BasicSQLUtilsMapValueIFace()
        {
            @Override
            public String mapValue(Object oldValue)
            {
                return "0";
            }
        };
    }

    /**
     * Removes all the records from every table in the new database and then copies over all the
     * tables that have few if any changes to their schema
     */
    public void copyTables(final boolean doBrief)
    {
        log.debug("copyTables()");
        // cleanAllTables(); // from DBCOnnection which is the new DB

        // String[] tablesToMoveOver2 = {
        // "LoanPhysicalObject"
        // };

        BasicSQLUtilsMapValueIFace collectionMemberIDValueMapper = getCollectionMemberIDValueMapper();
        BasicSQLUtilsMapValueIFace agentCreatorValueMapper = getAgentCreatorValueMapper();
        BasicSQLUtilsMapValueIFace agentModiferValueMapper = getAgentModiferValueMapper();
        BasicSQLUtilsMapValueIFace versionValueMapper      = getVersionValueMapper();
        BasicSQLUtilsMapValueIFace divisionValueMapper     = getDivisionValueMapper();
        BasicSQLUtilsMapValueIFace disciplineValueMapper   = getDisciplineValueMapper();
        BasicSQLUtilsMapValueIFace isPrimaryValueMapper    = getIsPrimaryValueMapper();
        BasicSQLUtilsMapValueIFace srcLatLongUnitValueMapper = getSrcLatLongUnitValueMapper();

        String[] tablesToMoveOver;
        if (!doBrief)
        {
            tablesToMoveOver = new String[] { 
                "CollectingEvent",
                "AccessionAgent", 
                "Accession", 
                "AccessionAuthorization",
                "Author", 
                "BiologicalObjectAttributes", // ZZZ 
                "Borrow", 
                "BorrowAgent", 
                "BorrowMaterial",
                "BorrowReturnMaterial", 
                 
                "CollectionObjectCitation", 
                "Collector",
                "Deaccession", 
                "DeaccessionAgent", 
                "DeterminationCitation", 
                "ExchangeIn",
                "ExchangeOut", 
                "GroupPerson", 
                //"Habitat", 
                "Journal", 
                //"Loan", 
                //"LoanAgent",
                "DeaccessionCollectionObject", 
                "LoanReturnPhysicalObject", 
                "LocalityCitation",
                "OtherIdentifier",
                "Permit", 
                "Preparation", // this is really an Attributes Table (PreparationAttributes)
                "Project",
                // "ProjectCollectionObjects", // I think we got rid of this!
                "ReferenceWork",
                "Shipment", // needs it's own conversion
                "TaxonCitation", };
        } else
        {
            tablesToMoveOver = new String[] { "GroupPerson", "Habitat" };
        }
        String oldLoanReturnPhysicalObj_Date_FieldName = "Date";
        String oldRefWork_Date_FieldName               = "Date";
        String oldDeaccession_Date_FieldName           = "Date";
        //String oldHabitat_Current_FieldName            = "Current";
        String oldAuthors_Order_FieldName              = "Order";
        String oldCollectors_Order_FieldName           = "Order";
        String oldGroupPersons_Order_FieldName         = "Order";
        String oldStratigraphy_Group_FieldName         = "Group";
        String oldBorrowReturnMaterial_Date_FieldName  = "Date";

        /*if (BasicSQLUtils.mySourceServerType == BasicSQLUtils.SERVERTYPE.MySQL)
        {
            oldDeaccession_Date_FieldName           = "Date1";
            oldRefWork_Date_FieldName               = "Date1";
            oldLoanReturnPhysicalObj_Date_FieldName = "Date1";
            oldBorrowReturnMaterial_Date_FieldName  = "Date1";
            oldHabitat_Current_FieldName            = "IsCurrent";
            oldAuthors_Order_FieldName              = "Order1";
            oldCollectors_Order_FieldName           = "Order1";
            oldGroupPersons_Order_FieldName         = "Order1";
            oldStratigraphy_Group_FieldName         = "Group1";
        }*/

        // This maps old column names to new column names, they must be in pairs
        // Ths Table Name is the "TO Table" name
        Map<String, Map<String, String>> tableMaps = new HashMap<String, Map<String, String>>();
        // ----------------------------------------------------------------------------------------------------------------------
        // NEW TABLE NAME, NEW FIELD NAME, OLD FIELD NAME
        // ----------------------------------------------------------------------------------------------------------------------
        tableMaps.put("accession",              createFieldNameMap(new String[] { "AccessionNumber", "Number" }));
        tableMaps.put("accessionagent",         createFieldNameMap(new String[] { "AgentID",  "AgentAddressID", "AccessionAgentID", "AccessionAgentsID" }));
        tableMaps.put("accessionauthorization", createFieldNameMap(new String[] { "AccessionAuthorizationID", "AccessionAuthorizationsID" }));
        tableMaps.put("author",                 createFieldNameMap(new String[] { "OrderNumber",  oldAuthors_Order_FieldName, "AuthorID", "AuthorsID" }));
        tableMaps.put("borrow",                 createFieldNameMap(new String[] { "IsClosed", "Closed" }));
        tableMaps.put("borrowagent",            createFieldNameMap(new String[] { "AgentID", "AgentAddressID",  "BorrowAgentID", "BorrowAgentsID" }));
        tableMaps.put("borrowreturnmaterial",   createFieldNameMap(new String[] { "ReturnedDate", oldBorrowReturnMaterial_Date_FieldName }));
        tableMaps.put("borrowshipment",         createFieldNameMap(new String[] { "BorrowShipmentID", "BorrowShipmentsID" }));
        tableMaps.put("collectingevent",        createFieldNameMap(new String[] { "TaxonID", "TaxonNameID", "Visibility", "GroupPermittedToView" }));
        tableMaps.put("collectionobjectcitation", createFieldNameMap(new String[] { "CollectionObjectID", "BiologicalObjectID" }));
        tableMaps.put("collector",              createFieldNameMap(new String[] { "OrderNumber", oldCollectors_Order_FieldName, "CollectorID", "CollectorsID" }));
        tableMaps.put("deaccession",            createFieldNameMap(new String[] { "DeaccessionDate", oldDeaccession_Date_FieldName }));
        tableMaps.put("deaccessionagent",       createFieldNameMap(new String[] { "AgentID", "AgentAddressID", "DeaccessionAgentID", "DeaccessionAgentsID" }));
        tableMaps.put("deaccessionpreparation", createFieldNameMap(new String[] { "DeaccessionPreparationID", "DeaccessionCollectionObjectID", "PreparationID", "CollectionObjectID", }));
        // tableMaps.put("determination", createFieldNameMap(new String[] {"CollectionObjectID",
        // "BiologicalObjectID", "IsCurrent", "Current1", "DeterminationDate", "Date1", "TaxonID",
        // "TaxonNameID"}));
        tableMaps.put("groupperson",           createFieldNameMap(new String[] { "GroupPersonID", "GroupPersonsID", "OrderNumber", oldGroupPersons_Order_FieldName }));
        //tableMaps.put("loan",                  createFieldNameMap(new String[] { "IsClosed", "Closed" }));
        //tableMaps.put("loanagent",             createFieldNameMap(new String[] { "AgentID", "AgentAddressID", "LoanAgentID", "LoanAgentsID" }));
        //tableMaps.put("loanpreparation",       createFieldNameMap(new String[] { "PreparationID", "PhysicalObjectID" }));
        tableMaps.put("loanreturnpreparation", createFieldNameMap(new String[] {"DeaccessionPreparationID", "DeaccessionPhysicalObjectID", 
                                                                                "LoanPreparationID",        "LoanPhysicalObjectID", 
                                                                                "LoanReturnPreparationID",  "LoanReturnPhysicalObjectID",
                                                                                "ReturnedDate",             oldLoanReturnPhysicalObj_Date_FieldName, 
                                                                                "QuantityResolved",         "Quantity", }));
        
        tableMaps.put("permit",                   createFieldNameMap(new String[] { "IssuedByID", "IssuerID", "IssuedToID", "IssueeID" }));
        tableMaps.put("projectcollectionobjects", createFieldNameMap(new String[] { "ProjectCollectionObjectID", "ProjectCollectionObjectsID" }));
        tableMaps.put("referencework",            createFieldNameMap(new String[] { "WorkDate",  oldRefWork_Date_FieldName, "IsPublished", "Published" }));
        tableMaps.put("stratigraphy",             createFieldNameMap(new String[] { "LithoGroup", oldStratigraphy_Group_FieldName }));
        tableMaps.put("taxoncitation",            createFieldNameMap(new String[] { "TaxonID", "TaxonNameID" }));

        // Turn back on when datamodel checked in
        tableMaps.put("collectionobjectattribute", createFieldNameMap(getCollectionObjectAttributeMappings()));
        tableMaps.put("preparationattribute",      createFieldNameMap(getPrepAttributeMappings()));
        tableMaps.put("collectingeventattribute",  createFieldNameMap(getHabitatAttributeMappings()));

        // Map<String, Map<String, String>> tableDateMaps = new Hashtable<String, Map<String,
        // String>>();
        // tableDateMaps.put("collectingevent", createFieldNameMap(new String[] {"TaxonID",
        // "TaxonNameID"}));
        // tableMaps.put("locality", createFieldNameMap(new String[] {"NationalParkName", "",
        // "ParentID", "TaxonParentID"}));

        clearValueMapper();
        addToValueMapper("CollectionMemberID", collectionMemberIDValueMapper);
        addToValueMapper("CreatedByAgentID",   agentCreatorValueMapper);
        addToValueMapper("ModifiedByAgentID",  agentModiferValueMapper);
        addToValueMapper("Version",            versionValueMapper);
        addToValueMapper("DivisionID",         divisionValueMapper);
        addToValueMapper("DisciplineID",       disciplineValueMapper);
        addToValueMapper("IsPrimary",          isPrimaryValueMapper);
        addToValueMapper("SrcLatLongUnit",     srcLatLongUnitValueMapper);
        
        
        
        TableWriter tblWriter = convLogger.getWriter("CopyTable.html", "Copy Tables");
        setTblWriter(tblWriter);
        
        for (String tableName : tablesToMoveOver)
        {
            String sql           = null;
            String fromTableName = tableName.toLowerCase(); 
            String toTableName   = fromTableName;
            
            tblWriter.log("From ["+fromTableName+"]["+toTableName+"]");

            setOneToOneIDHash(null);

            int errorsToShow = BasicSQLUtils.SHOW_ALL;

            TableStats tblStats = new TableStats(oldDBConn, fromTableName, newDBConn, toTableName);
            tableStatHash.put(fromTableName, tblStats);
            log.info("Getting ready to copy table: " + fromTableName);
            // if (tableName.equals("LoanPhysicalObject"))
            // {
            // String[] ignoredFields = {"IsResolved"};
            // BasicSQLUtils.setFieldsToIgnoreWhenMappingNames(ignoredFields);
            //               
            // } else
            //               
            setFieldsToIgnoreWhenMappingNames(null);
            HashMap<String, String> verbatimDateMapper = null;

            if (tableName.equals("Accession") || tableName.equals("AccessionAuthorization"))
            {
                String[] ignoredFields = { "RepositoryAgreementID", "Version", "CreatedByAgentID",
                                           "DateAcknowledged", "AddressOfRecordID", "AppraisalID", 
                                           "AccessionCondition", "DivisionID", "TotalValue" };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);

            } else if (fromTableName.equals("accession"))
            {
                String[] ignoredFields = { "RepositoryAgreementID", "Version", "CreatedByAgentID",
                                           "DivisionID", "TotalValue" };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
            } else if (fromTableName.equals("accessionagent"))
            {
                String[] ignoredFields = { "RepositoryAgreementID", "Version", "CreatedByAgentID",
                                           "CollectionMemberID" };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
            } else if (fromTableName.equals("attachment"))
            {
                String[] ignoredFields = { "Visibility", "VisibilitySetBy", "Version",
                                           "CreatedByAgentID", "CollectionMemberID" };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
            } else if (fromTableName.equals("biologicalobjectattributes"))
            {
                toTableName = "collectionobjectattribute";
                setFieldsToIgnoreWhenMappingNames(getCollectionObjectAttributeToIgnore());
                
            } else if (fromTableName.equals("borrow"))
            {
                String[] ignoredFields = { "IsFinancialResponsibility", "AddressOfRecordID",
                                           "Version", "CreatedByAgentID", "CollectionMemberID" };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
            } else if (fromTableName.equals("borrowreturnmaterial"))
            {
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for ReturnedByID
                
            } else if (fromTableName.equals("collectionobject"))
            {
                // The First name is the name of the new column that is an ID of a One-to-One
                // releationship
                // NOTE: We have mapped as a Many-to-One for Hibernate, because we really want it to
                // be
                // a Zero-or-One
                setOneToOneIDHash(createFieldNameMap(new String[] {
                        "PreparationAttributeID", "PreparationAttributeID",
                        "CollectionObjectAttributeID", "CollectionObjectAttributeID", "TotalValue" }));
                
            } else if (fromTableName.equals("collectionobjectcitation"))
            {
                String[] ignoredFields = { "IsFinancialResponsibility", "AddressOfRecordID",
                                           "Version", "CreatedByAgentID", "CollectionMemberID",
                                           "IsFigured"};
                setFieldsToIgnoreWhenMappingNames(ignoredFields);

            } else if (fromTableName.equals("collectingevent"))
            {
                String[] ignoredFields = {"VisibilitySetByID", "CollectingTripID",
                                          "EndDateVerbatim", "EndDatePrecision", 
                                          "StartDatePrecision", "HabitatAttributeID", "Version", "CreatedByAgentID",
                                          "CollectionMemberID", "CollectingEventAttributeID", "DisciplineID",
                                          "SGRStatus"};
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                setOneToOneIDHash(createFieldNameMap(new String[] {"HabitatAttributeID", "HabitatAttributeID" }));
                
                verbatimDateMapper = new HashMap<String, String>();
                verbatimDateMapper.put("StartDateVerbatim", "StartDateVerbatim");
                
                errorsToShow &= ~BasicSQLUtils.SHOW_NAME_MAPPING_ERROR; // urn off this error for StartDateVerbatim
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK;            // Turn off this error for LocalityID
                errorsToShow &= ~BasicSQLUtils.SHOW_VAL_MAPPING_ERROR;  // Turn off this error for Habitat

            } else if (fromTableName.equals("collector") || fromTableName.equals("collectors"))
            {
                String[] ignoredFields = { "IsPrimary", "Version", "CreatedByAgentID",  "CollectionMemberID", "DisciplineID" , "DivisionID" };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                //errorsToShow &= ~BasicSQLUtils.SHOW_NAME_MAPPING_ERROR;
                
            } else if (fromTableName.equals("deaccession"))
            {
                String[] ignoredFields = {"AccessionID", "Version", "CreatedByAgentID",  "ModifiedByAgentID"};
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
            } else if (fromTableName.equals("deaccessioncollectionobject"))
            {
                toTableName = "deaccessionpreparation";
                String[] ignoredFields = {"Version", "CreatedByAgentID",  "ModifiedByAgentID"};
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
            } else if (fromTableName.equals("determination"))
            {
                String[] ignoredFields = { "Version", "CreatedByAgentID",  "CollectionMemberID" };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
            } else if (fromTableName.startsWith("groupperson"))
            {
                String[] ignoredFields = { "DivisionID",  "Version", "CreatedByAgentID",  };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
            } else if (fromTableName.equals("habitat"))
            {
                toTableName = "collectingeventattribute";
                setFieldsToIgnoreWhenMappingNames(getHabitatAttributeToIgnore());
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for DeaccessionPhysicalObjectID
                
            } else if (fromTableName.equals("loan"))
            {
                String[] ignoredFields = { "SpecialConditions", "AddressOfRecordID", 
                                           "DateReceived", "ReceivedComments", "PurposeOfLoan", "OverdueNotiSetDate",
                                           "IsFinancialResponsibility", "Version", "CreatedByAgentID",
                                           "IsFinancialResponsibility", "SrcTaxonomy", "SrcGeography",
                                           "CollectionMemberID", "DisciplineID", "DivisionID",
                                           "IsGift"};
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
                sql = "SELECT * FROM loan WHERE Category = 1";
                
                
            } else if (fromTableName.equals("journal"))
            {
                String[] ignoredFields = { "ISSN", "Version", "CreatedByAgentID", "InstitutionID", "GUID", "Text1"};
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
            } else /*if (fromTableName.equals("loancollectionobject"))
            {
                toTableName = "loanreturnpreparation";
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for DeaccessionPhysicalObjectID
                
            } else*/ if (fromTableName.equals("loanreturnphysicalobject"))
            {
                toTableName = "loanreturnpreparation";
                
                String[] ignoredFields = { "QuantityReturned", "Version", "CreatedByAgentID",  "DisciplineID" };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for
                                                                // DeaccessionPhysicalObjectID
            } else if (fromTableName.equals("localitycitation"))
            {
                String[] ignoredFields = { "Version", "CreatedByAgentID",  "DisciplineID" };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
            } else if (fromTableName.equals("otheridentifier"))
            {
                String[] ignoredFields = { "Institution", "Version", "CreatedByAgentID",  "CollectionMemberID" };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                
            } else if (fromTableName.equals("permit"))
            {
                String[] ignoredFields = { "InstitutionID", "Version", "CreatedByAgentID",  };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for IssueeID,
                                                                // IssuerID
            } else if (fromTableName.equals("preparation"))
            {
                toTableName = "preparationattribute";
                setFieldsToIgnoreWhenMappingNames(getPrepAttributeAttributeToIgnore());
                setFieldsToIgnoreWhenMappingIDs(new String[] { "MediumID", "PreparationAttributeID" });
                
            } else if (fromTableName.equals("referencework"))
            {
                String[] ignoredFields = { "GUID", "Version", "CreatedByAgentID","CollectionMemberID", "ISBN", "InstitutionID", "ContainedRFParentID", };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for
                                                                // ContainingReferenceWorkID
            } else if (fromTableName.equals("shipment"))
            {
                String[] ignoredFields = { "GUID", "Version", "CreatedByAgentID", "DisciplineID",
                                           "BorrowID", "ExchangeOutID", "GiftID", "LoanID", };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
                setFieldsToIgnoreWhenMappingIDs(new String[] { "ShipmentMethodID" });

                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for ShippedByID
            } else if (fromTableName.equals("stratigraphy"))
            {
                errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for GeologicTimePeriodID
            } else
            {
                String[] ignoredFields = { "GUID", "Version", "CreatedByAgentID", "CollectionMemberID" };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
            }

            if (fromTableName.equals("accessionagent")
                || fromTableName.equals("accessionauthorization")
                || fromTableName.equals("author") 
                || fromTableName.equals("borrowshipment")
                || fromTableName.equals("borrowagent") 
                || fromTableName.equals("collector")
                || fromTableName.equals("deaccessionagent")
                || fromTableName.equals("groupperson"))
            {
                fromTableName = fromTableName + "s";
            }
            
            Map<String, String> map = tableMaps.get(toTableName);
            if (map == null)
            {
                tableMaps.put(toTableName, createFieldNameMap(new String[] { "ModifiedByAgentID", "LastEditedBy", }));
            } else
            {
                map.put("ModifiedByAgentID", "LastEditedBy");
            }


            if (!hasIgnoreFields())
            {
                String[] ignoredFields = { "Version", "CreatedByAgentID", "CollectionMemberID" };
                setFieldsToIgnoreWhenMappingNames(ignoredFields);
            }

            deleteAllRecordsFromTable(toTableName, BasicSQLUtils.myDestinationServerType);

            errorsToShow &= ~BasicSQLUtils.SHOW_NAME_MAPPING_ERROR; //assuming that new sp6 fields do not have sp5 equivalents
            setShowErrors(errorsToShow);
            setIdentityInsertONCommandForSQLServer(newDBConn, toTableName, BasicSQLUtils.myDestinationServerType);
            
            if (!copyTable(oldDBConn, newDBConn, sql, fromTableName, toTableName, tableMaps.get(toTableName), 
                           verbatimDateMapper, BasicSQLUtils.mySourceServerType, BasicSQLUtils.myDestinationServerType))
            {
                String msg = "Table [" + tableName + "] didn't copy correctly.";
                log.error(msg);
                tblWriter.logError(msg);
                break;

            }
            setIdentityInsertOFFCommandForSQLServer(newDBConn, toTableName, BasicSQLUtils.myDestinationServerType);
            setFieldsToIgnoreWhenMappingIDs(null);
            tblStats.collectStats();
        }
        setShowErrors(BasicSQLUtils.SHOW_ALL);
        
        setTblWriter(null);

        tblWriter.close();
    }

    /**
     * @return
     */
    protected String[] getCollectionObjectAttributeToIgnore()
    {
        return new String[] { "BiologicalObjectTypeId", "BiologicalObjectAttributesID", "SexId",
                "StageId", "Number34", "Number35", "Number36", "Version",
                "CreatedByAgentID", "CollectionMemberID", "Text11",
                "Text12", "Text13", "Text14", "CollectionObjectAttributeID",
                "PositionState", "Direction", "TopDistance", "BottomDistance", "DistanceUnits"};
    }

    protected String[] getCollectionObjectAttributeMappings()
    {
        //String oldBiologicalObjectAttribute_Condition_FieldName = "Condition";
        //if (BasicSQLUtils.mySourceServerType == BasicSQLUtils.SERVERTYPE.MySQL)
        //{
        //    oldBiologicalObjectAttribute_Condition_FieldName = "Condition1";// TODO change when get
        //                                                                    // new MySQL dumps, Meg
        //}

        return new String[] {
                "CollectionObjectAttributeID", "BiologicalObjectAttributesID",
                // "biologicalObjectTypeId", ??? "Number36"
                "Text10",   "Sex", 
                "Text11",   "Condition", 
                "Text15",   "Age", 
                "Text12",   "Stage", 
                "Number37", "Weight",
                "Number38", "Length", 
                "Number8",  "GosnerStage", 
                "Number9",  "SnoutVentLength",
                "Text8",    "Activity", 
                "Number10", "LengthTail", 
                "Text13",   "ReproductiveCondition",
                "Text14",   "ObjCondition", 
                "Number11", "LengthTarsus", 
                "Number12", "LengthWing",
                "Number13", "LengthHead", 
                "Number14", "LengthBody", 
                "Number15", "LengthMiddleToe",
                "Number16", "LengthBill", 
                "Number17", "TotalExposedCulmen", 
                "Number39", "MaxLength", 
                "Number40", "MinLength", 
                "Number18", "LengthHindFoot", 
                "Number19", "LengthForeArm", 
                "Number20", "LengthTragus", 
                "Number21", "LengthEar", 
                "Number22", "EarFromNotch", 
                "Number23", "Wingspan", 
                "Number24", "LengthGonad", 
                "Number25", "WidthGonad", 
                "Number26", "LengthHeadBody", 
                "Number41", "Width", 
                "Number27", "HeightFinalWhorl", 
                "Number28", "InsideHeightAperture", 
                "Number29", "InsideWidthAperture", 
                "Number30", "NumberWhorls", 
                "Number31", "OuterLipThickness",
                "Number32", "Mantle", 
                "Number42", "Height", 
                "Number33", "Diameter", 
                "Text9",    "BranchingAt",
                "ModifiedByAgentID", "LastEditedBy",
        // "Text1",
        // "Text2",
        // "Text3",
        // "Text4",
        // "Text5",
        // "remarks",
        // "sexId", "Number34", ?????
        // "stageId", "Number35", ????
        // "yesNo1",
        // "yesNo2",
        // "yesNo3",
        // "Number1",
        // "Number2",
        // "Number3",
        // "Number4",
        // "Number5",
        // "Number6",
        // "Number7",
        // "Text6",
        // "Text7",
        // "yesNo4",
        // "yesNo5",
        // "yesNo6",
        // "yesNo7",
        };
    }

    protected String[] getPrepAttributeAttributeToIgnore()
    {
        return new String[] { "MediumId", "PreparationTypeId", "ContainerTypeId", "Number3",
                "Number8",
                "Text16", // ??? JUST FOR NOW!
                "Version", "CreatedByAgentID", "CollectionMemberID", "Text22",
                "Text23", "Text24", "Text25", "Text26", "YesNo3", "YesNo4",

        };
    }

    /**
     * @return
     */
    protected String[] getPrepAttributeMappings()
    {
        return new String[] { "PreparationAttributeID", "PreparationID", 
                "AttrDate", "PreparedDate", 
                "Number3", "MediumID", 
                "Text3", "PartInformation", 
                "Text4", "StartBoxNumber", 
                "Text5", "EndBoxNumber", 
                "Text6", "StartSlideNumber", 
                "Text7", "EndSlideNumber", 
                "Text8", "SectionOrientation", 
                "Text9", "SectionWidth", 
                "Text26", "size", 
                "Text10", "URL", 
                "Text11", "Identifier", 
                "Text12", "NestLining", 
                "Text13", "NestMaterial", 
                "Text14", "NestLocation", 
                "Text15", "SetMark", 
                "Number4", "CollectedEggCount", 
                "Number5", "CollectedParasiteEggCount", 
                "Number6", "FieldEggCount", 
                "Number7", "FieldParasiteEggCount", 
                "Text17", "EggIncubationStage", 
                "Text18", "EggDescription", 
                "Text19", "Format", 
                "Text25", "StorageInfo", 
                "Text22", "PreparationType",
                // "preparationTypeId", "Number8", ?????
                "Text23", "ContainerType", 
                "Text24", "Medium",
                // "containerTypeId",????????
                "Text20", "DNAConcentration", 
                "Text21", "Volume",
                // "Text1",
                // "Text2",
                // "Number1",
                // "Number2",
                // "remarks",
                "Number9", "NestCollected",
                "ModifiedByAgentID", "LastEditedBy",
        // "yesNo1",
        // "yesNo2",
        };
    }

    /**
     * @return
     */
    protected String[] getHabitatAttributeToIgnore()
    {
        return new String[] { "HabitatTypeId", "CreatedByAgentID",  
                              "CollectionMemberID", "Number10",  "Number12", "DivisionID",
                              "Number13",  "Number9", "Text12", 
                              "Text13", "Text14", "Text15", 
                              "Text16", "Text17", "Version", "DisciplineID"};
    }

    /**
     * @return
     */
    protected String[] getHabitatAttributeMappings()
    {
        String oldHabitatAttribute_Current_FieldName = "Current";
        //if (BasicSQLUtils.mySourceServerType == BasicSQLUtils.SERVERTYPE.MySQL)
        //{
            //oldHabitatAttribute_Current_FieldName = "IsCurrent";// TODO change when get new MySQL
        //}
        
        return new String[] { 
                "CollectingEventAttributeID", "HabitatID", 
                "Number9",  "AirTempC",
                "Number10", "WaterTempC", 
                "Number11", "WaterpH", 
                "Text12",  "Turbidity", 
                "Text16",  "Clarity", 
                "Text14",  "Salinity", 
                "Text6",   "SoilType", 
                "Number6", "SoilPh",
                "Number7", "SoilTempC", 
                "Text7",   "SoilMoisture", 
                "Text15",  "Slope", 
                "Text13",  "Vegetation", 
                "Text17",  "HabitatType", 
                "Text8",   oldHabitatAttribute_Current_FieldName,
                "Text9",   "Substrate", 
                "Text10",  "SubstrateMoisture", 
                "Number8", "HeightAboveGround", 
                "Text11",  "NearestNeighbor",
                // "remarks",
                "Number13", "MinDepth", 
                "Number12", "MaxDepth",
                "ModifiedByAgentID", "LastEditedBy",
        // "text1",
        // "text2",
        // "number1",
        // "number2",
        // "habitatTypeId", ???????
        // "yesNo1",
        // "yesNo2",
        // "number3",
        // "number4",
        // "number5",
        // "text3",
        // "text4",
        // "text5",
        // "yesNo3",
        // "yesNo4",
        // "yesNo5",
        };
    }

    //--------------------------------------------------
    //
    //--------------------------------------------------
    private static Object[] oldAgentPairs = null;/*{
        "DeRose", -1926772415,
        "Diana Macpherson",   1368278510,
        "Ellen Garland", -1785514075,
        "Fred Wei",   2079366500,
        "Garry", -1926772415,
        "Garry DeRose", -1926772415,
        "Geoff Read", -2130793896,
        "hart", -1008280944,
        "Helen Sui",  1635071045,
        "herrera", -2030009241,
        "Jane Robbins",   20509669,
        "Janet Grieve",   1025273512,
        "Jeffrey Robinson",   1806176344,
        "Jeremy Yeoman",  1889954074,
        "Juan A. Sanchez",174762752,
        "Juan Sanchez",   174762752,
        "Kareen Schnabel", -1077337308,
        "Kate Neill", -2018113744,
        "Kelly Merrin", -2019247222,
        "Kerry Harrison", 760232751,
        "Kevin Mackay",   1579613419,
        "kroegerk", -563648105,
        "Lisa Bryant", -2143310869,
        "mackayk",1579613419,
        "Marian Holman",  71949857,
        "Mark Fenwick", -1142361525,
        "Matt Knox",  1108655478,
        "millers",30459730,
        "millss", -2883989,
        "neillk", -2018113744,
        "Niki Davey", 2121686451,
        "OSD Upload", 0,
        "Owen Anderson",  871846370,
        "Paul Grimes",438015252,
        "Pauline Roberts", 1127457126,
        "Sadie Mills", -2883989,
        "Sarah Gerken",   1602715995,
        "schnabel", -1077337308,
        "schnabelk", -1077337308,
        "Serena Cox", -310949401,
        "Stacey Buchanan", -16360493,
        "Susanne Schroeder", -608056062,
        "topdog_NIWA7Nov",1635071045,
        "topdog_Sp_NIWA_types2",  1635071045,
        "weif",   2079366500,
        "Caroline Chin", 302404493, 
        "Andrew Hosie", -1825647568,
        "Anne", -1254740093,
        "Anne-Nina", -1254740093,
        "Anne-Nina Loerz", -1254740093,
        "Ava Szabo", 546492723,
        "Carina Sim-Smith", 1032933123,
        "Caroline Stolter", -1857675833,
        "Daniel McGibbon", -279581929,
        "Dean Stotter", -520478407,
        "beaumont", -952853702,
        "bradleya", -715752117,
        "carterm", -126562704, // Megan
        //"casper", 101,
        "chinc", 302404493,
        "daveyn", 2121686451,
        //"topdog", 101,
        };
    
    private static final Object[] oldAgentPairsBio = {
        "Niamh K", 1657084373,
        "Andrew Hosie", 1968520716,
        "Sadie", 1713364627,
        "Serena Cox", 496451136,
        "Laith Jawad", -2113108612,
        "Jane Robbins", 1755336907,
        "Caroline", -1779840688,
        "Mark Fenwick2", -956474007,
        //"Data Import", 101,
        "Gread", 76256187,
    };*/
    
    private void loadAgentIdMappings()
    {
        if (oldAgentPairs == null)
        {
            JTextField      textField = new JTextField();
            BrowseBtnPanel  bbp       = new BrowseBtnPanel(textField, false, true);
            CellConstraints cc        = new CellConstraints();
            PanelBuilder    pb        = new PanelBuilder(new FormLayout("f:p:g", "p"));
            pb.add(bbp, cc.xy(1, 1));
            pb.setDefaultDialogBorder();
            
            CustomDialog    dlg       = new CustomDialog((Frame)UIRegistry.getTopWindow(), "Choose Agent Mapping File", true, pb.getPanel());
            dlg.createUI();
            dlg.pack();
            dlg.setVisible(true);
            if (!dlg.isCancelled())
            {
                String fileName = textField.getText();
                if (isNotEmpty(fileName))
                {
                    try
                    {
                        File file = new File(fileName);
                        if (file.exists())
                        {
                            XStream xstream = new XStream();
                            oldAgentPairs = (Object[])xstream.fromXML(new FileInputStream(file));
                        } else
                        {
                            UIRegistry.showError("The file ["+fileName+"] does not exist!");
                        }
                        
                    } catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                        UIRegistry.showError("Error parsing agent XML file ["+fileName+"]");
                        //if (pw != null) pw.close();
                        oldAgentPairs = null;//new Object[] {"XXXXXXX", 0};
                    }
                }
            }
        }
    }
    
    /**
     * @param name
     * @return
     */
    private Integer getOldAgentIdFromName(final String nameArg)
    {
        if (oldAgentPairs != null)
        {
            String name = nameArg;
            
            if (agentIdMapper == null)
            {
                agentIdMapper = idMapperMgr.get("agent", "AgentID");
            }
        
            if (oldAgentHash.size() == 0)
            {
                for (int i=0;i<oldAgentPairs.length;i+=2)
                {
                    oldAgentHash.put((String)oldAgentPairs[i], (Integer)oldAgentPairs[i+1]);
                }
            }
            
            if (name.startsWith("'"))
            {
                name = name.substring(1);
            }
            if (name.endsWith("'"))
            {
                name = name.substring(0, name.length()-1);
            }

            Integer oldId = oldAgentHash.get(name);
            if (oldId != null)
            {
                agentIdMapper = idMapperMgr.get("agent", "AgentID");
                Integer newId = agentIdMapper.get(oldId);
                if (newId == null)
                {
                    agentIdMapper = idMapperMgr.get("agentaddress", "AgentAddressID");
                    newId = agentIdMapper.get(oldId);
                    agentIdMapper = idMapperMgr.get("agent", "AgentID");
                }
                if (newId != null)
                {
                    return newId;
                }
                if (!oldUnmappedAgentNames.contains(name))
                {
                    log.error(String.format("Couldn't map old Agent Name [%s] oldId[%d]", name, oldId));
                }
                oldUnmappedAgentNames.add(name);
                
            } else if (!oldUnmappedAgentNames.contains(name))
            {
                log.error(String.format("Couldn't find old Agent Name [%s] in hash", name));
                oldUnmappedAgentNames.add(name);
            }
        }
        return null;
    }
    
    /**
     * @param createdBy
     * @return
     */
    protected Integer getCreatorAgentId(final String createdByName)
    {
        if (isNotEmpty(createdByName))
        {
            Integer id = getOldAgentIdFromName(createdByName);
            if (id != null)
            {
                return id;
            }
        }
        return creatorAgent == null ? null : creatorAgent.getAgentId();
    }

    /**
     * @param modifierAgent
     * @return
     */
    public Integer getModifiedByAgentId(final String modifierAgentName)
    {
        if (isNotEmpty(modifierAgentName) && agentIdMapper != null)
        {
            Integer id = getOldAgentIdFromName(modifierAgentName);
            if (id != null)
            {
                return id;
            }
        }
        return modifierAgent == null ? null : modifierAgent.getAgentId();
    }

    /**
     * @param createdByName
     * @return
     */
    protected Integer getCreatorAgentIdForAgent(final String createdByName)
    {
        return creatorAgent == null ? null : creatorAgent.getAgentId();
    }

    /**
     * @param modifierAgent
     * @return
     */
    public Integer getModifiedByAgentIdForAgent(final String modifierAgentName)
    {
        return modifierAgent == null ? null : modifierAgent.getAgentId();
    }
    
    /**
     * @param itUsername
     * @param itPwd
     * @param dbNameDest
     */
    public void fixCreatedModifiedByAgents(final String itUsername, final String itPwd, final String dbNameSrc)
    {
        DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
        if (!dbMgr.connect(itUsername, itPwd, "localhost", dbNameSrc))
        {
            log.error("Connecting to old DB");
            return;
        }
        
        String[] newTableNames = {"collectingevent",   "shipment",   "preparationattribute", "collectionobject",   "permit",   "otheridentifier",   "locality",   "loanreturnpreparation",   
                                  "loanpreparation",   "loanagent",  "loan",   "journal",   "groupperson",   "geologictimeperiod",   "geography",   "exchangein",   "exchangeout",
                                  "determination",     "collector",  "collectionobjectattribute", "gift"};
        
        String[] oldTableNames = {"collectingevent",   "shipment",   "preparation",          "collectionobject",   "permit",   "otheridentifier",   "locality",   "loanreturnpreparation",   
                                  "loanpreparation",   "loanagent",  "loan",   "journal",   "groupperson",   "geologictimeperiod",   "geography",   "exchangein",   "exchangeout",
                                  "determination",     "collector",  "biologicalobjectattributes", "loan"};
        
        String[] oldIDNames    = {"CollectingEventID", "ShipmentID",  "PreparationID",        "CollectionObjectID", "PermitID", "OtherIdentifierID", "LocalityID", "LoanReturnPreparationID", 
                                  "LoanPreparationID", "LoanAgentID", "LoanID", "JournalID", "GroupPersonID", "GeologicTimePeriodID", "GeographyID", "ExchangeInID", "ExchangeOutID",
                                  "DeterminationID",   "CollectorID", "BiologicalObjectAttributesID", "LoanID"};
        
        try
        {
            PrintWriter pw = new PrintWriter("missing_agentmappings_work.txt");
            
            Statement oStmt = oldDBConn.createStatement();
            
            for (int i=0;i<newTableNames.length;i++)
            {
                String newTblName = newTableNames[i];
                String oldTblName = oldTableNames[i];
                
                String msg = "------------------------------------------- " + newTblName + "-------------------------------------------";
                log.debug(msg);
                pw.println(msg);
                
                DBTableInfo ti = DBTableIdMgr.getInstance().getInfoByTableName(newTblName);
                if (dbMgr.doesFieldExistInTable(oldTblName, "LastEditedBy"))
                {
                    String mapTblName  = oldTblName;
                    String mapTblIdCol = oldIDNames[i];
                    if (newTblName.equals("gift"))
                    {
                        mapTblName  = "gift";
                        mapTblIdCol = "GiftID";
                    }
                    
                    IdMapperIFace idMapper = idMapperMgr.get(mapTblName, mapTblIdCol);
                    if (idMapper == null)
                    {
                        idMapper = idMapperMgr.addTableMapper(mapTblName, mapTblIdCol, false);
                    }
                    if (idMapper != null)
                    {
                        String uSql = String.format("UPDATE %s SET CreatedByAgentID=? WHERE %s=?", ti.getName(), ti.getIdColumnName());
                        log.debug(uSql);
                        PreparedStatement pStmt = newDBConn.prepareStatement(uSql);
                        
                        int totalCnt = BasicSQLUtils.getCountAsInt(oldDBConn, String.format("SELECT COUNT(*) FROM %s WHERE LastEditedBy IS NOT NULL", oldTblName));
                        System.out.println("Total Records: " + totalCnt);
                        pw.println("Total Records: " + totalCnt);
                                               
                        int       cnt = 0;
                        String    sql = String.format("SELECT %s, LastEditedBy FROM %s WHERE LastEditedBy IS NOT NULL", oldIDNames[i], oldTableNames[i]);
                        ResultSet rs  = oStmt.executeQuery(sql);
                        while (rs.next())
                        {
                            int    oldId           = rs.getInt(1);
                            String lastEditedByStr = rs.getString(2);
                            
                            Integer newId = idMapper.get(oldId);
                            if (newId != null)
                            {
                                Integer agtId  = getCreatorAgentId(lastEditedByStr);
                                if (agtId != null)
                                {
                                    pStmt.setInt(1, agtId);
                                    pStmt.setInt(2, newId);
                                    if (pStmt.executeUpdate() != 1)
                                    {
                                        msg = String.format("Error updating AgentID for %s for old Id %d  New Id: %d", oldTblName, oldId, newId);
                                        log.error(msg);
                                        pw.println(msg);
                                    } else
                                    {
                                        cnt++;
                                    }
                                } else
                                {
                                    if (!lastEditedByStr.equals("casper"))
                                   {
                                        msg = String.format("Error updating AgentID for %s for no agent for '%s'", oldTblName, lastEditedByStr);
                                        log.error(msg);
                                        pw.println(msg);
                                   }
                                }
                            } else
                            {
                                msg = String.format("No mapping for %s for old Id %d", oldTblName, oldId);
                                log.error(msg);
                                pw.println(msg);
                            }
                        }
                        msg = "Records updated: " + cnt;
                        log.debug(msg);
                        pw.println(msg);
                        
                        rs.close();
                        pStmt.close();
                    }
                }
            }
            pw.close();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        dbMgr.close();
    }

    /**
     * 
     */
    public void checkCreatedModifiedByAgents()
    {
        try
        {
            PrintWriter pw = new PrintWriter("missing_agentmappings.txt");
            
            for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
            {
                boolean doCheck = false;
                DBRelationshipInfo ri = ti.getRelationshipByName("modifiedByAgent");
                if (ri != null)
                {
                    doCheck = true;
                }
                
                if (doCheck)
                {
                    String sql = String.format("SELECT T1.*, a.LastName FROM (SELECT s.ModifiedByAgentID, COUNT(s.ModifiedByAgentID) " +
                    		"FROM %s s GROUP BY s.ModifiedByAgentID) T1 LEFT JOIN agent a ON a.AgentID = T1.ModifiedByAgentID ", ti.getName());
                    Vector<Object[]> counts = BasicSQLUtils.query(newDBConn, sql);
                    if (counts.size() > 0 && counts.size() < 5)
                    {
                        String msg = String.format("Table %s has %d different agents", ti.getName(), counts.size());
                        System.out.println(msg);
                        pw.println(msg);
                        
                        for (Object[] row : counts)
                        {
                            for (Object obj : row)
                            {
                                System.out.print(obj + " ");
                                pw.print(obj + " ");

                            }
                            System.out.println();
                            pw.println();
                        }
                    }
                }
            }
            pw.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * @return
     */
    protected int getCollectionMemberId()
    {
        return curCollectionID;
    }

    protected int getDisciplineId()
    {
        return curDisciplineID;
    }

    /**
     * Checks to see if any of the names in the array are in passed in name
     * @param referenceNames array of reference names
     * @param kingdomTaxonName the name to be figured out
     * @return true if there is a match
     */
    protected boolean checkName(String[] referenceNames, final String nameArg)
    {
        String name = nameArg.toLowerCase();
        String[] tokens = split(name.toLowerCase(), ' ');
        for (String tok : tokens)
        {
            for (String rn : referenceNames)
            {
                if (tok.startsWith(rn.toLowerCase())) { return true; }
                if (rn.toLowerCase().startsWith(tok)) { return true; }
            }
        }
        for (String tok : tokens)
        {
            for (String rn : referenceNames)
            {
                if (contains(tok, rn.toLowerCase())) { return true; }
                if (contains(rn.toLowerCase(), tok)) { return true; }
            }
        }
        return false;
    }

    /**
     * Convert a CollectionObjectTypeName to a DataType
     * @param collectionObjTypeName the name
     * @return the Standard DataType
     */
    public String getStandardDataTypeName(final String collectionObjTypeName)
    {
        if (checkName(new String[] { "Plant", "Herb" }, collectionObjTypeName)) { return "Plant"; }

        if (checkName(new String[] { "Fish", "Bird", "Frog", "Insect", "Fossil", "Icth", "Orn",
                "Herp", "Entom", "Paleo", "Mammal", "Invertebrate", "Animal" },
                collectionObjTypeName)) { return "Animal"; }

        if (checkName(new String[] { "Mineral", "Rock" }, collectionObjTypeName)) { return "Mineral"; }

        if (checkName(new String[] { "Fungi" }, collectionObjTypeName)) { return "Fungi"; }
        
        String msg = "****** Unable to Map [" + collectionObjTypeName + "] to a standard DataType.";
        log.error(msg);
        showError(msg);

        return null;
    }

    /**
     * Convert a taxonomyTypeName to a Discipline name
     * @param taxonDefName the name
     * @return the Standard DataType
     */
    public DisciplineType getStandardDisciplineName(final String taxonDefName, 
                                                    final String colObjTypeName, 
                                                    final String catSeriesName)
    {
        String taxonDescrStr = taxonDefName.toLowerCase() + " " + colObjTypeName.toLowerCase();
        DisciplineType dispType = getStandardDisciplineName(taxonDescrStr);
        if (dispType != null)
        {
            return dispType;
        }
        
        log.debug("**************** ["+taxonDescrStr+"]["+catSeriesName+"] *****************");
        
        if (dispType == null && catSeriesName != null)
        {
            StringTokenizer st = new StringTokenizer(catSeriesName, " ,"); //$NON-NLS-1$
            while (st.hasMoreTokens())
            {
                String name = st.nextToken().trim();
                log.debug("Checking token["+name+"]");
                dispType = getStandardDisciplineName(name);
                if (dispType != null)
                {
                    log.debug("    Found["+dispType+"]");
                    if (!dispType.isPaleo() &&
                            (contains(taxonDescrStr, "paleo") || 
                             contains(taxonDescrStr, "fossil") ||
                             contains(catSeriesName.toLowerCase(), "paleo") ||
                             contains(catSeriesName.toLowerCase(), "fossil")))
                    {
                        if (dispType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.botany)
                        {
                            return DisciplineType.getDiscipline(DisciplineType.STD_DISCIPLINES.paleobotany);
                        } 
                        
                        if (dispType.getDisciplineType() == DisciplineType.STD_DISCIPLINES.invertebrate)
                        {
                            return DisciplineType.getDiscipline(DisciplineType.STD_DISCIPLINES.invertpaleo);
                        }
                        
                        return DisciplineType.getDiscipline(DisciplineType.STD_DISCIPLINES.vertpaleo);
                    }
                    return dispType;
                }
            }
        }
        
        String msg = String.format("<html>Unable to automap type '%s' <BR>Catalog Series: '%s'", taxonDefName, catSeriesName);
        ToggleButtonChooserDlg<DisciplineType> dlg = new ToggleButtonChooserDlg<DisciplineType>((Frame)null, 
                                                               "Choose a Discipline", 
                                                               msg,
                                                               DisciplineType.getDisciplineList(), 
                                                               CustomDialog.OKCANCEL,
                                                               ToggleButtonChooserPanel.Type.RadioButton);
        dlg.setUseScrollPane(true);
        dlg.setVisible(true);

        if (!dlg.isCancelled())
        {
            return (DisciplineType)dlg.getSelectedObject();
        }
        
        return null;
    }

    /**
     * Convert a taxonomyTypeName to a Discipline name
     * @param name the name
     * @return the Standard DataType
     */
    private DisciplineType getStandardDisciplineName(final String name)
    {
        DisciplineType dispType = DisciplineType.getDiscipline(name.toLowerCase());
        
        if (dispType != null) 
        { 
            return dispType; 
        }

        STD_DISCIPLINES type = null;
        
        if (contains(name.toLowerCase(), "paleo") ||
            contains(name.toLowerCase(), "fossil")) 
        { 
            if (contains(name.toLowerCase(), "invert"))
            {
                type = STD_DISCIPLINES.invertpaleo;
                
            } else if (contains(name.toLowerCase(), "botan"))
            {
                type = STD_DISCIPLINES.paleobotany;
            } else
            {
                type = STD_DISCIPLINES.vertpaleo; // Default (should have a generic 'paleo' default)
            }
        } else if (checkName(new String[] { "Plant", "Herb", "Botan", "Fungi" }, name)) 
        { 
            type = STD_DISCIPLINES.botany; 
            
        } else if (checkName(new String[] { "ichthy", "Fish"}, name)) 
        { 
            type = STD_DISCIPLINES.fish;
            
        } else if (checkName(new String[] { "FishHerps", "Herps", "Herp", "Frog", "Reptile", "Amphib"}, name)) 
        { 
            type = STD_DISCIPLINES.herpetology; 
            
        } else if (checkName(new String[] { "Bird", "Ornithology", "Ornith"}, name)) 
        { 
            type = STD_DISCIPLINES.bird; 
            
        } else if (checkName(new String[] { "Insect", "Ento", "Bug", "Spider", "arachn" }, name)) 
        { 
            type = STD_DISCIPLINES.insect; 
            
        } else if (checkName(new String[] { "Mineral", "Rock" }, name)) 
        { 
            type = STD_DISCIPLINES.minerals;
            
        } else if (checkName(new String[] { "mammal", "mammals", "mammology"}, name)) 
        { 
            type = STD_DISCIPLINES.mammal;
        }
        
        if (type != null)
        {
            return DisciplineType.getDiscipline(type);
        }
        
        return null;
    }
    
    /**
     * Create a data type.
     * @param taxonomyTypeName the name
     * @return the ID (record id) of the data type
     */
    public int createDataType()
    {
        int    dataTypeId = -1;
        String dataTypeName = "Biota";//getStandardDataTypeName(taxonomyTypeName);
        if (dataTypeName == null) 
        { 
            return dataTypeId; 
        }

        try
        {
            if (dataTypeNameToIds.get(dataTypeName) == null)
            {
                /*
                 * describe datatype;
                 * +------------+-------------+------+-----+---------+----------------+ | Field |
                 * Type | Null | Key | Default | Extra |
                 * +------------+-------------+------+-----+---------+----------------+ | DataTypeID |
                 * int(11) | NO | PRI | | auto_increment | | Name | varchar(50) | YES | | | |
                 * +------------+-------------+------+-----+---------+----------------+
                 */

                Statement updateStatement = newDBConn.createStatement();
                // String updateStr = "INSERT INTO datatype (DataTypeID, TimestampCreated,
                // TimestampModified, LastEditedBy, Name) VALUES
                // (null,'"+nowStr+"','"+nowStr+"',NULL,'"+dataTypeName+"')";
                // Meg removed explicit insert of null value into DataTypeID, it was failing on SQL
                // Server
                // String updateStr = "INSERT INTO datatype ( TimestampCreated, TimestampModified,
                // Name, CreatedByAgentID, ModifiedByAgentID) VALUES
                // ('"+nowStr+"','"+nowStr+"','"+dataTypeName+"',"+getCreatorAgentId(null)+","+getModifiedByAgentId(null)+")";
                String updateStr = "INSERT INTO datatype ( TimestampCreated, TimestampModified, Name, Version, CreatedByAgentID, ModifiedByAgentID) "
                        + "VALUES ('"
                        + nowStr
                        + "','"
                        + nowStr
                        + "','"
                        + dataTypeName
                        + "', 0, "
                        + getCreatorAgentId(null) + "," + getModifiedByAgentId(null) + ")";
                updateStatement.executeUpdate(updateStr);
                updateStatement.clearBatch();
                updateStatement.close();
                updateStatement = null;

                dataTypeId = getHighestId(newDBConn, "DataTypeID", "datatype");
                log.info("Created new datatype[" + dataTypeName + "]");

                dataTypeNameToIds.put(dataTypeName, dataTypeId);

            } else
            {
                dataTypeId = dataTypeNameToIds.get(dataTypeName);
                log.info("Reusing new datatype[" + dataTypeName + "]");
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            showError(e.getMessage());
            throw new RuntimeException(e);
        }
        return dataTypeId;
    }

    /**
     * 
     */
    public void doLocalizeSchema()
    {

        Session     localSession = HibernateUtil.getNewSession();
        Transaction trans        = null;

        try
        {
            for (Object obj : localSession.createQuery("FROM Discipline").list())
            {
                
                trans = localSession.beginTransaction();
                
                Discipline discipline = (Discipline)obj;
    
                BuildSampleDatabase bsd = new BuildSampleDatabase();
                bsd.setSession(localSession);
                
                bsd.loadSchemaLocalization(discipline, 
                                           SpLocaleContainer.CORE_SCHEMA, 
                                           DBTableIdMgr.getInstance(),
                                           "CatalogNumberNumeric",
                                           null,
                                           BuildSampleDatabase.UpdateType.eBuildNew,
                                           null);
                localSession.save(discipline);
                trans.commit();
            }

            /*DBTableIdMgr schema = new DBTableIdMgr(false);
            schema.initialize(new File(XMLHelper.getConfigDirPath("specify_workbench_datamodel.xml")));
            BuildSampleDatabase.loadSchemaLocalization(discipline, SpLocaleContainer.WORKBENCH_SCHEMA, schema, null, null);

            trans = localSession.beginTransaction();
            localSession.save(discipline);
            trans.commit();
            */

        } catch (Exception ex)
        {
            ex.printStackTrace();
            System.err.println(ex);

            trans.rollback();

        } finally
        {
            localSession.close(); // OK
        }
    }
    
    public Integer createInstitution(final String instName)
    {
        StorageTreeDef storageTreeDef = buildSampleStorageTreeDef();
        
        try
        {
            BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "institution", BasicSQLUtils.myDestinationServerType);
            
            Statement updateStatement = newDBConn.createStatement();
            
            int institutionId = getNextIndex();
            
            strBuf.setLength(0);
            
            // Adding Institution
            strBuf.append("INSERT INTO institution (InstitutionID, IsServerBased, IsAccessionsGlobal, IsSingleGeographyTree, IsSharingLocalities, TimestampModified, Name, TimestampCreated, StorageTreeDefID, ");
            strBuf.append("CreatedByAgentID, ModifiedByAgentID, Version, UserGroupScopeId, IsSecurityOn, Remarks) VALUES (");
            strBuf.append(institutionId + ",FALSE,FALSE,FALSE,FALSE,");
            strBuf.append("'" + dateTimeFormatter.format(now) + "',"); // TimestampModified
            strBuf.append("'" + instName + "',");
            strBuf.append("'" + dateTimeFormatter.format(now) + "',"); // TimestampCreated
            strBuf.append(storageTreeDef.getStorageTreeDefId()+","); // StorageTreeDefID
            strBuf.append(getCreatorAgentId(null) + "," + getModifiedByAgentId(null) + ",0, ");
            strBuf.append(institutionId); // UserGroupScopeID
            strBuf.append(", 0"); // IsSecurityOn
            strBuf.append(", 'Sp5Converted'"); // Remarks
            strBuf.append(")");
            log.info(strBuf.toString());

            updateStatement.executeUpdate(strBuf.toString());
            
            updateStatement.clearBatch();
            updateStatement.close();
            
            return institutionId;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            System.exit(0);
        }

        return null;
    }

    /**
     * 
     */
    @SuppressWarnings("cast")
    public void convertDivision(final Integer institutionId)
    {
        try
        {
            strBuf.setLength(0);
            
            curDivisionID = getNextIndex();
            
            Statement updateStatement = newDBConn.createStatement();
            
            // Adding Institution
            strBuf.append("INSERT INTO division (DivisionID, InstitutionID, TimestampModified, DisciplineType, Name, AltName, Abbrev, TimestampCreated, ");
            strBuf.append("CreatedByAgentID, ModifiedByAgentID, Version, UserGroupScopeId) VALUES (");
            strBuf.append(curDivisionID + ",");
            strBuf.append(institutionId + ",");
            strBuf.append("'" + dateTimeFormatter.format(now) + "',"); // TimestampModified
            strBuf.append("'" + disciplineType.getName() + "',");
            strBuf.append("'" + disciplineType.getTitle() + "',");
            strBuf.append("NULL,");
            strBuf.append("'" + disciplineType.getAbbrev() + "',");
            strBuf.append("'" + dateTimeFormatter.format(now) + "',"); // TimestampCreated
            strBuf.append(getCreatorAgentId(null) + "," + getModifiedByAgentId(null) + ",0, ");
            strBuf.append(curDivisionID); // UserGroupScopeID
            strBuf.append(")");
            
            log.info(strBuf.toString());

            updateStatement.executeUpdate(strBuf.toString());
            
            updateStatement.clearBatch();
            updateStatement.close();
            updateStatement = null;
            
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        Session cacheSession = DataBuilder.getSession();
        DataBuilder.setSession(null);

        Session     localSession = HibernateUtil.getNewSession();
        List<?> list = (List<?>)localSession.createQuery("FROM Division WHERE id = "+curDivisionID).list();
        division = (Division)list.get(0);
        localSession.close();
        
        AppContextMgr.getInstance().setClassObject(Division.class, division);

        DataBuilder.setSession(cacheSession);
    }
    
    /**
     * @param o
     */
    public void persist(Object o)
    {
        if (session != null)
        {
            session.saveOrUpdate(o);
        }
    }

    public void setSession(Session s)
    {
        session = s;
    }

    public void startTx()
    {
        HibernateUtil.beginTransaction();
    }

    public void commitTx()
    {
        HibernateUtil.commitTransaction();
    }
    
    

    /**
     * Converts Object Defs.
     * @param specifyUserId
     * @return true on success, false on failure
     */
    public boolean convertCollectionObjectTypes(final int specifyUserId)
    {
        try
        {
            HashSet<Integer> hashSet = new HashSet<Integer>();
            StringBuilder    inSB    = new StringBuilder();
            for (CollectionInfo ci : collectionInfoShortList)
            {
                if (!hashSet.contains(ci.getTaxonomyTypeId()))
                {
                    if (inSB.length() > 0) inSB.append(',');
                    inSB.append(ci.getTaxonomyTypeId());
                    hashSet.add(ci.getTaxonomyTypeId());
                }
            }
            
            StringBuilder sb = new StringBuilder("SELECT TaxonomyTypeID FROM taxonomytype WHERE TaxonomyTypeId in (");
            sb.append(inSB);
            sb.append(')');
            log.debug(sb.toString());
            
            // This mapping is used by Discipline
            //for (Object txTypIdObj : BasicSQLUtils.querySingleCol(oldDBConn, sb.toString()))
            //{
            //    Integer txTypId = (Integer)txTypIdObj;
            //    taxonomyTypeMapper.put(txTypId, getNextIndex());
            //}

            // Create a Hashtable to track which IDs have been handled during the conversion process
            deleteAllRecordsFromTable(newDBConn, "datatype",       BasicSQLUtils.myDestinationServerType);
            deleteAllRecordsFromTable(newDBConn, "discipline",     BasicSQLUtils.myDestinationServerType);
            deleteAllRecordsFromTable(newDBConn, "collection",     BasicSQLUtils.myDestinationServerType);
            // BasicSQLUtils.deleteAllRecordsFromTable(newDBConn, "collection_colobjdef");

            Hashtable<Integer, Integer> newColObjIDTotaxonomyTypeID = new Hashtable<Integer, Integer>();
            
            TableWriter tblWriter = convLogger.getWriter("convertCollectionObjectTypes.html", "Collection Object Type");
            
            // Create a Hashed List of CollectionInfo for each unique TaxonomyTypeId
            // where the TaxonomyTypeId is a Discipline
            HashMap<Integer, Vector<CollectionInfo>> collDispHash = new HashMap<Integer, Vector<CollectionInfo>>();
            for (CollectionInfo info : collectionInfoShortList)
            {
                Vector<CollectionInfo> colInfoList = collDispHash.get(info.getTaxonomyTypeId());
                if (colInfoList == null)
                {
                    colInfoList = new Vector<CollectionInfo>();
                    collDispHash.put(info.getTaxonomyTypeId(), colInfoList);
                }
                colInfoList.add(info);
            }
            
            String dateTimeNow = dateTimeFormatter.format(now);
            int collectionCnt      = 0;
            for (Integer taxonTypeId : collDispHash.keySet())
            {
                Vector<CollectionInfo> collInfoList = collDispHash.get(taxonTypeId);
                
                // Pick any of the CollectionInfo objects because they will
                // all share the same Discipline
                CollectionInfo      info = null;
                for (CollectionInfo ci : collInfoList)
                {
                    if (ci.getCatSeriesId() != null)
                    {
                        info = ci;
                        break;
                    }
                }
                
                if (info == null)
                {
                    //UIRegistry.showError("No viable CatSeriesId to create Discipline. \n(Picking one...)");
                    info = collInfoList.get(0);
                    //System.exit(0);
                }
                
                String  taxonomyTypeName = info.getTaxonomyTypeName();
                Integer taxonomyTypeID   = info.getTaxonomyTypeId();
                String  lastEditedBy     = null;
                
                String msg = "Creating a new Discipline for taxonomyTypeName[" + taxonomyTypeName + "] disciplineType[" + disciplineType.getTitle() + "]";
                log.info(msg);
                tblWriter.log(msg);
                
                DisciplineType disciplineTypeObj = info.getDisciplineTypeObj();
                if (disciplineTypeObj == null)
                {
                    disciplineTypeObj = getStandardDisciplineName(info.getTaxonomyTypeName(), info.getColObjTypeName(), info.getCatSeriesName());
                }
                taxonomyTypeName = disciplineTypeObj.getName();

                // Figure out what type of standard data type this is from the
                // CollectionObjectTypeName
                setIdentityInsertOFFCommandForSQLServer(newDBConn, "datatype", BasicSQLUtils.myDestinationServerType);
    
                int dataTypeId = createDataType();
                if (dataTypeId == -1)
                {
                    msg = "**** Had to Skip record because of DataType mapping error[" + taxonomyTypeName + "]";
                    log.error(msg);
                    tblWriter.logError(msg);
                    System.exit(1);
                }
            
                String taxTypeName      = info.getTaxonomyTypeName();
                lastEditedBy            = info.getCatSeriesLastEditedBy();
                taxonomyTypeID          = info.getTaxonomyTypeId();
                
                //System.err.println(String.format("TaxonomyTypeName: %s  taxonomyTypeID: %d", taxTypeName, taxonomyTypeID, info.get));
                
                //---------------------------------------------------------------------------------
                //-- Create Discipline
                //---------------------------------------------------------------------------------
                //Integer newColObjDefID = getNextIndex();//taxonomyTypeMapper.get(taxonomyTypeID);
                //if (newColObjDefID == null)
                //{
                //    UIRegistry.showError("Was unable to map old TaxonomyTypeId["+taxonomyTypeID+"] to new ColectionObjectDefId. \nSeries Name: ["+info.getCatSeriesName()+"]\n(Exiting...)");
                //    //System.exit(0);
                //}
                
                // use the old CollectionObjectTypeName as the new Discipline name
                setIdentityInsertONCommandForSQLServer(newDBConn, "discipline", BasicSQLUtils.myDestinationServerType);
                Statement     updateStatement = newDBConn.createStatement();
                StringBuilder strBuf2         = new StringBuilder();
                
                curDisciplineID = getNextIndex();
                info.setDisciplineId(curDisciplineID);
                
                // adding DivisioniID
                strBuf2.setLength(0);
                strBuf2.append("INSERT INTO discipline (DisciplineID, TimestampModified, Type, Name, TimestampCreated, ");
                strBuf2.append("DataTypeID, GeographyTreeDefID, GeologicTimePeriodTreeDefID, TaxonTreeDefID, DivisionID, ");
                strBuf2.append("CreatedByAgentID, ModifiedByAgentID, Version, UserGroupScopeId, PaleoContextChildTable, IsPaleoContextEmbedded) VALUES (");
                strBuf2.append(info.getDisciplineId() + ",");
                strBuf2.append("'" + dateTimeNow + "',"); // TimestampModified
                strBuf2.append("'" + disciplineTypeObj.getName() + "',");
                strBuf2.append("'" + disciplineTypeObj.getTitle() + "',");
                strBuf2.append("'" + dateTimeNow + "',"); // TimestampCreated
                strBuf2.append(dataTypeId + ",");
                strBuf2.append("1,"); // GeographyTreeDefID
                strBuf2.append("1,"); // GeologicTimePeriodTreeDefID
                strBuf2.append("1,"); // TaxonTreeDefID
    
                strBuf2.append(division.getDivisionId() + ","); // DivisionID
                strBuf2.append(getCreatorAgentId(null) + "," + getModifiedByAgentIdForAgent(lastEditedBy) + ",0, ");
                strBuf2.append(curDisciplineID + ",");  // UserGroupScopeId
                strBuf2.append("'collectionobject', true)"); //PaleoContextChildTable, IsPaleoContextEmbedded
                
                // strBuf2.append("NULL)");// UserPermissionID//User/Security changes
                log.info(strBuf2.toString());
    
                removeForeignKeyConstraints(newDBConn, BasicSQLUtils.myDestinationServerType);
                updateStatement.executeUpdate(strBuf2.toString());
                
                updateStatement.clearBatch();
                updateStatement.close();
                updateStatement = null;
                setIdentityInsertOFFCommandForSQLServer(newDBConn, "discipline", BasicSQLUtils.myDestinationServerType);
                //Integer disciplineID = getHighestId(newDBConn, "DisciplineID", "discipline");
    
                newColObjIDTotaxonomyTypeID.put(curDisciplineID, taxonomyTypeID);
    
                msg = "**** Created new discipline[" + taxonomyTypeName + "] is dataType ["  + dataTypeId + "]";
                log.info(msg);
                tblWriter.log(msg);
                
                Session localSession = HibernateUtil.getNewSession();

                for (CollectionInfo collInfo : collInfoList)
                {
                    Integer catalogSeriesID = collInfo.getCatSeriesId();
                    String  seriesName      = collInfo.getCatSeriesName();
                    String  prefix          = collInfo.getCatSeriesPrefix();
                    String remarks          = collInfo.getCatSeriesRemarks();
                    
                    collInfo.setDisciplineId(curDisciplineID);
                    
                    AutoNumberingScheme cns = null;
                    if (catalogSeriesID != null && isNotEmpty(seriesName))
                    {
                        cns = catSeriesToAutoNumSchemeHash.get(catalogSeriesID);
                        if (cns == null)
                        {
                            try
                            {
                                cns = new AutoNumberingScheme();
                                cns.initialize();
                                cns.setIsNumericOnly(true);
                                cns.setSchemeClassName("");
                                cns.setSchemeName(seriesName);
                                cns.setTableNumber(CollectionObject.getClassTableId());
                                Transaction trans = localSession.beginTransaction();
                                localSession.save(cns);
                                trans.commit();
                                catSeriesToAutoNumSchemeHash.put(catalogSeriesID, cns);
                                
                            } catch (Exception ex)
                            {
                                ex.printStackTrace();
                                throw new RuntimeException(ex);
                            }
                        }
                    } else
                    {
                        seriesName = taxTypeName;
                    }
                    
                    Integer catNumSchemeId = cns != null ? cns.getAutoNumberingSchemeId() : null;
                    
                    collInfo.setCollectionId(getNextIndex());
                    curCollectionID = collInfo.getCollectionId();
                    
                    msg = "**** Created new Collection [" + seriesName + "] is curCollectionID ["  + curCollectionID + "]";
                    log.info(msg);
                    
                    updateStatement = newDBConn.createStatement();
                    strBuf.setLength(0);
                    strBuf.append("INSERT INTO collection (CollectionID, DisciplineID, CollectionName, Code, Remarks, CatalogFormatNumName, ");
                    strBuf.append("IsEmbeddedCollectingEvent, TimestampCreated, TimestampModified, CreatedByAgentID, ModifiedByAgentID, ");
                    strBuf.append("Version, UserGroupScopeId) VALUES (");
                    strBuf.append(curCollectionID + ",");
                    strBuf.append(curDisciplineID + ",");
                    strBuf.append(getStrValue(seriesName) + ",");
                    strBuf.append(getStrValue(prefix) + ",");
                    strBuf.append(getStrValue(remarks) + ",");
                    strBuf.append("'CatalogNumberNumeric',");
                    strBuf.append((isSharingCollectingEvents ? 0 : 1)  + ",");
                    strBuf.append("'" + dateTimeFormatter.format(now) + "',"); // TimestampModified
                    strBuf.append("'" + dateTimeFormatter.format(now) + "',"); // TimestampCreated
                    strBuf.append(getCreatorAgentId(null) + "," + getModifiedByAgentId(lastEditedBy) + ", 0, ");
                    strBuf.append(curCollectionID);  // UserGroupScopeId
                    strBuf.append(")");
    
                    log.debug(strBuf.toString());
        
                    updateStatement.executeUpdate(strBuf.toString());
                    
                    //curCollectionID = getInsertedId(updateStatement);
                    
                    updateStatement.clearBatch();
                    updateStatement.close();
                    updateStatement = null;
    
                    if (catNumSchemeId != null && catalogSeriesID != null)
                    {
                        joinCollectionAndAutoNum(curCollectionID, catNumSchemeId);
    
                        String hashKey = catalogSeriesID + "_" + taxonomyTypeID;
    
                        Integer newCatSeriesID = getHighestId(newDBConn, "CollectionID", "collection");
                        collectionHash.put(hashKey, newCatSeriesID);
                        if (isNotEmpty(prefix))
                        {
                            prefixHash.put(hashKey, prefix);
                        }
    
                        msg = "Collection New[" + newCatSeriesID + "] [" + seriesName + "] [" + prefix + "] curDisciplineID[" + curDisciplineID + "]";
                    } else
                    {
                        msg = "Collection New[" + seriesName + "] [" + prefix + "] curDisciplineID[" + curDisciplineID + "]";
                    }
                    log.info(msg);
                    tblWriter.log(msg);
    
                    //recordCnt++;
                    //msg = "Collection Join Records: " + recordCnt;
                    //log.info(msg);
                    //tblWriter.log(msg);
    
                    
                    //rs.close();
                    //stmt.close();
                    
                    collectionCnt++;
                } // Collection for loop
                
                localSession.close();
                
            } // for loop 
            
            tblWriter.close();
            
            for (CollectionInfo ci : collectionInfoShortList)
            {
                if (ci.getCatSeriesId() != null)
                {
                    log.debug("Cat Series: " + ci.getCatSeriesId() + " "+ci.getCollectionId());
                    Vector<Integer> colList = catSeriesToNewCollectionID.get(ci.getCatSeriesId());
                    if (colList == null)
                    {
                        colList = new Vector<Integer>();
                        catSeriesToNewCollectionID.put(ci.getCatSeriesId(), colList);
                    }
                    colList.add(ci.getCollectionId());
                }
            }
            
            for (Integer catSeriesId : catSeriesToNewCollectionID.keySet())
            {
                Vector<Integer> colList = catSeriesToNewCollectionID.get(catSeriesId);
                if (colList.size() > 1)
                {
                    UIRegistry.showError("There are multiple Collections assigned to the same CatalogSeries and we can't handle that right now.");
                    return false;
                }
            }
            
            return true;

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            showError(e.toString());
            System.exit(0);
        }
        
        return false;
    }
    
    /**
     * 
     */
    /*public void loadDisciplineObjects()
    {
        try
        {
            for (CollectionInfo ci : collectionInfoShortList)
            {
                log.debug(ci.getCatSeriesName()+"   ci.getDisciplineId(): "+ci.getDisciplineId());
                List<?> list = (List<?>)HibernateUtil.getCurrentSession().createQuery("FROM Discipline WHERE id = " + ci.getDisciplineId()).list();
                if (list != null && list.size() == 1)
                {
                    ci.setDiscipline((Discipline)list.get(0));
                } else
                {
                    log.error("Couldn't load discipline["+ci.getDisciplineId()+"]");
                }
            }

        } catch (Exception ex)
        {
            log.error("******* " + ex);
            throw new RuntimeException("Couldn't load disciplines");
        }

    }*/
    
    /**
     * @param collId
     * @param autoNumId
     */
    protected void joinCollectionAndAutoNum(final Integer collId, 
                                            final Integer autoNumId)
    {
        try
        {
            Statement updateStatement = newDBConn.createStatement();
            strBuf.setLength(0);
            strBuf.append("INSERT INTO autonumsch_coll (CollectionID, AutoNumberingSchemeID) VALUES (");
            strBuf.append(collId + ",");
            strBuf.append(autoNumId.toString());
            strBuf.append(")");

            log.debug(strBuf.toString());

            updateStatement.executeUpdate(strBuf.toString());
            updateStatement.clearBatch();
            updateStatement.close();
            updateStatement = null;

        } catch (SQLException ex)
        {
            ex.printStackTrace();
            showError(ex.getMessage());
        }
    }

    /**
     * Looks up all the current Permit Types and uses them, instead of the usystable
     */
    @SuppressWarnings("unchecked")
    public void createPermitTypePickList()
    {
        /*
         * try { Statement stmt = oldDBConn.createStatement(); String sqlStr = "select count(Type)
         * from (select distinct Type from permit where Type is not null) as t";
         * 
         * log.info(sqlStr);
         * 
         * boolean useField = false; ResultSet rs = stmt.executeQuery(sqlStr); } catch (SQLException
         * e) { e.printStackTrace(); log.error(e); }
         */

        Session localSession = HibernateUtil.getCurrentSession();
        PickList pl = new PickList();
        pl.initialize();
        Set<PickListItemIFace> items = pl.getItems();

        try
        {
            pl.setName("Permit");
            pl.setSizeLimit(-1);

            HibernateUtil.beginTransaction();
            localSession.saveOrUpdate(pl);
            HibernateUtil.commitTransaction();

        } catch (Exception ex)
        {
            log.error("******* " + ex);
            HibernateUtil.rollbackTransaction();
            throw new RuntimeException("Couldn't create PickList for [Permit]");
        }

        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);

            String sqlStr = "select distinct Type from permit where Type is not null";

            log.info(sqlStr);

            ResultSet rs = stmt.executeQuery(sqlStr);

            // check for no records which is OK
            if (!rs.first()) { return; }

            int count = 0;
            do
            {
                String typeStr = rs.getString(1);
                if (typeStr != null)
                {
                    log.info("Permit Type[" + typeStr + "]");
                    PickListItem pli = new PickListItem();
                    pli.initialize();
                    pli.setTitle(typeStr);
                    pli.setValue(typeStr);
                    pli.setTimestampCreated(now);
                    items.add(pli);
                    pli.setPickList(pl);
                    count++;

                }
            } while (rs.next());

            log.info("Processed Permit Types " + count + " records.");

            HibernateUtil.beginTransaction();

            localSession.saveOrUpdate(pl);

            HibernateUtil.commitTransaction();

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }

    }

    /**
     * Converts an old USYS table to a PickList.
     * @param usysTableName old table name
     * @param pickListName new pciklist name
     * @return true on success, false on failure
     */
    @SuppressWarnings("unchecked")
    public boolean convertUSYSToPicklist(final Session    localSession, 
                                         final Collection collection,
                                         final String     usysTableName, 
                                         final String     pickListName)
    {
        List<FieldMetaData> fieldMetaData = getFieldMetaDataFromSchema(oldDBConn, usysTableName);

        int ifaceInx    = -1;
        int dataInx     = -1;
        int fieldSetInx = -1;
        int i           = 0;
        for (FieldMetaData md : fieldMetaData)
        {
            if (ifaceInx == -1 && md.getName().equals("InterfaceID"))
            {
                ifaceInx = i + 1;

            } else if (fieldSetInx == -1 && md.getName().equals("FieldSetSubTypeID"))
            {
                fieldSetInx = i + 1;

            } else if (dataInx == -1 && md.getType().toLowerCase().indexOf("varchar") > -1)
            {
                dataInx = i + 1;
            }
            i++;
        }

        if (ifaceInx == -1 || dataInx == -1 || fieldSetInx == -1) 
        { 
            throw new RuntimeException("Couldn't decypher USYS table ifaceInx[" + ifaceInx + "] dataInx[" + dataInx+ "] fieldSetInx[" + fieldSetInx + "]"); 
        }

        PickList pl = new PickList();
        pl.initialize();

        try
        {
            pl.setName(pickListName);
            
            if (pickListName.equals("PrepType"))
            {
                pl.setReadOnly(true);
                pl.setSizeLimit(-1);
                pl.setIsSystem(true);
                pl.setTableName("preptype");
                pl.setType((byte)1);
                
            } else
            {
                pl.setReadOnly(false);
                pl.setSizeLimit(-1);
            }
            pl.setCollection(collection);
            collection.getPickLists().add(pl);

            Transaction trans = localSession.beginTransaction();
            localSession.saveOrUpdate(pl);
            localSession.saveOrUpdate(collection);
            trans.commit();
            localSession.flush();
            
        } catch (Exception ex)
        {
            log.error("******* " + ex);
            HibernateUtil.rollbackTransaction();
            throw new RuntimeException("Couldn't create PickList for [" + usysTableName + "]");
        }

        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            String sqlStr = "select * from " + usysTableName + " where InterfaceID is not null";

            log.info(sqlStr);

            boolean useField = false;
            ResultSet rs = stmt.executeQuery(sqlStr);

            // check for no records which is OK
            if (!rs.first()) { return true; }

            do
            {
                Object fieldObj = rs.getObject(fieldSetInx);
                if (fieldObj != null)
                {
                    useField = true;
                    break;
                }
            } while (rs.next());

            Hashtable<String, String> values = new Hashtable<String, String>();

            //log.info("Using FieldSetSubTypeID " + useField);
            rs.first();
            int count = 0;
            do
            {
                if (!useField || rs.getObject(fieldSetInx) != null)
                {
                    String val      = rs.getString(dataInx);
                    String lowerStr = val.toLowerCase();
                    if (values.get(lowerStr) == null)
                    {
                        //log.info("[" + val + "]");
                        pl.addItem(val, val);
                        values.put(lowerStr, val);
                        count++;
                    } else
                    {
                        log.info("Discarding duplicate picklist value[" + val + "]");
                    }
                }
            } while (rs.next());

            log.info("Processed " + usysTableName + "  " + count + " records.");

            Transaction trans = localSession.beginTransaction();

            localSession.saveOrUpdate(pl);

            trans.commit();
            
            localSession.flush();
            
            return true;

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
            
        }
    }

    /**
     * Converts all the USYS tables to PickLists.
     * @return true on success, false on failure
     */
    public boolean convertUSYSTables(final Session localSession, 
                                     final Collection collection)
    {
        log.info("Creating picklists");
        
        log.info("Converting USYS Tables.");

        //int tableType = PickListDBAdapterIFace.Type.Table.ordinal();
        // int tableFieldType = PickListDBAdapterIFace.Type.TableField.ordinal();

        // Name Type Table Name Field Formatter R/O Size IsSys
        Session cachedSession = DataBuilder.getSession();
        DataBuilder.setSession(null);
        
        String[] tables = { 
                "usysaccessionstatus",            "AccessionStatus", 
                "usysaccessiontype",              "AccessionType", 
                "usysborrowagenrole",             "BorrowAgentRole", 
                "usysaccessionarole",             "AccessionRole", 
                "usysdeaccessiorole",             "DeaccessionaRole", 
                "usysloanagentsrole",             "LoanAgentRole", 
                "usysbiologicalsex",              "BiologicalSex", 
                "usysbiologicalstage",            "BiologicalStage", 
                "usyscollectingmethod",           "CollectingMethod",
                "usyscollobjprepmeth",            "CollObjPrepMeth", 
                "usysdeaccessiotype",             "DeaccessionType",
                "usysdeterminatconfidence",       "DeterminationConfidence", 
                "usysdeterminatmethod",           "DeterminationMethod", 
                "usysdeterminattypestatusname",   "TypeStatus",
                "usyshabitathabitattype",         "HabitatType", 
                "usyslocalityelevationmethod",    "LocalityElevationMethod", 
                "usysobservatioobservationmetho", "ObservationMethod",
                "usyspermittype",                 "PermitType", 
                "usyspreparatiocontainertype",    "PrepContainertype",
                "usyspreparatiomedium",           "PreparatioMedium", 
                //"usyspreparatiopreparationtype",  "PreparationType", 
                "usysshipmentshipmentmethod",     "ShipmentMethod",
                "usyslocalitylatlongmethod",      "LatLonMethod" };

        setProcess(0, tables.length);

        for (int i = 0; i < tables.length; i++)
        {
            setDesc("Converting " + tables[i]);

            setProcess(i);

            boolean status = convertUSYSToPicklist(localSession, collection, tables[i], tables[i + 1]);
            if (!status)
            {
                log.error(tables[i] + " failed to convert.");
                return false;
            }
            i++;
        }
        DataBuilder.setSession(cachedSession);

        setProcess(tables.length);
        return true;
    }
    
    /**
     * Creates a map from a String Preparation Type to its ID in the table.
     * @return map of name to PrepType
     */
    public Map<String, PrepType> createPreparationTypesFromUSys(final Collection collection)
    {
        //deleteAllRecordsFromTable("preptype", BasicSQLUtils.myDestinationServerType);
        
        log.debug("Creating PrepTypes for Collection: " + collection.getCollectionName());

        Hashtable<String, PrepType> prepTypeMapper = new Hashtable<String, PrepType>();

            
        CollectionInfo colInfo = CollectionInfo.getCollectionObjectTypeForNewCollection(collection);
        if (colInfo == null)
        {
            throw new RuntimeException("Couldn't locate a CollectionInfo for collection: "+collection.getId());
        }
        
        String sql = "select preparationmethod FROM usyscollobjprepmeth pt inner join usysmetafieldsetsubtype st on st.fieldsetsubtypeid = pt.fieldsetsubtypeid " +
                        "inner join collectionobjecttype ct1 on ct1.collectionobjecttypeid = st.fieldvalue " +
                        "inner join collectionobjecttype ct on ct.collectionobjecttypename = replace(ct1.collectionobjecttypename, ' Preparation', '') " +
                        "inner join catalogseriesdefinition csd on csd.objecttypeid = ct.collectionobjecttypeid " +
                        "inner join catalogseries cs on cs.catalogseriesid = csd.catalogseriesid " +
                        "WHERE csd.catalogseriesid = " + colInfo.getCatSeriesId() +"  GROUP BY preparationmethod";

        log.info(sql);
        
        boolean   foundMisc = false;
        int       count     = 0;
        Vector<Object> list = BasicSQLUtils.querySingleCol(oldDBConn, sql);
        if (list.size() > 0)
        {
            for (Object nameObj : list)
            {
                String   name     = nameObj.toString();
                log.debug("Creating prep type["+name+"] for collection "+ collection.getCollectionName());
                PrepType prepType = AttrUtils.loadPrepType(name, collection);
                prepTypeMapper.put(name.toLowerCase(), prepType);
                if (name.equalsIgnoreCase("misc"))
                {
                    foundMisc = true;
                }
                count++;
            }
        }

        if (!foundMisc)
        {
            String name = "Misc";
            PrepType prepType = AttrUtils.loadPrepType(name, collection);
            prepTypeMapper.put(name.toLowerCase(), prepType);
            count++;
        }
        
        log.info("Processed PrepType " + count + " records.");


        return prepTypeMapper;
    }

    /**
     * Convert the column name
     * @param name the name
     * @return the converted name
     */
    protected String convertColumnName(final String name)
    {
        StringBuilder nameStr = new StringBuilder();
        int cnt = 0;
        for (char c : name.toCharArray())
        {
            if (cnt == 0)
            {
                nameStr.append(name.toUpperCase().charAt(0));
                cnt++;

            } else if (c < 'a')
            {
                nameStr.append(' ');
                nameStr.append(c);

            } else
            {
                nameStr.append(c);
            }
        }
        return nameStr.toString();
    }

    /**
     * Returns the proper value depending on the type
     * @param value the data value from the database object
     * @param type the defined type
     * @param attr the data value from the database object
     * @return the data object for the value
     */
    protected Object getData(final AttributeIFace.FieldType type, AttributeIFace attr)
    {
        if (type == AttributeIFace.FieldType.BooleanType)
        {
            return attr.getDblValue() != 0.0;

        } else if (type == AttributeIFace.FieldType.FloatType)
        {
            return attr.getDblValue().floatValue();

        } else if (type == AttributeIFace.FieldType.DoubleType)
        {
            return attr.getDblValue();

        } else if (type == AttributeIFace.FieldType.IntegerType)
        {
            return attr.getDblValue().intValue();

        } else
        {
            return attr.getStrValue();
        }
    }

    /**
     * Returns a converted value from the old schema to the new schema
     * @param rs the resultset
     * @param index the index of the column in the resultset
     * @param type the defined type for the new schema
     * @param metaData the metat data describing the old schema column
     * @return the new data object
     */
    protected Object getData(final ResultSet rs,
                             final int index,
                             final AttributeIFace.FieldType type,
                             final FieldMetaData metaData)
    {
        // Note: we need to check the old schema once again because the "type" may have been mapped
        // so now we must map the actual value

        AttributeIFace.FieldType oldType = getDataType(metaData.getName(), metaData.getType());

        try
        {
            Object value = rs.getObject(index);

            if (type == AttributeIFace.FieldType.BooleanType)
            {
                if (value == null)
                {
                    return false;

                } else if (oldType == AttributeIFace.FieldType.IntegerType)
                {
                    return rs.getInt(index) != 0;

                } else if (oldType == AttributeIFace.FieldType.FloatType)
                {
                    return rs.getFloat(index) != 0.0f;

                } else if (oldType == AttributeIFace.FieldType.DoubleType)
                {
                    return rs.getDouble(index) != 0.0;

                } else if (oldType == AttributeIFace.FieldType.StringType) { return rs.getString(
                        index).equalsIgnoreCase("true"); }
                log.error("Error maping from schema[" + metaData.getType() + "] to ["
                        + type.toString() + "]");
                return false;

            } else if (type == AttributeIFace.FieldType.FloatType)
            {
                if (value == null)
                {
                    return 0.0f;

                } else if (oldType == AttributeIFace.FieldType.FloatType)
                {
                    return rs.getFloat(index);

                } else if (oldType == AttributeIFace.FieldType.DoubleType) { return rs.getFloat(index); }
                log.error("Error maping from schema[" + metaData.getType() + "] to ["
                        + type.toString() + "]");
                return 0.0f;

            } else if (type == AttributeIFace.FieldType.DoubleType)
            {
                if (value == null)
                {
                    return 0.0;

                } else if (oldType == AttributeIFace.FieldType.FloatType)
                {
                    return rs.getDouble(index);

                } else if (oldType == AttributeIFace.FieldType.DoubleType) { return rs
                        .getDouble(index); }
                log.error("Error maping from schema[" + metaData.getType() + "] to ["
                        + type.toString() + "]");
                return 0.0;

            } else if (type == AttributeIFace.FieldType.IntegerType)
            {
                if (value == null)
                {
                    return 0;

                } else if (oldType == AttributeIFace.FieldType.IntegerType) { return rs
                        .getInt(index) != 0; }
                log.error("Error maping from schema[" + metaData.getType() + "] to ["
                        + type.toString() + "]");
                return 0;

            } else
            {
                return rs.getString(index);
            }
        } catch (SQLException ex)
        {
            log.error("Error maping from schema[" + metaData.getType() + "] to [" + type.toString()
                    + "]");
            log.error(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sets a converted value from the old schema to the new schema into the CollectionObjectAttr
     * object
     * @param rs the resultset
     * @param index the index of the column in the resultset
     * @param type the defined type for the new schema
     * @param metaData the metat data describing the old schema column
     * @param colObjAttr the object the data is set into
     * @return the new data object
     */
    protected void setData(final ResultSet rs,
                           final int index,
                           final AttributeIFace.FieldType type,
                           final FieldMetaData metaData,
                           final CollectionObjectAttr colObjAttr)
    {
        // Note: we need to check the old schema once again because the "type" may have been mapped
        // so now we must map the actual value

        AttributeIFace.FieldType oldType = getDataType(metaData.getName(), metaData.getType());

        try
        {
            Object value = rs.getObject(index);

            if (type == AttributeIFace.FieldType.BooleanType)
            {
                if (value == null)
                {
                    colObjAttr.setDblValue(0.0); // false

                } else if (oldType == AttributeIFace.FieldType.IntegerType)
                {
                    colObjAttr.setDblValue(rs.getInt(index) != 0 ? 1.0 : 0.0);

                } else if (oldType == AttributeIFace.FieldType.FloatType)
                {
                    colObjAttr.setDblValue(rs.getFloat(index) != 0.0f ? 1.0 : 0.0);

                } else if (oldType == AttributeIFace.FieldType.DoubleType)
                {
                    colObjAttr.setDblValue(rs.getDouble(index) != 0.0 ? 1.0 : 0.0);

                } else if (oldType == AttributeIFace.FieldType.StringType)
                {
                    colObjAttr
                            .setDblValue(rs.getString(index).equalsIgnoreCase("true") ? 1.0 : 0.0);
                } else
                {
                    log.error("Error maping from schema[" + metaData.getType() + "] to ["
                            + type.toString() + "]");
                }

            } else if (type == AttributeIFace.FieldType.IntegerType
                    || type == AttributeIFace.FieldType.DoubleType
                    || type == AttributeIFace.FieldType.FloatType)
            {
                if (value == null)
                {
                    colObjAttr.setDblValue(0.0);

                } else if (oldType == AttributeIFace.FieldType.IntegerType)
                {
                    colObjAttr.setDblValue((double)rs.getInt(index));

                } else if (oldType == AttributeIFace.FieldType.FloatType)
                {
                    colObjAttr.setDblValue((double)rs.getFloat(index));

                } else if (oldType == AttributeIFace.FieldType.DoubleType)
                {
                    colObjAttr.setDblValue(rs.getDouble(index));

                } else
                {
                    log.error("Error maping from schema[" + metaData.getType() + "] to ["
                            + type.toString() + "]");
                }

            } else
            {
                colObjAttr.setStrValue(rs.getString(index));
            }
        } catch (SQLException ex)
        {
            log.error("Error maping from schema[" + metaData.getType() + "] to [" + type.toString()
                    + "]");
            log.error(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Figure out the data type given the database column's field's name and data type
     * @param name the column name
     * @param type the database schema type for the column
     * @return the AttributeIFace.Type for the column
     */
    protected AttributeIFace.FieldType getDataType(final String name, final String type)
    {
        if (name.startsWith("YesNo"))
        {
            return AttributeIFace.FieldType.BooleanType;

            // } else if (name.equalsIgnoreCase("remarks"))
            // {
            // return AttributeIFace.FieldType.MemoType;

        } else if (type.equalsIgnoreCase("float"))
        {
            return AttributeIFace.FieldType.FloatType;

        } else if (type.equalsIgnoreCase("double"))
        {
            return AttributeIFace.FieldType.DoubleType;

        } else if (type.startsWith("varchar") || type.startsWith("text")
                || type.startsWith("longtext"))
        {
            return AttributeIFace.FieldType.StringType;

        } else
        {
            return AttributeIFace.FieldType.IntegerType;
        }
    }

    /**
     * @param data
     * @param fromTableName
     * @param oldColName
     * @return
     */
    protected Object getMappedId(final Object data,
                                 final String fromTableName,
                                 final String oldColName)
    {
        if (idMapperMgr != null && oldColName.endsWith("ID"))
        {

            IdMapperIFace idMapper = idMapperMgr.get(fromTableName, oldColName);
            if (idMapper != null) { return idMapper.get((Integer)data); }
            // else

            // throw new RuntimeException("No Map for ["+fromTableName+"]["+oldMappedColName+"]");
            if (!oldColName.equals("MethodID") && !oldColName.equals("RoleID")
                    && !oldColName.equals("CollectionID") && !oldColName.equals("ConfidenceID")
                    && !oldColName.equals("TypeStatusNameID")
                    && !oldColName.equals("ObservationMethodID"))
            {
                log.debug("No Map for [" + fromTableName + "][" + oldColName + "]");
            }
        }
        return data;
    }

    /**
     * Convert all the biological attributes to Collection Object Attributes. Each old record may
     * end up being multiple records in the new schema. This will first figure out which columns in
     * the old schema were used and only map those columns to the new database.<br>
     * <br>
     * It also will use the old name if there is not mapping for it. The old name is converted from
     * lower/upper case to be space separated where each part of the name starts with a capital
     * letter.
     * 
     * @param discipline the Discipline
     * @param colToNameMap a mape for old names to new names
     * @param typeMap a map for changing the type of the data (meaning an old value may be a boolean
     *            stored in a float)
     * @return true for success
     */
    public boolean convertBiologicalAttrs(final Discipline discipline, 
                                          @SuppressWarnings("unused") final Map<String, String> colToNameMap, 
                                          final Map<String, Short> typeMap)
    {
        AttributeIFace.FieldType[] attrTypes = { AttributeIFace.FieldType.IntegerType,
                AttributeIFace.FieldType.FloatType, AttributeIFace.FieldType.DoubleType,
                AttributeIFace.FieldType.BooleanType, AttributeIFace.FieldType.StringType,
        // AttributeIFace.FieldType.MemoType
        };

        Session localSession = HibernateUtil.getCurrentSession();

        deleteAllRecordsFromTable(newDBConn, "collectionobjectattr", BasicSQLUtils.myDestinationServerType);
        deleteAllRecordsFromTable(newDBConn, "attributedef", BasicSQLUtils.myDestinationServerType);

        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);

            // grab the field and their type from the old schema
            List<FieldMetaData> oldFieldMetaData = new ArrayList<FieldMetaData>();
            Map<String, FieldMetaData> oldFieldMetaDataMap = getFieldMetaDataFromSchemaHash(oldDBConn, "biologicalobjectattributes");

            // create maps to figure which columns where used
            List<String> columnsInUse = new ArrayList<String>();
            Map<String, AttributeDef> attrDefs = new Hashtable<String, AttributeDef>();

            List<Integer> counts = new ArrayList<Integer>();

            int totalCount = 0;

            for (FieldMetaData md : oldFieldMetaData)
            {
                // Skip these fields
                if (md.getName().indexOf("ID") == -1 && md.getName().indexOf("Timestamp") == -1
                        && md.getName().indexOf("LastEditedBy") == -1)
                {
                    oldFieldMetaDataMap.put(md.getName(), md); // add to map for later

                    // log.info(convertColumnName(md.getName())+" "+ md.getType());
                    String sqlStr = "select count(" + md.getName()
                            + ") from biologicalobjectattributes where " + md.getName()
                            + " is not null";
                    ResultSet rs = stmt.executeQuery(sqlStr);
                    if (rs.first() && rs.getInt(1) > 0)
                    {
                        int rowCount = rs.getInt(1);
                        totalCount += rowCount;
                        counts.add(rowCount);

                        log.info(md.getName() + " has " + rowCount + " rows of values");

                        columnsInUse.add(md.getName());
                        AttributeDef attrDef = new AttributeDef();

                        String newName = convertColumnName(md.getName());
                        attrDef.setFieldName(newName);
                        log.debug("mapping[" + newName + "][" + md.getName() + "]");

                        // newNameToOldNameMap.put(newName, md.getName());

                        short dataType = -1;
                        if (typeMap != null)
                        {
                            Short type = typeMap.get(md.getName());
                            if (type == null)
                            {
                                dataType = type;
                            }
                        }

                        if (dataType == -1)
                        {
                            dataType = getDataType(md.getName(), md.getType()).getType();
                        }

                        attrDef.setDataType(dataType);
                        attrDef.setDiscipline(discipline);
                        attrDef.setTableType(GenericDBConversion.TableType.CollectionObject
                                .getType());
                        attrDef.setTimestampCreated(now);

                        attrDefs.put(md.getName(), attrDef);

                        try
                        {
                            HibernateUtil.beginTransaction();
                            localSession.save(attrDef);
                            HibernateUtil.commitTransaction();

                        } catch (Exception e)
                        {
                            log.error("******* " + e);
                            HibernateUtil.rollbackTransaction();
                            throw new RuntimeException(e);
                        }

                    }
                    rs.close();
                }
            } // for
            log.info("Total Number of Attrs: " + totalCount);

            // Now that we know which columns are being used we can start the conversion process

            log.info("biologicalobjectattributes columns in use: " + columnsInUse.size());
            if (columnsInUse.size() > 0)
            {
                int inx = 0;
                StringBuilder str = new StringBuilder("select BiologicalObjectAttributesID");
                for (String name : columnsInUse)
                {
                    str.append(", ");
                    str.append(name);
                    inx++;
                }

                str.append(" from biologicalobjectattributes order by BiologicalObjectAttributesID");
                log.info("sql: " + str.toString());
                ResultSet rs = stmt.executeQuery(str.toString());

                int[] countVerify = new int[counts.size()];
                for (int i = 0; i < countVerify.length; i++)
                {
                    countVerify[i] = 0;
                }
                boolean useHibernate = false;
                StringBuilder strBufInner = new StringBuilder();
                int recordCount = 0;
                while (rs.next())
                {

                    if (useHibernate)
                    {
                        Criteria criteria = localSession.createCriteria(CollectionObject.class);
                        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
                        criteria.add(Restrictions.eq("collectionObjectId", rs.getInt(1)));
                        List<?> list = criteria.list();
                        if (list.size() == 0)
                        {
                            log.error("**** Can't find the CollectionObject " + rs.getInt(1));
                        } else
                        {
                            CollectionObject colObj = (CollectionObject)list.get(0);

                            inx = 2; // skip the first column (the ID)
                            for (String name : columnsInUse)
                            {
                                AttributeDef attrDef = attrDefs.get(name); // the needed
                                                                            // AttributeDef by name
                                FieldMetaData md = oldFieldMetaDataMap.get(name);

                                // Create the new Collection Object Attribute
                                CollectionObjectAttr colObjAttr = new CollectionObjectAttr();
                                colObjAttr.setCollectionObject(colObj);
                                colObjAttr.setDefinition(attrDef);
                                colObjAttr.setTimestampCreated(now);

                                // String oldName = newNameToOldNameMap.get(attrDef.getFieldName());
                                // log.debug("["+attrDef.getFieldName()+"]["+oldName+"]");

                                // log.debug(inx+" "+attrTypes[attrDef.getDataType()]+"
                                // "+md.getName()+" "+md.getType());
                                setData(rs, inx, attrTypes[attrDef.getDataType()], md, colObjAttr);

                                HibernateUtil.beginTransaction();
                                localSession.save(colObjAttr);
                                HibernateUtil.commitTransaction();

                                inx++;
                                if (recordCount % 2000 == 0)
                                {
                                    log.info("CollectionObjectAttr Records Processed: "
                                            + recordCount);
                                }
                                recordCount++;
                            } // for
                            // log.info("Done - CollectionObjectAttr Records Processed:
                            // "+recordCount);
                        }
                    } else
                    {
                        inx = 2; // skip the first column (the ID)
                        for (String name : columnsInUse)
                        {
                            AttributeDef attrDef = attrDefs.get(name); // the needed AttributeDef
                                                                        // by name
                            FieldMetaData md = oldFieldMetaDataMap.get(name);

                            if (rs.getObject(inx) != null)
                            {
                                Integer newRecId = (Integer)getMappedId(rs.getInt(1), "biologicalobjectattributes", "BiologicalObjectAttributesID");

                                Object data = getData(rs, inx, attrTypes[attrDef.getDataType()], md);
                                boolean isStr = data instanceof String;

                                countVerify[inx - 2]++;

                                strBufInner.setLength(0);
                                strBufInner.append("INSERT INTO collectionobjectattr VALUES (");
                                strBufInner.append("NULL");// Integer.toString(recordCount));
                                strBufInner.append(",");
                                strBufInner.append(getStrValue(isStr ? data : null));
                                strBufInner.append(",");
                                strBufInner.append(getStrValue(isStr ? null : data));
                                strBufInner.append(",");
                                strBufInner.append(getStrValue(now));
                                strBufInner.append(",");
                                strBufInner.append(getStrValue(now));
                                strBufInner.append(",");
                                strBufInner.append(newRecId.intValue());
                                strBufInner.append(",");
                                strBufInner.append(getStrValue(attrDef.getAttributeDefId()));
                                strBufInner.append(")");

                                try
                                {
                                    Statement updateStatement = newDBConn.createStatement();
                                    // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                                    removeForeignKeyConstraints(newDBConn, BasicSQLUtils.myDestinationServerType);
                                    if (false)
                                    {
                                        log.debug(strBufInner.toString());
                                    }
                                    updateStatement.executeUpdate(strBufInner.toString());
                                    updateStatement.clearBatch();
                                    updateStatement.close();
                                    updateStatement = null;

                                } catch (SQLException e)
                                {
                                    log.error(strBufInner.toString());
                                    log.error("Count: " + recordCount);
                                    e.printStackTrace();
                                    log.error(e);
                                    throw new RuntimeException(e);
                                }

                                if (recordCount % 2000 == 0)
                                {
                                    log.info("CollectionObjectAttr Records Processed: " + recordCount);
                                }
                                recordCount++;
                            }
                            inx++;
                        } // for
                    } // if
                } // while
                rs.close();
                stmt.close();

                log.info("Count Verification:");
                for (int i = 0; i < counts.size(); i++)
                {
                    log.info(columnsInUse.get(i) + " [" + counts.get(i) + "][" + countVerify[i] + "] " + (counts.get(i) - countVerify[i]));
                }
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Converts all the CollectionObject Physical records and CollectionObjectCatalog Records into
     * the new schema Preparation table.
     * @return true if no errors
     */
    public boolean convertLoanRecords(final boolean doingGifts)
    {
    	String newTableName = doingGifts ? "gift" : "loan";
        setIdentityInsertONCommandForSQLServer(newDBConn, newTableName, BasicSQLUtils.myDestinationServerType);

        deleteAllRecordsFromTable(newDBConn, newTableName, BasicSQLUtils.myDestinationServerType); // automatically closes the connection

        if (getNumRecords(oldDBConn, "loan") == 0) 
        { 
            return true; 
        }
        
        String[] ignoredFields = { "SpecialConditions", "AddressOfRecordID", 
					                "DateReceived", "ReceivedComments", "PurposeOfLoan", "OverdueNotiSetDate",
					                "IsFinancialResponsibility", "Version", "CreatedByAgentID",
					                "IsFinancialResponsibility", "SrcTaxonomy", "SrcGeography",
					                "CollectionMemberID", 
					                "PurposeOfGift", "IsFinancialResponsibility", "SpecialConditions", 
					                "ReceivedComments", "AddressOfRecordID"
					             };
        
        Hashtable<String, Boolean> fieldToSkip = new Hashtable<String, Boolean>();
        for (String nm : ignoredFields)
        {
        	fieldToSkip.put(nm, true);
        }
        
        IdTableMapper loanIdMapper = (IdTableMapper)idMapperMgr.get(newTableName, doingGifts ? "GiftID" : "LoanID");
        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            StringBuilder str = new StringBuilder();

            List<String> oldFieldNames = getFieldNamesFromSchema(oldDBConn, "loan");

            StringBuilder sql   = new StringBuilder("SELECT ");
            sql.append(buildSelectFieldList(oldFieldNames, "loan"));
            sql.append(" FROM loan WHERE loan.Category = ");
            sql.append(doingGifts ? "1" : "0");
            sql.append(" ORDER BY loan.LoanID");
            log.info(sql);
            
            List<FieldMetaData> newFieldMetaData = getFieldMetaDataFromSchema(newDBConn, newTableName);
            log.info("Number of Fields in New " + newTableName + " " + newFieldMetaData.size());
            String sqlStr = sql.toString();
            
            if (doingGifts && loanIdMapper == null)
            {
                StringBuilder mapSQL = new StringBuilder("SELECT LoanID FROM loan WHERE loan.Category = ");
                mapSQL.append(doingGifts ? "1" : "0");
                mapSQL.append(" ORDER BY loan.LoanID");
                log.info(mapSQL.toString());
                
                BasicSQLUtils.deleteAllRecordsFromTable(oldDBConn, "gift_GiftID", BasicSQLUtils.myDestinationServerType);
                loanIdMapper = new IdTableMapper(newTableName, "GiftID",  mapSQL.toString(), false, false);
                idMapperMgr.addMapper(loanIdMapper);
                loanIdMapper.mapAllIdsWithSQL();
            }


            Map<String, Integer> oldNameIndex = new Hashtable<String, Integer>();
            int inx = 1;
            for (String name : oldFieldNames)
            {
                oldNameIndex.put(name, inx++);
            }
            
            Map<String, String> colNewToOldMap = doingGifts ? createFieldNameMap(new String[] { "GiftNumber", "LoanNumber", "GiftDate", "LoanDate", "IsCurrent", "Current", "IsClosed", "Closed"}) :
                                                              createFieldNameMap(new String[] { "IsCurrent", "Current", "IsClosed", "Closed", });

            log.info(sqlStr);
            ResultSet rs = stmt.executeQuery(sqlStr);

            if (hasFrame)
            {
                if (rs.last())
                {
                    setProcess(0, rs.getRow());
                    rs.first();

                } else
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            } else
            {
                if (!rs.first())
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            }

            PartialDateConv partialDateConv = new PartialDateConv();
            
            int lastEditedByInx = oldNameIndex.get("LastEditedBy");
            
            int count = 0;
            do
            {
                partialDateConv.nullAll();
                
                str.setLength(0);
                StringBuffer fieldList = new StringBuffer();
                fieldList.append("( ");
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if ((i > 0) && (i < newFieldMetaData.size()))
                    {
                        fieldList.append(", ");
                    }
                    String newFieldName = newFieldMetaData.get(i).getName();
                    fieldList.append(newFieldName);
                }
                
                fieldList.append(")");
                
                str.append("INSERT INTO " + newTableName + " " + fieldList + " VALUES (");
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if (i > 0)
                    {
                        str.append(", ");
                    }

                    String newFieldName = newFieldMetaData.get(i).getName();

                    if (i == 0)
                    {
                        Integer oldID = rs.getInt(1);
                        Integer newID = loanIdMapper.get(oldID);
                        if (newID != null)
                        {
                            str.append(getStrValue(newID));
                        } else
                        {
                            log.error(newTableName+" Old/New ID problem ["+oldID+"]["+newID+"]");
                        }

                    } else if (newFieldName.equals("Version")) // User/Security changes
                    {
                        str.append("0");

                    } else if (newFieldName.equals("CreatedByAgentID")) // User/Security changes
                    {
                        str.append(getCreatorAgentId(null));

                    } else if (newFieldName.equals("ModifiedByAgentID")) // User/Security changes
                    {
                        String lastEditedByStr = rs.getString(lastEditedByInx);
                        str.append(getModifiedByAgentId(lastEditedByStr));

                    } else if (fieldToSkip.get(newFieldName) != null)
                    {
                        str.append("NULL");

                    } else if (newFieldName.equals("DisciplineID")) // User/Security changes
                    {
                        str.append(curDisciplineID);

                    } else if (newFieldName.equals("DivisionID")) // User/Security changes
                    {
                        str.append(curDivisionID);

                    } else
                    {
                        Integer index = null;
                        String oldMappedColName = colNewToOldMap.get(newFieldName);
                        if (oldMappedColName != null)
                        {
                            index = oldNameIndex.get(oldMappedColName);

                        } else
                        {
                            index = oldNameIndex.get(newFieldName);
                            oldMappedColName = newFieldName;
                        }

                        Object data;
                        if (index == null)
                        {
                            String msg = "convertLoanRecords - Couldn't find new field name[" + newFieldName  + "] in old field name in index Map";
                            log.warn(msg);
//                            stmt.close();
//                            throw new RuntimeException(msg);
                            data = null;
                        } else {

                            data = rs.getObject(index);
                        }
                        if (data != null)
                        {
                            int idInx = newFieldName.lastIndexOf("ID");
                            if (idMapperMgr != null && idInx > -1)
                            {
                            	IdMapperIFace idMapper = idMapperMgr.get("loan", oldMappedColName);
                                if (idMapper != null)
                                {
                                    data = idMapper.get(rs.getInt(index));
                                } else
                                {
                                    log.error("No Map for [" + "loan" + "][" + oldMappedColName + "]");
                                }
                            }
                        }

                        // hack for ??bug?? found in Sp5 that inserted null values in
                        // timestampmodified field of determination table?
                        BasicSQLUtils.fixTimestamps(newFieldName, newFieldMetaData.get(i).getType(), data, str);
                    }
                }
                str.append(")");

                if (hasFrame)
                {
                    if (count % 500 == 0)
                    {
                        setProcess(count);
                    }

                } else
                {
                    if (count % 2000 == 0)
                    {
                        log.info("Loan/Gifts Records: " + count);
                    }
                }

                try
                {
                    //log.debug(str.toString());
                    Statement updateStatement = newDBConn.createStatement();
                    // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                    updateStatement.executeUpdate(str.toString());
                    updateStatement.clearBatch();
                    updateStatement.close();
                    updateStatement = null;

                } catch (SQLException e)
                {
                    log.error("Count: " + count);
                    log.error("Exception on insert: " + str.toString());
                    e.printStackTrace();
                    log.error(e);
                    rs.close();
                    stmt.close();
                    throw new RuntimeException(e);
                }

                count++;
                // if (count > 10) break;
            } while (rs.next());

            if (hasFrame)
            {
                setProcess(count);
            } else
            {
                log.info("Processed Loan/Gift " + count + " records.");
            }
            rs.close();

            stmt.close();

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }

        setIdentityInsertOFFCommandForSQLServer(newDBConn, "determination", BasicSQLUtils.myDestinationServerType);

        return true;
    }

    /**
     * Converts all the CollectionObject Physical records and CollectionObjectCatalog Records into
     * the new schema Preparation table.
     * @return true if no errors
     */
    public boolean convertLoanAgentRecords(final boolean doingGifts)
    {
        TableWriter tblWriter = convLogger.getWriter("convert"+(doingGifts ? "Gift" : "Loan")+".html", (doingGifts ? "Gifts" : "Loans"));

    	String newTableName = doingGifts ? "giftagent"   : "loanagent";
        String idName       = doingGifts ? "GiftAgentID" : "LoanAgentID";
        String refName      = doingGifts ? "GiftID"      : "LoanID";

        setIdentityInsertONCommandForSQLServer(newDBConn, newTableName, BasicSQLUtils.myDestinationServerType);

        deleteAllRecordsFromTable(newDBConn, newTableName, BasicSQLUtils.myDestinationServerType); // automatically closes the connection

        if (getNumRecords(oldDBConn, "loanagents") == 0) 
        { 
            return true; 
        }

        try
        {
            IdMapperIFace agentAddrIDMapper = idMapperMgr.get("agentaddress", "AgentAddressID");

        	IdMapperIFace loanMapper  = null;
        	if (doingGifts)
        	{
        	    IdTableMapper idMapper = new IdTableMapper(newTableName, "GiftID",  "SELECT LoanID FROM loan WHERE Category = 1 ORDER BY LoanID", true, false);
                idMapperMgr.addMapper(idMapper);
                if (shouldCreateMapTables)
                {
                    idMapper.mapAllIdsWithSQL();
                }
                
        		/*IdTableMapper idMapper = new IdTableMapper("loan", "LoanID", "SELECT LoanID FROM loan WHERE Category = 1 ORDER BY LoanID"); // Gifts
                if (shouldCreateMapTables)
                {
                    idMapper.mapAllIdsWithSQL();
                }*/

                loanMapper = idMapper;
                
        	} else
        	{
        		loanMapper = idMapperMgr.get("loan", "LoanID"); // Loans
        	}
        	
            StringBuilder str = new StringBuilder();
            
            StringBuilder cntSQL = new StringBuilder("SELECT count(*) FROM loanagents la INNER JOIN loan l ON la.LoanID = l.LoanID WHERE l.Category = ");
            cntSQL.append(doingGifts ? "1" : "0");
            
            Integer totalCnt = getCount(oldDBConn, cntSQL.toString());
            if (totalCnt == null || totalCnt == 0)
            {
            	return true;
            }

            StringBuilder sql = new StringBuilder("SELECT la.LoanAgentsID, la.Role, la.Remarks, la.LoanID, la.AgentAddressID, la.TimestampCreated, la.TimestampModified ");
            sql.append("FROM loanagents la INNER JOIN loan l ON la.LoanID = l.LoanID WHERE l.Category = ");
            sql.append(doingGifts ? "1" : "0");
            sql.append(" ORDER BY l.LoanID");
            
            String sqlStr = sql.toString();
            log.info(sqlStr);
            
            Statement stmt = oldDBConn.createStatement();//ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs   = stmt.executeQuery(sqlStr);

            if (hasFrame)
            {
            	setProcess(0, totalCnt);
            }

            Statement updateStatement = newDBConn.createStatement();

            int count = 0;
            while (rs.next())
            {
            	Integer   id          = rs.getInt(1);
                String    role        = rs.getString(2);
                String    remarks     = escapeStringLiterals(rs.getString(3));
            	Integer   loadId      = rs.getInt(4);
            	Integer   agentAddrId = rs.getInt(5);
               	Timestamp timestampCr = rs.getTimestamp(6);
               	Timestamp timestampMd = rs.getTimestamp(7);
                           	
            	Integer newId      = count+1;
            	Integer newLoanId  = loanMapper.get(loadId);
            	Integer newAgentId = agentAddrIDMapper.get(agentAddrId);
            	
            	if (newLoanId == null)
            	{
            		tblWriter.logError("The new Loan Id mapped from ["+newLoanId+"] was not found in the mappers. Skipping LoanAgent Record: "+id);
            		continue;
            	}
            	
            	if (newAgentId == null)
            	{
            		tblWriter.logError("The new Agent Id mapped from ["+agentAddrId+"] was not found in the mapper. Skipping LoanAgent Record: "+id);
            		continue;
            	}
            	
            	
            	String tsStr = timestampCr == null ? nowStr : dateTimeFormatter.format(timestampMd);
            	String tmStr = timestampMd == null ? nowStr : dateTimeFormatter.format(timestampCr);
            	
            	String insertSQLFmt = "INSERT INTO %s (%s, Role, Remarks, %s, AgentID, TimestampCreated, TimestampModified, Version, DisciplineID) VALUES(%d, '%s', '%s', %d, %d, '%s', '%s', 0, %d)";
            	String insertSql    = String.format(insertSQLFmt, newTableName, idName, refName, newId, role, remarks, newLoanId, newAgentId, tsStr, tmStr, getDisciplineId());

                if (hasFrame)
                {
                    if (count % 10 == 0)
                    {
                        setProcess(count);
                    }

                } else
                {
                    if (count % 10 == 0)
                    {
                        log.info("Loan Agent Records: " + count);
                    }
                }

                try
                {
                    String chkSQL = String.format("SELECT COUNT(*) FROM %s WHERE %s = %d AND Role = '%s' AND AgentID = %d", newTableName, refName, newLoanId, role, newAgentId);
                    //System.err.println(chkSQL);
                    int cnt = BasicSQLUtils.getCountAsInt(chkSQL);
                    if (cnt < 1)
                    {
                        updateStatement.executeUpdate(insertSql);
                    } else
                    {
                        String errStr = String.format("Duplciate key Tbl: %s WHERE %s = %d Role = '%s' AND AgentID = %d", newTableName, refName, newLoanId, role, newAgentId);
                        log.error(errStr);
                        tblWriter.log(errStr);
                    }

                } catch (SQLException e)
                {
                    log.error("Count: " + count);
                    log.error("Exception on insert: " + str.toString());
                    e.printStackTrace();
                    log.error(e);
                    rs.close();
                	updateStatement.close();
                    stmt.close();
                    throw new RuntimeException(e);
                    
                }

                count++;
            }

            if (hasFrame)
            {
                setProcess(count);
            } else
            {
                log.info("Processed LoanAgents " + count + " records.");
            }
            rs.close();
            stmt.close();
        	updateStatement.close();

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }

        setIdentityInsertOFFCommandForSQLServer(newDBConn, "loanagents", BasicSQLUtils.myDestinationServerType);

        return true;
    }
    
    /**
     * 
     */
    public void convertHostTaxonId()
    {
        int total = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) FROM habitat WHERE HostTaxonID IS NOT NULL");
        if (total > 0)
        {
            setProcess(0, total);
            
            String sql = "SELECT h.HabitatID, co.CollectionObjectID FROM habitat h " + 
                         "INNER JOIN collectingevent ce ON h.HabitatID = ce.CollectingEventID " +
                         "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID WHERE h.HostTaxonID IS NOT NULL";
            
            String lookupSql = "SELECT ca.CollectionObjectAttributeID FROM collectionobject co " + 
                               "INNER JOIN collectionobjectattribute ca ON co.CollectionObjectAttributeID = ca.CollectionObjectAttributeID WHERE co.CollectionObjectID";

            try
            {
                IdMapperIFace coMapper = IdMapperMgr.getInstance().get("collectionobject", "CollectionObjectID");
                IdMapperIFace txMapper = IdMapperMgr.getInstance().get("taxonname",        "TaxonNameID");
                
                Statement         stmt       = oldDBConn.createStatement();
                PreparedStatement updateStmt = newDBConn.prepareStatement("UPDATE collectionobjectattribute SET RelatedTaxonID=? WHERE CollectionObjectAttributeID = ?");
                PreparedStatement insertStmt = newDBConn.prepareStatement("INSERT INTO collectionobjectattribute (RelatedTaxonID) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
                
                int count = 0;
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next())
                {
                    setProcess(count++);
                    
                    boolean isErr = false;
                    Integer newTXId = txMapper.get(rs.getInt(1));
                    if (newTXId == null)
                    {
                        String msg = "Couldn't map old Taxon Id " + rs.getInt(1);
                        log.error(msg);
                        isErr = true;
                    }
                    Integer newCOId = coMapper.get(rs.getInt(2));
                    if (newCOId == null)
                    {
                        String msg = "Couldn't map old CO Id " + rs.getInt(2);
                        log.error(msg);
                        isErr = true;
                    }
                    
                    if (isErr)
                    {
                        continue;
                    }
                    
                    Integer coAttrId = BasicSQLUtils.getCount(newDBConn, lookupSql + newCOId);
                    if (coAttrId == null)
                    {
                        insertStmt.setInt(1, newTXId);
                        
                        if (insertStmt.executeUpdate() != 1)
                        {
                            String msg = "Error inserting CO Attr record for CO Id: " + newCOId + " and TxId: " + newTXId;
                            log.error(msg);
                        }
                        coAttrId = BasicSQLUtils.getInsertedId(insertStmt);
                    }
                    
                    updateStmt.setInt(1, newTXId);
                    updateStmt.setInt(2, newCOId); 
                    
                    if (updateStmt.executeUpdate() != 1)
                    {
                        String msg = "Error updating CO RelatedTaxonId record for CO Id: " + newCOId + " and TxId: " + newTXId;
                        log.error(msg);
                    }
                }
                
                rs.close();
                stmt.close();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    //-------------------------------------------------------------------------------------------------


    /**
     * Converts all the CollectionObject Physical records and CollectionObjectCatalog Records into
     * the new schema Preparation table.
     * @return true if no errors
     */
    public boolean convertPreparationRecords(final Hashtable<Integer, Map<String, PrepType>> collToPrepTypeHash)
    {
        TableWriter tblWriter = convLogger.getWriter("convertPreparations.html", "Preparations");

        deleteAllRecordsFromTable(newDBConn, "preparation", BasicSQLUtils.myDestinationServerType);
        
        TimeLogger timeLogger = new TimeLogger();
        
        // BasicSQLUtils.setIdentityInsertONCommandForSQLServer(newDBConn, "preparation",
        // BasicSQLUtils.myDestinationServerType);
        try
        {
            Statement     stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            StringBuilder str  = new StringBuilder();

            List<String> oldFieldNames = new ArrayList<String>();

            StringBuilder sql   = new StringBuilder("SELECT ");
            List<String>  names = getFieldNamesFromSchema(oldDBConn, "collectionobject");

            sql.append(buildSelectFieldList(names, "co"));
            sql.append(", ");
            oldFieldNames.addAll(names);

            names = getFieldNamesFromSchema(oldDBConn, "collectionobjectcatalog");
            sql.append(buildSelectFieldList(names, "cc"));
            oldFieldNames.addAll(names);

            String sqlPostfix = " FROM collectionobject co LEFT JOIN collectionobjectcatalog cc ON co.CollectionObjectID = cc.CollectionObjectCatalogID " +
                                "WHERE NOT (co.DerivedFromID IS NULL) AND CatalogSeriesID IS NOT NULL ORDER BY co.CollectionObjectID";
            sql.append(sqlPostfix);
            
            log.info(sql);

            List<FieldMetaData> newFieldMetaData = getFieldMetaDataFromSchema(newDBConn, "preparation");

            log.info("Number of Fields in (New) Preparation " + newFieldMetaData.size());
            for (FieldMetaData field : newFieldMetaData)
            {
                log.info(field.getName());
            }
            
            String sqlStr = sql.toString();
            log.debug(sql);
            
            log.debug("------------------------ Old Names");
            Map<String, Integer> oldNameIndex = new Hashtable<String, Integer>();
            int inx = 1;
            for (String name : oldFieldNames)
            {
                oldNameIndex.put(name, inx++);
                log.debug("OldName: " + name + " " + (inx - 1));
            }
            log.debug("------------------------");
            
            Hashtable<String, String> newToOld = new Hashtable<String, String>();
            newToOld.put("PreparationID",      "CollectionObjectID");
            newToOld.put("CollectionObjectID", "DerivedFromID");
            newToOld.put("StorageLocation",    "Location");

            boolean doDebug = false;
            ResultSet rs = stmt.executeQuery(sqlStr);

            if (!rs.next())
            {
                rs.close();
                stmt.close();
                setProcess(0, 0);
                return true;
            }

            Statement prepStmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            prepStmt.setFetchSize(Integer.MIN_VALUE);

            IdTableMapper prepIdMapper = idMapperMgr.addTableMapper("CollectionObject", "CollectionObjectID", doDeleteAllMappings);
            
            if (shouldCreateMapTables)
            {
                String sql2 = "SELECT c.CollectionObjectID FROM collectionobject c WHERE NOT (c.DerivedFromID IS NULL) ORDER BY c.CollectionObjectID";
                prepIdMapper.mapAllIds(sql2);
                
            } else
            {
                prepIdMapper = (IdTableMapper)idMapperMgr.get("preparation", "PreparationID");
            }
            
            String    insertStmtStr        = null;
            boolean   shouldCheckPrepAttrs = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM preparation WHERE PreparedByID IS NOT NULL OR PreparedDate IS NOT NULL") > 0;
            Statement prepTypeStmt         = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            prepTypeStmt.setFetchSize(Integer.MIN_VALUE);

            PartialDateConv partialDateConv = new PartialDateConv();
            
            prepIdMapper.setShowLogErrors(false);
            
            int totalPrepCount = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*)" + sqlPostfix);
            setProcess(0, totalPrepCount);
            
            Statement updateStatement = newDBConn.createStatement();
            
            //int     prepDateInx     = oldNameIndex.get("CatalogedDate") + 1;
            int     lastEditedByInx = oldNameIndex.get("LastEditedBy");
            Integer idIndex         = oldNameIndex.get("CollectionObjectID");
            Integer catSeriesIdInx  = oldNameIndex.get("CatalogSeriesID");
            int     count           = 0;
            do
            {
                partialDateConv.nullAll();
                
                Integer preparedById = null;
                if (shouldCheckPrepAttrs)
                {
                    Integer   recordId    = rs.getInt(idIndex + 1);
                    
                    String    subQueryStr = "select PreparedByID, PreparedDate from preparation where PreparationID = " + recordId;
                    ResultSet subQueryRS  = prepTypeStmt.executeQuery(subQueryStr);
                    
                    if (subQueryRS.next())
                    {
                        preparedById = subQueryRS.getInt(1);
                        getPartialDate(rs.getObject(2), partialDateConv);
                    } else
                    {
                        partialDateConv.setDateStr("NULL");
                        partialDateConv.setPartial("NULL");
                    }
                    subQueryRS.close();
                }
                
                Integer         catSeriesId      = rs.getInt(catSeriesIdInx);
                //log.debug("catSeriesId "+catSeriesId+"  catSeriesIdInx "+catSeriesIdInx);
                Vector<Integer> collectionIdList = catSeriesToNewCollectionID.get(catSeriesId);
                if (collectionIdList == null)
                {
                    //Integer colObjId = rs.getInt(idIndex);
                    throw new RuntimeException("There are no Collections mapped to CatSeriesId["+catSeriesId+"] (converting Preps)");
                }
                
                if (collectionIdList.size() == 0)
                {
                    UIRegistry.showError("There are NO Collections assigned to the same CatalogSeries and we can't handle that right now.");
                    return false;
                }
                
                if (collectionIdList.size() > 1)
                {
                    UIRegistry.showError("There are multiple Collections assigned to the same CatalogSeries and we can't handle that right now.");
                    return false;
                }
                
                Integer colId = collectionIdList.get(0);
                Collection collection = collIdToCollObj.get(colId);
                if (collection == null)
                {
                    Session localSession = HibernateUtil.getCurrentSession();
                    List<Collection> colList = (List<Collection>)localSession.createQuery("FROM Collection WHERE id = "+colId).list();
                    if (colList == null || colList.size() == 0)
                    {
                        UIRegistry.showError("The collection is null for Catalog Series ID: "+catSeriesId);
                        return false;
                    }
                    collection = colList.get(0);
                    collIdToCollObj.put(colId, collection);
                }
                Map<String, PrepType> prepTypeMap = collToPrepTypeHash.get(collectionIdList.get(0));

                String lastEditedBy = rs.getString(lastEditedByInx);

                /*
                 * int catNum = rs.getInt(oldNameIndex.get("CatalogNumber")+1); doDebug = catNum ==
                 * 30972;
                 * 
                 * if (doDebug) { log.debug("CatalogNumber "+catNum);
                 * log.debug("CollectionObjectID
                 * "+rs.getInt(oldNameIndex.get("CollectionObjectID")+1));
                 * log.debug("DerivedFromID
                 * "+rs.getInt(oldNameIndex.get("DerivedFromID"))); }
                 */

                str.setLength(0);

                if (insertStmtStr == null)
                {
                    StringBuffer fieldList = new StringBuffer();
                    fieldList.append("( ");
                    for (int i = 0; i < newFieldMetaData.size(); i++)
                    {
                        if ((i > 0) && (i < newFieldMetaData.size()))
                        {
                            fieldList.append(", ");
                        }
                        
                        String newFieldName = newFieldMetaData.get(i).getName();
                        fieldList.append(newFieldName + " ");
                    }
                    fieldList.append(")");
                    insertStmtStr = "INSERT INTO preparation " + fieldList + " VALUES (";
                }
                str.append(insertStmtStr);
                
                Integer oldId   = null;
                boolean isError = false;
                
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if (i > 0)
                    {
                        str.append(", ");
                    }

                    String newFieldName = newFieldMetaData.get(i).getName();
                    String mappedName   = newToOld.get(newFieldName);
                    //log.debug("["+newFieldName+"]["+mappedName+"]");
                    
                    if (mappedName != null)
                    {
                        newFieldName = mappedName;
                    } else
                    {
                        mappedName = newFieldName;
                    }

                    if (i == 0)
                    {
                        oldId = rs.getInt(1);
                        Integer newId = prepIdMapper.get(oldId);
                        if (newId == null)
                        {
                            isError = true;
                            break;
                            //throw new RuntimeException("Preparations - Couldn't find new ID for old ID["+oldId+"]");
                        }
                        str.append(newId);

                    } else if (newFieldName.equals("PreparedByID"))
                    {
                        if (agentIdMapper != null)
                        {
                            str.append(getStrValue(agentIdMapper.get(preparedById)));
                        } else
                        {
                            str.append("NULL");
                            //log.error("No Map for PreparedByID[" + preparedById + "]");
                        }

                    } else if (newFieldName.equals("PreparedDate"))
                    {
                        str.append(partialDateConv.getDateStr());

                    } else if (newFieldName.equals("PreparedDatePrecision"))
                    {
                        str.append(partialDateConv.getPartial());

                    } else if (newFieldName.equals("DerivedFromIDX"))
                    {
                        // skip

                    } else if (newFieldName.equals("PreparationAttributeID"))
                    {
                        Integer id   = rs.getInt(idIndex + 1);
                        Object  data = prepIdMapper.get(id);
                        if (data == null)
                        {
                            // throw new RuntimeException("Couldn't map ID for new
                            // PreparationAttributesID [CollectionObjectID]["+id+"]");
                            str.append("NULL");

                        } else
                        {
                            ResultSet prepRS = prepStmt.executeQuery("select PreparationID from preparation where PreparationID = " + id);
                            if (prepRS.first())
                            {
                                str.append(getStrValue(data));
                            } else
                            {
                                str.append("NULL");
                            }
                            prepRS.close();
                        }

                    } else if (newFieldName.equals("CountAmt"))
                    {
                        Integer value = rs.getInt("Count");
                        if (rs.wasNull())
                        {
                            value = null;
                        }
                        str.append(getStrValue(value));

                    } else if (newFieldName.equalsIgnoreCase("SampleNumber")
                            || newFieldName.equalsIgnoreCase("Status")
                            || newFieldName.equalsIgnoreCase("YesNo3")
                            || newFieldName.equalsIgnoreCase("alternateStorageId"))
                    {
                        str.append("NULL");

                    } else if (newFieldName.equalsIgnoreCase("Version"))
                    {
                        str.append("0");

                    } else if (newFieldName.equalsIgnoreCase("CollectionMemberID"))
                    {
                        str.append(getCollectionMemberId());

                    } else if (newFieldName.equalsIgnoreCase("TimestampCreated"))
                    {
                        Object value = rs.getString(oldNameIndex.get("TimestampCreated"));
                        if (value == null)
                        {
                            value = new Timestamp(Calendar.getInstance().getTime().getTime());
                        }
                        str.append(getStrValue(value, newFieldMetaData.get(i).getType()));

                    } else if (newFieldName.equalsIgnoreCase("TimestampModified"))
                    {
                        Object value = rs.getString(oldNameIndex.get("TimestampModified"));
                        if (value == null)
                        {
                            value = new Timestamp(Calendar.getInstance().getTime().getTime());
                        }
                        str.append(getStrValue(value, newFieldMetaData.get(i).getType()));

                    } else if (newFieldName.equalsIgnoreCase("ModifiedByAgentID"))
                    {
                        str.append(getModifiedByAgentId(lastEditedBy));

                    } else if (newFieldName.equalsIgnoreCase("CreatedByAgentID"))
                    {
                        str.append(getCreatorAgentId(null));

                    } else if (newFieldName.equals("PrepTypeID"))
                    {
                        String value = rs.getString(oldNameIndex.get("PreparationMethod"));
                        if (value == null || value.length() == 0)
                        {
                            value = "n/a";
                        }
                        
                        /*if (value.equalsIgnoreCase("Slide"))
                        {
                            PrepType prepType = prepTypeMap.get(value.toLowerCase());
                            if (prepType != null)
                            {
                                Integer prepTypeId = prepType.getPrepTypeId();
                                System.err.println(String.format("%s -> %d %s", value, prepTypeId, prepType.getName()));
                            }
                        }*/

                        PrepType prepType = prepTypeMap.get(value.toLowerCase());
                        if (prepType != null)
                        {
                            Integer prepTypeId = prepType.getPrepTypeId();
                            if (prepTypeId != null)
                            {
                                str.append(getStrValue(prepTypeId));

                            } else
                            {
                                str.append("NULL");
                                String msg = "***************** Couldn't find PreparationMethod[" + value + "] in PrepTypeMap";
                                log.error(msg);
                                tblWriter.log(msg);

                            }
                        } else
                        {
                            String msg = "Couldn't find PrepType[" + value + "] creating it.";
                            log.info(msg);
                            tblWriter.log(msg);
                            
                            prepType = new PrepType();
                            prepType.initialize();
                            prepType.setName(value);
                            prepType.setCollection(collection);
                            
                            prepTypeMap.put(value, prepType);
                            
                            Session tmpSession = null;
                            try
                            {
                                tmpSession = HibernateUtil.getCurrentSession();
                                Transaction trans = tmpSession.beginTransaction();
                                trans.begin();
                                tmpSession.save(prepType);
                                trans.commit();

                                str.append(getStrValue(prepType.getPrepTypeId()));

                            } catch (Exception ex)
                            {
                                ex.printStackTrace();
                                throw new RuntimeException(ex);
                                
                            }
                        }

                    } else if (newFieldName.equals("StorageID") || newFieldName.equals("Storage"))
                    {
                        str.append("NULL");

                    } else
                    {
                        Integer index = oldNameIndex.get(newFieldName);
                        Object data;
                        if (index == null)
                        {
                        	// convertPreparationRecords
                            String msg = "convertPreparationRecords - Couldn't find new field name[" + newFieldName  + "] in old field name in index Map";
                            log.warn(msg);
                            //stmt.close();
                            //throw new RuntimeException(msg);
                            data = null;
                        } else 
                        {
                           data = rs.getObject(index);
                        }
                        if (idMapperMgr != null && mappedName.endsWith("ID") && !mappedName.endsWith("GUID"))
                        {
                            //log.debug(mappedName);
                            
                            IdMapperIFace idMapper;
                            if (mappedName.equals("DerivedFromID"))
                            {
                                idMapper = idMapperMgr.get("collectionobjectcatalog", "CollectionObjectCatalogID");

                            } else
                            {
                                idMapper = idMapperMgr.get("collectionobject", mappedName);
                            }
                            
                            if (idMapper != null)
                            {
                                //Object prevObj = data;
                                data = idMapper.get((Integer)data);
                                if (data == null)
                                {
                                    String msg = "The mapped value came back null for old record Id ["+oldId+"] field ["+mappedName+"] => ["+data+"]";
                                    log.error(msg);
                                    tblWriter.logError(msg);
                                    isError = true;
                                    break;
                                }
                            } else
                            {
                                String msg = "The could find mapper collectionobject_"+mappedName+" for old record Id ["+oldId+"] field=["+data+"]";
                                log.error(msg);
                                tblWriter.logError(msg);
                                isError = true;
                                break;
                            }
                        }
                        str.append(getStrValue(data, newFieldMetaData.get(i).getType()));

                    }
                }
                
                str.append(")");
                // log.info("\n"+str.toString());
                if (hasFrame)
                {
                    if (count % 500 == 0)
                    {
                        setProcess(count);
                        log.info("Preparation Records: " + count);
                    }

                } else
                {
                    if (count % 2000 == 0)
                    {
                        log.info("Preparation Records: " + count);
                    }
                }

                if (!isError)
                {
	                try
	                {
	                    
	                    // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
	                    if (BasicSQLUtils.myDestinationServerType != BasicSQLUtils.SERVERTYPE.MS_SQLServer)
	                    {
	                        removeForeignKeyConstraints(newDBConn, "preparation", BasicSQLUtils.myDestinationServerType);
	                    }
	                    
	                    if (doDebug)
	                    {
	                        log.debug(str.toString());
	                    }
	                    //log.debug(str.toString());
	                    updateStatement.executeUpdate(str.toString());
	
	                } catch (Exception e)
	                {
	                    log.error("Error trying to execute: " + str.toString());
	                    log.error("Count: " + count);
	                    e.printStackTrace();
	                    log.error(e);
	                    throw new RuntimeException(e);
	                }
                }

                count++;
                // if (count == 1) break;
            } while (rs.next());
            
            prepTypeStmt.close();
            prepStmt.close();
            updateStatement.clearBatch();
            updateStatement.close();
            updateStatement = null;

            if (hasFrame)
            {
                if (count % 2000 == 0)
                {
                    final int cnt = count;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            setProcess(cnt);
                        }
                    });
                }
                
            } else
            {
                if (count % 2000 == 0)
                {
                    log.info("Processed CollectionObject " + count + " records.");
                }
            }
            
            
            String postSQL = " FROM collectionobject co " +
            	             "INNER JOIN collectionobjectcatalog cc ON co.CollectionObjectID = cc.CollectionObjectCatalogID " +
                             "WHERE NOT (co.DerivedFromID IS NOT NULL) AND Location IS NOT NULL";
            
            int cntTotal = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) " + postSQL);
            if (cntTotal > 0)
            {
                frame.setProcess(0, cntTotal);
                frame.setDesc("Moving Location data to Preparations...");
                
                IdMapperIFace idMapper   = idMapperMgr.get("collectionobjectcatalog", "CollectionObjectCatalogID");
                
                PreparedStatement pStmt = newDBConn.prepareStatement("UPDATE preparation SET StorageLocation=? WHERE CollectionObjectID=?");
                stmt   = oldDBConn.createStatement();
                sqlStr = "SELECT CollectionObjectID, Location " + postSQL;
                log.debug(sqlStr);
                
                rs    = stmt.executeQuery(sqlStr);
                int cnt = 0;
                while (rs.next())
                {
                    int    id     = rs.getInt(1);
                    String locStr = rs.getString(2);
                    
                    Integer newId = idMapper.get(id);
                    if (newId != null)
                    {
                        pStmt.setString(1, locStr);
                        pStmt.setInt(2, newId);
                        pStmt.execute();
                    }
                    cnt++;
                    if (cnt % 100 == 0)
                    {
                        frame.setProcess(cnt);
                    }
                }
                rs.close();
                stmt.close();
                pStmt.close();
                frame.setProcess(cntTotal);
                
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }
        
        tblWriter.log(String.format("Preparations Processing Time: %s", timeLogger.end()));
        tblWriter.close();
        
        return true;
    }

    /**
     * Converts all the Determinations.
     * @return true if no errors
     */
    public boolean convertDeterminationRecords()
    {
    	        TableWriter tblWriter = convLogger.getWriter("convertDeterminations.html", "Determinations");

        setIdentityInsertONCommandForSQLServer(newDBConn, "determination", BasicSQLUtils.myDestinationServerType);

        deleteAllRecordsFromTable(newDBConn, "determination", BasicSQLUtils.myDestinationServerType); // automatically closes the connection

        if (getNumRecords(oldDBConn, "determination") == 0) 
        { 
            return true; 
        }

        TimeLogger timeLogger = new TimeLogger();
        
        String oldDetermination_Current = "Current";
        String oldDetermination_Date    = "Date";

        /*if (BasicSQLUtils.mySourceServerType == BasicSQLUtils.SERVERTYPE.MySQL)
        {
            oldDetermination_Date     = "Date1";
            oldDetermination_Current = "IsCurrent";
        }*/

        Map<String, String> colNewToOldMap = createFieldNameMap(new String[] {
                "CollectionObjectID", "BiologicalObjectID", // meg is this right?
                "IsCurrent", oldDetermination_Current,
                "DeterminedDate", oldDetermination_Date, // want to change  over to DateField TODO Meg!!!
                "TaxonID", "TaxonNameID" });

        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            List<String>  oldFieldNames = new ArrayList<String>();

            StringBuilder sql = new StringBuilder("SELECT ");
            List<String> names = getFieldNamesFromSchema(oldDBConn, "determination");

            sql.append(buildSelectFieldList(names, "determination"));
            oldFieldNames.addAll(names);

            sql.append(", cc.CatalogSeriesID AS CatSeriesID FROM determination Inner Join collectionobjectcatalog AS cc ON determination.BiologicalObjectID = cc.CollectionObjectCatalogID");

            log.info(sql);

            if (BasicSQLUtils.mySourceServerType == BasicSQLUtils.SERVERTYPE.MS_SQLServer)
            {
                log.debug("FIXING select statement to run against SQL Server...");
                log.debug("old string: " + sql.toString());
                String currentSQL = sql.toString();
                currentSQL = currentSQL.replaceAll("Current", "[" + "Current" + "]");
                log.debug("new string: " + currentSQL);
                sql = new StringBuilder(currentSQL);

            }
            
            oldFieldNames.add("CatSeriesID");
            
            log.info(sql);
            List<FieldMetaData> newFieldMetaData = getFieldMetaDataFromSchema(newDBConn, "determination");

            log.info("Number of Fields in New Determination " + newFieldMetaData.size());
            String sqlStr = sql.toString();

            Map<String, Integer> oldNameIndex = new Hashtable<String, Integer>();
            int inx = 1;
            for (String name : oldFieldNames)
            {
                oldNameIndex.put(name, inx++);
            }

            String tableName = "determination";

            //int isCurrentInx = oldNameIndex.get(oldDetermination_Current) + 1;

            log.info(sqlStr);
            System.err.println(sqlStr);
            ResultSet rs = stmt.executeQuery(sqlStr);

            if (hasFrame)
            {
                if (rs.last())
                {
                    setProcess(0, rs.getRow());
                    rs.first();

                } else
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            } else
            {
                if (!rs.first())
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            }

            PartialDateConv partialDateConv = new PartialDateConv();
            
            IdMapperIFace detIdMapper       = IdMapperMgr.getInstance().get("determination", "DeterminationID");
            IdMapperIFace colObjIdMapper    = idMapperMgr.get("collectionobjectcatalog", "CollectionObjectCatalogID");
            IdMapperIFace colObjCatIdMapper = idMapperMgr.get("collectionobject", "CollectionObjectID");

            
            Integer catSeriesIdInx  = oldNameIndex.get("CatSeriesID");
            Integer oldRecIDInx     = oldNameIndex.get("DeterminationID");
            int     lastEditedByInx = oldNameIndex.get("LastEditedBy");
            Integer detDateInx      = oldNameIndex.get("Date");
            
            System.err.println("catSeriesIdInx: "+catSeriesIdInx);
            
            HashMap<String, Integer> nameToInxHash = new HashMap<String, Integer>();
            StringBuffer  fieldList    = new StringBuffer();
            StringBuilder insertQuesDB = new StringBuilder();
            for (int i = 0; i < newFieldMetaData.size(); i++)
            {
                if (i > 0) 
                {
                    fieldList.append(',');
                    insertQuesDB.append(',');
                }
                
                String newFieldName = newFieldMetaData.get(i).getName();
                fieldList.append(newFieldName);
                insertQuesDB.append('?');
                nameToInxHash.put(newFieldName, (i + 1));
                System.out.println(newFieldName+" "+(i+1));
            }
            
            String insertStmtStr = "INSERT INTO determination (" + fieldList + ") VALUES (" + insertQuesDB.toString() +')';
            log.debug(insertStmtStr);
            PreparedStatement pStmt = newDBConn.prepareStatement(insertStmtStr);
            
            int count = 0;
            do
            {
                partialDateConv.nullAll();
                
                String lastEditedBy = rs.getString(lastEditedByInx);
                
                Integer catSeriesId = rs.getInt(catSeriesIdInx);
                if (catSeriesId != null && rs.wasNull())
                {
                    String msg = String.format("Error - The Determination had a null CatalogSeries DeterminationID %d; it's CollectionObjectID: %d", rs.getInt(1), rs.getInt(6));
                    log.error(msg);
                    tblWriter.logError(msg);
                    
                    //if (rs.next())
                    //{
                        continue;
                    //}
                    //break;
                }
                
                Vector<Integer> collectionIdList = catSeriesToNewCollectionID.get(catSeriesId);
                if (collectionIdList == null)
                {
                    //Integer colObjId = rs.getInt(idIndex);
                    throw new RuntimeException("There are no Collections mapped to CatSeriesId["+catSeriesId+"] (converting Determinations)");
                }
                
                if (collectionIdList.size() > 1)
                {
                    UIRegistry.showError("There are multiple Collections assigned to the same CatalogSeries and we can't handle that right now.");
                }
                
                Integer collectionId = collectionIdList.get(0);
                if (collectionId == null)
                {
                    throw new RuntimeException("CollectionId is null when mapped from CatSeriesId");
                }
                
                this.curCollectionID = collectionId;

                boolean isError = false;
                
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    
                    String newFieldName = newFieldMetaData.get(i).getName();
                    int fldInx = nameToInxHash.get(newFieldName);

                    if (i == 0)
                    {
                        Integer recId = rs.getInt(oldRecIDInx);
                        Integer newId = detIdMapper.get(recId);
                        if (newId != null)
                        {
                            pStmt.setInt(fldInx, newId);
                            
                        } else 
                        {
                            String msg = String.format("Error - Unable to map old id %d to new Id", recId);
                            log.error(msg);
                            tblWriter.logError(msg);
                            isError = true;
                            continue;
                        }

                    } else if (newFieldName.equals("Version")) // User/Security changes
                    {
                        pStmt.setInt(fldInx, 0);

                    } else if (newFieldName.equals("DeterminedDate"))
                    {
                        //System.out.println("["+rs.getObject(detDateInx)+"]");
                        
                        if (partialDateConv.getDateStr() == null)
                        {
                            getPartialDate(rs.getObject(detDateInx), partialDateConv);
                        }
                        
                        if (!partialDateConv.isNull())
                        {
                            int len = partialDateConv.getDateStr().length();
                            if (len == 12)
                            {
                                String tsStr = partialDateConv.getDateStr().length() == 12 ? partialDateConv.getDateStr().substring(1, 11) : partialDateConv.getDateStr();
                                pStmt.setString(fldInx, tsStr);
                                
                            } else
                            {
                                if (!partialDateConv.getDateStr().equals("NULL")) log.error("Determined Date was in error["+partialDateConv.getDateStr()+"]");
                                pStmt.setObject(fldInx, null);
                            }
                        } else
                        {
                            pStmt.setObject(fldInx, null);
                        }
                        
                        /*
                         if (partialDateConv.getDateStr() == null)
                        {
                            getPartialDate(rs.getObject(detDateInx), partialDateConv);
                        }
                        if (isNotEmpty(partialDateConv.getDateStr()))
                        {
                            try
                            {
                                Date tsDate = sdf.parse(partialDateConv.getDateStr());
                                pStmt.setTimestamp(fldInx, new Timestamp(tsDate.getTime()));
                                
                            } catch (ParseException e)
                            {
                                e.printStackTrace();
                                pStmt.setObject(fldInx, null);
                            }
                        } else
                        {
                            pStmt.setObject(fldInx, null);
                        }
                         */
                        
                        
                    } else if (newFieldName.equals("DeterminedDatePrecision"))
                    {
                        if (partialDateConv.getDateStr() == null)
                        {
                            getPartialDate(rs.getObject(detDateInx), partialDateConv);
                        }
                        
                        if (partialDateConv.getPartial() != null)
                        {
                            if (partialDateConv.getPartial().length() > 1)
                            {
                                pStmt.setInt(fldInx, 1);
                            } else
                            {
                                pStmt.setInt(fldInx, Integer.parseInt(partialDateConv.getPartial()));
                            }
                        } else
                        {
                            pStmt.setInt(fldInx, 1);
                        }
                        
                    } else if (newFieldName.equals("CreatedByAgentID")) // User/Security changes
                    {
                        Integer agentId = getCreatorAgentId(null);
                        pStmt.setInt(fldInx, agentId);

                    } else if (newFieldName.equals("ModifiedByAgentID")) // User/Security changes
                    {
                        Integer agentId = getModifiedByAgentId(lastEditedBy);
                        pStmt.setInt(fldInx, agentId);
                        
                    } else if (newFieldName.equals("Qualifier") || 
                               newFieldName.equals("SubSpQualifier") || 
                               newFieldName.equals("VarQualifier") || 
                               newFieldName.equals("Addendum") || 
                               newFieldName.equals("AlternateName") || 
                               newFieldName.equals("NameUsage") || 
                               newFieldName.equals("GUID") || 
                               newFieldName.equals("PreferredTaxonID"))
                    {
                        pStmt.setObject(fldInx, null);

                    } else if (newFieldName.equals("CollectionMemberID")) // User/Security changes
                    {
                        pStmt.setInt(fldInx, getCollectionMemberId());

                    } else
                    {
                        Integer index = null;
                        String  oldMappedColName = colNewToOldMap.get(newFieldName);
                        if (oldMappedColName != null)
                        {
                            index = oldNameIndex.get(oldMappedColName);

                        } else
                        {
                            index = oldNameIndex.get(newFieldName);
                            oldMappedColName = newFieldName;
                        }

                        Object data;
                        if (index == null)
                        {
                            String msg = "convertDeterminationRecords - Couldn't find new field name[" + newFieldName  + "] in old field name in index Map";
                            log.warn(msg);
//                            stmt.close();
//                            tblWriter.logError(msg);
//                            throw new RuntimeException(msg);
                            data = null;
                        } else {
                            data = rs.getObject(index);
                        }

                        if (data != null)
                        {
                            int idInx = newFieldName.lastIndexOf("ID");
                            if (idMapperMgr != null && idInx > -1)
                            {
                            	Integer       oldId = (Integer)data;
                            	IdMapperIFace idMapper;
                            	
                            	if (oldMappedColName.equals("BiologicalObjectID"))
                                {
                            	    data = colObjIdMapper.get(oldId);
                            	    if (data == null)
                            	    {
                            	        data = colObjCatIdMapper.get(oldId);
                            	    }

                                } else
                                {
                                    idMapper = idMapperMgr.get(tableName, oldMappedColName);
                                    if (idMapper != null)
                                    {
                                        data = idMapper.get(oldId);
                                    } else
                                    {
                                        String msg = "No Map for [" + tableName + "][" + oldMappedColName + "]"; 
                                        log.error(msg);
                                        tblWriter.logError(msg);
                                        isError = true;
                                        break;
                                    }
                                }
                                
                                
                                if (data == null)
                                {
                                	String msg = "The determination with recordID["+rs.getInt(oldRecIDInx)+"] could not find a mapping for record ID["+oldId+"] for Old Field["+oldMappedColName+"]";
                                	log.debug(msg);
                                	tblWriter.logError(msg);
                                	
                                	tblWriter.log(ConvertVerifier.dumpSQL(oldDBConn, "SELECT * FROM determination WHERE DeterminationId = "+rs.getInt(oldRecIDInx)));
                                	
                                	if (isValueRequired(tableName, newFieldName))
                                	{
                                		msg = "For table["+tableName+"] the field ["+newFieldName+"] is null and can't be. Old value["+oldId+"]";
                                		log.error(msg);
                                		tblWriter.logError(msg);
                                	}
                                	isError = true;
                                    break;
                                }
                            }
                        }
                        //fixTimestamps(newFieldName, newFieldMetaData.get(i).getType(), data, str);
                        FieldMetaData fldMetaData = newFieldMetaData.get(i);
                        if (fldMetaData == null)
                        {
                            String msg = "For table["+tableName+"] the field ["+newFieldName+"] FieldMeataDate was null for index["+i+"]";
                            log.error(msg);
                            tblWriter.logError(msg);
                            
                        } else
                        {
                            //System.out.println(fldMetaData.getName()+"  "+fldMetaData.getSqlType()+"  "+fldMetaData.getType());
                            BasicSQLUtils.setData(pStmt, newFieldMetaData.get(i).getSqlType(), fldInx, data);
                        }
                    }
                }

                if (hasFrame)
                {
                    if (count % 500 == 0)
                    {
                        setProcess(count);
                    }

                } else
                {
                    if (count % 2000 == 0)
                    {
                        log.info("Determination Records: " + count);
                    }
                }

                if (!isError)
                {
	                try
	                {
	                    if (pStmt.executeUpdate() != 1)
	                    {
	                        log.error("Count:  " + count);
	                        log.error("Error inserting record.");
	                    }
	                    
	                } catch (SQLException e)
	                {
	                    log.error("Count:  " + count);
	                    e.printStackTrace();
	                    log.error(e);
	                    rs.close();
	                    stmt.close();
	                    showError(e.toString());
	                    throw new RuntimeException(e);
	                }
                }

                count++;
                // if (count > 10) break;
            } while (rs.next());
            
            pStmt.close();

            if (hasFrame)
            {
                setProcess(count);
            } else
            {
                log.info("Processed Determination " + count + " records.");
            }
            rs.close();

            stmt.close();
            
            tblWriter.log(String.format("Determination Processing Time: %s", timeLogger.end()));
            
            tblWriter.close();

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            throw new RuntimeException(e);
        }

        setIdentityInsertOFFCommandForSQLServer(newDBConn, "determination", BasicSQLUtils.myDestinationServerType);

        return true;
    }
    
    /**
     * Checks to see if a Column name is a required field.
     * @param tableName the table for the column
     * @param fieldName the field name
     * @return truye if required
     */
    private boolean isValueRequired(final String tableName, final String fieldName)
    {
    	DBTableInfo ti = DBTableIdMgr.getInstance().getInfoByTableName(tableName);
    	if (ti != null)
    	{
    		DBTableChildIFace fi = ti.getFieldByName(fieldName);
    		if (fi != null)
    		{
    			return fi.isRequired();
    		}
    		DBRelationshipInfo ri = ti.getRelationshipByName(fieldName);
    		if (ri != null)
    		{
    			return ri.isRequired();
    		}
    	}
    	return false;
    }
    
    /**
     * Converts all the CollectionObject and CollectionObjectCatalog Records into the new schema
     * CollectionObject table. All "logical" records are moved to the CollectionObject table and all
     * "physical" records are moved to the Preparation table.
     * @return true if no errors
     */
    @SuppressWarnings("cast")
    public boolean convertCollectionObjects(final boolean useNumericCatNumbers,
                                            final boolean usePrefix)
    {
        final String ZEROES = "000000000";
        
        UIFieldFormatterIFace formatter0 = UIFieldFormatterMgr.getInstance().getFormatter("CatalogNumber");
        log.debug(formatter0);
        
        UIFieldFormatterIFace formatter = UIFieldFormatterMgr.getInstance().getFormatter("CatalogNumberNumeric");
        log.debug(formatter);
        
        DisciplineType dt;
        Discipline discipline = (Discipline)AppContextMgr.getInstance().getClassObject(Discipline.class);
        if (discipline != null)
        {
            System.out.println("discipline.getType()["+discipline.getType()+"]");
            dt = DisciplineType.getDiscipline(discipline.getType());
        } else
        {
            Vector<Object[]> list = query(newDBConn, "SELECT Type FROM discipline");
            String typeStr = (String)list.get(0)[0];
            System.out.println("typeStr["+typeStr+"]");
            dt = DisciplineType.getDiscipline(typeStr);
        }
        
        Pair<Integer, Boolean> objTypePair = dispToObjTypeHash.get(dt.getDisciplineType());
        if (objTypePair == null)
        {
            System.out.println("objTypePair is null dt["+dt.getName()+"]["+dt.getTitle()+"]");
            
            for (STD_DISCIPLINES key : dispToObjTypeHash.keySet())
            {
                Pair<Integer, Boolean> p = dispToObjTypeHash.get(key);
                System.out.println("["+key+"] ["+p.first+"]["+p.second+"]");
            }
            
        } else if (objTypePair.first == null)
        {
            System.out.println("objTypePair.first is null dt["+dt+"]");
            
            for (STD_DISCIPLINES key : dispToObjTypeHash.keySet())
            {
                Pair<Integer, Boolean> p = dispToObjTypeHash.get(key);
                System.out.println("["+key+"] ["+p.first+"]["+p.second+"]");
            }

        }
        //int objTypeId  = objTypePair.first;
        //boolean isEmbedded = objTypePair.second;
        
        idMapperMgr.dumpKeys();
        IdHashMapper colObjTaxonMapper = (IdHashMapper)idMapperMgr.get("ColObjCatToTaxonType" .toLowerCase());
        IdHashMapper colObjAttrMapper  = (IdHashMapper)idMapperMgr.get("biologicalobjectattributes_BiologicalObjectAttributesID");
        IdHashMapper colObjMapper      = (IdHashMapper)idMapperMgr.get("collectionobjectcatalog_CollectionObjectCatalogID");
        
        colObjTaxonMapper.setShowLogErrors(false); // NOTE: TURN THIS ON FOR DEBUGGING or running new Databases through it
        colObjAttrMapper.setShowLogErrors(false);

        //IdHashMapper stratMapper    = (IdHashMapper)idMapperMgr.get("stratigraphy_StratigraphyID");
        //IdHashMapper stratGTPMapper = (IdHashMapper)idMapperMgr.get("stratigraphy_GeologicTimePeriodID");

        String[] fieldsToSkip = { "ContainerID", "ContainerItemID",
                                  "AltCatalogNumber",
                                  "GUID",
                                  "ContainerOwnerID",
                                  "RepositoryAgreementID",
                                  "GroupPermittedToView", // this may change when converting Specify 5.x
                                  "CollectionObjectID", "VisibilitySetBy", "ContainerOwnerID", "InventoryDate",
                                  "ObjectCondition", "Notifications", "ProjectNumber", "Restrictions", "YesNo3",
                                  "YesNo4", "YesNo5", "YesNo6", "FieldNotebookPageID", "ColObjAttributesID",
                                  "DNASequenceID", "AppraisalID", "TotalValue", "Description", "SGRStatus", "OCR",
                                  "ReservedText", "Text3"};

        HashSet<String> fieldsToSkipHash = new HashSet<String>();
        for (String fName : fieldsToSkip)
        {
            fieldsToSkipHash.add(fName);
        }

        TableWriter tblWriter = convLogger.getWriter("convertCollectionObjects.html", "Collection Objects");

        String msg = "colObjTaxonMapper: " + colObjTaxonMapper.size();
        log.info(msg);
        tblWriter.log(msg);
        
        setIdentityInsertONCommandForSQLServer(newDBConn, "collectionobject", BasicSQLUtils.myDestinationServerType);

        deleteAllRecordsFromTable(newDBConn, "collectionobject", BasicSQLUtils.myDestinationServerType); // automatically closes the connection
        
        TreeSet<String> badSubNumberCatNumsSet = new TreeSet<String>();
        
        TimeLogger timeLogger = new TimeLogger();
        
        try
        {
            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            StringBuilder str = new StringBuilder();
            
            List<String> oldFieldNames = new ArrayList<String>();

            StringBuilder sql = new StringBuilder("select ");
            List<String> names = getFieldNamesFromSchema(oldDBConn, "collectionobject");

            sql.append(buildSelectFieldList(names, "collectionobject"));
            sql.append(", ");
            oldFieldNames.addAll(names);

            names = getFieldNamesFromSchema(oldDBConn, "collectionobjectcatalog");
            sql.append(buildSelectFieldList(names, "collectionobjectcatalog"));
            oldFieldNames.addAll(names);

            String fromClause = " FROM collectionobject Inner Join collectionobjectcatalog ON " +
                                "collectionobject.CollectionObjectID = collectionobjectcatalog.CollectionObjectCatalogID " +
                                "WHERE (collectionobject.DerivedFromID IS NULL) AND collectionobjectcatalog.CollectionObjectCatalogID = ";
            sql.append(fromClause);

            log.info(sql);
            String sqlStr = sql.toString();

            List<FieldMetaData> newFieldMetaData = getFieldMetaDataFromSchema(newDBConn, "collectionobject");

            log.info("Number of Fields in New CollectionObject " + newFieldMetaData.size());

            Map<String, Integer> oldNameIndex = new Hashtable<String, Integer>();
            int inx = 1;
            log.info("---- Old Names ----");
            for (String name : oldFieldNames)
            {
                log.info("[" + name + "][" + inx + "]");
                oldNameIndex.put(name, inx++);
            }

            log.info("---- New Names ----");
            for (FieldMetaData fmd : newFieldMetaData)
            {
                log.info("[" + fmd.getName() + "]");
            }
            String tableName = "collectionobject";

            Statement newStmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            newStmt.setFetchSize(Integer.MIN_VALUE);
            ResultSet rsLooping = newStmt.executeQuery("SELECT OldID, NewID FROM collectionobjectcatalog_CollectionObjectCatalogID ORDER BY OldID");

            if (hasFrame)
            {
                if (rsLooping.last())
                {
                    setProcess(0, rsLooping.getRow());
                    rsLooping.first();

                } else
                {
                    rsLooping.close();
                    stmt.close();
                    return true;
                }
            } else
            {
                if (!rsLooping.first())
                {
                    rsLooping.close();
                    stmt.close();
                    return true;
                }
            }

            int boaCnt = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) FROM biologicalobjectattributes"); // ZZZ
            
            PartialDateConv partialDateConv = new PartialDateConv();
            
            Statement stmt2 = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt2.setFetchSize(Integer.MIN_VALUE);

            int catNumInx       = oldNameIndex.get("CatalogNumber");
            int catDateInx      = oldNameIndex.get("CatalogedDate");
            int catSeriesIdInx  = oldNameIndex.get("CatalogSeriesID");
            int lastEditedByInx = oldNameIndex.get("LastEditedBy");
            
            /*int     grpPrmtViewInx    = -1;
            Integer grpPrmtViewInxObj = oldNameIndex.get("GroupPermittedToView");
            if (grpPrmtViewInxObj != null)
            {
                grpPrmtViewInx = grpPrmtViewInxObj + 1;
            }*/
            
            Hashtable<Integer, CollectionInfo> oldCatSeriesIDToCollInfo = new Hashtable<Integer, CollectionInfo>();
            for (CollectionInfo ci : collectionInfoShortList)
            {
                if (ci.getCatSeriesId() != null)
                {
                    oldCatSeriesIDToCollInfo.put(ci.getCatSeriesId(), ci);
                }
            }
            
            String insertStmtStr = null;
            
            /*String catIdTaxIdStrBase = "SELECT cc.CollectionObjectCatalogID, cc.CatalogSeriesID, ct.TaxonomyTypeID "
                                        + "FROM collectionobjectcatalog AS cc "
                                        + "Inner Join collectionobject AS co ON cc.CollectionObjectCatalogID = co.CollectionObjectID "
                                        + "Inner Join collectiontaxonomytypes as ct ON co.CollectionObjectTypeID = ct.BiologicalObjectTypeID "
                                        + "where cc.CollectionObjectCatalogID = ";*/
            
            int     colObjAttrsNotMapped = 0;
            int     count                = 0;
            boolean skipRecord           = false;
            do
            {
                String catSQL = sqlStr + rsLooping.getInt(1);
                //log.debug(catSQL);
                ResultSet rs = stmt.executeQuery(catSQL);
                if (!rs.next())
                {
                    log.error("Couldn't find CO with old  id["+rsLooping.getInt(1)+"] "+catSQL);
                    continue;
                }
                
                partialDateConv.nullAll();
                
                skipRecord = false;
                
                CollectionInfo collInfo = oldCatSeriesIDToCollInfo.get(rs.getInt(catSeriesIdInx));
                
                /*String catIdTaxIdStr = catIdTaxIdStrBase + rs.getInt(1);
                //log.info(catIdTaxIdStr);
                
                ResultSet rs2 = stmt2.executeQuery(catIdTaxIdStr);
                if (!rs2.next())
                {
                    log.info("QUERY failed to return results:\n"+catIdTaxIdStr+"\n");
                    continue;
                }
                Integer catalogSeriesID = rs2.getInt(2);
                Integer taxonomyTypeID  = rs2.getInt(3);
                Integer newCatSeriesId  = collectionHash.get(catalogSeriesID + "_" + taxonomyTypeID);
                String  prefix          = prefixHash.get(catalogSeriesID + "_" + taxonomyTypeID);
                rs2.close();

                if (newCatSeriesId == null)
                {
                    msg = "Can't find " + catalogSeriesID + "_" + taxonomyTypeID;
                    log.info(msg);
                    tblWriter.logError(msg);
                    continue;
                }*/
                

                /*if (false)
                {
                    String stratGTPIdStr = "SELECT co.CollectionObjectID, ce.CollectingEventID, s.StratigraphyID, g.GeologicTimePeriodID FROM collectionobject co " +
                        "LEFT JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID  " +
                        "LEFT JOIN stratigraphy s ON ce.CollectingEventID = s.StratigraphyID  " +
                        "LEFT JOIN geologictimeperiod g ON s.GeologicTimePeriodID = g.GeologicTimePeriodID  " +
                        "WHERE co.CollectionObjectID  = " + rs.getInt(1);
                    log.info(stratGTPIdStr);
                    rs2 = stmt2.executeQuery(stratGTPIdStr);
    
                    Integer coId = null;
                    Integer ceId = null;
                    Integer stId = null;
                    Integer gtpId = null;
                    if (rs2.next())
                    {
                        coId = rs2.getInt(1);
                        ceId = rs2.getInt(2);
                        stId = rs2.getInt(3);
                        gtpId = rs2.getInt(4);
                    }
                    rs2.close();
                }*/

                String catalogNumber = null;
                String colObjId = null;

                str.setLength(0);

                if (insertStmtStr == null)
                {
                    StringBuffer fieldList = new StringBuffer();
                    fieldList.append("( ");
                    for (int i = 0; i < newFieldMetaData.size(); i++)
                    {
                        if ((i > 0) && (i < newFieldMetaData.size()))
                        {
                            fieldList.append(", ");
                        }
                        String newFieldName = newFieldMetaData.get(i).getName();
                        fieldList.append(newFieldName + " ");
                    }
                    fieldList.append(")");
                    insertStmtStr = "INSERT INTO collectionobject " + fieldList + "  VALUES (";
                }
                str.append(insertStmtStr);
                
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if (i > 0)
                    {
                        str.append(", ");
                    }

                    String newFieldName = newFieldMetaData.get(i).getName();

                    if (i == 0)
                    {
                        Integer oldColObjId = rs.getInt(1);
                        Integer newColObjId = colObjMapper.get(oldColObjId);
                        
                        if (newColObjId == null)
                        {
                            msg = "Couldn't find new ColObj Id for old ["+oldColObjId+"]";
                            tblWriter.logError(msg);
                            showError(msg);
                            throw new RuntimeException(msg);
                        }
                        
                        colObjId = getStrValue(newColObjId);
                        if (contains(colObjId, '.'))
                        {
                            String msgStr = String.format("CatalogNumber '%d' contains a decimal point.", colObjId);
                            log.debug(msgStr);
                            tblWriter.logError(msgStr);
                            skipRecord = true;
                            break;
                        }
                        str.append(colObjId);

                        if (useNumericCatNumbers)
                        {
                            catalogNumber = rs.getString(catNumInx);
                            
                            if (catalogNumber != null)
                            {
                                int catNumInt = (int)Math.abs(rs.getDouble(catNumInx));
                                catalogNumber = Integer.toString(catNumInt);
                                    
                                if (catalogNumber.length() > 0 && catalogNumber.length() < ZEROES.length())
                                {
                                    catalogNumber = "\"" + ZEROES.substring(catalogNumber.length()) + catalogNumber + "\"";
                                    
                                } else if (catalogNumber.length() > ZEROES.length())
                                {
                                    showError("Catalog Number["+catalogNumber+"] is too long for formatter of 9");
                                }
                                
                            } else
                            {
                                String mssg = "Empty catalog number.";
                                log.debug(mssg);
                                //showError(msg);
                                tblWriter.logError(mssg);
                            }

                        } else
                        {
                            String prefix = collInfo.getCatSeriesPrefix();
                            
                            float catNum = rs.getFloat(catNumInx);
                            catalogNumber = "\"" + (usePrefix && isNotEmpty(prefix) ? (prefix + "-") : "")
                                            + String.format("%9.0f", catNum).trim() + "\"";
                        }

                        int subNumber = rs.getInt(oldNameIndex.get("SubNumber"));
                        if (subNumber < 0 || rs.wasNull())
                        {
                            badSubNumberCatNumsSet.add(catalogNumber);
                            
                            skipRecord = true;
                            //msg = "Collection Object is being skipped because SubNumber is less than zero CatalogNumber["+ catalogNumber + "]";
                            //log.error(msg);
                            //tblWriter.logError(msg);
                            //showError(msg);
                            break;
                        }

                    } else if (fieldsToSkipHash.contains(newFieldName))
                    {
                        str.append("NULL");

                    } else if (newFieldName.equals("CollectionID")) // User/Security changes
                    {
                        str.append(collInfo.getCollectionId());

                    } else if (newFieldName.equals("Version")) // User/Security changes
                    {
                        str.append("0");

                    } else if (newFieldName.equals("CreatedByAgentID")) // User/Security changes
                    {
                        str.append(getCreatorAgentId(null));

                    } else if (newFieldName.equals("ModifiedByAgentID")) // User/Security changes
                    {
                        String lastEditedByStr = rs.getString(lastEditedByInx);
                        str.append(getModifiedByAgentId(lastEditedByStr));

                    } else if (newFieldName.equals("CollectionMemberID")) // User/Security changes
                    {
                        str.append(collInfo.getCollectionId());
                        
                    } else if (newFieldName.equals("PaleoContextID"))
                    {
                        str.append("NULL");// newCatSeriesId);

                    } else if (newFieldName.equals("CollectionObjectAttributeID")) // User/Security changes
                    {
                        Object idObj = rs.getObject(1);
                        if (idObj != null)
                        {
                            Integer coId = rs.getInt(1);
                            Integer newId = colObjAttrMapper.get(coId);
                            if (newId != null)
                            {
                                str.append(getStrValue(newId));
                            } else
                            {
                                if (boaCnt > 0) colObjAttrsNotMapped++;
                                str.append("NULL");
                            }
                        } else
                        {
                            str.append("NULL");
                        }
                        
                    } else if (newFieldName.equals("CatalogedDate"))
                    {
                        if (partialDateConv.getDateStr() == null)
                        {
                            getPartialDate(rs.getObject(catDateInx), partialDateConv);
                        }
                        str.append(partialDateConv.getDateStr());

                    } else if (newFieldName.equals("CatalogedDatePrecision"))
                    {
                        if (partialDateConv.getDateStr() == null)
                        {
                            getPartialDate(rs.getObject(catDateInx), partialDateConv);
                        }
                        str.append(partialDateConv.getPartial());

                    } else if (newFieldName.equals("CatalogedDateVerbatim"))
                    {
                        if (partialDateConv.getDateStr() == null)
                        {
                            getPartialDate(rs.getObject(catDateInx), partialDateConv);
                        }
                        str.append(partialDateConv.getVerbatim());

                    } else if (newFieldName.equals("Availability"))
                    {
                        str.append("NULL");

                    } else if (newFieldName.equals("CatalogNumber"))
                    {
                        str.append(catalogNumber);

                    } else if (newFieldName.equals("Visibility")) // User/Security changes
                    {
                        //str.append(grpPrmtViewInx > -1 ? rs.getObject(grpPrmtViewInx) : "NULL");
                        str.append("0");
                        
                    } else if (newFieldName.equals("VisibilitySetByID")) // User/Security changes
                    {
                        str.append("NULL");

                    } else if (newFieldName.equals("CountAmt"))
                    {
                        Integer index = oldNameIndex.get("Count1");
                        if (index == null)
                        {
                            index = oldNameIndex.get("Count");
                        }
                        Object countObj = rs.getObject(index);
                        if (countObj != null)
                        {
                            str.append(getStrValue(countObj, newFieldMetaData.get(i).getType()));
                        } else
                        {
                            str.append("NULL");
                        }

                    } else
                    {
                        Integer index = oldNameIndex.get(newFieldName);
                        Object data;
                        if (index == null)
                        {
                            msg = "convertCollectionObjects - Couldn't find new field name[" + newFieldName + "] in old field name in index Map";
                            log.warn(msg);
//                            tblWriter.logError(msg);
//                            showError(msg);
                            data = null;
                            // for (String key : oldNameIndex.keySet())
                            // {
                            // log.info("["+key+"]["+oldNameIndex.get(key)+"]");
                            // }
                            //stmt.close();
                            //throw new RuntimeException(msg);
                        } else {
                            
                            data = rs.getObject(index);
                        }
                        if (data != null)
                        {
                            int idInx = newFieldName.lastIndexOf("ID");
                            if (idMapperMgr != null && idInx > -1)
                            {
                                IdMapperIFace idMapper = idMapperMgr.get(tableName, newFieldName);
                                if (idMapper != null)
                                {
                                    Integer origValue = rs.getInt(index);
                                    data = idMapper.get(origValue);
                                    if (data == null)
                                    {
                                        msg = "No value ["+origValue+"] in map  [" + tableName + "][" + newFieldName + "]";
                                        log.error(msg);
                                        tblWriter.logError(msg);
                                        //showError(msg);
                                    }
                                } else
                                {
                                    msg = "No Map for [" + tableName + "][" + newFieldName + "]";
                                    log.error(msg);
                                    tblWriter.logError(msg);
                                    //showError(msg);
                                }
                            }
                        }
                        str.append(getStrValue(data, newFieldMetaData.get(i).getType()));
                    }
                }

                if (!skipRecord)
                {
                    str.append(")");
                    // log.info("\n"+str.toString());
                    if (hasFrame)
                    {
                        if (count % 500 == 0)
                        {
                            setProcess(count);
                        }
                        if (count % 5000 == 0)
                        {
                            log.info("CollectionObject Records: " + count);
                        }

                    } else
                    {
                        if (count % 2000 == 0)
                        {
                            log.info("CollectionObject Records: " + count);
                        }
                    }

                    try
                    {
                        Statement updateStatement = newDBConn.createStatement();
                        if (BasicSQLUtils.myDestinationServerType != BasicSQLUtils.SERVERTYPE.MS_SQLServer)
                        {
                            removeForeignKeyConstraints(newDBConn, BasicSQLUtils.myDestinationServerType);
                        }
                        // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                        //if (count < 50) System.err.println(str.toString());
                        
                        updateStatement.executeUpdate(str.toString());
                        updateStatement.clearBatch();
                        updateStatement.close();
                        updateStatement = null;

                    } catch (SQLException e)
                    {
                        log.error("Count: " + count);
                        log.error("Key: [" + colObjId + "][" + catalogNumber + "]");
                        log.error("SQL: " + str.toString());
                        e.printStackTrace();
                        log.error(e);
                        showError(e.getMessage());
                        rs.close();
                        stmt.close();
                        throw new RuntimeException(e);
                    }

                    count++;
                } else
                {
                    tblWriter.logError("Skipping - CatNo:"+catalogNumber);
                }
                // if (count > 10) break;
                
                rs.close();
                
            } while (rsLooping.next());

            /*if (boaCnt > 0)
            {
                msg = "CollectionObjectAttributes not mapped: " + colObjAttrsNotMapped + " out of "+boaCnt;
                log.info(msg);
                tblWriter.logError(msg);
            }*/
            
            stmt2.close();

            if (hasFrame)
            {
                setProcess(count);
            } else
            {
                log.info("Processed CollectionObject " + count + " records.");
            }
            
            tblWriter.log(String.format("Collection Objects Processing Time: %s", timeLogger.end()));
            
            tblWriter.log("Processed CollectionObject " + count + " records.");
            rsLooping.close();
            newStmt.close();
            stmt.close();
            
            tblWriter.append("<br><br><b>Catalog Numbers rejected because the SubNumber was NULL or less than Zero</b><br>");
            tblWriter.startTable();
            tblWriter.logHdr("Catalog Number");
            for (String catNum : badSubNumberCatNumsSet)
            {
                tblWriter.log(catNum);
            }
            tblWriter.endTable();
            
        } catch (SQLException e)
        {
            setIdentityInsertOFFCommandForSQLServer(newDBConn, "collectionobject", BasicSQLUtils.myDestinationServerType);
            e.printStackTrace();
            log.error(e);
            tblWriter.logError(e.getMessage());
            showError(e.getMessage());
            throw new RuntimeException(e);
            
        } finally
        {
             tblWriter.close();
        }
        setIdentityInsertOFFCommandForSQLServer(newDBConn, "collectionobject", BasicSQLUtils.myDestinationServerType);
        
        return true;
    }


    /**
     * @param rs
     * @param columnIndex
     * @return
     */
    protected int getIntValue(final ResultSet rs, final int columnIndex)
    {
        try
        {
            int val = rs.getInt(columnIndex);
            return rs.wasNull() ? 0 : val;

        } catch (Exception ex)
        {
            // TODO: what now?
        }
        return 0;
    }
    
    /**
     * @return
     */
    public boolean convertLoanPreparations()
    {
        setIdentityInsertOFFCommandForSQLServer(newDBConn, "determination", BasicSQLUtils.myDestinationServerType);
        setIdentityInsertONCommandForSQLServer(newDBConn, "loanpreparation", BasicSQLUtils.myDestinationServerType);
        
        deleteAllRecordsFromTable(newDBConn, "loanpreparation", BasicSQLUtils.myDestinationServerType); // automatically closes the connection

        if (getNumRecords(oldDBConn, "loanphysicalobject") == 0)
        {
            setIdentityInsertOFFCommandForSQLServer(newDBConn, "loanpreparation", BasicSQLUtils.myDestinationServerType);
            return true;
        }
        
        Integer recCount = getCount(oldDBConn, "SELECT count(*) FROM loan WHERE Category = 0 ORDER BY LoanID");
        if (recCount == null || recCount == 0)
        {
            return true;
        }
        
        TableWriter tblWriter = convLogger.getWriter("convertLoanPreparations.html", "Loan Preparations");
        
        IdTableMapper loanPrepsMapper = (IdTableMapper)IdMapperMgr.getInstance().get("loanphysicalobject", "LoanPhysicalObjectID");
        if (loanPrepsMapper == null) {
            String msg = "LoanPrepsMapper not found. (This was a fatal error).";
            tblWriter.logError(msg);
            log.error(msg);
            return false;
        }
        
        TimeLogger timeLogger = new TimeLogger();

        try
        {
            Map<String, String> colNewToOldMap = createFieldNameMap(new String[] { "PreparationID", "PhysicalObjectID", });

            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            StringBuilder str  = new StringBuilder();

            List<String> oldFieldNames = new ArrayList<String>();

            StringBuilder sql   = new StringBuilder("SELECT ");
            List<String>  names = getFieldNamesFromSchema(oldDBConn, "loanphysicalobject");

            sql.append(buildSelectFieldList(names, "loanphysicalobject"));
            oldFieldNames.addAll(names);

            sql.append(" FROM loanphysicalobject INNER JOIN loan ON loanphysicalobject.LoanID = loan.LoanID WHERE loan.Category = 0");

            log.info(sql);

            List<FieldMetaData> newFieldMetaData = getFieldMetaDataFromSchema(newDBConn, "loanpreparation");

            log.info("Number of Fields in New loanpreparation " + newFieldMetaData.size());
            String sqlStr = sql.toString();

            Map<String, Integer> oldNameIndex = new Hashtable<String, Integer>();
            int inx = 1;
            for (String name : oldFieldNames)
            {
                oldNameIndex.put(name, inx++);
            }

            String tableName = "loanphysicalobject";

            int loanPhysIdIndex  = oldNameIndex.get("LoanPhysicalObjectID");
            int quantityIndex    = oldNameIndex.get("Quantity");
            int quantityRetIndex = oldNameIndex.get("QuantityReturned");
            int quantityResIndex = oldNameIndex.get("QuantityResolved");
            int lastEditedByInx  = oldNameIndex.get("LastEditedBy");

            log.info(sqlStr);
            ResultSet rs = stmt.executeQuery(sqlStr);

            if (hasFrame)
            {
                if (rs.last())
                {
                    setProcess(0, rs.getRow());
                    rs.first();

                } else
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            } else
            {
                if (!rs.first())
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            }
            
            String insertStmtStr = null;
            

            int count = 0;
            do
            {
                boolean skipInsert = false;
                
                int quantity         = getIntValue(rs, quantityIndex);
                int quantityResolved = getIntValue(rs, quantityResIndex);
                int quantityReturned = getIntValue(rs, quantityRetIndex);
                Boolean isResolved   = quantityReturned == quantity || quantityResolved == quantity;
                String lastEditedBy  = rs.getString(lastEditedByInx);

                str.setLength(0);
                
                if (insertStmtStr == null)
                {
                    StringBuffer fieldList = new StringBuffer();
                    fieldList.append("( ");
                    for (int i = 0; i < newFieldMetaData.size(); i++)
                    {
                        if ((i > 0) && (i < newFieldMetaData.size()))
                        {
                            fieldList.append(", ");
                        }
                        String newFieldName = newFieldMetaData.get(i).getName();
                        fieldList.append(newFieldName + " ");
                    }
                    fieldList.append(")");
                    insertStmtStr = "INSERT INTO loanpreparation " + fieldList + " VALUES (";
                }
                
                str.append(insertStmtStr);
                
                /*int    loanPhysId = rs.getInt(loanPhysIdIndex);
                String loanNumber = BasicSQLUtils.querySingleObj(oldDBConn, "SELECT LoanNumber FROM loan l INNER JOIN loanphysicalobject lp ON l.LoanID = lp.LoanID WHERE LoanPhysicalObjectID = "+loanPhysId);
                if (loanNumber != null && loanNumber.equals("25"))
                {
                    debug = true;
                    System.out.println("-------------------------------------\n"+loanPhysId);
                }*/
                
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if (i > 0)
                        str.append(", ");

                    String newFieldName = newFieldMetaData.get(i).getName();
                    
                    if (i == 0)
                    {
                        Integer oldId = rs.getInt(loanPhysIdIndex);
                        Integer newID = loanPrepsMapper.get(oldId);
                        if (newID != null)
                        {
                            str.append(getStrValue(newID));
                        } else
                        {
                            String msg = String.format("loanPhysIdIndex: %d; Old Id: %d could be mapped to a new ID. (This was a fatal error).", loanPhysIdIndex, oldId);
                            tblWriter.logError(msg);
                            log.error(msg);
                            return false;
                        }

                    } else if (newFieldName.equals("ReceivedComments"))
                    {
                        str.append("NULL");

                    } else if (newFieldName.equals("IsResolved"))
                    {
                        str.append(getStrValue(isResolved));

                    } else if (newFieldName.equalsIgnoreCase("Version"))
                    {
                        str.append("0");

                    } else if (newFieldName.equalsIgnoreCase("DisciplineID"))
                    {
                        str.append(getDisciplineId());

                    } else if (newFieldName.equalsIgnoreCase("ModifiedByAgentID"))
                    {
                        str.append(getModifiedByAgentId(lastEditedBy));

                    } else if (newFieldName.equalsIgnoreCase("CreatedByAgentID"))
                    {
                        str.append(getCreatorAgentId(null));

                    } else
                    {
                        Integer index            = null;
                        String  oldMappedColName = colNewToOldMap.get(newFieldName);
                        if (oldMappedColName != null)
                        {
                            index = oldNameIndex.get(oldMappedColName);

                        } else
                        {
                            index = oldNameIndex.get(newFieldName);
                            oldMappedColName = newFieldName;
                        }

                        Object data;
                        if (index == null)
                        {
                            String msg = "convertLoanPreparations - Couldn't find new field name[" + newFieldName + "] in old field name in index Map";
                            log.warn(msg);
//                            stmt.close();
//                            tblWriter.logError(msg);
//                            showError(msg);
//                            throw new RuntimeException(msg);
                            data = null;
                        } else {
                           data = rs.getObject(index);
                        }
                        if (data != null)
                        {
                            int idInx = newFieldName.lastIndexOf("ID");
                            if (idMapperMgr != null && idInx > -1)
                            {
                                IdMapperIFace idMapper = idMapperMgr.get(tableName, oldMappedColName);
                                if (idMapper != null)
                                {
                                    Integer oldId = rs.getInt(index);
                                    data = idMapper.get(oldId);
                                    if (data == null)
                                    {
                                        String msg = "No Map ID for [" + tableName + "][" + oldMappedColName + "] for ID["+oldId+"]";
                                        log.error(msg);
                                        tblWriter.logError(msg);
                                        skipInsert = true;
                                    }
                                } else
                                {
                                    String msg = "No Map for [" + tableName + "][" + oldMappedColName + "]";
                                    log.error(msg);
                                    tblWriter.logError(msg);
                                    skipInsert = true;
                               }
                            }
                        }
                        str.append(getStrValue(data, newFieldMetaData.get(i).getType()));
                    }
                }
                str.append(")");

                if (hasFrame)
                {
                    if (count % 500 == 0)
                    {
                        setProcess(count);
                    }

                } else
                {
                    if (count % 2000 == 0)
                    {
                        log.info("LoanPreparation Records: " + count);
                    }
                }

                try
                {
                    if (!skipInsert)
                    {
                        Statement updateStatement = newDBConn.createStatement();
                        if (BasicSQLUtils.myDestinationServerType != BasicSQLUtils.SERVERTYPE.MS_SQLServer)
                        {
                            removeForeignKeyConstraints(newDBConn, BasicSQLUtils.myDestinationServerType);
                        }
                        // log.debug("executring: " + str.toString());
                        // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                        updateStatement.executeUpdate(str.toString());
                        updateStatement.clearBatch();
                        updateStatement.close();
                        updateStatement = null;
                    }

                } catch (SQLException e)
                {
                    log.error("Count: " + count);
                    e.printStackTrace();
                    log.error(e);
                    rs.close();
                    stmt.close();
                    throw new RuntimeException(e);
                }

                count++;
                // if (count > 10) break;
            } while (rs.next());

            if (hasFrame)
            {
                setProcess(count);
                log.info("Processed LoanPreparation " + count + " records.");
            } else
            {
                log.info("Processed LoanPreparation " + count + " records.");
            }
            rs.close();

            stmt.close();

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            setIdentityInsertOFFCommandForSQLServer(newDBConn, "LoanPreparation", BasicSQLUtils.myDestinationServerType);
            throw new RuntimeException(e);
        }
        log.info("Done processing LoanPhysicalObject");
        setIdentityInsertOFFCommandForSQLServer(newDBConn, "LoanPreparation", BasicSQLUtils.myDestinationServerType);
        
        //tblWriter.log(String.format("Loan Preps Processing Time: %s", timeLogger.end()));
        tblWriter.close();
        
        return true;

    }

    /**
     * Converts all the LoanPhysicalObjects.
     * @return true if no errors
     */
    public boolean convertGiftPreparations()
    {
        setIdentityInsertOFFCommandForSQLServer(newDBConn, "determination",   BasicSQLUtils.myDestinationServerType);
        setIdentityInsertONCommandForSQLServer(newDBConn,  "giftpreparation", BasicSQLUtils.myDestinationServerType);
        
        deleteAllRecordsFromTable(newDBConn, "giftpreparation", BasicSQLUtils.myDestinationServerType); // automatically closes the connection

        if (getNumRecords(oldDBConn, "loanphysicalobject") == 0)
        {
            setIdentityInsertOFFCommandForSQLServer(newDBConn, "giftpreparation", BasicSQLUtils.myDestinationServerType);
            return true;
        }
        
        Integer recCount = getCount(oldDBConn, "SELECT count(*) FROM loan WHERE Category = 1 ORDER BY LoanID");
        if (recCount == null || recCount == 0)
        {
        	return true;
        }
        
        // This mapping is used by Gifts
        IdMapperIFace giftsIdMapper = IdMapperMgr.getInstance().get("gift", "GiftID");
        //if (shouldCreateMapTables)
        //{
        //	giftsIdMapper.mapAllIdsWithSQL();
        //}

         // This mapping is used by Gifts Preps
        
        IdMapperIFace giftPrepsIdMapper = IdMapperMgr.getInstance().get("giftphysicalobject", "id");
        
        TableWriter tblWriter = convLogger.getWriter("convertGiftPreparations.html", "Gift Preparations");
        TimeLogger timeLogger = new TimeLogger();

        try
        {
            Map<String, String> colNewToOldMap = createFieldNameMap(new String[] { "PreparationID", "PhysicalObjectID"});

            Statement stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            StringBuilder str  = new StringBuilder();

            List<String> oldFieldNames = new ArrayList<String>();

            StringBuilder sql = new StringBuilder("SELECT ");
            List<String> names = getFieldNamesFromSchema(oldDBConn, "loanphysicalobject");

            sql.append(buildSelectFieldList(names, "loanphysicalobject"));
            oldFieldNames.addAll(names);

            sql.append(" FROM loanphysicalobject INNER JOIN loan ON loanphysicalobject.LoanID = loan.LoanID WHERE loan.Category = 1 ORDER BY loanphysicalobject.LoanPhysicalObjectID");

            log.info(sql);

            List<FieldMetaData> newFieldMetaData = getFieldMetaDataFromSchema(newDBConn, "giftpreparation");

            log.info("Number of Fields in New giftpreparation " + newFieldMetaData.size());
            String sqlStr = sql.toString();
            
            colNewToOldMap.put("GiftID", "LoanID");

            Map<String, Integer> oldNameIndex = new Hashtable<String, Integer>();
            int inx = 1;
            for (String name : oldFieldNames)
            {
                oldNameIndex.put(name, inx++);
            }

            String tableName = "loanphysicalobject";

            //int quantityIndex   = oldNameIndex.get("Quantity");
            int lastEditedByInx = oldNameIndex.get("LastEditedBy");
            int loanPhysIdIndex = oldNameIndex.get("LoanPhysicalObjectID");


            log.info(sqlStr);
            ResultSet rs = stmt.executeQuery(sqlStr);

            if (hasFrame)
            {
                if (rs.last())
                {
                    setProcess(0, rs.getRow());
                    rs.first();

                } else
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            } else
            {
                if (!rs.first())
                {
                    rs.close();
                    stmt.close();
                    return true;
                }
            }
            
            String insertStmtStr = null;
            
            int count = 0;
            do
            {
                //int quantity         = getIntValue(rs, quantityIndex);
                String lastEditedBy  = rs.getString(lastEditedByInx);

                str.setLength(0);
                
                if (insertStmtStr == null)
                {
                    StringBuffer fieldList = new StringBuffer();
                    fieldList.append("( ");
                    for (int i = 0; i < newFieldMetaData.size(); i++)
                    {
                        if ((i > 0) && (i < newFieldMetaData.size()))
                        {
                            fieldList.append(", ");
                        }
                        String newFieldName = newFieldMetaData.get(i).getName();
                        fieldList.append(newFieldName + " ");
                    }
                    fieldList.append(")");
                    insertStmtStr = "INSERT INTO giftpreparation " + fieldList + " VALUES (";
                }
                str.append(insertStmtStr);
                
                for (int i = 0; i < newFieldMetaData.size(); i++)
                {
                    if (i > 0) str.append(", ");

                    String newFieldName = newFieldMetaData.get(i).getName();

                    if (i == 0)
                    {
                        Integer oldId = rs.getInt(loanPhysIdIndex);
                        Integer newID = giftPrepsIdMapper.get(oldId);
                        if (newID != null)
                        {
                            str.append(getStrValue(newID));
                        } else
                        {
                            String msg = String.format("loanPhysIdIndex: %d; Old Id: %d could be mapped to a new ID. (This was a fatal error).", loanPhysIdIndex, oldId);
                            tblWriter.logError(msg);
                            log.error(msg);
                            return false;
                        }

                    } else if (newFieldName.equals("ReceivedComments"))
                    {
                        str.append("NULL");

                    } else if (newFieldName.equalsIgnoreCase("Version"))
                    {
                        str.append("0");

                    } else if (newFieldName.equalsIgnoreCase("DisciplineID"))
                    {
                        str.append(getDisciplineId());

                    } else if (newFieldName.equalsIgnoreCase("ModifiedByAgentID"))
                    {
                        str.append(getModifiedByAgentId(lastEditedBy));

                    } else if (newFieldName.equalsIgnoreCase("CreatedByAgentID"))
                    {
                        str.append(getCreatorAgentId(null));

                    } else
                    {
                        Integer index            = null;
                        String  oldMappedColName = colNewToOldMap.get(newFieldName);
                        if (oldMappedColName != null)
                        {
                            index = oldNameIndex.get(oldMappedColName);

                        } else
                        {
                            index = oldNameIndex.get(newFieldName);
                            oldMappedColName = newFieldName;
                        }
                        
                        Object data;

                        if (index == null)
                        {
                            String msg = "convertGiftPreparations - Couldn't find new field name[" + newFieldName + "] in old field name in index Map";
                            log.warn(msg);
//                            stmt.close();
//                            tblWriter.logError(msg);
//                            showError(msg);
//                            throw new RuntimeException(msg);
                            data = null;
                        } else {
                            data = rs.getObject(index);
                        }
                        if (data != null)
                        {
                        	if (newFieldName.equalsIgnoreCase("GiftID"))
                        	{
                        		data = giftsIdMapper.get((Integer)data);
                        		
                        	} else
                        	{
	                            int idInx = newFieldName.lastIndexOf("ID");
	                            if (idMapperMgr != null && idInx > -1)
	                            {
	                                IdMapperIFace idMapper = idMapperMgr.get(tableName, oldMappedColName);
	                                if (idMapper != null)
	                                {
	                                	Integer oldId = rs.getInt(index);
	                                    data = idMapper.get(oldId);
	                                    if (data == null)
	                                    {
	                                    	String msg = "No Map ID for [" + tableName + "][" + oldMappedColName + "] for ID["+oldId+"]";
	                                    	log.error(msg);
	                                    	tblWriter.logError(msg);
	                                    }
	                                } else
	                                {
	                                	String msg = "No Map for [" + tableName + "][" + oldMappedColName + "]";
	                                	log.error(msg);
	                                	tblWriter.logError(msg);
	                                }
	                            }
                        	}
                        }
                        str.append(getStrValue(data, newFieldMetaData.get(i).getType()));
                    }
                }
                str.append(")");

                if (hasFrame)
                {
                    if (count % 500 == 0)
                    {
                        setProcess(count);
                    }

                } else
                {
                    if (count % 2000 == 0)
                    {
                        log.info("LoanPreparation Records: " + count);
                    }
                }

                try
                {
                    Statement updateStatement = newDBConn.createStatement();
                    if (BasicSQLUtils.myDestinationServerType != BasicSQLUtils.SERVERTYPE.MS_SQLServer)
                    {
                        removeForeignKeyConstraints(newDBConn, BasicSQLUtils.myDestinationServerType);
                    }
                    // log.debug("executring: " + str.toString());
                    // updateStatement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                    updateStatement.executeUpdate(str.toString());
                    updateStatement.clearBatch();
                    updateStatement.close();
                    updateStatement = null;

                } catch (SQLException e)
                {
                    log.error("Count: " + count);
                    e.printStackTrace();
                    log.error(e);
                    rs.close();
                    stmt.close();
                    throw new RuntimeException(e);
                }

                count++;
                // if (count > 10) break;
            } while (rs.next());

            if (hasFrame)
            {
                setProcess(count);
                log.info("Processed LoanPreparation " + count + " records.");
            } else
            {
                log.info("Processed LoanPreparation " + count + " records.");
            }
            rs.close();
            stmt.close();
            
            tblWriter.log(String.format("Determinations Processing Time: %s", timeLogger.end()));

        } catch (SQLException e)
        {
            e.printStackTrace();
            log.error(e);
            setIdentityInsertOFFCommandForSQLServer(newDBConn, "LoanPreparation", BasicSQLUtils.myDestinationServerType);
            throw new RuntimeException(e);
        }
        log.info("Done processing LoanPhysicalObject");
        setIdentityInsertOFFCommandForSQLServer(newDBConn, "LoanPreparation", BasicSQLUtils.myDestinationServerType);
        
        tblWriter.close();
        return true;

    }

    /**
     * Creates a Standard set of DataTypes for Collections
     * @param returnName the name of a DataType to return (ok if null)
     * @return the DataType requested
     */
    public DataType createDataTypes(final String returnName)
    {
        String[] dataTypeNames = { "Animal", "Plant", "Fungi", "Mineral", "Other" };

        DataType retDataType = null;
        try
        {
            Session localSession = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();

            for (String name : dataTypeNames)
            {
                DataType dataType = new DataType();
                dataType.initialize();
                dataType.setName(name);
                localSession.save(dataType);

                if (returnName != null && name.equals(returnName))
                {
                    retDataType = dataType;
                }
            }

            HibernateUtil.commitTransaction();

        } catch (Exception e)
        {
            log.error("******* " + e);
            HibernateUtil.rollbackTransaction();
            throw new RuntimeException(e);
        }
        return retDataType;
    }

 
    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public GeographyTreeDef createStandardGeographyDefinitionAndItems(final boolean doDelete)
    {
        if (doDelete)
        {
            // empty out any pre-existing tree definitions
            deleteAllRecordsFromTable(newDBConn, "geographytreedef", BasicSQLUtils.myDestinationServerType);
            deleteAllRecordsFromTable(newDBConn, "geographytreedefitem", BasicSQLUtils.myDestinationServerType);
        }

        Session localSession = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        GeographyTreeDef def = new GeographyTreeDef();
        def.initialize();
        def.setName("Default Geography Definition");
        def.setRemarks("A simple continent/country/state/county geography tree");
        def.setFullNameDirection(TreeDefIface.REVERSE);

        GeographyTreeDefItem planet = new GeographyTreeDefItem();
        planet.initialize();
        planet.setName("Planet");
        planet.setRankId(0);
        planet.setIsEnforced(true);
        planet.setFullNameSeparator(", ");

        GeographyTreeDefItem cont = new GeographyTreeDefItem();
        cont.initialize();
        cont.setName("Continent");
        cont.setRankId(100);
        cont.setFullNameSeparator(", ");

        GeographyTreeDefItem country = new GeographyTreeDefItem();
        country.initialize();
        country.setName("Country");
        country.setRankId(200);
        country.setIsInFullName(true);
        country.setFullNameSeparator(", ");

        GeographyTreeDefItem state = new GeographyTreeDefItem();
        state.initialize();
        state.setName("State");
        state.setRankId(300);
        state.setIsInFullName(true);
        state.setFullNameSeparator(", ");

        GeographyTreeDefItem county = new GeographyTreeDefItem();
        county.initialize();
        county.setName("County");
        county.setRankId(400);
        county.setIsInFullName(true);
        county.setFullNameSeparator(", ");

        // setup parents
        county.setParent(state);
        state.setParent(country);
        country.setParent(cont);
        cont.setParent(planet);

        // set the tree def for each tree def item
        planet.setTreeDef(def);
        cont.setTreeDef(def);
        country.setTreeDef(def);
        state.setTreeDef(def);
        county.setTreeDef(def);

        Set defItems = def.getTreeDefItems();
        defItems.add(planet);
        defItems.add(cont);
        defItems.add(country);
        defItems.add(state);
        defItems.add(county);

        localSession.save(def);

        HibernateUtil.commitTransaction();

        return def;
    }

    /**
     * @param dbConn
     * @return
     */
    @SuppressWarnings("unchecked")
    public static LithoStratTreeDef createStandardLithoStratDefinitionAndItems(final Connection dbConn)
    {
        // empty out any pre-existing tree definitions
        deleteAllRecordsFromTable(dbConn, "lithostrattreedef", BasicSQLUtils.myDestinationServerType);
        deleteAllRecordsFromTable(dbConn, "lithostrattreedefitem", BasicSQLUtils.myDestinationServerType);

        Session localSession = HibernateUtil.getCurrentSession();

        HibernateUtil.beginTransaction();

        LithoStratTreeDef def = DataBuilder.createLithoStratTreeDef("Standard LithoStrat Tree");
        BuildSampleDatabase.createSimpleLithoStrat(def, true);

        localSession.save(def);

        HibernateUtil.commitTransaction();

        return def;
    }

    /**
     * @return
     */
    public boolean convertDeaccessionCollectionObject()
    {
        deleteAllRecordsFromTable("deaccessionpreparation", BasicSQLUtils.myDestinationServerType);

        if (getNumRecords(oldDBConn, "deaccessioncollectionobject") == 0) 
        { 
            return true; 
        }

        Map<String, String> colNewToOldMap = createFieldNameMap(new String[] { "PreparationID", "CollectionObjectID", 
                                                                               "DeaccessionPreparationID", "DeaccessionCollectionObjectID" });

        setIdentityInsertONCommandForSQLServer(newDBConn, "deaccessionpreparation", BasicSQLUtils.myDestinationServerType);
        
        // Need to add Fields to ignore!
        
        if (copyTable(oldDBConn, newDBConn, "deaccessioncollectionobject", "deaccessionpreparation", 
                colNewToOldMap, null, BasicSQLUtils.mySourceServerType, BasicSQLUtils.myDestinationServerType))
        {
            log.info("deaccessionpreparation copied ok.");
        } else
        {
            log.error("problems coverting deaccessionpreparation");
        }
        setIdentityInsertOFFCommandForSQLServer(newDBConn, "deaccessionpreparation", BasicSQLUtils.myDestinationServerType);
        setFieldsToIgnoreWhenMappingNames(null);

        return true;
    }

    /**
     * @param fields
     * @param fieldName
     */
    protected void removeFieldsFromList(final List<String> fields, final String[] fieldNames)
    {
        for (String fieldName : fieldNames)
        {
            int fndInx = -1;
            int inx = 0;
            for (String fld : fields)
            {
                if (contains(fld, fieldName))
                {
                    fndInx = inx;
                }
                inx++;
            }

            if (fndInx != -1)
            {
                fields.remove(fndInx);
            }
        }
    }

    /**
     * @param tableName
     */
    protected void convertLocalityExtraInfo(final String tableName, final boolean isGeoCoordDetail)
    {
        removeForeignKeyConstraints(newDBConn, BasicSQLUtils.myDestinationServerType);
        
        String capName = capitalize(tableName);
        TableWriter tblWriter = convLogger.getWriter(capName + ".html", capName);
        setTblWriter(tblWriter);
        IdHashMapper.setTblWriter(tblWriter);
        
        setDesc("Converting " + capName);
        
        List<String> localityDetailNamesTmp = getFieldNamesFromSchema(newDBConn, tableName);

        List<String> localityDetailNames = new ArrayList<String>();
        Hashtable<String, Boolean> nameHash = new Hashtable<String, Boolean>();

        for (String fieldName : localityDetailNamesTmp)
        {
            localityDetailNames.add(fieldName);
            nameHash.put(fieldName, true);
            System.out.println("["+fieldName+"]");
        }

        String fieldList = buildSelectFieldList(localityDetailNames, null);
        log.info(fieldList);

        IdMapperIFace locIdMapper = idMapperMgr.get("locality", "LocalityID");
        IdMapperIFace agtIdMapper = idMapperMgr.get("agent", "AgentID");
        
        Statement updateStatement = null;
        try
        {
            updateStatement = newDBConn.createStatement();
            
            Hashtable<String, Boolean> usedFieldHash = new Hashtable<String, Boolean>();

            Statement stmt      = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            Integer   countRows = getCount("select count(LocalityID) from locality,geography where locality.GeographyID = geography.GeographyID");
            if (countRows != null)
            {
                frame.setProcess(0, countRows);
            }

            ResultSet rs = stmt.executeQuery("select locality.*,geography.* from locality LEFT JOIN geography on locality.GeographyID = geography.GeographyID ");

            StringBuilder colSQL    = new StringBuilder();
            StringBuilder valuesSQL = new StringBuilder();

            int rows = 0;
            while (rs.next())
            {
                usedFieldHash.clear();
                valuesSQL.setLength(0);

                boolean           hasData  = false;
                ResultSetMetaData metaData = rs.getMetaData();
                int               cols     = metaData.getColumnCount();
                for (int i = 1; i <= cols; i++)
                {
                    String colName = metaData.getColumnName(i); // Old Column Name
                    
                    if (colName.equals("GeoRefDetBy"))
                    {
                        colName = "AgentID";
                    }
                    
                    if ((nameHash.get(colName) == null || usedFieldHash.get(colName) != null) && !colName.startsWith("Range"))
                    {
                        if (rows == 0)
                        {
                            log.debug("Skipping[" + colName + "]");
                        }
                        continue;
                    }
                    
                    usedFieldHash.put(colName, true);

                    if (rows == 0)
                    {
                        System.err.println("["+colName+"]");
                        
                        if (colName.equals("Range"))
                        {
                            if (!isGeoCoordDetail)
                            {
                                if (colSQL.length() > 0) colSQL.append(",");
                                colSQL.append("RangeDesc");
                            }
                            
                        } else if (isGeoCoordDetail)
                        {
                            if (!colName.equals("RangeDirection"))
                            {
                                if (colSQL.length() > 0) colSQL.append(",");
                                colSQL.append(colName);
                            }
                            
                        } else 
                        {
                            if (colSQL.length() > 0) colSQL.append(",");
                            colSQL.append(colName);
                        }
                    }

                    String value;
                    if (colName.equals("LocalityID"))
                    {
                        Integer oldId = rs.getInt(i);
                        Integer newId = locIdMapper.get(oldId);
                        if (newId != null)
                        {
                            value = Integer.toString(newId);
                        } else
                        {
                            String msg = "Couldn't map LocalityId oldId[" + rs.getInt(i) + "]";
                            log.error(msg);
                            tblWriter.logError(msg);
                            value = "NULL";
                        }

                    } else if (isGeoCoordDetail && colName.equals("GeoRefDetDate"))
                    {
                        Integer dateInt = rs.getInt(i);
                        value = getStrValue(dateInt, "date");
                        
                    } else if (colName.startsWith("YesNo"))
                    {
                        Integer bool = rs.getInt(i);
                        if (bool == null)
                        {
                            value = "NULL";
                            
                        } else if (bool == 0)
                        {
                            value = "0";
                        } else
                        {
                            value = "1";
                        }
                    } else if (isGeoCoordDetail && colName.equals("AgentID"))
                    {
                        Integer agentID = (Integer)rs.getObject(i);
                        if (agentID != null)
                        {
                            Integer newID = agtIdMapper.get(agentID);
                            if (newID != null)
                            {
                                value = newID.toString();
                            } else
                            {
                                String msg = "Couldn't map GeoRefDetBY (Agent) oldId[" + agentID + "]";
                                log.error(msg);
                                tblWriter.logError(msg);
                                value = "NULL";
                            }
                        } else
                        {
                            value = "NULL";
                        }
                        
                    } else if (colName.equals("Range") || colName.equals("RangeDirection"))
                    {
                        if (!isGeoCoordDetail)
                        {
                            String range = rs.getString(i);
                            range = escapeStringLiterals(range);
                            if (range != null)
                            {
                                hasData = true;
                                value = "'" + range + "'";
                            } else
                            {
                                value = "NULL";
                            }
                        } else
                        {
                            value = null;
                        }
                    } else
                    {
                        Object obj = rs.getObject(i);
                        if (obj != null && 
                            !colName.equals("TimestampCreated") &&
                            !colName.equals("TimestampModified"))
                        {
                            hasData = true;
                        }
                        /*if (obj instanceof String)
                        {
                            String str = (String)obj;
                            int inx = str.indexOf('\'');
                            if (inx > -1)
                            {
                                obj = escapeStringLiterals(str);
                            }
                        }*/
                        value = getStrValue(obj);
                    }
                    // log.debug(colName+" ["+value+"]");

                    if (value != null)
                    {
                        if (valuesSQL.length() > 0)
                        {
                            valuesSQL.append(",");
                        }
                        valuesSQL.append(value);
                    }
                }

                if (hasData)
                {
                    String insertSQL = "INSERT INTO "
                            + tableName
                            + " ("
                            + colSQL.toString()
                            + ", Version, CreatedByAgentID, ModifiedByAgentID) "
                            + " VALUES(" + valuesSQL.toString() + ", 0, " + getCreatorAgentId(null)
                            + "," + getModifiedByAgentId(null) 
                            + ")";

                    /*if (true)
                    {
                        log.info(insertSQL);
                    }*/
                    try
                    {
                        updateStatement.executeUpdate(insertSQL);
                        updateStatement.clearBatch();

                    } catch (Exception ex)
                    {
                        System.out.println("isGeoCoordDetail: "+isGeoCoordDetail);
                        System.out.println(insertSQL);
                        ex.printStackTrace();
                    }
                }
                rows++;
                if (rows % 500 == 0)
                {
                    frame.setProcess(rows);
                }
            }

            rs.close();
            stmt.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                updateStatement.close();
            } catch (SQLException ex) 
            {
                ex.printStackTrace();
            }
        }

    }

    /**
     * 
     */
    public void convertLocality()
    {
        int errorsToShow = BasicSQLUtils.SHOW_ALL;
        log.debug("Preparing to convert localities");
        // Ignore these field names from new table schema when mapping IDs

        setIdentityInsertOFFCommandForSQLServer(newDBConn, "collectionobject", BasicSQLUtils.myDestinationServerType);
        setIdentityInsertONCommandForSQLServer(newDBConn, "locality", BasicSQLUtils.myDestinationServerType);
        deleteAllRecordsFromTable("locality", BasicSQLUtils.myDestinationServerType);


        Hashtable<String, String> newToOldColMap = new Hashtable<String, String>();
        newToOldColMap.put("Visibility", "GroupPermittedToView");
        
        String[] fieldsToIgnore = new String[] { "GML", "NamedPlaceExtent", "GeoRefAccuracyUnits",
                "GeoRefDetRef", "GeoRefDetDate", "GeoRefDetBy", "NoGeoRefBecause", "GeoRefRemarks",
                "GeoRefVerificationStatus", "NationalParkName", "VisibilitySetBy",
                "GeoRefDetByID",
                "Drainage",   // TODO make sure this is right, meg added due to conversion non-mapping errors????
                "Island",     // TODO make sure this is right, meg added due to conversion non-mapping errors????
                "IslandGroup",// TODO make sure this is right, meg added due to conversion non-mapping errors????
                "WaterBody",  // TODO make sure this is right, meg added due to conversion non-mapping errors????
                "Version", 
                "CreatedByAgentID", 
                "CollectionMemberID",
                "ShortName", 
                "DisciplineID",
                "GUID",
                "GML",
                "SrcLatLongUnit",
                "Visibility",
                "VisibilitySetByID",
                // Special String
                "LocalityName",
                "NamedPlace",
                "RelationToNamedPlace",
                "SGRStatus",
                "PaleoContextID",
                "Text3", "Text4", "Text5", "VerbatimLatitude", "VerbatimLongitude" };
        
        setFieldsToIgnoreWhenMappingNames(fieldsToIgnore);

        errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for LocalityID
        errorsToShow &= ~BasicSQLUtils.SHOW_NAME_MAPPING_ERROR; //Assuming new sp6 fields do not have sp5 equivalent
        setShowErrors(errorsToShow);
        
        TableWriter tblWriter = convLogger.getWriter("Locality.html", "Localities");
        setTblWriter(tblWriter);
        IdHashMapper.setTblWriter(tblWriter);
        
        Map<String, String> mappedFields = createFieldNameMap(new String[] { "ModifiedByAgentID", "LastEditedBy", });

        String sql = "SELECT locality.*,g.* FROM locality LEFT JOIN geography g on locality.GeographyID = g.GeographyID WHERE locality.GeographyID IS NOT NULL";

        if (copyTable(oldDBConn, newDBConn, sql, "locality", "locality", mappedFields, null, BasicSQLUtils.mySourceServerType, BasicSQLUtils.myDestinationServerType))
        {
            log.info("Locality/Geography copied ok.");
        } else
        {
            log.error("Copying locality/geography (fields) to new Locality");
        }
        
        setFieldsToIgnoreWhenMappingNames(fieldsToIgnore);
        
        sql = "SELECT * FROM locality WHERE locality.GeographyID IS NULL";
        
        if (copyTable(oldDBConn, newDBConn, sql, "locality", "locality", mappedFields, null, BasicSQLUtils.mySourceServerType, BasicSQLUtils.myDestinationServerType))
        {
            log.info("Locality/Geography copied ok.");
        } else
        {
            log.error("Copying locality/geography (fields) to new Locality");
        }
        
        frame.setProcess(0, BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM locality"));
        
        PreparedStatement pStmt = null;
        Statement         stmt  = null;
        sql = "SELECT LocalityID, LocalityName, NamedPlace, RelationToNamedPlace, Remarks FROM locality ORDER BY LocalityID";
        try
        {
            IdMapperIFace locMapper = idMapperMgr.get("locality_LocalityID");
            pStmt = newDBConn.prepareStatement("UPDATE locality SET LocalityName=?, NamedPlace=?, RelationToNamedPlace=?, Remarks=? WHERE LocalityID=?");
            stmt  = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            ResultSet rs = stmt.executeQuery(sql);
            int cnt = 0;
            while (rs.next())
            {
                int     oldId = rs.getInt(1);
                Integer newId = locMapper.get(oldId);
                if (newId != null)
                {
                    pStmt.setString(1, rs.getString(2));
                    pStmt.setString(2, rs.getString(3));
                    pStmt.setString(3, rs.getString(4));
                    pStmt.setString(4, rs.getString(5));
                    pStmt.setInt(5,    newId);
                    
                    pStmt.execute();
                    
                    /*if (!pStmt.execute())
                    {
                        if ()
                        String msg = "Error Updating OldId ["+rs.getInt(1)+"] NewId ["+newId+"]";
                        log.error(msg);
                        tblWriter.logErrors(Integer.toString(rs.getInt(1)), msg);
                    }*/
                    
                } else
                {
                    String msg = "No Mapping for OldId ["+rs.getInt(1)+"]";
                    log.error(msg);
                    tblWriter.logErrors(Integer.toString(rs.getInt(1)), msg);
                }
                cnt++;
                if (cnt % 500 == 0)
                {
                    frame.setProcess(cnt);
                }
            }
            
            frame.setProcess(cnt);
            
        } catch (Exception ex)
        {
            log.error(ex);
            tblWriter.logErrors("Exception", ex.toString());
        } finally
        {
            try
            {
                if (stmt != null) stmt.close();
                if (pStmt != null) pStmt.close();
                
            } catch (SQLException ex)
            {
                
            }
        }

        convertLocalityExtraInfo("localitydetail", false);
        convertLocalityExtraInfo("geocoorddetail", true);

        setFieldsToIgnoreWhenMappingNames(null);
        setIdentityInsertOFFCommandForSQLServer(newDBConn, "locality", BasicSQLUtils.myDestinationServerType);
        
        setTblWriter(null);
        IdHashMapper.setTblWriter(null);
    }
    
    /**
     * @param fieldName
     */
    private void fixGeography(final String fieldName)
    {
        int cnt = BasicSQLUtils.getCountAsInt(oldDBConn, String.format("SELECT COUNT(*) FROM geography WHERE %s = 'null'", fieldName));
        if (cnt > 0)
        {
            int recs = BasicSQLUtils.update(oldDBConn, String.format("UPDATE geography SET %s = NULL WHERE %s = 'null'", fieldName, fieldName));
            if (cnt == recs)
            {
                log.debug(String.format("%d Geography field %s  was updated correctly .", recs, fieldName));
            } else
            {
                log.debug(String.format("Geography field %s  was updated in error %d / %d.", fieldName, cnt, recs));
            }
        }
    }
    
    
    //-------------------------------------------------------------------------------------------------
    //
    //-------------------------------------------------------------------------------------------------
    private static final HashMap<Integer, Agent> agentHash = new HashMap<Integer, Agent>();
    /**
     * @param sessionArg
     * @param agtId
     * @return
     */
    private Agent getAgentObj(final Session sessionArg, final Integer agtId)
    {
        if (agtId != null)
        {
            Agent agent = agentHash.get(agtId);
            if (agent != null)
            {
                return agent;
            }
            
            List<?> list = sessionArg.createQuery("FROM Agent WHERE id = "+agtId).list();
            if (list != null && list.size() == 1)
            {
                agentHash.put(agtId, (Agent)list.get(0));
                return (Agent)list.get(0);
            }
        }
        return null;
    }

    /**
     * @param sessionArg
     * @param agtId
     * @return
     */
    private Agent getCreatedByAgent(final Session sessionArg, final Integer agtId)
    {
        Agent agent = getAgentObj(sessionArg, agtId);
        if (agent == null)
        {
            agent = getAgentObj(sessionArg, getCurAgentCreatorID());
        }
        return null;
    }
    
    /**
     * @param sessionArg
     * @param agtId
     * @return
     */
    private Agent getModifiedByAgent(final Session sessionArg, final Integer agtId)
    {
        Agent agent = getAgentObj(sessionArg, agtId);
        if (agent == null)
        {
            agent = getAgentObj(sessionArg, getCurAgentModifierID());
        }
        return null;
    }
    
    /**
     * @param id
     * @param desc
     * @param str
     * @param len
     * @return
     */
    private String fixSize(final TableWriter tblWriter, final int id, final String desc, final String str, final int len)
    {
        String s = str;
        if (isNotEmpty(str))
        {
            if (s.length() > len)
            {
                s   = str.substring(0, len);
                String msg = String.format("Id; %d %s - %s chopped[%s]", id, desc, str, str.substring(64, str.length()));
                log.debug(msg);
                tblWriter.logError(msg);
            }
        }
        return s;
    }

    /**
     * @param treeDef
     * @throws SQLException
     */
    public void convertGeography(final GeographyTreeDef treeDef,
                                 final String           dispName,
                                 final boolean          firstTime) throws SQLException
    {
        TableWriter tblWriter = convLogger.getWriter("Geography" + (dispName != null ? dispName : "") + ".html", "Geography");
        setTblWriter(tblWriter);
        
        IdHashMapper.setTblWriter(tblWriter);

        if (firstTime)
        {
            // empty out any pre-existing records
            deleteAllRecordsFromTable(newDBConn, "geography", BasicSQLUtils.myDestinationServerType);
        }

        IdTableMapper geoIdMapper = (IdTableMapper)IdMapperMgr.getInstance().get("geography", "GeographyID");
        if (geoIdMapper == null)
        {
            // create an ID mapper for the geography table (mainly for use in converting localities)
            geoIdMapper = IdMapperMgr.getInstance().addTableMapper("geography", "GeographyID");
        } else
        {
            geoIdMapper.clearRecords();
        }
        
        Hashtable<Integer, Geography> oldIdToGeoMap = new Hashtable<Integer, Geography>();

        // get a Hibernate session for saving the new records
        Session localSession = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        // get all of the old records
        String    sql       = "SELECT GeographyID,ContinentOrOcean,Country,State,County,LastEditedBy FROM geography";
        Statement statement = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(Integer.MIN_VALUE);
        
        ResultSet oldGeoRecords = statement.executeQuery(sql);
        
        fixGeography("ContinentOrOcean");
        fixGeography("Country");
        fixGeography("State");
        fixGeography("County");

        if (hasFrame)
        {
            if (oldGeoRecords.last())
            {
                setProcess(0, oldGeoRecords.getRow());
                oldGeoRecords.first();
            }
        } else
        {
            oldGeoRecords.first();
        }

        // setup the root Geography record (planet Earth)
        Geography planetEarth = new Geography();
        planetEarth.initialize();
        planetEarth.setName("Earth");
        planetEarth.setCommonName("Earth");
        planetEarth.setRankId(0);
        planetEarth.setDefinition(treeDef);
        for (GeographyTreeDefItem defItem : treeDef.getTreeDefItems())
        {
            if (defItem.getRankId() == 0)
            {
                planetEarth.setDefinitionItem(defItem);
                break;
            }
        }
        GeographyTreeDefItem defItem = treeDef.getDefItemByRank(0);
        planetEarth.setDefinitionItem(defItem);

        int counter = 0;
        // for each old record, convert the record
        do
        {
            if (counter % 500 == 0)
            {
                if (hasFrame)
                {
                    setProcess(counter);

                } else
                {
                    log.info("Converted " + counter + " geography records");
                }
            }

            // grab the important data fields from the old record
            int    oldId        = oldGeoRecords.getInt(1);
            String cont         = fixSize(tblWriter, oldId, "continent", oldGeoRecords.getString(2), 64);
            String country      = fixSize(tblWriter, oldId, "country",   oldGeoRecords.getString(3), 64);
            String state        = fixSize(tblWriter, oldId, "state",     oldGeoRecords.getString(4), 64);
            String county       = fixSize(tblWriter, oldId, "county",    oldGeoRecords.getString(5), 64);
            String lastEditedBy = oldGeoRecords.getString(6);
            
            Integer agtId           = getCreatorAgentId(lastEditedBy);
            Agent   createdByAgent  = getCreatedByAgent(localSession, agtId);
            Agent   modifiedByAgent = getAgentObj(localSession, getCurAgentModifierID());
            
            
            /*cont    = isNotEmpty(county)  && cont.equals("null")    ? null : cont;
            country = isNotEmpty(country) && country.equals("null") ? null : country;
            state   = isNotEmpty(state)   && state.equals("null")   ? null : state;
            county  = isNotEmpty(county)  && county.equals("null")  ? null : county;
            */
            
            if (isEmpty(cont) && isEmpty(country) && 
                isEmpty(state) && isEmpty(county))
            {
                //String msg = "For Record Id["+oldId+"] Continent, Country, State and County are all null.";
                //log.error(msg);
                //tblWriter.logError(msg);
                
                cont    = "Undefined";
                country = "Undefined";
                state   = "Undefined";
                county  = "Undefined";
                
            } else if (isEmpty(cont) && isEmpty(country) && isEmpty(state))
            {
                //String msg = "For Record Id["+oldId+"] Continent, Country and State are all null.";
                //log.error(msg);
                //tblWriter.logError(msg);
                
                cont    = "Undefined";
                country = "Undefined";
                state   = "Undefined";
                
            } else if (isEmpty(cont) && isEmpty(country))
            {
                //String msg = "For Record Id["+oldId+"] Country is null.";
                //log.error(msg);
                //tblWriter.logError(msg);
                
                cont    = "Undefined"; 
                country = "Undefined"; 
                
            } else if (isEmpty(cont))
            {
                //String msg = "For Record Id["+oldId+"] Country is null.";
                //log.error(msg);
                //tblWriter.logError(msg);
                
                cont = "Undefined"; 
            }

            // create a new Geography object from the old data
            List<Geography> newGeos = convertOldGeoRecord(cont, country, state, county, createdByAgent, modifiedByAgent, planetEarth);
            if (newGeos.size() > 0)
            {
                Geography lowestLevel = newGeos.get(newGeos.size() - 1);

                oldIdToGeoMap.put(oldId, lowestLevel);
            }
            
            counter++;
            
        } while (oldGeoRecords.next());

        if (hasFrame)
        {
            setProcess(counter);

        } else
        {
            log.info("Converted " + counter + " geography records");
        }

        TreeHelper.fixFullnameForNodeAndDescendants(planetEarth);
        planetEarth.setNodeNumber(1);
        fixNodeNumbersFromRoot(planetEarth);

        localSession.save(planetEarth);

        HibernateUtil.commitTransaction();
        log.info("Converted " + counter + " geography records");

        if (shouldCreateMapTables)
        {
            // add all of the ID mappings
            for (Integer oldId : oldIdToGeoMap.keySet())
            {
                Geography geo = oldIdToGeoMap.get(oldId);
                geoIdMapper.put(oldId, geo.getId());
            }
        }

        if (firstTime)
        {
            // set up Geography foreign key mapping for locality
            idMapperMgr.mapForeignKey("Locality", "GeographyID", "Geography", "GeographyID");
        }
    }

    /**
     * Using the data passed in the parameters, create a new Geography object and attach it to the
     * Geography tree rooted at geoRoot.
     * 
     * @param cont continent or ocean name
     * @param country country name
     * @param state state name
     * @param county county name
     * @param createdByAgent
     * @param modifiedByAgent
     * @param geoRoot the Geography tree root node (planet)
     * @return a list of Geography items created, the lowest level being the last item in the list
     */
    protected List<Geography> convertOldGeoRecord(String cont,
                                                  String country,
                                                  String state,
                                                  String county,
                                                  Agent  createdByAgent,
                                                  Agent  modifiedByAgent,
                                                  Geography geoRoot)
    {
        List<Geography> newRecords = new Vector<Geography>();
        String levelNames[] = { cont, country, state, county };
        int levelsToBuild = 0;
        for (int i = 4; i > 0; --i)
        {
            if (levelNames[i - 1] != null)
            {
                levelsToBuild = i;
                break;
            }
        }

        Geography prevLevelGeo = geoRoot;
        for (int i = 0; i < levelsToBuild; ++i)
        {
            Geography newLevelGeo = buildGeoLevel(levelNames[i], createdByAgent, modifiedByAgent, prevLevelGeo);
            newRecords.add(newLevelGeo);
            prevLevelGeo = newLevelGeo;
        }

        return newRecords;
    }

    /**
     * @param nameArg
     * @param modifiedByAgent
     * @param createdByAgent
     * @param parentArg
     * @return
     */
    protected Geography buildGeoLevel(final String nameArg,
                                      final Agent modifiedByAgent,
                                      final Agent createdByAgent, 
                                      final Geography parentArg)
    {
        String name = nameArg;
        if (name == null)
        {
            name = "N/A";
        }

        // search through all of parent's children to see if one already exists with the same name
        Set<Geography> children = parentArg.getChildren();
        for (Geography child : children)
        {
            if (name.equalsIgnoreCase(child.getName()))
            {
                // this parent already has a child by the given name
                // don't create a new one, just return this one
                return child;
            }
        }

        // we didn't find a child by the given name
        // we need to create a new Geography record
        Geography newGeo = new Geography();
        newGeo.initialize();
        newGeo.setName(name);
        newGeo.setParent(parentArg);
        parentArg.addChild(newGeo);
        newGeo.setDefinition(parentArg.getDefinition());
        int newGeoRank = parentArg.getRankId() + 100;
        GeographyTreeDefItem defItem = parentArg.getDefinition().getDefItemByRank(newGeoRank);
        newGeo.setDefinitionItem(defItem);
        newGeo.setRankId(newGeoRank);
        
        newGeo.setCreatedByAgent(createdByAgent);
        newGeo.setModifiedByAgent(modifiedByAgent);
        
        return newGeo;
    }

    /**
     * @return
     */
    public boolean convertHabitat()
    {
        deleteAllRecordsFromTable("collectingeventattribute", BasicSQLUtils.myDestinationServerType);

        if (getNumRecords(oldDBConn, "habitat") == 0) 
        { 
            return true; 
        }

        setFieldsToIgnoreWhenMappingNames(getHabitatAttributeToIgnore());
        
        setIdentityInsertONCommandForSQLServer(newDBConn, "collectingeventattribute", BasicSQLUtils.myDestinationServerType);
        
        clearValueMapper();
        addToValueMapper("CollectionMemberID", getCollectionMemberIDValueMapper());
        addToValueMapper("CreatedByAgentID",   getAgentCreatorValueMapper());
        addToValueMapper("ModifiedByAgentID",  getAgentModiferValueMapper());
        addToValueMapper("Version",            getVersionValueMapper());
        addToValueMapper("DivisionID",         getDivisionValueMapper());
        addToValueMapper("DisciplineID",       getDisciplineValueMapper());
        addToValueMapper("IsPrimary",          getIsPrimaryValueMapper());
        addToValueMapper("SrcLatLongUnit",     getSrcLatLongUnitValueMapper());
        
        int errorsToShow = BasicSQLUtils.SHOW_ALL;
        errorsToShow &= ~BasicSQLUtils.SHOW_NULL_FK; // Turn off this error for DeaccessionPhysicalObjectID
        errorsToShow &= ~BasicSQLUtils.SHOW_NAME_MAPPING_ERROR; //Assuming any new fields in CollectingEventAttribute do not have Sp5 equivalents.
        setShowErrors(errorsToShow);
        
        // Need to add Fields to ignore!
        
        if (copyTable(oldDBConn, newDBConn, "habitat", "collectingeventattribute", 
                createFieldNameMap(getHabitatAttributeMappings()), null, BasicSQLUtils.mySourceServerType, BasicSQLUtils.myDestinationServerType))
        {
            log.info("habitat copied ok.");
        } else
        {
            log.error("problems coverting habitat");
        }
        setIdentityInsertOFFCommandForSQLServer(newDBConn, "collectingeventattribute", BasicSQLUtils.myDestinationServerType);
        setFieldsToIgnoreWhenMappingNames(null);
        setShowErrors(BasicSQLUtils.SHOW_ALL);
        
        return true;
    }
    
    /**
     * 
     */
    public void updateHabitatIds()
    {
        PreparedStatement pStmtUpd = null;
        Statement         stmt     = null;
        
        IdMapperIFace ceMapper = IdMapperMgr.getInstance().get("collectingevent", "CollectingEventID");
        IdMapperIFace hbMapper = IdMapperMgr.getInstance().get("habitat", "HabitatID");
        
        try
        {
            String sql = " SELECT CollectingEventID, HabitatID FROM collectingevent Inner Join habitat ON CollectingEventID = HabitatID";
            
            stmt       = oldDBConn.createStatement();
            pStmtUpd   = newDBConn.prepareStatement("UPDATE collectingevent SET CollectingEventAttributeID=?, DisciplineID=? WHERE CollectingEventID = ?");
            
            int cnt = 0;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                Integer ceId   = rs.getInt(1);
                Integer hbId   = rs.getInt(2);
                
                Integer newCEID = ceMapper.get(ceId);
                Integer newHBID = hbMapper.get(hbId);
                
                Integer colMemID = BasicSQLUtils.getCount("SELECT DisciplineID FROM collectingevent WHERE CollectingEventID = " + newCEID);
                if (colMemID == null)
                {
                    log.debug("DisciplineID is null for CE ID " + newCEID);    
                }
                
                if (newHBID != null)
                {
                    pStmtUpd.setInt(1, newHBID);
                    pStmtUpd.setInt(2, colMemID);
                    pStmtUpd.setInt(3, newCEID);
                    pStmtUpd.execute();
                    cnt++;
                }
            }
            rs.close();
            
            log.debug("Updated CollectingEvents: "+cnt);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                stmt.close();
                pStmtUpd.close();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

    }
    
    /**
     * Collection Object Attributes
     */
    public void updateBioLogicalObjAttrIds()
    {
        PreparedStatement pStmtUpd = null;
        Statement         stmt     = null;
        
        IdMapperIFace ceMapper = IdMapperMgr.getInstance().get("collectionobjectcatalog",    "CollectionObjectCatalogID");
        IdMapperIFace hbMapper = IdMapperMgr.getInstance().get("biologicalobjectattributes", "BiologicalObjectAttributesID");
        
        try
        {
            String sql = "SELECT c.CollectionObjectID, p.BiologicalObjectAttributesID FROM collectionobject c INNER Join biologicalobjectattributes p ON c.CollectionObjectID = p.BiologicalObjectAttributesID";
            
            stmt       = oldDBConn.createStatement();
            pStmtUpd   = newDBConn.prepareStatement("UPDATE collectionobject SET CollectionObjectAttributeID=? WHERE CollectionObjectID = ?");
            
            int cnt = 0;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                Integer ceId   = rs.getInt(1);
                Integer hbId   = rs.getInt(2);
                
                Integer newCEID = ceMapper.get(ceId);
                Integer newHBID = hbMapper.get(hbId);
                
                if (newHBID != null)
                {
                    pStmtUpd.setInt(1, newHBID);
                    pStmtUpd.setInt(2, newCEID);
                    pStmtUpd.execute();
                    cnt++;
                }
            }
            rs.close();
            
            log.debug("Updated Preparations: "+cnt);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                stmt.close();
                pStmtUpd.close();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Collection Object Attributes
     */
    public void updatePrepAttrIds()
    {
        PreparedStatement pStmtUpd = null;
        Statement         stmt     = null;
        
        IdMapperIFace coMapper = IdMapperMgr.getInstance().get("collectionobject",        "CollectionObjectID");
        IdMapperIFace ccMapper = IdMapperMgr.getInstance().get("collectionobjectcatalog", "CollectionObjectCatalogID");
        IdMapperIFace ppMapper = IdMapperMgr.getInstance().get("preparation",             "PreparationID");
        
        try
        {
            String sql = "SELECT c.CollectionObjectID, p.PreparationID FROM collectionobject c INNER Join preparation p ON c.CollectionObjectID = p.PreparationID";
            
            stmt       = oldDBConn.createStatement();
            pStmtUpd   = newDBConn.prepareStatement("UPDATE preparation SET PreparationAttributeID=? WHERE PreparationID = ?");
            
            int cnt = 0;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                Integer coId   = rs.getInt(1);
                Integer ppId   = rs.getInt(2);
                
                Integer newCOID = coMapper.get(coId);
                Integer newPPID = ppMapper.get(ppId);
                
                if (newCOID == null)
                {
                    newCOID = ccMapper.get(coId);
                }
                
                if (newPPID != null && newCOID != null)
                {
                    pStmtUpd.setInt(1, newPPID);
                    pStmtUpd.setInt(2, newCOID);
                    pStmtUpd.execute();
                    cnt++;
                    
                } else
                {
                    log.debug("newPPID: "+newPPID+"  or newCOID: "+newCOID+"  was null coId: "+coId+"  ppId "+ppId);
                }
            }
            rs.close();
            
            log.debug("Updated Preparations: "+cnt);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                stmt.close();
                pStmtUpd.close();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * @param treeDef
     * @throws SQLException
     */
    public void convertLithoStratGeneral(final LithoStratTreeDef treeDef, 
                                  final LithoStrat        earth,
                                  final TableWriter       tblWriter,
                                  final String            srcTableName) throws SQLException
    {
        Statement stmt = null;
        ResultSet rs   = null;
        String s = "";
        try
        {
            // get a Hibernate session for saving the new records
            Session localSession = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
    
            int count = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) FROM "+srcTableName);
            if (count < 1) return;
            
            if (hasFrame)
            {
                setProcess(0, count);
            }
            
            // create an ID mapper for the geography table (mainly for use in converting localities)
            IdHashMapper lithoStratIdMapper = IdMapperMgr.getInstance().addHashMapper("stratigraphy_StratigraphyID", true);
            if (lithoStratIdMapper == null)
            {
                UIRegistry.showError("The lithoStratIdMapper was null.");
                return;
            }
            
            IdMapperIFace gtpIdMapper = IdMapperMgr.getInstance().get("geologictimeperiod", "GeologicTimePeriodID");
                        
            IdMapperIFace ceMapper = IdMapperMgr.getInstance().get("collectingevent", "CollectingEventID");
            if (ceMapper == null) {
                ceMapper = IdMapperMgr.getInstance().addTableMapper("collectingevent", "CollectingEventID", null, false);
            }
            String sql  = String.format("SELECT s.StratigraphyID, s.SuperGroup, s.Group, s.Formation, s.Member, s.Bed, Remarks, " +
                                        "Text1, Text2, Number1, Number2, YesNo1, YesNo2, GeologicTimePeriodID FROM %s s " +
                                        "ORDER BY s.StratigraphyID", srcTableName);
            
            stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            rs   = stmt.executeQuery(sql);
            
            Map<Integer, Pair<Integer, Integer>> stratHash = new HashMap<Integer, Pair<Integer,Integer>>();
            
            int stratsWithNoGTP       = 0;
            int stratsWithNoMappedGTP = 0;
            int missingCEMapping      = 0;
            
            int lithoCnt = 0;
    
            int counter = 0;
            // for each old record, convert the record
            while (rs.next()) {
				if (counter % 500 == 0) {
					if (hasFrame) {
						setProcess(counter);

					} else {
						log.info("Converted " + counter
								+ " Stratigraphy records");
					}
				}
    
                // grab the important data fields from the old record
                int oldStratId    = rs.getInt(1);            // This is a one-to-one with CollectingEvent
                String superGroup = rs.getString(2);
                String lithoGroup = rs.getString(3);
                String formation  = rs.getString(4);
                String member     = rs.getString(5);
                String bed        = rs.getString(6);
                String remarks    = escapeStringLiterals(rs.getString(7));
                String text1      = escapeStringLiterals(rs.getString(8));
                String text2      = escapeStringLiterals(rs.getString(9));
                BigDecimal number1    = rs.getObject(10) != null ? rs.getBigDecimal(10)  : null;
                BigDecimal number2    = rs.getObject(11) != null ? rs.getBigDecimal(11)  : null;
                Boolean yesNo1    = rs.getObject(12) != null ? rs.getBoolean(12) : null;
                Boolean yesNo2    = rs.getObject(13) != null ? rs.getBoolean(13) : null;
                Integer oldGTPId  = rs.getObject(14) != null ? rs.getInt(14)     : null;
                
                // Check to see if there is any Litho information OR an GTP Id
                // If both are missing then skip the record.
                boolean hasLithoFields = isNotEmpty(superGroup) || isNotEmpty(lithoGroup) || isNotEmpty(formation) || isNotEmpty(member);
                if (!hasLithoFields && oldGTPId == null) {
                    continue;
                }
                
                Integer gtpId = null;
                if (oldGTPId != null) {
                    gtpId = gtpIdMapper.get(oldGTPId);
                    if (gtpId == null) {
                        tblWriter.logError("Old GTPID["+gtpId+"] in the Strat record could not be mapped for Old StratID["+oldStratId+"]");
                        stratsWithNoMappedGTP++;
                    }
                } else {
                    stratsWithNoGTP++;
                }
    
                // There may not be any Litho information to add to the LithoStrat tree, 
                // but it did have GTP Information if we got here
                Integer lithoStratID = null;
                if (hasLithoFields) {
                    // create a new Geography object from the old data
                    LithoStrat[] newStrats = convertOldStratRecord(superGroup, lithoGroup, formation, member, bed, remarks, 
                                                                   text1, text2, number1, number2, yesNo1, yesNo2,
                                                                   earth, localSession);
                    
                    LithoStrat newStrat = getLastLithoStrat(newStrats);
                    counter++;
                    lithoCnt += newStrats.length;
        
                    // Map Old LithoStrat ID to the new Tree Id
                    //System.out.println(oldStratId + " " + newStrat.getLithoStratId());
                    if (newStrat != null) {
                        lithoStratID = newStrat.getLithoStratId();
                    	lithoStratIdMapper.put(oldStratId, newStrat.getLithoStratId());
                    } else {
                        String msg = String.format("Strat Fields were all null for oldID", oldStratId);
                        tblWriter.logError(msg);
                        log.error(msg);
                        missingCEMapping++;
                    }
                }
                if (lithoStratID != null || gtpId != null) {
                    Integer newCEId = ceMapper.get(oldStratId);
                    if (newCEId == null) {
                        String msg = String.format("No CE mapping for Old StratId %d, when they are a one-to-one.", oldStratId);
                        tblWriter.logError(msg);
                        log.error(msg);
                        missingCEMapping++;
                    } else {
                    	stratHash.put(newCEId, new Pair<Integer, Integer>(gtpId, lithoStratID));
                    }
                }
            }
            stmt.close();
            
            System.out.println("lithoCnt: "+lithoCnt);
    
            if (hasFrame) {
                setProcess(counter);
            } else {
                log.info("Converted " + counter + " Stratigraphy records");
            }
    
            TreeHelper.fixFullnameForNodeAndDescendants(earth);
            earth.setNodeNumber(1);
            fixNodeNumbersFromRoot(earth);
    
            HibernateUtil.commitTransaction();
            log.info("Converted " + counter + " Stratigraphy records");
            
            rs.close();
            
            Statement updateStatement = newDBConn.createStatement();
            
            int ceCnt    = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT Count(CollectingEventID) FROM collectingevent");
            int stratCnt = BasicSQLUtils.getCountAsInt(oldDBConn, String.format("SELECT Count(CollectingEventID) FROM collectingevent " +
            		                                                            "INNER JOIN %s ON CollectingEventID = StratigraphyID", srcTableName));
            
            String msg = String.format("There are %d CE->Strat and %d CEs. The diff is %d", stratCnt, ceCnt, (ceCnt - stratCnt));
            tblWriter.log(msg);
            log.debug(msg);
            
            // Create a PaleoContext for each ColObj
            stmt = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);

            int processCnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM collectionobject WHERE CollectingEventID IS NOT NULL");
            if (frame != null) {
                frame.setDesc("Converting PaleoContext...");
                frame.setProcess(0, processCnt);
            }
            
            TreeSet<Integer> missingStratIds = new TreeSet<Integer>();

            int missingStrat = 0;
            int missingGTP   = 0;
            int coUpdateCnt  = 0;
            int cnt          = 0;
            sql  = "SELECT CollectionObjectID, CollectingEventID FROM collectionobject WHERE CollectingEventID IS NOT NULL ORDER BY CollectionObjectID";
            rs   = stmt.executeQuery(sql);
            while (rs.next()) {
                int     coId = rs.getInt(1); // New CO Id
                Integer ceId = rs.getInt(2); // New CE Id
                
                Pair<Integer, Integer> strat = stratHash.get(ceId);
                Integer newLithoId = null;
                Integer gtpId = null;
                if (strat != null) {
                	gtpId = strat.getFirst();
                	newLithoId = strat.getSecond();
                }
                
                if (newLithoId == null) {
                    missingStrat++;
                    missingStratIds.add(ceId);
                    if (gtpId == null) continue;
                }
                                
                try {
                    String updateStr = "INSERT INTO paleocontext (TimestampCreated, TimestampModified, DisciplineID, Version, CreatedByAgentID, ModifiedByAgentID, LithoStratID, ChronosStratID) "
                            + "VALUES ('"
                            + nowStr
                            + "','"
                            + nowStr
                            + "',"
                            + getDisciplineId()
                            + ", 0, " 
                            + getCreatorAgentId(null) + "," + getModifiedByAgentId(null) 
                            +"," + (newLithoId != null ? newLithoId : "NULL")
                            +"," + (gtpId != null ? gtpId : "NULL")
                            + ")";
                    updateStatement.executeUpdate(updateStr, Statement.RETURN_GENERATED_KEYS);
                    
                    Integer paleoContextID = getInsertedId(updateStatement);
                    if (paleoContextID == null) {
                        throw new RuntimeException("Couldn't get the Agent's inserted ID");
                    }
                    
                    String sqlUpdate = "UPDATE collectionobject SET PaleoContextID=" + paleoContextID + " WHERE CollectionObjectID = " + coId;
                    updateStatement.executeUpdate(sqlUpdate);
                    coUpdateCnt++;
                } catch (SQLException e) {
                    e.printStackTrace();
                    log.error(e);
                    showError(e.getMessage());
                    throw new RuntimeException(e);
                }
                processCnt++;
                if (frame != null && cnt % 100 == 0) frame.setProcess(cnt);
            }
            rs.close();
            stmt.close();
            
            if (frame != null) frame.setProcess(processCnt);
            
            msg = String.format("There are %d unmappable Strat Records and %d unmappable GTP records.", missingStrat, missingGTP);
            tblWriter.log(msg);
            log.debug(msg);
            
            msg = String.format("There are %d CO records updated.", coUpdateCnt);
            tblWriter.log(msg);
            log.debug(msg);
            updateStatement.close();
            
            msg = String.format("No CE mapping for Old StratId Count: %d", missingCEMapping);
            tblWriter.logError(msg);
            log.error(msg);
            
            msg = String.format("Strats with No GTP Count: %d", stratsWithNoGTP);
            tblWriter.logError(msg);
            log.error(msg);
            
            msg = String.format("Strats with missing Mapping to GTP Count: %d", stratsWithNoMappedGTP);
            tblWriter.logError(msg);
            log.error(msg);
            
            msg = String.format("Number of Old StratIds mapped to a new Strat ID Count: %d", lithoStratIdMapper.size());
            tblWriter.logError(msg);
            log.error(msg);
            
            StringBuilder sb = new StringBuilder();
            sb.append("Missing New Strat: ");
            if (missingStratIds.size() == 0) sb.append("None");

            for (Integer id : missingStratIds)
            {
               sb.append(String.format("%d, ", id));
            }
            tblWriter.logError(sb.toString());
            log.error(sb.toString());
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        // Now in this Step we Add the PaleoContext to the Collecting Events
        
    }


    /**
     * @param treeDef
     * @throws SQLException
     */
    public void convertLithoStratCustom(final LithoStratTreeDef treeDef, 
                                  final LithoStrat        earth,
                                  final TableWriter       tblWriter,
                                  final String            srcTableName,
                                  final boolean           doMapGTPIds) throws SQLException
    {
        Statement stmt = null;
        ResultSet rs   = null;
        String s = "";
        try
        {
            // get a Hibernate session for saving the new records
            Session localSession = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
    
            int count = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) FROM "+srcTableName);
            if (count < 1) return;
            
            if (hasFrame)
            {
                setProcess(0, count);
            }
            
            // create an ID mapper for the geography table (mainly for use in converting localities)
            IdHashMapper lithoStratIdMapper = IdMapperMgr.getInstance().addHashMapper("stratigraphy_StratigraphyID", true);
            if (lithoStratIdMapper == null)
            {
                UIRegistry.showError("The lithoStratIdMapper was null.");
                return;
            }
            
            IdTableMapper gtpIdMapper = IdMapperMgr.getInstance().addTableMapper("geologictimeperiod", "GeologicTimePeriodID", null, false);
            if (doMapGTPIds)
            {
                gtpIdMapper.clearRecords();
                gtpIdMapper.mapAllIds();
            }
            
            Hashtable<Integer, Integer> stratGTPIdHash     = new Hashtable<Integer, Integer>();
            //Hashtable<Integer, Integer> newCEIdToNewStratIdHash = new Hashtable<Integer, Integer>();
            
            // stratigraphy2 goes here.
            IdHashMapper newCEIdToNewStratIdHash = IdMapperMgr.getInstance().addHashMapper("stratigraphy_StratigraphyID_2", true);
            newCEIdToNewStratIdHash.setShowLogErrors(false);
            
            IdMapperIFace ceMapper = IdMapperMgr.getInstance().get("collectingevent", "CollectingEventID");
            if (ceMapper == null)
            {
                ceMapper = IdMapperMgr.getInstance().addTableMapper("collectingevent", "CollectingEventID", null, false);
            }
            // get all of the old records
//            String sql  = String.format("SELECT s.StratigraphyID, s.SuperGroup, s.Group, s.Formation, s.Member, s.Bed, Remarks, " +
//            		                    "Text1, Text2, Number1, Number2, YesNo1, YesNo2, GeologicTimePeriodID FROM %s s " +
//            	                        "WHERE s.SuperGroup IS NOT NULL OR s.Group IS NOT NULL OR s.Formation IS NOT NULL OR " +
//            	                        "s.Member IS NOT NULL OR s.Bed IS NOT NULL ORDER BY s.StratigraphyID", srcTableName);
            String sql  = String.format("SELECT s.StratigraphyID, s.SuperGroup, s.Group, s.Formation, s.Member, s.Bed, Remarks, " +
                                        "Text1, Text2, Number1, Number2, YesNo1, YesNo2, GeologicTimePeriodID FROM %s s " +
                                        "ORDER BY s.StratigraphyID", srcTableName);
            
            stmt = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            rs   = stmt.executeQuery(sql);
            
            int stratsWithNoGTP       = 0;
            int stratsWithNoMappedGTP = 0;
            int missingCEMapping      = 0;
            
            int lithoCnt = 0;
    
            int counter = 0;
            // for each old record, convert the record
            while (rs.next())
            {
                if (counter % 500 == 0)
                {
                    if (hasFrame)
                    {
                        setProcess(counter);
    
                    } else
                    {
                        log.info("Converted " + counter + " Stratigraphy records");
                    }
                }
    
                // grab the important data fields from the old record
                int oldStratId    = rs.getInt(1);            // This is a one-to-one with CollectingEvent
                String superGroup = rs.getString(2);
                String lithoGroup = rs.getString(3);
                String formation  = rs.getString(4);
                String member     = rs.getString(5);
                String bed        = rs.getString(6);
                String remarks    = escapeStringLiterals(rs.getString(7));
                String text1      = escapeStringLiterals(rs.getString(8));
                String text2      = escapeStringLiterals(rs.getString(9));
                BigDecimal number1    = rs.getObject(10) != null ? rs.getBigDecimal(10)  : null;
                BigDecimal number2    = rs.getObject(11) != null ? rs.getBigDecimal(11)  : null;
                Boolean yesNo1    = rs.getObject(12) != null ? rs.getBoolean(12) : null;
                Boolean yesNo2    = rs.getObject(13) != null ? rs.getBoolean(13) : null;
                Integer oldGTPId  = rs.getObject(14) != null ? rs.getInt(14)     : null;
                
                // Check to see if there is any Litho information OR an GTP Id
                // If both are missing then skip the record.
                boolean hasLithoFields = isNotEmpty(superGroup) || isNotEmpty(lithoGroup) || isNotEmpty(formation) || isNotEmpty(member);
                if (!hasLithoFields && oldGTPId == null)
                {
                    continue;
                }
                
                Integer gtpId = null;
                if (doMapGTPIds)
                {
                    if (oldGTPId != null)
                    {
                        gtpId = oldGTPId;
                    }
                } else 
                {
                    gtpId = oldStratId;
                }
                
                if (gtpId != null)
                {
                    gtpId = gtpIdMapper.get(gtpId);
                    if (gtpId == null)
                    {
                        tblWriter.logError("Old GTPID["+gtpId+"] in the Strat record could not be mapped for Old StratID["+oldStratId+"]");
                        stratsWithNoMappedGTP++;
                    }
                } else
                {
                    stratsWithNoGTP++;
                }
    
                // There may not be any Litho information to add to the LithoStrat tree, 
                // but it did have GTP Information if we got here
                if (hasLithoFields)
                {
                    // create a new Geography object from the old data
                    LithoStrat[] newStrats = convertOldStratRecord(superGroup, lithoGroup, formation, member, bed, remarks, 
                                                                   text1, text2, number1, number2, yesNo1, yesNo2,
                                                                   earth, localSession);
                    
                    LithoStrat newStrat = getLastLithoStrat(newStrats);
                    counter++;
                    lithoCnt += newStrats.length;
        
                    // Map Old LithoStrat ID to the new Tree Id
                    //System.out.println(oldStratId + " " + newStrat.getLithoStratId());
                    if (newStrat != null)
                    {
                        lithoStratIdMapper.put(oldStratId, newStrat.getLithoStratId());
                        
                        // Convert Old CEId (StratID) to new CEId, then map the new CEId -> new StratId
                        Integer newCEId = ceMapper.get(oldStratId);
                        if (newCEId != null)
                        {
                            newCEIdToNewStratIdHash.put(newCEId, newStrat.getLithoStratId());
                        } else
                        {
                            String msg = String.format("No CE mapping for Old StratId %d, when they are a one-to-one.", oldStratId);
                            tblWriter.logError(msg);
                            log.error(msg);
                            missingCEMapping++;
                        }
                        
                        // Map the New StratId to the new GTP Id
                        if (gtpId != null && stratGTPIdHash.get(newStrat.getLithoStratId()) == null)
                        {
                            stratGTPIdHash.put(newStrat.getLithoStratId(), gtpId);  // new ID to new ID
                        }
                    } else
                    {
                        String msg = String.format("Strat Fields were all null for oldID", oldStratId);
                        tblWriter.logError(msg);
                        log.error(msg);
                        missingCEMapping++;
                    }
                }
            }
            stmt.close();
            
            System.out.println("lithoCnt: "+lithoCnt);
    
            if (hasFrame)
            {
                setProcess(counter);
    
            } else
            {
                log.info("Converted " + counter + " Stratigraphy records");
            }
    
            TreeHelper.fixFullnameForNodeAndDescendants(earth);
            earth.setNodeNumber(1);
            fixNodeNumbersFromRoot(earth);
    
            HibernateUtil.commitTransaction();
            log.info("Converted " + counter + " Stratigraphy records");
            
            rs.close();
            
            Statement updateStatement = newDBConn.createStatement();
            
            //Hashtable<Integer, Integer> ceToPCHash = new Hashtable<Integer, Integer>();
            
            int ceCnt    = BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT Count(CollectingEventID) FROM collectingevent");
            int stratCnt = BasicSQLUtils.getCountAsInt(oldDBConn, String.format("SELECT Count(CollectingEventID) FROM collectingevent " +
            		                                                            "INNER JOIN %s ON CollectingEventID = StratigraphyID", srcTableName));
            
            String msg = String.format("There are %d CE->Strat and %d CEs. The diff is %d", stratCnt, ceCnt, (ceCnt - stratCnt));
            tblWriter.log(msg);
            log.debug(msg);
            
            // Create a PaleoContext for each ColObj
            stmt = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);

            int processCnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM collectionobject WHERE CollectingEventID IS NOT NULL");
            if (frame != null) 
            {
                frame.setDesc("Converting PaleoContext...");
                frame.setProcess(0, processCnt);
            }
            
            TreeSet<Integer> missingStratIds = new TreeSet<Integer>();

            int missingStrat = 0;
            int missingGTP   = 0;
            int coUpdateCnt  = 0;
            int cnt          = 0;
            sql  = "SELECT CollectionObjectID, CollectingEventID FROM collectionobject WHERE CollectingEventID IS NOT NULL ORDER BY CollectionObjectID";
            rs   = stmt.executeQuery(sql);
            while (rs.next())
            {
                int     coId = rs.getInt(1); // New CO Id
                Integer ceId = rs.getInt(2); // New CE Id
                
                // Use the new CE ID to get the new Strat Id
                Integer newLithoId = newCEIdToNewStratIdHash.get(ceId);
                Integer gtpId      = null;
                
                if (newLithoId == null)
                {
                    missingStrat++;
                    missingStratIds.add(ceId);
                    
                    Integer oldStratID = ceMapper.reverseGet(ceId);
                    if (oldStratID != null)
                    {
                        sql = "SELECT GeologicTimePeriodID FROM stratigraphy WHERE StratigraphyID = " + oldStratID;
                        Integer oldGTPId = BasicSQLUtils.getCount(oldDBConn, sql);
                        if (oldGTPId != null)
                        {
                            gtpId = gtpIdMapper.get(oldGTPId);
                        }
                    }
                    if (gtpId == null) continue;
                }
                
                // Use the new StratID to get the new GTP Id (ChronosStratigraphy)
                if (gtpId == null)
                {
                    gtpId = stratGTPIdHash.get(newLithoId);
                    if (gtpId == null)
                    {
                        missingGTP++;
                        if (newLithoId == null) continue;
                    }
                }
                
                try
                {
                    String updateStr = "INSERT INTO paleocontext (TimestampCreated, TimestampModified, DisciplineID, Version, CreatedByAgentID, ModifiedByAgentID, LithoStratID, ChronosStratID) "
                            + "VALUES ('"
                            + nowStr
                            + "','"
                            + nowStr
                            + "',"
                            + getDisciplineId()
                            + ", 0, " 
                            + getCreatorAgentId(null) + "," + getModifiedByAgentId(null) 
                            +"," + (newLithoId != null ? newLithoId : "NULL")
                            +"," + (gtpId != null ? gtpId : "NULL")
                            + ")";
                    updateStatement.executeUpdate(updateStr, Statement.RETURN_GENERATED_KEYS);
                    
                    Integer paleoContextID = getInsertedId(updateStatement);
                    if (paleoContextID == null)
                    {
                        throw new RuntimeException("Couldn't get the Agent's inserted ID");
                    }
                    
                    String sqlUpdate = "UPDATE collectionobject SET PaleoContextID=" + paleoContextID + " WHERE CollectionObjectID = " + coId;
                    updateStatement.executeUpdate(sqlUpdate);
                    coUpdateCnt++;
                    
                } catch (SQLException e)
                {
                    e.printStackTrace();
                    log.error(e);
                    showError(e.getMessage());
                    throw new RuntimeException(e);
                }
                processCnt++;
                if (frame != null && cnt % 100 == 0) frame.setProcess(cnt);
                    
            }
            rs.close();
            stmt.close();
            
            if (frame != null) frame.setProcess(processCnt);
            
            msg = String.format("There are %d unmappable Strat Records and %d unmappable GTP records.", missingStrat, missingGTP);
            tblWriter.log(msg);
            log.debug(msg);
            
            msg = String.format("There are %d CO records updated.", coUpdateCnt);
            tblWriter.log(msg);
            log.debug(msg);
            updateStatement.close();
            
            msg = String.format("No CE mapping for Old StratId Count: %d", missingCEMapping);
            tblWriter.logError(msg);
            log.error(msg);
            
            msg = String.format("Strats with No GTP Count: %d", stratsWithNoGTP);
            tblWriter.logError(msg);
            log.error(msg);
            
            msg = String.format("Strats with missing Mapping to GTP Count: %d", stratsWithNoMappedGTP);
            tblWriter.logError(msg);
            log.error(msg);
            
            msg = String.format("Number of Old StratIds mapped to a new Strat ID Count: %d", lithoStratIdMapper.size());
            tblWriter.logError(msg);
            log.error(msg);
            
            StringBuilder sb = new StringBuilder();
            sb.append("Missing New Strat: ");
            if (missingStratIds.size() == 0) sb.append("None");

            for (Integer id : missingStratIds)
            {
               sb.append(String.format("%d, ", id));
            }
            tblWriter.logError(sb.toString());
            log.error(sb.toString());
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        // Now in this Step we Add the PaleoContext to the Collecting Events
        
    }


    private LithoStrat getLastLithoStrat(final LithoStrat[] lithoStrats)
    {
        if (lithoStrats != null && lithoStrats.length > 0)
        {
            int inx = lithoStrats.length - 1;
            while (lithoStrats[inx] == null)
            {
                inx++;
            }
            return lithoStrats[inx];
        }
        return null;
    }
    
    /**
     * @param treeDef
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public LithoStrat convertLithoStratFromCSV(LithoStratTreeDef treeDef, final boolean doSave)
    {
        lithoStratHash.clear();

        File file = new File("Stratigraphy.csv");
        if (!file.exists())
        {
            log.error("Couldn't file[" + file.getAbsolutePath() + "]");
            return null;
        }

        // empty out any pre-existing records
        deleteAllRecordsFromTable(newDBConn, "lithostrat", BasicSQLUtils.myDestinationServerType);

        // get a Hibernate session for saving the new records
        Session localSession = doSave ? HibernateUtil.getCurrentSession() : null;
        if (localSession != null)
        {
            HibernateUtil.beginTransaction();
        }

        List<String> lines = null;
        try
        {
            lines = FileUtils.readLines(file);

        } catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }

        // setup the root Geography record (planet Earth)
        LithoStrat earth = new LithoStrat();
        earth.initialize();
        earth.setName("Earth");
        earth.setRankId(0);
        earth.setDefinition(treeDef);
        for (Object o : treeDef.getTreeDefItems())
        {
            LithoStratTreeDefItem defItem = (LithoStratTreeDefItem)o;
            if (defItem.getRankId() == 0)
            {
                earth.setDefinitionItem(defItem);
                break;
            }
        }
        LithoStratTreeDefItem defItem = treeDef.getDefItemByRank(0);
        earth.setDefinitionItem(defItem);
        if (doSave)
        {
            localSession.save(earth);
        }

        // create an ID mapper for the geography table (mainly for use in converting localities)
        IdTableMapper lithoStratIdMapper = doSave ? IdMapperMgr.getInstance().addTableMapper("lithostrat", "LithoStratID") : null;

        int counter = 0;
        // for each old record, convert the record
        for (String line : lines)
        {
            if (counter == 0)
            {
                counter = 1;
                continue; // skip header line
            }

            if (counter % 500 == 0)
            {
                if (hasFrame)
                {
                    setProcess(counter);

                } else
                {
                    log.info("Converted " + counter + " Stratigraphy records");
                }
            }

            String[] columns = splitPreserveAllTokens(line, ',');
            if (columns.length < 7)
            {
                log.error("Skipping[" + line + "]");
                continue;
            }

            // grab the important data fields from the old record
            int oldId = Integer.parseInt(columns[0]);
            String superGroup = columns[2];
            String lithoGroup = columns[3];
            String formation  = columns[4];
            String member     = columns[5];
            String bed        = columns[6];

            // create a new Geography object from the old data 
            LithoStrat[] newStrats = convertOldStratRecord(superGroup, lithoGroup, formation, member, bed, null, null, null, null, null, null, null, earth, localSession);
            
            
            counter++;

            // add this new ID to the ID mapper
            if (shouldCreateMapTables && lithoStratIdMapper != null)
            {
                lithoStratIdMapper.put(oldId, getLastLithoStrat(newStrats).getLithoStratId());
            }
        }

        if (hasFrame)
        {
            setProcess(counter);

        } else
        {
            log.info("Converted " + counter + " Stratigraphy records");
        }

        TreeHelper.fixFullnameForNodeAndDescendants(earth);
        earth.setNodeNumber(1);
        fixNodeNumbersFromRoot(earth);

        if (doSave)
        {
            HibernateUtil.commitTransaction();
        }
        log.info("Converted " + counter + " Stratigraphy records");

        // set up Geography foreign key mapping for locality
        if (doSave)
        {
            idMapperMgr.mapForeignKey("Locality", "StratigraphyID", "LithoStrat", "LithoStratID");
        }

        lithoStratHash.clear();

        return earth;
    }

    /**
     * @param nameArg
     * @param remarks
     * @param text1
     * @param text2
     * @param number1
     * @param number2
     * @param yesNo1
     * @param yesNo2
     * @param parentArg
     * @param sessionArg
     * @return
     */
    protected LithoStrat buildLithoStratLevel(final String     nameArg,
                                              final String     remarks,
                                              final String     text1, 
                                              final String     text2, 
                                              final BigDecimal     number1, 
                                              final BigDecimal     number2, 
                                              final Boolean    yesNo1, 
                                              final Boolean    yesNo2,
                                              final LithoStrat parentArg,
                                              final Session    sessionArg)
    {
        String name = nameArg;
        if (name == null)
        {
            name = "N/A";
        }

        // search through all of parent's children to see if one already exists with the same name
        Set<LithoStrat> children = parentArg.getChildren();
        for (LithoStrat child : children)
        {
            if (name.equalsIgnoreCase(child.getName()))
            {
                // this parent already has a child by the given name
                // don't create a new one, just return this one
                return child;
            }
        }

        // we didn't find a child by the given name
        // we need to create a new Geography record
        LithoStrat newStrat = new LithoStrat();
        newStrat.initialize();
        newStrat.setName(name);
        newStrat.setRemarks(remarks);
        
        newStrat.setText1(text1);
        newStrat.setText2(text2);
        newStrat.setNumber1(number1);
        newStrat.setNumber2(number2);
        newStrat.setYesNo1(yesNo1);
        newStrat.setYesNo2(yesNo2);
        
        newStrat.setParent(parentArg);
        parentArg.addChild(newStrat);
        newStrat.setDefinition(parentArg.getDefinition());
        
        int                   newGeoRank = parentArg.getRankId() + 100;
        LithoStratTreeDefItem defItem    = parentArg.getDefinition().getDefItemByRank(newGeoRank);
        newStrat.setDefinitionItem(defItem);
        
        newStrat.setRankId(newGeoRank);

        if (sessionArg != null)
        {
            sessionArg.save(newStrat);
        }

        return newStrat;
    }

    /**
     * @param superGroup
     * @param lithoGroup
     * @param formation
     * @param member
     * @param bed
     * @param stratRoot
     * @param localSession
     * @return
     */
    protected LithoStrat[] convertOldStratRecord(final String     superGroup,
                                               final String     lithoGroup,
                                               final String     formation,
                                               final String     member,
                                               final String     bed,
                                               final String     remarks,
                                               final String     text1, 
                                               final String     text2, 
                                               final BigDecimal     number1, 
                                               final BigDecimal     number2, 
                                               final Boolean    yesNo1, 
                                               final Boolean    yesNo2,
                                               final LithoStrat stratRoot,
                                               final Session    localSession)
    {
        String levelNames[] = { superGroup, lithoGroup, formation, member, bed };
        int levelsToBuild = 0;
        for (int i = levelNames.length; i > 0; --i)
        {
            if (isNotEmpty(levelNames[i - 1]))
            {
                levelsToBuild = i;
                break;
            }
        }

        for (int i = 0; i < levelsToBuild; i++)
        {
            if (isEmpty(levelNames[i]))
            {
                levelNames[i] = "(Empty)";
            }
        }

        LithoStrat[] newStrat = new LithoStrat[levelsToBuild];
        LithoStrat prevLevelGeo = stratRoot;
        for (int i = 0; i < levelsToBuild; ++i)
        {
            LithoStrat newLevelStrat = buildLithoStratLevel(levelNames[i], remarks, text1, text2, number1, number2, yesNo1, yesNo2, prevLevelGeo, localSession);
            newStrat[i]  = newLevelStrat;
            prevLevelGeo = newLevelStrat;
        }

        return newStrat;
    }

    /**
     * @return
     */
    public StorageTreeDef buildSampleStorageTreeDef()
    {
        // empty out any pre-existing tree definitions
        deleteAllRecordsFromTable(newDBConn, "storagetreedef", BasicSQLUtils.myDestinationServerType);
        deleteAllRecordsFromTable(newDBConn, "storagetreedefitem", BasicSQLUtils.myDestinationServerType);

        log.info("Creating a sample storage tree definition");

        Session localSession = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        StorageTreeDef locDef = TreeFactory.createStdStorageTreeDef("Sample storage tree", null);
        locDef.setRemarks("This definition is merely for demonstration purposes.  Consult documentation or support staff for instructions on creating one tailored for an institutions specific needs.");
        locDef.setFullNameDirection(TreeDefIface.FORWARD);
        localSession.save(locDef);

        // get the root def item
        StorageTreeDefItem rootItem = locDef.getTreeDefItems().iterator().next();
        rootItem.setFullNameSeparator(", ");
        localSession.save(rootItem);

        Storage rootNode = rootItem.getTreeEntries().iterator().next();
        localSession.save(rootNode);

        StorageTreeDefItem building = new StorageTreeDefItem();
        building.initialize();
        building.setRankId(100);
        building.setName("Building");
        building.setIsEnforced(false);
        building.setIsInFullName(false);
        building.setTreeDef(locDef);
        building.setFullNameSeparator(", ");
        localSession.save(building);

        StorageTreeDefItem room = new StorageTreeDefItem();
        room.initialize();
        room.setRankId(200);
        room.setName("Room");
        room.setIsEnforced(true);
        room.setIsInFullName(true);
        room.setTreeDef(locDef);
        room.setFullNameSeparator(", ");
        localSession.save(room);

        StorageTreeDefItem freezer = new StorageTreeDefItem();
        freezer.initialize();
        freezer.setRankId(300);
        freezer.setName("Freezer");
        freezer.setIsEnforced(true);
        freezer.setIsInFullName(true);
        freezer.setTreeDef(locDef);
        freezer.setFullNameSeparator(", ");
        localSession.save(freezer);

        rootItem.setChild(building);
        building.setParent(rootItem);
        building.setChild(room);
        room.setParent(building);
        room.setChild(freezer);
        freezer.setParent(room);

        locDef.addTreeDefItem(building);
        locDef.addTreeDefItem(room);
        locDef.addTreeDefItem(freezer);

        HibernateUtil.commitTransaction();

        return locDef;
    }

    /**
     * Walks the old GTP records and creates a GTP tree def and items based on the ranks and rank
     * names found in the old records
     * 
     * @return the new tree def
     * @throws SQLException on any error while contacting the old database
     */
    public GeologicTimePeriodTreeDef convertGTPDefAndItems(final boolean isPaleo) throws SQLException
    {
        deleteAllRecordsFromTable("geologictimeperiodtreedef", BasicSQLUtils.myDestinationServerType);
        deleteAllRecordsFromTable("geologictimeperiodtreedefitem", BasicSQLUtils.myDestinationServerType);
        log.info("Inferring geologic time period definition from old records");
        int count = 0;

        // get all of the old records
        String sql = "SELECT RankCode, RankName from geologictimeperiod";
        Statement statement = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(Integer.MIN_VALUE);

        ResultSet oldGtpRecords = statement.executeQuery(sql);

        Session localSession = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        GeologicTimePeriodTreeDef def = new GeologicTimePeriodTreeDef();
        def.initialize();
        def.setName("Inferred Geologic Time Period Definition");
        def.setRemarks("");
        def.setFullNameDirection(TreeDefIface.REVERSE);
        localSession.save(def);

        Vector<GeologicTimePeriodTreeDefItem> newItems = new Vector<GeologicTimePeriodTreeDefItem>();

        GeologicTimePeriodTreeDefItem rootItem = addGtpDefItem(0, "Time Root", def);
        rootItem.setIsEnforced(true);
        rootItem.setIsInFullName(false);
        rootItem.setFullNameSeparator(", ");
        localSession.save(rootItem);
        newItems.add(rootItem);
        ++count;

        if (isPaleo)
        {        while (oldGtpRecords.next())
            {
                // we're modifying the rank since the originals were 1,2,3,...
                // to make them 100, 200, 300, ... (more like the other trees)
                Integer rankCode = oldGtpRecords.getInt(1) * 100;
                String rankName = oldGtpRecords.getString(2);
                GeologicTimePeriodTreeDefItem newItem = addGtpDefItem(rankCode, rankName, def);
                if (newItem != null)
                {
                    newItem.setFullNameSeparator(", ");
                    localSession.save(newItem);
                    newItems.add(newItem);
                }
                if (++count % 1000 == 0)
                {
                    log.info(count + " geologic time period records processed");
                }
            }
        }

        // sort the vector to put them in parent/child order
        Comparator<GeologicTimePeriodTreeDefItem> itemComparator = new Comparator<GeologicTimePeriodTreeDefItem>()
        {
            public int compare(GeologicTimePeriodTreeDefItem o1, GeologicTimePeriodTreeDefItem o2)
            {
                return o1.getRankId().compareTo(o2.getRankId());
            }
        };
        Collections.sort(newItems, itemComparator);

        // set the parent/child pointers
        for (int i = 0; i < newItems.size() - 1; ++i)
        {
            GeologicTimePeriodTreeDefItem parent = newItems.get(i);
            GeologicTimePeriodTreeDefItem child = newItems.get(i + 1);
            parent.setChild(child);
            child.setParent(parent);
        }

        HibernateUtil.commitTransaction();

        log.info("Finished inferring GTP tree definition and items");
        return def;
    }

    /**
     * Creates a new GTP def item if one with the same rank doesn't exist.
     * 
     * @param rankCode the rank of the new item
     * @param rankName the name of the new item
     * @param def the def to which the item is attached
     * @return the new item, or null if one already exists with this rank
     */
    protected GeologicTimePeriodTreeDefItem addGtpDefItem(Integer rankCode,
                                                          String rankName,
                                                          GeologicTimePeriodTreeDef def)
    {
        // check to see if this item already exists
        for (Object o : def.getTreeDefItems())
        {
            GeologicTimePeriodTreeDefItem item = (GeologicTimePeriodTreeDefItem)o;
            if (item.getRankId().equals(rankCode)) { return null; }
        }

        // create a new item
        GeologicTimePeriodTreeDefItem item = new GeologicTimePeriodTreeDefItem();
        item.initialize();
        item.setRankId(rankCode);
        item.setName(rankName);
        def.addTreeDefItem(item);
        return item;
    }

    /**
     * @param tblWriter
     * @param treeDef
     * @param isPaleo
     * @throws SQLException
     */
    public void convertGTP(final TableWriter tblWriter, final GeologicTimePeriodTreeDef treeDef, final boolean isPaleo) throws SQLException
    {
        deleteAllRecordsFromTable("geologictimeperiod", BasicSQLUtils.myDestinationServerType);

        log.info("Converting old geologic time period records");
        int count = 0;

        // create an ID mapper for the geologictimeperiod table
        IdTableMapper gtpIdMapper = IdMapperMgr.getInstance().addTableMapper("geologictimeperiod", "GeologicTimePeriodID");
        Hashtable<Integer, GeologicTimePeriod> oldIdToGTPMap = new Hashtable<Integer, GeologicTimePeriod>();

//        String    sql = "SELECT g.GeologicTimePeriodID,g.RankCode,g.Name,g.Standard,g.Remarks,g.TimestampModified,g.TimestampCreated,p1.Age as Upper," +
//        		       "p1.AgeUncertainty as UpperUncertainty,p2.Age as Lower,p2.AgeUncertainty as LowerUncertainty FROM geologictimeperiod g, " +
//        		       "geologictimeboundary p1, geologictimeboundary p2 WHERE g.UpperBoundaryID=p1.GeologicTimeBoundaryID AND " +
//        		       "g.LowerBoundaryID=p2.GeologicTimeBoundaryID ORDER BY Lower DESC, RankCode";
        String sql = "SELECT g.GeologicTimePeriodID,g.RankCode,g.Name,g.Standard,g.Remarks,g.TimestampModified,g.TimestampCreated,gb1.Age as Upper," +
                     "gb1.AgeUncertainty as UpperUncertainty,gb2.Age as Lower,gb2.AgeUncertainty as LowerUncertainty FROM geologictimeperiod g " +
                     "LEFT OUTER JOIN geologictimeboundary gb1 ON g.UpperBoundaryID = gb1.GeologicTimeBoundaryID " +
                     "LEFT OUTER JOIN geologictimeboundary gb2 ON g.LowerBoundaryID = gb2.GeologicTimeBoundaryID " +
                     "ORDER BY Lower DESC, RankCode";
        Statement statement = oldDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(Integer.MIN_VALUE);
        ResultSet rs        = statement.executeQuery(sql);

        Session localSession = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction();

        Vector<GeologicTimePeriod> newItems = new Vector<GeologicTimePeriod>();

        GeologicTimePeriod allTime = new GeologicTimePeriod();
        allTime.initialize();
        allTime.setDefinition(treeDef);
        GeologicTimePeriodTreeDefItem rootDefItem = treeDef.getDefItemByRank(0);
        allTime.setDefinitionItem(rootDefItem);
        allTime.setRankId(0);
        allTime.setName("Time");
        allTime.setFullName("Time");
        allTime.setStartPeriod(new BigDecimal("100000"));
        allTime.setEndPeriod(BigDecimal.ZERO);
        allTime.setEndUncertainty(BigDecimal.ZERO);
        allTime.setTimestampCreated(now);
        ++count;
        newItems.add(allTime);
        
        ArrayList<GeologicTimePeriod> needsPlaceHolderList = new ArrayList<GeologicTimePeriod>();
        
        boolean needsTbl = true;

        if (isPaleo)
        {
            while (rs.next())
            {
                Integer   id       = rs.getInt(1);
                Integer   rank     = rs.getInt(2) * 100;
                String    name     = rs.getString(3);
                String    std      = rs.getString(4);
                String    rem      = rs.getString(5);
                Date      modTDate = rs.getDate(6);
                Date      creTDate = rs.getDate(7);
                Timestamp modT     = (modTDate != null) ? new Timestamp(modTDate.getTime()) : null;
                Timestamp creT     = (creTDate != null) ? new Timestamp(creTDate.getTime()) : null;
                BigDecimal     upper    = new BigDecimal((Double)rs.getObject(8));
                BigDecimal     uError   = new BigDecimal((Double)rs.getObject(9));
                BigDecimal     lower    = new BigDecimal((Double)rs.getObject(10));
                BigDecimal     lError   = new BigDecimal((Double)rs.getObject(11));
                
                if (isEmpty(name))
                {
                    if (needsTbl)
                    {
                        tblWriter.startTable();
                        tblWriter.logHdr("ID", "Rank Name", "Name", "Reason");
                        needsTbl = false;
                    }
                    tblWriter.log(id.toString(), rank.toString(), name, "Name is null, Name set to 'XXXX'");
                    log.error("The Name is empty (or null) for GTP ID["+id+"]  Rank["+rank+"]");
                    name = "XXXX";
                }
    
                if (modT == null && creT == null)
                {
                    creT = now;
                    modT = now;
    
                } else if (modT == null && creT != null)
                {
                    modT = new Timestamp(creT.getTime());
                    
                } else if (modT != null && creT == null)
                {
                    creT = new Timestamp(modT.getTime());
                }
                // else (neither are null, so do nothing)

                GeologicTimePeriodTreeDefItem defItem = rank != null ? treeDef.getDefItemByRank(rank) : null;
                
                GeologicTimePeriod gtp = new GeologicTimePeriod();
                gtp.initialize();
                gtp.setName(name);
                gtp.setFullName(name);
                gtp.setDefinitionItem(defItem);
                gtp.setRankId(rank);
                gtp.setDefinition(treeDef);
                gtp.setStartPeriod(lower);
                gtp.setStartUncertainty(lError);
                gtp.setEndPeriod(upper);
                gtp.setEndUncertainty(uError);
                gtp.setStandard(std);
                gtp.setRemarks(rem);
                gtp.setTimestampCreated(creT);
                gtp.setTimestampModified(modT);
    
                if (lower == null || upper == null || rank == null)
                {
                    needsPlaceHolderList.add(gtp);
                    log.debug("PlaceHold Old ID: "+id);
                } else
                {
                    newItems.add(gtp);
                }
    
                oldIdToGTPMap.put(id, gtp);
    
                if (++count % 500 == 0)
                {
                    log.info(count + " geologic time period records converted");
                }
            }
    
            // now we need to fix the parent/pointers
            for (int i = 0; i < newItems.size(); ++i)
            {
                GeologicTimePeriod gtp = newItems.get(i);
                for (int j = 0; j < newItems.size(); ++j)
                {
                    GeologicTimePeriod child = newItems.get(j);
                    if (isParentChildPair(gtp, child))
                    {
                        gtp.addChild(child);
                    }
                }
            }
            
            if (needsPlaceHolderList.size() > 0)
            {
                int rank = 100;
                for (GeologicTimePeriodTreeDefItem di : treeDef.getTreeDefItems())
                {
                    System.out.println(di.getName()+" -> "+di.getRankId());
                }
                GeologicTimePeriodTreeDefItem defItem = treeDef.getDefItemByRank(rank);
                        
                GeologicTimePeriod gtp = new GeologicTimePeriod();
                gtp.initialize();
                gtp.setName("Placeholder");
                gtp.setFullName("Placeholder");
                gtp.setDefinitionItem(defItem);
                gtp.setRankId(rank);
                gtp.setDefinition(treeDef);
                gtp.setStartPeriod(BigDecimal.ZERO);
                gtp.setStartUncertainty(BigDecimal.ZERO);
                gtp.setEndPeriod(BigDecimal.ZERO);
                gtp.setEndUncertainty(BigDecimal.ZERO);
                gtp.setStandard(null);
                gtp.setRemarks(null);
                gtp.setTimestampCreated(now);
                gtp.setTimestampModified(now);
                allTime.addChild(gtp);

                rank = 200;
                defItem = treeDef.getDefItemByRank(rank);

                for (GeologicTimePeriod gtpPH : needsPlaceHolderList)
                {
                    gtpPH.setDefinition(treeDef);
                    gtpPH.setDefinitionItem(defItem);
                    gtpPH.setRankId(rank);
                    gtpPH.setStartPeriod(BigDecimal.ZERO);
                    gtpPH.setStartUncertainty(BigDecimal.ZERO);
                    gtpPH.setEndPeriod(BigDecimal.ZERO);
                    gtpPH.setEndUncertainty(BigDecimal.ZERO);
                    gtp.addChild(gtpPH);
                }
            }
    
            TreeHelper.fixFullnameForNodeAndDescendants(allTime);
        }
        
        // fix node number, child node number stuff
        allTime.setNodeNumber(1);
        fixNodeNumbersFromRoot(allTime);
        localSession.save(allTime);

        HibernateUtil.commitTransaction();

        if (shouldCreateMapTables)
        {
            // add all of the ID mappings
            for (Integer oldId : oldIdToGTPMap.keySet())
            {
                if (oldId != null)
                {
                    GeologicTimePeriod gtp = oldIdToGTPMap.get(oldId);
                    if (gtp != null)
                    {
                        if (gtp.getId() != null)
                        {
                            gtpIdMapper.put(oldId, gtp.getId());
                        } else
                        {
                            log.debug("GTP id is null: "+gtp.getName());
                        }
                    } else
                    {
                        log.debug("GTP missing in hash for Old ID: "+oldId);
                    }
                } else
                {
                    log.debug("Old ID in Hash is null: "+oldId);
                }
            }
        }

        // set up geologictimeperiod foreign key mapping for stratigraphy
        IdMapperMgr.getInstance().mapForeignKey("Stratigraphy", "GeologicTimePeriodID",
                                                "GeologicTimePeriod", "GeologicTimePeriodID");

        log.info(count + " geologic time period records converted");
        
        if (!needsTbl)
        {
            tblWriter.endTable();
        }
    }
    
    /**
     * @param tblWriter
     */
    public void convertStrat(final TableWriter tblWriter, final boolean isPaleo) throws SQLException
    {
        Transaction trans = null;
        Session lclSession = null;
        try
        {
            // empty out any pre-existing records
            deleteAllRecordsFromTable(newDBConn, "lithostrat", BasicSQLUtils.myDestinationServerType);
            
            lclSession = HibernateUtil.getNewSession();
            
            
            List<?>  disciplineeList = lclSession.createQuery("FROM Discipline").list();
            
            for (Object obj : disciplineeList)
            {
                trans = lclSession.beginTransaction();
                
                Discipline discipline = (Discipline)obj;
                LithoStratTreeDef lithoStratTreeDef = createLithoStratTreeDef("LithoStrat");
                
                lithoStratTreeDef.getDisciplines().add(discipline);
                discipline.setLithoStratTreeDef(lithoStratTreeDef);
                
                lclSession.saveOrUpdate(lithoStratTreeDef);
                lclSession.saveOrUpdate(discipline);
                
                LithoStratTreeDefItem earth     = createLithoStratTreeDefItem(lithoStratTreeDef, "Surface", 0, false);
                LithoStratTreeDefItem superGrp  = createLithoStratTreeDefItem(earth,     "Super Group", 100, false);
                LithoStratTreeDefItem lithoGrp  = createLithoStratTreeDefItem(superGrp,  "Litho Group", 200, false);
                LithoStratTreeDefItem formation = createLithoStratTreeDefItem(lithoGrp,  "Formation",   300, false);
                LithoStratTreeDefItem member    = createLithoStratTreeDefItem(formation, "Member",      400, false);
                @SuppressWarnings("unused")
                LithoStratTreeDefItem bed       = createLithoStratTreeDefItem(member,    "Bed",         500, true);
                lclSession.saveOrUpdate(earth);
                
                // setup the root Geography record (planet Earth)
                LithoStrat earthNode = new LithoStrat();
                earthNode.initialize();
                earthNode.setName("Earth");
                earthNode.setFullName("Earth");
                earthNode.setNodeNumber(1);
                earthNode.setHighestChildNodeNumber(1);
                earthNode.setRankId(0);
                earthNode.setDefinition(lithoStratTreeDef);
                earthNode.setDefinitionItem(earth);
                earth.getTreeEntries().add(earthNode);
                lclSession.saveOrUpdate(earthNode);
                
                trans.commit();
                
                if (isPaleo)
                {
                    convertLithoStratGeneral(lithoStratTreeDef, earthNode, tblWriter, "stratigraphy");
                    //convertLithoStratCustom(lithoStratTreeDef, earthNode, tblWriter, "stratigraphy", true);
                }
            }
            
        } catch (Exception ex)
        {
            if (trans != null)
            {
                trans.rollback();
            }
            
            ex.printStackTrace();
        } finally
        {
            if (lclSession != null) lclSession.close();
        }
    }

    /**
     * Regenerates all nodeNumber and highestChildNodeNumber field values for all nodes attached to
     * the given root. The nodeNumber field of the given root must already be set.
     * 
     * @param root the top of the tree to be renumbered
     * @return the highest node number value present in the subtree rooted at <code>root</code>
     */
    public static <T extends Treeable<T, ?, ?>> int fixNodeNumbersFromRoot(T root)
    {
        int nextNodeNumber = root.getNodeNumber();
        for (T child : root.getChildren())
        {
            child.setNodeNumber(++nextNodeNumber);
            nextNodeNumber = fixNodeNumbersFromRoot(child);
        }
        root.setHighestChildNodeNumber(nextNodeNumber);
        return nextNodeNumber;
    }

    /**
     * @param parent
     * @param child
     * @return
     */
    protected boolean isParentChildPair(final GeologicTimePeriod parent, final GeologicTimePeriod child)
    {
        if (parent == child) { return false; }

        Double startParent = parent.getStartPeriod().doubleValue();
        Double endParent   = parent.getEndPeriod().doubleValue();

        Double startChild = child.getStartPeriod().doubleValue();
        Double endChild   = child.getEndPeriod().doubleValue();

        // remember, the numbers represent MYA (millions of yrs AGO)
        // so the logic seems a little backwards
        if (startParent >= startChild && endParent <= endChild
                && parent.getRankId() < child.getRankId()) { return true; }

        return false;
    }

    /**
     * Copies the filed names to the list and prepend the table name
     * 
     * @param list the destination list
     * @param fieldNames the list of field names
     * @param tableName the table name
     */
    protected static void addNamesWithTableName(final List<String> list,
                                                final List<String> fieldNames,
                                                final String tableName)
            {
        for (String fldName : fieldNames)
        {
            list.add(tableName + "." + fldName);
        }
    }

    /**
     * @param rsmd
     * @param map
     * @param tableNames
     * @throws SQLException
     */
    protected void buildIndexMapFromMetaData(final ResultSetMetaData rsmd,
                                             final Hashtable<String, Integer> map,
                                             final String[] tableNames) throws SQLException
    {
        map.clear();

        // Find the missing table name by figuring our which one isn't used.
        Hashtable<String, Boolean> existsMap = new Hashtable<String, Boolean>();
        for (String tblName : tableNames)
        {
            existsMap.put(tblName, true);
            log.debug("[" + tblName + "]");
        }
        for (int i = 1; i <= rsmd.getColumnCount(); i++)
        {
            String tableName = rsmd.getTableName(i);
            // log.info("["+tableName+"]");
            if (isNotEmpty(tableName))
            {
                if (existsMap.get(tableName) != null)
                {
                    existsMap.remove(tableName);
                    log.info("Removing Table Name[" + tableName + "]");
                }
            }
        }

        String missingTableName = null;
        if (existsMap.size() == 1)
        {
            missingTableName = existsMap.keys().nextElement();
            log.info("Missing Table Name[" + missingTableName + "]");

        } else if (existsMap.size() > 1)
        {
            throw new RuntimeException("ExistsMap cannot have more than one name in it!");
        } else
        {
            log.info("No Missing Table Names.");
        }

        for (int i = 1; i <= rsmd.getColumnCount(); i++)
        {
            strBuf.setLength(0);
            String tableName = rsmd.getTableName(i);
            strBuf.append(isNotEmpty(tableName) ? tableName : missingTableName);
            strBuf.append(".");
            strBuf.append(rsmd.getColumnName(i));
            map.put(strBuf.toString(), i);
        }
    }

    /**
     * @param rsmd
     * @param map
     * @param tableNames
     * @throws SQLException
     */
    protected void buildIndexMapFromMetaData(final ResultSetMetaData rsmd,
                                             final List<String> origList,
                                             final Hashtable<String, Integer> map)
            throws SQLException
    {
        map.clear();

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= rsmd.getColumnCount(); i++)
        {
            sb.setLength(0);
            String tableName = rsmd.getTableName(i);
            String fieldName = rsmd.getColumnName(i);

            if (isNotEmpty(tableName))
            {
                sb.append(tableName);
            } else
            {
                for (String fullName : origList)
                {
                    String[] parts = split(fullName, ".");
                    if (parts[1].equals(fieldName))
                    {
                        sb.append(parts[0]);
                        break;
                    }
                }
            }
            sb.append(".");
            sb.append(fieldName);
            // log.info("["+strBuf.toString()+"] "+i);
            map.put(sb.toString(), i);
        }
    }
    
    /**
     * 
     */
    public void cleanUp()
    {
        try
        {
            PrintWriter pw = new PrintWriter("unmappedagents.txt");
            Vector<String> nameList = new Vector<String>(oldUnmappedAgentNames);
            Collections.sort(nameList);
            
            for (String name : nameList)
            {
                pw.println(name);
            }
            pw.close();
            
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    	File indexFile = convLogger.closeAll();
    	if (indexFile != null && indexFile.exists())
    	{
    	    try
    	    {
    	        AttachmentUtils.openURI(indexFile.toURI());
    	        
    	    } catch (Exception ex)
    	    {
    	        ex.printStackTrace();
    	    }
    	}
    }

    /**
     * 
     */
    public void createAndFillStatTable()
    {
        try
        {
            Statement stmtNew = newDBConn.createStatement();
            String str = "DROP TABLE `webstats`";
            try
            {
                stmtNew.executeUpdate(str);

            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }

            str = "CREATE TABLE `webstats` (`WebStatsID` int(11) NOT NULL default '0', "
                    + "`UniqueVisitors1` int(11), " + "`UniqueVisitors2` int(11), "
                    + "`UniqueVisitors3` int(11), " + "`UniqueVisitors4` int(11), "
                    + "`UniqueVisitors5` int(11), " + "`UniqueVisitors6` int(11), "
                    + "`UniqueVisitorsMon1` varchar(32), " + "`UniqueVisitorsMon2` varchar(32), "
                    + "`UniqueVisitorsMon3` varchar(32), " + "`UniqueVisitorsMon4` varchar(32), "
                    + "`UniqueVisitorsMon5` varchar(32), " + "`UniqueVisitorsMon6` varchar(32), "
                    + "`UniqueVisitorsYear` varchar(32), " + "`Taxon1` varchar(32), "
                    + "`TaxonCnt1` int(11), " + "`Taxon2` varchar(32), " + "`TaxonCnt2` int(11), "
                    + "`Taxon3` varchar(32), " + "`TaxonCnt3` int(11), " + "`Taxon4` varchar(32), "
                    + "`TaxonCnt4` int(11), " + "`Taxon5` varchar(32), " + "`TaxonCnt5` int(11), "
                    + " PRIMARY KEY (`WebStatsID`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1";
            // log.info(str);
            stmtNew.executeUpdate(str);

            str = "INSERT INTO webstats VALUES (0, 234, 189, 211, 302, 229, 276, "
                    + "'Nov', 'Dec', 'Jan', 'Feb', 'Mar', 'Apr', " + " 2621, "
                    + "'Etheostoma',  54," + "'notatus',  39," + "'lutrensis',  22,"
                    + "'anomalum',  12," + "'platostomus',  8" + ")";

            stmtNew.executeUpdate(str);

            stmtNew.clearBatch();
            stmtNew.close();

        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param isSharingCollectingEvents the isSharingCollectingEvents to set
     */
    public void setSharingCollectingEvents(final boolean isSharingCollectingEvents)
    {
        this.isSharingCollectingEvents = isSharingCollectingEvents;
    }

    // --------------------------------------------------------------------
    // -- TaxonTypeHolder
    // --------------------------------------------------------------------
    class TaxonTypeHolder 
    {
        private Object[] row;
        
        public TaxonTypeHolder(final Object[] row)
        {
            this.row = row;
        }
        
        public Integer getId()
        {
            return (Integer)row[0];
        }
        
        public String getName()
        {
            return row[1].toString();
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return row[1] + " - " + row[2];
        }
    }
    
    // --------------------------------------------------------------------
    // -- IdMapperIndexIncrementerIFace
    // --------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.conversion.IdMapperIndexIncrementerIFace#getNextIndex()
     */
    @Override
    public int getNextIndex()
    {
        return globalIdNumber++;
    }
    
    // --------------------------------------------------------------------
    // -- Static Methods
    // --------------------------------------------------------------------

    /**
     * @return wehether it should create the Map Tables, if false it assumes they have already been
     *         created
     */
    public static boolean shouldCreateMapTables()
    {
        return shouldCreateMapTables;
    }

    /**
     * @return whether the map tables should be removed
     */
    public static boolean shouldDeleteMapTables()
    {
        return shouldDeleteMapTables;
    }

    /**
     * Sets whether to create all the mapping tables
     * @param shouldCreateMapTables true to create, false to do nothing
     */
    public static void setShouldCreateMapTables(boolean shouldCreateMapTables)
    {
        GenericDBConversion.shouldCreateMapTables = shouldCreateMapTables;
    }

    /**
     * Sets whether the mapping tables should be deleted after the conversion process completes
     * @param shouldDeleteMapTables true to delete tables, false means do nothing
     */
    public static void setShouldDeleteMapTables(boolean shouldDeleteMapTables)
    {
        GenericDBConversion.shouldDeleteMapTables = shouldDeleteMapTables;
    }

    /**
     * @return the curDivisionID
     */
    public Integer getCurDivisionID()
    {
        return curDivisionID;
    }

    /**
     * @return the curDisciplineID
     */
    public Integer getCurDisciplineID()
    {
        return curDisciplineID;
    }

    /**
     * @return the curCollectionID
     */
    public Integer getCurCollectionID()
    {
        return curCollectionID;
    }

    /**
     * @return the curAgentCreatorID
     */
    public Integer getCurAgentCreatorID()
    {
        return curAgentCreatorID;
    }

    /**
     * @return the curAgentModifierID
     */
    public Integer getCurAgentModifierID()
    {
        return curAgentModifierID;
    }

    /**
     * @return the convLogger
     */
    public ConversionLogger getConvLogger()
    {
        return convLogger;
    }

    /**
     * @return the columnValueMapper
     */
    public Hashtable<String, BasicSQLUtilsMapValueIFace> getColumnValueMapper()
    {
        return columnValueMapper;
    }

    /**
     * @return the oldDBConn
     */
    public Connection getOldDBConn()
    {
        return oldDBConn;
    }

    /**
     * @param oldDBConn the oldDBConn to set
     */
    public void setOldDBConn(Connection oldDBConn)
    {
        this.oldDBConn = oldDBConn;
    }

    /**
     * @return the newDBConn
     */
    public Connection getNewDBConn()
    {
        return newDBConn;
    }
    
    
}
