package android.community.erni.ernimoods.controller;

import android.app.Fragment;
import android.community.erni.ernimoods.R;
import android.community.erni.ernimoods.model.Mood;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
 * This fragment faciliates the achartengine plotting library to display a scrollable chart
 * of the current user's mood history
 */
public class MoodHistoryFragment extends Fragment {

    //The time series stores dates (x) and mood (y)
    private TimeSeries mySeries = null;
    //achartengine can store multiple series by default, that's why this is used
    private XYMultipleSeriesDataset mDataset = null;
    /*
    The series renderer defines rendering options for one series
     */
    private XYSeriesRenderer renderer = null;
    /*
    The multiple series renderer defindes rendering options for the whole chart
     */
    private XYMultipleSeriesRenderer mRenderer = null;
    //this is the view element to store the chart and add to the layout
    private GraphicalView myChart = null;

    /**
     * To be able to map between dates and comments, we need to create a hashmap with an element
     * for each mood object. this is used to display the comment, when clicking on a datapoint in the chart
     */
    private HashMap<Long, String> dateCommentMap = new HashMap<Long, String>();

    //store the user's moods in a hashmap
    private ArrayList<Mood> currentMoods = null;

    //this date format is used to display the chart's x-labels
    SimpleDateFormat myDateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm");

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_history, container, false);

        /*
        chart setup
         */

        //create new time series
        mySeries = new TimeSeries(getString(R.string.tab_mood_history));

        //create new multipleseriesdataset and add our time series to the set
        mDataset = new XYMultipleSeriesDataset();
        mDataset.addSeries(mySeries);

        //create a new series renderer
        renderer = new XYSeriesRenderer();

        renderer.setLineWidth(4); //linewith
        renderer.setColor(Color.BLUE); //erni-like blue for the series
        renderer.setDisplayBoundingPoints(true);
        renderer.setPointStyle(PointStyle.CIRCLE); //circular markers
        renderer.setPointStrokeWidth(3);
        renderer.setAnnotationsColor(Color.LTGRAY); //light, grey background

        //create new multiple series renderer and add our mood history renderer
        mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);

        /*
        lots of settings for the chart's appearance
         */
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); //no black margin
        //black text labels
        mRenderer.setLabelsColor(Color.BLACK);
        mRenderer.setXLabelsColor(Color.BLACK);
        mRenderer.setYLabelsColor(0, Color.BLACK);
        //this makes the chart scrollable and zoomable only on the time-axis
        mRenderer.setPanEnabled(true, false);
        mRenderer.setZoomEnabled(true, false);
        //set the y-asxis limit such that all moods can be displayed
        mRenderer.setYAxisMax(6);
        mRenderer.setYAxisMin(0);
        mRenderer.setLabelsTextSize(20); //size of the labels
        mRenderer.setXRoundedLabels(false); //if this is true, date-labels somehow don't display
        mRenderer.setXLabelsAngle(285); //oblique x-labels
        //axis titles
        mRenderer.setXTitle(getString(R.string.date));
        mRenderer.setYTitle(getString(R.string.mood));
        mRenderer.setAxisTitleTextSize(28);
        //background color
        mRenderer.setBackgroundColor(Color.LTGRAY);
        mRenderer.setShowLegend(false); //no legend is necessary
        mRenderer.setMargins(new int[]{100, 100, 100, 100}); //add a nice margin around the chart
        //enable the chart to be clickable. clicks on datapoints will show the respective comments
        mRenderer.setClickEnabled(true);
        mRenderer.setSelectableBuffer(50);

        // show the action bar when this fragment is displayed
        getActivity().getActionBar().show();

        //make sure the MyMood Tab is highlighted
        getActivity().getActionBar().setSelectedNavigationItem(2);

        this.view = view;

        return view;
    }

    /**
     * This methods is called each time the fragment is resumed (also when it is created). We first
     * check whether the chart already exists. If not, we create it and initialize it with an empty
     * time series. Thereafter we load the moods and wait for the event handler to add them to the
     * chart.
     */
    @Override
    public void onResume() {
        super.onResume();

        //load username
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = prefs.getString("pref_username", null);

        //this is where we are going to add the chart
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.chart);

        //if the chart does not exist yet (application start up)
        if (myChart == null) {
            //create a timechartview
            myChart = ChartFactory.getTimeChartView(getActivity(), mDataset, mRenderer, myDateFormat.toPattern());
            //make it clickable
            myChart.setClickable(true);
            //attach an event listener, that handles clicks on datapoints
            myChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //stores the clicked time-series
                    SeriesSelection seriesSelection = myChart.getCurrentSeriesAndPoint();
                    //we only want to handle the event if actually a series has been clicked
                    if (seriesSelection != null) {

                        //get the date of the closest datapoint in milliseconds
                        long clickedDateSeconds = (long) seriesSelection.getXValue();
                        //get the respective comment from the hashmap
                        String comment = dateCommentMap.get((long) seriesSelection.getXValue());

                        // Displaying Toast Message
                        Toast.makeText(
                                getActivity().getBaseContext(),
                                getString(R.string.comment_alert_title) + comment,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //finally add the chart to our view
            layout.addView(myChart);
            //if the chart has already been created, we don't to anything else than repainting
        } else {
            myChart.repaint();
        }

        //get current user's moods
        currentMoods = ((EntryPoint) getActivity()).getMyMoods();
        //clear previous hashmap
        dateCommentMap.clear();
        //clear plot data from previous plots
        mDataset.removeSeries(mySeries);
        mRenderer.removeAllRenderers();
        mySeries.clear();
        //iterate through all moods belonging to a user and add the data to the plot and
        //to the hashmap
        if (currentMoods != null) {
            Iterator<Mood> it = currentMoods.iterator();
            Mood currentMood = null;
            while (it.hasNext()) {
                currentMood = it.next();
                mySeries.add((Date) currentMood.getDate(), currentMood.getMood());
                dateCommentMap.put(currentMood.getDate().getTime(), currentMood.getComment());
            }
        }
        //add the new series to the renderer and repaint the chart
        mDataset.addSeries(mySeries);
        mRenderer.addSeriesRenderer(renderer);
        myChart.repaint();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
