package edu.ku.brc.specify;

import java.io.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class FixInitializers
{
    /**
     * 
     */
    protected String createCapitalizedName(final String name)
    {
        StringBuilder newName = new StringBuilder();
        newName.append(name.toUpperCase().charAt(0));
        newName.append(name.substring(1, name.length()));
        
        return newName.toString();
    }

      
    /**
     * 
     */
    public FixInitializers()
    {

        File outDir = new File("newClasses");
        if (!outDir.exists())
        {
            outDir.mkdir();
        }
        
        String path = "src/edu/ku/brc/specify/datamodel";
        String prefix = "edu.ku.brc.specify.datamodel.";
        
        StringBuilder strBuf     = new StringBuilder();
        StringBuilder entireFile = new StringBuilder();

        
        File srcDir = new File(path);
        String[] names = srcDir.list();
        List<String> fileList = new ArrayList<String>();


        if (names != null)
        {
            Collections.addAll(fileList, names);
            Collections.sort(fileList);
            
            int cnt = 0;
            for (String fileName : fileList)
            {
                //System.out.println("["+fileName+"]");
                if (fileName.startsWith(".") || fileName.endsWith("IFace.java"))
                {
                    continue;
                }
                
                File file = new File(path+"/"+fileName);
                //String buffer = getContents(file);
                
                if (fileName.indexOf("xml") > -1)
                {
                    continue;
                }
                String shortName = fileName.substring(0, fileName.indexOf('.'));
                String className = prefix + shortName;
                String lowerName = shortName.toLowerCase();
                
                try
                {
                    Class classObj = Class.forName(className);
                    Field[] fields = classObj.getDeclaredFields();

                    File oFile = new File(outDir.toString() + "/" + shortName + ".java");
                    Writer output = new BufferedWriter( new FileWriter( oFile ) );
       
                
                    strBuf.setLength(0);
                    
                    String startStr = "class " + shortName;
                    // Read Contents of file
                    BufferedReader input = new BufferedReader( new FileReader(file) );
                    String line;
                    boolean started = false;
                    boolean done    = false;
                    Hashtable<String, String> namesToFix = new Hashtable<String, String>();
                    
                    while (( line = input.readLine()) != null)
                    {
                        //System.out.println(line);
                        
                        boolean doWrite = true;
                        if (!done)
                        {
                            if (!started && line.indexOf(" // Fields") > -1)
                            {
                                started = true;
                                
                            } else if (started && line.indexOf(" // Constructors") > -1)
                            {
                                started = false;
                                done = true;
                            }
                        }

                        
                        if (started)
                        {
                            if (StringUtils.isNotEmpty(line) && (line.indexOf("protected") > -1 || line.indexOf("private") > -1))
                            {
                                String[] strs = StringUtils.split(line, " ");
                                if (strs.length == 3)
                                {
                                    String type      = strs[1];
                                    String fieldName = StringUtils.stripEnd(strs[2], ";");

                                    output.write("     protected ");
                                    if (strs[1].equals("Set"))
                                    {
                                        String capName = StringUtils.capitalize(fieldName);
                                        if (fieldName.charAt(fieldName.length()-1) == 's')
                                        {
                                            capName = capName.substring(0, capName.length()-1);
                                        }
                                        output.write("Set<"+capName+"> " + fieldName+" = new HashSet<"+capName+">();");
                                        namesToFix.put("get"+capName+"s", capName);
                                        namesToFix.put("set"+capName+"s", capName);
                                        
                                    } else if (fieldName.equals("timestampCreated"))
                                    {
                                        output.write("timestampCreated = Calendar.getInstance();");
                                    } else
                                    {
                                        output.write(type+" "+fieldName+" = null;");
                                    }
                                    output.write("\n");

                                    
                                    doWrite = false;
                                } else
                                {
                                    System.out.println("More than 3 tokens["+line+"]");
                                }
                            } 
                        }
                        
                        
                        if (doWrite)
                        {
                            int inx = line.indexOf(" get");
                            if (inx > -1)
                            {
                                int eInx = line.indexOf("(");
                                if (eInx > -1)
                                {
                                    String methodName = line.substring(inx+1, eInx);
                                    System.out.println(methodName);
                                    String clsName = namesToFix.get(methodName);
                                    if (clsName != null)
                                    {
                                        output.write("    public Set<"+clsName+"> "+methodName+"() {");
                                        output.write("\n");
                                        continue;
                                    }
                                }
                            } else 
                            {
                                inx = line.indexOf(" set");
                                if (inx > - 1)
                                {
                                    int eInx = line.indexOf("(");
                                    if (eInx > -1)
                                    {
                                        String methodName = line.substring(inx+1, eInx);
                                        System.out.println(methodName);
                                        String clsName = namesToFix.get(methodName);
                                        if (clsName != null)
                                        {
                                            output.write("    public void "+methodName+"(Set<"+clsName+"> "+StringUtils.uncapitalize(clsName)+"s) {");
                                            output.write("\n");
                                            continue;
                                        }
                                    }
                                    
                                }
                            }
                            output.write(line);
                            output.write("\n");
                        }
                    }
                    input.close();
                    output.flush();
                    output.close();
                    
                    //System.out.println(strBuf.toString());
                    if (true) return;

                    /*int cnt = 0;
                    int indent = code.length();
                    Field[] fields = classObj.getDeclaredFields();
                    int fieldsUsed = 0;
                    for (Field fld : fields)
                    {
                        String fieldName = fld.getName();
                        String type      = fld.getType().getSimpleName();
                        if (fieldsToSkip.get(fieldName) != null ||
                            fieldName.endsWith("Id") ||
                            type.equals("Set") || 
                            type.equals("Date"))
                        {
                            continue;
                        }
                        fieldsUsed++;
                    }
                    
                    // Parameter (Fields) for the "create" method call
                    for (Field fld : fields)
                    {
                        String fieldName = fld.getName();
                        String type      = fld.getType().getSimpleName();
                        if (fieldsToSkip.get(fieldName) != null ||
                                fieldName.endsWith("Id") ||
                                type.equals("Set") || 
                                type.equals("Date"))
                        {
                            continue;
                        }
                        
                        if (cnt > 0) 
                        {
                            for (int ii=0;ii<indent;ii++) strBuf.append(' ');
                        }
                        
                        strBuf.append("final "+type+" " +fld.getName());
                        
                        if (cnt < fieldsUsed-1)
                        {
                            strBuf.append(",\n");
                        }
                        cnt++;
                    }
                    
                    strBuf.append(")\n    {\n");
                    
                    strBuf.append("        "+shortName+" "+lowerName+" = new "+shortName+"();\n");
                
                
                    Method[] methods = classObj.getMethods();
                    for (Method method : methods)
                    {
                        //String fieldName
                        if (method.getName().startsWith("setTimestamp"))
                        {
                            strBuf.append("        "+lowerName+"."+method.getName()+"(new Date());\n"); 
                            
                        } else if (method.getName().startsWith("set"))
                        {
                            Class[] parms    = method.getParameterTypes();
                            String type      = parms[0].getSimpleName();
                            String mn        = method.getName();
                            String fieldName = mn.toLowerCase().charAt(3) + mn.substring(4, mn.length());
                            
                            if (fieldsToSkip.get(fieldName) != null ||
                                    fieldName.endsWith("Id"))
                            {
                                continue;
                            }
                            
                            strBuf.append("        "+lowerName+"."+method.getName());
                            //System.out.println(parms[0].getSimpleName());
                            if (parms[0].getSimpleName().equals("Set"))
                            {
                                strBuf.append("(new HashSet<Object>());\n");
                                
                            } else if (type.equals("Set"))
                            {
                                strBuf.append("(new HashSet<Object>());\n");
                            } else
                            {
                                strBuf.append("("+ fieldName +");\n");
                            }

                        }
                    }
                    
                    strBuf.append("        if (session != null)\n");
                    strBuf.append("        {\n");
                    strBuf.append("          session.saveOrUpdate("+lowerName+");\n");
                    strBuf.append("        }\n");
                    strBuf.append("        return "+lowerName+";\n");
                    strBuf.append("    }\n");
                    
                    System.out.println(strBuf.toString());
                    entireFile.append(strBuf);
                    
                    //Object obj = classObj.newInstance();
                     */
                     
                    cnt++;
                    if (cnt == 1) break;
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

            
        } else
        {
            System.out.println("Dir was null");
        }

    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        FixInitializers ixInitializers = new FixInitializers();

    }

}
