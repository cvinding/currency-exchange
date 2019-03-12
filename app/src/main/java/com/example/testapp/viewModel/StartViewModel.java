package com.example.testapp.viewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.util.Log;

import com.example.testapp.listener.FormatCurrencyRatesListener;
import com.example.testapp.listener.RequestListener;
import com.example.testapp.model.CurrencyConverter;
import com.example.testapp.model.CustomRequest;
import com.example.testapp.model.FileHandler;
import com.example.testapp.model.NetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class StartViewModel extends ViewModel {

    private HashMap<NetworkHandler.NetworkType, Boolean> networkStatus;

    private MutableLiveData<Map<String, Double>> currencyRates;

    private MutableLiveData<String> lastUpdated;

    /**
     *
     * @return Map<String, Double>
     */
    public LiveData<Map<String, Double>> getCurrencyList(Context context) {
        if (lastUpdated == null) {
            lastUpdated = new MutableLiveData<>();
        }

        if (currencyRates == null) {
            currencyRates = new MutableLiveData<>();
            this.loadCurrencyRates(context, false);
        }

        return currencyRates;
    }

    public LiveData<String> getLastUpdated() {
        if (lastUpdated == null) {
            lastUpdated = new MutableLiveData<>();
        }
        return lastUpdated;
    }

    /**
     *
     * @param fromCurrency
     * @param toCurrency
     * @param inputAmount
     * @return
     */
    public double convertCurrency(String fromCurrency, String toCurrency, double inputAmount) {
        CurrencyConverter ccm = new CurrencyConverter(this.currencyRates.getValue());
        return ccm.ConvertCurrency(fromCurrency, toCurrency, inputAmount);
    }

    /**
     *
     * @param context
     */
    public void setNetworkStatus(Context context) {
        this.networkStatus = NetworkHandler.getNetworkStatus(context);
    }

    /**
     *
     * @param context
     */
    private void loadCurrencyRates(Context context, boolean force) {
        // Initialize the FileHandler class
        FileHandler fileHandler = new FileHandler();

        // Give the file a name
        String filename = "apiData.json";

        // Check if the fileExists and read the file if that is true
        if(fileHandler.fileExists(filename, context) && !force) {
            Log.d("LoadCurrencyRates", "Reading file");
            formatCurrencyRates(fileHandler.readFromFile(filename, context), date -> {
                try {

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date updatedDate = sdf.parse(date);

                    long updateDateMS = updatedDate.getTime() + 57600000;

                    Date now = new Date();

                    if(now.getTime() > updateDateMS) {
                        loadCurrencyRates(context, true);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            });

            return;
        }

        // Check if there is a network connection
        if(!networkStatus.get(NetworkHandler.NetworkType.TYPE_MOBILE) && !networkStatus.get(NetworkHandler.NetworkType.TYPE_WIFI)) {
            return;
        }

        // Call API and write the data to the file
        new Thread(() -> {
            CustomRequest cReq = new CustomRequest("https://www.ecb.europa.eu", true);

            cReq.sendRequest("/stats/eurofxref/eurofxref-daily.xml", "POST", new RequestListener() {
                @Override
                public void onSuccess(String response) {
                    Log.d("LoadCurrencyRates", "Fetching file");
                    fileHandler.writeToFile(response, filename, context);
                    formatCurrencyRates(response, new FormatCurrencyRatesListener() {
                        @Override
                        public void onPostedDate(String date) {
                            //TODO: do nothing
                        }
                    });
                }

                @Override
                public void onFailure(String response, int responseCode) {
                    Log.d("CustomRequest", "Code: " + responseCode + " - " + response);
                }
            });
        }).start();
    }

    /**
     *
     * @param json
     */
    private void formatCurrencyRates(String json, FormatCurrencyRatesListener listener) {
        try {
            // This is ugly
            String converted = (new JSONObject(json)).optString("gesmes:Envelope");
            converted = (new JSONObject(converted)).optString("Cube");
            converted = (new JSONObject(converted)).optString("Cube");

            // Get last updated
            lastUpdated.postValue((new JSONObject(converted)).optString("time"));
            // Callback the time
            listener.onPostedDate((new JSONObject(converted)).optString("time"));

            converted = (new JSONObject(converted)).optString("Cube");

            // This is ugly
            JSONArray finalJSON = new JSONArray(converted);
            Map<String, Double> currencyRatesMap = new TreeMap<>();

            // Create the CurrencyRate object and insert the JSON data
            for(int i = 0; i < finalJSON.length(); i++){
                JSONObject obj = new JSONObject(finalJSON.getString(i));

                currencyRatesMap.put(obj.getString("currency"), obj.getDouble("rate"));
            }

            currencyRatesMap.put("EUR", 1.0);

            currencyRates.postValue(currencyRatesMap);

        } catch (JSONException ex) {
            Log.e("Exception", "JSON data was invalid: " + ex.toString());
        }
    }

}
