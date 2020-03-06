package com.ats.wizo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ats.wizo.R;
import com.ats.wizo.model.Room;
import com.ats.wizo.sqlite.DBHandler;

import github.chenupt.springindicator.SpringIndicator;
import github.chenupt.springindicator.viewpager.ScrollerViewPager;

public class HelperTabActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private SpringIndicator indicator;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private static ScrollerViewPager mViewPager;

    public static int no = 3;
    static RatingBar ratingBar;
    static ImageView ivLogo;
    static TextView tvHeading, tvNote, tvSkip, tvNext, tvPageCount;
    static boolean isLast = false;
    static int section = -1;
    static ImageView ivInfo;
    static Button btnOrderNow, btnRegister, btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_tab);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        indicator = (SpringIndicator) findViewById(R.id.indicator);

        // Set up the ViewPager with the sections adapter.

        tvNext = findViewById(R.id.tvNext);
        tvSkip = findViewById(R.id.tvSkip);

        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        indicator.setViewPager(mViewPager);

    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        mViewPager.setCurrentItem(item, smoothScroll);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {


        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }


        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_helper_tab, container, false);


            //  tvHeading = rootView.findViewById(R.id.tvHeading);
            //  tvNote = rootView.findViewById(R.id.tvNote);
            //  tvPageCount = rootView.findViewById(R.id.tvPageCount);

            ivInfo = rootView.findViewById(R.id.ivInfo);
            btnOrderNow = rootView.findViewById(R.id.btnOrderNow);
            btnRegister = rootView.findViewById(R.id.btnRegister);
            btnLogin = rootView.findViewById(R.id.btnLogin);

            try {
                section = getArguments().getInt(ARG_SECTION_NUMBER);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Exception ", "..");
            }


            if (section == 1) {
                // tvNote.setText("1");
                // TextView tv = rootView.findViewById(tvPageCount.getId());
                // tv.setText("1");
                ivInfo.setImageResource(R.mipmap.info_screen_1);

                isLast = false;
            } else if (section == 2) {
                // tvNote.setText("2");
                // TextView tv = rootView.findViewById(tvPageCount.getId());

                // tv.setText("2");
                ivInfo.setImageResource(R.mipmap.info_screen_2);


                isLast = false;
            } else if (section == 3) {
                // tvNote.setText("3");
                //TextView tv = rootView.findViewById(tvPageCount.getId());

                //tv.setText("3");
                ivInfo.setImageResource(R.mipmap.info_screen_3);
                btnOrderNow.setVisibility(View.VISIBLE);

                isLast = true;
            }


            tvNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int current = mViewPager.getCurrentItem();

                    if (current == 0 || current == 1) {
                        ((HelperTabActivity) getActivity()).setCurrentItem(current + 1, true);
                    } else {


                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();
                    }

                }
            });


            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            });

            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), RegisterActivity.class));
                }
            });


            btnOrderNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(getActivity(), OrderActivity.class));

                }
            });


            return rootView;
        }


    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        Context context;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {


            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {

            return no;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return String.valueOf(position + 1);
        }
    }
}
