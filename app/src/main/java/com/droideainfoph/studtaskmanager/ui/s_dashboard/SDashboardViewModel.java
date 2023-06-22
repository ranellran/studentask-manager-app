package com.droideainfoph.studtaskmanager.ui.s_dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SDashboardViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SDashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is s_dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}