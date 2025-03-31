package com.example.progetto_android_spotify;

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
        this.port = port;
        this.address = address;

        this.additional_request_fields = new HashMap<String, String>();
        additional_request_fields.put("Playlist name", "");
        additional_request_fields.put("Arist name", "");
        additional_request_fields.put("User name", "");
        additional_request_fields.put("Song name", "");
    }

    public void set_for(int job){
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

        if( connect() != SharedData.TCP_OPERATION_SUCCESSFUL ){
            return;
        }

        this.running = true;

        while( this.running ){

            if( !this.new_job )
                continue;

            switch ( this.job_type ){

                default:
                    this.running = false;

                case SharedData.SIGN_IN_REQUEST:
                    String sign_in_request_message = "request_code:"+SharedData.SIGN_IN_REQUEST+";username:"+SharedData.USERNAME+";access_token:"+SharedData.PASSWORD+"\n";
                    if( send(sign_in_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                    }
                    break;

                case SharedData.SIGN_UP_REQUEST:
                    String sign_up_request_message = "request_code:"+SharedData.SIGN_UP_REQUEST+";email:"+SharedData.EMAIL+";username:"+SharedData.USERNAME+";password:"+SharedData.PASSWORD+"\n";
                    if( send(sign_up_request_message) == SharedData.SEND_MESSAGE_ERROR_CODE ){
                        this.running = false;
                    }
                    break;

                case SharedData.PLAYLIST_CONTENT_REQUEST:
                    String playlist_name = this.additional_request_fields.getOrDefault("Playlist name", null);
                    if( playlist_name == null){
                        this.error_message = "The name of the playlist was not set in the Addition_Fields. Setting it as a empty string";
                        this.additional_request_fields.replace("Playlist name", "");
                        break;
                    }
                    String playlist_content_request_message  ="request_code:"+SharedData.PLAYLIST_CONTENT_REQUEST+";username:"+SharedData.USERNAME+";access_token:"+SharedData.ACCESS_TOKEN+";playlist_name:"+playlist_name;
                    break;

                case SharedData.SONGS_DATA_REQUEST:
                    break;

                case SharedData.SONG_SEARCH_REQUEST:
                    String song_name = this.additional_request_fields.getOrDefault("Song name", null);
                    if( song_name == null){
                        this.error_message = "The name of the playlist was not set in the Additional_Fields. Setting it as a empty string";
                        this.additional_request_fields.replace("Song name", "");
                        break;
                    }
                    break;

                case SharedData.PLAYLIST_SEARCH_REQUEST:
                    break;

                case SharedData.ARTIST_SEARCH_REQUEST:
                    break;

                case SharedData.USER_SEARCH_REQUEST:
                    break;

                case SharedData.SONG_IN_PLAYLIST_SEARCH_REQUEST:
                    break;

                case SharedData.ADD_TO_PLAYLIST_REQUEST:
                    break;

                case SharedData.ADD_TO_LIKED_SONGS_REQUEST:
                    break;

                case SharedData.REFRESH_TOKEN_REQUEST:
                    break;

                case SharedData.REVOKE_TOKEN_REQUEST:
                    break;
            }
        }
    }

    private int send(String request){

        this.send.print(request);
        if( this.send.checkError() ) {
            this.error_message = "Unknown";
            return SharedData.SEND_MESSAGE_ERROR_CODE;
        }

        return SharedData.TCP_OPERATION_SUCCESSFUL;

    }

    private int connect(){
        try{
            this.socket = new Socket( this.address, this.port);
        }catch( IOException ex){
            this.error_message = ex.getMessage();
            return SharedData.SOCKET_CREATION_ERROR_CODE;
        }

        try{
            this.input = this.socket.getInputStream();
        }catch( IOException ex ){
            this.error_message = ex.getMessage();
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
