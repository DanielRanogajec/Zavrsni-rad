package zavrsni;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import model.Gen;

public class GenesListModel implements ListModel<Gen>{
	
	private List<Gen> elements;
	private List<ListDataListener> listeners;
	
	public GenesListModel(List<Gen> elements) {
		this.elements = elements;
		this.listeners = new ArrayList<>();
	}

	@Override
	public int getSize() {
		return elements.size();
	}

	@Override
	public Gen getElementAt(int index) {
		return elements.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}
	
	

}
