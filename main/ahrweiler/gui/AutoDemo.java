package ahrweiler.gui;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrAL;
import ahrweiler.util.AhrDate;
import ahrweiler.support.FCI;
import ahrweiler.support.RCode;
import ahrweiler.bgm.ANN;
import ahrweiler.bgm.BGM_Manager;
import ahrweiler.bgm.AttributesSK;
import javax.swing.*;
import java.lang.Thread;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Button;
import java.awt.event.*;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


public class AutoDemo {

	private int minimumX = 400;
	private int preferredX = 600;
	private int maximumX = 800;
	private int fillItr = 0;

	public AutoDemo(){
		runDemo();
	}

	public void runDemo(){
		//lists and over-arching data
		int totSamplings = 5;	//# of random samplings to create a benchmark
		ImageIcon iiPic = new ImageIcon("./../resources/cool.png");


		//layout
		int xdim = 600;
		int ydim = 700;
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("STAG3 Demonstration");
		frame.setSize(xdim, ydim);
		JPanel pMain = new JPanel();
		pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
		pMain.setOpaque(false);
		JScrollPane spMain = new JScrollPane(pMain, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
											JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		//init components
		HashMap<String, JComponent> jcTracker = new HashMap<String, JComponent>();
		JButton bAcronymRef = new JButton("Acronym Reference Table");
		JButton bEditInputs = new JButton("Edit Input Params");
		JButton bStartDemo = new JButton("Start Demo");
		JButton bTestComps = new JButton("Test Components");
		JTextPane tpDesc = new JTextPane();
		tpDesc.setText("This demonstration will show the effectiveness of using an Artificial "+
						"Neural Network (ANN) on stock data to help predict short-term price fluctuations "+
						"in individual stock tickers. Below are the steps needed to calcualate the performance"+
						" of the ANN algorithm, the data will be filled out below the corresponding step when"+
						" the demo is ran.");
		JTextPane tpStep1 = new JTextPane();
		tpStep1.setText("Step 1 : Create a set of random samples to show innate market performance before any"+
						" machine learning algorithm is applied"); 
		JTextPane tpStep2 = new JTextPane();
		tpStep2.setText("Step 2 : Apply ANN algorithm to all dates in which the market price is below the 3-day"+
						" SMA of the market (MS Mask = xxxxxx0x).");
		JLabel lbProgressSK1 = new JLabel("Placeholder");
		JProgressBar pbProgressSK1 = new JProgressBar();
		JTextPane tpStep3 = new JTextPane();
		tpStep3.setText("Step 3 : Apply ANN algorithm to all dates in which the market price is above the 3-day"+
						" SMA of the market (MS Mask = xxxxxx1x).");
		JLabel lbProgressSK2 = new JLabel("Placeholder");
		JProgressBar pbProgressSK2 = new JProgressBar();
		JTextPane tpStep4 = new JTextPane();
		tpStep4.setText("Step 4 : Coelesce the results from steps 2 and 3 to create aggregate keys, one short"+
						" and one long, that will work in any market state condition.");
		JLabel lbProgressAK1 = new JLabel("Placeholder");
		JProgressBar pbProgressAK1 = new JProgressBar();
		JTextPane tpNoteDatasets = new JTextPane();
		tpNoteDatasets.setText("NOTE : Each key is ran over two datasets, the dataset the machine learning algorithm"+
						" is trained on, and the one it is tested against to see its effectiveness. Step 5 and the"+
						" results will be using the test dataset exclusively."); 
		JTextPane tpStep5 = new JTextPane();
		tpStep5.setText("Step 5 : The APAPT can be optimized farther by simulating real world trading conditions"+
						" and calculating the best buy-in multiplier (BIM) and sell-out multiplier (SOM) of the"+
						" trades. The multiplier would be in regards to the stocks last price.");
		JLabel lbProgressBSO = new JLabel("Placeholder");
		JProgressBar pbProgressBSO = new JProgressBar();
		JLabel lbShortHeatmap = new JLabel("Placeholder");
		JLabel lbShortHeatmapPic = new JLabel();
		JLabel lbLongHeatmap = new JLabel("Placeholder");
		JLabel lbLongHeatmapPic = new JLabel();
		//JLabel lbRndHeatmap = new JLabel("Placeholder");
		//JLabel lbRndHeatmapPic = new JLabel();
		JTextPane tpResults = new JTextPane();
		tpResults.setText("Final Results: Now that we have two comprehensive trading strategies, one for short calls"+
						", and one for long calls, plots comparing the two alongside the random sampling can be shown.");
		JLabel lbResultsBW = new JLabel();
		JLabel lbResultsProfits = new JLabel();
				


		//JTextArea taSK1 = new JTextArea("Create a Single Key (SK) with a market state xxxxxx0x", 2, 20);
		//JTextArea taSK2 = new JTextArea("Create a Single Key (SK) with a market state xxxxxx1x", 2, 20);
		//JLabel lbPic = new JLabel();
		//JLabel lbBottom = new JLabel("Bottom Label");

		jcTracker.put("pMain", pMain);
		jcTracker.put("spMain", spMain);
		jcTracker.put("bAcronymRef", bAcronymRef);
		jcTracker.put("bEditInputs", bEditInputs);
		jcTracker.put("bStartDemo", bStartDemo);
		jcTracker.put("bTestComps", bTestComps);
		jcTracker.put("tpDesc", tpDesc);
		jcTracker.put("tpStep1", tpStep1);
		jcTracker.put("tpStep2", tpStep2);
		jcTracker.put("tpStep3", tpStep3);
		jcTracker.put("tpStep4", tpStep4);
		jcTracker.put("tpStep5", tpStep5);

		//---------- init tables -----------
		//DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		//centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		int tableRowHeight = 17;
		//create random sample (benchmark) table and scrollpane
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
		JTable tBenchmark = new JTable(benchmarkData, benchmarkHeader);
		centerCols(tBenchmark);
		tBenchmark.getColumnModel().getColumn(0).setPreferredWidth(25);
		tBenchmark.setRowHeight(tableRowHeight);
		tBenchmark.setOpaque(false);
		JScrollPane spBenchmark = new JScrollPane(tBenchmark, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//create ANN xxxxxx0x key creation table and scrollpane
		String[][] skData = new String[4][5];
		String[] skHeader = new String[]{"SK ID", "Call", "Dataset", "APAPT", "Pos %"};
		skData[0] = new String[]{"", "Short", "Train", "", ""};
		skData[1] = new String[]{"", "Short", "Test", "", ""};
		skData[2] = new String[]{"", "Long", "Train", "", ""};
		skData[3] = new String[]{"", "Long", "Test", "", ""};
		DefaultTableModel dtmSK1 = new DefaultTableModel(skData, skHeader);
		JTable tSK1 = new JTable(dtmSK1);
		centerCols(tSK1);
		tSK1.setRowHeight(tableRowHeight);
		JScrollPane spSK1 = new JScrollPane(tSK1, JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//create ANN xxxxxx1x key creation table and scrollpane
		DefaultTableModel dtmSK2 = new DefaultTableModel(skData, skHeader);
		JTable tSK2 = new JTable(dtmSK2);
		centerCols(tSK2);
		tSK2.setRowHeight(tableRowHeight);
		JScrollPane spSK2 = new JScrollPane(tSK2, JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//create basic AK table and scrollpane
		String[][] akData = new String[5][5];
		String[] akHeader = new String[]{"AK ID", "Call", "Dataset", "APAPT", "Pos %"};
		akData[0] = new String[]{"", "Short", "Train", "", ""};
		akData[1] = new String[]{"", "Short", "Test", "", ""};
		akData[2] = new String[]{"", "Long", "Train", "", ""};
		akData[3] = new String[]{"", "Long", "Test", "", ""};
		DefaultTableModel dtmAK1 = new DefaultTableModel(akData, akHeader);
		JTable tAK1 = new JTable(dtmAK1);
		centerCols(tAK1);
		tAK1.setRowHeight(tableRowHeight);
		JScrollPane spAK1 = new JScrollPane(tAK1, JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//create BSO AK table and scrollpane


			
		jcTracker.put("tBenchmark", tBenchmark);
		jcTracker.put("spBenchmark", spBenchmark);
		jcTracker.put("tSK1", tSK1);
		jcTracker.put("spSK1", spSK1);
		jcTracker.put("tSK2", tSK2);
		jcTracker.put("spSK2", spSK2);


		//component placement
		//pMain.setMinimumSize(new Dimension(this.preferredX-40, 500));
		//pMain.setPreferredSize(new Dimension(this.preferredX-40, 500));
		//pMain.setMaximumSize(new Dimension(this.maximumX, 5000));		
		//spMain.setMinimumSize(new Dimension(xdim, ydim));
		//spMain.setPreferredSize(new Dimension(xdim, ydim));
		//spMain.setMaximumSize(new Dimension(900, 5000));		
		bAcronymRef.setAlignmentX(Component.CENTER_ALIGNMENT);
		bEditInputs.setAlignmentX(Component.CENTER_ALIGNMENT);
		bStartDemo.setAlignmentX(Component.CENTER_ALIGNMENT);
		bTestComps.setAlignmentX(Component.CENTER_ALIGNMENT);
		int tBenchmarkHeight = ((totSamplings+1)*tableRowHeight)+22;
		int tSK1Height = ((4*tableRowHeight)+22);
		int tSK2Height = ((4*tableRowHeight)+22);
		int tAK1Height = ((4*tableRowHeight)+22);

		
		//basic functionality and aesthetic
		spMain.getViewport().setBackground(new Color(233, 225, 212));
		spMain.getVerticalScrollBar().setUnitIncrement(16);
		disguiseAndUnderlineTextPane(tpDesc, 0);
		disguiseAndUnderlineTextPane(tpStep1, 8);
		disguiseAndUnderlineTextPane(tpStep2, 8);
		disguiseAndUnderlineTextPane(tpStep3, 8);
		disguiseAndUnderlineTextPane(tpStep4, 8);
		disguiseAndUnderlineTextPane(tpNoteDatasets, 6);
		disguiseAndUnderlineTextPane(tpStep5, 8);
		//lbPic.setIcon(iiPic);

		//add everything
		pMain.add(compPlacer(bAcronymRef, false, 25, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(bEditInputs, false, 25, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(bStartDemo, false, 25, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(bTestComps, false, 25, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpDesc, false, 60, 1.00));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpStep1, false, 60, 1.00));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(spBenchmark, true, tBenchmarkHeight, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpStep2, false, 30, 1.00));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(lbProgressSK1, false, 20, 0.80));
		pMain.add(compPlacer(pbProgressSK1, false, 15, 0.80));
		pMain.add(compPlacer(spSK1, true, tSK1Height, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpStep3, false, 30, 1.00));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(lbProgressSK2, false, 20, 0.80));
		pMain.add(compPlacer(pbProgressSK2, false, 15, 0.80));
		pMain.add(compPlacer(spSK2, true, tSK2Height, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpStep4, false, 40, 1.00));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(lbProgressAK1, false, 20, 0.80));
		pMain.add(compPlacer(pbProgressAK1, false, 15, 0.80));
		pMain.add(compPlacer(spAK1, true, tAK1Height, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpNoteDatasets, false, 30, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpStep5, false, 60, 1.00));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(lbProgressBSO, true, 20, 0.80));
		pMain.add(compPlacer(pbProgressBSO, true, 15, 0.80));
		pMain.add(compPlacer(lbShortHeatmap, true, 20, 0.80));
		pMain.add(compPlacer(lbShortHeatmapPic, false, 500, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(lbLongHeatmap, true, 20, 0.80));
		pMain.add(compPlacer(lbLongHeatmapPic, false, 500, 0.80));


		//reset button as visible
		bAcronymRef.setVisible(true);
		bEditInputs.setVisible(true);
		bStartDemo.setVisible(true);
		bTestComps.setVisible(true);	
		tpDesc.setVisible(true);
		tpStep1.setVisible(true);
		tpStep2.setVisible(true);
		tpStep3.setVisible(true);
		tpStep4.setVisible(true);
		tpStep5.setVisible(true);

		//pMain.add(leftJustify(lbPic, 0));
		//pMain.add(leftJustify(lbBottom, 40));
		frame.add(spMain);
		frame.pack();
		frame.setVisible(true);
		//iiPic.getImage().flush();	

		//worker threads
		AttributesSK kattr = new AttributesSK();
		BimSomOptWorker bsoWork = new BimSomOptWorker(lbProgressBSO, pbProgressBSO, kattr);
		bsoWork.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e){
				if("progress".equals(e.getPropertyName())){
					int progress = Integer.parseInt(e.getNewValue().toString());
					if(progress >= 50){
						System.out.println("--> Show Short Heatmap here.");
					}
				}
				if("state".equals(e.getPropertyName())){
					if("DONE".equals(e.getNewValue().toString())){
						System.out.println("--> Show Long Heatmap here.\n--> Done btw :)");
					}
				}
			}
		});
		AggKeyWorker akWork = new AggKeyWorker(lbProgressAK1, pbProgressAK1, kattr);
		akWork.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e){
				if("state".equals(e.getPropertyName())){
					if("DONE".equals(e.getNewValue().toString())){	
						lbProgressAK1.setVisible(false);
						pbProgressAK1.setVisible(false);
						spAK1.setVisible(true);
						tpNoteDatasets.setVisible(true);
						fillTableAK(tAK1);
						lbProgressBSO.setVisible(true);
						pbProgressBSO.setVisible(true);
						bsoWork.execute();					
					}

				}
			}
		});
		kattr.setMsMask("xxxxxx1x");
		SingleKeyWorker skWork2 = new SingleKeyWorker(lbProgressSK2, pbProgressSK2, kattr, 2);
		skWork2.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e){
				//System.out.println("PCL Triggered : " + e.toString());
				if("state".equals(e.getPropertyName())){
					//System.out.println("state: " + e.getNewValue().toString());
					if("DONE".equals(e.getNewValue().toString())){
						lbProgressSK2.setVisible(false);
						pbProgressSK2.setVisible(false);
						spSK2.setVisible(true);
						fillTableSK(tSK2);
						lbProgressAK1.setVisible(true);
						pbProgressAK1.setVisible(true);
						akWork.execute();
					}
				}
			}
		});
		kattr.setMsMask("xxxxxx0x");
		SingleKeyWorker skWork1 = new SingleKeyWorker(lbProgressSK1, pbProgressSK1, kattr, 1);
		skWork1.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e){
				//System.out.println("PCL Triggered : " + e.toString());
				if("state".equals(e.getPropertyName())){
					//System.out.println("state: " + e.getNewValue().toString());
					if("DONE".equals(e.getNewValue().toString())){
						lbProgressSK1.setVisible(false);
						pbProgressSK1.setVisible(false);
						spSK1.setVisible(true);
						fillTableSK(tSK1);
						lbProgressSK2.setVisible(true);
						pbProgressSK2.setVisible(true);
						skWork2.execute();
					}
				}
			}
		});
		BenchmarkWorker bmWork = new BenchmarkWorker(tBenchmark, totSamplings);
		bmWork.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e){
				//System.out.println("PCL Triggered : " + e.toString());
				//do something upon completion of the thread
				if("state".equals(e.getPropertyName())){
					//System.out.println("state: " + e.getNewValue().toString());
					if("DONE".equals(e.getNewValue().toString())){
						lbProgressSK1.setVisible(true);
						pbProgressSK1.setVisible(true);
						skWork1.execute();
					}
				}
			}
		});

		//listener functionality
		bAcronymRef.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

			}
		});
		bEditInputs.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

			}
		});
		bStartDemo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				spBenchmark.setVisible(true);
				frame.revalidate();
				frame.repaint();
				bmWork.execute();
				System.out.println("--> Post bmWork thread.");
			}
		});
		bTestComps.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				printComps(jcTracker);
			}
		});

	}

	//----------------- GUI Helper Functions ------------------

	//places a JTable onto the GUI (needs strict height coords and needs indenting)
	private JComponent compPlacer(JComponent jcomp, boolean strict_height, int height, double indent){
		int minX = (int)(this.minimumX*indent);
		int prefX = (int)(this.preferredX*indent);
		int maxX = (int)(this.maximumX*indent);
		jcomp.setMinimumSize(new Dimension(minX, 20));
		jcomp.setPreferredSize(new Dimension(prefX, height));
		jcomp.setMaximumSize(new Dimension(maxX, 200));
		jcomp.setVisible(false);
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(jcomp);
		box.add(Box.createHorizontalGlue());
		return box;
	}
	//places a JTextPane onto the GUI
	private JComponent textPanePlacer(JTextPane tpane){
		tpane.setMaximumSize(new Dimension(1000, 1000));
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(tpane);
		box.add(Box.createHorizontalGlue());	
		return box;
	}

	//fill out given JTable w/ data from last AKs in ak_log.txt
	private void fillTableAK(JTable table){
		String laPath = "./../baseis/log/ak_log.txt";
		FCI fciLA = new FCI(true, laPath);
		ArrayList<ArrayList<String>> laFile = AhrIO.scanFile(laPath, ",");
		if(laFile.size() > 2){
			int lastShortRowIdx = laFile.size()-2;
			int lastLongRowIdx = laFile.size()-1;
			String sakID = laFile.get(lastShortRowIdx).get(fciLA.getIdx("ak_num"));
			String sTrainAPAPT = laFile.get(lastShortRowIdx).get(fciLA.getIdx("true_train_apapt"));
			String sTrainPosp = laFile.get(lastShortRowIdx).get(fciLA.getIdx("true_train_posp"));
			String sTestAPAPT = laFile.get(lastShortRowIdx).get(fciLA.getIdx("true_test_apapt"));
			String sTestPosp = laFile.get(lastShortRowIdx).get(fciLA.getIdx("true_test_posp"));
			String lakID = laFile.get(lastLongRowIdx).get(fciLA.getIdx("ak_num"));
			String lTrainAPAPT = laFile.get(lastLongRowIdx).get(fciLA.getIdx("true_train_apapt"));
			String lTrainPosp = laFile.get(lastLongRowIdx).get(fciLA.getIdx("true_train_posp"));
			String lTestAPAPT = laFile.get(lastLongRowIdx).get(fciLA.getIdx("true_test_apapt"));
			String lTestPosp = laFile.get(lastLongRowIdx).get(fciLA.getIdx("true_test_posp"));
			table.setValueAt(sakID, 0, 0);
			table.setValueAt(sTrainAPAPT, 0, 3);
			table.setValueAt(sTrainPosp, 0, 4);
			table.setValueAt(sakID, 1, 0);
			table.setValueAt(sTestAPAPT, 1, 3);
			table.setValueAt(sTestPosp, 1, 4);
			table.setValueAt(lakID, 2, 0);
			table.setValueAt(lTrainAPAPT, 2, 3);
			table.setValueAt(lTrainPosp, 2, 4);
			table.setValueAt(lakID, 3, 0);
			table.setValueAt(lTestAPAPT, 3, 3);
			table.setValueAt(lTestPosp, 3, 4);
		}else{
			System.out.println("ERR: not enough AKs in ak_log.txt");
		}
	}
	//fill out given JTable w/ data from last SKs in keys_perf.txt
	private void fillTableSK(JTable table){
		String kpPath = "./../out/ml/ann/keys_perf.txt";
		FCI fciKP = new FCI(true, kpPath);
		ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(kpPath, ",");
		if(kpFile.size() >= 3){
			int lastShortRowIdx = kpFile.size()-2;
			int lastLongRowIdx = kpFile.size()-1;
			String shortID = kpFile.get(lastShortRowIdx).get(fciKP.getIdx("sk_num"));
			String sTrainAPAPT = kpFile.get(lastShortRowIdx).get(fciKP.getIdx("true_train_apapt"));
			String sTrainPosp = kpFile.get(lastShortRowIdx).get(fciKP.getIdx("true_train_posp"));
			String sTestAPAPT = kpFile.get(lastShortRowIdx).get(fciKP.getIdx("true_test_apapt"));
			String sTestPosp = kpFile.get(lastShortRowIdx).get(fciKP.getIdx("true_test_posp"));
			String longID = kpFile.get(lastLongRowIdx).get(fciKP.getIdx("sk_num"));
			String lTrainAPAPT = kpFile.get(lastLongRowIdx).get(fciKP.getIdx("true_train_apapt"));
			String lTrainPosp = kpFile.get(lastLongRowIdx).get(fciKP.getIdx("true_train_posp"));
			String lTestAPAPT = kpFile.get(lastLongRowIdx).get(fciKP.getIdx("true_test_apapt"));
			String lTestPosp = kpFile.get(lastLongRowIdx).get(fciKP.getIdx("true_test_posp"));
			table.setValueAt(shortID, 0, 0);
			table.setValueAt(sTrainAPAPT, 0, 3);
			table.setValueAt(sTrainPosp, 0, 4);
			table.setValueAt(shortID, 1, 0);
			table.setValueAt(sTestAPAPT, 1, 3);
			table.setValueAt(sTestPosp, 1, 4);
			table.setValueAt(longID, 2, 0);
			table.setValueAt(lTrainAPAPT, 2, 3);
			table.setValueAt(lTrainPosp, 2, 4);
			table.setValueAt(longID, 3, 0);
			table.setValueAt(lTestAPAPT, 3, 3);
			table.setValueAt(lTestPosp, 3, 4);	
		}else{
			System.out.println("ERR: not enough SKs in ann/keys_perf.txt");
		}
	}

	//center all rows in a JTable
	private void centerCols(JTable tbl){
		int cols = tbl.getColumnCount();
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		for(int i = 0; i < cols; i++){
			tbl.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}
	}

	//add flowlayout to each section of GUI to left justify them
	private Component leftJustify(JComponent jcomp, int leftPadding){
		Box box = Box.createHorizontalBox();
		box.add(Box.createRigidArea(new Dimension(leftPadding, 0)));
		box.add(jcomp);
		box.add(Box.createHorizontalGlue());
		return box;
	}
	//use for a GUI comp on a single line to center justify it
	private Component centerJustify(JComponent jcomp){
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
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
	//make a textpane look and act like a label
	private void disguiseAndUnderlineTextPane(JTextPane jtp, int underlineLen){
		//jtp.setWrapStyleWord(true);
		//jtp.setLineWrap(true);
		jtp.setOpaque(false);
		jtp.setEditable(false);
		jtp.setFocusable(false);
		jtp.setBackground(UIManager.getColor("Label.background"));
		jtp.setFont(UIManager.getFont("Label.font"));
		jtp.setBorder(UIManager.getBorder("Label.border"));
		//set first part as underlined
		StyledDocument style = jtp.getStyledDocument();
		SimpleAttributeSet sas = new SimpleAttributeSet();
		StyleConstants.setUnderline(sas, true);
		style.setCharacterAttributes(0, underlineLen, sas, false);
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

}

//worker threads
class BenchmarkWorker extends SwingWorker<ArrayList<ArrayList<Double>>, ArrayList<Double>>{
	private JTable table;
	private int totSamplings;
	private final String kpPath;
	private FCI fciKP;
	BenchmarkWorker(JTable table, int totSamplings){
		this.table = table;
		this.totSamplings = totSamplings;
		this.kpPath = "./../out/ml/rnd/keys_perf.txt";
		fciKP = new FCI(true, kpPath);			
	}
	@Override
	protected ArrayList<ArrayList<Double>> doInBackground(){
		ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
		double avgAPAPT = 0.0;
		double avgPosp = 0.0;
		for(int i = 0; i < totSamplings; i++){ 
			if(!isCancelled()){
				//create rnd basis (using def params)
				String sdate = "2016-01-01";
				String edate = "2020-12-31";
				int spd = 10;
				int tvi = 4;
				String msMask = "xxxxxxxx";
				String narMask = "1111";
				BGM_Manager bgmm = new BGM_Manager();
				bgmm.genBasisRnd(sdate, edate, spd, tvi, msMask, narMask);
				//get rnd data back from file
				ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(kpPath, ",");
				double itrAPAPT = 0.0;
				double itrPosp = 0.0;
				try{
					itrAPAPT = Double.parseDouble(kpFile.get(kpFile.size()-1).get(fciKP.getIdx("apapt")));
					itrPosp = Double.parseDouble(kpFile.get(kpFile.size()-1).get(fciKP.getIdx("posp")));
				}catch(NumberFormatException ex){
					System.out.println("ERR: " + ex.getMessage());
				}
				//add data and publish
				ArrayList<Double> line = new ArrayList<Double>();
				line.add((double)i);
				line.add(itrAPAPT);
				line.add(itrPosp);
				data.add(line);
				publish(line);
				setProgress(i);
				//increment avg vals
				avgAPAPT += itrAPAPT;
				avgPosp += itrPosp;
			}
		}
		//calc and add avg vals
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
			ArrayList<Double> line = data.get(i);
			int row = line.get(0).intValue();
			table.setValueAt(String.format("%.5f",line.get(1)), row, 1);
			table.setValueAt(String.format("%.3f",line.get(2)), row, 2);
		}
	}
	@Override
    protected void done(){
		try{
			ArrayList<ArrayList<Double>> data = get();
			ArrayList<Double> line = data.get(data.size()-1);
			int row = line.get(0).intValue();
			table.setValueAt(String.format("%.5f",line.get(1)), row, 1);
			table.setValueAt(String.format("%.3f",line.get(2)), row, 2);
		}catch(Exception e){
			System.out.println("ERR: " + e.getMessage());
		}
	}
}

