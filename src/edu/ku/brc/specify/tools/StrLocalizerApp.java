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
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.af.core.FrameworkAppIFace;
import edu.ku.brc.af.core.MacOSAppHandler;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.tools.StrLocaleEntry.STATUS;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.IconManager;
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
    private static final Logger  log                = Logger.getLogger(StrLocalizerApp.class);
    
    protected GhostGlassPane glassPane;

    protected String srcLangCode = "en";

    protected String currentPath = null;
    
    protected LanguageEntry destLanguage = null;
    
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
    
    protected JMenuItem  startTransMenuItem;
    protected JMenuItem  stopTransMenuItem;
    
    protected ResultSetController rsController;
    protected int        oldInx = -1;
    
    protected Vector<String> newKeyList = new Vector<String>();
    protected AtomicBoolean  contTrans  = new AtomicBoolean(true);
    
    
    /**
     * 
     */
    public StrLocalizerApp()
    {
        super();
        
        new MacOSAppHandler(this);
        
        // "src/resources_en.properties"
        
        loadLanguages();
        setupSrcFiles(getPath());
        destLanguage = getLanguageByCode("es");
        if (destLanguage == null)
        {
        	destLanguage = new LanguageEntry("Spanish", "es");
        }
        setupDestFiles(destLanguage.getCode(), null);
        
        createUI();
        
        newSrcFile(getResourcesFile());
    }
    
    /**
     * @return the resources_xx.properties file if it exists, otherwise
     * return the first file in the source files list.
     */
    protected StrLocaleFile getResourcesFile()
    {
        StrLocaleFile file = getLocaleFileByPath(srcFiles, getPath() + File.separator + "resources_" + 
        		 (destLanguage == null ? "en" : destLanguage.getCode()) + ".properties");
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
    
    protected String getDefaultPath()
    {
    	return "src" + File.separator;
    }
    /**
     * @param dirName
     */
    protected void setupSrcFiles(final String dirName)
    {
    	srcFiles.clear();
    	destFiles.clear();
    	File dir = new File(dirName);
    	String[] exts = {"properties"};
    	Collection<?> files = FileUtils.listFiles(dir, exts, false);
    	for (Object fobj : files)
    	{
    		File f = (File )fobj;
    		if (f.getName().endsWith("_" + (destLanguage == null ? "en" : destLanguage.getCode()) + ".properties"))
    		{
    			srcFiles.add(new StrLocaleFile(dirName + f.getName(), null, false));
    		}
    	}
    	Collections.sort(srcFiles, new Comparator<StrLocaleFile>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
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
    			newPath = f.getPath().replace("_" + srcLangCode + ".", "_" + langCode + ".");
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
    	Element root = XMLHelper.readDOMFromConfigDir("languagecodes.xml");
    	for (Object langObj : root.selectNodes("languagecode"))
    	{
    		Element lang = (Element )langObj;
    		languages.add(new LanguageEntry(lang.attributeValue("englishname"), lang.attributeValue("code")));
    	}
    	Collections.sort(languages);
    }
    
    /**
     * 
     */
    private void createUI()
    {
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,10px,f:p:g", "f:p:g"), this);
        
        termList   = new JList(new ItemModel(srcFile));
        newTermList = new JList(newKeyList);
        
        srcLbl = UIHelper.createTextArea(3, 40);
        srcLbl.setBorder(new LineBorder(srcLbl.getForeground()));
        textField  = UIHelper.createTextField(40);
        
        srcLbl.setEditable(false);
        
        rsController = new ResultSetController(null, false, false, false, "", 1, true);
        
        transBtn = UIHelper.createButton(UIRegistry.getResourceString("StrLocalizerApp.Translate"));
        
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder pbr = new PanelBuilder(new FormLayout("r:p,2px,f:p:g", "p, 4px, c:p,4px,p,4px,p,4px,p,4px,p"));

        pbr.add(UIHelper.createLabel(UIRegistry.getResourceString("StrLocalizerApp.FileLbl")), cc.xy(1, 1));
        fileLbl = UIHelper.createLabel("   ");
        pbr.add(fileLbl, cc.xy(3, 1));

        pbr.add(UIHelper.createLabel("English:"), cc.xy(1, 3));
        pbr.add(srcLbl, cc.xy(3, 3));
        
        
        destLbl = UIHelper.createLabel(destLanguage.getEnglishName() + ":");
        pbr.add(destLbl, cc.xy(1, 5));
        pbr.add(textField,                        cc.xy(3, 5));
        
        pbr.add(rsController.getPanel(),          cc.xyw(1, 7, 3));
        pbr.add(transBtn,                         cc.xy(1, 9));
        
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
        
        
        //mainPane = pb.getPanel();
        
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
                String txt = srcLbl.getText().replace("\\n", " ");
                
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
    }
    
    
    /**
     * 
     */
    private void listSelected()
    {
        if (oldInx > -1)
        {
            StrLocaleEntry entry = srcFile.getKey(oldInx);
            entry.setDstStr(textField.getText());
        }
        
        int inx = termList.getSelectedIndex();
        if (inx > -1)
        {

            StrLocaleEntry srcEntry = srcFile.getKey(inx);
            srcLbl.setText(srcEntry.getSrcStr());
            String str = srcEntry.getDstStr();
            textField.setText(str != null ? str : srcEntry.getSrcStr());
            
            rsController.setIndex(inx);
        } else
        {
            srcLbl.setText("");
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
            Integer inx = srcFile.getInxForKey(key);
            //System.out.println("newListSelected: index = " + inx);
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
    
    private void translateNewItems()
    {
        UIRegistry.writeGlassPaneMsg(UIRegistry.getResourceString("StrLocalizerApp.TranslatingNew"), 24);
        
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
                    StrLocaleEntry entry = srcFile.getItemHash().get(key);
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
//        FileDialog dlg = new FileDialog((JFrame)UIRegistry.getTopWindow(), "Open a properties I18N File", FileDialog.LOAD);
//        dlg.setFilenameFilter(new FilenameFilter(){
//            public boolean accept(File dir, String name){
//              return (name.endsWith(".properties"));
//            }
//         });
//        dlg.setVisible(true);
//        String fileName = dlg.getFile();
//        if (fileName != null)
//        {
//            ChooseFromListDlg<LanguageEntry> ldlg = new ChooseFromListDlg<LanguageEntry>((Frame )UIRegistry.getTopWindow(), 
//            		UIRegistry.getResourceString("StrLocalizerApp.ChooseLanguageDlgTitle"), languages);
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
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu(UIRegistry.getResourceString("FILE"));
        
        JMenuItem newLocaleItem = new JMenuItem(UIRegistry.getResourceString("StrLocalizerApp.NewLocaleMenu"));
        fileMenu.add(newLocaleItem);
        
        newLocaleItem.addActionListener(new ActionListener() {

            /* (non-Javadoc)
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doNewLocale();
            }
        });        

        JMenuItem chooseLocaleItem = new JMenuItem(UIRegistry.getResourceString("StrLocalizerApp.ChooseLocaleMenu"));
        fileMenu.add(chooseLocaleItem);
        
        chooseLocaleItem.addActionListener(new ActionListener() {

            /* (non-Javadoc)
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doChooseLocale();
            }
        });        

        JMenuItem chooseFileItem = new JMenuItem(UIRegistry.getResourceString("StrLocalizerApp.ChooseFileMenu"));
        fileMenu.add(chooseFileItem);
        
        chooseFileItem.addActionListener(new ActionListener() {

            /* (non-Javadoc)
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doChooseFile();
            }
        });        

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
        
        JMenuItem saveItem = new JMenuItem(UIRegistry.getResourceString("SAVE"));
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
            JMenuItem exitMenu = new JMenuItem(UIRegistry.getResourceString("EXIT"));
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
        
        
        JMenu transMenu = new JMenu(UIRegistry.getResourceString("StrLocalizerApp.Translate"));
        menuBar.add(transMenu);
        
        startTransMenuItem = new JMenuItem(UIRegistry.getResourceString("StrLocalizerApp.Start"));
        transMenu.add(startTransMenuItem);
        
        startTransMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                 translateNewItems();
            }
        });
        
        stopTransMenuItem = new JMenuItem(UIRegistry.getResourceString("Stop"));
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
     * Open existing language 'project'
     */
    protected void doChooseLocale()
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
    					UIRegistry.showLocalizedError("StrLocalizerApp.InvalidLangCode", langCode);
    					return;
    				}
    			}
    			else if (!langCode.equals(fileName.substring(extPos - 2, extPos)))
    			{
					UIRegistry.showLocalizedError("StrLocalizerApp.InvalidLocaleDir", langCode);
					return;
    			}
    			
    		}
    	}
    	if (lang == null)
		{
			UIRegistry.showLocalizedError("StrLocalizerApp.InvalidLocaleDir", langCode);
			return;
		}
        destLanguage = lang;
        final String newLang = destLanguage.getEnglishName();
        SwingUtilities.invokeLater(new Runnable() {

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
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
    		
    }
    /**
     * New language 'project'
     */
    protected void doNewLocale()
    {
        if (!checkForChanges())
        {
        	return;
        }
    	
    	ChooseFromListDlg<LanguageEntry> ldlg = new ChooseFromListDlg<LanguageEntry>((Frame )UIRegistry.getTopWindow(), 
        		UIRegistry.getResourceString("StrLocalizerApp.ChooseLanguageDlgTitle"), languages);
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

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run()
			{
				destLbl.setText(newLang);
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
        	int response = JOptionPane.showOptionDialog((Frame )UIRegistry.getTopWindow(), 
        			String.format(UIRegistry.getResourceString("StrLocalizerApp.SaveChangesMsg"), destFile.getPath()), 
        			UIRegistry.getResourceString("StrLocalizerApp.SaveChangesTitle"), 
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
    	
    	ChooseFromListDlg<StrLocaleFile> ldlg = new ChooseFromListDlg<StrLocaleFile>((Frame )UIRegistry.getTopWindow(), 
        		UIRegistry.getResourceString("StrLocalizerApp.ChooseFileDlgTitle"), srcFiles);
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
    protected void newSrcFile(StrLocaleFile newSrc)
    {
        termList.clearSelection();
        newTermList.clearSelection();
        
        srcFile = newSrc;
        
        //re-create and reload the dest file to clear unsaved changes and/or states
        int destInx = getLocaleFileInxBySrcPath(destFiles, srcFile.getPath());
        StrLocaleFile oldDest = destFiles.get(destInx);
        destFile = new StrLocaleFile(oldDest.getPath(), oldDest.getSrcPath(), true);
        destFiles.set(destInx, destFile);
        
        newKeyList.clear();
        
        mergeToSrc(srcFile, destFile);
        
        termList.setModel(new ItemModel(srcFile));
        DefaultListModel model = new DefaultListModel();
        model.clear();
        for (String str : newKeyList)
        {
            model.addElement(str);
        }
        newTermList.setModel(model);
        rsController.setLength(srcFile.getNumberOfKeys());
        
        SwingUtilities.invokeLater(new Runnable() {

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run()
			{
				fileLbl.setText(srcFile.getPath());				
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
        NameValuePair[] postParams = createPostParameters(inputText, "en_" + destLanguage.getCode());
        postMethod.setRequestBody(postParams);
        
        // connect to the server
        try
        {
            int status = httpClient.executeMethod(postMethod);
            //System.out.println(status);
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
                        
                        //System.out.println(responseString.substring(sInx+1, eInx));
                        
                        return responseString.substring(sInx+1, eInx);
                    }
                }
            }   

        }
        catch (Exception e)
        {
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
                
                JFrame frame = new JFrame(UIRegistry.getResourceString("StrLocalizerApp.AppTitle"));
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                StrLocalizerApp sl = new StrLocalizerApp();
                sl.addMenuBar(frame);
                frame.setContentPane(sl);
                frame.pack();
                Dimension size = frame.getPreferredSize();
                size.height = 500;
                frame.setSize(size);
                frame.addWindowListener(sl);
                UIRegistry.setAppName("Specify");  //$NON-NLS-1$
                IconManager.setApplicationClass(Specify.class);
            	frame.setIconImage(IconManager.getImage(IconManager.makeIconName("SpecifyWhite32")).getImage());
                UIHelper.centerAndShow(frame);
            }
        });
    }

    /**
     * @param code
     * @return
     */
    protected LanguageEntry getLanguageByCode(String code)
    {
    	for (LanguageEntry l : languages)
    	{
    		if (l.getCode().equals(code))
    		{
    			return l;
    		}
    	}
    	return null;
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
