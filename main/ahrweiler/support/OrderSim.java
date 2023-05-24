package ahrweiler.support;
import ahrweiler.Globals;
import ahrweiler.util.*;
import ahrweiler.support.FCI;
import java.io.*;
import java.util.*;
import java.lang.Math;
import javax.swing.JOptionPane;

public class OrderSim {	//simulates ordering in reality 

	int keyNum;
	String bfPath = "";		//basis file path
	FCI fciBF;				//FCI that changes according to given basis path
	String sdate = "";
	String edate = "";
	double bim = 0.0;		//buy-in mult (of last close)
	double som = 0.0;		//sell-off mult (of buy-in)
	String ttvMask = "111";	//train test verify bit mask
	ArrayList<ArrayList<ArrayList<String>>> techBuf;
	int techLen = 0;
	//ML algo related vars
	boolean is_long;
	String dbUsed = "";
	int tvi = -1;
	double cmult = 10.0;
	double principal = 100000;		//def start value of a portfolio
	double maxOrderPrice = 10000;	//upper limit (in $) of any 1 order
	//calculations and results
	ArrayList<ArrayList<String>> orders;
	double posPer = 0.0;		//% of triggered orders that are positive
	double bimPer = 0.0;		//BIM percent triggered
	double somPer = 0.0;		//SOM percent triggered
	double tpr = 0.0;			//throughput rate (# of compounds/year) 
	double trigAppr = 0.0;		//% appr of triggered lines
	double secAppr = 0.0;		//tot % appr of whole date range given
	double yoyAppr = 0.0;		//year-over-year % appr
	boolean ismt = false;		//min-max true appr (false = min true appr)

