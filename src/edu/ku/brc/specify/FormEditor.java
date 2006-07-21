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
package edu.ku.brc.specify;


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
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createLocality;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPrepType;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPreparation;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createPreparationAttr;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createSpecifyUser;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createTaxonTreeDef;
import static edu.ku.brc.specify.tests.ObjCreatorHelper.createUserGroup;
import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.AttributeIFace;
import edu.ku.brc.specify.datamodel.CatalogSeries;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Collectors;
import edu.ku.brc.specify.datamodel.DataType;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.dbsupport.DBConnection;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.helpers.EMailHelper;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.prefs.PrefMainPanel;
import edu.ku.brc.specify.tests.CreateTestDatabases;
import edu.ku.brc.specify.tests.forms.TestDataObj;
import edu.ku.brc.specify.tests.forms.TestDataSubObj;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.db.DatabaseLoginDlg;
import edu.ku.brc.ui.db.DatabaseLoginListener;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.ViewMgr;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.View;

/**
 * The stand alone part of the FormEditor (this is a prototype at the moment that is used for viewing forms)
 *
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



    public FormEditor()
    {
        ViewMgr.setAsDefaultViewSet("Fish Views");
        
    }

    /**
     * @param disciplineName fish, birds, bees etc
     * @return true on success
     */
    public CollectionObject[] createSingleDiscipline(final String disciplineName)
    {
        UserGroup        userGroup        = createUserGroup(disciplineName);
        SpecifyUser      user             = createSpecifyUser("John", "Doe", (short)0, userGroup);
        DataType         dataType         = createDataType(disciplineName);


        TaxonTreeDef     taxonTreeDef     = createTaxonTreeDef("TreeDef");
        CollectionObjDef collectionObjDef = createCollectionObjDef(disciplineName, dataType, user, taxonTreeDef);

        Geography[] geographies = createGeographies(collectionObjDef, "GeoTree");

        Locality[] localities = new Locality[2];
        localities[0] = createLocality("This is the place", geographies[0]);
        localities[1] = createLocality("My Private Forest", geographies[1]);

        Location[] locations = createLocations(collectionObjDef, "GLocationTree");
        Taxon[]    taxonomy  = createTaxonomy(taxonTreeDef);

        Agent[] agents = createAgentsInMemory();

        CatalogSeries catalogSeries = createCatalogSeries("KUFSH", "Fish");


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
        // Create Determination
        for (int i=0;i<colObjs.length;i++)
        {
            for (int j=0;j<i+2;j++)
            {
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(1990-i, 11-i, 28-(i+j));
                createDetermination(colObjs[i], agents[agentInx % agents.length], taxonomy[taxonInx % taxonomy.length], j == 0, cal);
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
        int numDataTypes = BasicSQLUtils.getNumRecords(DBConnection.getConnection(), "datatype");
        if (numDataTypes == 0)
        {
            String[] dataTypeNames = {"Animal", "Plant", "Fungi", "Mineral", "Other"};


            try
            {
                Session session = HibernateUtil.getCurrentSession();
                HibernateUtil.beginTransaction();

                for (String name : dataTypeNames)
                {
                    DataType dataType = new DataType();
                    dataType.setName(name);
                    dataType.setCollectionObjDef(null);
                    session.save(dataType);

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
            }
        } else
        {
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(DataType.class);
            criteria.add(Expression.eq("name", "Animal"));
            java.util.List list = criteria.list();
            for (Object obj : list)
            {
                DataType dataType = (DataType)obj;
                //System.out.println(dataType.getName());
                retDataType = dataType;
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
        multiView   = new MultiView(null, view, AltView.CreationMode.Edit, true, true);
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

            if (currViewSetName.equals("Fish Views") && currViewName.equals("Accession"))
            {
                boolean doDB = true;
                if (doDB)
                {
                    Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Accession.class).setMaxResults(10);
                    //Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Accession.class).setFetchMode(Accession.class.getName(), FetchMode.DEFAULT).setMaxResults(300);
                    java.util.List list = criteria.list();//session.find("from collev");
                    dataObj = list;

                } else
                {
                    Accession[] accessions = CreateTestDatabases.createAccessionsInMemory();
                    Vector<Object> list = new Vector<Object>();
                    for (Accession accession : accessions)
                    {
                        list.add(accession);
                    }
                    dataObj = list;
                }
                multiView.setData(dataObj);
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


            if (currViewSetName.equals("Fish Views") && currViewName.equals("Collection Object"))
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


                boolean doCatalogItems = true;
                if (doCatalogItems)
                {
                    Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(CollectionObject.class).setMaxResults(300);
                    //criteria.add(Expression.isNull("derivedFromId"));


                    java.util.List data = criteria.list();
                    //System.out.println("Items Returned: "+data.size());

                    dataObj = data;
                }

                boolean doMemoryCollection = false;
                if (doMemoryCollection)
                {
                    CollectionObject[] colObjs = createSingleDiscipline("Fish");

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
       // ViewMgr.reset();

        //fvo = createForm(ViewMgr.getView(currViewSetName, currViewName));
    }

    /**
     *
     */
    protected void selectForm()
    {
        /*

        List<FormView>    fullFormsList = new ArrayList<FormView>();

        for (ViewSet viewSet : ViewMgr.getViewSets())
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
   private void initialize()
   {
       UICacheManager.getInstance(); // initializes it first thing
       
       AppPrefs.initialPrefs(); // Must be done first thing!
       
       try
       {
           //System.out.println(System.getProperty("os.name"));

           if (!System.getProperty("os.name").equals("Mac OS X"))
           {
               UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
               //PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
               //UIManager.setLookAndFeel(new WindowsLookAndFeel());
               //UIManager.setLookAndFeel(new com.jgoodies.looks.windows.WindowsLookAndFeel());
               PlasticLookAndFeel.setCurrentTheme(new ExperienceBlue());
               //PlasticLookAndFeel.setPlasticTheme(new ConfigurableTheme());
               //PlasticLookAndFeel.setMyCurrentTheme(new ConfigurableTheme());
           }

           //UIManager.setLookAndFeel(new PlasticLookAndFeel());
           //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
           //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
           
           
           // Note: This is asynchronous
           UIHelper.doLogin(false, false, this); // true means do auto login if it can, second true means use dialog


       }
       catch (Exception e)
       {
           log.error("Can't change L&F: ", e);
       }
   }

   /**
   * Create the GUI and show it.  For thread safety,
   * this method should be invoked from the
   * event-dispatching thread.
   */
  private void startup()
  {
        
        for (int i=0;i<10;i++)
        {
            testDataObj = new TestDataObj();

            Set<Object> set = new HashSet<Object>();
            for (int j=0;j<4;j++)
            {
                TestDataSubObj subObj = new TestDataSubObj();
                subObj.setTextField("Sub Obj Item #"+Integer.toString(j));
                set.add(subObj);
            }

            if (i == 2)
            {
                testDataObj.setImagePathURL("");
            }

            testDataObj.setSubObjects(set);
            testDataObj.setTextField("Item #"+Integer.toString(i));
            list.add(testDataObj);
        }

        dataObj = list;




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

        //currViewName      = "Collection Object";

        currViewName      = "FishBase";
        currViewSetName =   "Fish Views";

        currViewName      = "Accession";
        currViewSetName =   "Fish Views";

        View view = ViewMgr.getView(currViewSetName, currViewName);

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
            menu = (JMenu) menuBar.add(new JMenu(getResourceString(labelKey)));
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
     * Window - Preferences - Java - Code Generation - Code and Comments
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
        JMenuItem mi = (JMenuItem) aMenu.add(new JMenuItem(aLabel));
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
        DatabaseLoginDlg dl = new DatabaseLoginDlg(this);
        UIHelper.centerAndShow(dl);
    }
    
    //---------------------------------------------------------
    // DatabaseLoginListener Interface
    //---------------------------------------------------------
    
    public void loggedIn()
    {
        startup();
    }
    
    public void cancelled()
    {
        System.exit(0);
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
                FormEditor formEditor = new FormEditor();
                formEditor.initialize();    
            }
      });

        
    }

    
}
