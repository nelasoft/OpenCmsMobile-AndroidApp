package cz.nelasoft.opencms.mobile;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EventAdapter extends ArrayAdapter<Event> {

	private final LayoutInflater mInflater;

	private List<Event> data;

	private Context context;

	public EventAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_2);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
	}

	public void setData(List<Event> data) {
		this.data = data;
		clear();
		if (data != null) {
			for (Event appEntry : data) {
				add(appEntry);
			}
		}
	}

	public List<Event> getData() {
		return data;
	}

	/**
	 * Populate new items in the list.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;

		if (convertView == null) {
			view = mInflater.inflate(R.layout.list_item_event, parent, false);
		} else {
			view = convertView;
		}

		Event item = getItem(position);

		((TextView) view.findViewById(R.id.eventTitle)).setText(item.getTitle());
		StyleableSpannableStringBuilder bs = new StyleableSpannableStringBuilder();

		if (item.getStartDate() != null) {
			java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
			String dateValue = dateFormat.format(item.getStartDate());
			bs.append(dateValue);
		}

		if (!TextUtils.isEmpty(item.getLocation())) {
			bs.appendColorBold(" / ", Color.WHITE);
			bs.appendColorBold(item.getLocation(), Color.BLACK);
		}

		if (item.getStartDate() != null) {
			java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
			String timeValue = timeFormat.format(item.getStartDate());
			bs.appendColorBold(" / ", Color.WHITE);
			bs.appendColorBold(timeValue, Color.BLACK);
		}

		((TextView) view.findViewById(R.id.eventDate)).setText(bs);
		return view;
	}
}
