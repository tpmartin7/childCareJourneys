package childCareJourney;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JTextPane;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.awt.Font;

public class MainWindow {
	
	private CSVReader csvReader;

	private JFrame frame;

	private Map<Integer, Journey> allJourneyMap;

	private Map<Integer, TreeMap<Date, Referral>> allReferralMap;

	private JTextPane consoleTextPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		this.csvReader = new CSVReader();
		getInitialData();
		initialize();
		
		Set<Integer> selectedIDs = selectJourneysThatStartWithReferral();
		updateConsole("Total journeys found : " + allJourneyMap.size());
		updateConsole("Selecting journeys that begin with a referral : " + selectedIDs.size() + " total.");
		updateConsole("Average journey length : " + getAverageJourneyLength(selectedIDs));
		printLACsummary(selectedIDs);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JPanel topPanel = new JPanel();
		GridBagConstraints gbc_topPanel = new GridBagConstraints();
		gbc_topPanel.insets = new Insets(0, 0, 5, 0);
		gbc_topPanel.fill = GridBagConstraints.BOTH;
		gbc_topPanel.gridx = 0;
		gbc_topPanel.gridy = 0;
		frame.getContentPane().add(topPanel, gbc_topPanel);
		
		JPanel displayPanel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		frame.getContentPane().add(displayPanel, gbc_panel);
		
		this.consoleTextPane = new JTextPane();
		consoleTextPane.setFont(new Font("Consolas", Font.PLAIN, 10));
		GridBagConstraints gbc_consoleTextPane = new GridBagConstraints();
		gbc_consoleTextPane.fill = GridBagConstraints.BOTH;
		gbc_consoleTextPane.gridx = 0;
		gbc_consoleTextPane.gridy = 2;
		frame.getContentPane().add(consoleTextPane, gbc_consoleTextPane);
	}
	
	public void getInitialData() {
		this.allJourneyMap = csvReader.getAllJourneyMap();
		this.allReferralMap = csvReader.getReferrals();
	}
	
	private void updateConsole(String newText) {
		this.consoleTextPane.setText(this.consoleTextPane.getText() + newText + "\n");
	}
	
	public void printLACsummary(Set<Integer> selectedIDs) {
		Set<Integer> targetJourneyIDs = selectJourneysThatStartWithReferral();

		
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
					Date startDate = null, 
							endDate;
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
		updateConsole("Total number of complete LAC (start + end) : " + lacCount);
		updateConsole("Average LAC length : " + (totalLength / lacCount) + " days.");
	}
	
	public Set<Integer> selectJourneysThatStartWithReferral(){
		Set<Integer> result = new HashSet<Integer>();
		for(Integer nextID : allJourneyMap.keySet()) {
			Journey nextJourney = allJourneyMap.get(nextID);
			Map<Date, EStatus> journeyMap = nextJourney.getMap();
			Date[] keySet = new Date[journeyMap.size()];
			journeyMap.keySet().toArray(keySet);
			Date firstDate = keySet[0];
			EStatus firstStatus = journeyMap.get(firstDate);
			if(firstStatus == EStatus.REFERRAL)
				result.add(nextID);
		}
		return result;
	}
	
	private long getAverageJourneyLength(Set<Integer> selectedIDs) {
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
			//Test order
			for(Date nextDate : dateArray) {
				if(nextDate.after(dateArray[dateArray.length-1])){
					System.out.println("Dates not in order.");
				}
			}
			long length = TimeUnit.DAYS.convert((journeyEnd.getTime() - journeyStart.getTime()), TimeUnit.MILLISECONDS);
			totalDays += length;
		}
		return totalDays / count;
	}
}
