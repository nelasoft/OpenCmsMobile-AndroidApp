package cz.nelasoft.opencms.mobile;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class EventDetailActivity extends SherlockFragmentActivity {

	private Event content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light_DarkActionBar);
		getSupportActionBar().setIcon(R.drawable.logo2);
		getSupportActionBar().setTitle(null);

		content = getIntent().getExtras().getParcelable("content");

		setContentView(R.layout.activity_event_detail);

		((TextView) findViewById(R.id.eventDetailTitle)).setText(content.getTitle());

		StyleableSpannableStringBuilder bs = new StyleableSpannableStringBuilder();

		if (content.getStartDate() != null) {
			java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
			String dateValue = dateFormat.format(content.getStartDate());
			bs.append(dateValue);
			((TextView) findViewById(R.id.eventDetailBoxDate)).setText(dateValue);
		}

		if (!TextUtils.isEmpty(content.getLocation())) {
			bs.appendColorBold(" / ", Color.WHITE);
			bs.appendColorBold(content.getLocation(), Color.BLACK);
			((TextView) findViewById(R.id.eventDetailBoxLocality)).setText(content.getLocation());
		}

		if (content.getStartDate() != null) {
			java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
			String timeValue = timeFormat.format(content.getStartDate());
			bs.appendColorBold(" / ", Color.WHITE);
			bs.appendColorBold(timeValue, Color.BLACK);
			((TextView) findViewById(R.id.eventDetailBoxTime)).setText(timeValue);
		}

		((TextView) findViewById(R.id.eventDetailDate)).setText(bs);
		((TextView) findViewById(R.id.eventDetailDescription)).setText(content.getDescription());

		ActionBarSherlock actionBar = getSherlock();
		actionBar.setTitle("OpenCms");
	}
}
