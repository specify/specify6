package edu.ku.brc.specify.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;

/**
 * This class sends usage stats.
 * 
 * @author rods
 * 
 * @code_status Complete
 */
public class StatsTrackerTask extends edu.ku.brc.af.tasks.StatsTrackerTask
{
    private JProgressBar progress;
    
    /**
     * Constructor.
     */
    public StatsTrackerTask()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.StatsTrackerTask#createClosingFrame()
     */
    @Override
    protected void showClosingFrame()
    {
        ImageIcon img = IconManager.getIcon("SpecifySplash");
        
        CellConstraints    cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,150px", "f:p:g,2px,p"));
        pb.setDefaultDialogBorder();
        
        JLabel lbl = new JLabel(img);
        pb.add(lbl, cc.xy(1, 1));
        lbl = new JLabel("Closing Specify...", SwingConstants.CENTER);
        lbl.setFont(lbl.getFont().deriveFont(18.0f));
        pb.add(lbl, cc.xyw(1, 3, 2));
        
        progress = new JProgressBar(0, 100);
        pb.add(progress, cc.xy(2, 3));
        
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setContentPane(pb.getPanel());
        frame.pack();
        UIHelper.centerAndShow(frame);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.StatsTrackerTask#getPCLForWorker()
     */
    @Override
    protected PropertyChangeListener getPCLForWorker()
    {
        return new PropertyChangeListener() {
            public  void propertyChange(final PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) 
                {
                    progress.setValue((Integer)evt.getNewValue());
                }
            }
        };
    }

    
    /**
     * Collection Statistics about the Collection (synchronously).
     */
    @Override
    protected Vector<NameValuePair> collectExtraStats()
    {
        Vector<NameValuePair> stats = new Vector<NameValuePair>();
        
        try
        {
            String resourceName = "CollStats";
            Element rootElement = AppContextMgr.getInstance().getResourceAsDOM(resourceName);
            if (rootElement == null)
            {
                throw new RuntimeException("Couldn't find resource ["+resourceName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            int count = 0;
            List<?> rows = rootElement.selectNodes("/statistics/stat"); //$NON-NLS-1$
            double total = rows.size();
            progress.setIndeterminate(true);
            for (Object obj : rows)
            {
                Element statElement = (Element)obj;
                String  statsName   = XMLHelper.getAttr(statElement, "name", null);
                if (StringUtils.isNotEmpty(statsName))
                {
                    count++;
                    addStat(statsName, stats, statElement.getText());
                    progress.setIndeterminate(false);
                    worker.setProgressValue((int)(100.0 * (((double)count) / total)));
                }
            }
            worker.setProgressValue(100);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        // Gather Collection Counts;
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        stats.add(new NameValuePair("Collection_number", fixParam(collection.getRegNumber()))); //$NON-NLS-1$
        stats.add(new NameValuePair("Collection_website", fixParam(collection.getWebSiteURI()))); //$NON-NLS-1$
        stats.add(new NameValuePair("Collection_portal", fixParam(collection.getWebPortalURI()))); //$NON-NLS-1$

        return stats;
    }
    
}
