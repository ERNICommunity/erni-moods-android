package android.community.erni.ernimoods.controller;

import android.app.Fragment;
import android.community.erni.ernimoods.R;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * This fragment is used to enter your current mood
 */
public class MoodHistoryFragment extends Fragment {

    private XYSeries mySeries = null;
    private XYMultipleSeriesDataset mDataset = null;
    private XYSeriesRenderer renderer = null;
    private XYMultipleSeriesRenderer mRenderer = null;
    private GraphicalView myChart = null;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_history, container, false);

        mySeries = new XYSeries("Mood History");

        mDataset = new XYMultipleSeriesDataset();
        mDataset.addSeries(mySeries);

        renderer = new XYSeriesRenderer();

        renderer.setLineWidth(2);
        renderer.setColor(Color.RED);
        // Include low and max value
        renderer.setDisplayBoundingPoints(true);
        // we add point markers
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setPointStrokeWidth(3);

        mRenderer = new XYMultipleSeriesRenderer();

        mRenderer.addSeriesRenderer(renderer);

        // We want to avoid black border
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        // transparent margins // Disable Pan on two axis
        mRenderer.setPanEnabled(false, false);
        mRenderer.setYAxisMax(6);
        mRenderer.setYAxisMin(0);
        mRenderer.setShowGrid(true);

        this.view = view;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        for (int i = 1; i <= 10; i++) {
            mySeries.add(i, Math.round(Math.random() * 5));
        }

        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.chart);

        if (myChart == null) {
            myChart = ChartFactory.getLineChartView(getActivity(), mDataset, mRenderer);
            layout.addView(myChart);
        } else {
            myChart.repaint();
        }


    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
