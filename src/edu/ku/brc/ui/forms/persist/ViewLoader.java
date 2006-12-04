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
package edu.ku.brc.ui.forms.persist;

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.ui.UICacheManager.getResourceString;
import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
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

import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.ui.forms.UIFieldFormatterMgr;
import edu.ku.brc.ui.forms.UIFieldFormatterMgr.Formatter;
import edu.ku.brc.ui.validation.TypeSearchForQueryFactory;

/**
 * Factory that creates Views from ViewSet files. This class uses the singleton ViewSetMgr to verify the View Set Name is unique.
 * If it is not unique than it throws an exception.<br> In this case a "form" is really the definition of a form. The form's object hierarchy
 * is used to creates the forms using Swing UI objects. The classes will also be used by the forms editor.
 
 * @code_status Beta
 **
 * @author rods
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
        // do nothing
    }


    /**
     * Creates the view.
     * @param element the element to build the View from
     * @return the view
     * @throws Exception anything
     */
    public static View createView(final Element element,
                                  final Hashtable<String, ViewDef> viewDefs) throws Exception
    {
       boolean useResourceLabels = getAttr(element, "useresourcelabels", "false").equals("true");

        String   name              = element.attributeValue(NAME);
        String   objTitle          = getAttr(element, "objtitle", null);
        String   className         = element.attributeValue(CLASSNAME);
        String   resLabels         = element.attributeValue(RESOURCELABELS);
        String   desc              = getDesc(element);
        String   businessRules     = getAttr(element, "busrules", null);

        View view = new View(instance.viewSetName, name, objTitle, className, businessRules != null ? businessRules.trim() : null, desc, useResourceLabels, resLabels);

        Element altviews = (Element)element.selectSingleNode("altviews");
        if (altviews != null)
        {
            AltView defaultAltView = null;
            
            AltView.CreationMode defaultMode  = AltView.parseMode(getAttr(altviews, "mode", ""), AltView.CreationMode.View);
            String               selectorName = altviews.attributeValue("selector");
            
            view.setDefaultMode(defaultMode);
            view.setSelectorName(selectorName);
            
            Hashtable<String, Boolean> nameCheckHash = new Hashtable<String, Boolean>();
            
            // iterate through child elements
            for ( Iterator<?> i = altviews.elementIterator( "altview" ); i.hasNext(); )
            {
                Element altElement = (Element) i.next();

                String altName      = altElement.attributeValue(NAME);
                String viewDefName  = altElement.attributeValue("viewdef");
                String label        = altElement.attributeValue(LABEL);
                boolean isValidated = getAttr(altElement, "validated", false);
                boolean isDefault   = getAttr(altElement, "default", false);

                AltView.CreationMode mode = AltView.parseMode(getAttr(altElement, "mode", ""), AltView.CreationMode.View);

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
                
                // Check to make sure all the AlViews have different names.
                Boolean nameExists = nameCheckHash.get(altName);
                if (nameExists == null) // no need to check the boolean
                {
                    AltView altView = new AltView(view, altName, label, mode, isValidated, isDefault, viewDef);
                    if (StringUtils.isNotEmpty(selectorName))
                    {
                        altView.setSelectorName(selectorName);
                        
                        String selectorValue = altElement.attributeValue("selector_value");
                        if (StringUtils.isNotEmpty(selectorValue))
                        {
                            altView.setSelectorValue(selectorValue);
                            
                        } else
                        {
                            throw new RuntimeException("Selector Value is missing for viewDefName["+viewDefName+"] altName["+altName+"]");
                        }
                    }
                    
                    if (defaultAltView == null && isDefault)
                    {
                        defaultAltView = altView;
                    }
    
                    view.addAltView(altView);
                    nameCheckHash.put(altName, true);
                    
                } else
                {
                    log.error("The altView name["+altName+"] already exists!");
                }
                nameCheckHash.clear(); // why not?
            }
            
            // Very Special Case
            // Add a grid view if we have an Edit and a View
            if (view.isSpecialViewAndEdit())
            {
                // Clones Alt view and ViewDef (Deep Clone)
                AltView gridAltView = view.getAltViews().get(0).clone();
                gridAltView.getViewDef().setType(ViewDef.ViewType.formTable);
                gridAltView.getViewDef().setName("Grid");
                gridAltView.setName("Grid");
                gridAltView.setLabel(getResourceString("Grid"));
                view.addAltView(gridAltView);
            }

            /*
            // iterate through child elements of root with element name "foo"
            for ( Iterator i = altviews.elementIterator( "altview" ); i.hasNext(); )
            {
                Element altElement = (Element) i.next();

                String altName      = altElement.attributeValue(NAME);
                String viewDefName  = altElement.attributeValue("viewdef");
                String label        = altElement.attributeValue(LABEL);
                boolean isValidated = getAttr(altElement, "validated", false);
                boolean isDefault   = getAttr(altElement, "default", false);

                AltView.CreationMode mode = AltView.parseMode(getAttr(altElement, "mode", ""), AltView.CreationMode.View);



                // Make sure we only have one default view
                if (defaultAltView != null && isDefault)
                {
                    isDefault = false;
                }

                
                String selectorName = altElement.attributeValue("selector");
                if (StringUtils.isNotEmpty(selectorName))
                {
                    List subViewList = altElement.elements("subview");
                    if (subViewList.size() > 0)
                    {
                        //List<AltView> subViews = new ArrayList<AltView>(subViewList.size());
                        for (Object svObj : subViewList)
                        {
                            Element sbvElement = (Element)svObj;
                            altName      = sbvElement.attributeValue(NAME);
                            viewDefName  = sbvElement.attributeValue("viewdef");
                            
                            ViewDef viewDef = viewDefs.get(viewDefName);
                            if (viewDef == null)
                            {
                                throw new RuntimeException("View Name["+name+"] refers to a ViewDef that doesn't exist.");
                            }
                            String selectorValue  = sbvElement.attributeValue("selector_value");
                            AltView subView = new AltView(view, altName, label, mode, isValidated, isDefault, viewDef);
                            subView.setSelectorName(selectorName);
                            subView.setSelectorValue(selectorValue);
                            //subViews.add(subView);
                            view.addAltView(subView);
                            if (defaultAltView == null && isDefault)
                            {
                                defaultAltView = subView;
                                isDefault = false;
                            }
                        }
                        //altView.setSelectorName(selectorName);
                        //altView.setSubViews(subViews);
                    }
                } else
                {
                    
                    ViewDef viewDef = viewDefs.get(viewDefName);
                    if (viewDef == null)
                    {
                        throw new RuntimeException("View Name["+name+"] refers to a ViewDef that doesn't exist.");
                    }
                    
                    AltView altView = new AltView(view, altName, label, mode, isValidated, isDefault, viewDef);

                    if (defaultAltView == null && isDefault)
                    {
                        defaultAltView = altView;
                    }
                    view.addAltView(altView);
                }
            }
            */

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
            case rstable:
            case formTable :
            case form :
                viewDef = createFormViewDef(element, type, name, className, gettableClassName, settableClassName, desc);
                break;

            case table :
                //view = createTableView(element, id, name, className, gettableClassName, settableClassName,
                //                       desc, instance.doingResourceLabels, isValidated);
                break;

            case field :
                //view = createFormView(FormView.ViewType.field, element, id, name, gettableClassName, settableClassName,
                //                      className, desc, instance.doingResourceLabels, isValidated);
               break;
               
            case iconview:
                viewDef = createIconViewDef(element, type, name, className, gettableClassName, settableClassName, desc);
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
            for ( Iterator<?> i = viewsElement.elementIterator( "view" ); i.hasNext(); )
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
            for ( Iterator<?> i = viewDefsElement.elementIterator( "viewdef" ); i.hasNext(); )
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
                for ( Iterator<?> i = enableRules.elementIterator( "rule" ); i.hasNext(); )
                {
                    Element ruleElement = (Element) i.next();
                    String id = getAttr(ruleElement, "id", "");
                    if (isNotEmpty(id))
                    {
                        rulesList.put(id, ruleElement.getTextTrim());
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
     * @param numRows the number of rows
     * @return the String representing the column definition for JGoodies
     */
    protected static String createDef(final Element element, final String attrName, final int numRows)
    {
        Element cellDef = (Element)element.selectSingleNode(attrName);
        if (cellDef != null)
        {
            String cellText = cellDef.getText();
            String cellStr  = getAttr(cellDef, "cell", null);
            String sepStr   = getAttr(cellDef, "sep", null);
            
            if (cellStr != null && sepStr != null)
            {
                
                boolean auto = getAttr(cellDef, "auto", false);
                if (auto)
                {
                    return createDuplicateJGoodiesDef(cellStr, sepStr, numRows) + 
                                (StringUtils.isNotEmpty(cellText) ? ("," + cellText) : "");
                }
                
                int dup = getAttr(cellDef, "dup", -1);
                if (dup > 0)
                {
                    return createDuplicateJGoodiesDef(cellStr, sepStr, dup) + 
                        (StringUtils.isNotEmpty(cellText) ? ("," + cellText) : "");

                }
                // else
                throw new RuntimeException("Element ["+element.getName()+"] Cell or Sep is null for 'dup' or 'auto 'on column def.");
            }
            // else
            return cellText;
        }
        // else
        log.error("Element ["+element.getName()+"] must have a columnDef");
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
        }
        // else
        return "";

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
            for ( Iterator<?> i = rowsElement.elementIterator( "row" ); i.hasNext(); ) {
                Element rowElement = (Element) i.next();

                FormRow formRow = new FormRow();

                for ( Iterator<?> cellIter = rowElement.elementIterator( "cell" ); cellIter.hasNext(); )
                {
                    Element cellElement = (Element)cellIter.next();
                    String  cellId      = getAttr(cellElement, "id", "");
                    String  cellName    = getAttr(cellElement, NAME, cellId); // let the name default to the id if it doesn't have a name
                    int     colspan     = getAttr(cellElement, "colspan", 1);
                    int     rowspan     = getAttr(cellElement, "rowspan", 1);

                    FormCell.CellType cellType = FormCell.CellType.valueOf(cellElement.attributeValue(TYPE));
                    FormCell          cell     = null;

                    switch (cellType)
                    {
                        case label:
                        {
                            cell = formRow.addCell(new FormCellLabel(cellId, 
                                                                     cellName, 
                                                                     getLabel(cellElement), 
                                                                     getAttr(cellElement, "labelfor", ""),
                                                                     getAttr(cellElement, "icon", null),
                                                                     getAttr(cellElement, "recordobj", false), 
                                                                     colspan));

                            break;
                        }
                        case separator:
                        {
                            cell = formRow.addCell(new FormCellSeparator(cellId, cellName, getLabel(cellElement), colspan));
                            break;
                        }
                        case field:
                        {
                            String uitypeStr      = getAttr(cellElement, "uitype", "");
                            String format         = getAttr(cellElement, "format", "");
                            String formatName     = getAttr(cellElement, "formatname", "");
                            String uiFieldFormatter = getAttr(cellElement, "uifieldformatter", "");
                            int    cols           = getAttr(cellElement, "cols", 10); // XXX PREF for default width of text field
                            int    rows           = getAttr(cellElement, "rows", 5);  // XXX PREF for default heightof text area
                            String validationType = getAttr(cellElement, "valtype", "Changed");
                            String validationRule = getAttr(cellElement, "validation", "");
                            String initialize     = getAttr(cellElement, "initialize", "");
                            boolean isRequired    = getAttr(cellElement, "isrequired", false);
                            String  pickListName  = getAttr(cellElement, "picklist", "");

                            if (isNotEmpty(format) && isNotEmpty(formatName))
                            {
                                //throw new RuntimeException("Both format and formatname cannot both be set! ["+cellName+"]");
                                log.error("Both format and formatname cannot both be set! ["+cellName+"] ignoring format");
                                format = "";
                            }
                            
                            Hashtable<String, String> properties = processInitializeString(initialize);
                            
                            // XXX DEBUG ONLY PLease REMOVE LATER
                            if (StringUtils.isEmpty(uitypeStr))
                            {
                                System.err.println("***************************************************************************");
                                System.err.println("***** Cell Id["+cellId+"] Name["+cellName+"] uitype is empty and should be 'text'. (Please Fix!)");
                                System.err.println("***************************************************************************");
                                uitypeStr = "text";
                            }

                            // THis switch is used to get the "display type" and 
                            // set up other vars needed for creating the controls
                            FormCellField.FieldType uitype       = FormCellField.FieldType.valueOf(uitypeStr);
                            String                  dspUITypeStr = null;
                            switch (uitype)
                            {
                                case textarea:
                                    dspUITypeStr = getAttr(cellElement, "dspuitype", "dsptextarea");
                                    break;
                                
                                case  querycbx:
                                {
                                    dspUITypeStr = getAttr(cellElement, "dspuitype", "textfieldinfo");
                                    
                                    String fmtName = TypeSearchForQueryFactory.getFormatName(properties.get("name"));
                                    if (isNotEmpty(fmtName))
                                    {
                                        formatName = fmtName;
                                    }
                                    break;
                                }

                                case formattedtext:
                                {
                                    validationRule = getAttr(cellElement, "validation", "formatted");
                                    dspUITypeStr   = getAttr(cellElement, "dspuitype", "dsptextfield");
                                    if (isNotEmpty(uiFieldFormatter))
                                    {
                                        Formatter formatter = UIFieldFormatterMgr.getFormatter(uiFieldFormatter);
                                        if (formatter == null)
                                        {
                                            log.error("Couldn't find formatter["+uiFieldFormatter+"]");
                                            uiFieldFormatter = "";
                                            uitype = FormCellField.FieldType.text;
                                        }
                                    }
                                    break;
                                }

                                case url:
                                    dspUITypeStr = getAttr(cellElement, "dspuitype", uitypeStr);
                                    properties = processInitializeString(initialize);
                                    break;
                                    
                                case list:
                                case image:
                                case checkbox:
                                case password:
                                    dspUITypeStr = getAttr(cellElement, "dspuitype", uitypeStr);
                                    break;
                                
                                case plugin:
                                case button:
                                    dspUITypeStr = getAttr(cellElement, "dspuitype", uitypeStr);
                                    properties = processInitializeString(initialize);
                                    break;

                                case combobox:
                                    dspUITypeStr = getAttr(cellElement, "dspuitype", "dsptextfield");
                                    break;
                                    
                                default:
                                    dspUITypeStr = getAttr(cellElement, "dspuitype", "dsptextfield");
                                    break;
                                
                            } //switch

                            FormCellField.FieldType dspUIType = FormCellField.FieldType.valueOf(dspUITypeStr);
                            
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

                            boolean isEncrypted = getAttr(cellElement, "isencrypted", false);
                            
                            
                            FormCellField field = new FormCellField(FormCell.CellType.field, cellId, 
                                                                    cellName, uitype, dspUIType, format, formatName, uiFieldFormatter, isRequired,
                                                                    cols, rows, colspan, rowspan, validationType, validationRule, isEncrypted);
                            
                            field.setLabel(getAttr(cellElement,        "label", ""));
                            field.setReadOnly(getAttr(cellElement,     "readonly", false));
                            field.setDefaultValue(getAttr(cellElement, "default", ""));
                            field.setPickListName(pickListName);
                            field.setChangeListenerOnly(getAttr(cellElement, "changesonly", true) && !isRequired);
                            field.setProperties(properties);

                            cell = formRow.addCell(field);
                            break;
                        }
                        case command:
                        {
                            cell =  formRow.addCell(new FormCellCommand(cellId, cellName,
                                                                getLabel(cellElement),
                                                                getAttr(cellElement, "commandtype", ""),
                                                                getAttr(cellElement, "action", "")));
                            break;
                        }
                        case panel:
                        {
                            FormCellPanel cellPanel = new FormCellPanel(cellId, cellName,
                                                                        getAttr(cellElement, "paneltype", ""),
                                                                        getAttr(cellElement, "coldef", "p"),
                                                                        getAttr(cellElement, "rowdef", "p"),
                                                                        colspan, rowspan);
                            processRows(cellElement, cellPanel.getRows());
                            cell = formRow.addCell(cellPanel);
                            break;
                        }
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
                                                   getAttr(cellElement, "desc", ""),
                                                   colspan,
                                                   rowspan));

                            break;
                        }
                        case iconview:
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
                                    getAttr(cellElement, "desc", ""),
                                    colspan,
                                    rowspan));
                            break;
                        }

                        case statusbar:
                        {
                            cell = formRow.addCell(new FormCell(FormCell.CellType.statusbar, cellId, cellName, colspan, rowspan));
                            break;
                        }
                        default:
                        {
                            // what is this?
                            log.error("Encountered unknown cell type");
                            continue;
                        }
                    } // switch
                    cell.setIgnoreSetGet(getAttr(cellElement, "ignore", false));
                }
                cellRows.add(formRow);
            }
        }

    }

    /**
     * @param element the DOM element for building the form
     * @param type the type of form to be built
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
                                                   final String  desc)
    {
        FormViewDef formView = new FormViewDef(type, name, className, gettableClassName, settableClassName, desc);

        List<FormRow> rows = formView.getRows();
        
        processRows(element, rows);
        
        formView.setColumnDef(createDef(element, "columnDef", rows.size()));
        formView.setRowDef(createDef(element, "rowDef", rows.size()));
        formView.setEnableRules(getEnableRules(element));


        return formView;
    }
    
    
    /**
     * @param element the DOM element for building the form
     * @param type the type of form to be built
     * @param name the name of the form
     * @param className the class name of the data object
     * @param gettableClassName the class name of the getter
     * @param settableClassName the class name of the setter
     * @param desc the description
     * @return a form view of type "form"
     */
    protected static ViewDef createIconViewDef(final Element element,
                                               final ViewDef.ViewType type,
                                               final String  name,
                                               final String  className,
                                               final String  gettableClassName,
                                               final String  settableClassName,
                                               final String  desc)
    {
        ViewDef formView = new ViewDef(type, name, className, gettableClassName, settableClassName, desc);

        //formView.setEnableRules(getEnableRules(element));

        return formView;
    }

    /**
     * Creates a Table Form View
     * @param type the type of form to be built
     * @param element the DOM element for building the form
     * @param name the name of the form
     * @param className the class name of the data object
     * @param gettableClassName the class name of the getter
     * @param settableClassName the class name of the setter
     * @param desc the description
     * @return a form view of type "table"
     */
    protected static TableViewDef createTableView(final Element element,
                                                   final String  name,
                                                   final String  className,
                                                   final String  gettableClassName,
                                                   final String  settableClassName,
                                                   final String  desc)
    {
        TableViewDef tableView = new TableViewDef( name, className, gettableClassName, settableClassName, desc);

        //tableView.setResourceLabels(resLabels);

        Element columns = (Element)element.selectSingleNode("columns");
        if (columns != null)
        {
            for ( Iterator<?> i = columns.elementIterator( "column" ); i.hasNext(); ) {
                Element colElement = (Element) i.next();

                FormColumn column = new FormColumn(colElement.attributeValue(NAME),
                        colElement.attributeValue(LABEL),
                        getAttr(colElement, "dataobjformatter", null),
                        getAttr(colElement, "format", null)
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

