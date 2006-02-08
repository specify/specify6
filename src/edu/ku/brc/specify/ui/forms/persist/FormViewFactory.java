 /* Filename:    $RCSfile: FormViewFactory,v $
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
import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.io.File;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.specify.exceptions.ConfigurationException;
import edu.ku.brc.specify.ui.forms.ViewMgr;

/**
 * Factory that creates Views from ViewSet files. This class uses the singleton ViewMgr to verify the View Set Name is unique.
 * If it is not unique than it throws an exception.<br> In this case a "form" is really the definition of a form. The form's object hierarchy
 * is used to creates the forms using Swing UI objects. The classes will also be used by the forms editor.
 *
 * @author Rod Spears <rods@ku.edu>
 */
public class FormViewFactory
{
    // Statics
    private static final Logger     log = Logger.getLogger(FormViewFactory.class);
    private static final FormViewFactory instance = new FormViewFactory();
    
    private static final String NAME  = "name";
    private static final String ID    = "id";
    private static final String TYPE  = "type";
    private static final String LABEL = "label";
    private static final String DESC  = "desc";
    private static final String CLASSNAME  = "class";
    private static final String GETTABLE   = "gettable";
    private static final String SETTABLE   = "settable";

    // Data Members
    protected boolean doingResourceLabels = false;
    protected String  viewSetName         = null;
    
    /**
     * Default Constructor
     *
     */
    protected FormViewFactory()
    {
    }
    
     /**
     * Creates the view object hierarchy
     * @param element the element to build the FormView from
     * @return a form view
     */
    public static FormView createView(final Element element) throws Exception
    {
        // set a global value while creating this form as to whether the labels are keys to a resource bundle
        // or whether they are the actual label
        instance.doingResourceLabels = getAttr(element, "useresourcelabels", "false").equals("true");
        
        FormView view        = null;
        int      id          = Integer.parseInt(element.attributeValue(ID));
        String   name        = element.attributeValue(NAME);
        String   className   = element.attributeValue(CLASSNAME);
        String   gettableClassName = element.attributeValue(GETTABLE);
        String   settableClassName = element.attributeValue(SETTABLE);
        String   desc        = "";
        boolean  isValidated = getAttr(element, "validated", false);
       
        Element descElement = (Element)element.selectSingleNode(DESC);
        if (descElement != null)
        {
            desc = descElement.getTextTrim();
        }
        
        FormView.ViewType type;
        try
        {
            type = FormView.ViewType.valueOf(element.attributeValue(TYPE));
        } catch (Exception ex)
        {
            log.error("view["+id+"] has illegal type["+element.attributeValue(TYPE)+"]", ex);
            throw ex;
        }

        switch (type)
        {
            case form :
                view = createFormView(FormView.ViewType.form, element, id, name, 
                                      className, gettableClassName, settableClassName, 
                                      desc, instance.doingResourceLabels, isValidated);
                break;
        
            case table :
                view = createTableView(element, id, name, className, gettableClassName, settableClassName, 
                                       desc, instance.doingResourceLabels, isValidated);
                break;
                
            case field :
                view = createFormView(FormView.ViewType.field, element, id, name, gettableClassName, settableClassName, 
                                      className, desc, instance.doingResourceLabels, isValidated);
               break;
        }
        
        addAltViews(view, element);
        
        return view;
    }

