package ahrweiler.support;
import ahrweiler.util.AhrGen;
import ahrweiler.util.AhrDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.HashMap;
import java.sql.*;
import javax.swing.JOptionPane;

public class SQLCode {

	//basic vars for connection
	private String url;
	private String username;
	private String password;	
	private boolean has_date_range;
	private String sdate;
	private String edate;
	private boolean has_nar_prereq;
	private String narMask;
	private int whereCondCount;
	private String whereCond;
	private String webSource;
	private boolean uses_all_cols;

	//connection vars
	Connection conn;
	Statement stmt;
	boolean has_active_connection = false;

	//----------- Constructors ----------
	public SQLCode(){
		this.webSource = "hostgator";
		this.url = "jdbc:mysql://danielahrweiler.com:3306/daniela5_sbase";
		this.username = "daniela5_client";
		this.password = "gKvMZvj6wZ34";
		this.has_date_range = false;
		this.has_nar_prereq = false;
		this.whereCondCount = 0;
		this.uses_all_cols = false;
	}
	public SQLCode(String sourceName){
		this.webSource = sourceName.toLowerCase();
		if(this.webSource.equals("hostgator")){
			this.url = "jdbc:mysql://danielahrweiler.com:3306/daniela5_sbase";
			this.username = "daniela5_client";
			this.password = "gKvMZvj6wZ34";
		}else if(this.webSource.equals("aws")){
			this.url = "jdbc:mysql://demo-stag.cudwbghcmyxb.us-east-2.rds.amazonaws.com:3306/sbase";
			this.username = "client";
			this.password = "two.Lester34";
		}else{
			String message = "Web source "+this.webSource+" not recognized.";
			JOptionPane.showMessageDialog(null, message, "Web Error", JOptionPane.ERROR_MESSAGE);
		}
		this.has_date_range = false;
	}

	//---------- Getters & Setters ----------
	public void setDB(String dbName){
		if(this.webSource.equals("hostgator")){
			this.url = "jdbc:mysql://danielahrweiler.com:3306/daniela5_"+dbName;
		}else if(this.webSource.equals("aws")){
			this.url = "jdbc:mysql://demo-stag.cudwbghcmyxb.us-east-2.rds.amazonaws.com:3306/"+dbName;
		}
		if(this.has_active_connection){
			connect();
		}
	}
	public String getUsername(){
		return this.username;
	}
	public void setUsername(String uname){
		this.username = uname;
	}
	public void setPassword(String pword){
		this.password = pword;
	}
	public void setWhereCond(String wcond, int numOfConds){
		this.whereCond = wcond;
		this.whereCondCount = numOfConds;
	}
	public void setDateRange(String sdate, String edate){
		this.sdate = sdate;
		this.edate = edate;
		this.has_date_range = true;
	}
	public String getNar(){
		return this.narMask;
	}
	public void setNar(String narMask){
		this.narMask = narMask;
		for(int i = 0; i < narMask.length(); i++){
			if(narMask.charAt(i) != 'x' && narMask.charAt(i) != 'X'){
				this.has_nar_prereq = true;
				break;
			}
		}
	}
	public boolean getUsesAllCols(){
		return this.uses_all_cols;
	}
	public void setUsesAllCols(boolean all_cols){
		this.uses_all_cols = all_cols;
	}
	public String getHostGatorURL(String dbName){
		return "jdbc:mysql://danielahrweiler.com:3306/daniela5_"+dbName;
	}
	public String getAWSURL(String dbName){
		return "jdbc:mysql://demo-stag.cudwbghcmyxb.us-east-2.rds.amazonaws.com:3306/"+dbName;
	}
	public boolean hasActiveConnection(){
		return this.has_active_connection;
	}

	//------------- Adders -------------
	public void addWhereCond(String wcond){
		if(this.whereCondCount == 0){
			this.whereCond = wcond;
		}else{
			String andCond = wcond.replace("WHERE", " AND");
			this.whereCond += " "+andCond;
		}
		this.whereCondCount++;
	}

