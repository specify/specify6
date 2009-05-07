/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.datamodel.SpExportSchema;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 */
@SuppressWarnings("serial")
public class ExportSchemaMapEditor extends CustomDialog
{
	protected final SpExportSchema exportSchema;
	protected final QueryBldrPane  qb;
	protected final Taskable task;

    protected JButton                        mapToBtn;
    protected JButton                        unmapBtn;
    protected JButton                        upBtn;
    protected JButton                        downBtn;

	protected JPanel										btnPanel;

	protected Color											btnPanelColor;

	public ExportSchemaMapEditor(final Frame frame, final Taskable task, final SpExportSchema exportSchema)
	{
	    super(frame, UIRegistry.getResourceString("ExportSchemaMapEditor.Title"), true, OKCANCELHELP, null); //XXX i18n		
	    this.task = task;
	    this.exportSchema = exportSchema;
	    qb = buildQb();
	    
	    createUI();
	}
	
	protected QueryBldrPane buildQb()
	{
        SpQuery query = createNewQueryDataObj();
        //XXX testing
        return new QueryBldrPane(exportSchema == null ? "nada" : exportSchema.getSchemaName(), task, query, false, true);
	}

	/**
     * Creates a new Query Data Object.
     * @param tableInfo the table information
     * @return the query
     */
    protected SpQuery createNewQueryDataObj()
    {
    	DBTableInfo tableInfo = getTableInfo();
    	SpQuery query = new SpQuery();
        query.initialize();
        //XXX testing
        query.setName(exportSchema == null ? "nada" : exportSchema.getSchemaName());
        query.setNamed(false);
        query.setContextTableId((short)tableInfo.getTableId());
        query.setContextName(tableInfo.getShortClassName());
        return query;
    }

	protected DBTableInfo getTableInfo()
	{
		return DBTableIdMgr.getInstance().getInfoById(1);
	}
	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.CustomDialog#createUI()
	 */
	@Override
	public void createUI()
	{
		super.createUI();

		String mapListLeftLabel;
		String mapListRightLabel;
		
        upBtn = createIconBtn("ReorderUp", "WB_MOVE_UP", null);
        downBtn = createIconBtn("ReorderDown", "WB_MOVE_DOWN", null);

		// Note: if workbenchTemplate is null then it is
		String fieldsLabel = getResourceString("WB_FIELDS");

		mapListLeftLabel = fieldsLabel;
		mapListRightLabel = getResourceString("WB_COLUMNS");

		CellConstraints cc = new CellConstraints();

		JPanel mainLayoutPanel = new JPanel();

		PanelBuilder labelsBldr = new PanelBuilder(new FormLayout(
				"p, f:p:g, p", "p"));
		labelsBldr.add(createLabel(mapListLeftLabel, SwingConstants.LEFT), cc
				.xy(1, 1));
		labelsBldr.add(createLabel(mapListRightLabel, SwingConstants.RIGHT), cc
				.xy(3, 1));

		JButton dumpMappingBtn = createIconBtn("BlankIcon",
				IconManager.IconSize.Std16, "WB_MAPPING_DUMP",
				new ActionListener() {
					public void actionPerformed(ActionEvent ae)
					{
						dumpMapping();
					}
				});
		dumpMappingBtn.setEnabled(true);
		dumpMappingBtn.setFocusable(false);

        mapToBtn = createIconBtn("Map", "WB_ADD_MAPPING_ITEM", null);
        unmapBtn = createIconBtn("Unmap", "WB_REMOVE_MAPPING_ITEM", null);

		PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p",
				"p,f:p:g, p, 2px, p, f:p:g"));
		upDownPanel.add(dumpMappingBtn, cc.xy(1, 1));
		upDownPanel.add(upBtn, cc.xy(1, 3));
		upDownPanel.add(downBtn, cc.xy(1, 5));

		PanelBuilder middlePanel = new PanelBuilder(new FormLayout("c:p:g",
				"p, 2px, p"));
		middlePanel.add(mapToBtn, cc.xy(1, 1));
		middlePanel.add(unmapBtn, cc.xy(1, 3));

		btnPanel = middlePanel.getPanel();

		PanelBuilder outerMiddlePanel = new PanelBuilder(new FormLayout(
				"c:p:g", "f:p:g, p, f:p:g"));
		outerMiddlePanel.add(btnPanel, cc.xy(1, 2));
		PanelBuilder builder = new PanelBuilder(
				new FormLayout(
						"f:max(200px;p):g, 5px, max(200px;p), 5px, p:g, 5px, f:max(250px;p):g, 2px, p",
						"p, 2px, f:max(350px;p):g"), mainLayoutPanel);

		builder.add(qb, cc.xy(1, 3));

		mainLayoutPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPanel = mainLayoutPanel;

		Color bgColor = btnPanel.getBackground();
		int inc = 16;
		btnPanelColor = new Color(Math.min(255, bgColor.getRed() + inc), Math
				.min(255, bgColor.getGreen() + inc), Math.min(255, bgColor
				.getBlue()
				+ inc));
		btnPanel.setBackground(btnPanelColor);
		btnPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

		okBtn.setEnabled(false);

		HelpMgr.registerComponent(helpBtn, helpContext);

		mainPanel.add(contentPanel, BorderLayout.CENTER);
		
		pack();

	}

	protected void dumpMapping()
	{
		
	}
	
}
