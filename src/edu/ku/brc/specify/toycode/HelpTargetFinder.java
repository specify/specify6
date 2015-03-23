/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.toycode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.dom4j.Element;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.ConversionLogger;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 3, 2007
 *
 */
public class HelpTargetFinder 
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
    public HelpTargetFinder()
    {
        
    }
    
 
    
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
    
    /**
     * @param query
     * @param hitsPerPage
     * @return
     * @throws IOException
     */
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
     * 
     */
    public void processProperties()
    {
        initLucene();
        
        try
        {
            ArrayList<String> terms = new ArrayList<String>();
            
            
            HashSet<String> tocTargets = new HashSet<String>();
            Element root = XMLHelper.readFileToDOM4J(new File("help/SpecifyHelpTOC.xml"));
            if (root != null)
            {
                String path = "/toc/tocitem";
                while (true)
                {
                    List<?> mapIds = root.selectNodes(path);
                    if (mapIds.size() == 0) break;
                    
                    for (Object obj : mapIds)
                    {
                        Element mapElement = (Element) obj;
                        String target = XMLHelper.getAttr(mapElement, "target", null);
                        if (target != null)
                        {
                            tocTargets.add(target);
                            System.out.println(target);
                        }
                    }
                    path += "/tocitem";
                }
            } else
            {
                System.err.println("Couldn't open DOM for uiformatters.xml");
            }
            
            root = XMLHelper.readFileToDOM4J(new File("help/SpecifyHelp.jhm"));
            if (root != null)
            {
                List<?> mapIds = root.selectNodes("/map/mapID");
                for (Object obj : mapIds)
                {
                    Element mapElement = (Element) obj;
                    String target = XMLHelper.getAttr(mapElement, "target", null);
                    if (target != null && !tocTargets.contains(target))
                    {
                        terms.add(target);
                    }
                }
            } else
            {
                System.err.println("Couldn't open DOM for uiformatters.xml");
            }
                
            Vector<Pair<String, String>> notFoundList = new Vector<Pair<String, String>>();
            String field = "contents";
            QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, field, analyzer);
               
            int unFoundCnt = 0;
            for (String term : terms)
            {
                Query query;
                try
                {
                    query = parser.parse(term);
                    
                    String subTerm = null;
                    int    hits    = getTotalHits(query, 10);
                    if (hits == 0)
                    {
                        notFoundList.add(new Pair<String, String>(term, subTerm));
                        
                        System.out.println("'" + term + "' was not found " + (subTerm != null ? ("SubTerm["+subTerm+"]") : ""));
                        unFoundCnt++;
                    }
                    
                } catch (ParseException e)
                {
                    e.printStackTrace();
                }
            }
            
            System.out.println("Unfound Count: "+unFoundCnt+" / "+terms.size());
            
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
            
        } catch (Exception ex)
        {
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
        if (!SRC_DIR.exists())
        {
            System.out.println("Source directory doesn't exist '" + SRC_DIR);
            System.exit(1);
        }

        Date start = new Date();
        try
        {
            IndexWriter writer = new IndexWriter(FSDirectory.open(INDEX_DIR), new StandardAnalyzer(Version.LUCENE_CURRENT), true, IndexWriter.MaxFieldLength.LIMITED);
            System.out.println("Indexing to directory '" + INDEX_DIR + "'...");
            indexDocs(writer, SRC_DIR);
            indexDocs(writer, new File("config"));
            //indexDocs(writer, new File("help"));
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
     * @param writer
     * @param file
     * @throws IOException
     */
    void indexDocs(final IndexWriter writer, final File file) throws IOException
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
            } else if (fileName.endsWith(".java") || fileName.endsWith(".xml") || fileName.endsWith(".html"))
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
        HelpTargetFinder fd = new HelpTargetFinder();
        //fd.indexSourceFiles();
        fd.processProperties();
        System.out.println("App Done.");
    }
    
}
