package cz.nelasoft.opencms.mobile;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.SupportMapFragment;

public class MainActivity extends SherlockFragmentActivity {

	TabHost mTabHost;
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	Menu menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light_DarkActionBar);

			ActionBarSherlock actionBar = getSherlock();
			actionBar.setTitle("OpenCms");
			getSupportActionBar().setIcon(R.drawable.logo2);
			getSupportActionBar().setTitle(null);

			JSONObject json = Config.getMainConfig();

			JSONObject mobApp = json.getJSONObject("MobileApplication");

			JSONArray tabulars = mobApp.getJSONArray("Tabs");

			setContentView(R.layout.activity_main);

			mTabHost = (TabHost) findViewById(android.R.id.tabhost);
			mTabHost.setup();

			mViewPager = (ViewPager) findViewById(R.id.pager);

			mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
			mTabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
			mTabHost.getTabWidget().setStripEnabled(true);

			for (int i = 0; i < tabulars.length(); i++) {

				JSONObject tab = tabulars.getJSONObject(i).getJSONObject("Tab");
				String title = tab.getString("Title");

				Bundle bundle = new Bundle();
				bundle.putString("menuId", title);

				Class<? extends Fragment> fragmentClass = null;

				if (tab.has("NewsCollector")) {
					bundle.putString("subtitle", getString(tab, "Subtitle"));
					fragmentClass = NewsFragment.class;
				}
				if (tab.has("EventCollector")) {
					bundle.putString("subtitle", getString(tab, "Subtitle"));
					fragmentClass = EventFragment.class;
				} else if (tab.has("Url")) {
					fragmentClass = WebContentFragment.class;
					bundle.putString("url", getString(tab, "Url"));
				} else if (tab.has("ContactCompanyName")) {
					fragmentClass = ContactsFragment.class;
					bundle.putString(ContactsFragment.CONTACT_NAME, getString(tab, "ContactCompanyName"));
					bundle.putString(ContactsFragment.CONTACT_STREET, getString(tab, "ContactCompanyStreet"));
					bundle.putString(ContactsFragment.CONTACT_CITY, getString(tab, "ContactCompanyCity"));
					bundle.putString(ContactsFragment.CONTACT_ZIP, getString(tab, "ContactCompanyZip"));
					bundle.putString(ContactsFragment.CONTACT_PHONE, getString(tab, "ContactCompanyPhone"));
					bundle.putString(ContactsFragment.CONTACT_FAX, getString(tab, "ContactCompanyFax"));
					bundle.putString(ContactsFragment.CONTACT_EMAIL, getString(tab, "ContactCompanyEmail"));
					bundle.putString(ContactsFragment.CONTACT_WWW, getString(tab, "ContactCompanyWww"));
					bundle.putString(ContactsFragment.CONTACT_MAP_TYPE, getString(tab, "ContactMapType"));
					bundle.putString(ContactsFragment.CONTACT_MAP_ZOOM, getString(tab, "ContactMapZoom"));
				}

				if (fragmentClass == null) {
					continue;
				}

				// Location info
				TextView txtTabInfo = new TextView(this);
				txtTabInfo.setText(title);

				// txtTabInfo.setBackgroundResource(R.drawable.bg_tab_left_active_right_inactive);
				txtTabInfo.setTextColor(Color.WHITE);
				txtTabInfo.setGravity(Gravity.CENTER_HORIZONTAL);
				txtTabInfo.setHeight(100);

				View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tab_layout, null);
				TextView tv = (TextView) view.findViewById(R.id.textView);

				tv.setText(title);
				mTabsAdapter.addTab(mTabHost.newTabSpec(title).setIndicator(view), fragmentClass, bundle, title);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private String getString(JSONObject object, String key) throws JSONException {
		if (object.has(key)) {
			String res = object.getString(key);
			if (!"{}".equals(res)) {
				return res;
			}
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.refresh_button:
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(mTabsAdapter.getCurrentFragmentTag(mTabHost.getCurrentTab()));
			if (fragment instanceof IRefreshFragment) {
				((IRefreshFragment) fragment).refreshFragment();
			}

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tab", mTabHost.getCurrentTabTag());
	}

	/**
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated TabHost. It relies on a
	 * trick. Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show. This is not sufficient for switching
	 * between pages. So instead we make the content part of the tab host 0dp
	 * high (it is not shown) and the TabsAdapter supplies its own dummy view to
	 * show as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct paged in the ViewPager whenever the selected tab
	 * changes.
	 */
	public static class TabsAdapter extends FragmentPagerAdapter implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
		private final MainActivity mainActivity;

		static final class TabInfo {
			private String tag;
			private final Class<?> clss;
			private final Bundle args;
			// private final String menuId;
			private Fragment fragment;

			TabInfo(String _tag, Class<?> _class, Bundle _args, String _menuId) {
				tag = _tag;
				clss = _class;
				args = _args;
				// menuId = _menuId;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabsAdapter(MainActivity activity, TabHost tabHost, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mTabHost = tabHost;
			mViewPager = pager;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
			mainActivity = activity;
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args, String menuId) {
			tabSpec.setContent(new DummyTabFactory(mContext));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args, menuId);
			mTabs.add(info);
			mTabHost.addTab(tabSpec);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			Fragment fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
			return fragment;
		}

		@Override
		public void onTabChanged(String tabId) {
			int position = mTabHost.getCurrentTab();

			Log.i("ll", "" + position);
			/**
			 * TabInfo newTab = mTabs.get(position); if (mLastTab != newTab) {
			 * if (mLastTab != newTab) { FragmentTransaction ft =
			 * mainActivity.getSupportFragmentManager().beginTransaction();
			 * 
			 * if (newTab != null) { if (newTab.fragment == null) {
			 * newTab.fragment = Fragment.instantiate(mainActivity,
			 * newTab.clss.getName(), newTab.args); ft.add(newTab.fragment,
			 * newTab.tag); } else { ft.replace(newTab.fragment, newTab.tag);
			 * 
			 * ft.attach(newTab.fragment); } } mLastTab = newTab; ft.commit();
			 * mainActivity
			 * .getSupportFragmentManager().executePendingTransactions(); } }
			 */

			TabInfo newTab = mTabs.get(position);

			if (newTab != null) {

			}
			/**
			 * if (mLastTab != newTab) {
			 * 
			 * if (mLastTab != null) { if (mLastTab.fragment != null) {
			 * 
			 * if (mLastTab.fragment instanceof ContactsFragment) {
			 * FragmentTransaction ft =
			 * mainActivity.getSupportFragmentManager().beginTransaction();
			 * ft.SupportMapFragment map = ((ContactsFragment)
			 * mLastTab.fragment).getGoogleMap(); ft.hide(map); ft.commit(); }
			 * // ft.detach(mLastTab.fragment);
			 * 
			 * } }
			 * 
			 * /** if (newTab != null) { if (newTab.fragment == null) {
			 * newTab.fragment = getItem(position); ft.add(newTab.fragment,
			 * newTab.tag); } else { ft.attach(newTab.fragment); } }
			 */

			// mLastTab = newTab;
			// }

			try {

				for (int i = 0; i < mTabs.size(); i++) {
					if (i == position) {
						continue;
					}
					TabInfo tab = mTabs.get(i);
					if (tab.fragment instanceof ContactsFragment) {

						SupportMapFragment map = ((ContactsFragment) tab.fragment).getGoogleMap();
						if (map != null) {
							FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
							ft.remove(map);
							ft.commit();
						}
					}
				}
			} catch (Exception ex) {

			}

			mViewPager.setCurrentItem(position);

			if (mainActivity.menu != null) {
				mainActivity.menu.findItem(R.id.refresh_button).setVisible(newTab.fragment instanceof IRefreshFragment);
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			// Unfortunately when TabHost changes the current tab, it kindly
			// also takes care of putting focus on it when not in touch mode.
			// The jerk.
			// This hack tries to prevent this from pulling focus out of our
			// ViewPager.
			TabWidget widget = mTabHost.getTabWidget();
			int oldFocusability = widget.getDescendantFocusability();
			widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			mTabHost.setCurrentTab(position);
			widget.setDescendantFocusability(oldFocusability);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			final Fragment fragment = (Fragment) super.instantiateItem(container, position);
			final TabInfo info = mTabs.get(position);
			info.tag = fragment.getTag(); // set it here
			info.fragment = fragment;
			return fragment;
		}

		public String getCurrentFragmentTag(int order) {
			return mTabs.get(order).tag;
		}
	}
}
