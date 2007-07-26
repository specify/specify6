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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import edu.ku.brc.ui.forms.DataGetterForObj;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterField;
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
        setProperties(properties);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AutoNumberIFace#setProperties(java.util.Properties)
     */
    public void setProperties(final Properties properties)
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
     * @param session
     * @return
     * @throws Exception
     */
    protected Object getHighestObject(final Session session, 
                                      final Pair<Integer, Integer> year, 
                                      final Pair<Integer, Integer> pos) throws Exception
    {
        //List list = session.createCriteria(classObj).addOrder( Order.desc(fieldName) ).setMaxResults(1).list();
        StringBuilder sb = new StringBuilder(" FROM "+classObj.getSimpleName()+" ORDER BY");
        try
        {
            if (year != null)
            {
                sb.append(" substring("+fieldName+","+(year.first+1)+","+year.second+") desc");
                
            }
            
            if (pos != null)
            {
                if (year != null)
                {
                    sb.append(", ");
                }
                sb.append(" substring("+fieldName+","+(pos.first+1)+","+pos.second+") desc");
            }

            System.out.println(sb.toString());
            List list = session.createQuery(sb.toString()).setMaxResults(1).list();
            if (list.size() == 1)
            {
                return list.get(0);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
    protected String getHighestValue(final Pair<Integer, Integer> year, final Pair<Integer, Integer> pos)
    {
        // XXX MYSQL SPECIFIC!
        
        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append(fieldName);
        sb.append(" FROM ");
        sb.append(classObj.getSimpleName().toLowerCase());
        sb.append(" ORDER BY ");
        
        if (year != null)
        {
            sb.append(" substring("+fieldName+","+year.first+","+year.second+") desc");
            
        }
        
        if (pos != null)
        {
            if (year != null)
            {
                sb.append(", ");
            }
            sb.append(" substring("+fieldName+","+pos.first+","+pos.second+") desc");
        }
        
        System.out.println(sb.toString());
        
        Connection conn = null;
        Statement stmt  = null;
        ResultSet rs  = null;
        try
        {
            conn = DBConnection.getInstance().createConnection();
            stmt = conn.createStatement();
            
            rs = stmt.executeQuery(sb.toString());
            if (rs.first())
            {
                return rs.getString(1);
            }
            
        } catch(Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                if (conn != null)
                {
                    conn.close();
                }
               
            } catch (SQLException sqlex)
            {
                sqlex.printStackTrace();
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AutoNumberIFace#getNextNumber(edu.ku.brc.ui.forms.formatters.UIFieldFormatter, java.lang.String)
     */
    public String getNextNumber(final UIFieldFormatterIFace formatter, final String value)
    {
        if (true)
        {
            Session session = null;
            try
            {
                session = HibernateUtil.getNewSession();
                
                Object dataObj = getHighestObject(session, formatter.getYearPosition(), formatter.getIncPosition());
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
        } else
        {
            String highestVal = getHighestValue(formatter.getYearPosition(), formatter.getIncPosition());
            if (StringUtils.isEmpty(highestVal))
            {
                return buildNewNumber(formatter, value, 1);
            }
            int incr =  getIncValue(formatter, highestVal);
            if (incr > -1)
            {
                incr++;
                return buildNewNumber(formatter, value, incr);
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
        UIFieldFormatterField  yearField   = formatter.getYear();
        Pair<Integer, Integer> yrPos       = null;         
        if (yearField != null && yearField.isByYear())
        {
            yrPos = formatter.getYearPosition();
            if (yrPos != null)
            {
                Calendar cal   = Calendar.getInstance();
                int      calYr = cal.get(Calendar.YEAR);
                String   yrStr = value.substring(yrPos.first, yrPos.second);
                int      year  = Integer.parseInt(yrStr);
                if (year != calYr)
                {
                    return 0;
                }
            }
        }
        
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
                throw new RuntimeException("Largest Value in Database  ["+value+"] was not numeric");
            }
            throw new RuntimeException("Formatter ["+formatter.getName()+"] doesn't have an incrementer field.");
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
            int                    currentYear = -1;
            UIFieldFormatterField  yearField   = formatter.getYear();
            Pair<Integer, Integer> yrPos       = null;         
            if (yearField != null && yearField.isByYear())
            {
                yrPos = formatter.getYearPosition();
                if (yrPos != null)
                {
                    Calendar cal   = Calendar.getInstance();
                    int      calYr = cal.get(Calendar.YEAR);
                    String   yrStr = value.substring(yrPos.first, yrPos.second);
                    int      year  = Integer.parseInt(yrStr);
                    if (year != calYr)
                    {
                        currentYear = calYr;
                    } else
                    {
                        //currentYear = year;
                    }
                }
            }
            
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
                if (currentYear != -1 && yrPos != null)
                {
                    sb.replace(yrPos.first, yrPos.second, Integer.toString(currentYear));
                }
                return sb.toString();
                
            }
            // else
            throw new RuntimeException("Formatter ["+formatter.getName()+"] doesn't have an incrementer field.");
        }
        return null;
    }

    
}
