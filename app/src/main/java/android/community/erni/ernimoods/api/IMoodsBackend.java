package android.community.erni.ernimoods.api;

import android.community.erni.ernimoods.model.Mood;

import java.util.ArrayList;

/**
 * Created by niklausd on 02.09.2014.
 */
public interface IMoodsBackend {
    public void postMood(Mood mood);
    public void updateMood(Mood mood);
    public ArrayList<Mood>getAllMoods();
}
