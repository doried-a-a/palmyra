package storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import model2Alignment.Model;

public class NumericConverter {
	
	private HashMap<String,Integer> sourceHashTable ;
	private HashMap<String,Integer> targetHashTable ;
	private String [] 				sourceValueTable;
	private String []  				targetValueTable;
	
	
	public NumericConverter(String numericSourcePath , String numericTargetPath) throws FileNotFoundException{
		
		sourceHashTable = new HashMap<String,Integer>();
		
		Scanner sc = new Scanner(new File(numericSourcePath));
		
		int sourceWordsCount = Integer.parseInt(sc.nextLine());
		sourceValueTable = new String[sourceWordsCount];
		
		while(sc.hasNextLine()){
			String line = sc.nextLine();
			Scanner lineSc = new Scanner(line);
			String str = lineSc.next();
			int value = lineSc.nextInt();
			sourceHashTable.put(str, value);
			sourceValueTable[value] = str; 
		}
		
		targetHashTable = new HashMap<String,Integer>();
		
		sc = new Scanner(new File(numericTargetPath));
		
		int targetWordsCount = Integer.parseInt(sc.nextLine());
		targetValueTable = new String[targetWordsCount];
		
		while(sc.hasNextLine()){
			String line = sc.nextLine();
			Scanner lineSc = new Scanner(line);
			String str = lineSc.next();
			int value = lineSc.nextInt();
			targetHashTable.put(str, value);
			targetValueTable[value] = str; 
		}
	}
	
	public int getHashCodeOfSourceWord(String word){
		Integer num = sourceHashTable.get(word);
		if(num==null)
			return -2;
		else return num.intValue();
	}
	
	public String getWordOfSourceHash(int hash){
		if(hash==-1) return "#NULL#";
		if(hash<sourceValueTable.length)
			return sourceValueTable[hash];
		else return null;
	}
	
	public int getHashCodeOfTargetWord(String word){
		Integer num = targetHashTable.get(word);
		if(num==null)
			return -2;
		else return num.intValue();
	}
	
	public String getWordOfTargetHash(int hash){
		if(hash==-1) return "#NULL#";
		if(hash<targetValueTable.length)
			return targetValueTable[hash];
		else return null;
	}
	
	public int getSourceWordsCount(){
		return sourceValueTable.length;
	}
	public int getTargetWordsCount(){
		return targetValueTable.length;
	}
	
	
	public String getWordsOfSouceHashes(String hashString){
		String str[] = hashString.split(" ");
		String res = "";
		for(int i=0;i<str.length;i++)
			res += getWordOfSourceHash(Integer.parseInt(str[i])) + " ";
		return res.trim();
	}
	
	
	public String getWordsOfTargetHashes(String hashString){
		String str[] = hashString.split(" ");
		String res = "";
		for(int i=0;i<str.length;i++)
			res += getWordOfTargetHash(Integer.parseInt(str[i])) + " ";
		return res.trim();
	}
	
	//======================================================
	public static void buildNumbericModel(String corpusSourcePath , String corpusTargetPath  , String outputPath) throws FileNotFoundException, UnsupportedEncodingException{
		
		// processing the source corpus file
		//===========================================================
		HashMap<String,Integer> hash = new HashMap<String, Integer>();
		
		Scanner sc = new Scanner(new File(corpusSourcePath));
		PrintWriter writer = new PrintWriter(outputPath + "source.num" , "UTF-8");
		
		int counter=0;
		
		while(sc.hasNextLine()){
			
			String line = sc.nextLine();
			
			String [] tokens = line.split(" ");
			
			String toWrite = "";
			
			for(int i=0;i<tokens.length;i++){
				Integer value =  hash.get(tokens[i]);
				if(value == null ){
					hash.put(tokens[i],counter );
					toWrite += counter + " ";
					
					counter++;
				}
				else
					toWrite += value.intValue() + " ";
			}
	
			writer.println(toWrite.trim());
			
		}
		
		writer.close();
		
		writer = new PrintWriter(outputPath + "source.hash" , "UTF-8");
		
		writer.println(counter + "");
		
		Iterator< Map.Entry<String, Integer> > it = hash.entrySet().iterator();
		
		while(it.hasNext()){
			Map.Entry<String, Integer> entry = ( Map.Entry<String, Integer>) it.next();
			writer.println(entry.getKey() + " " + entry.getValue());
		}
		
		hash = null;
		it = null; // disposing any reference to the hash map to release the reserved memory
		
		writer.close();
		
		
		// processing the target corpus file
		//===========================================================
		hash = new HashMap<String, Integer>();
		
		sc = new Scanner(new File(corpusTargetPath));
		writer = new PrintWriter(outputPath + "target.num" , "UTF-8");
		
		counter=0;
		
		while(sc.hasNextLine()){
			
			String [] tokens = sc.nextLine().trim().split(" ");
			
			String toWrite = "";
			
			for(int i=0;i<tokens.length;i++){
				Integer value =  hash.get(tokens[i]);
				if(value == null ){
					hash.put(tokens[i],counter );
					toWrite += counter + " ";
					counter++;
				}
				else
					toWrite += value.intValue() + " ";
			}
			
			writer.println(toWrite.trim());
			
		}
		
		writer.close();
		
		writer = new PrintWriter(outputPath + "target.hash" , "UTF-8");
		
		writer.println(counter + "");
		
		it = hash.entrySet().iterator();
		
		while(it.hasNext()){
			Map.Entry<String, Integer> entry = ( Map.Entry<String, Integer>) it.next();
			writer.println(entry.getKey() + " " + entry.getValue());
		}
		
		writer.close();

	}
	
