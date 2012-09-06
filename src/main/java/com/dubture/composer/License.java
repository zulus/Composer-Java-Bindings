/*******************************************************************************
 * Copyright (c) 2012 The PDT Extension Group (https://github.com/pdt-eg)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.dubture.composer;

public class License {
	
	private String[] names;
	
	public License() {
		names = new String[]{};
	}
	
	public License(String[] names) {
		this.names = names;
	}

	public String[] getNames() {
		return names;
	}

	public void setNames(String[] names) {
		this.names = names;
	}
}
