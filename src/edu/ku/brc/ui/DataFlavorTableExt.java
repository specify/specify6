/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.ui;

import java.awt.datatransfer.DataFlavor;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Jun 7, 2007
 *
 */
public class DataFlavorTableExt extends DataFlavor
{
    protected Vector<Integer>             tableIds = null;
    protected Hashtable<Integer, Boolean> hash     = null;
    
    /**
     * 
     */
    public DataFlavorTableExt()
    {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param mimeType
     * @throws ClassNotFoundException
     */
    public DataFlavorTableExt(String mimeType) throws ClassNotFoundException
    {
        super(mimeType);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param representationClass
     * @param humanPresentableName
     */
    public DataFlavorTableExt(Class<?> representationClass, String humanPresentableName, int[] tableids)
    {
        super(representationClass, humanPresentableName);
        addTableIds(tableids);
    }

    /**
     * @param representationClass
     * @param humanPresentableName
     */
    public DataFlavorTableExt(Class<?> representationClass, String humanPresentableName, int tableid)
    {
        super(representationClass, humanPresentableName);
        addTableId(tableid);
    }

    /**
     * @param representationClass
     * @param humanPresentableName
     */
    public DataFlavorTableExt(Class<?> representationClass, String humanPresentableName)
    {
        super(representationClass, humanPresentableName);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param mimeType
     * @param humanPresentableName
     */
    public DataFlavorTableExt(String mimeType, String humanPresentableName)
    {
        super(mimeType, humanPresentableName);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param mimeType
     * @param humanPresentableName
     * @param classLoader
     * @throws ClassNotFoundException
     */
    public DataFlavorTableExt(String mimeType, String humanPresentableName, ClassLoader classLoader)
            throws ClassNotFoundException
    {
        super(mimeType, humanPresentableName, classLoader);
        // TODO Auto-generated constructor stub
    }
    
    public void addTableIds(final int[] ids)
    {
        if (tableIds == null)
        {
            tableIds = new Vector<Integer>();
            hash     = new Hashtable<Integer, Boolean>();
        }
        for (int id : ids)
        {
            tableIds.add(id);
            hash.put(id, true);
        }
    }

    public void addTableId(final int id)
    {
        if (tableIds == null)
        {
            tableIds = new Vector<Integer>();
            hash     = new Hashtable<Integer, Boolean>();
        }
        tableIds.add(id);
        hash.put(id, true);
    }

    @Override
    public boolean equals(DataFlavor that)
    {
        if (that instanceof DataFlavorTableExt)
        {
            DataFlavorTableExt thatDF = (DataFlavorTableExt)that;
            if (that != this)
            {
                if (getHumanPresentableName().equals(that.getHumanPresentableName()))
                {
                    if (tableIds != null)
                    {
                        if (thatDF.tableIds != null)
                        {
                            /*System.out.print("SRC:");
                            for (Integer id : tableIds)
                            {
                                //System.out.print(" "+id);
                            }
                            //System.out.println();
                            //System.out.print("DST:");
                            for (Integer id : thatDF.tableIds)
                            {
                                //System.out.print(" "+id);
                            }
                            //System.out.println();*/
                            for (Integer id : thatDF.tableIds)
                            {
                                //System.out.println(hash+" "+id);
                                if (hash.get(id) != null)
                                {
                                    //System.out.println("Id Match "+ id);
                                    return true;
                                }
                            }
                        }
                    } else if (thatDF.tableIds == null)
                    {
                        //System.out.println("thatDF.tableIds == null");
                        return true;
                    }
                }
            } else
            {
                //System.out.println("same");
                return true; // same object
                
            }
            return false;
            
        } else if (tableIds == null)
        {
            //System.out.println("Here1");
            return getHumanPresentableName().equals(that.getHumanPresentableName());
        }
        //System.out.println("default "+that.getClass().getSimpleName());
        return false;
    }
    
    

}
