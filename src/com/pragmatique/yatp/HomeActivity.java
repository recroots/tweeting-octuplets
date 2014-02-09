package com.pragmatique.yatp;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.TreeMap;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity implements OnClickListener {
	static String TWITTER_CONSUMER_KEY = "uaOvF6UgV8iQW0KkKCp3XA";
	static String TWITTER_CONSUMER_SECRET = "nOhlJsP7THt6RRBBkjyJnsdsLL6dFr8aC58N8kc8";
	static String TWITTER_ACCESS_TOKEN = "177919673-2VpiRegOE4Fwu6cYi7KicxFurOPbMeAeyqK6ZXlO";
	static String TWITTER_ACCESS_SECRET = "EMLmfKujdTxjrnxmLVv4uJI8kQXSs5WMHnpuHhqNVF7Z7";

	private static Twitter twitter;
	Button btnLoginTwitter;
	ImageView handleImageView, retweeterPI[] = new ImageView[8];
	ProgressBar pBar;
	TextView alertText;

	// twitter id Array of first 10 retweeters and other ids
	long idArray[] = new long[10];
	TreeMap<Integer, Long> treeMap = new TreeMap<Integer, Long>();
	Object[] followerSet = new Object[10];

	// status used to get the numbers
	String status;

	int i;
	// Internet Connection detector
	private ConnectionDetector cd;

	// Async task obj
	PostTask p;

	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// set up twitter object
		twitter = TwitterFactory.getSingleton();
		twitter.setOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
		AccessToken at = new AccessToken(TWITTER_ACCESS_TOKEN,
				TWITTER_ACCESS_SECRET);
		twitter.setOAuthAccessToken(at);

		// assign layout elements to variables
		handleImageView = (ImageView) findViewById(R.id.handleImage);
		retweeterPI[0] = (ImageView) findViewById(R.id.retweeterImage00);
		retweeterPI[1] = (ImageView) findViewById(R.id.retweeterImage01);
		retweeterPI[2] = (ImageView) findViewById(R.id.retweeterImage02);
		retweeterPI[3] = (ImageView) findViewById(R.id.retweeterImage03);
		retweeterPI[4] = (ImageView) findViewById(R.id.retweeterImage04);
		retweeterPI[5] = (ImageView) findViewById(R.id.retweeterImage05);
		retweeterPI[6] = (ImageView) findViewById(R.id.retweeterImage06);
		retweeterPI[7] = (ImageView) findViewById(R.id.retweeterImage07);
		btnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
		pBar = (ProgressBar) findViewById(R.id.progressBar);
		alertText = (TextView) findViewById(R.id.alertText);

		btnLoginTwitter.setOnClickListener(this);
		for (i = 0; i < 8; i++)
			retweeterPI[i].setOnClickListener(this);

		cd = new ConnectionDetector(getApplicationContext());
		if (!cd.isConnectingToInternet()) {
			alert.showAlertDialog(HomeActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	private class PostTask extends AsyncTask<String, Integer, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				Random rand = new Random();
				final User user = twitter.showUser("github");
				setimage(handleImageView, user.getProfileImageURL());
				publishProgress(6);
				status = user.getStatus().getText();
				IDs iDs = twitter.getRetweeterIds(user.getStatus().getId(), 10);
				publishProgress(12);

				User retweetUser;
				int count = 0;
				int progress = 12;
				for (long idIterator : iDs.getIDs()) {
					if (count > 7 || count > iDs.getIDs().length)
						break;
					retweetUser = twitter.showUser(idIterator);
					treeMap.put(retweetUser.getFollowersCount(), idIterator);
					progress = progress + rand.nextInt(7);
					publishProgress(progress);
					count++;
				}

				if (treeMap.size() == 0) {
					Log.d("Debug", "empty treemap");
					return "fails";
				}
				int key = treeMap.lastKey();
				long id = treeMap.get(treeMap.lastKey());
				setimage(retweeterPI[0], twitter.showUser(id)
						.getProfileImageURL());
				progress = 60;
				publishProgress(60);
				for (int i = 1; i < 8; i++) {
					key = treeMap.lowerKey(key);
					id = treeMap.get(key);
					setimage(retweeterPI[i], twitter.showUser(id)
							.getProfileImageURL());

					progress = progress + rand.nextInt(6);
					publishProgress(progress);
				}
				publishProgress(100);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			return "All Done!";
		}

		// set image to ImageView from imgURL
		void setimage(final ImageView iv, String imgURL) {
			final Bitmap bitmap;
			try {
				bitmap = BitmapFactory.decodeStream((InputStream) new URL(
						imgURL).getContent());

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						iv.setImageBitmap(bitmap);
					}
				});
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			updateProgressBar(values[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dismissProgressBar();
		}
	}

	public void updateProgressBar(Integer progress) {
		alertText.setText("Loading...    " + progress + "%");
		pBar.setProgress(progress);
	}

	public void dismissProgressBar() {
		pBar.setVisibility(View.GONE);
		alertText.setText(status);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.retweeterImage00:
			Toast.makeText(getApplicationContext(), "   1   ",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.retweeterImage01:
			Toast.makeText(getApplicationContext(), "   2   ",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.retweeterImage02:
			Toast.makeText(getApplicationContext(), "   3   ",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.retweeterImage03:
			Toast.makeText(getApplicationContext(), "   4   ",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.retweeterImage04:
			Toast.makeText(getApplicationContext(), "   5   ",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.retweeterImage05:
			Toast.makeText(getApplicationContext(), "   6   ",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.retweeterImage06:
			Toast.makeText(getApplicationContext(), "   7   ",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.retweeterImage07:
			Toast.makeText(getApplicationContext(), "   8   ",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.btnLoginTwitter:
			alertText.setText("Loading...    0%");
			pBar.setVisibility(View.VISIBLE);
			new PostTask().execute("skadoosh!");
			break;
		}

	}

}
