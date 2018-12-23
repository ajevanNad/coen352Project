import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/**
 * class with tools that can be used to preprocess the file with image vectors (.csv file)
 * @author Ajevan
 *
 */
public class PreprocessFile {
	
	//list of keys (which are the ceiling of the cityblock distance)
	private ArrayList<Integer> keyList = new ArrayList<Integer>(255);
	
	//array of value 255 to represent a white image, which will be used as reference to calculate the cityblock distance
	private static int[] referenceWhite;
	
	/**
	 * constructor in which the size of an image vector is provided.
	 * Important to make an instance of this class (use this constructor to initialize referenceWhite)
	 * @param size the length of an image vector
	 */
	public PreprocessFile(int size) {
		referenceWhite = new int[size];
		for(int i = 0; i < size; i++) {
			referenceWhite[i] = 255;
		}
	}
	
	/**
	 * get the list of keys
	 * @return int arrayList of keys
	 */
	public ArrayList<Integer> getKeyList() {
		return keyList;
	}
	
	/**
	 * get the referenceWhite array
	 * @return referenceWhite
	 */
	public int[] getReferenceWhite() {
		return referenceWhite;
	}
	
	/**
	 * calculate the cityblock distance between the provided image and the reference white image
	 * @param v vector of the image
	 * @param r vector of the reference image
	 * @return the city block distance
	 */
	public static double calCityBlockDist(int[] r, int[] v) {
		double total = 0; //the summation of the differences of each pixel
		
		for(int i = 0; i < v.length; i++) {
			total += Math.abs(r[i] - v[i]);
		}
		
		return total / (double) v.length;
	}
	
	/**
	 * convert a string of ints array into an int array
	 * @param a string array to convert
	 * @return int array
	 */
	public static int[] convertStringArrayToInt(String[] a) {
		
		int[] converted = new int[a.length];
		
		for(int i = 0; i < a.length; i++) {
			converted[i] = Integer.parseInt(a[i]);
		}
		
		return converted;
	}
	
	/**
	 * Separate the image vectors into files based on their city block distance
	 * @param filepath path of the original file (ending with two backward slash)
	 * @param filename name of the original file
	 */
	public void seperateFileOnDist(String filepath, String filename) {
		
		File originalFile = new File(filepath + filename);
		
		try {
			Scanner sc = new Scanner(originalFile);
			
			while(sc.hasNextLine()) {
				
				//vector that's not processed (ex: 1,2,3,4)
				String rawVector = sc.nextLine();
				
				//vector that's processed & each pixel is in an array index
				String[] stringVector = rawVector.split(",");
				int[] intVector = convertStringArrayToInt(stringVector);
				
				double cityBlockDist = calCityBlockDist(referenceWhite, intVector); //get cityblock distance ex: 244.56
				int intcityBlockDist = (int) Math.floor(cityBlockDist); //get cityblock distance int ex: 244
				keyList.add(intcityBlockDist); //add key to list
				
				//put associated vector in its key file ex: keyFile244
				FileWriter filewriter = new FileWriter(filepath + "keyFile" + intcityBlockDist + ".csv", true);
				BufferedWriter bufferedwriter = new BufferedWriter(filewriter);
				
				//put in file the cityblock distance & the vector itself
				bufferedwriter.write(cityBlockDist + " " + rawVector + '\n');
				
				bufferedwriter.close();
				filewriter.close();
			}
			sc.close();
			
			//sort each keyFile that was created in ascending order
			divideAndSort(filepath);
		}
		catch(Exception ex) {
			
		}
	}
	
