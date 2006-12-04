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
package edu.ku.brc.specify.tests;


import static edu.ku.brc.specify.tests.CreateTestDatabases.createAgentsInMemory;
import static edu.ku.brc.specify.tests.CreateTestDatabases.createGeographies;
import static edu.ku.brc.specify.tests.CreateTestDatabases.createLocations;
import static edu.ku.brc.specify.tests.CreateTestDatabases.createTaxonomy;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createAttributeDef;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCatalogSeries;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollectingEvent;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollectionObjDef;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollectionObject;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createCollector;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createDataType;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createDetermination;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createDeterminationStatus;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createLocality;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPrepType;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPreparation;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPreparationAttr;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createSpecifyUser;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createTaxonTreeDef;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createUserGroup;
import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTree;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPrefsEditor;
import edu.ku.brc.af.prefs.PrefMainPanel;
import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.EMailHelper;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Collectors;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.LoanPhysicalObject;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.tests.forms.TestDataObj;
import edu.ku.brc.specify.ui.LoanSelectPrepsDlg;
import edu.ku.brc.ui.CurvedBorder;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.DatabaseLoginDlg;
import edu.ku.brc.ui.db.DatabaseLoginListener;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.ViewSetMgr;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.util.FileCache;

/**
 * The stand alone part of the FormEditor (this is a prototype at the moment that is used for viewing forms)

 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class FormEditor implements DatabaseLoginListener
{
    private static final Logger log = Logger.getLogger(FormEditor.class);

    protected JPanel    contentPane = new JPanel(new BorderLayout());
    protected JFrame    mainFrame;

    protected String    currViewSetName;
    protected String    currViewName;

    protected Object            dataObj     = null;
    protected TestDataObj       testDataObj = null;
    protected List<TestDataObj> list        = new ArrayList<TestDataObj>();

    protected Viewable      fvo;
    protected DataType          dataType;
    protected MultiView         multiView;

    protected PanelBuilder    builder    = null;
    protected CellConstraints cc         = new CellConstraints();
    
    protected ViewSetMgr    viewSetMgr      = null;
    protected List<Object>  databaseObjects = null; 



    public FormEditor()
    {
        //ViewSetMgr.setAsDefaultViewSet("Fish Views");

    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void initialize()
    {
        UICacheManager.getInstance(); // initializes it first thing
        UICacheManager.setAppName("Specify");
        IconManager.setApplicationClass(Specify.class);
        
        /*
        List<Object> objects = BuildSampleDatabase.createSingleDiscipline("fish", "fish");
        
        List<Object> dbOBjs = BuildSampleDatabase.get(objects, CollectionObject.class);
        */
        
        /*
        System.setProperty("edu.ku.brc.af.core.AppContextMgrFactory", "edu.ku.brc.specify.config.SpecifyAppContextMgr");
        System.setProperty("AppPrefsIOClassName", "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");
        
        UICacheManager.getInstance(); // initializes it first thing
        UICacheManager.setAppName("Specify");
        IconManager.setApplicationClass(Specify.class);
        
        
        // Load Local Prefs
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UICacheManager.getDefaultWorkingPath());
        localPrefs.load();
        */
        
        FileCache.setDefaultPath(UICacheManager.getDefaultWorkingPath());

        try
        {
            //System.out.println(System.getProperty("os.name"));

            if (!System.getProperty("os.name").equals("Mac OS X"))
            {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                 PlasticLookAndFeel.setCurrentTheme(new ExperienceBlue());
             }

            // Note: This is asynchronous
            //UIHelper.doLogin(true, true, false, this); // true means do auto login if it can, second true means use dialog
            
            if (false)
            {
                showAppPrefsEditor(false);
            }

            selectViewSetMgr();

        }
        catch (Exception e)
        {
            log.error("Can't change L&F: ", e);
        }
    }
    
    /**
     * Loads the dialog
     * @param hashNames every other one is the new name
     * @return the list of selected DBs
     */
    protected ViewSetMgr selectViewSetMgr()
    {
        File         config   = new File(XMLHelper.getConfigDirPath(null));
        ViewFileNode rootNode = new ViewFileNode("Config", null);
        
        recurseForViews(rootNode, config);
         

        
        ChooseVSM dlg = new ChooseVSM(null, "Choose a View Set", rootNode);
        dlg.setModal(true);
        UIHelper.centerAndShow(dlg);
        
        dlg.dispose();
        if (!dlg.isCancelled())
        {
            viewSetMgr = new ViewSetMgr(dlg.getSelectedNode());
            startup("fish", "fish");
        }

        
        return null;
    }

    
    /**
     * @param parentNode
     * @param parentDir
     * @return
     */
    protected boolean recurseForViews(final ViewFileNode parentNode, final File parentDir)
    {
        if (parentDir.getName().startsWith("."))
        {
            return false;
        }
        
        System.out.println("Going into ["+parentDir.getAbsolutePath()+"]");
        boolean added = false;
        for (File f : parentDir.listFiles())
        {
            if (f.isDirectory() && !parentDir.getName().startsWith("."))
            {
                ViewFileNode node     = new ViewFileNode(f.getName(), null);
                File         viewFile = new File(f.getAbsoluteFile() + File.separator + "viewset_registry.xml");
                System.out.println(viewFile.getAbsolutePath());
                if (viewFile.exists())
                {
                    if (parentNode != null)
                    {
                        parentNode.addKid(node);
                        added = true;
                    }
                }
                
                boolean wasAdded = recurseForViews(node, f);
                if (wasAdded)
                {
                    if (parentNode != null && !added)
                    {
                        parentNode.addKid(node);
                    }
                    added = true; 
                }
            }
        }
        return added;
    }
    
    /**
     * Shows App Prefs Editor
     */
    public void showAppPrefsEditor(final boolean doRemote)
    {
        JFrame.setDefaultLookAndFeelDecorated(false);

        JFrame frame = new JFrame("Application Prefs Editor");
        frame.setContentPane(new AppPrefsEditor(doRemote));
        frame.setIconImage(IconManager.getIcon("AppIcon", IconManager.IconSize.Std16).getImage());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.pack();

        UIHelper.centerAndShow(frame);
    }


    /**
     * @param disciplineName fish, birds, bees etc
     * @return true on success
     */
    public CollectionObject[] createSingleDiscipline(final String colObjDefName, final String disciplineName)
    {
        UserGroup        userGroup        = createUserGroup(disciplineName);
        SpecifyUser      user             = createSpecifyUser("rods", "rods@ku.edu", (short)0, userGroup, "CollectionManager");
        DataType         dType            = createDataType(disciplineName);


        TaxonTreeDef     taxonTreeDef     = createTaxonTreeDef("TreeDef");
        CollectionObjDef collectionObjDef = createCollectionObjDef(colObjDefName, disciplineName, dType, user, taxonTreeDef);

        Geography[] geographies = createGeographies(collectionObjDef, "GeoTree");

        Locality[] localities = new Locality[2];
        localities[0] = createLocality("This is the place", geographies[0]);
        localities[1] = createLocality("My Private Forest", geographies[1]);

        Location[] locations = createLocations(collectionObjDef, "GLocationTree");
        Taxon[]    taxonomy  = createTaxonomy(taxonTreeDef);

        Agent[] agents = createAgentsInMemory();

        CatalogSeries catalogSeries = createCatalogSeries("KUFSH", "Fish", collectionObjDef);


        // Create Collecting Event
        CollectingEvent colEv = createCollectingEvent(localities[0],
                new Collectors[] {createCollector(agents[0], 0), createCollector(agents[1], 1)});

        // Create AttributeDef for Collecting Event
        //AttributeDef cevAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "ParkName", null);

        // Create CollectingEventAttr
        //CollectingEventAttr cevAttr = createCollectingEventAttr(colEv, cevAttrDef, "Clinton Park", null);

        // Create Collection Object
        Object[]  values = {1001010.1f, "RCS101", agents[0], 5,
                            1101011.1f, "RCS102", agents[1], 20,
                            1201012.1f, "RCS103", agents[2], 15,
                            1301013.1f, "RCS104", agents[3], 25,
                            1401014.1f, "RCS105", agents[4], 35,
                            1501015.1f, "RCS106", agents[5], 45,
                            1601016.1f, "RCS107", agents[0], 55,
                            1701017.1f, "RCS108", agents[1], 65};
        CollectionObject[] colObjs = new CollectionObject[values.length/4];
        for (int i=0;i<values.length;i+=4)
        {
            colObjs[i/4] = createCollectionObject((Float)values[i],
                                                  (String)values[i+1],
                                                  null,
                                                  (Agent)values[i+2],
                                                  catalogSeries,
                                                  collectionObjDef,
                                                  (Integer)values[+3],
                                                  colEv);
        }

        // Create AttributeDef for Collection Object
        //AttributeDef colObjAttrDef = createAttributeDef(AttributeIFace.FieldType.StringType, "MoonPhase", null);

        // Create CollectionObjectAttr
        //CollectionObjectAttr colObjAttr = createCollectionObjectAttr(colObjs[0], colObjAttrDef, "Full", null);

        int agentInx = 0;
        int taxonInx = 0;
        // Create DeterminationStatus
        DeterminationStatus current = createDeterminationStatus("Current","Test Status");
        DeterminationStatus notCurrent = createDeterminationStatus("Not current","Test Status");
        // Create Determination
        for (int i=0;i<colObjs.length;i++)
        {
            for (int j=0;j<i+2;j++)
            {
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(1990-i, 11-i, 28-(i+j));
                DeterminationStatus status = (j==0) ? current : notCurrent;
                createDetermination(colObjs[i], agents[agentInx % agents.length], taxonomy[taxonInx % taxonomy.length], status, cal);
                agentInx++;
                taxonInx++;
            }
        }

        // Create Preparation Type
        PrepType prepType = createPrepType("Skeleton");
        //PrepType prepType2 = createPrepType("C&S");

        // Create Preparation for each CollectionObject
        agentInx = 3; // arbitrary
        Preparation[] preps = new Preparation[colObjs.length];
        for (int i=0;i<preps.length;i++)
        {
            preps[i] = createPreparation(prepType,  agents[agentInx % agents.length], colObjs[i], locations[0], 10+i);
            agentInx++;
        }

        // Create AttributeDef for Preparation
        AttributeDef prepAttrDefSize = createAttributeDef(AttributeIFace.FieldType.IntegerType, "size", prepType);
        AttributeDef prepAttrDefSex  = createAttributeDef(AttributeIFace.FieldType.StringType, "sex", prepType);

        // Create PreparationAttr
        for (int i=0;i<preps.length;i++)
        {
            createPreparationAttr(prepAttrDefSize, preps[i], null, 100.0);
            createPreparationAttr(prepAttrDefSex,  preps[i], i % 2 == 0 ? "Male" : "Female", null);
        }

        return colObjs;

    }


    /**
     * Creates a Standard set of DataTypes for Collections
     * @param returnName the name of a DataType to return (ok if null)
     * @return the DataType requested
     */
    public DataType createDataTypes(final String returnName)
    {
        DataType retDataType = null;
        int numDataTypes = BasicSQLUtils.getNumRecords(DBConnection.getInstance().getConnection(), "datatype");
        if (numDataTypes == 0)
        {
            String[] dataTypeNames = {"Animal", "Plant", "Fungi", "Mineral", "Other"};


            try
            {
                Session session = HibernateUtil.getCurrentSession();
                HibernateUtil.beginTransaction();

                for (String name : dataTypeNames)
                {
                    DataType dType = new DataType();
                    dType.setName(name);
                    dType.setCollectionObjDef(null);
                    session.save(dType);

                    if (returnName != null && name.equals(returnName))
                    {
                        retDataType = dType;
                    }
                }

                HibernateUtil.commitTransaction();

            } catch (Exception e)
            {
                log.error("******* " + e);
                HibernateUtil.rollbackTransaction();
            }
        } else
        {
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(DataType.class);
            criteria.add(Expression.eq("name", "Animal"));
            for (Object obj : criteria.list())
            {
                DataType dType = (DataType)obj;
                //System.out.println(dataType.getName());
                retDataType = dType;
                break;
            }

        }
        return retDataType;
    }

    /**
     * Create a form
     * @param formView the definition of the form to create
     */
    protected Viewable createView(View view)
    {
        //multiView   = new MultiView(null, view, AltView.CreationMode.View, false, false);
        multiView   = new MultiView(null, null, view, AltView.CreationMode.Edit, MultiView.VIEW_SWITCHER | MultiView.RESULTSET_CONTROLLER);
        contentPane.removeAll();
        builder.add(multiView, cc.xy(1,1));

        //contentPane.setBackground(Color.BLUE);
        //contentPane.setOpaque(true);

        //Viewable form = ViewFactory.createView(view, null, null);
        //fvo = multiView.get;

        //Component comp = form.getUIComponent();
        //if (comp != null)
       // {
            //comp.invalidate();

            //contentPane.add(new CatalogSeriesWizard(null), BorderLayout.CENTER);


            contentPane.doLayout();
            UICacheManager.forceTopFrameRepaint();

            mainFrame.invalidate();
            mainFrame.doLayout();
            mainFrame.repaint();

            // XXX Why???
            mainFrame.pack();
            //mainFrame.setSize(new Dimension(800, 550));
            //mainFrame.pack();
            UIHelper.centerAndShow(mainFrame);

            boolean doSend = false;
            if (doSend)
            {
                String msg = "<!-- NUM_ITEMS 1 --><form name=\"form\"><table><tr><td><!-- ITEM0 101 -->[  ]</td><td>Megalotis</td></tr></table><form>";
                EMailHelper.sendMsg("imap.ku.edu", "rods", "Inverness1601*", "rods@ku.edu",
                        "rods@ku.edu",
                        "Catalog Items You Requested",
                         msg, "text/html", null);
            } else
            {
                //EMailHelper.findRepliesFromResearch("imap.ku.edu", "rods", "Inverness1601*");
            }

            /*
            if (currViewSetName.equals("view valid") && (currViewName == 0 || currViewName == 333))
            {
                form.setDataObj(dataObj);

                JButton btn = (JButton)form.getComp("OK");
                if (btn != null)
                {
                    ((FormViewObj)form).getValidator().registerOKButton(btn);
                }

                btn = (JButton)form.getComp("validateBtn");
                if (btn != null)
                {
                    btn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae)
                        {
                            fvo.getValidator().validateFormForOK();
                        }
                    });
                }

            } else if (currViewSetName.equals("SystemSetup") && currViewName == 500)
            {
                PickList pl = new PickList();
                pl.setItems(new HashSet());
                form.setDataObj(pl);

                FormViewObj fvo = (FormViewObj)form;
                fvo.getValidator().registerOKButton((JButton)form.getComp("savePL"));
                fvo.getValidator().validateForm();

            } else
            */

            if (false)
            {
                if (currViewSetName.equals("Fish Views") && currViewName.equals("Accession"))
                {
                    boolean doDB = false;
                    if (doDB)
                    {
                        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Accession.class).setMaxResults(10);
                        //Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Accession.class).setFetchMode(Accession.class.getName(), FetchMode.DEFAULT).setMaxResults(300);
                        dataObj = criteria.list();//session.find("from collev");
    
                    } else
                    {
                        Accession[] accessions = CreateTestDatabases.createAccessionsInMemory();
                        Vector<Object> accessionsList = new Vector<Object>();
                        for (Accession accession : accessions)
                        {
                            accessionsList.add(accession);
                        }
                        dataObj = accessionsList;
                    }
                    multiView.setData(dataObj);
                }
            }

            if (currViewSetName.equals("Fish Views") && currViewName.equals("FishBase"))
            {
                try
                {
                    Element dom = XMLHelper.readFileToDOM4J(new File("/tmp/Pimephales_notatus_Summary.xml"));
                    dataObj = dom;
                    multiView.setData(dataObj);

                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }


            if (currViewSetName.equals("Fish Views") && currViewName.equals("CollectionObject"))
            {


                //Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Accession.class);
                //Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Accession.class).setFetchMode(Accession.class.getName(), FetchMode.DEFAULT).setMaxResults(300);
                //java.util.List list = criteria.list();//session.find("from collev");
                boolean skip = false;
                if (skip)
                {
                    /*
                    Query q = HibernateUtil.getCurrentSession().createQuery("from accession in class Accession where accession.accessionId in (74,68262114,508322272)");
                    java.util.List list = q.list();
                    for (Object obj : list)
                    {
                        Accession accession = (Accession)obj;
                        System.out.println(accession.getAccessionId());
                    }
                    */
                }


                boolean doCatalogItems = false;
                if (doCatalogItems)
                {
                    Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(CollectionObject.class).setMaxResults(30);
                    //criteria.add(Expression.isNull("derivedFromId"));


                    java.util.List data = criteria.list();
                    //System.out.println("Items Returned: "+data.size());

                    dataObj = data;
                }

                boolean doMemoryCollection = false;
                if (doMemoryCollection)
                {
                    CollectionObject[] colObjs = createSingleDiscipline("Fish", "fish");

                    Set<CollectionObject> set = new HashSet<CollectionObject>();
                    for (int i=0;i<colObjs.length;i++)
                    {
                        set.add(colObjs[i]);
                    }
                    dataObj = set;
                }


                /*boolean doit = false;
                if (doit)
                {
                    //Query q = HibernateUtil.getCurrentSession().createQuery("from accession in class Accession where accession.accessionId in (74,68262114,508322272)");
                    Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(InfoRequest.class);
                    java.util.List list = criteria.list();
                    for (Object obj : list)
                    {
                        InfoRequest infoReq = (InfoRequest)obj;
                        JXPathContext context = JXPathContext.newContext(infoReq);

                        //System.out.println(context.getValue(""));
                    }

                }*/

                multiView.setData(dataObj);
                //multiView.setDataIntoUI();

            }


        //}


        return null;
    }

    /**
     *
     */
    protected void reload()
    {
       // ViewSetMgr.reset();

        //fvo = createForm(ViewSetMgrTests.getView(currViewSetName, currViewName));
    }

    /**
     *
     */
    protected void selectForm()
    {
        /*

        List<FormView>    fullFormsList = new ArrayList<FormView>();

        for (ViewSet viewSet : ViewSetMgrTests.getViewSets())
        {

            List<FormView>    forms   = viewSet.getViews();
            fullFormsList.addAll(forms);
        }
        ChooseFromListDlg dlg = new ChooseFromListDlg("Choose Form", fullFormsList); // XXX I18N
        dlg.setVisible(true);


        FormView form = (FormView)dlg.getSelectedObject();
        if (form != null)
        {
            currViewSetName = form.getViewSetName();
            currViewName      = form.getId();

            fvo = createForm(form);
        }
        */
    }

   /**
   * Create the GUI and show it.  For thread safety,
   * this method should be invoked from the
   * event-dispatching thread.
   */
    
  @SuppressWarnings("unused")
  private void startup(final String databaseName, final String userName)
  {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        // Create and set up the window.
        mainFrame = new JFrame("Specify Form Editor");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setContentPane(contentPane);

        builder = new PanelBuilder(new FormLayout("p", "p"), contentPane);

        UICacheManager.register(UICacheManager.TOPFRAME, mainFrame);

        JMenuBar menuBar = createMenus();
        if (menuBar != null)
        {
            //top.add(menuBar, BorderLayout.NORTH);
            mainFrame.setJMenuBar(menuBar);
        }

        // temp for testing

        currViewName      = "";
        currViewSetName =   "SystemSetup";

        currViewName      = "";
        currViewSetName =  "view valid";

        //currViewName      = "CollectionObject";

        currViewName      = "FishBase";
        currViewSetName =   "Fish Views";

        currViewName      = "CollectionObject";
        currViewSetName =   "Fish Views";

        //View view =  AppContextMgr.getInstance().getView(currViewSetName, currViewName);
        View view = viewSetMgr.getView(currViewSetName, currViewName);

        if (view != null)
        {
            createView(view);

        } else
        {
            log.info("Couldn't load form with name ["+currViewSetName+"] Id ["+currViewName+"]");
        }

    }
    
    
    /**
     * Create menus
     */
    public JMenuBar createMenus()
    {
        JMenuBar mb = new JMenuBar();
        JMenuItem mi;

        JMenu menu = createMenu(mb, "FileMenu", "FileMneu");
        mi = createMenuItem(menu, "Select Form", "s", "Select Form", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        selectForm();
                    }
                });

        mi = createMenuItem(menu, "Reload", "r", "Reload", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        reload();
                    }
                });

        mi = createMenuItem(menu, "Exit", "x", "Exit Appication", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        doExit();
                    }
                });

        menu = createMenu(mb, "EditMenu", "EditMneu");
        mi = createMenuItem(menu, "Preferences", "P", "Preferences", false, null);
        mi.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        preferences();
                    }
                });


         return mb;
    }

    /**
     * Create menus
     */
    public void preferences()
    {

        JDialog dlg = new JDialog();
        dlg.setModal(true);
        PrefMainPanel pane = new PrefMainPanel(dlg);
        dlg.setContentPane(pane);
        dlg.pack();
        dlg.doLayout();
        //System.out.println(dlg.getPreferredSize());
        dlg.setPreferredSize(dlg.getPreferredSize());
        dlg.setSize(dlg.getPreferredSize());
        UIHelper.centerAndShow(dlg);
    }


    /**
     * Create a menu
     * @param mennuBar the menubar
     * @param labelKey the label key to be localized
     * @param mneuKey the mneu key to be localized
     * @return returns a menu
     */
    protected JMenu createMenu(final JMenuBar menuBar, final String labelKey, final String mneuKey)
    {
        JMenu menu = null;
        try
        {
            menu = menuBar.add(new JMenu(getResourceString(labelKey)));
            menu.setMnemonic(getResourceString(mneuKey).charAt(0));
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.info("Couldn't create menu for " + labelKey + "  " + mneuKey);
        }
        return menu;
    }

    /**
     * Checks to see if cache has changed before exiting
     *
     */
    protected void doExit()
    {
        if (multiView != null)
        {
            multiView.shutdown();
        }
        System.exit(0);
    }

    /**
     * @param b
     * @return
     */
    protected PropertyChangeListener createActionChangeListener(final JMenuItem b)
    {
        return new ActionChangedListener(b);
    }

    /**
     *
     *
     * TODO To change the template for this generated type comment go to
     * Window - AppPreferences - Java - Code Generation - Code and Comments
     */
    private class ActionChangedListener implements PropertyChangeListener
    {
        JMenuItem menuItem;
        ActionChangedListener(JMenuItem mi)
        {
            super();
            this.menuItem = mi;
        }
        public void propertyChange(PropertyChangeEvent e)
        {
            String propertyName = e.getPropertyName();
            if (e.getPropertyName().equals(Action.NAME))
            {
                String text = (String) e.getNewValue();
                menuItem.setText(text);
            } else if (propertyName.equals("enabled"))
            {
                Boolean enabledState = (Boolean) e.getNewValue();
                menuItem.setEnabled(enabledState.booleanValue());
            }
        }
    }

  /**
   * Creates a generic menu item
   */
    protected JMenuItem createMenuItem(final JMenu aMenu,
                                       final String aLabel,
                                       final String aMnemonic,
                                       final String aAccessibleDescription,
                                       final boolean aEnabled,
                                       final AbstractAction aAction)
    {
        JMenuItem mi = aMenu.add(new JMenuItem(aLabel));
        if (aMnemonic.length() > 0)
        {
            mi.setMnemonic(aMnemonic.charAt(0));
        }
        mi.getAccessibleContext().setAccessibleDescription(aAccessibleDescription);
        mi.addActionListener(aAction);
        if (aAction != null)
        {
          aAction.addPropertyChangeListener(createActionChangeListener(mi));
          aAction.setEnabled(aEnabled);
        }
        //mi.setEnabled(aEnabled);
        return mi;
    }

    /**
     *
     */
    public void testLogin()
    {
        DatabaseLoginDlg dl = new DatabaseLoginDlg(null, this);
        UIHelper.centerAndShow(dl);
    }

    //---------------------------------------------------------
    // DatabaseLoginListener Interface
    //---------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#loggedIn(java.lang.String, java.lang.String)
     */
    public void loggedIn(final String databaseName, final String userName)
    {
        SpecifyAppPrefs.initialPrefs();

        if (AppContextMgr.getInstance().setContext(databaseName, userName, false) == AppContextMgr.CONTEXT_STATUS.Error)
        {
            log.error("Problems setting AppResourceDefault!");
            System.exit(0);
        }
        //startup(databaseName, userName);
        
        if (false)
        {
            showAppPrefsEditor(true);
        }

        //preferences();  
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.DatabaseLoginListener#cancelled()
     */
    public void cancelled()
    {
        System.exit(0);
    }
    
    class PrepPanel extends JPanel
    {
        protected Preparation prep;
        protected JLabel     label    = null;
        protected JLabel     label2    = null;
        protected JSpinner   spinner; 

        public PrepPanel(Preparation prep)
        {
            super();
            this.prep = prep;
            
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
     
            PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("max(120px;p),2px,max(50px;p),2px,p:g", "c:p"), this);
            CellConstraints cc      = new CellConstraints();
            
            
            pbuilder.add(label = new JLabel(prep.getPrepType().getName()), cc.xy(1,1));
            label.setOpaque(false);
            
            //JPanel contentPanel = new JPanel();
            //contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            
            int count       = prep.getCount();
            int quantityOut = 0;
            
            if (prep.getLoanPhysicalObjects().size() > 0)
            {
                for (LoanPhysicalObject lpo : prep.getLoanPhysicalObjects())
                {
                    int quantityLoaned   = lpo.getQuantity();
                    int quantityReturned = lpo.getQuantityReturned();
                    
                    quantityOut = quantityLoaned - quantityReturned;
                }
            }
            
            int quantityAvailable = count - quantityOut;
            if (quantityAvailable > 0)
            {
                SpinnerModel model = new SpinnerNumberModel(0, //initial value
                                           0, //min
                                           quantityAvailable, //max
                                           1);                //step
                spinner = new JSpinner(model);
                pbuilder.add(spinner, cc.xy(3, 1));
                pbuilder.add(label2 = new JLabel(" of " + Integer.toString(quantityAvailable)), cc.xy(5, 1));
                
                
            } else
            {
                pbuilder.add(label2 = new JLabel("(None Available)"), cc.xywh(3, 1, 3, 1));
            }
            //pbuilder.add(contentPanel, cc.xy(1,3));
        }
        
        public void setEnabled(final boolean enabled)
        {
            if (label != null)
            {
                label.setEnabled(enabled);
            }
            if (label2 != null)
            {
                label2.setEnabled(enabled);
            }
            if (spinner != null)
            {
                spinner.setEnabled(enabled);
            }
        }
        
        public int getCount()
        {
            if (spinner != null)
            {
                Object valObj = spinner.getValue();
                return valObj == null ? 0 : ((Integer)valObj).intValue();
                
            } else
            {
                return 0;
            }
        }
    }
    

    class ColObjPanel extends JPanel
    {
        protected CollectionObject colObj;
        protected JCheckBox checkBox;
        protected Vector<PrepPanel> panels = new Vector<PrepPanel>();
        
        
        /**
         * @param colObj
         */
        public ColObjPanel(CollectionObject colObj)
        {
            super();
            
            this.colObj = colObj;
            
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
            //setBorder(new CurvedBorder(new Color(160,160,160)));
     
            PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("f:p:g", "p,5px,p"), this);
            CellConstraints cc      = new CellConstraints();
     
            String taxonName = "";
            for (Determination deter : colObj.getDeterminations())
            {
                if (deter.getStatus().getDeterminationStatusId() == null ? deter.getStatus().getName().equals("Current") : deter.getStatus().getDeterminationStatusId() == 1)
                {
                    if (deter.getTaxon().getFullName() == null)
                    {
                        Taxon parent = deter.getTaxon().getParent();
                        String genus = parent.getFullName() == null ? parent.getName() : parent.getFullName();
                        taxonName = genus + " " + deter.getTaxon().getName();
                        
                    } else
                    {
                        taxonName = deter.getTaxon().getFullName();
                    }

                    break;
                }
            }
            String descr = String.format("%6.0f - %s", new Object[]{colObj.getCatalogNumber(), taxonName});
            
            
            pbuilder.add(checkBox = new JCheckBox(descr), cc.xy(1,1));
            //builder.add(new JLabel(String.format("%6.0f", new Object[]{colObj.getCatalogNumber()})), cc.xy(1,1));
            checkBox.setSelected(true);
            
            JPanel outerPanel = new JPanel();
            outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
            outerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            outerPanel.add(contentPanel);
            
            Color[] colors = new Color[] { new Color(255,255,255), new Color(245,245,255)};
            
            int i = 0;
            for (Preparation prep : colObj.getPreparations())
            {
                PrepPanel pp = new PrepPanel(prep);
                panels.add(pp);
                pp.setBackground(colors[i % 2]);
                contentPanel.add(pp);
                i++;
            }
            pbuilder.add(outerPanel, cc.xy(1,3));
            
            checkBox.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    for (PrepPanel pp : panels)
                    {
                        pp.setEnabled(checkBox.isSelected());
                    }
                    System.out.println(getNewLoanCount());
                    repaint();
                }
            });
        }
        
        public int getNewLoanCount()
        {
            int count = 0;
            if (checkBox.isSelected())
            {
                for (PrepPanel pp : panels)
                {
                    count += pp.getCount();
                }
            }
            return count;
        }
    }
    
    protected void createLoanSelector()
    {
        List<Object> dataObjs = BuildSampleDatabase.createSingleDiscipline("fish", "fish");
        
        List<CollectionObject> colObjs = (List<CollectionObject>)BuildSampleDatabase.getObjectsByClass(dataObjs, CollectionObject.class);
        
        if (true)
        {
            LoanSelectPrepsDlg dlg = new LoanSelectPrepsDlg(colObjs);
            UIHelper.centerAndShow(dlg);
            return;
        }
        
        JPanel mainPanel = new JPanel();
        
        //mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));     
        //mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("f:p:g", UIHelper.createDuplicateJGoodiesDef("p", "1px,p,4px,", (colObjs.size()*2)-1)), mainPanel);
        CellConstraints cc      = new CellConstraints();

        //mainPanel.setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), mainPanel.getBorder()));
 
        int i = 0;
        int y = 1;
        for (CollectionObject co : colObjs)
        {
            if (i > 0)
            {
                pbuilder.addSeparator("", cc.xy(1,y));
            }
            y += 2;
            
            ColObjPanel p = new ColObjPanel(co);
            //mainPanel.add(p);
            pbuilder.add(p, cc.xy(1,y));
            y += 2;
            i++;
            if (i > 2)
                break;
        }
        
        
        
        JFrame frame = new JFrame("Select Preparations");
        frame.setContentPane(new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        //frame.setIconImage(IconManager.getIcon("AppIcon", IconManager.IconSize.Std16).getImage());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.pack();

        UIHelper.centerAndShow(frame);

        
    }


    /**
     * @param args args
     */
    public static void main(String[] args)
    {

        // Create Specify Application
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
  /*
JCollapsiblePane cp = new JCollapsiblePane();

// JCollapsiblePane can be used like any other container
cp.setLayout(new BorderLayout());

// the Controls panel with a textfield to filter the tree
JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
controls.add(new JLabel("Search:"));
controls.add(new JTextField(10));
controls.add(new JButton("Refresh"));
controls.setBorder(new TitledBorder("Filters"));
cp.add("Center", controls);

JFrame frame = new JFrame();
frame.setLayout(new BorderLayout());

// Put the "Controls" first
frame.add("North", cp);

// Then the tree - we assume the Controls would somehow filter the tree
JScrollPane scroll = new JScrollPane(new JTree());
frame.add("Center", scroll);

// Show/hide the "Controls"
JButton toggle = new JButton(cp.getActionMap().get(JCollapsiblePane.TOGGLE_ACTION));
toggle.setText("Show/Hide Search Panel");
frame.add("South", toggle);

frame.pack();
frame.setVisible(true);
*/
                //FormEditor formEditor = new FormEditor();
                //formEditor.createLoanSelector();
                String text = "<table><tr><td>XXXX</rd></tr></table>";
                File excelFile = new File("collection_items_64367.tmp.xls");
                EMailHelper.sendMsg("imap.ku.edu", "rods", "Vintage1601*", "rods@ku.edu", "rods@ku.edu", 
                        "Info Request", text, EMailHelper.HTML_TEXT, excelFile);
            }
      });


    }

    class ViewFileNode implements TreeNode
    {
        protected ViewFileNode parent;
        protected String name;
        protected File   file;
        protected Vector<ViewFileNode> kids = new Vector<ViewFileNode>();
        
        public ViewFileNode(final String name, final File file)
        {
            super();
            this.name = name;
            this.file = file;
        }
        
        public ViewFileNode addKid(final String nameArg, final File fileArg)
        {
            ViewFileNode node = new ViewFileNode(nameArg, fileArg);
            kids.add(node);
            return node;
        }

        public void addKid(final ViewFileNode node)
        {
            kids.add(node);
        }

        public Vector<ViewFileNode> getKids()
        {
            return kids;
        }

        public String getName()
        {
            return name;
        }

        public File getFile()
        {
            return file;
        }

        public void setFile(File file)
        {
            this.file = file;
        }
        
        public void setParent(ViewFileNode parent)
        {
            this.parent = parent;
        }

        ///////////////////////////
        // TreeNode
        ///////////////////////////
        public Enumeration children()
        {
            return kids.elements();
        }
        public boolean getAllowsChildren()
        {
            return true;
        }
        public TreeNode getChildAt(int childIndex)
        {
            return kids.elementAt(childIndex);
        }
        public int getChildCount()
        {
            return kids.size();
        }
        public int getIndex(TreeNode node)
        {
            return kids.indexOf(node);
        }
        public TreeNode getParent()
        {
            return parent;
        }
        public boolean isLeaf()
        {
            return kids.size() == 0;
        }
        
        public String toString()
        {
            return name;
        }
    }
    
    public class ChooseVSM extends JDialog implements ActionListener
    {
        protected JButton        cancelBtn;
        protected JButton        okBtn;
        protected boolean        isCancelled = false;
        protected JTree          tree        = null;
        protected ViewFileNode   selectedNode = null;
        
         /**
         * Constructor.
         */
        public ChooseVSM(final Frame frame, final String title, final ViewFileNode root)
        {
            super(frame, title);
            createUI(root);
        }
        
        /**
         * Creates a list of Comboxes for setting the logging.
         */
        protected void createUI(final ViewFileNode root)
        {
            //PanelBuilder    builder = new PanelBuilder(new FormLayout("p,2px,p", UIHelper.createDuplicateJGoodiesDef("p", "4px", loggers.size())));
            //CellConstraints cc      = new CellConstraints();
            
            tree = new JTree(root);
            tree.expandPath(new TreePath(root));

            
            MouseListener ml = new MouseAdapter() {
                public void mousePressed(MouseEvent e) 
                {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    if(selRow != -1) {
                        if(e.getClickCount() == 1) 
                        {
                            selectedNode = (ViewFileNode)selPath.getLastPathComponent();
                        }
                        else if(e.getClickCount() == 2) 
                        {
                            selectedNode = (ViewFileNode)selPath.getLastPathComponent();
                            setVisible(false);
                        }
                    }
                }
            };
            tree.addMouseListener(ml);
            
            // Bottom Button UI
            cancelBtn = new JButton(getResourceString("Cancel"));
            okBtn     = new JButton(getResourceString("OK"));

            
            okBtn.addActionListener(this);
            okBtn.setEnabled(false);
            
            getRootPane().setDefaultButton(okBtn);

            ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
            btnBuilder.addGlue();
            btnBuilder.addGriddedButtons(new JButton[] { cancelBtn, okBtn });
           
            
            okBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    setVisible(false);
                }
            });

            cancelBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    isCancelled = true;
                    setVisible(false);
                }
            });

     
            
            //Dimension size = builder.getPanel().getPreferredSize();
            //size.width  += 15;
            //builder.getPanel().setPreferredSize(size);
            
            PanelBuilder outerPanel = new PanelBuilder(new FormLayout("p:g", "min(400px;p):g,10px,p"));
            outerPanel.add(new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), cc.xy(1,1));
            outerPanel.add(btnBuilder.getPanel(), cc.xy(1, 3));
            outerPanel.getPanel().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setContentPane(outerPanel.getPanel());
            
            setTitle("View Sets");
            
            //setLocationRelativeTo(UICacheManager.get(UICacheManager.FRAME));
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            pack();
        }
        
        /**
         * Sets the enabled/disabled state for the OK button.
         */
        protected void updateUI()
        {
            boolean enable = false;

            okBtn.setEnabled(enable);
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            // Handle clicks on the OK and Cancel buttons.
           setVisible(false);
        }

        public boolean isCancelled()
        {
            return isCancelled;
        }

        public File getSelectedNode()
        {
            return selectedNode.getFile();
        }
        
        
     }

}
