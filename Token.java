public class Token {
    public String name;
    public String type = "None";
    public Token(String name) {this.name = name;}
    public Token(String name, String type) {this.name = name;this.type = type;}

    // TODO:toString is only used for development purposes, nothing should depend on it
    public String toString() {return this.name + " " + this.type;}
}
