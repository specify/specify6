/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tools.schemalocale;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 3, 2007
 *
 */
public class LocalizerApp extends LocalizableBaseApp
{
    private static final Logger log = Logger.getLogger(LocalizerApp.class);
 
    protected static String    fileName   = "field_desc.xml";
    
    protected Locale           currLocale = Locale.getDefault();
    

    
    protected Hashtable<String, String>         resHash     = new Hashtable<String, String>();
    protected Hashtable<String, PackageTracker> packageHash = new Hashtable<String, PackageTracker>();
    protected Hashtable<String, Boolean>        nameHash    = new Hashtable<String, Boolean>();
    
    /**
     * 
     */
    public LocalizerApp()
    {
        
        new MacOSAppHandler(this);
        
        appName             = "Schema Localizer";
        appVersion          = "6.0";
        appBuildVersion     = "200706111309 (SVN: 2291)";
        
        setTitle(appName + " " + appVersion);// + "  -  "+ appBuildVersion);
    }
    
 
    protected void printLocales(final PrintWriter pw,
                                final LocalizableItemIFace parent, 
                                final LocalizableItemIFace lndi, 
                                final String lang, final String country)
    {/*
        for (Name nm : lndi.getNames())
        {
            if (nm.getLang().equals(lang) && nm.getCountry().equals(country))
            {
                if (parent != null)
                {
                    pw.write(parent.getName() + "_");
                }
                pw.write(lndi.getName());
                pw.write("=");
                pw.write(nm.getText());
                pw.write("\n");
            }
        }
        for (Desc d : lndi.getDescs())
        {
            if (parent != null)
            {
                pw.write(parent.getName() + "_");
            }
            pw.write(lndi.getName());
            pw.write("_desc");
            pw.write("=");
            pw.write(d.getText());
            pw.write("\n");
        }
        */
    }
    
    
    protected String findLineWith(final List<String> lines, final String key)
    {
        for (String line : lines)
        {
            if (line.indexOf(key) > -1)
            {
                return line;
            }
        }
        return null;
    }
    
    protected boolean isAllCaps(final String key)
    {
        for (int i=0;i<key.length();i++)
        {
            char c = key.charAt(i);
            if (Character.isLowerCase(c))
            {
                return false;
            }
        }
        return true;
    }
    
    protected void extractQuotedValues(final String line, Vector<String> keyNamesList)
    {
        String[] toks = StringUtils.split(line, "\"");
        int cnt = 1;
        for (String t : toks)
        {
            //System.err.println("T["+t+"]");
            if (cnt % 2 == 0)
            {
                keyNamesList.add(t);
            }
            cnt++;
        }
    }
    
    protected int parseForNames(final String line, 
                                final int sinx, 
                                final char termChar,
                                final Vector<String> keyNamesList,
                                final List<String> lines,
                                final int lineNum)
    {
        int einx = line.indexOf(termChar, sinx);
        if (einx == -1)
        {
            log.error("Couldn't closing '"+termChar+"' ["+line+"]");
            return -1;
            
        } else if (einx == sinx+1)
        {
            return -1;
        }
        
        if (line.charAt(sinx) == '\"') // Has Quotes
        {
            if (line.charAt(einx-1) == '\"')
            {
                einx--;
            } else
            {
                System.err.println("No Quote at end! ["+line+"] "+lineNum);
            }
            keyNamesList.add(line.substring(sinx+1, einx));
            
        } else
        {
            String key = line.substring(sinx, einx);
            int inx = key.indexOf('[');
            if (inx > -1) // Is an array
            {
                String srcLine = findLineWith(lines, key);
                if (srcLine != null)
                {
                    Vector<String> keys = new Vector<String>();
                    extractQuotedValues(key, keys);
                    if (keys.size() > 0)
                    {
                        keyNamesList.addAll(keys);
                    } else
                    {
                        System.err.println("1 - Source line for Key["+key+"]  src["+srcLine+"]");
                    }
                    //for (String nm : keyNamesList)
                    //{
                    //    System.err.println("Fnd["+nm+"]");
                    //}
                } else
                {
                    System.err.println("Couldn't find source line for ["+key+"] "+lineNum);
                }
            } else
            {
                inx = key.indexOf('\"');
                if (inx > -1) // Has one or more quote's
                {
                    Vector<String> keys = new Vector<String>();
                    extractQuotedValues(key, keys);
                    if (keys.size() > 0)
                    {
                        keyNamesList.addAll(keys);
                    } else
                    {
                        System.err.println("2 - Source line for Key["+key+"]  src["+key+"]");
                    }
                } else
                {
                    if (isAllCaps(key))
                    {
                        String srcLine = findLineWith(lines, key);
                        if (srcLine != null)
                        {
                            Vector<String> keys = new Vector<String>();
                            extractQuotedValues(srcLine, keys);
                            if (keys.size() > 0)
                            {
                                keyNamesList.addAll(keys);
                            } else
                            {
                                System.err.println("3 - Source line for Key["+key+"]  src["+srcLine+"]");
                            }
                        } else
                        {
                            System.err.println("Couldn't find source line for ["+key+"] "+lineNum);
                        }
                    } else
                    {
                        System.err.println("Not Sure what to do with ["+key+"] ["+line+"]"+lineNum);    
                    }
                }
            }
            
            //System.out.println(key);
            //keyNamesList.add(key);
        }
        return einx;
    }
    
