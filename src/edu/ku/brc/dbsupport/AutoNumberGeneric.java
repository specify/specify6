/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.dbsupport;

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import edu.ku.brc.ui.forms.DataGetterForObj;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jun 20, 2007
 *
 */
public class AutoNumberGeneric implements AutoNumberIFace
{
    protected Class            classObj  = null;
    protected String           fieldName = null;
    protected DataGetterForObj getter    = new DataGetterForObj();
    
    /**
     * 
     */
    public AutoNumberGeneric()
    {
        // no op
    }
    
    /**
     * @param properties
     */
    public AutoNumberGeneric(final Properties properties)
    {
        String className = properties.getProperty("class");
        if (StringUtils.isNotEmpty(className))
        {
            DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(className);
            if (tblInfo != null)
            {
                classObj  = tblInfo.getClassObj();
                fieldName = properties.getProperty("field");
            } else
            {
                throw new RuntimeException("Class property ["+className+"] was not found.");
            }
        } else
        {
            throw new RuntimeException("Class property was null/empty.");
        }
        
    }
    
    /**
     * @param formatter
     * @return
     */
    protected String getFirstValue(@SuppressWarnings("unused")final UIFieldFormatterIFace formatter)
    {
        return null;
    }
    
    /**
     * @param session
     * @return
     * @throws Exception
     */
    protected Object getHighestObject(final Session session) throws Exception
    {
        List list = session.createCriteria(classObj).addOrder( Order.desc(fieldName) ).setMaxResults(1).list();
        if (list.size() == 1)
        {
            return list.get(0);
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AutoNumberIFace#getNextNumber(edu.ku.brc.ui.forms.formatters.UIFieldFormatter, java.lang.String)
     */
    public String getNextNumber(final UIFieldFormatterIFace formatter, final String value)
    {
        Session session = null;
        try
        {
            session = HibernateUtil.getNewSession();
            
            Object dataObj = getHighestObject(session);
            if (dataObj == null)
            {
                return buildNewNumber(formatter, value, 1);
            }
            
            String largestVal = (String)getter.getFieldValue(dataObj, fieldName);
            int    incr       =  getIncValue(formatter, largestVal);
            if (incr > -1)
            {
                incr++;
                return buildNewNumber(formatter, value, incr);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return null;
    }
    
    /**
     * Returns the increment portion as an Integer.
     * @param formatter the formatter
     * @param value the value that the incrementer portion will be extracted from
     * @return the integer portion for the incrementer part
     */
    public int getIncValue(final UIFieldFormatterIFace formatter, final String value)
    {
        if (StringUtils.isNotEmpty(value) && value.length() == formatter.getLength())
        {
            Pair<Integer, Integer> pos = formatter.getIncPosition();
            if (pos != null)
            {
                String fieldStr = value.substring(pos.first, pos.second);
                if (StringUtils.isNumeric(fieldStr))
                {
                    return Integer.parseInt(fieldStr);
                }
            } else
            {
                throw new RuntimeException("Formatter ["+formatter.getName()+"] doesn't have an incrementer field.");
            }
        }
        return -1;
    }

    /**
     * Builds a new string from a formatter.
     * @param formatter the formatter
     * @param value the existing largest value
     * @param incVal the inc value that will be substituted in
     * @return the new formatted value
     */
    public String buildNewNumber(final UIFieldFormatterIFace formatter, final String value, final int incVal)
    {
        if (StringUtils.isNotEmpty(value) && value.length() == formatter.getLength())
        {
            Pair<Integer, Integer> pos = formatter.getIncPosition();
            if (pos != null)
            {
                StringBuilder sb        = new StringBuilder(value.substring(0, pos.first));
                String        formatStr = "%0" + (pos.second - pos.first) + "d";
                sb.append(String.format(formatStr, incVal));
                if (formatter.getLength() > pos.second)
                {
                    sb.append(value.substring(pos.second, formatter.getLength()));
                }
                return sb.toString();
                
            }
            // else
            throw new RuntimeException("Formatter ["+formatter.getName()+"] doesn't have an incrementer field.");
        }
        return null;
    }

    
}
