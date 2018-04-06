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
	private Map<Integer, Journey> allJourneyMap;

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
		
		for(Integer nextID : referrals.keySet()) {
			Set<Date> referralDates = referrals.get(nextID).keySet();
			allJourneyMap.put(nextID, new Journey(nextID,referralDates));
		}
		
//Add assessment data to journey timelines
		for(int i = 1; i < assessmentCsvMap.size(); i++) {
			List<String> nextRow = assessmentCsvMap.get(i);
			Integer nextID = Integer.parseInt(nextRow.get(0));
			String assessmentStartDateString = nextRow.get(1);
			String assessmentEndDateString = nextRow.get(2);
			
			Date startDate;
			Date endDate;
			try {
				if(assessmentStartDateString.equals(""))
					startDate = new Date(1);
				else
					startDate = usaDateFormat2digitYear.parse(assessmentStartDateString);
				if(assessmentEndDateString.equals(""))
					endDate = new Date(1);
				else
					endDate = usaDateFormat2digitYear.parse(assessmentEndDateString);
			} catch (ParseException e) {
				System.err.println(e);
				continue;
			}
			
			if(allJourneyMap.containsKey(nextID)) {
				allJourneyMap.get(nextID).updateAssessment(startDate, endDate);
			} else {
				Journey newJourney = new Journey(nextID);
				newJourney.updateAssessment(startDate, endDate);
				allJourneyMap.put(nextID, newJourney);
				idList.add(nextID);
			}
		}
		
//Add CIN records to journey timelines
		for (int i = 1; i < cinCsvMap.size(); i++) {
			List<String> nextCSVrow = cinCsvMap.get(i);
			Integer nextID = Integer.parseInt(nextCSVrow.get(0));
			String cinStartString = nextCSVrow.get(1);
			String cinEndString = nextCSVrow.get(2);
			Date start, end;
			if(cinStartString.equals(""))
				start = new Date(1);
			else {
				try {
					start = usaDateFormat2digitYear.parse(cinStartString);
				}
				catch(ParseException e) {
					System.err.println(e);
					continue;
				}
			}
			if(cinEndString.equals(""))
				end = new Date(1);
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
				idList.add(nextID);
			}
			if(!allJourneyMap.containsKey(nextID)) {
				Journey newJourney = new Journey(nextID);
				newJourney.updateCIN(start, end);
				allJourneyMap.put(nextID, newJourney);
			} else {
				allJourneyMap.get(nextID).updateCIN(start, end);
			}
		}
		
//Add CPP records to journey timelines
		for(int i=1; i<cppCsvMap.size(); i++) {
			List<String> nextRow = cppCsvMap.get(i);
			Integer nextID = Integer.parseInt(nextRow.get(0));
			if(!idList.contains(nextID)) {
				idList.add(nextID);
			}
			String startDateString = nextRow.get(1),
					endDateString = nextRow.get(2);
			Date startDate, endDate;
			try {
				if(startDateString.equals(""))
					startDate = new Date(1);
				else
					startDate = usaDateFormat2digitYear.parse(startDateString);
				if(endDateString.equals(""))
					endDate = new Date(1);
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

//Add S47 data to journey timelines
		for(int i = 1; i < s47CsvMap.size(); i++) {
			List<String> nextRow = s47CsvMap.get(i);
			Integer nextID = Integer.parseInt(nextRow.get(0));
			Date startDate, endDate;
			if(!idList.contains(nextID)) {
				idList.add(nextID);
			}
			String startDateString = nextRow.get(1),
					endDateString = nextRow.get(2);
			try {
				if(startDateString.equals(""))
					startDate = new Date(1);
				else
					startDate = usaDateFormat2digitYear.parse(startDateString);
				if(endDateString.equals(""))
					endDate = new Date(1);
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
				endDate = new Date(1);
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