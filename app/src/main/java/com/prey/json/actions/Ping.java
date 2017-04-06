package com.prey.json.actions;

import android.content.Context;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

import org.json.JSONObject;

import java.util.Map;
import java.util.List;


public class Ping extends JsonAction {
    @Override
    public HttpDataService run(Context ctx, List<ActionResult> list, JSONObject parameters) {
        return null;
    }

    @Override
    public List<HttpDataService> get(Context ctx, List<ActionResult> list, JSONObject parameters) {
        try{
            String messageId = null;
            try {
                messageId = parameters.getString(PreyConfig.MESSAGE_ID);
                PreyLogger.d("messageId:"+messageId);
            } catch (Exception e) {
            }
            String jobId = null;
            try {
                jobId = parameters.getString(PreyConfig.JOB_ID);
                PreyLogger.d("jobId:"+jobId);
            } catch (Exception e) {
            }
            Map<String,String> map=UtilJson.makeMapParam("start","ping","started",null);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"processed",messageId,map,jobId);
            PreyLogger.i("messageId:"+messageId);
        }catch (Exception e){
            PreyLogger.i("error ping:"+e.getMessage());
        }
        return null;
    }
}
