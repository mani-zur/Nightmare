import java.util.HashMap;
import java.util.Stack;

enum VarType{ INT, FLOAT, STRING, INT_TBL, FLOAT_TBL, INT_FUN, FLOAT_FUN}

class Value{ 
	public String name;
	public VarType type;
	public Value( String name, VarType type ){
		this.name = name;
		this.type = type;
	}
}


public class LLVMActions extends NightmareBaseListener {

	HashMap<String, VarType> variables = new HashMap<String, VarType>();
	HashMap<String, VarType> localvariables = new HashMap<String, VarType>();
	HashMap<String, String> tables = new HashMap<String, String>();
	Stack <Value> stack = new Stack<Value>();
	
	Boolean global;

    @Override 
    public void exitNightmare(NightmareParser.NightmareContext ctx) { 
		LLVMGenerator.close_main();
        System.out.println(LLVMGenerator.generate());
	}

	//LOOP
	@Override 
	public void exitRepetitions(NightmareParser.RepetitionsContext ctx) { 
		LLVMGenerator.repeatstart(stack.pop().name);
	}
	
	@Override 
	public void exitLoop_stm(NightmareParser.Loop_stmContext ctx) { 
		LLVMGenerator.repeatend();
	}


	//IF

	@Override 
	public void exitCondition(NightmareParser.ConditionContext ctx) { 
		Value v1 = stack.pop();
		Value v2 = stack.pop();
		if (v1.type == v2.type){
			switch (v1.type) {
                case INT:
                    LLVMGenerator.icmp(v1.name, v2.name);
                    break;
				case FLOAT:
					LLVMGenerator.fcmp(v1.name, v2.name);
                    break;
            
                default:
                    error(ctx.getStart().getLine(), "type mismatch");
                    break;
            }
		}
		LLVMGenerator.ifstart();
	}

	@Override 
	public void exitIf_stm(NightmareParser.If_stmContext ctx) { 
		LLVMGenerator.ifend();

	}

	//FUNCTION
	@Override 
	public void exitParams(NightmareParser.ParamsContext ctx) { 
		String ID = ctx.ID().getText();
		if (variables.containsKey(ID)) error(ctx.getStart().getLine(), "Redeclaration");
		String type = ctx.TYPE().getText();
		switch (type) {
			case "Tank":	//int
				LLVMGenerator.functionstart(ID, "i32");
				variables.put(ID, VarType.INT_FUN);
				break;
			case "Daisy":	//Float
				LLVMGenerator.functionstart(ID, "double");
				variables.put(ID, VarType.FLOAT_FUN);
				break;
			default:
				error(ctx.getStart().getLine(),":" + type + ":Unknown type");
				break;
		}
	}

	@Override 
	public void exitFunction_declaration(NightmareParser.Function_declarationContext ctx) { 
		String ID = ctx.params().ID().getText();
		VarType type = variables.get(ID);
		switch (type) {
			case INT_FUN:
				LLVMGenerator.load_var(ID,"i32","%");
				LLVMGenerator.functionend("i32");
				break;
			case FLOAT_FUN:
				LLVMGenerator.load_var(ID,"double","%");
				LLVMGenerator.functionend("double");
				break;
			default:
				break;
		}

	}

	
	//TYPE

	@Override 
	public void exitInt(NightmareParser.IntContext ctx) { 
        stack.push( new Value(ctx.INT().getText(), VarType.INT ));
	}

    @Override 
    public void exitFloat(NightmareParser.FloatContext ctx) { 
        stack.push( new Value(ctx.FLOAT().getText(), VarType.FLOAT ));
	}

	@Override 
	public void exitId(NightmareParser.IdContext ctx) {
		String ID = ctx.ID().getText();
		VarType type = variables.get(ID);
		if (type != null){
			switch (type) {
				case INT:
					LLVMGenerator.load_var(ID,"i32","%");
					break;
				case FLOAT:
					LLVMGenerator.load_var(ID,"double","%");
					break;
				case STRING:
					if (tables.containsKey(ID)) LLVMGenerator.getTableElement(ID,"0","i8",tables.get(ID));
					else LLVMGenerator.load_var(ID,"i8*","%");
					break;
				case INT_FUN:
					LLVMGenerator.call(ID, "i32");
					break;
				case FLOAT_FUN:
					LLVMGenerator.call(ID, "double");
					break;
				default:
					break;
			}
		}
		else error(ctx.getStart().getLine(), "Variable not exist !");
		stack.push(new Value('%' + String.valueOf(LLVMGenerator.getCurrentReg()-1),type));
	}

