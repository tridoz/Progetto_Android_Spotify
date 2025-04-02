package com.example.progetto_android_spotify;

import android.util.Log;

import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


public class TCPConnection extends Thread{
    private final int port ;
    private final String address;

    private String error_message;

    private Map<String, String> additional_request_fields;
    private Map<String, String> response_fields;


    private Socket socket;

    private InputStream input;
    private BufferedReader recv;
    private OutputStream output;
    private PrintWriter send;

    private int job_type;

    private ArrayList<String> songs_patt;

    private boolean new_job;
    private boolean running;


    public TCPConnection(int port, String address){
        Log.d("TCPConnection", "TCPConnection created with port: " + port + " and address: " + address);
        this.port = port;
        this.address = address;

        this.additional_request_fields = new HashMap<String, String>();
        additional_request_fields.put("Playlist name", "");
        additional_request_fields.put("Arist name", "");
        additional_request_fields.put("User name", "");
        additional_request_fields.put("Song name", "");

        this.response_fields = new HashMap<String, String>();
        this.response_fields.put("response_code", "0");
        this.response_fields.put("access_token", "0");
        this.response_fields.put("refresh_token", "0");
        this.response_fields.put("revoke_token", "0");
        this.response_fields.put("songs", "0");
        this.response_fields.put("playlists", "0");
        this.response_fields.put("artists", "0");
        this.response_fields.put("users", "0");


    }

    public void set_job_for(int job){
        this.job_type = job;
        this.new_job = true;
    }

    public int set_additional_request_field(String key, String value){
        if( this.additional_request_fields.containsKey(key) ){
            this.additional_request_fields.replace(key, value);
            return SharedData.DATA_STRUCTURE_OPERATION_SUCCESSFUL;
        }else{
            this.error_message = "Provided field <"+key+"> is an invalid field\n";
            return SharedData.ADDITIONAL_FIELD_DOESNT_EXISTS_ERROR_CODE;
        }
    }

