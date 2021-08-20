package com.company;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Main {

    HttpResponse response;
    HttpClient client;
    HttpRequest request;
    JSONArray repos;
    JSONObject repo;
    Map<Integer,Integer> JSONMap = new HashMap<>();
    Map<Integer, Integer> result = new LinkedHashMap<>();
    ArrayList<String> names = new ArrayList<>();
    WriteToCSV fork_wcsv = new WriteToCSV();
    WriteToCSV contributes_wcsv = new WriteToCSV();
    String organization_name =getOrgName();
    String forkedFilePath = organization_name+"_repos.csv";
    String contributesFilePath = organization_name+"_user.csv";

    int count =0;
    int followersExtract;



    public static void main(String[] args) {


        new Main().init();

    }
    private void init()
    {
        fork_wcsv.init(forkedFilePath);
        contributes_wcsv.init(contributesFilePath);
        repoHttpRequest(organization_name);

    }



    private void repoHttpRequest(String repo_url) {
        client = HttpClient.newHttpClient();

        client.newBuilder().version(Version.HTTP_1_1);
        request = HttpRequest.newBuilder().uri(URI.create("https://api.github.com/orgs/"+repo_url+"/repos")).build();

        try {
            response =  client.send(request,HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        System.out.println(response.body().toString());
        findMostForked(response.body().toString());


    }
    private void findMostForked(String responseBody) {
        int largestForkedValue = 0;
        int largestForkedRepoId = 0;

        // Catching the limit exceed error.
        if(responseBody.contains("exceeded") || responseBody.contains("limit")) {
            System.out.println("limit exceeded");
        }
        else {
            repos = new JSONArray(responseBody);
            for(int i=0;i<repos.length();i++) {
                repo = repos.getJSONObject(i);
                int forks = repo.getInt("forks");
                if(forks>largestForkedValue){
                    largestForkedValue = forks;

                }
                if (forks>400) {

                    JSONMap.put(i,forks);
                }

            }
            largestForkedValue = getMaxValue(false);
            largestForkedRepoId = getMaxValue(true);

            System.out.println("this is the number of most forked project : "+largestForkedValue);


            List<Entry<Integer, Integer>> list = new ArrayList<>(JSONMap.entrySet());
            list.sort(Entry.comparingByValue());

            for (Entry<Integer, Integer> entry : list) {
                result.put(entry.getKey(), entry.getValue());
            }
            String header1 = String.format("%-8.8s", "id");
            String header2 = String.format("%-8.8s", "Fork");
            String header3 = String.format("%-40.40s","Repo_Name");
            String header4 = String.format("%-64.64s","URL");
            String header5 = String.format("%-128.128s","Description");
            fork_wcsv.write(header1+""+header2+""+header3+""+header4+""+header5);
            fork_wcsv.write("");

            //Creating CSV File
            for (Map.Entry<Integer, Integer> entry : result.entrySet()) {

                JSONObject proje = repos.getJSONObject(entry.getKey());
                String repo_name = String.format("%-40.40s",proje.getString("full_name"));
                String description = String.format("%-128.128s", proje.getString("description"));
                String url = String.format("%-64.64s", proje.getString("html_url"));
                String entryKey = String.format("%-8.8s", entry.getKey());
                String entryVal = String.format("%-8.8s", entry.getValue());
                String contributors_url = proje.getString("contributors_url");
                names.add(repo_name);
                fork_wcsv.write(entryKey+""+entryVal+""+repo_name+""+url+""+description);
                contributesHttpRequest(contributors_url);



            }

            fork_wcsv.close();
            contributes_wcsv.close();
        }

    }
    private int getMaxValue(boolean key)
    {
        Entry<Integer, Integer> maxEntry = Collections.max(JSONMap.entrySet(), new Comparator<>() {
            public int compare(Entry<Integer, Integer> e1, Entry<Integer, Integer> e2) {

                return e1.getValue().compareTo(e2.getValue());
            }
        });
        return key ? maxEntry.getKey() : maxEntry.getValue();

    }
    private void contributesHttpRequest(String contributes_url) {



        HttpClient client = HttpClient.newHttpClient();

        client.newBuilder().version(Version.HTTP_1_1);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(contributes_url)).GET().timeout(Duration.ofMillis(20000)).build();
        // HttpResponse response = null;
        try {
            response = client.send(request,HttpResponse.BodyHandlers.ofString());
        }catch(java.net.http.HttpConnectTimeoutException e)
        {
            System.out.println("Connection Timeout!");
        } catch(java.net.ConnectException e)
        {
            System.out.println("Connection Timeout!");
        }catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Exceptttt");
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        findMostContributes(response.body().toString());

    }
    private void findMostContributes(String responseBody){

        // Catching the limit exceed error.
        if(responseBody.contains("exceeded") || responseBody.contains("limit")) {
            System.out.println("limit exceeded");
        }
        else {
            String header1 = String.format("%-64.64s", "Repository Name");
            String header2 = String.format("%-64.64s", "Username");
            String header3 = String.format("%-32.32s","Contribution Quantity");
            String header4 = String.format("%-32.32s","Users Follower Quantity");

            contributes_wcsv.write(header1+""+header2+""+header3+""+header4);
            contributes_wcsv.write("");
            JSONArray contributes = new JSONArray(responseBody);

            for(int j=count;j<count+1;j++){
                for(int i=0;i<5;i++){

                    JSONObject contribute = contributes.getJSONObject(i);

                    String username = contribute.getString("login");
                    int contributions = contribute.getInt("contributions");
                    //Create URL for follower quantity
                    String url = "https://api.github.com/users/" +username;
                    followerHttpRequest(url);
                    String repo_name = String.format("%-64.64s", names.get(j));
                    String login = String.format("%-64.64s",username);
                    String contribs = String.format("%-32.32s",contributions);
                    String followers = String.format("%-32.32s",""+followersExtract);
                    // System.out.println("Repo Name : " + names.get(j)+ " | " + "Login : " + " | " + username + "             | " +"Count of contribute: "+" | "+contributions+" follower: "+followersExtract);
                    contributes_wcsv.write(repo_name+""+login+""+contribs+""+followers);
                }

            }


            count +=1;
        }




    }

    private void findFollowerQuantity(String responseBody){

        String temp = responseBody;
        temp = "["+temp+"]";

        if(temp.charAt(temp.indexOf("\"followers\":")+12) == ',' || temp.charAt(temp.indexOf("\"followers\":")+12) == ' ')
        {
            temp = temp.substring(0, temp.indexOf("\"followers\":")+12) + "0" + temp.substring(temp.indexOf("\"followers\":")+13, temp.length());
        }
        System.out.println(temp);
        JSONArray profile = new JSONArray(temp);
        JSONObject follower_info = profile.getJSONObject(0);
        int follower_quantity = follower_info.getInt("followers");
        followersExtract = follower_quantity;
        System.out.println("-------Follower Quantity is :--------------" +follower_quantity);




    }
    private String getOrgName(){
        Scanner s = new Scanner(System.in);
        String orgname;
        System.out.println("Enter organization name : ");
        orgname = s.nextLine();
        return orgname;
    }
    private void followerHttpRequest(String userprofile_url){
        client = HttpClient.newHttpClient();
        client.newBuilder().version(Version.HTTP_1_1);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(userprofile_url)).GET().timeout(Duration.ofMillis(20000)).build();

        client.connectTimeout();
        try {
            response = client.send(request,HttpResponse.BodyHandlers.ofString());

        }catch(java.net.http.HttpConnectTimeoutException e)
        {
            System.out.println("Connection Timeout!");
        } catch(java.net.ConnectException e)
        {
            System.out.println("Connection Timeout!");
        }catch (IOException e) {
            System.out.println("Dur");
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        findFollowerQuantity(response.body().toString());



    }



}