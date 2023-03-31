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
package edu.ku.brc.specify.tools;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getAppDataDir;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getStatusBar;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.getUserHomeDir;
import static edu.ku.brc.ui.UIRegistry.setBaseFont;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.ViewSetMgr;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewSetIFace;
import edu.ku.brc.af.ui.forms.validation.TypeSearchForQueryFactory;
import edu.ku.brc.helpers.HTTPGetter;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.helpers.ZipFileHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.utilapps.ERDVisualizer;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * May 20, 2008
 *
 */
public class FormDisplayer
{
    protected PrintWriter pw   = null;
    protected File        file = null;
    
    protected String contentTag = "<!-- Content -->"; //$NON-NLS-1$

    protected int MARGIN  = 10;
    protected int HGAP    = 60;
    protected int VGAP    = 30;
    
    protected String  mapTemplate = ""; //$NON-NLS-1$
    protected File    baseDir;
    protected File    outputDir;
    protected boolean doPNG       = true;
    protected boolean doAll       = true;
    
    protected List<Pair<String, File>> entries = new Vector<Pair<String, File>>();
    protected List<ViewIFace> viewList;
    protected AtomicBoolean   okToProc   = new AtomicBoolean(true);
    protected int             viewInx    = 0;
    protected JFrame          frame      = null;

    protected CustomDialog    cancelDlg;
    
    /**
     * 
     */
    public FormDisplayer()
    {
        UIHelper.adjustUIDefaults();
        
        setBaseFont((createLabel("")).getFont()); //$NON-NLS-1$
        
        Specify.setUpSystemProperties();
        
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(getAppDataDir());
        
        UIFieldFormatterMgr.setDoingLocal(true);
        DataObjFieldFormatMgr.setDoingLocal(true);
        TypeSearchForQueryFactory.setDoingLocal(true);
    }
    
