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

            int cnt = 0;
            for (String fileName : fileList)
            {
                System.out.println("["+fileName+"]");
                if (fileName.startsWith(".") || fileName.indexOf("hbm") > -1 || fileName.endsWith("IFace.java"))
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

                    boolean started = false;
                    boolean done    = false;
                    boolean doneInit = false;
                    boolean importDone = false;

                    Hashtable<String, String> namesToFix = new Hashtable<String, String>();
                    List<String>              initLines  = new ArrayList<String>();
                    List<String>              addMethods = new ArrayList<String>();

                    int maxWidth = 0;
                    String line;
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

                        if (!importDone &&!done && !started && line.startsWith("import"))
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

                                    if (strs[1].equals("Set"))
                                    {
                                        String  capName       = StringUtils.capitalize(fieldName);
                                        String  singleObjName = fieldName;
                                        boolean endsInS       = fieldName.charAt(fieldName.length()-1) == 's';
                                        if (endsInS && classNamesHash.get(capName) == null)
                                        {

                                            if (endsInS)
                                            {
                                                capName = capName.substring(0, capName.length()-1);
                                                singleObjName = fieldName.substring(0, fieldName.length()-1);
                                            }
                                        }

                                        initLines.add("        " + fieldName+" = new HashSet<"+capName+">();");
                                        namesToFix.put("get"+capName+"s", capName);
                                        namesToFix.put("set"+capName+"s", capName);

                                        addMethods.add("\n    public void add"+capName+"(final "+capName+" "+singleObjName+")\n    {");
                                        addMethods.add("        this."+fieldName+".add("+singleObjName+");\n    }");

                                        output.write("     protected Set<"+capName+"> " + fieldName+";\n");
                                        doWrite = false;

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

                        if (done && !doneInit && line.indexOf("// Property accessors") > -1)
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
                            output.write("    // Add Methods\n");
                            for (String s : addMethods)
                            {
                                output.write(s);
                                output.write("\n");
                            }
                            output.write("\n    // Done Add Methods\n");
                        }


                        if (doWrite)
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
