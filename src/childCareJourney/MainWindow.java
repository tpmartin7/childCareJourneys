package childCareJourney;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.data.category.DefaultCategoryDataset;

public class MainWindow {
	
	private CSVReader csvReader;

	private JFrame frame;

	private Map<Integer, Journey> allJourneyMap;

	private Map<Integer, TreeMap<Date, Referral>> allReferralMap;

	private JTextPane consoleTextPane;
	
	private JourneySelector journeySelector;
	private JPanel bottomPanel;
	private JPanel leftSelectionPanel;
	private JLabel leftCurrentSelection;
	private DefaultListModel<Integer> currentSelectionModel;
	private JFreeChart leftChart;
	private JFreeChart rightChart;
	private ChartPanel leftChartPanel;
	private ChartPanel rightChartPanel;
	private ChartPanel rightChartPanel_1;
	private JPanel rightSelectionPanel;
	private JLabel rightSelectionLabel;
	private JComboBox leftAgeComboBox;
	private JComboBox leftRaceComboBox;
	private JComboBox leftWardComboBox;
	private JComboBox leftIndividualComboBox;
	
	private Set<Integer> selectedIDs;

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
		getInitialData();
		selectedIDs = journeySelector.startWithReferral();
		initialize();
		allJourneyMap.remove(20494842);
		for(Integer nextID : selectedIDs)
			currentSelectionModel.addElement(nextID);
		updateConsole("Total journeys found : " + allJourneyMap.size());
		updateConsole("Selecting journeys that begin with a referral : " + selectedIDs.size() + " total.");
		updateConsole("Average journey length : " + journeySelector.getAverageJourneyLength(selectedIDs));
		updateConsole("Last date in any journey : " + journeySelector.dateOfLastEvent());
		updateConsole("First date in any journey : " + journeySelector.dateOfFirstEvent(selectedIDs));
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(10, 10, 1360, 720);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JPanel displayPanel = new JPanel();
		GridBagConstraints gbc_displayPanel = new GridBagConstraints();
		gbc_displayPanel.gridwidth = 2;
		gbc_displayPanel.weighty = 1.0;
		gbc_displayPanel.insets = new Insets(0, 0, 5, 0);
		gbc_displayPanel.fill = GridBagConstraints.BOTH;
		gbc_displayPanel.gridx = 0;
		gbc_displayPanel.gridy = 0;
		frame.getContentPane().add(displayPanel, gbc_displayPanel);
		
		leftChart = ChartFactory.createLineChart("Journey Line Chart", "Days", "Status", new DefaultCategoryDataset(), PlotOrientation.VERTICAL, true, true, false);
		GridBagLayout gbl_displayPanel = new GridBagLayout();
		gbl_displayPanel.columnWidths = new int[]{292, 680, 0};
		gbl_displayPanel.rowHeights = new int[]{420, 0, 0};
		gbl_displayPanel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_displayPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		displayPanel.setLayout(gbl_displayPanel);
		leftChartPanel = new ChartPanel(leftChart);
		GridBagConstraints gbc_leftChartPanel = new GridBagConstraints();
		gbc_leftChartPanel.insets = new Insets(0, 0, 0, 0);
		gbc_leftChartPanel.fill = GridBagConstraints.BOTH;
		gbc_leftChartPanel.anchor = GridBagConstraints.CENTER;
		gbc_leftChartPanel.gridx = 0;
		gbc_leftChartPanel.gridy = 0;
		displayPanel.add(leftChartPanel, gbc_leftChartPanel);
		
		rightChartPanel_1 = new ChartPanel(leftChart);
		GridBagConstraints gbc_rightChartPanel_1 = new GridBagConstraints();
		gbc_rightChartPanel_1.insets = new Insets(0, 0, 0, 0);
		gbc_rightChartPanel_1.fill = GridBagConstraints.BOTH;
		gbc_rightChartPanel_1.anchor = GridBagConstraints.CENTER;
		gbc_rightChartPanel_1.gridx = 1;
		gbc_rightChartPanel_1.gridy = 0;
		displayPanel.add(rightChartPanel_1, gbc_rightChartPanel_1);
		
