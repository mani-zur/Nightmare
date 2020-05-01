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
		Value v1 = stack.pop();
		Value v2 = stack.pop();
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
			default:
				error(ctx.getStart().getLine(), "Unknown type");
				break;
		}

	}

	@Override 
	public void exitTable_Init(NightmareParser.Table_InitContext ctx) { 
		String ID = ctx.ID().getText();
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
			case "Fork":	//c_string
				LLVMGenerator.declare_table(ID, size, "i8");
				variables.put(ID, VarType.STRING);
			default:
				break;
		}
	}

    @Override 
    public void exitAssign_stm(NightmareParser.Assign_stmContext ctx) {
		String ID = ctx.ID().getText();
		if (!variables.containsKey(ID)) error(ctx.getStart().getLine(), "Unknown vaariable");
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

	// I/O OPERATIONS


    @Override
    public void exitWrite_stm(NightmareParser.Write_stmContext ctx) { 
		String ID = ctx.ID().getText();
		if (!variables.containsKey(ID)) error(ctx.getStart().getLine(), "Unknown vaariable");
		VarType type = variables.get(ID);
		switch (type) {
			case INT:
				LLVMGenerator.printf_i32(ID);
				break;
			case FLOAT:
				LLVMGenerator.printf_float(ID);
			default:
				break;
		}
	}
	
	@Override 
	public void exitRead_stm(NightmareParser.Read_stmContext ctx) { 
		String ID = ctx.ID().getText();
		if (!variables.containsKey(ID)) error(ctx.getStart().getLine(), "Unknown vaariable");
		VarType type = variables.get(ID);
		switch (type) {
			case INT:
				LLVMGenerator.scanf_i32(ID);
				break;
			case FLOAT:
				LLVMGenerator.scanf_float(ID);
				break;
			default:
				break;
		}
	}


    void error(int line, String msg){
    System.err.println("Error, line "+line+", "+msg);
    System.exit(1);
    } 

    

}
