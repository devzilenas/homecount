import javax.sql.*;

public interface RowSetProvider
{ 
	public RowSet getRowSet();
	public void   refreshRowSet();
}
