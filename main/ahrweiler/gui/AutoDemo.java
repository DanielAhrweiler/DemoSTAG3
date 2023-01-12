package ahrweiler.gui;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrAL;
import ahrweiler.support.FCI;
import ahrweiler.bgm.BGM_Manager;
import javax.swing.*;
import java.lang.Thread;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Button;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


public class AutoDemo {

	//worker threads
	private class BenchmarkWorker extends SwingWorker<ArrayList<ArrayList<Double>>, ArrayList<Double>>{
		BenchmarkWorker(){
			String kpPath = "./../out/ml/rnd/keys_perf.txt";
			FCI fciKP = new FCI(true, kpPath);			
		}
		@Override
		protected ArrayList<ArrayList<Double>> doInBackground(){
			ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
			double avgAPAPT = 0.0;
			double avgPosp = 0.0;
			for(int i = 0; i < totSamplings; i++){ 
				if(!isCancelled()){
					rndSample();
					ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(kpPath, ",");
					double itrAPAPT = 0.0;
					double itrPosp = 0.0;
					try{
						itrAPAPT = Double.parseDouble(kpFile.get(kpFile.size()-1).get(fciKP.getIdx("apapt")));
						itrPosp = Double.parseDouble(kpFile.get(kpFile.size()-1).get(fciKP.getIdx("posp")));
					}catch(NumberFormatException ex){
						System.out.println("ERR: " + ex.getMessage());
					}
					ArrayList<Double> line = new ArrayList<Double>();
					vals.add((double)i);
					vals.add(itrAPAPT);
					vals.add(itrPosp);
					publish(line);
					setProgress(i);
					data.add(line);
					avgAPAPT += itrAPAPT;
					avgPosp += itrPosp;
				}
			}
			avgAPAPT = avgAPAPT / (double)totSamplings;
			avgPosp = avgPosp / (double)totSamplings;
			ArrayList<Double> lastLine = new ArrayList<Double>();
			lastLine.add((double)totSamplings);
			lastLine.add(avgAPAPT);
			lastLine.add(avgPosp);
			data.add(lastLine);
			return data;
		}
		@Override
		protected void process(List<ArrayList<Double>> data){
			for(int i = 0; i < data.size(); i++){
				ArrayList<String> line = data.get(i);
				int row = (int)line.get(0);
				tBenchmark.setValueAt(String.format("%.5f",line.get(1)), row, 1);
				tBenchmark.setValueAt(String.format("%.3f",line.get(2)), row, 2);
			}
		}
	}


	public AutoDemo(){
		runDemo();
	}

