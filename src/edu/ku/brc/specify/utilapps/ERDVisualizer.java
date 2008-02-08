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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
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
    
    protected TableTracker              tblTracker;
    protected ERDPanel                  mainPanel;
    
    protected int     inx = 0;
    protected String  mapTemplate = "";
    protected File    schemaDir;
    protected Timer   timer;
    protected boolean doPNG = true;
    
    // For Trees
    
        
    public ERDVisualizer()
    {
        boolean showTreeHierarchy = false;
        boolean doGerman          = false;
        
        if (doGerman)
        {
            Locale german = new Locale("de", "", "");
            Locale.setDefault(german);
            UIRegistry.setResourceLocale(german);
        }
        
        ERDTable.setDisplayType(showTreeHierarchy ? ERDTable.DisplayType.Title : ERDTable.DisplayType.All);
        
        tblTracker = new TableTracker();
        
        final File schemaDir = new File("schema");
        if (!schemaDir.exists())
        {
            schemaDir.mkdir();
        } else
        {
            try
            {
                FileUtils.cleanDirectory(schemaDir);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        this.schemaDir = schemaDir;
        
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(mainPanel = new ERDPanel(tblTracker), BorderLayout.CENTER);
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
        
        // Choose what to display
        
        
        if (!showTreeHierarchy)
        {
            advance();
            
            int period = isDoShadow() ? 10000 : 1000;
            int delay  = isDoShadow() ? 5000 : 1000;
            timer = new Timer();
            
            if (true)

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
            ERDTable.setDisplayType(ERDTable.DisplayType.Title);
            
            //this.tblTracker.setFont(this.tblTracker.getFont().deriveFont((float)10.0));
            ERDTable root = null;
            
            boolean doCollectionSchema = true;
            
            if (doCollectionSchema)
            {
                root = tblTracker.getTable("Discipline");
                
                //                                                skip,  processKids, alwaysAKid, processAnyRel, okWhenParent
                tblTracker.addNodeInfo("Agent",                   false, true,        true,       false,         null);
                tblTracker.addNodeInfo("Determination",           false, true,        true,       true,          null);
                tblTracker.addNodeInfo("ConservDescription",      false, true,        true,       true,          null);
                
                tblTracker.addNodeInfo("Attachment",              true,  true,        true,       false,         null);
                tblTracker.addNodeInfo("AttributeDef",            true,  true,        true,       false,         null);
                tblTracker.addNodeInfo("UserPermission",          true,  true,        true,       false,         null);
                tblTracker.addNodeInfo("SpAppResourceDir",        true,  true,        true,       false,         null);
                tblTracker.addNodeInfo("SpLocaleContainer",       true,  true,        true,       false,         null);
                tblTracker.addNodeInfo("DeaccessionPreparation",  true,  true,        true,       false,         null);
                tblTracker.addNodeInfo("OtherIdentifier",         true,  true,        true,       false,         null);
                tblTracker.addNodeInfo("CollectionRelationship",  true,  true,        true,       false,         null);
                tblTracker.addNodeInfo("ProjectCollectionObject", true,  true,        true,       false,         null);
                tblTracker.addNodeInfo("CollectionObjectAttr",    true,  true,        true,       false,         null);
                tblTracker.addNodeInfo("CollectionObjectAttachment", true,   true,    true,       false,         null);
                tblTracker.addNodeInfo("ConservDescriptionAttachment", true, true,    true,       false,         null);
                tblTracker.addNodeInfo("ConservEventAttachment",  true, true,         true,       false,         null);
                
                // No Kids
                tblTracker.addNodeInfo("Taxon",                  false, false, true,  false, null);
                tblTracker.addNodeInfo("TaxonCitation",          false, false, true,  false, null);
                tblTracker.addNodeInfo("DeterminationStatus",    false, false, true,  false, null);
                tblTracker.addNodeInfo("DeterminationCitation",  false, false, true,  false, null);
                tblTracker.addNodeInfo("ReferenceWork",          false, false, true,  false, null);
                tblTracker.addNodeInfo("Journal",                false, false, true,  false, null);
                tblTracker.addNodeInfo("CollectingEvent",        false, false, true,  false, null);
                tblTracker.addNodeInfo("Locality",               false, false, true,  false, null);
                tblTracker.addNodeInfo("Geography",              false, false, true,  false, null);
                tblTracker.addNodeInfo("PaleoContext",           false, false, true,  false, null);
                tblTracker.addNodeInfo("LithoStrat",             false, false, true,  false, null);
                tblTracker.addNodeInfo("GeologicTimePeriod",     false, false, true,  false, null);
                tblTracker.addNodeInfo("CollectionObjectCitation", false, false, true,  false, null);
                tblTracker.addNodeInfo("Accession",              false, false, true,  false, null);
                tblTracker.addNodeInfo("AccessionAuthorization", false, false, true,  false, null);
                tblTracker.addNodeInfo("AccessionAgent",         false, false, true,  false, null);
                tblTracker.addNodeInfo("Agent",                  false, false, true,  false, null);
                tblTracker.addNodeInfo("Preparation",            false, false, true,  false, null);
                tblTracker.addNodeInfo("PrepType",               false, false, true,  false, null);
                tblTracker.addNodeInfo("RepositoryAgreement",    false, false, true,  false, null);
                tblTracker.addNodeInfo("ConservEvent",           false, false, true,  false, null);
                
                tblTracker.addNodeInfo("DNASequence",            false, false, true,  false, null);
                tblTracker.addNodeInfo("TreatmentEvent",         false, false, true,  false, null);
                tblTracker.addNodeInfo("Ipm",                    false, false, true,  false, null);
                tblTracker.addNodeInfo("FieldNotebook",          false, false, true,  false, null);
                tblTracker.addNodeInfo("FieldNotebookPageSet",   false, false, true,  false, null);
                tblTracker.addNodeInfo("FieldNotebookPage",      false, false, true,  false, null);
                
                NodeInfo det = tblTracker.getNodeInfo("Determination");
                det.addKid(tblTracker.getTable("Taxon"));
                det.addKid(tblTracker.getTable("DeterminationStatus"));
                
                NodeInfo ni = tblTracker.getNodeInfo("Taxon");
                ni.addKid(tblTracker.getTable("TaxonCitation"));
                
                ni = tblTracker.getNodeInfo("CollectionObjectCitation");
                ni.addKid(tblTracker.getTable("ReferenceWork"));
                
                ni = tblTracker.getNodeInfo("TaxonCitation");
                ni.addKid(tblTracker.getTable("ReferenceWork"));
                
                ni = tblTracker.getNodeInfo("ReferenceWork");
                ni.setOkToDuplicate(true);
                ni.addKid(tblTracker.getTable("Journal"));
    
                ni = tblTracker.getNodeInfo("GeologicTimePeriod");
                ni.setOkToDuplicate(true);
                
                ni = tblTracker.getNodeInfo("Journal");
                ni.setOkToDuplicate(true);
                
                ni = tblTracker.getNodeInfo("CollectingEvent");
                ni.addKid(tblTracker.getTable("Locality"));
                
                ni = tblTracker.getNodeInfo("CollectionObject");
                ni.addKid(tblTracker.getTable("CollectingEvent"));
                ni.addKid(tblTracker.getTable("PaleoContext"));
                ni.addKid(tblTracker.getTable("Accession"));
                ni.addKid(tblTracker.getTable("DNASequence"));
                ni.addKid(tblTracker.getTable("TreatmentEvent"));
                ni.addKid(tblTracker.getTable("Ipm"));
                
                ni = tblTracker.getNodeInfo("Locality");
                ni.addKid(tblTracker.getTable("Geography"));
                
                ni = tblTracker.getNodeInfo("PaleoContext");
                ni.addKid(tblTracker.getTable("LithoStrat"));
                ni.addKid(tblTracker.getTable("GeologicTimePeriod"));
                
                ni = tblTracker.getNodeInfo("DeterminationCitation");
                ni.addKid(tblTracker.getTable("ReferenceWork"));
                
                ni = tblTracker.getNodeInfo("Preparation");
                ni.addKid(tblTracker.getTable("PrepType"));
                
                ni = tblTracker.getNodeInfo("Accession");
                ni.addKid(tblTracker.getTable("AccessionAuthorization"));
                ni.addKid(tblTracker.getTable("AccessionAgent"));
                ni.addKid(tblTracker.getTable("RepositoryAgreement"));
                //ni.addKid(tblTracker.getTable("Attachment"));
                //protected RepositoryAgreement repositoryAgreement;
                
                ni = tblTracker.getNodeInfo("ConservEvent");
                ni.addKid(tblTracker.getTable("ConservRecommendation"));
                
                ni = tblTracker.getNodeInfo("FieldNotebook");
                ni.addKid(tblTracker.getTable("FieldNotebookPageSet"));
                
                ni = tblTracker.getNodeInfo("FieldNotebookPageSet");
                ni.addKid(tblTracker.getTable("FieldNotebookPage"));
                
                
            } else
            {
                root = tblTracker.getTable("SpAppResourceDir");
                
                //                                           skip,  processKids, alwaysAKid, processAnyRel, okWhenParent
                //tblTracker.addNodeInfo("Agent",              false, true,        true,       false,         null);
                
                // No Kids
                tblTracker.addNodeInfo("SpAppResourceData", false, false,        true,       false,         null);
                tblTracker.addNodeInfo("Collection",        false, false,        true,       false,         null);
                tblTracker.addNodeInfo("Discipline",    false, false,        true,       false,         null);
                tblTracker.addNodeInfo("SpecifyUser",       false, false,        true,       false,         null);
                tblTracker.addNodeInfo("SpAppResource",     false, false,        true,       false,         null);
                tblTracker.addNodeInfo("SpViewSetObj",      false, false,        true,       false,         null);
                tblTracker.addNodeInfo("SpUIViewSet",       false, false,        true,       false,         null);
                tblTracker.addNodeInfo("SpUIView",          false, false,        true,       false,         null);
                tblTracker.addNodeInfo("SpUIViewDef",       false, false,        true,       false,         null);
                tblTracker.addNodeInfo("SpUIAltView",       false, false,        true,       false,         null);
                tblTracker.addNodeInfo("SpUIColumn",        false, false,        true,       false,         null);
                tblTracker.addNodeInfo("SpUIRow",           false, false,        true,       false,         null);
                tblTracker.addNodeInfo("SpUICell",          false, false,        true,       false,         null);
                
                NodeInfo ni = tblTracker.getNodeInfo("SpAppResourceDir");
                ni.addKid(tblTracker.getTable("SpAppResource"));
                ni.addKid(tblTracker.getTable("SpViewSetObj"));
                //ni.addKid(tblTracker.getTable("Collection"));
                //ni.addKid(tblTracker.getTable("Discipline"));
                //ni.addKid(tblTracker.getTable("SpecifyUser"));
                
                ni = tblTracker.getNodeInfo("SpAppResource");
                ni.addKid(tblTracker.getTable("SpAppResourceData"));
                
                ni = tblTracker.getNodeInfo("SpViewSetObj");
                ni.addKid(tblTracker.getTable("SpAppResourceData"));
                ni.addKid(tblTracker.getTable("SpUIViewSet"));
                
                ni = tblTracker.getNodeInfo("SpUIViewSet");
                ni.addKid(tblTracker.getTable("SpUIView"));
                ni.addKid(tblTracker.getTable("SpUIViewDef"));
                
                ni = tblTracker.getNodeInfo("SpUIView");
                ni.addKid(tblTracker.getTable("SpUIAltView"));
                
                ni = tblTracker.getNodeInfo("SpUIViewDef");
                ni.addKid(tblTracker.getTable("SpUIColumn"));
                ni.addKid(tblTracker.getTable("SpUIRow"));
                
                ni = tblTracker.getNodeInfo("SpUIRow");
                ni.addKid(tblTracker.getTable("SpUICell"));
                //ni.addKid(tblTracker.getTable("SpUIViewDef"));
                
                //ni = tblTracker.getNodeInfo("SpUIAltView");
                //ni.addKid(tblTracker.getTable("SpUIView"));
                //ni.addKid(tblTracker.getTable("SpUIViewDef"));

                
                ni = tblTracker.getNodeInfo("SpAppResourceData");
                ni.setOkToDuplicate(true);

                
                if (false)
                {
                    NodeInfo det = tblTracker.getNodeInfo("Determination");
                    det.addKid(tblTracker.getTable("Taxon"));
                    det.addKid(tblTracker.getTable("DeterminationStatus"));
                    
                    ni = tblTracker.getNodeInfo("Taxon");
                    ni.addKid(tblTracker.getTable("TaxonCitation"));
                    
                    ni = tblTracker.getNodeInfo("CollectionObjectCitation");
                    ni.addKid(tblTracker.getTable("ReferenceWork"));
                    
                    ni = tblTracker.getNodeInfo("TaxonCitation");
                    ni.addKid(tblTracker.getTable("ReferenceWork"));
                    
                    ni = tblTracker.getNodeInfo("ReferenceWork");
                    ni.setOkToDuplicate(true);
                    ni.addKid(tblTracker.getTable("Journal"));
        
                    ni = tblTracker.getNodeInfo("GeologicTimePeriod");
                    ni.setOkToDuplicate(true);
                    
                    ni = tblTracker.getNodeInfo("Journal");
                    ni.setOkToDuplicate(true);
                    
                    ni = tblTracker.getNodeInfo("CollectingEvent");
                    ni.addKid(tblTracker.getTable("Locality"));
                    
                    ni = tblTracker.getNodeInfo("CollectionObject");
                    ni.addKid(tblTracker.getTable("CollectingEvent"));
                    ni.addKid(tblTracker.getTable("PaleoContext"));
                    ni.addKid(tblTracker.getTable("Accession"));
                    
                    ni = tblTracker.getNodeInfo("Locality");
                    ni.addKid(tblTracker.getTable("Geography"));
                    
                    ni = tblTracker.getNodeInfo("PaleoContext");
                    ni.addKid(tblTracker.getTable("LithoStrat"));
                    ni.addKid(tblTracker.getTable("GeologicTimePeriod"));
                    
                    ni = tblTracker.getNodeInfo("DeterminationCitation");
                    ni.addKid(tblTracker.getTable("ReferenceWork"));
                    
                    ni = tblTracker.getNodeInfo("Preparation");
                    ni.addKid(tblTracker.getTable("PrepType"));
                    
                    ni = tblTracker.getNodeInfo("Accession");
                    ni.addKid(tblTracker.getTable("AccessionAuthorization"));
                    ni.addKid(tblTracker.getTable("AccessionAgent"));
                    ni.addKid(tblTracker.getTable("RepositoryAgreement"));
                }
 
            }

            if (false)
            {
                NodeInfo ni = tblTracker.getNodeInfo("Agent");
                ni.setOkToDuplicate(true);
                
                String[] toAddAgent = {"CollectionObject", "Accession", "Determination", "Preparation", };
                for (String name : toAddAgent)
                {
                    ni = tblTracker.getNodeInfo(name);
                    ni.addKid(tblTracker.getTable("Agent"));
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
                DBTableInfo ti = t.getTable();
                output.write("<LI><a href=\""+ti.getShortClassName()+".html\">"+StringEscapeUtils.escapeHtml(ti.getTitle())+"</a></LI>");
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
        for (DBRelationshipInfo r : table.getTable().getRelationships())
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
        
        for (DBRelationshipInfo r : table.getTable().getRelationships())
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
        
        BufferedImage bufImage = new BufferedImage(rect.width, rect.height, (doPNG ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB));
        Graphics2D    g2       = bufImage.createGraphics();
        if (!doPNG)
        {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, rect.width, rect.height);
        }
        g2.setRenderingHints(createTextRenderingHints());
        g2.setFont( tblTracker.getFont());
        getPanel().getParent().paint(g2);

        g2.dispose();
        
        Component stop = getPanel().getParent();
        Point p = new Point(0,0);
        calcLoc(p, getPanel().getMainTable(), stop);
        
        String      name    = StringUtils.substringAfterLast(getPanel().getMainTable().getClassName(), ".");
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(name);
        String fName = schemaDir.getAbsolutePath() + File.separator + name;
        try
        {
            File           html   = new File(fName + ".html");
            BufferedWriter output = new BufferedWriter( new FileWriter(html) );
            
            int    inx        = mapTemplate.indexOf(contentTag);
            String subContent = mapTemplate.substring(0, inx);
            output.write(StringUtils.replace(subContent, "<!-- Title -->", tblInfo.getTitle()));
        
            File imgFile = new File(fName + (doPNG ? ".png" : ".jpg"));
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
            if (doPNG)
            {
                ImageIO.write(bufImage, "PNG", oFile);
            } else
            {
                writeJPEG(oFile, bufImage, 0.75f);
            }
            //ImageIO.write(bufImage, "JPG", new File(schemaDir + File.separator + imgFile.getName()+".jpg"));
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void writeJPEG(File outfile, BufferedImage bufferedImage, float compressionQuality)
    {
        try
        {
        
            long start = System.currentTimeMillis();
            boolean oldWay = true;
            if (oldWay)
            {

                // Find a jpeg writer
                ImageWriter writer = null;
                Iterator<?> iter = ImageIO.getImageWritersByFormatName("jpg");
                if (iter.hasNext())
                {
                    writer = (ImageWriter)iter.next();
                }

                // Prepare output file
                ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
                writer.setOutput(ios);

                // Set the compression quality
                ImageWriteParam iwparam = new MyImageWriteParam();
                iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
                iwparam.setCompressionQuality(compressionQuality);

                // Write the image
                writer.write(null, new IIOImage(bufferedImage, null, null), null);

                // Cleanup
                ios.flush();
                writer.dispose();
                ios.close();

            } else
            {
                FileOutputStream out  = new FileOutputStream(outfile);

                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
                JPEGEncodeParam  param   = encoder.getDefaultJPEGEncodeParam(bufferedImage);
                param.setQuality(0.75f, false);
                encoder.setJPEGEncodeParam(param);
                encoder.encode(bufferedImage);
            }
            System.out.println(System.currentTimeMillis() - start);

        } catch (IOException e)
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

    /**
     * @param table
     * @param level
     */
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
            Hashtable<String, Boolean> usedRelClasses = new Hashtable<String, Boolean>();
            for (DBRelationshipInfo r : table.getTable().getRelationships())
            {
                ERDTable rTable = tblTracker.getHash().get(r.getClassName());
                if (rTable == null)
                {
                    continue;
                }
                
                if (usedRelClasses.get(r.getClassName()) != null)
                {
                    continue;
                }
                usedRelClasses.put(r.getClassName(), true);
                
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
                
                if (kidOK && (override || r.getType() == DBRelationshipInfo.RelationshipType.OneToMany))
                {
                    System.out.println("    ["+rTable.getClassName()+"]");
                    
                    if (rTable.getClassName().indexOf("FieldNote") > -1)
                    {
                        int x= 0;
                        x++;
                    }

                    if (table.addKid(rTable))
                    {
                        rTable.setTreeParent(table);
                        processAsTree(rTable, level+1);
                        
                        Dimension size = rTable.getSpace();
                        maxWidth  += size.width + 10;
                        maxHeight = Math.max(maxHeight, size.height+VGAP);
                    }
                }
            }
        }

        //System.out.println(level+"   mw "+maxWidth+"  mh "+ maxHeight+ "  " + table.getKids().size());
        table.getSpace().width  = Math.max(tableSize.width, maxWidth);
        table.getSpace().height = tableSize.height + maxHeight;
        System.out.println("Table "+table.getClassName()+" " + table.getSpace().width + " "+table.getSpace().height);
        tblTracker.getUsedHash().put(table.getClassName(), true);
    }

    // This class overrides the setCompressionQuality() method to workaround
    // a problem in compressing JPEG images using the javax.imageio package.
    public class MyImageWriteParam extends JPEGImageWriteParam
    {
        public MyImageWriteParam()
        {
            super(Locale.getDefault());
        }

        // This method accepts quality levels between 0 (lowest) and 1 (highest) and simply converts
        // it to a range between 0 and 256; this is not a correct conversion algorithm.
        // However, a proper alternative is a lot more complicated.
        // This should do until the bug is fixed.
        public void setCompressionQuality(float quality)
        {
            if (quality < 0.0F || quality > 1.0F)
            {
                throw new IllegalArgumentException("Quality out-of-bounds!");
            }
            this.compressionQuality = 256 - (quality * 256);
        }
    }

    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        System.setProperty(SchemaI18NService.factoryName, "edu.ku.brc.specify.config.SpecifySchemaI18NServiceXML");    // Needed for Localization and Schema
        
        //SchemaI18NService.setCurrentLocale(new Locale("de", "", ""));
        // Note: DisciplineId is not used so a '1' doesn't matter.
        SchemaI18NService.getInstance().loadWithLocale(SpLocaleContainer.CORE_SCHEMA, 1, DBTableIdMgr.getInstance(), SchemaI18NService.getCurrentLocale());

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                ERDVisualizer frame = new ERDVisualizer();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setBounds(0, 0, 800, 2000);
                frame.setVisible(true);
            }
        });
    }
    

}

