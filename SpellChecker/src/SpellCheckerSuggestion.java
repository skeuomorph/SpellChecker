import java.io.*;
import java.util.*;

public class SpellCheckerSuggestion{
    //A function that checks to see if the current word in the input file is also in the dictionary
    public static boolean Check(String word, String path)throws IOException{   
        Scanner dictCheck = new Scanner(new File(path + "Dictionary.txt"));
        while(dictCheck.hasNext()){
            String temp = dictCheck.next();
            if(temp.equals(word)){
                dictCheck.close();
                return true;
            }
        }
        dictCheck.close();
        return false;    
    }

    //A function to add a word to the dictionary
    public static void Update(String path, String word)throws IOException{    
        //New scanner that iterates over Dictionary.txt to copy over existing words to the updated Dictionary.txt
        Scanner dictUpdate = new Scanner(new File(path + "Dictionary.txt"));

        //Create a Tree Set which will have the existing words and the new word added to it, which are ordered alphabetically automatically
        Set<String> newDict = new TreeSet<String>();

        //Add old words to Tree Set
        while(dictUpdate.hasNext()){
            newDict.add(dictUpdate.next());
        }

        //Add new word to Tree Set
        newDict.add(word);
        
        //Create PrintWriter to copy over words from Tree Set to an updated version of Dictionary.txt
        PrintWriter dictWrite = new PrintWriter(path + "Dictionary.txt");
        Object[] arr = newDict.toArray();
        for(int i = 0; i < arr.length; i++){
            dictWrite.println(arr[i]);
        }
        dictUpdate.close();
        dictWrite.close();
    }

    //code for Levenshtein distance taken from https://rosettacode.org/wiki/Levenshtein_distance#Java
    public static int levenshtein(String x, String y) {
        x = x.toLowerCase();
        y = y.toLowerCase();
        
        /*Create a 1D array called "costs". Through memoization, this dynamic programming problem
        will only ever be represented in this array, until the final value(distance) is reached*/
        
        //Length of array is y.length + 1 as the array must start from 0, however comparisons of strings starts from 1
        int [] costs = new int [y.length() + 1];

        //Initialise array as the top row (character indices for string "x")
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;

        int northWest;

        //Outer for-loop represents each time the row moves down
        //Comparisons of strings starts from 1
        for (int i = 1; i <= x.length(); i++) {
            //Assign current value of i to the start of that row, to represent the indices of each character in string "y"
            costs[0] = i;

            //northWest represents the upper left diagonal position in the hypothetical table the loops are iterating over
            northWest = i - 1;

            //Inner for-loop to iterate over each value in the row
            for (int j = 1; j <= y.length(); j++) {

                /*An equation that finds the minimum path from the previous operation. If the two characters currently being considered are
                equal, then the last value of the North West diagonal is assigned to this value. If not equal, then it is the minimum of the values
                "above","left of", or "north west" to the current value + 1. */
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), x.charAt(i - 1) == y.charAt(j - 1) ? northWest : northWest + 1);

                /*This step allows us to operate within a 1D array,
                without losing the value "above" cj, which becomes the new North West for the next value along this row*/
                northWest = costs[j];

                //Result of cj assigned to current position, to be assigned as a northWest when the outer for-loop iterates onto the next row. 
                costs[j] = cj;
            }
        }
        return costs[y.length()];
    }

    /*A function which calls levenshtein() on all words in Dictionary.txt, to find those within Levenshtein distance 2 of the
    typo being parsed in.
    All words within the distance are then assigned to an ArrayList, which gets parsed to Input() and displayed to the user as suggestions.*/ 
    public static ArrayList<String> findSuggestions(String typo, String path)throws IOException{
         ArrayList<String> suggested = new ArrayList<String>();
         Scanner sugScan = new Scanner(new File(path + "Dictionary.txt"));
         while(sugScan.hasNext()){
            String temp = sugScan.next();
            if(levenshtein(temp, typo) <= 2){
                suggested.add(temp);
            }
         }
         sugScan.close();
         return suggested;
    }

    //Begins iterating over words in input.txt
    public static void Input(String path, Scanner user, Scanner scan, File output)throws IOException{
        PrintWriter outWrite = new PrintWriter(output);
        //For each word in the file until there are no more words...
        while(scan.hasNext()){
            //Assign current word to "temp"
            String temp = scan.next();
            //If temp fails the Check
            while(!Check(temp, path)){
                //Find suggestions for the word
                ArrayList<String> suggestions = findSuggestions(temp, path);
                //Ask user if they would like to replace the word
                System.out.println("Typo found:" + "\n" + temp + "\n" + "Here are some suggested replacements:" + "\n" + suggestions.toString() + "\n" + "Would you like replace the word? (Y/N)");
                String ans = user.next().trim();
                if(ans.equals("Y")){
                    //Replacement word the user enters is assigned to temp, and runs through the !Check while loop
                    System.out.println("Please enter the replacement word.");
                    temp = user.next().trim();
                }
                else{
                    //User is asked to add the word to the dictionary, which at this point they have to do to progress with the file
                    System.out.println("Would you like to add the word to the dictionary? (Y/N)");
                    ans = user.next().trim();
                    if(ans.equals("Y")){
                        Update(path, temp);
                    }
                }
            }
            outWrite.println(temp);
        }
        outWrite.close();   
    }
    public static void main(String[] args)throws IOException{ 
        //Declare path
        String path = "C:\\Users\\Kela\\Documents\\UNI\\2nd yr\\PAP\\Coursework2\\SpellChecker\\src\\";

        //Checks to see if output file already exists, creates one if not
        File output = new File(path + "output.txt");
        if(output.createNewFile()){
            System.out.println("Output file has been created.");
        }
        else{
            System.out.println("An output file already exists. This will be overwritten.");
        }

        //Asks user for input file name
        Scanner user = new Scanner(System.in);
        System.out.println("Please enter an input file name.");
        String fileName = user.next().trim();

        //Creates scanner for input file to then be used by Input()
        Scanner scan = new Scanner (new File(path + fileName));

        Input(path, user, scan, output);

        user.close();
        scan.close();
    }
}