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
package edu.ku.brc.specify.tools.schemalocale;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

import edu.ku.brc.specify.conversion.ConversionLogger;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

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
    //private static final Logger log = Logger.getLogger(LocalizerApp.class);
    
    protected FileDocument fileDoc   = new FileDocument();
    protected File         INDEX_DIR = new File("index");
    
    protected IndexReader  reader;
    protected Searcher     searcher;
    protected Analyzer     analyzer;
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
    
 /*
    protected void printLocales(final PrintWriter pw,
                                final LocalizableItemIFace parent, 
                                final LocalizableItemIFace lndi, 
                                final String lang, final String country)
    {
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
        
    }*/
    
    private void initLucene()
    {
        try
        {
            reader = IndexReader.open(FSDirectory.open(INDEX_DIR), true);
            
        } catch (CorruptIndexException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        searcher = new IndexSearcher(reader);
        analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
    }
    
    public void processProperties()
    {
        initLucene();
        
        try
        {
            Locale currLocale = Locale.getDefault();
            
            Vector<String> terms = new Vector<String>();
            
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
                        if (toks.length > 1)
                        {
                            terms.add(toks[0]);
                        }
                    }
                }
            }
            
            Vector<Pair<String, String>> notFoundList = new Vector<Pair<String, String>>();
            String field = "contents";
            QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, field, analyzer);
               
            for (String term : terms)
            {
                Query query;
                try
                {
                    if (term.equals("AND") || term.equals("OR")) continue;
                    
                    query = parser.parse(term);
                    
                    String subTerm = null;
                    int    hits    = getTotalHits(query, 10);
                    if (hits == 0)
                    {
                        int inx = term.indexOf('.');
                        if (inx > -1)
                        {
                            subTerm = term.substring(inx+1);
                            hits    = getTotalHits(parser.parse(subTerm), 10);
                            
                            if (hits == 0)
                            {
                                int lastInx = term.lastIndexOf('.');
                                if (lastInx > -1 && lastInx != inx)
                                {
                                    subTerm = term.substring(lastInx+1);
                                    hits    = getTotalHits(parser.parse(subTerm), 10);
                                }
                            }
                        }
                    }
                    
                    if (hits == 0)
                    {
                        notFoundList.add(new Pair<String, String>(term, subTerm));
                        
                        System.out.println("'" + term + "' was not found " + (subTerm != null ? ("SubTerm["+subTerm+"]") : ""));
                    }
                    
                } catch (ParseException e)
                {
                    e.printStackTrace();
                }
            }
            
            ConversionLogger convLogger = new ConversionLogger();
            convLogger.initialize("resources", "Resources");
            
            
            TableWriter tblWriter = convLogger.getWriter("resources.html", "Resources");
            tblWriter.startTable();
            tblWriter.logHdr("Id", "Full Key", "Sub Key");
            int cnt = 1;
            for (Pair<String, String> pair : notFoundList)
            {
                tblWriter.log(Integer.toString(cnt++), pair.first, pair.second != null ? pair.second : "&nbsp;");
            }
            tblWriter.endTable();
            convLogger.closeAll();
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LocalizerApp.class, ex);
            ex.printStackTrace();
        }   
    }
    
    /**
     * 
     */
    protected void indexSourceFiles()
    {
        if (INDEX_DIR.exists())
        {
            System.out.println("Cannot save index to '" + INDEX_DIR + "' directory, please delete it first");
            System.exit(1);
        }
        File SRC_DIR = new File("src");
        if (INDEX_DIR.exists())
        {
            System.out.println("Cannot save index to '" + INDEX_DIR + "' directory, please delete it first");
            System.exit(1);
        }

        Date start = new Date();
        try
        {
            IndexWriter writer = new IndexWriter(FSDirectory.open(INDEX_DIR), new StandardAnalyzer(Version.LUCENE_CURRENT), true, IndexWriter.MaxFieldLength.LIMITED);
            System.out.println("Indexing to directory '" + INDEX_DIR + "'...");
            indexDocs(writer, SRC_DIR);
            System.out.println("Optimizing...");
            writer.optimize();
            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e)
        {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }
    
    /**
     * 
     */
    public void doInteractiveSearch()
    {
        try
        {
            String field = "contents";
            String queries = null;

            int     repeat      = 0;
            boolean raw         = false;
            boolean paging      = true;
            int     hitsPerPage = 10;

            BufferedReader in = null;
            if (queries != null)
            {
                in = new BufferedReader(new FileReader(queries));
            } else
            {
                in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            }

            QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, field, analyzer);
            while (true)
            {
                if (queries == null) // prompt the user
                    System.out.println("Enter query: ");

                String line = in.readLine();

                if (line == null || line.length() == -1)
                    break;

                line = line.trim();
                if (line.length() == 0)
                    break;

                Query query = parser.parse(line);
                System.out.println("Searching for: " + query.toString(field));

                if (repeat > 0)
                { // repeat & time as benchmark
                    Date start = new Date();
                    for (int i = 0; i < repeat; i++)
                    {
                        searcher.search(query, null, 100);
                    }
                    Date end = new Date();
                    System.out.println("Time: " + (end.getTime() - start.getTime()) + "ms");
                }

                if (paging)
                {
                    doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null);
                } else
                {
                    doStreamingSearch(searcher, query);
                }
            }
            reader.close();

        } catch (CorruptIndexException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {    FileDocument fileDoc = new FileDocument();
            e.printStackTrace();
        } // only searching, so read-only=true
        catch (ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    protected int getTotalHits(final Query query, int hitsPerPage) throws IOException
    {
        TopScoreDocCollector collector = TopScoreDocCollector.create(5 * hitsPerPage, false);
        searcher.search(query, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        int numTotalHits = collector.getTotalHits();
        //System.out.println(numTotalHits + " total matching documents");
        return numTotalHits;
    }
    
    /**
     * This demonstrates a typical paging search scenario, where the search engine presents 
     * pages of size n to the user. The user can then go to the next page if interested in
     * the next hits.
     * 
     * When the query is executed for the first time, then only enough results are collected
     * to fill 5 result pages. If the user wants to page beyond this limit, then the query
     * is executed another time and all hits are collected.
     * 
     */
    public static void doPagingSearch(BufferedReader in,
                                      Searcher searcher,
                                      Query query,
                                      int hitsPerPage,
                                      boolean raw,
                                      boolean interactive) throws IOException
    {

        // Collect enough docs to show 5 pages
        TopScoreDocCollector collector = TopScoreDocCollector.create(5 * hitsPerPage, false);
        searcher.search(query, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        int numTotalHits = collector.getTotalHits();
        System.out.println(numTotalHits + " total matching documents");

        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);

        while (true)
        {
            if (end > hits.length)
            {
                System.out.println("Only results 1 - " + hits.length + " of " + numTotalHits
                        + " total matching documents collected.");
                System.out.println("Collect more (y/n) ?");
                String line = in.readLine();
                if (line.length() == 0 || line.charAt(0) == 'n')
                {
                    break;
                }

                collector = TopScoreDocCollector.create(numTotalHits, false);
                searcher.search(query, collector);
                hits = collector.topDocs().scoreDocs;
            }

            end = Math.min(hits.length, start + hitsPerPage);

            for (int i = start; i < end; i++)
            {
                if (raw)
                { // output raw format
                    System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);
                    continue;
                }

                Document doc = searcher.doc(hits[i].doc);
                String path = doc.get("path");
                if (path != null)
                {
                    System.out.println((i + 1) + ". " + path);
                    String title = doc.get("title");
                    if (title != null)
                    {
                        System.out.println("   Title: " + doc.get("title"));
                    }
                } else
                {
                    System.out.println((i + 1) + ". " + "No path for this document");
                }

            }

            if (!interactive)
            {
                break;
            }

            if (numTotalHits >= end)
            {
                boolean quit = false;
                while (true)
                {
                    System.out.print("Press ");
                    if (start - hitsPerPage >= 0)
                    {
                        System.out.print("(p)revious page, ");
                    }
                    if (start + hitsPerPage < numTotalHits)
                    {
                        System.out.print("(n)ext page, ");
                    }
                    System.out.println("(q)uit or enter number to jump to a page.");

                    String line = in.readLine();
                    if (line.length() == 0 || line.charAt(0) == 'q')
                    {
                        quit = true;
                        break;
                    }
                    if (line.charAt(0) == 'p')
                    {
                        start = Math.max(0, start - hitsPerPage);
                        break;
                    } else if (line.charAt(0) == 'n')
                    {
                        if (start + hitsPerPage < numTotalHits)
                        {
                            start += hitsPerPage;
                        }
                        break;
                    } else
                    {
                        int page = Integer.parseInt(line);
                        if ((page - 1) * hitsPerPage < numTotalHits)
                        {
                            start = (page - 1) * hitsPerPage;
                            break;
                        } else
                        {
                            System.out.println("No such page");
                        }
                    }
                }
                if (quit)
                    break;
                end = Math.min(numTotalHits, start + hitsPerPage);
            }

        }

    }
    
    /**
     * This method uses a custom HitCollector implementation which simply prints out
     * the docId and score of every matching document. 
     * 
     *  This simulates the streaming search use case, where all hits are supposed to
     *  be processed, regardless of their relevance.
     */
    public void doStreamingSearch(final Searcher searcher, Query query) throws IOException
    {
        Collector streamingHitCollector = new Collector()
        {
            private Scorer scorer;
            private int    docBase;

            // simply print docId and score of every matching document
            @Override
            public void collect(int doc) throws IOException
            {
                System.out.println("doc=" + doc + docBase + " score=" + scorer.score());
            }

            @Override
            public boolean acceptsDocsOutOfOrder()
            {
                return true;
            }

            @Override
            public void setNextReader(IndexReader reader, int docBase) throws IOException
            {
                this.docBase = docBase;
            }

            @Override
            public void setScorer(Scorer scorer) throws IOException
            {
                this.scorer = scorer;
            }

        };

        searcher.search(query, streamingHitCollector);
    }
    

    
    /**
     * @param writer
     * @param file
     * @throws IOException
     */
    void indexDocs(final IndexWriter writer, File file) throws IOException
    {
        
        String fileName = file.getName();
        System.out.println("Parsing "+fileName);
        
        // do not try to index files that cannot be read
        if (file.canRead())
        {
            if (file.isDirectory())
            {
                String[] files = file.list();
                // an IO error could occur
                if (files != null)
                {
                    for (int i = 0; i < files.length; i++)
                    {
                        indexDocs(writer, new File(file, files[i]));
                    }
                }
            } else if (fileName.endsWith(".java") || fileName.endsWith(".xml"))
            {
                System.out.println("adding " + file);
                try
                {
                    writer.addDocument(fileDoc.loadDocument(file));
                }
                // at least on windows, some temporary files raise this exception with an
                // "access denied" message
                // checking if the file can be read doesn't help
                catch (FileNotFoundException fnfe)
                {
                    
                }
            }
        }
    }
    
    
    /**
     * @author rods
     *
     * @code_status Alpha
     *
     * Created Date: Apr 15, 2010
     *
     */
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

    public class FileDocument {
        /** Makes a document for a File.
          <p>
          The document has three fields:
          <ul>
          <li><code>path</code>--containing the pathname of the file, as a stored,
          untokenized field;
          <li><code>modified</code>--containing the last modified date of the file as
          a field as created by <a
          href="lucene.document.DateTools.html">DateTools</a>; and
          <li><code>contents</code>--containing the full contents of the file, as a
          Reader field;
          */
        public Document loadDocument(File f) throws java.io.FileNotFoundException {
           
          // make a new, empty document
          Document doc = new Document();

          // Add the path of the file as a field named "path".  Use a field that is 
          // indexed (i.e. searchable), but don't tokenize the field into words.
          doc.add(new Field("path", f.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));

          // Add the last modified date of the file a field named "modified".  Use 
          // a field that is indexed (i.e. searchable), but don't tokenize the field
          // into words.
          doc.add(new Field("modified",
              DateTools.timeToString(f.lastModified(), DateTools.Resolution.MINUTE),
              Field.Store.YES, Field.Index.NOT_ANALYZED));

          // Add the contents of the file to a field named "contents".  Specify a Reader,
          // so that the text of the file is tokenized and indexed, but not stored.
          // Note that FileReader expects the file to be in the system's default encoding.
          // If that's not the case searching for special characters will fail.
          doc.add(new Field("contents", new FileReader(f)));

          // return the document
          return doc;
        }

        private FileDocument() {}
      }
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        if (true)
        {
            LocalizerApp fd = new LocalizerApp();
            
            //fd.indexSourceFiles();
            fd.processProperties();
            
            return;
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
                    
                } catch (MissingResourceException ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LocalizerApp.class, ex);
                    Locale.setDefault(Locale.ENGLISH);
                    UIRegistry.setResourceLocale(Locale.ENGLISH);
                }
                
                LocalizerApp fd = new LocalizerApp();
                
                fd.indexSourceFiles();
                
                //fd.createDisplay();
                //UIHelper.centerAndShow(fd);
            }
        });

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

}
