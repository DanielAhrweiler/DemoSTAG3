package ahrweiler.util;
import ahrweiler.Globals;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Collections;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.sql.*;

public class AhrIO {

	//File scanner that returns all file contents packed in a nested arraylist
	public static ArrayList<ArrayList<String>> scanFile(String path, String delim){
		ArrayList<ArrayList<String>> contents = new ArrayList<ArrayList<String>>();
		String pLine = "";			//reads txt file into this string to be manipulated
		try{
	    	BufferedReader file_in = new BufferedReader(new FileReader(path));
	    	while(file_in.ready()){
				pLine = file_in.readLine();
				String[] arr = new String[pLine.split(delim).length];
				arr = pLine.split(delim);
				ArrayList<String> singleLine = new ArrayList<String>();
				for(int i = 0; i < arr.length; i++){
					singleLine.add(arr[i]);
				}
				contents.add(singleLine);
			}
		}catch(FileNotFoundException e){
			System.out.println("FileNotFoundException: " + e.getMessage());
		}catch(IOException e){
			System.out.println("IOException: " + e.getMessage());
		}	
		return contents;
	}

	//prints 2D array of strings to a file
	public static void writeToFile(String path, ArrayList<ArrayList<String>> pts, String delim){
		//check if file exists
		File f = new File(path);
		//Write any 2D array list to file
		try{
		    BufferedWriter writer = new BufferedWriter(new FileWriter(path, false));
	    	for(int i = 0; i < pts.size(); i++){
	        	String s = pts.get(i).get(0);
	        	for(int j = 1; j < pts.get(i).size(); j++){
		    		s += delim + pts.get(i).get(j);
				}		 
				writer.write(s);
				writer.newLine();
	    	}   
	    	writer.close();
		}catch(IOException e){
	    	System.out.println("IOException: " + e.getMessage());
		}
	}

	//File scanner that returns a row with specified 1st col val
	public static ArrayList<String> scanRow(String path, String delim, String rowName){
		ArrayList<String> line = new ArrayList<String>();
		boolean rowReached = false;
		try{
	    	BufferedReader inFile = new BufferedReader(new FileReader(path));	
			while(inFile.ready() && !rowReached){
				String rawLine = inFile.readLine();
				String[] arrLine = rawLine.split(delim);
				if(arrLine[0].equals(rowName)){
					for(int i = 0; i < arrLine.length; i++){
						line.add(arrLine[i]);
					}
					rowReached = true;
				}
			}
		}catch(FileNotFoundException e){
			//System.out.println("FileNotFoundException: " + e.getMessage());
		}catch(IOException e){
			//System.out.println("IOException: " + e.getMessage());
		}
		return line;
	}

	//File scanner that returns a row with specified 1st col val
	public static ArrayList<String> scanRow(String path, String delim, int rowIdx){
		ArrayList<String> line = new ArrayList<String>();
		boolean rowReached = false;
		int rowCount = 0;
		try{
	    	BufferedReader inFile = new BufferedReader(new FileReader(path));	
			while(inFile.ready() && !rowReached){
				String rawLine = inFile.readLine();
				String[] arrLine = rawLine.split(delim);
				if(rowCount == rowIdx){
					for(int i = 0; i < arrLine.length; i++){
						line.add(arrLine[i]);
					}
					rowReached = true;				
				}
				rowCount++;
			}
		}catch(FileNotFoundException e){
			//System.out.println("FileNotFoundException: " + e.getMessage());
		}catch(IOException e){
			//System.out.println("IOException: " + e.getMessage());
		}
		return line;
	}

	//File scanner that returns all rows that match specified col val
	public static ArrayList<ArrayList<String>> scanSelectRows(String path, String delim, String rowVal, int colIdx){
		ArrayList<ArrayList<String>> content = new ArrayList<ArrayList<String>>();
		try{
	    	BufferedReader file_in = new BufferedReader(new FileReader(path));
	    	while(file_in.ready()){
				String rawLine = file_in.readLine();
				String[] arr = rawLine.split(delim);
				if(arr[colIdx].equals(rowVal)){
					ArrayList<String> singleLine = new ArrayList<String>();
					for(int i = 0; i < arr.length; i++){
						singleLine.add(arr[i]);
					}
					content.add(singleLine);
				}
			}
		}catch(FileNotFoundException e){
			System.out.println("FileNotFoundException: " + e.getMessage());
		}catch(IOException e){
			System.out.println("IOException: " + e.getMessage());
		}
		return content;
	}