	public void runDemo(){
		System.out.println("--> In AutoDemo is EDT : " + SwingUtilities.isEventDispatchThread());
		//lists and over-arching data
		int totSamplings = 5;	//# of random samplings to create a benchmark


		//layout
		int xdim = 700;
		int ydim = 700;
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("STAG3 Demonstration");
		frame.setSize(xdim, ydim);
		JPanel pMain = new JPanel();
		pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
		JScrollPane spMain = new JScrollPane(pMain, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
											JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		//init components
		HashMap<String, JComponent> jcTracker = new HashMap<String, JComponent>();
		//ArrayList<JComponent> jcTracker = new ArrayList<JComponent>();
		JButton bAcronymRef = new JButton("Acronym Reference Table");
		JButton bEditInputs = new JButton("Edit Input Params");
		JButton bStartDemo = new JButton("Start Demo");
		JButton bTestComps = new JButton("Test Components");
		JTextArea taStart = new JTextArea("This demonstration will show the effectiveness of using an Artificial "+
								"Neural Network (ANN) on stock data to help predict short-term price fluctuations "+
								"in individual stock tickers.", 3, 20);
		JTextArea taBenchmark = new JTextArea("Before using the ANN, however, we must set a benchmark to compare "+
								"against. A benchmark will be created by random sampling.", 3, 20); 
		JTextArea taSK1 = new JTextArea("Create a Single Key (SK) with a market state xxxxxxx0x", 2, 20);
		JTextArea taSK2 = new JTextArea("Create a Single Key (SK) with a market state xxxxxxx1x", 2, 20);
		ImageIcon iiPic = new ImageIcon("./../resources/cool.png");
		JLabel lbPic = new JLabel();
		JLabel lbBottom = new JLabel("Bottom Label");
		JCheckBox cbTest = new JCheckBox("Test Box", false);


		jcTracker.put("pMain", pMain);
		jcTracker.put("spMain", spMain);
		jcTracker.put("bAcronymRef", bAcronymRef);
		jcTracker.put("bEditInputs", bEditInputs);
		jcTracker.put("bStartDemo", bStartDemo);
		jcTracker.put("bTestComps", bTestComps);
		jcTracker.put("taStart", taStart);
		jcTracker.put("taBenchmark", taBenchmark);
		jcTracker.put("taSK1", taSK1);
		jcTracker.put("taSK2", taSK2);
		jcTracker.put("lbPic", lbPic);
		jcTracker.put("lbBottom", lbBottom);

		//init tables
		//create headers for random sample table
		String[][] benchmarkData = new String[totSamplings+1][3];
		String[] benchmarkHeader = new String[]{"Sample", "APAPT", "Pos %"};
		for(int i = 0; i < totSamplings; i++){
			benchmarkData[i][0] = String.valueOf(i+1);
			benchmarkData[i][1] = "";
			benchmarkData[i][2] = "";
		}
		benchmarkData[totSamplings][0] = "Avg";
		benchmarkData[totSamplings][1] = "";
		benchmarkData[totSamplings][2] = "";
		//create random sample table and scrollpane
		JTable tBenchmark = new JTable(benchmarkData, benchmarkHeader);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		tBenchmark.getColumnModel().getColumn(0).setPreferredWidth(25);
		tBenchmark.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		tBenchmark.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		tBenchmark.setRowHeight(17);
		JScrollPane spBenchmark = new JScrollPane(tBenchmark, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jcTracker.put("tBenchmark", tBenchmark);
		jcTracker.put("spBenchmark", spBenchmark);


		//component placement
		//pMain.setPreferredSize(new Dimension(xdim-40, 500));
		//pMain.setMaximumSize(new Dimension(900, 2000));		
		spMain.setPreferredSize(new Dimension(xdim, ydim));
		spMain.setMaximumSize(new Dimension(900, 5000));		
		spMain.getVerticalScrollBar().setUnitIncrement(16);
		bAcronymRef.setMinimumSize(new Dimension(30, 35));
		bAcronymRef.setPreferredSize(new Dimension(300, 35));
		bAcronymRef.setMaximumSize(new Dimension(700, 35));
		bEditInputs.setMinimumSize(new Dimension(30, 35));
		bEditInputs.setPreferredSize(new Dimension(300, 35));
		bEditInputs.setMaximumSize(new Dimension(700, 35));
		bStartDemo.setMinimumSize(new Dimension(30, 35));
		bStartDemo.setPreferredSize(new Dimension(300, 35));
		bStartDemo.setMaximumSize(new Dimension(700, 35));
		bTestComps.setMinimumSize(new Dimension(30, 35));
		bTestComps.setPreferredSize(new Dimension(300, 35));
		bTestComps.setMaximumSize(new Dimension(700, 35));
		taStart.setMaximumSize(new Dimension(700, 50));
		taBenchmark.setMaximumSize(new Dimension(700, 50));
		int tableHeight = ((totSamplings+1)*17)+22;
		spBenchmark.setMinimumSize(new Dimension(100, tableHeight));
		spBenchmark.setPreferredSize(new Dimension(400, tableHeight));
		spBenchmark.setMaximumSize(new Dimension(700, tableHeight));

		

		//basic functionality
		disguiseTextAreaAsLabel(taStart);
		disguiseTextAreaAsLabel(taBenchmark);
		disguiseTextAreaAsLabel(taSK1);
		disguiseTextAreaAsLabel(taSK2);
		lbPic.setIcon(iiPic);

		//add everything
		pMain.add(bAcronymRef);
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(bEditInputs);
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(bStartDemo);
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(bTestComps);
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(cbTest);
		pMain.add(leftJustify(taStart, 10));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(leftJustify(taBenchmark, 10));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(leftJustify(spBenchmark, 40));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(taSK1);
		pMain.add(Box.createRigidArea(new Dimension(0, 30)));
		pMain.add(taSK2);
		pMain.add(Box.createRigidArea(new Dimension(0, 30)));
		pMain.add(leftJustify(lbPic, 0));
		pMain.add(leftJustify(lbBottom, 40));
		frame.add(spMain);
		frame.pack();
		frame.setVisible(true);
		iiPic.getImage().flush();	

		//worker threads
		BenchmarkWorker bmWork = new BenchmarkWorker();
		bmWork.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e){
				Integer row = (Integer)e.getNewValue();
				if("progress".equals(e.getPropertyName())){
					System.out.println("PN" + row +" "+ e.getPropertyName());
				}else{
					System.out.println("Property Name = " +e.getPropertyName());
				}
			}
		});

