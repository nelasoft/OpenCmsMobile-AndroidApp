package cz.nelasoft.opencms.mobile;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {

	private String menuId;
	private String title;
	private String location;
	private String description;
	private Date startDate;

	public Event() {

	}

	public Event(Parcel in) {
		super();
		menuId = in.readString();
		title = in.readString();
		location = in.readString();
		description = in.readString();

		long dateTime = in.readLong();
		if (dateTime != 0) {
			startDate = new Date(dateTime);
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(menuId);
		out.writeString(title);
		out.writeString(location);
		out.writeString(description);

		long dateTime = 0;
		if (startDate != null) {
			dateTime = startDate.getTime();
		}

		out.writeLong(dateTime);
	}

	public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
		public Event createFromParcel(Parcel in) {
			return new Event(in);
		}

		public Event[] newArray(int size) {
			return new Event[size];
		}
	};

	public String getMenuId() {
		return menuId;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
}
