/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExtensionName implements Serializable {

	public enum Type {
		anonymity_service, anonymity_reference, anonymity_extendsion_point, //
		anonymity_extendsion, //
		anonymity_spring_context, // Spring
		anonymity_publisher, //
		anonymity_comsumer, //
		anonymity_distributed_resource, //
		nomoal, all;

		public boolean isAnonymity() {
			return this.name().startsWith("anonymity");
		}

		public static List<Type> anonymityTypes() {
			List<Type> types = new ArrayList<Type>();
			types.add(anonymity_service);
			types.add(anonymity_reference);
			types.add(anonymity_extendsion_point);
			types.add(anonymity_extendsion);
			types.add(anonymity_spring_context);
			types.add(anonymity_distributed_resource);
			return types;
		}
	}

	public static void main(String[] args) {
		System.out.println(Type.anonymity_extendsion.isAnonymity());
	}

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -6856142686482139411L;

	public static final Type DEFAULT_TYPE = Type.nomoal;

	private final Type type;
	private final String name;
	private final String rawName;

	/**
	 * Constructs a component name from its string representation.
	 * 
	 * @param rawName
	 *            the string representation of this name
	 */
	public ExtensionName(String name) {
		this(DEFAULT_TYPE, name);
	}

	/**
	 * Constructs a component name from its two parts: type and name.
	 * 
	 * @param type
	 *            the type part of the component name
	 * @param name
	 *            the name part of the component name
	 */
	protected ExtensionName(Type type, String name) {
		this.type = type;
		this.name = name;
		this.rawName = this.type.name() + ":" + this.name;
	}

	/**
	 * Gets the type part of the component name.
	 * 
	 * @return the type part
	 */
	public final Type getType() {
		return type;
	}

	/**
	 * Gets the name part of the component name.
	 * 
	 * @return the name part
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Gets the qualified component name.
	 * 
	 * @return the qualified component name
	 */
	public final String getRawName() {
		return rawName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ExtensionName) {
			return rawName.equals(((ExtensionName) obj).rawName);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return rawName.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return rawName;
	}

}
