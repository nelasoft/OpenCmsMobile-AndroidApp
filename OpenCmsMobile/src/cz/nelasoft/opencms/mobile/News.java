package cz.nelasoft.opencms.mobile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class News implements Parcelable {

	private static Map<String, Bitmap> images = new HashMap<String, Bitmap>();

	private String menuId;
	private String title;
	private String imagePath;

	private String perex;
	private String content;
	private Date date;

	public News() {
	}

	public News(Parcel in) {
		super();
		menuId = in.readString();
		title = in.readString();
		perex = in.readString();
		imagePath = in.readString();
		content = in.readString();

		long dateTime = in.readLong();
		if (dateTime != 0) {
			date = new Date(dateTime);
		}
	}

	public String getHeadline() {
		return title;
	}

	public void setHeadline(String headline) {
		this.title = headline;
	}

	public Bitmap getBitmap() {
		return images.get(getKey());
	}

	public void setBitmap(Bitmap bitmap) {
		images.put(getKey(), bitmap);
	}

	public String getPerex() {
		return perex;
	}

	public void setPerex(String perex) {
		this.perex = perex;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMenuId() {
		return menuId;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	private String getKey() {
		return menuId + imagePath;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		// out.writeParcelable(bitmap, flags);
		out.writeString(menuId);
		out.writeString(title);
		out.writeString(perex);
		out.writeString(imagePath);
		out.writeString(content);
		long dateTime = 0;
		if (date != null) {
			dateTime = date.getTime();
		}

		out.writeLong(dateTime);
	}

	public static final Parcelable.Creator<News> CREATOR = new Parcelable.Creator<News>() {
		public News createFromParcel(Parcel in) {
			return new News(in);
		}

		public News[] newArray(int size) {
			return new News[size];
		}
	};
}
