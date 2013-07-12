/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tools.l10nios;

import static edu.ku.brc.ui.UIRegistry.FRAME;
import static edu.ku.brc.ui.UIRegistry.GLASSPANE;
import static edu.ku.brc.ui.UIRegistry.MAINPANE;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.register;
import static edu.ku.brc.ui.UIRegistry.setAppName;
import static edu.ku.brc.ui.UIRegistry.setTopWindow;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.getFullPath;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.api.translate.Language;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;
import com.jgoodies.looks.plastic.theme.ExperienceBlue;

import edu.ku.brc.af.core.FrameworkAppIFace;
import edu.ku.brc.af.core.MacOSAppHandler;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.ResultSetControllerListener;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
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
public class StrLocalizerAppForiOS extends JPanel implements FrameworkAppIFace, WindowListener
{
    private static final Logger  log = Logger.getLogger(StrLocalizerAppForiOS.class);
    
    protected JFrame                       frame         = null;
    protected GhostGlassPane               glassPane;

    protected File                         rootDir       = null;
    protected String                       currentPath   = null;
    protected Locale                       englishLocale = Locale.US;
    protected Locale                       destLanguage  = null;

    protected boolean                      doSrcParsing = false;
    
    // Terms in a file
    protected JList                        termList;
    protected ItemModel                    model         = null;
    
    // Files
    protected Vector<L10NFile>             srcFiles      = new Vector<L10NFile>();
    protected L10NFile                     currFile      = null;
    protected JList                        fileList;
    protected DefaultListModel             fileModel;
    
    
    protected JTextArea                    srcLbl;
    protected JTextArea                    comment;
    protected JLabel	                   destLbl;
    protected JTextField                   textField;
    protected JButton                      prevBtn;
    protected JButton                      nxtBtn;
    protected JButton                      transBtn;
    protected JLabel	                   fileLbl;
    protected JPanel                       mainPane;
    protected JStatusBar                   statusBar;
    
    // SearchBox
    
    protected JMenuItem                    startTransMenuItem;
    protected JMenuItem                    stopTransMenuItem;
    protected JMenuItem                    scanMI;
    
    protected L10NSrcIndexer               srcIndexer;
    protected L10NUIIndexer                uiIndexer;
    
    protected ResultSetController          rsController;
    protected ResultSetControllerListener  rscListener = null;
    protected int                          oldInx      = -1;
    
    protected Vector<String>               newKeyList  = new Vector<String>();
    protected AtomicBoolean                contTrans   = new AtomicBoolean(true);
    
    protected boolean                      isSkippingIndexes = false;
    
    /**
     * 
     */
    public StrLocalizerAppForiOS()
    {
        super();
        
        new MacOSAppHandler(this);
        
    }
    
    /**
     * @return
     */
    public String[] getFileNames()
    {
        // These need to be moved to an XML file
        String[] fileNames = {"Localizable.strings", 
                              "MainStoryboard_iPad.strings", 
                             };
        return fileNames;
    }
    
    /**
     * 
     */
    public void startUp()
    {
        //AskForDirectory afd = new AskForDirectory(frame);
        //currentPath = afd.getDirectory();
        FileDialog dlg = new FileDialog(frame, "Select a Directory", FileDialog.LOAD);
        UIHelper.centerAndShow(dlg);
        String currentPath = dlg.getDirectory();
        
        if (StringUtils.isNotEmpty(currentPath))
        {
            if (doSrcParsing)
            {
                File rootDir = new File("/Users/rods/Documents/SVN/SpecifyInsightL10N");
                srcIndexer = new L10NSrcIndexer(rootDir);
                doScanSources();
            }
            
            boolean isDirOK = true;
            rootDir = new File(currentPath);
            if (!rootDir.exists())
            {
                String dirPath = getFullPath(currentPath);
                rootDir = new File(dirPath);
                isDirOK = rootDir.exists();
            }
            
            if (isDirOK)
            {
                createUI();
                
                register(MAINPANE, mainPane);
                frame.setGlassPane(glassPane = GhostGlassPane.getInstance());
                frame.setLocationRelativeTo(null);
                Toolkit.getDefaultToolkit().setDynamicLayout(true);
                register(GLASSPANE, glassPane);
                
                if (setupSrcFiles(rootDir) > 0)
                {
                    frame.pack();
                    UIHelper.centerAndShow(frame);
                    
                    destLanguage = doChooseLangLocale(Locale.ENGLISH);
                    if (destLanguage != null)
                    {
                        destLbl.setText(destLanguage.getDisplayName() + ":");
                        return;
                    }
                } else
                {
                    UIRegistry.showError("The are no localizable files in the directory you selected.");
                    System.exit(0);
                }
            } 
        }

        UIRegistry.showError("StrLocalizer will exit.");
        System.exit(0);
    }
    
    /**
     * @param hideExisting
     * @return
     */
    private void appendLocale(final Vector<Locale> locales, final Vector<Locale> destLocales, final String language)
    {
        for (Locale l :locales)
        {
            if (l.getLanguage().equals(language) && StringUtils.isEmpty(l.getCountry()))
            {
                destLocales.add(l);
                break;
            }
        }
    }
    
