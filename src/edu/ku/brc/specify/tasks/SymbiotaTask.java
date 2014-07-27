/**
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.DroppableNavBox;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.SpSymbiotaInstance;
import edu.ku.brc.specify.tasks.subpane.SymbiotaPane;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.Trash;


/**
 * @author timo
 *
 */
public class SymbiotaTask extends BaseTask {
    //private static final Logger log  = Logger.getLogger(VisualQueryTask.class);
    
    private static final String  SYMBIOTA        = "Symbiota";
    private static final String SELECT_INSTANCE  = "SelectSymbiotaInstance";
    private static final String  SYMBIOTA_TITLE     = "SYMBIOTA_TITLE";
 
    public static final String BASE_URL_PREF = "SymbioTask.BaseUrlPref";
    public static final String BASE_URL_DEFAULT = "http://pinkava.asu.edu/symbiota/sandbox/webservices/dwc/dwcaingesthandler.php";
    
    public static final DataFlavor SYMBIOTA_FLAVOR = new DataFlavor(SymbiotaTask.class, SYMBIOTA);

    protected SpSymbiotaInstance theInstance = null;
    
    // Data Members
    protected NavBox                actionNavBox     = null;
    protected DroppableNavBox       navBox           = null;

    protected NavBoxItemIFace 		sendToSymAction;
    protected NavBoxItemIFace		sendToArchiveAction;
    protected NavBoxItemIFace		getFromSymAction;
    
    /**
     * 
     */
    public SymbiotaTask() {
        super(SYMBIOTA, UIRegistry.getResourceString(SYMBIOTA_TITLE));
    }


	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.BaseTask#getStarterPane()
	 */
	@Override
	public SubPaneIFace getStarterPane() {
		//This may be abuse of the starterPane concept?
		//Possibly SymbiotaPane's IntroPane should be the StarterPane?
		List<NavBoxItemIFace> its = navBox.getItems();
		if (its.size() > 0) {
			selectInstance(Integer.class.cast(its.get(0).getData()), its.get(0), false);
		}
        SymbiotaPane pane = new SymbiotaPane(name, this);
        starterPane = pane;
        return starterPane;
	}

	/**
	 * 
	 */
	public void refreshInstance() {
		if (theInstance != null) {
			selectInstance(theInstance.getId(), getNavBottomItemForInstance(theInstance.getId()), true);
		}
	}
	
	
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#requestContext()
     */
    @Override
    public void requestContext() {
    	//Only allow one SymbiotaInstance pane to be present.
    	boolean goodStarterPane = starterPane != null && SubPaneMgr.getInstance().indexOfComponent(starterPane.getUIComponent()) > -1;
        if (goodStarterPane) {
            ContextMgr.requestContext(this);
        }

        if (!goodStarterPane) {
               super.requestContext();
        } else {
            SubPaneMgr.getInstance().showPane(starterPane);
        }
    }

