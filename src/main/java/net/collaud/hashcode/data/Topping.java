package net.collaud.hashcode.data;

import lombok.Data;

@Data
public class Topping {
	private final boolean tomato;
	private boolean used;

	public Topping(boolean tomato) {
		this.tomato = tomato;
		this.used = false;
	}

	@Override
	public String toString() {
		if(used){
			return ".";
//			if(tomato){
//				return "t";
//			}
//			return "m";
		}
		if(tomato){
			return "T";
		}
		return "M";
	}

	public Topping clone(){
		Topping topping = new Topping(tomato);
		topping.setUsed(used);
		return topping;
	}
}
