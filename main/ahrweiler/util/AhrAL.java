package ahrweiler.util;
import java.util.ArrayList;

public class AhrAL {

	//converts String[] to String AL
	public static ArrayList<String> toAL(String[] arr){
		ArrayList<String> al = new ArrayList<String>();
		for(int i = 0; i < arr.length; i++){
			al.add(arr[i]);
		}
		return al;
	}

	//converts String AL to Array
	public static String[][] toArr(ArrayList<ArrayList<String>> al){
		String[][] arr = new String[al.size()][al.get(0).size()];
		for(int i = 0; i < al.size(); i++){
			for(int j = 0; j < al.get(0).size(); j++){
				arr[i][j] = al.get(i).get(j);
			}
		}
		return arr;
	}

	//convert string AL to double AL
	public static ArrayList<Double> convertStoD(ArrayList<String> sal){
		ArrayList<Double> dal = new ArrayList<Double>();
		for(int i = 0; i < sal.size(); i++){
			dal.add(Double.parseDouble(sal.get(i)));
		}
		return dal;
	}

	//convert double AL to string AL
	public static ArrayList<String> convertDtoS(ArrayList<Double> dal){
		ArrayList<String> sal = new ArrayList<String>();
		for(int i = 0; i < dal.size(); i++){
			sal.add(String.format("%.5f", dal.get(i)));
		}
		return sal;
	}

	//return row of AL with given row name
	public static ArrayList<String> getRow(ArrayList<ArrayList<String>> al, String rowName){
		ArrayList<String> line = new ArrayList<String>();
		for(int i = 0; i < al.size(); i++){
			if(al.get(i).get(0).equals(rowName)){
				line = al.get(i);
				break;
			}
		}
		return line;
	}
	
	//get all rows of given cell val for a specific col
	public static ArrayList<ArrayList<String>> getSelectRows(ArrayList<ArrayList<String>> al, 
												String cellVal, int colIdx){
		ArrayList<ArrayList<String>> outAL = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < al.size(); i++){
			if(al.get(i).get(colIdx).equals(cellVal)){
				outAL.add(al.get(i));
			}
		}
		return outAL;
	}

	//get index in AL of given row
	public static int getRowIdx(ArrayList<ArrayList<String>> al, String rowName){
		int rowIdx = -1;
		for(int i = 0; i < al.size(); i++){
			if(al.get(i).get(0).equals(rowName)){
				rowIdx = i;
				break;
			}
		}
		return rowIdx;
	}
	
	//return col of AL with given col idx
	public static ArrayList<String> getCol(ArrayList<ArrayList<String>> al, int colIdx){
		ArrayList<String> line = new ArrayList<String>();
		for(int i = 0; i < al.size(); i++){
			line.add(al.get(i).get(colIdx));
		}
		return line;
	}
	
	//add a string[] to a string 2D AL
	public static ArrayList<ArrayList<String>> addArrayToAL(ArrayList<ArrayList<String>> al, String[] arr){
		ArrayList<String> line = new ArrayList<String>();
		for(int i = 0; i < arr.length; i++){
			line.add(arr[i]);
		}
		al.add(line);
		return al;
	}

	//count instances of all unique strs in AL
	public static ArrayList<ArrayList<String>> countUniq(ArrayList<String> al){
		//get list of uniq items in AL
		ArrayList<String> uniq = new ArrayList<String>();
		for(int i = 0; i < al.size(); i++){
			if(!uniq.contains(al.get(i))){
				uniq.add(al.get(i));
			}
		}
		//get count of each uniq item
		ArrayList<Integer> counts = new ArrayList<Integer>();
		for(int i = 0; i < uniq.size(); i++){
			counts.add(0);
		}
		for(int i = 0; i < al.size(); i++){
			int idx = uniq.indexOf(al.get(i));
			counts.set(idx, counts.get(idx) + 1);
		}
		//merge into 2d AL and return
		ArrayList<ArrayList<String>> cuniq = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < uniq.size(); i++){
			ArrayList<String> line = new ArrayList<String>();
			line.add(String.valueOf(counts.get(i)));
			line.add(uniq.get(i));
			cuniq.add(line);
		}
		return cuniq;
	}

	//given 2 ALs itr thru all divide all eles
	public static ArrayList<String> divideSAL(ArrayList<String> al1, ArrayList<String> al2){
		ArrayList<String> res = new ArrayList<String>();
		for(int i = 0; i < al1.size(); i++){
			if(!al1.get(i).equals("NA") && !al1.get(i).equals("NaN")){
				if(!al2.get(i).equals("NA") && !al2.get(i).equals("NaN")){
					double val1 = Double.parseDouble(al1.get(i));
					val1 = val1 / Double.parseDouble(al2.get(i));
					res.add(String.format("%.7f", val1));
				}else{
					res.add(al2.get(i));
				}
			}else{
				res.add(al1.get(i));
			}
		}
		return res;
	}


}
