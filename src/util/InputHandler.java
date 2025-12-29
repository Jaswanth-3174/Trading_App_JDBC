package util;

import java.util.Scanner;
import java.util.InputMismatchException;

public class InputHandler {

    private Scanner scanner;

    public InputHandler(Scanner scanner){
        this.scanner = scanner;
    }

    public int getInteger(String string){
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

    public String getString(String string){
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

    public double getDouble(String string){
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

    public boolean isNegative(int i){
        if(i < 0){
            return true;
        }
        return false;
    }

    public boolean isNegative(double d){
        if(d < 0.0){
            return true;
        }
        return false;
    }

    // Get menu choice without printing prompt (prompt already printed by menu)
    public int getMenuChoice() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.print("Enter choice: ");
                    continue;
                }
                int choice = Integer.parseInt(input);
                if (choice < 0) {
                    System.out.println("Choice cannot be negative!");
                    System.out.print("Enter choice: ");
                    continue;
                }
                return choice;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
                System.out.print("Enter choice: ");
            }
        }
    }

    // Get positive integer with custom error message
    public int getPositiveInteger(String prompt) {
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

    // Get positive double with custom error message
    public double getPositiveDouble(String prompt) {
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
}
