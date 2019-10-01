package d.somewheres.uieosio;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Yeelight_Parser {
    String falut = "wrong_data";
    String result;
    public String parser(JSONObject input){
        if (input.get("method").equals("set_bright")){
            return bright_parser(input);
        } else if (input.get("method").equals("set_ct_abx")){
            return ct_parser(input);
        } else if (input.get("method").equals("set_hsv")){
            return hsv_parser(input);
        } else if (input.get("method").equals("set_power")){
            return set_power(input);
        } else{
            return falut;
        }
    }
    public String bright_parser(JSONObject input){
        List param = new ArrayList<>();
        param = (List) input.get("params");
        result = "Bright_IS_" + param.get(0).toString() + "_Percent_changed";
        return result;
    }
    public String set_power(JSONObject input){
        List param = new ArrayList<>();
        param = (List) input.get("params");
        result = "Device_power_IS_" + param.get(0).toString();
        return result;
    }
    public String ct_parser(JSONObject input){
        List param = new ArrayList<>();
        param = (List) input.get("params");
        result = "Color_temperature_IS_" + param.get(0).toString() + "_changed";
        return result;
    }
    public String hsv_parser(JSONObject input){
        List param = new ArrayList<>();
        param = (List) input.get("params");
        java.lang.Long value = (Long) param.get(0);
        if (value <= 6) {
            result = "Color_change_Red";
        } else if (7 <= (value) && (value) <= 32 ) {
            result = "Color_change_Orange";
        } else if (33 <= value && value <= 56){
            result = "Color_change_Yellow";
        } else if (57<= value && value <= 97) {
            result = "Color_change_Fluorescent";
        } else if (98 <= value && value <= 153) {
            result = "Color_change_Green";
        } else if (154 <= value && value <= 206) {
            result = "Color_change_Blue";
        } else if (207 <= value && value <= 260) {
            result = "Color_change_Indigo";
        } else if (261 <= value && value <= 285) {
            result = "Color_change_Purple";
        } else if (286 <= value && value <= 333) {
            result = "Color_change_Pink";
        } else if (334 <=  value && value <= 360) {
            result = "Color_change_Grapefruit";
        }
        return result;
    }
}