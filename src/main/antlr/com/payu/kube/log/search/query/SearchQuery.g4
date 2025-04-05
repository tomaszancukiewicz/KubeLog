grammar SearchQuery;

fullQuery
    : query EOF
    ;

query
   : LC query RC                        # bracket
   | LS query RS                        # bracket
   | NOT query                          # unaryOperation
   | query BinaryOperator query         # binaryOperation
   | IdentifierLiteral LC query RC     # function
   | RegexLiteral                       # regex
   | StringLiteral                      # string
   | IdentifierLiteral                  # identifier
   ;

BinaryOperator
   : AND
   | OR
   ;

// lexer
NOT : 'NOT' | 'not' ;
AND : 'AND' | 'and' ;
OR : 'OR' | 'or' ;

LC : '(' ;
RC : ')' ;
LS : '[' ;
RS : ']' ;

RegexLiteral
    : 'r' StringLiteral
    ;

StringLiteral
	:	'"' (~'"' | '\\"')* '"'
	|	'\'' (~'\'' | '\\\'')* '\''
	;

IdentifierLiteral
    : ~["' ()[\]]+
    ;

WS
    :  [ \t\r\n\u000C]+ -> skip
    ;