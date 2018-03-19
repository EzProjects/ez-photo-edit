package com.xulaoyao.ezphotoedit.model;

import android.graphics.Path;

/**
 * EzPathInfo
 * Created by renwoxing on 2018/3/19.
 */
public class EzPathInfo {

    public EzPathInfo() {
    }

    public EzPathInfo(Path path) {
        this.path = path;
    }

    public String name;
    public String id;
    public Path path;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
