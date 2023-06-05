import ahrweiler.util.AhrAL;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrDate;
import ahrweiler.util.AhrDTF;
import ahrweiler.util.AhrGen;
import ahrweiler.Globals;
import ahrweiler.support.FCI;
import ahrweiler.support.RCode;
import ahrweiler.support.SQLCode;
import ahrweiler.support.OrderSim;
import ahrweiler.support.StockFilter;
import ahrweiler.bgm.ANN;
import ahrweiler.bgm.BGM_Manager;
import ahrweiler.bgm.AttributesSK;
import ahrweiler.gui.DB_Charting;
import ahrweiler.gui.DB_Filter;
import ahrweiler.gui.DB_DataIntegrity;
import ahrweiler.gui.ML_CreateSK;
import ahrweiler.gui.ML_CreateAK;
import ahrweiler.gui.ML_Basis;
import ahrweiler.gui.PA_BimSomOpt;
import ahrweiler.gui.PA_KeyPerf;
import ahrweiler.gui.AutoDemo;
import ahrweiler.gui.AD_Params;
import ahrweiler.gui.AD_Acronyms;
import ahrweiler.gui.TableSortPanel;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.channels.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.*;

public class STAG3 {

	public static void main(String[] args){
		STAG3 stag3 = new STAG3();
		//stag3.tempCode();
		stag3.init();
		stag3.mainGUI();
	}

	public void tempCode(){
		//test scanSelectRows
		String skbPath = AhrIO.uniPath("./../out/sk/baseis/ann/ANN_0.txt");
		FCI fciSKB = new FCI(false, AhrIO.uniPath("./../out/sk/baseis/"));
		ArrayList<ArrayList<String>> ssRows = AhrIO.scanSelectRows(skbPath, ",", "2016-02-02", fciSKB.getIdx("date"));
		System.out.println("--> ssRows");
		AhrAL.print(ssRows);

		System.out.println("--> TempCode ... DONE");
	}

	public void init(){
		if(File.separator.equals("/")){
			System.out.println("Running in Linux");
		}else if(File.separator.equals("\\")){
			System.out.println("Running in Windows");
		}else{
			System.out.println("Running in unknown");
		}
		System.out.print("--> Initializing Data ... ");
		//remove lines from rnd keys_struct & keys_perf
		String ksPath = AhrIO.uniPath("./../out/sk/log/rnd/keys_struct.txt");
		String kpPath = AhrIO.uniPath("./../out/sk/log/rnd/keys_perf.txt");
		ArrayList<String> ksRow = AhrIO.scanRow(ksPath, ",", 0);
		ArrayList<String> kpRow = AhrIO.scanRow(kpPath, ",", 0);
		ArrayList<ArrayList<String>> ksFile = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> kpFile = new ArrayList<ArrayList<String>>();
		ksFile.add(ksRow);
		kpFile.add(kpRow);
		AhrIO.writeToFile(ksPath, ksFile, ",");
		AhrIO.writeToFile(kpPath, kpFile, ",");
		//remove rnd tmp basis files
		String rbPath = AhrIO.uniPath("./../out/sk/baseis/rnd/");
		ArrayList<String> rndFiles = AhrIO.getFilesInPath(rbPath);
		for(int i = 0; i < rndFiles.size(); i++){
			File file = new File(rbPath+rndFiles.get(i));
			if(file.exists()){
				file.delete();
			}
		}
		//set sk_attrs.txt to default values
		AttributesSK kattr = new AttributesSK();
		kattr.saveToFile(AhrIO.uniPath("./../data/tmp/sk_attrs.txt"));
		System.out.println("DONE");
	}

