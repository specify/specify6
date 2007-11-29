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
/**
 * 
 */
package edu.ku.brc.af.core.expresssearch;

import static edu.ku.brc.ui.UIHelper.createI18NLabel;

import java.awt.Color;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.VerticalSeparator;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 27, 2007
 *
 */
public class RelatedTableInfoPanel extends JPanel implements ChangeListener
{
    protected SearchConfig config;
    
    protected JList                relatedTablesList;
    protected DefaultListModel     relatedTablesModel  = new DefaultListModel();
    protected JTextArea            relatedTableDescTA;
    
    protected Vector<RelatedQuery> relatedQueriesInUse = new Vector<RelatedQuery>();
    
    protected ToggleButtonChooserPanel<RelatedQuery> togPanel;
    protected boolean                                ignoreChanges = false;
    
    /**
     * 
     */
    public RelatedTableInfoPanel(final SearchConfig config)
    {
        this.config = config;
        
        createUI();
    }

    /**
     * 
     */
    protected void createUI()
    {
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        togPanel = new ToggleButtonChooserPanel<RelatedQuery>(config.getRelatedQueries(), ToggleButtonChooserPanel.Type.Checkbox);
        togPanel.setUseScrollPane(true);
        togPanel.setChangeListener(this);
        togPanel.createUI();
        
        relatedTablesList = new JList(relatedTablesModel);
        TableNameRenderer nameRender = new TableNameRenderer(IconManager.IconSize.Std24);
        nameRender.setUseIcon("PlaceHolder");
        relatedTablesList.setCellRenderer(nameRender);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,10px,p,10px,p,4px,f:p:g", "p,2px,t:p:g,f:p:g"), this);
        pb.add(createI18NLabel("ES_RELATED_ACTIVATE"), cc.xy(1,1));
        pb.add(togPanel.getUIComponent(), cc.xywh(1, 3, 1, 2));
        
        Color color = getBackground();
        pb.add(new VerticalSeparator(color.brighter(), color.darker()), cc.xywh(3, 1, 1, 4));
        
        JScrollPane sp = new JScrollPane(relatedTablesList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pb.add(createI18NLabel("ES_RELATED_SEARCHES"), cc.xy(5, 1));
        pb.add(sp, cc.xywh(5, 3, 1, 2));
        
        relatedTableDescTA = new JTextArea(8, 40);
        relatedTableDescTA.setEditable(false);
        relatedTableDescTA.setWrapStyleWord(true);
        relatedTableDescTA.setBackground(Color.WHITE);
        relatedTableDescTA.setLineWrap(true);
        sp = new JScrollPane(relatedTableDescTA, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pb.add(createI18NLabel("ES_RELATED_DESC"), cc.xy(7, 1));
        pb.add(sp, cc.xy(7, 3));

        
        relatedTablesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            //@Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    rtRelatedTableSelected();
                }
            }
        });
    }
    
    /**
     * 
     */
    protected void rtRelatedTableSelected()
    {
        RelatedQuery rq = (RelatedQuery)relatedTablesList.getSelectedValue();
        if (rq != null)
        {
            ExpressResultsTableInfo erti = ExpressSearchConfigCache.getSearchIdToTableInfoHash().get(rq.getId());
            if (erti != null)
            {
                relatedTableDescTA.setText(erti.getDescription());
            } else
            {
                relatedTableDescTA.setText("");
            }
        } else
        {
            relatedTableDescTA.setText("");
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setVisible(boolean)
     */
    public void setVisible(final boolean vis)
    {
        RelatedQuery.setAddRealtedQueryTitle(!vis);
        
        if (vis)
        {
            ignoreChanges = true;
            relatedQueriesInUse.clear();
            relatedTablesModel.clear();
            for (RelatedQuery rq : config.getRelatedQueries())
            {
                if (rq.isInUse())
                {
                    relatedQueriesInUse.add(rq);
                }
            }
            
            Collections.sort(relatedQueriesInUse);
            togPanel.setItems(relatedQueriesInUse);
            
            for (RelatedQuery rq : relatedQueriesInUse)
            {
                if (rq.getIsActive())
                {
                    togPanel.setSelectedObj(rq);
                }
                relatedTablesModel.addElement(rq);
            }
            ignoreChanges = false;
            //togPanel.setSelectedObjects(relatedQueriesInUse);
        }
        super.setVisible(vis);
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(ChangeEvent e)
    {
        if (!ignoreChanges)
        {
            JToggleButton togBtn = (JToggleButton)e.getSource();
            RelatedQuery  rq     = togPanel.getItemForBtn(togBtn);
            rq.setIsActive(togBtn.isSelected());
            rq.setDisplayOrder(Integer.MAX_VALUE);
        }
    }
    
}
