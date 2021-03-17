grammar SearchQuery;

fullQuery
    : query EOF
    ;

query
   : query BinaryOperator query         # binaryOperation
   | NOT query                          # unaryOperation
   | LC query RC                        # curlyBracket
   | LS query RS                        # squareBracket
   | StringLiteral                      # string
   ;

BinaryOperator
   : AND
   | OR
   ;

NOT : 'NOT' ;
AND : 'AND' ;
OR : 'OR' ;

LC : '(' ;
RC : ')' ;
LS : '[' ;
RS : ']' ;

FunctionName
    : [a-zA-Z0-9]+
    ;

StringLiteral
	:	'"' StringCharacter* '"'
	|	'\'' StringCharacterApostrophe* '\''
	;

fragment
StringCharacter
	:	~["]
	|	'\\' ["]
	;

fragment
StringCharacterApostrophe
    :	~[']
    |	'\\' [']
    ;

WS
    :  [ \t\r\n\u000C]+ -> skip
    ;