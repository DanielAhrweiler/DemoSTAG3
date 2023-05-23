package ahrweiler.support;
import ahrweiler.util.AhrGen;
import java.util.ArrayList;
import java.util.Collections;
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
		this.has_nar_prereq = false;
		this.whereCondCount = 0;
		this.uses_all_cols = false;
	}

	//---------- Getters & Setters ----------
	public void setDB(String dbName){
		if(this.webSource.equals("hostgator")){
			this.url = "jdbc:mysql://danielahrweiler.com:3306/daniela5_"+dbName;
		}else if(this.webSource.equals("aws")){
			this.url = "jdbc:mysql://demo-stag.cudwbghcmyxb.us-east-2.rds.amazonaws.com:3306/"+dbName;

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
	//---------- Adders ----------
	public void addWhereCond(String wcond){
		if(this.whereCondCount == 0){
			this.whereCond = wcond;
		}else{
			String andCond = wcond.replace("WHERE", " AND");
			this.whereCond += " "+andCond;
		}
		this.whereCondCount++;
	}


	//MetaData functions
	public ArrayList<String> getTables(){
		ArrayList<String> tableList = new ArrayList<String>();
		try{
			Connection conn = DriverManager.getConnection(this.url, this.username, this.password);
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getTables(conn.getCatalog(), null, "%", null);
			while(rs.next()){
				tableList.add(rs.getString(3));
			}
			//close the connection
			rs.close();
			conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return tableList;
	}

	//CREATE functions
	public void create(String tname, ArrayList<String> colNames, ArrayList<String> dataTypes){
		if(colNames.size() == dataTypes.size()){
			try{
				Connection conn = DriverManager.getConnection(this.url, this.username, this.password);
				Statement stmt = conn.createStatement();
				String sqlCreate = "CREATE TABLE `"+tname+"` (";
				for(int i = 0; i < colNames.size(); i++){
					if(i == (colNames.size()-1)){
						sqlCreate += colNames.get(i)+"     "+dataTypes.get(i)+")";
					}else{
						sqlCreate += colNames.get(i)+"     "+dataTypes.get(i)+", ";
					}
				}
				//close the connection
				stmt.close();
				conn.close();
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
				Connection conn = DriverManager.getConnection(this.url, this.username, this.password);
				Statement stmt = conn.createStatement();
				String sqlCreate = "CREATE TABLE IF NOT EXISTS `"+tname+"` (";
				for(int i = 0; i < colNames.size(); i++){
					if(i == (colNames.size()-1)){
						sqlCreate += colNames.get(i)+"     "+dataTypes.get(i)+")";
					}else{
						sqlCreate += colNames.get(i)+"     "+dataTypes.get(i)+", ";
					}
				}
				stmt.execute(sqlCreate);
				//close the connection
				stmt.close();
				conn.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		}else{
			System.out.println("SQLCode ERR : Num of cols & Num of data types are diff len.");
		}
	}

	//TRUNCATE functions
	public void truncate(String tname){
		try{
			Connection conn = DriverManager.getConnection(this.url, this.username, this.password);
			Statement stmt = conn.createStatement();
			String sqlTruncate = "TRUNCATE TABLE `"+tname+"`";
			stmt.execute(sqlTruncate);
			//close the connection
			stmt.close();
			conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	//TODO
	//INSERT functions
	public void multiInsert(String tname, ArrayList<String> colNames){
		boolean need_quotes = false;
		if(colNames.equals("DATE") || colNames.contains("CHAR")){
			need_quotes = true;
		}
	}

	//SELECT functions
	public ArrayList<ArrayList<String>> selectAll(String tname, ArrayList<String> colNames){
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		try{
			Connection conn = DriverManager.getConnection(this.url, this.username, this.password);
			Statement stmt = conn.createStatement();
			String sqlSelect = "SELECT * FROM `"+tname+"`";
			ResultSet rs = stmt.executeQuery(sqlSelect);
			//System.out.println(sqlSelect);
			int colNum = rs.getMetaData().getColumnCount();
			//System.out.println("--> colNum = " + colNum+"\n--> colNames size = "+colNames.size());
			while(rs.next()){
				ArrayList<String> line = new ArrayList<String>();
				for(int i = 0; i < colNum; i++){
					line.add(rs.getString(colNames.get(i)));
				}
				data.add(line);
			}
			//close the connection
			rs.close();
			stmt.close();
			conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return data;
	}
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
			Connection conn = DriverManager.getConnection(this.url, this.username, this.password);
			Statement stmt = conn.createStatement();
			String sqlSelect = "";
			if(this.has_date_range){
				sqlSelect = "SELECT "+colStr+" FROM `"+tname+"` WHERE date BETWEEN '"+this.sdate+"' AND '"+this.edate+"'";
			}else{
				sqlSelect = "SELECT "+colStr+" FROM `"+tname+"`";
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
			stmt.close();
			conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return data;
	}
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
			Connection conn = DriverManager.getConnection(this.url, this.username, this.password);
			Statement stmt = conn.createStatement();
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
			stmt.close();
			conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return data;
	}
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
			Connection conn = DriverManager.getConnection(this.url, this.username, this.password);
			Statement stmt = conn.createStatement();
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
			conn.close();
			stmt.close();
			rs.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return data;
	}

	//------------ Specific Situation Functions ----------
		
	//replace all appr NULL vals in bydate DB w/ 0.0 vals
	public void fixNullInByDate(){
		setDB("bydate");
		String amqURL = this.url+"?allowMultiQueries=true";
		ArrayList<String> tableList = new ArrayList<String>();
		try{
			Connection conn = DriverManager.getConnection(amqURL, this.username, this.password);
			//Statement stmt = conn.createStatement();
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
				try(Statement stmt = conn.createStatement()){
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
					stmt.execute(sqlUpdate);			
				}catch(SQLException e2){
					e2.printStackTrace();
				}
			}
			//close connection
			conn.close();
		}catch(SQLException e1){
			e1.printStackTrace();
		}
	}

}
