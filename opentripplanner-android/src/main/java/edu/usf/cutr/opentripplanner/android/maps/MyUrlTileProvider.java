/*
 * Copyright 2013 University of South Florida
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package edu.usf.cutr.opentripplanner.android.maps;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.android.gms.maps.model.UrlTileProvider;

public class MyUrlTileProvider extends UrlTileProvider {

	private String baseUrl;

	public MyUrlTileProvider(int width, int height, String url) {
	    super(width, height);
	    this.baseUrl = url;
	}

	@Override
	public URL getTileUrl(int x, int y, int zoom) {
	    try {
	        return new URL(baseUrl.replace("{z}", ""+zoom).replace("{x}",""+x).replace("{y}",""+y));
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

}
