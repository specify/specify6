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
package edu.ku.brc.specify.toycode.mexconabio;

import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import edu.ku.brc.specify.toycode.mexconabio.FieldDef.DataType;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Feb 3, 2010
 *
 */
public class FMPCreateTable extends DefaultHandler
{
    
    public static final String[] twoByteSyms  = {"√º", "√≥", "√©", "√§", "√°", "√≠", "√∂", "√∏", "√Ö", "√™", "√±", "√ß", "√∫", "√Å"};
    //public static final String[] syms         = {"º",  "≥",  "©",  "§",  "°",  "≠",  "∂",  "∏",  "Ö",  "™",  "±",  "ß",  "∫",  "Å"};
    
    public static final String[] chars        = {"ü",  "ó",  "é",  "ä",  "á",  "í",  "ö",  "ø",  "Å",  "ê",  "ñ",  "ç",  "ú",  "Á"};
    public static final String[] ascii        = {"u",  "o",  "e",  "a",  "a",  "i",  "o",  "o",  "A",  "e",  "n",  "c",  "u",  "A"};

    
    protected XMLReader        xmlReader;
    protected StringBuilder    buffer = new StringBuilder();
    protected Vector<FieldDef> fields = new Vector<FieldDef>();
       
    protected int              rowCnt = 0;
    protected int              rowNum = 0;
    
    protected boolean          debug  = false;
    
    protected String           fieldListString = null;
    protected String           prepareStmtStr  = null;
    
    protected String           tableName = null;
    protected String           keyField  = null;
    protected boolean          doAddKey  = false;
    