    /**
     * @param hideExisting
     * @return
     */
    private Locale doChooseLangLocale(final Locale hideLocale)
    {
        Vector<Locale> locales = new Vector<Locale>();
        Collections.addAll(locales, Locale.getAvailableLocales());
        
        String[] langs = {"es", "sv", "sq", "fr"};
        Vector<Locale> destLocales = new Vector<Locale>();
        destLocales.add(Locale.GERMAN);
        for (String lang : langs)
        {
            appendLocale(locales, destLocales, lang);
        }
        
        Vector<String> localeNames = new Vector<String>();
        for (Locale l : destLocales)
        {
            localeNames.add(l.getDisplayName());
        }

        ToggleButtonChooserDlg<String> chooser = new ToggleButtonChooserDlg<String>((Frame) null,
                "CHOOSE_LOCALE", localeNames, ToggleButtonChooserPanel.Type.RadioButton);
        chooser.setUseScrollPane(true);
        UIHelper.centerAndShow(chooser);
        
        if (!chooser.isCancelled()) 
        { 
            return destLocales.get(chooser.getSelectedIndex()); 
        }
        return null;
    }

    /**
     * @param dirName
     */
    protected int setupSrcFiles(final File dir)
    {
    	srcFiles.clear();
    	String[] exts = {"strings"};
    	Collection<?> files = FileUtils.listFiles(dir, exts, false);
    	for (Object fobj : files)
    	{
    		File f = (File )fobj;
    		L10NFile l10nFile = new L10NFile(new File(dir.getAbsolutePath() + File.separator + f.getName()));
    		if (l10nFile.load())
    		{
    		    srcFiles.add(l10nFile);
    		}
    	} 
    	Collections.sort(srcFiles);
    	
    	fileModel = new DefaultListModel();
    	for (L10NFile f : srcFiles)
    	{
    	    fileModel.addElement(f);
    	}
    	fileList.setModel(fileModel);
    	SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                fileList.setSelectedIndex(0);
            }
        });
    	
    	return srcFiles.size();
    }
    
    /**
     * @param langCode the language code - "es", "en" ,etc
     *	
     * Assumes srcLocaleFiles has already been setup.
     */
