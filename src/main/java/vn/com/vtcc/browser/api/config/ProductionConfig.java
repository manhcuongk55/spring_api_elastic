package vn.com.vtcc.browser.api.config;

/**
 * Created by giang on 5/27/17.
 */
public class ProductionConfig {
    public static final int RESPONE_STATAUS_OK = 200;
    public static String ES_SERVER = System.getProperty("es_server");
    public static final String URL_ELASTICSEARCH =  "http://192.168.107.231:9200/br_article_v4/_search?";
    public static final String ES_INDEX_NAME = "br_article_v4";
    public static final String ES_INDEX_TYPE = "article";
    public static final String URL_GOOGLE = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=";

    public static final String STATUS_DISPLAY = "1";
    //public static final String HOST_NAME = "http://news.safenet.vn/";
    public static final String MEDIA_HOST_NAME = "http://media.sfive.vn/";
    public static final String REDIS_KEY = "HOT_TAGS";
    public static final String REDIS_KEY_IOS = "HOT_TAGS_IOS";
    public static final String USER_AGENT = "Mozilla/5.0 (X11; CrOS x86_64 8172.45.0) AppleWebKit/537.36" +
            " (KHTML, like Gecko) Chrome/51.0.2704.64 Safari/537.36";

    public static final int REDIS_PORT = 3001;
    public static final int ES_TRANSPORT_PORT = 9300;

    public static final String[] REDIS_HOST_STAGING = {"192.168.107.201", "192.168.107.202", "192.168.107.203",
                                                        "192.168.107.204", "192.168.107.205", "192.168.107.206"};

    public static final String[] REDIS_HOST_PRODUCTION = {"10.240.152.63", "10.240.152.64", "10.240.152.65",
                                                        "10.240.152.66", "10.240.152.67", "10.240.152.68"};

    public static final String[] ES_HOST_PRODUCTION = {"10.240.152.69", "10.240.152.70","10.240.152.71","10.240.152.72",
    "10.240.152.73","10.240.152.74","10.240.152.75","10.240.152.76","10.240.152.77","10.240.152.78"};

    public static final String[] ES_HOST_STAGING = {"192.168.107.231", "192.168.107.232","192.168.107.233","192.168.107.234",
                                                    "192.168.107.235","192.168.107.236"};

    public static final String WHITELIST_SOURCE_MYSQL = "'tiin.vn','netnews.vn','moison.vn','songkhoe.vn','news.zing.vn'," +
            "'kenh14.vn','cand.com.vn','ictnews.vn','soha.vn','doisongphapluat.com','vietnamplus.vn','congluan.vn'," +
            "'infonet.vn','baodatviet.vn','daidoanket.vn','vuathethao.vn','thegioitre.vn','cafef.vn','cafebiz.vn'," +
            "'thoibaonganhang.vn','nongnghiep.vn','baohaiquan.vn','doanhnhansaigon.vn','enternews.vn','baobaovephapluat.vn'," +
            "'qdnd.vn','baodauthau.vn','thoibaotaichinhvietnam.vn','congan.com.vn','sggp.org.vn','plo.vn'," +
            "'baodautu.vn','vtv.vn','khampha.vn','autopro.com.vn','gamek.vn','suckhoedoisong.vn','saostar.vn'";

    public static final String WHITELIST_SOURCE_ES = "tiin.vn,netnews.vn,moison.vn,songkhoe.vn,news.zing.vn," +
            "kenh14.vn,cand.com.vn,ictnews.vn,soha.vn,doisongphapluat.com,vietnamplus.vn,congluan.vn," +
            "infonet.vn,baodatviet.vn,daidoanket.vn,vuathethao.vn,thegioitre.vn,cafef.vn,cafebiz.vn," +
            "thoibaonganhang.vn,nongnghiep.vn,baohaiquan.vn,doanhnhansaigon.vn,enternews.vn,baobaovephapluat.vn," +
            "qdnd.vn,baodauthau.vn,thoibaotaichinhvietnam.vn,congan.com.vn,sggp.org.vn,plo.vn," +
            "baodautu.vn,vtv.vn,khampha.vn,autopro.com.vn,gamek.vn,suckhoedoisong.vn,saostar.vn";
}
