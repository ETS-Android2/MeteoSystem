package cc.pogoda.mobile.meteosystem.activity;

// https://www.softicons.com/web-icons/vector-stylish-weather-icons-by-bartosz-kaszubowski/sun-rays-cloud-icon#google_vignette

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.strictmode.Violation;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cc.pogoda.mobile.meteosystem.R;
import cc.pogoda.mobile.meteosystem.activity.handler.MainActImageButtonAllStationsClickEvent;
import cc.pogoda.mobile.meteosystem.activity.handler.MainActImageButtonExportClickEvent;
import cc.pogoda.mobile.meteosystem.activity.handler.MainActImageButtonFavouritesClickEvent;
import cc.pogoda.mobile.meteosystem.activity.handler.MainActImageButtonSettingsClickEvent;
import cc.pogoda.mobile.meteosystem.config.AppConfiguration;
import cc.pogoda.mobile.meteosystem.dao.AllStationsDao;
import cc.pogoda.mobile.meteosystem.file.ConfigurationFile;
import cc.pogoda.mobile.meteosystem.file.FavouritiesFile;
import cc.pogoda.mobile.meteosystem.file.FileNames;
import cc.pogoda.mobile.meteosystem.type.ParceableStationsList;
import cc.pogoda.mobile.meteosystem.type.WeatherStation;
import cc.pogoda.mobile.meteosystem.type.WeatherStationListEvent;

public class MainActivity extends AppCompatActivity {

    private Context baseContext;

    private FileNames fileNames;

    private FavouritiesFile favouritiesFile;

    private List<WeatherStation> listOfAllStations;

    List<WeatherStation> favs;

    private ParceableStationsList parceableListOfAllStations;

    private ParceableStationsList parceableListOfFavStations;

    private MainActImageButtonFavouritesClickEvent mainActImageButtonFavouritesClickEvent = null;

    private ImageButton imageButtonFavourites;

    private ImageButton exportButton;

    private ImageButton settingsButton;

    public MainActivity() {

    }

