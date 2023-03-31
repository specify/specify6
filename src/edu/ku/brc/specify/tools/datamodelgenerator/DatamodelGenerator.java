/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.persistence.CascadeType;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.dbsupport.AttributeIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace;
import edu.ku.brc.specify.tools.schemalocale.LocalizableItemIFace;
import edu.ku.brc.specify.tools.schemalocale.LocalizableStrIFace;
import edu.ku.brc.specify.tools.schemalocale.LocalizerBasePanel;
import edu.ku.brc.specify.tools.schemalocale.SchemaLocalizerXMLHelper;
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
    enum RelType {OneToMany, OneToOne, ManyToOne, ManyToMany, ZeroOrOne}
    
    private static final Logger log = Logger.getLogger(DatamodelGenerator.class);
    
    protected static final boolean DEBUG = false;

    protected Hashtable<String, TableMetaData> tblMetaDataHash = new Hashtable<String, TableMetaData>();
    
    //protected Vector<SpLocaleContainer> descTableList = new Vector<SpLocaleContainer>();
    
    protected File         srcCodeDir   = null;
    protected String       packageName  = null;
    protected int          missing      = 0;
    
    protected SchemaLocalizerXMLHelper schemaLocalizer          = null;
    
    protected Hashtable<String, String> abbrvHash = new Hashtable<String, String>();
    
    protected boolean      includeDesc       = false;
    protected boolean      doRelsToZeroToOne = true;
    protected boolean      doPT              = true;
    protected boolean      showDescErrors    = false;
    protected boolean      showDebug         = false;

    /**
     * 
     */
    public DatamodelGenerator(final boolean includeDesc)
    {
        this.includeDesc = includeDesc;
        
        readDescriptions();
    }
    
    protected void readDescriptions()
    {
        if (includeDesc)
        {
            if (doPT)
            {
                SchemaI18NService.setCurrentLocale(new Locale("pt", "", ""));
            }
            
            schemaLocalizer = new SchemaLocalizerXMLHelper(SpLocaleContainer.CORE_SCHEMA, DBTableIdMgr.getInstance());
            includeDesc = schemaLocalizer.load(true);
            
            //descTableList = schemaLocalizer.getSpLocaleContainers();
        }
    }
    
    /**
     * @param tableName
     * @return
     */
    protected Desc getTableDesc(final String tableName)
    {
        LocalizableContainerIFace container = schemaLocalizer.getContainer(tableName);
        if (container != null)
        {
            LocalizableStrIFace d = LocalizerBasePanel.getDescForCurrLocale(container);
            if (d != null)
            {
                Desc desc = new Desc(d.getText(), d.getCountry(), d.getLanguage(), d.getVariant());
                return desc;
            }
            if (showDescErrors) log.error("No Desc for Table["+tableName+"]");
        } else
        {
            log.error("No Table["+tableName+"]");
        }
            
        return null;
    }
    
    /**
     * @param tableName
     * @return
     */
    protected Name getTableNameDesc(final String tableName)
    {
        LocalizableContainerIFace container = schemaLocalizer.getContainer(tableName);
        if (container != null)
        {
            LocalizableStrIFace dn = LocalizerBasePanel.getNameDescForCurrLocale(container);
            if (dn != null)
            {
                Name nm = new Name(dn.getText(), dn.getCountry(), dn.getLanguage(), dn.getVariant());
                return nm;
            }
            log.error("No NameDesc for ["+tableName+"]");
        } else
        {
            log.error("No Table["+tableName+"]");
        }
        return null;
    }
    
    
    /**
     * @param tableName
     * @return
     */
    protected Desc getFieldDesc(final String tableName, final String fieldName)
    {
        LocalizableContainerIFace container = schemaLocalizer.getContainer(tableName);
        if (container != null)
        {
            LocalizableItemIFace item = container.getItemByName(fieldName);
            if (item != null)
            {
                LocalizableStrIFace d = LocalizerBasePanel.getDescForCurrLocale(item);
                if (d != null)
                {
                    Desc desc = new Desc(d.getText(), d.getCountry(), d.getLanguage(), d.getVariant());
                    return desc;
                }
                if (showDescErrors) log.error("No Desc for ["+tableName+"] Field["+fieldName+"]");
            } else
            {
                log.error("No Field ["+tableName+"] Field["+fieldName+"]");
            }
        } else
        {
            log.error("No Table["+tableName+"] Field["+fieldName+"]");
        }
            
        return null;
    }
    
    /**
     * @param tableName
     * @param fieldName
     * @return
     */
    protected Name getFieldNameDesc(final String tableName, final String fieldName)
    {
        LocalizableContainerIFace container = schemaLocalizer.getContainer(tableName);
        if (container != null)
        {
            LocalizableItemIFace item = container.getItemByName(fieldName);
            if (item != null)
            {
                LocalizableStrIFace dn = LocalizerBasePanel.getNameDescForCurrLocale(item);
                if (dn != null)
                {
                    Name nm = new Name(dn.getText(), dn.getCountry(), dn.getLanguage(), dn.getVariant());
                    return nm;
                }
                log.error("No Name for ["+tableName+"] Field["+fieldName+"]");
            } else
            {
                log.error("No Field ["+tableName+"] Field["+fieldName+"]");
            }
        } else
        {
            log.error("No Table["+tableName+"] Field["+fieldName+"]");
        }
            
        return null;
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
                return new Display(fdElement.attributeValue("view"), 
                                   fdElement.attributeValue("dataobjformatter"), 
                                   fdElement.attributeValue("uiformatter"), 
                                   fdElement.attributeValue("searchdlg"), 
                                   fdElement.attributeValue("newobjdlg"));
            }
        }
        return null;
    }
    
    private Vector<FieldAlias> createFieldAliases(final Element element)
    {
        if (element != null)
        {
            Vector<FieldAlias> aliases = new Vector<FieldAlias>();
            List<?> items = element.selectNodes("fieldaliases/field");
            if (items.size() > 0)
            {
                for (Iterator<?> iter = items.iterator(); iter.hasNext(); )
                {
                    Element    faItem = (Element)iter.next();
                    FieldAlias fa     = new FieldAlias(XMLHelper.getAttr(faItem, "vname", null),
                                                       XMLHelper.getAttr(faItem, "aname", null));
                    aliases.add(fa);
                }
                return aliases;
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
        
        Table tbl = new Table(className, 
                              tableName, 
                              null, 
                              tableMetaData.getId(), 
                              tableMetaData.getDisplay(),
                              tableMetaData.getFieldAliase(), 
                              tableMetaData.isSearchable(), 
                              tableMetaData.getBusinessRule(),
                              tableMetaData.getAbbrv());
        tbl.setLikeManyToOneHash(tableMetaData.getLikeManyToOneHash());
        return tbl;
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
        if (name.startsWith("gGBN_")) {
        	name = name.replace("gGBN_","GGBN_");
        }
        return name;
    }
    
    /**
     * @param method
     * @param type
     * @param joinCol
     * @return
     */
    public Relationship createRelationship(final Method method, 
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
            len = retType.equals("java.lang.String") ? Integer.toString(col.length()) : (col.length() != 255 ? Integer.toString(col.length()) : "");
            
        } else
        {
            retType = isLob ? "text" : getReturnType(method);
            if (retType.equals("int"))
            {
                retType = "java.lang.Integer";
            }
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatamodelGenerator.class, ex);
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatamodelGenerator.class, ex);
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
    @SuppressWarnings("cast")
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
    @SuppressWarnings("cast")
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
                                             @SuppressWarnings("unused")
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
        String result = methodName.substring(0,1).toLowerCase() + methodName.substring(1, methodName.length());
        return result;
    }
    
    /**
     * @param className
     * @param tableList
     */
    @SuppressWarnings("cast")
    protected void processClass(final String className, final List<Table> tableList)
    {
        try
        {
            Class<?> classObj = Class.forName(packageName + "." + className);
            
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
                    //System.out.println("Table Indexes: ");
                    for (Index index : hiberTableAnno.indexes())
                    {
                        //System.out.println("  "+index.name() + "  "+ index.columnNames());
                        indexes.add(new TableIndex(index.name(), index.columnNames()));
                    }
                }
                
                table = createTable(packageName + "." + className, tableName);
                if (includeDesc)
                {
                    table.setDesc(getTableDesc(tableName));
                    table.setNameDesc(getTableNameDesc(tableName));
                }
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
                    
                    Type     type = method.getGenericReturnType();
                    Class<?> typeClass;
                    if (type instanceof Class<?>)
                    {
                        typeClass = (Class<?>)type;
                        
                    } else if (type instanceof ParameterizedType)
                    {
                        typeClass = null;
                        for (Type t : ((ParameterizedType)type).getActualTypeArguments())
                        {
                            if (t instanceof Class<?>)
                            {
                                typeClass = (Class<?>)t;
                            }
                        }
                    } else
                    {
                        if (!method.getName().equals("getDataObj") && !method.getName().equals("getTreeRootNode"))
                        {
                            log.warn("Not handled: "+type);
                        }
                        typeClass = null;
                    }
                    
                    // rods 07/10/08 - Used to skip all relationships that point to themselves
                    // that works now and is needed.
                    if (typeClass == null || typeClass.isInterface() //general fix for problems with interfaces for std java compiler.
                        /*typeClass == AttributeIFace.class ||
                        typeClass == PickListItemIFace.class || 
                        typeClass == RecordSetItemIFace.class*/)
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
                            Field field = createField(method, (javax.persistence.Column)method.getAnnotation(javax.persistence.Column.class), isLob);
                            if (includeDesc)
                            {
                                field.setDesc(getFieldDesc(tableName, field.getName()));
                                field.setNameDesc(getFieldNameDesc(tableName, field.getName()));
                            }
                            
                            if (typeClass == java.util.Calendar.class)
                            {
                                String mName = method.getName() + "Precision";
                                for (Method mthd : classObj.getMethods())
                                {
                                    if (mthd.getName().equals(mName))
                                    {
                                        field.setPartialDate(true);
                                        field.setDatePrecisionName(field.getName()+ "Precision");
                                        break;
                                    }
                                }
                            }
                            table.addField(field);
                        }
                        
                    } else if (method.isAnnotationPresent(javax.persistence.ManyToOne.class))
                    {
                        javax.persistence.ManyToOne oneToMany = (javax.persistence.ManyToOne)method.getAnnotation(javax.persistence.ManyToOne.class);
                        
                        boolean isSave = false;
                        for (CascadeType ct : oneToMany.cascade())
                        {
                            if (ct == CascadeType.ALL || ct == CascadeType.PERSIST)
                            {
                                isSave = true;
                            }
                        }
                        isSave = !isSave ? isOKToSave(method) : isSave;
                        
                        String otherSideName = getRightSideForManyToOne(classObj, typeClass, thisSideName);
                        
                        javax.persistence.JoinColumn join = method.isAnnotationPresent(javax.persistence.JoinColumn.class) ? (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class) : null;
                        if (join != null)
                        {
                            //String othersideName = typeClass == null ? "" : getOthersideName(classObj, typeClass, thisSideName, RelType.OneToMany);
                            Relationship rel = createRelationship(method, "many-to-one", join, otherSideName, join != null ? !join.nullable() : false);
                            table.addRelationship(rel);
                            rel.setSave(isSave);
                            
                            if (includeDesc)
                            {
                                rel.setDesc(getFieldDesc(tableName, rel.getRelationshipName()));
                                rel.setNameDesc(getFieldNameDesc(tableName, rel.getRelationshipName()));
                            }
                            
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
                        
                        boolean isSave = false;
                        for (CascadeType ct : manyToMany.cascade())
                        {
                            if (ct == CascadeType.ALL || ct == CascadeType.PERSIST)
                            {
                                isSave = true;
                            }
                        }
                        isSave = !isSave ? isOKToSave(method) : isSave;
                        
                        javax.persistence.JoinColumn join = method.isAnnotationPresent(javax.persistence.JoinColumn.class) ? (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class) : null;
                        Relationship rel = createRelationship(method, "many-to-many", join, othersideName, join != null ? !join.nullable() : false);
                        rel.setLikeManyToOne(table.getIsLikeManyToOne(rel.getRelationshipName()));
                        rel.setSave(isSave);
                        
                        table.addRelationship(rel);
                        if (includeDesc)
                        {
                            rel.setDesc(getFieldDesc(tableName, rel.getRelationshipName()));
                            rel.setNameDesc(getFieldNameDesc(tableName, rel.getRelationshipName()));
                        }
                        
                        javax.persistence.JoinTable joinTable = method.getAnnotation(javax.persistence.JoinTable.class);
                        if (joinTable != null)
                        {
                            rel.setJoinTableName(joinTable.name());
                        }
                        
                    } else if (method.isAnnotationPresent(javax.persistence.OneToMany.class))
                    {
                        javax.persistence.OneToMany oneToMany = (javax.persistence.OneToMany)method.getAnnotation(javax.persistence.OneToMany.class);
                        
                        String  othersideName = oneToMany.mappedBy();
                        if (StringUtils.isEmpty(othersideName))
                        {
                            // This Should never happen
                            othersideName = getRightSideForOneToMany(classObj, typeClass, oneToMany.mappedBy());
                        }
                        
                        boolean isSave = false;
                        for (CascadeType ct : oneToMany.cascade())
                        {
                            if (ct == CascadeType.ALL || ct == CascadeType.PERSIST)
                            {
                                isSave = true;
                            }
                        }
                        isSave = !isSave ? isOKToSave(method) : isSave;
                        
                        javax.persistence.JoinColumn join = method.isAnnotationPresent(javax.persistence.JoinColumn.class) ? (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class) : null;
                        Relationship rel = createRelationship(method, "one-to-many", join, othersideName, join != null ? !join.nullable() : false);
                        rel.setLikeManyToOne(table.getIsLikeManyToOne(rel.getRelationshipName()));
                        rel.setSave(isSave);
                        table.addRelationship(rel);
                        if (includeDesc)
                        {
                            rel.setDesc(getFieldDesc(tableName, rel.getRelationshipName()));
                            rel.setNameDesc(getFieldNameDesc(tableName, rel.getRelationshipName()));
                        }
                        
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

                        boolean isSave = false;
                        for (CascadeType ct : oneToOne.cascade())
                        {
                            if (ct == CascadeType.ALL || ct == CascadeType.PERSIST)
                            {
                                isSave = true;
                            }
                        }
                        isSave = !isSave ? isOKToSave(method) : isSave;

                        javax.persistence.JoinColumn join = method.isAnnotationPresent(javax.persistence.JoinColumn.class) ? (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class) : null;
                        Relationship rel = createRelationship(method, "one-to-one", join, othersideName, join != null ? !join.nullable() : false);
                        rel.setSave(isSave);
                        table.addRelationship(rel);
                        if (includeDesc)
                        {
                            rel.setDesc(getFieldDesc(tableName, rel.getRelationshipName()));
                            rel.setNameDesc(getFieldNameDesc(tableName, rel.getRelationshipName()));
                        }
                        

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
    
    /**
     * @param method
     * @return
     */
    @SuppressWarnings("cast")
    protected boolean isOKToSave(final Method method)
    {
        org.hibernate.annotations.Cascade hibCascade = (org.hibernate.annotations.Cascade)method.getAnnotation(org.hibernate.annotations.Cascade.class);
        if (hibCascade != null)
        {
            for (org.hibernate.annotations.CascadeType ct : hibCascade.value())
            {
                if (ct == org.hibernate.annotations.CascadeType.ALL || 
                    ct == org.hibernate.annotations.CascadeType.PERSIST || 
                    ct == org.hibernate.annotations.CascadeType.SAVE_UPDATE)
                {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected void addCascadeRule(Class<?> cls, Method method, final String rule)
    {
        
        String import1 = "import org.hibernate.annotations.Cascade;";
        //String import2 = "import org.hibernate.annotations.CascadeType;";
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
            //boolean addedImports = false;
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatamodelGenerator.class, ex);
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
            Class<?> classObj = Class.forName(packageName + "." + className);
            
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
                    
                    //String thisSideName = getFieldNameFromMethod(method);

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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatamodelGenerator.class, ex);
            ex.printStackTrace();
        }  
    }
    
    @SuppressWarnings("unchecked")
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
                    }
                }
                lines.add(line);
                inx++;
            }
            
            FileUtils.writeLines(f, lines);
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatamodelGenerator.class, ex);
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
            Class<?> classObj = Class.forName(packageName + "." + className);
            
            //Table   table       = null; 
            //String  tableName   = null;

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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatamodelGenerator.class, ex);
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
                if (showDebug) log.debug("Reading    " + file.getAbsolutePath());
                List<?> lines = FileUtils.readLines(file);
                count++;
                if (showDebug) log.debug("Processing " + count + " of " + files.length + "  " + file.getAbsolutePath());

                String  className   = null;
                
                for (Object lineObj : lines)
                {
                    String line = ((String)lineObj).trim();
                    //System.out.println(line);
                    if (line.startsWith(PACKAGE))
                    {
                        packageName = line.substring(PACKAGE.length(), line.length()-1);
                    }
                    if (StringUtils.contains(line, CLASS))
                    {
                        String str = line.substring(CLASS.length());
                        while (str.charAt(0) == ' ')
                        {
                            str = str.substring(1);
                        }
                        
                        int eInx = str.indexOf(' ');
                        if (eInx == -1)
                        {
                            className = str;
                        } else
                        {
                            className = str.substring(0, eInx);
                        }
                        break;
                    }
                }
                
                if (className != null)
                {
                    if (!StringUtils.contains(className, "SpUI"))
                    {
                        processClass(className, tableList);
                    }
                    
                    // These were used for correcting Cascading rules
                    // Eventually these can be removed along with the methods.
                    //processCascade(className, tableList);
                    //processCascadeAddCascade(className, tableList);
                } else
                {
                    String fileName = file.getName();
                    if (!StringUtils.contains(fileName, "DataModelObjBase") && 
                        !StringUtils.contains(fileName, "CollectionMember") && 
                        !StringUtils.contains(fileName, "DisciplineMember") && 
                        !StringUtils.contains(fileName, "UserGroupScope") && 
                        !StringUtils.contains(fileName, "Treeable") && 
                        !StringUtils.contains(fileName, "SpLocaleBase") && 
                        !StringUtils.contains(fileName.toLowerCase(), "iface") &&
                        !StringUtils.contains(fileName, "BaseTreeDef") &&
                        !StringUtils.contains(fileName, "TreeDefItemStandardEntry"))
                    {
                        throw new RuntimeException("Couldn't locate class name for "+file.getAbsolutePath());
                    }
                }
            }
            System.out.println(missing);
            return tableList;

        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatamodelGenerator.class, ex);
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
    
    protected void setRelToZeroToOne(Collection<Relationship> rels, final String name)
    {
        for (Relationship rel : rels)
        {
            if (rel.getRelationshipName().equalsIgnoreCase(name))
            {
                rel.setType("zero-to-one");
            }
        }
    }

    protected void adjustRelsForZeroToOne(final Table tbl)
    {
        String shortTableName = StringUtils.substringAfterLast(tbl.getName(), ".");
        if (shortTableName.equals("Locality"))
        {
            setRelToZeroToOne(tbl.getRelationships(), "localityDetails");
            setRelToZeroToOne(tbl.getRelationships(), "geoCoordDetails");
        }
    }
    /**
     * Takes a list and prints out data model file using betwixt.
     * @param classesList the class list
     */
    public boolean writeTree(final List<Table> classesList)
    {
        if (doRelsToZeroToOne)
        {
            for (Table tbl : classesList)
            {
                adjustRelsForZeroToOne(tbl);
            }
        }
        
        try
        {
            if (classesList == null)
            {
                log.error("Datamodel information is null - datamodel file will not be written!!");
                return false;
            }
            log.info("writing data model tree to file: " + DatamodelHelper.getDatamodelFilePath());
            
            File file = DatamodelHelper.getDatamodelFilePath();
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatamodelGenerator.class, ex);
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
        Hashtable<String, Boolean> abbrvHashLocal = new Hashtable<String, Boolean>();
        
        log.info("Preparing to read in Table and TableID listing from file: " + tableIdListingFilePath);
        try
        {
            File               tableIdFile     = new File(tableIdListingFilePath);
            FileInputStream    fileInputStream = new FileInputStream(tableIdFile);
            SAXReader          reader          = new SAXReader();
            reader.setValidation(false);
            org.dom4j.Document doc             = reader.read(fileInputStream);
            Element            root            = doc.getRootElement();
            Element            dbNode          = (Element) root.selectSingleNode("database");
            if (dbNode != null)
            {
                for (Iterator<?> i = dbNode.elementIterator("table"); i.hasNext();)
                {
                    Element element      = (Element)i.next();
                    String tablename     = element.attributeValue("name");
                    String defaultView   = element.attributeValue("view");
                    String id            = element.attributeValue("id");
                    String abbrv         = XMLHelper.getAttr(element, "abbrev", null);
                    boolean isSearchable = XMLHelper.getAttr(element, "searchable", false);
                    
                    if (StringUtils.isNotEmpty(abbrv))
                    {
                        if (abbrvHashLocal.get(abbrv) == null)
                        {
                            abbrvHashLocal.put(abbrv, true);
                        } else
                        {
                            throw new RuntimeException("`abbrev` ["+abbrv+"]  or table["+ tablename +"] ids already in use.");
                        }
                            
                    } else
                    {
                        throw new RuntimeException("`abbrev` is missing or empty for table["+ tablename +"]");
                    }
                    
                    String busRule      = "";
                    Element brElement = (Element)element.selectSingleNode("businessrule");
                    if (brElement != null)
                    {
                        busRule = brElement.getTextTrim();
                    }
                    //log.debug("Creating TableMetaData and putting in tblMetaDataHashtable for name: " + tablename + " id: " + id + " defaultview: " + defaultView);
                    
                    TableMetaData tblMetaData = new TableMetaData(id, 
                                                                  defaultView, 
                                                                  createDisplay(element), 
                                                                  createFieldAliases(element),
                                                                  isSearchable, 
                                                                  busRule,
                                                                  abbrv);
                    tblMetaDataHash.put(tablename, tblMetaData);
                    
                    for (Iterator<?> ir = element.elementIterator("relationship"); ir.hasNext();)
                    {
                        Element relElement = (Element)ir.next();
                        String  relName    = relElement.attributeValue("relationshipname");
                        boolean isLike     = XMLHelper.getAttr(relElement, "likemanytoone", false);
                        tblMetaData.setIsLikeManyToOne(relName, isLike);
                    }
                    
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DatamodelGenerator.class, ex);
            ex.printStackTrace();
            log.fatal(ex);
        }
        return false;

    }
    
    /**
     * @param includeDescArg
     */
    public void process(final String outputFileName)
    {
        if (StringUtils.isNotEmpty(outputFileName))
        {
            DatamodelHelper.setOutputFileName(outputFileName);
        }
        
        System.out.println("Starting...");
        List<Table>        tableList              = new ArrayList<Table>(150);
        String             tableIdListingFilePath = DatamodelHelper.getTableIdFilePath();
        
        if (readTableMetadataFromFile(tableIdListingFilePath))
        {
            String dmSrc = DatamodelHelper.getDataModelSrcDirPath();
            tableList    = generateDatamodelTree(tableList, dmSrc);
            
            // Sort all the elements by class name
            Collections.sort(tableList);

            boolean didWrite = writeTree(tableList);
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

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DatamodelGenerator datamodelWriter = new DatamodelGenerator(false);
        datamodelWriter.process(null);
    }
    
    
}
