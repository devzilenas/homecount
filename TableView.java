import javax.swing.table.AbstractTableModel;
import java.sql.SQLException;

/**
 * A class for table view.
 */
public class TableView
{
	String     tableName ;//table name
	RowSet     rowSet    ;

    //data for the table
	TableModel tableModel = new AbstractTableModel()
	{
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
				return lastRow();
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
	}

	public TableView(RowSet rowSet)
	{
		this.rowSet = rowSet;
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public void setRowSet(RowSet rowSet)
	{
		this.rowSet = rowSet;
	}

	public RowSet getRowSet()
	{
		return rowSet;
	}

	public TableModel getTableModel()
	{
		return tableModel;
	}

	public void setTableModel(TableModel tableModel)
	{
		this.tableModel = tableModel;
	}

}
