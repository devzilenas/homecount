import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.sql.*;
import javax.sql.*;
import javax.sql.rowset.JdbcRowSet  ;
import com.sun.rowset.JdbcRowSetImpl;
import javax.sql.rowset.spi.SyncProviderException;
import javax.sql.rowset.spi.SyncResolver;
import java.math.BigDecimal;
import org.h2.tools.Server;

public class HomeCountApp
{
	/**
	 * Main window.
	 */
	JFrame     frame = null;
	Connection connection = null;
	StatementProvider sp = null;

	TableSelector ts = null;
	CategoryTableSelector cts = null;

	JTabbedPane tpane = new JTabbedPane(JTabbedPane.TOP);

	//IERecordUI fields
	JTextField          nameTf, categoryTf;
	JFormattedTextField amountFtf, ondateFtf;

	//Filter fields
	JComboBox<String> nameCb = new JComboBox<>(new String[]{""});
	JSpinner   yearSp = new JSpinner(
			new SpinnerNumberModel(
				Calendar.getInstance().get(Calendar.YEAR),
				Calendar.getInstance().get(Calendar.YEAR) - 100,
				Calendar.getInstance().get(Calendar.YEAR) + 100,
				1));
	JSpinner monthSp = new JSpinner(new SpinnerNumberModel(1,1,12,1));
	KeyStroke ksF11 = KeyStroke.getKeyStroke(KeyEvent.VK_F11,0);
	KeyStroke ksF12 = KeyStroke.getKeyStroke(KeyEvent.VK_F12,0);

	/**
	 * Category fields
	 */
	JTextField catnameTf;
	JComboBox catParentCb;

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

	public void setStatementProvider(StatementProvider sp)
	{
		this.sp = sp;
	}

	public StatementProvider getStatementProvider()
	{
		return sp;
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
		/**
		 * Makes thread-safe programm.
		 */
		Runnable runner = 
			new Runnable()
			{ 
				public void run()
				{ 
					HomeCountApp hca = new HomeCountApp(); 
					hca.startTCPServer();
					hca.connectDB();
					//Create DB with a table if not found
					hca.setupDB(); 
					hca.prepareFrame();
				}
			};
		EventQueue.invokeLater(runner);
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
			setDBServer(Server.createTcpServer("-tcpAllowOthers").start());
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
					DriverManager.getConnection("jdbc:h2:tcp://localhost/~/homecount;DB_CLOSE_ON_EXIT=FALSE", "sa", "")); 
			setStatementProvider(
					new StatementProvider()
					{
						public Statement getStatement()
						{
							return newStatement();
						}
					});
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