    /**
     * 
     */
    protected boolean grep(final String term)
    {
        File dir = new File(".");
        try
        {
            Collection<?> files = FileUtils.listFiles(dir, new String[] {"java", "xml"  }, true);
            for (Object obj : files)
            {
                String contents = FileUtils.readFileToString((File)obj);
                if (contents.indexOf(term) > -1)
                {
                    return true;
                }
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    protected void collectResources()
    {
        String[] reskeys = {"getResourceString(", "getLocalizedMessage(", "createI18NLabel("};
        char[]   termChar = {')', ','};
        
        Vector<String> keyNamesList = new Vector<String>();
        
        String[] filesToSkip = {"PrefsToolbar",};
        Hashtable<String, Boolean> skipNameHash = new Hashtable<String, Boolean>();
        for (String nm : filesToSkip)
        {
            skipNameHash.put(nm, true);
        }
        
        File dir = new File("src");
        try
        {
            Collection<?> files = FileUtils.listFiles(dir, new String[] {"java"}, true);
            for (Object obj : files)
            {
                File file = (File)obj;
                
                //System.out.println(file.getAbsolutePath());
                if (file.getAbsolutePath().indexOf("/tools/") > -1)
                {
                    continue;
                }
                
                if (skipNameHash.get(FilenameUtils.getBaseName(file.getName())) != null)
                {
                    continue;
                }

                boolean firstTime = true;
                
                String         packName = getPackageName(file);
                PackageTracker pt       = packageHash.get(packName);
                if (pt == null)
                {
                    pt = new PackageTracker(packName);
                    packageHash.put(packName, pt);
                }
                
                FileTracker ft = pt.getFileHash().get(file);
                if (ft == null)
                {
                    ft = new FileTracker(file);
                    pt.getFileHash().put(file, ft);
                }
                
                int lineNum = 1;
                List<?> lines = FileUtils.readLines(file);
                for (String line : (List<String>)lines)
                {
                    //System.out.println(lineNum);
                    int  inx     = -1;
                    int  len     = 0;
                    char endChar = ' ';
                    // Search the line of code for one of the resource keys
                    for (int i=0;i<reskeys.length;i++)
                    {
                        inx = line.indexOf(reskeys[i]);
                        if (inx > -1)
                        {
                            len     = reskeys[i].length();
                            endChar = termChar[i];
                            break;
                        }
                    }
                    
                    
                    while (inx > -1)
                    {
                        keyNamesList.clear();
                        int einx = parseForNames(line, inx + len, endChar, keyNamesList, (List<String>)lines, lineNum);
                        if (einx == -1)
                        {
                            break;
                        }
                        
                        if (keyNamesList.size() > 0 && firstTime)
                        {
                            System.out.println(file.getAbsolutePath());
                            firstTime = false;
                        }
                        
                        for (String nm : keyNamesList)
                        {
                            if (nameHash.get(nm) == null)
                            {
                                nameHash.put(nm, true);
                                ft.getMapping().put(nm, nm);
                            } else
                            {
                                //log.warn("["+nm+"] name was found.");
                            }
                        }
                        
                        for (int i=0;i<reskeys.length;i++)
                        {
                            inx = line.indexOf(reskeys[i], einx);
                            if (inx > -1)
                            {
                                len     = reskeys[i].length();
                                endChar = termChar[i];
                                break;
                            }
                        }
                    } // while
                    lineNum++;
                }
            }
            
            File resFile = new File("src/resources_"+currLocale.getLanguage()+".properties");
            List<?> lines = FileUtils.readLines(resFile);
            for (String line : (List<String>)lines)
            {
                if (!line.startsWith("#"))
                {
                    int inx = line.indexOf("=");
                    if (inx > -1)
                    {
                        String[] toks = StringUtils.split(line, "=");
                        resHash.put(toks[0], toks[1]);
                    }
                }
            }
            
            System.out.println("nameHash ("+nameHash.size()+") resHash ("+resHash.size()+")");
            System.out.println("In Resource not in Source Code :");
            int cnt = 0;
            for (String key : resHash.keySet())
            {
                if (nameHash.get(key) == null)
                {
                    if (!grep(key))
                    {
                        System.out.println(key);
                        cnt++;
                    }
                }
            }
            System.out.println("Missing: "+cnt);
            System.out.println("In Source not in Resource:");
            for (String key : nameHash.keySet())
            {
                if (resHash.get(key) == null)
                {
                    System.out.println(key);
                }
            }
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    protected String getPackageName(final File f)
    {
        String name = f.getAbsolutePath();
        int    sinx = name.indexOf("src/") + 4;
        int    einx = StringUtils.lastIndexOf(name, "/");
        name = name.substring(sinx, einx);
        name = StringUtils.replaceChars(name, "/", ".");
        return name;
    }
    
    /**
     * 
     */
    protected void createResourceFiles()
    {

    }
    
    class PackageTracker 
    {
        protected String packageName;
        protected Hashtable<File, FileTracker> fileHash = new Hashtable<File, FileTracker>();
        /**
         * @param packageName
         */
        public PackageTracker(String packageName)
        {
            super();
            this.packageName = packageName;
        }
        /**
         * @return the packageName
         */
        public String getPackageName()
        {
            return packageName;
        }
        /**
         * @return the fileHash
         */
        public Hashtable<File, FileTracker> getFileHash()
        {
            return fileHash;
        }
        
        
    }

    class FileTracker 
    {
        protected File file;
        protected Hashtable<String, String> mapping = new Hashtable<String, String>();
        /**
         * @param file
         */
        public FileTracker(File file)
        {
            super();
            this.file = file;
        }
        /**
         * @return the file
         */
        public File getFile()
        {
            return file;
        }
        /**
         * @return the mapping
         */
        public Hashtable<String, String> getMapping()
        {
            return mapping;
        }
    }

    public class MacOSAppHandler extends Application
    {
        protected WeakReference<LocalizerApp> app;

        public MacOSAppHandler(final LocalizerApp app)
        {
            this.app = new WeakReference<LocalizerApp>(app);

            addApplicationListener(new AppHandler());

            setEnabledPreferencesMenu(false);
        }

        class AppHandler extends ApplicationAdapter
        {
            public void handleAbout(ApplicationEvent event)
            {
                app.get().doAbout();
                event.setHandled(true);
            }

            public void handleAppPrefsMgr(ApplicationEvent event)
            {
                event.setHandled(true);
            }
            
            public void handlePreferences(ApplicationEvent event) 
            {
                event.setHandled(true);
            }

            public void handleQuit(ApplicationEvent event)
            {
                //app.get().shutdown();
                event.setHandled(false);  // This is so bizarre that this needs to be set to false
                                          // It seems to work backwards compared to the other calls
             }
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
                LocalizerApp fd = new LocalizerApp();
                
                fd.collectResources();
                
                //fd.createDisplay();
                //UIHelper.centerAndShow(fd);
            }
        });

    }

}
