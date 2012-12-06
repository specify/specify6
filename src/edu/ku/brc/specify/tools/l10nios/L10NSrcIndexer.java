/* Copyright (C) 2012, University of Kansas Center for Research
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

import static edu.ku.brc.ui.UIRegistry.getAppDataDir;
import static org.apache.commons.lang.StringUtils.countMatches;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.dbsupport.cleanuptools.FirstLastVerifier;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Dec 4, 2012
 *
 */
public class L10NSrcIndexer
{
    private static final Logger  log = Logger.getLogger(L10NSrcIndexer.class);
    
    protected File            FILE_INDEX_DIR;
    
    protected IndexReader   reader;
    protected IndexSearcher searcher;
    protected Analyzer      analyzer;
    
    protected IndexWriter   writer;
    protected QueryParser   parser;
    
    protected HashSet<String> hashSet = new HashSet<String>();
    protected File            rootDir;
    
    
    /**
     * 
     */
    public L10NSrcIndexer(final File rootDir)
    {
        super();
        this.rootDir = rootDir;
        
        FILE_INDEX_DIR = new File("/Users/rods/Documents/l10n-index");//getAppDataDir() + File.separator + "l10n-index");
    }
    
    /**
     * @param rootDir
     */
    public void startIndexing()
    {
        
//        initLuceneForIndexing(true);
//        
//        try
//        {
//            indexDocs(writer, rootDir);
//            
//            writer.close();
//        } catch (CorruptIndexException e)
//        {
//            e.printStackTrace();
//        } catch (IOException e)
//        {
//            e.printStackTrace();
//        }
        
        try
        {
            indexDocs(null, rootDir);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    public void startSearching()
    {
        //initLuceneforReading();
    }

    /**
     * @param doDeleteIndex
     */
    private void initLuceneForIndexing(final boolean doDeleteIndex)
    {
        try
        {
            if (doDeleteIndex && FILE_INDEX_DIR.exists())
            {
                FileUtils.deleteDirectory(FILE_INDEX_DIR);
            }
            
            if (!FILE_INDEX_DIR.mkdirs())
            {
                System.err.println("Unable to create root");
                return;
            }
            
            analyzer = new StandardAnalyzer(Version.LUCENE_36, new HashSet<Object>());
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            writer = new IndexWriter(FSDirectory.open(FILE_INDEX_DIR), config);
            
            log.debug("Indexing to directory '" + FILE_INDEX_DIR + "'...");
            
        } catch (CorruptIndexException e)
        {
            e.printStackTrace();
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @param writer
     * @param file
     * @throws IOException
     */
    void indexDocs(final IndexWriter writer, File file) throws IOException
    {
        String fileName = file.getName();
        
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
            } else if (fileName.endsWith(".m"))
            {
                System.out.println("Parsing "+fileName);
                indexSrcFile(writer, file);
                
            } else if (fileName.endsWith(".storyboard"))
            {
                //indexSrcFile(writer, file);
            }
        }
    }

    /**
     * 
     */
    public boolean indexSrcFile(final IndexWriter writer, final File file)
    {
                           //  123456789012345678
        final String token = "NSLocalizedString(";

        boolean isOK = true;
        try
        {
            String contents = FileUtils.readFileToString(file);
            int prvInx = 0;
            do 
            {
                int inx = contents.indexOf(token, prvInx);
                if (inx == -1) 
                {
                    isOK = false;
                    break;
                }
                inx += 17;
                
                inx = contents.indexOf('"', inx);
                if (inx == -1) 
                {
                    isOK = false;
                    break;
                }
                
                inx++;
                int eInx = contents.indexOf('"', inx);
                if (eInx == -1) 
                {
                    isOK = false;
                    break;
                }
                String key = contents.substring(inx, eInx);
                hashSet.add(key);
                
                /*try
                {
                    Document doc = new Document();
                    doc.add(new Field("s",  key,  Field.Store.NO, Field.Index.ANALYZED));
                    System.out.println(key);
                    
                    writer.addDocument(doc);
                    
                } catch (CorruptIndexException e)
                {
                    isOK = false;
                    e.printStackTrace();
                } catch (IOException e)
                {
                    isOK = false;
                    e.printStackTrace();
                }*/
                
                prvInx = eInx;
                
            } while (true);
            
        } catch (IOException ex)
        {
            isOK = false;
            ex.printStackTrace();
        }
        return isOK;
    }
    
    /**
     * @return
     */
    public int getNumKeys()
    {
        return hashSet.size();
    }

    /**
     * @return
     */
    public int countSrcFiles()
    {
        return cntDocs(rootDir);
    }

    /**
     * @param writer
     * @param file
     * @throws IOException
     */
    private int cntDocs(final File file)
    {
        int total = 0;
        String fileName = file.getName();
        
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
                        total += cntDocs(new File(file, files[i]));
                    }
                }
            } else if (fileName.endsWith(".m") || fileName.endsWith(".storyboard"))
            {
                total++;
            }
        }
        return total;
    }

    /**
     * 
     */
    private void initLuceneforReading()
    {
        try
        {
            reader = IndexReader.open(FSDirectory.open(FILE_INDEX_DIR));
            
        } catch (CorruptIndexException e)
        {
            e.printStackTrace();
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        searcher = new IndexSearcher(reader);
        analyzer = new StandardAnalyzer(Version.LUCENE_36, new HashSet<Object>());
        parser   = new QueryParser(Version.LUCENE_36, "s", analyzer);
    }
    
    /**
     * @param text
     * @return
     */
    public boolean searchFor(final String text)
    {
//        try
//        {
//            Query query = parser.parse("s:"+text);
//            log.debug("Searching for: " + query.toString());
//            
//            Document             doc         = null;
//            int                  hitsPerPage = 10;
//            TopScoreDocCollector collector   = TopScoreDocCollector.create(hitsPerPage, true);
//            searcher.search(query, collector);
//            ScoreDoc[] hits = collector.topDocs().scoreDocs;
//            System.out.println("Hits: "+(hits != null ? hits.length : 0));
//            
//            for (int i=0;i<hits.length;++i) 
//            {
//                System.out.println("doc: "+i+" scrore: "+hits[i].score+"  "+searcher.doc(hits[i].doc).get("full"));
//                //if (hits[i].score > 1.0)
////                {
////                    int docId     = hits[i].doc;
////                    doc           = searcher.doc(docId);
////                }
//            }
//            return true;
//            
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        return false;
        
        return hashSet.contains(text);
    }
    
    /**
     * 
     */
    public void shutdown()
    {
        try
        {
            if (searcher != null) searcher.close();
            if (analyzer != null) analyzer.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public static void main(String[] args)
    {
        File rootDir = new File("/Users/rods/Documents/SVN/SpecifyInsightL10N");
        L10NSrcIndexer indexer = new L10NSrcIndexer(rootDir);
        indexer.startIndexing();
        System.out.println("Total Src Files: "+indexer.countSrcFiles());
        System.out.println("Total Keys:      "+indexer.getNumKeys());
        indexer.startSearching();
        boolean fnd = indexer.searchFor("Too Many Points");
        System.out.println("Fnd: "+fnd);
        indexer.shutdown();
        
    }
}
