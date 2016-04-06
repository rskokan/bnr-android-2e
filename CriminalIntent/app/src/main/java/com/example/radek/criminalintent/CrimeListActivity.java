package com.example.radek.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by radek on 09/01/16.
 */
public class CrimeListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_twopane;
    }
}
