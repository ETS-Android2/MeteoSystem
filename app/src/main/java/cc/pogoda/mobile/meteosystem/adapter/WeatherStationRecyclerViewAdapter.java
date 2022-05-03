package cc.pogoda.mobile.meteosystem.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cc.pogoda.mobile.meteosystem.Main;
import cc.pogoda.mobile.meteosystem.R;
import cc.pogoda.mobile.meteosystem.activity.handler.AllStationsActRecyclerViewButtonClickEvent;
import cc.pogoda.mobile.meteosystem.activity.updater.FavouritesStationDetailsOnListUpdater;
import cc.pogoda.mobile.meteosystem.activity.view.AllStationsActRecyclerViewHolder;
import cc.pogoda.mobile.meteosystem.dao.AvailableParametersDao;
import cc.pogoda.mobile.meteosystem.dao.SummaryDao;
import cc.pogoda.mobile.meteosystem.type.ParceableFavsCallReason;
import cc.pogoda.mobile.meteosystem.type.WeatherStation;

public class WeatherStationRecyclerViewAdapter extends RecyclerView.Adapter<AllStationsActRecyclerViewHolder> {

    private List<WeatherStation> stations;

    AppCompatActivity activity;

    ParceableFavsCallReason.Reason reason;

    SummaryDao summaryDao;
    
    AvailableParametersDao paramsDao;

    /**
     * This updater takes data stored in the hashmap and then updates TextViews on View Holders on
     * Favourites list
     */
    private FavouritesStationDetailsOnListUpdater favsUpdater = null;

    Handler handler = null;

    /**
     * This instance of 'Main' singleton class is used to obtain HashMap<String, Summary> stationSystemNameToSummary
     */
    Main main;

    public WeatherStationRecyclerViewAdapter(List<WeatherStation> stations, AppCompatActivity parentActivity, ParceableFavsCallReason.Reason callReason) {
        this.stations = stations;
        this.activity = parentActivity;
        this.reason = callReason;
        this.summaryDao = new SummaryDao();
        this.paramsDao = new AvailableParametersDao();
        this.main = (Main) parentActivity.getApplication();
    }

    @NonNull
    @Override
    public AllStationsActRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view;

        // check the call reason
        if (reason.equals(ParceableFavsCallReason.Reason.FAVOURITES)) {
            // inflate custom layout
            view = inflater.inflate(R.layout.activity_favourites_linear_layout_data, parent, false);
        }
        else {
            // Inflate the custom layout without current data
            view = inflater.inflate(R.layout.activity_all_stations_linear_layout, parent, false);
        }

        // Return a new holder instance
        AllStationsActRecyclerViewHolder viewHolder = new AllStationsActRecyclerViewHolder(view, reason);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AllStationsActRecyclerViewHolder holder, int position) {
        // this TextView shows the station name
        TextView textView = holder.textView;

        // this TextView shows station data if this is favourites list
        TextView textViewData = holder.textViewData;

        // button to go to the StationDetailsActivity
        Button button = holder.button;

        // get the station object from a list of either all stations or favourites
        WeatherStation station = stations.get(position);

        if (station != null) {
            textView.setText(station.getDisplayedName());
            button.setText(R.string.select_station);

            if (!reason.equals(ParceableFavsCallReason.Reason.FAVOURITES)) {
                if (station.getDisplayedName().length() > 22) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f);
                } else {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22.0f);
                }
            }
            else {
                if (station.getDisplayedName().length() > 22) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f);
                } else {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22.0f);
                }
            }

            button.setOnClickListener(new AllStationsActRecyclerViewButtonClickEvent(station, activity, reason));
        }

        // this if distinguish between All Stations and Favorites view
        if (textViewData != null && favsUpdater != null) {

            favsUpdater.addNewStation(station.getSystemName(), textViewData);
        }

    }

    @Override
    public int getItemCount() {
        return stations.size();
    }


    public void createAndStartUpdater() {

        // check if there is previous instance of updater
        if (favsUpdater != null && favsUpdater.isEnabled()) {
            stopUpdater();
        }

        handler = new Handler(Looper.getMainLooper());
        favsUpdater = new FavouritesStationDetailsOnListUpdater(handler, main.getHashmapStationSystemNameToSummary());

        handler.postDelayed(favsUpdater, 100);
        favsUpdater.setEnabled(true);
    }

    public void stopUpdater() {
        if (reason.equals(ParceableFavsCallReason.Reason.FAVOURITES)) {
            handler.removeCallbacks(favsUpdater);
            favsUpdater.setEnabled(false);
        }
    }

}
