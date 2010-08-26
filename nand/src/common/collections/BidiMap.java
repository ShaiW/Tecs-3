/********************************************************************************
 * The contents of this file are subject to the GNU General Public License      *
 * (GPL) Version 2 or later (the "License"); you may not use this file except   *
 * in compliance with the License. You may obtain a copy of the License at      *
 * http://www.gnu.org/copyleft/gpl.html                                         *
 *                                                                              *
 * Software distributed under the License is distributed on an "AS IS" basis,   *
 * without warranty of any kind, either expressed or implied. See the License   *
 * for the specific language governing rights and limitations under the         *
 * License.                                                                     *
 *                                                                              *
 * This file was originally developed as part of the software suite that        *
 * supports the book "The Elements of Computing Systems" by Nisan and Schocken, *
 * MIT Press 2005. If you modify the contents of this file, please document and *
 * mark your changes clearly, for the benefit of others.                        *
 ********************************************************************************/
package common.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A hash based bidirectional map
 * 
 * @author Shai (Deshe) Wyborski
 *
 * @param <K1> keys
 * @param <K2> values
 */
public class BidiMap<K1,K2> implements Map<K1,K2>{
	
	//The maps containing the values
	private HashMap<K1, K2> regular;
	private HashMap<K2, K1> inverse;
	
	/**
	 * Instantiates a new bidirectional map
	 */
	public BidiMap() {
		regular = new HashMap<K1, K2>();
		inverse = new HashMap<K2, K1>();
	}

	/**
	 * Instantiates a new bidirectional map from an existing map.
	 * In case of multiple-keyed values only one of the mappings will prevail.
	 * 
	 * @param m the original map
	 */
	public BidiMap(Map<K1, K2> m) {
		regular = new HashMap<K1, K2>();
		inverse = new HashMap<K2, K1>();
		putAll(m);
	}

	/**
	 * Put a new key-value pair into the map.
	 * Notice that this method <strong>DOES NOT</strong> abide to the hashmap API
	 * since it <i>always</i> returns a null value.
	 * 
	 * @return null
	 */
	public K2 put(K1 key, K2 value){
		if (regular.containsKey(key)) inverse.remove(value);
		if (inverse.containsKey(value)) regular.remove(key);
		regular.put(key, value);
		inverse.put(value, key);
		return null;
	}
	
	@Override
	public K2 get(Object key){
		return regular.get(key);
	}
	
	/**
	 * Returns a key from a value.
	 * 
	 * @param value the value
	 * @return the corresponding key
	 */
	public K1 getKey(K2 value){
		return inverse.get(value);
	}

	@Override
	public void clear() {
		regular.clear();
		inverse.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return regular.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return inverse.containsKey(value);
	}

	@Override
	public Set<java.util.Map.Entry<K1, K2>> entrySet() {
		return regular.entrySet();
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Set<K1> keySet() {
		return regular.keySet();
	}

	@Override
	public void putAll(Map<? extends K1, ? extends K2> m) {
		for(K1 k: m.keySet()){
			if (regular.containsKey(k)) continue;
			regular.put(k, m.get(k));
			inverse.put(m.get(k), k);
		}
	}

	@Override
	public K2 remove(Object key) {
		inverse.remove(regular.get(key));
		return regular.remove(key);
	}

	@Override
	public int size() {
		return regular.size();
	}

	@Override
	public Collection<K2> values() {
		return inverse.keySet();
	}

}
