package edu.ku.brc.specify.ui.forms.persist;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import edu.ku.brc.specify.InitializeData;
import edu.ku.brc.specify.prefs.PrefGroup;
import edu.ku.brc.specify.exceptions.ConfigurationException;

public class FormViewFactory
{
    protected final static Logger log = Logger.getLogger(FormViewFactory.class);

    protected boolean doingResourceLabels = false;
    
    /**
     * 
     *
     */
    public FormViewFactory()
    {
        super();
    }
    
    /**
     * 
     * @param aElement
     * @return
     */
    public FormView createView(Element aElement)
    {
        String bStr = aElement.attributeValue("resourceLabels");
        if (bStr != null)
        {
            doingResourceLabels = Boolean.parseBoolean(bStr);
        }
        
        FormView          view = null;
        int               id   = Integer.parseInt(aElement.attributeValue("id"));
        FormView.ViewType type;
        try
        {
            type = FormView.ViewType.valueOf(aElement.attributeValue("type"));
        } catch (Exception ex)
        {
            log.error("view["+id+"] has illegal type["+aElement.attributeValue("type")+"]", ex);
            return null;
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
     * 
     * @param aElement
     * @return
     */
    public Vector<FormView> getViews(Element aElement) throws ConfigurationException
    {
        Hashtable<Integer, FormView> idHash = new Hashtable<Integer, FormView>();
        
        Vector<FormView> viewList = new Vector<FormView>();
        
        for ( Iterator i = aElement.elementIterator( "view" ); i.hasNext(); ) 
        {
            Element element = (Element) i.next();
            FormView view = createView(element);
            if (idHash.get(view.getId()) == null)
            {
                idHash.put(view.getId(), view);
                viewList.add(view);
            } else
            {
                String msg = "View ["+view.getId()+"] is not unique.";
                log.error(msg);
                throw new ConfigurationException(msg);
            }
        }
        
        // Validate all the Alt Views and SubViews
        for (FormView view : viewList)
        {
            Hashtable<Integer, FormAltView> altIdHash = new Hashtable<Integer, FormAltView>();
            for (FormAltView altView : view.getAltViews())
            {
                if (idHash.get(altView.getId()) == null)
                {
                    String msg = "View ["+view.getId()+"] has invalid Alt View Id ["+altView.getId()+"]";
                    log.error(msg);
                    throw new ConfigurationException(msg);
                    
                } else if (view.getId() == altView.getId())
                {
                    String msg = "View ["+view.getId()+"] cannot be its own AltView.";
                    log.error(msg);
                    throw new ConfigurationException(msg);
                }
            }
            
            if (view.getType() == FormView.ViewType.form) // faster than instance of
            {
                FormFormView formView = (FormFormView)view;
                for (FormRow row : formView.getRows())
                {
                    for (FormCell cell : row.getCells())
                    {
                        if (cell.getType() == FormCell.CellType.subview) // faster than instance of
                        {
                            FormCellSubView cellSV = (FormCellSubView)cell;
                            if (idHash.get(cellSV.getId()) == null)
                            {
                                String msg = "View ["+view.getId()+"] Cell SubView Id ["+cellSV.getId()+"] cannot be found.";
                                log.error(msg);
                                throw new ConfigurationException(msg);
                            }
                        }
                    }
                }
            }
        }
        
        // Validate all the SubViews
        
       
        // testing only 
        save(viewList);
            
        return viewList;
    }
    
    /**
     * 
     * @param aLabel
     * @return
     */
    protected String getResourceLabel(String aLabel)
    {
        return aLabel;
    }
    
    /**
     * 
     * @param aFormView
     * @param aElement
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
                    
                    String label = element.attributeValue("label");
                    int id = Integer.parseInt(element.attributeValue("id"));
                    aFormView.addAltView(new FormAltView(id, doingResourceLabels && label != null ? getResourceLabel(label) : label));
                }
            }
        } else
        {
            log.error("View ["+aFormView+"] or element ["+aElement+"] is null.");
        }
    }
    
    /**
     * 
     * @param aElement
     * @param aDefName
     * @return
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
     * 
     * @param aType
     * @param aElement
     * @param aId
     * @param aResLabels
     * @return
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
                    String label = cellElement.attributeValue("label");
                    
                    FormCell.CellType cellType = FormCell.CellType.valueOf(cellElement.attributeValue("type"));
                    switch (cellType) 
                    {
                        case label:
                        case separator:
                        case field:
                            row.createCell(cellType, 
                                           cellElement.attributeValue("name"),
                                           doingResourceLabels && label != null ? getResourceLabel(label) : label);
                            break;
                            
                        case subview:
                            row.createSubView(cellElement.attributeValue("name"), 
                                    Integer.parseInt(cellElement.attributeValue("id")), 
                                    cellElement.attributeValue("class"));
                            break;
                    }        
                }
                formView.addRow(row);                    
            }
        }

        return formView;
    }
    
    /**
     * 
     * @param aElement
     * @param aId
     * @param aResLabels
     * @return
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
                
                FormColumn column = new FormColumn(element.attributeValue("name"), element.attributeValue("label"));
                tableView.addColumn(column);
            }
        }
        
        return tableView;
    }
    
    /**
     * Can't use Betwixt
     *
     */
    public void save(Vector<FormView> aViews)
    {
            try
            {
                /*Collection<FormView> collection = new ArrayList<PrefGroup>(); 
                for (Enumeration e=aViews.elements();e.hasMoreElements();)
                {
                    collection.add((FormView)e.nextElement());
                }*/
    
                File       file = new File("/home/rods/form.xml");
                FileWriter fw   = new FileWriter(file);
                
                fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    
                BeanWriter beanWriter = new BeanWriter(fw);            
                XMLIntrospector introspector = beanWriter.getXMLIntrospector();
                introspector.getConfiguration().setWrapCollectionsInElement(true);
                beanWriter.getBindingConfiguration().setMapIDs(false);
                beanWriter.setWriteEmptyElements(false);
                
                beanWriter.enablePrettyPrint();
                beanWriter.write("views", aViews);
                
                fw.close();
                
            } catch(Exception ex)
            {
                log.error("error writing views", ex);
            }
        }
}
