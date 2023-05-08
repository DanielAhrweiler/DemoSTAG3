package ahrweiler.support;
import ahrweiler.Globals;
import ahrweiler.util.AhrIO;
import ahrweiler.util.AhrAL;
import ahrweiler.util.AhrGen;
import java.io.*;
import java.util.ArrayList;

public class StockFilter {

	class NameAndRange{
		private String name;
		private int start;
		private int end;
		public NameAndRange(String sname){
			this.name = sname;
			this.start = 0;
			this.end = Integer.MAX_VALUE;	
		}
		public NameAndRange(ArrayList<String> line){
			if(line.size() > 0){
				this.name = line.get(0);
			}else{
				this.name = "";
			}
			if(line.size() > 2){
				try{
					this.start = Integer.parseInt(line.get(1));
					this.end = Integer.parseInt(line.get(2));
				}catch(NullPointerException e){
					System.out.println("ERR: "+e);
				}catch(NumberFormatException e){
					System.out.println("ERR: "+e);
				}
			}else{
				this.start = Integer.MIN_VALUE;
				this.end = Integer.MAX_VALUE;
			}
		}
		public String getName(){
			return this.name;
		}
		public int getStartVal(){
			return this.start;
		}
		public int getEndVal(){
			return this.end;
		}
		public void setName(String name){
			this.name = name;
		}
		public void setStartVal(int sval){
			this.start = sval;
		}
		public void setEndVal(int eval){
			this.end = eval;
		}
	}

	ArrayList<ArrayList<String>> scodes;
	ArrayList<ArrayList<String>> results;
	ArrayList<String> sectors;
	ArrayList<String> industries;
	NameAndRange mcRange;
	ArrayList<NameAndRange> indRanges;
	

	public StockFilter(){
		this.scodes = new ArrayList<ArrayList<String>>();
		this.results = new ArrayList<ArrayList<String>>();
		this.sectors = new ArrayList<String>();
		this.industries = new ArrayList<String>();
		this.mcRange = new NameAndRange("mc");
		this.indRanges = new ArrayList<NameAndRange>();	
	}
	public StockFilter(ArrayList<ArrayList<String>> filters){
		this.scodes = new ArrayList<ArrayList<String>>();
		this.results = new ArrayList<ArrayList<String>>();
		this.sectors = new ArrayList<String>();
		this.industries = new ArrayList<String>();
		this.indRanges = new ArrayList<NameAndRange>();
		for(int i = 0; i < filters.size(); i++){
			if(filters.get(i).get(0).contains("ind") && !filters.get(i).get(0).equals("industry")){
				this.indRanges.add(new NameAndRange(filters.get(i)));
			}else if(filters.get(i).get(0).equals("mc")){
				this.mcRange = new NameAndRange(filters.get(i));
			}else if(filters.get(i).get(0).equals("sector")){
				if(!filters.get(i).get(1).equals("")){
					String[] allSecs = filters.get(i).get(1).split(",");
					for(int j = 0; j < allSecs.length; j++){
						this.sectors.add(allSecs[j]);
					}
				}
			}else if(filters.get(i).get(0).equals("industry")){
				if(!filters.get(i).get(1).equals("")){
					String[] allInds = filters.get(i).get(1).split(",");
					for(int j = 0; j < allInds.length; j++){
						this.industries.add(allInds[j]);
					}
				}
			}
		}
	}
	public StockFilter(String fpath){
		this.scodes = new ArrayList<ArrayList<String>>();
		this.results = new ArrayList<ArrayList<String>>();
		this.sectors = new ArrayList<String>();
		this.industries = new ArrayList<String>();
		this.indRanges = new ArrayList<NameAndRange>();
		//sort out the filter lines
		ArrayList<ArrayList<String>> filters = AhrIO.scanFile(fpath, "~");
		System.out.println("--> In StockFilter(fpath) constructor, filters are ...");
		AhrAL.print(filters);
		for(int i = 0; i < filters.size(); i++){
			if(filters.get(i).get(0).contains("ind") && !filters.get(i).get(0).equals("industry")){
				this.indRanges.add(new NameAndRange(filters.get(i)));
			}else if(filters.get(i).get(0).equals("mc")){
				this.mcRange = new NameAndRange(filters.get(i));
			}else if(filters.get(i).get(0).equals("sector")){
				if(!filters.get(i).get(1).equals("")){
					String[] allSecs = filters.get(i).get(1).split(",");
					for(int j = 0; j < allSecs.length; j++){
						this.sectors.add(allSecs[j]);
					}
				}
			}else if(filters.get(i).get(0).equals("industry")){
				if(filters.get(i).size() > 1){
					String[] allInds = filters.get(i).get(1).split(",");
					for(int j = 0; j < allInds.length; j++){
						this.industries.add(allInds[j]);
					}
				}
			}
		}
	}

