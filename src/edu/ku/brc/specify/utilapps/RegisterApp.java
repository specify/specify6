/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.utilapps;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,4px,p"));
        
        rp = new RegProcessor();
        try
        {
            rp.process(new File("reg.dat"));
            rp.processTracks(new File("track.dat"));
            
            tree       = new JTree(rp.getRoot(false));
            propsTable = new JTable();
            
            pb.add(UIHelper.createScrollPane(tree), cc.xy(1, 1));
            pb.add(UIHelper.createScrollPane(propsTable), cc.xy(1, 3));
            pb.setDefaultDialogBorder();
            
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
        
        tabbedPane.add("Registration", pb.getPanel());
        
        pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,4px,p"));
        
        Vector<String> tucn = new Vector<String>(rp.getTrackCatsHash().keySet());
        Collections.sort(tucn);
        final JList list = new JList(tucn);
        pb.add(UIHelper.createScrollPane(list), cc.xy(1, 1));
        
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    String catName = (String)list.getSelectedValue();
                    fillUsageItemsList(catName);
                }
            }
        });
        
        
        DefaultListModel model = new DefaultListModel();
        trackItemsList = new JList(model);
        
        pb.add(UIHelper.createScrollPane(trackItemsList), cc.xy(1, 3));
        trackUsageHash = rp.getTrackCatsHash();
        tabbedPane.add("Tracking", pb.getPanel());
        
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
    }
    
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
    private void createTable(final StringBuilder sb, final String title, final Vector<RegProcEntry> entries)
    {
        sb.append("<table class=\"brd\" border=\"1\">\n");
        sb.append("<tr>");
        sb.append("<th colspan=\"2\">");
        sb.append(title);
        sb.append("</th>");
        sb.append("</tr>");
        sb.append("<tr><th>Item</th><th>Count</th></tr>\n");
        
        for (RegProcEntry inst : entries)
        {
            int cols = count(inst, CountType.Cols);
            sb.append("<tr>\n  <td rowspan=\""+cols+"\">"+inst.getName()+"</td>\n");
            
            for (RegProcEntry div : inst.getKids())
            {
                int disps = count(inst, CountType.Disps);
                sb.append("  <td rowspan=\""+disps+"\">"+div.getName()+"</td>\n");
                for (RegProcEntry disp : div.getKids())
                {
                    sb.append("  <td rowspan=\""+disp.getKids().size()+"\">"+disp.getName()+"</td>\n");
                    for (RegProcEntry col : disp.getKids())
                    {
                        sb.append("  <td>"+col.getName()+"</td>\n  <td>"+col.getISANumber()+"</td>\n");
                        sb.append("</tr>\n");
                        sb.append("<tr>\n");
                    }
                }
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
        createTable(sb, "Registrations", rp.getRoot(false).getKids());
        sb.append("</body></html>");
        
        try
        {
            FileUtils.writeStringToFile(new File("report.html"), sb.toString());
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
                JMenuItem doReportMI = new JMenuItem("Report");
                menu.add(doReportMI);
                mb.add(menu);
                
                final RegisterApp regApp = new RegisterApp();
                frame.setJMenuBar(mb);
                frame.setContentPane(regApp);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setBounds(0, 0, 800, 768);
                frame.setVisible(true);
                
                doReportMI.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        regApp.createRegReport();
                    }
                });
            }
        });
    }

}
