package com.artifex.mupdfdemo;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SearchListActivity extends Activity implements OnItemClickListener{
	
	ArrayList<SearchData> searchList;
	private MuPDFCore core;
	private Builder mAlertBuilder;

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		if (findViewById(200) == null) {
			
			SearchData searchData = (SearchData)arg0.getAdapter().getItem(arg2);
			setResult(searchData.pageNumber);
			finish();
		}
	}
	
	private MuPDFCore openFile(String path) {
		Log.v("Trying to open ", path);
		try {
			core = new MuPDFCore(path);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return core;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAlertBuilder = new AlertDialog.Builder(this);

		if (core == null) {
			core = (MuPDFCore)getLastNonConfigurationInstance();
			
		}
		if (core == null) {
			String pdfURL = getIntent().getStringExtra("pdf_url");
			Uri uri = Uri.parse(pdfURL);
			core = openFile(Uri.decode(uri.getEncodedPath()));
		}
		if (core == null)
		{
			showAlert();
			return;
		}
		
		createUI(savedInstanceState);
	}

	private void showAlert()
	{
		AlertDialog alert = mAlertBuilder.create();
		alert.setTitle(R.string.open_failed);
		alert.setButton(AlertDialog.BUTTON_POSITIVE, "Dismiss",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
		alert.show();
	}

	public void createUI(Bundle savedInstanceState) {
		if (core == null)
			return;

		setContentView(R.layout.search_list_layout);
		searchList = PDFSearchResultManager.getSharedInstance().getSearchResult();
		PDFSearchResultManager.getSharedInstance().setSearchResult(null);
		ListView listView = (ListView)findViewById(R.id.search_list);
		listView.setOnItemClickListener(this);
		listView.setAdapter(new MuPDFPageAdapter());
		
	}
	
	private class MuPDFPageAdapter extends BaseAdapter {
		
		private final SparseArray<PointF> mPageSizes = new SparseArray<PointF>();
		private Point mPoint;
		
		public MuPDFPageAdapter() {
			
			mPoint = new Point(100, 100);
			core.countPages();
		}

		@Override
		public int getCount() {
			return searchList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return searchList.get(arg0);
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			
			final MuPDFPageView pageView;
			LinearLayout pageLayout = null;
			if (convertView == null) 
			{
				pageView = new MuPDFPageView(SearchListActivity.this, core, mPoint);
				pageView.setLayoutParams(new LayoutParams(100, 100));
				convertView = View.inflate(SearchListActivity.this, R.layout.search_item_layout, null);
				pageLayout = (LinearLayout) convertView.findViewById(R.id.thumb_layout);
				pageLayout.addView(pageView, 0);
			}
			else {
				pageLayout = (LinearLayout) convertView.findViewById(R.id.thumb_layout);
				pageView = (MuPDFPageView) pageLayout.getChildAt(0);
			}

			final int pageNumber = searchList.get(position).pageNumber;
			
			setDataOnItem(convertView, searchList.get(position));
			
			PointF pageSize = mPageSizes.get(position);
			if (pageSize != null) {
				// We already know the page size. Set it up
				// immediately
				pageView.setPage(pageNumber, pageSize);
			} else {
				// Page size as yet unknown. Blank it for now, and
				// start a background task to find the size
				pageView.blank(pageNumber);
				AsyncTask<Void,Void,PointF> sizingTask = new AsyncTask<Void,Void,PointF>() {
					@Override
					protected PointF doInBackground(Void... arg0) {
						return core.getPageSize(pageNumber);
					}

					@Override
					protected void onPostExecute(PointF result) {
						super.onPostExecute(result);
						// We now know the page size
						mPageSizes.put(position, result);
						// Check that this view hasn't been reused for
						// another page since we started
						if (pageView.getPage() == pageNumber)
							pageView.setPage(pageNumber, result);
					}
				};

				sizingTask.execute((Void)null);
			}
			return convertView;
		}
		
		private void setDataOnItem(View view, SearchData searchData)
		{
			TextView textView = (TextView)view.findViewById(R.id.searched_page_number);
			textView.setText("Page " + (searchData.pageNumber + 1));
			
			textView = (TextView)view.findViewById(R.id.text_containing_search);
			textView.setText(Html.fromHtml(searchData.spanText));
		}
		
	}

}