	//------------- Connection -----------------
	public void connect(){
		try{
			if(this.has_active_connection){
				close();
			}
			this.conn = DriverManager.getConnection(this.url, this.username, this.password);
			this.stmt = conn.createStatement();
			has_active_connection = true;
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void close(){
		try{
			conn.close();
			stmt.close();
			has_active_connection = false;
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	//-------------- MetaData ----------------
	public ArrayList<String> getTables(){
		ArrayList<String> tableList = new ArrayList<String>();
		try{
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getTables(conn.getCatalog(), null, "%", null);
			while(rs.next()){
				tableList.add(rs.getString(3));
			}
			//close the connection
			rs.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return tableList;
	}

	//------------ Select Commands ---------------
	//get and entire table
	public ArrayList<ArrayList<String>> selectAll(String tname, ArrayList<String> colNames){
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		try{
			String sqlSelect = "SELECT * FROM `"+tname+"`";
			if(this.whereCondCount > 0){
				sqlSelect += this.whereCond;
			}
			ResultSet rs = stmt.executeQuery(sqlSelect);
			while(rs.next()){
				ArrayList<String> line = new ArrayList<String>();
				for(int i = 0; i < colNames.size(); i++){
					line.add(rs.getString(colNames.get(i)));
				}
				data.add(line);
			}
			rs.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return data;
	}
	//get specific cols from table
	public ArrayList<ArrayList<String>> selectCols(String tname, ArrayList<String> colNames){
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		//construct single string for col section of SQL command
		if(this.has_nar_prereq && !colNames.contains("nar_mask")){
			colNames.add("nar_mask");
		}
		String colStr = "";
		for(int i = 0; i < colNames.size(); i++){
			if(i == (colNames.size()-1)){
				colStr += colNames.get(i);
			}else{
				colStr += colNames.get(i)+", ";
			}
		}
		//connect to DB and fetch data
		try{
			String sqlSelect = "";
			if(this.has_date_range){
				sqlSelect = "SELECT "+colStr+" FROM `"+tname+"` WHERE date BETWEEN '"+this.sdate+"' AND '"+this.edate+"'";
			}else{
				if(this.whereCondCount > 0){
					sqlSelect = "SELECT "+colStr+" FROM `"+tname+"` "+this.whereCond;
				}else{
					sqlSelect = "SELECT "+colStr+" FROM `"+tname+"`";
				}
			}
			//System.out.println(sqlSelect);
			ResultSet rs = stmt.executeQuery(sqlSelect);			
			int colNum = rs.getMetaData().getColumnCount();
			while(rs.next()){
				ArrayList<String> line = new ArrayList<String>();
				for(int i = 0; i < colNum; i++){
					line.add(rs.getString(colNames.get(i)));
				}
				data.add(line);
			}
			//filter out rows that dont have good NAR
			if(this.has_nar_prereq){
				int narColIdx = colNames.indexOf("nar_mask");
				ArrayList<ArrayList<String>> passNar = new ArrayList<ArrayList<String>>();
				for(int i = 0; i < data.size(); i++){
					String itrNar = data.get(i).get(narColIdx);
					if(AhrGen.compareMasks(this.narMask, itrNar)){
						passNar.add(data.get(i));
					}
				}
				data = passNar;
			}
			//close the connection
			rs.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return data;
	}
	//get specific row from table
	public ArrayList<String> selectRow(String tname, ArrayList<String> colNames, String rowName, boolean row_needs_quotes){
		ArrayList<String> data = new ArrayList<String>();
		//construct single string for col section of SQL command
		String colStr = "";
		if(this.uses_all_cols){
			colStr = "*";
		}else{
			for(int i = 0; i < colNames.size(); i++){
				if(i == (colNames.size()-1)){
					colStr += colNames.get(i);
				}else{
					colStr += colNames.get(i)+", ";
				}
			}
		}
		//connect to DB and fetch data
		try{
			String sqlSelect = "";
			if(this.whereCondCount > 0){
				sqlSelect = "SELECT "+colStr+" FROM `"+tname+"` "+this.whereCond;
			}else{
				sqlSelect = "SELECT "+colStr+" FROM `"+tname+"` WHERE `"+colNames.get(0)+"` = ";
			}
			if(row_needs_quotes){
				sqlSelect += "'"+rowName+"'";
			}else{
				sqlSelect += rowName;
			}
			//System.out.println(sqlSelect);
			ResultSet rs = stmt.executeQuery(sqlSelect);			
			int colNum = rs.getMetaData().getColumnCount();
			if(rs.next()){
				for(int i = 0; i < colNum; i++){
					data.add(rs.getString(colNames.get(i)));
				}
			}
			//close the connection
			rs.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return data;
	}
	//get data from mult tables
	public ArrayList<ArrayList<String>> selectUnion(ArrayList<String> tnames, ArrayList<String> colNames){
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		//construct single string for col section of SQL command
		String colStr = "";
		if(this.uses_all_cols){
			colStr = "*";
		}else{
			for(int i = 0; i < colNames.size(); i++){
				if(i == (colNames.size()-1)){
					colStr += colNames.get(i);
				}else{
					colStr += colNames.get(i)+", ";
				}
			}
		}
		//connect to DB and fetch data
		try{
			String sqlSelect = "";
			for(int i = 0; i < tnames.size(); i++){
				sqlSelect += "SELECT "+colStr+" FROM `"+tnames.get(i)+"` ";
				if(this.whereCondCount > 0){
					sqlSelect += this.whereCond;
				}
				if(i != (tnames.size()-1)){
					sqlSelect += "\nUNION\n";
				}
			}
			//System.out.println(sqlSelect);
			ResultSet rs = stmt.executeQuery(sqlSelect);
			while(rs.next()){
				ArrayList<String> line = new ArrayList<String>();
				for(int j = 0; j < colNames.size(); j++){
					line.add(rs.getString(colNames.get(j)));
				}
				data.add(line);
			}
			//close the connections
			rs.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return data;
	}

	//-------------- Create ------------------
	public void create(String tname, ArrayList<String> colNames, ArrayList<String> dataTypes){
		if(colNames.size() == dataTypes.size()){
			try{
				String sqlCreate = "CREATE TABLE `"+tname+"` (";
				for(int i = 0; i < colNames.size(); i++){
					if(i == (colNames.size()-1)){
						sqlCreate += colNames.get(i)+"     "+dataTypes.get(i)+")";
					}else{
						sqlCreate += colNames.get(i)+"     "+dataTypes.get(i)+", ";
					}
				}
				stmt.execute(sqlCreate);
			}catch(SQLException e){
				e.printStackTrace();
			}
		}else{
			System.out.println("SQLCode ERR : Num of cols & Num of data types are diff len.");
		}
	}
	public void createIfNotExists(String tname, ArrayList<String> colNames, ArrayList<String> dataTypes){
		if(colNames.size() == dataTypes.size()){
			try{
				String sqlCreate = "CREATE TABLE IF NOT EXISTS `"+tname+"` (";
				for(int i = 0; i < colNames.size(); i++){
					if(i == (colNames.size()-1)){
						sqlCreate += colNames.get(i)+"     "+dataTypes.get(i)+")";
					}else{
						sqlCreate += colNames.get(i)+"     "+dataTypes.get(i)+", ";
					}
				}
				stmt.execute(sqlCreate);
			}catch(SQLException e){
				e.printStackTrace();
			}
		}else{
			System.out.println("SQLCode ERR : Num of cols & Num of data types are diff len.");
		}
	}

	//------------- TRUNCATE -----------------
	public void truncate(String tname){
		try{
			String sqlTruncate = "TRUNCATE TABLE `"+tname+"`";
			stmt.execute(sqlTruncate);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	//------------- INSERT ----------------


	//------------- Specific situation functions ----------------

	//get data for a RND basis file w/ rnd selection from bydate DB
	public ArrayList<ArrayList<String>> genBasisRnd(ArrayList<String> bdFiles, String tvColName, int spd){
		System.out.println("--> bdFiles size = "+bdFiles.size());
		Random rnd = new Random();
		ArrayList<ArrayList<String>> basis = new ArrayList<ArrayList<String>>();
		int sectionSize = 200;
		int sections = (bdFiles.size() / sectionSize) + 1;
		if(bdFiles.size()%sectionSize == 0){
			sections--;
		}
		String colStr = "ticker, date, "+tvColName;
		//connect to DB and fetch data
		try{
			for(int i = 0; i < sections; i++){
				ArrayList<String> tnames = new ArrayList<String>();
				for(int j = (i*sectionSize); j < ((i+1)*sectionSize); j++){
					if(j >= bdFiles.size()){
						break;
					}
					tnames.add(bdFiles.get(j));
				}
				String sqlSelect = "";
				for(int x = 0; x < tnames.size(); x++){
					String sqlLine = "SELECT "+colStr+" FROM `"+tnames.get(x)+"` ";
					sqlSelect += sqlLine;
					if(x != (tnames.size()-1)){
						sqlSelect += "\nUNION\n";
					}else{
						System.out.println(sqlLine);
					}
				}
				//System.out.println(sqlSelect);
				ResultSet rs = stmt.executeQuery(sqlSelect);
				HashMap<String, ArrayList<ArrayList<String>>> data = new HashMap<String, ArrayList<ArrayList<String>>>();			
				ArrayList<String> justDates = new ArrayList<String>();
				while(rs.next()){
					//System.out.println(rs.getString("date"));
					String itrTick = rs.getString("ticker");
					String itrDate = rs.getString("date");
					String itrAppr = rs.getString(tvColName);
					if(data.containsKey(itrDate)){
						ArrayList<ArrayList<String>> buf = data.get(itrDate);
						if(buf.size() < spd){//add new line regardless
							ArrayList<String> basisLine = new ArrayList<String>();
							basisLine.add(itrDate);
							basisLine.add("ph");
							basisLine.add("ph");
							basisLine.add(itrTick);
							basisLine.add(String.valueOf(buf.size()));
							basisLine.add(itrAppr);
							buf.add(basisLine);
							data.put(itrDate, buf);
						}else{//add new line if randomly chosen
							double rndVal = rnd.nextDouble();
							int year = Integer.parseInt(itrDate.split("-")[0]);
							int avgFileSize = 3000;
							if(year > 2017){
								avgFileSize = 3200;
							}
							if(year > 2019){
								avgFileSize = 3400;
							}
							if(rndVal <= ((double)spd/(double)avgFileSize)){
								int rndIdx = rnd.nextInt(spd);
								ArrayList<String> basisLine = new ArrayList<String>();
								basisLine.add(itrDate);
								basisLine.add("ph");
								basisLine.add("ph");
								basisLine.add(itrTick);
								basisLine.add(String.valueOf(rndIdx));
								basisLine.add(itrAppr);
								buf.set(rndIdx, basisLine);
								data.put(itrDate, buf);
							}
						}
					}else{//add date to hashmap
						justDates.add(itrDate);
						ArrayList<ArrayList<String>> buf = new ArrayList<ArrayList<String>>();
						ArrayList<String> basisLine = new ArrayList<String>();
						basisLine.add(itrDate);
						basisLine.add("ph");
						basisLine.add("ph");
						basisLine.add(itrTick);
						basisLine.add("0");
						basisLine.add(itrAppr);
						buf.add(basisLine);
						data.put(itrDate, buf);
					}
				}
				//sort dates and write basis file in order
				AhrDate.sortDates(justDates, true);
				for(int j = 0; j < justDates.size(); j++){
					ArrayList<ArrayList<String>> basisSection = data.get(justDates.get(j));
					for(int k = 0; k < basisSection.size(); k++){
						basis.add(basisSection.get(k));
					}
					data.remove(justDates.get(j));
				}
				/*
				ArrayList<ArrayList<String>> buf = new ArrayList<ArrayList<String>>();
				String focusDate = "";
				if(rs.next()){
					ArrayList<String> line = new ArrayList<String>();
					line.add(rs.getString("ticker"));
					focusDate = rs.getString("date");
					line.add(focusDate);
					line.add(rs.getString(tvColName));
					buf.add(line);
				}
				while(rs.next()){
					String newDate = rs.getString("date");
					System.out.println("date: "+newDate);
					//get rnd lines from buf to add to basis
					if(!newDate.equals(focusDate)){
						int count = 0;
						ArrayList<String> uniqTicks = new ArrayList<String>();
						while(count < spd){
							int rndIdx = rnd.nextInt(buf.size());
							String bufTick = buf.get(rndIdx).get(0);
							if(!uniqTicks.contains(bufTick)){
								ArrayList<String> basisLine = new ArrayList<String>();
								basisLine.add(focusDate);					//[0] date
								basisLine.add("ph");						//[1] sk num
								basisLine.add("ph");						//[2] ttv code
								basisLine.add(bufTick);						//[3] ticker
								basisLine.add(String.valueOf(count));		//[4] score
								basisLine.add(buf.get(rndIdx).get(2));		//[5] appr
								System.out.println("main: "+basisLine);
								basis.add(basisLine);
								count++;
								uniqTicks.add(bufTick);
							}
						}
						focusDate = newDate;
						buf = new ArrayList<ArrayList<String>>();
					}
					//add line to buf
					ArrayList<String> line = new ArrayList<String>();
					line.add(rs.getString("ticker"));
					line.add(newDate);
					line.add(rs.getString(tvColName));
					buf.add(line);
				}
				//have to add to basis file the latest date also (the non-empty buf)
				if(buf.size() > spd){
					int count = 0;
					ArrayList<String> uniqTicks = new ArrayList<String>();
					while(count < spd){
						int rndIdx = rnd.nextInt(buf.size());
						String bufTick = buf.get(rndIdx).get(0);
						if(!uniqTicks.contains(bufTick)){
							ArrayList<String> basisLine = new ArrayList<String>();
							basisLine.add(focusDate);					//[0] date
							basisLine.add("ph");						//[1] sk num
							basisLine.add("ph");						//[2] ttv code
							basisLine.add(bufTick);						//[3] ticker
							basisLine.add(String.valueOf(count));		//[4] score
							basisLine.add(buf.get(rndIdx).get(2));		//[5] appr
							System.out.println("last: "+basisLine);
							basis.add(basisLine);
							count++;
							uniqTicks.add(bufTick);
						}
					}				
				}
				*/
				//close the connections
				rs.close();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return basis;
	}

	//replace all appr NULL vals in bydate DB w/ 0.0 vals
	public void fixNullInByDate(){
		setDB("bydate");
		String amqURL = this.url+"?allowMultiQueries=true";
		ArrayList<String> tableList = new ArrayList<String>();
		try{
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getTables(conn.getCatalog(), null, "%", null);
			while(rs.next()){
				tableList.add(rs.getString(3));
			}
			Collections.sort(tableList);
			//System.out.println("**** Fix Null In ByDate ****");
			for(int i = 0; i < tableList.size(); i++){
				//System.out.println("--> bydate : "+tableList.get(i));
				//if(i%100 == 0){
				//	System.out.println("   "+i+" out of "+tableList.size());
				//}
				try(Statement updateStmt = conn.createStatement()){
					String sqlUpdate = "UPDATE `"+tableList.get(i)+"` SET `appr_intra1` = 0.0 WHERE `appr_intra1` IS NULL;"
								+"\nUPDATE `"+tableList.get(i)+"` SET `appr_inter1` = 0.0 WHERE `appr_inter1` IS NULL;"
								+"\nUPDATE `"+tableList.get(i)+"` SET `appr_intra2` = 0.0 WHERE `appr_intra2` IS NULL;"
								+"\nUPDATE `"+tableList.get(i)+"` SET `appr_inter2` = 0.0 WHERE `appr_inter2` IS NULL;"
								+"\nUPDATE `"+tableList.get(i)+"` SET `appr_intra3` = 0.0 WHERE `appr_intra3` IS NULL;"
								+"\nUPDATE `"+tableList.get(i)+"` SET `appr_inter3` = 0.0 WHERE `appr_inter3` IS NULL;"
								+"\nUPDATE `"+tableList.get(i)+"` SET `appr_intra5` = 0.0 WHERE `appr_intra5` IS NULL;"
								+"\nUPDATE `"+tableList.get(i)+"` SET `appr_inter5` = 0.0 WHERE `appr_inter5` IS NULL;"
								+"\nUPDATE `"+tableList.get(i)+"` SET `appr_intra10` = 0.0 WHERE `appr_intra10` IS NULL;"
								+"\nUPDATE `"+tableList.get(i)+"` SET `appr_inter10` = 0.0 WHERE `appr_inter10` IS NULL;";
					//System.out.println(sqlUpdate);
					updateStmt.execute(sqlUpdate);			
				}catch(SQLException e2){
					e2.printStackTrace();
				}
			}
		}catch(SQLException e1){
			e1.printStackTrace();
		}
	}

}
