package pt.ulisboa.tecnico.cnv.javassist.apps;

public class Fibonacci {
  public static void main(String[] args) {
    int i = 0;
    int current = 0;
    int prev = 1;
    int prevprev = 0;
    for(;i < 20; i++) {
      current = prev + prevprev;
      System.out.print(current + " ");
      prevprev = prev;
      prev = current;
    }
    System.out.println();
  }
}