	public void mainGUI(){
		//component initialization
		int xdim = 275;
		int ydim = 600;
		if(File.separator.equals("\\")){
			xdim += 15;
		}
		JFrame mframe = new JFrame();
		mframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mframe.setTitle("STAG 3");
		mframe.setSize(xdim, ydim);
		mframe.setLocationRelativeTo(null);
		mframe.setLayout(null);
		JLabel lbDB = new JLabel("Database");
		JButton bCharting = new JButton("Stock & Indicator Charting");
		JButton bFilter = new JButton("Stock Filter");
		JLabel lbML = new JLabel("Machine Learning");
		JButton bCreateSK = new JButton("Create SK");
		JButton bCreateAK = new JButton("Create AK");
		JButton bBasis = new JButton("SK, AK, & Basis Info");
		JLabel lbPA = new JLabel("Post Analysis");
		JButton bBuyInOpt = new JButton("BIM/SOM Optimization");
		JButton bKeyPerf = new JButton("Key Performance");
		JButton bAutoDemo = new JButton("Run Automated Demo");
		JButton bAcronyms = new JButton("Acronym Ref Table");
		JButton bResetData = new JButton("Reset Data");

		//component bounds
		lbDB.setBounds(10, 10, 160, 35);
		bCharting.setBounds(40, 45, 200, 35);
		bFilter.setBounds(40, 85, 200, 35);
		lbML.setBounds(10, 125, 160, 35);
		bCreateSK.setBounds(40, 160, 200, 35);
		bCreateAK.setBounds(40, 200, 200, 35);
		bBasis.setBounds(40, 240, 200, 35);
		lbPA.setBounds(10, 280, 160, 35);
		bBuyInOpt.setBounds(40, 315, 200, 35);
		bKeyPerf.setBounds(40, 355, 200, 35);
		bAutoDemo.setBounds(10, 405, 250, 40);
		bAcronyms.setBounds(10, 455, 250, 40);
		bResetData.setBounds(10, 505, 250, 40);
	
		//change look of the buttons
		setButtonStyle(bCharting);
		setButtonStyle(bFilter);
		setButtonStyle(bCreateSK);
		setButtonStyle(bCreateAK);
		setButtonStyle(bBasis);
		setButtonStyle(bBuyInOpt);
		setButtonStyle(bKeyPerf);
		setButtonStyle(bAutoDemo);
		setButtonStyle(bAcronyms);
		setButtonStyle(bResetData);

		//component funtionality

		bCharting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				DB_Charting dbchart = new DB_Charting();
			}
		});
		bFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				DB_Filter dbfilter = new DB_Filter();
			}
		});
		bCreateSK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ML_CreateSK mlsk = new ML_CreateSK();
			}
		});
		bCreateAK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ML_CreateAK mlak = new ML_CreateAK();
			}
		});
		bBasis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ML_Basis mlbasis = new ML_Basis();
			}
		});
		bBuyInOpt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				PA_BimSomOpt pabim = new PA_BimSomOpt();
			}
		});
		bKeyPerf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				PA_KeyPerf paperf = new PA_KeyPerf();
			}
		});
		bAutoDemo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				AutoDemo demo = new AutoDemo();
				//TestSwing tswing = new TestSwing();
			}
		});
		bAcronyms.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				AD_Acronyms ada = new AD_Acronyms();
			}
		});
		bResetData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				resetData(mframe);	
			}
		});
		
		mframe.add(lbDB);
		mframe.add(bCharting);
		mframe.add(bFilter);
		mframe.add(lbML);
		mframe.add(bCreateSK);
		mframe.add(bCreateAK);
		mframe.add(bBasis);
		mframe.add(lbPA);
		mframe.add(bBuyInOpt);
		mframe.add(bKeyPerf);
		mframe.add(bAutoDemo);
		mframe.add(bAcronyms);
		mframe.add(bResetData);
		mframe.setVisible(true);
	}

	//GUI related, set specific style for a JButton
	public void setButtonStyle(JButton btn){
		Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}
	
	//reset all SK, AK, basis information
	public void resetData(JFrame frame){
		String message = "This will delete all SK data, AK data, baseis, filters, R scripts"+
						", and R plot images.\nAre you sure you want to continue?";
		if(JOptionPane.showConfirmDialog(frame, message, "WARNING", JOptionPane.YES_NO_OPTION) == 
																	JOptionPane.YES_OPTION){ 
			System.out.print("--> Reseting Data ... ");		
			ArrayList<ArrayList<String>> blank = new ArrayList<ArrayList<String>>();
			//delete filters
			String ftPath = AhrIO.uniPath("./../data/filters/");
			AhrIO.deleteFilesInPath(ftPath);
			//delete ML custom DBs
			String cdbPath1 = AhrIO.uniPath("./../data/ml/ann/cust/test/");
			String cdbPath2 = AhrIO.uniPath("./../data/ml/ann/cust/train/");
			AhrIO.deleteFilesInPath(cdbPath1);
			AhrIO.deleteFilesInPath(cdbPath2);
			String dbsPath = AhrIO.uniPath("./../data/ml/ann/cust/db_sizes.txt");
			AhrIO.writeToFile(dbsPath, blank, ",");
			//delete R data
			String rdataPath = AhrIO.uniPath("./../data/r/rdata/");
			AhrIO.deleteFilesInPath(rdataPath);
			//delete R scripts
			String rscriptPath = AhrIO.uniPath("./../data/r/rscripts/");
			AhrIO.deleteFilesInPath(rscriptPath);
			//delete tmp files
			String tmpPath = AhrIO.uniPath("./../data/tmp/");
			AhrIO.deleteFilesInPath(tmpPath);
			//delete AK basis files
			String akbPath = AhrIO.uniPath("./../out/ak/baseis/ann/");
			AhrIO.deleteFilesInPath(akbPath);
			//refresh ak_log (keep 1st line)
			String alPath = AhrIO.uniPath("./../out/ak/log/ak_log.txt");
			ArrayList<ArrayList<String>> alFile = AhrIO.scanFile(alPath, ",");
			ArrayList<ArrayList<String>> alFileNew = new ArrayList<ArrayList<String>>();
			alFileNew.add(alFile.get(0));
			AhrIO.writeToFile(alPath, alFileNew, ",");
			//delete SK basis files
			String skbPath = AhrIO.uniPath("./../out/sk/baseis/ann/");
			AhrIO.deleteFilesInPath(skbPath);
			//delete SK error files
			String skePath = AhrIO.uniPath("./../out/sk/log/ann/error/");
			AhrIO.deleteFilesInPath(skePath);
			//delete SK struct files
			String sksPath = AhrIO.uniPath("./../out/sk/log/ann/structure/");
			AhrIO.deleteFilesInPath(sksPath);
			//delete keys_perf (keep 1st line)
			String kpPath = AhrIO.uniPath("./../out/sk/log/ann/keys_perf.txt");
			ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(kpPath, ",");
			ArrayList<ArrayList<String>> kpFileNew = new ArrayList<ArrayList<String>>();
			kpFileNew.add(kpFile.get(0));
			AhrIO.writeToFile(kpPath, kpFileNew, ",");
			//delete keys_struct (keep 1st line)
			String ksPath = AhrIO.uniPath("./../out/sk/log/ann/keys_struct.txt");
			ArrayList<ArrayList<String>> ksFile = AhrIO.scanFile(ksPath, ",");
			ArrayList<ArrayList<String>> ksFileNew = new ArrayList<ArrayList<String>>();
			ksFileNew.add(ksFile.get(0));
			AhrIO.writeToFile(ksPath, ksFileNew, ",");
			//delete contents of score_percentile
			String spPath = AhrIO.uniPath("./../out/score_percentiles.txt");
			AhrIO.writeToFile(spPath, blank, ",");
			//delete resources files
			String resPath = AhrIO.uniPath("./../resources/");
			AhrIO.deleteFilesInPath(resPath);

			System.out.println("DONE");
		}
	}


}
