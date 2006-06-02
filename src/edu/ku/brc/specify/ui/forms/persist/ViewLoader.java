 /* Filename:    $RCSfile: ViewLoader,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui.forms.persist;

import static edu.ku.brc.specify.helpers.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.specify.helpers.XMLHelper.getAttr;
import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.specify.exceptions.ConfigurationException;
import edu.ku.brc.specify.ui.forms.UIFieldFormatterMgr;
import edu.ku.brc.specify.ui.forms.UIFieldFormatterMgr.Formatter;
import edu.ku.brc.specify.ui.validation.ComboBoxFromQueryFactory;

/**
 * Factory that creates Views from ViewSet files. This class uses the singleton ViewMgr to verify the View Set Name is unique.
 * If it is not unique than it throws an exception.<br> In this case a "form" is really the definition of a form. The form's object hierarchy
 * is used to creates the forms using Swing UI objects. The classes will also be used by the forms editor.
 *
 * @author Rod Spears <rods@ku.edu>
 */
public class ViewLoader
{
    // Statics
    private static final Logger     log = Logger.getLogger(ViewLoader.class);
    private static final ViewLoader instance = new ViewLoader();

    private static final String NAME       = "name";
    private static final String TYPE       = "type";
    private static final String LABEL      = "label";
    private static final String DESC       = "desc";
    private static final String CLASSNAME  = "class";
    private static final String GETTABLE   = "gettable";
    private static final String SETTABLE   = "settable";
    private static final String RESOURCELABELS = "resourcelabels";

    // Data Members
    protected boolean doingResourceLabels = false;
    protected String  viewSetName         = null;

    /**
     * Default Constructor
     *
     */
    protected ViewLoader()
    {
    }

    /**
     * Creates the view
     * @param element the element to build the View from
     * @return the view
     * @throws Exception anything
     */
    public static View createView(final Element element,
                                  final Hashtable<String, ViewDef> viewDefs) throws Exception
    {
        // set a global value while creating this form as to whether the labels are keys to a resource bundle
        // or whether they are the actual label
        instance.doingResourceLabels = getAttr(element, "useresourcelabels", "false").equals("true");

        String   name              = element.attributeValue(NAME);
        String   className         = element.attributeValue(CLASSNAME);
        String   resLabels         = element.attributeValue(RESOURCELABELS);
        String   desc              = getDesc(element);

        View view = new View(instance.viewSetName, name, className, desc, resLabels);

        Element altviews = (Element)element.selectSingleNode("altviews");
        if (altviews != null)
        {
            AltView defaultAltView = null;

            // iterate through child elements of root with element name "foo"
            for ( Iterator i = altviews.elementIterator( "altview" ); i.hasNext(); )
            {
                Element altElement = (Element) i.next();

                String altName      = altElement.attributeValue(NAME);
                String viewDefName  = altElement.attributeValue("viewdef");
                String label        = altElement.attributeValue(LABEL);
                String modeStr      = getAttr(altElement, "mode", "");
                boolean isValidated = getAttr(altElement, "validated", false);
                boolean isDefault   = getAttr(altElement, "default", false);

                AltView.CreationMode mode;
                if (isEmpty(modeStr))
                {
                    mode = AltView.CreationMode.None;

                } else
                {
                    mode = modeStr.equals("edit") ? AltView.CreationMode.Edit : AltView.CreationMode.View;
                }

                ViewDef viewDef = viewDefs.get(viewDefName);
                if (viewDef == null)
                {
                    throw new RuntimeException("View Name["+name+"] refers to a ViewDef that doesn't exist.");
                }

                // Make sure we only have one default view
                if (defaultAltView != null && isDefault)
                {
                    isDefault = false;
                }

                AltView altView = new AltView(view, altName, label, mode, isValidated, isDefault, viewDef);

                if (defaultAltView == null && isDefault)
                {
                    defaultAltView = altView;
                }

                view.addAltView(altView);
            }

            // No default Alt View was indicated, so choose the first one
            if (defaultAltView == null && view.getAltViews() != null)
            {
                view.getAltViews().get(0).setDefault(true);
            }
        }

        return view;
    }

