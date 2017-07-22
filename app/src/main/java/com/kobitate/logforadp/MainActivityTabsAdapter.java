package com.kobitate.logforadp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import layout.StatusFragment;
import layout.TimecardFragment;

/**
 * Created by kobi on 7/20/17.
 */

public class MainActivityTabsAdapter extends FragmentPagerAdapter {
	public MainActivityTabsAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position){
			case 0:
				return new StatusFragment();
			case 1:
				return new TimecardFragment();
			default:
				return null;
		}
	}

	@Override
	public int getCount() {
		return 2;
	}
}
