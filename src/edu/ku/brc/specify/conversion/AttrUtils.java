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
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.getStrValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.AttributeDef;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationAttr;

/**
 * Class to assist in creating the various different types of attributes (Habitat, Biological, Preparation)
 * 
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class AttrUtils
{
    protected static final Logger log = Logger.getLogger(BasicSQLUtils.class);
    
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
    public static PrepType loadPrepType(final String name, final Collection collection)
    {
        try 
        {
            Session session = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
 
            PrepType prepType = new PrepType();
            prepType.initialize();
            prepType.setName(name);
            
            collection.addReference(prepType, "prepTypes");
            session.save(prepType);
            session.saveOrUpdate(collection);
            
            HibernateUtil.commitTransaction();
            
            return prepType;
            
        } catch (Exception e) 
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AttrUtils.class, e);
            log.warn("******* " + e);
            HibernateUtil.rollbackTransaction();
        } 
        return null;
    }
    
    /**
     * @param discipline xxxx
     * @param tableType xxxx
     * @param prepType xxxx
     * @param attrNames xxxx
     * @param dataTypes xxxx
     * @return xxxx
     */
    public static List<AttributeDef> loadAttrDefs(final Discipline discipline,
                                              final GenericDBConversion.TableType tableType, 
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
                    attrsDef.setTableType(tableType.getType());
                    attrsDef.setDiscipline(discipline);
                    attrsDef.setPrepType(prepType);
                    attrsDef.setFieldName(attrNames[i]);
                    attrsDef.setDataType(dataTypes[i]);
                    
                    discipline.getAttributeDefs().add(attrsDef);
                    
                    session.save(attrsDef);

                    list.add(attrsDef);
                }
                session.saveOrUpdate(discipline);
                HibernateUtil.commitTransaction();

                //HibernateUtil.closeSession();
                
                return list;

            } catch (Exception e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AttrUtils.class, e);
                log.warn("******* " + e);
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
            Timestamp now = new Timestamp(System.currentTimeMillis());
            prepAttr.setTimestampCreated(now);
            
            session.save(prepAttr);
            
            HibernateUtil.commitTransaction();
            //HibernateUtil.closeSession();
            
            return prepAttr;
            
        } catch (Exception e) 
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AttrUtils.class, e);
            log.warn("******* " + e);
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
        StringBuilder strBuf = new StringBuilder(128);
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
