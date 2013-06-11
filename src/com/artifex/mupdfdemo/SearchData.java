package com.artifex.mupdfdemo;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchData implements Parcelable
{
	public int pageNumber;
	public String spanText;
	
	public SearchData() {}
	
	public SearchData(Parcel in) {
		
		pageNumber = in.readInt();
		spanText = in.readString();
	}
	
	public static final Parcelable.Creator<SearchData> CREATOR = new Parcelable.Creator<SearchData>() {
		public SearchData createFromParcel(Parcel in) {
			return new SearchData(in);
		}

		public SearchData[] newArray(int size) {
			return new SearchData[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
	
		dest.writeInt(pageNumber);
		dest.writeString(spanText);
	}
}