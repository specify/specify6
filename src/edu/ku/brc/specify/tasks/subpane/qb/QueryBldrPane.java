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
package edu.ku.brc.specify.tasks.subpane.qb;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo.RelationshipType;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.ERTICaptionInfo.ColInfo;
import edu.ku.brc.af.ui.forms.formatters.DataObjDataField;
import edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionRelationship;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.SpExportSchema;
import edu.ku.brc.specify.datamodel.SpExportSchemaItem;
import edu.ku.brc.specify.datamodel.SpExportSchemaItemMapping;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.dbsupport.RecordTypeCodeBuilder;
import edu.ku.brc.specify.tasks.ExportMappingTask;
import edu.ku.brc.specify.tasks.QueryTask;
import edu.ku.brc.specify.tasks.ReportsBaseTask;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.specify.tasks.subpane.JasperCompilerRunnable;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchJRDataSource;
import edu.ku.brc.specify.tools.export.ConceptMapUtils;
import edu.ku.brc.specify.tools.export.MappedFieldInfo;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DropDownButton;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 * 
 * @code_status Alpha
 * 
 * Feb 23, 2007
 * 
 */
@SuppressWarnings("serial")
public class QueryBldrPane extends BaseSubPane implements QueryFieldPanelContainerIFace, CommandListener
{
    protected static final Logger                            log            = Logger.getLogger(QueryBldrPane.class);
    protected static final Color                             TITLEBAR_COLOR = new Color(82, 160, 52);
    protected static final int                               ExportSchemaPreviewSize = 120;
    //the maximum number of times the Parent relationship can be opened for recursive relationships.
    //This is currently only used for Containers (parent relationship has been unavailable for some time for Treeable tables).
    //It was originally necessary as a workaround to prevent a memory leak when the parent relationship was recursively opened, 
    //but now is used just to limit the recursion to a sane depth
    protected static final int                               maxParentChainLen = 7;     
    
    protected JList                                          tableList;
    protected Vector<QueryFieldPanel>                        queryFieldItems  = new Vector<QueryFieldPanel>();
    protected QueryFieldPanel                                selectedQFP = null; 
    protected int                                            currentInx       = -1;
    protected JPanel                                         queryFieldsPanel;
    protected JScrollPane                                    queryFieldsScroll;
    
    protected SpQuery                                        query            = null;

    protected JButton                                        addBtn;

    protected ImageIcon                                      blankIcon        = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);

    protected String                                         columnDefStr     = null;

    protected JPanel                                         listBoxPanel;
    protected Vector<JList>                                  listBoxList      = new Vector<JList>();
    protected JScrollPane                                    scrollPane;
    protected Vector<JScrollPane>                            spList           = new Vector<JScrollPane>();
    protected Vector<TableTree>                              tableTreeList    = new Vector<TableTree>();
    protected JPanel                                         contextPanel;
    protected DropDownButton                                 saveBtn;
    protected JButton                                        searchBtn;
    protected JCheckBox                                      distinctChk;
    protected JCheckBox                                      countOnlyChk;
    protected JCheckBox                                      searchSynonymyChk;
    protected boolean                                 		 searchSynonymy     = true;
    
    /**
     * When countOnly is true, count of matching records is displayed, but the records are not displayed.
     */
    protected boolean                                        countOnly = false;
    
    protected Hashtable<String, Boolean>                     fieldsToSkipHash = new Hashtable<String, Boolean>();
    protected QryListRenderer                                qryRenderer      = new QryListRenderer(IconManager.STD_ICON_SIZE);
    
    protected int                                            listCellHeight;

    protected TableTree                                      tableTree;
    protected Hashtable<String, TableTree>                   tableTreeHash;    
    protected boolean                                        processingLists  = false;

    protected RolloverCommand                                queryNavBtn      = null;

    // Reordering
    protected JButton                                        orderUpBtn  = null;
    protected JButton                                        orderDwnBtn = null;
    protected boolean                                        doOrdering  = false;
    
    protected ExpressSearchResultsPaneIFace                  esrp        = null;
    protected boolean                                        isHeadless  = false; 
    
    protected SpExportSchema                                 exportSchema;
    protected SpExportSchemaMapping                          schemaMapping;   
    /**
     * True if warning to reload after schema/treeDef changes has been shown.
     */
    protected boolean                                        reloadMsgShown = false;
    
    protected final AtomicReference<QBQueryForIdResultsHQL> runningResults = new AtomicReference<QBQueryForIdResultsHQL>();
    protected final AtomicReference<QBQueryForIdResultsHQL> completedResults = new AtomicReference<QBQueryForIdResultsHQL>();
    protected final AtomicLong doneTime = new AtomicLong(-1);
    protected final AtomicLong startTime = new AtomicLong(-1);
        
    
    /**
     * Constructor.
     * 
     * @param name name of subpanel
     * @param task the owning task
     */
    public QueryBldrPane(final String name, final Taskable task, final SpQuery query)
    {
        this(name, task, query, false);
    }
    
    /**
     * Constructor.
     * 
     * @param name name of subpanel
     * @param task the owning task
     */
    public QueryBldrPane(final String name, 
                         final Taskable task, 
                         final SpQuery query,
                         final boolean isHeadless)
    {
    	this(name, task, query, isHeadless, null, null);
    }
    /**
     * Constructor.
     * 
     * @param name name of subpanel
     * @param task the owning task
     */
    public QueryBldrPane(final String name, 
                         final Taskable task, 
                         final SpQuery query,
                         final boolean isHeadless,
                         final SpExportSchema exportSchema,
                         final SpExportSchemaMapping schemaMapping)
    {
        super(name, task);

        this.query      = query;
        this.isHeadless = isHeadless;
        this.exportSchema = exportSchema != null ? exportSchema : 
        	schemaMapping != null ? schemaMapping.getSpExportSchema() : null;
        if (exportSchema != null && schemaMapping == null)
        {
        	this.schemaMapping = new SpExportSchemaMapping();
        	this.schemaMapping.initialize();
        	this.schemaMapping.setSpExportSchema(exportSchema);
        }
        else
        {
        	this.schemaMapping = schemaMapping; 
        }
        String[] skipItems = { "TimestampCreated", "LastEditedBy", "TimestampModified" };
        for (String nameStr : skipItems)
        {
            fieldsToSkipHash.put(nameStr, true);
        }
        
        //loadAutoMaps();
        //writeAutoMapsToXml();
        
        QueryTask qt = (QueryTask )task;
        Pair<TableTree, Hashtable<String, TableTree>> trees = qt.getTableTrees();
        tableTree = trees.getFirst();
        tableTreeHash = trees.getSecond();
 
        createUI();

        setupUI();
        
        CommandDispatcher.register(ReportsBaseTask.REPORTS, this);
    }

//    protected void loadAutoMaps() {

//        for (Map.Entry<String, AutoMap> am : autoMaps.entrySet())
//        {
//        	System.out.println(am.getKey() + ": " + am.getValue());
//        }
    	
    	//from older darwin cores 
//    	autoMaps.put("scientificnameauthor", new AutoMap(
//				"1,9-determinations,4-preferredTaxon.taxon.author", "author",
//				"1,9-determinations,4-preferredTaxon", false));
//		autoMaps.put("collector", new AutoMap(
//				"1,10,30-collectors.collector.collectors", "collectors",
//				"1,10,30-collectors", true));
//		autoMaps.put("globaluniqueidentifier", new AutoMap(
//				"1.collectionobject.guid", "guid", "1", false));
//		autoMaps.put("daycollected", new AutoMap(
//				"1,10.collectingevent.startDate", "startDate", "1,10", false));
//		
//		//from darwin core used by ipt
//		autoMaps.put("catalognumber", new AutoMap("1.collectionobject.catalogNumber", "catalogNumber", "1", false));
//		autoMaps.put("class", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.Class", "Class", "1,9-determinations,4-preferredTaxon", false));
//		autoMaps.put("collectioncode", new AutoMap("1,23.collection.code", "code", "1,23", false));
//		autoMaps.put("continent", new AutoMap("1,10,2,3.geography.Continent", "Continent", "1,10,2,3", false));
//		
//		autoMaps.put("coordinateuncertaintyinmeters", new AutoMap("1,10,2.locality.latLongAccuracy", "latLongAccuracy", "1,10,2", false, false));
//		
//		autoMaps.put("country", new AutoMap("1,10,2,3.geography.Country", "Country", "1,10,2,3", false));
//		autoMaps.put("county", new AutoMap("1,10,2,3.geography.County", "County", "1,10,2,3", false));
//		
//		autoMaps.put("dateidentified", new AutoMap("1,9-determinations.determination.determinedDate", "determinedDate", "1,9-determinations", false, false));
//		
//		autoMaps.put("decimallatitude", new AutoMap("1,10,2.locality.latitude1", "latitude1", "1,10,2", false));
//		autoMaps.put("decimallongitude", new AutoMap("1,10,2.locality.longitude1", "longitude1", "1,10,2", false));
//		autoMaps.put("eventdate", new AutoMap("1,10.collectingevent.startDate", "startDate", "1,10", false));
//		
//		autoMaps.put("eventremarks", new AutoMap("1,10.collectingevent.remarks", "remarks", "1,10", false, false));
//		autoMaps.put("eventtime", new AutoMap("1,10.collectingevent.startTime", "startTime", "1,10", false, false));
//		
//		autoMaps.put("family", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.Family", "Family", "1,9-determinations,4-preferredTaxon", false));
//		autoMaps.put("fieldnumber", new AutoMap("1.collectionobject.fieldNumber", "fieldNumber", "1", false));
//		autoMaps.put("genus", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.Genus", "Genus", "1,9-determinations,4-preferredTaxon", false));
//		
//		autoMaps.put("geodeticdatum", new AutoMap("1,10,2.locality.datum", "datum", "1,10,2", false, false));
//		autoMaps.put("georeferencedby", new AutoMap("1,10,2,123-geoCoordDetails,5-geoRefDetBy.agent.geoRefDetBy", "geoRefDetBy", "1,10,2,123-geoCoordDetails,5-geoRefDetBy", false, false));
//		autoMaps.put("georeferenceprotocol", new AutoMap("1,10,2,123-geoCoordDetails.geocoorddetail.protocol", "protocol", "1,10,2,123-geoCoordDetails", false, false));
//		autoMaps.put("georeferenceremarks", new AutoMap("1,10,2,123-geoCoordDetails.geocoorddetail.geoRefRemarks", "geoRefRemarks", "1,10,2,123-geoCoordDetails", false, false));
//		autoMaps.put("georeferencesources", new AutoMap("1,10,2,123-geoCoordDetails.geocoorddetail.source", "source", "1,10,2,123-geoCoordDetails", false, false));
//		autoMaps.put("georeferenceverificationstatus", new AutoMap("1,10,2,123-geoCoordDetails.geocoorddetail.geoRefVerificationStatus", "geoRefVerificationStatus", "1,10,2,123-geoCoordDetails", false, false));
//		autoMaps.put("habitat", new AutoMap("1,10,92.collectingeventattribute.text9", "text9", "1,10,92", false, false));
//		autoMaps.put("identificationqualifier", new AutoMap("1,9-determinations.determination.qualifier", "qualifier", "1,9-determinations", false, false));
//		autoMaps.put("identificationreferences", new AutoMap("1,9-determinations,38-determinationCitations.determinationcitation.determinationCitations", "determinationCitations", "1,9-determinations,38-determinationCitations", false, false));
//		autoMaps.put("identificationremarks", new AutoMap("1,9-determinations.determination.remarks", "remarks", "1,9-determinations", false, false));
//		
//		autoMaps.put("identifiedby", new AutoMap("1,9-determinations,5-determiner.agent.determiner", "determiner", "1,9-determinations,5-determiner", false));
//		autoMaps.put("individualcount", new AutoMap("1.collectionobject.countAmt", "countAmt", "1", false));
//		autoMaps.put("infraspecificepithet", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.Subspecies", "Subspecies", "1,9-determinations,4-preferredTaxon", false));
//		autoMaps.put("institutioncode", new AutoMap("1,23,26,96,94.institution.code", "code", "1,23,26,96,94", false));
//		
//		autoMaps.put("island", new AutoMap("1,10,2,124-localityDetails.localitydetail.island", "island", "1,10,2,124-localityDetails", false, false));
//		autoMaps.put("islandgroup", new AutoMap("1,10,2,124-localityDetails.localitydetail.islandGroup", "islandGroup", "1,10,2,124-localityDetails", false, false));
//		
//		autoMaps.put("kingdom", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.Kingdom", "Kingdom", "1,9-determinations,4-preferredTaxon", false));
//		
//		autoMaps.put("lifestage", new AutoMap("1,93.collectionobjectattribute.text4", "text4", "1,93", false, false));
//		
//		autoMaps.put("locality", new AutoMap("1,10,2.locality.localityName", "localityName", "1,10,2", false));
//		
//		autoMaps.put("locationremarks", new AutoMap("1,10,2.locality.remarks", "remarks", "1,10,2", false, false));
//		autoMaps.put("maximumdepthinmeters", new AutoMap("1,10,92.collectingeventattribute.text2", "text2", "1,10,92", false, false));
//		
//		autoMaps.put("maximumelevationinmeters", new AutoMap("1,10,2.locality.maxElevation", "maxElevation", "1,10,2", false));
//		
//		autoMaps.put("minimumdepthinmeters", new AutoMap("1,10,92.collectingeventattribute.text1", "text1", "1,10,92", false, false));
//		
//		autoMaps.put("minimumelevationinmeters", new AutoMap("1,10,2.locality.minElevation", "minElevation", "1,10,2", false));
//		
//		autoMaps.put("occurrenceremarks", new AutoMap("1.collectionobject.remarks", "remarks", "1", false, false));
//		
//		autoMaps.put("order", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.Order", "Order", "1,9-determinations,4-preferredTaxon", false));
//		
//		autoMaps.put("othercatalognumbers", new AutoMap("1.collectionobject.altCatalogNumber", "altCatalogNumber", "1", false, false));
//		
//		autoMaps.put("phylum", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.Phylum", "Phylum", "1,9-determinations,4-preferredTaxon", false));
//		autoMaps.put("preparations", new AutoMap("1,63-preparations.preparation.preparations", "preparations", "1,63-preparations", false));
//		
//		autoMaps.put("previousidentifications", new AutoMap("1,9-determinations.determination.determinations", "determinations", "1,9-determinations", false, false));
//		
//		autoMaps.put("recordedby", new AutoMap(
//				"1,10,30-collectors.collector.collectors", "collectors",
//				"1,10,30-collectors", true));
//		
//		autoMaps.put("reproductivecondition", new AutoMap("1,93.collectionobjectattribute.text3", "text3", "1,93", false, false));
//		
//		autoMaps.put("scientificname", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.fullName", "fullName", "1,9-determinations,4-preferredTaxon", false));
//		
//		autoMaps.put("scientificnameauthorship", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.author", "author", "1,9-determinations,4-preferredTaxon", false, false));
//		autoMaps.put("sex", new AutoMap("1,93.collectionobjectattribute.text1", "text1", "1,93", false, false));
//		
//		autoMaps.put("specificepithet", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.Species", "Species", "1,9-determinations,4-preferredTaxon", false));
//		autoMaps.put("stateprovince", new AutoMap("1,10,2,3.geography.State", "State", "1,10,2,3", false));
//		
//		autoMaps.put("subgenus", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.Subgenus", "Subgenus", "1,9-determinations,4-preferredTaxon", false, false));
//		autoMaps.put("taxonremarks", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.remarks", "remarks", "1,9-determinations,4-preferredTaxon", false, false));
//		
//		autoMaps.put("typestatus", new AutoMap("1,9-determinations.determination.typeStatusName", "typeStatusName", "1,9-determinations", false));
//		
//		autoMaps.put("verbatimelevation", new AutoMap("1,10,2.locality.verbatimElevation", "verbatimElevation", "1,10,2", false, false));
//		autoMaps.put("verbatimeventdate", new AutoMap("1,10.collectingevent.verbatimDate", "verbatimDate", "1,10", false, false));
//		autoMaps.put("verbatimlocality", new AutoMap("1,10.collectingevent.verbatimLocality", "verbatimLocality", "1,10", false, false));
//		autoMaps.put("vernacularname", new AutoMap("1,9-determinations,4-preferredTaxon.taxon.commonName", "commonName", "1,9-determinations,4-preferredTaxon", false, false));
//		autoMaps.put("waterbody", new AutoMap("1,10,2,124-localityDetails.localitydetail.waterBody", "waterBody", "1,10,2,124-localityDetails", false, false));	
//	}
    
