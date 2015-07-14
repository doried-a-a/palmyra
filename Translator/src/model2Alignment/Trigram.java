package model2Alignment;


public class Trigram extends Bigram{
	String c;
	public Trigram(String a,String b,String c){
		super(a,b);
		this.c=c;
	}
	public String getC() {
		return c;
	}
	public void setC(String c) {
		this.c = c;
	};
	
	public boolean equals(Object obj) {
		Trigram bb = (Trigram) obj;
		return a.equals(bb.a) && b.equals(bb.b) && c.equals(bb.c);
	}
	
	public int hashCode() {
		return (a+b+c).hashCode();
	}
}