import java.util.*;

enum VehicleType {
    CAR, BIKE, TRUCK
}

class InsufficientFuelException extends Exception {
    public InsufficientFuelException(String message) {
        super(message);
    }
}

class Trip {
    double distance;
    double fuelUsed;

    Trip(double distance, double fuelUsed) {
        this.distance = distance;
        this.fuelUsed = fuelUsed;
    }

    double getEfficiency() {
        return distance / fuelUsed;
    }
}

class Vehicle {
    String name;
    VehicleType type;
    double fuelExists;
    double totalFuelAdded;
    double fuelPricePerLitre;
    ArrayList<Trip> trips;
    Trip mostEfficientTrip;

    Vehicle(String name, VehicleType type) {
        this.name = name;
        this.type = type;
        this.fuelExists = 0;
        this.totalFuelAdded = 0;
        this.trips = new ArrayList<>();
        this.mostEfficientTrip = null;
    }

    void refillFuel(double fuelAmount, double pricePerLitre) {
        fuelExists += fuelAmount;
        totalFuelAdded += fuelAmount;
        fuelPricePerLitre = pricePerLitre;
        System.out.println("Fuel updated successfully!");
    }

    void addTrip(double distance, double fuelUsed) throws InsufficientFuelException {
        if (fuelUsed > fuelExists) {
            throw new InsufficientFuelException("Not enough fuel! Please refill first.");
        }
        Trip trip = new Trip(distance, fuelUsed);
        trips.add(trip);
        fuelExists -= fuelUsed;
        if (mostEfficientTrip == null || trip.getEfficiency() > mostEfficientTrip.getEfficiency()) {
            mostEfficientTrip = trip;
        }
        System.out.println("Trip added successfully! Fuel left: " + fuelExists + " liters");
    }

    double getOverallEfficiency() {
        double totalDistance = 0, totalFuel = 0;
        for (Trip t : trips) {
            totalDistance += t.distance;
            totalFuel += t.fuelUsed;
        }
        return (totalFuel == 0) ? 0 : totalDistance / totalFuel;
    }

    void displayTrips() {
        if (trips.isEmpty()) {
            System.out.println("No trips recorded for this vehicle.");
            return;
        }
        System.out.println("+-----+-------------+-----------+-------------+");
        System.out.println("| S.No| Distance(km) | FuelUsed  | Efficiency  |");
        System.out.println("+-----+-------------+-----------+-------------+");
        for (int i = 0; i < trips.size(); i++) {
            Trip t = trips.get(i);
            System.out.printf("| %-4d| %-12.2f | %-9.2f | %-11.2f |\n", i + 1, t.distance, t.fuelUsed, t.getEfficiency());
        }
        System.out.println("+-----+-------------+-----------+-------------+");
        if (mostEfficientTrip != null) {
            System.out.printf("Most Efficient Trip: %.2f km/l\n", mostEfficientTrip.getEfficiency());
        }
    }

    void displayVehicleSummary(int sno) {
        System.out.printf("| %-4d | %-15s | %-6s | %-10.2f | %-10.2f | %-9.2f | %-11.2f |\n",
                sno, name, type, fuelExists, totalFuelAdded, fuelPricePerLitre, getOverallEfficiency());
    }
}

class FuelMonitor extends Thread {
    Vehicle vehicle;
    FuelMonitor(Vehicle v) {
        this.vehicle = v;
    }
    public void run() {
        try {
            while (true) {
                if (vehicle.fuelExists < 5) {
                    System.out.println("Warning: Low fuel in vehicle " + vehicle.name + " (" + vehicle.fuelExists + " liters left)");
                }
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            System.out.println("Fuel monitoring stopped for " + vehicle.name);
        }
    }
}

public class FuelEfficiencyTracker {
    static ArrayList<Vehicle> vehicles = new ArrayList<>();
    static Map<String, Vehicle> vehicleMap = new HashMap<>();
    static Scanner sc = new Scanner(System.in);

