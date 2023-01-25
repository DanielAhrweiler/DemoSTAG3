package ahrweiler.bgm;
import ahrweiler.Globals;
import ahrweiler.util.*;
import ahrweiler.support.FCI;
import ahrweiler.bgm.ann.Network;
import ahrweiler.bgm.ann.Node;
import ahrweiler.bgm.AttributesSK;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.io.File;

public class ANN {

	private String db_name = "Intrinio";
	
	//attrs to set up before SK calc
	private int id;
	private String dbuc;			//DB Used (IT or YH)
	private String sdate;
	private String edate;
	private boolean is_long;
	private boolean is_cr_method;		//target method (continuous or binomial)
	private double plateau;
	private double learnRate;
	private int iteration;
	private int spd;				//stocks per day
	private int tvi;				//target var index
	private String msMask;
	private String narMask;
	private String indMask;
	private int activeIndNum;		//# of active inds
	private ArrayList<String> activeIndNames;
	//attrs used during SK calc
	private Network network;
	private ArrayList<String> trainFiles;	
	private int trainLineCount;
	private int testLineCount;
	private double avgError;
	private ArrayList<ArrayList<String>> errLog;
	private ArrayList<ArrayList<String>> results;

	//------------- CONSTRUCTORS ----------------
	//pull data from a saved file
	public ANN(int idNum){
		String path = "./../out/ml/ann/keys_struct.txt";
		FCI fciKS = new FCI(true, path);
		ArrayList<String> ksLine = AhrIO.scanRow(path, ",", String.valueOf(idNum));
		if(ksLine.size() > 3){
			this.id = idNum;
			this.dbuc = ksLine.get(fciKS.getIdx("db_used"));
			this.is_cr_method = false;
			if(ksLine.get(fciKS.getIdx("method")).equals("CR")){
				this.is_cr_method = true;
			}
			this.is_long = false;
			if(ksLine.get(fciKS.getIdx("call")).equals("1")){
				this.is_long = true;
			}
			this.sdate = ksLine.get(fciKS.getIdx("start_date"));
			this.edate = ksLine.get(fciKS.getIdx("end_date"));
			this.learnRate = Double.parseDouble(ksLine.get(fciKS.getIdx("learn_rate")));
			this.plateau = Double.parseDouble(ksLine.get(fciKS.getIdx("plateau")));
			this.spd = Integer.parseInt(ksLine.get(fciKS.getIdx("spd")));
			this.tvi = Integer.parseInt(ksLine.get(fciKS.getIdx("tvi")));
			this.msMask = ksLine.get(fciKS.getIdx("ms_mask"));
			this.narMask = ksLine.get(fciKS.getIdx("nar_mask"));
			this.indMask = ksLine.get(fciKS.getIdx("ind_mask"));
			setActiveIndNames(this.indMask);
			this.trainFiles = new ArrayList<String>();
			this.errLog = new ArrayList<ArrayList<String>>();
			this.results = new ArrayList<ArrayList<String>>();
		}else{
			System.out.println("ERROR: ID # not found in this file.");
			this.id = idNum;
			this.activeIndNum = 0;
			this.activeIndNames = new ArrayList<String>();
			this.trainFiles = new ArrayList<String>();
			this.errLog = new ArrayList<ArrayList<String>>();
			this.results = new ArrayList<ArrayList<String>>();
		}
	}
	//use data from ArritubesSK class, easy comm method b/w classes
	public ANN(AttributesSK attrs){
		this.id = -1;
		this.is_cr_method = false;
		if(attrs.getAnnMethod().equals("CR")){
			this.is_cr_method = true;
		}
		this.is_long = attrs.getCall();
		this.sdate = attrs.getSDate();
		this.edate = attrs.getEDate();
		this.learnRate = attrs.getLearnRate();
		this.plateau = attrs.getPlateau();
		this.spd = attrs.getSPD();
		this.tvi = attrs.getTVI();
		this.msMask = attrs.getMsMask();
		this.narMask = attrs.getNarMask();
		this.indMask = attrs.getIndMask();
		setActiveIndNames(this.indMask);
		this.trainFiles = new ArrayList<String>();
		this.errLog = new ArrayList<ArrayList<String>>();
		this.results = new ArrayList<ArrayList<String>>();
	}
	//if empty, just use default AttributeSK obj
	public ANN(){
		this(new AttributesSK());		
	}

	//------------- GETTERS ----------------
	public int getID(){
		return this.id;
	}
	public String getDBUC(){
		return this.dbuc;
	}
	public boolean getCall(){
		return this.is_long;
	}
	public boolean getMethod(){
		return this.is_cr_method;
	}
	public String getSDate(){
		return this.sdate;
	}
	public String getEDate(){
		return this.edate;
	}
	public double getLearnRate(){
		return this.learnRate;
	}
	public double getPlateau(){
		return this.plateau;
	}
	public int getSPD(){
		return this.spd;
	}
	public int getTVI(){
		return this.tvi;
	}
	public String getMsMask(){
		return this.msMask;
	}
	public String getIndMask(){
		return this.indMask;
	}
	public String getNarMask(){
		return this.narMask;
	}
	public int getActiveIndNum(){
		return this.activeIndNum;
	}
	public double getAvgError(){
		return this.avgError;
	}
	public ArrayList<String> getTrainFiles(){
		return this.trainFiles;
	}
	public int getTrainFilesSize(){
		return this.trainFiles.size();
	}
	public ArrayList<ArrayList<String>> getErrorLog(){
		return this.errLog;	
	}
	public ArrayList<String> getActiveIndNames(){
		return this.activeIndNames;
	}
	public int getItr(){
		return this.iteration;
	}


	//------------- SETTERS ---------------
	public void setID(int idVal){
		this.id = idVal;
	}
	public void setDBUC(String dbucVal){
		this.dbuc = dbucVal;
	}
	public void setCall(boolean callVal){
		this.is_long = callVal;
	}
	public void setMethod(boolean methVal){
		this.is_cr_method = methVal;
	}
	public void setSDate(String sdVal){
		this.sdate = sdVal;
	}
	public void setEDate(String edVal){
		this.edate = edVal;
	}
	public void setLearnRate(double lrVal){
		this.learnRate = lrVal;
	}
	public void setPlateau(double platVal){
		this.plateau = platVal;
	}
	public void setSPD(int spdVal){
		this.spd = spdVal;
	}
	public void setTVI(int tviVal){
		this.tvi = tviVal;
	}
	public void setMsMask(String msVal){
		this.msMask = msVal;
	}
	public void setIndMask(String maskVal){
		this.indMask = maskVal;
		setActiveIndNames(this.indMask);
	}
	public void setNarMask(String narVal){
		this.narMask = narVal;
	}
	public void setAvgError(int errVal){
		this.avgError = errVal;
	}
	public void setErrorLog(ArrayList<ArrayList<String>> errAL){
		this.errLog = errAL;
	}
	public void setActiveIndNames(ArrayList<String> indsAL){
		this.activeIndNum = indsAL.size();
		this.activeIndNames = indsAL;
	}
	public void setActiveIndNames(String indMask){
		this.activeIndNum = 0;
		this.activeIndNames = new ArrayList<String>();
		for(int i = 0; i < indMask.length(); i++){
			if(indMask.charAt(i) == '1'){
				this.activeIndNum++;
				this.activeIndNames.add(Globals.ind_names[i]);
			}
		}
	}
	public void setItr(int itrVal){
		this.iteration = itrVal;
	}

