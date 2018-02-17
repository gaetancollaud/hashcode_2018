package net.collaud.hashcode.common.data;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Gaetan Collaud
 * @param <K>
 */
public class Stock<K> {
	
	private final Map<K,Integer> items;

	public Stock() {
		items = new HashMap<>();
	}
	
	public Map<K, Integer> getAll(){
		return items;
	}
	
	public boolean inStock(K item){
		return items.getOrDefault(item, 0)>0;
	}
	
	public void add(K item, int amount){
		items.compute(item, (k, v) -> v==null ? amount : v+amount);
	}
	
	public int count(K item){
		return items.getOrDefault(item, 0);
	}
	
	public int remove(K item, int amount){
		int inStock = items.getOrDefault(item, 0);
		if(inStock<amount){
			throw new RuntimeException("Not enough stock for item "+item);
		}
		inStock -= amount;
		if(inStock==0){
			items.remove(item);
		}else{
			items.put(item, inStock);
		}
		return inStock;
	}
	
	public boolean isEmpty(){
		return items.isEmpty();
	}
	
	
}
