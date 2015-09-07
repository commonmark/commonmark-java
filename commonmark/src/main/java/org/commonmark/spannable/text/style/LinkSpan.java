package org.commonmark.spannable.text.style;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

public class LinkSpan extends ClickableSpan {
    public static final String LOG_TAG = "LinkSpan";

    private final String mURL;

    public LinkSpan(String url) {
        mURL = url;
    }

    @Override
    public void onClick(View widget) {
        Uri uri = Uri.parse(mURL);
        onClick(widget, uri);
    }

    protected void onClick(View widget, Uri uri) {
        Context context = widget.getContext();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
        }
    }
}
