package model2Alignment;


public class Bigram extends Object{
	
	Bigram(String a,String b){
		this.a = a;
		this.b = b;
	}
	
	protected String a;
	protected String b;
	
	
	public String getA() {
		return a;
	}
	public String getB() {
		return b;
	}
	public void setA(String a) {
		this.a = a;
	}
	public void setB(String b) {
		this.b = b;
	}
	
	public boolean equals(Object obj) {
		Bigram bb = (Bigram) obj;
		return a.equals(bb.a) && b.equals(bb.b);
	}
	
	public int hashCode() {
		return (a+b).hashCode();
	}
	
	
}