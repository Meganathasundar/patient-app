package com.patientapp.health.data;

public interface Callback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
