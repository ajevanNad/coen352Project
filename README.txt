COEN 352 Project 1

Problem:

You are provided with a large number (T) of vectors of equal size (S). Each vector represents a grey-scale image of L x W pixels. Hence, every vector has LW values, with each value in the range [0,255]. You are free to store the set of vectors in any way that would optimize the performance of your program. Assume distance (D) between any two vectors is the normalized city-block distance between them. For example, if V1=(1,2,3) and V2=(2,3,4) then D(V1,V2)=(|1-2|+|2-3|+|3-4|)/3=1. Given an unseen cue vector (Vc), your problem is to design, implement and test algorithm(s) and data structure(s) that would return all stored vectors within a user-provided distance of any user-provided Vc. The time and space complexity of your solution must (a) scale-up as well as possible (approximately, linearly or better) with both the size of vectors (LW) and the number of stored vectors (T); (b) be practically useable. 

How to use this program: 

1) Once you run the program, the first message that you will see in the console is "Enter the file path for where the file with the image vectors are located:", along with details on how to enter the path correctly.

2) Next you will be prompted to enter the file name of the file that contains all the image vectors.

3) Now the program will take care of all the neccessary preprocessing.

4) Once preprocessing is complete, you will be prompted to enter the complete file path and file name which contains the image vector for which you want to find similar images for. SO MAKE SURE that you have a file with ONLY ONE image vector in it.

5) Next, you will be prompted to enter the threshold for which you want to find similar images.
*NOTE: This program uses city block distance to check if two images are within the threshold.

6) Once it has found all image vectors within the given threshold, these image vectors will be saved in a file called ImagesWithinThreshold.csv in the file path provided earlier. 

7) You will be asked if you would like to look for similar images for another image based on the same database of images. Enter 'y' if yes, otherwise, if you want to end the program, then enter 'n'.  