		bottomPanel = new JPanel();
		GridBagConstraints gbc_bottomPanel = new GridBagConstraints();
		gbc_bottomPanel.anchor = GridBagConstraints.SOUTH;
		gbc_bottomPanel.fill = GridBagConstraints.BOTH;
		gbc_bottomPanel.gridx = 0;
		gbc_bottomPanel.gridy = 2;
		frame.getContentPane().add(bottomPanel, gbc_bottomPanel);
		GridBagLayout gbl_bottomPanel = new GridBagLayout();
		gbl_bottomPanel.columnWidths = new int[]{557, 150, 0};
		gbl_bottomPanel.rowHeights = new int[]{32, 0, 0};
		gbl_bottomPanel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_bottomPanel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		bottomPanel.setLayout(gbl_bottomPanel);
		
		leftSelectionPanel = new JPanel();
		GridBagConstraints gbc_leftSelectionPanel = new GridBagConstraints();
		gbc_leftSelectionPanel.insets = new Insets(0, 0, 5, 5);
		gbc_leftSelectionPanel.fill = GridBagConstraints.VERTICAL;
		gbc_leftSelectionPanel.gridx = 0;
		gbc_leftSelectionPanel.gridy = 0;
		bottomPanel.add(leftSelectionPanel, gbc_leftSelectionPanel);
		GridBagLayout gbl_leftSelectionPanel = new GridBagLayout();
		gbl_leftSelectionPanel.columnWidths = new int[]{0, 0, 0};
		gbl_leftSelectionPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_leftSelectionPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_leftSelectionPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		leftSelectionPanel.setLayout(gbl_leftSelectionPanel);
		
		leftCurrentSelection = new JLabel("Left Display");
		leftCurrentSelection.setFont(new Font("Tahoma", Font.BOLD, 14));
		leftCurrentSelection.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_leftCurrentSelection = new GridBagConstraints();
		gbc_leftCurrentSelection.insets = new Insets(0, 0, 5, 5);
		gbc_leftCurrentSelection.anchor = GridBagConstraints.EAST;
		gbc_leftCurrentSelection.ipady = 50;
		gbc_leftCurrentSelection.fill = GridBagConstraints.VERTICAL;
		gbc_leftCurrentSelection.gridx = 0;
		gbc_leftCurrentSelection.gridy = 0;
		leftSelectionPanel.add(leftCurrentSelection, gbc_leftCurrentSelection);
		
		Set<String> individuals = new LinkedHashSet<String>();
		individuals.add("Individual IDs");
		for(Integer nextID : selectedIDs) {
			individuals.add(nextID.toString());
		}
		
		String[] individualComboOptions = new String[individuals.size()];
		individuals.toArray(individualComboOptions);
		
