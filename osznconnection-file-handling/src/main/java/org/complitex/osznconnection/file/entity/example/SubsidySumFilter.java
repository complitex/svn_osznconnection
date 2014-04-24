package org.complitex.osznconnection.file.entity.example;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly Ivanov
 */
public class SubsidySumFilter implements Serializable{
    private boolean abs = false;
    private int compare = 0;

    private Map<String, Object> map = new HashMap<>();

    public boolean isAbs() {
        return abs;
    }

    public void setAbs(boolean abs) {
        this.abs = abs;
    }

    public int getCompare() {
        return compare;
    }

    public void setCompare(int compare) {
        this.compare = compare;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