	/**
	 * divide a file with more than 10,000 image vectors into several files of 10,000 pieces so that in memory
	 * sort can be used (ex: Collections.sort), and then each of the sorted results can be merged backed to the file 
	 * @param filepath location of where the file that needs to be divided is located
	 */
	private void divideAndSort(String filepath) {
		
		for(Integer keynumber : keyList) {
			
			int numOfLines = 0; //number of lines so far
			int count = 1; //number of extra files one file is separated into; min is one extra file with copy
			
			try {
				
				//original key file ex: keyFile245
				File inputFile = new File(filepath + "keyFile" + keynumber + ".csv");
				Scanner sc = new Scanner(inputFile);
				
				//output file after dividing ex: keyFile245-1 <-- 1 here means 1 extra file created
				FileWriter outfilewriter = new FileWriter(filepath + "keyFile" + keynumber + "-" + count + ".csv");
				BufferedWriter bufferedwriter = new BufferedWriter(outfilewriter);
				
				while(sc.hasNextLine()) {
					String line = sc.nextLine();
					bufferedwriter.write(line + '\n');
					numOfLines++;
					
					if(numOfLines >= 10000) {
						
						//reset number of lines and close current output file
						numOfLines = 0;
						bufferedwriter.close();
						outfilewriter.close();
						
						count++;
						//open new output file to continue writing on
						outfilewriter = new FileWriter(filepath + "keyFile" + keynumber + "-" + count + ".csv");
						bufferedwriter = new BufferedWriter(outfilewriter);
						
					}
				}
				//close everything
				sc.close();
				bufferedwriter.close();
				outfilewriter.close();
				
				//sort the first piece
				sortFile(filepath, "keyFile" + keynumber + "-" + 1 + ".csv");
				
				//sort and merge every other piece
				for(int i = 2; i <= count; i++) {
					
					sortFile(filepath, "keyFile" + keynumber + "-" + i + ".csv"); //sort the next piece
					merge(filepath, "keyFile" + keynumber, "-1.csv", "-" + i + ".csv"); //merge this sorted piece with 1st sorted piece
				}
				
				//write everything that was merged into 1st piece back to the original keyFile ex:keyFile244
				outfilewriter = new FileWriter(filepath + "keyFile" + keynumber + ".csv");
				bufferedwriter = new BufferedWriter(outfilewriter);
				
				File file = new File(filepath + "keyFile" + keynumber + "-" + 1 + ".csv");
				sc = new Scanner(file);
				
				while(sc.hasNextLine()) {
					bufferedwriter.write(sc.nextLine() + '\n');
				}
				
				sc.close();
				bufferedwriter.close();
				outfilewriter.close();
				
				//delete all the extra pieces of files that were made
				for(int i = 1; i <= count; i++) {
					File fileTodel = new File(filepath + "keyFile" + keynumber + "-" + i + ".csv");
					fileTodel.delete();
				}
				
			}
			catch(Exception ex) {
				
			}
		}
	}
	
	/**
	 * since divideAndSort is splitting the files in order to sort them, these files need to be merged back together
	 * using this method
	 * @param filepath location of where the files are
	 * @param keyFileNumber name of the original key File (ex: keyFile244)
	 * @param count1 the file to be merged in (ex: -1)
	 * @param count2 the file that will be merged (ex: -2)
	 */
	private void merge(String filepath, String keyFileNumber, String count1, String count2) {
		
		try {
			//temporary keyFile to write back to
			FileWriter filewriter = new FileWriter(filepath + keyFileNumber + "TEMP.csv");
			BufferedWriter bufferedwriter = new BufferedWriter(filewriter);
			
			//1st input file 
			File inputFile1 = new File(filepath + keyFileNumber + count1);
			Scanner sc1 = new Scanner(inputFile1);
			
			//2nd input file; which will be merged with the 1st
			File inputFile2 = new File(filepath + keyFileNumber + count2);
			Scanner sc2 = new Scanner(inputFile2);
			
			boolean read1 = true; //to know if line1 has been written down
			boolean read2 = true; //to know if line2 has been written down
			
			String line1 = null; //line from the first file
			String line2 = null; //line from the second file
			
			while(sc1.hasNextLine() || sc2.hasNextLine()) {
				
				if(!sc1.hasNextLine()) {
					bufferedwriter.write(sc2.nextLine() + '\n');
					continue;
				}
				else if(!sc2.hasNextLine()) {
					bufferedwriter.write(sc1.nextLine() + '\n');
					continue;
				}
				
				if(read1) {
					line1 = sc1.nextLine();
					read1 = false;
				}
				if(read2) {
					line2 = sc2.nextLine();
					read2 = false;
				}
				
				if(compareDist(line1, line2) <= 0) {
					bufferedwriter.write(line1 + '\n');
					read1 = true;
				}
				else if(compareDist(line2, line1) < 0) {
					bufferedwriter.write(line2 + '\n');
					read2 = true;
				}
			}
			
			sc1.close();
			sc2.close();
			bufferedwriter.close();
			filewriter.close();
			
			//append back to original keyFile
			filewriter = new FileWriter(filepath + keyFileNumber + count1);
			bufferedwriter = new BufferedWriter(filewriter);
			
			//read from the TEMP keyfile
			File file = new File(filepath + keyFileNumber + "TEMP.csv");
			Scanner sc = new Scanner(file);
			
			while(sc.hasNextLine()) {
				bufferedwriter.write(sc.nextLine() + '\n');
			}
			
			sc.close();
			bufferedwriter.close();
			filewriter.close();
			file.delete();
			
		}
		catch(Exception ex) {
			
		}
	}
	