    public static void displayVehicles() {
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles added yet!");
            return;
        }
        System.out.println("+-----+-----------------+--------+------------+------------+-----------+-------------+");
        System.out.println("| S.No| Name            | Type   | FuelExists | TotalFuel  | Price/L   | Efficiency  |");
        System.out.println("+-----+-----------------+--------+------------+------------+-----------+-------------+");
        for (int i = 0; i < vehicles.size(); i++) {
            vehicles.get(i).displayVehicleSummary(i + 1);
        }
        System.out.println("+-----+-----------------+--------+------------+------------+-----------+-------------+");
    }

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Vehicle Fuel Tracker ---");
            System.out.println("1. Add Vehicle");
            System.out.println("2. Record Trip");
            System.out.println("3. Refill Fuel");
            System.out.println("4. View Vehicle Details");
            System.out.println("5. Show Most Efficient Vehicle");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter vehicle name: ");
                    String name = sc.nextLine();
                    System.out.println("Select Type: 1.CAR 2.BIKE 3.TRUCK");
                    int typeChoice = sc.nextInt();
                    VehicleType type = VehicleType.values()[typeChoice - 1];
                    Vehicle newVehicle = new Vehicle(name, type);
                    vehicles.add(newVehicle);
                    vehicleMap.put(name, newVehicle);
                    new FuelMonitor(newVehicle).start();
                    System.out.println("Vehicle added successfully!");
                    displayVehicles();
                    break;

                case 2:
                    displayVehicles();
                    System.out.print("Enter Vehicle S.No to record trip: ");
                    int tripIndex = sc.nextInt() - 1;
                    if (tripIndex >= 0 && tripIndex < vehicles.size()) {
                        Vehicle v = vehicles.get(tripIndex);
                        try {
                            System.out.print("Distance traveled (km): ");
                            double distance = sc.nextDouble();
                            System.out.print("Fuel used (liters): ");
                            double fuelUsed = sc.nextDouble();
                            v.addTrip(distance, fuelUsed);
                            v.displayTrips();
                        } catch (InsufficientFuelException e) {
                            System.out.println(e.getMessage());
                        }
                    } else System.out.println("Invalid S.No");
                    break;

                case 3:
                    displayVehicles();
                    System.out.print("Enter Vehicle S.No to refill fuel: ");
                    int refIndex = sc.nextInt() - 1;
                    if (refIndex >= 0 && refIndex < vehicles.size()) {
                        Vehicle v = vehicles.get(refIndex);
                        System.out.print("Current fuel (liters): ");
                        double existingFuel = sc.nextDouble();
                        v.fuelExists = existingFuel;
                        System.out.print("Fuel added (liters): ");
                        double fuelAdded = sc.nextDouble();
                        System.out.print("Price per litre: ");
                        double price = sc.nextDouble();
                        v.refillFuel(fuelAdded, price);
                    } else System.out.println("Invalid S.No");
                    displayVehicles();
                    break;

                case 4:
                    displayVehicles();
                    for (int i = 0; i < vehicles.size(); i++) {
                        System.out.println("\nTrips for Vehicle S.No " + (i + 1) + " (" + vehicles.get(i).name + "):");
                        vehicles.get(i).displayTrips();
                    }
                    break;

                case 5:
                    if (!vehicles.isEmpty()) {
                        Vehicle mostEff = vehicles.get(0);
                        for (Vehicle v : vehicles) {
                            if (v.getOverallEfficiency() > mostEff.getOverallEfficiency()) {
                                mostEff = v;
                            }
                        }
                        System.out.println("Most Efficient Vehicle: " + mostEff.name +
                                " | Efficiency: " + String.format("%.2f", mostEff.getOverallEfficiency()) + " km/l");
                    } else System.out.println("No vehicles added!");
                    break;

                case 6:
                    running = false;
                    System.out.println("Exiting tracker. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option! Try again.");
            }
        }
        sc.close();
    }
}
