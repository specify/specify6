/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.utilapps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.helpers.BrowserLauncher;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.CustomFrame;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Nov 27, 2008
 *
 */
public class RegisterApp extends JPanel
{
    private enum CountType {Divs, Disps, Cols}
    private enum DateType  {None, Date, Monthly, Yearly, Time}
    
    protected String                    connectStr = "jdbc:mysql://specify6-test.nhm.ku.edu/stats";
    protected String                    username   = "rods";
    protected String                    password   = "rods";

    protected boolean                   doLocal = true;
    protected JFrame                    frame;
    protected String                    title = "Registration and Statistics Tool";
    protected JTree                     tree;
    protected JTable                    propsTable;
    
    protected Hashtable<String, String> fullDescHash = null;
    
    protected JList                     trackItemsList;
    protected Hashtable<String, Hashtable<String, Integer>> trackUsageHash;
    
    //protected Hashtable<String, Boolean> viewsHash     = new Hashtable<String, Boolean>();
    //protected Hashtable<String, Boolean> dbCmdsHash    = new Hashtable<String, Boolean>();
    
    protected CustomFrame               chartFrame    = null;
    protected Hashtable<String, String> trackDescHash = new Hashtable<String, String>();

    protected CustomFrame               classFrame    = null;
    protected JList                     classList;
    
    protected RegProcessor              rp = new RegProcessor();
    
    protected Comparator<Pair<String, Integer>> countComparator = null;
    protected Comparator<Pair<String, Integer>> titleComparator  = null;
    
