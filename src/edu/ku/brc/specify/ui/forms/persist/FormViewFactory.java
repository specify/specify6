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

import java.io.File;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
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
    private final static Logger     log = Logger.getLogger(FormViewFactory.class);
    private static  FormViewFactory instance = new FormViewFactory();;
    
    private static final String NAME  = "name";
    private static final String ID    = "id";
    private static final String TYPE  = "type";
    private static final String LABEL = "label";
    private static final String DESC  = "desc";
    private static final String CLASSNAME  = "class";
    private static final String GETTABLE  = "gettable";

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
     * @param element
     * @return a form view
     */
    public static FormView createView(final Element element) throws Exception
    {
        String bStr = element.attributeValue("resourceLabels");
        if (bStr != null)
        {
            instance.doingResourceLabels = Boolean.parseBoolean(bStr);
        }
        
        FormView view      = null;
        int      id        = Integer.parseInt(element.attributeValue(ID));
        String   name      = element.attributeValue(NAME);
        String   className = element.attributeValue(CLASSNAME);
        String   gettableClassName = element.attributeValue(GETTABLE);
        String   desc      = "";
        
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
                view = createFormView(FormView.ViewType.form, element, id, name, className, gettableClassName, desc, instance.doingResourceLabels);
                break;
        
            case table :
                view = createTableView(element, id, name, className, gettableClassName, desc, instance.doingResourceLabels);
                break;
                
            case field :
                view = createFormView(FormView.ViewType.field, element, id, name, gettableClassName, className, desc, instance.doingResourceLabels);
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
     * Returns the Resource bundle string for a label (not implemented fully)
     * @param aLabel the label to be localized
     * @return a string that has been localized using a ResourceBundle
     */
    protected static String getResourceLabel(final String aLabel)
    {
        return aLabel;
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
                    aFormView.addAltView(new FormAltView(id, instance.doingResourceLabels && label != null ? getResourceLabel(label) : label));
                }
            }
        } else
        {
            log.error("View Set ["+instance.viewSetName+"] ["+aFormView+"] or element ["+aElement+"] is null.");
        }
    }
    
    /**
     * Gets the list of defs (cellDef or rowDef)
     * @param aElement the DOM lement to process
     * @param aDefName the name of the element to go get all the elements (strings) from
     * @return a vector of Strings with all the cell or row definitions
     */
    protected static Vector<String> getDefs(final Element aElement, final String aDefName)
    {
        Vector<String> defs = new Vector<String>();
        Element cellDef = (Element)aElement.selectSingleNode(aDefName);
        
        if (cellDef != null)
        {
            for ( Iterator i = cellDef.elementIterator( "cellDef" ); i.hasNext(); )
            {
                defs.add(((Element) i.next()).getText());         
            }
        } else
        {
            log.error("Element ["+aElement.getName()+"] must have a "+aDefName);
        }
        return defs;
    }
    
    /**
     *  Creates a particular type of form
     * @param type the type of form to be built
     * @param element the DOM element for building the form
     * @param id the id of the form
     * @param resLabels indicates whether the labels are really resource identifiers so the labels should come froma resource bundle
     * @return a form view of type "form"
     */
    protected static FormFormView createFormView(final FormView.ViewType type, 
                                                 final Element element, 
                                                 final int id, 
                                                 final String name, 
                                                 final String className, 
                                                 final String gettableClassName, 
                                                 final String desc, 
                                                 final boolean resLabels)
    {
        FormFormView formView = new FormFormView(type, id, name, className, gettableClassName, desc);
        
        formView.setResourceLabels(resLabels);
        formView.setColumnDef(getDefs(element, "columnDef"));
        formView.setRowDef(getDefs(element, "rowDef"));
        
        Element rowsElement = (Element)element.selectSingleNode("rows");        
        if (rowsElement != null)
        {
            for ( Iterator i = rowsElement.elementIterator( "row" ); i.hasNext(); ) {
                Element rowElement = (Element) i.next();      
                
                FormRow formRow = new FormRow();
                
                for ( Iterator cellIter = rowElement.elementIterator( "cell" ); cellIter.hasNext(); )
                {
                    Element cellElement = (Element) cellIter.next();
                    String cellName  = getAttr(cellElement, NAME, "");
                    String label     = getAttr(cellElement, LABEL, "");
                    String uitype    = getAttr(cellElement, "uitype", "");
                    String format    = getAttr(cellElement, "format", "");
                    int    cols      = getAttr(cellElement, "cols", 10); // XXX PREF for default width of text field
                    int    rows      = getAttr(cellElement, "rows", 5);  // XXX PREF for default heightof text area
                    int    colspan   = getAttr(cellElement, "colspan", 1);
                    int    rowspan   = getAttr(cellElement, "rowspan", 1);
                    
                    FormCell.CellType cellType = FormCell.CellType.valueOf(cellElement.attributeValue(TYPE));
                    switch (cellType)
                    {
                        case label:
                        case separator:
                        case field:
                            formRow.createCell(cellType, 
                                               cellName, 
                                               instance.doingResourceLabels && label != null ? getResourceLabel(label) : label, 
                                               uitype, format, cols, rows, colspan, rowspan);
                            break;
                            
                        case subview:
                        {
                            String vsName = cellElement.attributeValue("viewsetname");
                            if (vsName == null ||vsName.length() == 0)
                            {
                                vsName = instance.viewSetName;
                            }
                            formRow.createSubView(cellElement.attributeValue(NAME),
                                                  vsName,
                                                  Integer.parseInt(cellElement.attributeValue(ID)),
                                                  cellElement.attributeValue("class"),
                                                  colspan,
                                                  rowspan);
                        }
                        break;
                    }        
                }
                formView.addRow(formRow);                    
            }
        }

        return formView;
    }
    
    /**
     * @param element
     * @param attrName
     * @param defValue
     * @return
     */
    public static String getAttr(final Element element, final String attrName, final String defValue)
    {
        String str = element.attributeValue(attrName);
        return str != null ? str : defValue;
    }
    
    /**
     * @param element
     * @param attrName
     * @param defValue
     * @return
     */
    public static int getAttr(final Element element, final String attrName, final int defValue)
    {
        String str = element.attributeValue(attrName);
        return str != null && str.length() > 0 ? Integer.parseInt(str) : defValue;
    }
    
    /**
     * Creates a Table Form View
     * @param element the DOM element to process
     * @param id the id of the table
     * @param name the name of the table
     * @param desc the desc of the table
     * @param resLabels indicates whether the labels are really resource identifiers so the labels should come froma resource bundle
     * @return a form view of type "table"
     */
    protected static FormTableView createTableView(final Element element,
                                                   final int     id,
                                                   final String  name,
                                                   final String  className,
                                                   final String  gettableClassName,
                                                   final String  desc,
                                                   final boolean resLabels)
    {
        FormTableView tableView = new FormTableView(id, name, className, gettableClassName, desc);
        
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
    
    public static void save(final ViewSet aViewSet, final String aFileName)
    {
        try
        {
            Vector<ViewSet> viewsets = new Vector<ViewSet>();
            viewsets.add(aViewSet);
            
            File       file = new File(aFileName);
            FileWriter fw   = new FileWriter(file);
            
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

            BeanWriter beanWriter = new BeanWriter(fw);            
            XMLIntrospector introspector = beanWriter.getXMLIntrospector();
            introspector.getConfiguration().setWrapCollectionsInElement(false);
            beanWriter.getBindingConfiguration().setMapIDs(false);
            beanWriter.setWriteEmptyElements(false);
            
            beanWriter.enablePrettyPrint();
            beanWriter.write(aViewSet);
            
            fw.close();
            
        } catch(Exception ex)
        {
            log.error("error writing views", ex);
        }
    }
}