	//GETERS & SETTERS
	public ArrayList<String> getTickers(){
		ArrayList<String> tickers = new ArrayList<String>();
		for(int i = 0; i < this.scodes.size(); i++){
			tickers.add(this.scodes.get(i).get(0));
		}
		return tickers;
	}
	public ArrayList<ArrayList<String>> getResults(){
		return this.results;
	}
	public ArrayList<String> getIndicators(){
		ArrayList<String> inds = new ArrayList<String>();
		for(int i = 0; i < this.indRanges.size(); i++){
			inds.add(this.indRanges.get(i).getName());
		}
		return inds;
	}
	public void setMarketCap(int startVal, int endVal){
		this.mcRange.setStartVal(startVal);
		this.mcRange.setEndVal(endVal);
	}
	public void setSectors(String secRaw){
		this.sectors = new ArrayList<String>();
		String[] allSec = secRaw.split(",");
		for(int i = 0; i < allSec.length; i++){
			String itrSec = allSec[i].replaceAll("\\s+", "");
			if(AhrGen.isInt(itrSec)){
				this.sectors.add(itrSec);
			}
		}
	}
	public void setIndustries(String indRaw){
		this.industries = new ArrayList<String>();
		String[] allInd = indRaw.split(",");
		for(int i = 0; i < allInd.length; i++){
			String itrInd = allInd[i].replaceAll("\\s+", "");
			if(AhrGen.isInt(itrInd)){
				this.industries.add(itrInd);
			}
		}
	}

	//OTHER FUNCTIONS

	//add an indicator name, start val, and end val
	public void addIndicatorFilter(ArrayList<String> filterLine){
		this.indRanges.add(new NameAndRange(filterLine));
	}
	//edit already existing indicator range
	public void editIndicatorFilter(ArrayList<String> filterLine){
		int idx = -1;
		String newName = filterLine.get(0);
		for(int i = 0; i < this.indRanges.size(); i++){
			NameAndRange itrNAR = this.indRanges.get(i);
			String oldName = itrNAR.getName();
			if(oldName.equals(newName)){
				idx = i;
				break;
			}
		}
		if(idx != -1){
			this.indRanges.set(idx, new NameAndRange(filterLine));
		}
	}

