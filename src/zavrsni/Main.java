package zavrsni;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;



import postgres.database.tools.DatabaseConnection;

public class Main extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static JTextArea search;
	private static JComboBox<String> comboBox;
	private static List<String> userData = null;
	private static String[] info;


	static class DbRunnable implements Runnable{
		
		String element;
		
		DbRunnable(String element) {
			this.element = element;
		}
		
		DbRunnable() {
			this(null);
		}
		
		@Override
		public void run() {
			if (element == null)
				dbSearch();
			else
				dbGet();
		}
		
		private void dbGet() {
			if (userData == null)
				return;
			try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + userData.get(0), userData.get(1), userData.get(2))){

				try {
					PreparedStatement pstmt = connection.prepareStatement("select * from names, nodes, gencode, division where nodes.tax_id = names.tax_id "
							+ "and nodes.genetic_code_id = gencode.genetic_code_id and nodes.division_id = division.division_id and name_txt = ?;");
					pstmt.setString(1, element);
					ResultSet rs = pstmt.executeQuery();
					Map<String, String> data = new HashMap<>();
					if (rs.next()) {
						for (int i = 0; i < info.length; i++) {
							data.put(info[i], rs.getString(info[i]));
						}
					}
			        for (Map.Entry<String,String> entry : data.entrySet()) {
			        	System.out.println(entry.getKey() + ": " + entry.getValue());
			        }
				} catch (SQLException ex) {
					ex.printStackTrace();
				}


			} catch (SQLException e) {
				System.out.println("Connection failure.");
				e.printStackTrace();

				try {
					DatabaseConnection.WrongData();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
		}



		private void dbSearch() {
			if (userData == null)
				return;
			try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + userData.get(0), userData.get(1), userData.get(2))){

				try {
					PreparedStatement pstmt = connection.prepareStatement("select * from names where lower(name_txt) like lower(?);");
					pstmt.setString(1, search.getText() + "%");
					ResultSet rs = pstmt.executeQuery();
					List<String> names = new ArrayList<>();
					int counter = 0;
					while (rs.next()) {
						if (counter++ > 100)
							break;
						names.add(rs.getString("name_txt"));
					}
					if (comboBox.getItemCount() != 0)
						comboBox.removeAllItems();
					for (String s : names) 
						comboBox.addItem(s);	
				} catch (SQLException ex) {
					ex.printStackTrace();
				}


			} catch (SQLException e) {
				System.out.println("Connection failure.");
				e.printStackTrace();

				try {
					DatabaseConnection.WrongData();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	
	

	public Main() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		initGUI();
		//pack();
		setSize(800, 200);
		setLocationRelativeTo(null);
		setTitle("Aplikacija za pohranu bioinformatičkih podataka u sustavu PostgreSQL");
		
		try {
			userData = DatabaseConnection.Connect();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		info = new String[] {"tax_id", "name_txt", "unique_name", "name_class", "parent_tax_id", "rank", "embl_code",
				"division_id", "inherited_div_flag", "genetic_code_id", "inherited_GC_flag", "mitochondrial_genetic_code_id",
				"inheritedMGC_flag", "genbank_hidden_flag", "hidden_subtree_root_flag", "division_cde", "division_name", 
				"abbrevation", "name", "cde", "starts"};
				
	}


	private void initGUI() {
		Container cp = this.getContentPane();
		
		cp.setLayout(new GridLayout(4,0));

		JTextField text = new JTextField("Tražilica:");
		text.setEditable(false);
		cp.add(text);

		search = new JTextArea();

		cp.add(search);


		search.addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e) {
				Thread t = new Thread(new DbRunnable());
				t.start();
			}
		});
		
		comboBox = new JComboBox<String>();
		comboBox.setSize(800, 80);
		cp.add(comboBox);
		
		comboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getModifiers() > 1) {
					search.setText(((JComboBox<?>)e.getSource()).getSelectedItem().toString());	
					Thread t = new Thread(new DbRunnable());
					t.start();				
				}
			}
		});
		
		JButton next = new JButton("Traži");
		next.addActionListener(e -> {
			Thread t = new Thread(new DbRunnable(search.getText()));
			t.start();
		});
		cp.add(next);
		
		

	}


	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new Main().setVisible(true);
		});
	}
	


}
