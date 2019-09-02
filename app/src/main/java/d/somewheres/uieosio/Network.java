package d.somewheres.uieosio;

public class Network {
    //PK
    private int _id;

    private String name; //네트워크 이름
    private String desc; //네트워크 설명
    private String account; //관리자, 사용자

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
