
grammar Nightmare ;

nightmare
    :  ((command | function_declaration) 'xD')*
    ;

command
    : read_stm
    | write_stm
    | init_stm
    | assign_stm
    | assign_table_stm
    | assign_string
    | loop_stm
    | if_stm
    | comment
    ;

function_declaration
    : 'FUN' params (command 'xD')* 'ENDFUN'
    ;

params
    : TYPE ID 
    ;



//Statemnts

read_stm
    : 'Thanks' ID
    ;

write_stm
    : 'Think' expr0
    ;

init_stm
    : TYPE ID       #Variable_Init
    | TYPE table #Table_Init
    | TYPE assign_stm #ass_init
    ;

assign_stm
    : ID '=' expr0
    ;

assign_table_stm
    : table '=' expr0
    ;

assign_string
    : ID '=' STRING
    ;

expr0
    : expr1             #Single0
    | expr1 ADD expr1   #Add
    | expr1 SUB expr1   #Sub
    ;

expr1
    : expr2             #Single1
    | expr2 MUL expr2   #Mul
    | expr2 DIV expr2   #Div
    ;

expr2
    : INT   #Int
    | FLOAT #Float
    | ID    #Id
    | table #Table_load
    | '(' expr0 ')' #par
    ;

loop_stm
    : 'LOOP' repetitions (command 'xD')* 'ENDLOOP'
    ;

repetitions
    : expr2
    ;

if_stm
    : 'IF' condition 'THEN' (command 'xD')* 'ENDIF'
    ;

condition
    : expr0 '==' expr0
    ;

comment
    : 'nvm' ( ~('nvm'|'\\'|'"') )* 'nvm'
    ;


//Types

TYPE
    :'Tank'
    |'Daisy'
    |'Fork'
    ;

ID
    : [a-zA-Z]+
    ;

INT  
    :'-'?[0-9]+
    ;

FLOAT
    :'-'?[0-9]*'.'[0-9]+
    ;

STRING 
    : '"' ( ~('\\'|'"') )* '"'
    ;
 
table
    :ID '^' WS* INT WS* '^'
    ;

//Whitesymbols

ADD : '+';
SUB : '-';
MUL : '*';
DIV : '/';

WS 
    : [ \t\r\n]+ -> skip 
    ;