	//assign sector/industry codes to all tickers from given sector range
	public void applySecIndFilter(){
		//cols [0]    ticker
		//	   [1]    mc
		//	   [2]    code
		//	   [3]+   other inds
		System.out.println("--> In applySecIndFilter()");

		//get all (non xx) codes from sector_ticks.txt
		ArrayList<ArrayList<String>> stFC = AhrIO.scanFile("./../in/sector_ticks.txt", ",");
		ArrayList<Integer> allCodes = new ArrayList<Integer>();
		for(int i = 0; i < stFC.size(); i++){
			String itrCode = stFC.get(i).get(0);
			if(!itrCode.contains("xx")){
				allCodes.add(Integer.parseInt(stFC.get(i).get(0)));
			}
		}
	
		System.out.println("--> Sector List : " + this.sectors);
		System.out.println("--> Industry List : " + this.industries);
		//determine all sectors/industries (no xx) to get tickers from in sector_ticks.txt
		ArrayList<Integer> stList = new ArrayList<Integer>();
		if(this.industries.size() > 0){
			int secInt = Integer.parseInt(this.sectors.get(0));
			for(int i = 0; i < this.industries.size(); i++){
				stList.add((secInt*100)+Integer.parseInt(this.industries.get(i)));
			}
		}else{
			for(int i = 0; i < this.sectors.size(); i++){
				int secInt = Integer.parseInt(this.sectors.get(i));
				for(int j = 0; j < allCodes.size(); j++){
					int divBy100 = allCodes.get(j) / 100;
					if(secInt == divBy100){
						stList.add(allCodes.get(j));
					}
				}
			}
		}
		System.out.println("  > stList = " + stList);
		//itr thru sector_ticks.txt and add all necessary tickers
		ArrayList<String> scTicks = new ArrayList<String>();
		for(int i = 0; i < stFC.size(); i++){
			String itrSCode = stFC.get(i).get(0);
			int intSCode = 0;
			try{
				intSCode = Integer.parseInt(itrSCode);
			}catch(NumberFormatException e){
				//System.out.println("ERR: "+e);
			}
			if(stList.contains(intSCode)){
				for(int j = 1; j < stFC.get(i).size(); j++){
					String tick = stFC.get(i).get(j);
					if(!scTicks.contains(tick)){
						scTicks.add(tick);
						ArrayList<String> line = new ArrayList<String>();
						line.add(tick);
						line.add(itrSCode);
						this.scodes.add(line);
					}
				}
			}
		}
		//print scode data
		System.out.println("--> Ticker / SCode after applySecIndFilter() ... ");
		AhrAL.print(this.scodes);
	}

