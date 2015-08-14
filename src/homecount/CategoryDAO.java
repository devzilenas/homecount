import java.util.List;
public interface CategoryDAO
{
	public void save(Category cat);
	public Category getById(Integer id);
	public void update(Category cat);
	public void deleteById(Integer id);
	public List<Category> getAll();
}
