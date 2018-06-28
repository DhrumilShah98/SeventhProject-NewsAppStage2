package com.example.dhrumilshah.newsappstage2;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

class QueryUtils {
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();
    private static final int OK_RESPONSE_CODE = 200;
    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;
    private static final String KEY_RESPONSE_OBJECT = "response";
    private static final String KEY_RESULT_ARRAY = "results";
    private static final String KEY_TITLE = "webTitle";
    private static final String KEY_SECTION = "sectionName";
    private static final String KEY_WEB_PUBLICATION_DATE_AND_TIME = "webPublicationDate";
    private static final String KEY_URL = "webUrl";
    private static final String KEY_FIELDS_OBJECT = "fields";
    private static final String KEY_THUMBNAIL = "thumbnail";
    private static final String KEY_TAGS_ARRAY = "tags";
    private static final String KEY_WEB_TITLE_IN_TAGS_ARRAY = "webTitle";

    private QueryUtils() {
    }

    static List<News> fetchNewsData(String queryUrl, Context context) {
        URL url = createUrl(queryUrl, context);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url, context);
        } catch (IOException e) {
            Log.e(LOG_TAG, context.getString(R.string.http_request_error_message), e);
        }
        return extractNews(jsonResponse, context);
    }

    private static URL createUrl(String queryUrl, Context context) {
        URL url = null;
        try {
            url = new URL(queryUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, context.getString(R.string.problem_building_url), e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url, Context context) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setRequestMethod(context.getString(R.string.method_GET));
            urlConnection.connect();

            if (urlConnection.getResponseCode() == OK_RESPONSE_CODE) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream, context);
            } else {
                Log.e(LOG_TAG, context.getString(R.string.error_response_code_message) + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, context.getString(R.string.problem_retrieving_json_result), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream, Context context) throws IOException {
        StringBuilder outputString = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader isr = new InputStreamReader(inputStream, Charset.forName(context.getString(R.string.utf_8)));
            BufferedReader br = new BufferedReader(isr);
            String currentLine = br.readLine();
            while (currentLine != null) {
                outputString.append(currentLine);
                currentLine = br.readLine();
            }
        }
        return outputString.toString();
    }

    private static List<News> extractNews(String jsonResponse, Context context) {
        List<News> news = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            if(jsonObject.has(KEY_RESPONSE_OBJECT)){
                JSONObject responseJSONObject = jsonObject.getJSONObject(KEY_RESPONSE_OBJECT);
                if(responseJSONObject.has(KEY_RESULT_ARRAY)) {
                    JSONArray resultsJSONArray = responseJSONObject.getJSONArray(KEY_RESULT_ARRAY);
                    for (int i = 0; i < resultsJSONArray.length(); i++) {
                        JSONObject currentJSONObject = resultsJSONArray.getJSONObject(i);
                        String title;
                        if(currentJSONObject.has(KEY_TITLE)) {
                            title = currentJSONObject.getString(KEY_TITLE);
                        }else{
                            title = null;
                        }
                        String section;
                        if(currentJSONObject.has(KEY_SECTION)) {
                            section = currentJSONObject.getString(KEY_SECTION);
                        }else{
                            section = null;
                        }
                        String publicationDateTime;
                        if(currentJSONObject.has(KEY_WEB_PUBLICATION_DATE_AND_TIME)) {
                            publicationDateTime = currentJSONObject.getString(KEY_WEB_PUBLICATION_DATE_AND_TIME);
                        }else{
                            publicationDateTime = null;
                        }
                        String webUrl;
                        if(currentJSONObject.has(KEY_URL)) {
                            webUrl = currentJSONObject.getString(KEY_URL);
                        }else{
                            webUrl = null;
                        }
                        String thumbnail;
                        if(currentJSONObject.has(KEY_FIELDS_OBJECT)) {
                            JSONObject currentFieldsObject = currentJSONObject.getJSONObject(KEY_FIELDS_OBJECT);
                            if(currentFieldsObject.has(KEY_THUMBNAIL)) {
                                thumbnail = currentFieldsObject.getString(KEY_THUMBNAIL);
                            }else{
                                thumbnail = null;
                            }
                        }
                        else{
                            thumbnail = null;
                        }
                        ArrayList<String> authors = new ArrayList<>();
                        if(currentJSONObject.has(KEY_TAGS_ARRAY)) {
                            JSONArray currentTagsArray = currentJSONObject.getJSONArray(KEY_TAGS_ARRAY);
                            if (currentTagsArray == null || currentTagsArray.length() == 0) {
                                authors = null;
                            } else {
                                for (int j = 0; j < currentTagsArray.length(); j++) {
                                    JSONObject currentObjectInTags = currentTagsArray.getJSONObject(j);
                                    authors.add(currentObjectInTags.getString(KEY_WEB_TITLE_IN_TAGS_ARRAY));
                                }
                            }
                        }else{
                            authors = null;
                        }
                        news.add(new News(title, section, publicationDateTime, webUrl, thumbnail, authors));
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, context.getString(R.string.problem_parsing_news_json_result), e);
        }
        return news;
    }
}
