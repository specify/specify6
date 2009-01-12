/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Gets lists of valid values for predefined system coded fields like Agent.agentType, DeterminationStatus.type.
 * 
 * 
 */
public class RecordTypeCodeBuilder
{
    protected static final Logger log = Logger.getLogger(RecordTypeCodeBuilder.class);
    
    /**
     *  Default constructor
     */
    public RecordTypeCodeBuilder()
    {
        //nothing to do.
    }
    
    /**
     * @param tblClass
     * @return a List of predefined system pick lists for coded fields such as Agent.agentType,
     * ReferenceWork.referenceWorkType, DeterminationStatus.type
     * 
     * Tables that contain such fields should implement a public static method named getSpSystemTypeCodes 
     * with no arguments that returns List<PickListDBAdapterIFace>.
     * 
     * AND a public static method named getSpSystemTypeCodeFlds with no arguments that returns String[]
     */
    @SuppressWarnings("unchecked")
    public static List<PickListDBAdapterIFace> getTypeCodes(Class<?> tblClass)
    {
        try
        {
            Method codeGetter = tblClass.getMethod("getSpSystemTypeCodes", (Class<?>[] )null);
            //just to try to make sure codeGetter is really the method we want:
            if (Modifier.isStatic(codeGetter.getModifiers()) && codeGetter.getReturnType().equals(java.util.List.class))
            {
                log.debug("building TypeCodes for " + tblClass.getName());
                try
                {
                    return (List<PickListDBAdapterIFace> )codeGetter.invoke(null, (Object[] )null);
                }
                catch (InvocationTargetException ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RecordTypeCodeBuilder.class, ex);
                    log.error(ex);
                    return null;
                }
                catch (IllegalAccessException ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RecordTypeCodeBuilder.class, ex);
                    log.error(ex);
                    return null;
                }
            }
        }
        catch (NoSuchMethodException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RecordTypeCodeBuilder.class, ex);
            //ignore it and move on.
        }
        log.debug("returning null TypeCodes for " + tblClass.getName());
        return null;
    }
    
    /**
     * @param fi
     * @return a predefined system pick list for fi if one is defined for it.
     */
    public static PickListDBAdapterIFace getTypeCode(final DBFieldInfo fi)
    {
        if (isTypeCodeField(fi))
        {
            List<PickListDBAdapterIFace> picks = RecordTypeCodeBuilder.getTypeCodes(fi.getTableInfo().getClassObj());
            if (picks != null)
            {
                for (int p = 0; p < picks.size(); p++)
                {
                    if (((TypeCode) picks.get(p)).getFldName().equalsIgnoreCase(fi.getName()))
                    {
                        return picks.get(p);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * @param field
     * @return true if field has a predefined system picklist.
     */
    @SuppressWarnings("unchecked")
    public static boolean isTypeCodeField(final DBFieldInfo field)
    {
        Class<?> tblClass = field.getTableInfo().getClassObj();
        try
        {
            Method codeGetter = tblClass.getMethod("getSpSystemTypeCodeFlds", (Class<?>[] )null);
            //just to try to make sure codeGetter is really the method we want:
            if (Modifier.isStatic(codeGetter.getModifiers()) && codeGetter.getReturnType().equals(String[].class))
            {
                log.debug("retrieving TypeCode Fields for " + tblClass.getName());
                try
                {
                    String[] flds = (String[] )codeGetter.invoke(null, (Object[] )null);
                    for (int f = 0; f < flds.length; f++)
                    {
                        if (flds[f].equals(field.getName()))
                        {
                            return true;
                        }
                    }
                    return false;
                }
                catch (InvocationTargetException ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RecordTypeCodeBuilder.class, ex);
                    log.error(ex);
                    return false;
                }
                catch (IllegalAccessException ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RecordTypeCodeBuilder.class, ex);
                    log.error(ex);
                    return false;
                }
             }
        }
        catch (NoSuchMethodException ex)
        {
            //ignore it and move on.
        }
        return false;
    }
}
