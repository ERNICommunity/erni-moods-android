package android.community.erni.ernimoods.model;

import java.util.List;

/**
 * Created by ue65403 on 18.11.2014.
 */
public class Places {
    private List<GooglePlace> results;

    public Places() {

    }

    public List<GooglePlace> getResults() {
        return this.results;
    }

    public void setResults(List<GooglePlace> results) {
        this.results = results;
    }
}
