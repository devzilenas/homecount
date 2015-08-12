import java.util.*;
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
	String query = "SELECT c.id, c.catname, c.parcatid FROM category c";

	RowSet             rs ;
	ConnectionProvider conp;
	List<Category> data = new LinkedList<>();
	Map<Integer, Category> categories = new HashMap<>();

	public CategoryTableModel(ConnectionProvider conp)
	{
		this.conp = conp;
		this.rs   = makeRowSet();
		makeData();
	}

	private Map<Integer, Category> getCategories()
	{
		return categories;
	}

	public List<Category> getData()
	{
		return data;
	}

	/**
	 * For @override
	 */
	public Statement getStatement()
	{
		return null;
	}

	public String getQuery() 
	{
		return query;
	}

	private void makeData()
	{
		RowSet rs = getRowSet();

		try
		{
			while (rs.next())
			{
				Category cat = new Category(
						rs.getInt(1), rs.getString(2), rs.getInt(3));
				categories.put(cat.getId(), cat);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		for (Category cat : categories.values())
		{
			cat.setParent(
					categories.get(
						cat.getParId()));
		}
		data = new LinkedList<>(categories.values());
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
		return 3;
	}

	public int getRowCount()
	{
		return data.size();
	}

	/**
	 * as arguments expects 0-based indexes
	 */
	public Object getValueAt(int row, int column)
	{
		Object value = null;
		Category cat = data.get(row);
		switch (column)
		{
			case 0:
				value = cat.getId();
				break;
			case 1:
				value = cat.getName();
				break;
			case 2:
				value = cat.getParent();
				break;
		}

		return value;
	}

	public String getColumnName(int column)
	{
		String name = "";
		switch (column)
		{
			case 0:
				name = "Category ID";
				break;
			case 1:
				name = "Category name";
				break;
			case 2:
				name = "Parent category";
				break;
		}
		return name;
	}

	public void refreshRowSet()
	{
		this.rs   = makeRowSet();
		makeData();
	}

	public void insertRow(Category cat)
	{ 
		RowSet rs = getRowSet();

		try
		{
			rs.last();
			rs.moveToInsertRow();
			rs.updateString(2,cat.getName());
			rs.insertRow();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
