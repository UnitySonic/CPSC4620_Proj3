package cpsc4620;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;

/*
 * This file is where the front end magic happens.
 * 
 * You will have to write the methods for each of the menu options.
 * 
 * This file should not need to access your DB at all, it should make calls to the DBNinja that will do all the connections.
 * 
 * You can add and remove methods as you see necessary. But you MUST have all of the menu methods (including exit!)
 * 
 * Simply removing menu methods because you don't know how to implement it will result in a major error penalty (akin to your program crashing)
 * 
 * Speaking of crashing. Your program shouldn't do it. Use exceptions, or if statements, or whatever it is you need to do to keep your program from breaking.
 * 
 */

public class Menu {

	public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	public static void main(String[] args) throws SQLException, IOException {
		System.out.println("Welcome to Pizzas-R-Us!");

		int menu_option = 0;

		// present a menu of options and take their selection

		PrintMenu();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String option = reader.readLine();
		menu_option = Integer.parseInt(option);

		while (menu_option != 9) {
			switch (menu_option) {
				case 1:// enter order
					EnterOrder();
					break;
				case 2:// view customers
					viewCustomers();
					break;
				case 3:// enter customer
					EnterCustomer();
					break;
				case 4:// view order
						// open/closed/date
					ViewOrders();
					break;
				case 5:// mark order as complete
					MarkOrderAsComplete();
					break;
				case 6:// view inventory levels
					ViewInventoryLevels();
					break;
				case 7:// add to inventory
					AddInventory();
					break;
				case 8:// view reports
					PrintReports();
					break;
			}
			PrintMenu();
			option = reader.readLine();
			menu_option = Integer.parseInt(option);
		}

	}

	// prompt user to enter yes (y) or no (n) for a choice
	public static String askYesOrNo() throws SQLException, IOException {
		String answer = reader.readLine();
		while (!answer.equals("y") && !answer.equals("n")) {
			System.out.println("Invalid choice. Please enter y or n.");
			answer = reader.readLine();
		}
		return answer;
	}

