package ahrweiler.gui;
import ahrweiler.Globals;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrAL;
import ahrweiler.util.AhrDate;
import ahrweiler.util.AhrDTF;
import ahrweiler.support.FCI;
import ahrweiler.support.RCode;
import ahrweiler.support.OrderSim;
import ahrweiler.bgm.ANN;
import ahrweiler.bgm.BGM_Manager;
import ahrweiler.bgm.AttributesSK;
import ahrweiler.gui.AD_Params;
import ahrweiler.gui.AD_Acronyms;
import ahrweiler.gui.TableSortPanel;
import javax.swing.*;
import java.lang.Thread;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Button;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


//worker threads
class BenchmarkWorker extends SwingWorker<ArrayList<ArrayList<Double>>, ArrayList<Double>>{
	private JTable table;
	private int totSamplings;
	private final String kpPath;
	private FCI fciKP;
	BenchmarkWorker(JTable table, int totSamplings){
		this.table = table;
		this.totSamplings = totSamplings;
		this.kpPath = "./../out/sk/log/rnd/keys_perf.txt";
		fciKP = new FCI(true, kpPath);			
	}
	@Override
	protected ArrayList<ArrayList<Double>> doInBackground(){
		ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
		double avgAPAPT = 0.0;
		double avgPosp = 0.0;
		for(int i = 0; i < totSamplings; i++){ 
			if(!isCancelled()){
				AttributesSK kattr = new AttributesSK("./../data/tmp/sk_attrs.txt");
				kattr.setBGM("rnd");
				//create rnd basis (using saved key params)
				BGM_Manager bgmm = new BGM_Manager(kattr);
				bgmm.genBasisRnd(0.75);
				//get rnd data back from file
				ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(kpPath, ",");
				double itrAPAPT = 0.0;
				double itrPosp = 0.0;
				try{
					itrAPAPT = Double.parseDouble(kpFile.get(kpFile.size()-1).get(fciKP.getIdx("true_apapt")));
					itrPosp = Double.parseDouble(kpFile.get(kpFile.size()-1).get(fciKP.getIdx("true_posp")));
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
	private String msMask;
	private int itr;
	public SingleKeyWorker(JLabel lb, JProgressBar pb, String msMask, int itr){
		this.lb = lb;
		this.msMask = msMask;
		this.itr = itr;
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
		double progress = 0;
		setProgress((int)progress);
		int progDB = 10;
		int progBasis = 10;	
		//create attr set and ANN instance
		AttributesSK kattr = new AttributesSK("./../data/tmp/sk_attrs.txt");
		kattr.setMsMask(msMask);
		ANN ann = new ANN(kattr);
		//create DB for ANN algo, init ANN for SK creation
		int secSize = 10000;
		ann.deleteCustDB();
		publish("Creating Train Database ... ");
		if(Globals.uses_mysql_source){
			ann.createTrainDBFromWeb(secSize);
		}else{
			ann.createTrainDBFromLocal(secSize);
		}
		progress += (double)progDB;
		setProgress((int)progress);
		publish("Creating Test Database ... ");
		if(Globals.uses_mysql_source){
			ann.createTestDBFromWeb(secSize);
		}else{
			ann.createTestDBFromLocal(secSize);
		}	
		progress += (double)progDB;
		setProgress((int)progress);
		ann.initSK();

		//calc step value for progressbar
		int totSections = ann.getTrainFilesSize();
		int loopNum = 2;//num of times to loop thru entire train DB
		double step = (100-(progDB+progDB+progBasis+progBasis)) / ((double)totSections * loopNum);
		//calc SK
		publish("Running ANN algorithm ... ");
		System.out.println("Total Sections : " + totSections);
		for(int i = 0; i < (totSections*loopNum); i++){
			if(!isCancelled()){
				System.out.print(i+",");
				ann.calcSKBySection(i%totSections);
				progress += step;
				setProgress((int)Math.round(progress));
			}else{
				System.out.println("Thread skWork"+this.itr+" is cancelled().");
			}
		}
		System.out.println("DONE");
		//save created SK info to file
		ann.writeToFileSK();
		//create basis file (calc actual predictions)
		publish("Calculating Short Predictions ... ");
		BGM_Manager shortSK = new BGM_Manager("ANN", ann.getID());
		shortSK.genBasisSK(ann.getID());
		String shortBasisPath = "./../out/sk/baseis/ann/ANN_"+String.valueOf(ann.getID())+".txt";
		ArrayList<String> shortPerf = shortSK.perfFromBasisFile(shortBasisPath);
		shortSK.perfToFileSK(shortPerf);	
		progress += (double)progBasis;
		setProgress((int)progress);	
		publish("Calculating Long Predictions ... ");
		BGM_Manager longSK = new BGM_Manager("ANN", ann.getID()+1);
		longSK.genBasisSK(ann.getID()+1);
		String longBasisPath = "./../out/sk/baseis/ann/ANN_"+String.valueOf(ann.getID()+1)+".txt";
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
	public AggKeyWorker(JLabel lb, JProgressBar pb){
		this.ksPath = "./../out/sk/log/ann/keys_struct.txt";
		this.fciKS = new FCI(true, this.ksPath);
		this.laPath = "./../out/ak/log/ak_log.txt";
		this.fciLA = new FCI(true, this.laPath);
		this.lb = lb;
		this.pb = pb;
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
		setProgress(0);
		publish("Writing Basic AK Info To File ...");
		ArrayList<ArrayList<String>> laFile = AhrIO.scanFile(this.laPath, ",");
		ArrayList<ArrayList<String>> ksFile = AhrIO.scanFile(this.ksPath, ",");
		AttributesSK kattr = new AttributesSK("./../data/tmp/sk_attrs.txt");
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
		//write long and short AK info to ak_log.txt
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
		String shortPath = "./../out/ak/baseis/ann/ANN_"+sakStr+".txt";
		ArrayList<String> shortPerf = akShort.perfFromBasisFile(shortPath);
		akShort.perfToFileAK(shortPerf);
		setProgress(60);
		publish("Creating Long Aggregate Key (AK"+lakStr+") ...");
		BGM_Manager akLong = new BGM_Manager(lakID);
		akLong.genBasisAK();
		setProgress(80);
		publish("Calculating Performance For AK"+lakStr+" ...");
		String longPath = "./../out/ak/baseis/ann/ANN_"+lakStr+".txt";
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
	private int picDimX;
	private int picDimY;
	private int totSamplings;
	public BimSomOptWorker(JLabel lb, JProgressBar pb, int dimX, int dimY, int totSamplings){
		this.laPath = "./../out/ak/log/ak_log.txt";
		this.fciLA = new FCI(true, this.laPath);
		this.lb = lb;
		this.pb = pb;
		this.picDimX = dimX;
		this.picDimY = dimY;
		this.totSamplings = totSamplings;
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
		String bmPath = "./../data/tmp/bso_multiple.txt";
		FCI fciBM = new FCI(false, bmPath);
		int progress = 0;
		setProgress(progress);
		int[] progressSteps = new int[]{8, 28, 28, 28, 8};
		//get short AK and long AK
		publish("Retrieving AK Information ... ");
		ArrayList<ArrayList<String>> laFile = AhrIO.scanFile(this.laPath, ",");
		int shortID = -1;
		int longID = -1;
		if(laFile.size() >= 3){
			shortID = Integer.parseInt(laFile.get(laFile.size()-2).get(fciLA.getIdx("ak_num")));
			longID = Integer.parseInt(laFile.get(laFile.size()-1).get(fciLA.getIdx("ak_num")));
		}else{
			System.out.println("ERR: not enough AKs in ak_log.txt");
		}
		progress += progressSteps[0];
		setProgress(progress);
		//===== short AK =====
		String sakName = "AK"+String.valueOf(shortID);
		String shortDataPath = "./../data/r/rdata/demo_short_heat.csv";
		String shortPlotPath = "./../resources/demo_short_heat.png";
		String shortScriptPath = "./../data/r/rscripts/demo_short_heat.R";
		//[1] calc BSO
		publish("Calculating BIM/SOM Optimization for "+sakName+" (short calls) ...");
		BGM_Manager akShort = new BGM_Manager(shortID);
		OrderSim osimShort = new OrderSim(shortID);
		osimShort.setTtvMask("010");
		osimShort.calcBSO();
		ArrayList<ArrayList<String>> orderlist = AhrIO.scanFile("./../data/tmp/os_orderlist.txt", ",");
		ArrayList<String> firstLine = new ArrayList<String>();
		firstLine.add(String.format("%.2f", osimShort.getYoyAppr()));
		orderlist.add(0, firstLine);
		AhrIO.writeToFile("./../data/tmp/ad_ol_short_bso.txt", orderlist, ",");
		akShort.bsoPerfToFileAK(osimShort);
		progress += progressSteps[1];
		setProgress(progress);
		//[2] write data needed for R plot to file
		ArrayList<ArrayList<String>> shortBM = AhrIO.scanFile(bmPath, ",");
		ArrayList<ArrayList<String>> shortRF = new ArrayList<ArrayList<String>>();
		shortRF.add(AhrAL.toAL(new String[]{"xvals", "yvals", "data"}));
		for(int i = 0; i < shortBM.size(); i++){
			ArrayList<String> line = new ArrayList<String>();
			line.add(shortBM.get(i).get(fciBM.getIdx("bim")));
			line.add(shortBM.get(i).get(fciBM.getIdx("som")));
			line.add(shortBM.get(i).get(fciBM.getIdx("yoy")));
			shortRF.add(line);
		}
		AhrIO.writeToFile(shortDataPath, shortRF, ",");
		//===== long AK =====
		String lakName = "AK"+String.valueOf(longID);
		String longDataPath = "./../data/r/rdata/demo_long_heat.csv";
		String longPlotPath = "./../resources/demo_long_heat.png";
		String longScriptPath = "./../data/r/rscripts/demo_long_heat.R";
		//[1] calc BIM/SOM Opt (BSO)
		publish("Calculating BIM/SOM Optimization for "+lakName+" (long calls) ...");
		BGM_Manager akLong = new BGM_Manager(longID);
		OrderSim osimLong = new OrderSim(longID);
		osimLong.setTtvMask("010");
		osimLong.calcBSO();
		orderlist = AhrIO.scanFile("./../data/tmp/os_orderlist.txt", ",");
		firstLine = new ArrayList<String>();
		firstLine.add(String.format("%.2f", osimLong.getYoyAppr()));
		orderlist.add(0, firstLine);
		AhrIO.writeToFile("./../data/tmp/ad_ol_long_bso.txt", orderlist, ",");
		akLong.bsoPerfToFileAK(osimLong);
		progress += progressSteps[2];
		setProgress(progress);
		//[2] write data needed for R plot to file
		ArrayList<ArrayList<String>> longBM = AhrIO.scanFile(bmPath, ",");
		ArrayList<ArrayList<String>> longRF = new ArrayList<ArrayList<String>>();
		longRF.add(AhrAL.toAL(new String[]{"xvals", "yvals", "data"}));
		for(int i = 0; i < longBM.size(); i++){
			ArrayList<String> line = new ArrayList<String>();
			line.add(longBM.get(i).get(fciBM.getIdx("bim")));
			line.add(longBM.get(i).get(fciBM.getIdx("som")));
			line.add(longBM.get(i).get(fciBM.getIdx("yoy")));
			longRF.add(line);
		}
		AhrIO.writeToFile(longDataPath, longRF, ",");
		//===== RND =====
		publish("Calculating BIM/SOM Optimization for RND ... ");
		AttributesSK kattr = new AttributesSK("./../data/tmp/sk_attrs.txt");
		ArrayList<String> dates = AhrDate.getDatesBetween(kattr.getSDate(), kattr.getEDate());
		//create another rnd basis file using all others to fill all dates
		String bsPath = "./../out/sk/baseis/rnd/";
		FCI fciBS = new FCI(false, bsPath);
		ArrayList<String> rndBasisFiles = AhrIO.getFilesInPath(bsPath);
		ArrayList<ArrayList<String>> allRND = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < rndBasisFiles.size(); i++){
			ArrayList<String> elimDates = new ArrayList<String>();//eliminate at end of file
			ArrayList<ArrayList<String>> fc = AhrIO.scanFile(bsPath+rndBasisFiles.get(i), ",");
			for(int j = 0; j < fc.size(); j++){
				String itrDate = fc.get(j).get(fciBS.getIdx("date"));
				if(dates.contains(itrDate)){
					allRND.add(fc.get(j));
					if(!elimDates.contains(itrDate)){
						elimDates.add(itrDate);
					}
				}
			}
			for(int j = 0; j < elimDates.size(); j++){
				dates.remove(elimDates.get(j));
			}
		}
		System.out.println("--> In bsoWork, dates left after rnd basis creation : " + dates);
		//write rnd basis file
		AhrIO.writeToFile("./../out/sk/baseis/rnd/RND_"+String.valueOf(totSamplings)+".txt", allRND, ",");
		//write new lines to keys_struct & keys_perf
		String ksPath = "./../out/sk/log/rnd/keys_struct.txt";
		String kpPath = "./../out/sk/log/rnd/keys_perf.txt";
		FCI fciKS= new FCI(true, ksPath);
		FCI fciKP = new FCI(true, kpPath);
		ArrayList<ArrayList<String>> ksFile = AhrIO.scanFile(ksPath, ",");
		ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile(kpPath, ",");
		ArrayList<String> ksRow = new ArrayList<String>(ksFile.get(ksFile.size()-1));
		ArrayList<String> kpRow = new ArrayList<String>(kpFile.get(kpFile.size()-1));
		ksRow.set(fciKS.getIdx("sk_num"), String.valueOf(totSamplings));
		kpRow.set(fciKP.getIdx("sk_num"), String.valueOf(totSamplings));
		kpRow.set(fciKP.getIdx("true_apapt"), "tbd");
		kpRow.set(fciKP.getIdx("true_posp"), "tbd");
		ksFile.add(ksRow);
		kpFile.add(kpRow);
		AhrIO.writeToFile(ksPath, ksFile, ",");
		AhrIO.writeToFile(kpPath, kpFile, ",");
		//calc rnd BSO
		OrderSim osimRnd = new OrderSim("rnd", totSamplings);
		osimRnd.setTtvMask("010");
		osimRnd.calcBSO();
		orderlist = AhrIO.scanFile("./../data/tmp/os_orderlist.txt", ",");
		firstLine = new ArrayList<String>();
		firstLine.add(String.format("%.3f", osimRnd.getBIM()));
		firstLine.add(String.format("%.3f", osimRnd.getSOM()));
		firstLine.add(String.format("%.4f", osimRnd.getTrigAppr()));
		firstLine.add(String.format("%.2f", osimRnd.getYoyAppr()));
		AhrDate.sortDates2D(orderlist, true, 0);
		orderlist.add(0, firstLine);
		AhrIO.writeToFile("./../data/tmp/ad_ol_rnd_bso.txt", orderlist, ",");
		progress += progressSteps[3];
		setProgress(progress);	

		//calc orderlists for all methods for no BSO
		publish("Saving BSO Information ... ");
		osimShort.setBIM(0.001);
		osimShort.setSOM(50.0);
		osimShort.calcOrderList();
		orderlist = AhrIO.scanFile("./../data/tmp/os_orderlist.txt", ",");
		AhrIO.writeToFile("./../data/tmp/ad_ol_short_normal.txt", orderlist, ",");	
		osimLong.setBIM(50.0);
		osimLong.setSOM(0.001);
		osimLong.calcOrderList();
		orderlist = AhrIO.scanFile("./../data/tmp/os_orderlist.txt", ",");
		AhrIO.writeToFile("./../data/tmp/ad_ol_long_normal.txt", orderlist, ",");	
		osimRnd.setBIM(50.0);
		osimRnd.setSOM(0.001);
		osimRnd.calcOrderList();
		orderlist = AhrIO.scanFile("./../data/tmp/os_orderlist.txt", ",");
		AhrIO.writeToFile("./../data/tmp/ad_ol_rnd_normal.txt", orderlist, ",");	
		progress += progressSteps[4];
		setProgress(progress);

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


public class AutoDemo {

	private JFrame frame;
	private int minimumX = 400;
	private int preferredX = 600;
	private int maximumX = 800;
	private int totSamplings = 5;	//# of rnd samplings to create a benchmark

	public AutoDemo(){
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setTitle("STAG3 Demonstration");
		frame.setSize(600, 700);	
		runDemo();
	}

	public void runDemo(){
		//lists and over-arching data
		int picDimX = 440;
		int picDimY = 500;
		ImageIcon iiPic = new ImageIcon("./../resources/cool.png");
		AttributesSK defAttrs = new AttributesSK();
		defAttrs.saveToFile("./../data/tmp/sk_attrs.txt");
		String[] resultPlotList = new String[]{"Appr Distribution (B&W)", "Portfolio Growth ($)"};
		String[] simTradeList = new String[]{"Short", "Random", "Long"};

		//layout
		JPanel pMain = new JPanel();
		pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
		pMain.setOpaque(false);
		JScrollPane spMain = new JScrollPane(pMain, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
											JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel pPlots = new JPanel();
		pPlots.setLayout(null);
		JPanel pTrades = new JPanel();
		pTrades.setLayout(null);

		//init components
		JButton bStartDemo = new JButton("Start Demo");
		JButton bEditInputs = new JButton("Edit Input Params");
		JButton bAcronymRef = new JButton("Acronym Reference Table");
		JButton bTestComps = new JButton("Test Components");
		JTextPane tpDesc = new JTextPane();
		tpDesc.setText("This demonstration will show the effectiveness of using an Artificial "+
						"Neural Network (ANN) on stock data to help predict short-term price fluctuations "+
						"in individual stock tickers. Below are the steps needed to calculate the performance"+
						" of the ANN algorithm, the data will be filled out below the corresponding step when"+
						" the demo is ran.");
		JTextPane tpNoteLen = new JTextPane();
		tpNoteLen.setText("NOTE :  Each step many take several minutes to compute.");
		JTextPane tpStep1 = new JTextPane();
		tpStep1.setText("Step 1 :  Create a set of random samples to show innate market performance before any"+
						" machine learning algorithm is applied."); 
		JTextPane tpStep2 = new JTextPane();
		tpStep2.setText("Step 2 :  Apply ANN algorithm to all dates in which the market price is below the 5-day"+
						" SMA of the market (MS Mask = xxxxxx0x).");
		JLabel lbProgressSK1 = new JLabel("Placeholder");
		JProgressBar pbProgressSK1 = new JProgressBar();
		JTextPane tpStep3 = new JTextPane();
		tpStep3.setText("Step 3 :  Apply ANN algorithm to all dates in which the market price is above the 5-day"+
						" SMA of the market (MS Mask = xxxxxx1x).");
		JLabel lbProgressSK2 = new JLabel("Placeholder");
		JProgressBar pbProgressSK2 = new JProgressBar();
		JTextPane tpStep4 = new JTextPane();
		tpStep4.setText("Step 4 :  Coelesce the results from steps 2 and 3 to create aggregate keys, one short"+
						" and one long, that will work in any market state condition.");
		JLabel lbProgressAK1 = new JLabel("Placeholder");
		JProgressBar pbProgressAK1 = new JProgressBar();
		JTextPane tpNoteDatasets = new JTextPane();
		tpNoteDatasets.setText("NOTE :  Each key is ran over two datasets, the dataset the machine learning algorithm"+
						" is trained on, and the one it is tested against to see its effectiveness. Step 5 and the"+
						" results will be using the test dataset exclusively."); 
		JTextPane tpStep5 = new JTextPane();
		tpStep5.setText("Step 5 :  The APAPT can be optimized farther by simulating real world trading conditions"+
						" and calculating the best buy-in multiplier (BIM) and sell-out multiplier (SOM) of the"+
						" trades. The multiplier would be in regards to the stocks last closing price.");
		JLabel lbProgressBSO = new JLabel("Placeholder");
		JProgressBar pbProgressBSO = new JProgressBar();
		JLabel lbDescTableBSO = new JLabel("Final results, with BIM/SOM optimization.");
		JTextPane tpResults = new JTextPane();
		tpResults.setText("Final Results: Now that we have two comprehensive trading strategies, one for short calls"+
						", and one for long calls, plots comparing the two alongside the random sampling can be shown.");
		JLabel lbPlotType = new JLabel("Plot Type:");
		JComboBox cbPlotType = new JComboBox();
		JButton bPlot = new JButton("Plot");
		JLabel lbTrades = new JLabel("View Simulated Trades:");
		JComboBox cbTrades = new JComboBox();
		JButton bTrades = new JButton("View");
				
		//---------- init tables -----------
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
		DefaultTableModel dtmBench = new DefaultTableModel(benchmarkData, benchmarkHeader);
		JTable tBenchmark = new JTable(dtmBench);
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
		String[][] bsoData = new String[3][6];
		String[] bsoHeader = new String[]{"AK ID", "Call", "BIM", "SOM", "APAPT", "APY"};
		bsoData[0] = new String[]{"", "Short", "", "", "", ""};
		bsoData[1] = new String[]{"", "Long", "", "", "", ""};
		bsoData[2] = new String[]{"RND", "Long", "", "", "", ""};
		DefaultTableModel dtmBSO = new DefaultTableModel(bsoData, bsoHeader);
		JTable tBSO = new JTable(dtmBSO);
		centerCols(tBSO);
		tBSO.setRowHeight(tableRowHeight);
		JScrollPane spBSO = new JScrollPane(tBSO, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


		//component placement
		bStartDemo.setAlignmentX(Component.CENTER_ALIGNMENT);
		bEditInputs.setAlignmentX(Component.CENTER_ALIGNMENT);
		bAcronymRef.setAlignmentX(Component.CENTER_ALIGNMENT);
		bTestComps.setAlignmentX(Component.CENTER_ALIGNMENT);
		int tBenchmarkHeight = ((totSamplings+1)*tableRowHeight)+22;
		int tSK1Height = ((4*tableRowHeight)+22);
		int tSK2Height = ((4*tableRowHeight)+22);
		int tAK1Height = ((4*tableRowHeight)+22);
		int tBSOHeight = ((3*tableRowHeight)+22);
		lbPlotType.setBounds(10, 5, 100, 25);
		cbPlotType.setBounds(100, 5, 200, 25);
		bPlot.setBounds(310, 5, 60, 25);
		lbTrades.setBounds(10, 5, 180, 25);
		cbTrades.setBounds(190, 5, 110, 25);
		bTrades.setBounds(310, 5, 60, 25);
		
		//basic functionality and aesthetic
		Color backColor = new Color(233, 225, 212);
		spMain.getViewport().setBackground(backColor);
		spMain.getVerticalScrollBar().setUnitIncrement(16);
		setButtonStyle(bStartDemo);
		setButtonStyle(bEditInputs);
		setButtonStyle(bAcronymRef);
		setButtonStyle(bPlot);
		setButtonStyle(bTrades);
		disguiseAndUnderlineTextPane(tpDesc, 0);
		disguiseAndUnderlineTextPane(tpNoteLen, 6);
		disguiseAndUnderlineTextPane(tpStep1, 8);
		disguiseAndUnderlineTextPane(tpStep2, 8);
		disguiseAndUnderlineTextPane(tpStep3, 8);
		disguiseAndUnderlineTextPane(tpStep4, 8);
		disguiseAndUnderlineTextPane(tpNoteDatasets, 6);
		disguiseAndUnderlineTextPane(tpStep5, 8);
		disguiseAndUnderlineTextPane(tpResults, 14);
		for(int i = 0; i < resultPlotList.length; i++){
			cbPlotType.addItem(resultPlotList[i]);
		}
		for(int i = 0; i < simTradeList.length; i++){
			cbTrades.addItem(simTradeList[i]);
		}
		pPlots.setBackground(backColor);
		pTrades.setBackground(backColor);
		//lbPic.setIcon(iiPic);

		//add everything
		pMain.add(Box.createRigidArea(new Dimension(0, 5)));
		pMain.add(compPlacer(bStartDemo, false, 25, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(bEditInputs, false, 25, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(bAcronymRef, false, 25, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		//pMain.add(compPlacer(bTestComps, false, 25, 0.80));
		//pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpDesc, false, 80, 1.00));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpNoteLen, false, 30, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpStep1, false, 30, 1.00));
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
		pMain.add(compPlacer(tpStep4, false, 30, 1.00));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(lbProgressAK1, false, 20, 0.80));
		pMain.add(compPlacer(pbProgressAK1, false, 15, 0.80));
		pMain.add(compPlacer(spAK1, true, tAK1Height, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpNoteDatasets, false, 70, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpStep5, false, 60, 1.00));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(lbProgressBSO, true, 20, 0.80));
		pMain.add(compPlacer(pbProgressBSO, true, 15, 0.80));
		pMain.add(compPlacer(lbDescTableBSO, true, 20, 0.80));
		pMain.add(compPlacer(spBSO, true, tBSOHeight, 0.80));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pMain.add(compPlacer(tpResults, false, 45, 1.00));
		pMain.add(Box.createRigidArea(new Dimension(0, 10)));
		pPlots.add(lbPlotType);
		pPlots.add(cbPlotType);
		pPlots.add(bPlot);
		pMain.add(compPlacer(pPlots, false, 40, 1.00));
		pTrades.add(lbTrades);
		pTrades.add(cbTrades);
		pTrades.add(bTrades);
		pMain.add(compPlacer(pTrades, false, 40, 1.00));

		//reset button as visible
		bAcronymRef.setVisible(true);
		bEditInputs.setVisible(true);
		bStartDemo.setVisible(true);
		bTestComps.setVisible(true);	
		tpDesc.setVisible(true);
		tpNoteLen.setVisible(true);
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

		//worker thread listeners
		PropertyChangeListener bsoPCL = new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e){
				if("progress".equals(e.getPropertyName())){
					int progress = Integer.parseInt(e.getNewValue().toString());
					if(progress >= 50){
					}
				}
				if("state".equals(e.getPropertyName())){
					if("DONE".equals(e.getNewValue().toString())){
						//setup final table (AK BSO)
						lbProgressBSO.setVisible(false);
						pbProgressBSO.setVisible(false);
						spBSO.setVisible(true);
						fillTableBSO(tBSO);
						//enabled buttons, make results stuff visible
						bStartDemo.setEnabled(true);
						bEditInputs.setEnabled(true);
						tpResults.setVisible(true);
						pPlots.setVisible(true);
						pTrades.setVisible(true);
					}
				}
			}
		};
		PropertyChangeListener akPCL = new PropertyChangeListener(){
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
						BimSomOptWorker bsoWork = new BimSomOptWorker(lbProgressBSO, pbProgressBSO, picDimX, 
																					picDimY, totSamplings);
						bsoWork.addPropertyChangeListener(bsoPCL);
						bsoWork.execute();					
					}
				}
			}
		};
		PropertyChangeListener sk2PCL = new PropertyChangeListener(){
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
						AggKeyWorker akWork = new AggKeyWorker(lbProgressAK1, pbProgressAK1);
						akWork.addPropertyChangeListener(akPCL);
						akWork.execute();
					}
				}
			}
		};
		PropertyChangeListener sk1PCL = new PropertyChangeListener(){
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
						pbProgressSK2.setValue(0);
						pbProgressSK2.setVisible(true);
						SingleKeyWorker skWork2 = new SingleKeyWorker(lbProgressSK2, pbProgressSK2, "xxxxxx1x", 2);
						skWork2.addPropertyChangeListener(sk2PCL);
						skWork2.execute();
					}
				}
			}
		};
		PropertyChangeListener bmPCL = new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e){
				//System.out.println("PCL Triggered : " + e.toString());
				//do something upon completion of the thread
				if("state".equals(e.getPropertyName())){
					//System.out.println("state: " + e.getNewValue().toString());
					if("DONE".equals(e.getNewValue().toString())){
						lbProgressSK1.setVisible(true);
						pbProgressSK1.setValue(0);
						pbProgressSK1.setVisible(true);
						SingleKeyWorker skWork1 = new SingleKeyWorker(lbProgressSK1, pbProgressSK1, "xxxxxx0x", 1);
						skWork1.addPropertyChangeListener(sk1PCL);
						skWork1.execute();
					}
				}
			}
		};

		//listener functionality
		bAcronymRef.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				AD_Acronyms ada = new AD_Acronyms();
			}
		});
		bEditInputs.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				AD_Params adp = new AD_Params();
			}
		});
		bStartDemo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//reset tables
				for(int i = 0; i < tBenchmark.getRowCount(); i++){
					tBenchmark.setValueAt("", i, 1);
					tBenchmark.setValueAt("", i, 2);	
				}
				for(int i = 0; i < tSK1.getRowCount(); i++){
					tSK1.setValueAt("", i, 0);
					tSK1.setValueAt("", i, 3);
					tSK1.setValueAt("", i, 4);
				}
				for(int i = 0; i < tSK2.getRowCount(); i++){
					tSK2.setValueAt("", i, 0);
					tSK2.setValueAt("", i, 3);
					tSK2.setValueAt("", i, 4);
				}
				for(int i = 0; i < tAK1.getRowCount(); i++){
					tAK1.setValueAt("", i, 0);
					tAK1.setValueAt("", i, 3);
					tAK1.setValueAt("", i, 4);
				}
				for(int i = 0; i < tBSO.getRowCount(); i++){
					if(i != 2){
						tBSO.setValueAt("", i, 0);
					}
					tBSO.setValueAt("", i, 2);
					tBSO.setValueAt("", i, 3);
					tBSO.setValueAt("", i, 4);
					tBSO.setValueAt("", i, 5);
				}
				//reset whats visible
				spSK1.setVisible(false);
				spSK2.setVisible(false);
				spAK1.setVisible(false);
				spBSO.setVisible(false);
				tpResults.setVisible(false);
				pPlots.setVisible(false);
				pTrades.setVisible(false);

				//remove lines from rnd keys_Struct & keys_perf
				String ksPath = "./../out/sk/log/rnd/keys_struct.txt";
				String kpPath = "./../out/sk/log/rnd/keys_perf.txt";
				ArrayList<String> ksRow = AhrIO.scanRow(ksPath, ",", 0);
				ArrayList<String> kpRow = AhrIO.scanRow(kpPath, ",", 0);
				ArrayList<ArrayList<String>> ksFile = new ArrayList<ArrayList<String>>();
				ArrayList<ArrayList<String>> kpFile = new ArrayList<ArrayList<String>>();
				ksFile.add(ksRow);
				kpFile.add(kpRow);
				AhrIO.writeToFile(ksPath, ksFile, ",");
				AhrIO.writeToFile(kpPath, kpFile, ",");
				//remove rnd tmp basis files
				String rbPath = "./../out/sk/baseis/rnd/";
				ArrayList<String> rndFiles = AhrIO.getFilesInPath(rbPath);
				for(int i = 0; i < rndFiles.size(); i++){
					File file = new File(rbPath+rndFiles.get(i));
					if(file.exists()){
						file.delete();
					}
				}

				//start worker threads
				bStartDemo.setEnabled(false);
				bEditInputs.setEnabled(false);
				spBenchmark.setVisible(true);
				frame.revalidate();
				frame.repaint();
				BenchmarkWorker bmWork = new BenchmarkWorker(tBenchmark, totSamplings);
				bmWork.addPropertyChangeListener(bmPCL);
				bmWork.execute();
				System.out.println("--> Post bmWork thread.");
			}
		});
		bPlot.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int plotIdx = cbPlotType.getSelectedIndex();
				double[] avgs = new double[6];
				int[] counts = new int[6];
				if(plotIdx == 0){//B&W, appr distn
					FCI fciOL = new FCI(false, "./../data/tmp/os_orderlist.txt");
					//data to write to file for R to use
					ArrayList<ArrayList<String>> rdata = new ArrayList<ArrayList<String>>();
					rdata.add(AhrAL.toAL(new String[]{"variable", "value"}));
					ArrayList<ArrayList<String>> orderlist = AhrIO.scanFile("./../data/tmp/ad_ol_short_normal.txt", ",");
					for(int i = 0; i < orderlist.size(); i++){
						String itrMethAppr = orderlist.get(i).get(fciOL.getIdx("method_appr"));
						avgs[0] += Double.parseDouble(itrMethAppr);
						counts[0]++;
						ArrayList<String> line = new ArrayList<String>();
						line.add("short_normal");
						line.add(itrMethAppr);
						rdata.add(line);
					}
					orderlist = AhrIO.scanFile("./../data/tmp/ad_ol_short_bso.txt",",");
					for(int i = 0; i < orderlist.size(); i++){
						String itrMethAppr = orderlist.get(i).get(fciOL.getIdx("method_appr"));
						String itrTrigCode = orderlist.get(i).get(fciOL.getIdx("trigger_code"));
						if(!itrTrigCode.equals("NO")){
							avgs[1] += Double.parseDouble(itrMethAppr);
							counts[1]++;
							ArrayList<String> line = new ArrayList<String>();
							line.add("short_bso");
							line.add(itrMethAppr);
							rdata.add(line);
						}
					}
					orderlist = AhrIO.scanFile("./../data/tmp/ad_ol_rnd_normal.txt", ",");
					for(int i = 0; i < orderlist.size(); i++){
						String itrMethAppr = orderlist.get(i).get(fciOL.getIdx("method_appr"));
						avgs[2] += Double.parseDouble(itrMethAppr);
						counts[2]++;
						ArrayList<String> line = new ArrayList<String>();
						line.add("rnd_normal");
						line.add(itrMethAppr);
						rdata.add(line);
					}
					orderlist = AhrIO.scanFile("./../data/tmp/ad_ol_rnd_bso.txt",",");
					for(int i = 0; i < orderlist.size(); i++){
						String itrMethAppr = orderlist.get(i).get(fciOL.getIdx("method_appr"));
						String itrTrigCode = orderlist.get(i).get(fciOL.getIdx("trigger_code"));
						if(!itrTrigCode.equals("NO")){
							avgs[3] += Double.parseDouble(itrMethAppr);
							counts[3]++;
							ArrayList<String> line = new ArrayList<String>();
							line.add("rnd_bso");
							line.add(itrMethAppr);
							rdata.add(line);
						}
					}
					orderlist = AhrIO.scanFile("./../data/tmp/ad_ol_long_normal.txt", ",");
					for(int i = 0; i < orderlist.size(); i++){
						String itrMethAppr = orderlist.get(i).get(fciOL.getIdx("method_appr"));
						avgs[4] += Double.parseDouble(itrMethAppr);
						counts[4]++;
						ArrayList<String> line = new ArrayList<String>();
						line.add("long_normal");
						line.add(itrMethAppr);
						rdata.add(line);
					}
					orderlist = AhrIO.scanFile("./../data/tmp/ad_ol_long_bso.txt",",");
					for(int i = 0; i < orderlist.size(); i++){
						String itrMethAppr = orderlist.get(i).get(fciOL.getIdx("method_appr"));
						String itrTrigCode = orderlist.get(i).get(fciOL.getIdx("trigger_code"));
						if(!itrTrigCode.equals("NO")){
							avgs[5] += Double.parseDouble(itrMethAppr);
							counts[5]++;
							ArrayList<String> line = new ArrayList<String>();
							line.add("long_bso");
							line.add(itrMethAppr);
							rdata.add(line);
						}
					}
					for(int i = 0; i < 6; i++){
						avgs[i] = avgs[i] / (double)counts[i];
						System.out.println("Avg"+i+" : "+String.format("%.3f", avgs[i]));
					}
					//R related paths and plot dims
					String dataPath = "./../data/r/rdata/ad_mappr_baw.csv";
					String scriptPath = "./../data/r/rscripts/ad_mappr_baw.R";
					String plotPath1 = "./../resources/ad_mappr_baw1.png";
					String plotPath2 = "./../resources/ad_mappr_baw2.png";
					int xdim = 500;
					int ydim = 500;
					//create R plot
					AhrIO.writeToFile(dataPath, rdata, ",");
					RCode rcode = new RCode();
					rcode.setTitle("Trade Appr Distribution By Method");
					rcode.setXLabel("Method");
					rcode.setYLabel("Trade Appr (%)");
					rcode.createBAW(dataPath, plotPath1, xdim, ydim, false);
					rcode.writeCode(scriptPath);
					rcode.runScript(scriptPath);
					rcode.resetCode();
					rcode.setTitle("Trade Appr Distribution By Method w/ Averages (zoomed)");
					rcode.softLimY(-20, 20);
					rcode.createBAW(dataPath, plotPath2, xdim, ydim, true);
					rcode.writeCode(scriptPath);
					rcode.runScript(scriptPath); 
					//show plot on popout frame
					JFrame rframe = new JFrame();
					rframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					rframe.setTitle("Demo Results");
					JLabel lbBAW1 = new JLabel();
					JLabel lbBAW2 = new JLabel();
					lbBAW1.setPreferredSize(new Dimension(xdim, ydim));
					lbBAW2.setPreferredSize(new Dimension(xdim, ydim));
					ImageIcon iiBAW1 = new ImageIcon(plotPath1);
					ImageIcon iiBAW2 = new ImageIcon(plotPath2);
					lbBAW1.setIcon(iiBAW1);
					lbBAW2.setIcon(iiBAW2);
					rframe.getContentPane().add(lbBAW1, BorderLayout.CENTER);
					rframe.getContentPane().add(lbBAW2, BorderLayout.EAST);
					rframe.pack();
					rframe.setVisible(true);
					iiBAW1.getImage().flush();
					iiBAW2.getImage().flush();
				}else if(plotIdx == 1){//portfolio growth, short
					//R related paths and plot dims
					String dataPath = "./../data/r/rdata/ad_growth.csv";
					String scriptPath = "./../data/r/rscripts/ad_growth.R";
					String plotPath = "./../resources/ad_growth.png";
					int xdim = 800;
					int ydim = 600;
					//create growth data
					double principal = 100000.0;
					ArrayList<ArrayList<String>> rdata = new ArrayList<ArrayList<String>>();
					rdata.add(AhrAL.toAL(new String[]{"date", "variable", "value"}));
					OrderSim osim = new OrderSim();
					ArrayList<ArrayList<String>> orderlist = AhrIO.scanFile("./../data/tmp/ad_ol_short_bso.txt", ",");
					osim.setIsLong(false);
					osim.setOrderList(orderlist);
					ArrayList<ArrayList<String>> growth = osim.calcGrowth(principal);
					for(int i = 0; i < growth.size(); i++){
						ArrayList<String> line = new ArrayList<String>();
						line.add(growth.get(i).get(0));
						line.add("short_bso");
						line.add(growth.get(i).get(1));
						rdata.add(line);
					}
					orderlist = AhrIO.scanFile("./../data/tmp/ad_ol_rnd_bso.txt", ",");
					osim.setIsLong(true);
					osim.setOrderList(orderlist);
					growth = osim.calcGrowth(principal);
					for(int i = 0; i < growth.size(); i++){
						ArrayList<String> line = new ArrayList<String>();
						line.add(growth.get(i).get(0));
						line.add("rnd_bso");
						line.add(growth.get(i).get(1));
						rdata.add(line);
					}
					orderlist = AhrIO.scanFile("./../data/tmp/ad_ol_long_bso.txt", ",");
					osim.setIsLong(true);
					osim.setOrderList(orderlist);
					growth = osim.calcGrowth(principal);
					for(int i = 0; i < growth.size(); i++){
						ArrayList<String> line = new ArrayList<String>();
						line.add(growth.get(i).get(0));
						line.add("long_bso");
						line.add(growth.get(i).get(1));
						rdata.add(line);
					}
					AhrIO.writeToFile(dataPath, rdata, ",");
					//create R plot
					RCode rcode = new RCode();
					rcode.setTitle("Simulated Portfolio Growth By Method");
					rcode.setXLabel("Date");
					rcode.setYLabel("Portfolio Value ($)");
					rcode.createTimeSeries(dataPath, plotPath, xdim, ydim);
					rcode.writeCode(scriptPath);
					rcode.runScript(scriptPath);
					//show plot on popout frame
					JFrame rframe = new JFrame();
					rframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					rframe.setTitle("Demo Results");
					JLabel lbGrowth = new JLabel();
					lbGrowth.setPreferredSize(new Dimension(xdim, ydim));
					ImageIcon iiGrowth = new ImageIcon(plotPath);
					lbGrowth.setIcon(iiGrowth);
					rframe.getContentPane().add(lbGrowth, BorderLayout.CENTER);
					rframe.pack();
					rframe.setVisible(true);
					iiGrowth.getImage().flush();
				}else if(plotIdx == 2){//portolio growth, long

				}
			}
		});
		bTrades.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//get col names for data to be displayed
				FCI fciOL = new FCI(false, "./../data/tmp/os_orderlist.txt");
				String[] header = AhrAL.toArr(fciOL.getTags());
				//get orderlist file path according to cb selection
				int cbIdx = cbTrades.getSelectedIndex();
				String olPath = "";
				String rfTitle = "";
				if(cbIdx == 0){
					olPath = "./../data/tmp/ad_ol_short_bso.txt";
					rfTitle = "All Trades for AK (short trading strategy)";
				}else if(cbIdx == 1){
					olPath = "./../data/tmp/ad_ol_rnd_bso.txt";
					rfTitle = "All Trades for AK (random trading strategy)";
				}else if(cbIdx == 2){
					olPath = "./../data/tmp/ad_ol_long_bso.txt";
					rfTitle = "All Trades for AK (long trading strategy)";
				}
				//create sperate frame to show data
				JFrame rframe = new JFrame();
				rframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				rframe.setTitle(rfTitle);
				JPanel pTableSort = new TableSortPanel(olPath, header);
				rframe.getContentPane().add(pTableSort);
				rframe.pack();
				rframe.setVisible(true);
			}
		});
	}
	//----------------- Table Data Functions ------------------
	//fill out given JTable w/ data from last SKs in keys_perf.txt
	private void fillTableSK(JTable table){
		String kpPath = "./../out/sk/log/ann/keys_perf.txt";
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
	//fill out given JTable w/ data from last AKs in ak_log.txt
	private void fillTableAK(JTable table){
		String laPath = "./../out/ak/log/ak_log.txt";
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
	//fill out given JTable w/ data from AKs BSO data
	private void fillTableBSO(JTable table){
		String laPath = "./../out/ak/log/ak_log.txt";
		FCI fciLA = new FCI(true, laPath);
		ArrayList<ArrayList<String>> laFile = AhrIO.scanFile(laPath, ",");
		//get APY vals from tmp files then update to remove it from file
		String solPath = "./../data/tmp/ad_ol_short_bso.txt";
		String lolPath = "./../data/tmp/ad_ol_long_bso.txt";
		String rolPath = "./../data/tmp/ad_ol_rnd_bso.txt";
		ArrayList<ArrayList<String>> solFile = AhrIO.scanFile(solPath, ",");
		ArrayList<ArrayList<String>> lolFile = AhrIO.scanFile(lolPath, ",");
		ArrayList<ArrayList<String>> rolFile = AhrIO.scanFile(rolPath, ",");
		String shortAPY = solFile.get(0).get(0);
		String longAPY = lolFile.get(0).get(0);
		String rndBIM = rolFile.get(0).get(0);
		String rndSOM = rolFile.get(0).get(1);
		String rndAPAPT = rolFile.get(0).get(2);
		String rndAPY = rolFile.get(0).get(3);
		solFile.remove(0);
		lolFile.remove(0);
		rolFile.remove(0);
		AhrIO.writeToFile(solPath, solFile, ",");
		AhrIO.writeToFile(lolPath, lolFile, ",");
		AhrIO.writeToFile(rolPath, rolFile, ",");
		if(laFile.size() > 2){
			int lastShortRowIdx = laFile.size()-2;
			int lastLongRowIdx = laFile.size()-1;
			String shortID = laFile.get(lastShortRowIdx).get(fciLA.getIdx("ak_num"));
			String shortBimSom = laFile.get(lastShortRowIdx).get(fciLA.getIdx("ak_bso"));
			System.out.println("--> shortBimSom = " + shortBimSom);
			String shortBim = shortBimSom.split("\\|")[0];
			String shortSom = shortBimSom.split("\\|")[1];
			String shortAPAPT = laFile.get(lastShortRowIdx).get(fciLA.getIdx("bso_test_apapt"));
			String longID = laFile.get(lastLongRowIdx).get(fciLA.getIdx("ak_num"));
			String longBimSom = laFile.get(lastLongRowIdx).get(fciLA.getIdx("ak_bso"));
			System.out.println("--> longBimSom = " + longBimSom);
			String longBim = longBimSom.split("\\|")[0];
			String longSom = longBimSom.split("\\|")[1];
			String longAPAPT = laFile.get(lastLongRowIdx).get(fciLA.getIdx("bso_test_apapt"));
			table.setValueAt(shortID, 0, 0);
			table.setValueAt(shortBim, 0, 2);
			table.setValueAt(shortSom, 0, 3);
			table.setValueAt(shortAPAPT, 0, 4);
			table.setValueAt(shortAPY, 0, 5);
			table.setValueAt(longID, 1, 0);
			table.setValueAt(longBim, 1, 2);
			table.setValueAt(longSom, 1, 3);
			table.setValueAt(longAPAPT, 1, 4);
			table.setValueAt(longAPY, 1, 5);
			table.setValueAt(rndBIM, 2, 2);
			table.setValueAt(rndSOM, 2, 3);
			table.setValueAt(rndAPAPT, 2, 4);
			table.setValueAt(rndAPY, 2, 5);
		}else{
			System.out.println("ERR: not enough AKs in ak_log.txt");
		}
	}

	//----------------- GUI Helper Functions ------------------
	//GUI related, sets style to a JButton
	public void setButtonStyle(JButton btn){
		Font plainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		btn.setFont(plainFont);
		btn.setBackground(new Color(230, 230, 230));
		btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}
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
	//places a JLabel that has pic in it
	private JLabel picPlacer(JLabel lb, int width, int height){
		lb.setSize(new Dimension(width, height));
		lb.setAlignmentX(Component.CENTER_ALIGNMENT);
		lb.setVisible(false);
		return lb;
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


