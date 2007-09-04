/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.utilapps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 26, 2007
 *
 */
public class ERDVisualizer extends JFrame
{
    protected static boolean doShadow = true;
    
    protected String contentTag = "<!-- Content -->";

    protected int MARGIN = 10;
    protected int HGAP    = 60;
    protected int VGAP    = 30;
    
    //protected Vector<ERDTable>            list;
    protected TableTracker              tblTracker;
    protected ERDPanel                  mainPanel;
    
    protected int    inx = 0;
    protected String mapTemplate = "";
    protected File   schemaDir;
    protected Timer  timer;
    
    // For Trees
    
        
    public ERDVisualizer(final TableTracker tblOrg, final File schemaDir)
    {
        this.tblTracker = tblOrg;
        this.schemaDir  = schemaDir;
        
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(mainPanel = new ERDPanel(tblOrg), BorderLayout.CENTER);
        setContentPane(new JScrollPane(p));
        p.setBackground(Color.WHITE);
        mainPanel.setBackground(Color.WHITE);
        
        try
        {
            File templateFile = new File(UIRegistry.getDefaultWorkingPath() + File.separator + "site/schema_template.html");
            mapTemplate = FileUtils.readFileToString(templateFile);
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        if (StringUtils.isEmpty(mapTemplate))
        {
            System.err.println("The template file is empty!");
        }
        
        try
        {
            File srcDir = new File(UIRegistry.getDefaultWorkingPath() + File.separator + "site");
            for (File f: srcDir.listFiles())
            {
                if (!f.getName().startsWith("."))
                {
                    File dst = new File(UIRegistry.getDefaultWorkingPath() + File.separator + "schema" + File.separator + f.getName());
                    if (!FilenameUtils.getExtension(f.getName()).toLowerCase().equals("html"))
                    {
                        FileUtils.copyFile(f, dst);
                    }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        if (true)
        {
            advance();
            
            int period = isDoShadow() ? 10000 : 1000;
            int delay  = isDoShadow() ? 5000 : 1000;
            timer = new Timer();
            
            if (false)
            {
                timer.scheduleAtFixedRate(new TimerTask() {
                        public void run() 
                        {
                            advance();
                        }
                    }, period, delay);
            }
        } else
        {
            //this.tblOrg.setFont(this.tblOrg.getFont().deriveFont((float)10.0));
            
            ERDTable root = tblOrg.getTable("CollectionType");
            
            
            //                                skip,  processKids, alwaysAKid, processAnyRel, okWhenParent
            tblOrg.addNodeInfo("Agent",              false, true,        true,       false,         null);
            tblOrg.addNodeInfo("Determination",      false, true,        true,       true,          null);
            tblOrg.addNodeInfo("Attachment",         true,  true,        true,       false,         null);
            tblOrg.addNodeInfo("AttributeDef",       true,  true,        true,       false,         null);
            tblOrg.addNodeInfo("UserPermission",     true,  true,        true,       false,         null);
            tblOrg.addNodeInfo("AppResourceDefault", true,  true,        true,       false,         null);
            tblOrg.addNodeInfo("DeaccessionPreparation",  true,  true,    true,       false,         null);
            tblOrg.addNodeInfo("OtherIdentifier",         true,  true,    true,       false,         null);
            tblOrg.addNodeInfo("CollectionRelationship",  true,  true,    true,       false,         null);
            tblOrg.addNodeInfo("ProjectCollectionObject", true,  true,    true,       false,         null);
            
            // No Kids
            tblOrg.addNodeInfo("Taxon",                  false, false, true,  false, null);
            tblOrg.addNodeInfo("TaxonCitation",          false, false, true,  false, null);
            tblOrg.addNodeInfo("DeterminationStatus",    false, false, true,  false, null);
            tblOrg.addNodeInfo("DeterminationCitation",  false, false, true,  false, null);
            tblOrg.addNodeInfo("ReferenceWork",          false, false, true,  false, null);
            tblOrg.addNodeInfo("Journal",                false, false, true,  false, null);
            tblOrg.addNodeInfo("CollectingEvent",        false, false, true,  false, null);
            tblOrg.addNodeInfo("Locality",               false, false, true,  false, null);
            tblOrg.addNodeInfo("Geography",              false, false, true,  false, null);
            tblOrg.addNodeInfo("PaleoContext",           false, false, true,  false, null);
            tblOrg.addNodeInfo("LithoStrat",             false, false, true,  false, null);
            tblOrg.addNodeInfo("GeologicTimePeriod",     false, false, true,  false, null);
            tblOrg.addNodeInfo("CollectionObjectCitation", false, false, true,  false, null);
            tblOrg.addNodeInfo("Accession",              false, false, true,  false, null);
            tblOrg.addNodeInfo("AccessionAuthorization", false, false, true,  false, null);
            tblOrg.addNodeInfo("AccessionAgent",         false, false, true,  false, null);
            tblOrg.addNodeInfo("Agent",                  false, false, true,  false, null);
            tblOrg.addNodeInfo("Preparation",            false, false, true,  false, null);
            tblOrg.addNodeInfo("PrepType",               false, false, true,  false, null);
            tblOrg.addNodeInfo("RepositoryAgreement",    false, false, true,  false, null);
            
            NodeInfo det = tblOrg.getNodeInfo("Determination");
            det.addKid(tblOrg.getTable("Taxon"));
            det.addKid(tblOrg.getTable("DeterminationStatus"));
            
            NodeInfo ni = tblOrg.getNodeInfo("Taxon");
            ni.addKid(tblOrg.getTable("TaxonCitation"));
            
            ni = tblOrg.getNodeInfo("CollectionObjectCitation");
            ni.addKid(tblOrg.getTable("ReferenceWork"));
            
            ni = tblOrg.getNodeInfo("TaxonCitation");
            ni.addKid(tblOrg.getTable("ReferenceWork"));
            
            ni = tblOrg.getNodeInfo("ReferenceWork");
            ni.setOkToDuplicate(true);
            ni.addKid(tblOrg.getTable("Journal"));

            ni = tblOrg.getNodeInfo("GeologicTimePeriod");
            ni.setOkToDuplicate(true);
            
            ni = tblOrg.getNodeInfo("Journal");
            ni.setOkToDuplicate(true);
            
            ni = tblOrg.getNodeInfo("CollectingEvent");
            ni.addKid(tblOrg.getTable("Locality"));
            
            ni = tblOrg.getNodeInfo("CollectionObject");
            ni.addKid(tblOrg.getTable("CollectingEvent"));
            ni.addKid(tblOrg.getTable("PaleoContext"));
            ni.addKid(tblOrg.getTable("Accession"));
            
            ni = tblOrg.getNodeInfo("Locality");
            ni.addKid(tblOrg.getTable("Geography"));
            
            ni = tblOrg.getNodeInfo("PaleoContext");
            ni.addKid(tblOrg.getTable("LithoStrat"));
            ni.addKid(tblOrg.getTable("GeologicTimePeriod"));
            
            ni = tblOrg.getNodeInfo("DeterminationCitation");
            ni.addKid(tblOrg.getTable("ReferenceWork"));
            
            ni = tblOrg.getNodeInfo("Preparation");
            ni.addKid(tblOrg.getTable("PrepType"));
            
            ni = tblOrg.getNodeInfo("Accession");
            ni.addKid(tblOrg.getTable("AccessionAuthorization"));
            ni.addKid(tblOrg.getTable("AccessionAgent"));
            ni.addKid(tblOrg.getTable("RepositoryAgreement"));
            //ni.addKid(tblOrg.getTable("Attachment"));
            //protected RepositoryAgreement repositoryAgreement;

            if (false)
            {
                ni = tblOrg.getNodeInfo("Agent");
                ni.setOkToDuplicate(true);
                
                String[] toAddAgent = {"CollectionObject", "Accession", "Determination", "Preparation", };
                for (String name : toAddAgent)
                {
                    ni = tblOrg.getNodeInfo(name);
                    ni.addKid(tblOrg.getTable("Agent"));
                }
            }
            
            processAsTree(root, 0);
            mainPanel.addTree(root);
            
            final SwingWorker worker = new SwingWorker()
            {
                public Object construct()
                {
                    try
                    {
                        Thread.sleep(3000);
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    return null;
                }

                //Runs on the event-dispatching thread.
                public void finished()
                {
                    generate();
                    System.out.println("Done.");
                }
            };
            worker.start();
        }
        
        createIndexFile();
    }
    
    /**
     * 
     */
    protected void createIndexFile()
    {
        String fName = schemaDir.getAbsolutePath() + File.separator + "index.html";
        try
        {
            File html = new File(fName);
            BufferedWriter output = new BufferedWriter( new FileWriter(html) );
            
            int inx = mapTemplate.indexOf(contentTag);
            String subContent = mapTemplate.substring(0, inx);
            output.write(StringUtils.replace(subContent, "<!-- Title -->", "Schema Index"));
        
            output.write("<UL>");
            output.write("<LI><a href=\"CollectionOverview.html\">Schema Overview</a></LI>");
            for (ERDTable t : tblTracker.getList())
            {
                String name  = StringUtils.substringAfterLast(t.getClassName(), ".");
                String prettyName = UIHelper.makeNamePretty(name);
                output.write("<LI><a href=\""+name+".html\">"+prettyName+"</a></LI>");
            }
            output.write("</UL>");
            output.write(mapTemplate.substring(inx+contentTag.length()+1, mapTemplate.length()));
            
            output.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    
    /**
     * @return the doShadow
     */
    public static boolean isDoShadow()
    {
        return doShadow;
    }

    /**
     * 
     */
    public synchronized void advance()
    {
        if (inx >= 0)
        {
            generate();
        }
        
        if (inx >= tblTracker.getList().size())
        {
            timer.cancel();
            return;
        }
        
        addTables();
        inx++;
        

        validate();
        
        
        final ERDVisualizer frame = this;
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        frame.repaint();
                    }
                });
            }
        });
    }
    
    /**
     * @return
     */
    public ERDPanel getPanel()
    {
        return mainPanel;
    }
    
    /**
     * 
     */
    protected synchronized void addTables()
    {
        
        mainPanel.setIgnorePaint(true);
        
        mainPanel.clear();
        
        ERDTable table = tblTracker.getList().get(inx);
        
        int relCount = 0;
        for (DBTableIdMgr.TableRelationship r : table.getTable().getRelationships())
        {
            ERDTable relTable = tblTracker.getHash().get(r.getClassName());
            if (relTable != table)
            {
                relCount++;
            }

        }
        //mainPanel.setup(relCount);
        
        mainPanel.addTable(table);
        mainPanel.setSize(mainPanel.getPreferredSize());
        
        for (DBTableIdMgr.TableRelationship r : table.getTable().getRelationships())
        {
            ERDTable relTable = tblTracker.getHash().get(r.getClassName());
            if (relTable != table)
            {
                if (relTable != null)
                {
                    mainPanel.addTable(relTable);
                    relTable.setSize(relTable.getPreferredSize());
                    
                } else
                {
                    System.out.println("Couldn't find "+r.getClassName());
                }
            }
        }
        mainPanel.initTables();
        mainPanel.setIgnorePaint(false);
    }
    
    /**
     * 
     */
    public void generate()
    {
        Rectangle     rect     = getPanel().getParent().getBounds();
        System.out.println("MAIN["+rect+"]");
        if (rect.width == 0 || rect.height == 0)
        {
            return;
        }
        
        BufferedImage bufImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D    g2       = bufImage.createGraphics();
        g2.setRenderingHints(createTextRenderingHints());
        g2.setFont( tblTracker.getFont());
        getPanel().getParent().paint(g2);

        g2.dispose();
        
        Component stop = getPanel().getParent();
        Point p = new Point(0,0);
        calcLoc(p, getPanel().getMainTable(), stop);
        
        String name  = StringUtils.substringAfterLast(getPanel().getMainTable().getClassName(), ".");
        String fName = schemaDir.getAbsolutePath() + File.separator + name;
        try
        {
            File html = new File(fName + ".html");
            BufferedWriter output = new BufferedWriter( new FileWriter(html) );
            
            int inx = mapTemplate.indexOf(contentTag);
            String subContent = mapTemplate.substring(0, inx);
            output.write(StringUtils.replace(subContent, "<!-- Title -->", UIHelper.makeNamePretty(name)));
        
            File imgFile = new File(fName + ".png");
            output.write("<map name=\"schema\" id=\"schema\">\n");
            
            Vector<ERDTable> nList = mainPanel.isRoot() ? tblTracker.getTreeAsList(mainPanel.getMainTable()) : getPanel().getRelTables();
            for (ERDTable erdt : nList)
            {
                p = new Point(0,0);
                calcLoc(p, erdt, stop);
                Dimension s = erdt.getSize();
                String linkname  = StringUtils.substringAfterLast(erdt.getClassName(), ".");
                output.write("<area shape=\"rect\" coords=\""+p.x+","+p.y+","+(p.x+s.width)+","+(p.y+s.height)+"\" href=\""+linkname+".html\"/>\n");
            }

            output.write("</map>\n");
            output.write("<img border=\"0\" usemap=\"#schema\" src=\""+imgFile.getName()+"\"/>\n");
            
            output.write(mapTemplate.substring(inx+contentTag.length()+1, mapTemplate.length()));
            
            output.close();

            File oFile = new File(schemaDir + File.separator + imgFile.getName());
            System.out.println(oFile.getAbsolutePath());
            ImageIO.write(bufImage, "PNG", oFile);
            //ImageIO.write(bufImage, "JPG", new File(schemaDir + File.separator + imgFile.getName()+".jpg"));
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @return
     */
    public static RenderingHints createTextRenderingHints()
    {
        RenderingHints renderingHints;
        //RenderingHints renderingHints1 = new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        RenderingHints renderingHints2 = new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        //RenderingHints renderingHints3 = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        renderingHints = renderingHints2;

        Object value = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
        try
        {
            java.lang.reflect.Field declaredField = RenderingHints.class.getDeclaredField("VALUE_TEXT_ANTIALIAS_LCD_HRGB");
            value = declaredField.get(null);

        } catch (Exception e)
        {
            // do nothing
        }
        renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, value);
        //renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        //renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB);
        return renderingHints;
    }
    
    /**
     * @param font
     * @param name
     * @param align
     * @return
     */
    protected static JLabel mkLabel(final Font font, final String name, int align)
    {
        JLabel lbl = new JLabel(name, align);
        lbl.setFont(font);
        lbl.setOpaque(false);
        return lbl;
    }
    
    protected void calcLoc(final Point p, final Component src, final Component stop)
    {
        if (src != stop)
        {
            Point sp = src.getLocation();
            p.translate(sp.x, sp.y);
            calcLoc(p, src.getParent(), stop);
        }
    }

    protected void processAsTree(final ERDTable table, final int level)
    {
        System.out.println("["+table.getClassName()+"]");
        if (table.getClassName().indexOf("Paleo") > -1)
        {
            int x = 0;
            x++;
        }
        NodeInfo ni = tblTracker.getNodeInfo(table);
        
        boolean skip = false;
        if (ni.getOkWhenParent() != null)
        {
            skip = ni.getOkWhenParent() != table.getTreeParent();
        }
        
        if (ni.isSkip())
        {
            tblTracker.getUsedHash().put(table.getClassName(), true);
            return;
        }
        
        if (skip || !ni.isAlwaysAKid() || tblTracker.getUsedHash().get(table) != null)
        {
            return;
        }
        
        /* DEBUG
        if (table.getClassName().indexOf("Taxon") > 0)
        {
            System.out.println(table.getClassName());
        }
        
        if (table.getClassName().indexOf("ReferenceWork") > 0)
        {
            System.out.println(table.getClassName());
            for (ERDTable t : ni.getKids())
            {
                System.out.println(ni.getClassName()+ " " + t.getClassName());
            }
        }*/
        
        Dimension tableSize = table.getPreferredSize();
        table.getSpace().width  = tableSize.width;
        table.getSpace().height = tableSize.height;

        int maxHeight = 0;
        int maxWidth  = 0;
        
        if (ni.isProcessKids() || ni.getKids().size() > 0)
        {
            for (DBTableIdMgr.TableRelationship r : table.getTable().getRelationships())
            {
                ERDTable rTable = tblTracker.getHash().get(r.getClassName());
                if (rTable == null)
                {
                    continue;
                }
                
                NodeInfo kni = tblTracker.getNodeInfo(rTable);
                
                boolean kidOK = true;
                boolean override = false;
                if (!ni.isProcessKids())
                {
                    kidOK = ni.getKids().contains(rTable);
                    override = true;
                    
                } else
                {
                    if (kni.isSkip())
                    {
                        tblTracker.getUsedHash().put(rTable.getClassName(), true);
                        continue;
                    }
                    
                    if (ni.getKids().contains(rTable))
                    {
                        override = true;
                    }
                }
                
                if (kni.isOkToDuplicate())
                {
                    System.out.println("Duplicating ["+rTable.getClassName()+"]");
                    rTable = rTable.duplicate(tblTracker.getFont());
                }
                
                if (kidOK && (override || r.getType() == DBTableIdMgr.RelationshipType.OneToMany || r.getType() == DBTableIdMgr.RelationshipType.OneToMany))
                {
                    System.out.println("    ["+rTable.getClassName()+"]");

                    table.getKids().add(rTable);
                    rTable.setTreeParent(table);
                    processAsTree(rTable, level+1);
                    
                    Dimension size = rTable.getSpace();
                    maxWidth  += size.width;
                    maxHeight = Math.max(maxHeight, size.height);
                }
            }
        }

        //System.out.println(level+"   mw "+maxWidth+"  mh "+ maxHeight+ "  " + table.getKids().size());
        table.getSpace().width  = Math.max(tableSize.width, maxWidth);
        table.getSpace().height = tableSize.height + maxHeight;
        tblTracker.getUsedHash().put(table.getClassName(), true);
    }

    

    
    /**
     * @param args
     */
    public static void main(String[] args)
    {

        TableTracker tableTracker = new TableTracker();
        
        final File schemaDir = new File("schema");
        if (!schemaDir.exists())
        {
            schemaDir.mkdir();
        } else
        {
            try
            {
                //FileUtils.cleanDirectory(schemaDir);
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        final ERDVisualizer frame = new ERDVisualizer(tableTracker, schemaDir);

        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setBounds(0, 0, 2000, 2000);
                frame.setVisible(true);
                
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        //frame.generate();
                    }
                });
            }
        });
    }
    

}

