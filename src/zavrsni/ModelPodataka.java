package zavrsni;

import java.util.Arrays;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class ModelPodataka extends AbstractTableModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String[] dataKey;
	private String[] dataValue;
	
	
	public ModelPodataka(Map<String, String> data) {
		dataKey = Arrays.copyOf(data.keySet().toArray(), data.size(), String[].class);
		dataValue = Arrays.copyOf(data.values().toArray(), data.size(), String[].class);
	}

	@Override
	public int getRowCount() {
		return dataKey.length;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return switch(columnIndex) {
		case 0 -> dataKey[rowIndex];
		case 1 -> dataValue[rowIndex];
		default -> throw new IllegalArgumentException();
		};
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	
	@Override
	public String getColumnName(int column) {
		return "";
	}
	
}
