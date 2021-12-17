import java.util.Scanner;
import java.util.ArrayList;
public class Main {
    public static void main(String[] args) {
        new Shell();    // Creating a new Shell
    }
}
class Shell {
    // The current code-snippet grabbed from the Shell interface
    private String snippet;
    // A list of the tokens created from the current snippet
    private ArrayList<Token> tokens = new ArrayList<>();
    // A list of the tokens after normalization + paren verification/matching
    private final ArrayList<Token> prepared_tokens = new ArrayList<>();
    // The Runtime for the Shell that handles the language side
    private final Runtime runtime = new Runtime();
    // Contains the code Shell-loop and initialization
    public Shell() {
        Scanner scan = new Scanner(System.in);
        while (true) {
            // Grabbing the snippet from the user
            System.out.print(">>");
            this.snippet = scan.nextLine();

            // Evaluating the snippet
            if (this.snippet.equals("exit")) break;
            // If snippet isn't empty, then we evaluate it
            else if (!(this.snippet.replace(" ", "").equals(""))) eval_snippet();
        }
        scan.close();
    }
    // A driver for snippet tokenization and also early driver for code parsing
    private void eval_snippet() {
        tokenize();
        lex_tokens();
        // Prepared_tokens is saved and appended to until the statement is complete (think of code-blocks)
        this.prepared_tokens.addAll(this.tokens);

        if (!(verify_parens())) return;
        // Executing the commands based off of the statement tokens
        this.runtime.parse_tokens(this.prepared_tokens);
        this.prepared_tokens.clear();
    }
    // Fully tokenizes the snippet passed by the Shell
    private void tokenize() {

        boolean split = true;   // Whether string is split by spaces and symbols
        char paren_type = ' ';  // The type of parentheses used for the detected string
        StringBuilder token_name = new StringBuilder();
        ArrayList<Token> tokens = new ArrayList<>();
        for (char ch : this.snippet.toCharArray()) {
            if (split) {    // When splitting
                // Looking for the beginning of a string
                if (ch == '"' || ch == '\'') {
                    // Append paren variant to token_name, set split, then continue this tokenization cycle
                    paren_type = ch;
                    split = false;
                }
                // Append all Alphanumeric Characters to token_name
                if (Character.isAlphabetic(ch) || Character.isDigit(ch)) {
                    token_name.append(ch);
                    continue;
                }
                // -- Reached when non-alnum characters is encountered --
                // Doesn't append token of token_name when variable is empty
                if (!(token_name.length() == 0)) {
                    // Variable names are not to be split by underscores
                    if (!(ch == '_')) {
                        // Add token then delete contents of token_name
                        tokens.add(new Token(token_name.toString()));
                        token_name.delete(0, token_name.length());
                    }
                    else token_name.append(ch);
                }
                // Doesn't add token of current char if it matches any of the ones below
                if (!(ch == ' ' || ch == '_' || ch == '"' || ch == '\'')) {
                    tokens.add(new Token(Character.toString(ch)));
                }
                continue;
            }
            // When paren matching paren_type is encountered
            if (ch == paren_type) {
                // Inserts paren_type into first char of token_name, appends ch to token_name
                token_name.insert(0, paren_type);
                token_name.append(ch);
                // Adds token of token_name then deletes contents of token_name
                tokens.add(new Token(token_name.toString()));
                token_name.delete(0, token_name.length());
                split = true;
                continue;
            }
            token_name.append(ch);  // Otherwise append ch to token_name
        }
        // For when loop ends without appending potential token inside of token_name
        if (!(token_name.toString().replace(" ", "").equals(""))) {
            tokens.add(new Token(token_name.toString()));
        }
        this.tokens = tokens;
    }
    // Partially lexes all Tokens that aren't combinations of standalone or single symbol Tokens
    private void lex_tokens() {
        ArrayList<Token> tokens = this.tokens;

        for (Token tok : tokens) {
            // Sets Token type to string if fist and last char are '"' or "'"
            if (tok.name.charAt(0) == '"' && tok.name.charAt(tok.name.length() - 1) == '"' ||
                tok.name.charAt(0) == '\'' && tok.name.charAt(tok.name.length() - 1) == '\'') tok.type = "string";
            else {
                switch (tok.name) {
                    case "print":
                    case "println":
                    case "exit":
                        tok.type = "instruction";
                        break;
                    case "var":
                    case "const":
                        tok.type = "declaration";
                        break;
                    case "=":
                    case "+":
                    case "-":
                    case "*":
                    case "%":
                    case "/":    
                    case "~":
                        tok.type = "operator";
                        break;
                    case "#":
                        tok.type = "comment";
                        break;
                    case ">":
                    case "<":
                        tok.type = "logical";
                        break;
                    case ",":
                    case ";":
                        tok.type = "separator";
                        break;
                    case "(":
                    case "[":
                    case "{":
                        tok.type = "opening paren";
                        break;
                    case ")":
                    case "]":
                    case "}":
                        tok.type = "closing paren";
                        break;
                    default:
                        // If one alpha char is encountered, item must be a variable
                        for (char ch : tok.name.toCharArray()) {
                            if (Character.isAlphabetic(ch)) {
                                tok.type = "variable";
                                break;
                            }
                        }
                        // If token type is unset, then the token name was entirely numeric, making it an integer
                        if (tok.type.equals("None")) tok.type = "integer";
                        break;
                }
            }
        }
        // Lexing pre-lexed tokens for previously undetectable token types
        lex_float(tokens);
        lex_logical();
    }
    // Lexes all Tokens that are deemed to be floats
    private void lex_float(ArrayList<Token> tokens) {
        ArrayList<Token> fully_lexed = new ArrayList<>(); int skip = 0;
        for (int i = 0; i < tokens.size(); i++) {
            if (!(skip == 0)) {     // Needed to skip re-tokenizing the three tokens that were made into a float
                skip--;
                continue;
            }
            // If current token is integer. And there is a token one after the current token
            if (tokens.get(i).type.equals("integer") && i + 1 < tokens.size()) {
                // If token one after current is '.'. And there is a token two after the current token
                if (tokens.get(i + 1).name.equals(".") && i + 2 < tokens.size()) {
                    // If token two after current is integer
                    if (tokens.get(i + 2).type.equals("integer")) {
                        fully_lexed.add(new Token(
                                // Appends the three tokens from current to 2+ current as one token
                                tokens.get(i).name + tokens.get(i + 1).name + tokens.get(i + 2).name,
                                "float" // With the type 'float'
                        ));
                        skip = 2;
                        continue;
                    }
                }
            }
            // Appends current token
            fully_lexed.add(new Token(tokens.get(i).name, tokens.get(i).type));
        }
        this.tokens = fully_lexed;
    }
    // Lexes all other Tokens that can be described as two symbols (Which happens to be a lot of logical Tokens)
    private void lex_logical() {
        ArrayList<Token> tokens = this.tokens;
        ArrayList<Token> final_tokens = new ArrayList<>();
        boolean skip = false;
        for (int index = 0; index < tokens.size(); index++) {
            if (skip) {
                skip = false;
                continue;
            }
            switch (tokens.get(index).name) {
                case "|":
                case "&":
                case "=":
                case "+":
                    // Checks if there is a token after the current token
                    // Then checks if the next token has the same name as the current token
                    if (index + 1 < tokens.size() && tokens.get(index + 1).name.equals(tokens.get(index).name)) {
                        final_tokens.add(new Token(tokens.get(index).name + tokens.get(index + 1).name));
                        skip = true;
                        continue;
                    }
                    break;
                case "<":
                case ">":
                    // Checks if there is another token after the current token
                    // Then checks if the next token has a name of '='
                    if (index + 1 < tokens.size() && tokens.get(index + 1).name.equals("=")) {
                        final_tokens.add(new Token(tokens.get(index).name + tokens.get(index + 1).name));
                        skip = true;
                        continue;
                    }
                    break;
            }
            final_tokens.add(new Token(tokens.get(index).name, tokens.get(index).type));
        }
        // Applying types to the newly created tokens
        for (Token token : final_tokens) {
            switch (token.name) {
                case "==":
                case ">=":
                case "<=":
                case "&&":
                case "||":
                    token.type = "logical";
                    break;
                case "++":
                    token.type = "operator";
                    break;
            }
        }
        this.tokens = final_tokens;
    }
    // TODO: I don't fully trust this code just yet. Seems to be just a little laggy sometimes
    // Verifies that all parens '(, [, {' have been correctly closed and not over-closed
    private boolean verify_parens() {
        // All the block based Tokens that need to match for a valid statement
        String[][] pairs = {{"{","}"},{"[","]"},{"(",")"}};
        // Runs the loop over the Tokens for each pair of parens
        for (String[] pair : pairs) {
            int paren_depth = 0;    // Amount of opened parens
            for (Token token : this.prepared_tokens) {
                // Guard clause against non-pair tokens
                if (!(token.name.equals(pair[0]) || token.name.equals(pair[1]))) continue;
                // Running appropriate paren_depth operations for each type of paren
                if (token.type.equals("opening paren")) paren_depth++;
                else paren_depth--;
                if (paren_depth < 0) return false;  // Returns if at any point more parens are closed then opened
            }
            // Returns if there are unclosed parens left
            if (!(paren_depth == 0)) return false;
        }
        return true;
    }
}
