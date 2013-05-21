/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.af.core.db;

import static edu.ku.brc.helpers.XMLHelper.xmlNode;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import edu.ku.brc.af.ui.forms.DataGetterForObj;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
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
    protected Class<?>         classObj  = null;
    protected String           fieldName = null;
    protected DataGetterForObj getter    = new DataGetterForObj();
    protected boolean          isGeneric = true;
    
    // For Errors
    protected String           errorMsg  = null;
    
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
    
    /**
     * @param properties
     */
    public AutoNumberGeneric(final String className, final String fieldName, final boolean isGeneric)
    {
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(className);
        if (tblInfo != null)
        {
            this.classObj  = tblInfo.getClassObj();
            this.fieldName = fieldName;
            this.isGeneric = isGeneric;
        } else
        {
            throw new RuntimeException("Class property ["+className+"] was not found."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AutoNumberIFace#setProperties(java.util.Properties)
     */
    public void setProperties(final Properties properties)
    {
        String className = properties.getProperty("class"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(className))
        {
            isGeneric = StringUtils.contains(className, "Generic"); //$NON-NLS-1$
            
            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(className);
            if (tblInfo != null)
            {
                classObj  = tblInfo.getClassObj();
                fieldName = properties.getProperty("field"); //$NON-NLS-1$
            } else
            {
                throw new RuntimeException("Class property ["+className+"] was not found."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else
        {
            throw new RuntimeException("Class property was null/empty."); //$NON-NLS-1$
        }
    }
    
    /**
     * @param formatter
     * @param session
     * @param value
     * @param yearPos
     * @param pos
     * @return
     * @throws Exception
     */
    protected String getHighestObject(final UIFieldFormatterIFace formatter, 
                                      final Session session, 
                                      final String  value,
                                      final Pair<Integer, Integer> yearPos, 
                                      final Pair<Integer, Integer> pos) throws Exception
    {
        if (value == null || pos == null)
        {
            errorMsg  = getLocalizedMessage("AUTONUM_INC_ERR", value != null ? value : "null", (pos != null ? pos : "null"));
            return null;
        }
        
        int yearLen = 0;
        int posLen  = 0;
        
        Integer yearVal = null;
        if (yearPos != null && StringUtils.isNotEmpty(value) && value.length() >= yearPos.second)
        {
            yearVal = extractIntegerValue(yearPos, value);
            
            yearLen = yearPos.second - yearPos.first;
        }

        //List list = session.createCriteria(classObj).addOrder( Order.desc(fieldName) ).setMaxResults(1).list();
        StringBuilder sb = new StringBuilder("SELECT "); //$NON-NLS-1$
        sb.append(fieldName);
        sb.append(" FROM "); //$NON-NLS-1$
        sb.append(classObj.getSimpleName());
        
        if (yearVal != null && yearPos != null)
        {
            sb.append(" WHERE '"); //$NON-NLS-1$
            sb.append(yearVal);
            sb.append("' = substring("+fieldName+","+(yearPos.first+1)+","+yearLen+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        sb.append(" ORDER BY"); //$NON-NLS-1$
        
        try
        {
            if (yearPos != null)
            {
                sb.append(" substring("+fieldName+","+(yearPos.first+1)+","+yearLen+") desc"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
            
            if (pos != null)
            {
                posLen = pos.second - pos.first;
                
                if (yearPos != null)
                {
                    sb.append(", "); //$NON-NLS-1$
                }
                sb.append(" substring("+fieldName+","+(pos.first+1)+","+posLen+") desc"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
            
            List<?> list = session.createQuery(sb.toString()).setMaxResults(1).list();
            if (list.size() == 1)
            {
                Object dataObj = list.get(0);
                if (dataObj != null)
                {
                    return dataObj.toString();
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AutoNumberGeneric.class, ex);
            errorMsg  = ex.toString();
        }
        return null;
    }
    
    /**
     * Returns the integer from a  portion of the string. If the string is not long enough than it return null.
     * @param pos the pos to extract
     * @param value the string value to be extracted from
     * @return the new integer value or null
     */
    protected Integer extractIntegerValue(final Pair<Integer, Integer> pos, final String value)
    {
        if (StringUtils.isNotEmpty(value))
        {
            if (pos != null && value.length() >= pos.second)
            {
                String str = value.substring(pos.first, pos.second);
                if (StringUtils.isNumeric(str))
                {
                    try
                    {
                        return Integer.parseInt(str);
                        
                    } catch (Exception ex) 
                    {
                        errorMsg = ex.toString();
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AutoNumberGeneric.class, ex);
                        //ex.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the year portion of the string. If the string is not long enough than it return null.
     * Note: We really shouldn't ever get a string that isn't the right length, but we we will make sure.
     * @param formatter the formatter describing the format
     * @param value the string value to be parsed.
     * @return null or the year
     */
    protected Integer getYearValue(final UIFieldFormatterIFace formatter, final String value)
    {
        UIFieldFormatterField  yearField = formatter.getYear();
        if (yearField != null)
        {
            return extractIntegerValue(formatter.getYearPosition(), value);
        }
        return null;
    }
    
    /**
     * Returns the increment portion as an Integer.
     * @param formatter the formatter
     * @param highestValue the value that the incrementer portion will be extracted from (this value came from the database.
     * @param formValue value from the form
     * @return the integer portion for the incrementer part
     */
    protected Pair<Integer, Integer> getYearAndIncVal(final UIFieldFormatterIFace formatter, 
                                                      final String                highestValue, 
                                                      final String                formValue)
    {
        
        UIFieldFormatterField yearField = formatter.getYear();
        boolean               isByYear  = yearField != null && yearField.isByYear();
        
        Integer valToBeInc     = null;
        Integer yearToUse      = null;
        String  strToUseForInc = highestValue;   // This is the string where the incrementer value will be extracted from

        if (isByYear)
        {
            Integer highestYear = getYearValue(formatter, highestValue);
            Integer formYear    = getYearValue(formatter, formValue);
            
            boolean calcNewValue = false;          // Caused by year changing
            
            if (highestYear != null && formYear != null)
            {
                // Since the Form Value had a good 'Year' number we will always use that
                // and only when it is greater than the database number do we calculate a new value
                yearToUse = formYear;
                
                if (formYear > highestYear)
                {
                    calcNewValue = true;
                }
                
            } else if (highestYear != null && formYear == null)
            {
                // The form value was empty or bad form some reason
                // use the database's value for year
                yearToUse = highestYear;
    
            } else if (highestYear == null && formYear != null)
            {
                // Here the database might have been empty
                // so we will use the form's value
                // and we will need to calculate a brand new increment number
                yearToUse    = formYear;
                calcNewValue = true;
                
            } else
            {
                // Here both are bad so use the current year and 
                // calculate a brand new increment number
                Calendar cal = Calendar.getInstance();
                yearToUse    = cal.get(Calendar.YEAR);
                
                // If this is a "by Year" and the year was null for both than we need to start from scratch
                if (isByYear)
                {
                    strToUseForInc = null;
                    calcNewValue   = true;
                }
            }
    
            // Now get the incrementer number portion if we aren't creating a new number
            if (!calcNewValue)
            {
                if (strToUseForInc == null)
                {
                    valToBeInc = 0;
                } else
                {
                    valToBeInc = extractIntegerValue(formatter.getIncPosition(), strToUseForInc);
                }
            } else
            {
                valToBeInc = 0;
            }
        } else
        {
            if (formatter.getYear() != null)
            {
                yearToUse = getYearValue(formatter, formValue);
            }
            
            if (StringUtils.isNotEmpty(highestValue))
            {
                valToBeInc = extractIntegerValue(formatter.getIncPosition(), strToUseForInc);
            } else
            {
                valToBeInc = 0;
            }
        }
           
        return new Pair<Integer, Integer>(yearToUse, valToBeInc);
    }

    /**
     * Builds a new string from a formatter.
     * @param formatter the formatter
     * @param value the existing largest value
     * @param yearAndIncVal a year,incVal pair
     * @return the new formatted value
     */
    protected String buildNewNumber(final UIFieldFormatterIFace  formatter, 
                                    final String                 value, 
                                    final Pair<Integer, Integer> yearAndIncVal)
    {
        String trimmedValue = StringUtils.deleteWhitespace(value);
        int    fmtLen       = formatter.getLength();
        if (trimmedValue.length() == 0 || (StringUtils.isNotEmpty(value) && formatter.isLengthOK(value.length())))
        {
            Pair<Integer, Integer> pos = formatter.getIncPosition();
            if (pos != null)
            {
                if (pos.second != null)
                {
                    int incVal = yearAndIncVal.second + 1;
                    
                    StringBuilder sb        = new StringBuilder(value.substring(0, pos.first));
                    String        formatStr = "%0" + (pos.second - pos.first) + "d"; //$NON-NLS-1$ //$NON-NLS-2$
                    sb.append(String.format(formatStr, incVal));
                    if (fmtLen > pos.second)
                    {
                        sb.append(value.substring(pos.second, fmtLen));
                    }
                    
                    UIFieldFormatterField  yearField = formatter.getYear();
                    if (yearField != null)
                    {
                        Pair<Integer, Integer> yrPos = formatter.getYearPosition();
                        if (yrPos != null)
                        {
                            sb.replace(yrPos.first, yrPos.second, Integer.toString(yearAndIncVal.first));
                        }
                    } // else
                    //throw new RuntimeException("There was an error trying to obtain the highest number, there may be a bad value in the database.");
                    return sb.toString();
                } // else
                //errorMsg = "There was an error trying to obtain the highest number, there may be a bad value in the database."; //$NON-NLS-1$
            }
            // else
            errorMsg = "Formatter ["+formatter.getName()+"] doesn't have an incrementer field."; //$NON-NLS-1$ //$NON-NLS-2$
        }   
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AutoNumberIFace#getNextNumber(edu.ku.brc.ui.forms.formatters.UIFieldFormatter, java.lang.String)
     */
    public String getNextNumber(final UIFieldFormatterIFace formatter, final String formValue)
    {
    	return getNextNumber(formatter, formValue, false);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.AutoNumberIFace#getNextNumber(edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace, java.lang.String, boolean)
     */
    public String getNextNumber(final UIFieldFormatterIFace formatter, final String formValue, final boolean incrementValue)
    {
        errorMsg = null;
            
        if (StringUtils.isNotEmpty(formValue) && formatter.isLengthOK(formValue.length()))
        {
            Session session = null;
            try
            {
                session = HibernateUtil.getNewSession();
                
                UIFieldFormatterField  yearField = formatter.getYear();
                Pair<Integer, Integer> yrPos     = yearField != null ? formatter.getYearPosition() : null;
                
                String valueToIncrement  = incrementValue ? formValue
                		: getHighestObject(formatter, session, formValue, yrPos, formatter.getIncPosition());
                
                Pair<Integer, Integer> yearAndIncVal = getYearAndIncVal(formatter, valueToIncrement, formValue);
                
                // Should NEVER be null
                if (yearAndIncVal != null)
                {
                    if (yearAndIncVal.second != null)
                    {
                        return buildNewNumber(formatter, formValue, yearAndIncVal);
                    }
                    errorMsg = getResourceString("AUTONUM_BAD_DATA_ERR");
                    
                } else
                {
                    errorMsg = "yearAndIncVal was NULL and should NEVER be!"; //$NON-NLS-1$
                }
                return null;
                
            } catch (Exception ex)
            {
                errorMsg = ex.toString();
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AutoNumberGeneric.class, ex);
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
        errorMsg = errorMsg == null ? "" : "\n";
        errorMsg += "Value ["+formValue+"] was not the proper length to be incremented."; //$NON-NLS-1$ //$NON-NLS-2$
        return null;
    }

    /**
     * @return the isGeneric
     */
    public boolean isGeneric()
    {
        return isGeneric;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace#toXML(java.lang.StringBuilder)
     */
    public void toXML(final StringBuilder sb)
    {
        sb.append("  "); //$NON-NLS-1$
        xmlNode(sb, "autonumber", getClass().getName(), false); //$NON-NLS-1$
        
        /*sb.append("    <autonumber");
        if (classObj != null)
        {
            xmlAttr(sb, "class", classObj.getName());
        }
        xmlAttr(sb, "field", fieldName);
        sb.append("/>\n");
        */
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.AutoNumberIFace#getErrorMsg()
     */
    @Override
    public String getErrorMsg()
    {
        return errorMsg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.AutoNumberIFace#isInError()
     */
    @Override
    public boolean isInError()
    {
        return errorMsg != null;
    }

}
