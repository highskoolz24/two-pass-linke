/**
 * @author Valentine Choi <jhc591@nyu.edu>
 * @Course CSCI-UA 202: Operating System
 * 
 * <Two Pass Linker>
 * The linker takes scanner standard input. By using 2D ArrayList, the data is manipulated and stored to be
 * used during the first and second pass. 
 * 
 * The first pass determines the base address for each module and 
 * absolute address of the symbol(in the coding, referred as value) 
 * 
 * The second pass uses the symbol address and base address to generate memory map by relocating relative 
 * addresses and resolving external references.
 */

import java.util.ArrayList;
import java.util.Scanner;


public class twoPassLinker {

	public static void main(String[] args) {
		
		//Scanner and store data in 2D and 1D arrayLists
		Scanner sc = new Scanner(System.in);
		
		ArrayList<ArrayList<String>> symbolAl = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<Integer>> valueAl = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<String>> useSymAl = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<Integer>> useIdAl = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> baseAd = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> addressAl = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> finalAd = new ArrayList<Integer>();
		
		int machineSize = 200;
		int numMod = sc.nextInt();
		int defNum;
		int useNum;
		int adNum;
		int base = 0;
		String tempSym;
		int tempVal;
		String tempUse;
		int tempId;
		int tempAd;
		
		//First-pass
		
		//For-loop for each modules 
		for (int i = 0; i < numMod; i++) {
			defNum = sc.nextInt();
			ArrayList<String> symbol = new ArrayList<String>();
			ArrayList<Integer> value = new ArrayList<Integer>();
			//For-loop for definition
			for (int j = 0; j < defNum; ++j) {
				tempSym = sc.next();
				tempVal = sc.nextInt();
				tempVal += base;
				symbol.add(tempSym);
				value.add(tempVal);
			}
			symbolAl.add(symbol);
			valueAl.add(value);
			
			useNum = sc.nextInt();
			ArrayList<String> useSym = new ArrayList<String>();
			ArrayList<Integer> useId = new ArrayList<Integer>();
			//For-loop for use
			for(int j = 0; j < useNum; j++) {
				tempUse = sc.next();
				tempId = sc.nextInt();
				useSym.add(tempUse);
				useId.add(tempId);
			}
			useSymAl.add(useSym);
			useIdAl.add(useId);
			
			adNum= sc.nextInt();
			
			//Error catching if definition index of a symbol exceeds the relative base address of the module
			for(int j = 0; j < valueAl.get(i).size(); j++) {
				if (valueAl.get(i).get(j)-base >= adNum) {
					valueAl.get(i).set(j, base);
					System.out.println("-------------\n"+"Error: The definition of "+symbolAl.get(i).get(j)+" is outside module "+i+"; zero (relative) used.");
				}
			}
			
			baseAd.add(adNum);
			base+=adNum;
			ArrayList<Integer> address = new ArrayList<Integer>();
			//For-loop for address
			for(int j = 0; j < adNum; j++) {
				tempAd = sc.nextInt();
				address.add(tempAd);
			}
			addressAl.add(address);
		}
		
		//Second-pass
		
		//Reconstruct symbol data and preparing arrayList to check if a symbol is used for error catching
		ArrayList<String> symbolAlFinal = new ArrayList<String>();
		ArrayList<Integer> valueAlFinal = new ArrayList<Integer>();
		ArrayList<Integer> checkSymUse = new ArrayList<Integer>();

		for(int i = 0; i < symbolAl.size(); i++) {
			for(int j = 0; j < symbolAl.get(i).size(); j++) {
				if(!symbolAlFinal.contains(symbolAl.get(i).get(j))) {
					symbolAlFinal.add(symbolAl.get(i).get(j));
					valueAlFinal.add(valueAl.get(i).get(j));
					checkSymUse.add(0);
				}
				else {
					int dupIndex = symbolAlFinal.indexOf(symbolAl.get(i).get(j));
					checkSymUse.set(dupIndex, 2);
				}
			}
		}
		
		//Resolve External symbols
		ArrayList<Integer> tempIndex = new ArrayList<Integer>();
		ArrayList<Integer> tempAddress = new ArrayList<Integer>();
		ArrayList<String> undefinedSym = new ArrayList<String>();
		
		for(int i = 0; i < useSymAl.size(); i++) {
			for(int j = 0; j < useSymAl.get(i).size();j++) {
				 int curAdd = addressAl.get(i).get(useIdAl.get(i).get(j));
				 int indexCalc = curAdd / 10 % 1000;
				 int tempPlus = 0;
				 tempIndex.add(useIdAl.get(i).get(j));
				 if(curAdd % 10 != 5) {
				 	while (indexCalc != 777) {
				 		 tempIndex.add(indexCalc);
				 		 curAdd = addressAl.get(i).get(indexCalc);
				 		 indexCalc = curAdd / 10 % 1000;
				 	 }
				 }
				 
				 else {
					tempIndex.clear();
					for(int k = 0; k < addressAl.get(i).size(); k++) {
						if(addressAl.get(i).get(k)%10000 == indexCalc*10+5) {
							tempIndex.add(k);
						}
					}
				 }
			     
				 for (int k = 0; k < symbolAlFinal.size(); k++) {
					 if(symbolAlFinal.get(k).equals(useSymAl.get(i).get(j))) {
						 tempPlus = valueAlFinal.get(k);
						 if(checkSymUse.get(k)==0) {
							 checkSymUse.set(k, 1); 
						 }
						 else if(checkSymUse.get(k)==2) {
							 checkSymUse.set(k, 3);
						 }
						 break;
					 }
				 }
				 //Error catching symbol in use list is undefined or E type address is not on use chain
				 if(tempPlus==0) {
					 if(!symbolAlFinal.contains(useSymAl.get(i).get(j))) {
						 for(int er = 0; er<tempIndex.size(); er++) {
							 undefinedSym.add(useSymAl.get(i).get(j)); 
						 }
					 }
				 }
				 
				 //Manipulating last digit of external addresses to use for error catching
				 for(int k = 0; k<tempIndex.size(); k++) {
					 curAdd = addressAl.get(i).get(tempIndex.get(k));
					 int last = curAdd % 10;
					 int first = curAdd / 10000;
					 if(tempPlus == 0) {
						 if(!symbolAlFinal.contains(useSymAl.get(i).get(j))) {
							 last=7;
						 }
						 curAdd = first * 10000 + tempPlus * 10 + last;
					 }
					 else if(last == 1) {
						 last = 8;
						 curAdd = first * 10000 + tempPlus * 10 + last;
					 }
					 else if(last == 5) {
						 last = 6;
						 curAdd = first * 10000 + tempPlus * 10 + last;
					 }

					 else {
						 last = 5;
						 curAdd = first * 10000 + tempPlus * 10 + last;
					 }
					 
					 tempAddress.add(curAdd);
				 }
				 for(int k = 0; k< tempIndex.size();k++) {
					 addressAl.get(i).set(tempIndex.get(k), tempAddress.get(k)); 
				 }
				 tempIndex.clear();
				 tempAddress.clear();
			}
		}
		
		//Dividing different types of addresses and error catching by last digit 
		ArrayList<Integer> checkAddress = new ArrayList<Integer>();
		for(int i = 0; i < numMod; i++) {
			int adSize = addressAl.get(i).size(); 
			for(int j=0;  j < adSize;j++) {
				int adOrig = addressAl.get(i).get(j);
				int adRefine = adOrig / 10;
				int instruct = adOrig % 10;
				switch(instruct){
					case 1:
						finalAd.add(adRefine);
						checkAddress.add(0);
						break;
					case 2:
						if(adRefine % 1000 > machineSize-1) {
							int first = adRefine / 1000;
							adRefine = first * 1000 + (machineSize-1);
							finalAd.add(adRefine);
							checkAddress.add(5);
						}
						else {
							finalAd.add(adRefine);
							checkAddress.add(0);
						}
						break;
					case 3:
						int baseAdd = 0;
						for(int k = 0; k < i; k++) {
							baseAdd += baseAd.get(k);
						}
						adRefine += baseAdd;
						finalAd.add(adRefine);
						checkAddress.add(0);
						break;
					case 4:
						finalAd.add(adRefine);
						checkAddress.add(1);
						break;
					case 5:
						finalAd.add(adRefine);
						checkAddress.add(0);
						break;
					case 6:
						finalAd.add(adRefine);
						checkAddress.add(2);
						break;
					case 7:
						finalAd.add(adRefine);
						checkAddress.add(3);
						break;
					case 8:
						finalAd.add(adRefine);
						checkAddress.add(4);
					default:
						break;
				}
			}
		}
		
		//Printing format with cases of errors
		System.out.print("-------------\nSymbol Table\n");
		for(int i = 0; i<valueAlFinal.size(); i++) {
			if(checkSymUse.get(i)==1) {
				System.out.println(symbolAlFinal.get(i)+" = "+valueAlFinal.get(i));
			}
			else if(checkSymUse.get(i)==0) {
				System.out.print(symbolAlFinal.get(i)+" = "+valueAlFinal.get(i));
				System.out.println("      Warning: "+symbolAlFinal.get(i) + " was defined but not used.");
			}
			else if(checkSymUse.get(i)==2) {
				System.out.print(symbolAlFinal.get(i)+" = "+valueAlFinal.get(i));
				System.out.print("      Warning: "+symbolAlFinal.get(i) + " was defined but not used.");
				System.out.println("           Error: "+symbolAlFinal.get(i) + " is multiply defined; first value used.");
			}
			else if(checkSymUse.get(i)==3) {
				System.out.print(symbolAlFinal.get(i)+" = "+valueAlFinal.get(i));
				System.out.println("      Error: "+symbolAlFinal.get(i) + " is multiply defined; first value used.");
			}
			
		}
		System.out.println("-------------\nMemory Map");
		int undefinedIndex = 0;
		for(int i = 0; i<finalAd.size(); i++) {
			if(checkAddress.get(i)==0) {
				System.out.println(i+":  "+finalAd.get(i));
			}
			else if(checkAddress.get(i)==1) {
				System.out.print(i+":  "+finalAd.get(i));
				System.out.println("      Error: E type address not on use chain; treated as I type.");
			}
			else if(checkAddress.get(i)==2) {
				System.out.print(i+":  "+finalAd.get(i));
				System.out.println("      Error: multiple symbols are listed as used; last usuage applied.");
			}
			else if(checkAddress.get(i)==3) {
				System.out.print(i+":  "+finalAd.get(i));
				System.out.println("      Error: "+undefinedSym.get(undefinedIndex)+" is not defined; zero used.");
			}
			else if(checkAddress.get(i)==4) {
				System.out.print(i+":  "+finalAd.get(i));
				System.out.println("      Error: Immediate address on use list; treated as External.");
			}
			else if(checkAddress.get(i)==5) {
				System.out.print(i+":  "+finalAd.get(i));
				System.out.println("           Error: absolute address exceeded the size of the machine. Substituted with largest size. (Size = "+ machineSize +")");
			}
			
		}
	}
}
