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
package edu.ku.brc.specify.utilapps.morphbank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.tasks.subpane.images.CollectionDataFetcher;

/**
 * Given a class it produces a Filename parser using a standardized field name for that class. 
 * For example, CollectionObject uses the CatalogNumber field.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Feb 16, 2013
 *
 */
public class FileNameParserFactory
{
    private static final Class<?>[] classes = {CollectionObject.class, 
                                               CollectionObject.class, 
                                               CollectionObject.class, 
                                               CollectingEvent.class,
                                               CollectingEvent.class,
                                               Taxon.class};
    
    private static final String[]   fields  = {"catalogNumber", 
                                               "fieldNumber",
                                               "altCatalogNumber",
                                               "stationFieldNumber",
                                               "guid",
                                               "fullName"};
    
    private static final FileNameParserFactory instance = new FileNameParserFactory();
    
    private final HashMap<FileNameParserIFace, Integer> indexMap   = new HashMap<FileNameParserIFace, Integer>();
    private final ArrayList<FileNameParserIFace>        parserList = new ArrayList<FileNameParserIFace>();
    
    /**
     * 
     */
    public FileNameParserFactory()
    {
        super();
    }

    /**
     * @return the instance
     */
    public static FileNameParserFactory getInstance()
    {
        return instance;
    }

    /**
     * @return list of available parsers
     */
    public List<FileNameParserIFace> getList()
    {
        indexMap.clear();
        parserList.clear();
        
        Collection coll = AppContextMgr.getInstance().getClassObject(Collection.class);
        boolean isEmbedded = coll.getIsEmbeddedCollectingEvent();
        
        int i      = 0;
        int fldInx = 0;
        for (Class<?> cls : classes)
        {
            if (cls != CollectingEvent.class || !isEmbedded)
            {
                Class<?>           joinCls = CollectionDataFetcher.getAttachmentClassMap().get(cls);
                BaseFileNameParser parser  = new BaseFileNameParser(cls, joinCls, fields[fldInx]);
                if (parser != null)
                {
                    indexMap.put(parser, i);
                    parserList.add(parser);
                }
                i++;
            }
            fldInx++;
        }

        return new ArrayList<FileNameParserIFace>(parserList);
    }
}
