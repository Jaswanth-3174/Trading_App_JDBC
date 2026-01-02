package util;

import java.util.Scanner;
import java.util.InputMismatchException;

public class InputHandler {

    private static Scanner scanner = new Scanner(System.in);

//    public InputHandler(Scanner sc){
//        scanner = sc;
//    }

    public static int getInteger(String string){
        while (true){
            System.out.print(string);
            try {
                int num = scanner.nextInt();
                scanner.nextLine();
                if(isNegative(num)){
                    System.out.println("Input can't be negative!");
                }
                else return num;
            }catch (InputMismatchException e){
                System.out.println("Invalid input! Enter integer");
                scanner.nextLine();
            }
        }
    }

    public static String getString(String string){
        while (true){
            System.out.print(string);
            String input = scanner.nextLine();
            if(input.isEmpty()){
                System.out.println("Input cannot be empty!");
            }else{
                return input;
            }
        }
    }

    public static double getDouble(String string){
        while(true){
            System.out.print(string);
            try{
                double d = scanner.nextDouble();
                scanner.nextLine();
                if(isNegative(d)){
                    System.out.println("Input can't be negative!");
                }
                else return d;
            }catch (InputMismatchException e){
                System.out.println("Invalid input! Enter Double/Integer");
                scanner.nextLine();
            }
        }
    }

    public static boolean isNegative(int i){
        if(i < 0){
            return true;
        }
        return false;
    }

    public static boolean isNegative(double d){
        if(d < 0.0){
            return true;
        }
        return false;
    }

    public static int getPositiveInteger(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Input cannot be empty!");
                    continue;
                }
                int num = Integer.parseInt(input);
                if (num <= 0) {
                    System.out.println("Value must be positive!");
                    continue;
                }
                return num;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid integer.");
            }
        }
    }

    public static double getPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Input cannot be empty!");
                    continue;
                }
                double num = Double.parseDouble(input);
                if (num <= 0) {
                    System.out.println("Value must be positive!");
                    continue;
                }
                return num;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
            }
        }
    }

    public static boolean getYesNo(String s) {
        char c = s.charAt(0);
        if(c == 'y' || c == 'Y'){
            return true;
        }
        return false;
    }
}
