package zsombi.main;

public class StringToInt {

    public int generateInt(String strInput){
        int outInt = 0;
        String s = strInput;

        int start = s.indexOf("x") + 1;
        int end = s.lastIndexOf("%");

        if (end > start) {
           String strToInt = s.substring(start, end);
           outInt = Integer.parseInt(strToInt);
        }

        return outInt;
    }

}