	//-------------- ADDERS -------------------
	public void addToErrorLog(ArrayList<String> al){
		this.errLog.add(al);
	}
	public void addToActiveIndNames(String ind){
		this.activeIndNum++;
		this.activeIndNames.add(ind);
	}
	public void addToResults(ArrayList<String> al){
		this.results.add(al);
	}
	public void addToResultsD(ArrayList<Double> al){
		ArrayList<String> str = new ArrayList<String>();
		for(int i = 0; i < al.size(); i++){
			str.add(String.valueOf(al.get(i)));
		}
		this.results.add(str);
	}

	//================ CALC SINGLE KEY FUNCTIONS ========================
	/*	0) calcSK() or ...

		0) createTrainDB()
		1) createTestDB()
		2) initSK()
		3) for(sections){  calcSKBySection(i)  }
		4) writeToFileSK()
	*/
	
	//calcs SK and writes all necessary info to multiple files
	public void calcSK(){
		int secSize = 10000;
		String dbsPath = "./../data/ml/ann/cust/db_sizes.txt";
		String trainPath = "./../data/ml/ann/cust/train/";
		String testPath = "./../data/ml/ann/cust/test/";
		//print out basic info
		System.out.println("========== Algo Info ==========");
		if(is_cr_method){
			System.out.println(" --> Method Type: Continuous Range");
		}else{
			System.out.println(" --> Method Type: Binomial Distribution");
		}
		System.out.println(" --> Date Range: ["+sdate+" - "+edate+"]");
		System.out.println(" --> TV Index  : " + tvi);
		System.out.println(" --> Plateau % : " + plateau);
		System.out.println(" --> Ind Mask  : " + indMask);
		System.out.println(" --> MS Mask   : " + msMask);
		System.out.println(" --> NAR Mask  : " + narMask);
		System.out.println("===============================\n");
		//create the cust DB (if relv info is same keep DB)
		ArrayList<ArrayList<String>> cdbs = AhrIO.scanFile(dbsPath, ",");
		boolean keep_db = false;
		if(msMask.equals(cdbs.get(0).get(0)) && indMask.equals(cdbs.get(0).get(1))){
			String tvStr = String.valueOf(tvi);
			boolean is_cont_range;
			if(cdbs.get(0).get(3).equals("0")){
				is_cont_range = true;
			}else{
				is_cont_range = false;
			}
			if(tvStr.equals(cdbs.get(0).get(2)) && is_cr_method == is_cont_range){
				keep_db = true;
			}
		}
		if(true){//TODO: change back to !keep_db
			deleteCustDB();
			createCustDB(secSize);
		}else{
			System.out.println("Using same CUST DB");
		}
		System.out.println("--> Total DB Train Lines    : " + cdbs.get(2).get(0));
		System.out.println("--> Total DB Train Sections : " + cdbs.get(3).get(0));
		System.out.println("--> Total DB Test  Lines    : " + cdbs.get(2).get(1));
		System.out.println("--> Total DB Test  Sections : " + cdbs.get(3).get(1));
		//initialize
		ArrayList<Integer> hiddenDims = new ArrayList<Integer>();
		hiddenDims.add(activeIndNum+2);
		Network ann = new Network(activeIndNum, hiddenDims, 1);
		trainFiles = AhrIO.getFilesInPath(trainPath);
		Collections.sort(trainFiles);
		//ann.printFULL();
		int lcount1 = 0;	//counts lines trained
		int lcount2 = 0;	//counts lines tested
		//itr thru all 
		System.out.println("Itr thru all DB Sections ... ");
		for(int i = 0; i < trainFiles.size(); i++){
			if(i%25==0){System.out.println("   "+i+" out of "+trainFiles.size());}
			String trainFullPath = trainPath+trainFiles.get(i);
			ArrayList<ArrayList<String>> trainFC = AhrIO.scanFile(trainFullPath, "~");
			//train section of DB
			for(int j = 0; j < trainFC.size(); j++){
				//bring data thru ANN and make changes
				double rval = Double.parseDouble(trainFC.get(j).get(trainFC.get(j).size()-1));
				ann.feedForward(trainFC.get(j));
				ann.backpropagation2(learnRate, rval);
				setItr(getItr()+1);
				ann.setItr(ann.getItr()+1);
				lcount1++;
			}
			//test section of DB
			String testFullPath = testPath+trainFiles.get(i);
			File testFile = new File(testFullPath);
			if(testFile.exists()){
				ArrayList<ArrayList<String>> testFC = AhrIO.scanFile(testFullPath, "~");
				ArrayList<String> etLine = new ArrayList<String>();	//error testing line
				etLine.add("sec"+String.valueOf(i));
				double avgError = 0.0;
				for(int j = 0; j < testFC.size(); j++){
					//test the line and get the error of that line
					double thisError = 0.0;
					if(is_cr_method){
						thisError = testLineCR(ann, testFC.get(j));
						avgError += thisError;
					}else{
						thisError = testLineBN(ann, testFC.get(j));
						avgError += thisError;
					}
					//calc error after 1 line
					if(i == 0 && j == 1){
						ArrayList<String> fErrLine = new ArrayList<String>();//first error line
						fErrLine.add("0");
						fErrLine.add("2");
						fErrLine.add("0");
						fErrLine.add(String.valueOf(thisError));
						addToErrorLog(fErrLine);
					}
					//do error testing
					etLine.add(String.valueOf(thisError));
					lcount2++;
				}
				avgError = avgError / testFC.size();	
				//record result progress
				ArrayList<String> errLine = new ArrayList<String>();
				errLine.add(String.valueOf(i+1));
				errLine.add(String.valueOf(lcount1));
				errLine.add(String.valueOf(lcount2));
				errLine.add(String.valueOf(avgError));
				addToErrorLog(errLine);
			}
		}
		double overallError = 0.0;
		ArrayList<ArrayList<String>> errLog = getErrorLog();
		for(int x = 0; x < errLog.size(); x++){
			overallError += Double.parseDouble(errLog.get(x).get(3));
		}
		overallError = overallError / (double)errLog.size();
		//System.out.println("Overall Error Avg: " + overallError);
		
		//OUTPUT: save ANN info to files
		//[1] keys_struct.txt
		ArrayList<ArrayList<String>> keys = AhrIO.scanFile("./../out/ml/ann/keys_struct.txt", ",");
		ArrayList<String> ksHeader = keys.get(0);
		keys.remove(0);	//removes header
		cdbs = AhrIO.scanFile(dbsPath, ",");
		int maxID = -1; 
		for(int i = 0; i < keys.size(); i++){
			if(Integer.parseInt(keys.get(i).get(0)) > maxID){
				maxID = Integer.parseInt(keys.get(i).get(0));
			}
		}
		int sskID = maxID+1;
		int lskID = sskID+1;
		ArrayList<String> sline = new ArrayList<String>();	//short line
		sline.add(String.valueOf(sskID));						//[0] sk_num
		String dbNameCode = "ERR";								//[1] db_used
		if(db_name == "Intrinio"){
			dbNameCode = "IT";
		}else if(db_name == "Yahoo"){
			dbNameCode = "YH";
		}
		sline.add(dbNameCode);
		if(getMethod() == true){								//[2] method
			sline.add("CR");
		}else{
			sline.add("BN");
		}
		sline.add(AhrDate.getTodaysDate());						//[3] date_ran
		sline.add(cdbs.get(1).get(0));							//[4] start_date
		sline.add(cdbs.get(1).get(1));							//[5] end_date
		sline.add("0");											//[6] call
		sline.add(String.valueOf(getLearnRate()));				//[7] learn_rate
		sline.add(String.valueOf(getPlateau()));				//[8] plateau
		sline.add(String.valueOf(getSPD()));					//[9] spd
		sline.add(String.valueOf(getTVI()));					//[10] tvi
		sline.add(getMsMask());									//[11] ms_mask
		sline.add(getIndMask());								//[12] ind_mask
		sline.add(getNarMask());								//[13] nar_mask
		sline.add(String.valueOf(overallError));				//[14] avg_error
		ArrayList<String> lline = new ArrayList<String>(sline);
		lline.set(0, String.valueOf(lskID));
		lline.set(6, "1");
		keys.add(sline);
		keys.add(lline);
		keys.add(0, ksHeader);
		AhrIO.writeToFile("./../out/ml/ann/keys_struct.txt", keys, ",");
		//[2] struct_xxx.txt
		ArrayList<ArrayList<String>> tfStruct = new ArrayList<ArrayList<String>>();
		String fpath = "./../out/ml/ann/structure/struct_"+String.valueOf(id)+".txt";
		for(int i = 0; i < ann.getTotalLayers(); i++){
			ArrayList<String> strLine = new ArrayList<String>();
			if(i == 0){//input layer
				ArrayList<Node> layer = ann.getLayer(i);
				strLine.add("0");
				strLine.add(String.valueOf(layer.size()));
				tfStruct.add(strLine);
			}else{//hidden & output layers
				ArrayList<Node> layer = ann.getLayer(i);
				for(int j = 0; j < layer.size(); j++){
					strLine = new ArrayList<String>();
					strLine.add(String.valueOf(layer.get(j).getLayerID()));
					strLine.add(String.valueOf(layer.get(j).getNodeID()));
					strLine.add(String.valueOf(layer.get(j).getValue()));
					strLine.add(String.valueOf(layer.get(j).getBias()));
					String wgts = "";
					for(int k = 0; k < layer.get(j).getWeights().size(); k++){
						if(k != (layer.get(j).getWeights().size()-1)){
							wgts += String.valueOf(layer.get(j).getWeights().get(k)) + "~";
						}else{
							wgts += String.valueOf(layer.get(j).getWeights().get(k));
						}
					}
					strLine.add(wgts);
					tfStruct.add(strLine);
				}
			}
		}
		AhrIO.writeToFile("./../out/ml/ann/structure/struct_"+id+".txt", tfStruct, ",");
		AhrIO.writeToFile("./../out/ml/ann/structure/struct_"+(id+1)+".txt", tfStruct, ",");
		//[3] write to file err log : err_xxx.txt
		AhrIO.writeToFile("./../out/ml/ann/error/err_"+id+".txt", getErrorLog(), ",");
		AhrIO.writeToFile("./../out/ml/ann/error/err_"+(id+1)+".txt", getErrorLog(), ",");
		//[4] keys_perf.txt
		ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile("./../out/ml/ann/keys_perf.txt", ",");
		sline = new ArrayList<String>();
		sline.add(String.valueOf(sskID));					//[0] sk_num
		sline.add("0");										//[1] call
		sline.add(String.valueOf(getSPD()));				//[2] spd
		sline.add(String.valueOf(getTVI()));				//[3] tvi
		sline.add(getMsMask());								//[4] ms_mask
		sline.add(getNarMask());							//[5] nar_mask
		sline.add("ph");									//[6] bim
		sline.add("ph");									//[7] som
		sline.add("ph");									//[8] bso_train_apapt
		sline.add("ph");									//[9] bso_test_apapt
		sline.add("ph");									//[10] bso_train_posp
		sline.add("ph");									//[11] bso_test_posp
		sline.add("ph");									//[12] plat_train_apapt
		sline.add("ph");									//[13] plat_test_apapt
		sline.add("ph");									//[14] true_train_apapt
		sline.add("ph");									//[15] true_test_apapt
		sline.add("ph");									//[16] true_train_posp
		sline.add("ph");									//[17] true_test_posp
		lline = new ArrayList<String>(sline);
		lline.set(0, String.valueOf(lskID));
		lline.set(1, "1");

		System.out.println("\n========== Algo Perf ==========");
		System.out.println(" --> Long PAPAPT  (even) : "+lline.get(11)+"\n"+
						   " --> Long TAPAPT  (even) : "+lline.get(13)+"\n"+
						   " --> Long Pos %   (even) : "+lline.get(15)+"\n"+
						   " --> Short PAPAPT (even) : "+sline.get(11)+"\n"+
						   " --> Short TAPAPT (even) : "+sline.get(13)+"\n"+
						   " --> Short Pos %  (even) : "+sline.get(15)+"\n"+
						   " --> Long PAPAPT  (odd)  : "+lline.get(12)+"\n"+
						   " --> Long TAPAPT  (odd)  : "+lline.get(14)+"\n"+
						   " --> Long Pos %   (odd)  : "+lline.get(16)+"\n"+
						   " --> Short PAPAPT (odd)  : "+sline.get(12)+"\n"+
						   " --> Short TAPAPT (odd)  : "+sline.get(14)+"\n"+
						   " --> Short Pos %  (odd)  : "+sline.get(16)+"\n"+
						   "===============================\n");
		kpFile.add(sline);
		kpFile.add(lline);
		AhrIO.writeToFile("./../out/ml/ann/keys_perf.txt", kpFile, ",");

		//Done
		System.out.println("Output Files WRITTEN");		
	}