    @Override
    public void run(){
        Log.d("TCPConnection", "Thread started");
        if( connect() != SharedData.TCP_OPERATION_SUCCESSFUL ){
            Log.e("TCPConnection", "Connection failed: " + this.error_message);
            return;
        }

        this.running = true;
        Log.d("TCPConnection", "Connection successful, running...");

        while( this.running ){

            if( !this.new_job )
                continue;

            this.new_job = false;
            Log.i("TCP Client", "Creazione avvenuta con successo");

            switch ( this.job_type ){

                default:
                    this.running = false;

                case SharedData.SIGN_IN_REQUEST:
                    String sign_in_request_message = "request_code:"+SharedData.SIGN_IN_REQUEST+";username:"+SharedData.USERNAME+";password:"+SharedData.PASSWORD+"\n";
                    if( send(sign_in_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                        Log.d("TCPConnection", "Sending message error");
                    }
                    Log.d("TCPConnection", "Message sending successful");
                    if( recv() == SharedData.RECEIVE_MESSAGE_ERROR_CODE ){
                        this.running = false;
                        Log.d("TCPConnection", "Receiving message error => " + this.error_message);
                        break;
                    }

                    String[] sign_in_fields = SharedData.response.split(";");

                    for( String sign_in_field : sign_in_fields){
                        Log.d("Debug Stringa", sign_in_field);
                        String[] sub_fields = sign_in_field.split(":");
                        Log.d("Sebug Stringa", String.valueOf(sub_fields.length));
                        String sign_in_key = sub_fields[0];
                        String sign_in_value = sub_fields[1];
                        this.response_fields.replace(sign_in_key, sign_in_value);
                    }

                    int sign_in_response_code = Integer.parseInt(this.response_fields.get("response_code"));
                    if( sign_in_response_code == 200 ){
                        SharedData.ACCESS_TOKEN = this.response_fields.get("access_token");
                        SharedData.REFRESH_TOKEN = this.response_fields.get("refresh_token");
                        SharedData.REVOKE_TOKEN = this.response_fields.get("revoke_token");
                        SharedData.isLoggedIn = true;
                    }else {
                        SharedData.isLoggedIn = false;
                    }

                    break;

                case SharedData.SIGN_UP_REQUEST:
                    String sign_up_request_message = "request_code:" + SharedData.SIGN_UP_REQUEST + ";username:" + SharedData.USERNAME + ";password:" + SharedData.PASSWORD + "\n";
                    if (send(sign_up_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE) {
                        this.running = false;
                    }

                    if (recv() == SharedData.RECEIVE_MESSAGE_ERROR_CODE) {
                        this.running = false;
                        Log.d("TCPConnection", "Receiving message error => " + this.error_message);
                    }

                    String[] sign_up_fields = SharedData.response.split(":");
                    String sign_up_key = sign_up_fields[0];
                    String sign_up_value = sign_up_fields[1];
                    this.response_fields.replace(sign_up_key, sign_up_value);

                    break;

                case SharedData.PLAYLIST_CONTENT_REQUEST:
                    SharedData.songs_path.clear();

                    String playlist_name = this.additional_request_fields.getOrDefault("Playlist name", null);
                    if( playlist_name == null){
                        this.error_message = "The name of the playlist was not set in the Addition_Fields. Setting it as a empty string";
                        this.additional_request_fields.replace("Playlist name", "");
                        break;
                    }
                    String playlist_content_request_message  ="request_code:"+SharedData.PLAYLIST_CONTENT_REQUEST+";username:"+SharedData.USERNAME+";access_token:"+SharedData.ACCESS_TOKEN+";playlist_name:"+playlist_name+"\n";
                    if( send(playlist_name) == SharedData.SEND_MESSAGE_ERROR_CODE  ){
                        this.running = false;
                    }
                    break;

                case SharedData.SONGS_DATA_REQUEST:
                    for( String key: SharedData.songs_path.keySet() ){
                       String song_path = SharedData.songs_path.get(key);
                       String song_data_request_message = "request_code:"+SharedData.SONGS_DATA_REQUEST+";username:"+SharedData.USERNAME+";access_token:"+SharedData.ACCESS_TOKEN+";song_path:"+song_path+"\n";
                       if( send(song_data_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                           this.running = false;
                       }
                    }
                    break;

                case SharedData.SONG_SEARCH_REQUEST:
                    String song_name = this.additional_request_fields.getOrDefault("Song name", null);
                    if( song_name == null){
                        this.error_message = "The name of the playlist was not set in the Additional_Fields. Setting it as a empty string";
                        this.additional_request_fields.replace("Song name", "");
                        break;
                    }
                    String song_search_request_message = "request_code"+SharedData.SONG_SEARCH_REQUEST+";username:"+SharedData.USERNAME+";access_token:"+SharedData.ACCESS_TOKEN+";song_name:"+song_name+"\n";
                    if( send(song_search_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                    }
                    break;

                case SharedData.PLAYLIST_SEARCH_REQUEST:
                    String playlist_search_name = this.additional_request_fields.getOrDefault("Playlist name", null);
                    if( playlist_search_name == null){
                        this.error_message = "The name of the playlist was not set in the Additional_Fields. Setting it as an empty string";
                        this.additional_request_fields.replace("Playlist name", "");
                        break;
                    }

                    String playlist_search_request_message = "request_code:"+SharedData.PLAYLIST_SEARCH_REQUEST+";username:"+SharedData.USERNAME+";access_token"+SharedData.ACCESS_TOKEN+";playlist_name"+playlist_search_name+"\n";
                    if( send(playlist_search_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                    }
                    break;

                case SharedData.ARTIST_SEARCH_REQUEST:
                    String artist_name = this.additional_request_fields.getOrDefault("Artist name", null);
                    if( artist_name == null){
                        this.error_message = "The name of the artist was not set in the Additional_Fields. Setting in as an empty string";
                        this.additional_request_fields.replace("Playlist name", "");
                        break;
                    }

                    String artist_search_request_message = "request_code:"+SharedData.ARTIST_SEARCH_REQUEST+";username:"+SharedData.USERNAME+";access_token:"+SharedData.ACCESS_TOKEN+";artist_name:"+artist_name+"\n";
                    if( send(artist_search_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                    }
                    break;

                case SharedData.USER_SEARCH_REQUEST:
                    String user_name = this.additional_request_fields.getOrDefault("User name", null);
                    if( user_name == null ){
                        this.error_message = "The user name was not set in the Additional_Fields. Setting it as an empty string";
                        this.additional_request_fields.replace("User name", "");
                        break;
                    }

                    String user_search_request_message = "request_code:"+SharedData.USER_SEARCH_REQUEST+";username:"+SharedData.USERNAME+";access_token:"+SharedData.ACCESS_TOKEN+";user_name"+user_name+"\n";
                    if( send(user_search_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                    }
                    break;

                case SharedData.SONG_IN_PLAYLIST_SEARCH_REQUEST:
                    String song_playlist_name = this.additional_request_fields.getOrDefault("Song name", null);
                    if( song_playlist_name == null){
                        this.error_message = "The song name was not set in the Additional_Fields. Setting it as an empty string";
                        this.additional_request_fields.replace("User name", "");
                        break;
                    }

                    String song_playlist_search_request_message = "request:"+SharedData.SONG_IN_PLAYLIST_SEARCH_REQUEST+";username:"+SharedData.USERNAME+";access_token:"+SharedData.ACCESS_TOKEN+";song_name"+song_playlist_name+"\n";
                    if( send(song_playlist_search_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                    }
                    break;

                case SharedData.ADD_TO_PLAYLIST_REQUEST:
                    String song_to_add_name = this.additional_request_fields.getOrDefault("Song name", null);
                    String playlist_where_add_name = this.additional_request_fields.getOrDefault("Playlist name", null);
                    if( song_to_add_name == null ){
                        this.error_message = "The song name was not set in the Additional_Fields. Setting it as an empty string";
                        this.additional_request_fields.replace("Song name", "");
                        break;
                    }

                    if( playlist_where_add_name == null){
                        this.error_message = "The playlist name was not set in the Additional_Fields. Setting it as an empty string";
                        this.additional_request_fields.replace("Playlist name", "");
                        break;
                    }

                    String add_song_to_playlist_request_message = "request_code:"+SharedData.ADD_TO_PLAYLIST_REQUEST+";username:"+SharedData.USERNAME+";access_token:"+SharedData.ACCESS_TOKEN+";playlist_name:"+playlist_where_add_name+";song_name"+song_to_add_name+"\n";
                    if( send(add_song_to_playlist_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                    }
                    break;

                case SharedData.ADD_TO_LIKED_SONGS_REQUEST:
                    String liked_song_name = additional_request_fields.getOrDefault("Song name", null);
                    if( liked_song_name == null ){
                        this.error_message = "The song name was not set in the Additional_Fields. Setting it as an empty string";
                        this.additional_request_fields.replace("Song name", "");
                        break;
                    }

                    String add_song_to_liked_request_message = "request_code:"+SharedData.ADD_TO_LIKED_SONGS_REQUEST+";username"+SharedData.USERNAME+";access_token:"+SharedData.ACCESS_TOKEN+";song_name"+liked_song_name+"\n";
                    if( send(add_song_to_liked_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                    }

                    break;

                case SharedData.REFRESH_TOKEN_REQUEST:
                    String refresh_token_request_message = "request_code:"+SharedData.REFRESH_TOKEN_REQUEST+";username:"+SharedData.USERNAME+";refresh_token"+SharedData.REFRESH_TOKEN+"\n";
                    if( send(refresh_token_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                    }
                    break;

                case SharedData.REVOKE_TOKEN_REQUEST:
                    String revoke_token_request_message = "request_code:"+SharedData.REVOKE_TOKEN_REQUEST+";username:"+SharedData.USERNAME+";refresh_token"+SharedData.REVOKE_TOKEN+"\n";
                    if( send(revoke_token_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                    }
                    break;

                case SharedData.CREATE_PLAYLIST_REQUEST:
                    String playlist_to_create_name = this.additional_request_fields.getOrDefault("Playlist name", null);
                    if( playlist_to_create_name == null){
                        this.error_message = "The playlist name was not set in the Additional_Fields. Setting it as an empty string";
                        this.additional_request_fields.replace("Playlist name", "");
                        break;
                    }

                    String create_playlist_request = "request_code:"+SharedData.CREATE_PLAYLIST_REQUEST+";username:"+SharedData.USERNAME+";access_token:"+SharedData.ACCESS_TOKEN;
                    if( send(create_playlist_request) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                    }
                    break;
            }
        }
    }

    private int send(String request){
        Log.i("Message Send", request);
        this.send.print(request);
        if( this.send.checkError() ) {
            this.error_message = "Unknown";
            return SharedData.SEND_MESSAGE_ERROR_CODE;
        }

        return SharedData.TCP_OPERATION_SUCCESSFUL;

    }

    private int recv(){
        String response;
        try {
            response = recv.readLine();
        }catch(IOException ex){
            this.error_message = "Receiving the response from the server caused an exception: " + ex.getMessage();
            return SharedData.RECEIVE_MESSAGE_ERROR_CODE;
        }
        Log.d("TCPConnection", "Received_Message: " + response);
        SharedData.response = response;
        return SharedData.TCP_OPERATION_SUCCESSFUL;
    }

    public int getAnswer(){
        return Integer.parseInt( this.response_fields.get("response_code") );
    }

    private int connect(){
        try{
            this.socket = new Socket( this.address, this.port);
            Log.d("TCPConnection", "Socket connected");
        }catch( IOException ex){
            this.error_message = ex.getMessage();
            return SharedData.SOCKET_CREATION_ERROR_CODE;
        }

        try{
            this.input = this.socket.getInputStream();
            Log.d("TCPConnection", "Input stream created");
        }catch( IOException ex ){
            this.error_message = ex.getMessage();
            Log.d("TCPConnection", "Output stream created");
            return SharedData.INPUT_STREAM_CREATION_ERROR_CODE;
        }

        try{
            this.output = this.socket.getOutputStream();
        }catch( IOException ex){
            this.error_message = ex.getMessage();
            return SharedData.OUTPUT_STREAM_CREATION_ERROR_CODE;
        }

        this.recv = new BufferedReader( new InputStreamReader(input) );
        this.send = new PrintWriter( output, true);

        return SharedData.TCP_OPERATION_SUCCESSFUL;
    }

}
