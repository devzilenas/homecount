import java.util.*;
import java.sql.*;
import javax.sql.*;
import com.sun.rowset.JdbcRowSetImpl;

public class CategoryDAOImpl
	implements CategoryDAO
{
	Connection c;

	public CategoryDAOImpl(Connection c)
	{
		this.c = c;
	}

	Connection getConnection()
	{
		return c;
	}

	/**
	 * Modifies category.
	 */
	public void save(Category cat)
	{
		String q = "INSERT INTO category (catname, parcatid) VALUES (?, ?)";
		try
		{
			PreparedStatement s = getConnection().prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
			s.setString(1, cat.getName());
			s.setObject(2, cat.getParId());
			if (0 != s.executeUpdate())
			{
				System.out.println("Category created!");
				ResultSet genKeys = s.getGeneratedKeys();
				if (genKeys.next())
				{
					cat.setId(genKeys.getInt(1));
				}
				else
				{
					System.out.println(" Failed to get category id key!");
				}
			}
			else
			{
				System.out.println("Category create failed!");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public Category getById(Integer id)
	{
		String q = "SELECT id, catname, parcatid FROM category WHERE id = ? ORDER BY id DESC";
		Category category = new Category(id);
		try 
		{ 
			PreparedStatement s = getConnection().prepareStatement(q);
			s.setInt(1,id);
			ResultSet rs = s.executeQuery();
			rs.next();
			category.setName(rs.getString(2)); 
			category.setParid(rs.getInt(3)); 
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return category;
	}

	public void update(Category cat) 
	{
		String q = "UPDATE category SET catname = ? , parcatid = ? WHERE id = ?";
		try
		{
			PreparedStatement s = getConnection().prepareStatement(q);
			s.setString(1, cat.getName());
			s.setInt(2, cat.getParId());
			s.setInt(3, cat.getId());
			System.out.format("Category#id:%d ", cat.getId());
			if (0 != s.executeUpdate())
			{
				System.out.println("updated ");
			}
			else
			{
				System.out.println("not updated ");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void deleteById(Integer id)
	{
		String q = "DELETE FROM category WHERE id = ? LIMIT 1";
		try
		{
			PreparedStatement s = getConnection().prepareStatement(q);
			s.setInt(1, id);
			if (0 != s.executeUpdate())
			{
				System.out.println(" Category deleted!"); 
			}
			else
			{
				System.out.println("Category deletion not succeeded!"); 
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public List<Category> getAll()
	{
		return getCategories();
	}

	/**
	 * Gets a list of categories. Each category has a pointer to it's parent category.
	 */
	public List<Category> getCategories()
	{
		String q = "SELECT id, catname, parcatid FROM category";

		List<Category> data = new LinkedList<>();
		Map<Integer, Category> categories = new HashMap<>();

		try
		{
			RowSet rs = new JdbcRowSetImpl(getConnection().createStatement().executeQuery(q));
			
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
