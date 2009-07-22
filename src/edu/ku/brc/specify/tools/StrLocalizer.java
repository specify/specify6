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
package edu.ku.brc.specify.tools;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.af.core.FrameworkAppIFace;
import edu.ku.brc.af.core.MacOSAppHandler;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.specify.tools.StrLocaleEntry.STATUS;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostGlassPane;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 15, 2009
 *
 */
public class StrLocalizer extends JPanel implements FrameworkAppIFace
{
    private static final Logger  log                = Logger.getLogger(StrLocalizer.class);
    
    protected GhostGlassPane glassPane;
    
    protected StrLocaleFile englishFile;
    protected StrLocaleFile spanishFile;
    
    protected JList      termList;
    protected JList      newTermList;
    
    protected JTextArea  englishLbl;
    protected JTextField textField;
    protected JButton    prevBtn;
    protected JButton    nxtBtn;
    protected JButton    transBtn;
    protected JPanel     mainPane;
    
    protected JMenuItem  startTransMenuItem;
    protected JMenuItem  stopTransMenuItem;
    
    protected ResultSetController rsController;
    protected int        oldInx = -1;
    
    protected Vector<String> newKeyList = new Vector<String>();
    protected AtomicBoolean  contTrans  = new AtomicBoolean(true);
    
    
    /**
     * 
     */
    public StrLocalizer()
    {
        super();
        
        new MacOSAppHandler(this);
        
        // "src/resources_en.properties"
        
        String path = "src/resources_en.properties";
        englishFile = new StrLocaleFile(path, null, false);
        spanishFile = new StrLocaleFile("src/resources_es.properties", path, true);
        
        mergeToSrc(englishFile, spanishFile);
        
        createUI();
    }
    
