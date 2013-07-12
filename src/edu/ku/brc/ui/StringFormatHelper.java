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
 package edu.ku.brc.ui;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

/**
 * Class that enables nulls to be passed into String.format, it pre-processes and removes the nulls
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 7, 2011
 *
 */
public class StringFormatHelper
{
    private static final HashMap<String, StringFormatHelper> hashMap        = new HashMap<String, StringFormatHelper>();
    private static final HashMap<Integer, Object[]>          valueArrayHash = new HashMap<Integer, Object[]>();
    private static final ArrayList<Object>                   valueArrayList = new ArrayList<Object>();
    
    private boolean       isInError = false;
    private String[]      formats;
    //private Class<?>[]    clazzes;
    private StringBuilder sb   = new StringBuilder();
    
    /**
     * @param name
     */
    public static StringFormatHelper getStringFormatHelper(final String formatStr, final boolean displayError)
    {
        StringFormatHelper sfh = hashMap.get(formatStr);
        if (sfh == null)
        {
            sfh = new StringFormatHelper();
            sfh.isInError = !sfh.createFormatter(formatStr, displayError);
            hashMap.put(formatStr, sfh);
        }
        return sfh.isInError ? null : sfh;
    }
    
    /**
     * 
     */
    private StringFormatHelper()
    {
        
    }
    
    /**
     * @return the isInError
     */
    public boolean isInError()
    {
        return isInError;
    }

    public boolean createFormatter(final String formatStr, 
                                   final boolean displayError)
    {
        String[] fmts = StringUtils.splitPreserveAllTokens(formatStr, '%');
        if (fmts != null && fmts.length > 0)
        {
            ArrayList<String> fmtList = new ArrayList<String>();
            for (String str : fmts)
            {
                if (StringUtils.isNotEmpty(str))
                {
                    fmtList.add("%" + str);
                }
            }
            formats = new String[fmtList.size()];
            fmtList.toArray(formats);
            return true;
        }
        return false;
    }
    
    
    /**
     * @param args
     * @return
     */
    public synchronized String format(final Object...args)
    {
        sb.setLength(0);
        valueArrayList.clear();
        
        if (args.length == formats.length)
        {
            for (int i=0;i<formats.length;i++)
            {
                if (args[i] != null)
                {
                    sb.append(formats[i]);
                    valueArrayList.add(args[i]);
                }
            }
            
            Object[] vals = valueArrayHash.get(valueArrayList.size());
            if (vals == null)
            {
                vals = new Object[valueArrayList.size()];
                valueArrayHash.put(valueArrayList.size(), vals);
            }
            
            valueArrayList.toArray(vals);
            valueArrayList.clear();
            return String.format(sb.toString(), vals);
        }
        return null;
    }
}