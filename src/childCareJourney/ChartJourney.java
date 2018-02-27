package childCareJourney;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

public class ChartJourney {
	
	public ChartJourney(List<Journey> chartTheseJourneys) {
	
	Journey chartThisJourney = chartTheseJourneys.get(0);
	
	//Collate x axes.
	ArrayList<Integer> collatedXaxis = new ArrayList<>();
	for(Journey nextJourney : chartTheseJourneys) {
		collatedXaxis.addAll(nextJourney.getMap().keySet());
	}
	Set<Integer> removeDuplicateDays = new HashSet<>(collatedXaxis);
	collatedXaxis.clear();
	collatedXaxis.addAll(removeDuplicateDays);
	Collections.sort(collatedXaxis);
	System.out.println("Collated x vals: " + collatedXaxis);
	
	Map<Integer, EStatus> rawMap = chartThisJourney.getMap();
	
	//Convert days to array of doubles for quickchart
	ArrayList<Double> days = new ArrayList<Double>();
	for(Integer nextInteger : rawMap.keySet()) {
		days.add(nextInteger.doubleValue());
	}
	
	//For each journey, create an array of y values equal to the length of the array of x values.
	//Insert the status value at the appropriate index of this array.
	//The array of x values contains all days for all journeys.
	//Search this array for the next day in the current journey. Insert the status value at this index.
	ArrayList<ArrayList<EStatus>> allEvents = new ArrayList<>();

	for(int j = 0; j < chartTheseJourneys.size(); j++) {
		ArrayList<EStatus> currentEvents = new ArrayList<>(chartTheseJourneys.get(j).getMap().values());
		ArrayList<Integer> currentDays = new ArrayList<>(chartTheseJourneys.get(j).getMap().keySet());
		ArrayList<EStatus> translatedEvents = new ArrayList<>();
		for(int k = 0, l = 0; k < collatedXaxis.size(); k++) {
			if(l<currentEvents.size() && collatedXaxis.get(k) == currentDays.get(l)) {
				translatedEvents.add(currentEvents.get(l));
				l++;
			}
			else {
				translatedEvents.add(null);
			}	
		}
		allEvents.add(translatedEvents);
		System.out.println("Collated day array length: " + collatedXaxis.size());
		System.out.println("Translated event list length: " + translatedEvents.size());
	}
	
	System.out.println("allEvents : " + allEvents);
	/*
	ArrayList<EStatus> statusArrayList = new ArrayList<>(rawMap.values());

	ArrayList<Integer> yValArrayList = new ArrayList<Integer>();
	*/
	
	//Assigns level 1-5 for chart y axis.
	List<List<Integer>> allYvalues = new ArrayList<>();
	for(ArrayList<EStatus> nextStatusList : allEvents) {
		ArrayList<Integer> nextYValueList = new ArrayList<>();
	
		for(EStatus nextStatus : nextStatusList) {
			if (nextStatus == null)
				nextYValueList.add(null);
			else
				switch(nextStatus) {
					case ASSESSMENT_START: 
					case ASSESSMENT_END:
						nextYValueList.add(1);
						break;
					case CIN_END:
					case CIN_START:
						nextYValueList.add(2);
						break;
					case S47_END:
					case S47_START:
						nextYValueList.add(3);
						break;
					case CPP_START:
					case CPP_END: 
						nextYValueList.add(4);
						break;
					case LAC_START:
					case LAC_END:
						nextYValueList.add(5);
						break;
					default:
						break;
				}
		}
		
		allYvalues.add(nextYValueList);
		System.out.println("y valu list length " + nextYValueList.size());
	}
	System.out.println("all Y values : " + allYvalues);
	
	String[] fakeNames = { "Bill", "bob", "jane", "frank", "ann" };
	
	XYChart chart = getChart("Sample Journey", "Days", "Level", fakeNames, collatedXaxis, allYvalues/*statusArrayList*/);

	
	new SwingWrapper<XYChart>(chart).displayChart();
	}
	public static XYChart getChart(String chartTitle, String xTitle, String yTitle, String[] seriesNames, List<? extends Number> xData, List<List<Integer>> yData) {

	    // Create Chart
	    XYChart chart = new XYChart(600, 300);

	    // Customise Chart
	    chart.setTitle(chartTitle);
	    chart.setXAxisTitle(xTitle);
	    chart.setYAxisTitle(yTitle);
	    
	    for(int i = 0; i < yData.size(); i++) {
		    XYSeries series = chart.addSeries(seriesNames[i], xData, yData.get(i));
		    series.setMarker(SeriesMarkers.NONE);
	    }
	    return chart;
	  }
}
