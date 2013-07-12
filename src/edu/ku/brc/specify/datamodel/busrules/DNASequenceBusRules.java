/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.datamodel.busrules;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.IllustrativeBarCodeUI;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.specify.datamodel.DNASequence;
import edu.ku.brc.specify.datamodel.DNASequencingRun;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.DocumentAdaptor;

public class DNASequenceBusRules extends AttachmentOwnerBaseBusRules implements CommandListener
{
    private static final Logger dnalog = Logger.getLogger(DNASequenceBusRules.class);
    
    protected IllustrativeBarCodeUI  barCodeUI = null;
    
    /**
     * 
     */
    public DNASequenceBusRules()
    {
        super(DNASequence.class);
        
        CommandDispatcher.register("Data_Entry", this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            barCodeUI = (IllustrativeBarCodeUI)formViewObj.getControlById("8");
            if (barCodeUI == null)
            {
                barCodeUI = new IllustrativeBarCodeUI();
            }
            
            if (barCodeUI != null)
            {
                final JTextArea ta = (JTextArea)formViewObj.getCompById("4");
                ta.getDocument().addDocumentListener(new DocumentAdaptor() {
                    @Override
                    protected void changed(DocumentEvent e)
                    {
                        if (isEditMode())
                        {
                            barCodeUI.setSequence(ta.getText());
                            adjustTotals();
                        }
                    }
                });
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeMerge(java.lang.Object,
     *      edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    /*@Override
    public void beforeMerge(final Object dataObj, 
                            final DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
        
        DNASequence dnaSeq = (DNASequence)dataObj;
        if (dnaSeq != null && AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent())
        {
            //CollectingEvent ce = colObj.getCollectingEvent();
            //if (ce != null)
            {
                try
                {
                    session.saveOrUpdate(dnaSeq);
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CollectionObjectBusRules.class, ex);
                    ex.printStackTrace();
                }
            }
        }
    }*/
    
    /**
     * @param id
     * @param value
     */
    private void setValue(final String id, final int value)
    {
        JTextField  tf = (JTextField)formViewObj.getControlById(id);
        if (tf != null)
        {
            tf.setText(Integer.toString(value));    
        } else
        {
            dnalog.debug("Couldn't find id["+id+"] " + value);
        }
        
    }
    
    /**
     * 
     */
    private void adjustTotals()
    {
        JTextArea ta = (JTextArea)formViewObj.getCompById("4");
        if (formViewObj != null&& barCodeUI != null && ta != null)
        {
            barCodeUI.setSequence(ta.getText());
            
            int compA = barCodeUI.getTotal('A');
            int compG = barCodeUI.getTotal('G');
            int compT = barCodeUI.getTotal('T');
            int compC = barCodeUI.getTotal('C');
            int compX = barCodeUI.getTotal('X');
            int total = compA + compG + compC + compT + compX;
            
            setValue("residues", total);
            setValue("compA", compA);
            setValue("compG", compG);
            setValue("compT", compT);
            setValue("compC", compC);
            setValue("ambiguous", compX);
            
            DNASequence dnaSeq = (DNASequence)formViewObj.getDataObj();
            if (dnaSeq != null)
            {
                dnaSeq.setCompA(compA);
                dnaSeq.setCompG(compG);
                dnaSeq.setCompT(compT);
                dnaSeq.setCompC(compC);
                dnaSeq.setTotalResidues(total);
                dnaSeq.setAmbiguousResidues(compX);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        if (formViewObj != null && dataObj != null)
        {
            adjustTotals();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
        CommandDispatcher.unregister("Data_Entry", this);
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        /* This was added as part of the BOLD demo
        if (cmdAction.isAction("CLOSE_SUBVIEW") && formViewObj != null)
        {
            Pair<Object, Object> data = (Pair<Object, Object>)cmdAction.getData();
            if (data.first instanceof DNASequence)
            {
                for (Object obj : (TreeSet<?>)data.second)
                {
                    DNASequenceAttachment dnaAttachment = (DNASequenceAttachment)obj;
                    if (dnaAttachment != null && dnaAttachment.getAttachment() != null && dnaAttachment.getAttachment().getAttachmentId() == null)
                    {
                        Random rand = new Random(12345678L);
                        char[] syms = {'A', 'C', 'T', 'G', };
                        StringBuilder sb = new StringBuilder();
                        for (int j=0;j<((8*75)+52);j++)
                        {
                            sb.append(syms[rand.nextInt(syms.length)]);
                        }
                        JTextArea ta = (JTextArea)formViewObj.getCompById("4");
                        ta.setText(sb.toString());
                        
                        ValFormattedTextFieldSingle seqDateTF = (ValFormattedTextFieldSingle)formViewObj.getCompById("3");
                        
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(2008, rand.nextInt(12)+1, rand.nextInt(28)+1, 06, 12, 00);
                        seqDateTF.setValue(calendar, null);
                    }
                }
            }
        }*/
    }
    
    /**
     * @param attOwner
     */
    @Override
    protected void addExtraObjectForProcessing(final Object dObjAtt)
    {
        super.addExtraObjectForProcessing(dObjAtt);
        
        DNASequence dnaseq = (DNASequence)dObjAtt;
        
        for (DNASequencingRun dnasr : dnaseq.getDnaSequencingRuns())
        {
            super.addExtraObjectForProcessing(dnasr);
        }
    }
}
