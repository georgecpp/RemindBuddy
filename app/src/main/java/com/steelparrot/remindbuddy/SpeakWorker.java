package com.steelparrot.remindbuddy;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Locale;

public class SpeakWorker extends Worker implements TextToSpeech.OnInitListener {

    private static final String TAG = "SpeakWorker";
    private TextToSpeech mTextToSpeech = null;
    private String taskTitle = "";
    private String utteranceId = "";

    public SpeakWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mTextToSpeech = new TextToSpeech(context, this);
        taskTitle = getInputData().getString("TaskTitle");
        utteranceId = String.valueOf(getInputData().getInt("NotificationId",0));

    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG,"DOING SPEAK WORK ON BACKGROUND");
        return Result.success();
    }

    @Override
    public void onInit(int i) {
        if(i == TextToSpeech.SUCCESS) {
            mTextToSpeech.setPitch(1.0f);
            Bundle params = new Bundle();
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,1.0f);
            mTextToSpeech.speak("New task: "+taskTitle,TextToSpeech.QUEUE_FLUSH,params,utteranceId);
        }
    }
}
