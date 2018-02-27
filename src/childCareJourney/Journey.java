package childCareJourney;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

// Data structure to represent an individual child or a cohort's journey through the system.
public class Journey {
	
	private int id;
	private Map<Integer, EStatus> journeyMap;
	private Date journeyStartDate;
	
	public Journey(int id, Date startDate) {
		this.id = id;
		this.journeyStartDate = startDate;
		journeyMap = new HashMap<Integer, EStatus>();
	}
	
	public void updateAssessment(Date assessmentStart, Date assessmentEnd) {
		int startDay = (int)TimeUnit.MILLISECONDS.toDays(assessmentStart.getTime() - journeyStartDate.getTime());
		journeyMap.put(startDay, EStatus.ASSESSMENT_START);
		int endDay = (int)TimeUnit.MILLISECONDS.toDays(assessmentEnd.getTime() - journeyStartDate.getTime());
		journeyMap.put(endDay, EStatus.ASSESSMENT_END);
	}
	
	public void updateCIN(Date cinStart, Date cinEnd) {
		int startDay = (int)TimeUnit.MILLISECONDS.toDays(cinStart.getTime() - journeyStartDate.getTime());
		journeyMap.put(startDay, EStatus.CIN_START);
		int endDay = (int)TimeUnit.MILLISECONDS.toDays(cinEnd.getTime() - journeyStartDate.getTime());
		journeyMap.put(endDay, EStatus.CIN_END);
	}
	
	public void updateS47(Date s47Start, Date s47End) {
		int startDay = (int)TimeUnit.MILLISECONDS.toDays(s47Start.getTime() - journeyStartDate.getTime());
		journeyMap.put(startDay, EStatus.S47_START);
		int endDay = (int)TimeUnit.MILLISECONDS.toDays(s47End.getTime() - journeyStartDate.getTime());
		journeyMap.put(endDay, EStatus.S47_END);
	}
	
	public void updateCpp(Date cppStart, Date cppEnd) {
		int startDay = (int)TimeUnit.MILLISECONDS.toDays(cppStart.getTime() - journeyStartDate.getTime());
		journeyMap.put(startDay, EStatus.CPP_START);
		int endDay = (int)TimeUnit.MILLISECONDS.toDays(cppEnd.getTime() - journeyStartDate.getTime());
		journeyMap.put(endDay, EStatus.CPP_END);
	}
	
	public void updateLac(Date lacStart, Date lacEnd) {
		int startDay = (int)TimeUnit.MILLISECONDS.toDays(lacStart.getTime() - journeyStartDate.getTime());
		journeyMap.put(startDay, EStatus.LAC_START);
		int endDay = (int)TimeUnit.MILLISECONDS.toDays(lacEnd.getTime() - journeyStartDate.getTime());
		journeyMap.put(endDay, EStatus.LAC_END);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Journey for id " + this.id +"\n");
		//TreeMap will sort the map by key
		TreeMap<Integer, EStatus> tm = new TreeMap<Integer, EStatus>(journeyMap);
		sb.append(tm);
		return sb.toString();
	}
	
	public Map<Integer, EStatus> getMap() {
		return new TreeMap<Integer, EStatus>(journeyMap);
	}
	
	public Double[] getXvals() {
		TreeMap<Integer, EStatus> treeMap = new TreeMap<Integer, EStatus>(journeyMap);
		ArrayList<Double> xValArrayList = new ArrayList<Double>();
		for(Integer nextInteger : treeMap.keySet()) {
			xValArrayList.add((double)nextInteger);
		}
		Double xVals[] = new Double[xValArrayList.size()];
		return xValArrayList.toArray(xVals);
	}
	
	public Double[] getYvals() {
		TreeMap<Integer, EStatus> treeMap = new TreeMap<Integer, EStatus>(journeyMap);
		ArrayList<Double> yValArrayList = new ArrayList<Double>();
		for(EStatus nextStatus : treeMap.values()) {
			yValArrayList.add((double)nextStatus.ordinal());
		}
		Double yVals[] = new Double[yValArrayList.size()];
		return yValArrayList.toArray(yVals);
	}
	
	public int getID() {
		return this.id;
	}
}