	//apply all filter lines and return all stocks that fall within all ranges
	public void applyFilter(String date){
		System.out.println("--> In applyFilter("+date+")");
		//cols [0]  ticker
		//	   [1]  mc
		//	   [2]  code
		//	   [3]+ other inds
		//if no tickers, run applySecIndFilters() to get ticker list
		if(this.scodes.size() == 0){
			applySecIndFilter();
		}

		//get Clean data for this date
		String bdPath = "./../../DB_Intrinio/Clean/ByDate/";
		FCI fciBD = new FCI(false, bdPath);
		ArrayList<ArrayList<String>> bdFile = new ArrayList<ArrayList<String>>();
		if(Globals.uses_mysql_source){
			SQLCode sqlc = new SQLCode("aws");
			sqlc.setDB("bydate");
			//sqlc.setWhereCond("WHERE `date` = '"+date+"'");
			bdFile = sqlc.selectAll("2022-05-27", fciBD.getTags());
		}else{
			bdFile = AhrIO.scanFile(bdPath+date+".txt", "~");
		}

		//get stocks that pass all indicator rules
		//filter by sec/ind first
		
		//check to see if pass MC rule, and all indicator range rules
		long stime = System.currentTimeMillis();
		if(Globals.uses_mysql_source){
			//check MC first
			SQLCode sqlc = new SQLCode("aws");
			sqlc.setDB("sbase");
			sqlc.addWhereCond("WHERE `date` = '2022-05-27' AND `market_cap` BETWEEN "+this.mcRange.getStartVal()+
								" AND "+this.mcRange.getEndVal());
			//get table names, col names and fetch data (MC)
			ArrayList<String> tnames = AhrAL.getCol(this.scodes, 0);
			ArrayList<String> colNames = AhrAL.toAL(new String[]{"ticker", "market_cap"});;
			ArrayList<ArrayList<String>> passMC = sqlc.selectUnion(tnames, colNames);
			//System.out.println("--> MySQL Passes MC : ");
			//AhrAL.print(passMC);
			//check indicator ranges
			sqlc = new SQLCode("aws");
			sqlc.setDB("snorm");
			sqlc.addWhereCond("WHERE `date` = '2022-05-27'");
			colNames = new ArrayList<String>();
			colNames.add("ticker");
			for(int i = 0; i < this.indRanges.size(); i++){
				NameAndRange itrInd = this.indRanges.get(i);
				String itrName = itrInd.getName();
				colNames.add(itrName);
				int itrStartRange = itrInd.getStartVal();
				int itrEndRange = itrInd.getEndVal();
				String wcond = "WHERE `"+itrName+"` BETWEEN "+itrStartRange+" AND "+itrEndRange;
				sqlc.addWhereCond(wcond);
			}
			//get table names, col names and fetch data (MC)
			tnames = AhrAL.getCol(passMC, 0);
			ArrayList<ArrayList<String>> passInds = new ArrayList<ArrayList<String>>();
			if(colNames.size() > 0){
				passInds = sqlc.selectUnion(tnames, colNames);
			}
			//System.out.println("--> MySQL Passes Indicators : ");
			//AhrAL.print(passInds);
			//add MC and sec/ind codes into passInds for final filter data struct
			for(int i = 0; i < passInds.size(); i++){
				String itrTick = passInds.get(i).get(0);
				int mcIdx = AhrAL.getRowIdx(passMC, itrTick);
				int scIdx = AhrAL.getRowIdx(this.scodes, itrTick);
				if(mcIdx != -1 && scIdx != -1){
					ArrayList<String> line = passInds.get(i);
					line.add(passMC.get(mcIdx).get(1));
					line.add(this.scodes.get(scIdx).get(1));
					this.results.add(line);
				}
			}
			System.out.println("--> Final Results (MySQL) ...");
			AhrAL.print(this.results);
		}else{
			for(int i = 0; i < this.scodes.size(); i++){
				String ticker = this.scodes.get(i).get(0);
				String scode = this.scodes.get(i).get(1);
				if(i%500==0){
					//System.out.println("--> bdFile progress: "+i+" out of "+bdFile.size());
				}
				ArrayList<String> vals = new ArrayList<String>();
				double mcVal = -1.0;
				boolean is_in_bd = true;
				boolean passes_ind_rules = true;
				boolean passes_mc = true;
				for(int j = 0; j < this.indRanges.size(); j++){
					NameAndRange itrNAR = this.indRanges.get(j);
					String vname = itrNAR.getName();
					int vrs = itrNAR.getStartVal();
					int vre = itrNAR.getEndVal();
					int bdIdx = AhrAL.getRowIdx(bdFile, ticker);
					if(bdIdx == -1){
						is_in_bd = false;
					}else{
						int itrVal = Integer.parseInt(bdFile.get(bdIdx).get(fciBD.getIdx(vname)));
						if(itrVal < vrs || itrVal > vre){
							passes_ind_rules = false;
						}
						vals.add(String.valueOf(itrVal));
					}

				}
				if(is_in_bd && passes_ind_rules){//still have to check for mc, industry and sector
					//check market cap
					FCI fciSB = new FCI(false, "./../../DB_Intrinio/Main/S_Base/");
					ArrayList<String> sbLine = new ArrayList<String>();
					if(!Globals.uses_mysql_source){
						SQLCode sqlc = new SQLCode("aws");
						sqlc.setDB("sbase");
						sqlc.addWhereCond("WHERE `date` = '"+date+"'");
						//sbLine = sqlc.selectUnion(ticker, AhrAL.toAL(new String[]{"*"}), date, true);
					}else{
						sbLine = AhrIO.scanRow("./../../DB_Intrinio/Main/S_Base/"+ticker+".txt","~",date);
					}
					//System.out.println("--> Date: "+date+"  |  Ticker: "+ticker+"  | sbLine: "+sbLine);
					if(sbLine.size() < fciSB.getTags().size()){
						//System.out.println("ERR ==> sbLine: "+sbLine);
						//errTicks.add(ticker);
					}else{
						mcVal = Double.parseDouble(sbLine.get(fciSB.getIdx("market_cap")));
					}
					if(mcVal < this.mcRange.getStartVal() || mcVal > this.mcRange.getEndVal()){
						passes_mc = false;
					}
				}
				//System.out.println("Is In BD: "+is_in_bd+"  |  Passes Ind Rules: "+passes_ind_rules+"  |  Passes MC: "+passes_mc);
				if(is_in_bd && passes_ind_rules && passes_mc){
					vals.add(0, scode);
					vals.add(0, String.format("%.4f", mcVal));
					vals.add(0, ticker);
					this.results.add(vals);
				}
			}
		}
		long etime = System.currentTimeMillis();
		System.out.println("Elapsed Time : " + (etime - stime) + " ms");

		//print out results
		//System.out.println("==> Error Ticks on "+date+" ("+errTicks.size()+" total): "+errTicks);
		System.out.println("--> Final Results ... ");
		AhrAL.print(this.results);
	}

