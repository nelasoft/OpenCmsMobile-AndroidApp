package cz.nelasoft.opencms.mobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImageLoader {

	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;

	int placeholderResourceId;

	public ImageLoader(Context context, int placeholderResourceId) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
		this.placeholderResourceId = placeholderResourceId;
	}

	public static Bitmap ScaleBitmap(Bitmap bm, float scalingFactor) {
		int scaleHeight = (int) (bm.getHeight() * scalingFactor);
		int scaleWidth = (int) (bm.getWidth() * scalingFactor);

		return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
	}

	public void displayImage(String url, ImageView imageView, Context context) {
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);

		if (bitmap != null) {

			// Get display width from device
			int displayWidth = context.getResources().getDisplayMetrics().widthPixels;

			// Get margin to use it for calculating to max width of the
			// ImageView
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
			int leftMargin = layoutParams.leftMargin;
			int rightMargin = layoutParams.rightMargin;

			// Calculate the max width of the imageView
			int imageViewWidth = displayWidth - (leftMargin + rightMargin);

			// Calculate scaling factor and return it
			float scalingFactor = ((float) imageViewWidth / (float) bitmap.getWidth());

			// Create a new bitmap with the scaling factor
			Bitmap newBitmap = ScaleBitmap(bitmap, scalingFactor);

			// Set the bitmap as the ImageView source
			imageView.setImageBitmap(newBitmap);

		} else {
			queuePhoto(url, imageView, context);
			// imageView.setImageResource(placeholderResourceId);
		}
	}

	public void displayImage(String url, ImageView imageView) {
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);

		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		} else {
			queuePhoto(url, imageView, null);
			// imageView.setImageResource(placeholderResourceId);
		}
	}

	private void queuePhoto(String url, ImageView imageView, Context context) {
		PhotoToLoad p = new PhotoToLoad(url, imageView, context);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) {
		File f = fileCache.getFile(url);

		// from SD cache
		Bitmap b = decodeFile(f);
		if (b != null)
			return b;

		// from web
		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			copyStream(is, os);
			os.close();
			bitmap = decodeFile(f);
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			// o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		public Context context;

		public PhotoToLoad(String u, ImageView i, Context context) {
			url = u;
			imageView = i;
			this.context = context;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null) {
				if (photoToLoad.context != null) {
					// Get display width from device
					int displayWidth = photoToLoad.context.getResources().getDisplayMetrics().widthPixels;

					// Get margin to use it for calculating to max width of the
					// ImageView
					LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) photoToLoad.imageView.getLayoutParams();
					int leftMargin = layoutParams.leftMargin;
					int rightMargin = layoutParams.rightMargin;

					// Calculate the max width of the imageView
					int imageViewWidth = displayWidth - (leftMargin + rightMargin);

					// Calculate scaling factor and return it
					float scalingFactor = ((float) imageViewWidth / (float) bitmap.getWidth());

					// Create a new bitmap with the scaling factor
					Bitmap newBitmap = ScaleBitmap(bitmap, scalingFactor);

					// Set the bitmap as the ImageView source
					photoToLoad.imageView.setImageBitmap(newBitmap);
				} else {
					photoToLoad.imageView.setImageBitmap(bitmap);
				}
			}
			// photoToLoad.imageView.setImageResource(placeholderResourceId);
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}
}