	/**
	 * compare the city block distances with each image vector string
	 * @param a the first image vector string
	 * @param b the second image vector string
	 * @return -1 if a < b, 0 if a == b, 1 if a > b
	 */
	public int compareDist(String a, String b) {
		
		//use the city block distance associated with each image vector to decide if the image vector is in the 
		//correct order
		//ex: 244.23 2,3,4,5
		//    212.14 5,6,7,5
		//in this case the order is not correct and will output 1
		int cmp = Double.valueOf(a.replaceAll("(^\\d+(\\.\\d+)?)\\s+.+", "$1"))
		.compareTo(Double.valueOf(b.replaceAll("(^\\d+(\\.\\d+)?)\\s+.+", "$1")));
		
		if(cmp < 0) {
			return -1;
		}
		else if(cmp > 0) {
			return 1;
		}
		else {
			return 0;
		}
	}
	
	/**
	 * sort a file with cityblock distance and image vectors according to city block distance
	 * @param filepath
	 * @param filename
	 */
	private void sortFile(String filepath, String filename) {
		
		File file = new File(filepath + filename);
		ArrayList<String> distPlusVector = new ArrayList<String>(10000); //array with image vectors & cityblock distance
		
		try {
			Scanner sc = new Scanner(file);
			
			//get all the vectors with city block distance into an array so that it can be sorted
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				distPlusVector.add(line);
			}
			sc.close();
			
			//sort using the custom comparator for comparing
			Collections.sort(distPlusVector, new BlockDistComparator());
			
			FileWriter filewriter = new FileWriter(filepath + filename);
			BufferedWriter bufferedwriter = new BufferedWriter(filewriter);
			
			//write the sorted array back to file
			for(String s : distPlusVector) {
				bufferedwriter.write(s + '\n');
			}
			
			bufferedwriter.close();
			filewriter.close();
			
		}
		catch(Exception ex) {
			
		}
		
	}
	
	/**
	 * class that implements comparator so that this custom compare method can be used to decide if in order based
	 * on city block distances
	 * @author Ajevan
	 *
	 */
	class BlockDistComparator implements Comparator<String>{
		@Override
		public int compare(String a, String b) {
			
			//use the city block distance associated with each image vector to decide if the image vector is in the 
			//correct order
			//ex: 244.23 2,3,4,5
			//    212.14 5,6,7,5
			//in this case the order is not correct and will output 1
			int cmp = Double.valueOf(a.replaceAll("(^\\d+(\\.\\d+)?)\\s+.+", "$1"))
			.compareTo(Double.valueOf(b.replaceAll("(^\\d+(\\.\\d+)?)\\s+.+", "$1")));
			
			if(cmp < 0) {
				return -1;
			}
			else if(cmp > 0) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

}
