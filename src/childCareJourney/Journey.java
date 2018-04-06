package childCareJourney;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

// Data structure to represent an individual child or a cohort's journey through the system.
public class Journey {
	
	private Integer id;
	private Map<Date, EStatus> journeyMap;
	private Date journeyStartDate;
	
	public Journey(Integer id) {
		this.id = id;
		journeyMap = new TreeMap<Date, EStatus>();
	}
	
	public Journey(Integer id, Set<Date> referralDates) {
		this.id = id;
		journeyMap = new TreeMap<Date, EStatus>();
		updateReferrals(referralDates);
	}
	
	public void updateReferrals(Set<Date> referralDates) {
		for(Date nextDate : referralDates)
			journeyMap.put(nextDate, EStatus.REFERRAL);
	}
	
	public void updateAssessment(Date start, Date end) {
		journeyMap.put(start, EStatus.ASSESSMENT_START);
		journeyMap.put(end, EStatus.ASSESSMENT_END);
	}
	
	public void updateCIN(Date cinStart, Date cinEnd) {
		journeyMap.put(cinStart, EStatus.CIN_START);
		journeyMap.put(cinEnd, EStatus.CIN_END);
	}
	
	public void updateS47(Date s47Start, Date s47End) {
		journeyMap.put(s47Start, EStatus.S47_START);
		journeyMap.put(s47End, EStatus.S47_END);
	}
	
	public void updateCpp(Date cppStart, Date cppEnd) {
		journeyMap.put(cppStart, EStatus.CPP_START);
		journeyMap.put(cppEnd, EStatus.CPP_END);
	}
	
	public void updateLacStart(Date lacStart) {
		journeyMap.put(lacStart, EStatus.LAC_START);
	}
	
	public void updateLacEnd(Date lacEnd) {
		journeyMap.put(lacEnd, EStatus.LAC_END);
	}
	/*
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
	*/
	public int getID() {
		return this.id;
	}
	
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
		StringBuilder sb = new StringBuilder("Journey for id " + this.id +"\n");
		for(Date nextDate : journeyMap.keySet()) {
			sb.append(sdf.format(nextDate));
			sb.append(" : ");
			sb.append(journeyMap.get(nextDate));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public Map<Date, EStatus> getMap() {
		return journeyMap;
	}
	
	public CategoryDataset getDatasetForIndividualGraph() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for(Date nextDate : journeyMap.keySet()) {
			EStatus nextStatus = journeyMap.get(nextDate);
			int ord = nextStatus.ordinal();
			if(ord>0) {
				ord++;
				ord /= 2;
			}
			dataset.addValue(ord, id, nextDate);
		}
		return dataset;
	}
}

