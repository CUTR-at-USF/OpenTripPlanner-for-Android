/*
 * Copyright 2014 Vreixo Gonzalez on 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usf.cutr.opentripplanner.android.util;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;

import edu.usf.cutr.opentripplanner.android.R;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{

    private LayoutInflater layoutInflater;

    private Map<Marker, TripInfo> markers;

    private Context context;

    public CustomInfoWindowAdapter(LayoutInflater layoutInflater, Context context){
        this.layoutInflater = layoutInflater;
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view  = layoutInflater.inflate(R.layout.custom_info_window, null);
        TextView title = (TextView) view.findViewById(R.id.customInfoWindowTitle);
        TextView snippet = (TextView)view.findViewById(R.id.customInfoWindowSnippet);

        title.setText(marker.getTitle());

        TripInfo infoMarker = markers.get(marker);
        if ((infoMarker == null) || (infoMarker.getSnippet() == null)){
            snippet.setText(marker.getSnippet());
        }
        else {
            snippet.setText(infoMarker.getSnippet());
            if (Boolean.TRUE.equals(infoMarker.realtime)){
                TextView liveUpdatesText = (TextView) view
                        .findViewById(R.id.customInfoWindowLiveUpdatesText);
                liveUpdatesText.setVisibility(View.VISIBLE);
                int delayInSeconds = infoMarker.getDelayInSeconds();
                String delay = ConversionUtils
                        .getFormattedDurationTextNoSeconds(delayInSeconds,
                                true, context);
                int color = ConversionUtils.getDelayColor(delayInSeconds, context);
                String delayText = "\u25cf";
                if (delayInSeconds == 0) {
                    delayText += context.getString(R.string.map_markers_warning_live_upates_on_time);
                }
                else if (delayInSeconds > 0) {
                    delayText += " " + delay + " "
                            + context
                                .getResources()
                                .getString(R.string.map_markers_warning_live_upates_late_arrival);
                }
                else {
                    String positiveDelay = delay.replace("-","");
                    delayText += " " + positiveDelay + " "
                            + context
                                .getResources()
                                .getString(R.string.map_markers_warning_live_upates_early_arrival);
                }
                SpannableString delayFullText = new SpannableString(delayText);
                delayFullText.setSpan(new ForegroundColorSpan(color), 0, delayText.length(), 0);
                liveUpdatesText.setText(delayFullText);
            }
        }

        return view;
    }

    public void setMarkers(Map<Marker, TripInfo> markers) {
        this.markers = markers;
    }
}
