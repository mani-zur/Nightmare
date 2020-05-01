class LLVMGenerator{
   
	static String header_text = "";
	static String main_text = "";
	static int reg = 1;


    static void printf_i32(String id){
		main_text += "%"+reg+" = load i32, i32* %"+id+"\n";
		reg++;
		main_text += "%"+reg+" = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @pstr_int, i32 0, i32 0), i32 %"+(reg-1)+")\n";
		reg++;
	}

	static void printf_float(String id){
		main_text += "%"+reg+" = load double, double* %"+id+"\n";
		reg++;
		main_text += "%"+reg+" = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([5 x i8], [5 x i8]* @pstr_dbl, i32 0, i32 0), double %"+(reg-1)+")\n";
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



    static void add_i32(String val1, String val2){
    main_text += "%"+reg+" = add i32 "+val1+", "+val2+"\n";
    reg++;
	}

	static void add_float(String val1, String val2){
		main_text += "%"+reg+" = fadd double "+val1+", "+val2+"\n";
		reg++;
	}

	static void sub_i32(String val1, String val2){
		main_text += "%"+reg+" = sub i32 "+val1+", "+val2+"\n";
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

	static void declare_table(String id, String n, String type){
		main_text += "%"+id+" = alloca [ "+ n + " x " + type + " ] \n";
	}

	static void assign_i32(String id, String value){
		main_text += "store i32 "+value+", i32* %"+id+"\n";
	}
  
	static void assign_float(String id, String value){
		main_text += "store double "+value+", double* %"+id+"\n";
	}
 
 
    static String generate(){
    	String text = "";
		text += "declare i32 @printf(i8*, ...)\n";
    	text += "declare i32 @scanf(i8*, ...)\n";
		text += "@rstr_dbl = constant [4 x i8] c\"%lf\\00\", align 1\n";
		text += "@pstr_dbl = constant [5 x i8] c\"%lf\\0A\\00\", align 1\n";
		text += "@rstr_int = constant [3 x i8] c\"%d\\00\", align 1\n";
		text += "@pstr_int = constant [4 x i8] c\"%d\\0A\\00\", align 1\n";
    	text += header_text;
    	text += "define i32 @main() nounwind{\n";
    	text += main_text;
    	text += "ret i32 0 }\n";
    	return text;
    }
 
}
 