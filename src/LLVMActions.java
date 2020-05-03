import java.util.HashMap;
import java.util.Stack;

enum VarType{ INT, FLOAT, STRING, INT_TBL, FLOAT_TBL}

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
	HashMap<String, String> tables = new HashMap<String, String>();
    Stack <Value> stack = new Stack<Value>();

    @Override 
    public void exitNightmare(NightmareParser.NightmareContext ctx) { 
        System.out.println(LLVMGenerator.generate());
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
		switch (type) {
			case INT:
				LLVMGenerator.load_var(ID,"i32");
				break;
			case FLOAT:
				LLVMGenerator.load_var(ID,"double");
			case STRING:
				LLVMGenerator.load_var(ID,"i8*");
			default:
				break;
		}
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
				type = VarType.INT;
				break;
			case FLOAT_TBL:
				LLVMGenerator.getTableElement(ID,i,"double",len);
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
				LLVMGenerator.assign_i32(ID, v.name);
				break;
			case FLOAT:
				LLVMGenerator.assign_float(ID, v.name);
			case STRING:

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
				LLVMGenerator.printf_i32(v.name);
				break;
			case FLOAT:
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
				LLVMGenerator.scanf_string(ID);
			default:
				break;
		}
	}


    void error(int line, String msg){
    System.err.println("Error, line "+line+", "+msg);
    System.exit(1);
    } 
}