	// allow for a new order to be placed
	public static void EnterOrder() throws SQLException, IOException {
		/*
		 * EnterOrder should do the following:
		 * 
		 * Ask if the order is delivery, pickup, or dinein
		 * if dine in....ask for table number
		 * if pickup...
		 * if delivery...
		 * 
		 * Then, build the pizza(s) for the order (there's a method for this)
		 * until there are no more pizzas for the order
		 * add the pizzas to the order
		 *
		 * Apply order discounts as needed (including to the DB)
		 * 
		 * return to menu
		 * 
		 * make sure you use the prompts below in the correct order!
		 */

		// Order object fields
		String order_type = "";
		String customer_ID;
		int table_num = 0;

		// Customer address fields
		String customer_street = "";
		String customer_city = "";
		String customer_state = "";
		String customer_zip = "";

		// User Input Prompts...
		System.out.println(
				"Is this order for: \n1.) Dine-in\n2.) Pick-up\n3.) Delivery\nEnter the number of your choice:");
		String option = reader.readLine();
		int order_type_choice = Integer.parseInt(option);
		// Verify that user entered a valid choice
		while (order_type_choice < 1 || order_type_choice > 3) {
			System.out.println("Invalid choice. Please enter a number from 1 to 3.");
			System.out.println(
					"Is this order for: \n1.) Dine-in\n2.) Pick-up\n3.) Delivery\nEnter the number of your choice:");
			option = reader.readLine();
			order_type_choice = Integer.parseInt(option);
		}
		// Set order type
		if (order_type_choice == 1) {
			order_type = DBNinja.dine_in;
			// Dine-in orders need a table number
			System.out.println("What is the table number for this order?");
			option = reader.readLine();
			table_num = Integer.parseInt(option);
			while (table_num <= 0) {
				System.out.println("Table number must be at least 1.");
				System.out.println("What is the table number for this order?");
				option = reader.readLine();
				table_num = Integer.parseInt(option);
			}
		} else {
			// Pickup and delivery orders need a customer
			System.out.println("Is this order for an existing customer? Answer y/n: ");
			option = reader.readLine();
			while (!option.equals("y") && !option.equals("n")) {
				System.out.println("ERROR: I don't understand your input for: Is this order an existing customer?");
				System.out.println("Is this order for an existing customer? Answer y/n: ");
				option = reader.readLine();
			}
			if (option.equals("y")) {
				// Show the current customers
				System.out.println("Here's a list of the current customers: ");
				viewCustomers();
				System.out.println("Which customer is this order for? Enter ID Number:");
				customer_ID = reader.readLine();
			} else {
				// Prompt user to create a new customer
				EnterCustomer();
			}
			if (order_type_choice == 2) {
				order_type = DBNinja.pickup;
			} else {
				order_type = DBNinja.delivery;
				// Delivery orders require a customer's address
				System.out.println("What is the House/Apt Number for this order? (e.g., 111)");
				customer_street = (reader.readLine() + " ");
				System.out.println("What is the Street for this order? (e.g., Smile Street)");
				customer_street += reader.readLine();
				System.out.println("What is the City for this order? (e.g., Greenville)");
				customer_city = reader.readLine();
				System.out.println("What is the State for this order? (e.g., SC)");
				customer_state = reader.readLine();
				System.out.println("What is the Zip Code for this order? (e.g., 20605)");
				customer_zip = reader.readLine();
			}
		}

		// Prompt user to build a pizza
		System.out.println("Let's build a pizza!");
		option = "0";
		while (!option.equals("-1")) {
			buildPizza(100); // how do we get the latest order id?
			System.out.println(
					"Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
			option = reader.readLine();
		}

		System.out.println("Do you want to add discounts to this order? Enter y/n?");
		option = "0";
		while (!option.equals("-1")) {
			// make method to list all discounts
			System.out.println(
					"Which Order Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
			option = reader.readLine();
		}

		// Add code to create order object and create DB record

		System.out.println("Finished adding order...Returning to menu...");
	}

	public static void viewCustomers() throws SQLException, IOException {
		/*
		 * Simply print out all of the customers from the database.
		 */
		ArrayList<Customer> customerList = DBNinja.getCustomerList();
		for (Customer customer : customerList) {
			System.out.println(customer.toString());
		}
	}

	// Enter a new customer in the database
	public static void EnterCustomer() throws SQLException, IOException {
		/*
		 * Ask for the name of the customer:
		 * First Name <space> Last Name
		 * 
		 * Ask for the phone number.
		 * (##########) (No dash/space)
		 * 
		 * Once you get the name and phone number, add it to the DB
		 */

		// Customer object fields
		String name = "";
		String phone = "";

		// User Input Prompts...
		System.out.println("Please Enter the Customer name (First Name <space> Last Name):");
		name = reader.readLine();
		// Use a regex pattern to verify the name is correct
		Pattern check_name = Pattern.compile("^[^\\s]+\\s[^\\s]+$", Pattern.CASE_INSENSITIVE);
		Matcher match_name = check_name.matcher(name);
		// Do not accept invalid names
		while (!match_name.find()) {
			System.out.println("Please Enter the Customer name (First Name <space> Last Name):");
			name = reader.readLine();
			match_name = check_name.matcher(name);
		}

		System.out.println("What is this customer's phone number (##########) (No dash/space)");
		phone = reader.readLine();
		// Use a regex pattern to verify the phone number is correct
		Pattern check_phone = Pattern.compile("^[0-9]{10}$", Pattern.CASE_INSENSITIVE);
		Matcher match_phone = check_phone.matcher(phone);
		// Do not accept invalid phone numbers
		while (!match_phone.find()) {
			System.out.println("What is this customer's phone number (##########) (No dash/space)");
			phone = reader.readLine();
			match_phone = check_phone.matcher(phone);
		}

		// Create a Customer Object
		String[] split_name = name.split(" ", 0);
		String first_name = split_name[0];
		String last_name = split_name[1];
		// The 1 passed in for custID is a dummy value; custID is set in the addCustomer
		// method
		Customer cust = new Customer(1, first_name, last_name, phone);
		DBNinja.addCustomer(cust);
	}

	// View any orders that are not marked as completed
	public static void ViewOrders() throws SQLException, IOException {
		/*
		 * This method allows the user to select between three different views of the
		 * Order history:
		 * The program must display:
		 * a. all open orders
		 * b. all completed orders
		 * c. all the orders (open and completed) since a specific date (inclusive)
		 * 
		 * After displaying the list of orders (in a condensed format) must allow the
		 * user to select a specific order for viewing its details.
		 * The details include the full order type information, the pizza information
		 * (including pizza discounts), and the order discounts.
		 * 
		 */

		// User Input Prompts...
		System.out.println(
				"Would you like to:\n(a) display all orders [open or closed]\n(b) display all open orders\n(c) display all completed [closed] orders\n(d) display orders since a specific date");
		System.out.println("What is the date you want to restrict by? (FORMAT= YYYY-MM-DD)");
		System.out.println("I don't understand that input, returning to menu");
		System.out.println("Which order would you like to see in detail? Enter the number (-1 to exit): ");
		System.out.println("Incorrect entry, returning to menu.");
		System.out.println("No orders to display, returning to menu.");

	}

	// When an order is completed, we need to make sure it is marked as complete
	public static void MarkOrderAsComplete() throws SQLException, IOException {
		/*
		 * All orders that are created through java (part 3, not the orders from part 2)
		 * should start as incomplete
		 * 
		 * When this method is called, you should print all of the "open" orders marked
		 * and allow the user to choose which of the incomplete orders they wish to mark
		 * as complete
		 * 
		 */

		// User Input Prompts...
		System.out.println("There are no open orders currently... returning to menu...");
		System.out.println("Which order would you like mark as complete? Enter the OrderID: ");
		System.out.println("Incorrect entry, not an option");

	}

	public static void ViewInventoryLevels() throws SQLException, IOException {
		/*
		 * Print the inventory. Display the topping ID, name, and current inventory
		 */

	}

	public static void AddInventory() throws SQLException, IOException {
		/*
		 * This should print the current inventory and then ask the user which topping
		 * (by ID) they want to add more to and how much to add
		 */

		// User Input Prompts...
		System.out.println("Which topping do you want to add inventory to? Enter the number: ");
		System.out.println("How many units would you like to add? ");
		System.out.println("Incorrect entry, not an option");

	}

	// A method that builds a pizza. Used in our add new order method
	public static Pizza buildPizza(int orderID) throws SQLException, IOException {
		/*
		 * This is a helper method for first menu option.
		 * 
		 * It should ask which size pizza the user wants and the crustType.
		 * 
		 * Once the pizza is created, it should be added to the DB.
		 * 
		 * We also need to add toppings to the pizza. (Which means we not only need to
		 * add toppings here, but also our bridge table)
		 * 
		 * We then need to add pizza discounts (again, to here and to the database)
		 * 
		 * Once the discounts are added, we can return the pizza
		 */

		Pizza ret = null;
		String size = "";
		String crustType = "";

		Map<Topping, Boolean> toppingsUsed = new HashMap<Topping, Boolean>();
		ArrayList<Discount> discountsList = new ArrayList<Discount>();

		// User Input Prompts...
		System.out.println("What size is the pizza?");
		System.out.println("1." + DBNinja.size_s);
		System.out.println("2." + DBNinja.size_m);
		System.out.println("3." + DBNinja.size_l);
		System.out.println("4." + DBNinja.size_xl);

		// Picking A Pizza Size

		int choice = -1;
		while (choice < 1 || choice > 4) {
			try {
				System.out.println("Enter the corresponding number: ");
				String option = reader.readLine();
				choice = Integer.parseInt(option);
			} catch (Exception e) {
				// Doesn't need to do anything actually
			}
			switch (choice) {
				case 1:
					size = "Small";
					break;
				case 2:
					size = "Medium";
					break;
				case 3:
					size = "Large";
					break;
				case 4:
					size = "XLarge";
					break;
				default:
					break;
			}
		}

		// Picking a Crust Type
		System.out.println("What crust for this pizza?");
		System.out.println("1." + DBNinja.crust_thin);
		System.out.println("2." + DBNinja.crust_orig);
		System.out.println("3." + DBNinja.crust_pan);
		System.out.println("4." + DBNinja.crust_gf);

		choice = -1;
		while (choice < 1 || choice > 4) {
			try {
				System.out.println("Enter the corresponding number: ");
				String option = reader.readLine();
				choice = Integer.parseInt(option);
			} catch (Exception e) {
				// Doesn't need to do anything actually
			}
			switch (choice) {
				case 1:
					crustType = "Thin";
					break;
				case 2:
					crustType = "Original";
					break;
				case 3:
					crustType = "Pan";
					break;
				case 4:
					crustType = "Gluten-Free";
					break;
				default:
					break;
			}
		}

		// Printing Out Available Toppings
		DBNinja.printInventory();

		String inputtedTopID = "";

		while (!inputtedTopID.equals("-1")) {

			System.out.println("Which topping do you want to add? Enter the TopID. Enter -1 to stop adding toppings: ");
			inputtedTopID = reader.readLine();

			Topping selectedTopping = DBNinja.findToppingByID(inputtedTopID);

			if (selectedTopping == null)
				continue;

			String extraRequested = "";
			while (!extraRequested.equals("y") && !extraRequested.equals("n")) {
				System.out.println("Do you want to add extra topping? Enter y/n");
				extraRequested = reader.readLine().toLowerCase();
			}

			boolean requestExtra;
			if (extraRequested.equals("y"))
				requestExtra = true;
			else
				requestExtra = false;

			if (DBNinja.checkIfEnoughTopping(Integer.parseInt(inputtedTopID), size, requestExtra))
				toppingsUsed.put(selectedTopping, requestExtra);
			// Theen Add topping
			else
				System.out.println("We don't have enough of that topping to add it...");
		}

		// Ask if they want a discount
		String discountRequest = "";
		while (!discountRequest.equals("y") && !discountRequest.equals("n")) {
			System.out.println("Do you want to add discounts to this Pizza? Enter y/n?");
			discountRequest = reader.readLine().toLowerCase();
		}

		// Loop for asking which discounts they want
		if (discountRequest.equals("y")) {

			String discountChoice = "";
			while (!discountChoice.equals("-1")) {
				DBNinja.printDiscounts();
				System.out.println(
						"Which Pizza Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
				discountChoice = reader.readLine();
				Discount selectedDiscount = DBNinja.findDiscountByID(discountChoice);
				if (selectedDiscount != null) {
					discountsList.add(selectedDiscount);
				}

			}
		}

		////////////////////////////////////////
		/////////// ADDING FUNCTIONS ///////////
		////////////////////////////////////////
		double baseCost = DBNinja.getBaseBusPrice(size, crustType);
		double basePrice = DBNinja.getBaseCustPrice(size, crustType);

		// Get a Timestamp for the pizza
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timestampString = dateFormat.format(new Date(timestamp.getTime()));

		Pizza thePizza = new Pizza(5, size, crustType, orderID, "Incomplete", timestampString, basePrice,
				baseCost);

		for (Topping t : toppingsUsed.keySet()) {
			thePizza.addToppings(t, toppingsUsed.get(t));
		}

		for (Discount d : discountsList) {
			thePizza.addDiscounts(d);
		}

		DBNinja.addPizza(thePizza);

		return ret;
	}

	public static void PrintReports() throws SQLException, NumberFormatException, IOException {
		/*
		 * This method asks the use which report they want to see and calls the DBNinja
		 * method to print the appropriate report.
		 * 
		 */

		// User Input Prompts...

		System.out.println(
				"Which report do you wish to print? Enter\n(a) ToppingPopularity\n(b) ProfitByPizza\n(c) ProfitByOrderType:");
		String choice = reader.readLine();
		if (!choice.equals("a") && !choice.equals("b") && !choice.equals("c")) {
			System.out.println("I don't understand that input... returning to menu...");
			return;
		}
		if (choice.equals("a")) {
			DBNinja.printToppingPopReport();
		} else if (choice.equals("b")) {
			DBNinja.printProfitByPizzaReport();
		} else {
			DBNinja.printProfitByOrderType();
		}

	}

	// Prompt - NO CODE SHOULD TAKE PLACE BELOW THIS LINE
	// DO NOT EDIT ANYTHING BELOW HERE, THIS IS NEEDED TESTING.
	// IF YOU EDIT SOMETHING BELOW, IT BREAKS THE AUTOGRADER WHICH MEANS YOUR GRADE
	// WILL BE A 0 (zero)!!

	public static void PrintMenu() {
		System.out.println("\n\nPlease enter a menu option:");
		System.out.println("1. Enter a new order");
		System.out.println("2. View Customers ");
		System.out.println("3. Enter a new Customer ");
		System.out.println("4. View orders");
		System.out.println("5. Mark an order as completed");
		System.out.println("6. View Inventory Levels");
		System.out.println("7. Add Inventory");
		System.out.println("8. View Reports");
		System.out.println("9. Exit\n\n");
		System.out.println("Enter your option: ");
	}

	/*
	 * autograder controls....do not modiify!
	 */

	public final static String autograder_seed = "6f1b7ea9aac470402d48f7916ea6a010";

	private static void autograder_compilation_check() {

		try {
			Order o = null;
			Pizza p = null;
			Topping t = null;
			Discount d = null;
			Customer c = null;
			ArrayList<Order> alo = null;
			ArrayList<Discount> ald = null;
			ArrayList<Customer> alc = null;
			ArrayList<Topping> alt = null;
			double v = 0.0;
			String s = "";

			DBNinja.addOrder(o);
			DBNinja.addPizza(p);
			DBNinja.useTopping(p, t, false);
			DBNinja.usePizzaDiscount(p, d);
			DBNinja.useOrderDiscount(o, d);
			DBNinja.addCustomer(c);
			DBNinja.completeOrder(o);
			alo = DBNinja.getOrders(false);
			o = DBNinja.getLastOrder();
			alo = DBNinja.getOrdersByDate("01/01/1999");
			ald = DBNinja.getDiscountList();
			d = DBNinja.findDiscountByName("Discount");
			alc = DBNinja.getCustomerList();
			c = DBNinja.findCustomerByPhone("0000000000");
			alt = DBNinja.getToppingList();
			t = DBNinja.findToppingByName("Topping");
			DBNinja.addToInventory(t, 1000.0);
			v = DBNinja.getBaseCustPrice("size", "crust");
			v = DBNinja.getBaseBusPrice("size", "crust");
			DBNinja.printInventory();
			DBNinja.printToppingPopReport();
			DBNinja.printProfitByPizzaReport();
			DBNinja.printProfitByOrderType();
			s = DBNinja.getCustomerName(0);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

}
