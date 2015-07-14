package model2Alignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import storage.NumericConverter;

public class AlignmentCreater {
	
	public Model 	model	;
	public Corpora 	corpora	;
	
	public AlignmentCreater(Corpora corpora , Model model){
		if(model==null || corpora==null) throw new NullPointerException("model and corpora can not be null.");
		this.model = model;
		this.corpora = corpora;
	}
	
	public void createAlignmentFile(String filename , NumericConverter con) throws FileNotFoundException, UnsupportedEncodingException{
		
		PrintWriter writer = new PrintWriter(filename + ".al" , "UTF-8");
		
		PrintWriter writer1 = new PrintWriter(filename + ".al.view" , "UTF-8");
		
	
		for(int sentenceIdx=0;sentenceIdx<corpora.count;sentenceIdx++){
			
			String[] ssStr = divideString("-1 " + corpora.getSource(sentenceIdx));
			String[] ttStr = divideString(corpora.getTranslate(sentenceIdx));
			
			int [] ss = new int[ssStr.length];
			int [] tt = new int[ttStr.length];
			
			for(int i=0;i<ssStr.length;i++)
				ss[i] = Integer.parseInt(ssStr[i]);

			for(int i=0;i<ttStr.length;i++)
				tt[i] = Integer.parseInt(ttStr[i]);
			
			int  	[]	alignments 		= new int[tt.length];
			String 	[]	alignedWords 	= new String[tt.length];
			
			double totalProb = 1;
			
			for(int i=0;i<tt.length;i++){
				
				int target = tt[i];
	
				double wordAlignProb=0;
				
				// know we should find the best match of the translated word (target) in the original sentence
				for(int j=0;j<ss.length;j++){
					int sourceWord = ss[j];
					
					double alignProb=1 ;
					
					//if(tt.length>maxSentenceLength || ss.length>maxSentenceLength)
						//alignProb = 1.0/(ss.length+1-1);
					//else 
						alignProb = model.getAlignmentProb(j, i, tt.length, ss.length);
					
					//double transProb = model.getTranslationProb(sourceWord, target);
					double transProb = model.getTranslationProb(sourceWord, target);
					
					if(alignProb*transProb>wordAlignProb){
						wordAlignProb = alignProb*transProb;
						alignments[i] = j;
					}
					
				}
				
				totalProb *= wordAlignProb;
			}
			
			totalProb = -Math.log(totalProb)/tt.length;
			
			String toWrite = "";
			String toWrite1 = "";
			for(int i=0;i<alignments.length;i++)
			{
				toWrite += alignments[i];
				
				if(con==null)
					toWrite1 += "( '" + tt[i] + "' : '" + ss[alignments[i]] + "' )";
				else
					toWrite1 += "( '" + con.getWordOfTargetHash(tt[i]) + "' : '" + con.getWordOfSourceHash(ss[alignments[i]]) + "' )";
				
				if(i<alignments.length-1){
					toWrite += " ";
					toWrite1 += " ";
				}
			}
			
			toWrite		+= " -P " + totalProb;
			toWrite1	+= " -P " + totalProb;
			
			writer.println(toWrite);
			writer1.println(toWrite1);
			
			if((sentenceIdx+1)%5000==0){
				System.out.println("Alignment creator: " + (sentenceIdx+1) + " sentence(s) processed.");
				System.gc();
			}
				
		}
		
		if((corpora.count)%5000 != 0)
			System.out.println("Alignment creator: " + corpora.count + " sentence(s) processed.");
		
		writer.close();
		writer1.close();
		
	}
	
	
	
	public static String[] divideString(String str) {
		str = str.trim();
		return str.split(" ");
	}
	
