package com.muflone.android.django_hotels.api;

import android.content.Context;
import android.net.Uri;

import com.google.android.apps.authenticator.Base32String;
import com.muflone.android.django_hotels.Settings;
import com.muflone.android.django_hotels.Singleton;
import com.muflone.android.django_hotels.api.exceptions.InvalidDateTimeException;
import com.muflone.android.django_hotels.api.exceptions.InvalidResponseException;
import com.muflone.android.django_hotels.api.exceptions.NoConnectionException;
import com.muflone.android.django_hotels.api.exceptions.NoDownloadException;
import com.muflone.android.django_hotels.api.tasks.AsyncTaskDownload;
import com.muflone.android.django_hotels.AsyncTaskListener;
import com.muflone.android.django_hotels.api.tasks.AsyncTaskLoadDatabase;
import com.muflone.android.django_hotels.database.models.Contract;
import com.muflone.android.django_hotels.database.models.Service;
import com.muflone.android.django_hotels.database.models.Structure;
import com.muflone.android.django_hotels.otp.Token;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

public class Api {
    public final Settings settings;
    public final Context context;

    public Api(Context context) {
        this.context = context;
        this.settings = Singleton.getInstance().settings;
    }

    private Uri buildUri(String segment) {
        // Return the Uri for the requested segment
        return Uri.withAppendedPath(this.settings.getApiUri(), segment);
    }

    private Uri buildJsonUri(String segment) {
        // Return the Uri for the requested JSON API segment
        return Uri.withAppendedPath(this.buildUri("api/v1/"), segment);
    }

    private JSONObject getJSONObject(String segment) {
        // Return a JSONObject from the remote URL
        JSONObject jsonObject = null;
        try {
            URL requestUrl = new URL(this.buildJsonUri(segment).toString());
            URLConnection connection = requestUrl.openConnection();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonStringBuilder = new StringBuilder();
            // Save all the received text in jsonStringBuilder
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                jsonStringBuilder.append(line);
            }
            // Convert results to JSON
            jsonObject = new JSONObject(jsonStringBuilder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String getCurrentTokenCode() {
        // Return the current TokenCode
        Token token = null;
        try {
            Uri uri = Uri.parse(String.format(
                    "otpauth://totp/Hotels:Tablet %s?secret=%s&issuer=Muflone",
                    this.settings.getTabletID(),
                    Base32String.encode(this.settings.getTabletKey().getBytes())));
            token = new Token(uri);
        } catch (Token.TokenUriInvalidException e) {
            e.printStackTrace();
        }
        return token != null ? token.generateCodes().getCurrentCode() : null;
    }

    private void checkStatusResponse(JSONObject jsonObject) throws InvalidResponseException {
        // Check the status object for valid data
        try {
            if (!jsonObject.getString("status").equals("OK")) {
                throw new InvalidResponseException();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new InvalidResponseException();
        }
    }

    public ApiData getData(String tabletId, String tokenCode) {
        ApiData data = new ApiData();
        // Check if the system date/time matches with the remote date/time
        JSONObject jsonRoot = this.getJSONObject("dates/");
        if (jsonRoot != null) {
            try {
                // Get current system date only
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeZone(TimeZone.getTimeZone(this.settings.getTimeZone()));
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Date date1 = calendar.getTime();
                // Get remote date
                Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(jsonRoot.getString("date"));
                long difference = Math.abs(date1.getTime() - date2.getTime());
                // If the dates match then compare the time
                if (difference == 0) {
                    // Get current system time only
                    calendar = Calendar.getInstance();
                    calendar.setTimeZone(TimeZone.getTimeZone(this.settings.getTimeZone()));
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    calendar.set(Calendar.MONTH, 0);
                    calendar.set(Calendar.YEAR, 1970);
                    date1 = calendar.getTime();
                    // Get remote time
                    date2 = new SimpleDateFormat("HH:mm.ss").parse(jsonRoot.getString("time"));
                    // Find the difference in thirty seconds
                    difference = Math.abs(date1.getTime() - date2.getTime()) / 1000 / 30;
                }
                if (difference != 0) {
                    // Invalid date or time
                    data.exception = new InvalidDateTimeException();
                }
            } catch (ParseException exception) {
                exception.printStackTrace();
                data.exception = new InvalidResponseException();
            } catch (JSONException exception) {
                exception.printStackTrace();
                data.exception = new InvalidResponseException();
            }
        } else {
            // Whether the result cannot be get raise exception
            data.exception = new NoConnectionException();
        }
        if (data.exception == null) {
            // Get data from the server
            jsonRoot = this.getJSONObject(String.format("get/%s/%s/", tabletId, tokenCode));
            if (jsonRoot != null) {
                try {
                    // Loop over every structure
                    JSONObject jsonStructures = jsonRoot.getJSONObject("structures");
                    Iterator<?> jsonKeys = jsonStructures.keys();
                    while (jsonKeys.hasNext()) {
                        String key = (String) jsonKeys.next();
                        Structure objStructure = new Structure(jsonStructures.getJSONObject(key));
                        data.structuresMap.put(objStructure.id, objStructure);
                    }
                    // Loop over every contract
                    JSONArray jsonContracts = jsonRoot.getJSONArray("contracts");
                    for (int i = 0; i < jsonContracts.length(); i++) {
                        Contract contract = new Contract(jsonContracts.getJSONObject(i));
                        data.contractsMap.put(contract.id, contract);
                    }
                    // Loop over every service
                    JSONArray jsonServices = jsonRoot.getJSONArray("services");
                    for (int i = 0; i < jsonServices.length(); i++) {
                        Service service = new Service(jsonServices.getJSONObject(i));
                        if (service.extra_service) {
                            data.serviceExtraMap.put(service.id, service);
                        } else {
                            data.serviceMap.put(service.id, service);
                        }
                    }
                    // Check the final node for successful reads
                    this.checkStatusResponse(jsonRoot);
                } catch (JSONException e) {
                    data.exception = new InvalidResponseException();
                } catch (ParseException e) {
                    data.exception = new InvalidResponseException();
                } catch (InvalidResponseException e) {
                    data.exception = e;
                }
            } else {
                // Unable to download data from the server
                data.exception = new NoDownloadException();
            }
        }
        return data;
    }

    public void getData(AsyncTaskListener callback) {
        AsyncTaskDownload task = new AsyncTaskDownload(this, callback);
        task.execute();
    }

    public void loadFromDatabase(AsyncTaskListener callback) {
        AsyncTaskLoadDatabase task = new AsyncTaskLoadDatabase(this, callback);
        task.execute();
    }
}
