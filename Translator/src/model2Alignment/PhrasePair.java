package model2Alignment;

public class PhrasePair implements Comparable<PhrasePair> {
	public String source;
	public String target;
	public double probability=0;
	
	public PhrasePair(String source , String target){
		this.source = source;
		this.target = target;
	}
	
	public PhrasePair(String source , String target , double prob){
		this.source = source;
		this.target = target;
		this.probability = prob;
	}
	
	public boolean equals(Object obj){
		PhrasePair p = (PhrasePair) obj;
		return source.equals(p.source) && target.equals(p.target);
	}
	
	public int hashCode(){
		return ("" + source+target).hashCode();
	}


	public int compareTo(PhrasePair o) {
		if(probability<o.probability)
			return 1;
		else if(probability==o.probability)
			return 0;
		else
			return -1;
	}
	
}
