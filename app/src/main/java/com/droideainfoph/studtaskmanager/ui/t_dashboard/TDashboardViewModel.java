package com.droideainfoph.studtaskmanager.ui.t_dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TDashboardViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public TDashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is t_dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}