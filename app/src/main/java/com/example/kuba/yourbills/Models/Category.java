package com.example.kuba.yourbills.Models;

import android.graphics.drawable.Drawable;

public class Category {

    private String title;
    private Drawable icon;

    public Category(String title, Drawable icon){
        this.title = title;
        this.icon = icon;
    }


    public String getTitle(){
        return title;
    }

    public Drawable getIcon(){
        return icon;
    }






}
