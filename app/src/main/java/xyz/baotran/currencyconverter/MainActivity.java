package xyz.baotran.currencyconverter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    final String URL = "http://api.fixer.io/latest";
    //Default currencies
    String baseCurrency = "USD";
    String exchCurrency = "EUR";

    Map<String, Double> exchCurrenciesMap = null;

    EditText baseCurrency_EditText, exchCurrency_EditText;
    Spinner baseCurrency_Spinner, exchCurrency_Spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        makeServiceRequest();

        baseCurrency_EditText = (EditText) findViewById(R.id.base_currency_editText);
        exchCurrency_EditText = (EditText) findViewById(R.id.exch_currency_editText);
        activateTextEditListeners();

        baseCurrency_Spinner = (Spinner) findViewById(R.id.base_currency_spinner);
        exchCurrency_Spinner = (Spinner) findViewById(R.id.exch_currency_spinner);
        activateSpinnerListeners();

    }

    private void activateSpinnerListeners() {
        List<String> currencies = new ArrayList<>();
        //TODO why is this null ?????
        if (exchCurrenciesMap != null) {
            for (String currency : exchCurrenciesMap.keySet()) {
                currencies.add(currency);
            }

            ArrayAdapter<String> currenciesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currencies);
            baseCurrency_Spinner.setAdapter(currenciesArrayAdapter);
            baseCurrency_Spinner.setSelection(currenciesArrayAdapter.getPosition(baseCurrency));
            exchCurrency_Spinner.setAdapter(currenciesArrayAdapter);
            exchCurrency_Spinner.setSelection(currenciesArrayAdapter.getPosition(exchCurrency));
        }


    }

    private void activateTextEditListeners() {

        baseCurrency_EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Check will prevent the two editText to go into an infinite loop
                if (baseCurrency_EditText.isFocused() && baseCurrency_EditText.getText().length() > 0) {
                    Double dollarValue = Double.parseDouble(baseCurrency_EditText.getText().toString());
                    updateEditText(exchCurrency_EditText, convertCurrency(dollarValue, baseCurrency_EditText.getId()));
                }
            }
        });

        exchCurrency_EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
//                Check will prevent the two editText to go into an infinite loop
                if (exchCurrency_EditText.isFocused() && exchCurrency_EditText.getText().length() > 0) {
                    Double dollarValue = Double.parseDouble(exchCurrency_EditText.getText().toString());
                    updateEditText(baseCurrency_EditText, convertCurrency(dollarValue, exchCurrency_EditText.getId()));
                }
            }
        });


    }

    private Double convertCurrency(Double dollarValue, int editText_ID) {
//        Format double to three decimal places rouded up
        DecimalFormat df = new DecimalFormat(".###");
        df.setRoundingMode(RoundingMode.CEILING);

        if (editText_ID == baseCurrency_EditText.getId()){
            dollarValue *= exchCurrenciesMap.get(exchCurrency);
        } else {
            dollarValue /= exchCurrenciesMap.get(exchCurrency);
        }

        return Double.parseDouble(df.format(dollarValue));
    }

    private void updateEditText(EditText editText, Double dollarValue){

        editText.setText(dollarValue.toString());
    }

    private void makeServiceRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);

        String parameters = "base" + "=" + baseCurrency;
        StringBuilder queryURL = new StringBuilder();
        queryURL.append(URL);
        queryURL.append("?");
        queryURL.append(parameters);

        // Prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, queryURL.toString(), null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rates = (JSONObject) response.get("rates");
                            JSONArray currencyArray = rates.names();

                            Map<String, Double> currencyMap = new HashMap<>();

                            // Populate map with currency and conversion value
                            for (int i = 0; i < currencyArray.length(); i++) {
                                String currency = currencyArray.getString(i);
                                Double value = rates.getDouble(currency);

                                currencyMap.put(currency, value);
                            }
                            exchCurrenciesMap = currencyMap;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error.Response", error.getMessage());
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);
    }
}
