package org.hofapps.tinyplanet;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

//    private PlanetChangeCallBack mPlanetChangeCallBacks;
    private CoordinatorLayout coordinatorLayout;
    private SettingsFragment settingsFragment;
    FragmentManager fragmentManager;
    private ImageView imageView;

//    private static final int ARRAY_MIN_POS = 0;
//    private static final int ARRAY_MAX_POS = 1;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorLayout);

        // TODO: Check why we need here getChildFragmentManager instead of getFragmentManager and set sdkMinVersion to 15 back!

        imageView = (ImageView) view.findViewById(R.id.imageView);
        fragmentManager = getChildFragmentManager();
        settingsFragment = (SettingsFragment) fragmentManager.findFragmentById(R.id.settings_fragment);



//        ViewGroup.LayoutParams params = settingsFragment.getView().getLayoutParams();
//        params.height = 10;
//        settingsFragment.getView().setLayoutParams(params);

        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (settingsFragment.isHidden()) {
                    fragmentManager.beginTransaction().setCustomAnimations(
                            R.animator.slide_up,
                            R.animator.slide_down,
                            R.animator.slide_up,
                            R.animator.slide_down).show(settingsFragment).commit();
                    Animation animation = new ScaleAnimation(1f, .9f, 1f, .9f, imageView.getPivotX(), imageView.getPivotY());
                    animation.setFillAfter(true);
                    animation.setDuration(20);
                    imageView.startAnimation(animation);

                }
                else {
                    fragmentManager.beginTransaction().setCustomAnimations(
                            R.animator.slide_up,
                            R.animator.slide_down,
                            R.animator.slide_up,
                            R.animator.slide_down
                    ).hide(settingsFragment).commit();

                    Animation animation = new ScaleAnimation(.9f, 1f, .9f, 1f, imageView.getPivotX(), imageView.getPivotY());
                    animation.setFillAfter(true);

                    // config_mediumAnimTime
                    animation.setDuration(20);
                    imageView.startAnimation(animation);

                }





            }
        });



    }

    public void initSeekBarValues(int size, int scale, int angle) {

        settingsFragment.initSeekBarValues(size, scale, angle);

    }

    public void setScaleBarValue(int position) {

        settingsFragment.setScaleBarValue(position);

    }



//    public void updatePlanetSize(int size) {
//
//        settingsFragment.updateSizeSeekBar(size);
//
//    }


//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mPlanetChangeCallBacks = (PlanetChangeCallBack) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException("Activity must implement SeekBarCallBacks.");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//
//        super.onDetach();
//        mPlanetChangeCallBacks = null;
//
//    }


//    private SeekBar.OnSeekBarChangeListener getSeekBarListener() {
//
//        SeekBar.OnSeekBarChangeListener l = new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//
//                int id = seekBar.getId();
//
//                int value;
//
//                if (id == R.id.size_seekBar) {
//
//                    value = getSeekBarValue(R.array.size_seekbar_values, i);
//                    mPlanetChangeCallBacks.onSizeChange(value);
//                }
//                else if (id == R.id.scale_seekBar) {
//
//                    value = getSeekBarValue(R.array.scale_seekbar_values, i);
//                    mPlanetChangeCallBacks.onScaleChange(value);
//
//                }
//                else if (id == R.id.angle_seekBar) {
//
//                    value = getSeekBarValue(R.array.angle_seekbar_values, i);
//                    mPlanetChangeCallBacks.onAngleChange(value);
//
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        };
//
//        return l;
//
//    }

//    private int getSeekBarValue(int seekBarId, int pos) {
//
//        int value;
//
//        int[] array = getResources().getIntArray(seekBarId);
//
//        value = (int) (pos * (array[ARRAY_MAX_POS] - array[ARRAY_MIN_POS])) / 100;
//
//        return value;
//
//    }

//    private SeekBar.OnSeekBarChangeListener getScaleSeekBarListener() {
//
//        SeekBar.OnSeekBarChangeListener l = new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//
//                int sId = seekBar.getId();
//                int scaleId = R.id.scale_seekBar;
//
//                mPlanetChangeCallBacks.onScaleChange(i);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
////                mPlanetChangeCallBacks.onSizeChange();
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
////                mPlanetChangeCallBacks.onSizeChange();
//
//            }
//        };
//
//        return l;
//
//    }


//    public static interface PlanetChangeCallBack {
//
//        void onSizeChange(int size);
//        void onScaleChange(int scale);
//        void onAngleChange(int angle);
//    }




}
