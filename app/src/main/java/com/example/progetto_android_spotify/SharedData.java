package com.example.progetto_android_spotify;


import java.util.HashMap;
import java.util.Map;

public class SharedData {

    public static TCPConnection tcp_conn;

    public static Map<String, String> songs_path = new HashMap<String, String>();

    public static String USERNAME;
    public static String PASSWORD;

    public static String response;

    public static boolean isLoggedIn;

    public static  String ACCESS_TOKEN;
    public static String REFRESH_TOKEN;
    public static String REVOKE_TOKEN;

    public static final int SIGN_IN_REQUEST = 1;
    public static final int SIGN_UP_REQUEST = 2;
    public static final int PLAYLIST_CONTENT_REQUEST = 3;
    public static final int SONGS_DATA_REQUEST = 4;
    public static final int SONG_SEARCH_REQUEST = 5;
    public static final int PLAYLIST_SEARCH_REQUEST = 6;
    public static final int ARTIST_SEARCH_REQUEST = 7;
    public static final int USER_SEARCH_REQUEST = 8;
    public static final int SONG_IN_PLAYLIST_SEARCH_REQUEST = 9;
    public static final int ADD_TO_PLAYLIST_REQUEST = 10;
    public static final int ADD_TO_LIKED_SONGS_REQUEST = 11;
    public static final int REFRESH_TOKEN_REQUEST = 12;
    public static final int REVOKE_TOKEN_REQUEST = 13;
    public static final int CREATE_PLAYLIST_REQUEST = 14;

    public static final int TCP_OPERATION_SUCCESSFUL = 100;
    public static final int SOCKET_CREATION_ERROR_CODE = 101;
    public static final int INPUT_STREAM_CREATION_ERROR_CODE = 102;
    public static final int OUTPUT_STREAM_CREATION_ERROR_CODE = 103;
    public static final int SEND_MESSAGE_ERROR_CODE = 104;
    public static final int RECEIVE_MESSAGE_ERROR_CODE = 105;

    public static final int DATA_STRUCTURE_OPERATION_SUCCESSFUL = 200;
    public static final int ADDITIONAL_FIELD_DOESNT_EXISTS_ERROR_CODE = 201;


}



