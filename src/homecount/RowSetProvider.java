import javax.sql.*;
import java.util.Observer;

public interface RowSetProvider
{ 
	public RowSet getRowSet();
	public void   refreshRowSet();
	public void addObserver(Observer observer);
	public void setYear(Integer year);
	public void setMonth(Integer month);
	public void setName(String name);
}
