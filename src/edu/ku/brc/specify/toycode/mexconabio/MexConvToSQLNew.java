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
package edu.ku.brc.specify.toycode.mexconabio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import com.csvreader.CsvReader;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.BasicSQLUtils.SERVERTYPE;
import edu.ku.brc.specify.tasks.subpane.wb.CSVImport;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureCSV;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataIFace;
import edu.ku.brc.specify.tasks.subpane.wb.DataImportIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jan 8, 2010
 *
 */
public class MexConvToSQLNew
{

    enum DataType {TEXT, NUMBER, DATE, TIME}
    
    protected Vector<String> toks = new Vector<String>();
    
    /**
     * 
     */
    public MexConvToSQLNew()
    {
        super();
    }

    
    
    private String fixName(final String nameArg)
    {
        String name = nameArg.toLowerCase();
        StringBuilder nm = new StringBuilder();
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
    
    /**
     * @param line
     * @return
     */
    private Vector<String> split(final String line)
    {
        toks.clear();
        
        String[] tokens = StringUtils.splitPreserveAllTokens(line, '|');
        for (String s : tokens)
        {
            toks.add(s);
        }
        
        /*if (false)
        {
            System.out.println(toks.size());
            if (toks.size() != 199)
            {
                System.out.println("Line["+line+"] is "+toks.size());
            }
        }*/
        return toks;
    }
    
    public void convert(final String tableName, final String fileName)
    {
        String str    = "";
        int    fldLen = 0;
        int    inx    = 0;
        
        Connection conn = null;
        Statement  stmt = null;
        try
        {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/mex?characterEncoding=UTF-8&autoReconnect=true", "root", "root");
            stmt = conn.createStatement();
            
            int[] fieldLengths = null;
            
            BasicSQLUtils.deleteAllRecordsFromTable(conn, tableName, SERVERTYPE.MySQL);
            Vector<Integer> types = new Vector<Integer>();
            Vector<String>  names = new Vector<String>();
            
            String selectStr  = null;
            String prepareStr = null;
            try
            {
                prepareStr = FileUtils.readFileToString(new File("prepare_stmt.txt"));
                selectStr  = FileUtils.readFileToString(new File("select.txt"));
                
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            
            int idInx = selectStr.indexOf("ID,");
            if (idInx == 0)
            {
                selectStr = selectStr.substring(3);
            }
            
            File file = new File("/Users/rods/Documents/"+fileName);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            //SimpleDateFormat stf = new SimpleDateFormat("k:mm:ss");
            
            int rowCnt = 0;
            try
            {
                System.out.println(prepareStr);
                
                PreparedStatement pStmt = conn.prepareStatement(prepareStr);
                BufferedReader    in    = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
                str = in.readLine();
                
                String[] fieldNames   = StringUtils.split(str, ",");
                //String[] fieldNamesDB = StringUtils.split(selectStr, ",");
                
                String            sql  = "SELECT "+selectStr +" FROM " + tableName;
                System.out.println(sql);
                
                ResultSet         rs   = stmt.executeQuery(sql);
                ResultSetMetaData rsmd = rs.getMetaData();
                
                fieldLengths = new int[rsmd.getColumnCount()];
                for (int i=1;i<=rsmd.getColumnCount();i++)
                {
                    fieldLengths[i-1] = rsmd.getPrecision(i);
                    types.add(rsmd.getColumnType(i));
                    names.add(rsmd.getColumnName(i));
                    System.out.println((i > 1 ? fieldNames[i-2] : "ID")+" / "+rsmd.getColumnName(i)+" - "+ rsmd.getPrecision(i));
                }
                
                int numCols = rsmd.getColumnCount();
                rs.close();
                
                System.out.println("Number of Fields: "+numCols);
                
                str = in.readLine();
                while (str != null)
                {
                    //System.err.println(str);
                    
                    str = StringUtils.replace(str.substring(1, str.length()-1), "\",\"", "|");
                    
                    Vector<String> fields = split(str);
                    if (fields.size() != numCols)
                    {
                        System.out.println("numCols: " +numCols +" != " + fields.size() +"fields.size()");
                        continue;
                    }
                    
                    int col = 1;
                    inx     = 0;
                    for (String fld : fields)
                    {
                        String field = fld.trim();
                        //if (field.length() > 1)
                        //{
                        //    field = field.substring(1, field.length()-1);
                        //}
                        //if (inx > 204) break;
                        
                        fldLen = field.length();
                        
                        pStmt.setObject(col, null);
                        
                        switch (types.get(inx))
                        {
                            case java.sql.Types.LONGVARCHAR :
                            case java.sql.Types.VARCHAR :
                            case java.sql.Types.LONGNVARCHAR : 
                            {
                                if (field.length() > 0)
                                {
                                    if (field.length() <= fieldLengths[inx])
                                    {
                                        pStmt.setString(col, field);
                                    } else
                                    {
                                        System.err.println(String.format("The data for `%s` (%d) is too big %d f[%s]", names.get(inx), fieldLengths[inx], field.length(), field));
                                        pStmt.setString(col, null);
                                    }
                                } else
                                {
                                    pStmt.setString(col, null);
                                }
                            }
                            break;
                            
                            case java.sql.Types.DOUBLE :
                            case java.sql.Types.FLOAT : {
                                if (StringUtils.isNotEmpty(field))
                                {
                                    if (StringUtils.isNumeric(field))
                                    {
                                        pStmt.setDouble(col, field.length() > 0 ? Double.parseDouble(field) : null);
                                    } else
                                    {
                                        System.err.println(col + " Bad Number["+field+"] ");
                                        pStmt.setDate(col, null);
                                    }
                                } else
                                {
                                    pStmt.setDate(col, null);
                                }
                            } break;
                            
                            case java.sql.Types.INTEGER : {
                                if (StringUtils.isNotEmpty(field))
                                {
                                    if (StringUtils.isNumeric(field))
                                    {
                                        pStmt.setInt(col, field.length() > 0 ? Integer.parseInt(field) : null);
                                    } else
                                    {
                                        System.err.println(col + " Bad Number["+field+"] ");
                                        pStmt.setDate(col, null);
                                    }
                                } else
                                {
                                    pStmt.setDate(col, null);
                                }
                            } break;
                            
                            case java.sql.Types.TIME : {
                                Time time =  null;
                                try
                                {
                                   time = Time.valueOf(field);
                                } catch (Exception ex){}
                                pStmt.setTime(col, time);
                            } break;
                            
                            case java.sql.Types.DATE : {
                                try
                                {
                                    if (StringUtils.isNotEmpty(field))
                                    {
                                        if (StringUtils.contains(field, "/"))
                                        {
                                            field = StringUtils.replace(field, "/", "-");
                                        } else if (StringUtils.contains(field, " "))
                                        {
                                            field = StringUtils.replace(field, " ", "-");
                                        }
                                        pStmt.setDate(col, field.length() > 0 ? new java.sql.Date(sdf.parse(field).getTime()) : null);
                                    } else
                                    {
                                        pStmt.setDate(col, null);
                                    }
                                } catch (Exception ex)
                                {
                                    System.err.println(col+" Bad Date["+field+"]\n"+str);
                                    pStmt.setDate(col, null);
                                }
                            } break;
                            
                            default:
                            {
                                System.err.println("Error - "+types.get(inx));
                            }
                        }
                        inx++;
                        col++;
                    }
                    pStmt.execute();
                    str = in.readLine();
                    rowCnt++;
                }
                in.close();
                
            } catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
                
            } catch (Exception e)
            {
                System.err.println("Row: "+rowCnt);
                System.err.println(str);
                System.err.println(inx+"  "+fieldLengths[inx]+" - Field Len: "+fldLen);
                e.printStackTrace();
            }
            
            /*BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            while (bis.available() > 0)
            {
                int bytesRead = bis.read(bytes);
                if (bytesRead > 0)
                {
                    System.arraycopy(bytes, bytesRead, buffer, bufEndInx, bytesRead);
                    bufEndInx += bytesRead;
                    int inx = 0;
                    while (inx < bufEndInx)
                    {
                        if (buffer[inx] != '\n')
                        {
                            String line = 
                        }
                        inx++;
                    }
                }
            }*/
            
            
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                stmt.close();
                conn.close();
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }    
    }
    
    //----------------------------------------------------------------------------------------------------------------
    // CSV
    //----------------------------------------------------------------------------------------------------------------
    public void readCSV(final File file)
    {
        ConfigureCSV config = new ConfigureCSV(file);
        if (config.getStatus() == ConfigureExternalDataIFace.Status.Valid)
        {
            getData(config);
        } 
    }
    
    /**
     * @param config
     * @return
     */
    public DataImportIFace.Status getData(final ConfigureCSV config)
    {
        CsvReader csv = null;
        try
        {
            ConfigureCSV configCSV = null;
            if (config instanceof ConfigureCSV)
            {
                configCSV = (ConfigureCSV)config;
                
                if (configCSV.getStatus() == ConfigureExternalDataIFace.Status.Valid)
                {
                    csv = new CsvReader(new FileInputStream(config.getFile()), configCSV.getDelimiter(), configCSV.getCharset());

                    csv.setEscapeMode(configCSV.getEscapeMode());
                    csv.setTextQualifier(configCSV.getTextQualifier());
        
                    if (config.getFirstRowHasHeaders())
                    {
                        csv.readHeaders();
                    } else
                    {
                        csv.setHeaders(configCSV.setupHeaders());
                    }
                    
                    //add additional dummy column headers
                    String[] newHeaders = null;
                    if (configCSV.getNumOfColsToAppend() > csv.getColumnCount())
                    {
                        newHeaders = configCSV.padColumnHeaders(configCSV.getNumOfColsToAppend(), csv.getHeaders());
                        csv.setHeaders(newHeaders);   
                    }
                    
                    convertFROMCSV(config, csv);
                }
            }
            
            return DataImportIFace.Status.Valid;
            
        } catch (IOException ex)
        {
            
            csv.close();
            
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CSVImport.class, ex);
           //log.error(ex);
        }
        
        return DataImportIFace.Status.Error;
    }
    
