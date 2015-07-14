package model2Alignment;


public class Translation implements Comparable<Translation> {

	public int source;
	public int translation;
	public double probability;
	
	public Translation(int s,int t,double p){
		source = s;
		translation = t;
		probability = p;
	}
	
	public String toString(){
		return this.source + " \t" + this.translation + "( " + this.probability + " )" ;
	}
	
	public int compareTo(Translation o) {
		if(probability<o.probability)
			return 1;
		else if(probability==o.probability)
			return 0;
		else
			return -1;
	}

}
