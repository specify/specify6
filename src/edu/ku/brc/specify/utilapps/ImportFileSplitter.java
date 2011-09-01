/**
 * 
 */
package edu.ku.brc.specify.utilapps;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.helpers.UIFileFilter;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.rstools.ExportFileConfigurationFactory;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.IconManager.IconSize;

/**
 * 
 * Reads a csv or xls file and splits it into smaller files with a specified number of rows.
 * 
 * For example, a file called BigChunk.xls with 6300 rows would be split into the files
 * BigChunk_1.xls, BigChunk_2.xls, BigChunk_3.xls, and BigChunk_4.xls.
 * 
 * The default chunk size is 2000 records.
 * 
 * @author timbo
 *
 */
@SuppressWarnings("serial")
public class ImportFileSplitter extends CustomDialog
{
	private static final Logger log = Logger.getLogger(ImportFileSplitter.class);

	protected final int defaultChunkSize = edu.ku.brc.specify.tasks.WorkbenchTask.MAX_ROWS;
	protected JTextField fileName;
	protected JCheckBox headerChk;
	protected PosIntTextField fileSize;
	protected JProgressBar progBar;
	protected javax.swing.SwingWorker<ChunkageReport, Integer> worker = null;
	
	public ImportFileSplitter()
	{
		super((Frame )null, UIRegistry.getResourceString("ImportFileSplitter.Title"), true, OKCANCELHELP, null);
	}
	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.CustomDialog#createUI()
	 */
	@Override
	public void createUI()
	{
		super.createUI();
			
		//this.setHelpContext("CHANGE_PWD");
		this.setOkLabel(UIRegistry.getResourceString("ImportFileSplitter.SplitBtn"));
		this.setCancelLabel(UIRegistry.getResourceString("CLOSE"));
		
		PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu, f:p, 1dlu, f:max(150dlu;p):g, 1dlu, f:p, 5dlu", "8dlu, p, 2dlu, p, 2dlu, p, 9dlu"));
		CellConstraints cc = new CellConstraints();
		pb.add(UIHelper.createLabel(UIRegistry.getResourceString("ImportFileSplitter.FileToSplit")), cc.xy(2, 2)); 
		fileName = UIHelper.createTextField();
		pb.add(fileName, cc.xy(4, 2));
		JButton chooseBtn = UIHelper.createButton(UIRegistry.getResourceString("ImportFileSplitter.ChooseFile")); 
		chooseBtn.addActionListener(new ActionListener(){

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				chooseFile();
			}
			
		});
		pb.add(chooseBtn, cc.xy(6, 2));
		
		headerChk = UIHelper.createCheckBox(UIRegistry.getResourceString("ImportFileSplitter.FirstLineHeaders"));
		headerChk.setSelected(true);
		pb.add(headerChk, cc.xy(4, 4));
		
		pb.add(UIHelper.createLabel(UIRegistry.getResourceString("ImportFileSplitter.FileSize")), cc.xy(2, 6)); 
		//fileSize = UIHelper.createTextField(5);
		fileSize = new PosIntTextField(10000);
		fileSize.setText(String.valueOf(defaultChunkSize));
		pb.add(fileSize, cc.xy(4, 6));
		
		PanelBuilder mainPb = new PanelBuilder(new FormLayout("f:p:g", "p, f:max(15dlu;p)"));
		PanelBuilder progPb = new PanelBuilder(new FormLayout("5dlu, f:p:g, 5dlu", "p"));
		progBar = new JProgressBar();
		progBar.setVisible(false);
		progBar.setMinimum(0);
		progBar.setMaximum(100);
		progBar.setValue(0);
		progPb.add(progBar, cc.xy(2, 1));
		mainPb.add(pb.getPanel(), cc.xy(1, 1));
		mainPb.add(progPb.getPanel(), cc.xy(1, 2));
		
        ImageIcon icon = IconManager.getIcon("Splicer");
        if (icon != null)
        {
        	JLabel iconLbl = new JLabel(icon);
        	iconLbl.setBorder(BorderFactory.createEmptyBorder(10, 10, 2, 2));
        	PanelBuilder contentPb = new PanelBuilder(new FormLayout("f:p, f:p:g", "f:p:g"));
        	contentPb.add(iconLbl, cc.xy(1, 1));
        	contentPb.add(mainPb.getPanel(), cc.xy(2,1));
        	contentPanel = contentPb.getPanel();
        }
        else
        {
        	contentPanel = mainPb.getPanel();
        }
        
