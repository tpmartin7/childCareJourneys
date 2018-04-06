package childCareJourney;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class JourneySelector {
	
	private Map<Integer, Journey> allJourneyMap;
	
	public JourneySelector(Map<Integer, Journey> allJourneyMap) {
		this.allJourneyMap = allJourneyMap;
	}
	
	public Set<Integer> startWithReferral(){
		return startWithReferral(allJourneyMap.keySet());
	}
	
	public Set<Integer> startWithReferral(Set<Integer> selectedIDs){
		Set<Integer> targetJourneyIDs = new HashSet<Integer>();
	
		for(Integer nextID : selectedIDs) {
			Journey nextJourney = allJourneyMap.get(nextID);
			Map<Date, EStatus> journeyMap = nextJourney.getMap();
			Date[] keySet = new Date[journeyMap.size()];
			journeyMap.keySet().toArray(keySet);
			Date firstDate = keySet[0];
			EStatus firstStatus = journeyMap.get(firstDate);
			if(firstStatus == EStatus.REFERRAL)
				targetJourneyIDs.add(nextID);
		}
		return targetJourneyIDs;
	}
	
	public long getAverageJourneyLength() {
		return getAverageJourneyLength(allJourneyMap.keySet());
	}
	
	public long getAverageJourneyLength(Set<Integer> selectedIDs) {
		int count = selectedIDs.size();
		long totalDays = 0;
		for(Integer nextID : selectedIDs) {
			Journey nextJourney = allJourneyMap.get(nextID);
			Map<Date, EStatus> nextMap = nextJourney.getMap();
			if(nextMap.values().size() < 2) {
				count--;
				continue;
			}
			Set<Date> dateSet = nextMap.keySet();
			Date[] dateArray = new Date[dateSet.size()];
			dateSet.toArray(dateArray);
			Date journeyStart = dateArray[0];
			Date journeyEnd = dateArray[dateArray.length-1];
			long length = TimeUnit.DAYS.convert((journeyEnd.getTime() - journeyStart.getTime()), TimeUnit.MILLISECONDS);
			totalDays += length;
		}
		return totalDays / count;
	}
	
	public long getAverageLAClength() {
		return getAverageLAClength(allJourneyMap.keySet());
	}
	
	public long getAverageLAClength(Set<Integer> selectedIDs) {
		
		int lacCount = 0;
		long totalLength = 0;	
		for(Integer nextID : selectedIDs) {
			Journey nextJourney = allJourneyMap.get(nextID);
			Map<Date, EStatus> nextMap = nextJourney.getMap();
			if(nextMap.containsValue(EStatus.LAC_START) && nextMap.containsValue(EStatus.LAC_END)) {
				int entries = nextMap.size();
				Date [] dates = new Date[entries];
				nextMap.keySet().toArray(dates);
				EStatus [] statuses = new EStatus [entries];
				nextMap.values().toArray(statuses);
				int startCount = 0,
						endCount = 0;
				for(EStatus nextStatus : statuses) {
					if(nextStatus == EStatus.LAC_START)
						startCount++;
					else if(nextStatus == EStatus.LAC_END)
						endCount++;
				}
				if(startCount == endCount) {
					Date startDate = null, endDate;
					for(int i = 0; i < entries; i++) {
						if(statuses[i] == EStatus.LAC_START) {
							startDate = dates[i];
							lacCount++;
						} else if (statuses[i] == EStatus.LAC_END && startDate != null) {
							endDate = dates[i];
							long length = TimeUnit.DAYS.convert((endDate.getTime() - startDate.getTime()), TimeUnit.MILLISECONDS);
							totalLength += length;
						}
					}
				}
			}
		}
		return (totalLength / lacCount);
	}
	
	public Date dateOfLastEvent() {
		return dateOfLastEvent(allJourneyMap.keySet());
	}
	
	public Date dateOfLastEvent(Set<Integer> selectedIDs) {
		Date lastDate = new Date(1);
		Set<Journey> selectedJourneys = new HashSet<Journey>();
		for(Integer nextId : selectedIDs) {
			selectedJourneys.add(allJourneyMap.get(nextId));
		}
		for(Journey nextJourney : selectedJourneys) {
			for(Date nextDate : nextJourney.getMap().keySet()) {
				if(nextDate.after(lastDate)){
					lastDate = nextDate;
				}
			}
		}
		return lastDate;
	}
	
	public Date dateOfFirstEvent() {
		return dateOfFirstEvent(allJourneyMap.keySet());
	}
	
	public Date dateOfFirstEvent(Set<Integer> selectedIDs) {
		Date firstDate = new Date();
		Set<Journey> selectedJourneys = new HashSet<Journey>();
		for(Integer nextId : selectedIDs) {
			selectedJourneys.add(allJourneyMap.get(nextId));
		}
		for(Journey nextJourney : selectedJourneys) {
			for(Date nextDate : nextJourney.getMap().keySet()) {
				if(nextDate.before(firstDate)){
					firstDate = nextDate;
				}
			}
		}
		return firstDate;
	}
}