	@Override public void exitTable_load(NightmareParser.Table_loadContext ctx) {
		String ID = ctx.table().ID().getText();
		String i = ctx.table().INT().getText();
		if (!variables.containsKey(ID)) error(ctx.getStart().getLine(), "Unknown wariable");
		if (!tables.containsKey(ID)) error(ctx.getStart().getLine(), "Variable is not a table");
		VarType type = variables.get(ID);
		String len = tables.get(ID);
		switch (type) {
			case INT_TBL:
				LLVMGenerator.getTableElement(ID,i,"i32",len);
				LLVMGenerator.load_var(Integer.toString(LLVMGenerator.getCurrentReg()-1), "i32", "%");
				type = VarType.INT;
				break;
			case FLOAT_TBL:
				LLVMGenerator.getTableElement(ID,i,"double",len);
				LLVMGenerator.load_var(Integer.toString(LLVMGenerator.getCurrentReg()-1), "double", "%");
				type = VarType.FLOAT;
			default:
				break;
		}
		stack.push(new Value('%' + String.valueOf(LLVMGenerator.getCurrentReg()-1),type));
	}
	
	//ARITMETIC OPERATION

    @Override
    public void exitAdd(NightmareParser.AddContext ctx) { 
        Value v1 = stack.pop();
        Value v2 = stack.pop();
        if (v1.type == v2.type) {
            switch (v1.type) {
                case INT:
                    LLVMGenerator.add_i32(v1.name, v2.name);
                    stack.push(new Value("%"+(LLVMGenerator.reg-1), VarType.INT));
                    break;
				case FLOAT:
					LLVMGenerator.add_float(v1.name, v2.name);
					stack.push(new Value("%"+(LLVMGenerator.reg-1), VarType.FLOAT));
                    break;
            
                default:
                    error(ctx.getStart().getLine(), "type mismatch");
                    break;
            }
        }
	}

	@Override 
	public void exitSub(NightmareParser.SubContext ctx) {
		Value v2 = stack.pop();
		Value v1 = stack.pop();
		if (v1.type == v2.type) {
            switch (v1.type) {
                case INT:
                    LLVMGenerator.sub_i32(v1.name, v2.name);
                    stack.push(new Value("%"+(LLVMGenerator.reg-1), VarType.INT));
                    break;
				case FLOAT:
					LLVMGenerator.sub_float(v1.name, v2.name);
					stack.push(new Value("%"+(LLVMGenerator.reg-1), VarType.FLOAT));
                    break;
            
                default:
                    error(ctx.getStart().getLine(), "type mismatch");
                    break;
            }
        }
	}

	@Override 
	public void exitMul(NightmareParser.MulContext ctx) {
		Value v1 = stack.pop();
		Value v2 = stack.pop();
		if (v1.type == v2.type) {
            switch (v1.type) {
                case INT:
                    LLVMGenerator.mult_i32(v1.name, v2.name);
                    stack.push(new Value("%"+(LLVMGenerator.reg-1), VarType.INT));
                    break;
				case FLOAT:
					LLVMGenerator.mult_float(v1.name, v2.name);
					stack.push(new Value("%"+(LLVMGenerator.reg-1), VarType.FLOAT));
                    break;
            
                default:
                    error(ctx.getStart().getLine(), "type mismatch");
                    break;
            }
        }
	}

	@Override 
	public void exitDiv(NightmareParser.DivContext ctx) {
		Value v1 = stack.pop();
		Value v2 = stack.pop();
		if (v1.type == v2.type) {
            switch (v1.type) {
                case INT:
                    LLVMGenerator.div_i32(v1.name, v2.name);
                    stack.push(new Value("%"+(LLVMGenerator.reg-1), VarType.INT));
                    break;
				case FLOAT:
					LLVMGenerator.div_float(v1.name, v2.name);
					stack.push(new Value("%"+(LLVMGenerator.reg-1), VarType.FLOAT));
                    break;
            
                default:
                    error(ctx.getStart().getLine(), "type mismatch");
                    break;
            }
        }
	}



	//VARIABLES
	@Override 
	public void exitVariable_Init(NightmareParser.Variable_InitContext ctx) {
		String ID = ctx.ID().getText();
		if (variables.containsKey(ID)) error(ctx.getStart().getLine(), "Redeclaration");
		String type = ctx.TYPE().getText();
		switch (type) {
			case "Tank":	//int
				LLVMGenerator.declare_i32(ID);
				variables.put(ID, VarType.INT);
				break;
			case "Daisy":	//Float
				LLVMGenerator.declare_float(ID);
				variables.put(ID, VarType.FLOAT);
				break;
			case "Fork":	//String
				LLVMGenerator.declare_string(ID);
				variables.put(ID, VarType.STRING);
				break;
			default:
				error(ctx.getStart().getLine(),":" + type + ":Unknown type");
				break;
		}

	}

