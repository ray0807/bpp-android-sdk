/*******************************************************************************
 * Copyright 2011-2015 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.xmtj.bgptest.downloader;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;
import com.xmtj.imagedownloader.core.assist.ContentLengthInputStream;
import com.xmtj.imagedownloader.core.download.BaseImageDownloader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of ImageDownloader which uses {@link OkHttpClient} for image stream retrieving.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @author Leo Link (mr[dot]leolink[at]gmail[dot]com)
 */
public class OkHttpImageDownloader extends BaseImageDownloader {

	private OkHttpClient client;

	public OkHttpImageDownloader(Context context, OkHttpClient client) {
		super(context);
		this.client = client;
	}

	@Override
	protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
		Request request = new Request.Builder().url(imageUri).build();
		ResponseBody responseBody = client.newCall(request).execute().body();
		InputStream inputStream = responseBody.byteStream();
		int contentLength = (int) responseBody.contentLength();
		return new ContentLengthInputStream(inputStream, contentLength);
	}
}
