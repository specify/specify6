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
package edu.ku.brc.specify.toycode.mexconabio;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.toycode.mexconabio.FieldDef.DataType;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Feb 3, 2010
 *
 */
public class FileMakerToMySQL extends DefaultHandler
{
    protected SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
    protected SimpleDateFormat stf = new SimpleDateFormat("k:mm:ss");
    
    protected XMLReader        xmlReader;
    protected StringBuilder    buffer = new StringBuilder();
    protected Vector<FieldDef> fields = new Vector<FieldDef>();
    protected Vector<String>   values = new Vector<String>();
    protected Vector<Integer>  sizes  = new Vector<Integer>();
       
    protected int              rowCnt = 0;
    protected int              rowNum = 0;
    protected int              colNum = 0;
    
    protected boolean          doColSizes = false;
    
    protected boolean          debug  = false;
    protected int              chkRow = 106000;
    
    // Database Memebers
    protected String  dbName    = "mex";
    protected String  userName  = "root";
    protected String  password  = "root";
    protected String  tableName = null;
    protected String  keyField  = null;
    protected boolean doAddKey  = false;
    
    protected Connection        conn = null;
    protected PreparedStatement pStmt = null;
    
    /**
     * 
     */
    public FileMakerToMySQL(final String tableName, 
                            final String keyField,
                            final boolean doAddKey)
    {
        super();
        
        this.tableName = tableName;
        this.keyField  = keyField;
        this.doAddKey  = doAddKey;
        
        try
        {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/"+dbName+"?characterEncoding=UTF-8&autoReconnect=true", userName, password);
            
            Statement stmt = conn.createStatement();
            stmt.execute("DROP TABLE "+tableName);
            stmt.close();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    public void shutdown()
    {
        try
        {
            if (pStmt != null)
            {
                pStmt.close();
            }
            conn.close();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes attrs)
    {
        buffer.setLength(0);
        
        if (localName.equals("FIELD"))
        {
            if (doColSizes)
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
                        //value = StringUtils.replace(value, "–", "n");
                        value = StringUtils.replace(value, ":", "");
                        value = StringUtils.replace(value, ";", "");
                        //value = StringUtils.replace(value, "‡", "a");
                        value = StringUtils.replace(value, ".", "");
                        //value = StringUtils.replace(value, "Ã±", "a");
                        //value = StringUtils.replace(value, "Ã¡", "a");
                        value = StringUtils.replace(value, "/", "");
                        
                        //System.out.println(value);
                        
                        if ((value.charAt(0) >= '0' && value.charAt(0) <= '9') || 
                                value.equalsIgnoreCase("New") || 
                                value.equalsIgnoreCase("Group"))
                        {
                            value = "Fld" + value;
                        }
                        
                        fldDef.setName(value);
                        
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
                    //System.out.println(attrs.getLocalName(i)+" = "+attrs.getValue(i));
                }
            }
            
        } else if (localName.equals("ROW"))
        {
            for (int i=0;i<attrs.getLength();i++)
            {
                String attr  = attrs.getLocalName(i);
                String value = attrs.getValue(i);
                if (attr.equals("RECORDID"))
                {
                    rowNum = Integer.parseInt(value);
                    break;
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException
    {
        return super.resolveEntity(publicId, systemId);
    }
    //                               
    

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
    {
        
        String[] syms  = {"Ã¼", "Ã³", "Ã©", "Ã¤", "Ã¡", "Ã­", "Ã¶", "Ã¸", "Ã…", "Ãª", "Ã±", "Ã§", "Ãx", "Ãº", "Ã"};
        String[] chars = {"Ÿ",  "—",  "Ž", "Š",   "‡",  "’",  "š",  "¿",  "",  "",  "–",  "",  "’",  "œ",  "ç"};

        if (buffer != null) 
        {
          String s = new String(ch, start, length);
          
          for (int i=0;i<syms.length;i++)
          {
              s = StringUtils.replace(s, syms[i], chars[i]);
          }
          
          /*int inx = s.indexOf("Ã");
          if (inx > -1)
          {
              for (int i=0;i<s.length();i++)
              {
                  System.out.println((byte)s.charAt(i));
              }
              System.out.println(s);
              inx++; 
          }*/
          /*if (s != null && !s.isEmpty() && s.indexOf("mez") > -1)
          {
             try
            {
                 /*System.out.println(Arrays.toString("šŠ".getBytes("ISO-8859-1")));

                 s = "M.G³mez";
                 for (int i=0;i<s.length();i++)
                 {
                     int val = ((int)s.charAt(i));
                     System.out.println(s.charAt(i)+" ["+val+"]["+Integer.toHexString(val)+"]["+((byte)s.charAt(i))+"]");
                 }
                System.out.print(s+" - "+StringUtils.replace(s, "Ã³", "š") + " - ");
                ByteArrayInputStream encXML = new  ByteArrayInputStream(s.getBytes());
                byte[] bytes = new byte[1024];
                int n = encXML.read(bytes, 0, length);
                s = new String(bytes, 0, n, "UTF-8");
                System.out.println(">"+s+"<");
                encXML.close();
                
                
            } catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
              System.out.println(s);
          }*/
          buffer.append(s);
      }
      
      //System.out.println(buffer.toString());
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(final String namespaceURI, final String localName, final String qName)
    {
        //System.out.println(localName+" "+qName+" ["+buffer.toString()+"]");
        
        if (localName.equals("DATA"))
        {
            //if (buffer.length() > 0) System.out.println(buffer.toString());
            if (debug)
            {
                //if (rowCnt > 106000) 
                {
                    //System.out.println("col["+colNum+"]");
                    //if (colNum == 0) System.out.println("BarCd["+buffer.toString()+"]");
                }
            }
            
            if (!doColSizes)
            {
                values.add(buffer.toString());
                
            } else
            {
                String val = buffer.toString();
                
                FieldDef fd = fields.get(colNum);
                fd.setMaxSize(buffer.length());
                if (fd.isNumber() && !fd.isDouble() && StringUtils.contains(buffer.toString(), '.'))
                {
                    fd.setDouble(true);
                }
            }
            colNum++;
            
        } else if (localName.equals("ROW"))
        {
            if (!doColSizes)
            {
                writeRow();
                
            } else if (debug)
            {
                //System.out.println("R: "+rowCnt);
                //if (rowCnt > chkRow) System.out.println("R: "+rowCnt);
            }
            
            if (rowCnt % 1000 == 0) System.out.println(rowCnt);
            rowCnt++;
            
            colNum = 0;
            
        } else if (localName.equals("METADATA"))
        {
            if (!doColSizes)
            {
                createTable(tableName, keyField, true);
                if (true) 
                {
                    System.exit(0);
                }
            }
        } else if (localName.equals("RESULTSET"))
        {
            if (doColSizes)
            {
                for (FieldDef fd : fields)
                {
                    if (fd.getMaxSize() > 0) System.out.println(fd.getName()+" - "+fd.getMaxSize());
                }
            }
        }
        
        buffer.setLength(0);
    }
    
    /**
     * @param tblName
     * @param keyField
     */
    private void createTable(final String tblName, final String keyField, final boolean doForceAll)
    {
        /*if (doAddKey)
        {
            FieldDef fd = new FieldDef("ID", DataType.eNumber, false, false);
            fields.insertElementAt(fd, 0);
        }*/
        
        String[] syms  = {"Ã¼", "Ã³", "Ã©", "Ã¤", "Ã¡", "Ã­", "Ã¶", "Ã¸", "Ã…", "Ãª", "Ã±", "Ã§", "Ãx", "Ãº", "Ã"};
        String[] chars = {"u",  "o",  "e", "a",   "a",  "i",  "o",  "o",  "A",  "e",  "n",  "c",  "i",  "u",  "A"};
        //String[] chars = {"Ÿ",  "—",  "Ž", "Š",   "‡",  "’",  "š",  "¿",  "",  "",  "–",  "",  "’",  "œ",  "ç"};
        
        StringBuilder selectDB = new StringBuilder();
        for (FieldDef fd : fields)
        {
            String s = fd.getName();
            System.out.println(s);
            while (s.indexOf('Ã') > -1)
            {
                for (int ii=0;ii<syms.length;ii++)
                {
                    s = StringUtils.replace(s, syms[ii], chars[ii]);
                }
            }
            if (selectDB.length() > 0) selectDB.append(", ");
            selectDB.append(s);
            fd.setName(s);
        }
        try
        {
            FileUtils.writeStringToFile(new File("select.txt"), selectDB.toString());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
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
        
        
        sb.append(String.format("PRIMARY KEY (`%s`)\n", fields.get(primaryIndex).getName()));
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n");
        
        System.err.println(sb.toString());
        
        System.setProperty(DBMSUserMgr.factoryName, "edu.ku.brc.dbsupport.MySQLDMBSUserMgr");
        DBMSUserMgr mgr = DBMSUserMgr.getInstance();
        if (mgr.connect("root", "root", "localhost", "mex"))
        {
            if (mgr.doesDBHaveTable(tblName))
            {
                mgr.dropTable(tblName);
            }
            mgr.close();
        }
        BasicSQLUtils.update(conn, sb.toString());
        
        createPrepare(tblName, true);
    }
    
    /**
     * @param tblName
     */
    protected void createPrepare(final String tblName, final boolean doAllFields)
    {
        if (doAddKey)
        {
            fields.remove(0);
        }
        
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(tblName);
        sb.append(" (");
        
        int i = 0;
        for (FieldDef fd : fields)
        {
            if (doAllFields || fd.getMaxSize() > 0 || doAllFields)
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
        
        try
        {
            pStmt = conn.prepareStatement(sb.toString());
            FileUtils.writeStringToFile(new File("prepare_stmt.txt"), sb.toString());
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     */
    private void writeRow()
    {
        try
        {
            int i   = 0;
            int col = 1;
            for (FieldDef fd : fields)
            {
                if (fd.getMaxSize() > 0)
                {
                    String val = values.get(i);
                    if (StringUtils.isNotEmpty(val))
                    {
                        switch (fd.getType())
                        {
                            case eText :
                                pStmt.setString(col, val);
                                break;
                                
                            case eNumber :
                            {
                                if (StringUtils.isNumericSpace(val))
                                {
                                    if (fd.isDouble())
                                    {
                                        pStmt.setDouble(col, Double.parseDouble(val));    
                                    } else
                                    {
                                        pStmt.setInt(col, Integer.parseInt(val));
                                    }
                                } else
                                {
                                    pStmt.setObject(col, null);
                                }
                                break;
                            }
                            
                            case eDate :
                            {
                                Date date = null;
                                try
                                {
                                   date = sdf.parse(val);
                                   pStmt.setDate(col, new java.sql.Date(date.getTime()));
                                   
                                } catch (Exception ex)
                                {
                                    pStmt.setObject(col, null);
                                }
                                
                                
                                break;
                            }
                            
                            case eTime :
                            {
                                Time time =  null;
                                try
                                {
                                   time = Time.valueOf(val);
                                } catch (Exception ex){}
                                pStmt.setTime(col, time);
                                break;
                            }
                        }
                    } else
                    {
                        pStmt.setObject(col, null);
                    }
                    col++;
                }
                i++;
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        
        try
        {
            pStmt.execute();
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        values.clear();
    }
    
    
    /**
     * @param fileName
     */
    protected void cleanFile(final String fileName)
    {
        File srcFile = new File(fileName);
        try
        {
            File destFile = new File("xxx.xml");//File.createTempFile("DB", "xml");
            FileUtils.copyFile(srcFile, destFile);
            
            //System.out.println("Clean FIle: "+dest)
            
            FileReader fr       = new FileReader(srcFile);
            PrintWriter pw      = new PrintWriter(destFile);
            char[]     chars    = new char[4096*8];
            int        numChars = fr.read(chars, 0, chars.length);
            while (numChars > 0)
            {
                for (int i=0;i<chars.length;i++)
                {
                    //if (chars[i] < 32 || chars[i] > 126)
                    if (chars[i] == 0xb)
                    {
                        System.out.println("fixed["+chars[i]+"]");
                        chars[i] = ' ';
                        
                    }
                }
                pw.write(chars, 0, numChars);
                numChars = fr.read(chars, 0, chars.length);
            }
            
            fr.close();
            pw.close();
            
            FileUtils.copyFile(destFile, srcFile);
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
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
     * 
     */
    public void process(final boolean doColSizes)
    {
        rowCnt = 0;
        this.doColSizes = doColSizes;
        
        if (doColSizes && doAddKey)
        {
            fields.add(new FieldDef("ID", "ID", DataType.eNumber, false, false));
        }
        
        try
        {
            FileReader fr = new FileReader(new File("/Users/rods/Documents/Conabio.xml"));
            /*System.out.println("Encoding: "+fr.getEncoding());
            System.out.println("Encoding: "+System.getProperty("file.encoding"));
            
            String property = System.getProperty("file.encoding");
            String actual = new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding();
            System.out.println("property=" + property + ", actual=" + actual);
            */

            //FileReader fr = new FileReader(new File("/Users/rods/Documents/mex_conabio.xml"));
            //FileReader fr = new FileReader(new File("/Users/rods/Documents/mex.xml"));
            //FileReader fr = new FileReader(new File("xxx.xml"));
            
            /*File file = new File("/Users/rods/Documents/mex_conabio.xml");
            
            InputStream  inputStream = new FileInputStream(file);
            Reader       reader      = new InputStreamReader(inputStream,"UTF-8");
             
            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");
             
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            saxParser.parse(is, this);*/
            
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
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        if (!System.getProperty("os.name").equals("Mac OS X"))
        {
            try
            {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            } catch (UnsupportedLookAndFeelException e)
            {
                e.printStackTrace();
            }
            PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                //FileDialog dlg = new FileDialog((Frame)null, "Choose a FileMaker XML File");
                //dlg.setVisible(true);
                String fileName = "xxx.xml";//dlg.getFile();
                
                if (StringUtils.isNotEmpty(fileName))
                {
                    //String path = dlg.getDirectory() + File.separator + fileName;
                    
                    //String name = "AgentsBryo_Vasc";
                    String tableName = "Conabio";
                    //String name = "TaxonomicBryo_Vasc";
                    boolean addKey = true;
                    
                    FileMakerToMySQL fm2mysql = new FileMakerToMySQL(tableName, null, addKey);
                    //fm2mysql.cleanFile("/Users/rods/Documents/"+name+".xml");
                    fm2mysql.process(true);
                    fm2mysql.process(false);
                    
                    fm2mysql.shutdown();
                    
                } else
                {
                    System.exit(0);
                }
                
            }
        });
        
    }
    
    class FileMakerConvertFrame extends JFrame
    {
        
        public void start()
        {
            
            
        }
        
        /**
         * @param args
         */
        /*public static void main(String[] args)
        {
            //String name = "AgentsBryo_Vasc";
            //String name = "MexConabio";
            String name = "TaxonomicBryo_Vasc";
            boolean addKey = true;
            
            FileMakerToMySQL fm2mysql = new FileMakerToMySQL(name, null, addKey);
            //fm2mysql.cleanFile(name+".xml");
            fm2mysql.process(true);
            fm2mysql.process(false);
            
            fm2mysql.shutdown();
            
            if (!System.getProperty("os.name").equals("Mac OS X"))
            {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
            }
        }*/
    }

}