    /**
     * 
     */
    public RegisterApp()
    {
        super(new BorderLayout());
        
        DBConnection dbConn = DBConnection.getInstance();
        dbConn.setConnectionStr(connectStr);
        dbConn.setDatabaseName("stats");
        dbConn.setUsernamePassword(username, password);
        dbConn.setDriver("com.mysql.jdbc.Driver");
        
        createUI();
        
        for (Pair<String, String> p : rp.getTrackKeyDescPairs())
        {
            trackDescHash.put(p.first, p.second);
        }
        
        countComparator = new Comparator<Pair<String, Integer>>() {
            @Override
            public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2)
            {
                if (o1.second != null && o2.second != null)
                {
                    return o1.second.compareTo(o2.second);
                }
                return 0;
            }
        };
        titleComparator = new Comparator<Pair<String, Integer>>() {
            @Override
            public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2)
            {
                return o1.first.compareTo(o2.first);
            }
        };
    }

    /**
     * 
     */
    protected void createUI()
    {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        rp = new RegProcessor();
        try
        {
            boolean doBuild = false;
            if (doBuild)
            {
                rp.process(doLocal ? new File("reg.dat") : rp.getDataFromWeb("SpReg.REGISTER_URL", true));
                rp.processTracks(doLocal ? new File("track.dat") : rp.getDataFromWeb("StatsTrackerTask.URL", true));
                //rp.mergeStats();
            } else
            {
                rp.processSQL();
            }
            
            tree       = new JTree(rp.getRoot(false));
            propsTable = new JTable();
            
            tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e)
                {
                    fillPropsTable();
                }
            });
            tree.setCellRenderer(new MyTreeCellRenderer());
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        Component topComp = UIHelper.createScrollPane(tree);
        Component botComp = UIHelper.createScrollPane(propsTable);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topComp, botComp);

        splitPane.setDividerLocation(0.5);
        splitPane.setOneTouchExpandable(true);

        splitPane.setLastDividerLocation(300);
        Dimension minimumSize = new Dimension(200, 400);
        topComp.setMinimumSize(minimumSize);
        botComp.setMinimumSize(minimumSize);

        tabbedPane.add("Registration", splitPane);
        
        final List<Pair<String, String>> trackDescPairs = rp.getTrackKeyDescPairs();
        Vector<String> trkItems = new Vector<String>();
        for (Pair<String, String> p : trackDescPairs)
        {
            trkItems.add(p.second);
        }
        
        //Collections.sort(trkItems);
        final JList list = new JList(trkItems);
        //pb.add(UIHelper.createScrollPane(list), cc.xy(1, 1));
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    int inx = list.getSelectedIndex();
                    if (inx > -1)
                    {
                        fillUsageItemsList(trackDescPairs.get(inx).first);
                    }
                }
            }
        });
        
        tabbedPane.add("Reg Stats", getStatsPane("Registration", rp.getRegNumHash().values()));
        
        DefaultListModel model = new DefaultListModel();
        trackItemsList = new JList(model);
        
        topComp = UIHelper.createScrollPane(list);
        botComp = UIHelper.createScrollPane(trackItemsList);
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topComp, botComp);
        splitPane.setDividerLocation(0.5);
        splitPane.setOneTouchExpandable(true);

        topComp.setMinimumSize(minimumSize);
        botComp.setMinimumSize(minimumSize);
        
        splitPane.setDividerLocation(0.5);
        trackUsageHash = rp.getTrackCatsHash();
        tabbedPane.add("Usage Tracking", splitPane);
        
        tabbedPane.add("User Stats", getStatsPane("Tracking", rp.getTrackIdHash().values()));
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * @param catName
     */
    protected void fillUsageItemsList(final String catName)
    {

        //String sql = "SELECT Name, Total FROM (SELECT Name, SUM(CountAmt) as Total FROM trackitem WHERE SUBSTRING(Name, 1, 8) = 'Usage_"+catName+"' OR SUBSTRING(Name, 1, 2) = '"+catName+"' GROUP BY Name) AS T1 ORDER BY Total";
        String sql = "SELECT Name, Total FROM (SELECT Name, SUM(CountAmt) as Total FROM trackitem WHERE SUBSTRING(Name, 1, 8) = 'Usage_"+catName+"' GROUP BY Name) AS T1 ORDER BY Total";
        System.out.println(sql);
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        
        DefaultListModel model = (DefaultListModel)trackItemsList.getModel();
        if (rows != null && rows.size() > 0)
        {
            boolean isCatOK     = catName.length() == 2;
            boolean isDataEntry = catName.equals("DE");
            boolean isDatabase  = catName.equals("DB");
            
            Hashtable<String, Boolean> clsHash = new Hashtable<String, Boolean>();
            
            Vector<Pair<String, Integer>> values = new Vector<Pair<String,Integer>>();
            int total = 0;
            for (Object[] colData : rows)
            {
                String  name = (String)colData[0];
                int     val  = ((BigDecimal)colData[1]).intValue();
                
                if (name.startsWith("Usage_"))
                {
                    name = name.substring(6);
                }
                
                String str;
                if (isCatOK && name.charAt(2) == '_')
                {
                    str = name.substring(catName.length()+1);
                    /*if (str.startsWith(catName) && str.charAt(2) == '_')
                    {
                        str = str.substring(catName.length()+1);
                    }*/
                    if (str.startsWith("VIEW_"))
                    {
                        str = str.substring(5);
                    }
                    
                    if (isDataEntry)
                    {
                        String clsName = str;
                        if (clsName.endsWith("_RS"))
                        {
                            clsName = clsName.substring(0, clsName.length()-3);
                        }
                        clsHash.put(clsName, true);
                        
                    } else if (isDatabase)
                    {
                        int inx = str.lastIndexOf('_');
                        String clsName = str.substring(inx+1);
                        clsHash.put(clsName, true);
                    }

                } else
                {
                    str = name;
                }
                values.add(new Pair<String, Integer>(str, val));
                total += val;
            }
            
            Collections.sort(values, countComparator);
            
            createBarChart(trackDescHash.get(catName), "Actions", values);
            
            // Fill Model
            model.clear();
            sql  = "SELECT DISTINCT(Name) FROM trackitem WHERE SUBSTRING(Name, 1, 8) = 'Usage_"+catName+"' ORDER BY Name";
            rows = BasicSQLUtils.query(sql);
            if (rows != null && rows.size() > 0)
            {
                for (Object[] colData : rows)
                {
                    model.addElement(colData[0]);
                }
            }
            
            if (clsHash.size() > 0)
            {
                showClassList(clsHash);
                
            } else if (classFrame != null && classFrame.isVisible())
            {
                classFrame.setVisible(false);
            }
            
        } else
        {
            model.clear();
            if (chartFrame != null && chartFrame.isVisible())
            {
                chartFrame.setVisible(false);
            }
        }
    }
    
    /**
     * @param clsHash
     */
    private void showClassList(final Hashtable<String, Boolean> clsHash)
    {
        boolean wasVisible = false;
        boolean wasCreated = false;
        if (classFrame == null)
        {
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
            
            classList = new JList(new DefaultListModel());
            //pb.add(UIHelper.createLabel("Unused Classes"), cc.xy(1,1));
            pb.add(UIHelper.createScrollPane(classList),   cc.xy(1,1));
            
            pb.setDefaultDialogBorder();
            
            classFrame = new CustomFrame("Used Classes", CustomFrame.OK_BTN, pb.getPanel());
            classFrame.setOkLabel("Close");
            classFrame.createUI();
            wasCreated = true;
            
        } else
        {
            wasVisible = classFrame.isVisible();
        }
        
        Hashtable<String, Boolean> allClassesHash = new Hashtable<String, Boolean>();
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            allClassesHash.put(ti.getShortClassName(), true);
        }
        
        for (String key : clsHash.keySet())
        {
            allClassesHash.remove(key);
        }
        
        ((DefaultListModel)classList.getModel()).clear();
        TreeSet<String> keys = new TreeSet<String>(allClassesHash.keySet());
        for (String key : keys)
        {
            ((DefaultListModel)classList.getModel()).addElement(key);
        }
        
        // Can't believe I have to do this
        Rectangle r = classFrame.getBounds();
        r.height++;
        classFrame.setBounds(r);
        r.height--;
        //----
        classFrame.setBounds(r);
        
        if (wasCreated)
        {
            classFrame.pack();
        }
        
        if (!wasVisible)
        {
            UIHelper.centerAndShow(classFrame);
        }
    }
    
    /**
     * @param entries
     * @return
     */
    private Vector<String> getKeyWordsList(final Collection<RegProcEntry> entries)
    {
        Properties props = new Properties();
        Hashtable<String, Boolean> statKeywords = new Hashtable<String, Boolean>();
        for (RegProcEntry entry : entries)
        {
            props.clear();
            props.putAll(entry.getProps());
            
            String os  = props.getProperty("os_name");
            String ver = props.getProperty("os_version");
            props.put("platform", os+" "+ver);
            
            for (Object keywordObj : props.keySet())
            {
                statKeywords.put(keywordObj.toString(), true);
            }
        }
        
        String[] trackKeys = rp.getTrackKeys();
        
        for (String keyword : new Vector<String>(statKeywords.keySet()))
        {
            if (keyword.startsWith("Usage") || 
                keyword.startsWith("num_") || 
                keyword.endsWith("_portal") || 
                keyword.endsWith("_number") || 
                keyword.endsWith("_website") || 
                keyword.endsWith("id") || 
                keyword.endsWith("os_name") || 
                keyword.endsWith("os_version"))
            {
                statKeywords.remove(keyword);
                
            } else 
            {
                for (String key : trackKeys)
                {
                    if (keyword.startsWith(key))
                    {
                        statKeywords.remove(keyword); 
                    }
                }
            }
        }
        
        statKeywords.remove("date");
        //statKeywords.remove("time");
        statKeywords.put("by_date",  true);
        statKeywords.put("by_month", true);
        statKeywords.put("by_year",  true);
        
        return new Vector<String>(statKeywords.keySet());
    }
    
    /**
     * @return
     */
    private JPanel getStatsPane(final String chartPrefixTitle, 
                                final Collection<RegProcEntry> entries)
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        
        final Hashtable<String, String> keyDescPairsHash  = rp.getAllDescPairsHash();
        final Hashtable<String, String> desc2KeyPairsHash = new  Hashtable<String, String>();
        for (String key : keyDescPairsHash.keySet())
        {
            desc2KeyPairsHash.put(keyDescPairsHash.get(key), key); 
        }

        Vector<String> keywords = new Vector<String>();
        for (String keyword : getKeyWordsList(entries))
        {
            if (keyword.endsWith("_name") || 
                    keyword.endsWith("_type") || 
                    keyword.endsWith("ISA_Number") || 
                    keyword.endsWith("reg_isa"))
            {
                //keywords.add(keyword);
                
            } else
            {
                String desc = keyDescPairsHash.get(keyword);
                //System.out.println("["+keyword+"]->["+desc+"]");
                keywords.add(desc);
            }
        }
        Vector<Object[]> rvList = BasicSQLUtils.query("SELECT DISTINCT(Name) FROM registeritem WHERE SUBSTRING(Name, 1, 4) = 'num_'");
        for (Object[] array : rvList)
        {
            keywords.add(array[0].toString());
        }
        Collections.sort(keywords);
        
        final JList list = new JList(keywords);
        pb.add(UIHelper.createScrollPane(list),  cc.xy(1,1));
        
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    String statName = (String)list.getSelectedValue();
                    if (desc2KeyPairsHash.get(statName) != null)
                    {
                        statName = desc2KeyPairsHash.get(statName);
                    }
                    
                    DateType dateType = convertDateType(statName);
                    if (dateType == DateType.None)
                    {
                        Vector<Pair<String, Integer>> values;
                        if (statName.startsWith("num_"))
                        {
                            values = getCollNumValuesFromList(statName);
                            Hashtable<String, Boolean>    hash   = new Hashtable<String, Boolean>();
                            for (Pair<String, Integer> p : values)
                            {
                                if (hash.get(p.first) == null)
                                {
                                    hash.put(p.first, true);
                                } else
                                {
                                    int i = 0;
                                    String name = p.first;
                                    while (hash.get(p.first) != null)
                                    {
                                        p.first = name +" _" + i;
                                        i++;
                                    }
                                    hash.put(p.first, true);
                                }
                                //p.first += "(" + p.second.toString() + ")";
                            }
                            
                            
                        } else
                        {
                            values = getCollValuesFromList(statName);
                        }
                        
                        Collections.sort(values, countComparator);
                        Vector<Pair<String, Integer>> top10Values = new Vector<Pair<String,Integer>>();
                        for (int i=1;i<Math.min(11, values.size());i++)
                        {
                            top10Values.insertElementAt(values.get(values.size()-i), 0);
                        }
                        createBarChart(chartPrefixTitle + " "+ statName, statName, top10Values);
                        
                    } else
                    {
                        String                        desc   = getByDateDesc(dateType);
                        Vector<Pair<String, Integer>> values = getDateValuesFromList(dateType);
                        Collections.sort(values, titleComparator);
                        createBarChart(chartPrefixTitle + " "+ desc, desc, values);
                    }
                }
            }
        });

        return pb.getPanel();
    }
    
    protected Vector<Pair<String, Integer>> getCollValuesFromList(final String statName)
    {
        String sql = "SELECT Value, COUNT(Value) AS CNT FROM registeritem WHERE Name = '" + statName + "' GROUP BY Value ORDER BY CNT";
        System.err.println(sql);
        Vector<Pair<String, Integer>> values = new Vector<Pair<String,Integer>>();
        
        Vector<Object[]> list = BasicSQLUtils.query(sql);
        for (int i=0;i<list.size();i++)
        {
            Object[] row = list.get(i);
            values.add(new Pair<String, Integer>(row[0].toString(), ((Long)row[1]).intValue()));
        }
        return values;        

    }
    
    protected Vector<Pair<String, Integer>> getCollNumValuesFromList(final String statName)
    {
        String sql = "SELECT RegisterID, Value, CountAmt FROM registeritem WHERE Name = '" + statName + "' OR Name = 'Collection_name' ORDER BY RegisterID";
        
        Vector<Pair<String, Integer>> values = new Vector<Pair<String,Integer>>();
        
        Vector<Object[]> list = BasicSQLUtils.query(sql);
        for (int i=0;i<list.size();i++)
        {
            Object[] row1 = list.get(i);
            i++;
            if (i < list.size())
            {
                Object[] row2 = list.get(i);
                int id1 = (Integer)row1[0];
                int id2 = (Integer)row2[0];
                if (id1 == id2)
                {
                    String  desc  = (String)(row1[1] != null ? row1[1] : row2[1]);
                    Integer count = (Integer)(row1[2] != null ? row1[2] : row2[2]);
                    values.add(new Pair<String, Integer>(desc, count));
                } else
                {
                    i--;
                }
            }
        }
        return values;        
    }
    
    /**
     * @param name
     * @return
     */
    private DateType convertDateType(final String name)
    {
        if (name != null)
        {
            if (name.equals("by_date"))
            {
                return DateType.Date;
            }
            if (name.equals("by_month"))
            {
                return DateType.Monthly;
            }
            if (name.equals("by_year"))
            {
                return DateType.Yearly;
            }
            if (name.equals("time"))
            {
                return DateType.Time;
            }
        }
        return DateType.None;
    }
    
    /**
     * @param dateType
     * @return
     */
    private String getByDateDesc(final DateType dateType)
    {
        String[] desc = {"None", "By Date", "By Month", "By Year", "By Time"};
        return desc[dateType.ordinal()];
    }
    
    /**
     * @param dateType
     * @param srcList
     * @return
     */
    private Vector<Pair<String, Integer>> getDateValuesFromList(final DateType dateType)
    {
        String sql = "";
        switch (dateType)
        {
            case Time:
                sql = "SELECT hrs, COUNT(hrs) FROM (SELECT (hr + mn + sc) as hrs FROM (SELECT HOUR(TimestampCreated) as hr, ROUND(MINUTE(TimestampCreated) / 60) as mn, ROUND(SECOND(TimestampCreated) / 3600) as sc FROM track) AS T1) as T2 GROUP BY hrs";
                break;
                
            case Monthly:
                sql = "SELECT nm,COUNT(mon) FROM (SELECT MONTH(TimestampCreated) as mon, MONTHNAME(TimestampCreated) as nm FROM track) AS T1 GROUP BY mon";
                break;
                
            case Yearly:
                sql = "SELECT yr,count(yr) FROM (SELECT YEAR(TimestampCreated) as yr FROM track) AS T1 group by yr";
                break;
                
            case Date:
                sql = "SELECT dt,count(dt) FROM (SELECT DATE(TimestampCreated) as dt FROM track) AS T1 group by dt";
                break;
                
            case None:
                break;
        }
        
        Vector<Pair<String, Integer>> values = new Vector<Pair<String,Integer>>();
        for (Object[] colData : BasicSQLUtils.query(sql))
        {
            Long longVal = (Long)colData[1];
            String desc = "";
            if (colData[0] instanceof String)
            {
                desc = (String)colData[0];
                
            } else if (colData[0] instanceof Long)
            {
                Long val = (Long)colData[0]; 
                desc = val.toString();
                
            } else if (colData[0] instanceof Date)
            {
                Date val = (Date)colData[0]; 
                desc = val.toString();
                
            } else if (colData[0] instanceof Integer)
            {
                Integer val = (Integer)colData[0]; 
                desc = val.toString();
                
            } else if (colData[0] instanceof BigDecimal)
            {
                BigDecimal val = (BigDecimal)colData[0]; 
                desc = String.format("%02d", val.intValue());
                
            }  else if (colData[0] instanceof byte[])
            {
                //Byte val = (Byte)colData[0]; 
                desc = new String((byte[])colData[0]);
            } else
            {
                System.out.println(colData[0].getClass());
            }
            values.add(new Pair<String, Integer>(desc, longVal.intValue()));
        }
        return values;
    }
    
    /**
     * @param chartTitle
     * @param xTitle
     * @param values
     */
    private void createBarChart(final String chartTitle, 
                                final String xTitle,
                                final Vector<Pair<String, Integer>> values)
    {
        String cat = ""; //$NON-NLS-1$
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); 
        
        for (Pair<String, Integer> p : values)
        {
            //System.out.println("["+p.second+"]  ["+p.first+"]");
            //System.out.println("            values.add(new Pair<String, Integer>(\""+p.first+"\", "+p.second+"));");
            dataset.addValue(p.second, p.first, cat);
        }

        // create the chart... 
        JFreeChart chart = ChartFactory.createBarChart3D( 
                chartTitle,      // chart title 
                xTitle,     // domain axis label 
                "Occurrence", // range axis label 
                dataset,    // data 
                PlotOrientation.VERTICAL,
                true, //maxStrLen > 4 || values.size() > 15,       // include legend 
                true,       // tooltips? 
                false       // URLs? 
            ); 
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        
        // create and display a frame... 
        ChartPanel panel = new ChartPanel(chart, true, true, true, true, true); 
        
        boolean wasVisible = false;
        boolean wasCreated = false;
        if (chartFrame == null)
        {
            panel.setPreferredSize(new Dimension(800,600)); 
            chartFrame = new CustomFrame("Statistics", CustomFrame.OK_BTN, null);
            chartFrame.setOkLabel("Close");
            chartFrame.createUI();
            wasCreated = true;
        } else
        {
            wasVisible = chartFrame.isVisible();
        }
        
        chartFrame.setContentPane(panel);
        
        // Can't believe I have to do this
        Rectangle r = chartFrame.getBounds();
        r.height++;
        chartFrame.setBounds(r);
        r.height--;
        //----
        chartFrame.setBounds(r);
        
        if (wasCreated)
        {
            chartFrame.pack();
        }
        
        if (!wasVisible)
        {
            UIHelper.centerAndShow(chartFrame);
        }
    }
        
    /**
     * 
     */
    protected void fillPropsTable()
    {
        TreePath path = tree.getSelectionPath();
        if (path != null)
        {
            RegProcEntry rpe   = (RegProcEntry)path.getLastPathComponent();
            Properties   props = rpe.getProps();
         
            Vector<String> keys = new Vector<String>();
            for (Object obj : props.keySet())
            {
                keys.add(obj.toString());
            }
            Collections.sort(keys);
            
            if (fullDescHash == null)
            {
                fullDescHash = rp.getAllDescPairsHash();
            }
            
            Object[][] rows = new Object[keys.size()][2];
            int inx = 0;
            for (Object key : keys)
            {
                String titleStr = fullDescHash.get(key);
                if (titleStr == null)
                {
                    titleStr = key.toString();
                }
                rows[inx][0] = titleStr;
                
                rows[inx][1] = props.get(key);
                inx++;
            }
            propsTable.setModel(new DefaultTableModel(rows, new String[] {"Property", "Value"}));
        } else
        {
            propsTable.setModel(new DefaultTableModel());
        }
    }
    
    /**
     * @param inst
     * @return
     */
    private boolean hasReg(final RegProcEntry inst)
    {
        for (RegProcEntry div : inst.getKids())
        {
            for (RegProcEntry disp : div.getKids())
            {
                for (RegProcEntry col : disp.getKids())
                {
                    if (StringUtils.isNotEmpty(col.getISANumber()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * @param inst
     * @param type
     * @return
     */
    private int count(final RegProcEntry inst, 
                      final CountType type)
    {
        int divs = inst.getKids().size();
        
        int disps = 0;
        int cols  = 0;
        
        for (RegProcEntry div : inst.getKids())
        {
            disps += div.getKids().size();
            for (RegProcEntry disp : div.getKids())
            {
                cols += disp.getKids().size();
            }
        }
        
        switch (type)
        {
            case Divs:
                return divs; 
                
            case Disps:
                return disps; 
                
            case Cols:
                return cols; 
        }
        return 0;
    }
    
    /**
     * @param sb
     * @param tableTitle
     * @param entries
     * @param hasReg
     */
    private void createTable(final StringBuilder        sb, 
                             final String               tableTitle, 
                             final Vector<RegProcEntry> entries,
                             final boolean              hasReg)
    {
        Hashtable<String, RegProcEntry> collHash = rp.getCollectionHash();
        
        sb.append("<table class=\"brd\" border=\"0\" cellspacing=\"0\">\n");
        sb.append("<tr>");
        sb.append("<th colspan=\"8\">");
        sb.append(tableTitle);
        sb.append("</th>");
        sb.append("</tr>");
        sb.append("<tr><th>Institution</th><th>Division</th><th>Discipline</th><th>Collection</th>" +
                  "<th>ISA Number</th><th>Phone</th><th>EMail</th><th>Last Opened</th></tr>\n");
        
        for (RegProcEntry inst : entries)
        {
            if (hasReg(inst) != hasReg)
            {
                continue;
            }
            
            int cols = count(inst, CountType.Cols);
            sb.append("<tr>\n  <td valign=\"top\" rowspan=\""+cols+"\">"+inst.getName()+"</td>\n");
            
            int divsCnt = 0;
            for (RegProcEntry div : inst.getKids())
            {
                if (divsCnt > 0) sb.append("<tr>\n");
               
                int dispsCnt = 0;
                int disps = count(div, CountType.Disps);
                
                sb.append("  <td valign=\"top\" "+ (divsCnt == 0 ? " rowspan=\""+disps+"\"" : "") + ">"+div.getName()+"</td>\n");
                
                for (RegProcEntry disp : div.getKids())
                {
                    if (dispsCnt > 0) sb.append("<tr>\n");
                    sb.append("  <td valign=\"top\" "+ (dispsCnt == 0 ? " rowspan=\""+disp.getKids().size()+"\"" : "") + ">"+disp.getName()+"</td>\n");
                    int colsCnt = 0;
                    for (RegProcEntry col : disp.getKids())
                    {
                        
                        
                        RegProcEntry colEntry = collHash.get(col.get("reg_number"));
                        if (colEntry != null)
                        {
                            String       dateStr      = colEntry.get("date");
                            Date         lastUsedDate = new Date(rp.getDate(dateStr));
                            dateStr = rp.getDateFmt().format(lastUsedDate);
                            col.getProps().put("last_used_date", dateStr);
                        } else
                        {
                            col.getProps().put("last_used_date", "&nbsp;");
                        }

                        if (colsCnt > 0) sb.append("<tr>\n");
                        String isa = col.getISANumber();
                        sb.append("  <td>"+col.getName()+"</td>\n  <td>"+(StringUtils.isNotEmpty(isa) ? isa : "&nbsp;")+"</td>");
                        
                        String phone  = col.get("Phone");
                        String email  = col.get("User_email");
                        String ludate = col.get("last_used_date");
                        sb.append("<td>"+(StringUtils.isNotEmpty(phone) ? phone : "&nbsp;")+"</td>\n  <td>"+
                                   (StringUtils.isNotEmpty(email) ? email : "&nbsp;")+"</td>\n  <td>"+
                                   (StringUtils.isNotEmpty(ludate) ? ludate : "&nbsp;"));
                        sb.append("</tr>\n");
                        colsCnt++;
                    }
                    dispsCnt++;
                }
                divsCnt++;
            }
            sb.append("<tr>\n");
        }
        sb.append("</table>\n");
    }
    
    /**
     * 
     */
    public void createRegReport()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>\n<title>Registrations</title>\n");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://specify6.specifysoftware.org/css/report.css\">\n");
        sb.append("</head><body>\n<div style=\"text-align: left;\"><img src=\"http://specify6.specifysoftware.org/images/logosmaller.png\" height=\"42\" width=\"117\"></div>\n");

        createTable(sb, "ISA Registrations", rp.getRoot(false).getKids(), true);
        sb.append("<br><br>");
        createTable(sb, "No ISA Registrations", rp.getRoot(false).getKids(), false);
        sb.append("</body></html>");
        
        try
        {
            File file = new File("report.html");
            FileUtils.writeStringToFile(file, sb.toString());
            
            BrowserLauncher.openURL(file.toURI().toString());
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param sb
     * @param entries
     * @param key
     * @param desc
     * @param discipline
     * @return
     */
    protected int calcStat(final StringBuilder sb, 
                           final Collection<RegProcEntry> entries,
                           final String key, 
                           final String desc, 
                           final String discipline)
    {
        int total = 0;
        for (RegProcEntry entry : entries)
        {
            String       disp   = null;
            RegProcEntry parent = (RegProcEntry)entry.getParent();
            if (parent != null)
            {
                disp = parent.getName();
            }
            
            if (discipline == null || disp != null && discipline.equals(disp))
            {
                String val = entry.get(key);
                if (val != null)
                {
                    total += Integer.parseInt(val);
                }
            }
        }
        sb.append("<tr><td>");
        sb.append(desc);
        sb.append("</td><td align=\"right\">");
        sb.append(Integer.toString(total));
        sb.append("</td></tr>\n");
        return total;
    }
    
    /**
     * 
     */
    public void createStatsReport()
    {
        if (fullDescHash == null)
        {
            fullDescHash = rp.getAllDescPairsHash();
        }
        String[] statsArray = {"num_co",
                               "num_tx", 
                               "num_txu", 
                               "num_geo",
                               "num_geou", 
                               "num_loc", 
                               "num_locgr", 
                               "num_preps", 
                               "num_prpcnt", 
                               "num_litho", 
                               "num_lithou", 
                               "num_gtp", 
                               "num_gtpu"};
        
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>\n<title>Statistics</title>\n");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://specify6.specifysoftware.org/css/report.css\">\n");
        sb.append("</head><body>\n<div style=\"text-align: left;\"><img src=\"http://specify6.specifysoftware.org/images/logosmaller.png\" height=\"42\" width=\"117\"></div>\n");
        sb.append("<center\n<table class=\"brd\" border=\"0\" cellspacing=\"0\">\n");
        sb.append("<tr>");
        sb.append("<th colspan=\"2\">");
        sb.append("Statistics - All Collections");
        sb.append("</th>");
        sb.append("</tr>");
        sb.append("<tr><th>Name</th><th>Total</th></tr>\n");
        
        Collection<RegProcEntry> collEntries = rp.getCollectionsHash().values();
        
        int[] values = new int[statsArray.length];
        for (int i=0;i<statsArray.length;i++)
        {
            values[i] = calcStat(sb, collEntries, statsArray[i], fullDescHash.get(statsArray[i]), null);
        }
        sb.append("</table>");
        
        Hashtable<String, Boolean> dispHash = new Hashtable<String, Boolean>();
        for (RegProcEntry entry : collEntries)
        {
            String       disp   = null;
            RegProcEntry parent = (RegProcEntry)entry.getParent();
            if (parent != null)
            {
                disp = parent.getName();
                if (disp != null)
                {
                    dispHash.put(disp, true);
                }
            }
        }
        
        for (String disp : dispHash.keySet())
        {
            DisciplineType discipline = DisciplineType.getDiscipline(disp);
            String         dTitle     = discipline != null ? discipline.getTitle() : disp;
             
            sb.append("<br><br><table class=\"brd\" border=\"0\" cellspacing=\"0\">\n");
            sb.append("<tr>");
            sb.append("<th colspan=\"2\">");
            sb.append("Statistics for "+dTitle);
            sb.append("</th>");
            sb.append("</tr>");
            sb.append("<tr><th>Name</th><th>Total</th></tr>\n");

            values = new int[statsArray.length];
            for (int i=0;i<statsArray.length;i++)
            {
                
                values[i] = calcStat(sb, collEntries, statsArray[i], fullDescHash.get(statsArray[i]), disp);
            }
            sb.append("</table>"); 
        }
        //sb.append("<br><br>");
        //createTable(sb, "No ISA Registrations", rp.getRoot(false).getKids(), false);
        sb.append("</center></body></html>");
        
        try
        {
            File file = new File("stats.html");
            FileUtils.writeStringToFile(file, sb.toString());
            
            BrowserLauncher.openURL(file.toURI().toString());
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    protected void doSetVersion()
    {
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder pb   = new PanelBuilder(new FormLayout("f:p:g", "p,2px,f:p:g"));
        
        Vector<String>    versionsList = new Vector<String>();
        Hashtable<String, String> verToDateHash = new Hashtable<String, String>();
        try
        {
            SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy");
            SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy/MM/dd");
            List<String> lines = FileUtils.readLines(rp.getDataFromWeb("http://specify6-test.nhm.ku.edu/specifydownloads/specify6/alpha/versions.txt", false));
            for (String line : lines)
            {
                String[] toks = line.split(",");
                if (toks.length > 2)
                {
                    String ver = StringUtils.remove(toks[1].trim(), "Alpha ");
                    
                    versionsList.insertElementAt(ver, 0);
                    
                    String dateStr = toks[2].trim();
                    dateStr = StringUtils.replace(dateStr, ".", "/");
                    try
                    {
                        Date date = mmddyyyy.parse(dateStr);
                        verToDateHash.put(ver, yyyymmdd.format(date));
                        
                    } catch (Exception ex) {}
                }
            }
            versionsList.insertElementAt("Clear", 0);
            
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        final JList        list = new JList(versionsList);
        final CustomDialog dlg  = new CustomDialog(null, "Set Version", true, pb.getPanel());
        
        pb.add(UIHelper.createLabel("Versions", SwingConstants.CENTER), cc.xy(1,1));
        pb.add(UIHelper.createScrollPane(list),  cc.xy(1,3));
        
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    dlg.getOkBtn().setEnabled(list.getSelectedIndex() > -1);
                }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                if (e.getClickCount() == 2)
                {
                    dlg.getOkBtn().doClick();
                }
            }
            
        });
        
        pb.setDefaultDialogBorder();
        
        dlg.createUI();
        dlg.getOkBtn().setEnabled(false);
        
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            int   inx          = list.getSelectedIndex();
            String version     = (String)list.getSelectedValue();
            
            if (version.equals("Clear"))
            {
                rp.setVersionDates(null, null, null);
                frame.setTitle(title);
            } else
            {
                String prevVersion = inx == list.getModel().getSize()-1 ? null : (String)list.getModel().getElementAt(inx+1);
                rp.setVersionDates(version, prevVersion, verToDateHash);
                frame.setTitle(title+" for "+version);
            }
            
            try
            {
                rp.process(doLocal ? new File("reg.dat") : rp.getDataFromWeb("SpReg.REGISTER_URL", true));
                rp.processTracks(doLocal ? new File("track.dat") : rp.getDataFromWeb("StatsTrackerTask.URL", true));
                rp.mergeStats();
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
            
        }
    }
    
    /**
     * 
     */
    protected void doStart()
    {
        frame                = new JFrame();
        JMenuBar  mb         = new JMenuBar();
        JMenu     fileMenu   = new JMenu("File");
        JMenu     rptMenu    = new JMenu("Reports");
        //JMenu     chrtMenu   = new JMenu("Charts");
        
        frame.setTitle(title);
        
        JMenuItem doISARegReportMI = new JMenuItem("ISA Reg Report");
        rptMenu.add(doISARegReportMI);
        
        JMenuItem doStatsMI = new JMenuItem("Registration");
        rptMenu.add(doStatsMI);
        
        //JMenuItem doMiscMI = new JMenuItem("Misc");
        //chrtMenu.add(doMiscMI);
        
        //menu.addSeparator();
        JMenuItem doSetVersionMI = new JMenuItem("Set Version");
        fileMenu.add(doSetVersionMI);
        
        if (!UIHelper.isMacOS())
        {
            fileMenu.addSeparator();
            JMenuItem doExitMI = new JMenuItem("Exit");
            fileMenu.add(doExitMI);
            doExitMI.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    System.exit(0);
                }
            });
        }
        
        mb.add(fileMenu);
        mb.add(rptMenu);
        //mb.add(chrtMenu);
        
        frame.setJMenuBar(mb);
        frame.setContentPane(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(0, 0, 600, 768);
        UIHelper.centerAndShow(frame);
        
        doISARegReportMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                createRegReport();
            }
        });
        
        doStatsMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                createStatsReport();
            }
        });
        
        doSetVersionMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doSetVersion();
            }
        });
    }
    
    class MyTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer 
    {
        public Component getTreeCellRendererComponent(JTree treeArg,
                                                      Object value,
                                                      boolean bSelected,
                                                      boolean bExpanded,
                                                      boolean bLeaf,
                                                      int iRow,
                                                      boolean bHasFocus ) 
        {
            super.getTreeCellRendererComponent(treeArg,
                    value,
                    bSelected,
                    bExpanded,
                    bLeaf,
                    iRow,
                    bHasFocus);
            
            // Find out which node we are rendering and get its text
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            String labelText = (String)node.toString();
            setText(labelText);
            
            String       iconName = null;
            RegProcEntry entry    = (RegProcEntry)node;
            String       regType  = entry.get("reg_type");
            if (entry.getName().equals("Root"))
            {
                iconName = "InstBldg";
                
            } else if (regType!= null)
            {
                
                if (regType.equals("Discipline"))
                {
                    iconName = entry.get("Discipline_type");
                    if (iconName == null)
                    {
                        iconName = regType;
                    }
                } else if (!entry.getName().equals("Root"))
                {
                    iconName = regType;
                }
                
                
            }
            
            if (iconName != null)
            {
                Icon myIcon = IconManager.getIcon(iconName, IconManager.IconSize.Std16);
                if (myIcon != null)
                {
                    setOpenIcon(myIcon);
                    setClosedIcon(myIcon);
                    setLeafIcon(myIcon);
                    setIcon(myIcon);
                }
            }
            return this;
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                String appDir = System.getProperty("appdir"); //$NON-NLS-1$
                if (StringUtils.isNotEmpty(appDir))
                {
                    UIRegistry.setDefaultWorkingPath(appDir);
                }
                
                String appdatadir = System.getProperty("appdatadir"); //$NON-NLS-1$
                if (StringUtils.isNotEmpty(appdatadir))
                {
                    UIRegistry.setBaseAppDataDir(appdatadir);
                }
                
                String javadbdir = System.getProperty("javadbdir"); //$NON-NLS-1$
                if (StringUtils.isNotEmpty(javadbdir))
                {
                    UIRegistry.setJavaDBDir(javadbdir);
                }

                // Then set this
                IconManager.setApplicationClass(Specify.class);
                IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
                IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
                IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$
           
                new RegisterApp().doStart();
            }
        });
    }
}
