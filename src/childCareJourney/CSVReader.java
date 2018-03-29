package childCareJourney;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class CSVReader {
	//TODO: Use Guava tables where appropriate
	private Map<Integer, TreeMap<Date, Referral>> referrals;
	private Map<Integer, TreeMap<Date, CPP>> cppStore;
	private Set<Integer> idList;
	Map<Integer, Journey> allJourneyMap;

	public CSVReader() {
		String defaultPath = "data\\";
		this.referrals = new HashMap<Integer, TreeMap<Date, Referral>>();
		this.cppStore = new HashMap<Integer, TreeMap<Date,CPP>>();
		this.idList = new TreeSet<Integer>();
		this.allJourneyMap = new TreeMap<Integer, Journey>();
		
		List<List<String>> 	referralCsvMap = scanCsv(Paths.get(defaultPath + "AllReferrals.csv")),
							assessmentCsvMap = scanCsv(Paths.get(defaultPath + "AllAssessments.csv")),
							cinCsvMap = scanCsv(Paths.get(defaultPath + "CIN.csv")),
							cppCsvMap = scanCsv(Paths.get(defaultPath + "CPP.csv")),
							s47CsvMap = scanCsv(Paths.get(defaultPath + "S47.csv")),
							lacStartCsvMap = scanCsv(Paths.get(defaultPath + "LAC_start.csv")),
							lacEndCsvMap = scanCsv(Paths.get(defaultPath + "LAC_end.csv"));
		
		
		SimpleDateFormat usaDateFormat2digitYear = new SimpleDateFormat("MM/dd/yy"); // US date format
		

		
		for(int i = 1; i < referralCsvMap.size(); i++) {
			List<String> nextRow = referralCsvMap.get(i);
			Integer id;
			try{ 
				id = Integer.parseInt(nextRow.get(0));
				if(!idList.contains(id)) 
					idList.add(id);
			}
			catch(NumberFormatException e){ 
				continue; 
			}
			try {
				Integer age;
				if(nextRow.get(1).equals("")) {
					age = -1;
				}
				else age = Integer.parseInt(nextRow.get(1));
				
				String genderString = nextRow.get(2);
				EGender gender;
				if(genderString.startsWith("M")) gender = EGender.MALE;
				else if(genderString.startsWith("F")) gender = EGender.FEMALE;
				else gender = EGender.UNKNOWN;
				
				String ethnicityString = nextRow.get(3);
				EEthnicity ethnicity;
				if(ethnicityString.contains("White")) ethnicity = EEthnicity.WHITE;
				else if(ethnicityString.contains("Black")) ethnicity = EEthnicity.BLACK;
				else if(ethnicityString.contains("Asian")) ethnicity = EEthnicity.ASIAN;
				else if(ethnicityString.contains("Chinese")) ethnicity = EEthnicity.ORIENTAL;
				else if(ethnicityString.contains("Mixed")) ethnicity = EEthnicity.MIXED;
				else if(ethnicityString.contains("Other")) ethnicity = EEthnicity.OTHER;
				else ethnicity = EEthnicity.UNKOWN;
				
//TODO: Detect ward here
				
				Date date = usaDateFormat2digitYear.parse(nextRow.get(5));
				if(referrals.containsKey(id)) {
					
//TODO: Doesnt need to be new referral, change date in Referral type to a set 
//and have referrals map to a single referral object
					
					referrals.get(id).put(date, new Referral(id, age, gender, ethnicity, EWard.ALEXANDRA, date));
				}
				else {
					TreeMap<Date, Referral> newMap = new TreeMap<Date, Referral>();
					newMap.put(date, new Referral(id, age, gender, ethnicity, EWard.ALEXANDRA, date));
					referrals.put(id, newMap);
				}
			} catch (ParseException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
		
		System.out.println(referralCsvMap.size() - 1 + " total referrals for " + referrals.size() + " unique IDs");
		
		for(Integer nextID : referrals.keySet()) {
			Set<Date> referralDates = referrals.get(nextID).keySet();
			allJourneyMap.put(nextID, new Journey(nextID,referralDates));
		}
		System.out.println(allJourneyMap.size() + " journeys created");
		
		//int i = 0;
		
		//Add assessment data to journey timelines
		int noReferCount = 0;
		Set<Integer> assIDlist = new TreeSet<Integer>();
		for(int i = 1; i < assessmentCsvMap.size(); i++) {
			List<String> nextRow = assessmentCsvMap.get(i);
			Integer nextID = Integer.parseInt(nextRow.get(0));
			if(!assIDlist.contains(nextID))
				assIDlist.add(nextID);
			String assessmentStartDateString = nextRow.get(1);
			String assessmentEndDateString = nextRow.get(2);
			
			Date startDate;
			Date endDate;
			try {
				if(assessmentStartDateString.equals(""))
					startDate = new Date();
				else
					startDate = usaDateFormat2digitYear.parse(assessmentStartDateString);
				if(assessmentEndDateString.equals(""))
					endDate = new Date();
				else
					endDate = usaDateFormat2digitYear.parse(assessmentEndDateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
			if(allJourneyMap.containsKey(nextID)) {
				allJourneyMap.get(nextID).updateAssessment(startDate, endDate);
			} else {
				noReferCount++;
				Journey newJourney = new Journey(nextID);
				newJourney.updateAssessment(startDate, endDate);
				allJourneyMap.put(nextID, newJourney);
				idList.add(nextID);
			}
		}
		System.out.println(assessmentCsvMap.size() + " assessment records added for " + assIDlist.size() + " unique IDs.");
		System.out.println(noReferCount + " assessments had no referral.");
		System.out.println(allJourneyMap.size() + " journeys now created.");
		

//Add CIN records to journey timelines
		int CINonly = 0;
		Set<Integer> cinIDlist = new TreeSet<Integer>();
		for (int i = 1; i < cinCsvMap.size(); i++) {
			List<String> nextCSVrow = cinCsvMap.get(i);
			Integer nextID = Integer.parseInt(nextCSVrow.get(0));
			String cinStartString = nextCSVrow.get(1);
			String cinEndString = nextCSVrow.get(2);
			Date start, end;
			if(cinStartString.equals(""))
				start = new Date();
			else {
				try {
					start = usaDateFormat2digitYear.parse(cinStartString);
				}
				catch(ParseException e) {
					System.out.println(e);
					continue;
				}
			}
			if(cinEndString.equals(""))
				end = new Date();
			else {
				try {
					end = usaDateFormat2digitYear.parse(cinEndString);
				}
				catch(ParseException e) {
					System.out.println(e);
					continue;
				}
			}
			
			if(!idList.contains(nextID))
			{
				CINonly++;
				idList.add(nextID);
			}
			if(!cinIDlist.contains(nextID))
				cinIDlist.add(nextID);
			if(!allJourneyMap.containsKey(nextID)) {
				Journey newJourney = new Journey(nextID);
				newJourney.updateCIN(start, end);
				allJourneyMap.put(nextID, newJourney);
			} else {
				allJourneyMap.get(nextID).updateCIN(start, end);
			}
		}
		System.out.println(cinCsvMap.size() + " total CIN records for " + cinIDlist.size() + " unique IDs.");
		System.out.println(CINonly + " IDs appeared in CIN without referral or assessment.");
		System.out.println(allJourneyMap.size() + " journeys now created.");
		
//Add CPP records to journey timelines
		int cppOnly = 0;
		Set<Integer> cppIDlist = new TreeSet<Integer>();
		for(int i=1; i<cppCsvMap.size(); i++) {
			List<String> nextRow = cppCsvMap.get(i);
			Integer nextID = Integer.parseInt(nextRow.get(0));
			if(!idList.contains(nextID)) {
				idList.add(nextID);
				cppOnly++;
			}
			if(!cppIDlist.contains(nextID))
				cppIDlist.add(nextID);
			String startDateString = nextRow.get(1),
					endDateString = nextRow.get(2);
			Date startDate, endDate;
			try {
				if(startDateString.equals(""))
					startDate = new Date();
				else
					startDate = usaDateFormat2digitYear.parse(startDateString);
				if(endDateString.equals(""))
					endDate = new Date();
				else
					endDate = usaDateFormat2digitYear.parse(endDateString);
			} catch (ParseException e) {
				System.err.println(e);
				continue;
			}
			String allStatuses = nextRow.get(3);
			Set<ECPPStatus> statusSet = new TreeSet<ECPPStatus>();
			if(allStatuses.contains("Emotional"))
				statusSet.add(ECPPStatus.EMOTIONAL_ABUSE);
			if(allStatuses.contains("Neglect"))
				statusSet.add(ECPPStatus.NEGLECT);
			if(allStatuses.contains("Physical"))
				statusSet.add(ECPPStatus.PHYSICAL_ABUSE);
			if(allStatuses.contains("Sexual"))
				statusSet.add(ECPPStatus.SEXUAL_ABUSE);
			if(cppStore.containsKey(nextID))
				cppStore.get(nextID).put(startDate, new CPP(nextID, startDate, endDate, statusSet));
			else {
				TreeMap<Date, CPP> newMap = new TreeMap<Date, CPP>();
				newMap.put(startDate, new CPP(nextID, startDate, endDate, statusSet));
				cppStore.put(nextID, newMap);
			}
			if(allJourneyMap.containsKey(nextID)) {
				allJourneyMap.get(nextID).updateCpp(startDate, endDate);
			} else {
				Journey newJourney = new Journey(nextID);
				newJourney.updateCpp(startDate, endDate);
				allJourneyMap.put(nextID, newJourney);
			}
		}
		System.out.println(cppCsvMap.size() + " total CPP records for " + cppIDlist.size() + " unique IDs.");
		System.out.println(cppOnly + " IDs appeared in CPP without CIN, referral, or assessment.");
		System.out.println(allJourneyMap.size() + " journeys now created.");

		
//Add S47 data to journey timelines
		int s47only = 0;
		Set<Integer> s47IDs = new TreeSet<Integer>();
		for(int i = 1; i < s47CsvMap.size(); i++) {
			List<String> nextRow = s47CsvMap.get(i);
			Integer nextID = Integer.parseInt(nextRow.get(0));
			Date startDate, endDate;
			if(!idList.contains(nextID)) {
				s47only++;
				idList.add(nextID);
			}
			if(!s47IDs.contains(nextID))
				s47IDs.add(nextID);
			String startDateString = nextRow.get(1),
					endDateString = nextRow.get(2);
			try {
				if(startDateString.equals(""))
					startDate = new Date();
				else
					startDate = usaDateFormat2digitYear.parse(startDateString);
				if(endDateString.equals(""))
					endDate = new Date();
				else
					endDate = usaDateFormat2digitYear.parse(endDateString);
			} catch (ParseException e) {
				System.err.println(e);
				continue;
			}
			if(allJourneyMap.containsKey(nextID)) {
				allJourneyMap.get(nextID).updateS47(startDate, endDate);
			} else {
				Journey newJourney = new Journey(nextID);
				newJourney.updateS47(startDate, endDate);
				allJourneyMap.put(nextID, newJourney);
			}
		}
		System.out.println(s47CsvMap.size() + " total s47 records for " + s47IDs.size() + " unique IDs.");
		System.out.println(s47only + " IDs appeared in s47 without CPP, CIN, referral, or assessment.");
		System.out.println(allJourneyMap.size() + " journeys now created.");

//		Add LAC start dates to journey timelines.
//TODO: Include data structure for other columns in LAC_start
		
		for(int i=1; i<lacStartCsvMap.size(); i++) {
			List<String> nextRow = lacStartCsvMap.get(i);
			Integer nextID = Integer.parseInt(nextRow.get(0));
			if(!idList.contains(nextID))
				idList.add(nextID);
			Date startDate;
			String startDateString = nextRow.get(1);
			try {
				startDate = usaDateFormat2digitYear.parse(startDateString);
			} catch (ParseException e) {
				System.err.println(e);
				continue;
			}
			if(allJourneyMap.containsKey(nextID)) {
				allJourneyMap.get(nextID).updateLacStart(startDate);
			} else {
				Journey newJourney = new Journey(nextID);
				newJourney.updateLacStart(startDate);
				allJourneyMap.put(nextID, newJourney);
			}
		}
		System.out.println("LAC starts updated. " + allJourneyMap.size() + " journeys now created.");
		
//		Add LAC end records to journey timelines.
//		TODO: Create / update data structure to store other columns in LAC_end.
		
		for(int i=1; i<lacEndCsvMap.size(); i++) {
			List<String> nextRow = lacEndCsvMap.get(i);
			Integer nextID = Integer.parseInt(nextRow.get(0));
			if(!idList.contains(nextID))
				idList.add(nextID);
			String endDateString = nextRow.get(1);
			Date endDate;
			if(endDateString.equals(""))
				endDate = new Date();
			else {
				try {
					endDate = usaDateFormat2digitYear.parse(endDateString);
				} catch (ParseException e) {
					System.err.println(e);
					continue;
				}
			}
			if(allJourneyMap.containsKey(nextID))
				allJourneyMap.get(nextID).updateLacEnd(endDate);
			else {
				Journey newJourney = new Journey(nextID);
				newJourney.updateLacEnd(endDate);
				allJourneyMap.put(nextID, newJourney);
			}					
		}
		System.out.println("LAC ends updated. " + allJourneyMap.size() + " journeys now created.");

		Set<Integer> targetJourneyIDs = new HashSet<Integer>();

		for(Integer nextID : allJourneyMap.keySet()) {
			Journey nextJourney = allJourneyMap.get(nextID);
			Map<Date, EStatus> journeyMap = nextJourney.getMap();
			Date[] keySet = new Date[journeyMap.size()];
			journeyMap.keySet().toArray(keySet);
			Date firstDate = keySet[0];
			EStatus firstStatus = journeyMap.get(firstDate);
			if(firstStatus == EStatus.REFERRAL)
				targetJourneyIDs.add(nextID);
		}
		
		Integer [] referralFirst = new Integer [targetJourneyIDs.size()];
		targetJourneyIDs.toArray(referralFirst);
		
		int lacCount = 0;
		long totalLength = 0;	
		for(Integer nextID : referralFirst) {
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
							System.out.print(length + " ");
							totalLength += length;
						}
					}
				}
			}
		}
		System.out.println();
		System.out.println("Average LAC length : " + (totalLength / lacCount) + " days");

		//		new ChartJourney(journeyList);
	}
	
	public static List<List<String>> scanCsv(Path filePath){
		List<List<String>> csvMap = new ArrayList<>();
		try(Scanner scanner = new Scanner(filePath)) {
			while(scanner.hasNext()) {
				csvMap.add(parseLine(scanner.nextLine()));
			}
		}
		catch (IOException e) {
			System.err.println(e.getMessage());	
		}
		return csvMap;
	}
	
	public static List<String> parseLine(String csvLine){
		List<String> result = new ArrayList<>();
		
		if(csvLine == null || csvLine.isEmpty())
			return result;
		
		StringBuffer curVal = new StringBuffer();
		
		char[] chars = csvLine.toCharArray();

		boolean quoted = false;
		for(char ch : chars) {
			if(ch == '"') {
				quoted = !quoted;
			}
			if(ch == ',' && quoted == false) {
				result.add(curVal.toString());
				curVal = new StringBuffer();
			} else if (ch == '\n') {
				break;
			} else {
				curVal.append(ch);
			}
		}
		
		result.add(curVal.toString());
		
		return result;
	}
	
	public Map<Integer, TreeMap<Date, Referral>> getReferrals() {
		return referrals;
	}

	public Map<Integer, TreeMap<Date, CPP>> getCppStore() {
		return cppStore;
	}

	public Set<Integer> getIdList() {
		return idList;
	}

	public Map<Integer, Journey> getAllJourneyMap() {
		return allJourneyMap;
	}
}