//    protected void setupDestFiles(final String langCode, final String destPath)
//    {
//    	destFiles.clear();
//    	for (StrLocaleFile f : srcFiles)
//    	{
//    		String newPath;
//    		if (destPath != null)
//    		{
//    			File duh = new File(f.getPath());
//    			String newName = duh.getName().replace("_" + srcLangCode + ".", "_" + langCode + ".");
//    			newPath = destPath + File.separator + newName;
//    		}
//    		else
//    		{
//    		    String path = f.getPath();
//                newPath = path.replace("_" + srcLangCode + ".", "_" + langCode + ".");
//                newPath = newPath.replace(File.separator + srcLangCode + File.separator, File.separator + langCode + File.separator);
//    		}
//    		destFiles.add(new StrLocaleFile(newPath, f.getPath(), true));
//    	}
//    }
    
    private JTextArea setTAReadOnly(final JTextArea textArea)
    {
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        if (!UIHelper.isMacOS())
        {
            textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            //textArea.setBackground(new Color(245,245,245));
        }
        return textArea;
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

        fileList = new JList(fileModel = new DefaultListModel());
        termList = new JList(model = new ItemModel(null));
        
        srcLbl = setTAReadOnly(UIHelper.createTextArea(3, 40));
        //srcLbl.setBorder(new LineBorder(srcLbl.getForeground()));
        textField  = UIHelper.createTextField(40);
        
        comment = setTAReadOnly(UIHelper.createTextArea(3, 40));
        
        /*textField.getDocument().addDocumentListener(new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e)
            {
                hasChanged = true;
            }
        });*/
        
        statusBar = new JStatusBar();
        statusBar.setSectionText(1, "     "); //$NON-NLS-1$ //$NON-NLS-2$
        UIRegistry.setStatusBar(statusBar);

        srcLbl.setEditable(false);
        
        rsController = new ResultSetController(null, false, false, false, "", 1, true);
        
        transBtn = UIHelper.createButton(getResourceString("StrLocalizerApp.Translate"));
        transBtn.setVisible(false);

        PanelBuilder pbr = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,p,4px,p,4px,p,4px,p,4px,p"));

        //pbr.add(UIHelper.createLabel(getResourceString("StrLocalizerApp.FileLbl")), cc.xy(1, 1));
        //fileLbl = UIHelper.createLabel("   ");
        //pbr.add(fileLbl, cc.xy(3, 1));

        int y = 1;
        pbr.addSeparator("Item", cc.xyw(1,y,3));
        y += 2;
        
        pbr.add(UIHelper.createLabel("English:", SwingConstants.RIGHT), cc.xy(1, y));
        pbr.add(srcLbl, cc.xy(3, y));
        y += 2;
        
        pbr.add(UIHelper.createFormLabel("Comment", SwingConstants.RIGHT), cc.xy(1, y));
        pbr.add(comment,                             cc.xy(3, y));
        y += 2;
        
        destLbl = UIHelper.createFormLabel("", SwingConstants.RIGHT);//destLanguage.getDisplayName());
        pbr.add(destLbl,                          cc.xy(1, y));
        pbr.add(textField,                        cc.xy(3, y));
        y += 2;
        
        pbr.add(rsController.getPanel(),          cc.xyw(1, y, 3));
        y += 2;
        pbr.add(transBtn,                         cc.xy(1,  y));

        JScrollPane sp  = UIHelper.createScrollPane(termList);
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "p,4px,f:p:g,10px,p"));
        pb.addSeparator("Localize", cc.xy(1,1));
        pb.add(sp,                  cc.xy(1,3));
        pb.add(pbr.getPanel(),      cc.xy(1,5));
        pb.setDefaultDialogBorder();
        
        ResultSetController.setBackStopRS(rsController);

        PanelBuilder fpb = new PanelBuilder(new FormLayout("8px,f:p:g", "p,4px,f:p:g"));
        JScrollPane filesp  = UIHelper.createScrollPane(fileList);
        fpb.add(UIHelper.createLabel("Files", SwingConstants.CENTER), cc.xy(2,1));
        fpb.add(filesp, cc.xy(2,3));

        setLayout(new BorderLayout());
        add(fpb.getPanel(), BorderLayout.WEST);
        add(pb.getPanel(), BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        
        mainPane = this;
        
        textField.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                checkForChange();
            }
        });
        
        termList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    itemSelected();
                }
            }
        });
        
        fileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    fileSelected();
                }
            }
        });
        
        transBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String txt     = srcLbl.getText();
                String newText = translate(txt);
                if (StringUtils.isNotEmpty(newText))
                {
                    newText = newText.replace("&#039;", "'");
                    textField.setText(newText);
                    L10NItem entry = (L10NItem)termList.getSelectedValue();
                    entry.setValue(textField.getText());
                }
            }
        });
        
        rscListener = new ResultSetControllerListener()
        {
            @Override
            public void newRecordAdded(){}
            
            @Override
            public void indexChanged(int newIndex)
            {
                termList.setSelectedIndex(newIndex);
            }
            
            @Override
            public boolean indexAboutToChange(int oldIndex, int newIndex)
            {
                return true;
            }
        };
        
        rsController.addListener(rscListener);
    }
    
    /**
     * 
     */
    private void checkForChange()
    {
        if (oldInx > -1)
        {
            L10NItem entry = (L10NItem)model.getElementAt(oldInx);
            String oldText = entry.getValue();
            String newText = textField.getText();
            if (oldText == null || !oldText.equals(newText))
            {
                entry.setValue(newText);
                entry.setChanged(true);
                setDataHasChanged();
            }
        }
    }
    
    /**
     * 
     */
    private void setDataHasChanged()
    {
        String title = frame.getTitle();
        if (!title.endsWith("*"))
        {
            frame.setTitle(title + " *");
        }
    }
    
    /**
     * 
     */
    private void fileSelected()
    {
        checkForChange();
        
        loadFileIntoList((L10NFile)fileList.getSelectedValue());
    }
    
    /**
     * 
     */
    private void itemSelected()
    {
        checkForChange();
        
        int inx = termList.getSelectedIndex();
        if (inx > -1)
        {
            transBtn.setEnabled(true);
            L10NItem srcEntry = (L10NItem)model.getElementAt(inx);
            //System.out.println(srcEntry.hashCode());
            
            srcLbl.setText(srcEntry.getTitle());
            
            String comStr = srcEntry.isUIComp() ? "" : srcEntry.getComment();
            if (StringUtils.isNotEmpty(comStr))
            {
                comStr = StringUtils.remove(comStr, "/* ");
                comStr = StringUtils.remove(comStr, " */").trim();
                comStr = StringUtils.replace(comStr, " \n", "\n");
                comStr = StringUtils.replace(comStr, "\n  ", "\n");
                comStr = StringUtils.replace(comStr, "\n", ";\n");
            }
            comment.setText(comStr);
            
            String str = srcEntry.getValue();
            
            try
            {
                str = new String(str.getBytes("UTF-8"), "UTF-8");
            } catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
            textField.setText(str);//str != null ? str : srcEntry.getKey());
            
            rsController.removeListener(rscListener);
            rsController.setIndex(inx);
            rsController.addListener(rscListener);
            
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    textField.requestFocus();
                    textField.selectAll();
                }
            });
            
        } else
        {
            srcLbl.setText("");
            textField.setText("");
            transBtn.setEnabled(false);
        }
        oldInx = inx;
    }
    
    /**
     * @param code
     * @return
     */
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
//        if (inputText.isEmpty()) return "";
//        //System.out.println("\n"+inputText);
//        
//        Translate.setHttpReferrer("http://www.specifysoftware.org");
//
//        try
//        {
//            String text = inputText;
//            
//            boolean hasSpecialChars = false;
//            while (StringUtils.contains(text, "%d") || 
//                    StringUtils.contains(text, "%s") || 
//                    StringUtils.contains(text, "\\n"))
//            {
//                text = StringUtils.replace(text, "%d", "99");
//                text = StringUtils.replace(text, "%s", "88");
//                text = StringUtils.replace(text, "\\n", " 77 ");
//                hasSpecialChars = true;
//            }
//            
//            Language lang = getLangFromCode(destLanguage.getCode());
//            //System.out.println(text);
//            String newText = Translate.execute(text, Language.ENGLISH, lang);
//            
//            if (hasSpecialChars)
//            {
//                while (StringUtils.contains(newText, "77") ||
//                       StringUtils.contains(newText, "88") ||
//                       StringUtils.contains(newText, "99"))
//                {
//                    newText = StringUtils.replace(newText, "99", "%d");
//                    newText = StringUtils.replace(newText, "88", "%s");
//                    newText = StringUtils.replace(newText, " 77 ", " \\n ");
//                    newText = StringUtils.replace(newText, "77 ", "\\n ");
//                    newText = StringUtils.replace(newText, " 77", " \\n");
//                }
//            }
//            //System.out.println(newText);
//            return newText;
//            
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
        return null;
    }
    
    /**
     * 
     */
    private void translateNewItems()
    {
//        //writeGlassPaneMsg(getResourceString("StrLocalizerApp.TranslatingNew"), 24);
//        final String    STATUSBAR_NAME = "STATUS";
//        final JStatusBar statusBar     = UIRegistry.getStatusBar();
//        statusBar.setProgressRange(STATUSBAR_NAME, 0, 100);
//        
//        startTransMenuItem.setEnabled(false);
//        stopTransMenuItem.setEnabled(true);
//        
//        final double total = newKeyList.size();
//        
//        SwingWorker<Integer, Integer> translator = new SwingWorker<Integer, Integer>()
//        {
//            @Override
//            protected Integer doInBackground() throws Exception
//            {
//                int count = 0;
//                for (String key : newKeyList)
//                {
//                    L10NItem entry = srcFile.getItemHash().get(key);
//                    //if (StringUtils.contains(entry.getSrcStr(), "%") || StringUtils.contains(entry.getSrcStr(), "\n"))
//                    {
//                        String transText = translate(entry.getSrcStr());
//                        if (transText != null)
//                        {
//                            entry.setDstStr(transText);
//                            //writeGlassPaneMsg(String.format("%d / %d", count, newKeyList.size()), 18);
//                            //System.out.println(String.format("%s - %d / %d", key, count, newKeyList.size()));
//                        }
//                    }
//                    
//                    try
//                    {
//                        Thread.sleep(100 + (int)(Math.random() * 100.0));
//                    } catch (InterruptedException ex) {}
//                    
//                    setProgress((int)(((double)count/total)*100.0));
//                    System.out.println(entry.getSrcStr()+"  "+count);
//                    //glassPane.setProgress((int)( (100.0 * count) / total));
//                    count++;
//                    
//                    if (!contTrans.get())
//                    {
//                        return null;
//                    }
//                }
//                return null;
//            }
//
//            /* (non-Javadoc)
//             * @see javax.swing.SwingWorker#done()
//             */
//            @Override
//            protected void done()
//            {
//                //glassPane.setProgress(100);
//                //clearGlassPaneMsg();
//                
//                //statusBar.setIndeterminate(STATUSBAR_NAME, false);
//                statusBar.setText("");
//                statusBar.setProgressDone(STATUSBAR_NAME);
//                
//                UIRegistry.showLocalizedMsg("Done Localizing");
//                
//                startTransMenuItem.setEnabled(true);
//                stopTransMenuItem.setEnabled(false);
//                
//
//            }
//        };
//        
//        statusBar.setIndeterminate(STATUSBAR_NAME, true);
//        
//        translator.addPropertyChangeListener(
//                new PropertyChangeListener() {
//                    public  void propertyChange(final PropertyChangeEvent evt) {
//                        //System.out.println(evt.getPropertyName());
//                        
//                        if ("progress".equals(evt.getPropertyName())) 
//                        {
//                            statusBar.setText(String.format("%d / 100 ", (Integer)evt.getNewValue()) + "%");
//                        }
//                    }
//                });
//        translator.execute();
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
        
        boolean isOK = true;
        for (L10NFile file : srcFiles)
        {
            if (!file.save())
            {
                isOK = false;
            }
        }
        
        if (isOK)
        {
            String title = frame.getTitle();
            if (title.endsWith("*"))
            {
                frame.setTitle(title.substring(0, title.length()-2));
            }
        }
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
        
        JMenu scanMenu = new JMenu(getResourceString("Scan"));
        menuBar.add(scanMenu);
        
        scanMI = new JMenuItem(getResourceString("Source Code"));
        scanMenu.add(scanMI);
        
        scanMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                 scanSources();
            }
        });
        
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
        transMenu.setVisible(false);
        
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
     * @return true if no unsaved changes are present
     * else return results of prompt to save
     */
    protected boolean checkForChanges()
    {
//        if (termList != null)
//        {
//            int s = termList.getSelectedIndex();
//            if (s != -1)
//            {
//            	L10NItem entry = srcFile.getKey(s);
//            	entry.setDstStr(textField.getText());
//            }
//            if (srcFile.isEdited())
//            {
//            	int response = JOptionPane.showOptionDialog((Frame )getTopWindow(), 
//            			String.format(getResourceString("StrLocalizerApp.SaveChangesMsg"), destFile.getPath()), 
//            			getResourceString("StrLocalizerApp.SaveChangesTitle"), 
//            			JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.YES_OPTION);
//            	if (response == JOptionPane.CANCEL_OPTION)
//            	{
//            		return false;
//            	}
//            	if (response == JOptionPane.YES_OPTION)
//            	{
//            		doSave(); 
//            		return true; //what if it fails? 
//            	}
//            }
//            return true;
//        }
        
        boolean hasChanges = false;
        for (L10NFile f : srcFiles)
        {
            if (f.isChanged())
            {
                hasChanges = true;
                break;
            }
        }
        
        if (hasChanges)
          {
            int response = JOptionPane.showOptionDialog((Frame )getTopWindow(), 
                    "Changes have not been saved.\n\nDo you wish to save them?", 
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
    
    private void doScanSources()
    {
        srcIndexer.startIndexing();
        srcIndexer.startSearching();
    }
    
    private void scanSources()
    {
        scanMI.setEnabled(false);
        
        final String STATUSBAR_NAME = "STATUS";
        final JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setProgressRange(STATUSBAR_NAME, 0, 100);

        SwingWorker<Integer, Integer> translator = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                doScanSources();
                return null;
            }
            @Override
            protected void done()
            {
                scanMI.setEnabled(false);
            }
        };

        translator.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(final PropertyChangeEvent evt)
            {
                if ("progress".equals(evt.getPropertyName()))
                {
                    statusBar.setText(String.format("%d / 100 ", (Integer) evt.getNewValue()) + "%");
                }
            }
        });
        translator.execute();      
    }
    
    /**
     * Open another properties file for translation
     */
    protected void doChooseFile()
    {
    	if (!checkForChanges())
    	{
    		//return;
    	}
    	
    	ChooseFromListDlg<L10NFile> ldlg = new ChooseFromListDlg<L10NFile>((Frame )getTopWindow(), 
        		                                   getResourceString("StrLocalizerApp.ChooseFileDlgTitle"), srcFiles);
        UIHelper.centerAndShow(ldlg);
        if (ldlg.isCancelled() || ldlg.getSelectedObject() == null)
        {
        	return;
        }
        
        if (destLanguage == null)
        {
            destLanguage = doChooseLangLocale(Locale.ENGLISH);
            if (destLanguage != null)
            {
                destLbl.setText(destLanguage.getDisplayName() + ":");
                loadFileIntoList(ldlg.getSelectedObject());
                
                frame.pack();
            } else
            {
                System.exit(0);
            }
        }
    }
    
    /**
     * @param file
     */
    protected void loadFileIntoList(final L10NFile file)
    {
        termList.clearSelection();
        model = new ItemModel(file);
        termList.setModel(model);
        rsController.setLength(file.getItems().size());
    }
    
    /**
     * @param newSrc
     */
    /*protected void newSrcFile(final StrLocaleFile newSrc)
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
    }*/
    
    /**
     * @param locale
     * @return
     */
