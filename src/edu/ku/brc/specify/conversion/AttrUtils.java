/*
 * Filename:    $RCSfile: AttrUtils.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.3 $
 * Date:        $Date: 2005/10/20 12:53:02 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.specify.dbsupport.BasicSQLUtils.getStrValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.AttributeIFace;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationAttr;
import edu.ku.brc.specify.dbsupport.BasicSQLUtils;
import edu.ku.brc.specify.dbsupport.HibernateUtil;

/**
 * Class to assist in creating the various different types of attributes (Habitat, Biological, Preparation)
 *  
 * @author rods
 *
 */
public class AttrUtils
{
    protected static Log log = LogFactory.getLog(BasicSQLUtils.class);
    
    /**
     * 
     */
    public AttrUtils()
    {
    }
    

    /**
     * Creates a PrepType Object and stores it into the database
     * @param name the name of the PrepType
     * @return a PrepType Object
     */
    public static PrepType loadPrepType(final String name)
    {
        try 
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
 
            PrepType prepType = new PrepType();
            prepType.setName(name);
            //prepType.setPreparation(new HashSet());
            
            session.save(prepType);
            
            HibernateUtil.commitTransaction();
            
            return prepType;
            
        } catch (Exception e) 
        {
            System.err.println("******* " + e);
            HibernateUtil.rollbackTransaction();
        } 
        return null;
    }
    
    /**
     * @param colObjDef xxxx
     * @param tableType xxxx
     * @param prepType xxxx
     * @param attrNames xxxx
     * @param dataTypes xxxx
     * @return xxxx
     */
    public static List<AttributeDef> loadAttrDefs(final CollectionObjDef colObjDef,
                                              final AttributeIFace.TableType tableType, 
                                              final PrepType prepType, 
                                              final String[] attrNames, 
                                              final short[]  dataTypes)
    {
        if (attrNames.length == dataTypes.length)
        {
            List<AttributeDef> list = new ArrayList<AttributeDef>();
            try
            {
                Session session = HibernateUtil.getCurrentSession();

                for (int i = 0; i < attrNames.length; i++)
                {
                    HibernateUtil.beginTransaction();
                    AttributeDef attrsDef = new AttributeDef();
                    attrsDef.setTableType((short)tableType.getType());
                    attrsDef.setCollectionObjDef(colObjDef);
                    attrsDef.setPrepType(prepType);
                    attrsDef.setFieldName(attrNames[i]);
                    attrsDef.setDataType(dataTypes[i]);
                    
                    colObjDef.getAttributeDefs().add(attrsDef);
                    
                    session.save(attrsDef);

                    list.add(attrsDef);
                }
                session.saveOrUpdate(colObjDef);
                HibernateUtil.commitTransaction();

                //HibernateUtil.closeSession();
                
                return list;

            } catch (Exception e)
            {
                System.err.println("******* " + e);
                e.printStackTrace();
                HibernateUtil.rollbackTransaction();
            }

        } else
        {
            log.error("Names length: " + attrNames.length + " doesn't match Types length "+ dataTypes.length);
        }
        return null;
    } 
    
    protected static PreparationAttr createPrepsInsert(final AttributeDef attrDef,
                                                       final Preparation preparation,
                                                       final String      strValue, 
                                                       final Double      dblValue)
    {
        try 
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            
            PreparationAttr prepAttr = new PreparationAttr();
            prepAttr.setDefinition(attrDef);
            prepAttr.setPreparation(preparation);
            prepAttr.setDblValue(dblValue);
            prepAttr.setPreparation(null);
            prepAttr.setStrValue(strValue);
            prepAttr.setTimestampCreated(new Date());
            prepAttr.setTimestampModified(new Date());
            
            session.save(prepAttr);
            
            HibernateUtil.commitTransaction();
            //HibernateUtil.closeSession();
            
            return prepAttr;
            
        } catch (Exception e) 
        {
            System.err.println("******* " + e);
            HibernateUtil.rollbackTransaction();
        } 
        return null;

    }
    
    protected static String createPrepsInsert(final int     prepAttrsID,
                                              final String  name,
                                              final String  strValue,
                                              final Integer intValue,
                                              final Short   fieldType,
                                              final Short   unit,
                                              final Date    timeStamp,
                                              final Date    modifiedDate,
                                              final String  remarks,
                                              final int     prepsObjID,
                                              final Integer parasiteTaxonNameID)
    {
        StringBuilder strBuf = new StringBuilder();
        strBuf.setLength(0);
        strBuf.append("INSERT INTO prepattrs VALUES (");
        strBuf.append(prepAttrsID);
        strBuf.append(',');
        strBuf.append(getStrValue(name));
        strBuf.append(',');
        strBuf.append(getStrValue(strValue));
        strBuf.append(',');
        strBuf.append(getStrValue(intValue));
        strBuf.append(',');
        strBuf.append(getStrValue(fieldType));
        strBuf.append(',');
        strBuf.append(getStrValue(unit));
        strBuf.append(',');
        strBuf.append(getStrValue(timeStamp));
        strBuf.append(',');
        strBuf.append(getStrValue(modifiedDate));
        strBuf.append(',');
        strBuf.append(getStrValue(remarks));
        strBuf.append(',');
        strBuf.append(prepsObjID);
        strBuf.append(',');
        strBuf.append(parasiteTaxonNameID);
        strBuf.append(')');

        return strBuf.toString();
    }
}
