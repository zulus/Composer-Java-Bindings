package org.getcomposer.repositories;

import org.getcomposer.Resource;

public abstract class Repository extends Resource {

	public Repository(String type) {
		set("type", type);
	}

	/**
	 * Returns the <code>type</code> property.
	 * 
	 * @return the <code>type</code> property
	 */
	public String getType() {
		return getAsString("type");
	}

	/**
	 * Returns the <code>url</code> property.
	 * 
	 * @return the <code>url</code> property
	 */
	public String getUrl() {
		return getAsString("url");
	}

	public void setUrl(String url) {
		set("url", url);
	}
	
//	public static Object getSerializer() {
//		return new RepositorySerializer();
//	}

}
