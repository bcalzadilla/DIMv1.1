// Generated code from Butter Knife. Do not modify!
package com.thingworx.sdk.android.steamexample;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class MainActivity$$ViewInjector {
  public static void inject(Finder finder, final com.thingworx.sdk.android.steamexample.MainActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131361877, "field 'surfaceView'");
    target.surfaceView = (android.view.SurfaceView) view;
    view = finder.findRequiredView(source, 2131361883, "field 'btn_take_photo' and field 'btn_latitud'");
    target.btn_take_photo = (android.widget.ImageButton) view;
    target.btn_latitud = (android.widget.ImageButton) view;
  }

  public static void reset(com.thingworx.sdk.android.steamexample.MainActivity target) {
    target.surfaceView = null;
    target.btn_take_photo = null;
    target.btn_latitud = null;
  }
}
