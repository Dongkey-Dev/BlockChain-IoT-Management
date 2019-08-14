package d.somewheres.uieosio;

import org.json.simple.ItemList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;
import java.util.stream.Collectors;

public class Search_Json {
    String result;
    List<String> result_list = new ArrayList<>();
    List<String> rqster_list = new ArrayList<>();
    List<String> device_footprint = new ArrayList<>();
    List<String> act_iot = new ArrayList<>();
    HashMap<String, List<List>> per_iot = new HashMap<String, List<List>>();


    public List Parsing_Response(String input, String which){
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonParser.parse(input);
            JSONArray actions = (JSONArray) jsonObj.get("actions");
            for (int i = 0; i < actions.size(); i++) {
                JSONObject first = (JSONObject) actions.get(i);
                JSONObject action_trace = (JSONObject) first.get("action_trace");
                JSONObject act = (JSONObject) action_trace.get("act");
                String name = String.valueOf(act.get("name"));
                JSONObject data = (JSONObject) act.get("data");
                String rqster = String.valueOf(data.get("rqster"));
                if (name.equals("attachdevice") && which.equals("attachdevice")) {
                    String dvice = String.valueOf(data.get("dvice"));
                    this.result_list.add("attachdevice: " + dvice);
                } else if (name.equals("pushdata") && which.equals("pushdata")) {
                    String targetdevice = String.valueOf(data.get("targetdevice"));
                    String push_data = String.valueOf(data.get("data"));
                    String block_time = String.valueOf(first.get("block_time"));
                    this.result_list.add(" rqster: " + rqster + "\ntargetdevice: " + targetdevice + "\npush_data: " + push_data + "\nblock_time: " + block_time);
                } else if (name.equals("adduser") && which.equals("adduser")) {
                    String wantuser = String.valueOf(data.get("wantuser"));
                    this.result_list.add("adduser: " + wantuser);
                }
            }
        }catch (ParseException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result_list;
    }

    public HashMap<String, List<List>> Lookup_device_detail(String input){
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonParser.parse(input);
            JSONArray actions = (JSONArray) jsonObj.get("actions");
            for (int i = 0; i < actions.size(); i++) {
                JSONObject first = (JSONObject) actions.get(i);
                JSONObject action_trace = (JSONObject) first.get("action_trace");
                JSONObject act = (JSONObject) action_trace.get("act");
                String name = String.valueOf(act.get("name"));
                JSONObject data = (JSONObject) act.get("data");
                String rqster = String.valueOf(data.get("rqster"));
                this.rqster_list.add(rqster);
                this.rqster_list = rqster_list.stream()
                        .filter(rqster_list -> !"null".equals(rqster_list))
                        .collect(Collectors.toList());
                this.rqster_list = rqster_list.stream()
                        .distinct()
                        .collect(Collectors.toList());
                if (name.equals("pushdata")) {
                    String push_data = String.valueOf(data.get("data"));
                    String block_time = String.valueOf(first.get("block_time"));
                    for (int j = 0; j < rqster_list.size(); j++) {
                        List<List> temp = new ArrayList<>();
                        List<String> temp2 = new ArrayList<>();
                        // List<String> temp = new ArrayList<>(); 최근조작내역
                        if (rqster_list.get(j).equals(rqster)) {
                            temp2.add(push_data);
                            temp2.add(block_time);
                            this.per_iot.put(rqster, temp);
                            this.per_iot.get(rqster).add(temp2);
                        }
                    }
                }
            }
        }catch (ParseException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(per_iot);
        return per_iot;
    }
    public String Get_Account_names(String input){
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonParser.parse(input);
            JSONArray accountId = (JSONArray) jsonObj.get("account_names");
            this.result = String.valueOf(accountId);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public String Get_Accout_Public_key(String input){
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonParser.parse(input);
            JSONArray first_step = (JSONArray) jsonObj.get("permissions");
            JSONObject first = (JSONObject) first_step.get(0);
            JSONObject required_auth = (JSONObject) first.get("required_auth");
            JSONArray keys = (JSONArray) required_auth.get("keys");
            JSONObject second = (JSONObject) keys.get(0);
            String key = (String) second.get("key");
            this.result = String.valueOf(key);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public String Get_Head_Block_time(String input){
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonParser.parse(input);
            String accountId = (String) jsonObj.get("head_block_time");
            this.result = String.valueOf(accountId);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public String Get_Created(String input){
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonParser.parse(input);
            String accountId = (String) jsonObj.get("created");
            this.result = String.valueOf(accountId);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public String Get_Account_ID(String input){
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonParser.parse(input);
            String accountId = (String) jsonObj.get("account_name");
            this.result = String.valueOf(accountId);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }
}