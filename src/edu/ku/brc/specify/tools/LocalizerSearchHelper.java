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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import edu.ku.brc.specify.conversion.ConversionLogger;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * May 12, 2010
 *
 */
public class LocalizerSearchHelper
{
    private static final Logger log = Logger.getLogger(LocalizerSearchHelper.class);
    
    protected File         srcCodeFilesDir = null;
    protected File         baseDir;
    protected FileDocument fileDoc   = new FileDocument();
    protected File         FILE_INDEX_DIR;
    
    protected IndexReader  reader;
    protected Searcher     searcher;
    protected Analyzer     analyzer;
    
    protected Directory    memIndexer = null;
    
    /**
     * 
     */
    public LocalizerSearchHelper(final File baseDir, 
                                 final String indexDirName)
    {
        this.baseDir = baseDir;
        
        FILE_INDEX_DIR = new File(this.baseDir + File.separator + indexDirName);
    }
    
    
    /**
     * @param srcCodeFilesDir the srcCodeFilesDir to set
     */
    public void setSrcCodeFilesDir(File srcCodeFilesDir)
    {
        this.srcCodeFilesDir = srcCodeFilesDir;
    }


    /**
     * 
     */
    public void initLucene(final boolean doDeleteIndex)
    {
        try
        {
            /*if (doDeleteIndex && FILE_INDEX_DIR.exists())
            {
                FileUtils.deleteDirectory(FILE_INDEX_DIR);
            }
            
            if (!FILE_INDEX_DIR.mkdirs())
            {
                // error
            }*/
            
            reader = IndexReader.open(FSDirectory.open(FILE_INDEX_DIR), true);
            
        } catch (CorruptIndexException e)
        {
            e.printStackTrace();
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        searcher = new IndexSearcher(reader);
        analyzer = new StandardAnalyzer(Version.LUCENE_36);
    }
    
    /**
     * @param baseDir
     * @return
     */
    public Vector<Pair<String, String>> findOldL10NKeys(final String[] fileNames)
    {
        initLucene(true);
        
        //if (srcCodeFilesDir == null)
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (srcCodeFilesDir != null)
            {
                chooser.setSelectedFile(new File(FilenameUtils.getName(srcCodeFilesDir.getAbsolutePath())));
                chooser.setSelectedFile(new File(srcCodeFilesDir.getParent()));
            }
            
            if (chooser.showOpenDialog(UIRegistry.getMostRecentWindow()) == JFileChooser.APPROVE_OPTION)
            {
                srcCodeFilesDir = new File(chooser.getSelectedFile().getAbsolutePath());
            } else
            {
                return null;
            }
        }
        
        indexSourceFiles();
        
        Vector<Pair<String, String>> fullNotFoundList = new Vector<Pair<String, String>>();

        
        try
        {
            ConversionLogger convLogger = new ConversionLogger();
            convLogger.initialize("resources", "Resources");
            
            for (String fileName : fileNames)
            {
                Vector<Pair<String, String>> notFoundList = new Vector<Pair<String, String>>();
                
                Vector<String> terms = new Vector<String>();
                
                String propFileName = baseDir.getAbsolutePath() + "/" + fileName;
                
                File resFile = new File(propFileName + ".properties");
                if (resFile.exists())
                {
                    List<?> lines   = FileUtils.readLines(resFile);
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
                } else
                {
                    System.err.println("Doesn't exist: "+resFile.getAbsolutePath());
                }
                
                String field = "contents";
                QueryParser parser = new QueryParser(Version.LUCENE_36, field, analyzer);
                   
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
                        
                        if (hits == 0 && !term.endsWith("_desc"))
                        {
                            notFoundList.add(new Pair<String, String>(term, subTerm));
                            
                            log.debug("'" + term + "' was not found " + (subTerm != null ? ("SubTerm["+subTerm+"]") : ""));
                        }
                        
                    } catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                }
                
                String fullName = propFileName+".html";
                TableWriter tblWriter = convLogger.getWriter(FilenameUtils.getName(fullName), propFileName);
                tblWriter.startTable();
                tblWriter.logHdr("Id", "Full Key", "Sub Key");
                int cnt = 1;
                for (Pair<String, String> pair : notFoundList)
                {
                    tblWriter.log(Integer.toString(cnt++), pair.first, pair.second != null ? pair.second : "&nbsp;");
                }
                tblWriter.endTable();
                
                fullNotFoundList.addAll(notFoundList);
                
                if (notFoundList.size() > 0 && resFile.exists())
                {
                    List<String>   lines      = (List<String>)FileUtils.readLines(resFile);
                    Vector<String> linesCache = new Vector<String>();
                    
                    for (Pair<String, String> p : notFoundList)
                    {
                        linesCache.clear();
                        linesCache.addAll(lines);
                        
                        int lineInx = 0;
                        for (String line : linesCache)
                        {
                            if (!line.startsWith("#"))
                            {
                                int inx = line.indexOf("=");
                                if (inx > -1)
                                {
                                    String[] toks = StringUtils.split(line, "=");
                                    if (toks.length > 1)
                                    {
                                        if (toks[0].equals(p.first))
                                        {
                                            lines.remove(lineInx);
                                            break;
                                        }
                                    }
                                }
                            }
                            lineInx++;
                        }
                    }
                    FileUtils.writeLines(resFile, linesCache);
                }
                
            }
            convLogger.closeAll();
            
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LocalizerSearchHelper.class, ex);
            ex.printStackTrace();
        } 
        
        return fullNotFoundList;
    }
    
    /**
     * 
     */
    protected void indexSourceFiles()
    {
        if (FILE_INDEX_DIR.exists())
        {
            try
            {
                FileUtils.deleteDirectory(FILE_INDEX_DIR);
                
            } catch (IOException e)
            {
                e.printStackTrace();
                UIRegistry.displayErrorDlg("Cannot save index to '" + FILE_INDEX_DIR + "' directory, please delete it first");
                System.exit(1);
            }
        }
        
        if (!FILE_INDEX_DIR.mkdirs())
        {
            // error
        }
        
        if (srcCodeFilesDir == null || !srcCodeFilesDir.exists())
        {
            UIRegistry.displayErrorDlg("Cannot save index to '" + srcCodeFilesDir + "' directory, please delete it first");
            System.exit(1);
        }

        Date start = new Date();
        try
        {
            IndexWriter writer = new IndexWriter(FSDirectory.open(FILE_INDEX_DIR), new StandardAnalyzer(Version.LUCENE_36), true, IndexWriter.MaxFieldLength.LIMITED);
            log.debug("Indexing to directory '" + FILE_INDEX_DIR + "'...");
            indexDocs(writer, srcCodeFilesDir);
            log.debug("Optimizing...");
            writer.optimize();
            writer.close();

            Date end = new Date();
            log.debug(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e)
        {
            log.debug(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }
    
    /**
     * @param keys
     * @param texts
     */
    public void indexProps(final Vector<StrLocaleEntry> entries)
    {
        if (analyzer == null)
        {
            analyzer = new StandardAnalyzer(Version.LUCENE_36);
        }
        
        memIndexer = new RAMDirectory();
        try
        {
            IndexWriter w = new IndexWriter(memIndexer, analyzer, true,  IndexWriter.MaxFieldLength.UNLIMITED);
            int i = 0;
            for (StrLocaleEntry entry : entries)
            {
                if (entry.isValue())
                {
                    Document doc = new Document();
                    doc.add(new Field("key",   entry.getKey(),  Field.Store.NO, Field.Index.ANALYZED));
                    doc.add(new Field("src",   entry.getSrcStr(), Field.Store.NO, Field.Index.ANALYZED));
                    doc.add(new Field("dst",   entry.getDstStr() != null ? entry.getDstStr() : "", Field.Store.NO, Field.Index.ANALYZED));
                    doc.add(new Field("index", Integer.toString(i),  Field.Store.YES, Field.Index.NOT_ANALYZED));
                    w.addDocument(doc);
                }
                i++;
            }
            w.close();
            
        } catch (CorruptIndexException e)
        {
            e.printStackTrace();
            
        } catch (LockObtainFailedException e)
        {
            e.printStackTrace();
            
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public int[] doPropsSearch(final String searchText, final String fieldName)
    {
        try
        {
            QueryParser parser = new QueryParser(Version.LUCENE_36, fieldName, analyzer);
            Query       query  = parser.parse(searchText.toLowerCase());
            //System.out.println("Searching for: " + query.toString(fieldName));
            
            IndexSearcher memSearcher = new IndexSearcher(memIndexer, true);
            TopScoreDocCollector collector = TopScoreDocCollector.create(50000, true);
            memSearcher.search(query, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            int[] inxs = new int[hits.length];
            int i = 0;
            for (ScoreDoc doc : hits)
            {
                Document d = memSearcher.doc(doc.doc);
                //System.out.println(doc.doc+"  "+doc.score+"  "+d.get("index"));
                inxs[i++] = Integer.parseInt(d.get("index"));
            }
            return inxs;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * 
     */
    public void doInteractiveSearch(final String searchText)
    {
        try
        {
            String field = "contents";

            int     repeat      = 0;
            //boolean raw         = false;
            //boolean paging      = true;
            //int     hitsPerPage = 10;

            QueryParser parser = new QueryParser(Version.LUCENE_36, field, analyzer);
            if (StringUtils.isEmpty(searchText))
                return;

            Query query = parser.parse(searchText);
            log.debug("Searching for: " + query.toString(field));

            if (repeat > 0)
            { // repeat & time as benchmark
                Date start = new Date();
                for (int i = 0; i < repeat; i++)
                {
                    searcher.search(query, null, 100);
                }
                Date end = new Date();
                log.debug("Time: " + (end.getTime() - start.getTime()) + "ms");
            }

            doStreamingSearch(searcher, query);
            
            reader.close();

        } catch (CorruptIndexException e)
        {
            e.printStackTrace();
            
        } catch (IOException e)
        {    
            //FileDocument fileDoc = new FileDocument();
            e.printStackTrace();
        } // only searching, so read-only=true
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }
    
    protected int getTotalHits(final Query query, int hitsPerPage) throws IOException
    {
        TopScoreDocCollector collector = TopScoreDocCollector.create(5 * hitsPerPage, false);
        searcher.search(query, collector);
        @SuppressWarnings("unused")
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        int numTotalHits = collector.getTotalHits();
        //log.debug(numTotalHits + " total matching documents");
        return numTotalHits;
    }
    
    /**
     * This method uses a custom HitCollector implementation which simply prints out
     * the docId and score of every matching document. 
     * 
     *  This simulates the streaming search use case, where all hits are supposed to
     *  be processed, regardless of their relevance.
     */
    public void doStreamingSearch(final Searcher searcher, final Query query) throws IOException
    {
        Collector streamingHitCollector = new Collector()
        {
            private Scorer scorer;
            private int    docBase;

            // simply print docId and score of every matching document
            @Override
            public void collect(int doc) throws IOException
            {
                log.debug("doc=" + doc + docBase + " score=" + scorer.score());
            }

            @Override
            public boolean acceptsDocsOutOfOrder()
            {
                return true;
            }

            @Override
            public void setNextReader(final IndexReader reader, final int docBase) throws IOException
            {
                this.docBase = docBase;
            }

            @Override
            public void setScorer(final Scorer scorer) throws IOException
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
    void indexDocs(final IndexWriter writer, final File file) throws IOException
    {
        
        String fileName = file.getName();
        log.debug("Parsing "+fileName);
        
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
                log.debug("adding " + file);
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
    
   
    //---------------------------------------------------------------------------------------------
    //
    //---------------------------------------------------------------------------------------------
    public class FileDocument
    {
        /**
         * Makes a document for a File.
         * <p>
         * The document has three fields:
         * <ul>
         * <li><code>path</code>--containing the pathname of the file, as a stored, untokenized
         * field;
         * <li><code>modified</code>--containing the last modified date of the file as a field as
         * created by <a href="lucene.document.DateTools.html">DateTools</a>; and
         * <li><code>contents</code>--containing the full contents of the file, as a Reader field;
         */
        public Document loadDocument(File f) throws java.io.FileNotFoundException
        {

            // make a new, empty document
            Document doc = new Document();

            // Add the path of the file as a field named "path". Use a field that is
            // indexed (i.e. searchable), but don't tokenize the field into words.
            doc.add(new Field("path", f.getPath(), Field.Store.YES, Field.Index.NOT_ANALYZED));

            // Add the last modified date of the file a field named "modified". Use
            // a field that is indexed (i.e. searchable), but don't tokenize the field
            // into words.
            doc.add(new Field("modified", DateTools.timeToString(f.lastModified(),
                    DateTools.Resolution.MINUTE), Field.Store.YES, Field.Index.NOT_ANALYZED));

            // Add the contents of the file to a field named "contents". Specify a Reader,
            // so that the text of the file is tokenized and indexed, but not stored.
            // Note that FileReader expects the file to be in the system's default encoding.
            // If that's not the case searching for special characters will fail.
            doc.add(new Field("contents", new FileReader(f)));

            // return the document
            return doc;
        }

        private FileDocument()
        {
        }
    }
}
