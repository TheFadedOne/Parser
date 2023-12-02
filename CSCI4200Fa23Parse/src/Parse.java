/*
 * Andrew Blackwell
 * Dr. Salimi
 * CSCI 4200
 * Fall 2023
 * Lexical Analyzer
 */

//package lexPackage;
 
import java.util.*;
import java.io.*;

public class Parse {
        
    
    static final int MAX_LEXEME_LEN = 100;
	static Token charClass;                            // Compare to enum to identify the character's class
	static int lexLen;                                 // Current lexeme's length
	static char lexeme[] = new char[MAX_LEXEME_LEN];   // Current lexeme's character array
	static char nextChar;
	static Token nextToken;
	static int charIndex;
	static char ch = '0';
	static FileWriter myOutput;
    static Scanner scan;
    static boolean blockInfinite = false;
	static boolean lastChar = false; 
	static boolean semiColFound = false;

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
		THEN_KEYWORD
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

        System.out.println("Christopher Davidson, CSCI4200, Spring 2023, Parser"); 
		myOutput.write("Christopher Davidson, CSCI4200, Spring 2023, Parser\n");
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
				charIndex = 0;
				
				if (getChar(line)) {
					// Perform lexical analysis within array bounds 
					lex(line);
                    program();
				}
			}

			// If there are no more lines, it must be the end of file 
			if (!scan.hasNext()) {
				System.out.print("Exit <program>\n");
				myOutput.write("Exit <program>\n");
				System.out.print("Parsing of the program is complete!");
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
                charIndex = 0;
                blockInfinite = false;
                lastChar = false;
                
                //Indirect check to see whether another statement follows to determine if an error should be displayed
                if(nextToken != Token.PROGRAM_KEYWORD && nextToken != Token.SEMICOLON && !line.equals("END")){
                	System.out.println("**Error** - missing semicolon");
                	myOutput.write("**Error** - missing semicolon\n");
                }
                
                //Processes the first statement
                if (nextToken != Token.END_KEYWORD && getChar(line)) {
                    lex(line);
                    // Checks for inputs for statement and calls method
                    if (nextToken == Token.PRINT_KEYWORD || nextToken == Token.IDENT || nextToken == Token.IF_KEYWORD || nextToken == Token.READ_KEYWORD) {
                        statement(line);
                    }
                    else if (nextToken != Token.END_KEYWORD) {
                        System.out.println("**ERROR** - expected identifier or PRINT_KEYWORD\n");
                        myOutput.write("**ERROR** - expected identifier or PRINT_KEYWORD\n");
                    }
                    
                    //If there is a semicolon after a statement, another statement must follow, otherwise error
                    while(nextToken == Token.SEMICOLON && nextToken != Token.END_KEYWORD && scan.hasNextLine()){
                    	
                    	line = scan.nextLine().trim();//retrieves next line
                    	charIndex = 0;
                    	getChar(line);
                    	lex(line);
                    	
                    	if(nextToken != Token.END_KEYWORD)//if the end token is found, then a statement is missing
                    		statement(line);
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
    private static void statement(String ln) throws IOException {
        //If statements for checking statement type: Assign/output/selection/input
    	System.out.println("Enter <statement>");
    	myOutput.write("Enter <statement>\n");
        if(nextToken == Token.IDENT) {
            assign(ln);
        }
        else if(nextToken == Token.PRINT_KEYWORD) {
            output(ln);
        }
        else if(nextToken == Token.IF_KEYWORD){
        	selection(ln);
        }
        else if(nextToken == Token.READ_KEYWORD){
        	input(ln);
        }
        else if (nextToken != Token.END_KEYWORD){
            System.out.println("**ERROR** - expected identifier or PRINT_KEYWORD");
            myOutput.write("**ERROR** - expected identifier or PRINT_KEYWORD\n");
        }
        System.out.println("Exit <statement>\n");
        myOutput.write("Exit <statement>\n\n");
    }
    
    
    /*******output method*******/
    // used for printing
    private static void output(String ln) throws IOException {
        System.out.println("Enter <output>");
         myOutput.write("Enter <output>\n");
         // Calls lex to grab next token and then checks to see if that token is left parentheses. 
         // From there checks to see what the next token is and calls expression
         lex(ln);
         if(nextToken == Token.LEFT_PAREN) {
            lex(ln);
            expr(ln);
            if(lexeme[--lexLen] != nextChar)
            	lex(ln);
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
    private static void assign(String ln) throws IOException {
        System.out.println("Enter <assign>");
        myOutput.write("Enter <assign>\n");
        //checks to make sure next token is ident and calls lex
        if (nextToken == Token.IDENT) {
            lex(ln);
            // checks to make sure equals sign is there and then calls lex and expr method
            if (nextToken == Token.ASSIGN_OP) {
                    lex(ln);
                    expr(ln);
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
    public static void input(String ln) throws IOException{
    	System.out.println("Enter <input>");
    	myOutput.write("Enter <input>\n");
    	lex(ln);//grabs the left parentheses
    	if(nextToken==Token.LEFT_PAREN){
    		lex(ln);//grabs IDENT
    	}
    	else{
    		System.out.println("**Error** - missing left-parentheses");
    		myOutput.write("**Error** - missing left-parentheses\n");
    	}
    	if(nextToken==Token.IDENT)
    		lex(ln);//gets right parentheses
    	else{
    		System.out.println("**Error** - missing IDENT");
    		myOutput.write("**Error** - missing IDENT\n");
    	}
    	if(nextToken==Token.RIGHT_PAREN)
    		lex(ln);//gets semicolon
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
    private static void selection(String ln) throws IOException{
    	System.out.println("Enter <selection>");
    	myOutput.write("Enter <selection>\n");
    	if(nextToken == Token.IF_KEYWORD){//checks if the if keyword is there
    		lex(ln);//grabs the left-parentheses
    		if(nextToken == Token.LEFT_PAREN){//checks to see if the next token was a left-parenthses
    			expr(ln);
    		}
    		else{
    			System.out.println("**Error** - missing left-parentheses");
    			myOutput.write("**Error** - missing left-parentheses\n");
    		}
    				
    		if(nextToken == Token.THEN_KEYWORD){//checks to see if the then keyword has been found (processed)
    			lex(ln);//grabs the next token in the statement 
    	    	statement(ln);
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
    private static void expr(String ln) throws IOException{
        System.out.println("Enter <expr>");
        myOutput.write("Enter <expr>\n");
         term(ln);
        // Checks for addition or subtraction operators and calls term and lex for next token to parse
         while(nextToken == Token.ADD_OP || nextToken == Token.SUB_OP) {
            lex(ln);
            term(ln);
         }
         
         System.out.println("Exit <expr>");
         myOutput.write("Exit <expr>\n");
    }
    
    
    /*******term method*******/
    //terms produce factors and is called by expr
    private static void term(String ln) throws IOException{
        System.out.println("Enter <term>");
        myOutput.write("Enter <term>\n");
        // beginning call for factor
        factor(ln);
        // checks for multiplication or division operators and makes a recursive method call for the next factor to multiply or divide by
        while (nextToken == Token.MULT_OP || nextToken == Token.DIV_OP) {
            lex(ln);
            factor(ln);
        }
        System.out.println("Exit <term>");
        myOutput.write("Exit <term>\n");
    }
  
    
    /*******factor method*******/
    /* factor is called by term and is a key part of the parser as this is where the actual values 
     * are defined and where expr is looped back through to lengthen equations
   	*/
    private static void factor(String ln) throws IOException {
        System.out.println("Enter <factor>");
        myOutput.write("Enter <factor>\n");
        // if statements run through cases to ascertain what methods will be called. ident/int_lit/expr
        if (nextToken == Token.IDENT || nextToken == Token.INT_LIT || nextToken == Token.IF_KEYWORD) {
        	if(lexeme[--lexLen] != nextChar)
        		lex(ln);
        }
        
        else { 
            // Used to check for calling of expression and lex is called twice for left and right parentheses
            if (nextToken == Token.LEFT_PAREN) {
                lex(ln);
                expr(ln);
                if(nextToken == Token.RIGHT_PAREN)//ADDED in accordance to the ch 4 ver 6 slide 23
                	lex(ln);
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
    
        
    /*******isKeyWord method*******/
    /*
     * Determines if any of the processed words are equal to these three keywords.
    */
    static boolean isKeyWord(String ln) {
		ln = ln.toUpperCase();
		if (ln.contains("PROGRAM")) {
			return true;
		}
		else if (ln.contains("END")) {
			return true;
		}
		else if (ln.contains("PRINT")) {
			return true;
		}
		else {
			return false;
		}
	}

    
    /*******keyWordLookUp method*******/
    /*
     * This method is activated if the isKeyWord method is true. The nextToken is changed to reflect the
     * keyword. 
    */
	static Token keyWordLookUp(String ln) {
		ln = ln.toUpperCase();
		Token tk = null;
		switch (ln) {

		case "PROGRAM":
			tk = Token.PROGRAM_KEYWORD;
			break;
		case "END":
			tk = Token.END_KEYWORD;
			break;

		case "PRINT":
			tk = Token.PRINT_KEYWORD;
			break;

		}
		return tk;

	}

	/*******lookup method*******/
	/*
	 * Assign each lexeme with its respective token. This allows the lexical analyzer to determine
	 * what the Token names connect to.
	 */
	 static Token lookup(char ch) {

		switch (ch) {
		
		case '(':
			addChar();
			nextToken = Token.LEFT_PAREN;
			break;

		case ')':
			addChar();
			nextToken = Token.RIGHT_PAREN;
			break;

		case '+':
			addChar();
			nextToken = Token.ADD_OP;
			break;

		case '-':
			addChar();
			nextToken = Token.SUB_OP;
			break;

		case '*':
			addChar();
			nextToken = Token.MULT_OP;
			break;

		case '/':
			addChar();
			nextToken = Token.DIV_OP;
			break;

		case '=':
			addChar();
			nextToken = Token.ASSIGN_OP;
			break;
		case ';':
			addChar();
			nextToken = Token.SEMICOLON;
			break;

			/*
			 * No default case - each lexeme should fall
			 * within one of the categories set above.
			 */
		}

		return nextToken;
	}

	
	/************* addChar - a function to add nextChar to lexeme *************/
	 static boolean addChar() {
		
		if (lexLen <= 98) {
			lexeme[lexLen++] = nextChar;
			lexeme[lexLen] = 0;
			return true;
		}
		
		else {
			System.out.println("Error - lexeme is too long \n");
			return false;
		}
	}

	
	/************* getChar - a function to get the next character in the line *************/
	 static boolean getChar(String ln) {

		if (charIndex >= ln.length()) {
			return false;
		}
		
		nextChar = ln.charAt(charIndex++);

		if (Character.isDigit(nextChar)) {
			charClass = Token.DIGIT;
		} 
		
		else if (Character.isAlphabetic(nextChar)) {
			charClass = Token.LETTER;
		} 
		
		else {
			charClass = Token.UNKNOWN;
		}

		return true;
	}
	 

	/************* getNonBlank - a method to skip whitespace *************/
	public static boolean getNonBlank(String ln) {
		while (Character.isSpaceChar(nextChar) || nextChar == '	') {
			if (!getChar(ln)){
				return false;
			}
		}
		return true;
	}
	

	 /* @throws IOException ***************************************************/
	/************* lex - a simple lexical analyzer for arithmetic expressions *************/
	public static Token lex(String ln) throws IOException {
		
		lexLen = 0;
		getNonBlank(ln);
		
		switch (charClass) {

		// Parse identifiers 
		case LETTER:
			nextToken = Token.IDENT;
			addChar();
			
			if (getChar(ln)) {
				while (charClass == Token.LETTER || charClass == Token.DIGIT) {
					addChar();
					
					if (!getChar(ln)) {
						break;
					}
				}//end while
				
				if(String.valueOf(lexeme,0,lexLen).equals("if"))
					nextToken = Token.IF_KEYWORD;
				if(String.valueOf(lexeme,0,lexLen).equals("read"))
					nextToken = Token.READ_KEYWORD;
				if(String.valueOf(lexeme,0,lexLen).equals("then"))
					nextToken = Token.THEN_KEYWORD;
				
				if (charClass == Token.UNKNOWN && charIndex == ln.length()) {
					charIndex--;
				}
			}//end if 
			break;
			
		// Parse integer literals 
		case DIGIT:
			nextToken = Token.INT_LIT;
			addChar();
			if (getChar(ln)) {
				while (charClass == Token.DIGIT) {
					addChar();
					
					if(!getChar(ln)) {
						break;
					}
				}
				
				if (charClass == Token.UNKNOWN && charIndex == ln.length()) {
					charIndex--;
				}
			}
			break;
			
		 // Parentheses and operators 
		case UNKNOWN:
			lookup(nextChar);
			getChar(ln);
			
			if(lexeme[0]!=nextChar&&charIndex==ln.length()&&blockInfinite==false){
				charIndex--;
				lastChar=true;
				blockInfinite=true;
			}
			if(ln.charAt(ln.length()-2)==nextChar&&charIndex==ln.length()&&blockInfinite==false){
				charIndex--;
				blockInfinite=true;
				lastChar=true;
			}
			break;
			
		default:
			nextToken = Token.UNKNOWN;
			break;
		} // End of switch
		
		if (isKeyWord(String.valueOf(lexeme,0,lexLen))) {
			nextToken = keyWordLookUp(String.valueOf(lexeme,0,lexLen));
		}
		
		 // Print each token and its respective lexeme 
		System.out.printf("Next token is: %-12s \n", String.valueOf(nextToken));
		myOutput.write(String.format("Next token is: %-12s\n", String.valueOf(nextToken)));

		return nextToken;
		
	} // End of function lex 
}