	//return single string of filter data
	public String getText(){
		String ftext = "";
		//add MC data
		ftext += "mc~";
		ftext += String.valueOf(this.mcRange.getStartVal())+"~";
		ftext += String.valueOf(this.mcRange.getEndVal());
		//add sector data
		ftext += "\nsector~";
		String secRaw = "";
		for(int i = 0; i < this.sectors.size(); i++){
			if(i == this.sectors.size()-1){
				secRaw += this.sectors.get(i);
			}else{
				secRaw += this.sectors.get(i) + ",";
			}
		}
		ftext += secRaw;
		//add industry data
		ftext += "\nindustry~";
		String indRaw = "";
		for(int i = 0; i < this.industries.size(); i++){
			if(i == this.industries.size()-1){
				indRaw += this.industries.get(i);
			}else{
				indRaw += this.industries.get(i) + ",";
			}
		}
		ftext += indRaw;	
		//add all indicator data
		for(int i = 0; i < this.indRanges.size(); i++){
			NameAndRange itrNAR = this.indRanges.get(i);
			ftext += "\n"+itrNAR.getName()+"~";
			ftext += String.valueOf(itrNAR.getStartVal())+"~";
			ftext += String.valueOf(itrNAR.getEndVal());
		}
		ftext += "\n";

		return ftext;		
	}

	//return all filter data into formatted data block
	public ArrayList<ArrayList<String>> getData(){
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		//add MC data
		ArrayList<String> mcLine = AhrAL.toAL(new String[]{"mc"});
		mcLine.add(String.valueOf(this.mcRange.getStartVal()));
		mcLine.add(String.valueOf(this.mcRange.getEndVal()));
		//add sector data
		ArrayList<String> sectorLine = AhrAL.toAL(new String[]{"sector"});
		String secRaw = "";
		for(int i = 0; i < this.sectors.size(); i++){
			if(i == this.sectors.size()-1){
				secRaw += this.sectors.get(i) + ",";
			}else{
				secRaw += this.sectors.get(i);
			}
		}
		sectorLine.add(secRaw);
		//add industry line 
		ArrayList<String> industryLine = AhrAL.toAL(new String[]{"industry"});
		String indRaw = "";
		for(int i = 0; i < this.industries.size(); i++){
			if(i == this.industries.size()-1){
				indRaw += this.industries.get(i) + ",";
			}else{
				indRaw += this.industries.get(i);
			}
		}
		industryLine.add(indRaw);
		//add above data together
		data.add(mcLine);
		data.add(sectorLine);
		data.add(industryLine);
		//add all indicator data
		for(int i = 0; i < this.indRanges.size(); i++){
			NameAndRange itrNAR = this.indRanges.get(i);
			ArrayList<String> indLine = AhrAL.toAL(new String[]{itrNAR.getName()});
			indLine.add(String.valueOf(itrNAR.getStartVal()));
			indLine.add(String.valueOf(itrNAR.getEndVal()));
			data.add(indLine);
		}

		return data;	
	}

	//rest filter to def vals
	public void resetFilter(){
		this.scodes = new ArrayList<ArrayList<String>>();
		this.results = new ArrayList<ArrayList<String>>();
		this.sectors = new ArrayList<String>();
		this.industries = new ArrayList<String>();
		this.mcRange = new NameAndRange("mc");
		this.indRanges = new ArrayList<NameAndRange>();
	}
	//reset just sector codes
	public void clearSectorCodes(){
		this.scodes = new ArrayList<ArrayList<String>>();
	}
	//reset just results al
	public void clearResults(){
		this.results = new ArrayList<ArrayList<String>>();
	}
	
	
}