	//File scanner that returns a rnd row from a file or folder of files
	public static ArrayList<String> scanRndRow(String path, String delim, boolean is_folder){
		ArrayList<String> row = new ArrayList<String>();
		Random rnd = new Random();
		if(is_folder){	//sel row from folder of files
			ArrayList<String> files = getFilesInPath(path);
			int fIdx = rnd.nextInt(files.size());
			ArrayList<ArrayList<String>> fc = scanFile(path+files.get(fIdx), delim);
			int rIdx = rnd.nextInt(fc.size());
			row = fc.get(rIdx);
		}else{			//sel row from just 1 file
			ArrayList<ArrayList<String>> fc = scanFile(path, delim);
			int rIdx = rnd.nextInt(fc.size());
			row = fc.get(rIdx);
		}
		return row;
	}

	//File scanner that returns a row with specified 1st col val
	public static String scanCell(String path, String delim, int rowIdx, int colIdx){
		String cellStr = "";
		boolean rowReached = false;
		int rowCount = 0;
		try{
	    	BufferedReader inFile = new BufferedReader(new FileReader(path));	
			while(inFile.ready() && !rowReached){
				String rawLine = inFile.readLine();
				String[] arrLine = rawLine.split(delim);
				cellStr = arrLine[colIdx];
				if(rowCount == rowIdx){
					rowReached = true;
				}			
				rowCount++;
			}
		}catch(FileNotFoundException e){
			//System.out.println("FileNotFoundException: " + e.getMessage());
		}catch(IOException e){
			//System.out.println("IOException: " + e.getMessage());
		}
		return cellStr;
	}
	
	//File scanner that returns a row with specified 1st col val
	public static String scanCell(String path, String delim, String rowName, int colIdx){
		String cellStr = "";
		boolean rowReached = false;
		try{
	    	BufferedReader inFile = new BufferedReader(new FileReader(path));	
			while(inFile.ready() && !rowReached){
				String rawLine = inFile.readLine();
				String[] arrLine = rawLine.split(delim);
				cellStr = arrLine[colIdx];
				if(arrLine[0].equals(rowName)){
					rowReached = true;
				}	
			}
		}catch(FileNotFoundException e){
			//System.out.println("FileNotFoundException: " + e.getMessage());
		}catch(IOException e){
			//System.out.println("IOException: " + e.getMessage());
		}
		return cellStr;
	}

	//File scanner that returns one column in a file alongside the inputs index col
	public static ArrayList<ArrayList<String>> scanColWithIndex(String path, String delim, int colIdx){
		ArrayList<ArrayList<String>> contents = new ArrayList<ArrayList<String>>();
		String pLine = "";			//reads txt file into this string to be manipulated
		try{
	    	BufferedReader file_in = new BufferedReader(new FileReader(path));
	    	while(file_in.ready()){
				pLine = file_in.readLine();
				String[] arr = new String[pLine.split(delim).length];
				arr = pLine.split(delim);
				ArrayList<String> line = new ArrayList<String>();
				line.add(arr[0]);
				line.add(arr[colIdx]);
				contents.add(line);
			}
		}catch(FileNotFoundException e){
			System.out.println("FileNotFoundException: " + e.getMessage());
		}catch(IOException e){
			System.out.println("IOException: " + e.getMessage());
		}	
		return contents;
	}

	//File scanner that returns one column in a file in a 1D AL
	public static ArrayList<String> scanCol(String path, String delim, int colIdx){
		ArrayList<String> contents = new ArrayList<String>();
		String pLine = "";			//reads txt file into this string to be manipulated
		try{
	    	BufferedReader file_in = new BufferedReader(new FileReader(path));
	    	while(file_in.ready()){
				pLine = file_in.readLine();
				String[] arr = new String[pLine.split(delim).length];
				arr = pLine.split(delim);
				contents.add(arr[colIdx]);
			}
		}catch(FileNotFoundException e){
			System.out.println("FileNotFoundException: " + e.getMessage());
		}catch(IOException e){
			System.out.println("IOException: " + e.getMessage());
		}	
		return contents;
	}

