/*
 * Andrew Blackwell
 * Dr. Salimi
 * CSCI 4200
 * Fall 2023
 * Used my Lexical Analyzer: Yes
 */

//package lexPackage;
 
import java.util.*;
import java.io.*;

public class Parse {
    
	static String lexemeList[];
	static int currLexListIndex;
	static Token nextToken;
	static String lexeme;
	static FileWriter myOutput;
    static Scanner scan;

    // Tokens for parsing and lexical analysis
    enum Token {
        DIGIT,
        LETTER,
        UNKNOWN,
		ADD_OP,
		DIV_OP,
		END_KEYWORD,
		END_OF_FILE,
		IDENT,
		INT_LIT,
		LEFT_PAREN,
		MULT_OP,
		PRINT_KEYWORD,
		PROGRAM_KEYWORD,
		RIGHT_PAREN,
		SEMICOLON,
		SUB_OP,
		ASSIGN_OP,
		IF_KEYWORD,
		READ_KEYWORD,
		THEN_KEYWORD,
		ELSE_KEYWORD
	}


    /*******Main Method*******/
    /*
     * The main method pulls one line at a time from the sourceProgram.txt file for processing. After the
     * white spaces on each end of the line is removed, a call is made to the lex method to process the 
     * first word in the line. Then the program method is called to determine if the first word is the 
     * <program> nonterminal.
     */
    public static void main(String[] args) throws IOException {
        
        //File writer for creating the output text file and string for scanner to place lines from input file
        String line;
	    myOutput = new FileWriter("parseOut.txt");

        System.out.println("Andrew Blackwell, CSCI4200, Fall 2023, Parser"); 
		myOutput.write("Andrew Blackwell, CSCI4200, Fall 2023, Parser\n");
		System.out.println("********************************************************************************");
		myOutput.write("********************************************************************************\n");
        
        try {
			scan = new Scanner(new File("sourceProgram.txt"));
            while (scan.hasNextLine()) {
                myOutput.write(scan.nextLine() + "\n");
            }
            
            scan = new Scanner(new File("sourceProgram.txt"));
            while(scan.hasNextLine()){
            	System.out.print(scan.nextLine() + "\n");
            }
            
            System.out.println("********************\n");
            myOutput.write("********************\n");
            
            scan = new Scanner(new File("sourceProgram.txt"));
			// For each line, grab each character 
			while (scan.hasNextLine()) {
				line = scan.nextLine().trim();
				
				// Perform lexical analysis within array bounds 
                line = spaceOutLexemes(line);
                lexemeList = line.split(" ");
                currLexListIndex = 0;
				lex();
                program();
				
			}

			// If there are no more lines, it must be the end of file 
			if (!scan.hasNext()) {
				System.out.println("Exit <program>");
				myOutput.write("Exit <program>\n");
				System.out.println("Parsing of the program is complete!");
				myOutput.write("Parsing of the program is complete!\n");
                System.out.println("********************************************************************************");
		        myOutput.write("********************************************************************************\n");
			}
			
			scan.close();
		}
        
        catch (FileNotFoundException e) {
			System.out.println(e.toString());
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		
		myOutput.close();
	}
    
    private static void printList(String[] list) {
    	for (int i = 0; i < list.length; ++i) {
    		System.out.print("[" + list[i] + "], ");
    	}
    	System.out.println();
    }
    
    /*******Program method*******/
    /* Program is the start of the parsing process and calls each subsequent method until parsing is 
     * complete.
    */
    private static void program() throws IOException{
        // Checks for keyword to begin parsing of program
        if (nextToken == Token.PROGRAM_KEYWORD) {
            System.out.println("Enter <program>");
            myOutput.write("Enter <program>\n");
            
            while (scan.hasNextLine()) {
                // Begins iterating through lines and trim to get rid of white spaces
                String line = scan.nextLine().trim();
                line = spaceOutLexemes(line);
                lexemeList = line.split(" ");
                currLexListIndex = 0;
                
                //Indirect check to see whether another statement follows to determine if an error should be displayed
                if(nextToken != Token.PROGRAM_KEYWORD && nextToken != Token.SEMICOLON && !line.equals("END")){
                	System.out.println("**Error** - missing semicolon");
                	myOutput.write("**Error** - missing semicolon\n");
                }
                
                //Processes the first statement
                if (nextToken != Token.END_KEYWORD) {
                    lex();
                    // Checks for inputs for statement and calls method
                    if (nextToken == Token.PRINT_KEYWORD || nextToken == Token.IDENT || nextToken == Token.IF_KEYWORD || nextToken == Token.READ_KEYWORD) {
                        statement();
                    }
                    else if (nextToken != Token.END_KEYWORD) {
                        System.out.println("**ERROR** - expected identifier or PRINT_KEYWORD\n");
                        myOutput.write("**ERROR** - expected identifier or PRINT_KEYWORD\n");
                    }
                    
                    //If there is a semicolon after a statement, another statement must follow, otherwise error
                    while(nextToken == Token.SEMICOLON && nextToken != Token.END_KEYWORD && scan.hasNextLine()){
                    	
                    	line = scan.nextLine().trim();//retrieves next line
                    	line = spaceOutLexemes(line);
                    	lexemeList = line.split(" ");
                        currLexListIndex = 0;
                    	lex();
                    	if(nextToken != Token.END_KEYWORD)//if the end token is found, then a statement is missing
                    		statement();
                    	else{
                    		System.out.println("**Error** - missing statement");
                    		myOutput.write("**Error** - missing statement\n");
                    	}//end else
                    }//end while 
                }//end if
            }//end while
        }//end if
    }//end method
    
    
    /*******statement method*******/
    // First method called by program
    private static void statement() throws IOException {
        //If statements for checking statement type: Assign/output/selection/input
    	System.out.println("Enter <statement>");
    	myOutput.write("Enter <statement>\n");
        if(nextToken == Token.IDENT) {
            assign();
        }
        else if(nextToken == Token.PRINT_KEYWORD) {
            output();
        }
        else if(nextToken == Token.IF_KEYWORD){
        	selection();
        }
        else if(nextToken == Token.READ_KEYWORD){
        	input();
        }
        else if (nextToken != Token.END_KEYWORD){
        	System.out.println(nextToken);
            System.out.println("**ERROR** - expected identifier or PRINT_KEYWORD");
            myOutput.write("**ERROR** - expected identifier or PRINT_KEYWORD\n");
        }
        System.out.println("Exit <statement>\n");
        myOutput.write("Exit <statement>\n\n");
    }
    
    
    /*******output method*******/
    // used for printing
    private static void output() throws IOException {
        System.out.println("Enter <output>");
         myOutput.write("Enter <output>\n");
         // Calls lex to grab next token and then checks to see if that token is left parentheses. 
         // From there checks to see what the next token is and calls expression
         lex();
         if(nextToken == Token.LEFT_PAREN) {
            lex();
            expr();
            //if(lexeme[--lexLen] != nextChar)
            	lex();
         }
         else {
            System.out.println("**ERROR** - left-parenthesis\n");
            myOutput.write("**ERROR** - left-parenthesis\n");
         }
         System.out.println("Exit <output>");
         myOutput.write("Exit <output>\n");
    }
    
    
    /*******assign method*******/
    // Statement calls assign based on if nextToken is an ident
    private static void assign() throws IOException {
        System.out.println("Enter <assign>");
        myOutput.write("Enter <assign>\n");
        //checks to make sure next token is ident and calls lex
        if (nextToken == Token.IDENT) {
            lex();
            // checks to make sure equals sign is there and then calls lex and expr method
            if (nextToken == Token.ASSIGN_OP) {
                    lex();
                    expr();
            }
            else {
                System.out.println("**ERROR** - expected ASSIGN_OP");
                myOutput.write("**ERROR** - expected ASSIGN_OP\n");
            }
        }//end if
        else {
            System.out.println("**ERROR** - expected IDENT");
            myOutput.write("**ERROR** - expected IDENT\n");
        }
        System.out.println("Exit <assign>");
        myOutput.write("Exit <assign>\n");
    }
    
    
    /*******input method*******/
    /*First processes the left parentheses, then the IDENT, and then the right parentheses*/
    public static void input() throws IOException{
    	System.out.println("Enter <input>");
    	myOutput.write("Enter <input>\n");
    	lex();//grabs the left parentheses
    	if(nextToken==Token.LEFT_PAREN){
    		lex();//grabs IDENT
    	}
    	else{
    		System.out.println("**Error** - missing left-parentheses");
    		myOutput.write("**Error** - missing left-parentheses\n");
    	}
    	if(nextToken==Token.IDENT)
    		lex();//gets right parentheses
    	else{
    		System.out.println("**Error** - missing IDENT");
    		myOutput.write("**Error** - missing IDENT\n");
    	}
    	if(nextToken==Token.RIGHT_PAREN)
    		lex();//gets semicolon
    	else{
    		System.out.println("**Error** - missing right-parentheses");
    		myOutput.write("**Error** - missing right-parentheses\n");
    	}
    	System.out.println("Exit <input>");
    	myOutput.write("Exit <input>\n");
    }
    
    
    /*******selection method*******/
    /*
     *Handles statements that start with the if keyword. If the if keyword has been processed, the following
     *tokens in the statement are processed. 
     */
    private static void selection() throws IOException{
    	System.out.println("Enter <selection>");
    	myOutput.write("Enter <selection>\n");
    	if(nextToken == Token.IF_KEYWORD){//checks if the if keyword is there
    		lex();//grabs the left-parentheses
    		if(nextToken == Token.LEFT_PAREN){//checks to see if the next token was a left-parenthses
    			expr();
    		}
    		else{
    			System.out.println("**Error** - missing left-parentheses");
    			myOutput.write("**Error** - missing left-parentheses\n");
    		}
    				
    		if(nextToken == Token.THEN_KEYWORD){//checks to see if the then keyword has been found (processed)
    			lex();//grabs the next token in the statement 
    	    	statement();
    	    }
    		if(nextToken == Token.ELSE_KEYWORD){//checks to see if the then keyword has been found (processed)
    			lex();//grabs the next token in the statement 
    	    	statement();
    	    }
    		else{
    			System.out.println("**Error** - missing then keyword");
    			myOutput.write("**Error** - missing then keyword\n");
    		}
    		
    	}//end if
    	else{
    		System.out.println("**Error** - missing if");
    		myOutput.write("**Error** - missing if\n");
    	}
    	System.out.println("Exit <selection>");
    	myOutput.write("Exit <selection>\n");
    }
    
    
    /*******expr method*******/
    // expr is called by assign, output or factor and leads to terms
    private static void expr() throws IOException{
        System.out.println("Enter <expr>");
        myOutput.write("Enter <expr>\n");
         term();
        // Checks for addition or subtraction operators and calls term and lex for next token to parse
         while(nextToken == Token.ADD_OP || nextToken == Token.SUB_OP) {
            lex();
            term();
         }
         
         System.out.println("Exit <expr>");
         myOutput.write("Exit <expr>\n");
    }
    
    
    /*******term method*******/
    //terms produce factors and is called by expr
    private static void term() throws IOException{
        System.out.println("Enter <term>");
        myOutput.write("Enter <term>\n");
        // beginning call for factor
        factor();
        // checks for multiplication or division operators and makes a recursive method call for the next factor to multiply or divide by
        while (nextToken == Token.MULT_OP || nextToken == Token.DIV_OP) {
            lex();
            factor();
        }
        System.out.println("Exit <term>");
        myOutput.write("Exit <term>\n");
    }
  
    
    /*******factor method*******/
    /* factor is called by term and is a key part of the parser as this is where the actual values 
     * are defined and where expr is looped back through to lengthen equations
   	*/
    private static void factor() throws IOException {
        System.out.println("Enter <factor>");
        myOutput.write("Enter <factor>\n");
        // if statements run through cases to ascertain what methods will be called. ident/int_lit/expr
        if (nextToken == Token.IDENT || nextToken == Token.INT_LIT || nextToken == Token.IF_KEYWORD) {
        	lex();
        }
        
        else { 
            // Used to check for calling of expression and lex is called twice for left and right parentheses
            if (nextToken == Token.LEFT_PAREN) {
                lex();
                expr();
                if(nextToken == Token.RIGHT_PAREN)//ADDED in accordance to the ch 4 ver 6 slide 23
                	lex();
                else{
                	System.out.println("**ERROR** - expected right-parenthesis");
                    myOutput.write("**ERROR** - expected right-parenthesis\n");
                }
            }//end if     
         
            else {
                System.out.println("**ERROR** - expected identifier, integer or left-parenthesis");
                myOutput.write("**ERROR** - expected identifier, integer or left-parenthesis\n");
            }
        }//end else   
        System.out.println("Exit <factor>");
        myOutput.write("Exit <factor>\n");
    }
	

	/*
	 * My Lexical Analyzer
	 */
    private static String spaceOutLexemes(String string) {
		
		String spacedOutString = "";
		
		for (int currentChar = 0; currentChar < string.length(); ++currentChar) {
			if (string.charAt(currentChar) == '=' ||
				string.charAt(currentChar) == '+' ||
				string.charAt(currentChar) == '-' ||
				string.charAt(currentChar) == '*' ||
				string.charAt(currentChar) == '/' ||
				string.charAt(currentChar) == '(' ||
				string.charAt(currentChar) == ')' ||
				string.charAt(currentChar) == ';') {

				spacedOutString = spacedOutString.concat(" " + String.valueOf(string.charAt(currentChar)) + " ");
			} else {
				spacedOutString = spacedOutString.concat(String.valueOf(string.charAt(currentChar)));
			}
		}
		spacedOutString = spacedOutString.replaceAll("\\s+", " ");
		return spacedOutString;
	}

	
	/*
	 * This method takes the array of strings that represent individual lexemes.
	 * Then matches each lexeme to a token
	 */
	private static void matchLexemeToToken(String lexeme) {
		
		if (lexeme.matches("[0-9]+")) {
			nextToken = Token.INT_LIT;
		} 
		else if (lexeme.matches("=")) {
			nextToken = Token.ASSIGN_OP;
		} 
		else if (lexeme.matches("\\+")) {
			nextToken = Token.ADD_OP;
		}
		else if (lexeme.matches("\\-")) {
			nextToken = Token.SUB_OP;
		}
		else if (lexeme.matches("\\*")) {
			nextToken = Token.MULT_OP;
		}
		else if (lexeme.matches("\\/")) {
			nextToken = Token.DIV_OP;
		}
		else if (lexeme.matches("\\(")) {
			nextToken = Token.LEFT_PAREN;
		}
		else if (lexeme.matches("\\)")) {
			nextToken = Token.RIGHT_PAREN;
		}
		else if (lexeme.matches("END")) {
			nextToken = Token.END_KEYWORD;
		}
		else if (lexeme.matches("print")) {
			nextToken = Token.PRINT_KEYWORD;
		}
		else if (lexeme.matches("PROGRAM")) {
			nextToken = Token.PROGRAM_KEYWORD;
		}
		else if (lexeme.matches(";")) {
			nextToken = Token.SEMICOLON;
		}
		else if (lexeme.matches("if")) {
			nextToken = Token.IF_KEYWORD;
		}
		else if (lexeme.matches("read")) {
			nextToken = Token.READ_KEYWORD;
		}
		else if (lexeme.matches("then")) {
			nextToken = Token.THEN_KEYWORD;
		}
		else if (lexeme.matches("else")) {
			nextToken = Token.ELSE_KEYWORD;
		}
		//The IDENT Token is at the end, so we can check for if, then, end, print, program and read keywords first
		else if (lexeme.matches("[a-zA-Z]+")) {
			nextToken = Token.IDENT;
		} 
		else 
		{
			nextToken = Token.UNKNOWN;
		}
	}
	
	
	/*
	 * 
	 */
	private static void lex() {
		if (currLexListIndex < lexemeList.length) {
			lexeme = lexemeList[currLexListIndex];
			matchLexemeToToken(lexeme);
			System.out.println("Next token is: " + nextToken);
			currLexListIndex++;
		}
	}
}