	/**
	 * Show frame.
	 */
	public void prepareFrame()
	{
		JFrame frame = new JFrame("Main");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(
				new Dimension(600, 800));
		frame.setContentPane(tpane);

		JPanel panel = new JPanel(new BorderLayout());

		final RowSetTableModel tm =
			new RowSetTableModel(
				new RowSetProvider()
				{
					Statement stmt  = newStatement();
					String    query = "SELECT * FROM income_expense WHERE YEAR(CONVERT(ondate, TIMESTAMP))=%d AND MONTH(CONVERT(ondate, TIMESTAMP))=%d AND name LIKE '%%%s%%' ORDER BY ondate ASC";
					Integer   year  = Calendar.getInstance().get(Calendar.YEAR);
					Integer   month = Calendar.getInstance().get(Calendar.MONTH) + 1; 
					String    name  = "";
					RowSet    rs    ;

					public Set<Observer> observers = new HashSet<>();

					{
						setRowSet(makeRowSet());
					}

					/**
					 * It notifies also on addition because of the initialization of ComboBoxModel with entries from rowset.
					 */
					public void addObserver(Observer obs)
					{
						this.observers.add(obs);
						notifyObservers();
					}
			        
			        public Set<Observer> getObservers()
					{
						return observers;
					} 

					public Integer getYear()
					{
						return year;
					}

					public void setYear(Integer year)
					{
						this.year = year;
					}

					public void setName(String name)
					{
						this.name = name;
					}

					public String getName()
					{
						return name;
					}

					public void setMonth(Integer month)
					{
						this.month = month;
					}

					public Integer getMonth()
					{
						return month;
					}

			        public void notifyObservers()
					{
						for (Observer observer : getObservers())
						{
							observer.update(new Observable(){},null);
						}
					}

			        private String getQuery()
					{
						return String.format(query, getYear(), getMonth(), getName());
					}

					private RowSet makeRowSet()
					{
						RowSet rowSet = null;
						try
						{
							rowSet = new JdbcRowSetImpl(
								stmt.executeQuery(getQuery()));
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
						return rowSet;
					}

					public void refreshRowSet()
					{
						setRowSet(makeRowSet());
					}

					private void setRowSet(RowSet rs)
					{
						this.rs = rs;
						notifyObservers();
					}

					public RowSet getRowSet()
					{
						return rs;
					}
				})
				{
					public void deleteRow()
					{
						try
						{
							getRowSet().deleteRow();
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
					}

					public void insertRow(Object o)
					{
						IERecord r = (IERecord) o;
						RowSet rs = getRowSet();
						try
						{
							rs.last();
							rs.moveToInsertRow();
							rs.updateBigDecimal("amount", r.getAmount());
							rs.updateString    ("name"  , r.getName()  );
							rs.updateDate      ("ondate", new java.sql.Date(
										r.getOndate().getTime()));
							rs.updateString("category", r.getCategory());
							rs.insertRow();
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
					}

					public void updateRow(Object o)
					{ 
						RowSet rs = getRowSet();
						IERecord r = (IERecord) o;
						try
						{
							rs.updateBigDecimal("amount"   , r.getAmount());
							rs.updateString(    "name"     , r.getName()  );
							rs.updateDate  (    "ondate"   , new java.sql.Date(r.getOndate().getTime()));
							rs.updateString(    "category", r.getCategory());
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

		monthSp.setValue(Calendar.getInstance().get(Calendar.MONTH)+1);

		nameCb.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					JComboBox cb = (JComboBox) e.getSource();
					String value = (String)   cb.getSelectedItem();
					RowSetTableModel tm  = ts.getRowSetTableModel();
					RowSetProvider   rsp = tm.getRowSetProvider();
					rsp.setName(value);
					tm.refreshRowSet();
					ts.fireTableDataChanged();
				}
			});

		Box tl  = new Box(BoxLayout.Y_AXIS);
		Box tlf = new Box(BoxLayout.X_AXIS);

		yearSp.addChangeListener(
				new ChangeListener()
				{
					public void stateChanged(ChangeEvent e)
					{
						JSpinner sp = (JSpinner) e.getSource();
						SpinnerModel model = sp.getModel();
						Integer year = Calendar.getInstance().get(Calendar.YEAR);
						if (model instanceof SpinnerNumberModel)
						{ 
							year = ((SpinnerNumberModel) model).getNumber().intValue();
						}
						RowSetTableModel tm = ts.getRowSetTableModel();
						RowSetProvider rsp  = tm.getRowSetProvider();
						rsp.setYear(year);
						tm.refreshRowSet();  
						ts.fireTableDataChanged();
					}
				});

		monthSp.addChangeListener(
			new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					JSpinner sp = (JSpinner) e.getSource();
					SpinnerModel model = sp.getModel();
					Integer month = Calendar.getInstance().get(Calendar.MONTH);
					if (model instanceof SpinnerNumberModel)
					{
						month = ((SpinnerNumberModel) model).getNumber().intValue();
					}
					RowSetTableModel tm = ts.getRowSetTableModel();
					RowSetProvider rsp  = tm.getRowSetProvider();
					rsp.setMonth(month);
					tm.refreshRowSet();  
					ts.fireTableDataChanged();

				}
			});

		JPanel fp = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 0;
		c.gridwidth = 1;
		fp.add(new JLabel("Year"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 0;
		c.gridwidth = 1;
		fp.add(yearSp, c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 1;
		c.gridwidth = 1;
		fp.add(new JLabel("Month"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 1;
		c.gridwidth = 1;
		fp.add(monthSp, c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 2;
		c.gridwidth = 1;
		fp.add(new JLabel("Name"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 2;
		c.gridwidth = 1;
		fp.add(nameCb, c);

		tm.getRowSetProvider().addObserver(
			new Observer()
			{
				public void update(Observable o, Object arg)
				{
					nameCb.setModel( 
						new DefaultComboBoxModel<String>()
						{
							Set<String> dataSet = new TreeSet<String>();

							{
								int column = tm.getColumnIndex("name");
								for (int row = 0; row < tm.getTableModel().getRowCount(); row++)
								{
									getDataSet().add( 
										(String)tm.getTableModel().getValueAt(row, column));
								}
								/**
								 * Must always contain "" so that filtering without name would work
								 */
								if (!getDataSet().contains(""))
								{
									getDataSet().add("");
								}

								fireContentsChanged(this, 0, getDataSet().size());
							}

							public Set<String> getDataSet()
							{
								return dataSet;
							}

							public String getElementAt(int index)
							{
								return (String) getDataSet().toArray()[index];
							}
				
							public int getSize()
							{
								return getDataSet().size();
							}

						}
					);
				}
			});

		tlf.add(fp);
		tl.add(tlf);
		tl.add(new JScrollPane(tableView));
		panel.add(tl, BorderLayout.WEST);

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
						rs.getDate      ("ondate" ),
						rs.getString    ("category"));
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

		JButton button3 = new JButton("Reset");
		tlf.add(button3);
		button3.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{ 
					Calendar cal = Calendar.getInstance();
					nameCb.setSelectedItem("");
					yearSp.setValue(cal.get(Calendar.YEAR));
					monthSp.setValue(cal.get(Calendar.MONTH)+1);

					RowSetTableModel tm = ts.getRowSetTableModel(); 
					RowSetProvider rsp  = tm.getRowSetProvider();
					rsp.setName("");
					rsp.setYear(((SpinnerNumberModel) yearSp.getModel()).getNumber().intValue());
					rsp.setMonth(((SpinnerNumberModel) monthSp.getModel()).getNumber().intValue());
					tm.refreshRowSet();  
					ts.fireTableDataChanged();
				}
			});

		//Record pane
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		amountFtf = new JFormattedTextField((DecimalFormat)nf);

		nameTf   = new JTextField();
		//For reference see p. 568 in "Java Swing the definitive guide".
		DateFormat    displayFormat    = new SimpleDateFormat("yyyy-MM-dd");
		DateFormatter displayFormatter = new DateFormatter(displayFormat);
		ondateFtf  = new JFormattedTextField(displayFormatter);
		categoryTf = new JTextField();
		//clear all fields: get them filled with default values.
		clearTextFields();

		JPanel rp = new JPanel(new GridBagLayout());

		c = new GridBagConstraints();
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

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 3;
		c.gridwidth = 1;
		rp.add(new JLabel("Category"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 3;
		c.gridwidth = 1;
		rp.add(categoryTf, c);

		Box rpBBx = new Box(BoxLayout.X_AXIS);
		JButton newRow    = new JButton("New row");
		JButton insertRow = new JButton("Insert row");
		JButton updateRow = new JButton("Update row");
		JButton deleteRow = new JButton("Delete row");
		rpBBx.add(newRow);
		rpBBx.add(insertRow);
		rpBBx.add(updateRow);
		rpBBx.add(deleteRow);

		newRow.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					Calendar cal = Calendar.getInstance();
					IERecord r = new IERecord(
						BigDecimal.ZERO, 
						""             ,
						new java.sql.Date(System.currentTimeMillis()),
						"");
					setTextFields(r);
				}
			}
		);
		insertRow.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
				    {
						System.out.println("Inserting row");
						IERecord ier = readTextFields(); 
						RowSetTableModel tm = ts.getRowSetTableModel();
						tm.insertRow(
							readTextFields());
						tm.refreshRowSet();  
						ts.fireTableRowsInserted(
							tm.getTableModel().getRowCount()+1, 
							tm.getTableModel().getRowCount()+1);
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
						RowSetTableModel tm = ts.getRowSetTableModel();
						RowSet rs = ts.getRowSetTableModel().getRowSet();
						int row = 0;
						try
						{
							row = rs.getRow() - 1;
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
						tm.deleteRow();
						tm.refreshRowSet();  
						ts.fireTableRowsDeleted(row, row);
					}
				});

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.gridy     = 4;
		c.gridwidth = GridBagConstraints.REMAINDER;
		rp.add(rpBBx, c);

		panel.add(rp, BorderLayout.SOUTH); 

		tpane.addTab("Expenses", panel);

		/**
		 * Setup access keys.
		 */
		InputMap tpinpmap = tpane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		tpinpmap.put(ksF11, "navigatePrevious");
		tpinpmap.put(ksF12, "navigateNext");

		/**
		 * Setup categories panel.
		 */
		JPanel catPanel = new JPanel();
		catPanel.setLayout(new BoxLayout(catPanel, BoxLayout.Y_AXIS));

		JTable catTable = new JTable(
			new CategoryTableModel(
				new ConnectionProvider()
				{
					public Connection getConnection() 
					{
						return getConnection();
					}
				})
			{
				public Object readRow(int selected)
				{
					return getData().get(selected);
				}
				public Statement getStatement()
				{
					return newStatement();
				}
			})
			{
				@Override
				public TableCellEditor getCellEditor(int row, int column)
				{
				   Object value = super.getValueAt(row, column);
				   if (null != value)
				   {
					  if (value instanceof JComboBox)
					  {
						   return new DefaultCellEditor((JComboBox)value);
					  }
					  return getDefaultEditor(value.getClass());
				   }
				   return super.getCellEditor(row, column);
				}
			};

		cts = new CategoryTableSelector(catTable, (CategoryTableModel) catTable.getModel())
		{ 
			@Override
			public Object readRow(int selected)
			{
				try
				{
					RowSet rs = getTableModel().getRowSet();
					rs.absolute(selected + 1);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}

				return getTableModel().getData().get(selected);
			}
		};

		cts.addObserver(
			new Observer()
			{
				public void update(Observable o, Object arg)
				{
					CategoryTableSelector cts = (CategoryTableSelector) o;
					Category c = (Category) cts.getCurrent();
					System.out.format("Current updated %s %n", c);
					setCategoryTextFields(c);
				}
			});

		catPanel.add(new JScrollPane(catTable));

		catnameTf   = new JTextField();
		catParentCb = new JComboBox<Category>(
			new CategoryComboBoxModel<Category>()
			{
				@Override
				public void refreshData()
				{	
					categories = new CategoryDAO(getStatementProvider()).getCategories();
					Set<Category> uniqc = new TreeSet<Category>(
						new Comparator<Category>()
						{
							public int compare(Category c1, Category c2)
							{
								return new String(c1.getName()).compareTo(new String(c2.getName()));
							}

							public boolean equals(Object obj) 
							{
								return false;
							}
						});
					uniqc.addAll(categories);
					categories = new LinkedList<Category>(uniqc);
					
					fireContentsChanged(this, 0, getSize());
				}
			});

		JPanel f2p = new JPanel(new GridBagLayout());

		c = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 0;
		c.gridwidth = 1;
		f2p.add(new JLabel("Name"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 0;
		c.gridwidth = 1;
		f2p.add(catnameTf, c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_START;
		c.weightx   = 0.25;
		c.weighty   = 0;
		c.gridx     = 0;
		c.gridy     = 1;
		c.gridwidth = 1;
		f2p.add(new JLabel("Parent category"), c);

		c           = new GridBagConstraints();
		c.fill      = GridBagConstraints.HORIZONTAL;
		c.anchor    = GridBagConstraints.LINE_END;
		c.weightx   = 0.75;
		c.weighty   = 0;
		c.gridx     = 1;
		c.gridy     = 1;
		c.gridwidth = 1;
		f2p.add(catParentCb, c);

		catPanel.add(f2p);

		Box b2L = new Box(BoxLayout.X_AXIS);
		JButton cirB = new JButton("Insert row");
		JButton cuB = new JButton("Update row");

		b2L.add(cirB);
		b2L.add(new JButton("Delete row"));
		cuB.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					Category cat = null;
					if (null != (cat = (Category) cts.getCurrent()))
					{ 
						RowSet rs = cts.getTableModel().getRowSet(); 
						Category cu = readCategory();
						try
						{
							int currentPosition = rs.getRow();
							rs.updateString(2, cu.getName());
							rs.updateInt(   3, cu.getParId()); 
							rs.updateRow();
							cts.getTableModel().refreshRowSet();
							//Go to the same row that was before.
							rs.absolute(currentPosition);
							cts.fireTableRowsUpdated(rs.getRow()-1, rs.getRow()-1);
							((CategoryComboBoxModel) catParentCb.getModel()).refreshData();
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
					}
				}
			});
		cirB.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{ 
					cts.getTableModel().insertRow(readCategory());
					cts.getTableModel().refreshRowSet();
					cts.fireTableRowsInserted(
						cts.getTableModel().getRowCount()+1, 
						cts.getTableModel().getRowCount()+1);
				}
			});

		b2L.add(cuB);
		b2L.add(new JButton("Close"));

		catPanel.add(b2L);

		tpane.addTab("Categories", catPanel);

		tpane.addChangeListener(
			new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{ 
					JTabbedPane tp = (JTabbedPane) e.getSource();
					System.out.format("Selected pane %d %n", tp.getSelectedIndex());
				}
			});

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
		String         category = "";

		public IERecord()
		{
		}

		public IERecord(Integer id, BigDecimal amount, String name, java.util.Date ondate, String category)
		{
			this.id = id;
			this.amount = amount;
			this.name   = name  ;
			this.ondate = ondate;
			this.category = category;
		}

		public IERecord(BigDecimal amount, String name, java.util.Date ondate, String category)
		{
			this.amount = amount;
			this.name   = name  ;
			this.ondate = ondate;
			this.category = category;
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
		
		public String getCategory()
		{
			return category;
		}

		public String toString()
		{
			DecimalFormat formatter = new DecimalFormat("0.00");
			return String.format(
					"%tF;%s;%s", 
					getOndate(),
					formatter.format((Number) getAmount()), 
					getName(),
					getCategory());
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
				(java.util.Date) ondateFtf.getValue(),
				categoryTf.getText());
	}

	public Category readCategory()
	{
		return new Category( 
				catnameTf.getText(), 
				(Category) catParentCb.getSelectedItem());
	}

	public void setTextFields(IERecord ieRecord)
	{
		amountFtf.setValue( ieRecord.getAmount());
		nameTf.setText    ( ieRecord.getName());
		ondateFtf.setValue( ieRecord.getOndate());
		categoryTf.setText(ieRecord.getCategory());
	}

	public void setCategoryTextFields(Category cat)
	{
		catnameTf.setText(cat.getName());
		catParentCb.setSelectedItem(cat.getParent());
	}
	 
	public Integer parseInt(String value, Integer defVal)
	{
		int retVal;
		try
		{
			retVal = Integer.valueOf(value);
		}
		catch (NumberFormatException e)
		{
			retVal = defVal;
		} 
		return retVal;
	}
}