		leftIndividualComboBox = new JComboBox(individualComboOptions);
		leftIndividualComboBox.setEditable(true);
		GridBagConstraints gbc_leftIndividualComboBox = new GridBagConstraints();
		gbc_leftIndividualComboBox.insets = new Insets(2, 0, 2, 0);
		gbc_leftIndividualComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_leftIndividualComboBox.gridx = 0;
		gbc_leftIndividualComboBox.gridy = 4;
		leftSelectionPanel.add(leftIndividualComboBox, gbc_leftIndividualComboBox);
		leftIndividualComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("comboBoxChanged")) {
					String newSelectionStr = leftIndividualComboBox.getSelectedItem().toString();
					Integer[] selectedArray = new Integer[selectedIDs.size()];
					selectedIDs.toArray(selectedArray);
					Integer newSelectionInt = selectedArray[0];
					try {
						newSelectionInt = Integer.parseInt(newSelectionStr);
					} catch (NumberFormatException f) {
						System.err.println(f);
					}
					leftChart = ChartFactory.createLineChart("Journey for " + newSelectionStr, "", "", allJourneyMap.get(newSelectionInt).getDatasetForIndividualGraph(), PlotOrientation.VERTICAL, false, false, false);
					displayPanel.remove(leftChartPanel);
					leftChartPanel = new ChartPanel(leftChart);
					displayPanel.add(leftChartPanel, gbc_leftChartPanel);
					revalidate();
					repaint();
				}
				
			}
		});
		
		Set<String> ages = new LinkedHashSet<String>();
		ages.add("Age at referral");
		for(int i=0; i<18; i++) {
			ages.add(Integer.toString(i));
		}
		
		String[] ageComboBoxOptions = new String[ages.size()];
		ages.toArray(ageComboBoxOptions);
		
		leftAgeComboBox = new JComboBox(ageComboBoxOptions);
		
		GridBagConstraints gbc_leftAgeComboBox = new GridBagConstraints();
		gbc_leftAgeComboBox.insets = new Insets(2, 0, 2, 0);
		gbc_leftAgeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_leftAgeComboBox.gridx = 0;
		gbc_leftAgeComboBox.gridy = 1;
		leftSelectionPanel.add(leftAgeComboBox, gbc_leftAgeComboBox);
		
		Set<String> races = new LinkedHashSet<String>();
		races.add("Ethnicity");
		for(EEthnicity nextEthnicity : EEthnicity.values()) {
			races.add(nextEthnicity.toString());
		}
		
		String[] raceComboBoxOptions = new String[races.size()];
		races.toArray(raceComboBoxOptions);
		
		leftRaceComboBox = new JComboBox(raceComboBoxOptions);
		GridBagConstraints gbc_leftRaceComboBox = new GridBagConstraints();
		gbc_leftRaceComboBox.insets = new Insets(2, 0, 2, 0);
		gbc_leftRaceComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_leftRaceComboBox.gridx = 0;
		gbc_leftRaceComboBox.gridy = 2;
		leftSelectionPanel.add(leftRaceComboBox, gbc_leftRaceComboBox);
		
		Set<String> wards = new LinkedHashSet<String>();
		wards.add("Ward");
		for(EWard nextWard : EWard.values()) {
			wards.add(nextWard.toString());
		}
		
		String[] wardComboBoxOptions = new String[wards.size()];
		wards.toArray(wardComboBoxOptions);
		
		leftWardComboBox = new JComboBox(wardComboBoxOptions);
		GridBagConstraints gbc_leftWardComboBox = new GridBagConstraints();
		gbc_leftWardComboBox.insets = new Insets(2, 0, 2, 0);
		gbc_leftWardComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_leftWardComboBox.gridx = 0;
		gbc_leftWardComboBox.gridy = 3;
		leftSelectionPanel.add(leftWardComboBox, gbc_leftWardComboBox);
		
		currentSelectionModel = new DefaultListModel<>();
		
		this.consoleTextPane = new JTextPane();
		GridBagConstraints gbc_consoleTextPane = new GridBagConstraints();
		gbc_consoleTextPane.gridwidth = 2;
		gbc_consoleTextPane.weighty = 1.0;
		gbc_consoleTextPane.weightx = 1.0;
		gbc_consoleTextPane.insets = new Insets(0, 0, 5, 0);
		gbc_consoleTextPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_consoleTextPane.gridx = 2;
		gbc_consoleTextPane.gridy = 0;
		bottomPanel.add(consoleTextPane, gbc_consoleTextPane);
		consoleTextPane.setFont(new Font("Consolas", Font.PLAIN, 10));
		
		rightSelectionPanel = new JPanel();
		GridBagConstraints gbc_rightSelectionPanel = new GridBagConstraints();
		gbc_rightSelectionPanel.insets = new Insets(0, 0, 0, 5);
		gbc_rightSelectionPanel.fill = GridBagConstraints.BOTH;
		gbc_rightSelectionPanel.gridx = 1;
		gbc_rightSelectionPanel.gridy = 0;
		bottomPanel.add(rightSelectionPanel, gbc_rightSelectionPanel);
		GridBagLayout gbl_rightSelectionPanel = new GridBagLayout();
		gbl_rightSelectionPanel.columnWidths = new int[]{0, 0};
		gbl_rightSelectionPanel.rowHeights = new int[]{0, 0};
		gbl_rightSelectionPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_rightSelectionPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		rightSelectionPanel.setLayout(gbl_rightSelectionPanel);
		
		rightSelectionLabel = new JLabel("Right Display");
		GridBagConstraints gbc_rightSelectionLabel = new GridBagConstraints();
		gbc_rightSelectionLabel.gridx = 0;
		gbc_rightSelectionLabel.gridy = 0;
		rightSelectionPanel.add(rightSelectionLabel, gbc_rightSelectionLabel);
	}
	
	public void getInitialData() {
		this.csvReader = new CSVReader();
		this.allJourneyMap = csvReader.getAllJourneyMap();
		this.allReferralMap = csvReader.getReferrals();
		this.journeySelector = new JourneySelector(allJourneyMap);
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
	
	private void repaint() {
		this.frame.repaint();
	}
	
	private void revalidate() {
		this.frame.revalidate();
	}
}
