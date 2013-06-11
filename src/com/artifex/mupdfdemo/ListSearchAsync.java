package com.artifex.mupdfdemo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Base64;

public class ListSearchAsync extends AsyncTask<String, Integer, ArrayList<SearchData>>
{
	public interface ListSearchListener
	{
		public void onSearchCompleted(ArrayList<SearchData> searchList);
	}
	
	private ProgressDialog progressDialog;
	private Context mContext;
	private MuPDFCore mCore;
	
	public ListSearchAsync(Context context, MuPDFCore core) {
		
		mContext = context;
		mCore = core;
	}
	
	@Override
	protected ArrayList<SearchData> doInBackground(String... params) {
		
		ArrayList<SearchData> searchListing = null;
		String lowerCaseSearchString = params[0].toLowerCase();
		for (int i = 0; i < mCore.countPages(); i++) 
		{
			byte[] bytes = mCore.html(i);
			String base64Text = Base64.encodeToString(bytes, Base64.DEFAULT);
			byte[] decoded = Base64.decode(base64Text, Base64.DEFAULT);
			try 
			{
				String htmlText = new String(decoded, "UTF-8");
				ArrayList<String> spanTexts = getAllSpanText(htmlText);
				if(spanTexts != null && spanTexts.size() > 0)
				{
					for (int j = 0; j < spanTexts.size(); j++) 
					{
						String spanText = spanTexts.get(j);
						String lowerCaseSpanText = spanText.toLowerCase();
						int startIndex = 0;
						int searchFoundIndex = -1;
						while((searchFoundIndex = lowerCaseSpanText.indexOf(lowerCaseSearchString, startIndex)) != -1)
						{
							String subStringContainingSearchString = getSubStringContainingSearchString(spanText.substring(startIndex), lowerCaseSearchString);
							
							if(searchListing == null)
								searchListing = new ArrayList<SearchData>();
							SearchData searchData = new SearchData();
							searchData.pageNumber = i;
							searchData.spanText = subStringContainingSearchString;
							searchListing.add(searchData);
							searchData = null;
							
							startIndex = searchFoundIndex + lowerCaseSearchString.length();
						}
					}
				}
			}
			catch (UnsupportedEncodingException e) 
			{
				e.printStackTrace();
			}
			publishProgress(i);
		}

		return searchListing;
	}
	
	@Override
	protected void onPreExecute() {
		
		progressDialog = new ProgressDialog(mContext);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setTitle(mContext.getString(R.string.searching_));
		progressDialog.setMax(mCore.countPages());
		progressDialog.show();
		progressDialog.setProgress(0);
		super.onPreExecute();
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		
		progressDialog.setProgress(values[0]);
		super.onProgressUpdate(values);
	}
	
	@Override
	protected void onPostExecute(ArrayList<SearchData> result) {
		
		progressDialog.cancel();
		((ListSearchListener)mContext).onSearchCompleted(result);
		super.onPostExecute(result);
	}
	
	private String getSubStringContainingSearchString(String spanText, String lowerCaseSearchString) {

		String lowerCaseSpanText = spanText.toLowerCase();
		int foundIndex = lowerCaseSpanText.indexOf(lowerCaseSearchString);
		int start = 0;
		int end = lowerCaseSpanText.length();
		int wordsBeforeFoundString = 2;
		if(foundIndex > 1)
		{
			int count = 0;
			start = foundIndex;
			while(count < wordsBeforeFoundString && start > 1)
			{
				if((start = lowerCaseSpanText.substring(0, start - 1).lastIndexOf(" ")) == -1)
					start = 0;
				else
					start += 1;
				
				count++;
			}
		}
		
		int nextOccurance;
		if((nextOccurance = lowerCaseSpanText.indexOf(lowerCaseSearchString, foundIndex + lowerCaseSearchString.length())) != -1)
			end = nextOccurance;  
		
		return styleSelectedText(spanText.substring(start, end), lowerCaseSearchString);
	}
	
	private String styleSelectedText(String spanText, String lowerCaseSearchString)
	{
		String lowerCaseSpanText = spanText.toLowerCase();
		int foundIndex = lowerCaseSpanText.indexOf(lowerCaseSearchString);
		String searchStringInSpanText = spanText.substring(foundIndex, foundIndex + lowerCaseSearchString.length());
		spanText = spanText.replace(searchStringInSpanText, "<font size='24px'><b><u>" + searchStringInSpanText + "</u></b></font>");
		return spanText;
	}
	
	// To return ArrayList of all span text in a page
	private ArrayList<String> getAllSpanText(String pageText)
	{
		ArrayList<String> spanTexts = null;
		String startSpan = "<span class";
		String endSpan = "</span>";
		String endTag = ">";
		int startIndex = 0;
		int startSpanFoundIndex = -1;
		while((startSpanFoundIndex = pageText.indexOf(startSpan, startIndex)) != -1)
		{
			String spanText = pageText.substring(pageText.indexOf(endTag, startSpanFoundIndex) + endTag.length(), pageText.indexOf(endSpan, startSpanFoundIndex));
			
			if(spanTexts == null)
				spanTexts = new ArrayList<String>();
			spanTexts.add(spanText);
			startIndex = pageText.indexOf(endSpan, startSpanFoundIndex) + endSpan.length();
		}
		
		return spanTexts;
	}
	
}