		//listener functionality
		cbTest.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				System.out.println("--> cbTest is selected : " + cbTest.isSelected());
			}
		});
		bAcronymRef.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

			}
		});
		bEditInputs.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

			}
		});
		/*
		bStartDemo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.out.println("*** Start bStartDemo 2 ***");



				tBenchmark.setValueAt(String.format("%.5f", avgAPAPT), totSamplings, 1);
				tBenchmark.setValueAt(String.format("%.3f", avgPosp), totSamplings, 2);
				System.out.println("*** End bStartDemo 2 ***");
			}
		});*/
		bStartDemo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.out.println("*** Start bStartDemo 1 ***");
				cbTest.setSelected(true);
				bmWork.execute();
				System.out.println("--> In bStartDemo is EDT : " + SwingUtilities.isEventDispatchThread());
				//refreshGUI(frame);
				System.out.println("*** End bStartDemo 1 ***");
			}
		});
		bTestComps.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printComps(jcTracker);
			}
		});



	}

	//----------------- GUI Helper Functions ------------------

	//refresh GUI
	private void refreshGUI(JFrame frame){
		//frame.pack();
		System.out.println("--> In refreshGUI is EDT : " + SwingUtilities.isEventDispatchThread());
		frame.revalidate();
		frame.repaint();
		frame.setVisible(true);
	}

	//add flowlayout to each section of GUI to left justify them
	private Component leftJustify(JComponent jcomp, int leftPadding){
		Box box = Box.createHorizontalBox();
		box.add(Box.createRigidArea(new Dimension(leftPadding, 0)));
		box.add(jcomp);
		box.add(Box.createHorizontalGlue());
		return box;
	}

	//make a textarea look and act like a label
	private void disguiseTextAreaAsLabel(JTextArea jta){
		jta.setWrapStyleWord(true);
		jta.setLineWrap(true);
		jta.setOpaque(false);
		jta.setEditable(false);
		jta.setFocusable(false);
		jta.setBackground(UIManager.getColor("Label.background"));
		jta.setFont(UIManager.getFont("Label.font"));
		jta.setBorder(UIManager.getBorder("Label.border"));
	}

	//print component positions (testing)
	private void printComps(HashMap<String, JComponent> comps){
		System.out.println("========== Comp List Start ==========");
		for(String compName : comps.keySet()){
			JComponent itrComp = comps.get(compName);
			System.out.println(compName+"\n--> [x,y]  = ["+itrComp.getX()+", "+itrComp.getY()+"]"+
										"\n--> [w,h]  = ["+itrComp.getWidth()+", "+itrComp.getHeight()+"]"+
										"\n--> is vis = "+itrComp.isVisible());
		}
		System.out.println("========== Comp List End ==========");
	}

	/*---------------------------------------------------------------------
		Functional Steps In Demo
	----------------------------------------------------------------------*/

	//create 1 random sample with hard coded params
	private void rndSample(){
		String sdate = "2016-01-01";
		String edate = "2020-12-31";
		int spd = 10;
		int tvi = 4;
		String msMask = "xxxxxxxx";
		String narMask = "1111";
		BGM_Manager bgmm = new BGM_Manager();
		bgmm.genBasisRnd(sdate, edate, spd, tvi, msMask, narMask);
	}

	
	

}
