package cz.nelasoft.opencms.mobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class NewsDetailActivity extends SherlockFragmentActivity {

	private News content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light_DarkActionBar);
		getSupportActionBar().setIcon(R.drawable.logo2);
		getSupportActionBar().setTitle(null);

		content = getIntent().getExtras().getParcelable("content");

		setContentView(R.layout.activity_news_detail);

		((TextView) findViewById(R.id.newsDetailTitle)).setText(content.getHeadline());
		// ((TextView)findViewById(R.id.detailPerex)).setText(content.getPerex());
		if (content.getDate() != null) {
			java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
			String dateValue = dateFormat.format(content.getDate());

			((TextView) findViewById(R.id.newsDetailDate)).setText(dateValue);
		}

		if (!TextUtils.isEmpty(content.getImagePath())) {
			ImageLoader imageLoader = new ImageLoader(getApplicationContext(), R.drawable.z_file_png);
			imageLoader.displayImage(Config.getConfigContext() + content.getImagePath() + "?", (ImageView) findViewById(R.id.newsDetailImageIcon), getApplicationContext());
		}
		// ((ImageView)findViewById(R.id.detailImageIcon)).setImageBitmap(content.getBitmap());
		((TextView) findViewById(R.id.newsDetailText)).setText(content.getContent());

		ActionBarSherlock actionBar = getSherlock();
		actionBar.setTitle("OpenCms");
	}
}
