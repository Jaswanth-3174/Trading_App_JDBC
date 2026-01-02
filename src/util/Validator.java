package util;

public class Validator {

    public static boolean validateUserName(String userName){
        if(userName.length()<3){
            System.out.println("User name minimum of length 3 required");
            return false;
        }
        for(char c : userName.toCharArray()){
            if( (c>='a' && c<='z') || (c>='A' && c<='Z')){
            }else{
                System.out.println("User name can contain only alphabets");
                return false;
            }
        }
        return true;
    }

    public static boolean validatePassword(String password){
        if(password.length() < 8 || password.length() > 15){
            System.out.println("Password should be of length 8 to 15");
            return false;
        }
        int upperCaseCount = 0, lowerCaseCount = 0, digitCount = 0, specialCharacterCount = 0;
        for(char c : password.toCharArray()){
            if(c >= 'a' && c <= 'z') lowerCaseCount++;
            else if(c>='A' && c<='Z') upperCaseCount++;
            else if(Character.isDigit(c)) digitCount++;
            else specialCharacterCount++;
        }
        if(upperCaseCount == 0){
            System.out.println("Password doesn't contain any Upper case character");
            return false;
        }
        if(lowerCaseCount == 0){
            System.out.println("Password doesn't contain any Lower case character");
            return false;
        }
        if(digitCount == 0){
            System.out.println("Password doesn't contain any Digit");
            return false;
        }
        if(specialCharacterCount == 0){
            System.out.println("Password doesn't contain special case character");
            return false;
        }
        return true;
    }

    public static boolean validatePanNumber(String panNumber){
        if(panNumber.length() != 7){
            System.out.println("Invalid PAN Number");
            return false;
        }
        for(int i=0; i<7; i++){
            char c = panNumber.charAt(i);
            if(i==4 || i==5){
                if(c >= '0' && c <= '9'){
                }else{
                    System.out.println("Invalid PAN Number");
                    return false;
                }
            }else{
                if(c >= 'A' && c <= 'Z'){
                }else{
                    System.out.println("Invalid PAN Number");
                    return false;
                }
            }
        }
        return true;
    }
}