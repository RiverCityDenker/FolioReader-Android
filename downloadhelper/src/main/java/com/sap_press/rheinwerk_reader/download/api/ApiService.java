package com.sap_press.rheinwerk_reader.download.api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ApiService {

    @Headers({
            "file_path: /OEBPS/content.opf"
    })
    @Streaming
    @GET("ebooks/{ebookid}/download")
    Observable<ResponseBody> download(@Path("ebookid") String ebookId,
                                      @Header("Authorization") String authorization,
                                      @Header("app_version") String appVersion,
                                      @Query("app_version") String appVersionParam,
                                      @Query("file_path") String filePath);
}
