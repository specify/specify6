/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.specify.tools.datamodelgenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.annotations.Index;

import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.db.PickListItemIFace;
import edu.ku.brc.util.DatamodelHelper;

/**
 * This generates the specify datamodel file
 * 
 * @code_status Alpha
 * 
 * @author rods
 * 
 */
public class DatamodelGenerator
{
    enum RelType {OneToMany, OneToOne, ManyToOne, ManyToMany}
    
    private static final Logger log = Logger.getLogger(DatamodelGenerator.class);
    
    protected static final boolean DEBUG = false;

    protected Hashtable<String, TableMetaData> tblMetaDataHash = new Hashtable<String, TableMetaData>();
    
    protected File    srcCodeDir = null;
    protected String  packageName = null;
    protected int     missing = 0;
    

    /**
     * 
     */
    public DatamodelGenerator()
    {
        // no op
    }
    
    /**
     * Looks for a child node "display" and creates the appropriate object or returns null.
     * @param element the "table".
     * @return null or a Display object
     */
    private Display createDisplay(final Element element)
    {
        if (element != null)
        {
            Element fdElement = (Element)element.selectSingleNode("display");
            if (fdElement != null)
            {
                return new Display(fdElement.attributeValue("objtitle"), 
                                        fdElement.attributeValue("view"), 
                                        fdElement.attributeValue("dataobjformatter"), 
                                        fdElement.attributeValue("uiformatter"), 
                                        fdElement.attributeValue("searchdlg"), 
                                        fdElement.attributeValue("newobjdlg"));
            }
        }
        return null;
    }

    /**
     * Given and XML node, returns a Table object by grabbing the appropriate
     * attribute values.
     * 
     * @param element the XML node
     * @return Table object
     */
    private Table createTable(final String className, final String tableName)
    {
        // get Class Name (or name) from HBM file
        log.info("Processing: " + className);
        
        // Get Meta Data for HBM
        TableMetaData tableMetaData = tblMetaDataHash.get(className);
        if (tableMetaData == null)
        {
            // Throw exception if there is an HBM we don't have meta data for
            log.error("Could not retrieve TableMetaData from tblMetaDataHashtable for table: " + className);
            throw new RuntimeException("Could not retrieve TableMetaData from tblMetaDataHashtable for table: " + className 
                    + " check to see if table is listed in the file: " + DatamodelHelper.getTableIdFilePath());
        }
        
        return new Table(className, 
                         tableName, 
                         null, 
                         tableMetaData.getId(), 
                         tableMetaData.getDisplay(), 
                         tableMetaData.isForQuery(), 
                         tableMetaData.getBusinessRule());

    }

    /**
     * @param method
     * @return
     */
    protected String getReturnType(final Method method)
    {
        Class<?> classObj = method.getReturnType();
        // If there is a better way, PLEASE help me!
        if (classObj == Set.class)
        {
            ParameterizedType type = (ParameterizedType)method.getGenericReturnType();
            for (Type t : type.getActualTypeArguments())
            {
                String cls = t.toString();
                return cls.substring(6, cls.length());
            }
        }
        return classObj.getName();
    }
    
    /**
     * @param method
     * @return
     */
    protected String getNameFromMethod(final Method method)
    {
        String name = method.getName();
        name = name.substring(3, 4).toLowerCase() + name.substring(4, name.length());

        return name;
    }
    
    /**
     * @param method
     * @param type
     * @param joinCol
     * @return
     */
    public Relationship createRelationsip(final Method method, 
                                          final String type,
                                          final javax.persistence.JoinColumn joinCol,
                                          final String otherSideName,
                                          final boolean isRequired)
    {
        Relationship rel = new Relationship(type, getReturnType(method), joinCol != null ? joinCol.name() : "", getNameFromMethod(method));
        rel.setOtherSideName(otherSideName);
        rel.setRequired(isRequired);
        return rel;
    }


    /**
     * @param method
     * @param col
     * @return
     */
    public Id createId(final Method method, 
                       final javax.persistence.Column col)
    {
        return new Id(getNameFromMethod(method), getReturnType(method), col.name(), "");
    }

