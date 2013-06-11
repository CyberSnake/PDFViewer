package com.artifex.mupdfdemo;

import java.util.ArrayList;

public class PDFSearchResultManager {

	public static PDFSearchResultManager sharedInstance;
	private ArrayList<SearchData> searchResultList;
	
	private PDFSearchResultManager(){}
	
	public static PDFSearchResultManager getSharedInstance() {
		
		if(sharedInstance == null)
			sharedInstance = new PDFSearchResultManager();
		
		return sharedInstance;
	}
	
	public ArrayList<SearchData> getSearchResult()
	{
		return searchResultList;
	}
	
	public void setSearchResult(ArrayList<SearchData> searchList) {
		
		searchResultList = searchList;
	}
	
}
