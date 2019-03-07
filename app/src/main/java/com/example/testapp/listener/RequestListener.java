package com.example.testapp.listener;

/**
 * RequestListener is an interface for creating a callback for the CustomRequest class
 * @author Christian Vinding Rasmussen
 */
public interface RequestListener {

    void onSuccess(String response);

    void onFailure(String response, int responseCode);

}
