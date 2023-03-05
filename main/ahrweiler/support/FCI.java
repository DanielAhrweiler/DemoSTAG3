package ahrweiler.support;
import ahrweiler.util.AhrIO;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

//File Column Indexer
//Keeps track of all columns of all files used in TriStag. Allows for cols in files to
//be referenced by a tag instead of an index that would change if struct of file changes

public class FCI {
	//global vars
	boolean has_header;
	String path;
	ArrayList<String> columnTags;
	ArrayList<ArrayList<String>> log;
	//constructors
	public FCI(){
		this.has_header = false;
	}
	public FCI(boolean b1, String filePath){
		//first fill log AL with all column tags from ./../in/fci_log.txt file
		this.log = AhrIO.scanFile("./../in/fci_log.txt", ",");

		//fill rest of values from input
		this.has_header = b1;
		this.path = filePath;

		//TODO fix and remove this section
		if(!this.has_header){
			if(this.path.contains("./../out/sk/baseis/")){
				this.path = "./../out/sk/baseis/";
			}
			if(this.path.contains("./../out/ak/baseis/")){
				this.path = "./../out/ak/baseis/";
			}
		}


		this.columnTags = new ArrayList<String>();
		if(this.has_header){
			ArrayList<ArrayList<String>> fc = AhrIO.scanFile(this.path, "@");
			String rawHeader = fc.get(0).get(0);
			if(rawHeader.contains("//")){
				String[] splitHead1 = rawHeader.split(",");
				for(int i = 0; i < splitHead1.length; i++){
					String[] splitHead2 = splitHead1[i].split(" ");
					this.columnTags.add(splitHead2[splitHead2.length-1]);
				}
			}else{
				System.out.println("WARNING: Actual Header? ==> " + rawHeader);
			}
		}else{
			//if path is not in fci_log, then check for a more general path that is
			ArrayList<String> line = new ArrayList<String>();
			boolean is_path_in_file = false;
			for(int i = 0; i < this.log.size(); i++){
				if(this.log.get(i).get(0).equals(this.path)){
					is_path_in_file = true;
					line = this.log.get(i);
					line.remove(0);
					this.columnTags = line;
				}
			}
			if(!is_path_in_file){
				for(int i = 0; i < this.log.size(); i++){
					if(this.path.contains(this.log.get(i).get(0))){
						line = this.log.get(i);
						line.remove(0);
						this.columnTags = line;					
					}
				}
			}
		}



		//System.out.println("Col Tags: " + this.columnTags);
	}

	
	//functions
	public void setDelim(String delim){
		if(this.has_header){
			this.columnTags = new ArrayList<String>();
			ArrayList<String> header = AhrIO.scanRow(this.path, delim, 0);
			for(int i = 0; i < header.size(); i++){
				String[] splitHead2 = header.get(i).split(" ");
				this.columnTags.add(splitHead2[splitHead2.length-1]);
			}
		}
	}

	public boolean getHasHeader(){
		return this.has_header;
	}
	public String getTag(int idx){
		return this.columnTags.get(idx);
	}
	public ArrayList<String> getTags(){
		return this.columnTags;
	}
	public int getIdx(String tag){
		int index = this.columnTags.indexOf(tag);
		return index;
	}
	public int getNumOfCols(){
		return this.columnTags.size();
	}
	public int convertTVI(int tviVal){//like to have headers represent the actual tvi time period
		int tap = tviVal;			// instead of the tv index number so need to translate
		if(tviVal == 4){			//tap = target actual period
			tap = 5;
		}else if(tviVal == 5){
			tap = 10;
		}
		return tap;
	}
}//LLIF
