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

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIRegistry.FRAME;
import static edu.ku.brc.ui.UIRegistry.GLASSPANE;
import static edu.ku.brc.ui.UIRegistry.MAINPANE;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.register;
import static edu.ku.brc.ui.UIRegistry.setAppName;
import static edu.ku.brc.ui.UIRegistry.setTopWindow;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.BackingStoreException;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.af.core.FrameworkAppIFace;
import edu.ku.brc.af.core.MacOSAppHandler;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.SearchBox;
import edu.ku.brc.af.ui.db.JAutoCompTextField;
import edu.ku.brc.af.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.tools.StrLocaleEntry.STATUS;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
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
@SuppressWarnings("serial")
public class StrLocalizerApp extends JPanel implements FrameworkAppIFace, WindowListener
{
    private static final Logger  log = Logger.getLogger(StrLocalizerApp.class);
    
    protected JFrame         frame = null;
    protected GhostGlassPane glassPane;

    protected File          rootDir      = null;
    protected File          baseDir      = null;
    protected String        srcLangCode  = "en";
    protected String        currentPath  = null;
    protected LanguageEntry destLanguage = null;
    protected Locale        englishLocale = Locale.US;
    
    protected StrLocaleFile srcFile;
    protected StrLocaleFile destFile;
    
    protected Vector<StrLocaleFile> srcFiles = new Vector<StrLocaleFile>();
    protected Vector<StrLocaleFile> destFiles = new Vector<StrLocaleFile>();
    
    protected Vector<LanguageEntry> languages = new Vector<LanguageEntry>();
    
    protected JList      termList;
    protected JList      newTermList;
    
    protected JTextArea  srcLbl;
    protected JLabel	 destLbl;
    protected JTextField textField;
    protected JButton    prevBtn;
    protected JButton    nxtBtn;
    protected JButton    transBtn;
    protected JLabel	 fileLbl;
    protected JPanel     mainPane;
    
    // SearchBox
    protected SearchBox                     searchBox;
    protected JAutoCompTextField            searchText;
    protected JButton                       searchBtn;
    
    protected JTable                        searchResultsTbl;
    protected SearchResultsModel            model;
    protected Vector<StrLocaleEntry>        results    = new Vector<StrLocaleEntry>();
    protected LocalizerSearchHelper         searchable = null;
    protected HashSet<String>               fndKeyHash = new HashSet<String>();
    
    
    protected JMenuItem  startTransMenuItem;
    protected JMenuItem  stopTransMenuItem;
    
    protected ResultSetController rsController;
    protected int        oldInx = -1;
    protected boolean    hasChanged = false;
    
    protected Vector<String> newKeyList = new Vector<String>();
    protected AtomicBoolean  contTrans  = new AtomicBoolean(true);
    
    
    /**
     * 
     */
    public StrLocalizerApp()
    {
        super();
        
        new MacOSAppHandler(this);
        
        loadLanguages();
        
    }
    
    /**
     * @return
     */
    public String[] getFileNames()
    {
        // These need to be moved to an XML file
        String[] fileNames = {"backuprestore_en", 
                                "common_en", 
                                "expresssearch_en", 
                                "global_views_en", 
                                "masterusrpwd_en", 
                                "preferences_en", 
                                "resources_en",  
                                "specify", 
                                "specify_plugins_en", 
                                "specifydbsetupwiz_en", 
                                "stats_en", 
                                "system_setup_en", 
                                "views_en",};
        return fileNames;
    }
    
