package cc.pogoda.mobile.pogodacc.activity.updater;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

import cc.pogoda.mobile.pogodacc.type.WeatherStation;
import cc.pogoda.mobile.pogodacc.web.StationBackgroundDownloader;

public class StationBackgroundImageUpdater implements Runnable{
    ImageView iv;

    TextView station_name;
    WeatherStation station;
    StationBackgroundDownloader downloader;

    Handler handler;

    public StationBackgroundImageUpdater(ImageView _background, TextView _station_name, WeatherStation _station, StationBackgroundDownloader _downloader, Handler _handler) {
        iv = _background;
        station = _station;
        station_name = _station_name;
        downloader = _downloader;
        handler = _handler;
    }

    @Override
    public void run() {

        Bitmap bitmap = downloader.getBitmap();

        if (bitmap != null) {
            station_name.setTextColor(station.getStationNameTextColor());
            station_name.setText(station.getDisplayedName());

            iv.setImageBitmap(bitmap);
        }
        else {
            handler.postDelayed(this, 200);
        }

    }
}