	@Override 
	public void exitTable_Init(NightmareParser.Table_InitContext ctx) { 
		String ID = ctx.table().ID().getText();
		if (variables.containsKey(ID)) error(ctx.getStart().getLine(), "Redeclaration");
		String type = ctx.TYPE().getText();
		String size = ctx.table().INT().getText();
		switch (type) {
			case "Tank":	//int
				LLVMGenerator.declare_table(ID, size, "i32");
				variables.put(ID, VarType.INT_TBL);
				break;
			case "Daisy":	//Float
				LLVMGenerator.declare_table(ID, size, "double");
				variables.put(ID, VarType.FLOAT_TBL);
				break;
			case "Fork":
				LLVMGenerator.declare_table(ID,size, "i8");
				variables.put(ID, VarType.STRING);
			default:
				break;
		}
		tables.put(ID, size);
	}

	@Override 
	public void enterAss_init(NightmareParser.Ass_initContext ctx) { 
		String ID = ctx.assign_stm().ID().getText();
		if (variables.containsKey(ID)) error(ctx.getStart().getLine(), "Redeclaration");
		String type = ctx.TYPE().getText();
		switch (type) {
			case "Tank":	//int
				LLVMGenerator.declare_i32(ID);
				variables.put(ID, VarType.INT);
				break;
			case "Daisy":	//Float
				LLVMGenerator.declare_float(ID);
				variables.put(ID, VarType.FLOAT);
				break;
			default:
				error(ctx.getStart().getLine(), "Unknown type");
				break;
		}

	}

    @Override 
    public void exitAssign_stm(NightmareParser.Assign_stmContext ctx) {
		String ID = ctx.ID().getText();
		if (!variables.containsKey(ID)) error(ctx.getStart().getLine(), "Unknown variable");
		Value v = stack.pop();
		switch (v.type) {
			case INT:
			case INT_FUN:
				LLVMGenerator.assign_i32(ID, v.name);
				break;
			case FLOAT:
			case FLOAT_FUN:
				LLVMGenerator.assign_float(ID, v.name);
				break;
			default:
				break;
		}
	}

	@Override public void exitAssign_table_stm(NightmareParser.Assign_table_stmContext ctx) { 
		String ID = ctx.table().ID().getText();
		if (!variables.containsKey(ID)) error(ctx.getStart().getLine(), "Unknown variable");
		if (!tables.containsKey(ID)) error(ctx.getStart().getLine(), "This is not a table");
		VarType type = variables.get(ID);
		String size = tables.get(ID);
		String i = ctx.table().INT().getText();
		Value v = stack.pop();
		switch (type) {
			case INT_TBL:
				LLVMGenerator.table_assign(ID,i,"i32",size, v.name);
				break;
			case FLOAT_TBL:
				LLVMGenerator.table_assign(ID,i,"double",size, v.name);
				break;
			default:
				break;
		}
	}

	@Override 
	public void exitAssign_string(NightmareParser.Assign_stringContext ctx) { 
		String ID = ctx.ID().getText();
		String str = ctx.STRING().getText().replace("\"","");
		if (!variables.containsKey(ID)) error(ctx.getStart().getLine(), "Unknown variable");
		LLVMGenerator.string_assign(ID, str);
	}

	// I/O OPERATIONS


    @Override
    public void exitWrite_stm(NightmareParser.Write_stmContext ctx) { 
		Value v = stack.pop();
		switch (v.type) {
			case INT:
			case INT_FUN:
				LLVMGenerator.printf_i32(v.name);
				break;
			case FLOAT:
			case FLOAT_FUN:
				LLVMGenerator.printf_float(v.name);
				break;
			case STRING:
				LLVMGenerator.printf_string(v.name);
				break;
			default:
				break;
		}
	}
	
	@Override 
	public void exitRead_stm(NightmareParser.Read_stmContext ctx) { 
		String ID = ctx.ID().getText();
		if (!variables.containsKey(ID)) error(ctx.getStart().getLine(), "Unknown variable");
		VarType type = variables.get(ID);
		switch (type) {
			case INT:
				LLVMGenerator.scanf_i32(ID);
				break;
			case FLOAT:
				LLVMGenerator.scanf_float(ID);
				break;
			case STRING:
				LLVMGenerator.getTableElement(ID,"0","i8",tables.get(ID));
				LLVMGenerator.scanf_string(Integer.toString(LLVMGenerator.getCurrentReg()-1));
			default:
				break;
		}
	}


    void error(int line, String msg){
    System.err.println("Error, line "+line+", "+msg);
    System.exit(1);
    } 
}