    /**
     * 
     */
    public void convertFROMCSV(final ConfigureCSV config, final CsvReader csv)
    {
        String str = "";
        int strLen = 0;
        int inx    = 0;
        
        CsvReader  csvObj = null;
        Connection conn   = null;
        Statement  stmt   = null;
        try
        {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/mex?characterEncoding=UTF-8&autoReconnect=true", "root", "root");
            stmt = conn.createStatement();
            
            StringBuilder pStmtStr = new StringBuilder();
            StringBuilder sb       = new StringBuilder();
            StringBuilder ques     = new StringBuilder();
            
            int[] fieldLengths = null;
            
            BasicSQLUtils.deleteAllRecordsFromTable(conn, "mex", SERVERTYPE.MySQL);
            Vector<Integer> types = new Vector<Integer>();
            Vector<String>  names = new Vector<String>();
            
            File file = new File("/Users/rods/Documents/Untitled.mer");
            Element root = XMLHelper.readFileToDOM4J(new File("/Users/rods/Documents/Acer.xml"));
            if (root != null)
            {
                fieldLengths = new int[csv.getColumnCount()];
                for (int i=0;i<fieldLengths.length;i++)
                {
                    fieldLengths[i] = 0;
                }
                
                int row = 0;
                while (csv.readRecord())
                {
                    row++;
                    for (int col = 0; col < csv.getColumnCount(); col++)
                    {
                        String dataObj = csv.get(col);
                        
                        int len = dataObj.length()+1;
                        if (len > fieldLengths[inx])
                        {
                            fieldLengths[inx] = len;
                        }
                        inx++;
                    }
                    
                    if (row % 10000 == 0) System.out.println(row);
                    
                }
                
                System.out.println("--------------");
                HashMap<String, Integer> hashSize = new HashMap<String, Integer>();
                for (int i=0;i<names.size();i++)
                {
                    hashSize.put(names.get(i), fieldLengths[i]);
                    System.out.println(names.get(i)+" -> "+fieldLengths[i]);
                }
                
                sb.append("CREATE TABLE mex (");
                List<?> items = root.selectNodes("/FIELDS/FIELD"); //$NON-NLS-1$
                System.out.println(items.size());
                
                inx = 0;
                for (Iterator<?> capIter = items.iterator(); capIter.hasNext(); )
                {
                    Element fieldNode = (Element)capIter.next();
                    String  nullOK  = fieldNode.attributeValue("EMPTYOK"); //$NON-NLS-1$
                    String  fldName = fixName(fieldNode.attributeValue("NAME").trim()); //$NON-NLS-1$
                    String  type    = fieldNode.attributeValue("TYPE"); //$NON-NLS-1$
                    
                    sb.append("`");
                    sb.append(fldName);
                    sb.append("` ");
                    
                    System.err.println("["+fldName+"]");
                    int len = hashSize.get(fldName);
                    
                    if (pStmtStr.length() > 0) pStmtStr.append(',');
                    pStmtStr.append("`"+fldName+"`");
                    
                    if (ques.length() > 0) ques.append(',');
                    ques.append("?");
                    
                    if (type.equals("TEXT"))
                    {
                        if (StringUtils.contains(fldName, "img folder"))
                        {
                            sb.append("longtext ");
                        } else
                        {
                            sb.append("VARCHAR("+len+") CHARACTER SET utf8 ");
                        }
                        types.add(DataType.TEXT.ordinal());
                        
                    } else if (type.equals("NUMBER"))
                    {
                        sb.append("DOUBLE ");
                        types.add(DataType.NUMBER.ordinal());
                        
                    } else if (type.equals("DATE"))
                    {
                        sb.append("DATE ");
                        types.add(DataType.DATE.ordinal());
                        
                    } else if (type.equals("TIME"))
                    {
                        sb.append("VARCHAR(16) ");
                        types.add(DataType.TIME.ordinal());
                        
                    } else
                    {
                        System.err.println("Unhandled Type["+type+"]");
                    }
                    
                    sb.append(nullOK.equals("YES") ? "DEFAULT NULL," : ",");
                    sb.append("\n");
                    inx++;
                }
                sb.setLength(sb.length()-2);
                
                sb.append(") ENGINE=MyISAM DEFAULT CHARSET=utf8;");
            }
            
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            
            int rowCnt = 0;
            try
            {
                String stmtStr = "INSERT INTO mex ("+pStmtStr+") VALUES("+ques+")";
                System.out.println(stmtStr);
                
                try {
                    stmt.executeUpdate("DROP TABLE mex");
                } catch (SQLException e) {}
                System.err.println(sb.toString());
                stmt.executeUpdate(sb.toString());
                
                PreparedStatement pStmt = conn.prepareStatement(stmtStr);
                
                csv.close();
                
                csvObj = new CsvReader(new FileInputStream(config.getFile()), config.getDelimiter(), config.getCharset());
                
                csvObj.readRecord(); // skip header
                int row = 0;
                while (csvObj.readRecord())
                {
                    row++;
                    for (int col = 0; col < csvObj.getColumnCount(); col++)
                    {
                        String dataStr = csvObj.get(col);
                        strLen = dataStr.length();
                        
                        switch (types.get(inx))
                        {
                            case 3 :
                            case 0 : {
                                if (strLen > 0)
                                {
                                    if (strLen <= fieldLengths[inx])
                                    {
                                        pStmt.setString(col, dataStr);
                                    } else
                                    {
                                        System.err.println(String.format("The data for `%s` (%d) is too big %d", names.get(inx), fieldLengths[inx], strLen));
                                        pStmt.setString(col, null);
                                    }
                                } else
                                {
                                    pStmt.setString(col, null);
                                }
                            }
                            break;
                            
                            case 1 : {
                                if (StringUtils.isNotEmpty(dataStr))
                                {
                                    if (StringUtils.isNumeric(dataStr))
                                    {
                                        pStmt.setDouble(col, strLen > 0 ? Double.parseDouble(dataStr) : null);
                                    } else
                                    {
                                        System.err.println(col + " Bad Number["+dataStr+"] ");
                                        pStmt.setDate(col, null);
                                    }
                                } else
                                {
                                    pStmt.setDate(col, null);
                                }
                            } break;
                            
                            case 2 : {
                                try
                                {
                                    if (StringUtils.isNotEmpty(dataStr))
                                    {
                                        if (StringUtils.contains(dataStr, "/"))
                                        {
                                            dataStr = StringUtils.replace(dataStr, "/", "-");
                                        } else if (StringUtils.contains(dataStr, " "))
                                        {
                                            dataStr = StringUtils.replace(dataStr, " ", "-");
                                        }
                                        pStmt.setDate(col, strLen > 0 ? new java.sql.Date(sdf.parse(dataStr).getTime()) : null);
                                    } else
                                    {
                                        pStmt.setDate(col, null);
                                    }
                                } catch (Exception ex)
                                {
                                    System.err.println(col+" Bad Date["+dataStr+"]\n"+str);
                                    pStmt.setDate(col, null);
                                }
                            } break;
                            
                            default:
                            {
                                System.err.println("Error - "+types.get(inx));
                            }
                        }
                        inx++;
                        col++;
                    }
                    pStmt.execute();
                    row++;
                }
                
            } catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            } catch (Exception e)
            {
                System.err.println("Row: "+rowCnt);
                System.err.println(str);
                System.err.println(inx+"  "+fieldLengths[inx]+" - Field Len: "+strLen);
                e.printStackTrace();
            } finally
            {
                if (csvObj != null)
                {
                    csvObj.close();
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                stmt.close();
                conn.close();
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }    
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        MexConvToSQLNew m = new MexConvToSQLNew();
        m.convert("Conabio", "ConabioAll.mer");
    }

}