	//constructors
	public OrderSim(){
		this.techBuf = new ArrayList<ArrayList<ArrayList<String>>>();
		this.orders = new ArrayList<ArrayList<String>>();
		this.is_long = true;
		this.cmult = 1.0;
		this.tvi = 1;	
	}
	public OrderSim(String basisPath){//empty init
		basisPath = AhrIO.uniPath(basisPath);
		this.bfPath = basisPath;
		this.fciBF = new FCI(false, basisPath);
		this.techBuf = new ArrayList<ArrayList<ArrayList<String>>>();
		this.orders = new ArrayList<ArrayList<String>>();
		this.is_long = true;
		this.cmult = 1.0;
		this.tvi = 1;	
	}
	public OrderSim(int keyID){//for agg key (dont need bgm)
		this.keyNum = keyID;
		//read in this keys row in ak_log
		String laPath = AhrIO.uniPath("./../out/ak/log/ak_log.txt");
		ArrayList<String> laRow = AhrIO.scanRow(laPath, ",", String.valueOf(keyID));
		FCI fciLA = new FCI(true, laPath);
		//assign class objs according to file row
		String bgmLC = laRow.get(fciLA.getIdx("bgm")).toLowerCase();
		String bgmUC = bgmLC.toUpperCase();
		this.bfPath = AhrIO.uniPath("./../out/ak/baseis/"+bgmLC+"/"+bgmUC+"_"+String.valueOf(keyID)+".txt");
		this.dbUsed = laRow.get(fciLA.getIdx("db_used"));
		if(this.dbUsed .equals("YH")){
			this.dbUsed = "Yahoo";
		}else{
			this.dbUsed = "Intrinio";
		}
		this.setDateRange(laRow.get(fciLA.getIdx("start_date")), laRow.get(fciLA.getIdx("end_date")));
		this.tvi = Integer.parseInt(laRow.get(fciLA.getIdx("tvi")));
		if(Integer.parseInt(laRow.get(fciLA.getIdx("call"))) == 0){
			this.is_long = false;
			this.cmult = -1.0;
		}else{
			this.is_long = true;
			this.cmult = 1.0;
		}
		this.fciBF = new FCI(false, this.bfPath);
		//init empty ALs
		this.techBuf = new ArrayList<ArrayList<ArrayList<String>>>();
		this.orders = new ArrayList<ArrayList<String>>();
	}
	public OrderSim(String bgm, int keyID){//for single key
		//get info from keys_struct
		String bgmLC = bgm.toLowerCase();
		String bgmUC = bgm.toUpperCase();
		String ksPath = AhrIO.uniPath("./../out/sk/log/"+bgmLC+"/keys_struct.txt");
		FCI fciKS = new FCI(true, ksPath);
		ArrayList<String> ksRow = AhrIO.scanRow(ksPath, ",", String.valueOf(keyID));
		//System.out.println("--> ksRow = "+ksRow);
		//fill out attrs from keys_struct file
		this.keyNum = keyID;
		this.dbUsed = ksRow.get(fciKS.getIdx("db_used"));
		if(this.dbUsed.equals("YH")){
			this.dbUsed = "Yahoo";
		}else{
			this.dbUsed = "Intrinio";
		}
		this.tvi = Integer.parseInt(ksRow.get(fciKS.getIdx("tvi")));
		String call = ksRow.get(fciKS.getIdx("call"));
		if(call.equals("0")){
			this.is_long = false;
			this.cmult = -1.0;
		}else if(call.equals("1")){
			this.is_long = true;
			this.cmult = 1.0;
		}else{
			String message = "Call data for SK"+keyID+" not found.";
			JOptionPane.showMessageDialog(null, message, "OrderSim Error", JOptionPane.ERROR_MESSAGE);
		}
		this.setDateRange(ksRow.get(fciKS.getIdx("start_date")), ksRow.get(fciKS.getIdx("end_date")));
		//set path and FCI for basis file
		this.bfPath = AhrIO.uniPath("./../out/sk/baseis/"+bgmLC+"/"+bgmUC+"_"+String.valueOf(keyID)+".txt");
		this.fciBF = new FCI(false, this.bfPath);
		//init empty ALs
		this.techBuf = new ArrayList<ArrayList<ArrayList<String>>>();
		this.orders = new ArrayList<ArrayList<String>>();
	}
	//getters & setters
	public int getID(){
		return this.keyNum;
	}
	public String getPath(){
		return this.bfPath;
	}
	public String getSDate(){
		return this.sdate;
	}
	public String getEDate(){
		return this.edate;
	}
	public double getBIM(){
		return this.bim;
	}
	public double getSOM(){
		return this.som;
	}
	public double getTPR(){
		return this.tpr;
	}
	public String getTtvMask(){
		return this.ttvMask;
	}
	public boolean getIsLong(){
		return this.is_long;
	}
	public String getDB(){
		return this.dbUsed;
	}
	public double getTVI(){
		return this.tvi;
	}
	public double getCMULT(){
		return this.cmult;
	}
	public double getPrincipal(){
		return this.principal;
	}
	public double getMaxOrderPrice(){
		return this.maxOrderPrice;
	}
	public double getPosPer(){
		return this.posPer;
	}
	public double getBimPer(){
		return this.bimPer;
	}
	public double getSomPer(){
		return this.somPer;
	}
	public double getTrigAppr(){
		return this.trigAppr;
	}
	public double getSecAppr(){
		return this.secAppr;
	}
	public double getYoyAppr(){
		return this.yoyAppr;
	}
	public boolean getISMT(){
		return this.ismt;
	}
	public ArrayList<ArrayList<String>> getOrderList(){
		return this.orders;
	}
	public int getOrderListSize(){
		return this.orders.size();
	}
	public int getOrderListSPD(){
		int spd = 0;
		if(this.orders.size() == 1){
			spd = 1;
		}else if(this.orders.size() > 1){
			String date1 = this.orders.get(0).get(0);
			String date2 = this.orders.get(1).get(0);
			spd = 1;
			while(date2.equals(date1) || spd > (this.orders.size()-2)){
				date2 = this.orders.get(spd+1).get(0);
				spd++;
			}
		}
		return spd;
	}
	//------------- SETTERS --------------
	public void setPath(String path){
		this.bfPath = path;
	}
	public void setDateRange(String date1, String date2){
		this.sdate = date1;
		this.edate = date2;
		this.techBuf = new ArrayList<ArrayList<ArrayList<String>>>();
	}
	public void setBIM(double bimVal){
		this.bim = bimVal;
	}
	public void setSOM(double somVal){
		this.som = somVal;
	}
	public void setTPR(double tprVal){
		this.tpr = tprVal;
	}
	public void setTtvMask(String ttvVal){
		this.ttvMask = ttvVal;
		this.techBuf = new ArrayList<ArrayList<ArrayList<String>>>();
	}
	public void setIsLong(boolean isLongVal){
		this.is_long = isLongVal;
		if(this.is_long){
			this.cmult = 1.0;
		}else{
			this.cmult = -1.0;
		}
	}
	public void setDB(String dbVal){
		this.dbUsed = dbVal;
	}
	public void setTVI(int tviVal){
		this.tvi = tviVal;
		this.techBuf = new ArrayList<ArrayList<ArrayList<String>>>();
	}
	public void setISMT(boolean ismtVal){
		this.ismt = ismtVal;
	}
	public void setPrincipal(double principalVal){
		this.principal = principalVal;
	}
	public void setMaxOrderPrice(double mopVal){
		this.maxOrderPrice = mopVal;
	}
	public void setOrderList(ArrayList<ArrayList<String>> orderAL){
		this.orders = orderAL;
	}
		
	//functional methods