    /**
     * Fill the Vector with all the views from the DOM document
     * @param aDocument the DOM document conforming to form.xsd
     * @param aList the liust to be filled
     * @param aDoValidation indicates it should validate subviews ids
     * @return the name of the views
     * @throws Exception for duplicate view set names or if a Form ID is not unique
     */
    public static String getViews(final Element aDocument, final Vector<FormView> aList, final boolean aDoValidation) throws Exception
    {
        instance.viewSetName = aDocument.attributeValue(NAME);
        if (ViewMgr.isViewSetNameInUse(instance.viewSetName))
        {
            String msg = "Duplicate View Set Name [" + instance.viewSetName + "]";
            log.error(msg);
            throw new ConfigurationException(msg);
        }
        
        Hashtable<Integer, FormView> idHash = new Hashtable<Integer, FormView>();
        
        for ( Iterator i = aDocument.elementIterator( "view" ); i.hasNext(); )
        {
            Element  element = (Element) i.next(); // assume element is NOT null, if it is null it will cause an exception
            FormView view    = createView(element);
            if (idHash.get(view.getId()) == null)
            {
                view.setViewSetName(instance.viewSetName); // create the full name which the views element name plus the view's id
                idHash.put(view.getId(), view);
                aList.add(view);
            } else
            {
                String msg = "View Set ["+instance.viewSetName+"] ["+view.getId()+"] is not unique.";
                log.error(msg);
                throw new ConfigurationException(msg);
            }
        }
        
        return instance.viewSetName;
    }
    