	//do just the init part of SK
	public void initSK(){
		//create custom DB that fit params
		//int secSize = 10000;
		//deleteCustDB();
		//createCustDB(secSize);
		//setup struct of ANN network and tracker vars
		String trainPath = "./../data/ml/ann/cust/train/";
		this.trainLineCount = 0;
		this.testLineCount = 0;
		ArrayList<Integer> hiddenDims = new ArrayList<Integer>();
		System.out.println("--> activeIndNum = " + this.activeIndNum);
		hiddenDims.add(this.activeIndNum+2);
		this.network = new Network(this.activeIndNum, hiddenDims, 1);
		this.trainFiles = AhrIO.getFilesInPath(trainPath);
		Collections.sort(this.trainFiles);
		//calc ID numbers for short and long
		String ksPath = "./../out/ml/ann/keys_struct.txt";
		FCI fciKS = new FCI(true, ksPath);
		ArrayList<ArrayList<String>> ksFile = AhrIO.scanFile(ksPath, ",");
		int maxID = -1;
		if(ksFile.size() > 0){
			ksFile.remove(0);
			for(int i = 0; i < ksFile.size(); i++){
				if(Integer.parseInt(ksFile.get(i).get(0)) > maxID){
					maxID = Integer.parseInt(ksFile.get(i).get(0));
				}
			}
		}
		setID(maxID + 1);
		System.out.println("--> Init for SK"+getID()+" ... DONE");
	}

