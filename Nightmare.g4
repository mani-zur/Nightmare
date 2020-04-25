grammar Nightmare ;

nightmare
    : (command 'xD')*
    ;

command
    : read_stm
    | write_stm
    | init_stm
    | assign_stm
    ;

//Statemnts

read_stm
    : 'Thanks' ID
    ;

write_stm
    : 'Think' (ID | expr )
    ;

assign_stm
    : ID '=' expr
    ;

init_stm
    : TYPE TABLE? ID ( '=' expr )? 
    ;

expr
    : expr ('+'|'-'|'*'|'/') expr
    | '(' expr ')'
    | ID
    | value
    ;

//Types
TYPE
    :'Tank'
    |'Daisy'
    ;

value
    : INT
    | FLOAT
    | STRING
    ;

ID
    : [a-zA-Z]+
    ;

INT  
    :[0-9]+
    ;

FLOAT
    :[0-9]*'.'[0-9]+
    ;

STRING 
    : '"' ( ~('\\'|'"') )* '"'
    ;
 
TABLE
    :'^^' INT '^^'
    ;

//Whitesymbols

WS 
    : [ \t\r\n]+ -> skip 
    ;