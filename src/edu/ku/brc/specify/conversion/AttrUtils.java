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
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import edu.ku.brc.specify.datamodel.AttrsDef;
import edu.ku.brc.specify.datamodel.AttrsSettableGettable;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.PrepAttrs;
import edu.ku.brc.specify.datamodel.PrepTypes;
import edu.ku.brc.specify.datamodel.Taxon;
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
     * @param id  xxxx
     * @param name xxxx
     * @return xxxx
     */
    public static PrepTypes loadPrepType(final int id, final String name)
    {
        try 
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            

            PrepTypes prepType = new PrepTypes(id+1);
            prepType.setName(name);
            prepType.setPreparation(new HashSet());
            
            session.save(prepType);
            
            HibernateUtil.commitTransaction();
            //HibernateUtil.closeSession();
            
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
     * @param discipline xxxx
     * @param subType xxxx
     * @param attrNames xxxx
     * @param dataTypes xxxx
     * @return xxxx
     */
    public static List<AttrsDef> loadAttrDefs(final CollectionObjDef colObjDef,
                                              final AttrsSettableGettable.TableType tableType, 
                                              final int      subType, 
                                              final String[] attrNames, 
                                              final short[]  dataTypes)
    {
        if (attrNames.length == dataTypes.length)
        {
            List<AttrsDef> list = new ArrayList<AttrsDef>();
            try
            {
                Session session = HibernateUtil.getCurrentSession();

                for (int i = 0; i < attrNames.length; i++)
                {
                    HibernateUtil.beginTransaction();
                    AttrsDef attrsDef = new AttrsDef();
                    attrsDef.setTableType((short)tableType.getType());
                    attrsDef.setCollectionObjDefID(colObjDef);
                    attrsDef.setSubType((short)subType);
                    attrsDef.setFieldName(attrNames[i]);
                    attrsDef.setDataType(dataTypes[i]);
                    
                    colObjDef.getAttrsDefs().add(attrsDef);
                    
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
    
    protected static PrepAttrs createPrepsInsert(final String  name,
                                                 final String  strValue, 
                                                 final Integer intValue, 
                                                 final AttrsSettableGettable.FieldType   fieldType, 
                                                 final Short   unit, 
                                                 final String  remarks,
                                                 final Taxon   parasiteTaxonName)
    {
        try 
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            
            PrepAttrs prepAttr = new PrepAttrs();
            prepAttr.setName(name);
            prepAttr.setFieldType((short)fieldType.getType());
            prepAttr.setIntValue(intValue);
            prepAttr.setPreparation(null);
            prepAttr.setRemarks(remarks);
            prepAttr.setStrValue(strValue);
            prepAttr.setTaxon(parasiteTaxonName);
            prepAttr.setTimestampCreated(new Date());
            prepAttr.setTimestampModified(new Date());
            prepAttr.setUnit(unit);
            
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
