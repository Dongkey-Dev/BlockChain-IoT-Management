package d.somewheres.uieosio;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Search_Json {
    String result;

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