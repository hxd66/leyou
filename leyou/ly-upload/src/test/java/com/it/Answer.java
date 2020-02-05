package com.it;


public class Answer {
   /* public static String solution(String str){
        if( str == null||str.length()<2) return str;
        int pointer=0;
        boolean[] char_set = new boolean[256];
        while(pointer<str.length()){
            char value = str.charAt(pointer);
            if(char_set[value]==true){
                str = str.substring(0, pointer) + str.substring(pointer+1);
            } else{
                char_set[value] = true;
                pointer++;
            }
        }
        return str;
    }*/


    public static String solution2(String str){
        if(str == null||str.length()<2) return str;

        for (int i=0; i<str.length()-1; i++){
            int j=i+1;
            while (j<str.length()){
                if(str.charAt(i)==str.charAt(j)){
                    if(j<str.length()-1) str = str.substring(0, j) + str.substring(j+1);
                    else str = str.substring(0, j);
                } else{
                    j++;
                }
            }
        }
        return str;
    }

    public static void main(String[] args) {
        String s = "dadsdada";
        String answer =solution2(s);
        System.out.print(answer);
    }
}