//    protected void writeAutoMapsToXml()
//    {
//    	File out = new File("/home/timo/automap.xml");
//    	Vector<String> lines = new Vector<String>();
//    	for (Map.Entry<String, AutoMap> me : autoMaps.entrySet())
//    	{
//    		String line = "<default_mapping name=\"" + me.getKey() + "\" " + me.getValue().toXML() + "/>" ;
//    		lines.add(line);
//    	}
//    	try
//    	{
//    		FileUtils.writeLines(out, lines);
//    	} catch(Exception ex)
//    	{
//    		ex.printStackTrace();
//    	}
//    }
    
    /**
     * create the query builder UI.
     */
    protected void createUI()
    {
        removeAll();

        JMenuItem saveItem = new JMenuItem(UIRegistry.getResourceString("QB_SAVE"));
        Action saveActionListener = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (saveQuery(false))
                {
                    saveBtn.setEnabled(false);
                }
            }
        };
        saveItem.addActionListener(saveActionListener);
        
        JMenuItem saveAsItem = new JMenuItem(UIRegistry.getResourceString("QB_SAVE_AS"));
        Action saveAsActionListener = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (saveQuery(true))
                {
                    saveBtn.setEnabled(false);
                }
            }
        };
        saveAsItem.addActionListener(saveAsActionListener);
        JComponent[] itemSample = { saveItem, saveAsItem };
        saveBtn = new DropDownButton(UIRegistry.getResourceString("QB_SAVE"), null, 1,
                java.util.Arrays.asList(itemSample));
        saveBtn.addActionListener(saveActionListener);
        String ACTION_KEY = "SAVE";
        KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S,
        		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());        
        InputMap inputMap = saveBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(ctrlS, ACTION_KEY);
        ActionMap actionMap = saveBtn.getActionMap();
        actionMap.put(ACTION_KEY, saveActionListener);
        ACTION_KEY = "SAVE_AS";
        KeyStroke ctrlA = KeyStroke.getKeyStroke(KeyEvent.VK_A,
        		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());        
        inputMap.put(ctrlA, ACTION_KEY);
        actionMap.put(ACTION_KEY, saveAsActionListener);
        saveBtn.setActionMap(actionMap);
        
        UIHelper.setControlSize(saveBtn);
        //saveBtn.setOverrideBorder(true, BasicBorders.getButtonBorder());
        
        listBoxPanel = new JPanel(new HorzLayoutManager(2, 2));

        Vector<TableQRI> list = new Vector<TableQRI>();
        for (int k=0; k<tableTree.getKids(); k++)
        {
            list.add(tableTree.getKid(k).getTableQRI());
        }

        Collections.sort(list);
        DefaultListModel model = new DefaultListModel();
        for (TableQRI qri : list)
        {
            model.addElement(qri);
        }

        tableList = new JList(model);
        QryListRenderer qr = new QryListRenderer(IconManager.IconSize.Std16);
        qr.setDisplayKidIndicator(false);
        tableList.setCellRenderer(qr);

        JScrollPane spt = new JScrollPane(tableList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        Dimension pSize = spt.getPreferredSize();
        pSize.height = 200;
        spt.setPreferredSize(pSize);

        JPanel topPanel = new JPanel(new BorderLayout());

        scrollPane = new JScrollPane(listBoxPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        tableList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    int inx = tableList.getSelectedIndex();
                    if (inx > -1)
                    {
                        fillNextList(tableList);
                    }
                    else
                    {
                        listBoxPanel.removeAll();
                    }
                }
            }
        });

        addBtn = new JButton(IconManager.getImage("PlusSign", IconManager.IconSize.Std16));
        addBtn.setEnabled(false);
        addBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                BaseQRI qri = (BaseQRI) listBoxList.get(currentInx).getSelectedValue();
                if (qri.isInUse)
                {
                	return; 
                }
                
                if (exportSchema != null && QueryBldrPane.this.selectedQFP == null)
                {
                	return;
                }
                
                try
				{
					FieldQRI fieldQRI = buildFieldQRI(qri);
					if (fieldQRI == null)
					{
						throw new Exception("null FieldQRI");
					}
					SpQueryField qf = new SpQueryField();
					qf.initialize();
					qf.setFieldName(fieldQRI.getFieldName());
					qf.setStringId(fieldQRI.getStringId());
					query.addReference(qf, "fields");

					if (exportSchema == null)
					{
						addQueryFieldItem(fieldQRI, qf, false);
					} else
					{
						addNewMapping(fieldQRI, qf, selectedQFP);
					}
				} catch (Exception ex)
				{
					log.error(ex);
					UsageTracker.incrHandledUsageCount();
					edu.ku.brc.exceptions.ExceptionTracker.getInstance()
							.capture(QueryBldrPane.class, ex);
					return;
				}
             }
        });

        contextPanel = new JPanel(new BorderLayout());
        contextPanel.add(createLabel("Search Context", SwingConstants.CENTER), BorderLayout.NORTH); // I18N
        contextPanel.add(spt, BorderLayout.CENTER);
        contextPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JPanel schemaPanel = new JPanel(new BorderLayout());
        schemaPanel.add(scrollPane, BorderLayout.CENTER);

        topPanel.add(contextPanel, BorderLayout.WEST);
        topPanel.add(schemaPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        queryFieldsPanel = new JPanel();
        queryFieldsPanel.setLayout(new NavBoxLayoutManager(0, 2));
        queryFieldsScroll = new JScrollPane(queryFieldsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        queryFieldsScroll.setBorder(null);
        add(queryFieldsScroll);
        
        if (exportSchema == null)
        {
        	final JPanel mover = buildMoverPanel(false);
        	add(mover, BorderLayout.EAST);
        }
        
        String searchLbl = schemaMapping == null ? getResourceString("QB_SEARCH") : getResourceString("QB_EXPORT_PREVIEW");
        searchBtn   = createButton(searchLbl);
        searchBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
            	doSearch();
            }
        });
        distinctChk = createCheckBox(UIRegistry.getResourceString("QB_DISTINCT"));
        distinctChk.setVisible(schemaMapping == null);
        if (schemaMapping == null)
        {
        	distinctChk.setSelected(false);
        	distinctChk.addActionListener(new ActionListener()
        	{
        		public void actionPerformed(ActionEvent ae)
        		{
        			new SwingWorker() {

        				/* (non-Javadoc)
        				 * @see edu.ku.brc.helpers.SwingWorker#construct()
        				 */
        				@Override
        				public Object construct()
        				{
        					if (distinctChk.isSelected())
        					{
        						UsageTracker.incrUsageCount("QB.DistinctOn");
        					}
        					else
        					{
        						UsageTracker.incrUsageCount("QB.DistinctOff");
        					}
        					if ((isTreeLevelSelected() || isAggFieldSelected()) && countOnly && distinctChk.isSelected())
        					{
        						countOnlyChk.setSelected(false);
        						countOnly = false;
        					}
        					query.setCountOnly(countOnly);
        					query.setSelectDistinct(distinctChk.isSelected());
        					saveBtn.setEnabled(queryFieldItems.size() > 0);
        					return null;
        				}
        			}.start();
        		}
        	});
        }
        countOnlyChk = createCheckBox(UIRegistry.getResourceString("QB_COUNT_ONLY"));
        countOnlyChk.setSelected(false);
        countOnlyChk.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                new SwingWorker() {

                    /* (non-Javadoc)
                     * @see edu.ku.brc.helpers.SwingWorker#construct()
                     */
                    @Override
                    public Object construct()
                    {
                        //Don't allow change while query is running.
                        if (runningResults.get() == null)
                        {
                            countOnly = !countOnly;
                            if (countOnly)
                            {
                                UsageTracker.incrUsageCount("QB.CountOnlyOn");
                            }
                            else
                            {
                                UsageTracker.incrUsageCount("QB.CountOnlyOff");
                            }
                            if ((isTreeLevelSelected() || isAggFieldSelected()) && countOnly && distinctChk.isSelected())
                            {
                            	distinctChk.setSelected(false);
                            }
                        }
                        else
                        {
                            //This might be awkward and/or klunky...
                            countOnlyChk.setSelected(countOnly);
                        }
                        query.setCountOnly(countOnly);
                        query.setSelectDistinct(distinctChk.isSelected());
                        saveBtn.setEnabled(queryFieldItems.size() > 0);
                        return null;
                    }
                }.start();
            }
        });

        searchSynonymyChk = createCheckBox(UIRegistry.getResourceString("QB_SRCH_SYNONYMS"));
        searchSynonymyChk.setSelected(searchSynonymy);
        searchSynonymyChk.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                new SwingWorker() {

                    /* (non-Javadoc)
                     * @see edu.ku.brc.helpers.SwingWorker#construct()
                     */
                    @Override
                    public Object construct()
                    {
                        searchSynonymy = !searchSynonymy;
                        if (!searchSynonymy)
                        {
                            UsageTracker.incrUsageCount("QB.SearchSynonymyOff");
                        }
                        else
                        {
                            UsageTracker.incrUsageCount("QB.SearchSynonymyOn");
                        }
                        query.setSearchSynonymy(searchSynonymy);
                        saveBtn.setEnabled(queryFieldItems.size() > 0);
                        return null;
                    }
                }.start();
            }
        });

        PanelBuilder outer = new PanelBuilder(new FormLayout("p, 2dlu, p, 2dlu, p, 2dlu, p, 6dlu, p", "p"));
 
        CellConstraints cc = new CellConstraints();
        outer.add(searchSynonymyChk, cc.xy(1, 1));
        outer.add(distinctChk, cc.xy(3, 1));
        outer.add(countOnlyChk, cc.xy(5, 1));
        outer.add(searchBtn, cc.xy(7, 1));
        
        
        
        outer.add(saveBtn, cc.xy(9, 1));
        
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(outer.getPanel(), BorderLayout.EAST);
        
        JButton helpBtn = UIHelper.createHelpIconButton("QB");
        bottom.add(helpBtn, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
    
    /**
     * @param fieldQRI
     * @param qf
     * 
     * Adds a new mapping or condition for schema export.
     */
    protected void addNewMapping(FieldQRI fieldQRI, SpQueryField qf, QueryFieldPanel qfp)
    {
    	if (qfp != null)
    	{
    		QueryFieldPanel newQfp = null;
    		if (qfp.getFieldQRI() != null)
    		{
    			qfp.getFieldQRI().setIsInUse(false);
    		}
    		if (qfp.getQueryField() != null)
    		{
    			//removeReference doesn't work. Something off with the equals method or Comparable<?> stuff or something.
    			//query.removeReference(selectedQFP.getQueryField(), "fields");
    			removeFieldFromQuery(qfp.getQueryField());
    			if (!qfp.isConditionForSchema())
    			{
    				removeSchemaItemMapping(qfp.getItemMapping());
    			}
    		}
    		if (!qfp.isConditionForSchema())
			{
				SpExportSchemaItemMapping newMapping = new SpExportSchemaItemMapping();
				newMapping.initialize();
				newMapping.setExportSchemaItem(qfp.getSchemaItem());
				newMapping.setExportSchemaMapping(schemaMapping);
				newMapping.setQueryField(qf);
				schemaMapping.getMappings().add(newMapping);
				qf.setMapping(newMapping);
			}
			qfp.setField(fieldQRI, qf);
			fieldQRI.setIsInUse(true);
			if (qfp.isConditionForSchema())
			{
				final QueryFieldPanel theNewQfp = new QueryFieldPanel(this,
						null, columnDefStr, saveBtn, null, null, true);
				theNewQfp.addMouseListener(new MouseInputAdapter()
				{
					@Override
					public void mousePressed(MouseEvent e)
					{
						selectQFP(theNewQfp);
					}
				});
				queryFieldItems.add(theNewQfp);
				newQfp = theNewQfp;
			}
    		updateUIAfterAddOrMap(fieldQRI, newQfp, false, newQfp != null);
    	}
    }
    
    /**
     * @param schemaItem
     * @return true if schemaItem was removed
     */
    protected boolean removeSchemaItemMapping(SpExportSchemaItemMapping itemMapping)
    {
    	int size = schemaMapping.getMappings().size();
    	//XXX Most probably not necessary to worry multiple copies of mapping items.
    	SpExportSchemaItemMapping theOne = null;
    	for (SpExportSchemaItemMapping esim : schemaMapping.getMappings())
    	{
    		if (esim.getExportSchemaItem().getId().equals(itemMapping.getExportSchemaItem().getId()))
    		{
    			theOne = esim;
    			break;
    		}
    	}
    	if (theOne != null)
    	{
    		schemaMapping.getMappings().remove(theOne);
    		theOne.setExportSchemaItem(null);
    		theOne.setExportSchemaMapping(null);
    		theOne.getQueryField().setMapping(null);
    		if (theOne != itemMapping)
    		{
    			itemMapping.setExportSchemaMapping(null);
    			itemMapping.getQueryField().setMapping(null);
    			itemMapping.setQueryField(null);
    		}
    	}
    	return schemaMapping.getMappings().size() < size;
    }
    
    /**
     * @param toRemove
     * @return true if the field was actually removed
     * 
     */
    protected boolean removeFieldFromQuery(SpQueryField toRemove)
    {
    	int size = query.getFields().size();
    	//XXX probably don't need to worry about multiple copies of QueryFields and Mappings anymore???
    	SpQueryField theFieldObjectInTheFieldsSetToRemove = null;
    	for (SpQueryField fld : query.getFields())
    	{
    		if (fld == toRemove)
    		{
    			theFieldObjectInTheFieldsSetToRemove = fld;
    			break;
    		}
    		else if (fld.getId() != null && toRemove.getId() != null && fld.getId().equals(toRemove.getId()))
    		{
    			theFieldObjectInTheFieldsSetToRemove = fld;
    			break;
    		}
    		else if (fld.getStringId().equals(toRemove.getStringId()))
    		{
    			theFieldObjectInTheFieldsSetToRemove = fld;
    			break;
    		}
   		}
    	if (theFieldObjectInTheFieldsSetToRemove != null)
    	{
    		query.getFields().remove(theFieldObjectInTheFieldsSetToRemove);
    		theFieldObjectInTheFieldsSetToRemove.setQuery(null);
    	}
    	
    	//XXX probably not necessary to check for this anymore ???
    	if (toRemove != theFieldObjectInTheFieldsSetToRemove)
    	{
    		toRemove.getQuery().getFields().remove(toRemove);
    		toRemove.setQuery(null);
    	}
    	return query.getFields().size() < size;
    }

    /**
     * @param esrp the esrp to set
     */
    public void setEsrp(ExpressSearchResultsPaneIFace esrp)
    {
        this.esrp = esrp;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#doSearch()
     */
    public void doSearch()
    {
        if (canSearch())
        {
            if (distinctChk.isSelected())
            {
                UsageTracker.incrUsageCount("QB.DoSearchDistinct." + query.getContextName());
            }
            else
            {
                UsageTracker.incrUsageCount("QB.DoSearch." + query.getContextName());
            }
            doSearch((TableQRI)tableList.getSelectedValue(), distinctChk.isSelected());
        }
        else 
        {
            cancelSearch();
        }
    }

    /**
     * cancel the currently exeecuting search.
     */
    protected void cancelSearch()
    {
        if (runningResults.get() != null)
        {
            log.debug("cancelling search");
            UsageTracker.incrUsageCount("QB.CancelSearch." + query.getContextName());
            runningResults.get().cancel();
        }
    }
    /**
     * @param fieldName
     * @return fieldName with lower-cased first character.
     */
    public static String fixFieldName(final String fieldName)
    {
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }

    
    /**
     * @param q
     * @param container
     * @param tblTree
     * @param ttHash
     * @return a Vector of QueryFieldPanel objects for the supplied fields parameter.
     */
    protected static Vector<QueryFieldPanel> getQueryFieldPanels(final SpQuery q,
                                                               final QueryFieldPanelContainerIFace container,
                                                               final TableTree tblTree,
                                                               final Hashtable<String, TableTree> ttHash) 
    {
        return getQueryFieldPanels(container, q.getFields(), tblTree, ttHash, null, null);
    }
    
    
    /**
     * 
     */
    protected void setupUI()
    {
        if (!isHeadless && !SwingUtilities.isEventDispatchThread()) 
        { 
            throw new RuntimeException("Method called from invalid thread."); 
        }
   
        queryFieldsPanel.removeAll();
        queryFieldItems.clear();
        queryFieldsPanel.validate();
        columnDefStr = null;
        tableList.clearSelection();
        contextPanel.setVisible(query == null);
        tableList.setSelectedIndex(-1);
        if (query != null)
        {
            //query.forceLoad(true); 
            Short tblId = query.getContextTableId();
            if (tblId != null)
            {
                for (int i = 0; i < tableList.getModel().getSize(); i++)
                {
                    TableQRI qri = (TableQRI) tableList.getModel().getElementAt(i);
                    if (qri.getTableInfo().getTableId() == tblId.intValue())
                    {
                        tableList.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
        
        Vector<QueryFieldPanel> qfps = null;
        boolean dirty = false;
        List<String> missingFlds = new LinkedList<String>();
        if (query != null)
        {
            TableQRI qri = (TableQRI) tableList.getSelectedValue();
            if (qri == null) 
            { 
                throw new RuntimeException("Invalid context for query."); 
            }
            //query.forceLoad(true);                	
            qfps = exportSchema == null ? getQueryFieldPanels(this, query.getFields(), tableTree, tableTreeHash, saveBtn, missingFlds)
            		: getQueryFieldPanelsForMapping(this, query.getFields(), tableTree, tableTreeHash, saveBtn, schemaMapping, missingFlds, 
            				ConceptMapUtils.getDefaultDarwinCoreMappings());
            
            if (missingFlds.size() > 0)
            {
                dirty = true;
                String fldStr = new String();
                for (String fld : missingFlds)
                {
                    if (!StringUtils.isEmpty(fldStr))
                    {
                        fldStr += ", ";
                    }
                    fldStr += "'" + fld + "'";
                }
                if (missingFlds.size() == 1)
                {
                    UIRegistry.showLocalizedMsg("QB_FIELD_MISSING_TITLE", "QB_FIELD_NOT_ADDED", fldStr);
                }
                else
                {
                    UIRegistry.showLocalizedMsg("QB_FIELD_MISSING_TITLE", "QB_FIELDS_NOT_ADDED", fldStr);
                }
            }
        }

        boolean header = true;
        boolean doAutoMap = true;
        for (final QueryFieldPanel qfp : qfps)
        {
            if (header)
            {
                header = false;
                this.queryFieldsScroll.setColumnHeaderView(qfp);
            }
            else
            {
                queryFieldItems.add(qfp);
                qfp.addMouseListener(new MouseInputAdapter()
                {
                    @Override
                    public void mousePressed(MouseEvent e)
                    {
                        selectQFP(qfp);
                    }
                });
                qfp.resetValidator();
                queryFieldsPanel.add(qfp);
                doAutoMap &= qfp.getFieldQRI() == null;
            }
        }
        qualifyFieldLabels();
        
        if (doAutoMap)
        {
        	for (QueryFieldPanel qfp : queryFieldItems)
        	{
        		if (!qfp.isConditionForSchema())
				{
					MappedFieldInfo mappedTo = ConceptMapUtils.getDefaultDarwinCoreMappings().get(qfp.getSchemaItem()
							.getFieldName().toLowerCase());
					if (mappedTo != null)
					{
						FieldQRI fqri = getFieldQRI(tableTree, mappedTo
								.getFieldName(), mappedTo.isRel(), mappedTo
								.getStringId(), getTableIds(mappedTo
								.getTableIds()), 0, tableTreeHash);
						if (fqri != null)
						{
							SpQueryField qf = new SpQueryField();
							qf.initialize();
							qf.setFieldName(fqri.getFieldName());
							qf.setStringId(fqri.getStringId());
							query.addReference(qf, "fields");
							addNewMapping(fqri, qf, qfp);
							dirty = true;
						}
					}
				}
        	}
        }
        	
        SwingUtilities.invokeLater(new Runnable(){

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run()
			{
				distinctChk.setSelected(query.isSelectDistinct());
				countOnlyChk.setSelected(query.getCountOnly() == null ? false : query.getCountOnly());
				countOnly = countOnlyChk.isSelected();
				searchSynonymyChk.setSelected(query.getSearchSynonymy() == null ? true : query.getSearchSynonymy());
				searchSynonymy = searchSynonymyChk.isSelected();
			}
        	
        });
        final boolean saveBtnEnabled = dirty;
        if (!isHeadless)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    adjustPanelUI(saveBtnEnabled);
                }
            });
        } else
        {
            adjustPanelUI(saveBtnEnabled);
        }
    }
    
    /**
     * @return true if query is savable and the current user has permission to save.
     */
    protected boolean canSave()
    {
        boolean result = true;
        //if the query builder is enabled for a user then the user can save queries.
//        if (AppContextMgr.isSecurityOn() && (!task.getPermissions().canAdd() || !task.getPermissions().canModify()))
//        {
//            if (!task.getPermissions().canAdd() && !task.getPermissions().canModify())
//            {
//                result = false;
//            }
//            else
//            {
//                boolean newQ = query == null || query.getId() == null;
//                //if canAdd but !canModify then some strange behavior may result
//                result = newQ ? task.getPermissions().canAdd() : task.getPermissions().canModify();
//            }
//        }
        return result;
    }
    /**
     * 
     */
    protected void adjustPanelUI(boolean saveBtnEnabled)
    {
        //Sorry, but a new context can't be selected if any fields are selected from the current context.
        tableList.setEnabled(queryFieldItems.size() == 0);
        if (queryFieldItems.size() > 0)
        {
            selectQFP(queryFieldItems.get(0));
        }
        for (JList list : listBoxList)
        {
            list.repaint();
        }
        saveBtn.setEnabled(saveBtnEnabled);
        saveBtn.setVisible(canSave());
        updateSearchBtn();
        QueryBldrPane.this.validate();
    }
    
    /**
     * @param rootTable
     * @param distinct
     * @param qfps
     * @param tblTree
     * @param keysToRetrieve
     * @return HQLSpecs for the current fields and settings.
     */
    public static HQLSpecs buildHQL(final TableQRI rootTable, 
                                       final boolean distinct, 
                                       final Vector<QueryFieldPanel> qfps,
                                       final TableTree tblTree, 
                                       final RecordSetIFace keysToRetrieve,
                                       final boolean searchSynonymy,
                                       final boolean isSchemaExport,
                                       final Timestamp lastExportTime) throws ParseException
    {
        if (qfps.size() == 0)
            return null;
        
        if (keysToRetrieve != null && keysToRetrieve.getNumItems() == 0)
            return null;
        
        StringBuilder fieldsStr = new StringBuilder();
        Vector<BaseQRI> list = new Vector<BaseQRI>();
        StringBuilder criteriaStr = new StringBuilder();
        StringBuilder orderStr = new StringBuilder();
        LinkedList<SortElement> sortElements = new LinkedList<SortElement>();
        boolean postSortPresent = false;
        boolean debug = false;
        ProcessNode root = new ProcessNode();
        int fldPosition = distinct ? 0 : 1;

        for (QueryFieldPanel qfi : qfps)
        {
        	if (qfi.getFieldQRI() == null)
        	{
        		continue;
        	}
   
        	qfi.updateQueryField();

            if (qfi.isForDisplay())
            {
                fldPosition++;
            }

            if (debug)
            {
                log.debug("\nNode: " + qfi.getFieldName());
            }

            SortElement orderSpec = qfi.getOrderSpec(distinct ? fldPosition-1 : fldPosition-2);
            if (orderSpec != null)
            {
                postSortPresent |= qfi.getFieldQRI() instanceof TreeLevelQRI || qfi.getFieldQRI() instanceof RelQRI;
                sortElements.add(orderSpec);
            }

            // Create a Stack (list) of parent from
            // the current node up to the top
            // basically we are creating a path of nodes
            // to determine if we need to create a new node in the tree
            list.clear();
            FieldQRI pqri = qfi.getFieldQRI();
            TableTree parent = pqri.getTableTree();
            if (qfi.isForDisplay() || qfi.hasCriteria() || orderSpec != null
					|| pqri instanceof RelQRI)
			{
				boolean addToList = true;
				if (pqri instanceof RelQRI)
				{
					RelQRI relQRI = (RelQRI) pqri;
					RelationshipType relType = relQRI.getRelationshipInfo()
							.getType();

					// XXX Formatter.getSingleField() checks for ZeroOrOne and
					// OneToOne rels.

					if (!relType.equals(RelationshipType.ManyToOne)
							&& !relType.equals(RelationshipType.ManyToMany)/*
																			 * treat
																			 * manytomany
																			 * as
																			 * onetomany
																			 */) // Maybe
																					// need
																					// to
																					// consider
																					// some
																					// types
																					// of
																					// OneToOne
																					// also?????????
					{
						parent = parent.getParent();
						if (isSchemaExport && lastExportTime != null)
						{
							addToList = true;
						}
						else
						{
						// parent will initially point to the related table
						// and don't need to add related table unless it has
						// children displayed/queried,
							addToList = false;
						}
					} else
					{
						DataObjDataFieldFormatIFace formatter = relQRI
								.getDataObjFormatter();
						if (formatter != null)
						{
							addToList = formatter.getSingleField() != null || (isSchemaExport && lastExportTime != null);
						} else
						{
							addToList = false;
						}
					}
				}
				if (addToList)
				{
					list.insertElementAt(pqri, 0);
				}
				while (parent != tblTree)
				{
					list.insertElementAt(parent.getTableQRI(), 0);
					parent = parent.getParent();
				}

				if (debug)
				{
					log.debug("Path From Top Down:");
					for (BaseQRI qri : list)
					{
						log.debug("  " + qri.getTitle());
					}
				}

				// Now walk the stack top (the top most parent)
				// down and if the path form the top down doesn't
				// exist then add a new node
				ProcessNode parentNode = root;
				int q = 0;
				for (BaseQRI qri : list)
				{
					if (debug)
					{
						log.debug("ProcessNode[" + qri.getTitle() + "]");
					}
					q++;
					if (!parentNode.contains(qri) && (qri instanceof TableQRI || q == list.size()))
					{
						ProcessNode newNode = new ProcessNode(qri);
						parentNode.getKids().add(newNode);
						if (debug)
						{
							log.debug("Adding new node["
									+ newNode.getQri().getTitle()
									+ "] to Node["
									+ (parentNode.getQri() == null ? "root"
											: parentNode.getQri().getTitle())
									+ "]");
						}
						parentNode = newNode;
					} else
					{
						for (ProcessNode kidNode : parentNode.getKids())
						{
							if (kidNode.getQri().equals(qri))
							{
								parentNode = kidNode;
								break;
							}
						}
					}
				}
				
				if (debug)
				{
					log.debug("Current Tree:");
					printTree(root, 0);
				}
			}
        }

        if (debug)
        {
            printTree(root, 0);
        }
        
        StringBuilder fromStr = new StringBuilder();
        TableAbbreviator tableAbbreviator = new TableAbbreviator();
        List<Pair<DBTableInfo,String>> fromTbls = new LinkedList<Pair<DBTableInfo,String>>();
        boolean hqlHasSynJoins = processTree(root, fromStr, fromTbls, 0, tableAbbreviator, tblTree, qfps, searchSynonymy, isSchemaExport, lastExportTime);

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("select ");
        if (distinct || hqlHasSynJoins)
        {
            sqlStr.append(" distinct ");
        }
        if (!distinct)
        {
            fieldsStr.append(tableAbbreviator.getAbbreviation(rootTable.getTableTree()));
            fieldsStr.append(".");
            fieldsStr.append(rootTable.getTableInfo().getIdFieldName());
        }

        List<Pair<String,Object>> paramsToSet = new LinkedList<Pair<String, Object>>();
        boolean visibleFldExists = false;
        for (QueryFieldPanel qfi : qfps)
        {
            if (qfi.getFieldQRI() == null)
            {
            	continue;
            }
 
        	if (qfi.isForDisplay())
            {
                visibleFldExists = true;
        		String fldSpec = qfi.getFieldQRI().getSQLFldSpec(tableAbbreviator, false, isSchemaExport);
                if (StringUtils.isNotEmpty(fldSpec))
                {
                    if (fieldsStr.length() > 0)
                    {
                        fieldsStr.append(", ");
                    }
                    fieldsStr.append(fldSpec);
                }
            }
            if (keysToRetrieve == null || qfi.isEnforced())
            {
                String criteria = qfi.getCriteriaFormula(tableAbbreviator, paramsToSet);
                boolean isDisplayOnly = StringUtils.isEmpty(criteria);
                if (!isDisplayOnly)
                {
                    if (!isDisplayOnly && criteriaStr.length() > 0)
                    {
                        criteriaStr.append(" AND ");
                    }
                    if (hqlHasSynJoins && isSynSearchable(qfi.getFieldQRI()) && !qfi.isEmptyCriterion())
                    {
                        criteria = adjustForSynSearch(tableAbbreviator.getAbbreviation(qfi.getFieldQRI().getTable().getTableTree()), criteria, qfi.isNegated());
                    }
                    criteriaStr.append(criteria);
                }
            }
        }
        if (!visibleFldExists) 
        {
        	throw new ParseException(getResourceString("QueryBldrPane.NoVisibleColumns"), -1);
        }
        
        sqlStr.append(fieldsStr);

        sqlStr.append(" from ");
        sqlStr.append(fromStr);

        if (keysToRetrieve != null)
        {
        	if (!StringUtils.isEmpty(criteriaStr.toString()))
            {
                criteriaStr.append(" and ");
            }
            criteriaStr.append(tableAbbreviator.getAbbreviation(rootTable.getTableTree()) + "." 
                    + rootTable.getTableInfo().getIdFieldName() + " in(");
            boolean comma = false;
            for (RecordSetItemIFace item : keysToRetrieve.getOrderedItems())
            {
                if (comma)
                {
                    criteriaStr.append(",");
                }
                else
                {
                    comma = true;
                }
                criteriaStr.append(item.getRecordId());
            }
            criteriaStr.append(")");
        }
        else
        {
            //Assuming that this not necessary when keysToRetrieve is non-null because
            //the keys will already been filtered properly. (???)
            
        	// Add extra where's for system fields for each table in the from clause...
            boolean isRootTbl = true;
            for (Pair<DBTableInfo, String> fromTbl : fromTbls)
            {
                String specialColumnWhere = QueryAdjusterForDomain.getInstance().getSpecialColumns(
                        fromTbl.getFirst(), true,
                        !isRootTbl && true/* XXX should only use left join when necessary */, fromTbl.getSecond());
                isRootTbl = false;
                if (StringUtils.isNotEmpty(specialColumnWhere))
                {
                    if (criteriaStr.length() > 0)
                    {
                        criteriaStr.append(" AND ");
                    }
                    criteriaStr.append(specialColumnWhere);
                }
                //Actually, assuming data is valid, it should only be necessary to add the Adjustments for the root table?
                //XXX if this works, fix this loop. Also, join parameter code in getSpecialColumns will probably be irrelevant.
                break;
            }
            //...done adding system whereses
            
            //get only records modified/added since last export of the schema...
            if (isSchemaExport && lastExportTime != null)
            {
                if (criteriaStr.length() > 0)
                {
                    criteriaStr.append(" AND (");
                }
                String timestampParam = "spparam" + paramsToSet.size();
                paramsToSet.add(new Pair<String, Object>(timestampParam, lastExportTime));
                criteriaStr.append(getTimestampWhere(fromTbls, timestampParam, lastExportTime));
                criteriaStr.append(") ");
            }
        }
        
        if (criteriaStr.length() > 0)
        {
            sqlStr.append(" where ");
            sqlStr.append(criteriaStr);
        }

        if (sortElements.size() > 0 && !postSortPresent)
        {
            for (SortElement se : sortElements)
            {
                if (!StringUtils.isEmpty(orderStr.toString()))
                {
                    orderStr.append(", ");
                }
                orderStr.append(distinct ? se.getColumn()+1 : se.getColumn()+2);
                if (se.getDirection() == SortElement.DESCENDING)
                {
                    orderStr.append(" DESC");
                }
            }
            sortElements.clear();
        }
            
        if (orderStr.length() > 0)
        {
            sqlStr.append(" order by ");
            sqlStr.append(orderStr);
        }

        if (debug)
        {
            log.debug(sqlStr.toString());
            log.debug("sort:");
            for (SortElement s : sortElements)
            {
                log.debug("  " + s.getColumn() + " - " + s.getDirection());
            }
        }
        
        String result = sqlStr.toString();
        if (!checkHQL(result)) return null;
        
        log.info(result);
        return new HQLSpecs(result, paramsToSet, sortElements);
    }
  
    /**
     * @param hql
     * @return
     */
    protected static boolean checkHQL(String hql)
	{
		DataProviderSessionIFace session = DataProviderFactory.getInstance()
				.createSession();
		try
		{
			try
			{
				session.createQuery(hql, false);
				return true;
			} catch (Exception ex)
			{
				log.error(ex);
                UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryBldrPane.class, ex);				
				return false;
			}
		} finally
		{
			session.close();
		}
	}
    
    /**
     * @param hql
     * @return true if each record in the query defined by hql has
     * a unique key.
     * 
     * NOTE: for large databases this method might take a long time.
     * It probably should be called from a SwingWorker. 
     */
    public static boolean checkUniqueRecIds(final String hql)
    {
        //Assumes the ID field is selected by hql -
    	//Assumes that  'select' is lower case.
    	int fromStart = hql.toLowerCase().indexOf(" from ");
    	int idEnd = hql.indexOf(',', 0); 
        String fldPart = hql.substring(0, idEnd);
        if (fldPart.indexOf("distinct") != -1)
        {
        	fldPart = fldPart.replaceFirst("select distinct ", "select ");
        }
        String countHql = fldPart + " " + hql.substring(fromStart);
        String distinctFldPart = fldPart.replaceFirst("select ", "select distinct ");
        String distinctHql = distinctFldPart + " " + hql.substring(fromStart);
    	DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
        	try
        	{
            	QueryIFace q1 = session.createQuery(countHql, false);
        		QueryIFace q2 = session.createQuery(distinctHql, false);
        		return q1.list().size() == q2.list().size();
        	} catch (Exception ex) 
        	{
                UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryBldrPane.class, ex);
        	}
        } finally
        {
        	session.close();
        }
    	return false;
    }
    /**
     * @param rootAlias
     * @param node
     * @return reasonably likely to be unique alias for table represented by node.
     */
    protected static String getNextAlias(String rootAlias, ProcessNode node)
    {
    	return rootAlias + "_" + DBTableIdMgr.getInstance().getByClassName(node.getRel().getClassName()).getAbbrev();
    }
    
    /**
     * @param rootAlias
     * @param node
     * @param fromTbls
     * @return a from clause for use in adding timestamp conditions for the tables related by the tree rooted by node.
     * The ProcessNode tree is expected to have been created by processFormatter().
     */
    protected static String getTimestampFrom(String rootAlias, ProcessNode node, List<Pair<DBTableInfo,String>> fromTbls)
    {
    	String nextAlias = getNextAlias(rootAlias, node);
    	String result = " left join " + rootAlias + "." + node.getRel().getName() + " " + nextAlias;
    	fromTbls.add(new Pair<DBTableInfo, String>(null, nextAlias));
    	for (ProcessNode kid : node.getKids())
    	{
    		result += getTimestampFrom(nextAlias, kid, fromTbls);
    	}
    	return result;
    }

  
    /**
     * @param fromTbls
     * @param timestampParam
     * @param lastExportTime
     * @return a set of conditions to get records modified or created after lastExportTime for each table in fromTbls. 
     */
    protected static String getTimestampWhere( List<Pair<DBTableInfo, String>> fromTbls, String timestampParam, Timestamp lastExportTime)
    {
        String result = "";
    	int f = 0;
        for (Pair<DBTableInfo, String> fromTbl : fromTbls)
        {
            if (f > 0)
            {
                result += " or ";
            }
            f++;
            result += fromTbl.getSecond() + ".timestampModified > :" + timestampParam;
            result += " or ";
            result += fromTbl.getSecond() + ".timestampCreated > :" + timestampParam;
//            result += " or ";
//            result += fromTbl.getSecond() + ".timestampModified is null";
//            result += " or ";
//            result += fromTbl.getSecond() + ".timestampCreated is null";
        }
        return result;
    }
    
    /**
     * @param formatter
     * 
     * Eventually this will add conditions to check for changed data when exporting a schema.
     */
    protected static void processFormatter(DataObjDataFieldFormatIFace formatter, ProcessNode node)
    {
		for (DataObjDataField fld : formatter.getFields())
		{
			if (fld.getObjFormatter() != null)
			{
				ProcessNode subNode = null;
				for (DataObjDataFieldFormatIFace subformatter : fld.getObjFormatter().getFormatters())
				{
					if (subNode == null)
					{
						subNode = new ProcessNode(fld.getRelInfo());
						node.getKids().add(subNode);
					}
					processFormatter(subformatter, subNode);
				}
			}
		}
    }
    
    /**
     * @param tblAlias
     * @param criteria
     * @return supplied criteria parameter with adjustments to enable synonymy searching.
     */
    protected static String adjustForSynSearch(final String tblAlias, final String criteria, final boolean isNegated)
    {
        String result = "(" + criteria;
        String chunk = criteria.replace(tblAlias + ".", getAcceptedChildrenAlias(tblAlias) + ".");
        if (isNegated)
        {
        	result += " AND " + chunk;
        } else
        {
        	result += " OR " + chunk;
        }
        chunk = criteria.replace(tblAlias + ".", getAcceptedParentAlias(tblAlias) + ".");
        if (isNegated)
        {
        	result += " AND " + chunk;
        } else
        {
        	result += " OR " + chunk;
        }
        chunk = criteria.replace(tblAlias + ".", getAcceptedParentChildrenAlias(tblAlias) + ".");
        if (isNegated)
        {
        	result += " AND " + chunk + ") ";
        } else
        {
        	result += " OR " + chunk + ") ";
        }
        
        return result;
    }
    /**
     * Performs the Search by building the HQL String.
     */
    protected void doSearch(final TableQRI rootTable, boolean distinct)
    {
        try
        {
            //XXX need to determine exportQuery params (probably)
        	HQLSpecs hql = buildHQL(rootTable, distinct, queryFieldItems, tableTree, null, searchSynonymy, false, null);  
            processSQL(queryFieldItems, hql, rootTable.getTableInfo(), distinct);
        }
        catch (Exception ex)
        {
            String msg = StringUtils.isBlank(ex.getLocalizedMessage()) ? getResourceString("QB_RUN_ERROR") : ex.getLocalizedMessage();
            ex.printStackTrace();
        	UIRegistry.getStatusBar().setErrorMessage(msg, ex);
            UIRegistry.writeTimedSimpleGlassPaneMsg(msg, Color.RED);
            runningResults.set(null);
            completedResults.set(null);
        }
    }
    
    /**
     * @return Panel with up and down arrows for moving fields up and down in queryFieldsPanel.
     */
    protected JPanel buildMoverPanel(final boolean horizontal)
    {
        orderUpBtn = createIconBtn("ReorderUp", "QB_FLD_MOVE_UP", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                orderUp();
            }
        });
        orderDwnBtn = createIconBtn("ReorderDown", "QB_FLD_MOVE_DOWN", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                orderDown();
            }
        });
        
        PanelBuilder upDownPanel;
        if (horizontal)
        {
            upDownPanel = new PanelBuilder(new FormLayout("f:p:g, p, 2px, p, f:p:g","p"));        
            CellConstraints cc = new CellConstraints();
            upDownPanel.add(orderUpBtn,       cc.xy(2, 1));
            upDownPanel.add(orderDwnBtn,      cc.xy(4, 1));
        }
        else
        {
            upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
            CellConstraints cc = new CellConstraints();
            upDownPanel.add(orderUpBtn,       cc.xy(1, 2));
            upDownPanel.add(orderDwnBtn,      cc.xy(1, 4));
        }
        return upDownPanel.getPanel();
    }

    /**
     * Moves selected QFP up in queryFieldsPanel
     */
    protected void orderUp()
    {
        moveField(selectedQFP, queryFieldItems.get(queryFieldItems.indexOf(selectedQFP)-1));
    }
    
    /**
     * Moves selected QFP down in queryFieldsPanel
     */
    protected void orderDown()
    {
        moveField(selectedQFP, queryFieldItems.get(queryFieldItems.indexOf(selectedQFP)+1));
    }
    
    
    /**
     * @param fld
     * @return true if criteria have been entered for fld.
     */
    protected static boolean fieldHasCriteria(final FieldQRI fld, List<QueryFieldPanel> fieldPanels)
    {
        for (QueryFieldPanel fldPanel : fieldPanels)
        {
            if (fldPanel.getFieldQRI() != null)
            {
            	if (fldPanel.getFieldQRI() == fld || fldPanel.getFieldQRI().getStringId().equals(fld.getStringId()))
            	{
            		return fldPanel.hasCriteria();
            	}
            }
        }
        return false;
    }
    /**
     * @param node
     * @return " left join " unless it can be determined that data returned on other side of the join
     * cannot be null in which case " inner join " is returned.
     */
    protected static String getJoin(final ProcessNode node)
    {
        //XXX really should only use left join when necessary.
    	//XXX if this is ever modified to use inner join when conditions exists in the related table
    	//the 'allowNulls' setting must be checked and left join used when it is true.
        return " left join ";
    }
    
    /**
     * @param taxAlias
     * @return an alias for the acceptedParent joined table for table with alias taxAlias.
    */
    protected static String getAcceptedParentAlias(final String taxAlias)
    {
        return taxAlias + "accpar";
    }
    
    /**
     * @param taxAlias
     * @return an alias for the acceptedParentChildren joined table for table with alias taxAlias.
    */
    protected static String getAcceptedParentChildrenAlias(final String taxAlias)
    {
        return taxAlias + "accparchi";
    }
    
    /**
     * @param taxAlias
     * @return an alias for the acceptedChildren joined table for table with alias taxAlias.
    */
    protected static String getAcceptedChildrenAlias(final String taxAlias)
    {
        return  taxAlias + "accchi";
    }
    
    /**
     * @param fld
     * @return true if the the field is a name field for a treeable table.
    */
    protected static boolean isSynSearchable(final FieldQRI fld)
    {
        //XXX It would be good to have a way of knowing if synonymy is actually supported for a tree or treeable class.   
    	System.out.println("isSynSearchble " + fld.getTitle());
        if (!Treeable.class.isAssignableFrom(fld.getTableInfo().getClassObj()))
        {
            return false;
        }
        
        SpecifyAppContextMgr spMgr = (SpecifyAppContextMgr )AppContextMgr.getInstance();
        
        @SuppressWarnings("unchecked")
        TreeDefIface<?, ?, ?> treeDef = spMgr.getTreeDefForClass((Class<? extends Treeable<?,?,?>> )fld.getTableInfo().getClassObj());        
        
        if (treeDef.isSynonymySupported())
        {
        	System.out.println(fld.getFieldName() + "  --  " + fld.getClass().getSimpleName());
        	return fld.getFieldName().equalsIgnoreCase("name") || fld.getFieldName().equalsIgnoreCase("fullname") || fld instanceof TreeLevelQRI;
        }
        return false;
    }
    
    /**
     * @param parent
     * @param sqlStr
     * @param level
     * @param searchSynonymy
     * 
     * returns true if Joins on synonymizable tree tables are present.
     */
    protected static boolean processTree(final ProcessNode parent, final StringBuilder sqlStr, final List<Pair<DBTableInfo,String>> fromTbls,
                               final int level, 
                               final TableAbbreviator tableAbbreviator, final TableTree tblTree,
                               List<QueryFieldPanel> fieldPanels,
                               final boolean searchSynonymy,
                               final boolean isSchemaExport,
                               final Timestamp exportTimestamp)
    {
        BaseQRI qri = parent.getQri();
        boolean hqlHasSynJoins = false;
        if (qri != null && qri.getTableTree() != tblTree)
        {
            if (qri instanceof TableQRI)
            {
                TableTree tt = qri.getTableTree();
                String alias = tableAbbreviator.getAbbreviation(tt);
                fromTbls.add(new Pair<DBTableInfo, String>(tt.getTableInfo(), alias));
                if (level == 1)
                {
                    sqlStr.append(tt.getName());
                    sqlStr.append(' ');
                    sqlStr.append(alias);
                    sqlStr.append(' ');

                }
                else
                {
                    sqlStr.append(getJoin(parent));

                    sqlStr.append(tableAbbreviator.getAbbreviation(tt.getParent()));
                    sqlStr.append('.');
                    sqlStr.append(tt.getField());
                    sqlStr.append(' ');
                    sqlStr.append(alias);
                    sqlStr.append(' ');
                    if (searchSynonymy && Treeable.class.isAssignableFrom(((TableQRI )qri).getTableInfo().getClassObj()))
                    {
                        //check to see if Name is inUse and if so, add joins for accepted taxa
                        TableQRI tqri = (TableQRI )qri;
                        boolean addSynJoin = false;
                        for (QueryFieldPanel qfp : fieldPanels)
                        {
                        	if (qfp.getFieldQRI() != null)
                        	{
                        		if (isSynSearchable(qfp.getFieldQRI()) && qfp.hasCriteria())
                        		{
                        			addSynJoin = true;
                        			break;
                        		}
                        	}
                        }
                        if (addSynJoin)
                        {
                            hqlHasSynJoins = true;
                            sqlStr.append("left join ");
                            sqlStr.append(alias + ".acceptedChildren " + getAcceptedChildrenAlias(alias) + " ");
                            sqlStr.append("left join ");
                            sqlStr.append(alias + ".accepted" + tqri.getTableInfo().getShortClassName() + " " 
                                    + getAcceptedParentAlias(alias) + " left join "
                                    + getAcceptedParentAlias(alias) + ".acceptedChildren " + getAcceptedParentChildrenAlias(alias) + " ");
                        }
                    }
                }
                //XXX - should only use left joins when necessary (see 4th param below)
                String extraJoin = QueryAdjusterForDomain.getInstance().getJoinClause(tt.getTableInfo(), true, alias, true);
                
                if (StringUtils.isNotEmpty(extraJoin))
                {
                    sqlStr.append(extraJoin + " ");
                }
            }
            else if (qri instanceof RelQRI && isSchemaExport && exportTimestamp != null)
            {
				RelQRI relQRI = (RelQRI )qri;
				RelationshipType relType = relQRI.getRelationshipInfo().getType();
				DataObjDataFieldFormatIFace formatter = relQRI.getDataObjFormatter();
				if ((!relType.equals(RelationshipType.ManyToOne) && !relType.equals(RelationshipType.ManyToMany))
					|| formatter.getSingleField() == null)
				{
					ProcessNode newNode = new ProcessNode(relQRI);
					if (formatter != null)
					{
						processFormatter(formatter, newNode);
					}
					String rootAlias = tableAbbreviator.getAbbreviation(relQRI.getTableTree().getParent());
					String formFrom = getTimestampFrom(rootAlias, newNode, fromTbls);
					sqlStr.append(" ");
					sqlStr.append(formFrom);
					sqlStr.append(" ");
				}
            }
        }
        for (ProcessNode kid : parent.getKids())
        {
            hqlHasSynJoins |= processTree(kid, sqlStr, fromTbls, level + 1, tableAbbreviator, tblTree, fieldPanels, searchSynonymy, isSchemaExport, exportTimestamp);
        }
        return hqlHasSynJoins;
    }

    
    /**
     * @param fldName
     * @return fldName formatted for use as a field in a JasperReports data source or
     *  JasperReports data connection.
     */
    public static String fixFldNameForJR(final String fldName)
    {
        //not totally sure this is necessary.
        //other transformations may eventually be necessary.
        return fldName.trim().replaceAll(" ", "_");
    }
    
    /**
     * @param fqri
     * @param forSchemaExport
     * @return the formatter for the column displaying fqri's data.
     * Generally the default or user-defined formatter is used, except in special cases for
     * Schema mapping queries.
     */
    protected static UIFieldFormatterIFace getColumnFormatter(final FieldQRI fqri, final boolean forSchemaExport)
    {
    	if (fqri instanceof RelQRI)
    	{
            DataObjDataFieldFormatIFace formatter = ((RelQRI )fqri).getDataObjFormatter();
            if (formatter != null && formatter.getSingleField() != null)
            {
                return fqri.getTableInfo().getFieldByName(formatter.getSingleField()).getFormatter();
            }
            return null;
        }
    	if (forSchemaExport && 
    			(fqri.getDataClass().equals(Calendar.class) || 
    				java.util.Date.class.isAssignableFrom(fqri.getDataClass())))
    	{
    		return new DateExportFormatter();
    	}
    	return fqri.getFormatter();
    }
    /**
     * @param queryFieldItemsArg
     * @param fixLabels
     * @return ERTICaptionInfo for the visible columns returned by a query.
     */
    public static List<ERTICaptionInfoQB> getColumnInfo(final Vector<QueryFieldPanel> queryFieldItemsArg, final boolean fixLabels,
            final DBTableInfo rootTbl, boolean forSchemaExport)
    {
        List<ERTICaptionInfoQB> result = new Vector<ERTICaptionInfoQB>();
        Vector<ERTICaptionInfoTreeLevelGrp> treeGrps = new Vector<ERTICaptionInfoTreeLevelGrp>(5);
        for (QueryFieldPanel qfp : queryFieldItemsArg)
        {
            if (qfp.getFieldQRI() == null)
            {
            	continue;
            }
            
            System.out.println(qfp.getFieldQRI().getFieldName());
            
        	DBFieldInfo fi = qfp.getFieldInfo();
            DBTableInfo ti = null;
            if (fi != null)
            {
               ti = fi.getTableInfo();
            }
            String colName = qfp.getFieldName();
            if (ti != null && fi != null)
            {
                colName = ti.getAbbrev() + '.' + fi.getColumn();
            }
            if (qfp.isForDisplay())
            {
                String lbl = qfp.getSchemaItem() == null ? qfp.getLabel() : qfp.getSchemaItem().getFieldName();
                if (fixLabels)
                {
                    lbl = fixFldNameForJR(lbl);
                }
                ERTICaptionInfoQB erti = null;
                
                //Test to see if it is actually necessary to use a ERTICaptionInfoRel for the field.
                boolean buildRelERTI = false;
                if (qfp.getFieldQRI() instanceof RelQRI)
                {
                    //Test to see if it is actually necessary to use a ERTICaptionInfoRel for the field.
                    RelationshipType relType = ((RelQRI )qfp.getFieldQRI()).getRelationshipInfo().getType();
                    //XXX Formatter.getSingleField() checks for ZeroOrOne and OneToOne rels.
                    if (relType != RelationshipType.ManyToOne /*&& relType != RelationshipType.ZeroOrOne && relType != RelationshipType.OneToOne*/)
                    {
                        buildRelERTI = true;
                    }
                    else
                    {
                        DataObjDataFieldFormatIFace formatter = ((RelQRI )qfp.getFieldQRI()).getDataObjFormatter();
                        if (formatter != null)
                        {
                            buildRelERTI = formatter.getSingleField() == null;
                        }
                        else
                        {
                            buildRelERTI = true;
                        }
                    }
                }
                
                if (buildRelERTI)
                {
                    RelQRI rqri = (RelQRI )qfp.getFieldQRI();
                    RelationshipType relType = rqri.getRelationshipInfo().getType();
                    boolean useCache;
                    if (relType == RelationshipType.ManyToOne || relType == RelationshipType.ManyToMany)
                    {
                        useCache = true;
                    }
                    else 
                    {
                        //XXX actually need to be sure that this rel's table has a many-to-one relationship (direct or indirect) to the root.
                        useCache = rootTbl != null && rootTbl.getTableId() != rqri.getTableInfo().getTableId();
                    }
                    erti = new ERTICaptionInfoRel(colName, lbl, true, qfp.getFieldQRI().getFormatter(), 0,
                            qfp.getStringId(),
                            ((RelQRI)qfp.getFieldQRI()).getRelationshipInfo(),
                            useCache,
                            null);
                }
                else if (qfp.getFieldQRI() instanceof TreeLevelQRI)
                {
                    TreeLevelQRI tqri = (TreeLevelQRI )qfp.getFieldQRI();
                    for (ERTICaptionInfoTreeLevelGrp tg : treeGrps)
                    {
                        erti = tg.addRank((TreeLevelQRI )qfp.getFieldQRI(), colName, lbl, qfp.getStringId(), tqri.getRealFieldName());
                        if (erti != null)
                        {
                            break;
                        }
                    }
                    if (erti == null)
                    {
                        ERTICaptionInfoTreeLevelGrp newTg = new ERTICaptionInfoTreeLevelGrp(tqri.getTreeDataClass(), 
                                tqri.getTreeDefId(), tqri.getTableAlias(), true, null);
                        erti = newTg.addRank(tqri, colName, lbl, qfp.getStringId(), tqri.getRealFieldName());
                        treeGrps.add(newTg);
                    }
                }
                else
                {
                	erti = new ERTICaptionInfoQB(colName, lbl, true, getColumnFormatter(qfp.getFieldQRI(), forSchemaExport), 0, qfp.getStringId(), qfp.getPickList(), fi);
                }
                erti.setColClass(qfp.getFieldQRI().getDataClass());
                if (!forSchemaExport && 
                		qfp.getFieldInfo() != null && !(qfp.getFieldQRI() instanceof DateAccessorQRI) && qfp.getFieldQRI().getFieldInfo().isPartialDate())
                {
                    String precName = qfp.getFieldQRI().getFieldInfo().getDatePrecisionName();
                    
                    Vector<ColInfo> colInfoList = new Vector<ColInfo>();
                    ColInfo columnInfo = erti.new ColInfo(StringUtils.capitalize(precName), precName);
                    columnInfo.setPosition(0);
                    colInfoList.add(columnInfo);
                    
                    columnInfo = erti.new ColInfo(qfp.getFieldQRI().getFieldInfo().getColumn(), qfp.getFieldQRI().getFieldInfo().getName());
                    columnInfo.setPosition(1);
                    colInfoList.add(columnInfo);
                    erti.setColInfoList(colInfoList);
                    erti.setColName(null);
                    // XXX We need to get this from the SchemaConfig
                    erti.setUiFieldFormatter(UIFieldFormatterMgr.getInstance().getFormatter("PartialDate"));
                }
                result.add(erti);
            }
        }
        for (ERTICaptionInfoTreeLevelGrp tg : treeGrps)
        {
            try 
            {
            	tg.setUp();
            } catch (SQLException ex)
            {
                UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryBldrPane.class, ex);
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        return result;
    }
    
    /**
     * @param queryName
     * @param fixLabels
     * @return ERTICaptionInfo for the visible columns returned by query queryName.
     */
    public static List<ERTICaptionInfo> getColumnInfo(final String queryName, final boolean fixLabels)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance()
        .createSession();
        try
        {
            SpQuery fndQuery = session.getData(SpQuery.class, "name", queryName,
                    DataProviderSessionIFace.CompareType.Equals);
            if (fndQuery == null)
            {
                throw new Exception("Unable to load query " + queryName);
            }
            return getColumnInfoSp(fndQuery.getFields(), fixLabels);
        }
        catch (Exception e)
        {
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryBldrPane.class, e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally
        {
            session.close();
        }
    }
    
    /**
     * @param qf
     * @param fi
     * 
     * @return the data class for the result column defined by qf and fi.
     */
    protected static Class<?> getDataClass(final SpQueryField qf, final DBFieldInfo fi)
    {
    	if (Calendar.class.isAssignableFrom(fi.getDataClass()))
    	{
    		//sleazy way to see if qf is a DatePartAccessor
    		String idString = qf.getStringId();
    		if (idString.endsWith(fi.getName() + DateAccessorQRI.DATEPART.NumericDay.toString())
    				|| idString.endsWith(fi.getName() + DateAccessorQRI.DATEPART.NumericMonth.toString())
    				|| idString.endsWith(fi.getName() + DateAccessorQRI.DATEPART.NumericYear.toString()))
    		{
    			return Integer.class;
    		}
    	}
    	return fi.getDataClass();
    }
    
    /**
     * @param queryFields
     * @param fixLabels
     * @return ERTICaptionInfo for the visible columns represented in an SpQuery's queryFields.
     * 
     * This method is used by QBDataSourceConnection object which current do not actually need to
     * retrieve any data, so ERTICaptionInfoQB objects are used for all columns.
     */
    public static List<ERTICaptionInfo> getColumnInfoSp(final Set<SpQueryField> queryFields, final boolean fixLabels)
    {
        List<ERTICaptionInfo> result = new Vector<ERTICaptionInfo>();
        for (SpQueryField qf : queryFields)
        {
            if (qf.getContextTableIdent() != null)
            {
                DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(qf.getContextTableIdent());
                if (!AppContextMgr.isSecurityOn() || ti.getPermissions().canView())
                {
                    DBFieldInfo fi = ti.getFieldByColumnName(qf.getFieldName());
                    String colName = ti.getAbbrev() + '.' + qf.getFieldName();
                    if (qf.getIsDisplay())
                    {
                        String lbl = qf.getColumnAliasTitle();
                        if (fixLabels)
                        {
                            lbl = lbl.replaceAll(" ", "_");
                            lbl = lbl.replaceAll("/", "_");
                            lbl = lbl.replaceAll("#", "_");
                        }
                        ERTICaptionInfo erti;
                        if (fi != null)
                        {
                            erti = new ERTICaptionInfoQB(colName, lbl, true, fi.getFormatter(), 0, qf.getStringId(), RecordTypeCodeBuilder.getTypeCode(fi), fi);
                            erti.setColClass(getDataClass(qf, fi));
                        }
                        else
                        {
                            erti = new ERTICaptionInfoQB(colName, lbl, true, null, 0, qf.getStringId(), null, fi);
                            erti.setColClass(String.class);
                        }
                        result.add(erti);
                    }
                }
            }
            else log.error("null contextTableIdent for " + qf.getFieldName());
        }
        return result;
    }

    /**
     * @return list reports that use this.query as a data source.
     */
    protected List<SpReport> getReports()
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            //currently experiencing hibernate weirdness with loading of query's reports set so...
            //Object id = query.getId();
            return session.getDataList(SpReport.class, "query", query);
        }
        finally
        {
            session.close();
        }
    }
    
    
    /**
     * @param report
     * 
     * Loads and runs the query that acts as data source for report. Then runs report.
     */
    public static void runReport(final SpReport report, final String title, final RecordSetIFace rs)
    {
        //XXX This is now also used to run Workbench reports. Really should extract the general stuff out
    	//to a higher level...
    	boolean isQueryBuilderRep = report.getReportObject() instanceof SpQuery;
        if (isQueryBuilderRep)
        {
        	UsageTracker.incrUsageCount("QB.RunReport." + report.getQuery().getContextName());
        }
        else
        {
        	UsageTracker.incrUsageCount("WB.RunReport");
        }
        TableTree tblTree = null;
        Hashtable<String, TableTree> ttHash = null;
        QueryParameterPanel qpp = null;
        if (isQueryBuilderRep)
        {
        	UsageTracker.incrUsageCount("QB.RunReport." + report.getQuery().getContextName());
        	QueryTask qt = (QueryTask )ContextMgr.getTaskByClass(QueryTask.class);
        	if (qt != null)
        	{
        		Pair<TableTree, Hashtable<String, TableTree>> trees = qt.getTableTrees();
        		tblTree = trees.getFirst();
        		ttHash = trees.getSecond();
        	}
        	else
        	{
        		log.error("Could not find the Query task when running report " + report.getName());
        		//blow up
        		throw new RuntimeException("Could not find the Query task when running report " + report.getName());
        	}
            qpp = new QueryParameterPanel();
            qpp.setQuery(report.getQuery(), tblTree, ttHash);
        }
        boolean go = true;
        try
        {
            JasperCompilerRunnable jcr = new JasperCompilerRunnable(null, report.getName(), null);
            jcr.findFiles();
            if (jcr.isCompileRequired())
            {
                jcr.get();
            }
            //if isCompileRequired() is still true, then an error probably occurred compiling the report.
            JasperReport jr = !jcr.isCompileRequired() ? (JasperReport) JRLoader.loadObject(jcr.getCompiledFile()) : null;
            ReportParametersPanel rpp = jr != null ? new ReportParametersPanel(jr, true) : null;
            JRDataSource src = null;
            if (rs == null && ((qpp != null && qpp.getHasPrompts()) || (rpp != null && rpp.getParamCount() > 0)))
            {
                Component pane = null;
                if (qpp != null && qpp.getHasPrompts() && rpp != null && rpp.getParamCount() > 0)
                {
                    pane = new JTabbedPane();
                    ((JTabbedPane) pane).addTab(UIRegistry
                            .getResourceString("QB_REP_RUN_CRITERIA_TAB_TITLE"), new JScrollPane(qpp,
                                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));

                    ((JTabbedPane) pane).addTab(UIRegistry
                            .getResourceString("QB_REP_RUN_PARAM_TAB_TITLE"), new JScrollPane(rpp,
                                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
                }
                else if (qpp != null && qpp.getHasPrompts())
                {
                    pane = new JScrollPane(qpp,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                }
                else
                {
                    pane = new JScrollPane(rpp,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                }
                CustomDialog cd = new CustomDialog((Frame) UIRegistry.getTopWindow(), UIRegistry
                        .getResourceString("QB_GET_REPORT_CONTENTS_TITLE"), true, CustomDialog.OKCANCELHELP,
                        pane);
                cd.setHelpContext("RepRunSettings");
                cd.createUI();
                Dimension ps = cd.getPreferredSize();
                ps.setSize(ps.getWidth()*1.3, ps.getHeight());
                cd.setSize(ps);
                UIHelper.centerAndShow(cd);
                go = !cd.isCancelled();
                cd.dispose();
            }
            if (go)
            {
                if (isQueryBuilderRep)
                {
                	TableQRI rootQRI = null;
                	int cId = report.getQuery().getContextTableId();
                	for (TableTree tt : ttHash.values())
                	{
                		if (cId == tt.getTableInfo().getTableId())
                		{
                			rootQRI = tt.getTableQRI();
                			break;
                		}
                	}
                	Vector<QueryFieldPanel> qfps = new Vector<QueryFieldPanel>(qpp.getFields());
                	for (int f = 0; f < qpp.getFields(); f++)
                	{
                		qfps.add(qpp.getField(f));
                	}

                	HQLSpecs sql = null;

                	// XXX need to allow modification of SelectDistinct(etc) ???
                	//boolean includeRecordIds = true;
                	boolean includeRecordIds = !report.getQuery().isSelectDistinct();

                	try
                	{
                		//XXX Is it safe to assume that query is not an export query? 
                		sql = QueryBldrPane.buildHQL(rootQRI, !includeRecordIds, qfps, tblTree, rs, 
                    		report.getQuery().getSearchSynonymy() == null ? false : report.getQuery().getSearchSynonymy(),
                    				false, null);
                	}
                	catch (Exception ex)
                	{
                        String msg = StringUtils.isBlank(ex.getLocalizedMessage()) ? getResourceString("QB_RUN_ERROR") : ex.getLocalizedMessage();
                        UIRegistry.getStatusBar().setErrorMessage(msg, ex);
                        UIRegistry.writeTimedSimpleGlassPaneMsg(msg, Color.RED);
                		return;
                	}
                
                	src = new QBDataSource(sql.getHql(), sql.getArgs(), sql
                        .getSortElements(), getColumnInfo(qfps, true, rootQRI.getTableInfo(), false),
                        includeRecordIds, report.getRepeats());
                	((QBDataSource )src).startDataAcquisition();
                }
                else 
                {
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    try
                    {
                		boolean loadedWB = false;
                		if (rs != null && rs.getOnlyItem() != null)
                		{
                			Workbench wb = session.get(Workbench.class, rs.getOnlyItem().getRecordId());
                			if (wb != null)
                			{
                				wb.forceLoad();
                				src = new WorkbenchJRDataSource(wb, true);
                				loadedWB = true;
                			}
                		}
                		if (!loadedWB)
                		{
                			UIRegistry.displayErrorDlgLocalized("QueryBldrPane.WB_LOAD_ERROR_FOR_REPORT", 
                					rs != null ? rs.getName() : "[" + UIRegistry.getResourceString("NONE") + "]");
                			return;
                		}
                    }
                    finally
                    {
                    	session.close();
                    }
                }
                
                final CommandAction cmd = new CommandAction(ReportsBaseTask.REPORTS,
                        ReportsBaseTask.PRINT_REPORT, src);
                cmd.setProperty("title", title);
                cmd.setProperty("file", report.getName());
                if (rs == null)
                {
                	cmd.setProperty("skip-parameter-prompt", "true");
                }
                //if isCompileRequired is true then an error probably occurred while compiling,
                //and, if so, it will be caught again and reported in the report results pane.
                if (!jcr.isCompileRequired())
                {
                    cmd.setProperty("compiled-file", jcr.getCompiledFile());
                }
                if (rpp != null && rpp.getParamCount() > 0)
                {
                    StringBuilder params = new StringBuilder();
                    for (int p = 0; p < rpp.getParamCount(); p++)
                    {
                        Pair<String, String> param = rpp.getParam(p);
                        if (StringUtils.isNotBlank(param.getSecond()))
                        {
                            params.append(param.getFirst());
                            params.append("=");
                            params.append(param.getSecond());
                            params.append(";");
                        }
                        cmd.setProperty("params", params.toString());
                    }
                }
                CommandDispatcher.dispatch(cmd);
            }
        }
        catch (JRException ex)
        {
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryBldrPane.class, ex);
            log.error(ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * called when retrieved results have been displayed.
     */
    public void resultsComplete()
    {
        //debug
    	//System.out.println((System.nanoTime() - startTime.get()) / 1000000000L);
    	
    	completedResults.set(runningResults.get());
        runningResults.set(null);
        if (completedResults.get() != null && !completedResults.get().getCancelled())
		{
			int results = completedResults.get().getQuery().getDataObjects()
					.size();
			if (results > completedResults.get().getMaxTableRows()
					&& !countOnly
					&& !completedResults.get().getQuery().isCancelled())
			{
				if (schemaMapping == null)
				{
					UIRegistry.displayInfoMsgDlgLocalized(
							"QB_PARTIAL_RESULTS_DISPLAY", completedResults
									.get().getMaxTableRows(), results);
				} else
				{
					UIRegistry.displayInfoMsgDlgLocalized("QB_PREVIEW_DISPLAY",
							completedResults.get().getMaxTableRows(), results);
				}
			} else
			{
				if (schemaMapping != null)
				{
					UIRegistry
							.displayInfoMsgDlgLocalized("QB_PREVIEW_DISPLAY_TINY");
				}
			}
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                String searchLbl = schemaMapping == null ? getResourceString("QB_SEARCH") : getResourceString("QB_EXPORT_PREVIEW");
                QueryBldrPane.this.searchBtn.setText(searchLbl); 
                UIRegistry.getStatusBar().setProgressDone(query.getName());
                UIRegistry.displayStatusBarText("");
            }
        });
    }
    
    /**
     * @return countOnly
     */
    public boolean isCountOnly()
    {
        return countOnly;
    }
    
    /**
     * Called when the db record retrieval task has completed.
     */
    public boolean queryTaskDone()
    {
        boolean result = true;
        doneTime.set(System.nanoTime());
        if (runningResults.get() != null && runningResults.get().getQuery() != null)
        {
            if (!countOnly && !runningResults.get().getQuery().isCancelled() && !runningResults.get().getQuery().isInError())
            {
                final int results = runningResults.get().getQuery().getDataObjects().size();
                
                String msg = "";
                if (results <= runningResults.get().getMaxTableRows())
                {
                    msg = String.format(UIRegistry
                        .getResourceString("QB_DISPLAYING_RETRIEVED_RESULTS"), String
                        .valueOf(results), String.format("%04.2f", 
                        		(doneTime.get() - startTime.get()) / 1000000000D));
                }
                else if (!runningResults.get().isPostSorted())
                {
                    msg = String.format(UIRegistry.getResourceString("QB_DISPLAYING_RETRIEVED_RESULTS_PARTIAL"), 
                            String.valueOf(results), 
                            String.format("%04.2f", (doneTime.get() - startTime.get()) / 1000000000D),
                            String.valueOf(runningResults.get().getMaxTableRows()));
                }
                else
                {
                    result = false;
                }
                if (result)
                {
                    UIRegistry.displayStatusBarText(msg);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {
                            //UIRegistry.getStatusBar().setIndeterminate(query.getName(), true);
                            if (query != null && runningResults != null && runningResults.get() != null)
                            {
                                UIRegistry.getStatusBar().setProgressRange(query.getName(), 0, 
                                        Math.min(results, runningResults.get().getMaxTableRows()));
                            }
                        }
                    });
                }
            }
            if (!result || countOnly || runningResults.get().getQuery().isCancelled() || runningResults.get().getQuery().isInError())
            {
                
                UIRegistry.displayStatusBarText("");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        String searchLbl = schemaMapping == null ? getResourceString("QB_SEARCH") : getResourceString("QB_EXPORT_PREVIEW");
                        QueryBldrPane.this.searchBtn.setText(searchLbl); 
                        UIRegistry.getStatusBar().setProgressDone(query.getName());
                        
                    }
                });
                result = false;
            }
            if (!result || (countOnly && !runningResults.get().getQuery().isCancelled() && !runningResults.get().getQuery().isInError()))
            {
                if (countOnly)
                {
                    UIRegistry.showLocalizedMsg("QB_COUNT_TITLE", "QB_COUNT_MSG", runningResults.get().getQuery().getDataObjects().size());
                }
                else if (runningResults.get().isPostSorted())
                {
                    //this should be the case where more records were returned than can be post-sorted...
                	
                	PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, f:p:g, 5dlu", "5dlu, f:p:g, 2dlu, f:p:g, 2dlu, f:p:g, 5dlu"));
                    pb.add(new JLabel(String.format(UIRegistry.getResourceString("QB_CANT_DISPLAY_MSG1"), runningResults.get().getQuery().getDataObjects().size())), new CellConstraints().xy(2, 2));
                    pb.add(new JLabel(String.format(UIRegistry.getResourceString("QB_CANT_DISPLAY_MSG2"), runningResults.get().getMaxTableRows())), new CellConstraints().xy(2, 4));
                    pb.add(new JLabel(UIRegistry.getResourceString("QB_CANT_DISPLAY_MSG3")), new CellConstraints().xy(2, 6));
                    
                    CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(),
                            UIRegistry.getResourceString("QB_CANT_DISPLAY_TITLE"),
                            true,
                            CustomDialog.OKHELP,
                            pb.getPanel());
                    dlg.setHelpContext("QBTooManyRecordsForSort");
                    UIHelper.centerAndShow(dlg);
                    dlg.dispose();
                }
            }
        }
        return result;
    }
    
    /**
     * @param queryFieldItemsArg
     * @param sql
     * @param params
     * @param rootTable
     * @param distinct
     */
    @SuppressWarnings("unchecked")
    protected void processSQL(final Vector<QueryFieldPanel>    queryFieldItemsArg, 
                              final HQLSpecs                   hqlSpecs,
                              final DBTableInfo                rootTable, 
                              final boolean                    distinct)
    {
        List<? extends ERTICaptionInfo> captions = getColumnInfo(queryFieldItemsArg, false, rootTable, false);
        
        String iconName = distinct ? "BlankIcon" : rootTable.getClassObj().getSimpleName();
        int tblId = distinct ? -1 : rootTable.getTableId();
        final QBQueryForIdResultsHQL qri = new QBQueryForIdResultsHQL(TITLEBAR_COLOR,
                getResourceString("QB_SEARCH_RESULTS"),
                iconName,
                tblId,
                this);
        
        qri.setSQL(hqlSpecs.getHql());
        qri.setParams(hqlSpecs.getArgs());
        qri.setSort(hqlSpecs.getSortElements());
        // XXX check generics reference book. (unchecked conversion here)
        qri.setCaptions((List<ERTICaptionInfo> )captions);
        qri.setExpanded(true);
        qri.setHasIds(!distinct);
        boolean filterDups = false;
        if (distinct)
        {
        	for (ERTICaptionInfo caption : captions)
        	{
        		if (caption instanceof ERTICaptionInfoTreeLevel)
        		{
        			filterDups = true;
        			break;
        		}
        		else if (caption instanceof ERTICaptionInfoRel)
        		{
        			RelationshipType relType = ((ERTICaptionInfoRel)caption).getRelationship().getType();
        			if (relType.equals(RelationshipType.OneToMany) || relType.equals(RelationshipType.ManyToMany))
        			{
        				filterDups = true;
        				break;
        			}
        		}
        	}
        }
        qri.setFilterDups(filterDups);
        if (schemaMapping != null)
        {
        	qri.setMaxTableRows(ExportSchemaPreviewSize);
        }
        runningResults.set(qri);
        doneTime.set(-1);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                if (runningResults.get() != null && !runningResults.get().getCancelled())
                {
                    UIRegistry.getStatusBar().setText(UIRegistry.getResourceString("QB_SEARCHING"));
                    searchBtn.setText(UIRegistry.getResourceString("QB_CANCEL")); 
                    UIRegistry.getStatusBar().setIndeterminate(query.getName(), true);
                }
              //else the query got cancelled or crashed before this thread was executed
            }
        });
        
        new SwingWorker()
        {
            @Override
            public Object construct()
            {
            	if (schemaMapping != null)
            	{
            		if (!checkUniqueRecIds(hqlSpecs.getHql()))
            		{
            			SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run()
							{
		            			UIRegistry.displayErrorDlg(UIRegistry.getResourceString("ExportPanel.DUPLICATE_KEYS_EXPORT"));
							}
            			});
            			runningResults.set(null);
            			resultsComplete();
            			return null;
            		}
            	}
                if (esrp == null)
                {
                    CommandAction cmdAction = new CommandAction("Express_Search", "HQL", qri);
                    cmdAction.setProperty("reuse_panel", true); 
                    CommandDispatcher.dispatch(cmdAction);
                } else
                {
                    esrp.addSearchResults(qri);
                }
                return null;
            }
        }.start();
        startTime.set(System.nanoTime());
    }

    /**
     * @param pn
     * @param lvl
     */
    protected static void printTree(ProcessNode pn, int lvl)
    {
        for (int i = 0; i < lvl; i++)
        {
            log.debug(" ");
            System.out.print(" ");
        }
        log.debug(pn);
        System.out.println(pn);
        for (ProcessNode kid : pn.getKids())
        {
            printTree(kid, lvl + 1);
        }
    }
    
    /**
     * @return a clone of the current query object.
     */
    protected SpQuery cloneTheQuery()
    {
        SpQuery result = new SpQuery();
        result.initialize();
        result.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
        result.setName(query.getName());
        result.setContextTableId(query.getContextTableId());
        result.setContextName(query.getContextName());
        //result.setCreatedByAgent(query.getCreatedByAgent());
        for (QueryFieldPanel qfp : queryFieldItems)
        {
            SpQueryField qf = qfp.getQueryField();
            
            if (qf != null) 
            {
            	SpQueryField newQf = new SpQueryField();
            	newQf.initialize();
            	newQf.setFieldName(qf.getFieldName());
            	newQf.setPosition(qf.getPosition());
            	qfp.updateQueryField(newQf);
            	result.addReference(newQf, "fields");
            }
        }
        return result;
    }

    /**
     * @return an un-used name based on the schema name and version.
     * 
     * 
     * NOTE: sets query.name AND schemaMapping.mappingName
     */
    protected boolean getExportMappingQueryName()
    {
    	//not worrying about multi-user issues
    	String baseName = exportSchema.getSchemaName() + exportSchema.getSchemaVersion();
    	String result = baseName;
    	int cnt = BasicSQLUtils.getCount("select count(*) from spquery where name = '" + result + "'");
    	int suffix = 2;
    	while (cnt > 0)
    	{
    		result = baseName + "_" + suffix; 
    		cnt = BasicSQLUtils.getCount("select count(*) from spquery where name = '" + result + "'");
    		suffix++;
    	}
    	query.setName(result);
    	schemaMapping.setMappingName(result);
    	return true;
    }
    
    /**
     * @param saveAs
     * @return
     */
    protected boolean saveQuery(final boolean saveAs)
    {
     	boolean result = false;
     	
    	if (exportSchema == null)
		{
			if (!query.isNamed() || saveAs)
			{
				if (!getQueryNameFromUser(saveAs))
				{
					return false;
				}
			}
		} else
		{
			if (!query.isNamed() && !getExportMappingQueryName())
			{
				return false;
			}
		}
        
        UsageTracker.incrUsageCount("QB.SaveQuery." + query.getContextName());
        
        //This is necessary to indicate that a query has been changed when only field deletes have occurred.
        //If the query's timestampModified is not modified the schema export tool doesn't know the 
        //export schema needs to be rebuilt.
        if (!saveAs && query.getId() != null)
        {
        	int origCount = BasicSQLUtils.getCountAsInt("select count(*) from spqueryfield where spqueryid=" + query.getId());
        	if (origCount > query.getFields().size())
        	{
        		query.setTimestampModified(new Timestamp(System.currentTimeMillis()));
        	}
        }
        
        TableQRI tableQRI = (TableQRI) tableList.getSelectedValue();
        if (tableQRI != null)
        {
            short position = 0;
            
            Set<Integer> queryFldsWithoutPanels = new HashSet<Integer>();
            for (SpQueryField qf : query.getFields())
            {
            	queryFldsWithoutPanels.add(qf.getId());
            }
            
            for (QueryFieldPanel qfp : queryFieldItems)
            {
                if (qfp.getQueryField() != null)
                {
                	SpQueryField qf = qfp.getQueryField();
                	queryFldsWithoutPanels.remove(qf.getId());
                	qf.setPosition(position);
                	qfp.updateQueryField();

                	position++;
                }
            }

            //Remove query fields for which panels could be created in order to prevent
            //repeat of missing fld message in getQueryFieldPanels() whenever this query is loaded.
            for (Integer qfId : queryFldsWithoutPanels)
            {
            	//this is real lame but should hardly ever need to be executed
            	for (SpQueryField qf : query.getFields())
            	{
            		if (qfId.equals(qf.getId()))
            		{
            			query.getFields().remove(qf);
            			break;
            		}
            	}
            }
            
            if (query.getSpQueryId() == null || saveAs)
            {
                if (query.getSpQueryId() != null && saveAs)
                {
                    query = cloneTheQuery();
                    queryNavBtn.setEnabled(true);
                }
                
                queryNavBtn = ((QueryTask) task).saveNewQuery(query, schemaMapping, false); // false tells it to
                                                                                // disable the
                                                                                // navbtn

                query.setNamed(true); //XXX this isn't getting persisted!!!!!!!!!
                if (query.getSpQueryId() != null && saveAs)
                {
                    this.setupUI();
                }   
                
                SubPaneMgr.getInstance().renamePane(this, query.getName());
                
                return true;
            }
            
            
            if (schemaMapping != null)
            {
            	result =  DataModelObjBase.save(true, query, schemaMapping);
            }
            else
            {
            	result =  DataModelObjBase.save(true, query);
            }
            if (result)
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            	try
            	{
            		query = session.get(SpQuery.class, query.getId());
            		query.forceLoad(true);
            		schemaMapping = query.getMapping();
            		if (schemaMapping != null)
            		{
            			schemaMapping.forceLoad();
            		}
            	}
            	finally
            	{
            		session.close();
            	}
            }
            return result;
        }
        //else
        {
            log.error("No Context selected!");
            return false;
        }
    }

    /**
     * @return true if a valid query name was obtained from user
     */
    protected boolean getQueryNameFromUser(final boolean saveAs)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            String newQueryName = query.getName();
            String oldQueryName = query.getName();
            SpQuery fndQuery = null;
            boolean good = false;
            do
            {
                //if (QueryTask.askUserForInfo("Query", getResourceString("QB_DATASET_INFO"), query))
                //Using above method causes Hibernate 'Dirty Collection' issues for save as.
                //Currently the only info involved is the name so using a simple dialog box is good enuf.
                JTextField nameText = new JTextField(newQueryName);
                JLabel nameLbl = UIHelper.createLabel(UIRegistry.getResourceString("QB_Q_NAME_PROMPT"));
                PanelBuilder pane = new PanelBuilder(new FormLayout("4dlu, p, 2dlu, fill:p:grow, 4dlu", "5dlu, p, 5dlu"));
                CellConstraints cc = new CellConstraints();
                pane.add(nameLbl, cc.xy(2, 2));
                pane.add(nameText, cc.xy(4, 2));
                CustomDialog cd = new CustomDialog((Frame)UIRegistry.getTopWindow(), 
                        saveAs ? UIRegistry.getResourceString("QB_SAVE_Q_AS_TITLE") : UIRegistry.getResourceString("QB_SAVE_Q_TITLE"), 
                        true, CustomDialog.OKCANCELHELP, pane.getPanel());
                cd.setHelpContext("QBSave");
                UIHelper.centerAndShow(cd);
                if (!cd.isCancelled())
                {
                    //newQueryName = query.getName();
                    newQueryName = nameText.getText();
                    if (StringUtils.isNotEmpty(newQueryName) && newQueryName.length() > 64)
                    {
                        UIRegistry.getStatusBar().setErrorMessage(
                                getResourceString("QB_NAME_TOO_LONG"));
                    }
                    else if (StringUtils.isEmpty(newQueryName))
                    {
                        UIRegistry.getStatusBar().setErrorMessage(
                                getResourceString("QB_ENTER_A_NAME"));
                    }
                    else if (!UIHelper.isValidNameForDB(newQueryName))
                    {
                        UIRegistry.displayLocalizedStatusBarError("INVALID_CHARS_NAME");
                        Toolkit.getDefaultToolkit().beep();
                    }
                    else
                    {
                        fndQuery = session.getData(SpQuery.class, "name", newQueryName,
                                DataProviderSessionIFace.CompareType.Equals);
                        if (fndQuery != null)
                        {
                            UIRegistry.getStatusBar().setErrorMessage(
                                    String.format(getResourceString("QB_QUERY_EXISTS"),
                                            newQueryName));
                        }
                        else
                        {
                            good = true;
                            query.setName(newQueryName);
                        }
                    }
                }
                else
                {
                    query.setName(oldQueryName);
                    return false;
                }
            } while (!good);
        }
        catch (Exception ex)
        {
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryBldrPane.class, ex);
            log.error(ex);

        }
        finally
        {
            session.close();
        }
        UIRegistry.getStatusBar().setText("");
        return true;
    }

    /**
     * @param parentTT
     * @param nameArg
     * @return
     */
    protected static TableTree findTableTree(final TableTree parentTT, final String nameArg)
    {
        for (int k=0; k<parentTT.getKids(); k++)
        {
            TableTree tt = parentTT.getKid(k);
            if (tt.getName().equals(nameArg)) { return tt; }
        }
        return null;
    }


    /**
     * @param parentQRI
     * @param tableTree
     * @param model
     */
    protected void createNewList(final TableQRI tblQRI, final DefaultListModel model)
    {

        model.clear();
        if (tblQRI != null)
        {
            Vector<BaseQRI> sortList = new Vector<BaseQRI>();

            for (int f = 0; f < tblQRI.getFields(); f++)
            {
                if (!tblQRI.getField(f).isFieldHidden())
                {
                    sortList.add(tblQRI.getField(f));
                }
            }
            for (int k = 0; k < tblQRI.getTableTree().getKids(); k++)
            {
                boolean addIt;
                TableTree kidK = tblQRI.getTableTree().getKid(k);
                if (kidK.isAlias())
                {
//                	if (!fixAliases(kidK, tableTreeHash))
//                    {
//                        addIt = false;
//                    }
//                    else
//                    {
//                        addIt = tblIsDisplayable(kidK, tableTreeHash.get(kidK.getName())
//                                .getTableInfo());
//                    }
                	addIt = tblIsDisplayable(kidK, tableTreeHash.get(kidK.getName())
                            .getTableInfo()) && fixAliases(kidK, tableTreeHash);
                }
                else
                {
                    addIt = !kidK.getTableInfo().isHidden()
                            && tblIsDisplayable(kidK, kidK.getTableInfo());
                }
                if (addIt)
                {
                    if (kidK.getTableQRI().getRelationship() == null
                            || !kidK.getTableQRI().getRelationship().isHidden())
                    {
                        sortList.add(tblQRI.getTableTree().getKid(k).getTableQRI());
                    }
                }
            }

            Collections.sort(sortList);
            checkFldUsage(tblQRI.getTableTree(), sortList);
            for (QryListRendererIFace qri : sortList)
            {
                model.addElement(qri);
            }
        }
    }

    /**
     * @param tblTree
     * @param flds
     * 
     * Checks and updates isInUse status for fields in a newly created list of search fields.
     */
    protected void checkFldUsage(final TableTree tblTree, final Vector<BaseQRI> flds)
    {
        String treeStr = tblTree.getPathFromRootAsString() + tblTree.getField();
        
        for (QueryFieldPanel qfp : this.queryFieldItems)
        {
            FieldQRI qri = qfp.getFieldQRI();
            if (qri instanceof RelQRI)
            {
                //if (qri.getTable().getTableTree().getParent() == tblTree)
                TableTree qriTT = qri.getTable().getTableTree().getParent();
                String qriTTStr = qriTT.getPathFromRootAsString() + qriTT.getField(); 
                if (qriTTStr.equals(treeStr))
                {
                    for (BaseQRI fld : flds)
                    {
                        if (fld instanceof TableQRI)
                        {
                            if (((TableQRI )fld).getRelationship() == ((RelQRI )qri).getRelationshipInfo())
                            {
                                fld.setIsInUse(true);
                            }
                        }
                    }
                }
            }
            else if (qri != null && ((qri.getTableTree().getPathFromRootAsString() + qri.getTableTree().getField()).equals(treeStr)))
            {
                for (BaseQRI fld : flds)
                {
                    if (fld instanceof FieldQRI)
                    {
                        FieldQRI qri2 = (FieldQRI )fld;
                        if (qri2.getStringId().equals(qri.getStringId()))
                        {
                            qri2.setIsInUse(true);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * @param aliasTbl
     * @param tblInfo
     * @return true if aliasTbl should be displayed in the fields list for the current context.
     */
    protected static boolean tblIsDisplayable(final TableTree aliasTbl, final DBTableInfo tblInfo)
    {
        /*
        if (aliasTbl.isAlias())
        {
            return !isCyclic(aliasTbl, tblInfo.getTableId()) || isCyclicable(aliasTbl, tblInfo);
        }
        //else
        return true;
        */
        
        return !isCyclic(aliasTbl, tblInfo.getTableId()) || isCyclicable(aliasTbl, tblInfo);
        
    }
    
    /**
     * @param alias
     * @param tblId
     * @return true if the specified alias represents a table that is already
     * present in the alias' tabletree.
     */
    protected static boolean isCyclic(final TableTree alias, final int tblId)
    {
        TableTree parent = alias.getParent();
        while (parent != null)
        {
            if (parent.getTableInfo() != null && parent.getTableInfo().getTableId() == tblId)
            {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
    
    /**
     * @param alias
     * @param tblInfo
     * @return true if it is OK for the specified alias to create a cycle.
     */
    protected static boolean isCyclicable(final TableTree alias, final DBTableInfo tblInfo)
    {
        if  (Treeable.class.isAssignableFrom(tblInfo.getClassObj()))
        {
        	if (alias.getField() != null && alias.getField().startsWith("accepted"))
        	{
        		TableTree parent = alias.getParent();
        		int loop = 0;
        		while (parent != null)
        		{
        			if (parent.getTableInfo() != null && parent.getTableInfo().getTableId() == tblInfo.getTableId())
        			{
        				if (parent.getField() != null && parent.getField().startsWith("accepted"))
        				{
        					if(++loop > 1)
        					{
        						return false;
        					}
        				}
        			}
        			else
        			{
        				break;
        			}
        			parent = parent.getParent();
        		}
        	}
        	return true;
        }
        else if (Container.class.isAssignableFrom(tblInfo.getClassObj()))
        {
    		TableTree parent = alias.getParent();
        	if (alias.getField().equals("parent"))
        	{
        		if (parent != null)
        		{
        			//prevent loop back to parent container from expansion of Container.children
        			if (parent.getTableInfo() != null && parent.getTableInfo().getTableId() == tblInfo.getTableId())
        			{
        				if (parent.getField() != null && parent.getField().equals("children"))
        				{
        					return false;
        				}
        			}
        		}
        		int parentCount = 0;
        		while (parent != null && parentCount < maxParentChainLen)
        		{
        			parentCount++;
        			TableTree grandParent = null;
        			if (parent.getTableInfo() != null && parent.getTableInfo().getTableId() == tblInfo.getTableId())
        			{
        				if (parent.getField() != null && parent.getField().equals("parent"))
        				{
        					grandParent = parent.getParent();
        				}
        			}    
        			parent = grandParent;
        		}
        		if (parentCount == maxParentChainLen)
        		{
        			return false;
        		}

        	} else if (alias.getField().equals("children"))
        	{
        		if (parent != null)
        		{
        			//prevent loop back to children container from expansion of Container.parent
        			if (parent.getTableInfo() != null && parent.getTableInfo().getTableId() == tblInfo.getTableId())
        			{
        				if (parent.getField() != null && parent.getField().equals("parent"))
        				{
        					return false;
        				}
        			}
        		}
        		
        	}
        	else if (alias.getField().equals("container"))
        	{
        		if (parent != null)
        		{
        			//prevent loop back to container from expansion of Container.collectionObjects relationship
        			if (parent.getTableInfo() != null && parent.getTableInfo().getTableId() == CollectionObject.getClassTableId())
        			{
        				if (parent.getField() != null && parent.getField().equals("collectionObjects"))
        				{
        					return false;
        				}
        			}
        			
        			//prevent loop back to continer from expansion of Container.collectionObjectKids relationship
        			//Assuming that a container's collectionobject can't be contained in another container. 
        			if (parent.getTableInfo() != null && parent.getTableInfo().getTableId() == CollectionObject.getClassTableId())
        			{
        				if (parent.getField() != null && parent.getField().equals("collectionObjectKids"))
        				{
        					return false;
        				}
        			}
        		}
        		
        	} else if (alias.getField().equals("containerOwner"))
        	{
				// prevent loop back to container from expansion of Container.collectionObjects relationship
				// Assuming that collectionobjects linked by this relationship won't be containers.
				if (parent.getTableInfo() != null
						&& parent.getTableInfo().getTableId() == CollectionObject
								.getClassTableId())
				{
					if (parent.getField() != null
							&& parent.getField().equals("collectionObjects"))
					{
						return false;
					}
				}
				if (parent != null)
        		{
        			//prevent loop back to continer owner from expansion of Container.collectionObjectKids relationship
        			if (parent.getTableInfo() != null && parent.getTableInfo().getTableId() == CollectionObject.getClassTableId())
        			{
        				if (parent.getField() != null && parent.getField().equals("collectionObjectKids"))
        				{
        					return false;
        				}
        			}

        		}
        	}         		
        	return true;
        } else if (CollectionObject.class.isAssignableFrom(tblInfo.getClassObj()))
    	{
    		TableTree parent = alias.getParent();
//    		if (parent != null && parent.getTableInfo().getTableId() == Container.getClassTableId())
//    		{
//    			return true;
//    		}
    		if (parent != null && parent.getTableInfo().getTableId() == CollectionRelationship.getClassTableId())
    		{
    			//prevent looping back to left side when leftSideRels has been opened from parent
    			if (alias.getField().equals("leftSide") && 
    					parent.getField() != null && parent.getField().equals("leftSideRels")) 
    			{
    				return false;
    			}
    			//prevent looping back to right side when rightSideRels has been opened from parent
    			if (alias.getField().equals("rightSide") && 
    					parent.getField() != null && parent.getField().equals("rightSideRels")) 
    			{
    				return false;
    			}
    		}
    		
    		return true;
    	}

        else if (CollectionRelationship.class.isAssignableFrom(tblInfo.getClassObj()))
    	{
    		return true;
    	}
        	
        return false;
            //special conditions... (may be needed. For example for Determination and Taxon, but on the other hand
            //Determination <-> Taxon behavior seems ok for now.
            
            ////assuming isCyclic
            //&& !Taxon.class.isAssignableFrom(tblInfo.getClassObj()) || !isAncestorClass(alias, Determination.class);
    }
    
//    protected boolean isAncestorClass(final TableTree tbl, final Class<?> cls)
//    {
//        TableTree parent = tbl.getParent();
//        while (parent != null)
//        {
//            if (parent.getTableInfo() != null && parent.getTableInfo().getClassObj().equals(cls))
//            {
//                return true;
//            }
//            parent = parent.getParent();
//        }
//        return false;
//    }
    
    
    /**
     * @param parentList
     */
    protected void fillNextList(final JList parentList)
    {
        if (processingLists) { return; }

        processingLists = true;

        final int curInx = listBoxList.indexOf(parentList);
        if (curInx > -1)
        {
            int startSize = listBoxPanel.getComponentCount();
        	for (int i = curInx + 1; i < listBoxList.size(); i++)
            {
                listBoxPanel.remove(spList.get(i));
            }
        	int removed = startSize - listBoxPanel.getComponentCount();
        	for (int i = 0; i < removed; i++)
        	{
        		tableTreeList.remove(tableTreeList.size() - 1);
        	}

        }
        else
        {
            listBoxPanel.removeAll();
            tableTreeList.clear();
        }

        QryListRendererIFace item = (QryListRendererIFace) parentList.getSelectedValue();
        if (item instanceof ExpandableQRI)
        {
            JList newList;
            DefaultListModel model;
            JScrollPane sp;

            if (curInx == listBoxList.size() - 1)
            {
                newList = new JList(model = new DefaultListModel());
                newList.addMouseListener(new MouseAdapter()
                {

                    /* (non-Javadoc)
                     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
                     */
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        if (e.getClickCount() == 2)
                        {
                            if (currentInx != -1)
                            {
                                JList list = (JList)e.getSource();
                                QryListRendererIFace qriFace = (QryListRendererIFace) list.getSelectedValue();
                                if (BaseQRI.class.isAssignableFrom(qriFace.getClass()))
                                {
                                    BaseQRI qri = (BaseQRI) qriFace;
                                    if (qri.isInUse())
                                    {
                                        //remove the field
                                        for (QueryFieldPanel qfp : QueryBldrPane.this.queryFieldItems)
                                        {
                                            FieldQRI fqri = qfp.getFieldQRI();
                                            if (fqri == qri || (fqri instanceof RelQRI && fqri.getTable() == qri))
                                            {
                                        		boolean clearIt = qfp.getSchemaItem() != null;
                                        		QueryBldrPane.this.removeQueryFieldItem(qfp);
                                            	if (clearIt)
                                            	{
                                            		qfp.setField(null, null);
                                            	}
                                                break;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        // add the field
										try
										{
											FieldQRI fieldQRI = buildFieldQRI(qri);
											if (fieldQRI == null)
											{
												throw new Exception(
														"null FieldQRI");
											}
											SpQueryField qf = new SpQueryField();
											qf.initialize();
											qf.setFieldName(fieldQRI
													.getFieldName());
											qf.setStringId(fieldQRI
													.getStringId());
											query.addReference(qf, "fields");
											if (exportSchema == null)
											{
												addQueryFieldItem(fieldQRI, qf,
														false);
											} else
											{
												addNewMapping(fieldQRI, qf,
														selectedQFP);
											}
										} catch (Exception ex)
										{
											log.error(ex);
											UsageTracker
													.incrHandledUsageCount();
											edu.ku.brc.exceptions.ExceptionTracker
													.getInstance()
													.capture(
															QueryBldrPane.class,
															ex);
											return;
										}
                                    }
                                }
                            }
                        }
                    }
                });
                newList.setCellRenderer(qryRenderer);
                listBoxList.add(newList);
                sp = new JScrollPane(newList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                JLabel colHeader = UIHelper.createLabel(item.getTitle());
                colHeader.setHorizontalAlignment(SwingConstants.CENTER);
                colHeader.setBackground(listBoxPanel.getBackground());
                colHeader.setOpaque(true);
                
                sp.setColumnHeaderView(colHeader);
                
                spList.add(sp);
                
                newList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            fillNextList(listBoxList.get(curInx + 1));
                        }
                    }
                });

            }
            else
            {
                newList = listBoxList.get(curInx + 1);
                model = (DefaultListModel) newList.getModel();
                sp = spList.get(curInx + 1);
                JLabel colHeaderLbl = (JLabel)sp.getColumnHeader().getComponent(0);
                if (item instanceof TableQRI)
                {
                    colHeaderLbl.setText(((TableQRI)item).getTitle());
                }
                else
                {
                    colHeaderLbl.setText(getResourceString("QueryBldrPane.QueryFields")); 
                }
            }

            createNewList((TableQRI)item, model);

            listBoxPanel.remove(addBtn);
            listBoxPanel.add(sp);
            tableTreeList.add(((ExpandableQRI )item).getTableTree());
            listBoxPanel.add(addBtn);
            currentInx = -1;

        }
        else
        {
            listBoxPanel.add(addBtn);
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                updateAddBtnState();

                // Is all this really necessary
                listBoxPanel.validate();
                listBoxPanel.repaint();
                scrollPane.validate();
                scrollPane.invalidate();
                scrollPane.doLayout();
                scrollPane.repaint();
                validate();
                invalidate();
                doLayout();
                repaint();
                UIRegistry.forceTopFrameRepaint();
            }
        });

        processingLists = false;
        currentInx = curInx;
        
    }

    
//    /**
//     * @param rel
//     * @param classObj
//     * @return false if rel represents a 'system' relationship.
//     */
//    protected static boolean isRelevantRel(final DBRelationshipInfo rel, final Class<?> classObj)
//    {
//        if (classObj.equals(edu.ku.brc.specify.datamodel.Agent.class))
//        {
//            return rel.getColName() == null ||
//                (!rel.getColName().equalsIgnoreCase("modifiedbyagentid") &&
//                 !rel.getColName().equalsIgnoreCase("createdbyagentid"));
//        }
//        return true;
//    }
    
    /**
     * @param qri
     * @return
     */
    protected static boolean isTablePickList(final TableQRI qri)
    {
    	//PickListDBAdapterIFace pl = PickListDBAdapterFactory.getInstance().create(qri.getTableInfo().getName(), false);
    	//return (pl instanceof PickListTableAdapter);
    	
    	return false;
    	//return qri.getTableInfo().getName().equals("preptype");
    }
    
    /**
     * @param qri
     * @return
     */
    protected static FieldQRI buildFieldQRIForTablePickList(final TableQRI qri)
    {
    	return null;
    }
    /**
     * @param qri
     * @return qri if it is already a FieldQRI, else constructs a RelQRI and returns it.
     */
    protected static FieldQRI buildFieldQRI(final BaseQRI qri) 
    {
        if (qri instanceof FieldQRI) { return (FieldQRI) qri; }
        if (qri instanceof TableQRI)
        {
            if (isTablePickList((TableQRI )qri))
            {
            	System.out.println(((TableQRI )qri).getTableInfo().getName() + " is a table picklist.");
            	return buildFieldQRIForTablePickList((TableQRI )qri);
            	
            } else
            {
            	DBRelationshipInfo relInfo = ((TableQRI)qri).getRelationship();
            	if (relInfo != null)
            	{
            		return new RelQRI((TableQRI) qri, relInfo);
            	}
                throw new RuntimeException(QueryBldrPane.class.getName() + ": unable to determine relationship."
                		+ ((TableQRI )qri).getTableTree().getField() + " <-> " 
            			+ ((TableQRI )qri).getTableTree().getParent().getField());
            }
        }
        return null;
    }

    /**
     * Enables or disables the button used to add fields to the query, depending on the
     * current situation.
     */
    protected void updateAddBtnState()
    {
        if (currentInx != -1)
        {
            BaseQRI qri = (BaseQRI) listBoxList.get(currentInx)
                    .getSelectedValue();
            addBtn.setEnabled(qri != null && !qri.isInUse());
        }
    }

    /**
     * Removes it from the List.
     * 
     * @param qfp QueryFieldPanel to be removed
     */
    public void removeQueryFieldItem(final QueryFieldPanel qfp)
	{
		//refreshQuery();
		if (query.getReports().size() > 0)
		{
			CustomDialog cd = new CustomDialog(
					(Frame) UIRegistry.getTopWindow(),
					UIRegistry.getResourceString("REP_CONFIRM_DELETE_TITLE"),
					true,
					CustomDialog.OKCANCELHELP,
					new QBReportInfoPanel(
							query,
							UIRegistry
									.getResourceString("QB_USED_BY_REP_FLD_DELETE_CONFIRM")));
			cd.setHelpContext("QBFieldRemovedAndReports");
			UIHelper.centerAndShow(cd);
			cd.dispose();
			if (cd.isCancelled())
			{
				return;
			}
		}
		if (qfp.getFieldQRI() != null)
		{
			qfp.getFieldQRI().setIsInUse(false);
		}
		if (qfp.getQueryField() != null)
		{
			//query.removeReference(qfp.getQueryField(), "fields");
			removeFieldFromQuery(qfp.getQueryField());
			if (qfp.getItemMapping() != null)
			{
				removeSchemaItemMapping(qfp.getItemMapping());
			}
		}
		final FieldQRI qfpqri = qfp.getFieldQRI();
		if (exportSchema == null || qfp.isConditionForSchema())
		{
			queryFieldItems.remove(qfp);
			//XXX field label qualification issues for schema maps??
			qualifyFieldLabels(); 
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				if (selectedQFP == qfp)
				{
					selectQFP(null);
				}
				if (exportSchema == null || qfp.isConditionForSchema())
				{
					queryFieldsPanel.getLayout().removeLayoutComponent(qfp);
					queryFieldsPanel.remove(qfp);
				}
				queryFieldsPanel.validate();
				updateAddBtnState();

				// Sorry, but a new context can't be selected if any fields
				// are selected from the current context.
				tableList.setEnabled(queryFieldItems.size() == 0);

				try
				{
					BaseQRI qri = qfpqri instanceof RelQRI ? qfpqri.getTable() : qfpqri;
					//BaseQRI qri = qfp.getFieldQRI(); 
					boolean done = false;
					for (JList lb : listBoxList)
					{
						if (lb.isVisible())
						{
							for (int i = 0; i < ((DefaultListModel) lb
									.getModel()).getSize(); i++)
							{
								BaseQRI qriI = (BaseQRI )((DefaultListModel) lb.getModel()).getElementAt(i);
								if (qriI != null)
								{
									boolean match = qriI == qri;
									if (!match)
									{
										match = buildFieldQRI(qri)
												.getStringId().equals(
														buildFieldQRI(qriI)
																.getStringId());
									}
									if (match)
									{
										qriI.setIsInUse(false);
										lb.repaint();
										done = true;
										break;
									}
								}
							}
						}
						if (done)
						{
							break;
						}
					}
				} catch (Exception ex)
				{
					UsageTracker.incrHandledUsageCount();
					edu.ku.brc.exceptions.ExceptionTracker.getInstance()
							.capture(QueryBldrPane.class, ex);
					log.error(ex);
				}
				queryFieldsPanel.repaint();
				saveBtn
						.setEnabled(QueryBldrPane.this.queryFieldItems.size() > 0
								&& canSave());
				updateSearchBtn();
				UIRegistry.displayStatusBarText(null);
			}
		});
	}

    /**
     * @return path from root treeTable to treeTable for rightmost displayed list.
     */
    protected List<TableTreePathPoint> getCurrentDisplayPath()
    {
    	if (tableTreeList.size() > 0)
    	{
    		return tableTreeList.get(tableTreeList.size() - 1).getPathFromRoot();
    	}
    	return new Vector<TableTreePathPoint>();
    }
    
    /**
     * Updates lists in list panel to display field as the current selection.
     * 
     * @param field
     */
    protected void displayField(final FieldQRI field)
    {
    	List<TableTreePathPoint> displayedPath = getCurrentDisplayPath();
    	List<TableTreePathPoint> fieldPath = field.getTableTree().getPathFromRoot();

    	if (tableTreeList.size() != listBoxPanel.getComponentCount()-1)
        {
    		log.error("tableTreeList and listBoxPanel are out of sync");
        }
    	
    	int p = 0;
    	while (p < displayedPath.size() && p < fieldPath.size())
    	{
    		if (!displayedPath.get(p).equals(fieldPath.get(p)))
    		{
    			break;
    		}
    		p++;
    	}
    	if (!(p == fieldPath.size() && fieldPath.size() == displayedPath.size()))
		{
			if (p == fieldPath.size())
			{
				if (p == 1)
				{
					fillNextList(tableList);
				} else
				{
					fillNextList(listBoxList.get(p - 2));
				}
			} else
			{
				p--;
				while (p < fieldPath.size() - 1)
				{
					JList currList = p < 0 ? tableList : listBoxList.get(p);
					ListModel model = currList.getModel();
					// find and select item in path
					int i = 0;
					boolean foundPathItem = false;
					while (i < model.getSize() && !foundPathItem)
					{
						QryListRendererIFace item = (BaseQRI) model
								.getElementAt(i);
						if (item.hasChildren())
						{
							TableTree tt = ((BaseQRI) item).getTableTree();
							if (fieldPath.get(p + 1).equals(
									new TableTreePathPoint(tt)))
							{
								currList.setSelectedIndex(i);
								foundPathItem = true;
							}
						}
						i++;
					}
					if (foundPathItem)
					{
						// fillNextList(currList);
						p++;
					} else
					{
						log.error("unable to locate field: "
								+ field.getFieldName());
						return;
					}
				}
			}
		}
    	ListModel model = listBoxList.get(fieldPath.size()-1).getModel();
    	for (int f = 0; f < model.getSize(); f++)
    	{
    		BaseQRI item = (BaseQRI )model.getElementAt(f);
    		if (item.getTitle().equals(field.getTitle()))
    		{
    			processingLists = true;
    			listBoxList.get(fieldPath.size()-1).setSelectedIndex(f);
    			listBoxList.get(fieldPath.size()-1).ensureIndexIsVisible(f);
    			processingLists = false;
    			break;
    		}
    	}

    }
    
    public FieldQRI getFieldQRI(final SpQueryField field)
    {
//    	return getFieldQRI(tableTree, field, getTableIds(field.getTableList()),
//    			0, tableTreeHash);
    	return getFieldQRI(tableTree, field.getFieldName(), field.getIsRelFld() != null && field.getIsRelFld(),
    			field.getStringId(), getTableIds(field.getTableList()),
    			0, tableTreeHash);
    }
    
    /**
     * @param kids
     * @param field
     * @param tableIds
     * @param level
     * @return
     */
    protected static FieldQRI getFieldQRI(final TableTree tbl,
                                   //final SpQueryField field,
                                   final String fieldName,
    								final boolean isRelFld,
    								final String fldStringId,
    								final Vector<TableTreePathPoint> tableIds,
                                   final int level,
                                   final Hashtable<String, TableTree> ttHash)
    {
        TableTreePathPoint id = tableIds.get(level);
        for (int k=0; k<tbl.getKids(); k++)
        {
            TableTree kid = tbl.getKid(k);
            boolean checkKid = kid.isAlias() ? fixAliases(kid, ttHash) : true;
            if (checkKid && (kid.getTableQRI().getRelationship() == null 
            		|| !kid.getTableQRI().getRelationship().isHidden()))
            {
            	if (id.equals(new TableTreePathPoint(kid)))
                {
                    if (level == (tableIds.size() - 1))
                    {
                        //if (field.getIsRelFld() == null || !field.getIsRelFld())
                    	if (!isRelFld)
                        {
                            for (int f = 0; f < kid.getTableQRI().getFields(); f++)
                            {
                                //if (kid.getTableQRI().getField(f).getStringId().equals(field.getStringId())) 
                                if (kid.getTableQRI().getField(f).getStringId().equalsIgnoreCase(fldStringId))
                                { 
                                    return kid.getTableQRI().getField(f); 
                                }
                            }
                        }
                        //else if (kid.field.equalsIgnoreCase(field.getFieldName()))
                        else if (kid.field.equalsIgnoreCase(fieldName))
                        {
                            return buildFieldQRI(kid.getTableQRI());
                        }
                    }
                    else 
                    {
                        //FieldQRI fi = getFieldQRI(kid, field, tableIds, level + 1, ttHash);
                        FieldQRI fi = getFieldQRI(kid, fieldName, isRelFld, fldStringId, tableIds, level + 1, ttHash);
                        if (fi != null) 
                        { 
                        	return fi; 
                        }
                    }
                }
            }
        }
        return null;
    }

    protected static Vector<TableTreePathPoint> getTableIds(final String tableIdsList)
    {
    	String[] points = StringUtils.split(tableIdsList, ",");
    	Vector<TableTreePathPoint> result = new Vector<TableTreePathPoint>();
    	for (String point : points)
    	{
    		result.add(new TableTreePathPoint(point));
    	}
    	return result;
    }
    
    /**
     * @param container
     * @param fields
     * @param tblTree
     * @param ttHash
     * @param saveBtn
     * @param missingFlds
     * 
     * @return a Vector of QueryFieldPanel objects for the supplied fields parameter.
     */
    protected static Vector<QueryFieldPanel> getQueryFieldPanels(final QueryFieldPanelContainerIFace container, 
            final Set<SpQueryField> fields, final TableTree tblTree, 
            final Hashtable<String,TableTree> ttHash,
            final Component saveBtn, List<String> missingFlds) 
    {
        Vector<QueryFieldPanel> result = new Vector<QueryFieldPanel>();
        List<SpQueryField> orderedFlds = new ArrayList<SpQueryField>(fields);
        Collections.sort(orderedFlds);
        result.add(bldQueryFieldPanel(container, null, null, container.getColumnDefStr(), saveBtn));
        for (SpQueryField fld : orderedFlds)
        {
        	FieldQRI fieldQRI = getFieldQRI(tblTree, fld.getFieldName(), fld.getIsRelFld() != null && fld.getIsRelFld(),
        			fld.getStringId(), getTableIds(fld.getTableList()), 0, ttHash);
            if (fieldQRI != null)
            {
                result.add(bldQueryFieldPanel(container, fieldQRI, fld, container.getColumnDefStr(), saveBtn));
                fieldQRI.setIsInUse(true);
                if (fieldQRI.isFieldHidden() && !container.isPromptMode())
                {
                	UIRegistry.showLocalizedMsg("QB_FIELD_HIDDEN_TITLE", "QB_FIELD_HIDDEN_SHOULD_REMOVE", fieldQRI.getTitle());
                }
            }
            else
            {
                log.error("Couldn't find [" + fld.getFieldName() + "] [" + fld.getTableList()
                        + "]");
                fields.remove(fld);
                fld.setQuery(null);
                if (missingFlds != null)
                {
                    String fldText = fld.getColumnAlias() != null ? fld.getColumnAlias()
                    		: fld.getFieldName();
                	missingFlds.add(fldText);
                }
            }
        }
        return result;
    }
  
    protected static SpQueryField getQueryFieldMapping(final SpExportSchemaMapping schemaMapping, final SpExportSchemaItem schemaItem)
    {
    	if (schemaMapping != null)
    	{
    		for (SpExportSchemaItemMapping mapping : schemaMapping.getMappings())
    		{
    				if (mapping.getExportSchemaItem().getId().equals(schemaItem.getId()))
    				{
    					return mapping.getQueryField();
    				}
    		}
    	}
    	return null;
    }
    
    /**
     * @return
     */
    public static Vector<QueryFieldPanel> getQueryFieldPanelsForMapping(final QueryFieldPanelContainerIFace container, 
            final Set<SpQueryField> fields, final TableTree tblTree, 
            final Hashtable<String,TableTree> ttHash, final Component saveBtn,
            SpExportSchemaMapping schemaMapping, List<String> missingFlds,
            Map<String, MappedFieldInfo> autoMaps)
    {
        Vector<QueryFieldPanel> result = new Vector<QueryFieldPanel>();
        //Need to change columnDefStr if mapMode...
        //result.add(bldQueryFieldPanel(this, null, null, getColumnDefStr(), saveBtn));
        
        Vector<SpExportSchemaItem> sis = new Vector<SpExportSchemaItem>(schemaMapping.getSpExportSchema().getSpExportSchemaItems());
        Collections.sort(sis, new Comparator<SpExportSchemaItem>(){

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(SpExportSchemaItem o1, SpExportSchemaItem o2)
			{
				return o1.getFieldName().compareTo(o2.getFieldName());
			}
        		
        });
        for (SpExportSchemaItem schemaItem : sis)
        {
        	
        	SpQueryField fld = getQueryFieldMapping(schemaMapping, schemaItem);
        	if (fld == null)
        	{
        			result.add(new QueryFieldPanel(container, null, 
        				container.getColumnDefStr(), saveBtn, fld, schemaItem));
        	}
        	else
        	{
        		FieldQRI fieldQRI = getFieldQRI(tblTree, fld.getFieldName(), fld.getIsRelFld() != null && fld.getIsRelFld(),
            			fld.getStringId(), getTableIds(fld.getTableList()), 0, ttHash);
        		if (fieldQRI != null)
        		{
        			result.add(new QueryFieldPanel(container, fieldQRI, 
            				container.getColumnDefStr(), saveBtn, fld, schemaItem));
        			fieldQRI.setIsInUse(true);
        			if (fieldQRI.isFieldHidden() && !container.isPromptMode())
        			{
        				UIRegistry.showLocalizedMsg("QB_FIELD_HIDDEN_TITLE", "QB_FIELD_HIDDEN_SHOULD_REMOVE", fieldQRI.getTitle());
        			}
        		}
        		else
        		{
        			log.error("Couldn't find [" + fld.getFieldName() + "] [" + fld.getTableList()
                        + "]");
        			for (SpQueryField field : fields)
        			{
        				//ain't superstitious but checking ids in case 
        				//fld and field are different java objects
        				if (field.getId().equals(fld.getId()))
        				{
        					fields.remove(field);
        					field.setQuery(null);
        					fld.setQuery(null);
        					break;
        				}
        			}
        			if (missingFlds != null)
        			{
                        String fldText = fld.getColumnAlias() != null ? fld.getColumnAlias()
                        		: fld.getFieldName();
        				missingFlds.add(fldText);
        			}
        		}
        	}
        }
        
        //now add un-mapped fields
        for (SpQueryField fld : fields)
        {
        	//int insertAt = 0;
        	if (fld.getMapping() == null)
        	{
        		FieldQRI fieldQRI = getFieldQRI(tblTree, fld.getFieldName(), fld.getIsRelFld() != null && fld.getIsRelFld(),
            			fld.getStringId(), getTableIds(fld.getTableList()), 0, ttHash);
        		if (fieldQRI != null)
        		{
//        			result.insertElementAt(new QueryFieldPanel(container, fieldQRI, 
//            				container.getColumnDefStr(), saveBtn, fld, null, true), insertAt++);
        			result.add(new QueryFieldPanel(container, fieldQRI, 
            				container.getColumnDefStr(), saveBtn, fld, null, true));
        			fieldQRI.setIsInUse(true);
        			if (fieldQRI.isFieldHidden() && !container.isPromptMode())
        			{
        				UIRegistry.showLocalizedMsg("QB_FIELD_HIDDEN_TITLE", "QB_FIELD_HIDDEN_SHOULD_REMOVE", fieldQRI.getTitle());
        			}
        		}
        		else
        		{
        			log.error("Couldn't find [" + fld.getFieldName() + "] [" + fld.getTableList()
                        + "]");
        			if (missingFlds != null)
        			{
        				missingFlds.add(fld.getColumnAlias());
        			}
        		}
        	}
        }
        // now add placeHolder panel for adding new condition
		result.add(new QueryFieldPanel(container, null, 
				container.getColumnDefStr(), saveBtn, null, null, true));
        
        return result;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getColumnDefStr()
     */
    //@Override
    public String getColumnDefStr()
    {
        return columnDefStr;
    }

    /**
     * @param container
     * @param fieldQRI
     * @param fld
     * @param colDefStr
     * @param saveBtn
     * @return a QueryFieldPanel for the field represented by the fieldQRI object.
     */
    protected static QueryFieldPanel bldQueryFieldPanel(final QueryFieldPanelContainerIFace container,
                                                        final FieldQRI fieldQRI,
                                                        final SpQueryField fld,
                                                        String colDefStr,
                                                        final Component saveBtn)
    {
        if (colDefStr == null) 
        { 
            return new QueryFieldPanel(container, fieldQRI, colDefStr, saveBtn, null, null); 
        }
        return new QueryFieldPanel(container, fieldQRI, colDefStr, saveBtn, fld, null);
    }
    
    protected void updateUIAfterAddOrMap(final FieldQRI fieldQRI, final QueryFieldPanel qfp, final boolean loading, final boolean isAdd)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                if (currentInx > -1)
                {
                    if (isAdd)
                    {
                    	queryFieldsPanel.add(qfp);
                    	queryFieldsPanel.validate();
                    }
                    if (fieldQRI instanceof RelQRI)
                    {
                        BaseQRI qri = fieldQRI.getTable();
                        for (JList lb : listBoxList)
                        {
                            if (lb.isVisible())
                            {
                                if (((DefaultListModel) lb.getModel()).contains(qri))
                                {
                                    lb.repaint();
                                }
                            }
                        }
                    }
                    else
                    {
                        listBoxList.get(currentInx).repaint();
                    }
                    
                    updateAddBtnState();
                    selectQFP(qfp);
                    queryFieldsPanel.repaint();
                    if (!loading)
                    {
                        saveBtn.setEnabled(canSave());
                        updateSearchBtn();
                    }
                    //Sorry, but a new context can't be selected if any fields are selected from the current context.
                    tableList.setEnabled(queryFieldItems.size() == 0);
                    if (fieldQRI instanceof TreeLevelQRI && distinctChk.isSelected() && countOnly)
                    {
                    	countOnly = false;
                     	countOnlyChk.setSelected(false);
                    	UIRegistry.displayLocalizedStatusBarText("QB_NO_COUNT_WITH_DISTINCT_WITH_TREELEVEL");
                    }
                    else
                    {
                    	UIRegistry.displayStatusBarText(null);
                    }
                }
            }
        });
    }
    /**
     * Add QueryFieldItem to the list created with a TableFieldPair.
     * 
     * @param fieldItem the TableFieldPair to be in the list
     */
    protected void addQueryFieldItem(final FieldQRI fieldQRI, final SpQueryField queryField, final boolean loading)
    {
        if (fieldQRI != null)
        {            
            final QueryFieldPanel qfp = new QueryFieldPanel(this, fieldQRI, columnDefStr, saveBtn, queryField, null);
            qfp.addMouseListener(new MouseInputAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    selectQFP(qfp);
                }
            });
            queryFieldItems.add(qfp);
            qualifyFieldLabels();
            fieldQRI.setIsInUse(true);
            updateUIAfterAddOrMap(fieldQRI, qfp, loading, true);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#selectQFP(edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanel)
     * 
     * Use runSelectQFP if not calling from Swing thread.
     */
    public void selectQFP(final QueryFieldPanel qfp)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            //apparently this never happens, but...
            runSelectQFP(qfp);
        }
        else
        {
            if (selectedQFP != null)
            {
                selectedQFP.setSelected(false);
                selectedQFP.repaint();
            }
            selectedQFP = qfp;
            if (selectedQFP != null)
            {
                selectedQFP.setSelected(true);
                selectedQFP.repaint();
                scrollQueryFieldsToRect(selectedQFP.getBounds());
            }
            updateMoverBtns();
            if (qfp != null)
            {
            	FieldQRI fqri = qfp.getFieldQRI();
            	if (fqri != null)
            	{
            		displayField(fqri);
            	}
            }
        }
    }
    
    /**
     * @param qfp
     * 
     * runs selectQFP() in Swing thread.
     */
    private void runSelectQFP(final QueryFieldPanel qfp)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                selectQFP(qfp);
            }
        });
    }
    
    /**
     * Enables field mover buttons as appropriate for position of currently select QueryFieldPanel.
     */
    protected void updateMoverBtns()
    {
        if (exportSchema == null)
        {
        	int idx = queryFieldItems.indexOf(selectedQFP);
        	orderUpBtn.setEnabled(idx > 0);
        	orderDwnBtn.setEnabled(idx > -1 && idx < queryFieldItems.size()-1);
        }
    }
    
    /**
     * Adds qualifiers (TableOrRelationship/Field Title) to query fields where necessary.
     * 
     */
    protected void qualifyFieldLabels()
    {
        List<String> labels = new ArrayList<String>(queryFieldItems.size());
        Map<String, List<QueryFieldPanel>> map = new HashMap<String, List<QueryFieldPanel>>();
        for (QueryFieldPanel qfp : queryFieldItems)
        {
            if (qfp.getFieldQRI() != null && qfp.getFieldTitle() != null) //this means tree levels won't get qualified.
            {
                if (!map.containsKey(qfp.getFieldTitle()))
                {
                    map.put(qfp.getFieldTitle(), new LinkedList<QueryFieldPanel>());
                }
                map.get(qfp.getFieldTitle()).add(qfp);
                labels.add(qfp.getFieldTitle());
            }
        }
        
        for (Map.Entry<String, List<QueryFieldPanel>> entry : map.entrySet())
        {
            if (entry.getValue().size() > 1 || entry.getValue().get(0).isLabelQualified())
            {
                for (QueryFieldPanel q : entry.getValue())
                {
                    labels.remove(entry.getKey());
                    labels.add(q.qualifyLabel(labels, entry.getValue().size() == 1));
                }
            }
        }
    }


    /**
     * @param tbl
     * @param hash
     */
    protected static boolean fixAliases(final TableTree tbl, final Hashtable<String, TableTree> hash)
    {
        if (tbl.isAlias())
        {
            TableTree tt = hash.get(tbl.getName());
            if (tt != null)
            {
                if (!tt.getTableInfo().isHidden() && tblIsDisplayable(tbl, tt.getTableInfo()))
                {
                    tbl.clearKids();
                    try
                    {
                        for (int k = 0; k < tt.getKids(); k++)
                        {
//                            if (tblIsDisplayable(tt.getKid(k), tableTreeHash.get(tt.getKid(k).getName()).getTableInfo());
//                        	
//                        	if (tt.getKid(k).getTableInfo() == null)
//                            {
//                            	System.out.println("TableInfo is null for " + tt.getKid(k).getName() + " - " + tt.getKid(k).getField());
//                            }
//                            else if (tt.getKid(k).getTableInfo() != null && tblIsDisplayable(tt.getKid(k), tt.getKid(k).getTableInfo()))
//                            {
                            	tbl.addKid((TableTree) tt.getKid(k).clone());
//                            } else 
//                            {
//                            	System.out.println("Skipping " +  tt.getKid(k).getName() + " - " + tt.getKid(k).getField());
//                            }
                        }
                        tbl.setTableInfo(tt.getTableInfo());
                        tbl.setTableQRIClone(tt.getTableQRI());
                        return true;
                    }
                    catch (CloneNotSupportedException ex)
                    {
                        UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(QueryBldrPane.class, ex);
                        throw new RuntimeException(ex);
                    }
                }
                return false;
            }
            log.error("Couldn't find [" + tbl.getName() + "] in the hash.");
            return false;
        }
        return true;
    }

    
    /**
     * @param columnDefStr the columnDefStr to set
     */
    public void setColumnDefStr(String columnDefStr)
    {
        this.columnDefStr = columnDefStr;
    }

    /**
     * @return the btn that launched the editor
     */
    public RolloverCommand getQueryNavBtn()
    {
        return queryNavBtn;
    }

    /**
     * @param queryNavBtn
     */
    public void setQueryNavBtn(RolloverCommand queryNavBtn)
    {
        this.queryNavBtn = queryNavBtn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#shutdown()
     */
    @Override
    public void shutdown()
    {
        super.shutdown();

        CommandDispatcher.unregister(ReportsBaseTask.REPORTS, this);
        
        if (saveBtn != null && saveBtn.isEnabled())
        {
            saveBtn.setEnabled(false);
        }

        if (runningResults.get() != null)
        {
            runningResults.get().cancel();
        }
        
        if (completedResults.get() != null)
        {
        	completedResults.get().cancel();
        }
        
        //This is safe as long as we continue to allow only 1 qb result.
        //and the qbresult pane always returns true for aboutToShutdown()
        QBResultsSubPane qbResultPane = null;
        for (SubPaneIFace subPane : SubPaneMgr.getInstance().getSubPanes())
        {
        	if (subPane instanceof QBResultsSubPane)
        	{
        		qbResultPane = (QBResultsSubPane )subPane;
        		break;
        	}
        }
        if (qbResultPane != null)
        {
        	QBResultsTablePanel tblPane = qbResultPane.getResultsTable();
        	if (tblPane != null)
        	{
        		QBResultSetTableModel tblModel = tblPane.getTableModel();
        		if (tblModel != null)
        		{
        			tblModel.cancelBackgroundLoads();
        		}
        	}
        	SubPaneMgr.getInstance().removePane(qbResultPane);
        }
        
        query = null;
        if (queryNavBtn != null)
        {
            queryNavBtn.setEnabled(true);
        }
        
        /*NOTE: runningResults or completedResults may still be pointing to this so
        garbage collection will be hampered, but since only one QueryResult is allowed
        at any time it should not be a problem. (???)
        */
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
     */
    @Override
    public boolean aboutToShutdown()
    {
        boolean result = true;
        unlock();
        if (isChanged())
        {
            String msg = String.format(getResourceString("SaveChanges"), getTitle());
            JFrame topFrame = (JFrame)UIRegistry.getTopWindow();

            int rv = JOptionPane.showConfirmDialog(topFrame,
                                                   msg,
                                                   getResourceString("SaveChangesTitle"),
                                                   JOptionPane.YES_NO_CANCEL_OPTION);
            if (rv == JOptionPane.YES_OPTION)
            {
                saveQuery(false);
            }
            else if (rv == JOptionPane.CANCEL_OPTION || rv == JOptionPane.CLOSED_OPTION)
            {
                return false;
            }
            else if (rv == JOptionPane.NO_OPTION)
            {
                // nothing
            }
        }
        return result;
    }
    
    /**
     * Frees mapping lock if necessary
     */
    protected void unlock()
    {
    	if (query != null && query.getMapping() != null)
    	{
    		ExportMappingTask.unlockMapping(query.getMapping());
    	}
    }
    /**
     * @return true if there are unsaved changes to the query.
     */
    protected boolean isChanged()
    {
        return saveBtn.isEnabled(); //el cheapo
    }
    
    /**
     * @param toMove
     * @param moveTo
     * 
     * Moves toMove to moveTo's position and shifts other panels to fill toMove's former position.
     */
    protected void moveField(final QueryFieldPanel toMove, final QueryFieldPanel moveTo)
    {
        int fromIdx = queryFieldItems.indexOf(toMove);
        int toIdx = queryFieldItems.indexOf(moveTo);
        if (fromIdx == toIdx)
        {
            return;
        }
        
        queryFieldItems.remove(fromIdx);
        queryFieldItems.insertElementAt(toMove, toIdx);
         
        ((NavBoxLayoutManager)queryFieldsPanel.getLayout()).moveLayoutComponent(toMove, moveTo);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                queryFieldsPanel.doLayout();
                queryFieldsPanel.validate();
                scrollQueryFieldsToRect(toMove.getBounds());
                queryFieldsPanel.repaint();
                updateMoverBtns();
                saveBtn.setEnabled(canSave());
            }
        });
    }
    
    /**
     * @param rect - the rectangle to make visible.
     * 
     * Wrapper for JViewport.scrollReectToVisible() with a work around for a java bug.
     */
    protected void scrollQueryFieldsToRect(final Rectangle rect)
    {
        //scrollRectToVisible doesn't work when newBounds is above the viewport.
        //This is a java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6333318
//        if (rect.y < queryFieldsScroll.getViewport().getViewPosition().y)
//        {
//            queryFieldsScroll.getViewport().setViewPosition(new Point(rect.x,rect.y));
//        }
        queryFieldsScroll.getViewport().scrollRectToVisible(rect);
        
        //scrollRectToVisible doesn't work when newBounds is above the viewport.
        //This is a java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6333318
        if (rect.y < queryFieldsScroll.getViewport().getViewPosition().y)
        {
            queryFieldsScroll.getViewport().setViewPosition(new Point(rect.x,rect.y));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getFields()
     */
    public int getFields()
    {
        return queryFieldItems.size();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getField(int)
     */
    public QueryFieldPanel getField(int index)
    {
        return queryFieldItems.get(index);
    }
    
    /**
     * Disables search button when there is nothing to search for.
     */
    protected void updateSearchBtn()
    {
        searchBtn.setEnabled(queryFieldItems.size() > 0);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#isPromptMode()
     */
    //@Override
    public boolean isPromptMode()
    {
        return false;
    }

    /**
     * @return true if it is possible for the QueryBuilder to execute a search.
     */
    protected boolean canSearch()
    {
        if (runningResults.get() == null)
        {
            return true;
        }
//        if (runningResults.get().getQueryTask() == null)
//        {
//            //something has gone wrong?
//            runningResults.set(null);
//            return true;
//        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    //@Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(ReportsBaseTask.REPORTS)) 
        {
            refreshQuery(); //currently done for all commands
            if (cmdAction.isAction(ReportsBaseTask.REFRESH))
            {
                //nothing else to do
            }
//            else if (cmdAction.isAction(ReportsBaseTask.REPORT_DELETED))
//            {
//                if (runningResults.get() != null)
//                {
//                    
//                	runningResults.get().reportDeleted((Integer)cmdAction.getData());
//                }
//                if (completedResults.get() != null)
//                {
//                    completedResults.get().reportDeleted((Integer)cmdAction.getData());
//                }
//            }
        }
    }
    
    /**
     * Get latest persisted version of this query.
     */
    protected void refreshQuery()
    {
        if (query != null && query.getId() != null && !this.isChanged())
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance()
            .createSession();
            try
            {
                session.refresh(query);
                query.forceLoad(true);
            }
            finally
            {
                session.close();
            }
        }
    }
    
    /**
     * @return Set of reports that are based on this query.
     */
    public Set<SpReport> getReportsForQuery()
    {
        //assuming query.forceLoad() has been called
        return query.getReports();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#showingPane(boolean)
     */
    @Override
    public void showingPane(boolean show)
    {
        if (show && ((QueryTask )task).needToRebuildTableTree() && !reloadMsgShown)
        {
            //It seems that no serious problems will occur so for now just show a message:
            UIRegistry.showLocalizedMsg("QB_TREEDEF_LOCALIZ_CHANGES_TITLE", "QB_TREEDEF_LOCALIZ_CHANGES_WARN");
            reloadMsgShown = true;
        }
    }
    
    /**
     * @return the query
     */
    public SpQuery getQuery()
    {
        return query;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getAddBtn()
     */
    //@Override
    public JButton getAddBtn()
    {
        return addBtn;
    }    
    
    protected boolean isQRIClassSelected(Class<?> qriClass)
    {
    	for (QueryFieldPanel qfp : this.queryFieldItems)
    	{
    		if (qfp.getFieldQRI() != null && qriClass.isAssignableFrom(qfp.getFieldQRI().getClass()))
    		{
    			return true;
    		}
    	}
    	return false;
    }
    /**
     * @return true if the query's fields list contains a TreeLevel field. 
     */
    protected boolean isTreeLevelSelected()
    {
    	return isQRIClassSelected(TreeLevelQRI.class);
    }
    
    /**
     * @return true if the query's fields list contains an aggregated ( field. 
     */
    protected boolean isAggFieldSelected()
    {
    	for (QueryFieldPanel qfp : this.queryFieldItems)
    	{
    		if (qfp.getFieldQRI() instanceof RelQRI)
    		{
    			DBRelationshipInfo info = ((RelQRI)qfp.getFieldQRI()).getRelationshipInfo();
    			
    			if (info != null && 
    					(info.getType().equals(RelationshipType.ManyToMany) || info.getType().equals(RelationshipType.OneToMany)))
    			{
    				return true;
    			}
    		}
    	}
    	return false;
    }
    

}


