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
    
    Hashtable<String, String> props    = new Hashtable<String, String>();
    Hashtable<String, String> propsType = new Hashtable<String, String>();
    
    protected String getAttrName(final String line, final String attrName)
    {
        String name = attrName + "=\"";
        int inx = line.indexOf(name);
        boolean inError = false;
        if (inx > -1)
        {
            inx += name.length();
            
            int einx = line.indexOf('"', inx);
            if (einx > -1)
            {
                //System.out.println(line);
                return line.substring(inx, einx);
            }
        }
        //System.out.println("For ["+line+"] couldn't find ["+attrName+"]");
        return null;
    }
    
    protected boolean getHBMProperties(final File hbmFile)
    {
        try
        {
            BufferedReader input = new BufferedReader( new FileReader(hbmFile) );
            String         line;
            int            lineCnt = 0;
            while (( line = input.readLine()) != null)
            {
                lineCnt++;
                
                if (line.indexOf("<set") > -1)
                {
                    int setLineCnt = lineCnt;
                    String setName = getAttrName(line, "name");
                    String className = null;
                    String type = "";
                    boolean doNextLine = false;
                    while (( line = input.readLine()) != null)
                    {
                        lineCnt++;
                        
                        if (doNextLine ||
                            line.indexOf("one-to-many") > -1 || 
                            line.indexOf("many-to-many") > -1 ||
                            line.indexOf("many-to-one") > -1)
                        {
                            if (line.indexOf("one-to-many") > -1)
                            {
                                type = "one-to-many";
                                
                            } else if (line.indexOf("many-to-many") > -1)
                            {
                                type = "many-to-many";
                                
                            } else if (line.indexOf("many-to-one") > -1)
                            {
                                type = "many-to-one";
                            }
                            
                            className = getAttrName(line, "class");
                            if (className != null)
                            {
                                break;
                            } else
                            {
                                doNextLine = true;
                            }
                        }
                        if (line.indexOf("</set") > -1)
                        {
                            break;
                        }
                    }
                    if (className == null)
                    {
                        System.out.println("Couldn't find class for set ["+setName+"]");
                        className = ".RecordSetItem"; // RecordSet
                    }
                    if (setName == null)
                    {
                        System.out.println("Couldn't find set for file ["+hbmFile.getName()+"] line count["+setLineCnt+"]");
                    }
                    className = className.substring(className.lastIndexOf('.')+1, className.length());
                    props.put(setName, className);
                    propsType.put(setName, type);
                    System.out.println("["+setName+"]["+className+"]");
                }
            }
            input.close();
          
        } catch (FileNotFoundException ex)
        {
            return false;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return true;
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


        File srcDir = new File(path);
        String[] names = srcDir.list();
        List<String> fileList = new ArrayList<String>();


        if (names != null)
        {
            Collections.addAll(fileList, names);
            Collections.sort(fileList);

            // Make Hash of the Class names
            Hashtable<String, String> classNamesHash = new Hashtable<String, String>();
            for (String name : names)
            {
                if (name.startsWith(".") || name.startsWith("hbm") || name.endsWith("IFace.java"))
                {
                    continue;
                }
                System.out.println(name);
                String shortName = name.substring(0, name.indexOf('.'));
                classNamesHash.put(shortName, shortName);
            }
            
            boolean skipTrees = true;
            
            int cnt = 0;
            for (String fileName : fileList)
            {
                System.out.println("["+fileName+"]");
                if (fileName.startsWith(".") || fileName.indexOf("hbm") > -1 || fileName.toLowerCase().endsWith("iface.java"))
                {
                    continue;
                }
                
                if (skipTrees && fileName.indexOf("TreeDef") > -1)
                {
                    continue;
                }

                File file = new File(path+"/"+fileName);
                if (fileName.indexOf("xml") > -1)
                {
                    continue;
                }
                
                String shortName = fileName.substring(0, fileName.indexOf('.'));
                String className = prefix + shortName;

                props.clear();
                propsType.clear();
                
                boolean ok = getHBMProperties(new File(path+"/hbm/"+shortName+".hbm.xml"));
                if (!ok)
                {
                    System.out.println("Skipping file["+shortName+"] no corresponding HBM file.");
                    continue;
                }
                
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

                    boolean started             = false;
                    boolean done                = false;
                    boolean doneInit            = false;
                    boolean importDone          = false;
                    boolean filterOutAddMethods = true;
                    boolean addImports          = false;
                    boolean writeNewDataMembers = false;
                    boolean addInitializer      = false;
                    boolean fixGetSetMethods    = false;


                    Hashtable<String, String> namesToFix = new Hashtable<String, String>();
                    List<String>              initLines  = new ArrayList<String>();
                    List<String>              addMethods = new ArrayList<String>();
                    List<String>              delMethods = new ArrayList<String>();

                    int maxWidth = 0;
                    String line;
                    while (( line = input.readLine()) != null)
                    {
                        //System.out.println(line);
                        
                        if (filterOutAddMethods && line.indexOf("// Add Methods") > -1)
                        {
                            while (( line = input.readLine()) != null)
                            {
                                if (line.indexOf("// Done Add Methods") > -1)
                                {
                                    break;
                                }
                            }
                            filterOutAddMethods = false;
                            continue;
                        }
                                

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

                        if (addImports && !importDone && !done && !started && line.startsWith("import"))
                        {
                            output.write("import java.util.HashSet;\nimport java.util.Calendar;\n");
                            importDone = true;
                        }


                        if (started)
                        {
                            if (StringUtils.isNotEmpty(line) && (line.indexOf("protected") > -1 || line.indexOf("private") > -1))
                            {
                                String[] strs = StringUtils.split(line, " ");
                                if (strs.length == 3)
                                {
                                    String fieldName = StringUtils.stripEnd(strs[2], ";");
                                    maxWidth = Math.max(maxWidth, fieldName.length());

                                    if (strs[1].startsWith("Set"))
                                    {
                                        String  capName       = StringUtils.capitalize(fieldName);
                                        String  singleObjName = fieldName;
                                        boolean endsInS       = !fieldName.endsWith("ies") && !fieldName.endsWith("sses") && fieldName.charAt(fieldName.length()-1) == 's';
                                        String singleObjClassName = props.get(singleObjName);
                                        
                                        if (singleObjClassName == null)
                                        {
                                            throw new RuntimeException("Couldn't find class for datamember["+singleObjName+"]");
                                        }
                                        if (endsInS && classNamesHash.get(capName) == null)
                                        {

                                            if (endsInS)
                                            {
                                                capName = capName.substring(0, capName.length()-1);
                                                singleObjName = fieldName.substring(0, fieldName.length()-1);
                                            }
                                        }

                                        initLines.add("        " + fieldName+" = new HashSet<"+singleObjClassName+">();");
                                        namesToFix.put("get"+capName+"s", capName);
                                        namesToFix.put("set"+capName+"s", capName);

                                        //if (propsType.get(singleObjName).equals("many-to-many"))
                                        addMethods.add("\n    public void add"+capName+"(final "+singleObjClassName+" "+singleObjName+")\n    {");
                                        addMethods.add("        this."+fieldName+".add("+singleObjName+");");
                                        addMethods.add("        "+singleObjName+".set"+shortName+"(this);\n    }");

                                        delMethods.add("\n    public void remove"+capName+"(final "+singleObjClassName+" "+singleObjName+")\n    {");
                                        delMethods.add("        this."+fieldName+".remove("+singleObjName+");");
                                        if (singleObjClassName.endsWith("Attr"))
                                        {
                                            String name = StringUtils.capitalize(singleObjClassName.substring(0, singleObjClassName.indexOf("Attr")));
                                            delMethods.add("        "+singleObjName+".set"+name+"(null);\n    }");
                                        } else
                                        {
                                            delMethods.add("        "+singleObjName+".set"+shortName+"(null);\n    }");
                                        }

                                        if (writeNewDataMembers)
                                        {
                                            output.write("     protected Set<"+singleObjClassName+"> " + fieldName+";\n");
                                            doWrite = false;
                                        }

                                    } else if (fieldName.equals("timestampCreated"))
                                    {
                                        initLines.add("        timestampCreated = Calendar.getInstance().getTime();");
                                    } else
                                    {
                                        initLines.add("        "+fieldName+" = null;");
                                    }
                                } else
                                {
                                    System.out.println("More than 3 tokens["+line+"]");
                                }
                            }
                        }

                        if (addInitializer && done && !doneInit && line.indexOf("// Property accessors") > -1)
                        {
                            output.write("    // Initializer\n");
                            output.write("    public void initialize()\n    {\n");
                            for (String s : initLines)
                            {
                                output.write(s);
                                output.write("\n");
                            }
                            output.write("    }\n    // End Initializer\n\n");
                            doneInit = true;
                        }

                        if (line.startsWith("}"))
                        {
                            output.write("\n    // Add Methods\n");
                            for (String s : addMethods)
                            {
                                output.write(s);
                                output.write("\n");
                            }
                            output.write("\n    // Done Add Methods\n");
                            
                            output.write("\n    // Delete Methods\n");
                            for (String s : delMethods)
                            {
                                output.write(s);
                                output.write("\n");
                            }
                            output.write("\n    // Delete Add Methods\n");
                        }


                        if (doWrite)
                        {
                            if (fixGetSetMethods)
                            {
                                int inx = line.indexOf(" get");
                                if (inx > -1 && line.indexOf("if(") == -1)
                                {
                                    int eInx = line.indexOf("(");
                                    if (eInx > -1)
                                    {
                                        //System.out.println(inx+"  "+eInx+"[ "+line+"]");
                                        String methodName = line.substring(inx+1, eInx);
                                        //System.out.println(methodName);
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
                                            //System.out.println(methodName);
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
                            }
                            output.write(line);
                            output.write("\n");
                        }
                    }
                    input.close();
                    output.flush();
                    output.close();

                    cnt++;
                    //if (cnt == 1) break;

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
