package cz.nelasoft.opencms.mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockFragment;

public class WebContentFragment extends SherlockFragment implements IRefreshFragment {

	private WebView webView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);
		if (container == null)
			return null;

		Bundle bundle = getArguments();
		String url = bundle.getString("url");
		String htmlContent = bundle.getString("htmlContent");
		if (url == null && htmlContent == null) {
			htmlContent = "<p>" + getString(R.string.content_is_not_available) + "<//p>";
		}

		View view = inflater.inflate(R.layout.web_content, container, false);

		webView = (WebView) view.findViewById(R.id.web_content);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new CmsWebViewClient());
		webView.getSettings().setPluginState(WebSettings.PluginState.ON);
		webView.getSettings().setBuiltInZoomControls(false);
		webView.getSettings().setSupportZoom(false);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setDomStorageEnabled(true);
		if (url != null) {
			webView.loadUrl(url);
		} else {
			webView.loadData(htmlContent, "text/html", "UTF-8");
		}
		return view;
	}

	private class CmsWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	@Override
	public void refreshFragment() {
		webView.loadUrl("javascript:window.location.reload( true )");
	}

}
