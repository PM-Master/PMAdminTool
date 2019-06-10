package gov.nist.csd.pm.admintool.spt.common;

import java.util.ArrayList;

public class PMElement {
	Integer id;
	String name;
	String type;
	ArrayList<String> in;
//	int[] in;
	
	
	public PMElement(Integer id, String name, String type, ArrayList<String> in) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.in = in;
	}
	
	public PMElement() {
		
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<String> getIn() {
		return in;
	}

	public void setIn(ArrayList<String> in) {
		this.in = in;
	}
	
}