    /**
     * Creates a ViewDef
     * @param element the element to build the ViewDef from
     * @return a viewdef
     * @throws Exception
     */
    public static ViewDef createViewDef(final Element element) throws Exception
    {
        String   name              = element.attributeValue(NAME);
        String   className         = element.attributeValue(CLASSNAME);
        String   gettableClassName = element.attributeValue(GETTABLE);
        String   settableClassName = element.attributeValue(SETTABLE);
        String   desc              = getDesc(element);

        ViewDef.ViewType type;
        try
        {
            type = ViewDef.ViewType.valueOf(element.attributeValue(TYPE));

        } catch (Exception ex)
        {
            log.error("view["+name+"] has illegal type["+element.attributeValue(TYPE)+"]", ex);
            throw ex;
        }

        ViewDef viewDef = null;//new ViewDef(type, name, className, gettableClassName, settableClassName, desc);

        switch (type)
        {
            case form :
                viewDef = createFormViewDef(element, type, name, className, gettableClassName, settableClassName, desc, instance.doingResourceLabels);
                break;

            case table :
                //view = createTableView(element, id, name, className, gettableClassName, settableClassName,
                //                       desc, instance.doingResourceLabels, isValidated);
                break;

            case field :
                //view = createFormView(FormView.ViewType.field, element, id, name, gettableClassName, settableClassName,
                //                      className, desc, instance.doingResourceLabels, isValidated);
               break;
        }
        return viewDef;
    }


    /**
     * Gets the optoinal description text
     * @param element the parent eleemnt of the desc node
     * @return the string of the text or null
     */
    protected static String getDesc(final Element element)
    {
        String desc = null;
        Element descElement = (Element)element.selectSingleNode(DESC);
        if (descElement != null)
        {
            desc = descElement.getTextTrim();
        }
        return desc;
    }

    /**
     * Fill the Vector with all the views from the DOM document
     * @param doc the DOM document conforming to form.xsd
     * @param views the liust to be filled
     * @throws Exception for duplicate view set names or if a Form ID is not unique
     */
    public static String getViews(final Element doc,
                                  final Hashtable<String, View> views,
                                  final Hashtable<String, ViewDef> viewDefs) throws Exception
    {
        instance.viewSetName = doc.attributeValue(NAME);

        Element viewsElement = (Element)doc.selectSingleNode("views");
        if (viewsElement != null)
        {
            for ( Iterator i = viewsElement.elementIterator( "view" ); i.hasNext(); )
            {
                Element  element = (Element) i.next(); // assume element is NOT null, if it is null it will cause an exception
                View     view    = createView(element, viewDefs);
                if (views.get(view.getName()) == null)
                {
                    views.put(view.getName(), view);
                } else
                {
                    String msg = "View Set ["+instance.viewSetName+"] ["+view.getName()+"] is not unique.";
                    log.error(msg);
                    throw new ConfigurationException(msg);
                }
            }
        }

        return instance.viewSetName;
    }

    /**
     * Fill the Vector with all the views from the DOM document
     * @param doc the DOM document conforming to form.xsd
     * @param viewDefs the list to be filled
     * @throws Exception for duplicate view set names or if a ViewDef name is not unique
     */
    public static String getViewDefs(final Element doc, final Hashtable<String, ViewDef> viewDefs) throws Exception
    {
        instance.viewSetName = doc.attributeValue(NAME);

        Element viewDefsElement = (Element)doc.selectSingleNode("viewdefs");
        if (viewDefsElement != null)
        {
            for ( Iterator i = viewDefsElement.elementIterator( "viewdef" ); i.hasNext(); )
            {
                Element  element = (Element) i.next(); // assume element is NOT null, if it is null it will cause an exception
                ViewDef  viewDef = createViewDef(element);
                if (viewDefs.get(viewDef.getName()) == null)
                {
                    viewDefs.put(viewDef.getName(), viewDef);
                } else
                {
                    String msg = "View Set ["+instance.viewSetName+"] ["+viewDef.getName()+"] is not unique.";
                    log.error(msg);
                    throw new ConfigurationException(msg);
                }
            }
        }

        return instance.viewSetName;
    }


    /**
     * Processes all the AltViews
     * @param aFormView the form they should be associated with
     * @param aElement the element to process
     */
    protected static Map<String, String> getEnableRules(final Element element)
    {
        Map<String, String> rulesList = new Hashtable<String, String>();

        if (element != null)
        {
            Element enableRules = (Element)element.selectSingleNode("enableRules");
            if (enableRules != null)
            {
                // iterate through child elements of root with element name "foo"
                for ( Iterator i = enableRules.elementIterator( "rule" ); i.hasNext(); )
                {
                    Element ruleElement = (Element) i.next();
                    String name = getAttr(ruleElement, "name", "");
                    if (isNotEmpty(name))
                    {
                        rulesList.put(name, ruleElement.getTextTrim());
                    } else
                    {
                        throw new RuntimeException("The name is missing for rule["+ruleElement.getTextTrim()+"] is missing.");
                    }
                }
            }
        } else
        {
            log.error("View Set ["+instance.viewSetName+"] element ["+element+"] is null.");
        }
        return rulesList;
    }