    /**
     * Processes all the AltViews
     * @param aFormView the form they should be associated with
     * @param aElement the element to process
     */
    protected static void addAltViews(final FormView aFormView, final Element aElement)
    {
        if (aFormView != null && aElement != null)
        {
            Element altviews = (Element)aElement.selectSingleNode("altviews");        
            if (altviews != null)
            {
                // iterate through child elements of root with element name "foo"
                for ( Iterator i = altviews.elementIterator( "alt" ); i.hasNext(); )
                {
                    Element element = (Element) i.next();
                    
                    String label = element.attributeValue(LABEL);
                    int id = Integer.parseInt(element.attributeValue(ID));
                    aFormView.addAltView(new FormAltView(id, getResourceLabel(label)));
                }
            }
        } else
        {
            log.error("View Set ["+instance.viewSetName+"] ["+aFormView+"] or element ["+aElement+"] is null.");
        }
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
                    String  cellName    = getAttr(cellElement, NAME, "");
                    int     colspan     = getAttr(cellElement, "colspan", 1);
                    int     rowspan     = getAttr(cellElement, "rowspan", 1);
                    
                    FormCell.CellType cellType = FormCell.CellType.valueOf(cellElement.attributeValue(TYPE));
                    FormCell          cell     = null;
                    
                    switch (cellType)
                    {
                        case label:
                            cell = formRow.addCell(new FormCellLabel(cellName, getLabel(cellElement), getAttr(cellElement, "labelfor", ""), colspan));
                            break;
                        
                        case separator:
                            cell = formRow.addCell(new FormCellSeparator(cellName, getLabel(cellElement), colspan));
                            break;
                        
                        case field:
                        {
                            String uitype         = getAttr(cellElement, "uitype", "");
                            String format         = getAttr(cellElement, "format", "");
                            int    cols           = getAttr(cellElement, "cols", 10); // XXX PREF for default width of text field
                            int    rows           = getAttr(cellElement, "rows", 5);  // XXX PREF for default heightof text area
                            String validationType = getAttr(cellElement, "valtype", "OK");
                            String validationRule = getAttr(cellElement, "validation", "");
                            String initialize     = getAttr(cellElement, "initialize", "");
                            boolean isRequired    = getAttr(cellElement, "isrequired", false);
                            boolean isEncrypted    = getAttr(cellElement, "isencrypted", false);
                            
                            // check to see see if the validation is a node in the cell
                            if (isEmpty(validationRule))
                            {
                                Element valNode = (Element)cellElement.selectSingleNode("validation");
                                if (valNode != null)
                                {
                                    String str = valNode.getTextTrim();;
                                    if (isNotEmpty(str))
                                    {
                                        validationRule = str;
                                    }
                                }
                            }
                            
                            FormCellField field = new FormCellField(FormCell.CellType.field, 
                                                                    cellName, uitype, format, isRequired,  
                                                                    cols, rows, colspan, rowspan, validationType, validationRule, isEncrypted);
                            field.setLabel(getAttr(cellElement, "label", ""));
                            field.setPickListName(getAttr(cellElement, "picklist", ""));
                            field.setChangeListenerOnly(getAttr(cellElement, "changesonly", false));
                            field.setInitialize(initialize);
                            
                            cell = formRow.addCell(field);
                        } break;
                            
                        case command:
                        {
                            cell =  formRow.addCell(new FormCellCommand(cellName, 
                                                                getLabel(cellElement), 
                                                                getAttr(cellElement, "commandtype", ""),
                                                                getAttr(cellElement, "action", "")));
                        } break;
                            
                        case panel:
                        {
                            FormCellPanel cellPanel = new FormCellPanel(cellName, 
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
                            if (vsName == null ||vsName.length() == 0)
                            {
                                vsName = instance.viewSetName;
                            }
                            cell = formRow.addCell(new FormCellSubView(cellElement.attributeValue(NAME),
                                                  vsName,
                                                  Integer.parseInt(cellElement.attributeValue(ID)),
                                                  cellElement.attributeValue("class"),
                                                  colspan,
                                                  rowspan));
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
    protected static FormFormView createFormView(final FormView.ViewType type, 
                                                 final Element element, 
                                                 final int     id, 
                                                 final String  name, 
                                                 final String  className, 
                                                 final String  gettableClassName, 
                                                 final String  settableClassName, 
                                                 final String  desc, 
                                                 final boolean resLabels,
                                                 final boolean isValidated)
    {
        FormFormView formView = new FormFormView(type, id, name, className, gettableClassName, settableClassName, desc, isValidated);
        
        formView.setResourceLabels(resLabels);
        formView.setColumnDef(createDef(element, "columnDef"));
        formView.setRowDef(createDef(element, "rowDef"));
        formView.setEnableRules(getEnableRules(element));
        formView.setValidated(getAttr(element, "validate", "false").equals("true"));
        
        processRows(element, formView.getRows());

        return formView;
    }
    
    /**
     * Get a string attribute value from an element value
     * @param element the element to get the attribute from
     * @param attrName the name of the attribute to get
     * @param defValue the default value if the attribute isn't there
     * @return the attr value or the default value
     */
    public static String getAttr(final Element element, final String attrName, final String defValue)
    {
        String str = element.attributeValue(attrName);
        return str != null ? str : defValue;
    }
    
    /**
     * Get a int attribute value from an element value
     * @param element the element to get the attribute from
     * @param attrName the name of the attribute to get
     * @param defValue the default value if the attribute isn't there
     * @return the attr value or the default value
     */
    public static int getAttr(final Element element, final String attrName, final int defValue)
    {
        String str = element.attributeValue(attrName);
        return isNotEmpty(str) ? Integer.parseInt(str) : defValue;
    }
    
    /**
     * Get a int attribute value from an element value
     * @param element the element to get the attribute from
     * @param attrName the name of the attribute to get
     * @param defValue the default value if the attribute isn't there
     * @return the attr value or the default value
     */
    public static boolean getAttr(final Element element, final String attrName, final boolean defValue)
    {
        String str = element.attributeValue(attrName);
        return isNotEmpty(str) ? Boolean.parseBoolean(str.toLowerCase()) : defValue;
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
    protected static FormTableView createTableView(final Element element,
                                                   final int     id,
                                                   final String  name,
                                                   final String  className,
                                                   final String  gettableClassName,
                                                   final String  settableClassName,
                                                   final String  desc,
                                                   final boolean resLabels,
                                                   final boolean isValidated)
    {
        FormTableView tableView = new FormTableView(id, name, className, gettableClassName, settableClassName, desc, isValidated);
        
        tableView.setResourceLabels(resLabels);
        
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
     * This is a temporary method for testing this needs to be moved somewhere
     * XXX please move me!
     */

    public class FormViewComparator implements Comparator<FormView>
    {
        
        public int compare(FormView o1, FormView o2)
        {
            if (o1.getId() == o2.getId())
            {
                return 0;
            } else
            {
               return o1.getId() > o2.getId() ? 1 : -1;
            }
        }
        
        public boolean equals(Object obj)
        {
            return (obj instanceof FormView);
        }
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