	//calc 1 section of SK at a time
	public void calcSKBySection(int sectionNum){
		String trainPath = "./../data/ml/ann/cust/train/";
		String testPath = "./../data/ml/ann/cust/test/";
		String trainFullPath = trainPath+trainFiles.get(sectionNum);
		ArrayList<ArrayList<String>> trainFC = AhrIO.scanFile(trainFullPath, "~");
		//train section of DB
		for(int i = 0; i < trainFC.size(); i++){
			//bring data thru ANN and make changes
			double rval = Double.parseDouble(trainFC.get(i).get(trainFC.get(i).size()-1));
			this.network.feedForward(trainFC.get(i));
			this.network.backpropagation2(learnRate, rval);
			setItr(getItr()+1);
			this.network.setItr(this.network.getItr()+1);
			this.trainLineCount++;
		}
		//test section of DB, calc error
		String testFullPath = testPath+trainFiles.get(sectionNum);
		File testFile = new File(testFullPath);
		if(testFile.exists()){
			ArrayList<ArrayList<String>> testFC = AhrIO.scanFile(testFullPath, "~");
			ArrayList<String> etLine = new ArrayList<String>();	//error testing line
			etLine.add("sec"+String.valueOf(sectionNum));
			double avgError = 0.0;
			for(int i = 0; i < testFC.size(); i++){
				//test the line and get the error of that line
				double itrError = 0.0;
				if(is_cr_method){
					itrError = testLineCR(this.network, testFC.get(i));
					avgError += itrError;
				}else{
					itrError = testLineBN(this.network, testFC.get(i));
					avgError += itrError;
				}
				//calc error after 1 line
				if(sectionNum == 0 && i == 1){
					ArrayList<String> fErrLine = new ArrayList<String>();//first error line
					fErrLine.add("0");
					fErrLine.add("2");
					fErrLine.add("0");
					fErrLine.add(String.valueOf(itrError));
					addToErrorLog(fErrLine);
				}
				//do error testing
				etLine.add(String.valueOf(itrError));
				this.testLineCount++;
			}
			avgError = avgError / testFC.size();	
			//record result progress
			ArrayList<String> errLine = new ArrayList<String>();
			errLine.add(String.valueOf(sectionNum+1));
			errLine.add(String.valueOf(this.trainLineCount));
			errLine.add(String.valueOf(this.testLineCount));
			errLine.add(String.valueOf(avgError));
			addToErrorLog(errLine);
		}
	}

