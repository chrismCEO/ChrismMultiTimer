package com.chrismsolutions.chrismmultitimer;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * Created by Christian Myrvold on 30.11.2017.
 */

public class MultiTimerAdHelper extends ChrismAdHelper
{
    AdView mAdView;

    public MultiTimerAdHelper(Context context, boolean initHelper, boolean callback)
    {
        super(context, initHelper, callback);
    }

    @Override
    public AdView createAppSpecificAd(RelativeLayout view)
    {
        if (mAdView != null)
        {
            mAdView.setVisibility(View.GONE);
        }

        mAdView = null;

        LinearLayout linearLayout = view.findViewById(R.id.contentMainLayout);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        TypedValue typedValue = new TypedValue();
        int marginTop = 0;
        if (mContext.getTheme().resolveAttribute(R.attr.actionBarSize, typedValue, true))
        {
            marginTop =  TypedValue.complexToDimensionPixelSize(typedValue.data, mContext.getResources().getDisplayMetrics());
        }

        if (showAd())
        {
            mAdView = new AdView(mContext);
            RelativeLayout.LayoutParams adParams = new
                    RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            adParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            adParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

            adParams.setMargins(0, marginTop, 0, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                adParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mAdView.setId(View.generateViewId());
            }

            mAdView.setLayoutParams(adParams);
            mAdView.setAdSize(AdSize.SMART_BANNER);

            //Set the list to be below the ad
            layoutParams.addRule(RelativeLayout.BELOW, mAdView.getId());

        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                //Remove the rule so that the list can be shown without the ad
                layoutParams.removeRule(RelativeLayout.BELOW);
            }
        }
        linearLayout.setLayoutParams(layoutParams);
        layoutParams.topMargin = marginTop;

        return mAdView;
    }

}
