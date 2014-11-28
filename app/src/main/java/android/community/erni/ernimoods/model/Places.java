package android.community.erni.ernimoods.model;

import java.util.List;

/**
 * POJO to store a list of places
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
