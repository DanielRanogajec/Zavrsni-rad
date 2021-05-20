package zavrsni;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.WindowConstants;
import javax.swing.table.TableRowSorter;

import model.Gen;
import postgres.database.tools.DatabaseConnection;
import postgres.database.tools.DownloadFastaFile;
import postgres.database.tools.GeneParser;
import postgres.database.tools.GroupFastaFiles;
import postgresSeed.FileReader;
import zavrsni.Main.DbRunnable;

public class NewWindow extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JButton newSeq;
	private JButton seq;
	private Map<String, String> data;
	private String parent;
	private Map<String, String> dataInfo;
	private String fileLocation;
	private List<Gen> genes;
	private JButton genButton;
	private Action a;

	public NewWindow(String name, Map<String, String> data) {
		if (data == null) 
			dispose();
		parent = data.remove("parent_name");
		fileLocation = data.remove("file_location");
		this.data = data;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getGenes(name);
		initGUI();
		setSize(800,300);
		setLocationRelativeTo(null);
		setTitle(name);
		setVisible(true);
	}

	private void initGUI() {

		Container cp = this.getContentPane();

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		panel.setLayout(new BorderLayout());
		panel.setBackground(Color.CYAN);

		TableModel model = new TableModel(data);

		initDataInfo();

		JTable table = new JTable(model) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(MouseEvent e) {
				String toolTipText = null;
				Point p = e.getPoint();
				int row= rowAtPoint(p);
				int col= columnAtPoint(p);
				if (col== 1)
					return null;

				if(col == 0){
					toolTipText = dataInfo.get((getValueAt(row, 0).toString()));
				}

				return toolTipText;
			}

		};
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
		table.setRowSorter(sorter);
		List<RowSorter.SortKey> sortKey = new ArrayList<>();
		sortKey.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKey);


		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(table.getPreferredSize());
		panel.add(scrollPane, BorderLayout.CENTER);

		JPanel panel2 = new JPanel(new GridLayout(2,1));
		panel2.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		panel2.setBackground(Color.CYAN);
		JPanel panel3 = new JPanel(new GridLayout(1,2));
		panel3.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
		panel3.setBackground(Color.CYAN);

		if (fileLocation != null && parent != null) {
			seq = new JButton("Preuzmi sekvencu genoma za " + parent + ".");
			seq.addActionListener(saveFastaAction);
			panel3.add(seq);
			newSeq = new JButton("Dodaj sekvencu genoma za " + data.get("name_txt"));
			newSeq.addActionListener(addFastaAction);
			panel3.add(newSeq, BorderLayout.PAGE_END);
		} else if (fileLocation != null) {
			seq = new JButton("Preuzmi sekvencu genoma.");
			seq.addActionListener(saveFastaAction);
			panel3.add(seq, BorderLayout.PAGE_END);
		} else {
			newSeq = new JButton("Dodaj sekvencu genoma.");
			newSeq.addActionListener(addFastaAction);
			panel3.add(newSeq, BorderLayout.PAGE_END);
		}

		panel2.add(panel3);
		
		
		if (genes == null || genes.isEmpty()) {
			genButton = new JButton("Dodaj gene.");
			
			a = addGenesAction;
			genButton.addActionListener(a);
			
			

		} else {
			genButton = new JButton("Prikaži gene.");
			genButton.addActionListener(e -> new GenesWindow(genes));
		}
		panel2.add(genButton);
		panel.add(panel2, BorderLayout.PAGE_END);
		cp.add(panel);

	}



	private void getGenes(String name) {
		try {
			List<String> userData = DatabaseConnection.Connect();
			DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + userData.get(0), userData.get(1), userData.get(2));
			if (userData != null) {
				try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + userData.get(0), userData.get(1), userData.get(2))){

					try {
						PreparedStatement pstmt = connection.prepareStatement("select * from genes where organism = ?;");
						pstmt.setString(1, name);
						ResultSet rs = pstmt.executeQuery();
						genes = new ArrayList<>();

						while (rs.next()) {
							Gen g = new Gen();
							g.setSymbol(rs.getString("symbol"));
							g.setID(rs.getString("id"));
							g.setGene_description(rs.getString("gene_description"));
							g.setOrganism(rs.getString("organism"));
							g.setGenomic_context(rs.getString("genomic_context"));
							g.setAnnotation(rs.getString("annotation"));
							g.setOther_aliases(rs.getString("other_aliases"));
							g.setOther_designations(rs.getString("other_designations"));
							genes.add(g);
						}

					} catch (SQLException ex1) {

					}

				} 
			}

		} catch (SQLException | IOException ex2) {}
	}

	private Action addGenesAction = new AbstractAction() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {			
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Odaberite .txt datoteku sa genima!");
			fc.setMultiSelectionEnabled(true);
			if(fc.showOpenDialog(NewWindow.this)!=JFileChooser.APPROVE_OPTION) 
				return;
			File file = fc.getSelectedFile();
			genes = GeneParser.parseGenes(file);
			addToDb();
			genButton.removeActionListener(a);
			genButton.setText("Prikaži gene.");
			genButton.addActionListener(e1 -> new GenesWindow(genes));
			
		}

	};

	private Action addFastaAction = new AbstractAction() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Odaberite .fasta datoteku!");
			fc.setMultiSelectionEnabled(true);
			if(fc.showOpenDialog(NewWindow.this)!=JFileChooser.APPROVE_OPTION) 
				return;

			try {
				String name = Arrays.stream(data.get("name_txt").split(" ")).collect(Collectors.joining("_"));
				File[] files = fc.getSelectedFiles();
				if (files == null || files.length == 0) {
					return;
				} else if (files.length == 1) {
					DownloadFastaFile.download(name, 
							Files.readAllLines(files[0].toPath()), new File("src/resources/reference_genomes/"));
				} else {
					List<String> l = GroupFastaFiles.groupFiles(files);
					if (l == null || l.isEmpty())
						return;
					DownloadFastaFile.download(name, 
							l, new File("src/resources/reference_genomes/"));
				}

				Thread t = new Thread(new DbRunnable(name + ".fasta", DbRunnable.INSERT));
				t.start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
	};

	private Action saveFastaAction = new AbstractAction() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {

			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setDialogTitle("Odaberite direktorij u koji želite spremiti sekvencu!");
			if(jfc.showSaveDialog(NewWindow.this)!=JFileChooser.APPROVE_OPTION) {
				JOptionPane.showMessageDialog(
						NewWindow.this, 
						"Ništa nije spremljeno!", 
						"Upozorenje!", 
						JOptionPane.WARNING_MESSAGE);	
			}
			System.out.println(jfc.getSelectedFile());
			DownloadFastaFile.download(Arrays.stream(data.get("name_txt").split(" ")).collect(Collectors.joining("_")), 
					FileReader.readFile("reference_genomes/" + fileLocation), jfc.getSelectedFile());
		}
	};

	private void initDataInfo() {
		dataInfo = new HashMap<String, String>();

		dataInfo.put("tax_id", "Node id in GenBank taxonomy database");
		dataInfo.put("name_txt", "Name itself");
		dataInfo.put("unique_name", "The unique variant of this name if name not unique");
		dataInfo.put("parent_tax_id", "Parent node id in GenBank taxonomy database");
		dataInfo.put("abbrevation", "Genetic code name abbreviation");
		dataInfo.put("cde", "Translation table for this genetic code");
		dataInfo.put("division_cde", "GenBank division code (three characters)");
		dataInfo.put("division_name", "E.g. BCT, PLN, VRT, MAM, PRI...");
		dataInfo.put("division_id", "Taxonomy database division id");
		dataInfo.put("embl_code", "Locus-name prefix; not unique");
		dataInfo.put("genbank_hidden_flag", "1 if name is suppressed in GenBank entry lineage");
		dataInfo.put("genetic_code_id", "GenBank genetic code id");
		dataInfo.put("hidden_subtree_root_flag", "1 if this subtree has no sequence data yet");
		dataInfo.put("inherited_div_flag", "1 if node inherits division from parent");
		dataInfo.put("inherited_GC_flag", "1 if node inherits genetic code from parent");
		dataInfo.put("inheritedMGC_flag", "1 if node inherits mitochondrial gencode from parent");
		dataInfo.put("mitochondrial_genetic_code_id", "Mitochonrial GenBank genetic code id");
		dataInfo.put("name", "Genetic code name");
		dataInfo.put("name_class", "(Synonym, common name, ...)");
		dataInfo.put("rank", "Rank of this node (superkingdom, kingdom, ...)");
		dataInfo.put("starts", "Start codons for this genetic code");
	}

	protected void addToDb() {
		String SQLinsert = "INSERT INTO genes VALUES(?,?,?,?,?,?,?,?);";
		List<String> userData = null;
		try {
			userData = DatabaseConnection.Connect();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (userData == null)
			return;
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + userData.get(0), userData.get(1), userData.get(2))){

			try (PreparedStatement pstmt = connection.prepareStatement(SQLinsert)) {

				int counter = 0;
				for (Gen gen : genes) {
															
					if (gen.getSymbol() == null)
						continue;
					
					pstmt.setString(1, gen.getSymbol());
					pstmt.setString(2, gen.getID());
					if (gen.getGene_description() == null) 
						pstmt.setNull(3, java.sql.Types.VARCHAR);
					else
						pstmt.setString(3, gen.getGene_description());
					pstmt.setString(4, gen.getOrganism());
					if (gen.getGenomic_context() == null) 
						pstmt.setNull(5, java.sql.Types.VARCHAR);
					else
						pstmt.setString(5, gen.getGenomic_context());
					if (gen.getAnnotation() == null) 
						pstmt.setNull(6, java.sql.Types.VARCHAR);
					else
						pstmt.setString(6, gen.getAnnotation());
					if (gen.getOther_aliases() == null) 
						pstmt.setNull(7, java.sql.Types.VARCHAR);
					else
						pstmt.setString(7, Arrays.stream(gen.getOther_aliases()).collect(Collectors.joining(", ")));
					if (gen.getOther_designations() == null) 
						pstmt.setNull(8, java.sql.Types.VARCHAR);
					else
						pstmt.setString(8, Arrays.stream(gen.getOther_designations()).collect(Collectors.joining(", ")));
								
					
					pstmt.addBatch();

					if (++counter % 100 == 0 || counter == genes.size())
						pstmt.executeBatch();
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

}
