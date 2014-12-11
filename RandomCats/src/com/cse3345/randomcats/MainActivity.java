/**
 * Author: Justin Konersmann
 * */
package com.cse3345.randomcats;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends Activity {
	//Asynchronous Task
	private Context context;
	private ProgressDialog progressDialog;
	private ProgressDialog progressDialogPic;
	
	private ImageView mainImage;
	private Drawable pic;
	
	private String response;
	//private String catApiUrl = "http://thecatapi.com/api/images/get?format=xml";
	private String catApiUrl;
	private String sampleCatUrl = "http://24.media.tumblr.com/tumblr_m1jwx7Idy41qfhy97o1_500.jpg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = MainActivity.this;
		
		mainImage = (ImageView) findViewById(R.id.mainImage);
		
		//get xml with picture link(s)
		getLinksAsync task1 = new getLinksAsync();
		task1.execute((Object[]) null);
		
		//load pic into image
		getCatAsync task2 = new getCatAsync();
		task2.execute((Object[]) null);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class getCatAsync extends AsyncTask<Object, Object, Object> {
		private boolean success = false;

		@Override
		protected void onPreExecute() {
			progressDialogPic = new ProgressDialog(context);
			progressDialogPic.setTitle("Loading a cat pic...");
			progressDialogPic.setMessage(":3");
			progressDialogPic.setCancelable(false);
			progressDialogPic.setIndeterminate(true);
			progressDialogPic.show();
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				InputStream is = (InputStream) new URL(sampleCatUrl).getContent();
				pic = Drawable.createFromStream(is, "");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(pic.toString() != null){
				success = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (progressDialogPic != null) {
				progressDialogPic.dismiss();
			}
			if (success) {
				System.out.println("Pic: " + pic);
				System.out.println("Load pic");
				//This is a success full URL hit
				mainImage.setImageDrawable(pic);
			} else {
				System.out.println("Failure.");
			}
		}
	}
	
	private class getLinksAsync extends AsyncTask<Object, Object, Object> {
		private boolean success = false;

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(context);
			progressDialog.setTitle("Loading cats...");
			progressDialog.setMessage(":3");
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(true);
			progressDialog.show();
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(catApiUrl);
				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				response = EntityUtils.toString(httpEntity);
			} catch (ParseException | IOException e) {
				e.printStackTrace();
			}
			System.out.println(response);
			if(response.length() != 0){
				success = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			if (success) {
				//This is a success full URL hit
				Document doc = null;
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					doc = builder.parse(new InputSource(new StringReader(response)));
				} catch (SAXException | IOException | ParserConfigurationException e) {
					e.printStackTrace();
				} 
				System.out.println(doc.getElementsByTagName("url").item(0).getTextContent());
				System.out.println(doc.getElementsByTagName("source_url").item(0).getTextContent());
				catApiUrl = doc.getElementsByTagName("url").item(0).getTextContent();
			}
		}
	}
}