	//write to file all ANN output files after SK is calced
	public void writeToFileSK(){
		//calc overall avg error from error log
		double overallError = 0.0;
		ArrayList<ArrayList<String>> errLog = getErrorLog();
		for(int x = 0; x < errLog.size(); x++){
			overallError += Double.parseDouble(errLog.get(x).get(3));
		}
		overallError = overallError / (double)errLog.size();
		//[1] keys_struct.txt
		ArrayList<ArrayList<String>> ksFile = AhrIO.scanFile("./../out/ml/ann/keys_struct.txt", ",");
		ArrayList<String> ksHeader = ksFile.get(0);
		ksFile.remove(0);	//removes header
		ArrayList<ArrayList<String>> cdbs = AhrIO.scanFile("./../data/ml/ann/cust/db_sizes.txt", ",");
		int maxID = -1; 
		for(int i = 0; i < ksFile.size(); i++){
			if(Integer.parseInt(ksFile.get(i).get(0)) > maxID){
				maxID = Integer.parseInt(ksFile.get(i).get(0));
			}
		}
		int sskID = maxID+1;
		int lskID = sskID+1;
		//short call line
		ArrayList<String> sline = new ArrayList<String>();
		sline.add(String.valueOf(sskID));						//[0] sk_num
		String dbNameCode = "ERR";								//[1] db_used
		if(db_name == "Intrinio"){
			dbNameCode = "IT";
		}else if(db_name == "Yahoo"){
			dbNameCode = "YH";
		}
		sline.add(dbNameCode);
		if(getMethod() == true){								//[2] method
			sline.add("CR");
		}else{
			sline.add("BN");
		}
		sline.add(AhrDate.getTodaysDate());						//[3] date_ran
		sline.add(cdbs.get(1).get(0));							//[4] start_date
		sline.add(cdbs.get(1).get(1));							//[5] end_date
		sline.add("0");											//[6] call
		sline.add(String.valueOf(getLearnRate()));				//[7] learn_rate
		sline.add(String.valueOf(getPlateau()));				//[8] plateau
		sline.add(String.valueOf(getSPD()));					//[9] spd
		sline.add(String.valueOf(getTVI()));					//[10] tvi
		sline.add(getMsMask());									//[11] ms_mask
		sline.add(getIndMask());								//[12] ind_mask
		sline.add(getNarMask());								//[13] nar_mask
		sline.add(String.valueOf(overallError));				//[14] avg_error
		//long call line
		ArrayList<String> lline = new ArrayList<String>(sline);
		lline.set(0, String.valueOf(lskID));
		lline.set(6, "1");
		//add both lines to keys_struct.txt
		ksFile.add(sline);
		ksFile.add(lline);
		ksFile.add(0, ksHeader);
		AhrIO.writeToFile("./../out/ml/ann/keys_struct.txt", ksFile, ",");
		//[2] struct_xxx.txt
		ArrayList<ArrayList<String>> tfStruct = new ArrayList<ArrayList<String>>();
		String fpath = "./../out/ml/ann/structure/struct_"+String.valueOf(id)+".txt";
		for(int i = 0; i < this.network.getTotalLayers(); i++){
			ArrayList<String> strLine = new ArrayList<String>();
			if(i == 0){//input layer
				ArrayList<Node> layer = this.network.getLayer(i);
				strLine.add("0");
				strLine.add(String.valueOf(layer.size()));
				tfStruct.add(strLine);
			}else{//hidden & output layers
				ArrayList<Node> layer = this.network.getLayer(i);
				for(int j = 0; j < layer.size(); j++){
					strLine = new ArrayList<String>();
					strLine.add(String.valueOf(layer.get(j).getLayerID()));
					strLine.add(String.valueOf(layer.get(j).getNodeID()));
					strLine.add(String.valueOf(layer.get(j).getValue()));
					strLine.add(String.valueOf(layer.get(j).getBias()));
					String wgts = "";
					for(int k = 0; k < layer.get(j).getWeights().size(); k++){
						if(k != (layer.get(j).getWeights().size()-1)){
							wgts += String.valueOf(layer.get(j).getWeights().get(k)) + "~";
						}else{
							wgts += String.valueOf(layer.get(j).getWeights().get(k));
						}
					}
					strLine.add(wgts);
					tfStruct.add(strLine);
				}
			}
		}
		AhrIO.writeToFile("./../out/ml/ann/structure/struct_"+id+".txt", tfStruct, ",");
		AhrIO.writeToFile("./../out/ml/ann/structure/struct_"+(id+1)+".txt", tfStruct, ",");
		//[3] write to file err log : err_xxx.txt
		AhrIO.writeToFile("./../out/ml/ann/error/err_"+id+".txt", getErrorLog(), ",");
		AhrIO.writeToFile("./../out/ml/ann/error/err_"+(id+1)+".txt", getErrorLog(), ",");
		//[4] keys_perf.txt
		ArrayList<ArrayList<String>> kpFile = AhrIO.scanFile("./../out/ml/ann/keys_perf.txt", ",");
		sline = new ArrayList<String>();
		sline.add(String.valueOf(sskID));					//[0] sk_num
		sline.add("0");										//[1] call
		sline.add(String.valueOf(getSPD()));				//[2] spd
		sline.add(String.valueOf(getTVI()));				//[3] tvi
		sline.add(getMsMask());								//[4] ms_mask
		sline.add(getNarMask());							//[5] nar_mask
		sline.add("ph");									//[6] bim
		sline.add("ph");									//[7] som
		sline.add("ph");									//[8] bso_train_apapt
		sline.add("ph");									//[9] bso_test_apapt
		sline.add("ph");									//[10] bso_train_posp
		sline.add("ph");									//[11] bso_test_posp
		sline.add("ph");									//[12] plat_train_apapt
		sline.add("ph");									//[13] plat_test_apapt
		sline.add("ph");									//[14] true_train_apapt
		sline.add("ph");									//[15] true_test_apapt
		sline.add("ph");									//[16] true_train_posp
		sline.add("ph");									//[17] true_test_posp
		lline = new ArrayList<String>(sline);
		lline.set(0, String.valueOf(lskID));
		lline.set(1, "1");
		kpFile.add(sline);
		kpFile.add(lline);
		AhrIO.writeToFile("./../out/ml/ann/keys_perf.txt", kpFile, ",");
	}
	
