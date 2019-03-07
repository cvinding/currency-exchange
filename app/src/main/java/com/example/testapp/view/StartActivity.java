package com.example.testapp.view;

import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.testapp.R;
import com.example.testapp.viewModel.StartViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StartActivity extends AppCompatActivity {

    // View Context
    private Context context;

    // ViewModel
    private StartViewModel viewModel;

    // CurrencyList
    private Map<String, Double> currencyList;

    // ImageView
    private ImageView swapConversion;

    // Spinners
    private Spinner fromCurrency;
    private Spinner toCurrency;

    // TextViews
    private TextView lastUpdated;
    private TextView inputAmount;
    private TextView outputAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Set view context
        context = this;

        // Get the ImageView
        swapConversion = findViewById(R.id.swapConversion);

        // Get both spinners
        fromCurrency = findViewById(R.id.fromCurrency);
        toCurrency = findViewById(R.id.toCurrency);

        // Get lastUpdated TextView
        lastUpdated = findViewById(R.id.lastUpdated);

        // Get input and output TextViews
        inputAmount = findViewById(R.id.inputAmount);
        outputAmount = findViewById(R.id.outputAmount);

        inputAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0) {
                    outputAmount.setText("");
                    return;
                }

                double output = viewModel.convertCurrency(fromCurrency.getSelectedItem().toString(), toCurrency.getSelectedItem().toString(), Double.parseDouble(s.toString()));

                outputAmount.setText(String.format("%.2f", output));
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Get the viewModel
        this.viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(StartViewModel.class);

        this.viewModel.setNetworkStatus(this.context);

        // Observe the currencyList and populate the spinners when there is data in currencyList
        this.viewModel.getCurrencyList(this.context).observe(this, this::populateSpinners);

        this.viewModel.getLastUpdated().observe(this, updateTime -> {
            Date date = null;
            try {
                date = (new SimpleDateFormat("yyyy-MM-dd")).parse(updateTime);

            } catch (Exception e) {
                Log.e("Exception", "Date formatting failed " + e.toString());
            }

            lastUpdated.setText(String.format("Sidst opdareret: %s", (new SimpleDateFormat("dd-MM-yyyy 16:00:00 ")).format(date)));
        });
    }

    /**
     * populateSpinners() is used for populating the spinners with currencies
     * @param currencyList uses the currencyList to populate the spinners
     */
    private void populateSpinners(Map<String, Double> currencyList) {

        this.currencyList = currencyList;

        List<String> currencies = new ArrayList<>();

        for (Map.Entry<String, Double> entry : currencyList.entrySet()){
            currencies.add(entry.getKey());
        }

        ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);

        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fromCurrency.setAdapter(aa);
        toCurrency.setAdapter(aa);

        swapConversion.setOnClickListener(this::swapConversion);
    }

    /**
     * swapConversion() is used for swapping the selected currencies
     * @param view the clicked view
     */
    private void swapConversion(View view) {
        int fromIndex = (int) fromCurrency.getSelectedItemId();
        int toIndex = (int) toCurrency.getSelectedItemId();

        fromCurrency.setSelection(toIndex);
        toCurrency.setSelection(fromIndex);
    }

}