		mainPanel.add(contentPanel, BorderLayout.CENTER);
		
		this.setHelpContext("slicer");
		
		pack();
	}

	/**
	 * Opens file chooser dialog and sets fileName to selected file.
	 */
	protected void chooseFile()
	{
		JFileChooser chooser = new JFileChooser();
		UIFileFilter ff = new UIFileFilter();
		ff.setDescription(UIRegistry.getResourceString("ImportFileSplitter.ExcelAndCSVFiles")); 
		ff.addExtension("xls");
		ff.addExtension("csv");
		ff.addExtension("txt");
        chooser.setDialogTitle(UIRegistry.getResourceString("ImportFileSplitter.FileDlgTitle")); 
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(ff);
        
		int choice = chooser.showOpenDialog(this);
		if (choice == JFileChooser.APPROVE_OPTION)
		{
			fileName.setText(chooser.getSelectedFile().getPath());
		}
	}
	
	
    /**
     * @param file
     * @return mimeType based on the file extension.
     */
    protected String getMimeType(final File file)
    {
        String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
        if (extension.equalsIgnoreCase("xls"))
        {
            return ExportFileConfigurationFactory.XLS_MIME_TYPE;
            
        } else if (extension.equalsIgnoreCase("csv") || extension.equalsIgnoreCase("txt"))
        {
            return ExportFileConfigurationFactory.CSV_MIME_TYPE;
        }
        return "";
    }

    /**
     * @param file
     * @return true if file is an XLS file.
     */
    protected boolean isXLS(final File file)
    {
    	return getMimeType(file).equals(ExportFileConfigurationFactory.XLS_MIME_TYPE);
    }
    
    /**
     * @param file
     * @return true if file is a CSV file.
     */
    protected boolean isCSV(final File file)
    {
    	return getMimeType(file).equals(ExportFileConfigurationFactory.CSV_MIME_TYPE);
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
	 */
	@Override
	protected void okButtonPressed()
	{
		File toChunk = new File(fileName.getText());
		if (!toChunk.exists())
		{
			UIRegistry.displayErrorDlg(UIRegistry.getResourceString("ImportFileSplitter.InvalidFileName")); 
			return;
		}
		if (fileSize.getValue() > defaultChunkSize)
		{
			if (!UIRegistry.displayConfirmLocalized("ImportFileSplitter.BigLinesConfirmTitle",
					"ImportFileSplitter.BigLinesConfirmMsg", "OK", "Cancel", JOptionPane.WARNING_MESSAGE))
			{
				return;
			}
		}
		chunkFile(toChunk, fileSize.getValue());
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.CustomDialog#cancelButtonPressed()
	 */
	@Override
	protected void cancelButtonPressed()
	{
		boolean cancelled = true;
		if (worker != null && !worker.isDone())
		{
			 cancelled = worker.cancel(true);
		}
		if (cancelled)
		{
			super.cancelButtonPressed();
		}
		else
		{
			UIRegistry.showLocalizedMsg("ImportFileSplitter.TooLateToCancel");
		}
	}


	protected void chunkFile(final File toChunk, final int aChunkSize)
	{
		if (!isXLS(toChunk) && !isCSV(toChunk))
		{
			UIRegistry.displayErrorDlg("ImportFileSplitter.UnrecognizedFileType");
			return;
		}

		SwingUtilities.invokeLater(new Runnable(){

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run()
			{
				progBar.setIndeterminate(true);
				progBar.setVisible(true);
				pack();
			}
			
		});

		worker = new javax.swing.SwingWorker<ChunkageReport, Integer>() {

			ChunkageReport result = null;
			InputStream     input    = null;
			POIFSFileSystem fs       = null;
			HSSFWorkbook    workBook = null;
			HSSFSheet       sheet    = null;
			int             maxCols = 0;
			int 			chunkSize = aChunkSize;
			boolean         hasHeaders = headerChk.isSelected();
			
			protected void writeXLSChunk(final File toChunkArg, final HSSFWorkbook workBookOut, final int fileNum, final int numFiles,
					final String firstLine) throws FileNotFoundException, IOException
			{
	        	String fName = toChunkArg.getAbsolutePath();
	            String ext = "." + FilenameUtils.getExtension(fName);
	        	fName = fName.substring(0, fName.lastIndexOf(ext)) + "_" + fileNum;
	            FileOutputStream fos = new FileOutputStream(fName + ext);
	            workBookOut.write(fos);
	            fos.close();
	            Float ratio = new Float((float )fileNum / (float )numFiles);
	            Float prog = new Float(Math.max(0., Math.min(100., ratio.floatValue() * 100.)));
	            setProgress(prog.intValue());
	            result.addChunk(fName, firstLine);
			}
			
			protected void checkXLS()
			{
				Iterator<?> rows = sheet.rowIterator();
				int numRows = sheet.getLastRowNum() - sheet.getFirstRowNum() + 1;
				int rowNum = 0;
				setProgress(0);
                while (rows.hasNext())
                {
                	HSSFRow row = (HSSFRow )rows.next();
                	rowNum++;
                	int maxRowCol = row.getLastCellNum();
                	if (maxRowCol > maxCols)
                	{
                		maxCols = maxRowCol;
                	}
    	            Float ratio = new Float((float )rowNum / (float )numRows);
    	            Float prog = new Float(Math.max(0., Math.min(100., ratio.floatValue() * 100.)));
                	setProgress(prog.intValue());
                }
			}
			
			protected ChunkageReport chunkXLS(final File toChunkArg)
			{
				result = new ChunkageReport(toChunkArg);
				result.setSuccess(false);
				if (hasHeaders)
				{
					chunkSize++;
				}
				try
				{
					input    = new FileInputStream(toChunkArg);
					fs       = new POIFSFileSystem(input);
					workBook = new HSSFWorkbook(fs);
					sheet    = workBook.getSheetAt(0);
					HSSFRow headerRow = null;
					int             numRows  = sheet.getLastRowNum() - sheet.getFirstRowNum() + 1;
					if (hasHeaders)
					{
						numRows--;
					}
					int				numFiles = numRows / aChunkSize;
					int             leftover = numRows - (numFiles * aChunkSize);
					numFiles++;
					checkXLS();
					boolean go = false;
					if (!isCancelled())
					{
						go = UIRegistry.displayConfirm(UIRegistry.getResourceString("ImportFileSplitter.FileInfoTitle"), 
							String.format(UIRegistry.getResourceString("ImportFileSplitter.FileInfo"), 
							numRows, numFiles-1, 
							aChunkSize, leftover),
							UIRegistry.getResourceString("OK"), UIRegistry.getResourceString("CANCEL"), JOptionPane.INFORMATION_MESSAGE);
					}
					if (!go || isCancelled())
					{
						result.setCancelled(true);
					}
					else
					{
						setProgress(0);
		                int fileNum = 0;
						boolean newFile = true;
		                Iterator<?> rows = sheet.rowIterator();
				        HSSFWorkbook workBookOut  = null;
				        HSSFSheet    sheetOut = null;
				        int rowNum = 0;
				        short styleIdxOffset = 0;
				        String firstLine = null;
				        boolean wroteHeaders = false;
						while (rows.hasNext())
						{
							if (newFile)
							{
								if (workBookOut != null)
								{
									fileNum++;
							        try
							        {
							        	writeXLSChunk(toChunkArg, workBookOut, fileNum, numFiles, firstLine);
							        } 
							        catch (Exception e)
							        {
							            result.setMessage(e.getMessage());
							            return result;
							        }
								}
						        workBookOut = new HSSFWorkbook();
						        styleIdxOffset = workBookOut.getNumCellStyles();
						        for (short s = 0; s < workBook.getNumCellStyles(); s++)
						        {
						        	workBookOut.createCellStyle();
						        	workBookOut.getCellStyleAt(s).cloneStyleFrom(workBook.getCellStyleAt(s));
						        }
						        //Block below was added to try fix sporadic bug with copying formulas
						        //but it didn't help.
//						        for (int r = 0; r < workBook.getNumberOfNames(); r++)
//						        {
//						        	workBookOut.createName();
//						        	HSSFName name = workBookOut.getNameAt(r);
//						        	name.setNameName(workBook.getNameAt(r).getNameName());
//						        	if (workBook.getNameAt(r).getReference() !=  null)
//						        	{
//						        		name.setReference(workBook.getNameAt(r).getReference());
//						        	}
//						        }
						        sheetOut = workBookOut.createSheet();		
					            newFile = false;
						        rowNum = 0;
						        wroteHeaders = false;
							}
							HSSFRow rowIn;
							if (hasHeaders && !wroteHeaders)
		                    {
		                    	if (headerRow == null)
		                    	{
		                    		headerRow = (HSSFRow) rows.next();
		                    	}
	                    		rowIn = headerRow;
	                    		wroteHeaders = true;
		                    }
							else
							{
								rowIn = (HSSFRow) rows.next();
							}
		                    HSSFRow rowOut = sheetOut.createRow(rowNum);
		                    int cellNum = 0;
		                    while (cellNum < maxCols)
		                    {
		                    	HSSFCell cellIn = rowIn.getCell(cellNum, HSSFRow.CREATE_NULL_AS_BLANK);
		                    	HSSFCell cellOut = rowOut.createCell(cellNum);
		                    	cellNum++;
		                        cellOut.setCellType(cellIn.getCellType());
		                        short styleIdx = -1;
		                        HSSFCellStyle inStyle = cellIn.getCellStyle();
		                        for (short s = 0; s < workBook.getNumCellStyles(); s++)
		                        {
		                        	if (workBook.getCellStyleAt(s).equals(inStyle))
		                        	{
		                        		styleIdx = (short)(s + styleIdxOffset);
		                        		break;
		                        	}
		                        }
		                        if (styleIdx != -1)
		                        {
		                        	try
		                        	{
		                        		cellOut.setCellStyle(workBookOut.getCellStyleAt(styleIdx));
		                        	} catch (Exception ex)
		                        	{
		                        		//That didn't work. HSSF in action.
		                        		String msg = String.format(UIRegistry.getResourceString("ImportFileSplitter.CellStyleCopyErrMsg"),
		                        				rowNum+1, cellNum+1, fileNum, ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
			                        	log.error(msg);
			                        	result.setProblems(true);
			                        	result.getReport().add(msg);
		                        	}
		                        }
	                        	switch (cellIn.getCellType())
	                        	{
		                        	case HSSFCell.CELL_TYPE_NUMERIC:
		                        		cellOut.setCellValue(cellIn.getNumericCellValue());
		                        		break;

	                        		case HSSFCell.CELL_TYPE_STRING:
	                        			cellOut.setCellValue(cellIn.getRichStringCellValue());
	                        			break;

	                        		case HSSFCell.CELL_TYPE_BOOLEAN:
	                        			cellOut.setCellValue(cellIn.getBooleanCellValue());
	                        			break;
		                        		
	                        		case HSSFCell.CELL_TYPE_FORMULA:
	    	                        	try
	    	                        	{
	    	                        		cellOut.setCellFormula(cellIn.getCellFormula());
	    	                        	}
	    	                        	catch (Exception ex)
	    	                        	{
	    		                        	//That didn't work. HSSF in action.
	    	                        		String msg = String.format(UIRegistry.getResourceString("ImportFileSplitter.FormulaCellCopyErrMsg"),
	    	                        				rowNum+1, cellNum+1, fileNum, ex.getClass().getSimpleName() + ": " + ex.getLocalizedMessage());
	    		                        	log.error(msg);
	    		                        	result.setProblems(true);
	    		                        	result.getReport().add(msg);
	    	                        	}
	    	                        	break;
	    	                        	
		                        	case HSSFCell.CELL_TYPE_ERROR:
		                        		cellOut.setCellErrorValue(cellIn.getErrorCellValue());
		                        		break;
		                        		
		                        	case HSSFCell.CELL_TYPE_BLANK:
		                        		break;
		                        		
		                        	default:
		                        		throw new Exception(String.format(
		                        			UIRegistry.getResourceString("ImportFileSplitter.UnrecognizedCellTypeNotCopied"),
		                        			cellIn.getCellType()));
	                        	} 
		                    }
		                    if ((rowNum == 0 && !hasHeaders) || (rowNum == 1 && hasHeaders))
		                    {
		                    	StringBuilder bldr = new StringBuilder();
		                    	Iterator<?> cells = rowOut.cellIterator();
		                    	while (cells.hasNext())
		                    	{
		                    		HSSFCell cell = (HSSFCell )cells.next();
		                    		bldr.append(cell.toString());
		                    		if (cells.hasNext())
		                    		{
		                    			bldr.append("\t");
		                    		}
		                    	}
		                    	firstLine = bldr.toString();
		                    }
		                    rowNum++;
		                    newFile = rowNum == chunkSize;
						}
						if (rowNum > 0)
						{
							fileNum++;
							writeXLSChunk(toChunkArg, workBookOut, fileNum, numFiles, firstLine);
						}
						result.setSuccess(true);
					}
				}
				catch (Exception ex)
				{
		            log.error(ex);
		            result.setMessage(ex.getClass().getSimpleName() + (StringUtils.isBlank(ex.getLocalizedMessage()) ? "" : ": " + ex.getLocalizedMessage()));
				}
				return result;
			}
			
			protected ChunkageReport chunkCSV(final File toChunkArg)
			{
				result = new ChunkageReport(toChunkArg);
				result.setSuccess(false);
				if (hasHeaders)
				{
					chunkSize++;
				}
				try
				{
					LineIterator it = FileUtils.lineIterator(toChunkArg);
					int numRows = 0;
					while (it.hasNext())
					{
						numRows++;
						it.next();
					}
					if (hasHeaders)
					{
						numRows--;
					}
					int				numFiles = numRows / aChunkSize;
					int             leftover = numRows - (numFiles * aChunkSize);
					numFiles++;
					boolean go = UIRegistry.displayConfirm(UIRegistry.getResourceString("ImportFileSplitter.FileInfoTitle"), 
							String.format(UIRegistry.getResourceString("ImportFileSplitter.FileInfo"), 
							numRows, 
							numFiles-1, 
							aChunkSize, leftover),
							UIRegistry.getResourceString("OK"), UIRegistry.getResourceString("CANCEL"), JOptionPane.INFORMATION_MESSAGE);
					if (!go || isCancelled())
					{
						result.setCancelled(true);
					}
					else
					{
						setProgress(0);
		                int fileNum = 0;
				        String firstLine = null;
				        boolean wroteHeaders = false;
				        Vector<String> outLines = new Vector<String>(chunkSize);
						it = FileUtils.lineIterator(toChunkArg);
						String headerLine = null;
						String lineIn = null;
						while (it.hasNext())
						{
							if (outLines.size() == chunkSize)
							{
								fileNum++;
							    writeCSVChunk(toChunkArg, outLines, fileNum, numFiles, firstLine);
						        outLines.clear();
						        wroteHeaders = false;
							}
							if (hasHeaders && !wroteHeaders)
		                    {
		                    	if (headerLine == null)
		                    	{
		                    		headerLine = it.nextLine();
		                    	}
	                    		lineIn = headerLine;
	                    		wroteHeaders = true;
		                    }
							else
							{
								lineIn = it.nextLine();
							}
							outLines.add(lineIn);
		                    if ((outLines.size() == 1 && !hasHeaders) || (outLines.size() == 2 && hasHeaders))
		                    {
		                    	firstLine = lineIn;
		                    }
						}
						if (outLines.size() > 0)
						{
							fileNum++;
							writeCSVChunk(toChunkArg, outLines, fileNum, numFiles, firstLine);
						}
						result.setSuccess(true);	
					}
				}
				catch (Exception ex)
				{
		            log.error(ex);
		            result.setMessage(ex.getClass().getSimpleName() + (StringUtils.isBlank(ex.getLocalizedMessage()) ? "" : ": " + ex.getLocalizedMessage()));
				}
				return result;
			}

			protected void writeCSVChunk(final File toChunkArg, final Vector<String> outLines, final int fileNum, final int numFiles,
					final String firstLine) throws IOException
			{
	        	String fName = toChunkArg.getAbsolutePath();
	            String ext = "." + FilenameUtils.getExtension(fName);
	        	fName = fName.substring(0, fName.lastIndexOf(ext)) + "_" + fileNum;
	        	File file = new File(fName + ext);
	        	FileUtils.writeLines(file, outLines);
	            Float ratio = new Float((float )fileNum / (float )numFiles);
	            Float prog = new Float(Math.max(0., Math.min(100., ratio.floatValue() * 100.)));
	            setProgress(prog.intValue());
	            result.addChunk(fName, firstLine);
			}
			
			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#doInBackground()
			 */
			@Override
			protected ChunkageReport doInBackground() throws Exception
			{
				try
				{
					if (isXLS(toChunk))
					{
						return chunkXLS(toChunk);
					}
					else if (isCSV(toChunk))
					{
						return chunkCSV(toChunk);
					}
				}
				catch (Exception ex)
				{
					log.error(ex);
					result = new ChunkageReport(toChunk);
					result.setSuccess(false);
					result.setMessage(ex.getClass().getSimpleName() + (StringUtils.isBlank(ex.getLocalizedMessage()) ? "" : ": " + ex.getLocalizedMessage()));
					return result;
				}
				return null;
			}

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#done()
			 */
			@Override
			protected void done()
			{
				super.done();
				SwingUtilities.invokeLater(new Runnable(){

					/* (non-Javadoc)
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run()
					{
						progBar.setVisible(false);
						progBar.setValue(0);
					}
					
				});
				if (result != null)
				{
					if (!isCancelled())
					{
						if (result.isSuccess())
						{
							String msg = UIRegistry.getResourceString("ImportFileSplitter.Success");
							if (result.areProblems())
							{
					        	String logName = toChunk.getAbsolutePath();
					            String ext = "." + FilenameUtils.getExtension(logName);
					            logName = logName.substring(0, logName.lastIndexOf(ext)) + ".log";
								try
								{
									FileUtils.writeLines(new File(logName), result.getReport());
								} catch (IOException ex)
								{
									logName = UIRegistry.getResourceString("ImportFileSplitter.LogFileSaveError"); 
								}
								msg += "\n\n" + String.format(UIRegistry.getResourceString("ImportFileSplitter.CheckLogFile"),
										logName);
							}
							UIRegistry.displayInfoMsgDlg(msg);	
						}
						else if (!result.isCancelled())
						{
							UIRegistry.displayErrorDlg(StringUtils.isBlank(result.getMessage()) ? "File could not be split." : result.getMessage());
						}
					}
				}
			}
			
			
		};
        worker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if ("progress".equals(evt.getPropertyName())) 
                        {
                        	if (!progBar.isVisible())
                        	{
                				SwingUtilities.invokeLater(new Runnable(){

                					/* (non-Javadoc)
                					 * @see java.lang.Runnable#run()
                					 */
                					@Override
                					public void run()
                					{
                						progBar.setVisible(true);
                						pack();
                					}
                					
                				});
                        	}
                        	if (progBar.isIndeterminate())
                        	{
                				SwingUtilities.invokeLater(new Runnable(){

                					/* (non-Javadoc)
                					 * @see java.lang.Runnable#run()
                					 */
                					@Override
                					public void run()
                					{
                						progBar.setIndeterminate(false);
                					}
                					
                				});
                        	}
            				SwingUtilities.invokeLater(new Runnable(){

            					/* (non-Javadoc)
            					 * @see java.lang.Runnable#run()
            					 */
            					@Override
            					public void run()
            					{
                                	
            						progBar.setValue((Integer )evt.getNewValue());
            					}
            					
            				});
                        }
                    }
                });
		worker.execute();
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
        // Set App Name, MUST be done very first thing!
        UIRegistry.setAppName("ImportFileSplitter");  //$NON-NLS-1$
        
        for (String s : args)
        {
            String[] pairs = s.split("="); //$NON-NLS-1$
            if (pairs.length == 2)
            {
                if (pairs[0].startsWith("-D")) //$NON-NLS-1$
                {
                    //System.err.println("["+pairs[0].substring(2, pairs[0].length())+"]["+pairs[1]+"]");
                    System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
                } 
            } else
            {
                String symbol = pairs[0].substring(2, pairs[0].length());
                //System.err.println("["+symbol+"]");
                System.setProperty(symbol, symbol);
            }
        }
        
        // Now check the System Properties
        String appDir = System.getProperty("appdir");
        if (StringUtils.isNotEmpty(appDir))
        {
            UIRegistry.setDefaultWorkingPath(appDir);
        }
        
        String appdatadir = System.getProperty("appdatadir");
        if (StringUtils.isNotEmpty(appdatadir))
        {
            UIRegistry.setBaseAppDataDir(appdatadir);
        }
        
        
        // Then set this
        IconManager.setApplicationClass(Specify.class);
        IconManager.loadIcons(XMLHelper.getConfigDir("icons.xml")); //$NON-NLS-1$
        ImageIcon icon = IconManager.getIcon("AppIcon", IconManager.IconSize.Std16);
        
        try
        {
            ResourceBundle.getBundle("resources", Locale.getDefault()); //$NON-NLS-1$
            
        } catch (MissingResourceException ex)
        {
            Locale.setDefault(Locale.ENGLISH);
            UIRegistry.setResourceLocale(Locale.ENGLISH);
        }
        
        try
        {
            if (!System.getProperty("os.name").equals("Mac OS X"))
            {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
            }
        }
        catch (Exception e)
        {
            //whatever
        }
        
        ImageIcon helpIcon = IconManager.getIcon("AppIcon",IconSize.Std16); //$NON-NLS-1$
        HelpMgr.initializeHelp("SpecifyHelp", helpIcon.getImage());
        
        //JDialog jd = new JDialog();
        //jd.setModal(true);
        //jd.setTitle("DRAG ME");
        //UIHelper.centerAndShow(jd);
        
		ImportFileSplitter chunker = new ImportFileSplitter();
		//chunker.setCustomTitleBar(UIRegistry.getResourceString("ImportFileSplitter.Title"));
		chunker.setTitle(UIRegistry.getResourceString("ImportFileSplitter.Title"));
		chunker.setIconImage(icon.getImage());
		chunker.setHelpContext("slicer");
		UIHelper.centerAndShow(chunker);
		System.exit(0);
	}

	public class ChunkageReport
	{
		protected File chunked;
		protected int rows = 0;
		protected int chunks = 0;
		protected Vector<String> report = new Vector<String>();
		protected boolean problems = false;
		protected boolean success = false;
		protected String message = null;
		protected boolean cancelled = false;
		
		public ChunkageReport(final File chunked)
		{
			this.chunked = chunked;
		}
		
		public void addChunk(final String chunkFileName, final String chunkReport)
		{
			chunks++;
			report.add(chunkFileName + ": ");
		}

		/**
		 * @return the rows
		 */
		public int getRows()
		{
			return rows;
		}

		/**
		 * @param rows the rows to set
		 */
		public void setRows(int rows)
		{
			this.rows = rows;
		}

		/**
		 * @return the success
		 */
		public boolean isSuccess()
		{
			return success;
		}

		/**
		 * @param success the success to set
		 */
		public void setSuccess(boolean success)
		{
			this.success = success;
		}

		/**
		 * @return the message
		 */
		public String getMessage()
		{
			return message;
		}

		/**
		 * @param message the message to set
		 */
		public void setMessage(String message)
		{
			this.message = message;
		}

		/**
		 * @return the chunked
		 */
		public File getChunked()
		{
			return chunked;
		}

		/**
		 * @return the chunks
		 */
		public int getChunks()
		{
			return chunks;
		}

		/**
		 * @return the report
		 */
		public Vector<String> getReport()
		{
			return report;
		}

		/**
		 * @return the cancelled
		 */
		public boolean isCancelled()
		{
			return cancelled;
		}

		/**
		 * @param cancelled the cancelled to set
		 */
		public void setCancelled(boolean cancelled)
		{
			this.cancelled = cancelled;
		}

		/**
		 * @return problems
		 */
		public boolean areProblems() 
		{
			return problems;
		}

		/**
		 * @param problems the problems to set
		 */
		public void setProblems(boolean problems) 
		{
			this.problems = problems;
		}
		
		
	}
	
	private class PosIntTextField extends JTextField
	{
		private final int maxVal;
		
		/**
		 * @param maxVal
		 */
		public PosIntTextField(int maxVal)
		{
			super();
			this.maxVal = maxVal;
		}

		/* (non-Javadoc)
		 * @see javax.swing.JTextField#createDefaultModel()
		 */
		protected Document createDefaultModel()
		{
			return new IntTextDocument();
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#isValid()
		 */
		public boolean isValid()
		{
			try
			{
				Integer val = getValue();
				return (val >= 1 && val <= maxVal);
			} catch (NumberFormatException e)
			{
				return false;
			}
		}

		/**
		 * @return
		 */
		public int getValue()
		{
			try
			{
				if (getDocument() == null)
				{
					return 0;
				}
				return Integer.parseInt(getText());
			} catch (NumberFormatException e)
			{
				return 0;
			} 		
		}
		
		/**
		 * @author Tadmin
		 *
		 */
		class IntTextDocument extends PlainDocument
		{
			public void insertString(int offs, String str, AttributeSet a)
					throws BadLocationException
			{
				if (str == null)
					return;
				String oldString = getText(0, getLength());
				String newString = oldString.substring(0, offs) + str
						+ oldString.substring(offs);
				try
				{
					Integer newVal = Integer.parseInt(newString + "0");
					if (newVal >= 1 && newVal <= maxVal*10)
					{
						super.insertString(offs, str, a);
					}
				} catch (NumberFormatException e)
				{
				}
			}
		}
	}
}
