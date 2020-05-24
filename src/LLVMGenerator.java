import java.util.Stack;

class LLVMGenerator{
   
	static String header_text = "";
	static String main_text = "";
	static int reg = 1;
	static int br = 0;

	static Stack<Integer> brstack = new Stack<Integer>();

	static int getCurrentReg(){
		return reg;
	}

	static void load_var(String id, String type){
		main_text += "%" + reg + " = load " + type + ", " + type + "* %" + id + "\n";
		reg++;
	}

	static void table_assign(String id, String i, String type, String max, String val){
		main_text += "%" + reg + " = getelementptr inbounds ["+max+" x "+type+"], ["+max+" x "+type+"]* %"+id+", i64 0, i64 "+ i + "\n";
		reg++;
		main_text +="store "+ type +" "+ val + ", " + type + "* %" + (reg-1) + "\n";
	}

	static void string_assign(String id, String str){
		int len = (str.length()+1);
		header_text += "@." + id+" = private unnamed_addr constant ["+ len + " x i8] c\""+str+"\\00\"\n";
		main_text += "store i8* getelementptr inbounds (["+len+" x i8], ["+len+" x i8]* @."+id+", i64 0, i64 0), i8** %"+id+"\n";

	}



	

	static void getTableElement(String id, String i, String type, String max){
		main_text += "%" + reg + " = getelementptr inbounds ["+max+" x "+type+"], ["+max+" x "+type+"]* %"+id+", i64 0, i64 "+ i + "\n";
		reg++;
		//main_text += "%" + reg + " = load " + type + ", " + type + "* %" + Integer.toString(reg-1) + "\n";
		//reg++;
	}


    static void printf_i32(String id){
		//main_text += "%"+reg+" = load i32, i32* %"+id+"\n";
		//reg++;
		main_text += "%"+reg+" = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @pstr_int, i32 0, i32 0), i32 %"+(reg-1)+")\n";
		reg++;
	}

	static void printf_float(String id){
		//main_text += "%"+reg+" = load double, double* %"+id+"\n";
		//reg++;
		main_text += "%"+reg+" = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([5 x i8], [5 x i8]* @pstr_dbl, i32 0, i32 0), double %"+(reg-1)+")\n";
		reg++;
	}

	static void printf_string(String id){
		main_text += "%"+reg+" = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @pstr_str, i32 0, i32 0), i8* %"+(reg-1)+")\n";
		reg++;
	}


	static void scanf_i32(String id){
		main_text += "%"+reg+" = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @rstr_int, i32 0, i32 0), i32* %" + id + ")\n";
		reg++;      
	}
  
	static void scanf_float(String id){
		main_text += "%"+reg+" = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @rstr_dbl, i32 0, i32 0), double* %" + id + ")\n";
		reg++;      
	}

	static void scanf_string(String id){
		main_text += "%"+reg+" = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @rstr_str, i32 0, i32 0), i8* %" + id+ ")\n";
		reg++;    
	}


    static void add_i32(String val1, String val2){
    main_text += "%"+reg+" = add i32 "+val1+", "+val2+"\n";
    reg++;
	}

	static void add_float(String val1, String val2){
		main_text += "%"+reg+" = fadd double "+val1+", "+val2+"\n";
		reg++;
	}

	static void sub_i32(String val1, String val2){
		main_text += "%"+reg+" = sub nsw i32 "+val1+", "+val2+"\n";
		reg++;
	}
  
	static void sub_float(String val1, String val2){
		main_text += "%"+reg+" = fsub double "+val1+", "+val2+"\n";
		reg++;
	}

	static void mult_i32(String val1, String val2){
		main_text += "%"+reg+" = mul i32 "+val1+", "+val2+"\n";
		reg++;
	}
  
	static void mult_float(String val1, String val2){
		main_text += "%"+reg+" = fmul double "+val1+", "+val2+"\n";
		reg++;
	}
  
	static void div_i32(String val1, String val2){
		main_text += "%"+reg+" = udiv i32 "+val1+", "+val2+"\n";
		reg++;
	}
  
	static void div_float(String val1, String val2){
		main_text += "%"+reg+" = fdiv double "+val1+", "+val2+"\n";
		reg++;
	}

	static void declare_i32(String id){
		main_text += "%"+id+" = alloca i32\n";
	}
  
	static void declare_float(String id){
		main_text += "%"+id+" = alloca double\n";
	}

	static void declare_string(String id){
		main_text += "%"+id+" = alloca i8*\n";
	}

	static void declare_table(String id, String n, String type){
		main_text += "%"+id+" = alloca [ "+ n + " x " + type + " ] \n";
	}

	static void assign_i32(String id, String value){
		main_text += "store i32 "+value+", i32* %"+id+"\n";
	}
  
	static void assign_float(String id, String value){
		main_text += "store double "+value+", double* %"+id+"\n";
	}

	static void repeatstart(String repetitions){
		declare_i32(Integer.toString(reg));
		int counter = reg;
		reg++;
		assign_i32(Integer.toString(counter), "0");
		br++;
		main_text += "br label %cond"+br+"\n";
		main_text += "cond"+br+":\n";
		load_var(Integer.toString(counter),"i32");
		add_i32("%" + (reg-1), "1");
		assign_i32(Integer.toString(counter), "%"+(reg-1));

		main_text += "%"+reg+" = icmp slt i32 %"+(reg-2)+", "+repetitions+"\n";
		reg++;
		main_text += "br i1 %"+(reg-1)+", label %true"+br+", label %false"+br+"\n";
		main_text += "true"+br+":\n";
		brstack.push(br);
	}

	static void repeatend(){
		int b = brstack.pop();
		main_text += "br label %cond"+b+"\n";
		main_text += "false"+b+":\n";
	}

	static void ifstart(){
		br++;
		main_text += "br i1 %"+(reg-1)+", label %true"+br+", label %false"+br+"\n";
		main_text += "true"+br+":\n";
		brstack.push(br);
	}

	static void ifend(){
		int b = brstack.pop();
		main_text += "br label %false"+b+"\n";
		main_text += "false"+b+":\n";
	}

	static void icmp(String v1, String v2){
		main_text += "%"+reg+" = icmp eq i32 "+v1+", "+v2+"\n";
		reg++;
	}

	static void fcmp(String v1, String v2){
		main_text += "%"+reg+" = fcmp oeq double "+v1+", "+v2+"\n";
		reg++;
	}


	/*static void icmp(String v1, String v1, String type){
		main_text += "%"+reg+" = load i32, i32* %"+id+"\n";
		reg++;
		main_text += "%"+reg+" = icmp eq i32 %"+(reg-1)+", "+value+"\n";
		reg++;
	}*/

 
 
    static String generate(){
    	String text = "";
		text += "declare i32 @printf(i8*, ...)\n";
		text += "declare i32 @scanf(i8*, ...)\n";
		text += "@rstr_dbl = constant [4 x i8] c\"%lf\\00\", align 1\n";
		text += "@pstr_dbl = constant [5 x i8] c\"%lf\\0A\\00\", align 1\n";
		text += "@rstr_int = constant [3 x i8] c\"%d\\00\", align 1\n";
		text += "@pstr_int = constant [4 x i8] c\"%d\\0A\\00\", align 1\n";
		text += "@pstr_str = constant [4 x i8] c\"%s\\0A\\00\", align 1\n";
		text += "@rstr_str = constant [3 x i8] c\"%s\\00\", align 1\n";
    	text += header_text;
		text += "define i32 @main() nounwind{\n";
    	text += main_text;
		text += "ret i32 0 }\n";
    	return text;
    }
 
}
 