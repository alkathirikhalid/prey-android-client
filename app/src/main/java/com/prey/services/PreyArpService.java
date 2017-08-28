package com.prey.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.StrictMode;
import com.prey.PreyLogger;
import com.prey.actions.location.LocationUtil;
import com.prey.actions.location.PreyLocation;
import com.prey.net.PreyHttpResponse;
import com.prey.net.PreyWebServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by oso on 18-08-17.
 */

public class PreyArpService extends Service {




    public PreyArpService(){

    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        PreyLogger.i("PreyArpService onCreate");
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        final Context ctx = this;
        PreyLogger.i("PreyArpService onStart");
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("/proc/net/arp")));

            String line;
            int i=0;

            String mac="";
            while((line = br.readLine()) != null) {
                if(i>0){
                    String[] arr = line.split("[ ]+");
                  PreyLogger.i("line:"+line);
                    if(!"00:00:00:00:00:00".equals(arr[3])) {
                        PreyLogger.i("line:" + arr[0] + " " + arr[3]);
                        if(!"".equals(mac))
                            mac+=",";
                        mac+=arr[3];
                    }
                }
                i++;
            }
            HashMap<String, Object> parameters=new HashMap<String, Object>();
            parameters.put("macs[]",mac);

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);
            //String url="http://10.10.2.98:3000/api/v2/lost_macs.json?api_key=37de7da3b7b4";
            String url="https://panel.preyhq.com/api/v2/lost_macs.json?api_key=37de7da3b7b4";
            PreyHttpResponse response=PreyWebServices.getInstance().lostMacs(this,url, parameters);
            if(response!=null) {
                String out = response.getResponseAsString();
                PreyLogger.i("out:" + out);
                String json="{\"prey\":"+out+"}";
                JSONObject jsnobject = new JSONObject(json);
                JSONArray jsonArray = jsnobject.getJSONArray("prey");

                List<String> listKeys=new ArrayList<String>();
                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject jsonLost = jsonArray.getJSONObject(j) ;
                    String key=jsonLost.getString("key");
                    //String state=jsonLost.getString("state");
                    //String mac_addresses_list_values=jsonLost.getString("mac_addresses_list_values");
                    if(key!=null){
                        key=key.replace("[","");
                        key=key.replace("]","");
                        key=key.replace("\"","");
                        if(!"".equals(key)) {
                            listKeys.add(key);
                        }

                    }

                }
                final List<String> list=listKeys;
                new Thread() {
                    public void run() {
                        try{
                            PreyLocation location=LocationUtil.getLocation(ctx,"",false);
                            if(location!=null) {
                                //String url2="http://10.10.2.98:3000/api/v2/process_lost_macs.json?api_key=37de7da3b7b4";
                                String url2 = "https://panel.preyhq.com/api/v2/process_lost_macs.json?api_key=37de7da3b7b4";
                                HashMap<String, Object> parameters2 = new HashMap<>();

                                parameters2.put("location", location.getLat() + "," + location.getLng());

                                parameters2.put("keys[]", list);
                                PreyHttpResponse response2 = PreyWebServices.getInstance().lostMacs(ctx, url2, parameters2);
                                if (response2 != null) {
                                    String out2 = response2.getResponseAsString();
                                    PreyLogger.i("out2:" + out2);
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                }.start();

            }else{
                PreyLogger.i("out:nulo");
            }
        } catch (Exception e) {
            PreyLogger.e("line error:"+e.getMessage(),e);
        }


    }
}