    private void recreateListOfFavs() {

        // check if this is a first call after application start
        if (favs == null) {
            favs = favouritiesFile.loadFavourites();
        }

        // if favs is still null it means that favourites file doesn't even exists
        // so and user hasn't added any station to it yet
        if (favs == null) {
            favs = new ArrayList<>();
        }
        else {
            // update values for the fav list with listOfAllStations
            //for (WeatherStation f : favs) {
            for (int i = 0; i < favs.size(); i++) {

                //
                WeatherStation fromFavs = favs.get(i);

                // find an index of updated station
                int idx = listOfAllStations.indexOf(fromFavs);

                // get the station
                WeatherStation fromAllStations = listOfAllStations.get(idx);

                // update all parameters
                fromFavs.setAvailableParameters(fromAllStations.getAvailableParameters());
                fromFavs.setMoreInfo(fromAllStations.getMoreInfo());
                fromFavs.setImageAlign(fromAllStations.getImageAlign());
                fromFavs.setImageUrl(fromAllStations.getImageUrl());
                fromFavs.setSponsorUrl(fromAllStations.getSponsorUrl());
                fromFavs.setMoreInfo(fromAllStations.getMoreInfo());
                fromFavs.setLon(fromAllStations.getLon());
                fromFavs.setLat(fromAllStations.getLat());
                fromFavs.setDisplayedName(fromAllStations.getDisplayedName());
                fromFavs.setDisplayedLocation(fromAllStations.getDisplayedLocation());
                fromFavs.setTimezone(fromAllStations.getTimezone());
                fromFavs.setCallsignSsid(fromAllStations.getCallsignSsid());
                fromFavs.setStationNameTextColor(fromAllStations.getStationNameTextColor());

                // there is no need to delete and put object on the list once again
                // as a list does not make a copy of the object. It (ArrayList) keeps
                // only a reference to an object


            }
        }

        parceableListOfFavStations = ParceableStationsList.createFromStdList(favs);

        // create an event handler fired when a user click 'favourites' button
        mainActImageButtonFavouritesClickEvent = new MainActImageButtonFavouritesClickEvent(this, parceableListOfFavStations);

        // assign on click listener
        if (imageButtonFavourites != null) {
            imageButtonFavourites.setOnClickListener(mainActImageButtonFavouritesClickEvent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Called when a user goes back to the main screen
     */
    @Override
    protected void onResume() {
        super.onResume();
        recreateListOfFavs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        baseContext = getApplicationContext();

        StrictMode.VmPolicy.Builder b = new StrictMode.VmPolicy.Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.VmPolicy policy = b.detectAll().detectNonSdkApiUsage().penaltyListener((Runnable r) -> r.run(), (Violation v) -> {v.printStackTrace();}).build();
            StrictMode.setVmPolicy(policy);
        }

        // register to Event bus to receive events when a station is added od removed from favourites
        EventBus.getDefault().register(this);

        AndroidThreeTen.init(this);

        ConfigurationFile confFile = new ConfigurationFile(baseContext);

        confFile.restoreFromFile();

        if (AppConfiguration.locale != null && !AppConfiguration.locale.equals("default") ) {
            Locale locale = new Locale(AppConfiguration.locale);
            Locale.setDefault(locale);
            Resources resources = this.getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }

        setContentView(R.layout.activity_main);

        fileNames = new FileNames(baseContext);

        favouritiesFile = new FavouritiesFile(fileNames);

        // download all stations from API
        listOfAllStations = new AllStationsDao().getAllStations();

        // convert this to parceable to exchange across intents
        parceableListOfAllStations = ParceableStationsList.createFromStdList(listOfAllStations);

        // recreate list of favorites
        recreateListOfFavs();

        ImageButton imageButtonAllStations = (ImageButton)findViewById(R.id.imageButtonAllStations);
        if (imageButtonAllStations != null)
            imageButtonAllStations.setOnClickListener(new MainActImageButtonAllStationsClickEvent(this, parceableListOfAllStations));

        imageButtonFavourites = (ImageButton)findViewById(R.id.imageButtonFavourites);

        // set an action for clicking on export data button
        exportButton = (ImageButton)findViewById(R.id.imageButtonExport);
        if (exportButton != null) {
            exportButton.setOnClickListener(new MainActImageButtonExportClickEvent(this, parceableListOfFavStations));
        }

        settingsButton = (ImageButton) findViewById(R.id.imageButtonSettings);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(new MainActImageButtonSettingsClickEvent(this, confFile));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //return super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.menu_item_translation_authors: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("ENG: Mateusz Lubecki\r\n" +
                        "CZE: Sylwiusz Pachel\r\n" +
                        "GER: Jakub Fiałek\r\n" +
                        "LAT: Andris Stikáns\r\n" +
                        "UKR, RUS: Влад Поливач \r\n(Wład Polywacz)\r\n\r\nProgram Icon: Bartosz Kaszubowski");
                builder.setPositiveButton(R.string.ok, (DialogInterface var1, int var2) -> {
                    var1.dismiss();
                });
                builder.create();
                builder.show();
            }
        }

        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void weatherStationListHandler(WeatherStationListEvent serviceEvent) {
        System.out.println(serviceEvent.toString());

        switch (serviceEvent.getEventReason()) {

            case ADD:
                // check of list consist this station
                if (favs.contains(serviceEvent.getStation())) {
                    return;
                }

                // add favourites to list
                favs.add(serviceEvent.getStation());

                try {
                    // save the list into JSON file
                    favouritiesFile.persistFavourities(favs);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case DELETE:
                favs.remove(serviceEvent.getStation());
                break;
        }

        // recreate parceable object and pass it everywhere
        recreateListOfFavs();
        //Toast.makeText(this, intentServiceResult.getResultValue(), Toast.LENGTH_SHORT).show();
    }

}