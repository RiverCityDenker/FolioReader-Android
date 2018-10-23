package com.sap_press.rheinwerk_reader.download.api.network;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by hale on 8/29/2018.
 */
public final class HostSelectionInterceptor implements Interceptor {
    private String host;
    private String scheme;
    private static HostSelectionInterceptor instant;

    public HostSelectionInterceptor() {
    }

    public void setInterceptor(String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        scheme = httpUrl.scheme();
        host = httpUrl.host();
    }

    public static HostSelectionInterceptor getInstant() {
        if (instant != null) {
            return instant;
        } else {
            instant = new HostSelectionInterceptor();
            return instant;
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // If new Base URL is properly formatted then replace the old one
        if (scheme != null && host != null) {
            HttpUrl newUrl = request.url().newBuilder()
                    .scheme(scheme)
                    .host(host)
                    .build();
            request = request.newBuilder()
                    .url(newUrl)
                    .build();
        }
        Request.Builder builder = request.newBuilder().header("x-project", "sap-press");
        Request newRequest = builder.build();
        return chain.proceed(newRequest);
    }
}