class SingleKeyWorker extends SwingWorker<Void, String>{
	private JLabel lb;
	private JProgressBar pb;
	private ANN ann;
	private AttributesSK kattr;
	private int itr;
	public SingleKeyWorker(JLabel lb, JProgressBar pb, AttributesSK kattr, int itr){
		this.lb = lb;
		this.itr = itr;
		this.kattr = kattr;
		this.ann = new ANN(this.kattr);
		//add property change listener to update progress bar
		this.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e){	
				if("progress".equalsIgnoreCase(e.getPropertyName())){
					pb.setValue(getProgress());
				}
			}
		});
	}
	@Override
	protected Void doInBackground() {
		System.out.println("======= In skWork"+this.itr+" doInBackground() =======");
		//create DB for ANN algo, init ANN for SK creation
		int secSize = 10000;
		double progress = 0;
		int progDB = 10;
		int progBasis = 10;
		publish("Creating Train Database ... ");
		this.ann.createTrainDB(secSize);
		progress += (double)progDB;
		setProgress((int)progress);
		publish("Creating Test Database ... ");
		this.ann.createTestDB(secSize);
		progress += (double)progDB;
		setProgress((int)progress);
		this.ann.initSK();

		//calc step value for progressbar
		int totSections = this.ann.getTrainFilesSize();
		double step = (100-(progDB+progDB+progBasis+progBasis)) / (double)totSections;
		//calc SK
		publish("Running ANN algorithm ... ");
		for(int i = 0; i < totSections; i++){
			if(!isCancelled()){
				//System.out.println("--> Section "+i+" out of "+totSections);
				this.ann.calcSKBySection(i);
				progress += step;
				setProgress((int)Math.round(progress));
				//publish("WTF");
			}else{
				System.out.println("Thread skWork"+this.itr+" is cancelled().");
			}
		}
		//save created SK info to file
		this.ann.writeToFileSK();
		//create basis file (calc actual predictions)
		publish("Calculating Short Predictions ... ");
		BGM_Manager shortSK = new BGM_Manager("ANN", this.ann.getID()+1);
		shortSK.genBasisSK(this.ann.getID()+1);
		String shortBasisPath = "./../baseis/single/ann/ANN_"+String.valueOf(this.ann.getID()+1)+".txt";
		ArrayList<String> shortPerf = shortSK.perfFromBasisFile(shortBasisPath);
		shortSK.perfToFileSK(shortPerf);	
		progress += (double)progBasis;
		setProgress((int)progress);	
		publish("Calculating Long Predictions ... ");
		BGM_Manager longSK = new BGM_Manager("ANN", this.ann.getID());
		longSK.genBasisSK(this.ann.getID());
		String longBasisPath = "./../baseis/single/ann/ANN_"+String.valueOf(this.ann.getID())+".txt";
		ArrayList<String> longPerf = longSK.perfFromBasisFile(longBasisPath);
		longSK.perfToFileSK(longPerf);
		progress += (double)progBasis;
		setProgress((int)progress);

		System.out.println("--> End of doInBackground()");
		return null;
	}
	@Override
	protected void process(List<String> desc){
		for(int i = 0; i < desc.size(); i++){
			lb.setText(desc.get(i));
		}
	}
	@Override
	protected void done(){

	}
}

