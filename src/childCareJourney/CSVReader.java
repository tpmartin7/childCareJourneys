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
import java.util.TreeMap;

public class CSVReader {

	public static void main(String[] args) {
		String defaultPath = "data\\";
		if (args.length > 0) {
			defaultPath = args[0];
		}
		
		HashMap<Integer, TreeMap<Date, Referral>> referrals = new HashMap<Integer, TreeMap<Date, Referral>>();
		
		List<List<String>> 	referralCsvMap = scanCsv(Paths.get(defaultPath + "Referrals1.csv"));
							/*assessmentCsvMap = scanCsv(Paths.get(defaultPath + "sampleAssessment.csv")),
							cinCsvMap = scanCsv(Paths.get(defaultPath + "sampleCin.csv")),
							cppCsvMap = scanCsv(Paths.get(defaultPath + "sampleCpp.csv")),
							lacCsvMap = scanCsv(Paths.get(defaultPath + "sampleLac.csv")),
							s47CsvMap = scanCsv(Paths.get(defaultPath + "sampleS47.csv"));*/
		
		
		SimpleDateFormat usaDateFormat2digitYear = new SimpleDateFormat("MM/dd/yy"); // US date format
		
		//List<Integer> idList = new ArrayList<>();
		//List<Journey> journeyList = new ArrayList<>();
		
		for(int i = 1; i < referralCsvMap.size(); i++) {
			List<String> nextRow = referralCsvMap.get(i);
			Integer id;
			try{ 
				id = Integer.parseInt(nextRow.get(0));
				//idList.add(id);
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
				
				//Detect ward here
				
				Date date = usaDateFormat2digitYear.parse(nextRow.get(5));
				if(referrals.containsKey(id)) {
					referrals.get(id).put(date, new Referral(id, age, gender, ethnicity, EWard.ALEXANDRA, date));
				}
				else {
					TreeMap<Date, Referral> newMap = new TreeMap<Date, Referral>();
					newMap.put(date, new Referral(id, age, gender, ethnicity, EWard.ALEXANDRA, date));
					referrals.put(id, newMap);
				}
				//journeyList.add(new Journey(id,sdf.parse(nextRow.get(5))));
			} catch (ParseException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
		
		System.out.println(referrals.size() + " individuals added.");
		System.out.println(referralCsvMap.size() - 1 + " total referrals.");
		
		/*
		referralCsvMap = scanCsv(Paths.get(defaultPath + "Referrals2.csv"));
		for(List<String> nextRow : referralCsvMap) {
			Integer id;
			try{ 
				id = Integer.parseInt(nextRow.get(0));
				idList.add(id);
			}
			catch(NumberFormatException e){ 
				continue; 
			}
			try {
				journeyList.add(new Journey(id,sdf.parse(nextRow.get(5))));
			} catch (ParseException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
		System.out.println(journeyList.size() + " records added");
/*
		int i = 0;
		
		for(Integer nextId : idList) {
			for(List<String> nextAssessmentRow : assessmentCsvMap) {
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