    /**
     * @param dir
     * @param locale
     * @return
     */
    private boolean copyPropFiles(final File dir, final Locale locale, final boolean doAddOrigExt)
    {
        // These need to be moved to an XML file
        String[] fileNames = getFileNames();
        
        String ext = ".properties";
        for (String nm : fileNames)
        {
            try
            {
                String              name        = StringUtils.replace(nm, "_en", "_"+getFullLang(locale));
                String              outName     = dir.getAbsolutePath() + File.separator + name + ext + (doAddOrigExt ? ".orig" : "");
                
                //FileUtils.copyFile(new File(nm), new File(outName));
                
                PrintWriter         pw          = new PrintWriter(outName);
                System.out.println(name+"->"+outName);
                InputStream         inputStream = Specify.class.getResourceAsStream(nm + ext);
                try
                {
                    inputStream.available();
                } catch (Exception ex)
                {
                    inputStream = new FileInputStream(new File("src/"+nm + ext));
                }
                BufferedInputStream bis         = new BufferedInputStream(inputStream);
                byte[]              buf         = new byte[4096];
                int len = bis.read(buf);
                while (len > 0)
                {
                    pw.write(new String(buf, 0, len));
                    len = bis.read(buf);
                }
                pw.close();
                bis.close();
                inputStream.close();
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    /**
     * @param doFromStartup
     */
    private File doCopyLocale(final Locale locale, final boolean doAddOrigExt)
    {
        File tmpBaseDir = null;
        if (locale != null)
        {
            String path = rootDir.getAbsolutePath()+ File.separator + getFullLang(locale);
            tmpBaseDir = new File(path);
            if (tmpBaseDir.exists() || tmpBaseDir.mkdir())
            {
                if (!copyPropFiles(tmpBaseDir, locale, doAddOrigExt))
                {
                    UIRegistry.showError("There was an error creating the locale["+locale.getDisplayCountry()+"]");
                    System.exit(0);
                }
            }
        }
        return tmpBaseDir;
    }
    

    /**
     * @param doFromStartup
     */
    private void doCreateNewLocale(final boolean doFromStartup)
    {
        Locale locale = doChooseLangLocale(true);
        if (locale != null)
        {
            doCopyLocale(englishLocale, false);
            
            doCopyLocale(locale, true);
            baseDir = doCopyLocale(locale, false);
            
        } else if (doFromStartup)
        {
            UIRegistry.showError("StrLocalizer will exit now.");
            System.exit(0);
        }
    }
    
    /**
     * 
     */
    public void startUp()
    {
        rootDir = new File(UIRegistry.getUserHomeDir() + File.separator + "I18N");
        if (!rootDir.exists())
        {
            if (rootDir.mkdir())
            {
                doCreateNewLocale(true);
                
            } else
            {
                UIRegistry.showError("Error creating directory["+rootDir.getAbsolutePath()+"]");
                System.exit(0);
            }
        }
        
        String fullLanguage = doChooseExistingLocalization();
        
        if (fullLanguage != null)
        { 
            init(fullLanguage);
            
            register(MAINPANE, mainPane);
            frame.setGlassPane(glassPane = GhostGlassPane.getInstance());
            frame.setLocationRelativeTo(null);
            Toolkit.getDefaultToolkit().setDynamicLayout(true);
            register(GLASSPANE, glassPane);
            
            
        } else
        {
            UIRegistry.showError("StrLocalizer will exit.");
            System.exit(0);
        }
    }
    
    /**
     * 
     */
    protected void init(final String langStr)
    {
        setupSrcFiles(rootDir.getAbsolutePath()+ File.separator + "en");
        
        destLanguage = getLanguageByCode(langStr);
        if (destLanguage == null)
        {
            UIRegistry.showError("StrLocalizer will exit.");
            System.exit(0);
        }
        
        setupDestFiles(destLanguage.getCode(), null);
        
        srcFile = getResourcesFile();
        
        createUI();
        
        newSrcFile(getResourcesFile());
    }
    
    /**
     * @return the resources_xx.properties file if it exists, otherwise
     * return the first file in the source files list.
     */
    protected StrLocaleFile getResourcesFile()
    {
        String path = getPath() + File.separator + "resources_" + (destLanguage == null ? "en" : destLanguage.getCode()) + ".properties";
        StrLocaleFile file = getLocaleFileByPath(srcFiles, path);
        if (file == null)
        {
        	file = srcFiles.get(0);
        }
        return file;
    }
    
    /**
     * @return
     */
    protected String getPath()
    {
    	if (currentPath == null)
    	{
    		return getDefaultPath();
    	}
    	return currentPath + File.separator;
    }
    
    /**
     * @return
     */
    protected String getDefaultPath()
    {
        String prefName = "StrLoc.BASEDIR";

        if (baseDir == null)
        {
            String lang = AppPreferences.getLocalPrefs().get(prefName, null);
            if (StringUtils.isEmpty(lang))
            {
                lang = doChooseExistingLocalization();
                baseDir = new File(rootDir.getAbsolutePath() + File.separator + lang);
                try
                {
                    AppPreferences.getLocalPrefs().flush();

                } catch (BackingStoreException e)
                {
                    e.printStackTrace();
                }
                return baseDir.getAbsolutePath();
            } else
            {
                baseDir = new File(rootDir.getAbsolutePath() + File.separator + lang);
            }
        }
        return baseDir.getAbsolutePath();
    }
        
    /**
     * @return
     */
    private String doChooseExistingLocalization()
    {
        ArrayList<String> dirNames = new ArrayList<String>();
        for (String nm : rootDir.list())
        {
            if (!nm.startsWith(".") && !nm.equals("en"))
            {
                dirNames.add(nm);
            }
        }

        String fullLanguage = null;
        if (dirNames.size() > 1)
        {
            Collections.sort(dirNames);
            ToggleButtonChooserDlg<String> chooser = new ToggleButtonChooserDlg<String>(
                    (Frame) null, "CHOOSE_LOCALE", dirNames, ToggleButtonChooserPanel.Type.RadioButton);
            chooser.setUseScrollPane(true);
            chooser.setVisible(true);
            if (!chooser.isCancelled())
            {
                fullLanguage = chooser.getSelectedObject();
            }

        } else if (dirNames.size() == 1)
        {
            fullLanguage = dirNames.get(0);
        }
        return fullLanguage;
    }
    
    /**
     * @param dirName
     */
    protected void setupSrcFiles(final String dirName)
    {
        String postFix = "_" + (destLanguage == null ? "en" : destLanguage.getCode()) + ".properties";
        
    	srcFiles.clear();
    	destFiles.clear();
    	File dir = new File(dirName);
    	String[] exts = {"properties"};
    	Collection<?> files = FileUtils.listFiles(dir, exts, false);
    	for (Object fobj : files)
    	{
    		File f = (File )fobj;
    		if (f.getName().endsWith(postFix))
    		{
    			srcFiles.add(new StrLocaleFile(dirName + File.separator + f.getName(), null, false));
    		}
    	} 
    	
    	Collections.sort(srcFiles, new Comparator<StrLocaleFile>() {
			@Override
			public int compare(StrLocaleFile arg0, StrLocaleFile arg1)
			{
				return arg0.getPath().compareTo(arg1.getPath());
			}
    	});
    }
    
    /**
     * @param langCode the language code - "es", "en" ,etc
     *	
     * Assumes srcLocaleFiles has already been setup.
     */
    protected void setupDestFiles(final String langCode, final String destPath)
    {
    	destFiles.clear();
    	for (StrLocaleFile f : srcFiles)
    	{
    		String newPath;
    		if (destPath != null)
    		{
    			File duh = new File(f.getPath());
    			String newName = duh.getName().replace("_" + srcLangCode + ".", "_" + langCode + ".");
    			newPath = destPath + File.separator + newName;
    		}
    		else
    		{
    		    String path = f.getPath();
                newPath = path.replace("_" + srcLangCode + ".", "_" + langCode + ".");
                newPath = newPath.replace(File.separator + srcLangCode + File.separator, File.separator + langCode + File.separator);
    		}
    		destFiles.add(new StrLocaleFile(newPath, f.getPath(), true));
    	}
    }
    
    /**
     * build a list of languages 
     */
    protected void loadLanguages()
    {
    	languages.clear();
    	
    	boolean doXML = false;
    	if (doXML)
    	{
        	Element root = XMLHelper.readDOMFromConfigDir("languagecodes.xml");
        	for (Object langObj : root.selectNodes("languagecode"))
        	{
        		Element lang = (Element )langObj;
        		languages.add(new LanguageEntry(lang.attributeValue("englishname"), lang.attributeValue("code")));
        	}
    	} else
    	{
            languages.add(new LanguageEntry("Albania",   "sq"));
            languages.add(new LanguageEntry("English",   "en"));
            languages.add(new LanguageEntry("Swedish",   "se"));
            languages.add(new LanguageEntry("Portugese", "pt"));
            languages.add(new LanguageEntry("Spanish",   "es"));
    	}
    	Collections.sort(languages);
    }
    
    /**
     * 
     */
    private void createUI()
    {
        IconManager.setApplicationClass(Specify.class);
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$
        
        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.tools.StrLocPickListFactory");   // Needed By the Auto Cosmplete UI //$NON-NLS-1$ //$NON-NLS-2$

        CellConstraints cc = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,10px,f:p:g", "p,4px,f:p:g"), this);
        
        pb.addSeparator("Localize", cc.xyw(1,1,3));
        
        termList    = new JList(new ItemModel(srcFile));
        newTermList = new JList(newKeyList);
        
        srcLbl = UIHelper.createTextArea(3, 40);
        srcLbl.setBorder(new LineBorder(srcLbl.getForeground()));
        textField  = UIHelper.createTextField(40);
        
        textField.getDocument().addDocumentListener(new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e)
            {
                hasChanged = true;
            }
        });
        
        srcLbl.setEditable(false);
        
        rsController = new ResultSetController(null, false, false, false, "", 1, true);
        
        transBtn = UIHelper.createButton(getResourceString("StrLocalizerApp.Translate"));
        
        
        PanelBuilder pbr = new PanelBuilder(new FormLayout("r:p,2px,f:p:g", "p, 4px, c:p,4px,p,4px,p,4px,p,4px,p,10px,p,2px,f:p:g"));

        pbr.add(UIHelper.createLabel(getResourceString("StrLocalizerApp.FileLbl")), cc.xy(1, 1));
        fileLbl = UIHelper.createLabel("   ");
        pbr.add(fileLbl, cc.xy(3, 1));

        pbr.add(UIHelper.createLabel("English:"), cc.xy(1, 3));
        pbr.add(srcLbl, cc.xy(3, 3));
        
        
        destLbl = UIHelper.createFormLabel(destLanguage.getEnglishName());
        pbr.add(destLbl, cc.xy(1, 5));
        pbr.add(textField,                        cc.xy(3, 5));
        
        pbr.add(rsController.getPanel(),          cc.xyw(1, 7, 3));
        pbr.add(transBtn,                         cc.xy(1,  9));
        pbr.addSeparator("Searching",             cc.xyw(1, 11, 3));
        
        searchBtn = createButton(getResourceString("SEARCH"));
        searchBtn.setToolTipText(getResourceString("ExpressSearchTT"));
        searchText = new JAutoCompTextField(15, PickListDBAdapterFactory.getInstance().create("ExpressSearch", true));
        searchText.setAskBeforeSave(false);
        searchBox = new SearchBox(searchText, null, true);
        
        searchText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    fndKeyHash.clear();
                    results.clear();
                    String txt = searchText.getText();
                    if (!txt.isEmpty())
                    {
                        doSearch(txt, "key");
                        doSearch(txt, "src");
                        doSearch(txt, "dst");
                    }
                    model.fireChanges();
                }
            }
        });
        pbr.add(searchBox, cc.xyw(1, 13, 3));
        
        model = new SearchResultsModel();
        searchResultsTbl = new JTable(model);
        pbr.add(UIHelper.createScrollPane(searchResultsTbl), cc.xyw(1, 15, 3));
        
        searchResultsTbl.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                //tableRowSelected();
                StrLocaleEntry entry = (StrLocaleEntry)results.get(searchResultsTbl.getSelectedRow());
                if (entry != null)
                {
                    int listIndex = srcFile.getInxForKey(entry.getKey());
                    termList.setSelectedIndex(listIndex);
                    termList.ensureIndexIsVisible(listIndex);
                }
            }
        });
        
        PanelBuilder pbl = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,10px,p,4px,f:p:g"));

        JScrollPane sp  = UIHelper.createScrollPane(termList);
        JScrollPane nsp = UIHelper.createScrollPane(newTermList);
        pbl.add(sp,                   cc.xy(1,1));
        pbl.addSeparator("New Items", cc.xy(1, 3));
        pbl.add(nsp,                  cc.xy(1,5));
        
        
        pb.add(pbl.getPanel(),  cc.xy(1,3));
        pb.add(pbr.getPanel(),  cc.xy(3,3));
        
        
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
                String txt = srcLbl.getText();
                
                String newText = translate(txt);
                if (StringUtils.isNotEmpty(newText))
                {
                    newText = newText.replace("&#039;", "'");
                    textField.setText(newText);
                    StrLocaleEntry entry = srcFile.getKey(termList.getSelectedIndex());
                    entry.setDstStr(textField.getText());
                }
            }
        });
        
        frame.pack();
    }
    
    /**
     * @param searchText
     * @param fileName
     */
    private void doSearch(final String searchText, final String fileName)
    {
        int[] indexes = searchable.doPropsSearch(searchText, fileName);
        if (indexes != null)
        {
            for (int i=0;i<indexes.length;i++)
            {
                StrLocaleEntry entry = srcFile.getItems().get(indexes[i]);
                if (!fndKeyHash.contains(entry.getKey()))
                {
                    results.add(entry);
                    fndKeyHash.add(entry.getKey());
                }
            }
        }
    }
    
    /**
     * 
     */
    private void listSelected()
    {
        if (oldInx > -1 && hasChanged)
        {
            StrLocaleEntry entry = srcFile.getKey(oldInx);
            entry.setDstStr(textField.getText());
        }
        
        int inx = termList.getSelectedIndex();
        if (inx > -1)
        {

            StrLocaleEntry srcEntry = srcFile.getKey(inx);
            System.out.println(srcEntry.hashCode());
            
            srcLbl.setText(srcEntry.getSrcStr());
            String str = srcEntry.getDstStr();
            textField.setText(str != null ? str : srcEntry.getSrcStr());
            
            rsController.setIndex(inx);
        } else
        {
            srcLbl.setText("");
            textField.setText("");
        }
        oldInx     = inx;
        hasChanged = false;
    }
    
    
    /**
     * 
     */
    private void newListSelected()
    {
        String key = (String)newTermList.getSelectedValue();
        if (key != null)
        {
            final Integer inx = srcFile.getInxForKey(key);
            //System.out.println("newListSelected: index = " + inx);
            if (inx != null)
            {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        termList.setSelectedIndex(inx);
                        termList.ensureIndexIsVisible(inx);                        
                    }
                });
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
                    System.out.println("["+dst.getItemHash().get(srcEntry.getKey()).getSrcStr()+"]["+srcEntry.getSrcStr()+"]");
                    
                    // This checks to see if the text from ".orig" file matches the src file
                    // if it does it doesn't need to be changed
                    if (dst.isSrcSameAsDest(srcEntry.getKey(), srcEntry.getSrcStr()))
                    {
                        if (StringUtils.isEmpty(dstEntry.getSrcStr()) || dstEntry.getSrcStr().equals(srcEntry.getSrcStr()))
                        {
                            newKeyList.add(srcEntry.getKey());
                        }
                    } else
                    {
                        newKeyList.add(srcEntry.getKey());    
                    }
                    
                    srcEntry.setDstStr(dstEntry.getSrcStr());
                    srcEntry.setStatus(dstEntry.getStatus());
                    
                } else
                {
                    srcEntry.setDstStr(srcEntry.getSrcStr());
                    dstEntry = new StrLocaleEntry(srcEntry.getKey(), srcEntry.getSrcStr(), null, STATUS.IsNew);
                    dstHash.put(srcEntry.getKey(), dstEntry);
                    
                    newKeyList.add(srcEntry.getKey());
                }
                cnt++;
            }
        }
        srcFile.clearEditFlags();
        //System.out.println(cnt);
    }
    
    /**
     * @param src
     * @param dst
     */
    private void mergeToDst(final StrLocaleFile src, final StrLocaleFile dst)
    {
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
                    //dstEntry.setSrcStr(srcEntry.getDstStr());
                    dstItems.add(dstEntry);
                } else
                {
                    dstItems.add(srcEntry);
                }
            }
        }        
    }
    
    private Language getLangFromCode(final String code)
    {
        if (code.equals("se")) return Language.SWEDISH;
        if (code.equals("es")) return Language.SPANISH;
        if (code.equals("pt")) return Language.PORTUGUESE;
        
        return Language.ENGLISH;
    }
    
    
    /**
     * @param inputText
     */
    protected String translate(final String inputText)
    {
        if (inputText.isEmpty()) return "";
        //System.out.println("\n"+inputText);
        
        Translate.setHttpReferrer("http://www.specifysoftware.org");

        try
        {
            String text = inputText;
            
            boolean hasSpecialChars = false;
            while (StringUtils.contains(text, "%d") || 
                    StringUtils.contains(text, "%s") || 
                    StringUtils.contains(text, "\\n"))
            {
                text = StringUtils.replace(text, "%d", "99");
                text = StringUtils.replace(text, "%s", "88");
                text = StringUtils.replace(text, "\\n", " 77 ");
                hasSpecialChars = true;
            }
            
            Language lang = getLangFromCode(destLanguage.getCode());
            //System.out.println(text);
            String newText = Translate.execute(text, Language.ENGLISH, lang);
            
            if (hasSpecialChars)
            {
                while (StringUtils.contains(newText, "77") ||
                       StringUtils.contains(newText, "88") ||
                       StringUtils.contains(newText, "99"))
                {
                    newText = StringUtils.replace(newText, "99", "%d");
                    newText = StringUtils.replace(newText, "88", "%s");
                    newText = StringUtils.replace(newText, " 77 ", " \\n ");
                    newText = StringUtils.replace(newText, "77 ", "\\n ");
                    newText = StringUtils.replace(newText, " 77", " \\n");
                }
            }
            //System.out.println(newText);
            return newText;
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 
     */
    private void translateNewItems()
    {
        
        //writeGlassPaneMsg(getResourceString("StrLocalizerApp.TranslatingNew"), 24);
        
        startTransMenuItem.setEnabled(false);
        stopTransMenuItem.setEnabled(true);
        
        final double total = newKeyList.size();
        
        SwingWorker<Integer, Integer> translator = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                int count = 0;
                for (String key : newKeyList)
                {
                    StrLocaleEntry entry = srcFile.getItemHash().get(key);
                    //if (StringUtils.contains(entry.getSrcStr(), "%") || StringUtils.contains(entry.getSrcStr(), "\n"))
                    {
                        String transText = translate(entry.getSrcStr());
                        if (transText != null)
                        {
                            entry.setDstStr(transText);
                            //writeGlassPaneMsg(String.format("%d / %d", count, newKeyList.size()), 18);
                            System.out.println(String.format("%s - %d / %d", key, count, newKeyList.size()));
                        }
                    }
                    
                    try
                    {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {}
                    
                    //glassPane.setProgress((int)( (100.0 * count) / total));
                    count++;
                    
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
                //glassPane.setProgress(100);
                //clearGlassPaneMsg();
                
                UIRegistry.showLocalizedMsg("Done Localizing");
                
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
        if (doAppExit)
        {
        	System.exit(0);
        	return true;
        }
        return false;
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
//    private void doOpen()
//    {
//        FileDialog dlg = new FileDialog((JFrame)getTopWindow(), "Open a properties I18N File", FileDialog.LOAD);
//        dlg.setFilenameFilter(new FilenameFilter(){
//            public boolean accept(File dir, String name){
//              return (name.endsWith(".properties"));
//            }
//         });
//        dlg.setVisible(true);
//        String fileName = dlg.getFile();
//        if (fileName != null)
//        {
//            ChooseFromListDlg<LanguageEntry> ldlg = new ChooseFromListDlg<LanguageEntry>((Frame )getTopWindow(), 
//            		getResourceString("StrLocalizerApp.ChooseLanguageDlgTitle"), languages);
//            UIHelper.centerAndShow(ldlg);
//            if (ldlg.isCancelled() || ldlg.getSelectedObject() == null)
//            {
//            	return;
//            }
//            
//            termList.clearSelection();
//            newTermList.clearSelection();
//            
//
//            
//            String path = dlg.getDirectory() + File.separator + fileName;
//            String srcPath = path.replace("_en.", "_" + ldlg.getSelectedObject().getCode() + ".");
//            
//            srcFile = new StrLocaleFile(path, null, false);
//            destFile = new StrLocaleFile(srcPath, path, true);
//            destLanguage = ldlg.getSelectedObject();
//            final String newLang = destLanguage.getEnglishName();
//            SwingUtilities.invokeLater(new Runnable() {
//
//				/* (non-Javadoc)
//				 * @see java.lang.Runnable#run()
//				 */
//				@Override
//				public void run()
//				{
//					destLbl.setText(newLang);
//				}
//            	
//            });
//            newKeyList.clear();
//            
//            mergeToSrc(srcFile, destFile);
//            
//            termList.setModel(new ItemModel(srcFile));
//            DefaultListModel model = new DefaultListModel();
//            model.clear();
//            for (String str : newKeyList)
//            {
//                model.addElement(str);
//            }
//            newTermList.setModel(model);
//        }
//    }
    
    /**
     * 
     */
    private void doSave()
    {
        termList.clearSelection();
        
        mergeToDst(srcFile, destFile);
        
        destFile.save();
        destFile.clearEditFlags();
        srcFile.clearEditFlags();
    }
    
    /**
     * @param frame
     */
    public void addMenuBar(final JFrame frame)
    {
        this.frame = frame;
        
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu(getResourceString("FILE"));
        
        JMenuItem chooseFileItem = new JMenuItem(getResourceString("StrLocalizerApp.ChooseFileMenu"));
        fileMenu.add(chooseFileItem);
        
        chooseFileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doChooseFile();
            }
        });        

        
        JMenuItem saveItem = new JMenuItem(getResourceString("SAVE"));
        fileMenu.add(saveItem);
        
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doSave();
            }
        });
        
        fileMenu.addSeparator();
        
        JMenuItem chooseDirMenu = new JMenuItem(getResourceString("StrLocalizerApp.CreateNewLocaleMenu"));
        fileMenu.add(chooseDirMenu);
        
        chooseDirMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doCreateNewLocale(false);
            }
        });        


        JMenuItem newLocaleItem = new JMenuItem(getResourceString("StrLocalizerApp.ChooseLocaleMenu"));
        fileMenu.add(newLocaleItem);
        
        newLocaleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String fullLanguage = doChooseExistingLocalization();
                if (fullLanguage != null)
                { 
                    init(fullLanguage);
                }
            }
        });        

        /*
        JMenuItem mneuItem = new JMenuItem(getResourceString("Check For old Localizations"));
        fileMenu.add(mneuItem);
        
        mneuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {new
                if (baseDir == null)
                {
                    getDefaultPath();
                }
                
                if (baseDir != null)
                {
                    File englishDir = new File(rootDir.getAbsolutePath() + File.separator + "en");
                    LocalizerSearchHelper helper = new LocalizerSearchHelper(englishDir, "file-index");
                    helper.findOldL10NKeys(getFileNames());
                }
            }
        });*/        

        
