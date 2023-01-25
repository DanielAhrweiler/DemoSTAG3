package ahrweiler.util;
import ahrweiler.util.AhrIO;
import ahrweiler.support.FCI;
import java.util.ArrayList;

public class AhrGen {

	//checks if string is convertable to int or not
	public static boolean isInt(String str){
		if(str == null){
			return false;
		}
		int len = str.length();
		if(len == 0){
			return false;
		}
		int i = 0;
		if(str.charAt(i) == '-'){
			if(len == 1){
				return false;
			}
			i = 1;
		}
		for(; i < len; i++){
			char c = str.charAt(i);
			if(c < '0' || c > '9'){
				return false;
			}
		}
		return true;		
	}

	//================= Mask Functions =============================

	//compare two mask strings, sees if itr mask fits base mask
	//baseMask can have 'x' vals, itrMask cannot
	public static boolean compareMasks(String baseMask, String itrMask){
		boolean is_match = true;
		if(itrMask.length() == baseMask.length()){
			for(int i = 0; i < itrMask.length(); i++){
				if(baseMask.charAt(i) != 'x'){
					if(baseMask.charAt(i) != itrMask.charAt(i)){
						is_match = false;
					}
				}
			}
		}else{
			System.out.println("ERR: In CM, Masks diff lengths.\n--> Mask1 = "+baseMask+"\n--> Mask2 = "+itrMask);
			is_match = false;
			/*
			if(baseMask.length() < itrMask.length()){
				itrMask = itrMask.substring(0, baseMask.length());
				is_match = compareMasks(baseMask, itrMask);
			}else{
				baseMask = baseMask.substring(0, itrMask.length());
				is_match = compareMasks(baseMask, itrMask);
			}
			*/
		}
		return is_match;
	}
	//check if a mask is useless. Assume 1st masks has worse perf, 2nd mask has better perf
	public static boolean isMaskUseless(String mask1, String mask2){
		boolean is_useless = true;
		if(mask1.length() == mask2.length()){
			for(int i = 0; i < mask1.length(); i++){
				char char1 = mask1.charAt(i);
				char char2 = mask2.charAt(i);
				if(char2 != 'x' && char1 != char2){
					is_useless = false;
				}
			}
		}else{
			System.out.println("ERR: In IMU, Masks diff lengths.");
		}
		return is_useless;
	}

	//go thru every bitmask possibilty with 2 masks and count the 4 bin totals
	//[0] fits mask1 only, [1] fits mask2 only, [3] fits both, [4] fits neither
	public static int[] binMasks(String mask1, String mask2){
		int[] bins = {0, 0, 0, 0};
		int totItrCount = (int)Math.pow(2.0, (double)mask1.length());
		for(int i = 0; i < totItrCount; i++){
			String bitmask = Integer.toBinaryString(i);
			while(bitmask.length() < mask1.length()){
				bitmask = "0" + bitmask;
			}
			//check to see if the bitmask matches with the 2 masks
			boolean matchesMask1 = compareMasks(mask1, bitmask);
			boolean matchesMask2 = compareMasks(mask2, bitmask);
			if(matchesMask1 && !matchesMask2){			//[0] fits mask1 only
				bins[0] += 1;
			}else if(!matchesMask1 && matchesMask2){	//[1] fits mask2 only
				bins[1] += 1;
			}else if(matchesMask1 && matchesMask2){		//[2] fits both masks
				bins[2] += 1;
			}else{										//[3] fits neither mask
				bins[3] += 1;
			}
		}
		//determined if masks match (dont want for this funct atm)
		//boolean masks_match = false;
		//if(bins[0] == 0 || bins[1] == 0){
		//	masks_match = true;
		//}
		return bins;	
	}
	
	//check if list of masks met all states, return false if not met, true if all states met
	public static boolean allStatesMet(ArrayList<String> masks){
		boolean all_states_met = true;
		if(masks.size() > 0){
			int bits = masks.get(0).length();
			int combos = (int)Math.pow(2.0, (double)bits);
			for(int i = 0; i < combos; i++){
				String itrState = Integer.toBinaryString(i);
				while(itrState.length() < bits){
					itrState = "0" + itrState;
				}
				boolean single_state_met = false;
				for(int j = 0; j < masks.size(); j++){
					//System.out.print("Mask: "+masks.get(j)+"  |  State: "+itrState+"  ==> ");
					if(compareMasks(masks.get(j), itrState)){
						single_state_met = true;
						//System.out.println("TRUE");
					}else{
						//System.out.println("FALSE");
					}
				}
				if(!single_state_met){
					all_states_met = false;
				}
			}
		}
		return all_states_met;
	}

	//simple contains function for string array
	public static boolean contains(String[] arr, String ele){
		boolean is_ele_in_array = false;
		for(int i = 0; i < arr.length; i++){
			if(arr[i].equals(ele)){
				is_ele_in_array = true;
			}
		}
		return is_ele_in_array;
	}

	//calc index of given ele in a String[]
	public static int indexOf(String[] arr, String ele){
		int idx = -1;
		for(int i = 0; i < arr.length; i++){
			if(arr[i].equals(ele)){
				idx = i;
			}
		}
		return idx;
	}

	//covert a MC val that has T,B, or M into raw millions value
	public static String mcToRawMillions(String mcStr){
		char lastChar = mcStr.charAt(mcStr.length()-1);
		mcStr = mcStr.substring(0, mcStr.length()-1);
		double mcDbl = 0.0;
		if(lastChar == 'T'){
			try{
				mcDbl = Double.parseDouble(mcStr);
				mcDbl = mcDbl * 1000000;
			}catch(NumberFormatException e){
				System.out.println("NumberFormatException, tried to convert to double : " + mcStr);
			}
		}else if(lastChar == 'B'){
			try{
				mcDbl = Double.parseDouble(mcStr);
				mcDbl = mcDbl * 1000;
			}catch(NumberFormatException e){
				System.out.println("NumberFormatException, tried to convert to double : " + mcStr);
			}
		}else if(lastChar == 'M'){
			try{
				mcDbl = Double.parseDouble(mcStr);
			}catch(NumberFormatException e){
				System.out.println("NumberFormatException, tried to convert to double : " + mcStr);
			}
		}else{
			return "errNT";
		}
		return String.format("%.3f", mcDbl);
	}

}
