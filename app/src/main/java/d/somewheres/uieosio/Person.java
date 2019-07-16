package d.somewheres.uieosio;

public class Person {

    //PK
    private int _id;

    private String name;
    private String userkey;

    public int get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getUserkey() {
        return userkey;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserkey(String userkey){
        this.userkey = userkey;
    }
}
