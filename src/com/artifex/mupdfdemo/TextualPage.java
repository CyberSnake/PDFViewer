package com.artifex.mupdfdemo;

import java.util.ArrayList;
import android.os.Parcel;
import android.os.Parcelable;

public class TextualPage implements Parcelable
{
	public int pageNumber;
	public ArrayList<String> spanTexts;
	
	public TextualPage() {}
	
	public TextualPage(Parcel in) {
		
		pageNumber = in.readInt();
		spanTexts = new ArrayList<String>();
		in.readList(spanTexts, String.class.getClassLoader());
	}

	public static final Parcelable.Creator<TextualPage> CREATOR = new Parcelable.Creator<TextualPage>() {
		public TextualPage createFromParcel(Parcel in) {
			return new TextualPage(in);
		}
		
		public TextualPage[] newArray(int size) {
			return new TextualPage[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		dest.writeInt(pageNumber);
		dest.writeList(spanTexts);
	}
}