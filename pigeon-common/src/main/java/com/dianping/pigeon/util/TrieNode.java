package com.dianping.pigeon.util;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

public class TrieNode<K, V> {
	private K key;
	private SortedMap<K, TrieNode<K, V>> children;
	private Comparator<K> keyComparator;
	private V value;
	
	public TrieNode() {
		this(null, null);
	}
	
	public TrieNode(K key) {
		this(key, null);
	}
	
	public TrieNode(K key, Comparator<K> keyComparator) {
		this.key = key;
		this.keyComparator = keyComparator;
	}
	
	public void addChild(TrieNode<K, V> child) {
		if(this.children == null) {
			this.children = new TreeMap<K, TrieNode<K, V>>(keyComparator);
		}
		children.put(child.getKey(), child);
	}
	
	public TrieNode<K, V> getChild(K key) {
		if(this.children == null) {
			return null;
		}
		return children.get(key);
	}
	
	public K getKey() {
		return this.key;
	}
	
	public V getValue() {
		return this.value;
	}
	
	public void setValue(V value) {
		this.value = value;
	}
	
}