class AggKeyWorker extends SwingWorker<Void, String>{
	private String ksPath;
	private FCI fciKS;
	private String laPath;
	private FCI fciLA;
	private JLabel lb;
	private JProgressBar pb;
	private AttributesSK kattr;
	public AggKeyWorker(JLabel lb, JProgressBar pb, AttributesSK kattr){
		this.ksPath = "./../out/ml/ann/keys_struct.txt";
		this.fciKS = new FCI(true, this.ksPath);
		this.laPath = "./../baseis/log/ak_log.txt";
		this.fciLA = new FCI(true, this.laPath);
		this.lb = lb;
		this.pb = pb;
		this.kattr = kattr;
		//add property change listener to update progress bar
		this.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e){	
				if("progress".equalsIgnoreCase(e.getPropertyName())){
					pb.setValue(getProgress());
				}
			}
		});
	}

	@Override
	protected Void doInBackground(){
		System.out.println("======= In akWork doInBackground() =======");
		publish("Writing Basic AK Info To File ...");
		ArrayList<ArrayList<String>> laFile = AhrIO.scanFile(this.laPath, ",");
		ArrayList<ArrayList<String>> ksFile = AhrIO.scanFile(this.ksPath, ",");
		//get new aggregate key numbers
		int sakID = -1;	//short AK ID
		int lakID = -1;	//long AK ID
		if(laFile.size() > 1){
			for(int i = 1; i < laFile.size(); i++){
				int itrID = Integer.parseInt(laFile.get(i).get(this.fciLA.getIdx("ak_num")));
				if(itrID > sakID){
					sakID = itrID;
				}
			}
		}
		sakID++;
		lakID = sakID + 1;
		String sakStr = String.valueOf(sakID);
		String lakStr = String.valueOf(lakID);
		//get single keys for each aggregate key
		String[] skeysShort = new String[2];
		String[] skeysLong = new String[2];
		if(ksFile.size() > 4){
			skeysShort[0] = ksFile.get(ksFile.size()-4).get(fciKS.getIdx("sk_num"));
			skeysShort[1] = ksFile.get(ksFile.size()-2).get(fciKS.getIdx("sk_num"));
			skeysLong[0] = ksFile.get(ksFile.size()-3).get(fciKS.getIdx("sk_num"));
			skeysLong[1] = ksFile.get(ksFile.size()-1).get(fciKS.getIdx("sk_num"));
		}else{
			System.out.println("ERR: keys_struct.txt too small.");
		}
		String skeysShortStr = skeysShort[0]+"~"+skeysShort[1];
		String skeysLongStr = skeysLong[0]+"~"+skeysLong[1];
		//write AK info to ak_log.txt
		ArrayList<String> akShortLine = new ArrayList<String>();
		akShortLine.add(sakStr);										//[0] ak_num
		akShortLine.add("ANN");											//[1] bgm
		akShortLine.add("IT");											//[2] db_used
		akShortLine.add(AhrDate.getTodaysDate());						//[3] date_ran
		akShortLine.add(kattr.getSDate());								//[4] start_date
		akShortLine.add(kattr.getEDate());								//[5] end_date
		akShortLine.add("0");											//[6] call
		akShortLine.add(String.valueOf(kattr.getSPD()));				//[7] spd
		akShortLine.add(String.valueOf(kattr.getTVI()));				//[8] tvi
		akShortLine.add(kattr.getIndMask());							//[9] ind_mask
		akShortLine.add(kattr.getNarMask());							//[10] nar_mask
		akShortLine.add(skeysShortStr);									//[11] best_keys
		akShortLine.add("ph");											//[12] sk_bso
		akShortLine.add("ph");											//[13] ak_bso
		akShortLine.add("ph");											//[14] bso_train_apapt
		akShortLine.add("ph");											//[15] bso_test_apapt
		akShortLine.add("ph");											//[16] bso_train_posp
		akShortLine.add("ph");											//[17] bso_test_posp
		akShortLine.add("ph");											//[18] true_train_apapt
		akShortLine.add("ph");											//[19] true_test_apapt
		akShortLine.add("ph");											//[20] true_train_posp
		akShortLine.add("ph");											//[21] true_test_posp
		laFile.add(akShortLine);
		ArrayList<String> akLongLine = new ArrayList<String>(akShortLine);
		akLongLine.set(fciLA.getIdx("ak_num"), lakStr);
		akLongLine.set(fciLA.getIdx("call"), "1");
		akLongLine.set(fciLA.getIdx("best_keys"), skeysLongStr);
		laFile.add(akLongLine);
		AhrIO.writeToFile(this.laPath, laFile, ",");
		System.out.println("--> akWork1 -> doInBackground() -> after writeToFile()");
		//create AK basis files (both short and long)
		setProgress(20);
		publish("Creating Short Aggregate Key (AK"+sakStr+") ...");
		BGM_Manager akShort = new BGM_Manager(sakID);
		akShort.genBasisAK();
		setProgress(40);
		publish("Calculating Performance For AK"+sakStr+" ...");
		String shortPath = "./../baseis/aggregated/ann/ANN_"+sakStr+".txt";
		ArrayList<String> shortPerf = akShort.perfFromBasisFile(shortPath);
		akShort.perfToFileAK(shortPerf);
		setProgress(60);
		publish("Creating Long Aggregate Key (AK"+lakStr+") ...");
		BGM_Manager akLong = new BGM_Manager(lakID);
		akLong.genBasisAK();
		setProgress(80);
		publish("Calculating Performance For AK"+lakStr+" ...");
		String longPath = "./../baseis/aggregated/ann/ANN_"+lakStr+".txt";
		ArrayList<String> longPerf = akLong.perfFromBasisFile(longPath);
		akLong.perfToFileAK(longPerf);
		setProgress(100);	

		System.out.println("--> End of doInBackground()");
		return null;
	}
	@Override
	protected void process(List<String> desc){
		for(int i = 0; i < desc.size(); i++){
			lb.setText(desc.get(i));
		}
	}
	@Override
	protected void done(){

	}


}

