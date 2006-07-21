package edu.ku.brc.specify.tasks.subpane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.AltView;

public class SearchFormPane extends FormPane
{
    private static final Logger log  = Logger.getLogger(SearchFormPane.class);
    
    protected FormViewObj formViewObj;
    protected Hashtable<String, Hashtable<String, String>> hashTables = new Hashtable<String, Hashtable<String, String>>();
    
    
    public SearchFormPane(String   name, 
                          Taskable task, 
                          String   viewSetName, 
                          String   viewName)
    {
        super(name, task, viewSetName, viewName, AltView.CreationMode.Search.toString(), null, true);
        
        viewable = multiView.getCurrentView();
        if (viewable instanceof FormViewObj)
        {
            formViewObj = (FormViewObj)viewable;
            
        } else
        {
            throw new RuntimeException("The form didn't create a FormViewObj for the Viewable!.");
        }
        walkMultiViewsSetHash(multiView);
        
        formViewObj.getSaveBtn().setEnabled(true);
        formViewObj.getSaveBtn().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                doSearch();
            }
        });
    }
    
    protected void walkMultiViewsSetHash(final MultiView multiView)
    {
        Hashtable<String, String> formData        = new Hashtable<String, String>();
        Viewable                  currentViewable = multiView.getCurrentView();
        
        //System.out.println("["+currentViewable.getView().getName()+"]");
        hashTables.put(currentViewable.getView().getName(), formData);
        multiView.setData(formData);
        
        for (MultiView mv : multiView.getKids())
        {
            walkMultiViewsSetHash(mv);
        }
    }
    
    protected void walkMultiViewsGetValues(final MultiView multiView)
    {
        //Viewable currentViewable = multiView.getCurrentView();
        multiView.getCurrentView().getDataFromUI();
        
        for (MultiView mv : multiView.getKids())
        {
            walkMultiViewsGetValues(mv);
        }
    }
    
    protected void doSearch()
    {
        walkMultiViewsGetValues(multiView);
        
        String[] nameMap = {"Collection Object Search", 
                            "CollectionObject"};
        
        for (int i=0;i<nameMap.length;i++)
        {
            String name    = nameMap[i++];
            String objName = nameMap[i];
            
            Hashtable<String, String> formData = hashTables.get(name);
            
            for (Enumeration<String> e = formData.keys(); e.hasMoreElements();)
            {
                String key   = e.nextElement();
                String value = formData.get(key);
                log.debug("["+key+"]["+value+"]");
            }
        }
        
    }

}