	/*-----------------------------------------------------------------------
		MySQL web server functions
	------------------------------------------------------------------------*/
/*
	//get entire table from MySQL
	public static ArrayList<ArrayList<String>> scanWebAll(String url, String username, String password, String tname){
		ArrayList<ArrayList<String>> al = new ArrayList<ArrayList<String>>();
		try{
			Connection conn = DriverManager.getConnection(url, username, password);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "+tname);
			int colNum = rs.getMetaData().getColumnCount();
			while(rs.next()){
				ArrayList<String> line = new ArrayList<String>();
				for(int i = 0; i < colNum; i++){
					line.add(rs.getObject(i).toString());
				}
				al.add(line);
			}
			//close the connection
			rs.close();
			stmt.close();
			conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return al;
	}

	//get data from MYSQL SBase web table
	public static ArrayList<ArrayList<String>> scanWebSBase(String[] colNames, String tname, String sdate,
															String edate, boolean reverse_data){
		ArrayList<ArrayList<String>> al = new ArrayList<ArrayList<String>>();
		String colStr = "";
		for(int i = 0; i < colNames.length; i++){
			if(i == (colNames.length-1)){
				colStr += colNames[i];
			}else{
				colStr += colNames[i]+", ";
			}
		}
		//server connection basics
		String url = Globals.mysql_sbase_path;
		String username = Globals.mysql_username;
		String password = Globals.mysql_password;
		//connect to db and fetch data
		try{
			Connection conn = DriverManager.getConnection(url, username, password);
			Statement stmt = conn.createStatement();
			String sqlSelect = "SELECT "+colStr+" FROM "+tname+" WHERE date BETWEEN '"+sdate+"' AND '"+edate+"'";
			System.out.println(sqlSelect);
			ResultSet rs = stmt.executeQuery(sqlSelect);
			int colCount = rs.getMetaData().getColumnCount();
			int rowCount = 0; 
			System.out.println("--> colCount = " + colCount);
			while(rs.next()){
				ArrayList<String> line = new ArrayList<String>();
				for(int i = 0; i < colCount; i++){
					//System.out.println(rs.getString(colNames[i]));
					line.add(rs.getString(colNames[i]));
				}
				al.add(line);
				rowCount++;
			}
			System.out.println("--> rowCount = "+rowCount);
			//close the connection
			rs.close();
			stmt.close();
			conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		if(reverse_data){
			Collections.reverse(al);
		}
		return al;
	}

	//get data from MYSQL SNorm web table
	public static ArrayList<ArrayList<String>> scanWebSNorm(String[] cols, String tname, String sdate,
														String edate, String narMask, boolean reverse_data){
		ArrayList<ArrayList<String>> al = new ArrayList<ArrayList<String>>();
		HashMap<String, Integer> cnIdxs = new HashMap<String, Integer>();
		//add NAR to col names if not there, create col part of command
		ArrayList<String> colNames = AhrAL.toAL(cols);
		boolean cols_has_nar = false;
		for(int i = 0; i < colNames.size(); i++){
			if(colNames.equals("nar_mask")){
				cols_has_nar = true;
			}
			cnIdxs.put(colNames.get(i), i);
		}
		if(!cols_has_nar){
			colNames.add("nar_mask");
			cnIdxs.put("nar_mask", colNames.size());
		}
		String colStr = "";
		for(int i = 0; i < colNames.size(); i++){
			if(i == (colNames.size()-1)){
				colStr += colNames.get(i);
			}else{
				colStr += colNames.get(i)+",";
			}
		}		
		//server data
		String url = Globals.mysql_snorm_path;
		String username = Globals.mysql_username;
		String password = Globals.mysql_password;
		//setup connection and get data
		try{
			Connection conn = DriverManager.getConnection(url, username, password);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT "+colStr+" FROM "+tname+" WHERE date BETWEEN '"+
											sdate+"' AND '"+edate+"'");
			int colNum = rs.getMetaData().getColumnCount();
			while(rs.next()){
				ArrayList<String> line = new ArrayList<String>();
				for(int i = 0; i < colNum; i++){
					line.add(rs.getObject(i).toString());
				}
				al.add(line);
			}
			//close the connection
			rs.close();
			stmt.close();
			conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		//filter out rows that dont have good NAR
		boolean is_dont_care_nar = true;
		for(int i = 0; i < narMask.length(); i++){
			if(narMask.charAt(i) != 'x' && narMask.charAt(i) != 'X'){
				is_dont_care_nar = false;
				break;
			}
		}
		if(reverse_data){
			Collections.reverse(al);
		}
		if(is_dont_care_nar){
			return al;
		}else{
			ArrayList<ArrayList<String>> passesNAR = new ArrayList<ArrayList<String>>();
			for(int i = 0; i < al.size(); i++){
				String itrNar = al.get(i).get(cnIdxs.get("nar_mask"));
				if(AhrGen.compareMasks(narMask, itrNar)){
					passesNAR.add(al.get(i));
				}
			}
			return passesNAR;
		}
	}

	//get data from MYSQL SBase web table
	public static ArrayList<ArrayList<String>> scanWebIBase(String[] colNames, String tname, String sdate,
															String edate, boolean reverse_data){
		ArrayList<ArrayList<String>> al = new ArrayList<ArrayList<String>>();
		String colStr = "";
		for(int i = 0; i < colNames.length; i++){
			if(i == (colNames.length-1)){
				colStr += colNames[i];
			}else{
				colStr += colNames[i]+", ";
			}
		}
		//server connection basics
		String url = Globals.mysql_ibase_path;
		String username = Globals.mysql_username;
		String password = Globals.mysql_password;
		//connect to db and fetch data
		try{
			Connection conn = DriverManager.getConnection(url, username, password);
			Statement stmt = conn.createStatement();
			String sqlSelect = "SELECT "+colStr+" FROM "+tname+" WHERE date BETWEEN '"+sdate+"' AND '"+edate+"'";
			System.out.println(sqlSelect);
			ResultSet rs = stmt.executeQuery(sqlSelect);
			int colCount = rs.getMetaData().getColumnCount();
			int rowCount = 0; 
			System.out.println("--> colCount = " + colCount);
			while(rs.next()){
				ArrayList<String> line = new ArrayList<String>();
				for(int i = 0; i < colCount; i++){
					//System.out.println(rs.getString(colNames[i]));
					line.add(rs.getString(colNames[i]));
				}
				al.add(line);
				rowCount++;
			}
			System.out.println("--> rowCount = "+rowCount);
			//close the connection
			rs.close();
			stmt.close();
			conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		if(reverse_data){
			Collections.reverse(al);
		}
		return al;
	}

	//get data from MYSQL SBase web table
	public static ArrayList<ArrayList<String>> scanWebMBase(String[] colNames, String tname, String sdate,
															String edate, boolean reverse_data){
		ArrayList<ArrayList<String>> al = new ArrayList<ArrayList<String>>();
		String colStr = "";
		for(int i = 0; i < colNames.length; i++){
			if(i == (colNames.length-1)){
				colStr += colNames[i];
			}else{
				colStr += colNames[i]+", ";
			}
		}
		//server connection basics
		String url = Globals.mysql_mbase_path;
		String username = Globals.mysql_username;
		String password = Globals.mysql_password;
		//connect to db and fetch data
		try{
			Connection conn = DriverManager.getConnection(url, username, password);
			Statement stmt = conn.createStatement();
			String sqlSelect = "SELECT "+colStr+" FROM "+tname+" WHERE date BETWEEN '"+sdate+"' AND '"+edate+"'";
			System.out.println(sqlSelect);
			ResultSet rs = stmt.executeQuery(sqlSelect);
			int colCount = rs.getMetaData().getColumnCount();
			int rowCount = 0; 
			System.out.println("--> colCount = " + colCount);
			while(rs.next()){
				ArrayList<String> line = new ArrayList<String>();
				for(int i = 0; i < colCount; i++){
					//System.out.println(rs.getString(colNames[i]));
					line.add(rs.getString(colNames[i]));
				}
				al.add(line);
				rowCount++;
			}
			System.out.println("--> rowCount = "+rowCount);
			//close the connection
			rs.close();
			stmt.close();
			conn.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
		if(reverse_data){
			Collections.reverse(al);
		}
		return al;
	}
*/

	/*------------------------------------------------------------------------
		Path functions
	-------------------------------------------------------------------------*/

	//get list of tickers from a certain file path
	//TODO get names by reading before period not by static 4 chars
	public static ArrayList<String> getNamesInPath(String path){
		File folder = new File(path);
		File[] lof = folder.listFiles();
		ArrayList<String> names = new ArrayList<String>();
		for(int a = 0; a < lof.length; a++){
			names.add(lof[a].getName().substring(0, lof[a].getName().length()-4));
		}
		return names;
    }
	//get list of tickers from a certain file path
	public static ArrayList<String> getFilesInPath(String path){
		File folder = new File(path);
		File[] lof = folder.listFiles();
		ArrayList<String> names = new ArrayList<String>();
		for(int a = 0; a < lof.length; a++){
			names.add(lof[a].getName());
		}
		return names;
	}
    
}