class BimSomOptWorker extends SwingWorker<Void, String>{
	private String laPath;
	private FCI fciLA;
	private JLabel lb;
	private JProgressBar pb;
	private AttributesSK kattr;
	public BimSomOptWorker(JLabel lb, JProgressBar pb, AttributesSK kattr){
		this.laPath = "./../baseis/log/ak_log.txt";
		this.fciLA = new FCI(true, this.laPath);
		this.lb = lb;
		this.pb = pb;
		this.kattr = kattr;
		//add property change listener to update progress bar
		this.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e){	
				if("progress".equalsIgnoreCase(e.getPropertyName())){
					pb.setValue(getProgress());
				}
			}
		});	
	}

	@Override
	protected Void doInBackground(){
		int progress = 0;
		int startProgress = 8;
		int stepProgress = 23;
		//get short AK and long AK
		publish("Retrieving AK Information ... ");
		ArrayList<ArrayList<String>> laFile = AhrIO.scanFile(this.laPath, ",");
		int shortID = -1;
		int longID = -1;
		if(laFile.size() >= 3){
			shortID = Integer.parseInt(laFile.get(laFile.size()-2).get(fciLA.getIdx("ak_num")));
			longID = Integer.parseInt(laFile.get(laFile.size()-1).get(fciLA.getIdx("ak_num")));
		}else{
			System.out.println("ERR: Not enough AKs in ak_log.txt");
		}
		progress += startProgress;
		setProgress(progress);
		//calc BSO and plot heatmap for short AK
		String sakName = "AK"+String.valueOf(shortID);
		publish("Calculating BIM/SOM Optimization for "+sakName+" (short calls) ...");
		BGM_Manager akShort = new BGM_Manager(shortID);
		ArrayList<String> bsoShort = akShort.bsoMultiple(kattr.getSDate(), kattr.getEDate(), "010", true, false);
		String akBsoShort = bsoShort.get(0)+"|"+bsoShort.get(1);
		laFile.get(laFile.size()-2).set(fciLA.getIdx("ak_bso"), akBsoShort);
		progress += stepProgress;
		setProgress(progress);
		publish("Run R Program to Plot Heatmap for "+sakName+" (short calls) ...");
		RCode rcHeat = new RCode();
		rcHeat.setXLabel("Buy-In Multiple");
		rcHeat.setYLabel("Sell-Out Multiple");
		rcHeat.setTitle("Heatmap of "+sakName+" YoY %s For Each BIM/SOM Combination");
		rcHeat.createHeatmap("./../data/r/rdata/demo_short_heat.csv", "./../resources/demo_short_heat.png", 500, 500);
		rcHeat.writeCode("./../data/r/rscripts/demo_short_heat.R");
		rcHeat.runScript("./../data/r/rscripts/demo_short_heat.R");
		progress += stepProgress;
		setProgress(progress);
		//calc BSO and plot heatmap for long AK
		String lakName = "AK"+String.valueOf(longID);
		publish("Calculating BIM/SOM Optimization for "+lakName+" (long calls) ...");
		BGM_Manager akLong = new BGM_Manager(longID);
		ArrayList<String> bsoLong = akLong.bsoMultiple(kattr.getSDate(), kattr.getEDate(), "010", true, false);
		String akBsoLong = bsoLong.get(0)+"|"+bsoLong.get(1);
		laFile.get(laFile.size()-1).set(fciLA.getIdx("ak_bso"), akBsoLong);
		AhrIO.writeToFile(this.laPath, laFile, ",");
		progress += stepProgress;
		setProgress(progress);
		publish("Run R Program to Plot Heatmap for "+lakName+" (long calls) ...");
		rcHeat = new RCode();
		rcHeat.setXLabel("Buy-In Multiple");
		rcHeat.setYLabel("Sell-Out Multiple");
		rcHeat.setTitle("Heatmap of "+lakName+" YoY %s For Each BIM/SOM Combination");
		rcHeat.createHeatmap("./../data/r/rdata/demo_long_heat.csv", "./../resources/demo_long_heat.png", 500, 500);
		rcHeat.writeCode("./../data/r/rscripts/demo_long_heat.R");
		rcHeat.runScript("./../data/r/rscripts/demo_long_heat.R");
		progress += stepProgress;
		setProgress(progress);
		//calc BSO and plot heatmap for RND?? i dont think so
		

		System.out.println("--> End of doInBackground()");
		return null;
	}
	@Override
	protected void process(List<String> desc){
		for(int i = 0; i < desc.size(); i++){
			lb.setText(desc.get(i));
		}
	}
	@Override
	protected void done(){

	}


}

