package ahrweiler.util;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

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
