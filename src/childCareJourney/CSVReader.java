package childCareJourney;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.crypto.CipherInputStream;

public class CSVReader {

	public static void main(String[] args) {
		String defaultPath = "data\\";
		if (args.length > 0) {
			defaultPath = args[0];
		}
		
		HashMap<Integer, TreeMap<Date, Referral>> referrals = new HashMap<Integer, TreeMap<Date, Referral>>();
		
		List<List<String>> 	referralCsvMap = scanCsv(Paths.get(defaultPath + "Referrals1.csv")),
							assessmentCsvMap = scanCsv(Paths.get(defaultPath + "AllAssessments.csv")),
							cinCsvMap = scanCsv(Paths.get(defaultPath + "CIN.csv"));
							/*cppCsvMap = scanCsv(Paths.get(defaultPath + "sampleCpp.csv")),
							lacCsvMap = scanCsv(Paths.get(defaultPath + "sampleLac.csv")),
							s47CsvMap = scanCsv(Paths.get(defaultPath + "sampleS47.csv"));*/
		
		
		SimpleDateFormat usaDateFormat2digitYear = new SimpleDateFormat("MM/dd/yy"); // US date format
		
		Set<Integer> idList = new TreeSet<Integer>();
		Map<Integer, Journey> allJourneyMap = new TreeMap<Integer, Journey>();
		
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
		for (int i = 0; i < allJourneyMap.size(); i += 100) {
			System.out.println(allJourneyMap.get(idList.toArray()[i]));
		}
		
		
		/*
		
		for(Integer nextId : idList) {
			for(int i = 1; i < assessmentCsvMap.size(); i++) {
				if(nextAssessmentRow.get(0).equals("id"))
					continue;
				if( Integer.parseInt(nextAssessmentRow.get(0)) == nextId ) {
					try {
						journeyList.get(i).updateAssessment(sdf.parse(nextAssessmentRow.get(1)), sdf.parse(nextAssessmentRow.get(2)));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}/*
			for(List<String> nextCinRow : cinCsvMap) {
				if(nextCinRow.get(0).equals("id"))
					continue;
				if( Integer.parseInt(nextCinRow.get(0)) == nextId ) {
					try {
						journeyList.get(i).updateCIN(sdf.parse(nextCinRow.get(1)), sdf.parse(nextCinRow.get(2)));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			for(List<String> nextS47Row : s47CsvMap) {
				if(nextS47Row.get(0).equals("id"))
					continue;
				if( Integer.parseInt(nextS47Row.get(0)) == nextId ) {
					try {
						journeyList.get(i).updateS47(sdf.parse(nextS47Row.get(1)), sdf.parse(nextS47Row.get(2)));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			for(List<String> nextCppRow : cppCsvMap) {
				if(nextCppRow.get(0).equals("id"))
					continue;
				if( Integer.parseInt(nextCppRow.get(0)) == nextId ) {
					try {
						journeyList.get(i).updateCpp(sdf.parse(nextCppRow.get(1)), sdf.parse(nextCppRow.get(2)));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			for(List<String> nextLacRow : lacCsvMap) {
				if(nextLacRow.get(0).equals("id"))
					continue;
				if( Integer.parseInt(nextLacRow.get(0)) == nextId ) {
					try {
						journeyList.get(i).updateLac(sdf.parse(nextLacRow.get(1)), sdf.parse(nextLacRow.get(2)));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			i++;
		}
		
		new ChartJourney(journeyList);*/
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
		
		for(char ch : chars) {
			if(ch == ',') {
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
	
	
}