	/**
	 * 
	 */
	protected void addActions() {
		actionNavBox.add(NavBox.createBtnWithTT(
				UIRegistry.getResourceString("SymbiotaTask.NewInstance"),
				"Symbiota",
				UIRegistry.getResourceString("SymbiotaTask.NewInstanceTT"),
				IconManager.STD_ICON_SIZE, 
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						makeNewInstance();
					}
				}));

		sendToArchiveAction = NavBox.createBtnWithTT(
				UIRegistry.getResourceString("SymbiotaTask.SendToArchiveBtn"),
				"Symbiota",
				UIRegistry.getResourceString("SymbiotaTask.SendToArchiveBtnTT"),
				IconManager.STD_ICON_SIZE, 
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						sendToArchive();
					}
				});
		actionNavBox.add(sendToArchiveAction);
		
		sendToSymAction = NavBox.createBtnWithTT(
				UIRegistry.getResourceString("SymbiotaTask.SendToSymBtn"),
				"Symbiota",
				UIRegistry.getResourceString("SymbiotaTask.SendToSymBtnTT"),
				IconManager.STD_ICON_SIZE, 
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						sendToSym();
					}
				});
		actionNavBox.add(sendToSymAction);

		getFromSymAction = NavBox.createBtnWithTT(
				UIRegistry.getResourceString("SymbiotaTask.GetFromSymBtn"),
				"Symbiota",
				UIRegistry.getResourceString("SymbiotaTask.GetFromSymBtnTT"),
				IconManager.STD_ICON_SIZE, 
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getFromSym();
					}
				});
		actionNavBox.add(getFromSymAction);

	}

	/**
	 * 
	 */
	protected void sendToSym() {
		if (theInstance != null) {
			SymbiotaPane.class.cast(starterPane).startSend(null);
		}
	}
	
	/**
	 * 
	 */
	protected void sendToArchive() {
		if (theInstance != null) {
	        String path = AppPreferences.getLocalPrefs().get("DwcArchive_PATH", null);
	        
	        FileDialog fDlg = new FileDialog(((Frame)UIRegistry.getTopWindow()), UIRegistry.getResourceString("SAVE"), FileDialog.SAVE);
	        if (path != null)
	        {
	            fDlg.setDirectory(path);
	        }
	        fDlg.setVisible(true);
	        
	        String dirStr   = fDlg.getDirectory();
	        String fileName = fDlg.getFile();
	        if (StringUtils.isEmpty(dirStr) || StringUtils.isEmpty(fileName))
	        {
	            return;
	        }
	        
	        if (StringUtils.isEmpty(FilenameUtils.getExtension(fileName)))
	        {
	            fileName += ".zip";
	        }
	        path = dirStr + fileName;
	        AppPreferences.getLocalPrefs().put("DwcArchive_PATH", path);
			
	        SymbiotaPane.class.cast(starterPane).startSend(path);
		}
	}
	
	/**
	 * 
	 */
	protected void getFromSym() {
		if (theInstance != null) {
			SymbiotaPane.class.cast(starterPane).getFromSym();
		}
		
	}
	
	/**
	 * @return
	 */
	public String getSymbiotaPostUrlForCurrentInstance() {
		//XXX not sure if this should be a global preference or instance-specific...
		if (theInstance != null) {
			
		}
		return AppPreferences.getRemote().get(BASE_URL_PREF, BASE_URL_DEFAULT);
	}
	
	/**
	 * 
	 */
	protected void addInstances() {
		List<SpSymbiotaInstance> symInstances = getInstances();
		navBox.clear();
		for (SpSymbiotaInstance symInstance : symInstances) {
			final Integer instanceId = symInstance.getId();
			final NavBoxItemIFace nbi = makeDnDNavBtn(navBox, 
					symInstance.getInstanceName(), "Symbiota", null,
					new CommandAction("Symbiota", DELETE_CMD_ACT, instanceId), true, true);
			RolloverCommand roc = RolloverCommand.class.cast(nbi); 
			roc.setToolTip(UIRegistry.getResourceString("SymbiotaTask.InstanceTT"));
			roc.setData(instanceId);		
			roc.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectInstance(instanceId, nbi, false);
					}
				});
	        roc.addDragDataFlavor(new DataFlavorTableExt(getClass(), "Symbiota", SpSymbiotaInstance.getClassTableId()));
			roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
			