    /**
     * Gets the string (or creates one) from a columnDef
     * @param element the DOM lement to process
     * @param attrName the name of the element to go get all the elements (strings) from
     * @return the String representing the column definition for JGoodies
     */
    protected static String createDef(final Element element, final String attrName)
    {
        Element cellDef = (Element)element.selectSingleNode(attrName);
        if (cellDef != null)
        {
            int dup = getAttr(cellDef, "dup", -1);
            if (dup > 0)
            {
                String cellStr = getAttr(cellDef, "cell", null);
                String sepStr  = getAttr(cellDef, "sep", null);
                if (cellStr != null && sepStr != null)
                {
                    return createDuplicateJGoodiesDef(cellStr, sepStr, dup);
                } else
                {
                    throw new RuntimeException("Element ["+element.getName()+"] Cell or Sep is null for 'dup' on column def.");
                }
            } else
            {
                return cellDef.getText();
            }
        } else
        {
            log.error("Element ["+element.getName()+"] must have a columnDef");
        }
        return "";
    }


    /**
     * Returns a resource string if it is suppose to
     * @param label the label or the label key
     * @return Returns a resource string if it is suppose to
     */
    protected static String getResourceLabel(final String label)
    {
        if (isNotEmpty(label))
        {
            return instance.doingResourceLabels  ? getResourceString(label) : label;
        } else
        {
            return "";
        }

    }

    /**
     * Returns a Label from the cell and gets the resource string for it if necessary
     * @param cellElement the cell
     * @param labelId the Id of the resource or the string
     * @return the localized string (if necessary)
     */
    protected static String getLabel(final Element cellElement)
    {
        return getResourceLabel(getAttr(cellElement, LABEL, ""));
    }
    
    
    /**
     * Processes the initilize string as a set of named value pairs where each pair is separated by `;`
     * @param initStr the initialize string to be processed
     * @return the hash of values
     */
    protected static Hashtable<String, String> processInitializeString(final String initStr)
    {
        if (isNotEmpty(initStr))
        {
            Hashtable<String, String> hash = new Hashtable<String, String>();
            
            for (String pair : StringUtils.split(initStr, ";"))
            {
                String[] args = StringUtils.split(pair, "=");
                if (args.length % 2 != 0)
                {
                    log.error("Initialize string["+initStr+"] is an a set of named value pairs separated by `;`");
                } else
                {
                    for (int i=0;i<args.length;i++)
                    {
                        hash.put(args[i], args[i+1]);
                        i++;
                    }
                }
            }
            return hash.size() > 0 ? hash : null;
        }
        return null;
    }

