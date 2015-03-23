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
package edu.ku.brc.specify.utilapps.morphbank;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.subpane.images.CollectionDataFetcher;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Feb 16, 2013
 *
 */
public class BaseFileNameParser implements FileNameParserIFace
{
    private static final Pattern pattern = Pattern.compile("(?<=\\d)(?=\\p{L})");

    protected DBTableInfo tblInfo;
    protected DBFieldInfo fldInfo;
    protected Class<?>    attchJoinClass;
    protected String      fieldName;
    
    protected PreparedStatement pStmt = null;
    
    // Transient
    //private String fieldNameValue = null;
    
    /**
     * @param tableClass
     * @param tableJoinClass
     * @param fieldName
     */
    public BaseFileNameParser(final Class<?> attchOwnerClass,
                              final Class<?> tableJoinClass, 
                              final String fieldName)
    {
        super();
        
        this.tblInfo = DBTableIdMgr.getInstance().getByClassName(attchOwnerClass.getName());
        if (this.tblInfo == null)
        {
            throw new RuntimeException("No DBTableInfo object for "+attchOwnerClass.getName());
        }
        
        this.attchJoinClass = CollectionDataFetcher.getAttachmentClassMap().get(attchOwnerClass);
        if (this.attchJoinClass == null)
        {
            throw new RuntimeException("No attachment class for "+tblInfo.getTitle());
        }
        
        this.fieldName = fieldName;
        this.fldInfo   = this.tblInfo.getFieldByName(fieldName);
        if (this.attchJoinClass == null)
        {
            throw new RuntimeException("No field info class for "+fieldName+" in class "+tblInfo.getTitle());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return tblInfo.getTableId();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getAttachmentOwnerClass()
     */
    @Override
    public Class<?> getAttachmentOwnerClass()
    {
        return tblInfo != null ? tblInfo.getClassObj() : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getAttachmentJoinClass()
     */
    @Override
    public Class<?> getAttachmentJoinClass()
    {
        return this.attchJoinClass;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getFieldName()
     */
    @Override
    public String getFieldName()
    {
        return this.fieldName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getName()
     */
    @Override
    public String getTitle()
    {
        return String.format("%s / %s", tblInfo.getTitle(), fldInfo.getTitle());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getFieldTitle()
     */
    @Override
    public String getFieldTitle()
    {
        return fldInfo.getTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getFormatter()
     */
    @Override
    public UIFieldFormatterIFace getFormatter()
    {
        return fldInfo.getFormatter();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getRecordIds(java.lang.String)
     */
    @Override
    public List<Integer> getRecordIds(String fileName)
    {
        throw new RuntimeException("Method not implemented.");
    }
    
    /**
     * @param fieldValue
     * @return
     */
    private String getTrimmedFileName(final String fieldNameValue)
    {
        UIFieldFormatterIFace fmtr = fldInfo.getFormatter();
        if (fmtr != null)
        {
            if (fmtr.isNumeric())
            {
                String[] tokens = pattern.split(fieldNameValue);
                if (tokens.length == 0 || !StringUtils.isNumeric(tokens[0])) return null;
                
                return (String)fmtr.formatFromUI(tokens[0]);
            }
            
            if (fieldNameValue.length() > fmtr.getLength())
            {
                String baseValue = fieldNameValue.substring(0, fmtr.getLength());
                if (fmtr.isValid(baseValue))
                {
                    return baseValue;
                }
                return null;
            }
        }
        return fieldNameValue;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#isNameValid(java.lang.String)
     */
    @Override
    public boolean isNameValid(final String fileName, boolean getBaseName)
    {
        UIFieldFormatterIFace fmtr = fldInfo.getFormatter();
        
        String fieldNameValue = fileName;
        if (fmtr != null)
        {
            fieldNameValue = getTrimmedFileName(getBaseName ? FilenameUtils.getBaseName(fileName) : fileName);
            if (!fmtr.isValid(fieldNameValue))
            {
                fieldNameValue = null;
                return false;
            }
            return true;
        }
        
        if (fieldNameValue.length() > fldInfo.getLength())
        {
            fieldNameValue = fieldNameValue.substring(0, fldInfo.getLength());
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getRow(edu.ku.brc.specify.datamodel.Workbench, java.lang.String)
     */
    @Override
    public WorkbenchRow getRow(final Workbench workBench, final String fileName)
    {
        if (workBench == null || fileName == null) throw new RuntimeException("WorkBench or filename is null.");
        
        for (WorkbenchTemplateMappingItem wbmti : workBench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
        {
            if (wbmti.getFieldInfo() == fldInfo)
            {
                int index = workBench.getColumnIndex(tblInfo.getTableId(), this.fieldName);
                for (WorkbenchRow row : workBench.getWorkbenchRows())
                {
                    String keyStr = row.getData(index);
                    if (StringUtils.isNotEmpty(keyStr) && keyStr.equals(fileName))
                    {
                        return row;
                    }
                }
                break;
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getRecordId(java.lang.String)
     */
    @Override
    public Integer getRecordId(final String baseName)
    {
        Integer recId = null;
        if (pStmt == null)
        {
            String whereStr = QueryAdjusterForDomain.getInstance().getSpecialColumns(this.tblInfo, false);
            String sql      = String.format("SELECT %s FROM %s WHERE BINARY %s=? %s ", 
                                            tblInfo.getIdColumnName(),
                                            tblInfo.getName(),
                                            StringUtils.capitalize(this.fieldName),
                                            whereStr != null ? (" AND " + whereStr) : ""); 
            try
            {
                System.out.println(sql);
                pStmt = DBConnection.getInstance().getConnection().prepareStatement(sql);
            } catch (SQLException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        
        if (pStmt != null)
        {
            try
            {
                //String baseName       = FilenameUtils.getBaseName(fileName);
                String fieldNameValue = fldInfo.getFormatter() == null ? baseName : getTrimmedFileName(baseName);
                pStmt.setString(1, fieldNameValue);
                ResultSet rs = pStmt.executeQuery();
                if (rs.next())
                {
                    recId = rs.getInt(1);
                }
                rs.close();
                
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return recId;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getTitle();
    }
    
    /**
     * Cleans up any transient database data members created for performance.
     */
    public void cleanup()
    {
        if (pStmt != null)
        {
            try
            {
                pStmt.close();
                pStmt = null;
            } catch (Exception ex)
            {
                // no report
            }
        }
    }
}
