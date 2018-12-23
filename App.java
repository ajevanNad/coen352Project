import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The main application to run in order to preprocess the image vectors and search for similar images
 * @author Ajevan
 *
 */
public class App {

	public static void main(String[] args) {
		
		System.out.println("Enter the file path for where the file with the image vectors are located: \n\n"
				+ "- include the path excluding the file name \n"
				+ "- use backslashes \n"
				+ "- end path with backslash \n"
				+ "- if the file is in the same directory as this application, then leave filepath empty \n"
				+ "- Ex: C:\\Users\\Ajevan\\Desktop\\datasets\\");
		
		Scanner sc = new Scanner(System.in);
		String filepath = sc.nextLine();
		
		System.out.println("Enter the file name of the file with the image vectors \n"
				+ "Ex: dataset.csv");
		
		String filename = sc.nextLine();
//		sc.close();
		
		File imagesFile = new File(filepath + filename);
		
		try {
			Scanner sc1 = new Scanner(imagesFile);
			
			//the number of pixels in each image
			//so that the reference image can be initialized with this size
			int numOfPixels;
			
			if(sc1.hasNextLine()) {
				numOfPixels = sc1.nextLine().split(",").length;
				sc1.close();
			}
			else {
				sc1.close();
				return;
			}
			
			final long startTimePreProc = System.nanoTime();
			
			PreprocessFile preprocess = new PreprocessFile(numOfPixels);
			preprocess.seperateFileOnDist(filepath, filename);
			
			//the keys, which are based on the distance to the reference image
			ArrayList<Integer> keys = preprocess.getKeyList();
			BPlusTree<Integer,String> db = new BPlusTree<Integer,String>(128);
			
			//insert in the tree the key (ex:244) and value (ex: C:\Desktop\keyFile244.csv)
			for(int i = 0; i < keys.size(); i++) {
				db.insert(keys.get(i), filepath + "keyFile" + keys.get(i) + ".csv");
			}
			
			final long durationPreProc = System.nanoTime() - startTimePreProc;
			System.out.println("THE TIME FOR PREPROCESSING (IN SEC) = " + durationPreProc/1000000000);
			
			while(true) {
			
				System.out.println("Enter the complete file path and file name which contains the image vector "
						+ "for which you want to find similar images: \n"
						+ "Ex: C:\\Users\\Ajevan\\Desktop\\datasets\\inputImage.csv");
				
				String inputImage = sc.nextLine();
				File inputImageFile = new File(inputImage);
				Scanner sc2 = new Scanner(inputImageFile);
				String[] rawCueVector;
				
				if(sc2.hasNextLine()) {
					rawCueVector = sc2.nextLine().split(",");
					sc2.close();
				}
				else {
					sc2.close();
					return;
				}
				
				int[] cueVector = PreprocessFile.convertStringArrayToInt(rawCueVector);
				
				System.out.println("Enter the threshold: \n"
						+ "Ex: 10");
				double threshold = Double.parseDouble(sc.nextLine());
				
				final long startTimeProc = System.nanoTime();
				
				//calculate the city block distance between the cue vector & the reference white 
				double cueCityBlockDist = PreprocessFile.calCityBlockDist(preprocess.getReferenceWhite(), cueVector);
				double minthreshold = cueCityBlockDist - threshold; //based on threshold, the min cityblock distance included
				double maxthreshold = cueCityBlockDist + threshold; //based on threshold, the max cityblock distance included
				int intMinthreshold = (int) minthreshold; //same as min threshold but integer so can be used as key
				int intMaxthreshold = (int) maxthreshold; //same as max threshold but integer so can be used as key
				
				//list of all files that contain image vectors within the threshold distance
				List<String> picWithinDist = db.searchRange(intMinthreshold, BPlusTree.RangePolicy.INCLUSIVE, 
						intMaxthreshold, BPlusTree.RangePolicy.INCLUSIVE);
				
				//go through each of the files that are within threshold
				for(String file : picWithinDist) {
					File currentFile = new File(file);
					Scanner sc3 = new Scanner(currentFile);
					
					while(sc3.hasNextLine()) {
						String currentLine = sc3.nextLine(); //the complete line in the file
						
						//the associated city block distance for this image vector
						double assocDist = Double.valueOf(currentLine.replaceAll("(^\\d+(\\.\\d+)?)\\s+.+", "$1"));
						
						//if the associated city block distance is above the threshold, no need to continue to look
						//since sorted, can get out of this file
						if(assocDist > maxthreshold) {
							break;
						}
						
						//if the associated city block distance is below the threshold, then skip to next one
						else if(assocDist < minthreshold) {
							continue;
						}
						else {
							
							//contains only the image vector without the associated distance
							String currentVector = currentLine.replaceAll("(^\\d+(\\.\\d+)?)\\s+(.+)", "$3");
							int[] intArrayCurrentVector = PreprocessFile.convertStringArrayToInt(currentVector.split(","));
							
							//get the exact distance between the current image vector and the cue vector
							//instead of the distance with the reference image
							double exactDist = PreprocessFile.calCityBlockDist(cueVector, intArrayCurrentVector);
							
							//if the exact distance is within the threshold, then found match and can add to output file
							if(exactDist <= threshold) {
								FileWriter filewriter = new FileWriter(filepath + "ImagesWithinThreshold.csv", true);
								BufferedWriter bufferedwriter = new BufferedWriter(filewriter);
								bufferedwriter.write(currentVector + '\n');
								
								bufferedwriter.close();
								filewriter.close();
							}
						}
					}
					sc3.close();
				}
				
				System.out.println("The images within the given threshold of the given image are saved in the file "
						+ "ImagesWithinThreshold.csv in " + filepath);
				
				final long durationProc = System.nanoTime() - startTimeProc; 
				System.out.println("THE TIME FOR PROCESSING (IN SEC) = " + durationProc/1000000000);
				
				System.out.println("Would you like to search for another image with another threshold (y/n):");
				String ans = sc.nextLine();
								
				if(ans.equals("n")) {
					break;
				}
			}
			
			sc.close();
			for(int i = 0; i < keys.size(); i++) {
				File rmFile = new File(filepath + "keyFile" + keys.get(i) + ".csv");
				rmFile.delete();
			}
			
		}
		catch(Exception ex) {
			
			System.out.println(ex.getMessage());
			
		}
		

	}

}