//        JMenuItem openItem = new JMenuItem("Open");
//        fileMenu.add(openItem);
//        
//        openItem.addActionListener(new ActionListener() {
//
//            /* (non-Javadoc)
//             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//             */
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                doOpen();
//            }
//        });       

        
        if (!UIHelper.isMacOS())
        {
            fileMenu.addSeparator();
            JMenuItem exitMenu = new JMenuItem(getResourceString("EXIT"));
            exitMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    doExit(checkForChanges());
                }
            });

            fileMenu.add(exitMenu);
        }
        menuBar.add(fileMenu);
        
        
        JMenu transMenu = new JMenu(getResourceString("StrLocalizerApp.Translate"));
        menuBar.add(transMenu);
        
        startTransMenuItem = new JMenuItem(getResourceString("StrLocalizerApp.Start"));
        transMenu.add(startTransMenuItem);
        
        startTransMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                 translateNewItems();
            }
        });
        
        stopTransMenuItem = new JMenuItem(getResourceString("Stop"));
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
        
        setTopWindow(frame);
        
        register(FRAME, frame);
    }

    
    /**
     * Open existing language 'project'
     */
    /*protected void doChooseLocale()
    {        
    	JFileChooser fdlg = new JFileChooser();
    	fdlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	int fdlgResult = fdlg.showOpenDialog(null);
    	if (fdlgResult != JFileChooser.APPROVE_OPTION)
    	{
    		return;
    	}
    
    	File destDir = fdlg.getSelectedFile();

    	//figure out the language
    	String[] propFiles = destDir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".properties");
			}
    		
    	}); 
    	
    	String langCode = null;
    	LanguageEntry lang = null;
    	for (String fileName : propFiles)
    	{
    		int extPos = fileName.indexOf(".properties");
    		if (extPos != -1)
    		{
    			if (langCode == null)
    			{
    				langCode = fileName.substring(extPos - 2, extPos);
    				lang = getLanguageByCode(langCode);
    				if (lang == null)
    				{
    					showLocalizedError("StrLocalizerApp.InvalidLangCode", langCode);
    					return;
    				}
    			}
    			else if (!langCode.equals(fileName.substring(extPos - 2, extPos)))
    			{
					showLocalizedError("StrLocalizerApp.InvalidLocaleDir", langCode);
					return;
    			}
    			
    		}
    	}
    	if (lang == null)
		{
			showLocalizedError("StrLocalizerApp.InvalidLocaleDir", langCode);
			return;
		}
        destLanguage = lang;
        final String newLang = destLanguage.getEnglishName();
        SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				destLbl.setText(newLang);
			}
        	
        });
        
        currentPath = destDir.getPath();
        setupSrcFiles(getPath());
        setupDestFiles(destLanguage.getCode(), destDir.getPath());
        
        newSrcFile(getResourcesFile());       
    		
    }*/
    
    /**
     * New language 'project'
     */
    protected void doNewLocale()
    {
        if (!checkForChanges())
        {
        	return;
        }
    	
    	ChooseFromListDlg<LanguageEntry> ldlg = new ChooseFromListDlg<LanguageEntry>((Frame )getTopWindow(), getResourceString("StrLocalizerApp.ChooseLanguageDlgTitle"), languages);
        UIHelper.centerAndShow(ldlg);
        if (ldlg.isCancelled() || ldlg.getSelectedObject() == null)
        {
        	return;
        }

        JFileChooser fdlg = new JFileChooser();
        fdlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int fdlgResult = fdlg.showOpenDialog(null);
        if (fdlgResult != JFileChooser.APPROVE_OPTION)
        {
        	return;
        }
        
        File destDir = fdlg.getSelectedFile();
        
        destLanguage = ldlg.getSelectedObject();
        final String newLang = destLanguage.getEnglishName();
        SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				destLbl.setText(newLang + ":");
			}
        });
        
        setupSrcFiles(getDefaultPath());
        setupDestFiles(ldlg.getSelectedObject().getCode(), destDir.getPath());
        
    	for (StrLocaleFile file : destFiles)
    	{
    		file.save();
    	}
        currentPath = destDir.getPath();
        newSrcFile(getResourcesFile());       
    }
    
    /**
     * @param files
     * @param srcPath
     * @return
     */
    protected int getLocaleFileInxBySrcPath(Vector<StrLocaleFile> files, String srcPath)
    {
    	int result = 0;
    	for (StrLocaleFile file : files)
    	{
    		if (file.getSrcPath().equals(srcPath))
    		{
    			return result;
    		}
    		result++;
    	}
    	return -1;
    }
    
    /**
     * @param files
     * @param path
     * @return
     */
    protected StrLocaleFile getLocaleFileByPath(List<StrLocaleFile> files, String path)
    {
    	for (StrLocaleFile file : files)
    	{
    		if (file.getPath().equals(path))
    		{
    			return file;
    		}
    	}
    	return null;
    }
    
    /**
     * @return true if no unsaved changes are present
     * else return results of prompt to save
     */
    protected boolean checkForChanges()
    {
        int s = termList.getSelectedIndex();
        if (s != -1)
        {
        	StrLocaleEntry entry = srcFile.getKey(s);
        	entry.setDstStr(textField.getText());
        }
        if (srcFile.isEdited())
        {
        	int response = JOptionPane.showOptionDialog((Frame )getTopWindow(), 
        			String.format(getResourceString("StrLocalizerApp.SaveChangesMsg"), destFile.getPath()), 
        			getResourceString("StrLocalizerApp.SaveChangesTitle"), 
        			JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.YES_OPTION);
        	if (response == JOptionPane.CANCEL_OPTION)
        	{
        		return false;
        	}
        	if (response == JOptionPane.YES_OPTION)
        	{
        		doSave(); 
        		return true; //what if it fails? 
        	}
        }
        return true;
    }
    
    /**
     * Open another properties file for translation
     */
    protected void doChooseFile()
    {
    	if (!checkForChanges())
    	{
    		return;
    	}
    	
    	ChooseFromListDlg<StrLocaleFile> ldlg = new ChooseFromListDlg<StrLocaleFile>((Frame )getTopWindow(), 
        		                                   getResourceString("StrLocalizerApp.ChooseFileDlgTitle"), srcFiles);
        UIHelper.centerAndShow(ldlg);
        if (ldlg.isCancelled() || ldlg.getSelectedObject() == null)
        {
        	return;
        }
        
        newSrcFile(ldlg.getSelectedObject());
    }
    
    /**
     * @param newSrc
     */
    protected void newSrcFile(final StrLocaleFile newSrc)
    {
        termList.clearSelection();
        newTermList.clearSelection();
        
        srcFile = newSrc;
        
        // re-create and reload the dest file to clear unsaved changes and/or states
        int           destInx = getLocaleFileInxBySrcPath(destFiles, srcFile.getPath());
        StrLocaleFile oldDest = destFiles.get(destInx);
        destFile = new StrLocaleFile(oldDest.getPath(), oldDest.getSrcPath(), true);
        destFiles.set(destInx, destFile);
        
        newKeyList.clear();
        
        mergeToSrc(srcFile, destFile);
        
        ItemModel termsModel = new ItemModel(srcFile);
        termList.setModel(termsModel);
        DefaultListModel itemModel = new DefaultListModel();
        itemModel.clear();
        for (String str : newKeyList)
        {
            itemModel.addElement(str);
        }
        newTermList.setModel(itemModel);
        rsController.setLength(srcFile.getNumberOfKeys());
        
        SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				fileLbl.setText(destFile.getPath());				
			}
        });
        
        searchable = new LocalizerSearchHelper(baseDir, "props-index");
        searchable.indexProps(srcFile.getItems());
    }
    
    /**
     * @param locale
     * @return
     */
    private String getFullLang(final Locale locale)
    {
        return locale.getLanguage() + (StringUtils.isNotEmpty(locale.getVariant()) ? ("_"+locale.getVariant()): "");
    }

    /**
     * @param hideExisting
     * @return
     */
    private Locale doChooseLangLocale(final boolean hideExisting)
    {

        HashSet<String> existingLocs = new HashSet<String>();
        if (hideExisting)
        {
            for (String nm : rootDir.list())
            {
                if (!nm.startsWith("."))
                {
                    existingLocs.add(nm);
                }
            }
        }
        
        Vector<Locale> locales = new Vector<Locale>();
        for (Locale l : Locale.getAvailableLocales())
        {
            if (!hideExisting || !existingLocs.contains(getFullLang(l)))
            {
                locales.add(l);
            }
        }
        Collections.sort(locales, new Comparator<Locale>()
        {
            public int compare(Locale o1, Locale o2)
            {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });

        Vector<String> localeNames = new Vector<String>();
        for (Locale l : locales)
        {
            localeNames.add(l.getDisplayName());
        }

        ToggleButtonChooserDlg<String> chooser = new ToggleButtonChooserDlg<String>((Frame) null,
                "CHOOSE_LOCALE", localeNames, ToggleButtonChooserPanel.Type.RadioButton);
        chooser.setUseScrollPane(true);
        chooser.setVisible(true);
        if (!chooser.isCancelled()) 
        { 
            return locales.get(chooser.getSelectedIndex()); 
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
            postParams.add(new NameValuePair("doit",    "done")); //$NON-NLS-1$
            postParams.add(new NameValuePair("ei",      "UTF-8")); //$NON-NLS-1$
            postParams.add(new NameValuePair("lp",      src)); //$NON-NLS-1$
            postParams.add(new NameValuePair("fr",      "bf-home")); //$NON-NLS-1$
            
            postParams.add(new NameValuePair("intl",     "1")); //$NON-NLS-1$
            postParams.add(new NameValuePair("tt",       "urltext")); //$NON-NLS-1$
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
            ex.printStackTrace();
        }
        return null;
    }


    

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowActivated(WindowEvent arg0)
	{
		// ignore
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosed(WindowEvent arg0)
	{
		// ignore
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent arg0)
	{
		doExit(checkForChanges());
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeactivated(WindowEvent arg0)
	{
		// ignore
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeiconified(WindowEvent arg0)
	{
		// ignore
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowIconified(WindowEvent arg0)
	{
		// ignore
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowOpened(WindowEvent arg0)
	{
		// ignore
		
	}

	/**
     * @param args
     */
    public static void main(String[] args)
    {
        setAppName("Specify");  //$NON-NLS-1$
        System.setProperty(AppPreferences.factoryName, "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences //$NON-NLS-1$
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());
        
        boolean doIt = false;
        if (doIt)
        {
            Charset utf8charset = Charset.forName("UTF-8");
            Charset iso88591charset = Charset.forName("ISO-8859-1");

            ByteBuffer inputBuffer = ByteBuffer.wrap(new byte[]{(byte)0xC3, (byte)0xA2});

            // decode UTF-8
            CharBuffer data = utf8charset.decode(inputBuffer);

            // encode ISO-8559-1
            ByteBuffer outputBuffer = iso88591charset.encode(data);
            byte[] outputData = outputBuffer.array();
            System.out.println(new String(outputData));
            return;
        }
        
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
                
                JFrame frame = new JFrame(getResourceString("StrLocalizerApp.AppTitle"));
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                final StrLocalizerApp sl = new StrLocalizerApp();
                sl.addMenuBar(frame);
                frame.setContentPane(sl);
                frame.setSize(768,1024);
                
                //Dimension size = frame.getPreferredSize();
                //size.height = 500;
                //frame.setSize(size);
                frame.addWindowListener(sl);
                IconManager.setApplicationClass(Specify.class);
            	frame.setIconImage(IconManager.getImage(IconManager.makeIconName("SpecifyWhite32")).getImage());
                UIHelper.centerAndShow(frame);
                
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        sl.startUp();
                    }
                });
            }
        });
    }

    /**
     * @param code
     * @return
     */
    protected LanguageEntry getLanguageByCode(final String code)
    {
    	for (LanguageEntry l : languages)
    	{
    	    System.out.println(l.getCode()+"="+code);
    		if (l.getCode().equals(code))
    		{
    			return l;
    		}
    	}
    	return null;
    }
    
    //-------------------------------------------------------------
    class SearchResultsModel extends AbstractTableModel
    {
        protected String[] headers = {"Key", "Text"};
        
        public void fireChanges()
        {
            fireTableDataChanged();
        }
        
        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return headers != null ? headers[column] : "";
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;
        }

        @Override
        public int getColumnCount()
        {
            return headers != null ? headers.length : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return results.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            StrLocaleEntry entry = results.get(rowIndex);
            return columnIndex == 0 ? entry.getKey() : entry.getSrcStr();
        }
        
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
        	return file.getKey(index).getKey();
        }

        /* (non-Javadoc)
         * @see javax.swing.ListModel#getSize()
         */
        @Override
        public int getSize()
        {
            return file.getNumberOfKeys();
        }
    }
    
    private class LanguageEntry implements Comparable<LanguageEntry>
    {
    	private String englishName;
    	private String code;
    	
    	public LanguageEntry(String englishName, String code)
    	{
    		this.englishName = englishName;
    		this.code = code;
    	}

		/**
		 * @return the englishName
		 */
		public String getEnglishName()
		{
			return englishName;
		}

		/**
		 * @return the code
		 */
		public String getCode()
		{
			return code;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return englishName + " (" + code + ")";
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(LanguageEntry arg0)
		{
			return englishName.compareTo(arg0.englishName);
		}
    	
    }
}
