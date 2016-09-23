package com.example.senso.tutuapp;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ExpandableListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.app.SearchManager;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by Senso on 21.09.2016.
 */


public class WorkActivity extends Activity {

    ExpandableListAdapter listAdapter; //Кастомный адаптер
    ExpandableListView expListView; //View для нашего списка
    List<String> listDataHeader; //Лист заголовков(Header\Group)
    HashMap<String, List<String>> listDataChild; //Лист дочерних данных(child)
    SearchView searchView;
    List<String> departStation;//Массив станций отбытия
    List<String> arriveStation;//Массив станций прибытия
    List<String> data;//Дата поездки
    String textGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_layout);

        Intent newActivity1 = new Intent();
        setResult(RESULT_OK,newActivity1);


        //Ищем наш View
        expListView = (ExpandableListView)findViewById(R.id.expandable_list_view);

        // Подготавливаем List
        prepareListData();


        //"Прослушка" кликов на child
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                switch(groupPosition) {
                    case 0:
                        ListIterator<String> lit_d = departStation.listIterator();
                        json("citiesFrom",v,groupPosition,departStation,lit_d,departStation.size());
                        break;
                    case 1:
                        ListIterator<String> lit_a = arriveStation.listIterator();
                        json("citiesTo",v,groupPosition,arriveStation,lit_a,arriveStation.size());
                        break;
                }
                return false;
            }
        });

        //"Прослушка" кликов на заголовки (Header)
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                switch(groupPosition)
                {
                    case 0:
                        listDataHeader.set(groupPosition,"Станция отправления");
                        break;
                    case 1:
                        listDataHeader.set(groupPosition,"Станция прибытия");
                        break;
                    case 2:
                        DatePickerDialog _date;
                        _date = new DatePickerDialog(WorkActivity.this,dateCallback,2016, 10, 9);
                        _date.show();
                        break;
                }
                return false;
            }
        });


        //Подулючаем SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView = (SearchView) findViewById(R.id.searcher);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if(newText.isEmpty())
                {
                    //Обновляем заново наш лист, если строка поиска стала пуста
                    prepareListData();
                    return false;
                }
                else {
                    //Фильтруем данные
                    searchItem(newText);
                    return false;
                }
            }
        });
    }

    public void json(String where, View v, int groupPosition, List<String> array, ListIterator<String> lit, int size)
    {
        try {

            //Делаем поиск по городу для того, чтобы после процедуры поиска найти нужные нам данные
            expListView.getChildAt(expListView.getFirstVisiblePosition());
            TextView textView = (TextView) v.findViewById(R.id.lgroupItem);
            String sss = textView.getText().toString();
            String cityTitleView = sss.substring(sss.indexOf(",") + 1);//Берём из непосредственно нажатого view строку с названием инетресующего города
            cityTitleView = cityTitleView.replace(",", "");//Удаляем запятые

            JSONObject obj = new JSONObject(loadJSONFromAsset()); //Загружаем json файл из папки assets для создания объекта JSON. С помощью него можно будет работать с данным типом данных(CitiesTo/For)
            JSONArray jsonArray = obj.getJSONArray(where);//Создаём массив JSON, в который включаются все объекты типа citiesFrom

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject_in = jsonArray.getJSONObject(i);//Входим поочёредно в каждый объект и смотрим его свойства
                String city_Title = jsonObject_in.getString("cityTitle");
                if (cityTitleView.equals(city_Title)) {
                    String country_Title = jsonObject_in.getString("countryTitle");
                    String district_Title = jsonObject_in.getString("districtTitle");
                    String region_Title = jsonObject_in.getString("regionTitle");
                    String s = country_Title + "," + city_Title + "," + district_Title + "," + region_Title + " Станции: ";


                    JSONArray jsonArray1 = jsonObject_in.getJSONArray("stations");//Входим в массив станций внутри объекта типа citiesFrom
                    for (int j = 0; j < jsonArray1.length(); j++) {
                        JSONObject jsonObject = jsonArray1.getJSONObject(j);
                        String station_Title = jsonObject.getString("stationTitle");
                        String city_Title1 = jsonObject.getString("cityTitle");
                        s = s + station_Title + "(" + city_Title1 + ")" + "   ";
                    }
                    //Обновляем данные в listView
                    textView.setText(s);
                    //Обновлем данные в самом массиве
                    for (int j = 0; j < size; j++) {
                        if (lit.hasNext()) {
                            lit.next();
                            if (j == i) {
                                lit.set(s);
                            }
                        }
                    }

                    //Обновляем child массив
                    listDataChild.put(listDataHeader.get(groupPosition), array);

                    //Обновляем пункт отбытия
                    String ss = country_Title + " , " + city_Title;
                    View view = listAdapter.getGroupView(groupPosition,TRUE,null,null);
                    TextView textView_g = (TextView) view.findViewById(R.id.lgroupHeader);
                    textView_g.setText(ss);
                    textGroup = "";
                    textGroup = ss;
                    listAdapter.notifyDataSetChanged();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private DatePickerDialog.OnDateSetListener dateCallback = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            listDataHeader.set(2,"Дата отправления : " + dayOfMonth+"/"+  ++monthOfYear +"/"+  year);
            listAdapter.notifyDataSetChanged();
        }
    };


    //Создание контексного меню при удержании пункта назначения\отбытия
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

            // Показываем контекстное меню для child
         if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
             menu.setHeaderTitle("Выбор станций");
             try {
                 View view = listAdapter.getChildView(groupPosition,childPosition,FALSE,null,null);
                 expListView.getChildAt(expListView.getFirstVisiblePosition());
                 TextView textView = (TextView) view.findViewById(R.id.lgroupItem);
                 String sss = textView.getText().toString();
                 String[]myStrings = sss.split(",");
                 String cityTitleView = myStrings[1];

                 JSONObject obj = new JSONObject(loadJSONFromAsset());
                 JSONArray jsonArray = obj.getJSONArray("citiesFrom");

                 for (int i = 0; i < jsonArray.length(); i++) {
                     JSONObject jsonObject_in = jsonArray.getJSONObject(i);//Входим поочёредно в каждый объект и смотрим его свойства
                     String city_Title = jsonObject_in.getString("cityTitle");
                     if (cityTitleView.equals(city_Title)) {
                         JSONArray jsonArray1 = jsonObject_in.getJSONArray("stations");//Входим в массив станций внутри объекта типа citiesFrom
                         for (int j = 0; j < jsonArray1.length(); j++) {
                             JSONObject jsonObject = jsonArray1.getJSONObject(j);
                             String station_Title = jsonObject.getString("stationTitle");
                             String city_Title_child = jsonObject.getString("cityTitle");
                             String region_Title_child = jsonObject.getString("regionTitle");
                             String district_Title_child = jsonObject.getString("districtTitle");
                             String country_Title_child = jsonObject.getString("countryTitle");
                             menu.add(0, 0, 1,( station_Title + "," + city_Title_child  + ", " + country_Title_child + ", " +district_Title_child  + ", " +region_Title_child ).replaceAll("( ,)|(,,)", ""));//Добавляем имеющиеся станции в меню
                         }
                     }
                 }
             }
             catch (JSONException e)
             {
                 e.printStackTrace();
             }
             menu.add(0, 0, 1, "Выход");
        }
    }



    //Выбор пункта из контексного меню
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item
                .getMenuInfo();

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {


            String[]myStrings = item.toString().split(",");
            String stationTitle = myStrings[0];

            String ss = listDataHeader.get(groupPosition);
            if(!item.toString().equals("Выход")) {
                ss = textGroup + ", " + stationTitle;
            }

            TextView textView1 = (TextView) findViewById(R.id.lgroupHeader);
            textView1.setText(ss);

            listDataHeader.set(groupPosition,ss);

            listAdapter.notifyDataSetChanged();

        }
        return super.onContextItemSelected(item);
    }



    public void searchItem(String textToSearch)
    {
        int count = 0;
        for(;count < listDataHeader.size();count++) {
            int i = 0;
            int y = listAdapter.getChildrenCount(count);
            for (; i < y; i++) {
                if (!listDataChild.get(listDataHeader.get(count)).get(i).contains(textToSearch)) {
                    listDataChild.get(listDataHeader.get(count)).remove(i);
                    i--;
                    y--;
                }
            }
        }
        listAdapter.notifyDataSetChanged();

    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = WorkActivity.this.getAssets().open("allStations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }



        private void prepareListData() {
            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();
            // Наименования заголовков
            listDataHeader.add("Станция отправления");
            listDataHeader.add("Станция прибытия");
            listDataHeader.add("Дата отправления");
            departStation = new ArrayList<String>();
            arriveStation  = new ArrayList<String>();
            data = new ArrayList<String>();

            try {
                JSONObject obj = new JSONObject(loadJSONFromAsset());
                JSONArray jsonArray = obj.getJSONArray("citiesTo");
                for(int i = 0; i < jsonArray.length();i++)
                {
                    JSONObject jsonObject_in = jsonArray.getJSONObject(i);
                    String country_Title = jsonObject_in.getString("countryTitle");
                    String city_Title = jsonObject_in.getString("cityTitle");
                    String district_Title = jsonObject_in.getString("districtTitle");
                    String region_Title = jsonObject_in.getString("regionTitle");
                    arriveStation.add((country_Title + "," + city_Title + "," + district_Title + "," + region_Title).replaceAll("( ,)|(,,)", ""));
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            try {
                JSONObject obj = new JSONObject(loadJSONFromAsset());
                JSONArray jsonArray = obj.getJSONArray("citiesFrom");
                for(int i = 0; i < jsonArray.length();i++)
                {
                    JSONObject jsonObject_in = jsonArray.getJSONObject(i);
                    String country_Title = jsonObject_in.getString("countryTitle");
                    String city_Title = jsonObject_in.getString("cityTitle");
                    String district_Title = jsonObject_in.getString("districtTitle");
                    String region_Title = jsonObject_in.getString("regionTitle");
                    departStation.add((country_Title + "," + city_Title + "," + district_Title + "," + region_Title).replaceAll("( ,)|(,,)", ""));
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }


            listDataChild.put(listDataHeader.get(0), departStation); // Header, Child
            listDataChild.put(listDataHeader.get(1), arriveStation);
            listDataChild.put(listDataHeader.get(2), data);

            listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

            // Устанавлиаем listAdapter
            expListView.setAdapter(listAdapter);
            registerForContextMenu(expListView);


    }
}