//			navBox.add(NavBox.createBtnWithTT(
//				symInstance.getInstanceName(),
//				"Symbiota",
//				UIRegistry.getResourceString("SymbiotaTask.InstanceTT"),
//				IconManager.STD_ICON_SIZE, 
//				new ActionListener() {
//					public void actionPerformed(ActionEvent e) {
//						selectInstance(instanceId);
//					}
//				}));
		}
	}

	/**
	 * 
	 */
	protected void updateActionEnablement() {
		sendToArchiveAction.setEnabled(theInstance != null);
		sendToSymAction.setEnabled(theInstance != null);
		getFromSymAction.setEnabled(theInstance != null);
	}
	
	/**
	 * @param instanceId
	 */
	protected void selectInstance(Integer instanceId, NavBoxItemIFace roc, boolean isRefresh) {
		//System.out.println("selecting instance " + instanceId);
		
		theInstance = getInstanceFromDB(instanceId);
		if (!isRefresh && starterPane != null) {
			((SymbiotaPane)starterPane).instanceSelected();
		} //else the pane probably doesn't need to know.
		
		for (NavBoxItemIFace nb : navBox.getItems()) {
			if (nb instanceof RolloverCommand) {
				//RolloverCommand.class.cast(nb).setActive(nb == roc);
				RolloverCommand.class.cast(nb).setIsAccented(nb == roc);
			}
		}
		
		updateActionEnablement();
	}

	
	/**
	 * @param id
	 * @return
	 */
	protected SpSymbiotaInstance getInstanceFromDB(Integer id) {
		DataProviderSessionIFace theSession = DataProviderFactory.getInstance().createSession();
		try {
			SpSymbiotaInstance result = theSession.get(SpSymbiotaInstance.class, id);
			result.forceLoad();
			return result;
		} finally {
			theSession.close();
		}
	}
	
	/**
	 * @param instance
	 */
	public void updateLastPushForInstance(SpSymbiotaInstance instance) {
		updateLastPushOrPullForInstance(instance, true);
	}
	
	/**
	 * @param instance
	 * @param push
	 */
	protected void updateLastPushOrPullForInstance(SpSymbiotaInstance instance, boolean push) {
		DataProviderSessionIFace theSession = DataProviderFactory.getInstance().createSession();
		try {
			SpSymbiotaInstance mergedInstance = theSession.merge(instance);
			if (push) {
				mergedInstance.setLastPush(Calendar.getInstance());
			} else {
				mergedInstance.setLastPull(Calendar.getInstance());
			}
			theSession.beginTransaction();
			theSession.saveOrUpdate(mergedInstance);
			theSession.commit();
			mergedInstance.forceLoad();
			this.theInstance = mergedInstance;
		} catch (Exception ex) {
			UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SymbiotaTask.class, ex);
			//UIRegistry.displayStatusBarErrMsg(getResStr());
		} finally {
			theSession.close();
		}
	}

	/**
	 * @return
	 */
	protected List<SpSymbiotaInstance> getInstancesFromDB() {
		DataProviderSessionIFace theSession = DataProviderFactory.getInstance().createSession();
		try {
			List<SpSymbiotaInstance> result = theSession.getDataList(SpSymbiotaInstance.class, "collectionMemberId", AppContextMgr.getInstance().getClassObject(Collection.class).getId());
			for (SpSymbiotaInstance i : result) {
				i.forceLoad();
			}
			return result;
		} finally {
			theSession.close();
		}
	}
	
	/**
	 * @return
	 */
	protected List<SpSymbiotaInstance> getInstances() {
		List<SpSymbiotaInstance> result = getInstancesFromDB();
		Collections.sort(result, new Comparator<SpSymbiotaInstance>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(SpSymbiotaInstance o1, SpSymbiotaInstance o2) {
				return o1.getInstanceName().compareTo(o2.getInstanceName());
			}
			
		});
		return result;
	}
	
	
	/**
	 * 
	 */
	protected void makeNewInstance() {
		//System.out.println("makeNewInstance()");
		SpExportSchemaMapping instanceMapping = pickSchemaMapping();
		if (instanceMapping != null) {
			System.out.println("selected: " + instanceMapping.getMappingName());
	        Frame   parentFrame  = (Frame)UIRegistry.get(UIRegistry.FRAME);
	        String  displayName  = "SYM_INSTANCE_DISPLAY_NAME"; 	        
	        boolean isEdit       = true;
	        String  closeBtnText = (isEdit) ? getResourceString("SAVE") : getResourceString("CLOSE"); 
	        String  className    = SpSymbiotaInstance.class.getName();
	        DBTableInfo nodeTableInfo = DBTableIdMgr.getInstance().getInfoById(SpSymbiotaInstance.getClassTableId());
	        String  idFieldName  = nodeTableInfo.getIdFieldName();
	        int     options      = MultiView.HIDE_SAVE_BTN;
	        	        
	        // create the form dialog
	        String title = getResourceString("SymbiotaTask.DataEntryFormTitle"); 
	        ViewBasedDisplayDialog dialog = new ViewBasedDisplayDialog(parentFrame, null, "SpSymbiotaInstance", displayName, title, 
	                                                                   closeBtnText, className, idFieldName, isEdit, options);
	        SpSymbiotaInstance spym = new SpSymbiotaInstance();
	        spym.initialize();
	        spym.setSchemaMapping(instanceMapping);
	        
	        dialog.setModal(true);
	        dialog.setData(spym);
	        dialog.preCreateUI();
	        dialog.setVisible(true);
	        
	        // the dialog has been dismissed by the user
	        if (dialog.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN) {
	        	System.out.println("Hey. It's Ok.");
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                try {
                	session.beginTransaction();
                	session.save(spym);
                	session.commit();
                	final Integer spymId = spym.getId();
                	SwingUtilities.invokeLater(new Runnable() {

						/* (non-Javadoc)
						 * @see java.lang.Runnable#run()
						 */
						@Override
						public void run() {
							addInstances();
							selectInstance(spymId, getNavBottomItemForInstance(spymId), false);
						}
                		
                	});
                } catch (Exception ex) {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SymbiotaTask.class, ex);
                } finally {
                	session.close();
                }
	        	
	        }
		}
		
	}

	/**
	 * @param instanceId
	 * @return
	 */
	protected NavBoxItemIFace getNavBottomItemForInstance(Integer instanceId) {
		for (NavBoxItemIFace b : navBox.getItems()) {
			if (instanceId.equals(b.getData())) {
				return b;
			}
		}
		return null;
	}
	/**
	 * @return
	 */
	protected SpExportSchemaMapping pickSchemaMapping() {
		List<SpExportSchemaMapping> maps = getAvailableMappings();
		if (maps.size() > 0) {
            ChooseFromListDlg<SpExportSchemaMapping> dlg = new ChooseFromListDlg<SpExportSchemaMapping>((Frame)UIRegistry.get(UIRegistry.FRAME),
                    getResourceString("SymbiotaTask.PickMappingDlgTitle"), 
                    getResourceString("SymbiotaTask.PickMappingDlgLbl"), 
                    ChooseFromListDlg.OKCANCELHELP, 
                    maps, 
                    null);
            dlg.setModal(true);
            UIHelper.centerAndShow(dlg);
            if (!dlg.isCancelled())	{
            	return dlg.getSelectedObject();
            } 			
		} else {
			UIRegistry.displayInfoMsgDlgLocalized("SymbiotaTask.NoMappingsAvailableMsg", Object[].class.cast(null));
		}
		return null;
	}
	
	
	/**
	 * @return
	 */
	protected List<SpExportSchemaMapping> getAvailableMappings() {
		String testSql1 = "SELECT SpExportSchemaItemMappingID FROM spexportschemaitemmapping im INNER JOIN spqueryfield qf ON "
				+ "qf.SpQueryFieldID = im.SpQueryFieldID WHERE qf.IsDisplay AND im.ExportSchemaItemID IS NULL AND " 
				+ "im.SpExportSchemaMappingid=%d";
		String testSql2 = "SELECT SpSymbiotaInstanceID FROM spsymbiotainstance WHERE SchemaMappingID=%d";
		List<SpExportSchemaMapping> result = new ArrayList<SpExportSchemaMapping>();
		DataProviderSessionIFace theSession = DataProviderFactory.getInstance().createSession();
		try {
			List<SpExportSchemaMapping> mappingsInCollection = theSession.getDataList(SpExportSchemaMapping.class, "collectionMemberId", AppContextMgr.getInstance().getClassObject(Collection.class).getId());
			for (SpExportSchemaMapping m : mappingsInCollection) {
				List<Object> unConceptedFields = BasicSQLUtils.querySingleCol(String.format(testSql1, m.getSpExportSchemaMappingId()));
				List<Object> existingSymInst = BasicSQLUtils.querySingleCol(String.format(testSql2, m.getSpExportSchemaMappingId()));
				if (unConceptedFields.size() == 0 && existingSymInst.size() == 0) {
					m.forceLoad();
					result.add(m);
				}
			}
			return result;
		} finally {
			theSession.close();
		}
	}
	
	/**
	 * @return
	 */
	public SpSymbiotaInstance getTheInstance() {
		return theInstance;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#preInitialize()
	 */
	@Override
	public void preInitialize() 
	{
		super.preInitialize();
        CommandDispatcher.register(SYMBIOTA, this);
        actionNavBox = new NavBox(UIRegistry.getResourceString("Actions"));
    	addActions();
	}



	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#initialize()
	 */
	@Override
	public void initialize() {
        if (!isInitialized) {
        	super.initialize(); 
        	navBox = new DroppableNavBox(UIRegistry.getResourceString("SymbiotaTask.SymbiotaInstances"), SYMBIOTA_FLAVOR, SYMBIOTA, SELECT_INSTANCE);
            addInstances();
            List<SpSymbiotaInstance> instances = getInstances();
            theInstance = instances != null && instances.size() > 0 ? getInstances().get(0) : null;
            
            navBoxes.add(actionNavBox);
            navBoxes.add(navBox);
            
            updateActionEnablement();
        }
        isShowDefault = true;
	}



	@Override
	public List<ToolBarItemDesc> getToolBarItems() {
        toolbarItems = new Vector<ToolBarItemDesc>();

        String label    = UIRegistry.getResourceString(name);
        String localIconName = name;
        String hint     = UIRegistry.getResourceString("search_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label,localIconName,hint,null);
        toolbarItems.add(new ToolBarItemDesc(btn));

        return toolbarItems;

	}



	/**
	 * @return the theSymMap
	 */
	public SpExportSchemaMapping getSchemaMapping() {
		return theInstance == null ? null : theInstance.getSchemaMapping();
	}



	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
	 */
	@Override
	public void doCommand(CommandAction cmdActionArg) {
		super.doCommand(cmdActionArg);
		
        if (cmdActionArg.isType("Symbiota")) {
            processSymTaskCommand(cmdActionArg);
            
        }

	}

	
	/**
	 * @param cmdActionArg
	 */
	protected void processSymTaskCommand(CommandAction cmdActionArg) {
        if (cmdActionArg.isAction(DELETE_CMD_ACT) && cmdActionArg.getData() instanceof Integer) {
            Integer instanceId = Integer.class.cast(cmdActionArg.getData());
            if (deleteSymInstance(instanceId)) {
                deleteSymInstanceFromUI(instanceId);
            }
            return;
        }
	}
	
	/**
	 * @param recordSet
	 * @return
	 */
	protected boolean deleteSymInstance(Integer instanceId) {
		boolean result = false;
		if (okToDeleteSymInstance(instanceId)) {
			DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
			try {
				SpSymbiotaInstance spym = session.get(SpSymbiotaInstance.class, instanceId);
				session.beginTransaction();
				session.delete(spym);
				session.commit();
				result = true;
			} catch (Exception ex) {
				edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
				edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SymbiotaTask.class, ex);
				result = false;
			} finally {
				session.close();
			}
		}
		return result;
	}
	
	/**
	 * @param instanceId
	 * @return
	 */
	protected boolean okToDeleteSymInstance(Integer instanceId) {
		boolean result = false;
		
		List<SpSymbiotaInstance> spyms = getInstances();
		SpSymbiotaInstance spymToDump = null;
		for (SpSymbiotaInstance spym : spyms) {
			if (instanceId.equals(spym.getId())) {
				spymToDump = spym;
				break;
			}
		}
		
		String spymName = spymToDump == null ? /*huh?*/ instanceId.toString() : spymToDump.toString();
        int option = JOptionPane.showOptionDialog(UIRegistry.getMostRecentWindow(), 
                String.format(UIRegistry.getResourceString("SymbiotaTask.ConfirmDelete"), spymName),
                UIRegistry.getResourceString("SymbiotaTask.ConfirmDeleteTitle"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION); 
        
        
        if (option == JOptionPane.YES_OPTION) {
        	//check for cache and stuff ?? nah... delete the instance, leave its mapping alone.
        	result = true;
        }
        return result;
	}
	
	/**
	 * @param recordSet
	 */
	protected void deleteSymInstanceFromUI(Integer instanceId) {
		SwingUtilities.invokeLater(new Runnable() {

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				addInstances();
				List<SpSymbiotaInstance> instances = getInstances();
				if (instances.size() > 0) {
					selectInstance(instances.get(0).getId(), getNavBottomItemForInstance(instances.get(0).getId()), false);
				} else {
					theInstance = null;
					updateActionEnablement();
					((SymbiotaPane)starterPane).instanceSelected();
				}
			}
		});
	}
}
