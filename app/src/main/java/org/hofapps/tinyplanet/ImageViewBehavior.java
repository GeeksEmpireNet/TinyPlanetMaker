package org.hofapps.tinyplanet;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by fabian on 13.10.2015.
 */
public class ImageViewBehavior extends CoordinatorLayout.Behavior<ImageView> {

    public ImageViewBehavior(Context context, AttributeSet attrs) {


    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, ImageView child, View dependency) {

        boolean isDependent = dependency instanceof RelativeLayout;
        return isDependent;

    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ImageView child, View dependency) {

        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);

        return true;

    }

}