package cz.nelasoft.opencms.mobile;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class DownloadManager {

	private static Map<String, String> cache = new HashMap<String, String>();

	public DownloadManager() {
		super();
	}

	public static JSONObject getMenuContent() {
		return null;
	}

	/**
	 * 
	 * @param url
	 * @return
	 */
	public static String connect(String menuId) {

		try {

			if (isContentCached(menuId)) {
				return getContent(menuId);
			}

			String url = Config.getConfigUrl() + "?menuId=" + URLEncoder.encode(menuId, "utf-8") + "&lang=" + Locale.getDefault().getLanguage();

			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response;

			response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream is = new BufferedInputStream(entity.getContent());
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				StringBuilder sb = new StringBuilder();
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return sb.toString();
			}
		} catch (Exception e) {
		}
		return null;
	}

	public Bitmap bitmapFromUrl(String url) throws IOException {
		Bitmap x;

		BitmapFactory.Options opt = new BitmapFactory.Options();
		// opt.inSampleSize = 2;
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.connect();
		InputStream input = connection.getInputStream();

		x = BitmapFactory.decodeStream(input, null, opt);
		return x;
	}

	public Bitmap connectBitmap(String url) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				Bitmap result = BitmapFactory.decodeStream(instream);
				instream.close();
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static abstract class DownloadJsonTask extends AsyncTask<String, Integer, String> {

		public DownloadJsonTask() {
			super();
		}

		protected String doInBackground(String... urls) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(urls[0]);
			HttpResponse response;
			String result = null;
			try {
				response = httpclient.execute(httpget);

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					long length = entity.getContentLength();
					InputStream is = new BufferedInputStream(entity.getContent());
					try {

						ByteArrayOutputStream baos = new ByteArrayOutputStream();

						byte data[] = new byte[1024];
						long total = 0;
						int count;
						while ((count = is.read(data)) != -1) {
							total += count;
							// publishing the progress....
							publishProgress((int) (total * 100 / length));
							baos.write(data, 0, count);
						}

						result = new String(baos.toByteArray(), "UTF-8");
						return result;
					} finally {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return result;
		}
	}

	public static String getContent(String key) {
		return cache.get(key);
	}

	public static void setContent(String key, String value) {
		cache.put(key, value);
	}

	public static void cleanCache() {
		cache.clear();
	}

	public static void cleanCache(String key) {
		cache.remove(key);
	}

	public static boolean isContentCached(String key) {
		return cache.containsKey(key);
	}

}
