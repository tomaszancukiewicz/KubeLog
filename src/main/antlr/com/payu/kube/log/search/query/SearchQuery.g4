grammar SearchQuery;

fullQuery
    : query EOF
    ;

query
   : LC query RC                        # bracket
   | LS query RS                        # bracket
   | NOT query                          # notOperation
   | query AND? query                   # andOperation
   | query OR query                     # orOperation
   | RegexIndicator StringLiteral       # regex
   | StringLiteral                      # string
   | IdentifierLiteral                  # identifier
   ;

// lexer
NOT : 'NOT' | 'not' ;
AND : 'AND' | 'and' ;
OR : 'OR' | 'or' ;

LC : '(' ;
RC : ')' ;
LS : '[' ;
RS : ']' ;

RegexIndicator
    : 'r' ('i')? ('c')?
    ;

StringLiteral
	:	'"' (~'"' | '\\"')* '"'
	|	'\'' (~'\'' | '\\\'')* '\''
	;

IdentifierLiteral
    : ~["' ()[\]]+
    ;

WS
    :  [ \t\r\n\u000C]+ -> channel(HIDDEN)
    ;