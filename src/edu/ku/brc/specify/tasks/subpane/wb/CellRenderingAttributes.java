package edu.ku.brc.specify.tasks.subpane.wb;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import edu.ku.brc.specify.datamodel.WorkbenchDataItem;

/**
 * 
 */

/**
 * @author timo
 *
 */
	/**
	 * @author timo
	 *
	 */
	@SuppressWarnings("unused")
	class CellRenderingAttributes
	{
		public Color errorBorder = Color.RED;
		public Color errorForeground = errorBorder;
		public Color errorBackground = new Color(errorBorder.getRed(), errorBorder.getGreen(), errorBorder.getBlue(), 37);
		public Color newDataBorder = Color.YELLOW;
		public Color newDataForeground = Color.YELLOW;
		public Color newDataBackground = new Color(newDataBorder.getRed(), newDataBorder.getGreen(), newDataBorder.getBlue(), 37);
		public Color multipleMatchBorder = Color.ORANGE;
		public Color multipleMatchBackground = new Color(multipleMatchBorder.getRed(), multipleMatchBorder.getGreen(), multipleMatchBorder.getBlue(), 37);
		public Color multipleMatchForeground = multipleMatchBorder;
		public Color notMatchedBorder = newDataBorder;
		public Color notMatchedBackground = null;
		public Color notMatchedForeground = notMatchedBorder;
		public boolean highlightSkipped = false;
				
		private class Atts
		{
			public String toolTip;
			public Border border;
			public Color background;
			
			public Atts(String toolTip, Border border, Color background)
			{
				this.toolTip = toolTip;
				this.border = border;
				this.background = background;
			}
		}
		
		
		protected Atts getAtts(int wbCellStatus, String statusText, boolean doIncrementalValidation,
				boolean doIncrementalMatching)
		{
			LineBorder bdr = null;
			Color bg = null;
			if (doIncrementalValidation && (wbCellStatus == WorkbenchDataItem.VAL_ERROR 
					|| wbCellStatus == WorkbenchDataItem.VAL_ERROR_EDIT))
			{
				bdr = new LineBorder(errorBorder);
				bg = errorBackground;
			} else if (doIncrementalMatching && wbCellStatus == WorkbenchDataItem.VAL_NEW_DATA)
			{
				bdr = new LineBorder(newDataBorder);
				bg = newDataBackground;
			} else if (doIncrementalMatching && wbCellStatus == WorkbenchDataItem.VAL_MULTIPLE_MATCH)
			{
				bdr = new LineBorder(multipleMatchBorder);
				bg = multipleMatchBackground;
			} else if (highlightSkipped && doIncrementalMatching && wbCellStatus == WorkbenchDataItem.VAL_NOT_MATCHED)
			{
				bdr = new LineBorder(notMatchedBorder);
				bg = notMatchedBackground;
			}
			return new Atts(statusText, bdr, bg);
		}
		
		/**
		 * @param doIncrementalValidation
		 * @param doIncrementalMatching
		 */
		public CellRenderingAttributes()
		{
		
		}

		/**
		 * @param lbl
		 * @param wbCell
		 */
		public void addAttributes(JLabel lbl, final WorkbenchDataItem wbCell, boolean doIncrementalValidation,
				boolean doIncrementalMatching)
		{
			if (doIncrementalValidation || doIncrementalMatching)
			{
				int cellStatus = WorkbenchDataItem.VAL_OK;
				String cellStatusText = null;
				if (wbCell != null)
				{
					// XXX WorkbenchDataItems can be updated by GridCellEditor
					// or by background validation initiated at load time or
					// after find/replace ops
					// but probably not necessary to synchronize here?
					//synchronized (wbCell)
					//{
						cellStatus = wbCell.getEditorValidationStatus();
						cellStatusText = wbCell.getStatusText();
					//}
				} 			
				//currently nothing extra is done for OK cells
				if (cellStatus != WorkbenchDataItem.VAL_NONE && cellStatus != WorkbenchDataItem.VAL_OK)
				{
					Atts atts = getAtts(cellStatus, cellStatusText, doIncrementalValidation,
							doIncrementalMatching);
					System.out.println("pos " + wbCell.getRowNumber() + ", "+ wbCell.getColumnNumber() + ":" + cellStatusText);
					lbl.setBorder(atts.border);
					//Using ColorHighlighters to set background colors because
					//they do not override the selection color
//					if (atts.background != null)
//					{
//						//lbl.setOpaque(true);
//						lbl.setBackground(atts.background);
//					}
				}
			}
			
		}
	}