	public static void buildLexicalModel(String numerizedModelPath , String outputPath) throws FileNotFoundException, UnsupportedEncodingException{

		// processing source numerized corpus file
		//===============================================
		HashMap<Integer,String> hash = new HashMap<Integer,String>();
		
		Scanner sc = new Scanner(new File(numerizedModelPath + "source.hash"));
		sc.nextLine();
		
		while(sc.hasNextLine()){
			String line = sc.nextLine();
			Scanner lineSc = new Scanner(line);
			String str = lineSc.next();
			int value = lineSc.nextInt();
			hash.put(value, str);
		}
		
		sc = new Scanner(new File (numerizedModelPath + "source.num" ) );
		
		PrintWriter writer = new PrintWriter(outputPath + "source.lex" , "UTF-8");
		
		while(sc.hasNextLine()){
			
			String [] tokens = sc.nextLine().trim().split(" ");
			
			String toWrite = "";
			
			for(int i=0;i<tokens.length;i++){
				int num = Integer.parseInt(tokens[i]);
				String str = hash.get(new Integer(num));
				
				if(str == null ){
					throw new RuntimeException("hash value not found : " + num);
				}
				else
					toWrite += str + " ";
			}
			
			writer.println(toWrite.trim());
			
		}
		
		writer.close();
		
		// processing target numerized corpus file
		//===============================================
		hash = new HashMap<Integer,String>();
		
		sc = new Scanner(new File(numerizedModelPath + "target.hash"));
		sc.nextLine();
		
		while(sc.hasNextLine()){
			Scanner lineSc = new Scanner(sc.nextLine());
			String str = lineSc.next();
			int value = lineSc.nextInt();
			hash.put(value, str);
		}
		
		sc = new Scanner( new File(numerizedModelPath + "target.num") );
		writer = new PrintWriter(outputPath + "target.lex" , "UTF-8");

		while(sc.hasNextLine()){
			
			String [] tokens = sc.nextLine().trim().split(" ");
			
			String toWrite = "";
			
			for(int i=0;i<tokens.length;i++){
				int num = Integer.parseInt(tokens[i]);
				String str = hash.get(new Integer(num));
				
				if(str == null ){
					throw new RuntimeException("hash value not found : " + num);
				}
				else
					toWrite += str + " ";
			}
			
			writer.println(toWrite.trim());
			
		}
		writer.close();		
	}
	
	public static int getWordsCountFromHashTableFile(String hashTablePath) throws FileNotFoundException{
		
		Scanner sc = new Scanner(new File(hashTablePath) , "UTF-8");
		
		int wordsCount = Integer.parseInt(sc.nextLine());
		
		sc.close();
		return wordsCount;
	}
	
	public static int getMaxSentenceLengthOfNumericOrLexicalFile(String path) throws FileNotFoundException{
		
		Scanner sc = new Scanner(new File(path) , "UTF-8");
		
		int max=0;
		while(sc.hasNextLine()){
			String str = sc.nextLine();
			int len = str.split(" ").length ;
			if(len>max)
				max = len; 
		}
		sc.close();
		return max;
	}
	
	
	
	public void cleanConverter() {
		this.sourceHashTable.clear();
		this.sourceHashTable = null;
		this.sourceValueTable = null;
		this.targetHashTable.clear();
		this.targetHashTable=null;
		this.targetValueTable = null;
		Runtime.getRuntime().gc();
	}
	
	
}