	//techBuffer is 3D string AL that holds the basic tech data needed to calc an order around a date
	//a 1-day order only needs 2 lines of tech data while a 3-day needs more
	public void calcBuffer(){
		this.orders = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> slist = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> basis = AhrIO.scanFile(this.bfPath, ",");
		//add lines of dates in range and according to ttvMask
		int startRowIdx = 0;
		if(fciBF.getHasHeader()){
			startRowIdx = 1;
		}
		for(int i = startRowIdx; i < basis.size(); i++){
			String itrDate = basis.get(i).get(fciBF.getIdx("date"));
			if(AhrDate.isDateInPeriod(itrDate, sdate, edate)){
				String ticker = basis.get(i).get(fciBF.getIdx("ticker"));
				ArrayList<String> line = new ArrayList<String>();
				if(checkTTV(basis.get(i).get(fciBF.getIdx("ttv_code")))){
					line.add(itrDate);
					line.add(ticker);
					slist.add(line);
				}
			}
		}
		//itr thru each passed line and calc order line
		for(int i = 0; i < slist.size(); i++){
			String date = slist.get(i).get(0);
			String tick = slist.get(i).get(1);
			String dbPath = Globals.intrinio_path+tick+".txt";
			ArrayList<String> line = new ArrayList<String>();
			line.add(date);								// [0] date
			line.add(tick);								// [1] ticker
			line.add("ph");								// [2] % appr over order (from close before pred)
			line.add("ph");								// [3] % appr over order (from open at start of pred)
			line.add("ph");								// [4] Time when bought (OPEN, DAY, or NO)
			line.add("ph");								// [5] $ val bought in at
			line.add("ph");								// [6] $ val sold at
			line.add("ph");								// [7] % appr from method
			line.add("ph");								// [8] # of days cash needs to be reserved
			this.orders.add(line);
			//# of lines needed in the buffer
			this.techLen = 0;
			if(this.tvi == 0 || this.tvi == 1){//1-day
				this.techLen = 2;
			}
			if(this.tvi == 2 || this.tvi == 3){//2-day
				this.techLen = 3;
			}
			if(this.tvi == 4 || this.tvi == 5){//3-day
				this.techLen = 4;
			}
			if(this.tvi == 6 || this.tvi == 7){//5-day
				this.techLen = 6;
			}
			if(this.tvi == 8 || this.tvi == 9){//10-day
				this.techLen = 11;
			}
			//read files til you get to date
			ArrayList<ArrayList<String>> stockBuf = new ArrayList<ArrayList<String>>();
			String[] plArr = {""};	//prev line array (more recent)
			String[] clArr = {""};	//curr line array (less recent)
			try{
				BufferedReader br = new BufferedReader(new FileReader(dbPath));
				boolean date_reached = false;
				while(br.ready() && !date_reached){
					String fline = br.readLine();
					plArr = clArr;
					clArr = fline.split("~");
					if(stockBuf.size() == 0){
						//System.out.println("PL Array Length: " + plArr.length);
						stockBuf = AhrAL.addArrayToAL(stockBuf, clArr);
						stockBuf = AhrAL.addArrayToAL(stockBuf, clArr);
					}else{
						//AhrAL.print(stockBuf);
						if(stockBuf.size() < this.techLen){
							stockBuf = AhrAL.addArrayToAL(stockBuf, clArr);
						}else{
							stockBuf = AhrAL.addArrayToAL(stockBuf, clArr);
							stockBuf.remove(0);
						}
					}
					if(clArr[0].equals(date)){
						date_reached = true;
					}
				}
				br.close();
				//if date as not reached, zero the values in buffer
				if(!date_reached){
					for(int x = 0; x < stockBuf.size(); x++){
						for(int y = 1; y <= 10; y++){
							stockBuf.get(x).set(0, date);
							stockBuf.get(x).set(y, "0.0");
						}
					}		
				}
			}catch(FileNotFoundException e){
				//System.out.println("FileNotFoundException: " + e+"\n-> Ticker = "+tick+"  |  Date = "+date);
				for(int x = 0; x < this.techLen; x++){
					ArrayList<String> sbLine = new ArrayList<String>();
					sbLine.add(date);
					for(int y = 0; y < 10; y++){
						sbLine.add("0.0");
					}
					stockBuf.add(sbLine);
				}
			}catch(IOException e){
				System.out.println("IOException: " + e.getMessage());
			}
			//System.out.println("TICKER: " + tick);
			//printAL_S(stockBuf);
			this.techBuf.add(stockBuf);
		}
		//log tech to file for debugging
		ArrayList<ArrayList<String>> tfTechBuf = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < this.techBuf.size(); i++){
			for(int j = 0; j < this.techBuf.get(i).size(); j++){
				tfTechBuf.add(this.techBuf.get(i).get(j));
			}
		}
		AhrIO.writeToFile(AhrIO.uniPath("./../data/tmp/os_techbuf.txt"), tfTechBuf, ",");
	}

	public void calcOrderList(){
		if(this.techBuf.size() < 1){
			calcBuffer();
		}else{
			//System.out.println("TechBuf size = " + techBuf.size());
		}
		FCI fciTB = new FCI(false, Globals.intrinio_path);
		this.trigAppr = 0.0;
		this.tpr = 0.0;
		this.posPer = 0.0;
		int bimCount = 0;
		int somCount = 0;
		//System.out.println("***** Calculating Order List *****");
		for(int i = 0; i < this.techBuf.size(); i++){
			if(i%500==0){
				//System.out.println("   "+i+" out of "+this.techBuf.size());
			}
			ArrayList<Double> opens = new ArrayList<Double>();
			ArrayList<Double> highs = new ArrayList<Double>();
			ArrayList<Double> lows = new ArrayList<Double>();
			for(int x = 0; x < this.techBuf.get(i).size(); x++){
				//System.out.println("Idx "+x+" = "+this.techBuf.get(i).get(x).get(0));
				opens.add(Double.parseDouble(this.techBuf.get(i).get(x).get(fciTB.getIdx("adj_open"))));
				highs.add(Double.parseDouble(this.techBuf.get(i).get(x).get(fciTB.getIdx("adj_high"))));
				lows.add(Double.parseDouble(this.techBuf.get(i).get(x).get(fciTB.getIdx("adj_low"))));
			}
			//AhrAL.print(this.techBuf.get(i));
			//System.out.println("");
			double open = opens.get(opens.size()-2);
			double firstHi = highs.get(highs.size()-2);
			double firstLo = lows.get(lows.size()-2);
			int llIdx = this.techBuf.get(i).size()-1;	//last line idx
			double lastClose = Double.parseDouble(this.techBuf.get(i).get(llIdx).get(fciTB.getIdx("adj_close")));
			double endClose = Double.parseDouble(this.techBuf.get(i).get(0).get(fciTB.getIdx("adj_close")));
			double buyPrice = lastClose * this.bim;	//price bought at (if buy triggered)
			double sellPrice = buyPrice * this.som;	//price sold at (if buy triggered)
			boolean bim_triggered = true;
			boolean som_triggered = false;
			boolean allow_first_day_selling = true;
			int daysHeld = this.techLen -1;	//# of days order was held for
			if(lastClose != 0.0){
				this.orders.get(i).set(2, String.format("%.3f", ((endClose-lastClose)/lastClose)*100.0));
			}else{
				this.orders.get(i).set(2, "0.000");
			}
			if(open != 0.0){
				this.orders.get(i).set(3, String.format("%.3f", ((endClose-open)/open)*100.0));
			}else{
				this.orders.get(i).set(3, "0.000");
			}
			if(this.cmult > 0){	//--------------------------- LONG CALL -----------------------------------
				if(open <= buyPrice){								//OPTION 1 : triggered at open
					this.orders.get(i).set(4, "OPEN");
					buyPrice = open;
					sellPrice = buyPrice * this.som;
					bimCount++;
					if(allow_first_day_selling){
						if(sellPrice > buyPrice){//lock in gains
							if(firstHi > sellPrice){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;
							}
						}
						if(sellPrice < buyPrice){//stop-loss
							if(firstLo < sellPrice){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;									
							}
						}
					}
				}else if(open > buyPrice && firstLo < buyPrice){	//OPTION 2 : triggered during day
					this.orders.get(i).set(4, "DAY");
					bimCount++;
					if(allow_first_day_selling){
						if(sellPrice > buyPrice){//lock in gains
							// TODO: put max trig appr info here
							if(firstHi > sellPrice && this.ismt){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;
							}
						}
						if(sellPrice < buyPrice){//stop-loss
							if(firstLo < sellPrice){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;									
							}
						}
					}
				}else{												//OPTION 3 : not triggered
					bim_triggered = false;
				}
				//itr thru all days if postion is held over mult days
				if(bim_triggered && !som_triggered && sellPrice >= buyPrice){		//lock in gains
					if(this.techBuf.get(i).size() > 2){
						int x = this.techBuf.get(i).size() - 3;
						while(x >= 0 && !som_triggered){
							if(highs.get(x) >= sellPrice){
								som_triggered = true;
								daysHeld -= x;
								somCount++;
								if(sellPrice < opens.get(x)){
									sellPrice = opens.get(x);
								}
							}
							x--;
						}
					}
					this.tpr += daysHeld;
				}
				if(bim_triggered && !som_triggered && sellPrice < buyPrice){		//stop-loss
					if(this.techBuf.get(i).size() > 2){
						int x = this.techBuf.get(i).size() - 3;
						while(x >= 0 && !som_triggered){
							if(lows.get(x) <= sellPrice){
								som_triggered = true;
								daysHeld -= x;
								somCount++;
								if(sellPrice > opens.get(x)){
									sellPrice = opens.get(x);
								}
							}
							x--;
						}
					}
					this.tpr += daysHeld;
				}
				if(!som_triggered){
					sellPrice = endClose;
				}
				if(bim_triggered){
					double appr = 0.0;
					if(buyPrice != 0.0){
						appr = ((sellPrice - buyPrice) / buyPrice) * 100.0;
					}
					this.trigAppr += appr;
					this.orders.get(i).set(5, String.format("%.2f", buyPrice));
					this.orders.get(i).set(6, String.format("%.2f", sellPrice));
					this.orders.get(i).set(7, String.format("%.3f", appr));
				}else{
					this.orders.get(i).set(4, "NO");
					this.orders.get(i).set(5, "0.00");
					this.orders.get(i).set(6, "0.00");
					this.orders.get(i).set(7, "0.000");
				}					
			}else{			//--------------------------- SHORT CALL ----------------------------------
				if(open >= buyPrice){								//OPTION 1 : triggered at open
					this.orders.get(i).set(4, "OPEN");
					buyPrice = open;
					sellPrice = buyPrice * this.som;
					bimCount++;
					if(allow_first_day_selling){
						if(sellPrice < buyPrice){//lock in gains
							if(firstLo < sellPrice){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;
							}
						}
						if(sellPrice > buyPrice){//stop-loss
							if(firstHi > sellPrice){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;									
							}
						}
					}
				}else if(open < buyPrice && firstHi > buyPrice){	//OPTION 2 : triggered during day
					this.orders.get(i).set(4, "DAY");
					bimCount++;
					if(allow_first_day_selling){
						if(sellPrice < buyPrice){//lock in gains
							//TODO:  for max trig appr only
							if(firstLo < sellPrice && this.ismt){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;
							}
						}
						if(sellPrice > buyPrice){//stop-loss
							if(firstHi > sellPrice){
								som_triggered = true;
								daysHeld = 1;
								this.tpr += daysHeld;
								somCount++;									
							}
						}
					}
				}else{												//OPTION 3 : not triggered
					bim_triggered = false;
				}
				if(bim_triggered && !som_triggered && sellPrice <= buyPrice){		//lock in gains
					if(this.techBuf.get(i).size() > 2){
						int x = this.techBuf.get(i).size() - 3;
						while(x >= 0 && !som_triggered){
							if(lows.get(x) <= sellPrice){
								som_triggered = true;
								daysHeld -= x;
								somCount++;
								if(sellPrice > opens.get(x)){
									sellPrice = opens.get(x);
								}
							}
							x--;
						}
					}
					this.tpr += daysHeld;
				}
				if(bim_triggered && !som_triggered && sellPrice > buyPrice){		//stop-loss
					if(this.techBuf.get(i).size() > 2){
						int x = this.techBuf.get(i).size() - 3;
						while(x >= 0 && !som_triggered){
							if(highs.get(x) >= sellPrice){
								som_triggered = true;
								daysHeld -= x;
								somCount++;
								if(sellPrice < opens.get(x)){
									sellPrice = opens.get(x);
								}
							}
							x--;
						}
					}
					this.tpr += daysHeld;
				}
				if(!som_triggered){
					sellPrice = endClose;
				}
				if(bim_triggered){
					double appr = 0.0;
					if(buyPrice != 0.0){
						appr = ((sellPrice - buyPrice) / buyPrice) * 100.0;
					}
					this.trigAppr += appr;
					this.orders.get(i).set(5, String.format("%.2f", buyPrice));
					this.orders.get(i).set(6, String.format("%.2f", sellPrice));
					this.orders.get(i).set(7, String.format("%.3f", appr));
				}else{
					this.orders.get(i).set(4, "NO");
					this.orders.get(i).set(5, "0.00");
					this.orders.get(i).set(6, "0.00");
					this.orders.get(i).set(7, "0.000");
				}
			}
			this.orders.get(i).set(8, String.valueOf(daysHeld));
			if(Double.parseDouble(this.orders.get(i).get(7)) > 0.0){
				this.posPer++;
			}
			//print out order line if seems fishy
			//double olAppr = Double.parseDouble(this.orders.get(i).get(7));
			//if(olAppr > 100.0){
			//	System.out.println("BIG VAL: "+this.orders.get(i));
			//}
		}
		this.posPer = this.posPer / (double)bimCount;
		this.tpr = this.tpr / (double)bimCount;
		this.trigAppr = this.trigAppr / (double)bimCount;
		this.bimPer = ((double)bimCount / (double)this.techBuf.size()) * 100.0;
		this.somPer = ((double)somCount / (double)this.techBuf.size()) * 100.0;

		AhrIO.writeToFile(AhrIO.uniPath("./../data/tmp/os_orderlist.txt"), this.orders, ",");

		//calc active trades
		calcActiveTrades();
		//calc compound interest values
		ArrayList<ArrayList<String>> growth = calcGrowth(this.principal);
		double startVal = Double.parseDouble(growth.get(0).get(1));
		double endVal = Double.parseDouble(growth.get(growth.size()-1).get(1));
		this.secAppr = ((endVal - startVal) / startVal) * 100.0;
		double intervals = (double)growth.size() / 252.0;
		double amtGained = endVal - this.principal;
		this.yoyAppr = Math.pow((0.0 - ((0.0 - this.principal - amtGained) / this.principal)), (1.0/intervals)) - 1.0;
		this.yoyAppr = this.yoyAppr * 100.0;

		//write to file OL earlier to test
		AhrIO.writeToFile(AhrIO.uniPath("./../data/tmp/os_orderlist.txt"), this.orders, ",");
		//write orderlist to file, order by meth % and write that to file also
		String olPath = AhrIO.uniPath("./../data/tmp/os_orderlist.txt");
		FCI fciOL = new FCI(false, olPath);
		ArrayList<ArrayList<String>> olByAppr = new ArrayList<ArrayList<String>>(this.orders);
		Collections.sort(olByAppr, new Comparator<ArrayList<String>>(){
			@Override
			public int compare(ArrayList<String> obj1, ArrayList<String> obj2){
				double dcomp1 = Double.parseDouble(obj1.get(fciOL.getIdx("method_appr")));
				double dcomp2 = Double.parseDouble(obj2.get(fciOL.getIdx("method_appr")));
				return Double.compare(dcomp1, dcomp2);//ascending
			}
		});
		AhrIO.writeToFile(olPath, this.orders, ",");
		AhrIO.writeToFile(AhrIO.uniPath("./../data/tmp/os_orderlist_byappr.txt"), olByAppr, ",");
	}

	//calc growth on a porfolio using the calculated order list
	public ArrayList<ArrayList<String>> calcGrowth2(double value){
		FCI fciOL = new FCI(false, AhrIO.uniPath("./../data/tmp/os_orderlist.txt"));
		ArrayList<ArrayList<String>> growth = new ArrayList<ArrayList<String>>();
		String date = this.orders.get(0).get(fciOL.getIdx("date"));
		//itr thru orders, calcing growth
		ArrayList<Double> apprs = new ArrayList<Double>();
		apprs.add(Double.parseDouble(this.orders.get(0).get(fciOL.getIdx("method_appr"))) * cmult);
		for(int i = 1; i < this.orders.size(); i++){
			String itrDate = this.orders.get(i).get(fciOL.getIdx("date"));
			String itrAppr = this.orders.get(i).get(fciOL.getIdx("method_appr"));
			int itrDaysRes = Integer.parseInt(this.orders.get(i).get(fciOL.getIdx("days_reserved")));
			if(itrDate.equals(date)){	//on same date
				apprs.add(Double.parseDouble(itrAppr) * cmult);
			}else{						//date changes
				double orderSize = (value / apprs.size());
				if(orderSize > this.maxOrderPrice){
					orderSize = this.maxOrderPrice;
				}
				for(int j = 0; j < apprs.size(); j++){
					value += orderSize * (apprs.get(j) / 100.0);
				}
				ArrayList<String> line = new ArrayList<String>();
				line.add(date);
				line.add(String.format("%.4f", value));
				growth.add(line);
				date = itrDate;
				apprs = new ArrayList<Double>();
				apprs.add(Double.parseDouble(itrAppr) * cmult);
			}
		}
		return growth;
	}

	//calc growth on a porfolio using the calculated order list
	//totValue = cash on hand + sum of all invested into trades
	public ArrayList<ArrayList<String>> calcGrowth(double totValue){
		ArrayList<ArrayList<String>> portfolioValue = new ArrayList<ArrayList<String>>();
		FCI fciOL = new FCI(false, AhrIO.uniPath("./../data/tmp/os_orderlist.txt"));
		//find out SPD to calc # of max active trades at one time
		String date = this.orders.get(0).get(fciOL.getIdx("date"));
		int spd = -1;
		for(int i = 1; i < this.orders.size(); i++){
			String itrDate = this.orders.get(i).get(fciOL.getIdx("date"));
			if(!itrDate.equals(date)){
				spd = i;
				break;
			}
		}
		//setup vars to track portfolio value and $ per trade to opt it
		double cashOnHand = totValue;
		int maxActiveTrades = spd * (this.techLen-1);
		maxActiveTrades = (int)((double)maxActiveTrades * (this.bimPer/100.0));
		double pricePerTrade = totValue / (double)maxActiveTrades;
		if(pricePerTrade > this.maxOrderPrice){
			pricePerTrade = this.maxOrderPrice;
		}
		//itr thru orders, calcing growth
		ArrayList<ArrayList<String>> activeTradesBuf = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < this.orders.size(); i++){
			String itrDate = this.orders.get(i).get(fciOL.getIdx("date"));
			String itrTrigCode = this.orders.get(i).get(fciOL.getIdx("trigger_code"));
			String itrAppr = this.orders.get(i).get(fciOL.getIdx("method_appr"));
			String itrDaysRes = this.orders.get(i).get(fciOL.getIdx("days_reserved"));
			if(!itrDate.equals(date)){
				//decrease day left by 1, free the money of those with 0 days left
				ArrayList<ArrayList<String>> tmpBuf = new ArrayList<ArrayList<String>>();
				for(int j = 0; j < activeTradesBuf.size(); j++){
					int daysLeft = Integer.parseInt(activeTradesBuf.get(j).get(0)) - 1;
					if(daysLeft > 0){//money still sequestered, keep in buffer
						activeTradesBuf.get(j).set(0, String.valueOf(daysLeft));
						tmpBuf.add(activeTradesBuf.get(j));
					}else{//sell out of the trade, cash goes back into hand, add money made to totValue
						cashOnHand += Double.parseDouble(activeTradesBuf.get(j).get(1));
						cashOnHand += Double.parseDouble(activeTradesBuf.get(j).get(2));
						totValue += Double.parseDouble(activeTradesBuf.get(j).get(2));
					}
				}
				//update portfolio value file
				ArrayList<String> pvLine = new ArrayList<String>();
				pvLine.add(date);
				//pvLine.add(String.format("%.2f", cashOnHand));
				pvLine.add(String.format("%.2f", totValue));
				//pvLine.add(String.format("%.2f", pricePerTrade));
				portfolioValue.add(pvLine);
				//recalibrate vals for next date
				activeTradesBuf = tmpBuf;
				pricePerTrade = totValue / (double)maxActiveTrades;
				if(pricePerTrade > this.maxOrderPrice){
					pricePerTrade = this.maxOrderPrice;
				}
				date = itrDate;
			}
			if(!itrTrigCode.equals("NO")){
				double priceThisTrade = pricePerTrade;
				if(cashOnHand < pricePerTrade){//bottoms at 0 so cant be negative
					priceThisTrade = cashOnHand;
				}
				cashOnHand -= priceThisTrade;
				double tradeProfit = (priceThisTrade * ((Double.parseDouble(itrAppr)*this.cmult)/100.0));					
				ArrayList<String> atbLine = new ArrayList<String>();
				atbLine.add(itrDaysRes);							//[0] days left til trade is sold
				atbLine.add(String.format("%.2f", priceThisTrade));	//[1] money ($) into trade
				atbLine.add(String.format("%.2f", tradeProfit));	//[2] money ($) made from trade, can be neg
				activeTradesBuf.add(atbLine);
			}
		}
		return portfolioValue;
	}

	//calc num of ongoing trading going on on every date (goes up w/ SPD and TVI)
	public void calcActiveTrades(){
		FCI fciOL = new FCI(false, AhrIO.uniPath("./../data/tmp/os_orderlist.txt"));
		ArrayList<ArrayList<String>> activeTrades = new ArrayList<ArrayList<String>>();
		activeTrades.add(AhrAL.toAL(new String[]{"date", "active_count"}));
		ArrayList<String> activeBuf = new ArrayList<String>();
		String date = this.orders.get(0).get(fciOL.getIdx("date"));
		for(int i = 1; i < this.orders.size(); i++){
			String itrDate = this.orders.get(i).get(fciOL.getIdx("date"));
			int itrDaysRes = Integer.parseInt(this.orders.get(i).get(fciOL.getIdx("days_reserved")));

			if(itrDate.equals(date)){
				String buf = "";
				for(int j = 0; j < itrDaysRes; j++){
					buf += "0";
				}
				activeBuf.add(buf);		
			}else{
				//remove 1day from all buf strings, update activeBuf
				ArrayList<String> tmpBuf = new ArrayList<String>();
				for(int j = 0; j < activeBuf.size(); j++){
					activeBuf.set(j, activeBuf.get(j).substring(1));
					if(activeBuf.get(j).length() > 0){
						tmpBuf.add(activeBuf.get(j));
					}
				}
				activeBuf = tmpBuf;
				//add line to activeTrades
				ArrayList<String> line = new ArrayList<String>();
				line.add(date);
				line.add(String.valueOf(activeBuf.size()));
				activeTrades.add(line);
				//update date and add val
				date = itrDate;
				String buf = "";
				for(int j = 0; j < itrDaysRes; j++){
					buf += "0";
				}
				activeBuf.add(buf);
			}
		}
		AhrIO.writeToFile(AhrIO.uniPath("./../data/tmp/os_activetrades.csv"), activeTrades, ",");
	}
	
	//calc best BIM/SOM from narrowing down on ever increasing BIM/SOM combo precision
	public void calcBSO(){
		//determine start and end vals depending on call
		int loBimLim = 0;
		int hiBimLim = 110;
		int loSomLim = 0;
		int hiSomLim = 200;
		if(getIsLong()){
			loBimLim = 90;
			hiBimLim = 200;
		}
		//init vals
		ArrayList<ArrayList<String>> bsoMult = new ArrayList<ArrayList<String>>();
		double bestBIM = 0.0;
		double bestSOM = 0.0;
		double bestYOY = Double.MIN_VALUE;
		//1st cycle scan entire region by 0.10 increments
		for(int i = loBimLim; i <= hiBimLim; i+=10){
			for(int j = loSomLim; j <= hiSomLim; j+=10){
				double itrBIM = ((double)i/100.0);
				double itrSOM = ((double)j/100.0);
				if(j == 100){
					j -= 7;//makes next itr 103
					itrSOM = 0.97;
				}
				if(j == 103){
					j -= 3;//corrects back to 110
				}
				
				setBIM(itrBIM);
				setSOM(itrSOM);
				calcOrderList();
				double itrYOY = getYoyAppr();
				if(itrYOY > bestYOY){
					bestBIM = itrBIM;
					bestSOM = itrSOM;
					bestYOY = itrYOY;
				}
				ArrayList<String> line = new ArrayList<String>();
				line.add(String.format("%.3f", itrBIM));
				line.add(String.format("%.3f", itrSOM));
				line.add(String.format("%.5f", getTrigAppr()));
				line.add(String.format("%.5f", getYoyAppr()));
				line.add(String.format("%.3f", getPosPer()));
				bsoMult.add(line);
				//System.out.println("BIM: "+String.format("%.3f",itrBIM)+
				//					"  |  SOM: "+String.format("%.3f",itrSOM)+
				//					"  |  YoY %: "+String.format("%.3f",itrYOY)+
				//					"  |  Best YoY %: "+String.format("%.3f",bestYOY));

			}
		}
		//2nd cycle, scan a 0.18 by 0.18 region by 0.02 increments
		int centerBIM = (int)(bestBIM * 100.0);		//need unchanging BIM val while bestBIM can change
		int centerSOM = (int)(bestSOM * 100.0);		// ^^ same for SOM
		for(int i = centerBIM-8; i <= centerBIM+8; i+=2){
			for(int j = centerSOM-8; j <= centerSOM+8; j+=2){
				boolean is_useless_som = false;
				if(j > 97 && j < 103){
					is_useless_som = true;
				}
				if(i > 0 && j > 0 && !is_useless_som){
					double itrBIM = ((double)i/100.0);
					double itrSOM = ((double)j/100.0);
					setBIM(itrBIM);
					setSOM(itrSOM);
					calcOrderList();
					double itrYOY = getYoyAppr();
					if(itrYOY > bestYOY){
						bestBIM = itrBIM;
						bestSOM = itrSOM;
						bestYOY = itrYOY;
					}
					ArrayList<String> line = new ArrayList<String>();
					line.add(String.format("%.3f", itrBIM));
					line.add(String.format("%.3f", itrSOM));
					line.add(String.format("%.5f", getTrigAppr()));
					line.add(String.format("%.5f", getYoyAppr()));
					line.add(String.format("%.3f", getPosPer()));
					bsoMult.add(line);
					//System.out.println("BIM: "+String.format("%.3f",itrBIM)+
					//					"  |  SOM: "+String.format("%.3f",itrSOM)+
					//					"  |  YoY %: "+String.format("%.3f",itrYOY)+
					//					"  |  Best YoY %: "+String.format("%.3f",bestYOY));
				}
			}
		}
		centerBIM = (int)(bestBIM * 1000.0);		//need unchanging BIM val while bestBIM can change
		centerSOM = (int)(bestSOM * 1000.0);		// ^^ same for SOM
		for(int i = centerBIM-20; i <= centerBIM+20; i+=5){
			for(int j = centerSOM-20; j <= centerSOM+20; j+=5){
				boolean is_useless_som = false;
				if(j > 970 && j < 1030){
					is_useless_som = true;
				}
				if(i > 0 && j > 0 && !is_useless_som){
					double itrBIM = ((double)i/1000.0);
					double itrSOM = ((double)j/1000.0);
					setBIM(itrBIM);
					setSOM(itrSOM);
					calcOrderList();
					double itrYOY = getYoyAppr();
					if(itrYOY > bestYOY){
						bestBIM = itrBIM;
						bestSOM = itrSOM;
						bestYOY = itrYOY;
					}
					ArrayList<String> line = new ArrayList<String>();
					line.add(String.format("%.3f", itrBIM));
					line.add(String.format("%.3f", itrSOM));
					line.add(String.format("%.5f", getTrigAppr()));
					line.add(String.format("%.5f", getYoyAppr()));
					line.add(String.format("%.3f", getPosPer()));
					bsoMult.add(line);
					//System.out.println("BIM: "+String.format("%.3f",itrBIM)+
					//					"  |  SOM: "+String.format("%.3f",itrSOM)+
					//					"  |  YoY %: "+String.format("%.3f",itrYOY)+
					//					"  |  Best YoY %: "+String.format("%.3f",bestYOY));
				}
			}
		}
		centerBIM = (int)(bestBIM * 1000.0);		//need unchanging BIM val while bestBIM can change
		centerSOM = (int)(bestSOM * 1000.0);		// ^^ same for SOM
		for(int i = centerBIM-4; i <= centerBIM+4; i++){
			for(int j = centerSOM-4; j <= centerSOM+4; j++){
				boolean is_useless_som = false;
				if(j > 970 && j < 1030){
					is_useless_som = true;
				}
				if(i > 0 && j > 0 && !is_useless_som){
					double itrBIM = ((double)i/1000.0);
					double itrSOM = ((double)j/1000.0);
					setBIM(itrBIM);
					setSOM(itrSOM);
					calcOrderList();
					double itrYOY = getYoyAppr();
					if(itrYOY > bestYOY){
						bestBIM = itrBIM;
						bestSOM = itrSOM;
						bestYOY = itrYOY;
					}
					ArrayList<String> line = new ArrayList<String>();
					line.add(String.format("%.3f", itrBIM));
					line.add(String.format("%.3f", itrSOM));
					line.add(String.format("%.5f", getTrigAppr()));
					line.add(String.format("%.5f", getYoyAppr()));
					line.add(String.format("%.3f", getPosPer()));
					bsoMult.add(line);
					//System.out.println("BIM: "+String.format("%.3f",itrBIM)+
					//					"  |  SOM: "+String.format("%.3f",itrSOM)+
					//					"  |  YoY %: "+String.format("%.3f",itrYOY)+
					//					"  |  Best YoY %: "+String.format("%.3f",bestYOY));
				}
			}
		}
		AhrIO.writeToFile(AhrIO.uniPath("./../data/tmp/bso_multiple.txt"), bsoMult, ",");
		//calc one last time on best combo so can check files
		setBIM(bestBIM);
		setSOM(bestSOM);
		calcOrderList();
		//printData();
	}
	
	//given a TTV code from a basis line, returns bool whether code pass OrderSim ttvMask
	//code: 0 = is train line in basis, 1 = is test line, 2 = is verify line
	public boolean checkTTV(String code){
		char bitInMask = this.ttvMask.charAt(Integer.parseInt(code));
		boolean pass_ttv = false;
		if(bitInMask == '1'){
			pass_ttv = true;
		}
		return pass_ttv;
	}

	//print out all data related info of current OrderSim
	public void printData(){
		System.out.println("========== OrderSim Data =========="+
							"\n--> Key ID : "+getID()+
							"\n--> BIM    : "+getBIM()+
							"\n--> SOM    : "+getSOM()+
							"\n--> TPR    : "+getTPR()+
							"\n--> Positive %      : "+getPosPer()+
							"\n--> BIM Triggered % : "+getBimPer()+
							"\n--> SOM Triggered % : "+getSomPer()+
							"\n--> Triggered APAPT : "+getTrigAppr()+
							"\n--> Section %       : "+getSecAppr()+
							"\n--> YoY %           : "+getYoyAppr()+
							"\n===================================");
	}

}