    /**
     * 
     */
    private void createUI()
    {
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,10px,f:p:g", "f:p:g"), this);
        
        termList   = new JList(new ItemModel(englishFile));
        newTermList = new JList(newKeyList);
        
        int len = newKeyList.size();
        
        englishLbl = UIHelper.createTextArea(3, 40);
        textField  = UIHelper.createTextField(40);
        
        englishLbl.setEditable(false);
        
        rsController = new ResultSetController(null, false, false, false, "", englishFile.size(), true);
        
        transBtn = UIHelper.createButton("Translate");
        
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder pbr = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,p,4px,p,4px,p"));
        pbr.add(UIHelper.createLabel("English:"), cc.xy(1, 1));
        pbr.add(englishLbl, cc.xy(3, 1));
        
        pbr.add(UIHelper.createLabel("Spanish:"), cc.xy(1, 3));
        pbr.add(textField,                        cc.xy(3, 3));
        
        pbr.add(rsController.getPanel(),          cc.xyw(1, 5, 3));
        pbr.add(transBtn,                         cc.xy(1, 7));
        
        PanelBuilder pbl = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,10px,p,4px,f:p:g"));

        JScrollPane sp  = UIHelper.createScrollPane(termList);
        JScrollPane nsp = UIHelper.createScrollPane(newTermList);
        pbl.add(sp,                   cc.xy(1,1));
        pbl.addSeparator("New Items", cc.xy(1, 3));
        pbl.add(nsp,                  cc.xy(1,5));
        
        
        pb.add(pbl.getPanel(),  cc.xy(1,1));
        pb.add(pbr.getPanel(),  cc.xy(3,1));
        
        ResultSetController.setBackStopRS(rsController);
        
        pb.setDefaultDialogBorder();
        
        mainPane = pb.getPanel();
        
        termList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                listSelected();
            }
        });
        
        newTermList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                newListSelected();
            }
        });
        
        transBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String txt = englishLbl.getText().replace("\\n", " ");
                
                String newText = translate(txt);
                if (StringUtils.isNotEmpty(newText))
                {
                    newText = newText.replace("&#039;", "'");
                    textField.setText(newText);
                }
            }
        });
    }
    
    
    /**
     * 
     */
    private void listSelected()
    {
        if (oldInx > -1)
        {
            StrLocaleEntry entry = englishFile.get(oldInx);
            entry.setDstStr(textField.getText());
        }
        
        int inx = termList.getSelectedIndex();
        if (inx > -1)
        {

            StrLocaleEntry entry = englishFile.get(inx);
            englishLbl.setText(entry.getSrcStr());
            
            String str = entry.getDstStr();
            textField.setText(str != null ? str : "");
            
            rsController.setIndex(inx);
        } else
        {
            englishLbl.setText("");
            textField.setText("");
        }
        oldInx = inx;
    }
    
    
    /**
     * 
     */
    private void newListSelected()
    {
        String key = (String)newTermList.getSelectedValue();
        if (key != null)
        {
            Integer inx = englishFile.getInxForKey(key);
            if (inx != null)
            {
                termList.setSelectedIndex(inx);
                termList.ensureIndexIsVisible(inx);
            }
        }
    }
    
    /**
     * @param src
     * @param dst
     */
    private void mergeToSrc(final StrLocaleFile src, final StrLocaleFile dst)
    {
        Hashtable<String, StrLocaleEntry> dstHash = dst.getItemHash();
        
        int cnt = 0;
        for (StrLocaleEntry srcEntry : src.getItems())
        {
            if (srcEntry.isValue())
            {
                StrLocaleEntry dstEntry = dstHash.get(srcEntry.getKey());
                if (dstEntry != null)
                {
                    srcEntry.setDstStr(dstEntry.getSrcStr());
                    srcEntry.setStatus(dstEntry.getStatus());
                    
                    if (dst.isSrcSameAsDest(srcEntry.getKey(), srcEntry.getSrcStr()))
                    {
                        if (StringUtils.isEmpty(dstEntry.getSrcStr()))
                        {
                            newKeyList.add(srcEntry.getKey());
                        }
                    }
                    
                } else
                {
                    dstEntry = new StrLocaleEntry(srcEntry.getKey(), srcEntry.getSrcStr(), null, STATUS.IsNew);
                    dstHash.put(srcEntry.getKey(), dstEntry);
                    
                    newKeyList.add(srcEntry.getKey());
                }
                cnt++;
            }
        }
        System.out.println(cnt);
    }
    
    /**
     * @param src
     * @param dst
     */
    private void mergeToDst(final StrLocaleFile src, final StrLocaleFile dst)
    {
        //Hashtable<String, StrLocaleEntry> srcHash = src.getItemHash();
        Hashtable<String, StrLocaleEntry> dstHash = dst.getItemHash();
        
        Vector<StrLocaleEntry> srcItems = src.getItems();
        Vector<StrLocaleEntry> dstItems = dst.getItems();
        
        dstItems.clear();
        
        for (StrLocaleEntry srcEntry : srcItems)
        {
            String key = srcEntry.getKey();
            if (key == null || key.equals("#"))
            {
                dstItems.add(srcEntry);
            } else
            {
                StrLocaleEntry dstEntry = dstHash.get(key);
                if (dstEntry != null)
                {
                    dstEntry.setDstStr(srcEntry.getDstStr());
                    dstItems.add(dstEntry);
                } else
                {
                    dstItems.add(srcEntry);
                }
            }
        }
    }
    
    private void translateNewItems()
    {
        UIRegistry.writeGlassPaneMsg("Translating new items...", 24);
        
        startTransMenuItem.setEnabled(false);
        stopTransMenuItem.setEnabled(true);
        
        final double total = newKeyList.size();
        final Random rand = new Random();
        
        SwingWorker<Integer, Integer> translator = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                int count = 0;
                for (String key : newKeyList)
                {
                    StrLocaleEntry entry = englishFile.getItemHash().get(key);
                    if (StringUtils.isEmpty(entry.getDstStr()))
                    {
                        entry.setDstStr(translate(entry.getSrcStr()));
                    }
                    
                    glassPane.setProgress((int)( (100.0 * count) / total));
                    count++;
                    
                    if (count % 3 == 0)
                    {
                        try
                        {
                            Thread.sleep((int)(rand.nextDouble()*15000.0));
                        } catch (Exception ex) {}
                    }
                    if (!contTrans.get())
                    {
                        return null;
                    }
                }
                return null;
            }

            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#done()
             */
            @Override
            protected void done()
            {
                glassPane.setProgress(100);
                UIRegistry.clearGlassPaneMsg();
                
                startTransMenuItem.setEnabled(true);
                stopTransMenuItem.setEnabled(false);

            }
        };
        translator.execute();
    }
 
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doAbout()
     */
    @Override
    public void doAbout()
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doExit(boolean)
     */
    @Override
    public boolean doExit(boolean doAppExit)
    {
        System.exit(0);
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doPreferences()
     */
    @Override
    public void doPreferences()
    {
    }
    
    public void checkForAutoTranslation()
    {
        
    }
    
    /**
     * 
     */
    private void doOpen()
    {
        FileDialog dlg = new FileDialog((JFrame)UIRegistry.getTopWindow(), "Open a properties I18N File", FileDialog.LOAD);
        dlg.setFilenameFilter(new FilenameFilter(){
            public boolean accept(File dir, String name){
              return (name.endsWith(".properties"));
            }
         });
        dlg.setVisible(true);
        String fileName = dlg.getFile();
        if (fileName != null)
        {
            termList.clearSelection();
            newTermList.clearSelection();
            
            String path = dlg.getDirectory() + File.separator + fileName;
            String spanishPath = path.replace("_en.", "_es.");
            
            englishFile = new StrLocaleFile(path, null, false);
            spanishFile = new StrLocaleFile(spanishPath, path, true);
            
            newKeyList.clear();
            
            mergeToSrc(englishFile, spanishFile);
            
            termList.setModel(new ItemModel(englishFile));
            DefaultListModel model = new DefaultListModel();
            model.clear();
            for (String str : newKeyList)
            {
                model.addElement(str);
            }
            newTermList.setModel(model);
        }
    }
    
    /**
     * 
     */
    private void doSave()
    {
        termList.clearSelection();
        
        mergeToDst(englishFile, spanishFile);
        
        spanishFile.save();
    }
    
    /**
     * @param frame
     */
    public void addMenuBar(final JFrame frame)
    {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        
        
        JMenuItem openItem = new JMenuItem("Open");
        fileMenu.add(openItem);
        
        openItem.addActionListener(new ActionListener() {

            /* (non-Javadoc)
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doOpen();
            }
        });        
        JMenuItem saveItem = new JMenuItem("Save");
        fileMenu.add(saveItem);
        
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doSave();
            }
        });
        
        if (!UIHelper.isMacOS())
        {
            fileMenu.addSeparator();
            JMenuItem exitMenu = new JMenuItem("Exit");
            fileMenu.add(exitMenu);
        }
        menuBar.add(fileMenu);
        
        
        JMenu transMenu = new JMenu("Translate");
        menuBar.add(transMenu);
        
        startTransMenuItem = new JMenuItem("Start");
        transMenu.add(startTransMenuItem);
        
        startTransMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                 translateNewItems();
            }
        });
        
        stopTransMenuItem = new JMenuItem("Stop");
        transMenu.add(stopTransMenuItem);
        
        stopTransMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                contTrans.set(false);
            }
        });
        stopTransMenuItem.setEnabled(false);

        
        frame.setJMenuBar(menuBar);
        
        UIRegistry.setTopWindow(frame);
        
        UIRegistry.register(UIRegistry.MAINPANE, mainPane);
        UIRegistry.register(UIRegistry.FRAME, frame);
        
        
        frame.setGlassPane(glassPane = GhostGlassPane.getInstance());
        frame.setLocationRelativeTo(null);
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        UIRegistry.register(UIRegistry.GLASSPANE, glassPane);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(500);
                    //translateNewItems();
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            
        });
    }

    /**
     * @param inputText
     */
    protected String translate(final String inputText)
    {
        // check the website for the info about the latest version
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        
        // get the URL of the website to check, with usage info appended, if allowed
        String versionCheckURL = "http://babelfish.yahoo.com/translate_txt";
        
        PostMethod postMethod = new PostMethod(versionCheckURL);
        
        // get the POST parameters (which includes usage stats, if we're allowed to send them)
        NameValuePair[] postParams = createPostParameters(inputText, "en_es");
        postMethod.setRequestBody(postParams);
        
        // connect to the server
        try
        {
            int status = httpClient.executeMethod(postMethod);
            System.out.println(status);
            if (status == 200)
            {
                // get the server response
                //String responseString = postMethod.getResponseBodyAsString();
                
                InputStream iStream = postMethod.getResponseBodyAsStream();
                
                StringBuilder sb       = new StringBuilder();
                byte[]        bytes    = new byte[8196];
                int           numBytes = 0;
                do 
                {
                    numBytes = iStream.read(bytes);
                    if (numBytes > 0)
                    {
                       sb.append(new String(bytes, 0, numBytes));
                    }
                    
                } while (numBytes > 0);
                
                String responseString = sb.toString();
                
                
                if (StringUtils.isNotEmpty(responseString))
                {
                    //System.err.println(responseString);
                    
                    int inx = responseString.indexOf("<div id=\"result\">");
                    if (inx > -1)
                    {
                        int eInx = responseString.indexOf("</div></div>", inx);
                        int sInx = responseString.lastIndexOf(">", eInx-1);
                        
                        System.out.println(responseString.substring(sInx+1, eInx));
                        
                        return responseString.substring(sInx+1, eInx);
                    }
                }
            }   

        }
        catch (Exception e)
        {
            //e.printStackTrace();
            //UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(StatsTrackerTask.class, e);
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Creates an array of POST method parameters to send with the version checking / usage tracking connection.
     * 
     * @param doSendSecondaryStats if true, the POST parameters include usage stats
     * @return an array of POST parameters
     */
    protected NameValuePair[] createPostParameters(final String inputStr, final String src)
    {
        Vector<NameValuePair> postParams = new Vector<NameValuePair>();
        try
        {
            // get the install ID
            // get the OS name and version
            postParams.add(new NameValuePair("doit",      "done")); //$NON-NLS-1$
            postParams.add(new NameValuePair("ei",   "UTF-8")); //$NON-NLS-1$
            postParams.add(new NameValuePair("lp",   src)); //$NON-NLS-1$
            postParams.add(new NameValuePair("fr",   "bf-home")); //$NON-NLS-1$
            
            postParams.add(new NameValuePair("intl",      "1")); //$NON-NLS-1$
            postParams.add(new NameValuePair("tt",        "urltext")); //$NON-NLS-1$
            postParams.add(new NameValuePair("trtext",   inputStr)); //$NON-NLS-1$
            //postParams.add(new NameValuePair("wl_trglang",   dst)); //$NON-NLS-1$

            
            // create an array from the params
            NameValuePair[] paramArray = new NameValuePair[postParams.size()];
            for (int i = 0; i < paramArray.length; ++i)
            {
                paramArray[i] = postParams.get(i);
            }
            
            return paramArray;
        
        } catch (Exception ex)
        {
            //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(StatsTrackerTask.class, ex);
            ex.printStackTrace();
        }
        return null;
    }


    /**
     * @param args
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    UIHelper.OSTYPE osType = UIHelper.getOSType();
                    if (osType == UIHelper.OSTYPE.Windows )
                    {
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new ExperienceBlue());
                        
                    } else if (osType == UIHelper.OSTYPE.Linux )
                    {
                        UIManager.setLookAndFeel(new PlasticLookAndFeel());
                    }
                }
                catch (Exception e)
                {
                    log.error("Can't change L&F: ", e); //$NON-NLS-1$
                }
                
                JFrame frame = new JFrame("String Localizer");
                StrLocalizer sl = new StrLocalizer();
                sl.addMenuBar(frame);
                frame.setContentPane(sl);
                Dimension size = frame.getPreferredSize();
                size.height = 500;
                frame.setSize(size);
                UIHelper.centerAndShow(frame);
            }
        });
    }

    //-------------------------------------------------------------------------------
    //-- List Model
    //-------------------------------------------------------------------------------
    class ItemModel extends AbstractListModel
    {
        protected StrLocaleFile file;
        
        public ItemModel(final StrLocaleFile file)
        {
            this.file = file;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.ListModel#getElementAt(int)
         */
        @Override
        public Object getElementAt(int index)
        {
            StrLocaleEntry entry = file.get(index);
            return entry != null ? entry.getKey() : "";
        }

        /* (non-Javadoc)
         * @see javax.swing.ListModel#getSize()
         */
        @Override
        public int getSize()
        {
            return file.size();
        }
    }
}