	public static void mergeAlignmentFiles(String f1,String f2,String outpath) throws FileNotFoundException{
		Scanner sc1 = new Scanner(new File(f1));
		Scanner sc2 = new Scanner(new File(f2));
		int Ls , Lt;
		int c=0;
		
		PrintWriter wr = new PrintWriter(outpath);
		PrintWriter wr1 = new PrintWriter(outpath + ".probs");
		
		boolean ok = sc1.hasNext() && sc2.hasNext();
		while (true){
			String l1 = sc1.nextLine() , l2 = sc2.nextLine();
			String[] a1 = divideString(l1) , a2 = divideString(l2);
			
			
			double prob1 = Double.parseDouble(a1[a1.length-1]);
			double prob2 = Double.parseDouble(a2[a2.length-1]);
			
			
			// n1 is the length of target sentence , n2 is the length of the source sentence without #NULL# (or the inverse)
			int n1 = a1.length-2  , n2 = a2.length-2 ;  // there is two extra words (["-P"] then [the probability] )
														 
			boolean [][] map1 = new boolean[n1+1][];
			boolean [][] map2 = new boolean[n2+1][];
			
			int mappedTo, i , j , ii , jj , k ;
			
			map1[0] = new boolean[n2+1];
			
			for(i=0;i<n1; i++){
				// +1 because when merging , we will insert #NULL# in the begin of the target , so  it will be the 0 word
				map1[i+1] = new boolean[n2+1];
				
				mappedTo = Integer.parseInt(a1[i]);
				
				map1[i+1][ mappedTo] = true; 
				
			}
			
			map2[0] = new boolean[n1+1];
			for(i=0;i<n2; i++){
				map2[i+1] = new boolean[n1+1];
				
				mappedTo = Integer.parseInt(a2[i]);
				
				map2[i+1][ mappedTo] = true;
			}
			
			boolean [][] map = new boolean[n1+1][n2+1];
			boolean [] isAligned1 = new boolean[n1+1];
			boolean [] isAligned2 = new boolean[n2+1];
			
			for(i=0;i<=n1;i++){
				for(j=0;j<=n2;j++){
					map[i][j] = map1[i][j]&&map2[j][i];
					if(map[i][j]){
						isAligned1[i] = true;
						isAligned2[j] = true;
					}
				}
			}
			
			boolean changed=true;
			
			while(changed){
				changed=false;
			
				for(i=0;i<=n1;i++){
					for(j=0;j<=n2;j++){
						if(!map[i][j]) continue;
						for(ii=-1;ii<=1;ii++)
							for(jj=-1;jj<=1;jj++){
								if(i+ii<0 || i+ii>n1 || j+jj<0 || j+jj>n2) continue;
								
								if(isAligned1[i+ii] && isAligned2[j+jj]) continue;
								
								if( (!map[i+ii][j+jj]) &&  (map1[i+ii][j+jj] || map2[j+jj][i+ii]))
								{
									map[i+ii][j+jj]=true;
									isAligned1[i+ii]=true;
									isAligned2[j+jj]=true;
									changed=true;
								}		
							}
					}
				}
			}
			
		    boolean isFirst=true;
			
			isFirst=true;
			for(i=0;i<=n1;i++){
				for(j=0;j<=n2;j++){
					if(map[i][j] && (i*j)>0 ){
						wr.write( (isFirst?"":" ") + (j-1) + "-" + (i-1));
						isFirst = false;
					}
				}
			}
			wr.write('\n');
			wr1.println( (0.5*prob1 + 0.5*prob2));
			
			
				
			if(!sc1.hasNextLine() && !sc2.hasNextLine())
				break;
			else if(!sc1.hasNextLine() || !sc2.hasNextLine()){
				wr.close(); wr1.close();
				throw new  RuntimeException("Provided alignment files don't have the same number of lines.");
			}
			if((++c+1)%5000==0)
				System.out.println(c+1 + " Sentence alinement merged.");
		}
		wr.close();
		wr1.close();
	}

	public static void printmap(boolean [][] map){
		for(int i=-1;i<map.length;i++){
			for(int j=-1;j<map[0].length;j++){
				if(i==-1 && j==-1 ) System.out.print("  ");
				else if(i==-1) System.out.print(j + " ");
				else if(j==-1) System.out.print(i + " ");
				else
					System.out.print((map[i][j]==true?1:0)+ " ");
			}
			System.out.println();
		}
		System.out.println("------------------------");
	}
	
}
