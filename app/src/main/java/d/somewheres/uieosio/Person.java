package d.somewheres.uieosio;

public class Person {

    //PK
    private int _id;

    private String name; //계정이름
    private String userkey; //퍼블릭키
    private String password; //지갑 패스워드

    public int get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getUserkey() {
        return userkey;
    }

    public String getPassword() { return password;}

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserkey(String userkey){
        this.userkey = userkey;
    }

    public void setPassword(String password) { this.password = password; }
}
