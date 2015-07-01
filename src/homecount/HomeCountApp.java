import javax.swing.JFrame;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.sql.*;
import javax.sql.*;
import javax.sql.rowset.JdbcRowSet  ;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.JTable;
import com.sun.rowset.JdbcRowSetImpl;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ListSelectionModel;

import javax.swing.JFormattedTextField;
import java.text.DateFormat;
import javax.swing.text.DateFormatter;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

import javax.swing.JLabel;
import javax.sql.rowset.spi.SyncProviderException;
import javax.sql.rowset.spi.SyncResolver;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import org.h2.tools.Server;

import java.util.Observer;
import java.util.Observable;

public class HomeCountApp
{
	/**
	 * Main window.
	 */
	JFrame frame = null;
	Connection connection = null;

	TableSelector ts = null;

	//IERecordUI fields
	JTextField          nameTf;
	JFormattedTextField amountFtf, ondateFtf;

	Server dbServer = null;

	public void setFrame(JFrame frame)
	{
		this.frame = frame;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public Connection getConnection()
	{
		return connection;
	}

	public JFrame getFrame()
	{
		return frame;
	}

	void setDBServer(Server dbServer)
	{
		this.dbServer = dbServer;
	}

	Server getDBServer()
	{
		return dbServer;
	}

	public static void main(String[] args)
	{
		System.out.println("It's run!");
		HomeCountApp hca = new HomeCountApp(); 
		hca.startTCPServer();
		hca.connectDB();
		//Create DB with a table if not found
		hca.setupDB(); 
		hca.prepareFrame();
	}

	void setupDB()
	{
		try 
		{
			PreparedStatement create = getConnection().prepareStatement(
					"CREATE TABLE IF NOT EXISTS income_expense ( id     INTEGER       NOT NULL AUTO_INCREMENT PRIMARY KEY, amount DECIMAL(10,2) NOT NULL, name   VARCHAR(255)  NOT NULL, ondate DATE          NOT NULL)");
			create.execute();
		}
		catch (SQLException e)
		{ 
			System.out.println("Error creating table " + e);
		}
	}

	void startTCPServer()
	{
		try
		{
			setDBServer(Server.createTcpServer().start());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	void connectDB()
	{
		try
		{
			Class.forName("org.h2.Driver");
			setConnection(
					DriverManager.getConnection("jdbc:h2:tcp://localhost/~/homecount;AUTO_SERVER=TRUE", "sa", "")); 
		}
		catch (ClassNotFoundException | SQLException e)
		{
			System.out.println("DB connection error occured." + e);
		}
	}

	Statement newStatement()
	{
		Statement stmt = null;
		try
		{
			stmt = getConnection().createStatement(
					ResultSet.TYPE_SCROLL_SENSITIVE, 
					ResultSet.CONCUR_UPDATABLE); 
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return stmt;
	}

	RowSet makeRowSet(Statement stmt, String query)
	{
		RowSet rowSet = null;
		try
		{
			rowSet = new JdbcRowSetImpl(stmt.executeQuery(query));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return rowSet;
	}

	/**
	 * Show frame.
	 */
	public void prepareFrame()
	{ 
		JFrame frame = new JFrame("Main");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(
				new Dimension(600, 800));
		JPanel panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);

		RowSetTableModel tm =
			new RowSetTableModel(
				makeRowSet(
					newStatement(), 
					"SELECT * FROM income_expense"))
			{
				public void updateRow(Object o)
				{ 
					RowSet rs = getRowSet();
					IERecord r = (IERecord) o;
					try
					{
						rs.updateBigDecimal("amount", r.getAmount());
						rs.updateString(    "name"  , r.getName()  );
						rs.updateDate(      "ondate", new java.sql.Date(
									r.getOndate().getTime()));
						rs.updateRow();
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}

				public Class getColumnClass(int column)
				{
					Class<?> cl = String.class;
					switch (column)
					{
						case 0:
							cl = Integer.class;
							break;
						case 1:
							cl = BigDecimal.class;
							break;
						case 2:
							cl = String.class;
							break;
						case 3:
							cl = String.class;
							break;
					}
					return cl;
				}
			};

		JTable tableView = new JTable(tm.getTableModel()); 
		tableView.setDefaultRenderer(
				BigDecimal.class, 
				new DefaultTableCellRenderer()
				{
					public Component getTableCellRendererComponent(
						JTable table, Object value, boolean isSelected, 
						boolean hasFocus, int row, int column)
					{
						DecimalFormat formatter = new DecimalFormat("0.00");
						value = formatter.format((Number) value);

						return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					}
				});
		panel.add(new JScrollPane(tableView), BorderLayout.WEST);

		ts = new TableSelector(tableView, tm)
		{
			public Object readRow(int selected)
			{
				try
				{
					RowSet rs = getRowSetTableModel().getRowSet();
					rs.absolute(selected + 1);
					return new IERecord(
						rs.getBigDecimal("amount" ),
						rs.getString    ("name"   ),
						rs.getDate      ("ondate" ));
				} 
				catch (SQLException e)
				{
					e.printStackTrace();
				}
				return null;
			}
		};
		ts.addObserver(
			new Observer()
			{
				public void update(Observable o, Object arg)
				{
					System.out.println("Observable updated");
					TableSelector ts = (TableSelector) o;
					System.out.println("IERecord: " + (IERecord)ts.getCurrent());
					setTextFields((IERecord) ts.getCurrent());
				}
			});

		JButton button1 = new JButton("Action 1.");
		button1.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
					}
				});
		JButton button2 = new JButton("Action 2");
		button2.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
					}
				});
		Box buttonBox = new Box(BoxLayout.PAGE_AXIS);
		buttonBox.add(button1);
		buttonBox.add(button2);
		panel.add(buttonBox, BorderLayout.EAST);

