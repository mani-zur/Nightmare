
grammar Nightmare ;

nightmare
    : (command 'xD')*
    ;

command
    : read_stm
    | write_stm
    | init_stm
    | assign_stm
    | assign_table_stm
    | comment
    ;

//Statemnts

read_stm
    : 'Thanks' ID
    ;

write_stm
    : 'Think' (ID | expr0 )
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
    | STRING    #String
    | ID    #Id
    | '(' expr0 ')' #par
    ;

comment
    : 'nvm' ( ~('\\'|'"') )* 'nvm'
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