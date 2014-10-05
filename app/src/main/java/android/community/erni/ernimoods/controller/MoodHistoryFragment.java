package android.community.erni.ernimoods.controller;

import android.app.Fragment;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.api.JSONResponseException;
import android.community.erni.ernimoods.api.MoodsBackend;
import android.community.erni.ernimoods.api.UserBackend;
import android.community.erni.ernimoods.model.Mood;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This fragment is used to enter your current mood
 */
public class MoodHistoryFragment extends Fragment {

    private TimeSeries mySeries = null;
    private XYMultipleSeriesDataset mDataset = null;
    private XYSeriesRenderer renderer = null;
    private XYMultipleSeriesRenderer mRenderer = null;
    private GraphicalView myChart = null;

    //storage variable to handle the mood-request
    private MoodsBackend.OnConversionCompleted callHandlerGetMoods;
    //error handler to handle errors from the request
    private MoodsBackend.OnJSONResponseError errorHandlerGetMoods;
    private MoodsBackend getMoods = null;

    private HashMap<Long, String> dateCommentMap = new HashMap<Long, String>();

    private ArrayList<Mood> currentMoods = null;

    SimpleDateFormat myDateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");


    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_history, container, false);

        mySeries = new TimeSeries("Mood History");

        mDataset = new XYMultipleSeriesDataset();
        mDataset.addSeries(mySeries);

        renderer = new XYSeriesRenderer();

        renderer.setLineWidth(4);
        renderer.setColor(Color.BLUE);
        // Include low and max value
        renderer.setDisplayBoundingPoints(true);
        // we add point markers
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setPointStrokeWidth(3);
        renderer.setAnnotationsColor(Color.LTGRAY);

        mRenderer = new XYMultipleSeriesRenderer();

        mRenderer.addSeriesRenderer(renderer);

        // We want to avoid black border
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        mRenderer.setLabelsColor(Color.BLACK);
        mRenderer.setXLabelsColor(Color.BLACK);
        mRenderer.setYLabelsColor(0, Color.BLACK);
        // transparent margins // Disable Pan on two axis
        mRenderer.setPanEnabled(true, false);
        mRenderer.setZoomEnabled(true, false);
        mRenderer.setYAxisMax(6);
        mRenderer.setYAxisMin(0);
        mRenderer.setLabelsTextSize(20);
        mRenderer.setXRoundedLabels(false);
        mRenderer.setXLabelsAngle(285);
        mRenderer.setXTitle("Date");
        mRenderer.setYTitle("Mood [1-5]");
        mRenderer.setAxisTitleTextSize(28);
        mRenderer.setBackgroundColor(Color.LTGRAY);
        mRenderer.setShowLegend(false);
        mRenderer.setMargins(new int[]{100, 100, 100, 100});
        mRenderer.setClickEnabled(true);
        mRenderer.setSelectableBuffer(10);

        this.view = view;

        //attach call handler. this method is called as soon as the moods-list is loaded
        callHandlerGetMoods = new MoodsBackend.OnConversionCompleted<ArrayList<Mood>>() {
            @Override
            //what to do on successful conversion?
            public void onConversionCompleted(ArrayList<Mood> moods) {
                //Add markers for all moods
                currentMoods = moods;
                dateCommentMap.clear();
                mDataset.removeSeries(mySeries);
                mRenderer.removeAllRenderers();
                mySeries.clear();
                Iterator<Mood> it = moods.iterator();
                Mood currentMood = null;
                while (it.hasNext()) {
                    currentMood = it.next();
                    mySeries.add((Date) currentMood.getDate(), currentMood.getMood());
                    dateCommentMap.put(currentMood.getDate().getTime(), currentMood.getComment());
                }
                mDataset.addSeries(mySeries);
                mRenderer.addSeriesRenderer(renderer);
                myChart.repaint();
            }
        };

        errorHandlerGetMoods = new UserBackend.OnJSONResponseError() {
            @Override
            public void onJSONResponseError(JSONResponseException e) {
                //user does not exist or something else went wrong
                Log.d("Something went wrong", e.getErrorCode() + ": " + e.getErrorMessage());
            }
        };

        //create a moods backend object
        getMoods = new MoodsBackend();
        //set listener to handle successful retrieval
        getMoods.setListener(callHandlerGetMoods);
        getMoods.setErrorListener(errorHandlerGetMoods);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //load username and password from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = prefs.getString("pref_username", null);

        //start async-task
        /*for (int i = 1; i <= 10; i++) {
            mySeries.add(i, Math.round(Math.random() * 5));
        }*/

        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.chart);

        if (myChart == null) {
            myChart = ChartFactory.getTimeChartView(getActivity(), mDataset, mRenderer, myDateFormat.toPattern());
            myChart.setClickable(true);
            myChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SeriesSelection seriesSelection = myChart.getCurrentSeriesAndPoint();

                    if (seriesSelection != null) {


                        long clickedDateSeconds = (long) seriesSelection.getXValue();
                        Date clickedDate = new Date(clickedDateSeconds);
                        String comment = dateCommentMap.get((long) seriesSelection.getXValue());


                        // Displaying Toast Message
                        Toast.makeText(
                                getActivity().getBaseContext(),
                                "Comment: " + comment,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            layout.addView(myChart);
        } else {
            myChart.repaint();
        }

        getMoods.getMoodsByUsername(username);


    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
