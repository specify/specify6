package edu.ku.brc.specify.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;
import java.util.Vector;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;

public class CsvTableModel implements TableModel
{
	protected Vector<TableModelListener> listeners;
	
	protected File csvFile;
	
	protected Vector<String[]> rowData;
	protected String[] header;
	
	protected Random rand;
	protected int randStart;
	
	public CsvTableModel( File csvFile ) throws Exception
	{
		listeners = new Vector<TableModelListener>();
		rand = new Random();
		randStart = rand.nextInt(2756);
		
		rowData = new Vector<String[]>();
		
		this.csvFile = csvFile;
		FileReader fr = new FileReader(csvFile);
		BufferedReader br = new BufferedReader(fr);

		// first line is header
		String line = br.readLine();
		header = StringUtils.split(line);
		line = br.readLine();
		while( line != null )
		{
			String[] row = StringUtils.split(line);
			if( row.length > 0 )
			{
				rowData.add(row);
			}
			line = br.readLine();
		}
	}

	public int getColumnCount()
	{
		return 8;
	}

	public String getColumnName(int column)
	{
		return header[column];
	}

	public int getRowCount()
	{
		return rowData.size();
	}

	public Object getValueAt(int row, int column)
	{
		if( column == 0 )
		{
			return "ACB-2006-" + row + randStart;
		}
		else if( column == 3 )
		{
			int i = rand.nextInt(10);
			if( i == 0 )
			{
				return "dynamite";
			}
			if( i == 1 )
			{
				return "boat electro-shocker";
			}
			return "seine";
		}
		else if( column == 1 || column == 2 )
		{
			String[] r = rowData.get(row);
			return r[column-1];
		}
		else
		{
			String[] r = rowData.get(row);
			return r[column-2];
		}
	}

	public boolean isCellEditable(int row, int column)
	{
		return false;
	}

	public Class<?> getColumnClass(int columnIndex)
	{
		return String.class;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		return;
	}

	public void addTableModelListener(TableModelListener l)
	{
		listeners.add(l);
	}

	public void removeTableModelListener(TableModelListener l)
	{
		listeners.remove(l);
	}
}
