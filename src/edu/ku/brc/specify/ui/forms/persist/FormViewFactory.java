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
    private static  FormViewFactory instance;
    
    private static final String NAME  = "name";
    private static final String ID    = "id";
    private static final String TYPE  = "type";
    private static final String LABEL = "label";

    // Data Members
    protected boolean doingResourceLabels = false;
    protected String  viewSetName         = null;
    
    /**
     * Default Constructor
     *
     */
    private FormViewFactory()
    {
    }
    
    /**
     * Return the singleton
     * @return the singleton of the view factory
     */
    public static FormViewFactory getInstance()
    {
        // Purposely decided to have it created when it is asked for the first time.
        // ViewMgr is statically created also
        if (instance == null)
        {
            instance = new FormViewFactory();
        }
        return instance;
    }
     
    /**
     * Creates the view object hierarchy
     * @param aElement
     * @return a form view
     */
    public FormView createView(Element aElement) throws Exception
    {
        String bStr = aElement.attributeValue("resourceLabels");
        if (bStr != null)
        {
            doingResourceLabels = Boolean.parseBoolean(bStr);
        }
        
        FormView          view = null;
        int               id   = Integer.parseInt(aElement.attributeValue(ID));
        FormView.ViewType type;
        try
        {
            type = FormView.ViewType.valueOf(aElement.attributeValue(TYPE));
        } catch (Exception ex)
        {
            log.error("view["+id+"] has illegal type["+aElement.attributeValue(TYPE)+"]", ex);
            throw ex;
        }

        switch (type)
        {
            case form :
                view = createFormView(FormView.ViewType.form, aElement, id, doingResourceLabels);
                break;
        
            case table :
                view = createTableView(aElement, id, doingResourceLabels);
                break;
                
            case field :
                view = createFormView(FormView.ViewType.field, aElement, id, doingResourceLabels);
               break;
        }
        
        addAltViews(view, aElement);
        
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
    public String getViews(Element aDocument, Vector<FormView> aList, boolean aDoValidation) throws Exception
    {
        viewSetName = aDocument.attributeValue(NAME);
        if (ViewMgr.getInstance().isViewSetNameInUse(viewSetName))
        {
            String msg = "Duplicate View Set Name [" + viewSetName + "]";
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
                view.setViewSetName(viewSetName); // create the full name which the views element name plus the view's id
                idHash.put(view.getId(), view);
                aList.add(view);
            } else
            {
                String msg = "View Set ["+viewSetName+"] ["+view.getId()+"] is not unique.";
                log.error(msg);
                throw new ConfigurationException(msg);
            }
        }
        
        return viewSetName;
    }
    
    /**
     * Returns the Resource bundle string for a label (not implemented fully)
     * @param aLabel the label to be localized
     * @return a string that has been localized using a ResourceBundle
     */
    protected String getResourceLabel(String aLabel)
    {
        return aLabel;
    }
    
    /**
     * Processes all the AltViews
     * @param aFormView the form they should be associated with
     * @param aElement the element to process
     */
    protected void addAltViews(FormView aFormView, Element aElement)
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
                    aFormView.addAltView(new FormAltView(id, doingResourceLabels && label != null ? getResourceLabel(label) : label));
                }
            }
        } else
        {
            log.error("View Set ["+viewSetName+"] ["+aFormView+"] or element ["+aElement+"] is null.");
        }
    }
    
    /**
     * Gets the list of defs (cellDef or rowDef)
     * @param aElement the DOM lement to process
     * @param aDefName the name of the element to go get all the elements (strings) from 
     * @return a vector of Strings with all the cell or row definitions
     */
    protected Vector<String> getDefs(Element aElement, String aDefName)
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
     * @param aType the type of form to be built
     * @param aElement the DOM element for building the form
     * @param aId the id of the form
     * @param aResLabels indicates whether the labels are really resource identifiers so the labels should come froma resource bundle
     * @return a form view of type "form"
     */
    protected FormFormView createFormView(FormView.ViewType aType, Element aElement, int aId, boolean aResLabels)
    {
        FormFormView formView = new FormFormView(aType, aId);
        
        formView.setResourceLabels(aResLabels);
        formView.setColumnDef(getDefs(aElement, "columnDef"));
        formView.setRowDef(getDefs(aElement, "rowDef"));
        
        Element rows = (Element)aElement.selectSingleNode("rows");        
        if (rows != null)
        {
            for ( Iterator i = rows.elementIterator( "row" ); i.hasNext(); ) {
                Element element = (Element) i.next();      
                
                FormRow row = new FormRow();
                
                for ( Iterator cellIter = element.elementIterator( "cell" ); cellIter.hasNext(); ) 
                {
                    Element cellElement = (Element) cellIter.next();
                    String label = cellElement.attributeValue(LABEL);
                    
                    FormCell.CellType cellType = FormCell.CellType.valueOf(cellElement.attributeValue(TYPE));
                    switch (cellType) 
                    {
                        case label:
                        case separator:
                        case field:
                            row.createCell(cellType, 
                                           cellElement.attributeValue(NAME),
                                           doingResourceLabels && label != null ? getResourceLabel(label) : label);
                            break;
                            
                        case subview:
                        {
                            String vsName = cellElement.attributeValue("viewsetname");
                            if (vsName == null ||vsName.length() == 0)
                            {
                                vsName = viewSetName;
                            }
                            row.createSubView(cellElement.attributeValue(NAME), 
                                              vsName,
                                              Integer.parseInt(cellElement.attributeValue(ID)), 
                                              cellElement.attributeValue("class"));
                        }
                        break;
                    }        
                }
                formView.addRow(row);                    
            }
        }

        return formView;
    }
    
    /**
     * CReates a Table Form View
     * @param aElement the DOM element to process
     * @param aId the id of the tbale
     * @param aResLabels indicates whether the labels are really resource identifiers so the labels should come froma resource bundle
     * @return a form view of type "table"
     */
    protected FormTableView createTableView(Element aElement, int aId, boolean aResLabels)
    {
        FormTableView tableView = new FormTableView(FormView.ViewType.table, aId);
        
        tableView.setResourceLabels(aResLabels);
        
        Element columns = (Element)aElement.selectSingleNode("columns");        
        if (columns != null)
        {
            for ( Iterator i = columns.elementIterator( "column" ); i.hasNext(); ) {
                Element element = (Element) i.next();      
                
                FormColumn column = new FormColumn(element.attributeValue(NAME), element.attributeValue(LABEL));
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
    
    public void save(ViewSet aViewSet, String aFileName)
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
