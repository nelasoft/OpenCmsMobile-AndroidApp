package cz.nelasoft.opencms.mobile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class NewsFragment extends SherlockFragment implements IRefreshFragment {

	private NewsAdapter mAdapter;
	private ListView lv;
	private String menuId;
	private String subtitle;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (container == null)
			return null;

		Bundle arg = getArguments();
		menuId = arg.getString("menuId");
		subtitle = arg.getString("subtitle");

		View view = inflater.inflate(R.layout.news_fragment, container, false);

		TextView textView = (TextView) view.findViewById(R.id.newsFragmentSubtitle);

		if (!TextUtils.isEmpty(subtitle)) {
			textView.setText(subtitle);
		} else {
			textView.setVisibility(View.INVISIBLE);
			textView.setHeight(0);
		}

		mAdapter = new NewsAdapter(getActivity());

		lv = (ListView) view.findViewById(R.id.newslist);
		lv.setAdapter(mAdapter);

		lv.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				// Create an Intent that will start the main activity.
				Intent detailIntent = new Intent(getActivity(), NewsDetailActivity.class);
				News content = mAdapter.getData().get(position);

				detailIntent.putExtra("content", content);

				startActivity(detailIntent);

			}
		});

		StructuredContentAsyncTask task = new StructuredContentAsyncTask(getActivity(), menuId);
		task.execute((String[]) null);
		return view;
	}

	private class StructuredContentAsyncTask extends AsyncTask<String, Context, List<News>> {

		private ProgressDialog progressDialog;
		private Context context;
		private String menuId;

		public StructuredContentAsyncTask(Context context, String menuId) {
			this.context = context;
			this.menuId = menuId;
		}

		@Override
		protected void onPreExecute() {
			if (DownloadManager.isContentCached(menuId)) {
				return;
			}

			progressDialog = ProgressDialog.show(context, NewsFragment.this.getString(R.string.loadingData), NewsFragment.this.getString(R.string.wait), true, true);
			progressDialog.setOnCancelListener(new OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					cancel(false);
				}
			});

			progressDialog.show();
		}

		@Override
		protected List<News> doInBackground(String... params) {
			try {

				String jsonData = null;
				if (DownloadManager.isContentCached(menuId)) {
					jsonData = DownloadManager.getContent(menuId);
				} else {
					jsonData = DownloadManager.connect(menuId);
					DownloadManager.setContent(menuId, jsonData);
				}

				JSONObject json = new JSONObject(jsonData);

				JSONArray data = json.getJSONArray("data");

				ArrayList<News> list = new ArrayList<News>();

				for (int i = 0; i < data.length(); i++) {
					JSONObject sc = data.getJSONObject(i);
					News content = new News();
					content.setMenuId(menuId);
					if (sc.has("Title")) {
						content.setHeadline(sc.getString("Title"));
					}
					// content.setPerex(sc.getString("Text"));
					String image = null;
					if (sc.has("Image")) {
						image = sc.getString("Image");
					}
					if (sc.has("Content")) {
						content.setContent(sc.getString("Content"));
					}
					if (sc.has("Date")) {
						long date = sc.getLong("Date");
						content.setDate(new Date(date));
					}

					if (image != null && image.length() != 0) {
						content.setImagePath(image);
					}
					list.add(content);
				}

				return list;
			} catch (Exception ex) {
				NewsFragment.this.getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(NewsFragment.this.getActivity().getApplicationContext(), R.string.err_download_data, Toast.LENGTH_SHORT).show();
					}
				});

				ex.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onCancelled() {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			mAdapter.setData(null);
		}

		@Override
		protected void onPostExecute(List<News> result) {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			mAdapter.setData(result);
			mAdapter.notifyDataSetInvalidated();
		}
	}

	@Override
	public void refreshFragment() {
		DownloadManager.cleanCache(menuId);
		StructuredContentAsyncTask task = new StructuredContentAsyncTask(getActivity(), menuId);
		task.execute((String[]) null);
	}
}
