package cz.nelasoft.opencms.mobile;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsAdapter extends ArrayAdapter<News> {

	private final LayoutInflater mInflater;

	private List<News> data;

	private Context context;

	// private DrawableBackgroundDownloader dbd = new
	// DrawableBackgroundDownloader();

	private ImageLoader imageLoader;

	public NewsAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_2);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		imageLoader = new ImageLoader(context, R.drawable.z_file_png);
	}

	public void setData(List<News> data) {
		this.data = data;
		clear();
		if (data != null) {
			for (News appEntry : data) {
				add(appEntry);
			}
		}
	}

	public List<News> getData() {
		return data;
	}

	/**
	 * Populate new items in the list.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;

		if (convertView == null) {
			view = mInflater.inflate(R.layout.list_item_news, parent, false);
		} else {
			view = convertView;
		}

		News item = getItem(position);
		/**
		 * if (item.getBitmap() != null) {
		 * ((ImageView)view.findViewById(R.id.newsImage
		 * )).setImageBitmap(item.getBitmap()); } else {
		 * ((ImageView)view.findViewById
		 * (R.id.newsImage)).setImageResource(R.drawable.z_file_png); }
		 */

		if (!TextUtils.isEmpty(item.getImagePath())) {
			imageLoader.displayImage(Config.getConfigContext() + item.getImagePath(), (ImageView) view.findViewById(R.id.newsImage));
		}
		// dbd.loadDrawable(Config.getConfigContext() + item.getImagePath(),
		// (ImageView)view.findViewById(R.id.newsImage), null);

		((TextView) view.findViewById(R.id.newsHeadline)).setText(item.getHeadline());
		// ((TextView)view.findViewById(R.id.newsPerex)).setText(item.getPerex());

		if (item.getDate() != null) {
			java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
			String dateValue = dateFormat.format(item.getDate());

			((TextView) view.findViewById(R.id.newsDate)).setText(dateValue);
		}
		return view;
	}

}
