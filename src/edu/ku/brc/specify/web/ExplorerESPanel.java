/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.web;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 22, 2008
 *
 */
public class ExplorerESPanel implements ExpressSearchResultsPaneIFace
{

    protected Vector<ResultSetTableModel>    rsCollectors = new Vector<ResultSetTableModel>();
    
    /**
     * 
     */
    public ExplorerESPanel()
    {
        
    }
    
    public synchronized void reset()
    {
        rsCollectors.clear();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#addSearchResults(edu.ku.brc.ui.db.QueryForIdResultsIFace)
     */
    public synchronized void addSearchResults(final QueryForIdResultsIFace queryDef)
    {
        ResultSetTableModel rsm = new ResultSetTableModel(null, queryDef, true); // true - process sequentially
        rsCollectors.add(rsm);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#addTable(edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace)
     */
    public void addTable(final ESResultsTablePanelIFace table)
    {

    }

    public boolean hasResults()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#removeTable(edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace)
     */
    public void removeTable(final ESResultsTablePanelIFace table)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#revalidateScroll()
     */
    public void revalidateScroll()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#doQueriesSynchronously()
     */
    public boolean doQueriesSynchronously()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace#done()
     */
    public void done()
    {
        //int i = 0;
        //for (ResultSetTableModel rsm : rsCollectors)
        //{
        //    System.out.println(createJSON(rsm.getQueryForIdResults(), rsm).toString());
        //    i++;
        //}
    }
    
    /**
     * @return
     */
    public JSONArray getTables()
    {
        Vector<Integer> orderList = new Vector<Integer>();
        Hashtable<Integer, ResultSetTableModel> hash = new Hashtable<Integer, ResultSetTableModel>();
        for (ResultSetTableModel rsm : rsCollectors)
        {
            Integer order = rsm.getQueryForIdResults().getDisplayOrder();
            orderList.add(order);
            hash.put(order, rsm);
        }
        
        Collections.sort(orderList);
        
        JSONArray tableArray = new JSONArray();
        int i = 0;
        for (Integer order : orderList)
        {
            ResultSetTableModel rsm = hash.get(order);
            
            if (rsm.getRowCount() > 0)
            {
                tableArray.add(createJSON(rsm.getQueryForIdResults(), rsm));
                i++;
            }
        }
        //System.out.println(tableArray.toString());
        return tableArray;
    }
    
    /**
     * @param queryDef
     * @param rsm
     * @return
     */
    private JSONObject createJSON(final QueryForIdResultsIFace queryDef,
                                  final ResultSetTableModel    rsm)
    {
        //DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        try
        {
            int totalWidth = 600; // Width used in the Page
            
            List<ERTICaptionInfo> infoList = queryDef.getVisibleCaptionInfo();
            
            // 18 is for the border and a scrollbar
            int colWidth   = (totalWidth - (rsm.getRowCount() > 14 ? 20 : 4)) / infoList.size();
            
            int i = 0;
            JSONArray colModel = new JSONArray();
            for (ERTICaptionInfo info : infoList)
            {
                String colName = info.getColName();
                //Class<?> cls   = info.getColClass();
                
                JSONObject colObj = new JSONObject();
                if (i == 0)
                {
                    colObj.accumulate("id", colName);  
                }
                colObj.accumulate("header", info.getColLabel());
                colObj.accumulate("width", colWidth);
                colObj.accumulate("sortable", true);
                
                colObj.accumulate("dataIndex", colName);
                
                //if (cls == Date.class || cls == Calendar.class)
                //{
                //    //colObj.accumulate("renderer", "Ext.util.Format.dateRenderer('m/d/Y')");
                //}
                colModel.add(colObj);
                i++;
            }
            
            //System.out.println("COLMODEL: "+colModel.toString());
            
            JSONArray headerModel = new JSONArray();
            for (ERTICaptionInfo info : infoList)
            {
                String colName = info.getColName();
                Class<?> cls   = info.getColClass();
                
                JSONObject hdr = new JSONObject();
                hdr.accumulate("name", colName);
                
                String type = "string";
                //if (cls == Date.class || cls == Calendar.class)
                //{
                //    type = "date";
                //    hdr.accumulate("dateFormat", "n/j h:ia"); 
                //    
                //} else 
                if (cls == Float.class)
                {
                    type = "float";
                } else if (cls == Boolean.class)
                {
                    type = "bool";
                }
                hdr.accumulate("type", type); 
                headerModel.add(hdr);
            }
            
            //System.out.println("HEADER: "+headerModel.toString());
            
            
            JSONArray dataModel = new JSONArray();
            for (int rowInx=0;rowInx<rsm.getRowCount();rowInx++)
            {
                JSONArray row = new JSONArray();
                
                Object[] data = new Object[infoList.size()];
                for (int inx=0;inx<data.length;inx++)
                {
                    ERTICaptionInfo info = infoList.get(inx);
                
                    Object dataObj = rsm.getCacheValueAt(rowInx, inx);
                    if (dataObj != null)
                    {
                        if (Date.class.isAssignableFrom(dataObj.getClass()) || 
                            Calendar.class.isAssignableFrom(dataObj.getClass()))
                        {
                            if (dataObj instanceof java.util.GregorianCalendar)
                            {
                                java.util.GregorianCalendar gDate = (java.util.GregorianCalendar)dataObj;
                                dataObj = sdf.format(gDate.getTime());
                            } else
                            {
                                dataObj = sdf.format((Date)dataObj);
                            }
                        } else 
                        {
                            UIFieldFormatterIFace fmt = info.getUiFieldFormatter();
                            if (fmt != null)
                            {
                                dataObj = fmt.formatToUI(dataObj);
                            }
                        }
                    } else
                    {
                        dataObj = "";
                    }
                    row.add(dataObj);
                }
                dataModel.add(row);
            }
            
            JSONObject tableJSON = new JSONObject();
            tableJSON.accumulate("title",        queryDef.getTitle());
            tableJSON.accumulate("column_model", colModel);
            tableJSON.accumulate("headers",      headerModel);
            tableJSON.accumulate("rows",         dataModel);
            
            //System.out.println("===============");
            //System.out.println(tableJSON.toString());
            //System.out.println("===============");
            
            return tableJSON;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    
}
