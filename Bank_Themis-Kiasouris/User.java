public class User {
    private int id;
    private String username;

    // constructor to initialize id and username
    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    // getters and setters for id and username
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