    /**
     * Processes all the rows
     * @param element the parent DOM element of the rows
     * @param cellRows the list the rows are to be added to
     */
    protected static void processRows(Element element, List<FormRow> cellRows)
    {
        Element rowsElement = (Element)element.selectSingleNode("rows");
        if (rowsElement != null)
        {
            for ( Iterator i = rowsElement.elementIterator( "row" ); i.hasNext(); ) {
                Element rowElement = (Element) i.next();

                FormRow formRow = new FormRow();

                for ( Iterator cellIter = rowElement.elementIterator( "cell" ); cellIter.hasNext(); )
                {
                    Element cellElement = (Element)cellIter.next();
                    String  cellId      = getAttr(cellElement, "id", "");
                    String  cellName    = getAttr(cellElement, NAME, "");
                    int     colspan     = getAttr(cellElement, "colspan", 1);
                    int     rowspan     = getAttr(cellElement, "rowspan", 1);

                    FormCell.CellType cellType = FormCell.CellType.valueOf(cellElement.attributeValue(TYPE));
                    FormCell          cell     = null;

                    switch (cellType)
                    {
                        case label:
                            cell = formRow.addCell(new FormCellLabel(cellId, cellName, getLabel(cellElement), getAttr(cellElement, "labelfor", ""), colspan));
                            break;

                        case separator:
                            cell = formRow.addCell(new FormCellSeparator(cellId, cellName, getLabel(cellElement), colspan));
                            break;

                        case field:
                        {
                            String uitype         = getAttr(cellElement, "uitype", "");
                            String format         = getAttr(cellElement, "format", "");
                            String formatName     = getAttr(cellElement, "formatname", "");
                            String uiFieldFormatter = getAttr(cellElement, "uifieldformatter", "");
                            int    cols           = getAttr(cellElement, "cols", 10); // XXX PREF for default width of text field
                            int    rows           = getAttr(cellElement, "rows", 5);  // XXX PREF for default heightof text area
                            String validationType = getAttr(cellElement, "valtype", "Focus");
                            String validationRule = getAttr(cellElement, "validation", "");
                            String initialize     = getAttr(cellElement, "initialize", "");
                            boolean isRequired    = getAttr(cellElement, "isrequired", false);
                            boolean isEncrypted   = getAttr(cellElement, "isencrypted", false);

                            if (isNotEmpty(format) && isNotEmpty(formatName))
                            {
                                //throw new RuntimeException("Both format and formatname cannot both be set! ["+cellName+"]");
                                log.error("Both format and formatname cannot both be set! ["+cellName+"] ignoring format");
                                format = "";
                            }
                            
                            if (cellName.equals("agentAddressByIssuer.agent"))
                            {
                                int x = 0;
                                x++;
                            }

                            Hashtable<String, String> properties = null;
                            
                            String dspUIType;
                            if (uitype.equals("checkbox"))
                            {
                                dspUIType = getAttr(cellElement, "dspuitype", "checkbox");

                            } else if (uitype.equals("textarea"))
                            {
                                dspUIType = getAttr(cellElement, "dspuitype", "dsptextarea");

                            } else if (uitype.equals("list"))
                            {
                                dspUIType = getAttr(cellElement, "dspuitype", "list");

                            } else if (uitype.equals("querycbx"))
                            {
                                dspUIType = getAttr(cellElement, "dspuitype", "textfieldinfo");
                                
                                properties = processInitializeString(initialize);
                                String fmtName = ComboBoxFromQueryFactory.getFormatName(properties.get("name"));
                                if (isNotEmpty(fmtName))
                                {
                                    formatName = fmtName;
                                }

                            } else if (uitype.equals("formattedtext"))
                            {
                                validationRule = getAttr(cellElement, "validation", "formatted");
                                dspUIType = getAttr(cellElement, "dspuitype", "dsptextfield");
                                if (isNotEmpty(uiFieldFormatter))
                                {
                                    Formatter formatter = UIFieldFormatterMgr.getFormatter(uiFieldFormatter);
                                    if (formatter == null)
                                    {
                                        log.info("Couldn't find formatter["+uiFieldFormatter+"]");
                                        uiFieldFormatter = "";
                                        uitype = "text";
                                    }
                                }

                            } else if (uitype.equals("image"))
                            {
                                dspUIType = getAttr(cellElement, "dspuitype", "image");
                                
                            } else if (uitype.equals("url"))
                            {
                                dspUIType = getAttr(cellElement, "dspuitype", uitype);
                                properties = processInitializeString(initialize);

                            } else if (uitype.equals("progress"))
                            {
                                dspUIType = getAttr(cellElement, "dspuitype", uitype);
                                
                            } else if (uitype.equals("button"))
                            {
                                dspUIType = getAttr(cellElement, "dspuitype", uitype);
                                
                            } else if (uitype.equals("plugin"))
                            {
                                dspUIType = getAttr(cellElement, "dspuitype", uitype);
                                properties = processInitializeString(initialize);

                            } else
                            {
                                dspUIType = getAttr(cellElement, "dspuitype", "dsptextfield");
                            }


                            // check to see see if the validation is a node in the cell
                            if (isEmpty(validationRule))
                            {
                                Element valNode = (Element)cellElement.selectSingleNode("validation");
                                if (valNode != null)
                                {
                                    String str = valNode.getTextTrim();
                                    if (isNotEmpty(str))
                                    {
                                        validationRule = str;
                                    }
                                }
                            }

                            FormCellField field = new FormCellField(FormCell.CellType.field, cellId, 
                                                                    cellName, uitype, dspUIType, format, formatName, uiFieldFormatter, isRequired,
                                                                    cols, rows, colspan, rowspan, validationType, validationRule, isEncrypted);
                            field.setLabel(getAttr(cellElement, "label", ""));
                            field.setPickListName(getAttr(cellElement, "picklist", ""));
                            field.setChangeListenerOnly(getAttr(cellElement, "changesonly", true) && !isRequired);
                            field.setInitialize(initialize);
                            field.setProperties(properties);

                            cell = formRow.addCell(field);
                        } break;

                        case command:
                        {
                            cell =  formRow.addCell(new FormCellCommand(cellId, cellName,
                                                                getLabel(cellElement),
                                                                getAttr(cellElement, "commandtype", ""),
                                                                getAttr(cellElement, "action", "")));
                        } break;

                        case panel:
                        {
                            FormCellPanel cellPanel = new FormCellPanel(cellId, cellName,
                                                                        getAttr(cellElement, "paneltype", ""),
                                                                        getAttr(cellElement, "coldef", "p"),
                                                                        getAttr(cellElement, "rowdef", "p"),
                                                                        colspan, rowspan);
                            processRows(cellElement, cellPanel.getRows());
                            cell = formRow.addCell(cellPanel);
                        } break;

                        case subview:
                        {
                            String vsName = cellElement.attributeValue("viewsetname");
                            if (StringUtils.isEmpty(vsName))
                            {
                                vsName = instance.viewSetName;
                            }

                            cell = formRow.addCell(new FormCellSubView(cellId, cellName,
                                                   vsName,
                                                   cellElement.attributeValue("viewname"),
                                                   cellElement.attributeValue("class"),
                                                   colspan,
                                                   rowspan,
                                                   getAttr(cellElement, "single", false)));

                        }
                        break;
                    } // switch
                    cell.setIgnoreSetGet(getAttr(cellElement, "ignore", false));
                }
                cellRows.add(formRow);
            }
        }

    }

