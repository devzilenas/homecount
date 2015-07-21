import javax.swing.table.*;
import java.sql.*;
import javax.sql.*;
import javax.sql.rowset.JdbcRowSet  ;
import com.sun.rowset.JdbcRowSetImpl;
import javax.sql.rowset.spi.SyncProviderException;
import javax.sql.rowset.spi.SyncResolver;

public class CategoryTableModel
	extends AbstractTableModel
{
	String query = "SELECT c.*, cp.catname as \"parname\", cp.id as \"parcatid\" FROM category c INNER JOIN category cp ON c.parcat = cp.id ORDER BY catname DESC";

	RowSet             rs ;
	ConnectionProvider conp;

	public CategoryTableModel(ConnectionProvider conp)
	{
		this.conp = conp;
		this.rs   = makeRowSet();
	}

	/**
	 * For @overrid
	 */
	public Statement getStatement()
	{
		return null;
	}

	public String getQuery()
	{
		return query;
	} 

	public RowSet makeRowSet()
	{
		RowSet rowSet = null;
		try
		{
			rowSet = new JdbcRowSetImpl(
				getStatement().executeQuery(getQuery()));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return rowSet;
	}

	public CategoryTableModel(RowSet rs)
	{
		this.rs = rs;
	}

	public ConnectionProvider getConnectionProvider()
	{
		return conp;
	}

	public Connection getConnection()
	{
		return getConnectionProvider().getConnection();
	}

	public RowSet getRowSet()
	{
		return rs;
	}

	public Class<?> getColumnClass(int columnIndex)
	{
		return String.class;
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
			name = getRowSet().getMetaData().getColumnLabel(col);
		}
		catch (SQLException e)
		{
			name = super.getColumnName(column);
			e.printStackTrace();
		}
		return name;
	}
}
