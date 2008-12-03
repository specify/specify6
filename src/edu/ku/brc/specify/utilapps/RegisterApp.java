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
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultListModel;
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
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.ku.brc.helpers.BrowserLauncher;
import edu.ku.brc.ui.CustomFrame;
import edu.ku.brc.ui.UIHelper;
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

    protected JTree                tree;
    protected JTable               propsTable;
    
    protected JList                trackItemsList;
    protected Hashtable<String, Hashtable<String, Integer>> trackUsageHash;
    
    protected CustomFrame               chartFrame    = null;
    protected Hashtable<String, String> trackDescHash = new Hashtable<String, String>();
    
    protected RegProcessor              rp = new RegProcessor();
    
    /**
     * 
     */
    public RegisterApp()
    {
        super(new BorderLayout());
        createUI();
        
        String[] trackDescStr = {"DE", "Data Entry", "WB", "WorkBench", "SS", "System Tools", "RS", "RecordSets"};
        for (int i=0;i<trackDescStr.length;i++)
        {
            trackDescHash.put(trackDescStr[i], trackDescStr[i+1]);
            i++;
        }
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
            boolean doLocal = false;
            rp.process(doLocal ? new File("reg.dat") : rp.getDataFromWeb("SpReg.REGISTER"));
            rp.processTracks(doLocal ? new File("track.dat") : rp.getDataFromWeb("StatsTrackerTask.URL"));
            
            rp.mergeStats();
            
            
            tree       = new JTree(rp.getRoot(false));
            propsTable = new JTable();
            
            tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e)
                {
                    fillPropsTable();
                }
            });
            
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
        
        final String[] trackDescPairs = rp.getTrackDescPairs();
        Vector<String> trkItems = new Vector<String>();
        for (int i=1;i<trackDescPairs.length;i++)
        {
            trkItems.add(trackDescPairs[i]);
            i++;
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
                        fillUsageItemsList(trackDescPairs[inx*2]);
                    }
                }
            }
        });
        
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
        tabbedPane.add("Tracking", splitPane);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * @param catName
     */
    protected void fillUsageItemsList(final String catName)
    {
        DefaultListModel           model = (DefaultListModel)trackItemsList.getModel();
        Hashtable<String, Integer> hash  = trackUsageHash.get(catName);
        Vector<String>             tucn  = new Vector<String>(hash.keySet());
        Collections.sort(tucn);
        
        Vector<Pair<String, Integer>> values = new Vector<Pair<String,Integer>>();
        int total = 0;
        for (String key : hash.keySet())
        {
            Integer val = hash.get(key);
            values.add(new Pair<String, Integer>(key, val));
            total += val;
        }
        
        Collections.sort(values, new Comparator<Pair<String, Integer>>() {
            @Override
            public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2)
            {
                return o1.second.compareTo(o2.second);
            }
        });
        
        createBarChart(catName, values);
        
        // Fill Model
        model.clear();
        for (String n : tucn)
        {
            model.addElement(n);
        }
    }
    
    /**
     * @param catName
     * @param values
     */
    private void createBarChart(final String catName, final Vector<Pair<String, Integer>> values)
    {
        String cat = ""; //$NON-NLS-1$
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); 
        
        for (Pair<String, Integer> p : values)
        {         
            dataset.addValue(p.second, p.first, cat);
        }

        // create the chart... 
        JFreeChart chart = ChartFactory.createBarChart3D( 
                trackDescHash.get(catName),      // chart title 
                "Actions", // domain axis label 
                "Occurrence", // range axis label 
                dataset,    // data 
                PlotOrientation.VERTICAL,
                true,       // include legend 
                true,       // tooltips? 
                false       // URLs? 
            ); 
        
        // create and display a frame... 
        ChartPanel panel = new ChartPanel(chart, true, true, true, true, true); 
        
        
        if (chartFrame == null)
        {
            panel.setPreferredSize(new Dimension(800,600)); 
            chartFrame = new CustomFrame("Statistics", CustomFrame.OK_BTN, null);
            chartFrame.setOkLabel("Close");
            chartFrame.createUI();
        }
        
        chartFrame.setContentPane(panel);
        // Can't believe I have to do this
        Rectangle r = chartFrame.getBounds();
        r.height++;
        chartFrame.setBounds(r);
        r.height--;
        //----
        chartFrame.setBounds(r);
        chartFrame.pack();
        chartFrame.setVisible(true);
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
            
            Object[][] rows = new Object[keys.size()][2];
            int inx = 0;
            for (Object key : keys)
            {
                rows[inx][0] = key;
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
    private int count(final RegProcEntry inst, final CountType type)
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
     * @param title
     * @param entries
     */
    private void createTable(final StringBuilder sb, 
                             final String title, 
                             final Vector<RegProcEntry> entries,
                             final boolean hasReg)
    {
        sb.append("<table class=\"brd\" border=\"1\">\n");
        sb.append("<tr>");
        sb.append("<th colspan=\"7\">");
        sb.append(title);
        sb.append("</th>");
        sb.append("</tr>");
        sb.append("<tr><th>Inst</th><th>Division</th><th>Discipline</th><th>Collection</th><th>ISA Number</th><th>Phone</th><th>EMail</th></tr>\n");
        
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
                        if (colsCnt > 0) sb.append("<tr>\n");
                        String isa = col.getISANumber();
                        sb.append("  <td>"+col.getName()+"</td>\n  <td>"+(StringUtils.isNotEmpty(isa) ? isa : "&nbsp;")+"</td>");
                        
                        String phone = col.get("Phone");
                        String email = col.get("User_email");
                        sb.append("<td>"+(StringUtils.isNotEmpty(phone) ? phone : "&nbsp;")+"</td>\n  <td>"+(StringUtils.isNotEmpty(email) ? email : "&nbsp;")+"</td>\n");
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
        sb.append("<html><body>");
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
        String[] statsArray = {"num_co", "Collection Objects", 
                               "num_tx", "Taxon", 
                               "num_txu", "Taxon in Use", 
                               "num_geo", "Geography", 
                               "num_geou", "Geography in Use", 
                               "num_loc", "Localities", 
                               "num_locgr", "Localities Geo-Referenced", 
                               "num_preps", "Preparations", 
                               "num_prpcnt", "Preparations Count", 
                               "num_litho", "Lithostratigraphy", 
                               "num_lithou", "Lithostratigraphy in Use", 
                               "num_gtp", "Chronstragigraphy", 
                               "num_gtpu", "Chronstragigraphy in Use", };
        
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><center>");
        sb.append("<table class=\"brd\" border=\"1\">\n");
        sb.append("<tr>");
        sb.append("<th colspan=\"2\">");
        sb.append("Statistics - All Collections");
        sb.append("</th>");
        sb.append("</tr>");
        sb.append("<tr><th>Name</th><th>Total</th></tr>\n");

        Collection<RegProcEntry> collEntries = rp.getCollectionsHash().values();
        
        int[] values = new int[statsArray.length / 2];
        for (int i=0;i<statsArray.length;i++)
        {
            values[i/2] = calcStat(sb, collEntries, statsArray[i], statsArray[i+1], null);
            i++;
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
            sb.append("<br><br><table class=\"brd\" border=\"1\">\n");
            sb.append("<tr>");
            sb.append("<th colspan=\"2\">");
            sb.append("Statistics for "+disp);
            sb.append("</th>");
            sb.append("</tr>");
            sb.append("<tr><th>Name</th><th>Total</th></tr>\n");

            values = new int[statsArray.length / 2];
            for (int i=0;i<statsArray.length;i++)
            {
                values[i/2] = calcStat(sb, collEntries, statsArray[i], statsArray[i+1], disp);
                i++;
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
     * @param args
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame    frame      = new JFrame();
                JMenuBar  mb         = new JMenuBar();
                JMenu     menu       = new JMenu("File");
                JMenuItem doISARegReportMI = new JMenuItem("ISA Reg Report");
                menu.add(doISARegReportMI);
                
                JMenuItem doStatsMI = new JMenuItem("Stats");
                menu.add(doStatsMI);
                
                mb.add(menu);
                
                final RegisterApp regApp = new RegisterApp();
                frame.setJMenuBar(mb);
                frame.setContentPane(regApp);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setBounds(0, 0, 800, 768);
                frame.setVisible(true);
                
                doISARegReportMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        regApp.createRegReport();
                    }
                });
                
                doStatsMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        regApp.createStatsReport();
                    }
                });
            }
        });
    }

}
