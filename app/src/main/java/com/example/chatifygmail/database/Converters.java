package com.example.chatifygmail.database;

import androidx.room.TypeConverter;


import com.example.chatifygmail.data.Email;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Converters {
    @TypeConverter
    public static ArrayList<Email> fromString(String value) {
        Type listType = new TypeToken<ArrayList<Email>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<Email> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
