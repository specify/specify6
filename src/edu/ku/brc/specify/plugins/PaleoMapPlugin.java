/**
 * 
 */
package edu.ku.brc.specify.plugins;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.NotImplementedException;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.PaleoContext;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timo
 *
 */
@SuppressWarnings("serial")
public class PaleoMapPlugin extends JButton implements GetSetValueIFace, UIPluginable, PropertyChangeListener {
    protected Object                       origData     = null;
    protected FormViewObj formViewObj = null;
    protected String                   pcRel        = null;
    
    protected boolean                      isViewMode   = true;
    protected JComponent saveBtn = null;

	/**
	 * 
	 */
	public PaleoMapPlugin() {
		Discipline d = AppContextMgr.getInstance().getClassObject(Discipline.class);
		pcRel = d.getPaleoContextChildTable();
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                doButtonAction();
            }
        });
	}
	
    /**
     * 
     */
    protected void doButtonAction() {
    	List<Object> info = getMapInfo();
    	Locality locality = (Locality)info.get(0);
    	GeologicTimePeriod geoTime = (GeologicTimePeriod)info.get(1);
    	Float ma = null;
    	if (geoTime != null && (geoTime.getStartPeriod() != null || geoTime.getEndPeriod() != null)) {
    		if (geoTime.getStartPeriod() != null && geoTime.getEndPeriod() != null) {
    			ma = (geoTime.getStartPeriod() + geoTime.getEndPeriod()) / 2.0F;
    		} else if (geoTime.getStartPeriod() != null) {
    			ma = geoTime.getStartPeriod();
    		} else if (geoTime.getEndPeriod() != null) {
    			ma = geoTime.getEndPeriod();
    		}
    	}
    	if (ma != null && locality != null && locality.getLatitude1() != null && locality.getLongitude1() != null) {
    		 try {
    			 URI uri = new URI(String.format("http://paleolocation.org/map?lat=%s&amp;lng=%s&amp;ma=%s&amp;embed", locality.getLatitude1().toString(), locality.getLongitude1().toString(),
    				 ma.toString()));
    			 Desktop.getDesktop().browse(uri);
    		 } catch (Exception e) {
    			 e.printStackTrace();
    		 }
    	} else {
    		UIRegistry.displayInfoMsgDlg(getResourceString("PaleoMapPlugin.DataMissingMsg"));
    	}
    	 
    }



	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#initialize(java.util.Properties, boolean)
	 */
	@Override
	public void initialize(Properties properties, boolean isViewMode) {
        this.isViewMode = isViewMode;
        setIcon(IconManager.getIcon("LocalityMapper"));
        setText(getResourceString("PaleoMapPlugin.ButtonText"));
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#setCellName(java.lang.String)
	 */
	@Override
	public void setCellName(String cellName) {
		// no op
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#getUIComponent()
	 */
	@Override
	public JComponent getUIComponent() {
		return this;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#shutdown()
	 */
	@Override
	public void shutdown() {
		//no op
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#setParent(edu.ku.brc.af.ui.forms.FormViewObj)
	 */
	@Override
	public void setParent(FormViewObj parent) {
		formViewObj = parent;
	}

	/**
	 * 
	 */
	protected void listenToSaveBtn() {
		if (formViewObj != null) {
			FormViewObj fvo = formViewObj;
			while (fvo != null && fvo.getSaveComponent() == null) {
				MultiView mv = fvo.getMVParent().getMultiViewParent();
				fvo = mv == null ? null : mv.getCurrentViewAsFormViewObj();
			}
			if (fvo != null && fvo.getSaveComponent() != null) {
				if (saveBtn != fvo.getSaveComponent()) {
					if (saveBtn != null) {
						saveBtn.removePropertyChangeListener(this);
					}
					saveBtn = fvo.getSaveComponent();
					saveBtn.addPropertyChangeListener(this);
				}
			}
		}		
	}
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
	 */
	@Override
	public boolean isNotEmpty() {
        throw new NotImplementedException("isNotEmpty not implement!");
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#canCarryForward()
	 */
	@Override
	public boolean canCarryForward() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#getCarryForwardFields()
	 */
	@Override
	public String[] getCarryForwardFields() {
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#getTitle()
	 */
	@Override
	public String getTitle() {
		return "Paleo Map";
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
	 */
	@Override
	public String[] getFieldNames() {
        return null;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#setNewObj(boolean)
	 */
	@Override
	public void setNewObj(boolean isNewObj) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.UIPluginable#carryForwardStateChange()
	 */
	@Override
	public void carryForwardStateChange() {
		// TODO Auto-generated method stub

	}

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener(final ChangeListener listener)
    {
    	//intercept the the add. We don't want the validator listening to this plugin.
    }

	/**
	 * @return
	 */
	protected List<Object> getMapInfo() {
		Locality locality = null;
		PaleoContext pc = null;
		CollectionObject colObj = null;
		CollectingEvent ce = null;
	    GeologicTimePeriod geoTime      = null;
	    Object dataObj =  formViewObj != null ? formViewObj.getDataObj() : origData;
        if (dataObj != null) {
            if (dataObj instanceof CollectionObject) {
                colObj = (CollectionObject)dataObj;
                ce     = colObj.getCollectingEvent();
                if (ce != null) {
                    locality = ce.getLocality();
                }
                
            } else if (dataObj instanceof CollectingEvent) {
                ce       = (CollectingEvent)dataObj;
                locality = ce.getLocality(); // ce can't be null
                
            } else if (dataObj instanceof Locality) {
                locality = (Locality)dataObj;
                
            }
            if ("locality".equalsIgnoreCase(pcRel) && locality !=  null) {
            	pc = locality.getPaleoContext();
            } else if ("collectingevent".equalsIgnoreCase(pcRel) && ce !=  null) {
            	pc = ce.getPaleoContext();
            } else if ("collectionobject".equalsIgnoreCase(pcRel) && colObj !=  null) {
            	pc = colObj.getPaleoContext();
            }
            if (pc != null) {
            	geoTime = pc.getChronosStrat();
            }
        }        
        List<Object> result = new ArrayList<Object>();
        result.add(locality);
        result.add(geoTime);
        return result;
	}
	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
	 */
	@Override
	public void setValue(Object value, String defaultValue) {
        origData = value;
        listenToSaveBtn();
        setEnabled(true);
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
	 */
	@Override
	public Object getValue() {
		return origData;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("enabled")) {
        	setEnabled(!(Boolean)evt.getNewValue());
        }
		
	}

	
}