//    private String getFullLang(final Locale locale)
//    {
//        return locale.getLanguage() + (StringUtils.isNotEmpty(locale.getVariant()) ? ("_"+locale.getVariant()): "");
//    }

    /**
     * @param hideExisting
     * @return
     */
//    private Locale doChooseLangLocale(final boolean hideExisting)
//    {
//
//        HashSet<String> existingLocs = new HashSet<String>();
//        if (hideExisting)
//        {
//            for (String nm : rootDir.list())
//            {
//                if (!nm.startsWith("."))
//                {
//                    existingLocs.add(nm);
//                }
//            }
//        }
//        
//        Vector<Locale> locales = new Vector<Locale>();
//        for (Locale l : Locale.getAvailableLocales())
//        {
//            if (!hideExisting || !existingLocs.contains(getFullLang(l)))
//            {
//                locales.add(l);
//            }
//        }
//        Collections.sort(locales, new Comparator<Locale>()
//        {
//            public int compare(Locale o1, Locale o2)
//            {
//                return o1.getDisplayName().compareTo(o2.getDisplayName());
//            }
//        });
//
//        Vector<String> localeNames = new Vector<String>();
//        for (Locale l : locales)
//        {
//            localeNames.add(l.getDisplayName());
//        }
//
//        ToggleButtonChooserDlg<String> chooser = new ToggleButtonChooserDlg<String>((Frame) null,
//                "CHOOSE_LOCALE", localeNames, ToggleButtonChooserPanel.Type.RadioButton);
//        chooser.setUseScrollPane(true);
//        chooser.setVisible(true);
//        if (!chooser.isCancelled()) 
//        { 
//            return locales.get(chooser.getSelectedIndex()); 
//        }
//        return null;
//    }
    
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
        
        for (String s : args)
        {
            String[] pairs = s.split("="); //$NON-NLS-1$
            if (pairs.length == 2)
            {
                if (pairs[0].startsWith("-D")) //$NON-NLS-1$
                {
                    //System.err.println("["+pairs[0].substring(2, pairs[0].length())+"]["+pairs[1]+"]");
                    System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
                } 
            } else
            {
                String symbol = pairs[0].substring(2, pairs[0].length());
                //System.err.println("["+symbol+"]");
                System.setProperty(symbol, symbol);
            }
        }
        
        // Now check the System Properties
        String appDir = System.getProperty("appdir");
        if (StringUtils.isNotEmpty(appDir))
        {
            UIRegistry.setDefaultWorkingPath(appDir);
        }
        
        String appdatadir = System.getProperty("appdatadir");
        if (StringUtils.isNotEmpty(appdatadir))
        {
            UIRegistry.setBaseAppDataDir(appdatadir);
        }
        
        
        // Then set this
        IconManager.setApplicationClass(Specify.class);
        IconManager.loadIcons(XMLHelper.getConfigDir("icons.xml")); //$NON-NLS-1$
        //ImageIcon icon = IconManager.getIcon("AppIcon", IconManager.IconSize.Std16);
        
        try
        {
            ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
            
        } catch (MissingResourceException ex)
        {
            Locale.setDefault(Locale.ENGLISH);
            UIRegistry.setResourceLocale(Locale.ENGLISH);
        }
        
        try
        {
            if (!System.getProperty("os.name").equals("Mac OS X"))
            {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
            }
        }
        catch (Exception e)
        {
            //whatever
        }
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
                final StrLocalizerAppForiOS sl = new StrLocalizerAppForiOS();
                sl.addMenuBar(frame);
                frame.setContentPane(sl);
                frame.setSize(768,1024);
                
                //Dimension size = frame.getPreferredSize();
                //size.height = 500;
                //frame.setSize(size);
                frame.addWindowListener(sl);
                IconManager.setApplicationClass(Specify.class);
            	frame.setIconImage(IconManager.getImage(IconManager.makeIconName("SpecifyWhite32")).getImage());
                
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

    
    
    //-------------------------------------------------------------------------------
    //-- List Model
    //-------------------------------------------------------------------------------
    class ItemModel extends AbstractListModel
    {
        protected L10NFile l10nFile;
        
        public ItemModel(final L10NFile l10nFile)
        {
            this.l10nFile = l10nFile;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.ListModel#getElementAt(int)
         */
        @Override
        public Object getElementAt(final int index)
        {
        	return l10nFile != null ? l10nFile.getItems().get(index) : "N/A";
        }

        /* (non-Javadoc)
         * @see javax.swing.ListModel#getSize()
         */
        @Override
        public int getSize()
        {
            return l10nFile != null ? l10nFile.getItems().size() : 0;
        }
    }
    
    //-------------------------------------------------------------------------------
    //-- L10NItem
    //-------------------------------------------------------------------------------
    private class L10NItem
    {
        private boolean isUIComp;
        private String  title;
        private String  comment;
        private String  key;
        private String  value;
        private boolean isInUse;
        private boolean isChanged;
        private boolean isDuplicate;
        private Boolean isMappable;
        private L10NFile l10nFile;
        private Vector<L10NItem> dups = new Vector<L10NItem>();  
    	
    	/**
    	 * @param l10nFile
    	 * @param comment
    	 * @param key
    	 * @param value
    	 * @param isUIComp
    	 * @param isInUse
    	 */
    	public L10NItem(final L10NFile l10nFile,
    	                final String comment, 
    	                final String key, 
    	                final String value, 
    	                final boolean isUIComp,
    	                final boolean isInUse)
    	{
            this.l10nFile = l10nFile;
            this.comment  = comment;
            this.key      = key;
            this.value    = value;
            this.isUIComp = isUIComp;
            this.isInUse  = isInUse;
            this.isChanged = false;
            this.title     = null;
            this.isMappable = null;
            this.isDuplicate = false;
    	}

        /**
         * @return the value
         */
        public String getValue()
        {
            return value;
        }

        /**
         * @param value the value to set
         */
        public void setValue(String value)
        {
            this.value = value;
            for (L10NItem itm : dups)
            {
                itm.setValue(value);
            }
        }

        /**
         * @return the isUIComp
         */
        public boolean isUIComp()
        {
            return isUIComp;
        }

        /**
         * @return the comment
         */
        public String getComment()
        {
            return comment;
        }

        /**
         * @return the isDuplicate
         */
        public boolean isDuplicate()
        {
            return isDuplicate;
        }

        /**
         * @param isDuplicate the isDuplicate to set
         */
        public void setDuplicate(boolean isDuplicate)
        {
            this.isDuplicate = isDuplicate;
        }
        
        public void addDup(final L10NItem item)
        {
            if (!isUIComp)
            {
                mergeComment(item.getComment());
            }
            dups.add(item);
        }
        
        public int getDupCnt()
        {
            return dups.size();
        }

        /**
         * @param isChanged the isChanged to set
         */
        public void setChanged(boolean isChanged)
        {
            this.isChanged = isChanged;
            this.l10nFile.setChanged(true);
        }

        /**
         * @param l10nFile the l10nFile to set
         */
        public void setL10nFile(L10NFile l10nFile)
        {
            this.l10nFile = l10nFile;
        }
        
        public void mergeComment(final String newComment)
        {
            comment = StringUtils.remove(comment, "*/");
            String newCom = StringUtils.remove(newComment, "/*").trim();
            comment += "\n   " + newCom;
        }

        /**
         * @return the isMappable
         */
        public boolean isMappable()
        {
            if (isMappable == null)
            {
                String token = isUIComp ? value : key;
                if (!isInUse || isDuplicate || StringUtils.isEmpty(token) || 
                    StringUtils.isWhitespace(token) || 
                    token.equals("Label") ||
                    token.equals("Untitled") ||
                    (token.length() == 2 && token.charAt(0) == '%'))
                {
                    return isMappable = false;
                }
                isMappable = true;
            }
            return isMappable;
        }
        
        /**
         * @param pw
         */
        public void print(final PrintWriter pw)
        {
            if (isUIComp || !isDuplicate)
            {
                if (isInUse) pw.println();
                pw.println(comment);
                pw.println(String.format("\"%s\" = \"%s\";", key, value));
                if (!isInUse) pw.println();
            }
        }
        
        /**
         * @param attr
         * @return
         */
        private String getAttr(final String attr)
        {
            if (StringUtils.isNotEmpty(comment))
            {
                int inx = comment.indexOf(attr);
                if (inx > 0)
                {
                    inx += attr.length() + 3;
                    if (comment.charAt(inx) == '"')
                    {
                        inx++;
                        int endInx = comment.indexOf('"', inx);
                        if (endInx > -1)
                        {
                            return comment.substring(inx, endInx);
                        }
                    }
                }
            }
            return null;       
        }

        /**
         * @return the title
         */
        public String getTitle()
        {
            if (isUIComp)
            {
                if (title == null)
                {
                    String[] keys = {"title", "text", "headerTitle", "normalTitle", "placeholder", "ObjectID"};
                    for (String k : keys)
                    {
                        title = getAttr(k);
                        if (title != null)
                        {
                            return title;
                        }
                    }
                    title = "N/A";
                }
                return title;
            }
            return key;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return getTitle();
        }
    }
    
    //-------------------------------------------------------------------------------
    //-- L10NFile
    //-------------------------------------------------------------------------------
    class L10NFile implements Comparable<L10NFile>
    {
        private File             file;
        private Vector<L10NItem> fileItems;
        private Vector<L10NItem> items;
        private int[]            indexMap;
        private boolean          isChanged;
        private boolean          isUIComp;
        
        private int              currLineInx;
        private HashMap<String, L10NItem> itemsHash = new HashMap<String, L10NItem>();
        
        
        L10NUIIndexer uiIndexer = null;

        
        /**
         * @param file
         */
        public L10NFile(final File file)
        {
            super();
            this.file       = file;
            this.fileItems  = null;
            this.isChanged  = false;
            this.isUIComp   = false;
            
            String fName  = file.getAbsolutePath();
            String sbName = String.format("%s%s.%s", getFullPath(fName), getBaseName(fName), "storyboard");
            File uiFile = new File(sbName);
            if (uiFile.exists())
            {
                isUIComp = true;
                uiIndexer = new L10NUIIndexer(rootDir);
                uiIndexer.load(uiFile);
            }
        }
        
        /**
         * @param s
         * @return
         */
        private String unicodeEscape(String s)  
        {
            try
            {
                byte[] bytes = new byte[s.length()*2];
                int i=0;
                for (byte b : s.getBytes())
                {
                    bytes[i++] = b;
                    bytes[i++] = 0;
                }
                return new String(bytes, 0, bytes.length, "UTF-8");
            } catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
            return null;
        }
        
        /**
         * @param s
         */
        private void dump(String s)
        {
            try
            {
                for (byte b : s.getBytes("UTF-8"))
                {
                    System.out.print(b+", ");
                }
                System.out.println();
            } catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }
        
        /**
         * @param lines
         * @return
         */
        private String getComment(final List<String> lines)
        {
            StringBuilder sb   = new StringBuilder();
            String        line = lines.get(currLineInx);
            int           sInx = line.indexOf("/*");
            if (sInx > -1 && sInx < 5)
            {
                line = line.substring(sInx, line.length());
                do
                {
                    sb.append(line);
                    currLineInx++;
                    int eInx = line.lastIndexOf("*/");
                    int len  = line.length();
                    if (eInx == len-2)
                    {
                        return sb.toString();
                    }
                    
                    line = lines.get(currLineInx);
                } while (currLineInx < lines.size());
            }
            return null;
        }
        
        /**
         * @return
         */
        public boolean load()
        {
            if (file != null && !file.isDirectory() && file.exists())
            {
                try
                {
                    fileItems = new Vector<L10NItem>();
                    
                    List<String> fileLines = (List<String>)FileUtils.readLines(file, "UTF-16");
                    Vector<String> lines = new Vector<String>();
                    for (String line : fileLines)
                    {
                        if (StringUtils.isNotEmpty(line))
                        {
                            lines.add(line);
                        }
                    }
                    currLineInx = 0;
                    
                    String  idStr         = null;
                    String initialComment = getComment(lines);
                    if (initialComment != null)
                    {
                        int inx = initialComment.indexOf("ObjectID");
                        if (inx > -1)
                        {
                            isUIComp = true;
                            inx = initialComment.indexOf('"', inx);
                            if (inx > -1)
                            {
                                inx++;
                                int eInx = initialComment.indexOf('"', inx);
                                if (eInx > -1)
                                {
                                    isUIComp = true;
                                    idStr = initialComment.substring(inx, eInx);
                                }
                            }
                        }
                    }
                    
                    boolean hasChanges = false;
                    currLineInx = 0;
                    while (currLineInx < lines.size())
                    {
                        String comment = getComment(lines);
                        if (comment == null) return false;
                        String line = lines.get(currLineInx++);
                        int    inx  = line.indexOf("\" = \"");
                        if (inx > -1)
                        {
                            String   key     = line.substring(1, inx);
                            boolean  isInUse = (isUIComp && (uiIndexer == null || uiIndexer.hasId(idStr))) || 
                                               (!isUIComp && (srcIndexer == null || srcIndexer.searchFor(key)));
                            String   value   = line.substring(inx+5, line.length()-2);
                            
                            L10NItem item    = new L10NItem(this, comment, key, value, isUIComp, isInUse);
                            fileItems.add(item);
                            
                            String   itmTitle = item.getTitle();
                            L10NItem itm      = itemsHash.get(itmTitle);
                            if (itm == null)
                            {
                                itemsHash.put(itmTitle, item);
                            } else
                            {
                                item.setDuplicate(true);
                                itm.addDup(item);
                            }
                            
                            if (!isInUse) 
                            {
                                hasChanges = true;
                                //System.err.println(String.format("Skipping [%s][%s] %s", key, idStr != null ? idStr : "", file.getName()));
                            }
                        } else
                        {
                            return false;
                        }
                    }
                    
                    System.out.println(file.getName()+" Duplicates: "+itemsHash.size());
                    for (L10NItem itm : itemsHash.values())
                    {
                        if (itm.getDupCnt() > 0) System.out.println("  dup: ["+itm.getTitle()+"] "+itm.getDupCnt());
                    }
                    
                    if (hasChanges)
                    {
                        this.isChanged = true;
                        setDataHasChanged();
                    }
                    
                    items    = new Vector<L10NItem>();
                    indexMap = new int[fileItems.size()];
                    
                    // Map localizable items
                    int inx = 0;
                    for (int i=0;i<fileItems.size();i++)
                    {
                        L10NItem item = fileItems.get(i);
                        if (item.isMappable())
                        {
                            indexMap[inx++] = i;
                            items.add(item);
                        }
                    }

                    return true;
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            return false;
        }
        
        /**
         * @return the file
         */
        public File getFile()
        {
            return file;
        }
        /**
         * @param file the file to set
         */
        public void setFile(File file)
        {
            this.file = file;
        }
        /**
         * @return the fileItems
         */
        public Vector<L10NItem> getFileItems()
        {
            return fileItems;
        }
        /**
         * @return the fileItems
         */
        public Vector<L10NItem> getItems()
        {
            return items;
        }
        
        /**
         * @param fileItems the fileItems to set
         */
        public void setFileItems(Vector<L10NItem> fileItems)
        {
            this.fileItems = fileItems;
        }
        
        /**
         * @param isChanged the isChanged to set
         */
        public void setChanged(boolean isChanged)
        {
            this.isChanged = isChanged;
        }
        
        /**
         * @return the isChanged
         */
        public boolean isChanged()
        {
            return isChanged;
        }

        /**
         * 
         */
        public boolean save()
        {
            if (this.isChanged)
            {
                OutputStreamWriter osw;
                try
                {
                    String fName   = file.getAbsolutePath();
                    String ds      = (new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")).format(Calendar.getInstance().getTime());
                    String bakName = String.format("%s%s_%s.%s.bak", getFullPath(fName), getBaseName(fName), ds, getExtension(fName));
                    FileUtils.copyFile(file, new File(bakName));
                    
                    osw = new OutputStreamWriter(new FileOutputStream(file), "UTF-16");
                    PrintWriter pw = new PrintWriter(osw);
                    for (L10NItem item : fileItems)
                    {
                        item.print(pw);
                    }
                    pw.close();
                    
                    // Do it after saving
                    this.isChanged = false;
                    for (L10NItem item : items)
                    {
                        item.setChanged(false);
                    }
                    return true;
                    
                } catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                } catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                return false;
            }
            return true;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(final L10NFile o)
        {
            return file.getName().compareTo(o.getFile().getName());
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return file.getName();
        }
        
    }
}