	//creates the cust DB for ANN (each file will have secSize*2 length)
	public void createCustDB(int secSize){
		System.out.print("--> Creating Custom DB ... ");

		ArrayList<String> dates = AhrDate.getDatesBetween(sdate, edate);
		FCI fciMS = new FCI(false, "./../in/mstates.txt");
		ArrayList<ArrayList<String>> mstates = AhrIO.scanFile("./../in/mstates.txt", ",");
		ArrayList<String> evenDates = new ArrayList<String>();
		ArrayList<String> oddDates = new ArrayList<String>();
		int evenLines = 0;
		int oddLines = 0;
		int evenSections = 0;
		int oddSections = 0;
		//put all MS Dates in AL for easier idx finding
		ArrayList<String> msDates = new ArrayList<String>();
		for(int i = 0; i < mstates.size(); i++){
			msDates.add(mstates.get(i).get(fciMS.getIdx("date")));
		}
		//itr thru all dates in train/test range and split dates into even and odd
		for(int i = 0; i < dates.size(); i++){
			int msIdx = msDates.indexOf(dates.get(i)); 
			String itrMask = mstates.get(msIdx).get(fciMS.getIdx("ms_mask"));
			//check date, see if it matches the MS
			if(AhrGen.compareMasks(msMask, itrMask)){
				if(Integer.parseInt(dates.get(i).split("-")[2]) % 2 == 0){	//is even date
					evenDates.add(dates.get(i));
				}else{														//is odd date
					oddDates.add(dates.get(i));
				}			
			}
		}

		//create the sections and write them to file
		ArrayList<ArrayList<String>> tf = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < evenDates.size(); i++){
			//get only clean lines that pass NAR
			String bdBasePath = "./../../DB_"+db_name+"/Clean/ByDate/";
			FCI fciBD = new FCI(false, bdBasePath);
			ArrayList<ArrayList<String>> allClean = AhrIO.scanFile(bdBasePath+evenDates.get(i)+".txt", "~");		
			ArrayList<ArrayList<String>> narClean = new ArrayList<ArrayList<String>>();
			for(int j = 0; j < allClean.size(); j++){
				String narItr = allClean.get(j).get(fciBD.getIdx("nar_mask"));
				while(narMask.length() < narItr.length()){
					narMask += "x";
				}
				if(AhrGen.compareMasks(narMask, narItr)){
					narClean.add(allClean.get(j));
				}
			}
			evenLines += narClean.size();
			//itr thru all clean lines that pass NAR
			for(int j = 0; j < narClean.size(); j++){
				String apprCol = "appr"+fciBD.convertTVI(tvi);
				if(!narClean.get(j).get(fciBD.getIdx(apprCol)).equals("tbd")){	
					ArrayList<String> line = new ArrayList<String>();
					//add predictor variables
					for(int k = 0; k < indMask.length(); k++){
						if(indMask.charAt(k) == '1'){
							String indCol = "ind"+String.valueOf(k);
							double pval = Double.parseDouble(narClean.get(j).get(fciBD.getIdx(indCol)));
							pval = pval * (1.0/65535.0);	//normalize to range [0,1]
							String pstr = String.format("%.8f", pval);
							line.add(pstr);
						}
					}
					//add target variable
					if(is_cr_method){
						double platAppr = Double.parseDouble(narClean.get(j).get(fciBD.getIdx(apprCol)));
						if(platAppr > plateau){
							platAppr = plateau;
						}
						if(platAppr < (-1.0*plateau)){
							platAppr = (-1.0*plateau);
						}
						double range = plateau * 2.0;
						double azAppr = platAppr + plateau;	//above zero
						double tvNorm = azAppr * (1.0 / range);
						line.add(String.format("%.6f", tvNorm));
					}else{//is binomial distribution
						String mode = "0";
						if(Double.parseDouble(narClean.get(j).get(fciBD.getIdx(apprCol))) > 0){
							mode = "1";
						}
						line.add(mode);
					}		
					//add line and write to file (if at limit)
					String tfTrainPath = "./../data/ml/ann/cust/train/sec"+String.valueOf(evenSections)+".txt";
					tf.add(line);
					if(tf.size() >= secSize){
						AhrIO.writeToFile(tfTrainPath, tf, "~");
						tf = new ArrayList<ArrayList<String>>();
						evenSections++;
					}else{
						if(i == (evenDates.size()-1) && j == (narClean.size()-1)){//last line, add section
							AhrIO.writeToFile(tfTrainPath, tf, "~");
							tf = new ArrayList<ArrayList<String>>();
							evenSections++;
						}
					}
				}
			}
		}
		//itr thru all odd dates
		for(int i = 0; i < oddDates.size(); i++){
			//get only clean lines that pass NAR
			String bdPath = "./../../DB_"+db_name+"/Clean/ByDate/";
			FCI fciBD = new FCI(false, bdPath);
			ArrayList<ArrayList<String>> allClean = AhrIO.scanFile(bdPath+oddDates.get(i)+".txt", "~");		
			ArrayList<ArrayList<String>> narClean = new ArrayList<ArrayList<String>>();
			for(int j = 0; j < allClean.size(); j++){
				String narItr = allClean.get(j).get(fciBD.getIdx("nar_mask"));
				while(narMask.length() < narItr.length()){
					narMask += "x";
				}
				if(AhrGen.compareMasks(narMask, narItr)){
					narClean.add(allClean.get(j));
				}
			}
			oddLines += narClean.size();
			//itr thru all clean lines that pass NAR
			for(int j = 0; j < narClean.size(); j++){
				String apprCol = "appr"+fciBD.convertTVI(tvi);
				if(!narClean.get(j).get(fciBD.getIdx(apprCol)).equals("tbd")){
					ArrayList<String> line = new ArrayList<String>();
					//add predictor variables
					for(int k = 0; k < indMask.length(); k++){
						if(indMask.charAt(k) == '1'){
							String indCol = "ind"+String.valueOf(k);
							double pval = Double.parseDouble(narClean.get(j).get(fciBD.getIdx(indCol)));
							pval = pval * (1.0/65535.0);	//normalize to range [0,1]
							String pstr = String.format("%.8f", pval);
							line.add(pstr);
						}
					}
					//add target variable
					if(is_cr_method){
						double platAppr = Double.parseDouble(narClean.get(j).get(fciBD.getIdx(apprCol)));
						if(platAppr > plateau){
							platAppr = plateau;
						}
						if(platAppr < (-1.0*plateau)){
							platAppr = (-1.0*plateau);
						}
						double range = plateau * 2.0;
						double azAppr = platAppr + plateau;	//above zero
						double tvNorm = azAppr * (1.0 / range);
						line.add(String.format("%.8f", tvNorm));
					}else{//is binomial distribution
						String mode = "0";
						if(Double.parseDouble(narClean.get(j).get(fciBD.getIdx(apprCol))) > 0){
							mode = "1";
						}
						line.add(mode);
					}			
					//add line and write to file (if at limit)
					String tfTestPath = "./../data/ml/ann/cust/test/sec"+String.valueOf(oddSections)+".txt";
					tf.add(line);
					if(tf.size() >= secSize){
						AhrIO.writeToFile(tfTestPath, tf, "~");
						tf = new ArrayList<ArrayList<String>>();
						oddSections++;
					}else{
						if(i == (oddDates.size()-1) && j == (narClean.size()-1)){//last line, add section
							AhrIO.writeToFile(tfTestPath, tf, "~");
							tf = new ArrayList<ArrayList<String>>();
							oddSections++;
						}
					}
				}
			}
		}
		System.out.println("DONE");
		//write info to file
		ArrayList<ArrayList<String>> toFile = new ArrayList<ArrayList<String>>();
		ArrayList<String> line1 = new ArrayList<String>();
		line1.add(msMask);										//[0, 0] MS Mask
		line1.add(indMask);										//[0, 1] Ind Mask
		line1.add(String.valueOf(tvi));							//[0, 2] Target Var Idx
		if(is_cr_method){										//[0, 3] 1 = continuous target
			line1.add("1");										//       0 = binomial target
		}else{
			line1.add("0");
		}
		ArrayList<String> line2 = new ArrayList<String>();
		line2.add(sdate);										//[1, 1] Start Date
		line2.add(edate);										//[1, 2] End Date
		ArrayList<String> line3 = new ArrayList<String>();
		line3.add(String.valueOf(evenLines));					//[2, 1] # of Train Lines
		line3.add(String.valueOf(oddLines));					//[2, 2] # of Test Lines
		ArrayList<String> line4 = new ArrayList<String>();
		line4.add(String.valueOf(evenSections));				//[3, 1] # of Train Sections
		line4.add(String.valueOf(oddSections));					//[3, 2] # of Test Sections
		toFile.add(line1);
		toFile.add(line2);
		toFile.add(line3);
		toFile.add(line4);
		AhrIO.writeToFile("./../data/ml/ann/cust/db_sizes.txt", toFile, ",");
	}
	//creater only the train DB
	public void createTrainDB(int secSize){
		System.out.print("--> Creating Custom Train DB ... ");
		//read in mstates info
		String msPath = "./../in/mstates.txt";
		FCI fciMS = new FCI(false, msPath);
		ArrayList<ArrayList<String>> mstates = AhrIO.scanFile(msPath, ",");
		ArrayList<String> msDates = AhrAL.getCol(mstates, fciMS.getIdx("date"));
		//itr thru all dates in range and get all even dates
		ArrayList<String> dates = AhrDate.getDatesBetween(this.sdate, this.edate);
		ArrayList<String> evenDates = new ArrayList<String>();
		for(int i = 0; i < dates.size(); i++){
			int msIdx = msDates.indexOf(dates.get(i)); 
			String itrMask = mstates.get(msIdx).get(fciMS.getIdx("ms_mask"));
			//check date, see if it matches the MS
			if(AhrGen.compareMasks(msMask, itrMask)){
				if(Integer.parseInt(dates.get(i).split("-")[2]) % 2 == 0){	//is even date
					evenDates.add(dates.get(i));
				}			
			}
		}
		//create the sections and write them to file
		int evenLines = 0;
		int evenSections = 0;
		ArrayList<ArrayList<String>> tf = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < evenDates.size(); i++){
			//get only clean lines that pass NAR
			String bdBasePath = "./../../DB_"+this.db_name+"/Clean/ByDate/";
			FCI fciBD = new FCI(false, bdBasePath);
			ArrayList<ArrayList<String>> allClean = AhrIO.scanFile(bdBasePath+evenDates.get(i)+".txt", "~");		
			ArrayList<ArrayList<String>> narClean = new ArrayList<ArrayList<String>>();
			for(int j = 0; j < allClean.size(); j++){
				String narItr = allClean.get(j).get(fciBD.getIdx("nar_mask"));
				while(narMask.length() < narItr.length()){
					narMask += "x";
				}
				if(AhrGen.compareMasks(narMask, narItr)){
					narClean.add(allClean.get(j));
				}
			}
			evenLines += narClean.size();
			//itr thru all clean lines that pass NAR
			for(int j = 0; j < narClean.size(); j++){
				String apprCol = "appr"+fciBD.convertTVI(tvi);
				if(!narClean.get(j).get(fciBD.getIdx(apprCol)).equals("tbd")){	
					ArrayList<String> line = new ArrayList<String>();
					//add predictor variables
					for(int k = 0; k < this.indMask.length(); k++){
						if(this.indMask.charAt(k) == '1'){
							String indCol = "ind"+String.valueOf(k);
							double pval = Double.parseDouble(narClean.get(j).get(fciBD.getIdx(indCol)));
							pval = pval * (1.0/65535.0);	//normalize to range [0,1]
							String pstr = String.format("%.8f", pval);
							line.add(pstr);
						}
					}
					//add target variable
					if(is_cr_method){
						double platAppr = 0.0;
						try{
							platAppr = Double.parseDouble(narClean.get(j).get(fciBD.getIdx(apprCol)));
						}catch(NumberFormatException e){
							System.out.println("ERR: " + e.getMessage());
						}
						if(platAppr > plateau){
							platAppr = plateau;
						}
						if(platAppr < (-1.0*plateau)){
							platAppr = (-1.0*plateau);
						}
						double range = plateau * 2.0;
						double azAppr = platAppr + plateau;	//above zero
						double tvNorm = azAppr * (1.0 / range);
						line.add(String.format("%.6f", tvNorm));
					}else{//is binomial distribution
						String mode = "0";
						double appr = 0.0;
						try{
							appr = Double.parseDouble(narClean.get(j).get(fciBD.getIdx(apprCol)));
						}catch(NumberFormatException e){
							System.out.println("ERR: " + e.getMessage());
						}
						if(appr > 0){
							mode = "1";
						}
						line.add(mode);
					}
					//add line and write to file (if at limit)
					String tfTrainPath = "./../data/ml/ann/cust/train/sec"+String.valueOf(evenSections)+".txt";
					tf.add(line);
					if(tf.size() >= secSize){
						AhrIO.writeToFile(tfTrainPath, tf, "~");
						tf = new ArrayList<ArrayList<String>>();
						evenSections++;
					}else{
						if(i == (evenDates.size()-1) && j == (narClean.size()-1)){//last line, add section
							AhrIO.writeToFile(tfTrainPath, tf, "~");
							tf = new ArrayList<ArrayList<String>>();
							evenSections++;
						}
					}
				}
			}
		}
		//write info to file
		ArrayList<ArrayList<String>> dbSizes = new ArrayList<ArrayList<String>>();
		ArrayList<String> line1 = new ArrayList<String>();
		line1.add(msMask);										//[0, 0] MS Mask
		line1.add(indMask);										//[0, 1] Ind Mask
		line1.add(String.valueOf(tvi));							//[0, 2] Target Var Idx
		if(is_cr_method){										//[0, 3] 1 = continuous target
			line1.add("1");										//       0 = binomial target
		}else{
			line1.add("0");
		}
		ArrayList<String> line2 = new ArrayList<String>();
		line2.add(sdate);										//[1, 1] Start Date
		line2.add(edate);										//[1, 2] End Date
		ArrayList<String> line3 = new ArrayList<String>();
		line3.add(String.valueOf(evenLines));					//[2, 1] # of Train Lines
		line3.add("ph");										//[2, 2] # of Test Lines
		ArrayList<String> line4 = new ArrayList<String>();
		line4.add(String.valueOf(evenSections));				//[3, 1] # of Train Sections
		line4.add("ph");										//[3, 2] # of Test Sections
		dbSizes.add(line1);
		dbSizes.add(line2);
		dbSizes.add(line3);
		dbSizes.add(line4);
		AhrIO.writeToFile("./../data/ml/ann/cust/db_sizes.txt", dbSizes, ",");
		System.out.println("DONE");
	}
	//create only the test DB
	public void createTestDB(int secSize){
		System.out.print("--> Creating Custom Test DB ... ");
		//read in mstates info
		String msPath = "./../in/mstates.txt";
		FCI fciMS = new FCI(false, msPath);
		ArrayList<ArrayList<String>> mstates = AhrIO.scanFile(msPath, ",");
		ArrayList<String> msDates = AhrAL.getCol(mstates, fciMS.getIdx("date"));
		//itr thru all dates in range and get all even dates
		ArrayList<String> dates = AhrDate.getDatesBetween(this.sdate, this.edate);
		ArrayList<String> oddDates = new ArrayList<String>();
		for(int i = 0; i < dates.size(); i++){
			int msIdx = msDates.indexOf(dates.get(i)); 
			String itrMask = mstates.get(msIdx).get(fciMS.getIdx("ms_mask"));
			//check date, see if it matches the MS
			if(AhrGen.compareMasks(msMask, itrMask)){
				if(Integer.parseInt(dates.get(i).split("-")[2]) % 2 != 0){	//is odd date
					oddDates.add(dates.get(i));
				}			
			}
		}
		//create the sections and write them to file
		int oddLines = 0;
		int oddSections = 0;
		ArrayList<ArrayList<String>> tf = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < oddDates.size(); i++){
			//get only clean lines that pass NAR
			String bdPath = "./../../DB_"+this.db_name+"/Clean/ByDate/";
			FCI fciBD = new FCI(false, bdPath);
			ArrayList<ArrayList<String>> allClean = AhrIO.scanFile(bdPath+oddDates.get(i)+".txt", "~");		
			ArrayList<ArrayList<String>> narClean = new ArrayList<ArrayList<String>>();
			for(int j = 0; j < allClean.size(); j++){
				String narItr = allClean.get(j).get(fciBD.getIdx("nar_mask"));
				while(narMask.length() < narItr.length()){
					narMask += "x";
				}
				if(AhrGen.compareMasks(narMask, narItr)){
					narClean.add(allClean.get(j));
				}
			}
			oddLines += narClean.size();
			//itr thru all clean lines that pass NAR
			for(int j = 0; j < narClean.size(); j++){
				String apprCol = "appr"+fciBD.convertTVI(tvi);
				if(!narClean.get(j).get(fciBD.getIdx(apprCol)).equals("tbd")){
					ArrayList<String> line = new ArrayList<String>();
					//add predictor variables
					for(int k = 0; k < this.indMask.length(); k++){
						if(this.indMask.charAt(k) == '1'){
							String indCol = "ind"+String.valueOf(k);
							double pval = Double.parseDouble(narClean.get(j).get(fciBD.getIdx(indCol)));
							pval = pval * (1.0/65535.0);	//normalize to range [0,1]
							String pstr = String.format("%.8f", pval);
							line.add(pstr);
						}
					}
					//add target variable
					if(is_cr_method){
						double platAppr = 0.0;
						try{
							platAppr = Double.parseDouble(narClean.get(j).get(fciBD.getIdx(apprCol)));
						}catch(NumberFormatException e){
							System.out.println("ERR: " + e.getMessage());
						}
						if(platAppr > plateau){
							platAppr = plateau;
						}
						if(platAppr < (-1.0*plateau)){
							platAppr = (-1.0*plateau);
						}
						double range = plateau * 2.0;
						double azAppr = platAppr + plateau;	//above zero
						double tvNorm = azAppr * (1.0 / range);
						line.add(String.format("%.8f", tvNorm));
					}else{//is binomial distribution
						String mode = "0";
						double appr = 0.0;
						try{
							appr = Double.parseDouble(narClean.get(j).get(fciBD.getIdx(apprCol)));
						}catch(NumberFormatException e){
							System.out.println("ERR: " + e.getMessage());
						}
						if(appr > 0){
							mode = "1";
						}
						line.add(mode);
					}			
					//add line and write to file (if at limit)
					String tfTestPath = "./../data/ml/ann/cust/test/sec"+String.valueOf(oddSections)+".txt";
					tf.add(line);
					if(tf.size() >= secSize){
						AhrIO.writeToFile(tfTestPath, tf, "~");
						tf = new ArrayList<ArrayList<String>>();
						oddSections++;
					}else{
						if(i == (oddDates.size()-1) && j == (narClean.size()-1)){//last line, add section
							AhrIO.writeToFile(tfTestPath, tf, "~");
							tf = new ArrayList<ArrayList<String>>();
							oddSections++;
						}
					}
				}
			}
		}
		//add test info to db_sizes.txt file
		String dbsPath = "./../data/ml/ann/cust/db_sizes.txt";
		ArrayList<ArrayList<String>> dbSizes = AhrIO.scanFile(dbsPath, ",");
		dbSizes.get(2).set(1, String.valueOf(oddLines));
		dbSizes.get(3).set(1, String.valueOf(oddSections));
		AhrIO.writeToFile(dbsPath, dbSizes, ",");
		System.out.println("DONE");
	}
	//delete all files in custom made DB
	public void deleteCustDB(){
		String path = "./../data/ml/ann/cust/train/";
		ArrayList<String> fnames = AhrIO.getFilesInPath(path);
		for(int i = 0; i < fnames.size(); i++){
			File file = new File(path+fnames.get(i));
			file.delete();
		}
		path = "./../data/ml/ann/cust/test/";
		fnames = AhrIO.getFilesInPath(path);
		for(int i = 0; i < fnames.size(); i++){
			File file = new File(path+fnames.get(i));
			file.delete();
		}
	}

	//========== Testing and Scoring Lines ==========

	//get output of an ANN given an input line,  w/o changing ANN [Cont Range]
	public double scoreLineCR(Network network, ArrayList<String> dataLine){
		double score = 0.0;
		Network snet = network;	//static ANN, so no change happens to param network
		snet.feedForward(dataLine);
		//tally score from output layer
		for(int i = 0; i < snet.outputLayer.size(); i++){
			score += snet.outputLayer.get(i).getValue();
		}
		return score;
	}
	//test line in DB w/o changing the ANN [Cont Range]
	public double testLineCR(Network network, ArrayList<String> dataLine){
		Network snet = network;	//static ANN
		int totOutputs = dataLine.size() - snet.inputLayer.size();
		double avgError = 0.0;
		//convert data line from string to double
		ArrayList<Double> dline = new ArrayList<Double>();
		for(int i = 0; i < dataLine.size(); i++){
			dline.add(Double.parseDouble(dataLine.get(i)));
		}
		snet.feedForward(dataLine);
		for(int i = 0; i < totOutputs; i++){
			double rval = dline.get(snet.inputLayer.size()+i);		//real value
			double cval = snet.outputLayer.get(i).getValue();		//calculated value
			avgError += Math.abs(rval - cval);
			if(Math.abs(rval - cval) > 1.0){
				System.out.println("Real Value: " + rval + "  |  Calc Value: " + cval);
			}
		}
		avgError = avgError / totOutputs;
		return avgError;
	}
	
	//get output of an ANN given an input line, w/o changing ANN [Binomial]
	public double scoreLineBN(Network network, ArrayList<String> dataLine){
		double score = 0.0;
		return score;
	}
	//get error in line w/o changing the ANN [Binomial]
	public double testLineBN(Network network, ArrayList<String> dataLine){
		double avgError = 0.0;
		return avgError;
	}

}
