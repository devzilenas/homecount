import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import javax.sql.RowSet;
import java.sql.SQLException;

/**
 * RowSetTableModel provides an TableModel from RowSet.
 *
 * @author Marius Žilėnas
 * @version 1.0
 * @since 2015-06-29
 */
public class RowSetTableModel
{ 
	RowSetProvider rsp;

	public RowSetTableModel(RowSetProvider rsp)
	{
		this.rsp = rsp;
	}

	public RowSet getRowSet()
	{
		return getRowSetProvider().getRowSet();
	}

	public RowSetProvider getRowSetProvider()
	{ 
		return rsp;
	}

	public void refreshRowSet()
	{ 
		getRowSetProvider().refreshRowSet();
	}

	public TableModel getTableModel()
	{
		return tableModel;
	}

	/**
	 * @override
	 */
	public Class getColumnClass(int column)
	{
		return String.class;
	}

	/**
	 * @override
	 */
	public void updateRow(Object o)
	{
	}

	/**
	 * @override
	 */
	public void insertRow(Object o)
	{
	}

	/**
	 * @override
	 */
	public void deleteRow()
	{
	}

	/**
	 * 0-based indexing
	 * @returns 0-based index
	 */
	public int getColumnIndex(String name)
	{
		int index = 0;

		for (int i = getTableModel().getColumnCount() - 1; 0 < i; i--)
		{
			if (name.toUpperCase().equals(getTableModel().getColumnName(i).toUpperCase()))
			{
				index = i;
				break;
			}
		}

		return index;
	}

	TableModel tableModel = new AbstractTableModel()
	{
		public Class getColumnClass(int column)
		{
			return RowSetTableModel.this.getColumnClass(column);
		}

		public int getRowCount()
		{
			RowSet rowSet = getRowSet();
			try
			{
				int currentRow = rowSet.getRow();
				rowSet.last();
				int lastRow    = rowSet.getRow();

				if (0 < currentRow)
				{
					rowSet.absolute(currentRow);
				}
				return lastRow;
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				return 0;
			}
		}

		public int getColumnCount()
		{
			int count = 0;
			try 
			{
				count = getRowSet().getMetaData().getColumnCount();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			return count;
		}

		/**
		 * argument is 0-based index
		 * makes temporary 1-based index
		 */
		public String getColumnName(int column)
		{
			int col = column + 1; //in JDBC columns start with 1
			String name = "";
			try 
			{
				name = getRowSet().getMetaData().getColumnName(col);
			}
			catch (SQLException e)
			{
				name = super.getColumnName(column);
				e.printStackTrace();
			}
			return name;
		}

		/**
		 * as arguments expects 0-based indexes
		 */
		public Object getValueAt(int row, int column)
		{
			RowSet rowSet = getRowSet();
			Object value  = null;
			try 
			{
				rowSet.absolute(row + 1);
				value = rowSet.getObject(column + 1);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			return value;
		}
	};
}