    /**
     * @param fileName
     * @param title
     */
    protected void createHTMLFile(final String fileName, final String title)
    {
        try
        {
            file = new File(fileName);
            pw   = new PrintWriter(file);
            pw.print("<html><head><title>"+title+"</title></head><body>"); //$NON-NLS-1$ //$NON-NLS-2$
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormDisplayer.class, ex);
            ex.printStackTrace();
        }
    }

    
    /**
     * 
     */
    public void generateFormImages()
    {
        if (setup())
        {
            createFormImagesIndexFile();
        }
        
        int userChoice = JOptionPane.showConfirmDialog(getTopWindow(), 
                                                       getResourceString("FormDisplayer.CHOOSE_VIEWLIST"), //$NON-NLS-1$
                                                       getResourceString("FormDisplayer.CHOOSE_VIEWLIST_TITLE"),  //$NON-NLS-1$
                                                       JOptionPane.YES_NO_OPTION);
        
        doAll = userChoice == JOptionPane.YES_OPTION ? true : false;

        if (setup())
        {
            
            SpecifyAppContextMgr appContext = (SpecifyAppContextMgr)AppContextMgr.getInstance();
            
            viewList = doAll ? appContext.getEntirelyAllViews() : appContext.getAllViews();
            SwingUtilities.invokeLater(new Runnable() {
    
                /* (non-Javadoc)
                 * @see java.lang.Runnable#run()
                 */
                public void run()
                {
                    showView();
                }
            });
        }
        
        JButton stopBtn = UIHelper.createButton("Stop Generating Images");
        PanelBuilder pb = new PanelBuilder(new FormLayout("p", "p"));
        pb.add(stopBtn, (new CellConstraints()).xy(1, 1));
        pb.setDefaultDialogBorder();
        cancelDlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), "Stop Image Generation", false, CustomDialog.OK_BTN, pb.getPanel());
        cancelDlg.setOkLabel(getResourceString("CLOSE"));
        
        cancelDlg.setAlwaysOnTop(true);
        cancelDlg.setVisible(true);
        
        //Insets    screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(cancelDlg.getGraphicsConfiguration());;
        Rectangle screenRect = cancelDlg.getGraphicsConfiguration().getBounds();
        int y = screenRect.height - (cancelDlg.getSize().height*2);
        cancelDlg.setLocation(screenRect.x, y);
        
        stopBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        synchronized (okToProc)
                        {
                            okToProc.set(false);
                            viewInx  = viewList.size();
                        }
                    }
                });
            }
        });
    }
    
    /**
     * 
     */
    protected void showView()
    {
        boolean done = false;
        
        synchronized (okToProc)
        {
            if (okToProc.get())
            {
                if (frame != null)
                {
                    generateViewImage(viewList.get(viewInx));
                    frame.dispose();
                }
            
                viewInx++;
                
                ViewIFace view = viewList.get(viewInx);
                System.out.println(view.getName());
                
                if (!view.getViewSetName().equals("Editor")) //$NON-NLS-1$
                {
                    Object data = null;
                    try
                    {
                        ViewDefIFace viewDef = view.getAltViews().get(0).getViewDef();
                        if (!(viewDef.getDataGettable() instanceof edu.ku.brc.af.ui.forms.DataGetterForHashMap))
                        {
                            Class<?> dataCls = Class.forName(viewDef.getClassName());
                            if (dataCls != null)
                            {
                                System.err.println(dataCls);
                                data = dataCls.newInstance();
                                if (data instanceof FormDataObjIFace)
                                {
                                    ((FormDataObjIFace)data).initialize();
                                }
                            }
                        }
                    } catch (Exception ex) {}
                    
                    if (data instanceof Discipline)
                    {
                        ((Discipline)data).setType("fish");
                    }
                    FormPane formPane = new FormPane(view.getName(), null, null, view.getName(), "edit", data, MultiView.IS_NEW_OBJECT | MultiView.HIDE_SAVE_BTN, true); //$NON-NLS-1$
                    frame = new JFrame();
                    frame.setFocusable(false);
                    frame.setContentPane(formPane);
                    frame.setSize(1024, 768);
                    frame.setVisible(true);
                    
                    frame.setLocation(0, 0);
                    
                    Dimension size = frame.getContentPane().getPreferredSize();
                    size.height += 40;
                    size.width  += 30;
                    frame.setSize(size);
                    
                    String str = String.format("%d of %d", viewInx+1, viewList.size());
                    System.out.println(str); //$NON-NLS-1$
                    getStatusBar().setText(str); //$NON-NLS-1$
           
                } else if (frame != null)
                {
                    frame.setVisible(false);
                    frame.dispose();
                    frame = null;
                }
                
                if (viewInx < viewList.size()-1)
                {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Thread.sleep(500);
                            } catch (Exception ex)
                            {
                                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormDisplayer.class, ex);
                            }
                            showView();
                        }
                    });
                } else
                {
                    done = true;
                }
            } else
            {
                done = true;
            }
        }
        
        if (done) 
        {
            if (cancelDlg != null) cancelDlg.setVisible(false);
            if (frame != null)
            {
                frame.setVisible(false);
                frame.dispose();
            }
            
            createIndexFile();
            
            getStatusBar().setText("Done"); //$NON-NLS-1$
        }
    }
    
    /**
     * @param title
     * @param fileName
     * @param prevName
     * @param nxtName
     */
    protected void writeImageFile(final String title, 
                                  final String fileName,
                                  final String prevName,
                                  final String nxtName)
    {
        String fName = outputDir.getAbsolutePath() + File.separator + FilenameUtils.getBaseName(fileName) + ".html"; //$NON-NLS-1$
        try
        {
            File html = new File(fName);
            BufferedWriter output = new BufferedWriter( new FileWriter(html) );
            
            int inx = mapTemplate.indexOf(contentTag);
            String subContent = mapTemplate.substring(0, inx);
            output.write(StringUtils.replace(subContent, "<!-- Title -->", title)); //$NON-NLS-1$
            output.write("<br>"); //$NON-NLS-1$
            if (prevName != null)
            {
                output.write("<a href=\""+prevName+"\">Previous</a>"); //$NON-NLS-1$ //$NON-NLS-2$
            } else
            {
                output.write(getResourceString("FormDisplayer.PREV")); //$NON-NLS-1$
            }
            output.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"); //$NON-NLS-1$
            if (nxtName != null)
            {
                output.write("<a href=\""+nxtName+"\">Next</a>"); //$NON-NLS-1$ //$NON-NLS-2$
            } else
            {
                output.write(getResourceString("FormDisplayer.NEXT")); //$NON-NLS-1$
            }
            output.write("<center><br><img src=\""+fileName+"\"><br><br>"+title); //$NON-NLS-1$ //$NON-NLS-2$
            output.write("</center>"); //$NON-NLS-1$
            output.write(mapTemplate.substring(inx+contentTag.length()+1, mapTemplate.length()));
            
            output.close();

        } catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormDisplayer.class, e);
            e.printStackTrace();
        }

    }
    
    /**
     * Setup output directory and get template etc.
     */
    protected void createIndexFile()
    {
        String fName = outputDir.getAbsolutePath() + File.separator + "index.html"; //$NON-NLS-1$
        try
        {
            File html = new File(fName);
            BufferedWriter output = new BufferedWriter( new FileWriter(html) );
            
            int inx = mapTemplate.indexOf(contentTag);
            String subContent = mapTemplate.substring(0, inx);
            output.write(StringUtils.replace(subContent, "<!-- Title -->", getResourceString("FormDisplayer.FORM_INDEX"))); //$NON-NLS-1$ //$NON-NLS-2$
            output.write("<UL>"); //$NON-NLS-1$
            
            Collections.sort(entries, new Comparator<Pair<String, File>>() {
                public int compare(Pair<String, File> o1, Pair<String, File> o2)
                {
                    return o1.first.compareTo(o2.first);
                }
                
            });
            int cnt = 0;
            for (Pair<String, File> p : entries)
            {
                output.write("<LI><a href=\""+FilenameUtils.getBaseName(p.second.getName())+".html\">"+p.first+"</a></LI>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                String prvName = cnt == 0 ? null : FilenameUtils.getBaseName(entries.get(cnt-1).second.getName())+".html"; //$NON-NLS-1$
                String nxtName = cnt == entries.size()-1 ? null : FilenameUtils.getBaseName(entries.get(cnt+1).second.getName())+".html"; //$NON-NLS-1$
                writeImageFile(p.first, p.second.getName(), prvName, nxtName);
                cnt++;
            }
            output.write("</UL>"); //$NON-NLS-1$
            output.write(mapTemplate.substring(inx+contentTag.length()+1, mapTemplate.length()));
            
            output.close();

        } catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormDisplayer.class, e);
            e.printStackTrace();
        }
        
        createFormImagesIndexFile();
        
        JOptionPane.showMessageDialog(getTopWindow(), String.format(getResourceString("FormDisplayer.OUTPUT"), outputDir.getAbsoluteFile()));
    }
    
    /**
     * Setup output directory and get template etc.
     */
    protected void createFormImagesIndexFile()
    {
        String fName = baseDir.getAbsolutePath() + File.separator + "index.html"; //$NON-NLS-1$
        try
        {
            File html = new File(fName);
            BufferedWriter output = new BufferedWriter( new FileWriter(html) );
            
            int inx = mapTemplate.indexOf(contentTag);
            String subContent = mapTemplate.substring(0, inx);
            output.write(StringUtils.replace(subContent, "<!-- Title -->", getResourceString("FormDisplayer.FORM_INDEX"))); //$NON-NLS-1$ //$NON-NLS-2$
            output.write("<UL>\n"); //$NON-NLS-1$
            
            List<File> files = new Vector<File>();
            for (File f : baseDir.listFiles())
            {
                String nm = f.getName();
                if (nm.endsWith("_all") || nm.endsWith("_user")) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    files.add(f);
                }
            }
            
            Collections.sort(files, new Comparator<File>() {
                public int compare(File o1, File o2)
                {
                    return o1.getName().compareTo(o2.getName());
                }
                
            });
            
            String currDiscipline = "";
            for (File f : files)
            {
                String[] segments = StringUtils.split(f.getName(), "_");//$NON-NLS-1$
                if (segments != null && segments.length == 3)
                {
                    DisciplineType dt = DisciplineType.getDisciplineHash().get(segments[0]);
                    if (!dt.getName().equals(currDiscipline))
                    {
                        if (currDiscipline.length() > 0) output.write("</UL>\n"); //$NON-NLS-1$
                        output.write("<LI>"+dt.getTitle()+"</LI>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                        output.write("<UL>\n"); //$NON-NLS-1$
                        currDiscipline = dt.getName();
                    }
                    String title = String.format("%s %s", segments[1], (segments[2].equals("all") ? "All" : "User"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    output.write("<LI><a href=\""+f.getName() + File.separator + "index.html\">"+title+"</a></LI>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
            output.write("</UL>\n"); //$NON-NLS-1$
            output.write("</UL>\n"); //$NON-NLS-1$
            output.write(mapTemplate.substring(inx+contentTag.length()+1, mapTemplate.length()));
            
            output.close();

        } catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormDisplayer.class, e);
            e.printStackTrace();
        }
    }
    
    /**
     * 
     */
    private File checkForTemplateFiles(final String dstDirPath)
    {
        String templatePath = dstDirPath + File.separator + "schema_template.html";
        
        File templateFile = new File(templatePath); //$NON-NLS-1$
        if (templateFile.exists())
        {
            return templateFile;
        }
        
        System.out.println(templatePath);
        try
        {
            File dstDirFile = new File(dstDirPath);
            if (!dstDirFile.exists())
            {
                if (!dstDirFile.mkdirs())
                {
                    JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow() != null ? UIRegistry.getMostRecentWindow() : UIRegistry.getTopWindow(), "Error creating the site directory.");
                }
            }
            
            String zipFilePath = dstDirPath + File.separator + "site.zip";
            System.out.println("["+zipFilePath+"]");
            
            String           url    = "https://files.specifysoftware.org/site.zip";
            HTTPGetter       getter = new HTTPGetter();
            InputStream      ins    = getter.beginHTTPRequest(url);
            //DataInputStream  dins   = new DataInputStream(ins);
            DataOutputStream dos    = new DataOutputStream(new FileOutputStream(zipFilePath));
            byte[]           bytes  = new byte[4096];
            
            int totalBytes = 0;
            /*while (dins.available() > 0)
            {
                int len = dins.read(bytes);
                dos.write(bytes, 0, len);
                totalBytes += len;
                System.out.println(len+" / "+totalBytes);
            }*/
            int numBytes = 0;
            do 
            {
                numBytes = ins.read(bytes);
                if (numBytes > 0)
                {
                    dos.write(bytes, 0, numBytes);
                    totalBytes += numBytes;
                    System.out.println(numBytes);
                }
                
            } while (numBytes > 0);
            
            dos.flush();
            dos.close();
            //dins.close();
            
            System.out.println(totalBytes);
            
            
            File       zipFile       = new File(zipFilePath);
            System.out.println("zipFile: "+zipFile+" exists: "+zipFile.exists());
            
            List<File> unzippedFiles = ZipFileHelper.getInstance().unzipToFiles(zipFile);
            for (File unzippedFile : unzippedFiles)
            {
                FileUtils.copyFileToDirectory(unzippedFile, dstDirFile);
            }
            
            if (templateFile.exists())
            {
                return templateFile;
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow() != null ? UIRegistry.getMostRecentWindow() : UIRegistry.getTopWindow(), "You are missing the template that is needed to run this tool.");
        }
        return null;
    }
    
    /**
     * 
     */
    protected boolean setup()
    {
        String pathStr = AppContextMgr.getInstance().getClassObject(Discipline.class) != null ? AppContextMgr.getInstance().getClassObject(Discipline.class).getType() : ""; //$NON-NLS-1$
        pathStr += "_" + UIHelper.getOSType().toString() + "_" + (doAll ? "all" : "user");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        baseDir   = new File(getUserHomeDir() + File.separator + "FormImages"); //$NON-NLS-1$
        outputDir = new File(baseDir.getAbsoluteFile() + File.separator + pathStr);
        
        if (!baseDir.exists())
        {
            if (!baseDir.mkdir())
            {
                UIRegistry.showError(String.format(getResourceString("FormDisplayer.OUTPUT_ERROR"), baseDir.getAbsoluteFile()));
            }
        }
        
        if (!outputDir.exists())
        {
            if (!outputDir.mkdir())
            {
                UIRegistry.showError(String.format(getResourceString("FormDisplayer.OUTPUT_ERROR"), outputDir.getAbsoluteFile()));
            }
        } else
        {
            try
            {
                FileUtils.cleanDirectory(outputDir);
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormDisplayer.class, ex);
                ex.printStackTrace();
            }
        }
        
        String dstDirPath   = UIRegistry.getDefaultUserHomeDir() + File.separator + "Specify/site";
        try
        {
            File tmplateFile = checkForTemplateFiles(dstDirPath);
            mapTemplate = FileUtils.readFileToString(tmplateFile);
            mapTemplate = StringUtils.replace(mapTemplate, "Database Schema", getResourceString("FormDisplayer.FORMS")); //$NON-NLS-1$ //$NON-NLS-2$

        } catch (IOException ex)
        {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow() != null ? UIRegistry.getMostRecentWindow() : UIRegistry.getTopWindow(), "You are missing the template that is needed to run this tool.");
            return false;
        }
        
        if (StringUtils.isEmpty(mapTemplate))
        {
            System.err.println("The template file is empty!"); //$NON-NLS-1$
        }
        
        try
        {
            File srcDir = new File(dstDirPath); //$NON-NLS-1$
            for (File f : srcDir.listFiles())
            {
                if (!f.getName().startsWith(".")) //$NON-NLS-1$
                {
                    File dst = new File(outputDir.getAbsolutePath() + File.separator + f.getName()); //$NON-NLS-1$
                    if (!FilenameUtils.getExtension(f.getName()).toLowerCase().equals("html")) //$NON-NLS-1$
                    {
                        FileUtils.copyFile(f, dst);
                    }
                    
                    dst = new File(baseDir.getAbsolutePath() + File.separator + f.getName()); //$NON-NLS-1$
                    if (!FilenameUtils.getExtension(f.getName()).toLowerCase().equals("html")) //$NON-NLS-1$
                    {
                        FileUtils.copyFile(f, dst);
                    }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormDisplayer.class, ex);
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    
    /**
     * Generates an Image for the View.
     */
    protected void generateViewImage(final ViewIFace view)
    {
        Rectangle     rect     = frame.getContentPane().getBounds();
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
        g2.setRenderingHints(ERDVisualizer.createTextRenderingHints());
        frame.getContentPane().paint(g2);

        g2.dispose();
        
        String baseFileName = outputDir.getAbsoluteFile()+"/"+view.getName()+"_"+viewInx; //$NON-NLS-1$ //$NON-NLS-2$
        File   imgFile      = new File(baseFileName+(doPNG ?".png":".jpg")); //$NON-NLS-1$ //$NON-NLS-2$
        System.out.println(imgFile.getAbsolutePath());
        try
        {
            ImageIO.write(bufImage, "PNG", imgFile); //$NON-NLS-1$
            
            entries.add(new Pair<String, File>(view.getName(), imgFile));
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FormDisplayer.class, ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * @param viewSetMgrName
     * @param dir
     */
    protected void processDir(final String viewSetMgrName, final File dir)
    {
        ViewSetMgr viewSetMgr = new ViewSetMgr(viewSetMgrName, dir);
        
        pw.println("<br><h2>"+viewSetMgrName+"</h2>"); //$NON-NLS-1$ //$NON-NLS-2$
        if (viewSetMgr.getViewSets().size() > 0)
        {
            for (ViewSetIFace vs : viewSetMgr.getViewSets())
            {
                if (!vs.getName().equals("Editor")) //$NON-NLS-1$
                {
                    System.out.println(vs.getName());
                    if (vs.getViews().keySet().size() > 0)
                    {
                        pw.println("<h3>"+vs.getName()+"</h3>"); //$NON-NLS-1$ //$NON-NLS-2$
                        
                        pw.println("<table border=\"1\" width=\"50%\">"); //$NON-NLS-1$
                        pw.println("<tr><th>View Name</th><th>Is Internal</th><th>Types</th></tr>"); //$NON-NLS-1$
            
                        Vector<String> vNames = new Vector<String>(vs.getViews().keySet());
                        Collections.sort(vNames);
                        for (String vName : vNames)
                        {
                            ViewIFace view = vs.getViews().get(vName);
                            pw.println("<tr><td>"+view.getName()+"</td>"); //$NON-NLS-1$ //$NON-NLS-2$
                            
                            StringBuilder sb = new StringBuilder();
                            for (AltViewIFace av : view.getAltViews())
                            {
                                if (sb.length() > 0) sb.append(", "); //$NON-NLS-1$
                                if (av.getViewDef() != null)
                                {
                                    sb.append(av.getViewDef().getType().toString()+" ("+av.getMode().toString().toLowerCase() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                            }
                            pw.println("<td><center>"+view.isInternal()+"</center></td>"); //$NON-NLS-1$ //$NON-NLS-2$
                            pw.println("<td>"+sb.toString()+"</td></tr>"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        pw.println("</table><br>"); //$NON-NLS-1$
                    } else
                    {
                        pw.println("<i>"+getResourceString("FormDisplayer.NO_VIEWS_DEFINED")+"</i><br>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
            }
        } else
        {
            pw.println("<i>No viewset_registry.xml</i><br>"); //$NON-NLS-1$
        }
        
        if (dir != null && dir.isDirectory())
        {
            System.out.println(dir.getAbsolutePath());
            for (File file : dir.listFiles())
            {
                if (file.isDirectory() && !file.getName().startsWith(".")) //$NON-NLS-1$
                {
                    processDir(viewSetMgrName+"/"+file.getName(), file); //$NON-NLS-1$
                } 
            }
        }
    }
    
    /**
     * Creates an HTML file listing all the forms/Views.
     */
    public void createViewListing(final String path, final boolean doShowInBrowser)
    {
        String fullPath = (path != null ? path + File.separator : "") + "views.html"; //$NON-NLS-1$ //$NON-NLS-2$
        
        createHTMLFile(fullPath, getResourceString("FormDisplayer.VIEWS")); //$NON-NLS-1$
        
        processDir("Common", XMLHelper.getConfigDir("common")); //$NON-NLS-1$ //$NON-NLS-2$
        processDir("Backstop", XMLHelper.getConfigDir("backstop")); //$NON-NLS-1$ //$NON-NLS-2$
        
        for (File file : new File(XMLHelper.getConfigDirPath(".")).listFiles()) //$NON-NLS-1$
        {
            if (file.isDirectory() && 
                !file.getName().equals("common") &&  //$NON-NLS-1$
                !file.getName().equals("backstop") &&  //$NON-NLS-1$
                !file.getName().startsWith(".")) //$NON-NLS-1$
            {
                processDir(file.getName(), XMLHelper.getConfigDir(file.getName()));
            }
        }
        pw.println("</body><html>"); //$NON-NLS-1$
        pw.flush();
        pw.close();
        
        try
        {
            if (doShowInBrowser)
            {
                AttachmentUtils.openURI(file.toURI());
            } else
            {
                JOptionPane.showMessageDialog(getTopWindow(), String.format( getResourceString("FormDisplayer.OUTPUT"), file.getCanonicalFile()));
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        FormDisplayer fd = new FormDisplayer();
        fd.createViewListing(null, true);
    }

}
