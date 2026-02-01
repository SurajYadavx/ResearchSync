public class RTO_System {
    static int counter;
    private String registrationNo;
    private String ownerName;
    private String vehicleType;
    private int engineCC;

    static {
        counter = 7000;
    }

    RTO_System(String ownername, String vehicletype, int enginecc) {
        this.engineCC = enginecc;
        this.vehicleType = vehicletype;
        this.ownerName = ownername;
        this.registrationNo = "MH" + (++counter);

    }

    static int getcounter() {
        return counter;
    }

    public void displaydetails() {
        System.out.println("Reg NO. : " + registrationNo + ", Owner: " + ownerName + ", Type: " + vehicleType
                + ", Engine: " + engineCC);
    }

    static String findPairByEngineCC(RTO_System[] vehicle, int targetCC) {
        for (int i = 0; i < vehicle.length ; i++){
            for (int j = i+1 ; j < vehicle.length ; j++){
                if (vehicle[i] + vehicle[j] == targetCC){
                    return "found : " + 
                }
            }
        }
        return "no match found";
    }

    public static void main(String[] args) {
        RTO_System r1 = new RTO_System("kaliya", "car", 2000);
        RTO_System r2 = new RTO_System("kirmada", "Sidan", 550);
        RTO_System r3 = new RTO_System("chutaki", "bike", 1450);

        findPairByEngineCC(new RTO_System["raj", 1000], 2000);
        findPairByEngineCC(, 5000);

        r1.displaydetails();
        r2.displaydetails();
        r3.displaydetails();
    }
}

/*
 * Level 1: Basic Understanding
 * Q1: What is the difference between static int counter and String ownerName in
 * terms of memory?
 * 
 * ans - static int is the integar type parameter were the owener name is the
 * string and having diff memory size for storig like 1 bit and 4 bits
 * respectively
 * 
 * Q2: Why do we use ++counter instead of counter++ when generating registration
 * numbers?
 * 
 * ans - cause we need increment before the use by 1 so that why if we use this
 * after then the desired value that we want we get this in 2nd execution
 * 
 * Q3: What does this.ownerName = ownerName; mean? Why use this?
 * 
 * ans - this keyword tell that we are ssigning new inserted value to the this,
 * parameter where we want to save
 * 
 * Q4: What is the purpose of a static block? When does it execute?
 * 
 * ans - i dont know this type of lots of terms
 * 
 * Q5: Why make data members private instead of public?
 * 
 * ans - to protect them (i know only thid much )
 */

/*
 * Level 2: Intermediate Understanding
 * Q6: If I create 5 Vehicle objects, how many times does the constructor run?
 * How many times does the static block run?
 * 
 * ans - same as times of object is creater ie. 5
 * 
 * 
 * Q7: In your pair search method, why use j = i + 1 instead of j = 0?
 * 
 * ans - cause we need to get sun of next elment from the list not the same
 * items from the list
 * 
 * Q8: What's the difference between getCounter() being static vs non-static?
 * How would you call each?
 * 
 * ans - i dont know this type of more concept alslo
 * 
 * Q9: Explain what happens step-by-step when this line runs:
 * 
 * java
 * Vehicle v1 = new Vehicle("Raj", "Car", 1200);
 * 
 * this create the new object of class vehicle of names v1 and insert the 3
 * values into this
 * 
 * Q10: What is encapsulation? Give one example from your Vehicle class.
 * 
 * ans - basically it work to hide the data or protect the imfo.
 * 
 * we ahev use this while making parameres are private String ownerName;
 */

/*
 * Level 3: Deep Understanding
 * Q11: If I accidentally wrote static String ownerName; instead of String
 * ownerName;, what problem would occur?
 * 
 * ans - im not good at this part also
 * 
 * Q12: What exception might occur in your pair search if you pass an empty
 * array? How would you handle it?
 * 
 * ans - as thhis having length 0 so we return only an empty array
 * 
 * Q13: Explain the difference between method overloading and method overriding
 * with one example each.
 * 
 * 
 * 
 * as - its basically means taking the same methods from the parent class to
 * the child class by using super keyword
 * 
 * Q14: What is inheritance? If I create a class Car extends Vehicle, what does
 * Car automatically get?
 * 
 * ans - inheritance means inherith the all the properties form the parent class
 * to the may multiple class using extend keyword.
 * car is the child class whi get the all the propetires from parent class
 * vehicle
 * 
 * Q15: What are the four pillars of OOP? Name and give one-line definition of
 * each.
 * 
 * - polymorphism - is state that we can write methos in many form by just
 * chwnge parameter with same method name
 * - encapsulation - it is use to hide sensitive data
 * - inheritance - use to inherite properties between the classes
 * - abstractio - is hide the sensitive data and just show only require data to
 * the user
 */

/*
 * Level 4: Code Analysis (Look at this code and answer)
 * java
 * public class Test {
 * static int x = 10;
 * int y = 20;
 * 
 * static {
 * x = 100;
 * System.out.println("Static block: x = " + x);
 * }
 * 
 * public Test() {
 * y = 200;
 * System.out.println("Constructor: y = " + y);
 * }
 * 
 * public static void main(String[] args) {
 * System.out.println("Main starts");
 * Test t1 = new Test();
 * Test t2 = new Test();
 * System.out.println("x = " + x + ", t1.y = " + t1.y + ", t2.y = " + t2.y);
 * }
 * }
 * Q16: What is the exact output of this program? Write line by line.
 * ans - x= 10, t1.y = 200, t2.y = 200
 * Q17: If I add x = 50; inside the constructor, what will be the final value of
 * x after creating t1 and t2?
 * ans -
 * Q18: Why can I write Test.x but not Test.y?
 * 
 * Level 5: Exception Handling
 * Q19: Write 3 lines of code to demonstrate ArrayIndexOutOfBoundsException with
 * try-catch.
 * ans - try{(sout(array[index]))
 * }catch(ArrayIndexOutOfBoundsException){
 * sout("array is out of bound ")}
 * Q20: What's the difference between throw and throws keywords?
 * 
 */