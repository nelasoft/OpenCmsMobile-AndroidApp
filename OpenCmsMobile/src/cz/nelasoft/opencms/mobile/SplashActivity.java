package cz.nelasoft.opencms.mobile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SplashActivity extends Activity {

	public static final String OPEN_CMS_SHARED_PREFERENCE = "OPEN_CMS_SHARED_PREFERENCE";
	public static final String OPEN_CMS_CONFIG = "OPEN_CMS_CONFIG";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
	}

	@Override
	protected void onResume() {
		super.onResume();

		ProgressBar pb = (ProgressBar) findViewById(R.id.splashProgressBar);

		try {
			Config.initConfig(getResources());
			new DownloadConfig(pb).execute(Config.getConfigUrl() + "?lang=" + Locale.getDefault().getLanguage());
		} catch (Throwable e) {
			Log.e("OpenMobile", e.getMessage(), e);
		}
	}

	public void storeConfigToStorage(String data) {
		SharedPreferences settings = getSharedPreferences(OPEN_CMS_SHARED_PREFERENCE, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(OPEN_CMS_CONFIG, data);
		editor.commit();
	}

	public String getConfigFromStorage() {
		SharedPreferences settings = getSharedPreferences(OPEN_CMS_SHARED_PREFERENCE, 0);
		return settings.getString(OPEN_CMS_CONFIG, Config.getDefaultConfigJson());
	}

	private class DownloadConfig extends AsyncTask<String, Integer, String> {

		private ProgressBar progressBar;
		private long startTime;

		public DownloadConfig(ProgressBar progressBar) {
			super();
			this.progressBar = progressBar;
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
							int progressLoad = (int) (length * 100 / total);
							int progressTime = (int) (System.currentTimeMillis() - startTime * 100 / (Config.getSplashDisplayLength()));
							int progress;
							if (progressTime <= 50) {
								progress = progressTime;
							} else {
								progress = Math.min(progressLoad, progressTime);
							}
							publishProgress(progress);
							baos.write(data, 0, count);
						}

						result = new String(baos.toByteArray(), "UTF-8");

						Config.setDefaultConfigJson(result);
						storeConfigToStorage(result);

						long currentTime;
						while ((currentTime = System.currentTimeMillis() - startTime) < Config.getSplashDisplayLength()) {
							Thread.sleep(100);
							int progressTime = (int) (currentTime * 100 / (Config.getSplashDisplayLength()));
							publishProgress(progressTime);
						}
						publishProgress(100);

					} finally {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (Throwable e) {

				SplashActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {

						Toast.makeText(SplashActivity.this.getBaseContext(), R.string.err_download_config, Toast.LENGTH_SHORT).show();
					}
				});

				Log.e("OpenMobile", "Cannot download configuration", e);
				try {
					Config.setDefaultConfigJson(getConfigFromStorage());
				} catch (JSONException e1) {
				}
			}
			return result;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
			startTime = System.currentTimeMillis();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			SplashActivity.this.finish();
			// Create an Intent that will start the main activity.
			Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
			DownloadManager.cleanCache();
			SplashActivity.this.startActivity(mainIntent);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressBar.setProgress(values[0]);
		}
	}
}
