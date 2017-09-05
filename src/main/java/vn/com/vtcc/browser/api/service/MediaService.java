package vn.com.vtcc.browser.api.service;

import org.springframework.stereotype.Service;
import vn.com.vtcc.browser.api.config.ProductionConfig;
import vn.com.vtcc.browser.api.utils.UrlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by giang on 20/06/2017.
 */
@Service
public class MediaService {
    private static final String YOUTUBE_API = "https://www.youtube.com/get_video_info?video_id=";
    private static final String MEDIA_PATH = "/home/vbrowser/sample_video";
    private static final String VIDEO_EXT = ".mp4";
    private static final String AUDIO_EXT = ".mp3";

    public void streamingVideo(String input, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            //String parsedUrl = UrlUtils.convertToURLEscapingIllegalCharacters(input);
            String path = MEDIA_PATH + input + VIDEO_EXT;
            if (path != null) {
                Path contentPath = Paths.get(path);
                MultipartFileSender.fromPath(contentPath).with(request).with(response).serveResource();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
