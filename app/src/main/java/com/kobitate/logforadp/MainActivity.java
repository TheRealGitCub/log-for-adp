package com.kobitate.logforadp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kobitate.etimejava.ETime;
import com.kobitate.etimejava.Timecard;
import com.kobitate.etimejava.WorkBlock;
import com.kobitate.etimejava.WorkDay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import layout.StatusFragment;
import layout.TimecardFragment;

public class MainActivity extends FragmentActivity implements StatusFragment.OnFragmentInteractionListener,TimecardFragment.OnFragmentInteractionListener {

	private ViewPager viewPager;
	private MainActivityTabsAdapter tabsAdapter;
	private TabLayout tabs;

	private ETime etime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		viewPager = (ViewPager) findViewById(R.id.view_pager);
		tabsAdapter = new MainActivityTabsAdapter(getSupportFragmentManager());
		tabs = (TabLayout) findViewById(R.id.tabs);

		viewPager.setAdapter(tabsAdapter);
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				tabs.setScrollPosition(position, positionOffset, false);
			}

			@Override
			public void onPageSelected(int position) {
				viewPager.setCurrentItem(position);
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				viewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

		Intent intent = getIntent();
		Map<String, String> cookies = (HashMap<String, String>) intent.getSerializableExtra("cookies");

		etime = new ETime(cookies);

		GetTimecardTask getTimecard = new GetTimecardTask(MainActivity.this, findViewById(android.R.id.content));
		getTimecard.execute(new ETime[] {etime});


	}

	@Override
	public void onFragmentInteraction(Uri uri) {

	}
}

class GetTimecardTask extends AsyncTask {

	private View view;
	public Context context;
	private ETime etime;

	public GetTimecardTask(Context context, View view) {
		this.context = context;
		this.view = view;
	}

	@Override
	protected Object doInBackground(Object[] etime) {
		try {
			this.etime = (ETime) etime[0];
			return this.etime.getTimecard();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(context, "Error reading Timecard", Toast.LENGTH_SHORT).show();
			return e;
		}
	}

	@Override
	protected void onPostExecute(final Object o) {
		super.onPostExecute(o);
		if (o instanceof Timecard) {
			double hours = ((Timecard) o).getTotalHours();
			int minutes = (int)(hours - Math.floor(hours)) * 60;

			String todayKey = ((Timecard) o).getTodayKey();
			WorkDay today = ((Timecard) o).get(todayKey);

			double numHoursToday = 0;
			int minutesToday = 0;

			if (today != null) {
				numHoursToday = today.getHours();
				minutesToday = (int)(numHoursToday - Math.floor(hours)) * 60;
			}

			final boolean clockedIn = ((Timecard) o).clockedIn();

			RelativeLayout spinner = (RelativeLayout) view.findViewById(R.id.status_load);
			LinearLayout ui = (LinearLayout) view.findViewById(R.id.status_ui);

			TextView hoursToday = (TextView) view.findViewById(R.id.hours_today_value);
			TextView hoursTotal = (TextView) view.findViewById(R.id.hours_period_value);
			TextView statusTitle = (TextView) view.findViewById(R.id.clocked_status1);
			TextView statusSubtitle = (TextView) view.findViewById(R.id.clocked_status2);

			FloatingActionButton clockInOutFab = (FloatingActionButton) view.findViewById(R.id.fab_clock_in_out);

			if (clockedIn) {
				clockInOutFab.setImageDrawable(view.getResources().getDrawable(R.drawable.icon_clock_out, context.getTheme()));
				statusTitle.setText(context.getString(R.string.status_clocked_in));
				statusSubtitle.setVisibility(View.VISIBLE);
				for (WorkBlock block : today.getBlocks()) {
					if (block.getTimeOut() == null) {
						statusSubtitle.setText(context.getString(R.string.status_since_time, block.getTimeIn()));
					}
				}
			} else {
				clockInOutFab.setImageDrawable(view.getResources().getDrawable(R.drawable.icon_clock_in, context.getTheme()));
				statusTitle.setText(context.getString(R.string.status_clocked_out));
				statusSubtitle.setVisibility(View.GONE);
			}

			hoursToday.setText(context.getString(R.string.info_hours_value, (int) Math.floor(numHoursToday), minutesToday));
			hoursTotal.setText(context.getString(R.string.info_hours_value, (int) Math.floor(hours), minutes));

			clockInOutFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					String dialogTitle = (clockedIn ? context.getString(R.string.action_clock_out) : context.getString(R.string.action_clock_in));
					String dialogMessage = (clockedIn ? context.getString(R.string.confirm_clock_out) : context.getString(R.string.confirm_clock_in));
					new AlertDialog.Builder(context)
							.setTitle(dialogTitle)
							.setMessage(dialogMessage)
							.setPositiveButton(dialogTitle, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									try {
										etime.stamp();
									} catch (IOException e) {
										Log.e("LOG FOR ADP", "Error processing stamp");
										e.printStackTrace();
									}
								}
							})
							.setNeutralButton(android.R.string.cancel, null)
							.show();
				}
			});

			spinner.setVisibility(View.GONE);
			ui.setVisibility(View.VISIBLE);

		}
	}
}