    /**
     * @param type the type of form to be built
     * @param element the DOM element for building the form
     * @param id the id of the form
     * @param name the name of the form
     * @param className the class name of the data object
     * @param gettableClassName the class name of the getter
     * @param settableClassName the class name of the setter
     * @param desc the description
     * @param resLabels indicates whether the labels are really resource identifiers so the labels should come froma resource bundle
     * @param isValidated whether to turn on validation
     * @return a form view of type "form"
     */
    protected static FormViewDef createFormViewDef(final Element element,
                                                   final ViewDef.ViewType type,
                                                   final String  name,
                                                   final String  className,
                                                   final String  gettableClassName,
                                                   final String  settableClassName,
                                                   final String  desc,
                                                   final boolean resLabels)
    {
        FormViewDef formView = new FormViewDef(type, name, className, gettableClassName, settableClassName, desc);

        formView.setColumnDef(createDef(element, "columnDef"));
        formView.setRowDef(createDef(element, "rowDef"));
        formView.setEnableRules(getEnableRules(element));

        processRows(element, formView.getRows());

        return formView;
    }



    /**
     * Creates a Table Form View
     * @param type the type of form to be built
     * @param element the DOM element for building the form
     * @param id the id of the form
     * @param name the name of the form
     * @param className the class name of the data object
     * @param gettableClassName the class name of the getter
     * @param settableClassName the class name of the setter
     * @param desc the description
     * @param resLabels indicates whether the labels are really resource identifiers so the labels should come froma resource bundle
     * @param isValidated whether to turn on validation
     * @return a form view of type "table"
     */
    protected static TableViewDef createTableView(final Element element,
                                                   final int     id,
                                                   final String  name,
                                                   final String  className,
                                                   final String  gettableClassName,
                                                   final String  settableClassName,
                                                   final String  desc,
                                                   final boolean resLabels,
                                                   final boolean isValidated)
    {
        TableViewDef tableView = new TableViewDef( name, className, gettableClassName, settableClassName, desc);

        //tableView.setResourceLabels(resLabels);

        Element columns = (Element)element.selectSingleNode("columns");
        if (columns != null)
        {
            for ( Iterator i = columns.elementIterator( "column" ); i.hasNext(); ) {
                Element colElement = (Element) i.next();

                FormColumn column = new FormColumn(colElement.attributeValue(NAME),
                        colElement.attributeValue(LABEL),
                        getAttr(colElement, "format", "")
                        );

                tableView.addColumn(column);
            }
        }

        return tableView;
    }

    /**
     * Save out a viewSet to a file
     * @param viewSet the viewSet to save
     * @param filename the filename (full path) as to where to save it
     */
    public static void save(final ViewSet viewSet, final String filename)
    {
        try
        {
            Vector<ViewSet> viewsets = new Vector<ViewSet>();
            viewsets.add(viewSet);

            File       file = new File(filename);
            FileWriter fw   = new FileWriter(file);

            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            BeanWriter beanWriter = new BeanWriter(fw);
            XMLIntrospector introspector = beanWriter.getXMLIntrospector();
            introspector.getConfiguration().setWrapCollectionsInElement(false);
            beanWriter.getBindingConfiguration().setMapIDs(false);
            beanWriter.setWriteEmptyElements(false);

            beanWriter.enablePrettyPrint();
            beanWriter.write(viewSet);

            fw.close();

        } catch(Exception ex)
        {
            log.error("error writing views", ex);
        }
    }
}