    /**
     * 
     */
    public FMPCreateTable(final String    tableName, 
                            final String  keyField,
                            final boolean doAddKey)
    {
        super();
        
        this.tableName = tableName;
        this.keyField  = keyField;
        this.doAddKey  = doAddKey;
    }


    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes attrs)
    {
        buffer.setLength(0);
        
        if (localName.equals("FIELD"))
        {
            FieldDef fldDef = new FieldDef();
            fields.add(fldDef);
            
            for (int i=0;i<attrs.getLength();i++)
            {
                String attr  = attrs.getLocalName(i);
                String value = attrs.getValue(i);
                if (attr.equals("EMPTYOK"))
                {
                    fldDef.setNullable(value.equals("YES"));
                    
                } else if (attr.equals("NAME"))
                {
                    value = StringUtils.capitalize(value.trim());
                    value = StringUtils.deleteWhitespace(value);
                    value = StringUtils.replace(value, "_", "");
                    
                    if ((value.charAt(0) >= '0' && value.charAt(0) <= '9') || 
                            value.equalsIgnoreCase("New") || 
                            value.equalsIgnoreCase("Group"))
                    {
                        value = "Fld" + value;
                    }
                    
                    String fixedStr = convertFromTwoByteUTF8(value);
                    fixedStr = StringUtils.replace(fixedStr, ".", "");
                    fixedStr = StringUtils.replace(fixedStr, ":", "");
                    fixedStr = StringUtils.replace(fixedStr, "/", "_");
                    fldDef.setName(fixedStr);
                    fldDef.setOrigName(value);
                    
                } else if (attr.equals("TYPE"))
                {
                    if (value.equals("TEXT"))
                    {
                        fldDef.setType(DataType.eText);
                        
                    } else if (value.equals("NUMBER"))
                    {
                        fldDef.setType(DataType.eNumber);
                        
                    } else if (value.equals("DATE"))
                    {
                        fldDef.setType(DataType.eDate);
                        
                    } else if (value.equals("TIME"))
                    {
                        fldDef.setType(DataType.eTime);
                    } else
                    {
                        System.err.println("Unknown Type["+value+"]");
                    }
                }
            }
        }
    }
    
    /**
     * @param str
     * @return
     */
    public static String convertFromTwoByteUTF8(final String str)
    { 
        String s = str;
        //while (s.indexOf('√') > -1)
        {
            for (int ii=0;ii<twoByteSyms.length;ii++)
            {
                s = StringUtils.replace(s, twoByteSyms[ii], chars[ii]);
            }
        }
        return s;
    }    
    /**
     * @param str
     * @return
     */
    public static String convertToAcsii(final String str)
    { 
        String s = str;
        for (int ii=0;ii<chars.length;ii++)
        {
            s = StringUtils.replace(s, chars[ii], ascii[ii]);
        }
        return s;
    }
    
    
    /**
     * @param nameArg
     * @return
     */
    public static String fixName(final String nameArg)
    {
        String        name = nameArg.toLowerCase();
        StringBuilder nm   = new StringBuilder();
        for (int i=0;i<name.length();i++)
        {
            char c = name.charAt(i);
            if (c != ':' && c <= 'z') 
            {
                nm.append(c);
            }
        }
        return StringUtils.replace(nm.toString().trim(), " ", "_");
    }
    

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
    {
        if (buffer != null) 
        {
          String s = new String(ch, start, length);
          buffer.append(convertFromTwoByteUTF8(s));
        }
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(final String namespaceURI, final String localName, final String qName)
    {
        buffer.setLength(0);
    }
    
    /**
     * 
     */
    public String dropTableStr()
    {
        return "DROP TABLE " + tableName;
    }
    
    /**
     * 
     */
    public String createTableStr()
    {
        return createTableStr(tableName, keyField, true);
    }
    
    /**
     * @param tblName
     * @param keyField
     */
    private String createTableStr(final String tblName, final String keyField, final boolean doForceAll)
    {
        StringBuilder selectDB = new StringBuilder();
        for (FieldDef fd : fields)
        {
            String s = convertToAcsii(fd.getName());
            if (selectDB.length() > 0) selectDB.append(", ");
            selectDB.append(s);
            fd.setName(s);
        }
        
        fieldListString = selectDB.toString();
        
        int primaryIndex = -1;
        int i            = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE "+tblName+" (");
        for (FieldDef fd : fields)
        {
            if ((fd.getName() == null || fd.getMaxSize() == 0) && !doForceAll) continue;
            
            String fieldName = fd.getName();
            if ((i == 0 && keyField == null) || (keyField != null && keyField.equals(fieldName)))
            {
                primaryIndex = i;
                if (!fieldName.endsWith("ID"))
                {
                    fieldName += "ID";
                }
                String keyName = StringUtils.deleteWhitespace(fieldName);
                sb.append(keyName);
                sb.append(" int(11) NOT NULL AUTO_INCREMENT,\n");
                
                fd.setName(fieldName);
                
            } else
            {
                sb.append(fd.getName());
                sb.append(" ");
                
                switch (fd.getType())
                {
                    case eText :
                        int sz = ((int)(fd.getMaxSize() / 8.0) + 2) * 8;
                        System.out.println(fd.getName()+"  ["+fd.getMaxSize()+"]["+sz+"]");
                        if (sz > 255)
                        {
                            //fd.setType(DataType.eMemo);
                            sb.append("text");
                        } else
                        {
                            sb.append("varchar("+sz+")");
                        }
                        
                        break;
                        
                    case eNumber :
                        sb.append(fd.isDouble() ? "double" : "int(11)");
                        break;
                        
                    case eDate :
                        sb.append("date");
                        break;
                        
                    case eTime :
                        sb.append("TIME");
                        break;
                }
                sb.append(fd.isNullable() ? " DEFAULT NULL," : ",");
                sb.append("\n");
            }
            i++;
        }
        
        sb.append("KEY `GenusIDX` (`SpeciesName`),");
        sb.append("KEY `SpeciesIDX` (`GenusName`),");
        sb.append("KEY `BarCDIDX` (`BarCD`),");
        
        sb.append(String.format("PRIMARY KEY (`%s`)\n", fields.get(primaryIndex).getName()));
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n");
        
        System.err.println(sb.toString());
        
        return sb.toString();
    }
    
    
    
    /**
     * @return the fieldListString
     */
    public String getFieldListString()
    {
        return fieldListString;
    }

    /**
     * @param doAllFields
     * @param doRemoveIdField
     * @return
     */
    public String getPrepareStmtStr(final boolean doAllFields, final boolean doRemoveIdField)
    {
        if (prepareStmtStr == null)
        {
            createPrepare(doAllFields, doRemoveIdField);
        }
        return prepareStmtStr;
    }

    /**
     * @param doAllFields
     */
    private void createPrepare(final boolean doAllFields, final boolean doRemoveIdField)
    {
        if (doRemoveIdField && doAddKey)
        {
            fields.remove(0);
        }
        
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(tableName);
        sb.append(" (");
        
        int i = 0;
        for (FieldDef fd : fields)
        {
            if (doAllFields || fd.getMaxSize() > 0)
            {
                if (i > 0) sb.append(", ");
                sb.append(fd.getName());
                i++;
            }
        }
        
        sb.append(") VALUES(");
       
        i = 0;
        for (FieldDef fd : fields)
        {
            if (fd.getMaxSize() > 0 || doAllFields)
            {
                if (i > 0) sb.append(", ");
                sb.append('?');
                i++;
            }
        }
        sb.append(")");
        
        prepareStmtStr = sb.toString();
    }
     
    /**
     * <p>Handles any non-fatal errors generated by incorrect user input.</p>
     */
    public void error(SAXParseException exception)
    {
        System.err.println("line " + exception.getLineNumber() + ": col. "
                + exception.getColumnNumber() + ": " + exception.getMessage());
    }

    /**
     * <p>Handles any fatal errors generated by incorrect user input.</p>
     */
    public void fatalError(SAXParseException exception)
    {
        System.err.println("line " + exception.getLineNumber() + ": col. "
                + exception.getColumnNumber() + ": " + exception.getMessage());
    }
    
    /**
     * @return the fields
     */
    public Vector<FieldDef> getFields()
    {
        return fields;
    }


    /**
     * 
     */
    public void process(final String xmlFileName)
    {
        rowCnt = 0;
        if (doAddKey)
        {
            fields.add(new FieldDef("ID", "ID", DataType.eNumber, false, false));
        }
        
        try
        {
            FileReader fr = new FileReader(new File(xmlFileName));
            xmlReader = (XMLReader)new org.apache.xerces.parsers.SAXParser();
            xmlReader.setContentHandler(this);
            xmlReader.setErrorHandler(this);
            
            InputSource is = new InputSource(fr);
            is.setEncoding("UTF-8");
            xmlReader.parse(is);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
