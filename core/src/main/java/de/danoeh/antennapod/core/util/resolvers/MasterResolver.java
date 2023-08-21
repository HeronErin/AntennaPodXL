package de.danoeh.antennapod.core.util.resolvers;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.danoeh.antennapod.core.service.download.AntennapodHttpClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MasterResolver {
    public static Map<String, String> UnResolveMedia = new HashMap<>();



    public interface Resolver{
        String resolveUrl(String url) throws IOException;
    }

    private static Map<String, String> youtubeCache = new HashMap<>();
    private static final Resolver youtubeResolve = (url)->{
        if (youtubeCache.containsKey(url)){
            String potentialUrl = youtubeCache.get(url);
            long expire = Long.valueOf(potentialUrl.split("expire=")[1].split("&")[0])*1000l;
            if (expire > System.currentTimeMillis())
                return potentialUrl;
        }
        OkHttpClient client = AntennapodHttpClient.newBuilder().followRedirects(false).followSslRedirects(false).build();
        Request.Builder httpReq = new Request.Builder()
                .url("https://vid.puffyan.us/latest_version?itag=251&id="+url);

        Response response = client.newCall(httpReq.build()).execute();


        if (response.code() == 302) {
            String location = response.header("Location");
            youtubeCache.put(url, location);
            return location;
        }else{
            Log.e("MasterResolver", "Error with video " + url);
            return "https://vid.puffyan.us/latest_version?itag=251&id=" + url;
        }





    };

    public static final Map<String, Resolver> ResolveArray = Map.ofEntries(
            Map.entry("YT-VID://", youtubeResolve)
    );



    public static String resolve(String url) throws IOException {
        for (Map.Entry<String, Resolver> entry : ResolveArray.entrySet()){
            if (url.startsWith(entry.getKey())){
                return entry.getValue().resolveUrl(url.substring(entry.getKey().length()));
            }
        }
        return url;
    }
}
