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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 29, 2012
 *
 */
public class LuceneHelperBase
{
    private static final Logger  log = Logger.getLogger(LuceneHelperBase.class);
    
    protected File            FILE_INDEX_DIR;
    
    protected IndexReader   reader;
    protected IndexSearcher searcher;
    protected Analyzer      analyzer;
    
    protected IndexWriter   writer;
    protected QueryParser   parser;

    /**
     * 
     */
    public LuceneHelperBase()
    {
        super();
    }

    /**
     * @param doDeleteIndex
     */
    public void initLuceneForIndexing(final boolean doDeleteIndex)
    {
        try
        {
            if (doDeleteIndex && FILE_INDEX_DIR.exists())
            {
                FileUtils.deleteDirectory(FILE_INDEX_DIR);
            }
            
            if (!FILE_INDEX_DIR.mkdirs())
            {
                // error
            }
            
            analyzer = new StandardAnalyzer(Version.LUCENE_47, CharArraySet.EMPTY_SET);
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
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
     * 
     */
    public void initLuceneforReading(final String fieldName)
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
        Set<?>          stdStopWords = StandardAnalyzer.STOP_WORDS_SET;
        HashSet<Object> stopWords    = new HashSet<Object>(stdStopWords); 
        stopWords.remove("will");
        
        /*for (Object o : stopWords)
        {
            System.out.print(o.toString()+' ');
        }
        System.out.println();*/
        
        searcher = new IndexSearcher(reader);
        analyzer = new StandardAnalyzer(Version.LUCENE_47, CharArraySet.EMPTY_SET);
        parser   = new QueryParser(Version.LUCENE_47, fieldName, analyzer);
    }

}