		//Record pane
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		amountFtf = new JFormattedTextField((DecimalFormat)nf);

		nameTf   = new JTextField();
		//For reference see p. 568 in "Java Swing the definitive guide".
		DateFormat    displayFormat    = new SimpleDateFormat("yyyy-MM-dd");
		DateFormatter displayFormatter = new DateFormatter(displayFormat);
		ondateFtf = new JFormattedTextField(displayFormatter);
		//clear all fields: get them filled with default values.
		clearTextFields();

		JPanel rp = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 0;
		c.gridwidth = 1;
		rp.add(new JLabel("Amount"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 0;
		c.gridwidth = 1;
		rp.add(amountFtf, c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 1;
		c.gridwidth = 1;
		rp.add(new JLabel("Name"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 1;
		c.gridwidth = 1;
		rp.add(nameTf, c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 2;
		c.gridwidth = 1;
		rp.add(new JLabel("On date"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 2;
		c.gridwidth = 1;
		rp.add(ondateFtf, c);

		Box rpBBx = new Box(BoxLayout.X_AXIS);
		JButton insertRow = new JButton("Insert row");
		JButton updateRow = new JButton("Update row");
		JButton deleteRow = new JButton("Delete row");
		rpBBx.add(insertRow);
		rpBBx.add(updateRow);
		rpBBx.add(deleteRow);

		insertRow.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
				    {
						System.out.println("Inserting row");
						IERecord ier = readTextFields();

						/*
						getIETableModel().insertRow(
							ier.getAmount(), 
							ier.getName()  ,
							ier.getOndate());
						*/ 
					}
				});

		updateRow.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						//if has current selected row
						if (null != ts.getCurrent())
						{
							AbstractTableModel atm = (AbstractTableModel) ts.getRowSetTableModel().getTableModel();
							ts.getRowSetTableModel().updateRow(
								readTextFields());
							RowSet rs = ts.getRowSetTableModel().getRowSet();
							try
							{
								ts.fireTableRowsUpdated(rs.getRow()-1, rs.getRow()-1);
							}
							catch (SQLException e)
							{
								e.printStackTrace();
							}
						}
					}
				});

		deleteRow.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						System.out.println("Deleting row");
						if (-1 != -1)
						{
						}
					}
				});

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridy     = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;
		rp.add(rpBBx, c);

		panel.add(rp, BorderLayout.SOUTH); 

		frame.setVisible(true);
		frame.pack();
		setFrame(frame);
	}

	public static class IERecord
	{
		Integer        id;
		BigDecimal     amount = BigDecimal.ZERO;
		String         name   = "";
		java.util.Date ondate = new java.util.Date();

		public IERecord()
		{
		}

		public IERecord(Integer id, BigDecimal amount, String name, java.util.Date ondate)
		{
			this.id = id;
			this.amount = amount;
			this.name   = name  ;
			this.ondate = ondate;
		}

		public IERecord(BigDecimal amount, String name, java.util.Date ondate)
		{
			this.amount = amount;
			this.name   = name  ;
			this.ondate = ondate;
		}

		public BigDecimal getAmount()
		{
			return amount;
		}

		public String getName()
		{
			return name;
		}

		public java.util.Date getOndate()
		{
			return ondate;
		}
		
		public String toString()
		{
			DecimalFormat formatter = new DecimalFormat("0.00");
			return String.format(
					"%tF;%s;%s", 
					getOndate(),
					formatter.format((Number) getAmount()), 
					getName()
					);
		}
	}

	//Fills text fields with default data
	public void clearTextFields()
	{
		setTextFields(new IERecord());
	}

	public IERecord readTextFields()
	{
		return new IERecord(
				new BigDecimal(((Number)amountFtf.getValue()).doubleValue()), 
				nameTf.getText(),
				(java.util.Date) ondateFtf.getValue());
	}

	public void setTextFields(IERecord ieRecord)
	{
		amountFtf.setValue(ieRecord.getAmount());
		nameTf.setText(ieRecord.getName());
		ondateFtf.setValue(ieRecord.getOndate());
	}
}
