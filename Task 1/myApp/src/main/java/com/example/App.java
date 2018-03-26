package com.example;

import java.util.Properties;
    
import java.io.IOException;
    
import java.util.Map;

import java.util.HashMap;
    
import java.util.LinkedHashMap;
    
import fi.iki.elonen.*;
    
import java.util.UUID;
    
import java.util.*;
    
import java.util.Iterator;

import java.io.File;

import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.io.IOException;

import java.io.*;

import java.nio.file.*;

    public class App extends NanoHTTPD {

        static LinkedHashMap<String,messageBody> queue1 = new LinkedHashMap<String,messageBody>();
        static LinkedHashMap<String,messageBody> queue2 = new LinkedHashMap<String,messageBody>();
        static LinkedHashMap<String,messageBody> queue3 = new LinkedHashMap<String,messageBody>();
        static HashMap<String, LinkedHashMap<String,messageBody>> mainHashMap = new HashMap<String, LinkedHashMap<String,messageBody>>();
        public App() throws IOException {
            super(8080);
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
        }
    
        public static void main(String[] args) {
        	mainHashMap.put("queue1",queue1);
        	mainHashMap.put("queue2",queue2);
        	mainHashMap.put("queue3",queue3);
            try {
                new App();
            } catch (IOException ioe) {
                System.err.println("Couldn't start server:\n" + ioe);
            }
        }
    
        @Override
        public Response serve(IHTTPSession session) {
        	Map<String, String> headers = session.getHeaders(); //gets the headers
            Map<String, String> parms = session.getParms(); //gets the parameters
            Method method = session.getMethod(); //gets the methods - GET - PUT - DELETE
            String uri = session.getUri(); //gets the url
            Map<String, String> files = new HashMap<String,String>(); //creates hashmap of content and temp file
            String[] uriComponents = uri.split("/"); //gets the uri components
            if(Method.PUT.equals(method))
            {
            	try
            	{
            		session.parseBody(files); //getting the put body into the temp file
            		if(uriComponents[1].equals("queue")&&mainHashMap.containsKey(uriComponents[2]))
                    {
            	        String toPutInQueue = ""; //string to be inserted in the queue
                        String tmpFilePath = files.get("content"); //gets the address of temporary file 
                        try
                        {      
                 	        String content = new String(Files.readAllBytes(Paths.get(tmpFilePath))); //converts the components of temp file into string
                 	        toPutInQueue = content; //copy string
                        }
                        catch(IOException ioe)
                        {
                 	        ioe.printStackTrace();
                 	        System.out.println("Error : Copying from temp file/accessing temp file");
                        }
                        messageBody messagebody = new messageBody(toPutInQueue, 0); 
                        mainHashMap.get(uriComponents[2]).put(UUID.randomUUID().toString().replace("-", ""), messagebody);
                        if(mainHashMap.get(uriComponents[2]).isEmpty())
                        	System.out.println("Queue is empty");
                        else
                        {
                        	System.out.println("Entries in "+uriComponents[2]+" are");
                        	for(Map.Entry<String, messageBody> entry : mainHashMap.get(uriComponents[2]).entrySet())
                        	{
                        		System.out.println("ID : "+entry.getKey() + " Message Body : " + entry.getValue().messageText.toString());
                        	}
                        }
                        String response = "Success : Entry created successfully";
                        return newFixedLengthResponse(NanoHTTPD.Response.Status.CREATED,"text/plain", response);
                    }
                    else
                    {
                     	String response = "Error : Given queue name doesn't exits. Usage:- PUT/queue/<queue_name>";
                        return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,"text/plain", response);
                    }
            	}
            	catch(IOException ioe)
            	{
            		return newFixedLengthResponse("Internal Error IO Exception: " + ioe.getMessage());
            	}
            	catch(ResponseException re)
            	{
            		return newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
            	}

            }
            else if(Method.GET.equals(method))
            {
            	if(uriComponents[1].equals("queue")&&mainHashMap.containsKey(uriComponents[2]))
            	{
            		if(mainHashMap.get(uriComponents[2]).isEmpty())
            		{
            			String response = "Error : trying to access an empty queue";
            			return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,"text/plain",response);
            		}
            		else
            		{
            			StringBuffer sb = new StringBuffer("");
            			Map.Entry<String,messageBody> entry = mainHashMap.get(uriComponents[2]).entrySet().iterator().next();
            			String key = entry.getKey();
            			String value = entry.getValue().messageText.toString();
            			sb.append("ID : ");
            			sb.append(key);
            			sb.append(" Message Body : ");
            			sb.append(value);
            			System.out.println("ID : "+key+" Messgae Body : "+value);
            			String response = String.valueOf(sb);
            			return newFixedLengthResponse(NanoHTTPD.Response.Status.OK,"text/plain",response);
            		}
            	}
            	else
            	{
            		String response = "Error : Given queue name doesn't exits. Usage:- GET/queue/<queue_name>";
            		return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,"text/plain", response);
            	}            	
            }
            else if(Method.DELETE.equals(method))
            {
            	if(uriComponents[1].equals("queue")&&mainHashMap.containsKey(uriComponents[2]))
            	{
            		if(mainHashMap.get(uriComponents[2]).isEmpty())
            		{
            			String response = "Error : trying to delete from an empty queue";
            			return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,"text/plain",response);
            		}
            		else
            		{
            			Map.Entry<String,messageBody> entry = mainHashMap.get(uriComponents[2]).entrySet().iterator().next();
            			String key = entry.getKey();
            			mainHashMap.get(uriComponents[2]).remove(key);
            			if(mainHashMap.get(uriComponents[2]).isEmpty())
                        	System.out.println("Queue is empty");
                        else
                        {
                        	System.out.println("Entries in "+uriComponents[2]+" are");
                        	for(Map.Entry<String, messageBody> entryIterator : mainHashMap.get(uriComponents[2]).entrySet())
                        	{
                        		System.out.println("ID : "+entryIterator.getKey() + " Message Body : " + entryIterator.getValue().messageText.toString());
                        	}
                        }
            			String response = "Success : Entry deleted from queue";
            			return newFixedLengthResponse(NanoHTTPD.Response.Status.OK,"text/plain",response);
            		}
            	}
            	else
            	{
            		String response = "Error : Given queue name doesn't exits. Usage:- DELETE/queue/<queue_name>";
            		return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,"text/plain",response);
            	}
            }
            String response = "Error : Use only GET/queue/<queue_name> | PUT/queue/<queue_name> | DELETE/queue/<queue_name>";
            return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,"text/plain", response);
        }
    }