    /**
     * @param method
     * @param col
     * @return
     */
    public Field createField(final Method method, 
                             final javax.persistence.Column col,
                             final boolean isLob)
    {
        String retType;
        String len;
        
        if (isLob)
        {
            retType = "text";
            len     = "";
        } else
        {
            retType = isLob ? "text" : getReturnType(method);
            len = retType.equals("java.lang.String") ? Integer.toString(col.length()) : (col.length() != 255 ? Integer.toString(col.length()) : "");
        }
        Field field = new Field(getNameFromMethod(method), retType, col.name(), len);
        field.setRequired(!col.nullable());
        field.setUpdatable(col.updatable());
        field.setUnique(col.unique());
        return field;
    }

    @SuppressWarnings("unchecked")
    public Class<?> getSetsClassType(final Class<?> cls, final String methodName)
    {
        try
        {
            File f = new File(srcCodeDir.getAbsoluteFile() + File.separator + cls.getSimpleName() + ".java");
            if (!f.exists())
            {
                log.error("Can't locate source file["+f.getAbsolutePath()+"]");
                return null;
            }
            
            List<String> lines = FileUtils.readLines(f);
            for (String line : lines)
            {
                int sInx = line.indexOf("Set<");
                if (sInx > -1 && line.indexOf(methodName) > -1)
                {
                    int eInx = line.indexOf(">", sInx);
                    String className = line.substring(sInx+4, eInx);
                    return Class.forName(packageName + "." + className);
                }
            }
            
        } catch (ClassNotFoundException ex)
        {
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    

    
    /**
     * @param leftSide
     * @param rightSide
     * @param mappedByName
     * @return
     */
    protected String getRightSideForOneToMany(final Class<?> leftSide,
                                              final Class<?> rightSide, 
                                              final String   mappedByName)
    {
        if (StringUtils.isEmpty(mappedByName))
        {
            throw new RuntimeException("Couldn't find otherside method name missing for ["+ rightSide.getSimpleName()+ "]  mappedByName[" + mappedByName + "]");
        }
        
        for (Method method : rightSide.getMethods())
        {
            String methodName = method.getName();
            // Skip if it is a not a getter
            if (!methodName.startsWith("get"))
            {
                continue;
            }
            //System.out.println("Left Class["+leftSide.getSimpleName()+"]  Right["+rightSide.getSimpleName()+"] Right Side Method ["+methodName+"] Ret["+method.getReturnType().getSimpleName()+"]");
            
            // Skip if it is a not a ManyToOne
            if (!method.isAnnotationPresent(javax.persistence.ManyToOne.class))
            {
                continue;
            }
            
            Class<?> retType = method.getReturnType();
            boolean isSet = Collection.class.isAssignableFrom(retType);
            if (isSet)
            {
                Class<?> rt = getSetsClassType(rightSide, methodName);
                if (rt == null)
                {
                    continue; // probably because of an interface
                }
                retType = rt;                
                //System.out.println("Set["+(retType != null ? retType.getSimpleName() : "NULL")+"]");
            }
            
            // Skip if the Return Types don't match
            if (leftSide != retType)
            {
                continue;
            }
            
            return getFieldNameFromMethod(method);

        }
        return null;
    }
    
    /**
     * @param leftSide
     * @param rightSide
     * @param leftSideVarName
     * @return
     */
    protected String getRightSideForManyToOne(final Class<?> leftSide,
                                              final Class<?> rightSide, 
                                              final String   leftSideVarName)
    {
        for (Method method : rightSide.getMethods())
        {
            String methodName = method.getName();
            // Skip if it is a not a getter
            if (!methodName.startsWith("get"))
            {
                continue;
            }
            //System.out.println("getRightSideForManyToOne Left Class["+leftSide.getSimpleName()+"]  Right["+rightSide.getSimpleName()+"] Right Side Method ["+methodName+"] Ret["+method.getReturnType().getSimpleName()+"]");
            
            // Skip if it is a not a ManyToOne
            if (!method.isAnnotationPresent(javax.persistence.OneToMany.class))
            {
                continue;
            }
            
            Class<?> retType = method.getReturnType();
            boolean isSet = Collection.class.isAssignableFrom(retType);
            if (isSet)
            {
                Class<?> rt = getSetsClassType(rightSide, methodName);
                if (rt == null)
                {
                    continue; // probably because of an interface
                }
                retType = rt;
                //System.out.println("Set["+(retType != null ? retType.getSimpleName() : "NULL")+"]");
            }

            if (leftSide != retType)
            {
                continue;
            }
            
            javax.persistence.OneToMany oneToMany = (javax.persistence.OneToMany)method.getAnnotation(javax.persistence.OneToMany.class);
            String othersideName = oneToMany.mappedBy();
            if (StringUtils.isNotEmpty(othersideName)) // This should never be null
            {
                //System.out.println("\nXXX["+othersideName+"]["+retType+"]");
                //System.out.println("othersideName["+othersideName+"]  leftSideVarName["+leftSideVarName+"]");
                //System.out.println("["+leftSide.getSimpleName()+"]["+retType.getSimpleName()+"]");
                if (othersideName.equals(leftSideVarName))
                {
                    return getFieldNameFromMethod(method);
                }
            }
        }
        return null;
    }
    
    /**
     * @param leftSide
     * @param rightSide
     * @param leftSideVarName
     * @param isMappedBy
     * @return
     */
    protected String getRightSideForManyToMany(final Class<?> leftSide,
                                               final Class<?> rightSide, 
                                               final String   leftSideVarName)
    {
        for (Method method : rightSide.getMethods())
        {
            String methodName = method.getName();
            // Skip if it is a not a getter
            if (!methodName.startsWith("get"))
            {
                continue;
            }
            //System.out.println("getRightSideForManyToMany Left Class["+leftSide.getSimpleName()+"]  Right["+rightSide.getSimpleName()+"] Right Side Method ["+methodName+"] Ret["+method.getReturnType().getSimpleName()+"]");
            
            // Skip if it is a not a ManyToOne
            if (!method.isAnnotationPresent(javax.persistence.ManyToMany.class))
            {
                continue;
            }
            
            Class<?> retType = method.getReturnType();
            boolean isSet = Collection.class.isAssignableFrom(retType);
            if (isSet)
            {
                Class<?> rt = getSetsClassType(rightSide, methodName);
                if (rt == null)
                {
                    continue; // probably because of an interface
                }
                retType = rt;
                //System.out.println("Set["+(retType != null ? retType.getSimpleName() : "NULL")+"]");
            }
            
            if (leftSide == retType)
            {
                javax.persistence.ManyToMany manyToMany = (javax.persistence.ManyToMany)method.getAnnotation(javax.persistence.ManyToMany.class);
                // Caller wasn't mappedBy so look for mapped By
                String othersideName = manyToMany.mappedBy();
                
                if (StringUtils.isNotEmpty(othersideName) && leftSideVarName.equals(othersideName))
                {
                    return getFieldNameFromMethod(method);
                }
            }
        }
        return null;
    }
    
    /**
     * @param leftSide
     * @param rightSide
     * @param mappedByName
     * @return
     */
    @SuppressWarnings("cast")
    protected String getRightSideForOneToOne(final Class<?> leftSide,
                                             final Class<?> rightSide, 
                                             final String   leftSideVarName,
                                             final String   mappedName,
                                             final boolean  isMappedBy)
    {
        for (Method method : rightSide.getMethods())
        {
            String methodName = method.getName();
            // Skip if it is a not a getter
            if (!methodName.startsWith("get"))
            {
                continue;
            }
            //System.out.println("Left Class["+leftSide.getSimpleName()+"]  Right["+rightSide.getSimpleName()+"] Right Side Method ["+methodName+"] Ret["+method.getReturnType().getSimpleName()+"]");
            
            // Skip if it is a not a OneToOne
            if (!method.isAnnotationPresent(javax.persistence.OneToOne.class))
            {
                continue;
            }
            
            Class<?> retType = method.getReturnType();
            boolean isSet = Collection.class.isAssignableFrom(retType);
            if (isSet)
            {
                Class<?> rt = getSetsClassType(rightSide, methodName);
                if (rt == null)
                {
                    continue; // probably because of an interface
                }
                retType = rt;
                //System.out.println("Set["+(retType != null ? retType.getSimpleName() : "NULL")+"]");
            }
            
            String othersideName = "";
            if (isMappedBy)
            {
                othersideName = getFieldNameFromMethod(method);
                
            } else
            {
                javax.persistence.OneToOne oneToOne = (javax.persistence.OneToOne)method.getAnnotation(javax.persistence.OneToOne.class);
                // Caller wasn't mappedBy so look for mapped By
                othersideName = oneToOne.mappedBy();
            }
            
            if (StringUtils.isNotEmpty(othersideName) && leftSideVarName.equals(othersideName))
            {
                return getFieldNameFromMethod(method);
            }

        }
        return null;
    }
    


    /**
     * @param method
     * @return
     */
    protected String getFieldNameFromMethod(final Method method)
    {
        String methodName = method.getName().substring(3);
        return methodName.substring(0,1).toLowerCase() + methodName.substring(1, methodName.length());
    }
    
    /**
     * @param className
     * @param tableList
     */
    protected void processClass(final String className, final List<Table> tableList)
    {
        try
        {
            Class classObj = Class.forName(packageName + "." + className);
            
            Table   table       = null; 
            String  tableName   = null;

            if (classObj.isAnnotationPresent(javax.persistence.Table.class))
            {
                Vector<TableIndex> indexes = new Vector<TableIndex>();
                
                javax.persistence.Table tableAnno = (javax.persistence.Table)classObj.getAnnotation(javax.persistence.Table.class);
                tableName = tableAnno.name();
                
                org.hibernate.annotations.Table hiberTableAnno = (org.hibernate.annotations.Table)classObj.getAnnotation(org.hibernate.annotations.Table.class);
                if (hiberTableAnno != null)
                {
                    for (Index index : hiberTableAnno.indexes())
                    {
                        indexes.add(new TableIndex(index.name(), index.columnNames()));
                    }
                }
                
                table = createTable(packageName + "." + className, tableName);
                table.setIndexes(indexes);
                tableList.add(table);
            }
            
            if (table != null)
            {
                boolean isLob = false;
                for (Method method : classObj.getMethods())
                {
                    String methodName = method.getName();
                    if (!methodName.startsWith("get"))
                    {
                        continue;
                    }
                    
                    if (DEBUG)
                    {
                        System.out.println(className + " " + method.getName());
                    }
                    Type type = method.getGenericReturnType();
                    Class<?> typeClass;
                    if (type instanceof Class<?>)
                    {
                        typeClass = (Class<?>)type;
                    } else
                    {
                        typeClass = null;
                        for (Type t : ((ParameterizedType)type).getActualTypeArguments())
                        {
                            if (t instanceof Class<?>)
                            {
                                typeClass = (Class<?>)t;
                            }
                        }
                    }
                    if (typeClass == classObj || 
                            typeClass == AttributeIFace.class || 
                            typeClass == PickListItemIFace.class || 
                            typeClass == RecordSetItemIFace.class)
                    {
                        continue;
                    }
                    
                    String thisSideName = getFieldNameFromMethod(method);

                    if (method.isAnnotationPresent(javax.persistence.Lob.class))
                    {
                        isLob = true;
                    }

                    if (method.isAnnotationPresent(javax.persistence.Column.class))
                    {
                        if (method.isAnnotationPresent(javax.persistence.Id.class))
                        {
                            table.addId(createId(method, (javax.persistence.Column)method.getAnnotation(javax.persistence.Column.class)));
                        } else
                        {
                            table.addField(createField(method, (javax.persistence.Column)method.getAnnotation(javax.persistence.Column.class), isLob));
                        }
                        
                    } else if (method.isAnnotationPresent(javax.persistence.ManyToOne.class))
                    {
                        String otherSideName = getRightSideForManyToOne(classObj, typeClass, thisSideName);
                        
                        javax.persistence.JoinColumn join = method.isAnnotationPresent(javax.persistence.JoinColumn.class) ? (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class) : null;
                        if (join != null)
                        {
                            //String othersideName = typeClass == null ? "" : getOthersideName(classObj, typeClass, thisSideName, RelType.OneToMany);
                            table.addRelationship(createRelationsip(method, "many-to-one", join, otherSideName, join != null ? !join.nullable() : false));
                            
                        } else
                        {
                            log.error("No Join!");
                        }
                        
                    } else if (method.isAnnotationPresent(javax.persistence.ManyToMany.class))
                    {
                        javax.persistence.ManyToMany manyToMany = method.getAnnotation(javax.persistence.ManyToMany.class);

                        String  othersideName = manyToMany.mappedBy();
                        if (StringUtils.isEmpty(othersideName))
                        {
                            othersideName = getRightSideForManyToMany(classObj, typeClass, getFieldNameFromMethod(method));
                        }

                        javax.persistence.JoinColumn join = method.isAnnotationPresent(javax.persistence.JoinColumn.class) ? (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class) : null;
                        table.addRelationship(createRelationsip(method, "many-to-many", join, othersideName, join != null ? !join.nullable() : false));
                        
                    } else if (method.isAnnotationPresent(javax.persistence.OneToMany.class))
                    {
                        javax.persistence.OneToMany oneToMany = (javax.persistence.OneToMany)method.getAnnotation(javax.persistence.OneToMany.class);
                        
                        String  othersideName = oneToMany.mappedBy();
                        if (StringUtils.isEmpty(othersideName))
                        {
                            // This Should never happen
                            othersideName = getRightSideForOneToMany(classObj, typeClass, oneToMany.mappedBy());
                        }
                        
                        javax.persistence.JoinColumn join = method.isAnnotationPresent(javax.persistence.JoinColumn.class) ? (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class) : null;
                        table.addRelationship(createRelationsip(method, "one-to-many", join, othersideName, join != null ? !join.nullable() : false));
                        
                    } else if (method.isAnnotationPresent(javax.persistence.OneToOne.class))
                    {
                        javax.persistence.OneToOne oneToOne = (javax.persistence.OneToOne)method.getAnnotation(javax.persistence.OneToOne.class);
                        String  leftSideVarName = getFieldNameFromMethod(method);
                        boolean isMappedBy      = true;
                        String  othersideName   = oneToOne.mappedBy();
                        if (StringUtils.isEmpty(othersideName))
                        {
                            isMappedBy    = false;
                            othersideName = getRightSideForOneToOne(classObj, typeClass, leftSideVarName, othersideName, isMappedBy);
                        }
                        
                        javax.persistence.JoinColumn join = method.isAnnotationPresent(javax.persistence.JoinColumn.class) ? (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class) : null;
                        table.addRelationship(createRelationsip(method, "one-to-one", join, othersideName, join != null ? !join.nullable() : false));
                        
                    }
                    isLob = false;
                }
                
                // This updates each field as to whether it is an index
                table.updateIndexFields();
            }
                        
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }  
    }

    protected void addCascadeRule(Class<?> cls, Method method, final String rule)
    {
        
        String import1 = "import org.hibernate.annotations.Cascade;";
        String import2 = "import org.hibernate.annotations.CascadeType;";
        try
        {
            File f = new File(srcCodeDir.getAbsoluteFile() + File.separator + cls.getSimpleName() + ".java");
            if (!f.exists())
            {
                log.error("Can't locate source file["+f.getAbsolutePath()+"]");
                return;
            }
    
            List<String>   strLines = FileUtils.readLines(f);
            Vector<String> lines    = new Vector<String>();
            boolean addedImports = false;
            boolean fnd          = false;
            boolean passedImport = false;
            
            String methodName = method.getName()+"(";
            for (String line : strLines)
            {
                if (!passedImport && line.indexOf("import") > -1)
                {
                    passedImport = true;
                }
                
                if (!fnd && line.indexOf(import1) > -1)
                {
                    fnd = true;
                }
                
                /*if (!fnd && !addedImports && passedImport && (line.indexOf("/**") > -1 || line.indexOf("@Entity") > -1))
                {
                    lines.add(import1);
                    lines.add(import2);
                    lines.add("");
                    addedImports = true;
                }*/
                
                if (line.indexOf(methodName) > -1 && line.indexOf("public") > -1)
                {
                    lines.add(rule);
                }
                lines.add(line);
            }
            
            FileUtils.writeLines(f, lines);
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

        
        
    }

    
    /**
     * @param className
     * @param tableList
     */
    @SuppressWarnings("unchecked")
    protected void processCascadeAddCascade(final String className, final List<Table> tableList)
    {
        //System.out.println(className);
        try
        {
            Class classObj = Class.forName(packageName + "." + className);
            
            Table   table       = null; 
            String  tableName   = null;

            if (classObj.isAnnotationPresent(javax.persistence.Table.class))
            {
                Vector<TableIndex> indexes = new Vector<TableIndex>();
                
                javax.persistence.Table tableAnno = (javax.persistence.Table)classObj.getAnnotation(javax.persistence.Table.class);
                tableName = tableAnno.name();
                
                org.hibernate.annotations.Table hiberTableAnno = (org.hibernate.annotations.Table)classObj.getAnnotation(org.hibernate.annotations.Table.class);
                if (hiberTableAnno != null)
                {
                    for (Index index : hiberTableAnno.indexes())
                    {
                        indexes.add(new TableIndex(index.name(), index.columnNames()));
                    }
                }
                
                table = createTable(packageName + "." + className, tableName);
                table.setIndexes(indexes);
                tableList.add(table);
            }
            
            if (table != null)
            {
                
                for (Method method : classObj.getMethods())
                {
                    String methodName = method.getName();
                    if (!methodName.startsWith("get") || method.isAnnotationPresent(javax.persistence.Transient.class))
                    {
                        continue;
                    }
                    
                    String thisSideName = getFieldNameFromMethod(method);

                    if (method.isAnnotationPresent(javax.persistence.ManyToOne.class))
                    {
                        if (!method.isAnnotationPresent(org.hibernate.annotations.Cascade.class))
                        {
                            System.out.println("Missing Cascade["+method.getName()+"]");
                            missing++;
                            //addCascadeRule(classObj, method, "    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.LOCK })");
                        }

                        
                    } else if (method.isAnnotationPresent(javax.persistence.ManyToMany.class))
                    {
                        if (!method.isAnnotationPresent(org.hibernate.annotations.Cascade.class))
                        {
                            System.out.println("Missing Cascade["+method.getName()+"]");
                            missing++;
                            //addCascadeRule(classObj, method, "    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.LOCK })");
                        }
                        
                    } else if (method.isAnnotationPresent(javax.persistence.OneToMany.class))
                    {
                        if (!method.isAnnotationPresent(org.hibernate.annotations.Cascade.class))
                        {
                            System.out.println("Missing Cascade["+method.getName()+"]");
                            missing++;
                            addCascadeRule(classObj, method, "    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })");

                        }
                        
                    } else if (method.isAnnotationPresent(javax.persistence.OneToOne.class))
                    {
                        if (!method.isAnnotationPresent(org.hibernate.annotations.Cascade.class))
                        {
                            //System.out.println("Missing Cascade["+method.getName()+"]");
                            missing++;
                        }
                        
                    }
                }
                
                // This updates each field as to whether it is an index
                table.updateIndexFields();
            }
                        
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }  
    }
    
    protected void removeCascadeRule(Class<?> cls, Method method)
    {
        
        try
        {
            File f = new File(srcCodeDir.getAbsoluteFile() + File.separator + cls.getSimpleName() + ".java");
            if (!f.exists())
            {
                log.error("Can't locate source file["+f.getAbsolutePath()+"]");
                return;
            }
    
            List<String>   strLines = FileUtils.readLines(f);
            Vector<String> lines    = new Vector<String>();
            
            String methodName = method.getName()+"(";
            int inx = 0;
            for (String line : strLines)
            {
                
                if (line.indexOf(methodName) > -1 && line.indexOf("public") > -1)
                {
                    int i = inx;
                    int stop = i - 10;
                    System.out.println("["+strLines.get(i)+"]");
                    while (!StringUtils.contains(strLines.get(i), "@Cascade") && i > stop)
                    {
                        i--;
                        System.out.println("["+strLines.get(i)+"]");
                    }
                    
                    if (i < stop || StringUtils.contains(strLines.get(i), "@Cascade"))
                    {
                        lines.remove(i);
                    } else
                    {
                        int x = 0;
                        x++;
                    }
                }
                lines.add(line);
                inx++;
            }
            
            FileUtils.writeLines(f, lines);
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * @param className
     * @param tableList
     */
    @SuppressWarnings("unchecked")
    protected void processCascade(final String className, final List<Table> tableList)
    {
        //System.out.println(className);
        try
        {
            Class classObj = Class.forName(packageName + "." + className);
            
            Table   table       = null; 
            String  tableName   = null;

            if (classObj.isAnnotationPresent(javax.persistence.Table.class))
            {
                for (Method method : classObj.getMethods())
                {
                    String methodName = method.getName();
                    if (!methodName.startsWith("get") || method.isAnnotationPresent(javax.persistence.Transient.class))
                    {
                        continue;
                    }
                    
                    if (method.isAnnotationPresent(javax.persistence.ManyToOne.class))
                    {
                        if (method.isAnnotationPresent(org.hibernate.annotations.Cascade.class))
                        {
                            System.out.println("Missing Cascade["+method.getName()+"]");
                            missing++;
                            removeCascadeRule(classObj, method);
                        }
                    }
                }
            }
                        
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }  
    }
    
   
    /**
     * Reads in hbm files and generates datamodel tree.
     * @return List datamodel tree
     */
    @SuppressWarnings({ "unchecked", "cast" })
    public List<Table> generateDatamodelTree(final List<Table> tableList, final String dataModelPath)
    {
        try
        {
            log.debug("Preparing to read in DataModel Classes files from  path: " + dataModelPath);
            
            srcCodeDir = new File(dataModelPath);

            String path = srcCodeDir.getAbsolutePath();
            log.info(path);
            //dir = new File(path.substring(0, path.lastIndexOf(File.separator)));

            // This filter only returns directories
            FileFilter fileFilter = new FileFilter()
            {
                public boolean accept(File file)
                {
                    return file.toString().indexOf(".java") != -1;
                }
            };
            
            String PACKAGE = "package ";
            String CLASS   = "public class ";
            
            File[] files = srcCodeDir.listFiles(fileFilter);
            int count = 0;
            for (File file : files)
            {
                
                
                log.debug("Reading    " + file.getAbsolutePath());
                List<?> lines = FileUtils.readLines(file);
                count++;
                log.debug("Processing " + count + " of " + lines.size() + "  " + file.getAbsolutePath());

                String  className   = null;
                
                for (Object lineObj : lines)
                {
                    String line = ((String)lineObj).trim();
                    //System.out.println(line);
                    if (line.startsWith(PACKAGE))
                    {
                        packageName = line.substring(PACKAGE.length(), line.length()-1);
                    }
                    
                    if (line.startsWith(CLASS))
                    {
                        int eInx = line.substring(CLASS.length()).indexOf(' ') + CLASS.length();
                        if (eInx > -1)
                        {
                            className = line.substring(CLASS.length(), eInx);
                        }
                        break;
                    }
                }
                
                if (className != null)
                {
                    processClass(className, tableList);
                    
                    // These were used for correcting Cascading rules
                    // Eventually these can be removed along with the methods.
                    //processCascade(className, tableList);
                    //processCascadeAddCascade(className, tableList);
                }
            }
            System.out.println(missing);
            return tableList;

        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.fatal(ex);
        }
        return null;
    }


    /**
     * Gets column name.
     * @param element the element
     * @return the name of the column
     */
    public String getColumnName(final Element element)
    {
        String columnName = null;
        for (Iterator<?> i2 = element.elementIterator("column"); i2.hasNext();)
        {
            Element element1 = (Element) i2.next();
            columnName = element1.attributeValue("name");
        }
        return columnName;
    }

    /**
     * Takes a list and prints out datamodel file using betwixt.
     * @param classesList the class list
     */
    public boolean writeTree(final List<?> classesList)
    {

        try
        {
            if (classesList == null)
            {
                log.error("Datamodel information is null - datamodel file will not be written!!");
                return false;
            }
            log.info("writing data model tree to file: " + DatamodelHelper.getDatamodelFilePath());
            
            //Element root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath(datamodelOutputFileName)));
            File file = new File(DatamodelHelper.getDatamodelFilePath());
            FileWriter fw = new FileWriter(file);
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fw.write("<!-- \n");
            fw.write("    Do Not Edit this file!\n");
            fw.write("    Run DatamodelGenerator \n");
            Date date = new Date();
            fw.write("    Generated: "+date.toString()+"\n");
            fw.write("-->\n");
            
            //using betwixt for writing out datamodel file.  associated .betwixt files allow you to map and define 
            //output format of attributes in xml file.
            BeanWriter      beanWriter    = new BeanWriter(fw);
            XMLIntrospector introspector = beanWriter.getXMLIntrospector();
            
            introspector.getConfiguration().setWrapCollectionsInElement(false);
            
            beanWriter.getBindingConfiguration().setMapIDs(false);
            beanWriter.setWriteEmptyElements(false);
            beanWriter.enablePrettyPrint();
            beanWriter.write("database", classesList);
            
            fw.close();
            
            return true;
            
        } catch (Exception ex)
        {
            log.error("error writing writeTree", ex);
            return false;
        }
    }

    /**
     * Reads in file that provides listing of tables with their respective Id's and default views.
     * @return boolean true if reading of tableId file was successful.
     */
    private boolean readTableMetadataFromFile(final String tableIdListingFilePath)
    {
        log.info("Preparing to read in Table and TableID listing from file: " + tableIdListingFilePath);
        try
        {
            File tableIdFile = new File(tableIdListingFilePath);
            FileInputStream fileInputStream = new FileInputStream(tableIdFile);
            SAXReader reader = new SAXReader();
            reader.setValidation(false);
            org.dom4j.Document doc = reader.read(fileInputStream);
            Element root = doc.getRootElement();
            Element dbNode = (Element) root.selectSingleNode("database");
            if (dbNode != null)
            {
                for (Iterator<?> i = dbNode.elementIterator("table"); i.hasNext();)
                {
                    Element element     = (Element)i.next();
                    String tablename    = element.attributeValue("name");
                    String defaultView  = element.attributeValue("view");
                    String id           = element.attributeValue("id");
                    boolean isQuery     = XMLHelper.getAttr(element, "query", false);
                    
                    String busRule      = "";
                    Element brElement = (Element)element.selectSingleNode("businessrule");
                    if (brElement != null)
                    {
                        busRule = brElement.getTextTrim();
                    }
                    log.debug("Creating TableMetaData and putting in tblMetaDataHashtable for name: " + tablename + " id: " + id + " defaultview: " + defaultView);
                    
                     tblMetaDataHash.put(tablename, new TableMetaData(id, defaultView, createDisplay(element), isQuery, busRule));
                    
                }
                
            } else
            {
                log.debug("Ill-formatted file for reading in Table and TableID listing.  Filename:"
                        + tableIdFile.getAbsolutePath());
            }
            fileInputStream.close();
            return true;
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.fatal(ex);
        }
        return false;

    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        System.out.println("Starting...");
        List<Table>        tableList              = new ArrayList<Table>(100);
        DatamodelGenerator datamodelWriter        = new DatamodelGenerator();
        String             tableIdListingFilePath = DatamodelHelper.getTableIdFilePath();
        
        if (datamodelWriter.readTableMetadataFromFile(tableIdListingFilePath))
        {
            String dmSrc = DatamodelHelper.getDataModelSrcDirPath();
            tableList    = datamodelWriter.generateDatamodelTree(tableList, dmSrc);
            
            // Sort all the elements by class name
            Collections.sort(tableList);

            boolean didWrite = datamodelWriter.writeTree(tableList);
            if (!didWrite)
            {
                log.error("Failed to write out datamodel document");
            }
        } else
        {
            log.error("Could not find table/ID listing file for input ");
        }
        log.info("Done.");
        System.out.println("Done.");
    }

}
