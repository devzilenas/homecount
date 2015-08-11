import java.util.*;
import java.sql.*;
import javax.sql.*;
import com.sun.rowset.JdbcRowSetImpl;

public class CategoryDAO
{
	StatementProvider sp;

	public CategoryDAO(StatementProvider sp)
	{
		this.sp = sp;
	}

	StatementProvider getStatementProvider()
	{
		return sp;
	}

	Statement newStatement()
	{
		return getStatementProvider().getStatement();
	}

	public List<Category> getCategories()
	{
		String q = "SELECT id, catname, parcatid FROM category";

		List<Category> data = new LinkedList<>();
		Map<Integer, Category> categories = new HashMap<>();

		try
		{
			RowSet rs = new JdbcRowSetImpl(newStatement().executeQuery(q));
			
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

		return new LinkedList<>(categories.values());
	}
}
